package ru.mentions.manager.event_impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_4587;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class RenderItemEvent extends Event {
   private final class_4587 matrix;
   private final class_1268 hand;
   private final class_1306 arm;

   public RenderItemEvent(class_4587 matrix, class_1268 hand, class_1306 arm) {
      this.matrix = matrix;
      this.hand = hand;
      this.arm = arm;
   }

   public class_4587 getMatrix() {
      return this.matrix;
   }

   public class_1268 getHand() {
      return this.hand;
   }

   public class_1306 getArm() {
      return this.arm;
   }

   public boolean isRightHand() {
      return this.arm == class_1306.field_6183;
   }
}
