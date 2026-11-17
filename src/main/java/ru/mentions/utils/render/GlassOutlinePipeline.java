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
public class GlassOutlinePipeline {
   private static final int MIP_LEVELS = 3;
   private static final class_2960 DOWN_PIPELINE_ID = class_2960.method_60655("client", "pipeline/glass_outline_down");
   private static final class_2960 DOWN_VSH = class_2960.method_60655("client", "core/glass_outline");
   private static final class_2960 DOWN_FSH = class_2960.method_60655("client", "core/glass_outline_down");
   private static final class_2960 UP_PIPELINE_ID = class_2960.method_60655("client", "pipeline/glass_outline_up");
   private static final class_2960 UP_VSH = class_2960.method_60655("client", "core/glass_outline");
   private static final class_2960 UP_FSH = class_2960.method_60655("client", "core/glass_outline_up");
   private static final class_2960 COMBINE_PIPELINE_ID = class_2960.method_60655("client", "pipeline/glass_outline_combine");
   private static final class_2960 COMBINE_VSH = class_2960.method_60655("client", "core/glass_outline");
   private static final class_2960 COMBINE_FSH = class_2960.method_60655("client", "core/glass_outline_combine");
   private static final RenderPipeline DOWN_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(DOWN_PIPELINE_ID)
         .withVertexShader(DOWN_VSH)
         .withFragmentShader(DOWN_FSH)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("KawaseOutlineData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline UP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(UP_PIPELINE_ID)
         .withVertexShader(UP_VSH)
         .withFragmentShader(UP_FSH)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("KawaseOutlineData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline COMBINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(COMBINE_PIPELINE_ID)
         .withVertexShader(COMBINE_VSH)
         .withFragmentShader(COMBINE_FSH)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("OutlineData", class_10789.field_60031)
         .withSampler("maskTexture")
         .withSampler("glowTexture")
         .withSampler("sceneTexture")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private final GpuTexture[] mipTextures = new GpuTexture[3];
   private final GpuTextureView[] mipViews = new GpuTextureView[3];
   private final int[] mipWidths = new int[3];
   private final int[] mipHeights = new int[3];
   private static final int MAX_FRIEND_RECTS = 8;
   private GpuBuffer kawaseBuffer;
   private GpuBuffer combineBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer kawaseData;
   private ByteBuffer combineData;
   private boolean initialized = false;
   private int lastWidth = 0;
   private int lastHeight = 0;
   private boolean shimmerEnabled = true;
   private float shimmerWidth = 0.04F;
   private float shimmerPeriodSec = 5.0F;
   private final float[] friendRects = new float[32];
   private int friendCount = 0;

   public void setShimmerEnabled(boolean enabled) {
      this.shimmerEnabled = enabled;
   }

   public void setShimmerWidth(float width) {
      this.shimmerWidth = Math.max(0.005F, width);
   }

   public void setShimmerPeriodSec(float sec) {
      this.shimmerPeriodSec = Math.max(0.5F, sec);
   }

   public void setFriendRects(float[] rects, int count) {
      this.friendCount = Math.min(count, 8);
      System.arraycopy(rects, 0, this.friendRects, 0, this.friendCount * 4);
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         this.kawaseData = MemoryUtil.memAlloc(16);
         this.combineData = MemoryUtil.memAlloc(208);
         ByteBuffer dummy = MemoryUtil.memAlloc(4);
         dummy.putInt(0);
         dummy.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_outline_dummy_vertex", 32, dummy);
         MemoryUtil.memFree(dummy);
         this.kawaseBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_outline_kawase_ubo", 136, 16L);
         this.combineBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_outline_combine_ubo", 136, 208L);
         this.initialized = true;
      }
   }

   private void ensureMips(int width, int height) {
      if (width != this.lastWidth || height != this.lastHeight || this.mipTextures[0] == null) {
         this.cleanupMips();
         int w = width;
         int h = height;

         for (int i = 0; i < 3; i++) {
            w = Math.max(1, w / 2);
            h = Math.max(1, h / 2);
            this.mipWidths[i] = w;
            this.mipHeights[i] = h;
            int idx = i;
            this.mipTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_outline_mip_" + idx, 12, TextureFormat.RGBA8, w, h, 1, 1);
            this.mipViews[i] = RenderSystem.getDevice().createTextureView(this.mipTextures[i]);
         }

         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   private void cleanupMips() {
      for (int i = 0; i < 3; i++) {
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

   private void writeCombineUbo(
      CommandEncoder encoder,
      int width,
      int height,
      int fillColor,
      int outlineColor,
      int outlineWidth,
      int glowMode,
      float shimmerT,
      boolean useItemColor,
      float itemColorReach
   ) {
      this.combineData.clear();
      this.putColor(fillColor);
      this.putColor(outlineColor);
      this.combineData.putFloat(1.0F / (float)width);
      this.combineData.putFloat(1.0F / (float)height);
      this.combineData.putFloat((float)outlineWidth);
      this.combineData.putFloat((float)this.friendCount);
      this.combineData.putFloat((float)glowMode);
      this.combineData.putFloat(0.0F);
      this.combineData.putFloat(shimmerT);
      this.combineData.putFloat(this.shimmerEnabled ? 1.0F : 0.0F);
      this.combineData.putFloat(this.shimmerWidth);
      this.combineData.putFloat(useItemColor ? 1.0F : 0.0F);
      this.combineData.putFloat(itemColorReach);
      this.combineData.putFloat(0.0F);

      for (int i = 0; i < 8; i++) {
         int base = i * 4;
         if (i < this.friendCount) {
            this.combineData.putFloat(this.friendRects[base]);
            this.combineData.putFloat(this.friendRects[base + 1]);
            this.combineData.putFloat(this.friendRects[base + 2]);
            this.combineData.putFloat(this.friendRects[base + 3]);
         } else {
            this.combineData.putFloat(0.0F);
            this.combineData.putFloat(0.0F);
            this.combineData.putFloat(0.0F);
            this.combineData.putFloat(0.0F);
         }
      }

      this.combineData.flip();
      encoder.writeToBuffer(this.combineBuffer.slice(), this.combineData);
   }

   private void putColor(int argb) {
      this.combineData.putFloat((float)(argb >> 16 & 0xFF) / 255.0F);
      this.combineData.putFloat((float)(argb >> 8 & 0xFF) / 255.0F);
      this.combineData.putFloat((float)(argb & 0xFF) / 255.0F);
      this.combineData.putFloat((float)(argb >> 24 & 0xFF) / 255.0F);
   }

   public void renderOutline(
      GpuTextureView maskView,
      GpuTextureView targetView,
      int width,
      int height,
      float glowStrength,
      int outlineWidth,
      int glowMode,
      int fillColor,
      int outlineColor
   ) {
      this.renderOutline(maskView, targetView, width, height, glowStrength, outlineWidth, glowMode, fillColor, outlineColor, maskView, false);
   }

   public void renderOutline(
      GpuTextureView maskView,
      GpuTextureView targetView,
      int width,
      int height,
      float glowStrength,
      int outlineWidth,
      int glowMode,
      int fillColor,
      int outlineColor,
      GpuTextureView sceneView,
      boolean useItemColor
   ) {
      this.ensureInitialized();
      this.ensureMips(width, height);
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      class_12137 linearSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      GpuTextureView currentInput = maskView;
      int inputW = width;
      int inputH = height;

      for (int i = 0; i < 3; i++) {
         this.writeKawaseUbo(encoder, 0.5F / (float)inputW, 0.5F / (float)inputH, 1.0F);
         GpuBufferSlice dt = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         GpuTextureView src = currentInput;
         RenderPass rp2 = encoder.createRenderPass(() -> "minecraft:glass_outline_down_" + i, this.mipViews[i], OptionalInt.of(0));

         try {
            rp2.setPipeline(DOWN_PIPELINE);
            rp2.setVertexBuffer(0, this.dummyVertexBuffer);
            rp2.bindTexture("Sampler0", src, linearSampler);
            RenderSystem.bindDefaultUniforms(rp2);
            rp2.setUniform("DynamicTransforms", dt);
            rp2.setUniform("KawaseOutlineData", this.kawaseBuffer);
            rp2.draw(0, 6);
         } catch (Throwable var38) {
            if (rp2 != null) {
               try {
                  rp2.close();
               } catch (Throwable var35) {
                  var38.addSuppressed(var35);
               }
            }

            throw var38;
         }

         if (rp2 != null) {
            rp2.close();
         }

         currentInput = this.mipViews[i];
         inputW = this.mipWidths[i];
         inputH = this.mipHeights[i];
      }

      int var40 = 2;

      while (var40 > 0) {
         float rawOffset = Math.max(0.5F, glowStrength / 4.0F);
         float offset = Math.min(rawOffset, 4.0F);
         this.writeKawaseUbo(encoder, 0.5F / (float)this.mipWidths[var40], 0.5F / (float)this.mipHeights[var40], offset);
         GpuBufferSlice dt = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         int fi = var40--;
         RenderPass rp = encoder.createRenderPass(() -> "minecraft:glass_outline_up_" + (fi - 1), this.mipViews[fi - 1], OptionalInt.of(0));

         try {
            rp.setPipeline(UP_PIPELINE);
            rp.setVertexBuffer(0, this.dummyVertexBuffer);
            rp.bindTexture("Sampler0", this.mipViews[fi], linearSampler);
            RenderSystem.bindDefaultUniforms(rp);
            rp.setUniform("DynamicTransforms", dt);
            rp.setUniform("KawaseOutlineData", this.kawaseBuffer);
            rp.draw(0, 6);
         } finally {
            if (rp != null) {
               rp.close();
            }
         }
      }

      long periodMs = (long)(this.shimmerPeriodSec * 1000.0F);
      float shimmerT = (float)(System.currentTimeMillis() % periodMs) / (float)periodMs;
      float itemColorReach = Math.max(8.0F, glowStrength + (float)outlineWidth + 6.0F);
      this.writeCombineUbo(encoder, width, height, fillColor, outlineColor, outlineWidth, glowMode, shimmerT, useItemColor, itemColorReach);
      GpuBufferSlice dt = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass rp = encoder.createRenderPass(() -> "minecraft:glass_outline_combine", targetView, OptionalInt.empty());

      try {
         rp.setPipeline(COMBINE_PIPELINE);
         rp.setVertexBuffer(0, this.dummyVertexBuffer);
         rp.bindTexture("maskTexture", maskView, linearSampler);
         rp.bindTexture("glowTexture", this.mipViews[0], linearSampler);
         rp.bindTexture("sceneTexture", sceneView, linearSampler);
         RenderSystem.bindDefaultUniforms(rp);
         rp.setUniform("DynamicTransforms", dt);
         rp.setUniform("OutlineData", this.combineBuffer);
         rp.draw(0, 6);
      } finally {
         if (rp != null) {
            rp.close();
         }
      }
   }

   public void close() {
      this.cleanupMips();
      if (this.kawaseBuffer != null) {
         this.kawaseBuffer.close();
         this.kawaseBuffer = null;
      }

      if (this.combineBuffer != null) {
         this.combineBuffer.close();
         this.combineBuffer = null;
      }

      if (this.dummyVertexBuffer != null) {
         this.dummyVertexBuffer.close();
         this.dummyVertexBuffer = null;
      }

      if (this.kawaseData != null) {
         MemoryUtil.memFree(this.kawaseData);
         this.kawaseData = null;
      }

      if (this.combineData != null) {
         MemoryUtil.memFree(this.combineData);
         this.combineData = null;
      }

      this.initialized = false;
      this.lastWidth = 0;
      this.lastHeight = 0;
   }
}
