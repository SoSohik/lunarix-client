package ru.mentions.screen;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.module.impl.misc.AutoBuy;
import ru.mentions.module.impl.misc.AutoBuyPresets;
import ru.mentions.module.impl.misc.AutoBuyRule;
import ru.mentions.module.impl.misc.AutoBuyRules;
import ru.mentions.module.impl.misc.AutoParser;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public final class AutoBuyScreen extends class_437 implements IMinecraft {
   private static final float WIN_W = 620.0F;
   private static final float WIN_H = 340.0F;
   private static final float TITLE_H = 26.0F;
   private static final float PAD = 10.0F;
   private static final float GAP = 6.0F;
   private static final float LIST_W = 236.0F;
   private static final int COLS = 2;
   private static final float CARD_H = 26.0F;
   private static final float ROW_H = 18.0F;
   private static final float STEP_W = 34.0F;
   private static final float STEP_GAP = 4.0F;
   private static final float FOOTER_H = 24.0F;
   private static final float TARGET_SCALE = 2.0F;
   private static final int PRICE_STEP = 1000;
   private static final int PRICE_STEP_BIG = 10000;
   private static final int MAX_PRICE = 2000000000;
   private static final int MAX_QUANTITY = 2304;
   private final class_437 parent;
   private int selected = -1;
   private float open;
   private float closing;
   private boolean closingNow;
   private float scroll;
   private float scrollTarget;
   private float scaleFix = 1.0F;
   private float winX;
   private float winY;
   private float listY;
   private float listH;
   private float panelX;
   private float panelW;
   private float maxScroll;
   private int hovered = -1;

   public AutoBuyScreen(class_437 parent) {
      super(class_2561.method_43470("AutoBuy"));
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

   private static String shorten(String value, int limit) {
      if (value == null) {
         return "";
      } else {
         return value.length() > limit ? value.substring(0, limit - 1) + "…" : value;
      }
   }

   private List<AutoBuyRule> rules() {
      return AutoBuyRules.get().all();
   }

   private void layout() {
      float w = (float)mc.method_22683().method_4486() / this.scaleFix;
      float h = (float)mc.method_22683().method_4502() / this.scaleFix;
      this.winX = (w - 620.0F) / 2.0F;
      this.winY = (h - 340.0F) / 2.0F;
      this.listY = this.winY + 26.0F + 6.0F;
      this.listH = 340.0F - (this.listY - this.winY) - 24.0F - 6.0F;
      this.panelX = this.winX + 10.0F + 236.0F + 10.0F;
      this.panelW = this.winX + 620.0F - 10.0F - this.panelX;
      int rows = (this.rules().size() + 2 - 1) / 2;
      float content = (float)rows * 32.0F - (rows > 0 ? 6.0F : 0.0F);
      this.maxScroll = Math.max(0.0F, content - this.listH);
      this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget));
      this.scroll = Math.max(0.0F, Math.min(this.maxScroll, this.scroll));
   }

   private float listX() {
      return this.winX + 10.0F;
   }

   private float panelY() {
      return this.listY;
   }

   private float priceValueW() {
      return this.panelW - 16.0F - 136.0F - 16.0F;
   }

   private float priceY() {
      return this.panelY() + 34.0F;
   }

   private float quantityY() {
      return this.panelY() + 68.0F;
   }

   private float fuzzyY() {
      return this.panelY() + 102.0F;
   }

   private float enabledY() {
      return this.panelY() + 124.0F;
   }

   private float parserY() {
      return this.panelY() + 146.0F;
   }

   private float deleteY() {
      return this.panelY() + this.listH - 18.0F - 8.0F;
   }

   private int cardAt(float mx, float my) {
      List<AutoBuyRule> rules = this.rules();
      int first = Math.max(0, (int)Math.floor((double)(this.scroll / 32.0F)) * 2);

      for (int i = first; i < Math.min(rules.size(), first + 32); i++) {
         int cell = i - first;
         float x = this.listX() + (float)(cell % 2) * (this.listW() + 6.0F);
         float y = this.listY + (float)(cell / 2) * 32.0F - this.scroll;
         if (!(y + 26.0F < this.listY) && !(y > this.listY + this.listH) && MathUtil.isHovered(mx, my, x, y, this.listW(), 26.0F)) {
            return i;
         }
      }

      return -1;
   }

   private float listW() {
      return 115.0F;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         this.scaleFix = 2.0F / (float)mc.method_22683().method_4495();
         float mx = (float)mouseX / this.scaleFix;
         float my = (float)mouseY / this.scaleFix;
         this.open = approach(this.open, this.closingNow ? 0.0F : 1.0F, this.closingNow ? 0.3F : 0.18F);
         this.closing = approach(this.closing, this.closingNow ? 1.0F : 0.0F, 0.3F);
         float a = Math.max(0.0F, Math.min(1.0F, this.open)) * Math.max(0.0F, Math.min(1.0F, 1.0F - this.closing));
         if (this.closingNow && a <= 0.01F) {
            mc.method_1507(this.parent);
         } else {
            this.layout();
            this.scroll = approach(this.scroll, this.scrollTarget, 0.35F);
            this.hovered = this.cardAt(mx, my);
            int aWhite = (int)(255.0F * a);
            int accent = ColorUtil.getClientColor(1);
            float w = (float)mc.method_22683().method_4486() / this.scaleFix;
            float h = (float)mc.method_22683().method_4502() / this.scaleFix;
            float scale = 0.96F + 0.04F * a;
            float dx = this.winX + (620.0F - 620.0F * scale) / 2.0F;
            float dy = this.winY + (340.0F - 340.0F * scale) / 2.0F;
            float dw = 620.0F * scale;
            float dh = 340.0F * scale;
            Render2D.beginOverlay();
            RenderUtil.Render2D.rect(0.0F, 0.0F, w, h, ColorUtil.getColor(0, 0, 0, (int)(120.0F * a)));
            RenderUtil.Blur.blur(dx, dy, dw, dh, a, 10.0F, ColorUtil.replAlpha(ColorUtil.background(), 0.8F * a));
            RenderUtil.Render2D.rect(dx, dy, dw, dh, ColorUtil.getColor(255, 255, 255, (int)(7.0F * a)), 10.0F);
            RenderUtil.Render2D.outline(dx, dy, dw, dh, 0.75F, ThemeColor.getOutlineColor(a * 0.18F), 10.0F);
            RenderUtil.Render2D.rect(dx, dy, dw, 26.0F, ColorUtil.getColor(255, 255, 255, (int)(6.0F * a)), 10.0F);
            RenderUtil.Render2D.rect(dx + 10.0F, dy + 26.0F - 1.0F, dw - 20.0F, 0.5F, ThemeColor.getOutlineColor(a * 0.22F), 0.25F);
            Fonts.sf_bold.draw("АВТОБАЙ", dx + 10.0F + 2.0F, dy + 8.0F, 9.0F, ColorUtil.getColor(255, 255, 255, aWhite));
            Fonts.sf_regular
               .draw(this.rules().size() + " правил", dx + dw - 10.0F - 52.0F, dy + 9.0F, 6.5F, ColorUtil.getColor(190, 198, 216, (int)(220.0F * a)));
            Render2D.endOverlay();
            this.drawList(a, aWhite, accent);
            this.drawPanel(mx, my, a, aWhite, accent);
            this.drawFooter(mx, my, a, aWhite, accent);
         }
      }
   }

   private void drawList(float a, int aWhite, int accent) {
      List<AutoBuyRule> rules = this.rules();
      Scissor.enable(this.listX(), this.listY, 236.0F, this.listH, 2.0F);
      Render2D.beginOverlay();

      for (int i = 0; i < rules.size(); i++) {
         AutoBuyRule rule = rules.get(i);
         int cell = i - Math.max(0, (int)Math.floor((double)(this.scroll / 32.0F)) * 2);
         float x = this.listX() + (float)(cell % 2) * (this.listW() + 6.0F);
         float y = this.listY + (float)(cell / 2) * 32.0F - this.scroll;
         if (!(y + 26.0F < this.listY) && !(y > this.listY + this.listH)) {
            boolean on = i == this.selected;
            float hv = i == this.hovered ? 1.0F : 0.0F;
            RenderUtil.Render2D.rect(x, y, this.listW(), 26.0F, ColorUtil.getColor(255, 255, 255, (int)((6.0F + 10.0F * hv) * a)), 5.0F);
            if (on) {
               RenderUtil.Render2D.rect(x, y, this.listW(), 26.0F, ColorUtil.replAlpha(accent, 0.16F * a), 5.0F);
            }

            if (!rule.enabled) {
               RenderUtil.Render2D.rect(x, y, 2.0F, 26.0F, ColorUtil.getColor(150, 158, 176, (int)(200.0F * a)), 1.0F);
            }

            RenderUtil.Render2D.outline(
               x,
               y,
               this.listW(),
               26.0F,
               0.75F,
               on ? ColorUtil.replAlpha(accent, a * 0.55F) : (hv > 0.0F ? ColorUtil.replAlpha(accent, a * 0.28F) : ThemeColor.getOutlineColor(a * 0.1F)),
               5.0F
            );
            Fonts.sf_bold
               .draw(
                  shorten(rule.name, 17),
                  x + 5.0F,
                  y + 3.0F,
                  6.0F,
                  rule.enabled ? ColorUtil.getColor(240, 244, 252, aWhite) : ColorUtil.getColor(150, 158, 176, (int)(200.0F * a))
               );
            Fonts.sf_regular.draw(price(rule.maxPrice), x + 5.0F, y + 14.0F, 5.5F, ColorUtil.getColor(190, 198, 216, (int)(200.0F * a)));
         }
      }

      if (rules.isEmpty()) {
         Fonts.sf_regular
            .drawCentered(
               "Правил пока нет — возьмите предмет в руку и нажмите «Добавить»",
               this.listX() + 118.0F,
               this.listY + this.listH / 2.0F - 4.0F,
               6.5F,
               ColorUtil.getColor(190, 198, 216, (int)(200.0F * a))
            );
      }

      Render2D.endOverlay();
      Scissor.disable();
      if (this.maxScroll > 0.0F) {
         float barH = Math.max(24.0F, this.listH * (this.listH / (this.listH + this.maxScroll)));
         float barY = this.listY + (this.listH - barH) * (this.scroll / this.maxScroll);
         Render2D.beginOverlay();
         RenderUtil.Render2D.rect(this.listX() + 236.0F - 2.0F, barY, 1.5F, barH, ColorUtil.getColor(255, 255, 255, (int)(48.0F * a)), 0.75F);
         Render2D.endOverlay();
      }
   }

   private void drawPanel(float mx, float my, float a, int aWhite, int accent) {
      List<AutoBuyRule> rules = this.rules();
      AutoBuyRule rule = this.selected >= 0 && this.selected < rules.size() ? rules.get(this.selected) : null;
      float x = this.panelX;
      float y = this.panelY();
      Render2D.beginOverlay();
      RenderUtil.Render2D.rect(x, y, this.panelW, this.listH, ColorUtil.getColor(255, 255, 255, (int)(5.0F * a)), 6.0F);
      RenderUtil.Render2D.outline(x, y, this.panelW, this.listH, 0.75F, ThemeColor.getOutlineColor(a * 0.12F), 6.0F);
      if (rule == null) {
         Fonts.sf_regular
            .drawCentered(
               "Выберите правило слева", x + this.panelW / 2.0F, y + this.listH / 2.0F - 4.0F, 7.0F, ColorUtil.getColor(190, 198, 216, (int)(200.0F * a))
            );
         Render2D.endOverlay();
      } else {
         Fonts.sf_bold.draw(shorten(rule.name, 24), x + 8.0F, y + 7.0F, 7.5F, ColorUtil.getColor(240, 244, 252, aWhite));
         Fonts.sf_regular
            .draw(rule.fuzzy ? "Нечёткое совпадение" : "Точное совпадение", x + 8.0F, y + 18.0F, 6.0F, ColorUtil.getColor(180, 190, 210, (int)(200.0F * a)));
         this.label(x, y + 24.0F, "Цена за штуку", a);
         float vw = this.priceValueW();
         float stepY = this.priceY();
         this.button(x + 8.0F, stepY, 34.0F, 18.0F, "−10к", mx, my, a, aWhite, accent);
         this.button(x + 8.0F + 34.0F + 4.0F, stepY, 34.0F, 18.0F, "−1к", mx, my, a, aWhite, accent);
         RenderUtil.Render2D.rect(x + 8.0F + 76.0F, stepY, vw, 18.0F, ColorUtil.getColor(255, 255, 255, (int)(6.0F * a)), 5.0F);
         Fonts.sf_bold.drawCentered(price(rule.maxPrice), x + 8.0F + 76.0F + vw / 2.0F, stepY + 5.0F, 7.0F, ColorUtil.getColor(255, 255, 255, aWhite));
         this.button(x + 8.0F + 76.0F + vw + 4.0F, stepY, 34.0F, 18.0F, "+1к", mx, my, a, aWhite, accent);
         this.button(x + 8.0F + 114.0F + vw + 4.0F, stepY, 34.0F, 18.0F, "+10к", mx, my, a, aWhite, accent);
         this.label(x, this.quantityY() - 10.0F, "Минимальное количество", a);
         float qy = this.quantityY();
         this.button(x + 8.0F, qy, 34.0F, 18.0F, "−1", mx, my, a, aWhite, accent);
         RenderUtil.Render2D.rect(x + 8.0F + 34.0F + 4.0F, qy, vw, 18.0F, ColorUtil.getColor(255, 255, 255, (int)(6.0F * a)), 5.0F);
         Fonts.sf_bold
            .drawCentered(Integer.toString(rule.minQuantity), x + 8.0F + 34.0F + 4.0F + vw / 2.0F, qy + 5.0F, 7.0F, ColorUtil.getColor(255, 255, 255, aWhite));
         this.button(x + 8.0F + 76.0F + vw, qy, 34.0F, 18.0F, "+1", mx, my, a, aWhite, accent);
         this.toggle(x + 8.0F, this.fuzzyY(), vw, 18.0F, "Нечёткое совпадение", rule.fuzzy, mx, my, a, aWhite, accent);
         this.toggle(x + 8.0F, this.enabledY(), vw, 18.0F, "Правило включено", rule.enabled, mx, my, a, aWhite, accent);
         this.parserButton(x + 8.0F, this.parserY(), vw, 18.0F, rule, mx, my, a, aWhite, accent);
         this.button(x + 8.0F, this.deleteY(), vw, 18.0F, "Удалить правило", mx, my, a, aWhite, ColorUtil.getColor(255, 110, 120));
         Render2D.endOverlay();
      }
   }

   private void parserButton(float x, float y, float w, float h, AutoBuyRule rule, float mx, float my, float a, int aWhite, int accent) {
      int lowest = AutoParser.lowestPrice(rule);
      int seen = AutoParser.seenCount(rule);
      String text;
      if (lowest > 0) {
         text = "Цена парсера: " + price(lowest) + " · лотов " + seen;
      } else {
         text = "Парсер: " + (seen > 0 ? "лоты без цены" : "ничего не нашёл");
      }

      boolean hv = MathUtil.isHovered(mx, my, x, y, w, h);
      RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.getColor(255, 255, 255, (int)((6.0F + 10.0F * (hv ? 1.0F : 0.0F)) * a)), 5.0F);
      RenderUtil.Render2D.outline(
         x, y, w, h, 0.75F, lowest > 0 ? ColorUtil.replAlpha(accent, a * (0.3F + 0.3F * (hv ? 1.0F : 0.0F))) : ThemeColor.getOutlineColor(a * 0.12F), 5.0F
      );
      Fonts.sf_regular
         .draw(
            text,
            x + 8.0F,
            y + (h - Fonts.sf_regular.getHeight(6.0F)) / 2.0F,
            6.0F,
            lowest > 0 ? ColorUtil.getColor(230, 236, 248, aWhite) : ColorUtil.getColor(160, 168, 186, (int)(200.0F * a))
         );
   }

   private void drawFooter(float mx, float my, float a, int aWhite, int accent) {
      float x = this.winX + 10.0F;
      float y = this.winY + 340.0F - 24.0F;
      float h = 18.0F;
      AutoParser parser = AutoParser.get();
      Render2D.beginOverlay();
      RenderUtil.Render2D.rect(x, y, 600.0F, h, ColorUtil.getColor(255, 255, 255, (int)(5.0F * a)), 6.0F);
      this.button(x + 6.0F, y + 3.0F, 116.0F, h - 6.0F, "Добавить из руки", mx, my, a, aWhite, accent);
      this.button(x + 126.0F, y + 3.0F, 108.0F, h - 6.0F, "Пресеты", mx, my, a, aWhite, accent);
      this.button(x + 238.0F, y + 3.0F, 112.0F, h - 6.0F, "Сброс цен", mx, my, a, aWhite, accent);
      this.button(
         x + 354.0F,
         y + 3.0F,
         100.0F,
         h - 6.0F,
         parser != null && parser.isEnabled() ? "Парсер: вкл" : "Парсер: выкл",
         mx,
         my,
         a,
         aWhite,
         parser != null && parser.isEnabled() ? accent : ColorUtil.getColor(150, 158, 176)
      );
      Render2D.endOverlay();
   }

   private void label(float x, float y, String text, float a) {
      Fonts.sf_regular.draw(text, x + 8.0F, y, 6.0F, ColorUtil.getColor(170, 180, 200, (int)(200.0F * a)));
   }

   private void button(float x, float y, float w, float h, String text, float mx, float my, float a, int aWhite, int accent) {
      this.button(x, y, w, h, text, mx, my, a, aWhite, accent, accent);
   }

   private void button(float x, float y, float w, float h, String text, float mx, float my, float a, int aWhite, int accent, int tint) {
      boolean hv = MathUtil.isHovered(mx, my, x, y, w, h);
      RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.replAlpha(tint, (0.1F + 0.16F * (hv ? 1.0F : 0.0F)) * a), 5.0F);
      RenderUtil.Render2D.outline(x, y, w, h, 0.75F, hv ? ColorUtil.replAlpha(tint, 0.6F * a) : ColorUtil.replAlpha(tint, 0.25F * a), 5.0F);
      Fonts.sf_bold.drawCentered(text, x + w / 2.0F, y + (h - Fonts.sf_bold.getHeight(6.0F)) / 2.0F, 6.0F, ColorUtil.getColor(255, 255, 255, aWhite));
   }

   private void toggle(float x, float y, float w, float h, String text, boolean on, float mx, float my, float a, int aWhite, int accent) {
      boolean hv = MathUtil.isHovered(mx, my, x, y, w, h);
      RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.getColor(255, 255, 255, (int)((6.0F + 10.0F * (hv ? 1.0F : 0.0F)) * a)), 5.0F);
      RenderUtil.Render2D.outline(
         x, y, w, h, 0.75F, on ? ColorUtil.replAlpha(accent, a * (0.3F + 0.2F * (hv ? 1.0F : 0.0F))) : ThemeColor.getOutlineColor(a * 0.12F), 5.0F
      );
      Fonts.sf_regular.draw(text, x + 8.0F, y + (h - Fonts.sf_regular.getHeight(6.0F)) / 2.0F, 6.0F, ColorUtil.getColor(230, 236, 248, aWhite));
      RenderUtil.Render2D.rect(
         x + w - 24.0F, y + 5.0F, 19.0F, h - 10.0F, on ? ColorUtil.replAlpha(accent, 0.55F * a) : ColorUtil.getColor(255, 255, 255, (int)(22.0F * a)), 4.0F
      );
      RenderUtil.Render2D.rect(
         x + w - (on ? 6.0F : 22.0F),
         y + 6.0F,
         16.0F,
         h - 12.0F,
         on ? ColorUtil.getColor(255, 255, 255, (int)(225.0F * a)) : ColorUtil.getColor(160, 168, 186, (int)(200.0F * a)),
         3.0F
      );
   }

   private static String price(int value) {
      String digits = Integer.toString(Math.max(0, value));
      StringBuilder sb = new StringBuilder(digits.length() + digits.length() / 3);

      for (int i = 0; i < digits.length(); i++) {
         if (i > 0 && (digits.length() - i) % 3 == 0) {
            sb.append(' ');
         }

         sb.append(digits.charAt(i));
      }

      return sb.toString();
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.maxScroll > 0.0F) {
         this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget + (float)vertical * 28.0F));
      }

      return true;
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (this.closingNow) {
         return true;
      } else {
         float mx = (float)click.comp_4798() / this.scaleFix;
         float my = (float)click.comp_4799() / this.scaleFix;
         if (MathUtil.isHovered(mx, my, this.winX + 620.0F - 26.0F, this.winY + 5.0F, 20.0F, 18.0F)) {
            this.closingNow = true;
            return true;
         } else if (this.hovered >= 0) {
            this.selected = this.hovered;
            return true;
         } else if (!this.clickFooter(mx, my) && !this.clickPanel(mx, my)) {
            if (!MathUtil.isHovered(mx, my, this.winX, this.winY, 620.0F, 340.0F)) {
               this.closingNow = true;
            }

            return true;
         } else {
            return true;
         }
      }
   }

   private boolean clickFooter(float mx, float my) {
      float x = this.winX + 10.0F + 6.0F;
      float y = this.winY + 340.0F - 24.0F + 3.0F;
      float h = 12.0F;
      if (MathUtil.isHovered(mx, my, x, y, 116.0F, h)) {
         this.addFromHand();
         return true;
      } else if (MathUtil.isHovered(mx, my, x + 120.0F, y, 108.0F, h)) {
         this.installPresets();
         return true;
      } else if (MathUtil.isHovered(mx, my, x + 232.0F, y, 112.0F, h)) {
         this.resetParser();
         return true;
      } else if (MathUtil.isHovered(mx, my, x + 348.0F, y, 100.0F, h)) {
         this.toggleParser();
         return true;
      } else {
         return false;
      }
   }

   private void installPresets() {
      int added = AutoBuyPresets.install(AutoBuyRules.get());
      ChatUtils.addChatMessage(added > 0 ? "[AutoBuy] Добавлено правил из каталога: " + added : "[AutoBuy] Каталог уже установлен.");
   }

   private void resetParser() {
      AutoParser parser = AutoParser.get();
      if (parser != null) {
         parser.reset();
         ChatUtils.addChatMessage("[AutoParser] Накопленные цены сброшены.");
      }
   }

   private void toggleParser() {
      AutoParser parser = AutoParser.get();
      if (parser != null) {
         if (parser.isEnabled()) {
            parser.setEnabled(false);
            ChatUtils.addChatMessage("[AutoParser] Остановлен.");
         } else {
            parser.reset();
            parser.setEnabled(true);
            ChatUtils.addChatMessage("[AutoParser] Запущен.");
         }
      }
   }

   private boolean clickPanel(float mx, float my) {
      List<AutoBuyRule> rules = this.rules();
      AutoBuyRule rule = this.selected >= 0 && this.selected < rules.size() ? rules.get(this.selected) : null;
      if (rule == null) {
         return false;
      } else {
         float x = this.panelX + 8.0F;
         float vw = this.priceValueW();
         float stepY = this.priceY();
         if (MathUtil.isHovered(mx, my, x, stepY, 34.0F, 18.0F)) {
            this.stepPrice(rule, -10000);
            return true;
         } else if (MathUtil.isHovered(mx, my, x + 34.0F + 4.0F, stepY, 34.0F, 18.0F)) {
            this.stepPrice(rule, -1000);
            return true;
         } else if (MathUtil.isHovered(mx, my, x + 76.0F + vw + 4.0F, stepY, 34.0F, 18.0F)) {
            this.stepPrice(rule, 1000);
            return true;
         } else if (MathUtil.isHovered(mx, my, x + 114.0F + vw + 4.0F, stepY, 34.0F, 18.0F)) {
            this.stepPrice(rule, 10000);
            return true;
         } else {
            float qy = this.quantityY();
            if (MathUtil.isHovered(mx, my, x, qy, 34.0F, 18.0F)) {
               rule.minQuantity = Math.max(1, rule.minQuantity - 1);
               AutoBuyRules.get().save();
               return true;
            } else if (MathUtil.isHovered(mx, my, x + 76.0F + vw, qy, 34.0F, 18.0F)) {
               rule.minQuantity = Math.min(2304, rule.minQuantity + 1);
               AutoBuyRules.get().save();
               return true;
            } else if (MathUtil.isHovered(mx, my, x, this.fuzzyY(), vw, 18.0F)) {
               rule.fuzzy = !rule.fuzzy;
               AutoBuyRules.get().save();
               return true;
            } else if (MathUtil.isHovered(mx, my, x, this.enabledY(), vw, 18.0F)) {
               rule.enabled = !rule.enabled;
               AutoBuyRules.get().save();
               return true;
            } else if (MathUtil.isHovered(mx, my, x, this.parserY(), vw, 18.0F)) {
               int price = AutoParser.applyLowestTo(rule);
               if (price <= 0) {
                  ChatUtils.addChatMessage("[AutoParser] Для «" + rule.name + "» цена ещё не найдена.");
               }

               return true;
            } else if (MathUtil.isHovered(mx, my, x, this.deleteY(), vw, 18.0F)) {
               AutoBuyRules.get().remove(rule);
               this.selected = -1;
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private void stepPrice(AutoBuyRule rule, int delta) {
      long next = (long)rule.maxPrice + (long)delta;
      rule.maxPrice = (int)Math.max(1000L, Math.min(2000000000L, next));
      AutoBuyRules.get().save();
   }

   private void addFromHand() {
      if (mc.field_1724 != null) {
         class_1799 hand = mc.field_1724.method_6047();
         if (hand.method_7960()) {
            ChatUtils.addChatMessage("[AutoBuy] Возьмите предмет в руку.");
         } else {
            String name = hand.method_7964().getString();
            int price = AutoBuy.getPrice(hand);
            if (price <= 0) {
               price = 1000;
            }

            AutoBuyRule rule = AutoBuyRules.get().add(name, price, Math.max(1, hand.method_7947()), AutoBuyRule.itemIdOf(hand));
            this.selected = AutoBuyRules.get().all().indexOf(rule);
            this.scrollTarget = 0.0F;
            ChatUtils.addChatMessage("[AutoBuy] Правило добавлено: " + name);
         }
      }
   }

   public void method_25419() {
      this.closingNow = true;
   }
}
