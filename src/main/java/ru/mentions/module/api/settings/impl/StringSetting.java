package ru.mentions.module.api.settings.impl;

import java.util.function.Supplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;

@Environment(EnvType.CLIENT)
public class StringSetting extends Setting<String> {
   private final boolean onlyNumber;
   private String cachedValue;

   public StringSetting(Module parent, String name, String value) {
      super(parent, name, value);
      this.onlyNumber = false;
      this.cachedValue = value;
   }

   public StringSetting(Module parent, String name, String value, boolean onlyNumber) {
      super(parent, name, value);
      this.onlyNumber = onlyNumber;
      this.cachedValue = value;
   }

   public StringSetting set(String value) {
      StringSetting set = (StringSetting)super.set(value);
      this.cachedValue = (String)super.getValue();
      return set;
   }

   public StringSetting setVisible(Supplier<Boolean> value) {
      return (StringSetting)super.setVisible(value);
   }

   public StringSetting onAction(Runnable action) {
      return (StringSetting)super.onAction(() -> {
         action.run();
         this.cachedValue = (String)super.getValue();
      });
   }

   public StringSetting onSetVisible(Runnable action) {
      return (StringSetting)super.onSetVisible(action);
   }

   public String getValue() {
      if (this.cachedValue == null) {
         this.cachedValue = (String)super.getValue();
      }

      return this.cachedValue;
   }

   @Generated
   public boolean isOnlyNumber() {
      return this.onlyNumber;
   }

   @Generated
   public String getCachedValue() {
      return this.cachedValue;
   }
}
