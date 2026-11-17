package ru.mentions.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import ru.mentions.module.impl.misc.Optimization;

@Environment(EnvType.CLIENT)
public class RectPipeline extends UniformArrayPipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/rect");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/rect");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/rect");
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("RectData", class_10789.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final int MAX_RECTS = 64;
   private static final int RECT_SIZE = 224;
   private static final int UNIFORM_RING = 32;
   private final int[] colors9 = new int[9];

   public RectPipeline() {
      super("rect", PIPELINE, "RectData", 64, 224, 32);
   }

   @Override
   public int batchLayer() {
      return 0;
   }

   public void drawRect(float x, float y, float width, float height, int[] colors, float[] radii) {
      this.drawRect(x, y, width, height, colors, radii, 0.0F);
   }

   public void drawRect(float x, float y, float width, float height, int[] colors, float[] radii, float innerBlur) {
      class_310 client = class_310.method_1551();
      if (client.method_1522() != null) {
         this.convertTo9Colors(colors);
         this.appendRect(x, y, width, height, x, y, width, height, innerBlur, radii, 0.0F, 0.0F, 1.0F, 0);
         this.endItem();
      }
   }

   public void drawGlow(float x, float y, float width, float height, int color, float[] radii, float glowSize, float strength) {
      this.drawGlow(x, y, width, height, color, radii, glowSize, strength, 1.0F);
   }

   public void drawGlow(float x, float y, float width, float height, int color, float[] radii, float glowSize, float strength, float softness) {
      if (!Optimization.skipGlow()) {
         if (!(width <= 0.0F) && !(height <= 0.0F) && !(glowSize <= 0.0F) && !(strength <= 0.0F)) {
            class_310 client = class_310.method_1551();
            if (client.method_1522() != null) {
               for (int i = 0; i < 9; i++) {
                  this.colors9[i] = color;
               }

               float padding = Math.max(0.5F, glowSize);
               this.appendRect(
                  x - padding,
                  y - padding,
                  width + padding * 2.0F,
                  height + padding * 2.0F,
                  x,
                  y,
                  width,
                  height,
                  0.0F,
                  radii,
                  glowSize,
                  strength,
                  softness,
                  color
               );
               this.endItem();
            }
         }
      }
   }

   private void convertTo9Colors(int[] colors) {
      if (colors.length == 1) {
         for (int i = 0; i < 9; i++) {
            this.colors9[i] = colors[0];
         }
      } else if (colors.length == 4) {
         this.colors9[0] = colors[0];
         this.colors9[1] = this.blendColors(colors[0], colors[1]);
         this.colors9[2] = colors[1];
         this.colors9[3] = this.blendColors(colors[0], colors[3]);
         this.colors9[4] = this.blendColors(colors[0], colors[1], colors[2], colors[3]);
         this.colors9[5] = this.blendColors(colors[1], colors[2]);
         this.colors9[6] = colors[3];
         this.colors9[7] = this.blendColors(colors[3], colors[2]);
         this.colors9[8] = colors[2];
      } else if (colors.length >= 9) {
         System.arraycopy(colors, 0, this.colors9, 0, 9);
      } else {
         for (int i = 0; i < 9; i++) {
            this.colors9[i] = colors[i % colors.length];
         }
      }
   }

   private int blendColors(int... colors) {
      int r = 0;
      int g = 0;
      int b = 0;
      int a = 0;

      for (int color : colors) {
         a += color >> 24 & 0xFF;
         r += color >> 16 & 0xFF;
         g += color >> 8 & 0xFF;
         b += color & 0xFF;
      }

      int count = colors.length;
      return a / count << 24 | r / count << 16 | g / count << 8 | b / count;
   }

   private void appendRect(
      float drawX,
      float drawY,
      float drawWidth,
      float drawHeight,
      float x,
      float y,
      float width,
      float height,
      float innerBlur,
      float[] radii,
      float glowSize,
      float glowStrength,
      float glowSoftness,
      int glowColor
   ) {
      ByteBuffer dataBuffer = this.beginItem();
      dataBuffer.putFloat(drawX);
      dataBuffer.putFloat(drawY);
      dataBuffer.putFloat(drawWidth);
      dataBuffer.putFloat(drawHeight);
      dataBuffer.putFloat(radii[0]);
      dataBuffer.putFloat(radii[1]);
      dataBuffer.putFloat(radii[2]);
      dataBuffer.putFloat(radii[3]);

      for (int i = 0; i < 9; i++) {
         int color = this.colors9[i];
         float a = (float)(color >> 24 & 0xFF) / 255.0F;
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float bl = (float)(color & 0xFF) / 255.0F;
         dataBuffer.putFloat(r);
         dataBuffer.putFloat(g);
         dataBuffer.putFloat(bl);
         dataBuffer.putFloat(a);
      }

      dataBuffer.putFloat(x);
      dataBuffer.putFloat(y);
      dataBuffer.putFloat(width);
      dataBuffer.putFloat(height);
      dataBuffer.putFloat(glowSize);
      dataBuffer.putFloat(glowStrength);
      dataBuffer.putFloat(Math.max(0.05F, glowSoftness));
      dataBuffer.putFloat(innerBlur);
      dataBuffer.putFloat((float)(glowColor >> 16 & 0xFF) / 255.0F);
      dataBuffer.putFloat((float)(glowColor >> 8 & 0xFF) / 255.0F);
      dataBuffer.putFloat((float)(glowColor & 0xFF) / 255.0F);
      dataBuffer.putFloat((float)(glowColor >> 24 & 0xFF) / 255.0F);
   }
}
