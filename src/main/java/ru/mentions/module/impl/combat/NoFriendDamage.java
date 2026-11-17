package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "No Friend Damage",
   desc = "Убирает урон по друзьям",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class NoFriendDamage extends Module {
   @EventHandler
   public void onEvent(AttackEvent event) {
      if (Client.get().friendManager().isFriend(event.getTarget().method_5477().getString())) {
         event.cancel();
      }
   }
}
