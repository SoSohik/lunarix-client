package ru.mentions.manager.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;

@Environment(EnvType.CLIENT)
public class Event {
   public String getName() {
      return this.getClass().getSimpleName().toLowerCase();
   }

   public void hook() {
      Client.eventHandler().post(this);
   }
}
