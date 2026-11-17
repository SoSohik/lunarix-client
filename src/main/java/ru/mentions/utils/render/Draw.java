package ru.mentions.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import ru.mentions.Client;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public final class Draw {
   private static final float GLASS_DISTORTION = 125.0F;
   private static final float GLASS_WAVE_SIZE = 75.0F;

   private Draw() {
   }

   private static Render2D r2d() {
      return Client.get().render2D();
   }

   public static void flush() {
      r2d().flushAll();
   }

   public static void blur(float x, float y, float width, float height, float alpha, int tintColor) {
      blur(x, y, width, height, alpha, 0.0F, 0.0F, 0.0F, 0.0F, tintColor);
   }

   public static void blur(float x, float y, float width, float height, float alpha, float radius, int tintColor) {
      blur(x, y, width, height, alpha, radius, radius, radius, radius, tintColor);
   }

   public static void blur(
      float x, float y, float width, float height, float alpha, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor
   ) {
      float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      BlurPipeline pipeline = r2d().getBlurPipeline();
      if (ThemeColor.getDistortion()) {
         pipeline.drawGlassBlur(x, y, width, height, alpha, radii, tintColor, 125.0F, 75.0F, 0.0F, 0.0F);
      } else {
         pipeline.drawBlur(x, y, width, height, alpha, radii, tintColor);
      }
   }

   public static void glass(
      float x, float y, float width, float height, float alpha, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor
   ) {
      float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      r2d().getBlurPipeline().drawGlassBlur(x, y, width, height, alpha, radii, tintColor, 125.0F, 75.0F, 0.0F, 0.0F);
   }

   public static void rect(float x, float y, float width, float height, int color) {
      rect(x, y, width, height, color, 0.0F);
   }

   public static void rect(float x, float y, float width, float height, int color, float radius) {
      rect(x, y, width, height, color, radius, radius, radius, radius);
   }

   public static void rect(float x, float y, float width, float height, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {
      int[] colors = ColorUtil.solid(color);
      float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      r2d().getRectPipeline().drawRect(x, y, width, height, colors, radii);
   }

   public static void gradientRect(float x, float y, float width, float height, int[] colors, float radius) {
      gradientRect(x, y, width, height, colors, radius, radius, radius, radius);
   }

   public static void gradientRect(
      float x, float y, float width, float height, int[] colors, float topLeft, float topRight, float bottomRight, float bottomLeft
   ) {
      float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      r2d().getRectPipeline().drawRect(x, y, width, height, colors, radii);
   }

   public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength) {
      glow(x, y, width, height, color, radius, radius, radius, radius, glowSize, strength, 1.0F);
   }

   public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength, float softness) {
      glow(x, y, width, height, color, radius, radius, radius, radius, glowSize, strength, softness);
   }

   public static void glow(
      float x,
      float y,
      float width,
      float height,
      int color,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      float glowSize,
      float strength,
      float softness
   ) {
      float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      r2d().getRectPipeline().drawGlow(x, y, width, height, color, radii, glowSize, strength, softness);
   }

   public static void outline(float x, float y, float width, float height, float thickness, int color) {
      outline(x, y, width, height, thickness, color, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   public static void outline(float x, float y, float width, float height, float thickness, int color, float radius) {
      outline(x, y, width, height, thickness, color, radius, radius, radius, radius);
   }

   public static void outline(
      float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft
   ) {
      int[] colors = ColorUtil.solid8(color);
      float[] thicknesses = new float[]{thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness};
      float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      r2d().getOutlinePipeline().drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0F);
   }

   public static void glassOutline(float x, float y, float width, float height, float thickness, float radius, float alpha, float shine) {
      glassOutline(x, y, width, height, thickness, radius, radius, radius, radius, alpha, shine);
   }

   public static void glassOutline(
      float x,
      float y,
      float width,
      float height,
      float thickness,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      float alpha,
      float shine
   ) {
      float base = Math.max(0.0F, alpha);
      float hot = Math.max(0.0F, shine);
      int[] colors = new int[]{
         glassColor(base * 0.42F),
         glassColor(base * (0.55F + hot * 0.3F)),
         glassColor(base * (0.28F + hot * 0.1F)),
         glassColor(base * 0.24F),
         glassColor(base * (0.34F + hot * 0.16F)),
         glassColor(base * (0.48F + hot * 0.24F)),
         glassColor(base * 0.3F),
         glassColor(base * (0.62F + hot * 0.38F))
      };
      float[] thicknesses = new float[]{
         thickness,
         thickness + hot * 0.18F,
         thickness * 0.86F,
         thickness * 0.78F,
         thickness * 0.9F,
         thickness + hot * 0.16F,
         thickness * 0.84F,
         thickness + hot * 0.24F
      };
      float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      r2d().getOutlinePipeline().drawOutline(x, y, width, height, colors, thicknesses, radii, 1.35F);
   }

   private static int glassColor(float alpha) {
      return ColorUtil.getColor(255, Math.min(alpha, 1.0F));
   }

   public static void texture(class_2960 id, float x, float y, float width, float height, int color) {
      texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, 1.0F, 0.0F);
   }

   public static void texture(class_2960 id, float x, float y, float width, float height, float smoothness, float radius, int color) {
      texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, radius);
   }

   public static void texture(
      class_2960 id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float smoothness, float radius
   ) {
      int[] colors = new int[]{color, color, color, color};
      float[] radii = new float[]{radius, radius, radius, radius};
      r2d().getTexturePipeline().drawTexture(id, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness);
   }
}
