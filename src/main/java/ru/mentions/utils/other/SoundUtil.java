package ru.mentions.utils.other;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.FloatControl.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SoundUtil {
   private static final CopyOnWriteArrayList<Clip> CLIPS_LIST = new CopyOnWriteArrayList<>();

   public static void playSound_wav(String location, float volume) {
      playSound_wav(location, volume, 1.0F);
   }

   public static void playSound_wav(String location, float volume, float pitch) {
      CompletableFuture.runAsync(() -> {
         try {
            cleanUpClips();
            String resourcePath = "/assets/client/sound/" + location + ".wav";
            InputStream inputStream = SoundUtil.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
               return;
            }

            try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream))) {
               Clip clip = AudioSystem.getClip();
               if (Math.abs(pitch - 1.0F) < 0.001F) {
                  clip.open(stream);
               } else {
                  openPitched(clip, stream, pitch);
               }

               if (clip.isControlSupported(Type.MASTER_GAIN)) {
                  FloatControl volumeControl = (FloatControl)clip.getControl(Type.MASTER_GAIN);
                  float volumeVal = Math.max(0.0F, Math.min(1.0F, volume));
                  float dB = (float)(Math.log10(volumeVal <= 0.0F ? 1.0E-4 : (double)volumeVal) * 20.0);
                  volumeControl.setValue(dB);
               }

               clip.addLineListener(event -> {
                  if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                     clip.close();
                     CLIPS_LIST.remove(clip);
                  }
               });
               CLIPS_LIST.add(clip);
               clip.start();
            }
         } catch (Exception var12) {
            var12.printStackTrace();
         }
      });
   }

   private static void openPitched(Clip clip, AudioInputStream stream, float pitch) throws Exception {
      float p = Math.max(0.5F, Math.min(2.0F, pitch));
      AudioInputStream pcm = stream;
      AudioFormat src = stream.getFormat();
      if (src.getEncoding() != Encoding.PCM_SIGNED) {
         pcm = AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, stream);
         src = pcm.getFormat();
      }

      byte[] data = pcm.readAllBytes();
      if (pcm != stream) {
         pcm.close();
      }

      int frameSize;
      if ((frameSize = src.getFrameSize()) <= 0) {
         clip.open(src, data, 0, data.length);
      } else {
         int inFrames = data.length / frameSize;
         int outFrames = (int)((float)inFrames / p);
         if (outFrames <= 0) {
            clip.open(src, data, 0, data.length);
         } else {
            byte[] out = new byte[outFrames * frameSize];

            for (int i = 0; i < outFrames; i++) {
               int srcFrame = Math.min(inFrames - 1, (int)((float)i * p));
               System.arraycopy(data, srcFrame * frameSize, out, i * frameSize, frameSize);
            }

            clip.open(src, out, 0, out.length);
         }
      }
   }

   private static void cleanUpClips() {
      CLIPS_LIST.removeIf(clip -> !clip.isOpen());
   }
}
