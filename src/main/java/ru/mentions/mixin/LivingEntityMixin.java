package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1292;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.manager.event_impl.EventJump;
import ru.mentions.manager.event_impl.SwingDurationEvent;
import ru.mentions.module.impl.player.NoDelay;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
@Mixin({class_1309.class})
public abstract class LivingEntityMixin implements IMinecraft {
   @Shadow
   public abstract boolean method_6059(class_6880<class_1291> var1);

   @Shadow
   @Nullable
   public abstract class_1293 method_6112(class_6880<class_1291> var1);

   @ModifyConstant(
      method = {"method_6007"},
      constant = {@Constant(
         intValue = 10
      )}
   )
   private int modifyJumpTicks(int original) {
      class_1309 self = (class_1309)this;
      NoDelay noDelay;
      return self instanceof class_746 && (noDelay = NoDelay.get()) != null && noDelay.isEnabled() && noDelay.jump.getValue()
         ? MathUtil.randomInt(0, 3)
         : original;
   }

   @Inject(
      method = {"method_6028"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void swingProgressHook(CallbackInfoReturnable<Integer> cir) {
      if (this == mc.field_1724) {
         SwingDurationEvent event = new SwingDurationEvent();
         event.hook();
         if (event.isCancelled()) {
            float animation = event.getAnimation();
            float var4;
            animation = class_1292.method_5576(mc.field_1724)
               ? animation * (float)(6 - (1 + class_1292.method_5575(mc.field_1724)))
               : (var4 = animation * (float)(this.method_6059(class_1294.field_5901) ? 6 + (1 + this.method_6112(class_1294.field_5901).method_5578()) * 2 : 6));
            cir.setReturnValue((int)animation);
         }
      }
   }

   @Inject(
      method = {"method_6043"},
      at = {@At("HEAD")}
   )
   public void jumpYo(CallbackInfo ci) {
      class_1309 self = (class_1309)this;
      if (self instanceof class_746) {
         new EventJump().hook();
      }
   }
}
