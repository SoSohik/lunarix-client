package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Item Scroller",
   desc = "Помогает скроллить предметы",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class ItemScroller extends Module {
   public SliderSetting delay = new SliderSetting(this, "Задержка", 50.0F, 0.0F, 100.0F, 1.0F);

   public static ItemScroller getInstance() {
      return Instance.get(ItemScroller.class);
   }
}
