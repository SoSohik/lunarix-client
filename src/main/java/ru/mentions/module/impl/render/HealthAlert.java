package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix3x2fStack;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.RenderUtil;

@ModuleInfo(
   name = "Health Alert",
   category = Category.RENDER,
   desc = "Визуальный эффект когда мало здоровья"
)
@Environment(EnvType.CLIENT)
public class HealthAlert extends Module {
   public SliderSetting heath = new SliderSetting(this, "Кол в хп", 5.0F, 1.0F, 15.0F, 1.0F);
   public Animation animation_1 = new Animation();
   public Animation animation_2 = new Animation();
   public Animation animation_3 = new Animation();

   @EventHandler
   public void onDisplay(EventDisplay e) {
      Matrix3x2fStack matrixStack = e.getDrawContext().method_51448();
      matrixStack.pushMatrix();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float playerallHP = mc.field_1724.method_6032() + mc.field_1724.method_6067();
         boolean effect = false;
         if (playerallHP < this.heath.getValue()) {
            effect = true;
         }

         this.animation_1.update();
         this.animation_1.run(effect ? 1.0 : 0.0, 0.15F, Easings.SINE_OUT);
         if (this.animation_1.get() > 0.0F) {
            float alpha = (float)(0.6F + 0.15F * Math.sin((double)System.currentTimeMillis() / 150.0));
            float alpha2 = (float)(0.0 + 0.2F * Math.sin((double)System.currentTimeMillis() / 300.0));
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{ColorUtil.getColor(0, alpha * this.animation_1.get()), 0, 0, 0},
               0.0F
            );
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, ColorUtil.getColor(0, alpha * this.animation_1.get()), 0, 0},
               0.0F
            );
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, 0, ColorUtil.getColor(0, alpha * this.animation_1.get()), 0},
               0.0F
            );
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, 0, 0, ColorUtil.getColor(0, alpha * this.animation_1.get())},
               0.0F
            );
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{ColorUtil.getColor(255, 0, 0, alpha2 * this.animation_1.get()), 0, 0, 0},
               0.0F
            );
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, ColorUtil.getColor(255, 0, 0, alpha2 * this.animation_1.get()), 0, 0},
               0.0F
            );
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, 0, ColorUtil.getColor(255, 0, 0, alpha2 * this.animation_1.get()), 0},
               0.0F
            );
            RenderUtil.Render2D.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, 0, 0, ColorUtil.getColor(255, 0, 0, alpha2 * this.animation_1.get())},
               0.0F
            );
         }
      }

      matrixStack.popMatrix();
   }
}
