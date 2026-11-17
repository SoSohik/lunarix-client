package ru.mentions.module.impl.combat.aura.rotation;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.aura.RotationAura;
import ru.mentions.utils.aura.RayTraceUtil;
import ru.mentions.utils.aura.UAttack;
import ru.mentions.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public class FunTimeRotation implements RotationAura {
   private long jitterSwitchTime = ThreadLocalRandom.current().nextLong(1000L, 2001L);
   private long nextJitterSwitch = System.currentTimeMillis() + this.jitterSwitchTime;
   private int currentJitterPreset = 0;
   private int nextJitterPreset = 1;
   private long speedSwitchTime = ThreadLocalRandom.current().nextLong(2000L, 4001L);
   private long nextSpeedSwitch = System.currentTimeMillis() + this.speedSwitchTime;
   private int currentSpeedPreset = 0;
   private int nextSpeedPreset = 1;
   private static final long JITTER_SWITCH_TIME = 1000L;
   private float lastYawJitter;
   private float lastPitchJitter;
   private static final long SPEED_SWITCH_TIME = 2500L;
   private float lastYawSpeed;
   private float lastPitchSpeed;
   private final FunTimeRotation.JitterPreset[] jitterPresets = new FunTimeRotation.JitterPreset[]{new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 90.0) * 12.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 90.0) * 16.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 70.0) * 12.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 80.0) * 11.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 65.0) * 14.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 90.0) * 10.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 70.0) * 12.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 111.0) * 13.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 66.0) * 12.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 66.0) * 12.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 125.0) * 13.5);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 135.0) * 13.5);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 90.0) * 12.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 70.0) * 7.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 240.0) * 14.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 240.0) * 14.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 133.0) * 11.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 142.0) * 11.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 35.0) * 8.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 110.0) * 18.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 130.0 + 0.8) * 20.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 95.0) * 8.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 65.0 + 2.1) * 16.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 45.0) * 14.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 42.0) * 9.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 38.0) * 17.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 160.0) * 22.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 140.0) * 5.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 48.0 + 1.2) * 13.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 68.0 + 0.4) * 15.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 28.0) * 10.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 85.0) * 20.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 100.0) * 7.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 55.0) * 22.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 52.0 + 2.7) * 17.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 92.0 + 1.3) * 11.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.cos((double)t / 74.0) * 14.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.sin((double)t / 33.0) * 13.0);
      }
   }, new FunTimeRotation.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 115.0 + 0.5) * 19.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 58.0 + 2.4) * 16.0);
      }
   }};
   private final FunTimeRotation.SpeedPreset[] speedPresets = new FunTimeRotation.SpeedPreset[]{new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 40.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 12.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 55.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 15.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 30.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 10.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 48.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 18.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 60.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 14.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 35.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 16.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 50.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 13.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 44.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 17.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 58.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 11.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 32.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 19.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 46.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 12.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 53.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 15.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 37.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 18.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 42.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 13.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 57.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 16.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 34.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 11.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 49.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 20.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 62.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 14.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 39.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 17.0F;
      }
   }, new FunTimeRotation.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 52.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 12.0F;
      }
   }};

   @Override
   public void onRotation(AttackAura aura, class_1309 target, float[] ranges, boolean canAttack) {
      if (!mc.field_1724.method_5869()) {
         double orbitAngle = (double)System.currentTimeMillis() / 16.0;
         double orbitDist = 1.35 + 0.35 * Math.sin(orbitAngle * 0.7);
         class_243 orbitPoint = new class_243(
            mc.field_1724.method_23317() + Math.cos(orbitAngle) * orbitDist,
            target.method_23318() + (double)target.method_17682() * 0.55 + 0.22 * Math.sin(orbitAngle * 1.7),
            mc.field_1724.method_23321() + Math.sin(orbitAngle) * orbitDist
         );
         if (RayTraceUtil.rayTraceEntity(mc.field_1724.method_36454(), mc.field_1724.method_36455(), 6.0, target)) {
            orbitAngle += 0.9;
         }

         class_243 vec = orbitPoint.method_1020(mc.field_1724.method_33571()).method_1029();
         float rawYaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
         float rawPitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -90.0, 90.0);
         float randomBoga = (float)(
            (double)(MathUtil.randomLerp(-1.0F, 1.0F) * MathUtil.random(-1.0, 1.0) * (float)MathUtil.randomInt(-2, 2))
               * ThreadLocalRandom.current().nextDouble(-4.3535F, 3.3553F)
         );
         long currentTime = System.currentTimeMillis();
         int presetIndex = (int)(currentTime / 1000L % (long)this.jitterPresets.length);
         if (currentTime >= this.nextJitterSwitch) {
            this.currentJitterPreset = this.nextJitterPreset;
            this.nextJitterPreset = ThreadLocalRandom.current().nextInt(this.jitterPresets.length);

            while (this.nextJitterPreset == this.currentJitterPreset) {
               this.nextJitterPreset = ThreadLocalRandom.current().nextInt(this.jitterPresets.length);
            }

            this.jitterSwitchTime = ThreadLocalRandom.current().nextLong(1000L, 2001L);
            this.nextJitterSwitch = currentTime + this.jitterSwitchTime;
         }

         FunTimeRotation.JitterPreset currentPreset = this.jitterPresets[this.currentJitterPreset];
         FunTimeRotation.JitterPreset nextPreset = this.jitterPresets[this.nextJitterPreset];
         float blend = 1.0F - (float)(this.nextJitterSwitch - currentTime) / (float)this.jitterSwitchTime;
         blend = class_3532.method_15363(blend, 0.0F, 1.0F);
         blend = blend * blend * (3.0F - 2.0F * blend);
         blend = blend * blend * (3.0F - 2.0F * blend);
         float currentYaw = currentPreset.getYaw(currentTime);
         float nextYaw = nextPreset.getYaw(currentTime);
         float currentPitch = currentPreset.getPitch(currentTime);
         float nextPitch = nextPreset.getPitch(currentTime);
         float yawJitter = class_3532.method_16439(blend, currentYaw, nextYaw);
         float pitchJitter = class_3532.method_16439(blend, currentPitch, nextPitch);
         this.lastYawJitter = this.lastYawJitter + (yawJitter - this.lastYawJitter) * 0.6F;
         this.lastPitchJitter = this.lastPitchJitter + (pitchJitter - this.lastPitchJitter) * 0.6F;
         yawJitter = this.lastYawJitter;
         pitchJitter = this.lastPitchJitter;
         canAttack = UAttack.shouldAttack(target, false, true, true, (long)(-MathUtil.random(150.0, 250.0)), ranges);
         long speedTime = System.currentTimeMillis();
         if (speedTime >= this.nextSpeedSwitch) {
            this.currentSpeedPreset = this.nextSpeedPreset;
            this.nextSpeedPreset = ThreadLocalRandom.current().nextInt(this.speedPresets.length);

            while (this.nextSpeedPreset == this.currentSpeedPreset) {
               this.nextSpeedPreset = ThreadLocalRandom.current().nextInt(this.speedPresets.length);
            }

            this.speedSwitchTime = ThreadLocalRandom.current().nextLong(2000L, 4001L);
            this.nextSpeedSwitch = speedTime + this.speedSwitchTime;
         }

         FunTimeRotation.SpeedPreset currentSpeed = this.speedPresets[this.currentSpeedPreset];
         FunTimeRotation.SpeedPreset nextSpeed = this.speedPresets[this.nextSpeedPreset];
         float speedBlend = 1.0F - (float)(this.nextSpeedSwitch - speedTime) / (float)this.speedSwitchTime;
         speedBlend = class_3532.method_15363(speedBlend, 0.0F, 1.0F);
         speedBlend = speedBlend * speedBlend * (3.0F - 2.0F * speedBlend);
         speedBlend = speedBlend * speedBlend * (3.0F - 2.0F * speedBlend);
         float targetYawSpeed = class_3532.method_16439(speedBlend, currentSpeed.getYawSpeed(), nextSpeed.getYawSpeed());
         float targetPitchSpeed = class_3532.method_16439(speedBlend, currentSpeed.getPitchSpeed(), nextSpeed.getPitchSpeed());
         this.lastYawSpeed = this.lastYawSpeed + (targetYawSpeed - this.lastYawSpeed) * 0.5F;
         this.lastPitchSpeed = this.lastPitchSpeed + (targetPitchSpeed - this.lastPitchSpeed) * 0.5F;
         float speed = this.lastYawSpeed + MathUtil.randomLerp(-2.5F, 2.5F);
         float speed2 = this.lastPitchSpeed + MathUtil.randomLerp(-1.0F, 1.0F);
         if (aura.pitchFlickActive) {
            if (System.currentTimeMillis() > aura.pitchFlickEndTime) {
               aura.pitchFlickActive = false;
            } else {
               AttackAura.lastYaw = AttackAura.lastYaw + MathUtil.random(-14.0, 14.0);
            }
         }

         if (canAttack) {
            if (!aura.pitchFlickActive) {
               AttackAura.lastPitch = rawPitch;
            }

            if (!aura.pitchFlickActive) {
               AttackAura.lastYaw = rawYaw;
            }
         }

         if (aura.justAttacked && System.currentTimeMillis() >= aura.attackFlickAt) {
            aura.justAttacked = false;
         }

         RotationProcess.update(
            new Rotation(AttackAura.lastYaw + yawJitter, AttackAura.lastPitch + pitchJitter),
            speed,
            speed2,
            MathUtil.randomLerp(10.124812F, 20.412842F),
            MathUtil.randomLerp(10.124812F, 20.412842F),
            0,
            1,
            false
         );
      }
   }

   @Environment(EnvType.CLIENT)
   private interface JitterPreset {
      float getYaw(long var1);

      float getPitch(long var1);
   }

   @Environment(EnvType.CLIENT)
   private interface SpeedPreset {
      float getYawSpeed();

      float getPitchSpeed();
   }
}
