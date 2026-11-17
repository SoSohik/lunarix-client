package ru.mentions.module.impl.combat.aura.rotation;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class StyleDeRotation implements RotationAura {
   private static final float MAX_STEP = 50.0F;
   private final Random random = new Random();
   private double smoothedYawNoise;
   private double smoothedPitchNoise;
   private double noisePhase;
   private double curveDirection = 1.0;
   private double previousAngleError;
   private String lastEngine = "";
   private float lastTargetYaw;
   private float lastTargetPitch;
   private int lastTargetPlayerAge = Integer.MIN_VALUE;
   private float smoothedTargetPitchSpeed;
   private float smoothedTargetYawSpeed;
   private CursorTrajectoryLibrary.TrajectoryMatch match;
   private int frameIndex;
   private static final double MICRO_DELTA_THRESHOLD = 1.5;
   private static final int MAX_SKIPPED_INITIAL_FRAMES = 6;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         String engine = aura.deStyleEngine != null ? aura.deStyleEngine.getValue() : "Param";
         if (!engine.equals(this.lastEngine)) {
            this.lastEngine = engine;
            this.resetPlayback();
         }

         class_243 eye = mc.field_1724.method_33571();
         double drift = 0.0;
         if (aura.deMultipoint != null && aura.deMultipoint.getValue()) {
            drift = Math.sin((double)System.currentTimeMillis() / 700.0)
               * (double)(aura.deMultipointStrength != null ? aura.deMultipointStrength.getValue() : 0.6F);
         }

         class_243 aim = DeRotUtil.aimPoint(target.method_5829(), eye, drift, drift * 0.6).method_1020(eye);
         float desiredYaw = DeRotUtil.yawFromDir(aim);
         float desiredPitch = class_3532.method_15363(DeRotUtil.pitchFromDir(aim), -90.0F, 90.0F);
         this.updateTargetSpeed(desiredYaw, desiredPitch);
         float curYaw = mc.field_1724.method_36454();
         float curPitch = mc.field_1724.method_36455();
         float yawErr = class_3532.method_15393(desiredYaw - curYaw);
         float pitchErr = desiredPitch - curPitch;
         float yaw;
         float pitch;
         if ("Library".equals(engine)) {
            float[] result = this.playLibrary(aura, curYaw, curPitch, yawErr, pitchErr);
            yaw = result[0];
            pitch = result[1];
         } else {
            float[] result = this.playParam(aura, curYaw, curPitch, desiredYaw, desiredPitch);
            yaw = result[0];
            pitch = result[1];
         }

         yaw = curYaw + DeRotUtil.quantizeStep(class_3532.method_15393(yaw - curYaw));
         pitch = class_3532.method_15363(curPitch + DeRotUtil.quantizeStep(pitch - curPitch), -90.0F, 90.0F);
         RotationProcess.update(
            new Rotation(yaw, pitch),
            Math.max(1.0F, Math.abs(class_3532.method_15393(yaw - curYaw))),
            Math.max(1.0F, Math.abs(pitch - curPitch)),
            (float)MathUtil.randomInt(50, 80),
            (float)MathUtil.randomInt(32, 55),
            MathUtil.randomInt(2, 4),
            15,
            false
         );
      }
   }

   private float[] playParam(AttackAura aura, float curYaw, float curPitch, float desiredYaw, float desiredPitch) {
      float gain = aura.deParamGain != null ? aura.deParamGain.getValue() : 0.3F;
      float arc = aura.deParamArc != null ? aura.deParamArc.getValue() : 0.3F;
      float jitter = aura.deParamJitter != null ? aura.deParamJitter.getValue() : 0.5F;
      float yawErr = class_3532.method_15393(desiredYaw - curYaw);
      float pitchErr = desiredPitch - curPitch;
      double error = Math.hypot((double)yawErr, (double)pitchErr);
      if (error < 0.01) {
         return new float[]{curYaw, curPitch};
      } else {
         if (this.previousAngleError < 2.0 && error > 8.0) {
            this.curveDirection = this.random.nextBoolean() ? 1.0 : -1.0;
         }

         this.previousAngleError = error;
         double ux = (double)yawErr / error;
         double uy = (double)pitchErr / error;
         double perp = -uy;
         double arcTerm = (double)arc * this.curveDirection;
         double jitterScale = (double)jitter * (error / 45.0);
         double jx = this.random.nextGaussian() * jitterScale;
         double jy = this.random.nextGaussian() * jitterScale;
         float yawStep = (float)((double)(gain * yawErr) + arcTerm * error * perp + jx);
         float pitchStep = (float)((double)(gain * pitchErr) + arcTerm * error * ux + jy);
         if (Math.abs(yawErr) < 3.0F) {
            yawStep = yawErr;
         }

         if (Math.abs(pitchErr) < 3.0F) {
            pitchStep = pitchErr;
         }

         yawStep = this.clampStep(yawStep);
         pitchStep = this.clampStep(pitchStep);
         if ((double)gain <= 1.0) {
            if (Math.abs(yawStep) > Math.abs(yawErr) && Math.signum(yawStep) == Math.signum(yawErr) && Math.abs(yawErr) < 3.0F) {
               yawStep = yawErr;
            }

            if (Math.abs(pitchStep) > Math.abs(pitchErr) && Math.signum(pitchStep) == Math.signum(pitchErr) && Math.abs(pitchErr) < 3.0F) {
               pitchStep = pitchErr;
            }
         }

         return new float[]{curYaw + yawStep, class_3532.method_15363(curPitch + pitchStep, -90.0F, 90.0F)};
      }
   }

   private float[] playLibrary(AttackAura aura, float curYaw, float curPitch, float yawErr, float pitchErr) {
      double replaySpeed = aura.deReplaySpeed != null ? (double)aura.deReplaySpeed.getValue().floatValue() : 2.5;
      double yawLock = aura.deYawLock != null ? (double)aura.deYawLock.getValue().floatValue() : 1.0;
      double pitchDamp = aura.dePitchDamp != null ? (double)aura.dePitchDamp.getValue().floatValue() : 0.3;
      double shake = aura.deShake != null ? (double)aura.deShake.getValue().floatValue() : 1.2;
      double mirrorChance = aura.deStyleReverse != null ? (double)aura.deStyleReverse.getValue().floatValue() : 0.3;
      if (this.match == null || this.frameIndex >= this.match.trajectory.length()) {
         CursorTrajectoryLibrary lib = CursorTrajectoryLibrary.getCached();
         if (lib == null || lib.isEmpty()) {
            return this.playParam(aura, curYaw, curPitch, curYaw + yawErr, curPitch + pitchErr);
         }

         boolean mirrored = ThreadLocalRandom.current().nextDouble() < mirrorChance;
         this.match = lib.findMatch((double)yawErr, (double)pitchErr, mirrored);
         this.frameIndex = 0;
         this.skipInitialMicroDeltas();
         if (this.match == null) {
            return new float[]{curYaw, curPitch};
         }
      }

      CursorTrajectoryLibrary.Trajectory trajectory = this.match.trajectory;
      double dx = trajectory.dx[this.frameIndex];
      double dy = trajectory.dy[this.frameIndex];
      if (this.match.mirrored) {
         dx = -dx;
         dy = -dy;
      }

      this.frameIndex++;
      double error = Math.hypot((double)yawErr, (double)pitchErr);
      double replayMultiplier = 1.0 + (replaySpeed - 1.0) * this.smoothstep(class_3532.method_15350(error / 40.0, 0.0, 1.0));
      float yawStep = class_3532.method_15363((float)((double)yawErr * yawLock), -50.0F, 50.0F);
      if (Math.abs(yawStep) > 2.0F) {
         int n = (int)(Math.abs(yawStep) / 24.0F);
         float trim = (float)n * (1.0F + (float)(this.random.nextDouble() * 4.0));
         yawStep += yawStep > 0.0F ? -trim : trim;
         yawStep = class_3532.method_15363(yawStep, -50.0F, 50.0F);
      }

      double vertVel = mc.field_1724 != null ? Math.abs(mc.field_1724.method_18798().field_1351) : 0.0;
      double vScale = 1.0 - class_3532.method_15350((vertVel - 0.08) / 0.25, 0.0, 1.0);
      double dampener = 0.2 + 0.8 * vScale;
      double recordedPitch = Math.abs(dy) * replayMultiplier * 0.15;
      double pitchFrac = Math.max(0.5, class_3532.method_15350(((double)Math.abs(pitchErr) - 1.0) / 3.0, 0.0, 1.0));
      double pitchDemand = (double)Math.abs(this.smoothedTargetPitchSpeed) + 0.45 * (double)Math.abs(pitchErr);
      double pitchMag = Math.max(pitchDemand, recordedPitch * pitchDamp) * pitchFrac * dampener;
      pitchMag = Math.min(pitchMag, (double)Math.abs(pitchErr) + 0.15);
      double pitchSign = (double)pitchErr >= 0.0 ? 1.0 : -1.0;
      double shakeYaw = 0.0;
      double shakePitch = 0.0;
      if (shake > 0.0) {
         double amp = shake * vScale * Math.max(0.35, Math.min(error / 12.0, 1.0));
         this.smoothedYawNoise = 0.85 * this.smoothedYawNoise + 0.15 * this.random.nextGaussian();
         this.smoothedPitchNoise = 0.85 * this.smoothedPitchNoise + 0.15 * this.random.nextGaussian();
         double sy = this.smoothedYawNoise * 3.51;
         double sp = this.smoothedPitchNoise * 3.51;
         this.noisePhase = this.noisePhase + 0.55 + this.random.nextGaussian() * 0.05;
         if (this.noisePhase > Math.PI * 2) {
            this.noisePhase -= Math.PI * 2;
         }

         shakeYaw = (0.5 * Math.sin(this.noisePhase) + 0.5 * sy) * amp * 0.25;
         shakePitch = (0.5 * Math.cos(this.noisePhase * 0.7) + 0.5 * sp) * amp;
      }

      return new float[]{
         curYaw + yawStep + (float)shakeYaw, class_3532.method_15363(curPitch + this.clampStep((float)(pitchMag * pitchSign + shakePitch)), -90.0F, 90.0F)
      };
   }

   private void skipInitialMicroDeltas() {
      if (this.match != null) {
         CursorTrajectoryLibrary.Trajectory trajectory = this.match.trajectory;

         for (int skipped = 0;
            this.frameIndex < trajectory.length() - 1
               && skipped < 6
               && Math.abs(trajectory.dx[this.frameIndex]) + Math.abs(trajectory.dy[this.frameIndex]) < 1.5;
            skipped++
         ) {
            this.frameIndex++;
         }
      }
   }

   private void resetPlayback() {
      this.match = null;
      this.frameIndex = 0;
      this.lastTargetPlayerAge = Integer.MIN_VALUE;
      this.smoothedTargetYawSpeed = 0.0F;
      this.smoothedTargetPitchSpeed = 0.0F;
      this.previousAngleError = 0.0;
   }

   private void updateTargetSpeed(float yaw, float pitch) {
      int age = mc.field_1724 != null ? mc.field_1724.field_6012 : 0;
      if (age != this.lastTargetPlayerAge) {
         if (this.lastTargetPlayerAge != Integer.MIN_VALUE) {
            float dy = class_3532.method_15363(class_3532.method_15393(yaw - this.lastTargetYaw), -30.0F, 30.0F);
            float dp = class_3532.method_15363(pitch - this.lastTargetPitch, -30.0F, 30.0F);
            this.smoothedTargetYawSpeed = 0.5F * dy + 0.5F * this.smoothedTargetYawSpeed;
            this.smoothedTargetPitchSpeed = 0.5F * dp + 0.5F * this.smoothedTargetPitchSpeed;
         } else {
            this.smoothedTargetYawSpeed = 0.0F;
            this.smoothedTargetPitchSpeed = 0.0F;
         }

         this.lastTargetYaw = yaw;
         this.lastTargetPitch = pitch;
         this.lastTargetPlayerAge = age;
      }
   }

   private double smoothstep(double t) {
      return t * t * (3.0 - 2.0 * t);
   }

   private float clampStep(float step) {
      if (step > 50.0F) {
         return 50.0F - (float)(this.random.nextDouble() * 4.0);
      } else {
         return step < -50.0F ? -(50.0F - (float)(this.random.nextDouble() * 4.0)) : step;
      }
   }
}
