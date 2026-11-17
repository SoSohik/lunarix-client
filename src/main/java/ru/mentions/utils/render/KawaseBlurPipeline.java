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
public class KawaseBlurPipeline {
   private static final class_2960 DOWN_PIPELINE_ID = class_2960.method_60655("client", "pipeline/kawase_down");
   private static final class_2960 DOWN_VERTEX_SHADER = class_2960.method_60655("client", "core/kawase_down");
   private static final class_2960 DOWN_FRAGMENT_SHADER = class_2960.method_60655("client", "core/kawase_down");
   private static final class_2960 UP_PIPELINE_ID = class_2960.method_60655("client", "pipeline/kawase_up");
   private static final class_2960 UP_VERTEX_SHADER = class_2960.method_60655("client", "core/kawase_up");
   private static final class_2960 UP_FRAGMENT_SHADER = class_2960.method_60655("client", "core/kawase_up");
   private static final RenderPipeline DOWN_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(DOWN_PIPELINE_ID)
         .withVertexShader(DOWN_VERTEX_SHADER)
         .withFragmentShader(DOWN_FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("KawaseData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline UP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(UP_PIPELINE_ID)
         .withVertexShader(UP_VERTEX_SHADER)
         .withFragmentShader(UP_FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("KawaseData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int MAX_ITERATIONS = 8;
   private static final int MAX_PASSES = 17;
   private static final int UNIFORM_DATA_SIZE = 16;
   private GpuBuffer[] passUniformBuffers;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private GpuTexture[] downTextures;
   private GpuTextureView[] downTextureViews;
   private GpuTexture[] upTextures;
   private GpuTextureView[] upTextureViews;
   private int[] downWidths;
   private int[] downHeights;
   private int[] upWidths;
   private int[] upHeights;
   private GpuTexture finalTexture;
   private GpuTextureView finalTextureView;
   private int lastWidth = 0;
   private int lastHeight = 0;
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(16);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.passUniformBuffers = new GpuBuffer[17];

         for (int i = 0; i < 17; i++) {
            int idx = i;
            this.passUniformBuffers[i] = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_uniform_" + idx, 136, 16L);
         }

         this.downTextures = new GpuTexture[8];
         this.downTextureViews = new GpuTextureView[8];
         this.upTextures = new GpuTexture[8];
         this.upTextureViews = new GpuTextureView[8];
         this.downWidths = new int[8];
         this.downHeights = new int[8];
         this.upWidths = new int[8];
         this.upHeights = new int[8];
         this.initialized = true;
      }
   }

   private void ensureFramebuffers(int width, int height) {
      if (width != this.lastWidth || height != this.lastHeight) {
         this.cleanupFramebuffers();
         this.finalTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_final", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.finalTextureView = RenderSystem.getDevice().createTextureView(this.finalTexture);
         int w = width;
         int h = height;

         for (int i = 0; i < 8; i++) {
            w = Math.max(1, w / 2);
            h = Math.max(1, h / 2);
            int index = i;
            this.downWidths[i] = w;
            this.downHeights[i] = h;
            this.upWidths[i] = w;
            this.upHeights[i] = h;
            this.downTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_down_" + index, 13, TextureFormat.RGBA8, w, h, 1, 1);
            this.downTextureViews[i] = RenderSystem.getDevice().createTextureView(this.downTextures[i]);
            this.upTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_up_" + index, 13, TextureFormat.RGBA8, w, h, 1, 1);
            this.upTextureViews[i] = RenderSystem.getDevice().createTextureView(this.upTextures[i]);
         }

         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   private void cleanupFramebuffers() {
      if (this.finalTextureView != null) {
         this.finalTextureView.close();
         this.finalTextureView = null;
      }

      if (this.finalTexture != null) {
         this.finalTexture.close();
         this.finalTexture = null;
      }

      if (this.downTextureViews != null) {
         for (int i = 0; i < 8; i++) {
            if (this.downTextureViews[i] != null) {
               this.downTextureViews[i].close();
               this.downTextureViews[i] = null;
            }

            if (this.downTextures[i] != null) {
               this.downTextures[i].close();
               this.downTextures[i] = null;
            }

            if (this.upTextureViews[i] != null) {
               this.upTextureViews[i].close();
               this.upTextureViews[i] = null;
            }

            if (this.upTextures[i] != null) {
               this.upTextures[i].close();
               this.upTextures[i] = null;
            }
         }
      }
   }

   public GpuTextureView blur(GpuTexture sourceTexture, GpuTextureView sourceView, int width, int height, int iterations, float offset) {
      if (sourceTexture == null || sourceView == null) {
         return null;
      } else if (!sourceTexture.isClosed() && !sourceView.isClosed()) {
         this.ensureInitialized();
         this.ensureFramebuffers(width, height);
         iterations = Math.min(Math.max(iterations, 1), 8);
         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
         class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         GpuBufferSlice dynTransforms = RenderSystem.getDynamicUniforms()
            .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         GpuTextureView currentSource = sourceView;
         int currentWidth = width;
         int currentHeight = height;
         int passIdx = 0;

         for (int i = 0; i < iterations; i++) {
            GpuBuffer passBuf = this.passUniformBuffers[passIdx++];
            this.writeUniform(encoder, passBuf, currentWidth, currentHeight, offset);
            RenderPass rp = encoder.createRenderPass(() -> "minecraft:kawase_down_" + i, this.downTextureViews[i], OptionalInt.empty());

            try {
               rp.setPipeline(DOWN_PIPELINE);
               rp.setVertexBuffer(0, this.dummyVertexBuffer);
               rp.bindTexture("Sampler0", currentSource, sampler);
               RenderSystem.bindDefaultUniforms(rp);
               rp.setUniform("DynamicTransforms", dynTransforms);
               rp.setUniform("KawaseData", passBuf);
               rp.draw(0, 6);
            } finally {
               if (rp != null) {
                  rp.close();
               }
            }

            currentSource = this.downTextureViews[i];
            currentWidth = this.downWidths[i];
            currentHeight = this.downHeights[i];
         }

         for (int var36 = iterations - 1; var36 >= 0; var36--) {
            GpuBuffer passBuf = this.passUniformBuffers[passIdx++];
            this.writeUniform(encoder, passBuf, currentWidth, currentHeight, offset);
            int fi = var36;
            RenderPass rp = encoder.createRenderPass(() -> "minecraft:kawase_up_" + fi, this.upTextureViews[fi], OptionalInt.empty());

            try {
               rp.setPipeline(UP_PIPELINE);
               rp.setVertexBuffer(0, this.dummyVertexBuffer);
               rp.bindTexture("Sampler0", currentSource, sampler);
               RenderSystem.bindDefaultUniforms(rp);
               rp.setUniform("DynamicTransforms", dynTransforms);
               rp.setUniform("KawaseData", passBuf);
               rp.draw(0, 6);
            } finally {
               if (rp != null) {
                  rp.close();
               }
            }

            currentSource = this.upTextureViews[var36];
            currentWidth = this.upWidths[var36];
            currentHeight = this.upHeights[var36];
         }

         GpuBuffer passBuf2 = this.passUniformBuffers[passIdx];
         this.writeUniform(encoder, passBuf2, currentWidth, currentHeight, offset);
         RenderPass rp2 = encoder.createRenderPass(() -> "minecraft:kawase_final", this.finalTextureView, OptionalInt.empty());

         try {
            rp2.setPipeline(UP_PIPELINE);
            rp2.setVertexBuffer(0, this.dummyVertexBuffer);
            rp2.bindTexture("Sampler0", currentSource, sampler);
            RenderSystem.bindDefaultUniforms(rp2);
            rp2.setUniform("DynamicTransforms", dynTransforms);
            rp2.setUniform("KawaseData", passBuf2);
            rp2.draw(0, 6);
         } catch (Throwable var30) {
            if (rp2 != null) {
               try {
                  rp2.close();
               } catch (Throwable var29) {
                  var30.addSuppressed(var29);
               }
            }

            throw var30;
         }

         if (rp2 != null) {
            rp2.close();
         }

         return this.finalTextureView;
      } else {
         return null;
      }
   }

   private void writeUniform(CommandEncoder encoder, GpuBuffer buf, int width, int height, float offset) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat((float)width);
      this.dataBuffer.putFloat((float)height);
      this.dataBuffer.putFloat(offset);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.flip();
      encoder.writeToBuffer(buf.slice(), this.dataBuffer);
   }

   public void close() {
      this.cleanupFramebuffers();
      if (this.passUniformBuffers != null) {
         for (GpuBuffer buf : this.passUniformBuffers) {
            if (buf != null) {
               buf.close();
            }
         }

         this.passUniformBuffers = null;
      }

      if (this.dummyVertexBuffer != null) {
         this.dummyVertexBuffer.close();
         this.dummyVertexBuffer = null;
      }

      if (this.dataBuffer != null) {
         MemoryUtil.memFree(this.dataBuffer);
         this.dataBuffer = null;
      }

      this.initialized = false;
   }
}
