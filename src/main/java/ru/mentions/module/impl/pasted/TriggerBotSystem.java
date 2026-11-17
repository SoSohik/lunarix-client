package ru.mentions.module.impl.pasted;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_239;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Trigger Bot",
   desc = "Pasted:systemdlc | Автоматический удар при наведении на цель из System DLC",
   category = Category.PASTED
)
@Environment(EnvType.CLIENT)
public class TriggerBotSystem extends Module {
   public final ModeSetting attackMode = new ModeSetting(this, "Режим атаки", "Обычный");
   public final ModeSetting hitConditions = new ModeSetting(this, "Условия удара", "Только криты", "Обычный");
   public final BooleanSetting weaponOnly = new BooleanSetting(this, "Только с оружием", true);
   public final SliderSetting reach = new SliderSetting(this, "Дистанция", 3.0F, 1.0F, 6.0F, 0.1F);
   public final BooleanSetting throughEntities = new BooleanSetting(this, "Сквозь сущностей", false);
   public final BooleanSetting customCooldown = new BooleanSetting(this, "Кастомный кд", false);
   public final SliderSetting customCooldownPercent = new SliderSetting(this, "Процент кд", 100.0F, 0.0F, 100.0F, 5.0F)
      .setVisible(() -> this.customCooldown.getValue());
   public final BooleanSetting tpsSync = new BooleanSetting(this, "Синхронизация TPS", false);
   public final BooleanSetting comboLunge = new BooleanSetting(this, "Комбо выпад", false);

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (mc.field_1755 == null) {
            if (!mc.field_1761.method_2923()) {
               if (!mc.field_1724.method_6115()) {
                  if (!this.weaponOnly.getValue() || this.isHoldingWeapon()) {
                     class_239 hit = mc.field_1765;
                     if (hit != null && hit.method_17783() == class_240.field_1331) {
                        if (((class_3966)hit).method_17782() instanceof class_1309 target) {
                           if (target.method_5805() && target != mc.field_1724) {
                              String name = target.method_5477().getString();
                              if (!Client.get().friendManager().isFriend(name)) {
                                 if (!(mc.field_1724.method_5739(target) > this.reach.getValue())) {
                                    if (this.hitConditions.is("Только криты")) {
                                       boolean isFalling = !mc.field_1724.method_24828()
                                          && !mc.field_1724.method_5869()
                                          && !mc.field_1724.method_5799()
                                          && mc.field_1724.method_18798().field_1351 < -0.05
                                          && mc.field_1724.field_6017 > 0.0;
                                       if (!isFalling) {
                                          return;
                                       }
                                    }

                                    float progress = mc.field_1724.method_7261(0.0F);
                                    float needed = this.customCooldown.getValue() ? this.customCooldownPercent.getValue() / 100.0F : 0.95F;
                                    if (!(progress < needed)) {
                                       mc.field_1761.method_2918(mc.field_1724, target);
                                       mc.field_1724.method_6104(class_1268.field_5808);
                                       mc.field_1724.method_7350();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isHoldingWeapon() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         String name = mc.field_1724.method_6047().method_7909().toString().toLowerCase();
         return name.contains("sword") || name.contains("axe") || name.contains("mace");
      }
   }
}
