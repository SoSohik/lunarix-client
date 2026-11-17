package ru.mentions.module.impl.combat.aura.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.ai.BuiltinStyleSource;
import ru.mentions.manager.ai.StyleSource;

@Environment(EnvType.CLIENT)
public class NeuroAuraRotation extends AbstractStyleRotation {
   private static final BuiltinStyleSource STYLE = new BuiltinStyleSource("Neuro", 0.92F, 0.95F, 0.45F, 0.16F, 1.0F, 0.25F, 24301L);

   @Override
   protected StyleSource styleSource() {
      return STYLE;
   }
}
