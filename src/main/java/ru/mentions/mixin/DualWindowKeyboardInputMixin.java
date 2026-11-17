package ru.mentions.mixin;

import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.window.DualWindowInput;

@Mixin(
   value = {class_309.class},
   priority = 3000
)
public abstract class DualWindowKeyboardInputMixin {
   @Inject(
      method = {"method_1466"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dualWindow$guardKey(long window, int action, class_11908 input, CallbackInfo ci) {
      if (!DualWindowInput.acceptOriginal(window)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1457"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dualWindow$guardCharacter(long window, class_11905 input, CallbackInfo ci) {
      if (!DualWindowInput.acceptOriginal(window)) {
         ci.cancel();
      }
   }
}
