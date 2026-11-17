package ru.mentions.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_638.class_5271;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.mentions.module.impl.render.WorldTweaks;

@Environment(EnvType.CLIENT)
@Mixin({class_5271.class})
public abstract class ClientWorldPropertiesMixin {
   @Shadow
   private long field_24438;
   @Shadow
   private long field_24439;

   @ModifyReturnValue(
      method = {"method_217"},
      at = {@At("RETURN")}
   )
   private long hookGetTime(long original) {
      return WorldTweaks.get().isEnabled() && WorldTweaks.get().times.getValue() ? (long)(WorldTweaks.get().time.getValue() * 1000.0F) : this.field_24439;
   }
}
