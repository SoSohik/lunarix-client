package ru.mentions.module.impl.combat;

import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import net.minecraft.class_746;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.aura.AuraUtil;
import ru.mentions.utils.aura.TriggerTiming;
import ru.mentions.utils.aura.TriggerUAttack;

@ModuleInfo(
   name = "TriggerBot",
   desc = "Бьет цель на которую вы навелись",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class TriggerBot extends Module {
   public SliderSetting attackRange = new SliderSetting(this, "Радиус атаки", 3.0F, 2.5F, 5.0F, 0.1F);
   public SliderSetting preRange = new SliderSetting(this, "Радиус обнаружения", 1.0F, 0.0F, 3.0F, 0.1F);
   public BooleanSetting player = new BooleanSetting(this, "Атаковать игроков", true);
   public BooleanSetting golyPlayer = new BooleanSetting(this, "Атаковать голых", true);
   public BooleanSetting mobs = new BooleanSetting(this, "Атаковать мобов", false);
   public BooleanSetting friend = new BooleanSetting(this, "Атаковать друзей", false);
   public ModeSetting typeSprint = new ModeSetting(this, "Тип спринта", "Packet", "Silent", "Legit");
   public BooleanSetting smartCrit = new BooleanSetting(this, "Умные криты", false);
   public BooleanSetting onlyCrits = new BooleanSetting(this, "Только криты", false);
   public BooleanSetting hitOfBlock = new BooleanSetting(this, "Бить через блоки", false);
   public BooleanSetting nohitead = new BooleanSetting(this, "Не бить при еде", false);
   public SliderSetting reactionDelayMs = new SliderSetting(this, "Реакция на цель (мс)", 50.0F, 0.0F, 400.0F, 10.0F);
   public BooleanSetting randomizeTiming = new BooleanSetting(this, "Рандомизация задержки", true);
   public SliderSetting randomDelayMs = new SliderSetting(this, "Разброс задержки (мс)", 45.0F, 0.0F, 400.0F, 5.0F)
      .setVisible(() -> this.randomizeTiming.getValue());
   public SliderSetting minimumDelayMs = new SliderSetting(this, "Минимальная задержка (мс)", 0.0F, 0.0F, 400.0F, 5.0F)
      .setVisible(() -> this.randomizeTiming.getValue());
   public ModeSetting delayDistribution = new ModeSetting(this, "Распределение задержки", "Плавное", "Равномерное")
      .setVisible(() -> this.randomizeTiming.getValue());
   public BooleanSetting intentionalMisses = new BooleanSetting(this, "Иногда промахиваться", false);
   public SliderSetting missChance = new SliderSetting(this, "Шанс промаха (%)", 10.0F, 0.0F, 100.0F, 1.0F).setVisible(() -> this.intentionalMisses.getValue());
   public SliderSetting missRecoveryMs = new SliderSetting(this, "Пауза после промаха (мс)", 300.0F, 100.0F, 800.0F, 25.0F)
      .setVisible(() -> this.intentionalMisses.getValue());
   private final TriggerTiming attackTiming = new TriggerTiming();
   public static class_1309 targets = null;

   @Override
   protected void onDisable() {
      targets = null;
      this.attackTiming.reset();
   }

   @EventHandler
   public void onUpdate(EventUpdate eventUpdate) {
      if (mc.field_1724 == null || mc.field_1687 == null) {
         targets = null;
         this.attackTiming.reset();
      } else if (this.nohitead.getValue() && mc.field_1724.method_6115()) {
         targets = null;
         this.attackTiming.reset();
      } else {
         class_1309 target = this.resolveTarget();
         if (target != null && this.isValidTarget(target)) {
            targets = target;
            this.attackEntity(target);
         } else {
            targets = null;
            this.attackTiming.ready(null, 0L, false, 0L, 0L);
         }
      }
   }

   public float[] getRanges() {
      return new float[]{this.maxReach(), this.preRange.getValue()};
   }

   private float maxReach() {
      float configured = this.attackRange.getValue();
      float vanilla = mc.field_1724 != null && mc.field_1724.method_6128() ? 6.0F : 3.0F;
      return Math.min(configured, vanilla);
   }

   public void attackEntity(class_1309 target) {
      float reach = this.maxReach();
      if (!(AuraUtil.getStrictDistance(target) >= (double)reach) && TriggerUAttack.anyEntityOnRay(target, (double)reach)) {
         long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
         boolean cooldownReady = TriggerUAttack.msCooldownReached() && mc.field_1724.method_7261(0.0F) >= 0.95F;
         long maximumDelay = this.randomizeTiming.getValue() ? this.randomDelayMs.getValue().longValue() : 0L;
         long minimumDelay = this.randomizeTiming.getValue() ? this.minimumDelayMs.getValue().longValue() : 0L;
         if (this.attackTiming
            .ready(target, now, cooldownReady, this.reactionDelayMs.getValue().longValue(), minimumDelay, maximumDelay, this.delayDistribution.is("Плавное"))) {
            float[] ranges = new float[]{reach, this.preRange.getValue(), reach + this.preRange.getValue()};
            TriggerUAttack.antiMissesHittingUpdate(target, false, true, false);
            boolean canAttack = TriggerUAttack.shouldAttack(target, true, true, true, 0L, ranges);
            if (canAttack) {
               if (this.intentionalMisses.getValue() && this.attackTiming.shouldMiss(this.missChance.getValue().doubleValue())) {
                  mc.field_1724.method_6104(class_1268.field_5808);
                  this.attackTiming.attempted(now, this.missRecoveryMs.getValue().longValue());
                  return;
               }

               Runnable[] shieldBreak = TriggerUAttack.hitShieldBreakTaskForUse(target, true);
               Runnable[] shieldPressBypass = TriggerUAttack.resetShieldSilentTaskForUse(true);
               Runnable[] skipSilentSprint = TriggerUAttack.skipSilentSprintingTaskForUse(this.typeSprint.is("Packet") || this.typeSprint.is("Silent"));
               Runnable preHitSendCodeSingleTick = () -> {
                  skipSilentSprint[0].run();
                  shieldPressBypass[0].run();
                  shieldBreak[0].run();
               };
               Runnable postHitSendCodeSingleTick = () -> {
                  shieldBreak[1].run();
                  shieldPressBypass[1].run();
                  skipSilentSprint[1].run();
               };
               TriggerUAttack.useEntity(target, preHitSendCodeSingleTick, postHitSendCodeSingleTick, class_1268.field_5808, false);
               this.attackTiming.attempted(now, 0L);
            }
         }
      } else {
         this.attackTiming.ready(null, 0L, false, 0L, 0L);
      }
   }

   private class_1309 resolveTarget() {
      if (!this.hitOfBlock.getValue()) {
         if (!(mc.field_1765 instanceof class_3966 ehr)) {
            return null;
         } else {
            class_1297 e = ehr.method_17782();
            class_1309 living;
            return e instanceof class_1309 ? (living = (class_1309)e) : null;
         }
      } else {
         class_243 eyePos = mc.field_1724.method_33571();
         class_243 lookVec = mc.field_1724.method_5828(1.0F).method_1029();
         class_1309 best = null;
         double bestAngle = Double.MAX_VALUE;
         double MAX_ANGLE_RAD = Math.toRadians(10.0);

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1309) {
               class_1309 living = (class_1309)entity;
               double angle;
               if (!(entity instanceof class_746)
                  && entity.method_5805()
                  && !((double)mc.field_1724.method_5739(entity) > (double)this.maxReach())
                  && (angle = Math.acos(class_3532.method_15350(lookVec.method_1026(entity.method_33571().method_1020(eyePos).method_1029()), -1.0, 1.0)))
                     <= MAX_ANGLE_RAD
                  && angle < bestAngle) {
                  bestAngle = angle;
                  best = living;
               }
            }
         }

         return best;
      }
   }

   private boolean isValidTarget(class_1309 target) {
      if (target instanceof class_1657 playerEntity) {
         if (!this.player.getValue()) {
            return false;
         }

         if (Client.get().friendManager().isFriend(playerEntity.method_5477().getString()) && !this.friend.getValue()) {
            return false;
         }

         if (playerEntity.method_18396() <= 0.0F && !this.golyPlayer.getValue()) {
            return false;
         }

         if (playerEntity.method_68878()) {
            return false;
         }
      }

      return !(target instanceof class_1308) || this.mobs.getValue();
   }
}
