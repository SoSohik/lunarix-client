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
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mentions.module.impl.pasted.CustomSky;
import ru.mentions.module.impl.render.ShaderSky;

@Environment(EnvType.CLIENT)
public class ShaderSkyRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger("client/ShaderSky");
   private boolean passLogged = false;
   private static final class_2960 SHADER_PIPELINE_ID = class_2960.method_60655("client", "pipeline/shader_sky");
   private static final class_2960 BLUR_PIPELINE_ID = class_2960.method_60655("client", "pipeline/shader_sky_blur");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/shader_sky");
   private static final class_2960 FULLSCREEN_VERTEX_SHADER = class_2960.method_60655("client", "core/shader_sky_fullscreen");
   private static final class_2960 SHADER_FRAGMENT = class_2960.method_60655("client", "core/shader_sky");
   private static final class_2960 BLUR_FRAGMENT = class_2960.method_60655("client", "core/shader_sky_blur");
   private static final RenderPipeline SHADER_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(SHADER_PIPELINE_ID)
         .withVertexShader(FULLSCREEN_VERTEX_SHADER)
         .withFragmentShader(SHADER_FRAGMENT)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("ShaderSkyData", class_10789.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline BLUR_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(BLUR_PIPELINE_ID)
         .withVertexShader(FULLSCREEN_VERTEX_SHADER)
         .withFragmentShader(BLUR_FRAGMENT)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("ShaderSkyData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int BUFFER_SIZE = 144;
   private static ShaderSkyRenderer instance;
   private final class_310 client;
   private final KawaseBlurPipeline blurPipeline = new KawaseBlurPipeline();
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private GpuBuffer celestialVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;
   private boolean enabled = false;
   private long startTime = System.currentTimeMillis();

   public ShaderSkyRenderer() {
      this.client = class_310.method_1551();
      instance = this;
   }

   public static ShaderSkyRenderer getInstance() {
      if (instance == null) {
         instance = new ShaderSkyRenderer();
      }

      return instance;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (enabled) {
         this.ensureInitialized();
         this.startTime = System.currentTimeMillis();
         this.passLogged = false;
      }
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(144);
         ByteBuffer dummy = MemoryUtil.memAlloc(4);
         dummy.putInt(0);
         dummy.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:shader_sky_dummy_vertex", 32, dummy);
         MemoryUtil.memFree(dummy);
         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "client:shader_sky_uniform", 136, 144L);
         ByteBuffer celestialVertices = createCelestialQuad();
         this.celestialVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:shader_sky_celestial_vertex", 32, celestialVertices);
         MemoryUtil.memFree(celestialVertices);
         this.initialized = true;
      }
   }

   public void renderSky() {
      ShaderSky ss = ShaderSky.getInstance();
      CustomSky cs = CustomSky.getInstance();
      boolean ssOn = ss != null && ss.isEnabled();
      boolean csOn = cs != null && cs.isEnabled();
      if (this.enabled && (ssOn || csOn)) {
         class_276 framebuffer = this.client.method_1522();
         if (framebuffer != null && framebuffer.method_71639() != null) {
            this.ensureInitialized();
            if (ssOn && ss.mode.is("Blur")) {
               this.renderBlur(framebuffer, ss);
            } else {
               this.writeUniforms(framebuffer.field_1482, framebuffer.field_1481, ss, cs);
               this.drawSkybox(framebuffer);
            }
         }
      }
   }

   public void renderCelestialShader() {
      ShaderSky ss = ShaderSky.getInstance();
      CustomSky cs = CustomSky.getInstance();
      boolean ssOn = ss != null && ss.isEnabled();
      boolean csOn = cs != null && cs.isEnabled();
      if (this.enabled && (ssOn || csOn) && (!ssOn || !ss.mode.is("Blur"))) {
         class_276 framebuffer = this.client.method_1522();
         if (framebuffer != null && framebuffer.method_71639() != null) {
            this.ensureInitialized();
            this.writeUniforms(framebuffer.field_1482, framebuffer.field_1481, ss, cs);
            this.drawSkybox(framebuffer);
         }
      }
   }

   private void renderBlur(class_276 framebuffer, ShaderSky module) {
      if (framebuffer.method_30277() != null && framebuffer.method_71639() != null) {
         GpuTextureView blurred = this.blurPipeline
            .blur(framebuffer.method_30277(), framebuffer.method_71639(), framebuffer.field_1482, framebuffer.field_1481, 3, 3.0F);
         if (blurred != null) {
            this.writeUniforms(framebuffer.field_1482, framebuffer.field_1481, module, null);
            this.drawFullscreen(framebuffer, BLUR_PIPELINE, blurred);
         }
      }
   }

   private void writeUniforms(int width, int height, ShaderSky ssModule, CustomSky csModule) {
      float alpha = 1.0F;
      String modeName = "Aurora";
      float speed = 1.0F;
      float scale = 1.1F;
      float intensity = 1.0F;
      float stars = 0.85F;
      float vanillaSky = 0.0F;
      Color color1;
      Color color2;
      Color accentColor;
      if (csModule != null && csModule.isEnabled()) {
         alpha = csModule.vanillaSky.getValue() ? 0.78F : 1.0F;
         color1 = new Color(csModule.getResolvedColor1(), true);
         color2 = new Color(csModule.getResolvedColor2(), true);
         accentColor = new Color(csModule.getResolvedColor1() | 0xFF000000, true);
         modeName = csModule.mode.getValue();
         speed = csModule.speed.getValue();
         scale = csModule.scale.getValue();
         intensity = csModule.godrays.getValue() ? csModule.rayDensity.getValue() : 1.0F;
         stars = csModule.stars.getValue();
         vanillaSky = csModule.vanillaSky.getValue() ? 1.0F : 0.0F;
      } else if (ssModule != null && ssModule.isEnabled()) {
         alpha = getShaderAlpha(ssModule);
         color1 = new Color(ShaderSky.geteColor1(), true);
         color2 = new Color(ShaderSky.geteColor2(), true);
         accentColor = new Color(ShaderSky.geteColor1() | 0xFF000000, true);
         modeName = ssModule.mode.getValue();
         speed = ssModule.speed.getValue();
         scale = ssModule.scale.getValue();
         intensity = ssModule.intensity.getValue();
         stars = ssModule.stars.getValue();
         vanillaSky = ssModule.vanillaSky.getValue() ? 1.0F : 0.0F;
      } else {
         color1 = new Color(-11695873, true);
         color2 = new Color(-2145032, true);
         accentColor = color1;
      }

      float fovDeg = (float)((Integer)this.client.field_1690.method_41808().method_41753()).intValue();
      float tanHalf = (float)Math.tan(Math.toRadians((double)fovDeg) * 0.5);
      float aspect = (float)this.client.method_22683().method_4489() / Math.max(1.0F, (float)this.client.method_22683().method_4506());
      float invProjX = aspect * tanHalf;
      float rightX = 1.0F;
      float rightY = 0.0F;
      float rightZ = 0.0F;
      float upX = 0.0F;
      float upY = 1.0F;
      float upZ = 0.0F;
      float fwdX = 0.0F;
      float fwdY = 0.0F;
      float fwdZ = -1.0F;
      ShaderSky anchorModule = ShaderSky.getInstance();
      if (anchorModule != null && anchorModule.anchorToWorld.getValue()) {
         Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix());
         rightX = view.m00();
         rightY = view.m10();
         rightZ = view.m20();
         upX = view.m01();
         upY = view.m11();
         upZ = view.m21();
         fwdX = -view.m02();
         fwdY = -view.m12();
         fwdZ = -view.m22();
      }

      this.dataBuffer.clear();
      this.dataBuffer.putFloat((float)width);
      this.dataBuffer.putFloat((float)height);
      this.dataBuffer.putFloat((float)(System.currentTimeMillis() - this.startTime) / 1000.0F);
      this.dataBuffer.putFloat(alpha);
      putColor(this.dataBuffer, color1, alpha);
      putColor(this.dataBuffer, color2, alpha);
      putColor(this.dataBuffer, accentColor, alpha);
      this.dataBuffer.putFloat((float)getModeId(modeName));
      this.dataBuffer.putFloat(speed);
      this.dataBuffer.putFloat(scale);
      this.dataBuffer.putFloat(intensity);
      this.dataBuffer.putFloat(stars);
      this.dataBuffer.putFloat(vanillaSky);
      this.dataBuffer.putFloat(invProjX);
      this.dataBuffer.putFloat(tanHalf);
      putVec3(this.dataBuffer, rightX, rightY, rightZ);
      putVec3(this.dataBuffer, upX, upY, upZ);
      putVec3(this.dataBuffer, fwdX, fwdY, fwdZ);
      this.dataBuffer.flip();
      RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
   }

   private void drawSkybox(class_276 framebuffer) {
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      Matrix4f skyView = new Matrix4f(RenderSystem.getModelViewMatrix());
      skyView.m30(0.0F).m31(0.0F).m32(0.0F);
      GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106(skyView, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      if (!this.passLogged) {
         this.passLogged = true;
         ShaderSky skyModule = ShaderSky.getInstance();
         LOGGER.info(
            "[ShaderSky] celestial pass created: {}x{}, mode={}",
            new Object[]{framebuffer.field_1482, framebuffer.field_1481, skyModule != null ? skyModule.mode.getValue() : "?"}
         );
      }

      RenderPass renderPass = encoder.createRenderPass(() -> "client:shader_sky_pass", framebuffer.method_71639(), OptionalInt.empty());

      try {
         renderPass.setPipeline(SHADER_PIPELINE);
         renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
         RenderSystem.bindDefaultUniforms(renderPass);
         renderPass.setUniform("DynamicTransforms", transforms);
         renderPass.setUniform("ShaderSkyData", this.uniformBuffer);
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

   private static void putVec3(ByteBuffer buffer, float x, float y, float z) {
      buffer.putFloat(x);
      buffer.putFloat(y);
      buffer.putFloat(z);
      buffer.putFloat(0.0F);
   }

   private static void putColor(ByteBuffer buffer, Color color, float opacity) {
      buffer.putFloat((float)color.getRed() / 255.0F);
      buffer.putFloat((float)color.getGreen() / 255.0F);
      buffer.putFloat((float)color.getBlue() / 255.0F);
      buffer.putFloat((float)color.getAlpha() / 255.0F * opacity);
   }

   private static int getModeId(String mode) {
      return switch (mode) {
         case "Night", "Deep Space", "Purple Space" -> 1;
         case "Snow" -> 2;
         case "Sky" -> 3;
         case "Star" -> 4;
         case "Glow" -> 5;
         case "Nebula" -> 6;
         case "WildSky" -> 7;
         default -> 0;
      };
   }

   private static float getShaderAlpha(ShaderSky module) {
      if (module.mode.is("Blur")) {
         return module.intensity.getValue();
      } else {
         return module.vanillaSky.getValue() ? 0.78F : 1.0F;
      }
   }

   private void drawFullscreen(class_276 framebuffer, RenderPipeline pipeline, GpuTextureView texture) {
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
         .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass renderPass = encoder.createRenderPass(() -> "client:shader_sky_pass", framebuffer.method_71639(), OptionalInt.empty());

      try {
         renderPass.setPipeline(pipeline);
         renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
         if (texture != null) {
            class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            renderPass.bindTexture("Sampler0", texture, sampler);
         }

         RenderSystem.bindDefaultUniforms(renderPass);
         renderPass.setUniform("DynamicTransforms", transforms);
         renderPass.setUniform("ShaderSkyData", this.uniformBuffer);
         renderPass.draw(0, 6);
      } catch (Throwable var10) {
         if (renderPass != null) {
            try {
               renderPass.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (renderPass != null) {
         renderPass.close();
      }
   }

   private static ByteBuffer createCelestialQuad() {
      float s = 2.0F;
      float[] vertices = new float[]{
         -s,
         -s,
         -s,
         s,
         -s,
         -s,
         s,
         s,
         -s,
         -s,
         -s,
         -s,
         s,
         s,
         -s,
         -s,
         s,
         -s,
         s,
         -s,
         s,
         -s,
         -s,
         s,
         -s,
         s,
         s,
         s,
         -s,
         s,
         -s,
         s,
         s,
         s,
         s,
         s,
         -s,
         -s,
         s,
         -s,
         -s,
         -s,
         -s,
         s,
         -s,
         -s,
         -s,
         s,
         -s,
         s,
         -s,
         -s,
         s,
         s,
         s,
         -s,
         -s,
         s,
         -s,
         s,
         s,
         s,
         s,
         s,
         -s,
         -s,
         s,
         s,
         s,
         s,
         s,
         -s,
         -s,
         s,
         -s,
         s,
         s,
         -s,
         s,
         s,
         s,
         -s,
         s,
         -s,
         s,
         s,
         s,
         -s,
         s,
         s,
         -s,
         -s,
         s,
         s,
         -s,
         s,
         s,
         -s,
         -s,
         -s,
         -s,
         s,
         s,
         -s,
         -s,
         -s,
         -s,
         -s
      };
      ByteBuffer buffer = MemoryUtil.memAlloc(vertices.length * 4);

      for (float vertex : vertices) {
         buffer.putFloat(vertex);
      }

      buffer.flip();
      return buffer;
   }

   public void invalidate() {
      this.enabled = false;
      this.close();
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

      if (this.celestialVertexBuffer != null) {
         this.celestialVertexBuffer.close();
         this.celestialVertexBuffer = null;
      }

      if (this.dataBuffer != null) {
         MemoryUtil.memFree(this.dataBuffer);
         this.dataBuffer = null;
      }

      this.blurPipeline.close();
      this.initialized = false;
   }
}
