package ru.mentions.command.impl;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.command.Command;
import ru.mentions.module.impl.render.Globals;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class GlobalsCommand extends Command implements IMinecraft {
   private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list", "clear");

   public GlobalsCommand() {
      super("globals", ".globals <add|remove|list|clear> [name]", "Управление списком пользователей софта");
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.showHelp();
      } else {
         String var2 = args[0].toLowerCase(Locale.ROOT);
         switch (var2) {
            case "add":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("Использование: .globals add <ник>");
                  return;
               }

               String name = args[1];
               boolean ok = Globals.addSoftUser(name);
               if (ok) {
                  ChatUtils.addChatMessage("§a" + name + "§r добавлен в список пользователей софта");
               } else {
                  ChatUtils.addChatMessage("§7" + name + " уже в списке пользователей софта");
               }
               break;
            case "remove":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("Использование: .globals remove <ник>");
                  return;
               }

               String name = args[1];
               boolean ok = Globals.removeSoftUser(name);
               if (ok) {
                  ChatUtils.addChatMessage("§c" + name + "§r удалён из списка пользователей софта");
               } else {
                  ChatUtils.addChatMessage("§7" + name + " не найден в списке софта");
               }
               break;
            case "clear":
               Globals.clearSoftUsers();
               ChatUtils.addChatMessage("§cСписок пользователей софта очищен");
               break;
            case "list":
               Set<String> list = Globals.getSoftUsers();
               if (list.isEmpty()) {
                  ChatUtils.addChatMessage("§7Список пользователей софта пуст");
               } else {
                  String names = list.stream().collect(Collectors.joining("§7, §a"));
                  ChatUtils.addChatMessage("§7Пользователи софта §8[" + list.size() + "§8]§r: §a" + names);
               }
               break;
            default:
               this.showHelp();
         }
      }
   }

   private void showHelp() {
      ChatUtils.addChatMessage("§7.globals add/remove §f<ник> §8| §7.globals list §8| §7.globals clear");
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String[] parts = subPrefix.split(" ", 2);
      if (parts.length <= 1) {
         return SUBCOMMANDS.stream().filter(s -> s.startsWith(parts[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
      } else {
         String sub = parts[0].toLowerCase(Locale.ROOT);
         String namePrefix = parts[1].toLowerCase(Locale.ROOT);
         if (sub.equals("add")) {
            Stream<String> online = mc.field_1687 == null ? Stream.empty() : mc.field_1687.method_18456().stream().map(p -> p.method_5477().getString());
            return online.filter(n -> !Globals.isSoftUser(n))
               .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(namePrefix))
               .map(n -> "add " + n)
               .collect(Collectors.toList());
         } else {
            return sub.equals("remove")
               ? Globals.getSoftUsers()
                  .stream()
                  .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(namePrefix))
                  .map(n -> "remove " + n)
                  .collect(Collectors.toList())
               : List.of();
         }
      }
   }
}
