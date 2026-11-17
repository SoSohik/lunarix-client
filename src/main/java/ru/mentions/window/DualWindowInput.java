package ru.mentions.window;

import java.nio.IntBuffer;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1041;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11910;
import net.minecraft.class_276;
import net.minecraft.class_304;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import ru.mentions.mixin.DualWindowKeyBindingInputAccessor;
import ru.mentions.mixin.DualWindowKeyboardInputAccessor;
import ru.mentions.mixin.DualWindowMouseInputAccessor;
import ru.mentions.mixin.DualWindowToggleKeyInputAccessor;

public final class DualWindowInput {
   private static long original;
   private static long secondary;
   private static long owner;
   private static boolean forwarding;
   private static boolean reconciling;
   private static boolean raw;
   private static int cursorMode = 212993;
   private static final List<Runnable> restore = new ArrayList<>();
   private static final List<Callback> callbacks = new ArrayList<>();

   private DualWindowInput() {
   }

   public static boolean installed() {
      return secondary != 0L;
   }

   public static long ownerHandle() {
      return owner;
   }

   private static boolean focused(long handle) {
      return handle != 0L && GLFW.glfwGetWindowAttrib(handle, 131073) == 1;
   }

   private static class_310 client() {
      return class_310.method_1551();
   }

   private static DualWindowMouseInputAccessor mouse() {
      return (DualWindowMouseInputAccessor)client().field_1729;
   }

   private static DualWindowKeyboardInputAccessor keyboard() {
      return (DualWindowKeyboardInputAccessor)client().field_1774;
   }

   public static void install(long originalHandle, long secondaryHandle) {
      if (original != originalHandle || secondary != secondaryHandle) {
         if (originalHandle != 0L && secondaryHandle != 0L && originalHandle != secondaryHandle) {
            detach();
            original = originalHandle;
            secondary = secondaryHandle;
            cursorMode = client().field_1729.method_1613() ? 212995 : 212993;
            raw = GLFW.glfwRawMouseMotionSupported() && GLFW.glfwGetInputMode(original, 208901) == 1;

            try {
               GLFWKeyCallback key = GLFWKeyCallback.create((w, k, s, a, m) -> route(w, () -> keyboard().dualWindow$key(original, a, new class_11908(k, s, m))));
               callbacks.add(key);
               GLFWKeyCallback oldKey = GLFW.glfwSetKeyCallback(secondary, key);
               restore.add(() -> GLFW.glfwSetKeyCallback(secondary, oldKey));
               GLFWCharModsCallback character = GLFWCharModsCallback.create(
                  (w, c, m) -> route(w, () -> keyboard().dualWindow$character(original, new class_11905(c, m)))
               );
               callbacks.add(character);
               GLFWCharModsCallback oldCharacter = GLFW.glfwSetCharModsCallback(secondary, character);
               restore.add(() -> GLFW.glfwSetCharModsCallback(secondary, oldCharacter));
               GLFWMouseButtonCallback button = GLFWMouseButtonCallback.create(
                  (w, b, a, m) -> route(w, () -> mouse().dualWindow$button(original, new class_11910(b, m), a))
               );
               callbacks.add(button);
               GLFWMouseButtonCallback oldButton = GLFW.glfwSetMouseButtonCallback(secondary, button);
               restore.add(() -> GLFW.glfwSetMouseButtonCallback(secondary, oldButton));
               GLFWCursorPosCallback position = GLFWCursorPosCallback.create((w, x, y) -> route(w, () -> position(x, y)));
               callbacks.add(position);
               GLFWCursorPosCallback oldPosition = GLFW.glfwSetCursorPosCallback(secondary, position);
               restore.add(() -> GLFW.glfwSetCursorPosCallback(secondary, oldPosition));
               GLFWScrollCallback scroll = GLFWScrollCallback.create((w, x, y) -> route(w, () -> mouse().dualWindow$scroll(original, x, y)));
               callbacks.add(scroll);
               GLFWScrollCallback oldScroll = GLFW.glfwSetScrollCallback(secondary, scroll);
               restore.add(() -> GLFW.glfwSetScrollCallback(secondary, oldScroll));
               GLFWDropCallback drop = GLFWDropCallback.create((w, count, names) -> {
                  List<Path> paths = new ArrayList<>();
                  int invalid = 0;

                  for (int i = 0; i < count; i++) {
                     try {
                        paths.add(Path.of(GLFWDropCallback.getName(names, i)));
                     } catch (InvalidPathException var9x) {
                        invalid++;
                     }
                  }

                  int rejected = invalid;
                  route(w, () -> mouse().dualWindow$drop(original, paths, rejected));
               });
               callbacks.add(drop);
               GLFWDropCallback oldDrop = GLFW.glfwSetDropCallback(secondary, drop);
               restore.add(() -> GLFW.glfwSetDropCallback(secondary, oldDrop));
               GLFWWindowFocusCallback focus = GLFWWindowFocusCallback.create((w, f) -> tick());
               callbacks.add(focus);
               GLFWWindowFocusCallback oldFocus = GLFW.glfwSetWindowFocusCallback(secondary, focus);
               restore.add(() -> GLFW.glfwSetWindowFocusCallback(secondary, oldFocus));
               GLFWWindowCloseCallback close = GLFWWindowCloseCallback.create(w -> DualWindowService.requestClose());
               callbacks.add(close);
               GLFWWindowCloseCallback oldClose = GLFW.glfwSetWindowCloseCallback(secondary, close);
               restore.add(() -> GLFW.glfwSetWindowCloseCallback(secondary, oldClose));
               clear();
               tick();
            } catch (Throwable var20) {
               detach();
               throw var20;
            }
         } else {
            throw new IllegalArgumentException("Two distinct live GLFW windows are required");
         }
      }
   }

   public static void tick() {
      if (installed() && !reconciling && !DualWindowService.isCleanPass()) {
         reconciling = true;

         try {
            boolean closing = GLFW.glfwWindowShouldClose(secondary);
            if (closing) {
               DualWindowService.requestClose();
            }

            long next = !closing && focused(secondary) ? secondary : (focused(original) ? original : 0L);
            if (owner != next) {
               clear();
               if (owner != 0L) {
                  releaseCursor(owner);
               }

               owner = next;
               if (owner != 0L) {
                  applyMode(owner);
               }
            }

            client().method_15995(owner != 0L);
         } finally {
            reconciling = false;
         }
      }
   }

   private static void resetMouse() {
      ((DualWindowInput.MouseState)client().field_1729).dualWindow$clear();
   }

   private static void clear() {
      class_304.method_1437();

      for (class_304 binding : DualWindowKeyBindingInputAccessor.dualWindow$bindings().values()) {
         DualWindowKeyBindingInputAccessor state = (DualWindowKeyBindingInputAccessor)binding;
         state.dualWindow$pressed(false);
         state.dualWindow$presses(0);
         if (binding instanceof DualWindowToggleKeyInputAccessor toggle) {
            toggle.dualWindow$restoreToggle(false);
         }
      }

      resetMouse();
   }

   private static void route(long window, Runnable event) {
      tick();
      if (window == owner && !DualWindowService.isCleanPass()) {
         boolean previous = forwarding;
         forwarding = true;

         try {
            event.run();
         } finally {
            forwarding = previous;
         }
      }
   }

   public static boolean acceptOriginal(long window) {
      if (!installed()) {
         return true;
      } else if (DualWindowService.isCleanPass()) {
         return false;
      } else if (forwarding) {
         return window == original;
      } else {
         tick();
         return window == original && owner == original;
      }
   }

   private static void position(double x, double y) {
      if (cursorMode == 212995) {
         mouse().dualWindow$position(original, x, y);
      } else {
         double[] mapped = map(x, y, false);
         mouse().dualWindow$position(original, mapped[0], mapped[1]);
      }
   }

   private static double[] map(double x, double y, boolean inverse) {
      MemoryStack stack = MemoryStack.stackPush();

      double[] var29;
      label83: {
         label84: {
            double[] left;
            label85: {
               double[] scale;
               try {
                  IntBuffer w = stack.mallocInt(1);
                  IntBuffer h = stack.mallocInt(1);
                  GLFW.glfwGetWindowSize(secondary, w, h);
                  int ww = w.get(0);
                  int wh = h.get(0);
                  GLFW.glfwGetFramebufferSize(secondary, w, h);
                  int fw = w.get(0);
                  int fh = h.get(0);
                  class_276 source = client().method_1522();
                  int sw = source.field_1482;
                  int sh = source.field_1481;
                  class_1041 logical = client().method_22683();
                  int lw = logical.method_4480();
                  int lh = logical.method_4507();
                  if (ww > 0 && wh > 0 && fw > 0 && fh > 0 && sw > 0 && sh > 0 && lw > 0 && lh > 0) {
                     double scalex = Math.min((double)fw / (double)sw, (double)fh / (double)sh);
                     int dw = (int)((double)sw * scalex);
                     int dh = (int)((double)sh * scalex);
                     if (dw != 0 && dh != 0) {
                        int leftx = (fw - dw) / 2;
                        int top = fh - (fh - dh) / 2 - dh;
                        if (inverse) {
                           var29 = new double[]{
                              ((double)leftx + x * (double)dw / (double)lw) * (double)ww / (double)fw,
                              ((double)top + y * (double)dh / (double)lh) * (double)wh / (double)fh
                           };
                           break label83;
                        }

                        var29 = new double[]{
                           (x * (double)fw / (double)ww - (double)leftx) * (double)lw / (double)dw,
                           (y * (double)fh / (double)wh - (double)top) * (double)lh / (double)dh
                        };
                        break label84;
                     }

                     left = new double[]{x, y};
                     break label85;
                  }

                  scale = new double[]{x, y};
               } catch (Throwable var26) {
                  if (stack != null) {
                     try {
                        stack.close();
                     } catch (Throwable var25) {
                        var26.addSuppressed(var25);
                     }
                  }

                  throw var26;
               }

               if (stack != null) {
                  stack.close();
               }

               return scale;
            }

            if (stack != null) {
               stack.close();
            }

            return left;
         }

         if (stack != null) {
            stack.close();
         }

         return var29;
      }

      if (stack != null) {
         stack.close();
      }

      return var29;
   }

   public static void cursor(int mode, double x, double y) {
      tick();
      cursorMode = mode;
      resetMouse();
      if (owner != 0L) {
         if (owner == secondary) {
            double[] mapped = map(x, y, true);
            x = mapped[0];
            y = mapped[1];
         }

         GLFW.glfwSetCursorPos(owner, x, y);
         applyMode(owner);
         resetMouse();
      }
   }

   public static void raw(boolean enabled) {
      raw = enabled;
      tick();
      if (owner != 0L && GLFW.glfwRawMouseMotionSupported()) {
         GLFW.glfwSetInputMode(owner, 208901, enabled ? 1 : 0);
      }
   }

   private static void applyMode(long handle) {
      GLFW.glfwSetInputMode(handle, 208897, cursorMode);
      if (GLFW.glfwRawMouseMotionSupported()) {
         GLFW.glfwSetInputMode(handle, 208901, raw ? 1 : 0);
      }
   }

   private static void releaseCursor(long handle) {
      if (GLFW.glfwRawMouseMotionSupported()) {
         GLFW.glfwSetInputMode(handle, 208901, 0);
      }

      GLFW.glfwSetInputMode(handle, 208897, 212993);
   }

   public static void detach() {
      if (installed()) {
         reconciling = true;
         long primary = original;
         owner = 0L;

         try {
            clear();
            releaseCursor(secondary);
            releaseCursor(original);

            for (int i = restore.size() - 1; i >= 0; i--) {
               restore.get(i).run();
            }

            for (Callback callback : callbacks) {
               callback.free();
            }
         } finally {
            restore.clear();
            callbacks.clear();
            owner = 0L;
            secondary = 0L;
            original = 0L;
            reconciling = false;
            forwarding = false;
            GLFW.glfwFocusWindow(primary);
            client().method_15995(focused(primary));
            applyMode(primary);
            resetMouse();
         }
      }
   }

   public interface MouseState {
      void dualWindow$clear();
   }
}
