package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_465;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class EventScreenRender extends Event {
   private class_332 drawContext;
   private class_465<?> screen;
   private int mouseX;
   private int mouseY;

   @Generated
   public class_332 getDrawContext() {
      return this.drawContext;
   }

   @Generated
   public class_465<?> getScreen() {
      return this.screen;
   }

   @Generated
   public int getMouseX() {
      return this.mouseX;
   }

   @Generated
   public int getMouseY() {
      return this.mouseY;
   }

   @Generated
   public EventScreenRender(class_332 drawContext, class_465<?> screen, int mouseX, int mouseY) {
      this.drawContext = drawContext;
      this.screen = screen;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }
}
