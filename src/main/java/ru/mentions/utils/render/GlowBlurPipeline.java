package ru.mentions.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
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
public class GlowBlurPipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/glow_blur");
   private static final class_2960 SHADER_ID = class_2960.method_60655("client", "core/glow_blur");
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(SHADER_ID)
         .withFragmentShader(SHADER_ID)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("GlowBlurData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final int UNIFORM_SIZE = 16;
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private GpuBuffer horizontalUniform;
   private GpuBuffer verticalUniform;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private GpuTexture horizontalTexture;
   private GpuTextureView horizontalView;
   private GpuTexture verticalTexture;
   private GpuTextureView verticalView;
   private int lastWidth;
   private int lastHeight;
   private boolean initialized;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(16);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:glow_blur_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.horizontalUniform = RenderSystem.getDevice().createBuffer(() -> "client:glow_blur_horizontal", 136, 16L);
         this.verticalUniform = RenderSystem.getDevice().createBuffer(() -> "client:glow_blur_vertical", 136, 16L);
         this.initialized = true;
      }
   }

   private void ensureFramebuffers(int width, int height) {
      if (this.horizontalTexture == null || width != this.lastWidth || height != this.lastHeight) {
         this.cleanupFramebuffers();
         this.horizontalTexture = RenderSystem.getDevice().createTexture(() -> "client:glow_blur_horizontal", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.horizontalView = RenderSystem.getDevice().createTextureView(this.horizontalTexture);
         this.verticalTexture = RenderSystem.getDevice().createTexture(() -> "client:glow_blur_vertical", 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.verticalView = RenderSystem.getDevice().createTextureView(this.verticalTexture);
         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   private void cleanupFramebuffers() {
      if (this.horizontalView != null) {
         this.horizontalView.close();
         this.horizontalView = null;
      }

      if (this.horizontalTexture != null) {
         this.horizontalTexture.close();
         this.horizontalTexture = null;
      }

      if (this.verticalView != null) {
         this.verticalView.close();
         this.verticalView = null;
      }

      if (this.verticalTexture != null) {
         this.verticalTexture.close();
         this.verticalTexture = null;
      }

      this.lastWidth = 0;
      this.lastHeight = 0;
   }

   public GpuTextureView blur(GpuTextureView sourceView, int width, int height, float radius) {
      if (sourceView != null && !sourceView.isClosed() && width > 0 && height > 0) {
         this.ensureInitialized();
         this.ensureFramebuffers(width, height);
         float safeRadius = Math.max(0.0F, radius);
         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
         class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
            .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         this.writeUniform(encoder, this.horizontalUniform, 1.0F, 0.0F, safeRadius);
         RenderPass pass = encoder.createRenderPass(() -> "client:glow_blur_horizontal", this.horizontalView, OptionalInt.empty());

         try {
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.dummyVertexBuffer);
            pass.bindTexture("Sampler0", sourceView, sampler);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setUniform("GlowBlurData", this.horizontalUniform);
            pass.draw(0, 6);
         } catch (Throwable var15) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var13) {
                  var15.addSuppressed(var13);
               }
            }

            throw var15;
         }

         if (pass != null) {
            pass.close();
         }

         this.writeUniform(encoder, this.verticalUniform, 0.0F, 1.0F, safeRadius);
         pass = encoder.createRenderPass(() -> "client:glow_blur_vertical", this.verticalView, OptionalInt.empty());

         try {
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.dummyVertexBuffer);
            pass.bindTexture("Sampler0", this.horizontalView, sampler);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setUniform("GlowBlurData", this.verticalUniform);
            pass.draw(0, 6);
         } catch (Throwable var14) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var12) {
                  var14.addSuppressed(var12);
               }
            }

            throw var14;
         }

         if (pass != null) {
            pass.close();
         }

         return this.verticalView;
      } else {
         return null;
      }
   }

   private void writeUniform(CommandEncoder encoder, GpuBuffer buffer, float directionX, float directionY, float radius) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(directionX);
      this.dataBuffer.putFloat(directionY);
      this.dataBuffer.putFloat(radius);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.flip();
      encoder.writeToBuffer(buffer.slice(), this.dataBuffer);
   }

   public void close() {
      this.cleanupFramebuffers();
      if (this.horizontalUniform != null) {
         this.horizontalUniform.close();
         this.horizontalUniform = null;
      }

      if (this.verticalUniform != null) {
         this.verticalUniform.close();
         this.verticalUniform = null;
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
