package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_2856;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.class_1066$6")
public abstract class ServerResourcePackBypassMixin {
   @Redirect(
      method = "method_55619",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_2535;method_10743(Lnet/minecraft/class_2596;)V"
      )
   )
   private void lunarix$reportPackAsLoaded(class_2535 connection, class_2596 packet) {
      if (packet instanceof class_2856 packPacket) {
         class_2856 spoofed = new class_2856(
            packPacket.comp_2163(),
            class_2856.class_2857.field_13017
         );
         connection.method_10743(spoofed);
      } else {
         connection.method_10743(packet);
      }
   }
}
