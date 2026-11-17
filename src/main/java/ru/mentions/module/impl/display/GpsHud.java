package ru.mentions.module.impl.display;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_5498;
import org.joml.Vector2f;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "GpsHud",
   category = Category.RENDER,
   desc = "Стрелка к GPS координатам (.gps set <x> <z>)"
)
@Environment(EnvType.CLIENT)
public class GpsHud extends Module {
   private static final class_2960 ARROW_TEX = class_2960.method_60655("client", "textures/arrow.png");
   private static GpsHud instance;
   public SliderSetting offset = new SliderSetting(this, "Отступ от прицела", 30.0F, 10.0F, 120.0F, 1.0F);
   public StringSetting xCoord = new StringSetting(this, "Координата X", "0", true);
   public StringSetting zCoord = new StringSetting(this, "Координата Z", "0", true);
   public ButtonSetting pickCurrentPos = new ButtonSetting(this, "Текущая позиция", () -> {
      if (mc.field_1724 != null) {
         int curX = (int)Math.floor(mc.field_1724.method_23317());
         int curZ = (int)Math.floor(mc.field_1724.method_23321());
         if (instance != null) {
            instance.xCoord.set(String.valueOf(curX));
            instance.zCoord.set(String.valueOf(curZ));
         }

         setTarget((float)curX, (float)curZ);
         ChatUtils.addChatMessage("§aGPS §7метка установлена на вашу позицию: §f" + curX + "§7, §f" + curZ);
      }
   });
   public ButtonSetting setCoords = new ButtonSetting(this, "Установить метку", () -> {
      if (instance != null) {
         try {
            float x = Float.parseFloat(instance.xCoord.getValue().trim());
            float z = Float.parseFloat(instance.zCoord.getValue().trim());
            setTarget(x, z);
            ChatUtils.addChatMessage("§aGPS §7→ §f" + (int)x + "§7, §f" + (int)z);
         } catch (Exception var2) {
            ChatUtils.addChatMessage("§cGPS: неверный формат координат");
         }
      }
   });
   public ButtonSetting clearTarget = new ButtonSetting(this, "Сбросить метку", () -> {
      clearTarget();
      ChatUtils.addChatMessage("§7GPS метка сброшена");
   });
   private static Vector2f target = null;

   public GpsHud() {
      instance = this;
   }

   public static GpsHud getInstance() {
      return instance;
   }

   public static void setTarget(float x, float z) {
      target = new Vector2f(x, z);
      if (instance != null) {
         instance.xCoord.set(String.valueOf((int)x));
         instance.zCoord.set(String.valueOf((int)z));
      }
   }

   public static void clearTarget() {
      target = null;
   }

   public static Vector2f getTarget() {
      return target;
   }

   @EventHandler(
      priority = -500
   )
   public void onDisplay(EventDisplay e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (target != null) {
            if (!mc.field_1690.field_1842) {
               if (mc.field_1690.method_31044().equals(class_5498.field_26664)) {
                  double dx = (double)target.x + 0.5 - mc.field_1724.method_23317();
                  double dz = (double)target.y + 0.5 - mc.field_1724.method_23321();
                  int dst = (int)Math.sqrt(dx * dx + dz * dz);
                  float targetScale = 2.0F;
                  float currentScale = (float)mc.method_22683().method_4495();
                  float scaleFix = targetScale / currentScale;
                  int screenWidth = (int)((float)mc.method_22683().method_4486() / scaleFix);
                  int screenHeight = (int)((float)mc.method_22683().method_4502() / scaleFix);
                  float middleW = (float)screenWidth / 2.0F;
                  float middleH = (float)screenHeight / 2.0F - 90.0F;
                  float angleToTarget = (float)(-(Math.atan2(dx, dz) * (180.0 / Math.PI)));
                  float realYaw = angleToTarget - mc.field_1773.method_19418().method_19330();
                  float rad = (float)Math.toRadians((double)realYaw);
                  float posY = middleH - this.offset.getValue();
                  float off = posY + 10.0F - middleH;
                  float cx = middleW - off * (float)Math.sin((double)rad);
                  float cy = middleH + off * (float)Math.cos((double)rad);
                  int color = ColorUtil.fade(1);
                  Client.get()
                     .render2D()
                     .getTexturePipeline()
                     .drawGlowTexture(
                        ARROW_TEX,
                        cx - 10.0F,
                        cy - 10.0F,
                        20.0F,
                        20.0F,
                        0.0F,
                        0.0F,
                        1.0F,
                        1.0F,
                        new int[]{color, color, color, color},
                        new float[]{0.0F, 0.0F, 0.0F, 0.0F},
                        1.0F,
                        realYaw
                     );
                  Fonts.sf_regular.drawCentered(dst + "м", cx, cy + 10.0F + 4.0F, 6.0F, ColorUtil.getColor(255, 0.85F));
               }
            }
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      target = null;
   }

   @Override
   protected void onDisable() {
      target = null;
   }
}
