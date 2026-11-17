package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_638;
import net.minecraft.class_638.class_5271;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
@Mixin({class_638.class})
public class ClientWorldMixin implements IMinecraft {
   @Shadow
   @Final
   private class_5271 field_24430;

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   public void initHook(CallbackInfo info) {
      new WorldLoadEvent().hook();
   }
}
