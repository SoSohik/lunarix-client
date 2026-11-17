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
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Tracers",
   desc = "Линии-указатели к сущностям",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Tracers extends Module {
   public ModeSetting targets = new ModeSetting(this, "Цели", "Все", "Игроки", "Мобы");
   public ModeSetting style = new ModeSetting(this, "Стиль", "Линия", "Точка");
   public SliderSetting maxDistance = new SliderSetting(this, "Дистанция", 64.0F, 8.0F, 256.0F, 1.0F);
   public SliderSetting pointSize = new SliderSetting(this, "Размер точки", 0.15F, 0.05F, 1.0F, 0.05F).setVisible(() -> this.style.is("Точка"));
   public ColorSetting enemyColor = new ColorSetting(this, "Цвет врагов", -49136);
   public ColorSetting friendColor = new ColorSetting(this, "Цвет друзей", -16729280);
   private final class_9799 allocator = new class_9799(131072);
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "tracers_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline QUAD_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "tracers_quad"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "tracers_line", class_12247.method_75927(LINE_PIPELINE).method_75929(16384).method_75938()
   );
   private static final class_1921 QUAD_LAYER = class_1921.method_75940(
      "tracers_quad", class_12247.method_75927(QUAD_PIPELINE).method_75929(16384).method_75938()
   );

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.allocator.method_60809();
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         double maxDist = this.maxDistance.getValue().doubleValue();
         double maxDistSq = maxDist * maxDist;
         class_4184 camera = mc.field_1773.method_19418();
         class_243 camPos = camera.method_71156();
         class_4598 immediate = class_4597.method_22991(this.allocator);
         boolean lines = this.style.is("Линия");
         class_4588 lineBuf = lines ? immediate.method_73477(LINE_LAYER) : null;
         class_4588 quadBuf = lines ? null : immediate.method_73477(QUAD_LAYER);
         class_243 eye = mc.field_1724.method_30950(e.getTickDelta()).method_1031(0.0, (double)mc.field_1724.method_5751(), 0.0);
         Matrix4f matrix = e.getMatrixStack().method_23760().method_23761();
         float size = this.pointSize.getValue();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity != mc.field_1724 && entity instanceof class_1309 && entity.method_5805() && !(entity.method_5858(mc.field_1724) > maxDistSq)) {
               boolean valid;
               if (this.targets.is("Игроки")) {
                  valid = entity instanceof class_1657;
               } else if (this.targets.is("Мобы")) {
                  valid = !(entity instanceof class_1657);
               } else {
                  valid = true;
               }

               if (valid) {
                  boolean friend = entity instanceof class_1657 && Client.get().friendManager().isFriend(entity.method_5477().getString());
                  int color = friend ? this.friendColor.getValue() : this.enemyColor.getValue();
                  class_243 pos = entity.method_30950(e.getTickDelta()).method_1031(0.0, (double)(entity.method_17682() / 2.0F), 0.0);
                  double relX = pos.field_1352 - camPos.field_1352;
                  double relY = pos.field_1351 - camPos.field_1351;
                  double relZ = pos.field_1350 - camPos.field_1350;
                  if (lines) {
                     lineBuf.method_22918(
                           matrix,
                           (float)(eye.field_1352 - camPos.field_1352),
                           (float)(eye.field_1351 - camPos.field_1351),
                           (float)(eye.field_1350 - camPos.field_1350)
                        )
                        .method_39415(color);
                     lineBuf.method_22918(matrix, (float)relX, (float)relY, (float)relZ).method_39415(color);
                  } else {
                     e.getMatrixStack().method_22903();
                     e.getMatrixStack().method_22904(relX, relY, relZ);
                     e.getMatrixStack().method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
                     e.getMatrixStack().method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
                     Matrix4f m = e.getMatrixStack().method_23760().method_23761();
                     quadBuf.method_22918(m, -size, -size, 0.0F).method_39415(color);
                     quadBuf.method_22918(m, size, -size, 0.0F).method_39415(color);
                     quadBuf.method_22918(m, size, size, 0.0F).method_39415(color);
                     quadBuf.method_22918(m, -size, size, 0.0F).method_39415(color);
                     e.getMatrixStack().method_22909();
                  }
               }
            }
         }

         immediate.method_22993();
      }
   }
}
