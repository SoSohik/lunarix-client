package ru.mentions.lang;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.RenderUtil;

@Environment(EnvType.CLIENT)
public final class FlagRenderer {
   private FlagRenderer() {
   }

   public static void drawFlag(Lang.Language lang, float x, float y, float diameter, float alpha, boolean selected, boolean hovered) {
      float r = diameter / 2.0F;
      int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
      if (a > 0) {
         if (selected) {
            RenderUtil.Render2D.glow(
               x - 1.0F, y - 1.0F, diameter + 2.0F, diameter + 2.0F, ColorUtil.replAlpha(ColorUtil.client(), 0.55F * alpha), r + 1.0F, 9.0F, 1.0F
            );
         } else if (hovered) {
            RenderUtil.Render2D.glow(x, y, diameter, diameter, ColorUtil.getColor(255, 0.28F * alpha), r, 6.0F, 1.0F);
         }

         switch (lang) {
            case UKRAINIAN:
               float halfH = diameter / 2.0F;
               Draw.rect(x, y, diameter, halfH, ColorUtil.getColor(0, 87, 183, a), r, r, 0.0F, 0.0F);
               Draw.rect(x, y + halfH, diameter, halfH, ColorUtil.getColor(255, 215, 0, a), 0.0F, 0.0F, r, r);
               break;
            case RUSSIAN:
               float sH = diameter / 3.0F;
               Draw.rect(x, y, diameter, sH, ColorUtil.getColor(255, 255, 255, a), r, r, 0.0F, 0.0F);
               Draw.rect(x, y + sH, diameter, sH + 0.4F, ColorUtil.getColor(0, 57, 166, a), 0.0F, 0.0F, 0.0F, 0.0F);
               Draw.rect(x, y + sH * 2.0F, diameter, diameter - sH * 2.0F, ColorUtil.getColor(213, 43, 30, a), 0.0F, 0.0F, r, r);
               break;
            case ENGLISH:
               Draw.rect(x, y, diameter, diameter, ColorUtil.getColor(1, 33, 105, a), r);
               float barW = diameter * 0.28F;
               float redW = diameter * 0.16F;
               float halfBar = barW / 2.0F;
               float halfRed = redW / 2.0F;
               float cx = x + r;
               float cy = y + r;
               Draw.rect(x, cy - halfBar, diameter, barW, ColorUtil.getColor(255, 255, 255, a), 0.0F);
               Draw.rect(cx - halfBar, y, barW, diameter, ColorUtil.getColor(255, 255, 255, a), 0.0F);
               Draw.rect(x, cy - halfRed, diameter, redW, ColorUtil.getColor(200, 16, 46, a), 0.0F);
               Draw.rect(cx - halfRed, y, redW, diameter, ColorUtil.getColor(200, 16, 46, a), 0.0F);
         }

         int outlineCol = selected
            ? ColorUtil.replAlpha(ColorUtil.client(), alpha)
            : (hovered ? ColorUtil.getColor(255, 0.85F * alpha) : ColorUtil.getColor(70, 70, 90, (int)(160.0F * alpha)));
         float outlineThick = selected ? 1.4F : (hovered ? 1.0F : 0.6F);
         RenderUtil.Render2D.outline(x, y, diameter, diameter, outlineThick, outlineCol, r);
      }
   }

   public static float getTotalWidth(float diameter, float gap) {
      return diameter * 3.0F + gap * 2.0F;
   }
}
