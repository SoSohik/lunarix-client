package ru.mentions.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventScreenKey;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public final class AutoBuyKeys {
   private static boolean installed = false;

   private AutoBuyKeys() {
   }

   public static void install() {
      if (!installed) {
         installed = true;
         Client.eventHandler().subscribe(new AutoBuyKeys());
      }
   }

   @EventHandler
   public void onScreenKey(EventScreenKey event) {
      AutoBuy autoBuy = AutoBuy.get();
      if (autoBuy != null) {
         int key = autoBuy.toggleBind.getValue();
         if (key > 0 && key == event.getKey()) {
            autoBuy.toggle();
            if (autoBuy.report.getValue()) {
               ChatUtils.addChatMessage("[AutoBuy] " + (autoBuy.isEnabled() ? "включён" : "выключен"));
            }

            return;
         }
      }

      AutoParser parser = AutoParser.get();
      if (parser != null) {
         int parserKey = parser.toggleBind.getValue();
         if (parserKey > 0 && parserKey == event.getKey()) {
            if (parser.isEnabled()) {
               parser.setEnabled(false);
               if (parser.report.getValue()) {
                  ChatUtils.addChatMessage("[AutoParser] Остановлен.");
               }

               return;
            }

            parser.reset();
            parser.setEnabled(true);
            if (parser.report.getValue()) {
               ChatUtils.addChatMessage("[AutoParser] Запущен.");
            }
         }
      }
   }
}
