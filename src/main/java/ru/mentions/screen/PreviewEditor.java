package ru.mentions.screen;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_745;
import ru.mentions.Client;
import ru.mentions.lang.Lang;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.preview.ModulePreview;
import ru.mentions.module.api.preview.PreviewContext;
import ru.mentions.module.api.preview.PreviewSettings;
import ru.mentions.screen.editor.EditorButton;
import ru.mentions.screen.editor.EditorWidgets;
import ru.mentions.screen.editor.OverlayEditor;
import ru.mentions.screen.editor.Rect;
import ru.mentions.screen.editor.SettingsPanel;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class PreviewEditor implements OverlayEditor, IMinecraft {
   private static final PreviewEditor INSTANCE = new PreviewEditor();
   private static final UUID DUMMY_UUID = UUID.fromString("32d0a964-137a-2c03-7cc9-df6700000101");
   private static final String DUMMY_NAME = "LUNARIX_PREVIEW";
   private final EditorWidgets widgets = new EditorWidgets();
   private final SettingsPanel panel = new SettingsPanel();
   private final PreviewContext context = new PreviewContext();
   private final EditorButton saveButton = new EditorButton("Выйти и сохранить", true);
   private final EditorButton spawnButton = new EditorButton("Показать сейчас", false);
   private final Animation visibility = new Animation();
   private boolean active;
   private Module module;
   private ModulePreview preview;
   private boolean restoreEnabled;
   private class_745 dummy;
   private long nextSpawnAt;
   private boolean freeCamera;
   private Rect freeCameraBounds = Rect.EMPTY;
   private boolean draggingCamera;
   private float lastCameraMouseX;
   private float lastCameraMouseY;
   private class_243 frozenOrigin;
   private double frozenForwardX;
   private double frozenForwardZ;
   private float frozenFacingYaw;

   private PreviewEditor() {
   }

   public static PreviewEditor getInstance() {
      return INSTANCE;
   }

   @Override
   public boolean isActive() {
      return this.active;
   }

   public boolean isEditing(Module candidate) {
      return this.active && this.module == candidate;
   }

   public Module module() {
      return this.module;
   }

   public void open(Module target) {
      if (target instanceof ModulePreview modulePreview) {
         if (this.active) {
            this.deactivate();
         }

         this.active = true;
         this.module = target;
         this.preview = modulePreview;
         this.restoreEnabled = target.isEnabled();
         if (!target.isEnabled()) {
            target.setEnabled(true, false);
         }

         this.context.reset();
         this.panel.reset();
         this.panel.unplace();
         this.setFreeCamera(false);
         this.updateAnchor();
         if (modulePreview.previewNeedsDummy()) {
            this.spawnDummy();
         }

         this.context.setDummy(this.dummy);
         modulePreview.previewStart(this.context);
         this.nextSpawnAt = System.currentTimeMillis() + 250L;
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
      if (this.preview != null) {
         this.preview.previewStop();
      }

      this.setFreeCamera(false);
      this.removeDummy();
      if (this.module != null && this.module.isEnabled() != this.restoreEnabled) {
         this.module.setEnabled(this.restoreEnabled, false);
      }

      this.panel.releaseDrag();
      this.panel.closePickers();
      this.active = false;
      this.module = null;
      this.preview = null;
      this.context.reset();
   }

   private void spawnDummy() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.removeDummy();
         class_745 entity = new class_745(mc.field_1687, new GameProfile(DUMMY_UUID, "LUNARIX_PREVIEW"));
         class_243 anchor = this.context.anchor();
         entity.method_5808(anchor.field_1352, anchor.field_1351, anchor.field_1350, this.facingYaw(), 0.0F);
         entity.method_6122(class_1268.field_5808, new class_1799(class_1802.field_22022));
         entity.method_6122(class_1268.field_5810, new class_1799(class_1802.field_8288));
         entity.method_6033(20.0F);
         entity.method_6073(0.0F);
         mc.field_1687.method_53875(entity);
         this.dummy = entity;
         this.context.setDummy(entity);
      }
   }

   private void removeDummy() {
      if (this.dummy != null) {
         this.dummy.method_31472();
         this.dummy = null;
      }

      this.context.setDummy(null);
   }

   private float facingYaw() {
      if (this.frozenOrigin != null) {
         return this.frozenFacingYaw;
      } else {
         return mc.field_1724 == null ? 0.0F : class_3532.method_15393(mc.field_1724.method_36454() + 180.0F);
      }
   }

   private void updateAnchor() {
      if (this.module != null && this.preview != null && mc.field_1724 != null) {
         PreviewSettings settings = this.preview.previewSettings();
         float distance = settings.distance(4.0F);
         float height = settings.height(0.0F);
         class_243 origin = this.frozenOrigin != null ? this.frozenOrigin : mc.field_1724.method_73189();
         double forwardZ;
         double forwardX;
         if (this.frozenOrigin != null) {
            forwardX = this.frozenForwardX;
            forwardZ = this.frozenForwardZ;
         } else {
            double rad = Math.toRadians((double)mc.field_1724.method_36454());
            forwardX = -Math.sin(rad);
            forwardZ = Math.cos(rad);
         }

         this.context.setDistance(distance);
         this.context.setHeight(height);
         this.context
            .setAnchor(
               new class_243(
                  origin.field_1352 + forwardX * (double)distance, origin.field_1351 + (double)height, origin.field_1350 + forwardZ * (double)distance
               )
            );
      }
   }

   private void setFreeCamera(boolean enabled) {
      this.freeCamera = enabled;
      this.draggingCamera = false;
      if (enabled && mc.field_1724 != null) {
         double rad = Math.toRadians((double)mc.field_1724.method_36454());
         this.frozenOrigin = mc.field_1724.method_73189();
         this.frozenForwardX = -Math.sin(rad);
         this.frozenForwardZ = Math.cos(rad);
         this.frozenFacingYaw = class_3532.method_15393(mc.field_1724.method_36454() + 180.0F);
      } else {
         this.frozenOrigin = null;
      }
   }

   private void rotateCamera(float mouseX, float mouseY) {
      if (mc.field_1724 != null) {
         float dx = (mouseX - this.lastCameraMouseX) * 0.6F;
         float dy = (mouseY - this.lastCameraMouseY) * 0.6F;
         this.lastCameraMouseX = mouseX;
         this.lastCameraMouseY = mouseY;
         float yaw = mc.field_1724.method_36454() + dx;
         float pitch = class_3532.method_15363(mc.field_1724.method_36455() + dy, -90.0F, 90.0F);
         mc.field_1724.method_36456(yaw);
         mc.field_1724.method_36457(pitch);
         mc.field_1724.field_5982 = yaw;
         mc.field_1724.field_6004 = pitch;
         mc.field_1724.method_5847(yaw);
      }
   }

   private void updatePreview() {
      if (this.active && this.preview != null) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            this.updateAnchor();
            if (this.preview.previewNeedsDummy()) {
               if (this.dummy == null || this.dummy.method_31481()) {
                  this.spawnDummy();
               } else if (!this.preview.previewControlsDummy()) {
                  class_243 anchor = this.context.anchor();
                  this.dummy.method_5814(anchor.field_1352, anchor.field_1351, anchor.field_1350);
                  float yaw = this.facingYaw();
                  this.dummy.method_36456(yaw);
                  this.dummy.method_5636(yaw);
                  this.dummy.method_5847(yaw);
                  this.dummy.method_36457(0.0F);
               }
            }

            this.preview.previewTick(this.context);
            if (this.preview.previewSettings().repeats()) {
               long now = System.currentTimeMillis();
               if (now >= this.nextSpawnAt) {
                  this.preview.previewSpawn(this.context);
                  this.context.nextPhase();
                  this.nextSpawnAt = now + Math.max(200L, this.preview.previewSettings().intervalMs());
               }
            }
         }
      }
   }

   @Override
   public void render(float width, float height, float mouseX, float mouseY, float parentAlpha) {
      if (this.active && this.module != null) {
         this.widgets.setMouse(mouseX, mouseY);
         this.updatePreview();
         this.panel.layout(this.module, this.module.getBigName(), width - 168.0F - 16.0F, 14.0F, width, height, this.widgets);
         boolean repeats = this.preview.previewSettings().repeats();
         this.layoutButtons(width, height, repeats);
         this.saveButton.update(mouseX, mouseY);
         if (repeats) {
            this.spawnButton.update(mouseX, mouseY);
         }

         this.visibility.update();
         float alpha = this.visibility.get() * parentAlpha;
         this.drawBand(0.0F, 0.0F, width, 62.0F, alpha, true);
         this.drawBand(0.0F, height - 62.0F, width, 62.0F, alpha, false);
         Client.get().render2D().flushAll();
         Fonts.sf_regular.drawCentered(Lang.tr("Предпоказ") + " — " + this.module.getBigName(), width / 2.0F, 16.0F, 8.0F, ColorUtil.getColor(240, alpha));
         Fonts.sf_regular
            .drawCentered(
               Lang.tr(repeats ? "Эффект повторяется сам — крутите настройки справа" : "Эффект виден постоянно — крутите настройки справа"),
               width / 2.0F,
               29.0F,
               6.0F,
               ColorUtil.getColor(255, alpha * 0.45F)
            );
         Fonts.sf_regular
            .drawCentered(
               Lang.tr(this.freeCamera ? "Тащите по пустому месту, чтобы осмотреться" : "Настройки предпоказа — в самом низу панели")
                  + "   •   "
                  + Lang.tr("Esc — выйти"),
               width / 2.0F,
               39.0F,
               5.5F,
               ColorUtil.getColor(255, alpha * 0.3F)
            );
         this.widgets.drawToggleRow(this.freeCameraBounds, Lang.tr("Свободная камера"), this.freeCamera, "preview:freecamera", alpha);
         if (repeats) {
            this.widgets.drawButton(this.spawnButton, alpha);
         }

         this.widgets.drawButton(this.saveButton, alpha);
         this.panel.draw(this.widgets, alpha);
      }
   }

   private void drawBand(float x, float y, float width, float height, float alpha, boolean fadeDown) {
      int solid = ColorUtil.getColor(0, 0.55F * alpha);
      int clear = ColorUtil.getColor(0, 0.0F);
      int top = fadeDown ? solid : clear;
      int bottom = fadeDown ? clear : solid;
      RenderUtil.Render2D.gradientRect(x, y, width, height, new int[]{top, top, bottom, bottom}, 0.0F);
   }

   private void layoutButtons(float width, float height, boolean repeats) {
      float buttonH = 18.0F;
      float saveW = 120.0F;
      float spawnW = 118.0F;
      float bottomY = height - 30.0F;
      this.saveButton.set(width / 2.0F - saveW / 2.0F, bottomY, saveW, buttonH);
      this.spawnButton.set(width / 2.0F - spawnW / 2.0F, bottomY - 24.0F, spawnW, repeats ? buttonH : 0.0F);
      float rowY = bottomY - 24.0F - (repeats ? 22.0F : 0.0F);
      this.freeCameraBounds = new Rect(width / 2.0F - 80.0F, rowY, 160.0F, 13.0F);
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (!this.active) {
         return false;
      } else if ((button == 0 || button == 1) && this.panel.handleClick(mouseX, mouseY, button)) {
         return true;
      } else {
         if (button == 0) {
            if (this.freeCameraBounds.contains(mouseX, mouseY)) {
               this.setFreeCamera(!this.freeCamera);
            } else if (this.saveButton.contains(mouseX, mouseY)) {
               this.saveButton.press();
               this.saveAndExit();
            } else if (this.spawnButton.contains(mouseX, mouseY)) {
               this.spawnButton.press();
               this.spawnNow();
            }
         }

         return true;
      }
   }

   private boolean overControls(float mouseX, float mouseY) {
      return this.panel.bounds().contains(mouseX, mouseY)
         || this.freeCameraBounds.contains(mouseX, mouseY)
         || this.saveButton.contains(mouseX, mouseY)
         || this.spawnButton.contains(mouseX, mouseY);
   }

   private void spawnNow() {
      if (this.preview != null && mc.field_1724 != null) {
         if (this.preview.previewSettings().repeats()) {
            this.updateAnchor();
            this.preview.previewSpawn(this.context);
            this.context.nextPhase();
            this.nextSpawnAt = System.currentTimeMillis() + Math.max(200L, this.preview.previewSettings().intervalMs());
         }
      }
   }

   @Override
   public boolean mouseReleased(int button) {
      if (!this.active) {
         return false;
      } else {
         if (button == 0) {
            this.panel.releaseDrag();
            this.draggingCamera = false;
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
            if (button == 0) {
               this.panel.releaseDrag();
               this.draggingCamera = false;
            }
         } else {
            if (this.freeCamera && button == 0 && !this.overControls(mouseX, mouseY)) {
               this.draggingCamera = true;
               this.lastCameraMouseX = mouseX;
               this.lastCameraMouseY = mouseY;
            }
         }
      }
   }

   @Override
   public void rawMouseMoved(float mouseX, float mouseY) {
      if (this.active) {
         if (!this.panel.rawMouseMoved(mouseX, mouseY)) {
            if (this.draggingCamera) {
               this.rotateCamera(mouseX, mouseY);
            }
         }
      }
   }

   public Rect panelBounds() {
      return this.panel.bounds();
   }
}
