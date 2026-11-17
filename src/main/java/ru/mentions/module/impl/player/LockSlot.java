package ru.mentions.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2480;
import net.minecraft.class_7923;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Lock Slot",
   desc = "Блокирует смену слота хотбара,  защищает предметы от выброса",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class LockSlot extends Module {
   public final BooleanSetting slot1 = new BooleanSetting(this, "Слот 1", false);
   public final BooleanSetting slot2 = new BooleanSetting(this, "Слот 2", false);
   public final BooleanSetting slot3 = new BooleanSetting(this, "Слот 3", false);
   public final BooleanSetting slot4 = new BooleanSetting(this, "Слот 4", false);
   public final BooleanSetting slot5 = new BooleanSetting(this, "Слот 5", false);
   public final BooleanSetting slot6 = new BooleanSetting(this, "Слот 6", false);
   public final BooleanSetting slot7 = new BooleanSetting(this, "Слот 7", false);
   public final BooleanSetting slot8 = new BooleanSetting(this, "Слот 8", false);
   public final BooleanSetting slot9 = new BooleanSetting(this, "Слот 9", false);
   public final BooleanSetting onlyPvp = new BooleanSetting(this, "Только PvP", true);
   public final BooleanSetting swords = new BooleanSetting(this, "Мечи", false);
   public final BooleanSetting mace = new BooleanSetting(this, "Булава", false);
   public final BooleanSetting shulkers = new BooleanSetting(this, "Шалкеры", false);
   private final BooleanSetting[] slots = new BooleanSetting[]{
      this.slot1, this.slot2, this.slot3, this.slot4, this.slot5, this.slot6, this.slot7, this.slot8, this.slot9
   };

   public static LockSlot get() {
      return Instance.get(LockSlot.class);
   }

   public boolean isSlotLocked(int slot) {
      return slot >= 0 && slot <= 8 ? this.slots[slot].getValue() : false;
   }

   public boolean isDropProtected(class_1799 stack, int inventorySlot) {
      if (!this.isEnabled() || stack == null || stack.method_7960()) {
         return false;
      } else if (this.onlyPvp.getValue() && !ServerUtil.isPvp()) {
         return false;
      } else if (this.isShulker(stack)) {
         return this.shulkers.getValue();
      } else {
         return inventorySlot >= 0 && inventorySlot <= 8
            ? this.swords.getValue() && this.isSword(stack) || this.mace.getValue() && stack.method_31574(class_1802.field_49814)
            : false;
      }
   }

   private boolean isShulker(class_1799 stack) {
      class_1792 class_17922 = stack.method_7909();
      class_1747 blockItem;
      return class_17922 instanceof class_1747 && (blockItem = (class_1747)class_17922).method_7711() instanceof class_2480;
   }

   private boolean isSword(class_1799 stack) {
      return class_7923.field_41178.method_10221(stack.method_7909()).method_12832().endsWith("_sword");
   }
}
