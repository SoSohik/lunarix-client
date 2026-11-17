package ru.mentions.window;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.IntBuffer;
import net.minecraft.class_10868;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DualWindowService {
   private static final Logger LOG = LoggerFactory.getLogger("DualWindow");
   private static volatile boolean wanted;
   private static volatile long original;
   private static volatile long secondary;
   private static volatile String lastError;
   private static boolean cleanPass;
   private static boolean presenting;
   private static class_276 clean;
   private static long lastClean;
   private static GLCapabilities secondaryCaps;
   private static int secondaryFbo;

   private DualWindowService() {
   }

   public static void requestOpen() {
      wanted = true;
   }

   public static void requestClose() {
      wanted = false;
   }

   public static boolean isOpen() {
      return secondary != 0L;
   }

   public static String getLastError() {
      return lastError;
   }

   public static long originalHandle() {
      return original;
   }

   public static long secondaryHandle() {
      return secondary;
   }

   public static boolean isCleanPass() {
      return cleanPass;
   }

   public static void onFrameStart() {
      if (!cleanPass && !presenting) {
         original = class_310.method_1551().method_22683().method_4490();

         try {
            if (secondary != 0L && GLFW.glfwWindowShouldClose(secondary)) {
               wanted = false;
            }

            if (!wanted) {
               if (secondary != 0L) {
                  close();
               }

               return;
            }

            if (secondary == 0L) {
               open();
            }

            if (secondary != 0L) {
               DualWindowInput.tick();
            }
         } catch (Throwable var1) {
            fail(var1);
         }
      }
   }

   private static void open() {
      if (GLFW.glfwGetCurrentContext() != original) {
         throw new IllegalStateException("Original GL context is not current");
      } else {
         GLCapabilities caps = GL.getCapabilities();
         GLFW.glfwDefaultWindowHints();

         try {
            GLFW.glfwWindowHint(131076, 0);
            GLFW.glfwWindowHint(139266, 3);
            GLFW.glfwWindowHint(139267, 3);
            GLFW.glfwWindowHint(139272, 204801);
            GLFW.glfwWindowHint(131073, 0);
            class_276 main = class_310.method_1551().method_1522();
            secondary = GLFW.glfwCreateWindow(
               Math.max(320, main.field_1482), Math.max(240, main.field_1481), "Lunarix — experimental client view", 0L, original
            );
            if (secondary == 0L) {
               throw new IllegalStateException("GLFW could not create the shared window");
            }

            GLFW.glfwMakeContextCurrent(secondary);
            secondaryCaps = GL.createCapabilities();
            GLFW.glfwSwapInterval(0);
            secondaryFbo = GL33C.glGenFramebuffers();
         } finally {
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwMakeContextCurrent(original);
            GL.setCapabilities(caps);
         }

         DualWindowInput.install(original, secondary);
         GLFW.glfwShowWindow(secondary);
         lastError = null;
         lastClean = 0L;
      }
   }

   public static void beforePresent() {
      if (secondary != 0L && !cleanPass && !presenting) {
         presenting = true;
         class_310 client = class_310.method_1551();
         class_276 main = client.method_1522();

         try {
            presentSecondary(main);
            long now = System.nanoTime();
            boolean resized = clean == null || clean.field_1482 != main.field_1482 || clean.field_1481 != main.field_1481;
            if (resized || now - lastClean >= 66666667L) {
               if (resized) {
                  if (clean != null) {
                     clean.method_1238();
                  }

                  clean = new class_6367("Dual window clean", main.field_1482, main.field_1481, true);
               }

               DualWindowService.FramebufferBridge bridge = (DualWindowService.FramebufferBridge)client;
               int readFbo = GL33C.glGetInteger(36010);
               int drawFbo = GL33C.glGetInteger(36006);
               cleanPass = true;

               try {
                  bridge.dualWindow$setFramebuffer(clean);
                  RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(clean.method_30277(), -16777216, clean.method_30278(), 1.0);
                  client.field_1773.method_3192(client.method_61966(), true);
                  lastClean = now;
               } finally {
                  bridge.dualWindow$setFramebuffer(main);
                  cleanPass = false;
                  GL33C.glBindFramebuffer(36008, readFbo);
                  GL33C.glBindFramebuffer(36009, drawFbo);
               }
            }
         } catch (Throwable var17) {
            fail(var17);
         } finally {
            presenting = false;
         }
      }
   }

   public static void beforeSwap() {
      if (secondary != 0L && GLFW.glfwWindowShouldClose(secondary)) {
         requestClose();
      }
   }

   public static class_276 presentationFramebuffer(class_276 fallback) {
      return secondary != 0L && clean != null && lastClean != 0L ? clean : fallback;
   }

   private static void presentSecondary(class_276 source) {
      if (source.method_30277() instanceof class_10868 texture) {
         long var27 = GLFW.glfwGetCurrentContext();
         GLCapabilities previousCaps = GL.getCapabilities();
         long ready = GL33C.glFenceSync(37143, 0);
         GL33C.glFlush();
         long consumed = 0L;

         try {
            GLFW.glfwMakeContextCurrent(secondary);
            GL.setCapabilities(secondaryCaps);
            GL33C.glWaitSync(ready, 0, -1L);
            MemoryStack stack = MemoryStack.stackPush();

            try {
               IntBuffer width = stack.mallocInt(1);
               IntBuffer height = stack.mallocInt(1);
               GLFW.glfwGetFramebufferSize(secondary, width, height);
               int w = width.get(0);
               int h = height.get(0);
               if (w > 0 && h > 0) {
                  GL33C.glBindFramebuffer(36008, secondaryFbo);
                  GL33C.glFramebufferTexture2D(36008, 36064, 3553, texture.method_68427(), 0);
                  GL33C.glReadBuffer(36064);
                  if (GL33C.glCheckFramebufferStatus(36008) != 36053) {
                     throw new IllegalStateException("Secondary read framebuffer is incomplete");
                  }

                  GL33C.glBindFramebuffer(36009, 0);
                  GL33C.glDrawBuffer(1029);
                  GL33C.glDisable(3089);
                  GL33C.glDisable(36281);
                  GL33C.glColorMask(true, true, true, true);
                  GL33C.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
                  GL33C.glClear(16384);
                  double scale = Math.min((double)w / (double)source.field_1482, (double)h / (double)source.field_1481);
                  int dw = (int)((double)source.field_1482 * scale);
                  int dh = (int)((double)source.field_1481 * scale);
                  int x = (w - dw) / 2;
                  int y = (h - dh) / 2;
                  GL33C.glBlitFramebuffer(0, 0, source.field_1482, source.field_1481, x, y, x + dw, y + dh, 16384, 9729);
                  GL33C.glFramebufferTexture2D(36008, 36064, 3553, 0, 0);
                  GLFW.glfwSwapBuffers(secondary);
               }
            } catch (Throwable var25) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var24) {
                     var25.addSuppressed(var24);
                  }
               }

               throw var25;
            }

            if (stack != null) {
               stack.close();
            }
         } finally {
            if (GLFW.glfwGetCurrentContext() == secondary) {
               consumed = GL33C.glFenceSync(37143, 0);
               GL33C.glFlush();
               GL33C.glBindFramebuffer(36160, 0);
            }

            GLFW.glfwMakeContextCurrent(var27);
            GL.setCapabilities(previousCaps);
            if (consumed != 0L) {
               GL33C.glWaitSync(consumed, 0, -1L);
               GL33C.glDeleteSync(consumed);
            }

            GL33C.glDeleteSync(ready);
         }
      } else {
         throw new IllegalStateException("Dual window requires the OpenGL renderer");
      }
   }

   private static void fail(Throwable error) {
      lastError = error.getClass().getSimpleName() + ": " + error.getMessage();
      LOG.error("Experimental dual window disabled: {}", lastError, error);
      wanted = false;

      try {
         close();
      } catch (Throwable var3) {
         LOG.warn("Dual window cleanup failed", var3);
      }

      try {
         TinyFileDialogs.tinyfd_messageBox("Dual window unavailable", lastError, "ok", "error", false);
      } catch (Throwable var2) {
      }
   }

   private static void close() {
      try {
         DualWindowInput.detach();
      } finally {
         long handle = secondary;
         secondary = 0L;
         if (handle != 0L) {
            long context = GLFW.glfwGetCurrentContext();
            GLCapabilities caps = GL.getCapabilities();

            try {
               GLFW.glfwMakeContextCurrent(handle);
               GL.setCapabilities(secondaryCaps);
               if (secondaryFbo != 0) {
                  GL33C.glDeleteFramebuffers(secondaryFbo);
               }
            } finally {
               GLFW.glfwMakeContextCurrent(context == handle ? original : context);
               GL.setCapabilities(caps);
               GLFW.glfwDestroyWindow(handle);
               secondaryFbo = 0;
               secondaryCaps = null;
            }
         }

         if (clean != null) {
            clean.method_1238();
            clean = null;
         }

         lastClean = 0L;
      }
   }

   public static void onShutdown() {
      wanted = false;
      if (secondary != 0L || clean != null) {
         try {
            close();
         } catch (Throwable var1) {
            LOG.warn("Dual window shutdown failed", var1);
         }
      }
   }

   public interface FramebufferBridge {
      void dualWindow$setFramebuffer(class_276 var1);
   }
}
