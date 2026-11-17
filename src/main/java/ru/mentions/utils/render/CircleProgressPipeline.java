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

@Environment(EnvType.CLIENT)
public class CircleProgressPipeline extends UniformArrayPipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/circle_progress");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/circle_progress");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/circle_progress");
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("CircleData", class_10789.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final int MAX_CIRCLES = 64;
   private static final int CIRCLE_SIZE = 64;
   private static final int UNIFORM_RING = 32;

   public CircleProgressPipeline() {
      super("circle_progress", PIPELINE, "CircleData", 64, 64, 32);
   }

   @Override
   public int batchLayer() {
      return 0;
   }

   public void draw(float centerX, float centerY, float radius, float thickness, float progress, int color) {
      if (!(radius <= 0.0F) && !(thickness <= 0.0F)) {
         class_310 client = class_310.method_1551();
         if (client.method_1522() != null) {
            ByteBuffer dataBuffer = this.beginItem();
            float padding = thickness * 0.75F + 1.0F;
            float size = (radius + padding) * 2.0F;
            float drawX = centerX - size / 2.0F;
            float drawY = centerY - size / 2.0F;
            dataBuffer.putFloat(drawX);
            dataBuffer.putFloat(drawY);
            dataBuffer.putFloat(size);
            dataBuffer.putFloat(size);
            dataBuffer.putFloat(radius + padding);
            dataBuffer.putFloat(radius);
            dataBuffer.putFloat(thickness);
            dataBuffer.putFloat(Math.max(0.0F, Math.min(1.0F, progress)));
            dataBuffer.putFloat((float)(color >> 16 & 0xFF) / 255.0F);
            dataBuffer.putFloat((float)(color >> 8 & 0xFF) / 255.0F);
            dataBuffer.putFloat((float)(color & 0xFF) / 255.0F);
            dataBuffer.putFloat((float)(color >> 24 & 0xFF) / 255.0F);
            dataBuffer.putFloat(0.0F);
            dataBuffer.putFloat(0.0F);
            dataBuffer.putFloat(0.0F);
            dataBuffer.putFloat(0.0F);
            this.endItem();
         }
      }
   }
}
