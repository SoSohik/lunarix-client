package ru.mentions.cosmetics.render;

import net.minecraft.class_4588;
import net.minecraft.class_4587.class_4665;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mentions.cosmetics.CosmeticEntry;

public final class CapeRenderer {
   private static final int SEGMENTS_X = 6;
   private static final int SEGMENTS_Y = 10;
   private static final float[] X = new float[77];
   private static final float[] Y = new float[77];
   private static final float[] Z = new float[77];
   private static final Vector3f NORMAL = new Vector3f();
   private static final Vector3f CORNER = new Vector3f();

   private CapeRenderer() {
   }

   public static void render(
      CosmeticEntry cosmetic, class_4665 world, class_4588 consumer, int light, int tint, CapeRenderer.Shape shape, long now, float movement
   ) {
      double time = (double)now / 1000.0;
      float sway = (float)Math.sin(time * (double)shape.waveSpeed) * shape.wave
         + (float)Math.sin(time * (double)shape.waveSpeed * 0.61F + 1.7F) * shape.wave * 0.45F;
      float drag = shape.wave * (0.4F + Math.min(1.0F, movement * 4.0F)) * sway;
      buildGrid(shape, sway, drag);
      Matrix4f matrix = world.method_23761();
      NORMAL.set(0.0F, 0.0F, 1.0F);
      world.method_56821(NORMAL, NORMAL);
      int frame = frameOf(cosmetic, now);
      float vTop = (float)frame / (float)cosmetic.capeFrames;
      float vStep = 1.0F / (float)cosmetic.capeFrames;
      int stride = 7;

      for (int row = 0; row < 10; row++) {
         float v0 = vTop + vStep * (float)row / 10.0F;
         float v1 = vTop + vStep * (float)(row + 1) / 10.0F;

         for (int column = 0; column < 6; column++) {
            int topLeft = row * stride + column;
            int topRight = topLeft + 1;
            int bottomLeft = topLeft + stride;
            int bottomRight = bottomLeft + 1;
            float u0 = (float)column / 6.0F;
            float u1 = (float)(column + 1) / 6.0F;
            emit(consumer, matrix, light, tint, topLeft, u0, v0);
            emit(consumer, matrix, light, tint, topRight, u1, v0);
            emit(consumer, matrix, light, tint, bottomRight, u1, v1);
            emit(consumer, matrix, light, tint, bottomLeft, u0, v1);
         }
      }
   }

   private static void buildGrid(CapeRenderer.Shape shape, float sway, float drag) {
      int stride = 7;

      for (int row = 0; row <= 10; row++) {
         float t = (float)row / 10.0F;
         float half = shape.width * 0.5F * (1.0F + (shape.bottomRatio - 1.0F) * t);
         float fall = t * shape.length;
         float hem = (float)Math.sin((double)t * Math.PI) * shape.bend;

         for (int column = 0; column <= 6; column++) {
            float s = (float)column / 6.0F * 2.0F - 1.0F;
            int index = row * stride + column;
            X[index] = s * half;
            Y[index] = shape.topY + fall + (1.0F - Math.abs(s)) * hem;
            Z[index] = shape.backZ
               + (1.0F - s * s) * shape.bend
               + sway * t
               + drag * t * t
               + (float)Math.sin((double)(t * 6.0F + s * 2.0F)) * shape.wave * 0.5F * t;
         }
      }
   }

   private static void emit(class_4588 consumer, Matrix4f matrix, int light, int tint, int index, float u, float v) {
      CORNER.set(X[index], Y[index], Z[index]);
      matrix.transformPosition(CORNER);
      consumer.method_23919(CORNER.x, CORNER.y, CORNER.z, tint, u, v, 655360, light, NORMAL.x, NORMAL.y, NORMAL.z);
   }

   private static int frameOf(CosmeticEntry cosmetic, long now) {
      if (!cosmetic.hasCapeFrames()) {
         return 0;
      } else {
         long frameTime = (long)Math.max(1, cosmetic.capeFrameTicks) * 50L;
         return Math.floorMod(now / frameTime, cosmetic.capeFrames);
      }
   }

   public static final class Shape {
      public float width = 0.62F;
      public float length = 1.15F;
      public float topY = 0.42F;
      public float backZ = 0.16F;
      public float bend = 0.1F;
      public float wave = 0.055F;
      public float waveSpeed = 2.4F;
      public float bottomRatio = 1.0F;
   }
}
