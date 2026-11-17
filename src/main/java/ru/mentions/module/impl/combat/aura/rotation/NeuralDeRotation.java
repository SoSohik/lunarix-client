package ru.mentions.module.impl.combat.aura.rotation;

import java.util.Random;
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
public class NeuralDeRotation implements RotationAura {
   private static final float MAX_YAW_STEP = 50.0F;
   private static final float MAX_PITCH_STEP = 24.0F;
   private static final float MAX_TARGET_ANGLE_STEP = 30.0F;
   private static final float FALLBACK_GAIN = 0.4F;
   private final Random random = new Random();
   private int reactionTicksRemaining;
   private float lastTargetYaw;
   private float lastTargetPitch;
   private int lastTargetPlayerAge = Integer.MIN_VALUE;
   private float smoothedTargetYawSpeed;
   private float smoothedTargetPitchSpeed;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         class_243 eye = mc.field_1724.method_33571();
         class_243 aim = DeRotUtil.aimPoint(target.method_5829(), eye, 0.0, 0.0).method_1020(eye);
         float desiredYaw = DeRotUtil.yawFromDir(aim);
         float desiredPitch = class_3532.method_15363(DeRotUtil.pitchFromDir(aim), -90.0F, 90.0F);
         this.updateTargetSpeed(desiredYaw, desiredPitch);
         float curYaw = mc.field_1724.method_36454();
         float curPitch = mc.field_1724.method_36455();
         float yawErr = class_3532.method_15393(desiredYaw - curYaw);
         float pitchErr = desiredPitch - curPitch;
         double err = Math.hypot((double)yawErr, (double)pitchErr);
         if (aura.deNeuralFreeze != null && aura.deNeuralFreeze.getValue() && aura.justAttacked) {
            this.reactionTicksRemaining = Math.max(this.reactionTicksRemaining, 2);
         }

         if (this.reactionTicksRemaining > 0 && err > 3.0) {
            this.reactionTicksRemaining--;
         } else {
            float stepYaw = class_3532.method_15363(yawErr * 0.4F + this.smoothedTargetYawSpeed * 0.12F, -50.0F, 50.0F);
            float stepPitch = class_3532.method_15363(pitchErr * 0.4F + this.smoothedTargetPitchSpeed * 0.12F, -24.0F, 24.0F);
            stepYaw = DeRotUtil.quantizeStep(stepYaw);
            stepPitch = DeRotUtil.quantizeStep(stepPitch);
            float yaw = curYaw + stepYaw;
            float pitch = class_3532.method_15363(curPitch + stepPitch, -90.0F, 90.0F);
            RotationProcess.update(
               new Rotation(yaw, pitch),
               Math.max(1.0F, Math.abs(stepYaw)),
               Math.max(1.0F, Math.abs(stepPitch)),
               (float)MathUtil.randomInt(45, 75),
               (float)MathUtil.randomInt(32, 55),
               MathUtil.randomInt(2, 4),
               15,
               false
            );
         }
      } else {
         this.reactionTicksRemaining = this.random.nextInt(5);
         this.lastTargetPlayerAge = Integer.MIN_VALUE;
      }
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
}
