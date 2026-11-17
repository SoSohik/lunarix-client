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
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class MenuRaysPipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/menu_rays");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/menu_rays");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/menu_rays");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final int BUFFER_SIZE = 64;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("MenuRaysData", class_10789.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(64);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:menu_rays_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.initialized = true;
      }
   }

   public void draw(float alpha, int colorA, int colorB, float intensity, float rays, float reach) {
      if (!(alpha <= 0.01F)) {
         class_310 client = class_310.method_1551();
         if (client.method_1522() != null) {
            this.ensureInitialized();
            DrawBatcher.flushPending();
            int framebufferWidth = client.method_22683().method_4489();
            int framebufferHeight = client.method_22683().method_4506();
            this.dataBuffer.clear();
            this.dataBuffer.putFloat((float)framebufferWidth);
            this.dataBuffer.putFloat((float)framebufferHeight);
            this.dataBuffer.putFloat(0.0F);
            this.dataBuffer.putFloat(Math.max(0.0F, Math.min(1.0F, alpha)));
            this.dataBuffer.putFloat((float)(System.currentTimeMillis() % 100000L) / 1000.0F);
            this.dataBuffer.putFloat(intensity);
            this.dataBuffer.putFloat(rays);
            this.dataBuffer.putFloat(reach);
            this.putColor(colorA);
            this.putColor(colorB);
            this.dataBuffer.flip();
            this.uploadAndDraw(client);
         }
      }
   }

   private void putColor(int color) {
      this.dataBuffer.putFloat((float)(color >> 16 & 0xFF) / 255.0F);
      this.dataBuffer.putFloat((float)(color >> 8 & 0xFF) / 255.0F);
      this.dataBuffer.putFloat((float)(color & 0xFF) / 255.0F);
      this.dataBuffer.putFloat((float)(color >> 24 & 0xFF) / 255.0F);
   }

   private void uploadAndDraw(class_310 client) {
      int size = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "client:menu_rays_uniform", 136, (long)size);
      }

      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
      GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass renderPass = encoder.createRenderPass(
         () -> "client:menu_rays_pass", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty()
      );

      try {
         renderPass.setPipeline(PIPELINE);
         renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
         RenderSystem.bindDefaultUniforms(renderPass);
         renderPass.setUniform("DynamicTransforms", dynamicTransforms);
         renderPass.setUniform("MenuRaysData", this.uniformBuffer);
         renderPass.draw(0, 6);
      } catch (Throwable var9) {
         if (renderPass != null) {
            try {
               renderPass.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (renderPass != null) {
         renderPass.close();
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
