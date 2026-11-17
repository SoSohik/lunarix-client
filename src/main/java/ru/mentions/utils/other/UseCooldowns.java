package ru.mentions.utils.other;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.notification.NotificationManager;

@Environment(EnvType.CLIENT)
public final class UseCooldowns implements IMinecraft {
   private static final int USE_TOLERANCE = 8;
   public static boolean debug;
   private static final UseCooldowns INSTANCE = new UseCooldowns();
   private static final Map<UUID, Map<UseCooldowns.Item, Long>> COOLDOWNS = new HashMap<>();
   private static final Map<UUID, UseCooldowns.Use> USING = new HashMap<>();
   private static final List<BiConsumer<class_1657, UseCooldowns.Item>> LISTENERS = new ArrayList<>();
   private static EventUpdate lastTick;

   private UseCooldowns() {
   }

   public static void listen(BiConsumer<class_1657, UseCooldowns.Item> listener) {
      LISTENERS.add(listener);
   }

   public static void tick(EventUpdate event) {
      if (event != lastTick) {
         lastTick = event;
         if (mc.field_1724 != null && mc.field_1687 != null) {
            HashSet<UUID> seen = new HashSet<>();

            for (class_1657 player : mc.field_1687.method_18456()) {
               if (player != mc.field_1724) {
                  seen.add(player.method_5667());
                  UseCooldowns.Use use = USING.computeIfAbsent(player.method_5667(), u -> new UseCooldowns.Use());
                  boolean now = player.method_6115();
                  if (now && !use.active) {
                     class_1799 stack = player.method_6030();
                     if (stack.method_7960()) {
                        stack = player.method_6047();
                     }

                     if (stack.method_7960()) {
                        stack = player.method_6079();
                     }

                     use.stack = stack.method_7972();
                     use.ticks = 0;
                  }

                  if (now) {
                     use.ticks++;
                  }

                  if (!now && use.active) {
                     int max = use.stack.method_7960() ? 0 : use.stack.method_7935(player);
                     if (debug && max > 0) {
                        NotificationManager.send(
                           player.method_5477().getString() + ": " + use.stack.method_7909().method_63680().getString() + " " + use.ticks + "/" + max,
                           NotificationManager.Type.INFO
                        );
                     }

                     if (max > 0 && use.ticks >= max - 8) {
                        consumed(player, use.stack);
                     }

                     use.stack = class_1799.field_8037;
                     use.ticks = 0;
                  }

                  use.active = now;
               }
            }

            USING.keySet().retainAll(seen);
            cleanup();
         }
      }
   }

   private static void consumed(class_1657 player, class_1799 stack) {
      if (stack.method_31574(class_1802.field_8367)) {
         trigger(player, UseCooldowns.Item.NOTCH);
      } else if (stack.method_31574(class_1802.field_8463)) {
         trigger(player, UseCooldowns.Item.GAPPLE);
      } else if (stack.method_31574(class_1802.field_8233)) {
         trigger(player, UseCooldowns.Item.CHORUS);
      } else if (stack.method_31574(class_1802.field_8551)) {
         trigger(player, UseCooldowns.Item.KELP);
      } else if (stack.method_31574(class_1802.field_22021)) {
         trigger(player, UseCooldowns.Item.SCRAP);
      } else if (stack.method_31574(class_1802.field_8574)) {
         class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
         if (contents == null) {
            return;
         }

         for (class_1293 effect : contents.method_57397()) {
            if (effect.method_5579().comp_349() == class_1294.field_5915.comp_349()) {
               trigger(player, UseCooldowns.Item.HEAL);
            }

            if (effect.method_5579().comp_349() == class_1294.field_5916.comp_349()) {
               trigger(player, UseCooldowns.Item.NAUSEA);
            }
         }
      }
   }

   public static void trigger(class_1657 player, UseCooldowns.Item item) {
      COOLDOWNS.computeIfAbsent(player.method_5667(), u -> new EnumMap<>(UseCooldowns.Item.class))
         .put(item, System.currentTimeMillis() + (long)item.seconds * 1000L);

      for (BiConsumer<class_1657, UseCooldowns.Item> listener : LISTENERS) {
         listener.accept(player, item);
      }
   }

   public static int remaining(UUID uuid, UseCooldowns.Item item) {
      if (uuid == null) {
         return 0;
      } else {
         Map<UseCooldowns.Item, Long> map = COOLDOWNS.get(uuid);
         if (map == null) {
            return 0;
         } else {
            Long end = map.get(item);
            return end == null ? 0 : (int)Math.max(0.0, Math.ceil((double)(end - System.currentTimeMillis()) / 1000.0));
         }
      }
   }

   public static Map<UseCooldowns.Item, Long> of(UUID uuid) {
      Map<UseCooldowns.Item, Long> map = uuid == null ? null : COOLDOWNS.get(uuid);
      return map == null ? Map.of() : map;
   }

   public static int players() {
      return COOLDOWNS.size();
   }

   public static String format(int seconds) {
      return String.format("%d:%02d", seconds / 60, seconds % 60);
   }

   private static void cleanup() {
      long now = System.currentTimeMillis();
      COOLDOWNS.values().forEach(map -> map.values().removeIf(end -> end <= now));
      COOLDOWNS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
   }

   public static void clear() {
      COOLDOWNS.clear();
      USING.clear();
   }

   @Environment(EnvType.CLIENT)
   public static enum Buff {
      STRENGTH("Сила", class_1294.field_5910, -43691),
      SPEED("Скорость", class_1294.field_5904, -11141121),
      RESISTANCE("Резист", class_1294.field_5907, -7829249),
      REGENERATION("Регена", class_1294.field_5924, -43521),
      FIRE_RESISTANCE("Огнеупор", class_1294.field_5918, -22016),
      ABSORPTION("Абсорб", class_1294.field_5898, -171);

      public final String label;
      public final class_6880<class_1291> effect;
      public final int color;

      private Buff(String label, class_6880<class_1291> effect, int color) {
         this.label = label;
         this.effect = effect;
         this.color = color;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Item {
      NOTCH("Чарка", 150, class_1802.field_8367),
      GAPPLE("Гепл", 30, class_1802.field_8463),
      HEAL("Хилка", 20, class_1802.field_8574),
      NAUSEA("Тошнотка", 60, class_1802.field_8574),
      CHORUS("Хорус", 20, class_1802.field_8233),
      KELP("Пласт", 20, class_1802.field_8551),
      SCRAP("Трапка", 15, class_1802.field_22021);

      public final String label;
      public final int seconds;
      public final class_1792 icon;

      private Item(String label, int seconds, class_1792 icon) {
         this.label = label;
         this.seconds = seconds;
         this.icon = icon;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Use {
      boolean active;
      class_1799 stack = class_1799.field_8037;
      int ticks;
   }
}
