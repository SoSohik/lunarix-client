package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2815;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;

@ModuleInfo(
   name = "KtLeave",
   category = Category.PLAYER,
   desc = "Быстрый лив с режима или переход на PvP арену через /help"
)
@Environment(EnvType.CLIENT)
public class KtLeave extends Module {
   public ModeSetting mode = new ModeSetting(this, "Режим", "SpookyTime", "HolyWorld", "Custom");

   @Override
   public void onEnable() {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         if (this.mode.is("SpookyTime")) {
            mc.field_1724.field_3944.method_45730("help");
         } else if (this.mode.is("HolyWorld")) {
            mc.field_1724.field_3944.method_45730("hub");
         } else {
            mc.field_1724.field_3944.method_45730("spawn");
         }
         this.setEnabled(false, false);
      }
   }
}
