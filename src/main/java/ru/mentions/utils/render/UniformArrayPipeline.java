package ru.mentions.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public abstract class UniformArrayPipeline implements DrawBatcher.Batched {
   protected static final float FIXED_GUI_SCALE = 2.0F;
   private static final int HEADER_SIZE = 16;
   private final String debugName;
   private final RenderPipeline renderPipeline;
   private final String uniformName;
   private final int maxItems;
   private final int bufferSize;
   private final int initialUniformBuffers;
   private GpuBuffer[] uniformBuffers;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;
   private int batchedItems = 0;
   private final ArrayList<UniformArrayPipeline.Chunk> chunks = new ArrayList<>();

   protected UniformArrayPipeline(String debugName, RenderPipeline renderPipeline, String uniformName, int maxItems, int itemSize, int initialUniformBuffers) {
      this.debugName = debugName;
      this.renderPipeline = renderPipeline;
      this.uniformName = uniformName;
      this.maxItems = maxItems;
      this.bufferSize = 16 + maxItems * itemSize;
      this.initialUniformBuffers = initialUniformBuffers;
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(this.bufferSize);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:" + this.debugName + "_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.uniformBuffers = new GpuBuffer[this.initialUniformBuffers];
         this.initialized = true;
      }
   }

   private GpuBuffer nextUniformBuffer() {
      int idx = this.chunks.size();
      if (idx == this.uniformBuffers.length) {
         this.uniformBuffers = Arrays.copyOf(this.uniformBuffers, this.uniformBuffers.length * 2);
      }

      GpuBuffer buf = this.uniformBuffers[idx];
      if (buf == null) {
         this.uniformBuffers[idx] = buf = RenderSystem.getDevice()
            .createBuffer(() -> "client:" + this.debugName + "_uniform_" + idx, 136, (long)this.bufferSize);
      }

      return buf;
   }

   protected final ByteBuffer beginItem() {
      this.ensureInitialized();
      if (this.batchedItems >= this.maxItems) {
         this.sealChunk();
      }

      if (this.batchedItems == 0) {
         this.dataBuffer.clear();
         this.dataBuffer.position(16);
      }

      return this.dataBuffer;
   }

   protected final void endItem() {
      this.batchedItems++;
      if (DrawBatcher.isEnabled()) {
         DrawBatcher.register(this);
         if (this.batchedItems >= this.maxItems) {
            this.sealChunk();
         }
      } else {
         DrawBatcher.drawImmediate(this);
      }
   }

   private void sealChunk() {
      if (this.batchedItems != 0) {
         class_310 client = class_310.method_1551();
         float fixedScreenWidth = (float)client.method_22683().method_4489() / 2.0F;
         float fixedScreenHeight = (float)client.method_22683().method_4506() / 2.0F;
         int endPosition = this.dataBuffer.position();
         this.dataBuffer.position(0);
         this.dataBuffer.putFloat(fixedScreenWidth);
         this.dataBuffer.putFloat(fixedScreenHeight);
         this.dataBuffer.putFloat(2.0F);
         this.dataBuffer.putFloat(0.0F);
         this.dataBuffer.position(0);
         this.dataBuffer.limit(endPosition);

         try {
            GpuBuffer uniformBuffer = this.nextUniformBuffer();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uniformBuffer.slice(0L, (long)this.dataBuffer.remaining()), this.dataBuffer);
            this.chunks.add(new UniformArrayPipeline.Chunk(uniformBuffer, this.batchedItems));
         } finally {
            this.dataBuffer.limit(this.dataBuffer.capacity());
            this.dataBuffer.clear();
            this.dataBuffer.position(16);
            this.batchedItems = 0;
         }
      }
   }

   @Override
   public final void uploadBatch(CommandEncoder encoder) {
      this.sealChunk();
   }

   @Override
   public final void drawBatch(RenderPass pass) {
      if (!this.chunks.isEmpty()) {
         pass.setPipeline(this.renderPipeline);
         pass.setVertexBuffer(0, this.dummyVertexBuffer);

         for (UniformArrayPipeline.Chunk chunk : this.chunks) {
            pass.setUniform(this.uniformName, chunk.buffer());
            pass.draw(0, chunk.count() * 6);
         }

         this.chunks.clear();
      }
   }

   @Override
   public final void discardBatch() {
      this.batchedItems = 0;
      this.chunks.clear();
      if (this.dataBuffer != null) {
         this.dataBuffer.limit(this.dataBuffer.capacity());
         this.dataBuffer.clear();
         this.dataBuffer.position(16);
      }
   }

   public final void flush() {
      if (this.batchedItems != 0 || !this.chunks.isEmpty()) {
         DrawBatcher.drawImmediate(this);
      }
   }

   public void close() {
      this.batchedItems = 0;
      this.discardBatch();
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
   private static record Chunk(GpuBuffer buffer, int count) {
   }
}
