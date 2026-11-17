package ru.mentions.module.impl.utils;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1707;
import net.minecraft.class_1708;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.math.ChatUtils;

@ModuleInfo(
   name = "Potion Farmer",
   desc = "Автоматически варит зелье",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class PotionFarmer extends Module {
   private final class_310 mc = class_310.method_1551();
   private final List<class_2338> orderedChests = new ArrayList<>();
   private class_2338 brewingStandPos = null;
   private int currentStage = 0;
   private final class_1792[] INGREDIENTS = new class_1792[]{class_1802.field_8790, class_1802.field_8071, class_1802.field_8711, class_1802.field_8725};

   @Override
   public void onEnable() {
      super.onEnable();
      this.orderedChests.clear();
      this.brewingStandPos = null;
      this.currentStage = 0;
      this.findBlocks();
   }

   private void findBlocks() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         class_2338 playerPos = this.mc.field_1724.method_24515();
         ArrayList<class_2338> foundChests = new ArrayList<>();

         for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
               for (int z = -5; z <= 5; z++) {
                  class_2338 pos = playerPos.method_10069(x, y, z);
                  class_2248 block = this.mc.field_1687.method_8320(pos).method_26204();
                  if (block == class_2246.field_10034) {
                     foundChests.add(pos);
                  } else if (block == class_2246.field_10333) {
                     this.brewingStandPos = pos;
                  }
               }
            }
         }

         if (foundChests.size() >= 3) {
            foundChests.sort((b1, b2) -> Integer.compare(b1.method_10264(), b2.method_10264()));
            this.orderedChests.addAll(foundChests.subList(0, 3));
         }
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
         this.toggle();
         ChatUtils.addChatMessageDev("[PotionFarmer] Ошибка: Модуль временно не роботает!");
         if (this.mc.field_1724.field_7512 instanceof class_1708) {
            this.handleBrewingStand((class_1708)this.mc.field_1724.field_7512);
         } else if (this.mc.field_1724.field_7512 instanceof class_1707) {
            this.handleChest((class_1707)this.mc.field_1724.field_7512);
         } else {
            this.openRequiredContainer();
         }
      }
   }

   private void openRequiredContainer() {
      if (!this.hasWaterBottleInInventory()) {
         this.interactWithBlock(this.orderedChests.get(1));
      } else if (this.currentStage < 4 && !this.hasItemInInventory(this.INGREDIENTS[this.currentStage])) {
         this.interactWithBlock(this.orderedChests.get(0));
      } else if (this.hasReadyPotionInInventory()) {
         this.interactWithBlock(this.orderedChests.get(2));
      } else {
         this.interactWithBlock(this.brewingStandPos);
      }
   }

   private void handleBrewingStand(class_1708 stand) {
      class_1799 ingredientSlot = stand.method_7611(3).method_7677();
      class_1799 fuelSlot = stand.method_7611(4).method_7677();
      if (fuelSlot.method_7960() && this.hasItemInInventory(class_1802.field_8183)) {
         this.shiftClickItem(class_1802.field_8183, stand);
      } else {
         boolean hasBottles = !stand.method_7611(0).method_7677().method_7960()
            || !stand.method_7611(1).method_7677().method_7960()
            || !stand.method_7611(2).method_7677().method_7960();
         if (!hasBottles) {
            this.shiftClickWaterBottle(stand);
         } else {
            int invSlot;
            if (ingredientSlot.method_7960() && this.currentStage < 4 && (invSlot = this.findItemSlotInInventory(this.INGREDIENTS[this.currentStage])) != -1) {
               int serverSlot = invSlot + 5;
               this.mc.field_1761.method_2906(stand.field_7763, serverSlot, 1, class_1713.field_7790, this.mc.field_1724);
               this.mc.field_1761.method_2906(stand.field_7763, 3, 0, class_1713.field_7790, this.mc.field_1724);
               this.mc.field_1761.method_2906(stand.field_7763, serverSlot, 0, class_1713.field_7790, this.mc.field_1724);
               this.currentStage++;
               if (this.currentStage > 3) {
                  this.currentStage = 0;
               }
            }

            for (int i = 0; i < 3; i++) {
               class_1799 potion = stand.method_7611(i).method_7677();
               if (this.isLongInvisibilityPotion(potion)) {
                  this.mc.field_1761.method_2906(stand.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
               }
            }
         }
      }
   }

   private void handleChest(class_1707 chest) {
      if (this.hasReadyPotionInInventory()) {
         for (int i = 0; i < chest.field_7761.size(); i++) {
            class_1799 stack = chest.method_7611(i).method_7677();
            if (this.isLongInvisibilityPotion(stack) && i >= chest.method_17388() * 9) {
               this.mc.field_1761.method_2906(chest.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
            }
         }
      } else {
         for (class_1792 ingredient : this.INGREDIENTS) {
            if (!this.hasItemInInventory(ingredient)) {
               this.takeItemFromChest(chest, ingredient, 1);
               return;
            }
         }

         if (!this.hasItemInInventory(class_1802.field_8183)) {
            this.takeItemFromChest(chest, class_1802.field_8183, 2);
         } else {
            if (!this.hasWaterBottleInInventory()) {
               this.takeWaterBottleFromChest(chest, 3);
            }
         }
      }
   }

   private boolean isLongInvisibilityPotion(class_1799 stack) {
      if (stack.method_7909() != class_1802.field_8574) {
         return false;
      } else {
         class_1844 potionContents = (class_1844)stack.method_58694(class_9334.field_49651);
         if (potionContents != null && potionContents.comp_2378().isPresent()) {
            class_6880 entry = (class_6880)potionContents.comp_2378().get();
            return entry.equals(class_1847.field_9000);
         } else {
            return false;
         }
      }
   }

   private boolean isWaterBottle(class_1799 stack) {
      if (stack.method_7909() != class_1802.field_8574) {
         return false;
      } else {
         class_1844 potionContents = (class_1844)stack.method_58694(class_9334.field_49651);
         if (potionContents != null && potionContents.comp_2378().isPresent()) {
            class_6880 entry = (class_6880)potionContents.comp_2378().get();
            return entry.equals(class_1847.field_8991);
         } else {
            return false;
         }
      }
   }

   private boolean hasWaterBottleInInventory() {
      if (this.mc.field_1724 == null) {
         return false;
      } else {
         class_1661 inv = this.mc.field_1724.method_31548();

         for (int i = 0; i < 36; i++) {
            if (this.isWaterBottle(inv.method_5438(i))) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean hasReadyPotionInInventory() {
      if (this.mc.field_1724 == null) {
         return false;
      } else {
         class_1661 inv = this.mc.field_1724.method_31548();

         for (int i = 0; i < 36; i++) {
            if (this.isLongInvisibilityPotion(inv.method_5438(i))) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean hasItemInInventory(class_1792 item) {
      return this.findItemSlotInInventory(item) != -1;
   }

   private int findItemSlotInInventory(class_1792 item) {
      if (this.mc.field_1724 == null) {
         return -1;
      } else {
         class_1661 inv = this.mc.field_1724.method_31548();

         for (int i = 0; i < 36; i++) {
            if (inv.method_5438(i).method_7909() == item) {
               return i;
            }
         }

         return -1;
      }
   }

   private void interactWithBlock(class_2338 pos) {
      if (pos != null && this.mc.field_1761 != null && this.mc.field_1724 != null) {
         class_3965 hitResult = new class_3965(
            new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5),
            class_2350.field_11036,
            pos,
            false
         );
         this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, hitResult);
      }
   }

   private void shiftClickItem(class_1792 item, class_1708 stand) {
      for (int i = 5; i < stand.field_7761.size(); i++) {
         if (stand.method_7611(i).method_7677().method_7909() == item) {
            this.mc.field_1761.method_2906(stand.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
            break;
         }
      }
   }

   private void shiftClickWaterBottle(class_1708 stand) {
      for (int i = 5; i < stand.field_7761.size(); i++) {
         if (this.isWaterBottle(stand.method_7611(i).method_7677())) {
            this.mc.field_1761.method_2906(stand.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
         }
      }
   }

   private void takeItemFromChest(class_1707 chest, class_1792 item, int amount) {
      int chestSize = chest.method_17388() * 9;
      int taken = 0;

      for (int i = 0; i < chestSize; i++) {
         if (chest.method_7611(i).method_7677().method_7909() == item) {
            this.mc.field_1761.method_2906(chest.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
            if (++taken >= amount) {
               break;
            }
         }
      }
   }

   private void takeWaterBottleFromChest(class_1707 chest, int amount) {
      int chestSize = chest.method_17388() * 9;
      int taken = 0;

      for (int i = 0; i < chestSize; i++) {
         if (this.isWaterBottle(chest.method_7611(i).method_7677())) {
            this.mc.field_1761.method_2906(chest.field_7763, i, 0, class_1713.field_7794, this.mc.field_1724);
            if (++taken >= amount) {
               break;
            }
         }
      }
   }
}
