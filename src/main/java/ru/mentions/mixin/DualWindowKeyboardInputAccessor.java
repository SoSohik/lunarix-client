package ru.mentions.mixin;

import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_309.class})
public interface DualWindowKeyboardInputAccessor {
   @Invoker("method_1466")
   void dualWindow$key(long var1, int var3, class_11908 var4);

   @Invoker("method_1457")
   void dualWindow$character(long var1, class_11905 var3);
}
