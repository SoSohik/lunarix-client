package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2735;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "No Slot Change",
   desc = "Запрещает серверу свапать предметы",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class NoSlotChange extends Module {
   @EventHandler
   public void onEvent(EventPacket e) {
      if (e.getPacket() instanceof class_2735) {
         e.setCancelled(true);
      }
   }
}
