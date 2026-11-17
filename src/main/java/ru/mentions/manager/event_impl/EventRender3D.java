package ru.mentions.manager.event_impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4587;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class EventRender3D extends Event {
   private final class_4587 matrixStack;
   private final float tickDelta;

   public EventRender3D(class_4587 matrixStack, float tickDelta) {
      this.matrixStack = matrixStack;
      this.tickDelta = tickDelta;
   }

   public class_4587 getMatrixStack() {
      return this.matrixStack;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }
}
