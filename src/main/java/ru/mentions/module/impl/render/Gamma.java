package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "Gamma",
   desc = "Полная яркость (фуллбрайт)",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Gamma extends Module {
   @Override
   public void onEnable() {
      super.onEnable();
      if (mc.field_1769 != null) {
         mc.field_1769.method_3279();
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.field_1769 != null) {
         mc.field_1769.method_3279();
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_6016(class_1294.field_5925);
      }
   }
}
