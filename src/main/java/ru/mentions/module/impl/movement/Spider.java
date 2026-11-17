package ru.mentions.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_2354;
import net.minecraft.class_2533;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_3481;
import net.minecraft.class_3749;
import net.minecraft.class_3965;
import net.minecraft.class_5554;
import ru.mentions.manager.event_impl.MotionEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.utils.other.TimerUtil;

@ModuleInfo(
   name = "Spider",
   desc = "я челавек пук пук",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class Spider extends Module {
   public ModeSetting modeSpider = new ModeSetting(this, "Режим", "Matrix");
   private final TimerUtil timerUtil = new TimerUtil();
   TimerUtil placeTimer = new TimerUtil();

   @EventHandler
   public void onMotion(MotionEvent e) {
      class_2350 facing = mc.field_1724.method_5735();
      class_2338 posInFront = class_2338.method_49638(mc.field_1724.method_73189()).method_10093(facing);
      class_2680 stateInFront = mc.field_1687.method_8320(posInFront);
      class_2248 blockInFront = stateInFront.method_26204();
      class_2338 posBelow = class_2338.method_49638(mc.field_1724.method_73189());
      class_2680 stateBelow = mc.field_1687.method_8320(posBelow);
      class_2248 blockBelow = stateBelow.method_26204();
      boolean penisF = blockInFront instanceof class_2533
         && Boolean.TRUE.equals(stateInFront.method_11654(class_2741.field_12537))
         && stateInFront.method_28498(class_2741.field_12481);
      boolean penisB = blockBelow instanceof class_2533
         && Boolean.TRUE.equals(stateBelow.method_11654(class_2741.field_12537))
         && stateBelow.method_28498(class_2741.field_12481);
      if (!(blockInFront instanceof class_2354)
         && !stateInFront.method_26164(class_3481.field_15504)
         && !(blockInFront instanceof class_2349)
         && !(blockInFront instanceof class_3749)
         && !(blockInFront instanceof class_5554)
         && !penisF) {
         boolean var28 = false;
      } else {
         boolean var10000 = true;
      }

      if (!(blockBelow instanceof class_2354)
         && !stateBelow.method_26164(class_3481.field_15504)
         && !(blockBelow instanceof class_2349)
         && !(blockBelow instanceof class_3749)
         && !(blockBelow instanceof class_5554)
         && !penisB) {
         boolean var30 = false;
      } else {
         boolean var29 = true;
      }

      if (this.modeSpider.is("Matrix") && this.timerUtil.finished(70L) && this.hozColl()) {
         e.ground(true);
         mc.field_1724.method_24830(true);
         mc.field_1724.method_6043();
         mc.field_1724.field_6017 = 0.0;
         this.timerUtil.reset();
      }

      if (this.modeSpider.is("FT - 2 2") && this.hozColl() && !mc.field_1724.method_24828()) {
         RotationProcess.update(new Rotation(mc.field_1773.method_19418().method_19330(), 60.0F), 255.0F, 255.0F, 0, 50);
         class_3965 hitResult = (class_3965)mc.field_1765;
         class_2338 hitPos = hitResult.method_17777();
         class_2680 hitState = mc.field_1687.method_8320(hitPos);
         boolean isTopSide = hitResult.method_17780() == class_2350.field_11036;
         boolean isSolidBlockOrRod = !hitState.method_45474();
         boolean isSpaceFree = mc.field_1687.method_8320(hitPos.method_10084()).method_45474();
         if (this.placeTimer.hasTimeElapsed(70L)) {
            mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hitResult);
            mc.field_1724.method_6104(class_1268.field_5808);
            this.placeTimer.reset();
            if (this.timerUtil.finished(90L)) {
               e.ground(true);
               mc.field_1724.method_24830(true);
               mc.field_1724.method_6043();
               mc.field_1724.field_6017 = 0.0;
               this.timerUtil.reset();
            }
         }
      }

      int rodSlot;
      if (this.modeSpider.is("FT - 2") && this.hozColl() && !mc.field_1724.method_24828() && (rodSlot = this.findLightningRodBlockInHotBar()) != -1) {
         class_3965 hitResult = (class_3965)mc.field_1765;
         class_2338 hitPos = hitResult.method_17777();
         class_2680 hitState = mc.field_1687.method_8320(hitPos);
         boolean isTopSide = hitResult.method_17780() == class_2350.field_11036;
         boolean isSolidBlockOrRod = !hitState.method_45474();
         boolean isSpaceFree = mc.field_1687.method_8320(hitPos.method_10084()).method_45474();
         if (this.placeTimer.hasTimeElapsed(120L)) {
            int oldSlot = mc.field_1724.method_31548().method_67532();
            mc.field_1724.method_31548().method_61496(rodSlot);
            mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hitResult);
            mc.field_1724.method_6104(class_1268.field_5808);
            mc.field_1724.method_31548().method_61496(oldSlot);
            this.placeTimer.reset();
         }

         if (this.timerUtil.finished(150L)) {
            e.ground(true);
            mc.field_1724.method_24830(true);
            mc.field_1724.method_6043();
            mc.field_1724.field_6017 = 0.0;
            this.timerUtil.reset();
         }
      }
   }

   private int findLightningRodBlockInHotBar() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() == class_1802.field_46991) {
               return i;
            }
         }

         return -1;
      }
   }

   public boolean hozColl() {
      return mc.field_1724.field_5976;
   }
}
