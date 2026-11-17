package ru.mentions.module.impl.display;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_5498;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;

@ModuleInfo(
   name = "Arrows",
   category = Category.RENDER,
   desc = "Стрелки на игроков"
)
@Environment(EnvType.CLIENT)
public class Arrows extends Module {
   public BooleanSetting onlyArmored = new BooleanSetting(this, "Игнорировать голых", false);
   public SliderSetting size = new SliderSetting(this, "Размер", 30.0F, 12.0F, 80.0F, 1.0F);
   public SliderSetting radius = new SliderSetting(this, "Радиус от прицела", 50.0F, 20.0F, 80.0F, 1.0F);
   private final Animation animation = new Animation();
   private static final class_2960 ARROW_TEX = class_2960.method_60655("client", "textures/arrow.png");

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null) {
         this.animation.run(mc.field_1724.method_5624() ? 1.0 : 0.0, 0.15F, Easings.SINE_OUT);
      }
   }

   @EventHandler(
      priority = -500
   )
   public void onDisplay(EventDisplay e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!mc.field_1690.field_1842 && mc.field_1690.method_31044().equals(class_5498.field_26664)) {
            List<class_1657> players = new ArrayList<>();

            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity instanceof class_1657) {
                  class_1657 player = (class_1657)entity;
                  if (player != mc.field_1724
                     && player.method_5805()
                     && (player.method_5797() == null || !player.method_5797().getString().startsWith("Ghost_"))
                     && (!this.onlyArmored.getValue() || hasArmor(player))) {
                     players.add(player);
                  }
               }
            }

            if (!players.isEmpty()) {
               this.animation.update();
               float targetScale = 2.0F;
               float currentScale = (float)mc.method_22683().method_4495();
               float scaleFix = targetScale / currentScale;
               int screenWidth = (int)((float)mc.method_22683().method_4486() / scaleFix);
               int screenHeight = (int)((float)mc.method_22683().method_4502() / scaleFix);
               float partialTicks = e.getPartialTicks();
               float middleW = (float)screenWidth / 2.0F;
               float middleH = (float)screenHeight / 2.0F;
               float size = this.size.getValue() / 4.0F;
               float posY = middleH - this.radius.getValue() - this.radius.getValue() / 4.0F * this.animation.get();
               float offset = posY + size / 2.0F - middleH;

               for (class_1657 player : players) {
                  int color = Client.get().friendManager().isFriend(player.method_5477().getString()) ? ColorUtil.GREEN : ColorUtil.fade(1);
                  float realYaw = getAngle(player, partialTicks) - mc.field_1773.method_19418().method_19330();
                  float rad = (float)Math.toRadians((double)realYaw);
                  float cx = middleW - offset * (float)Math.sin((double)rad);
                  float cy = middleH + offset * (float)Math.cos((double)rad);
                  Client.get()
                     .render2D()
                     .getTexturePipeline()
                     .drawGlowTexture(
                        ARROW_TEX,
                        cx - size / 2.0F,
                        cy - size / 2.0F,
                        size,
                        size,
                        0.0F,
                        0.0F,
                        1.0F,
                        1.0F,
                        new int[]{color, color, color, color},
                        new float[]{0.0F, 0.0F, 0.0F, 0.0F},
                        1.0F,
                        realYaw
                     );
               }
            }
         }
      }
   }

   private static boolean hasArmor(class_1657 player) {
      return !player.method_6118(class_1304.field_6169).method_7960()
         || !player.method_6118(class_1304.field_6174).method_7960()
         || !player.method_6118(class_1304.field_6172).method_7960()
         || !player.method_6118(class_1304.field_6166).method_7960();
   }

   private static float getAngle(class_1297 entity, float partialTicks) {
      double x = (double)(
         MathUtil.interpolate(entity.field_6038, entity.method_23317(), (double)partialTicks)
            - MathUtil.interpolate(mc.field_1724.field_6038, mc.field_1724.method_23317(), (double)partialTicks)
      );
      double z = (double)(
         MathUtil.interpolate(entity.field_5989, entity.method_23321(), (double)partialTicks)
            - MathUtil.interpolate(mc.field_1724.field_5989, mc.field_1724.method_23321(), (double)partialTicks)
      );
      return (float)(-(Math.atan2(x, z) * (180.0 / Math.PI)));
   }
}
