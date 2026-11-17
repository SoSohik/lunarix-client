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
import ru.mentions.utils.aura.GCDUtil;
import ru.mentions.utils.aura.RayTraceUtil;
import ru.mentions.utils.aura.UBoxPoints;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class ShardACRotation implements RotationAura {
   private final ShardACRotation.Axis yaw = new ShardACRotation.Axis();
   private final ShardACRotation.Axis pitch = new ShardACRotation.Axis();
   private class_1309 lastTarget = null;

   public ShardACRotation.Angles update(float currentYaw, float currentPitch, float targetYaw, float targetPitch, float maxSpeed) {
      if (Float.isFinite(currentYaw) && Float.isFinite(currentPitch) && Float.isFinite(targetYaw) && Float.isFinite(targetPitch) && Float.isFinite(maxSpeed)) {
         float speed = clamp(maxSpeed, 4.0F, 70.0F);
         float yawError = wrap(targetYaw - currentYaw);
         float pitchError = clamp(targetPitch, -90.0F, 90.0F) - currentPitch;
         return new ShardACRotation.Angles(
            currentYaw + this.yaw.step(yawError, speed), clamp(currentPitch + this.pitch.step(pitchError, speed * 0.8F), -90.0F, 90.0F)
         );
      } else {
         this.reset();
         return new ShardACRotation.Angles(
            Float.isFinite(currentYaw) ? currentYaw : 0.0F, Float.isFinite(currentPitch) ? clamp(currentPitch, -90.0F, 90.0F) : 0.0F
         );
      }
   }

   public void reset() {
      this.yaw.reset();
      this.pitch.reset();
   }

   private static float wrap(float angle) {
      float wrapped = angle % 360.0F;
      if (wrapped >= 180.0F) {
         wrapped -= 360.0F;
      }

      if (wrapped < -180.0F) {
         wrapped += 360.0F;
      }

      return wrapped;
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         if (this.lastTarget != target) {
            this.reset();
            this.lastTarget = target;
         }

         class_243 bestVec = UBoxPoints.getBestVector3dOnEntityBox(target.method_5829());
         if (bestVec != null) {
            class_243 dir = bestVec.method_1020(mc.field_1724.method_33571());
            float targetYaw = (float)Math.toDegrees(Math.atan2(-dir.field_1352, dir.field_1350));
            float targetPitch = (float)class_3532.method_15350(
               -Math.toDegrees(Math.atan2(dir.field_1351, Math.hypot(dir.field_1352, dir.field_1350))), -90.0, 90.0
            );
            float currentYaw = AttackAura.lastYaw != 0.0F ? AttackAura.lastYaw : mc.field_1724.method_36454();
            float currentPitch = AttackAura.lastPitch != 0.0F ? AttackAura.lastPitch : mc.field_1724.method_36455();
            float maxSpeed = MathUtil.randomLerp(45.0F, 65.0F);
            if (RayTraceUtil.rayTraceEntity(
               mc.field_1724.method_36454(), mc.field_1724.method_36455(), (double)aura.attackRange.getValue().floatValue(), target
            )) {
               maxSpeed = MathUtil.randomLerp(25.0F, 38.0F);
            } else if (canAttack) {
               maxSpeed = MathUtil.randomLerp(55.0F, 70.0F);
            }

            ShardACRotation.Angles angles = this.update(currentYaw, currentPitch, targetYaw, targetPitch, maxSpeed);
            float nextYaw = currentYaw + GCDUtil.getSensitivity(angles.yaw() - currentYaw);
            float nextPitch = currentPitch + GCDUtil.getSensitivity(angles.pitch() - currentPitch);
            nextPitch = clamp(nextPitch, -90.0F, 90.0F);
            AttackAura.lastYaw = nextYaw;
            AttackAura.lastPitch = nextPitch;
            RotationProcess.update(new Rotation(nextYaw, nextPitch), maxSpeed, maxSpeed * 0.8F, maxSpeed, maxSpeed * 0.8F, 1, 6, false);
         }
      } else {
         this.reset();
      }
   }

   public static record Angles(float yaw, float pitch) {
   }

   private static final class Axis {
      private float velocity;
      private float acceleration;

      float step(float error, float maxSpeed) {
         if (Math.abs(error) < 0.01F) {
            this.reset();
            return 0.0F;
         } else {
            float accelerationLimit = maxSpeed * 0.35F;
            float brakingSpeed = (float)Math.sqrt((double)(2.0F * accelerationLimit * Math.abs(error)));
            float desiredVelocity = Math.copySign(Math.min(maxSpeed, Math.min(brakingSpeed, Math.abs(error) * 0.65F)), error);
            float desiredAcceleration = ShardACRotation.clamp(desiredVelocity - this.velocity, -accelerationLimit, accelerationLimit);
            this.acceleration = this.acceleration
               + ShardACRotation.clamp(desiredAcceleration - this.acceleration, -accelerationLimit * 0.5F, accelerationLimit * 0.5F);
            this.velocity = ShardACRotation.clamp(this.velocity + this.acceleration, -maxSpeed, maxSpeed);
            if (Math.signum(this.velocity) == Math.signum(error) && Math.abs(this.velocity) > Math.abs(error)) {
               this.velocity = error;
               this.acceleration = 0.0F;
            }

            return this.velocity;
         }
      }

      void reset() {
         this.velocity = 0.0F;
         this.acceleration = 0.0F;
      }
   }
}
