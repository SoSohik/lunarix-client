package ru.mentions.module.impl.display.interfaceimpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_408;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import org.joml.Matrix3x2fStack;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorFormatting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class Notify implements element {
   private final Animation notifyGhostAnim = new Animation();
   private final Set<String> notifiedEffectExpiry = new HashSet<>();
   private final Map<String, Boolean> prevModuleStates = new HashMap<>();
   private boolean moduleStatesInitialized = false;
   private final Set<class_1304> armorNotified = new HashSet<>();
   private static float S = 1.0F;
   private static float FONT_GHOST = 5.5F * S;
   private static float FONT_MAIN = 7.0F * S;
   private static float RADIUS = 6.5F * S;
   private static float ITEM_H = 22.0F * S;
   private static float STEP_Y = 27.0F * S;
   private static float GHOST_W = 90.0F * S;
   private static float GHOST_H = 16.0F * S;
   private static float STACK_BASE_W = 38.0F * S;
   private static float ITEM_BASE_W = 38.0F * S;

   public void onTick(BooleanSetting notifModules, BooleanSetting notifArmor, BooleanSetting notifEffects) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         Collection<Module> allMods = Client.get().moduleManager().values();
         if (!this.moduleStatesInitialized) {
            for (Module module : allMods) {
               this.prevModuleStates.put(module.getName(), module.isEnabled());
            }

            this.moduleStatesInitialized = true;
         } else if (notifModules.getValue()) {
            for (Module module : allMods) {
               boolean curr = module.isEnabled();
               boolean prev = this.prevModuleStates.getOrDefault(module.getName(), false);
               if (prev != curr) {
                  NotificationManager.send(
                     "Функция " + module.getBigName() + (curr ? " активирована" : " деактивирована"), NotificationManager.Type.MODULE, module.getBigName()
                  );
               }

               this.prevModuleStates.put(module.getName(), curr);
            }
         } else {
            for (Module module : allMods) {
               this.prevModuleStates.put(module.getName(), module.isEnabled());
            }
         }

         if (notifArmor.getValue()) {
            for (class_1304 slot : new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166}) {
               class_1799 stack = mc.field_1724.method_6118(slot);
               if (!stack.method_7960() && stack.method_7936() != 0) {
                  float dur = 1.0F - (float)stack.method_7919() / (float)stack.method_7936();
                  if (dur < 0.1F && !this.armorNotified.contains(slot)) {
                     this.armorNotified.add(slot);

                     String slotName = switch (slot) {
                        case field_6169 -> "Шлем";
                        case field_6174 -> "Нагрудник";
                        case field_6172 -> "Поножи";
                        case field_6166 -> "Ботинки";
                        default -> slot.method_5923();
                     };
                     String armorSnd = InterFace.getInstance() != null && InterFace.getInstance().armorSound != null
                        ? InterFace.getInstance().armorSound.getValue()
                        : "Trevoga";
                     NotificationManager.send(slotName + " почти сломан!", NotificationManager.Type.WARNING, 5000L, armorSnd);
                  } else if (dur >= 0.1F) {
                     this.armorNotified.remove(slot);
                  }
               } else {
                  this.armorNotified.remove(slot);
               }
            }
         }

         if (notifEffects.getValue()) {
            for (class_1293 class_12932 : mc.field_1724.method_6026()) {
               String name = ((class_1291)class_12932.method_5579().comp_349()).method_5560().getString();
               int ticks = class_12932.method_5584();
               if (ticks > 0) {
                  if (ticks > 200) {
                     this.notifiedEffectExpiry.remove(name);
                  } else if (!this.notifiedEffectExpiry.contains(name)) {
                     this.notifiedEffectExpiry.add(name);
                     class_2960 tex = getEffectTexture(class_12932.method_5579());
                     String effSnd = InterFace.getInstance() != null && InterFace.getInstance().effectsSound != null
                        ? InterFace.getInstance().effectsSound.getValue()
                        : "DingDong";
                     NotificationManager.send(name + " заканчивается!", NotificationManager.Type.EFFECT, tex, 4000L, effSnd);
                  }
               }
            }
         }
      }
   }

   public void onRender(DragSetting drag, InterFace interFace, EventDisplay eventDisplay) {
      S = InterFace.getInstance().sizeHud.getValue();
      FONT_GHOST = 5.5F * S;
      FONT_MAIN = 7.0F * S;
      RADIUS = 6.5F * S;
      float hMul = interFace.notifyHeight.getValue();
      ITEM_H = 22.0F * S * hMul;
      STEP_Y = ITEM_H + 5.0F * S;
      GHOST_W = 90.0F * S;
      GHOST_H = 16.0F * S;
      STACK_BASE_W = 38.0F * S;
      ITEM_BASE_W = 38.0F * S;
      float targetScale = 2.0F;
      float currentScale = (float)mc.method_22683().method_4495();
      float scaleFix = targetScale / currentScale;
      int screenWidth = (int)((float)mc.method_22683().method_4486() / scaleFix);
      List<NotificationManager.Entry> list = NotificationManager.entries();
      synchronized (list) {
         List<NotificationManager.Entry> notifs = NotificationManager.entries();
         notifs.removeIf(e -> e.removing && e.anim.get() <= 0.01F);

         for (NotificationManager.Entry e2 : notifs) {
            e2.anim.update();
            if (e2.isExpired()) {
               e2.removing = true;
            }

            e2.anim.run(e2.removing ? 0.0 : 1.0, 0.18F, Easings.QUAD_OUT);
         }

         float stackW = GHOST_W;
         float stackH = 0.0F;

         for (NotificationManager.Entry e3 : notifs) {
            float a = e3.anim.get();
            if (!(a <= 0.01F)) {
               float tw = Fonts.sf_medium.getWidth(e3.text, FONT_MAIN);
               stackW = Math.max(stackW, STACK_BASE_W + tw);
               stackH += STEP_Y * a;
            }
         }

         if (stackH <= 0.0F) {
            stackH = ITEM_H;
         }

         drag.size.set(stackW, stackH);
         drag.targetPosition.x = (float)screenWidth / 2.0F - stackW / 2.0F;
         drag.position.x = (float)screenWidth / 2.0F - stackW / 2.0F;
         float ny = drag.position.y;
         boolean showGhost = mc.field_1755 instanceof class_408 && notifs.stream().noneMatch(e -> e.anim.get() > 0.01F);
         drag.active = showGhost || notifs.stream().anyMatch(e -> e.anim.get() > 0.01F);
         this.notifyGhostAnim.update();
         this.notifyGhostAnim.run(showGhost ? 1.0 : 0.0, 0.2, Easings.BACK_OUT, true);
         float ga = class_3532.method_15363(this.notifyGhostAnim.get(), 0.0F, 1.0F);
         if (ga > 0.01F) {
            float gw = GHOST_W * (0.6F + 0.4F * this.notifyGhostAnim.get());
            float gh = GHOST_H * (0.6F + 0.4F * this.notifyGhostAnim.get());
            float gx = (float)screenWidth / 2.0F - gw / 2.0F;
            float gy = ny + (GHOST_H - gh) / 2.0F;
            RenderUtil.Render2D.rect(gx, gy, gw, gh, ColorUtil.getColor(20, 0.45F * ga), RADIUS);
            Draw.outline(gx, gy, gw, gh, 0.85F * S, ThemeColor.getOutlineColor(ga * 0.35F), RADIUS);
            Fonts.sf_medium
               .draw(
                  "Уведомления",
                  (float)screenWidth / 2.0F - Fonts.sf_medium.getWidth("Уведомления", FONT_GHOST) / 2.0F,
                  ny + 5.0F * S,
                  FONT_GHOST,
                  ColorUtil.getColor(235, 0.85F * ga)
               );
         }

         for (NotificationManager.Entry e4 : notifs) {
            float a = e4.anim.get();
            if (!(a <= 0.01F)) {
               boolean isEnable = e4.text.contains("активирована") && !e4.text.contains("деактивирована");
               boolean isDisable = e4.text.contains("деактивирована");
               boolean isWarning = e4.type == NotificationManager.Type.WARNING || e4.text.contains("почти") || e4.text.contains("сломан");
               boolean isEffect = e4.type == NotificationManager.Type.EFFECT || e4.effectTexture != null;
               int accentColor;
               if (isEnable) {
                  accentColor = -13710223;
               } else if (isDisable) {
                  accentColor = -1618884;
               } else if (isWarning) {
                  accentColor = -812014;
               } else if (isEffect) {
                  accentColor = -6596170;
               } else {
                  accentColor = ThemeColor.getHudColor();
               }

               if (e4.itemStack != null && ItemRender.isPotionItem(e4.itemStack)) {
                  class_1844 potComp = (class_1844)e4.itemStack.method_58694(class_9334.field_49651);
                  if (potComp != null) {
                     int pc = potComp.method_8064();
                     if (pc != -1 && pc != 0) {
                        accentColor = pc | 0xFF000000;
                     }
                  }
               }

               float tw = Fonts.sf_medium.getWidth(e4.text, FONT_MAIN);
               float nw = Math.max(90.0F * S, ITEM_BASE_W + tw);
               float nx = (float)screenWidth / 2.0F - nw / 2.0F;
               float actualY = ny + (1.0F - a) * -8.0F * S;
               Draw.glow(nx, actualY, nw, ITEM_H, ColorUtil.replAlpha(accentColor, 0.4F * a), RADIUS, 14.0F, 1.0F);
               RenderUtil.Render2D.rect(
                  nx, actualY, nw, ITEM_H, ColorUtil.getColor(16, 18, 24, (0.88F + 0.1F * InterFace.getInstance().alphaHUD.getValue()) * a), RADIUS
               );
               RenderUtil.Blur.glass(nx, actualY, nw, ITEM_H, a, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), 0.35F * a), 16.0F, 1.0F, 1.0F, 3.5F);
               Draw.outline(nx, actualY, nw, ITEM_H, 0.85F * S, ColorUtil.replAlpha(accentColor, 0.48F * a), RADIUS);
               RenderUtil.Render2D.rect(nx + 3.0F * S, actualY + 4.5F * S, 2.5F * S, ITEM_H - 9.0F * S, ColorUtil.replAlpha(accentColor, 0.95F * a), 1.25F * S);
               float badgeSize = Math.min(14.0F * S, ITEM_H - 2.0F * S);
               float bx = nx + 8.5F * S;
               float by = actualY + (ITEM_H - badgeSize) / 2.0F;
               RenderUtil.Render2D.rect(bx, by, badgeSize, badgeSize, ColorUtil.replAlpha(accentColor, 0.2F * a), 4.0F * S);
               Draw.outline(bx, by, badgeSize, badgeSize, 0.6F * S, ColorUtil.replAlpha(accentColor, 0.38F * a), 4.0F * S);
               this.drawBadgeIcon(eventDisplay, e4, bx, by, badgeSize, a, scaleFix, accentColor, isEnable, isDisable, isWarning);
               float textX = bx + badgeSize + 6.0F * S;
               Client.get().render2D().flushAll();
               float textY = actualY + (ITEM_H - Fonts.sf_medium.getHeight(FONT_MAIN)) / 2.0F;
               Fonts.sf_medium.draw(formatText(e4, a, a), textX, textY, FONT_MAIN, ColorUtil.getColor(255, a));
               float progress = e4.getProgress();
               if (progress > 0.005F) {
                  RenderUtil.Render2D.rect(
                     nx + 8.0F * S, actualY + ITEM_H - 2.0F * S, (nw - 16.0F * S) * progress, 1.3F * S, ColorUtil.replAlpha(accentColor, 0.75F * a), 0.65F * S
                  );
               }

               ny += STEP_Y * a;
            }
         }
      }
   }

   private void drawBadgeIcon(
      EventDisplay eventDisplay,
      NotificationManager.Entry e,
      float bx,
      float by,
      float badgeSize,
      float a,
      float scaleFix,
      int accentColor,
      boolean isEnable,
      boolean isDisable,
      boolean isWarning
   ) {
      if (e.itemStack != null) {
         Matrix3x2fStack matrix = eventDisplay.getDrawContext().method_51448();
         matrix.pushMatrix();
         matrix.translate((bx + badgeSize / 2.0F) * scaleFix, (by + badgeSize / 2.0F) * scaleFix);
         matrix.scale(0.55F * S, 0.55F * S);
         ItemRender.drawItemWithContext(eventDisplay.getDrawContext(), e.itemStack, -8.0F, -8.0F, a, a);
         matrix.popMatrix();
      } else if (e.effectTexture != null) {
         Matrix3x2fStack matrix = eventDisplay.getDrawContext().method_51448();
         matrix.pushMatrix();
         matrix.translate((bx + 2.5F * S) * scaleFix, (by + 2.5F * S) * scaleFix);
         eventDisplay.getDrawContext()
            .method_52707(
               class_10799.field_56883,
               e.effectTexture,
               0,
               0,
               (int)((badgeSize - 5.0F * S) * scaleFix),
               (int)((badgeSize - 5.0F * S) * scaleFix),
               ColorUtil.getColor(255, a)
            );
         matrix.popMatrix();
      } else {
         float iconFont = 6.5F * S;
         if (isWarning) {
            Fonts.icon.drawCentered("r", bx + badgeSize / 2.0F, by + 3.2F * S, iconFont, ColorUtil.replAlpha(accentColor, a));
         } else if (isEnable) {
            Fonts.icon.drawCentered("2", bx + badgeSize / 2.0F, by + 3.0F * S, iconFont, ColorUtil.replAlpha(accentColor, a));
         } else if (isDisable) {
            Fonts.icon.drawCentered("1", bx + badgeSize / 2.0F, by + 3.0F * S, iconFont, ColorUtil.replAlpha(accentColor, a));
         } else {
            Fonts.sf_bold.drawCentered("i", bx + badgeSize / 2.0F, by + 2.4F * S, 7.0F * S, ColorUtil.replAlpha(accentColor, a));
         }
      }
   }

   private static String formatText(NotificationManager.Entry e, float a, float alpha) {
      return e.iconGlyph != null && !e.iconGlyph.isEmpty() && e.text.contains(e.iconGlyph)
         ? e.text.replace(e.iconGlyph, ColorFormatting.getColor(ThemeColor.getHudColor(a * alpha)) + e.iconGlyph + ColorFormatting.reset())
         : e.text;
   }

   private static float getAlpha() {
      return ThemeColor.getOpacity() < 0.98F ? ThemeColor.getOpacity() : 0.98F;
   }

   private static class_2960 getEffectTexture(class_6880<class_1291> effect) {
      return effect.method_40230()
         .<class_2960>map(class_5321::method_29177)
         .map(id -> id.method_45138("mob_effect/"))
         .orElse(class_2960.method_60656("mob_effect/speed"));
   }

   @Override
   public void onRender(DragSetting drag, InterFace interFace) {
   }
}
