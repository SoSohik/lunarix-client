package ru.mentions.module.impl.utils;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.utils.math.StopWatchP;

@ModuleInfo(
   name = "Anti AFK",
   desc = "Выполняет случайные действия, чтобы сервер не кикнул за АФК",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AntiAFK extends Module {
   public final BooleanSetting rotate = new BooleanSetting(this, "Поворот головы", true);
   public final BooleanSetting jump = new BooleanSetting(this, "Прыжок", true);
   public final BooleanSetting swing = new BooleanSetting(this, "Взмах рукой", true);
   private final StopWatchP timer = new StopWatchP();
   private final Random random = new Random();
   private long nextActionMs = 20000L;

   @Override
   public void onEnable() {
      super.onEnable();
      this.scheduleNext();
   }

   private void scheduleNext() {
      this.nextActionMs = 15000L + (long)(this.random.nextFloat() * 30000.0F);
      this.timer.reset();
   }

   private boolean playerActive() {
      return mc.field_1690.field_1894.method_1434()
         || mc.field_1690.field_1881.method_1434()
         || mc.field_1690.field_1913.method_1434()
         || mc.field_1690.field_1849.method_1434()
         || mc.field_1690.field_1903.method_1434()
         || mc.field_1690.field_1886.method_1434()
         || mc.field_1690.field_1904.method_1434();
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1755 != null || this.playerActive()) {
            this.timer.reset();
         } else if (this.timer.isReached(this.nextActionMs)) {
            int action = this.random.nextInt(3);
            if (action == 0 && this.rotate.getValue()) {
               float deltaYaw = (this.random.nextFloat() - 0.5F) * 30.0F;
               mc.field_1724.method_36456(mc.field_1724.method_36454() + deltaYaw);
            } else if (action == 1 && this.jump.getValue() && mc.field_1724.field_5976) {
               mc.field_1724.method_5764(true);
            } else if (this.swing.getValue()) {
               mc.field_1724.method_6104(class_1268.field_5808);
            }

            this.scheduleNext();
         }
      }
   }
}
