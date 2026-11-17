package ru.mentions.rpc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ServerUtil;

@Environment(EnvType.CLIENT)
public class DiscordRPCManager implements IMinecraft {
   private static DiscordRPCManager instance;
   public static final String APPLICATION_ID = "1345678901234567890";
   public static final String LARGE_IMAGE_KEY = "logo";
   public static final String LARGE_IMAGE_TEXT = "Lunarix Client v1.0";
   public static final String SMALL_IMAGE_WORLD = "world";
   public static final String SMALL_IMAGE_PVP = "pvp";
   public static final String DEFAULT_STATE = "Playing Lunarix Client";
   public static final String DEFAULT_DETAILS = "In-Game";
   private final DiscordRichPresence presence = new DiscordRichPresence();
   private volatile boolean running;
   private Thread workerThread;

   public static synchronized DiscordRPCManager getInstance() {
      if (instance == null) {
         instance = new DiscordRPCManager();
      }

      return instance;
   }

   public synchronized void start() {
      if (!this.running) {
         if (!DiscordRPC.Loader.isAvailable()) {
            System.err.println("[Lunarix] Discord RPC native library unavailable, presence disabled.");
         } else {
            DiscordRPC rpc = DiscordRPC.Loader.getInstance();
            if (rpc != null) {
               this.running = true;
               DiscordEventHandlers handlers = new DiscordEventHandlers();
               handlers.ready = user -> System.out.println("[Lunarix RPC] Connected to Discord user: " + user.username + "#" + user.discriminator);
               handlers.disconnected = (errorCode, message) -> System.out.println("[Lunarix RPC] Disconnected (" + errorCode + "): " + message);
               handlers.errored = (errorCode, message) -> System.err.println("[Lunarix RPC] Error (" + errorCode + "): " + message);
               rpc.Discord_Initialize("1345678901234567890", handlers, true, "");
               this.presence.startTimestamp = System.currentTimeMillis() / 1000L;
               this.presence.state = "Playing Lunarix Client";
               this.presence.details = "In-Game";
               this.presence.largeImageKey = "logo";
               this.presence.largeImageText = "Lunarix Client v1.0";
               this.presence.smallImageKey = "world";
               this.presence.smallImageText = "Lunarix v1.0";
               rpc.Discord_UpdatePresence(this.presence);
               this.workerThread = new Thread(() -> {
                  while (this.running && !Thread.currentThread().isInterrupted()) {
                     try {
                        rpc.Discord_RunCallbacks();
                        this.updatePresenceDynamic(rpc);
                        Thread.sleep(2000L);
                     } catch (InterruptedException var3) {
                        Thread.currentThread().interrupt();
                        break;
                     } catch (Throwable var4) {
                        System.err.println("[Lunarix RPC] Error during update tick: " + var4.getMessage());
                     }
                  }
               }, "Lunarix-Discord-RPC-Handler");
               this.workerThread.setDaemon(true);
               this.workerThread.start();
               System.out.println("[Lunarix] Discord RPC started successfully.");
            }
         }
      }
   }

   private void updatePresenceDynamic(DiscordRPC rpc) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.presence.details = "In-Game";
         if (mc.method_1542()) {
            this.presence.state = "Singleplayer";
            this.presence.smallImageKey = "world";
            this.presence.smallImageText = "Exploring World";
         } else {
            String server = ServerUtil.server;
            this.presence.state = server != null && !server.isEmpty() ? "Playing on " + server : "Playing Lunarix Client";
            this.presence.smallImageKey = "pvp";
            this.presence.smallImageText = "Competitive PvP";
         }
      } else {
         this.presence.details = "Main Menu";
         this.presence.state = "Idle";
         this.presence.smallImageKey = "world";
         this.presence.smallImageText = "In Menus";
      }

      this.presence.largeImageKey = "logo";
      this.presence.largeImageText = "Lunarix Client v1.0";
      rpc.Discord_UpdatePresence(this.presence);
   }

   public synchronized void updateActivity(String details, String state, String smallKey, String smallText) {
      if (this.running && DiscordRPC.Loader.isAvailable()) {
         DiscordRPC rpc = DiscordRPC.Loader.getInstance();
         if (rpc != null) {
            this.presence.details = details != null ? details : "In-Game";
            this.presence.state = state != null ? state : "Playing Lunarix Client";
            this.presence.largeImageKey = "logo";
            this.presence.largeImageText = "Lunarix Client v1.0";
            if (smallKey != null) {
               this.presence.smallImageKey = smallKey;
               this.presence.smallImageText = smallText;
            }

            rpc.Discord_UpdatePresence(this.presence);
         }
      }
   }

   public synchronized void stop() {
      this.running = false;
      if (this.workerThread != null) {
         this.workerThread.interrupt();
         this.workerThread = null;
      }

      if (DiscordRPC.Loader.isAvailable()) {
         DiscordRPC rpc = DiscordRPC.Loader.getInstance();
         if (rpc != null) {
            rpc.Discord_ClearPresence();
            rpc.Discord_Shutdown();
         }
      }

      System.out.println("[Lunarix] Discord RPC shut down.");
   }

   public boolean isRunning() {
      return this.running;
   }

   public DiscordRichPresence getPresence() {
      return this.presence;
   }
}
