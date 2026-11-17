package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_1299;
import net.minecraft.class_4587;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Poprygunchik",
   category = Category.RENDER,
   desc = "прыг скок"
)
@Environment(EnvType.CLIENT)
public class Poprygunchik extends Module {
   public SliderSetting amount = new SliderSetting(this, "Сила", 0.7F, 0.1F, 1.6F, 0.05F);
   public SliderSetting speed = new SliderSetting(this, "Скорость", 1.0F, 0.2F, 3.0F, 0.05F);
   public BooleanSetting players = new BooleanSetting(this, "Игроки", true);
   public BooleanSetting mobs = new BooleanSetting(this, "Мобы", true);

   public void applySquash(class_10017 state, class_4587 matrices) {
      if (this.isEnabled()) {
         if (state instanceof class_10042) {
            boolean isPlayer = state.field_58171 == class_1299.field_6097;
            if (isPlayer ? this.players.getValue() : this.mobs.getValue()) {
               float seconds = state.field_53328 / 20.0F;
               float wave = (float)Math.sin((double)(seconds * this.speed.getValue() * 6.28318F));
               float squash = this.amount.getValue() * 0.5F;
               float scaleY = 1.0F + wave * squash;
               float scaleXZ = 1.0F - wave * squash * 0.55F;
               matrices.method_22905(scaleXZ, scaleY, scaleXZ);
            }
         }
      }
   }
}
