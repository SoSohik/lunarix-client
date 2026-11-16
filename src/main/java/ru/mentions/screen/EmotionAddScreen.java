package ru.mentions.screen;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.module.impl.render.Emotions;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.emote.EmoteLibrary;
import ru.mentions.utils.emotion.EmotionRegistry;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class EmotionAddScreen extends class_437 implements IMinecraft {
   private static final int COLS = 4;
   private static final int ROWS = 3;
   private static final int PER_PAGE = 12;
   private static final float WIN_W = 560.0F;
   private static final float WIN_H = 330.0F;
   private static final float CARD_RADIUS = 7.0F;
   private final class_437 parent;
   private final float[] cardHover = new float[12];
   private final float[] sectionHover = new float[EmotionRegistry.Section.values().length];
   private float scaleFix = 1.0F;
   private float open = 0.0F;
   private float closing = 0.0F;
   private float pageSlide = 0.0F;
   private int sectionIndex = 0;
   private int page = 0;
   private boolean closingNow = false;

   public EmotionAddScreen(class_437 parent) {
      super(class_2561.method_43470("Emotions"));
      this.parent = parent;
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   private static float approach(float current, float target, float speed) {
      return current + (target - current) * speed;
   }

   private List<EmotionRegistry.Emotion> currentList() {
      return EmotionRegistry.bySection(EmotionRegistry.Section.values()[this.sectionIndex]);
   }

   private int listPages() {
      return Math.max(1, (this.currentList().size() + 12 - 1) / 12);
   }

   private float winX(float w) {
      return (w - 560.0F) / 2.0F;
   }

   private float winY(float h) {
      return (h - 330.0F) / 2.0F;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         Emotions module = Emotions.getInstance();
         if (module != null && mc.field_1724 != null) {
            float currentScale = (float)mc.method_22683().method_4495();
            this.scaleFix = 2.0F / currentScale;
            float mx = (float)mouseX / this.scaleFix;
            float my = (float)mouseY / this.scaleFix;
            float w = (float)mc.method_22683().method_4486() / this.scaleFix;
            float h = (float)mc.method_22683().method_4502() / this.scaleFix;
            this.open = approach(this.open, this.closingNow ? 0.0F : 1.0F, this.closingNow ? 0.3F : 0.18F);
            this.closing = approach(this.closing, this.closingNow ? 1.0F : 0.0F, 0.3F);
            this.pageSlide = approach(this.pageSlide, 0.0F, 0.2F);
            float a = Math.max(0.0F, Math.min(1.0F, this.open)) * Math.max(0.0F, Math.min(1.0F, 1.0F - this.closing));
            if (a <= 0.01F && this.closingNow) {
               super.method_25419();
            } else {
               int aWhite = (int)(255.0F * a);
               int accent = ColorUtil.getClientColor(1);
               Render2D.beginOverlay();
               RenderUtil.Render2D.rect(0.0F, 0.0F, w, h, ColorUtil.getColor(0, 0, 0, (int)(120.0F * a)));
               float scaleIn = 0.95F + 0.05F * a;
               float wx = this.winX(w);
               float wy = this.winY(h);
               RenderUtil.Blur.blur(wx, wy, 560.0F, 330.0F, a, 10.0F, ColorUtil.replAlpha(ColorUtil.background(), 0.8F * a));
               RenderUtil.Render2D.rect(wx, wy, 560.0F, 330.0F, ColorUtil.getColor(255, 255, 255, (int)(7.0F * a)), 10.0F);
               RenderUtil.Render2D.outline(wx, wy, 560.0F, 330.0F, 0.75F, ThemeColor.getOutlineColor(a * 0.18F), 10.0F);
               RenderUtil.Render2D.rect(wx, wy, 560.0F, 26.0F, ColorUtil.getColor(255, 255, 255, (int)(6.0F * a)), 10.0F);
               RenderUtil.Render2D.rect(wx + 12.0F, wy + 25.0F, 536.0F, 0.5F, ThemeColor.getOutlineColor(a * 0.22F), 0.25F);
               Fonts.sf_bold.draw("ЭМОЦИИ", wx + 12.0F, wy + 8.0F, 9.0F, ColorUtil.getColor(255, 255, 255, aWhite));
               Fonts.sf_regular
                  .draw(EmoteLibrary.size() + " эмоций", wx + 560.0F - 76.0F, wy + 9.0F, 7.0F, ColorUtil.getColor(205, 213, 230, (int)(215.0F * a)));
               boolean closeHover = MathUtil.isHovered(mx, my, wx + 560.0F - 22.0F, wy + 5.0F, 16.0F, 14.0F);
               float cbx = wx + 560.0F - 24.0F;
               float cby = wy + 6.0F;
               RenderUtil.Render2D.rect(
                  cbx,
                  cby,
                  16.0F,
                  16.0F,
                  closeHover ? ColorUtil.getColor(255, 95, 95, (int)(150.0F * a)) : ColorUtil.getColor(255, 255, 255, (int)(10.0F * a)),
                  5.0F
               );
               RenderUtil.Render2D.outline(cbx, cby, 16.0F, 16.0F, 0.75F, ThemeColor.getOutlineColor(a * (closeHover ? 0.45F : 0.18F)), 5.0F);

               for (int k = 0; k < 7; k++) {
                  RenderUtil.Render2D.rect(cbx + 4.5F + (float)k, cby + 4.5F + (float)k, 1.0F, 1.0F, ColorUtil.getColor(235, 240, 250, aWhite), 0.0F);
                  RenderUtil.Render2D.rect(cbx + 10.5F - (float)k, cby + 4.5F + (float)k, 1.0F, 1.0F, ColorUtil.getColor(235, 240, 250, aWhite), 0.0F);
               }

               float secX = wx + 10.0F;
               float secY = wy + 32.0F;
               float secW = 96.0F;
               float secH = 18.0F;
               EmotionRegistry.Section[] sections = EmotionRegistry.Section.values();

               for (int i = 0; i < sections.length; i++) {
                  float sy = secY + (float)i * (secH + 4.0F);
                  boolean hovered = MathUtil.isHovered(mx, my, secX, sy, secW, secH);
                  this.sectionHover[i] = approach(this.sectionHover[i], hovered ? 1.0F : 0.0F, 0.22F);
                  boolean active = i == this.sectionIndex;
                  float grow = 1.0F + 0.02F * this.sectionHover[i];
                  RenderUtil.Render2D.rect(
                     secX,
                     sy,
                     secW * grow,
                     secH,
                     active ? ColorUtil.replAlpha(accent, 0.32F * a) : ColorUtil.getColor(255, 255, 255, (int)((8.0F + 14.0F * this.sectionHover[i]) * a)),
                     5.0F
                  );
                  if (active) {
                     RenderUtil.Render2D.rect(secX + 2.0F, sy + 3.0F, 2.0F, secH - 6.0F, ColorUtil.replAlpha(accent, 0.85F * a), 1.0F);
                  }

                  Fonts.sf_regular.draw(sections[i].label, secX + 10.0F, sy + 6.0F, 7.0F, ColorUtil.getColor(232, 238, 250, aWhite));
               }

               List<EmotionRegistry.Emotion> list = this.currentList();
               int start = this.page * 12;
               float gridX = secX + secW + 10.0F;
               float gridY = secY;
               float gridW = wx + 560.0F - 10.0F - gridX;
               float cardW = (gridW - 18.0F) / 4.0F;
               float cardH = 72.0F;

               for (int i = 0; i < 12; i++) {
                  int index = start + i;
                  if (index >= list.size()) {
                     this.cardHover[i] = approach(this.cardHover[i], 0.0F, 0.25F);
                  } else {
                     EmotionRegistry.Emotion emotion = list.get(index);
                     int col = i % 4;
                     int row = i / 4;
                     float stagger = Math.max(0.0F, Math.min(1.0F, a * 1.6F - (float)i * 0.05F));
                     float slide = this.pageSlide * 40.0F * (1.0F - stagger * 0.2F);
                     float cx = gridX + (float)col * (cardW + 6.0F) + slide;
                     float cy = gridY + (float)row * (cardH + 6.0F) + (1.0F - stagger) * 12.0F;
                     boolean hovered = MathUtil.isHovered(mx, my, cx, cy, cardW, cardH);
                     this.cardHover[i] = approach(this.cardHover[i], hovered ? 1.0F : 0.0F, 0.24F);
                     float hv = this.cardHover[i] * stagger;
                     boolean placed = module.hasEmotion(emotion.name());
                     float zoom = 1.0F + 0.035F * hv;
                     float cw = cardW * zoom;
                     float chh = cardH * zoom;
                     float ox = cx - (cw - cardW) / 2.0F;
                     float oy = cy - (chh - cardH) / 2.0F;
                     RenderUtil.Blur.blur(ox, oy, cw, chh, a, 7.0F, ColorUtil.replAlpha(ColorUtil.background(), (0.48F + 0.18F * hv) * a));
                     RenderUtil.Render2D.rect(ox, oy, cw, chh, ColorUtil.getColor(255, 255, 255, (int)((6.0F + 12.0F * hv) * a)), 7.0F);
                     RenderUtil.Render2D.outline(
                        ox, oy, cw, chh, 0.75F, hv > 0.03F ? ColorUtil.replAlpha(accent, a * (0.2F + 0.35F * hv)) : ThemeColor.getOutlineColor(a * 0.1F), 7.0F
                     );
                     if (emotion.icon() != null) {
                        float plate = 34.0F * (1.0F + 0.06F * hv);
                        RenderUtil.Render2D.rect(
                           ox + cw / 2.0F - plate / 2.0F, oy + 5.0F, plate, plate, ColorUtil.getColor(255, 255, 255, (int)((6.0F + 10.0F * hv) * a)), 6.0F
                        );
                        float iconSize = 30.0F * (1.0F + 0.08F * hv);
                        RenderUtil.Images.texture(
                           emotion.icon(),
                           ox + cw / 2.0F - iconSize / 2.0F,
                           oy + 7.0F,
                           iconSize,
                           iconSize,
                           0.0F,
                           0.0F,
                           1.0F,
                           1.0F,
                           ColorUtil.getColor(255, 255, 255, (int)(255.0F * stagger)),
                           0.0F,
                           4.0F
                        );
                     }

                     Fonts.sf_regular
                        .drawCentered(
                           shorten(emotion.display() == null ? emotion.name() : emotion.display()),
                           ox + cw / 2.0F,
                           oy + 41.0F,
                           6.5F,
                           ColorUtil.getColor(238, 242, 252, (int)(255.0F * stagger))
                        );
                     if (placed) {
                        RenderUtil.Render2D.rect(ox + cw - 14.0F, oy + 4.0F, 10.0F, 10.0F, ColorUtil.getColor(120, 235, 160, (int)(220.0F * a)), 3.0F);
                     }

                     RenderUtil.Render2D.rect(
                        ox + 6.0F,
                        oy + chh - 20.0F,
                        cw - 12.0F,
                        15.0F,
                        ColorUtil.replAlpha(accent, (MathUtil.isHovered(mx, my, cx + 6.0F, cy + cardH - 20.0F, cardW - 12.0F, 15.0F) ? 0.65F : 0.4F) * a),
                        4.0F
                     );
                     Fonts.sf_bold.drawCentered("ДОБАВИТЬ", ox + cw / 2.0F, oy + chh - 16.0F, 6.0F, ColorUtil.getColor(255, 255, 255, (int)(255.0F * stagger)));
                  }
               }

               float pageX = gridX + gridW - 56.0F;
               float pageY = wy + 330.0F - 22.0F;
               boolean prevHover = MathUtil.isHovered(mx, my, pageX, pageY, 18.0F, 16.0F);
               boolean nextHover = MathUtil.isHovered(mx, my, pageX + 38.0F, pageY, 18.0F, 16.0F);
               RenderUtil.Render2D.rect(pageX, pageY, 18.0F, 16.0F, ColorUtil.getColor(255, 255, 255, (int)((prevHover ? 26.0F : 10.0F) * a)), 4.0F);
               RenderUtil.Render2D.rect(pageX + 38.0F, pageY, 18.0F, 16.0F, ColorUtil.getColor(255, 255, 255, (int)((nextHover ? 26.0F : 10.0F) * a)), 4.0F);
               Fonts.sf_bold.drawCentered("<", pageX + 9.0F, pageY + 5.0F, 7.0F, ColorUtil.getColor(235, 240, 252, aWhite));
               Fonts.sf_bold.drawCentered(">", pageX + 47.0F, pageY + 5.0F, 7.0F, ColorUtil.getColor(235, 240, 252, aWhite));
               Fonts.sf_regular
                  .drawCentered(
                     this.page + 1 + " / " + this.listPages(), pageX - 24.0F, pageY + 5.0F, 6.5F, ColorUtil.getColor(190, 198, 216, (int)(215.0F * a))
                  );
               Render2D.endOverlay();
            }
         } else {
            this.method_25419();
         }
      }
   }

   private static String shorten(String value) {
      return value.length() > 14 ? value.substring(0, 13) + "…" : value;
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      this.page = (this.page + (vertical > 0.0 ? -1 : 1) + this.listPages()) % this.listPages();
      this.pageSlide = vertical > 0.0 ? -1.0F : 1.0F;
      return true;
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      Emotions module = Emotions.getInstance();
      if (module != null && mc.field_1724 != null) {
         float mx = (float)(click.comp_4798() / (double)this.scaleFix);
         float my = (float)(click.comp_4799() / (double)this.scaleFix);
         float w = (float)mc.method_22683().method_4486() / this.scaleFix;
         float h = (float)mc.method_22683().method_4502() / this.scaleFix;
         float wx = this.winX(w);
         float wy;
         if (MathUtil.isHovered(mx, my, wx + 560.0F - 22.0F, (wy = this.winY(h)) + 5.0F, 16.0F, 14.0F)) {
            this.method_25419();
            return true;
         } else {
            float secX = wx + 10.0F;
            float secY = wy + 32.0F;
            float secW = 96.0F;
            float secH = 18.0F;
            EmotionRegistry.Section[] sections = EmotionRegistry.Section.values();

            for (int i = 0; i < sections.length; i++) {
               float sy = secY + (float)i * (secH + 4.0F);
               if (MathUtil.isHovered(mx, my, secX, sy, secW, secH)) {
                  this.sectionIndex = i;
                  this.page = 0;
                  this.pageSlide = 1.0F;
                  return true;
               }
            }

            List<EmotionRegistry.Emotion> list = this.currentList();
            int start = this.page * 12;
            float gridX = secX + secW + 10.0F;
            float gridY = secY;
            float gridW = wx + 560.0F - 10.0F - gridX;
            float cardW = (gridW - 18.0F) / 4.0F;
            float cardH = 72.0F;

            int index;
            for (int ix = 0; ix < 12 && (index = start + ix) < list.size(); ix++) {
               int col = ix % 4;
               float cx = gridX + (float)col * (cardW + 6.0F);
               int row = ix / 4;
               float cy = gridY + (float)row * (cardH + 6.0F);
               if (MathUtil.isHovered(mx, my, cx + 6.0F, cy + cardH - 20.0F, cardW - 12.0F, 15.0F)) {
                  mc.method_1507(new EmotionWheelScreen(list.get(index).name()));
                  return true;
               }
            }

            float pageX = gridX + gridW - 56.0F;
            float pageY = wy + 330.0F - 22.0F;
            if (MathUtil.isHovered(mx, my, pageX, pageY, 18.0F, 16.0F)) {
               this.page = (this.page - 1 + this.listPages()) % this.listPages();
               this.pageSlide = -1.0F;
               return true;
            } else if (MathUtil.isHovered(mx, my, pageX + 38.0F, pageY, 18.0F, 16.0F)) {
               this.page = (this.page + 1) % this.listPages();
               this.pageSlide = 1.0F;
               return true;
            } else {
               if (!MathUtil.isHovered(mx, my, wx, wy, 560.0F, 330.0F)) {
                  this.method_25419();
               }

               return true;
            }
         }
      } else {
         this.method_25419();
         return true;
      }
   }

   public void method_25419() {
      this.closingNow = true;
   }
}
