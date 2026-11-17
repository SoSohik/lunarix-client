package ru.mentions.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_4897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.mentions.utils.player.Spectator;

@Environment(EnvType.CLIENT)
@Mixin({class_4897.class})
public class BiomeEffectSoundPlayerMixin {
   @ModifyExpressionValue(
      method = {"method_4756"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_73189()Lnet/minecraft/class_243;"
      )},
      require = 0
   )
   private class_243 spectator$ambientPos(class_243 original) {
      class_1297 target = Spectator.getTarget();
      return target != null ? target.method_73189() : original;
   }

   @ModifyExpressionValue(
      method = {"method_75840"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_23317()D"
      )},
      require = 0
   )
   private double spectator$moodX(double original) {
      class_1297 target = Spectator.getTarget();
      return target != null ? target.method_23317() : original;
   }

   @ModifyExpressionValue(
      method = {"method_75840"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_23320()D"
      )},
      require = 0
   )
   private double spectator$moodY(double original) {
      class_1297 target = Spectator.getTarget();
      return target != null ? target.method_23320() : original;
   }

   @ModifyExpressionValue(
      method = {"method_75840"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_23321()D"
      )},
      require = 0
   )
   private double spectator$moodZ(double original) {
      class_1297 target = Spectator.getTarget();
      return target != null ? target.method_23321() : original;
   }
}
