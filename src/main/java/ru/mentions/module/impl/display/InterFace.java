package ru.mentions.module.impl.display;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import org.joml.Vector2f;
import ru.mentions.lang.Lang;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.MousePressEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.display.interfaceimpl.ArmorHud;
import ru.mentions.module.impl.display.interfaceimpl.ArrayListHud;
import ru.mentions.module.impl.display.interfaceimpl.Information;
import ru.mentions.module.impl.display.interfaceimpl.KeyBinds;
import ru.mentions.module.impl.display.interfaceimpl.MusicHud;
import ru.mentions.module.impl.display.interfaceimpl.Notify;
import ru.mentions.module.impl.display.interfaceimpl.Potions;
import ru.mentions.module.impl.display.interfaceimpl.ScoreBoardHud;
import ru.mentions.module.impl.display.interfaceimpl.TargetHud;
import ru.mentions.module.impl.display.interfaceimpl.UseTrackerHud;
import ru.mentions.module.impl.display.interfaceimpl.WaterMark;
import ru.mentions.module.impl.player.ClickHelper;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.potion.CustomBuffs;

@ModuleInfo(
   name = "Inter Face",
   desc = "Настройка интерфейса (HUD) клиента",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class InterFace extends Module {
   public MultiBooleanSetting element = new MultiBooleanSetting(
      this,
      "Элементы",
      new BooleanSetting("Water mark", true),
      new BooleanSetting("Information", true),
      new BooleanSetting("Key Binds", true),
      new BooleanSetting("Potions", true),
      new BooleanSetting("Music Player", true),
      new BooleanSetting("ArrayList", true),
      new BooleanSetting("Notifications", true),
      new BooleanSetting("Target Hud", true),
      new BooleanSetting("Armor HUD", true),
      new BooleanSetting("Item hud", true),
      new BooleanSetting("Use Tracker", true),
      new BooleanSetting("ScoreBoard", true)
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
   public ModeSetting watermarkMode = new ModeSetting(this, "Watermark mode", "normal", "dynamic island");
   public BooleanSetting islandNotifications = new BooleanSetting(this, "Island notifications", true);
   public BooleanSetting islandMusic = new BooleanSetting(this, "Island music", true);
   public BooleanSetting islandTrackAnnouncements = new BooleanSetting(this, "Island track announcements", true);
   public BooleanSetting mediaNative = new BooleanSetting(this, "Медиа-интеграция (нестабильно)", false);
   public ModeSetting targetHudMode = new ModeSetting(this, "Target HUD mode", "linear", "circle");
   public ModeSetting targetHudView = new ModeSetting(this, "Вид Target HUD", "2D", "3D");
   public SliderSetting targetHud3DX = new SliderSetting(this, "3D TargetHud X", 0.0F, -200.0F, 200.0F, 1.0F).setVisible(() -> this.targetHudView.is("3D"));
   public SliderSetting targetHud3DY = new SliderSetting(this, "3D TargetHud Y", 0.0F, -200.0F, 200.0F, 1.0F).setVisible(() -> this.targetHudView.is("3D"));
   public BooleanSetting customScoreboard = new BooleanSetting(this, "Кастомный скорборд", true);
   public ModeSetting scoreboardColorMode = new ModeSetting(this, "Цвет скорборда", "Обычный", "Радужный", "Клиентский", "Градиент")
      .setVisible(() -> this.customScoreboard.getValue());
   public BooleanSetting showAbsorption = new BooleanSetting(this, "Show absorption", true);
   public BooleanSetting targetHudBuffs = new BooleanSetting(this, Lang.isUkrainian() ? "Бафи в Target HUD" : "Target HUD buffs", true)
      .setVisible(() -> this.element.getValue("Target Hud"));
   public SliderSetting sizeHud = new SliderSetting(this, "Размер интерфейса", 1.0F, 0.5F, 1.5F, 0.05F);
   public SliderSetting alphaHUD = new SliderSetting(this, "Прозрачность худа", 0.6F, 0.0F, 0.9F, 0.1F);
   public BooleanSetting groupCustomBuffs = new BooleanSetting(this, Lang.isUkrainian() ? "Кастомні бафи" : "Кастомные бафы", true);
   public BooleanSetting notifyPlayerBuffs = new BooleanSetting(this, Lang.isUkrainian() ? "Сповіщення про бафи" : "Уведомление о бафах", true);
   public BooleanSetting notifyEffects = new BooleanSetting(this, "эффектах", true);
   public ModeSetting effectsSound = new ModeSetting(
         this,
         "Звук эффектов",
         "DingDong",
         "Bell",
         "Modern",
         "Bubble",
         "ApplePay",
         "Celestial",
         "Zenith",
         "Classic",
         "Soft",
         "Cyber",
         "Skeet",
         "Laser",
         "WexSide",
         "Spek",
         "Metallic",
         "Crime",
         "Tone",
         "Pop",
         "Trevoga",
         "По умолчанию",
         "Выкл"
      )
      .setVisible(() -> this.notifyEffects.getValue());
   public BooleanSetting notifyModules = new BooleanSetting(this, "модулях", true);
   public BooleanSetting notifyArmor = new BooleanSetting(this, "броне", true);
   public ModeSetting armorSound = new ModeSetting(
         this,
         "Звук брони",
         "Trevoga",
         "Bell",
         "Modern",
         "Bubble",
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
         "Tone",
         "Pop",
         "По умолчанию",
         "Выкл"
      )
      .setVisible(() -> this.notifyArmor.getValue());
   public SliderSetting notifyHeight = new SliderSetting(this, Lang.isUkrainian() ? "Висота сповіщень" : "Notification height", 1.0F, 0.7F, 1.5F, 0.05F)
      .setVisible(() -> this.element.getValue("Notifications"));
   public DragSetting waterMark = new DragSetting(this, "WaterMark", new Vector2f(10.0F, 10.0F));
   public DragSetting dynamicIsland = new DragSetting(this, "Dynamic Island", new Vector2f(0.0F, 10.0F), false);
   public DragSetting information = new DragSetting(this, "Information", new Vector2f(10.0F, 30.0F));
   public DragSetting keyBind = new DragSetting(this, "Key Binds", new Vector2f(10.0F, 50.0F));
   public DragSetting potion = new DragSetting(this, "Potions", new Vector2f(90.0F, 50.0F));
   public DragSetting music = new DragSetting(this, "Music Player", new Vector2f(10.0F, 400.0F));
   public DragSetting arrayList = new DragSetting(this, "ArrayList", new Vector2f(90.0F, 40.0F));
   public DragSetting notifications = new DragSetting(this, "Notifications", new Vector2f(0.0F, 200.0F));
   public DragSetting targetHudDrag = new DragSetting(this, "Target Hud", new Vector2f(90.0F, 40.0F));
   public DragSetting armorHudDrag = new DragSetting(this, "Armor HUD", new Vector2f(300.0F, 300.0F));
   public DragSetting itemHud = new DragSetting(this, "Item hud", new Vector2f(200.0F, 90.0F));
   public DragSetting useTracker = new DragSetting(this, "Use Tracker", new Vector2f(200.0F, 120.0F));
   public DragSetting scoreBoard = new DragSetting(this, "ScoreBoard", new Vector2f(10.0F, 150.0F));
   private final WaterMark waterMarkElemnt = new WaterMark();
   private final Information informationElemnt = new Information();
   private final KeyBinds keyBinds = new KeyBinds();
   private final Potions potions = new Potions();
   private final MusicHud musicHud = new MusicHud();
   private final ArrayListHud arrayListHud = new ArrayListHud();
   private final Notify notifyHud = new Notify();
   private final TargetHud targetHud = new TargetHud();
   private final ArmorHud armorHud = new ArmorHud();
   private final UseTrackerHud useTrackerHud = new UseTrackerHud();
   private final ScoreBoardHud scoreBoardHud = new ScoreBoardHud();

   public static InterFace getInstance() {
      return Instance.get(InterFace.class);
   }

   public MusicHud getMusicHud() {
      return this.musicHud;
   }

   public InterFace() {
      this.notifications.lockX = true;
      this.dynamicIsland.lockX = true;
      this.music.dragBlocker = (mx, my) -> this.musicHud.isControlAt((double)mx.floatValue(), (double)my.floatValue());
      this.dynamicIsland.dragBlocker = (mx, my) -> this.isDynamicIslandActive()
            && this.waterMarkElemnt.isIslandControlAt((double)mx.floatValue(), (double)my.floatValue());
   }

   public boolean isDynamicIslandActive() {
      return this.element.getValue("Water mark") && this.watermarkMode.is("dynamic island");
   }

   @Override
   protected void onDisable() {
      this.musicHud.shutdown();
      this.waterMarkElemnt.resetIsland(this);
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.isEnabled()) {
         if (this.element.getValue("Music Player") || this.isDynamicIslandActive() && this.islandMusic.getValue()) {
            this.musicHud.onTick(this.mediaNative.getValue());
         }

         if (this.element.getValue("Notifications") || this.isDynamicIslandActive() && this.islandNotifications.getValue()) {
            this.notifyHud.onTick(this.notifyModules, this.notifyArmor, this.notifyEffects);
         }

         if (this.notifyPlayerBuffs.getValue() && mc.field_1687 != null) {
            for (class_1657 player : mc.field_1687.method_18456()) {
               CustomBuffs.checkPlayerBuffs(player);
            }

            CustomBuffs.checkIncomingProjectiles();
         }
      }
   }

   @EventHandler
   public void onMousePress(MousePressEvent event) {
      if (this.isEnabled() && mc.field_1724 != null && mc.field_1687 != null) {
         if (this.isDynamicIslandActive() && this.waterMarkElemnt.onMouseClick(event, this, this.musicHud)) {
            event.cancel();
         } else {
            if (this.element.getValue("Music Player") && this.musicHud.onMouseClick(event)) {
               event.cancel();
            }
         }
      }
   }

   @EventHandler
   public void onDisplayEvent(EventDisplay eventDisplay) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.isEnabled()) {
         if (this.element.getValue("Potions")) {
            this.potions.onRender(this.potion, this, eventDisplay);
         }

         if (this.element.getValue("Information")) {
            this.informationElemnt.onRender(this.information, this);
         }

         if (this.element.getValue("Key Binds")) {
            this.keyBinds.onRender(this.keyBind, this);
         }

         if (this.element.getValue("Water mark")) {
            this.waterMarkElemnt.onRender(this.waterMark, this, this.musicHud);
         } else {
            this.waterMark.active = false;
            this.waterMarkElemnt.resetIsland(this);
         }

         if (this.element.getValue("Music Player")) {
            this.musicHud.onRender(this.music, this);
         } else {
            this.music.active = false;
         }

         if (this.element.getValue("ArrayList")) {
            this.arrayListHud.onRender(this.arrayList, this);
         }

         if (this.element.getValue("Notifications")) {
            this.notifyHud.onRender(this.notifications, this, eventDisplay);
         }

         if (this.element.getValue("Target Hud")) {
            this.targetHud.onRender(this.targetHudDrag, this, eventDisplay);
         }

         if (this.element.getValue("Armor HUD")) {
            this.armorHud.onRender(this.armorHudDrag, this, eventDisplay);
         }

         if (this.element.getValue("Use Tracker")) {
            this.useTrackerHud.onRender(this.useTracker, this);
         }

         if (this.element.getValue("Item hud")) {
            Vector2f size = ClickHelper.getInstance().getHudSize();
            this.itemHud.size.set(size.x, size.y);
            ClickHelper.getInstance().renderHud(eventDisplay.getDrawContext(), this.itemHud.position.x, this.itemHud.position.y);
         }

         if (this.customScoreboard.getValue() && this.element.getValue("ScoreBoard")) {
            this.scoreBoardHud.onRender(this.scoreBoard, this, eventDisplay);
         } else {
            this.scoreBoard.active = false;
         }
      }
   }
}
