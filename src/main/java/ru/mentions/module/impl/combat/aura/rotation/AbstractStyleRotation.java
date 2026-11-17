package ru.mentions.module.impl.combat.aura.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.ai.StyleSource;
import ru.mentions.manager.ai.StyleSpec;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.aura.GCDUtil;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public abstract class AbstractStyleRotation implements RotationAura {
   private static final float MAX_STEP_YAW = 55.0F;
   private static final float MAX_STEP_PITCH = 30.0F;
   private static final float DEAD_ZONE_YAW = 0.4F;
   private static final float DEAD_ZONE_PITCH = 0.4F;
   private static final float MIN_SPEED_YAW = 2.5F;
   private static final float MIN_SPEED_PITCH = 1.5F;
   private static final float ACCEL_UP = 0.7F;
   private static final float ACCEL_DOWN = 0.65F;
   private static final float JERK_UP_YAW = 26.0F;
   private static final float JERK_UP_PITCH = 15.0F;
   private static final float JERK_DOWN_YAW = 30.0F;
   private static final float JERK_DOWN_PITCH = 18.0F;
   private static final float FALLBACK_RESPONSE = 0.6F;
   private static final float JITTER_YAW_MAX = 1.5F;
   private static final float JITTER_PITCH_MAX = 1.0F;
   private static final float JITTER_FADE_ERROR = 12.0F;
   private static final float HIT_SMOOTHING = 0.35F;
   private static final double AIM_OFFSET_KEEP = 0.3;
   private static final int RETURN_YAW_MIN = 45;
   private static final int RETURN_YAW_MAX = 75;
   private static final int RETURN_PITCH_MIN = 32;
   private static final int RETURN_PITCH_MAX = 55;
   private static final int AIM_TIMEOUT = 4;
   private static final int AIM_PRIORITY = 15;
   private boolean initialized;
   private float lastPlayerYaw;
   private float lastPlayerPitch;
   private float lastCameraYaw;
   private float lastCameraPitch;
   private float smoothYawSpeed;
   private float smoothPitchSpeed;
   private float prevAppliedYaw;
   private float prevAppliedPitch;
   private float prevPrevAppliedYaw;
   private float prevPrevAppliedPitch;
   private float lastHitX = 0.5F;
   private float lastHitY = 0.5F;
   private float lastHitZ = 0.5F;
   private float lastSmoothness = 1.0F;
   private float lastPeak;
   private int sinceAttack = 60;

   protected abstract StyleSource styleSource();

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && mc.field_1687 != null && target != null) {
         StyleSource source = this.styleSource();
         if (source != null && source.hasStyle()) {
            float playerYaw = mc.field_1724.method_36454();
            float playerPitch = mc.field_1724.method_36455();
            float cameraYaw = Rotation.cameraYaw();
            float cameraPitch = Rotation.cameraPitch();
            float appliedYaw = this.initialized ? class_3532.method_15393(playerYaw - this.lastPlayerYaw) : 0.0F;
            float appliedPitch = this.initialized ? playerPitch - this.lastPlayerPitch : 0.0F;
            float visibleYaw = this.initialized ? class_3532.method_15393(cameraYaw - this.lastCameraYaw) : 0.0F;
            float visiblePitch = this.initialized ? cameraPitch - this.lastCameraPitch : 0.0F;
            this.initialized = true;
            if (aura != null && aura.justAttacked) {
               aura.justAttacked = false;
               this.sinceAttack = 0;
            } else {
               this.sinceAttack = Math.min(this.sinceAttack + 1, 60);
            }

            class_243 eye = mc.field_1724.method_33571();
            class_238 box = target.method_5829();
            class_243 center = box.method_1005().method_1020(eye);
            float distance = (float)eye.method_1022(box.method_1005());
            float yawErr = class_3532.method_15393(yawTo(center) - playerYaw);
            float pitchErr = class_3532.method_15363(pitchTo(center) - playerPitch, -90.0F, 90.0F);
            class_243 aim = aimPoint(box, this.lastHitX, this.lastHitY, this.lastHitZ);
            class_243 toAim = aim.method_1020(eye);
            float aimYawErr = class_3532.method_15393(yawTo(toAim) - playerYaw);
            float aimPitchErr = class_3532.method_15363(pitchTo(toAim) - playerPitch, -90.0F, 90.0F);
            float aimError = (float)Math.hypot((double)aimYawErr, (double)aimPitchErr);
            boolean settling = Math.abs(yawErr) <= 55.0F && Math.abs(pitchErr) <= 30.0F;
            float[] gesture = settling ? source.nextGesture(yawErr, pitchErr, (float)Math.min(this.sinceAttack, 60)) : null;
            float recordedSpeedYaw = 55.0F;
            float recordedSpeedPitch = 30.0F;
            float recordedStepYaw;
            float recordedStepPitch;
            boolean fromRecord;
            if (gesture != null) {
               recordedStepYaw = Math.abs(StyleSpec.fin(gesture[0]));
               recordedStepPitch = Math.abs(StyleSpec.fin(gesture[1]));
               recordedSpeedYaw = recordedStepYaw;
               recordedSpeedPitch = recordedStepPitch;
               fromRecord = true;
            } else {
               double[] features = StyleSpec.features(
                  yawErr,
                  pitchErr,
                  this.prevAppliedYaw,
                  this.prevAppliedPitch,
                  this.prevPrevAppliedYaw,
                  this.prevPrevAppliedPitch,
                  this.lastHitX,
                  this.lastHitY,
                  this.lastHitZ,
                  distance,
                  horizontalSpeed(target),
                  (float)this.sinceAttack,
                  this.lastSmoothness,
                  this.lastPeak
               );
               double[] prediction = source.predict(features);
               if (prediction != null && prediction.length >= 9) {
                  this.lastHitX = smoothHit(this.lastHitX, (float)prediction[2]);
                  this.lastHitY = smoothHit(this.lastHitY, (float)prediction[3]);
                  this.lastHitZ = smoothHit(this.lastHitZ, (float)prediction[4]);
                  this.lastSmoothness = StyleSpec.clamp01((float)prediction[5]);
                  this.lastPeak = StyleSpec.clamp01((float)prediction[6]);
                  recordedStepYaw = Math.abs(StyleSpec.unitToSigned((float)prediction[0])) * 35.0F;
                  recordedStepPitch = Math.abs(StyleSpec.unitToSigned((float)prediction[1])) * 22.0F;
                  recordedSpeedYaw = StyleSpec.clamp((float)prediction[7] * 180.0F, 2.5F, 55.0F);
                  recordedSpeedPitch = StyleSpec.clamp((float)prediction[8] * 120.0F, 1.5F, 30.0F);
                  fromRecord = true;
               } else {
                  recordedStepYaw = Math.abs(aimYawErr) * 0.6F;
                  recordedStepPitch = Math.abs(aimPitchErr) * 0.6F;
                  fromRecord = false;
               }
            }

            float stepYaw = 0.0F;
            float stepPitch = 0.0F;
            boolean onTarget = Math.abs(aimYawErr) < 0.4F && Math.abs(aimPitchErr) < 0.4F;
            if (!onTarget) {
               float yawMagnitude = Math.min(recordedStepYaw, Math.abs(aimYawErr));
               float pitchMagnitude = Math.min(recordedStepPitch, Math.abs(aimPitchErr));
               if (fromRecord) {
                  float handSpeed = (float)Math.hypot((double)appliedYaw, (double)appliedPitch);
                  float visibleSpeed = (float)Math.hypot((double)visibleYaw, (double)visiblePitch);
                  float[] stroke = source.nextStroke(Math.max(handSpeed, visibleSpeed));
                  if (stroke != null && stroke.length >= 2) {
                     float settled = 1.0F - StyleSpec.clamp01(aimError / 12.0F);
                     float jitterScale = (1.0F - settled) * (1.0F + (1.0F - StyleSpec.clamp01(this.lastSmoothness)) * 0.9F);
                     float jitterYaw = class_3532.method_15363(StyleSpec.fin(stroke[0]) * jitterScale, -1.5F, 1.5F);
                     float jitterPitch = class_3532.method_15363(StyleSpec.fin(stroke[1]) * jitterScale, -1.0F, 1.0F);
                     yawMagnitude = class_3532.method_15363(yawMagnitude + jitterYaw, 0.0F, Math.abs(aimYawErr));
                     pitchMagnitude = class_3532.method_15363(pitchMagnitude + jitterPitch, 0.0F, Math.abs(aimPitchErr));
                  }
               }

               float yawSpeedLimit = this.speedCeiling(true, Math.max(StyleSpec.fin(recordedSpeedYaw), yawMagnitude), 55.0F);
               float pitchSpeedLimit = this.speedCeiling(false, Math.max(StyleSpec.fin(recordedSpeedPitch), pitchMagnitude), 30.0F);
               stepYaw = towards(aimYawErr, Math.min(yawMagnitude, yawSpeedLimit));
               stepPitch = towards(aimPitchErr, Math.min(pitchMagnitude, pitchSpeedLimit));
            }

            if (Float.isFinite(stepYaw) && Float.isFinite(stepPitch)) {
               stepYaw = class_3532.method_15363(stepYaw, -55.0F, 55.0F);
               stepPitch = class_3532.method_15363(stepPitch, -30.0F, 30.0F);
               stepYaw = quantizeDown(stepYaw, aimYawErr);
               stepPitch = quantizeDown(stepPitch, aimPitchErr);
               RotationProcess.update(
                  new Rotation(playerYaw + stepYaw, class_3532.method_15363(playerPitch + stepPitch, -90.0F, 90.0F)),
                  Math.max(0.5F, Math.abs(stepYaw)),
                  Math.max(0.5F, Math.abs(stepPitch)),
                  (float)MathUtil.randomInt(45, 75),
                  (float)MathUtil.randomInt(32, 55),
                  4,
                  15,
                  false
               );
               this.prevPrevAppliedYaw = this.prevAppliedYaw;
               this.prevPrevAppliedPitch = this.prevAppliedPitch;
               this.prevAppliedYaw = appliedYaw;
               this.prevAppliedPitch = appliedPitch;
               this.lastPlayerYaw = playerYaw;
               this.lastPlayerPitch = playerPitch;
               this.lastCameraYaw = cameraYaw;
               this.lastCameraPitch = cameraPitch;
            } else {
               this.smoothYawSpeed = 0.0F;
               this.smoothPitchSpeed = 0.0F;
            }
         } else {
            this.reset();
         }
      }
   }

   public void reset() {
      this.initialized = false;
      this.smoothYawSpeed = 0.0F;
      this.smoothPitchSpeed = 0.0F;
      this.prevAppliedYaw = 0.0F;
      this.prevAppliedPitch = 0.0F;
      this.prevPrevAppliedYaw = 0.0F;
      this.prevPrevAppliedPitch = 0.0F;
      this.lastHitX = 0.5F;
      this.lastHitY = 0.5F;
      this.lastHitZ = 0.5F;
      this.lastSmoothness = 1.0F;
      this.lastPeak = 0.0F;
      this.sinceAttack = 60;
   }

   private float speedCeiling(boolean yawAxis, float demand, float limit) {
      float minSpeed = yawAxis ? 2.5F : 1.5F;
      float want = class_3532.method_15363(StyleSpec.fin(demand), minSpeed, limit);
      float current = yawAxis ? this.smoothYawSpeed : this.smoothPitchSpeed;
      float blend = want >= current ? 0.7F : 0.65F;
      float jerk = class_3532.method_15363((want - current) * blend, yawAxis ? -30.0F : -18.0F, yawAxis ? 26.0F : 15.0F);
      float next = class_3532.method_15363(current + jerk, 0.0F, limit);
      if (yawAxis) {
         this.smoothYawSpeed = next;
      } else {
         this.smoothPitchSpeed = next;
      }

      return next;
   }

   private static float towards(float error, float magnitude) {
      if (error != 0.0F && !(magnitude <= 0.0F)) {
         return error > 0.0F ? magnitude : -magnitude;
      } else {
         return 0.0F;
      }
   }

   private static float quantizeDown(float step, float error) {
      float magnitude = Math.abs(step);
      if (Float.isFinite(magnitude) && !(magnitude <= 0.0F)) {
         float quantum = GCDUtil.getGCDValue();
         if (Float.isFinite(quantum) && !(quantum <= 0.0F)) {
            float cap = Math.min(magnitude, Math.abs(error));
            int steps = (int)Math.floor((double)(cap / quantum + 1.0E-4F));
            float quantized = (float)steps * quantum;

            while (quantized > cap && steps > 0) {
               quantized = (float)(--steps) * quantum;
            }

            if (quantized <= 1.0E-4F) {
               return 0.0F;
            } else {
               return step < 0.0F ? -quantized : quantized;
            }
         } else {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   private static class_243 aimPoint(class_238 box, float hitX, float hitY, float hitZ) {
      class_243 center = box.method_1005();
      double x = class_3532.method_16436((double)StyleSpec.clamp01(hitX), box.field_1323, box.field_1320);
      double y = class_3532.method_16436((double)StyleSpec.clamp01(hitY), box.field_1322, box.field_1325);
      double z = class_3532.method_16436((double)StyleSpec.clamp01(hitZ), box.field_1321, box.field_1324);
      return new class_243(
         class_3532.method_16436(0.3, center.field_1352, x),
         class_3532.method_16436(0.3, center.field_1351, y),
         class_3532.method_16436(0.3, center.field_1350, z)
      );
   }

   private static float smoothHit(float previous, float predicted) {
      float target = StyleSpec.clamp01(predicted);
      return StyleSpec.fin(previous + (target - previous) * 0.35F);
   }

   private static float horizontalSpeed(class_1309 entity) {
      class_243 velocity = entity.method_18798();
      return (float)Math.hypot(velocity.field_1352, velocity.field_1350);
   }

   private static float yawTo(class_243 aim) {
      return (float)Math.toDegrees(Math.atan2(-aim.field_1352, aim.field_1350));
   }

   private static float pitchTo(class_243 aim) {
      return (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(aim.field_1351, Math.hypot(aim.field_1352, aim.field_1350))), -90.0, 90.0);
   }
}
