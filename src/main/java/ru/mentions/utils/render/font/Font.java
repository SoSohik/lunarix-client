package ru.mentions.utils.render.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.utils.colors.ColorFormatting;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public class Font {
   private final String name;

   public Font(String name) {
      this.name = name;
   }

   public void draw(String text, float x, float y, float size, int color) {
      Client.get().render2D().getFontRenderer().drawText(this.name, text, x, y, size, color);
   }

   public void drawCentered(String text, float x, float y, float size, int color) {
      Client.get().render2D().getFontRenderer().drawCenteredText(this.name, text, x, y, size, color);
   }

   public void drawCenterGradient(String text, float x, float y, float size, int color, int color2) {
      this.drawGradient(text, x - this.getWidth(text, size) / 2.0F, y, color, color2, size);
   }

   public void drawGradient(String text, float x, float y, int color1, int color2, float size) {
      this.drawGradient(text, x, y, color1, color2, size, 8);
   }

   public void drawGradient(String text, float x, float y, int color1, int color2, float size, int speed) {
      StringBuilder sb = new StringBuilder(text.length() * 12);

      for (int i = 0; i < text.length(); i++) {
         char ch = text.charAt(i);
         sb.append(ColorFormatting.getColor(ColorUtil.fade(speed, i * speed, color1, color2))).append(ch);
      }

      this.draw(sb.toString(), x, y, size, -1);
   }

   public void drawGradientB(String text, float x, float y, int color1, int color2, float size) {
      if (text != null && !text.isEmpty()) {
         int len = text.length();
         if (len == 1) {
            this.draw(ColorFormatting.getColor(color1) + text, x, y, size, -1);
         } else {
            int a1 = color1 >> 24 & 0xFF;
            int r1 = color1 >> 16 & 0xFF;
            int g1 = color1 >> 8 & 0xFF;
            int b1 = color1 & 0xFF;
            int a2 = color2 >> 24 & 0xFF;
            int r2 = color2 >> 16 & 0xFF;
            int g2 = color2 >> 8 & 0xFF;
            int b2 = color2 & 0xFF;
            float inv = 1.0F / (float)(len - 1);
            StringBuilder sb = new StringBuilder(len * 12);

            for (int i = 0; i < len; i++) {
               float t = (float)i * inv;
               t = 0.5F - 0.5F * (float)Math.cos(Math.PI * (double)t);
               int a = a1 + Math.round((float)(a2 - a1) * t);
               int r = r1 + Math.round((float)(r2 - r1) * t);
               int g = g1 + Math.round((float)(g2 - g1) * t);
               int b = b1 + Math.round((float)(b2 - b1) * t);
               int argb = a << 24 | r << 16 | g << 8 | b;
               sb.append(ColorFormatting.getColor(argb)).append(text.charAt(i));
            }

            this.draw(sb.toString(), x, y, size, -1);
         }
      }
   }

   public void drawGRS(CharSequence text, double x, double y, int c1, int c2, float size) {
      if (text != null) {
         int len = text.length();
         if (len != 0) {
            float xOff = 0.0F;
            boolean single = len == 1;
            float invDen = single ? 0.0F : 1.0F / (float)(len - 1);
            int a1 = c1 >>> 24 & 0xFF;
            int r1 = c1 >>> 16 & 0xFF;
            int g1 = c1 >>> 8 & 0xFF;
            int b1 = c1 & 0xFF;
            int a2 = c2 >>> 24 & 0xFF;
            int r2 = c2 >>> 16 & 0xFF;
            int g2 = c2 >>> 8 & 0xFF;
            int b2 = c2 & 0xFF;
            StringBuilder sb = new StringBuilder(1);
            sb.append('\u0000');

            for (int i = 0; i < len; i++) {
               char ch = text.charAt(i);
               float t = single ? 0.0F : (float)i * invDen;
               int a = a1 + Math.round((float)(a2 - a1) * t);
               int r = r1 + Math.round((float)(r2 - r1) * t);
               int g = g1 + Math.round((float)(g2 - g1) * t);
               int b = b1 + Math.round((float)(b2 - b1) * t);
               int argb = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
               sb.setCharAt(0, ch);
               String chunk = ColorFormatting.getColor(argb) + sb;
               this.draw(chunk, (float)(x + (double)xOff), (float)y, size, -1);
               xOff += this.getWidth(chunk, size);
            }
         }
      }
   }

   public float getWidth(String text, float size) {
      return Client.get().render2D().getFontRenderer().getTextWidth(this.name, text, size);
   }

   public void drawFadingText(String text, float x, float y, float maxWidth, int color, float size) {
      Client.get().render2D().getFontRenderer().drawTextFading(this.name, text, x, y, size, maxWidth, color);
   }

   public void drawFadingTextReverse(String text, float x, float y, float maxWidth, int color, float size) {
      Client.get().render2D().getFontRenderer().drawTextFadingReverse(this.name, text, x, y, size, maxWidth, color);
   }

   public void drawWrappedText(String text, float x, float y, float maxWidth, int color, float size) {
      String[] words = text.split(" ");
      StringBuilder currentLine = new StringBuilder();
      float currentY = y;

      for (String word : words) {
         Object potentialLine = currentLine.length() == 0 ? word : currentLine + " " + word;
         float lineWidth = this.getWidth((String)potentialLine, size);
         if (lineWidth > maxWidth) {
            this.draw(currentLine.toString(), x, currentY, size, color);
            currentLine = new StringBuilder(word);
            currentY += this.getHeight(size);
         } else {
            currentLine = new StringBuilder((String)potentialLine);
         }
      }

      if (currentLine.length() > 0) {
         this.draw(currentLine.toString(), x, currentY, size, color);
      }
   }

   public float getWrappedHeight(String text, float maxWidth, float size) {
      if (text != null && !text.isEmpty()) {
         String[] words = text.split(" ");
         StringBuilder currentLine = new StringBuilder();
         int lines = 1;

         for (String word : words) {
            String potentialLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (this.getWidth(potentialLine, size) > maxWidth) {
               currentLine = new StringBuilder(word);
               lines++;
            } else {
               currentLine = new StringBuilder(potentialLine);
            }
         }

         return (float)lines * this.getHeight(size);
      } else {
         return 0.0F;
      }
   }

   public void drawRightAlignFadeLeft(String text, float rightX, float y, float fadeStartX, int color, float size) {
      Client.get().render2D().getFontRenderer().drawTextRightAlignFadeLeft(this.name, text, rightX, y, size, fadeStartX, color);
   }

   public float getHeight(float size) {
      return Client.get().render2D().getFontRenderer().getLineHeight(this.name, size);
   }

   public String getName() {
      return this.name;
   }
}
