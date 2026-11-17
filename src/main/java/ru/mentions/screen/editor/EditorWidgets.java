package ru.mentions.screen.editor;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.RussianUi;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class EditorWidgets {
   private final Map<String, Animation> anims = new HashMap<>();
   private final Map<String, float[]> smoothVals = new HashMap<>();
   private float mouseX;
   private float mouseY;

   public void setMouse(float mouseX, float mouseY) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }

   public float mouseX() {
      return this.mouseX;
   }

   public float mouseY() {
      return this.mouseY;
   }

   public Animation anim(String key) {
      return this.anims.computeIfAbsent(key, k -> new EaseInOutQuad(300, 1.0));
   }

   public float rowHover(String key, Rect bounds) {
      Animation animation = this.anim(key + ":hover");
      animation.setDirection(bounds.contains(this.mouseX, this.mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
      return animation.getOutput();
   }

   public float smooth(String key, float target) {
      float[] value = this.smoothVals.computeIfAbsent(key, k -> new float[]{target});
      value[0] += (target - value[0]) * 0.2F;
      return value[0];
   }

   public void drawRowBackground(Rect b, float hover, float alpha) {
      RenderUtil.Render2D.glow(b.x(), b.y(), b.width(), b.height(), ColorUtil.getColor(0, 0.06F * alpha), 4.0F, 6.0F, 1.0F);
      RenderUtil.Render2D.rect(b.x(), b.y(), b.width(), b.height(), ColorUtil.getColor(40, 0.15F * alpha), 4.0F);
      RenderUtil.Render2D.outline(b.x(), b.y(), b.width(), b.height(), 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * hover), 4.0F);
   }

   public void drawToggleRow(Rect b, String label, boolean value, String key, float alpha) {
      if (!(b.width() <= 0.0F) && !(alpha <= 0.01F)) {
         float hover = this.rowHover(key, b);
         this.drawRowBackground(b, hover, alpha);
         Fonts.sf_regular.draw(label, b.x() + 6.0F, b.y() + 3.2F, 6.0F, ColorUtil.getColor(255, alpha * (0.5F + 0.5F * hover)));
         Animation state = this.anim(key + ":on");
         state.setDirection(value ? Direction.FORWARDS : Direction.BACKWARDS);
         float on = state.getOutput();
         RenderUtil.Render2D.rect(
            b.x() + b.width() - 6.0F - 11.0F,
            b.y() + 3.5F,
            11.0F,
            6.0F,
            ColorUtil.overCol(ColorUtil.getColor(0, 0.2F * alpha), ColorUtil.replAlpha(ColorUtil.client(), alpha * (0.5F + 0.5F * hover)), on),
            3.0F
         );
         RenderUtil.Render2D.rect(
            b.x() + b.width() - 6.0F - 11.0F + 1.0F + 4.5F * on,
            b.y() + 4.5F,
            4.0F,
            4.0F,
            ColorUtil.getColor(255, alpha * (0.2F + 0.4F * on + 0.4F * hover)),
            2.0F
         );
      }
   }

   public void drawButton(EditorButton button, float alpha) {
      float hover = button.hoverValue();
      float press = button.pressValue();
      float accent = Math.max(hover, press);
      RenderUtil.Render2D.glow(button.x(), button.y(), button.width(), button.height(), ColorUtil.getColor(0, 0.15F * alpha), 5.0F, 10.0F, 1.0F);
      RenderUtil.Blur.blur(
         button.x(), button.y(), button.width(), button.height(), alpha, 5.0F, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.6F), alpha)
      );
      if (button.accent()) {
         RenderUtil.Render2D.rect(
            button.x(), button.y(), button.width(), button.height(), ColorUtil.replAlpha(ColorUtil.client(), alpha * (0.2F + 0.2F * accent)), 5.0F
         );
      }

      RenderUtil.Render2D.outline(
         button.x(),
         button.y(),
         button.width(),
         button.height(),
         0.5F,
         ColorUtil.replAlpha(ColorUtil.client(), alpha * (button.accent() ? 0.5F + 0.5F * accent : accent)),
         5.0F
      );
      Fonts.sf_regular
         .drawCentered(
            RussianUi.text(button.label()),
            button.x() + button.width() / 2.0F,
            button.y() + button.height() / 2.0F - 3.8F,
            6.5F,
            ColorUtil.getColor(255, alpha * (0.6F + 0.4F * accent))
         );
   }
}
