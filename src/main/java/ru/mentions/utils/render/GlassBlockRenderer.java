package ru.mentions.utils.render;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_276;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public class GlassBlockRenderer {
   private static GlassBlockRenderer instance;
   private final class_310 client = class_310.method_1551();
   private KawaseBlurPipeline kawaseBlur;
   private GlassCompositePipeline glassComposite;
   private MaskDiffPipeline maskDiff;
   private GlassOutlinePipeline glassOutline;
   private GpuTexture sceneBeforeTexture;
   private GpuTextureView sceneBeforeTextureView;
   private GpuTexture sceneAfterTexture;
   private GpuTextureView sceneAfterTextureView;
   private GpuTexture depthBeforeTexture;
   private GpuTextureView depthBeforeTextureView;
   private GpuTexture depthAfterTexture;
   private GpuTextureView depthAfterTextureView;
   private GpuTexture maskTexture;
   private GpuTextureView maskTextureView;
   private int lastWidth = 0;
   private int lastHeight = 0;
   private boolean capturing = false;
   private boolean enabled = false;
   private boolean initialized = false;
   private boolean blurEnabled = true;
   private float blurRadius = 2.5F;
   private int blurIterations = 3;
   private float saturation = 0.0F;
   private boolean reflect = true;
   private int tintColor = 0;
   private float tintIntensity = 0.0F;
   private boolean outlineEnabled = true;
   private float outlineGlowStrength = 4.0F;
   private int outlineColor = -1;
   private int outlineColorFill = -1;
   private int outlineWidth = 1;
   private int outlineGlowMode = 2;

   public GlassBlockRenderer() {
      instance = this;
   }

   public static GlassBlockRenderer getInstance() {
      if (instance == null) {
         instance = new GlassBlockRenderer();
      }

      return instance;
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         if (this.kawaseBlur != null) {
            this.kawaseBlur.close();
         }

         if (this.glassComposite != null) {
            this.glassComposite.close();
         }

         if (this.maskDiff != null) {
            this.maskDiff.close();
         }

         if (this.glassOutline != null) {
            this.glassOutline.close();
         }

         this.kawaseBlur = new KawaseBlurPipeline();
         this.glassComposite = new GlassCompositePipeline();
         this.maskDiff = new MaskDiffPipeline();
         this.glassOutline = new GlassOutlinePipeline();
         this.lastWidth = 0;
         this.lastHeight = 0;
         this.initialized = true;
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (enabled) {
         this.ensureInitialized();
      }
   }

   public void setBlurEnabled(boolean enabled) {
      this.blurEnabled = enabled;
   }

   public void setBlurRadius(float radius) {
      this.blurRadius = radius;
   }

   public void setBlurIterations(int iterations) {
      this.blurIterations = Math.max(1, Math.min(8, iterations));
   }

   public void setSaturation(float saturation) {
      this.saturation = saturation;
   }

   public void setReflect(boolean reflect) {
      this.reflect = reflect;
   }

   public void setTintColor(int color) {
      this.tintColor = color;
   }

   public void setTintIntensity(float intensity) {
      this.tintIntensity = intensity;
   }

   public void setOutlineEnabled(boolean enabled) {
      this.outlineEnabled = enabled;
   }

   public void setOutlineGlowStrength(float strength) {
      this.outlineGlowStrength = strength;
   }

   public void setOutlineColor(int color) {
      this.outlineColor = color;
      this.outlineColorFill = color;
   }

   public void setOutlineWidth(int width) {
      this.outlineWidth = width;
   }

   public void setOutlineGlowMode(int mode) {
      this.outlineGlowMode = mode;
   }

   public void setShimmerEnabled(boolean enabled) {
      if (this.glassOutline != null) {
         this.glassOutline.setShimmerEnabled(enabled);
      }
   }

   public void setShimmerWidth(float width) {
      if (this.glassOutline != null) {
         this.glassOutline.setShimmerWidth(width);
      }
   }

   public void setShimmerPeriodSec(float sec) {
      if (this.glassOutline != null) {
         this.glassOutline.setShimmerPeriodSec(sec);
      }
   }

   private void ensureTextures(int width, int height) {
      if (width != this.lastWidth || height != this.lastHeight || this.sceneBeforeTexture == null) {
         this.cleanupTextures();
         this.sceneBeforeTexture = RenderSystem.getDevice()
            .createTexture(() -> "minecraft:glass_block_scene_before", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.sceneBeforeTextureView = RenderSystem.getDevice().createTextureView(this.sceneBeforeTexture);
         this.sceneAfterTexture = RenderSystem.getDevice()
            .createTexture(() -> "minecraft:glass_block_scene_after", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.sceneAfterTextureView = RenderSystem.getDevice().createTextureView(this.sceneAfterTexture);
         this.depthBeforeTexture = RenderSystem.getDevice()
            .createTexture(() -> "minecraft:glass_block_depth_before", 13, TextureFormat.DEPTH32, width, height, 1, 1);
         this.depthBeforeTextureView = RenderSystem.getDevice().createTextureView(this.depthBeforeTexture);
         this.depthAfterTexture = RenderSystem.getDevice()
            .createTexture(() -> "minecraft:glass_block_depth_after", 13, TextureFormat.DEPTH32, width, height, 1, 1);
         this.depthAfterTextureView = RenderSystem.getDevice().createTextureView(this.depthAfterTexture);
         this.maskTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_block_mask", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.maskTextureView = RenderSystem.getDevice().createTextureView(this.maskTexture);
         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   private void cleanupTextures() {
      if (this.sceneBeforeTextureView != null) {
         this.sceneBeforeTextureView.close();
         this.sceneBeforeTextureView = null;
      }

      if (this.sceneBeforeTexture != null) {
         this.sceneBeforeTexture.close();
         this.sceneBeforeTexture = null;
      }

      if (this.sceneAfterTextureView != null) {
         this.sceneAfterTextureView.close();
         this.sceneAfterTextureView = null;
      }

      if (this.sceneAfterTexture != null) {
         this.sceneAfterTexture.close();
         this.sceneAfterTexture = null;
      }

      if (this.depthBeforeTextureView != null) {
         this.depthBeforeTextureView.close();
         this.depthBeforeTextureView = null;
      }

      if (this.depthBeforeTexture != null) {
         this.depthBeforeTexture.close();
         this.depthBeforeTexture = null;
      }

      if (this.depthAfterTextureView != null) {
         this.depthAfterTextureView.close();
         this.depthAfterTextureView = null;
      }

      if (this.depthAfterTexture != null) {
         this.depthAfterTexture.close();
         this.depthAfterTexture = null;
      }

      if (this.maskTextureView != null) {
         this.maskTextureView.close();
         this.maskTextureView = null;
      }

      if (this.maskTexture != null) {
         this.maskTexture.close();
         this.maskTexture = null;
      }
   }

   public boolean captureSceneBeforeBlock() {
      if (!this.enabled) {
         return false;
      } else {
         this.ensureInitialized();
         class_276 fb = this.client.method_1522();
         if (fb != null && fb.method_30277() != null) {
            int width = fb.field_1482;
            int height = fb.field_1481;
            this.ensureTextures(width, height);
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(fb.method_30277(), this.sceneBeforeTexture, 0, 0, 0, 0, 0, width, height);
            if (fb.method_30278() != null) {
               encoder.copyTextureToTexture(fb.method_30278(), this.depthBeforeTexture, 0, 0, 0, 0, 0, width, height);
            }

            this.capturing = true;
            return true;
         } else {
            return false;
         }
      }
   }

   public void captureSceneAfterBlock() {
      if (this.enabled && this.capturing) {
         class_276 fb = this.client.method_1522();
         if (fb != null && fb.method_30277() != null) {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(fb.method_30277(), this.sceneAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
            if (fb.method_30278() != null) {
               encoder.copyTextureToTexture(fb.method_30278(), this.depthAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
            }
         }
      }
   }

   public void renderGlassEffect() {
      if (this.enabled && this.capturing) {
         class_276 fb = this.client.method_1522();
         if (fb != null && fb.method_30277() != null) {
            this.maskDiff
               .createMask(
                  this.maskTextureView,
                  this.sceneBeforeTextureView,
                  this.sceneAfterTextureView,
                  this.depthBeforeTextureView,
                  this.depthAfterTextureView,
                  this.lastWidth,
                  this.lastHeight
               );
            GpuTextureView glassView = this.sceneBeforeTextureView;
            if (this.blurEnabled
               && (
                     glassView = this.kawaseBlur
                        .blur(this.sceneBeforeTexture, this.sceneBeforeTextureView, this.lastWidth, this.lastHeight, this.blurIterations, this.blurRadius)
                  )
                  == null) {
               this.capturing = false;
            } else {
               this.glassComposite
                  .composite(
                     fb.method_71639(),
                     this.sceneBeforeTextureView,
                     glassView,
                     this.maskTextureView,
                     this.lastWidth,
                     this.lastHeight,
                     this.saturation,
                     this.reflect,
                     this.tintColor,
                     this.tintIntensity,
                     0.0F,
                     12.0F,
                     0.0F,
                     18.0F,
                     0.0F,
                     0.0F,
                     6.0F,
                     0.25F,
                     40.0F
                  );
               if (this.outlineEnabled) {
                  this.glassOutline
                     .renderOutline(
                        this.maskTextureView,
                        fb.method_71639(),
                        this.lastWidth,
                        this.lastHeight,
                        this.outlineGlowStrength,
                        this.outlineWidth,
                        this.outlineGlowMode,
                        this.outlineColorFill,
                        this.outlineColor
                     );
               }

               this.capturing = false;
            }
         } else {
            this.capturing = false;
         }
      }
   }

   public void invalidate() {
      this.cleanupTextures();
      if (this.kawaseBlur != null) {
         this.kawaseBlur.close();
      }

      if (this.glassComposite != null) {
         this.glassComposite.close();
      }

      if (this.maskDiff != null) {
         this.maskDiff.close();
      }

      if (this.glassOutline != null) {
         this.glassOutline.close();
      }

      this.kawaseBlur = null;
      this.glassComposite = null;
      this.maskDiff = null;
      this.glassOutline = null;
      this.lastWidth = 0;
      this.lastHeight = 0;
      this.initialized = false;
      this.capturing = false;
   }

   public void close() {
      this.cleanupTextures();
      if (this.kawaseBlur != null) {
         this.kawaseBlur.close();
         this.kawaseBlur = null;
      }

      if (this.glassComposite != null) {
         this.glassComposite.close();
         this.glassComposite = null;
      }

      if (this.maskDiff != null) {
         this.maskDiff.close();
         this.maskDiff = null;
      }

      if (this.glassOutline != null) {
         this.glassOutline.close();
         this.glassOutline = null;
      }

      this.lastWidth = 0;
      this.lastHeight = 0;
      this.initialized = false;
      this.capturing = false;
   }
}
