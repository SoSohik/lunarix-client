package ru.mentions.utils.render;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_276;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public class GlassHandsRenderer {
   private static GlassHandsRenderer instance;
   private final class_310 client;
   private KawaseBlurPipeline kawaseBlur;
   private GlassCompositePipeline glassComposite;
   private MaskDiffPipeline maskDiff;
   private GlassOutlinePipeline glassOutline;
   private BurningHandsPipeline burningHands;
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
   private GpuTexture burnGlowTexture;
   private GpuTextureView burnGlowTextureView;
   private final GpuTexture[] burnTrailTextures = new GpuTexture[2];
   private final GpuTextureView[] burnTrailTextureViews = new GpuTextureView[2];
   private int burnTrailIndex = 0;
   private int lastWidth = 0;
   private int lastHeight = 0;
   private boolean capturing = false;
   private boolean maskReady = false;
   private boolean keepHandsResult = false;
   private boolean enabled = false;
   private boolean initialized = false;
   private boolean blurEnabled = true;
   private float blurRadius = 6.0F;
   private int blurIterations = 4;
   private float saturation = 1.0F;
   private boolean reflect = true;
   private int tintColor = 0;
   private float tintIntensity = 0.1F;
   private float edgeGlowIntensity = 0.3F;
   private float edgeSoftness = 0.0F;
   private float iceIntensity = 0.0F;
   private float frostScale = 18.0F;
   private float crackIntensity = 0.5F;
   private float smokeAmount = 0.0F;
   private float smokeScale = 6.0F;
   private float smokeSpeed = 0.25F;
   private float smokeReach = 40.0F;
   private boolean fireEnabled = false;
   private float fireRadius = 15.0F;
   private float fireStrength = 1.18F;
   private float fireSpeed = 1.1F;
   private float fireHeight = 28.0F;
   private float fireDecay = 0.94F;
   private float fireTrailStrength = 1.16F;
   private int fireColor = 16744486;
   private float fireColorMix = 0.0F;
   private boolean fireFillMode = false;
   private boolean outlineEnabled = true;
   private float outlineGlowStrength = 20.0F;
   private int outlineColor = -1;
   private int outlineColorFill = -1;
   private int outlineWidth = 1;
   private int outlineGlowMode = 2;
   private boolean outlineUseItemColor = false;

   public GlassHandsRenderer() {
      this.client = class_310.method_1551();
      instance = this;
   }

   public static GlassHandsRenderer getInstance() {
      if (instance == null) {
         instance = new GlassHandsRenderer();
      }

      return instance;
   }

   public static void resetInstance() {
      if (instance != null) {
         instance.close();
         instance.initialized = false;
      }
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

         if (this.burningHands != null) {
            this.burningHands.close();
         }

         this.kawaseBlur = new KawaseBlurPipeline();
         this.glassComposite = new GlassCompositePipeline();
         this.maskDiff = new MaskDiffPipeline();
         this.glassOutline = new GlassOutlinePipeline();
         this.burningHands = new BurningHandsPipeline();
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

   public boolean isEnabled() {
      return this.enabled;
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

   public void setEdgeGlowIntensity(float intensity) {
      this.edgeGlowIntensity = intensity;
   }

   public void setEdgeSoftness(float px) {
      this.edgeSoftness = Math.max(0.0F, px);
   }

   public void setIceIntensity(float intensity) {
      this.iceIntensity = intensity;
   }

   public void setFrostScale(float scale) {
      this.frostScale = scale;
   }

   public void setCrackIntensity(float intensity) {
      this.crackIntensity = intensity;
   }

   public void setSmokeAmount(float amount) {
      this.smokeAmount = amount;
   }

   public void setSmokeScale(float scale) {
      this.smokeScale = scale;
   }

   public void setSmokeSpeed(float speed) {
      this.smokeSpeed = speed;
   }

   public void setSmokeReach(float reach) {
      this.smokeReach = reach;
   }

   public void setFireEnabled(boolean enabled) {
      this.fireEnabled = enabled;
   }

   public void setFireRadius(float radius) {
      this.fireRadius = radius;
   }

   public void setFireStrength(float strength) {
      this.fireStrength = strength;
   }

   public void setFireSpeed(float speed) {
      this.fireSpeed = speed;
   }

   public void setFireHeight(float height) {
      this.fireHeight = height;
   }

   public void setFireTrailStrength(float strength) {
      this.fireTrailStrength = strength;
   }

   public void setFireColor(int rgb) {
      this.fireColor = rgb;
   }

   public void setFireColorMix(float mix) {
      this.fireColorMix = mix;
   }

   public void setFireFillMode(boolean fill) {
      this.fireFillMode = fill;
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

   public void setOutlineFillColor(int color) {
      this.outlineColorFill = color;
   }

   public void setKeepHandsResult(boolean keep) {
      this.keepHandsResult = keep;
   }

   public void setOutlineWidth(int width) {
      this.outlineWidth = width;
   }

   public void setOutlineGlowMode(int mode) {
      this.outlineGlowMode = mode;
   }

   public void setOutlineUseItemColor(boolean use) {
      this.outlineUseItemColor = use;
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
         this.sceneBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_before", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.sceneBeforeTextureView = RenderSystem.getDevice().createTextureView(this.sceneBeforeTexture);
         this.sceneAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_after", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.sceneAfterTextureView = RenderSystem.getDevice().createTextureView(this.sceneAfterTexture);
         this.depthBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_before", 13, TextureFormat.DEPTH32, width, height, 1, 1);
         this.depthBeforeTextureView = RenderSystem.getDevice().createTextureView(this.depthBeforeTexture);
         this.depthAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_after", 13, TextureFormat.DEPTH32, width, height, 1, 1);
         this.depthAfterTextureView = RenderSystem.getDevice().createTextureView(this.depthAfterTexture);
         this.maskTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_mask", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.maskTextureView = RenderSystem.getDevice().createTextureView(this.maskTexture);
         this.burnGlowTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:burn_glow", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.burnGlowTextureView = RenderSystem.getDevice().createTextureView(this.burnGlowTexture);

         for (int i = 0; i < 2; i++) {
            int idx = i;
            this.burnTrailTextures[i] = RenderSystem.getDevice()
               .createTexture(() -> "minecraft:burn_trail_" + idx, 13, TextureFormat.RGBA8, width, height, 1, 1);
            this.burnTrailTextureViews[i] = RenderSystem.getDevice().createTextureView(this.burnTrailTextures[i]);
            CommandEncoder clearEncoder = RenderSystem.getDevice().createCommandEncoder();
            RenderPass clearPass = clearEncoder.createRenderPass(() -> "minecraft:burn_trail_clear", this.burnTrailTextureViews[i], OptionalInt.of(0));
            if (clearPass != null) {
               clearPass.close();
            }
         }

         this.burnTrailIndex = 0;
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

      if (this.burnGlowTextureView != null) {
         this.burnGlowTextureView.close();
         this.burnGlowTextureView = null;
      }

      if (this.burnGlowTexture != null) {
         this.burnGlowTexture.close();
         this.burnGlowTexture = null;
      }

      for (int i = 0; i < 2; i++) {
         if (this.burnTrailTextureViews[i] != null) {
            this.burnTrailTextureViews[i].close();
            this.burnTrailTextureViews[i] = null;
         }

         if (this.burnTrailTextures[i] != null) {
            this.burnTrailTextures[i].close();
            this.burnTrailTextures[i] = null;
         }
      }
   }

   public void captureSceneBeforeHands() {
      if (this.enabled) {
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
         }
      }
   }

   public void captureSceneAfterHands() {
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
            this.maskReady = true;
            boolean iceActive = this.iceIntensity > 0.001F;
            boolean smokeActive = this.smokeAmount > 0.001F;
            if (this.blurEnabled || iceActive || smokeActive) {
               GpuTextureView blurredView = this.blurEnabled
                  ? this.kawaseBlur
                     .blur(this.sceneBeforeTexture, this.sceneBeforeTextureView, this.lastWidth, this.lastHeight, this.blurIterations, this.blurRadius)
                  : this.sceneBeforeTextureView;
               if (blurredView == null) {
                  this.capturing = false;
                  return;
               }

               float effSaturation = this.blurEnabled ? this.saturation : 1.0F;
               int effTint = this.blurEnabled ? this.tintColor : 0;
               float effTintIntensity = this.blurEnabled ? this.tintIntensity : 0.0F;
               this.glassComposite
                  .composite(
                     fb.method_71639(),
                     this.sceneBeforeTextureView,
                     blurredView,
                     this.maskTextureView,
                     this.lastWidth,
                     this.lastHeight,
                     effSaturation,
                     this.reflect,
                     effTint,
                     effTintIntensity,
                     0.0F,
                     this.edgeSoftness,
                     this.iceIntensity,
                     this.frostScale,
                     this.crackIntensity,
                     this.smokeAmount,
                     this.smokeScale,
                     this.smokeSpeed,
                     this.smokeReach
                  );
            }

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
                     this.outlineColor,
                     this.outlineColorFill,
                     this.sceneAfterTextureView,
                     this.outlineUseItemColor
                  );
            }

            if (this.fireEnabled) {
               this.burningHands
                  .updateUniforms(
                     this.lastWidth,
                     this.lastHeight,
                     this.fireFillMode,
                     this.fireRadius,
                     this.fireStrength,
                     this.fireSpeed,
                     this.fireColorMix,
                     this.fireDecay,
                     this.fireHeight,
                     this.fireSpeed,
                     this.fireTrailStrength,
                     this.fireColor,
                     this.fireStrength
                  );
               this.burningHands.renderGlow(this.burnGlowTextureView, this.sceneAfterTextureView, this.maskTextureView);
               int prev = this.burnTrailIndex;
               int cur = 1 - this.burnTrailIndex;
               this.burningHands
                  .renderTrail(
                     this.burnTrailTextureViews[cur],
                     this.burnTrailTextureViews[prev],
                     this.burnGlowTextureView,
                     this.sceneAfterTextureView,
                     this.maskTextureView
                  );
               this.burnTrailIndex = cur;
               this.burningHands.composite(fb.method_71639(), this.maskTextureView, this.burnGlowTextureView, this.burnTrailTextureViews[cur]);
            }

            if (this.keepHandsResult && this.sceneAfterTexture != null) {
               RenderSystem.getDevice()
                  .createCommandEncoder()
                  .copyTextureToTexture(fb.method_30277(), this.sceneAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
            }

            this.capturing = false;
         } else {
            this.capturing = false;
         }
      }
   }

   public void restoreHandsAfterOverlay() {
      if (this.enabled
         && this.maskReady
         && this.sceneBeforeTexture != null
         && this.sceneAfterTextureView != null
         && this.maskTextureView != null
         && this.glassComposite != null) {
         class_276 fb = this.client.method_1522();
         if (fb != null && fb.method_30277() != null && fb.method_71639() != null) {
            DrawBatcher.flushPending();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(fb.method_30277(), this.sceneBeforeTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
            this.glassComposite
               .composite(
                  fb.method_71639(),
                  this.sceneBeforeTextureView,
                  this.sceneAfterTextureView,
                  this.maskTextureView,
                  this.lastWidth,
                  this.lastHeight,
                  1.0F,
                  false,
                  0,
                  0.0F,
                  0.0F,
                  this.edgeSoftness,
                  0.0F,
                  this.frostScale,
                  0.0F,
                  0.0F,
                  this.smokeScale,
                  this.smokeSpeed,
                  this.smokeReach
               );
            if (this.outlineEnabled && this.glassOutline != null) {
               this.glassOutline
                  .renderOutline(
                     this.maskTextureView,
                     fb.method_71639(),
                     this.lastWidth,
                     this.lastHeight,
                     this.outlineGlowStrength,
                     this.outlineWidth,
                     this.outlineGlowMode,
                     this.outlineColor,
                     this.outlineColorFill,
                     this.sceneAfterTextureView,
                     this.outlineUseItemColor
                  );
            }

            this.maskReady = false;
         } else {
            this.maskReady = false;
         }
      }
   }

   public boolean isCapturing() {
      return this.capturing;
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

      if (this.burningHands != null) {
         this.burningHands.close();
      }

      this.kawaseBlur = null;
      this.glassComposite = null;
      this.maskDiff = null;
      this.glassOutline = null;
      this.burningHands = null;
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

      if (this.burningHands != null) {
         this.burningHands.close();
         this.burningHands = null;
      }

      this.lastWidth = 0;
      this.lastHeight = 0;
      this.initialized = false;
   }
}
