package ru.mentions.utils.annotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public interface IMinecraft {
   class_310 mc = class_310.method_1551();
}
