package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_5498;
import org.lwjgl.glfw.GLFW;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.FreeLookUtil;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.impl.combat.AttackAura;

@ModuleInfo(
   name = "Free Look",
   desc = "Позволяет крутить свободно камерой.",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class FreeLook extends Module {
   public ModeSetting type = new ModeSetting(this, "Режим", "Зажать", "Нажать");
   public BindSetting bind = new BindSetting(this, "Клавиша", -1);
   private boolean looking;

   @Override
   public void onDisable() {
      if (this.looking) {
         this.stopLook();
      }
   }

   @EventHandler
   public void onKey(EventKey event) {
      int key = this.bind.get();
      if (key > 0 && event.getKey() == key) {
         if (this.type.is("Нажать")) {
            if (this.looking) {
               this.stopLook();
            } else {
               this.startLook();
            }
         } else if (!this.looking) {
            this.startLook();
         }
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      int key = this.bind.get();
      if (key <= 0) {
         if (this.looking) {
            this.stopLook();
         }
      } else {
         if (this.type.is("Зажать")) {
            boolean down = mc.method_22683() != null && GLFW.glfwGetKey(mc.method_22683().method_4490(), key) == 1;
            if (down && !this.looking) {
               this.startLook();
            } else if (!down && this.looking) {
               this.stopLook();
            }
         }

         if (this.looking) {
            FreeLookUtil.active = true;
         }
      }
   }

   private void startLook() {
      if (mc.field_1724 != null) {
         this.looking = true;
         FreeLookUtil.freeYaw = mc.field_1724.method_36454();
         FreeLookUtil.freePitch = mc.field_1724.method_36455();
         FreeLookUtil.forced = true;
         FreeLookUtil.active = true;
         mc.field_1690.method_31043(class_5498.field_26665);
      }
   }

   private void stopLook() {
      this.looking = false;
      FreeLookUtil.forced = false;
      AttackAura aura = AttackAura.get();
      if (aura == null || !aura.isEnabled()) {
         FreeLookUtil.active = false;
      }

      if (mc.field_1690 != null) {
         mc.field_1690.method_31043(class_5498.field_26664);
      }
   }
}
