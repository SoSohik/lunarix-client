package ru.mentions.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;
import ru.mentions.Client;
import ru.mentions.inventorypreset.InventoryPreset;
import ru.mentions.inventorypreset.InventoryPresetManager;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public final class InventoryPresetScreen extends class_437 implements IMinecraft {
   private static final float PANEL_W = 400.0F;
   private static final float PANEL_H = 250.0F;
   private static final float LEFT_W = 160.0F;
   private static final float ROW_H = 18.0F;
   private static final float ROW_GAP = 3.0F;
   private static final float CELL = 19.0F;
   private static final float TEXT = 6.5F;
   private static final float SMALL = 6.0F;
   private final class_437 parent;
   private final List<InventoryPresetScreen.Row> rows = new ArrayList<>();
   private final Map<String, Animation> anims = new HashMap<>();
   private final ru.mentions.utils.animation.Animation openAnim = new ru.mentions.utils.animation.Animation();
   private String name = "";
   private boolean focused;
   private float scroll;
   private float scrollTarget;
   private float maxScroll;
   private float scaleFix = 1.0F;
   private float panelX;
   private float panelY;
   private float mouseX;
   private float mouseY;
   private InventoryPreset preview;

   public InventoryPresetScreen(class_437 parent) {
      super(class_2561.method_43470("Менеджер пресетов"));
      this.parent = parent;
   }

   private InventoryPresetManager manager() {
      return Client.get().inventoryPresetManager();
   }

   protected void method_25426() {
      this.openAnim.set(0.0);
      this.openAnim.run(1.0, 0.25, Easings.SINE_OUT);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int rawMouseX, int rawMouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         this.scaleFix = targetScale / currentScale;
         float screenW = (float)mc.method_22683().method_4486() / this.scaleFix;
         float screenH = (float)mc.method_22683().method_4502() / this.scaleFix;
         this.mouseX = (float)rawMouseX / this.scaleFix;
         this.mouseY = (float)rawMouseY / this.scaleFix;
         this.openAnim.update();
         float alpha = this.openAnim.get();
         this.scroll = this.scroll + (this.scrollTarget - this.scroll) * 0.2F;
         this.panelX = screenW / 2.0F - 200.0F;
         this.panelY = screenH / 2.0F - 125.0F + (1.0F - alpha) * 30.0F;
         Render2D.beginOverlay();
         RenderUtil.Render2D.rect(0.0F, 0.0F, screenW, screenH, ColorUtil.getColor(0, 0.6F * alpha));
         RenderUtil.Render2D.glow(this.panelX, this.panelY, 400.0F, 249.5F, ColorUtil.getColor(0, 0.15F * alpha), 8.0F, 15.0F, 1.0F);
         RenderUtil.Blur.blur(
            this.panelX, this.panelY, 400.0F, 250.0F, alpha, 8.0F, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.6F), alpha)
         );
         RenderUtil.Render2D.outline(this.panelX, this.panelY, 400.0F, 250.0F, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * 0.35F), 8.0F);
         Font font = Fonts.sf_regular;
         RenderUtil.Render2D.rect(this.panelX + 160.0F, this.panelY + 8.0F, 0.5F, 234.0F, ColorUtil.getColor(255, 0.05F * alpha), 1.0F);
         Fonts.icon.draw("P", this.panelX + 10.0F, this.panelY + 12.5F, 7.0F, ColorUtil.multAlpha(ColorUtil.client(), alpha));
         font.draw("Пресеты инвентаря", this.panelX + 22.0F, this.panelY + 11.5F, 7.0F, ColorUtil.getColor(200, alpha));
         font.draw("Предпросмотр", this.panelX + 160.0F + 12.0F, this.panelY + 11.5F, 7.0F, ColorUtil.getColor(200, alpha));
         float inputX = this.panelX + 10.0F;
         float inputW = 140.0F;
         float inputY = this.panelY + 28.0F;
         this.drawInput(font, inputX, inputY, inputW, alpha);
         float saveY = inputY + 16.0F + 3.0F;
         boolean saveHover = this.drawButton(font, "Сохранить текущий", inputX, saveY, inputW, alpha, true);
         this.rows.clear();
         List<InventoryPreset> presets = this.manager().presets();
         float listY = saveY + 16.0F + 6.0F;
         float listH = this.panelY + 250.0F - 32.0F - listY;
         this.maxScroll = Math.max(0.0F, (float)presets.size() * 21.0F - listH);
         this.scrollTarget = MathUtil.clamp(this.scrollTarget, 0.0F, this.maxScroll);
         Scissor.enable(inputX, listY, inputW, listH, 2.0F);
         InventoryPreset hoveredPreset = null;
         float rowY = listY - this.scroll;

         for (InventoryPreset preset : presets) {
            boolean active = preset.name().equalsIgnoreCase(this.manager().activeName());
            boolean hovered = inside(this.mouseX, this.mouseY, inputX, rowY, inputW, 18.0F);
            if (hovered) {
               hoveredPreset = preset;
            }

            float hover = this.hoverAnim("row:" + preset.name(), hovered);
            Animation activeAnim = this.anim("active:" + preset.name());
            activeAnim.setDirection(active ? Direction.FORWARDS : Direction.BACKWARDS);
            float sel = activeAnim.getOutput();
            RenderUtil.Render2D.rect(
               inputX,
               rowY,
               inputW,
               18.0F,
               ColorUtil.overCol(ColorUtil.getColor(40, 0.15F * alpha), ColorUtil.replAlpha(ColorUtil.client(), 0.25F * alpha), sel),
               5.0F
            );
            RenderUtil.Render2D.outline(inputX, rowY, inputW, 18.0F, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * Math.max(hover, sel)), 5.0F);
            font.drawFadingText(
               preset.name(), inputX + 6.0F, rowY + 5.5F, inputW - 26.0F, ColorUtil.getColor(255, alpha * (0.5F + 0.5F * Math.max(hover, sel))), 6.5F
            );
            boolean deleteHover = inside(this.mouseX, this.mouseY, inputX + inputW - 18.0F, rowY, 18.0F, 18.0F);
            Fonts.icon
               .drawCentered(
                  "Q", inputX + inputW - 9.0F, rowY + 7.0F, 4.0F, deleteHover ? ColorUtil.getColor(235, 70, 70, alpha) : ColorUtil.getColor(255, alpha * 0.35F)
               );
            this.rows.add(new InventoryPresetScreen.Row(preset, inputX, rowY, inputW, 18.0F));
            rowY += 21.0F;
         }

         Scissor.disable();
         boolean hasActive = this.manager().activePreset().isPresent();
         this.drawButton(font, hasActive ? "Отключить пресет" : "Пресет не выбран", inputX, this.panelY + 250.0F - 26.0F, inputW, alpha, false);
         InventoryPreset selected = hoveredPreset != null ? hoveredPreset : this.preview;
         if (selected == null) {
            selected = this.manager().activePreset().orElse(presets.isEmpty() ? null : presets.get(0));
         }

         this.renderPreview(context, font, selected, alpha, rawMouseX, rawMouseY);
         Render2D.endOverlay();
      }
   }

   private void drawInput(Font font, float x, float y, float w, float alpha) {
      float hover = this.hoverAnim("input", inside(this.mouseX, this.mouseY, x, y, w, 16.0F) || this.focused);
      Animation focusAnim = this.anim("input:focus");
      focusAnim.setDirection(this.focused ? Direction.FORWARDS : Direction.BACKWARDS);
      float focus = focusAnim.getOutput();
      RenderUtil.Render2D.rect(
         x, y, w, 16.0F, ColorUtil.overCol(ColorUtil.getColor(40, 0.15F * alpha), ColorUtil.getColor(25, 0.3F * alpha), Math.max(hover, focus)), 5.0F
      );
      RenderUtil.Render2D.outline(x, y, w, 16.0F, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * focus), 5.0F);
      boolean placeholder = this.name.isEmpty() && focus < 0.99F;
      if (placeholder) {
         font.draw("Название пресета", x + 6.0F, y + 4.5F + 4.0F * focus, 6.5F, ColorUtil.getColor(255, alpha * (1.0F - focus) * 0.35F));
      }

      if (!this.name.isEmpty()) {
         font.drawFadingTextReverse(this.name, x + 6.0F, y + 4.5F, w - 14.0F, ColorUtil.getColor(255, alpha * 0.85F), 6.5F);
      }

      if (focus > 0.01F && System.currentTimeMillis() / 450L % 2L == 0L) {
         RenderUtil.Render2D.rect(x + 6.5F + font.getWidth(this.name, 6.5F), y + 4.5F, 0.6F, 8.0F, ColorUtil.replAlpha(ColorUtil.client(), alpha * focus), 0.3F);
      }
   }

   private boolean drawButton(Font font, String label, float x, float y, float w, float alpha, boolean accent) {
      boolean hovered = inside(this.mouseX, this.mouseY, x, y, w, 16.0F);
      float hover = this.hoverAnim("btn:" + label, hovered);
      RenderUtil.Render2D.glow(x, y, w, 16.0F, ColorUtil.getColor(0, 0.06F * alpha), 5.0F, 7.0F, 1.0F);
      RenderUtil.Render2D.rect(
         x,
         y,
         w,
         16.0F,
         ColorUtil.overCol(ColorUtil.getColor(40, 0.15F * alpha), ColorUtil.replAlpha(ColorUtil.client(), (accent ? 0.3F : 0.2F) * alpha), hover),
         5.0F
      );
      RenderUtil.Render2D.outline(x, y, w, 16.0F, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * (accent ? 0.4F + 0.6F * hover : hover)), 5.0F);
      font.drawCentered(label, x + w / 2.0F, y + 4.5F, 6.5F, ColorUtil.getColor(255, alpha * (0.55F + 0.45F * hover)));
      return hovered;
   }

   private void renderPreview(class_332 context, Font font, InventoryPreset preset, float alpha, int rawMouseX, int rawMouseY) {
      float baseX = this.panelX + 160.0F + 12.0F;
      float baseY = this.panelY + 34.0F;
      if (preset == null) {
         font.draw("Сохранённых пресетов пока нет", baseX, baseY, 6.5F, ColorUtil.getColor(255, alpha * 0.35F));
      } else {
         font.draw(preset.name(), baseX, baseY, 6.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha));
         float gridX = baseX + 26.0F;
         float gridY = baseY + 24.0F;
         class_1799 hovered = class_1799.field_8037;
         font.draw("Броня", baseX, baseY + 14.0F, 6.0F, ColorUtil.getColor(255, alpha * 0.35F));

         for (int i = 0; i < 4; i++) {
            class_1799 stack = preset.slot(36 + i).toStack();
            if (this.drawCell(context, stack, baseX, gridY + (float)i * 19.0F, alpha)) {
               hovered = stack;
            }
         }

         font.draw("Оффхенд", baseX, gridY + 76.0F + 4.0F, 6.0F, ColorUtil.getColor(255, alpha * 0.35F));
         class_1799 offhand = preset.slot(40).toStack();
         if (this.drawCell(context, offhand, baseX, gridY + 76.0F + 15.0F, alpha)) {
            hovered = offhand;
         }

         font.draw("Инвентарь", gridX, baseY + 14.0F, 6.0F, ColorUtil.getColor(255, alpha * 0.35F));

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               class_1799 stack = preset.slot(9 + row * 9 + col).toStack();
               if (this.drawCell(context, stack, gridX + (float)col * 19.0F, gridY + (float)row * 19.0F, alpha)) {
                  hovered = stack;
               }
            }
         }

         font.draw("Хотбар", gridX, gridY + 57.0F + 4.0F, 6.0F, ColorUtil.getColor(255, alpha * 0.35F));

         for (int colx = 0; colx < 9; colx++) {
            class_1799 stack = preset.slot(colx).toStack();
            if (this.drawCell(context, stack, gridX + (float)colx * 19.0F, gridY + 57.0F + 15.0F, alpha)) {
               hovered = stack;
            }
         }

         if (!hovered.method_7960()) {
            Render2D.endOverlay();
            context.method_51446(this.field_22793, hovered, rawMouseX, rawMouseY);
            Render2D.beginOverlay();
         }
      }
   }

   private boolean drawCell(class_332 context, class_1799 stack, float x, float y, float alpha) {
      boolean hovered = inside(this.mouseX, this.mouseY, x, y, 17.0F, 17.0F);
      RenderUtil.Render2D.rect(x, y, 17.0F, 17.0F, ColorUtil.getColor(40, 0.15F * alpha), 4.0F);
      if (hovered) {
         RenderUtil.Render2D.outline(x, y, 17.0F, 17.0F, 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha), 4.0F);
      }

      if (!stack.method_7960()) {
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate((x + 8.5F) * this.scaleFix, (y + 8.5F) * this.scaleFix);
         matrices.scale(this.scaleFix, this.scaleFix);
         context.method_51427(stack, -8, -8);
         context.method_51431(this.field_22793, stack, -8, -8);
         matrices.popMatrix();
      }

      return hovered && !stack.method_7960();
   }

   private Animation anim(String key) {
      return this.anims.computeIfAbsent(key, k -> new EaseInOutQuad(300, 1.0));
   }

   private float hoverAnim(String key, boolean hovered) {
      Animation animation = this.anim(key + ":hover");
      animation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
      return animation.getOutput();
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (click.method_74245() != 0) {
         return super.method_25402(click, doubled);
      } else {
         float mx = (float)(click.comp_4798() / (double)this.scaleFix);
         float my = (float)(click.comp_4799() / (double)this.scaleFix);
         float inputX = this.panelX + 10.0F;
         float inputW = 140.0F;
         float inputY = this.panelY + 28.0F;
         this.focused = inside(mx, my, inputX, inputY, inputW, 16.0F);
         if (inside(mx, my, inputX, inputY + 16.0F + 3.0F, inputW, 16.0F)) {
            this.saveCurrent();
            return true;
         } else {
            for (InventoryPresetScreen.Row row : this.rows) {
               if (inside(mx, my, row.x + row.w - 18.0F, row.y, 18.0F, row.h)) {
                  this.manager().delete(row.preset);
                  if (this.preview == row.preset) {
                     this.preview = null;
                  }

                  return true;
               }

               if (inside(mx, my, row.x, row.y, row.w - 18.0F, row.h)) {
                  this.manager().activate(row.preset);
                  this.field_22787.method_1507(this.parent);
                  return true;
               }
            }

            if (inside(mx, my, inputX, this.panelY + 250.0F - 26.0F, inputW, 16.0F) && this.manager().activePreset().isPresent()) {
               this.manager().deactivate();
               return true;
            } else {
               return true;
            }
         }
      }
   }

   private void saveCurrent() {
      InventoryPreset saved = this.manager().saveCurrent(this.name);
      if (saved != null) {
         this.preview = saved;
         this.name = "";
         this.focused = false;
      }
   }

   public boolean method_25404(class_11908 input) {
      if (!this.focused) {
         if (input.comp_4795() == 256) {
            this.field_22787.method_1507(this.parent);
            return true;
         } else {
            return super.method_25404(input);
         }
      } else {
         if (input.comp_4795() == 257) {
            this.saveCurrent();
         } else if (input.comp_4795() == 259 && !this.name.isEmpty()) {
            this.name = this.name.substring(0, this.name.length() - 1);
         } else if (input.comp_4795() == 256) {
            this.focused = false;
         }

         return true;
      }
   }

   public boolean method_25400(class_11905 input) {
      if (this.focused && input.method_74227() && this.name.length() < 32) {
         this.name = this.name + input.method_74226();
         return true;
      } else {
         return super.method_25400(input);
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      this.scrollTarget = MathUtil.clamp((float)((double)this.scrollTarget - vertical * 24.0), 0.0F, this.maxScroll);
      return true;
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }

   private static boolean inside(float mouseX, float mouseY, float x, float y, float w, float h) {
      return MathUtil.isHovered(mouseX, mouseY, x, y, w, h);
   }

   @Environment(EnvType.CLIENT)
   private static record Row(InventoryPreset preset, float x, float y, float w, float h) {
   }
}
