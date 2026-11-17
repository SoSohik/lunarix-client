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
import ru.mentions.utils.aura.RayTraceUtil;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class SpookyTimeNewRotation implements RotationAura {
   private static final int HIT_STOP_TICKS = 3;
   private static final float SPEED_SMOOTHING = 0.35F;
   private static final float IDLE_SPEED_YAW = 24.0F;
   private static final float IDLE_SPEED_PITCH = 6.0F;

   private float currentSpeedYaw = IDLE_SPEED_YAW;
   private float currentSpeedPitch = IDLE_SPEED_PITCH;
   private int hitStopTicks;
   private long lastHitAt;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 == null || target == null) {
         return;
      }

      float currentYaw = mc.field_1724.method_36454();
      float currentPitch = mc.field_1724.method_36455();

      if (aura.attackFlickAt > this.lastHitAt) {
         this.lastHitAt = aura.attackFlickAt;
         this.hitStopTicks = HIT_STOP_TICKS;
      }

      if (this.hitStopTicks > 0) {
         this.hitStopTicks--;
         RotationProcess.update(
            new Rotation(currentYaw, currentPitch),
            180.0F,
            180.0F,
            (float)MathUtil.random(360.0, 390.0),
            (float)MathUtil.random(360.0, 390.0),
            (int)MathUtil.random(3.0, 5.0),
            1,
            false
         );
         return;
      }

      double focusHeight = class_3532.method_15350(
         target.method_23320() - target.method_23318() - 0.3, 0.0, (double)target.method_17682()
      );
      class_243 vec = target.method_73189().method_1031(0.0, focusHeight, 0.0).method_1020(mc.field_1724.method_33571());

      float rawYaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
      float rawPitch = (float)class_3532.method_15350(
         -Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -90.0, 90.0
      );

      float targetSpeedYaw = MathUtil.randomLerp(45.0F, 60.0F);
      float targetSpeedPitch = MathUtil.randomLerp(8.0F, 14.0F);

      if (RayTraceUtil.rayTraceEntity(currentYaw, currentPitch, 5.0, target)) {
         targetSpeedYaw = 0.0F;
         targetSpeedPitch = 0.0F;
      }

      this.currentSpeedYaw = this.currentSpeedYaw + (targetSpeedYaw - this.currentSpeedYaw) * SPEED_SMOOTHING;
      this.currentSpeedPitch = this.currentSpeedPitch + (targetSpeedPitch - this.currentSpeedPitch) * SPEED_SMOOTHING;

      float yawDelta = class_3532.method_15393(rawYaw - currentYaw);
      float pitchDelta = rawPitch - currentPitch;
      float yawStep = Math.min(Math.abs(yawDelta), this.currentSpeedYaw);
      float pitchStep = Math.min(Math.abs(pitchDelta), this.currentSpeedPitch);

      float nextYaw = currentYaw + class_3532.method_15363(yawDelta, -yawStep, yawStep);
      float nextPitch = class_3532.method_15363(
         currentPitch + class_3532.method_15363(pitchDelta, -pitchStep, pitchStep), -90.0F, 90.0F
      );

      RotationProcess.update(
         new Rotation(nextYaw, nextPitch),
         180.0F,
         180.0F,
         (float)MathUtil.random(360.0, 390.0),
         (float)MathUtil.random(360.0, 390.0),
         (int)MathUtil.random(3.0, 5.0),
         1,
         false
      );
   }
}
