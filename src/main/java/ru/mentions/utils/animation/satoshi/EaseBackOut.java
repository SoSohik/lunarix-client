package ru.mentions.utils.animation.satoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EaseBackOut extends Animation {
   public EaseBackOut(int ms, double endPoint) {
      super(ms, endPoint);
   }

   public EaseBackOut(int ms, double endPoint, Direction direction) {
      super(ms, endPoint, direction);
   }

   @Override
   protected double getEquation(double x1) {
      double x = Math.max(0.0, Math.min(1.0, x1 / (double)this.duration));
      return 1.0 + 2.70158 * Math.pow(x - 1.0, 3.0) + 1.70158 * Math.pow(x - 1.0, 2.0);
   }
}
