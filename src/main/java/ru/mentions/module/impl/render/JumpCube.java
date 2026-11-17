package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.EventJump;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.preview.ModulePreview;
import ru.mentions.module.api.preview.PreviewContext;
import ru.mentions.module.api.preview.PreviewSettings;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Jump Cube",
   desc = "Кубики вокруг игрока - прыгают и крутятся",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class JumpCube extends Module implements ModulePreview {
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -16711681).setVisible(() -> this.typeColor.is("Свой"));
   public SliderSetting count = new SliderSetting(this, "Кубиков", 3.0F, 1.0F, 10.0F, 1.0F);
   public SliderSetting size = new SliderSetting(this, "Размер", 0.18F, 0.05F, 0.4F, 0.01F);
   private final PreviewSettings previewSettings = PreviewSettings.none(this);
   private static final int RES_PX = 16;
   private static final long LIFETIME_MS = 2000L;
   private static final double SPAWN_MIN_DST = 0.6;
   private static final double SPAWN_MAX_DST = 2.6;
   private static final double MAX_OWNER_DIST = 6.0;
   private static final long RANGE_FADE_MS = 400L;
   private static final int[] JUMP_YAWS = new int[]{0, 90, 180, 270};
   private final Random random = new Random();
   private final List<JumpCube.Cube> cubes = new ArrayList<>();
   private final List<JumpCube.Cube> visible = new ArrayList<>();
   private final class_9799 allocator = new class_9799(65536);
   private static final class_2960 GLOW_TEX_BIG = class_2960.method_60655("client", "textures/visuals/particles_1.png");
   private static final class_2960 GLOW_TEX_SMALL = class_2960.method_60655("client", "textures/visuals/particles_2.png");
   private static final RenderPipeline FILL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "jump_cube_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 FILL_LAYER = class_1921.method_75940(
      "jump_cube_fill", class_12247.method_75927(FILL_PIPELINE).method_75929(8192).method_75938()
   );
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "jump_cube_lines"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "jump_cube_lines", class_12247.method_75927(LINE_PIPELINE).method_75929(4096).method_75938()
   );
   private static final RenderPipeline GLOW_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("client", "jump_cube_glow"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 GLOW_LAYER_BIG = class_1921.method_75940(
      "jump_cube_glow_big", class_12247.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEX_BIG).method_75937().method_75929(4096).method_75938()
   );
   private static final class_1921 GLOW_LAYER_SMALL = class_1921.method_75940(
      "jump_cube_glow_small", class_12247.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEX_SMALL).method_75937().method_75929(4096).method_75938()
   );

   @Override
   protected void onDisable() {
      this.cubes.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.cubes.clear();
   }

   @EventHandler
   public void onJump(EventJump e) {
      for (JumpCube.Cube c : this.cubes) {
         c.tryStartJump();
      }
   }

   @Override
   public PreviewSettings previewSettings() {
      return this.previewSettings;
   }

   @Override
   public void previewSpawn(PreviewContext ctx) {
   }

   @Override
   public void previewTick(PreviewContext ctx) {
      for (JumpCube.Cube c : this.cubes) {
         c.tryStartJump();
      }
   }

   @Override
   public void previewStop() {
      this.cubes.clear();
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 playerPos = mc.field_1724.method_73189();
         List<class_2338> candidates = this.getPlaceableAround(playerPos, 0.6, 2.6, 3);
         int toSpawn = this.count.getValue().intValue();

         for (int i = 0; i < toSpawn && !candidates.isEmpty(); i++) {
            int idx = this.random.nextInt(candidates.size());
            class_243 sp = this.findSpawnPoint(candidates.get(idx), playerPos);
            if (sp != null) {
               this.cubes.add(new JumpCube.Cube(sp));
            }
         }

         this.cubes.forEach(JumpCube.Cube::updateLogic);
         this.cubes.removeIf(JumpCube.Cube::isDead);
      }
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.cubes.isEmpty()) {
         float pt = e.getTickDelta();
         class_243 cam = mc.field_1773.method_19418().method_71156();
         class_4587 pose = e.getMatrixStack();
         float s = this.size.getValue();
         int base = this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
         int cr = base >> 16 & 0xFF;
         int cg = base >> 8 & 0xFF;
         int cb = base & 0xFF;
         this.visible.clear();

         for (JumpCube.Cube c : this.cubes) {
            float alpha;
            if (!c.isDead() && !((alpha = c.getAlpha()) < 0.01F)) {
               double wx = c.spawnPos.field_1352 - cam.field_1352;
               double wy = c.spawnPos.field_1351 + c.getJumpYOffset(pt) - cam.field_1351;
               double wz = c.spawnPos.field_1350 - cam.field_1350;
               pose.method_22903();
               pose.method_22904(wx, wy, wz);
               if (c.jumpTicksMax > 0) {
                  float tLerped = class_3532.method_16439(pt, (float)c.prevJumpTicks, (float)c.jumpTicks);
                  float phase = 1.0F - tLerped / (float)c.jumpTicksMax;
                  pose.method_22907(class_7833.field_40716.rotationDegrees((float)c.jumpYaw));
                  pose.method_22907(class_7833.field_40714.rotationDegrees(90.0F * phase));
               }

               pose.method_22905(alpha, alpha, alpha);
               c.cubeMatrix.set(pose.method_23760().method_23761());
               pose.method_22909();
               pose.method_22903();
               pose.method_22904(wx, wy, wz);
               pose.method_22907(mc.field_1773.method_19418().method_23767());
               pose.method_22905(alpha, alpha, alpha);
               c.glowMatrix.set(pose.method_23760().method_23761());
               pose.method_22909();
               c.cachedAlpha = alpha;
               this.visible.add(c);
            }
         }

         if (!this.visible.isEmpty()) {
            class_4598 immediate = class_4597.method_22991(this.allocator);
            class_4588 buf = immediate.method_73477(FILL_LAYER);

            for (JumpCube.Cube cx : this.visible) {
               int fa = (int)(cx.cachedAlpha * 0.2F * 255.0F);
               if (fa > 0) {
                  drawCubeFill(buf, cx.cubeMatrix, s, cr, cg, cb, fa);
               }
            }

            buf = immediate.method_73477(LINE_LAYER);

            for (JumpCube.Cube cxx : this.visible) {
               int ea = (int)(cxx.cachedAlpha * 255.0F);
               if (ea > 0) {
                  drawCubeEdges(buf, cxx.cubeMatrix, s, cr, cg, cb, ea);
               }
            }

            buf = immediate.method_73477(GLOW_LAYER_BIG);

            for (JumpCube.Cube cxxx : this.visible) {
               drawGlow(buf, cxxx.glowMatrix, cr, cg, cb, (int)(35.0F * cxxx.cachedAlpha), s * 5.5F);
            }

            buf = immediate.method_73477(GLOW_LAYER_SMALL);

            for (JumpCube.Cube cxxx : this.visible) {
               drawGlow(buf, cxxx.glowMatrix, cr, cg, cb, (int)(40.0F * cxxx.cachedAlpha), s * 1.8F);
            }

            immediate.method_22993();
         }
      }
   }

   private List<class_2338> getPlaceableAround(class_243 center, double minDst, double maxDst, int offsetDown) {
      ArrayList<class_2338> result = new ArrayList<>();
      if (mc.field_1687 == null) {
         return result;
      } else {
         double xE = center.field_1352;
         double yE = center.field_1351 + 1.0;
         double zE = center.field_1350;

         for (double x = xE - maxDst; x < xE + maxDst; x++) {
            for (double z = zE - maxDst; z < zE + maxDst; z++) {
               for (double y = yE - (double)offsetDown; y < yE; y++) {
                  class_2338 pos = class_2338.method_49637(x, y, z);
                  if (this.canPlace(pos) && !result.contains(pos)) {
                     result.add(pos);
                  }
               }
            }
         }

         return result;
      }
   }

   private boolean canPlace(class_2338 pos) {
      if (mc.field_1687 == null) {
         return false;
      } else if (!mc.field_1687.method_22347(pos)) {
         return false;
      } else {
         return mc.field_1687.method_22347(pos.method_10074()) ? false : mc.field_1687.method_8335(null, new class_238(pos)).isEmpty();
      }
   }

   private class_243 findSpawnPoint(class_2338 pos, class_243 playerPos) {
      double resOff = 0.0625;
      double half = resOff / 2.0;

      for (int att = 64; att > 0; att--) {
         class_243 v = new class_243(
            (double)pos.method_10263() + this.random.nextDouble(),
            (double)pos.method_10264() + this.random.nextDouble(),
            (double)pos.method_10260() + this.random.nextDouble()
         );
         double dst = v.method_1022(playerPos);
         if (dst >= 0.6 && dst <= 2.6) {
            double sx = Math.floor(v.field_1352 * 16.0) / 16.0 + half;
            double sy = Math.floor(v.field_1351) + half - resOff;
            double sz = Math.floor(v.field_1350 * 16.0) / 16.0 + half;
            return new class_243(sx, sy, sz);
         }
      }

      return null;
   }

   private static void drawCubeFill(class_4588 b, Matrix4f m, float s, int r, int g, int bl, int a) {
      float h = s / 2.0F;
      b.method_22918(m, -h, -h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, -h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, -h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, -h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, -h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, -h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, -h, h).method_1336(r, g, bl, a);
      b.method_22918(m, h, -h, h).method_1336(r, g, bl, a);
      b.method_22918(m, h, h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, -h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, -h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, h, h).method_1336(r, g, bl, a);
      b.method_22918(m, -h, h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, -h, -h).method_1336(r, g, bl, a);
      b.method_22918(m, h, -h, h).method_1336(r, g, bl, a);
      b.method_22918(m, h, h, h).method_1336(r, g, bl, a);
      b.method_22918(m, h, h, -h).method_1336(r, g, bl, a);
   }

   private static void drawCubeEdges(class_4588 b, Matrix4f m, float s, int r, int g, int bl, int a) {
      float h = s / 2.0F;
      line(b, m, -h, -h, -h, h, -h, -h, r, g, bl, a);
      line(b, m, h, -h, -h, h, -h, h, r, g, bl, a);
      line(b, m, h, -h, h, -h, -h, h, r, g, bl, a);
      line(b, m, -h, -h, h, -h, -h, -h, r, g, bl, a);
      line(b, m, -h, h, -h, h, h, -h, r, g, bl, a);
      line(b, m, h, h, -h, h, h, h, r, g, bl, a);
      line(b, m, h, h, h, -h, h, h, r, g, bl, a);
      line(b, m, -h, h, h, -h, h, -h, r, g, bl, a);
      line(b, m, -h, -h, -h, -h, h, -h, r, g, bl, a);
      line(b, m, h, -h, -h, h, h, -h, r, g, bl, a);
      line(b, m, h, -h, h, h, h, h, r, g, bl, a);
      line(b, m, -h, -h, h, -h, h, h, r, g, bl, a);
   }

   private static void line(class_4588 b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int bl, int a) {
      b.method_22918(m, x1, y1, z1).method_1336(r, g, bl, a);
      b.method_22918(m, x2, y2, z2).method_1336(r, g, bl, a);
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
   private class Cube {
      final class_243 spawnPos;
      final long spawnTime = System.currentTimeMillis();
      float cachedAlpha;
      int jumpTicks = 0;
      int prevJumpTicks = 0;
      int jumpTicksMax = 0;
      int jumpYaw = 0;
      double jumpHeight = 0.0;
      boolean outOfRange = false;
      long outOfRangeStart = 0L;
      final Matrix4f cubeMatrix = new Matrix4f();
      final Matrix4f glowMatrix = new Matrix4f();

      Cube(class_243 spawnPos) {
         this.spawnPos = spawnPos;
      }

      void updateLogic() {
         this.prevJumpTicks = this.jumpTicks;
         if (this.jumpTicks > 0) {
            this.jumpTicks--;
         }

         if (IMinecraft.mc.field_1724 != null && IMinecraft.mc.field_1724.method_73189().method_1022(this.spawnPos) > 6.0 && !this.outOfRange) {
            this.outOfRange = true;
            this.outOfRangeStart = System.currentTimeMillis();
         }
      }

      void tryStartJump() {
         if (this.jumpTicks <= 0) {
            this.jumpTicksMax = this.jumpTicks = (int)(14.0F * (0.5F + 0.5F * JumpCube.this.random.nextFloat()));
            this.jumpHeight = (double)(2 + JumpCube.this.random.nextInt(11)) / 16.0;
            this.jumpYaw = JumpCube.JUMP_YAWS[JumpCube.this.random.nextInt(JumpCube.JUMP_YAWS.length)];
         }
      }

      float getTimePc() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.spawnTime) / 2000.0F, 0.0F, 1.0F);
      }

      float getRangeFade() {
         return !this.outOfRange ? 1.0F : class_3532.method_15363(1.0F - (float)(System.currentTimeMillis() - this.outOfRangeStart) / 400.0F, 0.0F, 1.0F);
      }

      float getAlpha() {
         float t = this.getTimePc();
         float a = t < 0.1F ? t / 0.1F : (t > 0.8F ? 1.0F - (t - 0.8F) / 0.2F : 1.0F);
         return class_3532.method_15363(a, 0.0F, 1.0F) * this.getRangeFade();
      }

      double getJumpYOffset(float pt) {
         if (this.jumpTicksMax <= 0) {
            return 0.0;
         } else {
            float t = class_3532.method_16439(pt, (float)this.prevJumpTicks, (float)this.jumpTicks) / (float)this.jumpTicksMax;
            if (t > 0.5F) {
               t = 1.0F - t;
            }

            return (double)t * 2.0 * this.jumpHeight;
         }
      }

      boolean isDead() {
         return this.getTimePc() >= 1.0F || this.outOfRange && this.getRangeFade() <= 0.01F;
      }
   }
}
