package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_2815;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.math.StopGPT;

@ModuleInfo(
   name = "Auto Tool",
   desc = "Автоматически выбирает лучший инструмент",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class AutoTool extends Module {
   private static final long SWAP_STOP_MS = 60L;
   private AutoTool.State state = AutoTool.State.IDLE;
   private final StopGPT timer = new StopGPT();
   private int toolSlot = -1;
   private int previousHotbarSlot = -1;

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1761 == null) {
         this.state = AutoTool.State.IDLE;
         this.toolSlot = -1;
         this.previousHotbarSlot = -1;
      } else if (mc.field_1755 == null) {
         switch (this.state) {
            case IDLE:
               this.tryStart();
               break;
            case PREPARE_SWAP:
               this.prepareSwap();
               break;
            case MINING:
               this.continueMining();
               break;
            case PREPARE_RESTORE:
               this.prepareRestore();
         }
      }
   }

   @Override
   public void onDisable() {
      this.abort();
      super.onDisable();
   }

   private void tryStart() {
      class_2680 blockState = this.getTargetState();
      if (blockState != null && mc.field_1690.field_1886.method_1434()) {
         int bestSlot = this.findBestTool(blockState);
         int selectedSlot = mc.field_1724.method_31548().method_67532();
         if (bestSlot != -1 && bestSlot != selectedSlot) {
            this.toolSlot = bestSlot;
            this.previousHotbarSlot = selectedSlot;
            if (bestSlot < 9) {
               mc.field_1724.method_31548().method_61496(bestSlot);
               ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
               this.state = AutoTool.State.MINING;
            } else {
               this.timer.reset();
               this.state = AutoTool.State.PREPARE_SWAP;
            }
         }
      }
   }

   private void prepareSwap() {
      if (mc.field_1690.field_1886.method_1434() && this.getTargetState() != null) {
         this.setKey(false);
         if (this.timer.hasTimePassed(60L)) {
            this.setKey(true);
            this.swapInventoryTool();
            this.state = AutoTool.State.MINING;
         }
      } else {
         this.reset();
      }
   }

   private void continueMining() {
      class_2680 blockState = this.getTargetState();
      if (mc.field_1690.field_1886.method_1434() && blockState != null) {
         int bestSlot;
         if (this.toolSlot < 9 && (bestSlot = this.findBestTool(blockState)) != -1 && bestSlot < 9 && bestSlot != mc.field_1724.method_31548().method_67532()) {
            this.toolSlot = bestSlot;
            mc.field_1724.method_31548().method_61496(bestSlot);
            ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
         }
      } else {
         if (this.toolSlot < 9) {
            this.restoreSelectedSlot();
            this.reset();
         } else {
            this.timer.reset();
            this.state = AutoTool.State.PREPARE_RESTORE;
         }
      }
   }

   private void prepareRestore() {
      this.setKey(false);
      if (this.timer.hasTimePassed(60L)) {
         this.swapInventoryTool();
         this.closeInventorySilent();
         this.reset();
      }
   }

   private void swapInventoryTool() {
      if (this.toolSlot >= 9 && this.previousHotbarSlot >= 0 && this.previousHotbarSlot <= 8) {
         mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.toolSlot, this.previousHotbarSlot, class_1713.field_7791, mc.field_1724);
      }
   }

   private void closeInventorySilent() {
      mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
      mc.field_1724.method_7346();
   }

   private void restoreSelectedSlot() {
      if (this.previousHotbarSlot >= 0 && this.previousHotbarSlot <= 8) {
         mc.field_1724.method_31548().method_61496(this.previousHotbarSlot);
         ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
      }
   }

   private class_2680 getTargetState() {
      class_239 class_2392 = mc.field_1765;
      class_3965 hit;
      return class_2392 instanceof class_3965 && (hit = (class_3965)class_2392).method_17783() == class_240.field_1332
         ? mc.field_1687.method_8320(hit.method_17777())
         : null;
   }

   private int findBestTool(class_2680 blockState) {
      int selectedSlot = mc.field_1724.method_31548().method_67532();
      int bestSlot = this.isUsable(mc.field_1724.method_31548().method_5438(selectedSlot)) ? selectedSlot : -1;
      float bestSpeed = bestSlot == -1 ? 1.0F : mc.field_1724.method_31548().method_5438(bestSlot).method_7924(blockState);
      boolean bestCanHarvest = bestSlot != -1 && (!blockState.method_29291() || mc.field_1724.method_31548().method_5438(bestSlot).method_7951(blockState));

      for (int slot = 0; slot < 36; slot++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (this.isUsable(stack)) {
            float speed = stack.method_7924(blockState);
            boolean canHarvest = !blockState.method_29291() || stack.method_7951(blockState);
            if (canHarvest && !bestCanHarvest || canHarvest == bestCanHarvest && speed > bestSpeed) {
               bestSlot = slot;
               bestSpeed = speed;
               bestCanHarvest = canHarvest;
            }
         }
      }

      return bestSlot;
   }

   private boolean isUsable(class_1799 stack) {
      return !stack.method_7960() && (!stack.method_7963() || stack.method_7936() - stack.method_7919() > 1);
   }

   private void reset() {
      this.setKey(true);
      this.state = AutoTool.State.IDLE;
      this.toolSlot = -1;
      this.previousHotbarSlot = -1;
   }

   private void abort() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.state == AutoTool.State.MINING || this.state == AutoTool.State.PREPARE_RESTORE) {
            if (this.toolSlot >= 9) {
               this.swapInventoryTool();
               this.closeInventorySilent();
            } else {
               this.restoreSelectedSlot();
            }
         }

         this.setKey(true);
      }

      this.state = AutoTool.State.IDLE;
      this.toolSlot = -1;
      this.previousHotbarSlot = -1;
   }

   private void setKey(boolean state) {
      for (class_304 keyBinding : new class_304[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      }) {
         boolean pressed = state && class_3675.method_15987(mc.method_22683(), keyBinding.method_1429().method_1444());
         keyBinding.method_23481(pressed);
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      IDLE,
      PREPARE_SWAP,
      MINING,
      PREPARE_RESTORE;
   }
}
