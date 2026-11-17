package ru.mentions.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_408;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.ClientTheme;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class ScoreBoardHud implements IMinecraft {
   private static final Comparator<class_9011> ENTRY_COMPARATOR = Comparator.<class_9011>comparingInt(class_9011::comp_2128)
      .reversed()
      .thenComparing(class_9011::comp_2127, String.CASE_INSENSITIVE_ORDER);
   private static float S = 1.0F;
   private static final float RADIUS = 6.0F;
   private static final float PADDING_X = 7.0F;
   private static final float ROW_H = 10.5F;

   public void onRender(DragSetting drag, InterFace interFace, EventDisplay eventDisplay) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         S = interFace.sizeHud.getValue();
         class_269 scoreboard = mc.field_1687.method_8428();
         class_266 objective = scoreboard != null ? scoreboard.method_1189(class_8646.field_45157) : null;
         List<ScoreBoardHud.ScoreLine> lines = new ArrayList<>();
         String title = null;
         if (objective != null) {
            title = objective.method_1114().getString();
            Collection<class_9011> entries = scoreboard.method_1184(objective);
            if (entries != null && !entries.isEmpty()) {
               List<class_9011> sorted = new ArrayList<>(entries);
               sorted.sort(ENTRY_COMPARATOR);

               for (class_9011 entry : sorted) {
                  class_270 team = scoreboard.method_1164(entry.comp_2127());
                  class_2561 formatted = class_268.method_1142(team, entry.method_55387());
                  String text = formatted.getString();
                  if (!text.isEmpty()) {
                     lines.add(new ScoreBoardHud.ScoreLine(text, entry.comp_2128()));
                  }
               }
            }
         }

         boolean isPreview = (lines.isEmpty() || title == null) && mc.field_1755 instanceof class_408;
         if (isPreview) {
            title = "Lunarix";
            lines.add(new ScoreBoardHud.ScoreLine("§7" + ServerUtil.server, 5));
            lines.add(new ScoreBoardHud.ScoreLine("§fИгрок: §a" + mc.field_1724.method_5477().getString(), 4));
            lines.add(new ScoreBoardHud.ScoreLine("§fБаланс: §612,450 $", 3));
            lines.add(new ScoreBoardHud.ScoreLine("§fПинг: §a24ms", 2));
            lines.add(new ScoreBoardHud.ScoreLine("§7lunarix.client", 1));
         }

         if (lines.isEmpty() && title == null) {
            drag.active = false;
         } else {
            Font font = Fonts.sf_regular;
            Font bold = Fonts.sf_bold;
            float titleSize = 7.5F * S;
            float textSize = 6.5F * S;
            float maxW = bold.getWidth(title != null ? title : "", titleSize);

            for (ScoreBoardHud.ScoreLine line : lines) {
               float lw = font.getWidth(line.text, textSize);
               if (lw > maxW) {
                  maxW = lw;
               }
            }

            float cardW = Math.max(90.0F * S, maxW + 14.0F * S + 10.0F * S);
            float headerH = title != null && !title.isEmpty() ? 16.0F * S : 4.0F * S;
            float cardH = headerH + (float)lines.size() * 10.5F * S + 5.0F * S;
            drag.active = true;
            drag.size.set(cardW, cardH);
            float x = drag.position.x;
            float y = drag.position.y;
            float r = 6.0F * S;
            boolean isGlass = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlass();
            boolean isGlassOld = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlassOld();
            boolean enableBlur = ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiBlur.getValue();
            float alpha = 1.0F;
            Draw.glow(x, y, cardW, cardH, ColorUtil.getColor(0, 0.12F), r, 12.0F, 1.0F);
            if (isGlass) {
               if (enableBlur) {
                  RenderUtil.Blur.blur(x, y, cardW, cardH, 1.5F, r, ColorUtil.getColor(10, 14, 22, 0.35F));
               }

               int topGlass = ColorUtil.getColor(255, 255, 255, 0.12F);
               int botGlass = ColorUtil.getColor(16, 22, 34, 0.38F);
               RenderUtil.Render2D.gradientRect(x, y, cardW, cardH, new int[]{topGlass, topGlass, botGlass, botGlass}, r);
               RenderUtil.Render2D.outline(x, y, cardW, cardH, 0.75F * S, ColorUtil.getColor(255, 255, 255, 0.18F), r);
               RenderUtil.Render2D.rect(x + 2.0F * S, y + 0.5F * S, cardW - 4.0F * S, 0.85F * S, ColorUtil.getColor(255, 255, 255, 0.4F), 0.5F * S);
            } else if (isGlassOld) {
               if (enableBlur) {
                  RenderUtil.Blur.blur(x, y, cardW, cardH, 1.5F, r, ColorUtil.getColor(12, 16, 26, 0.45F));
               }

               RenderUtil.Render2D.rect(x, y, cardW, cardH, ColorUtil.getColor(16, 22, 34, 0.5F), r);
               RenderUtil.Render2D.outline(x, y, cardW, cardH, 0.5F * S, ColorUtil.getColor(255, 255, 255, 0.16F), r);
            } else {
               if (enableBlur) {
                  RenderUtil.Blur.blur(x, y, cardW, cardH, 1.0F, r, ColorUtil.replAlpha(ColorUtil.background(), interFace.alphaHUD.getValue()));
               } else {
                  RenderUtil.Render2D.rect(x, y, cardW, cardH, ColorUtil.replAlpha(ColorUtil.background(), interFace.alphaHUD.getValue()), r);
               }

               Draw.outline(x, y, cardW, cardH, 0.75F * S, ThemeColor.getOutlineColor(0.08F), r);
            }

            float curY = y + 4.5F * S;
            boolean isRainbow = interFace.scoreboardColorMode != null && interFace.scoreboardColorMode.is("Радужный");
            boolean isClientCol = interFace.scoreboardColorMode != null && interFace.scoreboardColorMode.is("Клиентский");
            boolean isGradient = interFace.scoreboardColorMode != null && interFace.scoreboardColorMode.is("Градиент");
            if (title != null && !title.isEmpty()) {
               float sepW = cardW - 16.0F * S;
               if (isRainbow) {
                  int r1 = ColorUtil.rainbow(15, 0, 0.85F, 1.0F, 1.0F);
                  int r2 = ColorUtil.rainbow(15, 120, 0.85F, 1.0F, 1.0F);
                  bold.drawCenterGradient(title, x + cardW / 2.0F, curY, titleSize, r1, r2);
                  RenderUtil.Render2D.gradientRect(x + 8.0F * S, curY + titleSize + 3.0F * S, sepW, 0.6F * S, new int[]{r1, r2, r2, r1}, 0.3F * S);
               } else if (isClientCol) {
                  bold.drawCentered(title, x + cardW / 2.0F, curY, titleSize, ColorUtil.client());
                  RenderUtil.Render2D.rect(x + 8.0F * S, curY + titleSize + 3.0F * S, sepW, 0.6F * S, ColorUtil.replAlpha(ColorUtil.client(), 0.5F), 0.3F * S);
               } else if (isGradient) {
                  int c1 = ColorUtil.getClientColor(1);
                  int c2 = ColorUtil.getClientColor1(1);
                  bold.drawCenterGradient(title, x + cardW / 2.0F, curY, titleSize, c1, c2);
                  RenderUtil.Render2D.gradientRect(x + 8.0F * S, curY + titleSize + 3.0F * S, sepW, 0.6F * S, new int[]{c1, c2, c2, c1}, 0.3F * S);
               } else {
                  bold.drawCentered(title, x + cardW / 2.0F, curY, titleSize, ColorUtil.getColor(255));
                  RenderUtil.Render2D.rect(x + 8.0F * S, curY + titleSize + 3.0F * S, sepW, 0.6F * S, ColorUtil.replAlpha(ColorUtil.client(), 0.35F), 0.3F * S);
               }

               curY += headerH - 1.0F * S;
            }

            float lineX = x + 7.0F * S;

            for (int i = 0; i < lines.size(); i++) {
               ScoreBoardHud.ScoreLine scoreLine = lines.get(i);
               int rowColor;
               if (isRainbow) {
                  rowColor = ColorUtil.rainbow(15, i * 25, 0.85F, 1.0F, 1.0F);
               } else if (isClientCol) {
                  rowColor = ColorUtil.overCol(ColorUtil.getColor(240), ColorUtil.client(), 0.25F);
               } else if (isGradient) {
                  float factor = lines.size() > 1 ? (float)i / (float)(lines.size() - 1) : 0.0F;
                  rowColor = ColorUtil.overCol(ColorUtil.getClientColor(1), ColorUtil.getClientColor1(1), factor);
               } else {
                  rowColor = ColorUtil.getColor(240);
               }

               font.draw(scoreLine.text, lineX, curY + 1.5F * S, textSize, rowColor);
               curY += 10.5F * S;
            }
         }
      } else {
         drag.active = false;
      }
   }

   private static record ScoreLine(String text, int score) {
   }
}
