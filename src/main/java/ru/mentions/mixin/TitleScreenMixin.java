package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.module.impl.utils.UnHook;
import ru.mentions.screen.MainMenuScreen;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_442.class})
public class TitleScreenMixin {
   @Inject(
      method = {"method_25426"},
      at = {@At("HEAD")}
   )
   private void onInit(CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         if (!Client.get().moduleManager().get(UnHook.class).isEnabled()) {
            class_310.method_1551().method_1507(new MainMenuScreen());
         }
      }
   }
}
