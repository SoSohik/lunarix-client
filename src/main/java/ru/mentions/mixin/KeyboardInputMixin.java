package ru.mentions.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_315;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.manager.event_impl.EventMoveInput;
import ru.mentions.manager.event_impl.InputEvent;

@Environment(EnvType.CLIENT)
@Mixin({class_743.class})
public abstract class KeyboardInputMixin extends class_744 {
   @Shadow
   @Final
   private class_315 field_3902;

   @ModifyExpressionValue(
      method = {"method_3129"},
      at = {@At(
         value = "NEW",
         target = "(ZZZZZZZ)Lnet/minecraft/class_10185;"
      )}
   )
   private class_10185 tickHook(class_10185 original) {
      InputEvent event = new InputEvent(original);
      event.hook();
      return original;
   }

   @Unique
   private float abobaGetMovementMultiplier(boolean positive, boolean negative) {
      if (positive == negative) {
         return 0.0F;
      } else {
         return positive ? 1.0F : -1.0F;
      }
   }

   @Inject(
      method = {"method_3129"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_743;field_54155:Lnet/minecraft/class_10185;",
         ordinal = 0,
         shift = Shift.AFTER
      )},
      cancellable = true
   )
   public void injectInputEvent(CallbackInfo ci) {
      EventMoveInput event = new EventMoveInput(
         this.field_54155,
         this.abobaGetMovementMultiplier(this.field_54155.comp_3159(), this.field_54155.comp_3160()),
         this.abobaGetMovementMultiplier(this.field_54155.comp_3161(), this.field_54155.comp_3162())
      );
      event.hook();
      if (!event.isCancelled()) {
         float forward = event.getForward();
         float strafe = event.getStrafe();
         this.field_55868 = new class_241(strafe, forward).method_35581();
         this.field_54155 = new class_10185(
            forward > 0.0F,
            forward < 0.0F,
            strafe > 0.0F,
            strafe < 0.0F,
            this.field_3902.field_1903.method_1434(),
            this.field_3902.field_1832.method_1434(),
            this.field_3902.field_1867.method_1434()
         );
         ci.cancel();
      }
   }
}
