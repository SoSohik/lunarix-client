package ru.mentions.command.impl;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.module.impl.display.GpsHud;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class GpsCommand extends Command {
   public GpsCommand() {
      super("gps", ".gps <set|off> <x> <z>", "Стрелка на экране ведущая к координатам");
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.showHelp();
      } else {
         String var2 = args[0].toLowerCase();
         switch (var2) {
            case "set":
               if (args.length != 3) {
                  ChatUtils.addChatMessage("§7Использование: §f.gps set <x> <z>");
                  return;
               }

               float x;
               float z;
               try {
                  x = Float.parseFloat(args[1]);
                  z = Float.parseFloat(args[2]);
               } catch (NumberFormatException var7) {
                  ChatUtils.addChatMessage("§cОшибка: неверные координаты");
                  return;
               }

               GpsHud.setTarget(x, z);
               GpsHud module = Client.get().moduleManager().get(GpsHud.class);
               if (module != null && !module.isEnabled()) {
                  module.setEnabled(true);
               }

               ChatUtils.addChatMessage("§aGPS §7→ §f" + (int)x + "§7, §f" + (int)z);
               break;
            case "off":
               GpsHud.clearTarget();
               ChatUtils.addChatMessage("§7GPS сброшен");
               break;
            default:
               this.showHelp();
         }
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String[] parts = subPrefix.split(" ", -1);
      if (parts.length <= 1) {
         return List.of("set", "off").stream().filter(s -> s.startsWith(parts[0].toLowerCase())).toList();
      } else if (!parts[0].equalsIgnoreCase("set")) {
         return List.of();
      } else {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 == null) {
            return List.of();
         } else {
            int px = (int)Math.floor(mc.field_1724.method_23317());
            int pz = (int)Math.floor(mc.field_1724.method_23321());
            if (parts.length == 2) {
               String cur = String.valueOf(px);
               return cur.startsWith(parts[1]) ? List.of("set " + cur) : List.of();
            } else if (parts.length == 3) {
               String cur = String.valueOf(pz);
               return cur.startsWith(parts[2]) ? List.of("set " + px + " " + cur) : List.of();
            } else {
               return List.of();
            }
         }
      }
   }

   private void showHelp() {
      ChatUtils.addChatMessage("§7.gps §fset §7<x> <z> §8| §7.gps §foff");
   }
}
