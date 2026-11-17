package ru.mentions.module.impl.combat.aura;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public interface RotationAura extends IMinecraft {
   void onRotation(AttackAura var1, class_1309 var2, float[] var3, boolean var4);
}
