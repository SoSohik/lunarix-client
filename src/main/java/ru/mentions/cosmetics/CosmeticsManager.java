package ru.mentions.cosmetics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import ru.mentions.Client;
import ru.mentions.cosmetics.anim.GeoAnimation;
import ru.mentions.cosmetics.anim.GeoAnimationParser;
import ru.mentions.cosmetics.geo.GeoModel;
import ru.mentions.cosmetics.geo.GeoModelParser;

public final class CosmeticsManager {
   private static final String MODEL_ROOT = "/assets/client/cosmetics/models/";
   private static final String TEXTURE_PATH = "textures/cosmetics/cosmetic_";
   private static final int MAX_INDEX = 256;
   private static final String FILE_NAME = "cosmetics.json";
   private static final String NAMESPACE = "client";
   private static final CosmeticsManager INSTANCE = new CosmeticsManager();
   private final List<CosmeticEntry> entries = new ArrayList<>();
   private final Map<CosmeticType, CosmeticEntry> worn = new EnumMap<>(CosmeticType.class);
   private boolean loaded;

   private CosmeticsManager() {
   }

   public static CosmeticsManager get() {
      return INSTANCE;
   }

   public List<CosmeticEntry> entries() {
      return Collections.unmodifiableList(this.entries);
   }

   public List<CosmeticEntry> entries(CosmeticType type) {
      List<CosmeticEntry> out = new ArrayList<>();

      for (CosmeticEntry entry : this.entries) {
         if (entry.type == type) {
            out.add(entry);
         }
      }

      return out;
   }

   public CosmeticEntry worn(CosmeticType type) {
      return this.worn.get(type);
   }

   public boolean isWorn(CosmeticEntry entry) {
      return entry != null && this.worn.get(entry.type) == entry;
   }

   public boolean toggle(CosmeticEntry entry) {
      if (entry == null) {
         return false;
      } else if (this.isWorn(entry)) {
         this.worn.remove(entry.type);
         this.save();
         return false;
      } else {
         this.worn.put(entry.type, entry);
         this.save();
         return true;
      }
   }

   public void clear() {
      this.worn.clear();
      this.save();
   }

   public void load() {
      if (!this.loaded) {
         this.loaded = true;

         for (int index = 0; index < 256; index++) {
            CosmeticEntry entry = this.read(index);
            if (entry != null) {
               this.entries.add(entry);
            }
         }

         this.readSelection();
      }
   }

   private CosmeticEntry read(int index) {
      String name = "cosmetic_" + index;
      if (CosmeticsManager.class.getResource("/assets/client/textures/cosmetics/cosmetic_" + index + ".png") == null) {
         return null;
      } else {
         try {
            CosmeticEntry var29;
            try (InputStream stream = CosmeticsManager.class.getResourceAsStream("/assets/client/cosmetics/models/" + name + ".json")) {
               if (stream == null) {
                  return null;
               }

               Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

               JsonObject root;
               try {
                  root = JsonParser.parseReader(reader).getAsJsonObject();
               } catch (Throwable var22) {
                  try {
                     reader.close();
                  } catch (Throwable var21) {
                     var22.addSuppressed(var21);
                  }

                  throw var22;
               }

               reader.close();
               if (root == null) {
                  return null;
               }

               int[] size = pngSize(root);
               String displayName = clean(root.has("name") ? root.get("name").getAsString() : name);
               String author = root.has("author") ? root.get("author").getAsString() : "";
               int id = root.has("id") ? root.get("id").getAsInt() : index;
               CosmeticType type = classify(root, displayName);
               GeoModel model = null;
               Map<String, GeoAnimation> animations = new LinkedHashMap<>();
               if (root.has("model") && root.get("model").isJsonObject()) {
                  model = GeoModelParser.parse(root.get("model").toString());
                  if (root.has("animation") && root.get("animation").isJsonObject()) {
                     animations.putAll(GeoAnimationParser.parse(root.get("animation").toString()));
                  }
               }

               int frames = 1;
               int frameWidth = 0;
               int frameHeight = 0;
               int frameTicks = 0;
               if (root.has("capeAnimation") && root.get("capeAnimation").isJsonObject()) {
                  JsonObject cape = root.getAsJsonObject("capeAnimation");
                  frames = Math.max(1, optInt(cape, "frameCount", 1));
                  frameWidth = optInt(cape, "frameWidth", 0);
                  frameHeight = optInt(cape, "frameHeight", 0);
                  frameTicks = Math.max(1, optInt(cape, "frameTime", 3));
                  if (frameHeight > 0 && size[1] > frameHeight) {
                     frames = Math.max(frames, size[1] / frameHeight);
                  }
               }

               int textureFrames = 1;
               int textureFrameHeight = 0;
               int textureFrameTicks = 1;
               if (root.has("textureAnimation") && root.get("textureAnimation").isJsonObject()) {
                  JsonObject strip = root.getAsJsonObject("textureAnimation");
                  textureFrameHeight = optInt(strip, "frameHeight", 0);
                  if (textureFrameHeight > 0 && size[1] >= textureFrameHeight) {
                     textureFrames = Math.max(1, Math.min(optInt(strip, "frameCount", 1), size[1] / textureFrameHeight));
                     textureFrameTicks = Math.max(1, optInt(strip, "frameTime", 1));
                  }
               }

               var29 = new CosmeticEntry(
                  index,
                  id,
                  displayName,
                  author,
                  type,
                  class_2960.method_60655("client", "textures/cosmetics/cosmetic_" + index + ".png"),
                  size[0],
                  size[1],
                  model,
                  animations,
                  optInt(root, "pos", type == CosmeticType.HAT ? 2 : 1),
                  optFloat(root, "x", 0.0F),
                  optFloat(root, "y", 0.0F),
                  optFloat(root, "z", 0.0F),
                  optFloat(root, "yaw", 0.0F),
                  optFloat(root, "pitch", 0.0F),
                  optFloat(root, "roll", 0.0F),
                  optFloat(root, "scale", 1.0F),
                  optFloat(root, "previewY", 0.2F),
                  optFloat(root, "previewScale", 1.0F),
                  frames,
                  frameWidth,
                  frameHeight,
                  frameTicks,
                  textureFrames,
                  textureFrameHeight,
                  textureFrameTicks
               );
            }

            return var29;
         } catch (Exception var24) {
            return null;
         }
      }
   }

   private static CosmeticType classify(JsonObject root, String displayName) {
      String type = root.has("type") ? root.get("type").getAsString().toLowerCase(Locale.ROOT) : "";
      String name = displayName.toLowerCase(Locale.ROOT);
      if (type.equals("cape") || name.contains("cape") || name.contains("плащ")) {
         return CosmeticType.CAPE;
      } else if (type.equals("pet") || name.contains("pet") || name.contains("питом")) {
         return CosmeticType.PET;
      } else if (type.equals("wings") || name.contains("wing")) {
         return CosmeticType.WINGS;
      } else {
         return !type.equals("bodywear") && !name.contains("backpack") && !name.contains("crossbody") && !name.contains("back") && !name.contains("рюкзак")
            ? CosmeticType.HAT
            : CosmeticType.BACKPACK;
      }
   }

   private static String clean(String raw) {
      String out = raw.trim();
      int cut = out.indexOf(95);
      if (cut > 0 && cut < 14) {
         out = out.substring(cut + 1);
      }

      return out.trim();
   }

   private static int[] pngSize(JsonObject root) {
      int[] fallback = new int[]{64, 64};
      if (!root.has("texture")) {
         return fallback;
      } else {
         try {
            byte[] bytes = Base64.getDecoder().decode(root.get("texture").getAsString());
            if (bytes.length >= 24 && bytes[0] == -119 && bytes[1] == 80 && bytes[2] == 78 && bytes[3] == 71) {
               int width = readInt(bytes, 16);
               int height = readInt(bytes, 20);
               return new int[]{width <= 0 ? 64 : width, height <= 0 ? 64 : height};
            } else {
               return fallback;
            }
         } catch (Exception var5) {
            return fallback;
         }
      }
   }

   private static int readInt(byte[] bytes, int offset) {
      return (bytes[offset] & 0xFF) << 24 | (bytes[offset + 1] & 0xFF) << 16 | (bytes[offset + 2] & 0xFF) << 8 | bytes[offset + 3] & 0xFF;
   }

   private static int optInt(JsonObject json, String key, int fallback) {
      return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : fallback;
   }

   private static float optFloat(JsonObject json, String key, float fallback) {
      return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsFloat() : fallback;
   }

   private Path file() {
      try {
         return Client.get().configManager().getConfigDir().resolve("cosmetics.json");
      } catch (Exception var2) {
         return null;
      }
   }

   public void save() {
      Path path = this.file();
      if (path != null) {
         try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();

            for (Entry<CosmeticType, CosmeticEntry> entry : this.worn.entrySet()) {
               if (entry.getValue() != null) {
                  root.addProperty(entry.getKey().name(), entry.getValue().index);
               }
            }

            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
               writer.write(root.toString());
            }
         } catch (Exception var8) {
         }
      }
   }

   private void readSelection() {
      Path path = this.file();
      if (path != null && Files.exists(path)) {
         try (Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            for (CosmeticType type : CosmeticType.values()) {
               if (root.has(type.name())) {
                  int index = root.get(type.name()).getAsInt();

                  for (CosmeticEntry entry : this.entries) {
                     if (entry.index == index) {
                        this.worn.put(type, entry);
                        break;
                     }
                  }
               }
            }
         } catch (Exception var13) {
         }
      }
   }
}
