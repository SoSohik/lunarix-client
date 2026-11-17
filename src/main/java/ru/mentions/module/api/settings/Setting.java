package ru.mentions.module.api.settings;

import java.util.function.Supplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.utils.RussianUi;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public class Setting<Value> implements ISetting, IMinecraft {
   private Runnable onAction;
   private Runnable onSetVisible;
   private Value value;
   public final String name;
   private Supplier<Boolean> visible = () -> true;
   private Module parent;
   private final Animation animation = new Animation();
   private final ru.mentions.utils.animation.satoshi.Animation animation1 = new EaseInOutQuad(300, 1.0);

   public Setting(String name, Value value) {
      this.name = name;
      this.value = value;
   }

   public Setting(Module parent, String name, Value value) {
      this.parent = parent;
      this.name = name;
      this.value = value;
      parent.getSettings().add(this);
   }

   public Setting<?> set(Value value) {
      this.value = value;
      if (mc.field_1687 != null && mc.field_1724 != null && this.onAction != null) {
         this.onAction.run();
      }

      return this;
   }

   @Override
   public Setting<?> setVisible(Supplier<Boolean> value) {
      this.visible = value;
      if (mc.field_1687 != null && mc.field_1724 != null && this.onSetVisible != null) {
         this.onSetVisible.run();
      }

      return this;
   }

   public Setting<?> onAction(Runnable action) {
      this.onAction = action;
      return this;
   }

   public Setting<?> onSetVisible(Runnable action) {
      this.onSetVisible = action;
      return this;
   }

   @Generated
   public Runnable getOnAction() {
      return this.onAction;
   }

   @Generated
   public Runnable getOnSetVisible() {
      return this.onSetVisible;
   }

   @Generated
   public Value getValue() {
      return this.value;
   }

   @Generated
   public Supplier<Boolean> getVisible() {
      return this.visible;
   }

   @Generated
   public Module getParent() {
      return this.parent;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation1() {
      return this.animation1;
   }

   public String getDisplayName() {
      return RussianUi.text(this.name);
   }

   @Generated
   public String getName() {
      return this.name;
   }
}
