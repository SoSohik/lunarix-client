package ru.mentions.maintenance.mem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ru.mentions.maintenance.DiagLog;

public final class FabricEntrypoints {
   private FabricEntrypoints() {
   }

   public static Map<String, List<String>> read(MemoryJarIndex index) {
      byte[] json = index.fabricModJson();
      if (json == null) {
         return Collections.emptyMap();
      } else {
         Map<String, List<String>> result = new LinkedHashMap<>();

         try {
            JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject entrypoints = root.getAsJsonObject("entrypoints");
            if (entrypoints == null) {
               return result;
            }

            for (Entry<String, JsonElement> entry : entrypoints.entrySet()) {
               List<String> classes = new ArrayList<>();
               JsonElement value = entry.getValue();
               if (value.isJsonArray()) {
                  collect(value.getAsJsonArray(), classes);
               } else if (value.isJsonPrimitive()) {
                  classes.add(value.getAsString());
               } else if (value.isJsonObject()) {
                  collectObject(value.getAsJsonObject(), classes);
               }

               result.put(entry.getKey(), classes);
            }
         } catch (Throwable var9) {
            DiagLog.fail("mem.entrypoints", var9);
         }

         return result;
      }
   }

   private static void collect(JsonArray array, List<String> out) {
      for (JsonElement element : array) {
         if (element.isJsonPrimitive()) {
            out.add(element.getAsString());
         } else if (element.isJsonObject()) {
            collectObject(element.getAsJsonObject(), out);
         }
      }
   }

   private static void collectObject(JsonObject object, List<String> out) {
      JsonElement value = object.get("value");
      if (value != null && value.isJsonPrimitive()) {
         out.add(value.getAsString());
      }
   }

   public static List<String> mixinConfigs(MemoryJarIndex index) {
      byte[] json = index.fabricModJson();
      if (json == null) {
         return Collections.emptyList();
      } else {
         List<String> configs = new ArrayList<>();

         try {
            JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonElement mixins = root.get("mixins");
            if (mixins == null) {
               return configs;
            }

            if (mixins.isJsonArray()) {
               for (JsonElement element : mixins.getAsJsonArray()) {
                  if (element.isJsonPrimitive()) {
                     configs.add(element.getAsString());
                  } else if (element.isJsonObject()) {
                     JsonElement config = element.getAsJsonObject().get("config");
                     if (config != null && config.isJsonPrimitive()) {
                        configs.add(config.getAsString());
                     }
                  }
               }
            } else if (mixins.isJsonPrimitive()) {
               configs.add(mixins.getAsString());
            }
         } catch (Throwable var8) {
            DiagLog.fail("mem.mixins", var8);
         }

         return configs;
      }
   }

   public static List<Object> instantiate(MemoryClassLoader loader, Map<String, List<String>> entrypoints, String key) {
      List<String> names = entrypoints.get(key);
      if (names != null && !names.isEmpty()) {
         List<Object> instances = new ArrayList<>();

         for (String name : names) {
            try {
               Class<?> type = Class.forName(name, true, loader);
               instances.add(type.getDeclaredConstructor().newInstance());
            } catch (Throwable var8) {
               DiagLog.fail("mem.instantiate:" + name, var8);
            }
         }

         return instances;
      } else {
         return Collections.emptyList();
      }
   }
}
