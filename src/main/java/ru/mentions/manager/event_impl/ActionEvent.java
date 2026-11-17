package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class ActionEvent extends Event {
   private boolean sprintState;

   @Generated
   public boolean isSprintState() {
      return this.sprintState;
   }

   @Generated
   public void setSprintState(boolean sprintState) {
      this.sprintState = sprintState;
   }

   @Generated
   @Override
   public String toString() {
      return "ActionEvent(sprintState=" + this.isSprintState() + ")";
   }

   @Generated
   public ActionEvent(boolean sprintState) {
      this.sprintState = sprintState;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ActionEvent other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         return !super.equals(o) ? false : this.isSprintState() == other.isSprintState();
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ActionEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      return result * 59 + (this.isSprintState() ? 79 : 97);
   }
}
