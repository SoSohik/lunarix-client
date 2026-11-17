package ru.mentions.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_476;
import net.minecraft.class_5250;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import net.minecraft.class_1792.class_9635;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_1799.class})
public abstract class ItemStackTooltipMixin {
   @Unique
   private static final Pattern PRICE_PATTERN = Pattern.compile("Цен[аaAАыЫ]?:?\\s*([\\d,\\s\\.]+)", 2);

   @Inject(
      method = {"method_7950"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void appendUnitPrice(class_9635 context, class_1657 player, class_1836 type, CallbackInfoReturnable<List<class_2561>> cir) {
      try {
         class_310 mc = class_310.method_1551();
         if (!(mc.field_1755 instanceof class_476)) {
            return;
         }

         class_1799 stack = (class_1799)this;
         if (stack.method_7960() || stack.method_7947() <= 1) {
            return;
         }

         int totalPrice = parsePriceFromLore(stack);
         if (totalPrice <= 0) {
            return;
         }

         int unitPrice = totalPrice / stack.method_7947();
         if (unitPrice <= 0) {
            unitPrice = 1;
         }

         ArrayList<class_2561> tooltip = new ArrayList<>((Collection<? extends class_2561>)cir.getReturnValue());
         int priceLineIndex = findPriceLineIndex(tooltip);
         class_5250 unitLine = class_2561.method_43470("")
            .method_10852(class_2561.method_43470("$ ").method_27692(class_124.field_1077))
            .method_10852(class_2561.method_43470("За 1 шт: ").method_27692(class_124.field_1068))
            .method_10852(class_2561.method_43470(formatPrice(unitPrice)).method_27692(class_124.field_1060));
         if (priceLineIndex >= 0 && priceLineIndex + 1 <= tooltip.size()) {
            tooltip.add(priceLineIndex + 1, unitLine);
         } else {
            tooltip.add(unitLine);
         }

         cir.setReturnValue(tooltip);
      } catch (Exception var12) {
      }
   }

   @Unique
   private static int parsePriceFromLore(class_1799 stack) {
      class_9290 loreComp = (class_9290)stack.method_58694(class_9334.field_49632);
      if (loreComp == null) {
         return 0;
      } else {
         for (class_2561 text : loreComp.comp_2400()) {
            String line = class_124.method_539(text.getString());
            Matcher m;
            if (line != null && (m = PRICE_PATTERN.matcher(line)).find()) {
               try {
                  String priceStr = m.group(1).replaceAll("[,\\s\\.]", "");
                  return Integer.parseInt(priceStr);
               } catch (NumberFormatException var7) {
               }
            }
         }

         return 0;
      }
   }

   @Unique
   private static int findPriceLineIndex(List<class_2561> tooltip) {
      for (int i = 0; i < tooltip.size(); i++) {
         String line = class_124.method_539(tooltip.get(i).getString());
         if (line != null) {
            Matcher m = PRICE_PATTERN.matcher(line);
            if (m.find()) {
               return i;
            }

            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("цена") || lowerLine.contains("price") || lowerLine.contains("цен")) {
               return i;
            }
         }
      }

      return -1;
   }

   @Unique
   private static String formatPrice(int price) {
      String value = String.valueOf(price);
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < value.length(); i++) {
         if (i > 0 && (value.length() - i) % 3 == 0) {
            builder.append('.');
         }

         builder.append(value.charAt(i));
      }

      return builder.toString();
   }
}
