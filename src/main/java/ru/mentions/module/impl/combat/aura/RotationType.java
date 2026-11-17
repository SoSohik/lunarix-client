package ru.mentions.module.impl.combat.aura;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.impl.combat.aura.rotation.CustomAuraRotation;
import ru.mentions.module.impl.combat.aura.rotation.FocusedDeRotation;
import ru.mentions.module.impl.combat.aura.rotation.FunTimeBypassRotation;
import ru.mentions.module.impl.combat.aura.rotation.FunTimeRotation;
import ru.mentions.module.impl.combat.aura.rotation.FuntimeNewRotation;
import ru.mentions.module.impl.combat.aura.rotation.HvHRotation;
import ru.mentions.module.impl.combat.aura.rotation.MatrixRotation;
import ru.mentions.module.impl.combat.aura.rotation.NeuralDeRotation;
import ru.mentions.module.impl.combat.aura.rotation.NeuroAuraRotation;
import ru.mentions.module.impl.combat.aura.rotation.NeuroRotation;
import ru.mentions.module.impl.combat.aura.rotation.NewDeRotation;
import ru.mentions.module.impl.combat.aura.rotation.PolarDeRotation;
import ru.mentions.module.impl.combat.aura.rotation.ReallyWorldRotation;
import ru.mentions.module.impl.combat.aura.rotation.ShardACRotation;
import ru.mentions.module.impl.combat.aura.rotation.SnapRotation;
import ru.mentions.module.impl.combat.aura.rotation.SpaceTimeRotation;
import ru.mentions.module.impl.combat.aura.rotation.SpookyTimeLimitRotation;
import ru.mentions.module.impl.combat.aura.rotation.SpookyTimeNewRotation;
import ru.mentions.module.impl.combat.aura.rotation.SpookyTimeRotation;
import ru.mentions.module.impl.combat.aura.rotation.StyleDeRotation;

@Environment(EnvType.CLIENT)
public enum RotationType {
   SPOOKYTIME_NEW("SpookyTime-new", new SpookyTimeNewRotation()),
   FUNTIME_NEW("Funtime-new", new FuntimeNewRotation()),
   FUNTIME_BYPASS("FunTime-bypass", new FunTimeBypassRotation()),
   FUNTIME("FunTime_Legacy", new FunTimeRotation()),
   SPOOKYTIME("Spooky Duels", new SpookyTimeRotation()),
   MATRIX("Default", new MatrixRotation()),
   SNAP("Snap", new SnapRotation()),
   NEURO("Neuro", new NeuroRotation()),
   NEUROAURA("Neuro Aura", new NeuroAuraRotation()),
   CUSTOMAURA("Custom Aura", new CustomAuraRotation()),
   HVH("HvH", new HvHRotation()),
   REALLYWORLD("ReallyWorld", new ReallyWorldRotation()),
   SPACETIME("Space Time", new SpaceTimeRotation()),
   SPOOKYLIMIT("SpookyTime Limit", new SpookyTimeLimitRotation()),
   SPOOKY("SpookyTime 1.21", new MatrixRotation()),
   FOCUSED_DE("Focused-de", new FocusedDeRotation()),
   POLAR_DE("Polar-de", new PolarDeRotation()),
   STYLE_DE("Style-de", new StyleDeRotation()),
   NEW_DE("New-de", new NewDeRotation()),
   NEURAL_DE("Neural-de", new NeuralDeRotation()),
   SHARD_AC("Shard AC", new ShardACRotation());

   private final String name;
   private final RotationAura rotation;

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public RotationAura getRotation() {
      return this.rotation;
   }

   @Generated
   private RotationType(String name, RotationAura rotation) {
      this.name = name;
      this.rotation = rotation;
   }
}
