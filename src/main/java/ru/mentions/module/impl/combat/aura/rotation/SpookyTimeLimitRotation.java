package ru.mentions.module.impl.combat.aura.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.aura.UBoxPoints;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class SpookyTimeLimitRotation implements RotationAura {
   private static final float BASE_SPEED = 35.0F;
   private static final float TIME_AMPLITUDE = 1.5F;
   private static final double TIME_FREQUENCY = 0.007;
   private static final float YAW_WINDOW_MIN = 65.0F;
   private static final float YAW_WINDOW_MAX = 115.0F;
   private static final float SPECIAL_SPEED_MULTIPLIER = 0.78F;
   private static final float WINDOW_SPEED_MULTIPLIER = 0.84F;
   private static final float NORMAL_SPEED_MULTIPLIER = 0.9F;
   private static final float INITIAL_SPEED_CAP = 10.5F;
   private static final float INITIAL_SPEED_FACTOR = 0.35F;
   private static final float ACCELERATION = 0.42F;
   private static final float EASE_ANGLE = 35.0F;
   private static final float EASE_MIN = 0.28F;
   private static final float EASE_MAX = 0.72F;
   private static final float MIN_PITCH = -89.5F;
   private static final float MAX_PITCH = 89.5F;
   private float currentSpeed;
   private class_1309 lastTarget;

   public float getCurrentSpeed() {
      return this.currentSpeed;
   }

   public void reset() {
      this.currentSpeed = 0.0F;
      this.lastTarget = null;
   }

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         class_243 aim = this.aimPoint(target);
         Rotation desired = new Rotation((float)Math.toDegrees(Math.atan2(-aim.field_1352, aim.field_1350)), this.pitch(aim));
         Rotation current = new Rotation(mc.field_1724.method_36454(), mc.field_1724.method_36455());
         Rotation next = this.limitAngleChange(desired, current, target, false);
         float yawStep = Math.abs(class_3532.method_15393(next.yaw - current.yaw));
         float pitchStep = Math.abs(next.pitch - current.pitch);
         RotationProcess.update(next, yawStep, pitchStep, 360.0F, 360.0F, 3, 1, false);
      }
   }

   private Rotation limitAngleChange(Rotation desired, Rotation current, class_1309 target, boolean specialTarget) {
      if (desired == null) {
         return current;
      } else {
         if (current == null) {
            current = new Rotation(0.0F, 0.0F);
         }

         float deltaYaw = class_3532.method_15393(desired.yaw - current.yaw);
         float deltaPitch = desired.pitch - current.pitch;
         float totalDelta = (float)Math.hypot((double)deltaYaw, (double)deltaPitch);
         if (totalDelta < 35.0F) {
            this.currentSpeed = 0.0F;
            return new Rotation(desired.yaw, clampPitch(desired.pitch));
         } else {
            float targetSpeed = 35.0F + (float)Math.sin((double)System.currentTimeMillis() * 0.007) * 1.5F;
            float absYaw = Math.abs(deltaYaw);
            boolean windowed = absYaw < 65.0F ? totalDelta >= 65.0F && totalDelta <= 115.0F : absYaw <= 115.0F;
            if (windowed) {
               targetSpeed *= specialTarget ? 0.78F : 0.84F;
            } else {
               targetSpeed *= 0.9F;
            }

            if (this.lastTarget != target) {
               this.lastTarget = target;
               this.currentSpeed = Math.min(10.5F, targetSpeed * 0.35F);
            }

            if (this.currentSpeed < targetSpeed) {
               this.currentSpeed = this.currentSpeed + (targetSpeed - this.currentSpeed) * 0.42F;
            } else {
               this.currentSpeed = targetSpeed;
            }

            float ease = 1.0F;
            if (totalDelta < 35.0F) {
               float t = totalDelta / 35.0F;
               float smooth = t * t * (3.0F - 2.0F * t);
               ease = 0.28F + 0.72F * smooth;
            }

            float step = Math.min(this.currentSpeed * ease, totalDelta);
            step = Math.min(step, targetSpeed);
            if (step <= 0.0F) {
               return current;
            } else {
               float yawStep = deltaYaw / totalDelta * step;
               float pitchStep = deltaPitch / totalDelta * step;
               return new Rotation(current.yaw + yawStep, clampPitch(current.pitch + pitchStep));
            }
         }
      }
   }

   private class_243 aimPoint(class_1309 target) {
      class_243 eye = mc.field_1724.method_33571();

      try {
         return UBoxPoints.getBestVector3dOnEntityBox(target.method_5829(), true).method_1020(eye).method_1029();
      } catch (Throwable var4) {
         return target.method_33571().method_1020(eye).method_1029();
      }
   }

   private float pitch(class_243 vec) {
      return (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -89.5, 89.5);
   }

   private static float clampPitch(float pitch) {
      return MathUtil.clamp(pitch, -89.5F, 89.5F);
   }
}
