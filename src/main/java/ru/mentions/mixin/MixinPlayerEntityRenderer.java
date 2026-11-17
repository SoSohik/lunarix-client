package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import net.minecraft.class_1657;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.render.NameTag;
import ru.mentions.utils.render.ChinaHatFeatureRenderer;

@Environment(EnvType.CLIENT)
@Mixin({class_1007.class})
public class MixinPlayerEntityRenderer {
   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void onInit(class_5618 ctx, boolean slim, CallbackInfo ci) {
      class_1007 renderer = (class_1007)this;
      ((LivingEntityRendererAccessor)renderer).mentions$addFeature(new ChinaHatFeatureRenderer(renderer));
   }

   @Inject(
      method = {"method_62604"},
      at = {@At("TAIL")}
   )
   private void onUpdateRenderState(class_11890 player, class_10055 state, float tickDelta, CallbackInfo ci) {
      NameTag esp = NameTag.get();
      if (esp != null && esp.isEnabled() && esp.player.getValue()) {
         class_1657 p;
         if (!esp.ignoreNaked.getValue() || !(player instanceof class_1657) || NameTag.hasArmor(p = (class_1657)player)) {
            state.field_53525 = null;
            state.field_53337 = null;
         }
      }
   }
}
