package ru.mentions.utils.potion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1665;
import net.minecraft.class_1667;
import net.minecraft.class_1679;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3857;
import net.minecraft.class_742;
import net.minecraft.class_7923;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import ru.mentions.lang.Lang;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.notification.NotificationManager;

@Environment(EnvType.CLIENT)
public final class CustomBuffs {
   public static final List<CustomBuffs.CustomBuff> ALL_BUFFS = new ArrayList<>();
   public static final CustomBuffs.CustomBuff ASSASSIN = register(
      new CustomBuffs.CustomBuff(
         "assassin",
         "Зелье Ассасина",
         "Зілля Асасіна",
         "Зелье Ассасина",
         -1946805,
         class_1802.field_22024,
         "strength",
         CustomBuffs.BuffType.POTION,
         List.of(
            new CustomBuffs.EffectRequirement("strength", 3, 60),
            new CustomBuffs.EffectRequirement("speed", 2, 300),
            new CustomBuffs.EffectRequirement("haste", 0, 60)
         )
      )
   );
   public static final CustomBuffs.CustomBuff CLAPPER = register(
      new CustomBuffs.CustomBuff(
         "clapper",
         "Хлопушка",
         "Хлопавка",
         "Хлопушка",
         -46924,
         class_1802.field_8639,
         "slowness",
         CustomBuffs.BuffType.POTION,
         List.of(
            new CustomBuffs.EffectRequirement("slowness", 8, 10),
            new CustomBuffs.EffectRequirement("speed", 4, 20),
            new CustomBuffs.EffectRequirement("blindness", -1, 5),
            new CustomBuffs.EffectRequirement("glowing", 0, 180)
         )
      )
   );
   public static final CustomBuffs.CustomBuff HOLY_WATER = register(
      new CustomBuffs.CustomBuff(
         "holy_water",
         "Святая вода",
         "Свята вода",
         "Святая вода",
         -11669782,
         class_1802.field_8436,
         "regeneration",
         CustomBuffs.BuffType.POTION,
         List.of(new CustomBuffs.EffectRequirement("regeneration", 1, 45), new CustomBuffs.EffectRequirement("invisibility", 1, 600))
      )
   );
   public static final CustomBuffs.CustomBuff WRATH = register(
      new CustomBuffs.CustomBuff(
         "wrath",
         "Зелье Гнева",
         "Зілля Гніву",
         "Зелье Гнева",
         -39424,
         class_1802.field_8243,
         "strength",
         CustomBuffs.BuffType.POTION,
         List.of(new CustomBuffs.EffectRequirement("strength", 4, 30), new CustomBuffs.EffectRequirement("slowness", 3, 30))
      )
   );
   public static final CustomBuffs.CustomBuff PALADIN = register(
      new CustomBuffs.CustomBuff(
         "paladin",
         "Зелье Палладина",
         "Зілля Паладина",
         "Зелье Палладина",
         -10496,
         class_1802.field_8288,
         "health_boost",
         CustomBuffs.BuffType.POTION,
         List.of(
            new CustomBuffs.EffectRequirement("health_boost", 2, 60),
            new CustomBuffs.EffectRequirement("resistance", 0, 600),
            new CustomBuffs.EffectRequirement("fire_resistance", 0, 600),
            new CustomBuffs.EffectRequirement("invisibility", 0, 900)
         )
      )
   );
   public static final CustomBuffs.CustomBuff RADIATION = register(
      new CustomBuffs.CustomBuff(
         "radiation",
         "Зелье Радиации",
         "Зілля Радіації",
         "Зелье Радиации",
         -8061184,
         class_1802.field_8251,
         "poison",
         CustomBuffs.BuffType.POTION,
         List.of(
            new CustomBuffs.EffectRequirement("poison", 1, 60),
            new CustomBuffs.EffectRequirement("wither", 1, 60),
            new CustomBuffs.EffectRequirement("slowness", 2, 90),
            new CustomBuffs.EffectRequirement("hunger", 4, 60),
            new CustomBuffs.EffectRequirement("glowing", 0, 120)
         )
      )
   );
   public static final CustomBuffs.CustomBuff SLEEPER = register(
      new CustomBuffs.CustomBuff(
         "sleeper",
         "Снотворное",
         "Снодійне",
         "Снотворное",
         -6736897,
         class_1802.field_8233,
         "weakness",
         CustomBuffs.BuffType.POTION,
         List.of(
            new CustomBuffs.EffectRequirement("weakness", 1, 90),
            new CustomBuffs.EffectRequirement("mining_fatigue", 1, 10),
            new CustomBuffs.EffectRequirement("wither", 2, 90),
            new CustomBuffs.EffectRequirement("blindness", 0, 10)
         )
      )
   );
   public static final CustomBuffs.CustomBuff ZEUS_ARROW = new CustomBuffs.CustomBuff(
      "zeus_arrow",
      "Стрела Зевса",
      "Стріла Зевса",
      "Стрела Зевса",
      -10496,
      class_1802.field_8087,
      "slowness",
      CustomBuffs.BuffType.ARROW,
      List.of(new CustomBuffs.EffectRequirement("slowness", 2, 3))
   );
   public static final CustomBuffs.CustomBuff BLOOD_ARROW = register(
      new CustomBuffs.CustomBuff(
         "blood_arrow",
         "Кровавая стрела",
         "Кривава стріла",
         "Кровавая стрела",
         -5103070,
         class_1802.field_8087,
         "weakness",
         CustomBuffs.BuffType.ARROW,
         List.of(
            new CustomBuffs.EffectRequirement("weakness", 2, 3),
            new CustomBuffs.EffectRequirement("blindness", 0, 2),
            new CustomBuffs.EffectRequirement("mining_fatigue", 0, 2),
            new CustomBuffs.EffectRequirement("nausea", 0, 5)
         )
      )
   );
   public static final CustomBuffs.CustomBuff TORMENT_ARROW = register(
      new CustomBuffs.CustomBuff(
         "torment_arrow",
         "Мучительная стрела",
         "Мученицька стріла",
         "Мучительная стрела",
         -13726889,
         class_1802.field_8087,
         "wither",
         CustomBuffs.BuffType.ARROW,
         List.of(
            new CustomBuffs.EffectRequirement("slowness", 2, 5),
            new CustomBuffs.EffectRequirement("wither", 2, 5),
            new CustomBuffs.EffectRequirement("poison", 2, 5)
         )
      )
   );
   public static final CustomBuffs.CustomBuff FROST_ARROW = register(
      new CustomBuffs.CustomBuff(
         "frost_arrow",
         "Стрела обледенения",
         "Стріла обледеніння",
         "Стрела обледенения",
         -16718337,
         class_1802.field_8087,
         "slowness",
         CustomBuffs.BuffType.ARROW,
         List.of(new CustomBuffs.EffectRequirement("slowness", 9, 5), new CustomBuffs.EffectRequirement("mining_fatigue", 2, 2))
      )
   );
   public static final CustomBuffs.CustomBuff LIGHT_ARROW = new CustomBuffs.CustomBuff(
      "light_arrow",
      "Световая стрела",
      "Світлова стріла",
      "Световая стрела",
      -3722,
      class_1802.field_8236,
      "speed",
      CustomBuffs.BuffType.ARROW,
      List.of(
         new CustomBuffs.EffectRequirement("speed", 4, 5),
         new CustomBuffs.EffectRequirement("haste", 2, 5),
         new CustomBuffs.EffectRequirement("blindness", 0, 3)
      )
   );
   public static final CustomBuffs.CustomBuff THERAPY_ARROW = register(
      new CustomBuffs.CustomBuff(
         "therapy_arrow",
         "Стрела терапии",
         "Стріла терапії",
         "Стрела терапии",
         -8586240,
         class_1802.field_8087,
         "slowness",
         CustomBuffs.BuffType.ARROW,
         List.of(new CustomBuffs.EffectRequirement("slowness", 4, 7), new CustomBuffs.EffectRequirement("weakness", 0, 7))
      )
   );
   private static final Map<String, Set<String>> playerBuffCache = new HashMap<>();
   private static final Set<Integer> notifiedProjectiles = new HashSet<>();

   private static CustomBuffs.CustomBuff register(CustomBuffs.CustomBuff buff) {
      ALL_BUFFS.add(buff);
      return buff;
   }

   public static String getEffectId(class_1293 inst) {
      if (inst != null && inst.method_5579() != null) {
         class_1291 effect = (class_1291)inst.method_5579().comp_349();
         if (effect == null) {
            return null;
         } else {
            class_2960 id = class_7923.field_41174.method_10221(effect);
            return id != null ? id.method_12832() : null;
         }
      } else {
         return null;
      }
   }

   public static List<CustomBuffs.ActiveBuff> detectActiveBuffs(Collection<class_1293> instances) {
      if (instances != null && !instances.isEmpty()) {
         List<CustomBuffs.ActiveBuff> result = new ArrayList<>();
         Set<class_1293> usedEffects = new HashSet<>();
         List<CustomBuffs.CustomBuff> arrowBuffs = new ArrayList<>();
         List<CustomBuffs.CustomBuff> potionBuffs = new ArrayList<>();

         for (CustomBuffs.CustomBuff buff : ALL_BUFFS) {
            if (buff.isArrow()) {
               arrowBuffs.add(buff);
            } else {
               potionBuffs.add(buff);
            }
         }

         arrowBuffs.sort((a, b) -> Integer.compare(b.getRequiredEffects().size(), a.getRequiredEffects().size()));
         potionBuffs.sort((a, b) -> Integer.compare(b.getRequiredEffects().size(), a.getRequiredEffects().size()));
         List<class_1293> arrowPool = new ArrayList<>(instances);

         for (CustomBuffs.CustomBuff buffx : arrowBuffs) {
            CustomBuffs.ActiveBuff active = buffx.match(arrowPool);
            if (active != null) {
               result.add(active);
               usedEffects.addAll(active.getMatchedInstances());
               arrowPool.removeAll(active.getMatchedInstances());
            }
         }

         List<class_1293> potionPool = new ArrayList<>();

         for (class_1293 inst : instances) {
            if (!usedEffects.contains(inst)) {
               potionPool.add(inst);
            }
         }

         boolean found;
         do {
            found = false;
            CustomBuffs.ActiveBuff bestMatch = null;
            int bestScore = -1;

            for (CustomBuffs.CustomBuff buffxx : potionBuffs) {
               CustomBuffs.ActiveBuff candidate = buffxx.match(potionPool);
               if (candidate != null) {
                  int count = candidate.getMatchedInstances().size();
                  int score = count * 10;
                  if ("sleeper".equals(buffxx.getId())) {
                     score += 35;
                  } else if ("radiation".equals(buffxx.getId())) {
                     score += 25;
                  } else if ("clapper".equals(buffxx.getId())) {
                     score += 15;
                  }

                  if (score > bestScore) {
                     bestScore = score;
                     bestMatch = candidate;
                  }
               }
            }

            if (bestMatch != null) {
               result.add(bestMatch);
               potionPool.removeAll(bestMatch.getMatchedInstances());
               found = true;
            }
         } while (found && !potionPool.isEmpty());

         return result;
      } else {
         return Collections.emptyList();
      }
   }

   public static CustomBuffs.CustomBuff findByItemStack(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         String displayName = stack.method_7964().getString();
         CustomBuffs.CustomBuff byName = findByName(displayName);
         if (byName != null) {
            return byName;
         } else {
            class_9290 lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632);
            if (lore != null) {
               for (class_2561 line : lore.comp_2400()) {
                  CustomBuffs.CustomBuff byLore = findByName(line.getString());
                  if (byLore != null) {
                     return byLore;
                  }
               }
            }

            class_1844 potion = (class_1844)stack.method_58694(class_9334.field_49651);
            if (potion != null) {
               List<class_1293> customEffects = new ArrayList<>();
               potion.method_57397().forEach(customEffects::add);
               if (!customEffects.isEmpty()) {
                  for (CustomBuffs.CustomBuff buff : ALL_BUFFS) {
                     CustomBuffs.ActiveBuff match = buff.match(customEffects);
                     if (match != null) {
                        return buff;
                     }
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public static CustomBuffs.CustomBuff findByName(String name) {
      if (name != null && !name.isEmpty()) {
         String clean = name.replaceAll("§.", "").toLowerCase(Locale.ROOT).trim();
         if (!clean.contains("зевс") && !clean.contains("zeus") && !clean.contains("світл") && !clean.contains("свет") && !clean.contains("spectral")) {
            for (CustomBuffs.CustomBuff buff : ALL_BUFFS) {
               String ru = buff.getNameRu().toLowerCase(Locale.ROOT);
               String ua = buff.getNameUa().toLowerCase(Locale.ROOT);
               if (clean.contains(ru) || clean.contains(ua)) {
                  return buff;
               }

               if (buff.rawSearchName != null && clean.contains(buff.rawSearchName.toLowerCase(Locale.ROOT))) {
                  return buff;
               }
            }

            if (clean.contains("мучительн") || clean.contains("мучениц")) {
               return TORMENT_ARROW;
            } else if (clean.contains("кровав") || clean.contains("кривав")) {
               return BLOOD_ARROW;
            } else if (clean.contains("обледенен") || clean.contains("обледенін") || clean.contains("заморозк")) {
               return FROST_ARROW;
            } else {
               return !clean.contains("терапи") && !clean.contains("терапі") ? null : THERAPY_ARROW;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void checkPlayerBuffs(class_1657 player) {
      if (player != null) {
         String playerName = player.method_5477().getString();
         Collection<class_1293> effects = player.method_6026();
         List<CustomBuffs.ActiveBuff> activeBuffs = detectActiveBuffs(effects);
         Set<String> previous = playerBuffCache.computeIfAbsent(playerName, k -> new HashSet<>());
         Set<String> current = new HashSet<>();

         for (CustomBuffs.ActiveBuff ab : activeBuffs) {
            String buffId = ab.getBuff().getId();
            if (!"zeus_arrow".equals(buffId) && !"light_arrow".equals(buffId)) {
               if (ab.getBuff().isArrow() && ab.getBuff().getDynamicColor() == 0) {
                  OptionalInt effColor = class_1844.method_58111(ab.getMatchedInstances());
                  if (effColor.isPresent()) {
                     ab.getBuff().setDynamicColor(effColor.getAsInt());
                  }
               }

               current.add(buffId);
               if (!previous.contains(buffId)) {
                  broadcastBuffUsage(player, ab.getBuff());
               }
            }
         }

         playerBuffCache.put(playerName, current);
      }
   }

   public static void clearPlayerBuffCache() {
      playerBuffCache.clear();
   }

   private static void broadcastBuffUsage(class_1657 player, CustomBuffs.CustomBuff buff) {
      if (!buff.isArrow() || !"zeus_arrow".equals(buff.getId()) && !"light_arrow".equals(buff.getId())) {
         String playerName = player.method_5477().getString();
         boolean isSelf = player == class_310.method_1551().field_1724;
         String soundMode = "DingDong";
         if (InterFace.getInstance() != null && InterFace.getInstance().effectsSound != null) {
            soundMode = InterFace.getInstance().effectsSound.getValue();
         }

         String msg;
         String notifHeader;
         if (buff.isArrow()) {
            if (isSelf) {
               msg = Lang.isUkrainian() ? "§c[★] У вас влучила §e" + buff.getNameUa() + "§c!" : "§c[★] В вас попали: §e" + buff.getNameRu() + "§c!";
               notifHeader = Lang.isUkrainian() ? "Влучання: " : "Попадание: ";
            } else {
               msg = Lang.isUkrainian()
                  ? "§c[★] У гравця §f" + playerName + " §cвлучила §e" + buff.getNameUa() + "§c!"
                  : "§c[★] В игрока §f" + playerName + " §cпопали: §e" + buff.getNameRu() + "§c!";
               notifHeader = playerName + ": ";
            }
         } else if (isSelf) {
            msg = Lang.isUkrainian() ? "§e[★] Ви використали §a" + buff.getNameUa() + "§e!" : "§e[★] Вы использовали §a" + buff.getNameRu() + "§e!";
            notifHeader = Lang.isUkrainian() ? "Використано: " : "Использовано: ";
         } else {
            msg = Lang.isUkrainian()
               ? "§e[★] Гравець §f" + playerName + " §eзабафав §a" + buff.getNameUa() + "§e!"
               : "§e[★] Игрок §f" + playerName + " §eзабафал §a" + buff.getNameRu() + "§e!";
            notifHeader = playerName + ": ";
         }

         ChatUtils.addChatMessage(msg);
         NotificationManager.send(notifHeader + buff.getLocalizedName(), NotificationManager.Type.INFO, buff.createIconStack(), 3500L, soundMode);
      }
   }

   public static class_1799 createTippedArrowStack(int color) {
      class_1799 stack = new class_1799(class_1802.field_8087);

      try {
         stack.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(color & 16777215), List.of(), Optional.empty()));
      } catch (Throwable var3) {
      }

      return stack;
   }

   public static void checkIncomingProjectiles() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1687 != null) {
         class_742 self = client.field_1724;
         String soundMode = "DingDong";
         if (InterFace.getInstance() != null && InterFace.getInstance().effectsSound != null) {
            soundMode = InterFace.getInstance().effectsSound.getValue();
         }

         for (class_1297 entity : client.field_1687.method_18112()) {
            if (entity != null && entity != self && entity.method_5805() && !entity.method_31481()) {
               int id = entity.method_5628();
               if (!notifiedProjectiles.contains(id)) {
                  double distSq = entity.method_5858(self);
                  if (!(distSq > 400.0)) {
                     if (entity instanceof class_3857) {
                        class_3857 thrown = (class_3857)entity;
                        class_1799 stack = thrown.method_7495();
                        CustomBuffs.CustomBuff buff = findByItemStack(stack);
                        if (buff != null) {
                           notifiedProjectiles.add(id);
                           String msg = Lang.isUkrainian()
                              ? "§e[★] Поруч летить зілля: §a" + buff.getNameUa() + "§e!"
                              : "§e[★] Рядом летит зелье: §a" + buff.getNameRu() + "§e!";
                           String header = Lang.isUkrainian() ? "Зілля: " : "Зелье: ";
                           ChatUtils.addChatMessage(msg);
                           NotificationManager.send(header + buff.getLocalizedName(), NotificationManager.Type.INFO, buff.createIconStack(), 3000L, soundMode);
                        }
                     } else if (entity instanceof class_1665) {
                        class_1665 arrow = (class_1665)entity;
                        if (!arrow.method_7441() && arrow.method_24921() != self && !(arrow instanceof class_1679)) {
                           class_1799 stack = arrow.method_54759();
                           if (stack == null || stack.method_7960() || stack.method_7909() != class_1802.field_8236) {
                              String customName = stack != null && !stack.method_7960() ? stack.method_7964().getString() : "";
                              String cleanName = customName.replaceAll("§.", "").trim().toLowerCase(Locale.ROOT);
                              if (!cleanName.contains("зевс")
                                 && !cleanName.contains("zeus")
                                 && !cleanName.contains("світл")
                                 && !cleanName.contains("свет")
                                 && !cleanName.contains("spectral")) {
                                 int arrowColor = -1;
                                 if (arrow instanceof class_1667) {
                                    class_1667 arrowEntity = (class_1667)arrow;
                                    int c = arrowEntity.method_7460();
                                    if (c != -1) {
                                       arrowColor = c;
                                    }
                                 }

                                 if (arrowColor == -1 && stack != null && !stack.method_7960()) {
                                    class_1844 potion = (class_1844)stack.method_58694(class_9334.field_49651);
                                    if (potion != null) {
                                       int pc = potion.method_8064();
                                       if (pc != -1) {
                                          arrowColor = pc;
                                       }
                                    }
                                 }

                                 if (arrowColor != -1) {
                                    CustomBuffs.CustomBuff buff = findByItemStack(stack);
                                    if (buff == null && !cleanName.isEmpty()) {
                                       buff = findByName(cleanName);
                                    }

                                    if (buff != null) {
                                       if ("zeus_arrow".equals(buff.getId()) || "light_arrow".equals(buff.getId())) {
                                          continue;
                                       }

                                       buff.setDynamicColor(arrowColor);
                                    }

                                    notifiedProjectiles.add(id);
                                    String arrowDisplayName;
                                    if (buff != null) {
                                       arrowDisplayName = buff.getLocalizedName();
                                    } else if (!cleanName.isEmpty() && !cleanName.equalsIgnoreCase("arrow") && !cleanName.equalsIgnoreCase("стрела")) {
                                       arrowDisplayName = customName.replaceAll("§.", "").trim();
                                    } else {
                                       arrowDisplayName = Lang.isUkrainian() ? "Стріла з ефектом" : "Стрела с эффектом";
                                    }

                                    class_1799 iconStack = buff != null ? buff.createIconStack(arrowColor) : createTippedArrowStack(arrowColor);
                                    String msg = Lang.isUkrainian()
                                       ? "§c[★] У вас летить стріла: §e" + arrowDisplayName + "§c!"
                                       : "§c[★] В вас летит стрела: §e" + arrowDisplayName + "§c!";
                                    String header = Lang.isUkrainian() ? "Стріла: " : "Стрела: ";
                                    ChatUtils.addChatMessage(msg);
                                    NotificationManager.send(header + arrowDisplayName, NotificationManager.Type.WARNING, iconStack, 3000L, soundMode);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (notifiedProjectiles.size() > 200) {
            notifiedProjectiles.clear();
         }
      } else {
         notifiedProjectiles.clear();
      }
   }

   public static final class ActiveBuff {
      private final CustomBuffs.CustomBuff buff;
      private final int durationTicks;
      private final class_1293 primaryInstance;
      private final Set<class_1293> matchedInstances;

      public ActiveBuff(CustomBuffs.CustomBuff buff, int durationTicks, class_1293 primaryInstance, Set<class_1293> matchedInstances) {
         this.buff = buff;
         this.durationTicks = durationTicks;
         this.primaryInstance = primaryInstance;
         this.matchedInstances = matchedInstances;
      }

      public CustomBuffs.CustomBuff getBuff() {
         return this.buff;
      }

      public int getDurationTicks() {
         return this.durationTicks;
      }

      public int getDurationSeconds() {
         return this.durationTicks / 20;
      }

      public class_1293 getPrimaryInstance() {
         return this.primaryInstance;
      }

      public Set<class_1293> getMatchedInstances() {
         return this.matchedInstances;
      }

      public String getDurationString() {
         if (this.primaryInstance != null && this.primaryInstance.method_48559()) {
            return "∞";
         } else {
            int ticks = this.durationTicks;
            if (ticks > 0 && ticks <= 72000) {
               int sec = ticks / 20;
               return String.format(Locale.ROOT, "%d:%02d", sec / 60, sec % 60);
            } else {
               return "∞";
            }
         }
      }
   }

   public static enum BuffType {
      POTION,
      ARROW;
   }

   public static final class CustomBuff {
      private final String id;
      private final String nameRu;
      private final String nameUa;
      private final String rawSearchName;
      private final int color;
      private final class_1792 iconItem;
      private final String primaryEffectId;
      private final CustomBuffs.BuffType buffType;
      private final List<CustomBuffs.EffectRequirement> requiredEffects;
      private int dynamicColor;

      public CustomBuff(
         String id,
         String nameRu,
         String nameUa,
         String rawSearchName,
         int color,
         class_1792 iconItem,
         String primaryEffectId,
         CustomBuffs.BuffType buffType,
         List<CustomBuffs.EffectRequirement> requiredEffects
      ) {
         this.id = id;
         this.nameRu = nameRu;
         this.nameUa = nameUa;
         this.rawSearchName = rawSearchName;
         this.color = color;
         this.iconItem = iconItem;
         this.primaryEffectId = primaryEffectId;
         this.buffType = buffType;
         this.requiredEffects = requiredEffects;
      }

      public String getId() {
         return this.id;
      }

      public String getNameRu() {
         return this.nameRu;
      }

      public String getNameUa() {
         return this.nameUa;
      }

      public String getLocalizedName() {
         return Lang.isUkrainian() ? this.nameUa : this.nameRu;
      }

      public String getRawSearchName() {
         return this.rawSearchName;
      }

      public int getColor() {
         return this.dynamicColor != 0 ? this.dynamicColor : this.color;
      }

      public int getDynamicColor() {
         return this.dynamicColor;
      }

      public void setDynamicColor(int dynamicColor) {
         this.dynamicColor = dynamicColor;
      }

      public class_1792 getIconItem() {
         return this.iconItem;
      }

      public String getPrimaryEffectId() {
         return this.primaryEffectId;
      }

      public CustomBuffs.BuffType getBuffType() {
         return this.buffType;
      }

      public boolean isArrow() {
         return this.buffType == CustomBuffs.BuffType.ARROW;
      }

      public List<CustomBuffs.EffectRequirement> getRequiredEffects() {
         return this.requiredEffects;
      }

      public class_1799 createIconStack() {
         return this.createIconStack(0);
      }

      public class_1799 createIconStack(int explicitColor) {
         int finalColor = explicitColor != 0 ? explicitColor : (this.dynamicColor != 0 ? this.dynamicColor : this.color);
         if (this.isArrow()) {
            class_1799 stack = new class_1799(class_1802.field_8087);

            try {
               stack.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(finalColor & 16777215), List.of(), Optional.empty()));
            } catch (Throwable var5) {
            }

            return stack;
         } else {
            class_1799 stack = new class_1799(class_1802.field_8436);

            try {
               stack.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(finalColor & 16777215), List.of(), Optional.empty()));
            } catch (Throwable var6) {
            }

            return stack;
         }
      }

      public boolean hasSignature(Map<String, class_1293> map) {
         if (map == null || map.isEmpty()) {
            return false;
         } else if (this.isArrow()) {
            String var7 = this.id;
            switch (var7) {
               case "frost_arrow":
                  return map.containsKey("slowness")
                     && map.containsKey("mining_fatigue")
                     && !map.containsKey("wither")
                     && !map.containsKey("weakness")
                     && !map.containsKey("poison")
                     && map.get("slowness").method_5578() >= 4;
               case "blood_arrow":
                  return map.containsKey("weakness")
                     && (map.containsKey("blindness") || map.containsKey("mining_fatigue") || map.containsKey("nausea"))
                     && !map.containsKey("wither")
                     && !map.containsKey("poison")
                     && map.get("weakness").method_5578() >= 1;
               case "torment_arrow":
                  return map.containsKey("slowness") && map.containsKey("wither") && map.containsKey("poison");
               case "therapy_arrow":
                  return map.containsKey("slowness")
                     && map.containsKey("weakness")
                     && !map.containsKey("wither")
                     && !map.containsKey("mining_fatigue")
                     && !map.containsKey("poison");
               case "light_arrow":
               case "zeus_arrow":
                  return false;
               default:
                  return true;
            }
         } else {
            String var2 = this.id;
            switch (var2) {
               case "assassin":
                  if (!map.containsKey("strength") || !map.containsKey("speed") || !map.containsKey("haste")) {
                     return false;
                  } else {
                     return !map.containsKey("slowness") && !map.containsKey("health_boost") && !map.containsKey("wither")
                        ? map.get("strength").method_5578() >= 2 && map.get("speed").method_5578() >= 1
                        : false;
                  }
               case "clapper":
                  if (!map.containsKey("wither") && !map.containsKey("weakness") && !map.containsKey("hunger")) {
                     if (map.containsKey("slowness") && map.containsKey("speed")) {
                        boolean hasExtra = map.containsKey("glowing") || map.containsKey("blindness");
                        class_1293 slow = map.get("slowness");
                        class_1293 spd = map.get("speed");
                        return hasExtra || slow.method_5578() >= 3 && spd.method_5578() >= 2;
                     }

                     return false;
                  }

                  return false;
               case "holy_water":
                  if (map.containsKey("regeneration") && map.containsKey("invisibility")) {
                     if (!map.containsKey("health_boost") && !map.containsKey("poison") && !map.containsKey("wither")) {
                        return map.get("regeneration").method_5578() >= 1;
                     }

                     return false;
                  }

                  return false;
               case "wrath":
                  if (map.containsKey("strength") && map.containsKey("slowness")) {
                     if (!map.containsKey("haste") && !map.containsKey("health_boost") && !map.containsKey("speed") && !map.containsKey("wither")) {
                        return map.get("strength").method_5578() >= 3 && map.get("slowness").method_5578() >= 2;
                     }

                     return false;
                  }

                  return false;
               case "paladin":
                  if (!map.containsKey("health_boost")) {
                     return false;
                  } else {
                     if (!map.containsKey("slowness") && !map.containsKey("wither")) {
                        boolean hasArmorBuff = map.containsKey("resistance") || map.containsKey("fire_resistance") || map.containsKey("invisibility");
                        return hasArmorBuff && map.get("health_boost").method_5578() >= 1;
                     }

                     return false;
                  }
               case "radiation":
                  if (map.containsKey("hunger") && map.containsKey("glowing")) {
                     return map.containsKey("poison") || map.containsKey("wither");
                  }

                  return false;
               case "sleeper":
                  if (map.containsKey("glowing") && map.containsKey("hunger")) {
                     return false;
                  } else {
                     boolean hasWeakness = map.containsKey("weakness");
                     boolean hasWither = map.containsKey("wither");
                     boolean hasFatigue = map.containsKey("mining_fatigue");
                     if (hasWeakness && hasWither) {
                        return true;
                     } else {
                        if (!hasFatigue || !hasWeakness && !hasWither && !map.containsKey("blindness")) {
                           return false;
                        }

                        return true;
                     }
                  }
               default:
                  return true;
            }
         }
      }

      public CustomBuffs.ActiveBuff match(Collection<class_1293> instances) {
         if (instances != null && !instances.isEmpty()) {
            Map<String, class_1293> map = new HashMap<>();

            for (class_1293 inst : instances) {
               String effectId = CustomBuffs.getEffectId(inst);
               if (effectId != null) {
                  map.put(effectId, inst);
               }
            }

            if (!this.hasSignature(map)) {
               return null;
            } else {
               class_1293 primaryInstance = map.get(this.primaryEffectId);
               Set<class_1293> matchedInstances = new HashSet<>();

               for (CustomBuffs.EffectRequirement req : this.requiredEffects) {
                  class_1293 instx = map.get(req.effectId());
                  if (instx != null && (req.amplifier() < 0 || instx.method_5578() >= req.amplifier())) {
                     matchedInstances.add(instx);
                  }
               }

               if (primaryInstance == null) {
                  for (class_1293 instx : matchedInstances) {
                     if (primaryInstance == null || instx.method_5584() > primaryInstance.method_5584()) {
                        primaryInstance = instx;
                     }
                  }
               }

               if (primaryInstance != null && !matchedInstances.isEmpty()) {
                  int durationTicks = primaryInstance.method_5584();
                  return new CustomBuffs.ActiveBuff(this, durationTicks, primaryInstance, matchedInstances);
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      }
   }

   public static record EffectRequirement(String effectId, int amplifier, int nominalDurationSeconds) {
   }
}
