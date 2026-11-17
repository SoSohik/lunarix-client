package ru.mentions.module.impl.misc;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_7923;

@Environment(EnvType.CLIENT)
public final class AutoBuyRule {
   public String name = "";
   public int maxPrice = 0;
   public int minQuantity = 1;
   public boolean enabled = true;
   public boolean fuzzy = false;
   public String itemId = "";
   public List<String> aliases = new ArrayList<>();

   public AutoBuyRule() {
   }

   public AutoBuyRule(String name, int maxPrice, int minQuantity) {
      this.set(name, maxPrice, minQuantity);
   }

   public void set(String name, int maxPrice, int minQuantity) {
      this.name = name == null ? "" : name;
      this.maxPrice = Math.max(0, maxPrice);
      this.minQuantity = Math.max(1, minQuantity);
   }

   public boolean hasName() {
      return normalize(this.name).isEmpty() && normalize(this.itemId).isEmpty() ? this.aliases != null && !this.aliases.isEmpty() : true;
   }

   public boolean isValid() {
      return this.maxPrice > 0 && this.hasName();
   }

   public int limit(int percent) {
      long value = (long)this.maxPrice * (long)Math.max(1, percent) / 100L;
      return (int)Math.max(1L, Math.min(2147483647L, value));
   }

   public boolean matches(class_1799 stack) {
      return this.enabled && this.matchesItem(stack);
   }

   public boolean matchesItem(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         String expectedId = normalize(this.itemId);
         if (!expectedId.isEmpty() && expectedId.equals(itemIdOf(stack))) {
            return true;
         } else {
            String actual = normalize(stack.method_7964().getString());
            if (actual.isEmpty()) {
               return false;
            } else {
               String expected = normalize(this.name);
               if (expected.isEmpty() || (this.fuzzy ? !actual.contains(expected) : !actual.equals(expected))) {
                  if (this.aliases != null) {
                     for (String alias : this.aliases) {
                        String clean = normalize(alias);
                        if (!clean.isEmpty() && actual.contains(clean)) {
                           return true;
                        }
                     }
                  }

                  return false;
               } else {
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public static String itemIdOf(class_1799 stack) {
      return stack != null && stack.method_7909() != null ? class_7923.field_41178.method_10221(stack.method_7909()).toString() : "";
   }

   public static String normalize(String value) {
      if (value == null) {
         return "";
      } else {
         StringBuilder sb = new StringBuilder(value.length());

         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == 167 && i + 1 < value.length()) {
               i++;
            } else if (c != ' ') {
               sb.append(Character.toLowerCase(c));
            }
         }

         return sb.toString().trim().replaceAll("\\s+", " ");
      }
   }
}
