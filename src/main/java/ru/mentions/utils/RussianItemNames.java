package ru.mentions.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_7417;
import net.minecraft.class_9334;

@Environment(EnvType.CLIENT)
public final class RussianItemNames {
   private static final Map<String, String> TRANSLATIONS = load();

   private RussianItemNames() {
   }

   private static Map<String, String> load() {
      try (InputStream stream = RussianItemNames.class.getResourceAsStream("/assets/zenith/languages/ru_items.json")) {
         if (stream != null) {
            Map<String, String> map = (Map<String, String>)new Gson()
               .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), (new TypeToken<Map<String, String>>() {
               }).getType());
            if (map != null) {
               return map;
            }
         }

         return Collections.emptyMap();
      } catch (Exception var5) {
         return Collections.emptyMap();
      }
   }

   public static String get(String key) {
      return key == null ? null : TRANSLATIONS.get(key);
   }

   public static String getRussianName(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_2561 customName = (class_2561)stack.method_58694(class_9334.field_49631);
         if (customName != null) {
            String name = customName.getString().trim();
            if (!name.isEmpty()) {
               return name;
            }
         }

         class_2561 defaultName = stack.method_7964();
         if (defaultName != null) {
            class_7417 content = defaultName.method_10851();
            if (content instanceof class_2588) {
               String key = ((class_2588)content).method_11022();
               String ru = TRANSLATIONS.get(key);
               if (ru != null && !ru.trim().isEmpty()) {
                  return ru;
               }
            }
         }

         class_1792 item = stack.method_7909();
         if (item != null) {
            String key = item.method_7876();
            String ru = TRANSLATIONS.get(key);
            if (ru != null && !ru.trim().isEmpty()) {
               return ru;
            }
         }

         return defaultName != null ? defaultName.getString() : "";
      } else {
         return "";
      }
   }
}
