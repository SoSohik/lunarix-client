package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.module.impl.utils.UnHook;
import ru.mentions.screen.MenuStyle;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_437.class})
public class MenuScreenBackgroundMixin {
   @Inject(
      method = {"method_25420"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentions$menuBackground(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         if (MenuStyle.isMenuScreen((class_437)this)) {
            if (!Client.get().moduleManager().get(UnHook.class).isEnabled()) {
               MenuStyle.renderBackground(mouseX, mouseY);
               ci.cancel();
            }
         }
      }
   }
}
