package ru.mentions.mixin;

import net.minecraft.class_3887;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_922.class})
public interface LivingEntityRendererAccessor {
   @Invoker("method_4046")
   boolean mentions$addFeature(class_3887<?, ?> var1);
}
