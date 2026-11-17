package ru.mentions.manager.event_impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import ru.mentions.manager.events.CancellableEvent;

@Environment(EnvType.CLIENT)
public class MousePressEvent extends CancellableEvent {
   private final int button;
   private final int action;
   private final int modifiers;
   private final class_437 screen;
   private double mouseX;
   private double mouseY;

   public MousePressEvent(int button, int action, int modifiers, class_437 screen, double mouseX, double mouseY) {
      this.button = button;
      this.action = action;
      this.modifiers = modifiers;
      this.screen = screen;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }

   public int getKey() {
      return this.button;
   }

   public double getMouseX() {
      return this.mouseX;
   }

   public double getMouseY() {
      return this.mouseY;
   }

   public int getButton() {
      return this.button;
   }

   public int getAction() {
      return this.action;
   }

   public int getModifiers() {
      return this.modifiers;
   }

   public class_437 getScreen() {
      return this.screen;
   }
}
