package ru.mentions.mixin;

import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_1309.class})
public interface LivingEntityAccessor {
   @Accessor("field_6228")
   int mentions$getJumpingCooldown();

   @Accessor("field_6282")
   boolean mentions$isJumping();
}
