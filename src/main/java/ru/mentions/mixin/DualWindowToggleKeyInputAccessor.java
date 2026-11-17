package ru.mentions.mixin;

import net.minecraft.class_4666;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_4666.class})
public interface DualWindowToggleKeyInputAccessor {
   @Accessor("field_62108")
   void dualWindow$restoreToggle(boolean var1);
}
