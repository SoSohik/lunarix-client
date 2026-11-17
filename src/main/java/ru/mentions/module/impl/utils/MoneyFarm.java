package ru.mentions.module.impl.utils;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1714;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_476;
import net.minecraft.class_479;
import net.minecraft.class_7439;
import net.minecraft.class_7923;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.other.TimerUtil;

@ModuleInfo(
   name = "MoneyFarm",
   desc = "Крафтит и продает изумрудные предметы",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class MoneyFarm extends Module implements IMinecraft {
   private static final String IZUMRUDNYY_MECH = "Изумрудный меч";
   private static final String IZUMRUDNAYA_KIRKA = "Изумрудная кирка";
   private static final String DEREVO = "Дерево";
   private static final int INT_VALUE = 5;
   private static final long TIMESTAMP = 250L;
   private static final long TIMESTAMP_2 = 1200L;
   private static final long TIMESTAMP_3 = 7000L;
   private static final long TIMESTAMP_4 = 5000L;
   private static final int INT_VALUE_2 = 9;
   private static final int INT_VALUE_3 = 20;
   private final ModeSetting predmet = new ModeSetting(this, "Предмет", "Изумрудный меч", "Изумрудный меч", "Изумрудная кирка");
   private final StringSetting tsenaProdazhi = new StringSetting(this, "Цена продажи", "40000", true);
   private final StringSetting maksTsenaDereva = new StringSetting(this, "Макс. цена дерева", "0", true);
   private final SliderSetting zaderzhkaMs = new SliderSetting(this, "Задержка (мс)", 100.0F, 50.0F, 5000.0F, 50.0F);
   private final BooleanSetting avtoPokupka = new BooleanSetting(this, "Авто-покупка", true);
   private final BooleanSetting avtoProdazha = new BooleanSetting(this, "Авто-продажа", false);
   private final BooleanSetting uvedomleniya = new BooleanSetting(this, "Уведомления", true);
   private MoneyFarm.MoneyFarmState2 moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
   private final TimerUtil dualTimer = new TimerUtil();
   private final TimerUtil dualTimer2 = new TimerUtil();
   private final TimerUtil dualTimer3 = new TimerUtil();
   private final TimerUtil dualTimer4 = new TimerUtil();
   private final TimerUtil dualTimer5 = new TimerUtil();
   private class_2338 blockPos;
   private MoneyFarm.MoneyFarmState moneyFarmState = MoneyFarm.MoneyFarmState.ITEM;
   private int intValue = 0;
   private int intValue2 = 0;
   private int intValue3 = 0;
   private int intValue4 = 0;
   private int intValue5 = 0;
   private boolean flag = false;
   private boolean flag2 = false;
   private boolean flag3 = false;
   private boolean flag4 = false;
   private boolean flag5 = false;
   private boolean flag6 = false;
   private int intValue6 = 0;
   private int intValue7 = 0;
   private int intValue8 = 0;
   private int intValue9 = 0;
   private boolean flag7 = false;
   private int intValue10 = 0;
   private long timestamp = 0L;
   private long timestamp2 = 0L;

   @Override
   protected void onEnable() {
      super.onEnable();
      this.invoke43();
   }

   @Override
   protected void onDisable() {
      int intValue = this.intValue;
      this.invoke43();
      super.onDisable();
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && !event.isSend() && event.getPacket() instanceof class_7439 gameMessageS2CPacket) {
         String text = gameMessageS2CPacket.comp_763().getString();
         String text2 = this.resolve6(text);
         if (text2.contains("не удалось выставить") && text2.contains("освободите хранилище")) {
            this.flag = true;
            this.flag2 = false;
            this.flag5 = true;
         } else {
            if (text2.contains("у вас купили") && text2.contains(this.resolve6(this.resolve4()))) {
               this.flag3 = true;
               this.intValue8++;
               this.intValue9 = Math.max(0, this.intValue9 - 1);
               this.intValue3 = 0;
               this.intValue2 = 0;
               this.flag7 = false;
               this.timestamp2 = System.currentTimeMillis();
            }

            if ((text2.contains("выставлен") || text2.contains("выставлено") || text2.contains("успешно выстав")) && text2.contains("продаж")) {
               int intValue2 = this.intValue7 > 0 ? this.intValue7 : 1;
               this.flag = false;
               this.flag2 = true;
               this.intValue4 += intValue2;
               this.intValue9 += intValue2;
               this.intValue8 = Math.max(0, this.intValue8 - intValue2);
               this.flag7 = true;
               this.intValue7 = 0;
               this.timestamp2 = System.currentTimeMillis();
            }

            if (text2.contains("вы успешно купили")
               && (
                  this.moneyFarmState2 == MoneyFarm.MoneyFarmState2.BUY_WOOD_WAITING_CONFIRM
                     || this.moneyFarmState2 == MoneyFarm.MoneyFarmState2.BUY_WOOD_CONFIRMING
                     || this.moneyFarmState2 == MoneyFarm.MoneyFarmState2.BUY_WOOD_CLOSING
               )) {
               this.flag4 = true;
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         this.invoke42();
         if (this.flag && this.moneyFarmState2 == MoneyFarm.MoneyFarmState2.IDLE) {
            this.invoke33();
         }

         switch (this.moneyFarmState2) {
            case IDLE:
               this.invoke();
               break;
            case BUY_WOOD_SEARCHING:
               this.invoke2();
               break;
            case BUY_WOOD_WAITING_AUCTION:
               this.invoke3();
               break;
            case BUY_WOOD_READING_AUCTION:
               this.invoke4();
               break;
            case BUY_WOOD_WAITING_CONFIRM:
               this.invoke5();
               break;
            case BUY_WOOD_CONFIRMING:
               this.invoke6();
               break;
            case BUY_WOOD_CLOSING:
               this.invoke7();
               break;
            case BUY_OPENING_SHOP:
               this.invoke8();
               break;
            case BUY_WAITING_SHOP:
               this.invoke9();
               break;
            case BUY_FIND_GOLD_INGOT:
               this.invoke10();
               break;
            case BUY_WAITING_EMERALD_MENU:
               this.invoke11();
               break;
            case BUY_FIND_EMERALD:
               this.invoke12();
               break;
            case BUY_WAITING_CONFIRM:
               this.invoke13();
               break;
            case BUY_CLICK_LIME_PANE:
               this.invoke14();
               break;
            case BUY_CLOSING_SHOP:
               this.invoke15();
               break;
            case CHECK_SELL_GUI_OPENING:
               this.invoke16();
               break;
            case CHECK_SELL_GUI_WAITING:
               this.invoke17();
               break;
            case CHECK_SELL_GUI_READING:
               this.invoke18();
               break;
            case FINDING_CRAFTING_TABLE:
               this.invoke19();
               break;
            case AIMING_CRAFTING_TABLE:
               this.invoke20();
               break;
            case OPENING_CRAFTING_TABLE:
               this.invoke21();
               break;
            case PLACING_ITEMS:
               this.invoke22();
               break;
            case TAKING_RESULT:
               this.invoke23();
               break;
            case CLOSING_CRAFTING:
               this.invoke24();
               break;
            case SELLING:
               this.invoke25();
               break;
            case WAITING_SELL_RESULT:
               this.invoke26();
               break;
            case RESALE_SEARCH_OWN_AH:
               this.invoke27();
               break;
            case RESALE_WAITING_OWN_AH:
               this.invoke28();
               break;
            case RESALE_TAKE_ITEM:
               this.invoke29();
               break;
            case RESALE_CLOSING:
               this.invoke30();
               break;
            case RESALE_SELLING:
               this.invoke31();
               break;
            case RESALE_WAIT_SELL_RESULT:
               this.invoke32();
         }
      }
   }

   private void invoke() {
      if (this.dualTimer.isReached(this.compute23())) {
         if (this.flag) {
            this.invoke33();
         } else {
            if (this.avtoProdazha.getValue() && this.intValue8 > 0) {
               this.flag3 = false;
               if (!this.flag7) {
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.CHECK_SELL_GUI_OPENING;
                  this.dualTimer.reset();
                  return;
               }

               if (this.compute5() > 0 && this.compute12() >= this.compute5()) {
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.SELLING;
                  this.dualTimer.reset();
                  return;
               }
            } else {
               if (this.avtoProdazha.getValue() && this.intValue9 > 0 && System.currentTimeMillis() - this.timestamp2 >= 5000L) {
                  this.invoke33();
                  return;
               }

               if (this.avtoProdazha.getValue() && this.intValue9 > 0) {
                  return;
               }

               if (this.avtoProdazha.getValue() && !this.flag7) {
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.CHECK_SELL_GUI_OPENING;
                  this.dualTimer.reset();
                  return;
               }
            }

            if (this.avtoProdazha.getValue() && this.intValue9 == 0 && this.compute5() > 0 && this.compute12() >= this.compute5()) {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.SELLING;
               this.dualTimer.reset();
            } else if (!this.avtoProdazha.getValue() || !this.flag7 || this.compute5() > 0) {
               if (this.avtoProdazha.getValue() && this.flag7 && this.compute5() > 0 && this.compute12() >= this.compute5()) {
                  if (this.compute12() > 0) {
                     this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.SELLING;
                  }

                  this.dualTimer.reset();
               } else if (this.avtoPokupka.getValue() && this.check2()) {
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_OPENING_SHOP;
                  this.dualTimer.reset();
               } else if (this.check3()) {
                  if (this.check4()) {
                     this.moneyFarmState = MoneyFarm.MoneyFarmState.STICKS;
                     this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.FINDING_CRAFTING_TABLE;
                     this.dualTimer.reset();
                  } else if (this.check5()) {
                     this.moneyFarmState = MoneyFarm.MoneyFarmState.PLANKS;
                     this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.FINDING_CRAFTING_TABLE;
                     this.dualTimer.reset();
                  } else if (this.avtoPokupka.getValue() && this.compute21() > 0L) {
                     this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_SEARCHING;
                     this.dualTimer.reset();
                  }
               } else if (this.check()) {
                  this.moneyFarmState = MoneyFarm.MoneyFarmState.ITEM;
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.FINDING_CRAFTING_TABLE;
                  this.dualTimer.reset();
               }
            }
         }
      }
   }

   private void invoke2() {
      if (this.dualTimer.isReached(this.compute23()) && mc.field_1755 == null) {
         long longValue = this.compute21();
         if (longValue <= 0L) {
            this.invoke44("§cМаксимальная цена дерева не задана.");
         } else {
            this.invoke38("Дерево");
            this.intValue5 = 0;
            this.flag4 = false;
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_WAITING_AUCTION;
            this.dualTimer.reset();
            this.dualTimer3.reset();
            this.dualTimer4.reset();
            this.dualTimer5.reset();
         }
      }
   }

   private void invoke3() {
      if (mc.field_1755 instanceof class_476 genericContainerScreen2 && (isAuctionScreen(genericContainerScreen2) || this.check16(genericContainerScreen2))) {
         this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_READING_AUCTION;
         this.dualTimer.reset();
         return;
      }

      if (this.dualTimer.isReached(10000L)) {
         this.invoke44("§cТаймаут поиска дерева.");
      }
   }

   private void invoke4() {
      if (this.dualTimer.isReached(150L)) {
         if (mc.field_1755 instanceof class_476 genericContainerScreen3) {
            if (this.check16(genericContainerScreen3)) {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_CONFIRMING;
               this.dualTimer.reset();
            } else if (!isAuctionScreen(genericContainerScreen3)) {
               if (this.dualTimer.isReached(10000L)) {
                  this.invoke46();
                  this.invoke44("§cОткрылся не экран аукциона при покупке дерева.");
               }
            } else {
               long longValue2 = this.compute21();
               boolean flag = this.check11(genericContainerScreen3);
               class_1703 screenHandler2 = genericContainerScreen3.method_17577();
               boolean flag2 = false;

               for (int intValue3 = 0; intValue3 < Math.min(45, screenHandler2.field_7761.size()); intValue3++) {
                  class_1735 slot2 = screenHandler2.method_7611(intValue3);
                  if (this.check9(slot2) && (flag || this.check10(slot2))) {
                     long longValue3 = priceOf(slot2);
                     String text3 = sellerOf(slot2);
                     if ((mc.field_1724 == null || text3 == null || !text3.equalsIgnoreCase(mc.field_1724.method_5477().getString()))
                        && longValue3 > 0L
                        && longValue3 <= longValue2) {
                        flag2 = true;
                        if (this.dualTimer4.isReached(Math.max(50L, this.compute23()))) {
                           mc.field_1761.method_2906(screenHandler2.field_7763, intValue3, 0, class_1713.field_7794, mc.field_1724);
                           this.dualTimer4.reset();
                           this.dualTimer5.reset();
                           this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_WAITING_CONFIRM;
                           this.dualTimer.reset();
                           return;
                        }
                        break;
                     }
                  }
               }

               if (!flag2 && this.dualTimer3.isReached(250L)) {
                  if (screenHandler2.field_7761.size() > 49) {
                     mc.field_1761.method_2906(screenHandler2.field_7763, 49, 0, class_1713.field_7790, mc.field_1724);
                     this.dualTimer3.reset();
                  } else {
                     this.invoke46();
                     this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_SEARCHING;
                     this.dualTimer.reset();
                  }
               } else if (this.dualTimer.isReached(10000L)) {
                  this.invoke46();
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_SEARCHING;
                  this.dualTimer.reset();
               } else {
                  this.intValue5++;
               }
            }
         } else if (this.dualTimer.isReached(10000L)) {
            this.invoke45();
         }
      }
   }

   private void invoke5() {
      if (this.dualTimer.isReached(50L)) {
         if (this.flag4) {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_CLOSING;
            this.dualTimer.reset();
         } else if (mc.field_1755 instanceof class_476 genericContainerScreen4) {
            if (this.check16(genericContainerScreen4)) {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_CONFIRMING;
               this.dualTimer.reset();
            } else if (this.dualTimer.isReached(4000L)) {
               this.invoke46();
               this.invoke44("§cПодтверждение покупки дерева не открылось.");
            }
         } else if (this.dualTimer.isReached(4000L)) {
            this.invoke44("§cПодтверждение покупки дерева не открылось.");
         }
      }
   }

   private void invoke6() {
      if (this.dualTimer.isReached(50L)) {
         if (this.flag4) {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_CLOSING;
            this.dualTimer.reset();
         } else if (mc.field_1755 instanceof class_476 genericContainerScreen5) {
            int intValue4 = this.compute18(genericContainerScreen5.method_17577());
            if (intValue4 != -1 && this.dualTimer5.isReached(Math.max(50L, this.compute23()))) {
               this.invoke37(genericContainerScreen5, intValue4, 0, class_1713.field_7790);
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_CLOSING;
               this.dualTimer.reset();
               this.dualTimer5.reset();
            } else if (this.dualTimer.isReached(5000L)) {
               this.invoke46();
               this.invoke44("§cКнопка подтверждения покупки дерева не найдена.");
            }
         } else {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WOOD_CLOSING;
            this.dualTimer.reset();
         }
      }
   }

   private void invoke7() {
      if (this.dualTimer.isReached(300L)) {
         this.invoke46();
         if (this.flag4) {
         }

         this.invoke45();
      }
   }

   private void invoke8() {
      if (this.dualTimer.isReached(this.compute23()) && this.check23(150L)) {
         mc.field_1724.field_3944.method_45730("shop");
         this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WAITING_SHOP;
         this.dualTimer.reset();
      }
   }

   private void invoke9() {
      if (mc.field_1755 instanceof class_476) {
         this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_FIND_GOLD_INGOT;
         this.dualTimer.reset();
      } else if (this.dualTimer.isReached(10000L)) {
         this.invoke44("§cТаймаут магазина.");
      }
   }

   private void invoke10() {
      if (this.dualTimer.isReached(this.compute23())) {
         if (mc.field_1755 instanceof class_476 genericContainerScreen6) {
            int intValue5 = this.compute17(genericContainerScreen6, class_1802.field_8695);
            if (intValue5 != -1) {
               this.invoke37(genericContainerScreen6, intValue5, 0, class_1713.field_7790);
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WAITING_EMERALD_MENU;
               this.dualTimer.reset();
            } else if (this.dualTimer.isReached(5000L)) {
               this.invoke46();
               this.invoke44("§cЗолотой слиток не найден.");
            }
         } else {
            this.invoke45();
         }
      }
   }

   private void invoke11() {
      if (this.dualTimer.isReached(this.compute23())) {
         if (!(mc.field_1755 instanceof class_476)) {
            this.invoke45();
         } else {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_FIND_EMERALD;
            this.dualTimer.reset();
         }
      }
   }

   private void invoke12() {
      if (this.dualTimer.isReached(this.compute23())) {
         if (mc.field_1755 instanceof class_476 genericContainerScreen7) {
            int intValue6 = this.compute16(genericContainerScreen7);
            if (intValue6 != -1) {
               this.invoke37(genericContainerScreen7, intValue6, 1, class_1713.field_7790);
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WAITING_CONFIRM;
               this.dualTimer.reset();
            } else if (this.dualTimer.isReached(5000L)) {
               this.invoke46();
               this.invoke44("§cИзумруд не найден.");
            }
         } else {
            this.invoke45();
         }
      }
   }

   private void invoke13() {
      if (this.dualTimer.isReached(this.compute23())) {
         if (!this.check2()) {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_CLOSING_SHOP;
            this.dualTimer.reset();
         } else if (mc.field_1755 instanceof class_476 genericContainerScreen8) {
            if (this.check16(genericContainerScreen8)) {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_CLICK_LIME_PANE;
               this.dualTimer.reset();
            } else if (this.dualTimer.isReached(5000L)) {
               this.invoke46();
               this.invoke44("§cПодтверждение покупки изумрудов не открылось.");
            }
         } else {
            this.invoke45();
         }
      }
   }

   private void invoke14() {
      if (this.dualTimer.isReached(this.compute23())) {
         if (!this.check2()) {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_CLOSING_SHOP;
            this.dualTimer.reset();
         } else if (mc.field_1755 instanceof class_476 genericContainerScreen9) {
            if (!this.check16(genericContainerScreen9)) {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_WAITING_CONFIRM;
               this.dualTimer.reset();
            } else {
               int intValue7 = this.compute18(genericContainerScreen9.method_17577());
               if (intValue7 != -1) {
                  this.invoke37(genericContainerScreen9, intValue7, 0, class_1713.field_7790);
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.BUY_CLOSING_SHOP;
                  this.dualTimer.reset();
               } else if (this.dualTimer.isReached(5000L)) {
                  this.invoke46();
                  this.invoke44("§cЛаймовая панель не найдена.");
               }
            }
         } else {
            this.invoke45();
         }
      }
   }

   private void invoke15() {
      if (this.check23(150L)) {
         this.invoke45();
      }
   }

   private void invoke16() {
      if (this.dualTimer.isReached(50L) && mc.field_1755 == null) {
         long longValue4 = this.compute20();
         if (longValue4 <= 0L) {
            this.invoke44("§cЦена продажи не задана.");
         } else if (this.check21()) {
            this.invoke41();
            this.invoke40(longValue4);
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.CHECK_SELL_GUI_WAITING;
            this.dualTimer.reset();
         }
      }
   }

   private void invoke17() {
      if (mc.field_1755 instanceof class_476 genericContainerScreen10 && this.check17(genericContainerScreen10)) {
         this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.CHECK_SELL_GUI_READING;
         this.dualTimer.reset();
         return;
      }

      if (this.dualTimer.isReached(7000L)) {
         this.invoke46();
         this.invoke44("§cSellgui не открылся для проверки слотов.");
      }
   }

   private void invoke18() {
      if (this.dualTimer.isReached(150L)) {
         if (mc.field_1755 instanceof class_476 genericContainerScreen11 && this.check17(genericContainerScreen11)) {
            int intValue8 = this.compute4(genericContainerScreen11);
            this.intValue3 = intValue8;
            if (this.intValue8 > 0) {
               this.intValue8 = intValue8;
            }

            this.flag7 = true;
            this.intValue2 = 0;
            this.invoke46();
            if (intValue8 <= 0) {
               this.invoke33();
            } else {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
               this.dualTimer.reset();
            }

            return;
         }

         this.invoke45();
      }
   }

   private void invoke19() {
      if (this.check23(150L) && this.dualTimer.isReached(this.compute23())) {
         this.blockPos = this.resolve();
         if (this.blockPos == null) {
            this.invoke44("§cВерстак рядом не найден.");
         } else {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.AIMING_CRAFTING_TABLE;
            this.dualTimer.reset();
         }
      }
   }

   private void invoke20() {
      if (this.blockPos != null && this.check19(this.blockPos)) {
         MoneyFarm.Look look = this.resolve2(class_243.method_24953(this.blockPos));
         this.face(look);
         if (this.angleTo(look) <= 4.0F && this.dualTimer.isReached(this.compute23())) {
            class_3965 blockHitResult = new class_3965(class_243.method_24953(this.blockPos), this.resolve3(this.blockPos), this.blockPos, false);
            mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, blockHitResult);
            mc.field_1724.method_6104(class_1268.field_5808);
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.OPENING_CRAFTING_TABLE;
            this.dualTimer.reset();
         }
      } else {
         this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.FINDING_CRAFTING_TABLE;
         this.dualTimer.reset();
      }
   }

   private void invoke21() {
      if (this.dualTimer.isReached(this.compute23())) {
         if (mc.field_1755 instanceof class_479) {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.PLACING_ITEMS;
            this.dualTimer.reset();
         } else if (this.dualTimer.isReached(5000L)) {
            this.invoke44("§cВерстак не открылся.");
         }
      }
   }

   private void invoke22() {
      if (this.dualTimer.isReached(50L)) {
         if (mc.field_1755 instanceof class_479 craftingScreen) {
            class_1714 craftingScreenHandler2 = (class_1714)craftingScreen.method_17577();
            int intValue9 = craftingScreenHandler2.field_7763;
            switch (this.moneyFarmState) {
               case ITEM:
                  if (this.check22()) {
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8687, 2);
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8687, 5);
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8600, 8);
                  } else {
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8687, 1);
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8687, 2);
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8687, 3);
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8600, 5);
                     this.invoke34(craftingScreenHandler2, intValue9, class_1802.field_8600, 8);
                  }
                  break;
               case PLANKS:
                  this.invoke35(craftingScreenHandler2, intValue9, this::check14, 1);
                  break;
               case STICKS:
                  this.invoke35(craftingScreenHandler2, intValue9, this::check13, 2);
                  this.invoke35(craftingScreenHandler2, intValue9, this::check13, 5);
            }

            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.TAKING_RESULT;
            this.dualTimer.reset();
         } else {
            this.invoke45();
         }
      }
   }

   private void invoke23() {
      if (this.dualTimer.isReached(50L)) {
         if (mc.field_1755 instanceof class_479 craftingScreen2) {
            mc.field_1761.method_2906(((class_1714)craftingScreen2.method_17577()).field_7763, 0, 0, class_1713.field_7794, mc.field_1724);
            if (this.moneyFarmState == MoneyFarm.MoneyFarmState.ITEM) {
               this.intValue++;
               this.intValue2++;
            } else if (this.moneyFarmState == MoneyFarm.MoneyFarmState.PLANKS) {
            }

            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.CLOSING_CRAFTING;
            this.dualTimer.reset();
         } else {
            this.invoke45();
         }
      }
   }

   private void invoke24() {
      if (this.dualTimer.isReached(50L)) {
         this.invoke46();
         if (this.moneyFarmState == MoneyFarm.MoneyFarmState.ITEM && this.avtoProdazha.getValue()) {
            int intValue10 = this.compute5();
            int intValue11 = this.compute12();
            if (intValue10 > 0 && intValue11 < intValue10) {
               this.moneyFarmState2 = this.check() ? MoneyFarm.MoneyFarmState2.FINDING_CRAFTING_TABLE : MoneyFarm.MoneyFarmState2.IDLE;
            } else {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.SELLING;
            }
         } else {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
         }

         this.dualTimer.reset();
      }
   }

   private void invoke25() {
      if (this.dualTimer.isReached(50L)) {
         if (this.flag) {
            this.invoke33();
         } else {
            long longValue5 = this.compute20();
            if (longValue5 <= 0L) {
               this.invoke44("§cЦена продажи не задана.");
            } else if (!this.check6()) {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
               this.dualTimer.reset();
            } else {
               if (mc.field_1755 instanceof class_476 genericContainerScreen12 && this.check17(genericContainerScreen12)) {
                  this.invoke36(genericContainerScreen12, false);
                  return;
               }

               if (mc.field_1755 != null) {
                  this.invoke46();
                  this.dualTimer.reset();
               } else if (this.check21()) {
                  this.invoke41();
                  this.flag2 = false;
                  this.flag = false;
                  this.invoke40(longValue5);
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.WAITING_SELL_RESULT;
                  this.dualTimer.reset();
               }
            }
         }
      }
   }

   private void invoke26() {
      if (this.flag) {
         this.invoke33();
      } else {
         if (mc.field_1755 instanceof class_476 genericContainerScreen13 && this.check17(genericContainerScreen13)) {
            this.invoke36(genericContainerScreen13, false);
            return;
         }

         if (this.flag2) {
            this.flag2 = false;
            this.intValue2 = 0;
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
            this.dualTimer.reset();
         } else if (this.dualTimer.isReached(7000L)) {
            this.invoke46();
            this.moneyFarmState2 = this.check6() ? MoneyFarm.MoneyFarmState2.SELLING : MoneyFarm.MoneyFarmState2.IDLE;
            this.dualTimer.reset();
         }
      }
   }

   private void invoke27() {
      if (this.dualTimer.isReached(this.compute23()) && mc.field_1755 == null) {
         if (!this.flag5 && this.check6()) {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_SELLING;
            this.dualTimer.reset();
         } else {
            this.flag5 = false;
            String text4 = mc.field_1724.method_5477().getString();
            this.invoke39(text4);
            this.intValue5 = 0;
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_WAITING_OWN_AH;
            this.dualTimer.reset();
         }
      }
   }

   private void invoke28() {
      if (mc.field_1755 instanceof class_476 genericContainerScreen14 && this.check20(genericContainerScreen14)) {
         this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_TAKE_ITEM;
         this.dualTimer.reset();
         return;
      }

      if (this.dualTimer.isReached(10000L)) {
         this.invoke44("§cТаймаут поиска своих товаров.");
      }
   }

   private void invoke29() {
      if (this.dualTimer.isReached(200L)) {
         if (mc.field_1755 instanceof class_476 genericContainerScreen15) {
            if (!this.check20(genericContainerScreen15)) {
               if (this.dualTimer.isReached(10000L)) {
                  this.invoke46();
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_SEARCH_OWN_AH;
                  this.dualTimer.reset();
               }
            } else {
               int intValue12 = this.compute13(genericContainerScreen15);
               if (intValue12 != -1) {
                  this.invoke37(genericContainerScreen15, intValue12, 0, class_1713.field_7794);
                  this.intValue5 = 0;
                  this.dualTimer.reset();
               } else if (this.intValue5++ < 2) {
                  this.dualTimer.reset();
               } else {
                  this.invoke46();
                  if (this.check6()) {
                     this.intValue9 = 0;
                     this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_SELLING;
                  } else {
                     this.flag = false;
                     this.intValue4 = 0;
                     this.intValue9 = 0;
                     this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
                  }

                  this.dualTimer.reset();
               }
            }
         } else {
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_SEARCH_OWN_AH;
            this.dualTimer.reset();
         }
      }
   }

   private int compute(class_476 genericContainerScreen) {
      int intValue13 = 0;
      int intValue14 = this.compute19(genericContainerScreen);

      for (int intValue15 = 0; intValue15 < intValue14; intValue15++) {
         class_1735 slot3 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue15);
         if (slot3.method_7681() && this.check8(slot3.method_7677(), true)) {
            intValue13++;
         }
      }

      return intValue13;
   }

   private void invoke30() {
      if (this.dualTimer.isReached(300L)) {
         this.invoke46();
         this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_SELLING;
         this.dualTimer.reset();
      }
   }

   private void invoke31() {
      if (this.dualTimer.isReached(50L)) {
         if (this.flag) {
            this.invoke33();
         } else {
            long longValue6 = this.compute20();
            if (longValue6 <= 0L) {
               this.invoke44("§cЦена продажи не задана.");
            } else if (!this.check6()) {
               this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_SEARCH_OWN_AH;
               this.dualTimer.reset();
            } else {
               if (mc.field_1755 instanceof class_476 genericContainerScreen16 && this.check17(genericContainerScreen16)) {
                  this.invoke36(genericContainerScreen16, true);
                  return;
               }

               if (mc.field_1755 != null) {
                  this.invoke46();
                  this.dualTimer.reset();
               } else if (this.check21()) {
                  this.invoke41();
                  this.flag2 = false;
                  this.flag = false;
                  this.invoke40(longValue6);
                  this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_WAIT_SELL_RESULT;
                  this.dualTimer.reset();
               }
            }
         }
      }
   }

   private void invoke32() {
      if (this.flag3) {
         this.flag3 = false;
      }

      if (this.flag) {
         this.invoke33();
      } else {
         if (mc.field_1755 instanceof class_476 genericContainerScreen17 && this.check17(genericContainerScreen17)) {
            this.invoke36(genericContainerScreen17, true);
            return;
         }

         if (this.flag2) {
            this.flag2 = false;
            this.intValue4 = 0;
            this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
            this.dualTimer.reset();
         } else if (this.dualTimer.isReached(7000L)) {
            this.invoke46();
            this.moneyFarmState2 = this.check6() ? MoneyFarm.MoneyFarmState2.RESALE_SELLING : MoneyFarm.MoneyFarmState2.IDLE;
            this.dualTimer.reset();
         }
      }
   }

   private void invoke33() {
      this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.RESALE_SEARCH_OWN_AH;
      this.flag = false;
      this.flag2 = false;
      this.flag5 = true;
      this.intValue2 = 0;
      this.intValue3 = 0;
      this.intValue9 = 0;
      this.intValue8 = 0;
      this.flag7 = false;
      this.dualTimer.reset();
   }

   private void invoke34(class_1714 craftingScreenHandler, int i, class_1792 item, int j) {
      this.invoke35(craftingScreenHandler, i, itemStack -> itemStack.method_31574(item), j);
   }

   private void invoke35(class_1714 craftingScreenHandler, int i, Predicate<class_1799> predicate, int j) {
      int intValue16 = -1;

      for (int intValue17 = 10; intValue17 < craftingScreenHandler.field_7761.size(); intValue17++) {
         class_1735 slot4 = craftingScreenHandler.method_7611(intValue17);
         if (slot4.method_7681() && predicate.test(slot4.method_7677())) {
            intValue16 = intValue17;
            break;
         }
      }

      if (intValue16 != -1) {
         mc.field_1761.method_2906(i, intValue16, 0, class_1713.field_7790, mc.field_1724);
         mc.field_1761.method_2906(i, j, 1, class_1713.field_7790, mc.field_1724);
         mc.field_1761.method_2906(i, intValue16, 0, class_1713.field_7790, mc.field_1724);
      }
   }

   private void invoke36(class_476 genericContainerScreen, boolean bl) {
      class_1703 screenHandler3 = genericContainerScreen.method_17577();
      int intValue18 = this.compute19(genericContainerScreen);
      if (this.flag6) {
         if (this.dualTimer.isReached(1500L)) {
            this.invoke46();
         }
      } else {
         int intValue19 = 0;

         for (int intValue20 = this.compute2(bl); intValue19 < 4 && this.intValue6 < intValue20; intValue19++) {
            int intValue21 = this.compute3(genericContainerScreen);
            int intValue22 = this.compute6(screenHandler3, intValue18);
            if (intValue21 == -1 || intValue22 == -1) {
               break;
            }

            mc.field_1761.method_2906(screenHandler3.field_7763, intValue22, 0, class_1713.field_7790, mc.field_1724);
            mc.field_1761.method_2906(screenHandler3.field_7763, intValue21, 0, class_1713.field_7790, mc.field_1724);
            this.intValue6++;
         }

         if (intValue19 > 0) {
            this.dualTimer.reset();
         } else if (this.intValue6 <= 0) {
            this.invoke46();
            this.moneyFarmState2 = bl ? MoneyFarm.MoneyFarmState2.RESALE_SEARCH_OWN_AH : MoneyFarm.MoneyFarmState2.IDLE;
            this.dualTimer.reset();
         } else {
            int intValue23 = this.compute7(genericContainerScreen);
            if (intValue23 != -1) {
               this.invoke37(genericContainerScreen, intValue23, 0, class_1713.field_7790);
               this.flag6 = true;
               this.intValue7 = this.intValue6;
               this.dualTimer.reset();
            } else if (this.dualTimer.isReached(3000L)) {
               this.invoke46();
               this.moneyFarmState2 = bl ? MoneyFarm.MoneyFarmState2.RESALE_SELLING : MoneyFarm.MoneyFarmState2.SELLING;
               this.dualTimer.reset();
            }
         }
      }
   }

   private int compute2(boolean bl) {
      if (bl) {
         return Integer.MAX_VALUE;
      } else {
         return this.intValue8 > 0 ? this.intValue8 : Math.max(0, this.intValue3);
      }
   }

   private int compute3(class_476 genericContainerScreen) {
      int intValue24 = Math.min(9, this.compute19(genericContainerScreen));

      for (int intValue25 = 0; intValue25 < intValue24; intValue25++) {
         class_1735 slot5 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue25);
         if (!slot5.method_7681()) {
            return intValue25;
         }
      }

      return -1;
   }

   private int compute4(class_476 genericContainerScreen) {
      int intValue26 = 0;
      int intValue27 = Math.min(9, this.compute19(genericContainerScreen));

      for (int intValue28 = 0; intValue28 < intValue27; intValue28++) {
         class_1735 slot6 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue28);
         if (!slot6.method_7681()) {
            intValue26++;
         }
      }

      return intValue26;
   }

   private int compute5() {
      return this.intValue8 > 0 ? this.intValue8 : this.intValue3;
   }

   private int compute6(class_1703 screenHandler, int i) {
      for (int intValue29 = i; intValue29 < screenHandler.field_7761.size(); intValue29++) {
         class_1735 slot7 = screenHandler.method_7611(intValue29);
         if (slot7.method_7681() && this.check8(slot7.method_7677(), true)) {
            return intValue29;
         }
      }

      return -1;
   }

   private int compute7(class_476 genericContainerScreen) {
      int intValue30 = this.compute19(genericContainerScreen);
      int intValue31 = -1;

      for (int intValue32 = intValue30 - 1; intValue32 >= 0; intValue32--) {
         class_1735 slot8 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue32);
         if (slot8.method_7681()) {
            class_1799 itemStack2 = slot8.method_7677();
            String text5 = this.resolve6(itemStack2.method_7964().getString());
            if (itemStack2.method_31574(class_1802.field_8131)
               || itemStack2.method_31574(class_1802.field_8408)
               || itemStack2.method_31574(class_1802.field_8581)
               || itemStack2.method_31574(class_1802.field_8656)) {
               return intValue32;
            }

            if (text5.contains("выстав") || text5.contains("продать") || text5.contains("подтверд")) {
               intValue31 = intValue32;
            }
         }
      }

      return intValue31;
   }

   private boolean check() {
      return !this.check2() && !this.check3();
   }

   private boolean check2() {
      return this.compute11(class_1802.field_8687) < this.compute9();
   }

   private boolean check3() {
      return this.compute11(class_1802.field_8600) < this.compute10();
   }

   private boolean check4() {
      return this.compute8(this::check13) > 0;
   }

   private boolean check5() {
      return this.compute8(this::check14) > 0;
   }

   private int compute8(Predicate<class_1799> predicate) {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int intValue33 = 0;

         for (int intValue34 = 0; intValue34 < 36; intValue34++) {
            class_1799 itemStack3 = mc.field_1724.method_31548().method_5438(intValue34);
            if (!itemStack3.method_7960() && predicate.test(itemStack3)) {
               intValue33 += itemStack3.method_7947();
            }
         }

         return intValue33;
      }
   }

   private int compute9() {
      return this.check22() ? 2 : 3;
   }

   private int compute10() {
      return this.check22() ? 1 : 2;
   }

   private int compute11(class_1792 item) {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int intValue35 = 0;

         for (int intValue36 = 0; intValue36 < 36; intValue36++) {
            class_1799 itemStack4 = mc.field_1724.method_31548().method_5438(intValue36);
            if (!itemStack4.method_7960() && itemStack4.method_31574(item)) {
               intValue35 += itemStack4.method_7947();
            }
         }

         return intValue35;
      }
   }

   private boolean check6() {
      return this.compute12() > 0;
   }

   private int compute12() {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int intValue37 = 0;

         for (int intValue38 = 0; intValue38 < 36; intValue38++) {
            class_1799 itemStack5 = mc.field_1724.method_31548().method_5438(intValue38);
            if (this.check8(itemStack5, true)) {
               intValue37 += Math.max(1, itemStack5.method_7947());
            }
         }

         return intValue37;
      }
   }

   private boolean check7() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1724.field_7498 != null) {
         if (this.check8(mc.field_1724.method_6047(), true)) {
            return true;
         } else {
            for (int intValue39 = 0; intValue39 < 9; intValue39++) {
               if (this.check8(mc.field_1724.method_31548().method_5438(intValue39), true)) {
                  mc.field_1724.method_31548().method_61496(intValue39);
                  return true;
               }
            }

            int intValue40 = mc.field_1724.method_31548().method_67532();

            for (int intValue41 = 9; intValue41 < 36; intValue41++) {
               if (this.check8(mc.field_1724.method_31548().method_5438(intValue41), true)) {
                  mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, intValue41, intValue40, class_1713.field_7791, mc.field_1724);
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean check8(class_1799 itemStack, boolean bl) {
      if (itemStack != null && !itemStack.method_7960()) {
         String text6 = this.resolve6(itemStack.method_7964().getString());
         if (text6.contains(this.resolve6(this.resolve4()))) {
            return true;
         } else if (!bl) {
            return false;
         } else {
            return this.check22() ? itemStack.method_31574(class_1802.field_8802) : itemStack.method_31574(class_1802.field_8377);
         }
      } else {
         return false;
      }
   }

   private int compute13(class_476 genericContainerScreen) {
      int intValue42 = this.compute19(genericContainerScreen);

      for (int intValue43 = 0; intValue43 < intValue42; intValue43++) {
         class_1735 slot9 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue43);
         if (this.check9(slot9) && this.check8(slot9.method_7677(), true)) {
            return intValue43;
         }
      }

      return -1;
   }

   private boolean check9(class_1735 slot) {
      return slot != null && slot.method_7681() ? !this.check15(slot.method_7677()) : false;
   }

   private int compute14(class_1735 slot) {
      return slot != null && slot.method_7681() ? Math.max(1, slot.method_7677().method_7947()) : 1;
   }

   private boolean check10(class_1735 slot) {
      if (slot != null && slot.method_7681()) {
         class_1799 itemStack6 = slot.method_7677();
         return this.check15(itemStack6)
            ? false
            : this.check12(itemStack6) || this.resolve6(itemStack6.method_7964().getString()).contains(this.resolve6("Дерево"));
      } else {
         return false;
      }
   }

   private boolean check11(class_476 genericContainerScreen) {
      if (genericContainerScreen == null) {
         return false;
      } else {
         String text7 = this.resolve6(genericContainerScreen.method_25440().getString());
         return text7.contains(this.resolve6("Дерево"));
      }
   }

   private boolean check12(class_1799 itemStack) {
      if (itemStack == null || itemStack.method_7960()) {
         return false;
      } else if (itemStack.method_31574(class_1802.field_8600)) {
         return true;
      } else {
         String text8 = class_7923.field_41178.method_10221(itemStack.method_7909()).method_12832();
         String text9 = this.resolve6(itemStack.method_7964().getString());
         return text8.contains("log")
            || text8.contains("wood")
            || text8.contains("planks")
            || text9.contains("дерев")
            || text9.contains("древес")
            || text9.contains("палк");
      }
   }

   private boolean check13(class_1799 itemStack) {
      if (itemStack != null && !itemStack.method_7960()) {
         String text10 = class_7923.field_41178.method_10221(itemStack.method_7909()).method_12832();
         return text10.endsWith("_planks");
      } else {
         return false;
      }
   }

   private boolean check14(class_1799 itemStack) {
      if (itemStack != null && !itemStack.method_7960()) {
         String text11 = class_7923.field_41178.method_10221(itemStack.method_7909()).method_12832();
         return text11.endsWith("_log") || text11.endsWith("_wood") || text11.endsWith("_stem") || text11.endsWith("_hyphae");
      } else {
         return false;
      }
   }

   private boolean check15(class_1799 itemStack) {
      return itemStack.method_31574(class_1802.field_8656)
         || itemStack.method_31574(class_1802.field_8157)
         || itemStack.method_31574(class_1802.field_8581)
         || itemStack.method_31574(class_1802.field_8879)
         || itemStack.method_31574(class_1802.field_8162);
   }

   private int compute15(class_476 genericContainerScreen, class_1792 item, String string) {
      String text12 = this.resolve6(string);

      for (int intValue44 = 0; intValue44 < ((class_1707)genericContainerScreen.method_17577()).field_7761.size(); intValue44++) {
         class_1735 slot10 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue44);
         if (slot10.method_7681()) {
            class_1799 itemStack7 = slot10.method_7677();
            if (itemStack7.method_31574(item) && this.resolve6(itemStack7.method_7964().getString()).contains(text12)) {
               return intValue44;
            }
         }
      }

      return -1;
   }

   private int compute16(class_476 genericContainerScreen) {
      int intValue45 = this.compute19(genericContainerScreen);

      for (int intValue46 = 0; intValue46 < intValue45; intValue46++) {
         class_1735 slot11 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue46);
         if (slot11.method_7681()) {
            class_1799 itemStack8 = slot11.method_7677();
            String text13 = this.resolve6(itemStack8.method_7964().getString());
            if (itemStack8.method_31574(class_1802.field_8687)) {
               return intValue46;
            }

            if (itemStack8.method_31574(class_1802.field_8407) && (text13.contains("изумруд") || text13.contains("emerald"))) {
               return intValue46;
            }
         }
      }

      return -1;
   }

   private int compute17(class_476 genericContainerScreen, class_1792 item) {
      int intValue47 = this.compute19(genericContainerScreen);

      for (int intValue48 = 0; intValue48 < intValue47; intValue48++) {
         class_1735 slot12 = ((class_1707)genericContainerScreen.method_17577()).method_7611(intValue48);
         if (slot12.method_7681() && slot12.method_7677().method_31574(item)) {
            return intValue48;
         }
      }

      return -1;
   }

   private boolean check16(class_476 genericContainerScreen) {
      if (genericContainerScreen == null) {
         return false;
      } else {
         String text14 = this.resolve6(genericContainerScreen.method_25440().getString());
         if (text14.contains("подтверждение покупки")) {
            return this.compute18(genericContainerScreen.method_17577()) != -1;
         } else {
            class_1703 screenHandler4 = genericContainerScreen.method_17577();
            return this.compute18(screenHandler4) != -1 && this.check18(screenHandler4);
         }
      }
   }

   private boolean check17(class_476 genericContainerScreen) {
      if (genericContainerScreen == null) {
         return false;
      } else {
         String text15 = this.resolve6(genericContainerScreen.method_25440().getString());
         return text15.contains("продажа") || text15.contains("sellgui") || text15.contains("sell gui");
      }
   }

   private int compute18(class_1703 screenHandler) {
      int intValue49 = Math.min(screenHandler.field_7761.size(), Math.max(0, screenHandler.field_7761.size() - 36));

      for (int intValue50 = intValue49 - 1; intValue50 >= 0; intValue50--) {
         class_1799 itemStack9 = screenHandler.method_7611(intValue50).method_7677();
         String text16 = this.resolve6(itemStack9.method_7964().getString());
         if (text16.contains("купить")
            || itemStack9.method_31574(class_1802.field_8581)
            || itemStack9.method_31574(class_1802.field_8656)
            || itemStack9.method_31574(class_1802.field_8120)
            || itemStack9.method_31574(class_1802.field_8839)) {
            return intValue50;
         }
      }

      return -1;
   }

   private boolean check18(class_1703 screenHandler) {
      int intValue51 = Math.min(screenHandler.field_7761.size(), Math.max(0, screenHandler.field_7761.size() - 36));

      for (int intValue52 = 0; intValue52 < intValue51; intValue52++) {
         class_1799 itemStack10 = screenHandler.method_7611(intValue52).method_7677();
         if (itemStack10.method_31574(class_1802.field_8879) || itemStack10.method_31574(class_1802.field_8197)) {
            return true;
         }
      }

      return false;
   }

   private void invoke37(class_476 genericContainerScreen, int i, int j, class_1713 slotActionType) {
      mc.field_1761.method_2906(((class_1707)genericContainerScreen.method_17577()).field_7763, i, j, slotActionType, mc.field_1724);
   }

   private int compute19(class_476 genericContainerScreen) {
      int intValue53 = ((class_1707)genericContainerScreen.method_17577()).method_17388();
      int intValue54 = ((class_1707)genericContainerScreen.method_17577()).field_7761.size();
      return Math.max(0, Math.min(intValue53 * 9, intValue54));
   }

   private class_2338 resolve() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_2338 blockPos2 = mc.field_1724.method_24515();
         class_2338 blockPos3 = null;
         double doubleValue = Double.MAX_VALUE;

         for (class_2338 blockPos4 : class_2338.method_10097(blockPos2.method_10069(-5, -5, -5), blockPos2.method_10069(5, 5, 5))) {
            class_2338 blockPos5 = blockPos4.method_10062();
            if (this.check19(blockPos5)) {
               double doubleValue2 = mc.field_1724.method_5707(class_243.method_24953(blockPos5));
               if (doubleValue2 < doubleValue) {
                  doubleValue = doubleValue2;
                  blockPos3 = blockPos5;
               }
            }
         }

         return blockPos3;
      } else {
         return null;
      }
   }

   private boolean check19(class_2338 blockPos) {
      return mc.field_1687 != null && mc.field_1687.method_8320(blockPos).method_26204() == class_2246.field_9980;
   }

   private MoneyFarm.Look resolve2(class_243 vec3d) {
      if (mc.field_1724 == null) {
         return new MoneyFarm.Look(0.0F, 0.0F);
      } else {
         class_243 vec3d2 = mc.field_1724.method_33571();
         double doubleValue3 = vec3d.field_1352 - vec3d2.field_1352;
         double doubleValue4 = vec3d.field_1351 - vec3d2.field_1351;
         double doubleValue5 = vec3d.field_1350 - vec3d2.field_1350;
         float floatValue = (float)Math.toDegrees(Math.atan2(doubleValue5, doubleValue3)) - 90.0F;
         float floatValue2 = (float)(-Math.toDegrees(Math.atan2(doubleValue4, Math.sqrt(doubleValue3 * doubleValue3 + doubleValue5 * doubleValue5))));
         return new MoneyFarm.Look(floatValue, floatValue2);
      }
   }

   private class_2350 resolve3(class_2338 blockPos) {
      class_243 vec3d3 = mc.field_1724.method_18798();
      class_243 vec3d4 = class_243.method_24953(blockPos);
      double doubleValue6 = vec3d3.field_1352 - vec3d4.field_1352;
      double doubleValue7 = vec3d3.field_1350 - vec3d4.field_1350;
      if (Math.abs(doubleValue6) > Math.abs(doubleValue7)) {
         return doubleValue6 > 0.0 ? class_2350.field_11034 : class_2350.field_11039;
      } else {
         return doubleValue7 > 0.0 ? class_2350.field_11035 : class_2350.field_11043;
      }
   }

   private void invoke38(String string) {
      if (mc.field_1724 != null && string != null && !string.isBlank()) {
         mc.field_1724.field_3944.method_45730("ah search " + string.trim());
      }
   }

   private void invoke39(String string) {
      if (mc.field_1724 != null && string != null && !string.isBlank()) {
         mc.field_1724.field_3944.method_45730("ah " + string.trim());
      }
   }

   private boolean check20(class_476 genericContainerScreen) {
      if (genericContainerScreen != null && mc.field_1724 != null) {
         String text17 = this.resolve6(genericContainerScreen.method_25440().getString());
         String text18 = this.resolve6(mc.field_1724.method_5477().getString());
         return isAuctionScreen(genericContainerScreen)
            || text17.contains(text18)
            || text17.contains("мои товары")
            || text17.contains("мои предметы")
            || text17.contains("поиск:");
      } else {
         return false;
      }
   }

   private boolean check21() {
      return System.currentTimeMillis() - this.timestamp >= 1200L;
   }

   private void invoke40(long l) {
      mc.field_1724.field_3944.method_45730("ah sellgui " + l);
      this.timestamp = System.currentTimeMillis();
   }

   private void invoke41() {
      this.flag6 = false;
      this.intValue6 = 0;
      this.intValue7 = 0;
      if (this.intValue8 < 0) {
         this.intValue8 = 0;
      }
   }

   private String resolve4() {
      return this.check22() ? "Изумрудный меч" : "Изумрудная кирка";
   }

   private boolean check22() {
      return this.predmet.is("Изумрудный меч");
   }

   private long compute20() {
      return this.compute22(this.tsenaProdazhi.getValue());
   }

   private long compute21() {
      return this.compute22(this.maksTsenaDereva.getValue());
   }

   private long compute22(String string) {
      if (string == null) {
         return 0L;
      } else {
         String text19 = string.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace(",", "").replace(".", "");
         long longValue7 = 1L;
         if (text19.endsWith("тысяч")) {
            longValue7 = 1000L;
            text19 = text19.substring(0, text19.length() - 5);
         } else if (text19.endsWith("тысячи")) {
            longValue7 = 1000L;
            text19 = text19.substring(0, text19.length() - 6);
         } else if (text19.endsWith("тысяча")) {
            longValue7 = 1000L;
            text19 = text19.substring(0, text19.length() - 6);
         } else if (text19.endsWith("тыс")) {
            longValue7 = 1000L;
            text19 = text19.substring(0, text19.length() - 3);
         } else if (text19.endsWith("k") || text19.endsWith("к")) {
            longValue7 = 1000L;
            text19 = text19.substring(0, text19.length() - 1);
         } else if (text19.endsWith("m") || text19.endsWith("м")) {
            longValue7 = 1000000L;
            text19 = text19.substring(0, text19.length() - 1);
         }

         String text20 = text19.replaceAll("[^0-9]", "");
         if (text20.isEmpty()) {
            return 0L;
         } else {
            try {
               return Math.multiplyExact(Long.parseLong(text20), longValue7);
            } catch (ArithmeticException | NumberFormatException var7) {
               return 0L;
            }
         }
      }
   }

   private String resolve5(long l) {
      return l <= 0L ? "0" : Long.toString(l);
   }

   private long compute23() {
      return Math.max(50L, (long)Math.round(this.zaderzhkaMs.getValue()));
   }

   private void invoke42() {
      if (this.dualTimer2.isReached(10000L)) {
         ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
         float floatValue3 = threadLocalRandom.nextFloat() * 10.0F - 5.0F;
         float floatValue4 = threadLocalRandom.nextFloat() * 6.0F - 3.0F;
         mc.field_1724.method_36456(mc.field_1724.method_36454() + floatValue3);
         mc.field_1724.method_36457(Math.max(-90.0F, Math.min(90.0F, mc.field_1724.method_36455() + floatValue4)));
         this.dualTimer2.reset();
      }
   }

   private void invoke43() {
      this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
      this.blockPos = null;
      this.moneyFarmState = MoneyFarm.MoneyFarmState.ITEM;
      this.intValue = 0;
      this.intValue2 = 0;
      this.intValue3 = 0;
      this.intValue4 = 0;
      this.intValue5 = 0;
      this.flag = false;
      this.flag2 = false;
      this.flag3 = false;
      this.flag4 = false;
      this.flag5 = false;
      this.flag6 = false;
      this.intValue6 = 0;
      this.intValue7 = 0;
      this.intValue8 = 0;
      this.intValue9 = 0;
      this.flag7 = false;
      this.intValue10 = 0;
      this.timestamp = 0L;
      this.timestamp2 = 0L;
      this.dualTimer.reset();
      this.dualTimer2.reset();
      this.dualTimer3.reset();
      this.dualTimer4.reset();
      this.dualTimer5.reset();
   }

   private void invoke44(String string) {
      this.invoke47(string);
      this.invoke45();
   }

   private void invoke45() {
      this.moneyFarmState2 = MoneyFarm.MoneyFarmState2.IDLE;
      this.intValue10 = 0;
      this.dualTimer.reset();
   }

   private void invoke46() {
      if (mc.field_1724 != null) {
         mc.field_1724.method_7346();
      }
   }

   private boolean check23(long l) {
      if (mc.field_1755 == null) {
         this.intValue10 = 0;
         return true;
      } else if (!this.dualTimer.isReached(l)) {
         return false;
      } else {
         this.invoke46();
         this.intValue10++;
         if (this.intValue10 >= 20) {
            mc.method_1507(null);
            this.intValue10 = 0;
            return true;
         } else {
            this.dualTimer.reset();
            return false;
         }
      }
   }

   private String resolve6(String string) {
      return string == null ? "" : string.replaceAll("(?i)§.", "").replaceAll("(?i)&.", "").toLowerCase(Locale.ROOT).trim();
   }

   private void invoke47(String string) {
      if (this.uvedomleniya.getValue() && mc.field_1724 != null) {
         ChatUtils.addChatMessage("§8[§aMoneyFarm§8] §f" + string);
      }
   }

   private void face(MoneyFarm.Look look) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_36456(look.yaw);
         mc.field_1724.method_36457(look.pitch);
      }
   }

   private float angleTo(MoneyFarm.Look look) {
      if (mc.field_1724 == null) {
         return 0.0F;
      } else {
         float dyaw = wrapDegrees(mc.field_1724.method_36454() - look.yaw);
         float dpitch = look.pitch - mc.field_1724.method_36455();
         return (float)Math.sqrt((double)(dyaw * dyaw + dpitch * dpitch));
      }
   }

   private static float wrapDegrees(float value) {
      float wrapped = value % 360.0F;
      if (wrapped >= 180.0F) {
         wrapped -= 360.0F;
      }

      if (wrapped < -180.0F) {
         wrapped += 360.0F;
      }

      return wrapped;
   }

   private static boolean isAuctionScreen(class_476 screen) {
      return ((class_1707)screen.method_17577()).method_17388() == 6;
   }

   private static long priceOf(class_1735 slot) {
      String name = slot.method_7677().method_65130().getString();
      if (name == null) {
         return 0L;
      } else {
         int at = name.toLowerCase(Locale.ROOT).indexOf("цена");
         if (at < 0) {
            return 0L;
         } else {
            StringBuilder digits = new StringBuilder();

            for (int i = at; i < name.length(); i++) {
               char c = name.charAt(i);
               if (c >= '0' && c <= '9') {
                  digits.append(c);
               } else if (digits.length() > 0) {
                  break;
               }
            }

            try {
               return digits.length() == 0 ? 0L : Long.parseLong(digits.toString());
            } catch (NumberFormatException var6) {
               return 0L;
            }
         }
      }
   }

   private static String sellerOf(class_1735 slot) {
      String name = slot.method_7677().method_65130().getString();
      if (name == null) {
         return null;
      } else {
         int at = name.toLowerCase(Locale.ROOT).indexOf("продавец");
         if (at < 0) {
            return null;
         } else {
            int end = name.indexOf(10, at);
            String line = end < 0 ? name.substring(at) : name.substring(at, end);
            return line.substring(line.indexOf(58) + 1).replace('§', ' ').trim();
         }
      }
   }

   private static final class Look {
      final float yaw;
      final float pitch;

      Look(float yaw, float pitch) {
         this.yaw = yaw;
         this.pitch = pitch;
      }
   }

   static enum MoneyFarmState {
      ITEM,
      PLANKS,
      STICKS;
   }

   static enum MoneyFarmState2 {
      IDLE,
      BUY_WOOD_SEARCHING,
      BUY_WOOD_WAITING_AUCTION,
      BUY_WOOD_READING_AUCTION,
      BUY_WOOD_WAITING_CONFIRM,
      BUY_WOOD_CONFIRMING,
      BUY_WOOD_CLOSING,
      BUY_OPENING_SHOP,
      BUY_WAITING_SHOP,
      BUY_FIND_GOLD_INGOT,
      BUY_WAITING_EMERALD_MENU,
      BUY_FIND_EMERALD,
      BUY_WAITING_CONFIRM,
      BUY_CLICK_LIME_PANE,
      BUY_CLOSING_SHOP,
      CHECK_SELL_GUI_OPENING,
      CHECK_SELL_GUI_WAITING,
      CHECK_SELL_GUI_READING,
      FINDING_CRAFTING_TABLE,
      AIMING_CRAFTING_TABLE,
      OPENING_CRAFTING_TABLE,
      PLACING_ITEMS,
      TAKING_RESULT,
      CLOSING_CRAFTING,
      SELLING,
      WAITING_SELL_RESULT,
      RESALE_SEARCH_OWN_AH,
      RESALE_WAITING_OWN_AH,
      RESALE_TAKE_ITEM,
      RESALE_CLOSING,
      RESALE_SELLING,
      RESALE_WAIT_SELL_RESULT;
   }
}
