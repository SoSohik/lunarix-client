package ru.mentions.utils.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1923;
import net.minecraft.class_2806;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class Spectator {
   private static class_1297 target;

   private Spectator() {
   }

   public static boolean isActive() {
      return target != null;
   }

   public static class_1297 getTarget() {
      return target;
   }

   public static boolean start(class_1297 entity) {
      class_310 mc = class_310.method_1551();
      if (entity == null || mc.field_1724 == null || mc.field_1687 == null) {
         return false;
      } else if (entity != mc.field_1724 && !entity.method_31481()) {
         target = entity;
         mc.method_1504(entity);
         if (mc.field_1769 != null) {
            mc.field_1769.method_3292();
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean isTerrainLoaded(class_1297 entity) {
      class_310 mc = class_310.method_1551();
      if (entity != null && mc.field_1687 != null) {
         class_1923 pos = entity.method_31476();
         return mc.field_1687.method_2935().method_2857(pos.field_9181, pos.field_9180, class_2806.field_12803, false) != null;
      } else {
         return false;
      }
   }

   public static int loadedChunksAround(class_1297 entity, int radius) {
      class_310 mc = class_310.method_1551();
      if (entity != null && mc.field_1687 != null) {
         class_1923 center = entity.method_31476();
         int loaded = 0;

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (mc.field_1687.method_2935().method_2857(center.field_9181 + x, center.field_9180 + z, class_2806.field_12803, false) != null) {
                  loaded++;
               }
            }
         }

         return loaded;
      } else {
         return 0;
      }
   }

   public static int chunksInSquare(int radius) {
      int side = radius * 2 + 1;
      return side * side;
   }

   public static void stop() {
      class_310 mc = class_310.method_1551();
      target = null;
      if (mc.field_1724 != null) {
         mc.method_1504(mc.field_1724);
      }
   }

   public static void reset() {
      target = null;
   }

   public static boolean validate() {
      if (target == null) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null && mc.field_1687 != null && !target.method_31481() && target.method_73183() == mc.field_1687) {
            class_1297 class_12972 = target;
            class_1309 living;
            if (!(target instanceof class_1309) || (living = (class_1309)class_12972).method_5805()) {
               if (mc.method_1560() != target) {
                  mc.method_1504(target);
               }

               return false;
            }
         }

         stop();
         return true;
      }
   }
}
