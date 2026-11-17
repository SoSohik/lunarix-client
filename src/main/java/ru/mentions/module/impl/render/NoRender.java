package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "No Render",
   desc = "Убирает всякую хуйню на твоем ебале.",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class NoRender extends Module {
   public BooleanSetting ignoreFire = new BooleanSetting(this, "Убирать огонь", true);
   public BooleanSetting ignoreLava = new BooleanSetting(this, "Убирать туман лавы", true);
   public BooleanSetting ignoreZalupa = new BooleanSetting(this, "Убирать плохие эффекты", true);
   public BooleanSetting ignoreScoreboard = new BooleanSetting(this, "Убирать Скорборт", false);
   public BooleanSetting ignoreBossBar = new BooleanSetting(this, "Убирать Босс бар", false);
   public BooleanSetting noCameraClip = new BooleanSetting(this, "Камера сквозь блоки", false);
   public BooleanSetting ignoreTotemPop = new BooleanSetting(this, "Убирать тотем на экране", true);
   public BooleanSetting removeCamreZalupa = new BooleanSetting(this, "Убирать дерганее камеры", true);

   public static NoRender getInstance() {
      return Instance.get(NoRender.class);
   }
}
