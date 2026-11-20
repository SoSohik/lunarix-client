package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.utils.math.DarkUtils;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_310.class})
public class ClientMixin {
   @Inject(
      method = {"method_15993"},
      at = {@At("TAIL")}
   )
   private void applyDarkMode(CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         class_310 client = class_310.method_1551();
         DarkUtils.enableImmersiveDarkMode(client.method_22683().method_4490());
      }
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("TAIL")}
   )
   private void publishClientTick(CallbackInfo ci) {
      class_310 client = (class_310)(Object)this;
      if (!client.method_1493()) {
         class_746 player = client.field_1724;
         class_638 world = client.field_1687;
         if (player != null && world != null) {
            Client.eventHandler().post(new EventTick());
         }
      }
   }

   @Inject(
      method = {"close"},
      at = {@At("HEAD")}
   )
   private void onClientShutdown(CallbackInfo ci) {
      if (Client.get() != null && Client.get().configManager() != null) {
         Client.get().configManager().autoSave();
      }
   }

   @Inject(
      method = {"method_24287"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void getWindowTitle(CallbackInfoReturnable<String> cir) {
      if (!DualWindowService.isCleanPass()) {
         cir.setReturnValue("Lunarix Client v1.0");
      }
   }
}
