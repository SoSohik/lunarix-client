package ru.mentions.utils.other;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MediaKeys {
   private static final int VK_MEDIA_NEXT_TRACK = 176;
   private static final int VK_MEDIA_PREV_TRACK = 177;
   private static final int VK_MEDIA_STOP = 178;
   private static final int VK_MEDIA_PLAY_PAUSE = 179;
   private static final int KEYEVENTF_KEYUP = 2;
   private static volatile Boolean available;

   private MediaKeys() {
   }

   public static boolean next() {
      return tap(176);
   }

   public static boolean previous() {
      return tap(177);
   }

   public static boolean stop() {
      return tap(178);
   }

   public static boolean playPause() {
      return tap(179);
   }

   private static boolean tap(int virtualKey) {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            MediaKeys.User32.INSTANCE.keybd_event((byte)virtualKey, (byte)0, 0, 0L);
            MediaKeys.User32.INSTANCE.keybd_event((byte)virtualKey, (byte)0, 2, 0L);
            return true;
         } catch (Throwable var2) {
            return false;
         }
      }
   }

   private static boolean isAvailable() {
      Boolean cached = available;
      if (cached != null) {
         return cached;
      } else {
         try {
            MediaKeys.User32.INSTANCE.toString();
            available = Boolean.TRUE;
         } catch (Throwable var2) {
            available = Boolean.FALSE;
         }

         return available;
      }
   }

   private interface User32 extends StdCallLibrary {
      MediaKeys.User32 INSTANCE = (MediaKeys.User32)Native.load("user32", MediaKeys.User32.class, W32APIOptions.DEFAULT_OPTIONS);

      void keybd_event(byte var1, byte var2, int var3, long var4);
   }
}
