package ru.mentions.module.api.settings.impl;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;

@Environment(EnvType.CLIENT)
public class ButtonSetting extends Setting<Boolean> {
   private final Runnable action;
   public final Animation pressAnim = new Animation();

   public ButtonSetting(Module parent, String name, Runnable action) {
      super(parent, name, false);
      this.action = action;
   }

   public void press() {
      if (this.action != null) {
         this.action.run();
      }

      this.pressAnim.set(1.0);
      this.pressAnim.run(0.0, 0.45F, Easings.QUAD_OUT);
   }

   public ButtonSetting setVisible(Supplier<Boolean> value) {
      return (ButtonSetting)super.setVisible(value);
   }
}
