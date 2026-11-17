package ru.mentions.utils.player;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import ru.mentions.manager.event_impl.EventMoveInput;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class MoveUtil implements IMinecraft {
   public static boolean isMoving() {
      return mc.field_1724 == null
         ? false
         : mc.field_1724.field_3913.method_3128().field_1342 != 0.0F || mc.field_1724.field_3913.method_3128().field_1343 != 0.0F;
   }

   public static float getDirection() {
      float rotationYaw = mc.field_1724.method_36454();
      float strafeFactor = 0.0F;
      if (mc.field_1724.field_3913.field_54155.comp_3159()) {
         strafeFactor = 1.0F;
      }

      if (mc.field_1724.field_3913.field_54155.comp_3160()) {
         strafeFactor = -1.0F;
      }

      if (strafeFactor == 0.0F) {
         if (mc.field_1724.field_3913.field_54155.comp_3161()) {
            rotationYaw -= 90.0F;
         }

         if (mc.field_1724.field_3913.field_54155.comp_3162()) {
            rotationYaw += 90.0F;
         }
      } else {
         if (mc.field_1724.field_3913.field_54155.comp_3161()) {
            rotationYaw -= 45.0F * strafeFactor;
         }

         if (mc.field_1724.field_3913.field_54155.comp_3162()) {
            rotationYaw += 45.0F * strafeFactor;
         }
      }

      if (strafeFactor < 0.0F) {
         rotationYaw -= 180.0F;
      }

      return (float)Math.toRadians((double)rotationYaw);
   }

   public static double getDegreesRelativeToView(class_243 positionRelativeToPlayer, float yaw) {
      float optimalYaw = (float)Math.atan2(-positionRelativeToPlayer.field_1352, positionRelativeToPlayer.field_1350);
      double currentYaw = Math.toRadians((double)class_3532.method_15393(yaw));
      return Math.toDegrees(class_3532.method_15338((double)optimalYaw - currentYaw));
   }

   public static class_10185 getDirectionalInputForDegrees(class_10185 input, double dgs) {
      return getDirectionalInputForDegrees(input, dgs, 20.0F);
   }

   public static void setSpeed(double speed) {
      if (mc.field_1724 != null) {
         float yaw = mc.field_1724.method_36454();
         double rad = Math.toRadians((double)yaw);
         double x = 0.0;
         double z = 0.0;
         class_241 move = mc.field_1724.field_3913.method_3128();
         float forward = move.field_1342;
         float strafe = move.field_1343;
         if (forward > 0.0F) {
            x -= Math.sin(rad) * speed;
            z += Math.cos(rad) * speed;
         }

         if (forward < 0.0F) {
            x += Math.sin(rad) * speed;
            z -= Math.cos(rad) * speed;
         }

         if (strafe > 0.0F) {
            x += Math.cos(rad) * speed;
            z += Math.sin(rad) * speed;
         }

         if (strafe < 0.0F) {
            x -= Math.cos(rad) * speed;
            z -= Math.sin(rad) * speed;
         }

         if (strafe > 0.0F && forward > 0.0F) {
            x = Math.cos(Math.toRadians((double)(yaw + 45.0F))) * speed;
            z = Math.sin(Math.toRadians((double)(yaw + 45.0F))) * speed;
         }

         if (strafe < 0.0F && forward > 0.0F) {
            x = -Math.cos(Math.toRadians((double)(yaw - 45.0F))) * speed;
            z = -Math.sin(Math.toRadians((double)(yaw - 45.0F))) * speed;
         }

         if (strafe > 0.0F && forward < 0.0F) {
            x = -Math.cos(Math.toRadians((double)(yaw + 135.0F))) * speed;
            z = -Math.sin(Math.toRadians((double)(yaw + 135.0F))) * speed;
         }

         if (strafe < 0.0F && forward < 0.0F) {
            x = Math.cos(Math.toRadians((double)(yaw - 135.0F))) * speed;
            z = Math.sin(Math.toRadians((double)(yaw - 135.0F))) * speed;
         }

         mc.field_1724.method_18800(x, mc.field_1724.method_18798().field_1351, z);
      }
   }

   public static class_10185 getDirectionalInputForDegrees(class_10185 input, double dgs, float deadAngle) {
      boolean forwards = input.comp_3159();
      boolean backwards = input.comp_3160();
      boolean left = input.comp_3161();
      boolean right = input.comp_3162();
      if (dgs >= (double)(-90.0F + deadAngle) && dgs <= (double)(90.0F - deadAngle)) {
         forwards = true;
      } else if (dgs < (double)(-90.0F - deadAngle) || dgs > (double)(90.0F + deadAngle)) {
         backwards = true;
      }

      if (dgs >= (double)(0.0F + deadAngle) && dgs <= (double)(180.0F - deadAngle)) {
         right = true;
      } else if (dgs >= (double)(-180.0F + deadAngle) && dgs <= (double)(0.0F - deadAngle)) {
         left = true;
      }

      return new class_10185(forwards, backwards, left, right, input.comp_3163(), input.comp_3164(), input.comp_3165());
   }

   public static void targetMovement() {
      float[] movement = getMovementFromKeys();
      float forward = movement[0];
      float strafe = movement[1];
      if (forward != 0.0F || strafe != 0.0F) {
         class_238 box = AttackAura.target.method_5829();
         double randX = class_3532.method_16436(Math.random(), box.field_1323, box.field_1320);
         double randY = class_3532.method_16436(Math.random(), box.field_1322, box.field_1325);
         double randZ = class_3532.method_16436(Math.random(), box.field_1321, box.field_1324);
         randY = class_3532.method_15350(
            randY, AttackAura.target.method_23318() + 0.2, AttackAura.target.method_23318() + (double)AttackAura.target.method_17682() - 0.2
         );
         class_243 randomHitVec = new class_243(randX, randY, randZ);
         class_243 direction = randomHitVec.method_1020(mc.field_1724.method_33571()).method_1029();
         float targetYaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(direction.field_1350, direction.field_1352)) - 90.0);
         double angle = class_3532.method_15338(
            Math.toDegrees(direction(mc.field_1724.method_70987() ? mc.field_1724.method_36454() : targetYaw, forward, strafe))
         );
         float closestForward = 0.0F;
         float closestStrafe = 0.0F;
         float closestDifference = Float.MAX_VALUE;

         for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward++) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe++) {
               double difference;
               if ((predictedStrafe != 0.0F || predictedForward != 0.0F)
                  && (
                        difference = Math.abs(
                           angle - class_3532.method_15338(Math.toDegrees(direction(mc.field_1724.method_36454(), predictedForward, predictedStrafe)))
                        )
                     )
                     < (double)closestDifference) {
                  closestDifference = (float)difference;
                  closestForward = predictedForward;
                  closestStrafe = predictedStrafe;
               }
            }
         }

         mc.field_1690.field_1894.method_23481(closestForward > 0.0F);
         mc.field_1690.field_1881.method_23481(closestForward < 0.0F);
         mc.field_1690.field_1913.method_23481(closestStrafe > 0.0F);
         mc.field_1690.field_1849.method_23481(closestStrafe < 0.0F);
      }
   }

   public static float[] getMovementFromKeys() {
      float forward = 0.0F;
      float strafe = 0.0F;
      if (class_3675.method_15987(mc.method_22683(), mc.field_1690.field_1894.method_1429().method_1444())) {
         forward++;
      }

      if (class_3675.method_15987(mc.method_22683(), mc.field_1690.field_1881.method_1429().method_1444())) {
         forward--;
      }

      if (class_3675.method_15987(mc.method_22683(), mc.field_1690.field_1913.method_1429().method_1444())) {
         strafe++;
      }

      if (class_3675.method_15987(mc.method_22683(), mc.field_1690.field_1849.method_1429().method_1444())) {
         strafe--;
      }

      return new float[]{forward, strafe};
   }

   public static double[] calculateDirection(double distance) {
      float[] movement = getMovementFromKeys();
      return calculateDirection(movement[0], movement[1], distance);
   }

   public static double[] calculateDirection(float forward, float sideways, double distance) {
      float yaw = mc.field_1724.method_36454();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sinYaw = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cosYaw = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
      double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
      return new double[]{xMovement, zMovement};
   }

   public static void fixMovementTest(EventMoveInput event, float yaw, float targetYaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      double angle = class_3532.method_15338(Math.toDegrees(direction(mc.field_1724.method_6128() ? yaw : targetYaw, forward, strafe)));
      if (forward != 0.0F || strafe != 0.0F) {
         float closestForward = 0.0F;
         float closestStrafe = 0.0F;
         float closestDifference = Float.MAX_VALUE;

         for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward++) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe++) {
               double difference;
               if ((predictedStrafe != 0.0F || predictedForward != 0.0F)
                  && (difference = Math.abs(angle - class_3532.method_15338(Math.toDegrees(direction(yaw, predictedForward, predictedStrafe)))))
                     < (double)closestDifference) {
                  closestDifference = (float)difference;
                  closestForward = predictedForward;
                  closestStrafe = predictedStrafe;
               }
            }
         }

         event.setForward(closestForward);
         event.setStrafe(closestStrafe);
      }
   }

   public static void fixMovement(EventMoveInput event, float yaw, float targetYaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      double angle = class_3532.method_15338(Math.toDegrees(direction(mc.field_1724.method_6128() ? yaw : targetYaw, forward, strafe)));
      if (forward != 0.0F || strafe != 0.0F) {
         float closestForward = 0.0F;
         float closestStrafe = 0.0F;
         float closestDifference = Float.MAX_VALUE;

         for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward++) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe++) {
               double difference;
               if ((predictedStrafe != 0.0F || predictedForward != 0.0F)
                  && (difference = Math.abs(angle - class_3532.method_15338(Math.toDegrees(direction(yaw, predictedForward, predictedStrafe)))))
                     < (double)closestDifference) {
                  closestDifference = (float)difference;
                  closestForward = predictedForward;
                  closestStrafe = predictedStrafe;
               }
            }
         }

         event.setForward(closestForward);
         event.setStrafe(closestStrafe);
      }
   }

   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0F) {
         forward = -0.5F;
      }

      if (moveForward > 0.0F) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }

   @Generated
   private MoveUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
