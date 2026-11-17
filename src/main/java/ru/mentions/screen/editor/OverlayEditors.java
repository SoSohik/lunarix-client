package ru.mentions.screen.editor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.screen.CrosshairEditor;
import ru.mentions.screen.HandsEditor;
import ru.mentions.screen.PreviewEditor;

@Environment(EnvType.CLIENT)
public final class OverlayEditors {
   private static final OverlayEditor[] ALL = new OverlayEditor[]{HandsEditor.getInstance(), PreviewEditor.getInstance(), CrosshairEditor.getInstance()};

   private OverlayEditors() {
   }

   public static OverlayEditor[] all() {
      return ALL;
   }

   public static OverlayEditor active() {
      for (OverlayEditor editor : ALL) {
         if (editor.isActive()) {
            return editor;
         }
      }

      return null;
   }

   public static boolean anyActive() {
      return active() != null;
   }

   public static void closeAll() {
      for (OverlayEditor editor : all()) {
         if (editor.isActive()) {
            editor.closeFromMenuRemoval();
         }
      }
   }
}
