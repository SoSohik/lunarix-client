package ru.mentions.screen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import ru.mentions.alt.Account;
import ru.mentions.alt.AltManager;
import ru.mentions.lang.Lang;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.other.GuiSounds;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class AltManagerScreen extends class_437 implements IMinecraft {
   private final class_437 parent;
   private final Animation alpha = new Animation();
   private float scaleFix = 1.0F;
   private float mouseX;
   private float mouseY;
   private String inputText = "";
   private boolean inputFocused = false;
   private float scroll = 0.0F;
   private float scrollTarget = 0.0F;
   private boolean settingsOpen = false;
   private String randomBase = "";
   private boolean baseFocused = false;
   private final Animation addAnim = new Animation();
   private final Animation pasteAnim = new Animation();
   private final Animation genAnim = new Animation();
   private final Animation inputAnim = new Animation();
   private final Animation gearAnim = new Animation();
   private final Animation settingsAnim = new Animation();
   private final Animation baseAnim = new Animation();
   private float gearX;
   private float gearY;
   private float gearW;
   private float gearH;
   private float sideX;
   private float sideY;
   private float sideW;
   private float sideH;
   private float baseX;
   private float baseY;
   private float baseW;
   private float baseH;
   private final Map<Account, AltManagerScreen.RowAnims> rowAnims = new IdentityHashMap<>();
   private final Set<Account> removing = Collections.newSetFromMap(new IdentityHashMap<>());
   private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
   private float panelX;
   private float panelY;
   private float panelW;
   private float panelH;
   private float inputX;
   private float inputY;
   private float inputW;
   private float inputH;
   private float addX;
   private float addY;
   private float addW;
   private float addH;
   private float pasteX;
   private float pasteY;
   private float pasteW;
   private float pasteH;
   private float genX;
   private float genY;
   private float genW;
   private float genH;
   private float listX;
   private float listY;
   private float listW;
   private float listH;
   private final List<float[]> rowRects = new ArrayList<>();
   private final List<float[]> delRects = new ArrayList<>();
   private final List<float[]> favRects = new ArrayList<>();
   private final List<Account> rowRefs = new ArrayList<>();
   private static final float ROW_H = 20.0F;
   private static final float ROW_GAP = 4.0F;

   public AltManagerScreen(class_437 parent) {
      super(class_2561.method_43470("AltManager"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.alpha.set(0.0);
      this.alpha.run(1.0, 0.5, Easings.BACK_OUT);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         this.alpha.update();
         this.settingsAnim.update();
         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         this.scaleFix = currentScale > 0.0F ? targetScale / currentScale : 1.0F;
         this.mouseX = (float)mouseX / this.scaleFix;
         this.mouseY = (float)mouseY / this.scaleFix;
         int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
         int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
         if (context != null) {
            context.method_51448().pushMatrix();
         }

         Render2D.beginOverlay();
         float a = this.alpha.get();
         MainMenuScreen.renderMenuBg(screenWidth, screenHeight, a, this.mouseX, this.mouseY);
         ScreenBlur.capture(2);
         RenderUtil.Blur.blur(0.0F, 0.0F, (float)screenWidth, (float)screenHeight, a, ColorUtil.getColor(0, a * 0.2F));
         ScreenBlur.capture(4);
         Font font = Fonts.sf_regular;
         this.panelW = 280.0F;
         this.panelH = 274.0F;
         this.panelX = (float)screenWidth / 2.0F - this.panelW / 2.0F;
         this.panelY = (float)screenHeight / 2.0F - this.panelH / 2.0F + 16.0F - 16.0F * a;
         float pad = 8.0F;
         float btnH = 20.0F;
         float gap = 5.0F;
         float titleH = 14.0F;
         float btnW = (this.panelW - pad * 2.0F - gap * 2.0F) / 3.0F;
         this.addY = this.pasteY = this.genY = this.panelY + this.panelH - pad - btnH;
         this.addH = this.pasteH = this.genH = btnH;
         this.addW = this.pasteW = this.genW = btnW;
         this.addX = this.panelX + pad;
         this.pasteX = this.addX + btnW + gap;
         this.genX = this.pasteX + btnW + gap;
         this.gearW = this.gearH = this.inputH = 20.0F;
         this.inputX = this.panelX + pad;
         this.inputW = this.panelW - pad * 2.0F - this.gearW - 6.0F;
         this.inputY = this.addY - gap - this.inputH;
         this.gearX = this.inputX + this.inputW + 6.0F;
         this.gearY = this.inputY;
         this.listX = this.panelX + pad;
         this.listY = this.panelY + pad + titleH;
         this.listW = this.panelW - pad * 2.0F;
         this.listH = this.inputY - gap - this.listY;
         this.renderSidePanel(a, font);
         RenderUtil.Blur.blur(this.panelX, this.panelY, this.panelW, this.panelH, a, 8.0F, ColorUtil.getColor(20, a * 0.4F));
         RenderUtil.Render2D.outline(this.panelX, this.panelY, this.panelW, this.panelH, 0.75F, ThemeColor.getOutlineColor(a * 0.1F), 8.0F);
         Fonts.sf_regular
            .drawCentered(
               "Чтобы выйти, нажмите ESC или кликните по пустому месту.",
               (float)screenWidth / 2.0F,
               this.panelY + this.panelH + 5.0F,
               9.0F,
               ColorUtil.getColor(255, a * 0.06F)
            );
         this.rowRects.clear();
         this.delRects.clear();
         this.favRects.clear();
         this.rowRefs.clear();
         List<Account> all = AltManager.get().getAccounts();
         List<Account> accs = AltManager.get().getSorted();
         font.draw(
            Lang.pick("Аккаунты", "Accounts") + " · " + accs.size(), this.panelX + pad, this.panelY + pad + 2.0F, 8.0F, ColorUtil.getColor(255, a * 0.35F)
         );
         float contentH = (float)accs.size() * 24.0F;
         float maxScroll = Math.max(0.0F, contentH - this.listH);
         this.scrollTarget = MathUtil.clamp(this.scrollTarget, 0.0F, maxScroll);
         this.scroll = this.scroll + (this.scrollTarget - this.scroll) * 0.4F;
         this.rowAnims.keySet().retainAll(all);
         Scissor.enable(this.listX, this.listY, this.listW, this.listH, 2.0F);
         float off = 0.0F;
         String current = AltManager.get().currentName();
         ArrayList<Account> finished = null;

         for (Account acc : accs) {
            AltManagerScreen.RowAnims an = this.rowAnims.computeIfAbsent(acc, k -> new AltManagerScreen.RowAnims());
            boolean leaving = this.removing.contains(acc);
            an.appear.run(leaving ? 0.0 : 1.0, leaving ? 0.22 : 0.35, Easings.QUAD_OUT, true);
            float ap = MathUtil.clamp(an.appear.get(), 0.0F, 1.0F);
            float slotH = 24.0F * ap;
            if (!an.hasOff) {
               an.curOff = off;
               an.hasOff = true;
            } else {
               an.curOff = an.curOff + (off - an.curOff) * 0.35F;
            }

            float ry = this.listY - this.scroll + an.curOff;
            if (RenderUtil.Images.isUrlLoaded(acc.headUrl())) {
               an.headFade.run(1.0, 0.3, Easings.QUAD_OUT, true);
            }

            boolean visible = ry + slotH >= this.listY && ry <= this.listY + this.listH;
            boolean inside = this.mouseY() >= this.listY && this.mouseY() <= this.listY + this.listH;
            float dw = 16.0F;
            float dh = 16.0F;
            float dx = this.listX + this.listW - dw - 4.0F;
            float dy = ry + (20.0F - dh) / 2.0F;
            float fx = dx - dw - 4.0F;
            boolean hovered = !leaving && visible && inside && MathUtil.isHovered(this.mouseX(), this.mouseY(), this.listX, ry, this.listW, 20.0F);
            boolean delHover = !leaving && visible && inside && MathUtil.isHovered(this.mouseX(), this.mouseY(), dx, dy, dw, dh);
            boolean favHover = !leaving && visible && inside && MathUtil.isHovered(this.mouseX(), this.mouseY(), fx, dy, dw, dh);
            boolean active = acc.name.equalsIgnoreCase(current);
            an.hover.run(hovered && !delHover && !favHover ? 1.0 : 0.0, 0.18, Easings.QUAD_OUT, true);
            an.del.run(delHover ? 1.0 : 0.0, 0.18, Easings.QUAD_OUT, true);
            an.favHover.run(favHover ? 1.0 : 0.0, 0.18, Easings.QUAD_OUT, true);
            an.fav.run(acc.favorite ? 1.0 : 0.0, 0.25, Easings.QUAD_OUT, true);
            an.active.run(active ? 1.0 : 0.0, 0.25, Easings.QUAD_OUT, true);
            an.hover.update();
            an.del.update();
            an.favHover.update();
            an.fav.update();
            an.active.update();
            an.appear.update();
            an.headFade.update();
            float hp = MathUtil.clamp(an.hover.get(), 0.0F, 1.0F);
            float dp = MathUtil.clamp(an.del.get(), 0.0F, 1.0F);
            float fhp = MathUtil.clamp(an.favHover.get(), 0.0F, 1.0F);
            float fv = MathUtil.clamp(an.fav.get(), 0.0F, 1.0F);
            float acp = MathUtil.clamp(an.active.get(), 0.0F, 1.0F);
            float hf = MathUtil.clamp(an.headFade.get(), 0.0F, 1.0F);
            if (visible) {
               float ra = a * ap;
               float slide = (1.0F - ap) * 10.0F;
               float rx = this.listX + slide;
               float rw = this.listW;
               dx = rx + rw - dw - 3.0F;
               int rowBg = ColorUtil.getColor(255, ra * (0.015F + 0.05F * hp + 0.03F * acp));
               RenderUtil.Render2D.rect(rx, ry, rw, 20.0F, rowBg, 6.0F);
               if (acp > 0.001F) {
                  RenderUtil.Render2D.rect(rx + 2.0F, ry + 3.0F, 1.5F, 14.0F, ColorUtil.replAlpha(ColorUtil.getClientColor(1), ra * 0.9F * acp), 0.75F);
               }

               if (acp > 0.5F) {
                  RenderUtil.Render2D.outline(rx, ry, rw, 20.0F, 0.75F, ColorUtil.replAlpha(ColorUtil.getClientColor(1), ra * 0.5F * acp), 6.0F);
               }

               float asz = 12.0F;
               float axx = rx + 5.0F;
               float ay = ry + (20.0F - asz) / 2.0F;
               RenderUtil.Render2D.rect(axx, ay, asz, asz, ColorUtil.getColor(255, ra * 0.06F), 5.0F);
               RenderUtil.Images.url(acc.headUrl(), axx, ay, asz, asz, 1.0F, 5.0F, ColorUtil.getColor(255, ra * hf));
               if (acp > 0.5F) {
                  RenderUtil.Render2D.outline(axx, ay, asz, asz, 0.75F, ColorUtil.replAlpha(ColorUtil.getClientColor(1), ra * 0.5F * acp), 5.0F);
               }

               float tx = axx + asz + 4.0F;
               font.draw(acc.name, tx, ry + 5.0F, 8.0F, ColorUtil.getColor(255, ra * (0.7F + 0.3F * acp)));
               Fonts.sf_regular
                  .draw(
                     this.dateFormat.format(new Date(acc.created)),
                     tx + 1.0F + font.getWidth(acc.name, 8.0F),
                     ry + 5.0F,
                     5.0F,
                     ColorUtil.getColor(255, ra * (0.15F + 0.15F * acp))
                  );
               dy = ry + (20.0F - dh) / 2.0F;
               fx = dx - dw - 2.0F;
               RenderUtil.Render2D.rect(fx, dy, dw, dh, ColorUtil.getColor(255, ra * (0.015F + 0.025F * fhp)), 5.0F);
               int starCol = ColorUtil.overCol(
                  ColorUtil.getColor(255, 255, 255, ra * (0.45F + 0.25F * fhp)), ColorUtil.replAlpha(ColorUtil.getClientColor(1), ra), fv
               );
               Fonts.sf_bold.drawCentered(fv > 0.5F ? "★" : "☆", fx + dw / 2.0F, dy + dh / 2.0F - 3.5F, 7.5F, starCol);
               RenderUtil.Render2D.rect(dx, dy, dw, dh, ColorUtil.getColor(255, ra * (0.015F + 0.025F * dp)), 5.0F);
               Fonts.sf_bold.drawCentered("✕", dx + dw / 2.0F, dy + dh / 2.0F - 3.5F, 7.0F, ColorUtil.getColor(255, 110, 110, ra * (0.55F + 0.35F * dp)));
               if (!leaving) {
                  this.rowRects.add(new float[]{rx, ry, rw - dw * 2.0F - 16.0F, 20.0F});
                  this.delRects.add(new float[]{dx, dy, dw, dh});
                  this.favRects.add(new float[]{fx, dy, dw, dh});
                  this.rowRefs.add(acc);
               }
            }

            if (leaving && ap <= 0.01F) {
               if (finished == null) {
                  finished = new ArrayList<>();
               }

               finished.add(acc);
            }

            off += slotH;
         }

         if (finished != null) {
            for (Account acc : finished) {
               AltManager.get().remove(acc);
               this.rowAnims.remove(acc);
               this.removing.remove(acc);
            }
         }

         if (accs.isEmpty()) {
            font.drawCentered(
               Lang.pick("Пока нет аккаунтов", "No accounts yet"),
               this.listX + this.listW / 2.0F,
               this.listY + this.listH / 2.0F - 4.0F,
               12.0F,
               ColorUtil.getColor(255, a * 0.15F)
            );
         }

         Scissor.disable();
         this.inputAnim.run(this.inputFocused ? 1.0 : 0.0, 0.2, Easings.QUAD_OUT, true);
         this.inputAnim.update();
         float fp = MathUtil.clamp(this.inputAnim.get(), 0.0F, 1.0F);
         RenderUtil.Render2D.rect(this.inputX, this.inputY, this.inputW, this.inputH, ColorUtil.getColor(255, a * (0.015F + 0.03F * fp)), 6.0F);
         RenderUtil.Render2D.outline(this.inputX, this.inputY, this.inputW, this.inputH, 0.75F, ThemeColor.getOutlineColor(a * (0.05F + 0.12F * fp)), 6.0F);
         boolean blink = this.inputFocused && System.currentTimeMillis() / 450L % 2L == 0L;
         String shown = this.inputText.isEmpty() && !this.inputFocused ? "Введите ник" : this.inputText + (blink ? "_" : "");
         font.draw(
            shown,
            this.inputX + 10.0F,
            this.inputY + 5.4F,
            7.0F,
            this.inputText.isEmpty() && !this.inputFocused ? ColorUtil.getColor(140, a) : ColorUtil.getColor(185, a)
         );
         this.gearAnim
            .run(
               !MathUtil.isHovered(this.mouseX(), this.mouseY(), this.gearX, this.gearY, this.gearW, this.gearH) && !this.settingsOpen ? 0.0 : 1.0,
               0.18,
               Easings.QUAD_OUT,
               true
            );
         this.gearAnim.update();
         float gp = MathUtil.clamp(this.gearAnim.get(), 0.0F, 1.0F);
         RenderUtil.Render2D.rect(this.gearX, this.gearY, this.gearW, this.gearH, ColorUtil.getColor(255, a * (0.015F + 0.04F * gp)), 6.0F);
         RenderUtil.Render2D.outline(this.gearX, this.gearY, this.gearW, this.gearH, 0.75F, ThemeColor.getOutlineColor(a * (0.05F + 0.12F * gp)), 6.0F);
         Fonts.icon.draw("l", this.gearX + 6.25F, this.gearY + 6.3F, 8.0F, ColorUtil.getColor(255, a * (0.35F + 0.3F * gp)));
         this.addAnim
            .run(MathUtil.isHovered(this.mouseX(), this.mouseY(), this.addX, this.addY, this.addW, this.addH) ? 1.0 : 0.0, 0.18, Easings.QUAD_OUT, true);
         this.pasteAnim
            .run(MathUtil.isHovered(this.mouseX(), this.mouseY(), this.pasteX, this.pasteY, this.pasteW, this.pasteH) ? 1.0 : 0.0, 0.18, Easings.QUAD_OUT, true);
         this.genAnim
            .run(MathUtil.isHovered(this.mouseX(), this.mouseY(), this.genX, this.genY, this.genW, this.genH) ? 1.0 : 0.0, 0.18, Easings.QUAD_OUT, true);
         this.addAnim.update();
         this.pasteAnim.update();
         this.genAnim.update();
         this.drawButton(this.addX, this.addY, this.addW, this.addH, "Добавить", a, MathUtil.clamp(this.addAnim.get(), 0.0F, 1.0F), true);
         this.drawButton(this.pasteX, this.pasteY, this.pasteW, this.pasteH, "Вставить", a, MathUtil.clamp(this.pasteAnim.get(), 0.0F, 1.0F), false);
         this.drawButton(this.genX, this.genY, this.genW, this.genH, "Рандом", a, MathUtil.clamp(this.genAnim.get(), 0.0F, 1.0F), false);
         Render2D.endOverlay();
         if (context != null) {
            context.method_51448().popMatrix();
         }
      }
   }

   private void drawButton(float x, float y, float w, float h, String label, float a, float hp, boolean accent) {
      int bg = accent ? ColorUtil.replAlpha(ColorUtil.getClientColor(1), a * (0.18F + 0.12F * hp)) : ColorUtil.getColor(255, a * (0.015F + 0.03F * hp));
      RenderUtil.Render2D.rect(x, y, w, h, bg, 6.0F);
      Fonts.sf_regular.drawCentered(label, x + w / 2.0F, y + h / 2.0F - 4.75F, 7.0F, ColorUtil.getColor(175, a));
   }

   private void renderSidePanel(float a, Font font) {
      this.settingsAnim.run(this.settingsOpen ? 1.0 : 0.0, 0.25, Easings.SINE_OUT);
      float sp = this.settingsAnim.get();
      float sa = a * sp;
      this.sideW = 168.0F;
      this.sideH = 96.0F;
      this.sideY = this.inputY + this.inputH - this.sideH;
      float hiddenX = this.panelX + this.panelW - this.sideW + 24.0F;
      float shownX = this.panelX + this.panelW + 8.0F;
      this.sideX = hiddenX + (shownX - hiddenX) - 8.0F + 8.0F * sa;
      RenderUtil.Blur.blur(this.sideX, this.sideY, this.sideW, this.sideH, sa, 8.0F, ColorUtil.getColor(20, sa * 0.3F));
      RenderUtil.Render2D.outline(this.sideX, this.sideY, this.sideW, this.sideH, 0.75F, ThemeColor.getOutlineColor(sa * 0.1F), 8.0F);
      float pad = 12.0F;
      font.draw(Lang.pick("Настройки рандома", "Random settings"), this.sideX + pad, this.sideY + 9.0F, 8.0F, ColorUtil.getColor(185, sa));
      font.draw(Lang.pick("Начальный ник", "Initial nickname"), this.sideX + pad, this.sideY + 26.0F, 6.5F, ColorUtil.getColor(185, sa));
      this.baseX = this.sideX + pad;
      this.baseY = this.sideY + 37.0F;
      this.baseW = this.sideW - pad * 2.0F;
      this.baseH = 20.0F;
      this.baseAnim.run(this.baseFocused ? 1.0 : 0.0, 0.2, Easings.QUAD_OUT, true);
      this.baseAnim.update();
      float bf = MathUtil.clamp(this.baseAnim.get(), 0.0F, 1.0F);
      RenderUtil.Render2D.rect(this.baseX, this.baseY, this.baseW, this.baseH, ColorUtil.getColor(255, sa * (0.015F + 0.02F * bf)), 6.0F);
      boolean blink = this.baseFocused && System.currentTimeMillis() / 450L % 2L == 0L;
      Object shown = this.randomBase.isEmpty() && !this.baseFocused ? Lang.pick("напр. mentioons", "e.g. mentioons") : this.randomBase + (blink ? "_" : "");
      font.draw(
         (String)shown,
         this.baseX + 8.0F,
         this.baseY + 6.0F,
         6.5F,
         this.randomBase.isEmpty() && !this.baseFocused ? ColorUtil.getColor(140, sa) : ColorUtil.getColor(185, sa)
      );
      font.draw(
         Lang.pick("+ 4-12 случайных символов", "+ 4-12 random chars"),
         this.sideX + pad,
         this.sideY + this.sideH - 16.0F,
         7.0F,
         ColorUtil.getColor(185, sa * 0.5F)
      );
   }

   private float mouseX() {
      return this.mouseX;
   }

   private float mouseY() {
      return this.mouseY;
   }

   private void pasteFromClipboard() {
      try {
         String clip = mc.field_1774.method_1460();
         if (clip != null && !clip.isBlank()) {
            clip = clip.trim().split("[\\r\\n]+")[0];
            if (clip.length() > 16) {
               clip = clip.substring(0, 16);
            }

            this.inputText = clip;
            this.inputFocused = true;
            this.baseFocused = false;
         }
      } catch (Exception var2) {
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float mx = (float)(click.comp_4798() / (double)this.scaleFix);
      float my = (float)(click.comp_4799() / (double)this.scaleFix);
      if (click.method_74245() != 0) {
         return super.method_25402(click, doubled);
      } else {
         boolean inSide = this.settingsOpen && MathUtil.isHovered(mx, my, this.sideX, this.sideY, this.sideW, this.sideH);
         if (!MathUtil.isHovered(mx, my, this.panelX, this.panelY, this.panelW, this.panelH) && !inSide) {
            mc.method_1507((class_437)(this.parent != null ? this.parent : new MainMenuScreen()));
            return true;
         } else if (inSide) {
            if (MathUtil.isHovered(mx, my, this.baseX, this.baseY, this.baseW, this.baseH)) {
               this.baseFocused = true;
               this.inputFocused = false;
            } else {
               this.baseFocused = false;
            }

            return true;
         } else if (MathUtil.isHovered(mx, my, this.gearX, this.gearY, this.gearW, this.gearH)) {
            this.baseFocused = this.settingsOpen = !this.settingsOpen;
            this.inputFocused = false;
            GuiSounds.picker(this.settingsOpen);
            return true;
         } else if (MathUtil.isHovered(mx, my, this.inputX, this.inputY, this.inputW, this.inputH)) {
            this.inputFocused = true;
            this.baseFocused = false;
            GuiSounds.editStart();
            return true;
         } else {
            this.inputFocused = false;
            if (MathUtil.isHovered(mx, my, this.addX, this.addY, this.addW, this.addH)) {
               this.tryAdd();
               return true;
            } else if (MathUtil.isHovered(mx, my, this.pasteX, this.pasteY, this.pasteW, this.pasteH)) {
               this.pasteFromClipboard();
               GuiSounds.button();
               return true;
            } else if (MathUtil.isHovered(mx, my, this.genX, this.genY, this.genW, this.genH)) {
               this.inputText = AltManager.get().randomName(this.randomBase);
               this.inputFocused = true;
               this.baseFocused = false;
               GuiSounds.button();
               return true;
            } else {
               for (int i = 0; i < this.favRects.size(); i++) {
                  float[] r = this.favRects.get(i);
                  if (MathUtil.isHovered(mx, my, r[0], r[1], r[2], r[3])) {
                     AltManager.get().toggleFavorite(this.rowRefs.get(i));
                     GuiSounds.chipMulti(true);
                     return true;
                  }
               }

               for (int var11 = 0; var11 < this.delRects.size(); var11++) {
                  float[] r = this.delRects.get(var11);
                  if (MathUtil.isHovered(mx, my, r[0], r[1], r[2], r[3])) {
                     this.removing.add(this.rowRefs.get(var11));
                     GuiSounds.bindReset();
                     return true;
                  }
               }

               for (int var12 = 0; var12 < this.rowRects.size(); var12++) {
                  float[] r = this.rowRects.get(var12);
                  if (MathUtil.isHovered(mx, my, r[0], r[1], r[2], r[3])) {
                     AltManager.get().login(this.rowRefs.get(var12));
                     GuiSounds.click();
                     return true;
                  }
               }

               return super.method_25402(click, doubled);
            }
         }
      }
   }

   private void tryAdd() {
      Account added = AltManager.get().add(this.inputText);
      if (added != null) {
         this.inputText = "";
         this.scrollTarget = Float.MAX_VALUE;
         GuiSounds.success();
      } else {
         GuiSounds.error();
      }
   }

   public boolean method_25404(class_11908 keyInput) {
      int key = keyInput.comp_4795();
      if (this.baseFocused) {
         if (key == 257 || key == 256) {
            this.baseFocused = false;
         } else if (key == 259 && !this.randomBase.isEmpty()) {
            this.randomBase = this.randomBase.substring(0, this.randomBase.length() - 1);
         }

         return true;
      } else if (!this.inputFocused) {
         if (key == 256) {
            if (this.settingsOpen) {
               this.settingsOpen = false;
               return true;
            } else {
               mc.method_1507((class_437)(this.parent != null ? this.parent : new MainMenuScreen()));
               return true;
            }
         } else {
            return super.method_25404(keyInput);
         }
      } else {
         boolean ctrlDown = class_3675.method_15987(mc.method_22683(), 341) || class_3675.method_15987(mc.method_22683(), 345);
         if (key == 86 && ctrlDown) {
            this.pasteFromClipboard();
            return true;
         } else {
            if (key == 257) {
               this.tryAdd();
            } else if (key == 256) {
               this.inputFocused = false;
            } else if (key == 259 && !this.inputText.isEmpty()) {
               this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
            }

            return true;
         }
      }
   }

   public boolean method_25400(class_11905 input) {
      if (this.baseFocused && input.method_74227() && this.randomBase.length() < 12) {
         String s = input.method_74226();

         for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
               this.randomBase = this.randomBase + c;
            }
         }

         return true;
      } else if (this.inputFocused && input.method_74227() && this.inputText.length() < 16) {
         String s = input.method_74226();

         for (int ix = 0; ix < s.length(); ix++) {
            char c = s.charAt(ix);
            if (Character.isLetterOrDigit(c) || c == '_') {
               this.inputText = this.inputText + c;
            }
         }

         return true;
      } else {
         return super.method_25400(input);
      }
   }

   public boolean method_25401(double mx, double my, double horizontal, double vertical) {
      this.scrollTarget -= (float)vertical * 22.0F;
      return true;
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25422() {
      return false;
   }

   @Environment(EnvType.CLIENT)
   private static final class RowAnims {
      final Animation hover = new Animation();
      final Animation del = new Animation();
      final Animation active = new Animation();
      final Animation appear = new Animation();
      final Animation headFade = new Animation();
      final Animation fav = new Animation();
      final Animation favHover = new Animation();
      float curOff;
      boolean hasOff;

      RowAnims() {
         this.appear.set(0.0);
         this.headFade.set(0.0);
      }
   }
}
