package ru.mentions.cosmetics.render;

import org.joml.Matrix4f;

public final class PreviewTransform {
   private static final float FILL = 0.9F;
   private static final float PADDING = 1.25F;

   private PreviewTransform() {
   }

   public static Matrix4f frame(float[] box, float centerX, float centerY, float halfSize, float width, float height, float yaw) {
      float x = (box[3] - box[0]) * 0.5F;
      float y = (box[4] - box[1]) * 0.5F;
      float z = (box[5] - box[2]) * 0.5F;
      float radius = Math.max(0.01F, (float)Math.sqrt((double)(x * x + y * y + z * z)) * 1.25F);
      return new Matrix4f()
         .translate(centerX / width * 2.0F - 1.0F, 1.0F - centerY / height * 2.0F, 0.5F)
         .scale(0.9F * halfSize * 2.0F / (width * radius), 0.9F * halfSize * 2.0F / (height * radius), 0.45F / radius)
         .rotateX(-0.18F)
         .rotateY((float)Math.toRadians((double)yaw))
         .translate(-(box[0] + box[3]) * 0.5F, -(box[1] + box[4]) * 0.5F, -(box[2] + box[5]) * 0.5F);
   }
}
