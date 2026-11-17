package ru.mentions.utils.math;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import org.joml.Matrix4f;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class MathUtil implements IMinecraft {
   private static final double[] TRIG_TABLE = new double[65536];
   private static float[] lastSmoothRandom;
   private static float[] lastSmoothTarget;
   private static long lastSmoothTime = 0L;
   public static double PI2 = Math.PI * 2;
   private static final Random RANDOM = new Random();
   public static final Matrix4f lastProjMat = new Matrix4f();
   public static final Matrix4f lastModMat = new Matrix4f();
   public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

   public static int floorNearestMulN(int x, int n) {
      return n * (int)Math.floor((double)x / (double)n);
   }

   public static double deltaTime() {
      return mc.method_47599() > 0 ? 1.0 / (double)mc.method_47599() : 1.0;
   }

   public static float invertScaleValue(float value, float minInput, float maxInput, float minOutput, float maxOutput) {
      if (maxInput - minInput == 0.0F) {
         throw new IllegalArgumentException("Диапазон входных значений не может быть равен нулю.");
      } else {
         float scaledValue = (maxInput - value) / (maxInput - minInput) * (maxOutput - minOutput) + minOutput;
         return Math.max(minOutput, Math.min(maxOutput, scaledValue));
      }
   }

   public static float smoothRandom(float min, float max, float smoothness) {
      if (lastSmoothRandom == null) {
         lastSmoothRandom = new float[]{(min + max) / 2.0F};
         lastSmoothTarget = new float[]{randomValue(min, max)};
      }

      long currentTime;
      if ((double)((currentTime = System.currentTimeMillis()) - lastSmoothTime) > 250.0 + Math.random() * 300.0) {
         lastSmoothTarget[0] = randomValue(min, max);
         lastSmoothTime = currentTime;
      }

      lastSmoothRandom[0] = lastSmoothRandom[0] + (lastSmoothTarget[0] - lastSmoothRandom[0]) * smoothness;
      return lastSmoothRandom[0];
   }

   public static class_243 cosSin(int i, int size, double width) {
      int index = Math.min(i, size);
      float cos = (float)(Math.cos((double)index * PI2 / (double)size) * width);
      float sin = (float)(-Math.sin((double)index * PI2 / (double)size) * width);
      return new class_243((double)cos, 0.0, (double)sin);
   }

   public static float wrapLerp(float step, float input, float target) {
      return input + step * class_3532.method_15393(target - input);
   }

   public static double easeInOutQuadWave(double value) {
      value = (value > 0.5 ? 1.0 - value : value) * 2.0;
      value = value < 0.5 ? 2.0 * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 2.0) / 2.0;
      return value > 1.0 ? 1.0 : (value < 0.0 ? 0.0 : value);
   }

   public static float wrapAngleTo180_float(float p_76142_0_) {
      p_76142_0_ %= 360.0F;
      if (p_76142_0_ >= 180.0F) {
         p_76142_0_ -= 360.0F;
      }

      if (p_76142_0_ < -180.0F) {
         p_76142_0_ += 360.0F;
      }

      return p_76142_0_;
   }

   public static double sin(double radians) {
      int index = (int)(radians * 10430.378350470453) & 65535;
      return TRIG_TABLE[index];
   }

   public static double cos(double radians) {
      int index = (int)(radians * 10430.378350470453 + 16384.0) & 65535;
      return TRIG_TABLE[index];
   }

   public static float random1(float min, float max) {
      return (float)(Math.random() * (double)(max - min) + (double)min);
   }

   public static float random(double min, double max) {
      return (float)(min + (max - min) * Math.random());
   }

   public static float randomSin(double min, double max, double time) {
      return (float)(Math.sin((double)System.currentTimeMillis() / time) * (double)randomLerp((float)min, (float)max));
   }

   public static float randomCos(double min, double max, double time) {
      return (float)(Math.cos((double)System.currentTimeMillis() / time) * (double)randomLerp((float)min, (float)max));
   }

   public static float randomGaussian(double min, double max) {
      double mean = (min + max) * 0.5;
      double deviation = (max - min) / 6.0;
      double value = mean + RANDOM.nextGaussian() * deviation;
      return (float)Math.max(min, Math.min(max, value));
   }

   public static float randomGaussianSmooth(double min, double max, double time) {
      double mean = (min + max) * 0.5;
      double deviation = (max - min) / 6.0;
      double noise = RANDOM.nextGaussian() * deviation;
      double smooth = Math.sin((double)System.currentTimeMillis() / time);
      double value = mean + noise * smooth;
      return (float)Math.max(min, Math.min(max, value));
   }

   public static double lerp(double delta, double start, double end) {
      return start + delta * (end - start);
   }

   public static float lerp(float delta, float start, float end) {
      return start + delta * (end - start);
   }

   public static float fast(float end, float start, float multiple) {
      return (1.0F - class_3532.method_15363((float)(deltaTime() * (double)multiple), 0.0F, 1.0F)) * end
         + class_3532.method_15363((float)(deltaTime() * (double)multiple), 0.0F, 1.0F) * start;
   }

   public static double clamps(double min, double max, double value) {
      return Math.max(min, Math.min(max, value));
   }

   public static float randomLerp(float min, float max) {
      return Interpolator.lerp(max, min, (double)new SecureRandom().nextFloat());
   }

   public static double getRandomNumberBetween(double min, double max) {
      return Math.random() * (max - min) + min;
   }

   public static float interpolate(double oldValue, double newValue, double interpolationValue) {
      return (float)(oldValue + (newValue - oldValue) * interpolationValue);
   }

   public static class_243 interpolate(class_243 prevPos, class_243 pos) {
      return new class_243(
         interpolate(prevPos.field_1352, pos.field_1352), interpolate(prevPos.field_1351, pos.field_1351), interpolate(prevPos.field_1350, pos.field_1350)
      );
   }

   public static double interpolate(double prev, double orig) {
      return lerp((double)mc.method_61966().method_60637(false), prev, orig);
   }

   public static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public static float clamp(float value, float min, float max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   public static int randomInt(int min, int max) {
      return min + (int)(Math.random() * (double)(max - min + 1));
   }

   public static double randomValue(double min, double max) {
      validateRange(min, max);
      return min + ThreadLocalRandom.current().nextDouble() * (max - min);
   }

   public static float randomValue(float min, float max) {
      validateRange((double)min, (double)max);
      return min + ThreadLocalRandom.current().nextFloat() * (max - min);
   }

   private static void validateRange(double min, double max) {
      if (max < min) {
         throw new IllegalArgumentException("max не может быть меньше min.");
      }
   }

   public static float textScrolling(float textWidth) {
      int speed = 6000;
      return (float)class_3532.method_15350((double)(System.currentTimeMillis() % (long)speed) * Math.PI / (double)speed, 0.0, 1.0) * textWidth;
   }

   public static int clampMin(int value, int min) {
      return value < min ? min : value;
   }

   public static float clampMin(float value, float min) {
      return value < min ? min : value;
   }

   public static double clampMin(double value, double min) {
      return value < min ? min : value;
   }

   public static boolean isInRegion(int mouseX, int mouseY, float x, float y, float width, float height) {
      return (float)mouseX >= x && (float)mouseY >= y && (float)mouseX <= x + width && (float)mouseY <= y + height;
   }

   public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
      return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
   }

   public static float step(float value, float step) {
      return (float)Math.round(value / step) * step;
   }

   public static double round(double value, int increment) {
      double num = Math.pow(10.0, (double)increment);
      return (double)Math.round(value * num) / num;
   }

   public static float round(float value, int decimals) {
      float multiplier = (float)Math.pow(10.0, (double)decimals);
      return (float)Math.round(value * multiplier) / multiplier;
   }

   public static float round(float value, float increment) {
      return (float)Math.round(value / increment) * increment;
   }

   public static double getScaleFactor() {
      return (double)mc.method_22683().method_4495();
   }

   @Generated
   private MathUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      for (int i = 0; i < 65536; i++) {
         TRIG_TABLE[i] = Math.sin((double)i * (Math.PI * 2) / 65536.0);
      }
   }
}
