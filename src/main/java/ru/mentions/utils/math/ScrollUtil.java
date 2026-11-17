package ru.mentions.utils.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ScrollUtil {
   private final class_310 mc = class_310.method_1551();
   private float target;
   private float scroll;
   private float max;
   private float speed = 8.0F;
   private boolean enabled;
   private double wheel;
   float barHeight;

   public ScrollUtil() {
      this.setEnabled(true);
      GLFW.glfwSetScrollCallback(this.mc.method_22683().method_4490(), (window, xOffset, yOffset) -> this.wheel += yOffset);
   }

   public void update() {
      float wheelDelta = (float)this.wheel * this.speed * 10.0F;
      float stretch = 0.0F;
      this.scroll = this.lerp(this.scroll, this.target, (double)(this.speed / 100.0F));
      if (!this.enabled) {
         this.wheel = 0.0;
      } else {
         this.target = Math.min(
            Math.max(this.target + wheelDelta / 2.0F, this.max - (wheelDelta == 0.0F ? 0.0F : stretch)), wheelDelta == 0.0F ? 0.0F : stretch
         );
         this.wheel = 0.0;
      }
   }

   public void render(float x, float y, float width, float height, float alpha) {
      if (this.max != 0.0F) {
         float percentage = this.getScroll() / this.getMax();
         this.barHeight = this.lerp(height - this.getMax() / (this.getMax() - height) * height, this.barHeight, 0.9F);
         boolean allowed = this.barHeight < height;
         if (allowed) {
            float scrollY = y + height * percentage - this.barHeight * percentage;
         }
      }
   }

   public void reset() {
      this.scroll = 0.0F;
      this.target = 0.0F;
   }

   public void setMax(float max, float height) {
      this.max = -max + height;
   }

   public float lerp(float a, float b, double f) {
      return (float)((double)a + f * (double)(b - a));
   }

   public float clamp(float val, float min, float max) {
      return Math.max(min, Math.min(max, val));
   }

   public float getScroll() {
      return this.scroll;
   }

   public float getMax() {
      return this.max;
   }

   public void setMax(float max) {
      this.max = max;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
