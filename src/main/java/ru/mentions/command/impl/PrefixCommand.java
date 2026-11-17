package ru.mentions.command.impl;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class PrefixCommand extends Command {
   public PrefixCommand() {
      super("prefix", ".prefix <символ>", "Изменить префикс команд");
   }

   @Override
   public void execute(String[] args) {
      if (args.length != 0 && !args[0].isEmpty()) {
         char newPrefix = args[0].charAt(0);
         Client.get().commandManager().setPrefix(newPrefix);
         ChatUtils.addChatMessage("§7Префикс изменён на: §a" + newPrefix);
      } else {
         char cur = Client.get().commandManager().getPrefix();
         ChatUtils.addChatMessage("§7Текущий префикс: §a" + cur);
         ChatUtils.addChatMessage("§7Использование: " + cur + "prefix <символ>");
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      return List.of();
   }
}
