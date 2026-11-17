package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_476;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "Auto Resell",
   desc = "Автоматически перепродает предметы",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoResell extends Module {
   private static final int PERIOD_TICKS = 1200;
   private static final int OPEN_TIMEOUT_TICKS = 100;
   private static final int TICK_DELAY = 6;
   private AutoResell.State state = AutoResell.State.IDLE;
   private int cooldownTicks;
   private int waitTicks;
   private int noScreenTicks;

   @Override
   protected void onEnable() {
      this.resetCycle();
   }

   @Override
   protected void onDisable() {
      this.state = AutoResell.State.IDLE;
      this.cooldownTicks = 0;
      this.waitTicks = 0;
      this.noScreenTicks = 0;
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (this.waitTicks > 0) {
            this.waitTicks--;
         } else {
            switch (this.state) {
               case IDLE:
                  this.tickIdle();
                  break;
               case OPEN_AH:
                  this.tickOpenAh();
                  break;
               case CLICK_STORAGE:
                  this.tickClickStorage();
                  break;
               case CLICK_RELIST:
                  this.tickClickRelist();
                  break;
               case CLOSE:
                  this.finishCycle();
            }
         }
      }
   }

   private void tickIdle() {
      if (this.cooldownTicks > 0) {
         this.cooldownTicks--;
      } else {
         this.resetCycle();
      }
   }

   private void resetCycle() {
      if (mc.field_1724 == null) {
         this.state = AutoResell.State.IDLE;
      } else {
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }

         this.sendCommand("ah");
         this.state = AutoResell.State.OPEN_AH;
         this.noScreenTicks = 0;
         this.waitTicks = 20;
      }
   }

   private void tickOpenAh() {
      if (mc.field_1755 instanceof class_476) {
         this.noScreenTicks = 0;
         this.state = AutoResell.State.CLICK_STORAGE;
         this.waitTicks = 2;
      } else if (++this.noScreenTicks >= 100) {
         this.sendCommand("ah");
         this.noScreenTicks = 0;
         this.waitTicks = 20;
      } else {
         this.waitTicks = 6;
      }
   }

   private void tickClickStorage() {
      if (!(mc.field_1755 instanceof class_476 screen)) {
         this.state = AutoResell.State.OPEN_AH;
         this.waitTicks = 6;
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         if (this.findRelistSlot(handler) >= 0) {
            this.state = AutoResell.State.CLICK_RELIST;
            this.waitTicks = 2;
         } else {
            int storage = this.findStorageSlot(handler);
            if (storage < 0) {
               this.waitTicks = 6;
            } else {
               mc.field_1761.method_2906(handler.field_7763, storage, 0, class_1713.field_7790, mc.field_1724);
               this.state = AutoResell.State.CLICK_RELIST;
               this.waitTicks = 8;
            }
         }
      }
   }

   private void tickClickRelist() {
      if (!(mc.field_1755 instanceof class_476 screen)) {
         this.state = AutoResell.State.OPEN_AH;
         this.waitTicks = 6;
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         int relist = this.findRelistSlot(handler);
         if (relist < 0) {
            this.waitTicks = 6;
         } else {
            mc.field_1761.method_2906(handler.field_7763, relist, 0, class_1713.field_7790, mc.field_1724);
            this.state = AutoResell.State.CLOSE;
            this.waitTicks = 6;
         }
      }
   }

   private void finishCycle() {
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      this.state = AutoResell.State.IDLE;
      this.cooldownTicks = 1200;
      this.waitTicks = 0;
      this.noScreenTicks = 0;
   }

   private int findStorageSlot(class_1707 handler) {
      int containerSlots = handler.method_17388() * 9;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681() && slot.method_7677().method_7909() == class_1802.field_8466) {
            return i;
         }
      }

      return this.findTextSlot(handler, "хранилище");
   }

   private int findRelistSlot(class_1707 handler) {
      int containerSlots = handler.method_17388() * 9;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681()) {
            class_1799 stack = slot.method_7677();
            String text = this.stackText(stack);
            if (stack.method_7909() == class_1802.field_8557 && text.contains("перевыстав")) {
               return i;
            }
         }
      }

      return this.findTextSlot(handler, "перевыставить предметы");
   }

   private int findTextSlot(class_1707 handler, String needle) {
      int containerSlots = handler.method_17388() * 9;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681() && this.stackText(slot.method_7677()).contains(needle)) {
            return i;
         }
      }

      return -1;
   }

   private String stackText(class_1799 stack) {
      StringBuilder text = new StringBuilder(this.clean(stack.method_7964().getString()));
      class_9290 lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632);
      if (lore != null) {
         for (class_2561 line : lore.comp_2400()) {
            text.append(' ').append(this.clean(line.getString()));
         }
      }

      return text.toString();
   }

   private String clean(String text) {
      return text == null ? "" : text.replaceAll("§.", "").trim().toLowerCase();
   }

   private void sendCommand(String command) {
      mc.field_1724.field_3944.method_45730(command.startsWith("/") ? command.substring(1) : command);
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      IDLE,
      OPEN_AH,
      CLICK_STORAGE,
      CLICK_RELIST,
      CLOSE;
   }
}
