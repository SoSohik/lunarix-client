package ru.mentions.module.impl.render;

import com.mojang.blaze3d.textures.GpuTexture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10042;
import net.minecraft.class_1044;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1826;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_266;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_8646;
import net.minecraft.class_9013;
import net.minecraft.class_9025;
import net.minecraft.class_922;
import org.joml.Vector4d;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.TextFactoryEvent;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.module.impl.misc.Optimization;
import ru.mentions.module.impl.player.WorldTracker;
import ru.mentions.module.impl.utils.NameProtect;
import ru.mentions.module.impl.utils.ReportHelper;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.potion.CustomBuffs;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Name Tag",
   category = Category.RENDER,
   desc = "Отображает ники, хп и броню сущностей"
)
@Environment(EnvType.CLIENT)
public class NameTag extends Module {
   public BooleanSetting player = new BooleanSetting(this, "Отображения игроков", true);
   public BooleanSetting prefix = new BooleanSetting(this, "Префикс", true).setVisible(() -> this.player.getValue());
   public BooleanSetting suffix = new BooleanSetting(this, "Суффикс", true).setVisible(() -> this.player.getValue());
   public BooleanSetting health = new BooleanSetting(this, "Здоровье", true).setVisible(() -> this.player.getValue());
   public BooleanSetting armor = new BooleanSetting(this, "Броня", true).setVisible(() -> this.player.getValue());
   public BooleanSetting handItems = new BooleanSetting(this, "Предметы в руках", true).setVisible(() -> this.player.getValue());
   public BooleanSetting donBuffs = new BooleanSetting(this, "Дон баффы", true).setVisible(() -> this.player.getValue());
   public BooleanSetting ignoreNaked = new BooleanSetting(this, "Игнорировать голых", false).setVisible(() -> this.player.getValue());
   public BooleanSetting mobs = new BooleanSetting(this, "Отображения мобов", true);
   public BooleanSetting clientBadge = new BooleanSetting(this, "Значок софта", true).setVisible(() -> false);
   private final List<class_1297> entities = new ArrayList<>();

   public static boolean isClientUser(class_1297 entity) {
      return Globals.isSoftUser(entity);
   }

   public static NameTag get() {
      return Instance.get(NameTag.class);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.entities.clear();
   }

   @EventHandler
   public void onTick(EventTick e) {
      this.entities.clear();
      if (mc.field_1687 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1657) {
               class_1657 p = (class_1657)entity;
               if (this.player.getValue()
                  && (p.method_5797() == null || !p.method_5797().getString().startsWith("Ghost_"))
                  && (!this.ignoreNaked.getValue() || hasArmor(p))) {
                  this.entities.add(entity);
               }
            } else if (entity instanceof class_1309 && this.mobs.getValue()) {
               this.entities.add(entity);
            }
         }
      }
   }

   @EventHandler(
      priority = 500
   )
   public void onTick(EventDisplay e) {
      class_332 context = e.getDrawContext();
      float tickDelta = e.getPartialTicks();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         boolean captured = false;
         double maxDistanceSquared = Optimization.nameTagRangeSquared();

         for (class_1297 entity : this.entities) {
            double dx = entity.method_23317() - mc.field_1724.method_23317();
            double dy = entity.method_23318() - mc.field_1724.method_23318();
            double dz = entity.method_23321() - mc.field_1724.method_23321();
            if (!(dx * dx + dy * dy + dz * dz > maxDistanceSquared)) {
               Vector4d vec4d = Projection.getVector4D(entity, tickDelta);
               if (!Projection.cantSee(vec4d) && !((float)mc.field_1773.method_19418().method_71156().method_1022(entity.method_5829().method_1005()) < 1.0F)) {
                  if (!captured) {
                     ScreenBlur.capture();
                     captured = true;
                  }

                  float x = (float)Projection.centerX(vec4d);
                  float y = (float)vec4d.y - 12.0F;
                  Object displayName = "";
                  float hpValue = -1.0F;
                  float hpMax = 20.0F;
                  boolean isFriend = false;
                  if (entity instanceof class_1657) {
                     class_1657 p = (class_1657)entity;
                     isFriend = Client.get().friendManager().isFriend(p.method_5477().getString());
                     String baseName = p.method_5477().getString();
                     String rawName = toColoredString(p.method_5476()).replace("⚡", "");
                     if (Client.get().moduleManager().get(NameProtect.class).isEnabled()
                        && Client.get().moduleManager().get(NameProtect.class).friends.getValue()
                        && Client.get().friendManager().isFriend(p.method_5820())) {
                        baseName = "Friend";
                        rawName = "Friend";
                     }

                     if (!this.prefix.getValue()) {
                        int idx = rawName.indexOf(baseName);
                        if (idx != -1) {
                           rawName = rawName.substring(idx);
                        } else {
                           rawName = baseName;
                        }
                     }

                     if (!this.suffix.getValue()) {
                        int idx = rawName.indexOf(baseName);
                        if (idx != -1) {
                           rawName = rawName.substring(0, idx + baseName.length());
                        }
                     }

                     String formatted = rawName.replace(mc.field_1724.method_5477().getString(), "lunarix");
                     displayName = formatted;
                     if (this.health.getValue()) {
                        hpValue = this.getHealth(p);
                        hpMax = Math.max(1.0F, p.method_6063() + p.method_6067());
                     }
                  } else if (entity instanceof class_1309) {
                     class_1309 le = (class_1309)entity;
                     displayName = le.method_5864().method_5897().getString();
                     if (this.health.getValue()) {
                        hpValue = this.getHealth(le);
                        hpMax = Math.max(1.0F, le.method_6063() + le.method_6067());
                     }
                  }

                  this.renderTag(context, entity, (String)displayName, x, y, isFriend, tickDelta, hpValue, hpMax);
                  if (entity instanceof class_1657) {
                     class_1657 handPlayer = (class_1657)entity;
                     if (this.player.getValue() && this.donBuffs.getValue()) {
                        this.renderDonBuffs(context, handPlayer, x, y - 3.0F);
                     }

                     if (this.player.getValue() && this.handItems.getValue()) {
                        this.renderHandItems(context, handPlayer, x, (float)vec4d.w);
                     }
                  }

                  if (entity instanceof class_1657) {
                     class_1657 tracked = (class_1657)entity;
                     if (this.player.getValue()) {
                        WorldTracker.get().render(context, tracked, x, y - 3.0F);
                     }
                  }
               }
            }
         }
      }
   }

   private void renderHandItems(class_332 context, class_1657 entity, float centerX, float feetY) {
      class_1799 mainHand = entity.method_6047();
      class_1799 offHand = entity.method_6079();
      ArrayList<class_1799> hands = new ArrayList<>();
      if (!mainHand.method_7960()) {
         hands.add(mainHand);
      }

      if (!offHand.method_7960()) {
         hands.add(offHand);
      }

      if (!hands.isEmpty()) {
         float fontSize = 6.0F;
         float iconSize = 8.0F;
         float padding = 4.0F;
         float spacing = 2.0F;
         float rowH = 13.0F;
         float gap = 2.0F;
         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         float scaleFix = targetScale / currentScale;
         int bgColor = Client.get().friendManager().isFriend(entity.method_5477().getString()) ? ColorUtil.getColor(0, 255, 0) : ColorUtil.background();
         if (ReportHelper.isReported(entity)) {
            bgColor = ReportHelper.getReportColor(bgColor);
         }

         float y = feetY + 2.0F;

         for (class_1799 stack : hands) {
            String name = toColoredString(stack.method_63015());
            float textWidth = Fonts.sf_regular.getWidth(name, fontSize);
            float wr = padding * 2.0F + iconSize + spacing + textWidth;
            float bgX = centerX - wr / 2.0F;
            RenderUtil.Blur.blur(bgX, y - 0.75F, wr, rowH, 1.0F, 4.0F, ColorUtil.replAlpha(bgColor, InterFace.getInstance().alphaHUD.getValue() * 0.5F));
            ItemRender.drawItemCenteredWithContext(context, stack, bgX + padding + iconSize / 2.0F, y + rowH / 2.0F, 0.5F, 1.0F);
            float textX = bgX + padding + iconSize + spacing;
            float textY = y + rowH / 2.0F - 4.8F;
            Fonts.sf_regular.draw(name, textX, textY, fontSize, ColorUtil.WHITE);
            y += rowH + gap;
         }
      }
   }

   private void renderTag(class_332 context, class_1297 entity, String name, float x, float y, boolean isFriend, float tickDelta, float hp, float hpMax) {
      TextFactoryEvent nameEvent = new TextFactoryEvent(name);
      nameEvent.hook();
      name = nameEvent.getText();
      float fontSize = 7.5F;
      float padding = 3.5F;
      float spacing = 3.0F;
      ArrayList<class_1799> armorItems = new ArrayList<>();
      if (this.armor.getValue() && entity instanceof class_1309 living) {
         for (class_1304 slot : class_1304.field_54086) {
            class_1799 stack = living.method_6118(slot);
            if (!stack.method_7960()) {
               armorItems.add(stack);
            }
         }
      }

      float itemStep = 9.5F;
      float iconSize = 8.0F;
      float headSize = 9.5F;
      float headGap = 2.5F;
      boolean hasHead = entity instanceof class_1309;
      float headW = hasHead ? headSize + headGap : 0.0F;
      float itemsWidth = this.armor.getValue() && !armorItems.isEmpty() ? spacing + (float)(armorItems.size() - 1) * itemStep + iconSize : 0.0F;
      float textWidth = Fonts.sf_regular.getWidth(name, fontSize);
      boolean showHp = hp >= 0.0F;
      String hpText = showHp ? String.valueOf(Math.max(0, Math.round(hp))) : "";
      float hpFont = 6.5F;
      float hpH = 10.5F;
      float hpW = showHp ? Math.max(hpH, Fonts.sf_regular.getWidth(hpText, hpFont) + 5.0F) : 0.0F;
      float hpGap = showHp ? 3.0F : 0.0F;
      boolean showBadge = Globals.isSoftUser(entity);
      float badgeSize = 8.5F;
      float badgeGap = showBadge ? 2.5F : 0.0F;
      float badgeW = showBadge ? badgeSize + badgeGap : 0.0F;
      float contentWidth = headW + badgeW + textWidth + hpGap + hpW + itemsWidth;
      float wr = contentWidth + padding * 2.0F;
      float h = 13.5F;
      float bgX = x - wr / 2.0F;
      float bgY = y - 3.0F;
      int bgColor = isFriend ? ColorUtil.getColor(0, 255, 0) : ColorUtil.background();
      if (ReportHelper.isReported(entity)) {
         bgColor = ReportHelper.getReportColor(bgColor);
      }

      RenderUtil.Blur.blur(bgX, bgY, wr, h, 1.0F, 4.0F, ColorUtil.replAlpha(bgColor, InterFace.getInstance().alphaHUD.getValue() * 0.5F));
      Draw.outline(bgX, bgY, wr, h, 0.5F, ColorUtil.getColor(255, 0.08F), 4.0F);
      int hpColor = showHp ? ColorUtil.getHealthColor(hp, hpMax) : 0;
      float hpX = bgX + padding + headW + textWidth + hpGap;
      float hpY = bgY + (h - hpH) / 2.0F;
      if (showHp) {
         Draw.rect(hpX, hpY, hpW, hpH, ColorUtil.replAlpha(hpColor, 0.9F), 2.5F);
      }

      Client.get().render2D().flushAll();
      if (hasHead) {
         this.drawEntityHead(context, entity, bgX + padding, bgY + (h - headSize) / 2.0F, headSize, tickDelta);
      }

      float curX = bgX + padding + headW;
      if (showBadge) {
         float badgeY = bgY + (h - Fonts.sf_bold.getHeight(badgeSize)) / 2.0F;
         Fonts.sf_bold.draw("L", curX, badgeY, badgeSize, ColorUtil.client());
         curX += badgeSize + badgeGap;
      }

      float textY = bgY + (h - Fonts.sf_regular.getHeight(fontSize)) / 2.0F;
      Fonts.sf_regular.draw(name, curX, textY, fontSize, ColorUtil.WHITE);
      if (showHp) {
         float hpTextW = Fonts.sf_regular.getWidth(hpText, hpFont);
         Fonts.sf_regular
            .draw(hpText, hpX + (hpW - hpTextW) / 2.0F, hpY + (hpH - Fonts.sf_regular.getHeight(hpFont)) / 2.0F, hpFont, ColorUtil.getColor(15, 1.0F));
      }

      if (this.armor.getValue() && !armorItems.isEmpty()) {
         float itemX = curX + textWidth + hpGap + hpW + spacing;
         float itemCenterY = bgY + h / 2.0F;
         float currentItemOffset = 0.0F;

         for (class_1799 stack : armorItems) {
            ItemRender.drawItemCenteredWithContext(context, stack, itemX + currentItemOffset + iconSize / 2.0F, itemCenterY, 0.5F, 1.0F);
            currentItemOffset += itemStep;
         }
      }
   }

   private void drawEntityHead(class_332 context, class_1297 entity, float x, float y, float size, float tickDelta) {
      try {
         if (!(entity instanceof class_1309 living)) {
            return;
         }

         class_1826 egg;
         if (!(living instanceof class_1657) && (egg = class_1826.method_8019(living.method_5864())) != null) {
            class_1799 eggStack = new class_1799(egg);
            ItemRender.drawItemCenteredWithContext(context, eggStack, x + size / 2.0F, y + size / 2.0F, 0.68F, 1.0F);
            return;
         }

         if (!(mc.method_1561().method_3953(living) instanceof class_922 renderer)) {
            return;
         }

         class_10042 state = (class_10042)renderer.method_62425(living, tickDelta);
         class_2960 textureLocation = renderer.method_3885(state);
         float texW = 64.0F;
         float texH = 64.0F;

         try {
            class_1044 texture = mc.method_1531().method_4619(textureLocation);
            GpuTexture gpu;
            if (texture != null && (gpu = texture.method_68004()) != null) {
               texW = (float)gpu.getWidth(0);
               texH = (float)gpu.getHeight(0);
            }
         } catch (Exception var17) {
         }

         if (texW <= 0.0F) {
            texW = 64.0F;
         }

         if (texH <= 0.0F) {
            texH = 64.0F;
         }

         RenderUtil.Images.texture(textureLocation, x, y, size, size, 8.0F / texW, 8.0F / texH, 16.0F / texW, 16.0F / texH, ColorUtil.WHITE, 0.0F, 3.0F);
      } catch (Exception var18) {
      }
   }

   public static boolean hasArmor(class_1657 p) {
      return !p.method_6118(class_1304.field_6169).method_7960()
         || !p.method_6118(class_1304.field_6174).method_7960()
         || !p.method_6118(class_1304.field_6172).method_7960()
         || !p.method_6118(class_1304.field_6166).method_7960();
   }

   private float getHealth(class_1309 entity) {
      float hp = entity.method_6032() + entity.method_6067();
      class_266 scoreBoard;
      if (entity instanceof class_1657 player
         && mc.field_1687 != null
         && (scoreBoard = mc.field_1687.method_8428().method_1189(class_8646.field_45158)) != null) {
         class_5250 text = class_9013.method_55398(
            mc.field_1687.method_8428().method_55430(player, scoreBoard), scoreBoard.method_55380(class_9025.field_47566)
         );

         try {
            hp = Float.parseFloat(ColorUtil.removeFormatting(text.getString()));
         } catch (Exception var7) {
         }
      }

      return ServerUtil.isCopyTime() ? entity.method_6032() : class_3532.method_15363(hp, 0.0F, entity.method_6063() + entity.method_6067());
   }

   private static String toColoredString(class_2561 text) {
      StringBuilder sb = new StringBuilder();
      text.method_27658((style, str) -> {
         class_5251 tc = style.method_10973();
         if (tc != null) {
            sb.append("§#").append(String.format("%06X", tc.method_27716()));
         }

         sb.append(str);
         return Optional.empty();
      }, class_2583.field_24360);
      return sb.toString();
   }

   private void renderDonBuffs(class_332 context, class_1657 player, float centerX, float topY) {
      if (this.donBuffs.getValue()) {
         List<CustomBuffs.ActiveBuff> activeBuffs = CustomBuffs.detectActiveBuffs(player.method_6026());
         if (!activeBuffs.isEmpty()) {
            float iconSize = 8.0F;
            float fontSize = 5.5F;
            float pad = 3.0F;
            float spacing = 2.5F;
            float pillH = 11.0F;
            float pillGap = 2.5F;
            List<Float> pillWidths = new ArrayList<>();
            float totalW = 0.0F;

            for (CustomBuffs.ActiveBuff ab : activeBuffs) {
               String dur = ab.getDurationString();
               String buffName = ab.getBuff().getLocalizedName();
               float textW = Fonts.sf_bold.getWidth(buffName + " ", fontSize) + Fonts.sf_regular.getWidth(dur, fontSize);
               float pillW = pad * 2.0F + iconSize + spacing + textW;
               pillWidths.add(pillW);
               totalW += pillW;
            }

            totalW += (float)(activeBuffs.size() - 1) * pillGap;
            float startX = centerX - totalW / 2.0F;
            float y = topY - pillH - 2.5F;

            for (int i = 0; i < activeBuffs.size(); i++) {
               CustomBuffs.ActiveBuff ab = activeBuffs.get(i);
               float pillW = pillWidths.get(i);
               RenderUtil.Blur.blur(startX, y, pillW, pillH, 1.0F, 3.0F, ColorUtil.getColor(12, 16, 24, 0.55F));
               RenderUtil.Render2D.rect(startX, y, pillW, pillH, ColorUtil.getColor(18, 24, 36, 0.45F), 3.0F);
               RenderUtil.Render2D.outline(startX, y, pillW, pillH, 0.5F, ColorUtil.getColor(255, 255, 255, 0.16F), 3.0F);
               class_1799 icon = ab.getBuff().createIconStack();
               ItemRender.drawItemCenteredWithContext(context, icon, startX + pad + iconSize / 2.0F, y + pillH / 2.0F, 0.45F, 1.0F);
               float curTx = startX + pad + iconSize + spacing;
               float textY = y + (pillH - Fonts.sf_regular.getHeight(fontSize)) / 2.0F;
               Fonts.sf_bold.draw(ab.getBuff().getLocalizedName(), curTx, textY, fontSize, ab.getBuff().getColor());
               curTx += Fonts.sf_bold.getWidth(ab.getBuff().getLocalizedName() + " ", fontSize);
               Fonts.sf_regular.draw(ab.getDurationString(), curTx, textY, fontSize, ColorUtil.getColor(220));
               startX += pillW + pillGap;
            }
         }
      }
   }
}
