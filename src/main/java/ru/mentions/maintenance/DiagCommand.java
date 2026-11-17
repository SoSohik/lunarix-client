package ru.mentions.maintenance;

import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import ru.mentions.command.Command;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public final class DiagCommand extends Command implements IMinecraft {
   private static final List<String> ACTIONS = List.of("screen", "run", "live", "offline", "status", "logs", "selftest");

   public DiagCommand() {
      super("diag", ".diag [screen|run|live|offline|status|logs|selftest]", "Діагностика та очищення модуля");
   }

   @Override
   public void execute(String[] args) {
      String action = args.length == 0 ? "screen" : args[0].toLowerCase(Locale.ROOT);
      switch (action) {
         case "screen":
            this.openScreen();
            break;
         case "run":
            ChatUtils.addChatMessage("§7cleanup: §f" + CleanupPipeline.runAll());
            break;
         case "live":
            ChatUtils.addChatMessage("§7live: §f" + CleanupPipeline.runLive());
            break;
         case "offline":
            ModuleState.setShutdownRequested(true);
            ChatUtils.addChatMessage("§7offline: §f" + CleanupPipeline.runOffline());
            mc.method_1592();
            break;
         case "status":
            ChatUtils.addChatMessage(
               "§7mode: §f"
                  + ModuleState.modeLabel()
                  + " §8| §7lock: §f"
                  + ModuleState.fileLockState()
                  + " §8| §7jar: §f"
                  + ModuleState.jarState()
                  + " §8| §7logs: §f"
                  + ModuleState.logsDone()
                  + "/"
                  + ModuleState.logsTotal()
                  + " §8| §7buffer: §f"
                  + DiagLog.size()
                  + "/256"
            );
            break;
         case "logs":
            ChatUtils.addChatMessage("§7logs: §f" + CleanupPipeline.runLogsOnly());
            break;
         case "selftest":
            ChatUtils.addChatMessage("§7" + CleanupPipeline.selftest());
            break;
         default:
            ChatUtils.addChatMessage("§7" + this.getUsage());
      }
   }

   private void openScreen() {
      if (ModuleState.newCleanupScreen() instanceof class_437 value) {
         mc.method_1507(value);
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String prefix = subPrefix.toLowerCase(Locale.ROOT);
      return ACTIONS.stream().filter(value -> value.startsWith(prefix)).toList();
   }
}
