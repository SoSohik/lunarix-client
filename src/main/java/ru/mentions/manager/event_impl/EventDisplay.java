package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class EventDisplay extends Event {
   private class_332 drawContext;
   private float partialTicks;

   @Generated
   public class_332 getDrawContext() {
      return this.drawContext;
   }

   @Generated
   public float getPartialTicks() {
      return this.partialTicks;
   }

   @Generated
   public EventDisplay(class_332 drawContext, float partialTicks) {
      this.drawContext = drawContext;
      this.partialTicks = partialTicks;
   }
}
