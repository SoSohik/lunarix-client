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
public class PolarDeRotation implements RotationAura {
   private static final float BASE_YAW_STEP = 24.5F;
   private static final float BASE_PITCH_STEP = 35.0F;
   private static final float PITCH_LIMIT = 88.0F;
   private float speedYaw;
   private float speedPitch;
   private float yawGain = 1.0F;
   private float pitchGain = 1.0F;
   private float phase;
   private boolean primed;

   private void randomize() {
      this.yawGain = ThreadLocalRandom.current().nextBoolean() ? 1.12F : 2.24F;
      this.pitchGain = ThreadLocalRandom.current().nextBoolean() ? 1.0F : 1.4F;
      this.phase = ThreadLocalRandom.current().nextFloat() * (float) (Math.PI * 2);
      this.speedYaw = 0.0F;
      this.speedPitch = 0.0F;
   }

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 == null || target == null) {
         this.primed = false;
      } else if (!mc.field_1724.method_6128()) {
         if (!this.primed) {
            this.randomize();
            this.primed = true;
         }

         class_243 eye = mc.field_1724.method_33571();
         class_243 aim = DeRotUtil.aimPoint(target.method_5829(), eye, 0.0, 0.0).method_1020(eye);
         float desiredYaw = DeRotUtil.yawFromDir(aim);
         float desiredPitch = class_3532.method_15363(DeRotUtil.pitchFromDir(aim), -88.0F, 88.0F);
         float yawErr = class_3532.method_15393(desiredYaw - mc.field_1724.method_36454());
         float pitchErr = desiredPitch - mc.field_1724.method_36455();
         float err = (float)Math.hypot((double)yawErr, (double)pitchErr);
         float approach = class_3532.method_15363(err / 40.0F, 0.0F, 1.0F);
         approach = approach * approach * (3.0F - 2.0F * approach);
         float wantYaw = Math.min(Math.abs(yawErr), 24.5F * this.yawGain * (0.4F + 0.6F * approach));
         float wantPitch = Math.min(Math.abs(pitchErr), 35.0F * this.pitchGain * (0.35F + 0.65F * approach));
         this.speedYaw = this.speedYaw + (wantYaw - this.speedYaw) * 0.45F;
         this.speedPitch = this.speedPitch + (wantPitch - this.speedPitch) * 0.45F;
         this.phase += 0.55F;
         float wobble = (float)Math.sin((double)this.phase) * Math.min(1.5F, err * 0.05F);
         float stepYaw = DeRotUtil.quantizeStep(Math.copySign(Math.min(this.speedYaw, Math.abs(yawErr)), yawErr) + wobble);
         float stepPitch = DeRotUtil.quantizeStep(Math.copySign(Math.min(this.speedPitch, Math.abs(pitchErr)), pitchErr));
         float yaw = mc.field_1724.method_36454() + stepYaw;
         float pitch = class_3532.method_15363(mc.field_1724.method_36455() + stepPitch, -88.0F, 88.0F);
         RotationProcess.update(
            new Rotation(yaw, pitch),
            Math.max(1.0F, Math.abs(stepYaw)),
            Math.max(1.0F, Math.abs(stepPitch)),
            (float)MathUtil.randomInt(50, 80),
            (float)MathUtil.randomInt(35, 60),
            MathUtil.randomInt(1, 4),
            15,
            false
         );
      }
   }
}
