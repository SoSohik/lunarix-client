package ru.mentions.module.impl.display.interfaceimpl;

import com.mojang.blaze3d.textures.GpuTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10042;
import net.minecraft.class_1044;
import net.minecraft.class_1293;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1826;
import net.minecraft.class_266;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_408;
import net.minecraft.class_5250;
import net.minecraft.class_8646;
import net.minecraft.class_9013;
import net.minecraft.class_9025;
import net.minecraft.class_922;
import org.joml.Matrix3x2fStack;
import org.joml.Vector4d;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.combat.AimBot;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.module.impl.utils.NameProtect;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.potion.CustomBuffs;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.utils.taskript.StopWatch;

@Environment(EnvType.CLIENT)
public class TargetHud implements element {
   private static float S = 1.0F;
   private static float ITEM_SIZE = 8.0F * S;
   private static float ARMOR_PAD = 2.5F * S;
   private static float ARMOR_X = 10.0F * S;
   private static float ARMOR_Y = 12.0F * S;
   private static float W_BASE = 100.0F * S;
   private static float H_BASE = 30.0F * S;
   private static float RADIUS = 5.0F * S;
   private static float ANIM_OFFSET = 8.0F * S;
   private static float HEAD_CONTAINER_W = 30.0F * S;
   private static float HEAD_OFFSET_X = 3.0F * S;
   private static float HEAD_OFFSET_Y = 3.0F * S;
   private static float CONTENT_OFFSET_X = 34.0F * S;
   private static float BAR_OFFSET_X = 5.0F * S;
   private static float BAR_OFFSET_Y = 7.0F * S;
   private static float BAR_MARGIN_R = 10.0F * S;
   private static float BAR_HEIGHT = 3.0F * S;
   private static float NAME_Y = 4.5F * S;
   private static float HP_Y = 13.5F * S;
   private static float NAME_SIZE = 7.0F * S;
   private static float HP_SIZE = 6.0F * S;
   private static float GLOW_RADIUS = 12.0F * S;
   private static float BLUR_RADIUS = 20.0F * S;
   private static float FACE_SIZE = 24.0F * S;
   private final Animation openAnimation = new EaseInOutQuad(200, 1.0);
   private final ru.mentions.utils.animation.Animation settingsAnimation = new ru.mentions.utils.animation.Animation();
   private final ru.mentions.utils.animation.Animation animHP = new ru.mentions.utils.animation.Animation();
   private final ru.mentions.utils.animation.Animation animHpText = new ru.mentions.utils.animation.Animation();
   private final StopWatch time = new StopWatch();
   private static final class_1304[] ARMOR_SLOTS = new class_1304[]{
      class_1304.field_6173, class_1304.field_6171, class_1304.field_6166, class_1304.field_6172, class_1304.field_6174, class_1304.field_6169
   };
   private final ArrayList<class_1799> armorItems = new ArrayList<>(6);
   private float lastHpTextValue;
   private float lastAbsorptionValue;
   private String formattedHp;
   private String formattedAbsorption;
   private Locale formatLocale;
   private class_1309 target;
   private boolean inWorld;
   private boolean settingsOpen;
   private float lastX;
   private float lastY;
   private float lastW;
   private float lastH;
   private float popupX;
   private float popupY;
   private float popupW;
   private float popupH;
   private float hpColorX;
   private float hpColorY;
   private float hpColorW;
   private float hpColorH;
   private float view1X;
   private float view1Y;
   private float view1W;
   private float view1H;
   private float view2X;
   private float view2Y;
   private float view2W;
   private float view2H;

   @Override
   public void onRender(DragSetting drag, InterFace interFace) {
   }

   public void onRender(DragSetting drag, InterFace interFace, EventDisplay eventDisplay) {
      S = interFace.sizeHud.getValue();
      boolean circle = interFace.targetHudMode.is("circle");
      ITEM_SIZE = 8.0F * S;
      ARMOR_PAD = 2.5F * S;
      ARMOR_X = 10.0F * S;
      ARMOR_Y = 12.0F * S;
      W_BASE = 100.0F * S;
      H_BASE = 30.0F * S;
      RADIUS = 5.0F * S;
      ANIM_OFFSET = 8.0F * S;
      HEAD_CONTAINER_W = 30.0F * S;
      HEAD_OFFSET_X = 3.0F * S;
      HEAD_OFFSET_Y = 3.0F * S;
      CONTENT_OFFSET_X = 34.0F * S;
      BAR_OFFSET_X = 5.0F * S;
      BAR_OFFSET_Y = 7.0F * S;
      BAR_MARGIN_R = 10.0F * S;
      BAR_HEIGHT = 3.0F * S;
      NAME_Y = 4.5F * S;
      HP_Y = 13.5F * S;
      NAME_SIZE = 7.0F * S;
      HP_SIZE = 6.0F * S;
      GLOW_RADIUS = 12.0F * S;
      BLUR_RADIUS = 20.0F * S;
      FACE_SIZE = (circle ? 26.0F : 24.0F) * S;
      if (circle) {
         W_BASE = 136.0F * S;
         H_BASE = 36.0F * S;
         RADIUS = 6.0F * S;
         HEAD_OFFSET_X = 5.0F * S;
         HEAD_OFFSET_Y = 5.0F * S;
         CONTENT_OFFSET_X = 37.0F * S;
         NAME_Y = 4.5F * S;
         HP_Y = 14.0F * S;
         HP_SIZE = 5.5F * S;
      }

      if (mc.field_1724 != null && mc.field_1687 != null) {
         AttackAura attackAura = AttackAura.get();
         class_1309 auraTarget = attackAura != null && attackAura.isEnabled() ? AttackAura.target : null;
         AimBot legitAura = AimBot.get();
         if (auraTarget == null && legitAura != null && legitAura.isEnabled()) {
            auraTarget = AimBot.target;
         }

         if (auraTarget != null) {
            this.target = auraTarget;
            this.time.reset();
         }

         if (auraTarget == null && mc.field_1692 != null) {
            this.target = mc.field_1692.method_72393();
            this.time.reset();
         }

         if (mc.field_1755 instanceof class_408) {
            this.target = mc.field_1724;
            this.time.reset();
         }

         this.inWorld = this.target != null && mc.field_1687 != null && !this.target.method_31481();
         boolean out = !this.inWorld || this.time.finished(400.0);
         this.openAnimation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);
         float alpha = class_3532.method_15363(this.openAnimation.getOutput(), 0.0F, 1.0F);
         if (!(alpha <= 0.0F) && this.target != null) {
            float health = this.getHealth(this.target);
            float maxHealth = Math.max(1.0F, this.target.method_6063());
            float absorption = interFace.showAbsorption.getValue() ? Math.max(0.0F, this.target.method_6067()) : 0.0F;
            this.animHP.update();
            this.animHP.run((double)(health / maxHealth), 0.5, Easings.BACK_OUT);
            this.animHpText.update();
            this.animHpText.run((double)health, 0.15, Easings.LINEAR);
            Locale locale = Locale.getDefault(Category.FORMAT);
            boolean localeChanged = !locale.equals(this.formatLocale);
            this.formatLocale = locale;
            float hpTextValue = this.animHpText.get();
            if (this.formattedHp == null || localeChanged || Float.floatToIntBits(hpTextValue) != Float.floatToIntBits(this.lastHpTextValue)) {
               this.formattedHp = String.format(locale, "%.0f", hpTextValue);
               this.lastHpTextValue = hpTextValue;
            }

            if (this.formattedAbsorption == null || localeChanged || Float.floatToIntBits(absorption) != Float.floatToIntBits(this.lastAbsorptionValue)) {
               this.formattedAbsorption = absorption > 0.0F ? String.format(locale, "%.0f", absorption) : "";
               this.lastAbsorptionValue = absorption;
            }

            String hpText = !(health > 0.0F) && !ServerUtil.isCopyTime() && this.target.method_5767() ? "?" : this.formattedHp;
            String healthLabel = circle ? "HP " : "Здоровья: ";
            String absorptionText = this.formattedAbsorption.isEmpty()
               ? ""
               : (circle ? " +" + this.formattedAbsorption + " ABR" : " (+" + this.formattedAbsorption + " ABR)");
            if (!circle) {
               float healthTextWidth = Fonts.sf_regular.getWidth(healthLabel + hpText + absorptionText, HP_SIZE);
               W_BASE = Math.max(W_BASE, CONTENT_OFFSET_X + healthTextWidth + 5.0F * S);
            }

            float w = W_BASE;
            float h = H_BASE;
            boolean is3D = interFace.targetHudView != null && interFace.targetHudView.is("3D");
            float x;
            float y;
            if (is3D && this.inWorld && this.target != null && !(mc.field_1755 instanceof class_408)) {
               Vector4d projection = Projection.getVector4D(this.target, eventDisplay.getPartialTicks());
               if (projection == null || Projection.cantSee(projection)) {
                  drag.active = false;
                  return;
               }

               float offX = interFace.targetHud3DX != null ? interFace.targetHud3DX.getValue() : 0.0F;
               float offY = interFace.targetHud3DY != null ? interFace.targetHud3DY.getValue() : 0.0F;
               float centerX = (float)Projection.centerX(projection);
               float centerY = (float)projection.y - h - 8.0F * S;
               x = centerX - w / 2.0F + offX;
               y = centerY + offY;
               drag.active = false;
            } else {
               drag.active = true;
               drag.size.set(w, h);
               x = drag.position.x;
               y = drag.position.y;
            }

            this.lastX = x;
            this.lastY = y;
            this.lastW = w;
            this.lastH = h;
            float addALL = !circle && !is3D ? ANIM_OFFSET - ANIM_OFFSET * alpha : 0.0F;
            float cardX = x + addALL;
            int hpColor = ColorUtil.replAlpha(ColorUtil.getClientColor(1), alpha);
            int goldColor = ColorUtil.replAlpha(ColorUtil.getColor(255, 205, 85, 255), alpha);
            int trackColor = ColorUtil.getColor(255, 0.1F * alpha);
            float animatedHealth = class_3532.method_15363(this.animHP.get(), 0.0F, 1.0F) * maxHealth;
            float capacity = Math.max(maxHealth, animatedHealth + absorption);
            float hpProgress = animatedHealth / capacity;
            float totalProgress = (animatedHealth + absorption) / capacity;
            if (circle) {
               float cardAlpha = 0.9F + 0.1F * class_3532.method_15363(interFace.alphaHUD.getValue(), 0.0F, 1.0F);
               RenderUtil.Render2D.rect(cardX, y, w, h, ColorUtil.replAlpha(ColorUtil.getColor(17, 19, 25, 255), cardAlpha * alpha), RADIUS);
            } else {
               RenderUtil.Blur.blur(cardX, y, w, h, alpha, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), interFace.alphaHUD.getValue() * alpha));
            }

            Draw.outline(cardX, y, w, h, 0.75F * S, circle ? ColorUtil.multAlpha(hpColor, 0.25F) : ThemeColor.getOutlineColor(alpha * 0.08F), RADIUS);
            RenderUtil.Render2D.rect(cardX + HEAD_OFFSET_X, y + HEAD_OFFSET_Y, FACE_SIZE, FACE_SIZE, ColorUtil.getColor(255, 0.06F * alpha), 4.0F * S);
            float ringX = cardX + w - 17.0F * S;
            float ringY = y + h / 2.0F;
            if (circle) {
               RenderUtil.Render2D.roundedCircleProgress(
                  eventDisplay.getDrawContext(),
                  ringX,
                  ringY,
                  12.0F * S,
                  2.0F * S,
                  totalProgress,
                  trackColor,
                  absorption > 0.0F ? goldColor : hpColor,
                  absorption > 0.0F ? goldColor : hpColor
               );
               RenderUtil.Render2D.roundedCircleProgress(eventDisplay.getDrawContext(), ringX, ringY, 12.0F * S, 2.0F * S, hpProgress, 0, hpColor, hpColor);
            } else {
               RenderUtil.Render2D.rect(cardX + 31.0F * S, y + 5.0F * S, 0.5F * S, h - 10.0F * S, ThemeColor.getSeparatorColor(alpha), 0.25F * S);
               float barX = cardX + CONTENT_OFFSET_X;
               float barY = y + h - BAR_OFFSET_Y;
               float barW = w - CONTENT_OFFSET_X - BAR_MARGIN_R;
               float barRadius = BAR_HEIGHT / 2.0F;
               RenderUtil.Render2D.rect(barX, barY, barW, BAR_HEIGHT, trackColor, barRadius);
               if (absorption > 0.0F) {
                  RenderUtil.Render2D.rect(barX, barY, barW * totalProgress, BAR_HEIGHT, goldColor, barRadius);
               }

               if (hpProgress > 0.0F) {
                  RenderUtil.Render2D.gradientRect(
                     barX,
                     barY,
                     barW * hpProgress,
                     BAR_HEIGHT,
                     new int[]{ColorUtil.multDark(hpColor, 0.5F), hpColor, hpColor, ColorUtil.multDark(hpColor, 0.5F)},
                     barRadius
                  );
               }
            }

            Draw.flush();
            if (this.target instanceof class_1657) {
               this.drawFace(this.target, eventDisplay.getPartialTicks(), cardX + HEAD_OFFSET_X, y + HEAD_OFFSET_Y, alpha);
            } else {
               this.drawMobIcon(
                  eventDisplay, this.target, cardX + HEAD_OFFSET_X + FACE_SIZE / 2.0F, y + HEAD_OFFSET_Y + FACE_SIZE / 2.0F, FACE_SIZE, alpha, circle
               );
               eventDisplay.getDrawContext().method_71048();
            }

            Draw.flush();
            String playerName = mc.field_1724.method_5477().getString();
            String name = this.target
               .method_5477()
               .getString()
               .replace(playerName, Client.get().moduleManager().getOrDefault(NameProtect.class, null).isEnabled() ? "lunarix" : playerName);
            float contentX = cardX + CONTENT_OFFSET_X;
            float contentWidth = w - CONTENT_OFFSET_X - (circle ? 35.0F : 5.0F) * S;
            Fonts.sf_regular.drawFadingText(name, contentX, y + NAME_Y, contentWidth, ColorUtil.getColor(255, alpha), NAME_SIZE);
            float detailSize = HP_SIZE;
            if (circle) {
               float detailWidth = Fonts.sf_regular.getWidth(healthLabel + hpText + absorptionText, detailSize);
               detailSize *= Math.min(1.0F, contentWidth / Math.max(1.0F, detailWidth));
            }

            Fonts.sf_regular
               .draw(
                  healthLabel,
                  contentX,
                  y + HP_Y,
                  detailSize,
                  circle ? ColorUtil.replAlpha(ColorUtil.getColor(190, 197, 210, 255), alpha) : ColorUtil.getColor(255, alpha)
               );
            float hpX = contentX + Fonts.sf_regular.getWidth(healthLabel, detailSize);
            Fonts.sf_regular.draw(hpText, hpX, y + HP_Y, detailSize, hpColor);
            if (!absorptionText.isEmpty()) {
               float absorptionX = circle
                  ? contentX + contentWidth - Fonts.sf_regular.getWidth(absorptionText, detailSize)
                  : hpX + Fonts.sf_regular.getWidth(hpText, detailSize);
               Fonts.sf_regular.draw(absorptionText, absorptionX, y + HP_Y, detailSize, goldColor);
            }

            if (circle) {
               float numeralSize = Math.min(8.5F * S, 8.5F * S * 16.0F * S / Math.max(1.0F, Fonts.sf_regular.getWidth(hpText, 8.5F * S)));
               Fonts.sf_regular
                  .drawCentered(hpText, ringX, ringY - Fonts.sf_regular.getHeight(numeralSize) / 2.0F, numeralSize, ColorUtil.getColor(255, alpha));
            }

            Draw.flush();
            this.renderArmor(
               eventDisplay, this.target, circle ? contentX + ITEM_SIZE / 2.0F : x + ARMOR_X, circle ? y + 27.0F * S : y - ARMOR_Y - addALL, alpha, !circle
            );
            if (this.target instanceof class_1657 targetPlayer && interFace.targetHudBuffs.getValue()) {
               this.renderTargetBuffs(
                  eventDisplay,
                  targetPlayer,
                  circle ? contentX + ITEM_SIZE / 2.0F : x + ARMOR_X + 60.0F * S,
                  circle ? y + 36.0F * S : y - ARMOR_Y - addALL,
                  alpha,
                  circle
               );
            }

            Draw.flush();
            eventDisplay.getDrawContext().method_71048();
         } else {
            drag.active = false;
         }
      }
   }

   private void renderTargetBuffs(EventDisplay eventDisplay, class_1657 player, float buffX, float buffY, float alpha, boolean stackDown) {
      Collection<class_1293> currentEffects = player.method_6026();
      if (currentEffects != null && !currentEffects.isEmpty()) {
         List<CustomBuffs.ActiveBuff> buffs = CustomBuffs.detectActiveBuffs(currentEffects);
         if (!buffs.isEmpty()) {
            float bSize = ITEM_SIZE;
            float rowH = bSize + 2.0F * S;
            float rowTextSize = 4.5F * S;
            float rowY = stackDown ? buffY : buffY - (float)(buffs.size() - 1) * rowH;

            for (CustomBuffs.ActiveBuff ab : buffs) {
               class_1799 icon = ab.getBuff().createIconStack();
               ItemRender.drawItemWithContext(eventDisplay.getDrawContext(), icon, buffX - bSize / 2.0F, rowY - bSize / 2.0F, bSize / 16.0F, alpha);
               String dur = ab.getDurationString();
               Fonts.sf_regular
                  .draw(dur, buffX + bSize / 2.0F + 1.0F * S, rowY - Fonts.sf_regular.getHeight(rowTextSize) / 2.0F, rowTextSize, ab.getBuff().getColor());
               rowY += rowH;
            }
         }
      }
   }

   private void renderArmor(EventDisplay eventDisplay, class_1309 entity, float armorX, float armorY, float alpha, boolean background) {
      this.armorItems.clear();

      for (class_1304 slot : ARMOR_SLOTS) {
         class_1799 stack = entity.method_6118(slot);
         if (!stack.method_7960()) {
            this.armorItems.add(stack);
         }
      }

      if (!this.armorItems.isEmpty()) {
         float bgX = armorX - ITEM_SIZE / 2.0F - ARMOR_PAD;
         float bgY = armorY - ITEM_SIZE / 2.0F - ARMOR_PAD + 0.5F * S;
         float bgW = (float)this.armorItems.size() * ITEM_SIZE + ARMOR_PAD * 2.0F;
         float bgH = ITEM_SIZE + ARMOR_PAD * 2.0F;
         if (background) {
            Draw.glow(bgX, bgY, bgW, bgH - 0.5F * S, ColorUtil.getColor(0, 0.1F * alpha), 4.0F * S, GLOW_RADIUS, 1.0F);
            RenderUtil.Blur.glass(
               bgX,
               bgY,
               bgW,
               bgH,
               alpha,
               4.0F * S,
               ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue() * alpha),
               BLUR_RADIUS,
               1.0F,
               1.0F,
               4.0F
            );
         }

         Draw.flush();

         for (class_1799 stack : this.armorItems) {
            if (background) {
               ItemRender.drawItemWithContext(
                  eventDisplay.getDrawContext(), stack, armorX - ITEM_SIZE / 2.0F, armorY - ITEM_SIZE / 2.0F, ITEM_SIZE / 16.0F, alpha
               );
            } else {
               this.drawCircleItem(eventDisplay, stack, armorX, armorY, ITEM_SIZE, alpha);
            }

            armorX += ITEM_SIZE + (background ? 0.0F : S);
         }

         this.armorItems.clear();
      }
   }

   private void drawCircleItem(EventDisplay eventDisplay, class_1799 stack, float centerX, float centerY, float size, float alpha) {
      if (!ItemRender.needsContextRender(stack)) {
         ItemRender.drawItem(stack, centerX - size / 2.0F, centerY - size / 2.0F, size / 16.0F, alpha);
      } else {
         float scaleFix = 2.0F / Math.max(1.0F, (float)mc.method_22683().method_4495());
         Draw.flush();
         Matrix3x2fStack matrices = eventDisplay.getDrawContext().method_51448();
         matrices.pushMatrix();

         try {
            matrices.translate(centerX * scaleFix, centerY * scaleFix);
            ItemRender.drawItemWithContext(eventDisplay.getDrawContext(), stack, -size / 2.0F, -size / 2.0F, size / 16.0F, alpha);
         } finally {
            matrices.popMatrix();
         }
      }
   }

   private void drawMobIcon(EventDisplay eventDisplay, class_1309 entity, float centerX, float centerY, float size, float alpha, boolean circle) {
      try {
         class_1826 egg = class_1826.method_8019(entity.method_5864());
         if (egg == null) {
            this.drawFace(entity, eventDisplay.getPartialTicks(), centerX - size / 2.0F, centerY - size / 2.0F, alpha);
            return;
         }

         class_1799 stack = new class_1799(egg);
         if (circle) {
            this.drawCircleItem(eventDisplay, stack, centerX, centerY, size, alpha);
         } else {
            ItemRender.drawItemWithContext(eventDisplay.getDrawContext(), stack, centerX - size / 2.0F, centerY - size / 2.0F, size / 16.0F, alpha);
         }
      } catch (Exception var10) {
      }
   }

   private int getHealthColor(class_1309 entity) {
      float pct = class_3532.method_15363(this.getHealth(entity) / Math.max(1.0F, entity.method_6063()), 0.0F, 1.0F);
      int red = ColorUtil.getColor(255, 75, 75, 255);
      int yellow = ColorUtil.getColor(255, 205, 85, 255);
      int green = ColorUtil.getColor(95, 220, 115, 255);
      return pct < 0.5F ? ColorUtil.overCol(red, yellow, pct * 2.0F) : ColorUtil.overCol(yellow, green, (pct - 0.5F) * 2.0F);
   }

   private float getHealth(class_1309 entity) {
      float hp = entity.method_6032();
      if (entity instanceof class_1657 && mc.field_1687 != null) {
         class_266 objective = mc.field_1687.method_8428().method_1189(class_8646.field_45158);
         if (objective != null) {
            class_9013 score = mc.field_1687.method_8428().method_55430(entity, objective);
            if (score != null) {
               class_5250 text = class_9013.method_55398(score, objective.method_55380(class_9025.field_47566));

               try {
                  float scoreboardHp = Float.parseFloat(ColorUtil.removeFormatting(text.getString()));
                  if (Float.isFinite(scoreboardHp) && scoreboardHp >= 0.0F) {
                     hp = scoreboardHp;
                  }
               } catch (NumberFormatException var7) {
               }
            }
         }
      }

      return class_3532.method_15363(hp, 0.0F, Math.max(0.0F, entity.method_6063()));
   }

   private void drawFace(class_1309 lastTarget, float lastTickDelta, float x, float y, float alpha) {
      try {
         if (!(mc.method_1561().method_3953(lastTarget) instanceof class_922 renderer)) {
            return;
         }

         class_10042 state = (class_10042)renderer.method_62425(lastTarget, lastTickDelta);
         class_2960 textureLocation = renderer.method_3885(state);
         float hurtPercent = class_3532.method_15363((float)lastTarget.field_6235 / 10.0F, 0.0F, 1.0F);
         int r = 255;
         int g = (int)(255.0F * (1.0F - hurtPercent));
         int b = (int)(255.0F * (1.0F - hurtPercent));
         int color = new Color(r, g, b, (int)(255.0F * alpha)).getRGB();
         float texW = 64.0F;
         float texH = 64.0F;

         try {
            class_1044 texture = mc.method_1531().method_4619(textureLocation);
            GpuTexture gpu;
            if (texture != null && (gpu = texture.method_68004()) != null) {
               texW = (float)gpu.getWidth(0);
               texH = (float)gpu.getHeight(0);
            }
         } catch (Exception var19) {
         }

         if (texW <= 0.0F) {
            texW = 64.0F;
         }

         if (texH <= 0.0F) {
            texH = 64.0F;
         }

         RenderUtil.Images.texture(textureLocation, x, y, FACE_SIZE, FACE_SIZE, 8.0F / texW, 8.0F / texH, 16.0F / texW, 16.0F / texH, color, 0.0F, 6.0F);
      } catch (Exception var20) {
      }
   }

   private boolean inRect(float mouseX, float mouseY, float x, float y, float width, float height) {
      return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
   }
}
