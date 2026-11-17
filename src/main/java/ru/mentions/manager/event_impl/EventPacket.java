package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2596;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class EventPacket extends CancellableEvent {
   private class_2596<?> packet;
   private EventPacket.Type type;

   public boolean isSend() {
      return this.type.equals(EventPacket.Type.SEND);
   }

   @Generated
   public class_2596<?> getPacket() {
      return this.packet;
   }

   @Generated
   public EventPacket.Type getType() {
      return this.type;
   }

   @Generated
   public void setPacket(class_2596<?> packet) {
      this.packet = packet;
   }

   @Generated
   public void setType(EventPacket.Type type) {
      this.type = type;
   }

   @Generated
   public EventPacket(class_2596<?> packet, EventPacket.Type type) {
      this.packet = packet;
      this.type = type;
   }

   @Environment(EnvType.CLIENT)
   public static enum Type {
      SEND,
      RECEIVE;
   }
}
