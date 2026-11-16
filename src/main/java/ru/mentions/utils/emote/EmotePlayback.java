package ru.mentions.utils.emote;

import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11890;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class EmotePlayback extends PlayerAnimationController implements IMinecraft {
   private static final Logger LOGGER = LoggerFactory.getLogger("client/Emotes");
   private final EmoteMetadata emote;
   private static EmotePlayback active = null;

   public EmotePlayback(class_11890 player, EmoteMetadata emote, int startTick) {
      super(player, (controller, state, animationSetter) -> PlayState.STOP);
      this.emote = emote;
      this.triggerAnimation(emote.animation(), (float)Math.max(0, startTick));
   }

   public EmoteMetadata emote() {
      return this.emote;
   }

   public static boolean isPlaying() {
      return active != null && active.isActive();
   }

   @Override
   public boolean canRemove() {
      return !this.isActive();
   }

   public static String playingId() {
      return active == null ? null : active.emote().id();
   }

   public static void play(String id) {
      if (mc.field_1724 == null) {
         LOGGER.warn("[Emotes] play('{}') skipped: player is null", id);
      } else {
         EmoteMetadata meta = EmoteLibrary.resolve(id);
         if (meta == null) {
            LOGGER.warn("[Emotes] play('{}') skipped: emote not found (loaded: {})", id, EmoteLibrary.size());
         } else {
            stopEmote();

            try {
               AvatarAnimManager manager = PlayerAnimationAccess.getPlayerAnimManager(mc.field_1724);
               if (manager == null) {
                  LOGGER.warn("[Emotes] manager is null for player, cannot play '{}'", id);
                  return;
               }

               EmotePlayback playback = new EmotePlayback(mc.field_1724, meta, 0);
               manager.addAnimLayer(2000, playback);
               active = playback;
               LOGGER.info("[Emotes] started '{}' (layers: {})", meta.id(), manager.getLayers().size());
            } catch (Throwable var4) {
               LOGGER.error("[Emotes] play('{}') failed", id, var4);
            }
         }
      }
   }

   public static void stopEmote() {
      EmotePlayback current = active;
      active = null;
      if (current != null && mc.field_1724 != null) {
         try {
            AvatarAnimManager manager = PlayerAnimationAccess.getPlayerAnimManager(mc.field_1724);
            if (manager != null) {
               manager.removeLayer(current);
            }
         } catch (Throwable var2) {
            LOGGER.warn("[Emotes] stop failed: {}", var2.toString());
         }
      }
   }
}
