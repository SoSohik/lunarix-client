package ru.mentions.module.impl.movement;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2708;
import ru.mentions.manager.event_impl.EventMoveInput;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.MotionEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.utils.other.TimerUtil;
import ru.mentions.utils.player.ITimerSpeed;
import ru.mentions.utils.player.MoveUtil;
import ru.mentions.utils.player.TickManagerClient;

@ModuleInfo(
   name = "Speed",
   desc = "Увеличивает скорость",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class Speed extends Module {
   public ModeSetting type = new ModeSetting(this, "Режим", "Vanilla", "Meta");
   public SliderSetting speed = new SliderSetting(this, "Скорость", 1.0F, 0.3F, 2.0F, 0.1F).setVisible(() -> this.type.is("Vanilla"));
   private int ticks = 0;
   private int groundTicks = 0;
   public float tick = 1.0F;
   public TimerUtil timerUtil = new TimerUtil();

   @EventHandler
   public void onEvent(MotionEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.type.is("Grim Box") && !mc.field_1724.method_5869()) {
            double finalSpeed = 0.02F;
            if (finalSpeed <= 0.0) {
               return;
            }

            class_1297 nearest = null;
            double bestSq = Double.MAX_VALUE;
            double maxRangeSq = 0.1F;

            for (class_1297 ent : mc.field_1687.method_18112()) {
               if (ent != mc.field_1724) {
                  double dz;
                  double dx;
                  double sq;
                  if (ent == AttackAura.target
                     && (sq = (dx = ent.method_23317() - mc.field_1724.method_23317()) * dx + (dz = ent.method_23321() - mc.field_1724.method_23321()) * dz)
                        <= maxRangeSq
                     && sq < bestSq) {
                     bestSq = sq;
                     nearest = ent;
                  }

                  if (nearest != null) {
                     double[] dir = this.getDirectionToPoint(mc.field_1724.method_73189(), nearest.method_73189(), finalSpeed);
                     mc.field_1724.method_5762(dir[0], 0.0, dir[1]);
                  }
               }
            }
         }

         if (this.type.is("Meta")) {
            if (mc.field_1724.method_6059(class_1294.field_5909)) {
               return;
            }

            String offhandName = mc.field_1724.method_6079().method_7964().getString();
            byte[] nameBytes = offhandName.getBytes(StandardCharsets.UTF_8);
            String bytesString = Arrays.toString(nameBytes);
            boolean sharKing = bytesString.equals("[-48, -88, -48, -80, -47, -128, 32, 75, 73, 78, 71]");
            boolean sharTigr = bytesString.equals(
               "[-48, -94, -48, -72, -48, -77, -47, -128, -48, -72, -48, -67, -48, -67, -48, -80, -47, -113, 32, -48, -77, -48, -66, -48, -69, -48, -66, -48, -78, -48, -80]"
            );
            class_1293 effectInstance = mc.field_1724.method_6112(class_1294.field_5904);
            boolean hasSpeed = mc.field_1724.method_6059(class_1294.field_5904);
            boolean onGround = mc.field_1724.method_24828();
            float fallDist = (float)mc.field_1724.field_6017;
            if (hasSpeed && effectInstance != null && effectInstance.method_5578() == 2 && fallDist <= 0.2F && !onGround) {
               MoveUtil.setSpeed(sharKing ? 0.72F : (sharTigr ? 0.7F : 0.58F));
            } else if (hasSpeed && effectInstance != null && effectInstance.method_5578() == 2 && onGround) {
               MoveUtil.setSpeed(sharKing ? 0.5 : (sharTigr ? 0.49F : 0.42F));
            } else if (hasSpeed && effectInstance != null && effectInstance.method_5578() == 3 && fallDist <= 0.2F && !onGround) {
               MoveUtil.setSpeed(0.7F);
            } else if (hasSpeed && effectInstance != null && effectInstance.method_5578() == 3 && onGround) {
               MoveUtil.setSpeed(0.49F);
            } else if (hasSpeed && effectInstance != null && effectInstance.method_5578() == 1 && fallDist <= 0.2F && !onGround) {
               MoveUtil.setSpeed(0.52F);
            } else if (hasSpeed && effectInstance != null && effectInstance.method_5578() == 1 && onGround) {
               MoveUtil.setSpeed(0.36F);
            } else if (fallDist <= 0.2F && !onGround || fallDist <= 0.2F && hasSpeed) {
               MoveUtil.setSpeed(0.36F);
            }
         }

         if (this.type.is("Vanilla") && !mc.field_1724.method_5869()) {
            if (mc.field_1724.method_24828()) {
               MoveUtil.setSpeed(0.3F);
            } else {
               MoveUtil.setSpeed((double)this.speed.getValue().floatValue());
            }
         }

         if (this.type.is("RW")) {
            this.handleRW();
         }
      }
   }

   @EventHandler
   public void onEvent(EventMoveInput e) {
   }

   @EventHandler
   public void onEvent(EventPacket e) {
      if (this.type.is("RW") && e.getPacket() instanceof class_2708 && this.ticks % 2 == 1) {
         this.ticks++;
      }
   }

   private void handleRW() {
      if (mc.method_61966() instanceof ITimerSpeed speedTimer) {
         long elapsed = this.timerUtil.getTime();
         float currentSpeed;
         if (elapsed < 100L) {
            boolean speed = true;
            currentSpeed = 1.15F;
         } else if (elapsed < 500L) {
            currentSpeed = 1.4F;
         } else if (elapsed < 520L) {
            currentSpeed = 1.6F;
         } else if (elapsed < 540L) {
            boolean speed = false;
            currentSpeed = 0.25F;
         } else {
            this.timerUtil.reset();
            boolean speed = true;
            currentSpeed = 0.5F;
         }

         speedTimer.setSpeed(currentSpeed);
         double var10 = 0.03;
      }
   }

   private boolean canUseRW() {
      return mc.field_1724 != null
         && mc.field_1687 != null
         && mc.field_1724.field_3944 != null
         && MoveUtil.isMoving()
         && !mc.field_1724.method_5765()
         && !mc.field_1724.method_31549().field_7479;
   }

   private void resetRWState(boolean resetTimer) {
      this.ticks = 0;
      this.groundTicks = 0;
      if (resetTimer) {
         mc.field_1724.field_28627 = 0.0F;
      }
   }

   private double[] getDirectionToPoint(class_243 from, class_243 to, double spd) {
      double dx = to.field_1352 - from.field_1352;
      double dz = to.field_1350 - from.field_1350;
      double len = Math.sqrt(dx * dx + dz * dz);
      return len == 0.0 ? new double[]{0.0, 0.0} : new double[]{dx / len * spd, dz / len * spd};
   }

   @Override
   public void onEnable() {
      this.resetRWState(true);
      super.onEnable();
   }

   @Override
   public void onDisable() {
      if (mc.method_61966() instanceof ITimerSpeed speedTimer) {
         speedTimer.setSpeed(1.0F);
      }

      this.resetRWState(true);
      TickManagerClient.tick = 20.0F;
      super.onDisable();
   }
}
