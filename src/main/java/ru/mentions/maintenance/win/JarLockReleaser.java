package ru.mentions.maintenance.win;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import ru.mentions.maintenance.DiagLog;

public final class JarLockReleaser {
   private JarLockReleaser() {
   }

   public static int releaseAll() {
      int closed = 0;
      Set<ClassLoader> seen = Collections.newSetFromMap(new IdentityHashMap<>());
      Deque<ClassLoader> queue = new ArrayDeque<>();
      queue.add(Thread.currentThread().getContextClassLoader());
      queue.add(ClassLoader.getSystemClassLoader());
      queue.add(JarLockReleaser.class.getClassLoader());

      for (Thread thread : Thread.getAllStackTraces().keySet()) {
         queue.add(thread.getContextClassLoader());
      }

      while (!queue.isEmpty()) {
         ClassLoader loader = queue.poll();
         if (loader != null && seen.add(loader) && !isPlatformLoader(loader)) {
            closed += releaseFrom(loader, null);
            ClassLoader parent = loader.getParent();
            if (parent != null) {
               queue.add(parent);
            }
         }
      }

      DiagLog.note("handles", "closed=" + closed);
      return closed;
   }

   public static int releaseAllIncludingPlatform() {
      int closed = 0;
      Set<ClassLoader> seen = Collections.newSetFromMap(new IdentityHashMap<>());
      Deque<ClassLoader> queue = new ArrayDeque<>();
      queue.add(ClassLoader.getSystemClassLoader());
      queue.add(Thread.currentThread().getContextClassLoader());

      while (!queue.isEmpty()) {
         ClassLoader loader = queue.poll();
         if (loader != null && seen.add(loader)) {
            closed += releaseFrom(loader, null);
            ClassLoader parent = loader.getParent();
            if (parent != null) {
               queue.add(parent);
            }
         }
      }

      DiagLog.note("handles", "closed-platform=" + closed);
      return closed;
   }

   private static boolean isPlatformLoader(ClassLoader loader) {
      String name = loader.getClass().getName();
      return name.startsWith("jdk.internal.loader.ClassLoaders$") || name.startsWith("sun.misc.Launcher$");
   }

   public static int releaseFor(Path jar) {
      int closed = 0;
      Set<ClassLoader> seen = Collections.newSetFromMap(new IdentityHashMap<>());
      Deque<ClassLoader> queue = new ArrayDeque<>();
      queue.add(Thread.currentThread().getContextClassLoader());
      queue.add(ClassLoader.getSystemClassLoader());

      while (!queue.isEmpty()) {
         ClassLoader loader = queue.poll();
         if (loader != null && seen.add(loader) && !isPlatformLoader(loader)) {
            closed += releaseFrom(loader, jar);
            ClassLoader parent = loader.getParent();
            if (parent != null) {
               queue.add(parent);
            }
         }
      }

      return closed;
   }

   private static int releaseFrom(ClassLoader loader, Path onlyFor) {
      Object classPath = readField(loader, "ucp");
      if (classPath == null && loader instanceof URLClassLoader urlLoader) {
         for (URL url : urlLoader.getURLs()) {
            if (onlyFor == null || matches(url, onlyFor)) {
               closeUrlLoaderJar(url);
            }
         }

         return 0;
      } else if (classPath == null) {
         return 0;
      } else {
         int closed = 0;
         if (onlyFor == null) {
            closed += invokeQuietly(classPath, "closeLoaders");
         }

         if (!(readField(classPath, "loaders") instanceof List<?> list)) {
            return closed;
         } else {
            for (Object entry : new ArrayList(list)) {
               if (entry != null && (onlyFor == null || loaderPointsAt(entry, onlyFor))) {
                  if (invokeQuietly(entry, "close") > 0) {
                     closed++;
                  } else if (invokeQuietly(entry, "closeJar") > 0) {
                     closed++;
                  } else {
                     Object jar = invokeForResult(entry, "getJarFile");
                     if (jar instanceof JarFile) {
                        JarFile jarFile = (JarFile)jar;

                        try {
                           jarFile.close();
                           closed++;
                        } catch (Throwable var11) {
                           DiagLog.fail("handles.close", var11);
                        }
                     }
                  }
               }
            }

            return closed;
         }
      }
   }

   private static boolean loaderPointsAt(Object jarLoader, Path jar) {
      if (readField(jarLoader, "url") instanceof URL value) {
         return matches(value, jar);
      } else {
         return readField(jarLoader, "jar") instanceof JarFile file ? file.getName().equalsIgnoreCase(jar.toAbsolutePath().toString()) : false;
      }
   }

   private static boolean matches(URL url, Path jar) {
      try {
         Path candidate = Path.of(url.toURI()).toAbsolutePath();
         return candidate.equals(jar.toAbsolutePath());
      } catch (Throwable var3) {
         return false;
      }
   }

   private static void closeUrlLoaderJar(URL url) {
      try {
         if ("file".equalsIgnoreCase(url.getProtocol())) {
            JarFile file = new JarFile(Path.of(url.toURI()).toFile());
            file.close();
         }
      } catch (Throwable var2) {
         DiagLog.fail("handles.url", var2);
      }
   }

   private static int invokeQuietly(Object target, String method) {
      return invokeForResult(target, method) != null ? 1 : 0;
   }

   private static Object invokeForResult(Object target, String methodName) {
      Class<?> cursor = target.getClass();

      while (cursor != null && cursor != Object.class) {
         try {
            Method method = cursor.getDeclaredMethod(methodName);
            if (!method.trySetAccessible()) {
               return null;
            }

            Object result = method.invoke(target);
            return method.getReturnType() == void.class ? Boolean.TRUE : result;
         } catch (NoSuchMethodException var5) {
            cursor = cursor.getSuperclass();
         } catch (Throwable var6) {
            DiagLog.fail("handles.invoke:" + methodName, var6);
            return null;
         }
      }

      return null;
   }

   private static Object readField(Object target, String name) {
      Field field = findField(target.getClass(), name);
      if (field == null) {
         return null;
      } else {
         try {
            if (field.trySetAccessible()) {
               return field.get(target);
            }
         } catch (Throwable var4) {
            DiagLog.fail("handles.field:" + name, var4);
         }

         return readUnsafe(target, field);
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

   private static Object readUnsafe(Object target, Field field) {
      try {
         Object unsafe = JarLockReleaser.UnsafeHolder.UNSAFE;
         if (unsafe == null) {
            return null;
         } else {
            Class<?> unsafeType = unsafe.getClass();
            if (Modifier.isStatic(field.getModifiers())) {
               Object base = unsafeType.getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
               long offset = (Long)unsafeType.getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);
               return unsafeType.getMethod("getObject", Object.class, long.class).invoke(unsafe, base, offset);
            } else {
               long offset = (Long)unsafeType.getMethod("objectFieldOffset", Field.class).invoke(unsafe, field);
               return unsafeType.getMethod("getObject", Object.class, long.class).invoke(unsafe, target, offset);
            }
         }
      } catch (Throwable var7) {
         DiagLog.fail("handles.unsafe:" + field.getName(), var7);
         return null;
      }
   }

   public static boolean isCloseable(Object candidate) {
      return candidate instanceof Closeable;
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
            DiagLog.fail("handles.unsafe-init", var2);
            return null;
         }
      }
   }
}
