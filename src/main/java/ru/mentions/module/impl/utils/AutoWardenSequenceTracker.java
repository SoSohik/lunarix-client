package ru.mentions.module.impl.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AutoWardenSequenceTracker {
   private static final Logger LOGGER = LoggerFactory.getLogger("mentions/AutoWardenSequence");
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
   private static final Gson PRETTY_GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
   private static final String HISTORY_FILE = "autowarden-chest-sequence.jsonl";
   private static final String SNAPSHOT_FILE = "autowarden-chest-schedule.json";
   private static final long DUE_DRIFT_TOLERANCE_MS = 5500L;
   private static final long MIN_COOLDOWN_SAMPLE_MS = 60000L;
   private static final long MAX_COOLDOWN_SAMPLE_MS = 3600000L;
   private static final int MAX_COOLDOWN_SAMPLES = 128;
   private final Map<AutoWardenSequenceTracker.AnarchyChest, AutoWardenSequenceTracker.LiveTimer> liveTimers = new HashMap<>();
   private final Map<class_2338, AutoWardenSequenceTracker.CoordinateStats> coordinateStats = new HashMap<>();
   private final Set<class_2338> coordinateCatalog = new HashSet<>();
   private final Set<AutoWardenSequenceTracker.AnarchyChest> knownTimers = new HashSet<>();
   private final List<Long> cooldownSamples = new ArrayList<>();
   private final StringBuilder pendingLines = new StringBuilder();
   private boolean loaded;
   private boolean writeFailed;
   private boolean snapshotDirty;
   private String sessionId = UUID.randomUUID().toString();

   void beginSession() {
      this.logPos();
      this.sessionId = UUID.randomUUID().toString();
   }

   void endSession() {
      this.flush();
   }

   void observeTimer(String anarchy, class_2338 pos, long remainingMs) {
      if (pos != null && remainingMs >= 0L) {
         this.logPos();
         long var5 = System.currentTimeMillis();
         long var7 = var5 - ~remainingMs - 1L;
         String var9 = this.findAnarchy(anarchy);
         AutoWardenSequenceTracker.AnarchyChest var10 = new AutoWardenSequenceTracker.AnarchyChest(var9, pos.method_10062());
         this.coordinateCatalog.add(pos.method_10062());
         AutoWardenSequenceTracker.LiveTimer var11 = this.liveTimers.get(var10);
         boolean var12 = this.knownTimers.add(var10);
         String var13 = null;
         long var14 = var11 == null ? -1L : var11.dueAtEpochMs();
         long var16 = var11 == null ? 0L : var7 + ~var14 + 1L;
         if (var11 == null) {
            var13 = var12 ? "timer_first_seen" : null;
         } else if (Math.abs(var16) > 5500L) {
            var13 = var16 > 30000L ? "timer_reset" : "timer_resync";
         }

         AutoWardenSequenceTracker.LiveTimer var18 = new AutoWardenSequenceTracker.LiveTimer(
            var9, pos.method_10062(), remainingMs, var7, var5, var11 == null ? 1 : var11.observations() - -2 - 1
         );
         this.liveTimers.put(var10, var18);
         AutoWardenSequenceTracker.CoordinateStats var19 = this.coordinateStats
            .computeIfAbsent(pos.method_10062(), ignored -> new AutoWardenSequenceTracker.CoordinateStats());
         var19.findAnarchy2(var9, remainingMs, var7, var5);
         if (var13 != null) {
            this.findEvent(var13, var9, pos, remainingMs, var7, var14, -1L);
            this.snapshotDirty = true;
            LOGGER.info(
               "[WardenSequence] {} /an{} chest={} remaining={}ms dueAt={} period={}ms samples={}",
               new Object[]{var13, var9, pos, remainingMs, var7, this.estimatedCooldownMs(), this.cooldownSamples.size()}
            );
         }
      }
   }

   void recordEvent(String event, String anarchy, class_2338 pos, long remainingMs) {
      this.logPos();
      long var6 = remainingMs < 0L ? -1L : System.currentTimeMillis() - ~remainingMs - 1L;
      this.findEvent(event, this.findAnarchy(anarchy), pos, remainingMs, var6, -1L, -1L);
      this.snapshotDirty = true;
   }

   void recordTimerReady(String anarchy, class_2338 pos) {
      this.recordEvent("timer_ready", anarchy, pos, 0L);
   }

   void forgetTimerAfterDeath(String anarchy, class_2338 pos) {
      if (pos != null) {
         this.logPos();
         String var3 = this.findAnarchy(anarchy);
         class_2338 var4 = pos.method_10062();
         AutoWardenSequenceTracker.AnarchyChest var5 = new AutoWardenSequenceTracker.AnarchyChest(var3, var4);
         AutoWardenSequenceTracker.LiveTimer var6 = this.liveTimers.remove(var5);
         this.knownTimers.remove(var5);
         this.findEvent(
            "death_target_forgotten",
            var3,
            var4,
            var6 == null ? -1L : Math.max(0L, var6.dueAtEpochMs() + ~System.currentTimeMillis() + 1L),
            var6 == null ? -1L : var6.dueAtEpochMs(),
            -1L,
            -1L
         );
         this.snapshotDirty = true;
      }
   }

   void rememberLooted(String anarchy, class_2338 pos, long cooldownMs) {
      if (pos != null && cooldownMs >= 60000L) {
         this.logPos();
         long var5 = System.currentTimeMillis();
         long var7 = this.findValue(cooldownMs);
         long var9 = var5 - ~var7 - 1L;
         String var11 = this.findAnarchy(anarchy);
         class_2338 var12 = pos.method_10062();
         AutoWardenSequenceTracker.AnarchyChest var13 = new AutoWardenSequenceTracker.AnarchyChest(var11, var12);
         AutoWardenSequenceTracker.LiveTimer var14 = this.liveTimers.get(var13);
         long var15 = var14 == null ? -1L : var14.dueAtEpochMs();
         this.liveTimers.put(var13, new AutoWardenSequenceTracker.LiveTimer(var11, var12, var7, var9, var5, var14 == null ? 1 : var14.observations() - -2 - 1));
         this.knownTimers.add(var13);
         this.coordinateCatalog.add(var12);
         this.coordinateStats.computeIfAbsent(var12, ignored -> new AutoWardenSequenceTracker.CoordinateStats()).findAnarchy2(var11, var7, var9, var5);
         this.findSample(var7);
         this.findEvent("loot_timer_assumed", var11, var12, var7, var9, var15, var7);
         this.snapshotDirty = true;
         this.flush();
      }
   }

   long rememberedRemainingMs(String anarchy, class_2338 pos) {
      if (pos == null) {
         return -1L;
      } else {
         this.logPos();
         AutoWardenSequenceTracker.LiveTimer var3 = this.liveTimers.get(new AutoWardenSequenceTracker.AnarchyChest(this.findAnarchy(anarchy), pos));
         return var3 == null ? -1L : Math.max(0L, var3.dueAtEpochMs() + ~System.currentTimeMillis() + 1L);
      }
   }

   List<AutoWardenSequenceTracker.TimerPrediction> predictionsFor(String anarchy) {
      this.logPos();
      long var2 = System.currentTimeMillis();
      String var4 = this.findAnarchy(anarchy);
      return this.liveTimers
         .values()
         .stream()
         .filter(timer -> timer.anarchy().equals(var4))
         .map(
            timer -> new AutoWardenSequenceTracker.TimerPrediction(
                  timer.pos(), Math.max(0L, timer.dueAtEpochMs() + ~var2 + 1L), timer.dueAtEpochMs(), timer.observedAtEpochMs()
               )
         )
         .sorted(Comparator.comparingLong(AutoWardenSequenceTracker.TimerPrediction::remainingMs))
         .toList();
   }

   List<class_2338> knownCoordinates() {
      this.logPos();
      return this.coordinateCatalog
         .stream()
         .sorted(Comparator.comparingInt(class_2382::method_10263).thenComparingInt(class_2382::method_10264).thenComparingInt(class_2382::method_10260))
         .toList();
   }

   boolean hasTimer(String anarchy, class_2338 pos) {
      if (pos == null) {
         return false;
      } else {
         this.logPos();
         return this.liveTimers.containsKey(new AutoWardenSequenceTracker.AnarchyChest(this.findAnarchy(anarchy), pos));
      }
   }

   void flush() {
      if (!this.writeFailed && (!this.pendingLines.isEmpty() || this.snapshotDirty)) {
         try {
            this.findPos2();
            if (!this.pendingLines.isEmpty()) {
               File var1 = new File(AutoWardenPaths.DIRECTORY, "autowarden-chest-sequence.jsonl");
               Files.writeString(var1.toPath(), this.pendingLines.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
               this.pendingLines.setLength(0);
            }

            if (this.snapshotDirty) {
               File var3 = new File(AutoWardenPaths.DIRECTORY, "autowarden-chest-schedule.json");
               Files.writeString(
                  var3.toPath(), PRETTY_GSON.toJson(this.logPos2()), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
               );
               this.snapshotDirty = false;
            }
         } catch (IOException var21) {
            this.writeFailed = true;
            LOGGER.error("Could not write AutoWarden sequence files", var21);
         }
      }
   }

   long estimatedCooldownMs() {
      if (this.cooldownSamples.isEmpty()) {
         return -1L;
      } else {
         ArrayList<Long> var1 = new ArrayList<>(this.cooldownSamples);
         var1.sort(Long::compareTo);
         int var2 = var1.size() / 2;
         return var1.size() % 2 == 0 ? this.findValue((var1.get(var2 + -2 + 1) - ~var1.get(var2) - 1L) / 2L) : var1.get(var2);
      }
   }

   int knownCoordinateCount() {
      this.logPos();
      return this.coordinateCatalog.size();
   }

   private void findEvent(String event, String anarchy, class_2338 pos, long remainingMs, long dueAt, long previousDueAt, long periodSample) {
      JsonObject var12 = new JsonObject();
      long var13 = System.currentTimeMillis();
      var12.addProperty("event", event);
      var12.addProperty("sessionId", this.sessionId);
      var12.addProperty("observedAt", Instant.ofEpochMilli(var13).toString());
      var12.addProperty("observedAtEpochMs", var13);
      if (anarchy != null) {
         var12.addProperty("anarchy", anarchy);
      }

      if (pos != null) {
         var12.addProperty("coordinate", this.findPos(pos));
         var12.addProperty("x", pos.method_10263());
         var12.addProperty("y", pos.method_10264());
         var12.addProperty("z", pos.method_10260());
      }

      if (remainingMs >= 0L) {
         var12.addProperty("remainingMs", remainingMs);
      }

      if (dueAt >= 0L) {
         var12.addProperty("dueAtEpochMs", dueAt);
         var12.addProperty("dueAt", Instant.ofEpochMilli(dueAt).toString());
      }

      if (previousDueAt >= 0L) {
         var12.addProperty("previousDueAtEpochMs", previousDueAt);
         var12.addProperty("dueDeltaMs", dueAt + ~previousDueAt + 1L);
      }

      if (periodSample >= 0L) {
         var12.addProperty("periodSampleMs", periodSample);
      }

      long var15 = this.estimatedCooldownMs();
      if (var15 >= 0L) {
         var12.addProperty("estimatedCooldownMs", var15);
         var12.addProperty("cooldownSampleCount", this.cooldownSamples.size());
      }

      this.pendingLines.append(GSON.toJson(var12)).append(System.lineSeparator());
   }

   private JsonObject logPos2() {
      long var1 = System.currentTimeMillis();
      long var3 = this.estimatedCooldownMs();
      JsonObject var5 = new JsonObject();
      var5.addProperty("generatedAt", Instant.ofEpochMilli(var1).toString());
      var5.addProperty("generatedAtEpochMs", var1);
      var5.addProperty("knownCoordinates", this.coordinateStats.size());
      var5.addProperty("liveAnarchyCoordinates", this.liveTimers.size());
      var5.addProperty("cooldownSampleCount", this.cooldownSamples.size());
      var5.addProperty("predictionReady", var3 >= 0L && this.cooldownSamples.size() >= 3);
      if (var3 >= 0L) {
         var5.addProperty("estimatedCooldownMs", var3);
      }

      JsonArray var6 = new JsonArray();
      this.coordinateStats
         .entrySet()
         .stream()
         .sorted(
            Entry.comparingByKey(
               Comparator.comparingInt(class_2382::method_10263).thenComparingInt(class_2382::method_10264).thenComparingInt(class_2382::method_10260)
            )
         )
         .forEach(entry -> var6.add(this.logPos3(entry.getKey(), entry.getValue())));
      var5.add("coordinates", var6);
      JsonArray var7 = new JsonArray();
      this.liveTimers
         .values()
         .stream()
         .map(timer -> this.logTimer(timer, var1))
         .sorted(Comparator.comparingLong(AutoWardenSequenceTracker.PredictedTimer::dueAtEpochMs))
         .forEach(timer -> {
            JsonObject var5x = new JsonObject();
            var5x.addProperty("anarchy", timer.anarchy());
            var5x.addProperty("coordinate", this.findPos(timer.pos()));
            var5x.addProperty("x", timer.pos().method_10263());
            var5x.addProperty("y", timer.pos().method_10264());
            var5x.addProperty("z", timer.pos().method_10260());
            var5x.addProperty("predictedRemainingMs", Math.max(0L, timer.dueAtEpochMs() + ~var1 + 1L));
            var5x.addProperty("predictedDueAtEpochMs", timer.dueAtEpochMs());
            var5x.addProperty("predictedDueAt", Instant.ofEpochMilli(timer.dueAtEpochMs()).toString());
            var7.add(var5x);
         });
      var5.add("nextSequence", var7);
      return var5;
   }

   private JsonObject logPos3(class_2338 pos, AutoWardenSequenceTracker.CoordinateStats stats) {
      JsonObject var3 = new JsonObject();
      var3.addProperty("coordinate", this.findPos(pos));
      var3.addProperty("x", pos.method_10263());
      var3.addProperty("y", pos.method_10264());
      var3.addProperty("z", pos.method_10260());
      var3.addProperty("observations", stats.observations);
      var3.addProperty("lastAnarchy", stats.lastAnarchy);
      var3.addProperty("lastRemainingMs", stats.lastRemainingMs);
      var3.addProperty("lastDueAtEpochMs", stats.lastDueAtEpochMs);
      var3.addProperty("lastDueAt", Instant.ofEpochMilli(stats.lastDueAtEpochMs).toString());
      var3.addProperty("lastObservedAtEpochMs", stats.lastObservedAtEpochMs);
      return var3;
   }

   private AutoWardenSequenceTracker.PredictedTimer logTimer(AutoWardenSequenceTracker.LiveTimer timer, long now) {
      return new AutoWardenSequenceTracker.PredictedTimer(timer.anarchy(), timer.pos(), timer.dueAtEpochMs());
   }

   private void logPos() {
      if (!this.loaded) {
         this.loaded = true;
         this.logPos4();
         this.snapshotDirty = true;
         LOGGER.info(
            "Started fresh AutoWarden timer memory for this client session; retained {} structural chest coordinates only", this.coordinateCatalog.size()
         );
      }
   }

   private void logPos4() {
      File var1 = new File(AutoWardenPaths.DIRECTORY, "autowarden-chest-sequence.jsonl");
      if (var1.isFile()) {
         try {
            for (String var3 : Files.readAllLines(var1.toPath(), StandardCharsets.UTF_8)) {
               if (!var3.isBlank()) {
                  try {
                     JsonObject var4 = JsonParser.parseString(var3).getAsJsonObject();
                     if (var4.has("x") && var4.has("y") && var4.has("z")) {
                        this.coordinateCatalog.add(new class_2338(var4.get("x").getAsInt(), var4.get("y").getAsInt(), var4.get("z").getAsInt()));
                     }
                  } catch (RuntimeException var5) {
                  }
               }
            }
         } catch (IOException var6) {
            LOGGER.warn("Could not load the structural Warden chest coordinate catalog", var6);
         }
      }
   }

   private void findSample(long sample) {
      long var3 = this.findValue(sample);
      if (var3 >= 60000L && var3 <= 3600000L) {
         this.cooldownSamples.add(var3);
         if (this.cooldownSamples.size() > 128) {
            this.cooldownSamples.removeFirst();
         }
      }
   }

   private void findPos2() throws IOException {
      if (!AutoWardenPaths.DIRECTORY.exists() && !AutoWardenPaths.DIRECTORY.mkdirs()) {
         String var1 = String.valueOf(AutoWardenPaths.DIRECTORY);
         throw new IOException("could not create " + var1);
      }
   }

   private String findAnarchy(String anarchy) {
      if (anarchy != null && !anarchy.isBlank()) {
         String var2 = anarchy.replaceAll("[^0-9]", "");
         return var2.isBlank() ? anarchy.trim() : var2;
      } else {
         return "?";
      }
   }

   private String findPos(class_2338 pos) {
      int var10000 = pos.method_10263();
      int var2 = pos.method_10260();
      int var3 = pos.method_10264();
      return var10000 + "," + var3 + "," + var2;
   }

   private long findValue(long value) {
      return Math.max(0L, Math.round((double)value / 1000.0) * 1000L);
   }

   private static record AnarchyChest(String anarchy, class_2338 pos) {
   }

   private static final class CoordinateStats {
      private int observations;
      private String lastAnarchy = "?";
      private long lastRemainingMs;
      private long lastDueAtEpochMs;
      private long lastObservedAtEpochMs;

      private void findAnarchy2(String anarchy, long remainingMs, long dueAt, long observedAt) {
         this.observations = this.observations - -2 - 1;
         if (observedAt >= this.lastObservedAtEpochMs) {
            this.lastAnarchy = anarchy;
            this.lastRemainingMs = remainingMs;
            this.lastDueAtEpochMs = dueAt;
            this.lastObservedAtEpochMs = observedAt;
         }
      }
   }

   private static record LiveTimer(String anarchy, class_2338 pos, long remainingMs, long dueAtEpochMs, long observedAtEpochMs, int observations) {
   }

   private static record PredictedTimer(String anarchy, class_2338 pos, long dueAtEpochMs) {
   }

   static record TimerPrediction(class_2338 pos, long remainingMs, long dueAtEpochMs, long observedAtEpochMs) {
   }
}
