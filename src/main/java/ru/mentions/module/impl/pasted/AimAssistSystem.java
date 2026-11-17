package ru.mentions.module.impl.pasted;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Aim Assist",
   desc = "Pasted:systemdlc | Плавная доводка прицела на цель из System DLC",
   category = Category.PASTED
)
@Environment(EnvType.CLIENT)
public class AimAssistSystem extends Module {
   public final ModeSetting rotation = new ModeSetting(this, "Тип доводки", "Linear", "Smooth", "Instant");
   public final SliderSetting speed = new SliderSetting(this, "Скорость", 3.0F, 1.0F, 10.0F, 0.1F);
   public final SliderSetting strength = new SliderSetting(this, "Сила помощи (%)", 100.0F, 0.0F, 100.0F, 1.0F);
   public final SliderSetting verticalFreedom = new SliderSetting(this, "Свобода по вертикали", 0.0F, -90.0F, 90.0F, 1.0F);
   public final ModeSetting behavior = new ModeSetting(this, "Поведение", "Legit", "Rage");
   public final SliderSetting slowThreshold = new SliderSetting(this, "Порог замедления", 0.4F, 0.0F, 3.0F, 0.05F);
   public final SliderSetting legitSpeed = new SliderSetting(this, "Легит делитель", 1.0F, 0.1F, 5.0F, 0.1F);
   public final BooleanSetting randomize = new BooleanSetting(this, "Рандомизация", false);
   public final ModeSetting priority = new ModeSetting(this, "Приоритет", "Distance", "Health", "Angle");
   public final SliderSetting distance = new SliderSetting(this, "Дистанция", 4.5F, 1.0F, 8.0F, 0.1F);
   public final SliderSetting fov = new SliderSetting(this, "FOV", 90.0F, 10.0F, 360.0F, 5.0F);
   public final BooleanSetting sticky = new BooleanSetting(this, "Сквозь стены", false);
   public final BooleanSetting lastHitOnly = new BooleanSetting(this, "Только при ударе", false);
   public final ModeSetting targets = new ModeSetting(this, "Цели", "Players", "Mobs", "All");
   public final ModeSetting conditions = new ModeSetting(this, "Условия", "None", "Visible", "Weapon");
   private final List<class_1309> candidates = new ArrayList<>();
   private class_1309 cachedTarget = null;
   private class_243 lastSelfPosition = null;
   private double requiredSpeed = 1.0;
   private boolean hasTarget = false;

   @Override
   public void onDisable() {
      this.cachedTarget = null;
      this.hasTarget = false;
      this.lastSelfPosition = null;
      super.onDisable();
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 == null || mc.field_1687 == null || !mc.field_1724.method_5805()) {
         this.cachedTarget = null;
         this.hasTarget = false;
      } else if (this.conditions.is("Weapon") && !this.isHoldingWeapon()) {
         this.cachedTarget = null;
         this.hasTarget = false;
      } else {
         class_243 selfPosition = mc.field_1724.method_33571();
         if (this.hasTarget && this.lastSelfPosition != null) {
            double travelled = selfPosition.method_1022(this.lastSelfPosition);
            this.requiredSpeed = travelled / Math.max(0.01, (double)this.legitSpeed.getValue().floatValue());
         } else {
            this.requiredSpeed = (double)this.speed.getValue().floatValue();
         }

         this.lastSelfPosition = selfPosition;
         class_1309 target = this.findTarget(selfPosition);
         if (target == null) {
            this.cachedTarget = null;
            this.hasTarget = false;
         } else {
            this.cachedTarget = target;
            class_243 targetPosition = target.method_33571();
            double dx = targetPosition.field_1352 - selfPosition.field_1352;
            double dy = targetPosition.field_1351 - selfPosition.field_1351;
            double dz = targetPosition.field_1350 - selfPosition.field_1350;
            double distXZ = Math.sqrt(dx * dx + dz * dz);
            float targetYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
            float currentYaw = mc.field_1724.method_36454();
            float currentPitch = mc.field_1724.method_36455();
            float deltaYaw = class_3532.method_15393(targetYaw - currentYaw);
            float deltaPitch = targetPitch - currentPitch;
            double str = (double)this.strength.getValue().floatValue() / 100.0;
            double stepYaw;
            double stepPitch;
            if (this.rotation.is("Instant")) {
               stepYaw = (double)deltaYaw * str;
               stepPitch = (double)deltaPitch * str;
            } else if (this.rotation.is("Linear")) {
               double rate = Math.max(0.1, (double)this.speed.getValue().floatValue());
               stepYaw = (double)Math.signum(deltaYaw) * Math.min((double)Math.abs(deltaYaw), rate) * str;
               stepPitch = (double)Math.signum(deltaPitch) * Math.min((double)Math.abs(deltaPitch), rate) * str;
            } else {
               double divisor = Math.max(this.requiredSpeed, 0.01);
               stepYaw = (double)deltaYaw / divisor * str;
               stepPitch = (double)deltaPitch / divisor * str;
            }

            if (this.randomize.getValue()) {
               stepYaw += (Math.random() - 0.5) * 0.3;
               stepPitch += (Math.random() - 0.5) * 0.3;
            }

            float freedom = Math.abs(this.verticalFreedom.getValue());
            if (freedom > 0.01F) {
               stepPitch = (double)Math.max(-freedom, Math.min(freedom, (float)stepPitch));
            }

            mc.field_1724.method_36456(currentYaw + (float)stepYaw);
            mc.field_1724.method_36457(class_3532.method_15363(currentPitch + (float)stepPitch, -90.0F, 90.0F));
            this.hasTarget = true;
         }
      }
   }

   private class_1309 findTarget(class_243 eyePos) {
      this.collectCandidates(eyePos);
      if (this.candidates.isEmpty()) {
         return null;
      } else {
         Comparator<class_1309> comp;
         if (this.priority.is("Health")) {
            comp = Comparator.comparingDouble(class_1309::method_6032);
         } else if (this.priority.is("Angle")) {
            comp = Comparator.comparingDouble(e -> this.angleTo(eyePos, e));
         } else {
            comp = Comparator.comparingDouble(e -> eyePos.method_1022(e.method_33571()));
         }

         this.candidates.sort(comp);
         return this.candidates.get(0);
      }
   }

   private void collectCandidates(class_243 eyePos) {
      this.candidates.clear();
      double maxDist = (double)this.distance.getValue().floatValue();
      double maxFov = (double)this.fov.getValue().floatValue() / 2.0;
      boolean throughWalls = this.sticky.getValue();
      boolean visibleOnly = this.conditions.is("Visible");

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1309 && entity != mc.field_1724) {
            class_1309 living = (class_1309)entity;
            if (living.method_5805()) {
               String name = living.method_5477().getString();
               if (!Client.get().friendManager().isFriend(name)
                  && (!this.targets.is("Players") || living instanceof class_1657)
                  && (!this.targets.is("Mobs") || living instanceof class_1308)
                  && (!visibleOnly && throughWalls || mc.field_1724.method_6057(living))
                  && !(eyePos.method_1022(living.method_33571()) > maxDist)
                  && !(this.angleTo(eyePos, living) > maxFov)) {
                  this.candidates.add(living);
               }
            }
         }
      }
   }

   private double angleTo(class_243 eyePos, class_1309 entity) {
      class_243 targetPos = entity.method_33571();
      double dx = targetPos.field_1352 - eyePos.field_1352;
      double dz = targetPos.field_1350 - eyePos.field_1350;
      float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
      return (double)Math.abs(class_3532.method_15393(mc.field_1724.method_36454() - yaw));
   }

   private boolean isHoldingWeapon() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         String name = mc.field_1724.method_6047().method_7909().toString().toLowerCase();
         return name.contains("sword") || name.contains("axe") || name.contains("mace");
      }
   }
}
