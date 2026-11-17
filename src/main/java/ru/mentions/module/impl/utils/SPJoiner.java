package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "SP Joiner",
   desc = "Автоматические заходит на дуэли SpookyTime",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class SPJoiner extends Module {
   @EventHandler
   public void onEvent(EventUpdate event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         class_437 screen = mc.field_1755;
         if (mc.field_1724.method_31548().method_67532() == 0 && mc.field_1724.method_6047().method_7909() == class_1802.field_8251) {
            this.toggle();
         } else {
            if (screen instanceof class_465 handledScreen) {
               class_1703 handler = handledScreen.method_17577();

               for (int i = 0; i < handler.field_7761.size(); i++) {
                  class_1735 slot = (class_1735)handler.field_7761.get(i);
                  class_1799 stack = slot.method_7677();
                  if (stack.method_31574(class_1802.field_22022)) {
                     mc.field_1761.method_2906(handler.field_7763, i, 0, class_1713.field_7790, mc.field_1724);
                     return;
                  }
               }
            } else {
               selectCompass();
            }
         }
      }
   }

   public static void selectCompass() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         class_1661 inv = mc.field_1724.method_31548();
         int slot = -1;

         for (int i = 0; i < 9; i++) {
            if (inv.method_5438(i).method_31574(class_1802.field_8251)) {
               slot = i;
               break;
            }
         }

         if (slot != -1) {
            inv.method_61496(slot);
            mc.method_1562().method_52787(new class_2868(slot));
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         }
      }
   }
}
