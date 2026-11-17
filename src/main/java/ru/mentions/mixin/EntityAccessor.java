package ru.mentions.mixin;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_3611;
import net.minecraft.class_4048;
import net.minecraft.class_6862;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_1297.class})
public interface EntityAccessor {
   @Accessor("field_5964")
   Object2DoubleMap<class_6862<class_3611>> mentions$getFluidHeight();

   @Accessor("field_25599")
   Set<class_6862<class_3611>> mentions$getSubmergedFluidTag();

   @Accessor("field_18065")
   class_4048 mentions$getDimensions();

   @Accessor("field_5986")
   int mentions$getId();
}
