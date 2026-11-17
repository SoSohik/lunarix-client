package ru.mentions.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.mentions.window.DualWindowInput;

@Mixin(
   value = {class_310.class},
   priority = 3000
)
public abstract class DualWindowFocusInputMixin {
   @ModifyVariable(
      method = {"method_15995"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private boolean dualWindow$logicalFocus(boolean focused) {
      if (!DualWindowInput.installed()) {
         return focused;
      } else {
         DualWindowInput.tick();
         return DualWindowInput.ownerHandle() != 0L;
      }
   }
}
