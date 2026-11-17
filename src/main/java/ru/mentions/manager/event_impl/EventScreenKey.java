package ru.mentions.manager.event_impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class EventScreenKey extends Event {
   private final int key;
   private final class_437 screen;

   public EventScreenKey(int key, class_437 screen) {
      this.key = key;
      this.screen = screen;
   }

   public int getKey() {
      return this.key;
   }

   public class_437 getScreen() {
      return this.screen;
   }
}
