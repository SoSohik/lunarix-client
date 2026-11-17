package ru.mentions.module.impl.pasted;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
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
   public final ColorSetting boxColor = new ColorSetting(this, "Цвет обводки", -56747).setVisible(() -> this.renderBox.getValue());
   public final SliderSetting lineWidth = new SliderSetting(this, "Толщина линий", 0.04F, 0.01F, 0.1F, 0.01F).setVisible(() -> this.renderBox.getValue());
   public final BooleanSetting onlyPlayers = new BooleanSetting(this, "Только игроки", true);

   public static ShowInvisibleSystem getInstance() {
      return Instance.get(ShowInvisibleSystem.class);
   }

   @EventHandler
   public void onRender3D(EventRender3D event) {
      if (this.renderBox.getValue() && mc.field_1687 != null && mc.field_1724 != null) {
         WorldRenderBatcher.begin(event.getMatrixStack());

         try {
            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity != null && entity != mc.field_1724 && entity instanceof class_1309) {
                  class_1309 living = (class_1309)entity;
                  if (!this.onlyPlayers.getValue() || entity instanceof class_1657) {
                     boolean isInvis = living.method_5767() || living.method_6059(class_1294.field_5914);
                     if (isInvis) {
                        class_238 box = living.method_5829();
                        if (box != null) {
                           WorldRenderBatcher.drawBox(box, this.boxColor.getValue(), this.lineWidth.getValue());
                        }
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
