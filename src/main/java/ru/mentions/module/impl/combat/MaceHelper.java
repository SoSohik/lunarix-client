package ru.mentions.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2886;
import net.minecraft.class_2960;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.utils.FakePlayer;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "MaceHelper",
   desc = "Автоматически берет булаву при реальном падении с высоты или после порыва ветра",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class MaceHelper extends Module {
   public SliderSetting minFallHeight = new SliderSetting(this, "Мин. высота падения", 6.0F, 6.0F, 15.0F, 1.0F);
   public BooleanSetting windCharge = new BooleanSetting(this, "Порыв ветра", true);
   public SliderSetting maceSwapBackDelay = new SliderSetting(this, "Задержка возврата (сек)", 0.3F, 0.05F, 2.0F, 0.05F);
   public BooleanSetting swapBackOnHit = new BooleanSetting(this, "Возврат после удара", false);
   public BooleanSetting onlyWithTarget = new BooleanSetting(this, "Только при цели рядом", false);
   public SliderSetting targetRange = new SliderSetting(this, "Дистанция до цели", 5.0F, 2.0F, 10.0F, 0.5F).setVisible(() -> this.onlyWithTarget.getValue());
   public BooleanSetting autoHit = new BooleanSetting(this, "Авто удар булавой", true);
   private int previousSlot = -1;
   private boolean maceEquipped = false;
   private boolean maceLanded = false;
   private boolean hasAttacked = false;
   private long landTime = 0L;
   private long hitTime = 0L;
   private double lastGroundY = 0.0;
   private double peakY = Double.NaN;
   private double lastVy = 0.0;
   private boolean windBoosted = false;
   private long windBoostTime = 0L;

   public static MaceHelper getInstance() {
      return Instance.get(MaceHelper.class);
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && event.isSend()) {
         if (event.getPacket() instanceof class_2886 packet) {
            class_1799 stack = mc.field_1724.method_5998(packet.method_12551());
            if (isWindCharge(stack)) {
               this.windBoosted = true;
               this.windBoostTime = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   public void onEvent(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean onGround = mc.field_1724.method_24828();
         double currentY = mc.field_1724.method_23318();
         double vy = mc.field_1724.method_18798().field_1351;
         if (this.windCharge.getValue()) {
            if (!(vy >= 0.55) && (!(vy - this.lastVy >= 0.5) || !(vy > 0.2))) {
               if ((isWindCharge(mc.field_1724.method_6047()) || isWindCharge(mc.field_1724.method_6079())) && mc.field_1690.field_1904.method_1434()) {
                  this.windBoosted = true;
                  this.windBoostTime = System.currentTimeMillis();
               }
            } else {
               this.windBoosted = true;
               this.windBoostTime = System.currentTimeMillis();
            }

            if (this.windBoosted && System.currentTimeMillis() - this.windBoostTime > 4000L) {
               this.windBoosted = false;
            }
         } else {
            this.windBoosted = false;
         }

         if (onGround) {
            this.lastGroundY = currentY;
            this.peakY = currentY;
            this.windBoosted = false;
            if (this.maceEquipped) {
               if (!this.maceLanded) {
                  this.maceLanded = true;
                  this.landTime = System.currentTimeMillis();
               }

               long delayMs = (long)(this.maceSwapBackDelay.getValue() * 1000.0F);
               if (System.currentTimeMillis() - this.landTime >= delayMs) {
                  this.returnPreviousSlot();
               }
            }

            this.lastVy = vy;
         } else {
            if (Double.isNaN(this.peakY) || currentY > this.peakY) {
               this.peakY = currentY;
            }

            if (!mc.field_1724.method_6101() && !mc.field_1724.method_6128() && !mc.field_1724.method_5799() && !mc.field_1724.method_5771()) {
               class_1309 target = this.getNearbyTarget();
               if (this.onlyWithTarget.getValue() && target == null && !this.maceEquipped) {
                  this.lastVy = vy;
               } else {
                  boolean isDescending = vy < 0.0;
                  if (isDescending) {
                     double fallFromPeak = this.peakY - currentY;
                     double effectiveFall = Math.max(fallFromPeak, mc.field_1724.field_6017);
                     double distToGround = this.getDistanceToGround(mc.field_1724);
                     double totalFallHeight = effectiveFall + distToGround;
                     double requiredHeight = Math.max(6.0, Math.min(15.0, (double)this.minFallHeight.getValue().floatValue()));
                     boolean canEquip = totalFallHeight >= requiredHeight && (!this.windBoosted || this.windCharge.getValue());
                     if (canEquip && !this.maceEquipped) {
                        int maceSlot = this.findMaceSlot();
                        int currentSlot = mc.field_1724.method_31548().method_67532();
                        if (maceSlot != -1 && maceSlot != currentSlot) {
                           this.previousSlot = currentSlot;
                           mc.field_1724.method_31548().method_61496(maceSlot);
                           ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                           this.maceEquipped = true;
                           this.maceLanded = false;
                           this.hasAttacked = false;
                        }
                     }
                  }

                  if (this.maceEquipped) {
                     if (this.autoHit.getValue()
                        && !this.hasAttacked
                        && target != null
                        && mc.field_1724.method_5739(target) <= 4.0F
                        && mc.field_1724.method_7261(0.5F) >= 0.9F) {
                        mc.field_1761.method_2918(mc.field_1724, target);
                        mc.field_1724.method_6104(class_1268.field_5808);
                        this.hasAttacked = true;
                        this.hitTime = System.currentTimeMillis();
                     }

                     if (this.swapBackOnHit.getValue() && this.hasAttacked && System.currentTimeMillis() - this.hitTime >= 150L) {
                        this.returnPreviousSlot();
                     }
                  }

                  this.lastVy = vy;
               }
            } else {
               this.lastVy = vy;
            }
         }
      }
   }

   private void returnPreviousSlot() {
      if (this.previousSlot != -1 && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.previousSlot);
         ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
      }

      this.resetState();
   }

   private class_1309 getNearbyTarget() {
      if (AttackAura.target != null && AttackAura.target.method_5805()) {
         return AttackAura.target;
      } else if (FakePlayer.fakePlayer != null
         && FakePlayer.fakePlayer.method_5805()
         && mc.field_1724.method_5739(FakePlayer.fakePlayer) <= this.targetRange.getValue()) {
         return FakePlayer.fakePlayer;
      } else {
         class_1309 closest = null;
         double closestDist = Double.MAX_VALUE;

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1309 && !(entity instanceof class_746) && entity.method_5805()) {
               if (entity instanceof class_1657) {
                  class_1657 p = (class_1657)entity;
                  if (AntiBot.getInstance().isBot(p) || Client.get().friendManager().isFriend(p.method_5477().getString())) {
                     continue;
                  }
               }

               double dist = (double)mc.field_1724.method_5739(entity);
               if (dist <= (double)this.targetRange.getValue().floatValue() && dist < closestDist) {
                  closestDist = dist;
                  closest = (class_1309)entity;
               }
            }
         }

         return closest;
      }
   }

   private int findMaceSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_31574(class_1802.field_49814)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean isWindCharge(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         try {
            class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
            if (id != null) {
               String path = id.method_12832();
               if (path != null && (path.contains("wind_charge") || path.contains("breeze"))) {
                  return true;
               }
            }
         } catch (Throwable var3) {
         }

         return false;
      } else {
         return false;
      }
   }

   private double getDistanceToGround(class_1309 entity) {
      return AttackAura.getDistanceToGround(entity);
   }

   @EventHandler
   private void onWorldLoad(WorldLoadEvent event) {
      this.resetState();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.returnPreviousSlot();
   }

   private void resetState() {
      this.maceEquipped = false;
      this.maceLanded = false;
      this.hasAttacked = false;
      this.previousSlot = -1;
      this.landTime = 0L;
      this.hitTime = 0L;
      this.windBoosted = false;
      this.peakY = Double.NaN;
      this.lastVy = 0.0;
   }
}
