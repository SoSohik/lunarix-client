package ru.mentions.module.api.settings.impl;

import java.util.function.BiPredicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector2f;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.animation.Animation;

@Environment(EnvType.CLIENT)
public class DragSetting extends Setting<Vector2f> {
   public final Vector2f targetPosition = new Vector2f();
   public final Vector2f position = new Vector2f();
   public final Vector2f size = new Vector2f();
   public boolean active = true;
   public boolean structure = false;
   public boolean lockX = false;
   public Animation animationX = new Animation();
   public Animation animationY = new Animation();
   public BiPredicate<Float, Float> dragBlocker;

   public boolean blocksDragAt(float mouseX, float mouseY) {
      return this.dragBlocker != null && this.dragBlocker.test(mouseX, mouseY);
   }

   public DragSetting(Module parent, String name) {
      super(parent, name, new Vector2f());
   }

   public DragSetting(Module parent, String name, Vector2f value) {
      super(parent, name, value);
      this.position.set(value);
      this.targetPosition.set(value);
   }

   public DragSetting(Module parent, String name, Vector2f value, boolean active) {
      super(parent, name, value);
      this.position.set(value);
      this.targetPosition.set(value);
      this.active = active;
   }

   public DragSetting(Module parent, String name, Vector2f value, boolean active, boolean structure) {
      super(parent, name, value);
      this.position.set(value);
      this.targetPosition.set(value);
      this.active = active && !structure;
      this.structure = structure;
   }

   public Setting<?> set(Vector2f value) {
      this.position.set(value);
      this.targetPosition.set(value);
      return super.set(value);
   }

   public Vector2f getValue() {
      return this.position;
   }

   public DragSetting onAction(Runnable action) {
      return (DragSetting)super.onAction(action);
   }

   public DragSetting onSetVisible(Runnable action) {
      return (DragSetting)super.onSetVisible(action);
   }
}
