package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;
import net.minecraft.class_239.class_240;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.PixelGridSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.screen.ClickGuiScreen;
import ru.mentions.screen.CrosshairEditor;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.render.RenderUtil;

@ModuleInfo(
   name = "Cross Hair",
   desc = "Кастомный прицел",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class CrossHair extends Module {
   public static final int GRID_SIZE = 21;
   public ButtonSetting openEditor = new ButtonSetting(this, "Редактор прицела", () -> {
      class_437 patt0$temp = mc.field_1755;
      if (patt0$temp instanceof ClickGuiScreen) {
         ((ClickGuiScreen)patt0$temp).openCrosshairEditor();
      }
   }).setVisible(() -> this.isDrawing() && !this.isEditorOpen());
   public ModeSetting type = new ModeSetting(this, "Режим", "Кастомный", "Точка", "Кружочек");
   public final PixelGridSetting grid = new PixelGridSetting(this, "Рисунок", 21);
   public final SliderSetting pixelSize = new SliderSetting(this, "Размер пикселя", 1.0F, 0.5F, 4.0F, 0.5F).setVisible(this::isEditorSettingVisible);
   public final BooleanSetting pixelOutline = new BooleanSetting(this, "Обводка рисунка", true).setVisible(this::isEditorSettingVisible);
   public final ModeSetting pixelColorMode = new ModeSetting(this, "Цвет рисунка", "Тема", "Свой", "Градиент").setVisible(this::isEditorSettingVisible);
   public final ColorSetting pixelTint = new ColorSetting(this, "Свой цвет", -1)
      .setVisible(() -> this.isEditorSettingVisible() && "Свой".equalsIgnoreCase(this.pixelColorMode.getValue()));
   public final ModeSetting gradientMode = new ModeSetting(this, "Режим градиента", "Статический", "Анимированный")
      .setVisible(() -> this.isEditorSettingVisible() && "Градиент".equalsIgnoreCase(this.pixelColorMode.getValue()));
   public final ColorSetting gradientColor1 = new ColorSetting(this, "Цвет градиента 1", -65536)
      .setVisible(() -> this.isEditorSettingVisible() && "Градиент".equalsIgnoreCase(this.pixelColorMode.getValue()));
   public final ColorSetting gradientColor2 = new ColorSetting(this, "Цвет градиента 2", -16776961)
      .setVisible(() -> this.isEditorSettingVisible() && "Градиент".equalsIgnoreCase(this.pixelColorMode.getValue()));
   public final SliderSetting gradientSpeed = new SliderSetting(this, "Скорость градиента", 1.0F, 0.1F, 4.0F, 0.1F)
      .setVisible(
         () -> this.isEditorSettingVisible()
               && "Градиент".equalsIgnoreCase(this.pixelColorMode.getValue())
               && "Анимированный".equalsIgnoreCase(this.gradientMode.getValue())
      );
   public final BooleanSetting targetColorToggle = new BooleanSetting(this, "Цвет при наведении", false).setVisible(this::isEditorSettingVisible);
   public final ModeSetting targetColorMode = new ModeSetting(this, "Цвет сущности", "Тема", "Свой", "Градиент")
      .setVisible(() -> this.isEditorSettingVisible() && this.targetColorToggle.getValue());
   public final ColorSetting targetTint = new ColorSetting(this, "Свой цвет сущности", -65536)
      .setVisible(() -> this.isEditorSettingVisible() && this.targetColorToggle.getValue() && "Свой".equalsIgnoreCase(this.targetColorMode.getValue()));
   public final ModeSetting targetGradientMode = new ModeSetting(this, "Градиент сущности", "Статический", "Анимированный")
      .setVisible(() -> this.isEditorSettingVisible() && this.targetColorToggle.getValue() && "Градиент".equalsIgnoreCase(this.targetColorMode.getValue()));
   public final ColorSetting targetGradient1 = new ColorSetting(this, "Цвет сущности 1", -65536)
      .setVisible(() -> this.isEditorSettingVisible() && this.targetColorToggle.getValue() && "Градиент".equalsIgnoreCase(this.targetColorMode.getValue()));
   public final ColorSetting targetGradient2 = new ColorSetting(this, "Цвет сущности 2", -256)
      .setVisible(() -> this.isEditorSettingVisible() && this.targetColorToggle.getValue() && "Градиент".equalsIgnoreCase(this.targetColorMode.getValue()));
   public final SliderSetting targetGradientSpeed = new SliderSetting(this, "Скорость сущности", 1.0F, 0.1F, 4.0F, 0.1F)
      .setVisible(
         () -> this.isEditorSettingVisible()
               && this.targetColorToggle.getValue()
               && "Градиент".equalsIgnoreCase(this.targetColorMode.getValue())
               && "Анимированный".equalsIgnoreCase(this.targetGradientMode.getValue())
      );
   public final SliderSetting silaAnim = new SliderSetting(this, "Сила анимации при ударе", 5.0F, 0.0F, 10.0F, 1.0F).setVisible(this::isEditorSettingVisible);
   public Animation animation = new Animation();

   public static CrossHair getInstance() {
      return Instance.get(CrossHair.class);
   }

   public boolean isDrawing() {
      return this.type.is("Кастомный");
   }

   public boolean isEditorOpen() {
      return CrosshairEditor.getInstance() != null && CrosshairEditor.getInstance().isActive();
   }

   public boolean isEditorSettingVisible() {
      if (!this.isDrawing()) {
         return false;
      } else {
         boolean inNormalClickGui = mc.field_1755 instanceof ClickGuiScreen && !this.isEditorOpen();
         return !inNormalClickGui;
      }
   }

   public float expandFor(float animValue) {
      return this.silaAnim.getValue() * animValue;
   }

   @EventHandler
   public void onRender(EventDisplay eventDisplay) {
      float targetScale = 2.0F;
      float currentScale = (float)mc.method_22683().method_4495();
      float scaleFix = targetScale / currentScale;
      int screenWidth = (int)((float)mc.method_22683().method_4486() / scaleFix);
      int screenHeight = (int)((float)mc.method_22683().method_4502() / scaleFix);
      float x = (float)(screenWidth / 2);
      float y = (float)(screenHeight / 2);
      if (this.type.is("Кастомный")) {
         this.animation.update();
         float cooldown = 1.0F - mc.field_1724.method_7261(0.0F);
         this.animation.run((double)cooldown, 0.001F);
         this.drawPixelCrosshair(x, y, this.expandFor(this.animation.get()), 1.0F);
      } else {
         if (this.type.is("Кружочек")) {
            this.animation.update();
            float cooldown = 1.0F - mc.field_1724.method_7261(0.0F);
            this.animation.run((double)cooldown, 0.01F);
            RenderUtil.Render2D.roundedCircleProgress(
               eventDisplay.getDrawContext(), x, y, 4.0F, 1.0F, this.animation.get(), ThemeColor.getHudColor(), ColorUtil.getColor(0), ColorUtil.getColor(0)
            );
         }

         if (this.type.is("Точка")) {
            RenderUtil.Render2D.gradientRect(
               x - 1.5F,
               y - 1.5F,
               3.0F,
               3.0F,
               new int[]{ColorUtil.getColor(255), ColorUtil.getColor(255), ColorUtil.getColor(255), ColorUtil.getColor(255)},
               4.0F
            );
            RenderUtil.Render2D.outline(x - 2.0F, y - 2.0F, 4.0F, 4.0F, 0.5F, ColorUtil.getColor(0), 4.0F);
         }
      }
   }

   public void drawPixelCrosshair(float centerX, float centerY, float expand, float alphaMul) {
      if (!(alphaMul <= 0.01F)) {
         float ps = Math.max(0.5F, this.pixelSize.getValue());
         int c = this.grid.center();
         if (this.pixelOutline.getValue()) {
            int outline = ColorUtil.getColor(0, alphaMul * 0.85F);
            this.forEachPixel(
               centerX, centerY, ps, c, expand, (px, py, gx, gy) -> RenderUtil.Render2D.rect(px - 0.5F, py - 0.5F, ps + 1.0F, ps + 1.0F, outline)
            );
         }

         this.forEachPixel(centerX, centerY, ps, c, expand, (px, py, gx, gy) -> {
            int fill = this.resolvePixelColor(gx, gy, alphaMul);
            RenderUtil.Render2D.rect(px, py, ps, ps, fill);
         });
      }
   }

   private void forEachPixel(float centerX, float centerY, float ps, int c, float expand, CrossHair.PixelConsumer consumer) {
      for (int gy = 0; gy < this.grid.size; gy++) {
         for (int gx = 0; gx < this.grid.size; gx++) {
            if (this.grid.get(gx, gy)) {
               int dx = gx - c;
               int dy = gy - c;
               float px = centerX + (float)dx * ps + Math.signum((float)dx) * expand - ps / 2.0F;
               float py = centerY + (float)dy * ps + Math.signum((float)dy) * expand - ps / 2.0F;
               consumer.accept(px, py, gx, gy);
            }
         }
      }
   }

   public int resolvePixelColor(int gx, int gy, float alphaMul) {
      boolean isTarget = this.targetColorToggle.getValue() && mc.field_1765 != null && mc.field_1765.method_17783() == class_240.field_1331;
      String var7 = isTarget ? this.targetColorMode.getValue() : this.pixelColorMode.getValue();

      int base = switch (var7) {
         case "Свой" -> isTarget ? this.targetTint.getValue() : this.pixelTint.getValue();
         case "Градиент" -> {
            float pos = (float)(gx + gy) / (float)Math.max(1, this.grid.size * 2 - 2);
            String gMode = isTarget ? this.targetGradientMode.getValue() : this.gradientMode.getValue();
            float factor;
            if ("Анимированный".equalsIgnoreCase(gMode)) {
               double speedVal = (isTarget ? this.targetGradientSpeed : this.gradientSpeed).getValue().doubleValue();
               double time = (double)System.currentTimeMillis() * speedVal / 1000.0;
               factor = (float)((Math.sin(((double)pos + time) * Math.PI * 2.0) + 1.0) / 2.0);
            } else {
               factor = pos;
            }

            int c1 = isTarget ? this.targetGradient1.getValue() : this.gradientColor1.getValue();
            int c2 = isTarget ? this.targetGradient2.getValue() : this.gradientColor2.getValue();
            yield ColorUtil.interpolateColor(c1, c2, factor);
         }
         default -> ColorUtil.getClientColor1(1);
      };
      return ColorUtil.replAlpha(base, alphaMul);
   }

   public int resolvePixelColor(float alphaMul) {
      return this.resolvePixelColor(this.grid.center(), this.grid.center(), alphaMul);
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   private interface PixelConsumer {
      void accept(float var1, float var2, int var3, int var4);
   }
}
