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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class ColorGradePipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/color_grade");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/color_grade");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/color_grade");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final int BUFFER_SIZE = 48;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("ColorGradeData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private GpuTexture sceneTexture;
   private GpuTextureView sceneTextureView;
   private ByteBuffer dataBuffer;
   private int lastWidth;
   private int lastHeight;
   private boolean initialized;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(48);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:grayscale_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "client:color_grade_uniform", 136, 48L);
         this.initialized = true;
      }
   }

   private void ensureSceneTexture(int width, int height) {
      if (this.sceneTexture == null || width != this.lastWidth || height != this.lastHeight) {
         if (this.sceneTextureView != null) {
            this.sceneTextureView.close();
            this.sceneTextureView = null;
         }

         if (this.sceneTexture != null) {
            this.sceneTexture.close();
            this.sceneTexture = null;
         }

         this.sceneTexture = RenderSystem.getDevice().createTexture(() -> "client:grayscale_scene", 5, TextureFormat.RGBA8, width, height, 1, 1);
         this.sceneTextureView = RenderSystem.getDevice().createTextureView(this.sceneTexture);
         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   public void draw(float saturation, float brightness, float contrast, int tint, float intensity, float vignette) {
      if (!(Math.abs(saturation - 1.0F) < 0.001F)
         || !(Math.abs(brightness) < 0.001F)
         || !(Math.abs(contrast - 1.0F) < 0.001F)
         || !(intensity < 0.001F)
         || !(vignette < 0.001F)) {
         class_310 client = class_310.method_1551();
         if (client != null && client.method_1522() != null) {
            if (client.method_1522().method_30277() != null) {
               this.ensureInitialized();
               int width = client.method_1522().field_1482;
               int height = client.method_1522().field_1481;
               if (width > 0 && height > 0) {
                  this.ensureSceneTexture(width, height);
                  DrawBatcher.flushPending();
                  this.dataBuffer.clear();
                  this.dataBuffer.putFloat(Math.clamp(saturation, 0.0F, 2.0F));
                  this.dataBuffer.putFloat(Math.clamp(brightness, -0.5F, 0.5F));
                  this.dataBuffer.putFloat(Math.clamp(contrast, 0.5F, 1.5F));
                  this.dataBuffer.putFloat(Math.clamp(intensity, 0.0F, 0.7F));
                  this.dataBuffer.putFloat((float)(tint >> 16 & 0xFF) / 255.0F);
                  this.dataBuffer.putFloat((float)(tint >> 8 & 0xFF) / 255.0F);
                  this.dataBuffer.putFloat((float)(tint & 0xFF) / 255.0F);
                  this.dataBuffer.putFloat(Math.clamp(vignette, 0.0F, 0.85F));
                  this.dataBuffer.putFloat((float)width / (float)height);
                  this.dataBuffer.putFloat(0.0F);
                  this.dataBuffer.putFloat(0.0F);
                  this.dataBuffer.putFloat(0.0F);
                  this.dataBuffer.flip();
                  CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
                  encoder.copyTextureToTexture(client.method_1522().method_30277(), this.sceneTexture, 0, 0, 0, 0, 0, width, height);
                  encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
                  GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                     .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
                  class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
                  RenderPass renderPass = encoder.createRenderPass(
                     () -> "client:grayscale_pass",
                     client.method_1522().method_71639(),
                     OptionalInt.empty(),
                     client.method_1522().method_71640(),
                     OptionalDouble.empty()
                  );

                  try {
                     renderPass.setPipeline(PIPELINE);
                     renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
                     renderPass.bindTexture("Sampler0", this.sceneTextureView, sampler);
                     RenderSystem.bindDefaultUniforms(renderPass);
                     renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                     renderPass.setUniform("ColorGradeData", this.uniformBuffer);
                     renderPass.draw(0, 6);
                  } catch (Throwable var17) {
                     if (renderPass != null) {
                        try {
                           renderPass.close();
                        } catch (Throwable var16) {
                           var17.addSuppressed(var16);
                        }
                     }

                     throw var17;
                  }

                  if (renderPass != null) {
                     renderPass.close();
                  }
               }
            }
         }
      }
   }

   public void close() {
      if (this.sceneTextureView != null) {
         this.sceneTextureView.close();
         this.sceneTextureView = null;
      }

      if (this.sceneTexture != null) {
         this.sceneTexture.close();
         this.sceneTexture = null;
      }

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

      this.lastWidth = 0;
      this.lastHeight = 0;
      this.initialized = false;
   }
}
