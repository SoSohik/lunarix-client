package ru.mentions.module.impl.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2868;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.other.TimerUtil;

@ModuleInfo(
   name = "Auto Potion",
   desc = "Смотрит вниз и кидает все нужные зелья сразу, когда эффекта нет",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AutoPotion extends Module {
   public final MultiBooleanSetting potions = new MultiBooleanSetting(
      this, "Зелья", new BooleanSetting("Сила", true), new BooleanSetting("Скорость", true), new BooleanSetting("Огнестойкость", true)
   );
   public final SliderSetting delay = new SliderSetting(this, "Задержка", 500.0F, 100.0F, 2000.0F, 50.0F);
   private final TimerUtil throwCooldown = new TimerUtil();
   private int oldSlot = -1;

   @Override
   protected void onDisable() {
      this.restoreSlot();
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null && mc.field_1724.field_3944 != null) {
         if (mc.field_1755 == null && !mc.field_1724.method_6115()) {
            List<Integer> slots = this.collectPotionSlots();
            if (slots.isEmpty()) {
               this.restoreSlot();
            } else {
               RotationProcess.update(new Rotation(mc.field_1724.method_36454(), 90.0F), 180.0F, 180.0F, 2, 90);
               if (!(mc.field_1724.method_36455() < 88.0F)) {
                  if (this.throwCooldown.isReached((long)this.delay.getValue().floatValue())) {
                     this.oldSlot = mc.field_1724.method_31548().method_67532();

                     for (int slot : slots) {
                        mc.field_1724.method_31548().method_61496(slot);
                        mc.field_1724.field_3944.method_52787(new class_2868(slot));
                        mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                        mc.field_1724.method_6104(class_1268.field_5808);
                     }

                     this.restoreSlot();
                     this.throwCooldown.reset();
                  }
               }
            }
         }
      }
   }

   private void restoreSlot() {
      if (this.oldSlot != -1 && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.oldSlot);
         if (mc.field_1724.field_3944 != null) {
            mc.field_1724.field_3944.method_52787(new class_2868(this.oldSlot));
         }

         this.oldSlot = -1;
      }
   }

   private List<Integer> collectPotionSlots() {
      ArrayList<Integer> slots = new ArrayList<>();
      HashSet<String> queued = new HashSet<>();

      for (int i = 0; i <= 8; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         List<String> eligible;
         if (stack.method_7909() == class_1802.field_8436 && !(eligible = this.getEligibleEffects(stack)).isEmpty()) {
            boolean useful = false;

            for (String key : eligible) {
               if (!queued.contains(key)) {
                  useful = true;
                  break;
               }
            }

            if (useful) {
               slots.add(i);
               queued.addAll(eligible);
            }
         }
      }

      return slots;
   }

   private List<String> getEligibleEffects(class_1799 stack) {
      ArrayList<String> keys = new ArrayList<>();
      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      if (contents == null) {
         return keys;
      } else {
         for (class_1293 instance : contents.method_57397()) {
            class_6880 type = instance.method_5579();
            String key = this.effectKey(type);
            if (key != null && this.potions.getValue(key) && !mc.field_1724.method_6059(type)) {
               keys.add(key);
            }
         }

         return keys;
      }
   }

   private String effectKey(class_6880<class_1291> type) {
      if (type.comp_349() == class_1294.field_5910.comp_349()) {
         return "Сила";
      } else if (type.comp_349() == class_1294.field_5904.comp_349()) {
         return "Скорость";
      } else {
         return type.comp_349() == class_1294.field_5918.comp_349() ? "Огнестойкость" : null;
      }
   }
}
