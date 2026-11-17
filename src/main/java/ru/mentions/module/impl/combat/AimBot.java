package ru.mentions.module.impl.combat;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1531;
import net.minecraft.class_1569;
import net.minecraft.class_1621;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventLook;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.movement.Sprint;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.aura.AimSmoothing;
import ru.mentions.utils.aura.AttackUtil;
import ru.mentions.utils.aura.AuraUtil;
import ru.mentions.utils.aura.GCDUtil;
import ru.mentions.utils.aura.RayTraceUtil;
import ru.mentions.utils.render.RenderUtil;

@ModuleInfo(
   name = "Legit Aura",
   desc = "Гибкая легитная аура: естественная доводка прицела и реальный триггер бот",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AimBot extends Module {
   private static AimBot INSTANCE;
   public ModeSetting aimMode = new ModeSetting(this, "Аим ассист", "Постоянный", "При движении мыши", "Отключен");
   public BooleanSetting autoAttack = new BooleanSetting(this, "Авто-атака (Триггер)", true);
   public SliderSetting targetDistance = new SliderSetting(this, "Дистанция захвата (Lock)", 5.5F, 3.0F, 8.0F, 0.1F);
   public SliderSetting aimDistance = new SliderSetting(this, "Дистанция наводки (Aim)", 4.5F, 2.0F, 6.0F, 0.1F).setVisible(() -> !this.aimMode.is("Отключен"));
   public SliderSetting attackDistance = new SliderSetting(this, "Дистанция удара (Trigger)", 3.8F, 2.0F, 5.0F, 0.1F)
      .setVisible(() -> this.autoAttack.getValue());
   public BooleanSetting keepTarget = new BooleanSetting(this, "Сохранять цель (Target Lock)", true);
   public BooleanSetting smartRetarget = new BooleanSetting(this, "Умное перетаргетирование", true);
   public SliderSetting retargetSensitivity = new SliderSetting(this, "Чутливість перетаргетування (мс)", 150.0F, 0.0F, 500.0F, 10.0F)
      .setVisible(() -> this.smartRetarget.getValue());
   public SliderSetting fov = new SliderSetting(this, "Угол обзора (FOV)", 70.0F, 15.0F, 180.0F, 5.0F);
   public ModeSetting targetPoint = new ModeSetting(this, "Точка прицела", "Ближайшая", "Голова", "Тело", "Центр")
      .setVisible(() -> !this.aimMode.is("Отключен"));
   public SliderSetting speed = new SliderSetting(this, "Скорость по горизонту", 0.05F, 0.001F, 0.4F, 0.001F).setVisible(() -> !this.aimMode.is("Отключен"));
   public BooleanSetting pitchAssist = new BooleanSetting(this, "Доводка по вертикали", true).setVisible(() -> !this.aimMode.is("Отключен"));
   public SliderSetting speed2 = new SliderSetting(this, "Скорость по вертикали", 0.03F, 0.0F, 0.3F, 0.001F)
      .setVisible(() -> !this.aimMode.is("Отключен") && this.pitchAssist.getValue());
   public SliderSetting assistStrength = new SliderSetting(this, "Сила помощи (%)", 60.0F, 0.0F, 100.0F, 1.0F).setVisible(() -> !this.aimMode.is("Отключен"));
   public SliderSetting maxTurnSpeed = new SliderSetting(this, "Лимит поворота (град/с)", 120.0F, 5.0F, 720.0F, 5.0F)
      .setVisible(() -> !this.aimMode.is("Отключен"));
   public SliderSetting aimDeadZone = new SliderSetting(this, "Зона без доводки (град)", 0.15F, 0.0F, 3.0F, 0.05F)
      .setVisible(() -> !this.aimMode.is("Отключен"));
   public SliderSetting verticalFreedom = new SliderSetting(this, "Свобода по вертикали (град)", 0.4F, 0.0F, 10.0F, 0.1F)
      .setVisible(() -> !this.aimMode.is("Отключен") && this.pitchAssist.getValue());
   public SliderSetting aimReactionMs = new SliderSetting(this, "Реакция доводки (мс)", 50.0F, 0.0F, 500.0F, 10.0F)
      .setVisible(() -> !this.aimMode.is("Отключен"));
   public BooleanSetting humanize = new BooleanSetting(this, "Человеческое поведение (Humanize)", true).setVisible(() -> !this.aimMode.is("Отключен"));
   public ModeSetting humanizeCurve = new ModeSetting(
         this, "Кривая сглаживания", "EaseInOutSine", "EaseOutQuad", "EaseOutCubic", "EaseInOutCubic", "EaseOutSine", "Пружина (Back)"
      )
      .setVisible(() -> !this.aimMode.is("Отключен") && this.humanize.getValue());
   public BooleanSetting microJitter = new BooleanSetting(this, "Микро-дрожание прицела", false)
      .setVisible(() -> !this.aimMode.is("Отключен") && this.humanize.getValue());
   public SliderSetting jitterStrength = new SliderSetting(this, "Сила микро-дрожания", 0.12F, 0.02F, 0.35F, 0.01F)
      .setVisible(() -> !this.aimMode.is("Отключен") && this.humanize.getValue() && this.microJitter.getValue());
   public BooleanSetting overshooting = new BooleanSetting(this, "Легкий перелет (Overshoot)", false)
      .setVisible(() -> !this.aimMode.is("Отключен") && this.humanize.getValue());
   public BooleanSetting sticky = new BooleanSetting(this, "Прилипание к цели (Friction)", true).setVisible(() -> !this.aimMode.is("Отключен"));
   public SliderSetting stickyStrength = new SliderSetting(this, "Сила прилипания", 0.5F, 0.1F, 0.9F, 0.05F)
      .setVisible(() -> !this.aimMode.is("Отключен") && this.sticky.getValue());
   public ModeSetting typeTargetESP = new ModeSetting(
      this,
      "Отображение таргета",
      "Картинка",
      "Духи",
      "Кольцо",
      "Кубики",
      "Куб",
      "Ореол",
      "Треугольник",
      "Молния",
      "Спираль",
      "Орбита",
      "Угли",
      "Руны",
      "Пульс",
      "Бублик",
      "Цепи",
      "Души",
      "Celestial",
      "Кристаллы",
      "Ромб",
      "Гроза",
      "Пентограма",
      "Атом",
      "Карты",
      "Мечи",
      "Девочка-целочка",
      "Не отображать"
   );
   public SliderSetting cubeSize = new SliderSetting(this, "Размер куба", 1.15F, 0.6F, 2.5F, 0.05F).setVisible(() -> this.typeTargetESP.is("Куб"));
   public SliderSetting cubeRotateSpeed = new SliderSetting(this, "Скорость вращения куба", 1.2F, 0.2F, 4.0F, 0.05F)
      .setVisible(() -> this.typeTargetESP.is("Куб"));
   public SliderSetting cubeThickness = new SliderSetting(this, "Толщина куба", 2.2F, 0.6F, 5.0F, 0.1F).setVisible(() -> this.typeTargetESP.is("Куб"));
   public BooleanSetting cubeColorOnHit = new BooleanSetting(this, "Цвет куба при ударе", true).setVisible(() -> this.typeTargetESP.is("Куб"));
   public BooleanSetting cubeGlow = new BooleanSetting(this, "Свечение куба", true).setVisible(() -> this.typeTargetESP.is("Куб"));
   public ModeSetting sysEffect = new ModeSetting(this, "Эффект", "Кольцо", "Треугольник", "Молния", "Спираль", "Орбита", "Угли", "Руны", "Пульс")
      .setVisible(this::isParticleTargetEsp);
   public SliderSetting sysRadius = new SliderSetting(this, "Радиус", 0.7F, 0.4F, 1.6F, 0.05F).setVisible(this::isParticleTargetEsp);
   public SliderSetting sysSize = new SliderSetting(this, "Размер частиц", 0.17F, 0.04F, 0.4F, 0.01F).setVisible(this::isParticleTargetEsp);
   public SliderSetting sysDensity = new SliderSetting(this, "Плотность", 60.0F, 10.0F, 120.0F, 5.0F).setVisible(this::isParticleTargetEsp);
   public SliderSetting sysSpeed = new SliderSetting(this, "Скорость", 0.7F, 0.1F, 2.0F, 0.1F).setVisible(this::isParticleTargetEsp);
   public ModeSetting attackMode = new ModeSetting(this, "Режим атаки", "Обычный");
   public ModeSetting hitConditions = new ModeSetting(this, "Условия удара", "Умные криты", "Только криты", "Обычный");
   public BooleanSetting customCooldown = new BooleanSetting(this, "Кастомный кд", false).setVisible(() -> this.autoAttack.getValue());
   public SliderSetting customCooldownPercent = new SliderSetting(this, "Процент кд", 100.0F, 0.0F, 100.0F, 5.0F)
      .setVisible(() -> this.autoAttack.getValue() && this.customCooldown.getValue());
   public BooleanSetting tpsSync = new BooleanSetting(this, "Синхронизация TPS", false).setVisible(() -> this.autoAttack.getValue());
   public BooleanSetting shieldBreak = new BooleanSetting(this, "Ломать щит топором", true).setVisible(() -> this.autoAttack.getValue());
   public ModeSetting sprintReset = new ModeSetting(this, "Сброс спринта", "Packet", "Silent", "Legit", "Нет").setVisible(() -> this.autoAttack.getValue());
   public BooleanSetting maceReaction = new BooleanSetting(this, "Булава в воздухе (без КД)", true);
   public SliderSetting sprintResumeDelay = new SliderSetting(this, "Возврат спринта (мс)", 250.0F, 50.0F, 800.0F, 10.0F)
      .setVisible(() -> this.autoAttack.getValue() && this.sprintReset.is("Legit"));
   public BooleanSetting onlyOnClick = new BooleanSetting(this, "Только при зажатии ЛКМ", false);
   public BooleanSetting onlyWeapon = new BooleanSetting(this, "Только с оружием", true);
   public BooleanSetting throughWalls = new BooleanSetting(this, "Сквозь стены", false);
   public BooleanSetting renderFov = new BooleanSetting(this, "Отображать круг FOV", false);
   public MultiBooleanSetting targets = new MultiBooleanSetting(
      this,
      "Кого атаковать",
      new BooleanSetting("Игроков", true),
      new BooleanSetting("Голых игроков", true),
      new BooleanSetting("Мобов", false),
      new BooleanSetting("Друзей", false)
   );
   public static class_1309 target;
   private static final double MACE_HORIZONTAL_REACH = 3.0;
   private static final double MACE_RAY_REACH = 5.0;
   private static final double MACE_MIN_HEIGHT = 1.6;
   private static final double MACE_MAX_HEIGHT = 2.0;
   private float subpixelYaw = 0.0F;
   private float subpixelPitch = 0.0F;
   private long lastFrameTime = 0L;
   private class_1309 assistTarget;
   private long assistAcquiredAt;
   private class_1309 retargetCandidate;
   private long retargetSince;
   private String lastAimMode;
   private long nextJitterSample;
   private float heldJitterYaw;
   private float heldJitterPitch;
   private long legitSprintResumeAt;

   public boolean isParticleTargetEsp() {
      return this.typeTargetESP.is("Ореол")
         || this.typeTargetESP.is("Треугольник")
         || this.typeTargetESP.is("Молния")
         || this.typeTargetESP.is("Спираль")
         || this.typeTargetESP.is("Орбита")
         || this.typeTargetESP.is("Угли")
         || this.typeTargetESP.is("Руны")
         || this.typeTargetESP.is("Пульс");
   }

   public AimBot() {
      INSTANCE = this;
   }

   public static AimBot get() {
      return INSTANCE;
   }

   @Override
   public void onDisable() {
      target = null;
      this.subpixelYaw = 0.0F;
      this.subpixelPitch = 0.0F;
      this.legitSprintResumeAt = 0L;
      this.resetAim();
      super.onDisable();
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      this.tickLegitSprintResume();
      if (this.canAssist()) {
         this.updateTarget();
         if (this.autoAttack.getValue()) {
            this.processAttack();
         }
      } else {
         this.resetAim();
         if (!this.keepTarget.getValue() || !this.isClicking()) {
            target = null;
         }
      }
   }

   private void resetAim() {
      this.subpixelYaw = 0.0F;
      this.subpixelPitch = 0.0F;
      this.lastFrameTime = 0L;
      this.assistTarget = null;
      this.lastAimMode = null;
      this.retargetCandidate = null;
      this.retargetSince = 0L;
      this.nextJitterSample = 0L;
      this.heldJitterYaw = 0.0F;
      this.heldJitterPitch = 0.0F;
   }

   private void tickLegitSprintResume() {
      if (this.legitSprintResumeAt != 0L && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         if (now < this.legitSprintResumeAt) {
            mc.field_1724.method_5728(false);
            if (mc.field_1690 != null) {
               mc.field_1690.field_1867.method_23481(false);
            }
         } else {
            this.legitSprintResumeAt = 0L;
            if (mc.field_1690 != null && mc.field_1690.field_1894.method_1434()) {
               mc.field_1690.field_1867.method_23481(true);
            }
         }
      }
   }

   private boolean aimReady(long now) {
      String mode = this.aimMode.getValue();
      if (this.assistTarget != target || !mode.equals(this.lastAimMode)) {
         this.resetAim();
         this.assistTarget = target;
         this.lastAimMode = mode;
         this.assistAcquiredAt = now;
      }

      return now - this.assistAcquiredAt >= this.aimReactionMs.getValue().longValue() * 1000000L;
   }

   private float aimSeconds(long now) {
      float dt = this.lastFrameTime == 0L ? 0.016666668F : (float)((double)(now - this.lastFrameTime) / 1.0E9);
      this.lastFrameTime = now;
      return Math.max(0.0F, Math.min(0.05F, dt));
   }

   @EventHandler
   public void onEvent(EventDisplay event) {
      if (this.renderFov.getValue()) {
         this.renderFovCircle(event);
      }

      if (this.aimMode.is("Постоянный")) {
         if (this.canAssist() && target != null && target.method_5805()) {
            boolean maceAirborne = this.isMaceAirborne();
            double maxAimDist = maceAirborne ? MACE_HORIZONTAL_REACH : (double)this.aimDistance.getValue().floatValue();
            double dist = maceAirborne ? this.horizontalDistanceTo(target) : (double)mc.field_1724.method_5739(target);
            if (dist > maxAimDist) {
               this.resetAim();
            } else {
               long now = System.nanoTime();
               if (this.aimReady(now)) {
                  float dt = this.aimSeconds(now);
                  class_243 eyePos = mc.field_1724.method_33571();
                  class_243 aimPos = this.getAimPosition(target);
                  class_243 diff = aimPos.method_1020(eyePos);
                  float targetYaw = (float)Math.toDegrees(Math.atan2(-diff.field_1352, diff.field_1350));
                  float targetPitch = (float)class_3532.method_15350(
                     -Math.toDegrees(Math.atan2(diff.field_1351, Math.hypot(diff.field_1352, diff.field_1350))), -90.0, 90.0
                  );
                  float currentYaw = mc.field_1724.method_36454();
                  float currentPitch = mc.field_1724.method_36455();
                  float yawDelta = class_3532.method_15393(targetYaw - currentYaw);
                  float pitchDelta = class_3532.method_15393(targetPitch - currentPitch);
                  float absYaw = Math.abs(yawDelta);
                  float absPitch = Math.abs(pitchDelta);
                  float easeYaw = 1.0F;
                  float easePitch = 1.0F;
                  float jitterYaw = 0.0F;
                  float jitterPitch = 0.0F;
                  if (this.humanize.getValue()) {
                     float progressYaw = Math.min(1.0F, absYaw / Math.max(10.0F, this.fov.getValue() * 0.45F));
                     float progressPitch = Math.min(1.0F, absPitch / 25.0F);
                     String curve = this.humanizeCurve.getValue();
                     easeYaw = Math.max(0.15F, this.computeEasing(progressYaw, curve));
                     easePitch = Math.max(0.15F, this.computeEasing(progressPitch, curve));
                     if (this.overshooting.getValue() && absYaw > 3.0F && absYaw < 14.0F) {
                        easeYaw *= 1.08F;
                     }

                     if (this.microJitter.getValue() && absYaw > this.aimDeadZone.getValue()) {
                        if (now >= this.nextJitterSample) {
                           float jStrength = this.jitterStrength.getValue();
                           this.heldJitterYaw = (float)ThreadLocalRandom.current().nextDouble((double)(-jStrength), (double)jStrength);
                           this.heldJitterPitch = (float)ThreadLocalRandom.current().nextDouble((double)(-jStrength) * 0.4, (double)jStrength * 0.4);
                           this.nextJitterSample = now + 150000000L;
                        }

                        jitterYaw = this.heldJitterYaw;
                        jitterPitch = this.heldJitterPitch;
                     }
                  }

                  float friction = 1.0F;
                  boolean onTarget = this.isAimingAtTarget(target, maceAirborne ? MACE_RAY_REACH : (double)this.attackDistance.getValue().floatValue());
                  if (onTarget && this.sticky.getValue()) {
                     friction = 1.0F - this.stickyStrength.getValue();
                  }

                  float strength = this.assistStrength.getValue() / 100.0F;
                  if (maceAirborne) {
                     strength = Math.min(1.0F, Math.max(0.45F, strength * 1.25F));
                  }

                  if (this.retargetCandidate != null) {
                     strength *= 0.3F;
                  }

                  float speedMult = maceAirborne ? 1.35F : 1.0F;
                  float speedX = this.speed.getValue() * 0.75F * strength * speedMult;
                  float speedY = this.speed2.getValue() * 0.75F * strength * speedMult;
                  float turnLimit = this.maxTurnSpeed.getValue() * (maceAirborne ? 1.4F : 1.0F);
                  float deadZone = this.aimDeadZone.getValue();
                  float pitchZone = Math.max(deadZone, this.verticalFreedom.getValue() * (maceAirborne ? 0.6F : 1.0F));
                  float moveYaw = AimSmoothing.step(yawDelta, speedX * easeYaw * friction, dt, turnLimit, deadZone);
                  float movePitch = this.pitchAssist.getValue() ? AimSmoothing.step(pitchDelta, speedY * easePitch * friction, dt, turnLimit, pitchZone) : 0.0F;
                  float frameLimit = turnLimit * dt;
                  if (strength > 0.0F && absYaw > deadZone) {
                     moveYaw = class_3532.method_15363(moveYaw + jitterYaw * dt * 60.0F * strength, -frameLimit, frameLimit);
                  } else {
                     this.subpixelYaw = 0.0F;
                  }

                  if (strength > 0.0F && this.pitchAssist.getValue() && absPitch > pitchZone) {
                     movePitch = class_3532.method_15363(movePitch + jitterPitch * dt * 60.0F * strength, -frameLimit, frameLimit);
                  } else {
                     this.subpixelPitch = 0.0F;
                  }

                  float gcd = GCDUtil.getGCDValue();
                  if (gcd <= 1.0E-4F) {
                     gcd = 0.0015F;
                  }

                  this.subpixelYaw += moveYaw;
                  this.subpixelPitch += movePitch;
                  float stepsYaw = (float)Math.round(this.subpixelYaw / gcd) * gcd;
                  float stepsPitch = (float)Math.round(this.subpixelPitch / gcd) * gcd;
                  if (Math.abs(stepsYaw) >= gcd) {
                     mc.field_1724.method_36456(currentYaw + stepsYaw);
                     this.subpixelYaw -= stepsYaw;
                  }

                  if (this.pitchAssist.getValue() && Math.abs(stepsPitch) >= gcd) {
                     float newPitch = class_3532.method_15363(currentPitch + stepsPitch, -90.0F, 90.0F);
                     mc.field_1724.method_36457(newPitch);
                     this.subpixelPitch -= stepsPitch;
                  }
               }
            }
         } else {
            this.resetAim();
         }
      }
   }

   @EventHandler
   public void onLook(EventLook event) {
      if (this.aimMode.is("При движении мыши")) {
         if (this.canAssist() && target != null && target.method_5805()) {
            boolean maceAirborne = this.isMaceAirborne();
            double maxAimDist = maceAirborne ? MACE_HORIZONTAL_REACH : (double)this.aimDistance.getValue().floatValue();
            double dist = maceAirborne ? this.horizontalDistanceTo(target) : (double)mc.field_1724.method_5739(target);
            if (dist > maxAimDist) {
               this.resetAim();
            } else {
               long now = System.nanoTime();
               if (this.aimReady(now)) {
                  float dt = this.aimSeconds(now);
                  if (!(Math.abs(event.getYaw()) < 0.001) || !(Math.abs(event.getPitch()) < 0.001)) {
                     class_243 eyePos = mc.field_1724.method_33571();
                     class_243 aimPos = this.getAimPosition(target);
                     class_243 diff = aimPos.method_1020(eyePos);
                     float targetYaw = (float)Math.toDegrees(Math.atan2(-diff.field_1352, diff.field_1350));
                     float targetPitch = (float)class_3532.method_15350(
                        -Math.toDegrees(Math.atan2(diff.field_1351, Math.hypot(diff.field_1352, diff.field_1350))), -90.0, 90.0
                     );
                     float currentYaw = mc.field_1724.method_36454();
                     float currentPitch = mc.field_1724.method_36455();
                     float yawDelta = class_3532.method_15393(targetYaw - currentYaw);
                     float pitchDelta = class_3532.method_15393(targetPitch - currentPitch);
                     float absYaw = Math.abs(yawDelta);
                     float absPitch = Math.abs(pitchDelta);
                     float easeYaw = 1.0F;
                     float easePitch = 1.0F;
                     if (this.humanize.getValue()) {
                        float progressYaw = Math.min(1.0F, absYaw / Math.max(10.0F, this.fov.getValue() * 0.45F));
                        float progressPitch = Math.min(1.0F, absPitch / 25.0F);
                        String curve = this.humanizeCurve.getValue();
                        easeYaw = Math.max(0.15F, this.computeEasing(progressYaw, curve));
                        easePitch = Math.max(0.15F, this.computeEasing(progressPitch, curve));
                        if (this.overshooting.getValue() && absYaw > 3.0F && absYaw < 14.0F) {
                           easeYaw *= 1.08F;
                        }
                     }

                     float friction = 1.0F;
                     boolean onTarget = this.isAimingAtTarget(target, maceAirborne ? MACE_RAY_REACH : (double)this.attackDistance.getValue().floatValue());
                     if (onTarget && this.sticky.getValue()) {
                        friction = 1.0F - this.stickyStrength.getValue();
                     }

                     float strength = this.assistStrength.getValue() / 100.0F;
                     if (maceAirborne) {
                        strength = Math.min(1.0F, Math.max(0.45F, strength * 1.25F));
                     }

                     if (this.retargetCandidate != null) {
                        strength *= 0.3F;
                     }

                     float speedMult = maceAirborne ? 1.35F : 1.0F;
                     float turnLimit = this.maxTurnSpeed.getValue() * (maceAirborne ? 1.4F : 1.0F);
                     float deadZone = this.aimDeadZone.getValue();
                     double inputYaw = event.getYaw();
                     double inputPitch = event.getPitch();
                     float yawStep = AimSmoothing.step(yawDelta, this.speed.getValue() * strength * easeYaw * friction * speedMult, dt, turnLimit, deadZone);
                     yawStep = AimSmoothing.mouseStep(yawStep, inputYaw, inputPitch, strength);
                     event.setYaw(inputYaw + (double)yawStep / 0.15);
                     if (this.pitchAssist.getValue()) {
                        float pitchZone = Math.max(deadZone, this.verticalFreedom.getValue() * (maceAirborne ? 0.6F : 1.0F));
                        float pitchStep = AimSmoothing.step(
                           pitchDelta, this.speed2.getValue() * strength * easePitch * friction * speedMult, dt, turnLimit, pitchZone
                        );
                        pitchStep = AimSmoothing.mouseStep(pitchStep, inputYaw, inputPitch, strength);
                        event.setPitch(inputPitch + (double)pitchStep / 0.15);
                     }
                  }
               }
            }
         } else {
            this.resetAim();
         }
      }
   }

   private float computeEasing(float progress, String curve) {
      float x = class_3532.method_15363(progress, 0.0F, 1.0F);

      return switch (curve) {
         case "EaseInOutSine" -> -((float)(Math.cos(Math.PI * (double)x) - 1.0)) / 2.0F;
         case "EaseOutQuad" -> 1.0F - (1.0F - x) * (1.0F - x);
         case "EaseOutCubic" -> 1.0F - (float)Math.pow((double)(1.0F - x), 3.0);
         case "EaseInOutCubic" -> x < 0.5F ? 4.0F * x * x * x : 1.0F - (float)Math.pow((double)(-2.0F * x + 2.0F), 3.0) / 2.0F;
         case "EaseOutSine" -> (float)Math.sin((double)x * Math.PI / 2.0);
         case "Пружина (Back)" -> {
            float c1 = 1.70158F;
            float c3 = c1 + 1.0F;
            yield 1.0F + c3 * (float)Math.pow((double)(x - 1.0F), 3.0) + c1 * (float)Math.pow((double)(x - 1.0F), 2.0);
         }
         default -> 1.0F - (1.0F - x) * (1.0F - x);
      };
   }

   private void processAttack() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (mc.field_1755 == null) {
            if (!mc.field_1761.method_2923()) {
               if (!mc.field_1724.method_6115()) {
                  if (!this.onlyOnClick.getValue() || this.isClicking()) {
                     if (!this.onlyWeapon.getValue() || this.isHoldingWeapon()) {
                        boolean maceAirborne = this.isMaceAirborne();
                        double maxReach = maceAirborne ? MACE_RAY_REACH : (double)this.attackDistance.getValue().floatValue();
                        class_1309 aimedTarget = this.resolveCrosshairEntity(maxReach);
                        if (aimedTarget == null && target != null && this.isAimingAtTarget(target, maxReach)) {
                           aimedTarget = target;
                        }

                        if (aimedTarget != null && aimedTarget.method_5805() && aimedTarget != mc.field_1724) {
                           String name = aimedTarget.method_5477().getString();
                           if (!Client.get().friendManager().isFriend(name)) {
                              if (this.targets.get("Мобов").getValue() || aimedTarget instanceof class_1657) {
                                 double attackGate = maceAirborne
                                    ? this.horizontalDistanceTo(aimedTarget)
                                    : (double)mc.field_1724.method_5739(aimedTarget);
                                 double allowedReach = maceAirborne
                                    ? MACE_HORIZONTAL_REACH
                                    : (double)this.attackDistance.getValue().floatValue();
                                 if (!(attackGate > allowedReach)) {
                                    if (!maceAirborne) {
                                       float cd = mc.field_1724.method_7261(this.tpsSync.getValue() ? 1.0F / mc.method_61966().method_60637(true) : 0.0F);
                                       float threshold = this.customCooldown.getValue() ? this.customCooldownPercent.getValue() / 100.0F : 0.92F;
                                       if (cd < threshold) {
                                          return;
                                       }

                                       boolean onGround = mc.field_1724.method_24828();
                                       boolean holdingJump = mc.field_1690.field_1903.method_1434();
                                       double velY = mc.field_1724.method_18798().field_1351;
                                       double fallDist = mc.field_1724.field_6017;
                                       boolean restricted = AttackUtil.hasMovementRestrictions()
                                          || AuraUtil.isPlayerInWeb()
                                          || mc.field_1724.method_6101()
                                          || mc.field_1724.method_5771()
                                          || mc.field_1724.method_5869()
                                          || mc.field_1724.method_5799();
                                       boolean isCritState = !onGround
                                          && (velY < -0.05 || fallDist > 0.0 || AttackUtil.isPlayerInCriticalState());
                                       if (!restricted) {
                                          if (this.hitConditions.is("Только криты")) {
                                             if (!isCritState) {
                                                return;
                                             }
                                          } else if (this.hitConditions.is("Умные криты")) {
                                             if (holdingJump) {
                                                // затиснутий пробіл — б'ємо тільки в кріт-вікно: в повітрі під час падіння
                                                boolean critWindow = !onGround && (velY < -0.03 || fallDist > 0.0);
                                                if (!critWindow) {
                                                   return;
                                                }
                                             } else if (!onGround && !isCritState) {
                                                return;
                                             }
                                          }
                                       }
                                    }

                                    this.performAttack(aimedTarget);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void performAttack(class_1309 target) {
      if (mc.field_1761 != null && mc.field_1724 != null) {
         int previousSlot = -1;
         boolean brokeShield = false;
         if (this.shieldBreak.getValue() && target.method_6039()) {
            int axeSlot = this.findAxeSlot();
            if (axeSlot != -1 && axeSlot != mc.field_1724.method_31548().method_67532()) {
               previousSlot = mc.field_1724.method_31548().method_67532();
               mc.field_1724.method_31548().method_61496(axeSlot);
               brokeShield = true;
            }
         }

         boolean sprint = mc.field_1724.method_5624();
         boolean resetSprint = sprint && !this.sprintReset.is("Нет");
         boolean legitReset = resetSprint && this.sprintReset.is("Legit");
         boolean releaseSprintKey = resetSprint && (this.sprintReset.is("Silent") || legitReset);
         boolean sprintKeyPressed = mc.field_1690.field_1867.method_1434();
         boolean legitScheduled = false;

         try {
            if (releaseSprintKey) {
               mc.field_1690.field_1867.method_23481(false);
            }

            if (resetSprint) {
               mc.field_1724.method_5728(false);
            }

            mc.field_1761.method_2918(mc.field_1724, target);
            mc.field_1724.method_6104(class_1268.field_5808);
            if (legitReset) {
               long delay = (long)this.sprintResumeDelay.getValue().floatValue();
               this.legitSprintResumeAt = System.currentTimeMillis() + delay;
               Sprint sprintModule = Sprint.get();
               if (sprintModule != null) {
                  sprintModule.blockSprintFor(delay);
               }

               legitScheduled = true;
            }
         } finally {
            if (releaseSprintKey && (!legitReset || !legitScheduled)) {
               mc.field_1690.field_1867.method_23481(sprintKeyPressed);
            }

            if (brokeShield && previousSlot != -1) {
               mc.field_1724.method_31548().method_61496(previousSlot);
            }
         }
      }
   }

   private boolean isAimingAtTarget(class_1309 target, double maxDist) {
      if (target != null && mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1765 instanceof class_3966 ehr && ehr.method_17782() == target) {
            double d = mc.field_1724.method_33571().method_1020(ehr.method_17784()).method_1033();
            return d <= maxDist;
         }

         float tickDelta = mc.method_61966().method_60637(true);
         class_243 eyePos = mc.field_1724.method_5836(tickDelta);
         float yaw = mc.field_1724.method_36454();
         float pitch = mc.field_1724.method_36455();
         class_243 lookVec = RayTraceUtil.calculateViewVector(yaw, pitch);
         class_243 endVec = eyePos.method_1019(lookVec.method_1021(maxDist));
         class_238 box = target.method_5829();
         if (box.method_1006(eyePos)) {
            return true;
         } else {
            Optional<class_243> hit = box.method_992(eyePos, endVec);
            if (!hit.isPresent()) {
               return false;
            } else {
               class_243 hitPoint = hit.get();
               double hitDist = eyePos.method_1020(hitPoint).method_1033();
               if (hitDist > maxDist) {
                  return false;
               } else {
                  if (!this.throughWalls.getValue()) {
                     class_239 blockHit = RayTraceUtil.traceBlock(eyePos, hitPoint, class_3960.field_17558, class_242.field_1348);
                     if (blockHit != null && blockHit.method_17783() == class_240.field_1332) {
                        double blockDist = eyePos.method_1020(blockHit.method_17784()).method_1033();
                        if (blockDist < hitDist - 0.05 && !mc.field_1724.method_6057(target)) {
                           return false;
                        }
                     }
                  }

                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private class_1309 resolveCrosshairEntity(double maxDist) {
      if (mc.field_1765 instanceof class_3966 ehr && ehr.method_17782() instanceof class_1309) {
         double d = mc.field_1724.method_33571().method_1020(ehr.method_17784()).method_1033();
         if (d <= maxDist) {
            return (class_1309)ehr.method_17782();
         }
      }

      float tickDelta = mc.method_61966().method_60637(true);
      class_243 eyePos = mc.field_1724.method_5836(tickDelta);
      float yaw = mc.field_1724.method_36454();
      float pitch = mc.field_1724.method_36455();
      class_243 lookVec = RayTraceUtil.calculateViewVector(yaw, pitch);
      class_243 endVec = eyePos.method_1019(lookVec.method_1021(maxDist));
      class_1309 closest = null;
      double closestDist = maxDist;

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1309 && this.isValidCandidate((class_1309)entity, (float)maxDist)) {
            class_1309 living = (class_1309)entity;
            class_238 box = living.method_5829();
            if (box.method_1006(eyePos)) {
               return living;
            }

            Optional<class_243> hit = box.method_992(eyePos, endVec);
            if (hit.isPresent()) {
               double d = eyePos.method_1020(hit.get()).method_1033();
               if (d < closestDist) {
                  closestDist = d;
                  closest = living;
               }
            }
         }
      }

      return closest;
   }

   private int findAxeSlot() {
      for (int i = 0; i < 9; i++) {
         class_1799 s = mc.field_1724.method_31548().method_5438(i);
         if (s != null && !s.method_7960() && s.method_7909().toString().toLowerCase().contains("axe")) {
            return i;
         }
      }

      return -1;
   }

   private void renderFovCircle(EventDisplay event) {
      if (mc.field_1724 != null && mc.field_1755 == null) {
         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         float scaleFix = targetScale / currentScale;
         int screenWidth = (int)((float)mc.method_22683().method_4486() / scaleFix);
         int screenHeight = (int)((float)mc.method_22683().method_4502() / scaleFix);
         float cx = (float)screenWidth / 2.0F;
         float cy = (float)screenHeight / 2.0F;
         int fovSetting = (Integer)mc.field_1690.method_41808().method_41753();
         double halfFovRad = Math.toRadians((double)fovSetting / 2.0);
         double halfAimFovRad = Math.toRadians((double)this.fov.getValue().floatValue() / 2.0);
         float radius = (float)(Math.tan(halfAimFovRad) / Math.tan(halfFovRad) * (double)(screenWidth / 2));
         radius = Math.min(radius, (float)screenHeight / 2.0F);
         if (radius > 2.0F) {
            RenderUtil.Render2D.roundedCircleProgress(
               event.getDrawContext(), cx, cy, radius, 1.0F, 1.0F, 0, ThemeColor.getHudColor(0.25F), ThemeColor.getHudColor(0.25F)
            );
         }
      }
   }

   private boolean canAssist() {
      if (mc.field_1724 == null || mc.field_1687 == null) {
         return false;
      } else if (mc.field_1755 != null) {
         return false;
      } else {
         return this.onlyOnClick.getValue() && !this.isClicking() ? false : !this.onlyWeapon.getValue() || this.isHoldingWeapon();
      }
   }

   private boolean isClicking() {
      return mc.field_1690.field_1886.method_1434() || mc.field_1729.method_1608();
   }

   private boolean isHoldingWeapon() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         class_1799 stack = mc.field_1724.method_6047();
         if (stack != null && !stack.method_7960()) {
            String name = stack.method_7909().toString().toLowerCase();
            return name.contains("sword") || name.contains("axe") || name.contains("mace");
         } else {
            return false;
         }
      }
   }

   private void updateTarget() {
      if (AttackAura.get() != null
         && AttackAura.get().isEnabled()
         && AttackAura.get().typeRotation.is("TriggerBot")
         && AttackAura.target != null
         && this.isValidCandidate(AttackAura.target, this.targetDistance.getValue())) {
         target = AttackAura.target;
      } else {
         if (this.smartRetarget.getValue()) {
            if (this.tickSmartRetarget()) {
               return;
            }

            if (this.retargetCandidate != null && target != null && this.isValidLockedTarget(target)) {
               return;
            }
         }

         if (this.keepTarget.getValue() && target != null) {
            if (this.isValidLockedTarget(target)) {
               return;
            }

            target = null;
         }

         class_1309 bestTarget = null;
         double bestScore = Double.MAX_VALUE;
         class_243 eyePos = mc.field_1724.method_33571();
         class_243 lookVec = mc.field_1724.method_5828(1.0F).method_1029();
         boolean maceAir = this.isMaceAirborne();
         double maxFovHalf = (double)this.fov.getValue().floatValue() / 2.0 * (maceAir ? 1.5 : 1.0);
         float maxDist = maceAir ? (float)MACE_HORIZONTAL_REACH : this.targetDistance.getValue();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1309 && this.isValidCandidate((class_1309)entity, maxDist)) {
               class_1309 living = (class_1309)entity;
               class_243 aimPos = this.getAimPosition(living);
               class_243 toAim = aimPos.method_1020(eyePos).method_1029();
               double dot = class_3532.method_15350(lookVec.method_1026(toAim), -1.0, 1.0);
               double angle = Math.toDegrees(Math.acos(dot));
               if (!(angle > maxFovHalf)) {
                  double dist = maceAir ? this.horizontalDistanceTo(living) : (double)mc.field_1724.method_5739(living);
                  double score = angle + dist * 1.5;
                  if (score < bestScore) {
                     bestScore = score;
                     bestTarget = living;
                  }
               }
            }
         }

         target = bestTarget;
      }
   }

   private boolean tickSmartRetarget() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean maceAir = this.isMaceAirborne();
         double reach = maceAir
            ? MACE_RAY_REACH
            : (double)Math.max(this.attackDistance.getValue(), this.aimDistance.getValue());
         class_1309 crosshair = this.resolveCrosshairEntity(reach);
         float maxDist = maceAir ? (float)MACE_HORIZONTAL_REACH : this.targetDistance.getValue();
         if (crosshair != null && crosshair != target && this.isValidCandidate(crosshair, maxDist)) {
            long now = System.currentTimeMillis();
            if (crosshair != this.retargetCandidate) {
               this.retargetCandidate = crosshair;
               this.retargetSince = now;
               return false;
            } else if (now - this.retargetSince < this.retargetSensitivity.getValue().longValue()) {
               return false;
            } else {
               target = crosshair;
               this.retargetCandidate = null;
               this.retargetSince = 0L;
               return true;
            }
         } else {
            this.retargetCandidate = null;
            this.retargetSince = 0L;
            return false;
         }
      } else {
         this.retargetCandidate = null;
         this.retargetSince = 0L;
         return false;
      }
   }

   private boolean isValidCandidate(class_1309 entity, float maxDist) {
      if (entity == null || entity instanceof class_746) {
         return false;
      } else if (entity.method_5805() && !entity.method_5655() && !(entity instanceof class_1531)) {
         double distance = this.isMaceAirborne() ? this.horizontalDistanceTo(entity) : (double)mc.field_1724.method_5739(entity);
         if (distance > maxDist) {
            return false;
         } else if (!this.throughWalls.getValue() && !mc.field_1724.method_6057(entity)) {
            return false;
         } else {
            if (entity instanceof class_1657 p) {
               if (!this.targets.get("Игроков").getValue()) {
                  return false;
               }

               if (p.method_68878() || p.method_7325()) {
                  return false;
               }

               if (Client.get().friendManager().isFriend(p.method_5477().getString()) && !this.targets.get("Друзей").getValue()) {
                  return false;
               }

               if (p.method_18396() <= 0.0F && !this.targets.get("Голых игроков").getValue()) {
                  return false;
               }
            } else {
               if (!(entity instanceof class_1569) && !(entity instanceof class_1621) && !(entity instanceof class_1646) && !(entity instanceof class_1429)) {
                  return false;
               }

               if (!this.targets.get("Мобов").getValue()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private boolean isValidLockedTarget(class_1309 entity) {
      boolean maceAir = this.isMaceAirborne();
      float effectiveDist = maceAir ? (float)MACE_HORIZONTAL_REACH : this.targetDistance.getValue();
      if (!this.isValidCandidate(entity, effectiveDist)) {
         return false;
      } else {
         class_243 eyePos = mc.field_1724.method_33571();
         class_243 lookVec = mc.field_1724.method_5828(1.0F).method_1029();
         class_243 aimPos = this.getAimPosition(entity);
         class_243 toAim = aimPos.method_1020(eyePos).method_1029();
         double dot = class_3532.method_15350(lookVec.method_1026(toAim), -1.0, 1.0);
         double angle = Math.toDegrees(Math.acos(dot));
         return angle <= (double)(this.fov.getValue() / 2.0F + 25.0F);
      }
   }

   private class_243 getAimPosition(class_1309 target) {
      String point = this.targetPoint.getValue();
      double height = (double)target.method_17682();
      if (point.equals("Голова")) {
         return target.method_73189().method_1031(0.0, height * 0.88, 0.0);
      } else if (point.equals("Тело")) {
         return target.method_73189().method_1031(0.0, height * 0.55, 0.0);
      } else if (point.equals("Центр")) {
         return target.method_73189().method_1031(0.0, height * 0.5, 0.0);
      } else {
         class_243 eyePos = mc.field_1724.method_33571();
         class_238 box = target.method_5829();
         double padding = 0.05;
         double minX = box.field_1323 + padding;
         double maxX = box.field_1320 - padding;
         double minY = box.field_1322 + 0.15;
         double maxY = box.field_1325 - 0.15;
         double minZ = box.field_1321 + padding;
         double maxZ = box.field_1324 - padding;
         double clampedX = class_3532.method_15350(eyePos.field_1352, Math.min(minX, maxX), Math.max(minX, maxX));
         double clampedY = class_3532.method_15350(eyePos.field_1351, Math.min(minY, maxY), Math.max(minY, maxY));
         double clampedZ = class_3532.method_15350(eyePos.field_1350, Math.min(minZ, maxZ), Math.max(minZ, maxZ));
         return new class_243(clampedX, clampedY, clampedZ);
      }
   }

   public static boolean isHoldingMace(class_1309 entity) {
      if (entity == null) {
         return false;
      } else {
         class_1799 main = entity.method_6047();
         if (isMaceItem(main)) {
            return true;
         } else {
            class_1799 off = entity.method_6079();
            return isMaceItem(off);
         }
      }
   }

   public static boolean isMaceItem(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      } else if (stack.method_31574(class_1802.field_49814)) {
         return true;
      } else {
         try {
            class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
            if (id != null && "mace".equals(id.method_12832())) {
               return true;
            }
         } catch (Throwable var2) {
         }

         String name = stack.method_7909().toString().toLowerCase(Locale.ROOT);
         return name.contains("mace");
      }
   }

   public static boolean isTargetMaceAirborne(class_1309 entity) {
      if (entity == null || !entity.method_5805() || entity.method_5655()) {
         return false;
      } else {
         return !isHoldingMace(entity) ? false : !entity.method_24828() && !entity.method_5799() && !entity.method_5771() && !entity.method_6101();
      }
   }

   public boolean isMaceAirborne() {
      if (!this.maceReaction.getValue() || mc.field_1724 == null) {
         return false;
      } else if (!isHoldingMace(mc.field_1724)) {
         return false;
      } else {
         boolean airborne = (!mc.field_1724.method_24828() || mc.field_1724.field_6017 > 0.0)
            && !mc.field_1724.method_5799()
            && !mc.field_1724.method_5771()
            && !mc.field_1724.method_6101();
         if (!airborne) {
            return false;
         }

         double height = AttackAura.getDistanceToGround(mc.field_1724);
         return height >= MACE_MIN_HEIGHT && height <= MACE_MAX_HEIGHT;
      }
   }

   private double horizontalDistanceTo(class_1309 entity) {
      if (entity == null || mc.field_1724 == null) {
         return Double.MAX_VALUE;
      }

      double dx = entity.method_23317() - mc.field_1724.method_23317();
      double dz = entity.method_23321() - mc.field_1724.method_23321();
      return Math.sqrt(dx * dx + dz * dz);
   }
}
