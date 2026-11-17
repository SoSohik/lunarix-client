package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
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
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Fire Flies",
   desc = "Светлячки вокруг игрока",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class FireFlies extends Module {
   private static final long MAX_PART_ALIVE_TIME = 5500L;
   private static final RenderPipeline FLY_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("mytheria", "pipeline/fireflies"))
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   private static final RenderPipeline TRAIL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("mytheria", "pipeline/fireflies_trail"))
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .build()
   );
   private static final RenderPipeline SPARK_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("mytheria", "pipeline/fireflies_spark"))
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .build()
   );
   private static final class_2960 FIRE_PART_TEX = class_2960.method_60655("client", "textures/particles/circle.png");
   private static final class_2960 BLOOM_TEX = class_2960.method_60655("client", "textures/particles/glow.png");
   private static final class_1921 FLY_LAYER = class_1921.method_75940(
      "mytheria_fireflies", class_12247.method_75927(FLY_PIPELINE).method_75934("Sampler0", FIRE_PART_TEX).method_75937().method_75929(8192).method_75938()
   );
   private static final class_1921 BLOOM_LAYER = class_1921.method_75940(
      "mytheria_fireflies_bloom", class_12247.method_75927(FLY_PIPELINE).method_75934("Sampler0", BLOOM_TEX).method_75937().method_75929(8192).method_75938()
   );
   private static final class_1921 TRAIL_LAYER = class_1921.method_75940(
      "mytheria_fireflies_trail", class_12247.method_75927(TRAIL_PIPELINE).method_75937().method_75929(16384).method_75938()
   );
   private static final class_1921 SPARK_LAYER = class_1921.method_75940(
      "mytheria_fireflies_spark", class_12247.method_75927(SPARK_PIPELINE).method_75937().method_75929(16384).method_75938()
   );
   public ModeSetting colorMode = new ModeSetting(this, "Цвет", "Европа", "Свой", "Тема");
   public ColorSetting pickColor = new ColorSetting(this, "Свой цвет", ColorUtil.getColor(255, 70, 0, 255)).setVisible(() -> this.colorMode.is("Свой"));
   public BooleanSetting darkImprint = new BooleanSetting(this, "Тёмный след", false);
   public BooleanSetting lighting = new BooleanSetting(this, "Свечение", false);
   public SliderSetting spawnDelay = new SliderSetting(this, "Задержка спавна", 3.0F, 1.0F, 10.0F, 1.0F);
   private final List<FireFlies.FirePart> fireParts = new ArrayList<>();
   private static final class_2960 SPARK_GLOW_TEX = class_2960.method_60655("client", "textures/particles/glow.png");
   private static final class_1921 SPARK_GLOW_LAYER = class_1921.method_75940(
      "mytheria_fireflies_spark_glow",
      class_12247.method_75927(FLY_PIPELINE).method_75934("Sampler0", SPARK_GLOW_TEX).method_75937().method_75929(8192).method_75938()
   );

   @Override
   protected void onDisable() {
      super.onDisable();
      this.fireParts.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.fireParts.clear();
   }

   private int getPartColor() {
      if (this.colorMode.is("Европа")) {
         return Color.getHSBColor((float)Math.random(), 1.0F, 1.0F).getRGB();
      } else {
         return this.colorMode.is("Тема") ? ColorUtil.client() : this.pickColor.getValue();
      }
   }

   private static float getRandom(double min, double max) {
      return (float)ThreadLocalRandom.current().nextDouble(min, Math.max(max, min + 1.0E-6));
   }

   private class_243 generateVecForPart(double rangeXZ, double rangeY) {
      class_243 pos = this.randomAround(rangeXZ, rangeY);

      for (int i = 0; i < 30; i++) {
         double dx = pos.field_1352 - mc.field_1724.method_23317();
         double dz = pos.field_1350 - mc.field_1724.method_23321();
         if (this.posBlock(pos.field_1352, pos.field_1351, pos.field_1350) || Math.sqrt(dx * dx + dz * dz) < rangeXZ / 3.0) {
            pos = this.randomAround(rangeXZ, rangeY);
         }
      }

      return pos;
   }

   private class_243 randomAround(double rangeXZ, double rangeY) {
      return mc.field_1724
         .method_73189()
         .method_1031((double)getRandom(-rangeXZ, rangeXZ), (double)getRandom(-rangeY / 3.0, rangeY), (double)getRandom(-rangeXZ, rangeXZ));
   }

   private boolean posBlock(double x, double y, double z) {
      class_2680 state = mc.field_1687.method_8320(class_2338.method_49637(x, y, z));
      return !state.method_26215() && state.method_26227().method_15769() && !state.method_45474();
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1724.field_6012 == 1) {
            this.fireParts.forEach(FireFlies.FirePart::setToRemove);
         }

         this.fireParts.forEach(FireFlies.FirePart::updatePart);
         this.fireParts.removeIf(FireFlies.FirePart::isToRemove);
         if (mc.field_1724.field_6012 % (this.spawnDelay.getValue().intValue() + 1) == 0) {
            int color = this.getPartColor();
            this.fireParts.add(new FireFlies.FirePart(this, this.generateVecForPart(10.0, 4.0), 5500.0F, color));
            this.fireParts.add(new FireFlies.FirePart(this, this.generateVecForPart(6.0, 5.0), 5500.0F, color));
         }
      }
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.fireParts.isEmpty()) {
         float pTicks = e.getTickDelta();
         class_243 cameraPos = mc.field_1773.method_19418().method_71156();
         Quaternionf cameraRotation = mc.field_1773.method_19418().method_23767();
         Vector3f right = cameraRotation.transform(new Vector3f(1.0F, 0.0F, 0.0F));
         Vector3f up = cameraRotation.transform(new Vector3f(0.0F, 1.0F, 0.0F));
         Vector3f forward = cameraRotation.transform(new Vector3f(0.0F, 0.0F, -1.0F));
         class_4587 matrices = e.getMatrixStack();
         Matrix4f matrix = matrices.method_23760().method_23761();
         class_4598 consumers = mc.method_22940().method_23000();
         class_4588 trailConsumer = consumers.method_73477(TRAIL_LAYER);

         for (FireFlies.FirePart firePart : this.fireParts) {
            if (!this.isBehindCamera(firePart, cameraPos, forward)) {
               this.drawTrail(trailConsumer, matrix, firePart, cameraPos);
            }
         }

         consumers.method_22994(TRAIL_LAYER);
         if (this.lighting.getValue()) {
            class_4588 glowConsumer = consumers.method_73477(SPARK_GLOW_LAYER);

            for (FireFlies.FirePart firePartx : this.fireParts) {
               if (!this.isBehindCamera(firePartx, cameraPos, forward)) {
                  this.drawSparkGlow(glowConsumer, matrix, firePartx, cameraPos, right, up, pTicks);
               }
            }

            consumers.method_22994(SPARK_GLOW_LAYER);
         }

         class_4588 sparkConsumer = consumers.method_73477(SPARK_LAYER);

         for (FireFlies.FirePart firePartxx : this.fireParts) {
            if (!this.isBehindCamera(firePartxx, cameraPos, forward)) {
               this.drawSparks(sparkConsumer, matrix, firePartxx, cameraPos, right, up, pTicks);
            }
         }

         consumers.method_22994(SPARK_LAYER);
         class_4588 class_45882 = consumers.method_73477(FLY_LAYER);

         for (FireFlies.FirePart part : this.fireParts) {
            if (!this.isBehindCamera(part, cameraPos, forward)) {
               this.drawBody(class_45882, matrix, part, cameraPos, right, up, pTicks, false);
            }
         }

         consumers.method_22994(FLY_LAYER);
         if (this.lighting.getValue()) {
            class_4588 class_45883 = consumers.method_73477(BLOOM_LAYER);

            for (FireFlies.FirePart partx : this.fireParts) {
               if (!this.isBehindCamera(partx, cameraPos, forward)) {
                  this.drawBody(class_45883, matrix, partx, cameraPos, right, up, pTicks, true);
               }
            }

            consumers.method_22994(BLOOM_LAYER);
         }
      }
   }

   private boolean isBehindCamera(FireFlies.FirePart part, class_243 cameraPos, Vector3f forward) {
      float px = (float)(part.pos.field_1352 - cameraPos.field_1352);
      float py = (float)(part.pos.field_1351 - cameraPos.field_1351);
      float pz = (float)(part.pos.field_1350 - cameraPos.field_1350);
      return px * forward.x + py * forward.y + pz * forward.z < -2.0F;
   }

   private void drawSparkGlow(class_4588 consumer, Matrix4f matrix, FireFlies.FirePart part, class_243 cameraPos, Vector3f right, Vector3f up, float pTicks) {
      if (!part.sparkParts.isEmpty()) {
         double dist = mc.field_1724.method_5649(part.pos.field_1352, part.pos.field_1351 + 1.6, part.pos.field_1350);
         float size = 0.035F + 0.02F * class_3532.method_15363(1.0F - ((float)Math.sqrt(dist) - 3.0F) / 10.0F, 0.0F, 1.0F);

         for (FireFlies.SparkPart spark : part.sparkParts) {
            float life = (float)spark.timePC();
            int color = setAlphaF(part.color, alphaOf(part.color) * (1.0F - life) * 0.3F);
            class_243 pos = new class_243(spark.getRenderPosX(pTicks), spark.getRenderPosY(pTicks), spark.getRenderPosZ(pTicks));
            this.quad(consumer, matrix, pos, cameraPos, right, up, size * (1.4F - life), color);
         }
      }
   }

   private void drawBody(
      class_4588 consumer, Matrix4f matrix, FireFlies.FirePart part, class_243 cameraPos, Vector3f right, Vector3f up, float pTicks, boolean bloomPass
   ) {
      float alphaPC = part.getAlphaPC();
      if (!(alphaPC <= 0.004F)) {
         int color = setAlphaF(part.color, alphaOf(part.color) * alphaPC);
         float brightness = brightnessOf(color);
         int bodyColor = lerpToWhiteKeepAlpha(color, alphaOf(color) / 255.0F * brightness * 0.3F);
         class_243 pos = part.getRenderPosVec(pTicks);
         float half = 0.04F * alphaPC;
         if (bloomPass) {
            bodyColor = setAlphaF(bodyColor, alphaOf(bodyColor) * 0.3F);
            half *= 5.0F;
         }

         this.quad(consumer, matrix, pos, cameraPos, right, up, half, bodyColor);
      }
   }

   private void drawTrail(class_4588 consumer, Matrix4f matrix, FireFlies.FirePart part, class_243 cameraPos) {
      if (part.trailParts.size() >= 2) {
         int baseColor = setAlphaF(part.color, alphaOf(part.color) * part.getAlphaPC());
         if (this.darkImprint.getValue()) {
            baseColor = darken(baseColor, 0.3F);
         }

         double distToPart = mc.field_1724.method_5649(part.pos.field_1352, part.pos.field_1351 + 1.6, part.pos.field_1350);
         float widthFactor = class_3532.method_15363(1.0F - ((float)Math.sqrt(distToPart) - 3.0F) / 20.0F, 0.0F, 1.0F);
         float halfWidth = 0.012F * widthFactor + 1.0E-4F;
         int pointsCount = part.trailParts.size();

         for (int i = 0; i < pointsCount - 1; i++) {
            FireFlies.TrailPart a = part.trailParts.get(i);
            FireFlies.TrailPart b = part.trailParts.get(i + 1);
            float sizePC = (float)i / (float)pointsCount;
            float sizePC2 = (float)(i + 1) / (float)pointsCount;
            int c1 = this.trailColor(baseColor, part.getAlphaPC(), sizePC);
            int c2 = this.trailColor(baseColor, part.getAlphaPC(), sizePC2);
            this.emitRibbonSegment(consumer, matrix, a, b, cameraPos, halfWidth, c1, c2);
         }
      }
   }

   private int trailColor(int baseColor, float alphaPC, float sizePC) {
      float wave = easeInOutQuadWave(sizePC);
      int c = setAlphaF(baseColor, alphaOf(baseColor) * alphaPC * wave);
      return lerpToWhiteKeepAlpha(c, alphaOf(c) / 255.0F * brightnessOf(c) * 0.25F * sizePC);
   }

   private void emitRibbonSegment(
      class_4588 consumer, Matrix4f matrix, FireFlies.TrailPart a, FireFlies.TrailPart b, class_243 cameraPos, float halfWidth, int c1, int c2
   ) {
      float ay = (float)(a.y - cameraPos.field_1351);
      float bz = (float)(b.z - cameraPos.field_1350);
      float az = (float)(a.z - cameraPos.field_1350);
      float dz = bz - az;
      float by = (float)(b.y - cameraPos.field_1351);
      float dy = by - ay;
      float cx = ay * dz - az * dy;
      float bx = (float)(b.x - cameraPos.field_1352);
      float ax = (float)(a.x - cameraPos.field_1352);
      float dx = bx - ax;
      float cy = az * dx - ax * dz;
      float cz = ax * dy - ay * dx;
      float len = (float)Math.sqrt((double)(cx * cx + cy * cy + cz * cz));
      if (!(len < 1.0E-6F)) {
         cx = cx / len * halfWidth;
         cy = cy / len * halfWidth;
         cz = cz / len * halfWidth;
         this.vertex(consumer, matrix, ax - cx, ay - cy, az - cz, c1);
         this.vertex(consumer, matrix, ax + cx, ay + cy, az + cz, c1);
         this.vertex(consumer, matrix, bx + cx, by + cy, bz + cz, c2);
         this.vertex(consumer, matrix, bx - cx, by - cy, bz - cz, c2);
      }
   }

   private void drawSparks(class_4588 consumer, Matrix4f matrix, FireFlies.FirePart part, class_243 cameraPos, Vector3f right, Vector3f up, float pTicks) {
      if (part.sparkParts.size() >= 2) {
         double distToPart = mc.field_1724.method_5649(part.pos.field_1352, part.pos.field_1351 + 1.6, part.pos.field_1350);
         float pointsScale = 0.25F + 5.0F * class_3532.method_15363(1.0F - ((float)Math.sqrt(distToPart) - 3.0F) / 10.0F, 0.0F, 1.0F);
         float half = pointsScale * 0.0012F;
         float alphaPC = part.getAlphaPC();

         for (FireFlies.SparkPart spark : part.sparkParts) {
            float timePC = (float)spark.timePC();
            int c = lerpKeepNone(-1, part.color, timePC);
            c = setAlphaF(c, alphaOf(part.color) * alphaPC * (1.0F - timePC * timePC * timePC));
            class_243 pos = new class_243(spark.getRenderPosX(pTicks), spark.getRenderPosY(pTicks), spark.getRenderPosZ(pTicks));
            this.quadNoTex(consumer, matrix, pos, cameraPos, right, up, half, c);
         }
      }
   }

   private void quadNoTex(class_4588 consumer, Matrix4f matrix, class_243 pos, class_243 cameraPos, Vector3f right, Vector3f up, float half, int color) {
      float px = (float)(pos.field_1352 - cameraPos.field_1352);
      float py = (float)(pos.field_1351 - cameraPos.field_1351);
      float pz = (float)(pos.field_1350 - cameraPos.field_1350);
      float rx = right.x * half;
      float ry = right.y * half;
      float rz = right.z * half;
      float ux = up.x * half;
      float uy = up.y * half;
      float uz = up.z * half;
      this.vertex(consumer, matrix, px - rx - ux, py - ry - uy, pz - rz - uz, color);
      this.vertex(consumer, matrix, px - rx + ux, py - ry + uy, pz - rz + uz, color);
      this.vertex(consumer, matrix, px + rx + ux, py + ry + uy, pz + rz + uz, color);
      this.vertex(consumer, matrix, px + rx - ux, py + ry - uy, pz + rz - uz, color);
   }

   private void quad(class_4588 consumer, Matrix4f matrix, class_243 pos, class_243 cameraPos, Vector3f right, Vector3f up, float half, int color) {
      float px = (float)(pos.field_1352 - cameraPos.field_1352);
      float py = (float)(pos.field_1351 - cameraPos.field_1351);
      float pz = (float)(pos.field_1350 - cameraPos.field_1350);
      float rx = right.x * half;
      float ry = right.y * half;
      float rz = right.z * half;
      float ux = up.x * half;
      float uy = up.y * half;
      float uz = up.z * half;
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      float a = (float)(color >>> 24 & 0xFF) / 255.0F;
      consumer.method_22918(matrix, px - rx - ux, py - ry - uy, pz - rz - uz).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      consumer.method_22918(matrix, px - rx + ux, py - ry + uy, pz - rz + uz).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      consumer.method_22918(matrix, px + rx + ux, py + ry + uy, pz + rz + uz).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      consumer.method_22918(matrix, px + rx - ux, py + ry - uy, pz + rz - uz).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
   }

   private void vertex(class_4588 consumer, Matrix4f matrix, float x, float y, float z, int color) {
      consumer.method_22918(matrix, x, y, z)
         .method_22915(
            (float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F, (float)(color >>> 24 & 0xFF) / 255.0F
         );
   }

   private static float alphaOf(int color) {
      return (float)(color >>> 24 & 0xFF);
   }

   private static int setAlphaF(int color, float alpha) {
      int a = class_3532.method_15340(Math.round(alpha), 0, 255);
      return color & 16777215 | a << 24;
   }

   private static float brightnessOf(int color) {
      float[] hsb = Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);
      return hsb[2];
   }

   private static int darken(int color, float factor) {
      int r = (int)((float)(color >> 16 & 0xFF) * factor);
      int g = (int)((float)(color >> 8 & 0xFF) * factor);
      int b = (int)((float)(color & 0xFF) * factor);
      return color & 0xFF000000 | r << 16 | g << 8 | b;
   }

   private static int lerpKeepNone(int from, int to, float t) {
      t = class_3532.method_15363(t, 0.0F, 1.0F);
      int r = class_3532.method_48781(t, from >> 16 & 0xFF, to >> 16 & 0xFF);
      int g = class_3532.method_48781(t, from >> 8 & 0xFF, to >> 8 & 0xFF);
      int b = class_3532.method_48781(t, from & 0xFF, to & 0xFF);
      int a = class_3532.method_48781(t, from >>> 24 & 0xFF, to >>> 24 & 0xFF);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int lerpToWhiteKeepAlpha(int color, float t) {
      int a = color >>> 24 & 0xFF;
      int mixed = lerpKeepNone(color, -1, class_3532.method_15363(t, 0.0F, 1.0F));
      return mixed & 16777215 | a << 24;
   }

   private static float easeInOutQuadWave(float t) {
      float v = 2.0F * t - 1.0F;
      return 1.0F - v * v;
   }

   @Environment(EnvType.CLIENT)
   private class FirePart {
      final List<FireFlies.TrailPart> trailParts = new ArrayList<>();
      final List<FireFlies.SparkPart> sparkParts = new ArrayList<>();
      class_243 prevPos;
      class_243 pos;
      float alphaAnim = 0.0F;
      float alphaTarget = 1.0F;
      int msChangeSideRate = this.getMsChangeSideRate();
      final int color;
      float moveYawSet = FireFlies.getRandom(0.0, 360.0);
      float speed = FireFlies.getRandom(0.08, 0.175);
      float yMotion = FireFlies.getRandom(-0.075, 0.1);
      float moveYaw = this.moveYawSet;
      final float maxAlive;
      final long startTime;
      long rateTimer;
      boolean toRemove;

      FirePart(FireFlies fireFlies, class_243 pos, float maxAlive, int color) {
         this.rateTimer = this.startTime = System.currentTimeMillis();
         this.color = color;
         this.pos = pos;
         this.prevPos = pos;
         this.maxAlive = maxAlive;
      }

      float getTimePC() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / this.maxAlive, 0.0F, 1.0F);
      }

      float getAlphaPC() {
         return this.alphaAnim;
      }

      class_243 getRenderPosVec(float pTicks) {
         return this.pos
            .method_1031(
               -(this.prevPos.field_1352 - this.pos.field_1352) * (double)pTicks,
               -(this.prevPos.field_1351 - this.pos.field_1351) * (double)pTicks,
               -(this.prevPos.field_1350 - this.pos.field_1350) * (double)pTicks
            );
      }

      void updatePart() {
         this.alphaAnim = this.alphaAnim + class_3532.method_15363(this.alphaTarget - this.alphaAnim, -0.02F, 0.02F) * 2.0F;
         this.alphaAnim = class_3532.method_15363(this.alphaAnim, 0.0F, 1.0F);
         if (System.currentTimeMillis() - this.rateTimer >= (long)this.msChangeSideRate) {
            this.msChangeSideRate = this.getMsChangeSideRate();
            this.rateTimer = System.currentTimeMillis();
            this.moveYawSet = FireFlies.getRandom(0.0, 360.0);
         }

         this.moveYaw = class_3532.method_16439(0.065F, this.moveYaw, this.moveYawSet);
         this.speed /= 1.005F;
         float motionX = -class_3532.method_15374((double)((float)Math.toRadians((double)this.moveYaw))) * this.speed;
         float motionZ = class_3532.method_15362((double)((float)Math.toRadians((double)this.moveYaw))) * this.speed;
         this.prevPos = this.pos;
         float scaleBox = 0.1F;
         boolean colliding = !IMinecraft.mc
            .field_1687
            .method_18026(
               new class_238(
                  this.pos.field_1352 - (double)(scaleBox / 2.0F),
                  this.pos.field_1351,
                  this.pos.field_1350 - (double)(scaleBox / 2.0F),
                  this.pos.field_1352 + (double)(scaleBox / 2.0F),
                  this.pos.field_1351 + (double)scaleBox,
                  this.pos.field_1350 + (double)(scaleBox / 2.0F)
               )
            );
         float delente = colliding ? 0.3F : 1.0F;
         this.yMotion /= 1.02F;
         this.pos = this.pos.method_1031((double)(motionX / delente), (double)(this.yMotion / delente), (double)(motionZ / delente));
         if (this.getTimePC() >= 1.0F) {
            this.alphaTarget = 0.0F;
            if (this.getAlphaPC() < 0.004F) {
               this.setToRemove();
            }
         }

         this.trailParts.add(new FireFlies.TrailPart(this, 500));
         this.trailParts.removeIf(FireFlies.TrailPart::toRemove);

         for (int i = 0; i < 2; i++) {
            this.sparkParts.add(new FireFlies.SparkPart(this, 600));
         }

         this.sparkParts.forEach(FireFlies.SparkPart::motionSparkProcess);
         this.sparkParts.removeIf(FireFlies.SparkPart::toRemove);
      }

      void setToRemove() {
         this.toRemove = true;
      }

      boolean isToRemove() {
         return this.toRemove;
      }

      int getMsChangeSideRate() {
         return (int)FireFlies.getRandom(300.5, 900.5);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class SparkPart {
      double posX;
      double posY;
      double posZ;
      double prevPosX;
      double prevPosY;
      double prevPosZ;
      double speed = Math.random() / 50.0;
      final float radianYaw = (float)Math.random() * 360.0F;
      final float radianPitch = -45.0F + (float)Math.random() * 90.0F;
      final long startTime = System.currentTimeMillis();
      final int maxTime;

      SparkPart(FireFlies.FirePart part, int maxTime) {
         this.maxTime = maxTime;
         this.posX = part.pos.field_1352;
         this.posY = part.pos.field_1351;
         this.posZ = part.pos.field_1350;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
      }

      double timePC() {
         return (double)class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / (float)this.maxTime, 0.0F, 1.0F);
      }

      boolean toRemove() {
         return this.timePC() >= 1.0;
      }

      void motionSparkProcess() {
         float radYaw = (float)Math.toRadians((double)this.radianYaw);
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.posX = this.posX + (double)class_3532.method_15374((double)radYaw) * this.speed;
         this.posY = this.posY + (double)class_3532.method_15362((double)((float)Math.toRadians((double)(this.radianPitch - 90.0F)))) * this.speed;
         this.posZ = this.posZ + (double)class_3532.method_15362((double)radYaw) * this.speed;
         this.speed /= 1.2;
      }

      double getRenderPosX(float pTicks) {
         return this.prevPosX + (this.posX - this.prevPosX) * (double)pTicks;
      }

      double getRenderPosY(float pTicks) {
         return this.prevPosY + (this.posY - this.prevPosY) * (double)pTicks;
      }

      double getRenderPosZ(float pTicks) {
         return this.prevPosZ + (this.posZ - this.prevPosZ) * (double)pTicks;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class TrailPart {
      final double x;
      final double y;
      final double z;
      final long startTime = System.currentTimeMillis();
      final int maxTime;

      TrailPart(FireFlies.FirePart part, int maxTime) {
         this.maxTime = maxTime;
         this.x = part.pos.field_1352;
         this.y = part.pos.field_1351;
         this.z = part.pos.field_1350;
      }

      boolean toRemove() {
         return System.currentTimeMillis() - this.startTime >= (long)this.maxTime;
      }
   }
}
