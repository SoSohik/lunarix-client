package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.Collections;
import java.util.List;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3965;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_239.class_240;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.render.GlassBlockRenderer;

@ModuleInfo(
   name = "Glass Block",
   category = Category.RENDER,
   desc = "Делает наведенный блок стеклянным"
)
@Environment(EnvType.CLIENT)
public class GlassBlock extends Module {
   private static GlassBlock instance;
   public BooleanSetting enableBlur = new BooleanSetting(this, "Блюр", true);
   public SliderSetting blurRadius = new SliderSetting(this, "Сила размытия", 2.5F, 1.0F, 5.0F, 0.1F).setVisible(() -> this.enableBlur.getValue());
   public SliderSetting blurIterations = new SliderSetting(this, "Качество", 3.0F, 1.0F, 5.0F, 1.0F).setVisible(() -> this.enableBlur.getValue());
   public SliderSetting saturation = new SliderSetting(this, "Насыщенность", 0.0F, 0.0F, 2.0F, 0.1F).setVisible(() -> this.enableBlur.getValue());
   public BooleanSetting enableTint = new BooleanSetting(this, "Оттенок", false);
   public SliderSetting tintIntensity = new SliderSetting(this, "Сила оттенка", 0.2F, 0.0F, 0.5F, 0.01F)
      .setVisible(() -> this.enableBlur.getValue() && this.enableTint.getValue());
   public ColorSetting tintColor = new ColorSetting(this, "Цвет оттенка", -16711681).setVisible(() -> this.enableTint.getValue());
   public BooleanSetting enableEdgeGlow = new BooleanSetting(this, "Свечение краёв", true);
   public SliderSetting edgeGlowIntensity = new SliderSetting(this, "Сила свечения", 0.2F, 0.0F, 1.0F, 0.01F).setVisible(() -> this.enableEdgeGlow.getValue());
   public BooleanSetting shimmer = new BooleanSetting(this, "Шиммер", true).setVisible(() -> this.enableEdgeGlow.getValue());
   public SliderSetting shimmerWidth = new SliderSetting(this, "Ширина шиммера", 0.04F, 0.01F, 0.15F, 0.01F)
      .setVisible(() -> this.enableEdgeGlow.getValue() && this.shimmer.getValue());
   public SliderSetting shimmerPeriod = new SliderSetting(this, "Период шиммера", 5.0F, 1.0F, 15.0F, 0.5F)
      .setVisible(() -> this.enableEdgeGlow.getValue() && this.shimmer.getValue());
   public SliderSetting moveSpeed = new SliderSetting(this, "Скорость перемещения", 0.18F, 0.05F, 0.6F, 0.01F);
   private final class_9799 allocator = new class_9799(16384);
   private final Animation moveAnimation = new Animation();
   private final Animation alphaAnimation = new Animation();
   private class_2338 targetPos;
   private class_2338 lastTargetPos;
   private class_243 fromOrigin = class_243.field_1353;
   private class_243 toOrigin = class_243.field_1353;
   private List<class_238> targetBoxes = Collections.emptyList();
   private static final RenderPipeline MASK_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "glass_block_mask"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final class_1921 MASK_LAYER = class_1921.method_75940(
      "glass_block_mask", class_12247.method_75927(MASK_PIPELINE).method_75929(4096).method_75938()
   );

   public GlassBlock() {
      instance = this;
   }

   public static GlassBlock getInstance() {
      return instance;
   }

   @Override
   protected void onEnable() {
      this.targetPos = null;
      this.lastTargetPos = null;
      this.fromOrigin = class_243.field_1353;
      this.toOrigin = class_243.field_1353;
      this.moveAnimation.set(1.0);
      this.alphaAnimation.set(0.0);
      GlassBlockRenderer renderer = GlassBlockRenderer.getInstance();
      renderer.invalidate();
      renderer.setEnabled(true);
      this.updateRendererSettings();
   }

   @Override
   protected void onDisable() {
      GlassBlockRenderer.getInstance().setEnabled(false);
      this.targetPos = null;
      this.lastTargetPos = null;
      this.targetBoxes = Collections.emptyList();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      if (this.isEnabled()) {
         this.targetPos = null;
         this.lastTargetPos = null;
         this.targetBoxes = Collections.emptyList();
         GlassBlockRenderer renderer = GlassBlockRenderer.getInstance();
         renderer.invalidate();
         renderer.setEnabled(true);
         this.updateRendererSettings();
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D event) {
      if (this.isEnabled() && mc.field_1687 != null && mc.field_1724 != null) {
         this.updateTarget();
         this.moveAnimation.update();
         this.alphaAnimation.update();
         if (!(this.alphaAnimation.get() <= 0.01F) && !this.targetBoxes.isEmpty()) {
            GlassBlockRenderer renderer = GlassBlockRenderer.getInstance();
            this.updateRendererSettings();
            if (renderer.captureSceneBeforeBlock()) {
               this.renderMask(event, this.getAnimatedOrigin(), this.alphaAnimation.get());
               renderer.captureSceneAfterBlock();
               renderer.renderGlassEffect();
            }
         }
      }
   }

   private void updateTarget() {
      class_2338 newTarget = this.getLookedBlock();
      if (newTarget != null) {
         class_2680 state = mc.field_1687.method_8320(newTarget);
         class_265 shape = state.method_26218(mc.field_1687, newTarget);
         if (shape.method_1110()) {
            shape = state.method_26220(mc.field_1687, newTarget);
         }

         this.targetBoxes = shape.method_1110() ? Collections.singletonList(new class_238(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)) : shape.method_1090();
      }

      if (newTarget == null) {
         this.targetPos = null;
         this.alphaAnimation.run(0.0, (double)this.moveSpeed.getValue().floatValue(), Easings.QUAD_OUT, true);
      } else {
         if (!newTarget.equals(this.targetPos)) {
            this.fromOrigin = this.lastTargetPos == null ? class_243.method_24954(newTarget) : this.getAnimatedOrigin();
            this.toOrigin = class_243.method_24954(newTarget);
            this.targetPos = newTarget;
            this.lastTargetPos = newTarget;
            this.moveAnimation.set(0.0);
            this.moveAnimation.run(1.0, (double)this.moveSpeed.getValue().floatValue(), Easings.QUAD_OUT);
         }

         this.alphaAnimation.run(1.0, (double)(this.moveSpeed.getValue() * 0.75F), Easings.QUAD_OUT, true);
      }
   }

   private class_2338 getLookedBlock() {
      class_239 hitResult = mc.field_1765;
      if (hitResult instanceof class_3965 blockHit && hitResult.method_17783() == class_240.field_1332) {
         class_2338 pos = blockHit.method_17777();
         if (mc.field_1687 != null && !mc.field_1687.method_8320(pos).method_26215()) {
            return pos;
         }

         return null;
      }

      return null;
   }

   private class_243 getAnimatedOrigin() {
      float t = this.moveAnimation.get();
      return new class_243(
         this.fromOrigin.field_1352 + (this.toOrigin.field_1352 - this.fromOrigin.field_1352) * (double)t,
         this.fromOrigin.field_1351 + (this.toOrigin.field_1351 - this.fromOrigin.field_1351) * (double)t,
         this.fromOrigin.field_1350 + (this.toOrigin.field_1350 - this.fromOrigin.field_1350) * (double)t
      );
   }

   private void renderMask(EventRender3D event, class_243 origin, float alpha) {
      class_4598 immediate = class_4597.method_22991(this.allocator);
      class_4588 buffer = immediate.method_73477(MASK_LAYER);
      Matrix4f matrix = event.getMatrixStack().method_23760().method_23761();
      class_243 cam = mc.field_1773.method_19418().method_71156();
      int color = (int)(255.0F * alpha) << 24 | 16777215;

      for (class_238 box : this.targetBoxes) {
         double x0 = origin.field_1352 + box.field_1323 - cam.field_1352;
         double y0 = origin.field_1351 + box.field_1322 - cam.field_1351;
         double z0 = origin.field_1350 + box.field_1321 - cam.field_1350;
         double x1 = origin.field_1352 + box.field_1320 - cam.field_1352;
         double y1 = origin.field_1351 + box.field_1325 - cam.field_1351;
         double z1 = origin.field_1350 + box.field_1324 - cam.field_1350;
         drawBox(buffer, matrix, x0, y0, z0, x1, y1, z1, color);
      }

      immediate.method_22993();
   }

   private void updateRendererSettings() {
      GlassBlockRenderer renderer = GlassBlockRenderer.getInstance();
      renderer.setBlurEnabled(this.enableBlur.getValue());
      renderer.setBlurRadius(this.blurRadius.getValue());
      renderer.setBlurIterations(this.blurIterations.getValue().intValue());
      renderer.setSaturation(this.saturation.getValue());
      renderer.setReflect(true);
      if (this.enableBlur.getValue() && this.enableTint.getValue()) {
         renderer.setTintColor(this.tintColor.getValue());
         renderer.setTintIntensity(this.tintIntensity.getValue());
      } else {
         renderer.setTintColor(0);
         renderer.setTintIntensity(0.0F);
      }

      renderer.setOutlineEnabled(this.enableEdgeGlow.getValue());
      if (this.enableEdgeGlow.getValue()) {
         float strength = this.edgeGlowIntensity.getValue() * 20.0F;
         renderer.setOutlineGlowStrength(strength);
         int outlineRgb = this.enableTint.getValue() ? this.tintColor.getValue() : -1;
         renderer.setOutlineColor(outlineRgb);
         renderer.setShimmerEnabled(this.shimmer.getValue());
         renderer.setShimmerWidth(this.shimmerWidth.getValue());
         renderer.setShimmerPeriodSec(this.shimmerPeriod.getValue());
      }
   }

   private static void drawBox(class_4588 buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color) {
      float x0 = (float)minX;
      float x1 = (float)maxX;
      float y0 = (float)minY;
      float y1 = (float)maxY;
      float z0 = (float)minZ;
      float z1 = (float)maxZ;
      buffer.method_22918(matrix, x0, y0, z0).method_39415(color);
      buffer.method_22918(matrix, x1, y0, z0).method_39415(color);
      buffer.method_22918(matrix, x1, y0, z1).method_39415(color);
      buffer.method_22918(matrix, x0, y0, z1).method_39415(color);
      buffer.method_22918(matrix, x0, y1, z0).method_39415(color);
      buffer.method_22918(matrix, x0, y1, z1).method_39415(color);
      buffer.method_22918(matrix, x1, y1, z1).method_39415(color);
      buffer.method_22918(matrix, x1, y1, z0).method_39415(color);
      buffer.method_22918(matrix, x0, y0, z1).method_39415(color);
      buffer.method_22918(matrix, x1, y0, z1).method_39415(color);
      buffer.method_22918(matrix, x1, y1, z1).method_39415(color);
      buffer.method_22918(matrix, x0, y1, z1).method_39415(color);
      buffer.method_22918(matrix, x1, y0, z0).method_39415(color);
      buffer.method_22918(matrix, x0, y0, z0).method_39415(color);
      buffer.method_22918(matrix, x0, y1, z0).method_39415(color);
      buffer.method_22918(matrix, x1, y1, z0).method_39415(color);
      buffer.method_22918(matrix, x0, y0, z0).method_39415(color);
      buffer.method_22918(matrix, x0, y0, z1).method_39415(color);
      buffer.method_22918(matrix, x0, y1, z1).method_39415(color);
      buffer.method_22918(matrix, x0, y1, z0).method_39415(color);
      buffer.method_22918(matrix, x1, y0, z1).method_39415(color);
      buffer.method_22918(matrix, x1, y0, z0).method_39415(color);
      buffer.method_22918(matrix, x1, y1, z0).method_39415(color);
      buffer.method_22918(matrix, x1, y1, z1).method_39415(color);
   }

   @Generated
   public BooleanSetting getEnableBlur() {
      return this.enableBlur;
   }

   @Generated
   public SliderSetting getBlurRadius() {
      return this.blurRadius;
   }

   @Generated
   public SliderSetting getBlurIterations() {
      return this.blurIterations;
   }

   @Generated
   public SliderSetting getSaturation() {
      return this.saturation;
   }

   @Generated
   public BooleanSetting getEnableTint() {
      return this.enableTint;
   }

   @Generated
   public SliderSetting getTintIntensity() {
      return this.tintIntensity;
   }

   @Generated
   public ColorSetting getTintColor() {
      return this.tintColor;
   }

   @Generated
   public BooleanSetting getEnableEdgeGlow() {
      return this.enableEdgeGlow;
   }

   @Generated
   public SliderSetting getEdgeGlowIntensity() {
      return this.edgeGlowIntensity;
   }

   @Generated
   public BooleanSetting getShimmer() {
      return this.shimmer;
   }

   @Generated
   public SliderSetting getShimmerWidth() {
      return this.shimmerWidth;
   }

   @Generated
   public SliderSetting getShimmerPeriod() {
      return this.shimmerPeriod;
   }

   @Generated
   public SliderSetting getMoveSpeed() {
      return this.moveSpeed;
   }

   @Generated
   public class_9799 getAllocator() {
      return this.allocator;
   }

   @Generated
   public Animation getMoveAnimation() {
      return this.moveAnimation;
   }

   @Generated
   public Animation getAlphaAnimation() {
      return this.alphaAnimation;
   }

   @Generated
   public class_2338 getTargetPos() {
      return this.targetPos;
   }

   @Generated
   public class_2338 getLastTargetPos() {
      return this.lastTargetPos;
   }

   @Generated
   public class_243 getFromOrigin() {
      return this.fromOrigin;
   }

   @Generated
   public class_243 getToOrigin() {
      return this.toOrigin;
   }

   @Generated
   public List<class_238> getTargetBoxes() {
      return this.targetBoxes;
   }
}
