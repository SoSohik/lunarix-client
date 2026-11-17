package ru.mentions.mixin;

import java.util.Map;
import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_304.class})
public interface DualWindowKeyBindingInputAccessor {
   @Accessor("field_1657")
   static Map<String, class_304> dualWindow$bindings() {
      throw new AssertionError("Mixin not applied");
   }

   @Accessor("field_1653")
   void dualWindow$pressed(boolean var1);

   @Accessor("field_1661")
   void dualWindow$presses(int var1);
}
