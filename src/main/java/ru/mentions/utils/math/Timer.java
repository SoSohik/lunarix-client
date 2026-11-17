package ru.mentions.utils.math;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Timer {
   private long millis;

   public Timer() {
      this.reset();
   }

   public boolean finished(long delay) {
      return System.currentTimeMillis() - delay >= this.millis;
   }

   public void reset() {
      this.millis = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      return System.currentTimeMillis() - this.millis;
   }

   @Generated
   public long getMillis() {
      return this.millis;
   }

   @Generated
   public void setMillis(long millis) {
      this.millis = millis;
   }

   @Environment(EnvType.CLIENT)
   public static class TimerNew {
      private long time;

      public TimerNew() {
         this.reset();
      }

      public boolean passedS(double s) {
         return this.getMs(System.nanoTime() - this.time) >= (long)(s * 1000.0);
      }

      public boolean passedMs(long ms) {
         return this.getMs(System.nanoTime() - this.time) >= ms;
      }

      public boolean every(long ms) {
         boolean passed = this.getMs(System.nanoTime() - this.time) >= ms;
         if (passed) {
            this.reset();
         }

         return passed;
      }

      public void setMs(long ms) {
         this.time = System.nanoTime() - ms * 1000000L;
      }

      public long getPassedTimeMs() {
         return this.getMs(System.nanoTime() - this.time);
      }

      public void reset() {
         this.time = System.nanoTime();
      }

      public long getMs(long time) {
         return time / 1000000L;
      }

      public long getTimeMs() {
         return this.getMs(System.nanoTime() - this.time);
      }
   }
}
