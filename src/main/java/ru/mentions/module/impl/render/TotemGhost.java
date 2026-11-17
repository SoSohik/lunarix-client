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
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2663;
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
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventRender3D;
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
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Totem Ghost",
   desc = "Призрак модельки при лопании тотема",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class TotemGhost extends Module implements ModulePreview {
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -16711681).setVisible(() -> this.typeColor.is("Свой"));
   public SliderSetting rise = new SliderSetting(this, "Высота подъёма", 2.0F, 0.5F, 4.0F, 0.1F);
   public SliderSetting duration = new SliderSetting(this, "Длительность", 2.0F, 1.0F, 4.0F, 0.1F);
   private final PreviewSettings previewSettings = PreviewSettings.of(this, 4.0F, 0.0F, 3.0F);
   private static final float[][] PARTS = new float[][]{
      {-0.25F, 1.5F, -0.25F, 0.25F, 2.0F, 0.25F},
      {-0.25F, 0.75F, -0.125F, 0.25F, 1.5F, 0.125F},
      {0.25F, 0.75F, -0.125F, 0.5F, 1.5F, 0.125F},
      {-0.5F, 0.75F, -0.125F, -0.25F, 1.5F, 0.125F},
      {0.0F, 0.0F, -0.125F, 0.25F, 0.75F, 0.125F},
      {-0.25F, 0.0F, -0.125F, 0.0F, 0.75F, 0.125F}
   };
   private static final float[][] PIVOTS = new float[][]{
      {0.0F, 1.5F, 0.0F}, {0.0F, 0.75F, 0.0F}, {0.375F, 1.4F, 0.0F}, {-0.375F, 1.4F, 0.0F}, {0.125F, 0.75F, 0.0F}, {-0.125F, 0.75F, 0.0F}
   };
   private static final int PART_COUNT = PARTS.length;
   private static final int[][] FACES = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}};
   private static final int[][] EDGES = new int[][]{{0, 1}, {1, 3}, {3, 2}, {2, 0}, {4, 5}, {5, 7}, {7, 6}, {6, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
   private final List<TotemGhost.Ghost> ghosts = new ArrayList<>();
   private final class_9799 allocator = new class_9799(16384);
   private static final class_2960 GLOW_TEXTURE_BIG = class_2960.method_60655("client", "textures/visuals/particles_1.png");
   private static final class_2960 GLOW_TEXTURE_SMALL = class_2960.method_60655("client", "textures/visuals/particles_2.png");
   private static final RenderPipeline FILL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "totem_ghost_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 FILL_LAYER = class_1921.method_75940(
      "totem_ghost_fill", class_12247.method_75927(FILL_PIPELINE).method_75937().method_75929(4096).method_75938()
   );
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "totem_ghost_lines"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "totem_ghost_lines", class_12247.method_75927(LINE_PIPELINE).method_75929(4096).method_75938()
   );
   private static final RenderPipeline GLOW_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("client", "totem_ghost_glow"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 GLOW_LAYER_BIG = class_1921.method_75940(
      "totem_ghost_glow_big",
      class_12247.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEXTURE_BIG).method_75937().method_75929(1024).method_75938()
   );
   private static final class_1921 GLOW_LAYER_SMALL = class_1921.method_75940(
      "totem_ghost_glow_small",
      class_12247.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEXTURE_SMALL).method_75937().method_75929(1024).method_75938()
   );

   public int getColor() {
      return this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   @Override
   public void toggle() {
      super.toggle();
      this.ghosts.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.ghosts.clear();
   }

   @Override
   public PreviewSettings previewSettings() {
      return this.previewSettings;
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   @Override
   public void previewSpawn(PreviewContext ctx) {
      this.spawnGhost(ctx.dummy());
   }

   @Override
   public void previewStop() {
      this.ghosts.clear();
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      if (e.getPacket() instanceof class_2663 packet) {
         if (packet.method_11470() == 35) {
            if (mc.field_1687 != null) {
               class_1297 entity = packet.method_11469(mc.field_1687);
               if (entity != null) {
                  this.spawnGhost(entity);
               }
            }
         }
      }
   }

   public void spawnGhost(class_1297 entity) {
      if (this.isEnabled() && entity != null) {
         TotemGhost.Ghost g = new TotemGhost.Ghost(entity.method_73189(), entity.method_36454());
         if (entity instanceof class_1309 le) {
            g.bodyYaw = le.method_73188();
            g.headYawDeg = class_3532.method_15363(class_3532.method_15393(le.method_5791() - le.method_73188()), -75.0F, 75.0F);
            float limbAngle = le.field_42108.method_48569();
            float limbDist = Math.min(le.field_42108.method_48566(), 1.0F);
            g.partPitchDeg[0] = le.method_36455();
            g.partPitchDeg[2] = (float)Math.toDegrees(
               (double)(class_3532.method_15362((double)(limbAngle * 0.6662F + (float) Math.PI)) * 2.0F * limbDist * 0.5F)
            );
            g.partPitchDeg[3] = (float)Math.toDegrees((double)(class_3532.method_15362((double)(limbAngle * 0.6662F)) * 2.0F * limbDist * 0.5F));
            g.partPitchDeg[4] = (float)Math.toDegrees((double)(class_3532.method_15362((double)(limbAngle * 0.6662F)) * 1.4F * limbDist));
            g.partPitchDeg[5] = (float)Math.toDegrees((double)(class_3532.method_15362((double)(limbAngle * 0.6662F + (float) Math.PI)) * 1.4F * limbDist));
         } else {
            g.bodyYaw = entity.method_36454();
         }

         this.ghosts.add(g);
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (!this.ghosts.isEmpty() && mc.field_1724 != null && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         long durMs = (long)(this.duration.getValue() * 1000.0F);
         float riseH = this.rise.getValue();
         int baseColor = this.getColor();
         int cr = baseColor >> 16 & 0xFF;
         int cg = baseColor >> 8 & 0xFF;
         int cb = baseColor & 0xFF;
         class_4587 matrices = e.getMatrixStack();
         class_243 cam = mc.field_1773.method_19418().method_71156();
         Iterator<TotemGhost.Ghost> it = this.ghosts.iterator();

         while (it.hasNext()) {
            TotemGhost.Ghost g = it.next();
            float t = (float)(now - g.start) / (float)durMs;
            if (t >= 1.0F) {
               it.remove();
            } else {
               float ease = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
               float fadeIn = Math.min(t * 8.0F, 1.0F);
               float fadeOut = t > 0.55F ? 1.0F - (t - 0.55F) / 0.45F : 1.0F;
               g.alphaValue = fadeIn * fadeOut;
               double gx = g.x - cam.field_1352;
               double gy = g.y - cam.field_1351 + (double)(ease * riseH);
               double gz = g.z - cam.field_1350;
               matrices.method_22903();
               matrices.method_22904(gx, gy, gz);
               matrices.method_22907(class_7833.field_40716.rotationDegrees(-g.bodyYaw + t * 40.0F));

               for (int i = 0; i < PART_COUNT; i++) {
                  float pitch = g.partPitchDeg[i];
                  float yawOff = i == 0 ? g.headYawDeg : 0.0F;
                  if (pitch == 0.0F && yawOff == 0.0F) {
                     g.partMatrices[i].set(matrices.method_23760().method_23761());
                  } else {
                     float[] pv = PIVOTS[i];
                     matrices.method_22903();
                     matrices.method_46416(pv[0], pv[1], pv[2]);
                     if (yawOff != 0.0F) {
                        matrices.method_22907(class_7833.field_40716.rotationDegrees(-yawOff));
                     }

                     if (pitch != 0.0F) {
                        matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch));
                     }

                     matrices.method_46416(-pv[0], -pv[1], -pv[2]);
                     g.partMatrices[i].set(matrices.method_23760().method_23761());
                     matrices.method_22909();
                  }
               }

               matrices.method_22909();
               matrices.method_22903();
               matrices.method_22904(gx, gy + 1.1, gz);
               matrices.method_22907(mc.field_1773.method_19418().method_23767());
               g.glowMatrix.set(matrices.method_23760().method_23761());
               matrices.method_22909();
            }
         }

         if (!this.ghosts.isEmpty()) {
            class_4598 immediate = class_4597.method_22991(this.allocator);
            class_4588 buf = immediate.method_73477(FILL_LAYER);

            for (TotemGhost.Ghost g : this.ghosts) {
               int alpha = (int)(g.alphaValue * 0.25F * 255.0F);
               if (alpha > 0) {
                  for (int ix = 0; ix < PART_COUNT; ix++) {
                     drawBoxFill(buf, g.partMatrices[ix], PARTS[ix], cr, cg, cb, alpha);
                  }
               }
            }

            buf = immediate.method_73477(LINE_LAYER);

            for (TotemGhost.Ghost gx : this.ghosts) {
               int alpha = (int)(gx.alphaValue * 0.45F * 255.0F);
               if (alpha > 0) {
                  for (int ix = 0; ix < PART_COUNT; ix++) {
                     drawBoxLines(buf, gx.partMatrices[ix], PARTS[ix], cr, cg, cb, alpha);
                  }
               }
            }

            buf = immediate.method_73477(GLOW_LAYER_BIG);

            for (TotemGhost.Ghost gxx : this.ghosts) {
               drawGlow(buf, gxx.glowMatrix, cr, cg, cb, (int)(110.0F * gxx.alphaValue), 1.5F);
            }

            buf = immediate.method_73477(GLOW_LAYER_SMALL);

            for (TotemGhost.Ghost gxx : this.ghosts) {
               drawGlow(buf, gxx.glowMatrix, cr, cg, cb, (int)(160.0F * gxx.alphaValue), 0.7F);
            }

            immediate.method_22993();
         }
      }
   }

   private static float corner(float[] p, int bit, int axis) {
      return (bit >> axis & 1) == 0 ? p[axis] : p[axis + 3];
   }

   private static void drawBoxFill(class_4588 buf, Matrix4f m, float[] p, int r, int g, int b, int alpha) {
      int[][] nArray = FACES;
      int n = nArray.length;

      for (int i = 0; i < n; i++) {
         int[] face;
         for (int idx : face = nArray[i]) {
            buf.method_22918(m, corner(p, idx, 0), corner(p, idx, 1), corner(p, idx, 2)).method_1336(r, g, b, alpha);
         }
      }
   }

   private static void drawBoxLines(class_4588 buf, Matrix4f m, float[] p, int r, int g, int b, int alpha) {
      for (int[] edge : EDGES) {
         buf.method_22918(m, corner(p, edge[0], 0), corner(p, edge[0], 1), corner(p, edge[0], 2)).method_1336(r, g, b, alpha);
         buf.method_22918(m, corner(p, edge[1], 0), corner(p, edge[1], 1), corner(p, edge[1], 2)).method_1336(r, g, b, alpha);
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
   private static class Ghost {
      final double x;
      final double y;
      final double z;
      final long start;
      float bodyYaw;
      float headYawDeg;
      final float[] partPitchDeg = new float[TotemGhost.PART_COUNT];
      float alphaValue;
      final Matrix4f[] partMatrices = new Matrix4f[TotemGhost.PART_COUNT];
      final Matrix4f glowMatrix = new Matrix4f();

      Ghost(class_243 pos, float yaw) {
         this.x = pos.field_1352;
         this.y = pos.field_1351;
         this.z = pos.field_1350;
         this.bodyYaw = yaw;
         this.start = System.currentTimeMillis();

         for (int i = 0; i < TotemGhost.PART_COUNT; i++) {
            this.partMatrices[i] = new Matrix4f();
         }
      }
   }
}
