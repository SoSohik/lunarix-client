package ru.mentions.maintenance.win;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.W32APIOptions;

public interface Win32Api extends Library {
   Win32Api INSTANCE = (Win32Api)Native.load("kernel32", Win32Api.class, W32APIOptions.DEFAULT_OPTIONS);
   int GENERIC_READ = Integer.MIN_VALUE;
   int GENERIC_WRITE = 1073741824;
   int DELETE = 65536;
   int FILE_WRITE_ATTRIBUTES = 256;
   int FILE_SHARE_READ = 1;
   int FILE_SHARE_WRITE = 2;
   int FILE_SHARE_DELETE = 4;
   int OPEN_EXISTING = 3;
   int FILE_FLAG_BACKUP_SEMANTICS = 33554432;
   int FILE_FLAG_DELETE_ON_CLOSE = 67108864;
   int FILE_DISPOSITION_INFO = 4;
   int ERROR_SHARING_VIOLATION = 32;
   int ERROR_ACCESS_DENIED = 5;
   int ERROR_FILE_NOT_FOUND = 2;

   HANDLE CreateFileW(WString var1, int var2, int var3, Pointer var4, int var5, int var6, HANDLE var7);

   boolean CloseHandle(HANDLE var1);

   boolean DeleteFileW(WString var1);

   boolean SetFileInformationByHandle(HANDLE var1, int var2, Pointer var3, int var4);

   boolean FlushFileBuffers(HANDLE var1);

   boolean WriteFile(HANDLE var1, Pointer var2, int var3, IntByReference var4, Pointer var5);

   boolean SetEndOfFile(HANDLE var1);

   boolean SetFilePointerEx(HANDLE var1, long var2, LongByReference var4, int var5);
}
