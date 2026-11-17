package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2815;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.impl.render.SwingAnimation;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.math.StopGPT;
import ru.mentions.utils.math.StopWatchP;

@ModuleInfo(
   name = "Elytra Helper",
   desc = "авто-свап и полёт",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class ElytraHelper extends Module {
   public final BindSetting swapChestKey = new BindSetting(this, "Свап");
   public final BindSetting use = new BindSetting(this, "Исп. фейерверк");
   public final BooleanSetting autoFly = new BooleanSetting(this, "AutoFly", true);
   public final BooleanSetting autoPvpSwap = new BooleanSetting(this, "AutoPvpSwap", false);
   private ElytraHelper.SwapState chestSwapState = ElytraHelper.SwapState.IDLE;
   private final StopGPT chestTimer = new StopGPT();
   public static StopWatchP stopWatch = new StopWatchP();
   private class_1799 currentStack = class_1799.field_8037;
   private boolean pendingFly = false;
   private final StopGPT flyTimer = new StopGPT();
   private class_1792 prevChestItem = class_1802.field_8162;
   private boolean wasPvp = false;
   private boolean pvpJustEnded = false;
   private long pvpEndTime = 0L;
   private int pearlSlot = -1;
   private int oldSlot = -1;
   private ElytraHelper.UseState currentState = ElytraHelper.UseState.IDLE;
   private final StopGPT timer = new StopGPT();

   public boolean isPvpJustEnded() {
      if (this.pvpJustEnded && System.currentTimeMillis() - this.pvpEndTime < 3000L) {
         return true;
      } else {
         this.pvpJustEnded = false;
         return false;
      }
   }

   public void clearPvpEnded() {
      this.pvpJustEnded = false;
   }

   @EventHandler
   private void onEventKey(EventKey e) {
      if (mc.field_1724 != null && mc.field_1755 == null) {
         int boundSwap = this.swapChestKey.get() != null ? this.swapChestKey.get() : -1;
         int boundUse = this.use.get() != null ? this.use.get() : -1;
         if (boundSwap > 0 && e.getKey() == boundSwap) {
            if (this.chestSwapState == ElytraHelper.SwapState.IDLE) {
               this.chestSwapState = ElytraHelper.SwapState.SWAPPING;
               this.chestTimer.reset();
               stopWatch.reset();
            }
         } else if (boundUse > 0 && e.getKey() == boundUse && !mc.field_1724.method_24828() && this.currentState == ElytraHelper.UseState.IDLE) {
            this.startPearlSwap();
         }
      }
   }

   @EventHandler
   private void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null) {
         class_1799 currentChestStack = mc.field_1724.method_6118(class_1304.field_6174);
         class_1792 currentItem = currentChestStack.method_7909();
         if (currentItem == class_1802.field_8833 && this.prevChestItem != class_1802.field_8833 && this.autoFly.getValue()) {
            this.pendingFly = true;
            this.flyTimer.reset();
         }

         this.prevChestItem = currentItem;
         boolean currentPvp = ServerUtil.isPvp();
         if (this.wasPvp && !currentPvp) {
            this.pvpJustEnded = true;
            this.pvpEndTime = System.currentTimeMillis();
         }

         this.wasPvp = currentPvp;
         if (this.autoPvpSwap.getValue() && this.chestSwapState == ElytraHelper.SwapState.IDLE && currentItem == class_1802.field_8833) {
            boolean shouldSwap = false;
            if (ServerUtil.isPvp()) {
               shouldSwap = true;
            }

            if (!shouldSwap && mc.field_1687 != null) {
               for (class_1657 player : mc.field_1687.method_18456()) {
                  if (player != mc.field_1724
                     && player.method_5805()
                     && !Client.get().friendManager().isFriend(player.method_5477().getString())
                     && (double)mc.field_1724.method_5739(player) <= 5.0) {
                     shouldSwap = true;
                     break;
                  }
               }
            }

            if (shouldSwap && this.getChestPlateSlot() >= 0) {
               this.chestSwapState = ElytraHelper.SwapState.SWAPPING;
               this.chestTimer.reset();
               stopWatch.reset();
            }
         }

         this.handleChestSwap();
         if (this.pendingFly && this.flyTimer.hasTimePassed(400L)) {
            if (this.autoFly.getValue()) {
               mc.field_1724.field_3944.method_45730("fly");
            }

            this.pendingFly = false;
         }

         if (mc.field_1761 != null && this.currentState != ElytraHelper.UseState.IDLE) {
            this.setKey(false);
            switch (this.currentState) {
               case PREPARE:
                  if (this.timer.hasTimePassed(50L)) {
                     this.timer.reset();
                     this.currentState = ElytraHelper.UseState.SWAP;
                  }
                  break;
               case SWAP:
                  if (this.pearlSlot < 9) {
                     mc.field_1724.method_31548().method_61496(this.pearlSlot);
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                  } else if (!mc.field_1724.method_5624()) {
                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.pearlSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                  }

                  if (this.timer.hasTimePassed(30L)) {
                     this.timer.reset();
                     this.currentState = ElytraHelper.UseState.THROW;
                  }
                  break;
               case THROW:
                  SwingAnimation.interactItem(class_1268.field_5808);
                  if (this.timer.hasTimePassed(30L)) {
                     this.timer.reset();
                     this.currentState = ElytraHelper.UseState.RESTORE;
                  }
                  break;
               case RESTORE:
                  if (this.pearlSlot < 9) {
                     mc.field_1724.method_31548().method_61496(this.oldSlot);
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                  } else {
                     if (!mc.field_1724.method_5624()) {
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.pearlSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                     }

                     mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                  }

                  this.timer.reset();
                  this.currentState = ElytraHelper.UseState.COOLDOWN;
                  break;
               case COOLDOWN:
                  if (this.timer.hasTimePassed(70L)) {
                     this.setKey(true);
                     this.currentState = ElytraHelper.UseState.IDLE;
                  }
            }
         }
      }
   }

   private void startPearlSwap() {
      if (mc.field_1724 != null) {
         this.pearlSlot = -1;

         for (int i = 0; i < 36; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() == class_1802.field_8639) {
               this.pearlSlot = i;
               break;
            }
         }

         if (this.pearlSlot == -1) {
            mc.field_1724.method_7353(class_2561.method_43470("Фейерверк не найден!").method_27692(class_124.field_1061), true);
         } else {
            this.oldSlot = mc.field_1724.method_31548().method_67532();
            this.timer.reset();
            this.currentState = ElytraHelper.UseState.PREPARE;
         }
      }
   }

   private void handleChestSwap() {
      if (this.chestSwapState != ElytraHelper.SwapState.IDLE) {
         if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1755 == null) {
            this.setKey(false);
            switch (this.chestSwapState) {
               case SWAPPING:
                  if (!this.chestTimer.hasTimePassed(50L)) {
                     return;
                  }

                  class_1799 chestStack = mc.field_1724.method_6118(class_1304.field_6174);
                  boolean isWearingElytra = chestStack.method_7909() == class_1802.field_8833;
                  int targetSlot = isWearingElytra ? this.getChestPlateSlot() : this.getItemSlot(class_1802.field_8833);
                  if (targetSlot >= 0) {
                     this.swapToChestSlot(targetSlot, 6);
                     this.chestTimer.reset();
                     this.chestSwapState = ElytraHelper.SwapState.CLOSING;
                  } else {
                     this.chestSwapState = ElytraHelper.SwapState.IDLE;
                     this.setKey(true);
                  }
                  break;
               case CLOSING:
                  if (!this.chestTimer.hasTimePassed(70L)) {
                     return;
                  }

                  mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                  if (mc.field_1724.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833) {
                     this.pendingFly = true;
                     this.flyTimer.reset();
                  }

                  this.chestSwapState = ElytraHelper.SwapState.IDLE;
                  this.setKey(true);
                  break;
               default:
                  this.chestSwapState = ElytraHelper.SwapState.IDLE;
                  this.setKey(true);
            }
         } else {
            this.chestSwapState = ElytraHelper.SwapState.IDLE;
            this.setKey(true);
         }
      }
   }

   private void swapToChestSlot(int slot, int chestSlot) {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1755 == null) {
         int syncId = mc.field_1724.field_7498.field_7763;
         int handlerSlot = slot < 9 ? slot + 36 : slot;
         mc.field_1761.method_2906(syncId, chestSlot, 0, class_1713.field_7790, mc.field_1724);
         mc.field_1761.method_2906(syncId, handlerSlot, 0, class_1713.field_7790, mc.field_1724);
         mc.field_1761.method_2906(syncId, chestSlot, 0, class_1713.field_7790, mc.field_1724);
      }
   }

   private int getChestPlateSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         class_1792[] items = new class_1792[]{
            class_1802.field_22028, class_1802.field_8058, class_1802.field_8873, class_1802.field_8678, class_1802.field_8523, class_1802.field_8577
         };

         for (class_1792 item : items) {
            for (int i = 0; i < 36; i++) {
               class_1792 stack = mc.field_1724.method_31548().method_5438(i).method_7909();
               if (stack == item) {
                  return i;
               }
            }
         }

         return -1;
      }
   }

   private int getItemSlot(class_1792 input) {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 36; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960()
               && stack.method_7909() == input
               && (input != class_1802.field_8833 || stack.method_7919() < stack.method_7936() - 1 || stack.method_7936() <= 0)) {
               return i;
            }
         }

         return -1;
      }
   }

   private void setKey(boolean state) {
      if (mc != null && mc.field_1690 != null && mc.method_22683() != null) {
         class_304[] movementKeys = new class_304[]{
            mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
         };

         for (class_304 keyBinding : movementKeys) {
            boolean pressed = state && class_3675.method_15987(mc.method_22683(), keyBinding.method_1429().method_1444());
            keyBinding.method_23481(pressed);
         }
      }
   }

   @Override
   public void onDisable() {
      stopWatch.reset();
      if (this.currentState != ElytraHelper.UseState.IDLE || this.chestSwapState != ElytraHelper.SwapState.IDLE) {
         this.setKey(true);
      }

      this.currentState = ElytraHelper.UseState.IDLE;
      this.chestSwapState = ElytraHelper.SwapState.IDLE;
      super.onDisable();
   }

   @Environment(EnvType.CLIENT)
   public static enum SwapState {
      IDLE,
      SWAPPING,
      SWAPPED,
      USING,
      RETURNING,
      CLOSING;
   }

   @Environment(EnvType.CLIENT)
   private static enum UseState {
      IDLE,
      PREPARE,
      SWAP,
      THROW,
      RESTORE,
      COOLDOWN;
   }
}
