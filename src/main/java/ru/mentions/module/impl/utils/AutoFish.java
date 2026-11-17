package ru.mentions.module.impl.utils;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.math.StopWatchP;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Auto Fish",
   desc = "Автоматически ловит рыбу при всплеске поплавка",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoFish extends Module {
   public final SliderSetting recastDelay = new SliderSetting(this, "Перезакидка (сек)", 30.0F, 10.0F, 60.0F, 1.0F);
   public final SliderSetting reelDelay = new SliderSetting(this, "Задержка подсечки (мс)", 200.0F, 0.0F, 1000.0F, 10.0F);
   public final BooleanSetting beep = new BooleanSetting(this, "Звуковой сигнал", true);
   private final StopWatchP castTimer = new StopWatchP();
   private final StopWatchP biteTimer = new StopWatchP();
   private final Random random = new Random();
   private boolean waitingBite = false;
   private boolean bitePending = false;

   public static AutoFish get() {
      return Instance.get(AutoFish.class);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.waitingBite = false;
      this.bitePending = false;
      this.castTimer.reset();
   }

   public static void onSoundPlayed(String soundId, double x, double y, double z) {
      AutoFish self = get();
      if (self != null && self.isEnabled()) {
         if (mc.field_1724 != null) {
            if (soundId != null && (soundId.contains("bobber") || soundId.contains("fishing"))) {
               double dx = x - mc.field_1724.method_23317();
               double dy = y - mc.field_1724.method_23318();
               double dz = z - mc.field_1724.method_23321();
               if (!(dx * dx + dy * dy + dz * dz > 900.0)) {
                  if (self.waitingBite) {
                     self.bitePending = true;
                     self.biteTimer.reset();
                  }
               }
            }
         }
      }
   }

   private void useRod() {
      class_310 mc = AutoFish.mc;
      if (mc.field_1724 != null && mc.field_1761 != null) {
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         mc.field_1724.method_6104(class_1268.field_5808);
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (this.bitePending) {
            if (this.biteTimer.isReached((long)this.reelDelay.getValue().floatValue())) {
               this.bitePending = false;
               this.waitingBite = false;
               this.useRod();
               this.castTimer.reset();
               if (this.beep.getValue()) {
                  mc.field_1724.method_5783(beepSound(), 1.0F, 1.5F);
               }
            }
         } else if (!this.waitingBite) {
            if (this.castTimer.isReached(1200L)) {
               this.useRod();
               this.waitingBite = true;
               this.castTimer.reset();
            }
         } else {
            long timeoutMs = (long)(this.recastDelay.getValue() * 1000.0F);
            if (this.castTimer.isReached(timeoutMs)) {
               this.waitingBite = false;
               this.useRod();
               this.castTimer.reset();
            }
         }
      }
   }

   private static class_3414 beepSound() {
      return class_3414.method_47908(class_2960.method_60656("block.note_block.pling"));
   }
}
