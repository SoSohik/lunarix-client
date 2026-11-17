package ru.mentions.utils.math;

import java.util.stream.IntStream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2868;
import net.minecraft.class_2886;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class InvUtil implements IMinecraft {
   public static int getItemInHotBar(class_1792 item) {
      return IntStream.range(0, 9).filter(i -> mc.field_1724.method_31548().method_5438(i).method_7909().equals(item)).findFirst().orElse(-1);
   }

   public static int getItemIndex(class_1792 item) {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < mc.field_1724.method_31548().method_5439(); i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
               return i;
            }
         }

         return -1;
      }
   }

   public static void addTaskWithLock(Runnable task, long delayMs) {
      if (mc.field_1724 != null) {
         setMovementKeys(false);
         new Thread(() -> {
            try {
               Thread.sleep(30L);
               mc.execute(() -> {
                  if (task != null) {
                     task.run();
                  }
               });
               Thread.sleep(delayMs);
               mc.execute(() -> setMovementKeys(true));
            } catch (InterruptedException var4) {
               var4.printStackTrace();
               mc.execute(() -> setMovementKeys(true));
            }
         }).start();
      }
   }

   private static void setMovementKeys(boolean state) {
      class_304[] movementKeys = new class_304[]{
         mc.field_1690.field_1894,
         mc.field_1690.field_1881,
         mc.field_1690.field_1913,
         mc.field_1690.field_1849,
         mc.field_1690.field_1903,
         mc.field_1690.field_1867
      };
      long handle = mc.method_22683().method_4490();

      for (class_304 keyBinding : movementKeys) {
         boolean pressed = state && class_3675.method_15987(mc.method_22683(), keyBinding.method_1429().method_1444());
         keyBinding.method_23481(pressed);
      }
   }

   public static boolean doesHotbarHaveItem(class_1792 item) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         for (int i = 0; i < 9; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
               return true;
            }
         }

         return false;
      }
   }

   public static void inventorySwapClick(class_1792 item, boolean rotation) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         int index = getItemIndex(item);
         if (index != -1) {
            int currentSlot = mc.field_1724.method_31548().method_67532();
            if (index < 9) {
               if (index != currentSlot) {
                  mc.field_1724.field_3944.method_52787(new class_2868(index));
               }

               mc.field_1724.field_3944.method_52787(new class_2886(class_1268.field_5808, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
               if (index != currentSlot) {
                  mc.field_1724.field_3944.method_52787(new class_2868(currentSlot));
               }
            } else {
               mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, index, currentSlot, class_1713.field_7791, mc.field_1724);
               mc.field_1724.field_3944.method_52787(new class_2868(currentSlot));
               mc.field_1724.field_3944.method_52787(new class_2886(class_1268.field_5808, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
               mc.field_1724.field_3944.method_52787(new class_2868(currentSlot));
               mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, index, currentSlot, class_1713.field_7791, mc.field_1724);
            }
         }
      }
   }

   public static int find(class_1792 item) {
      int slot = -1;

      for (int i = 0; i < 36; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960() && stack.method_7909() == item) {
            slot = i;
            break;
         }
      }

      if (slot < 9 && slot != -1) {
         slot += 36;
      }

      return slot;
   }

   public static int find(class_1792 item, boolean ignoreEnchanted) {
      return find(item, ignoreEnchanted, false);
   }

   public static int find(class_1792 item, boolean ignoreEnchanted, boolean onlyEnchanted) {
      int slot = -1;
      if (onlyEnchanted) {
         for (int i = 0; i < 36; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_7909() == item && stack.method_7942()) {
               slot = i;
               break;
            }
         }
      } else {
         for (int ix = 0; ix < 36; ix++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(ix);
            if (!stack.method_7960() && stack.method_7909() == item && !stack.method_7942()) {
               slot = ix;
               break;
            }
         }

         if (slot == -1 && !ignoreEnchanted) {
            for (int var9 = 0; var9 < 36; var9++) {
               class_1799 stack = mc.field_1724.method_31548().method_5438(var9);
               if (!stack.method_7960() && stack.method_7909() == item) {
                  slot = var9;
                  break;
               }
            }
         }
      }

      if (slot < 9 && slot != -1) {
         slot += 36;
      }

      return slot;
   }

   @Generated
   private InvUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
