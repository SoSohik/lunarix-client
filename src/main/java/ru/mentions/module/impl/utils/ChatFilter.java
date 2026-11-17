package ru.mentions.module.impl.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import ru.mentions.Client;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Chat Filter",
   desc = "Фильтрует спам, мат и рекламу в чате",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class ChatFilter extends Module {
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Hide", "Censor", "NameOnly");
   public final BooleanSetting spam = new BooleanSetting(this, "Спам", true);
   public final BooleanSetting ads = new BooleanSetting(this, "Реклама", true);
   public final BooleanSetting profanity = new BooleanSetting(this, "Мат", true);
   public final BooleanSetting caps = new BooleanSetting(this, "Капс", true);
   public final BooleanSetting ignoreFriends = new BooleanSetting(this, "Пропускать друзей", true);
   private static final Pattern SPAM_PATTERN = Pattern.compile("(.)\\1{7,}");
   private static final Pattern ADS_PATTERN = Pattern.compile(
      "(?i)(discord\\.gg|\\.ru\\b|\\.com\\b|\\bshop\\b|\\bjoin\\b|магазин|продам|куплю|реклама|ип[:\\s]|\\bip[:\\s])"
   );
   private static final Pattern PROFANITY_PATTERN = Pattern.compile("(?i)\\b(сука|бля|блять|нахуй|пизд|хуй|хуя|ебат|ебал|fuck|shit|bitch)\\b");

   public static ChatFilter get() {
      return Instance.get(ChatFilter.class);
   }

   public static boolean shouldHide(class_2561 text) {
      ChatFilter self = get();
      if (self != null && self.isEnabled() && text != null) {
         String raw = text.getString();
         if (raw == null || raw.isEmpty()) {
            return false;
         } else if (self.ignoreFriends.getValue() && self.isFromFriend(raw)) {
            return false;
         } else {
            return !self.matches(raw) ? false : self.mode.is("Hide");
         }
      } else {
         return false;
      }
   }

   public static class_2561 transform(class_2561 text) {
      ChatFilter self = get();
      if (self != null && self.isEnabled() && text != null) {
         String raw = text.getString();
         if (raw == null || raw.isEmpty()) {
            return null;
         } else if (self.ignoreFriends.getValue() && self.isFromFriend(raw)) {
            return null;
         } else if (!self.matches(raw)) {
            return null;
         } else if (self.mode.is("Censor")) {
            return class_2561.method_43470(censor(raw)).method_10862(text.method_10866());
         } else if (self.mode.is("NameOnly")) {
            String name = extractName(raw);
            return name == null
               ? class_2561.method_43470("***").method_10862(text.method_10866())
               : class_2561.method_43470("<" + name + "> ***").method_10862(text.method_10866());
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean isFromFriend(String raw) {
      String name = extractName(raw);
      if (name == null) {
         return false;
      } else {
         try {
            return Client.get().friendManager().isFriend(name);
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   private static String extractName(String raw) {
      Matcher m = Pattern.compile("<([^>\\s]+)>").matcher(raw);
      if (m.find()) {
         return m.group(1);
      } else {
         m = Pattern.compile("^([A-Za-z0-9_]{3,16})").matcher(raw);
         return m.find() ? m.group(1) : null;
      }
   }

   private static String censor(String raw) {
      String out = PROFANITY_PATTERN.matcher(raw).replaceAll("***");
      out = ADS_PATTERN.matcher(out).replaceAll("***");
      return SPAM_PATTERN.matcher(out).replaceAll("***");
   }

   private boolean matches(String raw) {
      if (this.profanity.getValue() && PROFANITY_PATTERN.matcher(raw).find()) {
         return true;
      } else if (this.ads.getValue() && ADS_PATTERN.matcher(raw).find()) {
         return true;
      } else {
         return this.spam.getValue() && SPAM_PATTERN.matcher(raw).find() ? true : this.caps.getValue() && isMostlyCaps(raw);
      }
   }

   private static boolean isMostlyCaps(String raw) {
      int letters = 0;
      int upper = 0;

      for (int i = 0; i < raw.length(); i++) {
         char c = raw.charAt(i);
         if (Character.isLetter(c)) {
            letters++;
            if (Character.isUpperCase(c)) {
               upper++;
            }
         }
      }

      return letters >= 6 && (float)upper / (float)letters > 0.8F;
   }
}
