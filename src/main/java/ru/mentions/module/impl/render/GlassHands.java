package ru.mentions.module.impl.render;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.GlassHandsRenderEvent;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.GlassHandsRenderer;

@ModuleInfo(
   name = "Glass Hands",
   category = Category.RENDER,
   desc = "Делает руки и предметы стеклянными"
)
@Environment(EnvType.CLIENT)
public class GlassHands extends Module {
   private static GlassHands instance;
   public BooleanSetting enableBlur = new BooleanSetting(this, "Блюр", true);
   public SliderSetting blurRadius = new SliderSetting(this, "Сила размытия", 2.5F, 1.0F, 5.0F, 0.1F).setVisible(() -> this.enableBlur.getValue());
   public SliderSetting blurIterations = new SliderSetting(this, "Качество", 3.0F, 1.0F, 5.0F, 1.0F).setVisible(() -> this.enableBlur.getValue());
   public SliderSetting saturation = new SliderSetting(this, "Насыщенность", 0.0F, 0.0F, 2.0F, 0.1F).setVisible(() -> this.enableBlur.getValue());
   public SliderSetting tintIntensity = new SliderSetting(this, "Сила оттенка", 0.2F, 0.0F, 1.0F, 0.01F).setVisible(() -> this.enableBlur.getValue());
   public SliderSetting edgeSoftness = new SliderSetting(this, "Сглаживание краёв", 0.0F, 0.0F, 28.0F, 1.0F).setVisible(() -> this.enableBlur.getValue());
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -16711681).setVisible(() -> this.typeColor.is("Свой"));
   public BooleanSetting enableIce = new BooleanSetting(this, "Искажение", false);
   public SliderSetting iceIntensity = new SliderSetting(this, "Сила искажения", 1.0F, 0.0F, 2.0F, 0.05F).setVisible(() -> this.enableIce.getValue());
   public SliderSetting refractReach = new SliderSetting(this, "Ширина искажения", 25.0F, 0.0F, 80.0F, 1.0F).setVisible(() -> this.enableIce.getValue());
   public BooleanSetting enableFire = new BooleanSetting(this, "Огонь", false);
   public SliderSetting fireIntensity = new SliderSetting(this, "Сила огня", 1.18F, 0.2F, 2.0F, 0.02F).setVisible(() -> this.enableFire.getValue());
   public SliderSetting fireRadius = new SliderSetting(this, "Размер пламени", 15.0F, 4.0F, 40.0F, 1.0F).setVisible(() -> this.enableFire.getValue());
   public SliderSetting fireSpeed = new SliderSetting(this, "Скорость огня", 1.1F, 0.2F, 3.0F, 0.05F).setVisible(() -> this.enableFire.getValue());
   public SliderSetting fireHeight = new SliderSetting(this, "Высота дыма", 28.0F, 5.0F, 56.0F, 1.0F).setVisible(() -> this.enableFire.getValue());
   public SliderSetting fireTrail = new SliderSetting(this, "Сила дыма", 1.16F, 0.0F, 2.0F, 0.02F).setVisible(() -> this.enableFire.getValue());
   public ModeSetting fireColorMode = new ModeSetting(this, "Цвет огня", "Предмет", "Тема", "Свой").setVisible(() -> this.enableFire.getValue());
   public ColorSetting fireCustomColor = new ColorSetting(this, "Свой цвет огня", -32730)
      .setVisible(() -> this.enableFire.getValue() && this.fireColorMode.is("Свой"));
   public BooleanSetting fireBurnThrough = new BooleanSetting(this, "Прожиг", false).setVisible(() -> this.enableFire.getValue());
   public BooleanSetting enableEdgeGlow = new BooleanSetting(this, "Свечение краёв", true);
   public SliderSetting edgeGlowIntensity = new SliderSetting(this, "Сила свечения", 0.2F, 0.0F, 1.0F, 0.01F).setVisible(() -> this.enableEdgeGlow.getValue());
   public ModeSetting edgeGlowColorMode = new ModeSetting(this, "Цвет свечения", "Тема", "Свой").setVisible(() -> this.enableEdgeGlow.getValue());
   public ColorSetting edgeGlowCustomColor = new ColorSetting(this, "Свой цвет свечения", -16711681)
      .setVisible(() -> this.enableEdgeGlow.getValue() && this.edgeGlowColorMode.is("Свой"));
   public BooleanSetting shimmer = new BooleanSetting(this, "Шиммер", true).setVisible(() -> this.enableEdgeGlow.getValue());
   public SliderSetting shimmerWidth = new SliderSetting(this, "Ширина шиммера", 0.04F, 0.01F, 0.15F, 0.01F)
      .setVisible(() -> this.enableEdgeGlow.getValue() && this.shimmer.getValue());
   public SliderSetting shimmerPeriod = new SliderSetting(this, "Период шиммера", 5.0F, 1.0F, 15.0F, 0.5F)
      .setVisible(() -> this.enableEdgeGlow.getValue() && this.shimmer.getValue());

   public int getColor() {
      return this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   public GlassHands() {
      instance = this;
   }

   public static GlassHands getInstance() {
      return instance;
   }

   @Override
   protected void onEnable() {
      GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
      renderer.invalidate();
      renderer.setEnabled(true);
      this.applyRendererSettings();
   }

   @Override
   protected void onDisable() {
      GlassHandsRenderer.getInstance().setEnabled(false);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      if (this.isEnabled()) {
         GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
         renderer.invalidate();
         renderer.setEnabled(true);
         this.applyRendererSettings();
      }
   }

   @EventHandler
   public void onGlassHandsRender(GlassHandsRenderEvent event) {
      if (this.isEnabled()) {
         GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
         this.applyRendererSettings();
         if (event.getPhase() == GlassHandsRenderEvent.Phase.PRE) {
            renderer.captureSceneBeforeHands();
         } else if (event.getPhase() == GlassHandsRenderEvent.Phase.POST) {
            renderer.captureSceneAfterHands();
            renderer.renderGlassEffect();
         }
      }
   }

   public void applyRendererSettings() {
      GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
      renderer.setBlurEnabled(this.enableBlur.getValue());
      renderer.setBlurRadius(this.blurRadius.getValue());
      renderer.setBlurIterations(this.blurIterations.getValue().intValue());
      renderer.setSaturation(this.saturation.getValue());
      renderer.setEdgeSoftness(this.edgeSoftness.getValue());
      if (this.enableIce.getValue()) {
         renderer.setReflect(true);
         renderer.setIceIntensity(this.iceIntensity.getValue());
         renderer.setFrostScale(this.refractReach.getValue());
         renderer.setCrackIntensity(0.5F);
      } else {
         renderer.setReflect(false);
         renderer.setIceIntensity(0.0F);
         renderer.setFrostScale(0.0F);
         renderer.setCrackIntensity(0.0F);
      }

      renderer.setSmokeAmount(0.0F);
      renderer.setFireEnabled(this.enableFire.getValue());
      if (this.enableFire.getValue()) {
         renderer.setFireStrength(this.fireIntensity.getValue());
         renderer.setFireRadius(this.fireRadius.getValue());
         renderer.setFireSpeed(this.fireSpeed.getValue());
         renderer.setFireHeight(this.fireHeight.getValue());
         renderer.setFireTrailStrength(this.fireTrail.getValue());
         renderer.setFireFillMode(this.fireBurnThrough.getValue());
         if (this.fireColorMode.is("Предмет")) {
            renderer.setFireColorMix(0.0F);
         } else if (this.fireColorMode.is("Тема")) {
            renderer.setFireColor(ColorUtil.getClientColor1(1) & 16777215);
            renderer.setFireColorMix(1.0F);
         } else {
            renderer.setFireColor(this.fireCustomColor.getValue() & 16777215);
            renderer.setFireColorMix(1.0F);
         }
      }

      if (this.enableBlur.getValue()) {
         renderer.setTintColor(this.getColor());
         renderer.setTintIntensity(this.tintIntensity.getValue());
      } else {
         renderer.setTintColor(0);
         renderer.setTintIntensity(0.0F);
      }

      renderer.setOutlineEnabled(this.enableEdgeGlow.getValue());
      if (this.enableEdgeGlow.getValue()) {
         float strength = this.edgeGlowIntensity.getValue() * 20.0F;
         renderer.setOutlineGlowStrength(strength);
         if (this.edgeGlowColorMode.is("Тема")) {
            renderer.setOutlineUseItemColor(false);
            renderer.setOutlineColor(0xFF000000 | ColorUtil.getClientColor1(1) & 16777215);
         } else {
            renderer.setOutlineUseItemColor(false);
            renderer.setOutlineColor(0xFF000000 | this.edgeGlowCustomColor.getValue() & 16777215);
         }

         renderer.setShimmerEnabled(this.shimmer.getValue());
         renderer.setShimmerWidth(this.shimmerWidth.getValue());
         renderer.setShimmerPeriodSec(this.shimmerPeriod.getValue());
      }
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
   public SliderSetting getTintIntensity() {
      return this.tintIntensity;
   }

   @Generated
   public SliderSetting getEdgeSoftness() {
      return this.edgeSoftness;
   }

   @Generated
   public ModeSetting getTypeColor() {
      return this.typeColor;
   }

   @Generated
   public ColorSetting getTintColor() {
      return this.tintColor;
   }

   @Generated
   public BooleanSetting getEnableIce() {
      return this.enableIce;
   }

   @Generated
   public SliderSetting getIceIntensity() {
      return this.iceIntensity;
   }

   @Generated
   public SliderSetting getRefractReach() {
      return this.refractReach;
   }

   @Generated
   public BooleanSetting getEnableFire() {
      return this.enableFire;
   }

   @Generated
   public SliderSetting getFireIntensity() {
      return this.fireIntensity;
   }

   @Generated
   public SliderSetting getFireRadius() {
      return this.fireRadius;
   }

   @Generated
   public SliderSetting getFireSpeed() {
      return this.fireSpeed;
   }

   @Generated
   public SliderSetting getFireHeight() {
      return this.fireHeight;
   }

   @Generated
   public SliderSetting getFireTrail() {
      return this.fireTrail;
   }

   @Generated
   public ModeSetting getFireColorMode() {
      return this.fireColorMode;
   }

   @Generated
   public ColorSetting getFireCustomColor() {
      return this.fireCustomColor;
   }

   @Generated
   public BooleanSetting getFireBurnThrough() {
      return this.fireBurnThrough;
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
   public ModeSetting getEdgeGlowColorMode() {
      return this.edgeGlowColorMode;
   }

   @Generated
   public ColorSetting getEdgeGlowCustomColor() {
      return this.edgeGlowCustomColor;
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
}
