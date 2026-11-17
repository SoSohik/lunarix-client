package ru.mentions.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.cosmetics.CosmeticEntry;
import ru.mentions.cosmetics.CosmeticType;
import ru.mentions.cosmetics.CosmeticsManager;
import ru.mentions.cosmetics.render.CosmeticsPreview;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class CosmeticsScreen extends class_437 implements IMinecraft {
   private static final float WIN_W = 640.0F;
   private static final float WIN_H = 360.0F;
   private static final float TITLE_H = 26.0F;
   private static final float PAD = 10.0F;
   private static final float SIDE_W = 92.0F;
   private static final float SIDE_H = 20.0F;
   private static final float GAP = 6.0F;
   private static final int COLS = 5;
   private static final float FOOTER_H = 24.0F;
   private static final float TARGET_SCALE = 2.0F;
   private final class_437 parent;
   private final float[] sideHover = new float[CosmeticType.values().length];
   private float[] cardHover = new float[0];
   private CosmeticType slot = CosmeticType.CAPE;
   private float open;
   private float closing;
   private boolean closingNow;
   private float scroll;
   private float scrollTarget;
   private float scaleFix = 1.0F;
   private float winX;
   private float winY;
   private float gridX;
   private float gridY;
   private float gridW;
   private float gridH;
   private float cardW;
   private float cardH;
   private float maxScroll;
   private int hovered = -1;
   private float spinYaw = 0.0F;
   private List<CosmeticEntry> view = new ArrayList<>();
   private CosmeticEntry footerEntry;
   private float footerBX;
   private float footerBY;
   private float footerBW;
   private float footerBH;
   private float renderDX;
   private float renderDY;
   private float renderScale = 1.0F;
   private boolean renderedOnce;

   public CosmeticsScreen(class_437 parent) {
      super(class_2561.method_43470("Cosmetics"));
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

   private void refreshView() {
      CosmeticsManager manager = CosmeticsManager.get();
      manager.load();
      this.view = manager.entries(this.slot);
      this.ensureCardHover(this.view.size());
      this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget));
      this.scroll = Math.max(0.0F, Math.min(this.maxScroll, this.scroll));
      if (this.footerEntry != null && !this.view.contains(this.footerEntry)) {
         this.footerEntry = null;
      }
   }

   private void ensureCardHover(int size) {
      if (this.cardHover.length < size) {
         float[] grown = new float[size];
         System.arraycopy(this.cardHover, 0, grown, 0, this.cardHover.length);
         this.cardHover = grown;
      }
   }

   private void layout() {
      float w = (float)mc.method_22683().method_4486() / this.scaleFix;
      float h = (float)mc.method_22683().method_4502() / this.scaleFix;
      this.winX = (w - 640.0F) / 2.0F;
      this.winY = (h - 360.0F) / 2.0F;
      this.gridX = this.winX + 10.0F + 92.0F + 10.0F;
      this.gridY = this.winY + 26.0F + 6.0F;
      this.gridW = this.winX + 640.0F - 10.0F - this.gridX;
      this.gridH = 360.0F - (this.gridY - this.winY) - 24.0F - 6.0F;
      this.cardW = (this.gridW - 24.0F) / 5.0F;
      this.cardH = this.cardW + 15.0F;
      int rows = (this.view.size() + 5 - 1) / 5;
      float content = (float)rows * (this.cardH + 6.0F) - (rows > 0 ? 6.0F : 0.0F);
      this.maxScroll = Math.max(0.0F, content - this.gridH);
      this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget));
      this.scroll = Math.max(0.0F, Math.min(this.maxScroll, this.scroll));
   }

   private float sideX() {
      return this.winX + 10.0F;
   }

   private float sideY() {
      return this.winY + 26.0F + 6.0F;
   }

   private float rowH() {
      return this.cardH + 6.0F;
   }

   private float cardX(int index) {
      return this.gridX + (float)(index % 5) * (this.cardW + 6.0F);
   }

   private float cardY(int index) {
      return this.gridY + (float)(index / 5) * (this.cardH + 6.0F) - this.scroll;
   }

   private int firstIndex() {
      return this.rowH() <= 0.0F ? 0 : Math.max(0, (int)Math.floor((double)(this.scroll / this.rowH())) * 5);
   }

   private int lastIndex() {
      if (this.rowH() <= 0.0F) {
         return this.view.size();
      } else {
         int visibleRows = (int)Math.ceil((double)(this.gridH / this.rowH())) + 2;
         return Math.min(this.view.size(), this.firstIndex() + 5 * visibleRows);
      }
   }

   private int cardAt(float mx, float my) {
      if (!MathUtil.isHovered(mx, my, this.gridX, this.gridY, this.gridW, this.gridH)) {
         return -1;
      } else {
         for (int i = this.firstIndex(); i < this.lastIndex(); i++) {
            float x = this.cardX(i);
            float y = this.cardY(i);
            if (!(y + this.cardH < this.gridY) && !(y > this.gridY + this.gridH) && MathUtil.isHovered(mx, my, x, y, this.cardW, this.cardH)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         CosmeticsManager manager = CosmeticsManager.get();
         this.refreshView();
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
            if (this.hovered >= 0 && this.hovered < this.view.size()) {
               this.footerEntry = this.view.get(this.hovered);
            }

            this.spinYaw = (this.spinYaw + delta * 1.5F) % 360.0F;
            int aWhite = (int)(255.0F * a);
            int accent = ColorUtil.getClientColor(1);
            float w = (float)mc.method_22683().method_4486() / this.scaleFix;
            float h = (float)mc.method_22683().method_4502() / this.scaleFix;
            float scale = 0.96F + 0.04F * a;
            float dx = this.winX + (640.0F - 640.0F * scale) / 2.0F;
            float dy = this.winY + (360.0F - 360.0F * scale) / 2.0F;
            float dw = 640.0F * scale;
            float dh = 360.0F * scale;
            this.renderDX = dx;
            this.renderDY = dy;
            this.renderScale = scale;
            this.renderedOnce = true;
            Render2D.beginOverlay();

            try {
               RenderUtil.Render2D.rect(0.0F, 0.0F, w, h, ColorUtil.getColor(0, 0, 0, (int)(120.0F * a)));
               RenderUtil.Blur.blur(dx, dy, dw, dh, a, 10.0F, ColorUtil.replAlpha(ColorUtil.background(), 0.8F * a));
               RenderUtil.Render2D.rect(dx, dy, dw, dh, ColorUtil.getColor(255, 255, 255, (int)(7.0F * a)), 10.0F);
               RenderUtil.Render2D.outline(dx, dy, dw, dh, 0.75F, ThemeColor.getOutlineColor(a * 0.18F), 10.0F);
               RenderUtil.Render2D.rect(dx, dy, dw, 26.0F, ColorUtil.getColor(255, 255, 255, (int)(6.0F * a)), 10.0F);
               RenderUtil.Render2D.rect(dx + 10.0F, dy + 26.0F - 1.0F, dw - 20.0F, 0.5F, ThemeColor.getOutlineColor(a * 0.22F), 0.25F);
               Fonts.sf_bold.draw("КОСМЕТИКА", dx + 10.0F + 2.0F, dy + 8.0F, 9.0F, ColorUtil.getColor(255, 255, 255, aWhite));
               Fonts.sf_regular
                  .draw(manager.entries().size() + " предметов", dx + dw - 78.0F, dy + 9.0F, 7.0F, ColorUtil.getColor(205, 213, 230, (int)(215.0F * a)));
               this.drawClose(mx, my, dx, dy, a, aWhite);
               this.drawSlots(mx, my, a, aWhite, accent, manager);
               this.drawGrid(mx, my, a, aWhite, accent, manager);
               this.drawFooter(mx, my, a, aWhite, accent, manager);
            } finally {
               Render2D.endOverlay();
            }
         }
      }
   }

   private void drawClose(float mx, float my, float dx, float dy, float a, int aWhite) {
      float x = dx + 640.0F - 24.0F;
      float y = dy + 6.0F;
      boolean hover = MathUtil.isHovered(mx, my, x - 2.0F, y - 1.0F, 20.0F, 18.0F);
      RenderUtil.Render2D.rect(
         x, y, 16.0F, 16.0F, hover ? ColorUtil.getColor(255, 95, 95, (int)(150.0F * a)) : ColorUtil.getColor(255, 255, 255, (int)(10.0F * a)), 5.0F
      );
      RenderUtil.Render2D.outline(x, y, 16.0F, 16.0F, 0.75F, ThemeColor.getOutlineColor(a * (hover ? 0.45F : 0.18F)), 5.0F);

      for (int k = 0; k < 7; k++) {
         RenderUtil.Render2D.rect(x + 4.5F + (float)k, y + 4.5F + (float)k, 1.0F, 1.0F, ColorUtil.getColor(235, 240, 250, aWhite), 0.0F);
         RenderUtil.Render2D.rect(x + 10.5F - (float)k, y + 4.5F + (float)k, 1.0F, 1.0F, ColorUtil.getColor(235, 240, 250, aWhite), 0.0F);
      }
   }

   private void drawSlots(float mx, float my, float a, int aWhite, int accent, CosmeticsManager manager) {
      CosmeticType[] types = CosmeticType.values();

      for (int i = 0; i < types.length; i++) {
         CosmeticType type = types[i];
         float x = this.sideX();
         float y = this.sideY() + (float)i * 24.0F;
         boolean hover = MathUtil.isHovered(mx, my, x, y, 92.0F, 20.0F);
         this.sideHover[i] = approach(this.sideHover[i], hover ? 1.0F : 0.0F, 0.22F);
         boolean active = type == this.slot;
         RenderUtil.Render2D.rect(
            x,
            y,
            92.0F,
            20.0F,
            active ? ColorUtil.replAlpha(accent, 0.32F * a) : ColorUtil.getColor(255, 255, 255, (int)((8.0F + 14.0F * this.sideHover[i]) * a)),
            5.0F
         );
         if (active) {
            RenderUtil.Render2D.rect(x + 2.0F, y + 3.0F, 2.0F, 14.0F, ColorUtil.replAlpha(accent, 0.85F * a), 1.0F);
         }

         Fonts.sf_regular.draw(type.title(), x + 10.0F, y + 6.5F, 7.0F, ColorUtil.getColor(232, 238, 250, aWhite));
         if (manager.worn(type) != null) {
            RenderUtil.Render2D.rect(x + 92.0F - 12.0F, y + 10.0F - 2.0F, 4.0F, 4.0F, ColorUtil.replAlpha(accent, 0.9F * a), 2.0F);
         }
      }

      float yx = this.sideY() + (float)types.length * 24.0F + 4.0F;
      boolean any = false;

      for (CosmeticType typex : types) {
         if (manager.worn(typex) != null) {
            any = true;
            break;
         }
      }

      boolean hoverx = any && MathUtil.isHovered(mx, my, this.sideX(), yx, 92.0F, 20.0F);
      RenderUtil.Render2D.rect(
         this.sideX(), yx, 92.0F, 20.0F, hoverx ? ColorUtil.getColor(255, 95, 95, (int)(110.0F * a)) : ColorUtil.getColor(255, 255, 255, (int)(8.0F * a)), 5.0F
      );
      RenderUtil.Render2D.outline(this.sideX(), yx, 92.0F, 20.0F, 0.75F, ThemeColor.getOutlineColor(a * (hoverx ? 0.4F : 0.12F)), 5.0F);
      Fonts.sf_regular
         .drawCentered(
            "Снять всё",
            this.sideX() + 46.0F,
            yx + 6.5F,
            7.0F,
            ColorUtil.getColor(any ? 255 : 190, any ? 235 : 200, any ? 240 : 215, (int)((any ? 255.0F : 110.0F) * a))
         );
   }

   private void drawGrid(float mx, float my, float a, int aWhite, int accent, CosmeticsManager manager) {
      List<CosmeticEntry> list = this.view;
      Scissor.enable(this.gridX, this.gridY, this.gridW, this.gridH, 2.0F);

      try {
         this.drawCards(a, aWhite, accent, manager, list);
      } finally {
         Scissor.disable();
      }

      if (list.isEmpty()) {
         Fonts.sf_regular
            .drawCentered(
               "В этом слоте пусто",
               this.gridX + this.gridW / 2.0F,
               this.gridY + this.gridH / 2.0F - 4.0F,
               7.0F,
               ColorUtil.getColor(190, 198, 216, (int)(200.0F * a))
            );
      } else if (this.maxScroll > 0.0F) {
         float barH = Math.max(24.0F, this.gridH * (this.gridH / (this.gridH + this.maxScroll)));
         float barY = this.gridY + (this.gridH - barH) * (this.scroll / this.maxScroll);
         RenderUtil.Render2D.rect(this.gridX + this.gridW - 2.0F, barY, 1.5F, barH, ColorUtil.getColor(255, 255, 255, (int)(48.0F * a)), 0.75F);
      }
   }

   private void drawCards(float a, int aWhite, int accent, CosmeticsManager manager, List<CosmeticEntry> list) {
      int first = this.firstIndex();
      int last = this.lastIndex();

      for (int i = first; i < last; i++) {
         CosmeticEntry entry = list.get(i);
         float x = this.cardX(i);
         float y = this.cardY(i);
         this.cardHover[i] = approach(this.cardHover[i], this.hovered == i ? 1.0F : 0.0F, 0.24F);
         float hv = this.cardHover[i];
         boolean worn = manager.isWorn(entry);
         RenderUtil.Render2D.rect(x, y, this.cardW, this.cardH, ColorUtil.getColor(255, 255, 255, (int)((6.0F + 12.0F * hv) * a)), 6.0F);
         if (worn) {
            RenderUtil.Render2D.rect(x, y, this.cardW, this.cardH, ColorUtil.replAlpha(accent, 0.16F * a), 6.0F);
         }

         RenderUtil.Render2D.outline(
            x,
            y,
            this.cardW,
            this.cardH,
            0.75F,
            worn
               ? ColorUtil.replAlpha(accent, a * (0.45F + 0.25F * hv))
               : (hv > 0.03F ? ColorUtil.replAlpha(accent, a * 0.3F * hv) : ThemeColor.getOutlineColor(a * 0.1F)),
            6.0F
         );
         float box = this.cardW - 16.0F;
         RenderUtil.Render2D.rect(x + 5.0F, y + 7.0F, box, box, ColorUtil.getColor(255, 255, 255, (int)((5.0F + 9.0F * hv) * a)), 5.0F);
      }

      for (int i = first; i < last; i++) {
         CosmeticEntry entry = list.get(i);
         float x = this.cardX(i);
         float y = this.cardY(i);
         if (!(y + this.cardH < this.gridY - 2.0F) && !(y > this.gridY + this.gridH + 2.0F)) {
            float box = this.cardW - 16.0F;
            CosmeticsPreview.thumbnail(entry, x + 5.0F + box * 0.5F, y + 7.0F + box * 0.5F, box * 0.5F, this.spinYaw, a);
         }
      }

      CosmeticsPreview.flush(this.gridX, this.gridY, this.gridW, this.gridH, 2.0F);
      Scissor.refresh();

      for (int ix = first; ix < last; ix++) {
         CosmeticEntry entry = list.get(ix);
         float x = this.cardX(ix);
         float y = this.cardY(ix);
         boolean worn = manager.isWorn(entry);
         Fonts.sf_regular.drawCentered(shorten(entry.name, 12), x + this.cardW / 2.0F, y + this.cardH - 11.0F, 6.0F, ColorUtil.getColor(238, 242, 252, aWhite));
         if (worn) {
            RenderUtil.Render2D.rect(x + this.cardW - 14.0F, y + 4.0F, 10.0F, 10.0F, ColorUtil.getColor(120, 235, 160, (int)(220.0F * a)), 3.0F);
         }
      }
   }

   private void drawFooter(float mx, float my, float a, int aWhite, int accent, CosmeticsManager manager) {
      CosmeticEntry entry = this.footerEntry;
      float x = this.winX + 10.0F;
      float y = this.winY + 360.0F - 24.0F;
      float w = 620.0F;
      float h = 18.0F;
      RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.getColor(255, 255, 255, (int)(5.0F * a)), 6.0F);
      if (entry == null) {
         this.footerBW = 0.0F;
         Fonts.sf_regular.drawCentered("ЛКМ — надеть или снять", x + w / 2.0F, y + 5.0F, 6.5F, ColorUtil.getColor(170, 180, 200, (int)(180.0F * a)));
      } else {
         String author = entry.author != null && !entry.author.isEmpty() ? "автор " + entry.author : "автор неизвестен";
         Fonts.sf_bold.draw(shorten(entry.name, 26), x + 8.0F, y + 2.0F, 7.0F, ColorUtil.getColor(240, 244, 252, aWhite));
         Fonts.sf_regular.draw(shorten(author, 34), x + 8.0F, y + 11.0F, 6.0F, ColorUtil.getColor(180, 190, 210, (int)(200.0F * a)));
         boolean worn = manager.isWorn(entry);
         float bw = 78.0F;
         float bx = x + w - bw - 2.0F;
         this.footerBX = bx;
         this.footerBY = y + 1.0F;
         this.footerBW = bw;
         this.footerBH = h - 2.0F;
         boolean hover = MathUtil.isHovered(mx, my, bx, y + 1.0F, bw, h - 2.0F);
         RenderUtil.Render2D.rect(bx, y + 1.0F, bw, h - 2.0F, ColorUtil.replAlpha(accent, ((worn ? 0.22F : 0.5F) + (hover ? 0.15F : 0.0F)) * a), 5.0F);
         RenderUtil.Render2D.outline(bx, y + 1.0F, bw, h - 2.0F, 0.75F, ColorUtil.replAlpha(accent, a * (worn ? 0.35F : 0.6F)), 5.0F);
         Fonts.sf_bold.drawCentered(worn ? "Снять" : "Надеть", bx + bw / 2.0F, y + 5.0F, 6.5F, ColorUtil.getColor(255, 255, 255, aWhite));
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.maxScroll > 0.0F) {
         this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget - (float)vertical * 28.0F));
      }

      return true;
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (this.closingNow) {
         return true;
      } else {
         float mx = (float)click.comp_4798() / this.scaleFix;
         float my = (float)click.comp_4799() / this.scaleFix;
         float closeX = this.renderedOnce ? this.renderDX + 640.0F - 26.0F : this.winX + 640.0F - 26.0F;
         float closeY = this.renderedOnce ? this.renderDY + 5.0F : this.winY + 5.0F;
         if (MathUtil.isHovered(mx, my, closeX, closeY, 20.0F, 18.0F)) {
            this.closingNow = true;
            return true;
         } else {
            CosmeticType[] types = CosmeticType.values();

            for (int i = 0; i < types.length; i++) {
               float x = this.sideX();
               float y = this.sideY() + (float)i * 24.0F;
               if (MathUtil.isHovered(mx, my, x, y, 92.0F, 20.0F)) {
                  this.slot = types[i];
                  this.scroll = 0.0F;
                  this.scrollTarget = 0.0F;
                  this.hovered = -1;
                  this.footerEntry = null;
                  Arrays.fill(this.cardHover, 0.0F);
                  this.refreshView();
                  this.layout();
                  return true;
               }
            }

            float clearY = this.sideY() + (float)types.length * 24.0F + 4.0F;
            if (MathUtil.isHovered(mx, my, this.sideX(), clearY, 92.0F, 20.0F)) {
               CosmeticsManager.get().clear();
               return true;
            } else if (this.footerEntry != null
               && this.footerBW > 0.0F
               && MathUtil.isHovered(mx, my, this.footerBX, this.footerBY, this.footerBW, this.footerBH)) {
               CosmeticsManager.get().toggle(this.footerEntry);
               return true;
            } else {
               int nowHovered = this.cardAt(mx, my);
               if (nowHovered >= 0 && nowHovered < this.view.size()) {
                  CosmeticsManager.get().toggle(this.view.get(nowHovered));
                  return true;
               } else {
                  float bgX = this.renderedOnce ? this.renderDX : this.winX;
                  float bgY = this.renderedOnce ? this.renderDY : this.winY;
                  float bgW = this.renderedOnce ? 640.0F * this.renderScale : 640.0F;
                  float bgH = this.renderedOnce ? 360.0F * this.renderScale : 360.0F;
                  if (!MathUtil.isHovered(mx, my, bgX, bgY, bgW, bgH)) {
                     this.closingNow = true;
                  }

                  return true;
               }
            }
         }
      }
   }

   public void method_25419() {
      this.closingNow = true;
   }
}
