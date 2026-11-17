package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class UsingItemEvent extends CancellableEvent {
   byte type;

   @Generated
   public byte getType() {
      return this.type;
   }

   @Generated
   public void setType(byte type) {
      this.type = type;
   }

   @Generated
   public UsingItemEvent(byte type) {
      this.type = type;
   }
}
