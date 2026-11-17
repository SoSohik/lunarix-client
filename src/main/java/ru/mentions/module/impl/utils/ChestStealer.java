package ru.mentions.module.impl.utils;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1531;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_437;
import net.minecraft.class_476;
import net.minecraft.class_7923;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.math.StopGPT;

@ModuleInfo(
   name = "Chest Stealer",
   desc = "Автоматически ворует все предметы с сундуков",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class ChestStealer extends Module {
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Normal", "Warden", "Danj");
   public final SliderSetting delay = new SliderSetting(this, "Задержка", 100.0F, 60.0F, 300.0F, 10.0F);
   public final BooleanSetting autoClan = new BooleanSetting(this, "AutoClanStorage", false);
   public final BooleanSetting autoEc = new BooleanSetting(this, "Авто /ec", false);
   public final BooleanSetting closeEmpty = new BooleanSetting(this, "Закрывать пустой", true);
   public final BooleanSetting autoOpen = new BooleanSetting(this, "AutoOpen", false);
   private final Set<class_2338> openedChests = new HashSet<>();
   private final StopGPT timer = new StopGPT();
   private final StopGPT openTimer = new StopGPT();
   private final StopGPT ecTimer = new StopGPT();
   private final StopGPT closeTimer = new StopGPT();
   private long nextDelay = 0L;
   private class_2338 lastOpenedChest = null;
   private boolean waitingForLoot = false;
   private ChestStealer.EcState ecState = ChestStealer.EcState.IDLE;

   @Override
   public void onEnable() {
      super.onEnable();
      this.openedChests.clear();
      this.timer.reset();
      this.openTimer.reset();
      this.ecState = ChestStealer.EcState.IDLE;
      this.closeTimer.reset();
      this.lastOpenedChest = null;
      this.waitingForLoot = false;
      this.nextDelay = (long)(this.delay.getValue() + MathUtil.random(40.0, 90.0));
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.openedChests.clear();
      this.ecState = ChestStealer.EcState.IDLE;
      this.lastOpenedChest = null;
      this.waitingForLoot = false;
      this.nextDelay = 0L;
   }

   @EventHandler
   public void onLoadWorld(WorldLoadEvent event) {
      this.openedChests.clear();
      this.openTimer.reset();
      this.timer.reset();
   }

   private String getContainerTitle() {
      return mc.field_1755 instanceof class_476 screen ? screen.method_25440().getString() : "";
   }

   private boolean isValidContainer() {
      String title = this.getContainerTitle();
      if (this.mode.is("Warden")) {
         return title.contains("Сундук");
      } else {
         return this.mode.is("Danj") ? title.contains("Бочка") : true;
      }
   }

   private boolean isValidStorageContainer() {
      String title = this.getContainerTitle();
      return this.autoClan.getValue() ? title.contains("Клан: Хранилище") : true;
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if ((this.mode.is("Warden") || this.mode.is("Danj")) && !this.autoOpen.getValue()) {
            class_437 class_4372 = mc.field_1755;
            if (mc.field_1755 instanceof class_476) {
               class_476 screen = (class_476)class_4372;
               if (this.closeTimer.hasTimePassed(500L)) {
                  Object title = screen.method_25440().getString();
                  boolean hasZeroTimer = ((String)title).contains("0:00") || ((String)title).contains("0.00");
                  boolean isContainerEmpty = this.isContainerEmpty(screen);
                  if (hasZeroTimer || isContainerEmpty && this.waitingForLoot) {
                     mc.field_1724.method_7346();
                     this.closeTimer.reset();
                     this.waitingForLoot = true;
                     return;
                  }

                  if (!isContainerEmpty) {
                     this.waitingForLoot = false;
                  }
               }
            }
         }

         if (this.autoClan.getValue() && this.autoEc.getValue()) {
            this.autoEc.set(false);
         }

         if (this.ecState != ChestStealer.EcState.IDLE) {
            this.handleEcState();
         } else {
            if (!(mc.field_1755 instanceof class_476 screen)) {
               this.timer.reset();
               this.nextDelay = (long)(this.delay.getValue() + MathUtil.random(40.0, 90.0));
               return;
            }

            AutoStorage autoStorage = Client.get().moduleManager().get(AutoStorage.class);
            if (autoStorage == null || !autoStorage.isWorking()) {
               if (this.mode.is("Warden") || this.mode.is("Danj")) {
                  double x = mc.field_1724.method_23317();
                  double y = mc.field_1724.method_23318();
                  double z = mc.field_1724.method_23321();
                  boolean inXZ = Math.abs(Math.abs(x) - 2000.0) <= 150.0 && Math.abs(Math.abs(z) - 2000.0) <= 150.0;
                  if (!inXZ) {
                     return;
                  }

                  if (!this.isValidContainer()) {
                     return;
                  }

                  if (((class_1707)screen.method_17577()).method_17388() == 6) {
                     return;
                  }
               }

               if (this.timer.hasTimePassed(this.nextDelay)) {
                  this.processChest(screen);
               }
            }
         }
      }
   }

   private boolean isServerMenu(class_476 screen) {
      String title = this.getContainerTitle().toLowerCase();
      if (!title.contains("меню")
         && !title.contains("донат")
         && !title.contains("набор")
         && !title.contains("аукцион")
         && !title.contains("рынок")
         && !title.contains("клан")
         && !title.contains("скупщик")
         && !title.contains("казино")
         && !title.contains("выбор")
         && !title.contains("профиль")
         && !title.contains("инфо")
         && !title.contains("банк")
         && !title.contains("варп")
         && !title.contains("shop")
         && !title.contains("menu")
         && !title.contains("auction")
         && !title.contains("trade")
         && !title.contains("warp")) {
         int rows = ((class_1707)screen.method_17577()).method_17388();
         int containerSize = rows * 9;
         int glassCount = 0;

         for (int i = 0; i < containerSize; i++) {
            class_1735 slot = ((class_1707)screen.method_17577()).method_7611(i);
            if (slot.method_7681()) {
               class_1799 stack = slot.method_7677();
               String itemId = class_7923.field_41178.method_10221(stack.method_7909()).toString();
               if (itemId.contains("stained_glass_pane") || itemId.contains("barrier")) {
                  if (++glassCount >= 2) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private void processChest(class_476 screen) {
      if (!this.isServerMenu(screen)) {
         int rows = ((class_1707)screen.method_17577()).method_17388();
         int containerSize = rows * 9;
         boolean foundItemToSteal = false;
         boolean chestIsEmpty = true;

         for (int i = 0; i < containerSize; i++) {
            class_1735 slot = ((class_1707)screen.method_17577()).method_7611(i);
            if (slot.method_7681()) {
               class_1799 stack = slot.method_7677();
               String itemId = class_7923.field_41178.method_10221(stack.method_7909()).toString();
               if (!itemId.contains("stained_glass_pane") && !itemId.contains("barrier")) {
                  chestIsEmpty = false;
                  boolean shouldSteal = true;
                  if (this.mode.is("Warden") || this.mode.is("Danj")) {
                     shouldSteal = this.isValuableItem(slot.method_7677());
                  }

                  if (shouldSteal) {
                     foundItemToSteal = true;
                     mc.field_1761.method_2906(((class_1707)screen.method_17577()).field_7763, i, 0, class_1713.field_7794, mc.field_1724);
                     this.timer.reset();
                     this.nextDelay = (long)MathUtil.random((double)(this.delay.getValue() - 15.0F), (double)(this.delay.getValue() + 25.0F));
                     return;
                  }
               }
            }
         }

         if (!foundItemToSteal && (chestIsEmpty || this.closeEmpty.getValue())) {
            if (this.closeTimer.hasTimePassed(300L)) {
               mc.field_1724.method_7346();
               this.closeTimer.reset();
            }

            if (this.autoEc.getValue()) {
               this.ecState = ChestStealer.EcState.WAITING_EC;
               this.ecTimer.reset();
               mc.field_1724.field_3944.method_45730("ec");
            } else {
               AutoStorage storage;
               if (this.autoClan.getValue() && !ServerUtil.isPvp() && (storage = Client.get().moduleManager().get(AutoStorage.class)) != null) {
                  if (!storage.isEnabled()) {
                     storage.setEnabled(true);
                  }

                  storage.trigger();
                  storage.triggeredByChestStealer = true;
               }
            }
         }
      }
   }

   private void handleEcState() {
      switch (this.ecState) {
         case WAITING_EC:
            if (mc.field_1755 instanceof class_476) {
               this.ecState = ChestStealer.EcState.MOVING_TO_EC;
               this.ecTimer.reset();
               this.nextDelay = (long)MathUtil.random(120.0, 150.0);
            } else if (this.ecTimer.hasTimePassed(5000L)) {
               this.ecState = ChestStealer.EcState.IDLE;
            }
            break;
         case MOVING_TO_EC:
            if (!(mc.field_1755 instanceof class_476 screen)) {
               this.ecState = ChestStealer.EcState.IDLE;
               return;
            }

            if (!this.ecTimer.hasTimePassed(this.nextDelay)) {
               return;
            }

            int rows = ((class_1707)screen.method_17577()).method_17388();
            int containerSize = rows * 9;
            int totalSlots = ((class_1707)screen.method_17577()).field_7761.size();
            boolean moved = false;

            for (int i = containerSize; i < totalSlots; i++) {
               class_1735 slot = ((class_1707)screen.method_17577()).method_7611(i);
               if (slot.method_7681() && this.isValuableItem(slot.method_7677())) {
                  mc.field_1761.method_2906(((class_1707)screen.method_17577()).field_7763, i, 0, class_1713.field_7794, mc.field_1724);
                  this.nextDelay = (long)MathUtil.random(120.0, 150.0);
                  this.ecTimer.reset();
                  moved = true;
                  return;
               }
            }

            if (!moved) {
               this.ecState = ChestStealer.EcState.CLOSING_EC;
            }
            break;
         case CLOSING_EC:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.ecState = ChestStealer.EcState.IDLE;
      }
   }

   public boolean isValuableItem(class_1799 stack) {
      class_1792 item = stack.method_7909();
      String name = stack.method_7964().getString();
      if (item == class_1802.field_8107 || name.contains("Стрела")) {
         return false;
      } else if (name.contains("Хлопушка")) {
         return false;
      } else {
         return name.contains("★")
            ? true
            : item == class_1802.field_8436
               || item == class_1802.field_8712
               || item == class_1802.field_8086
               || item == class_1802.field_8136
               || item == class_1802.field_8477
               || item == class_1802.field_22021
               || item == class_1802.field_22020
               || item == class_1802.field_41946
               || item == class_1802.field_8367
               || item == class_1802.field_8837
               || item == class_1802.field_8449
               || item == class_1802.field_8626
               || item == class_1802.field_8137
               || item == class_1802.field_8463
               || item == class_1802.field_8791
               || item == class_1802.field_8603
               || item == class_1802.field_22022
               || item == class_1802.field_22027
               || item == class_1802.field_22029
               || item == class_1802.field_22028
               || item == class_1802.field_22030
               || item == class_1802.field_8366
               || item == class_1802.field_8668
               || item == class_1802.field_8543
               || item == class_1802.field_8675
               || item == class_1802.field_8288
               || item == class_1802.field_8833
               || item == class_1802.field_38418;
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.mode.is("Warden") || this.mode.is("Danj")) {
            if (this.autoOpen.getValue()) {
               if (!(mc.field_1755 instanceof class_476)) {
                  double x = mc.field_1724.method_23317();
                  double z = mc.field_1724.method_23321();
                  boolean inXZ = Math.abs(Math.abs(x) - 2000.0) <= 150.0 && Math.abs(Math.abs(z) - 2000.0) <= 150.0;
                  if (inXZ) {
                     class_2338 target = this.findNearestChest();
                     if (target != null) {
                        float[] rots = this.getRotations(target);
                        float newYaw = this.smoothRotation(mc.field_1724.method_36454(), rots[0], 15.0F);
                        float newPitch = this.smoothRotation(mc.field_1724.method_36455(), rots[1], 15.0F);
                        RotationProcess.update(new Rotation(newYaw, newPitch), 400.0F, 400.0F, 1, 1);
                        if (this.openTimer.hasTimePassed(350L)) {
                           double distSq = mc.field_1724.method_5707(target.method_46558());
                           if (distSq <= 16.0) {
                              float yawDiff = Math.abs(class_3532.method_15393(mc.field_1724.method_36454() - rots[0]));
                              float pitchDiff = Math.abs(class_3532.method_15393(mc.field_1724.method_36455() - rots[1]));
                              if (yawDiff < 18.0F && pitchDiff < 18.0F) {
                                 class_3965 hit = new class_3965(target.method_46558(), class_2350.field_11036, target, false);
                                 mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
                                 mc.field_1724.method_6104(class_1268.field_5808);
                                 this.openedChests.add(target);
                                 this.openTimer.reset();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private class_2338 findNearestChest() {
      class_2338 playerPos = mc.field_1724.method_24515();
      class_2338 best = null;
      double bestDist = 36.0;

      for (int x = -6; x <= 6; x++) {
         for (int y = -6; y <= 6; y++) {
            for (int z = -6; z <= 6; z++) {
               class_2338 pos = playerPos.method_10069(x, y, z);
               boolean isTargetBlock = false;
               if (this.mode.is("Warden")) {
                  isTargetBlock = mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10034
                     || mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10380;
               } else if (this.mode.is("Danj")) {
                  isTargetBlock = mc.field_1687.method_8320(pos).method_26204() == class_2246.field_16328;
               } else {
                  isTargetBlock = mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10034
                     || mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10380
                     || mc.field_1687.method_8320(pos).method_26204() == class_2246.field_16328;
               }

               double dist;
               if (isTargetBlock
                  && !((dist = pos.method_46558().method_1025(mc.field_1724.method_73189())) > 36.0)
                  && !this.openedChests.contains(pos)
                  && !this.hasTimerHologram(pos)
                  && dist < bestDist) {
                  bestDist = dist;
                  best = pos;
               }
            }
         }
      }

      return best;
   }

   private boolean isContainerEmpty(class_476 screen) {
      int rows = ((class_1707)screen.method_17577()).method_17388();
      int containerSize = rows * 9;

      for (int i = 0; i < containerSize; i++) {
         if (((class_1707)screen.method_17577()).method_7611(i).method_7681()) {
            return false;
         }
      }

      return true;
   }

   private boolean hasTimerHologram(class_2338 pos) {
      class_238 box = new class_238(pos).method_1009(0.0, 2.0, 0.0);

      for (class_1531 stand : mc.field_1687.method_8390(class_1531.class, box, val -> true)) {
         String name;
         if (stand.method_16914() && ((name = stand.method_5797().getString()).matches(".*\\d.*") || name.contains(":"))) {
            return true;
         }
      }

      return false;
   }

   private float[] getRotations(class_2338 pos) {
      class_243 eyesPos = mc.field_1724.method_33571();
      class_243 targetPos = pos.method_46558();
      double dX = targetPos.field_1352 - eyesPos.field_1352;
      double dY = targetPos.field_1351 - eyesPos.field_1351;
      double dZ = targetPos.field_1350 - eyesPos.field_1350;
      double dist = Math.sqrt(dX * dX + dZ * dZ);
      float yaw = (float)(Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-(Math.atan2(dY, dist) * 180.0 / Math.PI));
      return new float[]{yaw, pitch};
   }

   private float smoothRotation(float current, float target, float maxStep) {
      float delta = class_3532.method_15393(target - current);
      if (delta > maxStep) {
         delta = maxStep;
      }

      if (delta < -maxStep) {
         delta = -maxStep;
      }

      return current + delta;
   }

   @Environment(EnvType.CLIENT)
   private static enum EcState {
      IDLE,
      WAITING_EC,
      MOVING_TO_EC,
      CLOSING_EC;
   }
}
