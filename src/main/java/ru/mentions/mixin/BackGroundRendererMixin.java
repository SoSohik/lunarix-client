package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_758;
import net.minecraft.class_9779;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.mentions.manager.event_impl.FogEvent;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
@Mixin({class_758.class})
public class BackGroundRendererMixin {
   @Unique
   private static final ThreadLocal<FogEvent> FOG_EVENT = new ThreadLocal<>();

   @Inject(
      method = {"method_3211"},
      at = {@At("HEAD")}
   )
   private void onApplyFogHead(
      class_4184 camera, int fogType, class_9779 tickCounter, float viewDistance, class_638 world, CallbackInfoReturnable<Vector4f> cir
   ) {
      FogEvent event = new FogEvent();
      event.hook();
      if (event.isCancelled()) {
         FOG_EVENT.set(event);
      } else {
         FOG_EVENT.remove();
      }
   }

   @ModifyArgs(
      method = {"method_3211"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
      )
   )
   private void modifyFogArgs(Args args) {
      FogEvent event = FOG_EVENT.get();
      if (event != null && event.isCancelled()) {
         int color = event.getColor();
         Vector4f fogColor = (Vector4f)args.get(2);
         fogColor.set(ColorUtil.redf(color), ColorUtil.greenf(color), ColorUtil.bluef(color), ColorUtil.alphaf(color));
         float start = 2.0F;
         float end = event.getDistance();
         args.set(3, start);
         args.set(4, end);
         args.set(7, end);
         args.set(8, end);
      }
   }

   @Inject(
      method = {"method_3211"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onApplyFogReturn(
      class_4184 camera, int fogType, class_9779 tickCounter, float viewDistance, class_638 world, CallbackInfoReturnable<Vector4f> cir
   ) {
      FogEvent event = FOG_EVENT.get();
      if (event != null && event.isCancelled()) {
         int color = event.getColor();
         cir.setReturnValue(new Vector4f(ColorUtil.redf(color), ColorUtil.greenf(color), ColorUtil.bluef(color), ColorUtil.alphaf(color)));
      }

      FOG_EVENT.remove();
   }
}
