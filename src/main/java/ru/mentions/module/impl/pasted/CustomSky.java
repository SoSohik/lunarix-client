package ru.mentions.module.impl.pasted;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.render.ShaderSky;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.ShaderSkyRenderer;

@ModuleInfo(
   name = "CustomSky",
   category = Category.PASTED,
   desc = "Pasted:wild | Процедурное кастомное 3D небо в стиле Wild с эффектами Godrays, туманностями и звёздами"
)
@Environment(EnvType.CLIENT)
public class CustomSky extends Module {
   private static CustomSky instance;
   public ModeSetting mode = new ModeSetting(this, "Режим неба", "WildSky", "Deep Space", "Nebula", "Aurora", "Glow", "Purple Space");
   public BooleanSetting godrays = new BooleanSetting(this, "Солнечные лучи (Godrays)", true);
   public SliderSetting rayDensity = new SliderSetting(this, "Плотность лучей", 1.2F, 0.2F, 3.0F, 0.1F).setVisible(() -> this.godrays.getValue());
   public SliderSetting speed = new SliderSetting(this, "Скорость", 1.0F, 0.1F, 3.0F, 0.1F);
   public SliderSetting scale = new SliderSetting(this, "Масштаб", 1.15F, 0.45F, 3.0F, 0.05F);
   public SliderSetting stars = new SliderSetting(this, "Звёзды", 0.85F, 0.0F, 1.0F, 0.05F);
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой", "Космос");
   public ColorSetting color1 = new ColorSetting(this, "Цвет 1", -11695873).setVisible(() -> this.typeColor.is("Свой"));
   public ColorSetting color2 = new ColorSetting(this, "Цвет 2", -2145032).setVisible(() -> this.typeColor.is("Свой"));
   public BooleanSetting vanillaSky = new BooleanSetting(this, "Отключить ванильное небо", true);
   public BooleanSetting customClouds = new BooleanSetting(this, "Атмосферные облака", true);

   public CustomSky() {
      instance = this;
   }

   public static CustomSky getInstance() {
      return instance;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      ShaderSkyRenderer.getInstance().setEnabled(true);
   }

   @Override
   public void onDisable() {
      super.onDisable();
      ShaderSky ss = ShaderSky.getInstance();
      if (ss == null || !ss.isEnabled()) {
         ShaderSkyRenderer.getInstance().setEnabled(false);
      }
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (this.isEnabled()) {
         ShaderSkyRenderer.getInstance().setEnabled(true);
      }
   }

   public int getResolvedColor1() {
      if (this.typeColor.is("Свой")) {
         return this.color1.getValue();
      } else {
         return this.typeColor.is("Космос") ? -15066560 : ColorUtil.client();
      }
   }

   public int getResolvedColor2() {
      if (this.typeColor.is("Свой")) {
         return this.color2.getValue();
      } else {
         return this.typeColor.is("Космос") ? -8704833 : ColorUtil.multDark(ColorUtil.client(), 0.35F);
      }
   }
}
