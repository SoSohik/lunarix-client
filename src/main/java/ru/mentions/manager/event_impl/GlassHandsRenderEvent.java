package ru.mentions.manager.event_impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4587;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class GlassHandsRenderEvent extends CancellableEvent {
   private final GlassHandsRenderEvent.Phase phase;
   private final class_4587 matrices;
   private final float tickDelta;

   public GlassHandsRenderEvent(GlassHandsRenderEvent.Phase phase, class_4587 matrices, float tickDelta) {
      this.phase = phase;
      this.matrices = matrices;
      this.tickDelta = tickDelta;
   }

   public GlassHandsRenderEvent.Phase getPhase() {
      return this.phase;
   }

   public class_4587 getMatrices() {
      return this.matrices;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }

   @Environment(EnvType.CLIENT)
   public static enum Phase {
      PRE,
      POST;
   }
}
