package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_4587;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class HandAnimationEvent extends CancellableEvent {
   private class_4587 matrices;
   private class_1268 hand;
   private float swingProgress;
   private float equipProgress;
   private class_1306 arm;

   @Generated
   public HandAnimationEvent(class_4587 matrices, class_1268 hand, float swingProgress, float equipProgress, class_1306 arm) {
      this.matrices = matrices;
      this.hand = hand;
      this.swingProgress = swingProgress;
      this.equipProgress = equipProgress;
      this.arm = arm;
   }

   @Generated
   public class_4587 getMatrices() {
      return this.matrices;
   }

   @Generated
   public class_1268 getHand() {
      return this.hand;
   }

   @Generated
   public float getSwingProgress() {
      return this.swingProgress;
   }

   @Generated
   public float getEquipProgress() {
      return this.equipProgress;
   }

   @Generated
   public class_1306 getArm() {
      return this.arm;
   }

   @Generated
   public void setMatrices(class_4587 matrices) {
      this.matrices = matrices;
   }

   @Generated
   public void setHand(class_1268 hand) {
      this.hand = hand;
   }

   @Generated
   public void setSwingProgress(float swingProgress) {
      this.swingProgress = swingProgress;
   }

   @Generated
   public void setEquipProgress(float equipProgress) {
      this.equipProgress = equipProgress;
   }

   @Generated
   public void setArm(class_1306 arm) {
      this.arm = arm;
   }
}
