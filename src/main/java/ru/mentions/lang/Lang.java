package ru.mentions.lang;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Lang {
   private static final Path LOCAL_LANG_FILE = Path.of("lunarix/language.txt");
   private static final Path FALLBACK_LANG_FILE = Path.of("C:/lunarix/client1_21_11/language.txt");
   private static final Map<String, String> DICT_EN = Translations.build();
   private static final Map<String, String> DICT_UA = Translations.buildUa();
   private static Lang.Language current = Lang.Language.UKRAINIAN;

   private Lang() {
   }

   public static void init() {
      current = readSavedLanguage();
   }

   public static Lang.Language getLanguage() {
      return current;
   }

   public static boolean isUkrainian() {
      return current == Lang.Language.UKRAINIAN;
   }

   public static boolean isRussian() {
      return current == Lang.Language.RUSSIAN;
   }

   public static boolean isEnglish() {
      return current == Lang.Language.ENGLISH;
   }

   public static String tag() {
      return current == Lang.Language.UKRAINIAN ? "UA" : (current == Lang.Language.ENGLISH ? "EN" : "RU");
   }

   public static void setLanguage(Lang.Language language) {
      current = language != null ? language : Lang.Language.UKRAINIAN;
      saveLanguage();
   }

   public static void toggle() {
      if (current == Lang.Language.UKRAINIAN) {
         setLanguage(Lang.Language.ENGLISH);
      } else if (current == Lang.Language.ENGLISH) {
         setLanguage(Lang.Language.RUSSIAN);
      } else {
         setLanguage(Lang.Language.UKRAINIAN);
      }
   }

   public static void reloadDict() {
   }

   public static String tr(String text) {
      if (text == null) {
         return null;
      } else {
         if (current == Lang.Language.UKRAINIAN) {
            String res = DICT_UA.get(text);
            if (res != null) {
               return res;
            }
         } else if (current == Lang.Language.ENGLISH) {
            String res = DICT_EN.get(text);
            if (res != null) {
               return res;
            }
         }

         return text;
      }
   }

   public static String pick(String ua, String ru, String en) {
      if (current == Lang.Language.UKRAINIAN && ua != null && !ua.isEmpty()) {
         return ua;
      } else if (current == Lang.Language.ENGLISH && en != null && !en.isEmpty()) {
         return en;
      } else {
         return ru != null ? ru : (ua != null ? ua : en);
      }
   }

   public static String pick(String ru, String en) {
      if (current == Lang.Language.UKRAINIAN) {
         String res = DICT_UA.get(ru);
         return res != null ? res : ru;
      } else if (current == Lang.Language.ENGLISH) {
         return en != null ? en : ru;
      } else {
         return ru != null ? ru : en;
      }
   }

   private static Lang.Language readSavedLanguage() {
      try {
         Path file = Files.exists(LOCAL_LANG_FILE) ? LOCAL_LANG_FILE : (Files.exists(FALLBACK_LANG_FILE) ? FALLBACK_LANG_FILE : null);
         if (file != null) {
            String s = Files.readString(file, StandardCharsets.UTF_8).trim();
            if (!s.equalsIgnoreCase("EN") && !s.equalsIgnoreCase("ENGLISH")) {
               if (!s.equalsIgnoreCase("RU") && !s.equalsIgnoreCase("RUSSIAN")) {
                  if (!s.equalsIgnoreCase("UA") && !s.equalsIgnoreCase("UKRAINIAN") && !s.equalsIgnoreCase("UKRAINE")) {
                     return Lang.Language.UKRAINIAN;
                  }

                  return Lang.Language.UKRAINIAN;
               }

               return Lang.Language.RUSSIAN;
            }

            return Lang.Language.ENGLISH;
         }
      } catch (Exception var2) {
      }

      return Lang.Language.UKRAINIAN;
   }

   private static void saveLanguage() {
      try {
         if (LOCAL_LANG_FILE.getParent() != null) {
            Files.createDirectories(LOCAL_LANG_FILE.getParent());
         }

         Files.writeString(LOCAL_LANG_FILE, current.name(), StandardCharsets.UTF_8);
      } catch (Exception var1) {
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Language {
      UKRAINIAN,
      RUSSIAN,
      ENGLISH;
   }
}
