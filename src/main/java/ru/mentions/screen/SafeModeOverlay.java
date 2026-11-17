package ru.mentions.screen;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.Client;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.impl.misc.SafeMode;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class SafeModeOverlay extends class_437 implements IMinecraft {
   public static final float PANEL_W = 360.0F;
   public static final float PANEL_H = 340.0F;
   public static final float TARGET_SCALE = 2.0F;
   public static final float BOX_W = 328.0F;
   public static final float BOX_H = 150.0F;
   public static final float BUTTON_H = 32.0F;
   private static final int PANEL = -15460318;
   private static final int SURFACE = -14867664;
   private static final int BORDER = -12498079;
   private static final int TEXT = -986118;
   private static final int DIM = -5392179;
   private static final int ACCENT = -5330177;
   private static final int SUCCESS = -8592467;
   private static final int ERROR = -22614;
   private static final int PAGE_SIZE = 4;
   private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
   private final SafeMode safeMode;
   private float scaleFix = 1.0F;
   private float panelX;
   private float panelY;
   private boolean settingsTab;
   private int page;
   private int focus = -1;
   private boolean keyboardFocus;
   private boolean busy;
   private String actionError = "";

   public SafeModeOverlay() {
      this(null);
   }

   public SafeModeOverlay(SafeMode module) {
      super(class_2561.method_43470("SafeMode"));
      this.safeMode = module != null ? module : findModule();
   }

   private static SafeMode findModule() {
      return Client.get() != null && Client.get().moduleManager() != null ? Client.get().moduleManager().get(SafeMode.class) : null;
   }

   private boolean active() {
      return this.safeMode != null && this.safeMode.isActive();
   }

   private void layout() {
      float w = (float)mc.method_22683().method_4486();
      float h = (float)mc.method_22683().method_4502();
      float guiScale = (float)mc.method_22683().method_4495();
      float preferred = guiScale > 0.0F ? 2.0F / guiScale : 1.0F;
      this.scaleFix = Math.max(0.01F, Math.min(preferred, Math.min(Math.max(1.0F, w - 16.0F) / 360.0F, Math.max(1.0F, h - 16.0F) / 340.0F)));
      this.panelX = (w / this.scaleFix - 360.0F) / 2.0F;
      this.panelY = (h / this.scaleFix - 340.0F) / 2.0F;
   }

   private List<String> names() {
      if (this.safeMode == null) {
         return List.of();
      } else {
         List<String> names = this.safeMode.getDisabledModuleNames();
         return (List<String>)(names == null ? List.of() : new ArrayList<>(names));
      }
   }

   private int pages() {
      return Math.max(1, (this.names().size() + 4 - 1) / 4);
   }

   private void changePage(int direction) {
      this.page = Math.max(0, Math.min(this.pages() - 1, this.page + direction));
   }

   private BooleanSetting[] settings() {
      return new BooleanSetting[]{this.safeMode.disableCombat, this.safeMode.disableMovement, this.safeMode.disableRender, this.safeMode.restoreOnDisable};
   }

   private List<SafeModeOverlay.Control> controls() {
      List<SafeModeOverlay.Control> controls = new ArrayList<>();
      boolean available = this.safeMode != null && !this.busy;
      boolean active = this.active();
      controls.add(new SafeModeOverlay.Control(286.0F, 12.0F, 58.0F, 24.0F, "Закрити", true, this::method_25419));
      controls.add(new SafeModeOverlay.Control(16.0F, 78.0F, 160.0F, 26.0F, "Модулі", true, () -> this.settingsTab = false));
      controls.add(new SafeModeOverlay.Control(184.0F, 78.0F, 160.0F, 26.0F, "Налаштування", true, () -> this.settingsTab = true));
      if (this.settingsTab && this.safeMode != null) {
         String[] labels = new String[]{"Вимикати бій", "Вимикати рух", "Вимикати візуали", "Відновлювати модулі"};
         BooleanSetting[] settings = this.settings();

         for (int i = 0; i < settings.length; i++) {
            BooleanSetting setting = settings[i];
            controls.add(
               new SafeModeOverlay.Control(
                  24.0F,
                  (float)(116 + i * 25),
                  312.0F,
                  22.0F,
                  labels[i] + ": " + (setting.getValue() ? "Так" : "Ні"),
                  available && !active,
                  () -> setting.set(!setting.getValue())
               )
            );
         }
      } else if (!this.settingsTab) {
         controls.add(new SafeModeOverlay.Control(244.0F, 220.0F, 42.0F, 22.0F, "<", this.page > 0, () -> this.changePage(-1)));
         controls.add(new SafeModeOverlay.Control(294.0F, 220.0F, 42.0F, 22.0F, ">", this.page + 1 < this.pages(), () -> this.changePage(1)));
      }

      controls.add(new SafeModeOverlay.Control(16.0F, 278.0F, 160.0F, 32.0F, "Старт", available && !active, () -> this.runAction(true)));
      controls.add(new SafeModeOverlay.Control(184.0F, 278.0F, 160.0F, 32.0F, "Зупинити", available && active, () -> this.runAction(false)));
      return controls;
   }

   private void runAction(boolean start) {
      if (this.safeMode != null && !this.busy && start != this.active()) {
         this.busy = true;
         this.actionError = "";

         try {
            if (start) {
               this.safeMode.start();
            } else {
               this.safeMode.stop();
            }

            this.page = 0;
         } catch (RuntimeException var7) {
            String detail = var7.getMessage();
            this.actionError = detail != null && !detail.isBlank() ? detail : var7.getClass().getSimpleName();
         } finally {
            this.busy = false;
         }
      }
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int rawMouseX, int rawMouseY, float delta) {
      this.layout();
      context.method_25294(0, 0, mc.method_22683().method_4486(), mc.method_22683().method_4502(), -1342177280);
      float mx = (float)rawMouseX / this.scaleFix - this.panelX;
      float my = (float)rawMouseY / this.scaleFix - this.panelY;
      context.method_51448().pushMatrix();

      try {
         context.method_51448().scale(this.scaleFix, this.scaleFix);
         context.method_51448().translate(this.panelX, this.panelY);
         this.rect(context, 3.0F, 4.0F, 360.0F, 340.0F, 1610612736);
         this.rect(context, 0.0F, 0.0F, 360.0F, 340.0F, -15460318);
         this.frame(context, 0.0F, 0.0F, 360.0F, 340.0F, -12498079);
         this.rect(context, 0.0F, 0.0F, 360.0F, 2.0F, -5330177);
         this.text(context, "SafeMode", 16.0F, 16.0F, -986118, 250.0F);
         long pid = ProcessHandle.current().pid();
         this.text(context, "PID " + pid, 16.0F, 32.0F, -5392179, 320.0F);
         boolean active = this.active();
         this.rect(context, 16.0F, 52.0F, 4.0F, 14.0F, active ? -8592467 : -5392179);
         String status = this.safeMode == null ? "Модуль SafeMode недоступний" : this.safeMode.statusLine();
         this.text(context, status, 28.0F, 55.0F, active ? -8592467 : -5392179, 310.0F);
         this.rect(context, 16.0F, 110.0F, 328.0F, 136.0F, -14867664);
         if (this.settingsTab) {
            if (this.safeMode == null) {
               this.text(context, "Налаштування недоступні", 26.0F, 126.0F, -5392179, 300.0F);
            } else {
               this.text(context, active ? "Зупиніть режим для зміни налаштувань" : "Категорії застосуються під час старту", 26.0F, 226.0F, -5392179, 300.0F);
            }
         } else {
            List<String> names = this.names();
            this.page = Math.min(this.page, Math.max(0, (names.size() - 1) / 4));
            this.text(context, "Вимкнені модулі: " + names.size(), 26.0F, 119.0F, -986118, 300.0F);
            if (names.isEmpty()) {
               this.text(context, active ? "Жодного модуля не вимкнено" : "Режим ще не запущено або вже зупинено", 26.0F, 150.0F, -5392179, 300.0F);
               this.text(context, "Список містить лише фактичні зміни", 26.0F, 170.0F, -5392179, 300.0F);
            } else {
               for (int row = 0; row < 4; row++) {
                  int index = this.page * 4 + row;
                  if (index >= names.size()) {
                     break;
                  }

                  this.text(context, names.get(index), 28.0F, (float)(141 + row * 18), -986118, 220.0F);
                  this.text(context, "Вимкнено", 260.0F, (float)(141 + row * 18), -8592467, 72.0F);
               }
            }

            this.text(context, "Сторінка " + (this.page + 1) + " / " + this.pages(), 26.0F, 227.0F, -5392179, 210.0F);
         }

         String error = this.actionError;
         if (error.isEmpty() && this.safeMode != null) {
            error = this.safeMode.getLastError();
         }

         if (error != null && !error.isBlank()) {
            this.text(context, "Помилка: " + error, 16.0F, 254.0F, -22614, 328.0F);
         } else {
            this.text(context, "Остання дія: " + this.lastAction(), 16.0F, 254.0F, -5392179, 328.0F);
         }

         List<SafeModeOverlay.Control> controls = this.controls();

         for (int i = 0; i < controls.size(); i++) {
            SafeModeOverlay.Control control = controls.get(i);
            boolean hover = control.contains(mx, my) && control.enabled;
            boolean selected = i == (this.settingsTab ? 2 : 1);
            boolean focused = this.keyboardFocus && this.focus == i && control.enabled;
            this.rect(
               context,
               control.x,
               control.y,
               control.w,
               control.h,
               !control.enabled ? -14538955 : (!hover && !focused ? (selected ? -13355951 : -14012094) : -12433304)
            );
            this.frame(context, control.x, control.y, control.w, control.h, !focused && !selected ? -12498079 : -5330177);
            float labelW = (float)mc.field_1772.method_1727(control.label);
            this.text(
               context,
               control.label,
               control.x + Math.max(6.0F, (control.w - labelW) / 2.0F),
               control.y + (control.h - 8.0F) / 2.0F,
               control.enabled ? -986118 : -7695192,
               control.w - 12.0F
            );
         }

         this.text(context, "Esc — закрити; стан режиму не змінюється", 16.0F, 321.0F, -5392179, 328.0F);
      } finally {
         context.method_51448().popMatrix();
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (click.method_74245() != 0) {
         return super.method_25402(click, doubled);
      } else {
         this.layout();
         this.keyboardFocus = false;
         float mx = (float)click.comp_4798() / this.scaleFix - this.panelX;
         float my = (float)click.comp_4799() / this.scaleFix - this.panelY;

         for (SafeModeOverlay.Control control : this.controls()) {
            if (control.contains(mx, my)) {
               if (control.enabled) {
                  control.action.run();
               }

               return true;
            }
         }

         return true;
      }
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (key == 256) {
         this.method_25419();
         return true;
      } else {
         List<SafeModeOverlay.Control> controls = this.controls();
         if (key == 258) {
            int direction = (input.comp_4797() & 1) != 0 ? -1 : 1;

            for (int i = 0; i < controls.size(); i++) {
               this.focus = Math.floorMod(this.focus + direction, controls.size());
               if (controls.get(this.focus).enabled) {
                  break;
               }
            }

            this.keyboardFocus = true;
            return true;
         } else if (this.keyboardFocus && (key == 257 || key == 335 || key == 32)) {
            if (this.focus >= 0 && this.focus < controls.size() && controls.get(this.focus).enabled) {
               controls.get(this.focus).action.run();
               this.focus = -1;
            }

            return true;
         } else {
            return super.method_25404(input);
         }
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      this.layout();
      float mx = (float)mouseX / this.scaleFix - this.panelX;
      float my = (float)mouseY / this.scaleFix - this.panelY;
      if (!this.settingsTab && mx >= 16.0F && mx < 344.0F && my >= 110.0F && my < 246.0F && vertical != 0.0) {
         this.changePage(vertical > 0.0 ? -1 : 1);
         return true;
      } else {
         return super.method_25401(mouseX, mouseY, horizontal, vertical);
      }
   }

   public boolean method_25421() {
      return false;
   }

   private String lastAction() {
      if (this.safeMode == null) {
         return "—";
      } else {
         long start = this.safeMode.lastStartAt();
         long stop = this.safeMode.lastStopAt();
         long latest = Math.max(start, stop);
         return latest <= 0L ? "—" : (start > stop ? "Старт" : "Зупинка") + " · " + CLOCK.format(Instant.ofEpochMilli(latest).atZone(ZoneId.systemDefault()));
      }
   }

   private void text(class_332 context, String value, float x, float y, int color, float maxWidth) {
      if (value != null) {
         String line = value.replace('\n', ' ').replace('\r', ' ');
         if ((float)mc.field_1772.method_1727(line) > maxWidth) {
            float available = Math.max(0.0F, maxWidth - (float)mc.field_1772.method_1727("..."));

            while (!line.isEmpty() && (float)mc.field_1772.method_1727(line) > available) {
               line = line.substring(0, line.offsetByCodePoints(line.length(), -1));
            }

            line = line + "...";
         }

         context.method_51433(mc.field_1772, line, (int)x, (int)y, color, false);
      }
   }

   private void rect(class_332 context, float x, float y, float w, float h, int color) {
      context.method_25294((int)x, (int)y, (int)(x + w), (int)(y + h), color);
   }

   private void frame(class_332 context, float x, float y, float w, float h, int color) {
      this.rect(context, x, y, w, 1.0F, color);
      this.rect(context, x, y + h - 1.0F, w, 1.0F, color);
      this.rect(context, x, y, 1.0F, h, color);
      this.rect(context, x + w - 1.0F, y, 1.0F, h, color);
   }

   private static record Control(float x, float y, float w, float h, String label, boolean enabled, Runnable action) {
      boolean contains(float mx, float my) {
         return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
      }
   }
}
