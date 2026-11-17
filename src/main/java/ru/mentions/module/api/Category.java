package ru.mentions.module.api;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.lang.Lang;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;

@Environment(EnvType.CLIENT)
public enum Category {
   COMBAT("Combat", "B"),
   MOVEMENT("Movement", "H"),
   PLAYER("Player", "N"),
   OTHER("Misc", "L"),
   RENDER("Render", "Q"),
   PASTED("Pasted", "P"),
   THEMES("Themes", "E"),
   SETTINGS("Settings", "G");

   private final String name;
   private final String icon;
   public Animation alphaS = new EaseInOutQuad(300, 1.0);
   public Animation alphaS2 = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.Animation animation = new ru.mentions.utils.animation.Animation();

   @Generated
   public String getName() {
      return this.name;
   }

   public String getDisplayName() {
      switch (this) {
         case COMBAT:
            return Lang.pick("Бій", "Бой", "Combat");
         case MOVEMENT:
            return Lang.pick("Рух", "Движение", "Movement");
         case PLAYER:
            return Lang.pick("Гравець", "Игрок", "Player");
         case OTHER:
            return Lang.pick("Інше", "Другое", "Misc");
         case RENDER:
            return Lang.pick("Рендер", "Рендер", "Render");
         case PASTED:
            return "Pasted";
         case THEMES:
            return Lang.pick("Теми", "Темы", "Themes");
         case SETTINGS:
            return Lang.pick("Налаштування", "Настройки", "Settings");
         default:
            return this.name;
      }
   }

   @Generated
   public String getIcon() {
      return this.icon;
   }

   @Generated
   public Animation getAlphaS() {
      return this.alphaS;
   }

   @Generated
   public Animation getAlphaS2() {
      return this.alphaS2;
   }

   @Generated
   public ru.mentions.utils.animation.Animation getAnimation() {
      return this.animation;
   }

   @Generated
   private Category(String name, String icon) {
      this.name = name;
      this.icon = icon;
   }
}
