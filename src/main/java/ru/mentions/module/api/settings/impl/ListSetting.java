package ru.mentions.module.api.settings.impl;

import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;

@Environment(EnvType.CLIENT)
public class ListSetting<T> extends Setting<T> {
   public List<T> values;
   private T cachedValue;

   @SafeVarargs
   public ListSetting(Module parent, String name, T... values) {
      super(parent, name, values[0]);
      this.values = List.of(values);
      this.set(values[0]);
      this.cachedValue = values[0];
   }

   public int getIndex() {
      int index = 0;

      for (T value : this.values) {
         if (value.equals(this.getValue())) {
            return index;
         }

         index++;
      }

      return 0;
   }

   public boolean is(T value) {
      return this.getValue().equals(value);
   }

   public ListSetting<T> set(T value) {
      ListSetting set = (ListSetting)super.set(value);
      this.cachedValue = (T)super.getValue();
      return set;
   }

   public ListSetting<T> setAsObject(Object value) {
      ListSetting set = (ListSetting)super.set(value);
      this.cachedValue = (T)super.getValue();
      return set;
   }

   public ListSetting<T> setVisible(Supplier<Boolean> value) {
      return (ListSetting<T>)super.setVisible(value);
   }

   public ListSetting<T> onAction(Runnable action) {
      return (ListSetting<T>)super.onAction(() -> {
         action.run();
         this.cachedValue = (T)super.getValue();
      });
   }

   public ListSetting<T> onSetVisible(Runnable action) {
      return (ListSetting<T>)super.onSetVisible(action);
   }

   @Override
   public T getValue() {
      if (this.cachedValue == null) {
         this.cachedValue = (T)super.getValue();
      }

      return this.cachedValue;
   }
}
