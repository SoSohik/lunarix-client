package ru.mentions.utils.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10444;
import net.minecraft.class_1058;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5819;
import net.minecraft.class_811;
import org.joml.Matrix3x2fStack;

@Environment(EnvType.CLIENT)
public class ItemRender {
   private static final class_310 mc = class_310.method_1551();
   private static final Map<String, ItemRender.CachedSprite> SPRITE_CACHE = new ConcurrentHashMap<>();
   private static final class_5819 RANDOM = class_5819.method_43047();
   private static final int FORCED_GUI_SCALE = 2;

   private static int getCurrentGuiScale() {
      int scale = (Integer)mc.field_1690.method_42474().method_41753();
      if (scale == 0) {
         scale = mc.method_22683().method_4476(0, mc.method_1573());
      }

      return scale;
   }

   private static float getScaleCompensation() {
      return 2.0F / (float)getCurrentGuiScale();
   }

   public static boolean isBlockItem(class_1799 stack) {
      return stack.method_7909() instanceof class_1747;
   }

   public static boolean isPotionItem(class_1799 stack) {
      return stack.method_7909() == class_1802.field_8574
         || stack.method_7909() == class_1802.field_8436
         || stack.method_7909() == class_1802.field_8150
         || stack.method_7909() == class_1802.field_8087;
   }

   public static boolean hasGlint(class_1799 stack) {
      return stack.method_7958();
   }

   public static boolean needsContextRender(class_1799 stack) {
      return isBlockItem(stack) || isPotionItem(stack) || hasGlint(stack);
   }

   public static void drawItem(class_1799 stack, float x, float y, float scale, float alpha) {
      drawItem(stack, x, y, scale, alpha, -1);
   }

   public static void drawItem(class_1799 stack, float x, float y, float scale, float alpha, int tintColor) {
      if (!stack.method_7960() && !(alpha <= 0.01F)) {
         if (!needsContextRender(stack)) {
            class_1058 sprite = getSpriteForStack(stack);
            if (sprite != null) {
               int color = applyAlpha(tintColor, alpha);
               float size = 16.0F * scale;
               Render2D.drawSprite(sprite, x, y, size, size, color, true);
            }
         }
      }
   }

   public static void drawBlockItem(class_332 context, class_1799 stack, float x, float y, float scale, float alpha) {
      if (!stack.method_7960() && !(alpha <= 0.01F)) {
         float compensation = getScaleCompensation();
         float finalScale = scale * compensation;
         float size = 16.0F * scale;
         float centerX = x + size / 2.0F;
         float centerY = y + size / 2.0F;
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(centerX, centerY);
         matrices.scale(finalScale, finalScale);
         matrices.translate(-8.0F, -8.0F);
         context.method_51427(stack, 0, 0);
         matrices.popMatrix();
      }
   }

   public static void drawItemWithContext(class_332 context, class_1799 stack, float x, float y, float scale, float alpha) {
      if (!stack.method_7960() && !(alpha <= 0.01F)) {
         float compensation = getScaleCompensation();
         float finalScale = scale * compensation;
         float size = 16.0F * scale;
         float centerX = x + size / 2.0F;
         float centerY = y + size / 2.0F;
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(centerX, centerY);
         matrices.scale(finalScale, finalScale);
         matrices.translate(-8.0F, -8.0F);
         context.method_51427(stack, 0, 0);
         matrices.popMatrix();
      }
   }

   public static void drawItemCentered(class_1799 stack, float centerX, float centerY, float scale, float alpha) {
      float size = 16.0F * scale;
      float x = centerX - size / 2.0F;
      float y = centerY - size / 2.0F;
      drawItem(stack, x, y, scale, alpha);
   }

   public static void drawItemCenteredWithContext(class_332 context, class_1799 stack, float centerX, float centerY, float scale, float alpha) {
      float size = 16.0F * scale;
      float x = centerX - size / 2.0F;
      float y = centerY - size / 2.0F;
      drawItemWithContext(context, stack, x, y, scale, alpha);
   }

   private static class_1058 getSpriteForStack(class_1799 stack) {
      String cacheKey = getCacheKey(stack);
      ItemRender.CachedSprite cached = SPRITE_CACHE.get(cacheKey);
      if (cached != null) {
         return cached.sprite;
      } else {
         try {
            class_10444 state = new class_10444();
            mc.method_65386().method_65598(state, stack, class_811.field_4317, mc.field_1687, null, 0);
            class_1058 sprite = state.method_65603(RANDOM);
            if (sprite != null) {
               SPRITE_CACHE.put(cacheKey, new ItemRender.CachedSprite(sprite));
               return sprite;
            }
         } catch (Exception var5) {
         }

         return null;
      }
   }

   private static String getCacheKey(class_1799 stack) {
      return stack.method_7909().toString() + "_" + stack.method_57353().hashCode();
   }

   private static int applyAlpha(int color, float alpha) {
      int a = (int)((float)(color >> 24 & 0xFF) * alpha);
      return a << 24 | color & 16777215;
   }

   public static void clearCache() {
      SPRITE_CACHE.clear();
   }

   @Environment(EnvType.CLIENT)
   private static record CachedSprite(class_1058 sprite) {
   }
}
