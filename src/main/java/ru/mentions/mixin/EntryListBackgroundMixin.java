package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_350;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.module.impl.utils.UnHook;
import ru.mentions.screen.MenuStyle;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_350.class})
public class EntryListBackgroundMixin {
   @Inject(
      method = {"method_57715"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentions$transparentListBackground(class_332 context, CallbackInfo ci) {
      if (this.mentions$menuBackdrop()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_57713"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentions$noListSeparators(class_332 context, CallbackInfo ci) {
      if (this.mentions$menuBackdrop()) {
         ci.cancel();
      }
   }

   @Unique
   private boolean mentions$menuBackdrop() {
      if (DualWindowService.isCleanPass()) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         if (mc == null) {
            return false;
         } else {
            return Client.get().moduleManager().get(UnHook.class).isEnabled() ? false : MenuStyle.isMenuScreen(mc.field_1755);
         }
      }
   }
}
