package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Item Physics",
   category = Category.RENDER,
   desc = "Лежачие 3D-предметы с вращением как на земле"
)
@Environment(EnvType.CLIENT)
public class ItemPhysics extends Module {
   public static ItemPhysics INSTANCE;
   public SliderSetting rotationSpeed = new SliderSetting(this, "Скорость вращения", 1.0F, 0.0F, 4.0F, 0.1F);
   public SliderSetting scale = new SliderSetting(this, "Масштаб", 1.0F, 0.3F, 2.0F, 0.1F);
   public BooleanSetting layFlat = new BooleanSetting(this, "Класть на землю", true);
   public BooleanSetting spinOnGround = new BooleanSetting(this, "Вращать на земле", true);
   public BooleanSetting blockTilt = new BooleanSetting(this, "Наклон блоков", false);

   public ItemPhysics() {
      INSTANCE = this;
   }
}
