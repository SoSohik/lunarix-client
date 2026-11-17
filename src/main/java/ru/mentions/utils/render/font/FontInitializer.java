package ru.mentions.utils.render.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mentions.Client;

@Environment(EnvType.CLIENT)
public class FontInitializer {
   private static final Logger LOGGER = LoggerFactory.getLogger("client/FontInitializer");
   private static boolean registered = false;
   private static boolean initialized = false;

   public static void register() {
      if (!registered) {
         registered = true;
         ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
            if (!initialized && client.method_1478() != null && client.method_22683() != null) {
               try {
                  FontRenderer fontRenderer = Client.get().render2D().getFontRenderer();
                  if (fontRenderer != null && !fontRenderer.isInitialized()) {
                     fontRenderer.initialize();
                     initialized = true;
                     LOGGER.info("Fonts initialized successfully");
                  }
               } catch (Exception var2) {
                  LOGGER.error("Failed to initialize fonts", var2);
               }
            }
         });
      }
   }

   public static boolean isInitialized() {
      return initialized;
   }
}
