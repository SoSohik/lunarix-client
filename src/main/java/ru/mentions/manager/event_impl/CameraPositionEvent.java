package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class CameraPositionEvent extends Event {
   private class_243 pos;

   @Generated
   public class_243 getPos() {
      return this.pos;
   }

   @Generated
   public void setPos(class_243 pos) {
      this.pos = pos;
   }

   @Generated
   public CameraPositionEvent(class_243 pos) {
      this.pos = pos;
   }
}
