package ru.mentions.module.impl.pasted;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Zoom",
   desc = "Pasted:systemdlc | Плавный оптический зум камеры с настройкой FOV, колесика и чувствительности",
   category = Category.PASTED
)
@Environment(EnvType.CLIENT)
public class ZoomSystem extends Module {
   public final BindSetting zoomKey = new BindSetting(this, "Кнопка зума", 67);
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Зажатие", "Переключение");
   public final SliderSetting zoomFov = new SliderSetting(this, "FOV зума", 30.0F, 5.0F, 80.0F, 1.0F);
   public final SliderSetting speed = new SliderSetting(this, "Скорость", 14.0F, 2.0F, 30.0F, 1.0F);
   public final BooleanSetting scrollControl = new BooleanSetting(this, "Колесико мыши", true);
   public final BooleanSetting smoothSensitivity = new BooleanSetting(this, "Замедление мыши", true);
   private boolean activeZoom = false;
   private float currentFov = -1.0F;
   private float dynamicFov = 30.0F;
   private boolean wasKeyPressed = false;

   public static ZoomSystem getInstance() {
      return Instance.get(ZoomSystem.class);
   }

   @Override
   public void onEnable() {
      this.activeZoom = false;
      this.currentFov = -1.0F;
      this.dynamicFov = this.zoomFov.getValue();
      this.wasKeyPressed = false;
   }

   @Override
   public void onDisable() {
      this.activeZoom = false;
      this.currentFov = -1.0F;
      this.wasKeyPressed = false;
   }

   @EventHandler
   public void onTick(EventTick event) {
      class_310 client = class_310.method_1551();
      if (client != null && client.field_1724 != null) {
         int key = this.zoomKey.getValue() != null ? this.zoomKey.getValue() : -1;
         if (key > 0) {
            long handle = client.method_22683().method_4490();
            boolean isPressed = GLFW.glfwGetKey(handle, key) == 1;
            if ("Зажатие".equals(this.mode.getValue())) {
               this.activeZoom = isPressed;
               if (!this.activeZoom) {
                  this.dynamicFov = this.zoomFov.getValue();
               }
            } else if (isPressed && !this.wasKeyPressed) {
               this.activeZoom = !this.activeZoom;
               if (!this.activeZoom) {
                  this.dynamicFov = this.zoomFov.getValue();
               }
            }

            this.wasKeyPressed = isPressed;
         }
      } else {
         this.activeZoom = false;
      }
   }

   public boolean isZooming() {
      return this.isEnabled() && this.activeZoom;
   }

   public void onScroll(double amount) {
      if (this.scrollControl.getValue() && this.activeZoom) {
         this.dynamicFov = Math.clamp(this.dynamicFov - (float)amount * 5.0F, 5.0F, 90.0F);
      }
   }

   public float modifyFov(float baseFov, float tickDelta) {
      if (this.currentFov < 0.0F) {
         this.currentFov = baseFov;
      }

      float target = this.activeZoom ? this.dynamicFov : baseFov;
      float factor = Math.clamp(this.speed.getValue() * 0.15F * Math.max(0.1F, tickDelta), 0.01F, 1.0F);
      this.currentFov = this.currentFov + (target - this.currentFov) * factor;
      if (Math.abs(this.currentFov - baseFov) < 0.1F && !this.activeZoom) {
         this.currentFov = baseFov;
      }

      return this.currentFov;
   }

   public float getSensitivityFactor() {
      return this.activeZoom && !(this.currentFov <= 0.0F) ? Math.clamp(this.currentFov / 70.0F, 0.1F, 1.0F) : 1.0F;
   }
}
