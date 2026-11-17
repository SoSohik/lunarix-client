package ru.mentions.utils.render.shader;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class ScanWorldRenderer {
   private static ScanWorldRenderer instance;
   private final class_310 mc = class_310.method_1551();
   private final ScanWorldCompositePipeline pipeline = new ScanWorldCompositePipeline();
   private GpuTexture depthTexture;
   private GpuTextureView depthTextureView;
   private int lastWidth;
   private int lastHeight;
   private static final int MAX_SCANS = 12;
   private final List<ScanWorldRenderer.Scan> scans = new CopyOnWriteArrayList<>();
   private float duration = 2.5F;
   private float width = 10.0F;
   private float maxRadius = 80.0F;
   private float fadeOutPortion = 0.25F;
   private Color outerColor = new Color(0, 0, 0, 255);
   private Color midColor = new Color(0, 0, 0, 255);
   private Color innerColor = new Color(80, 220, 255, 180);
   private Color scanlineColor = new Color(120, 255, 255, 120);

   private ScanWorldRenderer() {
   }

   public static ScanWorldRenderer getInstance() {
      if (instance == null) {
         instance = new ScanWorldRenderer();
      }

      return instance;
   }

   public void startScan(class_243 center) {
      if (this.scans.size() >= 12) {
         this.scans.remove(0);
      }

      this.scans.add(new ScanWorldRenderer.Scan(center, System.currentTimeMillis()));
   }

   public void stopScan() {
      this.scans.clear();
   }

   public void setDuration(float duration) {
      this.duration = duration;
   }

   public void setFadeOutPortion(float fadeOutPortion) {
      this.fadeOutPortion = fadeOutPortion;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public void setMaxRadius(float maxRadius) {
      this.maxRadius = maxRadius;
   }

   public void setOuterColor(Color color) {
      this.outerColor = color;
   }

   public void setMidColor(Color color) {
      this.midColor = color;
   }

   public void setInnerColor(Color color) {
      this.innerColor = color;
   }

   public void setScanlineColor(Color color) {
      this.scanlineColor = color;
   }

   public void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
      if (!this.scans.isEmpty()) {
         long now = System.currentTimeMillis();
         float safeDuration = Math.max(this.duration, 0.001F);
         this.scans.removeIf(s -> (float)(now - s.startTime()) / 1000.0F > safeDuration);
         if (!this.scans.isEmpty()) {
            class_276 fb = this.mc.method_1522();
            if (fb != null && fb.method_30278() != null && fb.method_71639() != null) {
               int widthPx = fb.field_1482;
               int heightPx = fb.field_1481;
               this.ensureDepthTexture(widthPx, heightPx);
               CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
               encoder.copyTextureToTexture(fb.method_30278(), this.depthTexture, 0, 0, 0, 0, 0, widthPx, heightPx);
               Matrix4f invView = new Matrix4f(viewMatrix).invert();
               Matrix4f invProj = new Matrix4f(projectionMatrix).invert();
               class_243 cameraPos = this.mc.field_1773.method_19418().method_71156();
               float fadeStart = 1.0F - Math.max(0.01F, Math.min(this.fadeOutPortion, 0.95F));

               for (ScanWorldRenderer.Scan scan : this.scans) {
                  float elapsed = (float)(now - scan.startTime()) / 1000.0F;
                  float progress = elapsed / safeDuration;
                  float radius = progress * this.maxRadius;
                  float fade = 1.0F;
                  if (progress >= fadeStart) {
                     float t = (progress - fadeStart) / (1.0F - fadeStart);
                     t = Math.max(0.0F, Math.min(t, 1.0F));
                     fade = 1.0F - t;
                     fade *= fade;
                  }

                  this.pipeline
                     .render(
                        fb.method_71639(),
                        this.depthTextureView,
                        widthPx,
                        heightPx,
                        cameraPos,
                        scan.center(),
                        invView,
                        invProj,
                        radius,
                        this.width,
                        fade,
                        this.outerColor,
                        this.midColor,
                        this.innerColor,
                        this.scanlineColor
                     );
               }
            }
         }
      }
   }

   private void ensureDepthTexture(int width, int height) {
      if (this.depthTexture == null || width != this.lastWidth || height != this.lastHeight) {
         this.cleanupDepthTexture();
         this.depthTexture = RenderSystem.getDevice().createTexture(() -> "client:scanworld_depth_copy", 5, TextureFormat.DEPTH32, width, height, 1, 1);
         this.depthTextureView = RenderSystem.getDevice().createTextureView(this.depthTexture);
         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   private void cleanupDepthTexture() {
      if (this.depthTextureView != null) {
         this.depthTextureView.close();
         this.depthTextureView = null;
      }

      if (this.depthTexture != null) {
         this.depthTexture.close();
         this.depthTexture = null;
      }
   }

   public void close() {
      this.cleanupDepthTexture();
      this.pipeline.close();
      this.scans.clear();
   }

   @Environment(EnvType.CLIENT)
   private static record Scan(class_243 center, long startTime) {
   }
}
