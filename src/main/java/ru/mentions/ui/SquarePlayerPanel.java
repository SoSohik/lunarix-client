package ru.mentions.ui;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.MediaKeys;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.MediaTextureUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class SquarePlayerPanel {
   private static final String PREF_VOLUME_KEY = "square_player_volume";
   private static final Preferences PREFS = Preferences.userNodeForPackage(SquarePlayerPanel.class);
   private volatile IMediaSession activeSession;
   private volatile MediaInfo currentMedia;
   private byte[] lastTrackArtBytes = new byte[0];
   private float volume = PREFS.getFloat("square_player_volume", 70.0F);
   private boolean volumeSliderOpen = false;
   private boolean draggingVolume = false;
   private final Animation volumeSlideAnim = new Animation();
   private final Animation coverFadeAnim = new Animation();
   private int accentColor = -11500801;
   private int iconColor = -1;
   private float lastX;
   private float lastY;
   private float lastSize;
   private float prevBtnX;
   private float prevBtnY;
   private float playBtnX;
   private float playBtnY;
   private float nextBtnX;
   private float nextBtnY;
   private float stopBtnX;
   private float stopBtnY;
   private float volBtnX;
   private float volBtnY;
   private float btnRadius;
   private float volSliderX;
   private float volSliderY;
   private float volSliderW;
   private float volSliderH;

   public SquarePlayerPanel() {
      this.coverFadeAnim.run(1.0, 0.2, Easings.CUBIC_OUT, true);
   }

   public void updateEngine() {
      try {
         List<IMediaSession> sessions = MediaPlayerInfo.Instance.getMediaSessions();
         this.activeSession = sessions.stream().max(Comparator.comparingInt(s -> s.getMedia().getPlaying() ? 1 : 0)).orElse(null);
         if (this.activeSession != null) {
            MediaInfo info = this.activeSession.getMedia();
            if (this.currentMedia == null || !info.getTitle().equals(this.currentMedia.getTitle()) || !info.getArtist().equals(this.currentMedia.getArtist())) {
               this.currentMedia = info;
               byte[] png = info.getArtworkPng();
               if (png != null && png.length > 0) {
                  this.lastTrackArtBytes = png;
                  this.accentColor = extractAccentColor(png);
                  this.iconColor = getContrastingIconColor(this.accentColor);
                  this.coverFadeAnim.setValue(0.0);
                  this.coverFadeAnim.run(1.0, 0.2, Easings.CUBIC_OUT);
                  MediaTextureUtil.updateArtwork(png);
               }
            }
         }
      } catch (Throwable var4) {
      }
   }

   public void render(class_332 context, float x, float y, float size, float mouseX, float mouseY, float delta) {
      this.updateEngine();
      this.lastX = x;
      this.lastY = y;
      this.lastSize = size;
      float cornerRadius = class_3532.method_15363(size * 0.035F, 8.0F, 16.0F);
      float borderThickness = 1.0F;
      this.coverFadeAnim.update();
      float coverAlpha = this.coverFadeAnim.get();
      if (MediaTextureUtil.hasArtwork()) {
         RenderUtil.Images.texture(MediaTextureUtil.ARTWORK_ID, x, y, size, size, coverAlpha, cornerRadius, ColorUtil.getColor(255));
      } else {
         Draw.rect(x, y, size, size, ColorUtil.getColor(20, 20, 24, 255), cornerRadius);
         Fonts.sf_medium.drawCentered("No Media Playing", x + size / 2.0F, y + size * 0.42F, size * 0.045F, ColorUtil.getColor(180));
      }

      Draw.outline(x, y, size, size, borderThickness, ColorUtil.getColor(255, 255, 255, 32), cornerRadius);
      float scrimHeight = size * 0.24F;
      float scrimY = y + size - scrimHeight;
      int scrimBottom = ColorUtil.getColor(0, 0, 0, 185);
      int scrimTop = ColorUtil.getColor(0, 0, 0, 0);
      Draw.gradientRect(x, scrimY, size, scrimHeight, new int[]{scrimTop, scrimTop, scrimBottom, scrimBottom}, 0.0F, 0.0F, cornerRadius, cornerRadius);
      if (this.currentMedia != null && !this.currentMedia.getTitle().isEmpty()) {
         String title = this.currentMedia.getTitle();
         String artist = this.currentMedia.getArtist();
         float titleSize = size * 0.042F;
         float artistSize = size * 0.034F;
         Fonts.sf_medium.drawCentered(title, x + size / 2.0F, y + size - scrimHeight + size * 0.02F, titleSize, ColorUtil.getColor(255, 240));
         if (!artist.isEmpty()) {
            Fonts.sf_regular.drawCentered(artist, x + size / 2.0F, y + size - scrimHeight + size * 0.065F, artistSize, ColorUtil.getColor(200, 180));
         }
      }

      this.renderControlRow(x, y, size, mouseX, mouseY);
      this.renderVolumeSlider(x, y, size, mouseX, mouseY);
   }

   private void renderControlRow(float x, float y, float size, float mouseX, float mouseY) {
      float controlsCenterY = y + size - size * 0.085F;
      this.btnRadius = size * 0.048F;
      float btnDiameter = this.btnRadius * 2.0F;
      float spacing = size * 0.038F;
      float totalWidth = 5.0F * btnDiameter + 4.0F * spacing;
      float startX = x + (size - totalWidth) / 2.0F + this.btnRadius;
      this.prevBtnX = startX;
      this.prevBtnY = controlsCenterY;
      this.playBtnX = startX + btnDiameter + spacing;
      this.playBtnY = controlsCenterY;
      this.nextBtnX = startX + 2.0F * (btnDiameter + spacing);
      this.nextBtnY = controlsCenterY;
      this.stopBtnX = startX + 3.0F * (btnDiameter + spacing);
      this.stopBtnY = controlsCenterY;
      this.volBtnX = startX + 4.0F * (btnDiameter + spacing);
      this.volBtnY = controlsCenterY;
      boolean canSkip = this.activeSession != null;
      SquarePlayerPanel.PlayerState state = this.getState();
      this.drawCircularButton(
         this.prevBtnX, this.prevBtnY, this.btnRadius, mouseX, mouseY, canSkip, () -> this.drawPrevIcon(this.prevBtnX, this.prevBtnY, this.btnRadius)
      );
      this.drawCircularButton(this.playBtnX, this.playBtnY, this.btnRadius * 1.15F, mouseX, mouseY, true, () -> {
         if (state == SquarePlayerPanel.PlayerState.PLAYING) {
            this.drawPauseIcon(this.playBtnX, this.playBtnY, this.btnRadius * 1.15F);
         } else {
            this.drawPlayIcon(this.playBtnX, this.playBtnY, this.btnRadius * 1.15F);
         }
      });
      this.drawCircularButton(
         this.nextBtnX, this.nextBtnY, this.btnRadius, mouseX, mouseY, canSkip, () -> this.drawNextIcon(this.nextBtnX, this.nextBtnY, this.btnRadius)
      );
      this.drawCircularButton(
         this.stopBtnX, this.stopBtnY, this.btnRadius, mouseX, mouseY, true, () -> this.drawStopIcon(this.stopBtnX, this.stopBtnY, this.btnRadius)
      );
      this.drawCircularButton(
         this.volBtnX, this.volBtnY, this.btnRadius, mouseX, mouseY, true, () -> this.drawVolumeIcon(this.volBtnX, this.volBtnY, this.btnRadius)
      );
   }

   private void drawCircularButton(float cx, float cy, float r, float mouseX, float mouseY, boolean enabled, Runnable iconRenderer) {
      boolean hovered = enabled && Math.hypot((double)(mouseX - cx), (double)(mouseY - cy)) <= (double)r;
      float alpha = enabled ? (hovered ? 0.92F : 0.7F) : 0.25F;
      int bg = ColorUtil.replAlpha(this.accentColor, alpha);
      Draw.rect(cx - r, cy - r, r * 2.0F, r * 2.0F, bg, r);
      Draw.outline(cx - r, cy - r, r * 2.0F, r * 2.0F, 0.75F, ColorUtil.getColor(255, 255, 255, (int)(alpha * 80.0F)), r);
      iconRenderer.run();
   }

   private void drawPlayIcon(float cx, float cy, float r) {
      float h = r * 0.95F;
      float w = r * 0.85F;
      float left = cx - w * 0.42F;

      for (int i = 0; i < 7; i++) {
         float t = (float)i / 6.0F;
         float sliceX = left + t * w;
         float sliceH = (1.0F - t) * h;
         Draw.rect(sliceX, cy - sliceH / 2.0F, w / 6.0F + 0.5F, sliceH, this.iconColor, 0.5F);
      }
   }

   private void drawPauseIcon(float cx, float cy, float r) {
      float barW = r * 0.28F;
      float barH = r * 0.9F;
      float gap = r * 0.25F;
      Draw.rect(cx - gap / 2.0F - barW, cy - barH / 2.0F, barW, barH, this.iconColor, barW / 2.0F);
      Draw.rect(cx + gap / 2.0F, cy - barH / 2.0F, barW, barH, this.iconColor, barW / 2.0F);
   }

   private void drawPrevIcon(float cx, float cy, float r) {
      float h = r * 0.75F;
      float w = r * 0.65F;
      float right = cx + w * 0.45F;
      Draw.rect(cx - w * 0.55F - r * 0.16F, cy - h / 2.0F, r * 0.18F, h, this.iconColor, 0.5F);

      for (int i = 0; i < 6; i++) {
         float t = (float)i / 5.0F;
         float sliceX = right - t * w;
         float sliceH = (1.0F - t) * h;
         Draw.rect(sliceX - w / 5.0F, cy - sliceH / 2.0F, w / 5.0F + 0.5F, sliceH, this.iconColor, 0.5F);
      }
   }

   private void drawNextIcon(float cx, float cy, float r) {
      float h = r * 0.75F;
      float w = r * 0.65F;
      float left = cx - w * 0.45F;

      for (int i = 0; i < 6; i++) {
         float t = (float)i / 5.0F;
         float sliceX = left + t * w;
         float sliceH = (1.0F - t) * h;
         Draw.rect(sliceX, cy - sliceH / 2.0F, w / 5.0F + 0.5F, sliceH, this.iconColor, 0.5F);
      }

      Draw.rect(cx + w * 0.55F, cy - h / 2.0F, r * 0.18F, h, this.iconColor, 0.5F);
   }

   private void drawStopIcon(float cx, float cy, float r) {
      float side = r * 0.76F;
      Draw.rect(cx - side / 2.0F, cy - side / 2.0F, side, side, this.iconColor, side * 0.22F);
   }

   private void drawVolumeIcon(float cx, float cy, float r) {
      float hornW = r * 0.35F;
      float hornH = r * 0.5F;
      Draw.rect(cx - r * 0.5F, cy - hornH * 0.35F, hornW * 0.65F, hornH * 0.7F, this.iconColor, 0.5F);

      for (int i = 0; i < 4; i++) {
         float t = (float)i / 3.0F;
         float sliceH = (0.45F + 0.55F * t) * hornH;
         Draw.rect(cx - r * 0.5F + hornW * 0.65F + t * hornW * 0.75F, cy - sliceH / 2.0F, hornW * 0.35F, sliceH, this.iconColor, 0.5F);
      }

      if (this.volume > 15.0F) {
         Draw.rect(cx + r * 0.28F, cy - r * 0.28F, r * 0.12F, r * 0.56F, this.iconColor, 0.5F);
      }

      if (this.volume > 55.0F) {
         Draw.rect(cx + r * 0.52F, cy - r * 0.44F, r * 0.12F, r * 0.88F, this.iconColor, 0.5F);
      }
   }

   private void renderVolumeSlider(float x, float y, float size, float mouseX, float mouseY) {
      this.volumeSlideAnim.update();
      float animT = this.volumeSlideAnim.get();
      if (!(animT <= 0.01F)) {
         this.volSliderW = size * 0.48F;
         this.volSliderH = size * 0.075F;
         this.volSliderX = x + size / 2.0F - this.volSliderW / 2.0F;
         this.volSliderY = this.volBtnY - this.btnRadius - this.volSliderH - size * 0.02F - (1.0F - animT) * 10.0F;
         int popBg = ColorUtil.getColor(18, 18, 22, (int)(230.0F * animT));
         Draw.rect(this.volSliderX, this.volSliderY, this.volSliderW, this.volSliderH, popBg, this.volSliderH / 2.0F);
         Draw.outline(
            this.volSliderX,
            this.volSliderY,
            this.volSliderW,
            this.volSliderH,
            0.75F,
            ColorUtil.getColor(255, 255, 255, (int)(40.0F * animT)),
            this.volSliderH / 2.0F
         );
         float trackPad = this.volSliderH * 0.4F;
         float trackX = this.volSliderX + trackPad;
         float trackW = this.volSliderW - trackPad * 2.0F - size * 0.1F;
         float trackH = size * 0.012F;
         float trackY = this.volSliderY + (this.volSliderH - trackH) / 2.0F;
         Draw.rect(trackX, trackY, trackW, trackH, ColorUtil.getColor(255, 255, 255, (int)(45.0F * animT)), trackH / 2.0F);
         float fillW = trackW * (this.volume / 100.0F);
         Draw.rect(trackX, trackY, fillW, trackH, ColorUtil.replAlpha(this.accentColor, animT), trackH / 2.0F);
         float thumbR = trackH * 1.8F;
         float thumbX = trackX + fillW;
         float thumbY = trackY + trackH / 2.0F;
         Draw.rect(thumbX - thumbR, thumbY - thumbR, thumbR * 2.0F, thumbR * 2.0F, ColorUtil.getColor(255, 255, 255, (int)(255.0F * animT)), thumbR);
         String pct = String.format("%d%%", (int)this.volume);
         Fonts.sf_medium
            .draw(
               pct, trackX + trackW + size * 0.025F, this.volSliderY + this.volSliderH * 0.32F, size * 0.032F, ColorUtil.getColor(240, (int)(240.0F * animT))
            );
      }
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (button != 0) {
         return false;
      } else if (this.volumeSliderOpen && inRect(mx, my, this.volSliderX, this.volSliderY, this.volSliderW, this.volSliderH)) {
         float trackPad = this.volSliderH * 0.4F;
         float trackX = this.volSliderX + trackPad;
         float trackW = this.volSliderW - trackPad * 2.0F - this.lastSize * 0.1F;
         this.draggingVolume = true;
         this.updateVolumeFromMouse(mx, trackX, trackW);
         return true;
      } else {
         if (this.volumeSliderOpen && Math.hypot(mx - (double)this.volBtnX, my - (double)this.volBtnY) > (double)this.btnRadius) {
            this.toggleVolumeSlider(false);
         }

         if (Math.hypot(mx - (double)this.prevBtnX, my - (double)this.prevBtnY) <= (double)this.btnRadius) {
            this.prev();
            return true;
         } else if (Math.hypot(mx - (double)this.playBtnX, my - (double)this.playBtnY) <= (double)(this.btnRadius * 1.15F)) {
            if (this.getState() == SquarePlayerPanel.PlayerState.PLAYING) {
               this.pause();
            } else {
               this.play();
            }

            return true;
         } else if (Math.hypot(mx - (double)this.nextBtnX, my - (double)this.nextBtnY) <= (double)this.btnRadius) {
            this.next();
            return true;
         } else if (Math.hypot(mx - (double)this.stopBtnX, my - (double)this.stopBtnY) <= (double)this.btnRadius) {
            this.stop();
            return true;
         } else if (Math.hypot(mx - (double)this.volBtnX, my - (double)this.volBtnY) <= (double)this.btnRadius) {
            this.toggleVolumeSlider(!this.volumeSliderOpen);
            return true;
         } else if (inRect(mx, my, this.lastX, this.lastY, this.lastSize, this.lastSize)) {
            if (this.getState() == SquarePlayerPanel.PlayerState.PLAYING) {
               this.pause();
            } else {
               this.play();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
      if (button == 0 && this.draggingVolume) {
         float trackPad = this.volSliderH * 0.4F;
         float trackX = this.volSliderX + trackPad;
         float trackW = this.volSliderW - trackPad * 2.0F - this.lastSize * 0.1F;
         this.updateVolumeFromMouse(mx, trackX, trackW);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double mx, double my, int button) {
      if (button == 0 && this.draggingVolume) {
         this.draggingVolume = false;
         return true;
      } else {
         return false;
      }
   }

   private void updateVolumeFromMouse(double mx, float trackX, float trackW) {
      float norm = class_3532.method_15363(((float)mx - trackX) / trackW, 0.0F, 1.0F);
      this.setVolume(norm * 100.0F);
   }

   private void toggleVolumeSlider(boolean open) {
      this.volumeSliderOpen = open;
      this.volumeSlideAnim.run(open ? 1.0 : 0.0, 0.15, Easings.CUBIC_OUT);
   }

   public void play() {
      if (this.activeSession != null) {
         this.activeSession.playPause();
      }
   }

   public void pause() {
      if (this.activeSession != null) {
         this.activeSession.playPause();
      }
   }

   public void stop() {
      if (!MediaKeys.stop()) {
         if (this.activeSession != null) {
            try {
               this.activeSession.stop();
            } catch (Throwable var2) {
               this.activeSession.playPause();
            }
         }
      }
   }

   public void next() {
      if (!MediaKeys.next()) {
         if (this.activeSession != null) {
            this.activeSession.next();
         }
      }
   }

   public void prev() {
      if (!MediaKeys.previous()) {
         if (this.activeSession != null) {
            this.activeSession.previous();
         }
      }
   }

   public void setVolume(float vol) {
      this.volume = class_3532.method_15363(vol, 0.0F, 100.0F);
      PREFS.putFloat("square_player_volume", this.volume);
   }

   public float getVolume() {
      return this.volume;
   }

   public MediaInfo getCurrentTrack() {
      return this.currentMedia;
   }

   public SquarePlayerPanel.PlayerState getState() {
      if (this.activeSession != null && this.currentMedia != null) {
         return this.currentMedia.getPlaying() ? SquarePlayerPanel.PlayerState.PLAYING : SquarePlayerPanel.PlayerState.PAUSED;
      } else {
         return SquarePlayerPanel.PlayerState.IDLE;
      }
   }

   public static int extractAccentColor(byte[] pngBytes) {
      if (pngBytes != null && pngBytes.length != 0) {
         try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(pngBytes));
            if (img == null) {
               return -11500801;
            } else {
               long rSum = 0L;
               long gSum = 0L;
               long bSum = 0L;
               int count = 0;
               int w = img.getWidth();
               int h = img.getHeight();

               for (int y = 0; y < h; y += 4) {
                  for (int x = 0; x < w; x += 4) {
                     int rgb = img.getRGB(x, y);
                     rSum += (long)(rgb >> 16 & 0xFF);
                     gSum += (long)(rgb >> 8 & 0xFF);
                     bSum += (long)(rgb & 0xFF);
                     count++;
                  }
               }

               if (count == 0) {
                  return -11500801;
               } else {
                  float rAvg = (float)rSum / ((float)count * 255.0F);
                  float gAvg = (float)gSum / ((float)count * 255.0F);
                  float bAvg = (float)bSum / ((float)count * 255.0F);
                  float[] hsb = Color.RGBtoHSB((int)(rAvg * 255.0F), (int)(gAvg * 255.0F), (int)(bAvg * 255.0F), null);
                  hsb[0] = (hsb[0] + 0.5F) % 1.0F;
                  hsb[1] = Math.max(hsb[1], 0.65F);
                  hsb[2] = Math.min(Math.max(hsb[2], 0.6F), 0.85F);
                  return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
               }
            }
         } catch (Exception var15) {
            return -11500801;
         }
      } else {
         return -11500801;
      }
   }

   public static int getContrastingIconColor(int accentColor) {
      float r = (float)(accentColor >> 16 & 0xFF) / 255.0F;
      float g = (float)(accentColor >> 8 & 0xFF) / 255.0F;
      float b = (float)(accentColor & 0xFF) / 255.0F;
      double lum = 0.299 * (double)r + 0.587 * (double)g + 0.114 * (double)b;
      return lum < 0.55 ? -1 : -15066598;
   }

   private static boolean inRect(double mx, double my, float x, float y, float w, float h) {
      return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
   }

   public static enum PlayerState {
      IDLE,
      PLAYING,
      PAUSED,
      STOPPED;
   }
}
