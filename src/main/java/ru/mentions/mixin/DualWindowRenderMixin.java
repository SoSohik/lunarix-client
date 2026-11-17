package ru.mentions.mixin;

import net.minecraft.class_276;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.window.DualWindowService;

@Mixin({class_310.class})
public abstract class DualWindowRenderMixin implements DualWindowService.FramebufferBridge {
   @Shadow
   @Final
   @Mutable
   private class_276 field_1689;

   @Override
   public void dualWindow$setFramebuffer(class_276 framebuffer) {
      this.field_1689 = framebuffer;
   }

   @Inject(
      method = {"method_1523"},
      at = {@At("HEAD")}
   )
   private void dualWindow$frameStart(boolean tick, CallbackInfo ci) {
      DualWindowService.onFrameStart();
   }

   @Redirect(
      method = {"method_1523"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_276;method_1237()V"
      )
   )
   private void dualWindow$present(class_276 framebuffer) {
      DualWindowService.beforePresent();
      DualWindowService.presentationFramebuffer(framebuffer).method_1237();
   }

   @Inject(
      method = {"method_1523"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1041;method_15998(Lnet/minecraft/class_10219;)V"
      )}
   )
   private void dualWindow$beforeSwap(boolean tick, CallbackInfo ci) {
      DualWindowService.beforeSwap();
   }

   @Inject(
      method = {"close"},
      at = {@At("HEAD")}
   )
   private void dualWindow$shutdown(CallbackInfo ci) {
      DualWindowService.onShutdown();
   }
}
