package ru.mentions.module.impl.display;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_310;
import org.joml.Vector2f;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.render.GifTexture;

@ModuleInfo(
   name = "Hud",
   desc = "Настройка интерфейса (HUD) клиента",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Hud extends Module {
   public BooleanSetting waterMark = new BooleanSetting("Лого клиента", true);
   public BooleanSetting cortds = new BooleanSetting("Координаты", true);
   public BooleanSetting bpstps = new BooleanSetting("BPS и TPS", true);
   public BooleanSetting porionList = new BooleanSetting("Активные эффекты", true);
   public BooleanSetting keybindslist = new BooleanSetting("Активные клавиши", true);
   public BooleanSetting arrayListHude = new BooleanSetting("Аррай лист", false);
   public BooleanSetting armorHud = new BooleanSetting("Armor", true);
   public BooleanSetting inventoryHud = new BooleanSetting("Inventory", true);
   public MultiBooleanSetting elempt = new MultiBooleanSetting(
      this, "Отображать", this.waterMark, this.keybindslist, this.porionList, this.bpstps, this.cortds, this.arrayListHude, this.armorHud, this.inventoryHud
   );
   public SliderSetting volume = new SliderSetting(this, "Громкость уведомления", 0.5F, 0.1F, 1.0F, 0.1F);
   public ModeSetting typeNotify = new ModeSetting(
      this,
      "Тип уведомления",
      "Tone",
      "Notify",
      "Akrien",
      "Modern",
      "Bubble",
      "Bell",
      "ApplePay",
      "Celestial",
      "Zenith",
      "Classic",
      "Soft",
      "Cyber",
      "Skeet",
      "DingDong",
      "Laser",
      "WexSide",
      "Spek",
      "Metallic",
      "Crime",
      "Pop",
      "Первый",
      "Второй",
      "Третий"
   );
   public Animation withpan = new Animation();
   public DragSetting watermark_drag = new DragSetting(this, "watermark_draggings");
   public DragSetting keybinds_drag = new DragSetting(this, "Keybinds_dragings");
   public DragSetting potions_drag = new DragSetting(this, "Potions_dragings");
   public DragSetting clickbinds_drag = new DragSetting(this, "ClickBinds_dragings", new Vector2f(8.0F, 100.0F));
   public DragSetting arraylist_drag = new DragSetting(this, "ArrayList_dragings", new Vector2f(10000.0F, 4.0F));
   public DragSetting armor_drag = new DragSetting(this, "Armor_dragings", new Vector2f(160.0F, 90.0F));
   public DragSetting inventory_drag = new DragSetting(this, "Inventory_dragings", new Vector2f(160.0F, 170.0F));
   public DragSetting cords_drag = new DragSetting(this, "Cords_dragings", new Vector2f(-999.0F, -999.0F));
   public DragSetting bps_drag = new DragSetting(this, "Bps_dragings", new Vector2f(-999.0F, -999.0F));
   public DragSetting tps_drag = new DragSetting(this, "Tps_dragings", new Vector2f(-999.0F, -999.0F));
   private float smoothMaxKeyWidth = -1.0F;
   public Animation mainAnim = new Animation();
   public Animation keybindsWidthAnim = new Animation();
   public Animation keybindsMaxKeyWidthAnim = new Animation();
   private final Map<String, Hud.EffectData> displayedEffects = new LinkedHashMap<>();
   ru.mentions.utils.animation.satoshi.Animation animation = new EaseInOutQuad(200, 1.0);
   public class_310 mc = class_310.method_1551();
   private double prevX;
   private double prevY;
   private double prevZ;
   private long prevTime;
   private float bps;
   private GifTexture testGif;
   private boolean gifLoaded = false;

   public static boolean getShadow() {
      return false;
   }

   public static int getVisualsColor() {
      return ThemeColor.getVisualColor();
   }

   public static int getHudColor() {
      return ThemeColor.getHudColor();
   }

   public static float getAlpha() {
      return ThemeColor.getOpacity() < 0.98F ? ThemeColor.getOpacity() : 0.98F;
   }

   @Environment(EnvType.CLIENT)
   private static class EffectData {
      String name;
      String duration;
      Animation animation;
      boolean active;
      boolean negative;
      int durationTicks;
      int lvl;
      class_1293 effectInstance;

      public EffectData(String name, String duration, int lvl, boolean negative, int durationTicks, class_1293 effectInstance) {
         this.name = name;
         this.duration = duration;
         this.lvl = lvl;
         this.negative = negative;
         this.durationTicks = durationTicks;
         this.animation = new Animation();
         this.active = true;
         this.effectInstance = effectInstance;
      }
   }
}
