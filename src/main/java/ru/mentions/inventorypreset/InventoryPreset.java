package ru.mentions.inventorypreset;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.minecraft.class_9334;

@Environment(EnvType.CLIENT)
public final class InventoryPreset {
   public static final int INVENTORY_SLOTS = 36;
   public static final int ARMOR_SLOTS = 4;
   public static final int OFFHAND_SLOT = 40;
   public static final int SLOT_COUNT = 41;
   private final String name;
   private final long createdAt;
   private final List<InventoryPreset.Entry> slots;

   public InventoryPreset(String name, long createdAt, List<InventoryPreset.Entry> slots) {
      this.name = name;
      this.createdAt = createdAt;
      ArrayList<InventoryPreset.Entry> normalized = new ArrayList<>(41);

      for (int i = 0; i < 41; i++) {
         InventoryPreset.Entry entry = i < slots.size() ? slots.get(i) : InventoryPreset.Entry.EMPTY;
         normalized.add(entry == null ? InventoryPreset.Entry.EMPTY : entry);
      }

      this.slots = Collections.unmodifiableList(normalized);
   }

   public String name() {
      return this.name;
   }

   public long createdAt() {
      return this.createdAt;
   }

   public List<InventoryPreset.Entry> slots() {
      return this.slots;
   }

   public InventoryPreset.Entry slot(int logicalSlot) {
      return logicalSlot >= 0 && logicalSlot < this.slots.size() ? this.slots.get(logicalSlot) : InventoryPreset.Entry.EMPTY;
   }

   @Environment(EnvType.CLIENT)
   public static record Entry(String itemId, int count, String displayName, String potionSignature, String potionData) {
      public static final InventoryPreset.Entry EMPTY = new InventoryPreset.Entry("", 0, "", "", "");
      public static final String LEGACY_NAME_PREFIX = "legacy-name:";

      public Entry(String itemId, int count, String displayName, String potionSignature, String potionData) {
         itemId = itemId == null ? "" : itemId;
         count = Math.max(0, count);
         displayName = displayName == null ? "" : displayName;
         potionSignature = potionSignature == null ? "" : potionSignature;
         potionData = potionData == null ? "" : potionData;
         this.itemId = itemId;
         this.count = count;
         this.displayName = displayName;
         this.potionSignature = potionSignature;
         this.potionData = potionData;
      }

      public static InventoryPreset.Entry fromStack(class_1799 stack) {
         if (stack != null && !stack.method_7960()) {
            class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
            class_1844 potionContents = (class_1844)stack.method_58694(class_9334.field_49651);
            Object signature = potionContents != null
               ? "potion:" + potionContents
               : (id.method_12832().endsWith("bundle") ? "bundle:" + stack.method_57353() : "");
            return new InventoryPreset.Entry(
               id.toString(), stack.method_7947(), stack.method_7964().getString(), (String)signature, encodePotion(potionContents)
            );
         } else {
            return EMPTY;
         }
      }

      private static String encodePotion(class_1844 contents) {
         return contents == null ? "" : class_1844.field_49275.encodeStart(JsonOps.INSTANCE, contents).result().map(Object::toString).orElse("");
      }

      private static class_1844 decodePotion(String json) {
         if (json != null && !json.isBlank()) {
            try {
               return (class_1844)class_1844.field_49275.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElse(null);
            } catch (Exception var2) {
               return null;
            }
         } else {
            return null;
         }
      }

      public boolean isEmpty() {
         return this.itemId.isBlank() || this.count <= 0;
      }

      public boolean matches(class_1799 stack) {
         if (!this.isEmpty()) {
            return stack != null && !stack.method_7960() && stack.method_7947() >= this.count ? this.sameItem(stack) : false;
         } else {
            return stack == null || stack.method_7960();
         }
      }

      public boolean sameItem(class_1799 stack) {
         if (!this.isEmpty() && stack != null && !stack.method_7960()) {
            if (!class_7923.field_41178.method_10221(stack.method_7909()).toString().equals(this.itemId)) {
               return false;
            } else if (this.potionSignature.isBlank()) {
               return true;
            } else if (this.potionSignature.startsWith("legacy-name:")) {
               return this.potionSignature.substring("legacy-name:".length()).equals(stack.method_7964().getString());
            } else if (this.potionSignature.startsWith("bundle:")) {
               return this.potionSignature.equals("bundle:" + stack.method_57353());
            } else {
               Object potionContents = stack.method_58694(class_9334.field_49651);
               return potionContents == null
                  ? false
                  : this.potionSignature.equals(potionContents.toString()) || this.potionSignature.equals("potion:" + potionContents);
            }
         } else {
            return false;
         }
      }

      public String matchKey() {
         return this.itemId + "\u0000" + this.potionSignature;
      }

      public static boolean canHavePotionContents(String itemId) {
         return itemId.equals("minecraft:potion")
            || itemId.equals("minecraft:splash_potion")
            || itemId.equals("minecraft:lingering_potion")
            || itemId.equals("minecraft:tipped_arrow");
      }

      public class_1799 toStack() {
         if (this.isEmpty()) {
            return class_1799.field_8037;
         } else {
            class_2960 id = class_2960.method_12829(this.itemId);
            if (id == null) {
               return class_1799.field_8037;
            } else {
               class_1799 stack = class_7923.field_41178.method_17966(id).map(item -> new class_1799(item, this.count)).orElse(class_1799.field_8037);
               if (stack.method_7960()) {
                  return stack;
               } else {
                  class_1844 contents = decodePotion(this.potionData);
                  if (contents != null) {
                     stack.method_57379(class_9334.field_49651, contents);
                  }

                  if (!this.displayName.isBlank() && !this.displayName.equals(stack.method_7964().getString())) {
                     stack.method_57379(class_9334.field_49631, class_2561.method_43470(this.displayName));
                  }

                  return stack;
               }
            }
         }
      }
   }
}
