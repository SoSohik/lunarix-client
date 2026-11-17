package ru.mentions.module.impl.misc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.other.NotificationSound;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Alert Helper",
   category = Category.OTHER,
   desc = "Предупреждает об окончании выбранных эффектов"
)
@Environment(EnvType.CLIENT)
public class AlertHelper extends Module {
   private static final int YELLOW = 1;
   public final BooleanSetting sound = new BooleanSetting(this, "Звук", true);
   public final ModeSetting soundMode = new ModeSetting(
         this,
         "Звук оповещения",
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
         "Tone",
         "Pop",
         "По умолчанию"
      )
      .setVisible(() -> this.sound.getValue());
   public final MultiBooleanSetting effectFilter = new MultiBooleanSetting(
      this,
      "Эффекты",
      new BooleanSetting("Сила", true),
      new BooleanSetting("Скорость", true),
      new BooleanSetting("Огнестойкость", true),
      new BooleanSetting("Сопротивление", true),
      new BooleanSetting("Регенерация", true),
      new BooleanSetting("Поглощение", true),
      new BooleanSetting("Невидимость", true),
      new BooleanSetting("Спешка", true),
      new BooleanSetting("Прыгучесть", true),
      new BooleanSetting("Плавное падение", true),
      new BooleanSetting("Подводное дыхание", true),
      new BooleanSetting("Прилив здоровья", true),
      new BooleanSetting("Ночное зрение", true),
      new BooleanSetting("Морская сила", true),
      new BooleanSetting("Грация дельфина", true)
   );
   private final List<AlertHelper.Alert> alerts = new CopyOnWriteArrayList<>();
   private final Set<String> seenHigh = new HashSet<>();
   private final Set<String> alerted = new HashSet<>();
   private class_1937 lastWorld;

   @Override
   protected void onDisable() {
      this.alerts.clear();
      this.reset();
   }

   private void reset() {
      this.seenHigh.clear();
      this.alerted.clear();
   }

   private void push(String title, String text, int level, long ms) {
      String snd = this.sound.getValue() ? this.soundMode.getValue() : "Выкл";
      if (this.routedToHud()) {
         NotificationManager.send(text, NotificationManager.Type.WARNING, ms, snd);
      } else {
         while (this.alerts.size() >= 4) {
            this.alerts.remove(0);
         }

         this.alerts.add(new AlertHelper.Alert(title, text, level, ms));
         NotificationSound.playSoundForMode(snd, true);
      }
   }

   private boolean routedToHud() {
      InterFace hud = InterFace.getInstance();
      if (hud == null || !hud.isEnabled()) {
         return false;
      } else {
         return hud.isDynamicIslandActive() && hud.islandNotifications.getValue() ? true : hud.element.getValue("Notifications");
      }
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (this.isEnabled() && mc.field_1724 != null && mc.field_1687 != null) {
         if (this.lastWorld != mc.field_1687) {
            this.lastWorld = mc.field_1687;
            this.reset();
         }

         this.tickEffects();
      }
   }

   private void tickEffects() {
      InterFace hud = InterFace.getInstance();
      if (hud == null || !hud.isEnabled() || !hud.notifyEffects.getValue() || !this.routedToHud()) {
         float limitTicks = 200.0F;
         Set<String> active = new HashSet<>();

         for (class_1293 effect : mc.field_1724.method_6026()) {
            String id = effect.method_5579().method_40230().<class_2960>map(class_5321::method_29177).map(i -> i.method_12832()).orElse("");
            if (this.isEffectAllowed(id)) {
               int ticks = effect.method_5584();
               if (ticks >= 0) {
                  active.add(id);
                  if ((float)ticks > limitTicks) {
                     this.seenHigh.add(id);
                     this.alerted.remove(id);
                  } else if (this.seenHigh.contains(id) && this.alerted.add(id)) {
                     String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
                     this.push("ЭФФЕКТ", name + " заканчивается: " + Math.max(1, ticks / 20) + " сек", 1, 4500L);
                  }
               }
            }
         }

         this.seenHigh.retainAll(active);
         this.alerted.retainAll(active);
      }
   }

   private boolean isEffectAllowed(String id) {
      return switch (id) {
         case "strength" -> this.effectFilter.getValue("Сила");
         case "speed" -> this.effectFilter.getValue("Скорость");
         case "fire_resistance" -> this.effectFilter.getValue("Огнестойкость");
         case "resistance" -> this.effectFilter.getValue("Сопротивление");
         case "regeneration" -> this.effectFilter.getValue("Регенерация");
         case "absorption" -> this.effectFilter.getValue("Поглощение");
         case "invisibility" -> this.effectFilter.getValue("Невидимость");
         case "haste" -> this.effectFilter.getValue("Спешка");
         case "jump_boost" -> this.effectFilter.getValue("Прыгучесть");
         case "slow_falling" -> this.effectFilter.getValue("Плавное падение");
         case "water_breathing" -> this.effectFilter.getValue("Подводное дыхание");
         case "health_boost" -> this.effectFilter.getValue("Прилив здоровья");
         case "night_vision" -> this.effectFilter.getValue("Ночное зрение");
         case "conduit_power" -> this.effectFilter.getValue("Морская сила");
         case "dolphins_grace" -> this.effectFilter.getValue("Грация дельфина");
         default -> false;
      };
   }

   @EventHandler
   public void onDisplay(EventDisplay e) {
      if (this.isEnabled() && !this.alerts.isEmpty()) {
         float scaleFix = 2.0F / (float)mc.method_22683().method_4495();
         float screenW = (float)mc.method_22683().method_4486() / scaleFix;
         float y = 40.0F;
         long now = System.currentTimeMillis();

         for (AlertHelper.Alert a : this.alerts) {
            long age = now - a.created;
            if (age >= a.duration) {
               this.alerts.remove(a);
            } else {
               float in = Math.min(1.0F, (float)age / 200.0F);
               float out = Math.min(1.0F, (float)(a.duration - age) / 300.0F);
               float alpha = Math.min(in, out);
               float pulse = 0.65F + 0.35F * (float)Math.sin((double)age / 120.0);
               int color = ColorUtil.getColor(255, 205, 60);
               float titleSize = 6.5F;
               float textSize = 10.0F;
               float w = Math.max(150.0F, Fonts.sf_bold.getWidth(a.text, textSize) + 34.0F);
               float h = 28.0F;
               float x = screenW / 2.0F - w / 2.0F;
               float by = y - (1.0F - in) * 10.0F;
               Draw.glow(x, by, w, h, ColorUtil.replAlpha(color, 0.35F * alpha * pulse), 7.0F, 14.0F, 1.0F);
               Draw.rect(x, by, w, h, ColorUtil.getColor(14, 0.92F * alpha), 7.0F);
               Draw.rect(x, by, 4.0F, h, ColorUtil.replAlpha(color, alpha), 2.0F);
               Draw.outline(x, by, w, h, 1.0F, ColorUtil.replAlpha(color, alpha * pulse), 7.0F);
               Client.get().render2D().flushAll();
               Fonts.sf_bold.drawCentered(a.title, screenW / 2.0F + 2.0F, by + 3.5F, titleSize, ColorUtil.replAlpha(color, alpha));
               Fonts.sf_bold.drawCentered(a.text, screenW / 2.0F + 2.0F, by + 12.0F, textSize, ColorUtil.getColor(255, alpha));
               y += (h + 5.0F) * alpha;
            }
         }
      }
   }

   private static final class Alert {
      final String title;
      final String text;
      final int level;
      final long created = System.currentTimeMillis();
      final long duration;

      Alert(String title, String text, int level, long duration) {
         this.title = title;
         this.text = text;
         this.level = level;
         this.duration = duration;
      }
   }
}
