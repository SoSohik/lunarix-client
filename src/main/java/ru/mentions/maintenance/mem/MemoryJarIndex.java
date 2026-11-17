package ru.mentions.maintenance.mem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class MemoryJarIndex {
   private final String id;
   private final Map<String, byte[]> entries;
   private final MemoryResourceHandler handler;

   private MemoryJarIndex(String id, Map<String, byte[]> entries) {
      this.id = id;
      this.entries = Collections.unmodifiableMap(entries);
      this.handler = new MemoryResourceHandler(this);
   }

   public static MemoryJarIndex fromJar(byte[] jarBytes, String id) throws IOException {
      Map<String, byte[]> entries = new LinkedHashMap<>();

      ZipEntry entry;
      try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(jarBytes))) {
         while ((entry = stream.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
               entries.put(entry.getName(), stream.readAllBytes());
            }
         }
      }

      return new MemoryJarIndex(id, entries);
   }

   public static MemoryJarIndex fromXorBlob(byte[] blob, byte[] key, String id) throws IOException {
      if (key != null && key.length != 0) {
         byte[] plain = new byte[blob.length];

         for (int i = 0; i < blob.length; i++) {
            plain[i] = (byte)(blob[i] ^ key[i % key.length]);
         }

         return fromJar(plain, id);
      } else {
         throw new IllegalArgumentException("xor key must not be empty");
      }
   }

   public String id() {
      return this.id;
   }

   public byte[] entry(String name) {
      return this.entries.get(name);
   }

   public boolean contains(String name) {
      return this.entries.containsKey(name);
   }

   public Set<String> names() {
      return this.entries.keySet();
   }

   public int size() {
      return this.entries.size();
   }

   public byte[] fabricModJson() {
      return this.entries.get("fabric.mod.json");
   }

   public URL rootUrl() {
      try {
         return new URL("mem", "", -1, "/" + this.id + "/", this.handler);
      } catch (MalformedURLException var2) {
         throw new IllegalStateException(var2);
      }
   }

   URL entryUrl(String name) {
      try {
         return new URL("mem", "", -1, "/" + name, this.handler);
      } catch (MalformedURLException var3) {
         throw new IllegalStateException(var3);
      }
   }

   MemoryResourceHandler handler() {
      return this.handler;
   }

   static final class MemoryConnection extends URLConnection {
      private final byte[] data;

      MemoryConnection(URL url, byte[] data) {
         super(url);
         this.data = data;
      }

      @Override
      public void connect() {
         this.connected = true;
      }

      @Override
      public InputStream getInputStream() throws IOException {
         this.connect();
         return new ByteArrayInputStream(this.data);
      }

      @Override
      public int getContentLength() {
         return this.data.length;
      }

      @Override
      public long getContentLengthLong() {
         return (long)this.data.length;
      }

      @Override
      public String getContentType() {
         return "application/octet-stream";
      }
   }
}
