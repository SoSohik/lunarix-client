package ru.mentions.module.impl.display.interfaceimpl;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;
import net.minecraft.class_408;
import ru.mentions.manager.event_impl.MousePressEvent;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.MediaKeys;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.MediaTextureUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class MusicHud implements element {
   private static final MediaInfo EMPTY = new MediaInfo("Нет трека", "—", new byte[0], 0L, 0L, false);
   private static float S = 1.0F;
   private ExecutorService executor;
   private static final MusicHud.MediaState EMPTY_STATE = new MusicHud.MediaState(EMPTY, null, 0L);
   private final Animation openAnimation = new Animation();
   private volatile MusicHud.MediaState mediaState = EMPTY_STATE;
   private boolean pollPending;
   private long errorCooldownUntil = 0L;
   private int consecutiveErrors = 0;
   private float progressWidth;
   private float playX;
   private float playY;
   private float playW;
   private float playH;
   private float prevX;
   private float prevY;
   private float prevW;
   private float prevH;
   private float nextX;
   private float nextY;
   private float nextW;
   private float nextH;

   private synchronized ExecutorService executor() {
      if (this.executor == null || this.executor.isShutdown()) {
         this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "white-media-hud");
            t.setDaemon(true);
            return t;
         });
      }

      return this.executor;
   }

   public synchronized void onTick(boolean nativeIntegration) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!nativeIntegration) {
            if (this.mediaState != EMPTY_STATE) {
               this.mediaState = EMPTY_STATE;
            }
         } else {
            long now = System.currentTimeMillis();
            if (now >= this.errorCooldownUntil) {
               if (mc.field_1724.field_6012 % 35 == 0 && !this.pollPending) {
                  this.pollPending = true;
                  ExecutorService worker = this.executor();
                  worker.submit(
                     () -> {
                        try {
                           IMediaSession selected = null;
                           MediaInfo selectedInfo = EMPTY;
                           List<IMediaSession> sessions = null;

                           try {
                              sessions = MediaPlayerInfo.Instance.getMediaSessions();
                           } catch (Throwable var28) {
                              this.consecutiveErrors++;
                              this.errorCooldownUntil = System.currentTimeMillis() + Math.min(60000L, 5000L * (long)this.consecutiveErrors);
                              return;
                           }

                           if (sessions != null) {
                              for (IMediaSession candidate : sessions) {
                                 try {
                                    MediaInfo info = candidate.getMedia();
                                    if (info != null
                                       && (info.getTitle() != null && !info.getTitle().isBlank() || info.getArtist() != null && !info.getArtist().isBlank())) {
                                       if (selected == null || info.getPlaying()) {
                                          selected = candidate;
                                          selectedInfo = info;
                                       }

                                       if (info.getPlaying()) {
                                          break;
                                       }
                                    }
                                 } catch (Throwable var27) {
                                 }
                              }
                           }

                           this.consecutiveErrors = 0;
                           synchronized (this) {
                              if (this.executor == worker && !worker.isShutdown()) {
                                 MusicHud.MediaState snapshot = selected == null
                                    ? EMPTY_STATE
                                    : new MusicHud.MediaState(selectedInfo, selected, System.nanoTime());
                                 this.mediaState = snapshot;
                                 mc.execute(() -> {
                                    if (this.mediaState == snapshot && this.executor == worker && !worker.isShutdown()) {
                                       try {
                                          MediaTextureUtil.updateArtwork(snapshot.info().getArtworkPng());
                                       } catch (Throwable var4x) {
                                       }
                                    }
                                 });
                              }
                           }
                        } catch (Throwable var29) {
                           this.consecutiveErrors++;
                           this.errorCooldownUntil = System.currentTimeMillis() + Math.min(60000L, 5000L * (long)this.consecutiveErrors);
                        } finally {
                           synchronized (this) {
                              if (this.executor == worker) {
                                 this.pollPending = false;
                              }
                           }
                        }
                     }
                  );
               }
            }
         }
      }
   }

   public MusicHud.MediaState getMediaState() {
      MusicHud.MediaState state = this.mediaState;
      return state.hasMedia() ? state : EMPTY_STATE;
   }

   synchronized boolean control(MusicHud.Control control) {
      MusicHud.MediaState state = this.getMediaState();
      if (!state.hasMedia()) {
         return false;
      } else {
         boolean sent = switch (control) {
            case PREVIOUS -> MediaKeys.previous();
            case PLAY_PAUSE -> MediaKeys.playPause();
            case NEXT -> MediaKeys.next();
         };
         if (sent) {
            return true;
         } else {
            ExecutorService worker = this.executor();
            worker.submit(() -> {
               if (!worker.isShutdown() && state.hasMedia() && this.mediaState.session() == state.session()) {
                  try {
                     switch (control) {
                        case PREVIOUS:
                           state.session().previous();
                           break;
                        case PLAY_PAUSE:
                           state.session().playPause();
                           break;
                        case NEXT:
                           state.session().next();
                     }
                  } catch (Throwable var5) {
                  }
               }
            });
            return true;
         }
      }
   }

   static double hudMouseX(MousePressEvent event) {
      return event.getMouseX() * (double)mc.method_22683().method_4480() / Math.max(1.0, (double)mc.method_22683().method_4486()) / 2.0;
   }

   static double hudMouseY(MousePressEvent event) {
      return event.getMouseY() * (double)mc.method_22683().method_4507() / Math.max(1.0, (double)mc.method_22683().method_4502()) / 2.0;
   }

   static boolean acceptsMouseInput(MousePressEvent event) {
      return event.getAction() == 1 && event.getButton() == 0 && (event.getScreen() == null || event.getScreen() instanceof class_408);
   }

   public boolean onMouseClick(MousePressEvent event) {
      if (!acceptsMouseInput(event)) {
         return false;
      } else if (this.openAnimation.get() <= 0.25F) {
         return false;
      } else {
         double mx = hudMouseX(event);
         double my = hudMouseY(event);
         if (!this.getMediaState().hasMedia()) {
            return false;
         } else if (inRect(mx, my, this.prevX, this.prevY, this.prevW, this.prevH)) {
            return this.control(MusicHud.Control.PREVIOUS);
         } else if (inRect(mx, my, this.playX, this.playY, this.playW, this.playH)) {
            return this.control(MusicHud.Control.PLAY_PAUSE);
         } else {
            return inRect(mx, my, this.nextX, this.nextY, this.nextW, this.nextH) ? this.control(MusicHud.Control.NEXT) : false;
         }
      }
   }

   public boolean isControlAt(double mx, double my) {
      return !(this.openAnimation.get() <= 0.25F) && this.getMediaState().hasMedia()
         ? inRect(mx, my, this.prevX, this.prevY, this.prevW, this.prevH)
            || inRect(mx, my, this.playX, this.playY, this.playW, this.playH)
            || inRect(mx, my, this.nextX, this.nextY, this.nextW, this.nextH)
         : false;
   }

   @Override
   public void onRender(DragSetting dragSetting, InterFace interFace) {
      MusicHud.MediaState state = this.getMediaState();
      boolean hasMedia = state.hasMedia();
      boolean chatOpen = mc.field_1755 instanceof class_408;
      boolean visible = hasMedia || chatOpen;
      this.openAnimation.update();
      this.openAnimation.run(visible ? 1.0 : 0.0, 0.12F, Easings.SINE_OUT, true);
      float alpha = Math.max(0.0F, Math.min(1.0F, this.openAnimation.get()));
      if (!visible && alpha <= 0.01F) {
         dragSetting.active = false;
      } else {
         dragSetting.active = true;
         S = InterFace.getInstance().sizeHud.getValue();
         float WIDTH = 79.0F * S;
         float HEIGHT = 41.0F * S;
         float PAD = 4.0F * S;
         float RADIUS = 7.0F * S;
         float ART_SIZE = 19.0F * S;
         float ART_RADIUS = 4.0F * S;
         MediaInfo info = state.info();
         float x = dragSetting.position.x;
         float y = dragSetting.position.y;
         Draw.glow(x, y, WIDTH, HEIGHT, ColorUtil.replAlpha(ColorUtil.getColor(0), 0.1F * alpha), RADIUS, 12.0F, 1.0F);
         RenderUtil.Blur.blur(
            x, y, WIDTH, HEIGHT, alpha, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue() * alpha)
         );
         Draw.outline(x, y, WIDTH, HEIGHT, 0.75F, ThemeColor.getOutlineColor(alpha * 0.08F), RADIUS);
         float artX = x + PAD;
         float artY = y + PAD;
         if (MediaTextureUtil.hasArtwork()) {
            RenderUtil.Images.texture(MediaTextureUtil.ARTWORK_ID, artX, artY, ART_SIZE, ART_SIZE, 1.0F, ART_RADIUS, ColorUtil.getColor(255, alpha));
         } else {
            RenderUtil.Render2D.rect(artX, artY, ART_SIZE, ART_SIZE, ColorUtil.multAlpha(new Color(30, 30, 36).getRGB(), alpha), ART_RADIUS);
            Fonts.icon.drawCentered("M", artX + ART_SIZE / 2.0F, artY + ART_SIZE / 2.0F - 1.75F * S, 3.75F * S, ThemeColor.getHudColor(alpha));
         }

         float textX = artX + ART_SIZE + 5.0F * S;
         float textW = x + WIDTH - PAD - textX;
         Font titleFont = Fonts.sf_regular;
         Font metaFont = Fonts.sf_regular;
         String title = info.getTitle() != null && !info.getTitle().isBlank() ? info.getTitle() : "Нет трека";
         String artist = info.getArtist() != null && !info.getArtist().isBlank() ? info.getArtist() : "—";
         Scissor.enable(textX, artY, textW, ART_SIZE, 2.0F);
         titleFont.drawFadingText(title, textX, artY + 2.0F * S, textW, ColorUtil.getColor(255, alpha), 7.0F * S);
         metaFont.drawFadingText(artist, textX, artY + 10.0F * S, textW, ColorUtil.getColor(180, alpha), 6.0F * S);
         Scissor.disable();
         long duration = Math.max(1L, info.getDuration());
         long position = class_3532.method_53062(info.getPosition(), 0L, duration);
         String posText = formatDuration(position);
         String durText = formatDuration(duration);
         float timeSize = 6.0F * S;
         float progressRowY = artY + ART_SIZE + 2.0F * S;
         float posW = metaFont.getWidth(posText, timeSize);
         float durW = metaFont.getWidth(durText, timeSize);
         int timeColor = ColorUtil.getColor(150, alpha);
         metaFont.draw(posText, x + PAD, progressRowY + 0.5F * S, timeSize, timeColor);
         metaFont.draw(durText, x + WIDTH - PAD - durW, progressRowY + 0.5F * S, timeSize, timeColor);
         float barX = x + PAD + posW + 2.5F * S;
         float barW = x + WIDTH - PAD - durW - 2.5F * S - barX;
         float barY = progressRowY + 3.25F * S;
         float barH = 1.25F * S;
         float targetProgress = barW * ((float)position / (float)duration);
         this.progressWidth = class_3532.method_16439(0.18F, this.progressWidth, class_3532.method_15363(targetProgress, 0.0F, barW));
         int trackColor = ColorUtil.multAlpha(new Color(0).getRGB(), alpha * 0.1F);
         RenderUtil.Render2D.rect(barX, barY, barW, barH, trackColor, barH / 2.0F);
         if (this.progressWidth > 0.5F) {
            int[] progressColors = new int[]{
               ColorUtil.multAlpha(ColorUtil.getClientColor1(1), alpha),
               ColorUtil.multAlpha(ColorUtil.getClientColor1(1), alpha),
               ColorUtil.multAlpha(ColorUtil.getClientColor1(1), alpha),
               ColorUtil.multAlpha(ColorUtil.getClientColor1(1), alpha)
            };
            RenderUtil.Render2D.gradientRect(barX, barY, this.progressWidth, barH, progressColors, barH / 2.0F);
         }

         float controlsY = y + HEIGHT - PAD - 5.0F * S;
         float centerX = x + WIDTH / 2.0F;
         float controlSpacing = 10.0F * S;
         int controlColor = ThemeColor.getHudColor(alpha);
         int controlColor2 = ThemeColor.getHudColor(alpha * 0.6F);
         this.prevW = 6.0F * S;
         this.prevH = 5.0F * S;
         this.prevX = centerX - controlSpacing - this.prevW / 2.0F;
         this.prevY = controlsY;
         this.playW = 6.0F * S;
         this.playH = 5.0F * S;
         this.playX = centerX - this.playW / 2.0F;
         this.playY = controlsY;
         this.nextW = 6.0F * S;
         this.nextH = 5.0F * S;
         this.nextX = centerX + controlSpacing - this.nextW / 2.0F;
         this.nextY = controlsY;
         Font icon = Fonts.icon;
         float iconSize = 4.5F * S;
         icon.drawCentered("O", this.prevX + this.prevW / 2.0F, controlsY + 0.75F * S, iconSize, controlColor2);
         icon.drawCentered(info.getPlaying() ? "V" : "M", this.playX + this.playW / 2.0F, controlsY + 0.75F * S, iconSize, controlColor);
         icon.drawCentered("N", this.nextX + this.nextW / 2.0F, controlsY + 0.75F * S, iconSize, controlColor2);
         dragSetting.size.set(WIDTH, HEIGHT);
      }
   }

   public synchronized void shutdown() {
      if (this.executor != null) {
         this.executor.shutdownNow();
         this.executor = null;
      }

      this.pollPending = false;
      this.mediaState = EMPTY_STATE;
      this.openAnimation.set(0.0);
      this.progressWidth = 0.0F;
      MediaTextureUtil.reset();
   }

   private static boolean inRect(double mx, double my, float x, float y, float w, float h) {
      return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
   }

   private static String formatDuration(long seconds) {
      long mins = seconds / 60L;
      long secs = seconds % 60L;
      return String.format("%d:%02d", mins, secs);
   }

   static enum Control {
      PREVIOUS,
      PLAY_PAUSE,
      NEXT;
   }

   public static record MediaState(MediaInfo info, IMediaSession session, long updatedAt) {
      public boolean hasMedia() {
         return this.session != null && System.nanoTime() - this.updatedAt < 5000000000L;
      }
   }
}
