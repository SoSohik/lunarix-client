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
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.system.MemoryUtil;
import ru.mentions.Client;
import ru.mentions.module.impl.misc.Optimization;

@Environment(EnvType.CLIENT)
public class BlurPipeline implements DrawBatcher.Batched {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/blur");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/blur");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/blur");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("BlurData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final int BUFFER_SIZE = 128;
   private static final int INITIAL_UNIFORM_BUFFERS = 64;
   private GpuBuffer[] uniformBuffers;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;
   private final ArrayList<GpuBuffer> pendingDraws = new ArrayList<>();

   private int getFixedScaledWidth() {
      class_310 client = class_310.method_1551();
      return client != null && client.method_22683() != null ? (int)Math.ceil((double)client.method_22683().method_4489() / 2.0) : 960;
   }

   private int getFixedScaledHeight() {
      class_310 client = class_310.method_1551();
      return client != null && client.method_22683() != null ? (int)Math.ceil((double)client.method_22683().method_4506() / 2.0) : 540;
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(128);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:blur_vertex", 32, dummyData);
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
         this.uniformBuffers[idx] = buf = RenderSystem.getDevice().createBuffer(() -> "client:blur_uniform_" + idx, 136, 128L);
      }

      return buf;
   }

   public void drawBlur(float x, float y, float width, float height, float radius, float[] radii, int color) {
      this.drawBlur(x, y, width, height, radius, radii, color, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   public void drawGlassBlur(
      float x, float y, float width, float height, float alpha, float[] radii, int tintColor, float distortion, float waveSize, float edgeLight, float shine
   ) {
      this.drawBlur(x, y, width, height, alpha, radii, tintColor, distortion, waveSize, edgeLight, shine);
   }

   private void drawBlur(
      float x, float y, float width, float height, float radius, float[] radii, int color, float distortion, float waveSize, float edgeLight, float shine
   ) {
      if (!(width <= 0.0F) && !(height <= 0.0F) && !(radius <= 0.0F)) {
         if (Optimization.flatPanels()) {
            Client.get().render2D().getRectPipeline().drawRect(x, y, width, height, new int[]{color}, radii);
         } else {
            if (Optimization.simpleGlass()) {
               distortion = 0.0F;
               waveSize = 0.0F;
               edgeLight = 0.0F;
               shine = 0.0F;
            }

            ScreenBlur.markUsed();
            if (ScreenBlur.isReady()) {
               class_310 client = class_310.method_1551();
               if (client.method_1522() != null) {
                  this.ensureInitialized();
                  int fbWidth = ScreenBlur.getCachedWidth();
                  int fbHeight = ScreenBlur.getCachedHeight();
                  int fixedScreenWidth = this.getFixedScaledWidth();
                  int fixedScreenHeight = this.getFixedScaledHeight();
                  this.prepareUniformData(
                     x,
                     y,
                     width,
                     height,
                     (float)fixedScreenWidth,
                     (float)fixedScreenHeight,
                     fbWidth,
                     fbHeight,
                     2.0F,
                     radius,
                     radii,
                     color,
                     distortion,
                     waveSize,
                     edgeLight,
                     shine
                  );
                  GpuBuffer uniformBuffer = this.nextUniformBuffer();
                  RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uniformBuffer.slice(0L, (long)this.dataBuffer.remaining()), this.dataBuffer);
                  this.pendingDraws.add(uniformBuffer);
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
   }

   @Override
   public int batchLayer() {
      return -1;
   }

   @Override
   public void uploadBatch(CommandEncoder encoder) {
   }

   @Override
   public void drawBatch(RenderPass pass) {
      if (!this.pendingDraws.isEmpty()) {
         if (!ScreenBlur.isReady()) {
            this.pendingDraws.clear();
         } else {
            class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.dummyVertexBuffer);
            pass.bindTexture("Sampler0", ScreenBlur.getBlurredView(), sampler);

            for (GpuBuffer uniformBuffer : this.pendingDraws) {
               pass.setUniform("BlurData", uniformBuffer);
               pass.draw(0, 6);
            }

            this.pendingDraws.clear();
         }
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
      float width,
      float height,
      float screenWidth,
      float screenHeight,
      int fbWidth,
      int fbHeight,
      float guiScale,
      float blurRadius,
      float[] radii,
      int color,
      float distortion,
      float waveSize,
      float edgeLight,
      float shine
   ) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(x);
      this.dataBuffer.putFloat(y);
      this.dataBuffer.putFloat(width);
      this.dataBuffer.putFloat(height);
      this.dataBuffer.putFloat(screenWidth);
      this.dataBuffer.putFloat(screenHeight);
      this.dataBuffer.putFloat(guiScale);
      this.dataBuffer.putFloat(blurRadius);
      this.dataBuffer.putFloat((float)fbWidth);
      this.dataBuffer.putFloat((float)fbHeight);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(radii[0]);
      this.dataBuffer.putFloat(radii[1]);
      this.dataBuffer.putFloat(radii[2]);
      this.dataBuffer.putFloat(radii[3]);
      float a = (float)(color >> 24 & 0xFF) / 255.0F;
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      this.dataBuffer.putFloat(r);
      this.dataBuffer.putFloat(g);
      this.dataBuffer.putFloat(b);
      this.dataBuffer.putFloat(a);
      this.dataBuffer.putFloat(Math.max(0.0F, distortion));
      this.dataBuffer.putFloat(Math.max(0.0F, waveSize));
      this.dataBuffer.putFloat(Math.max(0.0F, edgeLight));
      this.dataBuffer.putFloat(Math.max(0.0F, shine));
      this.dataBuffer.flip();
   }

   public void close() {
      this.pendingDraws.clear();
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
}
