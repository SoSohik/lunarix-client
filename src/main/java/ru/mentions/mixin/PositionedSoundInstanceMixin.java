package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1102;
import net.minecraft.class_1109;
import net.minecraft.class_2960;
import net.minecraft.class_3419;
import net.minecraft.class_5819;
import net.minecraft.class_1113.class_1114;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.misc.Optimization;

@Environment(EnvType.CLIENT)
@Mixin({class_1109.class})
public abstract class PositionedSoundInstanceMixin extends class_1102 {
   protected PositionedSoundInstanceMixin() {
      super((class_2960)null, (class_3419)null, null);
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/class_2960;Lnet/minecraft/class_3419;FFLnet/minecraft/class_5819;ZILnet/minecraft/class_1113$class_1114;DDDZ)V"},
      at = {@At("RETURN")}
   )
   private void optimization$reduceVolume(
      class_2960 id,
      class_3419 category,
      float volume,
      float pitch,
      class_5819 random,
      boolean repeat,
      int repeatDelay,
      class_1114 attenuation,
      double x,
      double y,
      double z,
      boolean relative,
      CallbackInfo ci
   ) {
      if (id != null) {
         float factor = Optimization.soundVolumeFactor(id.toString());
         if (factor < 1.0F) {
            this.field_5442 *= factor;
         }
      }
   }
}
