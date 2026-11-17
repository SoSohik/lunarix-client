package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "View Model",
   desc = "Настройка положения рук от первого лица",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ViewModel extends Module {
   public BooleanSetting mirror = new BooleanSetting(this, "Зеркалить", false);
   public SliderSetting rightX = new SliderSetting(this, "Правая X", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting rightY = new SliderSetting(this, "Правая Y", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting rightZ = new SliderSetting(this, "Правая Z", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting rightScale = new SliderSetting(this, "Правая масштаб", 1.0F, 0.0F, 3.0F, 0.05F);
   public SliderSetting leftX = new SliderSetting(this, "Левая X", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting leftY = new SliderSetting(this, "Левая Y", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting leftZ = new SliderSetting(this, "Левая Z", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting leftScale = new SliderSetting(this, "Левая масштаб", 1.0F, 0.0F, 3.0F, 0.05F);

   public static ViewModel getInstance() {
      return Instance.get(ViewModel.class);
   }

   public float rightOffsetX() {
      return this.rightX.getValue();
   }

   public float rightOffsetY() {
      return this.rightY.getValue();
   }

   public float rightOffsetZ() {
      return this.rightZ.getValue();
   }

   public float rightScale() {
      return this.rightScale.getValue();
   }

   public float leftOffsetX() {
      return this.mirror.getValue() ? -this.rightOffsetX() : this.leftX.getValue();
   }

   public float leftOffsetY() {
      return this.mirror.getValue() ? this.rightOffsetY() : this.leftY.getValue();
   }

   public float leftOffsetZ() {
      return this.mirror.getValue() ? this.rightOffsetZ() : this.leftZ.getValue();
   }

   public float leftScale() {
      return this.mirror.getValue() ? this.rightScale() : this.leftScale.getValue();
   }
}
