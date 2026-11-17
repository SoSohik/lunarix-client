package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.FogBlurPipeline;

@ModuleInfo(
   name = "Fog Blur",
   category = Category.RENDER,
   desc = "Размывает мир за порогом дистанции тумана"
)
@Environment(EnvType.CLIENT)
public final class FogBlur extends Module {
   private final SliderSetting distance = new SliderSetting(this, "Дистанция", 0.05F, 0.001F, 0.5F, 0.001F);
   private final SliderSetting saturation = new SliderSetting(this, "Насыщенность", 0.5F, 0.05F, 0.95F, 0.05F);
   private final BooleanSetting clientColor = new BooleanSetting(this, "Цвет клиента", false);

   @EventHandler(
      priority = 500
   )
   public void onRender(EventRender3D e) {
      if (this.isEnabled()) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            int color1 = ColorUtil.getClientColor1(1);
            int color2 = ColorUtil.getClientColor1(1);
            int color3 = ColorUtil.getClientColor1(270);
            int color4 = ColorUtil.getClientColor1(270);
            FogBlurPipeline.draw(
               this.distance.getValue(),
               Math.max(0.0F, Math.min(1.0F, 1.0F - this.saturation.getValue())),
               this.clientColor.getValue(),
               color1,
               color2,
               color3,
               color4
            );
         }
      }
   }
}
