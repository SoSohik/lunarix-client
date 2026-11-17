package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_2338.class_2339;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.manager.event_impl.CameraPositionEvent;
import ru.mentions.manager.event_impl.EventRotation;
import ru.mentions.module.impl.pasted.CameraTweaksSystem;
import ru.mentions.module.impl.render.NoRender;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_4184.class})
public abstract class CameraMixin {
   @Unique
   private EventRotation mentions$rotationEvent;
   @Unique
   private float mentions$originalYaw;
   @Unique
   private float mentions$originalPitch;
   @Unique
   private boolean mentions$inverseView;
   @Shadow
   private class_243 field_18712;
   @Shadow
   @Final
   private class_2339 field_18713;

   @Inject(
      method = {"method_19321"},
      at = {@At("HEAD")}
   )
   private void onUpdateHead(class_1937 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
      if (DualWindowService.isCleanPass()) {
         this.mentions$rotationEvent = null;
      } else {
         boolean bl = this.mentions$inverseView = thirdPerson && inverseView;
         if (focusedEntity != null) {
            this.mentions$originalYaw = focusedEntity.method_5705(tickProgress);
            this.mentions$originalPitch = focusedEntity.method_5695(tickProgress);
            this.mentions$rotationEvent = new EventRotation(this.mentions$originalYaw, this.mentions$originalPitch, tickProgress);
            this.mentions$rotationEvent.hook();
         } else {
            this.mentions$rotationEvent = null;
         }
      }
   }

   @ModifyVariable(
      method = {"method_19318"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private float modifyDesiredCameraDistance(float desiredCameraDistance) {
      CameraTweaksSystem cameraTweaks = CameraTweaksSystem.getInstance();
      return cameraTweaks != null && cameraTweaks.isEnabled() ? cameraTweaks.getDistance(0.05F) : desiredCameraDistance;
   }

   @Inject(
      method = {"method_19318"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable<Float> cir) {
      NoRender noRender = NoRender.getInstance();
      CameraTweaksSystem cameraTweaks = CameraTweaksSystem.getInstance();
      boolean clipThrough = cameraTweaks != null && cameraTweaks.isEnabled() && cameraTweaks.clipThrough.getValue()
         || noRender != null && noRender.isEnabled() && noRender.noCameraClip.getValue();
      if (clipThrough) {
         float dist = cameraTweaks != null && cameraTweaks.isEnabled() ? cameraTweaks.getDistance(0.05F) : desiredCameraDistance;
         cir.setReturnValue(dist);
      }
   }

   @Shadow
   public abstract void method_19325(float var1, float var2);

   @Redirect(
      method = {"method_19321"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4184;method_19325(FF)V"
      )
   )
   private void redirectSetRotation(class_4184 instance, float yaw, float pitch) {
      if (!DualWindowService.isCleanPass()
         && this.mentions$rotationEvent != null
         && (this.mentions$rotationEvent.getYaw() != this.mentions$originalYaw || this.mentions$rotationEvent.getPitch() != this.mentions$originalPitch)) {
         if (this.mentions$inverseView) {
            this.method_19325(this.mentions$rotationEvent.getYaw() + 180.0F, -this.mentions$rotationEvent.getPitch());
         } else {
            this.method_19325(this.mentions$rotationEvent.getYaw(), this.mentions$rotationEvent.getPitch());
         }
      } else {
         this.method_19325(yaw, pitch);
      }
   }

   @Inject(
      method = {"method_19322"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void posHook(class_243 pos, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         CameraPositionEvent event = new CameraPositionEvent(pos);
         event.hook();
         this.field_18712 = pos = event.getPos();
         this.field_18713.method_10102(pos.field_1352, pos.field_1351, pos.field_1350);
         ci.cancel();
      }
   }
}
