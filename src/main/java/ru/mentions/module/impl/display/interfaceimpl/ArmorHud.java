package ru.mentions.module.impl.display.interfaceimpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class ArmorHud implements element {
   private static final class_310 mc = class_310.method_1551();
   private static final class_1304[] ARMOR_SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};

   @Override
   public void onRender(DragSetting dragSetting, InterFace interFace) {
   }

   public void onRender(DragSetting dragSetting, InterFace interFace, EventDisplay eventDisplay) {
      if (mc.field_1724 != null && eventDisplay != null) {
         float s = interFace.sizeHud.getValue() * 1.05F;
         float slotW = 20.0F * s;
         float slotH = 20.0F * s;
         float gap = 2.0F * s;
         float padding = 2.5F * s;
         float radius = 5.0F * s;
         float totalW = padding * 2.0F + slotW * 4.0F + gap * 3.0F;
         float totalH = padding * 2.0F + slotH;
         float x = dragSetting.position.x;
         float y = dragSetting.position.y;
         float hudAlpha = interFace.alphaHUD.getValue();
         Draw.glow(x, y, totalW, totalH - 0.5F, ColorUtil.getColor(0, 0.12F), radius, 8.0F, 1.0F);
         RenderUtil.Blur.blur(x, y, totalW, totalH, 1.0F, radius, ColorUtil.replAlpha(ColorUtil.background(), hudAlpha));
         Draw.outline(x, y, totalW, totalH, 0.75F, ThemeColor.getOutlineColor(0.08F), radius);
         Font font = Fonts.sf_medium;
         float textSize = 4.5F * s;
         class_332 context = eventDisplay.getDrawContext();
         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         float scaleFix = targetScale / currentScale;

         for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            class_1304 slot = ARMOR_SLOTS[i];
            class_1799 stack = mc.field_1724.method_6118(slot);
            float currentSlotX = x + padding + (float)i * (slotW + gap);
            float currentSlotY = y + padding;
            RenderUtil.Render2D.rect(currentSlotX, currentSlotY, slotW, slotH, ColorUtil.getColor(255, 0.035F), 3.0F * s);
            if (!stack.method_7960()) {
               float itemCenterX = currentSlotX + slotW / 2.0F;
               float itemCenterY = currentSlotY + 6.5F * s;
               Matrix3x2fStack matrices = context.method_51448();
               matrices.pushMatrix();
               matrices.translate(itemCenterX * scaleFix, itemCenterY * scaleFix);
               matrices.scale(0.52F * s, 0.52F * s);
               ItemRender.drawItemWithContext(context, stack, -8.0F, -8.0F, 1.0F, 1.0F);
               matrices.popMatrix();
               int maxDamage = stack.method_7936();
               int damage = stack.method_7919();
               float dur = maxDamage > 0 ? 1.0F - (float)damage / (float)maxDamage : 1.0F;
               dur = Math.max(0.0F, Math.min(1.0F, dur));
               int percent = Math.round(dur * 100.0F);
               String percentText = percent + "%";
               float textWidth = font.getWidth(percentText, textSize);
               float textX = currentSlotX + (slotW - textWidth) / 2.0F;
               float textY = currentSlotY + slotH - 5.5F * s;
               int textColor;
               if (maxDamage > 0 && percent < 100) {
                  int r = 255;
                  int g = (int)(45.0F + 180.0F * dur);
                  int b = (int)(45.0F + 75.0F * dur * dur);
                  float opacity = 0.72F + (1.0F - dur) * 0.28F;
                  textColor = ColorUtil.getColor(r, g, b, (int)(opacity * 255.0F));
               } else {
                  textColor = ColorUtil.getColor(230, 230, 230, (int)(190.0F * (0.4F + 0.6F * hudAlpha)));
               }

               font.draw(percentText, textX, textY, textSize, textColor);
            } else {
               String emptyText = "-";
               float tw = font.getWidth(emptyText, textSize);
               float tx = currentSlotX + (slotW - tw) / 2.0F;
               float ty = currentSlotY + (slotH - font.getHeight(textSize)) / 2.0F;
               font.draw(emptyText, tx, ty, textSize, ColorUtil.getColor(160, 160, 160, 90));
            }
         }

         dragSetting.size.set(totalW, totalH);
      }
   }
}
