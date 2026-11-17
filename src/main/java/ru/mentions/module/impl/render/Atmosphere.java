package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.FogEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.render.WorldColorRenderer;
import ru.mentions.utils.render.winter.WinterSnowfall;

@ModuleInfo(
   name = "Atmosphere",
   desc = "Зимняя атмосфера: снег, метель и дымка мира",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public final class Atmosphere extends Module {
   private final SliderSetting radius = new SliderSetting(this, "Радиус", 24.0F, 8.0F, 64.0F, 1.0F);
   private final SliderSetting snowDensity = new SliderSetting(this, "Плотность снега", 1.25F, 0.25F, 3.0F, 0.05F);
   private final SliderSetting snowWind = new SliderSetting(this, "Ветер", 1.0F, 0.0F, 3.0F, 0.05F);
   private final SliderSetting snowFlakeSize = new SliderSetting(this, "Размер снежинки", 1.0F, 0.45F, 2.5F, 0.05F);
   private final SliderSetting snowFallSpeed = new SliderSetting(this, "Скорость падения", 1.0F, 0.35F, 2.5F, 0.05F);
   private final SliderSetting blizzard = new SliderSetting(this, "Метель", 0.35F, 0.0F, 1.0F, 0.05F);
   private final BooleanSetting groundSnow = new BooleanSetting(this, "Снег на земле", true);
   private final SliderSetting snowDepth = new SliderSetting(this, "Глубина снега", 0.10F, 0.03F, 0.30F, 0.01F)
         .setVisible(() -> this.groundSnow.getValue());
   private final BooleanSetting skyOnly = new BooleanSetting(this, "Только в небе", true);
   private final SliderSetting winterHaze = new SliderSetting(this, "Зимняя дымка", 0.72F, 0.0F, 1.0F, 0.05F);
   private final ColorSetting snowColor = new ColorSetting(this, "Цвет снега", (int) 0xFFEAF4FFL);

   private final WinterSnowfall winterSnowfall = WinterSnowfall.getInstance();
   private final WinterSnowfall.Config winterConfig = new WinterSnowfall.Config();

   @Override
   protected void onEnable() {
      this.winterSnowfall.reset();
   }

   @Override
   protected void onDisable() {
      this.winterSnowfall.reset();
      WorldColorRenderer.getInstance().close();
   }

   @EventHandler(priority = 100)
   public void onFog(FogEvent event) {
      float storm = this.blizzard.getValue();
      float haze = this.winterHaze.getValue();
      event.setDistance(Math.max(18.0F,
            this.radius.getValue() * (2.0F - haze * 0.55F - storm * 0.28F)));
      event.setColor(this.snowColor.getValue());
      event.cancel();
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 == null || mc.field_1687 == null) this.winterSnowfall.reset();
   }

   @EventHandler
   public void onRender3D(EventRender3D event) {
      class_310 client = class_310.method_1551();
      if (client == null || client.field_1773 == null || client.field_1724 == null) return;
      try {
         WorldColorRenderer.getInstance().render(
               this.snowColor.getValue(), 0.92f, 1.03f, 0.18f, 1.03f,
               1.0f, this.winterHaze.getValue(), this.radius.getValue());
      } catch (Throwable ignored) {
      }

      class_4184 camera = client.field_1773.method_19418();
      if (camera == null) return;
      this.winterConfig.radius = this.radius.getValue();
      this.winterConfig.height = Math.min(30.0f, this.radius.getValue());
      this.winterConfig.density = this.snowDensity.getValue();
      this.winterConfig.flakeSize = this.snowFlakeSize.getValue();
      this.winterConfig.fallSpeed = this.snowFallSpeed.getValue();
      this.winterConfig.wind = this.snowWind.getValue();
      this.winterConfig.opacity = 1.0f;
      this.winterConfig.blizzard = this.blizzard.getValue();
      this.winterConfig.skyOnly = this.skyOnly.getValue();
      this.winterConfig.groundSnow = this.groundSnow.getValue();
      this.winterConfig.groundRadius = class_3532.method_15363(this.radius.getValue() * 1.5f, 32.0f, 48.0f);
      this.winterConfig.groundDepth = this.snowDepth.getValue();
      this.winterConfig.color = this.snowColor.getValue();
      this.winterSnowfall.render(camera, this.winterConfig);
   }
}
