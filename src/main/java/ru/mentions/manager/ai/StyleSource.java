package ru.mentions.manager.ai;

public interface StyleSource {
   boolean hasStyle();

   float[] nextGesture(float var1, float var2, float var3);

   float[] nextStroke(float var1);

   double[] predict(double[] var1);
}
