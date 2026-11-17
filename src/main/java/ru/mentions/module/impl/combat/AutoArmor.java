package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2815;
import net.minecraft.class_6880;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.math.StopWatchP;

@ModuleInfo(
   name = "Auto Armor",
   desc = "Автоматически меняет броню на лучшую",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AutoArmor extends Module {
   public final SliderSetting delay = new SliderSetting(this, "Задержка", 150.0F, 50.0F, 1000.0F, 10.0F);
   public final BooleanSetting preferProtection = new BooleanSetting(this, "Приоритет защиты", true);
   private final StopWatchP timer = new StopWatchP();
   private static final class_1304[] ARMOR_ORDER = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};

   private static int screenArmorSlot(class_1304 slot) {
      if (slot == class_1304.field_6169) {
         return 8;
      } else if (slot == class_1304.field_6174) {
         return 7;
      } else if (slot == class_1304.field_6172) {
         return 6;
      } else {
         return slot == class_1304.field_6166 ? 5 : -1;
      }
   }

   private int score(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         int score = 0;
         class_9304 comp = (class_9304)stack.method_57353().method_58694(class_9334.field_49633);
         if (comp != null && !comp.method_57543()) {
            for (class_6880 entry : comp.method_57534()) {
               String id = entry.method_55840().toLowerCase();
               int lvl = comp.method_57536(entry);
               if (id.contains("protection")) {
                  score += 1000 * lvl;
               } else if (id.contains("unbreaking")) {
                  score += 100 * lvl;
               } else if (id.contains("mending")) {
                  score += 50 * lvl;
               } else if (id.contains("thorns")) {
                  score += 10 * lvl;
               } else if (!id.contains("blast_protection")
                  && !id.contains("fire_protection")
                  && !id.contains("projectile_protection")
                  && !id.contains("feather_falling")) {
                  score += lvl;
               } else if (!this.preferProtection.getValue()) {
                  score += 500 * lvl;
               } else {
                  score += 5 * lvl;
               }
            }
         }

         int maxDamage = stack.method_7936();
         if (maxDamage > 0) {
            score += (int)(20.0F * (1.0F - (float)stack.method_7919() / (float)maxDamage));
         }

         return score;
      } else {
         return -1;
      }
   }

   private boolean isBetter(class_1799 candidate, class_1799 current) {
      int cs = this.score(candidate);
      int os = this.score(current);
      return cs > os;
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (mc.field_1755 == null && !mc.field_1724.method_5624()) {
            if (this.timer.isReached((long)this.delay.getValue().floatValue())) {
               for (class_1304 slot : ARMOR_ORDER) {
                  class_1799 worn = mc.field_1724.method_6118(slot);
                  int bestInvSlot = -1;
                  class_1799 bestStack = worn;

                  for (int i = 0; i < 36; i++) {
                     class_1799 candidate = mc.field_1724.method_31548().method_5438(i);
                     if (candidate != null && !candidate.method_7960() && this.fitsSlot(candidate, slot) && this.isBetter(candidate, bestStack)) {
                        bestStack = candidate;
                        bestInvSlot = i;
                     }
                  }

                  if (bestInvSlot != -1) {
                     int screenInv = bestInvSlot < 9 ? bestInvSlot + 36 : bestInvSlot;
                     int screenArmor = screenArmorSlot(slot);
                     int syncId = mc.field_1724.field_7498.field_7763;
                     mc.field_1761.method_2906(syncId, screenInv, 0, class_1713.field_7790, mc.field_1724);
                     mc.field_1761.method_2906(syncId, screenArmor, 0, class_1713.field_7790, mc.field_1724);
                     if (!worn.method_7960()) {
                        mc.field_1761.method_2906(syncId, screenInv, 0, class_1713.field_7790, mc.field_1724);
                     }

                     mc.field_1724.field_3944.method_52787(new class_2815(syncId));
                     this.timer.reset();
                     return;
                  }
               }
            }
         }
      }
   }

   private boolean fitsSlot(class_1799 stack, class_1304 slot) {
      if (stack.method_7909() == class_1802.field_8833) {
         return false;
      } else {
         class_1304 equip = mc.field_1724.method_32326(stack);
         return equip == slot;
      }
   }
}
