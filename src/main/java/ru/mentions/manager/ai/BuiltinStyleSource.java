package ru.mentions.manager.ai;

public class BuiltinStyleSource implements StyleSource {
   private static final float SETTLE_SPAN = 28.0F;
   private static final float STEP_SPAN_YAW = 70.0F;
   private static final float STEP_SPAN_PITCH = 44.0F;
   private static final float STEP_CURVE = 0.62F;
   private static final float PEAK_START = 45.0F;
   private static final float PEAK_SPAN = 70.0F;
   private static final double PHASE_STEP = 0.29;
   private static final int F_YAW = 2;
   private static final int F_PITCH = 3;
   private final String name;
   private final float stepGainYaw;
   private final float stepGainPitch;
   private final float strokeScale;
   private final float hitWander;
   private final float speedRequest;
   private final float roughness;
   private final double seedA;
   private final double seedB;
   private double phase;

   public BuiltinStyleSource(
      String name, float stepGainYaw, float stepGainPitch, float strokeScale, float hitWander, float speedRequest, float roughness, long seed
   ) {
      this.name = name;
      this.stepGainYaw = stepGainYaw;
      this.stepGainPitch = stepGainPitch;
      this.strokeScale = strokeScale;
      this.hitWander = hitWander;
      this.speedRequest = speedRequest;
      this.roughness = roughness;
      this.seedA = (double)(seed & 65535L) * 7.0E-4;
      this.seedB = (double)(seed >>> 16 & 65535L) * 7.0E-4;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public boolean hasStyle() {
      return true;
   }

   @Override
   public float[] nextGesture(float yawErr, float pitchErr, float ticksSinceAttack) {
      return null;
   }

   @Override
   public double[] predict(double[] features) {
      if (features != null && features.length >= 16) {
         this.phase += 0.29;
         float yawErr = (float)((double)StyleSpec.signed(features[2]) * 180.0);
         float pitchErr = (float)((double)StyleSpec.signed(features[3]) * 90.0);
         float error = (float)Math.hypot((double)yawErr, (double)pitchErr);
         float settle = StyleSpec.clamp01(1.0F - error / 28.0F);
         float open = 1.0F - settle;
         float tYaw = StyleSpec.clamp01(Math.abs(yawErr) / 70.0F);
         float tPitch = StyleSpec.clamp01(Math.abs(pitchErr) / 44.0F);
         float stepYaw = StyleSpec.clamp01((float)Math.pow((double)tYaw, 0.62F) * this.stepGainYaw) * 35.0F;
         float stepPitch = StyleSpec.clamp01((float)Math.pow((double)tPitch, 0.62F) * this.stepGainPitch) * 22.0F;
         float spread = this.hitWander * open;
         float hitX = 0.5F + spread * this.wander(0.47, 0.23);
         float hitY = 0.5F + spread * this.wander(0.31, 0.19);
         float hitZ = 0.5F + spread * this.wander(0.41, 0.17);
         float smoothness = StyleSpec.clamp01(1.0F - this.roughness * open);
         float peak = StyleSpec.clamp01((error - 45.0F) / 70.0F);
         return StyleSpec.targets(stepYaw, stepPitch, hitX, hitY, hitZ, smoothness, peak, this.speedRequest * 180.0F, this.speedRequest * 120.0F);
      } else {
         return null;
      }
   }

   @Override
   public float[] nextStroke(float speed) {
      return new float[]{
         this.strokeScale * (0.62F * this.wave(1.0, 0.0) + 0.38F * this.wave(2.37, 1.1)),
         this.strokeScale * (0.58F * this.wave(1.31, 2.2) + 0.42F * this.wave(2.71, 0.4))
      };
   }

   private float wave(double rate, double offset) {
      return (float)Math.sin(this.phase * rate + offset);
   }

   private float wander(double rateA, double rateB) {
      return 0.62F * this.wave(rateA, this.seedA) + 0.38F * this.wave(rateB, this.seedB);
   }
}
