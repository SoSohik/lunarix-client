package ru.mentions.rpc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public class RPC implements IMinecraft {
   public static DiscordRichPresence presence = DiscordRPCManager.getInstance().getPresence();
   public static boolean started;

   public void startRpc() {
      DiscordRPCManager.getInstance().start();
      started = DiscordRPCManager.getInstance().isRunning();
      presence = DiscordRPCManager.getInstance().getPresence();
   }

   public void stopRpc() {
      DiscordRPCManager.getInstance().stop();
      started = false;
   }
}
