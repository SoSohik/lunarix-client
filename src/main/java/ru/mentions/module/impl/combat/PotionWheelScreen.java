package ru.mentions.module.impl.combat;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;
import ru.mentions.Client;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.render.DrawBatcher;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class PotionWheelScreen extends class_437 {
   private final PotionHelper module;
   private final List<PotionHelper.PotionEntry> potions;
   private final int activeKey;
   public double STEP = Math.toRadians(0.5);
   private float scaleFix = 1.0F;
   private int hoveredSlot = -1;
   private final Animation openAnim = new Animation();
   private final float[] hoverProgress;
   private boolean closing = false;

   public PotionWheelScreen(PotionHelper module, List<PotionHelper.PotionEntry> potions, int activeKey) {
      super(class_2561.method_43470("Potion Wheel"));
      this.module = module;
      this.potions = potions;
      this.activeKey = activeKey;
      this.hoverProgress = new float[Math.max(1, potions.size())];
   }

   protected void method_25426() {
      this.closing = false;
      this.openAnim.set(0.0);
      this.openAnim.run(1.0, 0.25, Easings.QUAD_OUT);

      for (int i = 0; i < this.hoverProgress.length; i++) {
         this.hoverProgress[i] = 0.0F;
      }
   }

   private void updateMovementKeys() {
      if (IMinecraft.mc.field_1724 != null && IMinecraft.mc.field_1690 != null) {
         class_304[] movementKeys = new class_304[]{
            IMinecraft.mc.field_1690.field_1894,
            IMinecraft.mc.field_1690.field_1881,
            IMinecraft.mc.field_1690.field_1913,
            IMinecraft.mc.field_1690.field_1849,
            IMinecraft.mc.field_1690.field_1903,
            IMinecraft.mc.field_1690.field_1832,
            IMinecraft.mc.field_1690.field_1867
         };

         for (class_304 keyBinding : movementKeys) {
            if (keyBinding != null) {
               int keyCode = keyBinding.method_1429().method_1444();
               boolean isKeyPressed = class_3675.method_15987(IMinecraft.mc.method_22683(), keyCode);
               keyBinding.method_23481(isKeyPressed);
            }
         }
      }
   }

   public void method_25419() {
      super.method_25419();
      this.updateMovementKeys();
   }

   private float cx() {
      return (float)this.field_22789 / 2.0F;
   }

   private float cy() {
      return (float)this.field_22790 / 2.0F;
   }

   private float getOuterRadius(int total) {
      return Math.max(120.0F, 105.0F + (float)total * 2.8F);
   }

   private float getInnerRadius(int total) {
      return Math.max(54.0F, 45.0F + (float)total * 1.3F);
   }

   private double getSectorCenter(int index, int total) {
      if (total <= 0) {
         return 0.0;
      } else {
         double slice = (Math.PI * 2) / (double)total;
         return (-Math.PI / 2) + (double)index * slice;
      }
   }

   private int computeHoveredSlot(double mx, double my) {
      int total = this.potions.size();
      if (total == 0) {
         return -1;
      } else {
         double dx = mx - (double)this.cx();
         double dy = my - (double)this.cy();
         if (Math.hypot(dx, dy) < 4.0) {
            return -1;
         } else if (total == 1) {
            return 0;
         } else {
            double angle = Math.atan2(dy, dx);
            double slice = (Math.PI * 2) / (double)total;
            double halfSlice = slice / 2.0;

            for (int i = 0; i < total; i++) {
               double center = this.getSectorCenter(i, total);
               double diff = angle - center;

               while (diff > Math.PI) {
                  diff -= Math.PI * 2;
               }

               while (diff < -Math.PI) {
                  diff += Math.PI * 2;
               }

               if (Math.abs(diff) <= halfSlice) {
                  return i;
               }
            }

            return -1;
         }
      }
   }

   private void drawSector(class_332 ctx, float cx, float cy, float innerR, float outerR, double startAngle, double endAngle, int color) {
      Matrix3x2fStack ms = ctx.method_51448();
      double range = endAngle - startAngle;
      if (!(range <= 0.0)) {
         int steps = Math.max(1, (int)Math.ceil(range / this.STEP));
         double actualStep = range / (double)steps;
         int hw = Math.max(1, (int)((double)outerR * Math.sin(actualStep / 2.0) + 1.5));

         for (int i = 0; i < steps; i++) {
            double mid = startAngle + ((double)i + 0.5) * actualStep;
            ms.pushMatrix();
            ms.translate(cx, cy);
            ms.rotate((float)mid);
            ctx.method_25294((int)innerR, -hw, (int)outerR + 1, hw + 1, color);
            ms.popMatrix();
         }
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.updateMovementKeys();
      this.openAnim.update();
      float anim = this.openAnim.get();
      if (this.closing) {
         if (this.openAnim.isFinished()) {
            IMinecraft.mc.method_1507(null);
         }
      } else {
         if (this.activeKey > 0) {
            boolean isDown = class_3675.method_15987(IMinecraft.mc.method_22683(), this.activeKey);
            if (!isDown) {
               if (this.hoveredSlot >= 0 && this.hoveredSlot < this.potions.size()) {
                  this.executeThrow(this.potions.get(this.hoveredSlot));
               } else {
                  this.close();
               }

               return;
            }
         }

         this.hoveredSlot = this.computeHoveredSlot((double)mouseX, (double)mouseY);
         int total = this.potions.size();
         if (total == 0) {
            this.close();
         } else {
            float cx = this.cx();
            float cy = this.cy();
            float baseOuter = this.getOuterRadius(total);
            float baseInner = this.getInnerRadius(total);
            float sf = 2.0F / (float)IMinecraft.mc.method_22683().method_4495();
            if (sf <= 0.0F) {
               sf = 1.0F;
            }

            this.scaleFix = sf;
            float rScreenW = (float)this.field_22789 / sf;
            float rScreenH = (float)this.field_22790 / sf;
            float rCx = cx / sf;
            float rCy = cy / sf;
            float sw = (float)this.field_22789;
            float sh = (float)this.field_22790;
            if (this.module.blur.getValue()) {
               ScreenBlur.capture(2);
               RenderUtil.Blur.blur(0.0F, 0.0F, sw, sh, anim, ColorUtil.getColor(0, anim * 0.45F));
               Client.get().render2D().getBlurPipeline().flush();
            } else {
               Render2D.beginOverlay();
               RenderUtil.Render2D.rect(0.0F, 0.0F, rScreenW, rScreenH, ColorUtil.getColor(0, 0.45F * anim));
               DrawBatcher.flushPending();
               Client.get().render2D().getRectPipeline().flush();
               Render2D.endOverlay();
            }

            float centerR = (baseInner - 4.0F) * anim;
            float centerBox = centerR * 2.0F;
            float rCenterR = centerR / sf;
            float rCenterBox = centerBox / sf;
            float rOuter = baseOuter / sf;
            Render2D.beginOverlay();
            RenderUtil.Render2D.glow(rCx - rCenterR, rCy - rCenterR, rCenterBox, rCenterBox, ColorUtil.getColor(0, 0.35F * anim), rCenterR, 10.0F, 1.0F);
            RenderUtil.Render2D.rect(
               rCx - rCenterR, rCy - rCenterR, rCenterBox, rCenterBox, ColorUtil.getColor(18, 19, 23, (int)(0.88F * anim * 255.0F)), rCenterR
            );
            RenderUtil.Render2D.outline(
               rCx - rCenterR, rCy - rCenterR, rCenterBox, rCenterBox, 1.0F, ColorUtil.replAlpha(ColorUtil.client(), 0.6F * anim), rCenterR
            );
            DrawBatcher.flushPending();
            Client.get().render2D().getRectPipeline().flush();
            Client.get().render2D().getOutlinePipeline().flush();
            Render2D.endOverlay();
            double slice = (Math.PI * 2) / (double)total;
            double gap = total > 1 ? Math.min(Math.toRadians(1.5), slice * 0.08) : 0.0;
            double halfSlice = slice / 2.0 - gap;

            for (int i = 0; i < total; i++) {
               boolean hov = this.hoveredSlot == i;
               this.hoverProgress[i] = class_3532.method_16439(0.18F, this.hoverProgress[i], hov ? 1.0F : 0.0F);
            }

            for (int i = 0; i < total; i++) {
               double centerAngle = this.getSectorCenter(i, total);
               double startAngle = centerAngle - halfSlice;
               double endAngle = centerAngle + halfSlice;
               float hp = this.hoverProgress[i];
               float innerR = (baseInner - hp * 2.0F) * anim;
               float outerR = (baseOuter + hp * 6.0F) * anim;
               float shift = hp * 4.0F;
               float sectorCx = cx + (float)(Math.cos(centerAngle) * (double)shift);
               float sectorCy = cy + (float)(Math.sin(centerAngle) * (double)shift);
               int baseCol = ColorUtil.getColor(24, 25, 30, (int)(0.75F * anim * 255.0F));
               int hoverCol = ColorUtil.overCol(
                  ColorUtil.replAlpha(ColorUtil.client(), 0.7F * anim), ColorUtil.getColor(255, 255, 255, (int)(0.25F * anim * 255.0F)), 0.2F
               );
               int sectorCol = ColorUtil.overCol(baseCol, hoverCol, hp);
               this.drawSector(context, sectorCx, sectorCy, innerR, outerR, startAngle, endAngle, sectorCol);
            }

            for (int i = 0; i < total; i++) {
               PotionHelper.PotionEntry entry = this.potions.get(i);
               double centerAngle = this.getSectorCenter(i, total);
               float hp = this.hoverProgress[i];
               float innerR = (baseInner - hp * 2.0F) * anim;
               float outerR = (baseOuter + hp * 6.0F) * anim;
               float midR = (innerR + outerR) / 2.0F;
               float shift = hp * 4.0F;
               float sectorCx = cx + (float)(Math.cos(centerAngle) * (double)shift);
               float sectorCy = cy + (float)(Math.sin(centerAngle) * (double)shift);
               float iconX = sectorCx + (float)((double)midR * Math.cos(centerAngle));
               float iconY = sectorCy + (float)((double)midR * Math.sin(centerAngle));
               float iconScale = (1.95F + hp * 0.25F) * anim;
               if (total > 8) {
                  iconScale *= Math.max(0.85F, 1.0F - (float)(total - 8) * 0.025F);
               }

               ItemRender.drawItemCenteredWithContext(context, entry.getSampleStack(), iconX, iconY, iconScale, anim);
            }

            Render2D.beginOverlay();

            for (int i = 0; i < total; i++) {
               PotionHelper.PotionEntry entry = this.potions.get(i);
               double centerAngle = this.getSectorCenter(i, total);
               float hp = this.hoverProgress[i];
               float innerR = (baseInner - hp * 2.0F) * anim;
               float outerR = (baseOuter + hp * 6.0F) * anim;
               float midR = (innerR + outerR) / 2.0F;
               float shift = hp * 4.0F;
               float sectorCx = cx + (float)(Math.cos(centerAngle) * (double)shift);
               float sectorCy = cy + (float)(Math.sin(centerAngle) * (double)shift);
               float iconX = sectorCx + (float)((double)midR * Math.cos(centerAngle));
               float iconY = sectorCy + (float)((double)midR * Math.sin(centerAngle));
               if (this.module.numberSelect.getValue() && i < 9) {
                  String numText = String.valueOf(i + 1);
                  float numW = 10.0F;
                  float numH = 10.0F;
                  float rNumX = (iconX - 18.0F * anim) / sf;
                  float rNumY = (iconY - 14.0F * anim) / sf;
                  int badgeBg = ColorUtil.getColor(14, 15, 18, (int)(0.92F * anim * 255.0F));
                  int badgeBorder = hp > 0.01F
                     ? ColorUtil.replAlpha(ColorUtil.client(), 0.95F * anim)
                     : ColorUtil.getColor(80, 85, 95, (int)(0.85F * anim * 255.0F));
                  int textColor = hp > 0.01F ? ColorUtil.client() : ColorUtil.getColor(255, 0.9F * anim);
                  RenderUtil.Render2D.rect(rNumX, rNumY, numW, numH, badgeBg, 3.0F);
                  RenderUtil.Render2D.outline(rNumX, rNumY, numW, numH, 0.8F, badgeBorder, 3.0F);
                  Fonts.sf_bold.drawCentered(numText, rNumX + numW / 2.0F, rNumY + 2.0F, 6.5F, textColor);
               }

               if (entry.getCount() > 1) {
                  String badgeText = entry.getCount() + "x";
                  float badgeTextW = Fonts.sf_bold.getWidth(badgeText, 6.0F);
                  float badgeW = badgeTextW + 5.0F;
                  float badgeH = 9.0F;
                  float rBadgeX = (iconX + 11.0F * anim) / sf;
                  float rBadgeY = (iconY + 5.0F * anim) / sf;
                  RenderUtil.Render2D.rect(rBadgeX, rBadgeY, badgeW, badgeH, ColorUtil.getColor(14, 15, 18, (int)(0.92F * anim * 255.0F)), 3.0F);
                  RenderUtil.Render2D.outline(rBadgeX, rBadgeY, badgeW, badgeH, 0.8F, ColorUtil.replAlpha(ColorUtil.client(), 0.75F * anim), 3.0F);
                  Fonts.sf_bold.drawCentered(badgeText, rBadgeX + badgeW / 2.0F, rBadgeY + 1.8F, 6.0F, ColorUtil.getColor(255, anim));
               }
            }

            if (this.hoveredSlot >= 0 && this.hoveredSlot < total) {
               PotionHelper.PotionEntry hovered = this.potions.get(this.hoveredSlot);
               String pName = hovered.getName();
               if (hovered.getCount() > 1) {
                  pName = pName + " (" + hovered.getCount() + "x)";
               }

               float maxNameW = rCenterBox * 0.85F;
               float nameW = Fonts.sf_bold.getWidth(pName, 7.5F);
               if (nameW > maxNameW) {
                  while (pName.length() > 3 && Fonts.sf_bold.getWidth(pName + "...", 7.0F) > maxNameW) {
                     pName = pName.substring(0, pName.length() - 1);
                  }

                  pName = pName + "...";
               }

               Fonts.sf_bold.drawCentered(pName, rCx, rCy - 8.0F, 7.5F, ColorUtil.client());
               String slotInfo;
               if (hovered.isFromBundle()) {
                  slotInfo = hovered.getCount() > 1 ? "В мішечку (" + hovered.getCount() + " шт.)" : "В мішечку";
               } else if (hovered.getCount() > 1) {
                  slotInfo = "Всього: " + hovered.getCount() + " шт.";
               } else {
                  slotInfo = hovered.getSlot() == 40 ? "Ліва рука" : (hovered.getSlot() < 9 ? "Хотбар (" + (hovered.getSlot() + 1) + ")" : "Інвентар");
               }

               Fonts.sf_medium.drawCentered(slotInfo, rCx, rCy + 2.0F, 6.0F, ColorUtil.getColor(200, 0.95F * anim));
               if (this.module.numberSelect.getValue() && this.hoveredSlot < 9) {
                  String keyHint = "Клавіша [" + (this.hoveredSlot + 1) + "]";
                  Fonts.sf_bold.drawCentered(keyHint, rCx, rCy + 11.0F, 6.0F, ColorUtil.client());
               }
            } else {
               int totalCount = 0;

               for (PotionHelper.PotionEntry pe : this.potions) {
                  totalCount += pe.getCount();
               }

               Fonts.sf_bold.drawCentered("Зілля", rCx, rCy - 5.0F, 8.5F, ColorUtil.getColor(255, 0.95F * anim));
               Fonts.sf_medium.drawCentered(totalCount + " шт.", rCx, rCy + 6.0F, 6.5F, ColorUtil.getColor(190, 0.85F * anim));
            }

            String hint;
            if (this.module.numberSelect.getValue()) {
               hint = this.hoveredSlot >= 0
                  ? "Натисніть [" + (this.hoveredSlot + 1) + "], ЛКМ або відпустіть клавішу для кидка"
                  : "Натисніть [1-" + Math.min(9, total) + "], наведіть або виберіть ЛКМ";
            } else {
               hint = this.hoveredSlot >= 0 ? "Відпустіть клавішу або ЛКМ для кидка" : "Ведіть курсор у бік зілля — напрямок вибирає слот";
            }

            Fonts.sf_regular.drawCentered(hint, rCx, rCy + rOuter + 18.0F, 7.5F, ColorUtil.getColor(255, 0.85F * anim));
            Client.get().render2D().flushAll();
            Render2D.endOverlay();
         }
      }
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   private void executeThrow(PotionHelper.PotionEntry entry) {
      if (entry == null) {
         this.close();
      } else {
         if (IMinecraft.mc.field_1755 == this) {
            IMinecraft.mc.method_1507(null);
         }

         this.module.throwPotion(entry);
         if (this.module.notify.getValue()) {
            NotificationManager.sendWithSound("Брошено: " + entry.getName(), NotificationManager.Type.INFO, this.module.soundMode.getValue());
         }

         this.close();
      }
   }

   private void close() {
      this.closing = true;
      this.openAnim.run(0.0, 0.18, Easings.QUAD_IN);
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (this.module.numberSelect.getValue()) {
         int targetIdx = -1;
         if (key >= 49 && key <= 57) {
            targetIdx = key - 49;
         } else if (key >= 321 && key <= 329) {
            targetIdx = key - 321;
         }

         if (targetIdx >= 0 && targetIdx < this.potions.size()) {
            this.executeThrow(this.potions.get(targetIdx));
            return true;
         }
      }

      return super.method_25404(input);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (click.method_74245() == 0 && this.hoveredSlot >= 0 && this.hoveredSlot < this.potions.size()) {
         this.executeThrow(this.potions.get(this.hoveredSlot));
         return true;
      } else if (click.method_74245() == 1) {
         this.close();
         return true;
      } else {
         return super.method_25402(click, doubled);
      }
   }

   public boolean method_25422() {
      this.close();
      return false;
   }

   public boolean method_25421() {
      return false;
   }
}
