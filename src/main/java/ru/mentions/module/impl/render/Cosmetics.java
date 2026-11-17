package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Cosmetics",
   desc = "Косметика на своём игроке: крылья, плащи, шляпы, рюкзаки, питомцы. Выбор делается в окне косметики.",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Cosmetics extends Module {
   public ModeSetting audience = new ModeSetting(this, "Показывать", "Только себе", "Всем игрокам");
   public BooleanSetting animations = new BooleanSetting(this, "Анимации", true);
   public BooleanSetting unshaded = new BooleanSetting(this, "Без освещения", false);
   public SliderSetting globalScale = new SliderSetting(this, "Общий масштаб", 1.0F, 0.4F, 2.0F, 0.05F);
   public SliderSetting capeWidth = new SliderSetting(this, "Плащ: ширина", 0.62F, 0.2F, 1.4F, 0.01F);
   public SliderSetting capeLength = new SliderSetting(this, "Плащ: длина", 1.15F, 0.3F, 2.4F, 0.01F);
   public SliderSetting capeTop = new SliderSetting(this, "Плащ: высота", 0.42F, -0.4F, 1.4F, 0.01F);
   public SliderSetting capeDepth = new SliderSetting(this, "Плащ: отступ", 0.16F, 0.0F, 0.7F, 0.01F);
   public SliderSetting capeBend = new SliderSetting(this, "Плащ: изгиб", 0.1F, 0.0F, 0.6F, 0.01F);
   public SliderSetting capeWave = new SliderSetting(this, "Плащ: развев", 0.055F, 0.0F, 0.35F, 0.005F);
   public SliderSetting capeWaveSpeed = new SliderSetting(this, "Плащ: скорость", 2.4F, 0.0F, 9.0F, 0.1F);
   public SliderSetting capeHem = new SliderSetting(this, "Плащ: низ", 1.0F, 0.3F, 1.5F, 0.01F);
}
