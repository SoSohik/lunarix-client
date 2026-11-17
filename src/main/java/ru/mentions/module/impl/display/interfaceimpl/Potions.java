package ru.mentions.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Locale.Category;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_329;
import net.minecraft.class_408;
import net.minecraft.class_4081;
import org.joml.Matrix3x2fStack;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorFormatting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.potion.CustomBuffs;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class Potions implements IMinecraft {
   private static float S = 1.0F;
   private static float H = 16.0F * S;
   private static float MIN_W = 44.0F * S;
   private static float RADIUS = 7.0F * S;
   private static float TITLE_TEXT = 7.0F * S;
   private static float TITLE_ICON = 5.0F * S;
   private static float ROW_TEXT = 6.5F * S;
   private static float ICON = 7.0F * S;
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
   private static float SEP_PADDING_X = 4.0F * S;
   private static float SEP_OFFSET_Y = 3.2F * S;
   private static float SCROLL_OFFSET_Y = 4.0F * S;
   private Animation animation1 = new EaseInOutQuad(300, 1.0);
   private Animation animation2 = new EaseInOutQuad(300, 1.0);
   private final Map<String, Potions.EffectData> displayedEffects = new LinkedHashMap<>();
   private final List<Potions.EffectData> sortedEffects = new ArrayList<>();
   private final List<String> toRemove = new ArrayList<>();
   private boolean orderDirty;

   public void onRender(DragSetting dragSetting, InterFace interFace, EventDisplay eventDisplay) {
      Collection<class_1293> currentEffects = mc.field_1724.method_6026();
      this.displayedEffects.values().forEach(data -> data.active = false);
      S = InterFace.getInstance().sizeHud.getValue();
      H = 16.0F * S;
      MIN_W = 44.0F * S;
      RADIUS = 7.0F * S;
      TITLE_TEXT = 7.0F * S;
      TITLE_ICON = 5.0F * S;
      ROW_TEXT = 6.5F * S;
      ICON = 7.0F * S;
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
      SEP_PADDING_X = 4.0F * S;
      SEP_OFFSET_Y = 3.2F * S;
      SCROLL_OFFSET_Y = 4.0F * S;
      Locale formatLocale = Locale.getDefault(Category.FORMAT);
      Set<class_1293> groupedEffects = new HashSet<>();
      if (interFace.groupCustomBuffs != null && interFace.groupCustomBuffs.getValue()) {
         for (CustomBuffs.ActiveBuff ab : CustomBuffs.detectActiveBuffs(currentEffects)) {
            if (!"zeus_arrow".equals(ab.getBuff().getId()) && !"light_arrow".equals(ab.getBuff().getId())) {
               groupedEffects.addAll(ab.getMatchedInstances());
               String key = "custom:" + ab.getBuff().getId();
               String name = ab.getBuff().getLocalizedName();
               Potions.EffectData data = this.displayedEffects.get(key);
               int seconds = ab.getDurationSeconds();
               String duration = ab.getDurationString();
               if (data == null) {
                  data = new Potions.EffectData(name, duration, 1, false, ab.getDurationTicks(), ab.getPrimaryInstance());
                  data.mapKey = key;
                  data.customBuff = ab.getBuff();
                  data.customIcon = ab.getBuff().createIconStack();
                  this.displayedEffects.put(key, data);
                  this.orderDirty = true;
               } else {
                  data.mapKey = key;
                  data.customIcon = ab.getBuff().createIconStack();
                  data.customBuff = ab.getBuff();
                  if (!duration.equals(data.duration)) {
                     this.orderDirty = this.orderDirty | duration.length() != data.duration.length();
                     data.prevDuration = data.duration;
                     data.duration = duration;
                     data.digitAnim.set(0.0);
                     data.digitAnim.run(1.0, 0.18, Easings.QUAD_OUT);
                  }
               }

               data.name = name;
               data.durationSeconds = seconds;
               data.durationTicks = ab.getDurationTicks();
               data.effectInstance = ab.getPrimaryInstance();
               data.active = true;
            }
         }
      }

      for (class_1293 effect : currentEffects) {
         if (!groupedEffects.contains(effect)) {
            String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            int amplifier = effect.method_5578() + 1;
            boolean isNegative = ((class_1291)effect.method_5579().comp_349()).method_18792() == class_4081.field_18272;
            Potions.EffectData data2 = this.displayedEffects.get(name);
            int seconds = effect.method_5584() / 20;
            boolean infinite = effect.method_48559();
            String duration;
            if (data2 == null) {
               duration = this.getDurationString(effect);
               data2 = new Potions.EffectData(name, duration, amplifier, isNegative, effect.method_5584(), effect);
               this.displayedEffects.put(name, data2);
               this.orderDirty = true;
            } else {
               duration = seconds == data2.durationSeconds && infinite == data2.infinite && formatLocale.equals(data2.formatLocale)
                  ? data2.duration
                  : this.getDurationString(effect);
            }

            data2.durationSeconds = seconds;
            data2.infinite = infinite;
            data2.formatLocale = formatLocale;
            if (!duration.equals(data2.duration)) {
               this.orderDirty = this.orderDirty | duration.length() != data2.duration.length();
               data2.prevDuration = data2.duration;
               data2.duration = duration;
               data2.digitAnim.set(0.0);
               data2.digitAnim.run(1.0, 0.18, Easings.QUAD_OUT);
            }

            data2.durationTicks = effect.method_5584();
            data2.negative = isNegative;
            data2.effectInstance = effect;
            data2.active = true;
         }
      }

      boolean isEmpty = this.displayedEffects.isEmpty();
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
         Fonts.mentions_2.draw("P", x + TITLE_ICON_X, y + TITLE_ICON_Y, TITLE_ICON, ColorUtil.replAlpha(ColorUtil.client(), alpha2));
         font.draw("Potions", x + TITLE_TEXT_X, y + TITLE_TEXT_Y, TITLE_TEXT, ColorUtil.multAlpha(ColorUtil.getColor(240), alpha2));
         if (this.orderDirty) {
            this.sortedEffects.clear();
            this.sortedEffects.addAll(this.displayedEffects.values());
            this.sortedEffects.sort(Comparator.<Potions.EffectData>comparingInt(data -> data.name.length() + data.duration.length()).reversed());
            this.orderDirty = false;
         }

         float h = 4.0F * S;
         float w = 0.0F;
         this.toRemove.clear();

         for (Potions.EffectData data3 : this.sortedEffects) {
            data3.animation.update();
            data3.animation.run(data3.active ? 1.0 : 0.0, 0.12F, Easings.QUAD_OUT);
            data3.digitAnim.update();
            float a = data3.animation.get();
            if (a <= 0.01F && !data3.active) {
               this.toRemove.add(data3.mapKey != null ? data3.mapKey : data3.name);
            } else {
               data3.durationWidth = font.getWidth(data3.duration, ROW_TEXT);
               float rowW = ROW_BASE_W + font.getWidth(this.label(data3), ROW_TEXT) + data3.durationWidth + ICON;
               w = Math.max(w, rowW * a);
               h += ROW_HEIGHT * a;
            }
         }

         Draw.glow(x, y, w, h - 0.5F, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha * 0.1F), RADIUS, 12.0F, 1.0F);
         RenderUtil.Blur.blur(x, y, w, h, alpha, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha * InterFace.getInstance().alphaHUD.getValue()));
         Draw.outline(x, y, w, h, 0.75F, ThemeColor.getOutlineColor(alpha * 0.08F), RADIUS);
         float offsetY = y + ROW_START_Y;
         float offsetY2 = 0.0F;
         boolean firstRow = true;

         for (Potions.EffectData data4 : this.sortedEffects) {
            float a = data4.animation.get();
            if (!(a <= 0.01F) || data4.active) {
               float addX = 0.0F;
               int lvl = data4.effectInstance.method_5578() + 1;
               boolean bad = data4.negative;
               int nameColor = data4.customBuff != null
                  ? ColorUtil.multAlpha(data4.customBuff.getColor(), alpha * a)
                  : (bad ? ColorUtil.getColor(235, 70, 70, alpha * a) : ColorUtil.getColor(240, alpha * a));
               int lvlColor = bad ? ColorUtil.getColor(170, 55, 55, alpha * a) : ColorUtil.getColor(150, alpha * a);
               int timeColor = bad ? ColorUtil.getColor(200, 60, 60, alpha * a) : ColorUtil.getColor(200, alpha * a);
               String effectname = data4.customBuff != null
                  ? "[★] " + data4.name
                  : data4.name + (lvl > 1 ? " " + ColorFormatting.getColor(lvlColor) + lvl : "");
               font.draw(effectname, x + ROW_PADDING_X - addX, offsetY, ROW_TEXT, nameColor);
               float timeWidth = data4.durationWidth;
               this.drawDuration(font, data4, x + addX - ROW_PADDING_X + w - timeWidth - ICON - ICON_PADDING, offsetY, ROW_TEXT, timeColor);
               this.drawEffectIcon(eventDisplay, data4, x + addX - ROW_PADDING_X + w - ICON, offsetY, alpha * a);
               if (!firstRow) {
                  RenderUtil.Render2D.rect(
                     x + SEP_PADDING_X + addX, offsetY - SEP_OFFSET_Y, w - SEP_PADDING_X * 2.0F - addX, 0.5F, ColorUtil.getColor(255, 0.05F * alpha * a), 1.0F
                  );
               }

               firstRow = false;
               offsetY += ROW_HEIGHT * a;
               offsetY2 += ROW_HEIGHT * a;
            }
         }

         if (!this.toRemove.isEmpty()) {
            this.toRemove.forEach(this.displayedEffects::remove);
            this.orderDirty = true;
         }

         dragSetting.size
            .set((float)ColorUtil.overCol((int)Math.max(w, 20.0F * S), (int)MIN_W, alpha2), (float)ColorUtil.overCol((int)offsetY2, (int)H, alpha2));
      }
   }

   private void drawDuration(Font font, Potions.EffectData data, float x, float y, float size, int color) {
      float t = data.digitAnim.get();
      String now = data.duration;
      String was = data.prevDuration == null ? now : data.prevDuration;
      float cx = x;

      for (int i = 0; i < now.length(); i++) {
         String ch = now.substring(i, i + 1);
         int j = was.length() - (now.length() - i);
         String old = j >= 0 && j < was.length() ? was.substring(j, j + 1) : null;
         if (!(t >= 1.0F) && !ch.equals(old)) {
            font.draw(ch, cx, y + SCROLL_OFFSET_Y * (1.0F - t), size, ColorUtil.multAlpha(color, t));
            if (old != null) {
               font.draw(old, cx, y - SCROLL_OFFSET_Y * t, size, ColorUtil.multAlpha(color, 1.0F - t));
            }
         } else {
            font.draw(ch, cx, y, size, color);
         }

         cx += font.getWidth(ch, size);
      }
   }

   private String label(Potions.EffectData data) {
      if (data.customBuff != null) {
         return "[★] " + data.name;
      } else {
         int lvl = data.effectInstance.method_5578() + 1;
         if (data.label == null || data.lvl != lvl) {
            data.label = lvl > 1 ? data.name + " " + lvl : data.name;
            data.lvl = lvl;
         }

         return data.label;
      }
   }

   private void drawEffectIcon(EventDisplay eventDisplay, Potions.EffectData data, float x, float y, float alpha) {
      if (data.customBuff != null && data.customIcon != null) {
         ItemRender.drawItemWithContext(eventDisplay.getDrawContext(), data.customIcon, x, y - 0.5F, ICON / 16.0F, alpha);
      } else {
         class_2960 effectTex = class_329.method_71644(data.effectInstance.method_5579());
         Matrix3x2fStack matrices = eventDisplay.getDrawContext().method_51448();
         matrices.pushMatrix();
         matrices.translate(x, y);
         matrices.scale(ICON / 12.0F, ICON / 12.0F);
         eventDisplay.getDrawContext().method_52707(class_10799.field_56883, effectTex, 0, 0, 12, 12, ColorUtil.getColor(255, alpha));
         matrices.popMatrix();
      }
   }

   private String getDurationString(class_1293 effect) {
      if (effect.method_48559()) {
         return "∞";
      } else {
         int t = effect.method_5584() / 20;
         return String.format("%d:%02d", t / 60, t % 60);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class EffectData {
      String key;
      String mapKey;
      String name;
      String duration;
      String prevDuration;
      String label;
      int durationSeconds;
      boolean infinite;
      Locale formatLocale;
      float durationWidth;
      final ru.mentions.utils.animation.Animation digitAnim = new ru.mentions.utils.animation.Animation();
      ru.mentions.utils.animation.Animation animation;
      boolean active;
      boolean negative;
      int durationTicks;
      int lvl;
      class_1293 effectInstance;
      CustomBuffs.CustomBuff customBuff;
      class_1799 customIcon;

      public EffectData(String name, String duration, int lvl, boolean negative, int durationTicks, class_1293 effectInstance) {
         this.name = name;
         this.mapKey = name;
         this.duration = duration;
         this.prevDuration = duration;
         this.lvl = lvl;
         this.negative = negative;
         this.durationTicks = durationTicks;
         this.animation = new ru.mentions.utils.animation.Animation();
         this.active = true;
         this.effectInstance = effectInstance;
      }
   }
}
