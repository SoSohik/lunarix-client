package ru.mentions.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.aura.AuraUtil;

@ModuleInfo(
   name = "No Web",
   desc = "короче в паутине типа без замедления идешь круто оч",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class NoWeb extends Module {
   @EventHandler
   public void onEvent(EventUpdate e) {
      if (!AuraUtil.nullCheck() && AuraUtil.isPlayerInWeb()) {
         double[] speed = AuraUtil.calculateDirection(0.5);
         mc.field_1724.method_18800(speed[0], mc.field_1690.field_1903.method_1434() ? 1.2 : (mc.field_1690.field_1832.method_1434() ? -2.0 : 0.0), speed[1]);
      }
   }
}
