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
public class HvHRotation implements RotationAura {
   private float currentSpeedPitch = 12.0F;
   private float currentSpeedPitch2 = 0.0F;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      class_243 vec = AuraUtil.getVector2(target);
      float rawYaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
      float rawPitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -90.0, 90.0);
      Rotation rotation = new Rotation(rawYaw, rawPitch);
      float speed_1 = MathUtil.random1(480.0F, 625.0F);
      float speed_2 = MathUtil.random1(455.0F, 555.0F);
      float smoothFactor = 0.5F;
      if (RayTraceUtil.rayTraceEntity(mc.field_1724.method_36454(), mc.field_1724.method_36455(), (double)aura.attackRange.getValue().floatValue(), target)) {
         speed_2 = 0.0F;
         speed_1 = MathUtil.randomLerp(450.0F, 700.0F);
      }

      RotationProcess.update(rotation, speed_1, speed_2, 425.0F, 425.0F, 0, 1, false);
   }
}
