package ru.mentions.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1313;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.ActionEvent;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.MotionEvent;
import ru.mentions.manager.event_impl.MoveEvent;
import ru.mentions.manager.event_impl.UsingItemEvent;
import ru.mentions.module.impl.player.LockSlot;
import ru.mentions.module.impl.player.NoPush;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
@Mixin({class_746.class})
public abstract class ClientPlayerEntityMixin extends class_742 {
   @Shadow
   public float field_3916;
   @Shadow
   public float field_3932;
   @Shadow
   public float field_3914;
   @Shadow
   public float field_3931;

   @Inject(
      method = {"method_5773"},
      at = {@At("HEAD")}
   )
   private void onTickHead(CallbackInfo ci) {
      new EventUpdate().hook();
   }

   @Shadow
   public abstract boolean method_6115();

   public ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
      super(world, profile);
   }

   @Redirect(
      method = {"method_67270"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_241;method_35582(F)Lnet/minecraft/class_241;",
         ordinal = 1
      )
   )
   private class_241 cancelItemSlowdown(class_241 vec2f, float multiplier) {
      UsingItemEvent event = new UsingItemEvent((byte)1);
      event.hook();
      return event.isCancelled() && this.method_6115() && !this.method_5765() ? vec2f.method_35582(1.0F) : vec2f.method_35582(multiplier);
   }

   @ModifyVariable(
      method = {"method_46742"},
      at = @At(
         value = "STORE",
         ordinal = 0
      ),
      ordinal = 0
   )
   private boolean modifySprintingFlag(boolean flag) {
      ActionEvent actionEvent = new ActionEvent(flag);
      actionEvent.hook();
      return actionEvent.isSprintState();
   }

   @Inject(
      method = {"method_66282"},
      at = {@At("TAIL")}
   )
   private void fixCameraRenderAngles(CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1724 == this) {
         class_4184 camera = mc.field_1773.method_19418();
         this.field_3916 = this.field_3914 + (camera.method_19329() - this.field_3914) * 0.5F;
         this.field_3932 = this.field_3931 + (camera.method_19330() - this.field_3931) * 0.5F;
      }
   }

   @Inject(
      method = {"method_3136"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preMotion(CallbackInfo ci) {
      MotionEvent event = new MotionEvent(
         this.method_23317(), this.method_23318(), this.method_23321(), (double)this.method_5705(1.0F), (double)this.method_5695(1.0F), this.method_24828()
      );
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_30673"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
      NoPush noPush = Client.get().moduleManager().get(NoPush.class);
      if (noPush.block.getValue() && noPush.isEnabled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_7290"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      LockSlot lockSlot = Client.get().moduleManager().get(LockSlot.class);
      if (lockSlot != null && lockSlot.isEnabled()) {
         int slot = IMinecraft.mc.field_1724.method_31548().method_67532();
         if (lockSlot.isSlotLocked(slot) || lockSlot.isDropProtected(IMinecraft.mc.field_1724.method_6047(), slot)) {
            cir.setReturnValue(false);
         }
      }
   }

   @Shadow
   protected abstract void method_3148(float var1, float var2);

   @Inject(
      method = {"method_5784"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_742;method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V"
      )},
      cancellable = true
   )
   public void onMoveHook(class_1313 movementType, class_243 movement, CallbackInfo ci) {
      MoveEvent event = new MoveEvent(movement);
      event.hook();
      double d = this.method_23317();
      double e = this.method_23321();
      super.method_5784(movementType, event.getMovement());
      this.method_3148((float)(this.method_23317() - d), (float)(this.method_23321() - e));
      ci.cancel();
   }
}
