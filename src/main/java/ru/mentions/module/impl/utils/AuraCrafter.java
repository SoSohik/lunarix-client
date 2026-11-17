package ru.mentions.module.impl.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10730;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1714;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1842;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_476;
import net.minecraft.class_479;
import net.minecraft.class_6880;
import net.minecraft.class_7439;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import org.joml.Vector2f;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.manager.rotation.RotationProcess;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.module.impl.display.Hud;
import ru.mentions.module.impl.render.EntityEsp;
import ru.mentions.utils.aura.RayTraceUtil;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Aura Crafter",
   category = Category.OTHER,
   desc = "Крафт Божьей ауры, простая закупка и простая продажа по биндам."
)
@Environment(EnvType.CLIENT)
public class AuraCrafter extends Module {
   private static final String AURA_NAME = "Божья аура";
   private static final int TICK_DELAY = 6;
   private static final int JOIN_AH_DELAY = 200;
   private static final int NETHERITE_SWITCH_TICKS = 1800;
   private static final int AH_SEARCH_RETRY_TICKS = 100;
   private static final int AUTO_INVIS_CHECK_TICKS = 80;
   private static final int INVIS_DRINK_TIMEOUT_TICKS = 60;
   private static final int BALANCE_REFRESH_CALLS = 55;
   private static final int MAX_MILK_BUCKET_USES = 8;
   private static final int STORAGE_REOPEN_TIMEOUT_TICKS = 180;
   private static final int STORAGE_EMPTY_SETTLE_TICKS = 60;
   private static final int PLAYER_PAUSE_RETURN_TICKS = 6000;
   private static final String INVIS_SEARCH_QUERY = "инвиз";
   private static final double COW_INTERACT_RANGE = 4.5;
   public final BindSetting craftBind = new BindSetting(this, "Бинд крафта");
   public final BindSetting sellBind = new BindSetting(this, "Бинд продажи");
   public final BindSetting fullAutoBind = new BindSetting(this, "Бинд фулл авто");
   public final BindSetting buyNetheriteBind = new BindSetting(this, "Бинд незера");
   public final BindSetting buyDiamondsBind = new BindSetting(this, "Бинд алмазов");
   public final StringSetting buyItem = new StringSetting(this, "Что покупать", "Незеритовый слиток");
   public final StringSetting maxBuyPrice = new StringSetting(this, "Макс цена 1шт", "325000", true);
   public final StringSetting diamondStackPrice = new StringSetting(this, "Алмазы 64шт", "250000", true);
   public final StringSetting reserveMoney = new StringSetting(this, "Резерв денег", "3000000", true);
   public final StringSetting anarchyList = new StringSetting(this, "Анки закупа", "320,322,323");
   public final BooleanSetting clanInvest = new BooleanSetting(this, "Clan invest", false);
   public final StringSetting clanInvestAt = new StringSetting(this, "Invest порог", "10000000", true).setVisible(() -> this.clanInvest.getValue());
   public final StringSetting clanInvestAmount = new StringSetting(this, "Invest сумма", "1000000", true).setVisible(() -> this.clanInvest.getValue());
   public final BooleanSetting autoInvis = new BooleanSetting(this, "Авто инвиз", false);
   public final ModeSetting autoInvisType = new ModeSetting(this, "Тип инвиза", "3m", "8m", "8+").setVisible(() -> this.autoInvis.getValue());
   public final StringSetting sellItem = new StringSetting(this, "Что продавать", "Божья аура");
   public final StringSetting sellPrice = new StringSetting(this, "Цена продажи", "420000", true);
   public final BooleanSetting randomSellPrice = new BooleanSetting(this, "Рандом цены", false);
   public final StringSetting randomSellPriceRange = new StringSetting(this, "Разброс цены", "10000", true).setVisible(() -> this.randomSellPrice.getValue());
   public final BooleanSetting autoPausePlayers = new BooleanSetting(this, "Авто пауза игроки", true);
   public final SliderSetting autoPauseRange = new SliderSetting(this, "Радиус игроков", 64.0F, 8.0F, 128.0F, 1.0F)
      .setVisible(() -> this.autoPausePlayers.getValue());
   public final SliderSetting sellSlots = new SliderSetting(this, "Слотов AH", 5.0F, 1.0F, 50.0F, 1.0F);
   public final DragSetting infoDrag = new DragSetting(this, "Инфо панель", new Vector2f(8.0F, 145.0F));
   private AuraCrafter.Mode mode = AuraCrafter.Mode.IDLE;
   private int waitTicks;
   private int listed;
   private int sellAttemptsLeft;
   private int lastScreenSyncId = -1;
   private int noScreenTicks;
   private int fullSellTicks;
   private boolean fullAuto;
   private String activeBuyName;
   private long activeMaxUnitPrice = -1L;
   private class_1792 activeBuyItem;
   private int activeBuyTarget;
   private int buySearchTicks;
   private int buyOpenTicks;
   private int ignoredSearchScreenSyncId = -1;
   private int balanceRefreshTicks;
   private long balance = -1L;
   private int homeAnarchy = -1;
   private int targetAnarchy = -1;
   private int autoPauseAnarchy = -1;
   private int playerPauseTicks;
   private int playerPauseReturnTicks;
   private boolean playerPauseReturning;
   private AuraCrafter.Mode resumeModeAfterPlayerPause = AuraCrafter.Mode.IDLE;
   private String playerPauseName;
   private String lastPlayerPauseName;
   private int anarchyIndex;
   private AuraCrafter.JoinAction joinAction = AuraCrafter.JoinAction.NONE;
   private boolean allowAwayAnarchy;
   private int antiAfkTicks;
   private String pendingCommand;
   private String lastCommand;
   private int antiAfkStage;
   private boolean buyConfirmClicked;
   private int buyConfirmHoldTicks;
   private long selectedBuyPrice;
   private boolean balanceReservedForConfirm;
   private boolean afkBlocked;
   private String lastProgressSignature = "";
   private int stuckTicks;
   private AuraCrafter.Mode timedMode = AuraCrafter.Mode.IDLE;
   private int stateTicks;
   private String buyRejectReason = "";
   private boolean resumeCraftAfterDiamondBuy;
   private long sessionStartMillis;
   private long sessionStartBalance = -1L;
   private long sessionSellEarned;
   private long sessionBuySpent;
   private long sessionClanInvested;
   private int investCooldownTicks;
   private boolean storageTookSellLot;
   private boolean checkedStorageForSell;
   private int emptyStorageChecks;
   private int storageTakeClicks;
   private boolean autoInvisWasEnabled;
   private int autoInvisAnarchy = -1;
   private int autoInvisCheckTicks;
   private AuraCrafter.Mode resumeModeAfterInvis = AuraCrafter.Mode.IDLE;
   private int resumeWaitAfterInvis;
   private int invisOpenTicks;
   private boolean invisConfirmClicked;
   private int invisConfirmHoldTicks;
   private boolean invisUseStarted;
   private int invisDrinkTicks;
   private int invisPreviousSlot = -1;
   private int humanMoveTicks;
   private int nextHumanMoveTicks;
   private int humanLookTicks;
   private int humanSneakTicks;
   private String delayedAhCommand;
   private int delayedAhTicks;
   private int inventoryActionTicks;
   private int tableAimTicks;
   private int cowAimTicks;

   @EventHandler
   public void onKey(EventKey event) {
      if (mc.field_1724 != null && event.getKey() != -1) {
         if (event.getKey() == this.craftBind.get()) {
            this.startCraft();
         } else if (event.getKey() == this.sellBind.get()) {
            this.startSell();
         } else if (event.getKey() == this.buyNetheriteBind.get()) {
            this.toggleNetheriteBuy();
         } else if (event.getKey() == this.buyDiamondsBind.get()) {
            this.toggleDiamondBuy();
         } else if (event.getKey() == this.fullAutoBind.get()) {
            if (this.fullAuto) {
               this.stopFullAuto();
            } else {
               this.startFullAuto();
            }
         }
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         this.tickAntiAfkCommand();
         this.tickClanInvest();
         this.tickHumanMovement();
         this.tickHumanLook();
         if (!this.tickPlayerAutoPause()) {
            if (!this.tickDelayedAhCommand()) {
               if (!this.tickAutoInvis()) {
                  if (this.mode != AuraCrafter.Mode.IDLE) {
                     if (!this.tickAhSearchOpenWatchdog()) {
                        this.tickWatchdog();
                        if (this.fullAuto && this.homeAnarchy <= 0) {
                           this.homeAnarchy = this.readCurrentAnarchy();
                        }

                        if (!this.fullAuto || !this.guardAnarchy()) {
                           if (this.waitTicks > 0) {
                              this.waitTicks--;
                           } else {
                              switch (this.mode) {
                                 case PLAYER_PAUSE:
                                    this.tickPlayerPause();
                                    break;
                                 case AUTO_WAIT_JOIN:
                                    this.tickAutoWaitJoin();
                                    break;
                                 case AUTO_WAIT_BUDGET:
                                    this.tickAutoWaitBudget();
                                    break;
                                 case BUY_OPEN:
                                    this.tickBuyOpen();
                                    break;
                                 case BUY_READ:
                                    this.tickBuyRead();
                                    break;
                                 case BUY_CONFIRM:
                                    this.tickBuyConfirm();
                                    break;
                                 case CRAFT:
                                    this.tickCraft();
                                    break;
                                 case CRAFT_MILK:
                                    this.tickMilk();
                                    break;
                                 case CRAFT_OPEN_TABLE:
                                    this.tickOpenCraftingTable();
                                    break;
                                 case CRAFT_FILL:
                                    this.tickCraftFill();
                                    break;
                                 case INVIS_OPEN:
                                    this.tickInvisOpen();
                                    break;
                                 case INVIS_READ:
                                    this.tickInvisRead();
                                    break;
                                 case INVIS_CONFIRM:
                                    this.tickInvisConfirm();
                                    break;
                                 case INVIS_DRINK:
                                    this.tickInvisDrink();
                                    break;
                                 case SELL_PREPARE:
                                    this.tickSellPrepare();
                                    break;
                                 case SELL_SEND:
                                    this.tickSellSend();
                                    break;
                                 case STORAGE_OPEN:
                                    this.tickStorageOpen();
                                    break;
                                 case STORAGE_CLICK:
                                    this.tickStorageClick();
                                    break;
                                 case STORAGE_TAKE:
                                    this.tickStorageTake();
                                    break;
                                 case SELL:
                                    this.tickSell();
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

   @EventHandler
   public void onDisplay(EventDisplay event) {
      if (mc.field_1724 != null) {
         this.renderInfoPanel(event);
      }
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && !event.isSend()) {
         if (event.getPacket() instanceof class_7439 packet) {
            String message = packet.comp_763().getString();
            String lower = this.clean(message);
            String target = this.clean(this.sellItem.getValue());
            long money = this.extractDollarPrice(message);
            if ((lower.contains("ваш баланс") || lower.contains("баланс")) && money > 0L) {
               this.balance = money;
               this.balanceRefreshTicks = 0;
            }

            if ((lower.contains("успешно купили") || lower.contains("вы успешно купили")) && money > 0L && this.balance > 0L) {
               this.sessionBuySpent += money;
               if (!this.balanceReservedForConfirm) {
                  this.balance = Math.max(0L, this.balance - money);
               }

               this.balanceReservedForConfirm = false;
               this.balanceRefreshTicks = 0;
            }

            if (lower.contains("данная команда недоступна в режиме afk")) {
               this.afkBlocked = true;
               this.wakeForCommand(this.lastCommand);
            } else if ((lower.contains("хаб") || lower.contains("lobby")) && this.fullAuto) {
               this.rejoinHome();
            } else {
               if (this.balanceReservedForConfirm && this.isBuyFailedMessage(lower)) {
                  this.refundReservedBuyBalance();
                  this.sendCommand("money");
               }

               if ((lower.contains("не хватает") || lower.contains("не хвататет")) && lower.contains("монет") && this.fullAuto) {
                  this.refundReservedBuyBalance();
                  this.finishAutoBuy();
               } else {
                  if ((lower.contains("у вас купили") || lower.contains("купили")) && lower.contains(target)) {
                     if (money > 0L) {
                        this.sessionSellEarned += money;
                        this.balance = this.balance > 0L ? (this.balance += money) : money;
                     }

                     if (this.fullAuto) {
                        this.refreshBalanceNow(false);
                     }

                     this.listed = Math.max(0, this.listed - 1);
                     this.fullSellTicks = 0;
                     if (this.mode == AuraCrafter.Mode.SELL || this.mode == AuraCrafter.Mode.SELL_PREPARE || this.mode == AuraCrafter.Mode.SELL_SEND) {
                        this.sellAttemptsLeft++;
                     }
                  }

                  if ((lower.contains("выставлен") || lower.contains("выставили")) && lower.contains(target)) {
                     this.listed++;
                  }

                  if (lower.contains("не удалось выставить") && lower.contains(target) && !this.isAuctionBalanceSellBlocked(lower)) {
                     this.listed = this.sellSlots.getValue().intValue();
                     this.startStorageRelist();
                  }
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.reset();
      this.autoInvisWasEnabled = false;
      this.autoInvisAnarchy = -1;
      super.onDisable();
   }

   private void startCraft() {
      this.reset();
      this.startSessionStats();
      this.mode = AuraCrafter.Mode.CRAFT;
      this.msg("§e[AuraCrafter] Крафт запущен.");
   }

   private void toggleNetheriteBuy() {
      if (!this.fullAuto && this.isBuying(class_1802.field_22020)) {
         this.stopManualBuy();
      } else {
         this.reset();
         this.startSessionStats();
         this.beginBuy(this.buyItem.getValue().trim(), this.parsePrice(this.maxBuyPrice.getValue(), 325000L), class_1802.field_22020, 0);
         this.msg("§e[AuraCrafter] Закуп незера до $" + this.maxBuyPrice.getValue());
      }
   }

   private void toggleDiamondBuy() {
      if (!this.fullAuto && this.isBuying(class_1802.field_8477)) {
         this.stopManualBuy();
      } else {
         this.reset();
         this.startSessionStats();
         long diamondUnit = Math.max(1L, this.parsePrice(this.diamondStackPrice.getValue(), 250000L) / 64L);
         this.beginBuy("Алмаз", diamondUnit, class_1802.field_8477, 256);
         this.msg("§e[AuraCrafter] Закуп алмазов до $" + this.diamondStackPrice.getValue() + " за 64шт.");
      }
   }

   private boolean isBuying(class_1792 item) {
      return (this.mode == AuraCrafter.Mode.BUY_OPEN || this.mode == AuraCrafter.Mode.BUY_READ || this.mode == AuraCrafter.Mode.BUY_CONFIRM)
         && this.activeBuyItem == item;
   }

   private void stopManualBuy() {
      this.reset();
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      this.msg("§7[AuraCrafter] Закуп выключен.");
   }

   private void startSell() {
      if (!this.fullAuto
         && (
            this.mode == AuraCrafter.Mode.SELL
               || this.mode == AuraCrafter.Mode.SELL_PREPARE
               || this.mode == AuraCrafter.Mode.SELL_SEND
               || this.mode == AuraCrafter.Mode.STORAGE_OPEN
               || this.mode == AuraCrafter.Mode.STORAGE_CLICK
               || this.mode == AuraCrafter.Mode.STORAGE_TAKE
         )) {
         this.reset();
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }

         this.msg("§7[AuraCrafter] Продажа выключена.");
      } else {
         this.reset();
         this.startSessionStats();
         this.mode = AuraCrafter.Mode.SELL;
         this.sellAttemptsLeft = this.sellSlots.getValue().intValue();
         this.msg("§e[AuraCrafter] Продажа: " + this.sellItem.getValue() + " по $" + this.sellPrice.getValue() + this.sellPriceSuffix());
      }
   }

   private void startFullAuto() {
      this.reset();
      this.fullAuto = true;
      this.homeAnarchy = this.readCurrentAnarchy();
      this.balance = this.readScoreboardMoney();
      this.startSessionStats();
      this.sendCommand("money");
      this.msg("§e[AuraCrafter] Фулл авто: анка " + this.homeAnarchy + ", старт цикла.");
      this.beginAutoCycle();
   }

   private void stopFullAuto() {
      this.reset();
      this.msg("§7[AuraCrafter] Фулл авто выключен.");
   }

   private void beginAutoCycle() {
      if (this.fullAuto) {
         this.allowAwayAnarchy = false;
         this.resumeCraftAfterDiamondBuy = false;
         if (this.countInventory(class_1802.field_22020) > 0) {
            if (this.countInventory(class_1802.field_8477) < 4) {
               this.beginCraftDiamondBuy();
            } else {
               this.returnHomeForCraft();
            }
         } else if (this.countInventory(class_1802.field_8477) < 256) {
            long diamondUnit = Math.max(1L, this.parsePrice(this.diamondStackPrice.getValue(), 250000L) / 64L);
            this.beginBuy("Алмаз", diamondUnit, class_1802.field_8477, 256);
         } else {
            this.beginNetheriteBuy();
         }
      }
   }

   private void beginNetheriteBuy() {
      this.resumeCraftAfterDiamondBuy = false;
      this.allowAwayAnarchy = true;
      if (this.fullAuto && !this.canBuyNetheriteAboveReserve(0L)) {
         this.handleNoNetheriteBudget();
      } else {
         this.beginBuy(this.buyItem.getValue().trim(), this.parsePrice(this.maxBuyPrice.getValue(), 325000L), class_1802.field_22020, 0);
      }
   }

   private void beginCraftDiamondBuy() {
      this.resumeCraftAfterDiamondBuy = true;
      this.allowAwayAnarchy = false;
      long diamondUnit = Math.max(1L, this.parsePrice(this.diamondStackPrice.getValue(), 250000L) / 64L);
      this.beginBuy("Алмаз", diamondUnit, class_1802.field_8477, 256);
      this.msg("§e[AuraCrafter] Алмазы кончились, докупаю из резерва и вернусь к крафту.");
   }

   private boolean canBuyNetheriteAboveReserve(long nextPrice) {
      long currentBalance = this.knownBalance();
      if (currentBalance <= 0L) {
         return true;
      } else {
         long reserve = this.parsePrice(this.reserveMoney.getValue(), 3000000L);
         long needed = nextPrice > 0L ? nextPrice : Math.max(1L, this.parsePrice(this.maxBuyPrice.getValue(), 325000L));
         return currentBalance - needed >= reserve;
      }
   }

   private long knownBalance() {
      long currentBalance;
      if (this.balance <= 0L && (currentBalance = this.readScoreboardMoney()) > 0L) {
         this.balance = currentBalance;
         this.balanceRefreshTicks = 0;
      }

      return this.balance;
   }

   private void handleNoNetheriteBudget() {
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      this.refundReservedBuyBalance();
      this.activeBuyName = null;
      this.activeBuyItem = null;
      this.activeBuyTarget = 0;
      this.activeMaxUnitPrice = -1L;
      this.buySearchTicks = 0;
      this.buyOpenTicks = 0;
      this.allowAwayAnarchy = false;
      if (this.countSellTargetInventory() <= 0 && this.listed <= 0) {
         if (this.countInventory(class_1802.field_22020) > 0 && this.countInventory(class_1802.field_8477) >= 4) {
            this.returnHomeForCraft();
         } else {
            this.mode = AuraCrafter.Mode.AUTO_WAIT_BUDGET;
            this.waitTicks = 40;
         }
      } else {
         this.mode = AuraCrafter.Mode.SELL;
         this.waitTicks = 6;
      }
   }

   private void beginBuy(String name, long maxUnitPrice, class_1792 item, int targetCount) {
      this.activeBuyName = name;
      this.activeMaxUnitPrice = maxUnitPrice;
      this.activeBuyItem = item;
      this.activeBuyTarget = targetCount;
      this.normalizeActiveBuyState();
      this.buySearchTicks = 0;
      this.buyRejectReason = "";
      this.lastScreenSyncId = -1;
      this.noScreenTicks = 0;
      this.sendActiveBuySearch();
      this.mode = AuraCrafter.Mode.BUY_OPEN;
      this.waitTicks = 20;
   }

   private void sendActiveBuySearch() {
      this.normalizeActiveBuyState();
      String name = this.activeBuyName == null ? this.buyItem.getValue().trim() : this.activeBuyName;
      this.closeCurrentSearchScreen();
      this.lastScreenSyncId = -1;
      this.noScreenTicks = 0;
      this.buyOpenTicks = 0;
      this.sendAhCommand("ah search " + name);
   }

   private void normalizeActiveBuyState() {
      if (this.activeBuyName != null) {
         String name = this.clean(this.activeBuyName);
         boolean diamondSearch = name.contains("алмаз") || name.contains("diamond");
         if (diamondSearch) {
            this.activeBuyItem = class_1802.field_8477;
            if (this.activeBuyTarget <= 0) {
               this.activeBuyTarget = 256;
            }
         } else {
            if (this.activeBuyItem != class_1802.field_22020 || this.activeBuyTarget > 0) {
               this.activeBuyItem = class_1802.field_22020;
               this.activeBuyTarget = 0;
               this.activeMaxUnitPrice = this.parsePrice(this.maxBuyPrice.getValue(), 325000L);
               this.buyRejectReason = "";
            }
         }
      }
   }

   private void closeCurrentSearchScreen() {
      if (mc.field_1755 instanceof class_476 screen) {
         this.ignoredSearchScreenSyncId = ((class_1707)screen.method_17577()).field_7763;
         mc.field_1724.method_7346();
      } else {
         this.ignoredSearchScreenSyncId = -1;
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }
      }
   }

   private boolean isIgnoredSearchScreen() {
      if (this.ignoredSearchScreenSyncId == -1) {
         return false;
      } else {
         return !(mc.field_1755 instanceof class_476 screen) ? false : ((class_1707)screen.method_17577()).field_7763 == this.ignoredSearchScreenSyncId;
      }
   }

   private boolean tickAhSearchOpenWatchdog() {
      if (this.mode == AuraCrafter.Mode.INVIS_OPEN) {
         if (mc.field_1755 instanceof class_476) {
            if (this.isIgnoredSearchScreen()) {
               this.closeCurrentSearchScreen();
               this.waitTicks = 2;
               return true;
            } else {
               this.ignoredSearchScreenSyncId = -1;
               this.invisOpenTicks = 0;
               return false;
            }
         } else if (++this.invisOpenTicks < 100) {
            return false;
         } else {
            this.sendInvisSearch();
            this.waitTicks = 20;
            return true;
         }
      } else if (this.mode != AuraCrafter.Mode.BUY_OPEN) {
         return false;
      } else if (mc.field_1755 instanceof class_476) {
         if (this.isIgnoredSearchScreen()) {
            this.closeCurrentSearchScreen();
            this.waitTicks = 2;
            return true;
         } else {
            this.ignoredSearchScreenSyncId = -1;
            this.buyOpenTicks = 0;
            return false;
         }
      } else if (++this.buyOpenTicks < 100) {
         return false;
      } else if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && !this.canBuyNetheriteAboveReserve(0L)) {
         this.handleNoNetheriteBudget();
         return true;
      } else {
         this.sendActiveBuySearch();
         this.waitTicks = 20;
         return true;
      }
   }

   private void tickAutoWaitJoin() {
      int current = this.readCurrentAnarchy();
      if (this.targetAnarchy > 0 && current != this.targetAnarchy) {
         this.sendCommand("an" + this.targetAnarchy);
         this.waitTicks = 200;
      } else if (this.joinAction == AuraCrafter.JoinAction.SEARCH) {
         this.targetAnarchy = -1;
         this.sendActiveBuySearch();
         this.mode = AuraCrafter.Mode.BUY_OPEN;
         this.waitTicks = 20;
      } else if (this.joinAction == AuraCrafter.JoinAction.CRAFT) {
         this.targetAnarchy = -1;
         this.mode = AuraCrafter.Mode.CRAFT;
         this.waitTicks = 6;
      } else {
         this.mode = AuraCrafter.Mode.IDLE;
      }
   }

   private void tickAutoWaitBudget() {
      if (!this.fullAuto) {
         this.mode = AuraCrafter.Mode.IDLE;
         this.waitTicks = 0;
      } else {
         this.allowAwayAnarchy = false;
         this.refreshBalanceNow(false);
         if (this.countSellTargetInventory() > 0 || this.listed > 0) {
            this.mode = AuraCrafter.Mode.SELL;
            this.waitTicks = 6;
         } else if (this.countInventory(class_1802.field_22020) > 0) {
            if (this.countInventory(class_1802.field_8477) < 4) {
               this.beginCraftDiamondBuy();
            } else {
               this.returnHomeForCraft();
            }
         } else if (this.countInventory(class_1802.field_8477) < 256) {
            long diamondUnit = Math.max(1L, this.parsePrice(this.diamondStackPrice.getValue(), 250000L) / 64L);
            this.beginBuy("Алмаз", diamondUnit, class_1802.field_8477, 256);
         } else if (this.canBuyNetheriteAboveReserve(0L)) {
            this.beginNetheriteBuy();
         } else {
            this.waitTicks = 40;
         }
      }
   }

   private void tickBuyOpen() {
      if (mc.field_1755 instanceof class_476 screen) {
         if (this.isIgnoredSearchScreen()) {
            this.closeCurrentSearchScreen();
            this.waitTicks = 2;
         } else {
            this.ignoredSearchScreenSyncId = -1;
            this.lastScreenSyncId = ((class_1707)screen.method_17577()).field_7763;
            this.noScreenTicks = 0;
            this.mode = AuraCrafter.Mode.BUY_READ;
            this.waitTicks = 6;
         }
      } else {
         this.noScreenTicks++;
         if (this.lastScreenSyncId == -1 && this.noScreenTicks > 17) {
            this.sendActiveBuySearch();
            this.waitTicks = 20;
         } else if (this.lastScreenSyncId != -1 && this.noScreenTicks > 20) {
            if (this.fullAuto) {
               this.finishAutoBuy();
            } else {
               this.stop("§7[AuraCrafter] Закуп остановлен: AH закрыт.");
            }
         } else {
            this.waitTicks = 6;
         }
      }
   }

   private void tickBuyRead() {
      this.normalizeActiveBuyState();
      if (!(mc.field_1755 instanceof class_476 screen)) {
         if (this.fullAuto) {
            this.finishAutoBuy();
         } else {
            this.stop("§7[AuraCrafter] Закуп остановлен: AH закрыт.");
         }
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         if (this.lastScreenSyncId != -1 && handler.field_7763 != this.lastScreenSyncId) {
            this.lastScreenSyncId = handler.field_7763;
         }

         if (this.isBuyConfirmScreen(screen, handler)) {
            this.mode = AuraCrafter.Mode.BUY_CONFIRM;
            this.waitTicks = 6;
         } else if (this.activeBuyItem != null && this.activeBuyTarget > 0 && this.countInventory(this.activeBuyItem) >= this.activeBuyTarget) {
            this.finishAutoBuy();
         } else {
            if (this.fullAuto && this.activeBuyItem == class_1802.field_22020) {
               this.refreshBalanceNow(false);
               if (!this.canBuyNetheriteAboveReserve(0L)) {
                  if (this.countInventory(class_1802.field_22020) > 0) {
                     this.finishAutoBuy();
                  } else {
                     this.handleNoNetheriteBudget();
                  }

                  return;
               }
            }

            if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && this.balance <= 0L && this.balance <= 0L) {
               this.sendCommand("money");
               this.waitTicks = 20;
            } else {
               int slot = this.findBuySlot(handler);
               if (slot == -1) {
                  if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && "резерв".equals(this.buyRejectReason)) {
                     if (this.countInventory(class_1802.field_22020) > 0) {
                        this.finishAutoBuy();
                     } else {
                        this.handleNoNetheriteBudget();
                     }
                  } else if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && (this.buySearchTicks += 10) >= 1800) {
                     this.switchBuyAnarchy();
                  } else {
                     this.clickRefresh(handler);
                     this.waitTicks = 10;
                  }
               } else if (!this.fullAuto
                  || this.activeBuyItem != class_1802.field_22020
                  || this.balance <= 0L
                  || this.balance - this.selectedBuyPrice >= this.parsePrice(this.reserveMoney.getValue(), 3000000L)) {
                  this.clickSlot(handler.field_7763, slot, 0, class_1713.field_7794);
                  this.mode = AuraCrafter.Mode.BUY_CONFIRM;
                  this.waitTicks = 10;
               } else if (this.countInventory(class_1802.field_22020) > 0) {
                  this.finishAutoBuy();
               } else {
                  this.handleNoNetheriteBudget();
               }
            }
         }
      }
   }

   private void tickBuyConfirm() {
      if (!(mc.field_1755 instanceof class_476 screen)) {
         this.refundReservedBuyBalance();
         this.buyConfirmClicked = false;
         this.buyConfirmHoldTicks = 0;
         this.mode = AuraCrafter.Mode.BUY_OPEN;
         this.waitTicks = 6;
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         if (!this.isBuyConfirmScreen(screen, handler)) {
            this.refundReservedBuyBalance();
            this.buyConfirmClicked = false;
            this.buyConfirmHoldTicks = 0;
            this.mode = AuraCrafter.Mode.BUY_READ;
            this.waitTicks = 10;
         } else if (this.buyConfirmClicked) {
            if (++this.buyConfirmHoldTicks < 20) {
               this.waitTicks = 2;
            } else {
               this.refundReservedBuyBalance();
               this.buyConfirmClicked = false;
               this.buyConfirmHoldTicks = 0;
               this.mode = AuraCrafter.Mode.BUY_READ;
               this.waitTicks = 10;
            }
         } else {
            int confirm = this.findConfirmSlot(handler);
            if (confirm >= 0) {
               this.clickSlot(handler.field_7763, confirm, 0, class_1713.field_7790);
               if (this.balance > 0L && this.selectedBuyPrice > 0L) {
                  this.balance = Math.max(0L, this.balance - this.selectedBuyPrice);
                  this.balanceReservedForConfirm = true;
               }

               this.buyConfirmClicked = true;
               this.buyConfirmHoldTicks = 0;
               this.waitTicks = 10;
            } else {
               this.mode = AuraCrafter.Mode.BUY_CONFIRM;
               this.waitTicks = 6;
            }
         }
      }
   }

   private void tickCraft() {
      if (this.countInventory(class_1802.field_22020) <= 0) {
         if (this.fullAuto) {
            this.startAutoSellOrBuy();
         } else {
            this.stop("§a[AuraCrafter] Крафт закончен: незеритовые слитки закончились.");
         }
      } else if (this.countInventory(class_1802.field_8477) < 4) {
         if (this.fullAuto) {
            this.beginCraftDiamondBuy();
         } else {
            this.stop("§c[AuraCrafter] Нет алмазов для следующей Божки.");
         }
      } else if (this.countInventory(class_1802.field_8103) < this.milkBucketsTarget()) {
         if (this.countInventory(class_1802.field_8550) <= 0) {
            this.stop("§c[AuraCrafter] Нет молока и пустых ведер.");
         } else {
            this.mode = AuraCrafter.Mode.CRAFT_MILK;
            this.waitTicks = 6;
         }
      } else {
         this.mode = mc.field_1755 instanceof class_479 ? AuraCrafter.Mode.CRAFT_FILL : AuraCrafter.Mode.CRAFT_OPEN_TABLE;
         this.waitTicks = 6;
      }
   }

   private void tickMilk() {
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
         this.waitTicks = 1;
      } else if (this.countInventory(class_1802.field_8103) < this.milkBucketsTarget() && this.countInventory(class_1802.field_8550) > 0) {
         int bucketSlot = this.findInventorySlot(class_1802.field_8550);
         if (bucketSlot == -1) {
            this.mode = AuraCrafter.Mode.CRAFT;
            this.waitTicks = 6;
         } else {
            class_10730 cow = this.nearestCow();
            if (cow == null) {
               this.cowAimTicks = 0;
               if (this.fullAuto) {
                  this.waitTicks = 40;
               } else {
                  this.stop("§c[AuraCrafter] Коровы рядом нет.");
               }
            } else {
               this.lookAtCow(cow);
               if (this.cowAimTicks++ < 1) {
                  this.waitTicks = 1;
               } else if (!RayTraceUtil.rayTraceEntity(class_3532.method_15393(mc.field_1724.method_36454()), mc.field_1724.method_36455(), 4.5, cow)) {
                  this.waitTicks = 1;
               } else {
                  this.moveToHotbar(bucketSlot, 0);
                  mc.field_1724.method_31548().method_61496(0);
                  mc.field_1761.method_2905(mc.field_1724, cow, class_1268.field_5808);
                  this.cowAimTicks = 0;
                  this.waitTicks = 1;
               }
            }
         }
      } else {
         this.mode = AuraCrafter.Mode.CRAFT;
         this.waitTicks = 6;
      }
   }

   private void tickOpenCraftingTable() {
      if (mc.field_1755 instanceof class_479) {
         this.mode = AuraCrafter.Mode.CRAFT_FILL;
         this.tableAimTicks = 0;
         this.waitTicks = this.randomTicks(1, 3);
      } else {
         class_239 class_2392 = mc.field_1765;
         class_3965 blockHit;
         class_3965 hit = class_2392 instanceof class_3965
               && mc.field_1687.method_8320((blockHit = (class_3965)class_2392).method_17777()).method_27852(class_2246.field_9980)
            ? blockHit
            : null;
         class_2338 table = hit != null ? hit.method_17777() : this.findNearbyCraftingTable();
         if (table == null) {
            this.tableAimTicks = 0;
            if (this.fullAuto) {
               this.waitTicks = this.randomTicks(8, 16);
            } else {
               this.stop("§c[AuraCrafter] Наведись на верстак.");
            }
         } else {
            this.lookAtCraftingTable(table);
            if (hit == null && this.tableAimTicks++ < this.randomTicks(1, 2)) {
               this.waitTicks = 1;
            } else {
               if (hit == null) {
                  class_243 hitVec = new class_243((double)table.method_10263() + 0.5, (double)table.method_10264() + 1.0, (double)table.method_10260() + 0.5);
                  hit = new class_3965(hitVec, class_2350.field_11036, table, false);
               }

               mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
               this.tableAimTicks = 0;
               this.waitTicks = this.randomTicks(2, 4);
            }
         }
      }
   }

   private void tickCraftFill() {
      if (mc.field_1755 instanceof class_479) {
         class_1703 class_17032 = mc.field_1724.field_7512;
         if (mc.field_1724.field_7512 instanceof class_1714) {
            class_1714 handler = (class_1714)class_17032;
            if (this.hasCraftResult(handler)) {
               this.clickSlot(handler.field_7763, 0, 0, class_1713.field_7794);
               this.waitTicks = this.randomTicks(1, 3);
               this.mode = AuraCrafter.Mode.CRAFT;
               return;
            }

            if (!this.placeRecipe(handler)) {
               this.clearCraftGrid(handler);
               this.mode = AuraCrafter.Mode.CRAFT;
               this.waitTicks = this.randomTicks(2, 4);
               return;
            }

            this.waitTicks = 1;
            return;
         }
      }

      this.mode = AuraCrafter.Mode.CRAFT_OPEN_TABLE;
      this.waitTicks = 6;
   }

   private boolean tickAutoInvis() {
      this.updateAutoInvisAnchor();
      if (this.isInvisMode()) {
         return false;
      } else if (this.autoInvis.getValue() && !this.hasInvisibilityBuff()) {
         if (++this.autoInvisCheckTicks < 80) {
            return false;
         } else {
            this.autoInvisCheckTicks = 0;
            if (!this.canAutoInvisHere()) {
               return false;
            } else {
               this.startAutoInvisInterrupt();
               if (this.findInvisPotionInventorySlot() >= 0) {
                  this.mode = AuraCrafter.Mode.INVIS_DRINK;
                  this.waitTicks = 0;
               } else {
                  this.sendInvisSearch();
                  this.mode = AuraCrafter.Mode.INVIS_OPEN;
                  this.waitTicks = 20;
               }

               return true;
            }
         }
      } else {
         this.autoInvisCheckTicks = 0;
         return false;
      }
   }

   private boolean tickPlayerAutoPause() {
      if (this.mode == AuraCrafter.Mode.PLAYER_PAUSE) {
         this.tickPlayerPause();
         return true;
      } else if (this.autoPausePlayers.getValue() && this.mode != AuraCrafter.Mode.IDLE && this.autoPauseAnarchy > 0) {
         if (this.readCurrentAnarchy() != this.autoPauseAnarchy) {
            return false;
         } else {
            class_1657 player = this.findPauseThreatPlayer();
            if (player == null) {
               return false;
            } else {
               String name = player.method_5477().getString();
               if (this.lastPlayerPauseName != null && this.lastPlayerPauseName.equalsIgnoreCase(name)) {
                  if (mc.field_1755 != null) {
                     mc.field_1724.method_7346();
                  }

                  this.releaseInvisUse();
                  this.releaseHumanMovementKeys();
                  this.releaseAntiAfkKeys();
                  this.sendCommand("hub");
                  this.msg("Â§c[AuraCrafter] Ð˜Ð³Ñ€Ð¾Ðº " + name + " Ñ\u0081Ð½Ð¾Ð²Ð° Ñ€Ñ\u008fÐ´Ð¾Ð¼, Ð²Ñ‹Ñ…Ð¾Ð¶Ñƒ Ð¸ Ð²Ñ‹ÐºÐ»ÑŽÑ‡Ð°ÑŽÑ\u0081ÑŒ.");
                  this.setEnabled(false);
                  return true;
               } else {
                  if (mc.field_1755 != null) {
                     mc.field_1724.method_7346();
                  }

                  this.releaseInvisUse();
                  this.releaseHumanMovementKeys();
                  this.releaseAntiAfkKeys();
                  this.refundReservedBuyBalance();
                  this.resumeModeAfterPlayerPause = this.mode;
                  this.playerPauseName = name;
                  this.lastPlayerPauseName = name;
                  this.playerPauseTicks = 0;
                  this.playerPauseReturnTicks = 0;
                  this.playerPauseReturning = false;
                  this.sendCommand("hub");
                  this.mode = AuraCrafter.Mode.PLAYER_PAUSE;
                  this.waitTicks = 0;
                  this.msg("Â§e[AuraCrafter] Ð˜Ð³Ñ€Ð¾Ðº " + name + " Ñ€Ñ\u008fÐ´Ð¾Ð¼, Ð¿Ð°ÑƒÐ·Ð° 5 Ð¼Ð¸Ð½ÑƒÑ‚.");
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private void tickPlayerPause() {
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      if (!this.playerPauseReturning) {
         if (++this.playerPauseTicks >= 6000) {
            if (this.autoPauseAnarchy > 0) {
               this.sendCommand("an" + this.autoPauseAnarchy);
            }

            this.playerPauseReturning = true;
            this.playerPauseReturnTicks = 0;
         }
      } else if (++this.playerPauseReturnTicks >= 200) {
         this.resumeAfterPlayerPause();
      }
   }

   private void resumeAfterPlayerPause() {
      AuraCrafter.Mode resume = this.resumeModeAfterPlayerPause == null ? AuraCrafter.Mode.CRAFT : this.resumeModeAfterPlayerPause;
      this.playerPauseName = null;
      this.playerPauseTicks = 0;
      this.playerPauseReturnTicks = 0;
      this.playerPauseReturning = false;
      this.resumeModeAfterPlayerPause = AuraCrafter.Mode.IDLE;
      this.noScreenTicks = 0;
      this.stateTicks = 0;
      this.lastProgressSignature = "";
      this.stuckTicks = 0;
      switch (resume) {
         case BUY_OPEN:
         case BUY_READ:
         case BUY_CONFIRM:
            if (this.activeBuyName == null && this.activeBuyItem == null) {
               this.mode = AuraCrafter.Mode.CRAFT;
               this.waitTicks = 6;
            } else {
               if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && !this.canBuyNetheriteAboveReserve(0L)) {
                  this.handleNoNetheriteBudget();
                  return;
               }

               this.sendActiveBuySearch();
               this.mode = AuraCrafter.Mode.BUY_OPEN;
               this.waitTicks = 20;
            }
            break;
         case CRAFT:
         case CRAFT_MILK:
         case CRAFT_OPEN_TABLE:
         case CRAFT_FILL:
            this.mode = AuraCrafter.Mode.CRAFT;
            this.waitTicks = 6;
            break;
         case INVIS_OPEN:
         case INVIS_READ:
         case INVIS_CONFIRM:
         case INVIS_DRINK:
         default:
            this.mode = AuraCrafter.Mode.CRAFT;
            this.waitTicks = 6;
            break;
         case SELL_PREPARE:
         case SELL_SEND:
         case SELL:
            this.mode = AuraCrafter.Mode.SELL;
            this.waitTicks = 6;
            break;
         case STORAGE_OPEN:
         case STORAGE_CLICK:
         case STORAGE_TAKE:
            this.startStorageRelist();
      }

      this.msg("Â§a[AuraCrafter] ÐŸÐ°ÑƒÐ·Ð° Ð·Ð°ÐºÐ¾Ð½Ñ‡Ð¸Ð»Ð°Ñ\u0081ÑŒ, Ð²ÐµÑ€Ð½ÑƒÐ»Ñ\u0081Ñ\u008f Ð½Ð° Ð°Ð½ÐºÑƒ " + this.autoPauseAnarchy + ".");
   }

   private class_1657 findPauseThreatPlayer() {
      EntityEsp esp = EntityEsp.get();
      if (esp != null && esp.isEnabled() && esp.player.getValue()) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            double maxDistance = this.autoPauseRange.getValue().doubleValue();
            double maxSq = maxDistance * maxDistance;
            class_1657 best = null;
            double bestDistance = maxSq;

            for (class_1657 player : mc.field_1687.method_18456()) {
               double distance;
               String name;
               if (player != mc.field_1724
                  && (name = player.method_5477().getString()) != null
                  && !name.isEmpty()
                  && (player.method_5797() == null || !player.method_5797().getString().startsWith("Ghost_"))
                  && !Client.get().friendManager().isFriend(name)
                  && (!esp.ignoreNaked.getValue() || EntityEsp.hasArmor(player))
                  && (distance = player.method_5858(mc.field_1724)) <= bestDistance) {
                  bestDistance = distance;
                  best = player;
               }
            }

            return best;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private void updateAutoInvisAnchor() {
      if (!this.autoInvis.getValue()) {
         this.autoInvisWasEnabled = false;
         this.autoInvisAnarchy = -1;
      } else {
         if (!this.autoInvisWasEnabled) {
            this.autoInvisWasEnabled = true;
            this.autoInvisAnarchy = this.readCurrentAnarchy();
         }

         if (this.autoInvisAnarchy <= 0) {
            this.autoInvisAnarchy = this.readCurrentAnarchy();
         }
      }
   }

   private boolean canAutoInvisHere() {
      if (this.pendingCommand != null || mc.field_1724.method_6115()) {
         return false;
      } else if (!this.allowAwayAnarchy
         && this.targetAnarchy <= 0
         && this.mode != AuraCrafter.Mode.AUTO_WAIT_JOIN
         && this.mode != AuraCrafter.Mode.AUTO_WAIT_BUDGET) {
         int current = this.readCurrentAnarchy();
         return this.autoInvisAnarchy > 0 && current == this.autoInvisAnarchy;
      } else {
         return false;
      }
   }

   private void startAutoInvisInterrupt() {
      this.resumeModeAfterInvis = this.mode;
      this.resumeWaitAfterInvis = this.waitTicks;
      this.refundReservedBuyBalance();
      this.invisOpenTicks = 0;
      this.invisConfirmClicked = false;
      this.invisConfirmHoldTicks = 0;
      this.invisUseStarted = false;
      this.invisDrinkTicks = 0;
      this.invisPreviousSlot = -1;
      this.lastProgressSignature = "";
      this.stuckTicks = 0;
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      this.buyConfirmClicked = false;
      this.buyConfirmHoldTicks = 0;
   }

   private void sendInvisSearch() {
      this.closeCurrentSearchScreen();
      this.noScreenTicks = 0;
      this.invisOpenTicks = 0;
      this.sendAhCommand("ah search инвиз");
   }

   private void tickInvisOpen() {
      if (mc.field_1755 instanceof class_476) {
         if (this.isIgnoredSearchScreen()) {
            this.closeCurrentSearchScreen();
            this.waitTicks = 2;
         } else {
            this.ignoredSearchScreenSyncId = -1;
            this.noScreenTicks = 0;
            this.invisOpenTicks = 0;
            this.mode = AuraCrafter.Mode.INVIS_READ;
            this.waitTicks = 6;
         }
      } else if (++this.noScreenTicks > 17) {
         this.sendInvisSearch();
         this.waitTicks = 20;
      } else {
         this.waitTicks = 6;
      }
   }

   private void tickInvisRead() {
      if (this.findInvisPotionInventorySlot() >= 0) {
         this.mode = AuraCrafter.Mode.INVIS_DRINK;
         this.waitTicks = 0;
      } else if (!(mc.field_1755 instanceof class_476 screen)) {
         this.mode = AuraCrafter.Mode.INVIS_OPEN;
         this.waitTicks = 6;
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         if (this.isBuyConfirmScreen(screen, handler)) {
            this.mode = AuraCrafter.Mode.INVIS_CONFIRM;
            this.waitTicks = 6;
         } else {
            int slot = this.findInvisBuySlot(handler);
            if (slot == -1) {
               this.clickRefresh(handler);
               this.waitTicks = 10;
            } else {
               this.clickSlot(handler.field_7763, slot, 0, class_1713.field_7794);
               this.mode = AuraCrafter.Mode.INVIS_CONFIRM;
               this.waitTicks = 10;
            }
         }
      }
   }

   private void tickInvisConfirm() {
      if (this.findInvisPotionInventorySlot() >= 0) {
         this.mode = AuraCrafter.Mode.INVIS_DRINK;
         this.waitTicks = 0;
      } else if (!(mc.field_1755 instanceof class_476 screen)) {
         this.mode = AuraCrafter.Mode.INVIS_DRINK;
         this.waitTicks = 4;
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         if (!this.isBuyConfirmScreen(screen, handler)) {
            this.invisConfirmClicked = false;
            this.invisConfirmHoldTicks = 0;
            this.mode = AuraCrafter.Mode.INVIS_READ;
            this.waitTicks = 10;
         } else if (this.invisConfirmClicked) {
            if (++this.invisConfirmHoldTicks < 20) {
               this.waitTicks = 2;
            } else {
               this.invisConfirmClicked = false;
               this.invisConfirmHoldTicks = 0;
               this.mode = AuraCrafter.Mode.INVIS_DRINK;
               this.waitTicks = 4;
            }
         } else {
            int confirm = this.findConfirmSlot(handler);
            if (confirm >= 0) {
               this.clickSlot(handler.field_7763, confirm, 0, class_1713.field_7790);
               this.invisConfirmClicked = true;
               this.invisConfirmHoldTicks = 0;
               this.waitTicks = 10;
            } else {
               this.waitTicks = 6;
            }
         }
      }
   }

   private void tickInvisDrink() {
      if (this.hasInvisibilityBuff()) {
         this.finishAutoInvis();
      } else if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
         this.waitTicks = 2;
      } else {
         int potionSlot = this.findInvisPotionInventorySlot();
         if (!this.invisUseStarted) {
            if (potionSlot == -1) {
               if (++this.invisDrinkTicks > 40) {
                  this.sendInvisSearch();
                  this.mode = AuraCrafter.Mode.INVIS_OPEN;
                  this.waitTicks = 20;
               } else {
                  this.waitTicks = 2;
               }
            } else {
               this.invisPreviousSlot = mc.field_1724.method_31548().method_67532();
               int hotbarSlot = this.moveInvisPotionToHotbar(potionSlot);
               if (hotbarSlot < 0) {
                  this.waitTicks = 5;
               } else {
                  mc.field_1724.method_31548().method_61496(hotbarSlot);
                  mc.field_1690.field_1904.method_23481(true);
                  mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                  this.invisUseStarted = true;
                  this.invisDrinkTicks = 0;
                  this.waitTicks = 1;
               }
            }
         } else {
            mc.field_1690.field_1904.method_23481(true);
            if (++this.invisDrinkTicks <= 60) {
               this.waitTicks = 1;
            } else {
               this.releaseInvisUse();
               this.invisUseStarted = false;
               this.invisDrinkTicks = 0;
               if (this.findInvisPotionInventorySlot() == -1) {
                  this.sendInvisSearch();
                  this.mode = AuraCrafter.Mode.INVIS_OPEN;
                  this.waitTicks = 20;
               } else {
                  this.waitTicks = 4;
               }
            }
         }
      }
   }

   private void tickSell() {
      if (this.listed >= this.sellSlots.getValue().intValue()) {
         if (++this.fullSellTicks >= 34) {
            this.startStorageRelist();
         } else {
            this.waitTicks = 2;
         }
      } else {
         this.fullSellTicks = 0;
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
            this.waitTicks = 6;
         } else {
            this.mode = AuraCrafter.Mode.SELL_PREPARE;
         }
      }
   }

   private void tickSellPrepare() {
      if (!this.prepareSingleSellItem()) {
         if (this.fullAuto) {
            this.beginAutoCycle();
         } else {
            if (this.checkedStorageForSell && !this.storageTookSellLot && this.listed <= 0) {
               this.emptyStorageChecks++;
               if (this.emptyStorageChecks >= 3) {
                  this.stop("§a[AuraCrafter] Продажа выключена: хранилище пустое 3 раза.");
                  return;
               }
            }

            this.startStorageRelist();
         }
      } else {
         this.checkedStorageForSell = false;
         this.emptyStorageChecks = 0;
         mc.field_1724.method_31548().method_61496(0);
         this.mode = AuraCrafter.Mode.SELL_SEND;
         this.waitTicks = this.randomTicks(2, 7);
      }
   }

   private void tickSellSend() {
      mc.field_1724.method_31548().method_61496(0);
      class_1799 hand = mc.field_1724.method_6047();
      if (this.isSellTarget(hand) && hand.method_7947() == 1) {
         this.sendCommand("ah sell " + this.nextSellPrice());
         this.mode = AuraCrafter.Mode.SELL;
         this.waitTicks = this.randomTicks(8, 18);
      } else {
         this.mode = AuraCrafter.Mode.SELL_PREPARE;
         this.waitTicks = 3;
      }
   }

   private void renderInfoPanel(EventDisplay event) {
      int diamonds = this.countInventory(class_1802.field_8477);
      int netherite = this.countInventory(class_1802.field_22020);
      int milk = this.countInventory(class_1802.field_8103);
      int buckets = this.countInventory(class_1802.field_8550);
      int possibleCrafts = Math.min(netherite, diamonds / 4);
      int milkTarget = this.milkBucketsTarget();
      int slotsMax = this.sellSlots.getValue().intValue();
      String[] left = new String[]{
         "Режим", "Авто", "Анка", "Баланс", "Крафтов", "Алмазы", "Незер", "Молоко", "Пустые", "Закуп", "Резерв", "Клан", "Фильтр", "Продажа", "Лоты"
      };
      String[] right = new String[]{
         this.modeName(),
         this.fullAuto ? "ON" : "OFF",
         (this.homeAnarchy > 0 ? String.valueOf(this.homeAnarchy) : "?") + (this.targetAnarchy > 0 ? " -> " + this.targetAnarchy : ""),
         this.balance > 0L ? "$" + this.fmt(this.balance) : "?",
         this.fmt((long)possibleCrafts),
         this.fmt((long)diamonds),
         this.fmt((long)netherite),
         this.fmt((long)milk) + "/" + this.fmt((long)milkTarget),
         this.fmt((long)buckets),
         (this.activeBuyName == null ? this.buyItem.getValue() : this.activeBuyName)
            + " <= $"
            + this.fmt(this.activeMaxUnitPrice > 0L ? this.activeMaxUnitPrice : this.parsePrice(this.maxBuyPrice.getValue(), 0L)),
         "$" + this.fmt(this.parsePrice(this.reserveMoney.getValue(), 0L)),
         this.clanInvest.getValue()
            ? this.fmt(this.parsePrice(this.clanInvestAmount.getValue(), 0L)) + " от " + this.fmt(this.parsePrice(this.clanInvestAt.getValue(), 0L))
            : "OFF",
         this.buyRejectReason.isEmpty() ? "ok" : this.buyRejectReason,
         this.sellItem.getValue() + " $" + this.fmt(this.parsePrice(this.sellPrice.getValue(), 0L)) + this.sellPriceSuffix(),
         this.listed + "/" + slotsMax
      };
      String[] statLeft = new String[]{"Время", "Заработал", "Продажи", "В клан", "Вёдра"};
      String[] statRight = new String[]{
         this.sessionElapsedText(),
         this.signedMoney(this.sessionProfit()),
         "$" + this.fmt(this.sessionSellEarned),
         "$" + this.fmt(this.sessionClanInvested),
         milkTarget + "/8"
      };
      float width = 130.0F;

      for (int i2 = 0; i2 < statLeft.length; i2++) {
         width = Math.max(width, 16.0F + Fonts.sf_medium.getWidth(statLeft[i2], 5.0F) + Fonts.sf_medium.getWidth(statRight[i2], 5.0F) + 16.0F);
      }

      for (int var28 = 0; var28 < left.length; var28++) {
         width = Math.max(width, 16.0F + Fonts.sf_medium.getWidth(left[var28], 5.0F) + Fonts.sf_medium.getWidth(right[var28], 5.0F) + 16.0F);
      }

      float rowH = 8.0F;
      float height = 24.0F + (float)(left.length + statLeft.length) * rowH;
      float x = this.infoDrag.position.x;
      float y = this.infoDrag.position.y;
      this.infoDrag.size.set(width, height);
      if (Hud.getAlpha() != 1.0F) {
         RenderUtil.Blur.glass(x, y, width, height, 1.0F, 7.0F, ColorUtil.getRectColor(Hud.getAlpha()), 15.0F, 1.0F, 1.0F, 3.0F);
      }

      String state = this.mode == AuraCrafter.Mode.IDLE ? "IDLE" : "ACTIVE";
      float stateW = Fonts.sf_medium.getWidth(state, 6.5F);
      Fonts.sf_medium.draw("Aura Crafter", x + 6.5F, y + 7.0F, 6.5F, ColorUtil.getColor(255));
      Fonts.sf_medium
         .draw(
            state,
            x + width - 6.5F - stateW,
            y + 7.0F,
            6.5F,
            this.mode == AuraCrafter.Mode.IDLE ? ColorUtil.getColor(170) : ColorUtil.getColor(85, 255, 119, 1.0F)
         );
      float sy = y + 18.0F;

      for (int i = 0; i < statLeft.length; i++) {
         float rowY = sy + (float)i * rowH;
         Fonts.sf_medium.draw(statLeft[i], x + 6.5F, rowY + 2.0F, 6.0F, this.hudStatLabelColor(statLeft[i]));
         Fonts.sf_medium
            .draw(
               statRight[i],
               x + width - 6.5F - Fonts.sf_medium.getWidth(statRight[i], 6.0F),
               rowY + 2.0F,
               6.0F,
               this.hudStatValueColor(statLeft[i], statRight[i])
            );
      }

      sy += (float)statLeft.length * rowH;

      for (int var27 = 0; var27 < left.length; var27++) {
         float rowY = sy + (float)var27 * rowH;
         int leftColor = var27 == 0 ? ColorUtil.getClientColor(255) : ColorUtil.getColor(180);
         int rightColor = var27 == 1 && this.fullAuto ? ColorUtil.getColor(85, 255, 119, 1.0F) : ColorUtil.getColor(220);
         Fonts.sf_medium.draw(left[var27], x + 6.5F, rowY + 2.0F, 6.0F, leftColor);
         Fonts.sf_medium.draw(right[var27], x + width - 6.5F - Fonts.sf_medium.getWidth(right[var27], 6.0F), rowY + 2.0F, 6.0F, rightColor);
      }
   }

   private void drawBar(float x, float y, float w, float h, int value, int max, int color) {
      float fill = Math.max(0.0F, Math.min(w, w * ((float)value / (float)max)));
      RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.getColor(0, 0.18F), 2.0F);
      RenderUtil.Render2D.rect(x, y, fill, h, color, 2.0F);
      RenderUtil.Render2D.outline(x, y, w, h, 0.5F, ColorUtil.getColor(255, 0.08F), 2.0F);
   }

   private int hudStatLabelColor(String label) {
      if ("Время".equals(label)) {
         return ColorUtil.getColor(135, 214, 255, 1.0F);
      } else if ("Заработал".equals(label)) {
         return ColorUtil.getColor(159, 238, 188, 1.0F);
      } else {
         return "В клан".equals(label) ? ColorUtil.getColor(255, 220, 132, 1.0F) : ColorUtil.getColor(205);
      }
   }

   private int hudStatValueColor(String label, String value) {
      if ("Заработал".equals(label)) {
         return value.startsWith("-") ? ColorUtil.getColor(255, 120, 120, 1.0F) : ColorUtil.getColor(105, 255, 156, 1.0F);
      } else if ("Время".equals(label)) {
         return ColorUtil.getColor(150, 225, 255, 1.0F);
      } else if ("В клан".equals(label)) {
         return ColorUtil.getColor(255, 229, 148, 1.0F);
      } else {
         return "Вёдра".equals(label) ? ColorUtil.getColor(172, 210, 255, 1.0F) : ColorUtil.getColor(230);
      }
   }

   private void startStorageRelist() {
      if (this.listed <= 0 && this.countSellTargetInventory() <= 0) {
         this.storageTookSellLot = false;
         this.checkedStorageForSell = true;
         this.storageTakeClicks = 0;
         if (this.fullAuto) {
            this.beginAutoCycle();
         } else {
            this.stop("§a[AuraCrafter] Продажа выключена: лотов и предметов для продажи нет.");
         }
      } else {
         this.storageTookSellLot = false;
         this.checkedStorageForSell = true;
         this.storageTakeClicks = 0;
         this.sendAhCommand("ah");
         this.mode = AuraCrafter.Mode.STORAGE_OPEN;
         this.noScreenTicks = 0;
         this.stateTicks = 0;
         this.waitTicks = 20;
      }
   }

   private void tickStorageOpen() {
      if (mc.field_1755 instanceof class_476) {
         this.noScreenTicks = 0;
         this.mode = AuraCrafter.Mode.STORAGE_CLICK;
         this.waitTicks = 2;
      } else if (++this.noScreenTicks > 180) {
         this.sendAhCommand("ah");
         this.noScreenTicks = 0;
         this.waitTicks = 20;
      } else {
         this.waitTicks = 6;
      }
   }

   private void tickStorageClick() {
      if (!(mc.field_1755 instanceof class_476 screen)) {
         this.mode = AuraCrafter.Mode.STORAGE_OPEN;
         this.waitTicks = 6;
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         int storage = this.findStorageSlot(handler);
         if (storage == -1) {
            if (++this.noScreenTicks > 180) {
               mc.field_1724.method_7346();
               this.startStorageRelist();
            } else {
               this.waitTicks = 5;
            }
         } else {
            this.noScreenTicks = 0;
            this.clickSlot(handler.field_7763, storage, 0, class_1713.field_7790);
            this.mode = AuraCrafter.Mode.STORAGE_TAKE;
            this.storageTakeClicks = 0;
            this.waitTicks = 6;
         }
      }
   }

   private void tickStorageTake() {
      if (!(mc.field_1755 instanceof class_476 screen)) {
         this.mode = AuraCrafter.Mode.SELL;
         this.waitTicks = 6;
      } else {
         class_1707 handler = (class_1707)screen.method_17577();
         int slot = this.findStoredSellLot(handler);
         if (slot != -1) {
            this.noScreenTicks = 0;
            this.stateTicks = 0;
            this.clickSlot(handler.field_7763, slot, 0, class_1713.field_7790);
            this.storageTookSellLot = true;
            this.emptyStorageChecks = 0;
            this.storageTakeClicks++;
            this.listed = Math.max(0, this.listed - 1);
            if (this.listed > 0 && this.storageTakeClicks < this.sellSlots.getValue().intValue() + 2) {
               this.waitTicks = 2;
            } else {
               mc.field_1724.method_7346();
               this.mode = AuraCrafter.Mode.SELL;
               this.noScreenTicks = 0;
               this.stateTicks = 0;
               this.waitTicks = 6;
            }
         } else if (this.storageTookSellLot) {
            if (this.listed > 0 && ++this.noScreenTicks <= 20) {
               this.waitTicks = 3;
            } else {
               this.listed = 0;
               mc.field_1724.method_7346();
               this.mode = AuraCrafter.Mode.SELL;
               this.noScreenTicks = 0;
               this.stateTicks = 0;
               this.waitTicks = 6;
            }
         } else {
            if (this.listed > 0 && ++this.noScreenTicks <= 60) {
               this.waitTicks = 5;
            } else {
               this.listed = 0;
               mc.field_1724.method_7346();
               this.mode = AuraCrafter.Mode.SELL;
               this.waitTicks = 6;
            }
         }
      }
   }

   private boolean placeRecipe(class_1714 handler) {
      this.clearWrongCraftSlots(handler);
      if (!this.putSlot(handler, 1, class_1802.field_8477)) {
         return false;
      } else if (!this.putSlot(handler, 2, class_1802.field_8103)) {
         return false;
      } else if (!this.putSlot(handler, 3, class_1802.field_8477)) {
         return false;
      } else if (!this.putSlot(handler, 4, class_1802.field_8103)) {
         return false;
      } else if (!this.putSlot(handler, 5, class_1802.field_22020)) {
         return false;
      } else if (!this.putSlot(handler, 6, class_1802.field_8103)) {
         return false;
      } else if (!this.putSlot(handler, 7, class_1802.field_8477)) {
         return false;
      } else {
         return !this.putSlot(handler, 8, class_1802.field_8103) ? false : this.putSlot(handler, 9, class_1802.field_8477);
      }
   }

   private boolean putSlot(class_1714 handler, int craftSlot, class_1792 item) {
      class_1799 current = handler.method_7611(craftSlot).method_7677();
      if (current.method_7909() == item) {
         return true;
      } else if (!current.method_7960()) {
         return false;
      } else {
         int invSlot = this.findInventorySlot(item);
         if (invSlot == -1) {
            return false;
         } else {
            int screenSlot = this.craftingPlayerSlot(invSlot);
            this.clickSlot(handler.field_7763, screenSlot, 0, class_1713.field_7790);
            this.clickSlot(handler.field_7763, craftSlot, 1, class_1713.field_7790);
            this.clickSlot(handler.field_7763, screenSlot, 0, class_1713.field_7790);
            return true;
         }
      }
   }

   private void clearWrongCraftSlots(class_1714 handler) {
      for (int i = 1; i <= 9; i++) {
         class_1792 expected = this.expectedRecipeItem(i);
         class_1799 stack = handler.method_7611(i).method_7677();
         if (!stack.method_7960() && stack.method_7909() != expected) {
            this.clickSlot(handler.field_7763, i, 0, class_1713.field_7794);
         }
      }
   }

   private void clearCraftGrid(class_1714 handler) {
      for (int i = 1; i <= 9; i++) {
         if (!handler.method_7611(i).method_7677().method_7960()) {
            this.clickSlot(handler.field_7763, i, 0, class_1713.field_7794);
         }
      }
   }

   private class_1792 expectedRecipeItem(int slot) {
      return switch (slot) {
         case 1, 3, 7, 9 -> class_1802.field_8477;
         case 2, 4, 6, 8 -> class_1802.field_8103;
         case 5 -> class_1802.field_22020;
         default -> class_1802.field_8162;
      };
   }

   private boolean hasCraftResult(class_1714 handler) {
      class_1799 result = handler.method_7611(0).method_7677();
      return !result.method_7960() && this.clean(result.method_7964().getString()).contains("Божья аура".toLowerCase());
   }

   private boolean isInvisMode() {
      return this.mode == AuraCrafter.Mode.INVIS_OPEN
         || this.mode == AuraCrafter.Mode.INVIS_READ
         || this.mode == AuraCrafter.Mode.INVIS_CONFIRM
         || this.mode == AuraCrafter.Mode.INVIS_DRINK;
   }

   private boolean hasInvisibilityBuff() {
      return mc.field_1724 != null && mc.field_1724.method_6059(class_1294.field_5905);
   }

   private int findInvisPotionInventorySlot() {
      int fallback = -1;

      for (int i = 0; i < 36; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (this.matchesAutoInvisPotion(stack, true)) {
            return i;
         }

         if (fallback == -1 && this.matchesAutoInvisPotion(stack, false)) {
            fallback = i;
         }
      }

      return fallback;
   }

   private int findInvisBuySlot(class_1707 handler) {
      int strict = this.findInvisBuySlot(handler, true);
      return strict >= 0 ? strict : this.findInvisBuySlot(handler, false);
   }

   private int findInvisBuySlot(class_1707 handler, boolean strictPlus) {
      int containerSlots = handler.method_17388() * 9;
      int bestSlot = -1;
      long bestPrice = Long.MAX_VALUE;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         long price;
         class_1799 stack;
         if (slot.method_7681()
            && (stack = slot.method_7677()).method_7947() == 1
            && this.matchesAutoInvisPotion(stack, strictPlus)
            && (price = this.getLorePrice(stack)) > 0L
            && price < bestPrice) {
            bestPrice = price;
            bestSlot = i;
         }
      }

      return bestSlot;
   }

   private boolean matchesAutoInvisPotion(class_1799 stack, boolean strictPlus) {
      if (!stack.method_7960() && stack.method_7909() == class_1802.field_8574) {
         String type = this.autoInvisType.getValue();
         String text = this.stackText(stack);
         boolean textLooksInvis = text.contains("инвиз") || text.contains("невид");
         boolean shortInvis = this.isPotionEntry(stack, class_1847.field_8997);
         boolean longInvis = this.isPotionEntry(stack, class_1847.field_9000);
         if ("3m".equalsIgnoreCase(type)) {
            return shortInvis || textLooksInvis && text.contains("3") && !text.contains("8");
         } else if (!"8+".equalsIgnoreCase(type)) {
            return longInvis || textLooksInvis && text.contains("8");
         } else {
            return strictPlus ? (longInvis || textLooksInvis) && this.isEightPlusInvis(stack) : longInvis || textLooksInvis && text.contains("8");
         }
      } else {
         return false;
      }
   }

   private boolean isPotionEntry(class_1799 stack, class_6880<class_1842> potion) {
      class_1844 potionContents = (class_1844)stack.method_58694(class_9334.field_49651);
      return potionContents != null && potionContents.comp_2378().isPresent() && ((class_6880)potionContents.comp_2378().get()).equals(potion);
   }

   private boolean isEightPlusInvis(class_1799 stack) {
      String text = this.stackText(stack);
      boolean hasExtraDurationLine = text.matches(".*\\+\\s*\\d{1,2}\\s*[:：]\\s*\\d{2}.*");
      return hasExtraDurationLine || text.contains("8+") || text.contains("+8") || text.contains("усилен") || text.contains("plus");
   }

   private int moveInvisPotionToHotbar(int inventorySlot) {
      if (inventorySlot < 0) {
         return -1;
      } else if (inventorySlot < 9) {
         return inventorySlot;
      } else {
         int hotbarSlot = this.findEmptyHotbarSlot();
         if (hotbarSlot == -1) {
            hotbarSlot = 8;
         }

         this.moveToHotbar(inventorySlot, hotbarSlot);
         return hotbarSlot;
      }
   }

   private int findEmptyHotbarSlot() {
      for (int i = 8; i >= 0; i--) {
         if (mc.field_1724.method_31548().method_5438(i).method_7960()) {
            return i;
         }
      }

      return -1;
   }

   private void releaseInvisUse() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1904.method_23481(false);
      }
   }

   private void finishAutoInvis() {
      AuraCrafter.Mode resume = this.resumeModeAfterInvis == null ? AuraCrafter.Mode.IDLE : this.resumeModeAfterInvis;
      int savedWait = this.resumeWaitAfterInvis;
      this.releaseInvisUse();
      if (this.invisPreviousSlot >= 0) {
         mc.field_1724.method_31548().method_61496(this.invisPreviousSlot);
      }

      this.resumeModeAfterInvis = AuraCrafter.Mode.IDLE;
      this.resumeWaitAfterInvis = 0;
      this.invisOpenTicks = 0;
      this.invisConfirmClicked = false;
      this.invisConfirmHoldTicks = 0;
      this.invisUseStarted = false;
      this.invisDrinkTicks = 0;
      this.invisPreviousSlot = -1;
      this.lastProgressSignature = "";
      this.stuckTicks = 0;
      this.resumeAfterInvis(resume, savedWait);
   }

   private void resumeAfterInvis(AuraCrafter.Mode resume, int savedWait) {
      switch (resume) {
         case BUY_OPEN:
         case BUY_READ:
         case BUY_CONFIRM:
            if (this.activeBuyName == null && this.activeBuyItem == null) {
               this.mode = AuraCrafter.Mode.IDLE;
               this.waitTicks = 0;
            } else {
               if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && !this.canBuyNetheriteAboveReserve(0L)) {
                  this.handleNoNetheriteBudget();
                  return;
               }

               this.sendActiveBuySearch();
               this.mode = AuraCrafter.Mode.BUY_OPEN;
               this.waitTicks = 20;
            }
            break;
         case CRAFT:
         default:
            this.mode = resume;
            this.waitTicks = Math.max(6, savedWait);
            break;
         case CRAFT_MILK:
         case CRAFT_OPEN_TABLE:
         case CRAFT_FILL:
            this.mode = AuraCrafter.Mode.CRAFT;
            this.waitTicks = 6;
            break;
         case INVIS_OPEN:
         case INVIS_READ:
         case INVIS_CONFIRM:
         case INVIS_DRINK:
            this.mode = AuraCrafter.Mode.IDLE;
            this.waitTicks = 0;
            break;
         case SELL_PREPARE:
         case SELL_SEND:
            this.mode = AuraCrafter.Mode.SELL;
            this.waitTicks = 6;
            break;
         case STORAGE_OPEN:
         case STORAGE_CLICK:
         case STORAGE_TAKE:
            this.startStorageRelist();
      }
   }

   private int findBuySlot(class_1707 handler) {
      this.normalizeActiveBuyState();
      String target = this.clean(this.activeBuyName == null ? this.buyItem.getValue() : this.activeBuyName);
      long maxPrice = this.activeMaxUnitPrice > 0L ? this.activeMaxUnitPrice : this.parsePrice(this.maxBuyPrice.getValue(), Long.MAX_VALUE);
      int containerSlots = handler.method_17388() * 9;
      int bestSlot = -1;
      long bestUnit = Long.MAX_VALUE;
      long bestPrice = 0L;
      long reserve = this.parsePrice(this.reserveMoney.getValue(), 3000000L);
      int seen = 0;
      int wrongItem = 0;
      int wrongName = 0;
      int noPrice = 0;
      int reserveBlocked = 0;
      int expensive = 0;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681()) {
            class_1799 stack = slot.method_7677();
            seen++;
            if (this.activeBuyItem != null && stack.method_7909() != this.activeBuyItem) {
               wrongItem++;
            } else if (!this.clean(stack.method_7964().getString()).contains(target)) {
               wrongName++;
            } else {
               long price = this.getLorePrice(stack);
               if (price <= 0L) {
                  noPrice++;
               } else if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && this.balance > 0L && this.balance - price < reserve) {
                  reserveBlocked++;
               } else {
                  long unit = price / (long)Math.max(1, stack.method_7947());
                  if (unit <= maxPrice && unit < bestUnit) {
                     bestUnit = unit;
                     bestSlot = i;
                     bestPrice = price;
                  } else {
                     expensive++;
                  }
               }
            }
         }
      }

      this.selectedBuyPrice = bestPrice;
      this.buyRejectReason = bestSlot >= 0
         ? "$" + this.fmt(bestPrice) + "/" + this.fmt(bestUnit)
         : (
            seen == 0
               ? "нет лотов"
               : (
                  wrongItem >= seen
                     ? "item-id"
                     : (
                        wrongName >= seen - wrongItem
                           ? "имя"
                           : (noPrice > 0 ? "нет цены" : (reserveBlocked > 0 ? "резерв" : (expensive > 0 ? "дорого" : "нет слота")))
                     )
               )
         );
      return bestSlot;
   }

   private void clickRefresh(class_1707 handler) {
      int refresh = this.findStarSlot(handler);
      if (refresh >= 0) {
         this.clickSlot(handler.field_7763, refresh, 0, class_1713.field_7790);
      }
   }

   private boolean isBuyConfirmScreen(class_476 screen, class_1707 handler) {
      String title = this.clean(screen.method_25440().getString());
      return title.contains("подтверждение") || title.contains("подозрительная цена") || this.findConfirmGlassSlot(handler) >= 0;
   }

   private int findConfirmSlot(class_1707 handler) {
      int suspicious = this.findSuspiciousPriceConfirmSlot(handler);
      return suspicious >= 0 ? suspicious : this.findConfirmGlassSlot(handler);
   }

   private int findConfirmGlassSlot(class_1707 handler) {
      int containerSlots = handler.method_17388() * 9;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         class_1799 stack;
         if (slot.method_7681() && (stack = slot.method_7677()).method_7909() == class_1802.field_8581 && this.stackText(stack).contains("купить")) {
            return i;
         }
      }

      return -1;
   }

   private int findSuspiciousPriceConfirmSlot(class_1707 handler) {
      int containerSlots = handler.method_17388() * 9;
      int rows = Math.max(1, handler.method_17388());

      for (int row = 0; row < rows; row++) {
         for (int col = 0; col < 4; col++) {
            int i = row * 9 + col;
            class_1735 slot;
            if (i < containerSlots && (slot = handler.method_7611(i)).method_7681() && slot.method_7677().method_7909() == class_1802.field_8656) {
               return i;
            }
         }
      }

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681() && slot.method_7677().method_7909() == class_1802.field_8656) {
            return i;
         }
      }

      return -1;
   }

   private int findStarSlot(class_1707 handler) {
      int containerSlots = handler.method_17388() * 9;

      for (int i = Math.max(0, containerSlots - 9); i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681() && slot.method_7677().method_7909() == class_1802.field_8137) {
            return i;
         }
      }

      for (int var7 = 0; var7 < containerSlots; var7++) {
         class_1735 slot = handler.method_7611(var7);
         if (slot.method_7681() && slot.method_7677().method_7909() == class_1802.field_8137) {
            return var7;
         }
      }

      return -1;
   }

   private int findNamedSlot(class_1707 handler, String name) {
      String needle = name.toLowerCase();
      int containerSlots = handler.method_17388() * 9;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681() && this.stackText(slot.method_7677()).contains(needle)) {
            return i;
         }
      }

      return -1;
   }

   private int findStorageSlot(class_1707 handler) {
      int containerSlots = handler.method_17388() * 9;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681() && slot.method_7677().method_7909() == class_1802.field_8466) {
            return i;
         }
      }

      int named = this.findNamedSlot(handler, "Хранилище");
      return named >= 0 ? named : -1;
   }

   private int findStoredSellLot(class_1707 handler) {
      int containerSlots = handler.method_17388() * 9;

      for (int i = 0; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         class_1799 stack;
         if (slot.method_7681() && this.isSellTarget(stack = slot.method_7677()) && this.stackText(stack).contains("продается")) {
            return i;
         }
      }

      for (int var7 = 0; var7 < containerSlots; var7++) {
         class_1735 slot = handler.method_7611(var7);
         if (slot.method_7681() && this.isSellTarget(slot.method_7677())) {
            return var7;
         }
      }

      return -1;
   }

   private long getLorePrice(class_1799 stack) {
      class_9290 lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632);
      if (lore == null) {
         return 0L;
      } else {
         for (class_2561 line : lore.comp_2400()) {
            String text = line.getString();
            String digits;
            if ((text.contains("$") || text.toLowerCase().contains("цена")) && !(digits = text.replaceAll("[^0-9]", "")).isEmpty()) {
               return this.parsePrice(digits, 0L);
            }
         }

         return 0L;
      }
   }

   private class_2338 findNearbyCraftingTable() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_2338 origin = mc.field_1724.method_24515();
         class_2338 best = null;
         double bestDistance = Double.MAX_VALUE;

         for (int y = -2; y <= 2; y++) {
            for (int x = -4; x <= 4; x++) {
               for (int z = -4; z <= 4; z++) {
                  class_2338 pos = origin.method_10069(x, y, z);
                  double distance;
                  if (mc.field_1687.method_8320(pos).method_27852(class_2246.field_9980)
                     && (distance = mc.field_1724.method_5707(class_243.method_24953(pos))) < bestDistance) {
                     bestDistance = distance;
                     best = pos;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private void lookAtCraftingTable(class_2338 pos) {
      class_243 target = class_243.method_24953(pos).method_1031(this.randomDouble(-0.12, 0.12), this.randomDouble(-0.08, 0.1), this.randomDouble(-0.12, 0.12));
      class_243 eyes = mc.field_1724.method_33571();
      double dx = target.field_1352 - eyes.field_1352;
      double dy = target.field_1351 - eyes.field_1351;
      double dz = target.field_1350 - eyes.field_1350;
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(dy, horizontal)), -90.0, 90.0);
      RotationProcess.update(
         this.interpolatedRotation(yaw, pitch, 0.62F, 0.86F), this.randomFloat(14.0F, 26.0F), this.randomFloat(12.0F, 22.0F), this.randomTicks(1, 2), 2
      );
   }

   private Rotation interpolatedRotation(float targetYaw, float targetPitch, float minFactor, float maxFactor) {
      float factor = this.randomFloat(minFactor, maxFactor);
      float yaw = mc.field_1724.method_36454() + class_3532.method_15393(targetYaw - mc.field_1724.method_36454()) * factor;
      float pitch = mc.field_1724.method_36455() + (targetPitch - mc.field_1724.method_36455()) * factor;
      return new Rotation(class_3532.method_15393(yaw), class_3532.method_15363(pitch, -90.0F, 90.0F));
   }

   private void lookAtCow(class_10730 cow) {
      class_243 target = new class_243(cow.method_23317(), cow.method_23318(), cow.method_23321())
         .method_1031(
            this.randomDouble(-0.18, 0.18),
            class_3532.method_15350((double)cow.method_17682() * this.randomDouble(0.45, 0.78), 0.45, 1.35),
            this.randomDouble(-0.18, 0.18)
         );
      class_243 eyes = mc.field_1724.method_33571();
      double dx = target.field_1352 - eyes.field_1352;
      double dy = target.field_1351 - eyes.field_1351;
      double dz = target.field_1350 - eyes.field_1350;
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(dy, horizontal)), -90.0, 90.0);
      RotationProcess.update(
         this.interpolatedRotation(yaw, pitch, 0.58F, 0.82F), this.randomFloat(13.0F, 24.0F), this.randomFloat(11.0F, 21.0F), this.randomTicks(1, 2), 2
      );
   }

   private class_10730 nearestCow() {
      class_10730 best = null;
      double bestDistance = 36.0;

      for (class_1297 entity : mc.field_1687.method_18112()) {
         class_10730 cow;
         double distance;
         if (entity instanceof class_10730 && (distance = (cow = (class_10730)entity).method_5858(mc.field_1724)) < bestDistance) {
            bestDistance = distance;
            best = cow;
         }
      }

      return best;
   }

   private int countInventory(class_1792 item) {
      int count = 0;

      for (int i = 0; i < 36; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() == item) {
            count += stack.method_7947();
         }
      }

      return count;
   }

   private int milkBucketsTarget() {
      int possibleCrafts = Math.min(this.countInventory(class_1802.field_22020), this.countInventory(class_1802.field_8477) / 4);
      if (possibleCrafts <= 0) {
         return 4;
      } else {
         int buckets = this.countInventory(class_1802.field_8550) + this.countInventory(class_1802.field_8103);
         return Math.min(Math.min(buckets, possibleCrafts * 4), 8);
      }
   }

   private void markInventoryAction() {
      this.inventoryActionTicks = Math.max(this.inventoryActionTicks, 8);
      this.releaseHumanMovementKeys();
      this.humanLookTicks = Math.max(this.humanLookTicks, 3);
   }

   private void clickSlot(int syncId, int slot, int button, class_1713 actionType) {
      this.markInventoryAction();
      mc.field_1761.method_2906(syncId, slot, button, actionType, mc.field_1724);
   }

   private void tickHumanMovement() {
      if (this.inventoryActionTicks > 0) {
         this.inventoryActionTicks--;
         this.releaseHumanMovementKeys();
         this.humanMoveTicks = 0;
      } else if (this.pendingCommand == null) {
         if (this.mode != AuraCrafter.Mode.IDLE
            && this.mode != AuraCrafter.Mode.PLAYER_PAUSE
            && mc.field_1755 == null
            && !mc.field_1724.method_6115()
            && this.mode != AuraCrafter.Mode.CRAFT_OPEN_TABLE
            && this.mode != AuraCrafter.Mode.CRAFT_FILL
            && this.mode != AuraCrafter.Mode.CRAFT_MILK) {
            if (this.humanMoveTicks > 0) {
               this.humanMoveTicks--;
               if (this.humanMoveTicks <= 0) {
                  this.releaseHumanMovementKeys();
                  this.nextHumanMoveTicks = this.randomTicks(120, 520);
               }
            } else if (this.nextHumanMoveTicks > 0) {
               this.nextHumanMoveTicks--;
            } else {
               this.releaseHumanMovementKeys();
               int pattern = this.randomTicks(0, 8);
               mc.field_1690.field_1894.method_23481(pattern == 0 || pattern == 4 || pattern == 6);
               mc.field_1690.field_1881.method_23481(pattern == 1);
               mc.field_1690.field_1913.method_23481(pattern == 2 || pattern == 4 || pattern == 7);
               mc.field_1690.field_1849.method_23481(pattern == 3 || pattern == 5 || pattern == 6);
               mc.field_1690.field_1903.method_23481(pattern == 7 && Math.random() < 0.35);
               mc.field_1690.field_1832.method_23481(pattern == 8 || Math.random() < 0.12);
               this.humanMoveTicks = this.randomTicks(5, 32);
               if (Math.random() < 0.45) {
                  float targetYaw = mc.field_1724.method_36454() + this.randomFloat(-14.0F, 14.0F);
                  float targetPitch = class_3532.method_15363(mc.field_1724.method_36455() + this.randomFloat(-4.5F, 4.5F), -75.0F, 75.0F);
                  RotationProcess.update(
                     this.interpolatedRotation(targetYaw, targetPitch, 0.28F, 0.48F),
                     this.randomFloat(5.0F, 14.0F),
                     this.randomFloat(5.0F, 13.0F),
                     this.randomTicks(3, 7),
                     1
                  );
               }
            }
         } else {
            if (this.humanMoveTicks > 0) {
               this.releaseHumanMovementKeys();
            }

            this.humanMoveTicks = 0;
            if (this.nextHumanMoveTicks <= 0) {
               this.nextHumanMoveTicks = this.randomTicks(130, 380);
            }
         }
      }
   }

   private void releaseHumanMovementKeys() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1881.method_23481(false);
         mc.field_1690.field_1913.method_23481(false);
         mc.field_1690.field_1849.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
         mc.field_1690.field_1832.method_23481(false);
      }
   }

   private void tickHumanLook() {
      if (this.mode != AuraCrafter.Mode.IDLE && this.mode != AuraCrafter.Mode.PLAYER_PAUSE && mc.field_1724 != null) {
         if (this.inventoryActionTicks <= 0 || this.mode == AuraCrafter.Mode.CRAFT_MILK || this.mode == AuraCrafter.Mode.STORAGE_OPEN) {
            if (this.humanSneakTicks > 0) {
               this.humanSneakTicks--;
               mc.field_1690.field_1832.method_23481(true);
            }

            if (this.humanLookTicks > 0) {
               this.humanLookTicks--;
            } else if (this.isAhLikeMode() || mc.field_1755 instanceof class_476) {
               this.humanLookTicks = this.randomTicks(5, mc.field_1755 instanceof class_476 ? 12 : 26);
               float targetYaw = mc.field_1724.method_36454() + this.randomFloat(-14.0F, 14.0F);
               float targetPitch = class_3532.method_15363(mc.field_1724.method_36455() + this.randomFloat(-4.5F, 4.5F), -78.0F, 78.0F);
               RotationProcess.update(
                  this.interpolatedRotation(targetYaw, targetPitch, 0.24F, 0.44F),
                  this.randomFloat(4.0F, 12.0F),
                  this.randomFloat(4.0F, 11.0F),
                  this.randomTicks(3, 7),
                  0
               );
            }
         }
      }
   }

   private boolean tickDelayedAhCommand() {
      if (this.delayedAhCommand == null) {
         return false;
      } else {
         if (this.humanSneakTicks <= 0 && Math.random() < 0.35) {
            this.humanSneakTicks = this.randomTicks(3, 11);
         }

         if (this.delayedAhTicks-- > 0) {
            if (Math.random() < 0.18) {
               mc.field_1690.field_1832.method_23481(true);
            }

            return true;
         } else {
            String command = this.delayedAhCommand;
            this.delayedAhCommand = null;
            this.delayedAhTicks = 0;
            this.sendCommand(command);
            this.waitTicks = Math.max(this.waitTicks, this.randomTicks(8, 22));
            return true;
         }
      }
   }

   private void sendAhCommand(String command) {
      if (command != null && !command.isEmpty()) {
         if (this.delayedAhCommand != null) {
            this.delayedAhCommand = command;
         } else {
            this.delayedAhCommand = command;
            this.delayedAhTicks = this.randomTicks(4, 18);
            this.humanLookTicks = 0;
            this.humanSneakTicks = Math.random() < 0.65 ? this.randomTicks(3, 13) : 0;
         }
      }
   }

   private boolean isAhLikeMode() {
      return this.mode == AuraCrafter.Mode.BUY_OPEN
         || this.mode == AuraCrafter.Mode.BUY_READ
         || this.mode == AuraCrafter.Mode.BUY_CONFIRM
         || this.mode == AuraCrafter.Mode.INVIS_OPEN
         || this.mode == AuraCrafter.Mode.INVIS_READ
         || this.mode == AuraCrafter.Mode.INVIS_CONFIRM
         || this.mode == AuraCrafter.Mode.SELL
         || this.mode == AuraCrafter.Mode.SELL_PREPARE
         || this.mode == AuraCrafter.Mode.SELL_SEND
         || this.mode == AuraCrafter.Mode.STORAGE_OPEN
         || this.mode == AuraCrafter.Mode.STORAGE_CLICK
         || this.mode == AuraCrafter.Mode.STORAGE_TAKE;
   }

   private int findInventorySlot(class_1792 item) {
      for (int i = 0; i < 36; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private int findInventorySlotByName(String name) {
      String needle = name.trim().toLowerCase();

      for (int i = 0; i < 36; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960() && this.clean(stack.method_7964().getString()).contains(needle)) {
            return i;
         }
      }

      return -1;
   }

   private boolean prepareSingleSellItem() {
      String name = this.sellItem.getValue();
      class_1799 hotbar = mc.field_1724.method_31548().method_5438(0);
      if (this.isSellTarget(hotbar) && hotbar.method_7947() == 1) {
         return true;
      } else {
         int syncId = mc.field_1724.field_7512.field_7763;
         int hotbarScreenSlot = this.playerScreenSlot(0);
         if (!hotbar.method_7960()) {
            int emptyScreenSlot = this.findEmptyStorageScreenSlot();
            if (emptyScreenSlot == -1) {
               return false;
            }

            if (this.isSellTarget(hotbar) && hotbar.method_7947() > 1) {
               this.clickSlot(syncId, hotbarScreenSlot, 0, class_1713.field_7790);
               this.clickSlot(syncId, hotbarScreenSlot, 1, class_1713.field_7790);
               this.clickSlot(syncId, emptyScreenSlot, 0, class_1713.field_7790);
               return true;
            }

            this.clickSlot(syncId, hotbarScreenSlot, 0, class_1713.field_7790);
            this.clickSlot(syncId, emptyScreenSlot, 0, class_1713.field_7790);
         }

         int source;
         if ((source = this.findInventorySlotByName(name)) == -1) {
            return false;
         } else {
            class_1799 stack = mc.field_1724.method_31548().method_5438(source);
            if (stack.method_7947() == 1) {
               this.moveToHotbar(source, 0);
               return true;
            } else {
               int sourceScreenSlot = this.playerScreenSlot(source);
               this.clickSlot(syncId, sourceScreenSlot, 0, class_1713.field_7790);
               this.clickSlot(syncId, hotbarScreenSlot, 1, class_1713.field_7790);
               this.clickSlot(syncId, sourceScreenSlot, 0, class_1713.field_7790);
               return true;
            }
         }
      }
   }

   private int findEmptyStorageScreenSlot() {
      for (int i = 9; i < 36; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7960()) {
            return this.playerScreenSlot(i);
         }
      }

      for (int var2 = 1; var2 < 9; var2++) {
         if (mc.field_1724.method_31548().method_5438(var2).method_7960()) {
            return this.playerScreenSlot(var2);
         }
      }

      return -1;
   }

   private void moveToHotbar(int inventorySlot, int hotbarSlot) {
      if (inventorySlot != hotbarSlot) {
         int screenSlot = this.playerScreenSlot(inventorySlot);
         this.clickSlot(mc.field_1724.field_7512.field_7763, screenSlot, hotbarSlot, class_1713.field_7791);
      }
   }

   private int playerScreenSlot(int inventorySlot) {
      return inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
   }

   private int craftingPlayerSlot(int inventorySlot) {
      return inventorySlot < 9 ? inventorySlot + 37 : inventorySlot + 1;
   }

   private long parsePrice(String value, long fallback) {
      String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
      if (digits.isEmpty()) {
         return fallback;
      } else {
         try {
            return Long.parseLong(digits);
         } catch (NumberFormatException var7) {
            return fallback;
         }
      }
   }

   private long nextSellPrice() {
      long base = this.parsePrice(this.sellPrice.getValue(), 1L);
      if (!this.randomSellPrice.getValue()) {
         return Math.max(1L, base);
      } else {
         long range = this.parsePrice(this.randomSellPriceRange.getValue(), 0L);
         if (range <= 0L) {
            return Math.max(1L, base);
         } else {
            long min = Math.max(1L, base - range);
            long max = Math.max(min, base + range);
            long spread = max - min + 1L;
            return min + (long)Math.floor(Math.random() * (double)spread);
         }
      }
   }

   private String sellPriceSuffix() {
      if (!this.randomSellPrice.getValue()) {
         return "";
      } else {
         long range = this.parsePrice(this.randomSellPriceRange.getValue(), 0L);
         return range > 0L ? " ±$" + this.fmt(range) : "";
      }
   }

   private int randomTicks(int min, int max) {
      return max <= min ? min : min + (int)Math.floor(Math.random() * (double)(max - min + 1));
   }

   private float randomFloat(float min, float max) {
      return min + (float)Math.random() * (max - min);
   }

   private double randomDouble(double min, double max) {
      return min + Math.random() * (max - min);
   }

   private void startSessionStats() {
      this.sessionStartMillis = System.currentTimeMillis();
      this.sessionStartBalance = this.balance > 0L ? this.balance : this.readScoreboardMoney();
      this.autoPauseAnarchy = this.readCurrentAnarchy();
      this.playerPauseTicks = 0;
      this.playerPauseReturnTicks = 0;
      this.playerPauseReturning = false;
      this.resumeModeAfterPlayerPause = AuraCrafter.Mode.IDLE;
      this.playerPauseName = null;
      this.lastPlayerPauseName = null;
      this.sessionSellEarned = 0L;
      this.sessionBuySpent = 0L;
      this.sessionClanInvested = 0L;
   }

   private long sessionProfit() {
      return this.sessionStartBalance > 0L && this.balance >= 0L
         ? this.balance - this.sessionStartBalance + this.sessionClanInvested
         : this.sessionSellEarned - this.sessionBuySpent;
   }

   private String sessionElapsedText() {
      long started = this.sessionStartMillis <= 0L ? System.currentTimeMillis() : this.sessionStartMillis;
      long seconds = Math.max(0L, (System.currentTimeMillis() - started) / 1000L);
      long hours = seconds / 3600L;
      long minutes = seconds % 3600L / 60L;
      long secs = seconds % 60L;
      return hours > 0L ? String.format("%d:%02d:%02d", hours, minutes, secs) : String.format("%02d:%02d", minutes, secs);
   }

   private String signedMoney(long value) {
      return (value >= 0L ? "+$" : "-$") + this.fmt(Math.abs(value));
   }

   private String fmt(long value) {
      String raw = Long.toString(Math.max(0L, value));
      StringBuilder out = new StringBuilder(raw);

      for (int i = out.length() - 3; i > 0; i -= 3) {
         out.insert(i, '.');
      }

      return out.toString();
   }

   private void tickWatchdog() {
      if (this.timedMode != this.mode) {
         this.timedMode = this.mode;
         this.stateTicks = 0;
         this.lastProgressSignature = "";
      }

      String signature;
      if (!(signature = this.mode
            + "|s"
            + (mc.field_1755 == null ? "none" : mc.field_1755.getClass().getSimpleName())
            + "|sid"
            + (mc.field_1724 == null ? -1 : mc.field_1724.field_7512.field_7763)
            + "|l"
            + this.listed
            + "|d"
            + this.countInventory(class_1802.field_8477)
            + "|n"
            + this.countInventory(class_1802.field_22020)
            + "|m"
            + this.countInventory(class_1802.field_8103)
            + "|a"
            + this.countSellTargetInventory()
            + "|cmd"
            + this.pendingCommand)
         .equals(this.lastProgressSignature)) {
         this.lastProgressSignature = signature;
         this.stuckTicks = 0;
         this.stateTicks = 0;
      } else {
         this.stateTicks++;
         int stateTimeout = this.modeTimeoutTicks(this.mode);
         if (stateTimeout > 0 && this.stateTicks > stateTimeout) {
            this.stateTicks = 0;
            this.recoverStuckState();
         } else if (++this.stuckTicks >= 1200) {
            this.stuckTicks = 0;
            this.recoverStuckState();
         }
      }
   }

   private int modeTimeoutTicks(AuraCrafter.Mode mode) {
      return switch (mode) {
         case PLAYER_PAUSE -> 0;
         default -> 0;
         case BUY_OPEN, INVIS_OPEN -> 160;
         case BUY_CONFIRM, INVIS_CONFIRM -> 140;
         case CRAFT_OPEN_TABLE -> 140;
         case CRAFT_FILL -> 180;
         case INVIS_DRINK -> 100;
         case SELL_PREPARE, SELL_SEND -> 120;
         case STORAGE_OPEN, STORAGE_CLICK, STORAGE_TAKE -> 220;
      };
   }

   private void recoverStuckState() {
      this.msg("§e[AuraCrafter] Слишком долго без прогресса, перезапускаю этап.");
      switch (this.mode) {
         case PLAYER_PAUSE:
         default:
            break;
         case AUTO_WAIT_JOIN:
            if (this.targetAnarchy > 0) {
               this.sendCommand("an" + this.targetAnarchy);
            }

            this.waitTicks = 200;
            break;
         case AUTO_WAIT_BUDGET:
            this.refreshBalanceNow(true);
            this.waitTicks = 40;
            break;
         case BUY_OPEN:
         case BUY_READ:
         case BUY_CONFIRM:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.buyConfirmClicked = false;
            this.buyConfirmHoldTicks = 0;
            if (this.fullAuto && this.activeBuyItem == class_1802.field_22020 && !this.canBuyNetheriteAboveReserve(0L)) {
               this.handleNoNetheriteBudget();
               return;
            }

            this.sendActiveBuySearch();
            this.mode = AuraCrafter.Mode.BUY_OPEN;
            this.waitTicks = 20;
            break;
         case CRAFT:
         case CRAFT_MILK:
         case CRAFT_OPEN_TABLE:
         case CRAFT_FILL:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.mode = AuraCrafter.Mode.CRAFT;
            this.waitTicks = 20;
            break;
         case INVIS_OPEN:
         case INVIS_READ:
         case INVIS_CONFIRM:
         case INVIS_DRINK:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.releaseInvisUse();
            this.invisConfirmClicked = false;
            this.invisConfirmHoldTicks = 0;
            this.invisUseStarted = false;
            this.invisDrinkTicks = 0;
            this.sendInvisSearch();
            this.mode = AuraCrafter.Mode.INVIS_OPEN;
            this.waitTicks = 20;
            break;
         case SELL_PREPARE:
         case SELL_SEND:
         case SELL:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.mode = AuraCrafter.Mode.SELL;
            this.waitTicks = 10;
            break;
         case STORAGE_OPEN:
         case STORAGE_CLICK:
         case STORAGE_TAKE:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            if (this.storageTookSellLot) {
               this.listed = 0;
               this.mode = AuraCrafter.Mode.SELL;
               this.noScreenTicks = 0;
               this.waitTicks = 10;
            } else {
               this.startStorageRelist();
            }
      }
   }

   private void tickClanInvest() {
      if (this.investCooldownTicks > 0) {
         this.investCooldownTicks--;
      } else if (this.clanInvest.getValue() && mc.field_1755 == null) {
         long currentBalance = this.readScoreboardMoney();
         if (currentBalance > 0L) {
            this.balance = currentBalance;
         }

         if (this.balance > 0L) {
            long threshold = this.parsePrice(this.clanInvestAt.getValue(), Long.MAX_VALUE);
            long amount = this.parsePrice(this.clanInvestAmount.getValue(), 0L);
            if (amount > 0L && this.balance >= threshold && this.balance - amount >= 0L) {
               this.sendCommand("clan invest " + amount);
               this.sessionClanInvested += amount;
               this.balance = Math.max(0L, this.balance - amount);
               this.investCooldownTicks = 1200;
            }
         }
      }
   }

   private void refreshBalanceNow(boolean forceCommand) {
      long currentBalance;
      if (!forceCommand && this.balance <= 0L && (currentBalance = this.readScoreboardMoney()) > 0L) {
         this.balance = currentBalance;
         this.balanceRefreshTicks = 0;
      }

      if (forceCommand || ++this.balanceRefreshTicks >= 55 + this.randomTicks(0, 18)) {
         this.sendCommand("money");
         this.balanceRefreshTicks = 0;
      }
   }

   private void refundReservedBuyBalance() {
      if (this.balanceReservedForConfirm) {
         if (this.balance >= 0L && this.selectedBuyPrice > 0L) {
            this.balance = this.balance + this.selectedBuyPrice;
         }

         this.balanceReservedForConfirm = false;
         this.selectedBuyPrice = 0L;
      }
   }

   private boolean isBuyFailedMessage(String lower) {
      return lower.contains("не удалось купить")
         || lower.contains("уже куп")
         || lower.contains("куплен")
         || lower.contains("истек")
         || lower.contains("нет в наличии");
   }

   private boolean isAuctionBalanceSellBlocked(String lower) {
      return lower.contains("баланс") && lower.contains("аукцион") && lower.contains("монет");
   }

   private long extractDollarPrice(String text) {
      if (text == null) {
         return 0L;
      } else {
         int dollar = text.indexOf(36);
         if (dollar < 0) {
            return 0L;
         } else {
            String tail = text.substring(dollar).replaceAll("[^0-9]", "");
            return this.parsePrice(tail, 0L);
         }
      }
   }

   private long readScoreboardMoney() {
      if (mc.field_1687 == null) {
         return -1L;
      } else {
         class_269 scoreboard = mc.field_1687.method_8428();
         class_266 objective = scoreboard.method_1189(class_8646.field_45157);
         if (objective == null) {
            return -1L;
         } else {
            long titleMoney = this.parseMoneyLine(objective.method_1114().getString());
            if (titleMoney > 0L) {
               return titleMoney;
            } else {
               for (class_9011 entry : scoreboard.method_1184(objective)) {
                  String line = class_268.method_1142(scoreboard.method_1164(entry.comp_2127()), entry.method_55387()).getString();
                  long money = this.parseMoneyLine(line);
                  if (money > 0L) {
                     return money;
                  }
               }

               return -1L;
            }
         }
      }
   }

   private int readCurrentAnarchy() {
      int scoreboard = this.readScoreboardAnarchy();
      if (scoreboard > 0) {
         return scoreboard;
      } else {
         int cached = ServerUtil.getAnarchy();
         return cached > 0 ? cached : -1;
      }
   }

   private int readScoreboardAnarchy() {
      if (mc.field_1687 == null) {
         return -1;
      } else {
         class_269 scoreboard = mc.field_1687.method_8428();
         class_266 objective = scoreboard.method_1189(class_8646.field_45157);
         if (objective == null) {
            return -1;
         } else {
            int title = this.parseAnarchyLine(objective.method_1114().getString());
            if (title > 0) {
               return title;
            } else {
               for (class_9011 entry : scoreboard.method_1184(objective)) {
                  String line = class_268.method_1142(scoreboard.method_1164(entry.comp_2127()), entry.method_55387()).getString();
                  int anarchy = this.parseAnarchyLine(line);
                  if (anarchy > 0) {
                     return anarchy;
                  }
               }

               return -1;
            }
         }
      }
   }

   private int parseAnarchyLine(String line) {
      String text = this.clean(line);
      int index = text.indexOf("анархия");
      if (index < 0) {
         return -1;
      } else {
         String tail = text.substring(index).replaceAll("[^0-9]", "");
         return (int)this.parsePrice(tail, -1L);
      }
   }

   private long parseMoneyLine(String line) {
      String text = this.clean(line);
      return !text.contains("монет") && !text.contains("баланс") ? -1L : this.parsePrice(text, -1L);
   }

   private void finishAutoBuy() {
      this.normalizeActiveBuyState();
      if (!this.fullAuto) {
         this.reset();
      } else {
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }

         if (this.resumeCraftAfterDiamondBuy) {
            this.resumeCraftAfterDiamondBuy = false;
            this.activeBuyName = null;
            this.activeBuyItem = null;
            this.activeBuyTarget = 0;
            this.activeMaxUnitPrice = -1L;
            this.returnHomeForCraft();
         } else if (this.activeBuyItem == class_1802.field_8477) {
            this.beginNetheriteBuy();
         } else {
            this.returnHomeForCraft();
         }
      }
   }

   private void returnHomeForCraft() {
      this.allowAwayAnarchy = false;
      if (this.homeAnarchy <= 0) {
         this.homeAnarchy = this.readCurrentAnarchy();
      }

      int current = this.readCurrentAnarchy();
      if (this.homeAnarchy > 0 && current != this.homeAnarchy) {
         this.joinAnarchy(this.homeAnarchy, AuraCrafter.JoinAction.CRAFT);
      } else {
         this.mode = AuraCrafter.Mode.CRAFT;
         this.waitTicks = 6;
      }
   }

   private void startAutoSellOrBuy() {
      if (this.countSellTargetInventory() <= 0 && this.listed <= 0) {
         this.beginAutoCycle();
      } else {
         this.mode = AuraCrafter.Mode.SELL;
         this.waitTicks = 6;
      }
   }

   private int countSellTargetInventory() {
      int count = 0;

      for (int i = 0; i < 36; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (this.isSellTarget(stack)) {
            count += stack.method_7947();
         }
      }

      return count;
   }

   private void switchBuyAnarchy() {
      int next = this.nextBuyAnarchy();
      if (next <= 0) {
         this.buySearchTicks = 0;
         this.clickRefreshIfOpen();
      } else {
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }

         this.allowAwayAnarchy = true;
         this.msg("§e[AuraCrafter] Нету подходящих лотов, меняю анку на " + next + ".");
         this.joinAnarchy(next, AuraCrafter.JoinAction.SEARCH);
      }
   }

   private int nextBuyAnarchy() {
      String[] parts = this.anarchyList.getValue().split("[,; ]+");

      for (int attempts = 0; attempts < parts.length; attempts++) {
         int value;
         String part;
         if (!(part = parts[this.anarchyIndex++ % Math.max(1, parts.length)].replaceAll("[^0-9]", "")).isEmpty()
            && (value = (int)this.parsePrice(part, -1L)) > 0
            && value != this.readScoreboardAnarchy()) {
            return value;
         }
      }

      return -1;
   }

   private void clickRefreshIfOpen() {
      if (mc.field_1755 instanceof class_476 screen) {
         this.clickRefresh((class_1707)screen.method_17577());
      }

      this.waitTicks = 20;
   }

   private boolean guardAnarchy() {
      if (this.homeAnarchy > 0 && !this.allowAwayAnarchy && this.mode != AuraCrafter.Mode.AUTO_WAIT_JOIN) {
         int current = this.readCurrentAnarchy();
         if (current == this.homeAnarchy) {
            return false;
         } else {
            this.rejoinHome();
            return true;
         }
      } else {
         return false;
      }
   }

   private void rejoinHome() {
      if (this.homeAnarchy <= 0) {
         this.homeAnarchy = this.readCurrentAnarchy();
      }

      if (this.homeAnarchy > 0) {
         this.joinAnarchy(this.homeAnarchy, AuraCrafter.JoinAction.CRAFT);
      }
   }

   private void joinAnarchy(int anarchy, AuraCrafter.JoinAction action) {
      this.targetAnarchy = anarchy;
      this.joinAction = action;
      this.buySearchTicks = 0;
      this.noScreenTicks = 0;
      this.sendCommand("an" + anarchy);
      this.mode = AuraCrafter.Mode.AUTO_WAIT_JOIN;
      this.waitTicks = 200;
   }

   private void tickAntiAfkCommand() {
      if (this.pendingCommand != null) {
         this.antiAfkTicks++;
         mc.field_1690.field_1894.method_23481(this.antiAfkTicks < 10);
         mc.field_1690.field_1913.method_23481(this.antiAfkTicks >= 10 && this.antiAfkTicks < 22);
         mc.field_1690.field_1849.method_23481(this.antiAfkTicks >= 22 && this.antiAfkTicks < 34);
         mc.field_1690.field_1903.method_23481(this.antiAfkTicks == 8 || this.antiAfkTicks == 24);
         if (this.antiAfkTicks >= 38) {
            this.releaseAntiAfkKeys();
            this.lastCommand = this.pendingCommand;
            mc.field_1724.field_3944.method_45730(this.pendingCommand.startsWith("/") ? this.pendingCommand.substring(1) : this.pendingCommand);
            this.pendingCommand = null;
            this.afkBlocked = false;
            this.antiAfkTicks = 0;
            this.antiAfkStage = 0;
         }
      }
   }

   private void wakeForCommand(String command) {
      if (command == null || command.isEmpty()) {
         command = this.lastCommand;
      }

      if (command != null && !command.isEmpty()) {
         if (!command.equals(this.pendingCommand)) {
            this.pendingCommand = command;
            this.antiAfkTicks = 0;
            this.antiAfkStage++;
            this.releaseAntiAfkKeys();
         }
      }
   }

   private void releaseAntiAfkKeys() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1913.method_23481(false);
         mc.field_1690.field_1849.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
         mc.field_1690.field_1832.method_23481(false);
      }
   }

   private String modeName() {
      if (this.mode == AuraCrafter.Mode.PLAYER_PAUSE) {
         return "пауза игрок";
      } else {
         return switch (this.mode) {
            case IDLE -> "ожидает бинда";
            case PLAYER_PAUSE -> "пауза игрок";
            case AUTO_WAIT_JOIN -> "ждет анку";
            case AUTO_WAIT_BUDGET -> "ждет баланс";
            case BUY_OPEN, BUY_READ, BUY_CONFIRM -> "закупает";
            case CRAFT, CRAFT_MILK, CRAFT_OPEN_TABLE, CRAFT_FILL -> "крафтит";
            case INVIS_OPEN, INVIS_READ, INVIS_CONFIRM, INVIS_DRINK -> "инвиз";
            case SELL_PREPARE, SELL_SEND, SELL -> "продает";
            case STORAGE_OPEN, STORAGE_CLICK, STORAGE_TAKE -> "перевыставляет";
         };
      }
   }

   private boolean isSellTarget(class_1799 stack) {
      return !stack.method_7960() && this.clean(stack.method_7964().getString()).contains(this.sellItem.getValue().trim().toLowerCase());
   }

   private String stackText(class_1799 stack) {
      StringBuilder text = new StringBuilder(this.clean(stack.method_7964().getString()));
      class_9290 lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632);
      if (lore != null) {
         for (class_2561 line : lore.comp_2400()) {
            text.append(' ').append(this.clean(line.getString()));
         }
      }

      return text.toString();
   }

   private String clean(String text) {
      return text == null ? "" : text.replaceAll("§.", "").trim().toLowerCase();
   }

   private void sendCommand(String command) {
      if (command != null && !command.isEmpty()) {
         this.lastCommand = command;
         if (this.afkBlocked) {
            this.wakeForCommand(command);
         } else {
            mc.field_1724.field_3944.method_45730(command.startsWith("/") ? command.substring(1) : command);
         }
      }
   }

   private void stop(String message) {
      this.msg(message);
      this.reset();
   }

   private void reset() {
      this.mode = AuraCrafter.Mode.IDLE;
      this.waitTicks = 0;
      this.listed = 0;
      this.sellAttemptsLeft = 0;
      this.lastScreenSyncId = -1;
      this.noScreenTicks = 0;
      this.fullSellTicks = 0;
      this.fullAuto = false;
      this.activeBuyName = null;
      this.activeMaxUnitPrice = -1L;
      this.activeBuyItem = null;
      this.activeBuyTarget = 0;
      this.buySearchTicks = 0;
      this.buyOpenTicks = 0;
      this.ignoredSearchScreenSyncId = -1;
      this.balanceRefreshTicks = 0;
      this.homeAnarchy = -1;
      this.targetAnarchy = -1;
      this.autoPauseAnarchy = -1;
      this.playerPauseTicks = 0;
      this.playerPauseReturnTicks = 0;
      this.playerPauseReturning = false;
      this.resumeModeAfterPlayerPause = AuraCrafter.Mode.IDLE;
      this.playerPauseName = null;
      this.lastPlayerPauseName = null;
      this.anarchyIndex = 0;
      this.joinAction = AuraCrafter.JoinAction.NONE;
      this.allowAwayAnarchy = false;
      this.antiAfkTicks = 0;
      this.pendingCommand = null;
      this.lastCommand = null;
      this.antiAfkStage = 0;
      this.buyConfirmClicked = false;
      this.buyConfirmHoldTicks = 0;
      this.selectedBuyPrice = 0L;
      this.balanceReservedForConfirm = false;
      this.afkBlocked = false;
      this.lastProgressSignature = "";
      this.stuckTicks = 0;
      this.timedMode = AuraCrafter.Mode.IDLE;
      this.stateTicks = 0;
      this.buyRejectReason = "";
      this.resumeCraftAfterDiamondBuy = false;
      this.investCooldownTicks = 0;
      this.storageTookSellLot = false;
      this.checkedStorageForSell = false;
      this.emptyStorageChecks = 0;
      this.storageTakeClicks = 0;
      this.autoInvisCheckTicks = 0;
      this.resumeModeAfterInvis = AuraCrafter.Mode.IDLE;
      this.resumeWaitAfterInvis = 0;
      this.invisOpenTicks = 0;
      this.invisConfirmClicked = false;
      this.invisConfirmHoldTicks = 0;
      this.invisUseStarted = false;
      this.invisDrinkTicks = 0;
      this.invisPreviousSlot = -1;
      this.humanMoveTicks = 0;
      this.nextHumanMoveTicks = 0;
      this.humanLookTicks = 0;
      this.humanSneakTicks = 0;
      this.delayedAhCommand = null;
      this.delayedAhTicks = 0;
      this.inventoryActionTicks = 0;
      this.tableAimTicks = 0;
      this.cowAimTicks = 0;
      this.releaseInvisUse();
      this.releaseHumanMovementKeys();
      this.releaseAntiAfkKeys();
   }

   private void msg(String text) {
      ChatUtils.addChatMessage(text);
   }

   @Environment(EnvType.CLIENT)
   private static enum JoinAction {
      NONE,
      SEARCH,
      CRAFT;
   }

   @Environment(EnvType.CLIENT)
   private static enum Mode {
      IDLE,
      PLAYER_PAUSE,
      AUTO_WAIT_JOIN,
      AUTO_WAIT_BUDGET,
      BUY_OPEN,
      BUY_READ,
      BUY_CONFIRM,
      CRAFT,
      CRAFT_MILK,
      CRAFT_OPEN_TABLE,
      CRAFT_FILL,
      INVIS_OPEN,
      INVIS_READ,
      INVIS_CONFIRM,
      INVIS_DRINK,
      SELL_PREPARE,
      SELL_SEND,
      STORAGE_OPEN,
      STORAGE_CLICK,
      STORAGE_TAKE,
      SELL;
   }
}
