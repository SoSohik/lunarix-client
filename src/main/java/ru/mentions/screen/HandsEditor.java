package ru.mentions.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import ru.mentions.Client;
import ru.mentions.lang.Lang;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.render.GlassHands;
import ru.mentions.module.impl.render.Hands;
import ru.mentions.module.impl.render.SwingAnimation;
import ru.mentions.screen.editor.EditorButton;
import ru.mentions.screen.editor.EditorWidgets;
import ru.mentions.screen.editor.OverlayEditor;
import ru.mentions.screen.editor.Rect;
import ru.mentions.screen.editor.SettingsPanel;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.GlassHandsRenderer;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class HandsEditor implements OverlayEditor {
   private static final HandsEditor INSTANCE = new HandsEditor();
   private static final long BOUNDS_TTL_MS = 250L;
   private static final long AUTO_SWING_INTERVAL_MS = 500L;
   private static final class_1799 RIGHT_PREVIEW_STACK = new class_1799(class_1802.field_22022);
   private static final class_1799 LEFT_PREVIEW_STACK = new class_1799(class_1802.field_8288);
   private static final float[][] HAND_SAMPLE_POINTS = new float[][]{
      {-0.55F, -0.55F, -0.35F},
      {-0.55F, -0.55F, 0.35F},
      {-0.55F, 0.55F, -0.35F},
      {-0.55F, 0.55F, 0.35F},
      {0.55F, -0.55F, -0.35F},
      {0.55F, -0.55F, 0.35F},
      {0.55F, 0.55F, -0.35F},
      {0.55F, 0.55F, 0.35F}
   };
   private final HandsEditor.HandBounds leftBounds = new HandsEditor.HandBounds();
   private final HandsEditor.HandBounds rightBounds = new HandsEditor.HandBounds();
   private final Animation visibility = new Animation();
   private final Animation leftHover = new Animation();
   private final Animation rightHover = new Animation();
   private final EditorButton saveButton = new EditorButton("Выйти и сохранить", true);
   private final EditorButton resetAllButton = new EditorButton("Сбросить обе руки", false);
   private final EditorButton resetLeftButton = new EditorButton("Сбросить левую", false);
   private final EditorButton resetRightButton = new EditorButton("Сбросить правую", false);
   private boolean active;
   private class_1306 hoveredArm;
   private class_1306 draggingArm;
   private int draggingButton = -1;
   private boolean autoSwingPreview;
   private long nextAutoSwingAt;
   private float dragStartMouseX;
   private float dragStartMouseY;
   private float dragStartValueX;
   private float dragStartValueY;
   private float screenWidth;
   private float screenHeight;
   private Rect autoSwingBounds = Rect.EMPTY;
   private final EditorWidgets widgets = new EditorWidgets();
   private final SettingsPanel swingPanel = new SettingsPanel();
   private final SettingsPanel glassPanel = new SettingsPanel();

   private HandsEditor() {
   }

   public static HandsEditor getInstance() {
      return INSTANCE;
   }

   @Override
   public boolean isActive() {
      return this.active;
   }

   public class_1799 previewStackFor(class_1306 arm) {
      return arm == class_1306.field_6183 ? RIGHT_PREVIEW_STACK : LEFT_PREVIEW_STACK;
   }

   public void open() {
      if (!this.active) {
         this.active = true;
         this.hoveredArm = null;
         this.draggingArm = null;
         this.draggingButton = -1;
         this.autoSwingPreview = false;
         this.nextAutoSwingAt = 0L;
         this.swingPanel.reset();
         this.glassPanel.reset();
         this.closePickers();
         this.leftBounds.invalidate();
         this.rightBounds.invalidate();
         this.visibility.set(0.0);
         this.visibility.run(1.0, 0.24F, Easings.EXPO_OUT);
         Hands hands = Hands.get();
         if (hands != null) {
            hands.setEditorActive(true);
         }
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
      if (this.active) {
         if (Client.get() != null && Client.get().configManager() != null) {
            Client.get().configManager().autoSave();
         }

         this.deactivate();
      }
   }

   private void deactivate() {
      this.active = false;
      this.hoveredArm = null;
      this.draggingArm = null;
      this.draggingButton = -1;
      this.swingPanel.releaseDrag();
      this.glassPanel.releaseDrag();
      this.autoSwingPreview = false;
      this.nextAutoSwingAt = 0L;
      this.closePickers();
      Hands hands = Hands.get();
      if (hands != null) {
         hands.setEditorActive(false);
      }

      this.restoreGlassHandsRenderer();
   }

   private void closePickers() {
      GlassHands glass = GlassHands.getInstance();
      if (glass != null) {
         glass.tintColor.pickerOpen = false;
      }

      this.swingPanel.closePickers();
      this.glassPanel.closePickers();
   }

   @Override
   public void render(float width, float height, float mouseX, float mouseY, float parentAlpha) {
      if (this.active) {
         this.screenWidth = width;
         this.screenHeight = height;
         this.widgets.setMouse(mouseX, mouseY);
         this.layoutButtons(width, height);
         this.swingPanel.layout(SwingAnimation.get(), "Swing Animation", width - 168.0F - 16.0F, 14.0F, width, height, this.widgets);
         this.glassPanel.layout(GlassHands.getInstance(), "GlassHands", 16.0F, 14.0F, width, height, this.widgets);
         this.updateAutoSwingPreview();
         this.visibility.update();
         float alpha = this.visibility.get() * parentAlpha;
         this.hoveredArm = this.draggingArm != null ? this.draggingArm : this.findHoveredArm(mouseX, mouseY);
         this.updateHover(this.leftHover, this.hoveredArm == class_1306.field_6182);
         this.updateHover(this.rightHover, this.hoveredArm == class_1306.field_6183);
         this.saveButton.update(mouseX, mouseY);
         this.resetAllButton.update(mouseX, mouseY);
         this.resetLeftButton.update(mouseX, mouseY);
         this.resetRightButton.update(mouseX, mouseY);
         RenderUtil.Render2D.rect(0.0F, 0.0F, width, height, ColorUtil.getColor(0, 0.3F * alpha));
         Client.get().render2D().flushAll();
         GlassHandsRenderer.getInstance().restoreHandsAfterOverlay();
         this.drawHeader(width, alpha);
         this.widgets.drawButton(this.resetLeftButton, alpha);
         this.widgets.drawButton(this.resetRightButton, alpha);
         this.widgets.drawButton(this.resetAllButton, alpha);
         this.widgets.drawButton(this.saveButton, alpha);
         this.widgets.drawToggleRow(this.autoSwingBounds, Lang.tr("Авто-взмах правой руки"), this.autoSwingPreview, "editor:autoswing", alpha);
         this.swingPanel.draw(this.widgets, alpha);
         this.glassPanel.draw(this.widgets, alpha);
      }
   }

   private void drawHeader(float width, float alpha) {
      Client.get().render2D().flushAll();
      Fonts.sf_regular.drawCentered(Lang.tr("Редактор рук"), width / 2.0F, 16.0F, 8.0F, ColorUtil.getColor(240, alpha));
      Fonts.sf_regular.drawCentered(Lang.tr("Наведите курсор на руку"), width / 2.0F, 29.0F, 6.0F, ColorUtil.getColor(255, alpha * 0.45F));
      Fonts.sf_regular
         .drawCentered(
            Lang.tr("ЛКМ / ПКМ + движение — переместить") + "   •   " + Lang.tr("Колесо — изменить размер"),
            width / 2.0F,
            39.0F,
            5.5F,
            ColorUtil.getColor(255, alpha * 0.3F)
         );
   }

   private void layoutButtons(float width, float height) {
      float buttonH = 18.0F;
      float sideW = 100.0F;
      float saveW = 120.0F;
      float resetAllW = 112.0F;
      float bottomY = height - 30.0F;
      this.saveButton.set(width / 2.0F - saveW / 2.0F, bottomY, saveW, buttonH);
      this.resetAllButton.set(width / 2.0F - resetAllW / 2.0F, bottomY - 25.0F, resetAllW, buttonH);
      this.resetLeftButton.set(20.0F, bottomY, sideW, buttonH);
      this.resetRightButton.set(width - 20.0F - sideW, bottomY, sideW, buttonH);
      this.autoSwingBounds = new Rect(width / 2.0F - 80.0F, bottomY - 52.0F, 160.0F, 13.0F);
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (!this.active) {
         return false;
      } else if (button != 0 && button != 1 || !this.glassPanel.handleClick(mouseX, mouseY, button) && !this.swingPanel.handleClick(mouseX, mouseY, button)) {
         if (button == 0) {
            if (this.autoSwingBounds.contains(mouseX, mouseY)) {
               this.autoSwingPreview = !this.autoSwingPreview;
               this.nextAutoSwingAt = 0L;
               return true;
            } else {
               Hands hands = Hands.get();
               if (this.saveButton.contains(mouseX, mouseY)) {
                  this.saveButton.press();
                  this.saveAndExit();
               } else if (hands != null && this.resetAllButton.contains(mouseX, mouseY)) {
                  this.resetAllButton.press();
                  hands.resetAll();
               } else if (hands != null && this.resetLeftButton.contains(mouseX, mouseY)) {
                  this.resetLeftButton.press();
                  hands.resetLeft();
               } else if (hands != null && this.resetRightButton.contains(mouseX, mouseY)) {
                  this.resetRightButton.press();
                  hands.resetRight();
               } else {
                  class_1306 arm = this.findDragArm(mouseX, mouseY);
                  if (arm != null) {
                     this.beginDrag(arm, mouseX, mouseY, button);
                  }
               }

               return true;
            }
         } else if (button != 1) {
            return true;
         } else if (this.isOverControls(mouseX, mouseY)) {
            return true;
         } else {
            class_1306 arm = this.findDragArm(mouseX, mouseY);
            if (arm != null) {
               this.beginDrag(arm, mouseX, mouseY, button);
            }

            return true;
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean mouseDragged(float deltaX, float deltaY) {
      return this.active;
   }

   @Override
   public boolean mouseReleased(int button) {
      if (!this.active) {
         return false;
      } else {
         if (button == 0) {
            this.swingPanel.releaseDrag();
            this.glassPanel.releaseDrag();
         }

         if (button == this.draggingButton) {
            this.draggingArm = null;
            this.draggingButton = -1;
         }

         return true;
      }
   }

   @Override
   public boolean mouseScrolled(float mouseX, float mouseY, double verticalAmount) {
      if (!this.active) {
         return false;
      } else if (this.glassPanel.handleScroll(mouseX, mouseY, verticalAmount) || this.swingPanel.handleScroll(mouseX, mouseY, verticalAmount)) {
         return true;
      } else if (this.autoSwingBounds.contains(mouseX, mouseY)) {
         return true;
      } else {
         class_1306 arm = this.findHoveredArm(mouseX, mouseY);
         Hands hands = Hands.get();
         if (arm != null && hands != null && verticalAmount != 0.0) {
            SliderSetting scale = arm == class_1306.field_6183 ? hands.rScale : hands.lScale;
            float direction = verticalAmount > 0.0 ? 1.0F : -1.0F;
            float oldScale = scale.getValue();
            float newScale = this.clampAndRound(oldScale + direction * scale.increment, scale);
            if (Math.abs(newScale - oldScale) < 1.0E-4F) {
               return true;
            } else {
               Rect rendered = this.boundsFor(arm).freshRect();
               Rect anchor = rendered != null ? rendered : this.controlRect(arm);
               float ratio = newScale / oldScale;
               float shiftX = (1.0F - ratio) * (mouseX - anchor.centerX());
               float shiftY = (1.0F - ratio) * (mouseY - anchor.centerY());
               SliderSetting xSetting = arm == class_1306.field_6183 ? hands.rx : hands.lx;
               SliderSetting ySetting = arm == class_1306.field_6183 ? hands.ry : hands.ly;
               float pixelsPerUnit = this.dragPixelsPerUnit();
               xSetting.set(class_3532.method_15363(xSetting.getValue() + shiftX / pixelsPerUnit, xSetting.min, xSetting.max));
               ySetting.set(class_3532.method_15363(ySetting.getValue() - shiftY / pixelsPerUnit, ySetting.min, ySetting.max));
               scale.set(newScale);
               return true;
            }
         } else {
            return true;
         }
      }
   }

   @Override
   public void rawMouseButton(float mouseX, float mouseY, int button, boolean pressed) {
      if (this.active) {
         if (button == 0 || button == 1) {
            if (!pressed) {
               if (button == 0) {
                  this.swingPanel.releaseDrag();
                  this.glassPanel.releaseDrag();
               }

               if (button == this.draggingButton) {
                  this.draggingArm = null;
                  this.draggingButton = -1;
               }
            } else if (!this.isOverControls(mouseX, mouseY)) {
               class_1306 arm = this.findDragArm(mouseX, mouseY);
               if (arm != null) {
                  this.beginDrag(arm, mouseX, mouseY, button);
               }
            }
         }
      }
   }

   @Override
   public void rawMouseMoved(float mouseX, float mouseY) {
      if (!this.glassPanel.rawMouseMoved(mouseX, mouseY)) {
         if (!this.swingPanel.rawMouseMoved(mouseX, mouseY)) {
            this.applyDrag(mouseX, mouseY);
         }
      }
   }

   private void beginDrag(class_1306 arm, float mouseX, float mouseY, int button) {
      Hands hands = Hands.get();
      if (hands != null) {
         this.draggingArm = arm;
         this.draggingButton = button;
         this.dragStartMouseX = mouseX;
         this.dragStartMouseY = mouseY;
         this.dragStartValueX = arm == class_1306.field_6183 ? hands.rx.getValue() : hands.lx.getValue();
         this.dragStartValueY = arm == class_1306.field_6183 ? hands.ry.getValue() : hands.ly.getValue();
      }
   }

   private void applyDrag(float mouseX, float mouseY) {
      if (this.active && this.draggingArm != null) {
         Hands hands = Hands.get();
         if (hands != null) {
            float sensitivity = 1.0F / this.dragPixelsPerUnit();
            float valueX = this.dragStartValueX + (mouseX - this.dragStartMouseX) * sensitivity;
            float valueY = this.dragStartValueY - (mouseY - this.dragStartMouseY) * sensitivity;
            SliderSetting xSetting = this.draggingArm == class_1306.field_6183 ? hands.rx : hands.lx;
            SliderSetting ySetting = this.draggingArm == class_1306.field_6183 ? hands.ry : hands.ly;
            xSetting.set(class_3532.method_15363(valueX, xSetting.min, xSetting.max));
            ySetting.set(class_3532.method_15363(valueY, ySetting.min, ySetting.max));
         }
      }
   }

   private class_1306 findDragArm(float mouseX, float mouseY) {
      class_1306 arm = this.findHoveredArm(mouseX, mouseY);
      if (arm == null && mouseY >= this.screenHeight * 0.32F) {
         arm = mouseX < this.screenWidth / 2.0F ? class_1306.field_6182 : class_1306.field_6183;
      }

      return arm;
   }

   private float clampAndRound(float value, SliderSetting setting) {
      return class_3532.method_15363(MathUtil.round(value, setting.increment), setting.min, setting.max);
   }

   public boolean isAutoSwingPreview() {
      return this.active && this.autoSwingPreview;
   }

   private void updateAutoSwingPreview() {
      if (this.autoSwingPreview) {
         class_310 client = class_310.method_1551();
         if (client != null && client.field_1724 != null && !client.field_1724.field_6252) {
            long now = System.currentTimeMillis();
            if (now >= this.nextAutoSwingAt) {
               class_1268 rightHand = client.field_1724.method_6068() == class_1306.field_6183 ? class_1268.field_5808 : class_1268.field_5810;
               client.field_1724.method_23667(rightHand, false);
               this.nextAutoSwingAt = now + 500L;
            }
         }
      }
   }

   private class_1306 findHoveredArm(float mouseX, float mouseY) {
      if (!this.isOverControls(mouseX, mouseY) && !(this.screenWidth <= 0.0F) && !(this.screenHeight <= 0.0F)) {
         Rect left = this.controlRect(class_1306.field_6182);
         Rect right = this.controlRect(class_1306.field_6183);
         boolean overLeft = left.contains(mouseX, mouseY);
         boolean overRight = right.contains(mouseX, mouseY);
         if (overLeft && overRight) {
            float leftDistance = left.distanceSquaredToCenter(mouseX, mouseY);
            return leftDistance <= right.distanceSquaredToCenter(mouseX, mouseY) ? class_1306.field_6182 : class_1306.field_6183;
         } else if (overLeft) {
            return class_1306.field_6182;
         } else if (overRight) {
            return class_1306.field_6183;
         } else if (mouseY >= this.screenHeight * 0.32F) {
            return mouseX < this.screenWidth / 2.0F ? class_1306.field_6182 : class_1306.field_6183;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Rect controlRect(class_1306 arm) {
      Hands hands = Hands.get();
      float valueX = 0.0F;
      float valueY = 0.0F;
      float scale = 1.0F;
      if (hands != null) {
         valueX = arm == class_1306.field_6183 ? hands.rx.getValue() : hands.lx.getValue();
         valueY = arm == class_1306.field_6183 ? hands.ry.getValue() : hands.ly.getValue();
         scale = arm == class_1306.field_6183 ? hands.rScale.getValue() : hands.lScale.getValue();
      }

      float pixelsPerUnit = this.dragPixelsPerUnit();
      float baseX = this.screenWidth * (arm == class_1306.field_6182 ? 0.16F : 0.84F);
      float baseY = this.screenHeight * 0.78F;
      float baseSize = class_3532.method_15363(Math.min(this.screenWidth, this.screenHeight) * 0.38F, 150.0F, 240.0F);
      float size = class_3532.method_15363(baseSize * (0.88F + scale * 0.12F), 145.0F, 275.0F);
      float centerX = baseX + valueX * pixelsPerUnit;
      float centerY = baseY - valueY * pixelsPerUnit;
      centerX = class_3532.method_15363(centerX, size / 2.0F + 8.0F, this.screenWidth - size / 2.0F - 8.0F);
      centerY = class_3532.method_15363(centerY, size / 2.0F + 52.0F, this.screenHeight - size / 2.0F - 54.0F);
      return new Rect(centerX - size / 2.0F, centerY - size / 2.0F, size, size);
   }

   private float dragPixelsPerUnit() {
      return Math.max(1.0F, this.screenHeight / 1.05F);
   }

   private boolean isOverControls(float mouseX, float mouseY) {
      return this.saveButton.contains(mouseX, mouseY)
         || this.resetAllButton.contains(mouseX, mouseY)
         || this.resetLeftButton.contains(mouseX, mouseY)
         || this.resetRightButton.contains(mouseX, mouseY)
         || this.autoSwingBounds.contains(mouseX, mouseY)
         || this.swingPanel.bounds().contains(mouseX, mouseY)
         || this.glassPanel.bounds().contains(mouseX, mouseY);
   }

   private void updateHover(Animation animation, boolean hovered) {
      animation.update();
      animation.run(hovered ? 1.0 : 0.0, 0.18F, Easings.QUAD_OUT, true);
   }

   public void updateHandBounds(class_1306 arm, class_4587 matrices) {
      if (this.active && matrices != null) {
         class_310 client = class_310.method_1551();
         if (client != null && client.field_1773 != null && client.method_22683() != null) {
            Matrix4f position = new Matrix4f(matrices.method_23760().method_23761());
            Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
            Matrix4f projection = client.field_1773.method_22973(70.0F);
            float width = (float)client.method_22683().method_4489() / 2.0F;
            float height = (float)client.method_22683().method_4506() / 2.0F;
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            int projected = 0;
            Vector4f point = new Vector4f();

            for (float[] sample : HAND_SAMPLE_POINTS) {
               point.set(sample[0], sample[1], sample[2], 1.0F);
               point.mul(position);
               point.mul(modelView);
               point.mul(projection);
               if (!(point.w() <= 0.001F) && Float.isFinite(point.w())) {
                  float ndcX = point.x() / point.w();
                  float ndcY = point.y() / point.w();
                  if (Float.isFinite(ndcX) && Float.isFinite(ndcY)) {
                     float sx = (ndcX * 0.5F + 0.5F) * width;
                     float sy = (0.5F - ndcY * 0.5F) * height;
                     minX = Math.min(minX, sx);
                     minY = Math.min(minY, sy);
                     maxX = Math.max(maxX, sx);
                     maxY = Math.max(maxY, sy);
                     projected++;
                  }
               }
            }

            if (projected >= 2) {
               float centerX = (minX + maxX) / 2.0F;
               float centerY = (minY + maxY) / 2.0F;
               float boundsWidth = class_3532.method_15363(maxX - minX + 34.0F, 76.0F, 260.0F);
               float boundsHeight = class_3532.method_15363(maxY - minY + 42.0F, 90.0F, 280.0F);
               if (Float.isFinite(centerX) && Float.isFinite(centerY)) {
                  this.boundsFor(arm).set(centerX, centerY, boundsWidth, boundsHeight);
               }
            }
         }
      }
   }

   public void captureBeforeHands() {
      if (this.active) {
         GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
         renderer.setKeepHandsResult(true);
         GlassHands glassHands = GlassHands.getInstance();
         if (glassHands != null && glassHands.isEnabled()) {
            renderer.setEnabled(true);
            glassHands.applyRendererSettings();
         } else {
            this.configureEditorOutline(renderer);
         }

         renderer.captureSceneBeforeHands();
      }
   }

   public void captureAfterHands() {
      if (this.active) {
         GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
         renderer.captureSceneAfterHands();
         renderer.renderGlassEffect();
      }
   }

   private void configureEditorOutline(GlassHandsRenderer renderer) {
      renderer.setEnabled(true);
      renderer.setBlurEnabled(false);
      renderer.setSaturation(1.0F);
      renderer.setReflect(false);
      renderer.setTintColor(0);
      renderer.setTintIntensity(0.0F);
      renderer.setIceIntensity(0.0F);
      renderer.setSmokeAmount(0.0F);
      renderer.setOutlineEnabled(true);
      renderer.setOutlineGlowStrength(this.hoveredArm == null ? 12.0F : 20.0F);
      renderer.setOutlineWidth(1);
      renderer.setOutlineGlowMode(2);
      renderer.setOutlineFillColor(0);
      renderer.setOutlineColor(ColorUtil.getClientColor1(1));
      renderer.setShimmerEnabled(true);
      renderer.setShimmerWidth(0.045F);
      renderer.setShimmerPeriodSec(2.8F);
   }

   private void restoreGlassHandsRenderer() {
      GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
      renderer.setKeepHandsResult(false);
      GlassHands glassHands = GlassHands.getInstance();
      if (glassHands != null && glassHands.isEnabled()) {
         renderer.setEnabled(true);
         glassHands.applyRendererSettings();
      } else {
         renderer.setEnabled(false);
      }
   }

   private HandsEditor.HandBounds boundsFor(class_1306 arm) {
      return arm == class_1306.field_6183 ? this.rightBounds : this.leftBounds;
   }

   @Environment(EnvType.CLIENT)
   private static final class HandBounds {
      private float centerX;
      private float centerY;
      private float width;
      private float height;
      private long updatedAt;

      void set(float centerX, float centerY, float width, float height) {
         this.centerX = centerX;
         this.centerY = centerY;
         this.width = width;
         this.height = height;
         this.updatedAt = System.currentTimeMillis();
      }

      void invalidate() {
         this.updatedAt = 0L;
      }

      Rect freshRect() {
         boolean fresh = this.updatedAt > 0L && System.currentTimeMillis() - this.updatedAt <= 250L;
         return fresh ? new Rect(this.centerX - this.width / 2.0F, this.centerY - this.height / 2.0F, this.width, this.height) : null;
      }
   }
}
