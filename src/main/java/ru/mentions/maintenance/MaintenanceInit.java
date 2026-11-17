package ru.mentions.maintenance;

import java.lang.reflect.Method;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.class_437;

public final class MaintenanceInit implements ClientModInitializer {
   private static final String SCREEN_CLASS = "ru.mentions.maintenance.gui.CleanupScreen";

   public void onInitializeClient() {
      if (ModuleState.claimTickHook()) {
         ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
            int ticks = ModuleState.incrementClientTicks();
            if (ticks % 20 == 0) {
               class_437 screen = client.field_1755;
               if (screen != null && "ru.mentions.maintenance.gui.CleanupScreen".equals(screen.getClass().getName())) {
                  refreshStatus(screen);
               }
            }
         });
      }

      if (ModuleState.claimShutdownHook()) {
         ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
            ModuleState.setShutdownRequested(true);
            CleanupPipeline.runOffline();
         });
      }

      DiagLog.note("init", "hooks-installed");
   }

   private static void refreshStatus(class_437 screen) {
      try {
         Method method = screen.getClass().getMethod("onStatusTick");
         method.invoke(screen);
      } catch (Throwable var2) {
         DiagLog.fail("init.tick", var2);
      }
   }
}
