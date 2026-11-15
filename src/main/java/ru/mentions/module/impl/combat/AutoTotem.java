package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1511;
import net.minecraft.class_1541;
import net.minecraft.class_1657;
import net.minecraft.class_1685;
import net.minecraft.class_1701;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1819;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2815;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_490;
import net.minecraft.class_7923;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.movement.Sprint;
import ru.mentions.utils.math.StopWatchP;
import ru.mentions.utils.notification.NotificationManager;

@ModuleInfo(
   name = "Auto Totem",
   desc = "Автоматически берет тотем в руку при опасности",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AutoTotem extends Module {
   public final ModeSetting handMode = new ModeSetting(this, "Рука", "Левая", "Правая");
   public final BooleanSetting notif = new BooleanSetting(this, "Уведомление", true);
   public final ModeSetting soundMode = new ModeSetting(
         this,
         "Звук уведомления",
         "Pop",
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
         "DingDong",
         "Laser",
         "WexSide",
         "Spek",
         "Metallic",
         "Crime",
         "По умолчанию",
         "Выкл"
      )
      .setVisible(() -> this.notif.getValue());
   public final SliderSetting health = new SliderSetting(this, "Здоровье", 5.0F, 0.0F, 20.0F, 0.5F);
   public final BooleanSetting absorption = new BooleanSetting(this, "Учитывать абсорбцию", true);
   public final BooleanSetting elytra = new BooleanSetting(this, "Здоровье с элитрами", true);
   public final SliderSetting elytraHealth = new SliderSetting(this, "Здоровье на элитре", 10.0F, 0.0F, 20.0F, 0.5F).setVisible(() -> this.elytra.getValue());
   public final BooleanSetting crystal = new BooleanSetting(this, "Эндер-кристалл", true);
   public final SliderSetting crystalDistance = new SliderSetting(this, "Дистанция кристалла", 5.0F, 1.0F, 10.0F, 0.5F)
      .setVisible(() -> this.crystal.getValue());
   public final BooleanSetting tnt = new BooleanSetting(this, "Динамит", true);
   public final SliderSetting tntRange = new SliderSetting(this, "Дистанция динамита", 6.0F, 1.0F, 16.0F, 0.5F)
      .setVisible(() -> this.tnt.getValue() && !this.tntAnyDistance.getValue());
   public final BooleanSetting tntAnyDistance = new BooleanSetting(this, "Динамит на любой дистанции", false).setVisible(() -> this.tnt.getValue());
   public final BooleanSetting trident = new BooleanSetting(this, "Трезубец", true);
   public final BooleanSetting falling = new BooleanSetting(this, "Падение", false);
   public final BooleanSetting mace = new BooleanSetting(this, "Булава", true);
   public final SliderSetting maceRange = new SliderSetting(this, "Дистанция булавы", 5.0F, 1.0F, 16.0F, 0.5F).setVisible(() -> this.mace.getValue());
   public final SliderSetting maceHeight = new SliderSetting(this, "Высота булавы", 2.0F, 1.0F, 10.0F, 0.5F).setVisible(() -> this.mace.getValue());
   public final BooleanSetting maceRoof = new BooleanSetting(this, "Проверять крышу", true).setVisible(() -> this.mace.getValue());
   public final SliderSetting maceRoofRadius = new SliderSetting(this, "Радиус крыши", 2.0F, 1.0F, 4.0F, 1.0F)
      .setVisible(() -> this.mace.getValue() && this.maceRoof.getValue());
   public final BooleanSetting saveEnchanted = new BooleanSetting(this, "Сохранять зачарованные", false);
   public final BooleanSetting skipCooldown = new BooleanSetting(this, "Игнорировать кулдаун", true);
   public final BooleanSetting swapBack = new BooleanSetting(this, "Возвращать предмет", true);
   public final BooleanSetting noSwapWhileEating = new BooleanSetting(this, "Не свапать при еде", true);
   public final BooleanSetting notSwapPearl = new BooleanSetting(this, "Не свапать при перле", true);
   public final BooleanSetting noBallSwitch = new BooleanSetting(this, "Не свапать если шар", false);
   private int oldOffhandSlot = -1;
   private class_1799 oldOffhandItem = class_1799.field_8037;
   private int oldMainHandSlot = -1;
   private int rightHandSwapInvSlot = -1;
   private long lastActionTime = 0L;
   private final StopWatchP lockWatch = new StopWatchP();
   private boolean lockHeld;
   private int lastNotifiedTotemSlot = -1;

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1724.method_5805() && mc.field_1687 != null) {
         if (!this.shouldSkipSwap()) {
            this.handleAutoTotem();
         } else if (this.swapBack.getValue()) {
            this.handleSwapBack(this.handMode.is("Левая"));
         }
      } else {
         if (this.lockHeld) {
            this.lockHeld = false;
            this.resetSwapBack();
         }
      }
   }

   private boolean shouldSkipSwap() {
      if (mc.field_1724 == null) {
         return true;
      } else if (this.noSwapWhileEating.getValue() && mc.field_1724.method_6115() && !(mc.field_1724.method_6030().method_7909() instanceof class_1819)) {
         return true;
      } else {
         if (this.notSwapPearl.getValue()) {
            boolean holdingPearl = mc.field_1724.method_6047() != null && mc.field_1724.method_6047().method_31574(class_1802.field_8634)
               || mc.field_1724.method_6079() != null && mc.field_1724.method_6079().method_31574(class_1802.field_8634);
            if (holdingPearl) {
               return true;
            }
         }

         if (this.noBallSwitch.getValue() && this.isBall(mc.field_1724.method_6079())) {
            float totalHp = this.getEffectiveHealth();
            if (totalHp > Math.min(3.5F, this.health.getValue())) {
               return true;
            }
         }

         return false;
      }
   }

   private void handleAutoTotem() {
      boolean danger = this.isDanger();
      boolean isOffhand = this.handMode.is("Левая");
      int totemSlot = this.findTotemSlot();
      if (danger) {
         if (System.currentTimeMillis() - this.lastActionTime < 400L) {
            return;
         }

         if (isOffhand) {
            if (this.isTotem(mc.field_1724.method_6079())) {
               return;
            }

            if (totemSlot != -1) {
               if (!this.lockHeld) {
                  this.setKey(false);
                  AttackAura.stoptick = 3;
                  Sprint.get().tick++;
                  this.lockHeld = true;
                  this.lockWatch.reset();
               }

               if (mc.field_1755 == null || mc.field_1755 instanceof class_490) {
                  if (this.oldOffhandItem.method_7960() && !mc.field_1724.method_6079().method_7960()) {
                     this.oldOffhandItem = mc.field_1724.method_6079().method_7972();
                     this.oldOffhandSlot = totemSlot;
                  }

                  if (totemSlot != this.lastNotifiedTotemSlot) {
                     this.lastNotifiedTotemSlot = totemSlot;
                     int invIdx = totemSlot >= 36 ? totemSlot - 36 : totemSlot;
                     class_1799 totem = mc.field_1724.method_31548().method_5438(invIdx).method_7972();
                     if (!totem.method_7960() && this.notif.getValue()) {
                        NotificationManager.send("Тотем подложен", NotificationManager.Type.MODULE, totem, 1500L, this.soundMode.getValue());
                     }
                  }

                  this.swapHand(totemSlot, 40);
                  this.lastActionTime = System.currentTimeMillis();
                  this.lockHeld = false;
                  this.setKey(true);
               }
            }
         } else {
            if (this.isTotem(mc.field_1724.method_6047())) {
               return;
            }

            if (totemSlot != -1) {
               int invIdx = totemSlot >= 36 ? totemSlot - 36 : totemSlot;
               if (invIdx < 9) {
                  if (this.oldMainHandSlot == -1) {
                     this.oldMainHandSlot = mc.field_1724.method_31548().method_67532();
                  }

                  this.rightHandSwapInvSlot = -1;
                  mc.field_1724.method_31548().method_61496(invIdx);
                  this.lastActionTime = System.currentTimeMillis();
               } else {
                  if (this.oldMainHandSlot == -1) {
                     this.oldMainHandSlot = mc.field_1724.method_31548().method_67532();
                  }

                  this.rightHandSwapInvSlot = totemSlot;
                  this.swapHand(totemSlot, mc.field_1724.method_31548().method_67532());
                  this.lastActionTime = System.currentTimeMillis();
               }
            }
         }
      } else if (this.swapBack.getValue()) {
         this.handleSwapBack(isOffhand);
      } else {
         this.resetSwapBack();
      }
   }

   private void handleSwapBack(boolean isOffhand) {
      if (isOffhand) {
         if (this.oldOffhandSlot == -1 || this.oldOffhandItem.method_7960()) {
            this.resetSwapBack();
         } else if (System.currentTimeMillis() - this.lastActionTime >= 200L) {
            class_1799 offhand = mc.field_1724.method_6079();
            if (!this.sameItemIdentity(offhand, this.oldOffhandItem)) {
               if (this.canRestoreOffhand()) {
                  this.swapHand(this.oldOffhandSlot, 40);
                  this.lastActionTime = System.currentTimeMillis();
               }
            }

            this.resetSwapBack();
         }
      } else if (this.oldMainHandSlot != -1 && System.currentTimeMillis() - this.lastActionTime >= 200L) {
         if (this.rightHandSwapInvSlot != -1) {
            this.swapHand(this.rightHandSwapInvSlot, this.oldMainHandSlot);
            this.rightHandSwapInvSlot = -1;
         }

         mc.field_1724.method_31548().method_61496(this.oldMainHandSlot);
         this.lastActionTime = System.currentTimeMillis();
         this.oldMainHandSlot = -1;
      }
   }

   private boolean sameItemIdentity(class_1799 a, class_1799 b) {
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

   private boolean canRestoreOffhand() {
      if (mc.field_1724 == null || this.oldOffhandSlot == -1) {
         return false;
      } else {
         int invIdx = this.oldOffhandSlot >= 36 ? this.oldOffhandSlot - 36 : this.oldOffhandSlot;
         class_1799 s = mc.field_1724.method_31548().method_5438(invIdx);
         if (s == null || s.method_7960()) {
            return true;
         } else {
            return this.sameItemIdentity(s, this.oldOffhandItem);
         }
      }
   }

   private void swapHand(int slot, int button) {
      if (mc.field_1761 != null && mc.field_1724 != null) {
         mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, slot, button, class_1713.field_7791, mc.field_1724);
         mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
      }
   }

   private void resetSwapBack() {
      this.oldOffhandItem = class_1799.field_8037;
      this.oldOffhandSlot = -1;
      this.oldMainHandSlot = -1;
      this.rightHandSwapInvSlot = -1;
      this.lastNotifiedTotemSlot = -1;
   }

   private int findTotemSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         boolean saveEnch = this.saveEnchanted.getValue();
         boolean skipCd = this.skipCooldown.getValue();
         if (saveEnch) {
            for (int i = 0; i < 36; i++) {
               class_1799 stack = mc.field_1724.method_31548().method_5438(i);
               if (stack != null
                  && !stack.method_7960()
                  && stack.method_7909() == class_1802.field_8288
                  && !stack.method_7958()
                  && (skipCd || !mc.field_1724.method_7357().method_7904(stack))) {
                  return i < 9 ? i + 36 : i;
               }
            }
         }

         for (int ix = 0; ix < 36; ix++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(ix);
            if (stack != null
               && !stack.method_7960()
               && stack.method_7909() == class_1802.field_8288
               && (skipCd || !mc.field_1724.method_7357().method_7904(stack))) {
               return ix < 9 ? ix + 36 : ix;
            }
         }

         return -1;
      }
   }

   private boolean isDanger() {
      return mc.field_1724 == null
         ? false
         : this.checkHealth() || this.checkElytra() || this.checkFall() || this.checkCrystal() || this.checkTnt() || this.checkTrident() || this.checkMace();
   }

   private float getEffectiveHealth() {
      if (mc.field_1724 == null) {
         return 20.0F;
      } else {
         float hp = mc.field_1724.method_6032();
         if (this.absorption.getValue()) {
            hp += mc.field_1724.method_6067();
         }

         return hp;
      }
   }

   private boolean checkHealth() {
      return this.getEffectiveHealth() <= this.health.getValue();
   }

   private boolean checkElytra() {
      if (!this.elytra.getValue()) {
         return false;
      } else {
         class_1799 chest = mc.field_1724.method_6118(class_1304.field_6174);
         return chest != null && chest.method_7909() == class_1802.field_8833 ? this.getEffectiveHealth() <= this.elytraHealth.getValue() : false;
      }
   }

   private boolean checkFall() {
      return !this.falling.getValue()
         ? false
         : !mc.field_1724.method_24828() && !mc.field_1724.method_5869() && mc.field_1724.field_6017 >= 3.5 && mc.field_1724.method_18798().field_1351 < -0.6;
   }

   private boolean checkCrystal() {
      if (this.crystal.getValue() && mc.field_1687 != null) {
         float maxDist = this.crystalDistance.getValue();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1511 && mc.field_1724.method_5739(entity) <= maxDist) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean checkTnt() {
      if (this.tnt.getValue() && mc.field_1687 != null) {
         float maxDist = this.tntAnyDistance.getValue() ? 25.0F : this.tntRange.getValue();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if ((entity instanceof class_1541 || entity instanceof class_1701) && mc.field_1724.method_5739(entity) <= maxDist) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean checkTrident() {
      if (this.trident.getValue() && mc.field_1687 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1685 && mc.field_1724.method_5739(entity) <= 8.0F) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean checkMace() {
      if (this.mace.getValue() && mc.field_1687 != null && mc.field_1724 != null) {
         double range = this.maceRange.getValue().doubleValue();
         AttackAura aura = AttackAura.get();
         if (aura != null && aura.isEnabled() && aura.typeSprint.is("Legit")) {
            range = Math.min(range, 2.9);
         }
         double minHeight = this.maceHeight.getValue().doubleValue();
         boolean checkRoof = this.maceRoof.getValue();
         double roofRadius = this.maceRoofRadius.getValue().doubleValue();
         double px = mc.field_1724.method_23317();
         double py = mc.field_1724.method_23318();
         double pz = mc.field_1724.method_23321();

         for (class_1657 other : mc.field_1687.method_18456()) {
            if (other != mc.field_1724
               && other.method_5805()
               && !other.method_7325()
               && !Client.get().friendManager().isFriend(other.method_5477().getString())) {
               boolean holdingMace = this.isMace(other.method_6047()) || this.isMace(other.method_6079());
               if (holdingMace) {
                  double ox = other.method_23317();
                  double oy = other.method_23318();
                  double oz = other.method_23321();
                  double dx = ox - px;
                  double dz = oz - pz;
                  double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                  double heightDiff = oy - py;
                  if (horizontalDist <= range
                     && heightDiff >= minHeight
                     && (other.method_18798().field_1351 < -0.1 || other.field_6017 > 1.0)
                     && (!checkRoof || !this.hasRoofAbove(px, py, pz, heightDiff, roofRadius))) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean isMace(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      } else if (stack.method_31574(class_1802.field_49814)) {
         return true;
      } else {
         try {
            class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
            if (id != null && id.method_12832().contains("mace")) {
               return true;
            }
         } catch (Throwable var3) {
         }

         return false;
      }
   }

   private boolean hasRoofAbove(double px, double py, double pz, double maxH, double radius) {
      int startX = (int)Math.floor(px - radius);
      int endX = (int)Math.ceil(px + radius);
      int startZ = (int)Math.floor(pz - radius);
      int endZ = (int)Math.ceil(pz + radius);
      int startY = (int)Math.floor(py + 2.0);
      int endY = (int)Math.floor(py + Math.min(maxH, 10.0));

      for (int y = startY; y <= endY; y++) {
         for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
               class_2338 pos = new class_2338(x, y, z);
               class_2680 state = mc.field_1687.method_8320(pos);
               if (!state.method_26215()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isTotem(class_1799 stack) {
      return stack != null && !stack.method_7960() && stack.method_7909() == class_1802.field_8288;
   }

   private boolean isBall(class_1799 stack) {
      return stack != null && !stack.method_7960() ? stack.method_31574(class_1802.field_8575) : false;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.lockHeld) {
         this.lockHeld = false;
      }

      this.resetSwapBack();
   }

   private void setKey(boolean state) {
      class_304[] movementKeys = new class_304[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      };

      for (class_304 keyBinding : movementKeys) {
         boolean pressed = state && class_3675.method_15987(mc.method_22683(), keyBinding.method_1429().method_1444());
         keyBinding.method_23481(pressed);
      }
   }
}
