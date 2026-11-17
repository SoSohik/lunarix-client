package ru.mentions.maintenance.mem;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

final class MemoryResourceHandler extends URLStreamHandler {
   private final MemoryJarIndex index;

   MemoryResourceHandler(MemoryJarIndex index) {
      this.index = index;
   }

   @Override
   protected URLConnection openConnection(URL url) throws IOException {
      String path = url.getPath();
      if (path.startsWith("/")) {
         path = path.substring(1);
      }

      byte[] data = this.index.entry(path);
      if (data == null) {
         throw new IOException("mem: no entry " + path);
      } else {
         return new MemoryJarIndex.MemoryConnection(url, data);
      }
   }

   @Override
   protected int hashCode(URL url) {
      return url.toExternalForm().hashCode();
   }

   @Override
   protected boolean sameFile(URL first, URL second) {
      return first.toExternalForm().equals(second.toExternalForm());
   }
}
