package ru.mentions.utils.render.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class ScanWorldCompositePipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/scanworld_composite");
   private static final class_2960 SHADER_ID = class_2960.method_60655("client", "core/scanworld_composite");
   private static final BlendFunction ADD_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ONE, DestFactor.ONE);
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(SHADER_ID)
         .withFragmentShader(SHADER_ID)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("ScanData", class_10789.field_60031)
         .withSampler("DepthSampler")
         .withBlend(ADD_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int BUFFER_SIZE = 256;
   private static final int UNIFORM_RING = 16;
   private GpuBuffer[] uniformBuffers;
   private int uniformRingIndex;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(256);
         ByteBuffer dummy = MemoryUtil.memAlloc(4);
         dummy.putInt(0).flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:scanworld_dummy_vertex", 32, dummy);
         MemoryUtil.memFree(dummy);
         this.uniformBuffers = new GpuBuffer[16];
         this.initialized = true;
      }
   }

   private GpuBuffer nextUniformBuffer() {
      GpuBuffer buf = this.uniformBuffers[this.uniformRingIndex];
      if (buf == null) {
         int idx = this.uniformRingIndex;
         this.uniformBuffers[this.uniformRingIndex] = buf = RenderSystem.getDevice().createBuffer(() -> "client:scanworld_uniform_" + idx, 136, 256L);
      }

      this.uniformRingIndex = (this.uniformRingIndex + 1) % 16;
      return buf;
   }

   public void render(
      GpuTextureView targetView,
      GpuTextureView depthView,
      int width,
      int height,
      class_243 cameraPos,
      class_243 center,
      Matrix4f invView,
      Matrix4f invProj,
      float radius,
      float bandWidth,
      float fade,
      Color outerColor,
      Color midColor,
      Color innerColor,
      Color scanlineColor
   ) {
      this.ensureInitialized();
      this.prepareUniformData(width, height, cameraPos, center, invView, invProj, radius, bandWidth, fade, outerColor, midColor, innerColor, scanlineColor);
      GpuBuffer uniformBuffer = this.nextUniformBuffer();
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      encoder.writeToBuffer(uniformBuffer.slice(0L, (long)this.dataBuffer.remaining()), this.dataBuffer);
      GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      class_12137 depthSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
      RenderPass pass = encoder.createRenderPass(() -> "client:scanworld_pass", targetView, OptionalInt.empty());

      try {
         pass.setPipeline(PIPELINE);
         pass.setVertexBuffer(0, this.dummyVertexBuffer);
         pass.bindTexture("DepthSampler", depthView, depthSampler);
         RenderSystem.bindDefaultUniforms(pass);
         pass.setUniform("DynamicTransforms", dynamicTransforms);
         pass.setUniform("ScanData", uniformBuffer);
         pass.draw(0, 6);
      } catch (Throwable var24) {
         if (pass != null) {
            try {
               pass.close();
            } catch (Throwable var23) {
               var24.addSuppressed(var23);
            }
         }

         throw var24;
      }

      if (pass != null) {
         pass.close();
      }
   }

   private void prepareUniformData(
      int width,
      int height,
      class_243 cameraPos,
      class_243 center,
      Matrix4f invView,
      Matrix4f invProj,
      float radius,
      float bandWidth,
      float fade,
      Color outerColor,
      Color midColor,
      Color innerColor,
      Color scanlineColor
   ) {
      this.dataBuffer.clear();
      putMatrix(this.dataBuffer, invView);
      putMatrix(this.dataBuffer, invProj);
      this.dataBuffer.putFloat((float)cameraPos.field_1352);
      this.dataBuffer.putFloat((float)cameraPos.field_1351);
      this.dataBuffer.putFloat((float)cameraPos.field_1350);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat((float)center.field_1352);
      this.dataBuffer.putFloat((float)center.field_1351);
      this.dataBuffer.putFloat((float)center.field_1350);
      this.dataBuffer.putFloat(radius);
      putColor(this.dataBuffer, outerColor);
      putColor(this.dataBuffer, midColor);
      putColor(this.dataBuffer, innerColor);
      putColor(this.dataBuffer, scanlineColor);
      this.dataBuffer.putFloat((float)width);
      this.dataBuffer.putFloat((float)height);
      this.dataBuffer.putFloat(bandWidth);
      this.dataBuffer.putFloat((float)(System.currentTimeMillis() % 1000000L) / 1000.0F);
      this.dataBuffer.putFloat(fade);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.flip();
   }

   private static void putColor(ByteBuffer buffer, Color color) {
      buffer.putFloat((float)color.getRed() / 255.0F);
      buffer.putFloat((float)color.getGreen() / 255.0F);
      buffer.putFloat((float)color.getBlue() / 255.0F);
      buffer.putFloat((float)color.getAlpha() / 255.0F);
   }

   private static void putMatrix(ByteBuffer buffer, Matrix4f matrix) {
      buffer.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02()).putFloat(matrix.m03());
      buffer.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
      buffer.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
      buffer.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
   }

   public void close() {
      if (this.uniformBuffers != null) {
         for (int i = 0; i < this.uniformBuffers.length; i++) {
            if (this.uniformBuffers[i] != null) {
               this.uniformBuffers[i].close();
               this.uniformBuffers[i] = null;
            }
         }

         this.uniformBuffers = null;
      }

      this.uniformRingIndex = 0;
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
