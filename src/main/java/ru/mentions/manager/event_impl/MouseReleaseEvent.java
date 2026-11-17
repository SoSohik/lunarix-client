package ru.mentions.manager.event_impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class MouseReleaseEvent extends Event {
   private final double mouseX;
   private final double mouseY;
   private final int button;
   private final class_437 screen;

   public MouseReleaseEvent(double mouseX, double mouseY, int button, class_437 screen) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.button = button;
      this.screen = screen;
   }

   @Generated
   public double getMouseX() {
      return this.mouseX;
   }

   @Generated
   public double getMouseY() {
      return this.mouseY;
   }

   @Generated
   public int getButton() {
      return this.button;
   }

   @Generated
   public class_437 getScreen() {
      return this.screen;
   }
}
