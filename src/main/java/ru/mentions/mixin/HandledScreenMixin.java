package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_465;
import net.minecraft.class_490;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.inventorypreset.InventoryPreset;
import ru.mentions.inventorypreset.InventoryPresetManager;
import ru.mentions.module.impl.player.AuctionHelper;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_465.class})
public abstract class HandledScreenMixin {
   @Shadow
   @Final
   protected class_1703 field_2797;
   @Unique
   @Mutable
   private class_1735 ahSlot1 = null;
   @Unique
   @Mutable
   private class_1735 ahSlot2 = null;
   @Unique
   @Mutable
   private class_1735 ahSlot3 = null;

   @Shadow
   public abstract class_1703 method_17577();

   @Inject(
      method = {"method_25393"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      AuctionHelper mod = AuctionHelper.get();
      if (mod != null && mod.isEnabled()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1755 != null) {
            String title = mc.field_1755.method_25440().getString();
            if (!this.isAuctionScreen(title)) {
               this.ahSlot3 = null;
               this.ahSlot2 = null;
               this.ahSlot1 = null;
            } else {
               this.ahSlot3 = null;
               this.ahSlot2 = null;
               this.ahSlot1 = null;
               long p1 = Long.MAX_VALUE;
               long p2 = Long.MAX_VALUE;
               long p3 = Long.MAX_VALUE;

               for (class_1735 slot : this.method_17577().field_7761) {
                  int total;
                  if (slot.method_7681() && (total = AuctionHelper.getPrice(slot)) > 0) {
                     long perOne = (long)total / (long)Math.max(slot.method_7677().method_7947(), 1);
                     if (perOne < p1) {
                        p3 = p2;
                        this.ahSlot3 = this.ahSlot2;
                        p2 = p1;
                        this.ahSlot2 = this.ahSlot1;
                        p1 = perOne;
                        this.ahSlot1 = slot;
                     } else if (perOne < p2) {
                        p3 = p2;
                        this.ahSlot3 = this.ahSlot2;
                        p2 = perOne;
                        this.ahSlot2 = slot;
                     } else if (perOne < p3) {
                        p3 = perOne;
                        this.ahSlot3 = slot;
                     }
                  }
               }
            }
         }
      } else {
         this.ahSlot3 = null;
         this.ahSlot2 = null;
         this.ahSlot1 = null;
      }
   }

   @Inject(
      method = {"method_2385"},
      at = {@At("HEAD")}
   )
   private void onDrawSlot(class_332 context, class_1735 slot, int mouseX, int mouseY, CallbackInfo ci) {
      AuctionHelper mod = AuctionHelper.get();
      if (mod != null && mod.isEnabled()) {
         if (slot == this.ahSlot1) {
            mod.renderSlot(context, slot, 1);
         } else if (slot == this.ahSlot2) {
            mod.renderSlot(context, slot, 2);
         } else if (slot == this.ahSlot3) {
            mod.renderSlot(context, slot, 3);
         }
      }
   }

   @Inject(
      method = {"method_2385"},
      at = {@At("TAIL")}
   )
   private void onDrawPresetSlot(class_332 context, class_1735 slot, int mouseX, int mouseY, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         if (this instanceof class_490 && Client.get() != null && Client.get().inventoryPresetManager() != null) {
            InventoryPresetManager manager = Client.get().inventoryPresetManager();
            InventoryPreset preset = manager.activePreset().orElse(null);
            if (preset != null) {
               int logical = InventoryPresetManager.screenToLogicalSlot(slot.field_7874);
               if (logical >= 0) {
                  InventoryPreset.Entry expected = preset.slot(logical);
                  if (!expected.isEmpty()) {
                     InventoryPresetManager.SlotState state = manager.stateFor(logical);

                     int color = switch (state) {
                        case CORRECT -> -12400277;
                        case WRONG_SLOT -> -1787569;
                        case MISSING -> -2073503;
                        default -> 0;
                     };
                     class_1799 ghost;
                     if (state != InventoryPresetManager.SlotState.CORRECT && !(ghost = expected.toStack()).method_7960()) {
                        context.method_51427(ghost, slot.field_7873, slot.field_7872);
                        context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, 1695156746);
                        context.method_51431(class_310.method_1551().field_1772, ghost, slot.field_7873, slot.field_7872);
                     }

                     if (color != 0) {
                        context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 1, color);
                        context.method_25294(slot.field_7873, slot.field_7872 + 15, slot.field_7873 + 16, slot.field_7872 + 16, color);
                        context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 1, slot.field_7872 + 16, color);
                        context.method_25294(slot.field_7873 + 15, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, color);
                     }
                  }
               }
            }
         }
      }
   }

   @Unique
   private boolean isAuctionScreen(String title) {
      return title.contains("Аукцион")
         || title.contains("Auction")
         || title.contains("Маркет")
         || title.contains("ꈁꀀꈂꌲꈂꀁ")
         || title.contains("Аукционы")
         || title.contains("Поиск:")
         || title.contains("Аукцион")
         || title.contains("Аукционы ")
         || title.equals("☃️ Аукцион")
         || title.startsWith("☃️ П:")
         || title.startsWith("☃️ Поиск:")
         || title.startsWith("0A2z")
         || title.contains("Поиск: ");
   }
}
