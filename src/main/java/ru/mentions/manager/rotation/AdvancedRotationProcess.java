package ru.mentions.manager.rotation;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.utils.aura.GCDUtil;

@Environment(EnvType.CLIENT)
public class AdvancedRotationProcess extends Component {
   public static AdvancedRotationProcess.RotationTask currentTask = AdvancedRotationProcess.RotationTask.IDLE;
   public static int currentPriority;
   public static int currentTimeout;
   public static int idleTicks;
   public static Rotation targetRotation;
   public static float currentYawSpeed;
   public static float currentPitchSpeed;
   public static float currentYawReturnSpeed;
   public static float currentPitchReturnSpeed;
   public static float shakeYaw;
   public static float shakePitch;
   public static float resetShakeAmplitudeYaw;
   public static float resetShakeAmplitudePitch;
   public static int shakeDuration;
   private static int resetShakeTicks;
   private static final Random RNG = new Random();

   public static boolean isRotating() {
      return !currentTask.equals(AdvancedRotationProcess.RotationTask.IDLE);
   }

   @EventHandler
   public void onEvent(EventTick event) {
      if (currentTask.equals(AdvancedRotationProcess.RotationTask.AIM) && idleTicks > currentTimeout) {
         currentTask = AdvancedRotationProcess.RotationTask.RESET;
         resetShakeTicks = 0;
      }

      if (currentTask.equals(AdvancedRotationProcess.RotationTask.RESET)) {
         resetShakeTicks++;
         this.doResetRotation();
      }

      idleTicks++;
   }

   private void doResetRotation() {
      Rotation target = new Rotation(FreeLookUtil.freeYaw, FreeLookUtil.freePitch);
      float sYaw = 0.0F;
      float sPitch = 0.0F;
      if (shakeDuration > 0 && resetShakeTicks <= shakeDuration) {
         float fade = 1.0F - (float)resetShakeTicks / (float)shakeDuration;
         sYaw = (RNG.nextFloat() * 2.0F - 1.0F) * resetShakeAmplitudeYaw * fade;
         sPitch = (RNG.nextFloat() * 2.0F - 1.0F) * resetShakeAmplitudePitch * fade;
      }

      if (updateRotation(target, currentYawReturnSpeed, currentPitchReturnSpeed, sYaw, sPitch) && (shakeDuration <= 0 || resetShakeTicks > shakeDuration)) {
         this.stopRotation();
      }
   }

   public static void update(AdvancedRotationProcess.AdvancedRotationConfig cfg) {
      if (currentPriority <= cfg.priority) {
         if (currentTask.equals(AdvancedRotationProcess.RotationTask.IDLE) && !cfg.clientRotation) {
            FreeLookUtil.active = true;
         }

         currentYawSpeed = cfg.yawSpeed;
         currentPitchSpeed = cfg.pitchSpeed;
         currentYawReturnSpeed = cfg.yawReturnSpeed;
         currentPitchReturnSpeed = cfg.pitchReturnSpeed;
         currentTimeout = cfg.timeout;
         currentPriority = cfg.priority;
         shakeYaw = cfg.shakeYaw;
         shakePitch = cfg.shakePitch;
         resetShakeAmplitudeYaw = cfg.resetShakeAmplitudeYaw;
         resetShakeAmplitudePitch = cfg.resetShakeAmplitudePitch;
         shakeDuration = cfg.shakeDuration;
         currentTask = AdvancedRotationProcess.RotationTask.AIM;
         targetRotation = cfg.target;
         updateRotation(cfg.target, cfg.yawSpeed, cfg.pitchSpeed, cfg.shakeYaw, cfg.shakePitch);
      }
   }

   public static void update(Rotation target, float turnSpeed, float returnSpeed, int timeout, int priority) {
      update(new AdvancedRotationProcess.AdvancedRotationConfig(target).speed(turnSpeed, returnSpeed).timeout(timeout).priority(priority));
   }

   public static void update(
      Rotation target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation
   ) {
      update(
         new AdvancedRotationProcess.AdvancedRotationConfig(target)
            .speed(yawSpeed, pitchSpeed, yawReturnSpeed, pitchReturnSpeed)
            .timeout(timeout)
            .priority(priority)
            .clientRotation(clientRotation)
      );
   }

   public static void resetParentTimeout() {
      currentTimeout = 0;
      currentTask = AdvancedRotationProcess.RotationTask.IDLE;
      currentPriority = 0;
      FreeLookUtil.setActive(false);
   }

   static boolean updateRotation(Rotation targetRot, float yawSpeed, float pitchSpeed, float sYaw, float sPitch) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         Rotation current = new Rotation(mc.field_1724);
         float yawDelta = class_3532.method_15393(targetRot.yaw - current.yaw);
         float pitchDelta = targetRot.pitch - current.pitch;
         float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
         float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);
         float moveYaw = GCDUtil.getSensitivity(class_3532.method_15363(yawDelta, -clampedYaw, clampedYaw)) + sYaw;
         float movePitch = GCDUtil.getSensitivity(class_3532.method_15363(pitchDelta, -clampedPitch, clampedPitch)) + sPitch;
         mc.field_1724.method_36456(mc.field_1724.field_6241 += moveYaw);
         mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455() + movePitch, -90.0F, 90.0F));
         idleTicks = 0;
         return new Rotation(mc.field_1724).getDelta(targetRot) < 1.0F;
      }
   }

   public void stopRotation() {
      currentTask = AdvancedRotationProcess.RotationTask.IDLE;
      currentPriority = 0;
      FreeLookUtil.setActive(false);
   }

   @Environment(EnvType.CLIENT)
   public static class AdvancedRotationConfig {
      public Rotation target;
      public float yawSpeed = 180.0F;
      public float pitchSpeed = 180.0F;
      public float yawReturnSpeed = 180.0F;
      public float pitchReturnSpeed = 180.0F;
      public float shakeYaw = 0.0F;
      public float shakePitch = 0.0F;
      public float resetShakeAmplitudeYaw = 0.0F;
      public float resetShakeAmplitudePitch = 0.0F;
      public int shakeDuration = 0;
      public int timeout = 1;
      public int priority = 0;
      public boolean clientRotation = false;

      public AdvancedRotationConfig(Rotation target) {
         this.target = target;
      }

      public AdvancedRotationProcess.AdvancedRotationConfig speed(float turnSpeed, float returnSpeed) {
         this.yawSpeed = turnSpeed;
         this.pitchSpeed = turnSpeed;
         this.yawReturnSpeed = returnSpeed;
         this.pitchReturnSpeed = returnSpeed;
         return this;
      }

      public AdvancedRotationProcess.AdvancedRotationConfig speed(float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed) {
         this.yawSpeed = yawSpeed;
         this.pitchSpeed = pitchSpeed;
         this.yawReturnSpeed = yawReturnSpeed;
         this.pitchReturnSpeed = pitchReturnSpeed;
         return this;
      }

      public AdvancedRotationProcess.AdvancedRotationConfig shake(float yaw, float pitch) {
         this.shakeYaw = yaw;
         this.shakePitch = pitch;
         return this;
      }

      public AdvancedRotationProcess.AdvancedRotationConfig resetShake(float amplitudeYaw, float amplitudePitch, int durationTicks) {
         this.resetShakeAmplitudeYaw = amplitudeYaw;
         this.resetShakeAmplitudePitch = amplitudePitch;
         this.shakeDuration = durationTicks;
         return this;
      }

      public AdvancedRotationProcess.AdvancedRotationConfig timeout(int ticks) {
         this.timeout = ticks;
         return this;
      }

      public AdvancedRotationProcess.AdvancedRotationConfig priority(int p) {
         this.priority = p;
         return this;
      }

      public AdvancedRotationProcess.AdvancedRotationConfig clientRotation(boolean v) {
         this.clientRotation = v;
         return this;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum RotationTask {
      AIM,
      RESET,
      IDLE;
   }
}
