package ru.mentions.screen.editor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;
import ru.mentions.lang.Lang;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.DelimiterSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.RussianUi;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class SettingsPanel {
   private Module module;
   private boolean embedded;
   private float embeddedWidth;
   private float embeddedHeight;
   private String title = "";
   private float x;
   private float y;
   private boolean placed;
   private Rect bounds = Rect.EMPTY;
   private Rect headerBounds = Rect.EMPTY;
   private Rect enableBounds = Rect.EMPTY;
   private final List<SettingsPanel.PanelRow> rows = new ArrayList<>();
   private float scroll;
   private float scrollTarget;
   private float maxScroll;
   private float bottomInset = 82.0F;
   private boolean showEnableRow = true;
   private boolean draggingSelf;
   private float dragOffsetX;
   private float dragOffsetY;
   private SliderSetting draggingSlider;
   private Rect draggingSliderBounds = Rect.EMPTY;
   private ColorSetting draggingColor;
   private int draggingColorBar;
   private Rect draggingColorBounds = Rect.EMPTY;

   public Module module() {
      return this.module;
   }

   public Rect bounds() {
      return this.bounds;
   }

   public void setBottomInset(float bottomInset) {
      this.bottomInset = bottomInset;
   }

   public void setShowEnableRow(boolean showEnableRow) {
      this.showEnableRow = showEnableRow;
   }

   public void reset() {
      this.scroll = 0.0F;
      this.scrollTarget = 0.0F;
      this.releaseDrag();
      this.closePickers();
   }

   public void unplace() {
      this.placed = false;
   }

   public boolean isDragging() {
      return this.draggingSelf || this.draggingSlider != null || this.draggingColor != null;
   }

   public void releaseDrag() {
      this.draggingSelf = false;
      this.draggingSlider = null;
      this.draggingColor = null;
   }

   public void closePickers() {
      for (SettingsPanel.PanelRow row : this.rows) {
         if (row.setting instanceof ColorSetting colorSetting) {
            colorSetting.pickerOpen = false;
         }
      }

      this.draggingColor = null;
   }

   public void layoutEmbedded(Module module, String title, Rect area, EditorWidgets widgets) {
      this.embedded = true;
      this.embeddedWidth = area.width();
      this.embeddedHeight = area.height();
      this.x = area.x();
      this.y = area.y();
      this.placed = true;
      this.layout(module, title, this.x, this.y, this.x + area.width(), this.y + area.height(), widgets);
   }

   public void layout(Module module, String title, float defaultX, float defaultY, float screenW, float screenH, EditorWidgets widgets) {
      this.scroll = this.scroll + (this.scrollTarget - this.scroll) * 0.2F;
      this.rows.clear();
      this.module = module;
      this.title = title;
      if (module == null) {
         this.bounds = Rect.EMPTY;
         this.enableBounds = Rect.EMPTY;
         this.headerBounds = Rect.EMPTY;
      } else {
         if (!this.placed) {
            this.x = defaultX;
            this.y = defaultY;
            this.placed = true;
         }

         if (!this.embedded) {
            this.x = MathUtil.clamp(this.x, 4.0F, Math.max(4.0F, screenW - 168.0F - 4.0F));
            this.y = MathUtil.clamp(this.y, 4.0F, Math.max(4.0F, screenH - 16.0F - 30.0F));
         }

         float rowX = this.x + 6.0F;
         float rowW = this.embedded ? this.embeddedWidth - 12.0F : 156.0F;
         float top = this.y + 5.0F + 16.0F + 3.0F;
         this.headerBounds = new Rect(rowX, this.y + 5.0F, rowW, 16.0F);
         float maxBottom = this.embedded ? this.y + this.embeddedHeight - 5.0F : screenH - this.bottomInset;
         float viewH = Math.max(60.0F, maxBottom - top);
         float rowY = top - this.scroll;
         if (this.showEnableRow) {
            this.enableBounds = new Rect(rowX, rowY, rowW, 13.0F);
            rowY += 16.0F;
         } else {
            this.enableBounds = Rect.EMPTY;
         }

         for (Setting<?> setting : module.getSettings()) {
            boolean supported = setting instanceof BooleanSetting
               || setting instanceof SliderSetting
               || setting instanceof ModeSetting
               || setting instanceof ColorSetting
               || setting instanceof ButtonSetting
               || setting instanceof DelimiterSetting;
            if (supported) {
               Animation appear = widgets.anim(module.getName() + ":" + setting.getName() + ":vis");
               appear.setDirection(setting.getVisible().get() ? Direction.FORWARDS : Direction.BACKWARDS);
               float vis = appear.getOutput();
               if (!(vis <= 0.01F)) {
                  SettingsPanel.PanelRow row = new SettingsPanel.PanelRow(setting, setting.getName());
                  row.vis = vis;
                  float height = 13.0F;
                  if (setting instanceof SliderSetting) {
                     height = 20.0F;
                  } else if (setting instanceof DelimiterSetting) {
                     height = 11.0F;
                  } else if (setting instanceof ModeSetting mode) {
                     height = 15.0F + this.chipsHeight(mode.values, rowW - 12.0F) + 2.0F;
                  } else if (setting instanceof ColorSetting) {
                     ColorSetting color = (ColorSetting)setting;
                     if (color.pickerOpen) {
                        height = 40.0F;
                     }
                  }

                  row.bounds = new Rect(rowX, rowY, rowW, height);
                  if (setting instanceof ModeSetting mode) {
                     this.layoutChips(row, mode, rowX + 6.0F, rowY + 13.0F + 2.0F, rowW - 12.0F);
                  }

                  this.rows.add(row);
                  rowY += (height + 3.0F) * vis;
               }
            }
         }

         float content = rowY + this.scroll - top;
         this.maxScroll = Math.max(0.0F, content - viewH);
         this.scrollTarget = MathUtil.clamp(this.scrollTarget, 0.0F, this.maxScroll);
         float panelH = 24.0F + Math.min(content, viewH) + 5.0F;
         this.bounds = new Rect(this.x, this.y, this.embedded ? this.embeddedWidth : 168.0F, this.embedded ? this.embeddedHeight : panelH);
      }
   }

   private void layoutChips(SettingsPanel.PanelRow row, ModeSetting mode, float chipX, float chipY, float width) {
      row.chips.clear();
      Font font = Fonts.sf_regular;
      float px = 0.0F;
      float py = 0.0F;

      for (String value : mode.values) {
         float tw = font.getWidth(RussianUi.text(value), 5.5F) + 7.0F;
         if (px + tw > width && px > 0.0F) {
            px = 0.0F;
            py += 10.5F;
         }

         row.chips.add(new Rect(chipX + px, chipY + py, tw, 9.0F));
         px += tw + 2.5F;
      }
   }

   private float chipsHeight(List<String> values, float width) {
      Font font = Fonts.sf_regular;
      float px = 0.0F;
      float py = 0.0F;

      for (String value : values) {
         float tw = font.getWidth(RussianUi.text(value), 5.5F) + 7.0F;
         if (px + tw > width && px > 0.0F) {
            px = 0.0F;
            py += 10.5F;
         }

         px += tw + 2.5F;
      }

      return py + 9.0F;
   }

   public void draw(EditorWidgets widgets, float alpha) {
      if (this.module != null && !(this.bounds.width() <= 0.0F)) {
         Rect b = this.bounds;
         RenderUtil.Render2D.glow(b.x(), b.y(), b.width(), b.height() - 0.5F, ColorUtil.getColor(0, 0.15F * alpha), 8.0F, 15.0F, 1.0F);
         RenderUtil.Blur.blur(b.x(), b.y(), b.width(), b.height(), alpha, 8.0F, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.6F), alpha));
         RenderUtil.Render2D.outline(b.x(), b.y(), b.width(), b.height(), 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * 0.35F), 8.0F);
         Rect header = this.headerBounds;
         widgets.drawRowBackground(header, widgets.rowHover(this.module.getName() + ":header", header), alpha);
         Fonts.category.draw("U", header.x() + 6.0F, header.y() + 5.2F, 6.5F, ColorUtil.multAlpha(ColorUtil.client(), alpha));
         Fonts.sf_regular.draw(this.title, header.x() + 17.0F, header.y() + 4.4F, 6.5F, ColorUtil.getColor(200, alpha));
         Scissor.enable(b.x(), b.y() + 5.0F + 16.0F + 3.0F, b.width(), b.height() - 24.0F - 5.0F, 2.0F);
         if (this.showEnableRow) {
            widgets.drawToggleRow(this.enableBounds, Lang.tr("Вкл/Выкл"), this.module.isEnabled(), this.module.getName() + ":enabled", alpha);
         }

         for (SettingsPanel.PanelRow row : this.rows) {
            this.drawRow(widgets, row, alpha);
         }

         Scissor.disable();
      }
   }

   private void drawRow(EditorWidgets widgets, SettingsPanel.PanelRow row, float parentAlpha) {
      String key = this.module.getName() + ":" + row.label;
      Setting<?> setting = row.setting;
      Rect b = row.bounds;
      float alpha = parentAlpha * row.vis;
      if (!(alpha <= 0.01F)) {
         if (setting instanceof DelimiterSetting) {
            Font font = Fonts.sf_regular;
            String label = setting.getDisplayName();
            float labelW = font.getWidth(label, 5.5F);
            font.draw(label, b.x() + 2.0F, b.y() + 4.0F, 5.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * 0.85F));
            RenderUtil.Render2D.rect(
               b.x() + labelW + 6.0F, b.y() + 5.75F, Math.max(0.0F, b.width() - labelW - 8.0F), 0.5F, ColorUtil.getColor(255, alpha * 0.12F), 0.25F
            );
         } else if (setting instanceof BooleanSetting booleanSetting) {
            widgets.drawToggleRow(b, setting.getDisplayName(), booleanSetting.getValue(), key, alpha);
         } else if (setting instanceof SliderSetting slider) {
            float hover = widgets.rowHover(key, b);
            widgets.drawRowBackground(b, hover, alpha);
            Font font = Fonts.sf_regular;
            font.draw(setting.getDisplayName(), b.x() + 6.0F, b.y() + 3.2F, 6.0F, ColorUtil.getColor(255, alpha * (0.5F + 0.5F * hover)));
            String value = String.valueOf((double)Math.round((double)slider.getValue().floatValue() * 100.0) / 100.0);
            font.draw(
               value,
               b.x() + b.width() - 6.0F - font.getWidth(value, 5.5F),
               b.y() + 3.5F,
               5.5F,
               ColorUtil.replAlpha(ColorUtil.client(), alpha * (0.6F + 0.4F * hover))
            );
            float track = b.width() - 12.0F;
            float percent = class_3532.method_15363((slider.getValue() - slider.min) / (slider.max - slider.min), 0.0F, 1.0F);
            float shown = widgets.smooth(key + ":pc", percent);
            RenderUtil.Render2D.rect(b.x() + 6.0F, b.y() + 13.0F, track, 2.5F, ColorUtil.getColor(0, alpha * 0.15F), 1.25F);
            RenderUtil.Render2D.rect(
               b.x() + 6.0F, b.y() + 13.0F, track * shown, 2.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * (0.5F + 0.5F * hover)), 1.25F
            );
            RenderUtil.Render2D.rect(
               b.x() + 6.0F + track * shown - 2.5F, b.y() + 11.75F, 5.0F, 5.0F, ColorUtil.getColor((int)(200.0F + 55.0F * hover), alpha), 5.0F
            );
            RenderUtil.Render2D.rect(
               b.x() + 6.0F + track * shown - 1.75F, b.y() + 12.5F, 3.5F, 3.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * (0.7F + 0.3F * hover)), 5.0F
            );
         } else if (setting instanceof ModeSetting mode) {
            float hover = widgets.rowHover(key, b);
            widgets.drawRowBackground(b, hover, alpha);
            Font font = Fonts.sf_regular;
            String current = mode.getDisplayValue();
            font.drawFadingText(
               setting.getDisplayName(),
               b.x() + 6.0F,
               b.y() + 3.2F,
               b.width() - font.getWidth(current, 5.5F) - 16.0F,
               ColorUtil.getColor(255, alpha * (0.5F + 0.5F * hover)),
               6.0F
            );
            font.draw(
               current,
               b.x() + b.width() - 6.0F - font.getWidth(current, 5.5F),
               b.y() + 3.5F,
               5.5F,
               ColorUtil.replAlpha(ColorUtil.client(), alpha * (0.6F + 0.4F * hover))
            );

            for (int i = 0; i < row.chips.size(); i++) {
               Rect chip = row.chips.get(i);
               String value = mode.values.get(i);
               Animation anim = widgets.anim(key + ":chip:" + value);
               anim.setDirection(value.equals(mode.getValue()) ? Direction.FORWARDS : Direction.BACKWARDS);
               float selected = anim.getOutput();
               RenderUtil.Render2D.rect(
                  chip.x(),
                  chip.y(),
                  chip.width(),
                  chip.height(),
                  ColorUtil.overCol(ColorUtil.getColor(0, 0.25F * alpha), ColorUtil.replAlpha(ColorUtil.client(), alpha * (0.5F + 0.5F * hover)), selected),
                  2.5F
               );
               font.drawCentered(
                  RussianUi.text(value),
                  chip.centerX(),
                  chip.y() + 1.4F,
                  5.5F,
                  ColorUtil.getColor(255, alpha * (0.35F + 0.65F * selected) * (0.7F + 0.3F * hover))
               );
            }
         } else if (!(setting instanceof ColorSetting color)) {
            if (setting instanceof ButtonSetting buttonSetting) {
               float hover = widgets.rowHover(key, b);
               buttonSetting.pressAnim.update();
               float press = buttonSetting.pressAnim.get();
               float accent = Math.max(hover, press);
               RenderUtil.Render2D.glow(b.x(), b.y(), b.width(), b.height(), ColorUtil.getColor(0, 0.06F * alpha), 5.0F, 7.0F, 1.0F);
               RenderUtil.Render2D.rect(
                  b.x(),
                  b.y(),
                  b.width(),
                  b.height(),
                  ColorUtil.overCol(ColorUtil.getColor(40, 0.15F * alpha), ColorUtil.replAlpha(ColorUtil.client(), 0.25F * alpha), accent),
                  5.0F
               );
               RenderUtil.Render2D.outline(b.x(), b.y(), b.width(), b.height(), 0.5F, ColorUtil.replAlpha(ColorUtil.client(), alpha * accent), 4.0F);
               Fonts.sf_regular.drawCentered(setting.getDisplayName(), b.centerX(), b.y() + 3.2F, 6.0F, ColorUtil.getColor(255, alpha * (0.5F + 0.5F * hover)));
            }
         } else {
            float hover = widgets.rowHover(key, b);
            widgets.drawRowBackground(b, hover, alpha);
            Font font = Fonts.sf_regular;
            font.drawFadingText(
               setting.getDisplayName(), b.x() + 6.0F, b.y() + 3.2F, b.width() - 30.0F, ColorUtil.getColor(255, alpha * (0.5F + 0.5F * hover)), 6.0F
            );
            int argb = color.getValue();
            int r = (int)widgets.smooth(key + ":r", (float)(argb >> 16 & 0xFF));
            int g = (int)widgets.smooth(key + ":g", (float)(argb >> 8 & 0xFF));
            int bl = (int)widgets.smooth(key + ":b", (float)(argb & 0xFF));
            float px = b.x() + b.width() - 6.0F - 7.0F;
            float py = b.y() + 3.0F;
            RenderUtil.Render2D.rect(px, py, 7.0F, 7.0F, ColorUtil.getColor(r, g, bl, alpha), 7.0F);
            RenderUtil.Render2D.outline(px - 0.75F, py - 0.75F, 8.5F, 8.5F, 0.25F, ColorUtil.getColor(r, g, bl, alpha * (0.4F + 0.6F * hover)), 7.0F);
            RenderUtil.Render2D.glow(px, py, 7.0F, 7.0F, ColorUtil.getColor(r, g, bl, 0.1F * alpha), 7.0F, 6.0F, 1.0F);
            if (color.pickerOpen) {
               float[] hsb = Color.RGBtoHSB(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, null);
               float hue = widgets.smooth(key + ":h", hsb[0]);
               float sat = widgets.smooth(key + ":s", hsb[1]);
               float bri = widgets.smooth(key + ":v", hsb[2]);
               float[] smoothHsb = new float[]{hue, sat, bri};
               float barX = b.x() + 6.0F;
               float barW = b.width() - 12.0F;
               int segments = 16;
               float segment = barW / (float)segments;

               for (int bar = 0; bar < 3; bar++) {
                  float barY = b.y() + 13.0F + 2.0F + (float)bar * 10.0F;

                  for (int i = 0; i < segments; i++) {
                     float t0 = (float)i / (float)segments;
                     float t1 = (float)(i + 1) / (float)segments;

                     int c0 = switch (bar) {
                        case 0 -> Color.HSBtoRGB(t0, 1.0F, 1.0F);
                        case 1 -> Color.HSBtoRGB(hue, t0, bri);
                        default -> Color.HSBtoRGB(hue, sat, t0);
                     };

                     int c1 = switch (bar) {
                        case 0 -> Color.HSBtoRGB(t1, 1.0F, 1.0F);
                        case 1 -> Color.HSBtoRGB(hue, t1, bri);
                        default -> Color.HSBtoRGB(hue, sat, t1);
                     };
                     int a0 = ColorUtil.replAlpha(c0, alpha);
                     int a1 = ColorUtil.replAlpha(c1, alpha);
                     RenderUtil.Render2D.gradientRect(
                        barX + (float)i * segment,
                        barY,
                        segment + (i == segments - 1 ? 0.0F : 0.5F),
                        3.5F,
                        new int[]{a0, a1, a1, a0},
                        i == 0 ? 2.0F : 0.0F,
                        i == segments - 1 ? 2.0F : 0.0F,
                        i == segments - 1 ? 2.0F : 0.0F,
                        i == 0 ? 2.0F : 0.0F
                     );
                  }

                  float knobX = barX + barW * smoothHsb[bar];
                  int knobColor = bar == 0 ? Color.HSBtoRGB(hue, 1.0F, 1.0F) : Color.HSBtoRGB(hue, sat, bri);
                  RenderUtil.Render2D.rect(knobX - 2.5F, barY - 0.75F, 5.0F, 5.0F, ColorUtil.getColor(255, alpha), 5.0F);
                  RenderUtil.Render2D.rect(knobX - 1.75F, barY, 3.5F, 3.5F, ColorUtil.replAlpha(knobColor, alpha), 5.0F);
               }
            }
         }
      }
   }

   public boolean handleClick(float mouseX, float mouseY, int button) {
      if (this.module != null && this.bounds.contains(mouseX, mouseY)) {
         if (this.headerBounds.contains(mouseX, mouseY)) {
            if (this.embedded) {
               return true;
            } else {
               if (button == 0) {
                  this.draggingSelf = true;
                  this.dragOffsetX = mouseX - this.x;
                  this.dragOffsetY = mouseY - this.y;
               }

               return true;
            }
         } else if (this.embedded && mouseY < this.y + 24.0F) {
            return true;
         } else if (this.showEnableRow && this.enableBounds.contains(mouseX, mouseY)) {
            if (button == 0) {
               this.module.setEnabled(!this.module.isEnabled(), false);
            }

            return true;
         } else {
            for (SettingsPanel.PanelRow row : this.rows) {
               if (row.bounds.contains(mouseX, mouseY)) {
                  Setting<?> setting = row.setting;
                  if (setting instanceof BooleanSetting booleanSetting) {
                     if (button == 0) {
                        booleanSetting.set(!booleanSetting.getValue());
                     }
                  } else if (setting instanceof SliderSetting slider) {
                     if (button == 0) {
                        this.draggingSlider = slider;
                        this.draggingSliderBounds = row.bounds;
                        this.applySlider(slider, row.bounds, mouseX);
                     }
                  } else if (setting instanceof ModeSetting mode) {
                     if (button == 0) {
                        for (int i = 0; i < row.chips.size(); i++) {
                           if (row.chips.get(i).contains(mouseX, mouseY)) {
                              mode.set(mode.values.get(i));
                              break;
                           }
                        }
                     }
                  } else if (setting instanceof ColorSetting color) {
                     if (button == 0) {
                        if (mouseY <= row.bounds.y() + 13.0F) {
                           color.pickerOpen = !color.pickerOpen;
                        } else if (color.pickerOpen) {
                           for (int bar = 0; bar < 3; bar++) {
                              float barY = row.bounds.y() + 13.0F + 2.0F + (float)bar * 10.0F;
                              if (MathUtil.isHovered(mouseX, mouseY, row.bounds.x() + 6.0F, barY - 2.5F, row.bounds.width() - 12.0F, 9.0F)) {
                                 this.draggingColor = color;
                                 this.draggingColorBar = bar;
                                 this.draggingColorBounds = row.bounds;
                                 this.applyColorDrag(mouseX);
                              }
                           }
                        }
                     }
                  } else if (setting instanceof ButtonSetting buttonSetting && button == 0) {
                     buttonSetting.press();
                  }

                  return true;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean handleScroll(float mouseX, float mouseY, double verticalAmount) {
      if (this.module != null && this.bounds.contains(mouseX, mouseY)) {
         this.scrollTarget = MathUtil.clamp((float)((double)this.scrollTarget - verticalAmount * 18.0), 0.0F, this.maxScroll);
         return true;
      } else {
         return false;
      }
   }

   public boolean rawMouseMoved(float mouseX, float mouseY) {
      if (this.draggingSelf) {
         this.x = mouseX - this.dragOffsetX;
         this.y = mouseY - this.dragOffsetY;
         return true;
      } else if (this.draggingSlider != null) {
         this.applySlider(this.draggingSlider, this.draggingSliderBounds, mouseX);
         return true;
      } else if (this.draggingColor != null) {
         this.applyColorDrag(mouseX);
         return true;
      } else {
         return false;
      }
   }

   private void applySlider(SliderSetting setting, Rect sliderBounds, float mouseX) {
      float trackX = sliderBounds.x() + 6.0F;
      float trackW = Math.max(1.0F, sliderBounds.width() - 12.0F);
      float progress = class_3532.method_15363((mouseX - trackX) / trackW, 0.0F, 1.0F);
      setting.set(class_3532.method_15363(MathUtil.round(setting.min + (setting.max - setting.min) * progress, setting.increment), setting.min, setting.max));
   }

   private void applyColorDrag(float mouseX) {
      if (this.draggingColor != null) {
         int argb = this.draggingColor.getValue();
         float[] hsb = Color.RGBtoHSB(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, null);
         float t = MathUtil.clamp((mouseX - (this.draggingColorBounds.x() + 6.0F)) / Math.max(1.0F, this.draggingColorBounds.width() - 12.0F), 0.0F, 1.0F);
         float[] next = new float[]{hsb[0], hsb[1], hsb[2]};
         next[this.draggingColorBar] = t;
         int rgb = Color.HSBtoRGB(next[0], next[1], next[2]);
         this.draggingColor.set(argb & 0xFF000000 | rgb & 16777215);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class PanelRow {
      private final Setting<?> setting;
      private final String label;
      private final List<Rect> chips = new ArrayList<>();
      private Rect bounds = Rect.EMPTY;
      private float vis = 1.0F;

      private PanelRow(Setting<?> setting, String label) {
         this.setting = setting;
         this.label = label;
      }
   }
}
