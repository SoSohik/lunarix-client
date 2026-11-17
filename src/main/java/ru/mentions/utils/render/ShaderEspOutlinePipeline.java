package ru.mentions.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class ShaderEspOutlinePipeline {
   private static final int MAX_MIPS = 5;
   private static final int UBO_SIZE = 112;
   private static final class_2960 VSH = class_2960.method_60655("client", "core/shader_esp_glow");
   private static final RenderPipeline DOWN_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/shader_esp_down"))
         .withVertexShader(VSH)
         .withFragmentShader(class_2960.method_60655("client", "core/shader_esp_down"))
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("EspKawaseData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline UP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/shader_esp_up"))
         .withVertexShader(VSH)
         .withFragmentShader(class_2960.method_60655("client", "core/shader_esp_up"))
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("EspKawaseData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline COMBINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/shader_esp_combine"))
         .withVertexShader(VSH)
         .withFragmentShader(class_2960.method_60655("client", "core/shader_esp_combine"))
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("EspData", class_10789.field_60031)
         .withSampler("maskTexture")
         .withSampler("glowTexture")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private final GpuTexture[] mipTextures = new GpuTexture[5];
   private final GpuTextureView[] mipViews = new GpuTextureView[5];
   private final int[] mipWidths = new int[5];
   private final int[] mipHeights = new int[5];
   private GpuBuffer kawaseBuffer;
   private GpuBuffer espBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer kawaseData;
   private ByteBuffer espData;
   private boolean initialized = false;
   private int lastWidth = 0;
   private int lastHeight = 0;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.kawaseData = MemoryUtil.memAlloc(16);
         this.espData = MemoryUtil.memAlloc(112);
         ByteBuffer dummy = MemoryUtil.memAlloc(4);
         dummy.putInt(0);
         dummy.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:shader_esp_dummy_vertex", 32, dummy);
         MemoryUtil.memFree(dummy);
         this.kawaseBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:shader_esp_kawase_ubo", 136, 16L);
         this.espBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:shader_esp_ubo", 136, 112L);
         this.initialized = true;
      }
   }

   private void ensureMips(int width, int height) {
      if (width != this.lastWidth || height != this.lastHeight || this.mipTextures[0] == null) {
         this.cleanupMips();
         int w = width;
         int h = height;

         for (int i = 0; i < 5; i++) {
            w = Math.max(1, w / 2);
            h = Math.max(1, h / 2);
            this.mipWidths[i] = w;
            this.mipHeights[i] = h;
            int idx = i;
            this.mipTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:shader_esp_mip_" + idx, 12, TextureFormat.RGBA8, w, h, 1, 1);
            this.mipViews[i] = RenderSystem.getDevice().createTextureView(this.mipTextures[i]);
         }

         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   private void cleanupMips() {
      for (int i = 0; i < 5; i++) {
         if (this.mipViews[i] != null) {
            this.mipViews[i].close();
            this.mipViews[i] = null;
         }

         if (this.mipTextures[i] != null) {
            this.mipTextures[i].close();
            this.mipTextures[i] = null;
         }
      }
   }

   private void writeKawaseUbo(CommandEncoder encoder, float halfPixelX, float halfPixelY, float offset) {
      this.kawaseData.clear();
      this.kawaseData.putFloat(halfPixelX);
      this.kawaseData.putFloat(halfPixelY);
      this.kawaseData.putFloat(offset);
      this.kawaseData.putFloat(0.0F);
      this.kawaseData.flip();
      encoder.writeToBuffer(this.kawaseBuffer.slice(), this.kawaseData);
   }

   private void writeEspUbo(CommandEncoder encoder, int width, int height, ShaderEspOutlinePipeline.Params p) {
      this.espData.clear();
      this.putRgb(p.color);
      this.espData.putFloat(clamp(p.opacity, 0.0F, 1.0F));
      this.putRgb(p.friendColor);
      this.espData.putFloat(p.friendEnabled ? 1.0F : 0.0F);
      this.espData.putFloat(1.0F / (float)width);
      this.espData.putFloat(1.0F / (float)height);
      this.espData.putFloat((float)Math.max(0, Math.min(5, p.outlineWidth)));
      this.espData.putFloat((float)p.outlineMode);
      this.espData.putFloat(Math.max(0.0F, p.glowStrength));
      this.espData.putFloat(Math.max(0.05F, p.glowFalloff));
      this.espData.putFloat(Math.max(0.0F, p.fillOpacity));
      this.espData.putFloat(Math.max(0.0F, p.innerGlow));
      this.espData.putFloat(Math.max(0.0F, p.outlineStrength));
      this.espData.putFloat(clamp(p.outlineWhite, 0.0F, 1.0F));
      this.espData.putFloat(p.glowEnabled ? 1.0F : 0.0F);
      this.espData.putFloat(p.fillEnabled ? 1.0F : 0.0F);
      this.espData.putFloat(phase(p.shimmerPeriodSec));
      this.espData.putFloat(p.shimmerEnabled ? 1.0F : 0.0F);
      this.espData.putFloat(Math.max(0.005F, p.shimmerWidth));
      this.espData.putFloat(Math.max(0.0F, p.shimmerBrightness));
      this.espData.putFloat(p.pulseEnabled ? clamp(p.pulseAmount, 0.0F, 1.0F) : 0.0F);
      this.espData.putFloat(phase(1.0F / Math.max(0.05F, p.pulseSpeed)));
      this.espData.putFloat(Math.max(0.0F, p.saturation));
      this.espData.putFloat(p.outlineEnabled ? 1.0F : 0.0F);
      this.espData.flip();
      encoder.writeToBuffer(this.espBuffer.slice(), this.espData);
   }

   private void putRgb(int argb) {
      this.espData.putFloat((float)(argb >> 16 & 0xFF) / 255.0F);
      this.espData.putFloat((float)(argb >> 8 & 0xFF) / 255.0F);
      this.espData.putFloat((float)(argb & 0xFF) / 255.0F);
   }

   private static float clamp(float v, float min, float max) {
      return v < min ? min : Math.min(v, max);
   }

   private static float phase(float periodSec) {
      long periodMs = Math.max(1L, (long)(periodSec * 1000.0F));
      return (float)(System.currentTimeMillis() % periodMs) / (float)periodMs;
   }

   public void render(GpuTextureView maskView, GpuTextureView targetView, int width, int height, ShaderEspOutlinePipeline.Params p) {
      this.ensureInitialized();
      this.ensureMips(width, height);
      float radius = clamp(p.glowRadius, 0.0F, 1.0F);
      int levels = Math.max(1, Math.min(5, 1 + Math.round(radius * 4.0F)));
      float upOffset = 0.5F + radius * 3.5F;
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      class_12137 linearSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      GpuTextureView currentInput = maskView;
      int inputW = width;
      int inputH = height;

      for (int i = 0; i < levels; i++) {
         this.writeKawaseUbo(encoder, 0.5F / (float)inputW, 0.5F / (float)inputH, 1.0F);
         GpuBufferSlice dt = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         GpuTextureView src = currentInput;
         RenderPass rp = encoder.createRenderPass(() -> "minecraft:shader_esp_down_" + i, this.mipViews[i], OptionalInt.of(0));

         try {
            rp.setPipeline(DOWN_PIPELINE);
            rp.setVertexBuffer(0, this.dummyVertexBuffer);
            rp.bindTexture("Sampler0", src, linearSampler);
            RenderSystem.bindDefaultUniforms(rp);
            rp.setUniform("DynamicTransforms", dt);
            rp.setUniform("EspKawaseData", this.kawaseBuffer);
            rp.draw(0, 6);
         } catch (Throwable var26) {
            if (rp != null) {
               try {
                  rp.close();
               } catch (Throwable var23) {
                  var26.addSuppressed(var23);
               }
            }

            throw var26;
         }

         if (rp != null) {
            rp.close();
         }

         currentInput = this.mipViews[i];
         inputW = this.mipWidths[i];
         inputH = this.mipHeights[i];
      }

      int var28 = levels - 1;

      while (var28 > 0) {
         this.writeKawaseUbo(encoder, 0.5F / (float)this.mipWidths[var28], 0.5F / (float)this.mipHeights[var28], upOffset);
         GpuBufferSlice dt = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         int fi = var28--;
         RenderPass rp = encoder.createRenderPass(() -> "minecraft:shader_esp_up_" + (fi - 1), this.mipViews[fi - 1], OptionalInt.of(0));

         try {
            rp.setPipeline(UP_PIPELINE);
            rp.setVertexBuffer(0, this.dummyVertexBuffer);
            rp.bindTexture("Sampler0", this.mipViews[fi], linearSampler);
            RenderSystem.bindDefaultUniforms(rp);
            rp.setUniform("DynamicTransforms", dt);
            rp.setUniform("EspKawaseData", this.kawaseBuffer);
            rp.draw(0, 6);
         } catch (Throwable var25) {
            if (rp != null) {
               try {
                  rp.close();
               } catch (Throwable var22) {
                  var25.addSuppressed(var22);
               }
            }

            throw var25;
         }

         if (rp != null) {
            rp.close();
         }
      }

      this.writeEspUbo(encoder, width, height, p);
      GpuBufferSlice dt2 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass rp = encoder.createRenderPass(() -> "minecraft:shader_esp_combine", targetView, OptionalInt.empty());

      try {
         rp.setPipeline(COMBINE_PIPELINE);
         rp.setVertexBuffer(0, this.dummyVertexBuffer);
         rp.bindTexture("maskTexture", maskView, linearSampler);
         rp.bindTexture("glowTexture", this.mipViews[0], linearSampler);
         RenderSystem.bindDefaultUniforms(rp);
         rp.setUniform("DynamicTransforms", dt2);
         rp.setUniform("EspData", this.espBuffer);
         rp.draw(0, 6);
      } catch (Throwable var24) {
         if (rp != null) {
            try {
               rp.close();
            } catch (Throwable var21) {
               var24.addSuppressed(var21);
            }
         }

         throw var24;
      }

      if (rp != null) {
         rp.close();
      }
   }

   public void close() {
      this.cleanupMips();
      if (this.kawaseBuffer != null) {
         this.kawaseBuffer.close();
         this.kawaseBuffer = null;
      }

      if (this.espBuffer != null) {
         this.espBuffer.close();
         this.espBuffer = null;
      }

      if (this.dummyVertexBuffer != null) {
         this.dummyVertexBuffer.close();
         this.dummyVertexBuffer = null;
      }

      if (this.kawaseData != null) {
         MemoryUtil.memFree(this.kawaseData);
         this.kawaseData = null;
      }

      if (this.espData != null) {
         MemoryUtil.memFree(this.espData);
         this.espData = null;
      }

      this.initialized = false;
      this.lastWidth = 0;
      this.lastHeight = 0;
   }

   @Environment(EnvType.CLIENT)
   public static final class Params {
      public int color = -16711681;
      public int friendColor = -11141291;
      public boolean friendEnabled = true;
      public float opacity = 1.0F;
      public float saturation = 1.0F;
      public boolean glowEnabled = true;
      public float glowRadius = 0.5F;
      public float glowStrength = 1.4F;
      public float glowFalloff = 1.2F;
      public boolean fillEnabled = true;
      public float fillOpacity = 0.35F;
      public float innerGlow = 0.5F;
      public boolean outlineEnabled = true;
      public int outlineWidth = 2;
      public int outlineMode = 0;
      public float outlineStrength = 1.5F;
      public float outlineWhite = 0.5F;
      public boolean pulseEnabled = false;
      public float pulseSpeed = 1.0F;
      public float pulseAmount = 0.3F;
      public boolean shimmerEnabled = true;
      public float shimmerWidth = 0.04F;
      public float shimmerPeriodSec = 5.0F;
      public float shimmerBrightness = 0.8F;
   }
}
