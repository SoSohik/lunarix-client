package ru.mentions.command.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class CfgCommand extends Command {
   private static final List<String> SUBCOMMANDS = List.of("save", "load", "list", "dir");

   public CfgCommand() {
      super("cfg", ".cfg <save|load|list|dir> [name]", "Конфиги");
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.showHelp();
      } else {
         String var2 = args[0].toLowerCase();
         switch (var2) {
            case "save":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("Использование: .cfg save <название>");
                  return;
               }

               Client.get().configManager().save(args[1]);
               ChatUtils.addChatMessage("Конфиг §a" + args[1] + "§r сохранён");
               break;
            case "load":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("Использование: .cfg load <название>");
                  return;
               }

               boolean ok = Client.get().configManager().load(args[1]);
               if (ok) {
                  ChatUtils.addChatMessage("Конфиг §a" + args[1] + "§r загружен");
               } else {
                  ChatUtils.addChatMessage("§cКонфиг §r" + args[1] + "§c не найден");
               }
               break;
            case "list":
               List<String> configs = Client.get().configManager().listConfigs();
               if (configs.isEmpty()) {
                  ChatUtils.addChatMessage("§7Нет сохранённых конфигов");
               } else {
                  ChatUtils.addChatMessage("§7Конфиги §8[" + configs.size() + "§8]§r: " + String.join("§7, §r", configs));
               }
               break;
            case "dir":
               String path = Client.get().configManager().getConfigDir().toAbsolutePath().toString();

               try {
                  new ProcessBuilder("explorer.exe", path).start();
               } catch (IOException var6) {
                  ChatUtils.addChatMessage("§cНе удалось открыть папку: " + path);
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
         String namePrefix = parts[1].toLowerCase();
         return sub.equals("load")
            ? Client.get()
               .configManager()
               .listConfigs()
               .stream()
               .filter(n -> n.toLowerCase().startsWith(namePrefix))
               .map(n -> "load " + n)
               .collect(Collectors.toList())
            : List.of();
      }
   }

   private void showHelp() {
      ChatUtils.addChatMessage("§7.cfg save/load §f<name> §8| §7.cfg list §8| §7.cfg dir");
   }
}
