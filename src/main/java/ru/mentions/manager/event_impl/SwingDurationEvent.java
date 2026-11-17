package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class SwingDurationEvent extends CancellableEvent {
   private float animation;

   @Generated
   public float getAnimation() {
      return this.animation;
   }

   @Generated
   public void setAnimation(float animation) {
      this.animation = animation;
   }
}
