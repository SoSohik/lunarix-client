package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class AttackEvent extends CancellableEvent {
   private class_1297 target;

   @Generated
   public class_1297 getTarget() {
      return this.target;
   }

   @Generated
   public void setTarget(class_1297 target) {
      this.target = target;
   }

   @Generated
   public AttackEvent(class_1297 target) {
      this.target = target;
   }
}
