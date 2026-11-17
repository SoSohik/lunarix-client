package ru.mentions.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
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
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class TexturePipeline implements DrawBatcher.Batched {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/texture");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/texture");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/texture");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("TextureData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline GLOW_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/texture_glow"))
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("TextureData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final int BUFFER_SIZE = 256;
   private static final int INITIAL_UNIFORM_BUFFERS = 64;
   private GpuBuffer[] uniformBuffers;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;
   private final ArrayList<TexturePipeline.TexDraw> pendingDraws = new ArrayList<>();
   private final Map<GpuTexture, GpuTextureView> textureViewCache = new HashMap<>();

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(256);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:texture_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.uniformBuffers = new GpuBuffer[64];
         this.initialized = true;
      }
   }

   private GpuBuffer nextUniformBuffer() {
      int idx = this.pendingDraws.size();
      if (idx == this.uniformBuffers.length) {
         this.uniformBuffers = Arrays.copyOf(this.uniformBuffers, this.uniformBuffers.length * 2);
      }

      GpuBuffer buf = this.uniformBuffers[idx];
      if (buf == null) {
         this.uniformBuffers[idx] = buf = RenderSystem.getDevice().createBuffer(() -> "minecraft:texture_uniform_" + idx, 136, 256L);
      }

      return buf;
   }

   public void drawGlowTexture(
      class_2960 textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation
   ) {
      this.queueDraw(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, rotation, GLOW_PIPELINE);
   }

   public void drawTexture(
      class_2960 textureId, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int[] colors, float[] radii, float smoothness
   ) {
      this.drawTexture(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, 0.0F);
   }

   public void drawTexture(
      class_2960 textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation
   ) {
      this.queueDraw(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, rotation, PIPELINE);
   }

   private void queueDraw(
      class_2960 textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation,
      RenderPipeline pipeline
   ) {
      class_310 client = class_310.method_1551();
      if (client.method_1522() != null) {
         class_1044 texture = client.method_1531().method_4619(textureId);
         if (texture != null) {
            GpuTexture gpuTexture = texture.method_68004();
            if (gpuTexture != null) {
               this.ensureInitialized();
               float fixedScreenWidth = (float)client.method_22683().method_4489() / 2.0F;
               float fixedScreenHeight = (float)client.method_22683().method_4506() / 2.0F;
               this.prepareUniformData(x, y, width, height, u0, v0, u1, v1, fixedScreenWidth, fixedScreenHeight, 2.0F, colors, radii, smoothness, rotation);
               GpuBuffer uniformBuffer = this.nextUniformBuffer();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uniformBuffer.slice(0L, (long)this.dataBuffer.remaining()), this.dataBuffer);
               this.pendingDraws.add(new TexturePipeline.TexDraw(pipeline, this.getCachedTextureView(gpuTexture), uniformBuffer));
               if (DrawBatcher.isEnabled()) {
                  DrawBatcher.register(this);
               } else {
                  DrawBatcher.flushPending();
                  DrawBatcher.drawImmediate(this);
               }
            }
         }
      }
   }

   @Override
   public int batchLayer() {
      return 2;
   }

   @Override
   public void uploadBatch(CommandEncoder encoder) {
   }

   @Override
   public void drawBatch(RenderPass pass) {
      if (!this.pendingDraws.isEmpty()) {
         class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         RenderPipeline currentPipeline = null;

         for (TexturePipeline.TexDraw draw : this.pendingDraws) {
            if (!draw.view().isClosed()) {
               if (draw.pipeline() != currentPipeline) {
                  pass.setPipeline(draw.pipeline());
                  pass.setVertexBuffer(0, this.dummyVertexBuffer);
                  currentPipeline = draw.pipeline();
               }

               pass.bindTexture("Sampler0", draw.view(), sampler);
               pass.setUniform("TextureData", draw.uniformBuffer());
               pass.draw(0, 6);
            }
         }

         this.pendingDraws.clear();
      }
   }

   @Override
   public void discardBatch() {
      this.pendingDraws.clear();
   }

   public void flush() {
      if (!this.pendingDraws.isEmpty()) {
         DrawBatcher.drawImmediate(this);
      }
   }

   private void prepareUniformData(
      float x,
      float y,
      float w,
      float h,
      float u0,
      float v0,
      float u1,
      float v1,
      float screenWidth,
      float screenHeight,
      float guiScale,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation
   ) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(screenWidth);
      this.dataBuffer.putFloat(screenHeight);
      this.dataBuffer.putFloat(smoothness);
      this.dataBuffer.putFloat(guiScale);
      this.dataBuffer.putFloat(x);
      this.dataBuffer.putFloat(y);
      this.dataBuffer.putFloat(w);
      this.dataBuffer.putFloat(h);
      this.dataBuffer.putFloat(u0);
      this.dataBuffer.putFloat(v0);
      this.dataBuffer.putFloat(u1);
      this.dataBuffer.putFloat(v1);
      this.dataBuffer.putFloat(radii[0]);
      this.dataBuffer.putFloat(radii[1]);
      this.dataBuffer.putFloat(radii[2]);
      this.dataBuffer.putFloat(radii[3]);
      float rotationRadians = (float)Math.toRadians((double)rotation);
      this.dataBuffer.putFloat(rotationRadians);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);

      for (int i = 0; i < 4; i++) {
         int color = i < colors.length ? colors[i] : colors[colors.length - 1];
         float a = (float)(color >> 24 & 0xFF) / 255.0F;
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         this.dataBuffer.putFloat(r);
         this.dataBuffer.putFloat(g);
         this.dataBuffer.putFloat(b);
         this.dataBuffer.putFloat(a);
      }

      this.dataBuffer.flip();
   }

   private GpuTextureView getCachedTextureView(GpuTexture gpuTexture) {
      GpuTextureView view = this.textureViewCache.get(gpuTexture);
      if (view == null || view.isClosed()) {
         this.textureViewCache.entrySet().removeIf(e -> {
            if (!e.getKey().isClosed() && !e.getValue().isClosed()) {
               return false;
            } else {
               if (!e.getValue().isClosed()) {
                  e.getValue().close();
               }

               return true;
            }
         });
         view = RenderSystem.getDevice().createTextureView(gpuTexture);
         this.textureViewCache.put(gpuTexture, view);
      }

      return view;
   }

   public void close() {
      this.pendingDraws.clear();

      for (GpuTextureView view : this.textureViewCache.values()) {
         if (!view.isClosed()) {
            view.close();
         }
      }

      this.textureViewCache.clear();
      if (this.uniformBuffers != null) {
         for (GpuBuffer buf : this.uniformBuffers) {
            if (buf != null) {
               buf.close();
            }
         }

         this.uniformBuffers = null;
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

   @Environment(EnvType.CLIENT)
   private static record TexDraw(RenderPipeline pipeline, GpuTextureView view, GpuBuffer uniformBuffer) {
   }
}
