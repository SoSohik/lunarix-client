package ru.mentions.command.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import ru.mentions.command.Command;
import ru.mentions.module.impl.render.BlockEsp;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class BlockEspCommand extends Command {
   public BlockEspCommand() {
      super("blockesp", ".blockesp <add|remove|list|clear> [блок]", "Управление Block ESP");
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 1) {
         this.showHelp();
      } else {
         BlockEsp esp = BlockEsp.getInstance();
         if (esp == null) {
            ChatUtils.addChatMessage("§cBlock ESP модуль не найден");
         } else {
            String var3 = args[0].toLowerCase();
            switch (var3) {
               case "add":
                  if (args.length < 2) {
                     ChatUtils.addChatMessage("§7Использование: .blockesp add §f<блок>");
                     return;
                  }

                  class_2248 blockx = this.findBlock(args[1]);
                  if (blockx == null) {
                     ChatUtils.addChatMessage("§cБлок §f" + args[1] + "§c не найден");
                     return;
                  }

                  if (esp.addBlock(blockx)) {
                     esp.rescan();
                     ChatUtils.addChatMessage("§a" + args[1] + "§r добавлен в Block ESP");
                  } else {
                     ChatUtils.addChatMessage("§7" + args[1] + " уже добавлен");
                  }
                  break;
               case "remove":
                  if (args.length < 2) {
                     ChatUtils.addChatMessage("§7Использование: .blockesp remove §f<блок>");
                     return;
                  }

                  class_2248 block = this.findBlock(args[1]);
                  if (block == null) {
                     ChatUtils.addChatMessage("§cБлок §f" + args[1] + "§c не найден");
                     return;
                  }

                  if (esp.removeBlock(block)) {
                     ChatUtils.addChatMessage("§c" + args[1] + "§r удалён из Block ESP");
                  } else {
                     ChatUtils.addChatMessage("§7" + args[1] + " не был добавлен");
                  }
                  break;
               case "list":
                  if (esp.getTargetBlocks().isEmpty()) {
                     ChatUtils.addChatMessage("§7Список блоков пуст");
                  } else {
                     String names = esp.getTargetBlocks()
                        .stream()
                        .map(b -> class_7923.field_41175.method_10221(b).method_12832())
                        .collect(Collectors.joining("§7, §a"));
                     ChatUtils.addChatMessage("§7Блоки §8[" + esp.getTargetBlocks().size() + "§8]§r: §a" + names);
                  }
                  break;
               case "clear":
                  int count = esp.clearBlocks();
                  ChatUtils.addChatMessage("§7Удалено §f" + count + "§7 блоков из Block ESP");
                  break;
               default:
                  this.showHelp();
            }
         }
      }
   }

   private class_2248 findBlock(String name) {
      String raw = name.toLowerCase();
      class_2960 id = class_2960.method_12829(raw.contains(":") ? raw : "minecraft:" + raw);
      if (id != null && class_7923.field_41175.method_10250(id)) {
         class_2248 block = (class_2248)class_7923.field_41175.method_63535(id);
         return block == class_2246.field_10124 && !raw.equals("air") && !raw.equals("minecraft:air") ? null : block;
      } else {
         return null;
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String[] parts = subPrefix.split(" ", 2);
      String sub = parts[0].toLowerCase();
      if (parts.length <= 1) {
         return List.of("add", "remove", "list", "clear").stream().filter(s -> s.startsWith(sub)).collect(Collectors.toList());
      } else if (!sub.equals("add") && !sub.equals("remove")) {
         return List.of();
      } else {
         String blockPrefix = parts[1].toLowerCase();
         ArrayList<String> out = new ArrayList<>();

         for (class_2960 id : class_7923.field_41175.method_10235()) {
            String path = id.method_12832();
            if (path.startsWith(blockPrefix)) {
               out.add(sub + " " + path);
               if (out.size() < 30) {
                  continue;
               }
               break;
            }
         }

         return out;
      }
   }

   private void showHelp() {
      ChatUtils.addChatMessage("§7.blockesp add/remove §f<блок> §8| §7.blockesp list §8| §7.blockesp clear");
   }
}
