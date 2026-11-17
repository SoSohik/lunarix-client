package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2815;
import net.minecraft.class_2886;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_490;
import net.minecraft.class_2828.class_2831;
import org.lwjgl.glfw.GLFW;
import ru.mentions.lang.Lang;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.MousePressEvent;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.mixin.HandledScreenAccessor;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.render.SwingAnimation;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.math.StopGPT;
import ru.mentions.utils.notification.NotificationManager;

@ModuleInfo(
   name = "ClickPearl",
   desc = "Быстрый бросок эндер-перла без задержек и рывков",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class ClickPearlModule extends Module {
   public final BindSetting key = new BindSetting(this, "Клавиша", -1);
   public final BooleanSetting middleClick = new BooleanSetting(this, "По нажатию на колесико", true);
   public final ModeSetting soundMode = new ModeSetting(
      this,
      "Звук уведомления",
      "Bell",
      "Tone",
      "Notify",
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
      "Pop",
      "По умолчанию",
      "Выкл"
   );
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Мгновенный", "Инвентарный", "С автоповоротом");
   public final SliderSetting invDelay = new SliderSetting(this, "Задержка инвентаря (мс)", 50.0F, 10.0F, 300.0F, 10.0F)
      .setVisible(() -> this.mode.is("Инвентарный"));
   public final BooleanSetting invPitch = new BooleanSetting(this, "Автоповорот", false).setVisible(() -> this.mode.is("Инвентарный"));
   public final SliderSetting customPitch = new SliderSetting(this, "Угол броска", 0.0F, -90.0F, 90.0F, 5.0F)
      .setVisible(() -> this.mode.is("С автоповоротом") || this.mode.is("Инвентарный") && this.invPitch.getValue());
   public final BooleanSetting silent = new BooleanSetting(this, "Бесшумный поворот", true)
      .setVisible(() -> this.mode.is("С автоповоротом") || this.mode.is("Инвентарный") && this.invPitch.getValue());
   private int step = 0;
   private int invStep = 0;
   private int pearlSlot = -1;
   private int oldSlot = -1;
   private final StopGPT timer = new StopGPT();
   private boolean toggleTriggered = false;

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.reset();
   }

   @Override
   protected void onEnable() {
      super.onEnable();
      if (mc.field_1724 != null) {
         this.toggleTriggered = true;
         this.throwPearl();
      }
   }

   @Override
   protected void onDisable() {
      this.reset();
      super.onDisable();
   }

   @EventHandler
   public void onKey(EventKey event) {
      if (mc.field_1724 != null && mc.field_1755 == null) {
         int bound = this.key.get() != null ? this.key.get() : -1;
         if (bound > 0 && event.getKey() == bound && this.step == 0 && this.invStep == 0) {
            this.toggleTriggered = false;
            this.throwPearl();
         }
      }
   }

   @EventHandler
   public void onMouse(MousePressEvent event) {
      if (mc.field_1724 != null && mc.field_1755 == null) {
         if (this.middleClick.getValue() && event.getButton() == 2 && this.step == 0 && this.invStep == 0) {
            this.toggleTriggered = false;
            this.throwPearl();
         }
      }
   }

   public void throwPearl() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         class_1799 pearlStack = new class_1799(class_1802.field_8634);
         if (mc.field_1724.method_7357().method_7904(pearlStack)) {
            NotificationManager.send(Lang.isUkrainian() ? "Перл на відкаті!" : "Перл на откате!", NotificationManager.Type.INFO);
            this.finishThrow();
         } else {
            int slot = this.findPearlSlot();
            if (slot == -1) {
               NotificationManager.sendWithSound(
                  Lang.isUkrainian() ? "Ендер-перл не знайдено!" : "Эндер-перл не найден в инвентаре!",
                  NotificationManager.Type.INFO,
                  this.soundMode.getValue()
               );
               this.finishThrow();
            } else if (slot == 40) {
               this.executeThrowAction(class_1268.field_5810);
               this.finishThrow();
            } else {
               this.oldSlot = mc.field_1724.method_31548().method_67532();
               if (slot < 9 && slot == this.oldSlot) {
                  this.executeThrowAction(class_1268.field_5808);
                  this.finishThrow();
               } else if (this.mode.is("Инвентарный") && slot >= 9 && slot < 36) {
                  this.pearlSlot = slot;
                  this.oldSlot = mc.field_1724.method_31548().method_67532();
                  this.invStep = 1;
                  this.timer.reset();
                  if (mc.field_1724 != null) {
                     mc.field_1724.method_5728(false);
                     class_243 vel = mc.field_1724.method_18798();
                     mc.field_1724.method_18800(0.0, vel.field_1351, 0.0);
                  }

                  this.setKey(false);
                  AttackAura.stoptick = 3;
               } else {
                  this.pearlSlot = slot;
                  this.step = 1;
                  this.timer.reset();
                  AttackAura.stoptick = 3;
               }
            }
         }
      } else {
         this.finishThrow();
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.invStep > 0) {
            AttackAura.stoptick = 3;
            long delay = (long)this.invDelay.getValue().floatValue();
            if (this.timer.hasTimePassed(delay)) {
               this.timer.reset();
               switch (this.invStep) {
                  case 1:
                     if (mc.field_1755 == null) {
                        mc.method_1507(new class_490(mc.field_1724));
                     }

                     this.invStep = 2;
                     break;
                  case 2:
                     if (mc.field_1755 instanceof class_490 invScreen) {
                        this.aimCursorAtSlot(invScreen, this.pearlSlot);
                     }

                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.pearlSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                     mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     if (mc.field_1755 instanceof class_490) {
                        mc.field_1724.method_7346();
                        mc.method_1507(null);
                     }

                     this.invStep = 3;
                     break;
                  case 3:
                     this.executeThrowAction(class_1268.field_5808);
                     this.invStep = 4;
                     break;
                  case 4:
                     if (mc.field_1755 == null) {
                        class_490 invScreen = new class_490(mc.field_1724);
                        mc.method_1507(invScreen);
                        this.aimCursorAtSlot(invScreen, this.pearlSlot);
                     }

                     this.invStep = 5;
                     break;
                  case 5:
                     if (mc.field_1755 instanceof class_490 invScreen) {
                        this.aimCursorAtSlot(invScreen, this.pearlSlot);
                     }

                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.pearlSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                     mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     if (mc.field_1755 instanceof class_490) {
                        mc.field_1724.method_7346();
                        mc.method_1507(null);
                     }

                     this.setKey(true);
                     this.reset();
                     this.finishThrow();
                     break;
                  default:
                     this.setKey(true);
                     this.reset();
                     this.finishThrow();
               }
            }
         } else if (this.step != 0) {
            AttackAura.stoptick = 3;
            if (this.timer.hasTimePassed(50L)) {
               this.timer.reset();
               switch (this.step) {
                  case 1:
                     if (this.pearlSlot < 9) {
                        mc.field_1724.method_31548().method_61496(this.pearlSlot);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     } else {
                        mc.field_1761.method_2906(0, this.pearlSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     }

                     this.step = 2;
                     break;
                  case 2:
                     this.executeThrowAction(class_1268.field_5808);
                     this.step = 3;
                     break;
                  case 3:
                     if (this.pearlSlot < 9) {
                        if (this.oldSlot != -1 && this.oldSlot != this.pearlSlot) {
                           mc.field_1724.method_31548().method_61496(this.oldSlot);
                           ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                        }
                     } else {
                        mc.field_1761.method_2906(0, this.pearlSlot, this.oldSlot, class_1713.field_7791, mc.field_1724);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     }

                     this.step = 4;
                     break;
                  case 4:
                     if (this.pearlSlot < 9 && this.oldSlot != -1 && mc.field_1724.method_31548().method_67532() != this.oldSlot) {
                        mc.field_1724.method_31548().method_61496(this.oldSlot);
                     }

                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     this.reset();
                     this.finishThrow();
                     break;
                  default:
                     this.reset();
                     this.finishThrow();
               }
            }
         }
      }
   }

   private void aimCursorAtSlot(class_490 invScreen, int targetSlotId) {
      try {
         class_1735 targetSlotObj = null;

         for (class_1735 s : mc.field_1724.field_7498.field_7761) {
            if (s.field_7874 == targetSlotId) {
               targetSlotObj = s;
               break;
            }
         }

         if (targetSlotObj != null) {
            int guiX = ((HandledScreenAccessor)invScreen).getScreenX();
            int guiY = ((HandledScreenAccessor)invScreen).getScreenY();
            double scale = (double)mc.method_22683().method_4495();
            double cursorX = (double)(guiX + targetSlotObj.field_7873 + 8) * scale;
            double cursorY = (double)(guiY + targetSlotObj.field_7872 + 8) * scale;
            GLFW.glfwSetCursorPos(mc.method_22683().method_4490(), cursorX, cursorY);
         }
      } catch (Throwable var12) {
      }
   }

   private void executeThrowAction(class_1268 hand) {
      if (mc.field_1724 != null) {
         float yaw = mc.field_1724.method_36454();
         float currentPitch = mc.field_1724.method_36455();
         boolean needRotation = this.mode.is("С автоповоротом") || this.mode.is("Инвентарный") && this.invPitch.getValue();
         if (needRotation) {
            float pitch = this.customPitch.getValue();
            if (this.silent.getValue()) {
               mc.field_1724.field_3944.method_52787(new class_2831(yaw, pitch, mc.field_1724.method_24828(), false));
               SwingAnimation.sendSequencedPacket(id -> new class_2886(hand, id, yaw, pitch));
               mc.field_1724.method_6104(hand);
               mc.field_1724.field_3944.method_52787(new class_2831(yaw, currentPitch, mc.field_1724.method_24828(), false));
            } else {
               mc.field_1724.method_36457(pitch);
               SwingAnimation.interactItem(hand);
            }
         } else {
            SwingAnimation.interactItem(hand);
         }
      }
   }

   private void finishThrow() {
      if (this.toggleTriggered) {
         this.toggleTriggered = false;
         this.setEnabled(false, false);
      }
   }

   private void reset() {
      if (mc.field_1755 instanceof class_490 && this.invStep > 0) {
         if (mc.field_1724 != null) {
            mc.field_1724.method_7346();
         }

         mc.method_1507(null);
      }

      if (this.invStep > 0) {
         this.setKey(true);
      }

      this.step = 0;
      this.invStep = 0;
      this.pearlSlot = -1;
      this.oldSlot = -1;
      this.timer.reset();
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

   private boolean isPearl(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      } else if (stack.method_31574(class_1802.field_8634)) {
         return true;
      } else {
         String name = stack.method_7964().getString().toLowerCase();
         return name.contains("перл") || name.contains("pearl");
      }
   }

   private int findPearlSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else if (this.isPearl(mc.field_1724.method_6079())) {
         return 40;
      } else {
         class_1661 inv = mc.field_1724.method_31548();

         for (int i = 0; i < 9; i++) {
            if (this.isPearl(inv.method_5438(i))) {
               return i;
            }
         }

         for (int ix = 9; ix < 36; ix++) {
            if (this.isPearl(inv.method_5438(ix))) {
               return ix;
            }
         }

         return -1;
      }
   }
}
