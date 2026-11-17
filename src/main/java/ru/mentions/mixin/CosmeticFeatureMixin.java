package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.cosmetics.render.CosmeticFeatureRenderer;

@Environment(EnvType.CLIENT)
@Mixin({class_1007.class})
public class CosmeticFeatureMixin {
   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void onInit(class_5618 context, boolean slim, CallbackInfo ci) {
      class_1007 renderer = (class_1007)this;
      ((LivingEntityRendererAccessor)renderer).mentions$addFeature(new CosmeticFeatureRenderer(renderer));
   }
}
