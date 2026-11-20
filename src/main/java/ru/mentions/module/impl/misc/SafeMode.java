package ru.mentions.module.impl.misc;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.maintenance.CleanupPipeline;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.screen.SafeModeOverlay;

@ModuleInfo(
   name = "SafeMode",
   category = Category.OTHER,
   desc = "Тимчасове вимкнення вибраних модулів і панель керування."
)
@Environment(EnvType.CLIENT)
public class SafeMode extends Module {
   public static final String MODULE = "safe_mode";
   public static final String DISPLAY = "SafeMode";
   public static final String STATUS_START = "Старт";
   public static final String STATUS_STOP = "Зупинити";
   public final BooleanSetting disableCombat = new BooleanSetting(this, "Вимкнути бій", true);
   public final BooleanSetting disableMovement = new BooleanSetting(this, "Вимкнути рух", true);
   public final BooleanSetting disableRender = new BooleanSetting(this, "Вимкнути візуали", false);
   public final BooleanSetting clearLogs = new BooleanSetting(this, "Очищати логи", false);
   public final BooleanSetting restoreOnDisable = new BooleanSetting(this, "Відновлювати при вимкненні", true);
   public final ButtonSetting startButton;
   public final ButtonSetting stopButton;
   public final ButtonSetting panelButton;
   private final List<Module> disabledModules = new ArrayList<>();
   private volatile boolean active;
   private boolean restoreSession;
   private int lastProtectedCount;
   private long lastStartAt;
   private long lastStopAt;
   private String lastError = "";

   public SafeMode() {
      this.startButton = new ButtonSetting(this, "Старт", this::start);
      this.stopButton = new ButtonSetting(this, "Зупинити", this::stop);
      this.panelButton = new ButtonSetting(this, "Панель", this::openOverlay);
   }

   public synchronized void start() {
      if (!this.active) {
         this.lastError = "";
         if (Client.get() != null && Client.get().moduleManager() != null) {
            this.restoreSession = this.restoreOnDisable.getValue();
            this.disabledModules.clear();
            this.active = true;
            this.lastStartAt = System.currentTimeMillis();

            for (Module module : new ArrayList<>(Client.get().moduleManager().values())) {
               if (module != this && module.isEnabled() && module.isAllowDisable() && this.shouldDisable(module)) {
                  try {
                     module.setEnabled(false, false);
                  } catch (RuntimeException var4) {
                     this.recordError(module.getName(), var4);
                  }

                  if (!module.isEnabled()) {
                     this.disabledModules.add(module);
                  }
               }
            }

            this.lastProtectedCount = this.disabledModules.size();
            if (this.clearLogs.getValue()) {
               this.wipeLogs();
            }
         } else {
            this.lastError = "Менеджер модулів недоступний";
         }
      }
   }

   public synchronized void stop() {
      if (this.active) {
         this.lastError = "";
         if (this.restoreSession) {
            for (Module module : new ArrayList<>(this.disabledModules)) {
               try {
                  if (!module.isEnabled()) {
                     module.setEnabled(true, false);
                  }

                  this.disabledModules.remove(module);
               } catch (RuntimeException var4) {
                  this.recordError(module.getName(), var4);
                  if (module.isEnabled()) {
                     this.disabledModules.remove(module);
                  }
               }
            }

            if (!this.disabledModules.isEmpty()) {
               return;
            }
         } else {
            this.disabledModules.clear();
         }

         this.active = false;
         this.lastStopAt = System.currentTimeMillis();
      }
   }

   public synchronized void shutdown() {
      this.stop();
   }

   @Override
   public void setEnabled(boolean enabled, boolean notification) {
      if (!enabled && this.isEnabled()) {
         this.shutdown();
         if (this.active) {
            return;
         }
      }

      super.setEnabled(enabled, notification);
   }

   public boolean inGame() {
      return mc != null && mc.field_1724 != null && mc.field_1687 != null;
   }

   public boolean isActive() {
      return this.active;
   }

   public synchronized int getProtectedCount() {
      return this.active ? this.disabledModules.size() : this.lastProtectedCount;
   }

   public synchronized List<String> getDisabledModuleNames() {
      return this.disabledModules.stream().map(Module::getName).toList();
   }

   public synchronized String getLastError() {
      return this.lastError;
   }

   public long lastStartAt() {
      return this.lastStartAt;
   }

   public long lastStopAt() {
      return this.lastStopAt;
   }

   public String statusLine() {
      return this.active ? "Безпечний режим активний" : "Безпечний режим вимкнено";
   }

   public void openOverlay() {
      mc.execute(() -> mc.method_1507(new SafeModeOverlay(this)));
   }

   private boolean shouldDisable(Module module) {
      return switch (module.getCategory()) {
         case COMBAT -> this.disableCombat.getValue();
         case MOVEMENT -> this.disableMovement.getValue();
         case RENDER -> this.disableRender.getValue();
         default -> false;
      };
   }

   private void recordError(String name, RuntimeException exception) {
      String detail = exception.getMessage();
      String message = name + ": " + (detail != null && !detail.isBlank() ? detail : exception.getClass().getSimpleName());
      this.lastError = this.lastError.isEmpty() ? message : this.lastError + "; " + message;
   }

   private void wipeLogs() {
      // Логи більше не очищаються
   }
}
