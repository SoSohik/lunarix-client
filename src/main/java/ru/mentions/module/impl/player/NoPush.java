package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2258;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import ru.mentions.manager.event_impl.MotionEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "No Push",
   desc = "Не даёт другим игрокам толкать вас",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class NoPush extends Module {
   public BooleanSetting block = new BooleanSetting(this, "Блоки", true);
   public BooleanSetting entity = new BooleanSetting(this, "Сущности", true);
   public BooleanSetting xyiOblamova = new BooleanSetting(this, "Вода [пузырьки]", false);

   public static NoPush get() {
      return Instance.get(NoPush.class);
   }

   @EventHandler
   public void onEvent(MotionEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1724.method_5869() && this.xyiOblamova.getValue()) {
         class_2338 pos = mc.field_1724.method_24515();
         class_2680 state = mc.field_1687.method_8320(pos);
         boolean isUpwardBubbles = state.method_26204() == class_2246.field_10422 && !(Boolean)state.method_11654(class_2258.field_10680);
         if (isUpwardBubbles) {
            mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.0, mc.field_1724.method_18798().field_1350);
            boolean isSneaking = mc.field_1690.field_1832.method_1434();
            boolean isJumping = mc.field_1690.field_1903.method_1434();
            float motionSpeed = 0.6F;
            if (isSneaking) {
               mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, (double)(-motionSpeed), mc.field_1724.method_18798().field_1350);
            } else if (isJumping) {
               mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, (double)motionSpeed, mc.field_1724.method_18798().field_1350);
            }
         }
      }
   }
}
