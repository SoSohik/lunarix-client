package ru.mentions.module.api.settings.impl;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public class ColorSetting extends Setting<Integer> {
   private Integer cachedValue;
   public boolean pickerOpen = false;
   public Animation pickerAnim = new EaseInOutQuad(220, 1.0);

   public ColorSetting(Module parent, String name) {
      super(parent, name, ColorUtil.RED);
      this.cachedValue = ColorUtil.RED;
   }

   public ColorSetting(Module parent, String name, Integer value) {
      super(parent, name, value);
      this.cachedValue = value;
   }

   public ColorSetting set(Integer value) {
      ColorSetting set = (ColorSetting)super.set(value);
      this.cachedValue = (Integer)super.getValue();
      return set;
   }

   public ColorSetting setVisible(Supplier<Boolean> value) {
      return (ColorSetting)super.setVisible(value);
   }

   public ColorSetting onAction(Runnable action) {
      return (ColorSetting)super.onAction(() -> {
         action.run();
         this.cachedValue = (Integer)super.getValue();
      });
   }

   public ColorSetting onSetVisible(Runnable action) {
      return (ColorSetting)super.onSetVisible(action);
   }

   public Integer getValue() {
      if (this.cachedValue == null) {
         this.cachedValue = (Integer)super.getValue();
      }

      return this.cachedValue;
   }
}
