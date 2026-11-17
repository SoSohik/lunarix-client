package ru.mentions.module.impl.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_10192;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1304;
import net.minecraft.class_1531;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1743;
import net.minecraft.class_1753;
import net.minecraft.class_1794;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1821;
import net.minecraft.class_1826;
import net.minecraft.class_1844;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2551;
import net.minecraft.class_2625;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_2680;
import net.minecraft.class_269;
import net.minecraft.class_2745;
import net.minecraft.class_2818;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4174;
import net.minecraft.class_418;
import net.minecraft.class_490;
import net.minecraft.class_6880;
import net.minecraft.class_7260;
import net.minecraft.class_7439;
import net.minecraft.class_7715;
import net.minecraft.class_7923;
import net.minecraft.class_8242;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import net.minecraft.class_9334;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mentions.manager.event_impl.EventMoveInput;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.mixin.BossBarHudAccessor;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.render.WorldRenderBatcher;

@ModuleInfo(
   name = "AutoWarden",
   desc = "Находит и лутает сундуки города варденов по таймерам",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public final class AutoWarden extends Module implements IMinecraft {
   private static final Logger LOGGER = LoggerFactory.getLogger("mentions/AutoWarden");
   private final SliderSetting chestsBeforeHome = new SliderSetting(this, "Сундуков до дома", 5.0F, 1.0F, 64.0F, 1.0F);
   private final StringSetting farmAnarchies = new StringSetting(this, "Анархии фарма", "");
   private final StringSetting supplyAnarchy = new StringSetting(this, "Анархия снабжения", "");
   private final BooleanSetting memoryTimerHud = new BooleanSetting(this, "HUD таймеров сундуков", true);
   private final BooleanSetting autoSupplies = new BooleanSetting(this, "Авто-снабжение", true);
   private final StringSetting supplySign = new StringSetting(this, "Табличка снабжения", "снабжение").setVisible(() -> this.autoSupplies.getValue());
   private final BooleanSetting takeFood = new BooleanSetting(this, "Брать еду", true).setVisible(() -> this.autoSupplies.getValue());
   private final SliderSetting foodAmount = new SliderSetting(this, "Количество еды", 16.0F, 1.0F, 64.0F, 1.0F)
      .setVisible(() -> this.autoSupplies.getValue() && this.takeFood.getValue());
   private final BooleanSetting takeInvisibility = new BooleanSetting(this, "Брать невидимость", true).setVisible(() -> this.autoSupplies.getValue());
   private final SliderSetting invisibilityAmount = new SliderSetting(this, "Зелий невидимости", 3.0F, 1.0F, 16.0F, 1.0F)
      .setVisible(() -> this.autoSupplies.getValue() && this.takeInvisibility.getValue());
   private final BooleanSetting takeSpeed = new BooleanSetting(this, "Брать скорость", true).setVisible(() -> this.autoSupplies.getValue());
   private final SliderSetting speedAmount = new SliderSetting(this, "Зелий скорости", 3.0F, 1.0F, 16.0F, 1.0F)
      .setVisible(() -> this.autoSupplies.getValue() && this.takeSpeed.getValue());
   private static final int CITY_MIN_X = -2070;
   private static final int CITY_MAX_X = -1921;
   private static final int CITY_MIN_Y = -60;
   private static final int CITY_MAX_Y = -35;
   private static final int CITY_MIN_Z = -2076;
   private static final int CITY_MAX_Z = -1929;
   private static final int WARDEN_MEMORY_TICKS = 100;
   private static final double WARDEN_TARGET_SCAN_DISTANCE = 64.0;
   private static final double WARDEN_FLEE_CLEAR_DISTANCE = 30.0;
   private static final int WARDEN_TARGET_LOST_CONFIRM_TICKS = 20;
   private static final int WARDEN_FLEE_MIN_STEPS = 9;
   private static final int WARDEN_FLEE_MAX_STEPS = 26;
   private static final int WARDEN_FLEE_REPATH_TICKS = 12;
   private static final int FAILED_CHEST_TICKS = 200;
   private static final int OPEN_TIMEOUT_TICKS = 24;
   private static final int LOOT_TIMEOUT_TICKS = 100;
   private static final int LOOT_CLICK_DELAY_TICKS = 3;
   private static final int FARM_LOOT_CLICK_DELAY_TICKS = 1;
   private static final long EARLY_CHEST_OPEN_LEAD_MS = 2000L;
   private static final int EARLY_CHEST_REFRESH_GRACE_TICKS = 40;
   private static final int HOME_RETRY_TICKS = 200;
   private static final int HOME_CHEST_SEARCH_RADIUS = 2;
   private static final int SUPPLY_CHEST_SEARCH_RADIUS = 40;
   private static final int CONTAINER_RETRY_TICKS = 12;
   private static final int HOME_SEARCH_RETRY_TICKS = 120;
   private static final int PVP_CLEAR_CONFIRM_TICKS = 20;
   private static final int EMPTY_HAND_HOTBAR_SLOT = 8;
   private static final int HOTBAR_CLEANUP_DELAY_TICKS = 3;
   private static final int IMMEDIATE_DISCARD_SETTLE_TICKS = 3;
   private static final int IMMEDIATE_DISCARD_RELEASE_TICKS = 1;
   private static final int PATROL_MIN_DISTANCE = 10;
   private static final int PATROL_MAX_DISTANCE = 24;
   private static final int PATROL_FAILURE_RETRY_TICKS = 40;
   private static final int NO_TARGET_ANARCHY_SWITCH_TICKS = 100;
   private static final int NO_TIMER_ANARCHY_SWITCH_TICKS = 20;
   private static final int FARM_EFFECT_SYNC_GRACE_TICKS = 200;
   private static final int EMERGENCY_RESTOCK_CONFIRM_TICKS = 20;
   private static final int STUCK_ROUTE_FAILURE_LIMIT = 8;
   private static final int TIMED_ROUTE_RECOVERY_TICKS = 400;
   private static final int TIMED_CHEST_PREEMPT_BACKOFF_TICKS = 80;
   private static final int DISCOVERY_ROUTE_FAILURE_BACKOFF_TICKS = 240;
   private static final int DISCOVERY_NO_GOAL_LIMIT = 3;
   private static final double STUCK_ROUTE_FAILURE_RADIUS_SQ = 4.0;
   private static final double INTERACTION_REACH_SQ = 18.0625;
   private static final long REMEMBERED_LOOT_COOLDOWN_MS = 660000L;
   private static final long REMEMBERED_CHEST_HORIZON_MS = 45000L;
   private static final long FARM_TRANSFER_BASE_LEAD_MS = 15000L;
   private static final long LOCAL_CHEST_PREP_MS = 15000L;
   private static final double ESTIMATED_WALK_BLOCKS_PER_SECOND = 3.6;
   private static final long MAX_ESTIMATED_ARRIVAL_LEAD_MS = 90000L;
   private static final float CHEST_AIM_SPEED_MULTIPLIER = 2.0F;
   private static final double GROUND_LOOT_SCAN_DISTANCE = 18.0;
   private static final int GROUND_LOOT_TIMEOUT_TICKS = 120;
   private static final int GROUND_LOOT_REPATH_TICKS = 10;
   private static final int GROUND_LOOT_RETRY_TICKS = 200;
   private static final int PATH_GLOW_COLOR = 1342226431;
   private static final int PATH_COLOR = -16721409;
   private static final int PATH_NODE_COLOR = -794233601;
   private static final int PATH_GOAL_COLOR = -11713;
   private static final Pattern TIMER_PATTERN = Pattern.compile("(\\d{2}):(\\d{2})");
   private static final Pattern ANARCHY_PATTERN = Pattern.compile("(?:анархия|anarchy)\\s*[-:#№]?\\s*(\\d+)", 66);
   private final AutoWardenNavigator navigator = new AutoWardenNavigator(mc);
   private final AutoWardenSequenceTracker sequenceTracker = new AutoWardenSequenceTracker();
   private final AutoWardenLootTracker lootTracker = new AutoWardenLootTracker();
   private final Map<AutoWarden.ChestKey, AutoWarden.ChestTimer> chestTimers = new HashMap<>();
   private final Map<AutoWarden.ChestKey, Integer> openAttempts = new HashMap<>();
   private final Map<AutoWarden.ChestKey, Integer> skippedUntilAge = new HashMap<>();
   private final Map<AutoWarden.ChestKey, Integer> discoverySkippedUntilAge = new HashMap<>();
   private final Map<class_2338, Integer> recentWardens = new HashMap<>();
   private boolean fleeingWarden;
   private class_2338 wardenFleeThreatPos;
   private int wardenFleeRepathTicks;
   private int wardenFleeClearTicks;
   private AutoWarden.CycleState state = AutoWarden.CycleState.SEARCHING;
   private Object worldIdentity;
   private String currentAnarchy = "?";
   private AutoWarden.ChestKey targetKey;
   private class_2338 targetChest;
   private class_2338 movementGoal;
   private int targetSelectedAge;
   private int stateTicks;
   private int actionDelay;
   private boolean automationContainerOpened;
   private boolean interactionSent;
   private boolean currentChestLootTaken;
   private boolean currentChestLootSnapshotRecorded;
   private int emptyChestHoldUntilAge = -1;
   private int lootedChests;
   private boolean returnHomePending;
   private class_243 homeCommandPos;
   private Object homeCommandWorld;
   private boolean homeTeleportConfirmed;
   private AutoWarden.HomePurpose homePurpose = AutoWarden.HomePurpose.STORAGE;
   private String expectedAnarchy;
   private String deferredAnarchy;
   private Boolean deferredStorageTransfer;
   private AutoWarden.HomePurpose deferredHomePurpose;
   private int pvpClearTicks;
   private String farmAnarchyBeforeStorage;
   private boolean initialFarmRoutePending;
   private boolean initialSupplyPending;
   private boolean serverDeathMessagePending;
   private boolean deathRecoveryPending;
   private String deathResumeAnarchy;
   private int outsideCityTicks;
   private int noTargetTicks;
   private int farmEffectSyncGraceTicks;
   private int emergencyRestockTicks;
   private final Map<String, Long> rememberedInvisibilityUntilByAnarchy = new HashMap<>();
   private final Map<String, Long> rememberedSpeedUntilByAnarchy = new HashMap<>();
   private boolean initialEffectSyncPending;
   private boolean deathPotionRecovery;
   private int searchPatrolIndex;
   private class_2338 routeFailureAnchor;
   private AutoWarden.ChestKey routeFailureTarget;
   private int routeFailuresAtAnchor;
   private class_2338 timedRouteRecoveryChest;
   private int timedRouteRecoveryUntilAge;
   private int timedRecoveryNoPatrolAttempts;
   private class_2338 discoveryChest;
   private int discoveryNoGoalAttempts;
   private class_2338 homeChest;
   private class_2338 supplyChest;
   private int depositSlot;
   private int scanDelay;
   private int repathDelay;
   private final Set<class_2338> exhaustedHomeChests = new HashSet<>();
   private final Set<class_2338> exhaustedSupplyChests = new HashSet<>();
   private final EnumSet<AutoWarden.SupplyKind> unavailableSupplyKinds = EnumSet.noneOf(AutoWarden.SupplyKind.class);
   private AutoWarden.SupplyKind activeConsumable;
   private int consumablePreviousSlot = -1;
   private int consumableHotbarSlot = -1;
   private int consumableUseDelay;
   private int consumableTicks;
   private int consumableInteractCooldown;
   private int consumableReleaseTicks;
   private final class_1799[] lootHotbarSnapshot = new class_1799[9];
   private final class_1799[] ignoredLooseHotbarStacks = new class_1799[8];
   private final Set<Integer> pickedHotbarSlots = new HashSet<>();
   private boolean lootHotbarSnapshotActive;
   private boolean hotbarCleanupPending;
   private boolean automationInventoryOpened;
   private int hotbarCleanupDelay;
   private int immediateDiscardCooldown;
   private int immediateDiscardSettleTicks;
   private boolean immediateDiscardStopping;
   private int screenResumeDelay;
   private UUID groundLootTargetId;
   private class_2338 groundLootLastPos;
   private class_1799 groundLootExpectedStack = class_1799.field_8037;
   private int groundLootInventoryCountBefore;
   private int groundLootStartedAge;
   private int groundLootRepathAge;
   private final Map<UUID, Integer> ignoredGroundLootUntilAge = new HashMap<>();
   private class_2338 preciseAimBlock;
   private boolean preciseAimInitialized;
   private int preciseAimProfileTicks;
   private float preciseAimYawVelocity;
   private float preciseAimPitchVelocity;
   private float preciseAimYawSpeed = 12.0F;
   private float preciseAimPitchSpeed = 9.0F;
   private float preciseAimYawSpeedTarget = 12.0F;
   private float preciseAimPitchSpeedTarget = 9.0F;
   private double preciseAimOffsetX;
   private double preciseAimOffsetY;
   private double preciseAimOffsetZ;
   private double preciseAimOffsetXTarget;
   private double preciseAimOffsetYTarget;
   private double preciseAimOffsetZTarget;

   private void suppressSprint() {
   }

   private List<String> parseAnarchies() {
      String var1 = this.farmAnarchies.getValue();
      if (var1 != null && !var1.isBlank()) {
         ArrayList<String> var2 = new ArrayList<>();

         for (String var4 : var1.split(",")) {
            String var5 = var4.trim();
            if (!var5.isEmpty()) {
               var2.add(var5);
            }
         }

         return var2;
      } else {
         return List.of();
      }
   }

   @Override
   protected void onEnable() {
      this.collectClearTimers(true);
      this.sequenceTracker.beginSession();
      super.onEnable();
   }

   @Override
   protected void onDisable() {
      this.findWorld3();
      this.logWorld3();
      this.findWorld();
      this.findWorld7();
      this.navigator.stop();
      this.collectWorld();
      this.sequenceTracker.endSession();
      super.onDisable();
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (this.serverDeathMessagePending) {
            this.serverDeathMessagePending = false;
            this.logPlayer12();
         } else {
            int var2 = !(mc.field_1755 instanceof class_418)
                  && !mc.field_1724.method_29504()
                  && !(mc.field_1724.method_6032() <= 0.0F)
                  && mc.field_1724.field_6213 <= 0
               ? 0
               : 1;
            if (var2 != 0) {
               this.logPlayer12();
            } else if (this.deathRecoveryPending) {
               this.worldIdentity = mc.field_1687;
               this.logWorld4();
               this.logWorld5();
            } else {
               if (this.worldIdentity != mc.field_1687) {
                  if (this.isList3()) {
                     this.navigator.stop();
                     this.automationContainerOpened = false;
                     this.collectWorld();
                     this.worldIdentity = mc.field_1687;
                  } else {
                     this.collectClearTimers(false);
                     this.worldIdentity = mc.field_1687;
                  }
               }

               if (this.actionDelay > 0) {
                  this.actionDelay = this.actionDelay + -2 + 1;
               }

               if (this.scanDelay > 0) {
                  this.scanDelay = this.scanDelay + -2 + 1;
               }

               if (this.repathDelay > 0) {
                  this.repathDelay = this.repathDelay + -2 + 1;
               }

               if (this.farmEffectSyncGraceTicks > 0) {
                  this.farmEffectSyncGraceTicks = this.farmEffectSyncGraceTicks + -2 + 1;
               }

               this.stateTicks = this.stateTicks - -2 - 1;
               this.logWorld4();
               this.calculateStack2();
               if (this.isList2() || this.state == AutoWarden.CycleState.PVP_WAIT) {
                  this.logWorld2();
               }

               if (this.isList2()) {
                  this.findChests(this.findWorld2());
               }

               this.skippedUntilAge.entrySet().removeIf(entry -> mc.field_1724.field_6012 >= entry.getValue());
               this.discoverySkippedUntilAge.entrySet().removeIf(entry -> mc.field_1724.field_6012 < entry.getValue());
               if (!this.isWorld5()) {
                  this.logPlayer11();
                  if (this.isPlayer()) {
                     this.navigator.pauseMovement();
                  } else {
                     if (this.isList2()) {
                        this.logPlayer();
                     }

                     this.logPlayer16();
                     if (this.isStack()) {
                        this.navigator.pauseMovement();
                     } else if (mc.field_1724.field_7512 instanceof class_1707 var3) {
                        if (!this.isList()) {
                           this.navigator.pauseMovement();
                        } else {
                           if (!this.automationContainerOpened) {
                              this.stateTicks = 0;
                              this.actionDelay = 0;
                              this.interactionSent = false;
                              if ((this.state == AutoWarden.CycleState.OPENING || this.state == AutoWarden.CycleState.LOOTING) && this.targetChest != null) {
                                 this.sequenceTracker.recordEvent("container_opened", this.currentAnarchy, this.targetChest, this.logKey(this.targetKey));
                              }
                           }

                           this.automationContainerOpened = true;
                           this.navigator.pauseMovement();
                           switch (this.state) {
                              case OPEN_DEPOSIT:
                              case DEPOSIT:
                                 this.state = AutoWarden.CycleState.DEPOSIT;
                                 this.calculateHandler3(var3);
                                 break;
                              case FIND_SUPPLY:
                              case WALK_SUPPLY:
                              default:
                                 this.state = AutoWarden.CycleState.LOOTING;
                                 this.updateHandler(var3);
                                 break;
                              case OPEN_SUPPLY:
                              case TAKE_SUPPLY:
                                 this.state = AutoWarden.CycleState.TAKE_SUPPLY;
                                 this.logHandler(var3);
                           }
                        }
                     } else if (this.automationContainerOpened) {
                        this.automationContainerOpened = false;
                        if (this.state == AutoWarden.CycleState.LOOTING) {
                           this.logPlayer4();
                        } else if (this.state == AutoWarden.CycleState.DEPOSIT) {
                           this.homeChest = null;
                           this.state = AutoWarden.CycleState.FIND_DEPOSIT;
                           this.stateTicks = 0;
                        } else if (this.state == AutoWarden.CycleState.TAKE_SUPPLY) {
                           this.supplyChest = null;
                           this.state = AutoWarden.CycleState.FIND_SUPPLY;
                           this.stateTicks = 0;
                        }
                     } else if (mc.field_1755 != null) {
                        if (this.activeConsumable != null) {
                           this.findWorld3();
                        }

                        this.navigator.pauseMovement();
                     } else if (this.screenResumeDelay > 0) {
                        this.screenResumeDelay = this.screenResumeDelay + -2 + 1;
                        this.navigator.pauseMovement();
                     } else if (this.isList2() && this.state != AutoWarden.CycleState.OPENING && this.isPlayer5()) {
                        this.navigator.pauseMovement();
                     } else {
                        if (!this.isList2() || this.interactionSent) {
                           this.emergencyRestockTicks = 0;
                        } else if (this.farmEffectSyncGraceTicks <= 0 && this.isPlayer7()) {
                           this.emergencyRestockTicks = this.emergencyRestockTicks - -2 - 1;
                           if (this.emergencyRestockTicks >= 20) {
                              LOGGER.info("Configured farm supplies stayed depleted for {} ticks; returning home before continuing", this.emergencyRestockTicks);
                              this.emergencyRestockTicks = 0;
                              this.logAdvanceFarmAnarchy(false, this.logList2());
                              return;
                           }
                        } else {
                           this.emergencyRestockTicks = 0;
                        }

                        if (!this.isWorld()) {
                           this.navigator.tick();
                           if (this.navigator.hasArrived()) {
                              this.logPlayer3();
                           }

                           if (this.navigator.hasFailed()) {
                              this.logPlayer15();
                           } else if (this.isList3()) {
                              this.logWorld6();
                           } else if (!this.isPlayer3() && !this.isPlayer9()) {
                              if (this.returnHomePending) {
                                 LOGGER.info(
                                    "Reached configured chest limit {}; returning home immediately", Math.max(1, Math.round(this.chestsBeforeHome.getValue()))
                                 );
                                 this.logAdvanceFarmAnarchy2(true);
                              } else if (!this.isWorld2()) {
                                 this.navigator.stop();
                                 this.collectWorld();
                                 this.state = AutoWarden.CycleState.SEARCHING;
                                 this.outsideCityTicks = this.outsideCityTicks - -2 - 1;
                                 if (this.outsideCityTicks >= 40) {
                                    LOGGER.warn("Farm /home did not place AutoWarden in the city at {}; retrying /home", mc.field_1724.method_24515());
                                    this.outsideCityTicks = 0;
                                    this.logPurpose(AutoWarden.HomePurpose.FARM_RETURN);
                                 }
                              } else {
                                 this.outsideCityTicks = 0;
                                 this.logPlayer17();
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
   public void onPacket(EventPacket event) {
      if (!event.isSend() && event.getPacket() instanceof class_7439 var2) {
         String var5 = var2.comp_763().getString();
         if (var5 != null && !var5.isBlank()) {
            String var4 = var5.toLowerCase(Locale.ROOT);
            if (var4.contains("вы погибли")) {
               this.serverDeathMessagePending = true;
               LOGGER.info("Queued AutoWarden death recovery from server chat");
            }

            if (this.isMessage(var4)) {
               this.homeTeleportConfirmed = true;
               LOGGER.info("AutoWarden /home teleport confirmed by server chat");
            }
         }
      }
   }

   @EventHandler
   public void onMoveInput(EventMoveInput event) {
      if (this.isEnabled()
         && (
            this.automationContainerOpened
               || this.immediateDiscardStopping
               || this.activeConsumable != null
               || this.consumableReleaseTicks > 0
               || this.screenResumeDelay > 0
               || mc.field_1755 != null
         )) {
         event.setForward(0.0F);
         event.setStrafe(0.0F);
         event.setInput(class_10185.field_54098);
         if (mc.field_1724 != null) {
            mc.field_1724.method_5728(false);
            if (mc.field_1724.field_3913 != null) {
               mc.field_1724.field_3913.field_54155 = class_10185.field_54098;
            }
         }

         this.suppressSprint();
      }
   }

   @EventHandler
   public void onRender(EventRender3D event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         List<class_2338> var2 = this.navigator.getRemainingPath();
         if (!var2.isEmpty()) {
            class_243 var3 = mc.field_1724.method_73189().method_1031(0.0, 0.12, 0.0);

            for (int var4 = 0; var4 < var2.size(); var4++) {
               class_243 var5 = this.findPos8(var2.get(var4));
               WorldRenderBatcher.drawLineNoDepth(var3, var5, 1342226431, 6.0F);
               WorldRenderBatcher.drawLineNoDepth(var3, var5, -16721409, 2.25F);
               if (var4 == 0 || var4 == var2.size() + -2 + 1) {
                  this.findCenter(var5, var4 == var2.size() + -2 + 1 ? -11713 : -794233601);
               }

               var3 = var5;
            }

            this.findGoal(var2.getLast());
         }
      }
   }

   private void logPlayer17() {
      if (this.interactionSent) {
         if (this.stateTicks > 24) {
            this.interactionSent = false;
            this.logPlayer5();
         }
      } else {
         List<class_2338> var1 = this.findWorld2();
         if (!this.isChests(var1)) {
            if (!this.findChests4(var1)) {
               this.findChests6(var1);
            } else if (mc.field_1724.field_6012 + ~this.targetSelectedAge + 1 >= 20) {
               class_2338 var2 = this.findChests3(var1);
               long var3 = this.logKey(this.targetKey);
               int var5 = var3 >= 0L && var3 <= this.findChest9(this.targetChest, false) ? 1 : 0;
               if (var2 != null && !var2.equals(this.targetChest) && var5 == 0) {
                  this.logChest(var2);
               }
            }

            if (!this.containsList()) {
               if (this.targetChest == null) {
                  this.state = AutoWarden.CycleState.SEARCHING;
                  this.logChests(var1);
               } else {
                  this.noTargetTicks = 0;
                  long var7 = this.logKey(this.targetKey);
                  long var4 = this.findChest9(this.targetChest, false);
                  if (var7 >= 0L && var7 > var4) {
                     class_2338 var6 = this.targetChest;
                     LOGGER.info(
                        "Loaded timer for current chest {} is still {}ms away (approach window {}ms); releasing it and loading the next chest without a timer",
                        new Object[]{var6, var7, var4}
                     );
                     this.navigator.stop();
                     this.collectWorld();
                     this.state = AutoWarden.CycleState.SEARCHING;
                     this.repathDelay = 0;
                  } else if (!this.findChest8(this.targetChest) || this.findPos2(this.targetChest) == null) {
                     this.findChest5(this.targetChest, false);
                  } else if (var7 > 2000L) {
                     if (this.navigator.isWorking()) {
                        this.navigator.stop();
                     }

                     this.movementGoal = null;
                     this.state = AutoWarden.CycleState.WAITING;
                  } else {
                     if (this.navigator.isWorking()) {
                        this.navigator.stop();
                     }

                     if (this.state != AutoWarden.CycleState.OPENING) {
                        this.state = AutoWarden.CycleState.OPENING;
                        this.stateTicks = 0;
                     }

                     this.findRemaining(var7);
                  }
               }
            }
         }
      }
   }

   private boolean isChests(List<class_2338> chests) {
      if (this.timedRouteRecoveryChest == null || mc.field_1724 == null) {
         return false;
      } else if (this.targetChest != null && this.timedRouteRecoveryChest.equals(this.targetChest)) {
         long var2 = this.logKey(this.targetKey);
         if (var2 >= 0L && var2 <= Math.max(45000L, this.findChest9(this.targetChest, false))) {
            class_2338 var4 = this.findChests2(chests, this.timedRouteRecoveryChest);
            if (var4 != null) {
               AutoWarden.ChestKey var5 = this.targetKey;
               class_2338 var6 = this.targetChest;
               long var7 = this.logKey(this.logChest2(var4));
               if (var2 > 0L && var7 >= 0L && var7 - -1501L - 1L < var2) {
                  this.skippedUntilAge.put(var5, mc.field_1724.field_6012 - -81 - 1);
                  LOGGER.info("Earlier loaded chest {} ({}ms) preempts recovering chest {} ({}ms)", new Object[]{var4, var7, var6, var2});
                  this.logChest(var4);
                  return false;
               }
            }

            if (mc.field_1724.field_6012 >= this.timedRouteRecoveryUntilAge) {
               if (chests.contains(this.timedRouteRecoveryChest) && this.findChest10(this.timedRouteRecoveryChest)) {
                  this.navigator.stop();
                  this.movementGoal = null;
                  this.collectWorld2();
                  LOGGER.info("Remembered chest {} is loaded again; retrying its direct route", this.targetChest);
                  return false;
               }

               this.timedRouteRecoveryUntilAge = mc.field_1724.field_6012 - -401 - 1;
            }

            this.state = AutoWarden.CycleState.WAITING;
            this.logPreferredChest(this.timedRouteRecoveryChest);
            return true;
         } else {
            this.navigator.stop();
            this.collectWorld();
            return true;
         }
      } else {
         this.collectWorld2();
         return false;
      }
   }

   private boolean isList2() {
      return switch (this.state) {
         case SEARCHING, WAITING, WALKING, OPENING, LOOTING -> false;
         default -> true;
      };
   }

   private boolean isList3() {
      return switch (this.state) {
         case PVP_WAIT, STORAGE_ANARCHY_WAIT, FARM_ANARCHY_WAIT, HOME_WAIT, FIND_DEPOSIT, WALK_DEPOSIT, OPEN_DEPOSIT, DEPOSIT, FIND_SUPPLY, WALK_SUPPLY, OPEN_SUPPLY, TAKE_SUPPLY -> true;
         default -> false;
      };
   }

   private boolean isList() {
      return switch (this.state) {
         case OPENING, LOOTING, OPEN_DEPOSIT, DEPOSIT, OPEN_SUPPLY, TAKE_SUPPLY -> true;
         default -> false;
      };
   }

   private void logList() {
      this.logAdvanceFarmAnarchy2(false);
   }

   private void logAdvanceFarmAnarchy2(boolean advanceFarmAnarchy) {
      this.logAdvanceFarmAnarchy(advanceFarmAnarchy, null);
   }

   private void logAdvanceFarmAnarchy(boolean advanceFarmAnarchy, String forcedResumeAnarchy) {
      this.returnHomePending = false;
      this.navigator.stop();
      this.collectWorld();
      this.findWorld3();
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
      this.homeChest = null;
      this.supplyChest = null;
      this.depositSlot = 0;
      this.scanDelay = 0;
      this.repathDelay = 0;
      String var3 = this.logList2();
      String var4 = forcedResumeAnarchy != null ? forcedResumeAnarchy : (advanceFarmAnarchy ? this.findFrom(var3) : this.findPreferred(var3));
      this.farmAnarchyBeforeStorage = var4 != null ? var4 : var3;
      String var5 = this.findPos6();
      LOGGER.info(
         "Starting storage cycle: currentFarm=/an{}, resumeFarm=/an{}, storage=/an{}, restock={}",
         new Object[]{var3, this.farmAnarchyBeforeStorage, var5, this.isPlayer6()}
      );
      if (var5 != null && !var5.equals(this.currentAnarchy)) {
         this.logTarget(var5, true);
      } else if (var5 != null) {
         this.logReason2("already on supply anarchy");
      } else {
         this.logPurpose(AutoWarden.HomePurpose.STORAGE);
      }
   }

   private boolean isPlayer9() {
      if (this.initialFarmRoutePending && this.isList2()) {
         List var1 = this.parseAnarchies();
         if (var1.isEmpty()) {
            this.initialFarmRoutePending = false;
            return false;
         } else {
            String var2 = this.findPreferred(this.currentAnarchy);
            if (var2 == null) {
               var2 = var1.contains(this.currentAnarchy) ? this.currentAnarchy : (String)var1.getFirst();
            }

            this.farmAnarchyBeforeStorage = var2;
            if (!var2.equals(this.currentAnarchy)) {
               if ("?".equals(this.currentAnarchy) && this.stateTicks < 20) {
                  this.navigator.stop();
                  return true;
               } else {
                  this.initialFarmRoutePending = false;
                  this.logTarget(var2, false);
                  return true;
               }
            } else if (!this.isWorld2()) {
               this.initialFarmRoutePending = false;
               this.logPurpose(AutoWarden.HomePurpose.FARM_RETURN);
               return true;
            } else {
               this.initialFarmRoutePending = false;
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private boolean isPlayer3() {
      if (!this.initialSupplyPending || !this.isList2()) {
         return false;
      } else if (!this.autoSupplies.getValue()) {
         this.initialSupplyPending = false;
         return false;
      } else if ("?".equals(this.currentAnarchy) && this.stateTicks < 20) {
         this.navigator.stop();
         return true;
      } else if (!this.isPlayer6()) {
         this.initialSupplyPending = false;
         LOGGER.info("Initial supply check complete: hotbar already has all configured supplies");
         return false;
      } else {
         this.initialSupplyPending = false;
         LOGGER.info("Initial supplies are missing; starting storage before the first farm route");
         this.logList();
         return true;
      }
   }

   private void logTarget(String target, boolean storageTransfer) {
      if (target != null && !target.isBlank()) {
         if (this.isWorld3()) {
            this.logTarget2(target, storageTransfer);
         } else {
            this.navigator.stop();
            this.collectWorld();
            this.expectedAnarchy = target;
            mc.field_1724.field_3944.method_45730("an" + target);
            LOGGER.info("AutoWarden sent /an{} for {}", target, storageTransfer ? "storage" : "farm return");
            this.state = storageTransfer ? AutoWarden.CycleState.STORAGE_ANARCHY_WAIT : AutoWarden.CycleState.FARM_ANARCHY_WAIT;
            this.stateTicks = 0;
         }
      } else if (storageTransfer) {
         this.logPurpose(AutoWarden.HomePurpose.STORAGE);
      } else {
         this.logPurpose(AutoWarden.HomePurpose.FARM_RETURN);
      }
   }

   private void logStorageTransfer(boolean storageTransfer) {
      this.navigator.stop();
      if (this.expectedAnarchy != null && this.isWorld3()) {
         String var2 = this.expectedAnarchy;
         this.logTarget2(var2, storageTransfer);
      } else if (this.expectedAnarchy != null && this.expectedAnarchy.equals(this.currentAnarchy) && this.stateTicks >= 20) {
         this.expectedAnarchy = null;
         if (storageTransfer) {
            this.logReason2("arrived on supply anarchy");
         } else if (this.isWorld2()) {
            LOGGER.info("AutoWarden is already in the Warden city after /an{}; skipping /home", this.currentAnarchy);
            this.logPlayer14();
         } else {
            this.logPurpose(AutoWarden.HomePurpose.FARM_RETURN);
         }
      } else if (this.stateTicks > 0 && this.stateTicks % 100 == 0 && this.expectedAnarchy != null) {
         this.logTarget(this.expectedAnarchy, storageTransfer);
      }
   }

   private void logWorld6() {
      switch (this.state) {
         case PVP_WAIT:
            this.logWorld();
            break;
         case STORAGE_ANARCHY_WAIT:
            this.logStorageTransfer(true);
            break;
         case FARM_ANARCHY_WAIT:
            this.logStorageTransfer(false);
            break;
         case HOME_WAIT:
            this.logWorld7();
            break;
         case FIND_DEPOSIT:
            this.logPlayer6();
            break;
         case WALK_DEPOSIT:
            this.calculatePlayer();
            break;
         case OPEN_DEPOSIT:
            this.calculatePlayer2();
         case DEPOSIT:
         default:
            break;
         case FIND_SUPPLY:
            this.logState5();
            break;
         case WALK_SUPPLY:
            this.logState();
            break;
         case OPEN_SUPPLY:
            this.logState4();
      }
   }

   private void logPurpose(AutoWarden.HomePurpose purpose) {
      if (purpose == AutoWarden.HomePurpose.FARM_RETURN && this.isWorld2()) {
         LOGGER.info("AutoWarden is already in the Warden city; skipping redundant /home");
         this.logPlayer14();
      } else {
         String var2 = this.findPos6();
         if (purpose == AutoWarden.HomePurpose.STORAGE && var2 != null && var2.equals(this.currentAnarchy)) {
            this.logReason2("supply anarchy does not require /home");
         } else if (this.isWorld3()) {
            this.logPurpose2(purpose);
         } else {
            this.homePurpose = purpose;
            this.homeTeleportConfirmed = false;
            this.homeCommandPos = mc.field_1724.method_73189();
            this.homeCommandWorld = mc.field_1687;
            mc.field_1724.field_3944.method_45730("home");
            LOGGER.info("AutoWarden sent /home for {} from {}", purpose, this.homeCommandPos);
            this.state = AutoWarden.CycleState.HOME_WAIT;
            this.stateTicks = 0;
         }
      }
   }

   private void logReason2(String reason) {
      this.navigator.stop();
      this.homePurpose = AutoWarden.HomePurpose.STORAGE;
      this.homeCommandPos = null;
      this.homeCommandWorld = null;
      this.homeTeleportConfirmed = false;
      this.expectedAnarchy = null;
      this.homeChest = null;
      this.supplyChest = null;
      this.movementGoal = null;
      this.scanDelay = 0;
      this.repathDelay = 0;
      this.state = AutoWarden.CycleState.FIND_DEPOSIT;
      this.stateTicks = 0;
      LOGGER.info("AutoWarden started storage directly at {}: {}", mc.field_1724.method_24515(), reason);
   }

   private void logTarget2(String target, boolean storageTransfer) {
      this.navigator.stop();
      this.collectWorld();
      this.deferredAnarchy = target;
      this.deferredStorageTransfer = storageTransfer;
      this.deferredHomePurpose = null;
      this.expectedAnarchy = null;
      this.pvpClearTicks = 0;
      this.state = AutoWarden.CycleState.PVP_WAIT;
      this.stateTicks = 0;
      LOGGER.warn("PvP cooldown is active; holding loot and delaying /an{}", target);
   }

   private void logPurpose2(AutoWarden.HomePurpose purpose) {
      this.navigator.stop();
      this.collectWorld();
      this.deferredAnarchy = null;
      this.deferredStorageTransfer = null;
      this.deferredHomePurpose = purpose;
      this.pvpClearTicks = 0;
      this.state = AutoWarden.CycleState.PVP_WAIT;
      this.stateTicks = 0;
      LOGGER.warn("PvP cooldown is active; holding loot and delaying /home for {}", purpose);
   }

   private void logWorld() {
      if (this.isWorld3()) {
         this.pvpClearTicks = 0;
         this.logPlayer8();
      } else if ((this.pvpClearTicks = this.pvpClearTicks - -2 - 1) < 20) {
         this.logPlayer8();
      } else {
         this.logPlayer10();
         String var1 = this.deferredAnarchy;
         Boolean var2 = this.deferredStorageTransfer;
         AutoWarden.HomePurpose var3 = this.deferredHomePurpose;
         this.deferredAnarchy = null;
         this.deferredStorageTransfer = null;
         this.deferredHomePurpose = null;
         this.pvpClearTicks = 0;
         if (var1 != null && var2 != null) {
            LOGGER.info("PvP cooldown cleared; continuing with /an{}", var1);
            this.logTarget(var1, var2);
         } else if (var3 != null) {
            LOGGER.info("PvP cooldown cleared; continuing with /home for {}", var3);
            this.logPurpose(var3);
         } else {
            this.state = AutoWarden.CycleState.SEARCHING;
            this.stateTicks = 0;
         }
      }
   }

   private boolean isWorld3() {
      return ServerUtil.isPvp()
         ? true
         : (Boolean)(
            mc.field_1705 != null && mc.field_1705.method_1740() != null
               ? ((BossBarHudAccessor)mc.field_1705.method_1740())
                  .mentions$getBossBars()
                  .values()
                  .stream()
                  .map(bar -> bar.method_5414().getString().toLowerCase(Locale.ROOT))
                  .anyMatch(title -> !title.contains("pvp") && !title.contains("пвп") && !title.contains("combat"))
               : 0
         );
   }

   private void logWorld7() {
      this.navigator.stop();
      int var1 = this.homeTeleportConfirmed || this.homeCommandWorld != null && mc.field_1687 != this.homeCommandWorld ? 1 : 0;
      if (var1 == 0 && this.homeCommandPos != null) {
         var1 = mc.field_1724.method_73189().method_1025(this.homeCommandPos) > 64.0 ? 1 : 0;
      }

      if (var1 != 0) {
         this.homeTeleportConfirmed = false;
         LOGGER.info("AutoWarden arrived after /home for {} at {}", this.homePurpose, mc.field_1724.method_24515());
         if (this.homePurpose == AutoWarden.HomePurpose.FARM_RETURN) {
            this.logPlayer14();
         } else {
            this.homeChest = null;
            this.state = AutoWarden.CycleState.FIND_DEPOSIT;
            this.stateTicks = 0;
            this.scanDelay = 0;
         }
      } else if (this.stateTicks > 0 && this.stateTicks % 200 == 0) {
         this.logPurpose(this.homePurpose);
      }
   }

   private boolean isMessage(String message) {
      return !message.contains("home")
         ? false
         : !message.contains("телепортирование к home")
            && !message.contains("телепортирован к home")
            && !message.contains("teleporting to home")
            && !message.contains("teleported to home");
   }

   private void logPlayer6() {
      if (this.scanDelay <= 0) {
         this.scanDelay = 10;
         this.homeChest = this.findWorld6();
         if (this.homeChest != null) {
            LOGGER.info("AutoWarden selected deposit chest {}", this.homeChest);
            this.state = AutoWarden.CycleState.WALK_DEPOSIT;
            this.stateTicks = 0;
            this.movementGoal = null;
            this.repathDelay = 0;
         } else if (!this.isPlayer10()) {
            this.logState3();
         } else if (this.stateTicks > 120) {
            this.exhaustedHomeChests.clear();
            this.stateTicks = 0;
         }
      }
   }

   private void calculatePlayer() {
      if (!this.isPos(this.homeChest)) {
         this.navigator.stop();
         this.homeChest = null;
         this.state = AutoWarden.CycleState.FIND_DEPOSIT;
         this.stateTicks = 0;
         this.scanDelay = 0;
      } else if (this.findPos2(this.homeChest) != null) {
         this.navigator.stop();
         this.state = AutoWarden.CycleState.OPEN_DEPOSIT;
         this.stateTicks = 0;
         this.actionDelay = 0;
         this.findPos();
      } else {
         this.findChest7(this.homeChest);
      }
   }

   private void calculatePlayer2() {
      if (!this.isPos(this.homeChest)) {
         this.logState2();
      } else if (this.actionDelay <= 0) {
         if (this.isChest(this.homeChest)) {
            this.actionDelay = 12;
         }

         if (this.stateTicks > 100) {
            this.logState2();
         }
      }
   }

   private void calculateHandler3(class_1707 handler) {
      if (this.actionDelay <= 0) {
         int var2 = handler.method_7629().method_5439();
         if (this.depositSlot < var2) {
            this.depositSlot = var2;
         }

         int var3 = this.logHandler2(handler, this.depositSlot);
         if (var3 >= 0) {
            class_1735 var5 = (class_1735)handler.field_7761.get(var3);
            mc.field_1761.method_2906(handler.field_7763, var5.field_7874, 0, class_1713.field_7794, mc.field_1724);
            this.depositSlot = var3 - -2 - 1;
            this.actionDelay = 3;
         } else {
            int var4 = this.logHandler2(handler, var2) >= 0 ? 1 : 0;
            this.logWorld3();
            if (var4 != 0) {
               this.exhaustedHomeChests.add(this.homeChest.method_10062());
               this.homeChest = null;
               this.depositSlot = 0;
               this.state = AutoWarden.CycleState.FIND_DEPOSIT;
               this.stateTicks = 0;
               this.scanDelay = 0;
            } else {
               this.homeChest = null;
               this.logState3();
            }
         }
      }
   }

   private int logHandler2(class_1707 handler, int start) {
      for (int var3 = Math.max(0, start); var3 < handler.field_7761.size(); var3++) {
         class_1799 var4 = ((class_1735)handler.field_7761.get(var3)).method_7677();
         if (!var4.method_7960() && !this.containsStack4(var4)) {
            return var3;
         }
      }

      return -1;
   }

   private boolean isPlayer10() {
      for (int var1 = 0; var1 < 36; var1++) {
         class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
         if (!var2.method_7960() && !this.containsStack4(var2)) {
            return true;
         }
      }

      return false;
   }

   private void logState2() {
      if (this.homeChest != null) {
         this.exhaustedHomeChests.add(this.homeChest.method_10062());
      }

      this.navigator.stop();
      this.homeChest = null;
      this.depositSlot = 0;
      this.state = AutoWarden.CycleState.FIND_DEPOSIT;
      this.stateTicks = 0;
      this.scanDelay = 0;
      this.actionDelay = 0;
      this.findPos();
   }

   private void logState3() {
      if (this.isPlayer6()) {
         this.state = AutoWarden.CycleState.FIND_SUPPLY;
         this.stateTicks = 0;
         this.scanDelay = 0;
         this.unavailableSupplyKinds.clear();
      } else {
         this.logPlayer18();
      }
   }

   private void logState5() {
      if (!this.isPlayer6()) {
         this.logPlayer18();
      } else if (this.scanDelay <= 0) {
         this.scanDelay = 10;
         this.supplyChest = this.findWorld5();
         if (this.supplyChest != null) {
            LOGGER.info("AutoWarden selected signed supply chest {}", this.supplyChest);
            this.unavailableSupplyKinds.clear();
            this.state = AutoWarden.CycleState.WALK_SUPPLY;
            this.stateTicks = 0;
            this.movementGoal = null;
            this.repathDelay = 0;
         } else if (this.stateTicks > 120) {
            LOGGER.warn("Supply chest with sign '{}' was not found; returning to farm", this.supplySign.getValue());
            this.logPlayer18();
         }
      }
   }

   private void logState() {
      if (!this.isPos(this.supplyChest) || !this.findChest(this.supplyChest)) {
         this.navigator.stop();
         this.supplyChest = null;
         this.state = AutoWarden.CycleState.FIND_SUPPLY;
         this.stateTicks = 0;
         this.scanDelay = 0;
      } else if (this.findPos2(this.supplyChest) != null) {
         this.navigator.stop();
         this.state = AutoWarden.CycleState.OPEN_SUPPLY;
         this.stateTicks = 0;
         this.actionDelay = 0;
         this.findPos();
      } else {
         this.findChest7(this.supplyChest);
      }
   }

   private void logState4() {
      if (!this.isPos(this.supplyChest) || !this.findChest(this.supplyChest)) {
         this.findPlayer();
      } else if (this.actionDelay <= 0) {
         if (this.isChest(this.supplyChest)) {
            this.actionDelay = 12;
         }

         if (this.stateTicks > 100) {
            this.findPlayer();
         }
      }
   }

   private void logHandler(class_1707 handler) {
      if (this.actionDelay <= 0) {
         AutoWarden.SupplyKind var2 = this.calculatePlayer3();
         if (var2 == null) {
            boolean var6 = this.isPlayer6();
            this.logWorld3();
            if (var6) {
               this.exhaustedSupplyChests.add(this.supplyChest.method_10062());
               this.supplyChest = null;
               this.unavailableSupplyKinds.clear();
               this.state = AutoWarden.CycleState.FIND_SUPPLY;
               this.stateTicks = 0;
               this.scanDelay = 0;
            } else {
               this.supplyChest = null;
               this.logPlayer18();
            }
         } else {
            int var3 = handler.method_17388() * 9;
            int var4 = this.findHandler(handler, var3, var2);
            if (var4 < 0) {
               var4 = this.calculateHandler(handler, var3, var2);
            }

            if (var4 < 0) {
               this.unavailableSupplyKinds.add(var2);
               LOGGER.info("Supply chest {} has no {}", this.supplyChest, var2);
               this.actionDelay = 1;
            } else {
               int var5 = Math.max(0, this.calculateKind2(var2) + ~this.collectKind(var2) + 1);
               LOGGER.info("Taking {} x{} from supply chest {}", new Object[]{var2, var5, this.supplyChest});
               if (!this.findHandler2(handler, var4, var3, var5)) {
                  this.findPlayer();
               } else {
                  this.actionDelay = 3;
               }
            }
         }
      }
   }

   private int calculateHandler(class_1707 handler, int chestSlots, AutoWarden.SupplyKind kind) {
      for (int var4 = 0; var4 < chestSlots; var4++) {
         if (this.containsStack2(handler.method_7611(var4).method_7677(), kind)) {
            return var4;
         }
      }

      return -1;
   }

   private int findHandler(class_1707 handler, int chestSlots, AutoWarden.SupplyKind kind) {
      int var4 = Math.max(chestSlots, handler.field_7761.size() + -10 + 1);

      for (int var5 = chestSlots; var5 < var4; var5++) {
         if (this.containsStack2(handler.method_7611(var5).method_7677(), kind)) {
            return var5;
         }
      }

      return -1;
   }

   private boolean findHandler2(class_1707 handler, int source, int chestSlots, int wanted) {
      class_1799 var5 = handler.method_7611(source).method_7677();
      int var6 = Math.min(wanted, var5.method_7947());
      if (var6 <= 0) {
         return true;
      } else {
         int var7 = Math.max(chestSlots, handler.field_7761.size() + -10 + 1);
         int var8 = -1;

         for (int var9 = var7; var9 < handler.field_7761.size() + -2 + 1; var9++) {
            class_1799 var10 = handler.method_7611(var9).method_7677();
            if (!var10.method_7960() && class_1799.method_31577(var10, var5) && var10.method_7914() + ~var10.method_7947() + 1 >= var6) {
               var8 = var9;
               break;
            }
         }

         if (var8 < 0) {
            for (int var11 = var7; var11 < handler.field_7761.size() + -2 + 1; var11++) {
               if (!handler.method_7611(var11).method_7681()) {
                  var8 = var11;
                  break;
               }
            }
         }

         if (var8 < 0) {
            return false;
         } else {
            mc.field_1761.method_2906(handler.field_7763, handler.method_7611(source).field_7874, 0, class_1713.field_7790, mc.field_1724);

            for (int var12 = 0; var12 < var6; var12++) {
               mc.field_1761.method_2906(handler.field_7763, handler.method_7611(var8).field_7874, 1, class_1713.field_7790, mc.field_1724);
            }

            mc.field_1761.method_2906(handler.field_7763, handler.method_7611(source).field_7874, 0, class_1713.field_7790, mc.field_1724);
            return true;
         }
      }
   }

   private void findPlayer() {
      if (this.supplyChest != null) {
         this.exhaustedSupplyChests.add(this.supplyChest.method_10062());
      }

      this.logWorld3();
      this.navigator.stop();
      this.supplyChest = null;
      this.unavailableSupplyKinds.clear();
      this.state = AutoWarden.CycleState.FIND_SUPPLY;
      this.stateTicks = 0;
      this.scanDelay = 0;
      this.actionDelay = 0;
      this.findPos();
   }

   private void findChest7(class_2338 chest) {
      List var2 = this.findChest4(chest);
      if (var2.isEmpty()) {
         if (this.state == AutoWarden.CycleState.WALK_DEPOSIT) {
            this.logState2();
         } else {
            this.findPlayer();
         }
      } else {
         int var3 = this.movementGoal != null && var2.contains(this.movementGoal) ? 0 : 1;
         this.movementGoal = (class_2338)var2.getFirst();
         if (!this.navigator.isWorking() || var3 != 0 || this.repathDelay <= 0) {
            this.navigator.navigateToAny(var2, var3 != 0);
            this.repathDelay = 8;
         }
      }
   }

   private boolean isChest(class_2338 chest) {
      boolean var2 = this.findPos7(chest);
      class_3965 var3 = this.logPos(chest);
      if (var3 == null && !var2) {
         return false;
      } else {
         if (var3 == null) {
            var3 = this.findPos2(chest);
         }

         if (var3 == null) {
            return false;
         } else {
            mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, var3);
            mc.field_1724.method_6104(class_1268.field_5808);
            return true;
         }
      }
   }

   private class_3965 logPos(class_2338 pos) {
      if (mc.field_1765 instanceof class_3965 var2
         && var2.method_17783() == class_240.field_1332
         && this.findIntended(pos, var2.method_17777())
         && !(mc.field_1724.method_33571().method_1025(var2.method_17784()) > 18.0625)) {
         return var2;
      }

      return null;
   }

   private void logPlayer15() {
      if (!this.isPlayer8() && !this.isPlayer4() && !this.isPlayer2()) {
         if (this.state == AutoWarden.CycleState.WALK_DEPOSIT) {
            this.logState2();
         } else if (this.state == AutoWarden.CycleState.WALK_SUPPLY) {
            this.findPlayer();
         } else if (this.state == AutoWarden.CycleState.PVP_WAIT || this.state == AutoWarden.CycleState.SEARCHING && this.targetChest == null) {
            if (this.discoveryChest != null) {
               this.logReason("navigator rejected the loading route");
               return;
            }

            this.navigator.stop();
            this.movementGoal = null;
            this.repathDelay = 40;
         } else {
            this.logPlayer5();
         }
      }
   }

   private boolean isPlayer8() {
      if (this.isList2()
         && mc.field_1724 != null
         && this.timedRouteRecoveryChest != null
         && this.targetChest != null
         && this.timedRouteRecoveryChest.equals(this.targetChest)) {
         this.navigator.stop();
         this.movementGoal = null;
         this.repathDelay = 8;
         this.state = AutoWarden.CycleState.WAITING;
         LOGGER.info("Recovery patrol route failed; retaining remembered chest {} and trying another approach", this.timedRouteRecoveryChest);
         return true;
      } else {
         return false;
      }
   }

   private boolean isPlayer4() {
      if (this.isList2() && mc.field_1724 != null && this.targetChest != null && this.targetKey != null && this.timedRouteRecoveryChest == null) {
         long var1 = this.logKey(this.targetKey);
         if (var1 >= 0L && var1 <= Math.max(45000L, this.findChest9(this.targetChest, false))) {
            AutoWarden.ChestKey var3 = this.targetKey;
            class_2338 var4 = this.targetChest;
            this.skippedUntilAge.put(var3, mc.field_1724.field_6012 - -81 - 1);
            this.navigator.stop();
            this.logPlayer3();
            this.sequenceTracker.recordEvent("timed_route_direct_failed", this.currentAnarchy, var4, var1);
            this.collectWorld();
            this.state = AutoWarden.CycleState.SEARCHING;
            this.repathDelay = 0;
            this.noTargetTicks = 0;
            LOGGER.warn("Direct route to ready remembered chest {} failed; loading another chest without a timer instead of patrolling", var4);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isPlayer2() {
      if (this.isList2() && mc.field_1724 != null) {
         class_2338 var1 = mc.field_1724.method_24515();
         if (this.routeFailureAnchor != null && Objects.equals(this.routeFailureTarget, this.targetKey) && !(this.routeFailureAnchor.method_10262(var1) > 4.0)) {
            this.routeFailuresAtAnchor = this.routeFailuresAtAnchor - -2 - 1;
            if (this.routeFailuresAtAnchor < 8) {
               return false;
            } else {
               LOGGER.warn("AutoWarden is trapped at {} after {} consecutive route failures", var1, this.routeFailuresAtAnchor);
               this.logPlayer3();
               if (this.isReason("repeated route failures")) {
                  return true;
               } else {
                  LOGGER.warn("No alternate farm anarchy is configured; backing off without another /home");
                  this.navigator.stop();
                  this.collectWorld();
                  this.noTargetTicks = 0;
                  this.repathDelay = 40;
                  this.state = AutoWarden.CycleState.SEARCHING;
                  return true;
               }
            }
         } else {
            this.routeFailureAnchor = var1.method_10062();
            this.routeFailureTarget = this.targetKey;
            this.routeFailuresAtAnchor = 1;
            return false;
         }
      } else {
         return false;
      }
   }

   private void logPlayer3() {
      this.routeFailureAnchor = null;
      this.routeFailureTarget = null;
      this.routeFailuresAtAnchor = 0;
   }

   private void logPlayer18() {
      this.logWorld3();
      this.navigator.stop();
      this.homeChest = null;
      this.supplyChest = null;
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
      this.lootedChests = 0;
      this.returnHomePending = false;
      String var1 = this.logList2();
      LOGGER.info("Storage cycle complete; returning to farm /an{}", var1);
      if (var1 != null && !var1.equals(this.currentAnarchy)) {
         this.logTarget(var1, false);
      } else {
         this.logPurpose(AutoWarden.HomePurpose.FARM_RETURN);
      }
   }

   private void logPlayer14() {
      this.logWorld3();
      this.navigator.stop();
      this.homeChest = null;
      this.supplyChest = null;
      this.homeCommandPos = null;
      this.homeCommandWorld = null;
      this.homeTeleportConfirmed = false;
      this.expectedAnarchy = null;
      this.farmAnarchyBeforeStorage = null;
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
      this.lootedChests = 0;
      this.returnHomePending = false;
      this.state = AutoWarden.CycleState.SEARCHING;
      this.stateTicks = 0;
      this.scanDelay = 0;
      this.repathDelay = 0;
      this.initialFarmRoutePending = false;
      this.outsideCityTicks = 0;
      this.noTargetTicks = 0;
      this.farmEffectSyncGraceTicks = 200;
      this.emergencyRestockTicks = 0;
      if (this.deathPotionRecovery) {
         this.deathPotionRecovery = false;
         LOGGER.info("Death supply recovery reached the farm; potion use is enabled immediately");
      }

      LOGGER.info("Farm /home complete at {}; city={}", mc.field_1724.method_24515(), this.isWorld2());
      this.collectWorld();
   }

   private String logList2() {
      List var1 = this.parseAnarchies();
      if (this.farmAnarchyBeforeStorage == null || !var1.isEmpty() && !var1.contains(this.farmAnarchyBeforeStorage)) {
         if (var1.contains(this.currentAnarchy)) {
            return this.currentAnarchy;
         } else if (!var1.isEmpty()) {
            return (String)var1.getFirst();
         } else {
            return "?".equals(this.currentAnarchy) ? null : this.currentAnarchy;
         }
      } else {
         return this.farmAnarchyBeforeStorage;
      }
   }

   private boolean isReason(String reason) {
      String var2 = this.findFrom(this.currentAnarchy);
      if (var2 != null && !var2.equals(this.currentAnarchy)) {
         AutoWarden.FarmAnarchyPlan var3 = this.findAnarchy(var2);
         LOGGER.info(
            "Switching farm anarchy /an{} -> /an{}: {}; rememberedReadyByArrival={}, soonestMs={}",
            new Object[]{this.currentAnarchy, var2, reason, var3.readyByArrival(), var3.soonestMs()}
         );
         this.navigator.stop();
         this.collectWorld();
         this.logPlayer3();
         this.noTargetTicks = 0;
         this.repathDelay = 0;
         this.farmAnarchyBeforeStorage = var2;
         this.logTarget(var2, false);
         return true;
      } else {
         return false;
      }
   }

   private boolean containsList() {
      AutoWarden.FarmAnarchyPlan var1 = this.findFrom2(this.currentAnarchy);
      if (var1 != null && var1.knownTimers() != 0 && var1.readyByArrival() != 0) {
         long var2 = this.logKey(this.targetKey);
         if (this.targetChest == null || var2 >= 0L && var2 > this.findChest9(this.targetChest, false)) {
            AutoWarden.FarmAnarchyPlan var4 = this.findAnarchy(this.currentAnarchy);
            return var4.readyByArrival() <= 0 && this.isReason("remembered chest will be ready by transfer and walking arrival");
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private String calculateFrom(String from) {
      List var2 = this.parseAnarchies();
      if (var2.size() < 2) {
         return null;
      } else {
         int var3 = var2.indexOf(from);
         return var3 >= 0 ? (String)var2.get((var3 - -2 - 1) % var2.size()) : (String)var2.getFirst();
      }
   }

   private String findFrom(String from) {
      AutoWarden.FarmAnarchyPlan var2 = this.findFrom2(from);
      return var2 != null ? var2.anarchy() : this.calculateFrom(from);
   }

   private AutoWarden.FarmAnarchyPlan findFrom2(String from) {
      List var2 = this.parseAnarchies();
      if (var2.size() < 2) {
         return null;
      } else {
         int var3 = var2.indexOf(from);
         int var4 = var3 >= 0 ? var3 : var2.size() + -2 + 1;
         AutoWarden.FarmAnarchyPlan var5 = null;
         int var6 = var3 >= 0 ? var2.size() + -2 + 1 : var2.size();

         for (int var7 = 1; var7 <= var6; var7++) {
            String var8 = (String)var2.get((var4 - ~var7 - 1) % var2.size());
            if (!var8.equals(from)) {
               AutoWarden.FarmAnarchyPlan var9 = this.findAnarchy(var8);
               if (var5 == null || this.findCandidate(var9, var5)) {
                  var5 = var9;
               }
            }
         }

         return var5;
      }
   }

   private String findPreferred(String preferred) {
      List var2 = this.parseAnarchies();
      if (var2.isEmpty()) {
         return preferred;
      } else {
         int var3 = var2.indexOf(preferred);
         int var4 = var3 >= 0 ? var3 : 0;
         AutoWarden.FarmAnarchyPlan var5 = null;

         for (int var6 = 0; var6 < var2.size(); var6++) {
            String var7 = (String)var2.get((var4 - ~var6 - 1) % var2.size());
            AutoWarden.FarmAnarchyPlan var8 = this.findAnarchy(var7);
            if (var5 == null || this.findCandidate(var8, var5)) {
               var5 = var8;
            }
         }

         return var5 == null ? (String)var2.getFirst() : var5.anarchy();
      }
   }

   private AutoWarden.FarmAnarchyPlan findAnarchy(String anarchy) {
      int var2 = 0;
      int var3 = 0;
      long var4 = Long.MAX_VALUE;

      for (AutoWardenSequenceTracker.TimerPrediction var7 : this.sequenceTracker.predictionsFor(anarchy)) {
         AutoWarden.ChestKey var8 = new AutoWarden.ChestKey(anarchy, var7.pos());
         if (!this.isKey(var8) && this.openAttempts.getOrDefault(var8, 0) < 1) {
            var2++;
            var4 = Math.min(var4, var7.remainingMs());
            if (var7.remainingMs() <= this.findChest9(var7.pos(), true)) {
               var3++;
            }
         }
      }

      return new AutoWarden.FarmAnarchyPlan(anarchy, var2, var3, var4 == Long.MAX_VALUE ? -1L : var4);
   }

   private long findChest9(class_2338 chest, boolean transferring) {
      if (chest != null && mc.field_1724 != null) {
         double var3 = (double)chest.method_10263() + 0.5 - mc.field_1724.method_23317();
         double var5 = (double)chest.method_10260() + 0.5 - mc.field_1724.method_23321();
         long var7 = Math.round(Math.sqrt(var3 * var3 + var5 * var5) / 3.6 * 1000.0);
         long var9 = transferring ? 15000L : 15000L;
         return Math.min(90000L, var9 - ~var7 - 1L);
      } else {
         return transferring ? 15000L : 15000L;
      }
   }

   private boolean findCandidate(AutoWarden.FarmAnarchyPlan candidate, AutoWarden.FarmAnarchyPlan currentBest) {
      if (candidate.knownTimers() != 0 && currentBest.knownTimers() != 0) {
         return candidate.readyByArrival() != currentBest.readyByArrival()
            ? candidate.readyByArrival() > currentBest.readyByArrival()
            : candidate.soonestMs() >= currentBest.soonestMs();
      } else {
         return candidate.knownTimers() <= currentBest.knownTimers();
      }
   }

   private String findPos6() {
      String var1 = this.supplyAnarchy.getValue();
      if (var1 == null) {
         return null;
      } else {
         String var2 = var1.replaceAll("[^0-9]", "");
         return var2.isBlank() ? null : var2;
      }
   }

   public boolean isMemoryTimerHudEnabled() {
      return !this.isEnabled() || !this.memoryTimerHud.getValue();
   }

   public List<AutoWarden.MemoryChestHudEntry> memoryChestHudEntries() {
      HashSet<String> var1 = new HashSet<>(this.parseAnarchies());
      String var2 = this.findPos6();
      if (var2 != null) {
         var1.remove(var2);
      }

      if (var1.isEmpty() && this.currentAnarchy != null && !"?".equals(this.currentAnarchy)) {
         var1.add(this.currentAnarchy);
      }

      ArrayList<AutoWarden.MemoryChestHudEntry> var3 = new ArrayList<>();

      for (String var5 : var1) {
         for (AutoWardenSequenceTracker.TimerPrediction var7 : this.sequenceTracker.predictionsFor(var5)) {
            var3.add(new AutoWarden.MemoryChestHudEntry(var5, var7.pos(), var7.remainingMs(), var5.equals(this.currentAnarchy)));
         }
      }

      var3.sort(
         Comparator.<AutoWarden.MemoryChestHudEntry, Boolean>comparing(entry -> entry.remainingMs() <= 0L)
            .thenComparingLong(AutoWarden.MemoryChestHudEntry::remainingMs)
            .thenComparing(AutoWarden.MemoryChestHudEntry::anarchy)
            .thenComparingInt(entry -> entry.pos().method_10263())
            .thenComparingInt(entry -> entry.pos().method_10260())
      );
      return List.copyOf(var3);
   }

   private void findChests6(List<class_2338> chests) {
      class_2338 var2 = this.findChests3(chests);
      if (var2 == null) {
         var2 = this.findChests5(chests);
      }

      if (var2 == null) {
         if (this.targetChest != null) {
            this.collectWorld();
         }
      } else {
         this.logChest(var2);
      }
   }

   private class_2338 findChests3(List<class_2338> chests) {
      return this.findChests2(chests, null);
   }

   private class_2338 findChests2(List<class_2338> chests, class_2338 excludedChest) {
      class_2338 var3 = null;
      double var4 = Double.MAX_VALUE;
      long var6 = Long.MAX_VALUE;

      for (class_2338 var9 : chests) {
         if (!var9.equals(excludedChest)) {
            AutoWarden.ChestKey var10 = this.logChest2(var9);
            long var11 = this.logKey(var10);
            if (var11 >= 0L
               && var11 <= this.findChest9(var9, false)
               && this.openAttempts.getOrDefault(var10, 0) < 1
               && this.findChest10(var9)
               && !this.findChest2(var9)
               && !this.findChest6(var9)
               && !this.isKey(var10)) {
               double var13 = mc.field_1724.method_33571().method_1025(class_243.method_24953(var9));
               if (var13 < var4 || var13 == var4 && var11 < var6) {
                  var3 = var9;
                  var4 = var13;
                  var6 = var11;
               }
            }
         }
      }

      return var3;
   }

   private class_2338 findChests5(List<class_2338> chests) {
      class_2338 var2 = null;
      int var3 = Integer.MAX_VALUE;
      double var4 = Double.MAX_VALUE;

      for (class_2338 var7 : chests) {
         AutoWarden.ChestKey var8 = this.logChest2(var7);
         if (this.findChest10(var7) && !this.findChest6(var7) && !this.isKey(var8)) {
            double var9 = mc.field_1724.method_33571().method_1025(class_243.method_24953(var7));
            long var11 = this.logKey(var8);
            boolean var13 = this.findChest2(var7);
            if (!var13 || var11 < 0L && var9 <= 16.0) {
               int var14 = this.openAttempts.getOrDefault(var8, 0);
               if ((var11 >= 0L ? var14 < 1 : var14 < 3) && var11 < 0L) {
                  byte var15;
                  if (var9 <= 25.0) {
                     var15 = 0;
                  } else if (var9 <= 144.0) {
                     var15 = 1;
                  } else {
                     var15 = 2;
                  }

                  double var16 = class_243.method_24953(var7).field_1351 - mc.field_1724.method_23320();
                  double var18 = (double)var7.method_10263() + 0.5 - mc.field_1724.method_23317();
                  double var20 = (double)var7.method_10260() + 0.5 - mc.field_1724.method_23321();
                  double var22 = var18 * var18 + var20 * var20 + (var16 > 0.0 ? 2.0 : 1.0) * var16 * var16;
                  if (var15 < var3 || var15 == var3 && var22 < var4) {
                     var3 = var15;
                     var4 = var22;
                     var2 = var7;
                  }
               }
            }
         }
      }

      return var2;
   }

   private class_2338 logChests3(List<class_2338> chests) {
      class_2338 var2 = null;
      long var3 = Long.MAX_VALUE;

      for (class_2338 var6 : chests) {
         AutoWarden.ChestKey var7 = this.logChest2(var6);
         long var8 = this.logKey(var7);
         if (var8 >= 0L
            && var8 < var3
            && this.openAttempts.getOrDefault(var7, 0) < 1
            && this.findChest10(var6)
            && !this.findChest2(var6)
            && !this.findChest6(var6)
            && !this.isKey(var7)) {
            var3 = var8;
            var2 = var6;
         }
      }

      return var2;
   }

   private void logChests(List<class_2338> chests) {
      this.noTargetTicks = this.noTargetTicks - -2 - 1;
      int var2 = !this.sequenceTracker.predictionsFor(this.currentAnarchy).isEmpty() ? 1 : 0;
      AutoWardenSequenceTracker.TimerPrediction var3 = this.logLoadedChests2(chests);
      if (!this.isLoadedChests(chests)) {
         if (var2 == 0) {
            this.logPlayer10();
            if (this.noTargetTicks >= 20) {
               this.isReason("no remembered chest timers on the current anarchy");
            }
         } else if (this.noTargetTicks < 100 || !this.isReason("all known local chests have timers and none is ready by arrival")) {
            if (this.noTargetTicks % 100 == 0) {
               long var4 = chests.stream().mapToLong(chest -> this.logKey(this.logChest2(chest))).filter(remaining -> remaining < 0L).min().orElse(-1L);
               LOGGER.info(
                  "AutoWarden has no immediate target: loadedChests={}, soonestMs={}, rememberedMs={}, anarchy=/an{}, pos={}",
                  new Object[]{chests.size(), var4, var3 == null ? -1L : var3.remainingMs(), this.currentAnarchy, mc.field_1724.method_24515()}
               );
            }

            this.logPlayer10();
         }
      }
   }

   private boolean isLoadedChests(List<class_2338> loadedChests) {
      if (mc.field_1724 == null) {
         return false;
      } else if (this.discoveryChest != null && this.sequenceTracker.hasTimer(this.currentAnarchy, this.discoveryChest)) {
         LOGGER.info(
            "class_236 for previously unrecorded chest {} on /an{} is now loaded; handing it to normal chest selection",
            this.discoveryChest,
            this.currentAnarchy
         );
         this.logPlayer9();
         return false;
      } else {
         if (this.discoveryChest == null) {
            this.discoveryChest = this.logLoadedChests(loadedChests);
            if (this.discoveryChest == null) {
               return false;
            }

            this.discoveryNoGoalAttempts = 0;
            this.movementGoal = null;
            this.repathDelay = 0;
            LOGGER.info("Loading unrecorded Warden chest {} on /an{} from the structural coordinate catalog", this.discoveryChest, this.currentAnarchy);
         }

         this.logPreferredChest(this.discoveryChest);
         return true;
      }
   }

   private class_2338 logLoadedChests(List<class_2338> loadedChests) {
      class_2338 var2 = mc.field_1724.method_24515();
      return this.sequenceTracker
         .knownCoordinates()
         .stream()
         .filter(this::isPos2)
         .filter(pos -> this.sequenceTracker.hasTimer(this.currentAnarchy, pos))
         .filter(pos -> this.discoverySkippedUntilAge.getOrDefault(new AutoWarden.ChestKey(this.currentAnarchy, pos), 0) > mc.field_1724.field_6012)
         .min(
            Comparator.<class_2338>comparingDouble(pos -> this.logFrom(var2, pos))
               .thenComparingInt(class_2382::method_10263)
               .thenComparingInt(class_2382::method_10260)
         )
         .orElse(null);
   }

   private double logFrom(class_2338 from, class_2338 chest) {
      double var3 = (double)(chest.method_10263() + ~from.method_10263() + 1);
      double var5 = (double)(chest.method_10264() + ~from.method_10264() + 1);
      double var7 = (double)(chest.method_10260() + ~from.method_10260() + 1);
      return var3 * var3 + var7 * var7 + (var5 > 0.0 ? 2.0 : 1.0) * var5 * var5;
   }

   private void logReason(String reason) {
      class_2338 var2 = this.discoveryChest;
      if (var2 != null && mc.field_1724 != null) {
         this.discoverySkippedUntilAge.put(new AutoWarden.ChestKey(this.currentAnarchy, var2), mc.field_1724.field_6012 - -241 - 1);
         this.sequenceTracker.recordEvent("discovery_route_unreachable", this.currentAnarchy, var2, -1L);
      }

      LOGGER.warn("Could not load unrecorded chest {} on /an{}: {}; selecting another coordinate", new Object[]{var2, this.currentAnarchy, reason});
      this.logPlayer9();
      this.repathDelay = 0;
      this.noTargetTicks = 0;
   }

   private void logPlayer9() {
      this.discoveryChest = null;
      this.discoveryNoGoalAttempts = 0;
      this.movementGoal = null;
      this.navigator.stop();
   }

   private void logPlayer8() {
      this.logPreferredChest(null);
   }

   private void logPreferredChest(class_2338 preferredChest) {
      if (this.movementGoal != null) {
         double var2 = mc.field_1724.method_23317() - ((double)this.movementGoal.method_10263() + 0.5);
         double var4 = mc.field_1724.method_23321() - ((double)this.movementGoal.method_10260() + 0.5);
         if (var2 * var2 + var4 * var4 <= 2.25) {
            this.logPlayer10();
         }
      } else if (this.navigator.isWorking()) {
         this.navigator.stop();
      }

      if (this.movementGoal == null && this.repathDelay <= 0) {
         class_2338 var7 = this.findOrigin(mc.field_1724.method_24515(), ThreadLocalRandom.current(), preferredChest);
         if (var7 == null) {
            if (this.discoveryChest != null
               && preferredChest != null
               && preferredChest.equals(this.discoveryChest)
               && (this.discoveryNoGoalAttempts = this.discoveryNoGoalAttempts - -2 - 1) >= 3) {
               this.logReason("no reachable progress point toward the coordinate");
            } else if (preferredChest != null
               && this.timedRouteRecoveryChest != null
               && this.targetChest != null
               && preferredChest.equals(this.timedRouteRecoveryChest)
               && preferredChest.equals(this.targetChest)
               && (this.timedRecoveryNoPatrolAttempts = this.timedRecoveryNoPatrolAttempts - -2 - 1) >= 5) {
               AutoWarden.ChestKey var3 = this.targetKey;
               class_2338 var8 = this.targetChest;
               long var5 = this.logKey(var3);
               if (var3 != null) {
                  this.skippedUntilAge.put(var3, mc.field_1724.field_6012 - -401 - 1);
               }

               this.sequenceTracker.recordEvent("timed_route_unreachable", this.currentAnarchy, var8, var5);
               this.navigator.stop();
               this.collectWorld();
               this.state = AutoWarden.CycleState.SEARCHING;
               this.noTargetTicks = 100;
               this.repathDelay = 0;
               LOGGER.warn("No reachable recovery patrol exists for {}; trying another chest/anarchy instead of standing", var8);
            } else {
               this.repathDelay = 8;
            }
         } else {
            this.discoveryNoGoalAttempts = 0;
            this.timedRecoveryNoPatrolAttempts = 0;
            this.movementGoal = var7;
            this.searchPatrolIndex = this.searchPatrolIndex - -2 - 1;
            if (this.state == AutoWarden.CycleState.PVP_WAIT) {
               LOGGER.info("PvP cooldown movement toward {}", var7);
            } else if (this.discoveryChest != null && this.discoveryChest.equals(preferredChest)) {
               LOGGER.info("Loading unrecorded chest {} on /an{} via reachable point {}", new Object[]{this.discoveryChest, this.currentAnarchy, var7});
            } else {
               LOGGER.info("Loading remembered chest {} on /an{} via reachable point {}", new Object[]{preferredChest, this.currentAnarchy, var7});
            }

            if (!this.navigator.navigate(var7, 0, true)) {
               this.movementGoal = null;
               this.repathDelay = 40;
            } else {
               this.repathDelay = 8;
            }
         }
      }
   }

   private class_2338 findOrigin(class_2338 origin, ThreadLocalRandom random, class_2338 preferredChest) {
      class_2338 var4 = this.logOrigin(origin);
      if (var4 == null) {
         return null;
      } else {
         boolean var5 = this.isWorld2();
         ArrayDeque var6 = new ArrayDeque();
         HashSet<class_2338> var7 = new HashSet<>();
         ArrayList<class_2338> var8 = new ArrayList<>();
         class_2350[] var9 = new class_2350[]{class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034};
         int[] var10 = new int[]{0, 1, -1};
         int var11 = random.nextInt(var9.length);
         int var12 = this.discoveryChest != null && this.discoveryChest.equals(preferredChest) ? 2 : 10;

         while (!var6.isEmpty()) {
            AutoWarden.PatrolNode var13 = (AutoWarden.PatrolNode)var6.removeFirst();
            class_2338 var14 = var13.pos();
            int var15 = var13.steps();
            int var16 = var14.method_10263() + ~origin.method_10263() + 1;
            int var17 = var14.method_10260() + ~origin.method_10260() + 1;
            double var18 = (double)(var16 * var16 - ~(var17 * var17) - 1);
            if (var15 >= var12 && var18 >= (double)(var12 * var12) && var18 <= 576.0) {
               var8.add(var14);
            }

            if (var15 < 24) {
               for (int var20 = 0; var20 < var9.length; var20++) {
                  class_2350 var21 = var9[(var20 - ~var11 - 1) % var9.length];
                  class_2338 var22 = var14.method_10093(var21);

                  for (int var26 : var10) {
                     class_2338 var27 = var22.method_10069(0, var26, 0).method_10062();
                     if ((
                           !var5
                              || var27.method_10263() >= -2068
                                 && var27.method_10263() <= -1923
                                 && var27.method_10260() >= -2074
                                 && var27.method_10260() <= -1931
                        )
                        && var7.add(var27)
                        && this.navigator.isWalkableNode(var27)) {
                        var6.addLast(new AutoWarden.PatrolNode(var27, var15 - -2 - 1));
                        break;
                     }
                  }
               }
            }
         }

         if (var8.isEmpty()) {
            return null;
         } else if (preferredChest != null) {
            var8.sort(
               Comparator.comparingDouble(preferredChest::method_10262)
                  .thenComparingInt(class_2382::method_10264)
                  .thenComparingInt(class_2382::method_10263)
                  .thenComparingInt(class_2382::method_10260)
            );
            if (this.discoveryChest != null && this.discoveryChest.equals(preferredChest)) {
               class_2338 var29 = var8.getFirst();
               return this.logFrom(var29, preferredChest) + 4.0 >= this.logFrom(origin, preferredChest) ? null : var29;
            } else {
               int var28 = Math.min(48, var8.size());
               return var8.get(Math.floorMod(this.searchPatrolIndex, var28));
            }
         } else {
            return var8.get(random.nextInt(var8.size()));
         }
      }
   }

   private AutoWardenSequenceTracker.TimerPrediction logLoadedChests2(List<class_2338> loadedChests) {
      for (AutoWardenSequenceTracker.TimerPrediction var3 : this.sequenceTracker.predictionsFor(this.currentAnarchy)) {
         AutoWarden.ChestKey var4 = new AutoWarden.ChestKey(this.currentAnarchy, var3.pos());
         if (!this.isKey(var4)
            && this.openAttempts.getOrDefault(var4, 0) < 1
            && (!loadedChests.contains(var3.pos()) || this.findChest10(var3.pos()) && !this.findChest2(var3.pos()) && !this.findChest6(var3.pos()))) {
            return var3;
         }
      }

      return null;
   }

   private class_2338 logOrigin(class_2338 origin) {
      if (this.navigator.isWalkableNode(origin)) {
         return origin.method_10062();
      } else {
         for (int var2 = 1; var2 <= 8; var2++) {
            class_2338 var3 = origin.method_10087(var2);
            if (this.navigator.isWalkableNode(var3)) {
               return var3.method_10062();
            }
         }

         for (class_2350 var5 : new class_2350[]{class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034}) {
            class_2338 var6 = origin.method_10093(var5);
            if (this.navigator.isWalkableNode(var6)) {
               return var6.method_10062();
            }
         }

         return null;
      }
   }

   private void logPlayer10() {
      this.navigator.stop();
      this.movementGoal = null;
      this.repathDelay = 0;
   }

   private void logChest(class_2338 chest) {
      if (!chest.equals(this.targetChest)) {
         this.navigator.stop();
         this.discoveryChest = null;
         this.discoveryNoGoalAttempts = 0;
         this.collectWorld2();
         this.targetChest = chest.method_10062();
         this.targetKey = this.logChest2(this.targetChest);
         this.movementGoal = null;
         this.targetSelectedAge = mc.field_1724.field_6012;
         this.noTargetTicks = 0;
         this.stateTicks = 0;
         this.state = AutoWarden.CycleState.SEARCHING;
         this.findPos();
         long var2 = this.logKey(this.targetKey);
         this.sequenceTracker.recordEvent("target_selected", this.currentAnarchy, this.targetChest, var2);
         if (var2 < 0L) {
            LOGGER.info("Walking directly to loaded chest {} without a recorded timer on /an{}", this.targetChest, this.currentAnarchy);
         } else {
            LOGGER.info("Walking directly to ready-by-arrival chest {} on /an{} ({}ms remaining)", new Object[]{this.targetChest, this.currentAnarchy, var2});
         }
      }
   }

   private boolean findChests4(List<class_2338> chests) {
      return this.targetChest != null
         && this.targetKey != null
         && chests.contains(this.targetChest)
         && !this.isKey(this.targetKey)
         && !this.findChest6(this.targetChest)
         && this.findChest10(this.targetChest)
         && (this.logKey(this.targetKey) >= 0L ? this.openAttempts.getOrDefault(this.targetKey, 0) < 1 : this.openAttempts.getOrDefault(this.targetKey, 0) < 3);
   }

   private void findChest5(class_2338 chest, boolean force) {
      List var3 = this.findChest4(chest);
      if (var3.isEmpty()) {
         this.logPlayer5();
      } else {
         int var4 = this.movementGoal != null && var3.contains(this.movementGoal) ? 0 : 1;
         this.movementGoal = (class_2338)var3.getFirst();
         if (!this.navigator.isWorking() || force || var4 != 0) {
            this.navigator.navigateToAny(var3, !force && var4 == 0);
         }

         this.state = AutoWarden.CycleState.WALKING;
      }
   }

   private List<class_2338> findChest4(class_2338 chest) {
      ArrayList<class_2338> var2 = new ArrayList<>();

      for (int var3 = -1; var3 <= 1; var3++) {
         for (int var4 = -1; var4 <= 1; var4++) {
            if (Math.abs(var3) - ~Math.abs(var4) - 1 == 1) {
               class_2338 var5 = chest.method_10069(var3, 0, var4);
               if (this.navigator.isWalkableNode(var5) && this.findStand(var5, chest)) {
                  var2.add(var5.method_10062());
               }
            }
         }
      }

      if (var2.isEmpty()) {
         class_2338 var6 = chest.method_10084();
         if (this.navigator.isWalkableNode(var6) && this.findStand(var6, chest)) {
            var2.add(var6.method_10062());
         }
      }

      var2.sort(Comparator.comparingDouble(pos -> mc.field_1724.method_24515().method_10262(pos)));
      return List.copyOf(var2);
   }

   private boolean findChest8(class_2338 chest) {
      if (chest != null && mc.field_1724 != null) {
         class_2338 var2 = mc.field_1724.method_24515();

         for (class_2338 var4 : this.findChest4(chest)) {
            if (var4.equals(var2)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean findChest10(class_2338 chest) {
      return !this.findChest4(chest).isEmpty();
   }

   private class_2338 findChest3(class_2338 chest) {
      List var2 = this.findChest4(chest);
      return var2.isEmpty() ? null : (class_2338)var2.getFirst();
   }

   private boolean findStand(class_2338 stand, class_2338 chest) {
      class_243 var3 = class_243.method_24953(stand).method_1031(0.0, (double)mc.field_1724.method_18381(mc.field_1724.method_18376()) - 0.5, 0.0);
      return this.findEye(var3, chest) == null;
   }

   private class_243 findEye(class_243 eye, class_2338 chest) {
      class_243 var3 = class_243.method_24953(chest);
      class_243 var4 = null;
      double var5 = Double.MAX_VALUE;

      for (double var7 = -0.4; var7 <= 0.41; var7 += 0.4) {
         for (double var9 = -0.4; var9 <= 0.41; var9 += 0.4) {
            for (double var11 = -0.4; var11 <= 0.41; var11 += 0.4) {
               class_243 var13 = var3.method_1031(var7, var9, var11);
               class_3965 var14 = mc.field_1687.method_17742(new class_3959(eye, var13, class_3960.field_17558, class_242.field_1348, mc.field_1724));
               double var15 = var13.method_1025(var3);
               if (var14.method_17783() == class_240.field_1332 && this.findIntended(chest, var14.method_17777()) && var15 < var5) {
                  var5 = var15;
                  var4 = var13;
               }
            }
         }
      }

      return var4;
   }

   private void findRemaining(long remaining) {
      int var3 = remaining >= 0L ? 1 : 3;
      int var4 = this.openAttempts.getOrDefault(this.targetKey, 0);
      if (var4 >= var3) {
         this.collectWorld();
      } else {
         boolean var5 = this.findPos7(this.targetChest);
         if (var5 || this.stateTicks >= 12) {
            class_3965 var6 = this.findPos2(this.targetChest);
            if (var6 == null) {
               this.findChest5(this.targetChest, true);
            } else {
               this.currentChestLootTaken = false;
               this.emptyChestHoldUntilAge = remaining >= 0L
                  ? mc.field_1724.field_6012 - ~Math.max(0, (int)Math.ceil((double)remaining / 50.0)) - 1 - -41 - 1
                  : -1;
               this.collectPlayer2();
               mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, var6);
               mc.field_1724.method_6104(class_1268.field_5808);
               this.sequenceTracker.recordEvent("open_attempt", this.currentAnarchy, this.targetChest, remaining);
               this.openAttempts.merge(this.targetKey, 1, Integer::sum);
               this.interactionSent = true;
               this.stateTicks = 0;
               this.actionDelay = 2;
               this.state = AutoWarden.CycleState.OPENING;
            }
         }
      }
   }

   private class_3965 findPos2(class_2338 pos) {
      if (pos != null && mc.field_1724 != null && mc.field_1687 != null) {
         class_243 var2 = mc.field_1724.method_33571();
         if (mc.field_1765 instanceof class_3965 var3
            && var3.method_17783() == class_240.field_1332
            && this.findIntended(pos, var3.method_17777())
            && var2.method_1025(var3.method_17784()) <= 18.0625) {
            return var3;
         }

         ArrayList<class_243> var9 = new ArrayList<>();
         class_243 var10 = pos.method_46558();
         var9.add(var10);

         for (class_2350 var8 : class_2350.values()) {
            var9.add(var10.method_1031((double)var8.method_10148() * 0.49, (double)var8.method_10164() * 0.49, (double)var8.method_10165() * 0.49));
         }

         var9.sort(Comparator.comparingDouble(var2::method_1025));

         for (class_243 var12 : var9) {
            class_3965 var13 = mc.field_1687.method_17742(new class_3959(var2, var12, class_3960.field_17558, class_242.field_1348, mc.field_1724));
            if (var13.method_17783() == class_240.field_1332
               && this.findIntended(pos, var13.method_17777())
               && var2.method_1025(var13.method_17784()) <= 18.0625) {
               return var13;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean findIntended(class_2338 intended, class_2338 hit) {
      if (intended.equals(hit)) {
         return true;
      } else {
         return this.isPos(intended) && this.isPos(hit) && intended.method_10264() == hit.method_10264()
            ? Math.abs(intended.method_10263() + ~hit.method_10263() + 1) - ~Math.abs(intended.method_10260() + ~hit.method_10260() + 1) - 1 != 1
            : false;
      }
   }

   private void updateHandler(class_1707 handler) {
      if (this.stateTicks >= 100) {
         this.collectStack3();
      } else if (this.actionDelay <= 0) {
         int var2 = handler.method_7629().method_5439();
         this.calculateHandler2(handler, var2);
         class_1735 var3 = null;
         int var4 = Integer.MAX_VALUE;

         for (int var5 = 0; var5 < Math.min(var2, handler.field_7761.size()); var5++) {
            class_1735 var6 = (class_1735)handler.field_7761.get(var5);
            if (!var6.method_7677().method_7960() && this.containsStack3(var6.method_7677())) {
               int var7 = this.collectStack(var6.method_7677());
               if (var7 < var4) {
                  var3 = var6;
                  var4 = var7;
               }
            }
         }

         if (var3 == null) {
            if (this.currentChestLootTaken || this.emptyChestHoldUntilAge < 0 || mc.field_1724.field_6012 > this.emptyChestHoldUntilAge) {
               this.collectStack3();
            }
         } else if (!this.isIncoming(var3.method_7677())) {
            this.collectStack3();
         } else {
            mc.field_1761.method_2906(handler.field_7763, var3.field_7874, 0, class_1713.field_7794, mc.field_1724);
            this.currentChestLootTaken = true;
            this.actionDelay = 1;
         }
      }
   }

   public void onContainerContentsUpdated(int syncId) {
      if (this.isEnabled()
         && !this.currentChestLootTaken
         && this.targetChest != null
         && mc.field_1724 != null
         && mc.field_1724.field_7512 instanceof class_1707 var2
         && var2.field_7763 == syncId
         && (this.state == AutoWarden.CycleState.OPENING || this.state == AutoWarden.CycleState.LOOTING)) {
         this.actionDelay = 0;
         this.updateHandler(var2);
      }
   }

   private void calculateHandler2(class_1707 handler, int chestSlots) {
      if (!this.currentChestLootSnapshotRecorded && this.targetChest != null) {
         ArrayList var3 = new ArrayList();

         for (int var4 = 0; var4 < Math.min(chestSlots, handler.field_7761.size()); var4++) {
            class_1799 var5 = ((class_1735)handler.field_7761.get(var4)).method_7677();
            if (!var5.method_7960()) {
               var3.add(
                  new AutoWardenLootTracker.ObservedLoot(
                     class_7923.field_41178.method_10221(var5.method_7909()).toString(),
                     var5.method_7964().getString(),
                     var5.method_7947(),
                     this.collectStack(var5)
                  )
               );
            }
         }

         if (!var3.isEmpty()) {
            this.currentChestLootSnapshotRecorded = true;
            this.lootTracker.recordSnapshot(this.currentAnarchy, this.targetChest, var3);
         }
      }
   }

   private int collectStack(class_1799 stack) {
      String var2 = stack.method_7964().getString().toLowerCase(Locale.ROOT);
      String var3 = this.collectStack2(stack);
      String var10000;
      if (var3 == null) {
         var10000 = var2;
      } else {
         String var6 = var3.toLowerCase(Locale.ROOT);
         var10000 = var2 + " " + var6;
      }

      if (var10000.contains("отмыч")) {
         return 0;
      } else if (var10000.contains("сфер")) {
         return 1;
      } else if (var10000.contains("талисман")) {
         return 2;
      } else {
         int var5 = !stack.method_31574(class_1802.field_8574)
               && !stack.method_31574(class_1802.field_8436)
               && !stack.method_31574(class_1802.field_8150)
               && !var10000.contains("зелье")
               && !var10000.contains("снотворное")
               && !var10000.contains("святая вода")
               && !var10000.contains("кислота")
            ? 0
            : 1;
         if ((var3 == null || var5 != 0) && !stack.method_31574(class_1802.field_42716) && !var10000.contains("модификатор")) {
            return var5 != 0 ? 4 : 5;
         } else {
            return 3;
         }
      }
   }

   private String collectStack2(class_1799 stack) {
      return stack == null ? null : stack.method_65130().getString();
   }

   private void collectStack3() {
      this.logWorld3();
      this.logPlayer4();
   }

   private boolean containsStack3(class_1799 stack) {
      return stack == null || stack.method_7960() || this.containsStack6(stack) || this.containsStack(stack);
   }

   private boolean containsStack6(class_1799 stack) {
      return stack.method_31574(class_1802.field_37523)
         || stack.method_31574(class_1802.field_8639)
         || stack.method_31574(class_1802.field_8466)
         || stack.method_31574(class_1802.field_38746)
         || stack.method_31574(class_1802.field_8815)
         || stack.method_31574(class_1802.field_8634)
         || stack.method_31574(class_1802.field_28101)
         || stack.method_31574(class_1802.field_37525)
         || stack.method_31574(class_1802.field_8547);
   }

   private boolean containsStack(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      } else if (this.containsStack6(stack)) {
         return true;
      } else if (class_7923.field_41178.method_10221(stack.method_7909()).method_12832().startsWith("music_disc_")) {
         return true;
      } else {
         String var2 = stack.method_7964().getString().toLowerCase(Locale.ROOT);
         if (stack.method_31574(class_1802.field_42716) || var2.contains("модификатор")) {
            return false;
         } else if (stack.method_31574(class_1802.field_8107)
            || stack.method_31574(class_1802.field_8236)
            || stack.method_31574(class_1802.field_8087)
            || stack.method_7909() instanceof class_1821
            || stack.method_7909() instanceof class_1794
            || stack.method_7909() instanceof class_1753
            || stack.method_7909() instanceof class_1743) {
            return true;
         } else if (stack.method_7909() instanceof class_1826
            && !stack.method_31574(class_1802.field_8086)
            && !stack.method_31574(class_1802.field_8136)
            && !stack.method_31574(class_1802.field_8503)) {
            return true;
         } else {
            String var3 = class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
            return !var3.endsWith("_armor_trim_smithing_template")
                  && !var3.endsWith("_horse_armor")
                  && !var3.equals("book")
                  && !var3.endsWith("_book")
                  && (!var2.contains("сингуляр") || !var2.contains("скин"))
                  && !var2.contains("хлопушка")
               ? !stack.method_31574(class_1802.field_20391)
                  && !stack.method_31574(class_1802.field_8153)
                  && !stack.method_31574(class_1802.field_8539)
                  && !stack.method_31574(class_1802.field_8745)
                  && !stack.method_31574(class_1802.field_8606)
                  && !stack.method_31574(class_1802.field_8276)
                  && !stack.method_31574(class_1802.field_8711)
                  && !stack.method_31574(class_1802.field_8536)
                  && !stack.method_31574(class_1802.field_8233)
                  && !stack.method_31574(class_1802.field_8719)
                  && !stack.method_31574(class_1802.field_8140)
                  && !stack.method_31574(class_1802.field_8448)
                  && !stack.method_31574(class_1802.field_8864)
                  && !stack.method_31574(class_1802.field_8469)
                  && !stack.method_31574(class_1802.field_8777)
                  && !stack.method_31574(class_1802.field_8620)
                  && !stack.method_31574(class_1802.field_8695)
                  && !stack.method_31574(class_1802.field_8675)
                  && !stack.method_31574(class_1802.field_8397)
                  && !stack.method_31574(class_1802.field_33400)
                  && !stack.method_31574(class_1802.field_33402)
                  && !stack.method_31574(class_1802.field_8773)
                  && !stack.method_31574(class_1802.field_8494)
                  && !stack.method_31574(class_1802.field_33505)
                  && !stack.method_31574(class_1802.field_33507)
                  && !stack.method_31574(class_1802.field_8782)
                  && !stack.method_31574(class_1802.field_8750)
                  && !stack.method_31574(class_1802.field_8427)
                  && !stack.method_31574(class_1802.field_8687)
                  && !stack.method_31574(class_1802.field_8894)
                  && !stack.method_31574(class_1802.field_8070)
                  && !stack.method_31574(class_1802.field_8301)
                  && !stack.method_31574(class_1802.field_8657)
                  && !stack.method_31574(class_1802.field_17515)
                  && !stack.method_31574(class_1802.field_20417)
                  && !stack.method_31574(class_1802.field_8323)
               : true;
         }
      }
   }

   private boolean isPlayer() {
      if (mc.field_1724 != null
         && mc.field_1761 != null
         && !this.automationContainerOpened
         && this.activeConsumable == null
         && (mc.field_1755 == null || this.automationInventoryOpened)
         && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
         int var1 = -1;

         for (int var2 = 0; var2 < 36; var2++) {
            if (this.containsStack(mc.field_1724.method_31548().method_5438(var2))) {
               var1 = var2;
               break;
            }
         }

         if (var1 < 0 && this.immediateDiscardCooldown <= 0) {
            this.immediateDiscardStopping = false;
            this.immediateDiscardSettleTicks = 0;
            return false;
         } else {
            this.immediateDiscardStopping = true;
            this.navigator.pauseMovement();
            this.suppressSprint();
            mc.field_1724.method_5728(false);
            if (mc.field_1724.field_3913 != null) {
               mc.field_1724.field_3913.field_54155 = class_10185.field_54098;
            }

            class_243 var6 = mc.field_1724.method_18798();
            mc.field_1724.method_18800(0.0, var6.field_1351, 0.0);
            if (this.immediateDiscardCooldown > 0) {
               this.immediateDiscardCooldown = this.immediateDiscardCooldown + -2 + 1;
               return true;
            } else if (this.immediateDiscardSettleTicks < 3) {
               this.immediateDiscardSettleTicks = this.immediateDiscardSettleTicks - -2 - 1;
               return true;
            } else if (var1 >= 0) {
               class_1799 var3 = mc.field_1724.method_31548().method_5438(var1);
               class_1799 var4 = var3.method_7972();
               int var5 = var1 < 9 ? 36 - ~var1 - 1 : var1;
               mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, var5, 1, class_1713.field_7795, mc.field_1724);
               if (var1 < 9) {
                  this.pickedHotbarSlots.remove(var1);
                  if (var1 < this.ignoredLooseHotbarStacks.length) {
                     this.ignoredLooseHotbarStacks[var1] = null;
                  }
               }

               this.immediateDiscardSettleTicks = 0;
               this.immediateDiscardCooldown = 1;
               LOGGER.info(
                  "Immediately discarded filtered AutoWarden loot slot={} item={} count={} name='{}'",
                  new Object[]{var1, class_7923.field_41178.method_10221(var4.method_7909()), var4.method_7947(), var4.method_7964().getString()}
               );
               return true;
            } else {
               return true;
            }
         }
      } else {
         this.immediateDiscardStopping = false;
         this.immediateDiscardSettleTicks = 0;
         return false;
      }
   }

   private boolean isIncoming(class_1799 incoming) {
      for (int var2 = 0; var2 < 36; var2++) {
         class_1799 var3 = mc.field_1724.method_31548().method_5438(var2);
         if (var3.method_7960()) {
            return true;
         }

         if (class_1799.method_31577(var3, incoming) && var3.method_7947() < var3.method_7914()) {
            return true;
         }
      }

      return false;
   }

   private boolean isWorld() {
      if (this.isList2() && !this.interactionSent && !this.returnHomePending && mc.field_1724 != null && mc.field_1687 != null) {
         this.ignoredGroundLootUntilAge.entrySet().removeIf(entry -> mc.field_1724.field_6012 >= entry.getValue());
         List<class_1542> var1 = mc.field_1687
            .method_8390(
               class_1542.class,
               mc.field_1724.method_5829().method_1009(18.0, 8.0, 18.0),
               item -> !item.method_5805() || item.method_31481() || item.method_6983().method_7960()
            );
         class_1542 var2 = null;
         if (this.groundLootTargetId != null) {
            for (class_1542 var4 : var1) {
               if (this.groundLootTargetId.equals(var4.method_5667()) && this.findItem(var4)) {
                  var2 = var4;
                  break;
               }
            }

            if (var2 == null) {
               boolean var7 = this.isWorld4();
               LOGGER.info(
                  "Ground loot {} {}; resuming chest route",
                  this.groundLootTargetId,
                  var7 ? "was collected by AutoWarden" : "disappeared without entering inventory"
               );
               if (var7) {
                  this.logWorld8();
               }

               this.logTemporarilyIgnore(var7);
               if (this.returnHomePending) {
                  return false;
               }
            }
         }

         if (var2 == null) {
            var2 = var1.stream()
               .filter(this::findItem)
               .filter(item -> this.ignoredGroundLootUntilAge.getOrDefault(item.method_5667(), 0) > mc.field_1724.field_6012)
               .min(
                  Comparator.<class_1542>comparingInt(item -> this.collectStack(item.method_6983()))
                     .thenComparingDouble(it -> mc.field_1724.method_5707(it.method_73189()))
               )
               .orElse(null);
            if (var2 == null) {
               return false;
            }

            this.groundLootTargetId = var2.method_5667();
            this.groundLootLastPos = null;
            this.groundLootExpectedStack = var2.method_6983().method_7972();
            this.groundLootInventoryCountBefore = this.logExpected(this.groundLootExpectedStack);
            this.groundLootStartedAge = mc.field_1724.field_6012;
            this.groundLootRepathAge = 0;
            this.navigator.stop();
            this.movementGoal = null;
            this.repathDelay = 0;
            LOGGER.info(
               "Chasing useful dropped loot item={} count={} name='{}' at {}",
               new Object[]{
                  class_7923.field_41178.method_10221(var2.method_6983().method_7909()),
                  var2.method_6983().method_7947(),
                  var2.method_6983().method_7964().getString(),
                  var2.method_24515()
               }
            );
         }

         if (mc.field_1724.field_6012 + ~this.groundLootStartedAge + 1 >= 120) {
            LOGGER.info("Dropped loot {} could not be collected within {} ticks; resuming farming", this.groundLootTargetId, 120);
            this.logTemporarilyIgnore(true);
            return false;
         } else {
            class_2338 var8 = var2.method_24515().method_10062();
            int var9 = this.groundLootLastPos != null && this.groundLootLastPos.equals(var8) ? 0 : 1;
            int var5 = !this.navigator.isWorking() && !this.navigator.hasArrived() ? 1 : 0;
            if (var9 != 0 || var5 != 0 || mc.field_1724.field_6012 >= this.groundLootRepathAge) {
               List var6 = this.logItem(var2);
               if (var6.isEmpty()) {
                  LOGGER.info("No reachable pickup cell for dropped loot {} at {}", this.groundLootTargetId, var8);
                  this.logTemporarilyIgnore(true);
                  return false;
               }

               if (var9 != 0 || var5 != 0) {
                  this.navigator.stop();
                  if (!this.navigator.navigateToAny(var6, true)) {
                     this.logTemporarilyIgnore(true);
                     return false;
                  }
               }

               this.groundLootLastPos = var8;
               this.groundLootRepathAge = mc.field_1724.field_6012 - -11 - 1;
            }

            this.navigator.tick();
            if (this.navigator.hasFailed()) {
               LOGGER.info("Navigation to dropped loot {} failed; resuming farming", this.groundLootTargetId);
               this.logTemporarilyIgnore(true);
               return false;
            } else {
               return true;
            }
         }
      } else {
         if (this.groundLootTargetId != null) {
            this.logTemporarilyIgnore(false);
         }

         return false;
      }
   }

   private boolean findItem(class_1542 item) {
      class_1799 var2 = item.method_6983();
      return var2.method_57381(class_9334.field_50075) != null
         || var2.method_31574(class_1802.field_8287)
         || this.findItem2(item)
         || !this.containsStack3(var2)
         || !this.isIncoming(var2);
   }

   private boolean findItem2(class_1542 item) {
      class_243 var2 = item.method_73189();
      return mc.field_1687
         .method_8390(
            class_7260.class,
            item.method_5829().method_1014(5.0),
            warden -> warden.method_5805() && !warden.method_31481() && warden.method_73189().method_1025(var2) <= 25.0
         )
         .isEmpty();
   }

   private List<class_2338> logItem(class_1542 item) {
      class_2338 var2 = item.method_24515();
      HashSet var3 = new HashSet();

      for (int var4 = -1; var4 <= 1; var4++) {
         for (int var5 = -1; var5 <= 1; var5++) {
            for (int var6 = -1; var6 <= 1; var6++) {
               class_2338 var7 = var2.method_10069(var5, var4, var6);
               if (this.navigator.isWalkableNode(var7)) {
                  var3.add(var7.method_10062());
               }
            }
         }
      }

      class_243 var8 = item.method_73189();
      return var3.stream()
         .sorted(
            Comparator.<class_2338>comparingDouble(
                  pos -> new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264(), (double)pos.method_10260() + 0.5).method_1025(var8)
               )
               .thenComparingDouble(pos -> mc.field_1724.method_24515().method_10262(pos))
         )
         .limit(5L)
         .toList();
   }

   private void logTemporarilyIgnore(boolean temporarilyIgnore) {
      UUID var2 = this.groundLootTargetId;
      if (temporarilyIgnore && var2 != null && mc.field_1724 != null) {
         this.ignoredGroundLootUntilAge.put(var2, mc.field_1724.field_6012 - -201 - 1);
      }

      this.groundLootTargetId = null;
      this.groundLootLastPos = null;
      this.groundLootExpectedStack = class_1799.field_8037;
      this.groundLootInventoryCountBefore = 0;
      this.groundLootStartedAge = 0;
      this.groundLootRepathAge = 0;
      this.navigator.stop();
      this.movementGoal = null;
      this.repathDelay = 0;
   }

   private boolean isWorld4() {
      return this.groundLootExpectedStack.method_7960() || this.logExpected(this.groundLootExpectedStack) <= this.groundLootInventoryCountBefore;
   }

   private int logExpected(class_1799 expected) {
      if (mc.field_1724 != null && expected != null && !expected.method_7960()) {
         int var2 = 0;

         for (int var3 = 0; var3 < 36; var3++) {
            class_1799 var4 = mc.field_1724.method_31548().method_5438(var3);
            if (class_1799.method_31577(var4, expected)) {
               var2 = var2 - ~var4.method_7947() - 1;
            }
         }

         return var2;
      } else {
         return 0;
      }
   }

   private void logWorld8() {
      this.lootedChests = this.lootedChests - -2 - 1;
      int var1 = Math.max(1, Math.round(this.chestsBeforeHome.getValue()));
      LOGGER.info("Useful dropped loot counted toward storage: progress={}/{}", this.lootedChests, var1);
      if (this.lootedChests >= var1) {
         this.returnHomePending = true;
      }
   }

   private class_2338 findWorld6() {
      class_2338 var1 = mc.field_1724.method_24515();
      class_2338 var2 = null;
      double var3 = Double.MAX_VALUE;

      for (class_2338 var6 : class_2338.method_25996(var1, 2, 4, 2)) {
         int var7 = var6.method_10263() + ~var1.method_10263() + 1;
         int var8 = var6.method_10260() + ~var1.method_10260() + 1;
         if (var7 * var7 - ~(var8 * var8) - 1 <= 4 && this.isPos(var6) && !this.exhaustedHomeChests.contains(var6) && !this.findChest(var6)) {
            double var9 = mc.field_1724.method_33571().method_1025(var6.method_46558());
            if (var9 < var3) {
               var3 = var9;
               var2 = var6.method_10062();
            }
         }
      }

      return var2;
   }

   private class_2338 findWorld5() {
      class_2338 var1 = mc.field_1724.method_24515();
      int var2 = Math.min(3, (Integer)mc.field_1690.method_42503().method_41753());
      int var3 = var1.method_10263() >> 4;
      int var4 = var1.method_10260() >> 4;
      return this.findCenterChunkX(var3, var4, var2)
         .stream()
         .filter(pos -> this.exhaustedSupplyChests.contains(pos))
         .filter(this::findChest)
         .filter(pos -> !(var1.method_10262(pos) <= 1600.0))
         .min(Comparator.comparingDouble(var1::method_10262))
         .orElse(null);
   }

   private List<class_2338> findCenterChunkX(int centerChunkX, int centerChunkZ, int chunkRadius) {
      ArrayList var4 = new ArrayList();

      for (int var5 = centerChunkX + ~chunkRadius + 1; var5 <= centerChunkX - ~chunkRadius - 1; var5++) {
         for (int var6 = centerChunkZ + ~chunkRadius + 1; var6 <= centerChunkZ - ~chunkRadius - 1; var6++) {
            class_2818 var7 = mc.field_1687.method_2935().method_21730(var5, var6);
            if (var7 != null) {
               for (class_2338 var9 : var7.method_12214().keySet()) {
                  if (this.isPos(var9)) {
                     var4.add(var9.method_10062());
                  }
               }
            }
         }
      }

      return var4;
   }

   private boolean findChest(class_2338 chest) {
      if (!this.isPos(chest)) {
         return false;
      } else {
         for (class_2338 var3 : class_2338.method_10097(chest.method_10069(-1, -1, -1), chest.method_10069(1, 2, 1))) {
            if (mc.field_1687.method_8321(var3) instanceof class_2625 var4 && this.containsSign(var4) && this.findCandidate2(chest, this.findSignPos(var3))) {
               return true;
            }
         }

         return false;
      }
   }

   private class_2338 findSignPos(class_2338 signPos) {
      class_2680 var2 = mc.field_1687.method_8320(signPos);
      if (var2.method_26204() instanceof class_2551) {
         class_2338 var3 = signPos.method_10093(((class_2350)var2.method_11654(class_2551.field_11726)).method_10153());
         if (this.isPos(var3)) {
            return var3.method_10062();
         }
      } else if (var2.method_26204() instanceof class_7715) {
         class_2338 var10 = signPos.method_10093(((class_2350)var2.method_11654(class_7715.field_40319)).method_10153());
         if (this.isPos(var10)) {
            return var10.method_10062();
         }
      }

      class_2338 var11 = null;
      double var4 = Double.MAX_VALUE;

      for (class_2338 var7 : class_2338.method_25996(signPos, 2, 2, 2)) {
         if (this.isPos(var7)) {
            double var8 = signPos.method_10262(var7);
            if (var8 < var4) {
               var11 = var7.method_10062();
               var4 = var8;
            }
         }
      }

      return var11;
   }

   private boolean findCandidate2(class_2338 candidate, class_2338 assigned) {
      if (assigned == null) {
         return false;
      } else if (candidate.equals(assigned)) {
         return true;
      } else {
         class_2680 var3 = mc.field_1687.method_8320(assigned);
         return !(var3.method_26204() instanceof class_2281)
            || var3.method_11654(class_2281.field_10770) == class_2745.field_12569
            || !assigned.method_10093(class_2281.method_9758(var3)).equals(candidate);
      }
   }

   private boolean containsSign(class_2625 sign) {
      String var5 = this.collectText(sign.method_49854());
      String var6 = this.collectText(sign.method_49853());
      String var2 = this.calculateText(var6 + " " + var5);

      for (String var4 : this.collectPlayer()) {
         if (!var4.isBlank() && var2.contains(var4)) {
            return true;
         }
      }

      return false;
   }

   private String collectText(class_8242 text) {
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < 4; var3++) {
         var2.append(' ').append(text.method_49859(var3, false).getString());
      }

      return var2.toString();
   }

   private List<String> collectPlayer() {
      ArrayList var1 = new ArrayList();
      String var2 = this.supplySign.getValue();
      if (var2 != null) {
         for (String var6 : var2.split("[,;\\s]+")) {
            String var7 = this.calculateText(var6);
            if (!var7.isBlank()) {
               var1.add(var7);
            }
         }
      }

      if (var1.isEmpty()) {
         var1.add("снабжение");
      }

      return var1;
   }

   private String calculateText(String text) {
      return text == null ? "" : text.replaceAll("§.", "").toLowerCase(Locale.ROOT).trim();
   }

   private boolean isPlayer6() {
      if (this.autoSupplies.getValue() && mc.field_1724 != null) {
         for (AutoWarden.SupplyKind var4 : AutoWarden.SupplyKind.values()) {
            if (this.isKind3(var4)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean isPlayer7() {
      if (this.autoSupplies.getValue() && mc.field_1724 != null) {
         for (AutoWarden.SupplyKind var4 : AutoWarden.SupplyKind.values()) {
            if (this.isKind2(var4) && this.collectKind(var4) <= 0 && this.isKind4(var4)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private AutoWarden.SupplyKind calculatePlayer3() {
      for (AutoWarden.SupplyKind var4 : AutoWarden.SupplyKind.values()) {
         if (this.isKind3(var4) && !this.unavailableSupplyKinds.contains(var4)) {
            return var4;
         }
      }

      return null;
   }

   private boolean isKind3(AutoWarden.SupplyKind kind) {
      return !this.isKind2(kind) || this.collectKind(kind) >= this.calculateKind2(kind) || kind != AutoWarden.SupplyKind.FOOD && !this.isKind4(kind);
   }

   private boolean isKind4(AutoWarden.SupplyKind kind) {
      return switch (kind) {
         case FOOD -> mc.field_1724.method_7344().method_7586() < 20;
         case INVISIBILITY -> this.deathPotionRecovery || this.containsEffect(class_1294.field_5905);
         case SPEED -> this.deathPotionRecovery || this.containsEffect(class_1294.field_5904);
      };
   }

   private void calculateStack2() {
      if (!this.deathPotionRecovery && this.farmEffectSyncGraceTicks <= 0 && this.currentAnarchy != null && !"?".equals(this.currentAnarchy)) {
         this.calculateEffect(class_1294.field_5905, AutoWarden.SupplyKind.INVISIBILITY);
         this.calculateEffect(class_1294.field_5904, AutoWarden.SupplyKind.SPEED);
      }
   }

   private void calculateEffect(class_6880<class_1291> effect, AutoWarden.SupplyKind kind) {
      class_1293 var3 = mc.field_1724.method_6112(effect);
      if (var3 != null) {
         long var4 = var3.method_48559() ? Long.MAX_VALUE : System.currentTimeMillis() - ~(Math.max(0L, (long)var3.method_5584()) * 50L) - 1L;
         this.calculateKind(kind).put(this.currentAnarchy, var4);
      }
   }

   private Map<String, Long> calculateKind(AutoWarden.SupplyKind kind) {
      return kind == AutoWarden.SupplyKind.INVISIBILITY ? this.rememberedInvisibilityUntilByAnarchy : this.rememberedSpeedUntilByAnarchy;
   }

   private String calculateStack() {
      return this.isList3() && this.farmAnarchyBeforeStorage != null ? this.farmAnarchyBeforeStorage : this.currentAnarchy;
   }

   private boolean isKind2(AutoWarden.SupplyKind kind) {
      return switch (kind) {
         case FOOD -> this.takeFood.getValue();
         case INVISIBILITY -> this.takeInvisibility.getValue();
         case SPEED -> this.takeSpeed.getValue();
      };
   }

   private int calculateKind2(AutoWarden.SupplyKind kind) {
      return Math.max(1, Math.round(switch (kind) {
         case FOOD -> this.foodAmount.getValue();
         case INVISIBILITY -> this.invisibilityAmount.getValue();
         case SPEED -> this.speedAmount.getValue();
      }));
   }

   private int collectKind(AutoWarden.SupplyKind kind) {
      int var2 = 0;

      for (int var3 = 0; var3 < 9; var3++) {
         class_1799 var4 = mc.field_1724.method_31548().method_5438(var3);
         if (this.containsStack2(var4, kind)) {
            var2 = var2 - ~var4.method_7947() - 1;
         }
      }

      return var2;
   }

   private boolean containsStack4(class_1799 stack) {
      if (stack != null && !stack.method_7960() && !stack.method_31574(class_1802.field_42716)) {
         for (AutoWarden.SupplyKind var5 : AutoWarden.SupplyKind.values()) {
            if (this.isKind2(var5) && this.containsStack2(stack, var5)) {
               return true;
            }
         }

         return false;
      } else {
         return stack != null && !stack.method_7960();
      }
   }

   private boolean containsStack5(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_10192 var2 = (class_10192)stack.method_57381(class_9334.field_54196);
         if (var2 == null) {
            return false;
         } else {
            class_1304 var3 = var2.comp_3174();
            return var3 == class_1304.field_6169 || var3 == class_1304.field_6174 || var3 == class_1304.field_6172 || var3 == class_1304.field_6166;
         }
      } else {
         return false;
      }
   }

   private boolean containsStack2(class_1799 stack, AutoWarden.SupplyKind kind) {
      if (stack != null && !stack.method_7960()) {
         return switch (kind) {
            case FOOD -> stack.method_57381(class_9334.field_50075) != null
            && !((class_4174)stack.method_57381(class_9334.field_50075)).comp_2493()
            && !this.calculateText(stack.method_7964().getString()).contains("пласт");
            case INVISIBILITY -> this.isStack2(stack, class_1294.field_5905, "невид", "инвиз", "invis");
            case SPEED -> this.isStack2(stack, class_1294.field_5904, "скорост", "speed");
         };
      } else {
         return false;
      }
   }

   private boolean isStack2(class_1799 stack, class_6880<class_1291> effect, String... nameFallbacks) {
      if (!stack.method_31574(class_1802.field_8574)) {
         return false;
      } else {
         class_1844 var4 = (class_1844)stack.method_57381(class_9334.field_49651);
         if (var4 != null) {
            for (class_1293 var6 : var4.method_57397()) {
               if (var6.method_5579().equals(effect)) {
                  return true;
               }
            }
         }

         String var10 = this.calculateText(stack.method_7964().getString());

         for (String var9 : nameFallbacks) {
            if (var10.contains(var9)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean isPlayer5() {
      if (this.consumableReleaseTicks > 0) {
         mc.field_1690.field_1904.method_23481(false);
         this.consumableReleaseTicks = this.consumableReleaseTicks + -2 + 1;
         return true;
      } else if (this.activeConsumable != null) {
         return this.matchesPlayer();
      } else if (this.autoSupplies.getValue() && mc.field_1755 == null) {
         if (this.takeFood.getValue() && mc.field_1724.method_7344().method_7586() < 20 && this.isKind(AutoWarden.SupplyKind.FOOD)) {
            return true;
         } else {
            return this.takeInvisibility.getValue() && this.containsEffect(class_1294.field_5905) && this.isKind(AutoWarden.SupplyKind.INVISIBILITY)
               ? true
               : this.takeSpeed.getValue() && this.containsEffect(class_1294.field_5904) && this.isKind(AutoWarden.SupplyKind.SPEED);
         }
      } else {
         return false;
      }
   }

   private boolean isKind(AutoWarden.SupplyKind kind) {
      int var2 = this.collectKind2(kind);
      if (var2 < 0) {
         return false;
      } else {
         this.consumablePreviousSlot = mc.field_1724.method_31548().method_67532();
         this.consumableHotbarSlot = var2;
         mc.field_1724.method_31548().method_61496(var2);
         this.activeConsumable = kind;
         this.consumableUseDelay = 2;
         this.consumableTicks = 0;
         this.consumableInteractCooldown = 0;
         LOGGER.info("Pausing AutoWarden to use {} from hotbar slot {}", kind, var2);
         return true;
      }
   }

   private boolean matchesPlayer() {
      if (mc.field_1755 == null && this.containsKind(this.activeConsumable)) {
         if (this.consumableHotbarSlot >= 0
            && this.containsStack2(mc.field_1724.method_31548().method_5438(this.consumableHotbarSlot), this.activeConsumable)
            && (this.consumableTicks = this.consumableTicks - -2 - 1) <= 140) {
            mc.field_1724.method_31548().method_61496(this.consumableHotbarSlot);
            if (mc.field_1724.method_24828()) {
               mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455() - 7.0F, -45.0F, 90.0F));
            }

            int var10000 = this.consumableUseDelay;
            this.consumableUseDelay = this.consumableUseDelay + -2 + 1;
            if (var10000 > 0) {
               return true;
            } else {
               mc.field_1690.field_1904.method_23481(true);
               if (!mc.field_1724.method_6115()) {
                  if (this.consumableInteractCooldown <= 0) {
                     mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                     this.consumableInteractCooldown = 10;
                  } else {
                     this.consumableInteractCooldown = this.consumableInteractCooldown + -2 + 1;
                  }
               } else {
                  this.consumableInteractCooldown = 10;
               }

               return true;
            }
         } else {
            this.findWorld3();
            return this.consumableReleaseTicks <= 0;
         }
      } else {
         this.findWorld3();
         return this.consumableReleaseTicks <= 0;
      }
   }

   private boolean containsEffect(class_6880<class_1291> effect) {
      AutoWarden.SupplyKind var2 = effect.equals(class_1294.field_5905) ? AutoWarden.SupplyKind.INVISIBILITY : AutoWarden.SupplyKind.SPEED;
      String var3 = this.calculateStack();
      class_1293 var4 = mc.field_1724.method_6112(effect);
      long var5 = this.calculateKind(var2).getOrDefault(var3, 0L);
      if (this.farmEffectSyncGraceTicks > 0) {
         if (var4 != null && this.activeConsumable == var2 && var3.equals(this.currentAnarchy)) {
            this.calculateEffect(effect, var2);
            return var4.method_48559() || var4.method_5584() > 200;
         } else {
            return var5 > System.currentTimeMillis() - -10001L - 1L;
         }
      } else {
         return var4 == null ? var5 > System.currentTimeMillis() - -10001L - 1L : var4.method_48559() || var4.method_5584() > 200;
      }
   }

   private boolean containsKind(AutoWarden.SupplyKind kind) {
      switch (kind) {
         case FOOD:
            if (mc.field_1724.method_7344().method_7586() >= 20) {
               return true;
            }
            break;
         case INVISIBILITY:
            if (!this.containsEffect(class_1294.field_5905)) {
               return true;
            }
            break;
         case SPEED:
            if (!this.containsEffect(class_1294.field_5904)) {
               return true;
            }
            break;
         default:
            throw new MatchException(null, null);
      }

      return false;
   }

   private int collectKind2(AutoWarden.SupplyKind kind) {
      for (int var2 = 0; var2 < 9; var2++) {
         if (this.containsStack2(mc.field_1724.method_31548().method_5438(var2), kind)) {
            return var2;
         }
      }

      return -1;
   }

   private void collectPlayer2() {
      if (mc.field_1724 == null) {
         this.logPlayer13();
      } else {
         for (int var1 = 0; var1 < this.lootHotbarSnapshot.length; var1++) {
            this.lootHotbarSnapshot[var1] = mc.field_1724.method_31548().method_5438(var1).method_7972();
         }

         this.lootHotbarSnapshotActive = true;
      }
   }

   private void logPlayer13() {
      for (int var1 = 0; var1 < this.lootHotbarSnapshot.length; var1++) {
         this.lootHotbarSnapshot[var1] = null;
      }

      this.lootHotbarSnapshotActive = false;
   }

   private void logPlayer11() {
      if (this.lootHotbarSnapshotActive && mc.field_1724 != null) {
         for (int var1 = 0; var1 < this.lootHotbarSnapshot.length; var1++) {
            class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
            class_1799 var3 = this.lootHotbarSnapshot[var1];
            if (!var2.method_7960() && !this.containsStack4(var2) && !this.containsStack5(var2)) {
               int var4 = var3 != null && !var3.method_7960() ? 0 : 1;
               int var5 = var4 == 0 && !class_1799.method_31577(var3, var2) ? 1 : 0;
               int var6 = var4 == 0 && var5 == 0 && var2.method_7947() > var3.method_7947() ? 1 : 0;
               if (var4 != 0 || var5 != 0 || var6 != 0) {
                  this.pickedHotbarSlots.add(var1);
                  this.hotbarCleanupPending = true;
               }
            }
         }
      }
   }

   private void logPlayer() {
      if (mc.field_1724 != null && !this.automationInventoryOpened && !this.hotbarCleanupPending && this.pickedHotbarSlots.isEmpty()) {
         for (int var1 = 0; var1 < 8; var1++) {
            class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
            if (var2.method_7960()) {
               this.ignoredLooseHotbarStacks[var1] = null;
            } else {
               class_1799 var3 = this.ignoredLooseHotbarStacks[var1];
               if (var3 == null || var3.method_7947() != var2.method_7947() || !class_1799.method_31577(var3, var2)) {
                  this.ignoredLooseHotbarStacks[var1] = null;
                  if (!this.containsStack4(var2) && !this.containsStack5(var2)) {
                     this.pickedHotbarSlots.add(var1);
                     this.hotbarCleanupPending = true;
                  }
               }
            }
         }
      }
   }

   private void logSlot(int slot) {
      if (mc.field_1724 != null && slot >= 0 && slot <= 8 && mc.field_1724.method_31548().method_67532() != slot) {
         mc.field_1724.method_31548().method_61496(slot);
      }
   }

   private boolean isStack() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         class_1799 var1 = mc.field_1724.method_31548().method_5438(8);
         int var2 = !var1.method_7960() && !this.containsStack5(var1) ? 1 : 0;
         int var3 = !this.hotbarCleanupPending && this.pickedHotbarSlots.isEmpty() && var2 == 0 && !this.automationInventoryOpened ? 0 : 1;
         if (var3 == 0) {
            return false;
         } else if (this.activeConsumable != null || this.automationContainerOpened) {
            return false;
         } else if (!this.automationInventoryOpened) {
            if (mc.field_1755 != null) {
               return false;
            } else {
               this.navigator.pauseMovement();
               mc.method_1507(new class_490(mc.field_1724));
               this.automationInventoryOpened = true;
               this.hotbarCleanupDelay = 3;
               LOGGER.info("Opened player inventory to move Warden chest loot from hotbar {}", this.pickedHotbarSlots);
               return true;
            }
         } else if (mc.field_1755 instanceof class_490 && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
            this.navigator.pauseMovement();
            int var10000 = this.hotbarCleanupDelay;
            this.hotbarCleanupDelay = this.hotbarCleanupDelay + -2 + 1;
            if (var10000 > 0) {
               return true;
            } else {
               for (int var5 : new ArrayList<>(this.pickedHotbarSlots)) {
                  class_1799 var6 = mc.field_1724.method_31548().method_5438(var5);
                  if (!var6.method_7960() && !this.containsStack4(var6) && !this.containsStack5(var6)) {
                     if (var5 >= 0 && var5 < this.ignoredLooseHotbarStacks.length) {
                        this.ignoredLooseHotbarStacks[var5] = var6.method_7972();
                     }

                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, 36 - ~var5 - 1, 0, class_1713.field_7794, mc.field_1724);
                     LOGGER.info("Moved Warden loot from hotbar slot {} into main inventory", var5);
                     this.pickedHotbarSlots.remove(var5);
                     this.hotbarCleanupDelay = 3;
                     return true;
                  }

                  this.pickedHotbarSlots.remove(var5);
               }

               class_1799 var7 = mc.field_1724.method_31548().method_5438(8);
               if (!var7.method_7960() && !this.containsStack5(var7)) {
                  int var9 = this.logPlayer2();
                  if (var9 >= 0) {
                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, 44, var9, class_1713.field_7791, mc.field_1724);
                     LOGGER.info("Moved ninth-slot stack to empty hotbar slot {}", var9);
                  } else {
                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, 44, 0, class_1713.field_7794, mc.field_1724);
                     LOGGER.info("Moved ninth-slot stack into main inventory");
                  }

                  this.hotbarCleanupDelay = 3;
                  return true;
               } else {
                  this.hotbarCleanupPending = false;
                  this.findWorld();
                  int var8 = this.logPlayer7();
                  if (var8 >= 0) {
                     this.logSlot(var8);
                  }

                  this.screenResumeDelay = 2;
                  LOGGER.info("Warden hotbar cleanup completed");
                  return true;
               }
            }
         } else {
            this.automationInventoryOpened = false;
            return false;
         }
      } else {
         return false;
      }
   }

   private int logPlayer2() {
      for (int var1 = 0; var1 < 8; var1++) {
         if (mc.field_1724.method_31548().method_5438(var1).method_7960()) {
            return var1;
         }
      }

      return -1;
   }

   private int logPlayer7() {
      if (mc.field_1724 == null) {
         return -1;
      } else if (mc.field_1724.method_31548().method_5438(8).method_7960()) {
         return 8;
      } else {
         for (int var1 = 0; var1 < 8; var1++) {
            if (mc.field_1724.method_31548().method_5438(var1).method_7960()) {
               return var1;
            }
         }

         return -1;
      }
   }

   private void logPlayer16() {
      if (mc.field_1724 != null && this.activeConsumable == null) {
         int var1 = mc.field_1724.method_31548().method_67532();
         class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
         if (!var2.method_7960()) {
            int var3 = this.logPlayer7();
            if (var3 < 0) {
               if (!this.containsStack4(var2) && !this.containsStack5(var2)) {
                  this.pickedHotbarSlots.add(var1);
                  this.hotbarCleanupPending = true;
               }
            } else {
               this.logSlot(var3);
               LOGGER.info("Switched occupied hand slot {} to empty hotbar slot {}", var1, var3);
            }
         }
      }
   }

   private void findWorld() {
      if (this.automationInventoryOpened && mc.field_1724 != null && mc.field_1755 instanceof class_490) {
         mc.field_1724.method_3137();
         mc.method_1507(null);
      }

      this.automationInventoryOpened = false;
      this.hotbarCleanupDelay = 0;
   }

   private void findWorld7() {
      this.pickedHotbarSlots.clear();

      for (int var1 = 0; var1 < this.ignoredLooseHotbarStacks.length; var1++) {
         this.ignoredLooseHotbarStacks[var1] = null;
      }

      this.hotbarCleanupPending = false;
      this.automationInventoryOpened = false;
      this.hotbarCleanupDelay = 0;
      this.immediateDiscardCooldown = 0;
      this.immediateDiscardSettleTicks = 0;
      this.immediateDiscardStopping = false;
      this.screenResumeDelay = 0;
      this.logPlayer13();
   }

   private void findWorld3() {
      int var1 = this.activeConsumable != null || mc.field_1724 != null && mc.field_1724.method_6115() ? 1 : 0;
      if (mc.field_1690 != null) {
         mc.field_1690.field_1904.method_23481(false);
      }

      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1724.method_6115()) {
         mc.field_1761.method_2897(mc.field_1724);
      }

      this.activeConsumable = null;
      if (mc.field_1724 != null) {
         int var2 = this.logPlayer7();
         if (var2 >= 0) {
            mc.field_1724.method_31548().method_61496(var2);
         } else if (this.consumablePreviousSlot >= 0 && this.consumablePreviousSlot < 9) {
            mc.field_1724.method_31548().method_61496(this.consumablePreviousSlot);
         }
      }

      this.consumablePreviousSlot = -1;
      this.consumableHotbarSlot = -1;
      this.consumableUseDelay = 0;
      this.consumableTicks = 0;
      this.consumableInteractCooldown = 0;
      this.consumableReleaseTicks = var1 != 0 ? 2 : 0;
   }

   private List<class_2338> findWorld2() {
      ArrayList var1 = new ArrayList();
      if (mc.field_1687 != null && mc.field_1687.method_27983().equals(class_1937.field_25179)) {
         for (int var2 = -130; var2 <= -121; var2++) {
            for (int var3 = -130; var3 <= -121; var3++) {
               class_2818 var4 = mc.field_1687.method_2935().method_21730(var2, var3);
               if (var4 != null) {
                  for (class_2338 var6 : var4.method_12214().keySet()) {
                     if (this.isPos2(var6) && this.isPos(var6)) {
                        var1.add(var6.method_10062());
                     }
                  }
               }
            }
         }

         return var1;
      } else {
         return var1;
      }
   }

   private void findChests(List<class_2338> chests) {
      if (chests.isEmpty()) {
         this.sequenceTracker.flush();
      } else {
         for (class_1531 var3 : mc.field_1687.method_8390(class_1531.class, mc.field_1724.method_5829().method_1014(256.0), entity -> true)) {
            Matcher var4 = TIMER_PATTERN.matcher(var3.method_5477().getString());
            if (var4.find()) {
               long var5 = (Long.parseLong(var4.group(1)) * 60L - ~Long.parseLong(var4.group(2)) - 1L) * 1000L;
               class_2338 var7 = this.logChests2(chests, var3.method_24515());
               if (var7 != null) {
                  this.sequenceTracker.observeTimer(this.currentAnarchy, var7, var5);
                  AutoWarden.ChestKey var8 = this.logChest2(var7);
                  AutoWarden.ChestTimer var9 = this.chestTimers.get(var8);
                  if (var9 == null) {
                     this.chestTimers.put(var8, new AutoWarden.ChestTimer(var5));
                     this.openAttempts.remove(var8);
                  } else if (var9.findCurrentMs(var5)) {
                     this.openAttempts.remove(var8);
                     this.skippedUntilAge.remove(var8);
                  }
               }
            }
         }

         for (AutoWarden.ChestKey var12 : this.chestTimers.entrySet().stream().filter(entry -> entry.getValue().findPos4() <= 0L).map(Entry::getKey).toList()) {
            this.sequenceTracker.recordTimerReady(var12.anarchy(), var12.pos());
            this.chestTimers.remove(var12);
         }

         this.sequenceTracker.flush();
      }
   }

   private class_2338 logChests2(List<class_2338> chests, class_2338 stand) {
      class_2338 var3 = null;
      int var4 = Integer.MAX_VALUE;

      for (class_2338 var6 : chests) {
         if (var6.method_10263() == stand.method_10263() && var6.method_10260() == stand.method_10260()) {
            int var7 = Math.abs(var6.method_10264() + ~stand.method_10264() + 1);
            if (var7 < var4) {
               var4 = var7;
               var3 = var6;
            }
         }
      }

      return var3;
   }

   private long logKey(AutoWarden.ChestKey key) {
      if (key == null) {
         return -1L;
      } else {
         AutoWarden.ChestTimer var2 = this.chestTimers.get(key);
         return var2 == null ? this.sequenceTracker.rememberedRemainingMs(key.anarchy(), key.pos()) : var2.findPos4();
      }
   }

   private void logWorld2() {
      for (class_7260 var2 : mc.field_1687.method_8390(class_7260.class, mc.field_1724.method_5829().method_1014(256.0), entity -> true)) {
         this.recentWardens.put(var2.method_24515().method_10062(), mc.field_1724.field_6012 - -101 - 1);
      }

      this.recentWardens.entrySet().removeIf(entry -> mc.field_1724.field_6012 <= entry.getValue());
   }

   private boolean isWorld5() {
      if (mc.field_1724 != null
         && mc.field_1687 != null
         && this.isWorld2()
         && (this.isList2() || this.state == AutoWarden.CycleState.PVP_WAIT || this.fleeingWarden)) {
         List<class_7260> var1 = mc.field_1687
            .method_8390(class_7260.class, mc.field_1724.method_5829().method_1014(64.0), warden -> !warden.method_5805() || warden.method_31481());
         List<class_7260> var2 = var1.stream().filter(this::findWarden).toList();
         class_7260 var3 = var2.stream().min(Comparator.comparingDouble(w -> mc.field_1724.method_5707(w.method_73189()))).orElse(null);
         boolean var4 = var3 != null;
         class_243 var5 = var3 != null ? var3.method_73189() : (this.wardenFleeThreatPos == null ? null : class_243.method_24953(this.wardenFleeThreatPos));
         double var6 = var5 == null ? Double.POSITIVE_INFINITY : mc.field_1724.method_73189().method_1025(var5);
         if (!this.fleeingWarden && !var4) {
            return false;
         } else {
            if (!this.fleeingWarden) {
               this.fleeingWarden = true;
               this.wardenFleeClearTicks = 0;
               this.wardenFleeRepathTicks = 0;
               this.findWorld3();
               this.logWorld3();
               this.findWorld();
               this.findWorld7();
               this.navigator.stop();
               this.movementGoal = null;
               if (this.state != AutoWarden.CycleState.PVP_WAIT) {
                  this.collectWorld();
                  this.state = AutoWarden.CycleState.SEARCHING;
                  this.stateTicks = 0;
                  this.noTargetTicks = 0;
               }

               LOGGER.warn(
                  "AutoWarden escaping Warden targeting this player at {}: distance={} blocks, anger={}",
                  new Object[]{
                     var5 == null ? "unknown" : class_2338.method_49637(var5.field_1352, var5.field_1351, var5.field_1350),
                     var5 == null ? -1L : Math.round(Math.sqrt(var6)),
                     var3 == null ? -1 : var3.method_42222()
                  }
               );
            }

            if (!var4) {
               this.wardenFleeClearTicks = this.wardenFleeClearTicks - -2 - 1;
               if (this.wardenFleeClearTicks >= 20) {
                  this.navigator.stop();
                  this.findWorld8();
                  this.movementGoal = null;
                  this.repathDelay = 0;
                  this.noTargetTicks = 0;
                  if (this.state != AutoWarden.CycleState.PVP_WAIT) {
                     this.state = AutoWarden.CycleState.SEARCHING;
                     this.stateTicks = 0;
                  }

                  LOGGER.info("Warden no longer targets this player; AutoWarden resumed farming");
                  return false;
               }
            } else {
               this.wardenFleeClearTicks = 0;
            }

            if (this.wardenFleeRepathTicks > 0) {
               this.wardenFleeRepathTicks = this.wardenFleeRepathTicks + -2 + 1;
            }

            class_2338 var8 = var5 == null ? null : class_2338.method_49637(var5.field_1352, var5.field_1351, var5.field_1350);
            int var9 = var8 != null && (this.wardenFleeThreatPos == null || var8.method_10262(this.wardenFleeThreatPos) >= 9.0) ? 1 : 0;
            if (var4 && this.wardenFleeRepathTicks <= 0 && (!this.navigator.isWorking() || var9 != 0)) {
               ArrayList var10 = new ArrayList();

               for (class_7260 var12 : var2) {
                  var10.add(var12.method_73189());
               }

               if (var10.isEmpty() && this.wardenFleeThreatPos != null) {
                  var10.add(class_243.method_24953(this.wardenFleeThreatPos));
               }

               List var13 = this.findOrigin2(mc.field_1724.method_24515(), var10);
               this.navigator.stop();
               if (!var13.isEmpty() && this.navigator.navigateToAny(var13, true)) {
                  this.wardenFleeThreatPos = var8;
                  this.wardenFleeRepathTicks = 12;
                  LOGGER.info("AutoWarden planned Warden escape toward {} safe candidates", var13.size());
               } else {
                  this.wardenFleeRepathTicks = 6;
                  LOGGER.warn("AutoWarden could not find a reachable Warden escape goal at {}", mc.field_1724.method_24515());
               }
            }

            this.navigator.tick();
            if (this.navigator.hasFailed()) {
               this.navigator.stop();
               this.wardenFleeRepathTicks = 0;
            }

            return true;
         }
      } else {
         this.findWorld8();
         return false;
      }
   }

   private boolean findWarden(class_7260 warden) {
      return warden.method_5968() == mc.field_1724 || warden.method_42215().orElse(null) == mc.field_1724;
   }

   private List<class_2338> findOrigin2(class_2338 origin, List<class_243> threats) {
      if (threats.isEmpty()) {
         return List.of();
      } else {
         class_2338 var3 = this.logOrigin(origin);
         if (var3 == null) {
            return List.of();
         } else {
            ArrayDeque var4 = new ArrayDeque();
            HashSet var5 = new HashSet();
            ArrayList var6 = new ArrayList();
            ArrayList var7 = new ArrayList();
            var4.addLast(new AutoWarden.PatrolNode(var3, 0));
            var5.add(var3);
            double var8 = this.findPos3(var3, threats);
            class_2350[] var10 = new class_2350[]{class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034};
            int[] var11 = new int[]{0, 1, -1};

            while (!var4.isEmpty()) {
               AutoWarden.PatrolNode var12 = (AutoWarden.PatrolNode)var4.removeFirst();
               class_2338 var13 = var12.pos();
               int var14 = var12.steps();
               double var15 = this.findPos3(var13, threats);
               if (var14 >= 9 && var15 > var8 + 9.0) {
                  var7.add(var12);
                  if (var15 >= 900.0) {
                     var6.add(var12);
                  }
               }

               if (var14 < 26) {
                  for (class_2350 var20 : var10) {
                     class_2338 var21 = var13.method_10093(var20);

                     for (int var25 : var11) {
                        class_2338 var26 = var21.method_10069(0, var25, 0).method_10062();
                        if (var26.method_10263() >= -2068
                           && var26.method_10263() <= -1923
                           && var26.method_10260() >= -2074
                           && var26.method_10260() <= -1931
                           && var5.add(var26)
                           && this.navigator.isWalkableNode(var26)) {
                           var4.addLast(new AutoWarden.PatrolNode(var26, var14 - -2 - 1));
                           break;
                        }
                     }
                  }
               }
            }

            ArrayList var27 = var6.isEmpty() ? var7 : var6;
            var27.sort(
               Comparator.<AutoWarden.PatrolNode>comparingDouble(node -> this.findPos3(node.pos(), threats))
                  .reversed()
                  .thenComparingInt(AutoWarden.PatrolNode::steps)
            );
            int var28 = Math.min(32, var27.size());
            ArrayList var29 = new ArrayList(var28);

            for (int var30 = 0; var30 < var28; var30++) {
               var29.add(((AutoWarden.PatrolNode)var27.get(var30)).pos());
            }

            return List.copyOf(var29);
         }
      }
   }

   private double findPos3(class_2338 pos, List<class_243> threats) {
      double var3 = (double)pos.method_10263() + 0.5;
      double var5 = (double)pos.method_10260() + 0.5;
      double var7 = Double.POSITIVE_INFINITY;

      for (class_243 var10 : threats) {
         double var11 = var3 - var10.field_1352;
         double var13 = var5 - var10.field_1350;
         var7 = Math.min(var7, var11 * var11 + var13 * var13);
      }

      return var7;
   }

   private void findWorld8() {
      this.fleeingWarden = false;
      this.wardenFleeThreatPos = null;
      this.wardenFleeRepathTicks = 0;
      this.wardenFleeClearTicks = 0;
   }

   private boolean findChest2(class_2338 chest) {
      for (class_2338 var3 : this.recentWardens.keySet()) {
         if (var3.method_10262(chest) < 25.0) {
            return true;
         }
      }

      return false;
   }

   private boolean findChest6(class_2338 chest) {
      class_243 var2 = class_243.method_24953(chest);

      for (class_1657 var4 : mc.field_1687.method_18456()) {
         if (var4 != mc.field_1724 && !(var4.method_73189().method_1025(var2) >= 20.0)) {
            for (class_1304 var6 : List.of(class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166)) {
               class_1799 var7 = var4.method_6118(var6);
               class_10192 var8 = (class_10192)var7.method_57381(class_9334.field_54196);
               if (var8 != null && var8.comp_3174() == var6) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean findPos5(class_2338 pos, double range) {
      double var4 = range * range;
      class_243 var6 = class_243.method_24953(pos);

      for (class_1657 var8 : mc.field_1687.method_18456()) {
         if (var8 != mc.field_1724 && var8.method_73189().method_1025(var6) < var4) {
            return true;
         }
      }

      return false;
   }

   private boolean isWorld2() {
      return mc.field_1687.method_27983().equals(class_1937.field_25179)
         && mc.field_1724.method_23317() >= -2070.0
         && mc.field_1724.method_23317() <= -1921.0
         && mc.field_1724.method_23321() >= -2076.0
         && mc.field_1724.method_23321() <= -1929.0;
   }

   private boolean isPos2(class_2338 pos) {
      return pos.method_10263() < -2070
         || pos.method_10263() > -1921
         || pos.method_10264() < -60
         || pos.method_10264() > -35
         || pos.method_10260() < -2076
         || pos.method_10260() > -1929;
   }

   private boolean isPos(class_2338 pos) {
      return pos != null
         && mc.field_1687 != null
         && (mc.field_1687.method_8320(pos).method_27852(class_2246.field_10034) || mc.field_1687.method_8320(pos).method_27852(class_2246.field_10380));
   }

   private boolean isKey(AutoWarden.ChestKey key) {
      return this.skippedUntilAge.getOrDefault(key, 0) > mc.field_1724.field_6012;
   }

   private AutoWarden.ChestKey logChest2(class_2338 chest) {
      return new AutoWarden.ChestKey(this.currentAnarchy, chest.method_10062());
   }

   private void logPlayer5() {
      if (this.targetChest != null) {
         this.sequenceTracker.recordEvent("target_failed", this.currentAnarchy, this.targetChest, this.logKey(this.targetKey));
      }

      if (this.targetKey != null) {
         this.skippedUntilAge.put(this.targetKey, mc.field_1724.field_6012 - -201 - 1);
      }

      this.navigator.stop();
      this.collectWorld();
      this.state = AutoWarden.CycleState.SEARCHING;
   }

   private void logPlayer4() {
      boolean var1 = this.currentChestLootTaken;
      long var2 = this.logKey(this.targetKey);
      this.logPlayer11();
      if (this.targetChest != null) {
         this.sequenceTracker.recordEvent("loot_complete", this.currentAnarchy, this.targetChest, this.logKey(this.targetKey));
         if (var1) {
            this.sequenceTracker.rememberLooted(this.currentAnarchy, this.targetChest, 660000L);
         } else {
            this.sequenceTracker.recordEvent("empty_chest", this.currentAnarchy, this.targetChest, var2);
         }

         if (this.targetKey != null && var1) {
            this.chestTimers.put(this.targetKey, new AutoWarden.ChestTimer(660000L));
            this.openAttempts.remove(this.targetKey);
            this.skippedUntilAge.remove(this.targetKey);
         } else if (this.targetKey != null) {
            long var4 = var2 > 0L ? var2 - -751L - 1L : 10000L;
            int var6 = Math.max(1, (int)Math.ceil((double)var4 / 50.0));
            this.openAttempts.remove(this.targetKey);
            this.skippedUntilAge.put(this.targetKey, mc.field_1724.field_6012 - ~var6 - 1);
         }
      }

      this.navigator.stop();
      this.collectWorld();
      this.state = AutoWarden.CycleState.SEARCHING;
      if (!var1) {
         LOGGER.info("Opened Warden chest contained no collectable loot; keeping chest counter at {}", this.lootedChests);
      } else {
         this.lootedChests = this.lootedChests - -2 - 1;
         if (this.lootedChests >= Math.max(1, Math.round(this.chestsBeforeHome.getValue()))) {
            this.returnHomePending = true;
         }
      }
   }

   private void logPlayer12() {
      if (!this.deathRecoveryPending) {
         String var1 = this.targetKey != null ? this.targetKey.anarchy() : this.currentAnarchy;
         AutoWarden.ChestKey var2 = this.targetKey;
         this.deathResumeAnarchy = this.findFrom(var1);
         if (this.deathResumeAnarchy != null && this.deathResumeAnarchy.equals(var1)) {
            this.deathResumeAnarchy = null;
         }

         if (var2 != null) {
            this.chestTimers.remove(var2);
            this.openAttempts.remove(var2);
            this.skippedUntilAge.remove(var2);
            this.sequenceTracker.forgetTimerAfterDeath(var2.anarchy(), var2.pos());
         }

         LOGGER.info(
            "AutoWarden detected death on /an{} at target {}; cleared the old target and will resume on /an{} after supply",
            new Object[]{var1, var2 == null ? "none" : var2.pos(), this.deathResumeAnarchy}
         );
         this.rememberedInvisibilityUntilByAnarchy.clear();
         this.rememberedSpeedUntilByAnarchy.clear();
         this.deathPotionRecovery = true;
         this.deathRecoveryPending = true;
         this.returnHomePending = false;
         this.findWorld3();
         this.logWorld3();
         this.findWorld();
         this.findWorld7();
         this.navigator.stop();
         this.collectWorld();
      }
   }

   private void logWorld5() {
      LOGGER.info("AutoWarden player is alive; starting death recovery");
      this.deathRecoveryPending = false;
      this.lootedChests = 0;
      this.returnHomePending = false;
      this.initialFarmRoutePending = false;
      this.initialSupplyPending = false;
      this.outsideCityTicks = 0;
      this.noTargetTicks = 0;
      String var1 = this.deathResumeAnarchy;
      this.deathResumeAnarchy = null;
      this.logAdvanceFarmAnarchy(false, var1);
   }

   private void collectWorld() {
      this.collectWorld2();
      this.discoveryChest = null;
      this.discoveryNoGoalAttempts = 0;
      this.currentChestLootTaken = false;
      this.currentChestLootSnapshotRecorded = false;
      this.emptyChestHoldUntilAge = -1;
      this.targetKey = null;
      this.targetChest = null;
      this.movementGoal = null;
      this.targetSelectedAge = 0;
      this.stateTicks = 0;
      this.interactionSent = false;
      this.logPlayer13();
      this.findPos();
   }

   private void collectWorld2() {
      this.timedRouteRecoveryChest = null;
      this.timedRouteRecoveryUntilAge = 0;
      this.timedRecoveryNoPatrolAttempts = 0;
   }

   private void collectClearTimers(boolean clearTimers) {
      this.navigator.stop();
      this.findWorld3();
      this.logWorld3();
      this.findWorld();
      this.findWorld7();
      this.collectWorld();
      this.recentWardens.clear();
      this.findWorld8();
      this.skippedUntilAge.clear();
      this.discoverySkippedUntilAge.clear();
      this.openAttempts.clear();
      if (clearTimers) {
         this.chestTimers.clear();
         this.rememberedInvisibilityUntilByAnarchy.clear();
         this.rememberedSpeedUntilByAnarchy.clear();
         this.deathPotionRecovery = false;
         this.initialEffectSyncPending = true;
      }

      this.currentAnarchy = "?";
      this.worldIdentity = mc.field_1687;
      this.state = AutoWarden.CycleState.SEARCHING;
      this.actionDelay = 0;
      this.automationContainerOpened = false;
      this.interactionSent = false;
      this.currentChestLootTaken = false;
      this.lootedChests = 0;
      this.returnHomePending = false;
      this.homeCommandPos = null;
      this.homeCommandWorld = null;
      this.homeTeleportConfirmed = false;
      this.homePurpose = AutoWarden.HomePurpose.STORAGE;
      this.expectedAnarchy = null;
      this.deferredAnarchy = null;
      this.deferredStorageTransfer = null;
      this.deferredHomePurpose = null;
      this.pvpClearTicks = 0;
      this.farmAnarchyBeforeStorage = null;
      this.initialFarmRoutePending = true;
      if (clearTimers) {
         this.initialSupplyPending = this.autoSupplies.getValue();
      }

      this.serverDeathMessagePending = false;
      this.deathRecoveryPending = false;
      this.deathResumeAnarchy = null;
      this.outsideCityTicks = 0;
      this.noTargetTicks = 0;
      this.farmEffectSyncGraceTicks = 200;
      this.emergencyRestockTicks = 0;
      this.searchPatrolIndex = 0;
      this.logPlayer3();
      this.homeChest = null;
      this.supplyChest = null;
      this.depositSlot = 0;
      this.scanDelay = 0;
      this.repathDelay = 0;
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
   }

   private void logWorld3() {
      if (mc.field_1724 != null && mc.field_1724.field_7512 instanceof class_1707) {
         mc.field_1724.method_3137();
         mc.method_1507(null);
      }

      this.automationContainerOpened = false;
   }

   private void logWorld4() {
      String var1 = this.findWorld4();
      if (var1 != null && !var1.equals(this.currentAnarchy)) {
         String var2 = this.currentAnarchy;
         this.currentAnarchy = var1;
         if (this.initialEffectSyncPending) {
            this.initialEffectSyncPending = false;
            this.farmEffectSyncGraceTicks = 0;
         } else {
            this.farmEffectSyncGraceTicks = 200;
            LOGGER.info("Effect context changed /an{} -> /an{}; using isolated potion memory", var2, this.currentAnarchy);
         }

         this.navigator.stop();
         this.collectWorld();
         this.findWorld8();
      }
   }

   private String findWorld4() {
      class_269 var1 = mc.field_1687.method_8428();
      class_266 var2 = null;
      class_268 var3 = var1.method_1164(mc.field_1724.method_5820());
      if (var3 != null) {
         class_8646 var4 = class_8646.method_52622(var3.method_1202());
         if (var4 != null) {
            var2 = var1.method_1189(var4);
         }
      }

      if (var2 == null) {
         var2 = var1.method_1189(class_8646.field_45157);
      }

      if (var2 == null) {
         return null;
      } else {
         String var8 = this.findText(var2.method_1114().getString());
         if (var8 != null) {
            return var8;
         } else {
            for (class_9011 var6 : var1.method_1184(var2)) {
               String var7 = class_268.method_1142(var1.method_1164(var6.comp_2127()), var6.method_55387()).getString();
               var8 = this.findText(var7);
               if (var8 != null) {
                  return var8;
               }
            }

            return null;
         }
      }
   }

   private String findText(String text) {
      if (text != null && !text.isBlank()) {
         Matcher var2 = ANARCHY_PATTERN.matcher(text);
         return var2.find() ? var2.group(1) : null;
      } else {
         return null;
      }
   }

   private boolean findPos7(class_2338 pos) {
      if (pos == null) {
         this.findPos();
         return false;
      } else if (!mc.field_1724.method_24828()) {
         this.preciseAimYawVelocity = 0.0F;
         this.preciseAimPitchVelocity = 0.0F;
         return false;
      } else {
         if (!pos.equals(this.preciseAimBlock)) {
            this.findPos();
            this.preciseAimBlock = pos.method_10062();
         }

         ThreadLocalRandom var2 = ThreadLocalRandom.current();
         if (!this.preciseAimInitialized) {
            this.preciseAimInitialized = true;
            this.preciseAimProfileTicks = var2.nextInt(18, 33);
            this.preciseAimYawSpeed = var2.nextFloat(10.5F, 13.5F);
            this.preciseAimPitchSpeed = var2.nextFloat(7.5F, 10.5F);
            this.preciseAimYawSpeedTarget = this.preciseAimYawSpeed;
            this.preciseAimPitchSpeedTarget = this.preciseAimPitchSpeed;
            this.findRandom(var2);
         } else if ((this.preciseAimProfileTicks = this.preciseAimProfileTicks + -2 + 1) <= 0) {
            this.preciseAimYawSpeedTarget = var2.nextFloat(10.0F, 15.0F);
            this.preciseAimPitchSpeedTarget = var2.nextFloat(7.0F, 11.5F);
            this.findRandom(var2);
            this.preciseAimProfileTicks = var2.nextInt(18, 33);
         }

         this.preciseAimYawSpeed = this.preciseAimYawSpeed + class_3532.method_15363(this.preciseAimYawSpeedTarget - this.preciseAimYawSpeed, -0.25F, 0.25F);
         this.preciseAimPitchSpeed = this.preciseAimPitchSpeed
            + class_3532.method_15363(this.preciseAimPitchSpeedTarget - this.preciseAimPitchSpeed, -0.2F, 0.2F);
         this.preciseAimOffsetX = this.preciseAimOffsetX + class_3532.method_15350(this.preciseAimOffsetXTarget - this.preciseAimOffsetX, -0.012, 0.012);
         this.preciseAimOffsetY = this.preciseAimOffsetY + class_3532.method_15350(this.preciseAimOffsetYTarget - this.preciseAimOffsetY, -0.01, 0.01);
         this.preciseAimOffsetZ = this.preciseAimOffsetZ + class_3532.method_15350(this.preciseAimOffsetZTarget - this.preciseAimOffsetZ, -0.012, 0.012);
         class_243 var3 = pos.method_46558().method_1031(this.preciseAimOffsetX, this.preciseAimOffsetY, this.preciseAimOffsetZ);
         class_243 var4 = mc.field_1724.method_33571();
         double var5 = var3.field_1352 - var4.field_1352;
         double var7 = var3.field_1351 - var4.field_1351;
         double var9 = var3.field_1350 - var4.field_1350;
         double var11 = Math.sqrt(var5 * var5 + var9 * var9);
         float var13 = (float)Math.toDegrees(Math.atan2(var9, var5)) - 90.0F;
         float var14 = (float)(-Math.toDegrees(Math.atan2(var7, var11)));
         float var15 = class_3532.method_15393(var13 - mc.field_1724.method_36454());
         float var16 = var14 - mc.field_1724.method_36455();
         float var17 = this.findError(var15, this.preciseAimYawSpeed, 34.0F) * 2.0F;
         float var18 = this.findError(var16, this.preciseAimPitchSpeed, 24.0F) * 2.0F;
         this.preciseAimYawVelocity = this.preciseAimYawVelocity + class_3532.method_15363(var17 - this.preciseAimYawVelocity, -3.5F, 3.5F);
         this.preciseAimPitchVelocity = this.preciseAimPitchVelocity + class_3532.method_15363(var18 - this.preciseAimPitchVelocity, -2.5F, 2.5F);
         float var19 = Math.abs(this.preciseAimYawVelocity) > Math.abs(var15) ? var15 : this.preciseAimYawVelocity;
         float var20 = Math.abs(this.preciseAimPitchVelocity) > Math.abs(var16) ? var16 : this.preciseAimPitchVelocity;
         mc.field_1724.method_36456(mc.field_1724.method_36454() + var19);
         mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455() + var20, -90.0F, 90.0F));
         if (var19 == var15) {
            this.preciseAimYawVelocity *= 0.35F;
         }

         if (var20 == var16) {
            this.preciseAimPitchVelocity *= 0.35F;
         }

         return !(Math.abs(var15) <= 2.25F)
            || !(Math.abs(var16) <= 1.75F)
            || !(Math.abs(this.preciseAimYawVelocity) <= 4.0F)
            || !(Math.abs(this.preciseAimPitchVelocity) <= 3.2F);
      }
   }

   private float findError(float error, float maxSpeed, float slowAngle) {
      float var4 = Math.abs(error);
      if (var4 < 0.001F) {
         return 0.0F;
      } else {
         float var5 = class_3532.method_15363(var4 / slowAngle, 0.0F, 1.0F);
         float var6 = var5 * var5 * (3.0F - 2.0F * var5);
         float var7 = Math.max(var4 * 0.3F, maxSpeed * var6);
         return Math.copySign(Math.min(var4, Math.min(maxSpeed, var7)), error);
      }
   }

   private void findRandom(ThreadLocalRandom random) {
      this.preciseAimOffsetXTarget = random.nextDouble(-0.16, 0.16);
      this.preciseAimOffsetYTarget = random.nextDouble(-0.13, 0.15);
      this.preciseAimOffsetZTarget = random.nextDouble(-0.16, 0.16);
   }

   private void findPos() {
      this.preciseAimBlock = null;
      this.preciseAimInitialized = false;
      this.preciseAimProfileTicks = 0;
      this.preciseAimYawVelocity = 0.0F;
      this.preciseAimPitchVelocity = 0.0F;
      this.preciseAimYawSpeed = 12.0F;
      this.preciseAimPitchSpeed = 9.0F;
      this.preciseAimYawSpeedTarget = 12.0F;
      this.preciseAimPitchSpeedTarget = 9.0F;
      this.preciseAimOffsetX = 0.0;
      this.preciseAimOffsetY = 0.0;
      this.preciseAimOffsetZ = 0.0;
      this.preciseAimOffsetXTarget = 0.0;
      this.preciseAimOffsetYTarget = 0.0;
      this.preciseAimOffsetZTarget = 0.0;
   }

   private class_243 findPos8(class_2338 pos) {
      return new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.12, (double)pos.method_10260() + 0.5);
   }

   private void findCenter(class_243 center, int color) {
      double var3 = 0.16;
      WorldRenderBatcher.drawLineNoDepth(center.method_1031(-var3, 0.0, 0.0), center.method_1031(var3, 0.0, 0.0), color, 2.5F);
      WorldRenderBatcher.drawLineNoDepth(center.method_1031(0.0, 0.0, -var3), center.method_1031(0.0, 0.0, var3), color, 2.5F);
   }

   private void findGoal(class_2338 goal) {
      double var2 = (double)goal.method_10263() + 0.16;
      double var4 = (double)goal.method_10263() + 0.84;
      double var6 = (double)goal.method_10264() + 0.03;
      double var8 = (double)goal.method_10264() + 0.72;
      double var10 = (double)goal.method_10260() + 0.16;
      double var12 = (double)goal.method_10260() + 0.84;
      class_243 var14 = new class_243(var2, var6, var10);
      class_243 var15 = new class_243(var4, var6, var10);
      class_243 var16 = new class_243(var4, var6, var12);
      class_243 var17 = new class_243(var2, var6, var12);
      class_243 var18 = new class_243(var2, var8, var10);
      class_243 var19 = new class_243(var4, var8, var10);
      class_243 var20 = new class_243(var4, var8, var12);
      class_243 var21 = new class_243(var2, var8, var12);
      this.findStart(var14, var15);
      this.findStart(var15, var16);
      this.findStart(var16, var17);
      this.findStart(var17, var14);
      this.findStart(var18, var19);
      this.findStart(var19, var20);
      this.findStart(var20, var21);
      this.findStart(var21, var18);
      this.findStart(var14, var18);
      this.findStart(var15, var19);
      this.findStart(var16, var20);
      this.findStart(var17, var21);
   }

   private void findStart(class_243 start, class_243 end) {
      WorldRenderBatcher.drawLineNoDepth(start, end, 1358934784, 5.0F);
      WorldRenderBatcher.drawLineNoDepth(start, end, -11713, 2.0F);
   }

   private static record ChestKey(String anarchy, class_2338 pos) {
   }

   private static final class ChestTimer {
      private long shownMs;
      private long observedAtNanos;

      private ChestTimer(long shownMs) {
         this.shownMs = shownMs;
         this.observedAtNanos = System.nanoTime();
      }

      private boolean findCurrentMs(long currentMs) {
         if (Math.abs(currentMs / 1000L + ~(this.findPos4() / 1000L) + 1L) <= 5L) {
            return false;
         } else {
            this.shownMs = currentMs;
            this.observedAtNanos = System.nanoTime();
            return true;
         }
      }

      private long findPos4() {
         long var1 = (System.nanoTime() + ~this.observedAtNanos + 1L) / 1000000L;
         return Math.max(0L, this.shownMs + ~var1 + 1L);
      }
   }

   private static enum CycleState {
      SEARCHING,
      WAITING,
      WALKING,
      OPENING,
      LOOTING,
      PVP_WAIT,
      STORAGE_ANARCHY_WAIT,
      FARM_ANARCHY_WAIT,
      HOME_WAIT,
      FIND_DEPOSIT,
      WALK_DEPOSIT,
      OPEN_DEPOSIT,
      DEPOSIT,
      FIND_SUPPLY,
      WALK_SUPPLY,
      OPEN_SUPPLY,
      TAKE_SUPPLY;
   }

   private static record FarmAnarchyPlan(String anarchy, int knownTimers, int readyByArrival, long soonestMs) {
   }

   private static enum HomePurpose {
      STORAGE,
      FARM_RETURN;
   }

   public static record MemoryChestHudEntry(String anarchy, class_2338 pos, long remainingMs, boolean currentAnarchy) {
   }

   private static record PatrolNode(class_2338 pos, int steps) {
   }

   private static enum SupplyKind {
      FOOD,
      INVISIBILITY,
      SPEED;
   }
}
