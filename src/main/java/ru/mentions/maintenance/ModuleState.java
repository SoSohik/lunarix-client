package ru.mentions.maintenance;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ru.mentions.maintenance.gui.CleanupScreen;
import ru.mentions.maintenance.mem.MemoryClassLoader;

public final class ModuleState {
   private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
   private static final List<Runnable> UNSUBSCRIBERS = new CopyOnWriteArrayList<>();
   private static final List<String> MIXIN_CONFIGS = new CopyOnWriteArrayList<>();
   private static final List<Path> ARTIFACTS = new CopyOnWriteArrayList<>();
   private static volatile MemoryClassLoader memoryLoader;
   private static volatile boolean stealthMemoryMode;
   private static volatile boolean fileLockReleased;
   private static volatile ModuleState.FileLockState fileLockState = ModuleState.FileLockState.LOCKED;
   private static volatile long lastCleanupAt;
   private static volatile String lastStatus = "idle";
   private static volatile int clientTicks;
   private static volatile boolean dormant;
   private static volatile boolean shutdownRequested;
   private static volatile boolean shutdownComplete;
   private static volatile boolean tickHookClaimed;
   private static volatile boolean shutdownHookClaimed;
   private static volatile String modeLabel = "A";
   private static volatile String jarState = "unknown";
   private static volatile int logsDone;
   private static volatile int logsTotal;

   private ModuleState() {
   }

   public static MemoryClassLoader memoryLoader() {
      return memoryLoader;
   }

   public static void memoryLoader(MemoryClassLoader loader) {
      memoryLoader = loader;
   }

   public static boolean stealthMemoryMode() {
      return stealthMemoryMode;
   }

   public static void stealthMemoryMode(boolean value) {
      stealthMemoryMode = value;
   }

   public static boolean fileLockReleased() {
      return fileLockReleased;
   }

   public static void fileLockReleased(boolean value) {
      fileLockReleased = value;
      fileLockState = value ? ModuleState.FileLockState.FREE : ModuleState.FileLockState.LOCKED;
   }

   public static ModuleState.FileLockState fileLockState() {
      return fileLockState;
   }

   public static void fileLockState(ModuleState.FileLockState state) {
      fileLockState = state;
      fileLockReleased = state == ModuleState.FileLockState.FREE;
   }

   public static int ramModuleCount() {
      MemoryClassLoader loader = memoryLoader;
      return loader == null ? 0 : loader.definedClassCount();
   }

   public static long lastCleanupAt() {
      return lastCleanupAt;
   }

   public static void markCleaned(String status) {
      lastCleanupAt = System.currentTimeMillis();
      lastStatus = status == null ? "done" : status;
   }

   public static String lastStatus() {
      return lastStatus;
   }

   public static String lastCleanupClock() {
      long at = lastCleanupAt;
      return at == 0L ? "--:--:--" : CLOCK.format(Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()));
   }

   public static int clientTicks() {
      return clientTicks;
   }

   public static int incrementClientTicks() {
      return ++clientTicks;
   }

   public static void addUnsubscriber(Runnable runnable) {
      if (runnable != null) {
         UNSUBSCRIBERS.add(runnable);
      }
   }

   public static List<Runnable> drainUnsubscribers() {
      List<Runnable> copy = new ArrayList<>(UNSUBSCRIBERS);
      UNSUBSCRIBERS.clear();
      return copy;
   }

   public static void addMixinConfig(String config) {
      if (config != null && !MIXIN_CONFIGS.contains(config)) {
         MIXIN_CONFIGS.add(config);
      }
   }

   public static List<String> mixinConfigs() {
      return Collections.unmodifiableList(MIXIN_CONFIGS);
   }

   public static void registerArtifact(Path path) {
      if (path != null) {
         ARTIFACTS.add(path);
      }
   }

   public static List<Path> artifacts() {
      return new ArrayList<>(ARTIFACTS);
   }

   public static void clearArtifacts() {
      ARTIFACTS.clear();
   }

   public static boolean dormant() {
      return dormant;
   }

   public static void setDormant(boolean value) {
      dormant = value;
   }

   public static boolean shutdownRequested() {
      return shutdownRequested;
   }

   public static void setShutdownRequested(boolean value) {
      shutdownRequested = value;
   }

   public static boolean shutdownComplete() {
      return shutdownComplete;
   }

   public static void setShutdownComplete(boolean value) {
      shutdownComplete = value;
   }

   public static synchronized boolean claimTickHook() {
      if (tickHookClaimed) {
         return false;
      } else {
         tickHookClaimed = true;
         return true;
      }
   }

   public static synchronized boolean claimShutdownHook() {
      if (shutdownHookClaimed) {
         return false;
      } else {
         shutdownHookClaimed = true;
         return true;
      }
   }

   public static String modeLabel() {
      return modeLabel;
   }

   public static void setModeLabel(String value) {
      modeLabel = value == null ? "A" : value;
   }

   public static String jarState() {
      return jarState;
   }

   public static void setJarState(String value) {
      jarState = value == null ? "unknown" : value;
   }

   public static int logsDone() {
      return logsDone;
   }

   public static void setLogsDone(int value) {
      logsDone = Math.max(0, value);
   }

   public static int logsTotal() {
      return logsTotal;
   }

   public static void setLogsTotal(int value) {
      logsTotal = Math.max(0, value);
   }

   public static boolean pendingSwap() {
      return MemoryModeConfig.pendingSwap();
   }

   public static Object newCleanupScreen() {
      MemoryClassLoader loader = memoryLoader;
      if (loader != null) {
         try {
            Class<?> type = Class.forName("ru.mentions.maintenance.gui.CleanupScreen", true, loader);
            return type.getDeclaredConstructor().newInstance();
         } catch (Throwable var2) {
            DiagLog.fail("state.screen-memory", var2);
         }
      }

      return new CleanupScreen();
   }

   public static enum FileLockState {
      LOCKED,
      FREE;
   }
}
