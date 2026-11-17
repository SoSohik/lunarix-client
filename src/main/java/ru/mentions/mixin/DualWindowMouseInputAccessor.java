package ru.mentions.mixin;

import java.nio.file.Path;
import java.util.List;
import net.minecraft.class_11910;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_312.class})
public interface DualWindowMouseInputAccessor {
   @Invoker("method_1601")
   void dualWindow$button(long var1, class_11910 var3, int var4);

   @Invoker("method_1600")
   void dualWindow$position(long var1, double var3, double var5);

   @Invoker("method_1598")
   void dualWindow$scroll(long var1, double var3, double var5);

   @Invoker("method_29616")
   void dualWindow$drop(long var1, List<Path> var3, int var4);
}
