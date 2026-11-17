package ru.mentions.cosmetics;

public enum CosmeticType {
   CAPE("Плащ", "Плащи"),
   WINGS("Крылья", "Крылья"),
   BACKPACK("Рюкзак", "Рюкзаки"),
   PET("Питомец", "Питомцы"),
   HAT("Головной убор", "Головные уборы");

   private final String title;
   private final String plural;

   private CosmeticType(String title, String plural) {
      this.title = title;
      this.plural = plural;
   }

   public String title() {
      return this.title;
   }

   public String plural() {
      return this.plural;
   }

   public boolean attachesToHead(int position) {
      return this == HAT && position == 2;
   }
}
