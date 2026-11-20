package ru.mentions.maintenance;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import ru.mentions.maintenance.logclean.LogSuppressor;
import ru.mentions.maintenance.mem.MemoryMode;

public final class PreLaunch implements PreLaunchEntrypoint {
   public void onPreLaunch() {
      // Гарантуємо, що логи пишуться у logs/latest.log навіть якщо лаунчер не передав конфіг log4j
      this.ensureLogFileAppender();

      try {
         MemoryMode.bootstrapIfPending();
      } catch (Throwable var2) {
         DiagLog.fail("prelaunch.memory", var2);
      }
   }

   private void ensureLogFileAppender() {
      try {
         LoggerContext context = (LoggerContext)LogManager.getContext(false);
         Configuration configuration = context.getConfiguration();
         boolean hasFileAppender = false;
         for (Appender appender : configuration.getAppenders().values()) {
            if (appender instanceof FileAppender || appender instanceof RollingRandomAccessFileAppender) {
               hasFileAppender = true;
               break;
            }
         }

         if (!hasFileAppender) {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            Path logsDir = gameDir.resolve("logs");
            Files.createDirectories(logsDir);
            Path logFile = logsDir.resolve("latest.log");

            PatternLayout layout = PatternLayout.newBuilder()
               .withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n")
               .build();

            FileAppender appender = FileAppender.newBuilder()
               .setName("LunarixLatestLog")
               .withFileName(logFile.toAbsolutePath().toString())
               .withAppend(false)
               .setLayout(layout)
               .setConfiguration(configuration)
               .build();

            appender.start();
            configuration.addAppender(appender);
            for (LoggerConfig loggerConfig : configuration.getLoggers().values()) {
               loggerConfig.addAppender(appender, Level.INFO, null);
            }
            configuration.getRootLogger().addAppender(appender, Level.INFO, null);
            context.updateLoggers();
         }
      } catch (Throwable t) {
         DiagLog.fail("prelaunch.logger", t);
      }
   }

   private void installFilter() {
      try {
         LoggerContext context = (LoggerContext)LogManager.getContext(false);
         Configuration configuration = context.getConfiguration();
         ModNameFilter filter = new ModNameFilter();
         configuration.addFilter(filter);

         for (Entry<String, LoggerConfig> entry : configuration.getLoggers().entrySet()) {
            entry.getValue().addFilter(filter);
         }

         context.updateLoggers();
         DiagLog.note("prelaunch", "filter-installed");
      } catch (Throwable var6) {
         DiagLog.fail("prelaunch.filter", var6);
      }
   }

   private void quietLoaderHandler() {
      try {
         Class<?> logClass = Class.forName("net.fabricmc.loader.impl.util.log.Log");
         Field field = findField(logClass, "handler");
         if (field == null) {
            return;
         }

         field.setAccessible(true);
         Class<?> handlerType = field.getType();
         Object original = field.get(null);
         Object silent = Proxy.newProxyInstance(handlerType.getClassLoader(), new Class[]{handlerType}, (proxy, method, args) -> {
            String name = method.getName();
            if ("shouldLog".equals(name)) {
               return original != null ? method.invoke(original, args) : true;
            } else if ("log".equals(name)) {
               if (args != null) {
                  for (Object arg : args) {
                     if (arg instanceof String s && ModNameFilter.shouldDeny(null, s)) {
                        return null;
                     }
                  }
               }

               return original != null ? method.invoke(original, args) : null;
            } else if ("close".equals(name)) {
               return original != null ? method.invoke(original, args) : null;
            } else if ("toString".equals(name)) {
               return "LogHandlerProxy";
            } else if ("hashCode".equals(name)) {
               return System.identityHashCode(proxy);
            } else if (!"equals".equals(name)) {
               Class<?> returnType = method.getReturnType();
               if (returnType == boolean.class) {
                  return false;
               } else if (returnType == byte.class) {
                  return (byte)0;
               } else if (returnType == short.class) {
                  return (short)0;
               } else if (returnType == int.class) {
                  return 0;
               } else if (returnType == long.class) {
                  return 0L;
               } else if (returnType == float.class) {
                  return 0.0F;
               } else if (returnType == double.class) {
                  return 0.0;
               } else if (returnType == char.class) {
                  return '\u0000';
               } else {
                  return original != null ? method.invoke(original, args) : null;
               }
            } else {
               return proxy == (args != null && args.length > 0 ? args[0] : null);
            }
         });
         field.set(null, silent);
         DiagLog.note("prelaunch", "loader-handler-quieted");
      } catch (Throwable var6) {
         DiagLog.fail("prelaunch.loader", var6);
      }
   }

   private static Field findField(Class<?> owner, String name) {
      for (Class<?> cursor = owner; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
         try {
            return cursor.getDeclaredField(name);
         } catch (NoSuchFieldException var4) {
         }
      }

      return null;
   }
}
