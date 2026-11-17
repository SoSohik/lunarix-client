package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.module.impl.render.Gamma;
import ru.mentions.module.impl.render.NoRender;

@Environment(EnvType.CLIENT)
@Mixin({class_765.class})
public class LightmapTextureManagerMixin {
   @Redirect(
      method = {"method_3313"},
      at = @At(
         value = "INVOKE",
         target = "Ljava/lang/Double;floatValue()F",
         ordinal = 1
      )
   )
   private float mentions$getGammaValue(Double instance) {
      Gamma module;
      return Client.get() != null && (module = Client.get().moduleManager().get(Gamma.class)) != null && module.isEnabled() ? 200.0F : instance.floatValue();
   }

   @Inject(
      method = {"method_42596"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void removeDarknessEffect(CallbackInfoReturnable<Float> cir) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreZalupa.getValue()) {
         cir.setReturnValue(0.0F);
      }
   }
}
