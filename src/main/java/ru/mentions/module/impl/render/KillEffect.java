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
import net.minecraft.class_745;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventPacket;
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
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Kill Effect",
   desc = "Soft beam on killed target",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class KillEffect extends Module implements ModulePreview {
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public ModeSetting typeColor = new ModeSetting(this, "Color mode", "Theme", "Custom");
   public ColorSetting tintColor = new ColorSetting(this, "Color", -16711681).setVisible(() -> this.typeColor.is("Custom"));
   public SliderSetting duration = new SliderSetting(this, "Duration", 2.2F, 1.0F, 5.0F, 0.1F);
   public SliderSetting beamHeight = new SliderSetting(this, "Beam height", 5.2F, 1.5F, 12.0F, 0.5F);
   public SliderSetting beamWidth = new SliderSetting(this, "Beam width", 0.42F, 0.08F, 1.2F, 0.02F);
   private final PreviewSettings previewSettings = PreviewSettings.of(this, 4.0F, 0.0F, 2.5F);
   private static final byte DEATH_STATUS = 3;
   private static final long KILL_WINDOW_MS = 6500L;
   private static final long DUPLICATE_WINDOW_MS = 750L;
   private final List<KillEffect.KillBeam> beams = new ArrayList<>();
   private final class_9799 allocator = new class_9799(32768);
   private class_1309 lastTarget;
   private KillEffect.KillPoint lastPoint;
   private int lastTargetId = -1;
   private long lastAttackTime;
   private int lastSpawnedId = -1;
   private long lastSpawnTime;
   private static final RenderPipeline BEAM_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "kill_effect_beam"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 BEAM_LAYER = class_1921.method_75940(
      "kill_effect_beam", class_12247.method_75927(BEAM_PIPELINE).method_75929(32768).method_75938()
   );

   public int getColor() {
      return this.typeColor.is("Theme") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   @Override
   protected void onDisable() {
      this.clearState();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.clearState();
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
      class_745 target = ctx.dummy();
      KillEffect.KillPoint point = target != null ? this.point(target) : new KillEffect.KillPoint(ctx.anchor(), 0.65F);
      this.beams.add(new KillEffect.KillBeam(point));
   }

   @Override
   public void previewStop() {
      this.clearState();
   }

   @EventHandler
   public void onAttack(AttackEvent e) {
      class_1297 class_12972 = e.getTarget();
      class_1309 living;
      if (class_12972 instanceof class_1309 && (living = (class_1309)class_12972) != mc.field_1724) {
         this.lastTarget = living;
         this.lastTargetId = living.method_5628();
         this.lastAttackTime = System.currentTimeMillis();
         this.lastPoint = this.point(living);
      }
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      if (e.getPacket() instanceof class_2663 packet) {
         if (packet.method_11470() == 3 && mc.field_1687 != null) {
            if (packet.method_11469(mc.field_1687) instanceof class_1309 living) {
               this.trySpawn(living, false);
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (this.lastTarget != null) {
         if (System.currentTimeMillis() - this.lastAttackTime > 6500L) {
            this.lastTarget = null;
            this.lastPoint = null;
         } else {
            if (!this.lastTarget.method_5805() || this.lastTarget.method_31481() || this.lastTarget.method_6032() <= 0.0F) {
               this.trySpawn(this.lastTarget, true);
            }
         }
      }
   }

   private void trySpawn(class_1309 entity, boolean allowCachedPoint) {
      long now = System.currentTimeMillis();
      if (entity.method_5628() == this.lastTargetId && now - this.lastAttackTime <= 6500L) {
         if (this.lastSpawnedId != entity.method_5628() || now - this.lastSpawnTime >= 750L) {
            KillEffect.KillPoint p = allowCachedPoint && this.lastPoint != null ? this.lastPoint : this.point(entity);
            this.beams.add(new KillEffect.KillBeam(p));
            this.lastSpawnedId = entity.method_5628();
            this.lastSpawnTime = now;
            this.lastTarget = null;
            this.lastPoint = null;
         }
      }
   }

   private KillEffect.KillPoint point(class_1309 entity) {
      return new KillEffect.KillPoint(entity.method_73189(), Math.max(0.65F, entity.method_17681()));
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (!this.beams.isEmpty() && mc.field_1724 != null && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         long durMs = (long)(this.duration.getValue() * 1000.0F);
         int color = this.getColor();
         int cr = color >> 16 & 0xFF;
         int cg = color >> 8 & 0xFF;
         int cb = color & 0xFF;
         class_4587 matrices = e.getMatrixStack();
         Matrix4f mat = matrices.method_23760().method_23761();
         class_243 cam = mc.field_1773.method_19418().method_71156();
         Iterator<KillEffect.KillBeam> it = this.beams.iterator();

         while (it.hasNext()) {
            KillEffect.KillBeam beam = it.next();
            float progress = (float)(now - beam.start) / (float)durMs;
            if (progress >= 1.0F) {
               it.remove();
            } else {
               float fadeIn = class_3532.method_15363(progress * 5.5F, 0.0F, 1.0F);
               float fadeOut = progress > 0.5F ? 1.0F - (progress - 0.5F) / 0.5F : 1.0F;
               beam.alpha = smooth(fadeIn) * smooth(fadeOut);
               beam.progress = progress;
            }
         }

         if (!this.beams.isEmpty()) {
            class_4598 immediate = class_4597.method_22991(this.allocator);
            class_4588 buf = immediate.method_73477(BEAM_LAYER);
            float pulse = 0.9F + 0.1F * class_3532.method_15374((double)((float)(now % 1600L) / 1600.0F * (float) (Math.PI * 2)));

            for (KillEffect.KillBeam beam : this.beams) {
               this.drawBeam(buf, mat, cam, beam, cr, cg, cb, pulse);
            }

            immediate.method_22993();
         }
      }
   }

   private void drawBeam(class_4588 buf, Matrix4f mat, class_243 cam, KillEffect.KillBeam beam, int r, int g, int b, float pulse) {
      if (!(beam.alpha <= 0.01F)) {
         float grow = smooth(Math.min(beam.progress * 3.5F, 1.0F));
         float h = this.beamHeight.getValue() * grow;
         float base = this.beamWidth.getValue() * beam.radius;
         float x = (float)(beam.x - cam.field_1352);
         float y = (float)(beam.y - cam.field_1351 + 0.035F);
         float z = (float)(beam.z - cam.field_1350);
         float spin = (float)(System.currentTimeMillis() - beam.start) / 1000.0F * 0.85F;
         float coreAlpha = 150.0F * beam.alpha * pulse;
         float softAlpha = 54.0F * beam.alpha;
         drawVerticalPetals(buf, mat, x, y, z, h, base * 0.72F, spin, r, g, b, (int)coreAlpha, 6);
         drawVerticalPetals(buf, mat, x, y, z, h * 0.82F, base * 1.28F, -spin * 0.55F, r, g, b, (int)softAlpha, 8);
         float bloom = 0.55F + 0.45F * grow;
         drawSoftDisc(buf, mat, x, y + 0.01F, z, base * 2.25F * bloom, r, g, b, (int)(42.0F * beam.alpha));
         drawSoftDisc(buf, mat, x, y + h * 0.98F, z, base * 0.92F * beam.alpha, r, g, b, (int)(28.0F * beam.alpha));

         for (int i = 0; i < 3; i++) {
            float wave = (beam.progress + (float)i * 0.22F) % 1.0F;
            float radius = base * (1.05F + wave * 2.25F);
            int alpha = (int)(52.0F * beam.alpha * (1.0F - wave));
            drawRing(buf, mat, x, y + (float)i * 0.025F, z, radius, 0.035F + wave * 0.045F, r, g, b, alpha);
         }
      }
   }

   private static void drawVerticalPetals(
      class_4588 buf, Matrix4f mat, float x, float y, float z, float h, float halfWidth, float spin, int r, int g, int b, int alpha, int petals
   ) {
      if (alpha > 0 && !(h <= 0.01F)) {
         for (int i = 0; i < petals; i++) {
            float ang = spin + (float)i / (float)petals * (float) (Math.PI * 2);
            float dx = class_3532.method_15362((double)ang) * halfWidth;
            float dz = class_3532.method_15374((double)ang) * halfWidth;
            int edgeAlpha = Math.max(0, alpha / 3);
            buf.method_22918(mat, x - dx, y, z - dz).method_1336(r, g, b, edgeAlpha);
            buf.method_22918(mat, x, y + h * 0.08F, z).method_1336(r, g, b, alpha);
            buf.method_22918(mat, x, y + h, z).method_1336(r, g, b, 0);
            buf.method_22918(mat, x + dx, y, z + dz).method_1336(r, g, b, edgeAlpha);
         }
      }
   }

   private static void drawSoftDisc(class_4588 buf, Matrix4f mat, float x, float y, float z, float radius, int r, int g, int b, int alpha) {
      if (alpha > 0 && !(radius <= 0.01F)) {
         int segments = 56;

         for (int i = 0; i < segments; i++) {
            float a0 = (float)i / (float)segments * (float) (Math.PI * 2);
            float a1 = (float)(i + 1) / (float)segments * (float) (Math.PI * 2);
            buf.method_22918(mat, x, y, z).method_1336(r, g, b, alpha);
            buf.method_22918(mat, x + class_3532.method_15362((double)a0) * radius, y, z + class_3532.method_15374((double)a0) * radius)
               .method_1336(r, g, b, 0);
            buf.method_22918(mat, x + class_3532.method_15362((double)a1) * radius, y, z + class_3532.method_15374((double)a1) * radius)
               .method_1336(r, g, b, 0);
            buf.method_22918(mat, x, y, z).method_1336(r, g, b, alpha);
         }
      }
   }

   private static void drawRing(class_4588 buf, Matrix4f mat, float x, float y, float z, float radius, float width, int r, int g, int b, int alpha) {
      if (alpha > 0 && !(radius <= width)) {
         int segments = 64;

         for (int i = 0; i < segments; i++) {
            float a0 = (float)i / (float)segments * (float) (Math.PI * 2);
            float a1 = (float)(i + 1) / (float)segments * (float) (Math.PI * 2);
            float x0 = class_3532.method_15362((double)a0);
            float z0 = class_3532.method_15374((double)a0);
            float x1 = class_3532.method_15362((double)a1);
            float z1 = class_3532.method_15374((double)a1);
            buf.method_22918(mat, x + x0 * (radius - width), y, z + z0 * (radius - width)).method_1336(r, g, b, 0);
            buf.method_22918(mat, x + x0 * radius, y, z + z0 * radius).method_1336(r, g, b, alpha);
            buf.method_22918(mat, x + x1 * radius, y, z + z1 * radius).method_1336(r, g, b, alpha);
            buf.method_22918(mat, x + x1 * (radius - width), y, z + z1 * (radius - width)).method_1336(r, g, b, 0);
         }
      }
   }

   private static float smooth(float t) {
      t = class_3532.method_15363(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private void clearState() {
      this.beams.clear();
      this.lastTarget = null;
      this.lastPoint = null;
      this.lastTargetId = -1;
      this.lastSpawnedId = -1;
   }

   @Environment(EnvType.CLIENT)
   private static class KillBeam {
      final double x;
      final double y;
      final double z;
      final float radius;
      final long start;
      float alpha;
      float progress;

      KillBeam(KillEffect.KillPoint point) {
         this.x = point.pos.field_1352;
         this.y = point.pos.field_1351;
         this.z = point.pos.field_1350;
         this.radius = point.radius;
         this.start = System.currentTimeMillis();
      }
   }

   @Environment(EnvType.CLIENT)
   private static record KillPoint(class_243 pos, float radius) {
   }
}
