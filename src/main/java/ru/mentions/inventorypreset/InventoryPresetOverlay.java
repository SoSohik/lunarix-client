package ru.mentions.inventorypreset;

import java.util.ArrayList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.joml.Matrix3x2fStack;
import ru.mentions.Client;
import ru.mentions.mixin.HandledScreenAccessor;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class InventoryPresetOverlay {
   private static final float PANEL_W = 118.0F;
   private static final float ROW_H = 21.0F;

   private InventoryPresetOverlay() {
   }

   public static void render(class_332 context, int mouseX, int mouseY) {
      class_310 mc = class_310.method_1551();
      if (Client.get() != null && Client.get().inventoryPresetManager() != null) {
         if (mc.field_1755 instanceof class_465 screen) {
            InventoryPresetManager manager = Client.get().inventoryPresetManager();
            HandledScreenAccessor accessor = (HandledScreenAccessor)screen;
            int guiX = accessor.getScreenX();
            int guiY = accessor.getScreenY();
            float scaleFix = 2.0F / (float)mc.method_22683().method_4495();
            Render2D.beginOverlay();
            float btnX = (float)(guiX + 151) / scaleFix;
            float btnY = (float)(guiY - 20) / scaleFix;
            float btnW = 18.0F / scaleFix;
            float btnH = 18.0F / scaleFix;
            float mx = (float)mouseX / scaleFix;
            float my = (float)mouseY / scaleFix;
            boolean btnHover = inside(mx, my, btnX, btnY, btnW, btnH);
            RenderUtil.Blur.blur(btnX, btnY, btnW, btnH, 1.0F, 4.0F, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.6F), 1.0F));
            RenderUtil.Render2D.rect(btnX, btnY, btnW, btnH, ColorUtil.getColor(30, btnHover ? 0.45F : 0.22F), 4.0F);
            RenderUtil.Render2D.outline(btnX, btnY, btnW, btnH, 0.5F, btnHover ? ColorUtil.client() : ColorUtil.replAlpha(ColorUtil.client(), 0.35F), 4.0F);
            drawItem(context, class_1802.field_8106.method_7854(), (float)(guiX + 160) / scaleFix, (float)(guiY - 11) / scaleFix, scaleFix);
            if (manager.activePreset().isEmpty()) {
               Render2D.endOverlay();
            } else {
               Map<String, InventoryPresetManager.MissingItem> missing = manager.missingItems();
               ArrayList<InventoryPresetManager.MissingItem> missingItems = new ArrayList<>(missing.values());
               int visibleRows = Math.min(8, Math.max(1, missingItems.size()));
               int missingScroll = manager.missingScroll(missingItems.size(), visibleRows);
               float panelX = (float)(guiX + 182) / scaleFix;
               float panelY = (float)(guiY + 8) / scaleFix;
               float panelH = 31.0F + (float)visibleRows * 21.0F + 27.0F;
               Font font = Fonts.sf_regular;
               RenderUtil.Render2D.glow(panelX, panelY, 118.0F, panelH - 0.5F, ColorUtil.getColor(0, 0.15F), 8.0F, 15.0F, 1.0F);
               RenderUtil.Blur.blur(panelX, panelY, 118.0F, panelH, 1.0F, 8.0F, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.6F), 1.0F));
               RenderUtil.Render2D.outline(panelX, panelY, 118.0F, panelH, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), 0.35F), 8.0F);
               Fonts.icon.draw("K", panelX + 8.0F, panelY + 11.0F, 6.5F, ColorUtil.client());
               font.draw("Не хватает", panelX + 19.0F, panelY + 10.0F, 6.5F, ColorUtil.getColor(200));
               if (missingItems.size() > visibleRows) {
                  String counter = missingScroll + 1 + "–" + Math.min(missingItems.size(), missingScroll + visibleRows) + " / " + missingItems.size();
                  font.draw(counter, panelX + 118.0F - 8.0F - font.getWidth(counter, 5.5F), panelY + 10.5F, 5.5F, ColorUtil.getColor(255, 0.35F));
               }

               float rowY = panelY + 25.0F;
               if (missing.isEmpty()) {
                  font.draw("Все предметы найдены", panelX + 8.0F, rowY + 6.0F, 6.0F, ColorUtil.getColor(110, 220, 140, 1.0F));
               } else {
                  for (int index = missingScroll; index < Math.min(missingItems.size(), missingScroll + visibleRows); index++) {
                     InventoryPresetManager.MissingItem item = missingItems.get(index);
                     boolean hovered = inside(mx, my, panelX + 4.0F, rowY, 110.0F, 19.0F);
                     RenderUtil.Render2D.rect(panelX + 4.0F, rowY, 110.0F, 19.0F, ColorUtil.getColor(40, hovered ? 0.3F : 0.15F), 5.0F);
                     if (hovered) {
                        RenderUtil.Render2D.outline(panelX + 4.0F, rowY, 110.0F, 19.0F, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), 1.0F), 5.0F);
                     }

                     class_1799 stack;
                     if (!(stack = item.stack()).method_7960()) {
                        drawItem(context, stack, panelX + 13.0F, rowY + 10.5F - 1.0F, scaleFix);
                     }

                     font.draw("x" + item.count(), panelX + 24.0F, rowY + 5.4F, 8.0F, ColorUtil.getColor(230));
                     rowY += 21.0F;
                  }
               }

               float arrangeY = panelY + panelH - 23.0F;
               boolean arrangeHover = inside(mx, my, panelX + 5.0F, arrangeY, 108.0F, 18.0F);
               boolean arranging = manager.isArranging();
               RenderUtil.Render2D.rect(
                  panelX + 5.0F,
                  arrangeY,
                  108.0F,
                  18.0F,
                  arranging ? ColorUtil.getColor(200, 60, 60, 0.35F) : ColorUtil.replAlpha(ColorUtil.client(), arrangeHover ? 0.35F : 0.2F),
                  5.0F
               );
               RenderUtil.Render2D.outline(
                  panelX + 5.0F,
                  arrangeY,
                  108.0F,
                  18.0F,
                  0.5F,
                  arranging ? ColorUtil.getColor(200, 60, 60, 1.0F) : ColorUtil.replAlpha(ColorUtil.client(), arrangeHover ? 1.0F : 0.4F),
                  5.0F
               );
               font.drawCentered(arranging ? "Остановить" : "Разложить всё", panelX + 59.0F, arrangeY + 5.5F, 6.5F, ColorUtil.getColor(255, 0.9F));
               Render2D.endOverlay();
            }
         }
      }
   }

   private static void drawItem(class_332 context, class_1799 stack, float centerX, float centerY, float scaleFix) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(centerX * scaleFix, centerY * scaleFix);
      matrices.scale(scaleFix, scaleFix);
      context.method_51427(stack, -8, -8);
      matrices.popMatrix();
   }

   private static boolean inside(float mouseX, float mouseY, float x, float y, float w, float h) {
      return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
   }
}
