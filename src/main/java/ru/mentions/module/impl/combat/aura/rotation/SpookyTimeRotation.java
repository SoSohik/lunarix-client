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
import ru.mentions.utils.aura.AuraUtil;
import ru.mentions.utils.aura.RayTraceUtil;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class SpookyTimeRotation implements RotationAura {
   private float currentSpeedYaw = 24.0F;
   private float currentSpeedPitch = 6.0F;
   private float currentSpeedYawf = 0.0F;
   private float currentSpeedPitchf = 0.0F;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         class_243 vec = AuraUtil.getVector2(target);
         float yawJitter = (float)(Math.cos((double)System.currentTimeMillis() / 200.0) * (double)MathUtil.randomLerp(6.0F, 8.0F));
         float pitchJitter = (float)(Math.sin((double)System.currentTimeMillis() / 200.0) * (double)MathUtil.randomLerp(6.0F, 8.0F));
         float targetSpeedYaw = MathUtil.randomLerp(35.0F, 40.0F);
         float targetSpeedPitch = MathUtil.randomLerp(4.0F, 8.0F);
         float rawYaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
         float rawPitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -90.0, 90.0);
         float smoothFactor = 0.3F;
         if (RayTraceUtil.rayTraceEntity(mc.field_1724.method_36454(), mc.field_1724.method_36455(), 5.0, target)) {
            targetSpeedYaw = 0.0F;
            targetSpeedPitch = 0.0F;
         }

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
