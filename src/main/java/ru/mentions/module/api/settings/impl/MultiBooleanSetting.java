package ru.mentions.module.api.settings.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.animation.Animation;

@Environment(EnvType.CLIENT)
public class MultiBooleanSetting extends Setting<Map<String, BooleanSetting>> {
   private final Map<String, BooleanSetting> settingsMap = new LinkedHashMap<>();
   public Animation animation = new Animation();
   public boolean opened;

   public MultiBooleanSetting(Module parent, String name, BooleanSetting... values) {
      super(parent, name, new LinkedHashMap<>());

      for (BooleanSetting value : values) {
         this.getValue().put(value.getName().toLowerCase(), value);
         this.settingsMap.put(value.getName().toLowerCase(), value);
      }
   }

   public BooleanSetting get(String name) {
      return this.settingsMap.get(name.toLowerCase());
   }

   public boolean getValue(String name) {
      BooleanSetting setting = this.get(name);
      return setting != null && setting.getValue() && this.getVisible().get();
   }

   public Collection<BooleanSetting> getValues() {
      return this.getValue().values();
   }

   public MultiBooleanSetting setVisible(Supplier<Boolean> value) {
      return (MultiBooleanSetting)super.setVisible(value);
   }

   public MultiBooleanSetting onAction(Runnable action) {
      return (MultiBooleanSetting)super.onAction(action);
   }

   public MultiBooleanSetting onSetVisible(Runnable action) {
      return (MultiBooleanSetting)super.onSetVisible(action);
   }

   public String getNames() {
      ArrayList<String> includedOptions = new ArrayList<>();

      for (BooleanSetting option : this.getValues()) {
         if (option.getValue()) {
            includedOptions.add(option.getName());
         }
      }

      return String.join(", ", includedOptions);
   }

   public boolean isAnyTrue() {
      for (BooleanSetting setting : this.settingsMap.values()) {
         if (setting.getValue()) {
            return true;
         }
      }

      return false;
   }

   @Generated
   @Override
   public Animation getAnimation() {
      return this.animation;
   }
}
