package ru.mentions.module.impl.display;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.config.ConfigManager;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.utils.math.ChatUtils;

@ModuleInfo(
   name = "Profiles",
   category = Category.OTHER,
   desc = "Переключение профилей (конфигов) клавишами.",
   autoEnabled = false,
   allowDisable = true
)
@Environment(EnvType.CLIENT)
public class Profiles extends Module {
   public final BindSetting nextProfile = new BindSetting(this, "След. профиль", -1);
   public final BindSetting prevProfile = new BindSetting(this, "Пред. профиль", -1);
   private int lastIndex = -1;

   @EventHandler
   public void onKey(EventKey event) {
      Integer next = this.nextProfile.get();
      Integer prev = this.prevProfile.get();
      int key = event.getKey();
      if (next != null && next > 0 && key == next) {
         this.cycle(1);
      } else if (prev != null && prev > 0 && key == prev) {
         this.cycle(-1);
      }
   }

   private void cycle(int direction) {
      List<String> profiles = new ArrayList<>(Client.get().configManager().listConfigs());
      profiles.removeIf(namex -> namex.equalsIgnoreCase("auto") || ConfigManager.isReservedSystemFile(namex));
      if (profiles.isEmpty()) {
         ChatUtils.addChatMessage("§7Нет сохранённых профилей. Создайте в меню или через .cfg save <имя>");
      } else {
         int index;
         if (this.lastIndex >= 0 && this.lastIndex < profiles.size()) {
            index = Math.floorMod(this.lastIndex + direction, profiles.size());
         } else {
            index = direction > 0 ? 0 : profiles.size() - 1;
         }

         String name = profiles.get(index);
         boolean ok = Client.get().configManager().load(name);
         if (ok) {
            this.lastIndex = index;
            ChatUtils.addChatMessage("Профиль §a" + name + "§r загружен §8[" + (index + 1) + "/" + profiles.size() + "]");
         } else {
            ChatUtils.addChatMessage("§cНе удалось загрузить профиль §r" + name);
         }
      }
   }
}
