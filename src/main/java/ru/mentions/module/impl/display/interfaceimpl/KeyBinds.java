package ru.mentions.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_408;
import ru.mentions.Client;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.Keyboard;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class KeyBinds implements element {
   private static float S = 1.0F;
   private static float H = 16.0F * S;
   private static float MIN_W = 50.0F * S;
   private static float RADIUS = 7.0F * S;
   private static float TITLE_TEXT = 7.0F * S;
   private static float TITLE_ICON = 5.0F * S;
   private static float ROW_TEXT = 6.5F * S;
   private static float TITLE_ICON_X = 5.0F * S;
   private static float TITLE_ICON_Y = 5.5F * S;
   private static float TITLE_TEXT_X = 12.5F * S;
   private static float TITLE_TEXT_Y = 3.4F * S;
   private static float ROW_HEIGHT = 14.0F * S;
   private static float ROW_BASE_W = 27.0F * S;
   private static float ROW_PADDING_X = 5.0F * S;
   private static float ROW_START_Y = 5.0F * S;
   private static float ROW_ANIM_OFFSET = 15.0F * S;
   private static float ICON_PADDING = 4.0F * S;
   private static float ICON_OFFSET_Y = 0.9F * S;
   private static float SEP_PADDING_X = 4.0F * S;
   private static float SEP_OFFSET_Y = 3.2F * S;
   private final HudModuleOrder moduleOrder = new HudModuleOrder();
   private final List<KeyBinds.Row> sourceRows = new ArrayList<>();
   private final List<KeyBinds.Row> rows = new ArrayList<>();
   private Animation openAnimation = new Animation();
   private ru.mentions.utils.animation.satoshi.Animation animation1 = new EaseInOutQuad(300, 1.0);
   private ru.mentions.utils.animation.satoshi.Animation animation2 = new EaseInOutQuad(300, 1.0);

   private void updateRows() {
      boolean changed = false;
      int index = 0;

      for (Module module : this.moduleOrder.get(Client.get().moduleManager())) {
         if (module.getKey() > 0 && !module.getName().equalsIgnoreCase("ClickGui") && (module.isEnabled() || module.getAnimation().getOutput() > 0.0F)) {
            KeyBinds.Row row;
            if (index == this.sourceRows.size()) {
               row = new KeyBinds.Row(module);
               this.sourceRows.add(row);
               changed = true;
            } else {
               row = this.sourceRows.get(index);
               if (row.module != module) {
                  row = new KeyBinds.Row(module);
                  this.sourceRows.set(index, row);
                  changed = true;
               }
            }

            row.key = Keyboard.keyName(module.getKey());
            int length = module.getName().length() + row.key.length();
            changed |= row.sortLength != length;
            row.sortLength = length;
            index++;
         }
      }

      if (index < this.sourceRows.size()) {
         this.sourceRows.subList(index, this.sourceRows.size()).clear();
         changed = true;
      }

      if (changed) {
         this.rows.clear();
         this.rows.addAll(this.sourceRows);
         this.rows.sort(Comparator.<KeyBinds.Row>comparingInt(rowx -> rowx.sortLength).reversed());
      }
   }

   @Override
   public void onRender(DragSetting dragSetting, InterFace interFace) {
      S = InterFace.getInstance().sizeHud.getValue();
      H = 16.0F * S;
      MIN_W = 50.0F * S;
      RADIUS = 7.0F * S;
      TITLE_TEXT = 7.0F * S;
      TITLE_ICON = 5.0F * S;
      ROW_TEXT = 6.5F * S;
      TITLE_ICON_X = 5.0F * S;
      TITLE_ICON_Y = 5.5F * S;
      TITLE_TEXT_X = 12.5F * S;
      TITLE_TEXT_Y = 3.4F * S;
      ROW_HEIGHT = 14.0F * S;
      ROW_BASE_W = 27.0F * S;
      ROW_PADDING_X = 5.0F * S;
      ROW_START_Y = 5.0F * S;
      ROW_ANIM_OFFSET = 15.0F * S;
      ICON_PADDING = 4.0F * S;
      ICON_OFFSET_Y = 0.9F * S;
      SEP_PADDING_X = 4.0F * S;
      SEP_OFFSET_Y = 3.2F * S;
      this.updateRows();
      boolean isEmpty = this.rows.isEmpty();
      float x = dragSetting.position.x;
      float y = dragSetting.position.y;
      boolean closeCondition = isEmpty && !(mc.field_1755 instanceof class_408);
      this.animation1.setDirection(closeCondition ? Direction.BACKWARDS : Direction.FORWARDS);
      this.animation2.setDirection(mc.field_1755 instanceof class_408 && isEmpty ? Direction.FORWARDS : Direction.BACKWARDS);
      dragSetting.active = !closeCondition;
      float alpha = this.animation1.getOutput();
      if (!closeCondition || alpha != 0.0F) {
         float alpha2 = this.animation2.getOutput();
         Draw.glow(x, y, MIN_W, H, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha2 * 0.1F), RADIUS, 12.0F, 1.0F);
         RenderUtil.Blur.blur(x, y, MIN_W, H, alpha2, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha2 * InterFace.getInstance().alphaHUD.getValue()));
         Draw.outline(x, y, MIN_W, H, 0.75F, ThemeColor.getOutlineColor(alpha2 * 0.08F), RADIUS);
         Font font = Fonts.sf_regular;
         Font cat = Fonts.mentions_2;
         cat.draw("E", x + TITLE_ICON_X, y + TITLE_ICON_Y, TITLE_ICON, ColorUtil.replAlpha(ColorUtil.client(), alpha2));
         font.draw("Key binds", x + TITLE_TEXT_X, y + TITLE_TEXT_Y, TITLE_TEXT, ColorUtil.multAlpha(ColorUtil.getColor(240), alpha2));
         float h = 4.0F * S;
         float w = 0.0F;

         for (KeyBinds.Row row : this.rows) {
            Module m2 = row.module;
            m2.animation.setDirection(m2.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
            float mAnim = m2.getAnimation().getOutput();
            String name = m2.getCategory().getIcon();
            row.keyWidth = font.getWidth(row.key, ROW_TEXT);
            row.iconWidth = cat.getWidth(name, ROW_TEXT);
            if (!(mAnim <= 0.0F)) {
               float rowW = ROW_BASE_W + font.getWidth(m2.getBigName(), ROW_TEXT) + row.keyWidth + row.iconWidth;
               w = Math.max(w, rowW * mAnim);
               h += ROW_HEIGHT * mAnim;
            }
         }

         Draw.glow(x, y, w, h - 0.5F, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha * 0.1F), RADIUS, 12.0F, 1.0F);
         RenderUtil.Blur.blur(x, y, w, h, alpha, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha * InterFace.getInstance().alphaHUD.getValue()));
         Draw.outline(x, y, w, h, 0.75F, ThemeColor.getOutlineColor(alpha * 0.08F), RADIUS);
         float offsetY = y + ROW_START_Y;
         float offsetY2 = 0.0F;

         for (KeyBinds.Row rowx : this.rows) {
            Module m3 = rowx.module;
            float mAnim = m3.getAnimation().getOutput();
            if (!(mAnim <= 0.0F)) {
               float addX = 0.0F;
               String name = m3.getCategory().getIcon();
               font.draw(m3.getBigName(), x + ROW_PADDING_X - addX, offsetY, ROW_TEXT, ColorUtil.getColor(240, alpha * mAnim));
               String key = rowx.key;
               float keyWidth = rowx.keyWidth;
               float iconWidth = rowx.iconWidth;
               font.draw(key, x + addX - ROW_PADDING_X + w - keyWidth - iconWidth - ICON_PADDING, offsetY, ROW_TEXT, ColorUtil.getColor(200, alpha * mAnim));
               cat.draw(
                  name, x + addX - ROW_PADDING_X + w - iconWidth, offsetY + ICON_OFFSET_Y, ROW_TEXT, ColorUtil.replAlpha(ColorUtil.client(), alpha * mAnim)
               );
               if (this.rows.getFirst() != rowx) {
                  RenderUtil.Render2D.rect(
                     x + SEP_PADDING_X + addX,
                     offsetY - SEP_OFFSET_Y,
                     w - SEP_PADDING_X * 2.0F - addX,
                     0.5F,
                     ColorUtil.getColor(255, 0.05F * alpha * mAnim),
                     1.0F
                  );
               }

               offsetY += ROW_HEIGHT * mAnim;
               offsetY2 += ROW_HEIGHT * mAnim;
            }
         }

         dragSetting.size
            .set((float)ColorUtil.overCol((int)Math.max(w, 20.0F * S), (int)MIN_W, alpha2), (float)ColorUtil.overCol((int)offsetY2, (int)H, alpha2));
      }
   }

   private static final class Row {
      final Module module;
      String key;
      int sortLength;
      float keyWidth;
      float iconWidth;

      Row(Module module) {
         this.module = module;
      }
   }
}
