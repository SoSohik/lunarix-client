package ru.mentions.utils.animation.satoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EaseInOutQuad extends Animation {
   public EaseInOutQuad(int ms, double endPoint) {
      super(ms, endPoint);
   }

   public EaseInOutQuad(int ms, double endPoint, Direction direction) {
      super(ms, endPoint, direction);
   }

   @Override
   protected double getEquation(double x1) {
      double x = Math.max(0.0, Math.min(1.0, x1 / (double)this.duration));
      return x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0;
   }
}
