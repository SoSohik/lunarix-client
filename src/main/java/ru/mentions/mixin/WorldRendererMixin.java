package ru.mentions.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_276;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.module.impl.pasted.GlowEsp;
import ru.mentions.module.impl.render.NoRender;
import ru.mentions.module.impl.render.ShaderEsp;
import ru.mentions.module.impl.render.ShaderSky;
import ru.mentions.utils.render.ShaderSkyRenderer;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_761.class})
public class WorldRendererMixin {
   @Shadow
   @Nullable
   private class_276 field_53080;

   @Inject(
      method = {"method_3254"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void espCancelOutlineDraw(CallbackInfo ci) {
      ShaderEsp esp = ShaderEsp.getInstance();
      if (esp != null && esp.isEnabled() && !ShaderEsp.getAllTargets().isEmpty()) {
         ci.cancel();
      } else {
         GlowEsp glow = GlowEsp.getInstance();
         if (glow != null && glow.isEnabled() && glow.hasTargets()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_22710"},
      at = {@At("RETURN")}
   )
   private void publishWorldRenderEvent(
      class_9922 allocator,
      class_9779 tickCounter,
      boolean renderBlockOutline,
      class_4184 camera,
      Matrix4f positionMatrix,
      Matrix4f basicProjectionMatrix,
      Matrix4f projectionMatrix,
      GpuBufferSlice fogBuffer,
      Vector4f fogColor,
      boolean renderSky,
      CallbackInfo ci
   ) {
      if (!DualWindowService.isCleanPass()) {
         class_4587 stack = new class_4587();
         Matrix4f basePositionMatrix = new Matrix4f(positionMatrix);
         stack.method_34425(new Matrix4f(basePositionMatrix));
         new EventRender3D(stack, tickCounter.method_60637(true)).hook();
         ShaderEsp.getInstance().customFboReference = this.field_53080;
         if (ShaderEsp.getInstance().isEnabled()) {
            ShaderEsp.getInstance().onRender3D();
         } else {
            GlowEsp glow = GlowEsp.getInstance();
            if (glow != null && glow.isEnabled()) {
               glow.onRender3D(this.field_53080);
            }
         }

         ShaderSky shaderSky;
         if ((shaderSky = ShaderSky.getInstance()) != null && shaderSky.isEnabled() && shaderSky.mode.is("Blur")) {
            ShaderSkyRenderer.getInstance().renderSky();
         }
      }
   }

   @Inject(
      method = {"method_43788"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHasBlindnessOrDarkness(class_4184 camera, CallbackInfoReturnable<Boolean> cir) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isEnabled()) {
         if (camera.method_19331() instanceof class_1309 livingEntity) {
            boolean hasBlindness = livingEntity.method_6059(class_1294.field_5919);
            boolean hasDarkness = livingEntity.method_6059(class_1294.field_38092);
            if (noRender.ignoreZalupa.getValue() && hasBlindness && !hasDarkness) {
               cir.setReturnValue(false);
            }

            if (noRender.ignoreZalupa.getValue() && hasDarkness && !hasBlindness) {
               cir.setReturnValue(false);
            }

            if (noRender.ignoreZalupa.getValue()) {
               cir.setReturnValue(false);
            }
         }
      }
   }
}
