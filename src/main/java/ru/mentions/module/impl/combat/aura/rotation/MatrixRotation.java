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
import ru.mentions.utils.aura.UBoxPoints;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class MatrixRotation implements RotationAura {
   private float currentSpeedYaw = 34.0F;
   private float currentSpeedPitch = 12.0F;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         class_243 vec = UBoxPoints.getBestVector3dOnEntityBox(target.method_5829()).method_1020(mc.field_1724.method_33571()).method_1029();
         float rawYaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
         float rawPitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -90.0, 90.0);
         float targetSpeedYaw = MathUtil.randomLerp(50.0F, 80.0F);
         float targetSpeedPitch = MathUtil.randomLerp(7.0F, 12.0F);
         if (RayTraceUtil.rayTraceEntity(mc.field_1724.method_36454(), mc.field_1724.method_36455(), (double)aura.attackRange.getValue().floatValue(), target)) {
            targetSpeedYaw = MathUtil.randomLerp(3.0F, 4.0F);
            targetSpeedPitch = MathUtil.randomLerp(1.0F, 2.0F);
         }

         float smoothFactor = 0.3F;
         this.currentSpeedYaw = this.currentSpeedYaw + (targetSpeedYaw - this.currentSpeedYaw) * smoothFactor;
         this.currentSpeedPitch = this.currentSpeedPitch + (targetSpeedPitch - this.currentSpeedPitch) * smoothFactor;
         RotationProcess.update(
            new Rotation(rawYaw, rawPitch),
            this.currentSpeedYaw,
            this.currentSpeedPitch,
            MathUtil.random(360.0, 390.0),
            MathUtil.random(360.0, 390.0),
            (int)MathUtil.random(3.0, 5.0),
            1,
            false
         );
      }
   }
}
