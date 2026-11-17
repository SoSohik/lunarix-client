package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Trails",
   desc = "Касивый эффект заде игрока при движении",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Trails extends Module implements ModulePreview {
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public final ModeSetting colorMode = new ModeSetting(this, "Цвет", "Клиент", "Случайный");
   public final ModeSetting rotateMode = new ModeSetting(this, "Вращение", "Движение", "Камера");
   public final SliderSetting dashLength = new SliderSetting(this, "Длина шлейфа", 0.75F, 0.5F, 1.5F, 0.01F);
   public final SliderSetting moveLerp = new SliderSetting(this, "Move Lerp", 0.2F, 0.1F, 0.5F, 0.01F);
   public final BooleanSetting motionSmooth = new BooleanSetting(this, "Сглаживание", false);
   public final BooleanSetting dashSegs = new BooleanSetting(this, "Сегменты", false);
   public final BooleanSetting dashDots = new BooleanSetting(this, "Точки", true);
   public final BooleanSetting lighting = new BooleanSetting(this, "Свечение", true);
   private final PreviewSettings previewSettings = PreviewSettings.withoutInterval(this, 4.0F, 0.0F);
   private static final double PREVIEW_ORBIT_MS = 4000.0;
   private static final int DASH_TEX_COUNT = 21;
   private static final long DASH_FRAME_TIME_MS = 80L;
   private static final int[] GROUP_SIZES = new int[]{11, 23, 32, 16, 32};
   private static final List<class_2960> DASH_TEXTURES = new ArrayList<>();
   private static final List<List<class_2960>> DASH_ANIM_GROUPS = new ArrayList<>();
   private static final class_2960 BLOOM_TEX = class_2960.method_60655("client", "textures/particles/glow.png");
   private static final RenderPipeline SPARK_PIPELINE;
   private static final class_1921 SPARK_LAYER;
   private static final RenderPipeline CUBIC_PIPELINE;
   private static final Function<class_2960, class_1921> CUBIC_LAYER;
   private final List<Trails.DashCubic> cubics = new ArrayList<>();
   private final List<Trails.DashCubic> filteredCache = new ArrayList<>();
   private final Random rng = new Random(1234567891L);
   private final class_9799 allocator = new class_9799(262144);
   private final Vector3f renderRight = new Vector3f();
   private final Vector3f renderUp = new Vector3f();
   private final Quaternionf bloomRotCache = new Quaternionf();
   private float stateAnim = 0.0F;
   private float lightAnim = 0.0F;
   private double prevX;
   private double prevY;
   private double prevZ;
   private boolean prevInit = false;
   private class_1309 previewEntity;
   private double previewPrevX;
   private double previewPrevY;
   private double previewPrevZ;
   private boolean previewPrevInit = false;

   @Override
   protected void onEnable() {
      this.cubics.clear();
      this.filteredCache.clear();
      this.stateAnim = 0.0F;
      this.prevInit = false;
   }

   @Override
   protected void onDisable() {
      this.cubics.clear();
      this.filteredCache.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.cubics.clear();
      this.filteredCache.clear();
      this.prevInit = false;
      this.previewPrevInit = false;
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
   public boolean previewControlsDummy() {
      return true;
   }

   @Override
   public void previewStart(PreviewContext ctx) {
      this.previewEntity = ctx.dummy();
      this.previewPrevInit = false;
   }

   @Override
   public void previewTick(PreviewContext ctx) {
      this.previewEntity = ctx.dummy();
      if (this.previewEntity != null) {
         double angle = (double)ctx.elapsedMs() / 4000.0 * Math.PI * 2.0;
         double radius = 1.6;
         class_243 anchor = ctx.anchor();
         this.previewEntity.method_5814(anchor.field_1352 + Math.cos(angle) * radius, anchor.field_1351, anchor.field_1350 + Math.sin(angle) * radius);
         float yaw = (float)Math.toDegrees(angle);
         this.previewEntity.method_36456(yaw);
         this.previewEntity.method_5636(yaw);
         this.previewEntity.method_5847(yaw);
      }
   }

   @Override
   public void previewSpawn(PreviewContext ctx) {
   }

   @Override
   public void previewStop() {
      this.previewEntity = null;
      this.previewPrevInit = false;
      this.cubics.clear();
      this.filteredCache.clear();
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null) {
         this.stateAnim = this.stateAnim + (1.0F - this.stateAnim) * 0.08F;
         this.lightAnim = this.lightAnim + ((this.lighting.getValue() ? 1.0F : 0.0F) - this.lightAnim) * 0.08F;
         if (!this.prevInit) {
            this.prevX = mc.field_1724.method_23317();
            this.prevY = mc.field_1724.method_23318();
            this.prevZ = mc.field_1724.method_23321();
            this.prevInit = true;
         } else {
            long nowMs = System.currentTimeMillis();
            int size = this.cubics.size();

            for (int i = 0; i < size; i++) {
               Trails.DashCubic c = this.cubics.get(i);
               c.cachedTimePC = Math.min(1.0F, (float)(nowMs - c.startTime) / (float)c.base.rMTime);
               if (c.cachedTimePC >= 1.0F && c.alphaTarget != 0.0F) {
                  c.alphaTarget = 0.0F;
               }

               c.alphaAnim = c.alphaAnim + (c.alphaTarget - c.alphaAnim) * 0.08F;
            }

            for (int var20 = this.cubics.size() - 1; var20 >= 0; var20--) {
               Trails.DashCubic c = this.cubics.get(var20);
               if (c.cachedTimePC >= 1.0F && c.alphaTarget == 0.0F && c.alphaAnim < 0.02F) {
                  this.cubics.remove(var20);
               }
            }

            this.filteredCache.clear();
            int n = this.cubics.size();

            for (int var21 = 0; var21 < n; var21++) {
               Trails.DashCubic c2 = this.cubics.get(var21);
               if (c2.alphaAnim > 0.05F) {
                  this.filteredCache.add(c2);
               }
            }

            float cameraPitch = mc.field_1773.method_19418().method_19329();
            boolean smooth = this.motionSmooth.getValue();
            int fsz = this.filteredCache.size();

            for (int i2 = 0; i2 < fsz; i2++) {
               Trails.DashCubic cur = this.filteredCache.get(i2);
               Trails.DashCubic next = smooth && i2 + 1 < fsz ? this.filteredCache.get(i2 + 1) : null;
               if (next != null && next.base.entityId != cur.base.entityId) {
                  next = null;
               }

               cur.motionProcess(next, nowMs, cameraPitch);
            }

            double lx = this.prevX;
            double ly = this.prevY;
            double lz = this.prevZ;
            double lr = this.moveLerp.getValue().doubleValue();
            this.prevX = class_3532.method_16436(lr, this.prevX, mc.field_1724.method_23317());
            this.prevY = class_3532.method_16436(lr, this.prevY, mc.field_1724.method_23318());
            this.prevZ = class_3532.method_16436(lr, this.prevZ, mc.field_1724.method_23321());
            this.spawnFor(mc.field_1724, lx, ly, lz);
            this.tickPreviewTrail(lr);
         }
      }
   }

   private void tickPreviewTrail(double lr) {
      if (this.previewEntity == null || this.previewEntity.method_31481()) {
         this.previewPrevInit = false;
      } else if (!this.previewPrevInit) {
         this.previewPrevX = this.previewEntity.method_23317();
         this.previewPrevY = this.previewEntity.method_23318();
         this.previewPrevZ = this.previewEntity.method_23321();
         this.previewPrevInit = true;
      } else {
         double lx = this.previewPrevX;
         double ly = this.previewPrevY;
         double lz = this.previewPrevZ;
         this.previewPrevX = class_3532.method_16436(lr, this.previewPrevX, this.previewEntity.method_23317());
         this.previewPrevY = class_3532.method_16436(lr, this.previewPrevY, this.previewEntity.method_23318());
         this.previewPrevZ = class_3532.method_16436(lr, this.previewPrevZ, this.previewEntity.method_23321());
         this.spawnFor(this.previewEntity, lx, ly, lz);
      }
   }

   private void spawnFor(class_1309 entity, double lx, double ly, double lz) {
      if (entity != null) {
         double dx = entity.method_23317() - lx;
         double dy = entity.method_23318() - ly;
         double dz = entity.method_23321() - lz;
         if (!(dx * dx + dz * dz < 0.0064)) {
            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz);
            int count = (int)class_3532.method_15363((float)(speed / 0.4F), 1.0F, 16.0F);

            for (int i = 0; i < count; i++) {
               int rMTime = (int)((float)(550 + this.rng.nextInt(300)) * this.dashLength.getValue());
               this.cubics.add(new Trails.DashCubic(this, new Trails.DashBase(entity, 0.04F, (float)i / (float)count, rMTime)));
            }
         }
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (mc.field_1724 != null && !(this.stateAnim < 0.05F) && !this.filteredCache.isEmpty()) {
         float ticks = e.getTickDelta();
         float alphaPC = this.stateAnim;
         float lightPC = this.lightAnim;
         int fsz = this.filteredCache.size();
         long nowMs = System.currentTimeMillis();
         class_4587 matrix = e.getMatrixStack();
         class_243 cam = mc.method_1561().field_4686.method_71156();
         double camX = cam.field_1352;
         double camY = cam.field_1351;
         double camZ = cam.field_1350;
         class_4598 immediate = class_4597.method_22991(this.allocator);
         float fCamYaw = mc.field_1773.method_19418().method_19330();
         float fCamPitch = mc.field_1773.method_19418().method_19329();
         Quaternionf camRot = mc.field_1773.method_19418().method_23767();
         camRot.transform(this.renderRight.set(1.0F, 0.0F, 0.0F));
         camRot.transform(this.renderUp.set(0.0F, 1.0F, 0.0F));
         this.bloomRotCache.identity().rotateY((float)Math.toRadians((double)(-fCamYaw))).rotateX((float)Math.toRadians((double)fCamPitch));

         for (int i = 0; i < fsz; i++) {
            Trails.DashCubic cubic = this.filteredCache.get(i);
            class_2960 tex = cubic.dashTexture.getCurrent(nowMs);
            if (tex != null) {
               float aPC = cubic.alphaAnim * alphaPC;
               float ext = 4.224F * aPC * 0.3F;
               int col = cubic.color;
               matrix.method_22903();
               matrix.method_22904(cubic.getRenderX(ticks) - camX, cubic.getRenderY(ticks) - camY, cubic.getRenderZ(ticks) - camZ);
               this.applyBodyRotation(matrix, cubic.rotate, fCamYaw, fCamPitch);
               matrix.method_22905(-0.1F, -0.1F, 0.1F);
               drawTexQuad(
                  immediate.method_73477(CUBIC_LAYER.apply(tex)),
                  matrix.method_23760().method_23761(),
                  -ext,
                  -ext,
                  ext,
                  ext,
                  ColorUtil.multDark(ColorUtil.overCol(col, -1, 0.4F), aPC)
               );
               matrix.method_22909();
            }
         }

         class_4588 bloomBuf = immediate.method_73477(CUBIC_LAYER.apply(BLOOM_TEX));

         for (int ix = 0; ix < fsz; ix++) {
            Trails.DashCubic cubic = this.filteredCache.get(ix);
            float aPC = cubic.alphaAnim * alphaPC;
            float base = 4.224F * aPC * 0.3F;
            float timePcOf = 1.0F - cubic.cachedTimePC;
            int col = cubic.color;
            int baseCol = ColorUtil.overCol(col, -1, 0.18F);
            matrix.method_22903();
            matrix.method_22904(cubic.getRenderX(ticks) - camX, cubic.getRenderY(ticks) - camY, cubic.getRenderZ(ticks) - camZ);
            matrix.method_22907(this.bloomRotCache);
            matrix.method_22905(-0.1F, -0.1F, 0.1F);
            Matrix4f mat = matrix.method_23760().method_23761();
            drawTexQuad(bloomBuf, mat, -base * 6.0F, -base * 6.0F, base * 6.0F, base * 6.0F, ColorUtil.swapAlpha(baseCol, 15.0F * aPC));
            drawTexQuad(bloomBuf, mat, -base * 2.2F, -base * 2.2F, base * 2.2F, base * 2.2F, ColorUtil.swapAlpha(baseCol, 70.0F * aPC));
            drawTexQuad(bloomBuf, mat, -base * 1.1F, -base * 1.1F, base * 1.1F, base * 1.1F, ColorUtil.swapAlpha(baseCol, 140.0F * aPC));
            if (lightPC > 0.0F) {
               float aMul = aPC * lightPC;
               float bigExt = base * (1.0F + 8.0F * timePcOf * aMul);
               drawTexQuad(bloomBuf, mat, -bigExt, -bigExt, bigExt, bigExt, ColorUtil.swapAlpha(ColorUtil.multDark(baseCol, aMul / 4.0F), 95.0F * aMul));
            }

            matrix.method_22909();
         }

         immediate.method_22993();
      }
   }

   private void applyBodyRotation(class_4587 m, float[] rotate, float camYaw, float camPitch) {
      if (this.rotateMode.is("Движение")) {
         m.method_22907(class_7833.field_40716.rotationDegrees(-rotate[0]));
         m.method_22907(class_7833.field_40714.rotationDegrees(rotate[1]));
      } else {
         m.method_22907(class_7833.field_40716.rotationDegrees(-camYaw));
         m.method_22907(class_7833.field_40714.rotationDegrees(camPitch));
      }
   }

   private static void drawSparkQuad(class_4588 buf, Matrix4f mat, Vector3f right, Vector3f up, float s, float cx, float cy, float cz, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      float rx = right.x * s;
      float ry = right.y * s;
      float rz = right.z * s;
      float ux = up.x * s;
      float uy = up.y * s;
      float uz = up.z * s;
      buf.method_22918(mat, cx - rx + ux, cy - ry + uy, cz - rz + uz).method_1336(r, g, b, a);
      buf.method_22918(mat, cx + rx + ux, cy + ry + uy, cz + rz + uz).method_1336(r, g, b, a);
      buf.method_22918(mat, cx + rx - ux, cy + ry - uy, cz + rz - uz).method_1336(r, g, b, a);
      buf.method_22918(mat, cx - rx - ux, cy - ry - uy, cz - rz - uz).method_1336(r, g, b, a);
   }

   private static void drawTexQuad(class_4588 buf, Matrix4f mat, float x1, float y1, float x2, float y2, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      buf.method_22918(mat, x1, y1, 0.0F).method_1336(r, g, b, a).method_22913(0.0F, 1.0F);
      buf.method_22918(mat, x2, y1, 0.0F).method_1336(r, g, b, a).method_22913(1.0F, 1.0F);
      buf.method_22918(mat, x2, y2, 0.0F).method_1336(r, g, b, a).method_22913(1.0F, 0.0F);
      buf.method_22918(mat, x1, y2, 0.0F).method_1336(r, g, b, a).method_22913(0.0F, 0.0F);
   }

   private int getDashColor() {
      return this.colorMode.is("Случайный") ? Color.getHSBColor((float)this.rng.nextInt(255) / 255.0F, 1.0F, 1.0F).getRGB() | 0xFF000000 : ColorUtil.fade(0);
   }

   private static float computeMotionYaw(double mx, double mz) {
      int y = (int)(Math.toDegrees(Math.atan2(mz, mx)) - 90.0);
      return y < 0 ? (float)(y + 360) : (float)y;
   }

   static {
      for (int i = 1; i <= 21; i++) {
         DASH_TEXTURES.add(class_2960.method_60655("client", "textures/dash_cubes/dashcubic" + i + ".png"));
      }

      for (int g = 1; g <= GROUP_SIZES.length; g++) {
         ArrayList<class_2960> grp = new ArrayList<>();

         for (int f = 1; f <= GROUP_SIZES[g - 1]; f++) {
            grp.add(class_2960.method_60655("client", "textures/dash_cubes/group_dashs/group" + g + "/dashcubic" + f + ".png"));
         }

         DASH_ANIM_GROUPS.add(grp);
      }

      SPARK_PIPELINE = class_10799.method_67887(
         RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
            .withLocation(class_2960.method_60655("client", "pipeline/trails/spark"))
            .withVertexFormat(class_290.field_1576, class_5596.field_27382)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.LIGHTNING)
            .build()
      );
      SPARK_LAYER = class_1921.method_75940("trails_spark", class_12247.method_75927(SPARK_PIPELINE).method_75929(16384).method_75938());
      CUBIC_PIPELINE = class_10799.method_67887(
         RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
            .withLocation(class_2960.method_60655("client", "pipeline/trails/cubic"))
            .withVertexFormat(class_290.field_1575, class_5596.field_27382)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.LIGHTNING)
            .build()
      );
      CUBIC_LAYER = class_156.method_34866(
         tex -> class_1921.method_75940(
               "trails_cubic", class_12247.method_75927(CUBIC_PIPELINE).method_75934("Sampler0", tex).method_75937().method_75929(1536).method_75938()
            )
      );
   }

   @Environment(EnvType.CLIENT)
   private static class DashBase {
      final int entityId;
      double motionX;
      double motionY;
      double motionZ;
      double posX;
      double posY;
      double posZ;
      double prevPosX;
      double prevPosY;
      double prevPosZ;
      final int rMTime;
      final float yaw;
      final float prevYaw;

      DashBase(class_1309 entity, float speed, float offsetPC, int rmTime) {
         this.entityId = entity.method_5628();
         this.rMTime = rmTime;
         this.yaw = entity.method_36454();
         this.prevYaw = entity.field_5982;
         double mx = -(entity.field_6038 - entity.method_23317());
         double my = -(entity.field_5971 - entity.method_23318());
         double mz = -(entity.field_5989 - entity.method_23321());
         this.motionX = mx;
         this.motionY = my;
         this.motionZ = mz;
         float h = entity.method_17682();
         float swimDiv = entity.method_5869() ? 2.4F : 1.0F;
         this.posX = entity.field_6038 - mx * (double)offsetPC + -0.0875 + 0.175 * Math.random();
         this.posY = entity.field_5971 - my * (double)offsetPC + (double)(h / swimDiv) / 3.0 + (double)(h / swimDiv) / 4.0 * Math.random() * 2.7;
         this.posZ = entity.field_5989 - mz * (double)offsetPC + -0.0875 + 0.175 * Math.random();
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.motionX *= (double)speed;
         this.motionY *= (double)speed;
         this.motionZ *= (double)speed;
      }
   }

   @Environment(EnvType.CLIENT)
   private class DashCubic {
      float alphaAnim = 0.0F;
      float alphaTarget = 1.0F;
      float cachedTimePC = 0.0F;
      final long startTime = System.currentTimeMillis();
      final Trails.DashBase base;
      final int color;
      final float[] rotate = new float[2];
      final List<Trails.DashSpark> sparks = new ArrayList<>(4);
      final Trails.DashTexture dashTexture;

      DashCubic(Trails trails, Trails.DashBase base) {
         this.base = base;
         this.color = trails.getDashColor();
         this.dashTexture = Trails.this.new DashTexture(trails);
         this.recomputeRotate(IMinecraft.mc.field_1773.method_19418().method_19329());
      }

      void recomputeRotate(float cameraPitch) {
         double spd = Math.sqrt(this.base.motionX * this.base.motionX + this.base.motionZ * this.base.motionZ);
         if (spd < 5.0E-4) {
            this.rotate[0] = (float)(360.0 * Math.random());
            this.rotate[1] = cameraPitch;
         } else {
            float motYaw = Trails.computeMotionYaw(this.base.motionX, this.base.motionZ);
            this.rotate[0] = motYaw - 60.0F - (this.base.prevYaw - this.base.yaw) * 3.0F;
            float diff = Math.abs(motYaw + 26.3F - this.base.yaw) % 360.0F;
            if (diff > 180.0F) {
               diff = 360.0F - diff;
            }

            this.rotate[1] = !(diff < 10.0F) && !(diff > 160.0F) ? cameraPitch : -90.0F;
         }
      }

      double getRenderX(float pt) {
         return this.base.prevPosX + (this.base.posX - this.base.prevPosX) * (double)pt;
      }

      double getRenderY(float pt) {
         return this.base.prevPosY + (this.base.posY - this.base.prevPosY) * (double)pt;
      }

      double getRenderZ(float pt) {
         return this.base.prevPosZ + (this.base.posZ - this.base.prevPosZ) * (double)pt;
      }

      void motionProcess(Trails.DashCubic next, long nowMs, float cameraPitch) {
         this.base.prevPosX = this.base.posX;
         this.base.prevPosY = this.base.posY;
         this.base.prevPosZ = this.base.posZ;
         this.base.motionX = (next != null ? next.base.motionX : this.base.motionX) / 1.05;
         this.base.posX = this.base.posX + 5.0 * this.base.motionX;
         this.base.motionY = (next != null ? next.base.motionY : this.base.motionY) / 1.05;
         this.base.posY = this.base.posY + 5.0 * this.base.motionY / (this.base.motionY < 0.0 ? 1.0 : 3.5);
         this.base.motionZ = (next != null ? next.base.motionZ : this.base.motionZ) / 1.05;
         this.base.posZ = this.base.posZ + 5.0 * this.base.motionZ;
         this.recomputeRotate(cameraPitch);
         int n = this.sparks.size();

         for (int j = 0; j < n; j++) {
            this.sparks.get(j).update(nowMs);
         }

         for (int var7 = this.sparks.size() - 1; var7 >= 0; var7--) {
            if (this.sparks.get(var7).dead) {
               this.sparks.remove(var7);
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static class DashSpark {
      double posX;
      double posY;
      double posZ;
      double prevX;
      double prevY;
      double prevZ;
      boolean dead = false;
      float cachedAlphaPC = 1.0F;
      final double sinYaw;
      final double cosYaw;
      final double cosPitchOffset;
      final double speed = Math.random() / 50.0;
      final long startTime = System.currentTimeMillis();

      DashSpark() {
         double yaw = Math.random() * Math.PI * 2.0;
         double pitch = Math.toRadians(-90.0 + Math.random() * 180.0 - 90.0);
         this.sinYaw = Math.sin(yaw);
         this.cosYaw = Math.cos(yaw);
         this.cosPitchOffset = Math.cos(pitch);
      }

      void update(long nowMs) {
         float timePC = class_3532.method_15363((float)(nowMs - this.startTime) / 1000.0F, 0.0F, 1.0F);
         this.dead = timePC >= 1.0F;
         this.cachedAlphaPC = 1.0F - timePC;
         this.prevX = this.posX;
         this.prevY = this.posY;
         this.prevZ = this.posZ;
         this.posX = this.posX + this.sinYaw * this.speed;
         this.posY = this.posY + this.cosPitchOffset * this.speed;
         this.posZ = this.posZ + this.cosYaw * this.speed;
      }

      double getRenderX(float pt) {
         return this.prevX + (this.posX - this.prevX) * (double)pt;
      }

      double getRenderY(float pt) {
         return this.prevY + (this.posY - this.prevY) * (double)pt;
      }

      double getRenderZ(float pt) {
         return this.prevZ + (this.posZ - this.prevZ) * (double)pt;
      }
   }

   @Environment(EnvType.CLIENT)
   private class DashTexture {
      private final List<class_2960> textures;
      private final boolean animated;
      private final long spawnTime = System.currentTimeMillis();
      private final long animDur;

      DashTexture(Trails trails) {
         boolean doAnim;
         this.animated = doAnim = trails.rng.nextInt(100) > 40 && !Trails.DASH_ANIM_GROUPS.isEmpty();
         if (doAnim) {
            this.textures = Trails.DASH_ANIM_GROUPS.get(trails.rng.nextInt(Trails.DASH_ANIM_GROUPS.size()));
            this.animDur = (long)((float)(550 + trails.rng.nextInt(300)) * trails.dashLength.getValue());
         } else {
            this.textures = Trails.DASH_TEXTURES.isEmpty() ? List.of() : List.of(Trails.DASH_TEXTURES.get(trails.rng.nextInt(Trails.DASH_TEXTURES.size())));
            this.animDur = 0L;
         }
      }

      class_2960 getCurrent(long nowMs) {
         if (this.textures.isEmpty()) {
            return null;
         } else if (this.animated && this.textures.size() > 1 && this.animDur > 0L) {
            int idx = (int)((nowMs - this.spawnTime) / 80L % (long)this.textures.size());
            return this.textures.get(idx);
         } else {
            return this.textures.get(0);
         }
      }
   }
}
