package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class MoveEvent extends Event {
   private class_243 movement;

   @Generated
   public class_243 getMovement() {
      return this.movement;
   }

   @Generated
   public void setMovement(class_243 movement) {
      this.movement = movement;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof MoveEvent other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         class_243 this$movement = this.getMovement();
         class_243 other$movement = other.getMovement();
         return this$movement == null ? other$movement == null : this$movement.equals(other$movement);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof MoveEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      class_243 $movement = this.getMovement();
      return result * 59 + ($movement == null ? 43 : $movement.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "MoveEvent(movement=" + this.getMovement() + ")";
   }

   @Generated
   public MoveEvent(class_243 movement) {
      this.movement = movement;
   }
}
