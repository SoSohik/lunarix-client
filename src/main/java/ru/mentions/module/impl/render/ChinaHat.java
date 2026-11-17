package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "China Hat",
   desc = "Китайская шляпа",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ChinaHat extends Module {
   public static ChinaHat getInstance() {
      return Instance.get(ChinaHat.class);
   }
}
