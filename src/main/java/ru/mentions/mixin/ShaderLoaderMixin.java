package ru.mentions.mixin;

import com.mojang.blaze3d.shaders.ShaderType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.utils.render.shader.ShaderStore;

@Environment(EnvType.CLIENT)
@Mixin(
   targets = {"net/minecraft/class_10151$class_10170"}
)
public class ShaderLoaderMixin {
   @Inject(
      method = {"method_68498"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void white$provideEmbeddedSource(class_2960 id, ShaderType type, CallbackInfoReturnable<String> cir) {
      String source = ShaderStore.getSource(id, type);
      if (source != null) {
         cir.setReturnValue(source);
      }
   }
}
