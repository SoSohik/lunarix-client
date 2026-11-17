package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_9799;
import net.minecraft.class_2902.class_2903;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.MotionEvent;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;

@ModuleInfo(
   name = "Svetoch",
   desc = "Летающие светящиеся кубы с физикой",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Svetoch extends Module {
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -16711681).setVisible(() -> this.typeColor.is("Свой"));
   public SliderSetting count = new SliderSetting(this, "Кол кубиков", 100.0F, 50.0F, 300.0F, 1.0F);
   public SliderSetting cubeSize = new SliderSetting(this, "Размер куба", 0.26F, 0.1F, 0.6F, 0.02F);
   private static final long LIFETIME = 1500L;
   private static final double CULL_DIST_SQ = 9216.0;
   private static final float[][] CORNERS = new float[][]{
      {-1.0F, -1.0F, -1.0F},
      {1.0F, -1.0F, -1.0F},
      {1.0F, -1.0F, 1.0F},
      {-1.0F, -1.0F, 1.0F},
      {-1.0F, 1.0F, -1.0F},
      {1.0F, 1.0F, -1.0F},
      {1.0F, 1.0F, 1.0F},
      {-1.0F, 1.0F, 1.0F}
   };
   private static final int[][] FACES = new int[][]{{0, 1, 2, 3}, {4, 5, 6, 7}, {0, 1, 5, 4}, {2, 3, 7, 6}, {1, 2, 6, 5}, {3, 0, 4, 7}};
   private static final int[][] EDGES = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
   private final List<Svetoch.Particle> particles = new ArrayList<>();
   private final List<Svetoch.Particle> visible = new ArrayList<>();
   private final class_9799 allocator = new class_9799(65536);
   private long lastFrameTime = System.nanoTime();
   private static final class_2960 GLOW_TEXTURE_BIG = class_2960.method_60655("client", "textures/particles/glow.png");
   private static final class_2960 GLOW_TEXTURE_SMALL = class_2960.method_60655("client", "textures/visuals/particles_2.png");
   private static final RenderPipeline FILL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "svetoch_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 FILL_LAYER = class_1921.method_75940(
      "svetoch_fill", class_12247.method_75927(FILL_PIPELINE).method_75929(8192).method_75938()
   );
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "svetoch_lines"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "svetoch_lines", class_12247.method_75927(LINE_PIPELINE).method_75929(8192).method_75938()
   );
   private static final RenderPipeline GLOW_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("client", "svetoch_glow"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 GLOW_LAYER_BIG = class_1921.method_75940(
      "svetoch_glow_big", class_12247.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEXTURE_BIG).method_75937().method_75929(4096).method_75938()
   );
   private static final class_1921 GLOW_LAYER_SMALL = class_1921.method_75940(
      "svetoch_glow_small",
      class_12247.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEXTURE_SMALL).method_75937().method_75929(4096).method_75938()
   );

   public int getColor() {
      return this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   @Override
   public void toggle() {
      super.toggle();
      this.particles.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.particles.clear();
   }

   @EventHandler
   public void onUpdate(MotionEvent e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int max = this.count.getValue().intValue();
         int r = 24;

         for (int i = 0; i < 3 && this.particles.size() < max; i++) {
            class_243 additional = mc.field_1724
               .method_73189()
               .method_1031((double)MathUtil.randomValue((float)(-r), (float)r), 0.0, (double)MathUtil.randomValue((float)(-r), (float)r));
            class_2338 topPos = mc.field_1687.method_8598(class_2903.field_13197, class_2338.method_49638(additional));
            double x = (double)((float)topPos.method_10263() + MathUtil.randomValue(-3.0F, 3.0F));
            double z = (double)((float)topPos.method_10260() + MathUtil.randomValue(-3.0F, 3.0F));
            double y = mc.field_1724.method_23318() + (double)MathUtil.randomValue(mc.field_1724.method_17682(), (float)r);
            class_243 spawnPos = new class_243(x, y, z);

            while (!mc.field_1687.method_22347(class_2338.method_49638(spawnPos)) && spawnPos.field_1351 < (double)mc.field_1687.method_31600()) {
               spawnPos = spawnPos.method_1031(0.0, 1.0, 0.0);
            }

            class_243 velocity = new class_243(
               mc.field_1724.method_18798().field_1352 + (double)MathUtil.randomValue(-0.6F, 0.6F),
               MathUtil.randomValue(-0.01, 0.44),
               mc.field_1724.method_18798().field_1350 + (double)MathUtil.randomValue(-0.6F, 0.6F)
            );
            this.particles.add(new Svetoch.Particle(spawnPos, velocity));
         }
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (!this.particles.isEmpty() && mc.field_1724 != null && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         long nanoNow = System.nanoTime();
         float ticks = (float)Math.min((double)(nanoNow - this.lastFrameTime) / 1.0E9, 0.1) * 60.0F;
         this.lastFrameTime = nanoNow;
         class_4587 matrices = e.getMatrixStack();
         class_243 cam = mc.field_1773.method_19418().method_71156();
         Matrix4f base = matrices.method_23760().method_23761();
         Quaternionf camRotation = mc.field_1773.method_19418().method_23767();
         float rotation = (float)(now % 9000L) / 9000.0F * 360.0F;
         float rotXRad = (float)Math.toRadians((double)(rotation * 0.5F));
         int baseColor = this.getColor();
         int cr = baseColor >> 16 & 0xFF;
         int cg = baseColor >> 8 & 0xFF;
         int cb = baseColor & 0xFF;
         float size = this.cubeSize.getValue();
         class_4598 immediate = class_4597.method_22991(this.allocator);
         this.visible.clear();
         Iterator<Svetoch.Particle> it = this.particles.iterator();

         while (it.hasNext()) {
            Svetoch.Particle p = it.next();
            p.update(now, ticks);
            if (p.shouldRemove()) {
               it.remove();
            } else {
               double dz;
               double dy;
               double dx;
               if (!(p.alphaValue <= 0.01F)
                  && !((dx = p.x - cam.field_1352) * dx + (dy = p.y - cam.field_1351) * dy + (dz = p.z - cam.field_1350) * dz > 9216.0)) {
                  p.cubeMatrix
                     .set(base)
                     .translate((float)dx, (float)dy, (float)dz)
                     .rotateY((float)Math.toRadians((double)(rotation + p.phase)))
                     .rotateX(rotXRad);
                  p.glowMatrix.set(base).translate((float)dx, (float)dy, (float)dz).rotate(camRotation);
                  this.visible.add(p);
               }
            }
         }

         if (!this.visible.isEmpty()) {
            class_4588 buf = immediate.method_73477(FILL_LAYER);

            for (Svetoch.Particle p : this.visible) {
               int faceAlpha = (int)(p.alphaValue * 0.2F * 255.0F);
               if (faceAlpha > 0) {
                  int[][] nArray = FACES;
                  int n = nArray.length;

                  for (int i = 0; i < n; i++) {
                     int[] face;
                     for (int idx : face = nArray[i]) {
                        float[] c = CORNERS[idx];
                        buf.method_22918(p.cubeMatrix, c[0] * size, c[1] * size, c[2] * size).method_1336(cr, cg, cb, faceAlpha);
                     }
                  }
               }
            }

            buf = immediate.method_73477(LINE_LAYER);

            for (Svetoch.Particle px : this.visible) {
               int edgeAlpha = (int)(px.alphaValue * 255.0F);
               if (edgeAlpha > 0) {
                  for (int[] edge : EDGES) {
                     float[] a = CORNERS[edge[0]];
                     float[] b = CORNERS[edge[1]];
                     buf.method_22918(px.cubeMatrix, a[0] * size, a[1] * size, a[2] * size).method_1336(cr, cg, cb, edgeAlpha);
                     buf.method_22918(px.cubeMatrix, b[0] * size, b[1] * size, b[2] * size).method_1336(cr, cg, cb, edgeAlpha);
                  }
               }
            }

            buf = immediate.method_73477(GLOW_LAYER_BIG);

            for (Svetoch.Particle pxx : this.visible) {
               drawGlow(buf, pxx.glowMatrix, cr, cg, cb, (int)(80.0F * pxx.alphaValue), size * 6.0F);
            }

            immediate.method_22993();
         }
      }
   }

   private static void drawGlow(class_4588 buf, Matrix4f m, int r, int g, int b, int alpha, float s) {
      if (alpha > 0) {
         buf.method_22918(m, -s, -s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(0.0F, 1.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(m, s, -s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(1.0F, 1.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(m, s, s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(1.0F, 0.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(m, -s, s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(0.0F, 0.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Particle {
      private static final double BASE_VELOCITY = 0.05;
      private static final double BOUNCINESS = 0.85;
      private static final double GRAVITY = 1.0E-4;
      double x;
      double y;
      double z;
      double mX;
      double mY;
      double mZ;
      final long start;
      final float phase;
      boolean fadingOut;
      float alphaValue;
      final Animation alpha = new Animation();
      final Matrix4f cubeMatrix = new Matrix4f();
      final Matrix4f glowMatrix = new Matrix4f();

      Particle(class_243 pos, class_243 velocity) {
         this.start = System.currentTimeMillis();
         this.phase = (float)(Math.random() * 100.0);
         this.x = pos.field_1352;
         this.y = pos.field_1351;
         this.z = pos.field_1350;
         this.mX = velocity.field_1352 * 0.05;
         this.mY = velocity.field_1351 * 0.05;
         this.mZ = velocity.field_1350 * 0.05;
         this.alpha.set(0.0);
         this.alpha.run(1.0, 0.5, Easings.LINEAR, true);
      }

      void update(long now, float ticks) {
         double nextX = this.x + this.mX * (double)ticks;
         double nextY = this.y + this.mY * (double)ticks;
         double nextZ = this.z + this.mZ * (double)ticks;
         if (Math.floor(nextX) == Math.floor(this.x) && Math.floor(nextY) == Math.floor(this.y) && Math.floor(nextZ) == Math.floor(this.z)) {
            if (isHit(nextX, nextY, nextZ)) {
               this.mX = -this.mX * 0.85;
               this.mY = -this.mY * 0.85;
               this.mZ = -this.mZ * 0.85;
            } else {
               this.x = nextX;
               this.y = nextY;
               this.z = nextZ;
            }
         } else {
            if (isHit(nextX, this.y, this.z)) {
               this.mX = -this.mX * 0.85;
            } else {
               this.x = nextX;
            }

            if (isHit(this.x, nextY, this.z)) {
               this.mY = -this.mY * 0.85;
            } else {
               this.y = nextY;
            }

            if (isHit(this.x, this.y, nextZ)) {
               this.mZ = -this.mZ * 0.85;
            } else {
               this.z = nextZ;
            }
         }

         this.mY -= 1.0E-4 * (double)ticks;
         if (!this.fadingOut && now - this.start > 1500L) {
            this.fadingOut = true;
            this.alpha.run(0.0, 2.0, Easings.LINEAR, true);
         }

         this.alpha.update();
         this.alphaValue = this.alpha.get();
      }

      boolean shouldRemove() {
         return this.fadingOut && this.alphaValue <= 0.01F;
      }

      private static boolean isHit(double px, double py, double pz) {
         class_2338 pos = class_2338.method_49637(px, py, pz);
         return IMinecraft.mc.field_1687.method_8320(pos).method_26234(IMinecraft.mc.field_1687, pos);
      }
   }
}
