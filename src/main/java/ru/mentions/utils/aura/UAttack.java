package ru.mentions.utils.aura;

import java.security.SecureRandom;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2868;
import net.minecraft.class_2886;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_5134;
import net.minecraft.class_2846.class_2847;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class UAttack implements IMinecraft {
   private static final SecureRandom secureRandom = new SecureRandom();
   private static final class_310 mc = class_310.method_1551();
   public static long hitCounterCPSBypass;
   private static final StopWatch cooldownTimer = new StopWatch();
   private static boolean missDetected;
   private static int counterTo0PostMissHits;

   public static void hitCounterCPSBypassNext() {
      hitCounterCPSBypass++;
   }

   public static void hitCounterCPSBypassReset() {
      hitCounterCPSBypass = 0L;
   }

   public static boolean cpsBypassTrigger() {
      return hitCounterCPSBypass % 7L == 3L;
   }

   public static class_1657 getSelf() {
      return mc.field_1724;
   }

   public static class_1937 getWorld() {
      return mc.field_1687;
   }

   public static float applyGaussianJitter(float rotation) {
      float strength = 0.2F;
      return (float)((double)rotation + (secureRandom.nextGaussian() * 0.2F * 2.0 - 0.2F));
   }

   public static boolean randomBoolean(int chance010) {
      return (float)secureRandom.nextInt(chance010 + 1) >= 1.0F * (1.0F / Math.max((float)chance010, 1.0F));
   }

   public static boolean randomBoolean() {
      return secureRandom.nextInt(2) == 1;
   }

   public static float randomFloat(float min, float max) {
      return secureRandom.nextFloat(min, max);
   }

   public static float randomFloat() {
      return randomFloat(0.0F, 1.0F);
   }

   public static int randomInt1PosibleOrNot() {
      return randomBoolean() ? 1 : -1;
   }

   public static int getAxeSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909() instanceof class_1743) {
               return i;
            }
         }

         return -1;
      }
   }

   public static Runnable[] hitShieldBreakTaskForUse(class_1309 livingIn, boolean enabled) {
      Runnable[] pre$post = new Runnable[]{() -> {
      }, () -> {
      }};
      if (enabled && mc.field_1724 != null) {
         if (livingIn instanceof class_1657 player) {
            if (!player.method_6039()) {
               return pre$post;
            }

            class_1799 main = player.method_6047();
            class_1799 off = player.method_6079();
            class_1792 mainItem = main.method_7960() ? null : main.method_7909();
            class_1792 offItem = off.method_7960() ? null : off.method_7909();
            if (mainItem == class_1802.field_8255 || offItem == class_1802.field_8255) {
               int axeSlot = getAxeSlot();
               int handSlot = mc.field_1724.method_31548().method_67532();
               if (axeSlot != -1 && axeSlot != handSlot) {
                  pre$post[0] = () -> {
                     if (mc.method_1562() != null) {
                        mc.method_1562().method_52787(new class_2868(axeSlot));
                     }
                  };
                  pre$post[1] = () -> {
                     if (mc.method_1562() != null) {
                        mc.method_1562().method_52787(new class_2868(handSlot));
                     }
                  };
               }
            }
         }

         return pre$post;
      } else {
         return pre$post;
      }
   }

   public static Runnable[] resetShieldSilentTaskForUse(boolean enabled) {
      Runnable[] pre$post = new Runnable[]{() -> {
      }, () -> {
      }};
      if (enabled && mc.field_1724 != null) {
         if (mc.field_1724.method_6039()) {
            class_1268 active = mc.field_1724.method_6058();
            if (active == null) {
               return pre$post;
            }

            pre$post[0] = () -> mc.method_1562().method_52787(new class_2846(class_2847.field_12974, class_2338.field_10980, class_2350.field_11033));
            pre$post[1] = () -> mc.method_1562().method_52787(new class_2886(active, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
         }

         return pre$post;
      } else {
         return pre$post;
      }
   }

   public static Runnable[] skipSilentSprintingTaskForUse(boolean enabled) {
      Runnable[] pre$post = new Runnable[]{() -> {
      }, () -> {
      }};
      if (enabled && mc.field_1724 != null) {
         if (mc.field_1724.method_5624() && !mc.field_1724.method_24828() && !AttackUtil.hasMovementRestrictions()) {
            pre$post[0] = () -> {
               if (AttackAura.get().typeSprint.is("Silent")) {
                  mc.field_1690.field_1867.method_23481(false);
               } else {
                  mc.field_1690.field_1867.method_23481(false);
                  mc.field_1724.method_5728(false);
               }
            };
            pre$post[1] = () -> {
               mc.field_1690.field_1867.method_23481(true);
               mc.field_1724.method_5728(true);
            };
         }

         return pre$post;
      } else {
         return pre$post;
      }
   }

   private static long critWaitStartMs = 0L;
   private static final long CRIT_WAIT_TIMEOUT_MS = 450L;

   public static boolean isBestMomentToHit(boolean fallCheck) {
      if (mc.field_1724 == null) {
         return true;
      } else if (mc.field_1724.method_6047().method_7909() == class_1802.field_49814) {
         return true;
      } else if (!fallCheck) {
         return true;
      } else if (AttackUtil.hasMovementRestrictions()) {
         critWaitStartMs = 0L;
         return true;
      } else if (AttackUtil.isPlayerInCriticalState()) {
         critWaitStartMs = 0L;
         return true;
      } else {
         boolean isSmartCrit = AttackAura.get().others.getValue("Умные криты");
         if (!isSmartCrit) {
            return false;
         }

         boolean onGround = mc.field_1724.method_24828();
         double velY = mc.field_1724.method_18798().field_1351;
         double fallDistance = mc.field_1724.field_6017;
         boolean holdingJump = mc.field_1690.field_1903.method_1434();

         if (!onGround) {
            // справжнє вікно крита: починаємо бити трохи раніше (на вершині стрибка при velY <= 0 або мінімальному fallDistance)
            boolean critWindow = (fallDistance > 0.0F || velY <= 0.005) && (velY < 0.02 || fallDistance > 0.0F);
            if (critWindow) {
               critWaitStartMs = 0L;
               return true;
            }

            // ще підйом — не витрачаємо удар, чекаємо моменту крита
            return waitForCritWindow();
         }

         // на землі: якщо затиснутий пробіл — чекаємо фазу падіння для 100% крита, не б'ємо з землі
         if (holdingJump) {
            return waitForCritWindow();
         }

         critWaitStartMs = 0L;
         return true;
      }
   }

   private static boolean waitForCritWindow() {
      long now = System.currentTimeMillis();
      if (critWaitStartMs == 0L) {
         critWaitStartMs = now;
      }

      if (now - critWaitStartMs >= CRIT_WAIT_TIMEOUT_MS) {
         critWaitStartMs = 0L;
         return true;
      }

      return false;
   }

   public static boolean useEntity(class_1309 livingIn, Runnable preHit, Runnable postHit, class_1268 hand, boolean cpsBypass) {
      if (preHit != null) {
         preHit.run();
      }

      if (livingIn != null && mc.field_1761 != null && mc.field_1724 != null) {
         mc.field_1761.method_2918(mc.field_1724, livingIn);
         if (hand != null) {
            mc.field_1724.method_6104(hand);
         }

         if (cpsBypass) {
            hitCounterCPSBypassNext();
         } else {
            hitCounterCPSBypassReset();
         }

         cooldownTimer.reset();
      }

      if (postHit != null) {
         postHit.run();
      }

      return livingIn != null;
   }

   public static long getMsCooldown() {
      if (mc.field_1724 == null) {
         return 500L;
      } else {
         double attackSpeed = mc.field_1724.method_45325(class_5134.field_23723);
         float maxDeviation = 0.2F;
         long msCooldown = (long)(1.0 / attackSpeed * 1000.0 * (double)(1.0F - Math.min(maxDeviation, 1.0F)));
         if (attackSpeed == 4.0 || attackSpeed == 4.4000000059604645 || attackSpeed == 4.800000011920929) {
            msCooldown = 450L;
         }

         return Math.max(msCooldown, 450L);
      }
   }

   public static boolean msCooldownReached(long msOffset) {
      return cooldownTimer.finished((double)(getMsCooldown() + msOffset));
   }

   public static boolean msCooldownReached() {
      return msCooldownReached(0L);
   }

   public static boolean msCooldownHasMs(long ms) {
      return cooldownTimer.finished((double)ms);
   }

   public static float msCooldownPC01() {
      return Math.min((float)cooldownTimer.elapsedTime() / (float)getMsCooldown(), 1.0F);
   }

   public static float msCooldownReach() {
      return (float)cooldownTimer.elapsedTime();
   }

   public static boolean anyEntityOnRay(class_1309 livingIn, double range) {
      return livingIn != null
         && RayTraceUtil.rayTraceEntity(class_3532.method_15393(mc.field_1724.method_36454()), mc.field_1724.method_36455(), (double)((float)range), livingIn);
   }

   public static boolean shouldAttack(class_1309 livingTarget, boolean rayCast, boolean distanceCheck, boolean fallCheck, long cooldownMSOffset, float[] ranges) {
      if (distanceCheck && livingTarget != null && !AuraUtil.validDistance(livingTarget, ranges[0], true)) {
         return false;
      } else if (!msCooldownReached(cooldownMSOffset)) {
         return false;
      } else {
         boolean validNext = isBestMomentToHit(fallCheck);
         if (validNext && rayCast && !anyEntityOnRay(livingTarget, (double)ranges[0])) {
            validNext = false;
         }

         return validNext;
      }
   }

   public static boolean shouldAttack(class_1309 livingTarget, boolean rayCast, boolean fallCheck, long cooldownMSOffset, float[] ranges) {
      return shouldAttack(livingTarget, rayCast, true, fallCheck, cooldownMSOffset, ranges);
   }

   public static boolean resetSprintTickP(class_1309 targetIn, float[] ranges) {
      return targetIn != null
         && shouldAttack(targetIn, false, false, -20L, ranges)
         && !mc.field_1724.method_24828()
         && !mc.field_1724.method_5777(class_3486.field_15517)
         && mc.field_1724.method_18798().field_1351 <= 0.0030162615090425808;
   }

   public static boolean resetSprintTick(class_1309 targetIn, float[] ranges) {
      return targetIn != null
         && shouldAttack(targetIn, false, false, -50L, ranges)
         && !mc.field_1724.method_24828()
         && !mc.field_1724.method_5777(class_3486.field_15517)
         && mc.field_1724.method_18798().field_1351 <= 0.0030162615090425808;
   }

   private static int maxHitsCountOnMiss() {
      return 3;
   }

   public static void antiMissesHittingReset() {
      missDetected = false;
      counterTo0PostMissHits = 0;
   }

   public static void antiMissesHittingUpdate(class_1309 targetIn, boolean cpsBypass, boolean rayCastCheck, boolean enabled) {
      if (targetIn == null || counterTo0PostMissHits == 0 || !enabled || targetIn.field_6235 != 0) {
         antiMissesHittingReset();
      }

      if (enabled && targetIn != null && msCooldownHasMs(cpsBypassTrigger() ? 250L : 150L) && mc.field_1724.field_6252) {
         if (!missDetected && counterTo0PostMissHits == 0 && targetIn.field_6235 == 0) {
            missDetected = true;
            counterTo0PostMissHits = maxHitsCountOnMiss();
         }

         if (missDetected && counterTo0PostMissHits > 0 && targetIn != null && (!rayCastCheck || anyEntityOnRay(targetIn, 6.0)) && useEntity(targetIn, () -> {
         }, () -> {
         }, class_1268.field_5808, cpsBypass)) {
            counterTo0PostMissHits--;
         }
      }
   }

   @Generated
   private UAttack() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static StopWatch getCooldownTimer() {
      return cooldownTimer;
   }
}
