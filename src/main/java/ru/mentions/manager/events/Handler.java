package ru.mentions.manager.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;

@Environment(EnvType.CLIENT)
public abstract class Handler {
   public Handler() {
      Client.eventHandler().subscribe(this);
   }
}
