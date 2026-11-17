package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10042;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_4587;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.module.impl.pasted.ShowInvisibleSystem;
import ru.mentions.module.impl.render.Poprygunchik;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_922.class})
public class LivingEntityRendererScaleMixin {
   @Inject(
      method = {"method_4054"},
      at = {@At("HEAD")},
      require = 0
   )
   private void mentionSquashStretch(class_10042 state, class_4587 matrices, class_11659 queue, class_12075 cameraRenderState, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         Poprygunchik module = Client.get().moduleManager().get(Poprygunchik.class);
         if (module != null) {
            module.applySquash(state, matrices);
         }
      }
   }

   @Inject(
      method = {"method_62355"},
      at = {@At("RETURN")},
      require = 0
   )
   private void showInvis$updateRenderState(class_1309 entity, class_10042 state, float tickDelta, CallbackInfo ci) {
      ShowInvisibleSystem sis = ShowInvisibleSystem.getInstance();
      if (sis != null && sis.isEnabled()) {
         if (sis.onlyPlayers.getValue() && !(entity instanceof class_1657)) {
            return;
         }

         boolean isInvis = entity.method_5767() || entity.method_6059(class_1294.field_5914);
         if (isInvis && sis.renderModel.getValue()) {
            if (sis.mode.is("Полупрозрачный")) {
               state.field_53333 = true;
               state.field_53461 = true;
            } else {
               state.field_53333 = false;
               state.field_53461 = false;
            }
         }
      }
   }
}
