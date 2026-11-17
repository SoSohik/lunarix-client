package ru.mentions.utils.aura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NeuroRotation {
   public static final int IN = 8;
   public static final int H1 = 24;
   public static final int H2 = 16;
   public static final int OUT = 2;
   public static final float YAW_ERR_NORM = 180.0F;
   public static final float PITCH_ERR_NORM = 90.0F;
   public static final float DIST_NORM = 6.0F;
   public static final float SPEED_NORM = 0.3F;
   public static final float DELTA_YAW_NORM = 20.0F;
   public static final float DELTA_PITCH_NORM = 10.0F;
   public static final int MIN_SAMPLES = 300;
   private static final int MAX_SAMPLES = 40000;
   private static final Path MODEL_PATH = Path.of("C:/lunarix/client1_21_11/neuro_rotation.json");
   private static final Path SAMPLES_PATH = Path.of("C:/lunarix/client1_21_11/neuro_samples.bin");
   private static final Gson GSON = new GsonBuilder().create();
   private final float[][] w1 = new float[24][8];
   private final float[] b1 = new float[24];
   private final float[][] w2 = new float[16][24];
   private final float[] b2 = new float[16];
   private final float[][] w3 = new float[2][16];
   private final float[] b3 = new float[2];
   private final List<float[]> samples = new ArrayList<>();
   private volatile boolean trained = false;
   private volatile boolean training = false;

   public NeuroRotation() {
      this.reinit();
      this.load();
      this.loadSamples();
   }

   private void reinit() {
      Random r = new Random();
      initLayer(this.w1, this.b1, r);
      initLayer(this.w2, this.b2, r);
      initLayer(this.w3, this.b3, r);
   }

   private boolean hasInvalidWeights() {
      return invalid(this.w1) || invalid(this.b1) || invalid(this.w2) || invalid(this.b2) || invalid(this.w3) || invalid(this.b3);
   }

   private static boolean invalid(float[][] m) {
      for (float[] row : m) {
         if (invalid(row)) {
            return true;
         }
      }

      return false;
   }

   private static boolean invalid(float[] v) {
      for (float f : v) {
         if (Float.isNaN(f) || Float.isInfinite(f)) {
            return true;
         }
      }

      return false;
   }

   private static void initLayer(float[][] w, float[] b, Random r) {
      float scale = (float)Math.sqrt(2.0 / (double)w[0].length);

      for (float[] row : w) {
         for (int i = 0; i < row.length; i++) {
            row[i] = (float)(r.nextGaussian() * (double)scale);
         }
      }

      Arrays.fill(b, 0.0F);
   }

   public boolean isTrained() {
      return this.trained;
   }

   public boolean isTraining() {
      return this.training;
   }

   public int getSampleCount() {
      List<float[]> list = this.samples;
      synchronized (list) {
         return this.samples.size();
      }
   }

   public boolean hasMemory() {
      List<float[]> list = this.samples;
      synchronized (list) {
         return !this.samples.isEmpty();
      }
   }

   public void clearSamples() {
      List<float[]> list = this.samples;
      synchronized (list) {
         this.samples.clear();
      }

      try {
         Files.deleteIfExists(SAMPLES_PATH);
      } catch (Exception var4) {
      }
   }

   public void record(float[] features, float dYaw, float dPitch) {
      if (features.length == 8) {
         if (!invalid(features) && !Float.isNaN(dYaw) && !Float.isNaN(dPitch) && !Float.isInfinite(dYaw) && !Float.isInfinite(dPitch)) {
            if (!(Math.abs(dYaw) > 120.0F) && !(Math.abs(dPitch) > 90.0F)) {
               float[] s = new float[10];
               System.arraycopy(features, 0, s, 0, 8);
               s[8] = clamp(dYaw / 20.0F, -1.5F, 1.5F);
               s[9] = clamp(dPitch / 10.0F, -1.5F, 1.5F);
               List<float[]> list = this.samples;
               synchronized (list) {
                  if (this.samples.size() >= 40000) {
                     this.samples.remove(0);
                  }

                  this.samples.add(s);
               }
            }
         }
      }
   }

   public float[] predict(float[] features) {
      float[] remembered = this.predictFromMemory(features);
      if (remembered != null) {
         return remembered;
      } else {
         float[] h1 = new float[24];
         float[] h2 = new float[16];
         float[] out = new float[2];
         this.forward(features, h1, h2, out);
         return !Float.isNaN(out[0]) && !Float.isNaN(out[1]) && !Float.isInfinite(out[0]) && !Float.isInfinite(out[1])
            ? new float[]{clamp(out[0] * 20.0F, -30.0F, 30.0F), clamp(out[1] * 10.0F, -18.0F, 18.0F)}
            : new float[]{0.0F, 0.0F};
      }
   }

   private float[] predictFromMemory(float[] features) {
      if (features.length != 8) {
         return null;
      } else {
         List<float[]> list = this.samples;
         ArrayList<float[]> data;
         synchronized (list) {
            if (this.samples.isEmpty()) {
               return null;
            }

            data = new ArrayList<>(this.samples);
         }

         int k = Math.min(5, data.size());
         float[] bestDist = new float[k];
         float[] bestYaw = new float[k];
         float[] bestPitch = new float[k];
         Arrays.fill(bestDist, Float.MAX_VALUE);

         for (float[] sample : data) {
            float dist = sampleDistance(features, sample);

            for (int i = 0; i < k; i++) {
               if (!(dist >= bestDist[i])) {
                  for (int j = k - 1; j > i; j--) {
                     bestDist[j] = bestDist[j - 1];
                     bestYaw[j] = bestYaw[j - 1];
                     bestPitch[j] = bestPitch[j - 1];
                  }

                  bestDist[i] = dist;
                  bestYaw[i] = sample[8] * 20.0F;
                  bestPitch[i] = sample[9] * 10.0F;
                  break;
               }
            }
         }

         float yaw = 0.0F;
         float pitch = 0.0F;
         float weightSum = 0.0F;

         for (int ix = 0; ix < k; ix++) {
            if (bestDist[ix] != Float.MAX_VALUE) {
               float weight = 1.0F / (1.0E-4F + bestDist[ix] * bestDist[ix]);
               yaw += bestYaw[ix] * weight;
               pitch += bestPitch[ix] * weight;
               weightSum += weight;
            }
         }

         return weightSum <= 0.0F ? null : new float[]{clamp(yaw / weightSum, -30.0F, 30.0F), clamp(pitch / weightSum, -18.0F, 18.0F)};
      }
   }

   private static float sampleDistance(float[] features, float[] sample) {
      float dist = 0.0F;

      for (int i = 0; i < 8; i++) {
         float weight = switch (i) {
            case 0 -> 4.0F;
            case 1 -> 3.0F;
            default -> 1.0F;
            case 6 -> 1.7F;
            case 7 -> 1.4F;
         };
         float d = features[i] - sample[i];
         dist += d * d * weight;
      }

      return dist;
   }

   private void forward(float[] in, float[] h1, float[] h2, float[] out) {
      for (int i = 0; i < 24; i++) {
         float sum = this.b1[i];

         for (int j = 0; j < 8; j++) {
            sum += this.w1[i][j] * in[j];
         }

         h1[i] = (float)Math.tanh((double)sum);
      }

      for (int var12 = 0; var12 < 16; var12++) {
         float sum = this.b2[var12];

         for (int j = 0; j < 24; j++) {
            sum += this.w2[var12][j] * h1[j];
         }

         h2[var12] = (float)Math.tanh((double)sum);
      }

      for (int var13 = 0; var13 < 2; var13++) {
         float sum = this.b3[var13];

         for (int j = 0; j < 16; j++) {
            sum += this.w3[var13][j] * h2[j];
         }

         out[var13] = sum;
      }
   }

   public boolean trainAsync(BiConsumer<Boolean, Integer> onDone) {
      if (this.training) {
         return false;
      } else {
         List<float[]> list = this.samples;
         ArrayList<float[]> data;
         synchronized (list) {
            data = new ArrayList<>(this.samples);
         }

         this.training = true;
         Thread t = new Thread(() -> {
            boolean ok = false;

            try {
               this.saveSamples(data);
               if (data.size() >= 300) {
                  if (this.hasInvalidWeights()) {
                     this.reinit();
                  }

                  this.train(data);
                  if (this.hasInvalidWeights()) {
                     this.reinit();
                     this.trained = false;
                  } else {
                     this.trained = true;
                     this.save();
                     ok = true;
                  }
               }
            } catch (Throwable var8) {
               this.reinit();
               this.trained = false;
            } finally {
               this.training = false;
            }

            if (onDone != null) {
               onDone.accept(ok, data.size());
            }
         }, "neuro-rotation-train");
         t.setDaemon(true);
         t.start();
         return true;
      }
   }

   private void train(List<float[]> data) {
      int epochs = 60;
      float lr = 0.004F;
      float momentum = 0.85F;
      float clip = 3.0F;
      float[][] vw1 = new float[24][8];
      float[] vb1 = new float[24];
      float[][] vw2 = new float[16][24];
      float[] vb2 = new float[16];
      float[][] vw3 = new float[2][16];
      float[] vb3 = new float[2];
      float[] h1 = new float[24];
      float[] h2 = new float[16];
      float[] out = new float[2];
      float[] dOut = new float[2];
      float[] dH2 = new float[16];
      float[] dH1 = new float[24];
      Random rnd = new Random();
      ArrayList<float[]> shuffled = new ArrayList<>(data);

      for (int epoch = 0; epoch < epochs; epoch++) {
         Collections.shuffle(shuffled, rnd);
         float curLr = lr * (1.0F - (float)epoch / (float)epochs * 0.9F);

         for (float[] s : shuffled) {
            this.forward(s, h1, h2, out);

            for (int i2 = 0; i2 < 2; i2++) {
               dOut[i2] = clamp(out[i2] - s[8 + i2], -clip, clip);
            }

            for (int var32 = 0; var32 < 2; var32++) {
               for (int j2 = 0; j2 < 16; j2++) {
                  vw3[var32][j2] = momentum * vw3[var32][j2] - curLr * dOut[var32] * h2[j2];
                  float[] fArray = this.w3[var32];
                  fArray[j2] += vw3[var32][j2];
               }

               vb3[var32] = momentum * vb3[var32] - curLr * dOut[var32];
               this.b3[var32] = this.b3[var32] + vb3[var32];
            }

            for (int j = 0; j < 16; j++) {
               float g = 0.0F;

               for (int i = 0; i < 2; i++) {
                  g += dOut[i] * this.w3[i][j];
               }

               dH2[j] = clamp(g * (1.0F - h2[j] * h2[j]), -clip, clip);
            }

            for (int var33 = 0; var33 < 16; var33++) {
               for (int j3 = 0; j3 < 24; j3++) {
                  vw2[var33][j3] = momentum * vw2[var33][j3] - curLr * dH2[var33] * h1[j3];
                  float[] fArray = this.w2[var33];
                  fArray[j3] += vw2[var33][j3];
               }

               vb2[var33] = momentum * vb2[var33] - curLr * dH2[var33];
               this.b2[var33] = this.b2[var33] + vb2[var33];
            }

            for (int var31 = 0; var31 < 24; var31++) {
               float g = 0.0F;

               for (int i = 0; i < 16; i++) {
                  g += dH2[i] * this.w2[i][var31];
               }

               dH1[var31] = clamp(g * (1.0F - h1[var31] * h1[var31]), -clip, clip);
            }

            for (int var34 = 0; var34 < 24; var34++) {
               for (int j4 = 0; j4 < 8; j4++) {
                  vw1[var34][j4] = momentum * vw1[var34][j4] - curLr * dH1[var34] * s[j4];
                  float[] fArray = this.w1[var34];
                  fArray[j4] += vw1[var34][j4];
               }

               vb1[var34] = momentum * vb1[var34] - curLr * dH1[var34];
               this.b1[var34] = this.b1[var34] + vb1[var34];
            }
         }
      }
   }

   private void saveSamples(List<float[]> data) {
      try {
         Files.createDirectories(SAMPLES_PATH.getParent());

         try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(SAMPLES_PATH)))) {
            out.writeInt(10);
            out.writeInt(data.size());

            for (float[] s : data) {
               for (int i = 0; i < 10; i++) {
                  out.writeFloat(s[i]);
               }
            }
         }
      } catch (Exception var8) {
      }
   }

   private void loadSamples() {
      if (Files.exists(SAMPLES_PATH)) {
         try {
            label58: {
               try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(SAMPLES_PATH)))) {
                  int width = in.readInt();
                  if (width == 10) {
                     int count = Math.min(in.readInt(), 40000);
                     List<float[]> list = this.samples;
                     synchronized (list) {
                        this.samples.clear();

                        for (int c = 0; c < count; c++) {
                           float[] s = new float[10];

                           for (int i = 0; i < s.length; i++) {
                              s[i] = in.readFloat();
                           }

                           this.samples.add(s);
                        }
                        break label58;
                     }
                  }
               }

               return;
            }
         } catch (Exception var13) {
         }
      }
   }

   public void save() {
      if (!this.hasInvalidWeights()) {
         try {
            Files.createDirectories(MODEL_PATH.getParent());
            JsonObject root = new JsonObject();
            root.add("w1", matrixToJson(this.w1));
            root.add("b1", vectorToJson(this.b1));
            root.add("w2", matrixToJson(this.w2));
            root.add("b2", vectorToJson(this.b2));
            root.add("w3", matrixToJson(this.w3));
            root.add("b3", vectorToJson(this.b3));

            try (BufferedWriter w = Files.newBufferedWriter(MODEL_PATH, StandardCharsets.UTF_8)) {
               GSON.toJson(root, w);
            }
         } catch (Exception var7) {
         }
      }
   }

   public void load() {
      if (Files.exists(MODEL_PATH)) {
         try {
            try (BufferedReader r = Files.newBufferedReader(MODEL_PATH, StandardCharsets.UTF_8)) {
               JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
               jsonToMatrix(root.getAsJsonArray("w1"), this.w1);
               jsonToVector(root.getAsJsonArray("b1"), this.b1);
               jsonToMatrix(root.getAsJsonArray("w2"), this.w2);
               jsonToVector(root.getAsJsonArray("b2"), this.b2);
               jsonToMatrix(root.getAsJsonArray("w3"), this.w3);
               jsonToVector(root.getAsJsonArray("b3"), this.b3);
               if (!this.hasInvalidWeights()) {
                  this.trained = true;
                  return;
               }

               this.reinit();
            }
         } catch (Exception var6) {
            this.reinit();
         }
      }
   }

   private static JsonArray matrixToJson(float[][] m) {
      JsonArray arr = new JsonArray();

      for (float[] row : m) {
         arr.add(vectorToJson(row));
      }

      return arr;
   }

   private static JsonArray vectorToJson(float[] v) {
      JsonArray arr = new JsonArray();

      for (float f : v) {
         arr.add(f);
      }

      return arr;
   }

   private static void jsonToMatrix(JsonArray arr, float[][] m) {
      for (int i = 0; i < m.length; i++) {
         jsonToVector(arr.get(i).getAsJsonArray(), m[i]);
      }
   }

   private static void jsonToVector(JsonArray arr, float[] v) {
      for (int i = 0; i < v.length; i++) {
         v[i] = arr.get(i).getAsFloat();
      }
   }

   private static float clamp(float v, float min, float max) {
      return v < min ? min : Math.min(v, max);
   }
}
