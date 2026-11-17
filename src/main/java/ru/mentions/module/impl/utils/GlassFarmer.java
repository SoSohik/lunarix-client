package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "Glass Farmer",
   desc = "Легитный фарм стекла",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class GlassFarmer extends Module {
   protected static final class_310 mc = class_310.method_1551();
   private class_2338 startPos = null;
   private GlassFarmer.State currentState = GlassFarmer.State.BUILDING;
   private int currentX = -1;
   private int currentY = 0;
   private int currentZ = -1;
   private long actionTimer = 0L;
   private boolean isRotated = false;
   private class_2338 lastTargetPos = null;
   private final long clickDelayMs = 35L;
   private final int buildDelayTicks = 3;
   private int buildTickCounter = 0;

   @Override
   public void onEnable() {
      if (mc.field_1724 == null) {
         this.toggle();
      } else {
         this.startPos = mc.field_1724.method_24515();
         this.currentState = GlassFarmer.State.BUILDING;
         this.currentX = -1;
         this.currentY = 0;
         this.currentZ = -1;
         this.buildTickCounter = 0;
         this.actionTimer = 0L;
         this.isRotated = false;
         this.lastTargetPos = null;
      }
   }

   @Override
   public void onDisable() {
      this.startPos = null;
      mc.field_1690.field_1886.method_23481(false);
      mc.field_1690.field_1904.method_23481(false);
      mc.field_1690.field_1903.method_23481(false);
      this.lastTargetPos = null;
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         if (this.currentState == GlassFarmer.State.BUILDING) {
            mc.field_1690.field_1886.method_23481(false);
            if (this.buildTickCounter < 3) {
               this.buildTickCounter++;
               return;
            }

            this.doBuildingLogic();
         } else if (this.currentState == GlassFarmer.State.DIGGING) {
            mc.field_1690.field_1904.method_23481(false);
            mc.field_1690.field_1903.method_23481(false);
            this.doDiggingLogic();
         }
      }
   }

   private void doBuildingLogic() {
      int sandSlot = this.findItemInHotbar(class_1802.field_8858);
      if (sandSlot == -1) {
         this.currentY--;
         this.currentState = GlassFarmer.State.DIGGING;
      } else {
         class_2338 targetPos = this.startPos.method_10069(this.currentX, this.currentY, this.currentZ);
         if (mc.field_1687.method_8320(targetPos).method_26204() == class_2246.field_10102) {
            this.advanceBuildPosition();
            this.resetActionState();
         } else {
            if (this.currentX == 0 && this.currentZ == 0 && mc.field_1724.method_24515().method_10264() <= targetPos.method_10264()) {
               if (mc.field_1724.method_24828()) {
                  mc.field_1690.field_1903.method_23481(true);
                  return;
               }

               mc.field_1690.field_1903.method_23481(false);
            }

            mc.field_1724.method_31548().method_61496(sandSlot);
            this.lookAt(targetPos);
            if (this.isRotated && targetPos.equals(this.lastTargetPos)) {
               if (System.currentTimeMillis() - this.actionTimer >= 35L) {
                  new class_3965(
                     new class_243((double)targetPos.method_10263() + 0.5, (double)targetPos.method_10264() + 0.5, (double)targetPos.method_10260() + 0.5),
                     class_2350.field_11036,
                     targetPos,
                     false
                  );
                  mc.field_1690.field_1904.method_23481(true);
                  this.buildTickCounter = 0;
                  this.advanceBuildPosition();
                  this.resetActionState();
               }
            } else {
               this.actionTimer = System.currentTimeMillis();
               this.isRotated = true;
               this.lastTargetPos = targetPos;
            }
         }
      }
   }

   private void doDiggingLogic() {
      int shovelSlot = this.findShovelInHotbar();
      if (shovelSlot == -1) {
         this.toggle();
      } else {
         class_2338 targetPos = this.startPos.method_10069(0, this.currentY, 0);
         if (mc.field_1687.method_8320(targetPos).method_26204() == class_2246.field_10102) {
            mc.field_1724.method_31548().method_61496(shovelSlot);
            this.lookAt(targetPos);
            if (!this.isRotated || !targetPos.equals(this.lastTargetPos)) {
               this.actionTimer = System.currentTimeMillis();
               this.isRotated = true;
               this.lastTargetPos = targetPos;
               return;
            }

            if (System.currentTimeMillis() - this.actionTimer < 35L) {
               return;
            }

            if (mc.field_1761.method_2902(targetPos, class_2350.field_11036)) {
               if (!mc.field_1690.field_1886.method_1434()) {
                  mc.field_1690.field_1886.method_23481(true);
               }

               this.resetActionState();
            }
         } else {
            this.currentY--;
            this.resetActionState();
            if (this.currentY < 0) {
               this.toggle();
            }
         }
      }
   }

   private void resetActionState() {
      this.isRotated = false;
      this.actionTimer = 0L;
   }

   private void advanceBuildPosition() {
      this.currentX++;
      if (this.currentX > 1) {
         this.currentX = -1;
         this.currentZ++;
         if (this.currentZ > 1) {
            this.currentZ = -1;
            this.currentY++;
         }
      }
   }

   private int findItemInHotbar(class_1792 item) {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private int findShovelInHotbar() {
      for (int i = 0; i < 9; i++) {
         String name = mc.field_1724.method_31548().method_5438(i).method_7909().method_7876();
         if (name.contains("shovel")) {
            return i;
         }
      }

      return -1;
   }

   private void lookAt(class_2338 pos) {
      double diffX = (double)pos.method_10263() + 0.5 - mc.field_1724.method_23317();
      double diffY = (double)pos.method_10264() + 0.5 - (mc.field_1724.method_23318() + (double)mc.field_1724.method_5751());
      double diffZ = (double)pos.method_10260() + 0.5 - mc.field_1724.method_23321();
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      RotationProcess.update(new Rotation(yaw, pitch), 140.0F, 140.0F, 6, 1);
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      BUILDING,
      DIGGING;
   }
}
