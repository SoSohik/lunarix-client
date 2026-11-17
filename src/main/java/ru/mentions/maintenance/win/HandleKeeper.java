package ru.mentions.maintenance.win;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.mentions.maintenance.DiagLog;

public final class HandleKeeper {
   private static final Map<String, HANDLE> HANDLES = new ConcurrentHashMap<>();

   private HandleKeeper() {
   }

   public static boolean keep(Path path) {
      String key = key(path);
      HANDLE existing = HANDLES.get(key);
      if (existing != null) {
         return true;
      } else {
         HANDLE handle = Win32Api.INSTANCE.CreateFileW(new WString(path.toString()), -2147418112, 7, null, 3, 33554432, null);
         if (isInvalid(handle)) {
            DiagLog.win32("CreateFileW(keep)", Native.getLastError());
            return false;
         } else {
            HANDLES.put(key, handle);
            DiagLog.note("win", "handle-kept");
            return true;
         }
      }
   }

   public static boolean has(String path) {
      return HANDLES.containsKey(key(Path.of(path)));
   }

   public static boolean markDeleteOnClose(String path) {
      HANDLE handle = HANDLES.get(key(Path.of(path)));
      if (handle == null) {
         return false;
      } else {
         Memory disposition = new Memory(1L);

         boolean var3;
         label32: {
            try {
               disposition.setByte(0L, (byte)1);
               if (Win32Api.INSTANCE.SetFileInformationByHandle(handle, 4, disposition, 1)) {
                  DiagLog.note("win", "delete-on-close");
                  var3 = true;
                  break label32;
               }

               DiagLog.win32("SetFileInformationByHandle", Native.getLastError());
            } catch (Throwable var6) {
               try {
                  disposition.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }

               throw var6;
            }

            disposition.close();
            return markDeleteEx(handle);
         }

         disposition.close();
         return var3;
      }
   }

   private static boolean markDeleteEx(HANDLE handle) {
      Memory ioStatus = new Memory((long)NtApi.IO_STATUS_BLOCK_SIZE);

      boolean var4;
      label30: {
         try {
            ioStatus.clear();
            FileDispositionInfoEx info = new FileDispositionInfoEx(7);
            info.write();
            int status = NtApi.INSTANCE.NtSetInformationFile(handle, ioStatus, info.getPointer(), info.size(), 21);
            if (status == 0) {
               DiagLog.note("win", "delete-on-close-posix");
               var4 = true;
               break label30;
            }

            DiagLog.note("win", "ntstatus=" + Integer.toHexString(status));
         } catch (Throwable var6) {
            try {
               ioStatus.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         ioStatus.close();
         return false;
      }

      ioStatus.close();
      return var4;
   }

   public static HANDLE handle(String path) {
      return HANDLES.get(key(Path.of(path)));
   }

   public static void closeAll() {
      for (String key : new ArrayList<>(HANDLES.keySet())) {
         HANDLE handle = HANDLES.remove(key);
         if (handle != null) {
            Win32Api.INSTANCE.CloseHandle(handle);
         }
      }
   }

   public static int openCount() {
      return HANDLES.size();
   }

   static boolean isInvalid(HANDLE handle) {
      if (handle == null) {
         return true;
      } else {
         Pointer pointer = handle.getPointer();
         return pointer == null || Pointer.nativeValue(pointer) == -1L;
      }
   }

   private static String key(Path path) {
      return path.toAbsolutePath().toString().toLowerCase();
   }
}
