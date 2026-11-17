package ru.mentions.manager.rotation;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_3532;
import org.joml.Vector2f;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public class Rotation implements IMinecraft {
   public float yaw;
   public float pitch;

   public Rotation(class_1297 entity) {
      this.yaw = entity.method_36454();
      this.pitch = entity.method_36455();
   }

   public Rotation(float yawN, float pitchN) {
      this.yaw = yawN;
      this.pitch = pitchN;
   }

   public float getDelta(Rotation target) {
      float yawDelta = class_3532.method_15393(target.yaw - this.yaw);
      float pitchDelta = target.pitch - this.pitch;
      return (float)Math.hypot((double)Math.abs(yawDelta), (double)Math.abs(pitchDelta));
   }

   public double getDeltaDouble(Rotation target) {
      double yawDelta = (double)class_3532.method_15393(target.yaw - this.yaw);
      double pitchDelta = (double)class_3532.method_15393(target.pitch - this.pitch);
      return Math.hypot(yawDelta, pitchDelta);
   }

   public static Vector2f camera() {
      return new Vector2f(cameraYaw(), cameraPitch());
   }

   public static float cameraYaw() {
      return class_3532.method_15393(mc.field_1773.method_19418().method_19330() + (float)(mc.field_1690.method_31044().method_31035() ? 180 : 0));
   }

   public static float cameraPitch() {
      return (float)(mc.field_1690.method_31044().method_31035() ? -1 : 1) * mc.field_1773.method_19418().method_19329();
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }
}
