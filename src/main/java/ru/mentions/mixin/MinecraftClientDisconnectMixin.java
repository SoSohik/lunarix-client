package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.player.PvpSafe;

@Environment(EnvType.CLIENT)
@Mixin({class_310.class})
public class MinecraftClientDisconnectMixin {
   @Inject(
      method = {"method_72100", "method_72099"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pvpSafe$blockLeave(CallbackInfo ci) {
      if (PvpSafe.isActive()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_76797"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pvpSafe$blockLeaveTransfer(boolean transferring, CallbackInfo ci) {
      if (PvpSafe.isActive()) {
         ci.cancel();
      }
   }
}
