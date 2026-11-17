package ru.mentions.maintenance;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.maintenance.gui.CleanupScreen;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "Cleanup",
   desc = "Діагностика та очищення слідів модуля",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public final class CleanupModule extends Module {
   @Override
   protected void onEnable() {
      super.onEnable();
      mc.method_1507(new CleanupScreen());
   }

   @Override
   protected void onDisable() {
      super.onDisable();
   }
}
