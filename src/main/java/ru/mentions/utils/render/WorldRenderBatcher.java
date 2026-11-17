package ru.mentions.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.minecraft.class_10799;
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
import org.joml.Vector3f;

public final class WorldRenderBatcher {
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "mentions_world_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "mentions_world_line", class_12247.method_75927(LINE_PIPELINE).method_75929(65536).method_75938()
   );
   private static final class_9799 ALLOCATOR = new class_9799(131072);
   private static class_4598 immediate;
   private static class_4588 buffer;
   private static Matrix4f matrix;
   private static Vector3f camera;
   private static double camX;
   private static double camY;
   private static double camZ;

   private WorldRenderBatcher() {
   }

   public static void begin(class_4587 stack) {
      if (immediate == null && stack != null) {
         Matrix4f world = stack.method_23760().method_23761();

         try {
            class_310 client = class_310.method_1551();
            if (client.field_1773 != null && client.field_1773.method_19418() != null) {
               class_243 camPos = client.field_1773.method_19418().method_71156();
               if (camPos != null) {
                  camX = camPos.field_1352;
                  camY = camPos.field_1351;
                  camZ = camPos.field_1350;
               } else {
                  camZ = 0.0;
                  camY = 0.0;
                  camX = 0.0;
               }
            } else {
               camZ = 0.0;
               camY = 0.0;
               camX = 0.0;
            }
         } catch (Throwable var4) {
            camZ = 0.0;
            camY = 0.0;
            camX = 0.0;
         }

         camera = new Vector3f(0.0F, 0.0F, 0.0F);
         matrix = world;
         immediate = class_4597.method_22991(ALLOCATOR);
         buffer = immediate.method_73477(LINE_LAYER);
      }
   }

   public static boolean isOpen() {
      return immediate != null;
   }

   public static void drawLineNoDepth(class_243 from, class_243 to, int color, float width) {
      if (buffer != null && matrix != null && from != null && to != null) {
         int r = color >> 16 & 0xFF;
         int g = color >> 8 & 0xFF;
         int b = color & 0xFF;
         int a = color >>> 24 & 0xFF;
         double x0 = from.field_1352;
         double y0 = from.field_1351;
         double z0 = from.field_1350;
         double x1 = to.field_1352;
         double y1 = to.field_1351;
         double z1 = to.field_1350;
         double dx = x1 - x0;
         double dy = y1 - y0;
         double dz = z1 - z0;
         double mx = (x0 + x1) * 0.5 - camX;
         double my = (y0 + y1) * 0.5 - camY;
         double mz = (z0 + z1) * 0.5 - camZ;
         double px = -mx;
         double py = -my;
         double pz = -mz;
         double nx = dy * pz - dz * py;
         double ny = dz * px - dx * pz;
         double nz = dx * py - dy * px;
         double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
         if (len < 1.0E-6) {
            nx = 0.0;
            ny = 1.0;
            nz = 0.0;
            len = 1.0;
         }

         double half = Math.max((double)width, 0.02) * 0.5;
         nx = nx / len * half;
         ny = ny / len * half;
         nz = nz / len * half;
         vertex((float)(x0 - camX - nx), (float)(y0 - camY - ny), (float)(z0 - camZ - nz), r, g, b, a);
         vertex((float)(x0 - camX + nx), (float)(y0 - camY + ny), (float)(z0 - camZ + nz), r, g, b, a);
         vertex((float)(x1 - camX + nx), (float)(y1 - camY + ny), (float)(z1 - camZ + nz), r, g, b, a);
         vertex((float)(x0 - camX - nx), (float)(y0 - camY - ny), (float)(z0 - camZ - nz), r, g, b, a);
         vertex((float)(x1 - camX + nx), (float)(y1 - camY + ny), (float)(z1 - camZ + nz), r, g, b, a);
         vertex((float)(x1 - camX - nx), (float)(y1 - camY - ny), (float)(z1 - camZ - nz), r, g, b, a);
      }
   }

   public static void drawBox(class_238 box, int color, float width) {
      if (box != null) {
         class_243 min = box.method_61125();
         class_243 max = box.method_61126();
         class_243[] c = new class_243[]{
            new class_243(min.field_1352, min.field_1351, min.field_1350),
            new class_243(max.field_1352, min.field_1351, min.field_1350),
            new class_243(max.field_1352, min.field_1351, max.field_1350),
            new class_243(min.field_1352, min.field_1351, max.field_1350),
            new class_243(min.field_1352, max.field_1351, min.field_1350),
            new class_243(max.field_1352, max.field_1351, min.field_1350),
            new class_243(max.field_1352, max.field_1351, max.field_1350),
            new class_243(min.field_1352, max.field_1351, max.field_1350)
         };
         int[][] edges = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

         for (int[] e : edges) {
            drawLineNoDepth(c[e[0]], c[e[1]], color, width);
         }
      }
   }

   private static void vertex(float x, float y, float z, int r, int g, int b, int a) {
      buffer.method_22918(matrix, x, y, z).method_1336(r, g, b, a);
   }

   public static void end() {
      if (immediate != null) {
         immediate.method_22993();
         immediate = null;
         buffer = null;
         matrix = null;
         camera = null;
         camX = 0.0;
         camY = 0.0;
         camZ = 0.0;
      }
   }
}
