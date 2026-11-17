package ru.mentions.module.impl.combat.aura.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.rotation.FreeLookUtil;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.aura.AuraUtil;
import ru.mentions.utils.aura.UBoxPoints;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class SpaceTimeRotation implements RotationAura {
   private float tick;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         class_243 bestVec = UBoxPoints.getBestVector3dOnEntityBox(target.method_5829());
         if (bestVec != null) {
            class_243 directionVec = bestVec.method_1020(mc.field_1724.method_33571());
            float baseYaw = FreeLookUtil.freeYaw;
            if (canAttack && AuraUtil.getStrictDistance(target) < (double)ranges[0]) {
               this.tick = MathUtil.randomLerp(6.0F, 7.0F);
            }

            float yawChangeSpeed = MathUtil.randomLerp(22.0F, 29.0F);
            float randomAttackShift = 0.0F;
            float waveA = (float)Math.cos((double)System.currentTimeMillis() / 40.0);
            float waveB = (float)Math.sin((double)System.currentTimeMillis() / 70.0);
            if (this.tick > 0.0F) {
               yawChangeSpeed = MathUtil.randomLerp(90.0F, 120.0F);
               baseYaw = (float)Math.toDegrees(Math.atan2(-directionVec.field_1352, directionVec.field_1350));
               randomAttackShift = (waveA + waveB) * MathUtil.randomLerp(1.0F, 2.0F);
               this.tick--;
            }

            float basePitch = (float)class_3532.method_15350(
               -Math.toDegrees(Math.atan2(directionVec.field_1351, Math.hypot(directionVec.field_1352, directionVec.field_1350))), -90.0, 90.0
            );
            float yawJitter = waveA * MathUtil.randomLerp(11.0F, 14.0F) + randomAttackShift;
            float pitchJitter = waveB * MathUtil.randomLerp(4.0F, 7.0F) + randomAttackShift;
            RotationProcess.update(
               new Rotation(baseYaw + yawJitter, basePitch + pitchJitter), yawChangeSpeed, MathUtil.randomLerp(6.0F, 8.0F), 25.0F, 25.0F, 0, 15, false
            );
         }
      }
   }
}
