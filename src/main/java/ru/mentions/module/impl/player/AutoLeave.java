package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_476;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.math.StopGPT;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Auto Leave",
   desc = "Автоматически ливает с сервера",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class AutoLeave extends Module {
   public final ModeSetting leaveMode = new ModeSetting(this, "Команда", "/hub", "/spawn", "/darena");
   public final BooleanSetting onPlayerNearby = new BooleanSetting(this, "Игрок рядом", true);
   public final SliderSetting playerRange = new SliderSetting(this, "Дистанция игрока", 20.0F, 10.0F, 50.0F, 1.0F);
   public final BooleanSetting onPvpEnd = new BooleanSetting(this, "Конец PvP", false);
   private final StopGPT antiSpamTimer = new StopGPT();
   private final StopGPT darenaMenuTimer = new StopGPT();
   private boolean waitingForDarenaMenu = false;
   private boolean leaveTriggered = false;
   private boolean didLeave = false;
   private int darenaClickTicks = 0;
   private static final long ANTI_SPAM_DELAY = 5000L;
   private static final long POST_LEAVE_COOLDOWN = 10000L;
   private long lastLeaveTime = 0L;

   public static AutoLeave get() {
      return Instance.get(AutoLeave.class);
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.didLeave && !this.leaveMode.is("/darena")) {
            double x = Math.abs(mc.field_1724.method_23317());
            double z = Math.abs(mc.field_1724.method_23321());
            if (x <= 5.0 && z <= 5.0) {
               ChatUtils.addChatMessage("§e[AutoLeave] §aТелепортация успешна, выключаюсь.");
               this.didLeave = false;
               this.setEnabled(false);
               return;
            }
         }

         if (this.waitingForDarenaMenu) {
            this.darenaClickTicks++;
            if (mc.field_1755 instanceof class_476 screen) {
               class_1707 handler = (class_1707)screen.method_17577();
               int containerSlots = handler.method_17388() * 9;

               for (int i = 0; i < containerSlots; i++) {
                  class_1735 slot = handler.method_7611(i);
                  if (slot.method_7681() && slot.method_7677().method_7909() == class_1802.field_8323) {
                     mc.field_1761.method_2906(handler.field_7763, i, 0, class_1713.field_7790, mc.field_1724);
                     this.waitingForDarenaMenu = false;
                     this.darenaClickTicks = 0;
                     return;
                  }
               }

               if (this.darenaClickTicks > 40) {
                  this.waitingForDarenaMenu = false;
                  this.darenaClickTicks = 0;
               }
            } else if (this.darenaClickTicks > 60) {
               this.waitingForDarenaMenu = false;
               this.darenaClickTicks = 0;
            }
         } else if (System.currentTimeMillis() - this.lastLeaveTime >= 10000L) {
            if (this.antiSpamTimer.hasTimePassed(5000L)) {
               boolean shouldLeave = false;
               Object reason = "";
               if (this.onPlayerNearby.getValue()) {
                  float maxRange = this.playerRange.getValue();

                  for (class_1657 player : mc.field_1687.method_18456()) {
                     double dist;
                     if (player != mc.field_1724 && player.method_5805() && (dist = (double)mc.field_1724.method_5739(player)) <= (double)maxRange) {
                        shouldLeave = true;
                        reason = "Игрок " + player.method_5477().getString() + " рядом (" + (int)dist + "м)";
                        break;
                     }
                  }
               }

               if (!shouldLeave && this.onPvpEnd.getValue()) {
                  try {
                     ElytraHelper elytraHelper = Instance.get(ElytraHelper.class);
                     if (elytraHelper != null && elytraHelper.isEnabled() && elytraHelper.isPvpJustEnded()) {
                        shouldLeave = true;
                        reason = "PvP закончился";
                        elytraHelper.clearPvpEnded();
                     }
                  } catch (Exception var9) {
                  }
               }

               if (shouldLeave) {
                  this.executeLeave((String)reason);
               }
            }
         }
      }
   }

   private void executeLeave(String reason) {
      this.antiSpamTimer.reset();
      this.lastLeaveTime = System.currentTimeMillis();
      String mode = this.leaveMode.getValue();
      ChatUtils.addChatMessage("§e[AutoLeave] §c" + reason + " §7→ §a" + mode);
      switch (mode) {
         case "/hub":
            mc.field_1724.field_3944.method_45730("hub");
            this.didLeave = true;
            break;
         case "/spawn":
            mc.field_1724.field_3944.method_45730("spawn");
            this.didLeave = true;
            break;
         case "/darena":
            mc.field_1724.field_3944.method_45730("darena");
            this.waitingForDarenaMenu = true;
            this.darenaMenuTimer.reset();
            this.darenaClickTicks = 0;
      }
   }

   @Override
   public void onDisable() {
      this.waitingForDarenaMenu = false;
      this.darenaClickTicks = 0;
      this.leaveTriggered = false;
      this.didLeave = false;
      super.onDisable();
   }
}
