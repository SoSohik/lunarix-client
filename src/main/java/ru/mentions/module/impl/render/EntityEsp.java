package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.impl.combat.HitBoxes;
import ru.mentions.module.impl.utils.ReportHelper;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.render.RenderUtil;

@ModuleInfo(
   name = "Entity Esp",
   category = Category.RENDER,
   desc = "Боксы сущностей через стены"
)
@Environment(EnvType.CLIENT)
public class EntityEsp extends Module {
   public BooleanSetting player = new BooleanSetting(this, "Отображения игроков", true);
   public BooleanSetting ignoreNaked = new BooleanSetting(this, "Игнорировать голых", false).setVisible(() -> this.player.getValue());
   public BooleanSetting mobs = new BooleanSetting(this, "Отображения мобов", true);
   public BooleanSetting items = new BooleanSetting(this, "Отображения предметов", true);
   public ModeSetting box = new ModeSetting(this, "Вид бокса", "2д", "3д");
   private final List<class_1297> entities = new ArrayList<>();
   private final class_9799 boxAllocator = new class_9799(262144);
   private static final RenderPipeline BOX_FILL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("minecraft", "rendertype_lequal_depth_test"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline BOX_LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("minecraft", "rendertype_lines"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final int QUAD_BUFFER_SIZE_BYTES = 4096;
   private static final class_1921 BOX_FILL_LAYER = class_1921.method_75940(
      "mentions_esp_box_fill", class_12247.method_75927(BOX_FILL_PIPELINE).method_75929(4096).method_75938()
   );
   private static final class_1921 BOX_LINE_LAYER = class_1921.method_75940(
      "mentions_esp_box_line", class_12247.method_75927(BOX_LINE_PIPELINE).method_75929(4096).method_75938()
   );

   public static EntityEsp get() {
      return Instance.get(EntityEsp.class);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.entities.clear();
      this.boxAllocator.method_60809();
   }

   @EventHandler
   public void onTick(EventTick e) {
      this.entities.clear();
      if (mc.field_1687 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1657) {
               class_1657 p = (class_1657)entity;
               if (this.player.getValue()
                  && (p.method_5797() == null || !p.method_5797().getString().startsWith("Ghost_"))
                  && (!this.ignoreNaked.getValue() || hasArmor(p))) {
                  this.entities.add(entity);
               }
            } else if (entity instanceof class_1309) {
               if (this.mobs.getValue()) {
                  this.entities.add(entity);
               }
            } else if (entity instanceof class_1542 && this.items.getValue()) {
               this.entities.add(entity);
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventRender3D e) {
      float tickDelta = e.getTickDelta();
      if (mc.field_1687 != null && mc.field_1724 != null && !this.entities.isEmpty()) {
         if (this.box.is("3д")) {
            class_4598 immediate = class_4597.method_22991(this.boxAllocator);

            for (class_1297 entity : this.entities) {
               if ((entity != mc.field_1724 || !mc.field_1690.method_31044().method_31034())
                  && Projection.isInFrontOfCamera(entity.method_5829().method_1005())) {
                  this.renderBox(e.getMatrixStack(), immediate, entity, tickDelta);
               }
            }

            immediate.method_22993();
         }
      }
   }

   @EventHandler
   public void onTick(EventDisplay e) {
      float tickDelta = e.getPartialTicks();
      if (mc.field_1687 != null && mc.field_1724 != null && this.box.is("2д")) {
         for (class_1297 entity : this.entities) {
            Vector4d vec = Projection.getVector4D(entity, tickDelta);
            if (!Projection.cantSee(vec) && !(mc.field_1773.method_19418().method_71156().method_1022(entity.method_5829().method_1005()) < 1.0)) {
               boolean var10000;
               label24: {
                  if (entity instanceof class_1657 player && Client.get().friendManager().isFriend(player.method_5477().getString())) {
                     var10000 = true;
                     break label24;
                  }

                  var10000 = false;
               }

               boolean isFriend = var10000;
               this.drawBox(isFriend, vec, entity);
            }
         }
      }
   }

   private void drawBox(boolean friend, Vector4d vec, class_1297 player) {
      int client = friend ? ColorUtil.getColor(100, 200, 100) : ColorUtil.fade(1);
      if (ReportHelper.isReported(player)) {
         client = ReportHelper.getReportColor(client);
      }

      float posX = (float)vec.x;
      float posY = (float)vec.y;
      float endPosX = (float)vec.z;
      float endPosY = (float)vec.w;
      float size = (endPosX - posX) / 3.0F;
      RenderUtil.Render2D.rect(posX - 0.5F, posY - 0.5F, size, 0.5F, client);
      RenderUtil.Render2D.rect(posX - 0.5F, posY, 0.5F, size + 0.5F, client);
      RenderUtil.Render2D.rect(posX - 0.5F, endPosY - size - 0.5F, 0.5F, size, client);
      RenderUtil.Render2D.rect(posX - 0.5F, endPosY - 0.5F, size, 0.5F, client);
      RenderUtil.Render2D.rect(endPosX - size + 1.0F, posY - 0.5F, size, 0.5F, client);
      RenderUtil.Render2D.rect(endPosX + 0.5F, posY, 0.5F, size + 0.5F, client);
      RenderUtil.Render2D.rect(endPosX + 0.5F, endPosY - size - 0.5F, 0.5F, size, client);
      RenderUtil.Render2D.rect(endPosX - size + 1.0F, endPosY - 0.5F, size, 0.5F, client);
   }

   public static boolean hasArmor(class_1657 p) {
      return !p.method_6118(class_1304.field_6169).method_7960()
         || !p.method_6118(class_1304.field_6174).method_7960()
         || !p.method_6118(class_1304.field_6172).method_7960()
         || !p.method_6118(class_1304.field_6166).method_7960();
   }

   private void renderBox(class_4587 matrices, class_4598 immediate, class_1297 target, float partialTicks) {
      if (target != null) {
         class_243 cameraPos = mc.field_1773.method_19418().method_71156();
         double x = target.field_6038 + (target.method_23317() - target.field_6038) * (double)partialTicks;
         double y = target.field_5971 + (target.method_23318() - target.field_5971) * (double)partialTicks;
         double z = target.field_5989 + (target.method_23321() - target.field_5989) * (double)partialTicks;
         class_238 boundingBox = target.method_5829();
         double padding = 0.0;
         if (HitBoxes.get().isEnabled() && target instanceof class_1657) {
            padding = (double)HitBoxes.get().size.getValue().floatValue();
         }

         double minX = boundingBox.field_1323 - target.method_23317() + x - padding - cameraPos.field_1352;
         double minY = boundingBox.field_1322 - target.method_23318() + y - padding - cameraPos.field_1351;
         double minZ = boundingBox.field_1321 - target.method_23321() + z - padding - cameraPos.field_1350;
         double maxX = boundingBox.field_1320 - target.method_23317() + x + padding - cameraPos.field_1352;
         double maxY = boundingBox.field_1325 - target.method_23318() + y + padding - cameraPos.field_1351;
         double maxZ = boundingBox.field_1324 - target.method_23321() + z + padding - cameraPos.field_1350;
         int baseColor = ColorUtil.getClientColor1(1);
         if (target instanceof class_742 player && Client.get().friendManager().isFriend(player.method_5820())) {
            baseColor = ColorUtil.GREEN;
         }

         if (ReportHelper.isReported(target)) {
            baseColor = ReportHelper.getReportColor(baseColor);
         }

         int[] gradientColors = new int[]{
            ColorUtil.gradient(baseColor, baseColor, 0, 7),
            ColorUtil.gradient(baseColor, baseColor, 90, 7),
            ColorUtil.gradient(baseColor, baseColor, 180, 7),
            ColorUtil.gradient(baseColor, baseColor, 270, 7)
         };
         Matrix4f matrix = matrices.method_23760().method_23761();
         class_4588 fillBuffer = immediate.method_73477(BOX_FILL_LAYER);
         drawBoxFill(fillBuffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, gradientColors, 10);
         class_4588 lineBuffer = immediate.method_73477(BOX_LINE_LAYER);
         drawBoxOutline(lineBuffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, gradientColors, 255, 0.12, 0.08);
      }
   }

   public static void drawBoxFill(
      class_4588 buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int[] colors, int fillAlpha
   ) {
      int c0 = ColorUtil.replAlpha(colors[0], fillAlpha);
      int c1 = ColorUtil.replAlpha(colors[1], fillAlpha);
      int c2 = ColorUtil.replAlpha(colors[2], fillAlpha);
      int c3 = ColorUtil.replAlpha(colors[3], fillAlpha);
      float x0 = (float)minX;
      float x1 = (float)maxX;
      float y0 = (float)minY;
      float y1 = (float)maxY;
      float z0 = (float)minZ;
      float z1 = (float)maxZ;
      buffer.method_22918(matrix, x0, y0, z0).method_39415(c0);
      buffer.method_22918(matrix, x1, y0, z0).method_39415(c1);
      buffer.method_22918(matrix, x1, y0, z1).method_39415(c2);
      buffer.method_22918(matrix, x0, y0, z1).method_39415(c3);
      buffer.method_22918(matrix, x0, y1, z0).method_39415(c0);
      buffer.method_22918(matrix, x0, y1, z1).method_39415(c3);
      buffer.method_22918(matrix, x1, y1, z1).method_39415(c2);
      buffer.method_22918(matrix, x1, y1, z0).method_39415(c1);
      buffer.method_22918(matrix, x0, y0, z1).method_39415(c3);
      buffer.method_22918(matrix, x1, y0, z1).method_39415(c2);
      buffer.method_22918(matrix, x1, y1, z1).method_39415(c2);
      buffer.method_22918(matrix, x0, y1, z1).method_39415(c3);
      buffer.method_22918(matrix, x1, y0, z0).method_39415(c1);
      buffer.method_22918(matrix, x0, y0, z0).method_39415(c0);
      buffer.method_22918(matrix, x0, y1, z0).method_39415(c0);
      buffer.method_22918(matrix, x1, y1, z0).method_39415(c1);
      buffer.method_22918(matrix, x0, y0, z0).method_39415(c0);
      buffer.method_22918(matrix, x0, y0, z1).method_39415(c3);
      buffer.method_22918(matrix, x0, y1, z1).method_39415(c3);
      buffer.method_22918(matrix, x0, y1, z0).method_39415(c0);
      buffer.method_22918(matrix, x1, y0, z1).method_39415(c2);
      buffer.method_22918(matrix, x1, y0, z0).method_39415(c1);
      buffer.method_22918(matrix, x1, y1, z0).method_39415(c1);
      buffer.method_22918(matrix, x1, y1, z1).method_39415(c2);
   }

   public static void drawBoxOutline(
      class_4588 buffer,
      Matrix4f matrix,
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      int[] colors,
      int outlineAlpha,
      double dashLength,
      double gapLength
   ) {
      int[] c = new int[4];

      for (int i = 0; i < 4; i++) {
         c[i] = ColorUtil.replAlpha(colors[i], outlineAlpha);
      }

      drawDashedLineSegment(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, c[0], c[1], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, c[1], c[2], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, c[2], c[3], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, c[3], c[0], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, c[0], c[1], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, c[1], c[2], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, c[2], c[3], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, c[3], c[0], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, c[0], c[0], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, c[1], c[1], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, c[2], c[2], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, c[3], c[3], dashLength, gapLength);
   }

   public static void drawDashedLineSegment(
      class_4588 buffer,
      Matrix4f matrix,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      int color1,
      int color2,
      double dashLength,
      double gapLength
   ) {
      double dx = x2 - x1;
      double dy = y2 - y1;
      double dz = z2 - z1;
      double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(length < 0.001)) {
         double unitX = dx / length;
         double unitY = dy / length;
         double unitZ = dz / length;
         int r1 = color1 >> 16 & 0xFF;
         int g1 = color1 >> 8 & 0xFF;
         int b1 = color1 & 0xFF;
         int a1 = color1 >> 24 & 0xFF;
         int r2 = color2 >> 16 & 0xFF;
         int g2 = color2 >> 8 & 0xFF;
         int b2 = color2 & 0xFF;
         int a2 = color2 >> 24 & 0xFF;
         double segmentLength = dashLength + gapLength;

         for (double currentPos = 0.0; currentPos < length; currentPos += segmentLength) {
            double dashEnd = Math.min(currentPos + dashLength, length);
            if (dashEnd > currentPos) {
               double startX = x1 + unitX * currentPos;
               double startY = y1 + unitY * currentPos;
               double startZ = z1 + unitZ * currentPos;
               double endX = x1 + unitX * dashEnd;
               double endY = y1 + unitY * dashEnd;
               double endZ = z1 + unitZ * dashEnd;
               double t = currentPos / length;
               int r = (int)((double)r1 + (double)(r2 - r1) * t);
               int g = (int)((double)g1 + (double)(g2 - g1) * t);
               int b = (int)((double)b1 + (double)(b2 - b1) * t);
               int a = (int)((double)a1 + (double)(a2 - a1) * t);
               buffer.method_22918(matrix, (float)startX, (float)startY, (float)startZ).method_1336(r, g, b, a);
               t = dashEnd / length;
               r = (int)((double)r1 + (double)(r2 - r1) * t);
               g = (int)((double)g1 + (double)(g2 - g1) * t);
               b = (int)((double)b1 + (double)(b2 - b1) * t);
               a = (int)((double)a1 + (double)(a2 - a1) * t);
               buffer.method_22918(matrix, (float)endX, (float)endY, (float)endZ).method_1336(r, g, b, a);
            }
         }
      }
   }
}
