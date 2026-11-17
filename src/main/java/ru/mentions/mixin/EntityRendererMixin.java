package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.module.impl.render.NameTag;

@Environment(EnvType.CLIENT)
@Mixin({class_897.class})
public class EntityRendererMixin<T extends class_1297, S extends class_10017> {
   @Inject(
      method = {"method_3926"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderLabelIfPresent(S state, class_4587 matrices, class_11659 queue, class_12075 cameraRenderState, CallbackInfo ci) {
      NameTag esp = NameTag.get();
      if (esp != null && esp.isEnabled()) {
         if (state.field_58171 == class_1299.field_6097 && esp.player.getValue()) {
            if (state.field_53337 == null) {
               ci.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"method_62426"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookNametag(T entity, CallbackInfoReturnable<class_2561> cir) {
      NameTag esp = NameTag.get();
      if (esp != null && esp.isEnabled()) {
         if (entity instanceof class_1657 p && esp.player.getValue()) {
            if (esp.ignoreNaked.getValue() && !NameTag.hasArmor(p)) {
               return;
            }

            cir.setReturnValue(null);
         }
      }
   }
}
