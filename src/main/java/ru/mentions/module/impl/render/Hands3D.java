package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.DelimiterSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "3D Hands",
   desc = "Трехмерные руки и предметы от первого лица",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Hands3D extends Module {
   public DelimiterSetting positionsTitle = new DelimiterSetting(this, "Позиция");
   public SliderSetting offsetX = new SliderSetting(this, "Смещение X", 0.0F, -1.0F, 1.0F, 0.01F);
   public SliderSetting offsetY = new SliderSetting(this, "Смещение Y", 0.0F, -1.0F, 1.0F, 0.01F);
   public SliderSetting offsetZ = new SliderSetting(this, "Смещение Z", 0.0F, -1.0F, 1.0F, 0.01F);
   public DelimiterSetting rotationTitle = new DelimiterSetting(this, "Поворот");
   public SliderSetting rotateX = new SliderSetting(this, "Поворот X", 0.0F, -180.0F, 180.0F, 1.0F);
   public SliderSetting rotateY = new SliderSetting(this, "Поворот Y", 0.0F, -180.0F, 180.0F, 1.0F);
   public SliderSetting rotateZ = new SliderSetting(this, "Поворот Z", 0.0F, -180.0F, 180.0F, 1.0F);
   public DelimiterSetting sizeTitle = new DelimiterSetting(this, "Размер");
   public SliderSetting scale = new SliderSetting(this, "Размер", 1.0F, 0.2F, 3.0F, 0.05F);
   public DelimiterSetting extraTitle = new DelimiterSetting(this, "Дополнительно");
   public BooleanSetting showArm = new BooleanSetting(this, "Показывать руку", true);
   public BooleanSetting vanillaSwing = new BooleanSetting(this, "Замах", true);
   public BooleanSetting bothHands = new BooleanSetting(this, "Вторая рука", true);

   public static Hands3D get() {
      return Instance.get(Hands3D.class);
   }
}
