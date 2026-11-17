package ru.mentions.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import ru.mentions.lang.Lang;

public final class RussianUi {
   private static final Map<String, String> TEXT = load();

   private RussianUi() {
   }

   public static String text(String value) {
      if (value == null) {
         return null;
      } else {
         if (Lang.isUkrainian()) {
            int pipeIdx = value.indexOf(124);
            if (pipeIdx != -1) {
               String prefix = value.substring(0, pipeIdx + 1);
               String rest = value.substring(pipeIdx + 1).trim();
               String uaRest = Lang.tr(rest);
               if (uaRest != null && !uaRest.equals(rest)) {
                  return prefix + " " + uaRest;
               }
            }

            String ua = Lang.tr(value);
            if (ua != null && !ua.equals(value)) {
               return ua;
            }
         }

         return TEXT.getOrDefault(value, value);
      }
   }

   private static Map<String, String> load() {
      Properties english = read("en.language");
      Properties russian = read("ru.language");
      Map<String, String> result = new HashMap<>();

      for (String key : new TreeSet<>(english.stringPropertyNames())) {
         String translated = russian.getProperty(key);
         if (translated != null) {
            result.putIfAbsent(english.getProperty(key), translated);
         }
      }

      Properties labels = read("ui-ru.language");

      for (String keyx : labels.stringPropertyNames()) {
         result.put(keyx, labels.getProperty(keyx));
      }

      return Map.copyOf(result);
   }

   private static Properties read(String name) {
      Properties result = new Properties();

      try (InputStream stream = RussianUi.class.getResourceAsStream("/assets/zenith/languages/" + name)) {
         if (stream != null) {
            result.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
         }
      } catch (IOException var7) {
      }

      return result;
   }
}
