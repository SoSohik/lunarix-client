package ru.mentions.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1839;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_241;
import net.minecraft.class_2680;
import net.minecraft.class_2815;
import net.minecraft.class_2886;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_7204;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.UsingItemEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.math.StopWatchShadow;

@ModuleInfo(
   name = "No Slow",
   category = Category.MOVEMENT,
   desc = "Позволяет использовать придметы без замедления"
)
@Environment(EnvType.CLIENT)
public class NoSlow extends Module {
   public ModeSetting type = new ModeSetting(this, "Режим", "SpookyTime", "ФанТайм", "Грим", "Тики", "Обычный");
   private int ticks = 0;
   private int cycleCounter = 0;
   private boolean crossbowSwapped = false;
   private int savedCrossbowSlot = -1;
   private boolean wasPressingUseWithFood = false;
   private final StopWatchShadow swapWatch = new StopWatchShadow();
   private boolean bypassActive = false;
   private boolean bypassSwapped = false;
   private int pendingSwapSlot = -1;
   private boolean pendingIsRestore = false;

   @Override
   public void onDisable() {
      if (this.crossbowSwapped && mc.field_1724 != null) {
         this.swapSlotWithOffhand(this.savedCrossbowSlot);
         mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7512.field_7763));
      }

      this.setKey(true);
      this.crossbowSwapped = false;
      this.savedCrossbowSlot = -1;
      this.wasPressingUseWithFood = false;
      this.bypassActive = false;
      this.bypassSwapped = false;
      this.pendingSwapSlot = -1;
   }

   private int findCrossbowInInventory() {
      for (int i = 0; i < 9; i++) {
         if (((class_1799)mc.field_1724.method_31548().method_67533().get(i)).method_7909() instanceof class_1764) {
            return i;
         }
      }

      for (int var2 = 9; var2 < 36; var2++) {
         if (((class_1799)mc.field_1724.method_31548().method_67533().get(var2)).method_7909() instanceof class_1764) {
            return var2;
         }
      }

      return -1;
   }

   private void swapSlotWithOffhand(int inventoryMainSlot) {
      int screenSlot = inventoryMainSlot < 9 ? 36 + inventoryMainSlot : inventoryMainSlot;
      if (!mc.field_1724.method_5624()) {
         mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, screenSlot, 40, class_1713.field_7791, mc.field_1724);
      }
   }

   private void setKey(boolean state) {
      for (class_304 key : new class_304[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      }) {
         boolean pressed = state && class_3675.method_15987(mc.method_22683(), key.method_1429().method_1444());
         key.method_23481(pressed);
      }
   }

   private void triggerSwap(int slot, boolean isRestore) {
      this.pendingSwapSlot = slot;
      this.pendingIsRestore = isRestore;
      this.bypassActive = true;
      this.bypassSwapped = false;
      this.setKey(false);
      this.swapWatch.reset();
   }

   @EventHandler
   public void onSlow(UsingItemEvent e) {
      class_1268 first = mc.field_1724.method_6058();
      class_1268 second = first.equals(class_1268.field_5808) ? class_1268.field_5810 : class_1268.field_5808;
      switch (e.getType()) {
         case 1:
            if (this.type.is("Обычный")) {
               e.cancel();
            }

            if (this.type.is("Тики") && this.ticks >= new int[]{2, 2, 2}[this.cycleCounter % 2]) {
               e.cancel();
               this.ticks = 0;
               this.cycleCounter++;
            }

            if (this.type.is("ФанТайм") && (float)this.ticks > 0.0F && (float)mc.field_1724.method_6048() > 1.0F) {
               boolean mainHandCrossbow = mc.field_1724.method_6047().method_7909() instanceof class_1764;
               boolean offHandCrossbow = mc.field_1724.method_6079().method_7909() instanceof class_1764;
               class_2338 feetPos = new class_2338(
                  (int)Math.floor(mc.field_1724.method_23317()), (int)Math.floor(mc.field_1724.method_23318()), (int)Math.floor(mc.field_1724.method_23321())
               );
               class_2680 blockState = mc.field_1687.method_8320(feetPos);
               class_2248 block = blockState.method_26204();
               if (block == class_2246.field_10477 && mc.field_1724.method_24828()) {
                  e.cancel();
               }

               if (mainHandCrossbow || offHandCrossbow) {
                  e.cancel();
               }
            }

            if (this.type.is("Грим")
               && (
                  mc.field_1724.method_6079().method_7976().equals(class_1839.field_8952)
                     || mc.field_1724.method_6047().method_7976().equals(class_1839.field_8952)
               )) {
               this.interactItem(first);
               this.interactItem(second);
               e.cancel();
            }
      }
   }

   public void sendSequencedPacket(class_7204 packetCreator) {
      ClientInteractionAccess.sendSequencedPacket(mc.field_1761, mc.field_1687, packetCreator);
   }

   public void interactItem(class_1268 hand) {
      this.interactItem(hand, new class_241(mc.field_1724.method_36454(), mc.field_1724.method_36455()));
   }

   public void interactItem(class_1268 hand, class_241 angle) {
      this.sendSequencedPacket(i -> new class_2886(hand, i, angle.field_1343, angle.field_1342));
   }

   @EventHandler
   public void onUpdate(EventTick event) {
      if (!mc.field_1724.method_6123()) {
         if (mc.field_1724.method_6115()) {
            this.ticks++;
         } else {
            this.ticks = 0;
            this.cycleCounter = 0;
         }
      }

      if (this.type.is("ФанТайм")) {
      }
   }
}
