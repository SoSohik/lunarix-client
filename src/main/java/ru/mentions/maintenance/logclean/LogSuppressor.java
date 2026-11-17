package ru.mentions.maintenance.logclean;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import ru.mentions.maintenance.DiagLog;

/**
 * Log retention.
 *
 * Раніше цей клас глушив log4j, різав System.out/err, видаляв logs/*.log,
 * hs_err_pid*.log і ставив guard-потік, який чистив логи кожні 2 секунди.
 * Тепер нічого не видаляється: логи, креш-репорти й stderr лишаються як є,
 * щоб було що дивитись після вильоту.
 */
public final class LogSuppressor {
   private static volatile boolean installed;

   private LogSuppressor() {
   }

   public static void install() {
      if (!installed) {
         installed = true;
         DiagLog.note("logs", "retention-mode (nothing is deleted)");
      }
   }

   public static void silenceLog4j() {
      // свідомо порожньо: логер не чіпаємо
   }

   public static void deleteLogFiles() {
      // свідомо порожньо: логи більше не видаляються
      DiagLog.note("logs", "delete-skipped");
   }

   public static String status() {
      try {
         LoggerContext context = (LoggerContext)LogManager.getContext(false);
         Configuration configuration = context.getConfiguration();
         return "retention level=" + configuration.getRootLogger().getLevel() + " appenders=" + configuration.getAppenders().size();
      } catch (Throwable var2) {
         DiagLog.fail("logs.status", var2);
         return "unavailable";
      }
   }

   public static Path gameDirectory() {
      try {
         return FabricLoader.getInstance().getGameDir();
      } catch (Throwable var1) {
         DiagLog.fail("logs.gamedir", var1);
         return null;
      }
   }

   static Level retentionLevel() {
      return Level.ALL;
   }
}
