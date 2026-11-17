package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11398;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.module.impl.render.WorldTweaks;

@Environment(EnvType.CLIENT)
@Mixin({class_11398.class})
public class AtmosphericFogModifierMixin {
   @Inject(
      method = {"method_71654"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetFogColor(class_638 world, class_4184 camera, int viewDistance, float tickProgress, CallbackInfoReturnable<Integer> cir) {
      WorldTweaks worldTweaks = WorldTweaks.get();
      if (worldTweaks != null && worldTweaks.isEnabled() && worldTweaks.fogs.getValue()) {
         cir.setReturnValue(worldTweaks.getColor());
      }
   }

   @Inject(
      method = {"method_42591"},
      at = {@At("RETURN")}
   )
   private void onApplyStartEnd(class_7285 fogData, class_4184 camera, class_638 world, float viewDistance, class_9779 tickCounter, CallbackInfo ci) {
      WorldTweaks worldTweaks = WorldTweaks.get();
      if (worldTweaks != null && worldTweaks.isEnabled() && worldTweaks.fogs.getValue()) {
         float end = worldTweaks.fog.getValue();
         fogData.field_60582 = Math.min(2.0F, end * 0.25F);
         fogData.field_60584 = end;
         fogData.field_60099 = end;
         fogData.field_60100 = end;
      }
   }
}
