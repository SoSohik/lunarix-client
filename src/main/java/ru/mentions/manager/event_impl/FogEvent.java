package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class FogEvent extends CancellableEvent {
   private float distance;
   private int color;

   @Generated
   public float getDistance() {
      return this.distance;
   }

   @Generated
   public int getColor() {
      return this.color;
   }

   @Generated
   public void setDistance(float distance) {
      this.distance = distance;
   }

   @Generated
   public void setColor(int color) {
      this.color = color;
   }
}
