package ru.mentions.utils.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public final class DrawBatcher {
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final ArrayList<DrawBatcher.Batched> active = new ArrayList<>(8);
   private static boolean enabled = false;

   private DrawBatcher() {
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static void setEnabled(boolean value) {
      if (!value) {
         flushPending();
      }

      enabled = value;
   }

   public static void register(DrawBatcher.Batched owner) {
      if (!active.contains(owner)) {
         active.add(owner);
      }
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static void flushPending() {
      if (!active.isEmpty()) {
         DrawBatcher.Batched[] pending = active.toArray(new DrawBatcher.Batched[0]);
         active.clear();
         Arrays.sort(pending, Comparator.comparingInt(DrawBatcher.Batched::batchLayer));
         class_310 client = class_310.method_1551();
         if (client != null && client.method_1522() != null) {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

            for (DrawBatcher.Batched batch : pending) {
               batch.uploadBatch(encoder);
            }

            GpuBufferSlice dynamicTransforms = writeDynamicTransforms();
            boolean var17 = false /* VF: Semaphore variable */;

            try {
               var17 = true;
               RenderPass var25 = encoder.createRenderPass(() -> "client:2d_batch", client.method_1522().method_71639(), OptionalInt.empty());

               try {
                  RenderSystem.bindDefaultUniforms(var25);
                  var25.setUniform("DynamicTransforms", dynamicTransforms);

                  for (DrawBatcher.Batched batch : pending) {
                     batch.drawBatch(var25);
                  }
               } catch (Throwable var19) {
                  if (var25 != null) {
                     try {
                        var25.close();
                     } catch (Throwable var18) {
                        var19.addSuppressed(var18);
                     }
                  }

                  throw var19;
               }

               if (var25 != null) {
                  var25.close();
                  var17 = false;
               } else {
                  var17 = false;
               }
            } finally {
               if (var17) {
                  for (DrawBatcher.Batched batch : pending) {
                     batch.discardBatch();
                  }
               }
            }

            for (DrawBatcher.Batched batch : pending) {
               batch.discardBatch();
            }
         } else {
            for (DrawBatcher.Batched batch : pending) {
               batch.discardBatch();
            }
         }
      }
   }

   public static void drawImmediate(DrawBatcher.Batched owner) {
      drawImmediate(owner, true);
   }

   public static void drawImmediate(DrawBatcher.Batched owner, boolean upload) {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1522() != null) {
         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
         if (upload) {
            owner.uploadBatch(encoder);
         }

         GpuBufferSlice dynamicTransforms = writeDynamicTransforms();

         try {
            RenderPass pass = encoder.createRenderPass(() -> "client:2d_immediate", client.method_1522().method_71639(), OptionalInt.empty());

            try {
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("DynamicTransforms", dynamicTransforms);
               owner.drawBatch(pass);
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
         } finally {
            owner.discardBatch();
         }
      } else {
         owner.discardBatch();
      }
   }

   private static GpuBufferSlice writeDynamicTransforms() {
      return RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
   }

   @Environment(EnvType.CLIENT)
   public interface Batched {
      default int batchLayer() {
         return 0;
      }

      void uploadBatch(CommandEncoder var1);

      void drawBatch(RenderPass var1);

      void discardBatch();
   }
}
