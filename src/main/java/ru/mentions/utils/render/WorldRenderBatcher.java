package ru.mentions.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;

public final class WorldRenderBatcher {
   private static final RenderPipeline LINE_PIPELINE = RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelineHelper.getField56860()})
         .withLocation(class_2960.method_60655("client", "mentions_world_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );

   private static final RenderPipeline FILL_PIPELINE = RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelineHelper.getField56860()})
         .withLocation(class_2960.method_60655("client", "mentions_world_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );

   private static final class_1921 LINE_LAYER = RenderPipelineHelper.invokeMethod75940(
      "mentions_world_line", class_12247.method_75927(LINE_PIPELINE).method_75929(65536).method_75938()
   );

   private static final class_1921 FILL_LAYER = RenderPipelineHelper.invokeMethod75940(
      "mentions_world_fill", class_12247.method_75927(FILL_PIPELINE).method_75929(65536).method_75938()
   );

   private static final class_9799 ALLOCATOR = new class_9799(131072);
   private static class_4598 immediate;
   private static class_4588 lineBuffer;
   private static class_4588 fillBuffer;
   private static Matrix4f matrix;
   private static double camX;
   private static double camY;
   private static double camZ;

   private WorldRenderBatcher() {
   }

   public static void begin(class_4587 stack) {
      if (immediate == null && stack != null) {
         matrix = stack.method_23760().method_23761();

         try {
            class_310 client = class_310.method_1551();
            if (client.field_1773 != null && client.field_1773.method_19418() != null) {
               class_243 camPos = client.field_1773.method_19418().method_71156();
               if (camPos != null) {
                  camX = camPos.field_1352;
                  camY = camPos.field_1351;
                  camZ = camPos.field_1350;
               } else {
                  camX = camY = camZ = 0.0;
               }
            } else {
               camX = camY = camZ = 0.0;
            }
         } catch (Throwable t) {
            camX = camY = camZ = 0.0;
         }

         immediate = class_4597.method_22991(ALLOCATOR);
         lineBuffer = immediate.method_73477(LINE_LAYER);
         fillBuffer = immediate.method_73477(FILL_LAYER);
      }
   }

   public static boolean isOpen() {
      return immediate != null;
   }

   public static void drawLineNoDepth(class_243 from, class_243 to, int color, float width) {
      if (lineBuffer != null && matrix != null && from != null && to != null) {
         float x0 = (float)(from.field_1352 - camX);
         float y0 = (float)(from.field_1351 - camY);
         float z0 = (float)(from.field_1350 - camZ);
         float x1 = (float)(to.field_1352 - camX);
         float y1 = (float)(to.field_1351 - camY);
         float z1 = (float)(to.field_1350 - camZ);
         lineBuffer.method_22918(matrix, x0, y0, z0).method_39415(color);
         lineBuffer.method_22918(matrix, x1, y1, z1).method_39415(color);
      }
   }

   public static void drawBox(class_238 box, int color, float width) {
      if (box != null && lineBuffer != null && matrix != null) {
         float x0 = (float)(box.field_1323 - camX);
         float y0 = (float)(box.field_1322 - camY);
         float z0 = (float)(box.field_1321 - camZ);
         float x1 = (float)(box.field_1320 - camX);
         float y1 = (float)(box.field_1325 - camY);
         float z1 = (float)(box.field_1324 - camZ);
         drawBoxLinesDirect(x0, y0, z0, x1, y1, z1, color);
      }
   }

   public static void drawBoxLinesDirect(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
      if (lineBuffer != null && matrix != null) {
         edge(x0, y0, z0, x1, y0, z0, color);
         edge(x0, y0, z1, x1, y0, z1, color);
         edge(x0, y1, z0, x1, y1, z0, color);
         edge(x0, y1, z1, x1, y1, z1, color);
         edge(x0, y0, z0, x0, y0, z1, color);
         edge(x1, y0, z0, x1, y0, z1, color);
         edge(x0, y1, z0, x0, y1, z1, color);
         edge(x1, y1, z0, x1, y1, z1, color);
         edge(x0, y0, z0, x0, y1, z0, color);
         edge(x1, y0, z0, x1, y1, z0, color);
         edge(x0, y0, z1, x0, y1, z1, color);
         edge(x1, y0, z1, x1, y1, z1, color);
      }
   }

   public static void drawBoxFillDirect(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
      if (fillBuffer != null && matrix != null) {
         quad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, color);
         quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, color);
         quad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, color);
         quad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, color);
         quad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, color);
         quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, color);
      }
   }

   public static void drawChamsBox(double px, double py, double pz, float width, float height, int lineColor, int fillColor) {
      float halfW = width * 0.5F;
      float x0 = (float)(px - halfW - camX);
      float y0 = (float)(py - camY);
      float z0 = (float)(pz - halfW - camZ);
      float x1 = (float)(px + halfW - camX);
      float y1 = (float)(py + height - camY);
      float z1 = (float)(pz + halfW - camZ);
      if ((fillColor >>> 24) > 0) {
         drawBoxFillDirect(x0, y0, z0, x1, y1, z1, fillColor);
      }
      if ((lineColor >>> 24) > 0) {
         drawBoxLinesDirect(x0, y0, z0, x1, y1, z1, lineColor);
      }
   }

   private static void edge(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
      lineBuffer.method_22918(matrix, x0, y0, z0).method_39415(color);
      lineBuffer.method_22918(matrix, x1, y1, z1).method_39415(color);
   }

   private static void quad(float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int color) {
      fillBuffer.method_22918(matrix, x0, y0, z0).method_39415(color);
      fillBuffer.method_22918(matrix, x1, y1, z1).method_39415(color);
      fillBuffer.method_22918(matrix, x2, y2, z2).method_39415(color);
      fillBuffer.method_22918(matrix, x3, y3, z3).method_39415(color);
   }

   public static void end() {
      if (immediate != null) {
         immediate.method_22993();
         immediate = null;
         lineBuffer = null;
         fillBuffer = null;
         matrix = null;
         camX = camY = camZ = 0.0;
      }
   }
}
