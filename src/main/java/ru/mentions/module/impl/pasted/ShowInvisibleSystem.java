package ru.mentions.module.impl.pasted;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.render.WorldRenderBatcher;

@ModuleInfo(
   name = "Show Invisible",
   desc = "Отображает невидимых игроков и подсвечивает их",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ShowInvisibleSystem extends Module {
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Обычный", "Обычный", "Полупрозрачный");
   public final BooleanSetting renderModel = new BooleanSetting(this, "Модель игрока", true);
   public final BooleanSetting renderBox = new BooleanSetting(this, "3D Бокс", true);
   public final ModeSetting boxMode = new ModeSetting(this, "Тип бокса", "Оба", "Оба", "Обводка", "Заливка").setVisible(() -> this.renderBox.getValue());
   public final ColorSetting boxColor = new ColorSetting(this, "Цвет обводки", -56747).setVisible(() -> this.renderBox.getValue());
   public final SliderSetting fillAlpha = new SliderSetting(this, "Прозрачность", 0.25F, 0.05F, 1.0F, 0.05F).setVisible(() -> this.renderBox.getValue());
   public final BooleanSetting onlyPlayers = new BooleanSetting(this, "Только игроки", true);

   public static ShowInvisibleSystem getInstance() {
      return Instance.get(ShowInvisibleSystem.class);
   }

   @EventHandler
   public void onRender3D(EventRender3D event) {
      if (this.renderBox.getValue() && mc.field_1687 != null && mc.field_1724 != null) {
         boolean wantFill = this.boxMode.is("Заливка") || this.boxMode.is("Оба");
         boolean wantLine = this.boxMode.is("Обводка") || this.boxMode.is("Оба");
         int baseColor = this.boxColor.getValue();
         int lineC = wantLine ? ColorUtil.replAlpha(baseColor, 255) : 0;
         int fillC = wantFill ? ColorUtil.replAlpha(baseColor, (int)(this.fillAlpha.getValue() * 255.0F)) : 0;

         WorldRenderBatcher.begin(event.getMatrixStack());
         try {
            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity != null && entity != mc.field_1724 && entity instanceof class_1309) {
                  class_1309 living = (class_1309)entity;
                  if (!this.onlyPlayers.getValue() || entity instanceof class_1657) {
                     if (living.method_5767() || living.method_6059(class_1294.field_5914)) {
                        class_243 pos = entity.method_30950(event.getTickDelta());
                        float width = entity.method_17681();
                        float height = entity.method_17682();
                        WorldRenderBatcher.drawChamsBox(pos.field_1352, pos.field_1351, pos.field_1350, width, height, lineC, fillC);
                     }
                  }
               }
            }
         } finally {
            WorldRenderBatcher.end();
         }
      }
   }
}
