package ru.mentions.rpc;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.rpc.callbacks.DisconnectedCallback;
import ru.mentions.rpc.callbacks.ErroredCallback;
import ru.mentions.rpc.callbacks.JoinGameCallback;
import ru.mentions.rpc.callbacks.JoinRequestCallback;
import ru.mentions.rpc.callbacks.ReadyCallback;
import ru.mentions.rpc.callbacks.SpectateGameCallback;

@Environment(EnvType.CLIENT)
public class DiscordEventHandlers extends Structure {
   public DisconnectedCallback disconnected;
   public JoinRequestCallback joinRequest;
   public SpectateGameCallback spectateGame;
   public ReadyCallback ready;
   public ErroredCallback errored;
   public JoinGameCallback joinGame;

   protected List<String> getFieldOrder() {
      return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
   }
}
