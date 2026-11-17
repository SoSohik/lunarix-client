package ru.mentions.cosmetics.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.minecraft.class_10366;
import net.minecraft.class_11219;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4fStack;
import org.lwjgl.system.MemoryUtil;
import ru.mentions.Client;
import ru.mentions.cosmetics.CosmeticEntry;
import ru.mentions.cosmetics.anim.CosmeticAnimator;
import ru.mentions.module.impl.render.Cosmetics;
import ru.mentions.utils.render.Draw;

public final class CosmeticsPreview {
   private static final int FULL_BRIGHT = 15728880;
   private static final class_9799 ALLOCATOR = new class_9799(262144);
   private static final CosmeticAnimator ANIMATOR = new CosmeticAnimator();
   private static class_4598 immediate;
   private static GpuBuffer identityProjection;

   private CosmeticsPreview() {
   }

   public static void thumbnail(CosmeticEntry entry, float centerX, float centerY, float halfSize, float yaw, float alpha) {
      if (entry != null && entry.texture != null && !(alpha <= 0.01F) && !(halfSize <= 1.0F)) {
         Cosmetics module = Client.get().moduleManager().get(Cosmetics.class);
         boolean animated = module == null || module.animations.getValue();
         long now = animated ? System.currentTimeMillis() : 0L;
         CapeRenderer.Shape shape = CosmeticFeatureRenderer.capeShape(module);
         float[] box;
         if (entry.isCape()) {
            box = capeBounds(shape);
         } else {
            if (animated) {
               ANIMATOR.apply(entry, CosmeticAnimator.Pose.IDLE, false, now);
            } else if (entry.model != null) {
               entry.model.resetPose();
            }

            box = GeoTransforms.bounds(entry.model);
         }

         if (box != null) {
            class_310 client = class_310.method_1551();
            float width = (float)client.method_22683().method_4489() * 0.5F;
            float height = (float)client.method_22683().method_4506() * 0.5F;
            if (!(width <= 0.0F) && !(height <= 0.0F)) {
               if (immediate == null) {
                  immediate = class_4597.method_22991(ALLOCATOR);
               }

               class_4587 stack = new class_4587();
               stack.method_34425(PreviewTransform.frame(box, centerX, centerY, halfSize, width, height, yaw));
               int level = Math.max(0, Math.min(255, (int)(255.0F * alpha)));
               int tint = level << 16 | level << 8 | level | 0xFF000000;
               class_4588 consumer = immediate.method_73477(CosmeticRenderLayers.layer(entry.texture));
               if (entry.isCape()) {
                  stack.method_22905(1.0F, -1.0F, 1.0F);
                  CapeRenderer.render(entry, stack.method_23760(), consumer, 15728880, tint, shape, now, 0.0F);
               } else {
                  GeoRenderer.render(entry, stack.method_23760(), consumer, 15728880, tint, now);
               }
            }
         }
      }
   }

   public static void flush(float x, float y, float width, float height, float guiScale) {
      if (immediate != null) {
         try {
            Draw.flush();
            Matrix4fStack view = RenderSystem.getModelViewStack();
            RenderSystem.backupProjectionMatrix();

            try {
               view.pushMatrix();

               try {
                  view.identity();
                  RenderSystem.setProjectionMatrix(identity(), class_10366.field_54954);
                  class_276 framebuffer = class_310.method_1551().method_1522();
                  if (framebuffer != null && framebuffer.method_30278() != null) {
                     CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
                     encoder.clearDepthTexture(framebuffer.method_30278(), 0.0);
                  }

                  class_11219 scissor = RenderSystem.getScissorStateForRenderTypeDraws();
                  boolean enabled = scissor.method_72091();
                  int oldX = scissor.method_72092();
                  int oldY = scissor.method_72093();
                  int oldWidth = scissor.method_72094();
                  int oldHeight = scissor.method_72095();
                  int clipX = (int)(x * guiScale);
                  int clipY = (int)((float)class_310.method_1551().method_22683().method_4506() - (y + height) * guiScale);
                  int clipRight = (int)Math.ceil((double)((x + width) * guiScale));
                  int clipTop = (int)Math.ceil((double)((float)clipY + height * guiScale));
                  if (enabled) {
                     clipX = Math.max(clipX, oldX);
                     clipY = Math.max(clipY, oldY);
                     clipRight = Math.min(clipRight, oldX + oldWidth);
                     clipTop = Math.min(clipTop, oldY + oldHeight);
                  }

                  try {
                     RenderSystem.enableScissorForRenderTypeDraws(clipX, clipY, Math.max(0, clipRight - clipX), Math.max(0, clipTop - clipY));
                     immediate.method_22993();
                  } finally {
                     if (enabled) {
                        RenderSystem.enableScissorForRenderTypeDraws(oldX, oldY, oldWidth, oldHeight);
                     } else {
                        RenderSystem.disableScissorForRenderTypeDraws();
                     }
                  }
               } finally {
                  view.popMatrix();
               }
            } finally {
               RenderSystem.restoreProjectionMatrix();
            }
         } finally {
            immediate = null;
         }
      }
   }

   private static GpuBufferSlice identity() {
      if (identityProjection == null || identityProjection.isClosed()) {
         ByteBuffer data = MemoryUtil.memAlloc(64);

         try {
            for (int i = 0; i < 16; i++) {
               data.putFloat(i % 5 == 0 ? 1.0F : 0.0F);
            }

            data.flip();
            identityProjection = RenderSystem.getDevice().createBuffer(() -> "client:mentions_cosmetics_projection", 64, data);
         } finally {
            MemoryUtil.memFree(data);
         }
      }

      return identityProjection.slice();
   }

   private static float[] capeBounds(CapeRenderer.Shape shape) {
      float half = shape.width * Math.max(1.0F, shape.bottomRatio) * 0.5F;
      float wave = Math.abs(shape.wave) * 2.0F;
      return new float[]{-half, -shape.topY - shape.length - shape.bend, shape.backZ - wave, half, -shape.topY, shape.backZ + shape.bend + wave};
   }
}
