package ru.mentions.module.impl.display.interfaceimpl;

import dev.redstones.mediaplayerinfo.MediaInfo;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.MousePressEvent;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.MediaTextureUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class DynamicIsland implements IMinecraft {
   private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
   private static final long ANNOUNCEMENT_NANOS = 3000000000L;
   private long lastFrame;
   private long announcementUntil;
   private long pressedUntil;
   private long clockSecond = -1L;
   private String clock = "";
   private String trackKey;
   private boolean trackPending;
   private float width;
   private float height;
   private float alpha;
   private float mediaAlpha;
   private float notificationAlpha;
   private float announcementAlpha;
   private MediaInfo displayedMedia;
   private NotificationManager.Entry displayedNotification;
   private MusicHud.MediaState controlState;
   private boolean controlsVisible;
   private float controlsX;
   private float controlsY;
   private float controlSize;
   private float renderedScreenWidth;
   private float renderedScreenHeight;
   private int pressedControl = -1;

   public void onRender(DragSetting drag, InterFace hud, MusicHud musicHud) {
      long now = System.nanoTime();
      float elapsed = this.lastFrame == 0L ? 0.016666668F : Math.max(0.0F, Math.min(0.05F, (float)(now - this.lastFrame) / 1.0E9F));
      this.lastFrame = now;
      float step = 1.0F - (float)Math.exp((double)(-14.0F * elapsed));
      float growStep = 1.0F - (float)Math.exp((double)(-18.0F * elapsed));
      float shrinkStep = 1.0F - (float)Math.exp((double)(-9.0F * elapsed));
      float screenWidth = (float)mc.method_22683().method_4480() / 2.0F;
      float screenHeight = (float)mc.method_22683().method_4507() / 2.0F;
      if (!(screenWidth <= 12.0F) && !(screenHeight <= 12.0F)) {
         this.renderedScreenWidth = screenWidth;
         this.renderedScreenHeight = screenHeight;
         float scale = Math.min(hud.sizeHud.getValue(), Math.min((screenWidth - 12.0F) / 160.0F, (screenHeight - 4.0F) / 96.0F));
         MusicHud.MediaState state = musicHud.getMediaState();
         boolean hasMedia = hud.islandMusic.getValue() && state.hasMedia();
         if (hasMedia) {
            this.displayedMedia = state.info();
            String key = text(state.info().getTitle(), "") + "\u0000" + text(state.info().getArtist(), "");
            if (!key.equals(this.trackKey)) {
               this.trackKey = key;
               this.trackPending = true;
               this.announcementUntil = 0L;
            }

            if (state.info().getPlaying() && this.trackPending) {
               this.trackPending = false;
               if (hud.islandTrackAnnouncements.getValue()) {
                  this.announcementUntil = now + 3000000000L;
               }
            }
         } else {
            this.trackKey = null;
            this.trackPending = false;
            this.announcementUntil = 0L;
         }

         boolean announcing = hasMedia && state.info().getPlaying() && hud.islandTrackAnnouncements.getValue() && now < this.announcementUntil;
         if (!hud.islandTrackAnnouncements.getValue()) {
            this.announcementUntil = 0L;
         }

         NotificationManager.Entry notification = hud.islandNotifications.getValue() ? latestNotification() : null;
         if (notification != null) {
            this.displayedNotification = notification;
         }

         this.alpha = approach(this.alpha, 1.0F, step);
         this.mediaAlpha = approach(this.mediaAlpha, hasMedia ? 1.0F : 0.0F, step);
         this.notificationAlpha = approach(this.notificationAlpha, notification != null ? 1.0F : 0.0F, step);
         this.announcementAlpha = approach(this.announcementAlpha, announcing ? 1.0F : 0.0F, step);
         long second = System.currentTimeMillis() / 1000L;
         if (second != this.clockSecond) {
            this.clockSecond = second;
            this.clock = LocalTime.now().format(CLOCK);
         }

         String user = mc.field_1724 == null ? "User" : mc.field_1724.method_5477().getString();
         String stats = mc.method_47599() + " fps  |  " + this.clock;
         float idleWidth = Math.max(
            160.0F * scale,
            Math.min(210.0F * scale, 42.0F * scale + Fonts.sf_medium.getWidth(user, 6.5F * scale) + Fonts.sf_regular.getWidth(stats, 6.0F * scale))
         );
         float targetWidth = idleWidth;
         if (hasMedia) {
            targetWidth = Math.max(idleWidth, (announcing ? 272.0F : 238.0F) * scale);
         }

         if (notification != null) {
            targetWidth = Math.max(
               targetWidth,
               Math.min(
                  280.0F * scale, Math.max(220.0F * scale, Fonts.sf_regular.getWidth(text(notification.text, "Notification"), 6.5F * scale) + 24.0F * scale)
               )
            );
         }

         targetWidth = Math.min(targetWidth, screenWidth - 12.0F);
         float targetHeight = (22.0F + (hasMedia ? 34.0F : 0.0F) + (announcing ? 13.0F : 0.0F) + (notification != null ? 26.0F : 0.0F)) * scale;
         if (this.width == 0.0F) {
            this.width = Math.min(idleWidth, screenWidth - 12.0F);
            this.height = 22.0F * scale;
         }

         this.width = Math.min(screenWidth - 12.0F, this.width + (targetWidth - this.width) * (targetWidth > this.width ? growStep : shrinkStep));
         this.height = Math.min(screenHeight - 4.0F, this.height + (targetHeight - this.height) * (targetHeight > this.height ? growStep : shrinkStep));
         float x = (screenWidth - this.width) / 2.0F;
         float maxY = Math.max(0.0F, screenHeight - this.height);
         float y = Math.max(0.0F, Math.min(maxY, drag.position.y));
         drag.lockX = true;
         drag.active = true;
         drag.position.set(x, y);
         drag.targetPosition.x = x;
         drag.targetPosition.y = Math.max(0.0F, Math.min(maxY, drag.targetPosition.y));
         drag.size.set(this.width, this.height);
         float radius = Math.min(12.0F * scale, this.height / 2.0F);
         Draw.glow(x, y, this.width, this.height, ColorUtil.multAlpha(ColorUtil.getClientColor(1), 0.05F * this.alpha), radius, 18.0F, 1.0F);
         RenderUtil.Blur.blur(
            x, y, this.width, this.height, this.alpha, radius, ColorUtil.replAlpha(ColorUtil.background(), unit(hud.alphaHUD.getValue()) * this.alpha)
         );
         Draw.outline(x, y, this.width, this.height, 0.75F, ThemeColor.getOutlineColor(this.alpha * 0.08F), radius);
         this.controlsVisible = false;
         this.drawHeader(x, y, scale, user, stats);
         if (this.height > 24.0F * scale) {
            float divAlpha = Math.max(this.mediaAlpha, this.notificationAlpha) * this.alpha;
            RenderUtil.Render2D.rect(x + 8.0F * scale, y + 21.5F * scale, this.width - 16.0F * scale, 0.5F, ThemeColor.getHudColor(divAlpha * 0.07F), 0.25F);
         }

         Scissor.enable(x + 3.0F * scale, y + 2.0F * scale, Math.max(0.0F, this.width - 6.0F * scale), Math.max(0.0F, this.height - 4.0F * scale), 2.0F);

         try {
            float bodyY = y + 22.0F * scale;
            if (this.announcementAlpha > 0.01F && this.mediaAlpha > 0.01F) {
               float a2 = unit(this.alpha * this.announcementAlpha * this.mediaAlpha);
               int accent = ColorUtil.multAlpha(ColorUtil.getClientColor(1), a2);
               Fonts.sf_medium.draw("Now playing", x + 9.0F * scale, bodyY + 2.0F * scale, 6.0F * scale, accent);
               if (state.info().getPlaying()) {
                  float eqX = x + 13.0F * scale + Fonts.sf_medium.getWidth("Now playing", 6.0F * scale);
                  drawEqualizer(eqX, bodyY + 8.0F * scale, scale, a2);
               }
            }

            bodyY += 13.0F * scale * this.announcementAlpha;
            if (this.displayedMedia != null && this.mediaAlpha > 0.01F) {
               this.drawMedia(x, bodyY, scale, state, hasMedia);
            }

            float notificationY = bodyY + 34.0F * scale * this.mediaAlpha;
            if (this.displayedNotification != null && this.notificationAlpha > 0.01F) {
               float a = unit(this.alpha * this.notificationAlpha);
               int typeColor = typeColor(this.displayedNotification.type, a);
               RenderUtil.Render2D.rect(x + 6.0F * scale, notificationY + 3.0F * scale, 1.8F * scale, 19.0F * scale, typeColor, 0.9F * scale);
               Fonts.sf_medium.draw(typeLabel(this.displayedNotification.type), x + 12.0F * scale, notificationY + 2.0F * scale, 5.5F * scale, typeColor);
               clipped(
                  Fonts.sf_regular,
                  text(this.displayedNotification.text, "Notification"),
                  x + 12.0F * scale,
                  notificationY + 11.0F * scale,
                  this.width - 21.0F * scale,
                  6.5F * scale,
                  ThemeColor.getHudColor(a)
               );
            }
         } finally {
            Scissor.disable();
         }
      } else {
         drag.active = false;
         this.controlsVisible = false;
      }
   }

   private void drawHeader(float x, float y, float scale, String user, String stats) {
      float statsWidth = Fonts.sf_regular.getWidth(stats, 6.0F * scale);
      float statsX = x + this.width - 9.0F * scale - statsWidth;
      Fonts.sf_bold.drawCentered("L", x + 12.0F * scale, y + 6.0F * scale, 8.0F * scale, ColorUtil.multAlpha(ColorUtil.getClientColor(1), this.alpha));
      clipped(Fonts.sf_medium, user, x + 24.0F * scale, y + 7.0F * scale, statsX - x - 31.0F * scale, 6.5F * scale, ThemeColor.getHudColor(this.alpha));
      Fonts.sf_regular.draw(stats, statsX, y + 7.5F * scale, 6.0F * scale, ThemeColor.getHudColor(this.alpha * 0.75F));
   }

   private void drawMedia(float x, float y, float scale, MusicHud.MediaState state, boolean hasMedia) {
      float a = unit(this.alpha * this.mediaAlpha);
      float artworkSize = 22.0F * scale;
      float artX = x + 9.0F * scale;
      float artY = y + 2.0F * scale;
      boolean playing = this.displayedMedia.getPlaying();
      if (hasMedia && MediaTextureUtil.hasArtwork()) {
         if (playing) {
            Draw.glow(artX, artY, artworkSize, artworkSize, ColorUtil.multAlpha(ColorUtil.getClientColor(1), 0.16F * a), 5.0F * scale, 8.0F, 1.0F);
         }

         RenderUtil.Images.texture(MediaTextureUtil.ARTWORK_ID, artX, artY, artworkSize, artworkSize, 1.0F, 5.0F * scale, ColorUtil.getColor(255, a));
         Draw.outline(artX, artY, artworkSize, artworkSize, 0.5F, ThemeColor.getHudColor(a * 0.14F), 5.0F * scale);
      } else {
         RenderUtil.Render2D.rect(artX, artY, artworkSize, artworkSize, ThemeColor.getHudColor(a * 0.08F), 5.0F * scale);
         Fonts.icon.drawCentered("M", artX + artworkSize / 2.0F, artY + artworkSize / 2.0F - 3.0F * scale, 6.0F * scale, ThemeColor.getHudColor(a * 0.65F));
      }

      this.controlSize = 14.0F * scale;
      this.controlsX = x + this.width - 9.0F * scale - 3.0F * this.controlSize;
      this.controlsY = y + 4.0F * scale;
      float textX = x + 38.0F * scale;
      float textWidth = this.controlsX - textX - 6.0F * scale;
      scrolling(Fonts.sf_medium, text(this.displayedMedia.getTitle(), "Untitled"), textX, y + 3.0F * scale, textWidth, 6.8F * scale, ThemeColor.getHudColor(a));
      clipped(
         Fonts.sf_regular,
         text(this.displayedMedia.getArtist(), "Unknown artist"),
         textX,
         y + 13.0F * scale,
         textWidth,
         5.8F * scale,
         ThemeColor.getHudColor(a * 0.65F)
      );
      this.controlState = state;
      this.controlsVisible = hasMedia
         && this.mediaAlpha > 0.8F
         && this.alpha > 0.8F
         && this.controlsY + this.controlSize <= (float)mc.method_22683().method_4507() / 2.0F;
      float mouseFactor = (float)mc.method_22683().method_4480() / Math.max(1.0F, (float)mc.method_22683().method_4486()) / 2.0F;
      float hoverX = (float)mc.field_1729.method_1603() * mouseFactor;
      float hoverY = (float)mc.field_1729.method_1604() * mouseFactor;
      boolean cursorShown = mc.field_1755 != null;
      String[] icons = new String[]{"O", playing ? "V" : "M", "N"};

      for (int i = 0; i < icons.length; i++) {
         float buttonX = this.controlsX + (float)i * this.controlSize;
         boolean hovered = cursorShown
            && hoverX >= buttonX
            && hoverX < buttonX + this.controlSize
            && hoverY >= this.controlsY
            && hoverY < this.controlsY + this.controlSize;
         if (i == 1) {
            RenderUtil.Render2D.rect(
               buttonX + 1.0F * scale,
               this.controlsY + 1.0F * scale,
               this.controlSize - 2.0F * scale,
               this.controlSize - 2.0F * scale,
               ColorUtil.multAlpha(ColorUtil.getClientColor(1), a * (hovered ? 0.28F : 0.18F)),
               (this.controlSize - 2.0F * scale) / 2.0F
            );
         } else if (hovered || this.controlsVisible && this.pressedControl == i && System.nanoTime() < this.pressedUntil) {
            RenderUtil.Render2D.rect(
               buttonX + 1.0F * scale,
               this.controlsY + 1.0F * scale,
               this.controlSize - 2.0F * scale,
               this.controlSize - 2.0F * scale,
               ThemeColor.getHudColor(a * 0.12F),
               (this.controlSize - 2.0F * scale) / 2.0F
            );
         }

         int iconColor = i == 1 ? ColorUtil.getColor(255, a * (hasMedia ? 1.0F : 0.35F)) : ThemeColor.getHudColor(a * (hasMedia ? 0.9F : 0.35F));
         Fonts.icon.drawCentered(icons[i], buttonX + this.controlSize / 2.0F, this.controlsY + 4.0F * scale, 5.5F * scale, iconColor);
      }

      long duration = this.displayedMedia.getDuration();
      if (duration > 0L) {
         float barHeight = 1.6F * scale;
         float barY = y + 29.0F * scale;
         float barWidth = this.width - 18.0F * scale;
         float progress = unit((float)this.displayedMedia.getPosition() / (float)duration);
         RenderUtil.Render2D.rect(x + 9.0F * scale, barY, barWidth, barHeight, ThemeColor.getHudColor(a * 0.12F), barHeight / 2.0F);
         if (progress > 0.0F) {
            int c1 = ColorUtil.multAlpha(ColorUtil.getClientColor(0), a);
            int c2 = ColorUtil.multAlpha(ColorUtil.getClientColor(1), a);
            RenderUtil.Render2D.gradientRect(x + 9.0F * scale, barY, barWidth * progress, barHeight, new int[]{c1, c2, c2, c1}, barHeight / 2.0F);
            float capX = x + 9.0F * scale + barWidth * progress;
            RenderUtil.Render2D.rect(
               capX - 1.1F * scale,
               barY - 0.7F * scale,
               2.2F * scale,
               barHeight + 1.4F * scale,
               ColorUtil.multAlpha(ColorUtil.getClientColor(1), a),
               1.1F * scale
            );
         }
      }
   }

   private static void scrolling(Font font, String value, float x, float y, float maxWidth, float size, int color) {
      if (!(maxWidth <= 0.0F)) {
         float textWidth = font.getWidth(value, size);
         if (textWidth <= maxWidth) {
            font.draw(value, x, y, size, color);
         } else {
            float gap = size * 4.0F;
            float cycle = textWidth + gap;
            float offset = (float)((double)System.nanoTime() / 1.0E9 * 16.0) % cycle;
            Scissor.enable(x, y - size * 0.25F, maxWidth, size * 1.7F, 2.0F);

            try {
               font.draw(value, x - offset, y, size, color);
               font.draw(value, x - offset + cycle, y, size, color);
            } finally {
               Scissor.disable();
            }
         }
      }
   }

   private static void drawEqualizer(float x, float baselineY, float scale, float alpha) {
      double t = (double)System.nanoTime() / 1.2E8;

      for (int i = 0; i < 3; i++) {
         float barH = (1.6F + 3.2F * (float)Math.abs(Math.sin(t + (double)i * 1.7))) * scale;
         RenderUtil.Render2D.rect(
            x + (float)i * 2.4F * scale, baselineY - barH, 1.5F * scale, barH, ColorUtil.multAlpha(ColorUtil.getClientColor(1), alpha), 0.7F * scale
         );
      }
   }

   private static String typeLabel(NotificationManager.Type type) {
      return switch (type) {
         case WARNING -> "Alert";
         case MODULE -> "Module";
         case EFFECT -> "Effect";
         default -> "Notification";
      };
   }

   private static int typeColor(NotificationManager.Type type, float alpha) {
      return switch (type) {
         case WARNING -> ColorUtil.getColor(255, 200, 70, alpha);
         case MODULE, EFFECT -> ColorUtil.multAlpha(ColorUtil.getClientColor(1), alpha);
         default -> ColorUtil.getColor(255, alpha * 0.85F);
      };
   }

   public boolean onMouseClick(MousePressEvent event, MusicHud musicHud) {
      if (MusicHud.acceptsMouseInput(event)
         && this.controlsVisible
         && System.nanoTime() - this.lastFrame <= 250000000L
         && this.renderedScreenWidth == (float)mc.method_22683().method_4480() / 2.0F
         && this.renderedScreenHeight == (float)mc.method_22683().method_4507() / 2.0F) {
         MusicHud.MediaState state = musicHud.getMediaState();
         if (state.hasMedia() && this.controlState != null && state.session() == this.controlState.session()) {
            double mouseX = MusicHud.hudMouseX(event);
            double mouseY = MusicHud.hudMouseY(event);
            if (!(mouseY < (double)this.controlsY)
               && !(mouseY > (double)(this.controlsY + this.controlSize))
               && !(mouseX < (double)this.controlsX)
               && !(mouseX >= (double)(this.controlsX + 3.0F * this.controlSize))) {
               int index = (int)((mouseX - (double)this.controlsX) / (double)this.controlSize);

               MusicHud.Control control = switch (index) {
                  case 0 -> MusicHud.Control.PREVIOUS;
                  case 1 -> MusicHud.Control.PLAY_PAUSE;
                  default -> MusicHud.Control.NEXT;
               };
               if (!musicHud.control(control)) {
                  return false;
               } else {
                  this.pressedControl = index;
                  this.pressedUntil = System.nanoTime() + 150000000L;
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isControlAt(double mx, double my) {
      return this.controlsVisible
         && my >= (double)this.controlsY
         && my <= (double)(this.controlsY + this.controlSize)
         && mx >= (double)this.controlsX
         && mx < (double)(this.controlsX + 3.0F * this.controlSize);
   }

   private static NotificationManager.Entry latestNotification() {
      List<NotificationManager.Entry> entries = NotificationManager.entries();
      synchronized (entries) {
         for (int i = entries.size() - 1; i >= 0; i--) {
            NotificationManager.Entry entry = entries.get(i);
            if (!entry.removing && !entry.isExpired()) {
               return entry;
            }
         }

         return null;
      }
   }

   private static void clipped(Font font, String text, float x, float y, float width, float size, int color) {
      if (!(width <= 0.0F)) {
         Scissor.enable(x, y - size * 0.2F, width, size * 2.0F, 2.0F);

         try {
            font.drawFadingText(text, x, y, width, color, size);
         } finally {
            Scissor.disable();
         }
      }
   }

   private static String text(String value, String fallback) {
      return value != null && !value.isBlank() ? value.replace('\n', ' ').replace('\r', ' ') : fallback;
   }

   private static float unit(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static float approach(float value, float target, float step) {
      return unit(value + (target - value) * step);
   }

   public void reset() {
      this.lastFrame = 0L;
      this.announcementUntil = 0L;
      this.pressedUntil = 0L;
      this.trackKey = null;
      this.trackPending = false;
      this.width = 0.0F;
      this.height = 0.0F;
      this.alpha = 0.0F;
      this.mediaAlpha = 0.0F;
      this.notificationAlpha = 0.0F;
      this.announcementAlpha = 0.0F;
      this.displayedMedia = null;
      this.displayedNotification = null;
      this.controlState = null;
      this.controlsVisible = false;
      this.pressedControl = -1;
   }
}
