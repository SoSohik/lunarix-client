package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10439;
import net.minecraft.class_10442;
import net.minecraft.class_10444;
import net.minecraft.class_11566;
import net.minecraft.class_1799;
import net.minecraft.class_638;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.mentions.module.impl.render.ItemReplacer;

@Environment(EnvType.CLIENT)
@Mixin({class_10442.class})
public class ItemReplacerMixin {
   @Redirect(
      method = {"method_65596"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_10439;method_65584(Lnet/minecraft/class_10444;Lnet/minecraft/class_1799;Lnet/minecraft/class_10442;Lnet/minecraft/class_811;Lnet/minecraft/class_638;Lnet/minecraft/class_11566;I)V"
      )
   )
   private void mentions$replaceSwordModel(
      class_10439 model,
      class_10444 renderState,
      class_1799 stack,
      class_10442 resolver,
      class_811 displayContext,
      class_638 world,
      class_11566 owner,
      int seed
   ) {
      ItemReplacer module = ItemReplacer.INSTANCE;
      if (module != null) {
         stack = module.replaceModel(stack, owner, displayContext);
      }

      model.method_65584(renderState, stack, resolver, displayContext, world, owner, seed);
   }
}
