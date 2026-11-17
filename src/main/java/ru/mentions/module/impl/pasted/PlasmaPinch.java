package ru.mentions.module.impl.pasted;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.mentions.manager.event_impl.AttackEvent;
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
import ru.mentions.module.impl.render.TargetEsp;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Plasma Pinch",
   category = Category.PASTED,
   desc = "Pasted:wild | Эффект плазменного схлопывания (Plasma Pinch / Implosion) на цели при ударе"
)
@Environment(EnvType.CLIENT)
public class PlasmaPinch extends Module {
   private static PlasmaPinch instance;
   public SliderSetting startRadius = new SliderSetting(this, "Начальный радиус", 1.5F, 0.5F, 3.5F, 0.1F);
   public SliderSetting duration = new SliderSetting(this, "Длительность", 0.5F, 0.2F, 1.2F, 0.05F);
   public SliderSetting rotationSpeed = new SliderSetting(this, "Скорость вращения", 1.8F, 0.5F, 4.0F, 0.1F);
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Фиолетовый", "Электрик", "Кастомный");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -6461953).setVisible(() -> this.typeColor.is("Кастомный"));
   public BooleanSetting sparks = new BooleanSetting(this, "Искры распада", true);
   public SliderSetting sparkCount = new SliderSetting(this, "Количество искр", 16.0F, 6.0F, 32.0F, 2.0F).setVisible(() -> this.sparks.getValue());
   private final List<PlasmaPinch.PinchEffect> pinches = new ArrayList<>();
   private final class_9799 allocator = new class_9799(131072);

   public PlasmaPinch() {
      instance = this;
   }

   public static PlasmaPinch getInstance() {
      return instance;
   }

   @Override
   protected void onDisable() {
      this.pinches.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.pinches.clear();
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (this.isEnabled()) {
         class_1297 target = event.getTarget();
         if (target != null) {
            class_243 pos = new class_243(target.method_23317(), target.method_23318() + (double)target.method_17682() * 0.5, target.method_23321());
            PlasmaPinch.PinchEffect effect = new PlasmaPinch.PinchEffect(pos, System.currentTimeMillis());
            if (this.sparks.getValue()) {
               int sc = Math.round(this.sparkCount.getValue());
               ThreadLocalRandom rnd = ThreadLocalRandom.current();

               for (int i = 0; i < sc; i++) {
                  float theta = rnd.nextFloat() * (float) (Math.PI * 2);
                  float phi = (rnd.nextFloat() - 0.5F) * (float) Math.PI;
                  float spd = 1.4F + rnd.nextFloat() * 2.2F;
                  double vx = Math.cos((double)phi) * Math.cos((double)theta) * (double)spd;
                  double vy = Math.sin((double)phi) * (double)spd;
                  double vz = Math.cos((double)phi) * Math.sin((double)theta) * (double)spd;
                  effect.sparks.add(new PlasmaPinch.PlasmaSpark(pos, new class_243(vx, vy, vz)));
               }
            }

            this.pinches.add(effect);
         }
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D event) {
      if (this.isEnabled() && !this.pinches.isEmpty() && mc.field_1773 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         long durMs = (long)(this.duration.getValue() * 1000.0F);
         class_4587 matrices = event.getMatrixStack();
         class_4598 immediate = class_4597.method_22991(this.allocator);
         class_4588 fillBuf = immediate.method_73477(TargetEsp.RING_FILL_LAYER);
         class_4588 lineBuf = immediate.method_73477(TargetEsp.RING_LINE_LAYER);
         class_243 cam = mc.field_1773.method_19418().method_71156();
         Iterator<PlasmaPinch.PinchEffect> it = this.pinches.iterator();

         while (it.hasNext()) {
            PlasmaPinch.PinchEffect pinch = it.next();
            float progress = (float)(now - pinch.startTime) / (float)durMs;
            if (progress >= 1.0F) {
               it.remove();
            } else {
               this.renderPinch(matrices, fillBuf, lineBuf, pinch, progress, cam);
            }
         }

         immediate.method_22993();
      }
   }

   private void renderPinch(class_4587 matrices, class_4588 fillBuf, class_4588 lineBuf, PlasmaPinch.PinchEffect pinch, float progress, class_243 cam) {
      int baseColor;
      if (this.typeColor.is("Кастомный")) {
         baseColor = this.tintColor.getValue();
      } else if (this.typeColor.is("Фиолетовый")) {
         baseColor = -6461953;
      } else if (this.typeColor.is("Электрик")) {
         baseColor = -16718337;
      } else {
         baseColor = ColorUtil.client();
      }

      double rx = pinch.center.field_1352 - cam.field_1352;
      double ry = pinch.center.field_1351 - cam.field_1351;
      double rz = pinch.center.field_1350 - cam.field_1350;
      matrices.method_22903();
      matrices.method_22904(rx, ry, rz);
      matrices.method_22907(mc.field_1773.method_19418().method_23767());
      float pinchPhase = Math.min(1.0F, progress / 0.45F);
      float pinchContract = 1.0F - (float)Math.pow((double)pinchPhase, 2.0);
      float r = this.startRadius.getValue() * pinchContract;
      float alpha = 1.0F - progress;
      float rot = progress * this.rotationSpeed.getValue() * 360.0F;
      matrices.method_22907(new Quaternionf().rotationAxis((float)Math.toRadians((double)rot), 0.0F, 0.0F, 1.0F));
      Matrix4f matrix = matrices.method_23760().method_23761();
      int segments = 40;
      if (pinchPhase < 0.98F) {
         int rings = 3;

         for (int i = 0; i < rings; i++) {
            float ringR = r * (1.0F - (float)i * 0.28F);
            if (!(ringR <= 0.03F)) {
               float ringAlpha = alpha * (0.85F - (float)i * 0.2F);
               int ringCol = ColorUtil.replAlpha(baseColor, (int)(ringAlpha * 230.0F));
               int fillCol = ColorUtil.replAlpha(baseColor, (int)(ringAlpha * 50.0F));

               for (int seg = 0; seg < segments; seg++) {
                  double a0 = (Math.PI * 2) * (double)seg / (double)segments;
                  double a1 = (Math.PI * 2) * (double)(seg + 1) / (double)segments;
                  float x0 = (float)(Math.cos(a0) * (double)ringR);
                  float y0 = (float)(Math.sin(a0) * (double)ringR);
                  float x1 = (float)(Math.cos(a1) * (double)ringR);
                  float y1 = (float)(Math.sin(a1) * (double)ringR);
                  lineBuf.method_22918(matrix, x0, y0, 0.0F).method_39415(ringCol);
                  lineBuf.method_22918(matrix, x1, y1, 0.0F).method_39415(ringCol);
                  fillBuf.method_22918(matrix, 0.0F, 0.0F, 0.0F).method_39415(fillCol);
                  fillBuf.method_22918(matrix, x0, y0, 0.0F).method_39415(fillCol);
                  fillBuf.method_22918(matrix, x1, y1, 0.0F).method_39415(fillCol);
               }
            }
         }
      } else {
         float burstProg = (progress - 0.45F) / 0.55F;
         float burstR = this.startRadius.getValue() * 0.7F * burstProg;
         float burstAlpha = (1.0F - burstProg) * (1.0F - burstProg);
         int flashCol = ColorUtil.replAlpha(-1, (int)(burstAlpha * 240.0F));
         int burstRingCol = ColorUtil.replAlpha(baseColor, (int)(burstAlpha * 200.0F));
         float flashSize = 0.22F * (1.0F - burstProg);
         fillBuf.method_22918(matrix, -flashSize, -flashSize, 0.0F).method_39415(flashCol);
         fillBuf.method_22918(matrix, flashSize, -flashSize, 0.0F).method_39415(flashCol);
         fillBuf.method_22918(matrix, flashSize, flashSize, 0.0F).method_39415(flashCol);
         fillBuf.method_22918(matrix, -flashSize, -flashSize, 0.0F).method_39415(flashCol);
         fillBuf.method_22918(matrix, flashSize, flashSize, 0.0F).method_39415(flashCol);
         fillBuf.method_22918(matrix, -flashSize, flashSize, 0.0F).method_39415(flashCol);

         for (int seg = 0; seg < segments; seg++) {
            double a0 = (Math.PI * 2) * (double)seg / (double)segments;
            double a1 = (Math.PI * 2) * (double)(seg + 1) / (double)segments;
            float x0 = (float)(Math.cos(a0) * (double)burstR);
            float y0 = (float)(Math.sin(a0) * (double)burstR);
            float x1 = (float)(Math.cos(a1) * (double)burstR);
            float y1 = (float)(Math.sin(a1) * (double)burstR);
            lineBuf.method_22918(matrix, x0, y0, 0.0F).method_39415(burstRingCol);
            lineBuf.method_22918(matrix, x1, y1, 0.0F).method_39415(burstRingCol);
         }
      }

      matrices.method_22909();
      if (this.sparks.getValue() && progress > 0.35F && !pinch.sparks.isEmpty()) {
         float sparkProg = (progress - 0.35F) / 0.65F;
         float sparkAlpha = 1.0F - sparkProg;
         int sparkCol = ColorUtil.replAlpha(baseColor, (int)(sparkAlpha * 240.0F));
         int headCol = ColorUtil.replAlpha(-1, (int)(sparkAlpha * 255.0F));

         for (PlasmaPinch.PlasmaSpark spark : pinch.sparks) {
            double sx0 = pinch.center.field_1352 + spark.vel.field_1352 * (double)Math.max(0.0F, sparkProg - 0.08F) - cam.field_1352;
            double sy0 = pinch.center.field_1351 + spark.vel.field_1351 * (double)Math.max(0.0F, sparkProg - 0.08F) - cam.field_1351;
            double sz0 = pinch.center.field_1350 + spark.vel.field_1350 * (double)Math.max(0.0F, sparkProg - 0.08F) - cam.field_1350;
            double sx1 = pinch.center.field_1352 + spark.vel.field_1352 * (double)sparkProg - cam.field_1352;
            double sy1 = pinch.center.field_1351 + spark.vel.field_1351 * (double)sparkProg - cam.field_1351;
            double sz1 = pinch.center.field_1350 + spark.vel.field_1350 * (double)sparkProg - cam.field_1350;
            Matrix4f worldMatrix = matrices.method_23760().method_23761();
            lineBuf.method_22918(worldMatrix, (float)sx0, (float)sy0, (float)sz0).method_39415(sparkCol);
            lineBuf.method_22918(worldMatrix, (float)sx1, (float)sy1, (float)sz1).method_39415(headCol);
         }
      }
   }

   private static class PinchEffect {
      public final class_243 center;
      public final long startTime;
      public final List<PlasmaPinch.PlasmaSpark> sparks = new ArrayList<>();

      public PinchEffect(class_243 center, long startTime) {
         this.center = center;
         this.startTime = startTime;
      }
   }

   private static class PlasmaSpark {
      public final class_243 origin;
      public final class_243 vel;

      public PlasmaSpark(class_243 origin, class_243 vel) {
         this.origin = origin;
         this.vel = vel;
      }
   }
}
