package ru.mentions.utils.aura;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public class GCDUtil implements IMinecraft {
   public static float getSensitivity(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getGCDValue() {
      return (float)((double)getGCD() * 0.15);
   }

   public static float getGCD() {
      float f1 = (float)((Double)mc.field_1690.method_42495().method_41753() * 0.6 + 0.2);
      return f1 * f1 * f1 * 8.0F;
   }

   public static float getDeltaMouse(float delta) {
      return (float)Math.round(delta / getGCDValue());
   }

   public static float applyGCD(float targetRotation, float currentRotation) {
      float sensitivity = ((Double)mc.field_1690.method_42495().method_41753()).floatValue();
      float f = sensitivity * 0.6F + 0.2F;
      float f1 = f * f * f * 8.0F;
      float delta = targetRotation - currentRotation;
      float adjustedDelta = (float)Math.round(delta / (f1 * 0.15F)) * f1 * 0.15F;
      return currentRotation + adjustedDelta;
   }
}
