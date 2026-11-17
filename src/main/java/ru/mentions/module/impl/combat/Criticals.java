package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Criticals",
   desc = "ибашит критами жестка когда нельзя тип а она ебашит прикиньте как крута",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class Criticals extends Module {
   public static Criticals getInstance() {
      return Instance.get(Criticals.class);
   }

   @EventHandler
   public void onEvent(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1724.method_7261(2.0F) >= 1.0F && mc.field_1724.method_24828() && AttackAura.target != null) {
         mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.04, mc.field_1724.method_18798().field_1350);
         mc.field_1724.field_64356 = true;
      }
   }

   public boolean canCritical() {
      return this.isEnabled() && mc.field_1724.field_6017 <= 0.0 && !mc.field_1724.method_24828();
   }
}
