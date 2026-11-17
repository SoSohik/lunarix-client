package ru.mentions.mixin;

import java.util.ArrayList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_476;
import net.minecraft.class_490;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.inventorypreset.InventoryPresetManager;
import ru.mentions.module.impl.player.LockSlot;
import ru.mentions.module.impl.utils.ItemScroller;
import ru.mentions.screen.InventoryPresetScreen;

@Environment(EnvType.CLIENT)
@Mixin({class_465.class})
public abstract class MixinHandledScreen<T extends class_1703> extends class_437 {
   @Shadow
   @Final
   protected T field_2797;
   @Shadow
   @Nullable
   protected class_1735 field_2787;
   private long time = 0L;
   @Unique
   private class_1735 mentions$lastHoveredSlot;
   @Unique
   private static final int PRESET_BUTTON_SIZE = 18;
   @Unique
   private class_4185 presetButton;

   @Shadow
   protected abstract void method_2383(class_1735 var1, int var2, int var3, class_1713 var4);

   protected MixinHandledScreen(class_2561 title) {
      super(title);
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("TAIL")}
   )
   private void initPresetButton(CallbackInfo ci) {
   }

   @Inject(
      method = {"method_25402"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void clickPresetUi(class_11909 click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof class_490 && this.field_22787 != null && click.method_74245() == 0 && Client.get() != null) {
         HandledScreenAccessor accessor = (HandledScreenAccessor)this;
         int guiX = accessor.getScreenX();
         int guiY = accessor.getScreenY();
         float scaleFix = 2.0F / (float)this.field_22787.method_22683().method_4495();
         float mx = (float)click.comp_4798() / scaleFix;
         float my = (float)click.comp_4799() / scaleFix;
         float btnX = (float)(guiX + 151) / scaleFix;
         float btnY = (float)(guiY - 20) / scaleFix;
         float btnW = 18.0F / scaleFix;
         float btnH = 18.0F / scaleFix;
         if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH) {
            this.field_22787.method_1507(new InventoryPresetScreen(this));
            cir.setReturnValue(true);
         } else {
            InventoryPresetManager manager = Client.get().inventoryPresetManager();
            if (manager != null && !manager.activePreset().isEmpty()) {
               Map<String, InventoryPresetManager.MissingItem> missing = manager.missingItems();
               ArrayList<InventoryPresetManager.MissingItem> missingItems = new ArrayList<>(missing.values());
               int visibleRows = Math.min(8, Math.max(1, missingItems.size()));
               int missingScroll = manager.missingScroll(missingItems.size(), visibleRows);
               float panelX = (float)(guiX + 182) / scaleFix;
               float panelY = (float)(guiY + 8) / scaleFix;
               float panelW = 118.0F;
               float rowH = 21.0F;
               float rowY = panelY + 25.0F;

               for (int index = missingScroll; index < Math.min(missingItems.size(), missingScroll + visibleRows); index++) {
                  InventoryPresetManager.MissingItem item = missingItems.get(index);
                  if (mx >= panelX + 4.0F && mx < panelX + panelW - 4.0F && my >= rowY && my < rowY + rowH - 2.0F) {
                     manager.searchAuction(item);
                     cir.setReturnValue(true);
                     return;
                  }

                  rowY += rowH;
               }

               float panelH = 31.0F + (float)visibleRows * rowH + 27.0F;
               float arrangeY = panelY + panelH - 23.0F;
               if (mx >= panelX + 5.0F && mx < panelX + panelW - 5.0F && my >= arrangeY && my < arrangeY + 18.0F) {
                  if (manager.isArranging()) {
                     manager.cancelArrange(true);
                  } else {
                     manager.startArrange();
                  }

                  cir.setReturnValue(true);
               }
            }
         }
      }
   }

   @Inject(
      method = {"method_25401"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void scrollPresetMissing(double mouseX, double mouseY, double horizontal, double vertical, CallbackInfoReturnable<Boolean> cir) {
      ItemScroller scroller = ItemScroller.getInstance();
      if (scroller != null && scroller.isEnabled()) {
         class_1735 targetSlot = this.field_2787;
         if (targetSlot == null && this.field_2797 != null) {
            HandledScreenAccessor accessor = (HandledScreenAccessor)this;
            int gx = accessor.getScreenX();
            int gy = accessor.getScreenY();

            for (class_1735 s : this.field_2797.field_7761) {
               if (mouseX >= (double)(gx + s.field_7873)
                  && mouseX < (double)(gx + s.field_7873 + 16)
                  && mouseY >= (double)(gy + s.field_7872)
                  && mouseY < (double)(gy + s.field_7872 + 16)) {
                  targetSlot = s;
                  break;
               }
            }
         }

         if (targetSlot != null && targetSlot.method_7681()) {
            this.method_2383(targetSlot, targetSlot.field_7874, 0, class_1713.field_7794);
            cir.setReturnValue(true);
            return;
         }
      }

      if (this instanceof class_490 && Client.get() != null && this.field_22787 != null) {
         InventoryPresetManager manager = Client.get().inventoryPresetManager();
         if (manager != null && !manager.activePreset().isEmpty()) {
            int size = manager.missingItems().size();
            if (size > 8) {
               HandledScreenAccessor accessor = (HandledScreenAccessor)this;
               float scaleFix = 2.0F / (float)this.field_22787.method_22683().method_4495();
               float mx = (float)mouseX / scaleFix;
               float my = (float)mouseY / scaleFix;
               float panelX = (float)(accessor.getScreenX() + 182) / scaleFix;
               float panelY = (float)(accessor.getScreenY() + 8) / scaleFix;
               if (!(mx < panelX) && !(mx >= panelX + 118.0F) && !(my < panelY) && !(my >= panelY + 226.0F)) {
                  manager.scrollMissing(vertical < 0.0 ? 1 : -1, size, 8);
                  cir.setReturnValue(true);
               }
            }
         }
      }
   }

   @Unique
   private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
      return mouseX >= (double)x && mouseX < (double)(x + w) && mouseY >= (double)y && mouseY < (double)(y + h);
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")}
   )
   private void onRender(CallbackInfo ci) {
      ItemScroller itemScroller = ItemScroller.getInstance();
      if (this.field_22787 != null && itemScroller != null && itemScroller.isEnabled()) {
         boolean isShiftDown = this.hasShiftDown();
         long window = this.field_22787.method_22683().method_4490();
         boolean leftDown = GLFW.glfwGetMouseButton(window, 0) == 1;
         if (!leftDown) {
            this.mentions$lastHoveredSlot = null;
         } else if (isShiftDown) {
            class_1735 slot = this.field_2787;
            if (slot == null && this.field_2797 != null) {
               HandledScreenAccessor accessor = (HandledScreenAccessor)this;
               int gx = accessor.getScreenX();
               int gy = accessor.getScreenY();
               double mx = class_310.method_1551().field_1729.method_1603()
                  * (double)class_310.method_1551().method_22683().method_4486()
                  / (double)class_310.method_1551().method_22683().method_4480();
               double my = class_310.method_1551().field_1729.method_1604()
                  * (double)class_310.method_1551().method_22683().method_4502()
                  / (double)class_310.method_1551().method_22683().method_4507();

               for (class_1735 s : this.field_2797.field_7761) {
                  if (mx >= (double)(gx + s.field_7873)
                     && mx < (double)(gx + s.field_7873 + 16)
                     && my >= (double)(gy + s.field_7872)
                     && my < (double)(gy + s.field_7872 + 16)) {
                     slot = s;
                     break;
                  }
               }
            }

            if (slot != null
               && slot.method_7681()
               && slot != this.mentions$lastHoveredSlot
               && (float)(System.currentTimeMillis() - this.time) >= itemScroller.delay.getValue()) {
               this.method_2383(slot, slot.field_7874, 0, class_1713.field_7794);
               this.mentions$lastHoveredSlot = slot;
               this.time = System.currentTimeMillis();
            }
         }
      }
   }

   @Unique
   private boolean hasShiftDown() {
      if (this.field_22787 == null) {
         return false;
      } else {
         long window = this.field_22787.method_22683().method_4490();
         return GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
      }
   }

   @Unique
   private boolean hasControlDown() {
      if (this.field_22787 == null) {
         return false;
      } else {
         long window = this.field_22787.method_22683().method_4490();
         return GLFW.glfwGetKey(window, 341) == 1 || GLFW.glfwGetKey(window, 345) == 1;
      }
   }

   @Inject(
      method = {"method_25404"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onKeyPressed(class_11908 input, CallbackInfoReturnable<Boolean> cir) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         LockSlot lockSlot = Client.get().moduleManager().get(LockSlot.class);
         if (this.field_22787.field_1690.field_1869.method_1417(input)
            && this.field_2787 != null
            && this.field_2787.method_7681()
            && lockSlot != null
            && lockSlot.isDropProtected(this.field_2787.method_7677(), this.toInventorySlot(this.field_2787.field_7874))) {
            cir.setReturnValue(true);
            cir.cancel();
         } else {
            ItemScroller itemScroller = ItemScroller.getInstance();
            if (itemScroller != null && itemScroller.isEnabled()) {
               if (this.field_22787.field_1690.field_1869.method_1417(input)
                  && this.hasControlDown()
                  && this.hasShiftDown()
                  && this.field_2787 != null
                  && this.field_2787.method_7681()) {
                  class_1799 hoveredStack = this.field_2787.method_7677();
                  class_1792 itemToDrop = hoveredStack.method_7909();

                  for (class_1735 slot : this.field_2797.field_7761) {
                     int inventorySlot = this.toInventorySlot(slot.field_7874);
                     if (slot.method_7681()
                        && slot.method_7677().method_7909() == itemToDrop
                        && (lockSlot == null || !lockSlot.isDropProtected(slot.method_7677(), inventorySlot))) {
                        this.method_2383(slot, slot.field_7874, 1, class_1713.field_7795);
                     }
                  }

                  cir.setReturnValue(true);
                  cir.cancel();
               }
            }
         }
      }
   }

   @Unique
   private int toInventorySlot(int screenSlot) {
      if (this instanceof class_490) {
         return InventoryPresetManager.screenToLogicalSlot(screenSlot);
      } else {
         if (this instanceof class_476 screen) {
            int containerSlots = ((class_1707)screen.method_17577()).method_17388() * 9;
            int relative = screenSlot - containerSlots;
            if (relative >= 0 && relative < 27) {
               return 9 + relative;
            }

            if (relative >= 27 && relative < 36) {
               return relative - 27;
            }
         }

         return -1;
      }
   }
}
