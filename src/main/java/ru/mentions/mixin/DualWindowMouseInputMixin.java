package ru.mentions.mixin;

import java.nio.file.Path;
import java.util.List;
import net.minecraft.class_11910;
import net.minecraft.class_312;
import net.minecraft.class_3540;
import net.minecraft.class_9928;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.window.DualWindowInput;

@Mixin(
   value = {class_312.class},
   priority = 3000
)
public abstract class DualWindowMouseInputMixin implements DualWindowInput.MouseState {
   @Shadow
   private boolean field_1791;
   @Shadow
   private boolean field_1790;
   @Shadow
   private boolean field_1788;
   @Shadow
   private boolean field_1784;
   @Shadow
   private class_11910 field_1780;
   @Shadow
   private int field_1781;
   @Shadow
   private int field_1796;
   @Shadow
   private double field_1789;
   @Shadow
   private double field_1787;
   @Shadow
   private double field_1792;
   @Shadow
   @Final
   private class_3540 field_1793;
   @Shadow
   @Final
   private class_3540 field_1782;
   @Shadow
   @Final
   private class_9928 field_52757;
   @Shadow
   protected int field_61507;

   @Override
   public void dualWindow$clear() {
      this.field_1791 = this.field_1790 = this.field_1788 = false;
      this.field_1780 = null;
      this.field_61507 = -1;
      ((DualWindowScrollInputAccessor)this.field_52757).dualWindow$setX(0.0);
      ((DualWindowScrollInputAccessor)this.field_52757).dualWindow$setY(0.0);
      this.field_1781 = this.field_1796 = 0;
      this.field_1789 = this.field_1787 = this.field_1792 = 0.0;
      this.field_1784 = true;
      this.field_1793.method_15428();
      this.field_1782.method_15428();
   }

   @Inject(
      method = {"method_1601"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dualWindow$buttonGuard(long w, class_11910 input, int action, CallbackInfo ci) {
      if (!DualWindowInput.acceptOriginal(w)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1600", "method_1598"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dualWindow$motionGuard(long w, double x, double y, CallbackInfo ci) {
      if (!DualWindowInput.acceptOriginal(w)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_29616"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dualWindow$dropGuard(long w, List<Path> paths, int invalid, CallbackInfo ci) {
      if (!DualWindowInput.acceptOriginal(w)) {
         ci.cancel();
      }
   }
}
