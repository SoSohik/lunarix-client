package ru.mentions.module.impl.display.interfaceimpl;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.RollingText;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class Information implements element {
   private static float S = 1.0F;
   private static float TEXT = 5.5F * S;
   private static float ICON = 4.5F * S;
   private static float H = 16.0F * S;
   private static float RADIUS = 7.0F * S;
   private static float ICON_X = 6.0F * S;
   private static float ICON_Y = 5.7F * S;
   private static float TEXT_X = 14.0F * S;
   private static float TEXT_Y = 4.4F * S;
   private static float BLOCK = 15.0F * S;
   private static float AFTER_SEP = 7.0F * S;
   private static float AFTER_ICON = 9.0F * S;
   private static final String SEPARATOR = " ; ";
   private final RollingText coordX = new RollingText(3.0F);
   private final RollingText coordY = new RollingText(3.0F);
   private final RollingText coordZ = new RollingText(3.0F);
   private final RollingText bpsText = new RollingText(3.0F);
   private final RollingText tpsText = new RollingText(3.0F);
   private int lastX;
   private int lastY;
   private int lastZ;
   private float lastBps;
   private float lastTps;

   @Override
   public void onRender(DragSetting dragSetting, InterFace interFace) {
      S = InterFace.getInstance().sizeHud.getValue() * 1.1F;
      TEXT = 5.5F * S;
      ICON = 4.5F * S;
      H = 16.0F * S;
      RADIUS = 7.0F * S;
      ICON_X = 6.0F * S;
      ICON_Y = 5.7F * S;
      TEXT_X = 14.0F * S;
      TEXT_Y = 4.4F * S;
      BLOCK = 15.0F * S;
      AFTER_SEP = 7.0F * S;
      AFTER_ICON = 9.0F * S;
      float x = dragSetting.position.x;
      float y = dragSetting.position.y;
      double dx = mc.field_1724.method_23317() - mc.field_1724.field_6014;
      double dz = mc.field_1724.method_23321() - mc.field_1724.field_5969;
      float bps = (float)(Math.sqrt(dx * dx + dz * dz) * 20.0);
      Font fonts = Fonts.sf_regular;
      int playerX = (int)mc.field_1724.method_23317();
      int playerY = (int)mc.field_1724.method_23318();
      int playerZ = (int)mc.field_1724.method_23321();
      if (playerX != this.lastX || this.coordX.get().isEmpty()) {
         this.coordX.set(String.valueOf(playerX));
         this.lastX = playerX;
      }

      if (playerY != this.lastY || this.coordY.get().isEmpty()) {
         this.coordY.set(String.valueOf(playerY));
         this.lastY = playerY;
      }

      if (playerZ != this.lastZ || this.coordZ.get().isEmpty()) {
         this.coordZ.set(String.valueOf(playerZ));
         this.lastZ = playerZ;
      }

      if (Float.floatToIntBits(bps) != Float.floatToIntBits(this.lastBps) || this.bpsText.get().isEmpty()) {
         this.bpsText.set(String.format(Locale.US, "%.1f", bps));
         this.lastBps = bps;
      }

      float tps = ServerUtil.TPS;
      if (Float.floatToIntBits(tps) != Float.floatToIntBits(this.lastTps) || this.tpsText.get().isEmpty()) {
         this.tpsText.set(String.format(Locale.US, "%.1f", tps));
         this.lastTps = tps;
      }

      float coordXW = this.coordX.width(fonts, TEXT);
      float coordYW = this.coordY.width(fonts, TEXT);
      float coordZW = this.coordZ.width(fonts, TEXT);
      float bpsValueW = this.bpsText.width(fonts, TEXT);
      float tpsValueW = this.tpsText.width(fonts, TEXT);
      float separatorW = fonts.getWidth(" ; ", TEXT);
      float bpsSuffixW = fonts.getWidth("bps", TEXT);
      float tpsSuffixW = fonts.getWidth("tps", TEXT);
      float coordsW = coordXW + coordYW + coordZW + fonts.getWidth("xyz", TEXT) + separatorW * 2.0F;
      float bpsW = bpsValueW + bpsSuffixW;
      float tpsW = tpsValueW + tpsSuffixW;
      float w = BLOCK + coordsW;
      float w2 = 57.0F * S + coordsW + bpsW + tpsW;
      Draw.glow(x, y, w2, H - 0.5F, ColorUtil.getColor(0, 0.1F), RADIUS, 8.0F, 1.0F);
      RenderUtil.Blur.blur(x, y, w2, H, 1.0F, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue()));
      Draw.outline(x, y, w2, H, 0.75F, ThemeColor.getOutlineColor(0.08F), RADIUS);
      Fonts.mentions_2.draw("W", x + ICON_X, y + ICON_Y, ICON, ColorUtil.getClientColor(1));
      float cursor = x + TEXT_X;
      cursor += this.drawValue(fonts, this.coordX, "x", cursor, y + TEXT_Y, coordXW, fonts.getWidth("x", TEXT));
      cursor += this.drawSeparator(fonts, cursor, y + TEXT_Y, separatorW);
      cursor += this.drawValue(fonts, this.coordY, "y", cursor, y + TEXT_Y, coordYW, fonts.getWidth("y", TEXT));
      cursor += this.drawSeparator(fonts, cursor, y + TEXT_Y, separatorW);
      cursor += this.drawValue(fonts, this.coordZ, "z", cursor, y + TEXT_Y, coordZW, fonts.getWidth("z", TEXT));
      float x2 = x + w + 2.0F * S;
      Fonts.icon.draw("C", x2, y + ICON_Y, ICON, ThemeColor.getSeparatorColor());
      float var36;
      Fonts.mentions_2.draw("H", var36 = x2 + AFTER_SEP, y + ICON_Y, ICON, ThemeColor.getHudColor());
      this.drawValue(fonts, this.bpsText, "bps", x2 = var36 + AFTER_ICON, y + TEXT_Y, bpsValueW, bpsSuffixW);
      float x3 = x + w + 20.0F * S + bpsW;
      Fonts.icon.draw("C", x3, y + ICON_Y, ICON, ThemeColor.getSeparatorColor());
      float var38;
      Fonts.mentions_2.draw("V", var38 = x3 + AFTER_SEP, y + ICON_Y, ICON, ThemeColor.getHudColor());
      this.drawValue(fonts, this.tpsText, "tps", x3 = var38 + AFTER_ICON, y + TEXT_Y, tpsValueW, tpsSuffixW);
      dragSetting.size.set(w2, H);
   }

   private float drawValue(Font font, RollingText value, String suffix, float x, float y, float width, float suffixWidth) {
      value.draw(font, x, y, TEXT, ThemeColor.getTextColor());
      font.draw(suffix, x + width, y, TEXT, ColorUtil.getColor(220));
      return width + suffixWidth;
   }

   private float drawSeparator(Font font, float x, float y, float width) {
      font.draw(" ; ", x, y, TEXT, ThemeColor.getDarkTextColor());
      return width;
   }
}
