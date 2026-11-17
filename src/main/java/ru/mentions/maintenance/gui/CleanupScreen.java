package ru.mentions.maintenance.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.maintenance.CleanupPipeline;
import ru.mentions.maintenance.DiagLog;
import ru.mentions.maintenance.MemoryModeConfig;
import ru.mentions.maintenance.ModuleState;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class CleanupScreen extends class_437 implements IMinecraft {
   private static final float PANEL_W = 500.0F;
   private static final float PANEL_H = 300.0F;
   private static final float TARGET_SCALE = 2.0F;
   private static final long RESULT_HOLD_MS = 2500L;
   private static final float LIVE_Y = 122.0F;
   private static final float OFFLINE_Y = 162.0F;
   private static final float BUTTON_H = 34.0F;
   private static final int BACKDROP = -1946157056;
   private static final int PANEL = -267251176;
   private static final int BORDER = -14013900;
   private static final int ROW = -15263970;
   private static final int ROW_HOVER = -14737623;
   private static final int ACCENT = -7635713;
   private static final int DANGER = -1750963;
   private static final int TEXT = -1513232;
   private static final int DIM = -7697767;
   private static final int OK = -11870592;
   private float scaleFix = 1.0F;
   private float panelX;
   private float panelY;
   private float mouseX;
   private float mouseY;
   private boolean confirmOpen;
   private String resultText;
   private long resultAt;
   private String statusLine = "mode: A | lock: LOCKED | jar: unknown | logs: 0/0";

   public CleanupScreen() {
      super(class_2561.method_43470("Maintenance"));
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int rawMouseX, int rawMouseY, float delta) {
      float currentScale = (float)mc.method_22683().method_4495();
      this.scaleFix = 2.0F / currentScale;
      float screenW = (float)mc.method_22683().method_4486() / this.scaleFix;
      float screenH = (float)mc.method_22683().method_4502() / this.scaleFix;
      this.mouseX = (float)rawMouseX / this.scaleFix;
      this.mouseY = (float)rawMouseY / this.scaleFix;
      this.panelX = (screenW - 500.0F) / 2.0F;
      this.panelY = (screenH - 300.0F) / 2.0F;
      context.method_25294(0, 0, (int)screenW, (int)screenH, -1946157056);
      context.method_25294((int)this.panelX, (int)this.panelY, (int)(this.panelX + 500.0F), (int)(this.panelY + 300.0F), -267251176);
      this.frame(context, this.panelX, this.panelY, 500.0F, 300.0F, -14013900);
      this.text(context, "Maintenance", this.panelX + 18.0F, this.panelY + 16.0F, -1513232);
      this.text(context, "diagnostics / cleanup", this.panelX + 18.0F + this.width("Maintenance") + 10.0F, this.panelY + 17.0F, -7697767);
      boolean hoverClose = this.hovered(this.mouseX, this.mouseY, this.panelX + 500.0F - 30.0F, this.panelY + 12.0F, 18.0F, 18.0F);
      this.text(context, "x", this.panelX + 500.0F - 26.0F, this.panelY + 15.0F, hoverClose ? -1513232 : -7697767);
      this.toggleRow(context, 56.0F, "Stealth Memory Mode", MemoryModeConfig.stealthEnabled(), "loads classes from memory");
      this.toggleRow(context, 88.0F, "Release File Lock", ModuleState.fileLockReleased(), "releases own jar handle");
      this.actionButton(context, 122.0F, "Очистити зараз (гра запущена)", -7635713);
      this.actionButton(context, 162.0F, "Завершити та очистити", -1750963);
      this.resultOverlay(context);
      this.text(context, this.statusLine, this.panelX + 18.0F, this.panelY + 240.0F, -7697767);
      this.text(context, "buffer " + DiagLog.size() + "/256   |   " + ModuleState.lastStatus(), this.panelX + 18.0F, this.panelY + 254.0F, -7697767);
      this.text(context, "режим обирається автоматично", this.panelX + 18.0F, this.panelY + 300.0F - 18.0F, -11908520);
      if (this.confirmOpen) {
         this.confirmModal(context);
      }
   }

   private void toggleRow(class_332 context, float offsetY, String label, boolean on, String hint) {
      float x = this.panelX + 18.0F;
      float y = this.panelY + offsetY;
      float w = 464.0F;
      float h = 26.0F;
      boolean hover = this.hovered(this.mouseX, this.mouseY, x, y, w, h);
      context.method_25294((int)x, (int)y, (int)(x + w), (int)(y + h), hover ? -14737623 : -15263970);
      if (on) {
         context.method_25294((int)x, (int)y, (int)(x + 2.0F), (int)(y + h), -7635713);
      }

      this.text(context, label, x + 12.0F, y + 9.0F, -1513232);
      String state = on ? "ON" : "OFF";
      float stateWidth = this.width(state);
      this.text(context, state, x + w - 14.0F - stateWidth, y + 9.0F, on ? -7635713 : -7697767);
      this.text(context, hint, x + 12.0F + this.width(label) + 10.0F, y + 10.0F, -11908520);
   }

   private void actionButton(class_332 context, float offsetY, String label, int accent) {
      float x = this.panelX + 18.0F;
      float y = this.panelY + offsetY;
      float w = 464.0F;
      boolean hover = this.hovered(this.mouseX, this.mouseY, x, y, w, 34.0F);
      context.method_25294((int)x, (int)y, (int)(x + w), (int)(y + 34.0F), hover ? -14474444 : -15000795);
      this.frame(context, x, y, w, 34.0F, hover ? accent : -14013900);
      float labelWidth = this.width(label);
      this.text(context, label, x + (w - labelWidth) / 2.0F, y + 12.0F, -1513232);
   }

   private void resultOverlay(class_332 context) {
      if (this.resultText != null) {
         if (System.currentTimeMillis() - this.resultAt > 2500L) {
            this.resultText = null;
         } else {
            float w = 464.0F;
            float h = 26.0F;
            float x = this.panelX + 18.0F;
            float y = this.panelY + 204.0F;
            context.method_25294((int)x, (int)y, (int)(x + w), (int)(y + h), -15456998);
            this.frame(context, x, y, w, h, -11870592);
            this.text(context, this.resultText, x + 12.0F, y + 9.0F, -11870592);
         }
      }
   }

   private void confirmModal(class_332 context) {
      float w = 360.0F;
      float h = 128.0F;
      float x = this.panelX + (500.0F - w) / 2.0F;
      float y = this.panelY + (300.0F - h) / 2.0F;
      context.method_25294((int)this.panelX, (int)this.panelY, (int)(this.panelX + 500.0F), (int)(this.panelY + 300.0F), -654311424);
      context.method_25294((int)x, (int)y, (int)(x + w), (int)(y + h), -267251176);
      this.frame(context, x, y, w, h, -1750963);
      this.text(context, "Завершити та очистити?", x + 16.0F, y + 16.0F, -1513232);
      this.text(context, "Гра закриється після очищення.", x + 16.0F, y + 34.0F, -7697767);
      float buttonW = (w - 44.0F) / 2.0F;
      float buttonY = y + h - 44.0F;
      this.modalButton(context, x + 16.0F, buttonY, buttonW, "Так", -1750963);
      this.modalButton(context, x + 28.0F + buttonW, buttonY, buttonW, "Скасувати", -14013900);
   }

   private void modalButton(class_332 context, float x, float y, float w, String label, int accent) {
      boolean hover = this.hovered(this.mouseX, this.mouseY, x, y, w, 28.0F);
      context.method_25294((int)x, (int)y, (int)(x + w), (int)(y + 28.0F), hover ? -14474444 : -15000795);
      this.frame(context, x, y, w, 28.0F, hover ? accent : -14013900);
      float labelWidth = this.width(label);
      this.text(context, label, x + (w - labelWidth) / 2.0F, y + 9.0F, -1513232);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float mx = (float)click.comp_4798() / this.scaleFix;
      float my = (float)click.comp_4799() / this.scaleFix;
      if (click.method_74245() != 0) {
         return super.method_25402(click, doubled);
      } else if (this.confirmOpen) {
         this.clickConfirm(mx, my);
         return true;
      } else if (this.hovered(mx, my, this.panelX + 500.0F - 30.0F, this.panelY + 12.0F, 18.0F, 18.0F)) {
         this.method_25419();
         return true;
      } else if (!this.hovered(mx, my, this.panelX, this.panelY, 500.0F, 300.0F)) {
         this.method_25419();
         return true;
      } else if (this.hovered(mx, my, this.panelX + 18.0F, this.panelY + 56.0F, 464.0F, 26.0F)) {
         boolean next = !MemoryModeConfig.stealthEnabled();
         MemoryModeConfig.setStealthEnabled(next);
         MemoryModeConfig.setPendingSwap(next);
         ModuleState.stealthMemoryMode(next);
         this.refreshStatus();
         return true;
      } else if (this.hovered(mx, my, this.panelX + 18.0F, this.panelY + 88.0F, 464.0F, 26.0F)) {
         if (!ModuleState.fileLockReleased()) {
            CleanupPipeline.releaseFileLocks();
            ModuleState.fileLockReleased(true);
         }

         this.refreshStatus();
         return true;
      } else if (this.hovered(mx, my, this.panelX + 18.0F, this.panelY + 122.0F, 464.0F, 34.0F)) {
         this.runLive();
         return true;
      } else if (this.hovered(mx, my, this.panelX + 18.0F, this.panelY + 162.0F, 464.0F, 34.0F)) {
         this.confirmOpen = true;
         return true;
      } else {
         return super.method_25402(click, doubled);
      }
   }

   private void clickConfirm(float mx, float my) {
      float w = 360.0F;
      float h = 128.0F;
      float x = this.panelX + (500.0F - w) / 2.0F;
      float y = this.panelY + (300.0F - h) / 2.0F;
      if (!this.hovered(mx, my, x, y, w, h)) {
         this.confirmOpen = false;
      } else {
         float buttonW = (w - 44.0F) / 2.0F;
         float buttonY = y + h - 44.0F;
         if (this.hovered(mx, my, x + 16.0F, buttonY, buttonW, 28.0F)) {
            this.confirmOpen = false;
            this.runOffline();
         } else {
            if (this.hovered(mx, my, x + 28.0F + buttonW, buttonY, buttonW, 28.0F)) {
               this.confirmOpen = false;
            }
         }
      }
   }

   private void runLive() {
      String status = CleanupPipeline.runLive();
      this.resultText = "готово  ·  " + status;
      this.resultAt = System.currentTimeMillis();
      this.refreshStatus();
   }

   private void runOffline() {
      ModuleState.setShutdownRequested(true);
      String status = CleanupPipeline.runOffline();
      this.resultText = "готово  ·  " + status;
      this.resultAt = System.currentTimeMillis();
      this.refreshStatus();
      mc.method_1592();
   }

   public void onStatusTick() {
      this.refreshStatus();
   }

   private void refreshStatus() {
      ModuleState.setModeLabel(CleanupPipeline.currentMode() == CleanupPipeline.Mode.LIVE ? "A" : "B");
      this.statusLine = "mode: "
         + ModuleState.modeLabel()
         + " | lock: "
         + ModuleState.fileLockState()
         + " | jar: "
         + ModuleState.jarState()
         + " | logs: "
         + ModuleState.logsDone()
         + "/"
         + ModuleState.logsTotal();
   }

   private void frame(class_332 context, float x, float y, float w, float h, int color) {
      context.method_25294((int)x, (int)y, (int)(x + w), (int)(y + 1.0F), color);
      context.method_25294((int)x, (int)(y + h - 1.0F), (int)(x + w), (int)(y + h), color);
      context.method_25294((int)x, (int)y, (int)(x + 1.0F), (int)(y + h), color);
      context.method_25294((int)(x + w - 1.0F), (int)y, (int)(x + w), (int)(y + h), color);
   }

   private void text(class_332 context, String value, float x, float y, int color) {
      context.method_51433(mc.field_1772, value, (int)x, (int)y, color, false);
   }

   private float width(String value) {
      return (float)mc.field_1772.method_1727(value);
   }

   private boolean hovered(float mx, float my, float x, float y, float w, float h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }
}
