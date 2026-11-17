package ru.mentions.manager.event_impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class EventKey extends Event {
   private int key;

   public EventKey(int key) {
      this.key = key;
   }

   public int getKey() {
      return this.key;
   }

   public void setKey(int key) {
      this.key = key;
   }
}
