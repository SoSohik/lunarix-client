package ru.mentions.module.impl.render;

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
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Prism",
   category = Category.RENDER,
   desc = "Делает мир красивее"
)
@Environment(EnvType.CLIENT)
public class Prism extends Module {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/prism");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/shader_sky_fullscreen");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/prism");
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int UBO_SIZE = 32;
   private static final float COPY_MODE = 4.0F;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("PrismData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   public ModeSetting style = new ModeSetting(this, "Стиль", "Retro", "Neon", "Dream", "Chrome");
   public SliderSetting strength = new SliderSetting(this, "Сила", 1.0F, 0.1F, 2.0F, 0.05F);
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private GpuTexture copyTexture;
   private GpuTextureView copyView;
   private int copyWidth = 0;
   private int copyHeight = 0;
   private boolean initialized = false;
   private long startTime = System.currentTimeMillis();

   @Override
   protected void onEnable() {
      this.startTime = System.currentTimeMillis();
   }

   private int modeId() {
      String var1 = this.style.getValue();

      return switch (var1) {
         case "Neon" -> 1;
         case "Dream" -> 2;
         case "Chrome" -> 3;
         default -> 0;
      };
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(32);
         ByteBuffer dummy = MemoryUtil.memAlloc(4);
         dummy.putInt(0);
         dummy.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:prism_vertex", 32, dummy);
         MemoryUtil.memFree(dummy);
         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "client:prism_ubo", 136, 32L);
         this.initialized = true;
      }
   }

   private void ensureCopyTarget(int width, int height) {
      if (this.copyTexture == null || this.copyWidth != width || this.copyHeight != height) {
         if (this.copyView != null) {
            this.copyView.close();
            this.copyView = null;
         }

         if (this.copyTexture != null) {
            this.copyTexture.close();
            this.copyTexture = null;
         }

         this.copyTexture = RenderSystem.getDevice().createTexture(() -> "client:prism_copy", 12, TextureFormat.RGBA8, width, height, 1, 1);
         this.copyView = RenderSystem.getDevice().createTextureView(this.copyTexture);
         this.copyWidth = width;
         this.copyHeight = height;
      }
   }

   private void writeUniforms(float time, float strength, float mode) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(time);
      this.dataBuffer.putFloat(strength);
      this.dataBuffer.putFloat(mode);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.flip();
      RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
   }

   private void drawPass(CommandEncoder encoder, GpuTextureView target, GpuTextureView source, class_12137 sampler) {
      GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass pass = encoder.createRenderPass(() -> "client:prism_pass", target, OptionalInt.empty());

      try {
         pass.setPipeline(PIPELINE);
         pass.setVertexBuffer(0, this.dummyVertexBuffer);
         pass.bindTexture("Sampler0", source, sampler);
         RenderSystem.bindDefaultUniforms(pass);
         pass.setUniform("DynamicTransforms", transforms);
         pass.setUniform("PrismData", this.uniformBuffer);
         pass.draw(0, 6);
      } catch (Throwable var10) {
         if (pass != null) {
            try {
               pass.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (pass != null) {
         pass.close();
      }
   }

   @EventHandler(
      priority = 500
   )
   public void onRender3D(EventRender3D event) {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1687 != null) {
         class_276 framebuffer = client.method_1522();
         if (framebuffer != null && framebuffer.method_71639() != null) {
            this.ensureInitialized();
            this.ensureCopyTarget(framebuffer.field_1482, framebuffer.field_1481);
            if (this.copyView != null) {
               CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
               class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
               float time = (float)(System.currentTimeMillis() - this.startTime) / 1000.0F;
               this.writeUniforms(time, 0.0F, 4.0F);
               this.drawPass(encoder, this.copyView, framebuffer.method_71639(), sampler);
               this.writeUniforms(time, this.strength.getValue(), (float)this.modeId());
               this.drawPass(encoder, framebuffer.method_71639(), this.copyView, sampler);
            }
         }
      }
   }
}
