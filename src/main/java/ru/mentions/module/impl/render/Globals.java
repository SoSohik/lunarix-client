package ru.mentions.module.impl.render;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Globals",
   desc = "Отображает пользователей софта",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Globals extends Module {
   public final BooleanSetting badge = new BooleanSetting(this, "Значок софта", true);
   public final BooleanSetting notifyInChat = new BooleanSetting(this, "Уведомлять в чате", true);
   private static final Set<String> SOFT_USERS = Collections.synchronizedSet(new HashSet<>());
   public static final String SOFT_SIGNATURE = "\u200b\u200c\u200d";

   public static Globals getInstance() {
      return Instance.get(Globals.class);
   }

   public static boolean isSoftUser(class_1297 entity) {
      Globals globals = getInstance();
      if (globals == null || !globals.isEnabled()) {
         return false;
      } else if (!(entity instanceof class_1657 player)) {
         return false;
      } else if (mc.field_1724 != null && player.method_5667().equals(mc.field_1724.method_5667())) {
         return true;
      } else {
         String name = player.method_5477().getString();
         if (isSoftUser(name)) {
            return true;
         } else {
            String colored = player.method_5476().getString();
            return colored != null && (colored.toLowerCase(Locale.ROOT).contains("lunarix") || colored.toLowerCase(Locale.ROOT).contains("mentions"));
         }
      }
   }

   public static boolean isSoftUser(String name) {
      if (name == null || name.isEmpty()) {
         return false;
      } else {
         return mc.field_1724 != null && name.equalsIgnoreCase(mc.field_1724.method_5477().getString())
            ? true
            : SOFT_USERS.contains(name.toLowerCase(Locale.ROOT));
      }
   }

   public static boolean addSoftUser(String name) {
      if (name != null && !name.trim().isEmpty()) {
         boolean added = SOFT_USERS.add(name.trim().toLowerCase(Locale.ROOT));
         if (added) {
            Globals g = getInstance();
            if (g != null && g.isEnabled() && g.notifyInChat.getValue()) {
               ChatUtils.addChatMessage("§a[Globals] §rОбнаружен пользователь софта: §b" + name.trim());
            }
         }

         return added;
      } else {
         return false;
      }
   }

   public static boolean removeSoftUser(String name) {
      return name == null ? false : SOFT_USERS.remove(name.trim().toLowerCase(Locale.ROOT));
   }

   public static void clearSoftUsers() {
      SOFT_USERS.clear();
   }

   public static Set<String> getSoftUsers() {
      return Collections.unmodifiableSet(SOFT_USERS);
   }
}
