package ru.mentions.utils.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.utils.notification.NotificationManager;

@Environment(EnvType.CLIENT)
public class NotificationSound {
   private static long lastPlayTime = 0L;
   public static final String[] SOUND_MODES = new String[]{
      "По умолчанию",
      "Bell",
      "Trevoga",
      "Modern",
      "Bubble",
      "ApplePay",
      "Celestial",
      "Zenith",
      "Classic",
      "Soft",
      "Cyber",
      "Skeet",
      "DingDong",
      "Laser",
      "WexSide",
      "Spek",
      "Metallic",
      "Crime",
      "Tone",
      "Pop",
      "Выкл"
   };

   public static void playPreview(String soundMode) {
      playSoundForMode(soundMode, true);
   }

   public static void playToggle(boolean enabled) {
      if (InterFace.getInstance() != null && InterFace.getInstance().typeNotify != null) {
         long now = System.currentTimeMillis();
         if (now - lastPlayTime >= 35L) {
            lastPlayTime = now;
            String mode = InterFace.getInstance().typeNotify.getValue();
            playSoundForMode(mode, enabled);
         }
      }
   }

   public static void playNotification(NotificationManager.Type type) {
      if (InterFace.getInstance() != null) {
         float volume = InterFace.getInstance().volume.getValue();
         long now = System.currentTimeMillis();
         if (now - lastPlayTime >= 45L) {
            lastPlayTime = now;
            if (type == NotificationManager.Type.WARNING) {
               SoundUtil.playSound_wav("trevoga", volume * 0.9F);
            } else if (type == NotificationManager.Type.EFFECT) {
               SoundUtil.playSound_wav("EFFECT_END", volume * 0.85F);
            } else if (type == NotificationManager.Type.INFO) {
               SoundUtil.playSound_wav("infor", volume * 0.85F);
            } else {
               String mode = InterFace.getInstance().typeNotify != null ? InterFace.getInstance().typeNotify.getValue() : "Tone";
               playSoundForMode(mode, true);
            }
         }
      }
   }

   public static void playSoundForMode(String mode, boolean enabled) {
      if (InterFace.getInstance() != null && mode != null) {
         if (!mode.equalsIgnoreCase("Выкл") && !mode.equalsIgnoreCase("Off")) {
            if (mode.equalsIgnoreCase("По умолчанию") || mode.equalsIgnoreCase("Default")) {
               mode = InterFace.getInstance().typeNotify != null ? InterFace.getInstance().typeNotify.getValue() : "Tone";
            }

            float volume = InterFace.getInstance().volume.getValue();
            switch (mode) {
               case "Trevoga":
                  SoundUtil.playSound_wav("trevoga", volume * 0.9F);
                  break;
               case "Notify":
               case "Второй":
                  SoundUtil.playSound_wav(enabled ? "notify_enable" : "notify_disable", volume);
                  break;
               case "Akrien":
               case "Третий":
                  SoundUtil.playSound_wav(enabled ? "akrien_enable" : "akrien_disable", volume);
                  break;
               case "Modern":
               case "Type 6":
               case "Четвертый":
                  SoundUtil.playSound_wav(enabled ? "notify_type6_enabled" : "notify_type6_disabled", volume);
                  break;
               case "Bubble":
                  SoundUtil.playSound_wav("bubble", volume, enabled ? 1.15F : 0.88F);
                  break;
               case "Bell":
                  SoundUtil.playSound_wav("bell", volume, enabled ? 1.08F : 0.92F);
                  break;
               case "ApplePay":
                  SoundUtil.playSound_wav("applepay", volume, enabled ? 1.05F : 0.88F);
                  break;
               case "Celestial":
                  SoundUtil.playSound_wav(enabled ? "celestial_on" : "celestial_off", volume);
                  break;
               case "Zenith":
                  SoundUtil.playSound_wav(enabled ? "enable4" : "disable4", volume);
                  break;
               case "Classic":
                  SoundUtil.playSound_wav(enabled ? "enable2" : "disable2", volume);
                  break;
               case "Soft":
               case "Пятый":
                  SoundUtil.playSound_wav(enabled ? "enable3" : "disable3", volume);
                  break;
               case "Cyber":
                  SoundUtil.playSound_wav(enabled ? "enable6" : "disable6", volume);
                  break;
               case "Skeet":
                  SoundUtil.playSound_wav(enabled ? "skeet" : "skeet_off", volume);
                  break;
               case "DingDong":
                  SoundUtil.playSound_wav("dingdong", volume, enabled ? 1.0F : 0.82F);
                  break;
               case "Laser":
                  SoundUtil.playSound_wav(enabled ? "laser_on" : "laser_off", volume);
                  break;
               case "WexSide":
                  SoundUtil.playSound_wav(enabled ? "MODULE_ON" : "MODULE_OFF", volume);
                  break;
               case "Spek":
                  SoundUtil.playSound_wav("spek", volume, enabled ? 1.1F : 0.85F);
                  break;
               case "Metallic":
                  SoundUtil.playSound_wav("metallic", volume, enabled ? 1.08F : 0.88F);
                  break;
               case "Crime":
                  SoundUtil.playSound_wav("crime", volume, enabled ? 1.0F : 0.85F);
                  break;
               case "Pop":
                  SoundUtil.playSound_wav("POP_TOTEM", volume * 0.7F, enabled ? 1.1F : 0.85F);
                  break;
               case "Tone":
               case "Первый":
               default:
                  SoundUtil.playSound_wav(enabled ? "tone_enable" : "tone_disable", volume);
            }
         }
      }
   }
}
