package ru.mentions.utils.aura;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_4050;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class UBoxPoints implements IMinecraft {
   public static double clamp(double value, double min, double max) {
      return Math.min(max, Math.max(value, min));
   }

   public static int lerp(int a, int b, float f) {
      return a + (int)(f * (float)(b - a));
   }

   public static double lerp(double a, double b, double f) {
      return a + f * (b - a);
   }

   public static class_239 traceBlock(class_243 startVec, class_243 endVec, class_3960 blockMode, class_242 fluidMode) {
      return mc.field_1687.method_17742(new class_3959(startVec, endVec, blockMode, fluidMode, mc.field_1724));
   }

   private static double getDistanceXZ(class_746 self, double x, double z) {
      double d0 = self.method_23317() - x;
      double d1 = self.method_23321() - z;
      return (double)class_3532.method_15355((float)(d0 * d0 + d1 * d1));
   }

   private static boolean seenOnce3(class_746 self, double x, double y, double z) {
      class_243 vector3d1 = new class_243(x, y, z);
      return mc.field_1687 != null
         && traceBlock(self.method_33571(), vector3d1, class_3960.field_17558, class_242.field_1348).method_17783() != class_240.field_1332;
   }

   private static boolean seenOnceVector3d(class_746 self, class_243 vec) {
      class_243 vector3d = new class_243(self.method_23317(), self.method_23320(), self.method_23321());
      return mc.field_1687 != null && traceBlock(vector3d, vec, class_3960.field_17558, class_242.field_1348).method_17783() != class_240.field_1332;
   }

   private static boolean localSeen(class_746 selfEntity, class_243 xyz, float scale) {
      return scale == 0.0F
         ? seenOnce3(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350)
         : seenOnce3(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350)
            && seenOnce3(selfEntity, xyz.field_1352, xyz.field_1351 + (double)scale, xyz.field_1350)
            && seenOnce3(selfEntity, xyz.field_1352, xyz.field_1351 - (double)scale, xyz.field_1350)
            && seenOnce3(selfEntity, xyz.field_1352 + (double)scale, xyz.field_1351, xyz.field_1350)
            && seenOnce3(selfEntity, xyz.field_1352 - (double)scale, xyz.field_1351, xyz.field_1350)
            && seenOnce3(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350 + (double)scale)
            && seenOnce3(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350 - (double)scale);
   }

   public static List<class_243> entityBoxVec3dsAlternate(class_238 aabb) {
      ArrayList<class_243> vecs = new ArrayList<>();
      double offsetXYZ = 0.01F;
      int maxPointsCountXZ = 17;
      int minPointsCountXZ = 5;
      int maxPointsCountY = 24;
      int minPointsCountY = 6;
      aabb = aabb.method_989(-offsetXYZ, -offsetXYZ, -offsetXYZ);
      double[] whh = new double[]{aabb.field_1320 - aabb.field_1323, aabb.field_1325 - aabb.field_1322, (aabb.field_1325 - aabb.field_1322) / 1.05};
      double[] xyz = new double[]{aabb.field_1323 + whh[0] / 2.0, aabb.field_1322, aabb.field_1321 + whh[0] / 2.0};
      double[] xyz1 = new double[]{aabb.field_1323, aabb.field_1322, aabb.field_1321};
      double[] xyz2 = new double[]{aabb.field_1320, aabb.field_1325, aabb.field_1324};
      float sqrtWHH0CubeD2 = (float)Math.sqrt(whh[0] * whh[0] + whh[0] * whh[0] + whh[0] * whh[0]) / 2.0F;
      class_746 me = mc.field_1724;
      if (me == null) {
         return null;
      } else {
         float factorCount = (float)(
            (1.0 - Math.min(me.method_73189().method_1022(new class_243(xyz[0], xyz[1], xyz[2])) / 5.0, 1.0))
               * Math.min(me.method_73189().method_1022(new class_243(xyz[0], me.method_23318(), xyz[2])) / 0.6F, 1.0)
         );
         int pointsCountXZ = lerp(minPointsCountXZ, maxPointsCountXZ, factorCount);
         int pointsCountY = lerp(minPointsCountY, maxPointsCountY, factorCount);
         float scaleSeenCheck = 0.0F;
         int[] nArray = IntStream.range(0, pointsCountXZ).toArray();
         int n = nArray.length;

         for (int i = 0; i < n; i++) {
            Integer xsI = nArray[i];
            boolean edgeX = xsI == 0 || xsI == pointsCountXZ - 1;
            double xs = lerp(xyz1[0], xyz2[0], (double)((float)xsI.intValue() / (float)(pointsCountXZ - 1)));
            int[] nArray2 = IntStream.range(0, pointsCountXZ).toArray();
            int n2 = nArray2.length;

            for (int j = 0; j < n2; j++) {
               Integer zsI = nArray2[j];
               boolean edgeZ = zsI == 0 || zsI == pointsCountXZ - 1;
               double zs = lerp(xyz1[2], xyz2[2], (double)((float)zsI.intValue() / (float)(pointsCountXZ - 1)));
               int[] nArray3 = IntStream.range(0, pointsCountY).toArray();
               int n3 = nArray3.length;

               for (int k = 0; k < n3; k++) {
                  Integer ysI = nArray3[k];
                  boolean edgeY = ysI == 0 || ysI == pointsCountY - 1;
                  double ys = lerp(xyz1[1], xyz2[1], (double)((float)ysI.intValue() / (float)(pointsCountY - 1)));
                  class_243 vec = new class_243(xs, ys, zs);
                  if ((edgeX || edgeZ || edgeY)
                     && !(me.method_73189().method_1022(vec.method_1031(0.0, (double)(-me.method_18381(class_4050.field_18076)), 0.0)) < (double)sqrtWHH0CubeD2)
                     && localSeen(me, vec, scaleSeenCheck)
                     && !vecs.add(vec)) {
                     break;
                  }
               }
            }
         }

         return vecs;
      }
   }

   private static double getDistanceAtVec3dToVec3d(class_243 first, class_243 second) {
      double xDiff = first.field_1352 - second.field_1352;
      double yDiff = first.field_1351 - second.field_1351;
      double zDiff = first.field_1350 - second.field_1350;
      return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
   }

   public static class_243 getBestVector3dOnEntityBox(class_238 aabb) {
      return getBestVector3dOnEntityBox(aabb, false);
   }

   public static class_243 getBestVector3dOnEntityBox(class_238 aabb, boolean alwaysMultipoints) {
      if (aabb == null) {
         return mc.field_1724.method_33571();
      } else {
         double[] whh = new double[]{aabb.field_1320 - aabb.field_1323, aabb.field_1325 - aabb.field_1322, (aabb.field_1325 - aabb.field_1322) / 1.1F};
         double[] xyz = new double[]{aabb.field_1323 + whh[0] / 2.0, aabb.field_1322, aabb.field_1321 + whh[0] / 2.0};
         double[] diffs = new double[]{mc.field_1724.method_23318() - xyz[1], getDistanceXZ(mc.field_1724, xyz[0], xyz[2])};
         double ddtn = clamp(Easings.BACK_OUT.ease((diffs[1] - whh[0] / 2.0) / (5.0 + whh[0] / 2.0)), 0.1, 0.95);
         double pca = clamp(ddtn * ddtn, 0.0, 1.0);
         double pitchPointHeight = clamp(whh[2] / 2.0 * pca + whh[2] / 2.0 * clamp(diffs[0] + pca, 0.0, 1.0), 0.0, whh[2]);
         class_243 defaultVec = new class_243(xyz[0], xyz[1] + pitchPointHeight, xyz[2]);
         if (!alwaysMultipoints && !seenOnceVector3d(mc.field_1724, defaultVec)) {
            defaultVec = defaultVec.method_1031(0.0, -pitchPointHeight / 2.0, 0.0);
         }

         if (!(whh[1] <= 1.0) && (alwaysMultipoints || !seenOnceVector3d(mc.field_1724, defaultVec))) {
            List<class_243> normalVecs = entityBoxVec3dsAlternate(aabb);
            float factorDown = 1.0F - (float)Math.max(Math.min((diffs[1] - 2.0) / 3.0, 1.0), 0.0);
            class_243 toSortVec = new class_243(
               mc.field_1724.method_23317(),
               mc.field_1724.method_23318() + 0.6F + lerp(pitchPointHeight, pitchPointHeight / 2.5, (double)factorDown),
               mc.field_1724.method_23321()
            );
            if (normalVecs != null && normalVecs.size() > 1) {
               normalVecs.sort(Comparator.comparing(vec3 -> getDistanceAtVec3dToVec3d(toSortVec, vec3)));
            }

            return normalVecs != null && !normalVecs.isEmpty() ? normalVecs.getFirst() : defaultVec;
         } else {
            return defaultVec;
         }
      }
   }

   public static class_243 getVecNewStatic(class_238 aabb) {
      if (aabb == null) {
         return mc.field_1724.method_33571();
      } else {
         double centerX = aabb.field_1323 + (aabb.field_1320 - aabb.field_1323) / 2.0;
         double centerY = aabb.field_1322 + (aabb.field_1325 - aabb.field_1322) / 2.0;
         double centerZ = aabb.field_1321 + (aabb.field_1324 - aabb.field_1321) / 2.0;
         class_243 centerVec = new class_243(centerX, centerY + 0.25, centerZ);
         List<class_243> normalVecs;
         if (!seenOnceVector3d(mc.field_1724, centerVec) && (normalVecs = entityBoxVec3dsAlternate(aabb)) != null && !normalVecs.isEmpty()) {
            normalVecs.sort(Comparator.comparing(vec -> vec.method_1025(mc.field_1724.method_33571())));
            return normalVecs.get(0);
         } else {
            return centerVec;
         }
      }
   }

   @Generated
   private UBoxPoints() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
