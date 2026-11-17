package ru.mentions.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_408;
import ru.mentions.Client;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class ArrayListHud implements element {
   private static float S = 1.0F;
   private final HudModuleOrder moduleOrder = new HudModuleOrder();
   private final List<ArrayListHud.Row> sourceRows = new ArrayList<>();
   private final List<ArrayListHud.Row> activeRows = new ArrayList<>();

   private void updateRows(float textSize) {
      boolean changed = false;
      int index = 0;

      for (Module module : this.moduleOrder.get(Client.get().moduleManager())) {
         if (module.getAnimation().getOutput() > 0.0F && module.getCategory() != Category.RENDER) {
            ArrayListHud.Row row;
            if (index == this.sourceRows.size()) {
               row = new ArrayListHud.Row(module);
               this.sourceRows.add(row);
               changed = true;
            } else {
               row = this.sourceRows.get(index);
               if (row.module != module) {
                  row = new ArrayListHud.Row(module);
                  this.sourceRows.set(index, row);
                  changed = true;
               }
            }

            String name = module.getBigName();
            float width = Fonts.sf_regular.getWidth(name, textSize);
            changed |= (int)row.width != (int)width;
            row.name = name;
            row.width = width;
            index++;
         }
      }

      if (index < this.sourceRows.size()) {
         this.sourceRows.subList(index, this.sourceRows.size()).clear();
         changed = true;
      }

      if (changed) {
         this.activeRows.clear();
         this.activeRows.addAll(this.sourceRows);
         this.activeRows.sort(Comparator.<ArrayListHud.Row>comparingInt(rowx -> (int)rowx.width).reversed());
      }
   }

   private static float getAlpha() {
      float opacity = ThemeColor.getOpacity();
      return opacity < 0.98F ? opacity : 0.98F;
   }

   @Override
   public void onRender(DragSetting dragSetting, InterFace interFace) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         S = InterFace.getInstance().sizeHud.getValue();
         float TEXT_SIZE = 7.0F * S;
         float ROW_H = 12.0F * S;
         float BAR_W = Math.max(1.0F, 1.0F * S);
         float ROW_RADIUS = 5.0F * S;
         this.updateRows(TEXT_SIZE);
         boolean chatOpen = mc.field_1755 instanceof class_408;
         if (this.activeRows.isEmpty() && !chatOpen) {
            dragSetting.active = false;
         } else {
            dragSetting.active = true;
            float maxTextW = 0.0F;

            for (ArrayListHud.Row row : this.activeRows) {
               maxTextW = Math.max(maxTextW, row.width);
            }

            float listW = maxTextW + 11.0F * S;
            float dragX = dragSetting.position.x;
            float dragY = dragSetting.position.y;
            float scaleFix = (float)mc.method_22683().method_4495();
            int screenWidth = (int)((float)mc.method_22683().method_4486() / scaleFix);
            boolean rightSide = dragX + listW / 2.0F > (float)screenWidth / 2.0F;
            float yOffset = dragY;

            for (ArrayListHud.Row row : this.activeRows) {
               Module module3 = row.module;
               module3.animation15.setDirection(module3.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
               String moduleName = row.name;
               float textWidth = row.width;
               float mAnim = module3.animation15.getOutput();
               if (!(mAnim <= 0.0F)) {
                  float xPosition = rightSide ? dragX + listW - 5.0F * S - (textWidth + 4.0F * S) - 2.0F * S : dragX + 7.0F * S + 2.0F * S;
                  float rowW = textWidth + 8.0F * S;
                  RenderUtil.Blur.blur(
                     xPosition - 2.0F * S,
                     yOffset - 1.0F * S,
                     rowW,
                     ROW_H,
                     mAnim,
                     ROW_RADIUS,
                     ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue() * mAnim)
                  );
                  Fonts.sf_regular.draw(moduleName, xPosition + 1.5F * S, yOffset + 0.5F * S, TEXT_SIZE, ThemeColor.getTextColor(mAnim));
                  yOffset += (ROW_H + 0.5F * S) * mAnim;
               }
            }

            float listH = Math.max(yOffset - dragY, ROW_H);
            if (!this.activeRows.isEmpty()) {
               float barX = rightSide ? dragX + listW - 5.0F * S : dragX + 4.0F * S;
               RenderUtil.Blur.blur(
                  barX - 2.0F * S,
                  dragY - 1.0F * S - 2.0F * S,
                  BAR_W + 4.0F * S,
                  listH + 4.0F * S,
                  1.0F,
                  2.0F * S,
                  ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue())
               );
               RenderUtil.Render2D.rect(barX, dragY - 1.0F * S, BAR_W, listH, ThemeColor.getHudColor(), 3.0F * S);
            }

            dragSetting.size.set(listW, listH);
         }
      }
   }

   private static final class Row {
      final Module module;
      String name;
      float width;

      Row(Module module) {
         this.module = module;
      }
   }
}
