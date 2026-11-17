package ru.mentions.module.impl.combat.aura.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.ai.BuiltinStyleSource;
import ru.mentions.manager.ai.StyleSource;

@Environment(EnvType.CLIENT)
public class CustomAuraRotation extends AbstractStyleRotation {
   private static final BuiltinStyleSource STYLE = new BuiltinStyleSource("Custom", 0.62F, 0.7F, 1.0F, 0.3F, 0.22F, 0.75F, 12605196L);

   @Override
   protected StyleSource styleSource() {
      return STYLE;
   }
}
