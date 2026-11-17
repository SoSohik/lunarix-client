package ru.mentions.utils.render;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_276;
import net.minecraft.class_310;
import ru.mentions.module.impl.misc.Optimization;

@Environment(EnvType.CLIENT)
public class ScreenBlur {
   private static final KawaseBlurPipeline pipeline = new KawaseBlurPipeline();
   private static GpuTextureView blurredView = null;
   private static int cachedWidth = 0;
   private static int cachedHeight = 0;
   private static boolean usedThisFrame = false;
   private static boolean usedLastFrame = true;
   private static boolean capturedThisFrame;

   public static void markUsed() {
      usedThisFrame = true;
   }

   public static void frame() {
      capturedThisFrame = false;
      boolean wanted = usedThisFrame || usedLastFrame;
      usedLastFrame = usedThisFrame;
      usedThisFrame = false;
      if (!wanted) {
         blurredView = null;
      } else {
         doCapture(3, 2.0F);
      }
   }

   public static void capture() {
      usedLastFrame = true;
      doCapture(3, 2.0F);
   }

   public static void capture(int size) {
      usedLastFrame = true;
      doCapture(size, (float)size);
   }

   private static void doCapture(int iterations, float offset) {
      if (Optimization.flatPanels()) {
         invalidate();
      } else {
         class_310 mc = class_310.method_1551();
         if (mc != null) {
            class_276 fb = mc.method_1522();
            if (fb != null) {
               int w = fb.field_1482;
               int h = fb.field_1481;
               if (w <= 0 || h <= 0) {
                  invalidate();
               } else if (!Optimization.reuseBlur()
                  || mc.field_1687 == null
                  || mc.field_1755 != null
                  || !capturedThisFrame
                  || blurredView == null
                  || cachedWidth != w
                  || cachedHeight != h) {
                  iterations = Optimization.blurPasses(iterations);
                  DrawBatcher.flushPending();

                  try {
                     GpuTexture colorAttachment = fb.method_30277();
                     GpuTextureView colorAttachmentView = fb.method_71639();
                     if (colorAttachment == null || colorAttachmentView == null) {
                        blurredView = null;
                        return;
                     }

                     blurredView = pipeline.blur(colorAttachment, colorAttachmentView, w, h, iterations, offset);
                     if (blurredView != null) {
                        cachedWidth = w;
                        cachedHeight = h;
                        capturedThisFrame = true;
                     }
                  } catch (Exception var8) {
                     blurredView = null;
                  }
               }
            }
         }
      }
   }

   public static boolean isReady() {
      return blurredView != null;
   }

   public static GpuTextureView getBlurredView() {
      return blurredView;
   }

   public static int getCachedWidth() {
      return cachedWidth;
   }

   public static int getCachedHeight() {
      return cachedHeight;
   }

   public static void invalidate() {
      blurredView = null;
      capturedThisFrame = false;
   }

   public static void close() {
      invalidate();
      pipeline.close();
   }
}
