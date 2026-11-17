package ru.mentions.utils.aura;

public final class AimSmoothing {
   private AimSmoothing() {
   }

   public static float step(float error, float gainAt60Fps, float seconds, float degreesPerSecond, float deadZone) {
      if (Float.isFinite(error)
         && Float.isFinite(gainAt60Fps)
         && Float.isFinite(seconds)
         && Float.isFinite(degreesPerSecond)
         && Float.isFinite(deadZone)
         && !(seconds <= 0.0F)
         && !(gainAt60Fps <= 0.0F)
         && !(degreesPerSecond <= 0.0F)
         && !(Math.abs(error) <= Math.max(0.0F, deadZone))) {
         float dt = Math.min(seconds, 0.05F);
         double gain = Math.min(0.99, (double)gainAt60Fps);
         float correction = (float)((double)error * -Math.expm1(Math.log1p(-gain) * (double)dt * 60.0));
         float limit = Math.min(Math.abs(error), degreesPerSecond * dt);
         return Math.max(-limit, Math.min(limit, correction));
      } else {
         return 0.0F;
      }
   }

   public static float mouseStep(float correction, double yawInput, double pitchInput, float strength) {
      float limit = (float)(Math.hypot(yawInput, pitchInput) * 0.15 * (double)Math.max(0.0F, Math.min(1.0F, strength)));
      return Math.max(-limit, Math.min(limit, correction));
   }
}
