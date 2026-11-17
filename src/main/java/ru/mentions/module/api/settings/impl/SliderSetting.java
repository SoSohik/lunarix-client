package ru.mentions.module.api.settings.impl;

import java.math.BigDecimal;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;

@Environment(EnvType.CLIENT)
public class SliderSetting extends Setting<Float> {
   public float min;
   public float max;
   public float increment;
   private Float cachedValue;
   private float displayedValue = Float.NaN;
   private float displayedIncrement = Float.NaN;
   private String displayText;
   public Animation animation = new EaseInOutQuad(300, 1.0);
   public Animation animation2 = new EaseInOutQuad(300, 1.0);
   public Animation animation3 = new EaseInOutQuad(300, 1.0);

   public SliderSetting(Module parent, String name, float value, float min, float max, float increment) {
      super(parent, name, value);
      this.min = min;
      this.max = max;
      this.increment = increment;
      this.cachedValue = value;
   }

   public SliderSetting set(Float value) {
      SliderSetting set = (SliderSetting)super.set(value);
      this.cachedValue = (Float)super.getValue();
      return set;
   }

   public String displayValue() {
      float value = this.getValue();
      if (this.displayText == null || Float.compare(value, this.displayedValue) != 0 || Float.compare(this.increment, this.displayedIncrement) != 0) {
         int decimals = 2;
         if (this.increment > 0.0F && Float.isFinite(this.increment)) {
            decimals = Math.max(2, Math.min(6, new BigDecimal(Float.toString(this.increment)).stripTrailingZeros().scale()));
         }

         double scale = Math.pow(10.0, (double)decimals);
         this.displayText = String.valueOf((double)Math.round((double)value * scale) / scale);
         this.displayedValue = value;
         this.displayedIncrement = this.increment;
      }

      return this.displayText;
   }

   public SliderSetting setVisible(Supplier<Boolean> value) {
      return (SliderSetting)super.setVisible(value);
   }

   public SliderSetting onAction(Runnable action) {
      return (SliderSetting)super.onAction(() -> {
         action.run();
         this.cachedValue = (Float)super.getValue();
      });
   }

   public SliderSetting onSetVisible(Runnable action) {
      return (SliderSetting)super.onSetVisible(action);
   }

   public Float getValue() {
      if (this.cachedValue == null) {
         this.cachedValue = (Float)super.getValue();
      }

      return this.cachedValue;
   }
}
