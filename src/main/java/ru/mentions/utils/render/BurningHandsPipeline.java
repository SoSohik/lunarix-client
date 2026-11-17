package ru.mentions.utils.render;

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
public class BurningHandsPipeline {
   private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
   private static final BlendFunction ADDITIVE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE);
   private static final RenderPipeline GLOW_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/burn_glow"))
         .withVertexShader(class_2960.method_60655("client", "core/burn_glow"))
         .withFragmentShader(class_2960.method_60655("client", "core/burn_glow"))
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("BurnData", class_10789.field_60031)
         .withSampler("HandSampler")
         .withSampler("MaskSampler")
         .withBlend(REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline TRAIL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/burn_trail"))
         .withVertexShader(class_2960.method_60655("client", "core/burn_trail"))
         .withFragmentShader(class_2960.method_60655("client", "core/burn_trail"))
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("BurnData", class_10789.field_60031)
         .withSampler("TrailSampler")
         .withSampler("GlowSampler")
         .withSampler("HandSampler")
         .withSampler("MaskSampler")
         .withBlend(REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline COMPOSITE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/burn_composite"))
         .withVertexShader(class_2960.method_60655("client", "core/burn_composite"))
         .withFragmentShader(class_2960.method_60655("client", "core/burn_composite"))
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("BurnData", class_10789.field_60031)
         .withSampler("MaskSampler")
         .withSampler("GlowSampler")
         .withSampler("TrailSampler")
         .withBlend(ADDITIVE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int BUFFER_SIZE = 64;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(64);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:burn_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.initialized = true;
      }
   }

   public void updateUniforms(
      int width,
      int height,
      boolean fillMode,
      float radiusPx,
      float strength,
      float flameSpeed,
      float colorMix,
      float decay,
      float flameHeightPx,
      float flowSpeed,
      float trailStrength,
      int fireColorRgb,
      float glowStrength
   ) {
      this.ensureInitialized();
      this.dataBuffer.clear();
      float time = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;
      this.dataBuffer.putFloat((float)width);
      this.dataBuffer.putFloat((float)height);
      this.dataBuffer.putFloat(time);
      this.dataBuffer.putFloat(fillMode ? 1.0F : 0.0F);
      this.dataBuffer.putFloat(radiusPx);
      this.dataBuffer.putFloat(strength);
      this.dataBuffer.putFloat(flameSpeed);
      this.dataBuffer.putFloat(colorMix);
      this.dataBuffer.putFloat(decay);
      this.dataBuffer.putFloat(flameHeightPx);
      this.dataBuffer.putFloat(flowSpeed);
      this.dataBuffer.putFloat(trailStrength);
      this.dataBuffer.putFloat((float)(fireColorRgb >> 16 & 0xFF) / 255.0F);
      this.dataBuffer.putFloat((float)(fireColorRgb >> 8 & 0xFF) / 255.0F);
      this.dataBuffer.putFloat((float)(fireColorRgb & 0xFF) / 255.0F);
      this.dataBuffer.putFloat(glowStrength);
      this.dataBuffer.flip();
      int size = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:burn_uniform", 136, (long)size);
      }

      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
   }

   public void renderGlow(GpuTextureView targetView, GpuTextureView handView, GpuTextureView maskView) {
      class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass pass = encoder.createRenderPass(() -> "minecraft:burn_glow_pass", targetView, OptionalInt.of(0));

      try {
         pass.setPipeline(GLOW_PIPELINE);
         pass.setVertexBuffer(0, this.dummyVertexBuffer);
         pass.bindTexture("HandSampler", handView, sampler);
         pass.bindTexture("MaskSampler", maskView, sampler);
         RenderSystem.bindDefaultUniforms(pass);
         pass.setUniform("DynamicTransforms", dynamicTransforms);
         pass.setUniform("BurnData", this.uniformBuffer);
         pass.draw(0, 6);
      } catch (Throwable var11) {
         if (pass != null) {
            try {
               pass.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (pass != null) {
         pass.close();
      }
   }

   public void renderTrail(GpuTextureView targetView, GpuTextureView prevTrailView, GpuTextureView glowView, GpuTextureView handView, GpuTextureView maskView) {
      class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass pass = encoder.createRenderPass(() -> "minecraft:burn_trail_pass", targetView, OptionalInt.of(0));

      try {
         pass.setPipeline(TRAIL_PIPELINE);
         pass.setVertexBuffer(0, this.dummyVertexBuffer);
         pass.bindTexture("TrailSampler", prevTrailView, sampler);
         pass.bindTexture("GlowSampler", glowView, sampler);
         pass.bindTexture("HandSampler", handView, sampler);
         pass.bindTexture("MaskSampler", maskView, sampler);
         RenderSystem.bindDefaultUniforms(pass);
         pass.setUniform("DynamicTransforms", dynamicTransforms);
         pass.setUniform("BurnData", this.uniformBuffer);
         pass.draw(0, 6);
      } catch (Throwable var13) {
         if (pass != null) {
            try {
               pass.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (pass != null) {
         pass.close();
      }
   }

   public void composite(GpuTextureView targetView, GpuTextureView maskView, GpuTextureView glowView, GpuTextureView trailView) {
      class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass pass = encoder.createRenderPass(() -> "minecraft:burn_composite_pass", targetView, OptionalInt.empty());

      try {
         pass.setPipeline(COMPOSITE_PIPELINE);
         pass.setVertexBuffer(0, this.dummyVertexBuffer);
         pass.bindTexture("MaskSampler", maskView, sampler);
         pass.bindTexture("GlowSampler", glowView, sampler);
         pass.bindTexture("TrailSampler", trailView, sampler);
         RenderSystem.bindDefaultUniforms(pass);
         pass.setUniform("DynamicTransforms", dynamicTransforms);
         pass.setUniform("BurnData", this.uniformBuffer);
         pass.draw(0, 6);
      } catch (Throwable var12) {
         if (pass != null) {
            try {
               pass.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (pass != null) {
         pass.close();
      }
   }

   public void close() {
      if (this.uniformBuffer != null) {
         this.uniformBuffer.close();
         this.uniformBuffer = null;
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
