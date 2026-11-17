package ru.mentions.module.impl.player;

import java.lang.reflect.Field;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_7923;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "No Delay",
   category = Category.PLAYER,
   desc = "Убирает задержки"
)
@Environment(EnvType.CLIENT)
public class NoDelay extends Module {
   public BooleanSetting jump = new BooleanSetting(this, "Прыжок", true);
   public BooleanSetting expBottle = new BooleanSetting(this, "Пузырёк опыта", true);

   public static NoDelay get() {
      return Instance.get(NoDelay.class);
   }

   @EventHandler
   private void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null) {
         if (this.expBottle.getValue() && mc.field_1724.method_6047().method_7909() == class_1802.field_8287) {
            mc.field_1724.method_7357().method_7900(class_7923.field_41178.method_10221(class_1802.field_8287));
            this.resetCooldown();
         }
      }
   }

   private void resetCooldown() {
      try {
         Field f = class_310.class.getDeclaredField("itemUseCooldown");
         f.setAccessible(true);
         f.setInt(mc, 0);
      } catch (Exception var4) {
         try {
            Field fx = class_310.class.getDeclaredField("field_1752");
            fx.setAccessible(true);
            fx.setInt(mc, 0);
         } catch (Exception var3) {
         }
      }
   }
}
