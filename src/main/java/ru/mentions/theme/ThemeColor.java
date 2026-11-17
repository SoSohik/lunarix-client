package ru.mentions.theme;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public final class ThemeColor {
   private ThemeColor() {
   }

   public static int getVisualColor() {
      return ColorUtil.client();
   }

   public static int getHudColor() {
      return ColorUtil.client();
   }

   public static int getHudColor(float alpha) {
      return ColorUtil.replAlpha(ColorUtil.client(), alpha);
   }

   public static int getTextColor() {
      return ColorUtil.getColor(255);
   }

   public static int getDarkTextColor() {
      return ColorUtil.getColor(175);
   }

   public static int getBackgroundColor() {
      return ThemeManager.get().getActive().get(1);
   }

   public static int getOutlineColor() {
      return ThemeManager.get().getActive().get(2);
   }

   public static int getSeparatorColor() {
      return ColorUtil.getColor(255, 0.1F);
   }

   public static int getLightBackgroundColor() {
      return ThemeManager.get().getActive().get(7);
   }

   public static int byIndex(int index) {
      return ThemeManager.get().getActive().get(index);
   }

   public static float getOpacity() {
      return ThemeManager.get().getActive().getSlider(0);
   }

   public static int getBlur() {
      return Math.round(ThemeManager.get().getActive().getSlider(1));
   }

   public static boolean getShadow() {
      return ThemeManager.get().getActive().getBool(0);
   }

   public static boolean getDistortion() {
      return ThemeManager.get().getActive().getBool(1);
   }

   public static int getSeparatorColor(float alpha) {
      return ColorUtil.multAlpha(ThemeManager.get().getActive().get(6), alpha);
   }

   public static int getTextColor(float alpha) {
      return ColorUtil.replAlpha(ThemeManager.get().getActive().get(3), alpha);
   }

   public static int getLightBackgroundColor(float alpha) {
      return ColorUtil.multAlpha(ThemeManager.get().getActive().get(7), alpha);
   }

   public static int getOutlineColor(float alpha) {
      return ColorUtil.multAlpha(ThemeManager.get().getActive().get(2), alpha);
   }

   public static int getDarkTextColor(float alpha) {
      return ColorUtil.replAlpha(ThemeManager.get().getActive().get(4), alpha);
   }

   public static int getBackgroundColor(float alpha) {
      return ColorUtil.replAlpha(ThemeManager.get().getActive().get(1), alpha);
   }
}
