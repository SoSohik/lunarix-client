package ru.mentions.module.impl.utils;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1794;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.FreeLookUtil;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.math.ChatUtils;

@ModuleInfo(
   name = "Apple Farmer",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AppleFarmer extends Module {
   public final SliderSetting range = new SliderSetting(this, "Дистанция", 4.5F, 3.0F, 4.5F, 0.1F);
   private AppleFarmer.FarmState currentState = AppleFarmer.FarmState.FIND_SPOT;
   private class_2338 targetPos = null;
   private final List<class_2338> blocksToMine = new ArrayList<>();
   private static final int PLANT_DISTANCE = 2;
   private static final int TREE_SCAN_RADIUS = 4;
   private static final int TREE_SCAN_HEIGHT = 8;
   private class_2350 farmFacing = class_2350.field_11043;
   private class_2338 breakingBlock = null;
   private int timer = 0;
   private int refillTimer = 0;

   @Override
   public void onEnable() {
      super.onEnable();
      this.currentState = AppleFarmer.FarmState.FIND_SPOT;
      this.targetPos = null;
      this.blocksToMine.clear();
      this.breakingBlock = null;
      this.refillTimer = 0;
      if (mc.field_1724 != null) {
         this.farmFacing = mc.field_1724.method_5735();
      }

      this.timer = 0;
   }

   @Override
   public void onDisable() {
      RotationProcess.currentTask = RotationProcess.RotationTask.IDLE;
      RotationProcess.currentPriority = 0;
      RotationProcess.targetRotation = null;
      FreeLookUtil.active = false;
      this.breakingBlock = null;
      super.onDisable();
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.refillTimer++;
         if (this.refillTimer > 4) {
            this.handleAutoRefill();
            this.refillTimer = 0;
         }

         this.timer++;
         if (this.currentState == AppleFarmer.FarmState.BREAKING || this.timer >= 2) {
            switch (this.currentState) {
               case FIND_SPOT:
                  this.handleFindSpot();
                  break;
               case PLACE:
                  this.handlePlace();
                  break;
               case BONEMEAL:
                  this.handleBonemeal();
                  break;
               case SCAN_TREE:
                  this.handleScanTree();
                  break;
               case BREAKING:
                  this.handleBreaking();
            }
         }
      }
   }

   private void handleFindSpot() {
      this.farmFacing = mc.field_1724.method_5735();
      class_2338 playerPos = mc.field_1724.method_24515();
      class_2338 gapPos = playerPos.method_10093(this.farmFacing);
      class_2338 plantPos = playerPos.method_10079(this.farmFacing, 2);
      class_2680 plantState = mc.field_1687.method_8320(plantPos);
      if (this.isSpaceClear(gapPos) && this.isSpaceClear(gapPos.method_10084())) {
         if (plantState.method_26204() == class_2246.field_10394) {
            this.targetPos = plantPos;
            this.currentState = AppleFarmer.FarmState.BONEMEAL;
            this.timer = 0;
         } else if (this.isFarmLog(plantState)) {
            this.targetPos = plantPos;
            this.currentState = AppleFarmer.FarmState.SCAN_TREE;
            this.timer = 0;
         } else {
            class_2338 soilPos = plantPos.method_10074();
            if (this.isValidSoil(soilPos) && plantState.method_45474()) {
               this.targetPos = soilPos.method_10084();
               this.currentState = AppleFarmer.FarmState.PLACE;
            } else {
               ChatUtils.addChatMessage("§c[AppleFarmer] §fВстаньте напротив места посадки: земля должна быть через один блок перед вами");
               this.toggle();
            }

            this.timer = 0;
         }
      } else {
         ChatUtils.addChatMessage("§c[AppleFarmer] §fМежду вами и местом посадки должен быть свободный блок");
         this.toggle();
         this.timer = 0;
      }
   }

   private void handlePlace() {
      if (this.targetPos == null) {
         this.currentState = AppleFarmer.FarmState.FIND_SPOT;
      } else {
         class_2680 state = mc.field_1687.method_8320(this.targetPos);
         if (state.method_26204() == class_2246.field_10394) {
            this.currentState = AppleFarmer.FarmState.BONEMEAL;
            this.timer = 0;
         } else if (!state.method_45474()) {
            this.currentState = AppleFarmer.FarmState.FIND_SPOT;
            this.timer = 0;
         } else {
            int saplingSlot = this.findItemSlot(class_1802.field_17535);
            if (saplingSlot == -1) {
               ChatUtils.addChatMessage("§c[AppleFarmer] §fНет саженцев");
               this.toggle();
            } else if (this.rotateTo(this.targetPos.method_10074())) {
               int oldSlot = mc.field_1724.method_31548().method_67532();
               mc.field_1724.method_31548().method_61496(saplingSlot);
               this.interactBlock(this.targetPos.method_10074());
               mc.field_1724.method_31548().method_61496(oldSlot);
               this.currentState = AppleFarmer.FarmState.BONEMEAL;
               this.timer = 0;
            }
         }
      }
   }

   private void handleBonemeal() {
      if (this.targetPos != null) {
         class_2680 state = mc.field_1687.method_8320(this.targetPos);
         if (this.isFarmLog(state)) {
            this.currentState = AppleFarmer.FarmState.SCAN_TREE;
         } else if (state.method_45474()) {
            this.currentState = AppleFarmer.FarmState.PLACE;
         } else if (state.method_26204() != class_2246.field_10394) {
            this.currentState = AppleFarmer.FarmState.FIND_SPOT;
         } else {
            int boneMealSlot = this.findItemSlot(class_1802.field_8324);
            if (boneMealSlot == -1) {
               if (!this.pullBoneMealFromInventory()) {
                  ChatUtils.addChatMessage("§c[AppleFarmer] §fНет костной муки");
                  this.toggle();
                  return;
               }

               boneMealSlot = mc.field_1724.method_31548().method_67532();
            }

            if (this.rotateTo(this.targetPos)) {
               int oldSlot = mc.field_1724.method_31548().method_67532();
               mc.field_1724.method_31548().method_61496(boneMealSlot);
               this.interactBlock(this.targetPos);
               mc.field_1724.method_31548().method_61496(oldSlot);
               this.timer = 0;
            }
         }
      }
   }

   private void handleScanTree() {
      this.blocksToMine.clear();
      class_2338 root = this.targetPos;

      for (int x = -4; x <= 4; x++) {
         for (int y = 0; y <= 8; y++) {
            for (int z = -4; z <= 4; z++) {
               class_2338 p = root.method_10069(x, y, z);
               class_2680 s = mc.field_1687.method_8320(p);
               if (this.isFarmTreeBlock(s) && this.isWithinReach(p)) {
                  this.blocksToMine.add(p);
               }
            }
         }
      }

      if (this.blocksToMine.isEmpty()) {
         this.currentState = AppleFarmer.FarmState.PLACE;
      } else {
         this.blocksToMine.sort(this::compareTreeBlocks);
         this.breakingBlock = null;
         this.currentState = AppleFarmer.FarmState.BREAKING;
      }
   }

   private void handleBreaking() {
      this.blocksToMine.removeIf(pos -> !this.isFarmTreeBlock(mc.field_1687.method_8320(pos)) || !this.isWithinReach(pos));
      if (this.blocksToMine.isEmpty()) {
         this.currentState = AppleFarmer.FarmState.PLACE;
         this.breakingBlock = null;
      } else {
         class_2338 currentBlock = this.findNextMineableBlock();
         if (currentBlock == null) {
            this.currentState = AppleFarmer.FarmState.SCAN_TREE;
            this.breakingBlock = null;
            this.timer = 0;
         } else {
            class_2680 state = mc.field_1687.method_8320(currentBlock);
            class_3965 hit = this.raycastTarget(currentBlock);
            if (hit == null) {
               this.breakingBlock = null;
            } else {
               boolean isLeaves = this.isFarmLeaf(state);
               boolean isLog = this.isFarmLog(state);
               if (isLog) {
                  this.switchToBestTool(true);
               } else if (isLeaves) {
                  this.switchToBestTool(false);
               }

               Rotation targetRotation = this.calculateRotation(hit.method_17784());
               RotationProcess.update(targetRotation, 65.0F, 65.0F, 65.0F, 65.0F, 2, 20, false);
               Rotation rotation = new Rotation(mc.field_1724);
               if (!(rotation.getDelta(targetRotation) > 6.0F)) {
                  if (!currentBlock.equals(this.breakingBlock)) {
                     mc.field_1761.method_2910(currentBlock, hit.method_17780());
                     this.breakingBlock = currentBlock;
                  } else {
                     mc.field_1761.method_2902(currentBlock, hit.method_17780());
                  }

                  mc.field_1724.method_6104(class_1268.field_5808);
               }
            }
         }
      }
   }

   private void interactBlock(class_2338 pos) {
      class_243 hitVec = this.getHitVec(pos, class_2350.field_11036);
      class_3965 hit = new class_3965(hitVec, class_2350.field_11036, pos, false);
      mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
      mc.field_1724.method_6104(class_1268.field_5808);
   }

   private boolean rotateTo(class_2338 pos) {
      Rotation targetRotation = this.calculateRotation(this.getHitVec(pos, class_2350.field_11036));
      RotationProcess.update(targetRotation, 65.0F, 65.0F, 65.0F, 65.0F, 2, 20, false);
      return new Rotation(mc.field_1724).getDelta(targetRotation) <= 6.0F;
   }

   private class_243 getHitVec(class_2338 pos, class_2350 side) {
      return new class_243(
         (double)pos.method_10263() + 0.5 + (double)side.method_10148() * 0.5,
         (double)pos.method_10264() + 0.5 + (double)side.method_10164() * 0.5,
         (double)pos.method_10260() + 0.5 + (double)side.method_10165() * 0.5
      );
   }

   private Rotation calculateRotation(class_2338 pos) {
      return this.calculateRotation(new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5));
   }

   private Rotation calculateRotation(class_243 target) {
      if (mc.field_1724 == null) {
         return new Rotation(0.0F, 0.0F);
      } else {
         class_243 eyes = mc.field_1724.method_33571();
         double dx = target.field_1352 - eyes.field_1352;
         double dy = target.field_1351 - eyes.field_1351;
         double dz = target.field_1350 - eyes.field_1350;
         double dist = Math.sqrt(dx * dx + dz * dz);
         float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
         float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
         return new Rotation(yaw, pitch);
      }
   }

   private boolean isValidSoil(class_2338 pos) {
      class_2248 block = mc.field_1687.method_8320(pos).method_26204();
      return block == class_2246.field_10219 || block == class_2246.field_10566 || block == class_2246.field_10253 || block == class_2246.field_10520;
   }

   private boolean isSpaceClear(class_2338 pos) {
      class_2680 state = mc.field_1687.method_8320(pos);
      return state.method_26215() || state.method_45474();
   }

   private boolean isFarmTreeBlock(class_2680 state) {
      return this.isFarmLog(state) || this.isFarmLeaf(state);
   }

   private boolean isFarmLog(class_2680 state) {
      return state.method_26204() == class_2246.field_10431;
   }

   private boolean isFarmLeaf(class_2680 state) {
      return state.method_26204() == class_2246.field_10503;
   }

   private boolean isWithinReach(class_2338 pos) {
      double reach = (double)Math.min(this.range.getValue(), 4.5F);
      return mc.field_1724.method_33571().method_1025(class_243.method_24953(pos)) <= reach * reach;
   }

   private int compareTreeBlocks(class_2338 first, class_2338 second) {
      class_2680 firstState = mc.field_1687.method_8320(first);
      class_2680 secondState = mc.field_1687.method_8320(second);
      int firstGroup = this.getBreakGroup(first, firstState);
      int secondGroup;
      if (firstGroup != (secondGroup = this.getBreakGroup(second, secondState))) {
         return Integer.compare(firstGroup, secondGroup);
      } else if (this.isFarmLog(firstState) && this.isFarmLog(secondState)) {
         return Integer.compare(first.method_10264(), second.method_10264());
      } else {
         int firstDepth = this.getDepthFromPlayerSide(first);
         int secondDepth;
         if (firstDepth != (secondDepth = this.getDepthFromPlayerSide(second))) {
            return Integer.compare(secondDepth, firstDepth);
         } else {
            int yCompare = Integer.compare(first.method_10264(), second.method_10264());
            if (yCompare != 0) {
               return yCompare;
            } else {
               double firstDistance = first.method_19770(mc.field_1724.method_73189());
               double secondDistance = second.method_19770(mc.field_1724.method_73189());
               return Double.compare(firstDistance, secondDistance);
            }
         }
      }
   }

   private int getBreakGroup(class_2338 pos, class_2680 state) {
      if (this.isFarmLog(state)) {
         return 3;
      } else {
         int depth = this.getDepthFromPlayerSide(pos);
         if (depth > 0) {
            return 0;
         } else {
            return depth == 0 ? 1 : 2;
         }
      }
   }

   private int getDepthFromPlayerSide(class_2338 pos) {
      if (this.targetPos == null) {
         return 0;
      } else {
         class_2350 playerSide = this.farmFacing.method_10153();
         int dx = pos.method_10263() - this.targetPos.method_10263();
         int dz = pos.method_10260() - this.targetPos.method_10260();
         return dx * playerSide.method_10148() + dz * playerSide.method_10165();
      }
   }

   private class_2338 findNextMineableBlock() {
      for (class_2338 pos : this.blocksToMine) {
         if (this.raycastTarget(pos) != null) {
            return pos;
         }
      }

      return null;
   }

   private class_3965 raycastTarget(class_2338 pos) {
      class_243 eye = mc.field_1724.method_33571();

      double[] offsets;
      for (double x : offsets = new double[]{0.5, 0.2, 0.8}) {
         for (double y : offsets) {
            for (double z : offsets) {
               class_243 target = new class_243((double)pos.method_10263() + x, (double)pos.method_10264() + y, (double)pos.method_10260() + z);
               class_3965 hit = mc.field_1687.method_17742(new class_3959(eye, target, class_3960.field_17559, class_242.field_1348, mc.field_1724));
               if (hit.method_17783() == class_240.field_1332 && hit.method_17777().equals(pos)) {
                  return hit;
               }
            }
         }
      }

      return null;
   }

   private int findItemSlot(class_1792 item) {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private void switchToBestTool(boolean isAxe) {
      int bestSlot = -1;
      class_1799 current = mc.field_1724.method_6047();
      if (!isAxe || !(current.method_7909() instanceof class_1743)) {
         if (isAxe || !(current.method_7909() instanceof class_1794)) {
            for (int i = 0; i < 9; i++) {
               class_1799 stack = mc.field_1724.method_31548().method_5438(i);
               if (!stack.method_7960()) {
                  if (isAxe && stack.method_7909() instanceof class_1743) {
                     bestSlot = i;
                     break;
                  }

                  if (!isAxe && stack.method_7909() instanceof class_1794) {
                     bestSlot = i;
                     break;
                  }
               }
            }

            if (bestSlot != -1) {
               mc.field_1724.method_31548().method_61496(bestSlot);
            }
         }
      }
   }

   private boolean pullBoneMealFromInventory() {
      for (int i = 9; i < 36; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() == class_1802.field_8324) {
            mc.field_1761
               .method_2906(mc.field_1724.field_7498.field_7763, i, mc.field_1724.method_31548().method_67532(), class_1713.field_7791, mc.field_1724);
            return true;
         }
      }

      return false;
   }

   private void handleAutoRefill() {
      for (int i = 0; i < 9; i++) {
         class_1799 stackInHotbar = mc.field_1724.method_31548().method_5438(i);
         boolean isTargetItem = stackInHotbar.method_7909() == class_1802.field_8324 || stackInHotbar.method_7909() == class_1802.field_17535;
         if (isTargetItem && stackInHotbar.method_7947() < 64) {
            int bestSlot = -1;
            int bestCount = stackInHotbar.method_7947();

            for (int invSlot = 9; invSlot < 36; invSlot++) {
               class_1799 stackInInv = mc.field_1724.method_31548().method_5438(invSlot);
               if (stackInInv.method_7909() == stackInHotbar.method_7909() && stackInInv.method_7947() > bestCount) {
                  bestSlot = invSlot;
                  bestCount = stackInInv.method_7947();
                  if (bestCount == 64) {
                     break;
                  }
               }
            }

            if (bestSlot != -1) {
               mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, bestSlot, i, class_1713.field_7791, mc.field_1724);
               this.refillTimer = 0;
               return;
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum FarmState {
      FIND_SPOT,
      PLACE,
      BONEMEAL,
      SCAN_TREE,
      BREAKING;
   }
}
