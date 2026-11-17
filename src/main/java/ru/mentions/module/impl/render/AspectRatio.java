package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Aspect Ratio",
   desc = "Изменение соотношения сторон экрана",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class AspectRatio extends Module {
   private static AspectRatio INSTANCE;
   public ModeSetting mode = new ModeSetting(this, "Режим", "4:3", "16:9", "16:10", "1:1", "5:4", "21:9", "Кастом");
   public SliderSetting customRatio = new SliderSetting(this, "Пользовательское", 1.33F, 0.5F, 2.5F, 0.01F).setVisible(() -> this.mode.is("Кастом"));

   public AspectRatio() {
      INSTANCE = this;
   }

   public static AspectRatio getInstance() {
      return INSTANCE;
   }

   public float getAspectRatio() {
      if (!this.isEnabled()) {
         return 0.0F;
      } else {
         String m = this.mode.getValue();

         return switch (m) {
            case "4:3" -> 1.3333334F;
            case "16:9" -> 1.7777778F;
            case "16:10" -> 1.6F;
            case "1:1" -> 1.0F;
            case "5:4" -> 1.25F;
            case "21:9" -> 2.3333333F;
            case "Кастом" -> this.customRatio.getValue();
            default -> 1.7777778F;
         };
      }
   }
}
