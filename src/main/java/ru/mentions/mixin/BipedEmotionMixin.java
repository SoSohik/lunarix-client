package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10034;
import net.minecraft.class_572;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.module.impl.render.Emotions;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_572.class})
public class BipedEmotionMixin {
   @Inject(
      method = {"method_17087"},
      at = {@At("TAIL")},
      require = 0
   )
   private void mentionEmotionPose(class_10034 state, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         Emotions module = Client.get().moduleManager().get(Emotions.class);
         if (module != null) {
            module.applyModelAnimation((class_572<?>) (Object) this, state);
         }
      }
   }
}
