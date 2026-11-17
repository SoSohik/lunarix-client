package ru.mentions.utils.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.other.NotificationSound;

@Environment(EnvType.CLIENT)
public class NotificationManager {
   private static final List<NotificationManager.Entry> entries = new ArrayList<>();
   private static final Map<String, Long> recent = new HashMap<>();
   private static final long DEDUPE_MS = 1500L;

   public static void send(String text) {
      add(text, NotificationManager.Type.INFO, null, null, null, 3000L);
   }

   public static void send(String text, NotificationManager.Type type) {
      add(text, type, null, null, null, 3000L);
   }

   public static void send(String text, NotificationManager.Type type, long ms) {
      add(text, type, null, null, null, ms);
   }

   public static void send(String text, NotificationManager.Type type, String glyph) {
      add(text, type, glyph, null, null, 3000L);
   }

   public static void send(String text, NotificationManager.Type type, String glyph, long ms) {
      add(text, type, glyph, null, null, ms);
   }

   public static void sendWithSound(String text, NotificationManager.Type type, String soundMode) {
      add(text, type, null, null, null, 3000L, soundMode);
   }

   public static void send(String text, NotificationManager.Type type, long ms, String soundMode) {
      add(text, type, null, null, null, ms, soundMode);
   }

   public static void send(String text, NotificationManager.Type type, class_1799 item, long ms, String soundMode) {
      add(text, type, null, null, item, ms, soundMode);
   }

   public static void send(String text, NotificationManager.Type type, class_2960 texture, long ms, String soundMode) {
      add(text, type, null, texture, null, ms, soundMode);
   }

   public static void send(String text, NotificationManager.Type type, class_2960 texture) {
      add(text, type, null, texture, null, 3000L);
   }

   public static void send(String text, NotificationManager.Type type, class_2960 texture, long ms) {
      add(text, type, null, texture, null, ms);
   }

   public static void send(String text, NotificationManager.Type type, class_1799 item) {
      add(text, type, null, null, item, 3000L);
   }

   public static void send(String text, NotificationManager.Type type, class_1799 item, long ms) {
      add(text, type, null, null, item, ms);
   }

   private static void add(String text, NotificationManager.Type type, String glyph, class_2960 texture, class_1799 item, long ms) {
      add(text, type, glyph, texture, item, ms, null);
   }

   private static void add(String text, NotificationManager.Type type, String glyph, class_2960 texture, class_1799 item, long ms, String soundMode) {
      long now = System.currentTimeMillis();
      List<NotificationManager.Entry> list = entries;
      synchronized (list) {
         Long last = recent.get(text);
         if (last != null && now - last < 1500L) {
            return;
         }

         if (recent.size() > 128) {
            recent.entrySet().removeIf(entry -> now - entry.getValue() >= 1500L);
         }

         recent.put(text, now);
         entries.add(new NotificationManager.Entry(text, type, glyph, texture, item, ms));
      }

      if (soundMode != null) {
         NotificationSound.playSoundForMode(soundMode, true);
      } else if (type != NotificationManager.Type.MODULE) {
         NotificationSound.playNotification(type);
      }
   }

   public static List<NotificationManager.Entry> entries() {
      return entries;
   }

   @Environment(EnvType.CLIENT)
   public static class Entry {
      public final String text;
      public final NotificationManager.Type type;
      public final Animation anim = new Animation();
      public boolean removing = false;
      public final long created = System.currentTimeMillis();
      public final long duration;
      public final String iconGlyph;
      public final class_2960 effectTexture;
      public final class_1799 itemStack;

      Entry(String text, NotificationManager.Type type, String iconGlyph, class_2960 effectTexture, class_1799 itemStack, long duration) {
         this.text = text;
         this.type = type;
         this.iconGlyph = iconGlyph;
         this.effectTexture = effectTexture;
         this.itemStack = itemStack;
         this.duration = duration;
      }

      public float getProgress() {
         if (this.duration <= 0L) {
            return 0.0F;
         } else {
            long elapsed = System.currentTimeMillis() - this.created;
            return Math.max(0.0F, Math.min(1.0F, 1.0F - (float)elapsed / (float)this.duration));
         }
      }

      public boolean isExpired() {
         return System.currentTimeMillis() - this.created >= this.duration;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Type {
      MODULE,
      EFFECT,
      INFO,
      WARNING;
   }
}
