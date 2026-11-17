package ru.mentions.screen.editor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class EditorButton {
   private final String label;
   private final boolean accent;
   private final Animation hover = new Animation();
   private final Animation press = new Animation();
   private float x;
   private float y;
   private float width;
   private float height;

   public EditorButton(String label, boolean accent) {
      this.label = label;
      this.accent = accent;
   }

   public String label() {
      return this.label;
   }

   public boolean accent() {
      return this.accent;
   }

   public float x() {
      return this.x;
   }

   public float y() {
      return this.y;
   }

   public float width() {
      return this.width;
   }

   public float height() {
      return this.height;
   }

   public float hoverValue() {
      return this.hover.get();
   }

   public float pressValue() {
      return this.press.get();
   }

   public void set(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public void update(float mouseX, float mouseY) {
      this.hover.update();
      this.hover.run(this.contains(mouseX, mouseY) ? 1.0 : 0.0, 0.18F, Easings.QUAD_OUT, true);
      this.press.update();
   }

   public boolean contains(float mouseX, float mouseY) {
      return MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
   }

   public void press() {
      this.press.set(1.0);
      this.press.run(0.0, 0.35F, Easings.QUAD_OUT);
   }
}
