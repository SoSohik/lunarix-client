package ru.mentions.screen.editor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public record Rect(float x, float y, float width, float height) {
   public static final Rect EMPTY = new Rect(0.0F, 0.0F, 0.0F, 0.0F);

   public boolean contains(float mouseX, float mouseY) {
      return MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
   }

   public float centerX() {
      return this.x + this.width / 2.0F;
   }

   public float centerY() {
      return this.y + this.height / 2.0F;
   }

   public float distanceSquaredToCenter(float mouseX, float mouseY) {
      float dx = mouseX - this.centerX();
      float dy = mouseY - this.centerY();
      return dx * dx + dy * dy;
   }
}
