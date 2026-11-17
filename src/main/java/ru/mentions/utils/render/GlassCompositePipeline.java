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
public class GlassCompositePipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/glass_composite");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/glass_composite");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/glass_composite");
   private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("GlassData", class_10789.field_60031)
         .withSampler("SceneSampler")
         .withSampler("BlurSampler")
         .withSampler("MaskSampler")
         .withBlend(REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int BUFFER_SIZE = 80;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(80);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_composite_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.initialized = true;
      }
   }

   public void composite(
      GpuTextureView targetView,
      GpuTextureView sceneView,
      GpuTextureView blurView,
      GpuTextureView maskView,
      int width,
      int height,
      float saturation,
      boolean reflect,
      int tintColor,
      float tintIntensity,
      float edgeGlowIntensity,
      float edgeSoftness,
      float iceIntensity,
      float frostScale,
      float crackIntensity,
      float smokeAmount,
      float smokeScale,
      float smokeSpeed,
      float smokeReach
   ) {
      this.ensureInitialized();
      this.prepareUniformData(
         width,
         height,
         saturation,
         reflect,
         tintColor,
         tintIntensity,
         edgeGlowIntensity,
         edgeSoftness,
         iceIntensity,
         frostScale,
         crackIntensity,
         smokeAmount,
         smokeScale,
         smokeSpeed,
         smokeReach
      );
      int size = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_composite_uniform", 136, (long)size);
      }

      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
      GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      class_12137 linearSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:glass_composite_pass", targetView, OptionalInt.empty());

      try {
         renderPass.setPipeline(PIPELINE);
         renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
         renderPass.bindTexture("SceneSampler", sceneView, linearSampler);
         renderPass.bindTexture("BlurSampler", blurView, linearSampler);
         renderPass.bindTexture("MaskSampler", maskView, linearSampler);
         RenderSystem.bindDefaultUniforms(renderPass);
         renderPass.setUniform("DynamicTransforms", dynamicTransforms);
         renderPass.setUniform("GlassData", this.uniformBuffer);
         renderPass.draw(0, 6);
      } catch (Throwable var28) {
         if (renderPass != null) {
            try {
               renderPass.close();
            } catch (Throwable var27) {
               var28.addSuppressed(var27);
            }
         }

         throw var28;
      }

      if (renderPass != null) {
         renderPass.close();
      }
   }

   private void prepareUniformData(
      int width,
      int height,
      float saturation,
      boolean reflect,
      int tintColor,
      float tintIntensity,
      float edgeGlowIntensity,
      float edgeSoftness,
      float iceIntensity,
      float frostScale,
      float crackIntensity,
      float smokeAmount,
      float smokeScale,
      float smokeSpeed,
      float smokeReach
   ) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat((float)width);
      this.dataBuffer.putFloat((float)height);
      this.dataBuffer.putFloat(saturation);
      this.dataBuffer.putFloat(reflect ? 1.0F : 0.0F);
      float a = (float)(tintColor >> 24 & 0xFF) / 255.0F;
      float r = (float)(tintColor >> 16 & 0xFF) / 255.0F;
      float g = (float)(tintColor >> 8 & 0xFF) / 255.0F;
      float b = (float)(tintColor & 0xFF) / 255.0F;
      this.dataBuffer.putFloat(r);
      this.dataBuffer.putFloat(g);
      this.dataBuffer.putFloat(b);
      this.dataBuffer.putFloat(a);
      this.dataBuffer.putFloat(tintIntensity);
      this.dataBuffer.putFloat(edgeGlowIntensity);
      this.dataBuffer.putFloat(edgeSoftness);
      this.dataBuffer.putFloat(0.0F);
      float time = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;
      this.dataBuffer.putFloat(time);
      this.dataBuffer.putFloat(iceIntensity);
      this.dataBuffer.putFloat(frostScale);
      this.dataBuffer.putFloat(crackIntensity);
      this.dataBuffer.putFloat(smokeAmount);
      this.dataBuffer.putFloat(smokeScale);
      this.dataBuffer.putFloat(smokeSpeed);
      this.dataBuffer.putFloat(smokeReach);
      this.dataBuffer.flip();
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
