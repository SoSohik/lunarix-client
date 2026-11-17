package ru.mentions.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_1657;
import net.minecraft.class_408;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.combat.UseTracker;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.UseCooldowns;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.RollingText;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class UseTrackerHud implements element {
   private static float S = 1.0F;
   private static float H = 16.0F * S;
   private static float MIN_W = 60.0F * S;
   private static float RADIUS = 7.0F * S;
   private static float BASE_H = 4.0F * S;
   private static float TITLE_TEXT = 7.0F * S;
   private static float TITLE_ICON = 5.0F * S;
   private static float ROW_TEXT = 6.5F * S;
   private static float TITLE_ICON_X = 5.0F * S;
   private static float TITLE_ICON_Y = 5.5F * S;
   private static float TITLE_TEXT_X = 12.5F * S;
   private static float TITLE_TEXT_Y = 3.4F * S;
   private static float ROW_HEIGHT = 14.0F * S;
   private static float ROW_BASE_W = 22.0F * S;
   private static float ROW_PADDING_X = 5.0F * S;
   private static float ROW_START_Y = 5.0F * S;
   private static float ROW_ANIM_OFFSET = 15.0F * S;
   private static float HEAD_TEXT_X = 15.0F * S;
   private static float HEAD_ICON_Y = 0.9F * S;
   private static float SEP_PADDING_X = 4.0F * S;
   private static float SEP_OFFSET_Y = 3.2F * S;
   private final Animation animation1 = new EaseInOutQuad(300, 1.0);
   private final Animation animation2 = new EaseInOutQuad(300, 1.0);
   private float widthAnim = 0.0F;
   private final ru.mentions.utils.animation.Animation headAnimation = new ru.mentions.utils.animation.Animation();
   private String head = "";
   private final Map<String, UseTrackerHud.Row> rows = new LinkedHashMap<>();

   @Override
   public void onRender(DragSetting dragSetting, InterFace interFace) {
      UseTracker tracker = UseTracker.getInstance();
      S = InterFace.getInstance().sizeHud.getValue();
      H = 16.0F * S;
      MIN_W = 60.0F * S;
      RADIUS = 7.0F * S;
      BASE_H = 4.0F * S;
      TITLE_TEXT = 7.0F * S;
      TITLE_ICON = 5.0F * S;
      ROW_TEXT = 6.5F * S;
      TITLE_ICON_X = 5.0F * S;
      TITLE_ICON_Y = 5.5F * S;
      TITLE_TEXT_X = 12.5F * S;
      TITLE_TEXT_Y = 3.4F * S;
      ROW_HEIGHT = 14.0F * S;
      ROW_BASE_W = 22.0F * S;
      ROW_PADDING_X = 5.0F * S;
      ROW_START_Y = 5.0F * S;
      ROW_ANIM_OFFSET = 15.0F * S;
      HEAD_TEXT_X = 15.0F * S;
      HEAD_ICON_Y = 0.9F * S;
      SEP_PADDING_X = 4.0F * S;
      SEP_OFFSET_Y = 3.2F * S;
      this.rows.values().forEach(row -> row.active = false);
      boolean hasTarget = tracker.hasTarget();
      if (hasTarget) {
         for (UseCooldowns.Item item : UseCooldowns.Item.values()) {
            int left = tracker.remaining(item);
            if (left > 0) {
               UseTrackerHud.Row row2 = this.rows.computeIfAbsent(item.name(), k -> new UseTrackerHud.Row());
               row2.label = item.label;
               row2.color = ColorUtil.getColor(240);
               row2.time.set(time(left));
               row2.active = true;
            }
         }

         class_1657 player = tracker.getTargetPlayer();
         if (player != null) {
            for (UseCooldowns.Buff buff : UseCooldowns.Buff.values()) {
               class_1293 effect;
               if (tracker.buffs.getValue(buff.label) && (effect = player.method_6112(buff.effect)) != null) {
                  UseTrackerHud.Row row3 = this.rows.computeIfAbsent(buff.name(), k -> new UseTrackerHud.Row());
                  row3.label = buff.label + roman(effect.method_5578() + 1);
                  row3.color = buff.color;
                  row3.time.set(time(effect.method_5584() / 20));
                  row3.active = true;
               }
            }
         }
      }

      String targetName = hasTarget ? tracker.getTargetName() : "";
      if (!targetName.isEmpty()) {
         this.head = targetName;
      }

      this.headAnimation.update();
      this.headAnimation.run(targetName.isEmpty() ? 0.0 : 1.0, 0.12F, Easings.QUAD_OUT);
      float ha = this.headAnimation.get();
      boolean isEmpty = ha <= 0.01F && this.rows.values().stream().noneMatch(row -> row.active);
      float x = dragSetting.position.x;
      float y = dragSetting.position.y;
      boolean closeCondition = isEmpty && !(mc.field_1755 instanceof class_408);
      this.animation1.setDirection(closeCondition ? Direction.BACKWARDS : Direction.FORWARDS);
      this.animation2.setDirection(mc.field_1755 instanceof class_408 && isEmpty ? Direction.FORWARDS : Direction.BACKWARDS);
      dragSetting.active = !closeCondition;
      float alpha = this.animation1.getOutput();
      if (closeCondition && alpha == 0.0F) {
         this.rows.clear();
         this.head = "";
      } else {
         float alpha2 = this.animation2.getOutput();
         Draw.glow(x, y, MIN_W, H, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha2 * 0.1F), RADIUS, 12.0F, 1.0F);
         RenderUtil.Blur.blur(x, y, MIN_W, H, alpha2, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha2 * InterFace.getInstance().alphaHUD.getValue()));
         Draw.outline(x, y, MIN_W, H, 0.75F, ThemeColor.getOutlineColor(alpha2 * 0.08F), RADIUS);
         Font font = Fonts.sf_regular;
         Fonts.mentions_2.draw("V", x + TITLE_ICON_X, y + TITLE_ICON_Y, TITLE_ICON, ColorUtil.replAlpha(ColorUtil.client(), alpha2));
         font.draw("User Tracker", x + TITLE_TEXT_X, y + TITLE_TEXT_Y, TITLE_TEXT, ColorUtil.multAlpha(ColorUtil.getColor(240), alpha2));
         float h = BASE_H;
         float w = 0.0F;
         ArrayList<String> toRemove = new ArrayList<>();

         for (Entry<String, UseTrackerHud.Row> entry : this.rows.entrySet()) {
            UseTrackerHud.Row row4 = entry.getValue();
            row4.animation.update();
            row4.animation.run(row4.active ? 1.0 : 0.0, 0.12F, Easings.QUAD_OUT);
            float a = row4.animation.get();
            if (a <= 0.01F && !row4.active) {
               toRemove.add(entry.getKey());
            } else {
               float rowW = ROW_BASE_W + font.getWidth(row4.label, ROW_TEXT) + row4.time.width(font, ROW_TEXT);
               w = Math.max(w, rowW * a);
               h += ROW_HEIGHT * a;
            }
         }

         toRemove.forEach(this.rows::remove);
         if (ha > 0.01F) {
            w = Math.max(w, (ROW_BASE_W + font.getWidth(this.head, ROW_TEXT)) * ha);
            h += ROW_HEIGHT * ha;
         }

         this.widthAnim = this.widthAnim + (w - this.widthAnim) * 0.2F;
         Draw.glow(x, y, this.widthAnim, h - 0.5F, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha * 0.1F), RADIUS, 12.0F, 1.0F);
         RenderUtil.Blur.blur(
            x, y, this.widthAnim, h, alpha, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha * InterFace.getInstance().alphaHUD.getValue())
         );
         Draw.outline(x, y, this.widthAnim, h, 0.75F, ThemeColor.getOutlineColor(alpha * 0.08F), RADIUS);
         float offsetY = y + ROW_START_Y;
         float offsetY2 = 0.0F;
         if (ha > 0.01F) {
            float addX = 0.0F;
            Fonts.category.draw("Q", x + ROW_PADDING_X - addX, offsetY + HEAD_ICON_Y, ROW_TEXT, ColorUtil.replAlpha(ColorUtil.client(), alpha * ha));
            font.draw(this.head, x + HEAD_TEXT_X - addX, offsetY, ROW_TEXT, ColorUtil.getColor(240, alpha * ha));
            offsetY += ROW_HEIGHT * ha;
            offsetY2 += ROW_HEIGHT * ha;
         }

         boolean firstRow = ha <= 0.01F;

         for (UseTrackerHud.Row row5 : this.rows.values()) {
            float a = row5.animation.get();
            if (!(a <= 0.01F) || row5.active) {
               float addX = ROW_ANIM_OFFSET - ROW_ANIM_OFFSET * a;
               font.draw(row5.label, x + ROW_PADDING_X - addX, offsetY, ROW_TEXT, ColorUtil.replAlpha(row5.color, alpha * a));
               row5.time
                  .draw(
                     font, x + addX - ROW_PADDING_X + this.widthAnim - row5.time.width(font, ROW_TEXT), offsetY, ROW_TEXT, ColorUtil.getColor(200, alpha * a)
                  );
               if (!firstRow) {
                  RenderUtil.Render2D.rect(
                     x + SEP_PADDING_X + addX,
                     offsetY - SEP_OFFSET_Y,
                     this.widthAnim - SEP_PADDING_X * 2.0F - addX,
                     0.5F,
                     ColorUtil.getColor(255, 0.05F * alpha * a),
                     1.0F
                  );
               }

               firstRow = false;
               offsetY += ROW_HEIGHT * a;
               offsetY2 += ROW_HEIGHT * a;
            }
         }

         dragSetting.size
            .set(
               (float)ColorUtil.overCol((int)Math.max(this.widthAnim, 20.0F * S), (int)MIN_W, alpha2), (float)ColorUtil.overCol((int)offsetY2, (int)H, alpha2)
            );
      }
   }

   private static String time(int seconds) {
      return String.format("%d:%02d", seconds / 60, seconds % 60);
   }

   private static String roman(int level) {
      return switch (level) {
         case 1 -> "";
         case 2 -> " II";
         case 3 -> " III";
         case 4 -> " IV";
         case 5 -> " V";
         default -> " " + level;
      };
   }

   @Environment(EnvType.CLIENT)
   private static class Row {
      final RollingText time = new RollingText();
      final ru.mentions.utils.animation.Animation animation = new ru.mentions.utils.animation.Animation();
      boolean active;
      String label = "";
      int color;
   }
}
