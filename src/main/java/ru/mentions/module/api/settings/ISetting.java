package ru.mentions.module.api.settings;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ISetting {
   Setting<?> setVisible(Supplier<Boolean> var1);
}
