package ru.mentions.module.impl.combat;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1422;
import net.minecraft.class_1425;
import net.minecraft.class_1429;
import net.minecraft.class_1431;
import net.minecraft.class_1433;
import net.minecraft.class_1454;
import net.minecraft.class_1462;
import net.minecraft.class_1474;
import net.minecraft.class_1477;
import net.minecraft.class_1481;
import net.minecraft.class_1531;
import net.minecraft.class_1569;
import net.minecraft.class_1621;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_5762;
import net.minecraft.class_5776;
import net.minecraft.class_746;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventMoveInput;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.VisualRotEvent;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.combat.aura.RotationType;
import ru.mentions.module.impl.utils.FakePlayer;
import ru.mentions.screen.ClickGuiScreen;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.aura.AttackUtil;
import ru.mentions.utils.aura.AuraUtil;
import ru.mentions.utils.aura.UAttack;
import ru.mentions.utils.aura.UBoxPoints;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.other.TimerUtil;
import ru.mentions.utils.player.MoveUtil;
import ru.mentions.utils.render.RenderUtil;

@ModuleInfo(
   name = "Attack Aura",
   desc = "хуячит всем по ебалу мечом",
   category = Category.COMBAT,
   key = 82
)
@Environment(EnvType.CLIENT)
public class AttackAura extends Module {
   public SliderSetting attackRange = new SliderSetting(this, "Радиус атаки", 3.0F, 2.5F, 6.0F, 0.1F);
   public SliderSetting preRange = new SliderSetting(this, "Радиус обнаружения", 1.0F, 0.0F, 3.0F, 0.1F);
   public MultiBooleanSetting targets = new MultiBooleanSetting(
      this,
      "Кого атаковать",
      new BooleanSetting("Игроков", true),
      new BooleanSetting("Голых игроков", true),
      new BooleanSetting("Мобов", false),
      new BooleanSetting("Друзей", false)
   );
   public ModeSetting typeRotation = new ModeSetting(
      this,
      "Ротация",
      "SpookyTime-new",
      "Funtime-new",
      "FunTime-bypass",
      "FunTime",
      "ReallyWorld",
      "Spooky Duels",
      "Default",
      "Snap",
      "HvH",
      "Space Time",
      "SpookyTime Limit",
      "Neuro Aura",
      "Custom Aura",
      "Focused-de",
      "Polar-de",
      "Style-de",
      "New-de",
      "Neural-de",
      "Shard AC"
   );
   public ModeSetting typeSnap = new ModeSetting(this, "Режим снапа", "360", "Fov").setVisible(() -> this.typeRotation.is("Snap"));
   public SliderSetting fov = new SliderSetting(this, "Fov", 50.0F, 25.0F, 90.0F, 1.0F)
      .setVisible(() -> this.typeRotation.is("Snap") && this.typeSnap.is("Fov"));
   public BooleanSetting fovRender = new BooleanSetting(this, "Отображать Fov", false)
      .setVisible(() -> this.typeRotation.is("Snap") && this.typeSnap.is("Fov"));
   public SliderSetting deFocusedMaxDeg = new SliderSetting(this, "Max °/тик (Focused-de)", 0.0F, 0.0F, 180.0F, 1.0F)
      .setVisible(() -> this.typeRotation.is("Focused-de"));
   public ModeSetting deNewDirection = new ModeSetting(this, "New Direction", "Компенсация", "Назад", "Втягивание")
      .setVisible(() -> this.typeRotation.is("New-de"));
   public ModeSetting deNewCorrection = new ModeSetting(this, "New Correction", "Таргет", "Преследование", "Сфокусированная")
      .setVisible(() -> this.typeRotation.is("New-de"));
   public BooleanSetting deNewKeepDistance = new BooleanSetting(this, "New Keep Distance", false)
      .setVisible(() -> this.typeRotation.is("New-de") && !this.deNewCorrection.is("Сфокусированная"));
   public SliderSetting deNewMinDistance = new SliderSetting(this, "New Min Distance", 0.9F, 0.3F, 2.0F, 0.05F)
      .setVisible(() -> this.typeRotation.is("New-de") && !this.deNewCorrection.is("Сфокусированная") && this.deNewKeepDistance.getValue());
   public ModeSetting deStyleEngine = new ModeSetting(this, "Style Engine", "Library", "Param", "Neural").setVisible(() -> this.typeRotation.is("Style-de"));
   public SliderSetting deStyleReverse = new SliderSetting(this, "Style Reverse", 0.3F, 0.0F, 1.0F, 0.05F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Library"));
   public SliderSetting deReplaySpeed = new SliderSetting(this, "Replay Speed", 2.5F, 1.0F, 4.0F, 0.1F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Library"));
   public SliderSetting deYawLock = new SliderSetting(this, "Yaw Lock", 1.0F, 0.0F, 1.0F, 0.05F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Library"));
   public SliderSetting dePitchDamp = new SliderSetting(this, "Pitch Damp", 0.3F, 0.1F, 1.0F, 0.05F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Library"));
   public SliderSetting deShake = new SliderSetting(this, "Shake", 1.2F, 0.0F, 8.0F, 0.1F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Library"));
   public SliderSetting deParamGain = new SliderSetting(this, "Param Gain", 0.3F, 0.05F, 1.0F, 0.05F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Param"));
   public SliderSetting deParamArc = new SliderSetting(this, "Param Arc", 0.3F, 0.0F, 0.8F, 0.05F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Param"));
   public SliderSetting deParamJitter = new SliderSetting(this, "Param Jitter", 0.5F, 0.0F, 3.0F, 0.1F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deStyleEngine.is("Param"));
   public BooleanSetting deMultipoint = new BooleanSetting(this, "Multipoint", true).setVisible(() -> this.typeRotation.is("Style-de"));
   public SliderSetting deMultipointStrength = new SliderSetting(this, "Multipoint Strength", 0.6F, 0.1F, 1.0F, 0.05F)
      .setVisible(() -> this.typeRotation.is("Style-de") && this.deMultipoint.getValue());
   public BooleanSetting deNeuralFreeze = new BooleanSetting(this, "Neural Freeze", true).setVisible(() -> this.typeRotation.is("Neural-de"));
   public ModeSetting typeSprint = new ModeSetting(this, "Тип спринта", "Packet", "Silent", "Legit");
   public ModeSetting typeMove = new ModeSetting(this, "Коррекция движения", "Сфокусированная", "Свободная");
   public ModeSetting typeTargetESP = new ModeSetting(
      this,
      "Отображение таргета",
      "Картинка",
      "Духи",
      "Кольцо",
      "Кубики",
      "Куб",
      "Система",
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
   public ModeSetting sysEffect = new ModeSetting(this, "Эффект", "Кольцо", "Треугольник", "Молния", "Спираль", "Орбита", "Угли", "Руны", "Пульс")
      .setVisible(() -> this.typeTargetESP.is("Система"));
   public SliderSetting sysRadius = new SliderSetting(this, "Радиус", 0.7F, 0.4F, 1.6F, 0.05F).setVisible(this::isParticleTargetEsp);
   public SliderSetting sysSize = new SliderSetting(this, "Размер частиц", 0.17F, 0.04F, 0.4F, 0.01F).setVisible(this::isParticleTargetEsp);
   public SliderSetting sysDensity = new SliderSetting(this, "Плотность", 60.0F, 10.0F, 120.0F, 5.0F).setVisible(this::isParticleTargetEsp);
   public SliderSetting sysSpeed = new SliderSetting(this, "Скорость", 0.7F, 0.1F, 2.0F, 0.1F).setVisible(this::isParticleTargetEsp);
   public SliderSetting cubeSize = new SliderSetting(this, "Размер куба", 1.15F, 0.6F, 2.5F, 0.05F).setVisible(() -> this.typeTargetESP.is("Куб"));
   public SliderSetting cubeRotateSpeed = new SliderSetting(this, "Скорость вращения куба", 1.2F, 0.2F, 4.0F, 0.05F)
      .setVisible(() -> this.typeTargetESP.is("Куб"));
   public SliderSetting cubeThickness = new SliderSetting(this, "Толщина куба", 2.2F, 0.6F, 5.0F, 0.1F).setVisible(() -> this.typeTargetESP.is("Куб"));
   public BooleanSetting cubeColorOnHit = new BooleanSetting(this, "Цвет куба при ударе", true).setVisible(() -> this.typeTargetESP.is("Куб"));
   public BooleanSetting cubeGlow = new BooleanSetting(this, "Свечение куба", true).setVisible(() -> this.typeTargetESP.is("Куб"));
   public MultiBooleanSetting others = new MultiBooleanSetting(
      this, "Доп. проверки", new BooleanSetting("Умные криты", false), new BooleanSetting("Бить через блоки", false)
   );
   public MultiBooleanSetting noattackto = new MultiBooleanSetting(
      this, "Не бить если", new BooleanSetting("Используешь еду", false), new BooleanSetting("Открыт контейнер", false)
   );
   public BooleanSetting autoMace = new BooleanSetting(this, "Auto Mace", false);
   public SliderSetting maceDistanceToGround = new SliderSetting(this, "Высота для булавы", 2.5F, 0.5F, 10.0F, 0.1F).setVisible(() -> this.autoMace.getValue());
   public SliderSetting maceStrikeHeight = new SliderSetting(this, "Удар булавой (высота)", 1.5F, 0.6F, 3.0F, 0.1F)
      .setVisible(() -> this.autoMace.getValue());
   public SliderSetting maceSwapBackDelay = new SliderSetting(this, "Задержка возврата булавы", 0.5F, 0.1F, 2.0F, 0.05F)
      .setVisible(() -> this.autoMace.getValue());
   private int macePreviousSlot = -1;
   private boolean maceEquipped = false;
   private boolean maceLanded = false;
   private boolean maceHitDone = false;
   private long maceLandTime = 0L;
   private double maceLastGroundY = 0.0;
   private double macePeakY = 0.0;
   private double maceLastVy = 0.0;
   private boolean maceWindBoosted = false;
   public static class_1309 target = null;
   public static int stoptick = 0;
   public static long lastLookUpTime = 0L;
   public static long nextLookUpDelay = ThreadLocalRandom.current().nextLong(90000L, 180000L);
   public static boolean isLookingUp = false;
   public static long lookUpStartTime = 0L;
   public static int lookUpDuration = 0;
   public static long lastAttackTime = 0L;
   public float tick = 0.0F;
   public static float lastYaw;
   public static float lastPitch;
   public class_243 targetPoint;
   public class_243 currentPoint;
   private int count;
   public boolean justAttacked = false;
   public long attackFlickAt = 0L;
   private int hitCount = 0;
   private int pitchFlickThreshold = ThreadLocalRandom.current().nextInt(14, 19);
   public boolean pitchFlickActive = false;
   public long pitchFlickEndTime = 0L;
   public TimerUtil timeSped1 = new TimerUtil();
   public TimerUtil timeSped2 = new TimerUtil();
   private static final RenderPipeline CUBE_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField56860()})
         .withLocation(class_2960.method_60655("client", "pipeline/aura_target_cube"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final class_1921 CUBE_LAYER = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940(
      "aura_target_cube", class_12247.method_75927(CUBE_PIPELINE).method_75937().method_75929(16384).method_75938()
   );
   private float cubeRotationY;
   private float cubeRotationX;
   private float cubeTargetY;
   private float cubeTargetX;
   private float cubeHitFlash;
   private int cubePrevHurtTime;
   private long cubeLastUpdate = System.currentTimeMillis();
   private class_1309 cubeTrackedTarget;
   private class_243 cubeLastCenter;
   private float cubeLastHalf = 0.6F;
   private double cubeLastFeetY;
   private boolean cubeShattered;
   private final List<AttackAura.CubeFragment> cubeFragments = new ArrayList<>();
   private final Random cubeRandom = new Random();
   private Matrix4f cubeMatrix;

   public boolean isParticleTargetEsp() {
      return this.typeTargetESP.is("Система")
         || this.typeTargetESP.is("Ореол")
         || this.typeTargetESP.is("Треугольник")
         || this.typeTargetESP.is("Молния")
         || this.typeTargetESP.is("Спираль")
         || this.typeTargetESP.is("Орбита")
         || this.typeTargetESP.is("Угли")
         || this.typeTargetESP.is("Руны")
         || this.typeTargetESP.is("Пульс");
   }

   public static AttackAura get() {
      return Instance.get(AttackAura.class);
   }

   public float[] getRanges() {
      return new float[]{this.attackRange.getValue(), this.preRange.getValue()};
   }

   @EventHandler
   public void onEvent(EventUpdate e) {
      this.handleAutoMace();
      class_1309 prevTarget = target;
      if (target == null || !this.isValidTarget(target)) {
         this.updateTarget();
      }

      if (stoptick != 0) {
         stoptick--;
      }

      if (!this.checkToAttack() && target != null) {
         this.attackEntity();
      }
   }

   private int findMaceSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_31574(class_1802.field_49814)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static double getDistanceToGround(class_1309 entity) {
      if (entity != null && mc.field_1687 != null) {
         double x = entity.method_23317();
         double y = entity.method_23318();
         double z = entity.method_23321();

         for (double dy = 0.0; dy <= 30.0; dy += 0.25) {
            class_2338 pos = new class_2338((int)Math.floor(x), (int)Math.floor(y - dy), (int)Math.floor(z));
            class_2680 state = mc.field_1687.method_8320(pos);
            if (!state.method_26215()) {
               return dy;
            }
         }

         return 30.0;
      } else {
         return 0.0;
      }
   }

   private boolean isMaceInHand() {
      return mc.field_1724 != null && !mc.field_1724.method_6047().method_7960() && mc.field_1724.method_6047().method_31574(class_1802.field_49814);
   }

   private void releaseMace() {
      if (this.maceEquipped && this.macePreviousSlot != -1 && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.macePreviousSlot);
         ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
      }

      this.maceEquipped = false;
      this.maceLanded = false;
      this.macePreviousSlot = -1;
   }

   private void tryMaceStrike() {
      if (target == null || mc.field_1761 == null || mc.field_1724 == null) {
         return;
      }

      if (AuraUtil.getStrictDistance(target) >= (double)this.attackRange.getValue().floatValue()) {
         return;
      }

      UAttack.useEntity(target, null, null, class_1268.field_5808, false);
      this.justAttacked = true;
      this.attackFlickAt = System.currentTimeMillis() + 250L;
      this.maceHitDone = true;
      this.maceLanded = true;
      this.maceLandTime = System.currentTimeMillis();
   }

   private void handleAutoMace() {
      if (!this.autoMace.getValue() || mc.field_1724 == null) {
         this.releaseMace();
         this.maceWindBoosted = false;
         this.maceHitDone = false;
         return;
      }

      boolean onGround = mc.field_1724.method_24828();
      double currentY = mc.field_1724.method_23318();
      double vy = mc.field_1724.method_18798().field_1351;
      if (vy >= 0.55 || vy - this.maceLastVy >= 0.5 && vy > 0.2) {
         this.maceWindBoosted = true;
      }

      if (onGround) {
         this.maceLastGroundY = currentY;
         this.macePeakY = currentY;
         this.maceWindBoosted = false;
         this.maceHitDone = false;
         if (this.maceEquipped) {
            if (!this.maceLanded) {
               this.maceLanded = true;
               this.maceLandTime = System.currentTimeMillis();
            }

            long delayMs = (long)(this.maceSwapBackDelay.getValue() * 1000.0F);
            if (System.currentTimeMillis() - this.maceLandTime >= delayMs) {
               this.releaseMace();
            }
         }

         this.maceLastVy = vy;
         return;
      }

      if (currentY > this.macePeakY) {
         this.macePeakY = currentY;
      }

      // ще летимо вгору — тримаємо слот і чекаємо пік
      if (vy >= -0.1) {
         this.maceLastVy = vy;
         return;
      }

      double distanceToGround = getDistanceToGround(mc.field_1724);
      double fallFromPeak = this.macePeakY - currentY;
      double effectiveFall = Math.max(fallFromPeak, mc.field_1724.field_6017);
      double jumpHeight = this.macePeakY - this.maceLastGroundY;
      boolean smashFall = this.maceWindBoosted || effectiveFall >= 1.5 && jumpHeight >= 1.6;
      if (!smashFall) {
         this.maceLastVy = vy;
         return;
      }

      float strikeHeight = this.maceStrikeHeight.getValue();
      double fallSpeed = Math.max(-vy, 0.0);
      double equipHeight = Math.max((double)this.maceDistanceToGround.getValue().floatValue(), (double)strikeHeight + fallSpeed + 0.25);
      double nextTickDistance = distanceToGround - fallSpeed;

      // б'ємо рівно один раз за падіння: коли до землі лишилось ~strikeHeight блоків
      if (this.maceEquipped
         && !this.maceHitDone
         && this.isMaceInHand()
         && (distanceToGround <= (double)strikeHeight || nextTickDistance <= (double)strikeHeight)) {
         this.tryMaceStrike();
      }

      if (!this.maceEquipped && distanceToGround <= equipHeight) {
         int maceSlot = this.findMaceSlot();
         int currentSlot = mc.field_1724.method_31548().method_67532();
         if (maceSlot != -1 && maceSlot != currentSlot) {
            this.macePreviousSlot = currentSlot;
            mc.field_1724.method_31548().method_61496(maceSlot);
            ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
            this.maceEquipped = true;
            this.maceLanded = false;
            this.maceHitDone = false;
         }
      }

      this.maceLastVy = vy;
   }

   private boolean checkToAttack() {
      boolean baseCheck = mc.field_1724.method_6115() && this.noattackto.getValue("Используешь еду");
      boolean screenCheck = this.noattackto.getValue("Открыт контейнер") && mc.field_1755 != null && !(mc.field_1755 instanceof ClickGuiScreen);
      return baseCheck || screenCheck;
   }

   public void attackEntity() {
      // під час замаху булавою звичайна аура не добиває — удар робить tryMaceStrike()
      if (this.autoMace.getValue() && this.maceEquipped && this.isMaceInHand()) {
         return;
      }

      if (!(AuraUtil.getStrictDistance(target) >= (double)this.attackRange.getValue().floatValue())) {
         float[] ranges = this.getRanges();
         ranges = new float[]{ranges[0], ranges[1], ranges[0] + ranges[1]};
         if (target != null) {
            UAttack.antiMissesHittingUpdate(target, false, true, false);
            boolean canAttack = UAttack.shouldAttack(target, !this.typeRotation.is("HvH"), true, true, 0L, ranges);
            if (canAttack) {
               Runnable[] shieldBreak = UAttack.hitShieldBreakTaskForUse(target, true);
               Runnable[] shieldPressBypass = UAttack.resetShieldSilentTaskForUse(true);
               Runnable[] skipSilentSprint = UAttack.skipSilentSprintingTaskForUse(this.typeSprint.is("Packet") || this.typeSprint.is("Silent"));
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
               UAttack.useEntity(target, preHitSendCodeSingleTick, postHitSendCodeSingleTick, class_1268.field_5808, false);
               this.justAttacked = true;
               this.attackFlickAt = System.currentTimeMillis() + 250L;
               this.count = (this.count + 1) % 2;
               this.hitCount++;
               if (this.hitCount >= this.pitchFlickThreshold) {
                  this.hitCount = 0;
                  this.pitchFlickThreshold = ThreadLocalRandom.current().nextInt(15, 19);
                  this.pitchFlickEndTime = 0L;
               }
            }
         }
      }
   }

   @EventHandler
   private void setCorrection(EventMoveInput eventMoveInput) {
      if (target != null
         && mc.field_1724 != null
         && mc.field_1687 != null
         && UAttack.resetSprintTick(target, this.getRanges())
         && target != null
         && !AttackUtil.hasMovementRestrictions()
         && this.typeSprint.is("Legit")) {
         eventMoveInput.setForward(0.0F);
         eventMoveInput.setStrafe(0.0F);
      }

      if (target != null && mc.field_1724 != null && mc.field_1687 != null && this.typeMove.is("Свободная")) {
         MoveUtil.fixMovement(eventMoveInput, mc.field_1724.method_36454(), mc.field_1773.method_19418().method_19330());
      }

      if (target != null
         && mc.field_1724 != null
         && mc.field_1687 != null
         && this.typeMove.is("Сфокусированная")
         && ServerUtil.isFunTime()
         && !mc.field_1724.method_5869()) {
         if (ServerUtil.isCopyTime()) {
            class_243 vec3d = AuraUtil.getVector2(target);
            float yaw = (float)Math.toDegrees(Math.atan2(-vec3d.field_1352, vec3d.field_1350));
            MoveUtil.fixMovement(eventMoveInput, mc.field_1724.method_36454(), yaw);
         } else {
            class_243 vec = target.method_73189().method_1020(mc.field_1724.method_33571()).method_1029();
            float yaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
            MoveUtil.fixMovement(eventMoveInput, mc.field_1724.method_36454(), yaw);
         }
      }

      if (target != null && mc.field_1724 != null && mc.field_1687 != null && this.typeMove.is("Сфокусированная") && !ServerUtil.isFunTime()) {
         if (ServerUtil.isCopyTime()) {
            class_243 vec3d = AuraUtil.getVector2(target);
            float yaw = (float)Math.toDegrees(Math.atan2(-vec3d.field_1352, vec3d.field_1350));
            MoveUtil.fixMovement(eventMoveInput, mc.field_1724.method_36454(), yaw);
         } else {
            class_243 vec = target.method_73189().method_1020(mc.field_1724.method_33571()).method_1029();
            float yaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
            MoveUtil.fixMovement(eventMoveInput, mc.field_1724.method_36454(), yaw);
         }
      }
   }

   @EventHandler
   public void onRotate(EventTick e) {
      if (target != null && mc.field_1724 != null && mc.field_1687 != null) {
         float[] ranges = this.getRanges();
         ranges = new float[]{ranges[0], ranges[1], ranges[0] + ranges[1]};
         boolean canAttack = UAttack.shouldAttack(target, false, true, true, -350L, ranges);

         for (RotationType type : RotationType.values()) {
            if (this.typeRotation.is(type.getName())) {
               type.getRotation().onRotation(this, target, ranges, canAttack);
               break;
            }
         }

         if (this.typeRotation.is("FunTime")) {
            class_243 vec3d = UBoxPoints.getBestVector3dOnEntityBox(target.method_5829(), false)
               .method_1031(
                  0.2F * Math.sin((double)System.currentTimeMillis() / 250.0),
                  (double)(target.method_17682() / 6.0F) * Math.cos((double)System.currentTimeMillis() / 850.0),
                  0.0
               )
               .method_1020(mc.field_1724.method_33571());
            long currentTime = System.currentTimeMillis();
            if (!isLookingUp && currentTime - lastLookUpTime >= nextLookUpDelay) {
               isLookingUp = true;
               lookUpStartTime = currentTime;
               lookUpDuration = ThreadLocalRandom.current().nextInt(300, 400);
               lastLookUpTime = currentTime;
               nextLookUpDelay = ThreadLocalRandom.current().nextLong(9100L, 11200L);
            }

            boolean fastspeed = false;
            if (isLookingUp && currentTime - lookUpStartTime >= (long)lookUpDuration) {
               isLookingUp = false;
            }

            if (currentTime - lookUpStartTime >= (long)lookUpDuration + 70L) {
               fastspeed = true;
            }

            float yaw = (float)Math.toDegrees(Math.atan2(-vec3d.field_1352, vec3d.field_1350));
            float pitch = (float)class_3532.method_15350(
               -Math.toDegrees(Math.atan2(vec3d.field_1351, Math.hypot(vec3d.field_1352, vec3d.field_1350))), -90.0, 90.0
            );
            float speedYAW = MathUtil.randomLerp(44.0F, 66.0F);
            float speedPit = !fastspeed ? MathUtil.randomLerp(120.0F, 170.0F) : MathUtil.randomLerp(4.0F, 7.0F);
            if (canAttack && fastspeed) {
               this.tick = MathUtil.random(0.0, 2.0);
            }

            boolean attack = false;
            if (this.tick > 0.0F) {
               attack = true;
               this.tick--;
            }

            float randomAttackShift = 0.0F;
            float waveA = (float)Math.cos((double)System.currentTimeMillis() / 70.0);
            float waveB = (float)Math.sin((double)System.currentTimeMillis() / 110.0);
            if (attack) {
               lastYaw = yaw;
               lastPitch = pitch;
            }

            float yawJitter = waveA * MathUtil.randomLerp(6.0F, 15.0F);
            float pitchJitter = waveB * MathUtil.randomLerp(6.0F, 12.0F);
            float finalPitch = isLookingUp ? -MathUtil.randomLerp(85.0F, 90.0F) : lastPitch;
            RotationProcess.update(
               new Rotation(lastYaw + yawJitter, finalPitch + pitchJitter),
               speedYAW,
               speedPit,
               (float)MathUtil.randomInt(35, 45),
               (float)MathUtil.randomInt(19, 45),
               MathUtil.randomInt(0, 3),
               15,
               false
            );
         }
      }
   }

   @EventHandler
   private void onResetOnWorld(WorldLoadEvent event) {
      if (mc.field_1724 != null && mc.field_1773.method_19418() != null && mc.field_1687 != null) {
         lastPitch = mc.field_1773.method_19418().method_19329();
         lastYaw = mc.field_1773.method_19418().method_19330();
      }

      target = null;
      this.targetPoint = null;
      this.currentPoint = null;
      this.hitCount = 0;
      this.pitchFlickActive = false;
      this.maceEquipped = false;
      this.maceLanded = false;
      this.maceHitDone = false;
      this.macePreviousSlot = -1;
      this.maceWindBoosted = false;
      this.maceWindBoosted = false;
      this.resetCubeState();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.releaseMace();
      this.maceHitDone = false;
      this.maceWindBoosted = false;
      if (mc.field_1724 != null && mc.field_1773.method_19418() != null && mc.field_1687 != null) {
         lastPitch = mc.field_1773.method_19418().method_19329();
         lastYaw = mc.field_1773.method_19418().method_19330();
      }

      target = null;
      this.targetPoint = null;
      this.currentPoint = null;
      this.hitCount = 0;
      this.pitchFlickActive = false;
      RotationProcess.releaseRotation();
      this.resetCubeState();
   }

   private void updateTarget() {
      if (FakePlayer.fakePlayer != null && FakePlayer.fakePlayer.method_5805() && mc.field_1724.method_5739(FakePlayer.fakePlayer) <= this.auraDist()) {
         target = FakePlayer.fakePlayer;
      } else {
         class_1309 bestTarget = null;
         double bestAngle = Double.MAX_VALUE;
         class_243 eyePos = mc.field_1724.method_33571();
         class_243 lookVec = mc.field_1724.method_5828(1.0F).method_1029();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            double angle;
            class_1309 living;
            if (entity instanceof class_1309
               && this.isValidTarget(living = (class_1309)entity)
               && (
                     angle = Math.acos(
                        class_3532.method_15350(
                           lookVec.method_1026(
                              living.method_73189().method_1031(0.0, (double)living.method_17682() * 0.5, 0.0).method_1020(eyePos).method_1029()
                           ),
                           -1.0,
                           1.0
                        )
                     )
                  )
                  < bestAngle) {
               bestAngle = angle;
               bestTarget = living;
            }
         }

         target = bestTarget;
      }
   }

   private float auraDist() {
      return this.attackRange.getValue() + this.preRange.getValue();
   }

   public double getTargetFov(class_1309 entity) {
      class_243 targetPos = entity.method_73189().method_1031(0.0, (double)entity.method_17682() * 0.5, 0.0);
      class_243 vec = targetPos.method_1020(mc.field_1724.method_33571());
      float yaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
      float pitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))), -90.0, 90.0);
      float yawDelta = class_3532.method_15393(yaw - Rotation.cameraYaw());
      float pitchDelta = pitch - Rotation.cameraPitch();
      return Math.hypot((double)yawDelta, (double)pitchDelta);
   }

   private boolean isValidTarget(class_1309 entity) {
      if (entity instanceof class_746) {
         return false;
      } else if (mc.field_1724.method_5739(entity) > this.auraDist()) {
         return false;
      } else if (!this.others.getValue("Бить через блоки") && !mc.field_1724.method_6057(entity)) {
         return false;
      } else {
         if (entity instanceof class_1657 p) {
            if (AntiBot.getInstance().isBot(p)) {
               return false;
            }

            if (Client.get().friendManager().isFriend(p.method_5477().getString()) && !this.targets.getValue("Друзей")) {
               return false;
            }
         }

         if (entity instanceof class_1657 && !this.targets.getValue("Игроков")) {
            return false;
         } else if (entity instanceof class_1657 && entity.method_18396() <= 0.0F && !this.targets.getValue("Голых игроков")) {
            return false;
         } else if (entity instanceof class_1657 && ((class_1657)entity).method_68878()) {
            return false;
         } else {
            return (
                     entity instanceof class_1569
                        || entity instanceof class_1621
                        || entity instanceof class_1646
                        || entity instanceof class_1429
                        || entity instanceof class_1422
                        || entity instanceof class_1425
                        || entity instanceof class_1431
                        || entity instanceof class_1462
                        || entity instanceof class_1474
                        || entity instanceof class_1454
                        || entity instanceof class_1477
                        || entity instanceof class_5776
                        || entity instanceof class_1433
                        || entity instanceof class_1481
                        || entity instanceof class_5762
                  )
                  && !this.targets.getValue("Мобов")
               ? false
               : !entity.method_5655() && entity.method_5805() && !(entity instanceof class_1531);
         }
      }
   }

   @EventHandler
   public void onDisplay(VisualRotEvent e) {
   }

   @EventHandler
   public void onDisplay(EventDisplay e) {
      if (mc.field_1724 != null && mc.field_1687 != null && target != null) {
         if (!this.typeRotation.is("Snap") || !this.typeSnap.is("Fov") || !this.fovRender.getValue() || mc.field_1724 == null || mc.field_1687 == null) {
            return;
         }

         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         float scaleFix = targetScale / currentScale;
         int screenWidth = (int)((float)mc.method_22683().method_4486() / scaleFix);
         int screenHeight = (int)((float)mc.method_22683().method_4502() / scaleFix);
         float screenW = (float)screenWidth;
         float screenH = (float)screenHeight;
         float cx = screenW / 2.0F;
         float cy = screenH / 2.0F;
         float m00 = RenderUtil.Render3D.lastProjMat.m00();
         int hurtTicks = target.field_6235;
         float hurtPC = (float)Math.sin((double)hurtTicks * (Math.PI / 10));
         int redColor = ColorUtil.getColor(255, 100, 100, 255);
         float pixelRadius = (float)(Math.tan(Math.toRadians((double)(this.fov.getValue() - 15.0F))) * (double)(screenW / 2.0F) * (double)m00);
         float diameter = pixelRadius * 2.0F;
         float x = cx - pixelRadius;
         float y = cy - pixelRadius;
         boolean hasTarget = target != null;
         int baseAlpha = 255;
         int color = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.getColor(255), 255.0F), redColor, hurtPC);
         RenderUtil.Render2D.outline(x, y, diameter, diameter, 1.0F, ColorUtil.replAlpha(color, 0.1F), pixelRadius);
      }
   }

   @EventHandler
   public void onCubeTick(EventTick event) {
      if (this.typeTargetESP.is("Куб")) {
         this.updateCubeFragments();
         class_1309 trackedTarget = this.cubeTrackedTarget;
         if (trackedTarget != null) {
            boolean dead = trackedTarget.method_31481() || trackedTarget.method_6032() <= 0.0F || trackedTarget.method_29504();
            if (dead) {
               if (!this.cubeShattered && this.cubeLastCenter != null) {
                  this.shatterCube();
               }

               this.cubeShattered = true;
               this.cubeTrackedTarget = null;
               this.cubePrevHurtTime = 0;
            } else {
               int hurt = trackedTarget.field_6235;
               if (hurt > this.cubePrevHurtTime) {
                  int quartersY = 1 + this.cubeRandom.nextInt(3);
                  int quartersX = this.cubeRandom.nextInt(2);
                  float directionY = this.cubeRandom.nextBoolean() ? 1.0F : -1.0F;
                  float directionX = this.cubeRandom.nextBoolean() ? 1.0F : -1.0F;
                  this.cubeTargetY += directionY * 90.0F * (float)quartersY;
                  this.cubeTargetX += directionX * 90.0F * (float)quartersX;
                  this.cubeHitFlash = 1.0F;
               }

               this.cubePrevHurtTime = hurt;
            }
         }
      }
   }

   public void resetCubeState() {
      this.cubeRotationY = 0.0F;
      this.cubeRotationX = 0.0F;
      this.cubeTargetY = 0.0F;
      this.cubeTargetX = 0.0F;
      this.cubeHitFlash = 0.0F;
      this.cubePrevHurtTime = 0;
      this.cubeLastUpdate = System.currentTimeMillis();
      this.cubeTrackedTarget = null;
      this.cubeLastCenter = null;
      this.cubeLastHalf = 0.6F;
      this.cubeLastFeetY = 0.0;
      this.cubeShattered = false;
      this.cubeFragments.clear();
      this.cubeMatrix = null;
   }

   private float[][] buildCubeArms(float half) {
      float armLength = half * 0.46F;
      float inner = half - armLength;
      float[][] arms = new float[24][];
      int index = 0;

      for (int sideX = -1; sideX <= 1; sideX += 2) {
         for (int sideY = -1; sideY <= 1; sideY += 2) {
            for (int sideZ = -1; sideZ <= 1; sideZ += 2) {
               float cornerX = (float)sideX * half;
               float cornerY = (float)sideY * half;
               float cornerZ = (float)sideZ * half;
               arms[index++] = new float[]{cornerX, cornerY, cornerZ, (float)sideX * inner, cornerY, cornerZ};
               arms[index++] = new float[]{cornerX, cornerY, cornerZ, cornerX, (float)sideY * inner, cornerZ};
               arms[index++] = new float[]{cornerX, cornerY, cornerZ, cornerX, cornerY, (float)sideZ * inner};
            }
         }
      }

      return arms;
   }

   public void renderTargetCube(EventRender3D event, class_1309 renderTarget, float alphaProgress, class_4598 immediate) {
      class_243 cameraPos = mc.field_1773.method_19418().method_71156();
      class_243 feetPos = renderTarget.method_30950(event.getTickDelta());
      float entityHeight = renderTarget.method_17682();
      float entityWidth = renderTarget.method_17681();
      if (renderTarget.method_5805()) {
         if (this.cubeTrackedTarget != renderTarget) {
            this.cubePrevHurtTime = renderTarget.field_6235;
         }

         this.cubeTrackedTarget = renderTarget;
         this.cubeShattered = false;
      }

      float appear;
      if (!((appear = (float)Easings.SINE_IN_OUT.ease((double)class_3532.method_15363(alphaProgress, 0.0F, 1.0F))) <= 0.001F)) {
         float half = (entityWidth * 0.5F + 0.08F) * this.cubeSize.getValue() * (0.55F + 0.45F * appear);
         class_243 center;
         this.cubeLastCenter = center = feetPos.method_1031(0.0, (double)entityHeight * 0.5, 0.0);
         this.cubeLastHalf = half;
         this.cubeLastFeetY = feetPos.field_1351;
         long now = System.currentTimeMillis();
         float deltaTime = class_3532.method_15363((float)(now - this.cubeLastUpdate) / 1000.0F, 0.0F, 0.1F);
         this.cubeLastUpdate = now;
         float ease = 1.0F - (float)Math.exp((double)(-(6.0F + this.cubeRotateSpeed.getValue() * 3.0F) * deltaTime));
         this.cubeRotationY = this.cubeRotationY + (this.cubeTargetY - this.cubeRotationY) * ease;
         this.cubeRotationX = this.cubeRotationX + (this.cubeTargetX - this.cubeRotationX) * ease;
         if (Math.abs(this.cubeTargetY - this.cubeRotationY) < 0.05F) {
            this.cubeRotationY = this.cubeTargetY;
         }

         if (Math.abs(this.cubeTargetX - this.cubeRotationX) < 0.05F) {
            this.cubeRotationX = this.cubeTargetX;
         }

         this.cubeHitFlash = Math.max(0.0F, this.cubeHitFlash - deltaTime * 2.2F);
         int baseColor;
         int color = baseColor = this.getCubeEspColor();
         if (this.cubeColorOnHit.getValue() && this.cubeHitFlash > 0.0F) {
            color = this.blendCubeColor(baseColor, ColorUtil.getColor(255, 25, 25, 255), this.cubeHitFlash);
         }

         color = this.multiplyCubeAlpha(this.replaceCubeAlpha(color, 190), appear);
         Quaternionf rotation = new Quaternionf()
            .rotateY((float)Math.toRadians((double)this.cubeRotationY))
            .rotateX((float)Math.toRadians((double)this.cubeRotationX));
         float coreHalf = this.getCubeDistanceScale(cameraPos, center.field_1352, center.field_1351, center.field_1350) * this.cubeThickness.getValue() * 0.5F;
         class_4588 consumer = immediate.method_73477(CUBE_LAYER);
         double relativeX = center.field_1352 - cameraPos.field_1352;
         double relativeY = center.field_1351 - cameraPos.field_1351;
         double relativeZ = center.field_1350 - cameraPos.field_1350;
         this.cubeMatrix = event.getMatrixStack().method_23760().method_23761();

         for (float[] arm : this.buildCubeArms(half)) {
            this.drawCubeEdgeBar(consumer, rotation, relativeX, relativeY, relativeZ, arm, coreHalf, color);
         }

         for (int sideX = -1; sideX <= 1; sideX += 2) {
            for (int sideY = -1; sideY <= 1; sideY += 2) {
               for (int sideZ = -1; sideZ <= 1; sideZ += 2) {
                  this.drawCornerCube(
                     consumer, rotation, relativeX, relativeY, relativeZ, (float)sideX * half, (float)sideY * half, (float)sideZ * half, coreHalf, color
                  );
               }
            }
         }

         immediate.method_22994(CUBE_LAYER);
         if (this.cubeGlow.getValue()) {
            this.renderCubeGlow(event, rotation, relativeX, relativeY, relativeZ, half, coreHalf, baseColor, appear, immediate);
         }
      }
   }

   private void renderCubeGlow(
      EventRender3D event,
      Quaternionf rotation,
      double relativeX,
      double relativeY,
      double relativeZ,
      float half,
      float coreHalf,
      int baseColor,
      float appear,
      class_4598 immediate
   ) {
      class_1921 glowLayer = RotationProcess.ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_2.png"));
      class_4588 glowConsumer = immediate.method_73477(glowLayer);
      class_4587 matrices = event.getMatrixStack();
      Quaternionf cameraRotation = mc.field_1773.method_19418().method_23767();
      int glowColor = this.multiplyCubeAlpha(this.replaceCubeAlpha(baseColor, 55), 0.45F * appear);
      float glowSize = coreHalf * 5.0F;

      for (float[] arm : this.buildCubeArms(half)) {
         for (int index = 0; index <= 2; index++) {
            float progress = (float)index * 0.5F;
            float localX = arm[0] + (arm[3] - arm[0]) * progress;
            float localY = arm[1] + (arm[4] - arm[1]) * progress;
            float localZ = arm[2] + (arm[5] - arm[2]) * progress;
            Vector3f point = rotation.transform(new Vector3f(localX, localY, localZ));
            this.drawCubeGlowSprite(
               glowConsumer,
               matrices,
               cameraRotation,
               relativeX + (double)point.x,
               relativeY + (double)point.y,
               relativeZ + (double)point.z,
               glowSize,
               glowColor
            );
         }
      }

      for (int sideX = -1; sideX <= 1; sideX += 2) {
         for (int sideY = -1; sideY <= 1; sideY += 2) {
            for (int sideZ = -1; sideZ <= 1; sideZ += 2) {
               Vector3f point = rotation.transform(new Vector3f((float)sideX * half, (float)sideY * half, (float)sideZ * half));
               this.drawCubeGlowSprite(
                  glowConsumer,
                  matrices,
                  cameraRotation,
                  relativeX + (double)point.x,
                  relativeY + (double)point.y,
                  relativeZ + (double)point.z,
                  glowSize * 1.2F,
                  glowColor
               );
            }
         }
      }

      immediate.method_22994(glowLayer);
   }

   private void drawCubeGlowSprite(class_4588 consumer, class_4587 matrices, Quaternionf cameraRotation, double x, double y, double z, float size, int color) {
      if ((color >> 24 & 0xFF) > 0) {
         matrices.method_22903();
         matrices.method_22904(x, y, z);
         matrices.method_22907(cameraRotation);
         Matrix4f matrix = matrices.method_23760().method_23761();
         this.cubeGlowVertex(consumer, matrix, -size, -size, 0.0F, 0.0F, 1.0F, color);
         this.cubeGlowVertex(consumer, matrix, size, -size, 0.0F, 1.0F, 1.0F, color);
         this.cubeGlowVertex(consumer, matrix, size, size, 0.0F, 1.0F, 0.0F, color);
         this.cubeGlowVertex(consumer, matrix, -size, size, 0.0F, 0.0F, 0.0F, color);
         matrices.method_22909();
      }
   }

   private void cubeGlowVertex(class_4588 consumer, Matrix4f matrix, float x, float y, float z, float u, float v, int color) {
      consumer.method_22918(matrix, x, y, z)
         .method_39415(color)
         .method_22913(u, v)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
   }

   private void drawCornerCube(
      class_4588 consumer,
      Quaternionf rotation,
      double relativeX,
      double relativeY,
      double relativeZ,
      float centerX,
      float centerY,
      float centerZ,
      float thickness,
      int color
   ) {
      if ((color >> 24 & 0xFF) > 0) {
         Vector3f n000 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY - thickness, centerZ - thickness)
         );
         Vector3f n100 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY - thickness, centerZ - thickness)
         );
         Vector3f n110 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY + thickness, centerZ - thickness)
         );
         Vector3f n010 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY + thickness, centerZ - thickness)
         );
         Vector3f n001 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY - thickness, centerZ + thickness)
         );
         Vector3f n101 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY - thickness, centerZ + thickness)
         );
         Vector3f n111 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY + thickness, centerZ + thickness)
         );
         Vector3f n011 = this.cubeLocalToRelative(
            rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY + thickness, centerZ + thickness)
         );
         this.faceCubeQuad(consumer, this.cubeMatrix, n000, n100, n110, n010, color);
         this.faceCubeQuad(consumer, this.cubeMatrix, n001, n011, n111, n101, color);
         this.faceCubeQuad(consumer, this.cubeMatrix, n000, n001, n101, n100, color);
         this.faceCubeQuad(consumer, this.cubeMatrix, n010, n110, n111, n011, color);
         this.faceCubeQuad(consumer, this.cubeMatrix, n000, n010, n011, n001, color);
         this.faceCubeQuad(consumer, this.cubeMatrix, n100, n101, n111, n110, color);
      }
   }

   private void drawCubeEdgeBar(
      class_4588 consumer, Quaternionf rotation, double relativeX, double relativeY, double relativeZ, float[] arm, float thickness, int color
   ) {
      if ((color >> 24 & 0xFF) > 0) {
         Vector3f start = new Vector3f(arm[0], arm[1], arm[2]);
         Vector3f end = new Vector3f(arm[3], arm[4], arm[5]);
         Vector3f direction = new Vector3f(end).sub(start);
         Vector3f secondNormal;
         Vector3f firstNormal;
         if (Math.abs(direction.x) > 1.0E-5F) {
            firstNormal = new Vector3f(0.0F, thickness, 0.0F);
            secondNormal = new Vector3f(0.0F, 0.0F, thickness);
         } else if (Math.abs(direction.y) > 1.0E-5F) {
            firstNormal = new Vector3f(thickness, 0.0F, 0.0F);
            secondNormal = new Vector3f(0.0F, 0.0F, thickness);
         } else {
            firstNormal = new Vector3f(thickness, 0.0F, 0.0F);
            secondNormal = new Vector3f(0.0F, thickness, 0.0F);
         }

         Vector3f start00 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(start).sub(firstNormal).sub(secondNormal));
         Vector3f start10 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(start).add(firstNormal).sub(secondNormal));
         Vector3f start11 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(start).add(firstNormal).add(secondNormal));
         Vector3f start01 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(start).sub(firstNormal).add(secondNormal));
         Vector3f end00 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(end).sub(firstNormal).sub(secondNormal));
         Vector3f end10 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(end).add(firstNormal).sub(secondNormal));
         Vector3f end11 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(end).add(firstNormal).add(secondNormal));
         Vector3f end01 = this.cubeLocalToRelative(rotation, relativeX, relativeY, relativeZ, new Vector3f(end).sub(firstNormal).add(secondNormal));
         this.emitCubeBox(consumer, start00, start10, start11, start01, end00, end10, end11, end01, color);
      }
   }

   private Vector3f cubeLocalToRelative(Quaternionf rotation, double relativeX, double relativeY, double relativeZ, Vector3f local) {
      rotation.transform(local);
      local.add((float)relativeX, (float)relativeY, (float)relativeZ);
      return local;
   }

   private void emitCubeBox(
      class_4588 consumer,
      Vector3f start00,
      Vector3f start10,
      Vector3f start11,
      Vector3f start01,
      Vector3f end00,
      Vector3f end10,
      Vector3f end11,
      Vector3f end01,
      int color
   ) {
      this.faceCubeQuad(consumer, this.cubeMatrix, start00, end00, end10, start10, color);
      this.faceCubeQuad(consumer, this.cubeMatrix, start10, end10, end11, start11, color);
      this.faceCubeQuad(consumer, this.cubeMatrix, start11, end11, end01, start01, color);
      this.faceCubeQuad(consumer, this.cubeMatrix, start01, end01, end00, start00, color);
   }

   private void faceCubeQuad(class_4588 consumer, Matrix4f matrix, Vector3f first, Vector3f second, Vector3f third, Vector3f fourth, int color) {
      consumer.method_22918(matrix, first.x, first.y, first.z).method_39415(color);
      consumer.method_22918(matrix, second.x, second.y, second.z).method_39415(color);
      consumer.method_22918(matrix, third.x, third.y, third.z).method_39415(color);
      consumer.method_22918(matrix, fourth.x, fourth.y, fourth.z).method_39415(color);
   }

   private void shatterCube() {
      float half = this.cubeLastHalf;
      class_243 center = this.cubeLastCenter;
      Quaternionf rotation = new Quaternionf()
         .rotateY((float)Math.toRadians((double)this.cubeRotationY))
         .rotateX((float)Math.toRadians((double)this.cubeRotationX));
      double groundY = this.cubeLastFeetY + 0.05;

      for (float[] arm : this.buildCubeArms(half)) {
         Vector3f start = rotation.transform(new Vector3f(arm[0], arm[1], arm[2]));
         Vector3f end = rotation.transform(new Vector3f(arm[3], arm[4], arm[5]));
         class_243 worldStart = center.method_1031((double)start.x, (double)start.y, (double)start.z);
         class_243 worldEnd = center.method_1031((double)end.x, (double)end.y, (double)end.z);
         class_243 fragmentCenter = worldStart.method_1019(worldEnd).method_1021(0.5);
         Vector3f offset = new Vector3f(
            (float)(worldEnd.field_1352 - fragmentCenter.field_1352),
            (float)(worldEnd.field_1351 - fragmentCenter.field_1351),
            (float)(worldEnd.field_1350 - fragmentCenter.field_1350)
         );
         class_243 outward = fragmentCenter.method_1020(center);
         if (outward.method_1027() < 1.0E-6) {
            outward = new class_243(this.cubeRandom.nextDouble() - 0.5, 0.0, this.cubeRandom.nextDouble() - 0.5);
         }

         outward = outward.method_1029();
         double speed = 0.12 + this.cubeRandom.nextDouble() * 0.14;
         class_243 velocity = outward.method_1021(speed)
            .method_1031((this.cubeRandom.nextDouble() - 0.5) * 0.06, 0.08 + this.cubeRandom.nextDouble() * 0.16, (this.cubeRandom.nextDouble() - 0.5) * 0.06);
         Vector3f axis = new Vector3f(this.cubeRandom.nextFloat() - 0.5F, this.cubeRandom.nextFloat() - 0.5F, this.cubeRandom.nextFloat() - 0.5F);
         if (axis.lengthSquared() < 1.0E-6F) {
            axis.set(0.0F, 1.0F, 0.0F);
         }

         axis.normalize();
         float angularSpeed = 0.12F + this.cubeRandom.nextFloat() * 0.28F;
         int maxAge = 50 + this.cubeRandom.nextInt(30);
         this.cubeFragments.add(new AttackAura.CubeFragment(fragmentCenter, offset, velocity, axis, angularSpeed, groundY, maxAge));
      }
   }

   private void updateCubeFragments() {
      Iterator<AttackAura.CubeFragment> iterator = this.cubeFragments.iterator();

      while (iterator.hasNext()) {
         AttackAura.CubeFragment fragment = iterator.next();
         fragment.previousCenter = fragment.center;
         fragment.previousOffset = new Vector3f(fragment.offset);
         fragment.center = fragment.center.method_1019(fragment.velocity);
         fragment.velocity = new class_243(fragment.velocity.field_1352 * 0.98, fragment.velocity.field_1351 - 0.028, fragment.velocity.field_1350 * 0.98);
         if (fragment.center.field_1351 <= fragment.groundY && fragment.velocity.field_1351 < 0.0) {
            fragment.center = new class_243(fragment.center.field_1352, fragment.groundY, fragment.center.field_1350);
            fragment.velocity = new class_243(fragment.velocity.field_1352 * 0.55, -fragment.velocity.field_1351 * 0.38, fragment.velocity.field_1350 * 0.55);
            fragment.angularSpeed *= 0.6F;
         }

         Quaternionf spin = new Quaternionf().fromAxisAngleRad(fragment.axis.x, fragment.axis.y, fragment.axis.z, fragment.angularSpeed);
         fragment.offset = spin.transform(new Vector3f(fragment.offset));
         fragment.age++;
         if (fragment.age >= fragment.maxAge) {
            iterator.remove();
         }
      }
   }

   public void renderTargetCubeFragments(EventRender3D event, class_4598 immediate) {
      if (!this.cubeFragments.isEmpty()) {
         class_243 cameraPos = mc.field_1773.method_19418().method_71156();
         int baseColor = this.getCubeEspColor();
         class_4588 consumer = immediate.method_73477(CUBE_LAYER);
         this.cubeMatrix = event.getMatrixStack().method_23760().method_23761();
         float delta = event.getTickDelta();

         for (AttackAura.CubeFragment fragment : this.cubeFragments) {
            float life = 1.0F - (float)fragment.age / (float)Math.max(1, fragment.maxAge);
            float fade = life > 0.3F ? 1.0F : Math.max(0.0F, life / 0.3F);
            if (!(fade <= 0.0F)) {
               class_243 center = new class_243(
                  class_3532.method_16436((double)delta, fragment.previousCenter.field_1352, fragment.center.field_1352),
                  class_3532.method_16436((double)delta, fragment.previousCenter.field_1351, fragment.center.field_1351),
                  class_3532.method_16436((double)delta, fragment.previousCenter.field_1350, fragment.center.field_1350)
               );
               Vector3f offset = new Vector3f(fragment.previousOffset).lerp(fragment.offset, delta);
               double relativeX = center.field_1352 - cameraPos.field_1352;
               double relativeY = center.field_1351 - cameraPos.field_1351;
               double relativeZ = center.field_1350 - cameraPos.field_1350;
               float coreHalf = this.getCubeDistanceScale(cameraPos, center.field_1352, center.field_1351, center.field_1350)
                  * this.cubeThickness.getValue()
                  * 0.5F;
               int color = this.multiplyCubeAlpha(this.replaceCubeAlpha(baseColor, 255), fade);
               this.drawCubeFragmentBar(
                  consumer,
                  (float)(relativeX - (double)offset.x),
                  (float)(relativeY - (double)offset.y),
                  (float)(relativeZ - (double)offset.z),
                  (float)(relativeX + (double)offset.x),
                  (float)(relativeY + (double)offset.y),
                  (float)(relativeZ + (double)offset.z),
                  coreHalf,
                  color
               );
            }
         }

         immediate.method_22994(CUBE_LAYER);
      }
   }

   private void drawCubeFragmentBar(
      class_4588 consumer, float startX, float startY, float startZ, float endX, float endY, float endZ, float thickness, int color
   ) {
      if ((color >> 24 & 0xFF) > 0) {
         Vector3f end = new Vector3f(endX, endY, endZ);
         Vector3f start = new Vector3f(startX, startY, startZ);
         Vector3f direction = new Vector3f(end).sub(start);
         if (!(direction.lengthSquared() < 1.0E-10F)) {
            direction.normalize();
            Vector3f reference = Math.abs(direction.y) < 0.99F ? new Vector3f(0.0F, 1.0F, 0.0F) : new Vector3f(1.0F, 0.0F, 0.0F);
            Vector3f firstNormal = new Vector3f();
            direction.cross(reference, firstNormal).normalize().mul(thickness);
            Vector3f secondNormal = new Vector3f();
            direction.cross(firstNormal, secondNormal).normalize().mul(thickness);
            Vector3f start00 = new Vector3f(start).sub(firstNormal).sub(secondNormal);
            Vector3f start10 = new Vector3f(start).add(firstNormal).sub(secondNormal);
            Vector3f start11 = new Vector3f(start).add(firstNormal).add(secondNormal);
            Vector3f start01 = new Vector3f(start).sub(firstNormal).add(secondNormal);
            Vector3f end00 = new Vector3f(end).sub(firstNormal).sub(secondNormal);
            Vector3f end10 = new Vector3f(end).add(firstNormal).sub(secondNormal);
            Vector3f end11 = new Vector3f(end).add(firstNormal).add(secondNormal);
            Vector3f end01 = new Vector3f(end).sub(firstNormal).add(secondNormal);
            this.emitCubeBox(consumer, start00, start10, start11, start01, end00, end10, end11, end01, color);
         }
      }
   }

   private float getCubeDistanceScale(class_243 cameraPos, double worldX, double worldY, double worldZ) {
      double deltaX = worldX - cameraPos.field_1352;
      double deltaY = worldY - cameraPos.field_1351;
      double deltaZ = worldZ - cameraPos.field_1350;
      double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
      return (float)Math.max(0.1, distance * 0.007);
   }

   private int getCubeEspColor() {
      int color = ColorUtil.getClientColor1(1);
      if ((color >> 24 & 0xFF) == 0) {
         color |= -16777216;
      }

      return color;
   }

   private int multiplyCubeAlpha(int color, float multiplier) {
      int alpha = class_3532.method_15340((int)((float)(color >> 24 & 0xFF) * multiplier), 0, 255);
      return alpha << 24 | color & 16777215;
   }

   private int replaceCubeAlpha(int color, int alpha) {
      return class_3532.method_15340(alpha, 0, 255) << 24 | color & 16777215;
   }

   private int blendCubeColor(int first, int second, float factor) {
      float clampedFactor = class_3532.method_15363(factor, 0.0F, 1.0F);
      int firstRed = first >> 16 & 0xFF;
      int firstGreen = first >> 8 & 0xFF;
      int firstBlue = first & 0xFF;
      int firstAlpha = first >> 24 & 0xFF;
      int secondRed = second >> 16 & 0xFF;
      int secondGreen = second >> 8 & 0xFF;
      int secondBlue = second & 0xFF;
      int secondAlpha = second >> 24 & 0xFF;
      int red = (int)((float)firstRed + (float)(secondRed - firstRed) * clampedFactor);
      int green = (int)((float)firstGreen + (float)(secondGreen - firstGreen) * clampedFactor);
      int blue = (int)((float)firstBlue + (float)(secondBlue - firstBlue) * clampedFactor);
      int alpha = (int)((float)firstAlpha + (float)(secondAlpha - firstAlpha) * clampedFactor);
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   @Environment(EnvType.CLIENT)
   private static class CubeFragment {
      private class_243 previousCenter;
      private class_243 center;
      private Vector3f previousOffset;
      private Vector3f offset;
      private class_243 velocity;
      private final Vector3f axis;
      private float angularSpeed;
      private final double groundY;
      private final int maxAge;
      private int age;

      private CubeFragment(class_243 center, Vector3f offset, class_243 velocity, Vector3f axis, float angularSpeed, double groundY, int maxAge) {
         this.previousCenter = center;
         this.center = center;
         this.previousOffset = new Vector3f(offset);
         this.offset = offset;
         this.velocity = velocity;
         this.axis = axis;
         this.angularSpeed = angularSpeed;
         this.groundY = groundY;
         this.maxAge = maxAge;
      }
   }
}
