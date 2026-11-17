package ru.mentions.module.impl.render;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.render.ColorGradePipeline;

@ModuleInfo(
   name = "Color Correction",
   desc = "Цветокоррекция, насыщенность, контраст и яркость",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ColorGrade extends Module {
   private static ColorGrade instance;
   public SliderSetting saturation = new SliderSetting(this, "Насыщенность", 1.2F, 0.0F, 2.0F, 0.05F);
   public SliderSetting brightness = new SliderSetting(this, "Яркость", 0.0F, -0.5F, 0.5F, 0.01F);
   public SliderSetting contrast = new SliderSetting(this, "Контраст", 1.05F, 0.5F, 1.5F, 0.05F);
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", new Color(80, 130, 255).getRGB());
   public SliderSetting intensity = new SliderSetting(this, "Сила тинта", 0.0F, 0.0F, 0.7F, 0.01F);
   public BooleanSetting vignetteOn = new BooleanSetting(this, "Виньетка", false);
   public SliderSetting vignetteStr = new SliderSetting(this, "Сила виньетки", 0.45F, 0.1F, 0.85F, 0.05F).setVisible(() -> this.vignetteOn.getValue());
   private final ColorGradePipeline pipeline = new ColorGradePipeline();

   public ColorGrade() {
      instance = this;
   }

   public static void renderScene() {
      ColorGrade module = instance;
      if (module != null) {
         if (module.isEnabled() && mc.field_1724 != null && mc.field_1687 != null) {
            module.pipeline
               .draw(
                  module.saturation.getValue(),
                  module.brightness.getValue(),
                  module.contrast.getValue(),
                  module.tintColor.getValue(),
                  module.intensity.getValue(),
                  module.vignetteOn.getValue() ? module.vignetteStr.getValue() : 0.0F
               );
         } else {
            module.pipeline.close();
         }
      }
   }

   public static void closeRenderer() {
      if (instance != null) {
         instance.pipeline.close();
      }
   }

   @Override
   protected void onDisable() {
      this.pipeline.close();
   }
}
