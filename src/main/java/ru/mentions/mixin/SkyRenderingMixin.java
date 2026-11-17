package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12131;
import net.minecraft.class_4587;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.pasted.CustomSky;
import ru.mentions.module.impl.render.ShaderSky;
import ru.mentions.utils.render.ShaderSkyRenderer;

@Environment(EnvType.CLIENT)
@Mixin({class_9975.class})
public class SkyRenderingMixin {
   private static boolean shaderSkyHidesVanilla() {
      ShaderSky sky = ShaderSky.getInstance();
      if (sky != null && sky.isEnabled() && sky.mode.getValue() != null && !"Blur".equals(sky.mode.getValue()) && !sky.vanillaSky.getValue()) {
         return true;
      } else {
         CustomSky customSky = CustomSky.getInstance();
         return customSky != null && customSky.isEnabled() && customSky.vanillaSky.getValue();
      }
   }

   @Inject(
      method = {"method_62302"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void onRenderTopSkyHead(int arg, CallbackInfo ci) {
      ShaderSky sky = ShaderSky.getInstance();
      if (sky != null && sky.isEnabled() && sky.mode.getValue() != null && !"Blur".equals(sky.mode.getValue())) {
         if (!sky.vanillaSky.getValue()) {
            ShaderSkyRenderer.getInstance().renderCelestialShader();
            ci.cancel();
         }
      } else {
         CustomSky customSky = CustomSky.getInstance();
         if (customSky != null && customSky.isEnabled() && customSky.vanillaSky.getValue()) {
            ShaderSkyRenderer.getInstance().renderCelestialShader();
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_62302"},
      at = {@At("RETURN")},
      require = 0
   )
   private void onRenderTopSkyTail(int arg, CallbackInfo ci) {
      ShaderSky sky = ShaderSky.getInstance();
      if (sky != null && sky.isEnabled() && sky.mode.getValue() != null && !"Blur".equals(sky.mode.getValue())) {
         if (sky.vanillaSky.getValue()) {
            ShaderSkyRenderer.getInstance().renderCelestialShader();
         }
      } else {
         CustomSky customSky = CustomSky.getInstance();
         if (customSky != null && customSky.isEnabled() && !customSky.vanillaSky.getValue()) {
            ShaderSkyRenderer.getInstance().renderCelestialShader();
         }
      }
   }

   @Inject(
      method = {"method_62305"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void hideBottomSky(CallbackInfo ci) {
      if (shaderSkyHidesVanilla()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_62307"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void hideCelestial(class_4587 matrices, float a, float b, float c, class_12131 state, float d, float e, CallbackInfo ci) {
      if (shaderSkyHidesVanilla()) {
         ci.cancel();
      }
   }
}
