package ru.mentions.module.impl.combat;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2815;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_490;
import org.lwjgl.glfw.GLFW;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.mixin.HandledScreenAccessor;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.movement.Sprint;
import ru.mentions.utils.math.InvUtil;
import ru.mentions.utils.math.StopWatchShadow;
import ru.mentions.utils.notification.NotificationManager;

@ModuleInfo(
   name = "Auto Swap",
   category = Category.COMBAT,
   desc = "Свапает предметы по клавише"
)
@Environment(EnvType.CLIENT)
public class AutoSwap extends Module {
   private final ModeSetting mode = new ModeSetting(this, "Режим", "Rage", "Legit", "Inventory");
   private final ModeSetting rageStyle = new ModeSetting(this, "Тип свапа", "Двойной", "Тройной").setVisible(() -> this.mode.is("Rage"));
   private final ModeSetting invStyle = new ModeSetting(this, "Тип свапа (Inv)", "Двойной", "Тройной", "Бинды").setVisible(() -> this.mode.is("Inventory"));
   private final SliderSetting invSwapDelay = new SliderSetting(this, "Задержка инвентаря (мс)", 50.0F, 10.0F, 300.0F, 10.0F)
      .setVisible(() -> this.mode.is("Inventory"));
   private final ModeSetting firstItemSetting = new ModeSetting(this, "Первый предмет", "Шар", "Золотое яблоко", "Щит", "Тотем")
      .setVisible(() -> this.mode.is("Rage") && this.rageStyle.is("Двойной") || this.mode.is("Inventory") && this.invStyle.is("Двойной"));
   private final ModeSetting secondItemSetting = new ModeSetting(this, "Второй предмет", "Шар 2", "Золотое яблоко 2", "Щит 2", "Тотем 2")
      .setVisible(() -> this.mode.is("Rage") && this.rageStyle.is("Двойной") || this.mode.is("Inventory") && this.invStyle.is("Двойной"));
   private final BindSetting bind = new BindSetting(this, "Клавиша свапа", -1)
      .setVisible(() -> this.mode.is("Rage") || this.mode.is("Inventory") && (this.invStyle.is("Двойной") || this.invStyle.is("Тройной")));
   private final BooleanSetting swaprender = new BooleanSetting(this, "Уведомления о свапе", true);
   private final BooleanSetting onlyEnchanted = new BooleanSetting(this, "Только Зач. Тотем", false)
      .setVisible(() -> this.mode.is("Rage") && this.rageStyle.is("Двойной") || this.mode.is("Inventory") && this.invStyle.is("Двойной"));
   private final BooleanSetting tripleAnimations = new BooleanSetting(this, "Анимации тройного", true)
      .setVisible(() -> this.mode.is("Rage") && this.rageStyle.is("Тройной") || this.mode.is("Inventory") && this.invStyle.is("Тройной"));
   private final BindSetting pearlBind = new BindSetting(this, "Перл", -1)
      .setVisible(() -> this.mode.is("Legit") || this.mode.is("Inventory") && this.invStyle.is("Бинды"));
   private final BindSetting windChargeBind = new BindSetting(this, "Заряд ветра", -1)
      .setVisible(() -> this.mode.is("Legit") || this.mode.is("Inventory") && this.invStyle.is("Бинды"));
   private final BindSetting sphereBind = new BindSetting(this, "Шар", -1)
      .setVisible(() -> this.mode.is("Legit") || this.mode.is("Inventory") && this.invStyle.is("Бинды"));
   private final BindSetting totemBind = new BindSetting(this, "Тотем", -1)
      .setVisible(() -> this.mode.is("Legit") || this.mode.is("Inventory") && this.invStyle.is("Бинды"));
   private final BindSetting gappleBind = new BindSetting(this, "Золотое яблоко", -1)
      .setVisible(() -> this.mode.is("Legit") || this.mode.is("Inventory") && this.invStyle.is("Бинды"));
   private final BindSetting shieldBind = new BindSetting(this, "Щит", -1)
      .setVisible(() -> this.mode.is("Legit") || this.mode.is("Inventory") && this.invStyle.is("Бинды"));
   private final BooleanSetting legitNotifications = new BooleanSetting(this, "Уведомления (Legit)", true).setVisible(() -> this.mode.is("Legit"));
   private final SliderSetting legitSwapDelay = new SliderSetting(this, "Задержка свапа (мс)", 180.0F, 50.0F, 500.0F, 10.0F)
      .setVisible(() -> this.mode.is("Legit"));
   private final StopWatchShadow swapWatch = new StopWatchShadow();
   private boolean bypassActive;
   private boolean bypassSwapped;
   private int bypassSlot = -1;
   private String bypassItemName = "";
   private long legitSwapReadyAt;
   private int invStep = 0;
   private int invTargetSlot = -1;
   private String invTargetName = "";
   private long invStepTimer = 0L;
   class_1799[] tripleSlotItems = new class_1799[]{null, null, null};
   private boolean duoUseSecond;

   private AutoSwap.ItemTarget parseItemTarget(String settingValue, boolean enchantedTotem) {
      if (settingValue.contains("Шар")) {
         return new AutoSwap.ItemTarget(class_1802.field_8575, "Шар", false);
      } else if (settingValue.contains("Тотем")) {
         return new AutoSwap.ItemTarget(class_1802.field_8288, "Тотем", enchantedTotem);
      } else if (settingValue.contains("Золотое яблоко")) {
         return new AutoSwap.ItemTarget(class_1802.field_8463, "Золотое яблоко", false);
      } else {
         return settingValue.contains("Щит")
            ? new AutoSwap.ItemTarget(class_1802.field_8255, "Щит", false)
            : new AutoSwap.ItemTarget(class_1802.field_8288, "Тотем", enchantedTotem);
      }
   }

   private boolean matchesItem(class_1799 stack, class_1792 item, boolean onlyEnchanted) {
      if (stack == null || stack.method_7960()) {
         return false;
      } else if (item == class_1802.field_8575) {
         return isSphere(stack);
      } else {
         return item == class_1802.field_8288 && onlyEnchanted
            ? stack.method_7909() == class_1802.field_8288 && stack.method_7958()
            : stack.method_7909() == item;
      }
   }

   private boolean matchesStack(class_1799 stack, class_1799 target) {
      return sameStackIdentity(stack, target);
   }

   static boolean sameStackIdentity(class_1799 a, class_1799 b) {
      if (a == null || b == null || a.method_7960() || b.method_7960()) {
         return false;
      } else if (a.method_7909() != b.method_7909()) {
         return false;
      } else if (a.method_7958() != b.method_7958()) {
         return false;
      } else {
         return a.method_7964().getString().equals(b.method_7964().getString());
      }
   }

   private boolean isBindHeld() {
      int k = this.bind.get();
      if (k == -1) {
         return false;
      } else {
         long handle = mc.method_22683().method_4490();
         return k >= 0 && k <= 7 ? GLFW.glfwGetMouseButton(handle, k) == 1 : class_3675.method_15987(mc.method_22683(), k);
      }
   }

   @EventHandler
   public void update(EventTick event) {
      if (mc.field_1724 != null) {
         if (!mc.field_1724.method_5805()) {
            this.setKey(true);
         }

         boolean isRageTriple = this.mode.is("Rage") && this.rageStyle.is("Тройной");
         boolean isInvTriple = this.mode.is("Inventory") && this.invStyle.is("Тройной");
         if (isRageTriple || isInvTriple) {
            class_437 currentScreen = mc.field_1755;
            if (mc.field_1755 instanceof TripleWheelScreen ws) {
               if (!this.isBindHeld() && !ws.isSelectionAnimating()) {
                  int hov = ws.getHoveredSlot();
                  if (isInvTriple) {
                     mc.method_1507(null);
                     if (hov >= 0) {
                        this.executeInventoryTripleSwap(hov);
                     }

                     return;
                  }

                  if (this.tripleAnimations.getValue()) {
                     if (hov < 0) {
                        ws.startCloseAnimation(null);
                        return;
                     }

                     ws.startSelectionAnimation(hov, () -> this.executeTripleSwap(hov));
                  } else {
                     mc.method_1507(null);
                     if (hov >= 0) {
                        this.executeTripleSwap(hov);
                     }
                  }
               }

               return;
            }
         }

         if (this.invStep > 0) {
            long delay = (long)this.invSwapDelay.getValue().floatValue();
            if (System.currentTimeMillis() - this.invStepTimer >= delay) {
               this.invStepTimer = System.currentTimeMillis();
               AttackAura.stoptick = 3;
               switch (this.invStep) {
                  case 1:
                     class_490 invScreen = new class_490(mc.field_1724);
                     mc.method_1507(invScreen);
                     this.aimCursorAtSlot(invScreen, this.invTargetSlot);
                     this.invStep = 2;
                     break;
                  case 2:
                     int clickSlotId = this.invTargetSlot < 9 ? this.invTargetSlot + 36 : this.invTargetSlot;
                     int invSlot = this.invTargetSlot >= 36 ? this.invTargetSlot - 36 : this.invTargetSlot;
                     class_1799 swappedItem = mc.field_1724.method_31548().method_5438(invSlot).method_7972();
                     int syncId = mc.field_1724.field_7498.field_7763;
                     mc.field_1761.method_2906(syncId, clickSlotId, 40, class_1713.field_7791, mc.field_1724);
                     mc.field_1724.field_3944.method_52787(new class_2815(syncId));
                     if (this.swaprender.getValue()) {
                        String name = !this.invTargetName.isEmpty()
                           ? this.invTargetName
                           : (swappedItem.method_7960() ? "предмет" : swappedItem.method_7964().getString());
                        if (!swappedItem.method_7960()) {
                           NotificationManager.send("Свапнул на " + name, NotificationManager.Type.INFO, swappedItem.method_7972(), 2000L);
                        } else {
                           NotificationManager.send("Свапнул на " + name, NotificationManager.Type.INFO, 2000L);
                        }
                     }

                     this.invStep = 3;
                     break;
                  case 3:
                     if (mc.field_1755 instanceof class_490) {
                        mc.field_1724.method_7346();
                        mc.method_1507(null);
                     }

                     this.setKey(true);
                     this.invStep = 0;
                     this.invTargetSlot = -1;
                     this.invTargetName = "";
               }
            }
         } else {
            class_1703 screenHandler = mc.field_1724.field_7512;
            if (this.bypassActive) {
               if (this.mode.is("Legit") && System.currentTimeMillis() < this.legitSwapReadyAt) {
                  this.forceSprintStop();
                  return;
               }

               if (!this.bypassSwapped && this.bypassSlot != -1) {
                  this.forceSprintStop();
                  int invSlot = this.bypassSlot >= 36 ? this.bypassSlot - 36 : this.bypassSlot;
                  class_1799 swappedItem = mc.field_1724.method_31548().method_5438(invSlot).method_7972();
                  int clickSlotId = this.bypassSlot < 9 ? this.bypassSlot + 36 : this.bypassSlot;
                  mc.field_1761.method_2906(screenHandler.field_7763, clickSlotId, 40, class_1713.field_7791, mc.field_1724);
                  this.setKey(true);
                  if (this.swaprender.getValue()) {
                     String name = !this.bypassItemName.isEmpty()
                        ? this.bypassItemName
                        : (swappedItem.method_7960() ? "предмет" : swappedItem.method_7964().getString());
                     if (!swappedItem.method_7960()) {
                        NotificationManager.send("Свапнул на " + name, NotificationManager.Type.INFO, swappedItem.method_7972(), 2000L);
                     } else {
                        NotificationManager.send("Свапнул на " + name, NotificationManager.Type.INFO, 2000L);
                     }
                  }

                  this.bypassSwapped = true;
               }

               long timeout = this.mode.is("Legit") ? 1500L : 500L;
               if (this.bypassSwapped || this.swapWatch.hasTimeElapsed(timeout)) {
                  this.bypassActive = false;
                  this.bypassSwapped = false;
                  this.bypassSlot = -1;
                  this.bypassItemName = "";
                  this.legitSwapReadyAt = 0L;
               }
            }
         }
      }
   }

   @EventHandler
   public void input(EventKey event) {
      if (mc.field_1755 == null) {
         if (this.mode.is("Inventory")) {
            if (this.invStyle.is("Двойной")) {
               if (this.bind.get() != -1 && event.getKey() == this.bind.get()) {
                  this.triggerDoubleSwap(true);
               }
            } else if (this.invStyle.is("Тройной")) {
               if (this.bind.get() != -1 && event.getKey() == this.bind.get()) {
                  mc.execute(() -> mc.method_1507(new TripleWheelScreen(this)));
               }
            } else {
               this.handleInventoryBinds(event.getKey());
            }
         } else if (this.mode.is("Legit")) {
            this.handleLegitInput(event.getKey());
         } else if (this.bind.get() != -1 && event.getKey() == this.bind.get()) {
            if (this.rageStyle.is("Двойной")) {
               this.triggerDoubleSwap(false);
            } else if (this.rageStyle.is("Тройной")) {
               mc.execute(() -> mc.method_1507(new TripleWheelScreen(this)));
            }
         }
      }
   }

   private void triggerDoubleSwap(boolean isInventoryMode) {
      if (mc.field_1724 != null) {
         AutoSwap.ItemTarget firstTarget = this.parseItemTarget(this.firstItemSetting.getValue(), this.onlyEnchanted.getValue());
         AutoSwap.ItemTarget secondTarget = this.parseItemTarget(this.secondItemSetting.getValue(), this.onlyEnchanted.getValue());
         if (firstTarget.item == secondTarget.item && firstTarget.onlyEnchanted == secondTarget.onlyEnchanted) {
            int slotA = this.findLegitSlotEx(firstTarget.item, firstTarget.onlyEnchanted, -1);
            int slotB = this.findLegitSlotEx(firstTarget.item, firstTarget.onlyEnchanted, slotA);
            if (slotA == -1) {
               if (this.swaprender.getValue()) {
                  NotificationManager.send(firstTarget.name + " не найден", NotificationManager.Type.INFO, 1500L);
               }
            } else {
               int chosen = this.duoUseSecond && slotB != -1 ? slotB : slotA;
               this.duoUseSecond = !this.duoUseSecond;
               this.swapToSlot(chosen, firstTarget.name, isInventoryMode);
            }

            return;
         }

         class_1799 offhand = mc.field_1724.method_6079();
         AutoSwap.ItemTarget targetToSwap;
         if (this.matchesItem(offhand, firstTarget.item, firstTarget.onlyEnchanted)) {
            targetToSwap = secondTarget;
         } else {
            targetToSwap = firstTarget;
         }

         if (!this.matchesItem(offhand, targetToSwap.item, targetToSwap.onlyEnchanted)) {
            if (isInventoryMode) {
               this.startInventorySwap(targetToSwap.item, targetToSwap.name, targetToSwap.onlyEnchanted);
            } else {
               this.swap(targetToSwap.item, targetToSwap.name, targetToSwap.onlyEnchanted);
            }
         }
      }
   }

   private void handleLegitInput(int keyCode) {
      if (this.matches(this.pearlBind, keyCode)) {
         this.legitSwap(class_1802.field_8634, "Эндер-перл", false);
      } else if (this.matches(this.windChargeBind, keyCode)) {
         this.legitSwap(class_1802.field_49098, "Заряд ветра", false);
      } else if (this.matches(this.sphereBind, keyCode)) {
         this.legitSwap(class_1802.field_8575, "Шар", false);
      } else if (this.matches(this.totemBind, keyCode)) {
         this.legitSwap(class_1802.field_8288, "Тотем", this.onlyEnchanted.getValue());
      } else if (this.matches(this.gappleBind, keyCode)) {
         this.legitSwap(class_1802.field_8463, "Золотое яблоко", false);
      } else {
         if (this.matches(this.shieldBind, keyCode)) {
            this.legitSwap(class_1802.field_8255, "Щит", false);
         }
      }
   }

   private void handleInventoryBinds(int keyCode) {
      if (this.matches(this.pearlBind, keyCode)) {
         this.startInventorySwap(class_1802.field_8634, "Эндер-перл", false);
      } else if (this.matches(this.windChargeBind, keyCode)) {
         this.startInventorySwap(class_1802.field_49098, "Заряд ветра", false);
      } else if (this.matches(this.sphereBind, keyCode)) {
         this.startInventorySwap(class_1802.field_8575, "Шар", false);
      } else if (this.matches(this.totemBind, keyCode)) {
         this.startInventorySwap(class_1802.field_8288, "Тотем", this.onlyEnchanted.getValue());
      } else if (this.matches(this.gappleBind, keyCode)) {
         this.startInventorySwap(class_1802.field_8463, "Золотое яблоко", false);
      } else {
         if (this.matches(this.shieldBind, keyCode)) {
            this.startInventorySwap(class_1802.field_8255, "Щит", false);
         }
      }
   }

   private boolean matches(BindSetting setting, int keyCode) {
      Integer bound = setting.get();
      return bound != null && bound > 0 && bound == keyCode;
   }

   private void startInventorySwap(class_1792 item, String name, boolean enchantedOnly) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         class_1799 offhand = mc.field_1724.method_6079();
         if (!this.matchesItem(offhand, item, enchantedOnly)) {
            int slot = this.findLegitSlot(item, enchantedOnly);
            if (slot == -1) {
               if (this.swaprender.getValue()) {
                  NotificationManager.send(name + " не найден", NotificationManager.Type.INFO, 1500L);
               }
            } else {
               this.startInventorySwapSlot(slot, name);
            }
         }
      }
   }

   private void startInventorySwapSlot(int slot, String name) {
      this.invTargetSlot = slot;
      this.invTargetName = name;
      this.invStep = 1;
      this.invStepTimer = System.currentTimeMillis();
      this.forceSprintStop();
      this.setKey(false);
      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);
         class_243 vel = mc.field_1724.method_18798();
         mc.field_1724.method_18800(0.0, vel.field_1351, 0.0);
      }

      AttackAura.stoptick = 3;
   }

   private void legitSwap(class_1792 item, String name, boolean enchantedOnly) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         int slot = this.findLegitSlot(item, enchantedOnly);
         if (slot == -1) {
            if (this.legitNotifications.getValue()) {
               NotificationManager.send(name + " не найден", NotificationManager.Type.INFO, 1500L);
            }
         } else {
            this.beginSwapStop();
            AttackAura.stoptick = 3;
            this.bypassActive = true;
            this.bypassSwapped = false;
            this.bypassSlot = slot;
            this.bypassItemName = name;
            this.swapWatch.reset();
         }
      }
   }

   private void beginSwapStop() {
      this.setKey(false);
      this.forceSprintStop();
      if (!this.mode.is("Legit")) {
         this.legitSwapReadyAt = 0L;
      } else {
         long delay = (long)this.legitSwapDelay.getValue().floatValue() + ThreadLocalRandom.current().nextLong(0L, 71L);
         this.legitSwapReadyAt = System.currentTimeMillis() + delay;
         Sprint sprint = Sprint.get();
         if (sprint != null) {
            sprint.blockSprintFor(delay + 120L);
         }
      }
   }

   private void forceSprintStop() {
      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);
      }

      if (mc.field_1690 != null) {
         mc.field_1690.field_1867.method_23481(false);
      }
   }

   public static boolean isSphere(class_1799 stack) {
      return stack != null && !stack.method_7960() ? stack.method_31574(class_1802.field_8575) : false;
   }

   private int findSphereSlot() {
      return this.findSphereSlotEx(-1);
   }

   private int findSphereSlotEx(int excludeSlot) {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 36; i++) {
            int handlerSlot = i < 9 ? i + 36 : i;
            if (handlerSlot != excludeSlot && isSphere(mc.field_1724.method_31548().method_5438(i))) {
               return handlerSlot;
            }
         }

         return -1;
      }
   }

   private int findLegitSlot(class_1792 item, boolean enchantedOnly) {
      return this.findLegitSlotEx(item, enchantedOnly, -1);
   }

   private int findLegitSlotEx(class_1792 item, boolean enchantedOnly, int excludeSlot) {
      if (item == class_1802.field_8288) {
         return this.findTotemSlotEx(enchantedOnly, excludeSlot);
      } else if (item == class_1802.field_8575) {
         return this.findSphereSlotEx(excludeSlot);
      } else {
         if (mc.field_1724 == null) {
            return -1;
         } else {
            for (int i = 0; i < 36; i++) {
               int handlerSlot = i < 9 ? i + 36 : i;
               if (handlerSlot != excludeSlot
                  && this.matchesItem(mc.field_1724.method_31548().method_5438(i), item, enchantedOnly)) {
                  return handlerSlot;
               }
            }

            return -1;
         }
      }
   }

   private void swapToSlot(int slot, String itemName, boolean isInventoryMode) {
      if (isInventoryMode) {
         this.startInventorySwapSlot(slot, itemName);
      } else {
         this.beginSwapStop();
         AttackAura.stoptick = 3;
         this.bypassActive = true;
         this.bypassSwapped = false;
         this.bypassSlot = slot;
         this.bypassItemName = itemName;
         this.swapWatch.reset();
      }
   }

   void executeTripleSwap(int slotIndex) {
      if (mc.field_1724 != null) {
         class_1799 target = this.tripleSlotItems[slotIndex];
         if (target != null && !target.method_7960()) {
            class_1799 offhand = mc.field_1724.method_6079();
            if (!this.matchesStack(offhand, target)) {
               int slot = this.findSlotByStack(target);
               if (slot != -1) {
                  this.beginSwapStop();
                  AttackAura.stoptick = 3;
                  this.bypassActive = true;
                  this.bypassSwapped = false;
                  this.bypassSlot = slot;
                  this.bypassItemName = target.method_7964().getString();
                  this.swapWatch.reset();
               }
            }
         }
      }
   }

   void executeInventoryTripleSwap(int slotIndex) {
      if (mc.field_1724 != null) {
         if (slotIndex >= 0 && slotIndex < this.tripleSlotItems.length) {
            class_1799 target = this.tripleSlotItems[slotIndex];
            if (target != null && !target.method_7960()) {
               class_1799 offhand = mc.field_1724.method_6079();
               if (!this.matchesStack(offhand, target)) {
                  int slot = this.findSlotByStack(target);
                  if (slot != -1) {
                     this.startInventorySwapSlot(slot, target.method_7964().getString());
                  } else if (this.swaprender.getValue()) {
                     NotificationManager.send(target.method_7964().getString() + " не найден", NotificationManager.Type.INFO, 1500L);
                  }
               }
            }
         }
      }
   }

   private void aimCursorAtSlot(class_490 invScreen, int targetSlotId) {
      try {
         int slotToFind = targetSlotId < 9 ? targetSlotId + 36 : targetSlotId;
         class_1735 targetSlotObj = null;

         for (class_1735 s : mc.field_1724.field_7498.field_7761) {
            if (s.field_7874 == slotToFind) {
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
      } catch (Throwable var13) {
      }
   }

   boolean isTripleAnimationsEnabled() {
      return this.tripleAnimations.getValue();
   }

   private int findSlotByStack(class_1799 target) {
      for (int i = 0; i < 36; i++) {
         class_1799 s = mc.field_1724.method_31548().method_5438(i);
         if (class_1799.method_31577(s, target)) {
            return i < 9 ? i + 36 : i;
         }
      }

      for (int var5 = 0; var5 < 36; var5++) {
         class_1799 s = mc.field_1724.method_31548().method_5438(var5);
         if (sameStackIdentity(s, target)) {
            return var5 < 9 ? var5 + 36 : var5;
         }
      }

      return -1;
   }

   int tripleSlotState(int index) {
      class_1799 cfg = this.tripleSlotItems[index];
      if (cfg == null || cfg.method_7960()) {
         return 2;
      } else if (mc.field_1724 != null && sameStackIdentity(mc.field_1724.method_6079(), cfg)) {
         return 1;
      } else {
         return this.findSlotByStack(cfg) != -1 ? 2 : 0;
      }
   }

   private void swap(class_1792 item, String itemName, boolean onlyEnchanted) {
      int slot = item == class_1802.field_8288
         ? this.findTotemSlot(onlyEnchanted)
         : (item == class_1802.field_8575 ? this.findSphereSlot() : InvUtil.find(item));
      if (slot != -1) {
         this.swapToSlot(slot, itemName, false);
      } else if (this.swaprender.getValue()) {
         NotificationManager.send(itemName + " не найден", NotificationManager.Type.INFO, 1500L);
      }
   }

   private int findTotemSlot(boolean onlyEnchanted) {
      return this.findTotemSlotEx(onlyEnchanted, -1);
   }

   private int findTotemSlotEx(boolean onlyEnchanted, int excludeSlot) {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 36; i++) {
            int handlerSlot = i < 9 ? i + 36 : i;
            if (handlerSlot != excludeSlot) {
               class_1799 s = mc.field_1724.method_31548().method_5438(i);
               if (s.method_7909() == class_1802.field_8288 && s.method_7958()) {
                  return handlerSlot;
               }
            }
         }

         if (!onlyEnchanted) {
            for (int var5 = 0; var5 < 36; var5++) {
               int handlerSlot = var5 < 9 ? var5 + 36 : var5;
               if (handlerSlot != excludeSlot) {
                  class_1799 s = mc.field_1724.method_31548().method_5438(var5);
                  if (s.method_7909() == class_1802.field_8288) {
                     return handlerSlot;
                  }
               }
            }
         }

         return -1;
      }
   }

   @Override
   public void onDisable() {
      if (mc.field_1755 instanceof class_490 && this.invStep > 0) {
         if (mc.field_1724 != null) {
            mc.field_1724.method_7346();
         }

         mc.method_1507(null);
      }

      this.invStep = 0;
      this.setKey(true);
      if (mc.field_1755 instanceof TripleWheelScreen || mc.field_1755 instanceof TriplePickerScreen) {
         mc.execute(() -> mc.method_1507(null));
      }

      super.onDisable();
   }

   private void setKey(boolean state) {
      for (class_304 keyBinding : new class_304[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      }) {
         boolean pressed = state && class_3675.method_15987(mc.method_22683(), keyBinding.method_1429().method_1444());
         keyBinding.method_23481(pressed);
      }
   }

   private static record ItemTarget(class_1792 item, String name, boolean onlyEnchanted) {
   }
}
