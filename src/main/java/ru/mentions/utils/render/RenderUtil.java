package ru.mentions.utils.render;

import java.awt.Color;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.joml.Matrix4f;
import ru.mentions.Client;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public final class RenderUtil implements IMinecraft {
   @Generated
   private RenderUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Environment(EnvType.CLIENT)
   public static final class Blur {
      public static void blur(float x, float y, float width, float height, float alpha, int tintColor) {
         Draw.blur(x, y, width, height, alpha, tintColor);
      }

      public static void blur(float x, float y, float width, float height, float alpha, float cornerRadius, int tintColor) {
         Draw.blur(x, y, width, height, alpha, cornerRadius, tintColor);
      }

      public static void blur(
         float x, float y, float width, float height, float alpha, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor
      ) {
         Draw.blur(x, y, width, height, alpha, topLeft, topRight, bottomRight, bottomLeft, tintColor);
      }

      public static void glass(
         float x, float y, float width, float height, float alpha, float radius, int tintColor, float distortion, float waveSize, float edgeLight, float shine
      ) {
         float[] radii = new float[]{radius, radius, radius, radius};
         Client.get().render2D().getBlurPipeline().drawGlassBlur(x, y, width, height, alpha, radii, tintColor, distortion, waveSize, edgeLight, shine);
      }

      public static void glass(float x, float y, float width, float height, float alpha, float radius, float distortion, float waveSize) {
         float[] radii = new float[]{radius, radius, radius, radius};
         Client.get().render2D().getBlurPipeline().drawGlassBlur(x, y, width, height, alpha, radii, 0, distortion, waveSize, 0.0F, 0.0F);
      }

      public static void glass(
         float x,
         float y,
         float width,
         float height,
         float alpha,
         float topLeft,
         float topRight,
         float bottomRight,
         float bottomLeft,
         int tintColor,
         float distortion,
         float waveSize,
         float edgeLight,
         float shine
      ) {
         float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
         Client.get().render2D().getBlurPipeline().drawGlassBlur(x, y, width, height, alpha, radii, tintColor, distortion, waveSize, edgeLight, shine);
      }

      @Generated
      private Blur() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Images {
      public static class_2960 urlIdentifier(String url) {
         return UrlImageTexture.getIdentifier(url);
      }

      public static boolean isUrlLoaded(String url) {
         return UrlImageTexture.get(url).isLoaded();
      }

      public static void url(String url, float x, float y, float width, float height, int color) {
         url(url, x, y, width, height, 1.0F, 0.0F, color);
      }

      public static void url(String url, float x, float y, float width, float height, float smoothness, int color) {
         url(url, x, y, width, height, smoothness, 0.0F, color);
      }

      public static void url(String url, float x, float y, float width, float height, float smoothness, float radius, int color) {
         class_2960 id = UrlImageTexture.getIdentifier(url);
         if (id != null) {
            texture(id, x, y, width, height, smoothness, radius, color);
         }
      }

      public static void texture(class_2960 id, float x, float y, float width, float height, int color) {
         Draw.texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, 1.0F, 0.0F);
      }

      public static void texture(class_2960 id, float x, float y, float width, float height, float smoothness, int color) {
         Draw.texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, 0.0F);
      }

      public static void texture(class_2960 id, float x, float y, float width, float height, float smoothness, float radius, int color) {
         Draw.texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, radius);
      }

      public static void texture(class_2960 id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color) {
         Draw.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, 0.0F);
      }

      public static void texture(class_2960 id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float radius) {
         Draw.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, radius);
      }

      public static void texture(
         class_2960 id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float smoothness, float radius
      ) {
         Draw.texture(id, x, y, width, height, u0, v0, u1, v1, color, smoothness, radius);
      }

      public static void drawTexture(
         class_332 context,
         class_2960 id,
         float x,
         float y,
         float width,
         float height,
         float u,
         float v,
         float regionWidth,
         float regionHeight,
         float textureWidth,
         float textureHeight,
         int color
      ) {
         drawTexture(context, id, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, color, 0.0F);
      }

      public static void drawTexture(
         class_332 context,
         class_2960 id,
         float x,
         float y,
         float width,
         float height,
         float u,
         float v,
         float regionWidth,
         float regionHeight,
         float textureWidth,
         float textureHeight,
         int color,
         float radius
      ) {
         float u0 = u / textureWidth;
         float v0 = v / textureHeight;
         float u1 = (u + regionWidth) / textureWidth;
         float v1 = (v + regionHeight) / textureHeight;
         Draw.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, radius);
      }

      @Generated
      private Images() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Render2D {
      public static void outline(float x, float y, float width, float height, float thickness, int color) {
         Draw.outline(x, y, width, height, thickness, color);
      }

      public static void outline(float x, float y, float width, float height, float thickness, int color, float radius) {
         Draw.outline(x, y, width, height, thickness, color, radius);
      }

      public static void outline(
         float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft
      ) {
         Draw.outline(x, y, width, height, thickness, color, topLeft, topRight, bottomRight, bottomLeft);
      }

      public static void glassOutline(float x, float y, float width, float height, float thickness, float radius, float alpha, float shine) {
         Draw.glassOutline(x, y, width, height, thickness, radius, alpha, shine);
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
         Draw.glassOutline(x, y, width, height, thickness, topLeft, topRight, bottomRight, bottomLeft, alpha, shine);
      }

      public static void rect(float x, float y, float width, float height, int color) {
         Draw.rect(x, y, width, height, color);
      }

      public static void rect(float x, float y, float width, float height, int color, float radius) {
         Draw.rect(x, y, width, height, color, radius);
      }

      public static void rect(float x, float y, float width, float height, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {
         Draw.rect(x, y, width, height, color, topLeft, topRight, bottomRight, bottomLeft);
      }

      public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength) {
      }

      public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength, float softness) {
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
      }

      public static void clientRect(float x, float y, float width, float height, float alphaPC, float radius) {
         int color = ColorUtil.getColor(255, alphaPC);
         gradientRect(
            x,
            y,
            width,
            height,
            new int[]{
               new Color(24, 25, 31, ColorUtil.alpha(color)).getRGB(),
               new Color(30, 32, 37, ColorUtil.alpha(color)).getRGB(),
               new Color(14, 15, 21, ColorUtil.alpha(color)).getRGB(),
               new Color(13, 14, 20, ColorUtil.alpha(color)).getRGB()
            },
            radius
         );
      }

      public static void gradientRect(float x, float y, float width, float height, int[] colors, float radius) {
         Draw.gradientRect(x, y, width, height, colors, radius);
      }

      public static void gradientRect(
         float x, float y, float width, float height, int[] colors, float topLeft, float topRight, float bottomRight, float bottomLeft
      ) {
         Draw.gradientRect(x, y, width, height, colors, topLeft, topRight, bottomRight, bottomLeft);
      }

      public static void roundedCircleProgress(
         class_332 context, float centerX, float centerY, float radius, float thickness, float progress, int backgroundColor, int startColor, int endColor
      ) {
         progress = Math.max(0.0F, Math.min(1.0F, progress));
         if (!(radius <= 0.0F) && !(thickness <= 0.0F)) {
            ru.mentions.utils.render.Render2D render2D = Client.get().render2D();
            render2D.flushAll();
            render2D.getCircleProgressPipeline().draw(centerX, centerY, radius, thickness, 1.0F, backgroundColor);
            if (progress > 0.0F) {
               render2D.getCircleProgressPipeline().draw(centerX, centerY, radius, thickness, progress, ColorUtil.overCol(startColor, endColor, 0.65F));
            }

            render2D.getCircleProgressPipeline().flush();
         }
      }

      @Generated
      private Render2D() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class Render3D {
      public static final Matrix4f lastProjMat = new Matrix4f();
      public static final Matrix4f lastModMat = new Matrix4f();
      public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

      @Generated
      private Render3D() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class RenderPlayer3DMatrix {
      @Generated
      private RenderPlayer3DMatrix() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
   }
}
