package ru.mentions.manager.ai;

public final class StyleSpec {
   public static final int FEATURES = 16;
   public static final int TARGETS = 9;
   public static final float MAX_YAW_SPEED = 180.0F;
   public static final float MAX_PITCH_SPEED = 120.0F;
   public static final float YAW_STEP_NORM = 35.0F;
   public static final float PITCH_STEP_NORM = 22.0F;
   public static final float DISTANCE_NORM = 8.0F;
   public static final float TARGET_SPEED_NORM = 0.3F;
   public static final float TICKS_SINCE_ATTACK_NORM = 60.0F;

   private StyleSpec() {
   }

   public static double[] features(
      float yawErr,
      float pitchErr,
      float dYaw,
      float dPitch,
      float prevDYaw,
      float prevDPitch,
      float hitX,
      float hitY,
      float hitZ,
      float distance,
      float targetSpeed,
      float ticksSinceAttack,
      float prevSmoothness,
      float prevPeak
   ) {
      return new double[]{
         fin(Math.min((double)Math.abs(yawErr) / 180.0, 1.0)),
         fin(Math.min((double)Math.abs(pitchErr) / 90.0, 1.0)),
         unit((double)yawErr / 180.0),
         unit((double)pitchErr / 90.0),
         unit((double)(dYaw / 35.0F)),
         unit((double)(dPitch / 22.0F)),
         unit((double)((dYaw - prevDYaw) / 35.0F)),
         unit((double)((dPitch - prevDPitch) / 22.0F)),
         (double)clamp01(hitX),
         (double)clamp01(hitY),
         (double)clamp01(hitZ),
         fin(Math.min((double)(Math.max(distance, 0.0F) / 8.0F), 1.0)),
         fin(Math.min((double)(Math.max(targetSpeed, 0.0F) / 0.3F), 1.0)),
         fin(Math.min((double)(Math.max(ticksSinceAttack, 0.0F) / 60.0F), 1.0)),
         (double)clamp01(prevSmoothness),
         (double)clamp01(prevPeak)
      };
   }

   public static double[] targets(
      float dYaw, float dPitch, float hitX, float hitY, float hitZ, float smoothness, float peak, float emaYawSpeed, float emaPitchSpeed
   ) {
      return new double[]{
         unit((double)(dYaw / 35.0F)),
         unit((double)(dPitch / 22.0F)),
         (double)clamp01(hitX),
         (double)clamp01(hitY),
         (double)clamp01(hitZ),
         (double)clamp01(smoothness),
         (double)clamp01(peak),
         fin(Math.min((double)(Math.max(emaYawSpeed, 0.0F) / 180.0F), 1.0)),
         fin(Math.min((double)(Math.max(emaPitchSpeed, 0.0F) / 120.0F), 1.0))
      };
   }

   public static double unit(double x) {
      return !Double.isNaN(x) && !Double.isInfinite(x) ? (Math.min(Math.max(x, -1.0), 1.0) + 1.0) * 0.5 : 0.5;
   }

   public static float unitToSigned(float x) {
      return clamp01(x) * 2.0F - 1.0F;
   }

   public static float signed(double x) {
      return (float)clamp(x * 2.0 - 1.0, -1.0, 1.0);
   }

   public static float clamp01(float value) {
      return clamp(value, 0.0F, 1.0F);
   }

   public static float clamp(float value, float min, float max) {
      if (Float.isNaN(value)) {
         return min;
      } else {
         return value < min ? min : Math.min(value, max);
      }
   }

   public static double clamp(double value, double min, double max) {
      if (Double.isNaN(value)) {
         return min;
      } else {
         return value < min ? min : Math.min(value, max);
      }
   }

   public static float fin(float value) {
      return Float.isFinite(value) ? value : 0.0F;
   }

   public static double fin(double value) {
      return Double.isFinite(value) ? value : 0.0;
   }
}
