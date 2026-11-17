package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.FogEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "World Tweaks",
   desc = "Мелкие настройки отображения мира",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class WorldTweaks extends Module {
   public BooleanSetting times = new BooleanSetting(this, "Менять время", true);
   public BooleanSetting fogs = new BooleanSetting(this, "Менять туман", true);
   public SliderSetting time = new SliderSetting(this, "Время", 12.0F, 0.0F, 24.0F, 1.0F).setVisible(() -> this.times.getValue());
   public SliderSetting fog = new SliderSetting(this, "Дистанция тумана", 100.0F, 2.0F, 200.0F, 1.0F).setVisible(() -> this.fogs.getValue());
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -16711681).setVisible(() -> this.typeColor.is("Свой"));

   public static WorldTweaks get() {
      return Instance.get(WorldTweaks.class);
   }

   public int getColor() {
      return this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   @EventHandler
   public void onFog(FogEvent e) {
      if (this.fogs.getValue()) {
         e.setDistance(this.fog.getValue());
         e.setColor(this.getColor());
         e.cancel();
      }
   }
}
