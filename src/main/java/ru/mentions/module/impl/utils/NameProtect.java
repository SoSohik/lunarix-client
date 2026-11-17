package ru.mentions.module.impl.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_640;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.TextFactoryEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.StringSetting;

@ModuleInfo(
   name = "Name Protect",
   category = Category.OTHER,
   desc = "Скрывает имя, друзей, других игроков и пароли на скриншотах."
)
@Environment(EnvType.CLIENT)
public class NameProtect extends Module {
   private static final Pattern PASSWORD = Pattern.compile("(?iu)((?:пароль|password|pass(?:word)?)\\s*[:=]\\s*)\\S+");
   private static final int REFRESH_TICKS = 20;
   public final BooleanSetting hideNick = new BooleanSetting(this, "Ховати свій нік", true);
   public final StringSetting customNick = new StringSetting(this, "Заміна свого ніка", "lunarix");
   public final BooleanSetting friends = new BooleanSetting(this, "Скрывать друзей", true);
   public final BooleanSetting anarhy = new BooleanSetting(this, "Анархию", false);
   public final BooleanSetting hideOthers = new BooleanSetting(this, "Ховати інших гравців", false);
   public final StringSetting othersNick = new StringSetting(this, "Заміна інших", "Игрок");
   public final BooleanSetting hidePassword = new BooleanSetting(this, "Ховати паролі", true);
   private final Set<String> others = new HashSet<>();
   private String othersPattern;
   private int refreshTimer;

   @Override
   protected void onEnable() {
      this.refreshOthers(true);
   }

   @Override
   protected void onDisable() {
      this.others.clear();
      this.othersPattern = null;
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (++this.refreshTimer >= 20) {
         this.refreshTimer = 0;
         this.refreshOthers(false);
      }
   }

   @EventHandler
   public void onEvent(TextFactoryEvent event) {
      if (this.hideNick.getValue()) {
         String self = this.selfName();
         if (!self.isEmpty()) {
            String replacement = this.customNick.getValue();
            if (replacement == null || replacement.isBlank()) {
               replacement = "lunarix";
            }

            event.replaceText(self, escapeReplacement(replacement));
         }
      }

      if (this.friends.getValue()) {
         for (String friend : Client.get().friendManager().getFriends()) {
            event.replaceText(friend, "Друг");
         }
      }

      if (this.hideOthers.getValue() && this.othersPattern != null && !this.others.isEmpty()) {
         String replacement = this.othersNick.getValue();
         if (replacement == null || replacement.isBlank()) {
            replacement = "Игрок";
         }

         event.replaceRegex(this.othersPattern, escapeReplacement(replacement));
      }

      if (this.hidePassword.getValue()) {
         event.replaceRegex(PASSWORD.pattern(), "$1****");
      }

      if (this.anarhy.getValue()) {
         event.replaceRegex("(?ui)Анархия-(?:(?:1\\d{3})|(?:[1-9]\\d{0,2})|2000)", "lunarix");
         event.replaceRegex("funtime", "Успешный проект");
         event.replaceRegex("FunTime.su", "Успешный проект");
         event.replaceRegex("Анархия", "HvH");
      }
   }

   private void refreshOthers(boolean force) {
      if (!this.hideOthers.getValue()) {
         if (!this.others.isEmpty()) {
            this.others.clear();
            this.othersPattern = null;
         }
      } else {
         Set<String> next = new HashSet<>();
         String self = this.selfName();
         if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
            for (class_640 entry : mc.field_1724.field_3944.method_2880()) {
               String name = entry.method_2966().name();
               if (name != null && name.length() >= 3 && name.length() <= 16 && !name.equalsIgnoreCase(self) && !Client.get().friendManager().isFriend(name)) {
                  next.add(name);
               }
            }
         }

         if (force || !next.equals(this.others)) {
            this.others.clear();
            this.others.addAll(next);
            this.rebuildPattern();
         }
      }
   }

   private void rebuildPattern() {
      if (this.others.isEmpty()) {
         this.othersPattern = null;
      } else {
         StringBuilder builder = new StringBuilder("(?ui)(?<![A-Za-z0-9_])(");
         boolean first = true;

         for (String name : this.others) {
            if (!first) {
               builder.append('|');
            }

            builder.append(Pattern.quote(name));
            first = false;
         }

         builder.append(")(?![A-Za-z0-9_])");
         this.othersPattern = builder.toString();
      }
   }

   private String selfName() {
      try {
         return mc.method_1548().method_1676();
      } catch (Throwable var2) {
         return "";
      }
   }

   private static String escapeReplacement(String value) {
      return value.replace("\\", "\\\\").replace("$", "\\$");
   }
}
