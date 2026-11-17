package ru.mentions.module.impl.combat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1684;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.mixin.ClientPlayerInteractionManagerAccessor;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Target Pearl",
   desc = "Ловит чужой эндер-перл и кидает свой в точку падения",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class TargetPearl extends Module {
   private static final int STAGE_IDLE = 0;
   private static final int STAGE_TURN = 1;
   private static final int STAGE_THROW = 2;
   private static final int STAGE_RESTORE = 3;
   private static final double PEARL_DRAG = 0.99;
   private static final double PEARL_GRAVITY = 0.03;
   private static final double THROW_SPEED = 1.5;
   private static final int SIM_STEPS = 200;
   private final SliderSetting radius = new SliderSetting(this, "Радиус", 30.0F, 5.0F, 100.0F, 1.0F);
   private final SliderSetting delay = new SliderSetting(this, "Тики", 10.0F, 1.0F, 40.0F, 1.0F);
   private final BooleanSetting pauseAim = new BooleanSetting(this, "Пауза AimAssist", true);
   private final Set<UUID> seenPearls = new HashSet<>();
   private int lastThrowTick;
   private int stage = 0;
   private int pearlSlot = -1;
   private int oldSlot = -1;
   private float targetYaw;
   private float targetPitch;
   private boolean pausedAura;
   private boolean pausedAim;

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.seenPearls.clear();
      this.lastThrowTick = 0;
      this.resetThrow();
      this.resumeAim();
   }

   @Override
   protected void onDisable() {
      this.resetThrow();
      this.resumeAim();
      super.onDisable();
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.lastThrowTick++;
         if (this.stage != 0) {
            this.handleThrowSequence();
         } else {
            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity instanceof class_1684) {
                  class_1684 pearl = (class_1684)entity;
                  if (!this.seenPearls.contains(pearl.method_5667()) && pearl.field_6012 <= 2 && entity instanceof class_1676) {
                     class_1676 projectile = (class_1676)entity;
                     class_1297 slot = projectile.method_24921();
                     if (slot instanceof class_1657) {
                        class_1657 owner = (class_1657)slot;
                        if (owner != mc.field_1724
                           && !(mc.field_1724.method_5739(owner) > this.radius.getValue())
                           && !((float)this.lastThrowTick < this.delay.getValue())) {
                           int slotx = this.findPearlSlot();
                           if (slotx != -1) {
                              class_243 landing = this.predictPearlLanding(pearl);
                              float[] angles = this.calculateThrowAngles(landing);
                              this.pearlSlot = slotx;
                              this.oldSlot = mc.field_1724.method_31548().method_67532();
                              this.targetYaw = angles[0];
                              this.targetPitch = angles[1];
                              this.lastThrowTick = 0;
                              this.seenPearls.add(pearl.method_5667());
                              if (this.pauseAim.getValue()) {
                                 this.pauseAim();
                              }

                              this.stage = 1;
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void setLook(float yaw, float pitch) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_36456(yaw);
         mc.field_1724.method_36457(pitch);
      }
   }

   private void handleThrowSequence() {
      switch (this.stage) {
         case 1:
            this.selectSlot(this.pearlSlot);
            this.setLook(this.targetYaw, this.targetPitch);
            this.stage = 2;
            break;
         case 2:
            class_1799 stack = mc.field_1724.method_6047();
            if (!stack.method_7960() && stack.method_31574(class_1802.field_8634) && mc.field_1761 != null) {
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            }

            this.stage = 3;
            break;
         case 3:
            this.selectSlot(this.oldSlot);
            this.resetThrow();
            this.resumeAim();
            break;
         default:
            this.resetThrow();
      }
   }

   private class_243 predictPearlLanding(class_1684 pearl) {
      class_243 pos = pearl.method_30950(1.0F);
      class_243 vel = pearl.method_18798();

      for (int i = 0; i < 200; i++) {
         class_243 next = pos.method_1019(vel);
         class_3965 hit = mc.field_1687.method_17742(new class_3959(pos, next, class_3960.field_17558, class_242.field_1348, pearl));
         if (hit.method_17783() != class_240.field_1332) {
            return hit.method_17784();
         }

         pos = next;
         vel = new class_243(vel.field_1352 * 0.99, vel.field_1351 * 0.99 - 0.03, vel.field_1350 * 0.99);
      }

      return pos;
   }

   private float[] calculateThrowAngles(class_243 landing) {
      class_243 eye = mc.field_1724.method_33571();
      double dx = landing.field_1352 - eye.field_1352;
      double dz = landing.field_1350 - eye.field_1350;
      double dy = landing.field_1351 - eye.field_1351;
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float directPitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
      float bestPitch = directPitch;
      double bestDistance = Double.MAX_VALUE;

      for (float pitch = directPitch; pitch >= -85.0F && pitch >= directPitch - 50.0F; pitch -= 0.5F) {
         double distance = this.simulateThrow(eye, yaw, pitch).method_1022(landing);
         if (distance < bestDistance) {
            bestDistance = distance;
            bestPitch = pitch;
         }
      }

      return new float[]{yaw, bestPitch};
   }

   private class_243 simulateThrow(class_243 eye, float yaw, float pitch) {
      double yawRad = Math.toRadians((double)yaw);
      double pitchRad = Math.toRadians((double)pitch);
      class_243 vel = new class_243(-Math.sin(yawRad) * Math.cos(pitchRad) * 1.5, -Math.sin(pitchRad) * 1.5, Math.cos(yawRad) * Math.cos(pitchRad) * 1.5);
      class_243 pos = eye;

      for (int i = 0; i < 200; i++) {
         class_243 next = pos.method_1019(vel);
         class_3965 hit = mc.field_1687.method_17742(new class_3959(pos, next, class_3960.field_17558, class_242.field_1348, mc.field_1724));
         if (hit.method_17783() != class_240.field_1332) {
            return hit.method_17784();
         }

         pos = next;
         vel = new class_243(vel.field_1352 * 0.99, vel.field_1351 * 0.99 - 0.03, vel.field_1350 * 0.99);
      }

      return pos;
   }

   private int findPearlSlot() {
      for (int i = 0; i < 9; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960() && stack.method_31574(class_1802.field_8634)) {
            return i;
         }
      }

      return -1;
   }

   private void selectSlot(int slot) {
      if (mc.field_1724 != null && slot >= 0 && slot <= 8) {
         if (mc.field_1724.method_31548().method_67532() != slot) {
            mc.field_1724.method_31548().method_61496(slot);
            if (mc.field_1761 != null) {
               ((ClientPlayerInteractionManagerAccessor)mc.field_1761).mentions$syncSelectedSlot();
            }
         }
      }
   }

   private void pauseAim() {
      AttackAura aura = AttackAura.get();
      if (aura != null && aura.isEnabled()) {
         aura.setEnabled(false, false);
         this.pausedAura = true;
      }

      AimBot aim = AimBot.get();
      if (aim != null && aim.isEnabled()) {
         aim.setEnabled(false, false);
         this.pausedAim = true;
      }
   }

   private void resumeAim() {
      if (this.pausedAura) {
         AttackAura aura = AttackAura.get();
         if (aura != null) {
            aura.setEnabled(true, false);
         }

         this.pausedAura = false;
      }

      if (this.pausedAim) {
         AimBot aim = AimBot.get();
         if (aim != null) {
            aim.setEnabled(true, false);
         }

         this.pausedAim = false;
      }
   }

   private void resetThrow() {
      this.stage = 0;
      this.pearlSlot = -1;
      this.oldSlot = -1;
   }
}
