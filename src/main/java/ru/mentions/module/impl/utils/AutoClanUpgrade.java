package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_3965;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.math.InvUtil;
import ru.mentions.utils.other.TimerUtil;

@ModuleInfo(
   name = "Auto Clan Upgrade",
   desc = "Автоматическая прокачка клана через факела",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoClanUpgrade extends Module {
   private final TimerUtil timer = new TimerUtil();
   private boolean slowBreak = false;

   @EventHandler
   public void onUpdate(EventUpdate event) {
      int slot = InvUtil.getItemInHotBar(class_1802.field_8810);
      if (mc.field_1687.method_27983().method_29177().method_12832().equals("lobby")) {
         if (mc.field_1724.field_6012 % 200 == 0) {
            ChatUtils.addChatMessage("Тепнитесь на РТП");
         }
      } else if (slot == -1) {
         if (mc.field_1724.field_6012 % 200 == 0) {
            ChatUtils.addChatMessage("Нужен факел в хотбаре");
         }
      } else {
         class_2338 pos = mc.field_1724.method_24515().method_10074();
         class_2338 torchPos = pos.method_10084();
         if (mc.field_1687.method_8320(pos).method_26212(mc.field_1687, pos)) {
            if (mc.field_1724.field_6012 % 1 == 0) {
               float yaw = 25.0F;
               float pitch = 87.5F;
               RotationProcess.update(new Rotation(yaw, pitch), 255.0F, 255.0F, 0, 100);
            }

            int delay = 5;
            if (this.timer.hasReached((double)5)) {
               this.timer.reset();
               if (mc.field_1687.method_8320(torchPos).method_26215()) {
                  mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, new class_3965(pos.method_46558(), class_2350.field_11036, pos, false));
               }

               if (mc.field_1687.method_8320(torchPos).method_26204() == class_2246.field_10336) {
                  mc.field_1761.method_2910(torchPos, class_2350.field_11039);
                  mc.field_1724.method_6104(class_1268.field_5808);
               }
            }
         }
      }
   }
}
