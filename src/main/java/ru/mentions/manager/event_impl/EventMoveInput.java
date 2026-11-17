package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class EventMoveInput extends CancellableEvent {
   private class_10185 input;
   private float forward;
   private float strafe;

   public void setDirectionalLow(boolean forward, boolean backward, boolean left, boolean right) {
      this.input = new class_10185(forward, backward, left, right, this.input.comp_3163(), this.input.comp_3164(), this.input.comp_3165());
   }

   @Generated
   public class_10185 getInput() {
      return this.input;
   }

   @Generated
   public float getForward() {
      return this.forward;
   }

   @Generated
   public float getStrafe() {
      return this.strafe;
   }

   @Generated
   public void setInput(class_10185 input) {
      this.input = input;
   }

   @Generated
   public void setForward(float forward) {
      this.forward = forward;
   }

   @Generated
   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   @Generated
   public EventMoveInput(class_10185 input, float forward, float strafe) {
      this.input = input;
      this.forward = forward;
      this.strafe = strafe;
   }
}
