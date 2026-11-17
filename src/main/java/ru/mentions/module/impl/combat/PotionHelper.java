package ru.mentions.module.impl.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1803;
import net.minecraft.class_1828;
import net.minecraft.class_1844;
import net.minecraft.class_2815;
import net.minecraft.class_5537;
import net.minecraft.class_6880;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import net.minecraft.class_9837;
import net.minecraft.class_2828.class_2831;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.utils.client.ClientInteractionAccess;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.notification.NotificationManager;

@ModuleInfo(
   name = "PotionHelper",
   category = Category.COMBAT,
   desc = "Круговое меню взрывных зелий с автоматическим броском"
)
@Environment(EnvType.CLIENT)
public class PotionHelper extends Module {
   private static PotionHelper instance;
   public final BindSetting bind = new BindSetting(this, "Клавиша колеса", -1);
   public final ModeSetting throwDirection = new ModeSetting(this, "Направление", "Взгляд", "В ноги");
   public final BooleanSetting numberSelect = new BooleanSetting(this, "Выбор цифрами", true);
   public final BooleanSetting blur = new BooleanSetting(this, "Размытие фона", true);
   public final BooleanSetting notify = new BooleanSetting(this, "Уведомления", true);
   public final ModeSetting soundMode = new ModeSetting(
         this,
         "Звук уведомления",
         "Trevoga",
         "Bell",
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
         "Tone",
         "Pop",
         "По умолчанию",
         "Выкл"
      )
      .setVisible(() -> this.notify.getValue());
   private PotionHelper.PotionEntry pendingEntry = null;
   private int pendingStep = 0;
   private int pendingTargetHotbar = 0;
   private int pendingTicks = 0;
   private int bundleInvSlot = -1;
   private int bundleItemIndex = -1;
   private int bundleDestInvSlot = -1;
   private int bundleOriginalHotbar = 0;
   private boolean bundleSwappedToHotbar = false;
   private class_1799 bundleSacrificedStack = class_1799.field_8037;

   public PotionHelper() {
      instance = this;
   }

   public static PotionHelper getInstance() {
      return instance;
   }

   public static boolean arePotionsEquivalent(class_1799 a, class_1799 b) {
      if (a != null && b != null) {
         String nameA = a.method_7964().getString().trim();
         String nameB = b.method_7964().getString().trim();
         if (!nameA.isEmpty() && nameA.equalsIgnoreCase(nameB)) {
            return true;
         } else {
            try {
               class_1844 c1 = (class_1844)a.method_58694(class_9334.field_49651);
               class_1844 c2 = (class_1844)b.method_58694(class_9334.field_49651);
               if (c1 != null && c2 != null) {
                  return c1.equals(c2);
               }
            } catch (Throwable var6) {
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isThrowablePotion(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_1792 item = stack.method_7909();
         if (item == null) {
            return false;
         } else if (item == class_1802.field_8543) {
            return false;
         } else {
            String name = stack.method_7964().getString().toLowerCase();
            if (!name.contains("снеж") && !name.contains("снег") && !name.contains("snow")) {
               String itemTrans = item.method_7876();
               if (itemTrans != null) {
                  String lowerTrans = itemTrans.toLowerCase();
                  if (lowerTrans.contains("snow") || lowerTrans.contains("egg") || lowerTrans.contains("pearl") || lowerTrans.contains("wind_charge")) {
                     return false;
                  }
               }

               if (item == class_1802.field_8436 || item == class_1802.field_8150) {
                  return true;
               } else if (!(item instanceof class_1828) && !(item instanceof class_1803)) {
                  if (itemTrans != null) {
                     String lowerTrans = itemTrans.toLowerCase();
                     if (lowerTrans.contains("splash_potion") || lowerTrans.contains("lingering_potion")) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return true;
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static List<PotionHelper.PotionEntry> findSplashPotions() {
      ArrayList<PotionHelper.PotionEntry> list = new ArrayList<>();
      if (mc.field_1724 == null) {
         return list;
      } else {
         BiConsumer<class_1799, PotionHelper.PotionEntry.SlotRef> addOrUpdate = (stack, ref) -> {
            if (isThrowablePotion(stack)) {
               for (PotionHelper.PotionEntry entry : list) {
                  if (arePotionsEquivalent(entry.getSampleStack(), stack)) {
                     entry.addSlot(ref.invSlot(), ref.bundleIndex());
                     return;
                  }
               }

               list.add(new PotionHelper.PotionEntry(stack.method_7972(), stack.method_7964().getString(), ref.invSlot(), ref.bundleIndex()));
            }
         };
         BiConsumer<class_1799, Integer> scanStack = (stack, slot) -> {
            if (stack != null && !stack.method_7960()) {
               if (isThrowablePotion(stack)) {
                  addOrUpdate.accept(stack, new PotionHelper.PotionEntry.SlotRef(slot, -1));
               } else {
                  try {
                     if (stack.method_58694(class_9334.field_49650) instanceof class_9276 bundleComp) {
                        int size = bundleComp.method_57426();

                        for (int itemIdx = 0; itemIdx < size; itemIdx++) {
                           class_1799 nested = bundleComp.method_57422(itemIdx);
                           if (isThrowablePotion(nested)) {
                              addOrUpdate.accept(nested, new PotionHelper.PotionEntry.SlotRef(slot, itemIdx));
                           }
                        }
                     }
                  } catch (Throwable var8) {
                  }
               }
            }
         };
         scanStack.accept(mc.field_1724.method_6079(), 40);

         for (int i = 0; i < 9; i++) {
            scanStack.accept(mc.field_1724.method_31548().method_5438(i), i);
         }

         for (int i = 9; i < 36; i++) {
            scanStack.accept(mc.field_1724.method_31548().method_5438(i), i);
         }

         list.sort((a, b) -> {
            int pA = getPotionOrderPriority(a.getName());
            int pB = getPotionOrderPriority(b.getName());
            return pA != pB ? Integer.compare(pA, pB) : a.getName().compareToIgnoreCase(b.getName());
         });
         return list;
      }
   }

   public static int getPotionOrderPriority(String name) {
      if (name == null) {
         return 100;
      } else {
         String lower = name.toLowerCase();
         if (lower.contains("сил") || lower.contains("strength")) {
            return 1;
         } else if (lower.contains("стремительн") || lower.contains("скорост") || lower.contains("speed")) {
            return 2;
         } else if (lower.contains("огнестойк") || lower.contains("fire")) {
            return 3;
         } else if (lower.contains("черепаш") || lower.contains("сопротивлен") || lower.contains("turtle")) {
            return 4;
         } else if (lower.contains("реген") || lower.contains("regen")) {
            return 5;
         } else if (lower.contains("исцелен") || lower.contains("heal")) {
            return 6;
         } else if (lower.contains("невидим") || lower.contains("invis")) {
            return 7;
         } else if (lower.contains("прыгуч") || lower.contains("jump")) {
            return 8;
         } else if (lower.contains("паден") || lower.contains("slow_fall")) {
            return 9;
         } else if (lower.contains("вред") || lower.contains("урон") || lower.contains("harm")) {
            return 10;
         } else if (lower.contains("отрав") || lower.contains("poison")) {
            return 11;
         } else if (lower.contains("замедл") || lower.contains("slow")) {
            return 12;
         } else {
            return !lower.contains("слабост") && !lower.contains("weak") ? 50 : 13;
         }
      }
   }

   @EventHandler
   public void onKey(EventKey event) {
      if (this.isEnabled() && mc.field_1724 != null && mc.field_1755 == null) {
         boolean isModuleKey = this.getKey() != -1 && event.getKey() == this.getKey();
         boolean isSettingKey = this.bind.get() != -1 && event.getKey() == this.bind.get();
         if (isModuleKey || isSettingKey) {
            List<PotionHelper.PotionEntry> potions = findSplashPotions();
            if (potions.isEmpty()) {
               if (this.notify.getValue()) {
                  NotificationManager.sendWithSound("Взрывные зелья не найдены в инвентаре", NotificationManager.Type.WARNING, this.soundMode.getValue());
                  ChatUtils.addChatMessage("§c[PotionHelper] §7В инвентаре нет взрывных зелий!");
               }
            } else {
               int activeKey = event.getKey();
               mc.execute(() -> mc.method_1507(new PotionWheelScreen(this, potions, activeKey)));
            }
         }
      }
   }

   public static boolean isBuff(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         try {
            class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
            if (contents != null) {
               for (class_1293 instance : contents.method_57397()) {
                  class_6880 type = instance.method_5579();
                  if (type != null && type.comp_349() != null) {
                     class_1291 effect = (class_1291)type.comp_349();
                     if (effect.method_5573()) {
                        return true;
                     }
                  }
               }
            }
         } catch (Throwable var6) {
         }

         String name = stack.method_7964().getString().toLowerCase();
         return !name.contains("вред")
               && !name.contains("урон")
               && !name.contains("harm")
               && !name.contains("отрав")
               && !name.contains("poison")
               && !name.contains("замедл")
               && !name.contains("slow")
               && !name.contains("слабост")
               && !name.contains("weak")
               && !name.contains("иссуш")
               && !name.contains("wither")
               && !name.contains("тьм")
               && !name.contains("слеп")
               && !name.contains("dark")
            ? name.contains("стремительн")
               || name.contains("скорост")
               || name.contains("speed")
               || name.contains("сил")
               || name.contains("strength")
               || name.contains("огнестойк")
               || name.contains("fire")
               || name.contains("исцелен")
               || name.contains("heal")
               || name.contains("реген")
               || name.contains("regen")
               || name.contains("невидим")
               || name.contains("invis")
               || name.contains("прыгуч")
               || name.contains("jump")
               || name.contains("паден")
               || name.contains("мощ")
               || name.contains("поглощен")
               || name.contains("насыщен")
               || name.contains("удач")
               || name.contains("зелье")
               || name.contains("potion")
            : false;
      } else {
         return false;
      }
   }

   public static int getScreenSlot(int invSlot) {
      if (invSlot < 9) {
         return invSlot + 36;
      } else {
         return invSlot == 40 ? 45 : invSlot;
      }
   }

   public static int findItemInBundle(class_1799 bundleStack, class_1799 target) {
      if (bundleStack != null && !bundleStack.method_7960() && target != null && !target.method_7960()) {
         try {
            if (bundleStack.method_58694(class_9334.field_49650) instanceof class_9276 bundleComp) {
               int size = bundleComp.method_57426();

               for (int i = 0; i < size; i++) {
                  class_1799 nested = bundleComp.method_57422(i);
                  if (class_1799.method_7973(nested, target)) {
                     return i;
                  }
               }
            }
         } catch (Throwable var7) {
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static boolean isSafeToSacrifice(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_1792 item = stack.method_7909();
         if (item == null) {
            return false;
         } else if (item instanceof class_5537) {
            return false;
         } else if (item == class_1802.field_8288) {
            return false;
         } else {
            try {
               if (stack.method_58694(class_9334.field_49608) != null) {
                  return false;
               }
            } catch (Throwable var5) {
            }

            String trans = item.method_7876() != null ? item.method_7876().toLowerCase() : "";
            if (!trans.contains("sword")
               && !trans.contains("axe")
               && !trans.contains("pickaxe")
               && !trans.contains("shovel")
               && !trans.contains("hoe")
               && !trans.contains("mace")
               && !trans.contains("bow")
               && !trans.contains("crossbow")
               && !trans.contains("trident")
               && !trans.contains("helmet")
               && !trans.contains("chestplate")
               && !trans.contains("leggings")
               && !trans.contains("boots")
               && !trans.contains("totem")
               && !trans.contains("bundle")
               && !trans.contains("elytra")
               && !trans.contains("shield")) {
               try {
                  return class_9276.method_61667(stack);
               } catch (Throwable var4) {
                  return true;
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static int findSafeSacrificeSlot(class_1661 inv, int currentSlot, int bundleSlot) {
      for (int i = 9; i < 36; i++) {
         if (i != bundleSlot) {
            class_1799 stack = inv.method_5438(i);
            if (isSafeToSacrifice(stack)) {
               return i;
            }
         }
      }

      for (int ix = 0; ix < 9; ix++) {
         if (ix != currentSlot && ix != bundleSlot) {
            class_1799 stack = inv.method_5438(ix);
            if (isSafeToSacrifice(stack)) {
               return ix;
            }
         }
      }

      return -1;
   }

   public void throwPotion(PotionHelper.PotionEntry entry) {
      if (entry != null && mc.field_1724 != null && mc.field_1761 != null) {
         if (this.pendingStep == 0) {
            AttackAura.stoptick = 5;
            if (mc.field_1755 != null) {
               mc.method_1507(null);
            }

            PotionHelper.PotionEntry.SlotRef primary = entry.getPrimarySlot();
            int currentSlot = mc.field_1724.method_31548().method_67532();
            float yaw = mc.field_1724.method_36454();
            float oldPitch = mc.field_1724.method_36455();
            boolean buff = isBuff(entry.getSampleStack()) || this.throwDirection.is("В ноги");
            if (primary.isBundle()) {
               this.pendingEntry = entry;
               this.pendingStep = 10;
               this.pendingTicks = 0;
               this.bundleInvSlot = primary.invSlot();
               this.bundleItemIndex = primary.bundleIndex();
               this.bundleOriginalHotbar = currentSlot;
               this.bundleSacrificedStack = class_1799.field_8037;
               this.bundleSwappedToHotbar = false;
            } else {
               int slot = primary.invSlot();
               if (slot == 40) {
                  if (buff) {
                     mc.field_1724.field_3944.method_52787(new class_2831(yaw, 89.9F, mc.field_1724.method_24828(), false));
                  }

                  mc.field_1761.method_2919(mc.field_1724, class_1268.field_5810);
                  mc.field_1724.method_6104(class_1268.field_5810);
                  if (buff) {
                     mc.field_1724.field_3944.method_52787(new class_2831(yaw, oldPitch, mc.field_1724.method_24828(), false));
                  }
               } else if (slot < 9) {
                  if (slot != currentSlot) {
                     mc.field_1724.method_31548().method_61496(slot);
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                  }

                  if (buff) {
                     mc.field_1724.field_3944.method_52787(new class_2831(yaw, 89.9F, mc.field_1724.method_24828(), false));
                  }

                  mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                  mc.field_1724.method_6104(class_1268.field_5808);
                  if (buff) {
                     mc.field_1724.field_3944.method_52787(new class_2831(yaw, oldPitch, mc.field_1724.method_24828(), false));
                  }

                  if (slot != currentSlot) {
                     mc.field_1724.method_31548().method_61496(currentSlot);
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                  }
               } else {
                  this.pendingEntry = entry;
                  this.pendingTargetHotbar = currentSlot;
                  this.pendingStep = 1;
                  this.pendingTicks = 0;
               }
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (this.pendingEntry != null && mc.field_1724 != null && mc.field_1761 != null) {
         AttackAura.stoptick = 5;
         this.pendingTicks++;
         if (this.pendingTicks > 15) {
            this.resetPending();
         } else {
            int syncId = mc.field_1724.field_7512.field_7763;
            float yaw = mc.field_1724.method_36454();
            float oldPitch = mc.field_1724.method_36455();
            boolean buff = isBuff(this.pendingEntry.getSampleStack()) || this.throwDirection.is("В ноги");
            if (this.pendingStep < 10) {
               int slot = this.pendingEntry.getSlot();
               int hotbar = this.pendingTargetHotbar;
               switch (this.pendingStep) {
                  case 1:
                     mc.field_1761.method_2906(0, slot, hotbar, class_1713.field_7791, mc.field_1724);
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     this.pendingStep = 2;
                     break;
                  case 2:
                     if (buff) {
                        mc.field_1724.field_3944.method_52787(new class_2831(yaw, 89.9F, mc.field_1724.method_24828(), false));
                     }

                     mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                     mc.field_1724.method_6104(class_1268.field_5808);
                     if (buff) {
                        mc.field_1724.field_3944.method_52787(new class_2831(yaw, oldPitch, mc.field_1724.method_24828(), false));
                     }

                     this.pendingStep = 3;
                     break;
                  case 3:
                     mc.field_1761.method_2906(0, slot, hotbar, class_1713.field_7791, mc.field_1724);
                     mc.field_1724.field_3944.method_52787(new class_2815(0));
                     mc.field_1724.method_7346();
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     this.resetPending();
               }
            } else {
               switch (this.pendingStep) {
                  case 10:
                     int bInv = this.bundleInvSlot;
                     class_1799 bundleStack = bInv == 40 ? mc.field_1724.method_6079() : mc.field_1724.method_31548().method_5438(bInv);
                     if (bundleStack == null || bundleStack.method_7960()) {
                        this.resetPending();
                        return;
                     }

                     if (!(bundleStack.method_58694(class_9334.field_49650) instanceof class_9276 bundleComp) || bundleComp.method_57426() == 0) {
                        this.resetPending();
                        return;
                     }

                     int exactIndex = this.bundleItemIndex;
                     boolean found = false;
                     if (exactIndex >= 0
                        && exactIndex < bundleComp.method_57426()
                        && arePotionsEquivalent(this.pendingEntry.getSampleStack(), bundleComp.method_57422(exactIndex))) {
                        found = true;
                     }

                     if (!found) {
                        for (int i = 0; i < bundleComp.method_57426(); i++) {
                           if (arePotionsEquivalent(this.pendingEntry.getSampleStack(), bundleComp.method_57422(i))) {
                              exactIndex = i;
                              found = true;
                              break;
                           }
                        }
                     }

                     if (!found) {
                        for (PotionHelper.PotionEntry.SlotRef ref : this.pendingEntry.getSlots()) {
                           if (ref.isBundle()) {
                              class_1799 altB = ref.invSlot() == 40 ? mc.field_1724.method_6079() : mc.field_1724.method_31548().method_5438(ref.invSlot());
                              if (altB.method_58694(class_9334.field_49650) instanceof class_9276 altComp) {
                                 for (int ix = 0; ix < altComp.method_57426(); ix++) {
                                    if (arePotionsEquivalent(this.pendingEntry.getSampleStack(), altComp.method_57422(ix))) {
                                       bInv = ref.invSlot();
                                       bundleStack = altB;
                                       exactIndex = ix;
                                       found = true;
                                       break;
                                    }
                                 }
                              }

                              if (found) {
                                 break;
                              }
                           }
                        }
                     }

                     if (!found) {
                        NotificationManager.sendWithSound("Зелье не найдено в мешочке", NotificationManager.Type.WARNING, this.soundMode.getValue());
                        this.resetPending();
                        return;
                     }

                     this.bundleInvSlot = bInv;
                     this.bundleItemIndex = exactIndex;
                     class_1661 inv = mc.field_1724.method_31548();
                     int cur = this.bundleOriginalHotbar;
                     int destSlot = -1;

                     for (int ixx = 0; ixx < 9; ixx++) {
                        if (ixx != cur && inv.method_5438(ixx).method_7960()) {
                           destSlot = ixx;
                           break;
                        }
                     }

                     if (destSlot == -1 && inv.method_5438(cur).method_7960()) {
                        destSlot = cur;
                     }

                     if (destSlot == -1) {
                        for (int ixxx = 9; ixxx < 36; ixxx++) {
                           if (ixxx != bInv && inv.method_5438(ixxx).method_7960()) {
                              destSlot = ixxx;
                              break;
                           }
                        }
                     }

                     class_1799 sacrificed = class_1799.field_8037;
                     if (destSlot == -1) {
                        destSlot = findSafeSacrificeSlot(inv, cur, bInv);
                        if (destSlot == -1) {
                           NotificationManager.sendWithSound(
                              "Инвентарь полон (нет безопасного слота)", NotificationManager.Type.WARNING, this.soundMode.getValue()
                           );
                           this.resetPending();
                           return;
                        }

                        sacrificed = inv.method_5438(destSlot).method_7972();
                     }

                     this.bundleDestInvSlot = destSlot;
                     this.bundleSacrificedStack = sacrificed;
                     int screenBundle = getScreenSlot(bInv);
                     int screenDest = getScreenSlot(destSlot);
                     class_5537.method_61637(bundleStack, exactIndex);
                     mc.field_1724.field_3944.method_52787(new class_9837(screenBundle, exactIndex));
                     mc.field_1761.method_2906(syncId, screenBundle, 1, class_1713.field_7790, mc.field_1724);
                     mc.field_1761.method_2906(syncId, screenDest, 0, class_1713.field_7790, mc.field_1724);
                     if (!mc.field_1724.field_7512.method_34255().method_7960()) {
                        mc.field_1761.method_2906(syncId, screenBundle, 0, class_1713.field_7790, mc.field_1724);
                     }

                     mc.field_1724.field_3944.method_52787(new class_2815(syncId));
                     if (destSlot < 9) {
                        this.bundleSwappedToHotbar = false;
                        mc.field_1724.method_31548().method_61496(destSlot);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     } else {
                        this.bundleSwappedToHotbar = true;
                        mc.field_1761.method_2906(syncId, screenDest, cur, class_1713.field_7791, mc.field_1724);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     }

                     this.pendingStep = 11;
                     break;
                  case 11:
                     if (buff) {
                        mc.field_1724.field_3944.method_52787(new class_2831(yaw, 89.9F, mc.field_1724.method_24828(), false));
                     }

                     mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                     mc.field_1724.method_6104(class_1268.field_5808);
                     if (buff) {
                        mc.field_1724.field_3944.method_52787(new class_2831(yaw, oldPitch, mc.field_1724.method_24828(), false));
                     }

                     if (this.bundleSwappedToHotbar) {
                        int screenDest = getScreenSlot(this.bundleDestInvSlot);
                        mc.field_1761.method_2906(syncId, screenDest, this.bundleOriginalHotbar, class_1713.field_7791, mc.field_1724);
                        mc.field_1724.field_3944.method_52787(new class_2815(syncId));
                     }

                     if (mc.field_1724.method_31548().method_67532() != this.bundleOriginalHotbar) {
                        mc.field_1724.method_31548().method_61496(this.bundleOriginalHotbar);
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     }

                     if (this.bundleSacrificedStack != null && !this.bundleSacrificedStack.method_7960()) {
                        this.pendingStep = 12;
                     } else {
                        mc.field_1724.field_3944.method_52787(new class_2815(syncId));
                        mc.field_1724.method_7346();
                        ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                        this.resetPending();
                     }
                     break;
                  case 12:
                     int bInv = this.bundleInvSlot;
                     class_1799 bundleStack = bInv == 40 ? mc.field_1724.method_6079() : mc.field_1724.method_31548().method_5438(bInv);
                     int restoredIdx = findItemInBundle(bundleStack, this.bundleSacrificedStack);
                     if (restoredIdx != -1) {
                        int screenBundle = getScreenSlot(bInv);
                        int screenDest = getScreenSlot(this.bundleDestInvSlot);
                        class_5537.method_61637(bundleStack, restoredIdx);
                        mc.field_1724.field_3944.method_52787(new class_9837(screenBundle, restoredIdx));
                        mc.field_1761.method_2906(syncId, screenBundle, 1, class_1713.field_7790, mc.field_1724);
                        mc.field_1761.method_2906(syncId, screenDest, 0, class_1713.field_7790, mc.field_1724);
                        if (!mc.field_1724.field_7512.method_34255().method_7960()) {
                           mc.field_1761.method_2906(syncId, screenBundle, 0, class_1713.field_7790, mc.field_1724);
                        }

                        mc.field_1724.field_3944.method_52787(new class_2815(syncId));
                     }

                     mc.field_1724.method_7346();
                     ClientInteractionAccess.syncSelectedSlot(mc.field_1761);
                     this.resetPending();
               }
            }
         }
      } else {
         this.resetPending();
      }
   }

   private void resetPending() {
      this.pendingStep = 0;
      this.pendingEntry = null;
      this.pendingTicks = 0;
      this.bundleInvSlot = -1;
      this.bundleItemIndex = -1;
      this.bundleDestInvSlot = -1;
      this.bundleSwappedToHotbar = false;
      this.bundleSacrificedStack = class_1799.field_8037;
   }

   public static class PotionEntry {
      private final class_1799 sampleStack;
      private final String name;
      private final List<PotionHelper.PotionEntry.SlotRef> slots = new ArrayList<>();

      public PotionEntry(class_1799 sampleStack, String name, int invSlot, int bundleIndex) {
         this.sampleStack = sampleStack;
         this.name = name;
         this.slots.add(new PotionHelper.PotionEntry.SlotRef(invSlot, bundleIndex));
      }

      public void addSlot(int invSlot, int bundleIndex) {
         this.slots.add(new PotionHelper.PotionEntry.SlotRef(invSlot, bundleIndex));
      }

      public class_1799 getSampleStack() {
         return this.sampleStack;
      }

      public String getName() {
         return this.name;
      }

      public PotionHelper.PotionEntry.SlotRef getPrimarySlot() {
         return this.slots.isEmpty() ? new PotionHelper.PotionEntry.SlotRef(-1, -1) : this.slots.get(0);
      }

      public int getSlot() {
         return this.getPrimarySlot().invSlot();
      }

      public boolean isFromBundle() {
         return this.getPrimarySlot().isBundle();
      }

      public int getBundleInvSlot() {
         return this.getPrimarySlot().invSlot();
      }

      public int getBundleIndex() {
         return this.getPrimarySlot().bundleIndex();
      }

      public int getBundleSlot() {
         return this.getBundleInvSlot();
      }

      public List<PotionHelper.PotionEntry.SlotRef> getSlots() {
         return this.slots;
      }

      public int getCount() {
         return this.slots.size();
      }

      public static record SlotRef(int invSlot, int bundleIndex) {
         public boolean isBundle() {
            return this.bundleIndex >= 0;
         }
      }
   }
}
