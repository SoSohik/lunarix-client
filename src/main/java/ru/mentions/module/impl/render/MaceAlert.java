package ru.mentions.module.impl.render;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.render.WorldRenderBatcher;

@ModuleInfo(
   name = "Mace Alert",
   category = Category.RENDER,
   desc = "Предупреждает если у игрока рядом булава в руке и подсвечивает красным"
)
@Environment(EnvType.CLIENT)
public class MaceAlert extends Module {
   public SliderSetting range = new SliderSetting(this, "Радиус", 30.0F, 5.0F, 100.0F, 5.0F);
   public BooleanSetting sound = new BooleanSetting(this, "Звук", true);
   public ModeSetting soundMode = new ModeSetting(
         this,
         "Звук оповещения",
         "Trevoga",
         "Bell",
         "Modern",
         "Bubble",
         "ApplePay",
         "Celestial",
         "Zenith",
         "Classic",
         "Soft",
         "Cyber",
         "Skeet",
         "DingDong",
         "Laser",
         "WexSide",
         "Spek",
         "Metallic",
         "Tone",
         "Pop"
      )
      .setVisible(() -> this.sound.getValue());
   public BooleanSetting offHand = new BooleanSetting(this, "Проверять вторую руку", true);
   public BooleanSetting redGlow = new BooleanSetting(this, "Красная подсветка", true);
   private final Map<Integer, Long> lastWarnedTime = new HashMap<>();
   private final Set<Integer> activeMaceHolders = new HashSet<>();
   private static final long WARN_COOLDOWN_MS = 15000L;

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float r = this.range.getValue();
         Set<Integer> current = new HashSet<>();
         long now = System.currentTimeMillis();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1657) {
               class_1657 player = (class_1657)entity;
               if (entity != mc.field_1724 && !(mc.field_1724.method_5739(entity) > r) && this.isHoldingMace(player)) {
                  int id = entity.method_5628();
                  current.add(id);
                  long last = this.lastWarnedTime.getOrDefault(id, 0L);
                  if (now - last > 15000L) {
                     this.lastWarnedTime.put(id, now);
                     String name = player.method_5477().getString();
                     int dist = (int)mc.field_1724.method_5739(entity);
                     NotificationManager.send(
                        "§c" + name + " §fдержит булаву! §7(" + dist + "м)",
                        NotificationManager.Type.WARNING,
                        4000L,
                        this.sound.getValue() ? this.soundMode.getValue() : "Выкл"
                     );
                  }
               }
            }
         }

         this.activeMaceHolders.clear();
         this.activeMaceHolders.addAll(current);
      } else {
         this.activeMaceHolders.clear();
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (this.redGlow.getValue() && mc.field_1687 != null && mc.field_1724 != null && !this.activeMaceHolders.isEmpty()) {
         WorldRenderBatcher.begin(e.getMatrixStack());

         try {
            for (Integer id : this.activeMaceHolders) {
               class_1297 entity = mc.field_1687.method_8469(id);
               if (entity instanceof class_1657) {
                  class_1657 player = (class_1657)entity;
                  if (player.method_5805()) {
                     class_238 box = player.method_5829();
                     if (box != null) {
                        WorldRenderBatcher.drawBox(box, -56798, 0.05F);
                     }
                  }
               }
            }
         } finally {
            WorldRenderBatcher.end();
         }
      }
   }

   private boolean isHoldingMace(class_1657 player) {
      return isMace(player.method_6047()) ? true : this.offHand.getValue() && isMace(player.method_6079());
   }

   public static boolean isMace(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         try {
            if (stack.method_31574(class_1802.field_49814)) {
               return true;
            }
         } catch (Throwable var2) {
         }

         try {
            class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
            return id != null && "mace".equals(id.method_12832());
         } catch (Throwable var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   protected void onDisable() {
      this.lastWarnedTime.clear();
      this.activeMaceHolders.clear();
   }
}
