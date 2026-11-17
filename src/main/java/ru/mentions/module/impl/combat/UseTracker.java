package ru.mentions.module.impl.combat;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.other.UseCooldowns;

@ModuleInfo(
   name = "Use Tracker",
   desc = "Кулдауны хилок цели и её баффы",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class UseTracker extends Module {
   public final MultiBooleanSetting items = new MultiBooleanSetting(
      this,
      "Предметы",
      new BooleanSetting(UseCooldowns.Item.NOTCH.label, true),
      new BooleanSetting(UseCooldowns.Item.GAPPLE.label, true),
      new BooleanSetting(UseCooldowns.Item.HEAL.label, true),
      new BooleanSetting(UseCooldowns.Item.NAUSEA.label, true),
      new BooleanSetting(UseCooldowns.Item.CHORUS.label, true),
      new BooleanSetting(UseCooldowns.Item.KELP.label, false),
      new BooleanSetting(UseCooldowns.Item.SCRAP.label, false)
   );
   public final MultiBooleanSetting buffs = new MultiBooleanSetting(
      this,
      "Баффы цели",
      new BooleanSetting(UseCooldowns.Buff.STRENGTH.label, true),
      new BooleanSetting(UseCooldowns.Buff.SPEED.label, true),
      new BooleanSetting(UseCooldowns.Buff.RESISTANCE.label, true),
      new BooleanSetting(UseCooldowns.Buff.REGENERATION.label, true),
      new BooleanSetting(UseCooldowns.Buff.FIRE_RESISTANCE.label, true),
      new BooleanSetting(UseCooldowns.Buff.ABSORPTION.label, true)
   );
   public final BooleanSetting notifications = new BooleanSetting(this, "Уведомления", true);
   public final ModeSetting soundMode = new ModeSetting(
         this,
         "Звук уведомления",
         "DingDong",
         "Tone",
         "Notify",
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
         "Laser",
         "WexSide",
         "Spek",
         "Metallic",
         "Crime",
         "Pop",
         "По умолчанию",
         "Выкл"
      )
      .setVisible(() -> this.notifications.getValue());
   public final SliderSetting forget = new SliderSetting(this, "Забывать цель, сек", 60.0F, 10.0F, 300.0F, 10.0F);
   private UUID target;
   private String targetName;
   private long lastAttack;
   private boolean wasNausea;

   public static UseTracker getInstance() {
      return Instance.get(UseTracker.class);
   }

   public UseTracker() {
      UseCooldowns.listen(this::onUse);
   }

   @Override
   protected void onDisable() {
      this.target = null;
      this.targetName = null;
      this.lastAttack = 0L;
      this.wasNausea = false;
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.onDisable();
      UseCooldowns.clear();
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      class_1297 class_12972 = event.getTarget();
      class_1657 player;
      if (class_12972 instanceof class_1657 && (player = (class_1657)class_12972) != mc.field_1724) {
         this.target = player.method_5667();
         this.targetName = player.method_5477().getString();
         this.lastAttack = System.currentTimeMillis();
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      UseCooldowns.tick(event);
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean nausea = mc.field_1724.method_6059(class_1294.field_5916);
         class_1657 player;
         if (nausea && !this.wasNausea && this.hasTarget() && (player = this.getTargetPlayer()) != null) {
            UseCooldowns.trigger(player, UseCooldowns.Item.NAUSEA);
         }

         this.wasNausea = nausea;
      }
   }

   private void onUse(class_1657 player, UseCooldowns.Item item) {
      if (this.isEnabled() && this.notifications.getValue()) {
         if (player.method_5667().equals(this.target)) {
            if (this.items.getValue(item.label)) {
               NotificationManager.send(player.method_5477().getString() + " — " + item.label, NotificationManager.Type.INFO, new class_1799(item.icon));
            }
         }
      }
   }

   public boolean hasTarget() {
      return this.isEnabled() && this.target != null && System.currentTimeMillis() - this.lastAttack < (long)(this.forget.getValue() * 1000.0F);
   }

   public String getTargetName() {
      return this.targetName == null ? "" : this.targetName;
   }

   public UUID getTargetUuid() {
      return this.target;
   }

   public class_1657 getTargetPlayer() {
      return this.target != null && mc.field_1687 != null ? mc.field_1687.method_18470(this.target) : null;
   }

   public int remaining(UseCooldowns.Item item) {
      return !this.items.getValue(item.label) ? 0 : UseCooldowns.remaining(this.target, item);
   }
}
