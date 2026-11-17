package ru.mentions.module.impl.combat.aura.rotation;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;

@Environment(EnvType.CLIENT)
public class ReallyWorldRotation implements RotationAura {
   private static final float RANDOM_SPREAD = 3.0F;
   private static final double OSCILLATION_PERIOD = 60.0;
   private static final float SPREAD_MIN_1 = 155.0F;
   private static final float SPREAD_MAX_1 = 180.0F;
   private static final float SPREAD_MIN_2 = 155.0F;
   private static final float SPREAD_MAX_2 = 180.0F;
   private static final double YAW_OFFSET = 90.0;
   private static final double CLAMP_MIN_Y = 0.15;
   private static final double CLOSE_RANGE_THRESHOLD_SQ = 1.0E-6;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      aimAtEntity(target);
   }

   public static void aimAtEntity(class_1309 target) {
      if (target != null) {
         float yaw = getYawToEntity(target);
         float pitch = getPitchToEntity(target);
         float spread = randomRange(0.0F, 3.0F);
         float spreadY = randomRange(0.0F, 2.0F);
         float jitterYaw = randomRange(-spread, spread);
         float jitterPitch = randomRange(-spreadY, spreadY);
         boolean throughWalls = !mc.field_1724.method_6057(target) && !mc.field_1724.method_6128();
         float wallJitter = (float)Math.ceil((double)ThreadLocalRandom.current().nextInt(-17, 6) * Math.cos((double)System.currentTimeMillis() / 60.0));
         if (throughWalls) {
            RotationProcess.update(
               new Rotation(yaw + wallJitter, pitch),
               randomRange(155.0F, 180.0F),
               randomRange(155.0F, 180.0F),
               randomRange(155.0F, 180.0F),
               randomRange(155.0F, 180.0F),
               1,
               6,
               false
            );
         } else {
            RotationProcess.update(
               new Rotation(yaw + jitterYaw, pitch + jitterPitch),
               randomRange(155.0F, 180.0F),
               randomRange(155.0F, 180.0F),
               randomRange(155.0F, 180.0F),
               randomRange(155.0F, 180.0F),
               1,
               6,
               false
            );
         }
      }
   }

   public static float getPitchToEntity(class_1309 target) {
      class_243 dir = getDirectionToEntity(target);
      return (float)(-Math.toDegrees(Math.atan2(dir.field_1351, Math.hypot(dir.field_1352, dir.field_1350))));
   }

   private static class_243 getDirectionToEntity(class_1309 target) {
      class_243 eye = mc.field_1724.method_33571();
      double eyeHeight = class_3532.method_15350(mc.field_1724.method_23320() - target.method_23318(), 0.15, (double)target.method_17682());
      class_243 aimPoint = new class_243(target.method_23317(), target.method_23318(), target.method_23321()).method_1031(0.0, eyeHeight, 0.0);
      class_243 dir = aimPoint.method_1020(eye);
      if (dir.method_1027() < 1.0E-6) {
         class_243 clamped = clampToBoundingBox(eye, target);
         dir = clamped.method_1020(eye);
      }

      return dir;
   }

   public static class_243 clampToAABB(class_243 pos, class_238 box) {
      double x = class_3532.method_15350(pos.field_1352, box.field_1323, box.field_1320);
      double y = class_3532.method_15350(pos.field_1351, box.field_1322, box.field_1325);
      double z = class_3532.method_15350(pos.field_1350, box.field_1321, box.field_1324);
      return new class_243(x, y, z);
   }

   public static class_243 getDirectionToAABB(class_1297 entity) {
      class_243 eye = mc.field_1724.method_33571();
      class_243 clamped = clampToBoundingBox(eye, entity);
      return clamped.method_1020(eye);
   }

   public static class_243 clampToBoundingBox(class_243 pos, class_1297 entity) {
      return clampToAABB(pos, entity.method_5829());
   }

   public static float getYawToEntity(class_1309 target) {
      class_243 dir = getDirectionToEntity(target);
      return class_3532.method_15393((float)(Math.toDegrees(Math.atan2(dir.field_1350, dir.field_1352)) - 90.0));
   }

   private static float randomRange(float min, float max) {
      return (float)(Math.random() * (double)(max - min) + (double)min);
   }
}
