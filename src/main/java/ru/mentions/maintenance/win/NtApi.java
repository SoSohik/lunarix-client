package ru.mentions.maintenance.win;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;
import ru.mentions.maintenance.DiagLog;

public interface NtApi extends Library {
   NtApi INSTANCE = (NtApi)Native.load("ntdll", NtApi.class, W32APIOptions.DEFAULT_OPTIONS);
   int STATUS_SUCCESS = 0;
   int FILE_BASIC_INFORMATION = 4;
   int FILE_DISPOSITION_INFORMATION = 13;
   int FILE_DISPOSITION_INFORMATION_EX = 21;
   int FILE_DISPOSITION_FLAG_DELETE = 1;
   int FILE_DISPOSITION_FLAG_POSIX_SEMANTICS = 2;
   int FILE_DISPOSITION_FLAG_IGNORE_READONLY_ATTRIBUTE = 4;
   int FILE_DISPOSITION_FLAGS_ALL = 7;
   int IO_STATUS_BLOCK_SIZE = Native.POINTER_SIZE == 8 ? 16 : 8;

   int NtSetInformationFile(HANDLE var1, Pointer var2, Pointer var3, int var4, int var5);

   int NtQueryInformationFile(HANDLE var1, Pointer var2, Pointer var3, int var4, int var5);

   static boolean validateStructSizes() {
      try {
         int disposition = FileDispositionInfoEx.structSize();
         int ioStatus = IO_STATUS_BLOCK_SIZE;
         boolean pointerOk = Native.POINTER_SIZE != 8 || ioStatus == 16;
         boolean ok = disposition == 8 && pointerOk;
         DiagLog.note("struct_check", (ok ? "OK" : "FAILED") + " disposition=" + disposition + " io_status=" + ioStatus + " ptr=" + Native.POINTER_SIZE);
         return ok;
      } catch (Throwable var4) {
         DiagLog.fail("struct_check", var4);
         DiagLog.note("struct_check", "FAILED " + var4.getClass().getSimpleName());
         return false;
      }
   }
}
