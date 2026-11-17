package ru.mentions.module.impl.pasted;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
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
   name = "HitRefraction",
   category = Category.PASTED,
   desc = "Pasted:wild | Сферическая волна искажения и преломления пространства (Shockwave Refraction) при ударе"
)
@Environment(EnvType.CLIENT)
public class HitRefraction extends Module {
   private static HitRefraction instance;
   public SliderSetting maxRadius = new SliderSetting(this, "Радиус сферы", 1.8F, 0.5F, 4.0F, 0.1F);
   public SliderSetting duration = new SliderSetting(this, "Длительность", 0.45F, 0.15F, 1.2F, 0.05F);
   public SliderSetting distortion = new SliderSetting(this, "Сила искажения", 1.4F, 0.2F, 3.0F, 0.1F);
   public SliderSetting ringCount = new SliderSetting(this, "Количество колец", 3.0F, 1.0F, 6.0F, 1.0F);
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Хром", "Кастомный");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -10177034).setVisible(() -> this.typeColor.is("Кастомный"));
   public BooleanSetting chromatic = new BooleanSetting(this, "Хроматическая аберрация", true);
   private final List<HitRefraction.RefractionWave> waves = new ArrayList<>();
   private final class_9799 allocator = new class_9799(131072);

   public HitRefraction() {
      instance = this;
   }

   public static HitRefraction getInstance() {
      return instance;
   }

   @Override
   protected void onDisable() {
      this.waves.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.waves.clear();
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (this.isEnabled()) {
         class_1297 target = event.getTarget();
         if (target != null) {
            class_243 pos = new class_243(target.method_23317(), target.method_23318() + (double)target.method_17682() * 0.5, target.method_23321());
            this.waves.add(new HitRefraction.RefractionWave(pos, System.currentTimeMillis()));
         }
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D event) {
      if (this.isEnabled() && !this.waves.isEmpty() && mc.field_1773 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         long durMs = (long)(this.duration.getValue() * 1000.0F);
         class_4587 matrices = event.getMatrixStack();
         class_4598 immediate = class_4597.method_22991(this.allocator);
         class_4588 fillBuf = immediate.method_73477(TargetEsp.RING_FILL_LAYER);
         class_4588 lineBuf = immediate.method_73477(TargetEsp.RING_LINE_LAYER);
         class_243 cam = mc.field_1773.method_19418().method_71156();
         Iterator<HitRefraction.RefractionWave> it = this.waves.iterator();

         while (it.hasNext()) {
            HitRefraction.RefractionWave wave = it.next();
            float age = (float)(now - wave.startTime) / (float)durMs;
            if (age >= 1.0F) {
               it.remove();
            } else {
               this.renderWave(matrices, fillBuf, lineBuf, wave, age, cam);
            }
         }

         immediate.method_22993();
      }
   }

   private void renderWave(class_4587 matrices, class_4588 fillBuf, class_4588 lineBuf, HitRefraction.RefractionWave wave, float age, class_243 cam) {
      float progress = class_3532.method_15363(age, 0.0F, 1.0F);
      float ease = 1.0F - (float)Math.pow((double)(1.0F - progress), 3.0);
      float currentRadius = this.maxRadius.getValue() * ease;
      float alpha = (1.0F - progress) * (1.0F - progress);
      int baseColor;
      if (this.typeColor.is("Кастомный")) {
         baseColor = this.tintColor.getValue();
      } else if (this.typeColor.is("Хром")) {
         baseColor = ColorUtil.rainbow(2500, (int)(age * 360.0F), 1.0F, 1.0F, 1.0F);
      } else {
         baseColor = ColorUtil.client();
      }

      double rx = wave.pos.field_1352 - cam.field_1352;
      double ry = wave.pos.field_1351 - cam.field_1351;
      double rz = wave.pos.field_1350 - cam.field_1350;
      matrices.method_22903();
      matrices.method_22904(rx, ry, rz);
      matrices.method_22907(mc.field_1773.method_19418().method_23767());
      Matrix4f matrix = matrices.method_23760().method_23761();
      int rings = Math.round(this.ringCount.getValue());
      int segments = 48;

      for (int i = 0; i < rings; i++) {
         float r = currentRadius * (1.0F - (float)i * 0.16F);
         if (!(r <= 0.02F)) {
            float ringAlpha = alpha * (1.0F - (float)i * 0.22F);
            int lineColor = ColorUtil.replAlpha(baseColor, (int)(ringAlpha * 220.0F));
            int fillInnerColor = ColorUtil.replAlpha(baseColor, (int)(ringAlpha * 65.0F));
            int fillOuterColor = ColorUtil.replAlpha(baseColor, 0);
            float innerR = r * 0.72F;

            for (int seg = 0; seg < segments; seg++) {
               double a0 = (Math.PI * 2) * (double)seg / (double)segments;
               double a1 = (Math.PI * 2) * (double)(seg + 1) / (double)segments;
               float x0In = (float)(Math.cos(a0) * (double)innerR);
               float y0In = (float)(Math.sin(a0) * (double)innerR);
               float x1In = (float)(Math.cos(a1) * (double)innerR);
               float y1In = (float)(Math.sin(a1) * (double)innerR);
               float x0Out = (float)(Math.cos(a0) * (double)r);
               float y0Out = (float)(Math.sin(a0) * (double)r);
               float x1Out = (float)(Math.cos(a1) * (double)r);
               float y1Out = (float)(Math.sin(a1) * (double)r);
               fillBuf.method_22918(matrix, x0In, y0In, 0.0F).method_39415(fillInnerColor);
               fillBuf.method_22918(matrix, x0Out, y0Out, 0.0F).method_39415(fillOuterColor);
               fillBuf.method_22918(matrix, x1Out, y1Out, 0.0F).method_39415(fillOuterColor);
               fillBuf.method_22918(matrix, x0In, y0In, 0.0F).method_39415(fillInnerColor);
               fillBuf.method_22918(matrix, x1Out, y1Out, 0.0F).method_39415(fillOuterColor);
               fillBuf.method_22918(matrix, x1In, y1In, 0.0F).method_39415(fillInnerColor);
            }

            for (int seg = 0; seg < segments; seg++) {
               double a0 = (Math.PI * 2) * (double)seg / (double)segments;
               double a1 = (Math.PI * 2) * (double)(seg + 1) / (double)segments;
               float x0 = (float)(Math.cos(a0) * (double)r);
               float y0 = (float)(Math.sin(a0) * (double)r);
               float x1 = (float)(Math.cos(a1) * (double)r);
               float y1 = (float)(Math.sin(a1) * (double)r);
               lineBuf.method_22918(matrix, x0, y0, 0.0F).method_39415(lineColor);
               lineBuf.method_22918(matrix, x1, y1, 0.0F).method_39415(lineColor);
            }

            if (this.chromatic.getValue() && i == 0) {
               int cyanShift = ColorUtil.getColor(0, 240, 255, (int)(ringAlpha * 110.0F));
               int redShift = ColorUtil.getColor(255, 60, 100, (int)(ringAlpha * 110.0F));
               float rCyan = r * 1.025F;
               float rRed = r * 0.975F;

               for (int seg = 0; seg < segments; seg++) {
                  double a0 = (Math.PI * 2) * (double)seg / (double)segments;
                  double a1 = (Math.PI * 2) * (double)(seg + 1) / (double)segments;
                  lineBuf.method_22918(matrix, (float)(Math.cos(a0) * (double)rCyan), (float)(Math.sin(a0) * (double)rCyan), 0.0F).method_39415(cyanShift);
                  lineBuf.method_22918(matrix, (float)(Math.cos(a1) * (double)rCyan), (float)(Math.sin(a1) * (double)rCyan), 0.0F).method_39415(cyanShift);
                  lineBuf.method_22918(matrix, (float)(Math.cos(a0) * (double)rRed), (float)(Math.sin(a0) * (double)rRed), 0.0F).method_39415(redShift);
                  lineBuf.method_22918(matrix, (float)(Math.cos(a1) * (double)rRed), (float)(Math.sin(a1) * (double)rRed), 0.0F).method_39415(redShift);
               }
            }
         }
      }

      matrices.method_22909();
   }

   private static class RefractionWave {
      public final class_243 pos;
      public final long startTime;

      public RefractionWave(class_243 pos, long startTime) {
         this.pos = pos;
         this.startTime = startTime;
      }
   }
}
