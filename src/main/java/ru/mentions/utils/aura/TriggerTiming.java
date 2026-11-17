package ru.mentions.utils.aura;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class TriggerTiming {
   private final DoubleSupplier random;
   private Object target;
   private long acquiredAt;
   private long lastSeenAt;
   private long readyAt = -1L;
   private long blockedUntil;
   private long sampledSpread = -1L;
   private long sampledMinimum = -1L;
   private boolean sampledSmooth;

   public TriggerTiming() {
      this(() -> ThreadLocalRandom.current().nextDouble());
   }

   public TriggerTiming(DoubleSupplier random) {
      this.random = random;
   }

   public boolean ready(Object candidate, long now, boolean cooldownReady, long reactionMs, long spreadMs) {
      return this.ready(candidate, now, cooldownReady, reactionMs, 0L, spreadMs, false);
   }

   public boolean ready(Object candidate, long now, boolean cooldownReady, long reactionMs, long minimumMs, long maximumMs, boolean smooth) {
      if (candidate == null) {
         if (now - this.lastSeenAt > 200L) {
            this.target = null;
            this.readyAt = -1L;
         }

         return false;
      } else {
         this.lastSeenAt = now;
         if (this.target != candidate) {
            this.target = candidate;
            this.acquiredAt = now;
            this.readyAt = -1L;
         }

         if (cooldownReady && now >= this.blockedUntil) {
            long maximum = Math.max(0L, maximumMs);
            long minimum = Math.min(maximum, Math.max(0L, minimumMs));
            long spread = maximum - minimum;
            if (this.readyAt < 0L || this.sampledSpread != spread || this.sampledMinimum != minimum || this.sampledSmooth != smooth) {
               double fraction = 0.0;
               if (spread > 0L) {
                  fraction = this.random.getAsDouble();
                  if (smooth) {
                     fraction = (fraction + this.random.getAsDouble()) * 0.5;
                  }
               }

               this.readyAt = now + minimum + Math.round(fraction * (double)spread);
               this.sampledSpread = spread;
               this.sampledMinimum = minimum;
               this.sampledSmooth = smooth;
            }

            return now >= this.readyAt && now - this.acquiredAt >= Math.max(0L, reactionMs);
         } else {
            this.readyAt = -1L;
            return false;
         }
      }
   }

   public boolean shouldMiss(double percent) {
      return percent <= 0.0 ? false : percent >= 100.0 || this.random.getAsDouble() * 100.0 < percent;
   }

   public void attempted(long now, long recoveryMs) {
      this.readyAt = -1L;
      this.blockedUntil = now + Math.max(0L, recoveryMs);
   }

   public void reset() {
      this.target = null;
      this.acquiredAt = 0L;
      this.lastSeenAt = 0L;
      this.readyAt = -1L;
      this.blockedUntil = 0L;
      this.sampledSpread = -1L;
      this.sampledMinimum = -1L;
      this.sampledSmooth = false;
   }
}
