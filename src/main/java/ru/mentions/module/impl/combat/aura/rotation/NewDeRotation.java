package ru.mentions.module.impl.combat.aura.rotation;

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
public class NewDeRotation implements RotationAura {
   private static final float MAX_YAW_STEP = 50.0F;
   private static final float MAX_PITCH_STEP = 24.0F;
   private static final float TARGET_HEIGHT_FRACTION = 0.6F;
   private int pulseDirection = 0;
   private int pulseTicks = -1;
   private float pulseYawAmplitude = 0.0F;
   private float pulsePitchAmplitude = 0.0F;
   private float previousYawOffset = 0.0F;
   private float previousPitchOffset = 0.0F;
   private int attackCount = 0;
   private int lastOffsetPlayerTick = Integer.MIN_VALUE;
   private int lastAttackMarker = 0;
   private String lastMode = "";

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         String mode = aura.deNewDirection != null ? aura.deNewDirection.getValue() : "Компенсация";
         if (!mode.equals(this.lastMode)) {
            this.lastMode = mode;
            this.resetOffsets();
         }

         if (aura.justAttacked) {
            this.onAttack();
         }

         class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
         class_243 velocity = target.method_18798();
         class_243 delta = DeRotUtil.frameDelta(target);
         double px;
         double pz;
         switch (mode) {
            case "Назад":
               double dx = pos.field_1352 - mc.field_1724.method_23317();
               double dz = pos.field_1350 - mc.field_1724.method_23321();
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 1.0E-4) {
                  dx /= len;
                  dz /= len;
                  px = pos.field_1352 + dx * 0.25;
                  pz = pos.field_1350 + dz * 0.25;
               } else {
                  px = pos.field_1352;
                  pz = pos.field_1350;
               }
               break;
            case "Втягивание":
               px = pos.field_1352 + velocity.field_1352 * 0.19;
               pz = pos.field_1350 + velocity.field_1350 * 0.19;
               break;
            default:
               px = pos.field_1352 + delta.field_1352 * 1.2;
               pz = pos.field_1350 + delta.field_1350 * 1.2;
         }

         double aimY = pos.field_1351 + (double)(target.method_17682() * 0.6F);
         class_243 eye = mc.field_1724.method_33571();
         double dx = px - eye.field_1352;
         double dz = pz - eye.field_1350;
         double dy = aimY - eye.field_1351;
         double horiz = Math.sqrt(dx * dx + dz * dz);
         float desiredYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
         float desiredPitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz)));
         float curYaw = mc.field_1724.method_36454();
         float curPitch = mc.field_1724.method_36455();
         float yawErr = class_3532.method_15393(desiredYaw - curYaw);
         float pitchErr = desiredPitch - curPitch;
         float stepYaw = class_3532.method_15363(yawErr, -50.0F, 50.0F);
         float stepPitch = class_3532.method_15363(pitchErr, -24.0F, 24.0F);
         if (Math.abs(stepYaw) > 2.0F) {
            int jitter = (int)(Math.abs(stepYaw) / 24.0F);
            float trim = (float)jitter * (1.0F + (float)(ThreadLocalRandom.current().nextDouble() * 4.0));
            stepYaw += yawErr > 0.0F ? -trim : trim;
            stepYaw = class_3532.method_15363(stepYaw, -50.0F, 50.0F);
         }

         float[] offset = this.nextOffset();
         float yaw = curYaw + DeRotUtil.quantizeStep(stepYaw + offset[0]);
         float pitch = class_3532.method_15363(curPitch + DeRotUtil.quantizeStep(stepPitch + offset[1]), -90.0F, 90.0F);
         RotationProcess.update(
            new Rotation(yaw, pitch),
            Math.max(1.0F, Math.abs(stepYaw)),
            Math.max(1.0F, Math.abs(stepPitch)),
            (float)MathUtil.randomInt(45, 75),
            (float)MathUtil.randomInt(35, 55),
            MathUtil.randomInt(2, 5),
            15,
            false
         );
      }
   }

   private void onAttack() {
      this.attackCount++;
      if (this.attackCount % 6 == 0) {
         this.pulseDirection = this.pulseDirection == 1 ? -1 : 1;
         this.pulseYawAmplitude = 140.0F;
         this.pulsePitchAmplitude = 20.0F + (float)(Math.random() * 5.0);
         this.pulseTicks = 0;
      }
   }

   private void resetOffsets() {
      this.pulseTicks = -1;
      this.previousYawOffset = 0.0F;
      this.previousPitchOffset = 0.0F;
      this.lastOffsetPlayerTick = Integer.MIN_VALUE;
   }

   private float[] nextOffset() {
      int playerAge = mc.field_1724 != null ? mc.field_1724.field_6012 : 0;
      if (playerAge == this.lastOffsetPlayerTick) {
         return new float[]{0.0F, 0.0F};
      } else {
         this.lastOffsetPlayerTick = playerAge;
         float pulseYaw = 0.0F;
         float pulsePitch = 0.0F;
         if (this.pulseTicks >= 0 && this.pulseDirection != 0) {
            int window = 8;
            this.pulseTicks++;
            if (this.pulseTicks <= window) {
               float t = (float)this.pulseTicks / (float)window;
               float smooth = t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
               pulseYaw = this.pulseYawAmplitude * (float)this.pulseDirection * smooth;
               float half = (float)window / 2.0F;
               float tp = (float)this.pulseTicks <= half ? (float)this.pulseTicks / half : (float)(window - this.pulseTicks) / ((float)window - half);
               float smoothP = tp * tp * tp * (tp * (tp * 6.0F - 15.0F) + 10.0F);
               pulsePitch = this.pulsePitchAmplitude * (float)this.pulseDirection * smoothP;
            } else {
               this.pulseTicks = -1;
               this.previousYawOffset = this.previousYawOffset - this.pulseYawAmplitude * (float)this.pulseDirection;
            }
         }

         double seconds = (double)System.currentTimeMillis() / 1000.0;
         float wanderYaw = (float)(Math.sin(seconds * 1.1) * 0.6 + Math.sin(seconds * 1.7 + 0.1) * 0.1 + Math.sin(seconds * 1.1 + 1.3) * 0.1);
         float wanderPitch = (float)(Math.sin(seconds * 0.5 + 0.5) * 0.6 + Math.sin(seconds * 0.33 + 0.4) * 0.4 + Math.sin(seconds * 1.7 + 3.1) * 0.2);
         float totalYaw = pulseYaw + wanderYaw;
         float totalPitch = pulsePitch + wanderPitch;
         float dYaw = totalYaw - this.previousYawOffset;
         float dPitch = totalPitch - this.previousPitchOffset;
         this.previousYawOffset = totalYaw;
         this.previousPitchOffset = totalPitch;
         return new float[]{dYaw, dPitch};
      }
   }
}
