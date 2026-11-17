package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
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
   name = "Echo",
   category = Category.RENDER,
   desc = "Светящиеся призрачные копии сущностей вдоль их пути"
)
@Environment(EnvType.CLIENT)
public class Echo extends Module {
   private static final class_2960 CIRCLE_TEX = class_2960.method_60655("client", "textures/particles/circle.png");
   private static final class_2960 GLOW_TEX = class_2960.method_60655("client", "textures/particles/glow.png");
   private static final RenderPipeline GHOST_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("client", "pipeline/echo_ghost"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 GHOST_LAYER = class_1921.method_75940(
      "echo_ghost_layer", class_12247.method_75927(GHOST_PIPELINE).method_75934("Sampler0", CIRCLE_TEX).method_75937().method_75929(16384).method_75938()
   );
   private static final class_1921 GLOW_LAYER = class_1921.method_75940(
      "echo_glow_layer", class_12247.method_75927(GHOST_PIPELINE).method_75934("Sampler0", GLOW_TEX).method_75937().method_75929(16384).method_75938()
   );
   public ModeSetting colorMode = new ModeSetting(this, "Цвет", "Тема", "Кастом");
   public ColorSetting customColor = new ColorSetting(this, "Свой цвет", -9524993).setVisible(() -> this.colorMode.is("Кастом"));
   public BooleanSetting players = new BooleanSetting(this, "Игроки", true);
   public BooleanSetting mobs = new BooleanSetting(this, "Мобы", true);
   public BooleanSetting self = new BooleanSetting(this, "На себе (F5)", true);
   public SliderSetting length = new SliderSetting(this, "Длина эха", 10.0F, 3.0F, 24.0F, 1.0F);
   public SliderSetting interval = new SliderSetting(this, "Интервал (тики)", 2.0F, 1.0F, 6.0F, 1.0F);
   public SliderSetting size = new SliderSetting(this, "Размер", 1.0F, 0.6F, 2.0F, 0.05F);
   public BooleanSetting glow = new BooleanSetting(this, "Свечение", true);
   private final Map<UUID, ArrayDeque<class_243>> history = new HashMap<>();
   private int tickCounter = 0;

   @Override
   protected void onDisable() {
      this.history.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.history.clear();
   }

   private int baseColor() {
      return this.colorMode.is("Кастом") ? this.customColor.getValue() : ColorUtil.getClientColor(1);
   }

   private boolean track(class_1309 entity) {
      if (entity == mc.field_1724) {
         return this.self.getValue();
      } else {
         return entity instanceof class_1657 ? this.players.getValue() : this.mobs.getValue();
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         int step = Math.max(1, this.interval.getValue().intValue());
         this.tickCounter++;
         if (this.tickCounter % step == 0) {
            int maxSamples = Math.max(3, this.length.getValue().intValue());
            HashSet<UUID> seen = new HashSet<>();

            for (class_1297 entity : mc.field_1687.method_18112()) {
               class_1309 living;
               if (entity instanceof class_1309 && this.track(living = (class_1309)entity)) {
                  UUID id2 = entity.method_5667();
                  seen.add(id2);
                  ArrayDeque queue = this.history.computeIfAbsent(id2, key -> new ArrayDeque<>());
                  queue.addFirst(new class_243(entity.method_23317(), entity.method_23318(), entity.method_23321()));

                  while (queue.size() > maxSamples) {
                     queue.removeLast();
                  }
               }
            }

            this.history.keySet().removeIf(id -> !seen.contains(id));
         }
      }
   }

   @EventHandler(
      priority = 500
   )
   public void onRender(EventRender3D event) {
      if (mc.field_1687 != null && mc.field_1724 != null && !this.history.isEmpty()) {
         class_243 cameraPos = mc.field_1773.method_19418().method_71156();
         Quaternionf rotation = mc.field_1773.method_19418().method_23767();
         Vector3f right = rotation.transform(new Vector3f(1.0F, 0.0F, 0.0F));
         Vector3f up = rotation.transform(new Vector3f(0.0F, 1.0F, 0.0F));
         Vector3f forward = rotation.transform(new Vector3f(0.0F, 0.0F, -1.0F));
         Matrix4f matrix = event.getMatrixStack().method_23760().method_23761();
         class_4598 consumers = mc.method_22940().method_23000();
         int base = this.baseColor();
         boolean useGlow = this.glow.getValue();
         class_4588 ghostConsumer = consumers.method_73477(GHOST_LAYER);

         for (Entry<UUID, ArrayDeque<class_243>> entry : this.history.entrySet()) {
            ArrayDeque<class_243> queue = entry.getValue();
            int total = queue.size();
            if (total >= 2) {
               int index = 0;

               for (class_243 pos : queue) {
                  float f = 1.0F - (float)index / (float)total;
                  float alpha = f * f * 0.75F;
                  index++;
                  if (alpha <= 0.015F) {
                     break;
                  }

                  if (!(pos.method_1025(cameraPos) > 4096.0) && !this.behindCamera(pos, cameraPos, forward)) {
                     float scale = this.size.getValue() * (0.55F + 0.45F * f);
                     float halfW = 0.34F * scale;
                     float height = 1.85F * scale;
                     texturedQuad(ghostConsumer, matrix, pos, cameraPos, right, up, halfW, height, withAlpha(base, alpha));
                  }
               }
            }
         }

         consumers.method_22994(GHOST_LAYER);
         if (useGlow) {
            class_4588 glowConsumer = consumers.method_73477(GLOW_LAYER);

            for (Entry<UUID, ArrayDeque<class_243>> entryx : this.history.entrySet()) {
               ArrayDeque<class_243> queue = entryx.getValue();
               int total = queue.size();
               if (total >= 2) {
                  int index = 0;

                  for (class_243 pos : queue) {
                     float fx = 1.0F - (float)index / (float)total;
                     float alphax = fx * fx * 0.6F;
                     index++;
                     if (alphax <= 0.02F) {
                        break;
                     }

                     if (!(pos.method_1025(cameraPos) > 4096.0) && !this.behindCamera(pos, cameraPos, forward)) {
                        float scale = this.size.getValue() * (0.55F + 0.45F * fx);
                        texturedQuad(
                           glowConsumer,
                           matrix,
                           pos,
                           cameraPos,
                           right,
                           up,
                           0.34F * scale * 2.2F,
                           1.85F * scale * 1.15F,
                           withAlpha(base, Math.min(1.0F, alphax * 0.5F))
                        );
                     }
                  }
               }
            }

            consumers.method_22994(GLOW_LAYER);
         }
      }
   }

   private boolean behindCamera(class_243 pos, class_243 cameraPos, Vector3f forward) {
      float px = (float)(pos.field_1352 - cameraPos.field_1352);
      float py = (float)(pos.field_1351 - cameraPos.field_1351);
      float pz = (float)(pos.field_1350 - cameraPos.field_1350);
      return px * forward.x + py * forward.y + pz * forward.z < -2.0F;
   }

   private static int withAlpha(int color, float alpha) {
      int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
      return color & 16777215 | a << 24;
   }

   private static void texturedQuad(
      class_4588 consumer, Matrix4f matrix, class_243 pos, class_243 cameraPos, Vector3f right, Vector3f up, float halfWidth, float height, int color
   ) {
      float px = (float)(pos.field_1352 - cameraPos.field_1352);
      float py = (float)(pos.field_1351 - cameraPos.field_1351);
      float pz = (float)(pos.field_1350 - cameraPos.field_1350);
      float rx = right.x * halfWidth;
      float ry = right.y * halfWidth;
      float rz = right.z * halfWidth;
      float ux = up.x * height;
      float uy = up.y * height;
      float uz = up.z * height;
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      float a = (float)(color >>> 24 & 0xFF) / 255.0F;
      consumer.method_22918(matrix, px - rx, py, pz - rz).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      consumer.method_22918(matrix, px + rx, py, pz + rz).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      consumer.method_22918(matrix, px + rx + ux, py + uy, pz + rz + uz).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      consumer.method_22918(matrix, px - rx + ux, py + uy, pz - rz + uz).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
   }
}
