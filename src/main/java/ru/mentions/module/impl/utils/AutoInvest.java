package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.notification.NotificationManager;

@ModuleInfo(
   name = "Auto Invest",
   desc = "Автоматический инвестиции в клан по бинду или авто-порогу",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoInvest extends Module {
   public final ModeSetting soundMode = new ModeSetting(
      this,
      "Звук уведомления",
      "ApplePay",
      "Bell",
      "Tone",
      "Notify",
      "Modern",
      "Bubble",
      "Celestial",
      "Zenith",
      "Classic",
      "Soft",
      "Cyber",
      "Skeet",
      "DingDong",
      "Laser",
      "WexSide",
      "Spek",
      "Metallic",
      "Crime",
      "Pop",
      "По умолчанию",
      "Выкл"
   );
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Бинд", "Авто");
   public final BindSetting investKey = new BindSetting(this, "Клавиша инвеста", -1).setVisible(() -> this.mode.is("Бинд"));
   public final SliderSetting bindPercent = new SliderSetting(this, "% от баланса (Бинд)", 50.0F, 1.0F, 100.0F, 1.0F).setVisible(() -> this.mode.is("Бинд"));
   public final StringSetting triggerAmount = new StringSetting(this, "Триггер монет", "10000000").setVisible(() -> this.mode.is("Авто"));
   public final SliderSetting autoPercent = new SliderSetting(this, "% от баланса (Авто)", 50.0F, 1.0F, 100.0F, 1.0F).setVisible(() -> this.mode.is("Авто"));
   private int cooldownTicks = 0;

   @EventHandler
   public void onKey(EventKey e) {
      if (mc.field_1724 != null && e.getKey() != -1) {
         if (this.mode.is("Бинд")) {
            if (e.getKey() == this.investKey.get()) {
               int balance = this.getBalanceFromScoreboard();
               if (balance <= 0) {
                  this.sendMsg("§cНе удалось определить баланс!");
                  return;
               }

               int amount = (int)((float)balance * (this.bindPercent.getValue() / 100.0F));
               if (amount <= 0) {
                  this.sendMsg("§cСумма инвеста = 0!");
                  return;
               }

               mc.field_1724.field_3944.method_45730("clan invest " + amount);
               this.sendMsg("§aИнвестировано §f" + amount + " §aмонет (" + this.bindPercent.getValue().intValue() + "% от " + balance + ")");
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.mode.is("Авто")) {
            if (this.cooldownTicks > 0) {
               this.cooldownTicks--;
            } else {
               int balance = this.getBalanceFromScoreboard();
               if (balance > 0) {
                  int trigger;
                  try {
                     String tv = this.triggerAmount.getValue().trim();
                     trigger = tv.isEmpty() ? 0 : Integer.parseInt(tv);
                  } catch (NumberFormatException var5) {
                     return;
                  }

                  if (balance >= trigger) {
                     int amount = (int)((float)balance * (this.autoPercent.getValue() / 100.0F));
                     if (amount > 0) {
                        mc.field_1724.field_3944.method_45730("clan invest " + amount);
                        this.sendMsg("§aАвто-инвест: §f" + amount + " §aмонет (" + this.autoPercent.getValue().intValue() + "% от " + balance + ")");
                        this.cooldownTicks = 100;
                     }
                  }
               }
            }
         }
      }
   }

   private int getBalanceFromScoreboard() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_269 scoreboard = mc.field_1687.method_8428();
         class_266 objective = scoreboard.method_1189(class_8646.field_45157);
         if (objective == null) {
            return -1;
         } else {
            for (class_9011 entry : scoreboard.method_1184(objective)) {
               String line = class_268.method_1142(scoreboard.method_1164(entry.comp_2127()), entry.method_55387()).getString();
               int balance = this.parseMoneyLine(line);
               if (balance > 0) {
                  return balance;
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   private int parseMoneyLine(String line) {
      if (line == null) {
         return -1;
      } else {
         String text = line.replaceAll("§.", "").replaceAll("[\\u00A7].", "").toLowerCase();
         if (!text.contains("монет") && !text.contains("баланс")) {
            return -1;
         } else {
            String digits = text.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
               return -1;
            } else {
               try {
                  return Integer.parseInt(digits);
               } catch (NumberFormatException var5) {
                  return -1;
               }
            }
         }
      }
   }

   private void sendMsg(String text) {
      ChatUtils.addChatMessage("§6[AutoInvest] " + text);
      NotificationManager.sendWithSound(text, NotificationManager.Type.INFO, this.soundMode.getValue());
   }

   @Override
   public void onDisable() {
      this.cooldownTicks = 0;
      super.onDisable();
   }
}
