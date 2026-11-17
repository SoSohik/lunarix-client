package ru.mentions.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.lang.Lang;
import ru.mentions.module.api.settings.impl.PixelGridSetting;
import ru.mentions.module.impl.render.CrossHair;
import ru.mentions.screen.editor.EditorButton;
import ru.mentions.screen.editor.EditorWidgets;
import ru.mentions.screen.editor.OverlayEditor;
import ru.mentions.screen.editor.Rect;
import ru.mentions.screen.editor.SettingsPanel;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class CrosshairEditor implements OverlayEditor, IMinecraft {
   private static final CrosshairEditor INSTANCE = new CrosshairEditor();
   private final EditorWidgets widgets = new EditorWidgets();
   private final SettingsPanel panel = new SettingsPanel();
   private final EditorButton saveButton = new EditorButton("Выйти и сохранить", true);
   private final EditorButton clearButton = new EditorButton("Очистить", false);
   private final EditorButton resetButton = new EditorButton("Сбросить", false);
   private final Animation visibility = new Animation();
   private boolean active;
   private boolean mirrorX = true;
   private boolean mirrorY = true;
   private int paintMode = -1;
   private float canvasX;
   private float canvasY;
   private float canvasSize;
   private float cell;
   private Rect mirrorXBounds = Rect.EMPTY;
   private Rect mirrorYBounds = Rect.EMPTY;

   private CrosshairEditor() {
   }

   public static CrosshairEditor getInstance() {
      return INSTANCE;
   }

   @Override
   public boolean isActive() {
      return this.active;
   }

   public void open() {
      if (!this.active) {
         this.active = true;
         this.paintMode = -1;
         this.panel.reset();
         this.panel.unplace();
         this.visibility.set(0.0);
         this.visibility.run(1.0, 0.24F, Easings.EXPO_OUT);
      }
   }

   @Override
   public void saveAndExit() {
      if (this.active) {
         if (Client.get() != null && Client.get().configManager() != null) {
            Client.get().configManager().autoSave();
         }

         this.deactivate();
      }
   }

   @Override
   public void closeFromMenuRemoval() {
      this.saveAndExit();
   }

   private void deactivate() {
      this.active = false;
      this.paintMode = -1;
      this.panel.releaseDrag();
      this.panel.closePickers();
   }

   @Override
   public void render(float width, float height, float mouseX, float mouseY, float parentAlpha) {
      if (this.active) {
         CrossHair module = CrossHair.getInstance();
         if (module != null && module.isDrawing()) {
            this.widgets.setMouse(mouseX, mouseY);
            this.layout(width, height);
            this.saveButton.update(mouseX, mouseY);
            this.clearButton.update(mouseX, mouseY);
            this.resetButton.update(mouseX, mouseY);
            this.panel.layout(module, "Cross Hair", width - 168.0F - 16.0F, 14.0F, width, height, this.widgets);
            this.visibility.update();
            float alpha = this.visibility.get() * parentAlpha;
            RenderUtil.Render2D.rect(0.0F, 0.0F, width, height, ColorUtil.getColor(0, 0.55F * alpha));
            Client.get().render2D().flushAll();
            Fonts.sf_regular.drawCentered(Lang.tr("Редактор прицела"), width / 2.0F, 16.0F, 8.0F, ColorUtil.getColor(240, alpha));
            Fonts.sf_regular
               .drawCentered(
                  Lang.tr("ЛКМ — рисовать") + "   •   " + Lang.tr("ПКМ — стирать") + "   •   " + Lang.tr("Esc — выйти"),
                  width / 2.0F,
                  29.0F,
                  6.0F,
                  ColorUtil.getColor(255, alpha * 0.45F)
               );
            this.drawCanvas(module.grid, alpha);
            this.widgets.drawToggleRow(this.mirrorXBounds, Lang.tr("Зеркало по X"), this.mirrorX, "crosshair:mirrorx", alpha);
            this.widgets.drawToggleRow(this.mirrorYBounds, Lang.tr("Зеркало по Y"), this.mirrorY, "crosshair:mirrory", alpha);
            this.widgets.drawButton(this.clearButton, alpha);
            this.widgets.drawButton(this.resetButton, alpha);
            this.widgets.drawButton(this.saveButton, alpha);
            this.panel.draw(this.widgets, alpha);
         } else {
            this.saveAndExit();
         }
      }
   }

   private void layout(float width, float height) {
      float areaW = Math.max(200.0F, width - 168.0F - 40.0F);
      this.canvasSize = MathUtil.clamp(Math.min(areaW * 0.75F, height - 150.0F), 110.0F, 340.0F);
      this.canvasX = areaW / 2.0F - this.canvasSize / 2.0F;
      this.canvasY = 50.0F;
      this.cell = this.canvasSize / 21.0F;
      float toggleY = this.canvasY + this.canvasSize + 8.0F;
      float half = this.canvasSize / 2.0F - 3.0F;
      this.mirrorXBounds = new Rect(this.canvasX, toggleY, half, 13.0F);
      this.mirrorYBounds = new Rect(this.canvasX + this.canvasSize / 2.0F + 3.0F, toggleY, half, 13.0F);
      float buttonH = 18.0F;
      float saveW = 120.0F;
      float sideW = 88.0F;
      float bottomY = height - 30.0F;
      this.saveButton.set(this.canvasX + this.canvasSize / 2.0F - saveW / 2.0F, bottomY, saveW, buttonH);
      this.clearButton.set(this.canvasX + this.canvasSize / 2.0F - sideW - 4.0F, bottomY - 24.0F, sideW, buttonH);
      this.resetButton.set(this.canvasX + this.canvasSize / 2.0F + 4.0F, bottomY - 24.0F, sideW, buttonH);
   }

   private void drawCanvas(PixelGridSetting grid, float alpha) {
      RenderUtil.Render2D.glow(this.canvasX, this.canvasY, this.canvasSize, this.canvasSize, ColorUtil.getColor(0, 0.2F * alpha), 4.0F, 12.0F, 1.0F);
      RenderUtil.Render2D.rect(this.canvasX, this.canvasY, this.canvasSize, this.canvasSize, ColorUtil.getColor(18, 0.85F * alpha), 4.0F);
      int c = grid.center();
      int fill = ColorUtil.replAlpha(ColorUtil.client(), alpha);
      int gridLine = ColorUtil.getColor(255, 0.07F * alpha);
      int axisLine = ColorUtil.getColor(255, 0.18F * alpha);

      for (int i = 1; i < 21; i++) {
         float offset = (float)i * this.cell;
         boolean axis = i == c || i == c + 1;
         int line = axis ? axisLine : gridLine;
         RenderUtil.Render2D.rect(this.canvasX + offset, this.canvasY, 0.5F, this.canvasSize, line);
         RenderUtil.Render2D.rect(this.canvasX, this.canvasY + offset, this.canvasSize, 0.5F, line);
      }

      for (int gy = 0; gy < 21; gy++) {
         for (int gx = 0; gx < 21; gx++) {
            if (grid.get(gx, gy)) {
               int pixelFill = CrossHair.getInstance() != null
                  ? CrossHair.getInstance().resolvePixelColor(gx, gy, alpha)
                  : ColorUtil.replAlpha(ColorUtil.client(), alpha);
               RenderUtil.Render2D.rect(
                  this.canvasX + (float)gx * this.cell + 0.5F, this.canvasY + (float)gy * this.cell + 0.5F, this.cell - 1.0F, this.cell - 1.0F, pixelFill, 1.0F
               );
            }
         }
      }

      int hx = this.cellAt(this.widgets.mouseX(), this.canvasX);
      int hy;
      if (grid.inBounds(hx, hy = this.cellAt(this.widgets.mouseY(), this.canvasY))) {
         int hover = CrossHair.getInstance() != null
            ? CrossHair.getInstance().resolvePixelColor(hx, hy, alpha * 0.4F)
            : ColorUtil.replAlpha(ColorUtil.client(), alpha * 0.35F);

         for (int[] point : this.mirrored(hx, hy)) {
            RenderUtil.Render2D.rect(
               this.canvasX + (float)point[0] * this.cell + 0.5F,
               this.canvasY + (float)point[1] * this.cell + 0.5F,
               this.cell - 1.0F,
               this.cell - 1.0F,
               hover,
               1.0F
            );
         }
      }

      RenderUtil.Render2D.outline(
         this.canvasX, this.canvasY, this.canvasSize, this.canvasSize, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * 0.35F), 4.0F
      );
   }

   private int cellAt(float mouse, float origin) {
      return this.cell <= 0.0F ? -1 : (int)Math.floor((double)((mouse - origin) / this.cell));
   }

   private boolean overCanvas(float mouseX, float mouseY) {
      return mouseX >= this.canvasX && mouseY >= this.canvasY && mouseX < this.canvasX + this.canvasSize && mouseY < this.canvasY + this.canvasSize;
   }

   private int[][] mirrored(int gx, int gy) {
      int last = 20;
      if (this.mirrorX && this.mirrorY) {
         return new int[][]{{gx, gy}, {last - gx, gy}, {gx, last - gy}, {last - gx, last - gy}};
      } else if (this.mirrorX) {
         return new int[][]{{gx, gy}, {last - gx, gy}};
      } else {
         return this.mirrorY ? new int[][]{{gx, gy}, {gx, last - gy}} : new int[][]{{gx, gy}};
      }
   }

   private void paint(float mouseX, float mouseY) {
      if (this.paintMode >= 0 && this.overCanvas(mouseX, mouseY)) {
         CrossHair module = CrossHair.getInstance();
         if (module != null) {
            int gx = this.cellAt(mouseX, this.canvasX);
            int gy;
            if (module.grid.inBounds(gx, gy = this.cellAt(mouseY, this.canvasY))) {
               for (int[] point : this.mirrored(gx, gy)) {
                  module.grid.put(point[0], point[1], this.paintMode == 1);
               }
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (!this.active) {
         return false;
      } else if ((button == 0 || button == 1) && this.panel.handleClick(mouseX, mouseY, button)) {
         return true;
      } else if (this.overCanvas(mouseX, mouseY)) {
         this.paintMode = button == 1 ? 0 : 1;
         this.paint(mouseX, mouseY);
         return true;
      } else if (button != 0) {
         return true;
      } else {
         CrossHair module = CrossHair.getInstance();
         if (this.mirrorXBounds.contains(mouseX, mouseY)) {
            this.mirrorX = !this.mirrorX;
         } else if (this.mirrorYBounds.contains(mouseX, mouseY)) {
            this.mirrorY = !this.mirrorY;
         } else if (this.saveButton.contains(mouseX, mouseY)) {
            this.saveButton.press();
            this.saveAndExit();
         } else if (module != null && this.clearButton.contains(mouseX, mouseY)) {
            this.clearButton.press();
            module.grid.clear();
         } else if (module != null && this.resetButton.contains(mouseX, mouseY)) {
            this.resetButton.press();
            module.grid.reset();
         }

         return true;
      }
   }

   @Override
   public boolean mouseReleased(int button) {
      if (!this.active) {
         return false;
      } else {
         this.paintMode = -1;
         if (button == 0) {
            this.panel.releaseDrag();
         }

         return true;
      }
   }

   @Override
   public boolean mouseScrolled(float mouseX, float mouseY, double verticalAmount) {
      if (!this.active) {
         return false;
      } else {
         this.panel.handleScroll(mouseX, mouseY, verticalAmount);
         return true;
      }
   }

   @Override
   public void rawMouseButton(float mouseX, float mouseY, int button, boolean pressed) {
      if (this.active) {
         if (!pressed) {
            this.paintMode = -1;
            if (button == 0) {
               this.panel.releaseDrag();
            }
         } else {
            if (this.overCanvas(mouseX, mouseY)) {
               this.paintMode = button == 1 ? 0 : 1;
               this.paint(mouseX, mouseY);
            }
         }
      }
   }

   @Override
   public void rawMouseMoved(float mouseX, float mouseY) {
      if (this.active) {
         if (!this.panel.rawMouseMoved(mouseX, mouseY)) {
            this.paint(mouseX, mouseY);
         }
      }
   }
}
