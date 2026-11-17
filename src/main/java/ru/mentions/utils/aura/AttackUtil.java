package ru.mentions.utils.aura;

import java.util.function.Predicate;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_4050;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.player.SimulatedPlayer;

@Environment(EnvType.CLIENT)
public final class AttackUtil implements IMinecraft {
   private static int count = 0;

   public static void attackEntity(class_1297 entity) {
      mc.field_1761.method_2918(mc.field_1724, entity);
      mc.field_1724.method_6104(class_1268.field_5808);
      count++;
   }

   public static boolean hasMovementRestrictions() {
      return mc.field_1724.method_6059(class_1294.field_5919)
         || mc.field_1724.method_6059(class_1294.field_5902)
         || isPlayerInBlock(class_2246.field_10343)
         || mc.field_1724.method_5869()
         || mc.field_1724.method_5771()
         || mc.field_1724.method_6101()
         || !canChangeIntoPose(class_4050.field_18076) && mc.field_1724.method_18276()
         || mc.field_1724.method_31549().field_7479;
   }

   public static boolean hasPreMovementRestrictions(SimulatedPlayer simulatedPlayer) {
      return simulatedPlayer.hasStatusEffect(class_1294.field_5919)
         || simulatedPlayer.hasStatusEffect(class_1294.field_5902)
         || isBoxInBlock(simulatedPlayer.boundingBox, class_2246.field_10343)
         || simulatedPlayer.isSubmergedInWater()
         || simulatedPlayer.isInLava()
         || simulatedPlayer.isClimbing()
         || !canChangeIntoPose(class_4050.field_18076) && mc.field_1724.method_18276()
         || mc.field_1724.method_31549().field_7479;
   }

   public static boolean canChangeIntoPose(class_4050 pose) {
      return mc.field_1724
         .method_73183()
         .method_8587(mc.field_1724, mc.field_1724.method_18377(pose).method_30757(mc.field_1724.method_73189()).method_1011(1.0E-7));
   }

   public static boolean isPlayerInBlock(class_2248 block) {
      return isBoxInBlock(mc.field_1724.method_5829().method_1014(-0.001), block);
   }

   public static boolean isBoxInBlock(class_238 box, class_2248 block) {
      return isBox(box, pos -> mc.field_1687.method_8320(pos).method_26204().equals(block));
   }

   public static boolean isBox(class_238 box, Predicate<class_2338> pos) {
      return class_2338.method_29715(box).anyMatch(pos);
   }

   public static boolean isPlayerInCriticalState() {
      boolean crit = mc.field_1724.field_6017 > (double)(!ServerUtil.isCopyTime() && !ServerUtil.isFunTime() ? 0.0F : MathUtil.random(0.01F, 0.08F))
         && (mc.field_1724.field_6017 < 0.08 || !SimulatedPlayer.simulateLocalPlayer(1).onGround);
      return !mc.field_1724.method_24828() && crit;
   }

   public static boolean isPrePlayerInCriticalState(SimulatedPlayer simulatedPlayer) {
      boolean crit = simulatedPlayer.fallDistance > 0.0F && ((double)simulatedPlayer.fallDistance < 0.08 || !SimulatedPlayer.simulateLocalPlayer(2).onGround);
      return !simulatedPlayer.onGround && crit;
   }

   @Generated
   private AttackUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
