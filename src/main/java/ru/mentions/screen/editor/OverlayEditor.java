package ru.mentions.screen.editor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface OverlayEditor {
   boolean isActive();

   void render(float var1, float var2, float var3, float var4, float var5);

   boolean mouseClicked(float var1, float var2, int var3);

   boolean mouseReleased(int var1);

   boolean mouseScrolled(float var1, float var2, double var3);

   void saveAndExit();

   void closeFromMenuRemoval();

   default void rawMouseButton(float mouseX, float mouseY, int button, boolean pressed) {
   }

   default void rawMouseMoved(float mouseX, float mouseY) {
   }

   default boolean mouseDragged(float deltaX, float deltaY) {
      return this.isActive();
   }
}
