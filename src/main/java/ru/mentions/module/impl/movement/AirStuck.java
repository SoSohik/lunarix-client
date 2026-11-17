package ru.mentions.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2828;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.MotionEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Air Stuck",
   category = Category.MOVEMENT,
   desc = "Заморозка в воздухе"
)
@Environment(EnvType.CLIENT)
public class AirStuck extends Module {
   public static AirStuck getInstance() {
      return Instance.get(AirStuck.class);
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (!mc.field_1724.method_24828()) {
         mc.field_1724.method_18800(0.0, 0.0, 0.0);
      }
   }

   @EventHandler
   public void onMotion(MotionEvent eventMotion) {
      if (!mc.field_1724.method_24828()) {
         eventMotion.ground(false);
      }
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      if (!mc.field_1724.method_24828() && e.getPacket() instanceof class_2828) {
         e.cancel();
      }
   }
}
