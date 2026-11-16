package ru.mentions.screen;

import java.util.Objects;
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
import ru.mentions.utils.emote.EmoteMetadata;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class EmotionWheelScreen extends class_437 implements IMinecraft {
   private static final int SLOTS = 6;
   private static final float RADIUS = 96.0F;
   private final String assigning;
   private final String[] hoverIds = new String[6];
   private final float[] hover = new float[6];
   private float scaleFix = 1.0F;
   private float open = 0.0F;
   private float close = 1.0F;
   private int page = 0;
   private float pageSlide = 0.0F;
   private float centerHover = 0.0F;
   private float addHover = 0.0F;
   private boolean closing = false;

   public EmotionWheelScreen() {
      this(null);
   }

   public EmotionWheelScreen(String assigning) {
      super(class_2561.method_43470("Emotions"));
      this.assigning = assigning;
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   private static float approach(float current, float target, float speed) {
      return current + (target - current) * speed;
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
            this.open = approach(this.open, this.closing ? 0.0F : 1.0F, this.closing ? 0.28F : 0.18F);
            this.close = approach(this.close, this.closing ? 0.0F : 1.0F, 0.22F);
            this.pageSlide = approach(this.pageSlide, 0.0F, 0.2F);
            float a = Math.max(0.0F, Math.min(1.0F, this.open)) * Math.max(0.0F, Math.min(1.0F, this.close));
            if (a <= 0.01F && this.closing) {
               super.method_25419();
            } else {
               int aWhite = (int)(255.0F * a);
               int accent = ColorUtil.getClientColor(1);
               Render2D.beginOverlay();
               RenderUtil.Render2D.rect(0.0F, 0.0F, w, h, ColorUtil.getColor(0, 0, 0, (int)(125.0F * a)));
               float cx = w / 2.0F;
               float cy = h / 2.0F - 12.0F;
               float radius = 96.0F * (0.86F + 0.14F * a);
               Fonts.sf_bold.drawCentered("ЭМОЦИИ", cx, cy - radius - 54.0F, 13.0F, ColorUtil.getColor(255, 255, 255, aWhite));
               Fonts.sf_regular
                  .drawCentered(
                     this.assigning != null ? "выбери сектор для эмоции" : "Страница " + (this.page + 1) + " / " + module.pageCount(),
                     cx,
                     cy + radius + 30.0F,
                     8.0F,
                     ColorUtil.getColor(172, 180, 198, (int)(215.0F * a))
                  );

               for (int i = 0; i < 6; i++) {
                  double angle = (-Math.PI / 2) + (double)i * (Math.PI / 3);
                  float appear = Math.max(0.0F, Math.min(1.0F, a * 1.5F - (float)i * 0.07F));
                  float slide = this.pageSlide * 46.0F;
                  float sx = cx + (float)Math.cos(angle) * radius * appear + slide;
                  float sy = cy + (float)Math.sin(angle) * radius * appear;
                  String emotion = module.slotValue(this.page, i);
                  if (!Objects.equals(this.hoverIds[i], emotion)) {
                     this.hoverIds[i] = emotion;
                     this.hover[i] = 0.0F;
                  }

                  boolean isHovered = MathUtil.isHovered(mx, my, sx - 30.0F, sy - 30.0F, 60.0F, 60.0F);
                  this.hover[i] = approach(this.hover[i], isHovered ? 1.0F : 0.0F, 0.22F);
                  float size = 60.0F * (1.0F + 0.1F * this.hover[i]) * appear;
                  float half = size / 2.0F;
                  RenderUtil.Blur.blur(
                     sx - half, sy - half, size, size, a, 14.0F, ColorUtil.replAlpha(ColorUtil.background(), (0.55F + 0.25F * this.hover[i]) * a)
                  );
                  RenderUtil.Render2D.rect(
                     sx - half, sy - half, size, size, ColorUtil.getColor(255, 255, 255, (int)((12.0F + 22.0F * this.hover[i]) * a)), 14.0F
                  );
                  RenderUtil.Render2D.outline(
                     sx - half,
                     sy - half,
                     size,
                     size,
                     0.75F,
                     this.hover[i] > 0.03F ? ColorUtil.replAlpha(accent, a * (0.25F + 0.45F * this.hover[i])) : ThemeColor.getOutlineColor(a * 0.14F),
                     14.0F
                  );
                  if (emotion == null) {
                     Fonts.sf_bold.drawCentered("+", sx, sy - 6.0F, 16.0F, ColorUtil.getColor(205, 214, 232, (int)(200.0F * a)));
                     Fonts.sf_regular.drawCentered("пусто", sx, sy + 13.0F, 6.5F, ColorUtil.getColor(150, 158, 176, (int)(205.0F * a)));
                  } else {
                     EmoteMetadata meta2 = EmoteLibrary.byId(emotion);
                     if (meta2 != null) {
                        float icon = size * 0.66F;
                        RenderUtil.Images.texture(
                           meta2.icon(),
                           sx - icon / 2.0F,
                           sy - icon / 2.0F,
                           icon,
                           icon,
                           0.0F,
                           0.0F,
                           1.0F,
                           1.0F,
                           ColorUtil.getColor(255, 255, 255, aWhite),
                           0.0F,
                           7.0F
                        );
                     }
                  }
               }

               boolean leftHover = MathUtil.isHovered(mx, my, cx - radius - 80.0F, cy - 14.0F, 28.0F, 28.0F);
               boolean rightHover = MathUtil.isHovered(mx, my, cx + radius + 52.0F, cy - 14.0F, 28.0F, 28.0F);
               RenderUtil.Render2D.rect(
                  cx - radius - 80.0F, cy - 14.0F, 28.0F, 28.0F, ColorUtil.getColor(255, 255, 255, (int)((leftHover ? 36.0F : 14.0F) * a)), 8.0F
               );
               RenderUtil.Render2D.rect(
                  cx + radius + 52.0F, cy - 14.0F, 28.0F, 28.0F, ColorUtil.getColor(255, 255, 255, (int)((rightHover ? 36.0F : 14.0F) * a)), 8.0F
               );
               Fonts.sf_bold.drawCentered("<", cx - radius - 66.0F, cy - 7.0F, 10.0F, ColorUtil.getColor(232, 238, 250, aWhite));
               Fonts.sf_bold.drawCentered(">", cx + radius + 66.0F, cy - 7.0F, 10.0F, ColorUtil.getColor(232, 238, 250, aWhite));
               float btnW = 190.0F;
               float btnH = 24.0F;
               float btnX = cx - btnW / 2.0F;
               float btnY = h - 62.0F;
               boolean overAdd = MathUtil.isHovered(mx, my, btnX, btnY, btnW, btnH);
               this.addHover = approach(this.addHover, overAdd ? 1.0F : 0.0F, 0.22F);
               float btnScale = 1.0F + 0.04F * this.addHover;
               RenderUtil.Render2D.rect(btnX, btnY, btnW * btnScale, btnH * btnScale, ColorUtil.replAlpha(accent, (0.38F + 0.24F * this.addHover) * a), 9.0F);
               RenderUtil.Render2D.outline(
                  btnX, btnY, btnW * btnScale, btnH * btnScale, 0.75F, ThemeColor.getOutlineColor(a * (0.25F + 0.3F * this.addHover)), 9.0F
               );
               Fonts.sf_bold.drawCentered("+  ДОБАВИТЬ ЭМОЦИИ", btnX + btnW * btnScale / 2.0F, btnY + 8.0F, 8.5F, ColorUtil.getColor(255, 255, 255, aWhite));
               String active = module.activeEmotion();
               EmoteMetadata meta;
               if (active != null && (meta = EmoteLibrary.byId(active)) != null) {
                  RenderUtil.Images.texture(
                     meta.icon(), cx - 8.0F, cy + radius + 42.0F, 16.0F, 16.0F, 0.0F, 0.0F, 1.0F, 1.0F, ColorUtil.getColor(255, 255, 255, aWhite), 0.0F, 4.0F
                  );
               }

               Fonts.sf_regular
                  .drawCentered(
                     "ЛКМ — проиграть · ПКМ — очистить · колесо мыши — страница", cx, h - 24.0F, 7.0F, ColorUtil.getColor(158, 166, 184, (int)(200.0F * a))
                  );
               Render2D.endOverlay();
            }
         } else {
            this.method_25419();
         }
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      Emotions module = Emotions.getInstance();
      if (module == null) {
         return true;
      } else {
         this.page = (this.page + (vertical > 0.0 ? -1 : 1) + module.pageCount()) % module.pageCount();
         this.pageSlide = vertical > 0.0 ? -1.0F : 1.0F;
         return true;
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      Emotions module = Emotions.getInstance();
      if (module != null && mc.field_1724 != null) {
         float mx = (float)(click.comp_4798() / (double)this.scaleFix);
         float my = (float)(click.comp_4799() / (double)this.scaleFix);
         int button = click.method_74245();
         float w = (float)mc.method_22683().method_4486() / this.scaleFix;
         float h = (float)mc.method_22683().method_4502() / this.scaleFix;
         float cx = w / 2.0F;
         float cy = h / 2.0F - 12.0F;
         float radius = 96.0F;
         float btnW = 190.0F;
         float btnX = cx - btnW / 2.0F;
         float btnY = h - 62.0F;
         float btnH = 24.0F;
         if (MathUtil.isHovered(mx, my, btnX, btnY, btnW, btnH)) {
            module.openAddScreen(this);
            return true;
         } else if (MathUtil.isHovered(mx, my, cx - radius - 80.0F, cy - 14.0F, 28.0F, 28.0F)) {
            this.page = (this.page - 1 + module.pageCount()) % module.pageCount();
            this.pageSlide = -1.0F;
            return true;
         } else if (MathUtil.isHovered(mx, my, cx + radius + 52.0F, cy - 14.0F, 28.0F, 28.0F)) {
            this.page = (this.page + 1) % module.pageCount();
            this.pageSlide = 1.0F;
            return true;
         } else {
            for (int i = 0; i < 6; i++) {
               double angle = (-Math.PI / 2) + (double)i * (Math.PI / 3);
               float sx = cx + (float)Math.cos(angle) * radius;
               float sy;
               if (MathUtil.isHovered(mx, my, sx - 30.0F, (sy = cy + (float)Math.sin(angle) * radius) - 30.0F, 60.0F, 60.0F)) {
                  if (this.assigning != null) {
                     module.setSlot(this.page, i, this.assigning);
                     module.play(this.assigning);
                     this.method_25419();
                     return true;
                  }

                  if (button == 1) {
                     module.setSlot(this.page, i, null);
                     return true;
                  }

                  String emotion = module.slotValue(this.page, i);
                  if (emotion == null) {
                     if (module.activeEmotion() != null) {
                        module.stop();
                        this.method_25419();
                        return true;
                     }

                     module.openAddScreen(this);
                  } else {
                     EmoteMetadata slotMeta = EmoteLibrary.byId(emotion);
                     String canonical = slotMeta != null ? slotMeta.id() : emotion;
                     if (canonical.equalsIgnoreCase(module.activeEmotion())) {
                        module.stop();
                        this.method_25419();
                        return true;
                     }

                     module.play(canonical);
                     this.method_25419();
                  }

                  return true;
               }
            }

            if (module.activeEmotion() != null) {
               module.stop();
            }

            this.method_25419();
            return true;
         }
      } else {
         this.method_25419();
         return true;
      }
   }

   public void method_25419() {
      this.closing = true;
   }
}
