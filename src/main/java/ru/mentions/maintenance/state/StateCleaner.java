package ru.mentions.maintenance.state;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.spongepowered.asm.mixin.MixinEnvironment;
import ru.mentions.maintenance.DiagLog;
import ru.mentions.maintenance.ModuleState;

public final class StateCleaner {
   private StateCleaner() {
   }

   public static void run() {
      detachMixinConfigs();
      runUnsubscribers();
      detachOwnAppenders();
      clearStatics();
      forceGc();
      clearStringTable();
      forceGc();
      DiagLog.note("state", "cleaned");
   }

   private static void detachMixinConfigs() {
      List<String> ours = ModuleState.mixinConfigs();
      if (!ours.isEmpty()) {
         try {
            MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
            if (environment == null) {
               return;
            }

            Field field = findField(environment.getClass(), "configs");
            if (field == null) {
               return;
            }

            if (read(environment, field) instanceof List<?> list) {
               list.removeAll(ours);
               DiagLog.note("state", "mixin-configs-detached");
            }
         } catch (Throwable var6) {
            DiagLog.fail("state.mixin", var6);
         }
      }
   }

   private static void runUnsubscribers() {
      for (Runnable runnable : ModuleState.drainUnsubscribers()) {
         try {
            runnable.run();
         } catch (Throwable var3) {
            DiagLog.fail("state.unsubscribe", var3);
         }
      }
   }

   public static void detachOwnAppenders() {
      try {
         LoggerContext context = (LoggerContext)LogManager.getContext(false);
         Configuration configuration = context.getConfiguration();
         List<String> doomed = new ArrayList<>();

         for (Entry<String, ?> entry : configuration.getAppenders().entrySet()) {
            String lower = entry.getKey().toLowerCase(Locale.ROOT);
            if (lower.contains("mentions") || lower.contains("maintenance") || lower.contains("cleanup")) {
               doomed.add(entry.getKey());
            }
         }

         for (String name : doomed) {
            if (configuration instanceof AbstractConfiguration abstractConfiguration) {
               abstractConfiguration.removeAppender(name);
            }
         }

         if (!doomed.isEmpty()) {
            context.updateLoggers();
            DiagLog.note("state", "appenders-removed=" + doomed.size());
         }
      } catch (Throwable var6) {
         DiagLog.fail("state.appenders", var6);
      }
   }

   private static void clearStatics() {
      try {
         ModuleState.memoryLoader(null);
         ModuleState.stealthMemoryMode(false);
         DiagLog.note("state", "statics-cleared");
      } catch (Throwable var1) {
         DiagLog.fail("state.statics", var1);
      }
   }

   public static void forceGc() {
      for (int pass = 0; pass < 3; pass++) {
         System.gc();
         Runtime.getRuntime().gc();

         try {
            Thread.sleep(120L);
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
            return;
         }
      }
   }

   private static void clearStringTable() {
      try {
         Class<?> type = Class.forName("java.lang.StringTable");
         Field strsField = type.getDeclaredField("strs");
         Object unsafe = StateCleaner.UnsafeHolder.UNSAFE;
         String[] table = readStringArray(type, strsField, unsafe);
         if (table == null) {
            DiagLog.note("state", "stringtable-unavailable");
            return;
         }

         if (!writeNulls(unsafe, table)) {
            Arrays.fill(table, null);
         }

         DiagLog.note("state", "stringtable-cleared=" + table.length);
      } catch (Throwable var4) {
         DiagLog.fail("state.stringtable", var4);
      }
   }

   private static String[] readStringArray(Class<?> type, Field field, Object unsafe) {
      try {
         if (field.trySetAccessible()) {
            Object value = field.get(null);
            if (value instanceof String[]) {
               return (String[])value;
            }
         }
      } catch (Throwable var10) {
         DiagLog.fail("state.stringtable-access", var10);
      }

      if (unsafe == null) {
         return null;
      } else {
         try {
            Class<?> unsafeType = unsafe.getClass();
            Object base = unsafeType.getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
            long offset = (Long)unsafeType.getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);
            return unsafeType.getMethod("getObject", Object.class, long.class).invoke(unsafe, base, offset) instanceof String[] array ? array : null;
         } catch (Throwable var9) {
            DiagLog.fail("state.stringtable-unsafe", var9);
            return null;
         }
      }
   }

   private static boolean writeNulls(Object unsafe, String[] table) {
      if (unsafe == null) {
         return false;
      } else {
         try {
            Class<?> unsafeType = unsafe.getClass();
            Method putObject = unsafeType.getMethod("putObject", Object.class, long.class, Object.class);
            Method arrayBaseOffset = unsafeType.getMethod("arrayBaseOffset", Class.class);
            Method arrayIndexScale = unsafeType.getMethod("arrayIndexScale", Class.class);
            long base = (Long)arrayBaseOffset.invoke(unsafe, String[].class);
            long scale = (Long)arrayIndexScale.invoke(unsafe, String[].class);

            for (int index = 0; index < table.length; index++) {
               putObject.invoke(unsafe, table, base + (long)index * scale, null);
            }

            return true;
         } catch (Throwable var11) {
            DiagLog.fail("state.stringtable-write", var11);
            return false;
         }
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

   private static Object read(Object target, Field field) throws IllegalAccessException {
      if (field.trySetAccessible()) {
         return field.get(target);
      } else {
         Object unsafe = StateCleaner.UnsafeHolder.UNSAFE;
         if (unsafe == null) {
            return null;
         } else {
            try {
               Class<?> unsafeType = unsafe.getClass();
               if (Modifier.isStatic(field.getModifiers())) {
                  Object base = unsafeType.getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
                  long offset = (Long)unsafeType.getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);
                  return unsafeType.getMethod("getObject", Object.class, long.class).invoke(unsafe, base, offset);
               } else {
                  long offset = (Long)unsafeType.getMethod("objectFieldOffset", Field.class).invoke(unsafe, field);
                  return unsafeType.getMethod("getObject", Object.class, long.class).invoke(unsafe, target, offset);
               }
            } catch (Throwable var7) {
               DiagLog.fail("state.read:" + field.getName(), var7);
               return null;
            }
         }
      }
   }

   private static final class UnsafeHolder {
      static final Object UNSAFE = load();

      private static Object load() {
         try {
            Class<?> type = Class.forName("sun.misc.Unsafe");
            Field field = type.getDeclaredField("theUnsafe");
            field.trySetAccessible();
            return field.get(null);
         } catch (Throwable var2) {
            DiagLog.fail("state.unsafe-init", var2);
            return null;
         }
      }
   }
}
