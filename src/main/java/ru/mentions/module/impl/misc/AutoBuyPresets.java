package ru.mentions.module.impl.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AutoBuyPresets {
   private static final String[] CUSTOM = new String[]{
      "Сфера Афины",
      "Сфера Хаоса",
      "Сфера Сатира",
      "Сфера Бестии",
      "Сфера Ареса",
      "Сфера Гидры",
      "Сфера Икара",
      "Сфера Титана",
      "Сфера Эрида",
      "Шлем Infinity",
      "Нагрудник Infinity",
      "Поножи Infinity",
      "Ботинки Infinity",
      "Шлем Eternity",
      "Нагрудник Eternity",
      "Штаны Eternity",
      "Поножи Eternity",
      "Ботинки Eternity",
      "Шлем солнца",
      "Броневая элитра",
      "Меч Eternity",
      "Кирка Eternity",
      "Арбалет Eternity",
      "Громовержец",
      "Сфера Цербера",
      "Сфера Флеша",
      "Сфера Имморталити",
      "Сфера ɪᴍᴍᴏʀᴛᴀʟɪᴛʏ",
      "Сфера Арморталити",
      "Сфера ᴀʀᴍᴏʀᴛᴀʟɪᴛʏ",
      "Сфера на Скорость III",
      "Сфера на скорость 3",
      "Сфера Eternity",
      "Сфера Stinger",
      "Сфера на броня III скорость II",
      "Сфера на урон II броня III",
      "Сфера на броня II урон III",
      "Талисман Stinger",
      "Талисман Infinity",
      "Талисман Eternity",
      "Легендарный талисман",
      "Талисман Крушителя",
      "Талисман Раздора",
      "Талисман Тирана",
      "Талисман Ярости",
      "Талисман Вихря",
      "Талисман Мрака",
      "Талисман Демона",
      "Талисман Карателя",
      "Шлем Крушителя",
      "Нагрудник Крушителя",
      "Поножи Крушителя",
      "Ботинки Крушителя",
      "Меч Крушителя",
      "Булава Крушителя",
      "Кирка Крушителя",
      "Осколок сферы",
      "Руна «Бессмертие»",
      "Взрывная трапка",
      "Взрывная штучка",
      "Стан",
      "Хлопушка",
      "Святая Вода",
      "Святая вода",
      "Золотая кирка Джейка",
      "Боевой фрагмент",
      "Фейерверк",
      "Порох",
      "Динамит А",
      "Динамит A",
      "Динамит B",
      "динамит б",
      "Динамит B2",
      "динамит б2",
      "C4 ВзРыВчАтКа",
      "с4 взрывчатка",
      "C4",
      "Охотник",
      "Снеговик",
      "Иллюминатор",
      "Эндермен",
      "Анти Фантом",
      "Телекинез",
      "Гравитация",
      "Вампиризм",
      "Справедливость",
      "Универсальный ключ",
      "Фармер",
      "Золотая морковь",
      "Артефакт"
   };

   private AutoBuyPresets() {
   }

   public static int install(AutoBuyRules store) {
      if (store == null) {
         return 0;
      } else {
         int added = 0;

         for (AutoBuyRule preset : catalog()) {
            if (store.install(preset)) {
               added++;
            }
         }

         if (added > 0) {
            store.save();
         }

         return added;
      }
   }

   public static List<AutoBuyRule> catalog() {
      List<AutoBuyRule> list = new ArrayList<>();
      add(list, "minecraft:enchanted_golden_apple", "Чарка", 50000, true, "Зачарованное золотое яблоко");
      add(list, "minecraft:beacon", "Маяк", 80000, true, "Загадочный маяк");
      add(list, "minecraft:netherite_ingot", "Незеритовый слиток", 60000, true);
      add(list, "minecraft:shulker_box", "Шалкер", 60000, true, "Шалкеровый ящик");
      add(list, "minecraft:glowstone_dust", "Явная Пыль", 60000, true, "Явная пыль");
      add(list, "minecraft:ender_pearl", "Дезориентация", 60000, true, "Дезориентация");
      add(list, "minecraft:iron_trapdoor", "Трапка", 40000, true, "Трапка");
      add(list, "minecraft:paper", "Пласт", 40000, true, "Пласт");
      add(list, "minecraft:experience_bottle", "Опыт 15", 40000, true, "Пузырек опыта [15 ур]", "Пузырёк опыта [15 ур]", "Пузырек с 15 уровнем");
      add(list, "minecraft:experience_bottle", "Опыт 30", 40000, true, "Пузырек опыта [30 ур]", "Пузырёк опыта [30 Ур.]");
      add(list, "minecraft:experience_bottle", "Опыт 50", 40000, true, "Пузырек опыта [50 ур]");
      add(list, "minecraft:tnt", "Вайт", 40000, true, "TNT - TIER WHITE", "TNT WHITE");
      add(list, "minecraft:tnt", "Блек", 40000, true, "TNT - TIER BLACK", "TNT BLACK");
      add(list, "minecraft:totem_of_undying", "Тотем бессмертия", 40000, true);
      add(list, "minecraft:elytra", "Элитры", 50000, true);
      add(list, "minecraft:ender_pearl", "Эндер жемчуг", 50000, true, "Эндер-жемчуг");
      add(list, "minecraft:golden_apple", "Золотое яблоко", 50000, true);
      add(list, "minecraft:spawner", "Спавнер", 100000, true);
      add(list, "minecraft:netherite_block", "Незеритовый блок", 100000, true);
      add(list, "minecraft:wind_charge", "Заряд ветра", 100000, true);
      add(list, "minecraft:nether_star", "Звезда Незера", 100000, true, "Звезда незера", "Nether Star");
      add(list, "minecraft:paper", "Божья аура", 40000, true, "божья аура", "Божья Аура");
      add(list, "minecraft:snowball", "Снежок заморозка", 40000, true, "снежок заморозка", "Заморозка");
      add(list, "minecraft:tipped_arrow", "Кровавая стрела", 40000, true, "кровавая стрела", "Кровавая Стрела");
      add(list, "minecraft:chorus_fruit", "Плод хоруса", 40000, true, "Хорус", "хорус", "Chorus Fruit");
      add(list, "minecraft:tripwire_hook", "Отмычка к сферам", 50000, true, "Отмычка к Сферам", "Отмычка");
      add(list, "minecraft:tripwire_hook", "Отмычка к ресурсам", 40000, true, "Отмычка к Ресурсам");
      add(list, "minecraft:tripwire_hook", "Отмычка к рунам", 45000, true, "Отмычка к Рунам");
      add(list, "minecraft:tripwire_hook", "Отмычка к мистикам", 50000, true, "Отмычка к Мистикам");
      add(list, "minecraft:potion", "Улучшенное зелье силы", 0, false);
      add(list, "minecraft:potion", "Улучшенное зелье скорости", 0, false);
      add(list, "minecraft:potion", "Зелье исцеления", 0, false, "Зелье исцеление");
      add(list, "minecraft:potion", "Зелье черепашьей мощи", 0, false);
      add(list, "minecraft:potion", "Зелье черепашьей мощи II", 0, false);
      add(list, "minecraft:potion", "Зелье Ассасина", 0, false);
      add(list, "minecraft:potion", "Зелье Гнева", 0, false);
      add(list, "minecraft:potion", "Зелье Палладина", 0, false);
      add(list, "minecraft:potion", "Зелье Радиации", 0, false);
      add(list, "minecraft:potion", "Снотворное", 0, false);
      add(list, "minecraft:potion", "Мандариновый сок", 0, false);
      add(list, "minecraft:potion", "Зелье Агента", 0, false);
      add(list, "minecraft:potion", "Зелье Киллера", 0, false);
      add(list, "minecraft:potion", "Зелье Медика", 0, false);
      add(list, "minecraft:potion", "Зелье Победителя", 0, false, "Зелье победителя");

      for (String name : CUSTOM) {
         add(list, "", name, 0, false);
      }

      return list;
   }

   private static void add(List<AutoBuyRule> list, String itemId, String name, int maxPrice, boolean enabled, String... aliases) {
      AutoBuyRule rule = new AutoBuyRule();
      rule.set(name, maxPrice, 1);
      rule.itemId = itemId == null ? "" : itemId;
      rule.enabled = enabled;
      rule.aliases = new ArrayList<>(Arrays.asList(aliases));
      list.add(rule);
   }
}
