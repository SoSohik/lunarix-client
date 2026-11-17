package ru.mentions.maintenance.mem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import ru.mentions.maintenance.DiagLog;

public final class MemoryClassLoader extends SecureClassLoader {
   private final MemoryJarIndex index;
   private final ProtectionDomain protectionDomain;
   private final Set<String> declaredPackages = ConcurrentHashMap.newKeySet();
   private final AtomicInteger definedClasses = new AtomicInteger();

   public MemoryClassLoader(MemoryJarIndex index, ClassLoader parent) {
      super(parent == null ? MemoryClassLoader.class.getClassLoader() : parent);
      this.index = index;
      CodeSource codeSource = new CodeSource(index.rootUrl(), (Certificate[])null);
      this.protectionDomain = new ProtectionDomain(codeSource, null, this, null);
   }

   public MemoryJarIndex index() {
      return this.index;
   }

   public int definedClassCount() {
      return this.definedClasses.get();
   }

   @Override
   protected Class<?> findClass(String name) throws ClassNotFoundException {
      String entry = name.replace('.', '/') + ".class";
      byte[] bytes = this.index.entry(entry);
      if (bytes == null) {
         throw new ClassNotFoundException(name);
      } else {
         this.declarePackage(name);
         Class<?> defined = this.defineClass(name, bytes, 0, bytes.length, this.protectionDomain);
         this.definedClasses.incrementAndGet();
         return defined;
      }
   }

   @Override
   protected URL findResource(String name) {
      return !this.index.contains(name) ? null : this.index.entryUrl(name);
   }

   @Override
   protected Enumeration<URL> findResources(String name) {
      URL single = this.findResource(name);
      return single == null ? Collections.emptyEnumeration() : Collections.enumeration(List.of(single));
   }

   @Override
   public InputStream getResourceAsStream(String name) {
      byte[] bytes = this.index.entry(name);
      return (InputStream)(bytes != null ? new ByteArrayInputStream(bytes) : super.getResourceAsStream(name));
   }

   public List<Class<?>> preloadAll() {
      List<Class<?>> loaded = new ArrayList<>();

      for (String name : this.index.names()) {
         if (name.endsWith(".class")) {
            String binary = name.substring(0, name.length() - ".class".length()).replace('/', '.');
            if (!binary.endsWith("module-info") && !binary.endsWith("package-info")) {
               try {
                  loaded.add(this.loadClass(binary));
               } catch (Throwable var6) {
                  DiagLog.fail("mem.preload", var6);
               }
            }
         }
      }

      return loaded;
   }

   private void declarePackage(String binaryName) {
      int dot = binaryName.lastIndexOf(46);
      if (dot > 0) {
         String packageName = binaryName.substring(0, dot);
         if (this.declaredPackages.add(packageName)) {
            try {
               this.definePackage(packageName, null, null, null, null, null, null, null);
            } catch (IllegalArgumentException var5) {
            }
         }
      }
   }
}
