package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2547;
import net.minecraft.class_2596;
import net.minecraft.class_2815;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.ScreenCloseEvent;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
@Mixin({class_2535.class})
public class ClientConnectionMixin {
   @Unique
   private static boolean stackOverflowFix;

   @Inject(
      method = {"method_10759"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static <T extends class_2547> void triggerReceivePacketEvent(class_2596<T> packet, class_2547 listener, CallbackInfo ci) {
      EventPacket event = new EventPacket(packet, EventPacket.Type.RECEIVE);
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_10743"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void triggerSendPacketEvent(class_2596<?> packet, CallbackInfo ci) {
      if (!stackOverflowFix) {
         EventPacket event = new EventPacket(packet, EventPacket.Type.SEND);
         event.hook();
         if (event.isCancelled()) {
            ci.cancel();
         }

         class_2596<?> newPacket;
         if ((newPacket = event.getPacket()) != packet) {
            ci.cancel();
            stackOverflowFix = true;
            IMinecraft.mc.method_1562().method_52787(newPacket);
            stackOverflowFix = false;
         }

         if (packet instanceof class_2815 closePacket) {
            class_310 mc = class_310.method_1551();
            ScreenCloseEvent screenCloseEvent = new ScreenCloseEvent(mc.field_1755, closePacket.method_36168());
            screenCloseEvent.hook();
            if (screenCloseEvent.isCancelled()) {
               ci.cancel();
            }
         }
      }
   }
}
