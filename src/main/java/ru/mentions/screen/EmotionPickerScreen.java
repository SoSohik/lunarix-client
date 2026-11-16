package ru.mentions.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public class EmotionPickerScreen extends class_437 implements IMinecraft {
   public EmotionPickerScreen(class_437 parent, int page, int slot) {
      super(class_2561.method_43470("Emotions"));
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }
}
