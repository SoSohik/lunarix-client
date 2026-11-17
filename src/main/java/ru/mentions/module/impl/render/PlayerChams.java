package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Player Chams",
   desc = "Подсветка живых сущностей вокруг",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class PlayerChams extends Module {
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Заливка", "Wireframe", "Pulse", "Оба");
   public final BooleanSetting armourOnly = new BooleanSetting(this, "Только с бронёй", false);
   public final BooleanSetting self = new BooleanSetting(this, "Себя", false);
   public final SliderSetting distance = new SliderSetting(this, "Дистанция", 64.0F, 8.0F, 256.0F, 8.0F);
   public final ColorSetting color = new ColorSetting(this, "Цвет", -19641);
   public final SliderSetting fillAlpha = new SliderSetting(this, "Прозрачность", 0.35F, 0.05F, 1.0F, 0.05F);
   public final BooleanSetting throughWalls = new BooleanSetting(this, "Сквозь стены", true);
   private final class_9799 allocator = new class_9799(131072);
   private static final RenderPipeline FILL_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "pchams_fill_depth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline FILL_NO_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "pchams_fill_nodepth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline LINE_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "pchams_line_depth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline LINE_NO_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "pchams_line_nodepth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 FILL_DEPTH_LAYER = class_1921.method_75940(
      "pchams_fill_depth", class_12247.method_75927(FILL_DEPTH).method_75929(4096).method_75938()
   );
   private static final class_1921 FILL_NO_DEPTH_LAYER = class_1921.method_75940(
      "pchams_fill_nodepth", class_12247.method_75927(FILL_NO_DEPTH).method_75929(4096).method_75938()
   );
   private static final class_1921 LINE_DEPTH_LAYER = class_1921.method_75940(
      "pchams_line_depth", class_12247.method_75927(LINE_DEPTH).method_75929(16384).method_75938()
   );
   private static final class_1921 LINE_NO_DEPTH_LAYER = class_1921.method_75940(
      "pchams_line_nodepth", class_12247.method_75927(LINE_NO_DEPTH).method_75929(16384).method_75938()
   );

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.allocator.method_60809();
   }

   @Override
   public void onDisable() {
      this.allocator.method_60809();
      super.onDisable();
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_243 cam = mc.field_1773.method_19418().method_71156();
         double cx = mc.field_1724.method_23317();
         double cy = mc.field_1724.method_23318();
         double cz = mc.field_1724.method_23321();
         double reach = (double)this.distance.getValue().floatValue();
         double reachSq = reach * reach;
         boolean armourOnly = this.armourOnly.getValue();
         boolean includeSelf = this.self.getValue();
         boolean depth = !this.throughWalls.getValue();
         int baseColor = this.color.getValue();
         float pulse = (float)(0.5 + 0.5 * Math.sin((double)System.currentTimeMillis() / 250.0));
         class_4598 immediate = class_4597.method_22991(this.allocator);
         Matrix4f matrix = e.getMatrixStack().method_23760().method_23761();
         boolean wantFill = this.mode.is("Заливка") || this.mode.is("Pulse") || this.mode.is("Оба");
         boolean wantLine = this.mode.is("Wireframe") || this.mode.is("Оба");
         if (!wantFill && !wantLine) {
            immediate.method_22993();
         } else {
            if (wantFill) {
               class_4588 fill = immediate.method_73477(depth ? FILL_DEPTH_LAYER : FILL_NO_DEPTH_LAYER);
               int fillAlphaInt = (int)(this.fillAlpha.getValue() * 255.0F);
               int fillC = this.mode.is("Pulse")
                  ? ColorUtil.replAlpha(baseColor, (int)((float)fillAlphaInt * pulse))
                  : ColorUtil.replAlpha(baseColor, fillAlphaInt);

               for (class_1297 entity : mc.field_1687.method_18112()) {
                  if (entity instanceof class_1309) {
                     class_1309 living = (class_1309)entity;
                     if ((entity != mc.field_1724 || includeSelf) && living.method_5805()) {
                        double dx = entity.method_23317() - cx;
                        double dy = entity.method_23318() - cy;
                        double dz = entity.method_23321() - cz;
                        if (!(dx * dx + dy * dy + dz * dz > reachSq) && (!armourOnly || this.hasArmour(living))) {
                           class_243 pos = entity.method_30950(e.getTickDelta());
                           float halfW = 0.5F;
                           float h = living.method_17682();
                           double x0 = pos.field_1352 - (double)halfW - cam.field_1352;
                           double y0 = pos.field_1351 - cam.field_1351;
                           double z0 = pos.field_1350 - (double)halfW - cam.field_1350;
                           double x1 = pos.field_1352 + (double)halfW - cam.field_1352;
                           double y1 = pos.field_1351 + (double)h - cam.field_1351;
                           double z1 = pos.field_1350 + (double)halfW - cam.field_1350;
                           this.drawBoxFill(fill, matrix, x0, y0, z0, x1, y1, z1, fillC);
                        }
                     }
                  }
               }
            }

            if (wantLine) {
               class_4588 line = immediate.method_73477(depth ? LINE_DEPTH_LAYER : LINE_NO_DEPTH_LAYER);
               int lineC = ColorUtil.replAlpha(baseColor, 220);

               for (class_1297 entityx : mc.field_1687.method_18112()) {
                  if (entityx instanceof class_1309) {
                     class_1309 living = (class_1309)entityx;
                     if ((entityx != mc.field_1724 || includeSelf) && living.method_5805()) {
                        double dx = entityx.method_23317() - cx;
                        double dy = entityx.method_23318() - cy;
                        double dz = entityx.method_23321() - cz;
                        if (!(dx * dx + dy * dy + dz * dz > reachSq) && (!armourOnly || this.hasArmour(living))) {
                           class_243 pos = entityx.method_30950(e.getTickDelta());
                           float halfW = 0.5F;
                           float h = living.method_17682();
                           double x0 = pos.field_1352 - (double)halfW - cam.field_1352;
                           double y0 = pos.field_1351 - cam.field_1351;
                           double z0 = pos.field_1350 - (double)halfW - cam.field_1350;
                           double x1 = pos.field_1352 + (double)halfW - cam.field_1352;
                           double y1 = pos.field_1351 + (double)h - cam.field_1351;
                           double z1 = pos.field_1350 + (double)halfW - cam.field_1350;
                           this.drawBoxLines(line, matrix, x0, y0, z0, x1, y1, z1, lineC);
                        }
                     }
                  }
               }
            }

            immediate.method_22993();
         }
      }
   }

   private boolean hasArmour(class_1309 living) {
      for (class_1304 slot : new class_1304[]{class_1304.field_6166, class_1304.field_6172, class_1304.field_6174, class_1304.field_6169}) {
         if (!living.method_6118(slot).method_7960()) {
            return true;
         }
      }

      return false;
   }

   private void drawBoxFill(class_4588 buf, Matrix4f m, double x0, double y0, double z0, double x1, double y1, double z1, int color) {
      this.quad(
         buf, m, (float)x0, (float)y0, (float)z0, (float)x1, (float)y0, (float)z0, (float)x1, (float)y1, (float)z0, (float)x0, (float)y1, (float)z0, color
      );
      this.quad(
         buf, m, (float)x0, (float)y0, (float)z1, (float)x1, (float)y0, (float)z1, (float)x1, (float)y1, (float)z1, (float)x0, (float)y1, (float)z1, color
      );
      this.quad(
         buf, m, (float)x0, (float)y0, (float)z0, (float)x0, (float)y0, (float)z1, (float)x0, (float)y1, (float)z1, (float)x0, (float)y1, (float)z0, color
      );
      this.quad(
         buf, m, (float)x1, (float)y0, (float)z0, (float)x1, (float)y0, (float)z1, (float)x1, (float)y1, (float)z1, (float)x1, (float)y1, (float)z0, color
      );
      this.quad(
         buf, m, (float)x0, (float)y0, (float)z0, (float)x1, (float)y0, (float)z0, (float)x1, (float)y0, (float)z1, (float)x0, (float)y0, (float)z1, color
      );
      this.quad(
         buf, m, (float)x0, (float)y1, (float)z0, (float)x1, (float)y1, (float)z0, (float)x1, (float)y1, (float)z1, (float)x0, (float)y1, (float)z1, color
      );
   }

   private void quad(
      class_4588 buf,
      Matrix4f m,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      int color
   ) {
      buf.method_22918(m, x0, y0, z0).method_39415(color);
      buf.method_22918(m, x1, y1, z1).method_39415(color);
      buf.method_22918(m, x2, y2, z2).method_39415(color);
      buf.method_22918(m, x3, y3, z3).method_39415(color);
   }

   private void drawBoxLines(class_4588 buf, Matrix4f m, double x0, double y0, double z0, double x1, double y1, double z1, int color) {
      float fx0 = (float)x0;
      float fy0 = (float)y0;
      float fz0 = (float)z0;
      float fx1 = (float)x1;
      float fy1 = (float)y1;
      float fz1 = (float)z1;
      this.edge(buf, m, fx0, fy0, fz0, fx1, fy0, fz0, color);
      this.edge(buf, m, fx0, fy0, fz1, fx1, fy0, fz1, color);
      this.edge(buf, m, fx0, fy1, fz0, fx1, fy1, fz0, color);
      this.edge(buf, m, fx0, fy1, fz1, fx1, fy1, fz1, color);
      this.edge(buf, m, fx0, fy0, fz0, fx0, fy0, fz1, color);
      this.edge(buf, m, fx1, fy0, fz0, fx1, fy0, fz1, color);
      this.edge(buf, m, fx0, fy1, fz0, fx0, fy1, fz1, color);
      this.edge(buf, m, fx1, fy1, fz0, fx1, fy1, fz1, color);
      this.edge(buf, m, fx0, fy0, fz0, fx0, fy1, fz0, color);
      this.edge(buf, m, fx1, fy0, fz0, fx1, fy1, fz0, color);
      this.edge(buf, m, fx0, fy0, fz1, fx0, fy1, fz1, color);
      this.edge(buf, m, fx1, fy0, fz1, fx1, fy1, fz1, color);
   }

   private void edge(class_4588 buf, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int color) {
      buf.method_22918(m, x0, y0, z0).method_39415(color);
      buf.method_22918(m, x1, y1, z1).method_39415(color);
   }
}
