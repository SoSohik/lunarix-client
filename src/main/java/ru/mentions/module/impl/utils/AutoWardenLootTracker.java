package ru.mentions.module.impl.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2338;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AutoWardenLootTracker {
   private static final Logger LOGGER = LoggerFactory.getLogger("mentions/AutoWardenLoot");
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
   private static final String CATALOG_FILE = "autowarden-loot-catalog.json";
   private final Map<String, AutoWardenLootTracker.LootStats> catalog = new HashMap<>();
   private boolean loaded;
   private boolean writeFailed;

   void recordSnapshot(String anarchy, class_2338 chest, List<AutoWardenLootTracker.ObservedLoot> observed) {
      if (observed != null && !observed.isEmpty()) {
         this.logState();
         HashMap<String, AutoWardenLootTracker.ObservedLoot> var4 = new HashMap<>();

         for (AutoWardenLootTracker.ObservedLoot var6 : observed) {
            String var7 = findItemId(var6.itemId(), var6.name());
            AutoWardenLootTracker.ObservedLoot var8 = var4.get(var7);
            var4.put(
               var7,
               var8 == null
                  ? var6
                  : new AutoWardenLootTracker.ObservedLoot(
                     var6.itemId(), var6.name(), var8.count() - ~var6.count() - 1, Math.min(var8.priority(), var6.priority())
                  )
            );
         }

         long var11 = System.currentTimeMillis();

         for (AutoWardenLootTracker.ObservedLoot var13 : var4.values()) {
            String var9 = findItemId(var13.itemId(), var13.name());
            AutoWardenLootTracker.LootStats var10 = this.catalog
               .computeIfAbsent(var9, ignored -> new AutoWardenLootTracker.LootStats(var13.itemId(), var13.name()));
            var10.priority = Math.min(var10.priority, var13.priority());
            var10.observations = var10.observations - -2 - 1;
            var10.totalCount = var10.totalCount - ~var13.count() - 1;
            var10.lastCount = var13.count();
            var10.lastAnarchy = anarchy;
            var10.lastChest = chest == null ? null : chest.method_10062();
            var10.lastSeenAt = var11;
         }

         this.logNow(var11);
         LOGGER.info(
            "Recorded Warden chest loot snapshot /an{} chest={} stacks={} catalogItems={}", new Object[]{anarchy, chest, observed.size(), this.catalog.size()}
         );
      }
   }

   private void logState() {
      if (!this.loaded) {
         this.loaded = true;
         File var1 = new File(AutoWardenPaths.DIRECTORY, "autowarden-loot-catalog.json");
         if (var1.isFile()) {
            try {
               JsonObject var2 = JsonParser.parseString(Files.readString(var1.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
               JsonArray var3 = var2.getAsJsonArray("items");
               if (var3 == null) {
                  return;
               }

               for (JsonElement var5 : var3) {
                  JsonObject var6 = var5.getAsJsonObject();
                  String var7 = findJson2(var6, "itemId", "minecraft:air");
                  String var8 = findJson2(var6, "name", var7);
                  AutoWardenLootTracker.LootStats var9 = new AutoWardenLootTracker.LootStats(var7, var8);
                  var9.priority = findJson(var6, "priority", Integer.MAX_VALUE);
                  var9.observations = findJson(var6, "observations", 0);
                  var9.totalCount = findJson(var6, "totalCount", 0);
                  var9.lastCount = findJson(var6, "lastCount", 0);
                  var9.lastAnarchy = findJson2(var6, "lastAnarchy", "?");
                  if (var6.has("lastX") && var6.has("lastY") && var6.has("lastZ")) {
                     var9.lastChest = new class_2338(var6.get("lastX").getAsInt(), var6.get("lastY").getAsInt(), var6.get("lastZ").getAsInt());
                  }

                  var9.lastSeenAt = var6.has("lastSeenAtEpochMs") ? var6.get("lastSeenAtEpochMs").getAsLong() : 0L;
                  this.catalog.put(findItemId(var7, var8), var9);
               }
            } catch (IOException | RuntimeException var101) {
               LOGGER.warn("Could not load AutoWarden loot catalog", var101);
            }
         }
      }
   }

   private void logNow(long now) {
      if (!this.writeFailed) {
         try {
            if (!AutoWardenPaths.DIRECTORY.isDirectory() && !AutoWardenPaths.DIRECTORY.mkdirs()) {
               String var9x = String.valueOf(AutoWardenPaths.DIRECTORY);
               throw new IOException("could not create " + var9x);
            }

            JsonObject var3 = new JsonObject();
            var3.addProperty("generatedAt", Instant.ofEpochMilli(now).toString());
            var3.addProperty("generatedAtEpochMs", now);
            var3.addProperty("uniqueItems", this.catalog.size());
            JsonArray var4 = new JsonArray();
            ArrayList<AutoWardenLootTracker.LootStats> var5 = new ArrayList<>(this.catalog.values());
            var5.sort(
               Comparator.<AutoWardenLootTracker.LootStats>comparingInt(stats -> stats.priority)
                  .thenComparing(stats -> -stats.observations)
                  .thenComparing(stats -> stats.name)
            );

            for (AutoWardenLootTracker.LootStats var7 : var5) {
               JsonObject var8 = new JsonObject();
               var8.addProperty("itemId", var7.itemId);
               var8.addProperty("name", var7.name);
               var8.addProperty("priority", var7.priority);
               var8.addProperty("observations", var7.observations);
               var8.addProperty("totalCount", var7.totalCount);
               var8.addProperty("lastCount", var7.lastCount);
               var8.addProperty("lastAnarchy", var7.lastAnarchy);
               if (var7.lastChest != null) {
                  var8.addProperty("lastX", var7.lastChest.method_10263());
                  var8.addProperty("lastY", var7.lastChest.method_10264());
                  var8.addProperty("lastZ", var7.lastChest.method_10260());
               }

               var8.addProperty("lastSeenAtEpochMs", var7.lastSeenAt);
               if (var7.lastSeenAt > 0L) {
                  var8.addProperty("lastSeenAt", Instant.ofEpochMilli(var7.lastSeenAt).toString());
               }

               var4.add(var8);
            }

            var3.add("items", var4);
            Files.writeString(new File(AutoWardenPaths.DIRECTORY, "autowarden-loot-catalog.json").toPath(), GSON.toJson(var3), StandardCharsets.UTF_8);
         } catch (IOException var9) {
            this.writeFailed = true;
            LOGGER.warn("Could not write AutoWarden loot catalog", var9);
         }
      }
   }

   private static String findItemId(String itemId, String name) {
      return itemId + "\u0000" + name;
   }

   private static String findJson2(JsonObject json, String key, String fallback) {
      return json.has(key) ? json.get(key).getAsString() : fallback;
   }

   private static int findJson(JsonObject json, String key, int fallback) {
      return json.has(key) ? json.get(key).getAsInt() : fallback;
   }

   private static final class LootStats {
      private final String itemId;
      private final String name;
      private int priority = Integer.MAX_VALUE;
      private int observations;
      private int totalCount;
      private int lastCount;
      private String lastAnarchy = "?";
      private class_2338 lastChest;
      private long lastSeenAt;

      private LootStats(String itemId, String name) {
         this.itemId = itemId;
         this.name = name;
      }
   }

   static record ObservedLoot(String itemId, String name, int count, int priority) {
   }
}
