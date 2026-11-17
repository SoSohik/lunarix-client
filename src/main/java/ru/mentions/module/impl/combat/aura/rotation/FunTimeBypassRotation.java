package ru.mentions.module.impl.combat.aura.rotation;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.aura.RayTraceUtil;
import ru.mentions.utils.aura.UAttack;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class FunTimeBypassRotation implements RotationAura {
   private static final float POST_HIT_YAW_MIN = 18.0F;
   private static final float POST_HIT_YAW_MAX = 24.0F;
   private static final float POST_HIT_PITCH_MIN = 12.0F;
   private static final float POST_HIT_PITCH_MAX = 16.0F;
   private static final int POST_HIT_HOLD_TICKS = 9;
   private static final float JITTER_YAW = 20.0F;
   private static final float JITTER_PITCH = 10.0F;
   private static final float JITTER_BLEND = 0.6F;
   private static final float SNAP_YAW_SPEED = 60.0F;
   private static final float SNAP_PITCH_SPEED = 45.0F;
   private static final float IDLE_RETURN_SPEED = 14.0F;
   private float heldPostHitYaw;
   private float heldPostHitPitch;
   private float postHitYawOffset;
   private float postHitPitchOffset;
   private boolean hasPostHit;
   private int sinceHit = 9;
   private float smoothedJitterYaw;
   private float smoothedJitterPitch;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && mc.field_1687 != null && target != null) {
         float playerYaw = mc.field_1724.method_36454();
         float playerPitch = mc.field_1724.method_36455();
         if (aura != null && aura.justAttacked) {
            aura.justAttacked = false;
            this.sinceHit = 0;
         } else if (this.sinceHit < 9) {
            this.sinceHit++;
         }

         class_243 eye = mc.field_1724.method_33571();
         class_238 box = target.method_5829();
         class_243 aim = box.method_1005().method_1020(eye);
         float targetYaw = (float)Math.toDegrees(Math.atan2(-aim.field_1352, aim.field_1350));
         float targetPitch = (float)class_3532.method_15350(
            -Math.toDegrees(Math.atan2(aim.field_1351, Math.hypot(aim.field_1352, aim.field_1350))), -90.0, 90.0
         );
         boolean hitWindow = canAttack
            && UAttack.shouldAttack(target, true, true, true, 0L, ranges)
            && RayTraceUtil.rayTraceEntity(targetYaw, targetPitch, ranges.length > 0 ? (double)ranges[0] : 3.0, target);
         if (hitWindow) {
            this.heldPostHitYaw = targetYaw;
            this.heldPostHitPitch = targetPitch;
            this.generatePostHitOffset(targetPitch);
            this.hasPostHit = true;
            AttackAura.lastYaw = targetYaw;
            AttackAura.lastPitch = targetPitch;
            RotationProcess.update(
               new Rotation(targetYaw, targetPitch), 60.0F, 45.0F, (float)MathUtil.randomInt(35, 45), (float)MathUtil.randomInt(19, 45), 0, 15, false
            );
         } else {
            float baseYaw;
            float basePitch;
            if (this.hasPostHit && this.sinceHit < 9) {
               baseYaw = this.heldPostHitYaw + this.postHitYawOffset;
               basePitch = class_3532.method_15363(this.heldPostHitPitch + this.postHitPitchOffset, -90.0F, 90.0F);
            } else {
               this.hasPostHit = false;
               baseYaw = playerYaw;
               basePitch = playerPitch;
            }

            float[] jitter = this.jitter();
            float appliedYaw = baseYaw + jitter[0];
            float appliedPitch = class_3532.method_15363(basePitch + jitter[1], -90.0F, 90.0F);
            RotationProcess.update(
               new Rotation(appliedYaw, appliedPitch), 14.0F, 14.0F, (float)MathUtil.randomInt(30, 50), (float)MathUtil.randomInt(20, 40), 0, 10, false
            );
         }
      }
   }

   public void reset() {
      this.hasPostHit = false;
      this.sinceHit = 9;
      this.smoothedJitterYaw = 0.0F;
      this.smoothedJitterPitch = 0.0F;
   }

   private void generatePostHitOffset(float basePitch) {
      ThreadLocalRandom r = ThreadLocalRandom.current();
      this.postHitYawOffset = (r.nextBoolean() ? 1.0F : -1.0F) * r.nextFloat(18.0F, 24.0F);
      float magnitude = r.nextFloat(12.0F, 16.0F);
      boolean upOk = basePitch + magnitude <= 90.0F;
      boolean downOk = basePitch - magnitude >= -90.0F;
      float sign = upOk && downOk ? (r.nextBoolean() ? 1.0F : -1.0F) : (downOk ? -1.0F : 1.0F);
      this.postHitPitchOffset = sign * magnitude;
   }

   private float[] jitter() {
      ThreadLocalRandom r = ThreadLocalRandom.current();
      float tYaw = r.nextFloat(-20.0F, 20.0F);
      float tPitch = r.nextFloat(-10.0F, 10.0F);
      this.smoothedJitterYaw = this.smoothedJitterYaw + (tYaw - this.smoothedJitterYaw) * 0.6F;
      this.smoothedJitterPitch = this.smoothedJitterPitch + (tPitch - this.smoothedJitterPitch) * 0.6F;
      return new float[]{this.smoothedJitterYaw, this.smoothedJitterPitch};
   }
}
