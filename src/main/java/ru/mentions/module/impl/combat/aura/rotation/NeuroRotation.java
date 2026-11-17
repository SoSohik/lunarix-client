package ru.mentions.module.impl.combat.aura.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.neuro.NeuroManager;
import ru.mentions.manager.neuro.NeuroModel;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.aura.UAttack;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class NeuroRotation implements RotationAura {
   private boolean initialized;
   private float lastCameraYaw;
   private float lastCameraPitch;
   private float prevAppliedYaw;
   private float prevAppliedPitch;
   private float lastHitX = 0.5F;
   private float lastHitY = 0.5F;
   private float lastHitZ = 0.5F;
   private float lastSmoothness = 1.0F;
   private float lastPeak = 0.0F;
   private int sinceAttack = 999;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      boolean attack = UAttack.shouldAttack(target, false, true, true, -100L, ranges);
      float cameraYaw = Rotation.cameraYaw();
      float cameraPitch = Rotation.cameraPitch();
      if (!this.initialized) {
         this.lastCameraYaw = cameraYaw;
         this.lastCameraPitch = cameraPitch;
         this.initialized = true;
      }

      float appliedYaw = class_3532.method_15393(cameraYaw - this.lastCameraYaw);
      float appliedPitch = cameraPitch - this.lastCameraPitch;
      class_243 center = target.method_5829().method_1005().method_1020(mc.field_1724.method_33571());
      float centerYaw = yawTo(center);
      float centerPitch = pitchTo(center);
      float yawErr = class_3532.method_15393(centerYaw - cameraYaw);
      float pitchErr = centerPitch - cameraPitch;
      float distance = (float)mc.field_1724.method_33571().method_1022(target.method_5829().method_1005());
      if (aura.justAttacked) {
         aura.justAttacked = false;
         this.sinceAttack = 0;
      } else {
         this.sinceAttack = Math.min(this.sinceAttack + 1, 999);
      }

      float[] move = NeuroManager.get().nextMove(yawErr, pitchErr, (float)Math.min(this.sinceAttack, 60));
      if (move != null) {
         float replayYaw = cameraYaw + move[0];
         float replayPitch = class_3532.method_15363(cameraPitch + move[1], -90.0F, 90.0F);
         RotationProcess.update(
            new Rotation(replayYaw, replayPitch),
            Math.max(Math.abs(move[0]), 1.0F),
            Math.max(Math.abs(move[1]), 1.0F),
            (float)MathUtil.randomInt(900, 1424),
            (float)MathUtil.randomInt(900, 1424),
            MathUtil.randomInt(3, 5),
            15,
            false
         );
         this.prevAppliedYaw = appliedYaw;
         this.prevAppliedPitch = appliedPitch;
         this.lastCameraYaw = cameraYaw;
         this.lastCameraPitch = cameraPitch;
      } else {
         double[] features = NeuroModel.features(
            yawErr,
            pitchErr,
            appliedYaw,
            appliedPitch,
            this.prevAppliedYaw,
            this.prevAppliedPitch,
            this.lastHitX,
            this.lastHitY,
            this.lastHitZ,
            distance,
            this.lastSmoothness,
            this.lastPeak
         );
         NeuroModel.Prediction prediction = NeuroManager.get().predictDetailed(features);
         this.lastHitX = prediction.hitX;
         this.lastHitY = prediction.hitY;
         this.lastHitZ = prediction.hitZ;
         this.lastSmoothness = prediction.smoothness;
         this.lastPeak = prediction.peak;
         class_243 aimPoint = this.pointFromHitbox(target.method_5829(), prediction);
         class_243 aim = aimPoint.method_1020(mc.field_1724.method_33571());
         float baseYaw = yawTo(aim);
         float basePitch = pitchTo(aim);
         float aimYawDelta = class_3532.method_15393(baseYaw - cameraYaw);
         float aimPitchDelta = basePitch - cameraPitch;
         float aimError = (float)Math.hypot((double)aimYawDelta, (double)aimPitchDelta);
         float styleScale = class_3532.method_15363(1.0F - aimError / 35.0F, 0.15F, 1.0F);
         float jitterScale = 1.0F + (1.0F - prediction.smoothness) * 0.9F;
         float styleYaw = (prediction.yawShake + prediction.yawLead * 0.16F + prediction.yawAccel * 0.05F) * styleScale * jitterScale;
         float stylePitch = (prediction.pitchShake + prediction.pitchLead * 0.16F + prediction.pitchAccel * 0.05F) * styleScale * jitterScale;
         float[] handJitter = NeuroManager.get().nextJitter((float)Math.hypot((double)appliedYaw, (double)appliedPitch));
         float jitterYaw = class_3532.method_15363(handJitter[0], -8.0F, 8.0F);
         float jitterPitch = class_3532.method_15363(handJitter[1], -5.0F, 5.0F);
         float yaw = baseYaw + styleYaw + jitterYaw;
         float pitch = class_3532.method_15363(basePitch + stylePitch + jitterPitch, -90.0F, 90.0F);
         float yawDelta = class_3532.method_15393(yaw - cameraYaw);
         float pitchDelta = pitch - cameraPitch;
         float peakBoost = prediction.peak * 18.0F;
         float yawSpeed = class_3532.method_15363(Math.max(prediction.yawSpeed, Math.abs(aimYawDelta) * 0.6F) + peakBoost, 0.0F, 80.0F);
         float pitchSpeed = class_3532.method_15363(Math.max(prediction.pitchSpeed, Math.abs(aimPitchDelta) * 0.03F) + peakBoost * 0.4F, 0.0F, 11.0F);
         RotationProcess.update(
            new Rotation(yaw, pitch),
            yawSpeed,
            pitchSpeed,
            (float)MathUtil.randomInt(900, 1424),
            (float)MathUtil.randomInt(900, 1424),
            MathUtil.randomInt(3, 5),
            15,
            false
         );
         this.prevAppliedYaw = appliedYaw;
         this.prevAppliedPitch = appliedPitch;
         this.lastCameraYaw = cameraYaw;
         this.lastCameraPitch = cameraPitch;
      }
   }

   private class_243 pointFromHitbox(class_238 box, NeuroModel.Prediction prediction) {
      return new class_243(
         class_3532.method_16436((double)prediction.hitX, box.field_1323, box.field_1320),
         class_3532.method_16436((double)prediction.hitY, box.field_1322, box.field_1325),
         class_3532.method_16436((double)prediction.hitZ, box.field_1321, box.field_1324)
      );
   }

   private static float yawTo(class_243 aim) {
      return (float)Math.toDegrees(Math.atan2(-aim.field_1352, aim.field_1350));
   }

   private static float pitchTo(class_243 aim) {
      return (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(aim.field_1351, Math.hypot(aim.field_1352, aim.field_1350))), -90.0, 90.0);
   }
}
