package ru.mentions.module.impl.combat;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.FreeLookUtil;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.aura.UBoxPoints;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Hit Boxes",
   desc = "Увеличивает хитбокс игрока",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class HitBoxes extends Module {
   public SliderSetting size = new SliderSetting(this, "Размер хит бокса", 0.3F, 0.1F, 1.0F, 0.1F);
   public ModeSetting type = new ModeSetting(this, "Режим", "Обычный", "Ротация");
   public ModeSetting typeRot = new ModeSetting(this, "Режим ротации", "Постоянный", "При ударе").setVisible(() -> this.type.is("Ротация"));
   public ModeSetting priority = new ModeSetting(this, "Приоритет цели", "Distance", "Health", "Angle", "Random");
   public static class_1309 pendingTarget = null;
   public static int attackTimer = 0;
   private final Random pickRandom = new Random();
   private final ArrayList<class_1309> pickHits = new ArrayList<>();

   public static HitBoxes get() {
      return Instance.get(HitBoxes.class);
   }

   @EventHandler
   public void onEvent(AttackEvent event) {
      if (this.typeRot.is("При ударе")) {
         pendingTarget = (class_1309)event.getTarget();
         attackTimer = 0;
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.type.is("Ротация")) {
         if (this.typeRot.is("Постоянный")) {
            if (!(mc.field_1765 instanceof class_3966 entityHitResult)) {
               pendingTarget = null;
               return;
            }

            class_1297 entity = entityHitResult.method_17782();
            class_1309 target;
            if (!(entity instanceof class_1309) || mc.field_1724.method_5739(target = (class_1309)entity) > 4.0F) {
               pendingTarget = null;
               return;
            }

            class_1309 picked = this.pickByPriority(target);
            if (picked != null) {
               target = picked;
            }

            if (target instanceof class_1657 playerEntity) {
               pendingTarget = playerEntity;
               this.onHitBox(playerEntity);
            } else {
               pendingTarget = null;
            }
         }

         if (this.typeRot.is("При ударе") && pendingTarget != null) {
            if (!pendingTarget.method_5805() || mc.field_1724.method_5739(pendingTarget) > 4.0F) {
               pendingTarget = null;
               return;
            }

            this.onHitBox(pendingTarget);
            if (++attackTimer >= 3) {
               pendingTarget = null;
               attackTimer = 0;
            }
         }
      }
   }

   private class_1309 pickByPriority(class_1309 current) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 eye = mc.field_1724.method_33571();
         class_243 look = mc.field_1724.method_5828(1.0F).method_1029();
         class_243 rayEnd = eye.method_1031(look.field_1352 * 4.0, look.field_1351 * 4.0, look.field_1350 * 4.0);
         float grow = this.size.getValue();
         this.pickHits.clear();
         ArrayList<class_1309> hits = this.pickHits;

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1309 && entity != mc.field_1724) {
               class_1309 living = (class_1309)entity;
               if (living.method_5805() && !(mc.field_1724.method_5739(living) > 4.0F)) {
                  Optional<class_243> hit = living.method_5829().method_1009((double)grow, (double)grow, (double)grow).method_992(eye, rayEnd);
                  if (hit.isPresent()) {
                     hits.add(living);
                  }
               }
            }
         }

         if (hits.isEmpty()) {
            return current;
         } else if (this.priority.is("Random")) {
            return hits.get(this.pickRandom.nextInt(hits.size()));
         } else if (this.priority.is("Health")) {
            class_1309 best = null;
            float bestHp = Float.MAX_VALUE;

            for (class_1309 living : hits) {
               float hp = living.method_6032() + living.method_6067();
               if (hp < bestHp) {
                  bestHp = hp;
                  best = living;
               }
            }

            return best;
         } else if (this.priority.is("Angle")) {
            class_1309 best = null;
            double bestAngle = Double.MAX_VALUE;

            for (class_1309 livingx : hits) {
               class_243 toTarget = livingx.method_73189().method_1031(0.0, (double)(livingx.method_17682() * 0.5F), 0.0).method_1020(eye).method_1029();
               double angle = Math.acos(class_3532.method_15350(look.method_1026(toTarget), -1.0, 1.0));
               if (angle < bestAngle) {
                  bestAngle = angle;
                  best = livingx;
               }
            }

            return best;
         } else {
            class_1309 best = null;
            double bestDist = Double.MAX_VALUE;

            for (class_1309 livingxx : hits) {
               double dist = livingxx.method_73189().method_1022(eye);
               if (dist < bestDist) {
                  bestDist = dist;
                  best = livingxx;
               }
            }

            return best;
         }
      } else {
         return current;
      }
   }

   public void onHitBox(class_1309 target) {
      if (this.typeRot.is("Постоянный")) {
         class_243 vec3d = UBoxPoints.getBestVector3dOnEntityBox(target.method_5829(), false).method_1020(mc.field_1724.method_33571());
         float yaw = (float)Math.toDegrees(Math.atan2(-vec3d.field_1352, vec3d.field_1350));
         float pitch = (float)class_3532.method_15350(
            -Math.toDegrees(Math.atan2(vec3d.field_1351, Math.hypot(vec3d.field_1352, vec3d.field_1350))), -90.0, 90.0
         );
         float waveA = (float)Math.cos((double)System.currentTimeMillis() / 40.0);
         float waveB = (float)Math.sin((double)System.currentTimeMillis() / 70.0);
         float yawJitter = waveA * MathUtil.randomLerp(1.0F, 2.0F);
         float pitchJitter = waveB * MathUtil.randomLerp(1.0F, 2.0F);
         Rotation newRotation = new Rotation(yaw + yawJitter, FreeLookUtil.freePitch + pitchJitter);
         RotationProcess.update(
            newRotation,
            MathUtil.randomLerp(12.0F, 25.0F),
            MathUtil.randomLerp(22.0F, 25.0F),
            MathUtil.randomLerp(24.0F, 26.0F),
            MathUtil.randomLerp(24.0F, 26.0F),
            0,
            15,
            false
         );
      }

      if (this.typeRot.is("При ударе")) {
         class_243 vec3d = target.method_73189()
            .method_1031(0.0, (double)(target.method_17682() / 2.0F), 0.0)
            .method_1020(mc.field_1724.method_33571())
            .method_1029();
         float yaw = (float)Math.toDegrees(Math.atan2(-vec3d.field_1352, vec3d.field_1350));
         float pitch = (float)class_3532.method_15350(
            -Math.toDegrees(Math.atan2(vec3d.field_1351, Math.hypot(vec3d.field_1352, vec3d.field_1350))), -90.0, 90.0
         );
         float waveA = (float)Math.cos((double)System.currentTimeMillis() / 180.0);
         float waveB = (float)Math.sin((double)System.currentTimeMillis() / 180.0);
         float yawJitter = waveA * MathUtil.randomLerp(2.0F, 4.0F);
         float pitchJitter = waveB * MathUtil.randomLerp(2.0F, 4.0F);
         Rotation newRotation = new Rotation(yaw + yawJitter, pitch + pitchJitter);
         RotationProcess.update(
            newRotation,
            MathUtil.randomLerp(152.0F, 255.0F),
            MathUtil.randomLerp(125.0F, 255.0F),
            MathUtil.randomLerp(152.0F, 255.0F),
            MathUtil.randomLerp(152.0F, 255.0F),
            0,
            15,
            false
         );
      }
   }
}
