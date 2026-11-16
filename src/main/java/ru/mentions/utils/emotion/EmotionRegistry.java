package ru.mentions.utils.emotion;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import ru.mentions.utils.emote.EmoteLibrary;
import ru.mentions.utils.emote.EmoteMetadata;

@Environment(EnvType.CLIENT)
public final class EmotionRegistry {
   private static List<EmotionRegistry.Emotion> cache = null;

   private static List<EmotionRegistry.Emotion> build() {
      ArrayList<EmotionRegistry.Emotion> out = new ArrayList<>();

      for (EmoteMetadata meta : EmoteLibrary.all()) {
         String display = meta.displayName() == null || meta.displayName().isBlank() ? meta.id() : meta.displayName();
         out.add(new EmotionRegistry.Emotion(meta.id(), display, meta.icon(), EmotionRegistry.Section.ALL));
      }

      return out;
   }

   private static List<EmotionRegistry.Emotion> cached() {
      if (cache == null) {
         cache = build();
      }

      return cache;
   }

   public static List<EmotionRegistry.Emotion> all() {
      return cached();
   }

   public static List<EmotionRegistry.Emotion> bySection(EmotionRegistry.Section section) {
      return cached();
   }

   public static String[] names() {
      List<EmotionRegistry.Emotion> list = cached();
      String[] out = new String[list.size() + 1];
      out[0] = "—";

      for (int i = 0; i < list.size(); i++) {
         out[i + 1] = list.get(i).name();
      }

      return out;
   }

   public static EmotionRegistry.Emotion byName(String name) {
      if (name == null) {
         return null;
      }

      for (EmotionRegistry.Emotion emotion : cached()) {
         if (emotion.name().equalsIgnoreCase(name)) {
            return emotion;
         }
      }

      for (EmotionRegistry.Emotion emotion : cached()) {
         if (emotion.display() != null && emotion.display().equalsIgnoreCase(name)) {
            return emotion;
         }
      }

      return null;
   }

   public static int size() {
      return cached().size();
   }

   private EmotionRegistry() {
   }

   @Environment(EnvType.CLIENT)
   public static record Emotion(String name, String display, class_2960 icon, EmotionRegistry.Section section) {
   }

   @Environment(EnvType.CLIENT)
   public static enum Section {
      ALL("Все эмоции");

      public final String label;

      private Section(String label) {
         this.label = label;
      }
   }
}
