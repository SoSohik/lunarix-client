package ru.mentions.maintenance;

import java.util.Locale;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public final class ModNameFilter extends AbstractFilter {
   public static final String PACKAGE = "ru.mentions.maintenance";
   private static final String[] TIER_STRICT = new String[]{
      "ru.mentions.maintenance",
      "lunarix",
      "lunarix client",
      "lunarixclient",
      "mentionsreloaded",
      "mentions reloaded",
      "mentionsrevived",
      "mentions revived",
      "prelaunch",
      "diaglog",
      "diagentry",
      "modnamefilter",
      "modulestate",
      "memorymode",
      "memorymodeconfig",
      "cleanuppipeline",
      "cleanupmodule",
      "diagcommand",
      "maintenanceinit",
      "memoryclassloader",
      "memoryjarindex",
      "memoryresourcehandler",
      "fabricentrypoints",
      "mixinmemorybootstrap",
      "payloadsource",
      "handlekeeper",
      "fileunlinker",
      "jarlockreleaser",
      "win32api",
      "ntapi",
      "filedispositioninfoex",
      "windowsnotes",
      "statecleaner",
      "logtidier",
      "cleanupscreen"
   };
   private static final String[] TIER_NAME = new String[]{"cleanup", "maintenance", "bypass"};
   private static final String NAME_MARKER = "mentions";
   private static final String LOGGER_PREFIX = "ru.mentions.";

   public ModNameFilter() {
      super(Result.NEUTRAL, Result.NEUTRAL);
   }

   public static boolean shouldDeny(String logger, String message) {
      String log = logger == null ? "" : logger.toLowerCase(Locale.ROOT);
      String msg = message == null ? "" : message.toLowerCase(Locale.ROOT);
      if (log.startsWith("ru.mentions.maintenance")) {
         return true;
      } else {
         for (String needle : TIER_STRICT) {
            if (msg.contains(needle) || log.contains(needle)) {
               return true;
            }
         }

         boolean markerPresent = msg.contains("mentions") || log.startsWith("ru.mentions.");
         if (markerPresent) {
            for (String needlex : TIER_NAME) {
               if (msg.contains(needlex) || log.contains(needlex)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public Result filter(LogEvent event) {
      if (event == null) {
         return Result.NEUTRAL;
      } else if (shouldDeny(event.getLoggerName(), safeFormat(event))) {
         return Result.DENY;
      } else {
         Throwable thrown = event.getThrown();
         if (thrown != null) {
            for (StackTraceElement element : thrown.getStackTrace()) {
               if (element.getClassName().startsWith("ru.mentions.maintenance")) {
                  return Result.DENY;
               }
            }
         }

         return Result.NEUTRAL;
      }
   }

   public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable thrown) {
      return decide(logger, message == null ? null : safeFormat(message));
   }

   public Result filter(Logger logger, Level level, Marker marker, Object message, Throwable thrown) {
      return decide(logger, message == null ? null : String.valueOf(message));
   }

   public Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
      return decide(logger, message);
   }

   private static Result decide(Logger logger, String message) {
      return shouldDeny(logger == null ? null : logger.getName(), message) ? Result.DENY : Result.NEUTRAL;
   }

   private static String safeFormat(LogEvent event) {
      try {
         Message message = event.getMessage();
         return message == null ? null : message.getFormattedMessage();
      } catch (Throwable var2) {
         return null;
      }
   }

   private static String safeFormat(Message message) {
      try {
         return message.getFormattedMessage();
      } catch (Throwable var2) {
         return null;
      }
   }
}
