package ru.mentions.utils.other;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class GuiMusicPlayer {
   private static final String RESOURCE = "/assets/client/sound/gui/gui_bg.wav";
   private static final int FADE_MS = 500;
   private static final int FADE_STEPS = 60;
   private static volatile Clip currentClip = null;
   private static volatile float targetVolume = 0.0F;
   private static volatile int generation = 0;

   private GuiMusicPlayer() {
   }

   public static void start(float volume) {
      Class<GuiMusicPlayer> var2 = GuiMusicPlayer.class;
      synchronized (GuiMusicPlayer.class) {
         if (currentClip == null || !currentClip.isRunning()) {
            int startGeneration = ++generation;
            targetVolume = volume;
            CompletableFuture.runAsync(() -> {
               Clip clip = null;
               AudioInputStream stream = null;

               try {
                  InputStream is = GuiMusicPlayer.class.getResourceAsStream("/assets/client/sound/gui/gui_bg.wav");
                  if (is == null) {
                     return;
                  }

                  stream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                  clip = AudioSystem.getClip();
                  clip.open(stream);
                  Class<GuiMusicPlayer> var6 = GuiMusicPlayer.class;
                  synchronized (GuiMusicPlayer.class) {
                     if (startGeneration == generation) {
                        currentClip = clip;
                        applyVolume(clip, 0.0F);
                        clip.loop(-1);
                        clip.start();
                        fadeIn(clip, volume);
                        return;
                     }

                     clip.close();
                  }
               } catch (Exception var21) {
                  var21.printStackTrace();
                  return;
               } finally {
                  try {
                     if (stream != null) {
                        stream.close();
                     }
                  } catch (Exception var19) {
                  }
               }
            });
         }
      }
   }

   public static void stop() {
      Class<GuiMusicPlayer> var1 = GuiMusicPlayer.class;
      synchronized (GuiMusicPlayer.class) {
         generation++;
         Clip clip = currentClip;
         if (clip != null) {
            clip.stop();
            clip.close();
            currentClip = null;
         }
      }
   }

   public static void setVolume(float volume) {
      targetVolume = volume;
      Clip clip = currentClip;
      if (clip != null && clip.isOpen()) {
         applyVolume(clip, volume);
      }
   }

   private static void fadeIn(Clip clip, float target) throws InterruptedException {
      long stepMs = 8L;

      for (int i = 1; i <= 60 && clip.isOpen(); i++) {
         float t = (float)i / 60.0F;
         float eased = 1.0F - (1.0F - t) * (1.0F - t);
         applyVolume(clip, eased * target);
         Thread.sleep(stepMs);
      }

      if (clip.isOpen()) {
         applyVolume(clip, target);
      }
   }

   private static void applyVolume(Clip clip, float volume) {
      if (clip.isControlSupported(Type.MASTER_GAIN)) {
         FloatControl gain = (FloatControl)clip.getControl(Type.MASTER_GAIN);
         float dB = (float)(Math.log10(volume <= 1.0E-4F ? 1.0E-4F : (double)volume) * 20.0);
         gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
      }
   }
}
