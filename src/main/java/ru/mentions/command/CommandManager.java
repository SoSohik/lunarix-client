package ru.mentions.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.command.impl.BindCommand;
import ru.mentions.command.impl.BlockEspCommand;
import ru.mentions.command.impl.CfgCommand;
import ru.mentions.command.impl.FriendCommand;
import ru.mentions.command.impl.GlobalsCommand;
import ru.mentions.command.impl.GpsCommand;
import ru.mentions.command.impl.PrefixCommand;
import ru.mentions.command.impl.SpecCommand;
import ru.mentions.command.impl.WayCommand;
import ru.mentions.maintenance.DiagCommand;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class CommandManager {
   private char prefix = '.';
   private final Map<String, Command> commands = new LinkedHashMap<>();

   public void init() {
      this.register(new CfgCommand());
      this.register(new FriendCommand());
      this.register(new PrefixCommand());
      this.register(new BlockEspCommand());
      this.register(new BindCommand());
      this.register(new GpsCommand());
      this.register(new WayCommand());
      this.register(new SpecCommand());
      this.register(new DiagCommand());
      GlobalsCommand globalsCmd = new GlobalsCommand();
      this.register(globalsCmd);
      this.commands.put("global", globalsCmd);
   }

   public void register(Command command) {
      this.commands.put(command.getName().toLowerCase(), command);
   }

   public char getPrefix() {
      return this.prefix;
   }

   public void setPrefix(char prefix) {
      this.prefix = prefix;
   }

   public boolean handleMessage(String message) {
      if (!message.isEmpty() && message.charAt(0) == this.prefix) {
         String[] parts = message.substring(1).trim().split("\\s+");
         if (parts.length != 0 && !parts[0].isEmpty()) {
            String commandName = parts[0].toLowerCase();
            Command command = this.commands.get(commandName);
            if (command == null) {
               ChatUtils.addChatMessage("§cНеизвестная команда: §r" + commandName);
               this.showAll();
               return true;
            } else {
               command.execute(Arrays.copyOfRange(parts, 1, parts.length));
               return true;
            }
         } else {
            this.showAll();
            return true;
         }
      } else {
         return false;
      }
   }

   public Map<String, Command> getCommands() {
      return this.commands;
   }

   public List<String> getSuggestions(String input) {
      if (!input.isEmpty() && input.charAt(0) == this.prefix) {
         String without = input.substring(1);
         if (!without.contains(" ")) {
            return this.commands
               .entrySet()
               .stream()
               .filter(e -> e.getKey().startsWith(without.toLowerCase()))
               .map(e -> this.prefix + e.getKey())
               .collect(Collectors.toList());
         } else {
            String[] parts = without.split(" ", 2);
            String cmdName = parts[0].toLowerCase();
            String subPrefix = parts.length > 1 ? parts[1] : "";
            Command cmd = this.commands.get(cmdName);
            return cmd == null
               ? Collections.emptyList()
               : cmd.getSuggestions(subPrefix).stream().map(s -> this.prefix + cmdName + " " + s).collect(Collectors.toList());
         }
      } else {
         return Collections.emptyList();
      }
   }

   private void showAll() {
      String list = this.commands.keySet().stream().map(n -> "§a" + this.prefix + n).collect(Collectors.joining("§7, "));
      ChatUtils.addChatMessage("§7Команды: " + list);
   }
}
