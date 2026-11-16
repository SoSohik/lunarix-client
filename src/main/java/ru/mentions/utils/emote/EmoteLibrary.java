package ru.mentions.utils.emote;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class EmoteLibrary implements IMinecraft {
   private static final Logger LOGGER = LoggerFactory.getLogger("client/Emotes");
   private static final List<String> BUILT_IN = List.of(
      "backflip", "clap", "club_penguin_dance", "crying", "kazotsky_kick", "palm", "point", "roblox_potion_dance", "twerk", "waving"
   );
   private static final String ROOT = "/assets/zenith/emotes/";
   private static final String INDEX = "/assets/zenith/emotes/spemotes/index.txt";
   private static final List<EmoteMetadata> ALL = new ArrayList<>();
   private static final Map<String, EmoteMetadata> BY_ID = new LinkedHashMap<>();
   private static boolean loaded = false;

   private EmoteLibrary() {
   }

   public static synchronized void ensureLoaded() {
      if (!loaded) {
         loaded = true;

         for (String id : BUILT_IN) {
            load(id, id);
         }

         try {
            label83: {
               try (InputStream stream = EmoteLibrary.class.getResourceAsStream("/assets/zenith/emotes/spemotes/index.txt")) {
                  if (stream != null) {
                     BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

                     String line;
                     while ((line = reader.readLine()) != null) {
                        String path = line.trim();
                        if (!path.isEmpty()) {
                           String id = path.contains("/") ? path.substring(path.lastIndexOf(47) + 1) : path;
                           load(id, path);
                        }
                     }
                     break label83;
                  }
               }

               return;
            }
         } catch (Throwable var7) {
            LOGGER.warn("[Emotes] load failed: {}", var7.toString());
         }
      }
   }

   private static String displayName(JsonObject json, String fallback) {
      try {
         JsonElement element2 = json.get("name");
         if (element2 == null || element2.isJsonNull()) {
            return fallback;
         }

         if (element2.isJsonPrimitive()) {
            return element2.getAsString();
         }

         JsonObject object;
         if (element2.isJsonObject() && (object = element2.getAsJsonObject()).has("fallback") && object.get("fallback").isJsonPrimitive()) {
            return object.get("fallback").getAsString();
         }
      } catch (Throwable var4) {
      }

      return fallback;
   }

   private static void logLoaded() {
      LOGGER.info("[Emotes] loaded {} emotes", ALL.size());
   }

   private static void load(String id, String path) {
      if (!BY_ID.containsKey(id)) {
         try {
            try (InputStream stream = EmoteLibrary.class.getResourceAsStream("/assets/zenith/emotes/" + path + ".json")) {
               if (stream != null) {
                  JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                  String display = displayName(json, id);
                  Map animations = UniversalAnimLoader.loadAnimations(json);
                  if (animations.isEmpty()) {
                     LOGGER.warn("[Emotes] no animations in {}", path);
                     return;
                  }

                  Animation animation = (Animation)animations.values().iterator().next();
                  class_2960 icon = class_2960.method_60655("zenith", "emotes/" + path + ".png");
                  EmoteMetadata meta = new EmoteMetadata(id, display, icon, animation);
                  BY_ID.put(id, meta);
                  ALL.add(meta);
                  return;
               }
            }
         } catch (Throwable var11) {
            LOGGER.warn("[Emotes] load failed: {}", var11.toString());
         }
      }
   }

   public static List<EmoteMetadata> all() {
      ensureLoaded();
      return ALL;
   }

   public static EmoteMetadata byId(String id) {
      ensureLoaded();
      if (id == null) {
         return null;
      }

      EmoteMetadata exact = BY_ID.get(id);
      if (exact != null) {
         return exact;
      }

      for (EmoteMetadata meta : ALL) {
         if (id.equalsIgnoreCase(meta.id())) {
            return meta;
         }
      }

      return byDisplayName(id);
   }

   public static EmoteMetadata byDisplayName(String name) {
      ensureLoaded();
      if (name == null) {
         return null;
      }

      for (EmoteMetadata meta : ALL) {
         if (meta.displayName() != null && name.equalsIgnoreCase(meta.displayName())) {
            return meta;
         }
      }

      return null;
   }

   public static EmoteMetadata resolve(String key) {
      return byId(key);
   }

   public static List<String> ids() {
      ensureLoaded();
      return new ArrayList<>(BY_ID.keySet());
   }

   public static int size() {
      ensureLoaded();
      return ALL.size();
   }
}
