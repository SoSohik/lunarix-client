package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_5498;
import ru.mentions.manager.event_impl.CameraPositionEvent;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.InputEvent;
import ru.mentions.manager.event_impl.MoveEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.math.MathUtil;

@ModuleInfo(
   name = "Free Camera",
   desc = "ктото рил не знает че это лол",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class FreeCamera extends Module {
   public class_243 pos;
   public class_243 prevPos;

   @Override
   public void onEnable() {
      this.prevPos = this.pos = new class_243(mc.method_1561().field_4686.method_71156().method_46409());
      super.onEnable();
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      class_2596<?> packet = e.getPacket();
      if (packet instanceof class_2724 || packet instanceof class_2678) {
         this.setEnabled(false);
      }
   }

   @EventHandler
   public void onMove(MoveEvent e) {
      e.setMovement(class_243.field_1353);
   }

   @EventHandler
   public void onInput(InputEvent e) {
      float speed = 1.5F;
      double[] motion = this.calculateDirection((float)e.forward(), e.sideways(), (double)speed);
      this.prevPos = this.pos;
      this.pos = this.pos.method_1031(motion[0], e.getInput().comp_3163() ? (double)speed : (e.getInput().comp_3164() ? (double)(-speed) : 0.0), motion[1]);
      e.inputNone();
   }

   public double[] calculateDirection(float forward, float sideways, double distance) {
      float yaw = mc.field_1724.method_36454();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sinYaw = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cosYaw = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
      double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
      return new double[]{xMovement, zMovement};
   }

   @EventHandler
   public void onCameraPosition(CameraPositionEvent e) {
      e.setPos(MathUtil.interpolate(this.prevPos, this.pos));
      mc.field_1690.method_31043(class_5498.field_26664);
   }
}
