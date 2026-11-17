package ru.mentions.module.impl.display;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Click Gui",
   category = Category.OTHER,
   key = 344,
   desc = "Открывает меню настроек клиента. Нельзя отключить.",
   autoEnabled = true,
   allowDisable = false
)
@Environment(EnvType.CLIENT)
public class ClickGui extends Module {
   public static final int DEFAULT_KEY = 344;
   public final SliderSetting size = new SliderSetting(this, "Размер", 1.0F, 0.75F, 1.5F, 0.05F);
   public final ModeSetting style = new ModeSetting(this, "Стиль меню", "1", "2");
   public final BindSetting openBind = new BindSetting(this, "Клавиша меню", 344);

   @EventHandler
   public void onKey(EventKey event) {
      if (event.getKey() == this.getKey()) {
         this.openMenu();
      }
   }

   @Override
   public void toggle() {
      this.openMenu();
   }

   @Override
   public int getKey() {
      BindSetting bind = this.openBind;
      if (bind == null) {
         return 344;
      } else {
         Integer value = bind.get();
         return value != null && value > 0 ? value : 344;
      }
   }

   @Override
   public void setKey(int key) {
      BindSetting bind = this.openBind;
      if (bind != null) {
         bind.set(key <= 0 ? -1 : key);
      }
   }

   public boolean isMenuKey(int key) {
      return key == this.getKey();
   }

   public void openMenu() {
      mc.execute(() -> {
         class_437 current = mc.field_1755;
         class_437 target = (class_437)(this.style.is("2") ? Client.get().menu2Screen() : Client.get().clickGuiScreen());
         mc.method_1507(current == target ? null : target);
      });
   }
}
