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
import ru.mentions.utils.aura.UAttack;
import ru.mentions.utils.aura.UBoxPoints;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class SnapRotation implements RotationAura {
   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (aura.typeSnap.is("Fov")) {
         this.fov(aura, target, canAttack);
      } else {
         this.snap360(aura, target, ranges);
      }
   }

   private void snap360(AttackAura aura, class_1309 target, float[] ranges) {
      boolean canAttack = UAttack.shouldAttack(target, false, true, true, -100L, ranges);
      class_243 vec3d = AuraUtil.getVector3(target);
      float yaw = (float)Math.toDegrees(Math.atan2(-vec3d.field_1352, vec3d.field_1350));
      float pitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec3d.field_1351, Math.hypot(vec3d.field_1352, vec3d.field_1350))), -90.0, 90.0);
      float speedYAW = MathUtil.randomLerp(80.0F, 90.0F);
      float speedPit = MathUtil.randomLerp(80.0F, 90.0F);
      float waveA = (float)Math.cos((double)System.currentTimeMillis() / 30.0);
      float waveB = (float)Math.sin((double)System.currentTimeMillis() / 30.0);
      float yawJitter = waveA * MathUtil.randomLerp(8.0F, 16.0F);
      float pitchJitter = waveB * MathUtil.randomLerp(8.0F, 16.0F);
      boolean attack = false;
      if (canAttack) {
         aura.tick = 1.0F;
      }

      if (aura.tick > 0.0F) {
         attack = true;
         aura.tick--;
      }

      if (!attack) {
         yawJitter = 0.0F;
         pitchJitter = 0.0F;
         yaw = FreeLookUtil.freeYaw;
         pitch = FreeLookUtil.freePitch;
      }

      Rotation newRotation = new Rotation(yaw + yawJitter, pitch + pitchJitter);
      RotationProcess.update(
         newRotation, speedYAW, speedPit, (float)MathUtil.randomInt(90, 180), (float)MathUtil.randomInt(90, 180), MathUtil.randomInt(0, 3), 15, false
      );
   }

   private void fov(AttackAura aura, class_1309 target, boolean canAttack) {
      class_243 vec = UBoxPoints.getBestVector3dOnEntityBox(target.method_5829(), false)
         .method_1031(
            0.3F * Math.sin((double)System.currentTimeMillis() / 50.0),
            0.1F * Math.sin((double)System.currentTimeMillis() / 50.0) + 0.3F * Math.cos((double)System.currentTimeMillis() / 50.0),
            0.3F * Math.cos((double)System.currentTimeMillis() / 50.0)
         )
         .method_1020(mc.field_1724.method_33571());
      float yaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
      float pitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -90.0, 90.0);
      float waveA = (float)Math.cos((double)System.currentTimeMillis() / 40.0) * 4.0F;
      float waveB = (float)Math.sin((double)System.currentTimeMillis() / 70.0) * 4.0F;
      if (!canAttack || aura.getTargetFov(target) >= (double)aura.fov.getValue().floatValue()) {
         waveA = 0.0F;
         waveB = 0.0F;
         yaw = FreeLookUtil.freeYaw;
         pitch = FreeLookUtil.freePitch;
      }

      RotationProcess.update(new Rotation(yaw + waveA, pitch + waveB), 30.0F, 30.0F, 30.0F, 30.0F, 1, 15, false);
   }
}
