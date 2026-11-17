package ru.mentions.module.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Anti Bot",
   desc = "Не дает ударить бота",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AntiBot extends Module {
   private final List<class_1657> bots = new ArrayList<>();

   public static AntiBot getInstance() {
      return Instance.get(AntiBot.class);
   }

   @EventHandler
   public void onTick(EventUpdate event) {
      this.reallyWorldCheck();
   }

   private void reallyWorldCheck() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player != null && player != mc.field_1724) {
               boolean armorCheck = false;
               boolean enchantCheck = false;
               boolean armorDamagedCheck = false;
               boolean offHandCheck = player.method_6079().method_7909() == class_1802.field_8162;
               boolean equipCheck = player.method_6118(class_1304.field_6166).method_7909() == class_1802.field_8370
                  || player.method_6118(class_1304.field_6166).method_7909() == class_1802.field_8660
                  || player.method_6118(class_1304.field_6172).method_7909() == class_1802.field_8570
                  || player.method_6118(class_1304.field_6172).method_7909() == class_1802.field_8396
                  || player.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8577
                  || player.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8523
                  || player.method_6118(class_1304.field_6169).method_7909() == class_1802.field_8267
                  || player.method_6118(class_1304.field_6169).method_7909() == class_1802.field_8743;
               boolean mainHandCheck = player.method_6047().method_7909() != class_1802.field_8162;
               boolean foodCheck = player.method_7344().method_7586() == 20;

               for (class_1304 slot : new class_1304[]{class_1304.field_6166, class_1304.field_6172, class_1304.field_6174, class_1304.field_6169}) {
                  class_1799 armorPiece = player.method_6118(slot);
                  if (!armorCheck && armorPiece.method_7909() != class_1802.field_8162) {
                     armorCheck = true;
                  }

                  if (!enchantCheck && armorPiece.method_7923()) {
                     enchantCheck = true;
                  }

                  if (!armorDamagedCheck && !armorPiece.method_7986()) {
                     armorDamagedCheck = true;
                  }
               }

               if (!armorCheck || !enchantCheck || !armorDamagedCheck || !offHandCheck || !equipCheck || !mainHandCheck || !foodCheck) {
                  this.bots.remove(player);
               } else if (!this.bots.contains(player)) {
                  this.bots.add(player);
               }
            }
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.bots.clear();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.bots.clear();
   }

   public boolean isBot(class_1297 entity) {
      class_1657 player;
      return entity instanceof class_1657 && this.bots.contains(player = (class_1657)entity);
   }

   public boolean isBot(class_1309 entity) {
      class_1657 player;
      return entity instanceof class_1657 && this.bots.contains(player = (class_1657)entity);
   }

   public boolean checkBot(class_1297 entity) {
      return this.isBot(entity);
   }
}
