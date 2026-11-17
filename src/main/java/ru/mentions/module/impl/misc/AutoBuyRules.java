package ru.mentions.module.impl.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public final class AutoBuyRules {
   private static final String FILE_NAME = "autobuy.json";
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final AutoBuyRules INSTANCE = new AutoBuyRules();
   private final List<AutoBuyRule> rules = new ArrayList<>();
   private boolean loaded = false;

   public static AutoBuyRules get() {
      return INSTANCE;
   }

   public List<AutoBuyRule> all() {
      this.load();
      return this.rules;
   }

   public AutoBuyRule add(String name, int maxPrice, int minQuantity) {
      return this.add(name, maxPrice, minQuantity, "");
   }

   public AutoBuyRule add(String name, int maxPrice, int minQuantity, String itemId) {
      this.load();
      String clean = name == null ? "" : name.trim();
      String id = itemId == null ? "" : itemId;

      for (AutoBuyRule rule : this.rules) {
         if (rule != null && sameItem(rule, clean, id)) {
            rule.set(clean, maxPrice, minQuantity);
            if ((rule.itemId == null || rule.itemId.isEmpty()) && !id.isEmpty()) {
               rule.itemId = id;
            }

            this.save();
            return rule;
         }
      }

      AutoBuyRule rulex = new AutoBuyRule(clean, maxPrice, minQuantity);
      rulex.itemId = id;
      this.rules.add(rulex);
      this.save();
      return rulex;
   }

   public AutoBuyRule merge(AutoBuyRule preset) {
      this.load();
      if (preset == null) {
         return null;
      } else {
         String clean = preset.name == null ? "" : preset.name.trim();

         for (AutoBuyRule rule : this.rules) {
            if (rule != null && sameItem(rule, clean, preset.itemId)) {
               if (rule.maxPrice <= 0 && preset.maxPrice > 0) {
                  rule.set(clean, preset.maxPrice, preset.minQuantity);
               }

               if ((rule.itemId == null || rule.itemId.isEmpty()) && preset.itemId != null) {
                  rule.itemId = preset.itemId;
               }

               if (rule.aliases == null || rule.aliases.isEmpty()) {
                  rule.aliases = preset.aliases;
               }

               return rule;
            }
         }

         preset.set(clean, preset.maxPrice, preset.minQuantity);
         this.rules.add(preset);
         return preset;
      }
   }

   public boolean install(AutoBuyRule preset) {
      this.load();
      if (preset != null && preset.hasName()) {
         String clean = preset.name == null ? "" : preset.name.trim();

         for (AutoBuyRule rule : this.rules) {
            if (rule != null && sameItem(rule, clean, preset.itemId)) {
               return false;
            }
         }

         preset.set(clean, preset.maxPrice, preset.minQuantity);
         this.rules.add(preset);
         return true;
      } else {
         return false;
      }
   }

   public void setPrice(AutoBuyRule rule, int maxPrice) {
      this.load();
      if (rule != null) {
         rule.maxPrice = Math.max(0, maxPrice);
         this.save();
      }
   }

   public void remove(AutoBuyRule rule) {
      this.load();
      if (this.rules.remove(rule)) {
         this.save();
      }
   }

   private static boolean sameItem(AutoBuyRule rule, String name, String itemId) {
      return name.equalsIgnoreCase(rule.name) ? true : itemId != null && !itemId.isEmpty() && itemId.equalsIgnoreCase(rule.itemId);
   }

   public int size() {
      return this.all().size();
   }

   public void load() {
      if (!this.loaded) {
         this.loaded = true;
         Path path = this.file();
         if (path != null && Files.exists(path)) {
            try {
               try (Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
                  JsonElement root = JsonParser.parseReader(reader);
                  if (!root.isJsonObject()) {
                     return;
                  }

                  JsonArray array = root.getAsJsonObject().getAsJsonArray("rules");
                  if (array != null) {
                     for (JsonElement element : array) {
                        if (element.isJsonObject()) {
                           AutoBuyRule rule = (AutoBuyRule)GSON.fromJson(element, AutoBuyRule.class);
                           if (rule != null) {
                              rule.set(rule.name, rule.maxPrice, rule.minQuantity);
                              if (rule.aliases == null) {
                                 rule.aliases = new ArrayList<>();
                              }

                              if (rule.hasName()) {
                                 this.rules.add(rule);
                              }
                           }
                        }
                     }

                     return;
                  }
               }
            } catch (Exception var10) {
               ChatUtils.addChatMessage("[AutoBuy] Не удалось прочитать правила: " + var10.getMessage());
            }
         }
      }
   }

   public void save() {
      Path path = this.file();
      if (path != null) {
         try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            JsonArray array = new JsonArray();

            for (AutoBuyRule rule : this.rules) {
               if (rule != null && rule.hasName()) {
                  array.add(GSON.toJsonTree(rule));
               }
            }

            root.add("rules", array);

            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
               GSON.toJson(root, writer);
            }
         } catch (Exception var9) {
            ChatUtils.addChatMessage("[AutoBuy] Не удалось сохранить правила: " + var9.getMessage());
         }
      }
   }

   private Path file() {
      if (Client.get() != null && Client.get().configManager() != null) {
         Path dir = Client.get().configManager().getConfigDir();
         return dir == null ? null : dir.resolve("autobuy.json");
      } else {
         return null;
      }
   }
}
