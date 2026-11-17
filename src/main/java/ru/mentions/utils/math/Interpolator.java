package ru.mentions.utils.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Interpolator {
   public static <T extends Number> T lerp(T input, T target, double step) {
      double start = input.doubleValue();
      double end = target.doubleValue();
      double result = start + step * (end - start);
      if (input instanceof Integer) {
         return (T)(int)Math.round(result);
      } else if (input instanceof Double) {
         return (T)result;
      } else if (input instanceof Float) {
         return (T)(float)result;
      } else if (input instanceof Long) {
         return (T)Math.round(result);
      } else if (input instanceof Short) {
         return (T)(short)((int)Math.round(result));
      } else if (input instanceof Byte) {
         return (T)(byte)((int)Math.round(result));
      } else {
         throw new IllegalArgumentException("Unsupported type: " + input.getClass().getSimpleName());
      }
   }
}
