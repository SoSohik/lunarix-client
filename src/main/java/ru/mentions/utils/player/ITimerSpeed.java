package ru.mentions.utils.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ITimerSpeed {
   float getSpeed();

   void setSpeed(float var1);

   default void resetSpeed() {
      this.setSpeed(1.0F);
   }
}
