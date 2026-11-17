package ru.mentions.module.impl.display;

import java.awt.Color;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.Theme;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.screen.Menu;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Themes",
   desc = "Настройки оформления клиента: монотонные, градиентные и кастомные темы",
   category = Category.THEMES
)
@Environment(EnvType.CLIENT)
public class ClientTheme extends Module {
   public static ClientTheme INSTANCE;
   public ModeSetting uiStyle = new ModeSetting(this, "Стиль UI", "Liquid Glass", "Liquid Glass Old", "Обычный");
   public BooleanSetting whiteTheme = new BooleanSetting(this, "Белая тема", false);
   public BooleanSetting uiBlur = new BooleanSetting(this, "Размытие фона UI", true);
   public BooleanSetting uiSounds = new BooleanSetting(this, "Звуки интерфейса", true);
   public ModeSetting themeType = new ModeSetting(this, "Тип темы", "Монотонные", "Градиентные", "Кастомная");
   public ModeSetting solidTheme = new ModeSetting(
         this, "Монотонная", "Violet", "Red", "Blue", "Green", "Toxis", "White", "Turquoise", "Purple", "Sunset", "Sakura", "Midnight", "Gold", "Emerald"
      )
      .setVisible(() -> this.themeType.is("Монотонные"));
   public static final List<ClientTheme.GradientPreset> GRADIENT_PRESETS = List.of(
      new ClientTheme.GradientPreset("Neon", "Неон", 10181046, 53971, Theme.Purple),
      new ClientTheme.GradientPreset("Sunset Glow", "Сияние заката", 16741952, 16739201, Theme.SUNSET),
      new ClientTheme.GradientPreset("Aurora", "Северное сияние", 1954209, 5546239, Theme.Turquoise),
      new ClientTheme.GradientPreset("Cyberpunk", "Киберпанк", 16706215, 16742005, Theme.TOXIS),
      new ClientTheme.GradientPreset("Blood Moon", "Кровавая луна", 12597547, 9323693, Theme.AKAR),
      new ClientTheme.GradientPreset("Emerald Ice", "Изумрудный лёд", 3066993, 3731390, Theme.EMERALD),
      new ClientTheme.GradientPreset("Deep Ocean", "Глубокий океан", 2003199, 4915330, Theme.BLUE),
      new ClientTheme.GradientPreset("Sakura Dream", "Мечта сакуры", 16740536, 12813055, Theme.SAKURA),
      new ClientTheme.GradientPreset("Cosmic", "Космос", 7101671, 15221651, Theme.MIDNIGHT),
      new ClientTheme.GradientPreset("Fire & Ice", "Огонь и лёд", 16739125, 53971, Theme.GOLD)
   );
   public ModeSetting gradientTheme = new ModeSetting(
         this, "Градиентная", "Neon", "Sunset Glow", "Aurora", "Cyberpunk", "Blood Moon", "Emerald Ice", "Deep Ocean", "Sakura Dream", "Cosmic", "Fire & Ice"
      )
      .setVisible(() -> this.themeType.is("Градиентные"));
   public ModeSetting customMode = new ModeSetting(this, "Режим кастома", "Монотонный", "Градиент").setVisible(() -> this.themeType.is("Кастомная"));
   public ColorSetting customColor1 = new ColorSetting(this, "Цвет 1", new Color(124, 143, 246).getRGB()).setVisible(() -> this.themeType.is("Кастомная"));
   public ColorSetting customColor2 = new ColorSetting(this, "Цвет 2", new Color(246, 124, 126).getRGB())
      .setVisible(() -> this.themeType.is("Кастомная") && this.customMode.is("Градиент"));
   public SliderSetting gradientSpeed = new SliderSetting(this, "Скорость градиента", 5.0F, 1.0F, 10.0F, 0.5F)
      .setVisible(() -> this.themeType.is("Градиентные") || this.themeType.is("Кастомная") && this.customMode.is("Градиент"));
   public SliderSetting blurStrength = new SliderSetting(this, "Размытие фона", 3.0F, 0.0F, 6.0F, 1.0F);

   public boolean isWhiteTheme() {
      return this.whiteTheme != null && this.whiteTheme.getValue();
   }

   public boolean isLiquidGlass() {
      return this.uiStyle != null && this.uiStyle.is("Liquid Glass");
   }

   public boolean isLiquidGlassOld() {
      return this.uiStyle != null && this.uiStyle.is("Liquid Glass Old");
   }

   public ClientTheme() {
      INSTANCE = this;
   }

   @EventHandler
   public void onTick(EventTick e) {
      this.syncTheme();
   }

   public void syncTheme() {
      String type = this.themeType.getValue();
      if (type.equals("Монотонные")) {
         String solid = this.solidTheme.getValue();
         switch (solid) {
            case "Blue":
               this.applyManagerTheme(Theme.BLUE);
               break;
            case "Red":
               this.applyManagerTheme(Theme.AKAR);
               break;
            case "Green":
               this.applyManagerTheme(Theme.NIGHT);
               break;
            case "Violet":
               this.applyManagerTheme(Theme.VIOLKA);
               break;
            case "Toxis":
               this.applyManagerTheme(Theme.TOXIS);
               break;
            case "White":
               this.applyManagerTheme(Theme.White);
               break;
            case "Turquoise":
               this.applyManagerTheme(Theme.Turquoise);
               break;
            case "Purple":
               this.applyManagerTheme(Theme.Purple);
               break;
            case "Sunset":
               this.applyManagerTheme(Theme.SUNSET);
               break;
            case "Sakura":
               this.applyManagerTheme(Theme.SAKURA);
               break;
            case "Midnight":
               this.applyManagerTheme(Theme.MIDNIGHT);
               break;
            case "Gold":
               this.applyManagerTheme(Theme.GOLD);
               break;
            case "Emerald":
               this.applyManagerTheme(Theme.EMERALD);
               break;
            default:
               this.applyManagerTheme(Theme.VIOLKA);
         }
      } else if (type.equals("Градиентные")) {
         String grad = this.gradientTheme.getValue();

         for (ClientTheme.GradientPreset p : GRADIENT_PRESETS) {
            if (p.name.equals(grad)) {
               this.applyManagerTheme(p.baseTheme);
               return;
            }
         }

         this.applyManagerTheme(Theme.VIOLKA);
      } else if (type.equals("Кастомная")) {
         this.applyManagerTheme(Theme.VIOLKA);
      }
   }

   private void applyManagerTheme(Theme theme) {
      if (Menu.selectedTheme != theme) {
         Menu.preSelectedTheme = Menu.selectedTheme != null ? Menu.selectedTheme : theme;
         Menu.selectedTheme = theme;
         if (Menu.animation14 != null) {
            Menu.animation14.reset();
         }
      }
   }

   public int getAccentColor(int index) {
      String type = this.themeType.getValue();
      if (type.equals("Кастомная")) {
         int c1 = this.customColor1.getValue();
         if (this.customMode.is("Градиент")) {
            int c2 = this.customColor2.getValue();
            return ColorUtil.fade((int)(20.0F / Math.max(1.0F, this.gradientSpeed.getValue())), index, c1, c2);
         } else {
            return c1;
         }
      } else if (type.equals("Градиентные")) {
         int speed = (int)(20.0F / Math.max(1.0F, this.gradientSpeed.getValue()));
         String grad = this.gradientTheme.getValue();

         for (ClientTheme.GradientPreset p : GRADIENT_PRESETS) {
            if (p.name.equals(grad)) {
               return ColorUtil.fade(speed, index, 0xFF000000 | p.color1, 0xFF000000 | p.color2);
            }
         }

         return ColorUtil.baseClient();
      } else {
         return ColorUtil.baseClient();
      }
   }

   public static record GradientPreset(String name, String russianName, int color1, int color2, Theme baseTheme) {
   }
}
