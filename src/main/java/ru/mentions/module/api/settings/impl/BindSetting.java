package ru.mentions.module.api.settings.impl;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;

@Environment(EnvType.CLIENT)
public class BindSetting extends Setting<Integer> {
   public final boolean allowMouse;
   public Animation animation = new EaseInOutQuad(300, 1.0);
   private Integer cachedValue;

   public BindSetting(Module parent, String name) {
      super(parent, name, -1);
      this.allowMouse = true;
      this.cachedValue = -1;
   }

   public BindSetting(Module parent, String name, boolean allowMouse) {
      super(parent, name, -1);
      this.allowMouse = allowMouse;
      this.cachedValue = -1;
   }

   public BindSetting(Module parent, String name, Integer value) {
      super(parent, name, value);
      this.allowMouse = true;
      this.cachedValue = value;
   }

   public BindSetting(Module parent, String name, Integer value, boolean allowMouse) {
      super(parent, name, value);
      this.allowMouse = allowMouse;
      this.cachedValue = value;
   }

   public BindSetting setVisible(Supplier<Boolean> value) {
      return (BindSetting)super.setVisible(value);
   }

   public BindSetting set(Integer value) {
      BindSetting set = (BindSetting)super.set(value);
      this.cachedValue = (Integer)super.getValue();
      return set;
   }

   public BindSetting onAction(Runnable action) {
      return (BindSetting)super.onAction(() -> {
         action.run();
         this.cachedValue = (Integer)super.getValue();
      });
   }

   public BindSetting onSetVisible(Runnable action) {
      return (BindSetting)super.onSetVisible(action);
   }

   public Integer get() {
      if (this.cachedValue == null) {
         this.cachedValue = (Integer)super.getValue();
      }

      return this.cachedValue;
   }

   public Integer getValue() {
      if (this.cachedValue == null) {
         this.cachedValue = (Integer)super.getValue();
      }

      return this.cachedValue;
   }
}
