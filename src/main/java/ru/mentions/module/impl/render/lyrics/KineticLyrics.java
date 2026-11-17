package ru.mentions.module.impl.render.lyrics;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.module.impl.display.interfaceimpl.MusicHud;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "3D Lyrics",
   category = Category.RENDER,
   desc = "3D слова пісень у світі навколо гравця"
)
@Environment(EnvType.CLIENT)
public class KineticLyrics extends Module {
   public static KineticLyrics INSTANCE;
   public final ModeSetting animation = new ModeSetting(this, "Анімація", "LyricFlow", "Typewriter", "PopScale", "KineticSlide", "Fade");
   public final ModeSetting colorMode = new ModeSetting(this, "Колір", "Клієнт", "Градієнт", "Білий");
   public final ModeSetting fontMode = new ModeSetting(this, "Шрифт", "Жирний", "Звичайний", "Середній");
   public final ModeSetting splitMode = new ModeSetting(this, "Розділення", "Розумне", "Повна стрічка");
   public final SliderSetting textSize = new SliderSetting(this, "Розмір тексту", 1.0F, 0.4F, 2.5F, 0.1F);
   public final SliderSetting maxLines = new SliderSetting(this, "Макс. рядків", 3.0F, 1.0F, 6.0F, 1.0F);
   public final SliderSetting minDistance = new SliderSetting(this, "Мін. дистанція", 2.0F, 1.2F, 4.0F, 0.1F);
   public final SliderSetting maxDistance = new SliderSetting(this, "Макс. дистанція", 4.5F, 2.0F, 7.0F, 0.1F);
   public final SliderSetting arcSpread = new SliderSetting(this, "Розкид кута", 70.0F, 20.0F, 85.0F, 5.0F);
   public final SliderSetting floatHeight = new SliderSetting(this, "Висота польоту", 0.5F, 0.1F, 2.0F, 0.1F);
   public final BooleanSetting autoSync = new BooleanSetting(this, "Авто-підбір затримки", true);
   public final SliderSetting timeOffset = new SliderSetting(this, "Зсув (мс)", 0.0F, -5000.0F, 5000.0F, 50.0F);
   public final BooleanSetting background = new BooleanSetting(this, "Фон тексту", true);
   public final BooleanSetting occlusion = new BooleanSetting(this, "Перешкоди", true);
   public final BooleanSetting debugMode = new BooleanSetting(this, "Режим налагодження", false);
   private static final Set<String> GENERIC_APPS = Set.of(
      "google chrome",
      "chrome",
      "firefox",
      "opera",
      "msedge",
      "microsoft edge",
      "spotify",
      "spotify free",
      "spotify premium",
      "yandex",
      "brave",
      "vivaldi",
      "discord",
      "telegram",
      "media player",
      "unknown",
      "unknown artist",
      "user"
   );
   private final List<LyricLine> rawLyrics = new ArrayList<>();
   private final List<LyricLine> lyricsQueue = new ArrayList<>();
   private final List<LyricParticle3D> activeParticles = new CopyOnWriteArrayList<>();
   private int currentLyricIndex = 0;
   private volatile long anchorMediaPosMs = 0L;
   private volatile long anchorSysTimeMs = 0L;
   private volatile long lastReportedRawPosSec = -1L;
   private volatile boolean isAudioPlaying = false;
   private volatile boolean isPlaying = false;
   private volatile long lastValidMediaTimeMs = 0L;
   private String lastTrackKey = "";
   private String currentTrackTitle = "";
   private String currentTrackArtist = "";
   private long currentTrackDurationMs = 0L;
   private long lastWindowScanMs = 0L;
   private MediaInfo cachedWindowMedia = null;
   private final ScheduledExecutorService mediaScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "lunarix-lyrics-media-poller");
      t.setDaemon(true);
      return t;
   });
   private ScheduledFuture<?> pollFuture = null;

   public KineticLyrics() {
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.resetPlaybackState();
      this.startMediaPolling();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.stopMediaPolling();
      this.resetPlaybackState();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.resetPlaybackState();
   }

   private void startMediaPolling() {
      if (this.pollFuture == null || this.pollFuture.isCancelled() || this.pollFuture.isDone()) {
         this.mediaScheduler.submit(this::pollActiveMedia);
         this.pollFuture = this.mediaScheduler.scheduleWithFixedDelay(this::pollActiveMedia, 250L, 250L, TimeUnit.MILLISECONDS);
      }
   }

   private void stopMediaPolling() {
      if (this.pollFuture != null) {
         this.pollFuture.cancel(false);
         this.pollFuture = null;
      }
   }

   private void resetPlaybackState() {
      this.activeParticles.clear();
      this.rawLyrics.clear();
      this.lyricsQueue.clear();
      this.lastTrackKey = "";
      this.currentTrackTitle = "";
      this.currentTrackArtist = "";
      this.currentTrackDurationMs = 0L;
      this.currentLyricIndex = 0;
      this.anchorMediaPosMs = 0L;
      this.anchorSysTimeMs = System.currentTimeMillis();
      this.lastReportedRawPosSec = -1L;
      this.lastValidMediaTimeMs = 0L;
      this.isAudioPlaying = false;
      this.isPlaying = false;
      this.cachedWindowMedia = null;
   }

   private void pollActiveMedia() {
      if (this.isEnabled()) {
         try {
            MediaInfo activeMedia = null;

            try {
               List<IMediaSession> sessions = MediaPlayerInfo.Instance.getMediaSessions();
               if (sessions != null && !sessions.isEmpty()) {
                  for (IMediaSession session : sessions) {
                     try {
                        MediaInfo info = session.getMedia();
                        if (info != null && (info.getTitle() != null && !info.getTitle().isBlank() || info.getArtist() != null && !info.getArtist().isBlank())) {
                           if (activeMedia == null || info.getPlaying()) {
                              activeMedia = info;
                           }

                           if (info.getPlaying()) {
                              break;
                           }
                        }
                     } catch (Throwable var7) {
                     }
                  }
               }
            } catch (Throwable var8) {
            }

            if (activeMedia == null) {
               try {
                  InterFace interFace = InterFace.getInstance();
                  if (interFace != null && interFace.getMusicHud() != null) {
                     MusicHud.MediaState ms = interFace.getMusicHud().getMediaState();
                     if (ms != null && ms.hasMedia() && ms.info() != null) {
                        MediaInfo info = ms.info();
                        if (info.getTitle() != null && !info.getTitle().isBlank()) {
                           activeMedia = info;
                        }
                     }
                  }
               } catch (Throwable var6) {
               }
            }

            if (activeMedia == null) {
               long now = System.currentTimeMillis();
               if (now - this.lastWindowScanMs > 1000L) {
                  this.lastWindowScanMs = now;
                  this.cachedWindowMedia = this.scanSpotifyWindow();
               }

               activeMedia = this.cachedWindowMedia;
            }

            if (activeMedia != null && activeMedia.getTitle() != null && !activeMedia.getTitle().isBlank()) {
               this.processMediaUpdate(activeMedia);
            }
         } catch (Throwable var9) {
         }
      }
   }

   private synchronized void processMediaUpdate(MediaInfo media) {
      long now = System.currentTimeMillis();
      this.lastValidMediaTimeMs = now;
      String title = media.getTitle().trim();
      String artist = media.getArtist() != null ? media.getArtist().trim() : "";
      if ((artist.isEmpty() || isGenericApp(artist)) && title.contains(" - ")) {
         int dash = title.indexOf(" - ");
         artist = title.substring(0, dash).trim();
         title = title.substring(dash + 3).trim();
      }

      String key = (title + " - " + artist).toLowerCase(Locale.ROOT);
      long reportedPos = media.getPosition();
      long reportedDur = media.getDuration();
      long reportedPosMs = reportedPos > 0L && reportedPos < 36000L ? reportedPos * 1000L : reportedPos;
      long reportedDurMs = reportedDur > 0L && reportedDur < 36000L ? reportedDur * 1000L : reportedDur;
      if (!key.equalsIgnoreCase(this.lastTrackKey)) {
         this.playTrack(title, artist, reportedPosMs, reportedDurMs);
      } else {
         this.isAudioPlaying = media.getPlaying();
         if (reportedPos > 0L) {
            long currentEstimatedPos = this.getEstimatedRawMediaPosMs(now);
            boolean positionSecondChanged = reportedPos != this.lastReportedRawPosSec;
            boolean seekDetected = Math.abs(currentEstimatedPos - reportedPosMs) > 1500L;
            if (positionSecondChanged || seekDetected) {
               this.lastReportedRawPosSec = reportedPos;
               this.anchorMediaPosMs = reportedPosMs;
               this.anchorSysTimeMs = now;
               if (seekDetected && !this.lyricsQueue.isEmpty()) {
                  long currentAudioTime = this.getLiveAudioTimeMs(now);
                  this.resyncQueue(currentAudioTime);
               }
            }
         }
      }
   }

   public synchronized void playTrack(String trackName, String artistName, long initialPosMs, long durationMs) {
      long now = System.currentTimeMillis();
      this.currentTrackTitle = trackName != null ? trackName.trim() : "";
      this.currentTrackArtist = artistName != null ? artistName.trim() : "";
      this.currentTrackDurationMs = durationMs;
      this.lastTrackKey = (this.currentTrackTitle + " - " + this.currentTrackArtist).toLowerCase(Locale.ROOT);
      this.activeParticles.clear();
      this.rawLyrics.clear();
      this.lyricsQueue.clear();
      this.currentLyricIndex = 0;
      this.anchorMediaPosMs = Math.max(0L, initialPosMs);
      this.anchorSysTimeMs = now;
      this.lastReportedRawPosSec = initialPosMs > 0L ? initialPosMs / 1000L : -1L;
      this.isAudioPlaying = true;
      this.isPlaying = false;
      String expectedKey = this.lastTrackKey;
      LrcLibClient.fetchLyricsAsync(this.currentTrackTitle, this.currentTrackArtist, durationMs, this.debugMode.getValue()).thenAccept(lines -> {
         synchronized (this) {
            if (this.lastTrackKey.equalsIgnoreCase(expectedKey)) {
               this.activeParticles.clear();
               this.rawLyrics.clear();
               this.lyricsQueue.clear();
               this.rawLyrics.addAll(lines);
               this.rebuildLyricsQueue();
               this.isPlaying = !this.lyricsQueue.isEmpty();
               long completionNow = System.currentTimeMillis();
               long currentAudioTime = this.getLiveAudioTimeMs(completionNow);
               this.resyncQueue(currentAudioTime);
            }
         }
      });
   }

   private void rebuildLyricsQueue() {
      this.lyricsQueue.clear();
      if (!"Розумне".equals(this.splitMode.getValue()) && !"Умное".equals(this.splitMode.getValue())) {
         this.lyricsQueue.addAll(this.rawLyrics);
      } else {
         this.lyricsQueue.addAll(LyricLineSplitter.splitLongLines(this.rawLyrics));
      }
   }

   public long getEstimatedRawMediaPosMs(long now) {
      if (!this.isAudioPlaying) {
         return this.anchorMediaPosMs;
      } else {
         long elapsed = Math.max(0L, now - this.anchorSysTimeMs);
         return this.anchorMediaPosMs + elapsed;
      }
   }

   public long getEffectiveOffsetMs() {
      long manualOffset = (long)this.timeOffset.getValue().floatValue();
      if (this.autoSync.getValue()) {
         long autoOffset = -350L;
         return autoOffset + manualOffset;
      } else {
         return manualOffset;
      }
   }

   public long getLiveAudioTimeMs(long now) {
      long rawPos = this.getEstimatedRawMediaPosMs(now);
      return Math.max(0L, rawPos + this.getEffectiveOffsetMs());
   }

   private MediaInfo scanSpotifyWindow() {
      try {
         final AtomicReference<MediaInfo> found = new AtomicReference<>(null);
         User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
            public boolean callback(HWND hWnd, Pointer data) {
               char[] windowText = new char[512];
               User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
               String title = Native.toString(windowText).trim();
               if (!title.isEmpty() && !title.equalsIgnoreCase("Spotify") && !title.startsWith("Spotify Free") && !title.startsWith("Spotify Premium")) {
                  char[] className = new char[256];
                  User32.INSTANCE.GetClassName(hWnd, className, 256);
                  String cls = Native.toString(className).trim();
                  if (cls.startsWith("Chrome_WidgetWin_") && title.contains(" - ")) {
                     int dash = title.indexOf(" - ");
                     String artist = title.substring(0, dash).trim();
                     String track = title.substring(dash + 3).trim();
                     if (!track.isEmpty()) {
                        found.set(new MediaInfo(track, artist, new byte[0], 0L, 0L, true));
                        return false;
                     }
                  }

                  return true;
               } else {
                  return true;
               }
            }
         }, null);
         return found.get();
      } catch (Throwable var2) {
         return null;
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (this.isEnabled() && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         if (this.isPlaying && now - this.lastValidMediaTimeMs > 4500L) {
            this.isPlaying = false;
            this.activeParticles.clear();
         } else {
            if (this.isPlaying && !this.lyricsQueue.isEmpty()) {
               long currentAudioTime = this.getLiveAudioTimeMs(now);
               this.update(currentAudioTime);
            }
         }
      }
   }

   private static boolean isGenericApp(String s) {
      return s != null && !s.isBlank() ? GENERIC_APPS.contains(s.trim().toLowerCase(Locale.ROOT)) : true;
   }

   private synchronized void resyncQueue(long audioTimeMs) {
      this.activeParticles.clear();
      this.currentLyricIndex = 0;
      long now = System.currentTimeMillis();
      int activeLineIndex = -1;

      for (int i = 0; i < this.lyricsQueue.size(); i++) {
         LyricLine line = this.lyricsQueue.get(i);
         long nextStartMs = i + 1 < this.lyricsQueue.size() ? this.lyricsQueue.get(i + 1).timestampMs() : line.timestampMs() + 3500L;
         long durationMs = Math.max(1400L, Math.min(5500L, nextStartMs - line.timestampMs()));
         if (audioTimeMs < line.timestampMs()) {
            break;
         }

         this.currentLyricIndex = i + 1;
         long timeSinceLine = audioTimeMs - line.timestampMs();
         if (timeSinceLine < durationMs) {
            activeLineIndex = i;
         }
      }

      if (activeLineIndex != -1 && activeLineIndex < this.lyricsQueue.size()) {
         LyricLine activeLine = this.lyricsQueue.get(activeLineIndex);
         long nextStartMsx = activeLineIndex + 1 < this.lyricsQueue.size()
            ? this.lyricsQueue.get(activeLineIndex + 1).timestampMs()
            : activeLine.timestampMs() + 3500L;
         long durationMsx = Math.max(1400L, Math.min(5500L, nextStartMsx - activeLine.timestampMs()));
         long timeSinceLine = audioTimeMs - activeLine.timestampMs();
         this.spawnLyricParticle(activeLine.text(), now - timeSinceLine, durationMsx);
      }
   }

   public synchronized void update(long audioTimeMs) {
      if (this.isEnabled() && this.isPlaying && !this.lyricsQueue.isEmpty() && mc.field_1724 != null) {
         long now;
         for (now = System.currentTimeMillis(); this.currentLyricIndex < this.lyricsQueue.size(); this.currentLyricIndex++) {
            LyricLine line = this.lyricsQueue.get(this.currentLyricIndex);
            long nextStartMs = this.currentLyricIndex + 1 < this.lyricsQueue.size()
               ? this.lyricsQueue.get(this.currentLyricIndex + 1).timestampMs()
               : line.timestampMs() + 3500L;
            long durationMs = Math.max(1400L, Math.min(5500L, nextStartMs - line.timestampMs()));
            if (audioTimeMs < line.timestampMs()) {
               break;
            }

            long timeSinceLine = audioTimeMs - line.timestampMs();
            if (timeSinceLine < durationMs) {
               this.spawnLyricParticle(line.text(), now - timeSinceLine, durationMs);
            }
         }

         this.activeParticles.removeIf(particle -> particle.isDead(now));
      }
   }

   private class_243 findNonOverlappingSpawnPos(class_243 headPos, float cameraYaw) {
      double spread = this.arcSpread.getValue().doubleValue();
      double minD = this.minDistance.getValue().doubleValue();
      double maxD = Math.max(minD + 0.5, this.maxDistance.getValue().doubleValue());
      class_243 bestPos = null;
      double maxMinDistance = -1.0;

      for (int attempt = 0; attempt < 16; attempt++) {
         double randomAngleOffset = ThreadLocalRandom.current().nextDouble(-spread, spread);
         double targetYawDeg = (double)cameraYaw + randomAngleOffset;
         double distance = ThreadLocalRandom.current().nextDouble(minD, maxD);
         double yOffset = ThreadLocalRandom.current().nextDouble(-0.25, 0.55);
         double yawRad = Math.toRadians(targetYawDeg);
         double dirX = -Math.sin(yawRad);
         double dirZ = Math.cos(yawRad);
         double spawnX = headPos.field_1352 + dirX * distance;
         double spawnY = headPos.field_1351 + yOffset;
         double spawnZ = headPos.field_1350 + dirZ * distance;
         class_243 candidate = new class_243(spawnX, spawnY, spawnZ);
         if (this.activeParticles.isEmpty()) {
            return candidate;
         }

         double minDistanceToOthers = Double.MAX_VALUE;

         for (LyricParticle3D active : this.activeParticles) {
            double d = candidate.method_1022(active.getBasePosition());
            if (d < minDistanceToOthers) {
               minDistanceToOthers = d;
            }
         }

         if (minDistanceToOthers >= 1.35) {
            return candidate;
         }

         if (minDistanceToOthers > maxMinDistance) {
            maxMinDistance = minDistanceToOthers;
            bestPos = candidate;
         }
      }

      return bestPos;
   }

   private void spawnLyricParticle(String text, long spawnTimeMs, long durationMs) {
      if (mc.field_1724 != null && text != null && !text.isBlank() && mc.field_1773 != null) {
         long now = System.currentTimeMillis();

         for (LyricParticle3D p : this.activeParticles) {
            if (p.getText().equalsIgnoreCase(text) && !p.isDead(now)) {
               return;
            }
         }

         class_4184 camera = mc.field_1773.method_19418();
         if (camera != null) {
            class_243 headPos = mc.field_1724.method_33571();
            float cameraYaw = camera.method_19330();
            int maxAllowed = (int)this.maxLines.getValue().floatValue();

            while (this.activeParticles.size() >= maxAllowed && !this.activeParticles.isEmpty()) {
               this.activeParticles.remove(0);
            }

            class_243 spawnPos = this.findNonOverlappingSpawnPos(headPos, cameraYaw);
            if (spawnPos != null) {
               float riseHeight = this.floatHeight.getValue();
               this.activeParticles.add(new LyricParticle3D(text, spawnPos, spawnTimeMs, durationMs, riseHeight));
            }
         }
      }
   }

   private Font getSelectedFont() {
      String f = this.fontMode.getValue();
      if ("Середній".equals(f) || "Средний".equals(f)) {
         return Fonts.sf_medium;
      } else {
         return !"Звичайний".equals(f) && !"Обычный".equals(f) ? Fonts.sf_bold : Fonts.sf_regular;
      }
   }

   private int getActiveColorRgb() {
      String col = this.colorMode.getValue();
      if ("Градієнт".equals(col) || "Градиент".equals(col)) {
         return ColorUtil.fade(8, 0, ColorUtil.client(), ColorUtil.getColor(255, 255, 255));
      } else {
         return !"Білий".equals(col) && !"Белый".equals(col) ? ColorUtil.client() : 16777215;
      }
   }

   @EventHandler(
      priority = 400
   )
   public void onRenderDisplay(EventDisplay event) {
      if (this.isEnabled() && mc.field_1724 != null) {
         Font font = this.getSelectedFont();
         String anim = this.animation.getValue();
         int color = this.getActiveColorRgb();
         boolean bg = this.background.getValue();
         boolean occ = this.occlusion.getValue();
         float txtScale = this.textSize.getValue();
         long now = System.currentTimeMillis();

         for (LyricParticle3D particle : this.activeParticles) {
            particle.render(font, anim, color, bg, occ, txtScale, now);
         }

         if (this.debugMode.getValue()) {
            long displayTimeMs = this.getLiveAudioTimeMs(now);
            long rawPosMs = this.getEstimatedRawMediaPosMs(now);
            long effectiveOffset = this.getEffectiveOffsetMs();
            String titleStr = "Трек: " + (this.currentTrackTitle.isEmpty() ? "Немає" : this.currentTrackTitle + " - " + this.currentTrackArtist);
            String timerInfo = String.format(
               "Час аудіо: %02d:%02d.%03d (Сирий: %d ms, Зсув: %+d ms)",
               displayTimeMs / 60000L,
               displayTimeMs % 60000L / 1000L,
               displayTimeMs % 1000L,
               rawPosMs,
               effectiveOffset
            );
            String autoSyncInfo = String.format(
               "Авто-підбір: %s (База: %s)", this.autoSync.getValue() ? "Увімкнено (-350ms)" : "Вимкнено", this.isAudioPlaying ? "Відтворення" : "Пауза"
            );
            String particlesInfo = String.format(
               "3D Часточки: %d / %d (Масштаб: %.1fx)", this.activeParticles.size(), (int)this.maxLines.getValue().floatValue(), txtScale
            );
            String statusInfo = String.format("Черга слів: %d / %d рядків", this.currentLyricIndex, this.lyricsQueue.size());
            float x = 10.0F;
            float y = 70.0F;
            Fonts.sf_bold.draw("[3D Lyrics Debug]", x, y, 7.5F, ColorUtil.getColor(255, 90, 90, 255));
            Fonts.sf_medium.draw(titleStr, x, y + 10.0F, 6.5F, ColorUtil.getColor(220, 220, 220, 255));
            Fonts.sf_medium.draw(timerInfo, x, y + 19.0F, 6.5F, ColorUtil.getColor(255, 255, 255, 255));
            Fonts.sf_medium.draw(autoSyncInfo, x, y + 28.0F, 6.5F, ColorUtil.getColor(100, 230, 255, 255));
            Fonts.sf_medium.draw(particlesInfo, x, y + 37.0F, 6.5F, ColorUtil.getColor(120, 255, 120, 255));
            Fonts.sf_medium.draw(statusInfo, x, y + 46.0F, 6.5F, ColorUtil.getColor(180, 180, 255, 255));
         }
      }
   }
}
