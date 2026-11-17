package ru.mentions.utils.colors;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;

@Environment(EnvType.CLIENT)
public final class ColorFormatting {
   public static final Pattern PATTERN = Pattern.compile("\\$\\{(rgba|rgb)\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})(?:,(\\d{1,3}))?\\)}|\\$\\{reset}", 2);
   private static final HashMap<String, String> FORMATS = new HashMap<>();

   public static String get(String input) {
      if (input == null || input.isEmpty()) {
         return input;
      } else if (input.indexOf("${") < 0) {
         return input;
      } else {
         String result = input;

         for (Entry<String, String> format : FORMATS.entrySet()) {
            result = result.replace(format.getKey(), format.getValue());
         }

         return result;
      }
   }

   public static String getColor(int red, int green, int blue) {
      return "${rgb(" + red + "," + green + "," + blue + ")}";
   }

   public static String getColor(int red, int green, int blue, int alpha) {
      return "${rgba(" + red + "," + green + "," + blue + "," + alpha + ")}";
   }

   public static String getColor(int color) {
      return getColor(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), ColorUtil.alpha(color));
   }

   public static class_2561 gradient(String message) {
      class_5250 text = class_2561.method_43473();

      for (int i = 0; i < message.length(); i++) {
         int fade = ColorUtil.fade(i);
         text.method_10852(class_2561.method_43470(String.valueOf(message.charAt(i))).method_27694(style -> style.method_36139(fade)));
      }

      return text;
   }

   public static int overColor(int firstColor, int secondColor, float factorPercent) {
      return ColorUtil.overCol(firstColor, secondColor, factorPercent);
   }

   public static String reset() {
      return "${reset}";
   }

   public static String removeFormatting(String text) {
      if (text == null || text.isEmpty()) {
         return text;
      } else {
         return text.indexOf("${") < 0 ? text : PATTERN.matcher(text).replaceAll("");
      }
   }

   public static String typeRGB() {
      return "rgb";
   }

   public static String typeRGBA() {
      return "rgba";
   }

   public static String replaceColor(String text, int red, int green, int blue) {
      return PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement(getColor(red, green, blue)));
   }

   public static String replaceColor(String text, int red, int green, int blue, int alpha) {
      return PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement(getColor(red, green, blue, alpha)));
   }

   public static ColorFormatting.ColorTag parseTag(String text, int index, int defaultColor) {
      if (text == null || index < 0 || index >= text.length() - 1) {
         return null;
      } else if (text.charAt(index) == '$' && text.charAt(index + 1) == '{') {
         Matcher matcher = PATTERN.matcher(text);
         if (!matcher.find(index) || matcher.start() != index) {
            return null;
         } else if (matcher.group().equalsIgnoreCase(reset())) {
            return new ColorFormatting.ColorTag(defaultColor, matcher.end() - index);
         } else {
            String type = matcher.group(1).toLowerCase();
            int red = clamp255(Integer.parseInt(matcher.group(2)));
            int green = clamp255(Integer.parseInt(matcher.group(3)));
            int blue = clamp255(Integer.parseInt(matcher.group(4)));
            int alpha = type.equals(typeRGBA()) && matcher.group(5) != null ? clamp255(Integer.parseInt(matcher.group(5))) : ColorUtil.alpha(defaultColor);
            return new ColorFormatting.ColorTag(ColorUtil.getColor(red, green, blue, alpha), matcher.end() - index);
         }
      } else {
         return null;
      }
   }

   public static String stripForWidth(String text) {
      return text != null && !text.isEmpty() ? removeFormatting(get(text)) : text;
   }

   private static int clamp255(int value) {
      return Math.max(0, Math.min(255, value));
   }

   @Generated
   private ColorFormatting() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      FORMATS.put("${black}", class_124.field_1074.toString());
      FORMATS.put("${dark_blue}", class_124.field_1058.toString());
      FORMATS.put("${dark_green}", class_124.field_1077.toString());
      FORMATS.put("${dark_aqua}", class_124.field_1062.toString());
      FORMATS.put("${dark_red}", class_124.field_1079.toString());
      FORMATS.put("${dark_purple}", class_124.field_1064.toString());
      FORMATS.put("${orange}", class_124.field_1065.toString());
      FORMATS.put("${gray}", class_124.field_1080.toString());
      FORMATS.put("${dark_gray}", class_124.field_1063.toString());
      FORMATS.put("${blue}", class_124.field_1078.toString());
      FORMATS.put("${green}", class_124.field_1060.toString());
      FORMATS.put("${aqua}", class_124.field_1075.toString());
      FORMATS.put("${red}", class_124.field_1061.toString());
      FORMATS.put("${purple}", class_124.field_1076.toString());
      FORMATS.put("${yellow}", class_124.field_1054.toString());
      FORMATS.put("${white}", class_124.field_1068.toString());
      FORMATS.put("${bold}", class_124.field_1067.toString());
      FORMATS.put("${reset}", class_124.field_1070.toString());
   }

   @Environment(EnvType.CLIENT)
   public static record ColorTag(int color, int length) {
   }
}
