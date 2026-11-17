package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({class_465.class})
public interface HandledScreenAccessor {
   @Accessor("field_2776")
   int getScreenX();

   @Accessor("field_2800")
   int getScreenY();
}
