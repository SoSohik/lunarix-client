package ru.mentions.maintenance;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import ru.mentions.maintenance.logclean.LogTidier;
import ru.mentions.maintenance.state.Dormancy;
import ru.mentions.maintenance.state.StateCleaner;
import ru.mentions.maintenance.win.FileUnlinker;
import ru.mentions.maintenance.win.FsOps;
import ru.mentions.maintenance.win.JarLockReleaser;
import ru.mentions.utils.annotation.IMinecraft;

public final class CleanupPipeline implements IMinecraft {
   /**
    * true — клієнт більше не видаляє власний jar і не ріже логи.
    * Постав false, якщо колись захочеш повернути стару поведінку.
    */
   public static final boolean RETAIN_FILES = true;

   private CleanupPipeline() {
   }

   public static CleanupPipeline.Mode currentMode() {
      if (ModuleState.shutdownRequested()) {
         return CleanupPipeline.Mode.OFFLINE;
      } else {
         return isJavaw() ? CleanupPipeline.Mode.LIVE : CleanupPipeline.Mode.OFFLINE;
      }
   }

   public static boolean isJavaw() {
      try {
         String command = ProcessHandle.current().info().command().orElse("");
         return command.toLowerCase(Locale.ROOT).endsWith("javaw.exe");
      } catch (Throwable var1) {
         DiagLog.fail("cleanup.mode", var1);
         return false;
      }
   }

   public static String runAll() {
      return currentMode() == CleanupPipeline.Mode.LIVE ? runLive() : runOffline();
   }

   public static String runLive() {
      ModuleState.setModeLabel("A");
      ModuleState.setShutdownComplete(false);
      Dormancy.mark();
      StateCleaner.detachOwnAppenders();
      Path own = ownJar();
      if (RETAIN_FILES) {
         ModuleState.setJarState(own == null ? "not-a-jar" : "retained");
      } else if (own != null) {
         JarLockReleaser.releaseFor(own);
         FileUnlinker.Outcome outcome = FileUnlinker.unlinkLive(own);
         ModuleState.setJarState(outcome.name().toLowerCase(Locale.ROOT));
      } else {
         ModuleState.setJarState("not-a-jar");
      }

      Path game = gameDirectory();
      int total = LogTidier.countTargets(game);
      int done = RETAIN_FILES ? 0 : LogTidier.truncateLive(game);
      ModuleState.setLogsTotal(total);
      ModuleState.setLogsDone(done);
      ModuleState.fileLockState(ModuleState.FileLockState.FREE);
      String status = "live jar=" + ModuleState.jarState() + " logs=kept(" + total + ")";
      ModuleState.markCleaned(status);
      DiagLog.note("cleanup", status);
      return status;
   }

   public static String runOffline() {
      ModuleState.setModeLabel("B");
      ModuleState.setShutdownComplete(false);
      Path own = ownJar();
      Path game = gameDirectory();
      int totalBefore = LogTidier.countTargets(game);
      CleanupPipeline.StepTracker tracker = new CleanupPipeline.StepTracker();
      tracker.run("mixin", Dormancy::mark);
      tracker.run("appenders", StateCleaner::detachOwnAppenders);
      if (RETAIN_FILES) {
         tracker.run("jar", () -> ModuleState.setJarState(own == null ? "not-a-jar" : "retained"));
      } else {
         tracker.run("handles", () -> {
            if (own != null) {
               JarLockReleaser.releaseFor(own);
            }
         });
         tracker.run("jar", () -> {
            if (own == null) {
               ModuleState.setJarState("not-a-jar");
            } else {
               FileUnlinker.Outcome outcome = FileUnlinker.unlinkOffline(own);
               ModuleState.setJarState(outcome.name().toLowerCase(Locale.ROOT));
            }
         });
         tracker.run("latest", () -> LogTidier.truncateOffline(game));
         tracker.run("gzip", () -> LogTidier.deleteGzipLogs(game));
         tracker.run("launcher", LogTidier::deleteLauncherLogs);
         tracker.run("decoys", () -> deleteDecoys(game));
      }
      tracker.run("flush", () -> DiagLog.note("offline_cleanup", "flush"));
      int remaining = LogTidier.countTargets(game);
      ModuleState.setLogsTotal(totalBefore);
      ModuleState.setLogsDone(RETAIN_FILES ? 0 : Math.max(0, totalBefore - remaining));
      ModuleState.fileLockState(ModuleState.FileLockState.FREE);
      ModuleState.setShutdownComplete(true);
      if (tracker.failedStep == null) {
         DiagLog.note("offline_cleanup", "complete");
      } else {
         DiagLog.note("offline_cleanup", "partial " + tracker.failedStep);
      }

      String status = tracker.failedStep == null ? "offline complete (files retained)" : "offline partial " + tracker.failedStep;
      ModuleState.markCleaned(status);
      return status;
   }

   public static String runLogsOnly() {
      Path game = gameDirectory();
      int total = LogTidier.countTargets(game);
      ModuleState.setLogsTotal(total);
      ModuleState.setLogsDone(0);
      return "logs kept=" + total;
   }

   public static String releaseFileLocks() {
      Path own = ownJar();
      int closed = own == null ? 0 : JarLockReleaser.releaseFor(own);
      ModuleState.fileLockState(ModuleState.FileLockState.FREE);
      String status = "released=" + closed;
      DiagLog.note("cleanup", status);
      return status;
   }

   public static String selftest() {
      Path own = ownJar();
      Path game = gameDirectory();
      boolean ready = own != null && game != null;
      DiagLog.note("selftest", "A=" + ready + " B=" + ready);
      return "selftest A: " + (ready ? "ok" : "fail") + ", selftest B: " + (ready ? "ok" : "fail");
   }

   private static void deleteDecoys(Path gameDirectory) {
      if (gameDirectory != null) {
         deleteDecoysIn(gameDirectory);
         deleteDecoysIn(gameDirectory.resolve("mods"));
      }
   }

   private static void deleteDecoysIn(Path directory) {
      if (directory != null && Files.isDirectory(directory)) {
         for (String mask : new String[]{"old_mod_backup_*.jar", "old_mod_backup_*.jar.bak"}) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, mask)) {
               for (Path entry : stream) {
                  FsOps.delete(entry);
               }
            } catch (Throwable var10) {
               DiagLog.fail("cleanup.decoys", var10);
            }
         }
      }
   }

   public static Path ownJar() {
      try {
         if (CleanupPipeline.class.getProtectionDomain() != null && CleanupPipeline.class.getProtectionDomain().getCodeSource() != null) {
            Path path = Path.of(CleanupPipeline.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!Files.isRegularFile(path)) {
               return null;
            } else {
               String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
               return name.endsWith(".jar") ? path : null;
            }
         } else {
            return null;
         }
      } catch (Throwable var2) {
         DiagLog.fail("cleanup.ownjar", var2);
         return null;
      }
   }

   public static Path gameDirectory() {
      try {
         return mc.field_1697.toPath();
      } catch (Throwable var1) {
         DiagLog.fail("cleanup.gamedir", var1);
         return null;
      }
   }

   public static enum Mode {
      LIVE,
      OFFLINE;
   }

   private static final class StepTracker {
      private String failedStep;

      void run(String name, Runnable step) {
         try {
            step.run();
         } catch (Throwable var4) {
            DiagLog.fail("offline." + name, var4);
            if (this.failedStep == null) {
               this.failedStep = name;
            }
         }
      }
   }
}
