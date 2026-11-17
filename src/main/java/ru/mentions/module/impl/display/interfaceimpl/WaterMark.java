package ru.mentions.module.impl.display.interfaceimpl;

import dev.redstones.mediaplayerinfo.MediaInfo;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_640;
import ru.mentions.manager.event_impl.MousePressEvent;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.MediaTextureUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.RollingText;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class WaterMark implements element {
   private static float S = 1.0F;
   private static float TEXT = 5.5F * S;
   private static float ICON = 5.0F * S;
   private static float H = 16.0F * S;
   private static float RADIUS = 7.0F * S;
   private static float PAD = 6.0F * S;
   private static float GAP = 6.0F * S;
   private static float INNER = 3.0F * S;
   private static float SEP_W = 0.5F;
   private static float LOGO_W = 13.0F * S;
   private static float ICON_Y = 5.5F * S;
   private static float TEXT_Y = 4.4F * S;
   private static float SEP_Y = 4.5F * S;
   private final RollingText fpsText = new RollingText(3.0F);
   private final RollingText pingText = new RollingText(3.0F);
   private int lastFps;
   private int lastPing;
   private float mediaAlpha;
   private long mediaLastFrame;
   private final DynamicIsland island = new DynamicIsland();

   public void onRender(DragSetting dragSetting, InterFace interFace, MusicHud musicHud) {
      if (interFace.isDynamicIslandActive()) {
         dragSetting.active = false;
         this.island.onRender(interFace.dynamicIsland, interFace, musicHud);
      } else {
         this.resetIsland(interFace);
         dragSetting.active = true;
         this.onRender(dragSetting, interFace, musicHud.getMediaState());
      }
   }

   public boolean onMouseClick(MousePressEvent event, InterFace interFace, MusicHud musicHud) {
      return interFace.islandMusic.getValue() && this.island.onMouseClick(event, musicHud);
   }

   public boolean isIslandControlAt(double mx, double my) {
      return this.island.isControlAt(mx, my);
   }

   public void resetIsland(InterFace interFace) {
      interFace.dynamicIsland.active = false;
      this.island.reset();
   }

   private void onRender(DragSetting dragSetting, InterFace interFace, MusicHud.MediaState media) {
      S = InterFace.getInstance().sizeHud.getValue() * 1.1F;
      TEXT = 5.5F * S;
      ICON = 5.0F * S;
      H = 16.0F * S;
      RADIUS = 7.0F * S;
      PAD = 6.0F * S;
      GAP = 6.0F * S;
      INNER = 3.0F * S;
      SEP_W = 0.5F;
      LOGO_W = 13.0F * S;
      ICON_Y = 5.5F * S;
      TEXT_Y = 4.4F * S;
      SEP_Y = 4.5F * S;
      float x = dragSetting.position.x;
      float y = dragSetting.position.y;
      Font fonts = Fonts.sf_medium;
      String user = mc.field_1724 != null ? mc.field_1724.method_5477().getString() : "User";
      int pings = 0;
      class_640 entry;
      if (mc.method_1562() != null && mc.field_1724 != null && (entry = mc.method_1562().method_2871(mc.field_1724.method_5667())) != null) {
         pings = entry.method_2959();
      }

      int fps = mc.method_47599();
      if (fps != this.lastFps || this.fpsText.get().isEmpty()) {
         this.fpsText.set(String.valueOf(fps));
         this.lastFps = fps;
      }

      if (pings != this.lastPing || this.pingText.get().isEmpty()) {
         this.pingText.set(String.valueOf(pings));
         this.lastPing = pings;
      }

      float userW = fonts.getWidth(user, TEXT);
      float fpsValueW = this.fpsText.width(fonts, TEXT);
      float pingValueW = this.pingText.width(fonts, TEXT);
      float fpsW = fpsValueW + fonts.getWidth("fps", TEXT);
      float msW = pingValueW + fonts.getWidth("ms", TEXT);
      float userIconW = Fonts.mentions_2.getWidth("N", ICON);
      float fpsIconW = Fonts.fps.getWidth("\ue000", ICON);
      float msIconW = Fonts.ping.getWidth("\ue000", ICON);
      float userBlockW = userIconW + INNER + userW;
      float fpsBlockW = fpsIconW + INNER + fpsW;
      float msBlockW = msIconW + INNER + msW;
      float iconLogoSize = 9.0F * S;
      float logoTextW = Fonts.sf_bold.getWidth("Lunarix", TEXT);
      float logoW = iconLogoSize + 3.5F * S + logoTextW;
      float totalW = PAD * 2.0F + logoW + userBlockW + fpsBlockW + msBlockW + GAP * 6.0F + SEP_W * 3.0F;
      RenderUtil.Blur.blur(x, y, totalW, H, 1.0F, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue()));
      Draw.outline(x, y, totalW, H, 0.75F, ThemeColor.getOutlineColor(0.08F), RADIUS);
      float cursor = x + PAD;
      RenderUtil.Images.texture(
         class_2960.method_60655("client", "textures/logo.png"), cursor, y + (H - iconLogoSize) / 2.0F, iconLogoSize, iconLogoSize, ColorUtil.getColor(255)
      );
      Fonts.sf_bold.draw("Lunarix", cursor + iconLogoSize + 3.5F * S, y + TEXT_Y, TEXT, ColorUtil.getClientColor(1));
      float var34;
      this.drawSeparator(var34 = cursor + logoW + GAP, y);
      Fonts.mentions_2.draw("N", cursor = var34 + SEP_W + GAP, y + ICON_Y, ICON, ColorUtil.getClientColor(1));
      fonts.draw(user, cursor + userIconW + INNER, y + TEXT_Y, TEXT, ThemeColor.getTextColor());
      float var36;
      this.drawSeparator(var36 = cursor + userBlockW + GAP, y);
      Fonts.fps.draw("\ue000", cursor = var36 + SEP_W + GAP, y + ICON_Y, ICON, ThemeColor.getHudColor());
      this.drawValue(fonts, this.fpsText, "fps", cursor + fpsIconW + INNER, y + TEXT_Y, fpsValueW);
      float var38;
      this.drawSeparator(var38 = cursor + fpsBlockW + GAP, y);
      Fonts.ping.draw("\ue000", cursor = var38 + SEP_W + GAP, y + ICON_Y, ICON, ThemeColor.getHudColor());
      this.drawValue(fonts, this.pingText, "ms", cursor + msIconW + INNER, y + TEXT_Y, pingValueW);
      long now = System.nanoTime();
      float elapsed = this.mediaLastFrame == 0L ? 0.016666668F : Math.max(0.0F, Math.min(0.05F, (float)(now - this.mediaLastFrame) / 1.0E9F));
      this.mediaLastFrame = now;
      float step = 1.0F - (float)Math.exp((double)(-12.0F * elapsed));
      boolean hasMedia = interFace.islandMusic.getValue() && media != null && media.hasMedia();
      this.mediaAlpha = Math.max(0.0F, Math.min(1.0F, this.mediaAlpha + ((hasMedia ? 1.0F : 0.0F) - this.mediaAlpha) * step));
      float mediaH = 34.0F * S;
      float visibleMediaH = mediaH * this.mediaAlpha;
      if (visibleMediaH > 0.5F && hasMedia) {
         this.drawMediaRow(dragSetting.position.x, y + H + 3.0F * S, Math.max(totalW, 96.0F * S), visibleMediaH, mediaH, media.info());
      }

      dragSetting.size.set(totalW, H + (visibleMediaH > 0.5F ? visibleMediaH + 3.0F * S * this.mediaAlpha : 0.0F));
   }

   @Override
   public void onRender(DragSetting dragSetting, InterFace interFace) {
      this.onRender(dragSetting, interFace, (MusicHud.MediaState)null);
   }

   private void drawMediaRow(float x, float y, float w, float visibleH, float fullH, MediaInfo info) {
      float a = this.mediaAlpha;
      float pad = 5.0F * S;
      float artSize = fullH - pad * 2.0F;
      RenderUtil.Blur.blur(x, y, w, visibleH, a, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue() * a));
      Draw.outline(x, y, w, visibleH, 0.75F, ThemeColor.getOutlineColor(a * 0.08F), RADIUS);
      Scissor.enable(x, y, w, visibleH, 2.0F);

      try {
         if (MediaTextureUtil.hasArtwork()) {
            RenderUtil.Images.texture(MediaTextureUtil.ARTWORK_ID, x + pad, y + pad, artSize, artSize, 1.0F, 5.0F * S, ColorUtil.getColor(255, a));
         } else {
            RenderUtil.Render2D.rect(x + pad, y + pad, artSize, artSize, ColorUtil.multAlpha(new Color(30, 30, 36).getRGB(), a), 5.0F * S);
            Fonts.icon.drawCentered("M", x + pad + artSize / 2.0F, y + pad + artSize / 2.0F - 1.75F * S, 5.0F * S, ThemeColor.getHudColor(a));
         }

         float textX = x + pad * 2.0F + artSize;
         float textW = x + w - pad - textX - 9.0F * S;
         String title = info.getTitle() != null && !info.getTitle().isBlank() ? info.getTitle() : "Нет трека";
         String artist = info.getArtist() != null && !info.getArtist().isBlank() ? info.getArtist() : "—";
         Scissor.enable(textX, y + pad, Math.max(0.0F, textW), artSize, 2.0F);
         Fonts.sf_medium.drawFadingText(title, textX, y + pad + 1.0F * S, Math.max(0.0F, textW), ColorUtil.getColor(255, a), 6.5F * S);
         Fonts.sf_regular.drawFadingText(artist, textX, y + pad + 9.5F * S, Math.max(0.0F, textW), ColorUtil.getColor(180, a), 5.5F * S);
         Scissor.disable();
         long duration = info.getDuration();
         float barY = y + fullH - pad - 2.0F * S;
         float barW = w - pad * 2.0F;
         RenderUtil.Render2D.rect(x + pad, barY, barW, 1.4F * S, ThemeColor.getHudColor(a * 0.12F), 0.7F * S);
         if (duration > 0L) {
            float progress = Math.max(0.0F, Math.min(1.0F, (float)info.getPosition() / (float)duration));
            if (progress > 0.0F) {
               RenderUtil.Render2D.rect(x + pad, barY, barW * progress, 1.4F * S, ColorUtil.multAlpha(ColorUtil.getClientColor(1), a), 0.7F * S);
            }
         }

         Fonts.icon.drawCentered(info.getPlaying() ? "V" : "M", x + w - pad - 4.0F * S, y + pad + 2.0F * S, 5.5F * S, ThemeColor.getHudColor(a));
      } finally {
         Scissor.disable();
      }
   }

   private void drawSeparator(float x, float y) {
      RenderUtil.Render2D.rect(x, y + SEP_Y, SEP_W, H - SEP_Y * 2.0F, ThemeColor.getSeparatorColor(), 0.25F);
   }

   private void drawValue(Font font, RollingText value, String suffix, float x, float y, float width) {
      value.draw(font, x, y, TEXT, ThemeColor.getTextColor());
      font.draw(suffix, x + width, y, TEXT, ColorUtil.getColor(200));
   }
}
