package ru.mentions.manager.event_impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class EventLook extends CancellableEvent {
   public double yaw;
   public double pitch;

   public EventLook(double yaw, double pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public double getYaw() {
      return this.yaw;
   }

   public void setYaw(double yaw) {
      this.yaw = yaw;
   }

   public double getPitch() {
      return this.pitch;
   }

   public void setPitch(double pitch) {
      this.pitch = pitch;
   }
}
