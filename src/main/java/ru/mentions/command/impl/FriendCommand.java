package ru.mentions.command.impl;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class FriendCommand extends Command implements IMinecraft {
   private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list");

   public FriendCommand() {
      super("friend", ".friend <add|remove|list> [name]", "Список друзей");
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.showHelp();
      } else {
         String var2 = args[0].toLowerCase();
         switch (var2) {
            case "add":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("Использование: .friend add <ник>");
                  return;
               }

               String name = args[1];
               boolean ok = Client.get().friendManager().add(name);
               if (ok) {
                  ChatUtils.addChatMessage("§a" + name + "§r добавлен в друзья");
               } else {
                  ChatUtils.addChatMessage("§7" + name + " уже в друзьях");
               }
               break;
            case "remove":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("Использование: .friend remove <ник>");
                  return;
               }

               String name = args[1];
               boolean ok = Client.get().friendManager().remove(name);
               if (ok) {
                  ChatUtils.addChatMessage("§c" + name + "§r удалён из друзей");
               } else {
                  ChatUtils.addChatMessage("§7" + name + " не в друзьях");
               }
               break;
            case "list":
               Set<String> list = Client.get().friendManager().getFriends();
               if (list.isEmpty()) {
                  ChatUtils.addChatMessage("§7Список друзей пуст");
               } else {
                  String names = list.stream().collect(Collectors.joining("§7, §a"));
                  ChatUtils.addChatMessage("§7Друзья §8[" + list.size() + "§8]§r: §a" + names);
               }
               break;
            default:
               this.showHelp();
         }
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String[] parts = subPrefix.split(" ", 2);
      if (parts.length <= 1) {
         return SUBCOMMANDS.stream().filter(s -> s.startsWith(parts[0].toLowerCase())).collect(Collectors.toList());
      } else {
         String sub = parts[0].toLowerCase();
         String namePrefix = parts[1].toLowerCase(Locale.ROOT);
         if (sub.equals("add")) {
            Stream<String> online = mc.field_1687 == null ? Stream.empty() : mc.field_1687.method_18456().stream().map(p -> p.method_5477().getString());
            return online.filter(n -> !Client.get().friendManager().isFriend(n))
               .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(namePrefix))
               .map(n -> "add " + n)
               .collect(Collectors.toList());
         } else {
            return sub.equals("remove")
               ? Client.get()
                  .friendManager()
                  .getFriends()
                  .stream()
                  .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(namePrefix))
                  .map(n -> "remove " + n)
                  .collect(Collectors.toList())
               : List.of();
         }
      }
   }

   private void showHelp() {
      ChatUtils.addChatMessage("§7.friend add/remove §f<ник> §8| §7.friend list");
   }
}
