package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_823;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.mentions.module.impl.render.ItemPhysics;

@Environment(EnvType.CLIENT)
@Mixin({class_823.class})
public class ItemEntityRendererMixin {
   @Redirect(
      method = {"method_65555"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V"
      )
   )
   private static void mentions$physicsRotate(class_4587 matrices, Quaternionfc quaternion) {
      ItemPhysics mod = ItemPhysics.INSTANCE;
      if (mod != null && mod.isEnabled() && mod.layFlat.getValue()) {
         matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
         float scale = mod.scale.getValue();
         if (scale != 1.0F) {
            matrices.method_22905(scale, scale, scale);
         }
      } else {
         matrices.method_22907(quaternion);
      }
   }
}
