package ru.mentions.module.impl.render;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1541;
import net.minecraft.class_243;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Tnt Timer",
   desc = "Показывает время до взрыва заправленного ТНТ",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class TntTimer extends Module {
   public SliderSetting scale = new SliderSetting(this, "Размер текста", 8.0F, 5.0F, 16.0F, 0.5F);

   @EventHandler
   public void onDisplay(EventDisplay e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         float fontSize = this.scale.getValue();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            class_1541 tnt;
            if (entity instanceof class_1541 && (tnt = (class_1541)entity).method_5805()) {
               double fuse = (double)tnt.method_6969() / 20.0;
               if (fuse < 0.0) {
                  fuse = 0.0;
               }

               class_243 screen = Projection.worldSpaceToScreenSpace(
                  tnt.method_30950(e.getPartialTicks()).method_1031(0.0, (double)(tnt.method_17682() + 0.5F), 0.0)
               );
               if (!(screen.field_1350 <= 0.0) && !(screen.field_1350 >= 1.0)) {
                  String text = String.format(Locale.ROOT, "%.1f", fuse);
                  int color = fuse < 1.0
                     ? ColorUtil.getColor(255, 60, 60)
                     : (fuse < 2.0 ? ColorUtil.getColor(255, 200, 60) : ColorUtil.getColor(120, 255, 120));
                  float width = Fonts.sf_regular.getWidth(text, fontSize) + 6.0F;
                  float x = (float)screen.field_1352 - width / 2.0F;
                  float y = (float)screen.field_1351 - fontSize;
                  RenderUtil.Render2D.rect(x, y, width, fontSize + 3.0F, ColorUtil.getColor(0, 0, 0, 128), 3.0F);
                  Fonts.sf_regular.draw(text, x + 3.0F, y + 1.5F, fontSize, color);
               }
            }
         }
      }
   }
}
