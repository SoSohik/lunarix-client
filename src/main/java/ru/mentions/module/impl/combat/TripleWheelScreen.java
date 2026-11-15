package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
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
import ru.mentions.utils.render.DrawBatcher;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class TripleWheelScreen extends class_437 {
   public static final class_2960 LOGO = class_2960.method_60655("client", "textures/logo.png");
   private final AutoSwap module;
   public float OUTER_R = 115.0F;
   public float INNER_R = 58.0F;
   public double GAP = Math.toRadians(2.0);
   public double STEP = Math.toRadians(0.5);
   private static final double[] CENTERS = new double[]{-Math.PI / 2, Math.PI / 6, Math.PI * 5.0 / 6.0};
   private final Animation openAnim = new Animation();
   private float scaleFix = 1.0F;
   private boolean exit = false;
   private int hoveredSlot = -1;
   private long openTime;
   private boolean selectionAnimating;
   private boolean closingAfterSelection;
   private int selectionSlot = -1;
   private Runnable closeAction;
   private final Animation selectionAnimation = new Animation();
   private final float[] hoverProgress = new float[3];

   public TripleWheelScreen(AutoSwap module) {
      super(class_2561.method_43470("Auto Swap"));
      this.module = module;
   }

   protected void method_25426() {
      this.exit = false;
      this.openTime = System.currentTimeMillis();
      this.selectionAnimating = false;
      this.closingAfterSelection = false;
      this.selectionSlot = -1;
      this.closeAction = null;
      this.selectionAnimation.set(0.0);
      this.openAnim.set(0.0);
      this.openAnim.run(1.0, 0.22, Easings.QUAD_OUT);

      for (int i = 0; i < 3; i++) {
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

   public int getHoveredSlot() {
      return this.hoveredSlot;
   }

   public boolean isSelectionAnimating() {
      return this.selectionAnimating || this.closingAfterSelection || this.exit;
   }

   public void startSelectionAnimation(int slot, Runnable action) {
      if (!this.selectionAnimating && !this.closingAfterSelection && !this.exit) {
         this.selectionAnimating = true;
         this.selectionSlot = slot;
         this.closeAction = action;
         this.selectionAnimation.set(0.0);
         this.selectionAnimation.run(1.0, 0.28, Easings.QUAD_OUT);
      }
   }

   public void startCloseAnimation(Runnable action) {
      if (!this.closingAfterSelection && !this.exit) {
         this.closeAction = action;
         this.closingAfterSelection = true;
         this.openAnim.run(0.0, 0.2, Easings.QUAD_IN);
      }
   }

   private int computeHoveredSlot(double mx, double my) {
      double dx = mx - (double)this.cx();
      double dy = my - (double)this.cy();
      if (Math.hypot(dx, dy) < 4.0 * (double)Math.max(1.0F, this.scaleFix)) {
         return -1;
      } else {
         double angle = Math.atan2(dy, dx);
         double half = Math.PI / 3;

         for (int i = 0; i < 3; i++) {
            double diff = angle - CENTERS[i];

            while (diff > Math.PI) {
               diff -= Math.PI * 2;
            }

            while (diff < -Math.PI) {
               diff += Math.PI * 2;
            }

            if (Math.abs(diff) <= half) {
               return i;
            }
         }

         return -1;
      }
   }

   private boolean isInHand(int i) {
      if (IMinecraft.mc.field_1724 == null) {
         return false;
      } else {
         class_1799 cfg = this.module.tripleSlotItems[i];
         if (cfg != null && !cfg.method_7960()) {
            class_1799 offhand = IMinecraft.mc.field_1724.method_6079();
            return AutoSwap.sameStackIdentity(offhand, cfg);
         } else {
            return false;
         }
      }
   }

   private void drawSector(class_332 ctx, float cx, float cy, float innerR, float outerR, double startAngle, double endAngle, int color) {
      Matrix3x2fStack ms = ctx.method_51448();
      double range = endAngle - startAngle;
      int steps = Math.max(1, (int)Math.ceil(range / this.STEP));
      double actualStep = range / (double)steps;
      int hw = Math.max(1, (int)((double)outerR * Math.sin(actualStep / 2.0) + 1.2));

      for (int i = 0; i < steps; i++) {
         double mid = startAngle + ((double)i + 0.5) * actualStep;
         ms.pushMatrix();
         ms.translate(cx, cy);
         ms.rotate((float)mid);
         ctx.method_25294((int)innerR, -hw, (int)outerR + 1, hw + 1, color);
         ms.popMatrix();
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      float sf = 2.0F / (float)IMinecraft.mc.method_22683().method_4495();
      if (sf <= 0.0F) {
         sf = 1.0F;
      }

      this.scaleFix = sf;
      this.openAnim.update();
      float anim = this.openAnim.get();
      this.hoveredSlot = this.computeHoveredSlot((double)mouseX, (double)mouseY);
      if ((this.exit || this.closingAfterSelection) && this.openAnim.isFinished()) {
         Runnable action = this.closeAction;
         this.closeAction = null;
         this.exit = false;
         this.closingAfterSelection = false;
         IMinecraft.mc.method_1507(null);
         if (action != null) {
            action.run();
         }
      } else {
         if (this.selectionAnimating) {
            this.selectionAnimation.update();
            if (this.selectionAnimation.isFinished()) {
               this.selectionAnimating = false;
               this.closingAfterSelection = true;
               this.openAnim.run(0.0, 0.18, Easings.QUAD_IN);
            }
         }

         float sw = (float)this.field_22789;
         float sh = (float)this.field_22790;
         float cx = this.cx();
         float cy = this.cy();
         float rScreenW = sw / sf;
         float rScreenH = sh / sf;
         float rCx = cx / sf;
         float rCy = cy / sf;
         ScreenBlur.capture(2);
         RenderUtil.Blur.blur(0.0F, 0.0F, sw, sh, anim, ColorUtil.getColor(0, anim * 0.45F));
         Client.get().render2D().getBlurPipeline().flush();
         float baseInner = this.INNER_R * this.scaleFix;
         float baseOuter = this.OUTER_R * this.scaleFix;
         float centerR = (baseInner - 4.0F) * anim;
         float centerBox = centerR * 2.0F;
         float rCenterR = centerR / sf;
         float rCenterBox = centerBox / sf;
         Render2D.beginOverlay();
         RenderUtil.Render2D.glow(
            rCx - rCenterR, rCy - rCenterR, rCenterBox, rCenterBox, ColorUtil.replAlpha(ColorUtil.client(), 0.35F * anim), rCenterR, 12.0F, 1.0F
         );
         RenderUtil.Render2D.rect(rCx - rCenterR, rCy - rCenterR, rCenterBox, rCenterBox, ColorUtil.getColor(16, 18, 24, (int)(0.9F * anim * 255.0F)), rCenterR);
         RenderUtil.Render2D.outline(
            rCx - rCenterR, rCy - rCenterR, rCenterBox, rCenterBox, 1.0F, ColorUtil.replAlpha(ColorUtil.client(), 0.65F * anim), rCenterR
         );
         if (this.hoveredSlot >= 0 && this.module.tripleSlotItems[this.hoveredSlot] != null && !this.module.tripleSlotItems[this.hoveredSlot].method_7960()) {
            class_1799 hovItem = this.module.tripleSlotItems[this.hoveredSlot];
            String name = hovItem.method_7964().getString();
            Fonts.sf_bold.drawCentered(name, rCx, rCy - 4.0F, 9.0F, ColorUtil.getColor(255, anim));
            String sub = this.isInHand(this.hoveredSlot) ? "В руці" : "Слот " + (this.hoveredSlot + 1);
            Fonts.sf_medium.drawCentered(sub, rCx, rCy + 6.0F, 7.0F, ColorUtil.replAlpha(ColorUtil.client(), anim * 0.85F));
         } else {
            float logoDrawSize = 22.0F * anim;
            RenderUtil.Images.texture(LOGO, rCx - logoDrawSize / 2.0F, rCy - logoDrawSize / 2.0F, logoDrawSize, logoDrawSize, ColorUtil.getColor(255, anim));
         }

         DrawBatcher.flushPending();
         Client.get().render2D().getRectPipeline().flush();
         Client.get().render2D().getOutlinePipeline().flush();
         Render2D.endOverlay();

         for (int i = 0; i < 3; i++) {
            boolean hov = this.hoveredSlot == i && !this.closingAfterSelection;
            this.hoverProgress[i] = class_3532.method_16439(0.18F, this.hoverProgress[i], hov ? 1.0F : 0.0F);
         }

         double slice = Math.PI * 2.0 / 3.0;
         double halfSlice = slice / 2.0 - this.GAP;

         for (int i = 0; i < 3; i++) {
            double centerAngle = CENTERS[i];
            double startAngle = centerAngle - halfSlice;
            double endAngle = centerAngle + halfSlice;
            float hp = this.hoverProgress[i];
            float innerR = (baseInner - hp * 2.5F) * anim;
            float outerR = (baseOuter + hp * 6.5F) * anim;
            float midR = (innerR + outerR) / 2.0F;
            float shift = hp * 4.0F;
            float sectorCx = cx + (float)(Math.cos(centerAngle) * (double)shift);
            float sectorCy = cy + (float)(Math.sin(centerAngle) * (double)shift);
            class_1799 slotCfg = this.module.tripleSlotItems[i];
            int slotState = slotCfg == null || slotCfg.method_7960() ? 2 : this.module.tripleSlotState(i);
            int baseCol = slotState == 0
               ? ColorUtil.getColor(180, 50, 50, 0.35F * anim)
               : (slotState == 1 ? ColorUtil.replAlpha(ColorUtil.client(), 0.45F * anim) : ColorUtil.getColor(20, 22, 28, (int)(0.78F * anim * 255.0F)));
            int hoverCol = ColorUtil.overCol(
               ColorUtil.replAlpha(ColorUtil.client(), 0.7F * anim), ColorUtil.getColor(255, 255, 255, (int)(0.25F * anim * 255.0F)), 0.25F
            );
            int sectorCol = ColorUtil.overCol(baseCol, hoverCol, hp);
            this.drawSector(context, sectorCx, sectorCy, innerR, outerR, startAngle, endAngle, sectorCol);
            class_1799 item = this.module.tripleSlotItems[i];
            float iconX = sectorCx + (float)((double)midR * Math.cos(centerAngle));
            float iconY = sectorCy + (float)((double)midR * Math.sin(centerAngle));
            if (item != null && !item.method_7960()) {
               float iconScale = (0.9F + hp * 0.12F) * anim;
               ItemRender.drawItemCenteredWithContext(context, item, iconX, iconY, iconScale, anim);
            } else {
               Render2D.beginOverlay();
               Fonts.sf_bold.drawCentered("+", iconX / sf, iconY / sf - 5.0F, 13.0F, ColorUtil.getColor(140, 150, 170, (int)(0.5F * anim * 255.0F)));
               Render2D.endOverlay();
            }
         }

         class_1799 offhand = IMinecraft.mc.field_1724 != null ? IMinecraft.mc.field_1724.method_6079() : null;
         boolean hasOffhand = offhand != null && !offhand.method_7960();
         String offText = "В руці: " + (hasOffhand ? offhand.method_7964().getString() : "порожньо");
         float offY = rCy + baseOuter / sf + 6.0F;
         float offTextW = Fonts.sf_medium.getWidth(offText, 8.0F);
         float offTotalW = offTextW + (hasOffhand ? 18.0F : 0.0F);
         float offStartX = rCx - offTotalW / 2.0F;
         if (hasOffhand) {
            ItemRender.drawItemCenteredWithContext(context, offhand, (offStartX + 7.0F) * sf, (offY + 4.0F) * sf, 0.75F, anim);
         }

         Render2D.beginOverlay();
         Fonts.sf_medium
            .draw(offText, offStartX + (hasOffhand ? 18.0F : 0.0F), offY + 1.0F, 8.0F, ColorUtil.getColor(230, 235, 245, (int)(0.85F * anim * 255.0F)));
         Render2D.endOverlay();
         Render2D.beginOverlay();
         String hint = this.hoveredSlot >= 0
            ? "Відпусти клавішу — свапнути  |  ПКМ — змінити предмет"
            : "Веди курсор у будь-який бік — свапне на цю сторону  |  ПКМ — призначити";
         Fonts.sf_medium.drawCentered(hint, rCx, rCy + baseOuter / sf + 30.0F, 8.5F, ColorUtil.getColor(240, 245, 255, (int)(0.75F * anim * 255.0F)));
         Render2D.endOverlay();
         super.method_25394(context, mouseX, mouseY, delta);
      }
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      int slot;
      if (click.method_74245() == 1 && (slot = this.computeHoveredSlot(click.comp_4798(), click.comp_4799())) >= 0) {
         IMinecraft.mc.method_1507(new TriplePickerScreen(this.module, slot, this));
         return true;
      } else {
         return super.method_25402(click, doubled);
      }
   }

   public boolean method_25422() {
      if (!this.exit) {
         this.openAnim.run(0.0, 0.2, Easings.QUAD_IN);
         this.exit = true;
      }

      return false;
   }

   public boolean method_25421() {
      return false;
   }
}
