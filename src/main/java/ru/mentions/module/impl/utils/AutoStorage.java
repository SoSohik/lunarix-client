package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_476;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.StopGPT;

@ModuleInfo(
   name = "Auto Storage",
   desc = "Автоматически складывает лут в сундук",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class AutoStorage extends Module {
   public final BindSetting bind = new BindSetting(this, "Бинд", -1);
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Только ценные", "Все предметы");
   private AutoStorage.State state = AutoStorage.State.IDLE;
   private final StopGPT timer = new StopGPT();
   public boolean triggeredByChestStealer = false;
   private long nextDelay = 0L;

   @EventHandler
   public void onKey(EventKey e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (e.getKey() == this.bind.get()) {
            this.trigger();
         }
      }
   }

   public void trigger() {
      if (mc.field_1724 != null) {
         if (this.state == AutoStorage.State.IDLE) {
            mc.field_1724.field_3944.method_45730("clan storage");
            this.state = AutoStorage.State.WAITING_FOR_OPEN;
            this.timer.reset();
            this.triggeredByChestStealer = false;
         }
      }
   }

   public boolean isWorking() {
      return this.state != AutoStorage.State.IDLE;
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         switch (this.state) {
            case WAITING_FOR_OPEN:
               if (mc.field_1755 instanceof class_476) {
                  this.state = AutoStorage.State.MOVING_ITEMS;
                  this.timer.reset();
                  this.nextDelay = 0L;
               } else if (this.timer.hasTimePassed(5000L)) {
                  this.state = AutoStorage.State.IDLE;
                  if (this.triggeredByChestStealer) {
                     this.triggeredByChestStealer = false;
                  }
               }
               break;
            case MOVING_ITEMS:
               if (!(mc.field_1755 instanceof class_476 screen)) {
                  this.state = AutoStorage.State.IDLE;
                  return;
               }

               if (!this.timer.hasTimePassed(this.nextDelay)) {
                  return;
               }

               this.processInventory(screen);
               break;
            case CLOSING:
               if (mc.field_1755 != null) {
                  mc.field_1755.method_25419();
                  mc.method_1507(null);
               }

               this.state = AutoStorage.State.IDLE;
               if (this.triggeredByChestStealer) {
                  this.triggeredByChestStealer = false;
               }
         }
      } else {
         this.state = AutoStorage.State.IDLE;
      }
   }

   private void processInventory(class_476 screen) {
      int rows = ((class_1707)screen.method_17577()).method_17388();
      int containerSize = rows * 9;
      int totalSlots = ((class_1707)screen.method_17577()).field_7761.size();
      boolean movedItem = false;

      for (int i = containerSize; i < totalSlots; i++) {
         class_1735 slot = ((class_1707)screen.method_17577()).method_7611(i);
         if (slot.method_7681()) {
            boolean shouldMove = this.mode.is("Все предметы") || this.isValuableItem(slot.method_7677());
            if (shouldMove) {
               mc.field_1761.method_2906(((class_1707)screen.method_17577()).field_7763, i, 0, class_1713.field_7794, mc.field_1724);
               this.nextDelay = (long)MathUtil.random(120.0, 150.0);
               this.timer.reset();
               movedItem = true;
               return;
            }
         }
      }

      if (!movedItem) {
         this.state = AutoStorage.State.CLOSING;
      }
   }

   public boolean isValuableItem(class_1799 stack) {
      if (stack.method_7964().getString().contains("★") && stack != class_1802.field_8107.method_7854()) {
         return true;
      } else {
         class_1792 item = stack.method_7909();
         return item == class_1802.field_8436
            || item == class_1802.field_8712
            || item == class_1802.field_8086
            || item == class_1802.field_8136
            || item == class_1802.field_8477
            || item == class_1802.field_22021
            || item == class_1802.field_22020
            || item == class_1802.field_41946
            || item == class_1802.field_8367
            || item == class_1802.field_8837
            || item == class_1802.field_8449
            || item == class_1802.field_8626
            || item == class_1802.field_8137
            || item == class_1802.field_8463
            || item == class_1802.field_8791
            || item == class_1802.field_8603
            || item == class_1802.field_22022
            || item == class_1802.field_22027
            || item == class_1802.field_22029
            || item == class_1802.field_22028
            || item == class_1802.field_22030
            || item == class_1802.field_8366
            || item == class_1802.field_8668
            || item == class_1802.field_8543
            || item == class_1802.field_8675
            || item == class_1802.field_8288
            || item == class_1802.field_8833
            || item == class_1802.field_38418;
      }
   }

   @Override
   public void onDisable() {
      this.state = AutoStorage.State.IDLE;
      super.onDisable();
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      IDLE,
      WAITING_FOR_OPEN,
      MOVING_ITEMS,
      CLOSING;
   }
}
