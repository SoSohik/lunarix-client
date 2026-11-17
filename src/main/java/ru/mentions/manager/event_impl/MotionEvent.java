package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class MotionEvent extends CancellableEvent {
   private double x;
   private double y;
   private double z;
   private double yaw;
   private double pitch;
   private boolean ground;

   @Generated
   public double x() {
      return this.x;
   }

   @Generated
   public double y() {
      return this.y;
   }

   @Generated
   public double z() {
      return this.z;
   }

   @Generated
   public double yaw() {
      return this.yaw;
   }

   @Generated
   public double pitch() {
      return this.pitch;
   }

   @Generated
   public boolean ground() {
      return this.ground;
   }

   @Generated
   public MotionEvent x(double x) {
      this.x = x;
      return this;
   }

   @Generated
   public MotionEvent y(double y) {
      this.y = y;
      return this;
   }

   @Generated
   public MotionEvent z(double z) {
      this.z = z;
      return this;
   }

   @Generated
   public MotionEvent yaw(double yaw) {
      this.yaw = yaw;
      return this;
   }

   @Generated
   public MotionEvent pitch(double pitch) {
      this.pitch = pitch;
      return this;
   }

   @Generated
   public MotionEvent ground(boolean ground) {
      this.ground = ground;
      return this;
   }

   @Generated
   public MotionEvent(double x, double y, double z, double yaw, double pitch, boolean ground) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
      this.ground = ground;
   }
}
