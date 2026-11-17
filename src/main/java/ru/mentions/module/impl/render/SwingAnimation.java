package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2886;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7204;
import net.minecraft.class_7833;
import ru.mentions.manager.event_impl.HandAnimationEvent;
import ru.mentions.manager.event_impl.SwingDurationEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Swing Animation",
   category = Category.RENDER,
   desc = "Анимации взмаха руки"
)
@Environment(EnvType.CLIENT)
public class SwingAnimation extends Module {
   public ModeSetting swingMode = new ModeSetting(this, "Режим анимации", "Первый", "Второй", "Третий", "Четвёртый", "Пятый", "Шестой");
   public ModeSetting typeSwing = new ModeSetting(this, "Тип", "Первый", "Второй").setVisible(() -> this.swingMode.is("Третий"));
   public SliderSetting swingPowessss = new SliderSetting(this, "Угол", 90.0F, -180.0F, 180.0F, 1.0F).setVisible(() -> this.swingMode.is("Третий"));
   public ModeSetting easeSwin = new ModeSetting(this, "Режим интерполяции", "Первый", "Второй", "Третий");
   public BooleanSetting auraOnly = new BooleanSetting(this, "Только с аурой", false);
   public SliderSetting animSpeed = new SliderSetting(this, "Скорость анимации", 1.0F, 0.1F, 4.0F, 0.1F);
   public SliderSetting animSize = new SliderSetting(this, "Сила анимации", 6.0F, 0.1F, 10.0F, 0.1F);

   public static SwingAnimation get() {
      return Instance.get(SwingAnimation.class);
   }

   public static void interactItem(class_1268 hand) {
      mc.field_1724.method_6104(class_1268.field_5808);
      sendSequencedPacket(id -> new class_2886(hand, id, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
   }

   public static void sendSequencedPacket(class_7204 packetCreator) {
      ClientInteractionAccess.sendSequencedPacket(mc.field_1761, mc.field_1687, packetCreator);
   }

   @EventHandler
   public void onSwingDuration(SwingDurationEvent e) {
      if (this.auraCheck()) {
         class_1792 item = mc.field_1724.method_5998(class_1268.field_5808).method_7909();
         if (item != class_1802.field_8162) {
            float speed = this.animSpeed.getValue();
            e.setAnimation(speed);
            e.cancel();
         }
      }
   }

   @EventHandler
   public void onEvent(HandAnimationEvent event) {
      if (this.isEnabled()) {
         if (this.auraCheck()) {
            boolean rightHand = event.getArm() == class_1306.field_6183;
            ModeSetting mode = this.swingMode;
            if (event.getArm() == mc.field_1724.method_6068()) {
               class_1799 stack = mc.field_1724.method_5998(event.getHand());
               class_4587 matrices = event.getMatrices();
               float swingProgress = event.getSwingProgress();
               int i = rightHand ? 1 : -1;
               float t = this.easeSwin.is("Первый")
                  ? (float)Easings.SINE_OUT.ease((double)swingProgress)
                  : (this.easeSwin.is("Третий") ? swingProgress : (float)Easings.BACK_OUT.ease((double)swingProgress));
               float sin1 = class_3532.method_15374((double)(t * t * (float) Math.PI));
               float sin2 = class_3532.method_15374((double)(class_3532.method_15355(t) * (float) Math.PI));
               float sinSmooth = sin1 * sin2;
               if (this.easeSwin.is("Третий")) {
                  sinSmooth = (float)Math.sin((double)swingProgress * (Math.PI / 2) * 2.0);
               }

               float size = this.animSize.getValue();
               class_1306 arm = event.getArm();
               String var16 = mode.getValue();
               switch (var16) {
                  case "Первый":
                     matrices.method_46416((float)i * 0.56F, -0.52F, -0.72F);
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(sinSmooth * -180.0F * size / 10.0F));
                     matrices.method_22907(class_7833.field_40717.rotationDegrees(sinSmooth * (rightHand ? -60.0F * size / 10.0F : 60.0F * size / 10.0F)));
                     matrices.method_22907(class_7833.field_40716.rotationDegrees(sinSmooth * (rightHand ? -60.0F * size / 10.0F : 60.0F * size / 10.0F)));
                     break;
                  case "Второй":
                     matrices.method_46416((float)i * 0.56F, -0.42F, -0.72F);
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(sinSmooth * -(24.0F * size)));
                     matrices.method_22904(0.0, -0.1, 0.0);
                     break;
                  case "Третий":
                     matrices.method_22904((double)((float)i * 0.7F), -0.35F, -0.9);
                     matrices.method_22907(class_7833.field_40716.rotationDegrees((float)(i * 60)));
                     matrices.method_22907(class_7833.field_40718.rotationDegrees((float)(i * -60)));
                     matrices.method_22907(
                        class_7833.field_40714
                           .rotationDegrees(
                              -this.swingPowessss.getValue() - (this.typeSwing.is("Первый") ? size * 24.0F * sinSmooth : -(size * 24.0F) * sinSmooth)
                           )
                     );
                     break;
                  case "Четвёртый":
                     matrices.method_22904(
                        (double)i * (0.2F - 0.1 * (double)sinSmooth * (double)(0.25F * size)), 0.0, -0.2F - 0.2 * (double)sinSmooth * (double)(0.25F * size)
                     );
                     this.applyEquipOffset(matrices, arm, 0.0F);
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(50.0F - 4.0F * sinSmooth * 0.35F * size));
                     matrices.method_22907(class_7833.field_40716.rotationDegrees((float)i * (-60.0F + 0.6F * size * sinSmooth)));
                     matrices.method_22907(class_7833.field_40718.rotationDegrees((float)i * (110.0F + 40.0F * sinSmooth * 0.2F * size)));
                     break;
                  case "Пятый":
                     matrices.method_46416((float)i * 0.56F, -0.42F, -0.72F);
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(0.0F + t * 360.0F));
                     matrices.method_22904(0.0, -0.1, 0.0);
                     break;
                  case "Шестой":
                     matrices.method_22904((double)((float)i * 0.7F), -0.35F, -0.9);
                     matrices.method_22907(class_7833.field_40716.rotationDegrees((float)(i * 65)));
                     matrices.method_22907(class_7833.field_40718.rotationDegrees((float)(i * -10)));
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(-90.0F - size * 24.0F * sinSmooth));
               }

               event.cancel();
            }
         }
      }
   }

   public boolean auraCheck() {
      if (!this.auraOnly.getValue()) {
         return true;
      } else {
         AttackAura hitAura = AttackAura.get();
         return hitAura != null && hitAura.isEnabled() && AttackAura.target != null;
      }
   }

   public void onEatAnimsHands(class_4587 matrices, float tickDelta, class_1306 arm, class_1799 stack, class_1657 player) {
      float f = (float)player.method_6014() - tickDelta + 1.0F;
      float g = f / (float)stack.method_7935(player);
      if (g < 0.8F) {
         float h = class_3532.method_15379(class_3532.method_15362((double)(f / 5.0F * (float) Math.PI)) * 0.015F);
         matrices.method_46416(0.0F, h, 0.0F);
      }

      float h = 1.0F - (float)Math.pow((double)g, 27.0);
      int i = arm == class_1306.field_6183 ? 1 : -1;
      matrices.method_46416(h * 0.6F * (float)i, h * -0.5F, h * 0.0F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees((float)i * h * 90.0F));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(h * 10.0F));
      matrices.method_22907(class_7833.field_40718.rotationDegrees((float)i * h * 30.0F));
   }

   private void applyEquipOffset(class_4587 matrices, class_1306 arm, float equipProgress) {
      int i = arm == class_1306.field_6183 ? 1 : -1;
      matrices.method_46416((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }
}
