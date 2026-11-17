package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_476;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventScreenKey;
import ru.mentions.module.impl.display.ClickGui;
import ru.mentions.module.impl.player.ClickHelper;
import ru.mentions.screen.MainMenuScreen;

@Environment(EnvType.CLIENT)
@Mixin({class_309.class})
public class KeyboardMixin {
   @Shadow
   @Final
   private class_310 field_1678;

   @Inject(
      method = {"method_1466"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onKey(long window, int action, class_11908 input, CallbackInfo ci) {
      if (window == this.field_1678.method_22683().method_4490()) {
         class_437 screen = this.field_1678.field_1755;
         int key = input.comp_4795();
         if (key != -1 && key != 0) {
            if (key < 0 || key > 7) {
               if (action == 1) {
                  if (ClickHelper.isActionActive()) {
                     ci.cancel();
                  } else if (screen instanceof MainMenuScreen) {
                     ClickGui menu = Client.get().moduleManager().get(ClickGui.class);
                     if (menu != null && menu.isMenuKey(key)) {
                        menu.openMenu();
                        ci.cancel();
                     }
                  } else if (screen == null) {
                     EventKey event = new EventKey(key);
                     event.hook();
                  } else {
                     if (screen instanceof class_476) {
                        EventScreenKey event = new EventScreenKey(key, screen);
                        event.hook();
                     }
                  }
               }
            }
         }
      }
   }
}
