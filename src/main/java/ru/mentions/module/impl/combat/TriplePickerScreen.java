package ru.mentions.module.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class TriplePickerScreen extends class_437 {
   private float scaleFix = 1.0F;
   private final AutoSwap module;
   private final int slotIndex;
   private final class_437 parent;
   private static final float CELL_SIZE = 20.0F;
   private static final float CELL_GAP = 4.0F;
   private static final float PAD = 5.0F;
   private static final float TITLE_H = 14.0F;
   private static final float OFFHAND_H = 18.0F;
   private static final float BTN_H = 14.0F;
   private static final float BTN_W = 44.0F;
   private static final int MAX_COLS = 7;
   private final List<class_1799> items = new ArrayList<>();

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public TriplePickerScreen(AutoSwap module, int slotIndex) {
      this(module, slotIndex, null);
   }

   public TriplePickerScreen(AutoSwap module, int slotIndex, class_437 parent) {
      super(class_2561.method_43470("Выбор предмета"));
      this.module = module;
      this.slotIndex = slotIndex;
      this.parent = parent;
   }

   private void refreshItems() {
      this.items.clear();

      for (int i = 0; i < 36; i++) {
         class_1799 s = IMinecraft.mc.field_1724.method_31548().method_5438(i);
         if (!s.method_7960()) {
            this.items.add(s);
         }
      }
   }

   private int cols() {
      return Math.min(7, Math.max(1, this.items.size()));
   }

   private int rows() {
      return this.items.isEmpty() ? 0 : (int)Math.ceil((double)this.items.size() / (double)this.cols());
   }

   private float gridW() {
      return (float)this.cols() * 20.0F + (float)(this.cols() - 1) * 4.0F;
   }

   private float gridH() {
      return (float)this.rows() * 20.0F + (float)Math.max(0, this.rows() - 1) * 4.0F;
   }

   private float panelW() {
      return Math.max(this.gridW(), 44.0F) + 10.0F;
   }

   private float panelH() {
      return 41.0F + (this.items.isEmpty() ? 20.0F : this.gridH()) + 6.0F + 14.0F + 5.0F;
   }

   private float panelX() {
      return ((float)this.field_22789 / this.scaleFix - this.panelW()) / 2.0F;
   }

   private float panelY() {
      return ((float)this.field_22790 / this.scaleFix - this.panelH()) / 2.0F;
   }

   private float gridX() {
      return this.panelX() + (this.panelW() - this.gridW()) / 2.0F;
   }

   private float gridY() {
      return this.panelY() + 5.0F + 14.0F + 4.0F + 18.0F;
   }

   private float cellX(int i) {
      return this.gridX() + (float)(i % this.cols()) * 24.0F;
   }

   private float cellY(int i) {
      return this.gridY() + (float)(i / this.cols()) * 24.0F;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.scaleFix = 2.0F / (float)IMinecraft.mc.method_22683().method_4495();
      mouseX = (int)((float)mouseX / this.scaleFix);
      mouseY = (int)((float)mouseY / this.scaleFix);
      this.refreshItems();
      float px = this.panelX();
      float py = this.panelY();
      float pw = this.panelW();
      float ph = this.panelH();
      RenderUtil.Blur.blur(px, py, pw, ph, 1.0F, 8.0F, 0);
      RenderUtil.Render2D.rect(px, py, pw, ph, ColorUtil.getColor(0, 0.7F), 8.0F);
      String title = "Слот " + (this.slotIndex + 1) + " — выберите предмет";
      float tw = Fonts.sf_regular.getWidth(title, 7.0F);
      Fonts.sf_regular.draw(title, px + (pw - tw) / 2.0F, py + 5.0F + 2.0F, 7.0F, ColorUtil.getColor(255, 1.0F));
      class_1799 offhand = IMinecraft.mc.field_1724 != null ? IMinecraft.mc.field_1724.method_6079() : null;
      boolean hasOffhand = offhand != null && !offhand.method_7960();
      float offY = py + 5.0F + 14.0F + 2.0F;
      float offX = px + 8.0F;
      RenderUtil.Render2D.rect(offX, offY, 14.0F, 14.0F, ColorUtil.getColor(255, 255, 255, 0.06F), 3.0F);
      RenderUtil.Render2D.outline(offX, offY, 14.0F, 14.0F, 0.6F, ColorUtil.getColor(255, 255, 255, 0.15F), 3.0F);
      if (hasOffhand) {
         ItemRender.drawItemCenteredWithContext(context, offhand, (offX + 7.0F) * this.scaleFix, (offY + 7.0F) * this.scaleFix, 0.75F, 1.0F);
      }

      String offLabel = "В руці: " + (hasOffhand ? offhand.method_7964().getString() : "порожньо");
      Fonts.sf_regular.draw(offLabel, offX + 18.0F, offY + 4.0F, 6.5F, ColorUtil.getColor(225, 232, 245, hasOffhand ? 0.95F : 0.55F));
      if (this.items.isEmpty()) {
         String empty = "Инвентарь пуст";
         float ew = Fonts.sf_regular.getWidth(empty, 7.0F);
         Fonts.sf_regular.draw(empty, px + (pw - ew) / 2.0F, this.gridY() + 4.0F, 7.0F, ColorUtil.getColor(255, 1.0F));
      } else {
         class_1799 selectedItem = this.module.tripleSlotItems[this.slotIndex];

         for (int idx = 0; idx < this.items.size(); idx++) {
            class_1799 stack = this.items.get(idx);
            float cx = this.cellX(idx);
            float cy = this.cellY(idx);
            boolean hover = (float)mouseX >= cx && (float)mouseX <= cx + 20.0F && (float)mouseY >= cy && (float)mouseY <= cy + 20.0F;
            boolean selected = AutoSwap.sameStackIdentity(stack, selectedItem);
            int bg = selected
               ? ColorUtil.getColor(255, 255, 255, 0.15F)
               : (hover ? ColorUtil.getColor(255, 255, 255, 0.12F) : ColorUtil.getColor(255, 255, 255, 0.05F));
            RenderUtil.Render2D.rect(cx, cy, 20.0F, 20.0F, bg, 4.0F);
            if (selected || hover) {
               RenderUtil.Render2D.outline(cx, cy, 20.0F, 20.0F, selected ? 0.6F : 0.5F, ColorUtil.getColor(255, 255, 255, selected ? 0.3F : 0.2F), 4.0F);
            }

            if (hasOffhand && this.isSameItem(stack, offhand)) {
               RenderUtil.Render2D.outline(cx, cy, 20.0F, 20.0F, 0.8F, ColorUtil.replAlpha(ColorUtil.client(), 0.9F), 4.0F);
            }

            ItemRender.drawItemCenteredWithContext(context, stack, (cx + 10.0F) * this.scaleFix, (cy + 10.0F) * this.scaleFix, 0.85F, 1.0F);
            if (hover) {
               String name = stack.method_7964().getString();
               float nw = Fonts.sf_bold.getWidth(name, 7.0F);
               Fonts.sf_bold.draw(name, px + (pw - nw) / 2.0F, this.gridY() + this.gridH() + 30.0F, 7.0F, ColorUtil.getColor(255, 1.0F));
            }
         }
      }

      float btnX = px + (pw - 44.0F) / 2.0F;
      float btnY = this.gridY() + (this.items.isEmpty() ? 20.0F : this.gridH()) + 6.0F;
      boolean btnHov = (float)mouseX >= btnX && (float)mouseX <= btnX + 44.0F && (float)mouseY >= btnY && (float)mouseY <= btnY + 14.0F;
      RenderUtil.Render2D.rect(btnX, btnY, 44.0F, 14.0F, ColorUtil.getColor(255, 255, 255, btnHov ? 0.12F : 0.06F), 4.0F);
      RenderUtil.Render2D.outline(btnX, btnY, 44.0F, 14.0F, 0.6F, ColorUtil.getColor(255, 255, 255, btnHov ? 0.3F : 0.1F), 4.0F);
      float clw = Fonts.sf_regular.getWidth("Сброс", 6.0F);
      Fonts.sf_regular.draw("Сброс", btnX + (44.0F - clw) / 2.0F, btnY + 3.5F, 6.0F, ColorUtil.getColor(255, btnHov ? 0.9F : 0.45F));
      super.method_25394(context, mouseX, mouseY, delta);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      double mouseX = click.comp_4798() / (double)this.scaleFix;
      double mouseY = click.comp_4799() / (double)this.scaleFix;
      if (click.method_74245() != 0) {
         return super.method_25402(click, doubled);
      } else {
         float px = this.panelX();
         float py = this.panelY();

         for (int idx = 0; idx < this.items.size(); idx++) {
            float cx = this.cellX(idx);
            float cy = this.cellY(idx);
            if (mouseX >= (double)cx && mouseX <= (double)(cx + 20.0F) && mouseY >= (double)cy && mouseY <= (double)(cy + 20.0F)) {
               this.module.tripleSlotItems[this.slotIndex] = this.items.get(idx).method_7972();
               this.method_25419();
               return true;
            }
         }

         float btnX = px + (this.panelW() - 44.0F) / 2.0F;
         float btnY = this.gridY() + (this.items.isEmpty() ? 20.0F : this.gridH()) + 6.0F;
         if (mouseX >= (double)btnX && mouseX <= (double)(btnX + 44.0F) && mouseY >= (double)btnY && mouseY <= (double)(btnY + 14.0F)) {
            this.module.tripleSlotItems[this.slotIndex] = null;
            this.method_25419();
            return true;
         } else {
            if (mouseX < (double)px || mouseX > (double)(px + this.panelW()) || mouseY < (double)py || mouseY > (double)(py + this.panelH())) {
               this.method_25419();
            }

            return super.method_25402(click, doubled);
         }
      }
   }

   private boolean isSameItem(class_1799 stack, class_1799 other) {
      return AutoSwap.sameStackIdentity(stack, other);
   }

   public void method_25419() {
      IMinecraft.mc.method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }
}
