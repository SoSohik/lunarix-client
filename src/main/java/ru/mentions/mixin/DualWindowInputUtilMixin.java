package ru.mentions.mixin;

import net.minecraft.class_1041;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.window.DualWindowInput;
import ru.mentions.window.DualWindowService;

@Mixin(
   value = {class_3675.class},
   priority = 3000
)
public abstract class DualWindowInputUtilMixin {
   @Inject(
      method = {"method_15987"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void dualWindow$poll(class_1041 window, int key, CallbackInfoReturnable<Boolean> cir) {
      if (DualWindowInput.installed() && window.method_4490() == DualWindowService.originalHandle()) {
         DualWindowInput.tick();
         long owner = DualWindowInput.ownerHandle();
         cir.setReturnValue(owner != 0L && !DualWindowService.isCleanPass() && key >= 32 && key <= 348 && GLFW.glfwGetKey(owner, key) == 1);
      }
   }

   @Inject(
      method = {"method_15984"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void dualWindow$cursor(class_1041 window, int mode, double x, double y, CallbackInfo ci) {
      if (DualWindowInput.installed() && window.method_4490() == DualWindowService.originalHandle()) {
         if (!DualWindowService.isCleanPass()) {
            DualWindowInput.cursor(mode, x, y);
         }

         ci.cancel();
      }
   }

   @Inject(
      method = {"method_21736"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void dualWindow$raw(class_1041 window, boolean enabled, CallbackInfo ci) {
      if (DualWindowInput.installed() && window.method_4490() == DualWindowService.originalHandle()) {
         if (!DualWindowService.isCleanPass()) {
            DualWindowInput.raw(enabled);
         }

         ci.cancel();
      }
   }
}
