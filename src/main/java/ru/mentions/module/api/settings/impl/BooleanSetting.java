package ru.mentions.module.api.settings.impl;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;

@Environment(EnvType.CLIENT)
public class BooleanSetting extends Setting<Boolean> {
   private Boolean cachedValue;
   public Animation animation = new EaseInOutQuad(300, 1.0);
   public Animation animation2 = new EaseInOutQuad(300, 1.0);
   public Animation animation3 = new EaseInOutQuad(300, 1.0);

   public BooleanSetting(String name, Boolean value) {
      super(name, value);
      this.cachedValue = value;
   }

   public BooleanSetting(Module parent, String name, Boolean value) {
      super(parent, name, value);
      this.cachedValue = value;
   }

   public static BooleanSetting of(String name, Boolean value) {
      return new BooleanSetting(name, value);
   }

   public BooleanSetting setVisible(Supplier<Boolean> value) {
      return (BooleanSetting)super.setVisible(value);
   }

   public BooleanSetting set(Boolean value) {
      BooleanSetting set = (BooleanSetting)super.set(value);
      this.cachedValue = (Boolean)super.getValue() && this.getVisible().get();
      return set;
   }

   public BooleanSetting onAction(Runnable action) {
      return (BooleanSetting)super.onAction(() -> {
         action.run();
         this.cachedValue = (Boolean)super.getValue() && this.getVisible().get();
      });
   }

   public BooleanSetting onSetVisible(Runnable action) {
      return (BooleanSetting)super.onSetVisible(action);
   }

   public Boolean getValue() {
      if (this.cachedValue == null) {
         this.cachedValue = (Boolean)super.getValue();
      }

      return this.cachedValue && this.getVisible().get();
   }
}
