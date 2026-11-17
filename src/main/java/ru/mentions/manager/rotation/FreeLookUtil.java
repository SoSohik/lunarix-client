package ru.mentions.manager.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;
import ru.mentions.manager.event_impl.EventLook;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventRotation;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.utils.math.ServerUtil;

@Environment(EnvType.CLIENT)
public class FreeLookUtil extends Component {
   public static boolean active;
   public static boolean forced;
   public static boolean initialized;
   public static float freeYaw;
   public static float freePitch;

   public static void setActive(boolean value) {
      if (value || !forced) {
         active = value;
      }
   }

   @EventHandler
   public void onEvent(EventLook event) {
      if (active) {
         this.rotateTowards(event.getYaw(), event.getPitch());
         event.cancel();
      }
   }

   @EventHandler
   public void onEvent(EventRotation event) {
      if (active) {
         event.setYaw(freeYaw);
         event.setPitch(freePitch);
      } else {
         freeYaw = event.getYaw();
         freePitch = event.getPitch();
         initialized = true;
      }
   }

   @EventHandler
   public void onEvent(EventTick event) {
      ServerUtil.tick();
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      ServerUtil.packet(e);
   }

   private void rotateTowards(double targetYaw, double targetPitch) {
      freePitch = class_3532.method_15363((float)((double)freePitch + targetPitch * 0.15), -90.0F, 90.0F);
      freeYaw = (float)((double)freeYaw + targetYaw * 0.15);
   }
}
