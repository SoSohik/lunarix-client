package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2797;
import net.minecraft.class_7472;
import net.minecraft.class_9449;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Pvp Safe",
   category = Category.PLAYER,
   desc = "Во время ПВП блокирует выход с сервера и команды"
)
@Environment(EnvType.CLIENT)
public class PvpSafe extends Module {
   public static PvpSafe get() {
      return Instance.get(PvpSafe.class);
   }

   public static boolean isActive() {
      PvpSafe module = get();
      return module != null && module.isEnabled() && ServerUtil.isPvp();
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (this.isEnabled() && event.isSend() && ServerUtil.isPvp()) {
         String text = null;
         if (event.getPacket() instanceof class_2797 packet) {
            text = packet.comp_945();
         } else if (event.getPacket() instanceof class_9449 packet) {
            text = packet.comp_2532();
         } else if (event.getPacket() instanceof class_7472 packet) {
            text = packet.comp_808();
         }

         if (text != null && isBlocked(text)) {
            event.cancel();
         }
      }
   }

   public static boolean isBlocked(String raw) {
      if (raw != null && !raw.isEmpty()) {
         String s = raw.toLowerCase().trim();
         if (s.startsWith("/")) {
            s = s.substring(1).trim();
         }

         String cmd;
         return !(cmd = s.split("\\s+", 2)[0]).equals("hub") && !cmd.equals("limbo") && !cmd.equals("anarchy") && !cmd.startsWith("an")
            ? s.matches(".*\\b(hub|limbo|anarchy)\\b.*")
            : true;
      } else {
         return false;
      }
   }
}
