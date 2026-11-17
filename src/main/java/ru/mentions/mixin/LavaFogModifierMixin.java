package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11401;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.render.NoRender;

@Environment(EnvType.CLIENT)
@Mixin({class_11401.class})
public class LavaFogModifierMixin {
   @Inject(
      method = {"method_42591"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onApplyStartEnd(class_7285 fogData, class_4184 camera, class_638 world, float viewDistance, class_9779 tickCounter, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreLava.getValue()) {
         fogData.field_60582 = Float.MAX_VALUE;
         fogData.field_60584 = Float.MAX_VALUE;
         ci.cancel();
      }
   }
}
