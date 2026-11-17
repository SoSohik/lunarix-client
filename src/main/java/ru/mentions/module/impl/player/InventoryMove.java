package ru.mentions.module.impl.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2480;
import net.minecraft.class_2596;
import net.minecraft.class_2645;
import net.minecraft.class_2813;
import net.minecraft.class_2815;
import net.minecraft.class_2840;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_476;
import net.minecraft.class_490;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.ScreenCloseEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.impl.combat.PotionWheelScreen;
import ru.mentions.module.impl.combat.TriplePickerScreen;
import ru.mentions.module.impl.combat.TripleWheelScreen;
import ru.mentions.module.impl.utils.ReportHelper;
import ru.mentions.screen.ClickGuiScreen;
import ru.mentions.utils.other.TimerUtil;
import ru.mentions.utils.player.MoveUtil;

@ModuleInfo(
   name = "Inventory Move",
   desc = "Позволяет двигаться с открытым инвентарём",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class InventoryMove extends Module {
   public final ModeSetting mode = new ModeSetting(this, "Тип", "SpookyTime", "SpaceTime", "FunTime", "Vanilla");
   public final ModeSetting spaceTimeSpeed = new ModeSetting(this, "Обход перемещения", "Ускоренный", "Медленный").setVisible(() -> this.mode.is("SpaceTime"));
   private final List<InventoryMove.QueuedPacket> spaceTimeQueue = new ArrayList<>();
   private int spaceTimeFlushDelay;
   private long suppressSyncUntil;
   private final List<class_2596<?>> packetQueue = new ArrayList<>();
   private final Set<class_2596<?>> sendingPackets = new HashSet<>();
   private final Queue<class_2813> windowClickPacketQueue = new LinkedList<>();
   private final TimerUtil timer = new TimerUtil();
   public boolean stop = false;
   private boolean pendingClose = false;
   private long closeTime = 0L;
   private int stopTicksOut;
   private boolean stoppedStatus;
   private boolean previousStoppedStatus;
   public int ticksPostOnStop = 1;

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null) {
         if (this.mode.is("SpaceTime")) {
            boolean suppressed = System.currentTimeMillis() < this.suppressSyncUntil;
            if (!suppressed && this.isWalkableScreen(mc.field_1755)) {
               this.syncMovementKeys();
            }

            if (!this.isMoving() && !this.spaceTimeQueue.isEmpty()) {
               if (this.spaceTimeSpeed.is("Медленный")) {
                  InventoryMove.QueuedPacket qp = this.spaceTimeQueue.remove(0);
                  this.sendPacketWithoutEvent(qp.packet());
               } else {
                  for (InventoryMove.QueuedPacket qp : this.spaceTimeQueue) {
                     this.sendPacketWithoutEvent(qp.packet());
                  }

                  this.spaceTimeQueue.clear();
               }

               if (this.spaceTimeQueue.isEmpty()) {
                  this.spaceTimeFlushDelay--;
                  if (this.spaceTimeFlushDelay <= 0 && mc.field_1755 == null && mc.field_1724 != null) {
                     this.sendPacketWithoutEvent(new class_2815(mc.field_1724.field_7512.field_7763));
                  }
               }
            }
         } else {
            if (this.mode.is("Vanilla") && !ReportHelper.isAutoListActive()) {
               class_304[] movementKeys = new class_304[]{
                  mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
               };
               if (!(mc.field_1755 instanceof class_408)
                  && (
                     mc.field_1755 instanceof class_490
                        || mc.field_1755 instanceof TripleWheelScreen
                        || mc.field_1755 instanceof PotionWheelScreen
                        || mc.field_1755 instanceof ClickGuiScreen gui && !gui.isSearchActive()
                        || mc.field_1755 instanceof TriplePickerScreen
                        || mc.field_1755 instanceof class_476
                  )) {
                  this.updateKeyBindingState(movementKeys);
               }
            }

            if (this.mode.is("FunTime_2")) {
               if (this.previousStoppedStatus && this.stoppedStatus && this.stopTicksOut > 0) {
                  this.useAccumulatedPackets();
               }

               this.previousStoppedStatus = this.stoppedStatus;
               if (this.stopTicksOut > 0) {
                  this.stopTicksOut--;
               }

               this.stoppedStatus = this.stopTicksOut > 0;
               this.ticksPostOnStop++;
               if (this.stoppedStatus) {
                  this.ticksPostOnStop = 0;
               }
            }

            if (this.pendingClose && System.currentTimeMillis() >= this.closeTime) {
               this.pendingClose = false;

               for (class_2596<?> p : this.packetQueue) {
                  this.sendPacketWithoutEvent(p);
               }

               this.packetQueue.clear();
               if (mc.field_1724 != null) {
                  mc.execute(() -> {
                     if (mc.field_1724 != null) {
                        this.setKey(true);
                        mc.field_1724.method_7346();
                     }
                  });
               }
            }

            this.handleBypassMode();
         }
      }
   }

   public boolean isMoving() {
      return mc.field_1724 == null ? false : MoveUtil.isMoving() || mc.field_1690.field_1903.method_1434();
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      if (!(e.getPacket() instanceof class_2840)) {
         if (!ReportHelper.isAutoListActive()) {
            class_2596<?> rawPacket = e.getPacket();
            if (this.sendingPackets.contains(rawPacket)) {
               this.sendingPackets.remove(rawPacket);
            } else if (this.mode.is("SpaceTime")) {
               if (e.isSend()) {
                  boolean containerOpen = mc.field_1755 instanceof class_465;
                  boolean moving = this.isMoving();
                  if (rawPacket instanceof class_2813 click && containerOpen && moving) {
                     boolean shulker = click.comp_3845() == 1 && this.isShulker(click);
                     if (shulker) {
                        this.spaceTimeFlushDelay = Math.max(this.spaceTimeFlushDelay, 2);
                     }

                     int delay = this.spaceTimeSpeed.is("Медленный") && !this.spaceTimeQueue.isEmpty() ? this.spaceTimeQueue.size() + 1 : 2;
                     this.spaceTimeQueue.add(new InventoryMove.QueuedPacket(click, delay, shulker));
                     e.cancel();
                     return;
                  }

                  if (rawPacket instanceof class_2815 close && containerOpen && moving) {
                     e.cancel();
                     this.spaceTimeFlushDelay = Math.max(
                        this.spaceTimeFlushDelay, this.spaceTimeQueue.stream().mapToInt(InventoryMove.QueuedPacket::delayedTicks).max().orElse(1)
                     );
                     if (this.spaceTimeQueue.isEmpty()) {
                        this.spaceTimeQueue.add(new InventoryMove.QueuedPacket(close, this.spaceTimeFlushDelay, false));
                     }

                     return;
                  }
               } else if (this.spaceTimeSpeed.is("Медленный") && rawPacket instanceof class_2645) {
                  this.suppressSyncUntil = System.currentTimeMillis() + 150L;
               }
            } else if (rawPacket instanceof class_2645) {
               if (this.mode.is("FunTime_2")) {
                  e.cancel();
               }
            } else if (rawPacket instanceof class_2813 packet) {
               boolean isTargetScreen = mc.field_1755 instanceof class_490 || mc.field_1755 instanceof class_476;
               if (e.isSend() && isTargetScreen) {
                  if (this.mode.is("FunTime_2") && this.canStoppingOnWindowClick() && packet.comp_3844() != -1) {
                     this.setStop(packet.comp_3846());
                     if (!this.stoppedStatus) {
                        if (!this.windowClickPacketQueue.contains(packet)) {
                           this.windowClickPacketQueue.add(packet);
                        }

                        this.ticksPostOnStop = 0;
                        e.cancel();
                     }
                  }

                  if (this.mode.is("FunTime") && (this.isMoving() || mc.field_1690.field_1903.method_1434())) {
                     this.packetQueue.add(packet);
                     e.cancel();
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onClose(ScreenCloseEvent e) {
      if (!ReportHelper.isAutoListActive()) {
         if (this.mode.is("SpaceTime")) {
            if (this.isMoving() && mc.field_1755 instanceof class_465) {
               e.cancel();
               this.spaceTimeFlushDelay = Math.max(this.spaceTimeFlushDelay, 1);
            }
         } else {
            if (mc.field_1755 instanceof class_490 || mc.field_1755 instanceof class_476) {
               if (this.mode.is("FunTime")) {
                  e.cancel();
                  this.timer.reset();
                  this.closeTime = System.currentTimeMillis() + 60L;
                  this.pendingClose = true;
               }

               if (this.mode.is("FunTime_2")) {
                  e.cancel();
                  this.timer.reset();
                  this.closeTime = System.currentTimeMillis() + 50L;
                  this.pendingClose = true;
               }
            }
         }
      }
   }

   private void handleBypassMode() {
      class_304[] movementKeys = new class_304[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      };
      if (ReportHelper.isAutoListActive()) {
         this.setKey(false);
      } else if (!this.timer.isReached(60L)) {
         this.setKey(false);
      } else if (this.mode.is("FunTime_2") && this.stoppedStatus) {
         this.setKey(false);
      } else {
         if (!(mc.field_1755 instanceof class_408)
            && (
               mc.field_1755 instanceof class_490
                  || mc.field_1755 instanceof TripleWheelScreen
                  || mc.field_1755 instanceof PotionWheelScreen
                  || mc.field_1755 instanceof ClickGuiScreen gui2 && !gui2.isSearchActive()
                  || mc.field_1755 instanceof TriplePickerScreen
                  || mc.field_1755 instanceof class_476
            )) {
            this.updateKeyBindingState(movementKeys);
         }
      }
   }

   private void updateKeyBindingState(class_304[] keyBindings) {
      for (class_304 keyBinding : keyBindings) {
         int keyCode = keyBinding.method_1429().method_1444();
         boolean isKeyPressed = class_3675.method_15987(mc.method_22683(), keyCode);
         keyBinding.method_23481(isKeyPressed);
      }
   }

   private void syncMovementKeys() {
      if (mc.field_1755 != null && mc.method_22683() != null) {
         class_304[] movementKeys = new class_304[]{
            mc.field_1690.field_1894,
            mc.field_1690.field_1881,
            mc.field_1690.field_1913,
            mc.field_1690.field_1849,
            mc.field_1690.field_1903,
            mc.field_1690.field_1867
         };

         for (class_304 keyBinding : movementKeys) {
            int keyCode = keyBinding.method_1429().method_1444();
            boolean isKeyPressed = class_3675.method_15987(mc.method_22683(), keyCode);
            keyBinding.method_23481(isKeyPressed);
         }
      }
   }

   private boolean isWalkableScreen(class_437 screen) {
      if (screen == null) {
         return false;
      } else if (screen instanceof class_408) {
         return false;
      } else {
         if (screen instanceof ClickGuiScreen gui && gui.isSearchActive()) {
            return false;
         }

         String name = screen.getClass().getSimpleName();
         return !name.contains("Sign") && !name.contains("Anvil") && !name.contains("CommandBlock") && !name.contains("StructureBlock")
            ? screen instanceof class_465
               || screen instanceof ClickGuiScreen
               || screen instanceof TripleWheelScreen
               || screen instanceof PotionWheelScreen
               || screen instanceof TriplePickerScreen
            : false;
      }
   }

   private boolean isShulker(class_2813 click) {
      if (mc.field_1724 != null && mc.field_1724.field_7512 != null) {
         try {
            int slot = click.comp_3844();
            if (slot >= 0 && slot < mc.field_1724.field_7512.field_7761.size()) {
               class_1735 slotObj = mc.field_1724.field_7512.method_7611(slot);
               if (slotObj != null && slotObj.method_7677() != null) {
                  class_1799 stack = slotObj.method_7677();
                  if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2480) {
                     return true;
                  }
               }
            }

            class_1799 cursorStack = mc.field_1724.field_7512.method_34255();
            if (cursorStack != null && cursorStack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2480) {
               return true;
            }
         } catch (Exception var7) {
         }

         return false;
      } else {
         return false;
      }
   }

   private void sendPacketWithoutEvent(class_2596<?> packet) {
      if (packet != null && mc.method_1562() != null) {
         this.sendingPackets.add(packet);
         mc.method_1562().method_48296().method_10752(packet, null);
      }
   }

   private void setKey(boolean state) {
      class_304[] movementKeys = new class_304[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      };

      for (class_304 keyBinding : movementKeys) {
         boolean pressed = state && class_3675.method_15987(mc.method_22683(), keyBinding.method_1429().method_1444());
         keyBinding.method_23481(pressed);
      }
   }

   private boolean canStoppingOnWindowClick() {
      return this.isMoving() || mc.field_1690.field_1903.method_1434() || this.stoppedStatus;
   }

   private void setStop(class_1713 actionType) {
      this.stopTicksOut = this.ticksWindowClickOffset(actionType == null ? class_1713.field_7790 : actionType) + 1;
   }

   private int ticksWindowClickOffset(class_1713 actionType) {
      return 1;
   }

   private void useAccumulatedPackets() {
      if (!this.windowClickPacketQueue.isEmpty()) {
         while (!this.windowClickPacketQueue.isEmpty()) {
            class_2813 packet = this.windowClickPacketQueue.poll();
            if (packet != null) {
               this.sendPacketWithoutEvent(packet);
            }
         }

         this.ticksPostOnStop = 0;
      }
   }

   @Override
   public void onDisable() {
      if (mc.field_1724 != null && mc.field_1755 == null) {
         for (InventoryMove.QueuedPacket qp : this.spaceTimeQueue) {
            this.sendPacketWithoutEvent(qp.packet());
         }
      }

      this.spaceTimeQueue.clear();
      this.spaceTimeFlushDelay = 0;
      this.suppressSyncUntil = 0L;
      this.stop = false;
      this.pendingClose = false;
      this.useAccumulatedPackets();
      this.ticksPostOnStop = 1;
      this.packetQueue.clear();
      this.windowClickPacketQueue.clear();
      this.sendingPackets.clear();
      this.setKey(false);
      super.onDisable();
   }

   @Override
   public void onEnable() {
      this.spaceTimeQueue.clear();
      this.spaceTimeFlushDelay = 0;
      this.suppressSyncUntil = 0L;
      this.useAccumulatedPackets();
      this.windowClickPacketQueue.clear();
      this.ticksPostOnStop = 1;
   }

   public static record QueuedPacket(class_2596<?> packet, int delayedTicks, boolean shulker) {
   }
}
