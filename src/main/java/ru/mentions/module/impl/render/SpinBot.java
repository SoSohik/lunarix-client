package ru.mentions.module.impl.render;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Spin Bot",
   desc = "Крутит yaw игрока (отправляется на сервер — античиты видят)",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class SpinBot extends Module {
   public final SliderSetting speed = new SliderSetting(this, "Скорость", 8.0F, 1.0F, 20.0F, 0.5F);
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Spin", "Wobble", "Random");
   private final Random random = new Random();
   private float wobbleOffset = 0.0F;
   private boolean wobbleDir = true;

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1755 == null) {
            float delta = this.speed.getValue();
            if (this.mode.is("Spin")) {
               mc.field_1724.method_36456(mc.field_1724.method_36454() + delta);
            } else if (this.mode.is("Wobble")) {
               this.wobbleOffset = this.wobbleOffset + (this.wobbleDir ? delta * 0.3F : -delta * 0.3F);
               if (this.wobbleOffset > 45.0F) {
                  this.wobbleOffset = 45.0F;
                  this.wobbleDir = false;
               } else if (this.wobbleOffset < -45.0F) {
                  this.wobbleOffset = -45.0F;
                  this.wobbleDir = true;
               }

               mc.field_1724.method_36456(mc.field_1724.method_36454() + this.wobbleOffset * 0.1F);
            } else {
               float jitter = (this.random.nextFloat() - 0.5F) * 2.0F * delta;
               mc.field_1724.method_36456(mc.field_1724.method_36454() + jitter);
            }
         }
      }
   }
}
