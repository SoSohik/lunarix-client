package ru.mentions.utils.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StopGPT {
   private long lastMS = System.currentTimeMillis();

   public void reset() {
      this.lastMS = System.currentTimeMillis();
   }

   public boolean hasTimePassed(long ms) {
      return System.currentTimeMillis() - this.lastMS >= ms;
   }

   public long getPassedTime() {
      return System.currentTimeMillis() - this.lastMS;
   }
}
