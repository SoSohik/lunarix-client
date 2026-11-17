package ru.mentions.utils.aura;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.joml.Vector2f;
import org.joml.Vector4f;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class AuraUtil implements IMinecraft {
   public static class_243 getNearestPoint(class_1309 entity) {
      class_243 pos = mc.field_1724.method_33571();
      return new class_243(
         class_3532.method_15350(pos.field_1352, entity.method_5829().field_1323, entity.method_5829().field_1320),
         class_3532.method_15350(pos.field_1351, entity.method_5829().field_1322, entity.method_5829().field_1325),
         class_3532.method_15350(pos.field_1350, entity.method_5829().field_1321, entity.method_5829().field_1324)
      );
   }

   public static double[] calculateDirection(double distance) {
      float[] movement = getMovementFromKeys();
      return calculateDirection(movement[0], movement[1], distance);
   }

   public static boolean nullCheck() {
      return mc.field_1724 == null || mc.field_1687 == null;
   }

   public static boolean isPlayerInWeb() {
      class_238 playerBox = mc.field_1724.method_5829();
      class_2338 playerPosition = mc.field_1724.method_24515();
      return getNearbyBlockPositions(playerPosition).stream().anyMatch(pos -> isBlockCobweb(playerBox, pos));
   }

   private static boolean isBlockCobweb(class_238 playerBox, class_2338 pos) {
      if (!mc.field_1687.method_8320(pos).method_27852(class_2246.field_10343)) {
         return false;
      } else {
         class_238 blockBox = new class_238(pos);
         return playerBox.method_994(blockBox);
      }
   }

   private static List<class_2338> getNearbyBlockPositions(class_2338 center) {
      ArrayList<class_2338> positions = new ArrayList<>();

      for (int x = center.method_10263() - 2; x <= center.method_10263() + 2; x++) {
         for (int y = center.method_10264() - 1; y <= center.method_10264() + 4; y++) {
            for (int z = center.method_10260() - 2; z <= center.method_10260() + 2; z++) {
               positions.add(new class_2338(x, y, z));
            }
         }
      }

      return positions;
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

   public static float[] getMovementFromKeys() {
      float forward = 0.0F;
      float strafe = 0.0F;
      class_1041 handle = mc.method_22683();
      if (class_3675.method_15987(handle, mc.field_1690.field_1894.method_1429().method_1444())) {
         forward++;
      }

      if (class_3675.method_15987(handle, mc.field_1690.field_1881.method_1429().method_1444())) {
         forward--;
      }

      if (class_3675.method_15987(handle, mc.field_1690.field_1913.method_1429().method_1444())) {
         strafe++;
      }

      if (class_3675.method_15987(handle, mc.field_1690.field_1849.method_1429().method_1444())) {
         strafe--;
      }

      return new float[]{forward, strafe};
   }

   public static float calculateCorrectYawOffset(float yaw) {
      double xDiff = mc.field_1724.method_23317() - mc.field_1724.field_6014;
      double zDiff = mc.field_1724.method_23321() - mc.field_1724.field_5969;
      float distSquared = (float)(xDiff * xDiff + zDiff * zDiff);
      float renderYawOffset = mc.field_1724.field_6220;
      float offset = mc.field_1724.field_6220;
      if (distSquared > 0.0025000002F) {
         offset = (float)class_3532.method_15349(zDiff, xDiff) * 180.0F / (float) Math.PI - 90.0F;
      }

      if (mc.field_1724 != null && mc.field_1724.field_6251 > 0.0F) {
         offset = yaw;
      }

      float yawOffsetDiff = class_3532.method_15393(yaw - (renderYawOffset + class_3532.method_15393(offset - renderYawOffset) * 0.3F));
      yawOffsetDiff = class_3532.method_15363(yawOffsetDiff, -15.0F, 15.0F);
      renderYawOffset = yaw - yawOffsetDiff;
      if (yawOffsetDiff * yawOffsetDiff > 2500.0F) {
         renderYawOffset += yawOffsetDiff * 0.2F;
      }

      return renderYawOffset;
   }

   public static boolean collideWith(class_1309 entity, float grow) {
      class_238 box = mc.field_1724.method_5829();
      class_238 targetbox = entity.method_5829().method_1009((double)grow, 0.0, (double)grow);
      return box.field_1320 > targetbox.field_1323
         && box.field_1325 > targetbox.field_1322
         && box.field_1324 > targetbox.field_1321
         && box.field_1323 < targetbox.field_1320
         && box.field_1322 < targetbox.field_1325
         && box.field_1321 < targetbox.field_1324;
   }

   public static float getAngleDiff(float targetYaw, float currentYaw) {
      float diff = targetYaw - currentYaw;

      while (diff <= -180.0F) {
         diff += 360.0F;
      }

      while (diff > 180.0F) {
         diff -= 360.0F;
      }

      return Math.abs(diff);
   }

   public static class_3965 raycast(class_243 start, class_243 end, class_3960 shapeType, class_1297 entity) {
      return mc.field_1687.method_17742(new class_3959(start, end, shapeType, class_242.field_1348, entity));
   }

   public static Rotation getOffset(Rotation deltaToTarget, float progress) {
      float curveStrength = 3.0F;
      float smoothness = 1.0F - (float)Math.cos((double)progress * Math.PI);
      float yawSign = Math.signum(deltaToTarget.getYaw());
      float pitchSign = Math.signum(deltaToTarget.getPitch());
      float offsetYaw = 0.0F;
      float offsetPitch = 0.0F;
      boolean verticalAiming = Math.abs(deltaToTarget.getYaw()) < 1.5F && Math.abs(deltaToTarget.getYaw()) > 10.0F;
      if (verticalAiming) {
         offsetYaw = pitchSign * curveStrength * (1.0F - progress);
         offsetPitch = 0.0F;
      } else if (pitchSign > 0.0F && yawSign >= 0.0F) {
         offsetYaw = -curveStrength * (1.0F - progress);
         offsetPitch = -curveStrength * smoothness;
      } else if (pitchSign > 0.0F && yawSign < 0.0F) {
         offsetYaw = curveStrength * (1.0F - progress);
         offsetPitch = -curveStrength * smoothness;
      } else if (pitchSign < 0.0F && yawSign >= 0.0F) {
         offsetYaw = -curveStrength * (1.0F - progress);
         offsetPitch = curveStrength * smoothness;
      } else if (pitchSign < 0.0F && yawSign < 0.0F) {
         offsetYaw = curveStrength * (1.0F - progress);
         offsetPitch = curveStrength * smoothness;
      }

      return new Rotation(offsetYaw, offsetPitch);
   }

   public static double getStrictDistance(class_1297 entity) {
      return getClosestVec(entity).method_1033();
   }

   public static Rotation cameraAngle() {
      return new Rotation(mc.field_1724.method_36454(), mc.field_1724.method_36455());
   }

   public static float interpolate(double oldValue, double newValue, double interpolationValue) {
      return (float)(oldValue + (newValue - oldValue) * interpolationValue);
   }

   public static boolean validDistance(class_1297 entity, float distance, boolean smart) {
      return getStrictDistance(entity) < (double)distance;
   }

   public static class_243 getClosestVec(class_1297 entity) {
      class_243 eyePosVec = mc.field_1724.method_33571();
      return getClosestVec(eyePosVec, entity).method_1020(eyePosVec);
   }

   public static class_243 getClosestVec(class_243 vec, class_238 AABB) {
      return new class_243(
         MathUtil.clamp(vec.field_1352, AABB.field_1323, AABB.field_1320),
         MathUtil.clamp(vec.field_1351, AABB.field_1322, AABB.field_1325),
         MathUtil.clamp(vec.field_1350, AABB.field_1321, AABB.field_1324)
      );
   }

   public static class_243 getClosestVec(class_243 vec, class_1297 entity) {
      return getClosestVec(vec, entity.method_5829());
   }

   public static class_243 getVector4(class_1309 target) {
      double wHalf = (double)(target.method_17681() / 2.0F);
      double yExpand = class_3532.method_15350(target.method_23318() - 6.0, 0.0, (double)target.method_17682());
      double xExpand = class_3532.method_15350(mc.field_1724.method_23317() - target.method_23317(), -wHalf, wHalf);
      double zExpand = class_3532.method_15350(mc.field_1724.method_23321() - target.method_23321(), -wHalf, wHalf);
      return new class_243(
         target.method_23317() - mc.field_1724.method_23317() + xExpand,
         target.method_23318() - mc.field_1724.method_23318() - 0.8F,
         target.method_23321() - mc.field_1724.method_23321() + zExpand
      );
   }

   public static class_243 getVector3(class_1309 target) {
      double yExpand = class_3532.method_15350(target.method_23318() - target.method_23318(), 0.0, (double)target.method_17682());
      double xExpand = class_3532.method_15350(mc.field_1724.method_23317() - target.method_23317(), 0.0, 0.0);
      double zExpand = class_3532.method_15350(mc.field_1724.method_23321() - target.method_23321(), 0.0, 0.0);
      return new class_243(
         target.method_23317() - mc.field_1724.method_23317() + xExpand,
         target.method_23318() - mc.field_1724.method_23318() - 0.8F,
         target.method_23321() - mc.field_1724.method_23321() + zExpand
      );
   }

   public static class_241 getVecTAKSA1(class_243 targetedEntity) {
      double posX = targetedEntity.method_10216();
      double posY = targetedEntity.method_10214();
      double posZ = targetedEntity.method_10215();
      double deltaX = posX - mc.field_1724.method_23317();
      double deltaY = posY - (mc.field_1724.method_23318() + (double)mc.field_1724.method_18381(mc.field_1724.method_18376()));
      double deltaZ = posZ - mc.field_1724.method_23321();
      double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance)));
      return new class_241(yaw, pitch);
   }

   public static float wrapAngleTo180(float angle) {
      if ((angle = angle % 360.0F) >= 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   public static class_243 getVector2(class_1309 target) {
      double yExpand = class_3532.method_15350(target.method_23320() - target.method_23318(), 0.0, (double)target.method_17682());
      double xExpand = class_3532.method_15350(mc.field_1724.method_23317() - target.method_23317(), 0.0, 0.0);
      double zExpand = class_3532.method_15350(mc.field_1724.method_23321() - target.method_23321(), 0.0, 0.0);
      return new class_243(
         target.method_23317() - mc.field_1724.method_23317() + xExpand,
         target.method_23318() - mc.field_1724.method_23320() + yExpand,
         target.method_23321() - mc.field_1724.method_23321() + zExpand
      );
   }

   public static class_243 getVector(class_1309 target) {
      double wHalf = (double)(target.method_17681() / 2.0F);
      double yExpand = class_3532.method_15350(target.method_23320() - target.method_23318(), 0.0, (double)target.method_17682());
      double xExpand = class_3532.method_15350(mc.field_1724.method_23317() - target.method_23317(), -wHalf, wHalf);
      double zExpand = class_3532.method_15350(mc.field_1724.method_23321() - target.method_23321(), -wHalf, wHalf);
      return new class_243(
         target.method_23317() - mc.field_1724.method_23317() + xExpand,
         target.method_23318() - mc.field_1724.method_23320() + yExpand,
         target.method_23321() - mc.field_1724.method_23321() + zExpand
      );
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

   public static class_243 getClosestTargetPoint(class_243 vec, class_1297 entity, float point) {
      if (entity == null) {
         return class_243.field_1353;
      } else {
         class_238 box = entity.method_5829().method_1014((double)(-point));
         class_243 center = box.method_1005();
         class_243 closestPoint = null;
         double closestDistance = Double.MAX_VALUE;

         for (double offsetX = 0.0; offsetX <= (box.field_1320 - box.field_1323) / 2.0; offsetX += 0.1) {
            for (double offsetY = 0.0; offsetY <= (box.field_1325 - box.field_1322) / 2.0; offsetY += 0.1) {
               for (double offsetZ = 0.0; offsetZ <= (box.field_1324 - box.field_1321) / 2.0; offsetZ += 0.1) {
                  for (int signX : new int[]{-1, 1}) {
                     for (int signY : new int[]{-1, 1}) {
                        for (int signZ : new int[]{-1, 1}) {
                           double x = center.field_1352 + (double)signX * offsetX;
                           double y = center.field_1351 + (double)signY * offsetY;
                           double z = center.field_1350 + (double)signZ * offsetZ;
                           class_243 potentialPoint = new class_243(x, y, z);
                           Vector2f rotation = calculate(potentialPoint);
                           class_239 result = RayTraceUtil.calculateRayTrace(6.0, rotation.x, rotation.y, mc.field_1724, false);
                           double distance;
                           class_3966 entityTrace;
                           if (result instanceof class_3966
                              && (entityTrace = (class_3966)result).method_17782().equals(entity)
                              && (distance = vec.method_1022(potentialPoint)) < closestDistance) {
                              closestDistance = distance;
                              closestPoint = potentialPoint;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (closestPoint != null) {
            return closestPoint;
         } else {
            double closestX = MathUtil.clamp(vec.field_1352, box.field_1323, box.field_1320);
            double closestY = MathUtil.clamp(vec.field_1351, box.field_1322, box.field_1325);
            double closestZ = MathUtil.clamp(vec.field_1350, box.field_1321, box.field_1324);
            return new class_243(closestX, closestY, closestZ);
         }
      }
   }

   public static Vector2f calculate(class_243 toVec) {
      return calculate(mc.field_1724.method_73189().method_1031(0.0, mc.field_1724.method_23320(), 0.0), toVec);
   }

   public static Vector2f calculate(class_243 fromVec, class_243 toVec) {
      double TO_DEGREES = 180.0 / Math.PI;
      class_243 diff = toVec.method_1020(fromVec);
      double distance = Math.hypot(diff.field_1352, diff.field_1350);
      float yaw = (float)(class_3532.method_15349(diff.field_1350, diff.field_1352) * (180.0 / Math.PI)) - 90.0F;
      float pitch = (float)(-(class_3532.method_15349(diff.field_1351, distance) * (180.0 / Math.PI)));
      return new Vector2f(yaw, pitch);
   }

   public static class_243 getClosestTargetPoint(class_1297 entity) {
      float tickDelta = mc.method_61966().method_60637(false);
      return getClosestTargetPoint(mc.field_1724.method_5836(tickDelta), entity, Math.min(entity.method_17681(), entity.method_17682()) / 4.0F);
   }

   public static Vector4f calculateRotationFromCamera(class_1309 target) {
      float tickDelta = mc.method_61966().method_60637(false);
      class_243 eyePos = mc.field_1724.method_5836(tickDelta);
      class_243 vec = getClosestTargetPoint(target).method_1020(eyePos);
      float rawYaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(vec.field_1350, vec.field_1352)) - 90.0));
      float rawPitch = (float)(-Math.toDegrees(Math.atan2(vec.field_1351, Math.sqrt(vec.field_1352 * vec.field_1352 + vec.field_1350 * vec.field_1350))));
      float yawDelta = class_3532.method_15393(rawYaw - mc.field_1724.method_36454());
      float pitchDelta = rawPitch - mc.field_1724.method_36455();
      return new Vector4f(rawYaw, rawPitch, yawDelta, pitchDelta);
   }

   public static double calculateFOVFromCamera(class_1309 target) {
      Vector4f rotation = calculateRotationFromCamera(target);
      float yawDelta = rotation.z;
      float pitchDelta = rotation.w;
      return Math.sqrt((double)(yawDelta * yawDelta + pitchDelta * pitchDelta));
   }

   @Generated
   private AuraUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
