package ru.mentions.module.impl.combat.aura.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.aura.GCDUtil;

@Environment(EnvType.CLIENT)
final class DeRotUtil implements IMinecraft {
   private DeRotUtil() {
   }

   static float yawFromDir(class_243 dir) {
      return class_3532.method_15393((float)(Math.toDegrees(Math.atan2(dir.field_1350, dir.field_1352)) - 90.0));
   }

   static float pitchFromDir(class_243 dir) {
      return class_3532.method_15393((float)Math.toDegrees(-Math.atan2(dir.field_1351, Math.hypot(dir.field_1352, dir.field_1350))));
   }

   static class_243 aimPoint(class_238 box, class_243 eye, double driftH, double driftV) {
      double height = box.field_1325 - box.field_1322;
      double minY = box.field_1322 + height * 0.2;
      double maxY = box.field_1325 - height * 0.2;
      double cx = (box.field_1323 + box.field_1320) * 0.5;
      double cz = (box.field_1321 + box.field_1324) * 0.5;
      double y = class_3532.method_15350(eye.field_1351, minY, maxY);
      double dist = distanceToBox(eye, box);
      double pull = class_3532.method_15350(1.0 - dist / 2.5, 0.0, 1.0);
      double x = cx;
      double z = cz;
      if (pull > 0.0) {
         double mx = (box.field_1320 - box.field_1323) * 0.2;
         double mz = (box.field_1324 - box.field_1321) * 0.2;
         double ex = class_3532.method_15350(eye.field_1352, box.field_1323 + mx, box.field_1320 - mx);
         double ez = class_3532.method_15350(eye.field_1350, box.field_1321 + mz, box.field_1324 - mz);
         x = cx + pull * (ex - cx);
         z = cz + pull * (ez - cz);
      }

      if (driftH != 0.0 || driftV != 0.0) {
         double midY = (minY + maxY) * 0.5;
         double halfY = (maxY - minY) * 0.5;
         y = midY + driftV * halfY;
         double sx = (box.field_1320 - box.field_1323) * 0.5 * 0.55;
         double sz = (box.field_1324 - box.field_1321) * 0.5 * 0.55;
         x = class_3532.method_15350(x + driftH * sx, box.field_1323 + 0.05, box.field_1320 - 0.05);
         z = class_3532.method_15350(z + driftH * sz, box.field_1321 + 0.05, box.field_1324 - 0.05);
      }

      return new class_243(x, y, z);
   }

   static double distanceToBox(class_243 v, class_238 box) {
      double dx = Math.max(Math.max(box.field_1323 - v.field_1352, 0.0), v.field_1352 - box.field_1320);
      double dy = Math.max(Math.max(box.field_1322 - v.field_1351, 0.0), v.field_1351 - box.field_1325);
      double dz = Math.max(Math.max(box.field_1321 - v.field_1350, 0.0), v.field_1350 - box.field_1324);
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   static float quantizeStep(float step) {
      float gcd = GCDUtil.getGCDValue();
      return Float.isFinite(gcd) && !(gcd <= 0.0F) ? (float)Math.round(step / gcd) * gcd : step;
   }

   static class_243 frameDelta(class_1297 entity) {
      return new class_243(entity.method_23317() - entity.field_6014, entity.method_23318() - entity.field_6036, entity.method_23321() - entity.field_5969);
   }
}
