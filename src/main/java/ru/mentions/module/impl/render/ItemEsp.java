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
import net.minecraft.class_1542;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2480;
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
   name = "Item Esp",
   desc = "Подсветка выброшенных предметов",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ItemEsp extends Module {
   public ModeSetting mode = new ModeSetting(this, "Режим", "Бокс", "Углы");
   public SliderSetting scale = new SliderSetting(this, "Масштаб", 1.0F, 0.1F, 5.0F, 0.1F);
   public ColorSetting color = new ColorSetting(this, "Цвет", -1);
   public BooleanSetting categories = new BooleanSetting(this, "Цвет по категории", true);
   public BooleanSetting onlyValuable = new BooleanSetting(this, "Только ценное", false);
   public ColorSetting totemColor = new ColorSetting(this, "Цвет тотемов", -22016).setVisible(() -> this.categories.getValue());
   public ColorSetting shulkerColor = new ColorSetting(this, "Цвет шалкеров", -43521).setVisible(() -> this.categories.getValue());
   public ColorSetting elytraColor = new ColorSetting(this, "Цвет элитры", -11141121).setVisible(() -> this.categories.getValue());
   public ColorSetting pearlColor = new ColorSetting(this, "Цвет эндер-перлов", -11141291).setVisible(() -> this.categories.getValue());
   private final class_9799 allocator = new class_9799(131072);
   private static final RenderPipeline FILL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "item_esp_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "item_esp_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 FILL_LAYER = class_1921.method_75940(
      "item_esp_fill", class_12247.method_75927(FILL_PIPELINE).method_75929(4096).method_75938()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "item_esp_line", class_12247.method_75927(LINE_PIPELINE).method_75929(16384).method_75938()
   );

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.allocator.method_60809();
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_243 cam = mc.field_1773.method_19418().method_71156();
         Matrix4f matrix = e.getMatrixStack().method_23760().method_23761();
         boolean useCategories = this.categories.getValue();
         boolean valuableOnly = this.onlyValuable.getValue();
         int defaultColor = this.color.getValue();
         List<ItemEsp.ItemRenderData> items = new ArrayList<>();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1542) {
               class_1799 stack = ((class_1542)entity).method_6983();
               int c = this.colorFor(stack, defaultColor, useCategories, valuableOnly);
               if (c != 0) {
                  class_243 pos = entity.method_30950(e.getTickDelta());
                  items.add(new ItemEsp.ItemRenderData(pos.field_1352 - cam.field_1352, pos.field_1351 - cam.field_1351, pos.field_1350 - cam.field_1350, c));
               }
            }
         }

         if (!items.isEmpty()) {
            float s = 0.25F * this.scale.getValue();
            float h = 0.5F * this.scale.getValue();
            boolean corners = this.mode.is("Углы");
            class_4598 immediate = class_4597.method_22991(this.allocator);
            class_4588 fill = immediate.method_73477(FILL_LAYER);

            for (ItemEsp.ItemRenderData item : items) {
               this.drawFill(fill, matrix, item.x, item.y, item.z, s, h, ColorUtil.replAlpha(item.color, 40));
            }

            class_4588 line = immediate.method_73477(LINE_LAYER);

            for (ItemEsp.ItemRenderData item : items) {
               this.drawLines(line, matrix, item.x, item.y, item.z, s, h, item.color, corners);
            }

            immediate.method_22993();
         }
      }
   }

   private int colorFor(class_1799 stack, int defaultColor, boolean useCategories, boolean valuableOnly) {
      int c = -1;
      class_1792 item = stack.method_7909();
      if (item == class_1802.field_8288) {
         c = this.totemColor.getValue();
      } else if (item == class_1802.field_8833) {
         c = this.elytraColor.getValue();
      } else if (item == class_1802.field_8634) {
         c = this.pearlColor.getValue();
      } else if (item instanceof class_1747 && ((class_1747)item).method_7711() instanceof class_2480) {
         c = this.shulkerColor.getValue();
      }

      if (valuableOnly && c == -1) {
         return 0;
      } else if (c == -1) {
         return defaultColor;
      } else {
         return useCategories ? c : defaultColor;
      }
   }

   private void drawFill(class_4588 fill, Matrix4f m, double x, double y, double z, float s, float h, int fillColor) {
      float x0 = (float)x - s;
      float x1 = (float)x + s;
      float y0 = (float)y;
      float y1 = (float)y + h;
      float z0 = (float)z - s;
      float z1 = (float)z + s;
      this.quad(fill, m, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, fillColor);
      this.quad(fill, m, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, fillColor);
      this.quad(fill, m, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, fillColor);
      this.quad(fill, m, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, fillColor);
      this.quad(fill, m, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, fillColor);
      this.quad(fill, m, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, fillColor);
   }

   private void drawLines(class_4588 line, Matrix4f m, double x, double y, double z, float s, float h, int lineColor, boolean corners) {
      float x0 = (float)x - s;
      float x1 = (float)x + s;
      float y0 = (float)y;
      float y1 = (float)y + h;
      float z0 = (float)z - s;
      float z1 = (float)z + s;
      float cl = corners ? s * 0.5F : s;
      this.edge(line, m, x0, y0, z0, x0 + cl, y0, z0, lineColor);
      this.edge(line, m, x1, y0, z0, x1 - cl, y0, z0, lineColor);
      this.edge(line, m, x0, y0, z1, x0 + cl, y0, z1, lineColor);
      this.edge(line, m, x1, y0, z1, x1 - cl, y0, z1, lineColor);
      this.edge(line, m, x0, y1, z0, x0 + cl, y1, z0, lineColor);
      this.edge(line, m, x1, y1, z0, x1 - cl, y1, z0, lineColor);
      this.edge(line, m, x0, y1, z1, x0 + cl, y1, z1, lineColor);
      this.edge(line, m, x1, y1, z1, x1 - cl, y1, z1, lineColor);
      this.edge(line, m, x0, y0, z0, x0, y0, z0 + cl, lineColor);
      this.edge(line, m, x0, y0, z1, x0, y0, z1 - cl, lineColor);
      this.edge(line, m, x1, y0, z0, x1, y0, z0 + cl, lineColor);
      this.edge(line, m, x1, y0, z1, x1, y0, z1 - cl, lineColor);
      this.edge(line, m, x0, y1, z0, x0, y1, z0 + cl, lineColor);
      this.edge(line, m, x0, y1, z1, x0, y1, z1 - cl, lineColor);
      this.edge(line, m, x1, y1, z0, x1, y1, z0 + cl, lineColor);
      this.edge(line, m, x1, y1, z1, x1, y1, z1 - cl, lineColor);
      this.edge(line, m, x0, y0, z0, x0, y0 + Math.min(h, cl * 2.0F), z0, lineColor);
      this.edge(line, m, x1, y0, z0, x1, y0 + Math.min(h, cl * 2.0F), z0, lineColor);
      this.edge(line, m, x0, y0, z1, x0, y0 + Math.min(h, cl * 2.0F), z1, lineColor);
      this.edge(line, m, x1, y0, z1, x1, y0 + Math.min(h, cl * 2.0F), z1, lineColor);
      this.edge(line, m, x0, y1, z0, x0, y1 - Math.min(h, cl * 2.0F), z0, lineColor);
      this.edge(line, m, x1, y1, z0, x1, y1 - Math.min(h, cl * 2.0F), z0, lineColor);
      this.edge(line, m, x0, y1, z1, x0, y1 - Math.min(h, cl * 2.0F), z1, lineColor);
      this.edge(line, m, x1, y1, z1, x1, y1 - Math.min(h, cl * 2.0F), z1, lineColor);
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

   private void edge(class_4588 buf, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int color) {
      buf.method_22918(m, x0, y0, z0).method_39415(color);
      buf.method_22918(m, x1, y1, z1).method_39415(color);
   }

   private static record ItemRenderData(double x, double y, double z, int color) {
   }
}
