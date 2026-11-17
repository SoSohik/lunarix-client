package ru.mentions.command.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3675;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.module.api.Module;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class BindCommand extends Command {
   private static final List<String> KEY_SUGGESTIONS = new ArrayList<>();

   public BindCommand() {
      super("bind", ".bind <модуль> <клавиша|none>", "Биндить модуль на клавишу");
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.showHelp();
      } else if (args[0].equalsIgnoreCase("list")) {
         List<Module> bound = Client.get().moduleManager().values().stream().filter(m -> m.getKey() > 0).collect(Collectors.toList());
         if (bound.isEmpty()) {
            ChatUtils.addChatMessage("§7Ни один модуль не забинжен");
         } else {
            String names = bound.stream().map(m -> "§a" + m.getName() + "§7[" + keyName(m.getKey()) + "§7]").collect(Collectors.joining("§7, "));
            ChatUtils.addChatMessage("§7Бинды §8[" + bound.size() + "§8]§r: " + names);
         }
      } else {
         Module module = Client.get().moduleManager().getModule(args[0]);
         if (module == null) {
            ChatUtils.addChatMessage("§cМодуль §f" + args[0] + "§c не найден");
         } else if (args.length < 2) {
            if (module.getKey() > 0) {
               ChatUtils.addChatMessage("§7" + module.getName() + " забинжен на §a" + keyName(module.getKey()));
            } else {
               ChatUtils.addChatMessage("§7" + module.getName() + " не забинжен");
            }
         } else {
            String keyArg = args[1].toLowerCase();
            if (!keyArg.equals("none") && !keyArg.equals("reset") && !keyArg.equals("-")) {
               int code = parseKey(keyArg);
               if (code <= 0) {
                  ChatUtils.addChatMessage("§cНеизвестная клавиша: §f" + args[1]);
               } else {
                  module.setKey(code);
                  ChatUtils.addChatMessage("§a" + module.getName() + "§r забинжен на §a" + keyName(code));
               }
            } else {
               module.setKey(-1);
               ChatUtils.addChatMessage("§7Бинд §f" + module.getName() + "§7 сброшен");
            }
         }
      }
   }

   private static int parseKey(String name) {
      try {
         class_306 key = class_3675.method_15981("key.keyboard." + name);
         return key.method_1444();
      } catch (Exception var2) {
         return -1;
      }
   }

   private static String keyName(int code) {
      try {
         return class_307.field_1668.method_1447(code).method_27445().getString();
      } catch (Exception var2) {
         return "?";
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String[] parts = subPrefix.split(" ", 2);
      if (parts.length <= 1) {
         String p = parts[0].toLowerCase();
         ArrayList<String> out = new ArrayList<>();
         if ("list".startsWith(p)) {
            out.add("list");
         }

         Client.get().moduleManager().values().stream().map(Module::getName).filter(n -> n.toLowerCase().startsWith(p)).forEach(out::add);
         return out;
      } else {
         String moduleName = parts[0];
         String keyPrefix = parts[1].toLowerCase();
         return KEY_SUGGESTIONS.stream().filter(k -> k.startsWith(keyPrefix)).map(k -> moduleName + " " + k).collect(Collectors.toList());
      }
   }

   private void showHelp() {
      ChatUtils.addChatMessage("§7.bind §f<модуль> <клавиша>§8 | §7.bind §f<модуль> none§8 | §7.bind list");
   }

   static {
      KEY_SUGGESTIONS.add("none");

      for (char c = 'a'; c <= 'z'; c++) {
         KEY_SUGGESTIONS.add(String.valueOf(c));
      }

      for (char var2 = '0'; var2 <= '9'; var2++) {
         KEY_SUGGESTIONS.add(String.valueOf(var2));
      }

      KEY_SUGGESTIONS.add("space");
      KEY_SUGGESTIONS.add("left.shift");
      KEY_SUGGESTIONS.add("right.shift");
      KEY_SUGGESTIONS.add("left.control");
      KEY_SUGGESTIONS.add("left.alt");

      for (int i = 1; i <= 12; i++) {
         KEY_SUGGESTIONS.add("f" + i);
      }
   }
}
