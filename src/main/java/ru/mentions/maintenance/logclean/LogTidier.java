package ru.mentions.maintenance.logclean;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ru.mentions.maintenance.DiagLog;

/**
 * Log retention helper.
 *
 * Раніше обрізав latest.log (RAF/пробіли), видаляв *.log.gz і логи лаунчерів.
 * Тепер усі destructive-методи лишились лише як сумісний API, але нічого
 * не чіпають: файли логів читаються/пишуться грою в звичайному режимі.
 */
public final class LogTidier {
   private LogTidier() {
   }

   public static int truncateLive(Path gameDirectory) {
      DiagLog.note("logs", "truncateLive-skipped");
      return 0;
   }

   public static int truncateOffline(Path gameDirectory) {
      DiagLog.note("logs", "truncateOffline-skipped");
      return 0;
   }

   public static int deleteGzipLogs(Path gameDirectory) {
      return 0;
   }

   public static int deleteLauncherLogs() {
      return 0;
   }

   public static int tidyGameDirectory(Path gameDirectory) {
      return 0;
   }

   public static int countTargets(Path gameDirectory) {
      int total = 0;
      total += count(logsDir(gameDirectory), "*.log");
      total += count(logsDir(gameDirectory), "*.log.gz");

      for (Path directory : launcherDirs()) {
         total += count(directory, "*.log");
         total += count(directory, "*.log.gz");
      }

      return total;
   }

   private static int count(Path directory, String glob) {
      if (directory != null && Files.isDirectory(directory)) {
         int total = 0;

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, glob)) {
            for (Path ignored : stream) {
               total++;
            }
         } catch (Throwable var8) {
            DiagLog.fail("logs.count:" + glob, var8);
         }

         return total;
      } else {
         return 0;
      }
   }

   public static List<Path> launcherDirs() {
      List<Path> directories = new ArrayList<>();
      String appData = System.getenv("APPDATA");
      if (appData == null) {
         return directories;
      } else {
         addIfDirectory(directories, Path.of(appData, "LegacyLauncher", "logs"));
         addIfDirectory(directories, Path.of(appData, ".tlauncher", "legacy", "Minecraft", "game", "logs"));
         addIfDirectory(directories, Path.of(appData, "TLauncher", "logs"));
         return directories;
      }
   }

   public static Path launcherLogs() {
      List<Path> directories = launcherDirs();
      return directories.isEmpty() ? null : directories.get(0);
   }

   private static void addIfDirectory(List<Path> target, Path candidate) {
      if (Files.isDirectory(candidate)) {
         target.add(candidate);
      }
   }

   private static Path logsDir(Path gameDirectory) {
      return gameDirectory == null ? null : gameDirectory.resolve("logs");
   }
}
