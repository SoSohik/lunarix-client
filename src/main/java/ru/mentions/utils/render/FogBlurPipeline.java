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
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class FogBlurPipeline {
   private static final int UNIFORM_SIZE = 256;
   private static final float NEAR = 0.05F;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("client", "pipeline/fog_blur"))
         .withVertexShader(class_2960.method_60655("client", "core/fog_blur"))
         .withFragmentShader(class_2960.method_60655("client", "core/fog_blur"))
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("FogData", class_10789.field_60031)
         .withSampler("DepthTex")
         .withSampler("MinecraftTex")
         .withSampler("BlurTex")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static GpuBuffer dummyVertexBuffer;
   private static GpuBuffer uniformBuffer;
   private static ByteBuffer uniformData;
   private static GpuTexture colorCopy;
   private static GpuTextureView colorCopyView;
   private static class_6367 depthCopy;
   private static KawaseBlurPipeline kawaseBlur;
   private static int lastWidth = -1;
   private static int lastHeight = -1;

   private FogBlurPipeline() {
   }

   public static boolean draw(float distance, float saturation, boolean clientColor, int color1, int color2, int color3, int color4) {
      if (!ensureBuffers()) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         if (mc != null && mc.method_1522() != null && mc.method_1522().method_30277() != null) {
            class_276 fb = mc.method_1522();
            int fbW = fb.field_1482;
            int fbH = fb.field_1481;
            ensureTextures(fbW, fbH);
            if (colorCopy != null && depthCopy != null && fb.method_30278() != null && depthCopy.method_30278() != null) {
               CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
               encoder.copyTextureToTexture(fb.method_30277(), colorCopy, 0, 0, 0, 0, 0, fbW, fbH);
               encoder.copyTextureToTexture(fb.method_30278(), depthCopy.method_30278(), 0, 0, 0, 0, 0, fbW, fbH);
               GpuTextureView depthView = depthCopy.method_71640();
               if (depthView == null) {
                  return false;
               } else {
                  GpuTextureView blurredView = kawaseBlur.blur(colorCopy, colorCopyView, fbW, fbH, 3, 2.5F);
                  if (blurredView == null) {
                     blurredView = colorCopyView;
                  }

                  float far = (float)((Integer)mc.field_1690.method_42503().method_41753()).intValue() * 16.0F;
                  writeUniforms(0.05F, far, distance, saturation, clientColor, color1, color2, color3, color4);
                  GpuBufferSlice slice = uniformBuffer.slice(0L, 256L);
                  encoder.writeToBuffer(slice, uniformData);
                  RenderPass pass = encoder.createRenderPass(() -> "client:fog_blur", fb.method_71639(), OptionalInt.empty());

                  boolean bl;
                  try {
                     pass.setPipeline(PIPELINE);
                     pass.setVertexBuffer(0, dummyVertexBuffer);
                     pass.bindTexture("DepthTex", depthView, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                     pass.bindTexture("MinecraftTex", colorCopyView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                     pass.bindTexture("BlurTex", blurredView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                     pass.setUniform("FogData", slice);
                     pass.draw(0, 6);
                     bl = true;
                     if (pass == null) {
                        return bl;
                     }
                  } catch (Throwable var22) {
                     Throwable throwable = var22;

                     try {
                        if (pass != null) {
                           try {
                              pass.close();
                           } catch (Throwable var20) {
                              throwable.addSuppressed(var20);
                           }
                        }

                        throw throwable;
                     } catch (Exception var21) {
                        return false;
                     }
                  }

                  pass.close();
                  return bl;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private static boolean ensureBuffers() {
      if (dummyVertexBuffer != null && uniformBuffer != null && uniformData != null) {
         if (kawaseBlur == null) {
            kawaseBlur = new KawaseBlurPipeline();
         }

         return true;
      } else {
         try {
            ByteBuffer dummy = MemoryUtil.memAlloc(4);
            dummy.putInt(0).flip();
            dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:fog_blur_dummy_vertex", 32, dummy);
            MemoryUtil.memFree(dummy);
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "client:fog_blur_uniform", 136, 256L);
            uniformData = MemoryUtil.memAlloc(256);
            kawaseBlur = new KawaseBlurPipeline();
            return true;
         } catch (Exception var1) {
            return false;
         }
      }
   }

   private static void ensureTextures(int width, int height) {
      if (colorCopy == null || width != lastWidth || height != lastHeight) {
         if (colorCopyView != null) {
            colorCopyView.close();
            colorCopyView = null;
         }

         if (colorCopy != null) {
            colorCopy.close();
            colorCopy = null;
         }

         depthCopy = null;
         colorCopy = RenderSystem.getDevice().createTexture(() -> "client:fog_blur_color", 5, TextureFormat.RGBA8, width, height, 1, 1);
         colorCopyView = RenderSystem.getDevice().createTextureView(colorCopy);
         depthCopy = new class_6367("client_fog_blur_depth", width, height, true);
         lastWidth = width;
         lastHeight = height;
      }
   }

   private static void writeUniforms(
      float near, float far, float distance, float saturation, boolean clientColor, int color1, int color2, int color3, int color4
   ) {
      uniformData.clear();
      uniformData.putFloat(near).putFloat(far).putFloat(distance).putFloat(saturation);
      uniformData.putFloat(clientColor ? 1.0F : 0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
      putColor(uniformData, color1);
      putColor(uniformData, color2);
      putColor(uniformData, color3);
      putColor(uniformData, color4);

      while (uniformData.position() < 256) {
         uniformData.put((byte)0);
      }

      uniformData.flip();
   }

   private static void putColor(ByteBuffer buf, int argb) {
      buf.putFloat((float)(argb >> 16 & 0xFF) / 255.0F);
      buf.putFloat((float)(argb >> 8 & 0xFF) / 255.0F);
      buf.putFloat((float)(argb & 0xFF) / 255.0F);
      buf.putFloat((float)(argb >>> 24 & 0xFF) / 255.0F);
   }
}
