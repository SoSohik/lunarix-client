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
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class FocusedDeRotation implements RotationAura {
   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         class_243 eye = mc.field_1724.method_33571();
         class_243 aim = DeRotUtil.aimPoint(target.method_5829(), eye, 0.0, 0.0).method_1020(eye);
         float desiredYaw = DeRotUtil.yawFromDir(aim);
         float desiredPitch = class_3532.method_15363(DeRotUtil.pitchFromDir(aim), -90.0F, 90.0F);
         float curYaw = mc.field_1724.method_36454();
         float curPitch = mc.field_1724.method_36455();
         float maxDeg = aura.deFocusedMaxDeg != null ? aura.deFocusedMaxDeg.getValue() : 0.0F;
         float yaw = desiredYaw;
         float pitch = desiredPitch;
         if (maxDeg > 0.0F) {
            float dy = class_3532.method_15393(desiredYaw - curYaw);
            float dp = desiredPitch - curPitch;
            yaw = class_3532.method_15393(curYaw + class_3532.method_15363(dy, -maxDeg, maxDeg));
            pitch = class_3532.method_15363(curPitch + class_3532.method_15363(dp, -maxDeg, maxDeg), -88.0F, 88.0F);
         }

         yaw = curYaw + DeRotUtil.quantizeStep(class_3532.method_15393(yaw - curYaw));
         pitch = class_3532.method_15363(curPitch + DeRotUtil.quantizeStep(pitch - curPitch), -90.0F, 90.0F);
         RotationProcess.update(
            new Rotation(yaw, pitch),
            MathUtil.randomLerp(80.0F, 95.0F),
            MathUtil.randomLerp(80.0F, 95.0F),
            (float)MathUtil.randomInt(60, 120),
            (float)MathUtil.randomInt(60, 120),
            MathUtil.randomInt(1, 3),
            15,
            false
         );
      }
   }
}
