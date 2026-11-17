package ru.mentions.module.api.preview;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.DelimiterSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.screen.ClickGuiScreen;
import ru.mentions.screen.PreviewEditor;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class PreviewSettings {
   public final DelimiterSetting title;
   public final SliderSetting distance;
   public final SliderSetting height;
   public final SliderSetting interval;

   private PreviewSettings(Module parent, Float defaultDistance, Float defaultHeight, Float defaultInterval) {
      boolean anySlider = defaultDistance != null || defaultHeight != null || defaultInterval != null;
      this.title = !anySlider ? null : new DelimiterSetting(parent, "Настройки предпоказа").setVisible(() -> isEditing(parent));
      this.distance = defaultDistance == null
         ? null
         : new SliderSetting(parent, "Дистанция спавна", defaultDistance, 1.5F, 14.0F, 0.5F).setVisible(() -> isEditing(parent));
      this.height = defaultHeight == null
         ? null
         : new SliderSetting(parent, "Высота спавна", defaultHeight, -2.0F, 4.0F, 0.1F).setVisible(() -> isEditing(parent));
      this.interval = defaultInterval == null
         ? null
         : new SliderSetting(parent, "Интервал показа", defaultInterval, 0.5F, 10.0F, 0.5F).setVisible(() -> isEditing(parent));
   }

   public static PreviewSettings of(Module parent, float distance, float height, float interval) {
      return new PreviewSettings(parent, distance, height, interval);
   }

   public static PreviewSettings withoutInterval(Module parent, float distance, float height) {
      return new PreviewSettings(parent, distance, height, null);
   }

   public static PreviewSettings intervalOnly(Module parent, float interval) {
      return new PreviewSettings(parent, null, null, interval);
   }

   public static PreviewSettings none(Module parent) {
      return new PreviewSettings(parent, null, null, null);
   }

   public boolean repeats() {
      return this.interval != null;
   }

   public long intervalMs() {
      return this.interval == null ? 0L : (long)(this.interval.getValue() * 1000.0F);
   }

   public float distance(float fallback) {
      return this.distance == null ? fallback : this.distance.getValue();
   }

   public float height(float fallback) {
      return this.height == null ? fallback : this.height.getValue();
   }

   private static boolean isEditing(Module parent) {
      return PreviewEditor.getInstance().isEditing(parent);
   }

   public static ButtonSetting button(Module parent) {
      return new ButtonSetting(parent, "Предпоказ", () -> {
         class_437 patt0$temp = IMinecraft.mc.field_1755;
         if (patt0$temp instanceof ClickGuiScreen) {
            ((ClickGuiScreen)patt0$temp).openPreviewEditor(parent);
         }
      }).setVisible(() -> !isEditing(parent));
   }
}
