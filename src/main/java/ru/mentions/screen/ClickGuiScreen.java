package ru.mentions.screen;

import net.minecraft.class_332;
import net.minecraft.class_9779;
import ru.mentions.module.api.Module;

public interface ClickGuiScreen {
   boolean isSearchActive();

   void renderOverlay(class_332 var1, class_9779 var2);

   void openCosmetics();

   void openHandsEditor();

   void openPreviewEditor(Module var1);

   void openCrosshairEditor();
}
