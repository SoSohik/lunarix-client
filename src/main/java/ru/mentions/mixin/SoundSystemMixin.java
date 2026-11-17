package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1113;
import net.minecraft.class_1144;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.module.impl.utils.AutoFish;

@Environment(EnvType.CLIENT)
@Mixin({class_1144.class})
public class SoundSystemMixin {
   @Inject(
      method = {"method_4873"},
      at = {@At("HEAD")}
   )
   private void autofish$onPlay(class_1113 instance, CallbackInfoReturnable<?> cir) {
      if (instance != null) {
         class_2960 id = instance.method_4775();
         if (id != null) {
            AutoFish.onSoundPlayed(id.method_12832(), instance.method_4778(), instance.method_4779(), instance.method_4784());
         }
      }
   }
}
