package ru.mentions.maintenance.win;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import ru.mentions.maintenance.DiagLog;

public final class FileUnlinker {
   private static final int CHUNK = 65536;
   private static final int LIVE_HEAD_BYTES = 512;
   private static final SecureRandom RANDOM = new SecureRandom();

   private FileUnlinker() {
   }

   public static FileUnlinker.Outcome unlinkLive(Path target) {
      if (target != null && Files.exists(target)) {
         markOwnHandle(target);
         if (FsOps.delete(target)) {
            DiagLog.note("unlink", "live-deleted");
            return FileUnlinker.Outcome.DELETED;
         } else {
            Path decoy = DecoyRenamer.rename(target);
            if (decoy == null) {
               DiagLog.note("unlink", "live-locked-intact");
               return FileUnlinker.Outcome.FAILED;
            } else {
               overwriteHead(decoy, 512);
               DiagLog.note("unlink", "live-renamed");
               return FileUnlinker.Outcome.RENAMED;
            }
         }
      } else {
         return FileUnlinker.Outcome.DELETED;
      }
   }

   public static FileUnlinker.Outcome unlinkOffline(Path target) {
      if (target != null && Files.exists(target)) {
         markOwnHandle(target);
         if (FsOps.delete(target)) {
            DiagLog.note("unlink", "offline-deleted");
            return FileUnlinker.Outcome.DELETED;
         } else {
            Path decoy = DecoyRenamer.rename(target);
            if (decoy == null) {
               DiagLog.note("unlink", "offline-locked-intact");
               return FileUnlinker.Outcome.FAILED;
            } else {
               overwrite(decoy);
               truncate(decoy);
               DiagLog.note("unlink", "offline-renamed");
               return FileUnlinker.Outcome.RENAMED;
            }
         }
      } else {
         return FileUnlinker.Outcome.DELETED;
      }
   }

   public static FileUnlinker.Outcome unlink(Path target) {
      return unlinkOffline(target);
   }

   public static FileUnlinker.Outcome unlinkQuietly(Path target) {
      return unlinkLive(target);
   }

   private static void markOwnHandle(Path target) {
      try {
         if (HandleKeeper.has(target.toString())) {
            HandleKeeper.markDeleteOnClose(target.toString());
         }
      } catch (Throwable var2) {
         DiagLog.fail("unlink.mark", var2);
      }
   }

   public static void overwrite(Path target) {
      long size = sizeOf(target);
      if (size > 0L) {
         byte[] random = new byte[65536];
         ByteBuffer buffer = ByteBuffer.wrap(random);

         try (FileChannel channel = FileChannel.open(target, StandardOpenOption.WRITE)) {
            long written = 0L;

            while (written < size) {
               int len = (int)Math.min(65536L, size - written);
               byte[] slice = len == random.length ? random : new byte[len];
               RANDOM.nextBytes(slice);
               buffer.clear();
               buffer.limit(len);
               buffer.put(slice, 0, len);
               buffer.flip();
               channel.write(buffer);
               written += (long)len;
            }

            channel.force(true);
         } catch (Throwable var12) {
            DiagLog.fail("unlink.overwrite", var12);
         }
      }
   }

   public static void overwriteHead(Path target, int bytes) {
      long size = sizeOf(target);
      if (size > 0L && bytes > 0) {
         int limit = (int)Math.min((long)bytes, size);
         byte[] random = new byte[limit];
         RANDOM.nextBytes(random);

         try (FileChannel channel = FileChannel.open(target, StandardOpenOption.WRITE)) {
            channel.write(ByteBuffer.wrap(random));
            channel.force(true);
         } catch (Throwable var11) {
            DiagLog.fail("unlink.head", var11);
         }
      }
   }

   public static void truncate(Path target) {
      HANDLE handle = HandleKeeper.handle(target.toString());
      if (handle != null) {
         try {
            Win32Api.INSTANCE.SetFilePointerEx(handle, 0L, null, 0);
            if (!Win32Api.INSTANCE.SetEndOfFile(handle)) {
               DiagLog.win32("SetEndOfFile", Native.getLastError());
            }

            Win32Api.INSTANCE.FlushFileBuffers(handle);
            return;
         } catch (Throwable var8) {
            DiagLog.fail("unlink.truncate-handle", var8);
         }
      }

      try (FileChannel channel = FileChannel.open(target, StandardOpenOption.WRITE)) {
         channel.truncate(0L);
         channel.force(true);
      } catch (Throwable var7) {
         DiagLog.fail("unlink.truncate", var7);
      }
   }

   private static long sizeOf(Path target) {
      try {
         return Files.size(target);
      } catch (Throwable var2) {
         return 0L;
      }
   }

   public static enum Outcome {
      DELETED,
      RENAMED,
      FAILED;
   }
}
