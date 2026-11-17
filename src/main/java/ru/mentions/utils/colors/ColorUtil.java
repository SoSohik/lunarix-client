package ru.mentions.utils.colors;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;
import org.lwjgl.opengl.GL11;
import ru.mentions.manager.Theme;
import ru.mentions.module.impl.display.ClientTheme;
import ru.mentions.module.impl.display.Hud;
import ru.mentions.screen.Menu;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.math.Interpolator;

@Environment(EnvType.CLIENT)
public final class ColorUtil {
   private static final long CACHE_EXPIRATION_TIME = 60000L;
   private static final ConcurrentHashMap<ColorUtil.ColorKey, ColorUtil.CacheEntry> colorCache = new ConcurrentHashMap<>();
   private static final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
   private static final DelayQueue<ColorUtil.CacheEntry> cleanupQueue = new DelayQueue<>();
   public static Char2IntArrayMap colorCodes = new Char2IntArrayMap() {
      {
         this.put('0', 0);
         this.put('1', 170);
         this.put('2', 43520);
         this.put('3', 43690);
         this.put('4', 11141120);
         this.put('5', 11141290);
         this.put('6', 16755200);
         this.put('7', 11184810);
         this.put('8', 5592405);
         this.put('9', 5592575);
         this.put('A', 5635925);
         this.put('B', 5636095);
         this.put('C', 16733525);
         this.put('D', 16733695);
         this.put('E', 16777045);
         this.put('F', 16777215);
      }
   };
   public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9a-f-or]");
   public static final int RED = getColor(255, 0, 0);
   public static final int GREEN = getColor(0, 255, 0);
   public static final int BLUE = getColor(0, 0, 255);
   public static final int YELLOW = getColor(255, 255, 0);
   public static final int WHITE = getColor(255);
   public static final int BLACK = getColor(0);
   private static boolean clientResolving;

   public static int[] solid8(int color) {
      return new int[]{color, color, color, color, color, color, color, color};
   }

   public static int interpolateColorsBackAndForth(int speed, int index, int startColor, int endColor, boolean trueColor) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      angle = (angle >= 180 ? 360 - angle : angle) * 2;
      return interpolateColor(startColor, endColor, (float)angle / 360.0F, trueColor);
   }

   public static int getHealthColor(float health, float maxHealth) {
      float progress = class_3532.method_15363(health / maxHealth, 0.0F, 1.0F);
      return progress <= 0.5F
         ? interpolateColor2(new Color(255, 0, 0).getRGB(), new Color(255, 255, 0).getRGB(), progress / 0.5F)
         : interpolateColor2(new Color(255, 255, 0).getRGB(), new Color(0, 255, 0).getRGB(), (progress - 0.5F) / 0.5F);
   }

   public static int client() {
      if (clientResolving) {
         return baseClient();
      } else if (ClientTheme.INSTANCE != null) {
         clientResolving = true;

         int var0;
         try {
            var0 = ClientTheme.INSTANCE.getAccentColor(0);
         } finally {
            clientResolving = false;
         }

         return var0;
      } else {
         return baseClient();
      }
   }

   public static int baseClient() {
      Theme theme = getTheme();
      Theme preTheme = getPreTheme();
      return interpolate(theme.getClient(), preTheme.getClient(), (double)(1.0F - Menu.animation14.getOutput()));
   }

   public static int background() {
      if (ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isWhiteTheme()) {
         return getColor(245, 247, 252);
      } else {
         Theme theme = getTheme();
         Theme preTheme = getPreTheme();
         return interpolate(theme.getRect(), preTheme.getRect(), (double)(1.0F - Menu.animation14.getOutput()));
      }
   }

   private static Theme getTheme() {
      return Menu.selectedTheme != null ? Menu.selectedTheme : Theme.VIOLKA;
   }

   private static Theme getPreTheme() {
      return Menu.preSelectedTheme != null ? Menu.preSelectedTheme : Theme.VIOLKA;
   }

   public static int interpolateColor2(int color1, int color2, float amount) {
      amount = class_3532.method_15363(amount, 0.0F, 1.0F);
      Color c1 = new Color(color1, true);
      Color c2 = new Color(color2, true);
      int red = (int)((float)c1.getRed() + (float)(c2.getRed() - c1.getRed()) * amount);
      int green = (int)((float)c1.getGreen() + (float)(c2.getGreen() - c1.getGreen()) * amount);
      int blue = (int)((float)c1.getBlue() + (float)(c2.getBlue() - c1.getBlue()) * amount);
      int alpha = (int)((float)c1.getAlpha() + (float)(c2.getAlpha() - c1.getAlpha()) * amount);
      return new Color(red, green, blue, alpha).getRGB();
   }

   public static int interpolateColor(int color1, int color2, float amount, boolean trueColor) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      Color c1 = new Color(color1, true);
      Color c2 = new Color(color2, true);
      return new Color(
            (int)((float)c1.getRed() + (float)(c2.getRed() - c1.getRed()) * amount),
            (int)((float)c1.getGreen() + (float)(c2.getGreen() - c1.getGreen()) * amount),
            (int)((float)c1.getBlue() + (float)(c2.getBlue() - c1.getBlue()) * amount),
            (int)((float)c1.getAlpha() + (float)(c2.getAlpha() - c1.getAlpha()) * amount)
         )
         .getRGB();
   }

   public static int applyOpacity(int color, float opacity) {
      int alpha = color >> 24 & 0xFF;
      int newAlpha = (int)((float)alpha * opacity);
      newAlpha = Math.max(0, Math.min(255, newAlpha));
      return color & 16777215 | newAlpha << 24;
   }

   public static int interpolateColor(int color1, int color2, float factor) {
      factor = Math.min(1.0F, Math.max(0.0F, factor));
      int a1 = color1 >> 24 & 0xFF;
      int r1 = color1 >> 16 & 0xFF;
      int g1 = color1 >> 8 & 0xFF;
      int b1 = color1 & 0xFF;
      int a2 = color2 >> 24 & 0xFF;
      int r2 = color2 >> 16 & 0xFF;
      int g2 = color2 >> 8 & 0xFF;
      int b2 = color2 & 0xFF;
      int a = (int)((float)a1 + (float)(a2 - a1) * factor);
      int r = (int)((float)r1 + (float)(r2 - r1) * factor);
      int g = (int)((float)g1 + (float)(g2 - g1) * factor);
      int b = (int)((float)b1 + (float)(b2 - b1) * factor);
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static int[] solid(int color) {
      return new int[]{color, color, color, color, color, color, color, color, color};
   }

   public static String removeFormatting(String text) {
      return text != null && !text.isEmpty() ? FORMATTING_CODE_PATTERN.matcher(text).replaceAll("") : null;
   }

   public static int multRed(int color, float percent01) {
      return getColorRaw(
         red(color), Math.min(255, Math.round((float)green(color) / percent01)), Math.min(255, Math.round((float)blue(color) / percent01)), alpha(color)
      );
   }

   public static int reAlphaInt(int color, int alpha) {
      return class_3532.method_15340(alpha, 0, 255) << 24 | color & 16777215;
   }

   public static int swapAlpha(int color, float alpha) {
      int f = color >> 16 & 0xFF;
      int f1 = color >> 8 & 0xFF;
      int f2 = color & 0xFF;
      return getColor2(f, f1, f2, (int)alpha);
   }

   public static int getColor2(int red, int green, int blue, int alpha) {
      int color = 0;
      color |= alpha << 24;
      color |= red << 16;
      color |= green << 8;
      int var8;
      return var8 = color | blue;
   }

   public static Color TwoColoreffect(Color color, Color color2, double n) {
      float clamp = class_3532.method_15363((float)Math.sin((Math.PI * 6) * (n / 4.0 % 1.0)) / 2.0F + 0.5F, 0.0F, 1.0F);
      return new Color(
         class_3532.method_16439((float)color.getRed() / 255.0F, (float)color2.getRed() / 255.0F, clamp),
         class_3532.method_16439((float)color.getGreen() / 255.0F, (float)color2.getGreen() / 255.0F, clamp),
         class_3532.method_16439((float)color.getBlue() / 255.0F, (float)color2.getBlue() / 255.0F, clamp),
         class_3532.method_16439((float)color.getAlpha() / 255.0F, (float)color2.getAlpha() / 255.0F, clamp)
      );
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return interpolateD((double)oldValue, (double)newValue, (double)((float)interpolationValue)).intValue();
   }

   public static Double interpolateD(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static int glColor(int color) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      GL11.glColor4f(red, green, blue, alpha);
      return color;
   }

   public static int skyRainbow(int speed, int index) {
      double angle = (double)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      return Color.getHSBColor((float)(angle / 360.0), 0.5F, 1.0F).hashCode();
   }

   public static float[] normalize(Color color) {
      return new float[]{(float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F};
   }

   public static float[] normalize(int color) {
      int[] components = unpack(color);
      return new float[]{(float)components[0] / 255.0F, (float)components[1] / 255.0F, (float)components[2] / 255.0F, (float)components[3] / 255.0F};
   }

   public static int[] unpack(int color) {
      return new int[]{color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF};
   }

   public static int fadeBetween(float speed, int offset, int color1, int color2) {
      long time = System.currentTimeMillis() + (long)offset;
      double factor = (Math.sin((double)time * 0.001 * (double)speed) + 1.0) / 2.0;
      int r1 = color1 >> 16 & 0xFF;
      int g1 = color1 >> 8 & 0xFF;
      int b1 = color1 & 0xFF;
      int r2 = color2 >> 16 & 0xFF;
      int g2 = color2 >> 8 & 0xFF;
      int b2 = color2 & 0xFF;
      int r = (int)((double)r1 + (double)(r2 - r1) * factor);
      int g = (int)((double)g1 + (double)(g2 - g1) * factor);
      int b = (int)((double)b1 + (double)(b2 - b1) * factor);
      return r << 16 | g << 8 | b;
   }

   public static int fadeBetween(int from, int to, float fraction) {
      fraction = clamp01(fraction);
      int a1 = from >> 24 & 0xFF;
      int r1 = from >> 16 & 0xFF;
      int g1 = from >> 8 & 0xFF;
      int b1 = from & 0xFF;
      int a2 = to >> 24 & 0xFF;
      int r2 = to >> 16 & 0xFF;
      int g2 = to >> 8 & 0xFF;
      int b2 = to & 0xFF;
      int a = (int)((float)a1 + (float)(a2 - a1) * fraction);
      int r = (int)((float)r1 + (float)(r2 - r1) * fraction);
      int g = (int)((float)g1 + (float)(g2 - g1) * fraction);
      int b = (int)((float)b1 + (float)(b2 - b1) * fraction);
      return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
   }

   private static float clamp01(float v) {
      if (v < 0.0F) {
         return 0.0F;
      } else {
         return v > 1.0F ? 1.0F : v;
      }
   }

   public static float[] rgba(int color) {
      return new float[]{
         (float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F, (float)(color >> 24 & 0xFF) / 255.0F
      };
   }

   public static int[] rgbas(int color) {
      return new int[]{
         (int)((float)(color >> 16 & 0xFF) / 255.0F),
         (int)((float)(color >> 8 & 0xFF) / 255.0F),
         (int)((float)(color & 0xFF) / 255.0F),
         (int)((float)(color >> 24 & 0xFF) / 255.0F)
      };
   }

   public static int red(int c) {
      return c >> 16 & 0xFF;
   }

   public static int green(int c) {
      return c >> 8 & 0xFF;
   }

   public static int blue(int c) {
      return c & 0xFF;
   }

   public static int alpha(int c) {
      return c >> 24 & 0xFF;
   }

   public static float redf(int c) {
      return (float)red(c) / 255.0F;
   }

   public static float greenf(int c) {
      return (float)green(c) / 255.0F;
   }

   public static float bluef(int c) {
      return (float)blue(c) / 255.0F;
   }

   public static float alphaf(int c) {
      return (float)alpha(c) / 255.0F;
   }

   public static int[] getRGBA(int c) {
      return new int[]{red(c), green(c), blue(c), alpha(c)};
   }

   public static int[] getRGB(int c) {
      return new int[]{red(c), green(c), blue(c)};
   }

   public static float[] getRGBAf(int c) {
      return new float[]{redf(c), greenf(c), bluef(c), alphaf(c)};
   }

   public static float[] getRGBf(int c) {
      return new float[]{redf(c), greenf(c), bluef(c)};
   }

   public static int getColor(float red, float green, float blue, float alpha) {
      return getColor(Math.round(red * 255.0F), Math.round(green * 255.0F), Math.round(blue * 255.0F), Math.round(alpha * 255.0F));
   }

   public static int getColor(int red, int green, int blue, float alpha) {
      return getColor(red, green, blue, Math.round(alpha * 255.0F));
   }

   public static int getColor(float red, float green, float blue) {
      return getColor(red, green, blue, 1.0F);
   }

   public static int getColor(int brightness, int alpha) {
      return getColor(brightness, brightness, brightness, alpha);
   }

   public static int getColor(int brightness, float alpha) {
      return getColor(brightness, Math.round(alpha * 255.0F));
   }

   public static int getColor(int brightness) {
      return getColor(brightness, brightness, brightness);
   }

   public static int replAlpha(int color, int alpha) {
      return getColorRaw(red(color), green(color), blue(color), alpha);
   }

   public static int replAlpha(int color, float alpha) {
      return getColorRaw(red(color), green(color), blue(color), Math.round(alpha * 255.0F));
   }

   public static int multAlpha(int color, float percent01) {
      return getColorRaw(red(color), green(color), blue(color), Math.round((float)alpha(color) * percent01));
   }

   public static int toGray(int color, float percent01) {
      int r = red(color);
      int g = green(color);
      int b = blue(color);
      int a = alpha(color);
      int target = 128;
      r = Math.round((float)r + (float)(target - r) * percent01);
      g = Math.round((float)g + (float)(target - g) * percent01);
      b = Math.round((float)b + (float)(target - b) * percent01);
      float darkFactor = percent01 / 2.0F;
      r = Math.round((float)r * darkFactor);
      g = Math.round((float)g * darkFactor);
      b = Math.round((float)b * darkFactor);
      return getColorRaw(r, g, b, a);
   }

   public static int multDark(int color, float percent01) {
      return getColorRaw(
         Math.round((float)red(color) * percent01), Math.round((float)green(color) * percent01), Math.round((float)blue(color) * percent01), alpha(color)
      );
   }

   public static int multBright(int color, float percent01) {
      return getColorRaw(
         Math.min(255, Math.round((float)red(color) / percent01)),
         Math.min(255, Math.round((float)green(color) / percent01)),
         Math.min(255, Math.round((float)blue(color) / percent01)),
         alpha(color)
      );
   }

   public static int overCol(int color1, int color2, float percent01) {
      float percent = class_3532.method_15363(percent01, 0.0F, 1.0F);
      return getColorRaw(
         Interpolator.lerp(red(color1), red(color2), (double)percent),
         Interpolator.lerp(green(color1), green(color2), (double)percent),
         Interpolator.lerp(blue(color1), blue(color2), (double)percent),
         Interpolator.lerp(alpha(color1), alpha(color2), (double)percent)
      );
   }

   public static int overCol(int color1, int color2) {
      return overCol(color1, color2, 0.5F);
   }

   public static int[] genGradientForText(int color1, int color2, int length) {
      int[] gradient = new int[length];

      for (int i = 0; i < length; i++) {
         float pc = (float)i / (float)(length - 1);
         gradient[i] = overCol(color1, color2, pc);
      }

      return gradient;
   }

   public static int interpolate(int color1, int color2, double amount) {
      amount = (double)((float)class_3532.method_15350(amount, 0.0, 1.0));
      return getColorRaw(
         Interpolator.lerp(red(color1), red(color2), amount),
         Interpolator.lerp(green(color1), green(color2), amount),
         Interpolator.lerp(blue(color1), blue(color2), amount),
         Interpolator.lerp(alpha(color1), alpha(color2), amount)
      );
   }

   public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      float hue = (float)angle / 360.0F;
      int color = Color.HSBtoRGB(hue, saturation, brightness);
      return getColor(red(color), green(color), blue(color), Math.round(opacity * 255.0F));
   }

   public static int fade(int speed, int index, int first, int second) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      angle = angle >= 180 ? 360 - angle : angle;
      return overCol(first, second, (float)angle / 180.0F);
   }

   public static int fade(int index) {
      if (clientResolving) {
         return fade(10, index, getClientColor1(index), multBright(getClientColor1(index), 0.75F));
      } else if (ClientTheme.INSTANCE != null) {
         clientResolving = true;

         int var1;
         try {
            var1 = ClientTheme.INSTANCE.getAccentColor(index);
         } finally {
            clientResolving = false;
         }

         return var1;
      } else {
         return fade(10, index, getClientColor1(index), multBright(getClientColor1(index), 0.75F));
      }
   }

   public static int getClientColor1(int index) {
      return Hud.getVisualsColor();
   }

   public static int getRectColor(float alpha) {
      return replAlpha(ThemeColor.getBackgroundColor(), alpha * Hud.getAlpha());
   }

   public static int getClientColor(int index) {
      return Hud.getHudColor();
   }

   public static int gradient(int c1, int c2, int c3, int c4, int c5, int index, int speed) {
      double time = (double)((System.currentTimeMillis() + (long)index * 50L) % (long)speed);
      float ratio = (float)(time / (double)speed);
      float localRatio;
      int color2;
      int color1;
      if (ratio < 0.2F) {
         color1 = c1;
         color2 = c2;
         localRatio = ratio / 0.2F;
      } else if (ratio < 0.4F) {
         color1 = c2;
         color2 = c3;
         localRatio = (ratio - 0.2F) / 0.2F;
      } else if (ratio < 0.6F) {
         color1 = c3;
         color2 = c4;
         localRatio = (ratio - 0.4F) / 0.2F;
      } else if (ratio < 0.8F) {
         color1 = c4;
         color2 = c5;
         localRatio = (ratio - 0.6F) / 0.2F;
      } else {
         color1 = c5;
         color2 = c1;
         localRatio = (ratio - 0.8F) / 0.2F;
      }

      return getHSBColor(color1, color2, localRatio);
   }

   private static int getHSBColor(int color1, int color2, float ratio) {
      float[] hsb1 = Color.RGBtoHSB(color1 >> 16 & 0xFF, color1 >> 8 & 0xFF, color1 & 0xFF, null);
      float[] hsb2 = Color.RGBtoHSB(color2 >> 16 & 0xFF, color2 >> 8 & 0xFF, color2 & 0xFF, null);
      float smooth = (float)(1.0 - Math.cos((double)ratio * Math.PI)) / 2.0F;
      float h = interpolateFloat(hsb1[0], hsb2[0], smooth);
      float s = interpolateFloat(hsb1[1], hsb2[1], smooth);
      float b = interpolateFloat(hsb1[2], hsb2[2], smooth);
      return Color.HSBtoRGB(h, s, b);
   }

   private static float interpolateFloat(float f1, float f2, float ratio) {
      if ((double)Math.abs(f2 - f1) > 0.5) {
         if (f2 > f1) {
            f1++;
         } else {
            f2++;
         }
      }

      float res;
      return (res = f1 + (f2 - f1) * ratio) > 1.0F ? res - 1.0F : res;
   }

   private static int interpolateChannel(int b1, int b2, float fraction) {
      return (int)((float)b1 + (float)(b2 - b1) * fraction);
   }

   public static int getColorRectMain(float alpha) {
      return replAlpha(getClientColor(1), alpha);
   }

   public static int gradient(int first, int second, int third, int index, int speed) {
      float angle = ((float)System.currentTimeMillis() / (float)speed + (float)index) % 360.0F / 360.0F;
      int color = angle < 0.5F ? interpolate(first, second, (double)(angle * 2.0F)) : interpolate(second, third, (double)((angle - 0.5F) * 2.0F));
      float[] hs = rgba(color);
      float[] hsb = Color.RGBtoHSB((int)(hs[0] * 255.0F), (int)(hs[1] * 255.0F), (int)(hs[2] * 255.0F), null);
      hsb[1] *= 1.5F;
      hsb[1] = Math.min(hsb[1], 1.0F);
      return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
   }

   public static int gradient(int start, int end, int index, int speed) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      int color = interpolate(start, end, (double)class_3532.method_15363((float)angle / 180.0F - 1.0F, 0.0F, 1.0F));
      float[] hs = rgba(color);
      float[] hsb = Color.RGBtoHSB((int)(hs[0] * 255.0F), (int)(hs[1] * 255.0F), (int)(hs[2] * 255.0F), null);
      hsb[1] *= 1.5F;
      hsb[1] = Math.min(hsb[1], 1.0F);
      return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
   }

   public static int getColor(int red, int green, int blue, int alpha) {
      ColorUtil.ColorKey key = new ColorUtil.ColorKey(red, green, blue, alpha);
      ColorUtil.CacheEntry cacheEntry = colorCache.computeIfAbsent(key, k -> {
         ColorUtil.CacheEntry newEntry = new ColorUtil.CacheEntry(k, computeColor(red, green, blue, alpha), 60000L);
         cleanupQueue.offer(newEntry);
         return newEntry;
      });
      return cacheEntry.getColor();
   }

   public static int getColorRaw(int red, int green, int blue, int alpha) {
      return class_3532.method_15340(alpha, 0, 255) << 24
         | class_3532.method_15340(red, 0, 255) << 16
         | class_3532.method_15340(green, 0, 255) << 8
         | class_3532.method_15340(blue, 0, 255);
   }

   public static int getColorRaw(float red, float green, float blue, float alpha) {
      return class_3532.method_15340(Math.round(alpha), 0, 255) << 24
         | class_3532.method_15340(Math.round(red), 0, 255) << 16
         | class_3532.method_15340(Math.round(green), 0, 255) << 8
         | class_3532.method_15340(Math.round(blue), 0, 255);
   }

   public static int getColor(int red, int green, int blue) {
      return getColor(red, green, blue, 255);
   }

   private static int computeColor(int red, int green, int blue, int alpha) {
      return class_3532.method_15340(alpha, 0, 255) << 24
         | class_3532.method_15340(red, 0, 255) << 16
         | class_3532.method_15340(green, 0, 255) << 8
         | class_3532.method_15340(blue, 0, 255);
   }

   private static String generateKey(int red, int green, int blue, int alpha) {
      return red + "," + green + "," + blue + "," + alpha;
   }

   public static void shutdownCacheCleaner() {
      cacheCleaner.shutdown();
   }

   @Generated
   private ColorUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      cacheCleaner.scheduleWithFixedDelay(() -> {
         for (ColorUtil.CacheEntry entry = cleanupQueue.poll(); entry != null; entry = cleanupQueue.poll()) {
            if (entry.isExpired()) {
               colorCache.remove(entry.getKey());
            }
         }
      }, 0L, 1L, TimeUnit.SECONDS);
   }

   @Environment(EnvType.CLIENT)
   private static class CacheEntry implements Delayed {
      private final ColorUtil.ColorKey key;
      private final int color;
      private final long expirationTime;

      CacheEntry(ColorUtil.ColorKey key, int color, long ttl) {
         this.key = key;
         this.color = color;
         this.expirationTime = System.currentTimeMillis() + ttl;
      }

      @Override
      public long getDelay(TimeUnit unit) {
         long delay = this.expirationTime - System.currentTimeMillis();
         return unit.convert(delay, TimeUnit.MILLISECONDS);
      }

      public int compareTo(Delayed other) {
         return other instanceof ColorUtil.CacheEntry ? Long.compare(this.expirationTime, ((ColorUtil.CacheEntry)other).expirationTime) : 0;
      }

      public boolean isExpired() {
         return System.currentTimeMillis() > this.expirationTime;
      }

      @Generated
      public ColorUtil.ColorKey getKey() {
         return this.key;
      }

      @Generated
      public int getColor() {
         return this.color;
      }

      @Generated
      public long getExpirationTime() {
         return this.expirationTime;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class ColorKey {
      final int red;
      final int green;
      final int blue;
      final int alpha;

      @Generated
      public int getRed() {
         return this.red;
      }

      @Generated
      public int getGreen() {
         return this.green;
      }

      @Generated
      public int getBlue() {
         return this.blue;
      }

      @Generated
      public int getAlpha() {
         return this.alpha;
      }

      @Generated
      public ColorKey(int red, int green, int blue, int alpha) {
         this.red = red;
         this.green = green;
         this.blue = blue;
         this.alpha = alpha;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ColorUtil.ColorKey other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else if (this.getRed() != other.getRed()) {
            return false;
         } else if (this.getGreen() != other.getGreen()) {
            return false;
         } else {
            return this.getBlue() != other.getBlue() ? false : this.getAlpha() == other.getAlpha();
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ColorUtil.ColorKey;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + this.getRed();
         result = result * 59 + this.getGreen();
         result = result * 59 + this.getBlue();
         return result * 59 + this.getAlpha();
      }
   }
}
