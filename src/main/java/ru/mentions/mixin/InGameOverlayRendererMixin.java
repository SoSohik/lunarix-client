package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1058;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.render.NoRender;

@Environment(EnvType.CLIENT)
@Mixin({class_4603.class})
public class InGameOverlayRendererMixin {
   @Inject(
      method = {"method_23070"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderFireOverlayHook(class_4587 matrices, class_4597 vertexConsumers, class_1058 sprite, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender.isEnabled() && noRender.ignoreFire.getValue()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_23068"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderInWallOverlayHook(class_1058 sprite, class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender.isEnabled() && noRender.ignoreZalupa.getValue()) {
         ci.cancel();
      }
   }
}
