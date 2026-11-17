package ru.mentions.module.api.preview;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.screen.PreviewEditor;

@Environment(EnvType.CLIENT)
public interface ModulePreview {
   PreviewSettings previewSettings();

   default boolean isPreviewActive() {
      return !(this instanceof Module module) ? false : PreviewEditor.getInstance().isEditing(module);
   }

   void previewSpawn(PreviewContext var1);

   default boolean previewNeedsDummy() {
      return false;
   }

   default boolean previewControlsDummy() {
      return false;
   }

   default void previewStart(PreviewContext ctx) {
   }

   default void previewTick(PreviewContext ctx) {
   }

   default void previewStop() {
   }
}
