package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_419;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.utils.AutoReconnect;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_437.class})
public abstract class ScreenRenderMixin {
   @Shadow
   public int field_22789;
   @Shadow
   public int field_22790;

   @Inject(
      method = {"method_25420"},
      at = {@At("TAIL")}
   )
   private void mentions$autoReconnectCountdown(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         if (this instanceof class_419) {
            AutoReconnect module = AutoReconnect.get();
            if (module != null && module.isCounting()) {
               module.renderCountdown(context, this.field_22789, this.field_22790);
            }
         }
      }
   }
}
