package ru.mentions.module.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2815;
import net.minecraft.class_2886;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_465;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_3675.class_307;
import org.joml.Vector2f;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventMoveInput;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.FreeLookUtil;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.module.impl.render.SwingAnimation;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.Keyboard;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.StopGPT;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Click Helper",
   desc = "Помогает кликать по клавише",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class ClickHelper extends Module {
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Lunarix", "Inventory", "System");
   public final SliderSetting invActionDelay = new SliderSetting(this, "Задержка действий (мс)", 50.0F, 10.0F, 300.0F, 10.0F)
      .setVisible(() -> this.mode.is("Inventory"));
   public final ModeSetting type = new ModeSetting(this, "Сервер", "FunTime", "HolyWorld")
      .setVisible(() -> this.mode.is("Lunarix") || this.mode.is("Inventory"));
   public BindSetting friend = new BindSetting(this, "Клавиша добавления в друзья", -1);
   public BindSetting coordsKey = new BindSetting(this, "Клавиша координат", -1);
   public final BindSetting chorusKey = new BindSetting(this, "Клавиша хоруса", -1);
   public final BindSetting expKey = new BindSetting(this, "Клавиша опыта", -1);
   public final ModeSetting windChargeMode = new ModeSetting(this, "Wind Charge", "Под себя", "По прицелу").setVisible(() -> this.mode.is("System"));
   public final BooleanSetting windChargeAutoJump = new BooleanSetting(this, "Auto Jump (Ветер)", false)
      .setVisible(() -> this.mode.is("System") && this.windChargeMode.is("Под себя"));
   public final ModeSetting buffThrowMode = new ModeSetting(this, "Куда бросать бафы", "По прицелу", "Под себя").setVisible(() -> this.mode.is("System"));
   public final BooleanSetting noLeftHandPlace = new BooleanSetting(this, "No Left Hand Place", true).setVisible(() -> this.mode.is("System"));
   public final BindSetting popperKey = new BindSetting(this, "Клавиша хлопушки", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting arenaTrigger = new BindSetting(this, "Клавиша Арены", -1).setVisible(() -> this.mode.is("System"));
   public final BindSetting investTrigger = new BindSetting(this, "Клавиша Инвестиций", -1).setVisible(() -> this.mode.is("System"));
   public final BindSetting storageTrigger = new BindSetting(this, "Клавиша Хранилища", -1).setVisible(() -> this.mode.is("System"));
   public final SliderSetting arenaSlot = new SliderSetting(this, "Слот Арены", 25.0F, 1.0F, 54.0F, 1.0F).setVisible(() -> this.mode.is("System"));
   public final SliderSetting clickDelay = new SliderSetting(this, "Задержка клика (тик)", 1.0F, 0.0F, 20.0F, 1.0F).setVisible(() -> this.mode.is("System"));
   public final SliderSetting openTimeout = new SliderSetting(this, "Таймаут меню (тик)", 60.0F, 10.0F, 200.0F, 5.0F).setVisible(() -> this.mode.is("System"));
   public final BindSetting eyeKey = new BindSetting(this, "Клавиша дезки", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting sugarKey = new BindSetting(this, "Клавиша явки", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting snowKey = new BindSetting(this, "Клавиша снежка", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting plastKey = new BindSetting(this, "Клавиша пласта", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting trapKey = new BindSetting(this, "Клавиша трапки", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting windKey = new BindSetting(this, "Клавиша з.ветра", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting fireChargeKey = new BindSetting(this, "Клавиша огн.шара", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting bojAurakey = new BindSetting(this, "Клавиша бож.ауры", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting keySerka = new BindSetting(this, "Клавиша радейки", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting keyHilka = new BindSetting(this, "Клавиша бож.воды", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting palladinKey = new BindSetting(this, "Клавиша Палладина", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting assassinKey = new BindSetting(this, "Клавиша Ассасина", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting sleepKey = new BindSetting(this, "Клавиша Снотворного", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting angerKey = new BindSetting(this, "Клавиша Гнева", -1)
      .setVisible(() -> this.mode.is("System") || (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("FunTime"));
   public final BindSetting holyStunKey = new BindSetting(this, "Клавиша стана", -1)
      .setVisible(() -> (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld"));
   public final BindSetting holyTrapKey = new BindSetting(this, "Клавиша трапки (HolyWorld)", -1)
      .setVisible(() -> (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld"));
   public final BindSetting holyExplosiveTrapKey = new BindSetting(this, "Клавиша взрывной трапки", -1)
      .setVisible(() -> (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld"));
   public final BindSetting holyExplosiveKey = new BindSetting(this, "Клавиша взрывной штуки", -1)
      .setVisible(() -> (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld"));
   public final BindSetting holySnowKey = new BindSetting(this, "Клавиша кома снега", -1)
      .setVisible(() -> (this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld"));
   private boolean menuPending = false;
   private int menuTicks = 0;
   private int clickCounter = -1;
   private int targetMenuSlot = -1;
   private boolean expHeld = false;
   private int expSlot = -1;
   private int expOldSlot = -1;
   private int expStep = 0;
   private final StopGPT expTimer = new StopGPT();
   private int oldSlot = -1;
   private int step = 0;
   private final StopGPT timer = new StopGPT();
   private boolean chorusHeld = false;
   private int chorusSlot = -1;
   private int chorusOldSlot = -1;
   private int chorusStep = 0;
   private final StopGPT chorusTimer = new StopGPT();
   private int itemSlot = -1;
   private class_1792 targetItem = null;
   private String targetName = null;
   private boolean underFeetAction = false;
   private boolean isStoppingMovement = false;
   private final Map<class_1792, Float> animations = new HashMap<>();
   private final Map<class_1792, long[]> cdTracker = new ConcurrentHashMap<>();
   private static final int HUD_COLS = 20;
   private static final int HUD_CW = 24;
   private static final int HUD_CH = 24;
   private static final int HUD_GAP = 1;
   private static final float BADGE_PAD_X = 4.0F;
   private static final float BADGE_PAD_Y = 3.0F;
   private static final float BADGE_FONT = 5.0F;
   private final Map<class_1792, Animation> cdAnimations = new HashMap<>();
   private static final class_1792[] CD_ITEMS = new class_1792[]{
      class_1802.field_8449,
      class_1802.field_8479,
      class_1802.field_8543,
      class_1802.field_8551,
      class_1802.field_22021,
      class_1802.field_49098,
      class_1802.field_8614,
      class_1802.field_8137,
      class_1802.field_8882,
      class_1802.field_8662,
      class_1802.field_8814,
      class_1802.field_8639
   };
   private static ClickHelper INSTANCE_STATIC;

   public ClickHelper() {
      INSTANCE_STATIC = this;
   }

   public static boolean isActionActive() {
      return INSTANCE_STATIC != null && INSTANCE_STATIC.isEnabled() && INSTANCE_STATIC.isStoppingMovement;
   }

   public static ClickHelper getInstance() {
      return Instance.get(ClickHelper.class);
   }

   public boolean isNoLeftHandPlace() {
      return this.mode.is("System") && this.noLeftHandPlace.getValue();
   }

   @EventHandler
   public void onKey(EventKey e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.handleBind_2(e.getKey());
      }
   }

   private boolean isPlayerMotionless() {
      if (mc.field_1724 == null) {
         return true;
      } else {
         class_243 vel = mc.field_1724.method_18798();
         double hSpeedSq = vel.field_1350 * vel.field_1350 + vel.field_1352 * vel.field_1352;
         return hSpeedSq < 0.001;
      }
   }

   private void startPearlSwap() {
      this.startAction(class_1802.field_8634);
   }

   private void setKey(boolean state) {
      if (mc.field_1690 != null && mc.method_22683() != null) {
         class_304[] movementKeys = new class_304[]{
            mc.field_1690.field_1894,
            mc.field_1690.field_1881,
            mc.field_1690.field_1913,
            mc.field_1690.field_1849,
            mc.field_1690.field_1903,
            mc.field_1690.field_1867
         };

         for (class_304 keyBinding : movementKeys) {
            boolean pressed = state && class_3675.method_15987(mc.method_22683(), keyBinding.method_1429().method_1444());
            keyBinding.method_23481(pressed);
         }
      }
   }

   private void sendCoords() {
      if (mc.field_1724 != null) {
         mc.field_1724.field_3944.method_45729("/cc " + mc.field_1724.method_31477() + " " + mc.field_1724.method_31478() + " " + mc.field_1724.method_31479());
      }
   }

   @Override
   public void onDisable() {
      if (this.isStoppingMovement) {
         this.setKey(true);
      }

      this.step = 0;
      this.isStoppingMovement = false;
      if (this.chorusStep != 0) {
         this.abortChorus();
      }

      this.chorusHeld = false;
      this.menuPending = false;
      this.menuTicks = 0;
      this.clickCounter = -1;
      super.onDisable();
   }

   @EventHandler
   public void onMoveInput(EventMoveInput event) {
      if (this.isStoppingMovement && mc.field_1724 != null) {
         event.setForward(0.0F);
         event.setStrafe(0.0F);
         event.setInput(class_10185.field_54098);
         mc.field_1724.method_5728(false);
         if (mc.field_1724.field_3913 != null) {
            mc.field_1724.field_3913.field_54155 = class_10185.field_54098;
         }

         class_243 vel = mc.field_1724.method_18798();
         mc.field_1724.method_18800(0.0, vel.field_1351, 0.0);
      }
   }

   private void handleFriendAction() {
      if (AttackAura.target == null) {
         class_239 hitResult = mc.field_1765;
         if (hitResult != null && hitResult.method_17783() == class_240.field_1331) {
            if (((class_3966)hitResult).method_17782() instanceof class_1657 player) {
               String name = player.method_5477().getString();
               if (!Client.get().friendManager().isFriend(name)) {
                  Client.get().friendManager().add(name);
               } else {
                  Client.get().friendManager().remove(name);
               }
            }
         }
      }
   }

   @EventHandler
   public void onCooldownTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.menuPending) {
            if (++this.menuTicks > (int)this.openTimeout.getValue().floatValue()) {
               this.menuTicks = 0;
               this.menuPending = false;
               this.clickCounter = -1;
            } else if (mc.field_1755 instanceof class_465<?> screen) {
               int slot = this.targetMenuSlot;
               if (slot >= 0 && slot < screen.method_17577().field_7761.size()) {
                  if (this.clickCounter < 0) {
                     this.clickCounter = 0;
                  }

                  if (this.clickCounter < (int)this.clickDelay.getValue().floatValue()) {
                     this.clickCounter++;
                  } else {
                     mc.field_1761.method_2906(screen.method_17577().field_7763, slot, 0, class_1713.field_7790, mc.field_1724);
                     mc.field_1724.method_7346();
                     this.menuPending = false;
                     this.menuTicks = 0;
                     this.clickCounter = -1;
                  }
               }
            }
         }

         for (class_1792 item : CD_ITEMS) {
            boolean isCD = mc.field_1724.method_7357().method_7904(item.method_7854());
            long[] info = this.cdTracker.get(item);
            if (isCD) {
               if (info == null) {
                  this.cdTracker.put(item, new long[]{mc.field_1687.method_75260(), -1L});
               } else if (info[1] < 0L) {
                  float progress = mc.field_1724.method_7357().method_7905(item.method_7854(), 0.0F);
                  long elapsed = mc.field_1687.method_75260() - info[0];
                  long total;
                  if (elapsed > 0L && progress > 0.0F && progress < 1.0F && (total = (long)Math.round((float)elapsed / (1.0F - progress))) > 0L) {
                     info[1] = total;
                  }
               }
            } else if (info != null) {
               this.cdTracker.remove(item);
            }
         }
      }
   }

   private void handleBind_2(int key) {
      if (key == this.friend.get() && key != -1) {
         this.handleFriendAction();
      }

      if (key == this.coordsKey.get() && key != -1) {
         this.sendCoords();
      }

      if (this.mode.is("System")) {
         if (this.arenaTrigger.get() != -1 && key == this.arenaTrigger.get()) {
            if (mc.field_1724 != null) {
               mc.field_1724.field_3944.method_45730("arena");
               this.menuPending = true;
               this.menuTicks = 0;
               this.clickCounter = -1;
               this.targetMenuSlot = (int)this.arenaSlot.getValue().floatValue() - 1;
            }

            return;
         }

         if (this.investTrigger.get() != -1 && key == this.investTrigger.get()) {
            if (mc.field_1724 != null) {
               mc.field_1724.field_3944.method_45730("invest");
            }

            return;
         }

         if (this.storageTrigger.get() != -1 && key == this.storageTrigger.get()) {
            if (mc.field_1724 != null) {
               mc.field_1724.field_3944.method_45730("storage");
            }

            return;
         }
      }

      if (key != -1 && this.step == 0 && this.chorusSlot == -1) {
         class_1792 toUse = null;
         if (!this.mode.is("System") && (!this.mode.is("Lunarix") && !this.mode.is("Inventory") || !this.type.is("FunTime"))) {
            if ((this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld")) {
               if (key == this.holyStunKey.get()) {
                  toUse = class_1802.field_8137;
               } else if (key == this.holyTrapKey.get()) {
                  toUse = class_1802.field_8882;
               } else if (key == this.holyExplosiveTrapKey.get()) {
                  toUse = class_1802.field_8662;
               } else if (key == this.holyExplosiveKey.get()) {
                  toUse = class_1802.field_8814;
               } else if (key == this.holySnowKey.get()) {
                  toUse = class_1802.field_8543;
               }

               if (toUse != null) {
                  this.underFeetAction = false;
                  this.startAction(toUse);
               }
            }
         } else {
            if (key == this.sugarKey.get()) {
               toUse = class_1802.field_8479;
            } else if (key == this.eyeKey.get()) {
               toUse = class_1802.field_8449;
            } else if (key == this.snowKey.get()) {
               toUse = class_1802.field_8543;
            } else if (key == this.plastKey.get()) {
               toUse = class_1802.field_8551;
            } else if (key == this.trapKey.get()) {
               toUse = class_1802.field_22021;
            } else if (key == this.windKey.get()) {
               toUse = class_1802.field_49098;
            } else if (key == this.fireChargeKey.get()) {
               toUse = class_1802.field_8814;
            } else if (key == this.bojAurakey.get()) {
               toUse = class_1802.field_8614;
            } else if (key == this.popperKey.get()) {
               toUse = class_1802.field_8639;
            }

            boolean throwUnderFeet = !this.mode.is("System") || !this.buffThrowMode.is("По прицелу");
            if (key == this.keyHilka.get()) {
               this.underFeetAction = throwUnderFeet;
               this.startActionByName("Святая вода");
               return;
            }

            if (key == this.keySerka.get()) {
               this.underFeetAction = false;
               this.startActionByName("Зелье Радиации");
               return;
            }

            if (key == this.palladinKey.get()) {
               this.underFeetAction = throwUnderFeet;
               this.startActionByName("Зелье Палладина");
               return;
            }

            if (key == this.assassinKey.get()) {
               this.underFeetAction = throwUnderFeet;
               this.startActionByName("Зелье Ассасина");
               return;
            }

            if (key == this.sleepKey.get()) {
               this.underFeetAction = false;
               this.startActionByName("Снотворное");
               return;
            }

            if (key == this.angerKey.get()) {
               this.underFeetAction = throwUnderFeet;
               this.startActionByName("Зелье Гнева");
               return;
            }

            if (toUse != null) {
               this.underFeetAction = false;
               this.startAction(toUse);
            }
         }
      }
   }

   private void handleExpKey() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1755 == null) {
         int ek = this.expKey.get();
         if (ek == -1) {
            if (this.expStep != 0) {
               this.abortExp();
            }

            this.expHeld = false;
         } else {
            boolean nowPressed = class_3675.method_15987(mc.method_22683(), ek);
            if (!this.expHeld && nowPressed && this.step == 0 && this.expStep == 0) {
               int slot = -1;

               for (int i = 0; i < 36; i++) {
                  if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8287) {
                     slot = i;
                     break;
                  }
               }

               if (slot == -1) {
                  mc.field_1724.method_7353(class_2561.method_43470("Опыт не найден!").method_27692(class_124.field_1061), true);
               } else {
                  this.expSlot = slot;
                  this.expOldSlot = mc.field_1724.method_31548().method_67532();
                  this.expStep = 1;
                  this.expTimer.reset();
               }
            }

            this.expHeld = nowPressed;
            if (this.expStep == 2 && !nowPressed) {
               this.expStep = 3;
               this.expTimer.reset();
            }

            switch (this.expStep) {
               case 1:
                  if (this.expSlot > 9) {
                     this.setKey(false);
                  }

                  if (this.expTimer.hasTimePassed(50L)) {
                     if (this.expSlot < 9) {
                        mc.field_1724.method_31548().method_61496(this.expSlot);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     } else {
                        this.setKey(true);
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.expSlot, this.expOldSlot, class_1713.field_7791, mc.field_1724);
                     }

                     this.expStep = 2;
                  }
                  break;
               case 2:
                  mc.field_1690.field_1904.method_23481(true);
                  RotationProcess.update(new Rotation(FreeLookUtil.freeYaw, 90.0F), 90.0F, 90.0F, 2, 25);
                  break;
               case 3:
                  mc.field_1690.field_1904.method_23481(false);
                  if (this.expSlot > 9) {
                     this.setKey(false);
                  }

                  if (this.expTimer.hasTimePassed(50L)) {
                     if (this.expSlot < 9) {
                        mc.field_1724.method_31548().method_61496(this.expOldSlot);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     } else {
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.expSlot, this.expOldSlot, class_1713.field_7791, mc.field_1724);
                        mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                        mc.field_1724.method_7346();
                        this.setKey(true);
                     }

                     this.expSlot = -1;
                     this.expStep = 0;
                  }
            }
         }
      }
   }

   private void abortExp() {
      if (mc.field_1724 != null && this.expSlot != -1) {
         if (this.expSlot < 9) {
            mc.field_1724.method_31548().method_61496(this.expOldSlot);
            ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
         } else {
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.expSlot, this.expOldSlot, class_1713.field_7791, mc.field_1724);
            mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
            mc.field_1724.method_7346();
         }

         this.setKey(true);
      }

      this.expSlot = -1;
      this.expStep = 0;
   }

   private void startAction(class_1792 item) {
      if (mc.field_1724 != null) {
         this.targetName = null;
         this.targetItem = item;
         this.findSlotAndStart();
      }
   }

   private void startActionByName(String name) {
      if (mc.field_1724 != null) {
         this.targetItem = null;
         this.targetName = name.toLowerCase();
         this.findSlotAndStart();
      }
   }

   private boolean isWindCharge() {
      if (this.targetItem == class_1802.field_49098) {
         return true;
      } else if (this.targetName == null) {
         return false;
      } else {
         String lower = this.targetName.toLowerCase();
         return lower.contains("ветер") || lower.contains("ветра") || lower.contains("порыв") || lower.contains("wind");
      }
   }

   private boolean isFireCharge() {
      if (this.targetItem == class_1802.field_8814) {
         return true;
      } else if (this.targetName == null) {
         return false;
      } else {
         String lower = this.targetName.toLowerCase();
         return lower.contains("огненный шар") || lower.contains("огонь") || lower.contains("шар") || lower.contains("fire_charge");
      }
   }

   private boolean isPopper() {
      if (this.targetItem == class_1802.field_8639) {
         return true;
      } else if (this.targetName == null) {
         return false;
      } else {
         String lower = this.targetName.toLowerCase();
         return lower.contains("хлопушка") || lower.contains("хлопушк") || lower.contains("popper") || lower.contains("firework");
      }
   }

   private boolean matchesTarget(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         if (this.targetName != null) {
            String stackName = stack.method_7964().getString().toLowerCase();
            if (stackName.contains(this.targetName)) {
               return true;
            }

            if (this.targetName.contains("святая") && stackName.contains("свят")) {
               return true;
            }

            if (this.targetName.contains("радиаци") && stackName.contains("радиац")) {
               return true;
            }

            if (this.targetName.contains("палладин") && stackName.contains("палладин")) {
               return true;
            }

            if (this.targetName.contains("ассасин") && stackName.contains("ассасин")) {
               return true;
            }

            if (this.targetName.contains("снотворн") && stackName.contains("снотвор")) {
               return true;
            }

            if (this.targetName.contains("гнев") && stackName.contains("гнев")) {
               return true;
            }
         }

         if (this.targetItem != null) {
            if (stack.method_7909() == this.targetItem) {
               return true;
            }

            if (this.isWindCharge()) {
               String name = stack.method_7964().getString().toLowerCase();
               if (stack.method_31574(class_1802.field_49098) || name.contains("ветер") || name.contains("ветра") || name.contains("wind")) {
                  return true;
               }
            }

            if (this.isFireCharge()) {
               String name = stack.method_7964().getString().toLowerCase();
               if (stack.method_31574(class_1802.field_8814) || name.contains("огненный шар") || name.contains("огонь") || name.contains("шар")) {
                  return true;
               }
            }

            if (this.isPopper()) {
               String name = stack.method_7964().getString().toLowerCase();
               if (stack.method_31574(class_1802.field_8639) || name.contains("хлопушк")) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean isBlockInteractItem() {
      if (this.targetItem == class_1802.field_8551 || this.targetItem == class_1802.field_22021) {
         return true;
      } else if (this.targetName == null) {
         return false;
      } else {
         String lower = this.targetName.toLowerCase();
         return lower.contains("пласт") || lower.contains("трапк") || lower.contains("trap");
      }
   }

   private boolean isThrowUnderFeet() {
      return this.underFeetAction ? true : this.mode.is("System") && this.buffThrowMode.is("Под себя");
   }

   private void executeUseAction(class_1268 hand) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         float yaw = mc.field_1724.method_36454();
         float pitch = mc.field_1724.method_36455();
         boolean underFeet = this.isThrowUnderFeet();
         float targetPitch = underFeet ? 89.9F : pitch;
         if (this.isWindCharge()) {
            boolean under = !this.mode.is("System") || this.windChargeMode.is("Под себя");
            targetPitch = under ? 90.0F : pitch;
            if (under) {
               underFeet = true;
               // Автоматичний підскок при використанні під себе на землі
               if (mc.field_1724.method_24828()) {
                  mc.field_1724.method_6043();
               }
            }
         }

         float prevPlayerPitch = mc.field_1724.method_36455();
         if (underFeet) {
            mc.field_1724.method_36457(targetPitch);
            mc.field_1724.field_3944.method_52787(new class_2831(yaw, targetPitch, mc.field_1724.method_24828(), false));
         }

         mc.field_1761.method_2919(mc.field_1724, hand);
         if (mc.field_1765 instanceof class_3965 blockHit && this.isBlockInteractItem()) {
            mc.field_1761.method_2896(mc.field_1724, hand, blockHit);
         }

         mc.field_1724.method_6104(hand);
         if (underFeet) {
            mc.field_1724.method_36457(prevPlayerPitch);
            mc.field_1724.field_3944.method_52787(new class_2831(yaw, prevPlayerPitch, mc.field_1724.method_24828(), false));
         }
      }
   }

   private void findSlotAndStart() {
      this.itemSlot = -1;
      if (mc.field_1724 != null) {
         class_1799 offhandStack = mc.field_1724.method_6079();
         if (this.matchesTarget(offhandStack)) {
            this.itemSlot = 40;
            this.executeUseAction(class_1268.field_5810);
            this.step = 0;
            this.targetItem = null;
            this.targetName = null;
            this.underFeetAction = false;
         } else {
            for (int i = 0; i < 9; i++) {
               class_1799 stack = mc.field_1724.method_31548().method_5438(i);
               if (this.matchesTarget(stack)) {
                  this.itemSlot = i;
                  break;
               }
            }

            if (this.itemSlot == -1) {
               for (int ix = 9; ix < 36; ix++) {
                  class_1799 stack = mc.field_1724.method_31548().method_5438(ix);
                  if (this.matchesTarget(stack)) {
                     this.itemSlot = ix;
                     break;
                  }
               }
            }

            if (this.itemSlot == -1) {
               String msg = this.targetName != null ? this.targetName : (this.targetItem != null ? this.targetItem.method_63680().getString() : "предмет");
               mc.field_1724.method_7353(class_2561.method_43470("Предмет [" + msg + "] не найден!").method_27692(class_124.field_1061), true);
               this.underFeetAction = false;
            } else if (this.itemSlot < 9) {
               this.isStoppingMovement = false;
               int prev = mc.field_1724.method_31548().method_67532();
               this.oldSlot = prev;
               if (this.itemSlot != prev) {
                  mc.field_1724.method_31548().method_61496(this.itemSlot);
                  ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
               }

               this.executeUseAction(class_1268.field_5808);
               if (this.itemSlot != prev) {
                  mc.field_1724.method_31548().method_61496(prev);
                  ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
               }

               this.finishAction();
            } else {
               this.oldSlot = mc.field_1724.method_31548().method_67532();
               this.isStoppingMovement = true;
               this.setKey(false);
               if (mc.field_1724 != null) {
                  class_243 vel = mc.field_1724.method_18798();
                  mc.field_1724.method_18800(0.0, vel.field_1351, 0.0);
                  mc.field_1724.method_5728(false);
               }

               if (this.itemSlot == this.oldSlot) {
                  this.timer.reset();
                  this.step = 2;
               } else {
                  this.timer.reset();
                  this.step = 1;
               }
            }
         }
      }
   }

   private BindSetting[] hudKeys() {
      BindSetting[] bindSettingArray;
      if ((this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld")) {
         BindSetting[] bindSettingArray2 = new BindSetting[]{
            this.holyStunKey, this.holyTrapKey, this.holyExplosiveTrapKey, this.holyExplosiveKey, this.holySnowKey, this.chorusKey
         };
         bindSettingArray = bindSettingArray2;
      } else {
         BindSetting[] bindSettingArray3 = new BindSetting[]{
            this.eyeKey,
            this.sugarKey,
            this.snowKey,
            this.plastKey,
            this.trapKey,
            this.windKey,
            this.fireChargeKey,
            this.bojAurakey,
            this.popperKey,
            this.chorusKey
         };
         bindSettingArray = bindSettingArray3;
      }

      return bindSettingArray;
   }

   private class_1792[] hudItems() {
      class_1792[] class_1792Array;
      if ((this.mode.is("Lunarix") || this.mode.is("Inventory")) && this.type.is("HolyWorld")) {
         class_1792[] class_1792Array2 = new class_1792[]{
            class_1802.field_8137, class_1802.field_8882, class_1802.field_8662, class_1802.field_8814, class_1802.field_8543, class_1802.field_8233
         };
         class_1792Array = class_1792Array2;
      } else {
         class_1792[] class_1792Array3 = new class_1792[]{
            class_1802.field_8449,
            class_1802.field_8479,
            class_1802.field_8543,
            class_1802.field_8551,
            class_1802.field_22021,
            class_1802.field_49098,
            class_1802.field_8814,
            class_1802.field_8614,
            class_1802.field_8639,
            class_1802.field_8233
         };
         class_1792Array = class_1792Array3;
      }

      return class_1792Array;
   }

   private List<Integer> hudActive(BindSetting[] keys) {
      ArrayList<Integer> active = new ArrayList<>();

      for (int i = 0; i < keys.length; i++) {
         if (keys[i].get() != -1) {
            active.add(i);
         }
      }

      return active;
   }

   private float badgeWidth(int key) {
      return Fonts.sf_regular.getWidth(Keyboard.keyName(key), 5.0F) + 8.0F;
   }

   private float[] hudOffsets(List<Integer> active, BindSetting[] keys) {
      float[] xs = new float[active.size()];
      float x = 0.0F;

      for (int n = 0; n < active.size(); n++) {
         if (n % 20 == 0) {
            x = 0.0F;
         } else {
            float prev = this.badgeWidth(keys[active.get(n - 1)].get());
            float cur = this.badgeWidth(keys[active.get(n)].get());
            x += Math.max(24.0F, (prev + cur) / 2.0F) + 1.0F;
         }

         xs[n] = x;
      }

      return xs;
   }

   public Vector2f getHudSize() {
      BindSetting[] keys = this.hudKeys();
      List<Integer> active = this.hudActive(keys);
      if (active.isEmpty()) {
         return new Vector2f(0.0F, 0.0F);
      } else {
         float S = InterFace.getInstance().sizeHud.getValue();
         float[] xs = this.hudOffsets(active, keys);
         float scaledHUD_CW = 24.0F * S;
         float scaledHUD_CH = 24.0F * S;
         float scaledHUD_GAP = 1.0F * S;
         float width = 0.0F;

         for (int n = 0; n < active.size(); n++) {
            float bw = this.badgeWidth(keys[active.get(n)].get()) * S;
            float scaledX = xs[n] * S;
            width = Math.max(width, scaledX + Math.max(scaledHUD_CW, scaledHUD_CW / 2.0F + bw / 2.0F));
         }

         int rows = (active.size() + 20 - 1) / 20;
         return new Vector2f(width, (float)rows * scaledHUD_CH + (float)(rows - 1) * scaledHUD_GAP);
      }
   }

   public void renderHud(class_332 ctx, float sx, float sy) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         BindSetting[] keys = this.hudKeys();
         class_1792[] its = this.hudItems();
         List<Integer> active = this.hudActive(keys);
         if (!active.isEmpty()) {
            float S = InterFace.getInstance().sizeHud.getValue();
            float CW = 20.0F * S;
            float CH = 20.0F * S;
            float TH = 0.0F * S;
            float GAP = 1.0F * S;
            float[] xs = this.hudOffsets(active, keys);

            for (int n = 0; n < active.size(); n++) {
               int idx = active.get(n);
               int key = keys[idx].get();
               class_1792 item = its[idx];
               int row = n / 20;
               float cx = sx + xs[n] * S;
               float cy = sy + (float)row * (CH + TH + GAP);
               RenderUtil.Render2D.glow(cx, cy, CW, CH - 0.5F * S, ColorUtil.getColor(0, 0.1F), 5.0F * S, 12.0F * S, 1.0F);
               RenderUtil.Blur.blur(cx, cy, CW, CH, 1.0F, 5.0F * S, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue()));
               Animation cdAnim = this.cdAnimations.computeIfAbsent(item, i -> new Animation());
               cdAnim.update();
               cdAnim.run((double)mc.field_1724.method_7357().method_7905(item.method_7854(), 0.0F), 0.12F, Easings.LINEAR);
               float fill = MathUtil.clamp(cdAnim.get(), 0.0F, 1.0F);
               if (fill > 0.001F) {
                  float fh = CH * fill;
                  float top = 5.0F * S * MathUtil.clamp((fill - 0.85F) / 0.15F, 0.0F, 1.0F);
                  RenderUtil.Render2D.rect(cx, cy + CH - fh, CW, fh, ColorUtil.getColor(80, 10, 10, 0.7F), top, top, 5.0F * S, 5.0F * S);
               }

               float targetScale = 2.0F;
               float currentScale = (float)mc.method_22683().method_4495();
               float scaleFix = targetScale / currentScale;
               ctx.method_51448().pushMatrix();
               ctx.method_51448().translate((cx + CW / 2.0F) * scaleFix, (cy + CH / 2.0F - 1.0F * S) * scaleFix);
               ctx.method_51448().scale(scaleFix * 0.5F * S, scaleFix * 0.5F * S);
               ctx.method_51427(new class_1799(item), -8, -9);
               ctx.method_51448().popMatrix();
               String cnt = "x" + this.countItem(item);
               float cntSize = 5.0F * S;
               float cntW = Fonts.sf_regular.getWidth(cnt, cntSize);
               Fonts.sf_regular.draw(cnt, cx + CW - cntW - 3.0F * S, cy + CH - 8.0F * S, cntSize, ThemeColor.getTextColor());
               float ty = cy + CH + GAP;
               RenderUtil.Render2D.rect(cx, ty, CW, TH, -870835936, 2.0F * S);
               boolean onCD = mc.field_1724.method_7357().method_7904(item.method_7854());
               float remain = 0.0F;
               float maxSec = 0.0F;
               long[] info;
               if (onCD && (info = this.cdTracker.get(item)) != null && info[1] > 0L) {
                  float progress = mc.field_1724.method_7357().method_7905(item.method_7854(), 0.0F);
                  maxSec = (float)info[1] / 20.0F;
                  remain = progress * maxSec;
               }

               String keyText = Keyboard.keyName(key);
               float scaledBadgeFont = 5.0F * S;
               float textHeight = Fonts.sf_regular.getHeight(scaledBadgeFont);
               float CW2 = this.badgeWidth(key) * S;
               float CH2 = textHeight + 6.0F * S;
               float rectX = cx - CW2 / 2.0F + CW / 2.0F;
               float rectY = cy - CH2 / 2.0F - 9.0F * S;
               RenderUtil.Render2D.glow(rectX, rectY, CW2, CH2 - 0.5F * S, ColorUtil.getColor(0, 0.1F), 4.0F * S, 7.0F * S, 1.0F);
               RenderUtil.Blur.blur(
                  rectX, rectY, CW2, CH2, 1.0F, 4.0F * S, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue())
               );
               Fonts.sf_regular.drawCentered(keyText, cx + CW / 2.0F, cy - textHeight / 2.0F - 9.0F * S, scaledBadgeFont, ThemeColor.getTextColor());
            }
         }
      }
   }

   private String keyShortName(int key) {
      if (key <= 0) {
         return "";
      } else {
         try {
            String name = class_307.field_1668.method_1447(key).method_27445().getString();
            return (name.length() <= 3 ? name : String.valueOf(name.charAt(0))).toUpperCase();
         } catch (Exception var3) {
            return "?";
         }
      }
   }

   private int countItem(class_1792 item) {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int n = 0;

         for (int i = 0; i < 36; i++) {
            class_1799 s = mc.field_1724.method_31548().method_5438(i);
            if (s.method_7909() == item) {
               n += s.method_7947();
            }
         }

         return n;
      }
   }

   private void handleChorusKey() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1755 == null) {
         int ck = this.chorusKey.get();
         if (ck == -1) {
            if (this.chorusStep != 0) {
               this.abortChorus();
            }

            this.chorusHeld = false;
         } else {
            boolean nowPressed = class_3675.method_15987(mc.method_22683(), ck);
            if (!this.chorusHeld && nowPressed && this.step == 0 && this.chorusStep == 0) {
               int slot = -1;

               for (int i = 0; i < 36; i++) {
                  if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8233) {
                     slot = i;
                     break;
                  }
               }

               if (slot == -1) {
                  mc.field_1724.method_7353(class_2561.method_43470("Хорус не найден!").method_27692(class_124.field_1061), true);
               } else {
                  this.chorusSlot = slot;
                  this.chorusOldSlot = mc.field_1724.method_31548().method_67532();
                  this.chorusStep = 1;
                  this.chorusTimer.reset();
               }
            }

            this.chorusHeld = nowPressed;
            if (this.chorusStep == 2 && !nowPressed) {
               this.chorusStep = 3;
               this.chorusTimer.reset();
            }

            switch (this.chorusStep) {
               case 1:
                  if (this.chorusSlot > 9) {
                     this.setKey(false);
                  }

                  if (this.chorusTimer.hasTimePassed(50L)) {
                     if (this.chorusSlot < 9) {
                        mc.field_1724.method_31548().method_61496(this.chorusSlot);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     } else {
                        this.setKey(true);
                        mc.field_1761
                           .method_2906(mc.field_1724.field_7498.field_7763, this.chorusSlot, this.chorusOldSlot, class_1713.field_7791, mc.field_1724);
                     }

                     this.chorusStep = 2;
                  }
                  break;
               case 2:
                  mc.field_1690.field_1904.method_23481(true);
                  break;
               case 3:
                  mc.field_1690.field_1904.method_23481(false);
                  if (this.chorusSlot > 9) {
                     this.setKey(false);
                  }

                  if (this.chorusTimer.hasTimePassed(50L)) {
                     if (this.chorusSlot < 9) {
                        mc.field_1724.method_31548().method_61496(this.chorusOldSlot);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     } else {
                        mc.field_1761
                           .method_2906(mc.field_1724.field_7498.field_7763, this.chorusSlot, this.chorusOldSlot, class_1713.field_7791, mc.field_1724);
                        mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                        mc.field_1724.method_7346();
                        this.setKey(true);
                     }

                     this.chorusSlot = -1;
                     this.chorusStep = 0;
                  }
            }
         }
      }
   }

   private void abortChorus() {
      if (mc.field_1724 != null && this.chorusSlot != -1) {
         if (this.chorusSlot < 9) {
            mc.field_1724.method_31548().method_61496(this.chorusOldSlot);
            ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
         } else {
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.chorusSlot, this.chorusOldSlot, class_1713.field_7791, mc.field_1724);
            mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
            mc.field_1724.method_7346();
         }

         this.setKey(true);
      }

      this.chorusSlot = -1;
      this.chorusStep = 0;
   }

   @EventHandler
   public void onUpdate2(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         this.handleChorusKey();
         this.handleExpKey();
         if (this.step != 0) {
            AttackAura.stoptick = 3;
            this.isStoppingMovement = true;
            this.setKey(false);
            if (mc.field_1724 != null) {
               class_243 vel = mc.field_1724.method_18798();
               mc.field_1724.method_18800(0.0, vel.field_1351, 0.0);
               mc.field_1724.method_5728(false);
            }

            if (!this.isPlayerMotionless()) {
               this.timer.reset();
            } else {
               long delay = this.mode.is("Inventory") ? (long)this.invActionDelay.getValue().floatValue() : 50L;
               if (this.timer.hasTimePassed(Math.max(30L, delay))) {
                  this.timer.reset();
                  switch (this.step) {
                     case 1:
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.itemSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                        this.step = 2;
                        break;
                     case 2:
                        this.executeUseAction(class_1268.field_5808);
                        this.step = 3;
                        break;
                     case 3:
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.itemSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                        this.step = 4;
                        break;
                     case 4:
                        mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                        mc.field_1724.method_7346();
                        if (this.oldSlot != -1 && mc.field_1724.method_31548().method_67532() != this.oldSlot) {
                           mc.field_1724.method_31548().method_61496(this.oldSlot);
                        }

                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                        this.finishAction();
                  }
               }
            }
         }
      }
   }

   private void finishAction() {
      this.step = 0;
      this.itemSlot = -1;
      this.oldSlot = -1;
      this.targetItem = null;
      this.targetName = null;
      this.underFeetAction = false;
      this.isStoppingMovement = false;
      this.setKey(true);
   }
}
