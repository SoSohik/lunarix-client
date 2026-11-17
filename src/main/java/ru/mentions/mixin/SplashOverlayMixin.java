package ru.mentions.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4011;
import net.minecraft.class_425;
import net.minecraft.class_9848;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.screen.MainMenuScreen;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_425.class})
public abstract class SplashOverlayMixin {
   @Shadow
   @Final
   private class_310 field_18217;
   @Shadow
   @Final
   private class_4011 field_17767;
   @Shadow
   @Final
   private boolean field_18219;
   @Shadow
   private float field_17770;
   @Shadow
   private long field_17771;
   @Shadow
   private long field_18220;
   @Unique
   private static final int MENTIONS_BG = 330246;
   @Unique
   private static final class_2960[] MENU_BGS = new class_2960[]{
      class_2960.method_60655("client", "textures/frame/menu.png"),
      class_2960.method_60655("client", "textures/frame/menu2.png"),
      class_2960.method_60655("client", "textures/frame/menu3.png"),
      class_2960.method_60655("client", "textures/frame/menu4.png")
   };
   @Unique
   private long mentionsStart = -1L;

   @Unique
   private static int withAlpha(int rgb, int alpha) {
      return rgb & 16777215 | alpha << 24;
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentionsRender(class_332 context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         int i = context.method_51421();
         int j = context.method_51443();
         long l = class_156.method_658();
         if (this.mentionsStart == -1L) {
            this.mentionsStart = l;
         }

         if (this.field_18219 && this.field_18220 == -1L) {
            this.field_18220 = l;
         }

         float f = this.field_17771 > -1L ? (float)(l - this.field_17771) / 1000.0F : -1.0F;
         float g = this.field_18220 > -1L ? (float)(l - this.field_18220) / 500.0F : -1.0F;
         float alpha;
         if (f >= 1.0F) {
            if (this.field_18217.field_1755 != null) {
               this.field_18217.field_1755.method_47413(context, 0, 0, deltaTicks);
            } else {
               this.field_18217.field_1705.method_72738();
            }

            context.method_71048();
            alpha = 1.0F - class_3532.method_15363(f - 1.0F, 0.0F, 1.0F);
         } else if (this.field_18219) {
            if (this.field_18217.field_1755 != null && g < 1.0F) {
               this.field_18217.field_1755.method_47413(context, mouseX, mouseY, deltaTicks);
            } else {
               this.field_18217.field_1705.method_72738();
            }

            context.method_71048();
            alpha = class_3532.method_15363(g, 0.0F, 1.0F);
         } else {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.field_18217.method_1522().method_30277(), -16446970);
            alpha = 1.0F;
         }

         float rp = this.field_17767.method_18229();
         this.field_17770 = class_3532.method_15363(this.field_17770 * 0.95F + rp * 0.050000012F, 0.0F, 1.0F);
         boolean drawn = false;

         try {
            drawn = this.drawMentions(context, alpha, l);
         } catch (Throwable var17) {
            drawn = false;
         }

         if (!drawn) {
            this.drawFallback(context, i, j, alpha);
         }

         if (f >= 2.0F) {
            this.field_18217.method_18502(null);
         }

         ci.cancel();
      }
   }

   @Unique
   private boolean drawMentions(class_332 context, float fade, long now) {
      double scaleFactor = (double)this.field_18217.method_22683().method_4495();
      if (scaleFactor <= 0.0) {
         return false;
      } else {
         float scaleFix = 2.0F / (float)scaleFactor;
         int sw = (int)((float)this.field_18217.method_22683().method_4486() / scaleFix);
         int sh = (int)((float)this.field_18217.method_22683().method_4502() / scaleFix);
         if (sw > 0 && sh > 0) {
            float intro = class_3532.method_15363((float)(now - this.mentionsStart) / 600.0F, 0.0F, 1.0F);
            float ease = 1.0F - (1.0F - intro) * (1.0F - intro) * (1.0F - intro);
            float a = class_3532.method_15363(fade * ease, 0.0F, 1.0F);
            if (a <= 0.01F) {
               return true;
            } else {
               float t = (float)now / 1000.0F;
               float breathe = 1.0F + 0.03F * (float)Math.sin((double)(t * 2.0F));
               float floatY = 2.0F * (float)Math.sin((double)(t * 1.3F));
               float rise = (1.0F - ease) * 14.0F;
               context.method_51448().pushMatrix();
               Render2D.beginOverlay();
               RenderUtil.Render2D.rect(0.0F, 0.0F, (float)sw, (float)sh, ColorUtil.getColor(0, a));
               class_2960 bg = MENU_BGS[Math.floorMod(MainMenuScreen.getBgSaved(), MENU_BGS.length)];
               RenderUtil.Images.texture(bg, 0.0F, 0.0F, (float)sw, (float)sh, ColorUtil.getColor(255, a));
               ScreenBlur.capture(2);
               RenderUtil.Blur.blur(0.0F, 0.0F, (float)sw, (float)sh, a, ColorUtil.getColor(0, a * 0.2F));
               ScreenBlur.capture(4);
               RenderUtil.Render2D.rect(0.0F, 0.0F, (float)sw, (float)sh, ColorUtil.getColor(0, a * 0.2F));
               ScreenBlur.capture(2);
               float cx = (float)sw / 2.0F;
               float cy = (float)sh / 2.0F - 18.0F + floatY + rise;
               float logoSize = 48.0F * breathe;
               RenderUtil.Render2D.glow(
                  cx - logoSize / 2.0F,
                  cy - logoSize / 2.0F,
                  logoSize,
                  logoSize,
                  ColorUtil.replAlpha(ColorUtil.client(), a * 0.45F),
                  logoSize / 2.0F,
                  16.0F,
                  1.0F
               );
               RenderUtil.Images.texture(
                  class_2960.method_60655("client", "textures/logo.png"),
                  cx - logoSize / 2.0F,
                  cy - logoSize / 2.0F,
                  logoSize,
                  logoSize,
                  ColorUtil.getColor(255, a)
               );
               Fonts.sf_bold.drawCentered("LUNARIX", cx, cy + logoSize / 2.0F + 12.0F, 26.0F, -16723201);
               int dots = (int)(now / 400L % 4L);
               long elapsed = now - this.mentionsStart;
               String sub = elapsed < 1000L ? "Приветствую!" : "скоро загружусь" + ".".repeat(dots);
               Fonts.sf_regular.drawCentered(sub, cx, cy + 68.0F, 9.0F, ColorUtil.getColor(154, 164, 184, a * 0.85F));
               this.drawBarBranded(sw, sh, a, t);
               Fonts.sf_bold.drawCentered("Lunarix · 2026 · Все права защищены", cx, (float)sh - 22.0F, 8.0F, ColorUtil.getColor(255, a * 0.12F));
               Render2D.endOverlay();
               context.method_51448().popMatrix();
               return true;
            }
         } else {
            return false;
         }
      }
   }

   @Unique
   private void drawBarBranded(int sw, int sh, float a, float t) {
      float barW = Math.min((float)sw * 0.36F, 280.0F);
      float barH = 4.0F;
      float x = (float)sw / 2.0F - barW / 2.0F;
      float y = (float)sh * 0.5F + 104.0F;
      RenderUtil.Render2D.rect(x, y, barW, barH, ColorUtil.getColor(20, 36, 26, a * 0.85F), barH / 2.0F);
      float fillW = barW * this.field_17770;
      if (fillW > 0.5F) {
         RenderUtil.Render2D.rect(x, y, fillW, barH, ColorUtil.getColor(110, 255, 133, a), barH / 2.0F);
         float sweep = t * 0.6F % 1.0F;
         float sx = x + sweep * fillW;
         RenderUtil.Render2D.rect(sx - 6.0F, y, 12.0F, barH, ColorUtil.getColor(160, 255, 190, a * 0.35F), barH / 2.0F);
      }

      String pct = (int)(this.field_17770 * 100.0F) + "%";
      Fonts.sf_bold.draw(pct, x + barW + 8.0F, y - 2.5F, 8.0F, ColorUtil.getColor(220, 226, 240, a));
   }

   @Unique
   private void drawFallback(class_332 context, int width, int height, float alpha) {
      int a = class_3532.method_15386(class_3532.method_15363(alpha, 0.0F, 1.0F) * 255.0F);
      if (a > 4) {
         context.method_25294(0, 0, width, height, withAlpha(330246, a));
         class_327 tr = this.field_18217.field_1772;
         Matrix3x2fStack matrices = context.method_51448();
         String title = "LUNARIX";
         float s = 4.0F;
         matrices.pushMatrix();
         matrices.scale(s, s);
         float cx = (float)width / 2.0F / s;
         float ty = ((float)height / 2.0F - 26.0F) / s;
         float half = (float)tr.method_1727(title) / 2.0F;
         context.method_51433(tr, title, (int)(cx - half), (int)ty, withAlpha(7274373, a), false);
         matrices.popMatrix();
         int barW = (int)Math.min((float)width * 0.5F, 320.0F);
         int x = width / 2 - barW / 2;
         int y = (int)((float)height * 0.78F);
         context.method_25294(x, y, x + barW, y + 4, withAlpha(1055250, (int)((float)a * 0.85F)));
         int fillW = class_3532.method_15386((float)(barW - 2) * this.field_17770);
         context.method_25294(x + 1, y + 1, x + 1 + fillW, y + 3, withAlpha(7274373, a));
         String pct = (int)(this.field_17770 * 100.0F) + "%";
         context.method_51433(tr, pct, x + barW + 8, y - 2, class_9848.method_61324(a, 220, 226, 240), false);
      }
   }
}
