package ru.mentions.module.impl.render;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4063;
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
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.ShaderSkyRenderer;

@ModuleInfo(
   name = "Shader Sky",
   category = Category.RENDER,
   desc = "шейдер неба"
)
@Environment(EnvType.CLIENT)
public class ShaderSky extends Module {
   private static ShaderSky instance;
   public ModeSetting mode = new ModeSetting(this, "Режим", "Aurora", "Night", "Snow", "Sky", "Star", "Glow", "Nebula");
   public SliderSetting intensity = new SliderSetting(this, "Сила", 1.0F, 0.1F, 1.0F, 0.05F);
   public SliderSetting speed = new SliderSetting(this, "Скорость", 1.0F, 0.1F, 3.0F, 0.1F);
   public SliderSetting scale = new SliderSetting(this, "Масштаб", 1.1F, 0.45F, 3.0F, 0.05F);
   public SliderSetting stars = new SliderSetting(this, "Звёзды", 0.75F, 0.0F, 1.0F, 0.05F);
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting color1 = new ColorSetting(this, "Цвет 1", -8788225).setVisible(() -> this.typeColor.is("Свой"));
   public ColorSetting color2 = new ColorSetting(this, "Цвет 2", -34074).setVisible(() -> this.typeColor.is("Свой"));
   public BooleanSetting vanillaSky = new BooleanSetting(this, "Ванильное небо", false);
   public BooleanSetting anchorToWorld = new BooleanSetting(this, "Привязка к миру", false);
   private static class_4063 cloudsBackup = null;

   public ShaderSky() {
      instance = this;
   }

   public static ShaderSky getInstance() {
      return instance;
   }

   public static int geteColor2() {
      return getInstance().typeColor.is("Тема") ? ColorUtil.multDark(ColorUtil.getClientColor1(1), 0.25F) : getInstance().color2.getValue();
   }

   public static int geteColor1() {
      return getInstance().typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : getInstance().color1.getValue();
   }

   @Override
   protected void onEnable() {
      this.hideVanillaClouds();
      ShaderSkyRenderer renderer = ShaderSkyRenderer.getInstance();
      renderer.invalidate();
      renderer.setEnabled(true);
   }

   private void hideVanillaClouds() {
      try {
         if (mc.field_1690 == null || mc.field_1690.method_42528() == null) {
            return;
         }

         cloudsBackup = (class_4063)mc.field_1690.method_42528().method_41753();
         mc.field_1690.method_42528().method_41748(class_4063.field_18162);
      } catch (Throwable var2) {
      }
   }

   private void restoreVanillaClouds() {
      try {
         if (cloudsBackup == null || mc.field_1690 == null || mc.field_1690.method_42528() == null) {
            return;
         }

         mc.field_1690.method_42528().method_41748(cloudsBackup);
         cloudsBackup = null;
      } catch (Throwable var2) {
      }
   }

   @Override
   protected void onDisable() {
      this.restoreVanillaClouds();
      ShaderSkyRenderer.getInstance().setEnabled(false);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      if (this.isEnabled()) {
         ShaderSkyRenderer renderer = ShaderSkyRenderer.getInstance();
         renderer.invalidate();
      }
   }

   @EventHandler
   public void onSkyTick(EventTick event) {
      if (this.isEnabled() && mc.field_1690 != null && mc.field_1690.method_42528() != null) {
         if (mc.field_1690.method_42528().method_41753() != class_4063.field_18162) {
            mc.field_1690.method_42528().method_41748(class_4063.field_18162);
         }
      }
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public SliderSetting getIntensity() {
      return this.intensity;
   }

   @Generated
   public SliderSetting getSpeed() {
      return this.speed;
   }

   @Generated
   public SliderSetting getScale() {
      return this.scale;
   }

   @Generated
   public SliderSetting getStars() {
      return this.stars;
   }

   @Generated
   public ModeSetting getTypeColor() {
      return this.typeColor;
   }

   @Generated
   public ColorSetting getColor1() {
      return this.color1;
   }

   @Generated
   public ColorSetting getColor2() {
      return this.color2;
   }

   @Generated
   public BooleanSetting getVanillaSky() {
      return this.vanillaSky;
   }

   @Generated
   public BooleanSetting getAnchorToWorld() {
      return this.anchorToWorld;
   }
}
