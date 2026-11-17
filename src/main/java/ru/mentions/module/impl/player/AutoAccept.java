package ru.mentions.module.impl.player;

import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_7439;
import net.minecraft.class_2558.class_10609;
import net.minecraft.class_2558.class_10610;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;

@ModuleInfo(
   name = "Auto Accept",
   category = Category.PLAYER,
   desc = "Автоматически принимает запросы"
)
@Environment(EnvType.CLIENT)
public class AutoAccept extends Module {
   public final MultiBooleanSetting type = new MultiBooleanSetting(
      this, "Принимать", new BooleanSetting("Запрос на ТП", true), new BooleanSetting("Запрос в клан", true)
   );
   public final ModeSetting mode = new ModeSetting(this, "Принимать ТП от", "Друзей", "Всех").setVisible(() -> this.type.get("Запрос на ТП").getValue());
   public final BooleanSetting onlyFriends = new BooleanSetting("Принимать запрос в клан только от друзей", true)
      .setVisible(() -> this.type.get("Запрос в клан").getValue());
   private boolean canAccept;
   private boolean waitingForClan = false;
   private long clanInviteTime = 0L;
   private final String[] teleportMessages = new String[]{
      "has requested teleport", "просит телепортироваться", "хочет телепортироваться к вам", "просит к вам телепортироваться"
   };
   private final String[] clanMessage = new String[]{"приглашает вас в клан", "приглашает Вас в клан", "invited you to clan"};

   @EventHandler
   public void onPacket(EventPacket packetEvent) {
      class_2596<?> class_25962;
      if (mc.field_1724 != null && mc.field_1687 != null && (class_25962 = packetEvent.getPacket()) instanceof class_7439) {
         class_7439 m = (class_7439)class_25962;
         class_2561 content = m.comp_763();
         String message = content.getString();
         if (this.waitingForClan) {
            if (System.currentTimeMillis() - this.clanInviteTime > 5000L) {
               this.waitingForClan = false;
            } else {
               String cmd = this.findClickCommand(content, "Вступить");
               if (cmd != null) {
                  this.runCommand(cmd);
                  this.waitingForClan = false;
                  return;
               }
            }
         }

         if (this.type.getValue("Запрос на ТП") && this.isTeleportMessage(message)) {
            if (this.mode.is("Всех")) {
               this.canAccept = true;
            } else {
               String lowerMessage = message.toLowerCase();

               for (String friendName : Client.get().friendManager().getFriends()) {
                  if (lowerMessage.contains(friendName.toLowerCase())) {
                     this.canAccept = true;
                     break;
                  }
               }
            }
         }

         if (this.type.getValue("Запрос в клан") && this.isClanMessage(message)) {
            String playerName = this.extractName(message, "приглашает");
            if (playerName != null && !this.checkFriend(playerName)) {
               return;
            }

            String cmd = this.findClickCommand(content, "Вступить");
            if (cmd != null) {
               this.runCommand(cmd);
            } else {
               this.waitingForClan = true;
               this.clanInviteTime = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventUpdate e) {
      if (this.canAccept && mc.field_1724 != null) {
         mc.field_1724.field_3944.method_45730("tpaccept");
         this.canAccept = false;
      }
   }

   private String findClickCommand(class_2561 text, String buttonText) {
      if (text == null) {
         return null;
      } else {
         if (text.method_10866() != null && text.method_10866().method_10970() != null) {
            class_2558 ce = text.method_10866().method_10970();
            if (text.getString().contains(buttonText)) {
               if (ce instanceof class_10609 runCommand) {
                  return runCommand.comp_3506();
               }

               if (ce instanceof class_10610 suggestCommand) {
                  return suggestCommand.comp_3507();
               }
            }
         }

         List siblings;
         if ((siblings = text.method_10855()) != null) {
            for (Object siblingValue : siblings) {
               class_2561 sibling = (class_2561)siblingValue;
               String cmd = this.findClickCommand(sibling, buttonText);
               if (cmd != null) {
                  return cmd;
               }
            }
         }

         return null;
      }
   }

   private void runCommand(String cmd) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         if (cmd.startsWith("/")) {
            mc.field_1724.field_3944.method_45730(cmd.substring(1));
         } else {
            mc.field_1724.field_3944.method_45730(cmd);
         }
      }
   }

   private boolean checkFriend(String playerName) {
      return !this.onlyFriends.getValue() ? true : Client.get().friendManager().isFriend(playerName);
   }

   private String extractName(String message, String keyword) {
      String clean = message.replaceAll("§.", "");
      int index = clean.indexOf(keyword);
      if (index <= 0) {
         return null;
      } else {
         String before = clean.substring(0, index).trim();
         int lastSpace = before.lastIndexOf(32);
         return lastSpace >= 0 ? before.substring(lastSpace + 1).trim() : before.trim();
      }
   }

   private boolean isTeleportMessage(String message) {
      String lowerCaseMessage = message.toLowerCase();
      return Arrays.stream(this.teleportMessages).anyMatch(trigger -> lowerCaseMessage.contains(trigger.toLowerCase()));
   }

   private boolean isClanMessage(String message) {
      String lowerCaseMessage = message.toLowerCase();
      return Arrays.stream(this.clanMessage).anyMatch(trigger -> lowerCaseMessage.contains(trigger.toLowerCase()));
   }
}
