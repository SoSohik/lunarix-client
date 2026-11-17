package ru.mentions.mixin;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_429;
import net.minecraft.class_433;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.lang.Lang;
import ru.mentions.module.impl.player.PvpSafe;
import ru.mentions.screen.Menu;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.other.GuiSounds;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_433.class})
public abstract class GameMenuScreenMixin extends class_437 {
   @Shadow
   private class_4185 field_40792;
   @Unique
   private final Animation mentions$anim = new Animation();
   @Unique
   private final Map<Integer, Animation> mentions$buttonHovers = new HashMap<>();
   @Unique
   private static final float BTN_W = 180.0F;
   @Unique
   private static final float BTN_H = 26.0F;
   @Unique
   private static final float BTN_GAP = 6.0F;
   @Unique
   private static final int BTN_COUNT = 4;

   protected GameMenuScreenMixin(class_2561 title) {
      super(title);
   }

   @Unique
   private MentionsPauseLayout mentions$layout(float sw, float sh, float slideY) {
      MentionsPauseLayout l = new MentionsPauseLayout();
      float logoSize = 36.0F;
      float labelBlock = 28.0F;
      float topGap = 18.0F;
      float btnH = BTN_H;
      float gap = BTN_GAP;
      float total = logoSize + labelBlock + topGap + (float)BTN_COUNT * btnH + (float)(BTN_COUNT - 1) * gap;
      float available = Math.max(48.0F, sh - 12.0F);
      float scale = total > available ? available / total : 1.0F;
      scale = Math.max(0.35F, Math.min(1.0F, scale));
      l.scale = scale;
      l.logoSize = logoSize * scale;
      l.labelBlock = labelBlock * scale;
      l.topGap = topGap * scale;
      l.btnH = Math.max(9.0F, btnH * scale);
      l.gap = Math.max(1.5F, gap * scale);
      l.btnW = Math.min(BTN_W, Math.max(96.0F, sw - 24.0F));
      l.btnX = (sw - l.btnW) / 2.0F;
      float belowCenter = l.logoSize / 2.0F
         + l.labelBlock
         + l.topGap
         + (float)BTN_COUNT * l.btnH
         + (float)(BTN_COUNT - 1) * l.gap;
      float maxCenter = sh - 6.0F - belowCenter;
      float logoCenter = sh * 0.23F + slideY;
      float minCenter = l.logoSize / 2.0F + 4.0F + Math.max(0.0F, slideY - 4.0F);
      if (minCenter <= maxCenter) {
         logoCenter = Math.min(Math.max(logoCenter, minCenter), maxCenter);
      } else {
         logoCenter = maxCenter;
      }

      l.logoY = logoCenter;
      l.btnY = logoCenter + l.logoSize / 2.0F + l.labelBlock + l.topGap;
      return l;
   }

   @Unique
   private static final class MentionsPauseLayout {
      float scale;
      float logoY;
      float logoSize;
      float labelBlock;
      float topGap;
      float btnX;
      float btnY;
      float btnW;
      float btnH;
      float gap;
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("TAIL")}
   )
   private void mentions$onInit(CallbackInfo ci) {
      this.mentions$anim.set(0.0);
      this.mentions$anim.run(1.0, 0.22, Easings.CUBIC_OUT);
   }

   @Inject(
      method = {"method_25420"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentions$cancelDefaultBackground(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentions$renderCustomPauseMenu(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         ci.cancel();
         if (this.field_40792 != null) {
            this.field_40792.field_22763 = !PvpSafe.isActive();
         }

         this.mentions$anim.update();
         float alpha = Math.max(0.0F, Math.min(1.0F, this.mentions$anim.get()));
         float sw = (float)this.field_22789;
         float sh = (float)this.field_22790;
         ScreenBlur.capture(2);
         RenderUtil.Blur.blur(0.0F, 0.0F, sw, sh, alpha, ColorUtil.getColor(0, alpha * 0.48F));
         float slideY = 20.0F * (1.0F - alpha);
         MentionsPauseLayout layout = this.mentions$layout(sw, sh, slideY);
         float logoY = layout.logoY;
         float logoSize = layout.logoSize;
         RenderUtil.Render2D.glow(
            sw / 2.0F - logoSize / 2.0F,
            logoY - logoSize / 2.0F,
            logoSize,
            logoSize,
            ColorUtil.replAlpha(ColorUtil.client(), alpha * 0.4F),
            logoSize / 2.0F,
            14.0F * layout.scale,
            1.0F
         );
         RenderUtil.Images.texture(
            class_2960.method_60655("client", "textures/logo.png"),
            sw / 2.0F - logoSize / 2.0F,
            logoY - logoSize / 2.0F,
            logoSize,
            logoSize,
            ColorUtil.getColor(255, alpha)
         );
         Fonts.sf_bold.drawCentered(
            "LUNARIX",
            sw / 2.0F,
            logoY + logoSize / 2.0F + 10.0F * layout.scale,
            18.0F * layout.scale,
            ColorUtil.getColor(255, 255, 255, alpha)
         );
         float btnStartX = layout.btnX;
         float btnStartY = layout.btnY;
         String[] btnLabels = new String[]{
            Lang.pick("Повернутися до гри", "Вернуться в игру", "Back to Game"),
            Lang.pick("Налаштування", "Настройки", "Options"),
            Lang.pick("Меню клієнта", "Меню клиента", "Click GUI"),
            PvpSafe.isActive()
               ? Lang.pick("Відключитися (PvP Safe)", "Отключиться (PvP Safe)", "Disconnect (PvP Safe)")
               : Lang.pick("Відключитися", "Отключиться", "Disconnect")
         };

         for (int i = 0; i < btnLabels.length; i++) {
            float by = btnStartY + (float)i * (layout.btnH + layout.gap);
            boolean hov = MathUtil.isHovered((float)mouseX, (float)mouseY, btnStartX, by, layout.btnW, layout.btnH);
            Animation hoverAnim = this.mentions$buttonHovers.computeIfAbsent(i, k -> new Animation());
            hoverAnim.update();
            double targetHov = hov ? 1.0 : 0.0;
            if (hoverAnim.getToValue() != targetHov) {
               hoverAnim.run(targetHov, 0.16, Easings.QUAD_OUT);
            }

            float hp = hoverAnim.get();
            boolean isDisconnect = i == 3;
            boolean isResume = i == 0;
            boolean blocked = isDisconnect && PvpSafe.isActive();
            int bgCol = blocked
               ? ColorUtil.getColor(40, 20, 20, (int)(180.0F * alpha))
               : ColorUtil.getColor((int)(20.0F + hp * 14.0F), (int)(22.0F + hp * 14.0F), (int)(28.0F + hp * 18.0F), (int)((190.0F + hp * 40.0F) * alpha));
            RenderUtil.Render2D.rect(btnStartX, by, layout.btnW, layout.btnH, bgCol, 6.0F * layout.scale);
            if (blocked) {
               RenderUtil.Render2D.outline(
                  btnStartX, by, layout.btnW, layout.btnH, 0.8F, ColorUtil.getColor(200, 50, 50, (int)(180.0F * alpha)), 6.0F * layout.scale
               );
            } else if (isResume) {
               RenderUtil.Render2D.outline(
                  btnStartX,
                  by,
                  layout.btnW,
                  layout.btnH,
                  1.0F + hp * 0.4F,
                  ColorUtil.replAlpha(ColorUtil.client(), (0.4F + hp * 0.5F) * alpha),
                  6.0F * layout.scale
               );
               if (hp > 0.01F) {
                  RenderUtil.Render2D.glow(
                     btnStartX,
                     by,
                     layout.btnW,
                     layout.btnH,
                     ColorUtil.replAlpha(ColorUtil.client(), 0.15F * hp * alpha),
                     6.0F * layout.scale,
                     8.0F * layout.scale,
                     1.0F
                  );
               }
            } else if (isDisconnect && hp > 0.01F) {
               RenderUtil.Render2D.outline(
                  btnStartX, by, layout.btnW, layout.btnH, 1.0F, ColorUtil.getColor(235, 75, 75, (int)(hp * 220.0F * alpha)), 6.0F * layout.scale
               );
               RenderUtil.Render2D.glow(
                  btnStartX,
                  by,
                  layout.btnW,
                  layout.btnH,
                  ColorUtil.getColor(235, 75, 75, (int)(hp * 50.0F * alpha)),
                  6.0F * layout.scale,
                  8.0F * layout.scale,
                  1.0F
               );
            } else {
               int outlineCol = ColorUtil.getColor(255, (0.06F + hp * 0.2F) * alpha);
               RenderUtil.Render2D.outline(btnStartX, by, layout.btnW, layout.btnH, 0.6F + hp * 0.4F, outlineCol, 6.0F * layout.scale);
            }

            int textCol = blocked
               ? ColorUtil.getColor(200, 100, 100, (int)(200.0F * alpha))
               : (isResume ? ColorUtil.getColor(255, alpha) : ColorUtil.getColor((int)(190.0F + hp * 65.0F), alpha));
            Fonts.sf_regular.drawCentered(
               btnLabels[i], sw / 2.0F, by + layout.btnH / 2.0F - 4.0F * layout.scale, 7.0F * layout.scale, textCol
            );
         }
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float mouseX = (float)click.comp_4798();
      float mouseY = (float)click.comp_4799();
      int button = click.method_74245();
      if (button != 0) {
         return super.method_25402(click, doubled);
      } else {
         float sw = (float)this.field_22789;
         float sh = (float)this.field_22790;
         float alpha = Math.max(0.0F, Math.min(1.0F, this.mentions$anim.get()));
         MentionsPauseLayout layout = this.mentions$layout(sw, sh, 20.0F * (1.0F - alpha));
         float btnStartX = layout.btnX;
         float btnStartY = layout.btnY;

         for (int i = 0; i < BTN_COUNT; i++) {
            float by = btnStartY + (float)i * (layout.btnH + layout.gap);
            if (MathUtil.isHovered(mouseX, mouseY, btnStartX, by, layout.btnW, layout.btnH)) {
               GuiSounds.button();
               switch (i) {
                  case 0:
                     this.method_25419();
                     break;
                  case 1:
                     if (this.field_22787 != null) {
                        this.field_22787.method_1507(new class_429(this, this.field_22787.field_1690));
                     }
                     break;
                  case 2:
                     if (this.field_22787 != null) {
                        this.field_22787.method_1507(new Menu());
                     }
                     break;
                  case 3:
                     if (!PvpSafe.isActive() && this.field_40792 != null) {
                        this.field_40792.method_25306(click);
                     }
               }

               return true;
            }
         }

         return super.method_25402(click, doubled);
      }
   }
}
