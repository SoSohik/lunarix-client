package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.StopWatchP;

@ModuleInfo(
   name = "Crystal Aura",
   desc = "Автоматическая аура на кристаллы",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class CrystalAura extends Module {
   private final SliderSetting range = new SliderSetting(this, "Дальность", 5.0F, 1.0F, 10.0F, 0.5F);
   private final SliderSetting placeDelay = new SliderSetting(this, "Задержка постановки (мс)", 200.0F, 50.0F, 1000.0F, 50.0F);
   private final SliderSetting breakDelay = new SliderSetting(this, "Задержка взрыва (мс)", 100.0F, 50.0F, 1000.0F, 50.0F);
   private final BooleanSetting rotateToBlock = new BooleanSetting(this, "Наведение на блок", true);
   private final BooleanSetting onlyNearEnemy = new BooleanSetting(this, "Только возле врага", true);
   private final SliderSetting enemyRange = new SliderSetting(this, "Дальность врага", 6.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> this.onlyNearEnemy.getValue());
   private final BooleanSetting obsidianOnly = new BooleanSetting(this, "Только обсидиан", false);
   private final StopWatchP placeTimer = new StopWatchP();
   private final StopWatchP breakTimer = new StopWatchP();
   private int prevSlot = -1;

   @EventHandler
   public void update(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.breakNearbyCrystals();
         class_2338 targetBlock = this.findNearestObsidian();
         if (targetBlock == null) {
            this.restoreSlot();
         } else {
            int crystalSlot = this.findCrystalSlot();
            if (crystalSlot == -1) {
               this.restoreSlot();
            } else {
               if (this.rotateToBlock.getValue()) {
                  this.aimAtBlock(targetBlock);
               }

               if (this.placeTimer.isReached(this.placeDelay.getValue().longValue())) {
                  this.selectSlot(crystalSlot);
                  this.placeCrystal(targetBlock);
                  this.placeTimer.reset();
               }
            }
         }
      }
   }

   private void breakNearbyCrystals() {
      if (this.breakTimer.isReached(this.breakDelay.getValue().longValue())) {
         class_1511 nearest = null;
         double nearestDist = Double.MAX_VALUE;

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1511) {
               class_1511 crystal = (class_1511)entity;
               double dist = mc.field_1724.method_73189().method_1022(crystal.method_73189());
               if (!(dist > (double)this.range.getValue().floatValue()) && dist < nearestDist) {
                  nearestDist = dist;
                  nearest = crystal;
               }
            }
         }

         if (nearest != null) {
            mc.field_1761.method_2918(mc.field_1724, nearest);
            mc.field_1724.method_6104(class_1268.field_5808);
            this.breakTimer.reset();
         }
      }
   }

   private class_2338 findNearestObsidian() {
      int r = this.range.getValue().intValue();
      class_2338 playerPos = mc.field_1724.method_24515();
      int playerY = playerPos.method_10264();
      class_2338 nearest = null;
      double nearestDist = Double.MAX_VALUE;

      for (int x = -r; x <= r; x++) {
         for (int y = -r; y <= r; y++) {
            for (int z = -r; z <= r; z++) {
               class_2338 pos = playerPos.method_10069(x, y, z);
               class_2248 block = mc.field_1687.method_8320(pos).method_26204();
               boolean isObsidian = block == class_2246.field_10540;
               boolean isBedrock = !this.obsidianOnly.getValue() && block == class_2246.field_9987;
               double dist;
               if ((isObsidian || isBedrock)
                  && mc.field_1687.method_8320(pos.method_10084()).method_26215()
                  && mc.field_1687.method_8320(pos.method_10086(2)).method_26215()
                  && (!this.onlyNearEnemy.getValue() || this.hasEnemyNear(pos))
                  && (dist = mc.field_1724.method_73189().method_1022(class_243.method_24953(pos))) < nearestDist) {
                  nearestDist = dist;
                  nearest = pos;
               }
            }
         }
      }

      return nearest;
   }

   private boolean hasEnemyNear(class_2338 pos) {
      class_243 center = class_243.method_24953(pos);
      float maxDist = this.enemyRange.getValue();

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && player.method_73189().method_1022(center) <= (double)maxDist) {
            return true;
         }
      }

      return false;
   }

   private int findCrystalSlot() {
      for (int i = 0; i < 9; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() == class_1802.field_8301) {
            return i;
         }
      }

      return -1;
   }

   private void selectSlot(int slot) {
      if (mc.field_1724.method_31548().method_67532() != slot) {
         if (this.prevSlot == -1) {
            this.prevSlot = mc.field_1724.method_31548().method_67532();
         }

         mc.field_1724.method_31548().method_61496(slot);
         ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
      }
   }

   private void restoreSlot() {
      if (this.prevSlot != -1) {
         mc.field_1724.method_31548().method_61496(this.prevSlot);
         ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
         this.prevSlot = -1;
      }
   }

   private void aimAtBlock(class_2338 pos) {
      double dx = (double)pos.method_10263() + 0.5 - mc.field_1724.method_23317();
      double dy = (double)pos.method_10264() + 1.0 - (mc.field_1724.method_23318() + (double)mc.field_1724.method_5751());
      double dz = (double)pos.method_10260() + 0.5 - mc.field_1724.method_23321();
      double dxz = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dxz)));
      RotationProcess.update(
         new Rotation(yaw + MathUtil.random(-1.1248125F, 1.2148125F), pitch + MathUtil.random(-1.1248125F, 1.2148125F)), 140.0F, 140.0F, 1, 1
      );
   }

   private void placeCrystal(class_2338 pos) {
      class_243 hitVec = new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 1.0, (double)pos.method_10260() + 0.5);
      class_3965 hitResult = new class_3965(hitVec, class_2350.field_11036, pos, false);
      mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hitResult);
      mc.field_1724.method_6104(class_1268.field_5808);
   }

   @Override
   protected void onDisable() {
      this.restoreSlot();
      this.placeTimer.reset();
      this.breakTimer.reset();
   }
}
