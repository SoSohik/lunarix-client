package ru.mentions.utils.math;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFWNativeWin32;

@Environment(EnvType.CLIENT)
public class DarkUtils {
   public static void enableImmersiveDarkMode(long i_hwnd) {
      if (Platform.isWindows()) {
         long hwnd = GLFWNativeWin32.glfwGetWin32Window(i_hwnd);
         HWND hwndJna = new HWND(new Pointer(hwnd));
         int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
         Memory darkModeEnabled = new Memory(4L);
         darkModeEnabled.setInt(0L, 1);
         DarkUtils.DwmApi.INSTANCE.DwmSetWindowAttribute(hwndJna, DWMWA_USE_IMMERSIVE_DARK_MODE, darkModeEnabled, 4);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface DwmApi extends StdCallLibrary {
      DarkUtils.DwmApi INSTANCE = (DarkUtils.DwmApi)Native.loadLibrary("dwmapi", DarkUtils.DwmApi.class);

      int DwmSetWindowAttribute(HWND var1, int var2, Pointer var3, int var4);
   }
}
