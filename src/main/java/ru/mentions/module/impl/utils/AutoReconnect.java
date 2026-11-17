package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_412;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_642.class_8678;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.math.StopWatchP;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Auto Reconnect",
   desc = "Автоматически переподключается после кика",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoReconnect extends Module {
   public final SliderSetting delay = new SliderSetting(this, "Задержка (сек)", 10.0F, 5.0F, 30.0F, 1.0F);
   private final StopWatchP timer = new StopWatchP();
   private boolean counting = false;
   private boolean shouldReconnect = false;
   private class_437 parent;
   private class_639 lastAddress;
   private class_642 lastServer;

   public static AutoReconnect get() {
      return Instance.get(AutoReconnect.class);
   }

   public boolean isCounting() {
      return this.counting;
   }

   public String cancelLabel() {
      return "Отменить реконнект";
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.counting = false;
      this.shouldReconnect = false;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.counting = false;
      this.shouldReconnect = false;
   }

   public void onDisconnectedScreenInit(String reason) {
      this.counting = false;
      this.shouldReconnect = false;
      if (this.isEnabled()) {
         if (reason == null) {
            reason = "";
         }

         String lower = reason.toLowerCase();
         if (!lower.contains("banned") && !lower.contains(" ban") && !lower.contains("whitelist")) {
            class_310 mc = AutoReconnect.mc;
            class_642 server = mc.method_1558();
            if (server != null) {
               this.lastServer = server;
               this.lastAddress = class_639.method_2950(server.field_3752);
            } else if (this.lastAddress == null) {
               return;
            }

            this.parent = mc.field_1755;
            this.timer.reset();
            this.counting = true;
            this.shouldReconnect = true;
         }
      }
   }

   public void cancelReconnect() {
      this.counting = false;
      this.shouldReconnect = false;
   }

   public void renderCountdown(class_332 context, int width, int height) {
      if (this.counting) {
         long delayMs = (long)(this.delay.getValue() * 1000.0F);
         long remain = delayMs - this.timer.getTime();
         if (remain < 0L) {
            remain = 0L;
         }

         String text = "Переподключение через " + remain / 1000L + " сек";
         context.method_27534(mc.field_1772, class_2561.method_43470(text), width / 2, height - 70, -1);
         if (remain <= 0L) {
            this.reconnect();
         }
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (this.counting && this.shouldReconnect) {
         if (this.timer.isReached((long)(this.delay.getValue() * 1000.0F))) {
            this.reconnect();
         }
      }
   }

   private void reconnect() {
      if (this.counting && this.shouldReconnect) {
         this.counting = false;
         this.shouldReconnect = false;
         if (this.lastAddress != null) {
            class_310 mc = AutoReconnect.mc;
            class_437 parent = new class_442();
            class_642 server = this.lastServer != null
               ? this.lastServer
               : new class_642("AutoReconnect", this.lastAddress.method_2952() + ":" + this.lastAddress.method_2954(), class_8678.field_45611);
            class_412.method_36877(parent, mc, this.lastAddress, server, true, null);
         }
      }
   }
}
