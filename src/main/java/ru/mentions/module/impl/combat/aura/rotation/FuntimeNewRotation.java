package ru.mentions.module.impl.combat.aura.rotation;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.aura.GCDUtil;

@Environment(EnvType.CLIENT)
public class FuntimeNewRotation implements RotationAura {
   private static final FuntimeNewRotation.Easing[] EASING_SEQUENCE = FuntimeNewRotation.Easing.values();
   private float currentYaw = 0.0F;
   private float currentPitch = 0.0F;
   private boolean initialized = false;
   private int activeEaseIndex = 0;
   private int nextEaseIndex = 1;
   private long easeSwitchTimer = 0L;
   private long nextEaseSwitchTime = 0L;
   private final double[] pastDeltaPitch = new double[5];
   private double floatTargetX = 0.0;
   private double floatTargetY = 0.5;
   private double floatTargetZ = 0.0;
   private long lastPointShiftTime = 0L;
   private int overshootTicks = 0;
   private float overshootYaw = 0.0F;
   private float overshootPitch = 0.0F;

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (target != null && mc.field_1724 != null && mc.field_1687 != null) {
         if (!this.initialized) {
            this.currentYaw = mc.field_1724.method_36454();
            this.currentPitch = mc.field_1724.method_36455();
            this.initialized = true;
         }

         long now = System.currentTimeMillis();
         class_238 box = target.method_5829();
         double halfW = (box.field_1320 - box.field_1323) * 0.5;
         double height = (double)target.method_17682();
         if (canAttack || now - this.lastPointShiftTime > 180L) {
            this.lastPointShiftTime = now;
            this.floatTargetX = ThreadLocalRandom.current().nextDouble(-halfW * 0.65, halfW * 0.65);
            this.floatTargetY = ThreadLocalRandom.current().nextDouble(height * 0.3, height * 0.78);
            this.floatTargetZ = ThreadLocalRandom.current().nextDouble(-halfW * 0.65, halfW * 0.65);
         }

         double lissajousX = Math.sin((double)now / 290.0) * halfW * 0.25;
         double lissajousY = Math.sin((double)now / 430.0) * height * 0.12;
         double lissajousZ = Math.cos((double)now / 350.0) * halfW * 0.25;
         double targetHitX = class_3532.method_15350(target.method_23317() + this.floatTargetX + lissajousX, box.field_1323 + 0.05, box.field_1320 - 0.05);
         double targetHitY = class_3532.method_15350(target.method_23318() + this.floatTargetY + lissajousY, box.field_1322 + 0.12, box.field_1325 - 0.1);
         double targetHitZ = class_3532.method_15350(target.method_23321() + this.floatTargetZ + lissajousZ, box.field_1321 + 0.05, box.field_1324 - 0.05);
         class_243 eyePos = mc.field_1724.method_33571();
         class_243 aimPos = new class_243(targetHitX, targetHitY, targetHitZ);
         class_243 diff = aimPos.method_1020(eyePos);
         float targetYaw = (float)Math.toDegrees(Math.atan2(-diff.field_1352, diff.field_1350));
         float targetPitch = (float)class_3532.method_15350(
            -Math.toDegrees(Math.atan2(diff.field_1351, Math.hypot(diff.field_1352, diff.field_1350))), -90.0, 90.0
         );
         float yawDelta = class_3532.method_15393(targetYaw - this.currentYaw);
         float pitchDelta = targetPitch - this.currentPitch;
         float totalDelta = (float)Math.hypot((double)yawDelta, (double)pitchDelta);
         if (!(totalDelta < 0.18F)) {
            if (now >= this.nextEaseSwitchTime) {
               this.activeEaseIndex = this.nextEaseIndex;
               this.nextEaseIndex = ThreadLocalRandom.current().nextInt(EASING_SEQUENCE.length);
               this.easeSwitchTimer = ThreadLocalRandom.current().nextLong(600L, 1600L);
               this.nextEaseSwitchTime = now + this.easeSwitchTimer;
            }

            float normalizedDelta = class_3532.method_15363(totalDelta / 50.0F, 0.05F, 1.0F);
            double easeMultiplier = EASING_SEQUENCE[this.activeEaseIndex].ease((double)normalizedDelta);
            easeMultiplier = Math.max(0.2, Math.min(1.85, easeMultiplier));
            double distance = eyePos.method_1022(aimPos);
            float distanceSpeedMultiplier = 1.0F;
            if (distance < 2.2) {
               distanceSpeedMultiplier = (float)(0.65 + 0.25 * (distance / 2.2));
            } else if (distance <= 3.8) {
               distanceSpeedMultiplier = (float)(1.0 + 0.35 * (1.0 - Math.abs(distance - 2.9) / 1.0));
            } else {
               distanceSpeedMultiplier = (float)Math.max(0.6, 1.0 - (distance - 3.8) * 0.16);
            }

            double playerSpeed = mc.field_1724.method_18798().method_37267();
            float playerSpeedFactor = 1.0F + (float)Math.min(0.35, playerSpeed * 1.4);
            double fallDistance = mc.field_1724.field_6017;
            float fallFactor = fallDistance > 0.05 ? 1.08F : 1.0F;
            float swingProgress = mc.field_1724.field_6251;
            float swingFactor = 1.0F + swingProgress * 0.14F;
            float baseYawSpeed = ThreadLocalRandom.current().nextFloat(48.0F, 66.0F);
            float basePitchSpeed = ThreadLocalRandom.current().nextFloat(20.0F, 32.0F);
            float yawSpeed = baseYawSpeed * (float)easeMultiplier * distanceSpeedMultiplier * playerSpeedFactor * fallFactor * swingFactor;
            float pitchSpeed = basePitchSpeed * (float)easeMultiplier * distanceSpeedMultiplier * playerSpeedFactor * fallFactor * swingFactor;
            if (this.overshootTicks > 0) {
               this.overshootTicks--;
            } else if (totalDelta > 16.0F && ThreadLocalRandom.current().nextInt(22) == 0) {
               this.overshootTicks = ThreadLocalRandom.current().nextInt(2, 5);
               this.overshootYaw = (float)ThreadLocalRandom.current().nextDouble(-2.2, 2.2);
               this.overshootPitch = (float)ThreadLocalRandom.current().nextDouble(-1.1, 1.1);
            }

            double arPitchHistory = 0.45 * this.pastDeltaPitch[0] + 0.25 * this.pastDeltaPitch[1] + 0.12 * this.pastDeltaPitch[2];
            double gaussianMicroNoise = ThreadLocalRandom.current().nextGaussian() * 0.035;
            float sineShakeYaw = (float)(Math.sin((double)now / 41.0) * 0.16 + Math.cos((double)now / 83.0) * 0.11);
            float sineShakePitch = (float)(
               Math.cos((double)now / 49.0) * 0.12 + Math.sin((double)now / 97.0) * 0.08 + arPitchHistory * 0.08 + gaussianMicroNoise
            );
            if (ThreadLocalRandom.current().nextInt(35) == 0) {
               sineShakeYaw += (float)ThreadLocalRandom.current().nextDouble(-0.45, 0.45);
               sineShakePitch += (float)ThreadLocalRandom.current().nextDouble(-0.35, 0.35);
            }

            float clampedYawStep = class_3532.method_15363(yawDelta, -yawSpeed, yawSpeed);
            float clampedPitchStep = class_3532.method_15363(pitchDelta, -pitchSpeed, pitchSpeed);
            float nextYaw = this.currentYaw + clampedYawStep + sineShakeYaw + (this.overshootTicks > 0 ? this.overshootYaw : 0.0F);
            float nextPitch = class_3532.method_15363(
               this.currentPitch + clampedPitchStep + sineShakePitch + (this.overshootTicks > 0 ? this.overshootPitch : 0.0F), -90.0F, 90.0F
            );
            nextYaw = this.currentYaw + GCDUtil.getSensitivity(nextYaw - this.currentYaw);
            nextPitch = this.currentPitch + GCDUtil.getSensitivity(nextPitch - this.currentPitch);
            this.pastDeltaPitch[4] = this.pastDeltaPitch[3];
            this.pastDeltaPitch[3] = this.pastDeltaPitch[2];
            this.pastDeltaPitch[2] = this.pastDeltaPitch[1];
            this.pastDeltaPitch[1] = this.pastDeltaPitch[0];
            this.pastDeltaPitch[0] = (double)(nextPitch - this.currentPitch);
            this.currentYaw = nextYaw;
            this.currentPitch = nextPitch;
            AttackAura.lastYaw = nextYaw;
            AttackAura.lastPitch = nextPitch;
            RotationProcess.update(new Rotation(nextYaw, nextPitch), yawSpeed, pitchSpeed, yawSpeed, pitchSpeed, 1, 6, false);
         }
      }
   }

   public static enum Easing {
      EASE_IN_SINE {
         @Override
         public double ease(double t) {
            return 1.0 - Math.cos(t * Math.PI / 2.0);
         }
      },
      EASE_OUT_SINE {
         @Override
         public double ease(double t) {
            return Math.sin(t * Math.PI / 2.0);
         }
      },
      EASE_IN_OUT_SINE {
         @Override
         public double ease(double t) {
            return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
         }
      },
      EASE_IN_QUAD {
         @Override
         public double ease(double t) {
            return t * t;
         }
      },
      EASE_OUT_QUAD {
         @Override
         public double ease(double t) {
            return 1.0 - (1.0 - t) * (1.0 - t);
         }
      },
      EASE_IN_OUT_QUAD {
         @Override
         public double ease(double t) {
            return t < 0.5 ? 2.0 * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 2.0) / 2.0;
         }
      },
      EASE_IN_CUBIC {
         @Override
         public double ease(double t) {
            return t * t * t;
         }
      },
      EASE_OUT_CUBIC {
         @Override
         public double ease(double t) {
            return 1.0 - Math.pow(1.0 - t, 3.0);
         }
      },
      EASE_IN_OUT_CUBIC {
         @Override
         public double ease(double t) {
            return t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
         }
      },
      EASE_IN_QUART {
         @Override
         public double ease(double t) {
            return t * t * t * t;
         }
      },
      EASE_OUT_QUART {
         @Override
         public double ease(double t) {
            return 1.0 - Math.pow(1.0 - t, 4.0);
         }
      },
      EASE_IN_OUT_QUART {
         @Override
         public double ease(double t) {
            return t < 0.5 ? 8.0 * t * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 4.0) / 2.0;
         }
      },
      EASE_IN_QUINT {
         @Override
         public double ease(double t) {
            return t * t * t * t * t;
         }
      },
      EASE_OUT_QUINT {
         @Override
         public double ease(double t) {
            return 1.0 - Math.pow(1.0 - t, 5.0);
         }
      },
      EASE_IN_OUT_QUINT {
         @Override
         public double ease(double t) {
            return t < 0.5 ? 16.0 * t * t * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 5.0) / 2.0;
         }
      },
      EASE_IN_EXPO {
         @Override
         public double ease(double t) {
            return t <= 0.0 ? 0.0 : Math.pow(2.0, 10.0 * (t - 1.0));
         }
      },
      EASE_OUT_EXPO {
         @Override
         public double ease(double t) {
            return t >= 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * t);
         }
      },
      EASE_IN_OUT_EXPO {
         @Override
         public double ease(double t) {
            if (t <= 0.0) {
               return 0.0;
            } else if (t >= 1.0) {
               return 1.0;
            } else {
               return t < 0.5 ? Math.pow(2.0, 20.0 * t - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * t + 10.0)) / 2.0;
            }
         }
      },
      EASE_IN_CIRC {
         @Override
         public double ease(double t) {
            return 1.0 - Math.sqrt(1.0 - Math.pow(t, 2.0));
         }
      },
      EASE_OUT_CIRC {
         @Override
         public double ease(double t) {
            return Math.sqrt(1.0 - Math.pow(t - 1.0, 2.0));
         }
      },
      EASE_IN_OUT_CIRC {
         @Override
         public double ease(double t) {
            return t < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * t, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * t + 2.0, 2.0)) + 1.0) / 2.0;
         }
      },
      EASE_IN_BACK {
         @Override
         public double ease(double t) {
            double c1 = 1.70158;
            double c3 = c1 + 1.0;
            return c3 * t * t * t - c1 * t * t;
         }
      },
      EASE_OUT_BACK {
         @Override
         public double ease(double t) {
            double c1 = 1.70158;
            double c3 = c1 + 1.0;
            return 1.0 + c3 * Math.pow(t - 1.0, 3.0) + c1 * Math.pow(t - 1.0, 2.0);
         }
      },
      EASE_IN_OUT_BACK {
         @Override
         public double ease(double t) {
            double c1 = 1.70158;
            double c2 = c1 * 1.525;
            return t < 0.5
               ? Math.pow(2.0 * t, 2.0) * ((c2 + 1.0) * 2.0 * t - c2) / 2.0
               : (Math.pow(2.0 * t - 2.0, 2.0) * ((c2 + 1.0) * (t * 2.0 - 2.0) + c2) + 2.0) / 2.0;
         }
      },
      EASE_IN_ELASTIC {
         @Override
         public double ease(double t) {
            if (t <= 0.0) {
               return 0.0;
            } else if (t >= 1.0) {
               return 1.0;
            } else {
               double c4 = Math.PI * 2.0 / 3.0;
               return -Math.pow(2.0, 10.0 * t - 10.0) * Math.sin((t * 10.0 - 10.75) * c4);
            }
         }
      },
      EASE_OUT_ELASTIC {
         @Override
         public double ease(double t) {
            if (t <= 0.0) {
               return 0.0;
            } else if (t >= 1.0) {
               return 1.0;
            } else {
               double c4 = Math.PI * 2.0 / 3.0;
               return Math.pow(2.0, -10.0 * t) * Math.sin((t * 10.0 - 0.75) * c4) + 1.0;
            }
         }
      },
      EASE_IN_OUT_ELASTIC {
         @Override
         public double ease(double t) {
            if (t <= 0.0) {
               return 0.0;
            } else if (t >= 1.0) {
               return 1.0;
            } else {
               double c5 = Math.PI * 4.0 / 9.0;
               return t < 0.5
                  ? -(Math.pow(2.0, 20.0 * t - 10.0) * Math.sin((20.0 * t - 11.125) * c5)) / 2.0
                  : Math.pow(2.0, -20.0 * t + 10.0) * Math.sin((20.0 * t - 11.125) * c5) / 2.0 + 1.0;
            }
         }
      },
      EASE_OUT_BOUNCE {
         @Override
         public double ease(double t) {
            double n1 = 7.5625;
            double d1 = 2.75;
            if (t < 1.0 / d1) {
               return n1 * t * t;
            } else if (t < 2.0 / d1) {
               double var9;
               return n1 * (var9 = t - 1.5 / d1) * var9 + 0.75;
            } else {
               double var7;
               double var8;
               return t < 2.5 / d1 ? n1 * (var7 = t - 2.25 / d1) * var7 + 0.9375 : n1 * (var8 = t - 2.625 / d1) * var8 + 0.984375;
            }
         }
      },
      EASE_IN_BOUNCE {
         @Override
         public double ease(double t) {
            return 1.0 - EASE_OUT_BOUNCE.ease(1.0 - t);
         }
      },
      EASE_IN_OUT_BOUNCE {
         @Override
         public double ease(double t) {
            return t < 0.5 ? (1.0 - EASE_OUT_BOUNCE.ease(1.0 - 2.0 * t)) / 2.0 : (1.0 + EASE_OUT_BOUNCE.ease(2.0 * t - 1.0)) / 2.0;
         }
      };

      public abstract double ease(double var1);
   }
}
