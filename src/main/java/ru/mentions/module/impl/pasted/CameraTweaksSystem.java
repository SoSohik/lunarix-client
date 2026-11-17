package ru.mentions.module.impl.pasted;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Camera Tweaks",
   desc = "Pasted:systemdlc | Дистанция камеры от 3-го лица и проход сквозь блоки",
   category = Category.PASTED
)
@Environment(EnvType.CLIENT)
public class CameraTweaksSystem extends Module {
   public final SliderSetting distance = new SliderSetting(this, "Дистанция", 6.0F, 1.0F, 20.0F, 0.5F);
   public final BooleanSetting clipThrough = new BooleanSetting(this, "Сквозь блоки", true);
   public final SliderSetting animationSpeed = new SliderSetting(this, "Плавность", 10.0F, 1.0F, 25.0F, 1.0F);
   private float currentDistance = 4.0F;

   public static CameraTweaksSystem getInstance() {
      return Instance.get(CameraTweaksSystem.class);
   }

   public float getDistance(float delta) {
      float target = this.distance.getValue();
      float factor = Math.clamp(this.animationSpeed.getValue() * 0.05F, 0.05F, 1.0F);
      this.currentDistance = this.currentDistance + (target - this.currentDistance) * factor;
      return this.currentDistance;
   }

   public float getTargetDistance() {
      return this.distance.getValue();
   }
}
