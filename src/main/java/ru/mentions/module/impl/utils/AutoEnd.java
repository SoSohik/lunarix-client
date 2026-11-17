package ru.mentions.module.impl.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10185;
import net.minecraft.class_10192;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1304;
import net.minecraft.class_1542;
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
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2551;
import net.minecraft.class_2561;
import net.minecraft.class_2625;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_2680;
import net.minecraft.class_269;
import net.minecraft.class_2745;
import net.minecraft.class_2818;
import net.minecraft.class_2828;
import net.minecraft.class_345;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4174;
import net.minecraft.class_418;
import net.minecraft.class_490;
import net.minecraft.class_6880;
import net.minecraft.class_7439;
import net.minecraft.class_7715;
import net.minecraft.class_7923;
import net.minecraft.class_8242;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
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
import ru.mentions.mixin.EntityAccessor;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.render.WorldRenderBatcher;

@ModuleInfo(
   name = "AutoEnd",
   category = Category.OTHER,
   desc = "Ломает кувшины, чистит археологические блоки и подбирает лут"
)
public final class AutoEnd extends Module {
   private static final Logger LOGGER = LoggerFactory.getLogger("mentions/AutoEnd");
   private static final Gson LOOT_RECORD_GSON = new GsonBuilder().disableHtmlEscaping().create();
   private static final DateTimeFormatter LOOT_RECORD_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
   private static final String LOOT_RECORD_FILE = "autoend-loot-observed.jsonl";
   private static final File AUTOEND_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "autoend");
   private static final int PATH_GLOW_COLOR = 1342226431;
   private static final int PATH_COLOR = -16721409;
   private static final int PATH_NODE_COLOR = -794233601;
   private static final int PATH_GOAL_COLOR = -11713;
   private final SliderSetting potsBeforeHome = new SliderSetting(this, "Кувшинов до дома", 5.0F, 1.0F, 64.0F, 1.0F);
   private final BooleanSetting autoSupplies = new BooleanSetting(this, "Авто-снабжение", true);
   private final ModeSetting soundMode = new ModeSetting(
      this,
      "Звук уведомлений",
      "DingDong",
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
      "Laser",
      "WexSide",
      "Spek",
      "Metallic",
      "Crime",
      "Pop",
      "По умолчанию",
      "Выкл"
   );
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
   private final StringSetting anarchyNumbers = new StringSetting(this, "Анархия", "");
   private final StringSetting storageAnarchyNumber = new StringSetting(this, "Анархия склада", "");
   private final SliderSetting deathsBeforeAnarchySwitch = new SliderSetting(this, "Смертей до смены анархии", 3.0F, 1.0F, 20.0F, 1.0F);
   private static final double BREAK_REACH = 4.25;
   private static final double BREAK_REACH_SQ = 18.0625;
   private static final double MAX_TARGET_DISTANCE_SQ = 22500.0;
   private static final int MAX_CHUNK_RADIUS = 8;
   private static final int TARGET_SCAN_CHUNKS_PER_STEP = 24;
   private static final int RESCAN_DELAY_TICKS = 1;
   private static final int REPATH_DELAY_TICKS = 8;
   private static final int AIM_WARMUP_TICKS = 2;
   private static final int POT_BREAK_CONFIRM_WINDOW_TICKS = 10;
   private static final int LOOT_REPATH_DELAY_TICKS = 3;
   private static final int LOOT_TIMEOUT_TICKS = 120;
   private static final int LOOT_SPAWN_GRACE_TICKS = 12;
   private static final int LOOT_EMPTY_CONFIRM_TICKS = 8;
   private static final double LOOT_SEARCH_RADIUS = 12.0;
   private static final double LOOT_RECORD_RADIUS_SQUARED = 9.0;
   private static final int STALL_REPATH_TICKS = 80;
   private static final int STUCK_ROUTE_FAILURE_LIMIT = 8;
   private static final double STUCK_ROUTE_FAILURE_RADIUS_SQ = 4.0;
   private static final int STUCK_ROUTE_NO_PROGRESS_TICKS = 120;
   private static final double STUCK_ROUTE_PROGRESS_EPSILON_SQ = 1.0;
   private static final int PATH_CALCULATION_TIMEOUT_TICKS = 240;
   private static final int PATH_INACTIVE_TIMEOUT_TICKS = 30;
   private static final int NAVIGATION_LOG_INTERVAL_TICKS = 10;
   private static final int CONTAINER_RETRY_TICKS = 12;
   private static final int CONTAINER_OPEN_TIMEOUT_TICKS = 200;
   private static final int DEPOSIT_CLICK_DELAY_TICKS = 3;
   private static final int CONSUMABLE_RELEASE_TICKS = 2;
   private static final int CONSUMABLE_CHEST_AVOID_RADIUS = 6;
   private static final int EMPTY_HAND_HOTBAR_SLOT = 8;
   private static final int HOTBAR_CLEANUP_DELAY_TICKS = 3;
   private static final int IMMEDIATE_DISCARD_SETTLE_TICKS = 3;
   private static final int IMMEDIATE_DISCARD_RELEASE_TICKS = 1;
   private static final int HOME_CHEST_SEARCH_RADIUS = 2;
   private static final int SUPPLY_CHEST_SEARCH_RADIUS = 40;
   private static final int PORTAL_SEARCH_RADIUS = 48;
   private static final int PATROL_MIN_DISTANCE = 10;
   private static final int PATROL_MAX_DISTANCE = 24;
   private static final int PVP_CLEAR_CONFIRM_TICKS = 3;
   private static final int DARENA_RETRY_TICKS = 60;
   private static final int HOME_RETRY_TICKS = 120;
   private static final int EMERGENCY_VOID_MIN_Y = 2;
   private static final int EMERGENCY_VOID_MAX_Y = 6;
   private static final int HUB_CONFIRM_TICKS = 80;
   private static final int HUB_COMMAND_COOLDOWN_TICKS = 200;
   private static final int HUB_CENTER_RADIUS = 6;
   private static final int HUB_MIN_Y = 69;
   private static final int HUB_MAX_Y = 71;
   private static final Pattern PVP_SECONDS_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:сек|sec|s)", 66);
   private static final Pattern ANARCHY_SCOREBOARD_PATTERN = Pattern.compile("(?:анархия|anarchy)\\s*[-:#№]?\\s*(\\d+)", 66);
   private static final int UNREACHABLE_TARGET_TICKS = 1200;
   private class_2338 targetBlock;
   private boolean targetBrushable;
   private class_2338 lootOrigin;
   private class_2338 lootGoal;
   private boolean breakingStarted;
   private int potBreakAttemptTicks;
   private boolean brushingStarted;
   private boolean pathRequested;
   private int scanDelay;
   private int repathDelay;
   private int aimTicks;
   private int lootTicks;
   private int lootEmptyTicks;
   private int brushUseCooldown;
   private int previousSelectedSlot = -1;
   private int brushInventorySlot = -1;
   private int brushHotbarSlot = -1;
   private boolean brushMovedToHotbar;
   private final AutoEndNavigator navigator = new AutoEndNavigator(mc);
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
   private int brokenPots;
   private boolean returnHomePending;
   private boolean lootFromPot;
   private boolean deathRecoveryPending;
   private volatile boolean serverDeathMessagePending;
   private boolean deathRecoveryActive;
   private int initialPortalCheckTicks;
   private AutoEnd.CycleState cycleState = AutoEnd.CycleState.FARMING;
   private int cycleTicks;
   private int actionDelay;
   private class_2338 homeChest;
   private class_2338 supplyChest;
   private class_2338 portalTarget;
   private class_2338 portalWalkGoal;
   private int depositSlot;
   private boolean automationContainerOpened;
   private int automationContainerOpenTicks;
   private final Set<class_2338> exhaustedHomeChests = new HashSet<>();
   private final Set<class_2338> exhaustedSupplyChests = new HashSet<>();
   private final EnumSet<AutoEnd.SupplyKind> unavailableSupplyKinds = EnumSet.noneOf(AutoEnd.SupplyKind.class);
   private AutoEnd.SupplyKind activeConsumable;
   private int consumablePreviousSlot = -1;
   private int consumableHotbarSlot = -1;
   private int consumableUseDelay;
   private int consumableTicks;
   private int consumableInteractCooldown;
   private int consumableReleaseTicks;
   private class_2338 consumableAvoidChest;
   private int screenResumeDelay;
   private final class_1799[] lootHotbarSnapshot = new class_1799[9];
   private final Map<UUID, String> recordedLootEntities = new HashMap<>();
   private long lootRecordSession;
   private boolean lootRecordWriteFailed;
   private final Set<Integer> pickedHotbarSlots = new HashSet<>();
   private boolean hotbarCleanupPending;
   private boolean automationInventoryOpened;
   private int hotbarCleanupDelay;
   private int immediateDiscardCooldown;
   private int immediateDiscardSettleTicks;
   private boolean immediateDiscardStopping;
   private final Map<class_2338, Integer> temporarilySkippedTargets = new HashMap<>();
   private class_243 homeCommandPos;
   private class_243 darenaCommandPos;
   private Object homeCommandWorld;
   private Object darenaCommandWorld;
   private volatile boolean homeTeleportConfirmed;
   private int darenaMenuClickCooldown;
   private int pvpClearTicks;
   private int pvpRemainingSeconds = -1;
   private class_243 portalEntryPos;
   private Object portalEntryWorld;
   private class_2338 patrolGoal;
   private class_243 lastMovementPos;
   private int stationaryTicks;
   private int stalledRepaths;
   private class_2338 routeFailureAnchor;
   private int routeFailuresAtAnchor;
   private class_2338 progressTrackedGoal;
   private class_2338 progressTrackedWaypoint;
   private double bestProgressDistanceSq = Double.MAX_VALUE;
   private int noRouteProgressTicks;
   private int nonExecutingPathTicks;
   private int navigationLogTicks;
   private long movePacketWindowStartedNanos;
   private long previousMovePacketNanos;
   private long shortestMovePacketGapNanos;
   private int movePacketsInWindow;
   private int positionedMovePacketsInWindow;
   private int movementPacketAge = Integer.MIN_VALUE;
   private int movementPacketsThisAge;
   private int maxMovementPacketsInOneAge;
   private int updateAge = Integer.MIN_VALUE;
   private int updatesThisAge;
   private int maxUpdatesInOneAge;
   private int hubConfirmTicks;
   private int hubCommandCooldown;
   private boolean hubRecoveryHolding;
   private int currentAnarchyIndex;
   private int deathsOnCurrentAnarchy;
   private String detectedAnarchyNumber;
   private boolean anarchySwitchPending;
   private class_243 anarchyCommandPos;
   private Object anarchyCommandWorld;
   private String expectedAnarchyNumber;
   private String farmAnarchyBeforeStorage;
   private boolean storageRunActive;
   private boolean homeForPortalAfterStorage;
   private int targetScanCenterChunkX = Integer.MIN_VALUE;
   private int targetScanCenterChunkZ = Integer.MIN_VALUE;
   private int targetScanRadius = -1;
   private int targetScanCursor;
   private double targetScanNearestDistance = 22500.0;
   private AutoEnd.TargetSelection targetScanNearest;
   private boolean targetScanCanBrush;

   private List<String> anarchyNumberList() {
      String raw = this.anarchyNumbers.getValue();
      List<String> out = new ArrayList<>();
      if (raw == null) {
         return out;
      } else {
         for (String part : raw.split("[,;\\s]+")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
               out.add(trimmed);
            }
         }

         return out;
      }
   }

   private static String itemNbt(Object stack) {
      if (stack == null) {
         return "";
      } else {
         try {
            return String.valueOf(stack);
         } catch (Throwable var2) {
            return "<unavailable>";
         }
      }
   }

   private void suppressSprint() {
   }

   @Override
   public void onEnable() {
      this.screenResumeDelay = 0;
      this.logPlayer7();
      this.currentAnarchyIndex = 0;
      this.deathsOnCurrentAnarchy = 0;
      this.detectedAnarchyNumber = null;
      this.serverDeathMessagePending = false;
      this.anarchySwitchPending = false;
      this.anarchyCommandPos = null;
      this.anarchyCommandWorld = null;
      this.expectedAnarchyNumber = null;
      this.farmAnarchyBeforeStorage = null;
      this.storageRunActive = false;
      this.homeForPortalAfterStorage = false;
      this.lootRecordWriteFailed = false;
      this.findWorld7();
      this.findStopPath(false);
      this.collectPlayer2();
      this.findPlayer3();
      this.initialPortalCheckTicks = 100;
      this.temporarilySkippedTargets.clear();
      this.scanDelay = 0;
      this.findPlayer();
      this.logPlayer();
      this.navigator.stop();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.screenResumeDelay = 0;
      this.logPlayer7();
      this.anarchySwitchPending = false;
      this.anarchyCommandPos = null;
      this.anarchyCommandWorld = null;
      this.expectedAnarchyNumber = null;
      this.farmAnarchyBeforeStorage = null;
      this.storageRunActive = false;
      this.homeForPortalAfterStorage = false;
      this.serverDeathMessagePending = false;
      this.findWorld11();
      this.findWorld13();
      this.findWorld7();
      this.findWorld10();
      this.findState();
      this.findStack4();
      this.findStopPath(false);
      this.collectPlayer2();
      this.temporarilySkippedTargets.clear();
      this.findPlayer();
      super.onDisable();
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.serverDeathMessagePending) {
            this.serverDeathMessagePending = false;
            LOGGER.info("AutoEnd processing death confirmed by server chat");
            this.logText2();
         } else {
            this.logWorld14();
            int var2 = !(mc.field_1755 instanceof class_418)
                  && !mc.field_1724.method_29504()
                  && !(mc.field_1724.method_6032() <= 0.0F)
                  && mc.field_1724.field_6213 <= 0
               ? 0
               : 1;
            if (var2 != 0) {
               this.logText2();
            } else if (mc.field_1761 != null && !this.isWorld5()) {
               if (this.deathRecoveryPending) {
                  this.logWorld10();
               } else if (!this.isPlayer2()) {
                  if (this.isWorld3()) {
                     this.logPlayer3();
                     this.logForce(false);
                  } else {
                     if (this.cycleState == AutoEnd.CycleState.FARMING
                        && this.initialPortalCheckTicks > 0
                        && mc.field_1687.method_27983().equals(class_1937.field_25179)) {
                        this.initialPortalCheckTicks = this.initialPortalCheckTicks + -2 + 1;
                        if (this.initialPortalCheckTicks % 10 == 0) {
                           class_2338 var3 = this.findWorld2();
                           if (var3 != null) {
                              this.initialPortalCheckTicks = 0;
                              if (this.isStack()) {
                                 this.findStack4();
                                 this.findState();
                                 this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
                                 this.cycleTicks = 0;
                                 this.scanDelay = 0;
                                 this.pathRequested = false;
                                 this.repathDelay = 0;
                              } else {
                                 this.findStack4();
                                 this.findState();
                                 this.portalTarget = null;
                                 this.portalWalkGoal = null;
                                 this.cycleState = AutoEnd.CycleState.FIND_PORTAL;
                                 this.cycleTicks = 0;
                                 this.scanDelay = 0;
                              }
                           }
                        }
                     } else {
                        this.initialPortalCheckTicks = 0;
                     }

                     if (this.cycleState == AutoEnd.CycleState.FARMING && !this.isWorld2()) {
                        this.findStack4();
                        this.patrolGoal = null;
                        this.findStopPath(false);
                        this.collectPlayer2();
                        this.logPlayer11();
                        this.logPlayer3();
                     } else {
                        this.logWorld2();
                        if (this.scanDelay > 0) {
                           this.scanDelay = this.scanDelay + -2 + 1;
                        }

                        if (this.repathDelay > 0) {
                           this.repathDelay = this.repathDelay + -2 + 1;
                        }

                        if (this.brushUseCooldown > 0) {
                           this.brushUseCooldown = this.brushUseCooldown + -2 + 1;
                        }

                        if (this.actionDelay > 0) {
                           this.actionDelay = this.actionDelay + -2 + 1;
                        }

                        if (this.potBreakAttemptTicks > 0) {
                           this.potBreakAttemptTicks = this.potBreakAttemptTicks + -2 + 1;
                        }

                        if (this.darenaMenuClickCooldown > 0) {
                           this.darenaMenuClickCooldown = this.darenaMenuClickCooldown + -2 + 1;
                        }

                        this.findWorld12();
                        this.logWorld12();
                        if (this.isPlayer7()) {
                           this.logPlayer11();
                           this.logPlayer3();
                           this.logForce(false);
                        } else if (this.isStack2()) {
                           this.logPlayer11();
                           this.logPlayer3();
                           this.logForce(false);
                        } else {
                           this.findPlayer7();
                           if (mc.field_1755 != null) {
                              if (this.activeConsumable != null) {
                                 this.findWorld11();
                              }

                              this.logPlayer11();
                              if (!this.automationContainerOpened && !this.isPlayer3()) {
                                 this.screenResumeDelay = 3;
                                 this.logPlayer3();
                                 this.logForce(false);
                                 return;
                              }
                           } else if (this.screenResumeDelay > 0) {
                              this.screenResumeDelay = this.screenResumeDelay + -2 + 1;
                              this.logPlayer11();
                              this.logPlayer3();
                              this.logForce(false);
                              return;
                           }

                           int var9 = this.cycleState != AutoEnd.CycleState.FARMING && this.cycleState != AutoEnd.CycleState.FIND_PORTAL ? 0 : 1;
                           if (this.isPlayer4() && this.isAllowPotions(var9 != 0)) {
                              this.logPlayer11();
                              this.logPlayer3();
                              this.logForce(false);
                           } else {
                              if (mc.field_1755 == null && this.cycleState != AutoEnd.CycleState.ENTER_PORTAL) {
                                 this.navigator.tick();
                              }

                              this.logForce(false);
                              this.logPlayer5();
                              if (this.cycleState != AutoEnd.CycleState.FARMING) {
                                 this.handleWorld();
                              } else if (this.lootOrigin != null) {
                                 this.findWorld4();
                              } else if (this.targetBlock != null && this.targetBrushable && this.brushingStarted && !this.findPos8(this.targetBlock)) {
                                 class_2338 var11 = this.targetBlock.method_10062();
                                 this.findPlayer4();
                                 this.findSource(var11, false);
                              } else if (this.targetBlock != null && !this.targetBrushable && !this.findPos6(this.targetBlock)) {
                                 class_2338 var10 = this.targetBlock.method_10062();
                                 int var13 = this.potBreakAttemptTicks > 0 ? 1 : 0;
                                 if (var13 != 0) {
                                    LOGGER.info("Pot disappeared at {} after AutoEnd breaking input; entering loot collection", var10);
                                    this.findSource(var10, true);
                                 } else {
                                    LOGGER.info(
                                       "Pot target {} disappeared without AutoEnd breaking input; collecting drops while preserving no-pot timer", var10
                                    );
                                    this.findSource2(var10, true, false);
                                 }
                              } else {
                                 boolean var4 = this.findPlayer2();
                                 if (!var4) {
                                    if (this.targetBlock != null) {
                                       this.findStopPath(true);
                                    }

                                    if (this.scanDelay > 0) {
                                       this.findPlayer5();
                                       return;
                                    }

                                    AutoEnd.TargetScanResult var5 = this.findWorld5();
                                    this.scanDelay = 1;
                                    if (!var5.complete()) {
                                       this.findPlayer5();
                                       return;
                                    }

                                    AutoEnd.TargetSelection var6 = var5.selection();
                                    if (var6 == null) {
                                       this.findPlayer5();
                                       return;
                                    }

                                    this.logWorld3();
                                    this.findPlayer();
                                    this.targetBlock = var6.pos();
                                    this.targetBrushable = var6.brushable();
                                    LOGGER.info("Selected {} target at {}", this.targetBrushable ? "brushable" : "pot", this.targetBlock);
                                    int var7 = this.targetBrushable
                                       ? (!this.findPos2(this.targetBlock) ? 1 : 0)
                                       : (this.findPos(this.targetBlock) && this.findPos4(this.targetBlock) ? 0 : 1);
                                    if (var7 != 0) {
                                       this.logForce2(true);
                                       return;
                                    }
                                 }

                                 if (this.targetBrushable && !this.findPos2(this.targetBlock)) {
                                    if (this.brushingStarted) {
                                       this.findPlayer4();
                                    }

                                    this.breakingStarted = false;
                                    this.aimTicks = 0;
                                    this.logWorld11();
                                 } else if (!this.findPos(this.targetBlock)) {
                                    if (this.brushingStarted) {
                                       this.findPlayer4();
                                    }

                                    this.breakingStarted = false;
                                    this.aimTicks = 0;
                                    this.logWorld11();
                                 } else {
                                    boolean var12 = this.findStack();
                                    if (this.targetBrushable) {
                                       if (this.pathRequested || var12) {
                                          this.findStack4();
                                       }
                                    } else if (this.pathRequested) {
                                       if (var12) {
                                          return;
                                       }

                                       if (!this.findStand(mc.field_1724.method_24515(), this.targetBlock)) {
                                          class_2338 var15 = this.targetBlock.method_10062();
                                          this.temporarilySkippedTargets.put(var15, 1200);
                                          LOGGER.warn("Skipping completed pot route {}: target is still occluded", var15);
                                          this.findStack4();
                                          this.findStopPath(false);
                                          this.scanDelay = 1;
                                          return;
                                       }

                                       this.findStack4();
                                    }

                                    if (!this.findPos4(this.targetBlock)) {
                                       this.breakingStarted = false;
                                       this.aimTicks = 0;
                                       if (this.brushingStarted) {
                                          this.findPlayer4();
                                       }

                                       if (!this.pathRequested && !this.findStack()) {
                                          this.logForce2(true);
                                       }
                                    } else {
                                       boolean var14 = this.findPos3(this.targetBlock);
                                       this.aimTicks = this.aimTicks - -2 - 1;
                                       if ((var14 || this.aimTicks >= 12) && this.aimTicks >= 2) {
                                          if (this.targetBrushable) {
                                             this.findPlayer6();
                                          } else {
                                             if (mc.field_1765 instanceof class_3965 var16 && var16.method_17777().equals(this.targetBlock)) {
                                                this.breakingStarted = true;
                                                this.potBreakAttemptTicks = 10;
                                                return;
                                             }

                                             this.breakingStarted = false;
                                             this.aimTicks = 0;
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
            }
         }
      }
   }

   @EventHandler
   public void onRender(EventRender3D event) {
      WorldRenderBatcher.begin(event.getMatrixStack());
      if (mc.field_1724 != null && mc.field_1687 != null) {
         List var2 = this.navigator.getRemainingPath();
         if (!var2.isEmpty()) {
            class_243 var3 = mc.field_1724.method_73189().method_1031(0.0, 0.12, 0.0);

            for (int var4 = 0; var4 < var2.size(); var4++) {
               class_243 var5 = this.logPos((class_2338)var2.get(var4));
               WorldRenderBatcher.drawLineNoDepth(var3, var5, 1342226431, 6.0F);
               WorldRenderBatcher.drawLineNoDepth(var3, var5, -16721409, 2.25F);
               if (var4 == 0 || var4 == var2.size() + -2 + 1) {
                  this.logCenter(var5, var4 == var2.size() + -2 + 1 ? -11713 : -794233601);
               }

               var3 = var5;
            }

            this.logGoal((class_2338)var2.get(var2.size() + -2 + 1));
         }
      }

      WorldRenderBatcher.end();
   }

   private class_243 logPos(class_2338 pos) {
      return new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.12, (double)pos.method_10260() + 0.5);
   }

   private void logCenter(class_243 center, int color) {
      double var3 = 0.16;
      WorldRenderBatcher.drawLineNoDepth(center.method_1031(-var3, 0.0, 0.0), center.method_1031(var3, 0.0, 0.0), color, 2.5F);
      WorldRenderBatcher.drawLineNoDepth(center.method_1031(0.0, 0.0, -var3), center.method_1031(0.0, 0.0, var3), color, 2.5F);
   }

   private void logGoal(class_2338 goal) {
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
      this.logStart(var14, var15);
      this.logStart(var15, var16);
      this.logStart(var16, var17);
      this.logStart(var17, var14);
      this.logStart(var18, var19);
      this.logStart(var19, var20);
      this.logStart(var20, var21);
      this.logStart(var21, var18);
      this.logStart(var14, var18);
      this.logStart(var15, var19);
      this.logStart(var16, var20);
      this.logStart(var17, var21);
   }

   private void logStart(class_243 start, class_243 end) {
      WorldRenderBatcher.drawLineNoDepth(start, end, 1358934784, 5.0F);
      WorldRenderBatcher.drawLineNoDepth(start, end, -11713, 2.0F);
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (event.isSend() && event.getPacket() instanceof class_2828 var6) {
         this.logPacket(var6);
      } else if (!event.isSend() && event.getPacket() instanceof class_7439 var2) {
         String var8 = var2.comp_763().getString();
         if (var8 != null && !var8.isBlank()) {
            String var4 = var8.toLowerCase(Locale.ROOT);
            if (var4.contains("вы погибли")) {
               this.serverDeathMessagePending = true;
               LOGGER.info("AutoEnd queued death recovery from server chat");
            }

            if (this.isNormalizedMessage(var4)) {
               this.homeTeleportConfirmed = true;
               LOGGER.info("Home teleport confirmed by server chat");
            }

            int var5 = !var4.contains("simulation")
                  && !var4.contains("badpackets")
                  && !var4.contains("multiactions")
                  && !var4.contains("sprinta")
                  && !var4.contains("positionbreak")
                  && !var4.contains("airliquidbreak")
                  && !var4.contains("groundspoof")
                  && !var4.contains("timer")
               ? 0
               : 1;
            if (var5 != 0) {
               LOGGER.warn("[AutoEndFlag] {}", var8);
               this.logForce(true);
            } else {
               LOGGER.info("[AutoEndChat] {}", var8);
            }
         }
      }
   }

   private void logPlayer() {
      this.movePacketWindowStartedNanos = System.nanoTime();
      this.previousMovePacketNanos = 0L;
      this.shortestMovePacketGapNanos = Long.MAX_VALUE;
      this.movePacketsInWindow = 0;
      this.positionedMovePacketsInWindow = 0;
      this.movementPacketAge = Integer.MIN_VALUE;
      this.movementPacketsThisAge = 0;
      this.maxMovementPacketsInOneAge = 0;
      this.updateAge = Integer.MIN_VALUE;
      this.updatesThisAge = 0;
      this.maxUpdatesInOneAge = 0;
   }

   private void logWorld2() {
      int var1 = mc.field_1724.field_6012;
      if (var1 == this.updateAge) {
         this.updatesThisAge = this.updatesThisAge - -2 - 1;
      } else {
         this.updateAge = var1;
         this.updatesThisAge = 1;
      }

      this.maxUpdatesInOneAge = Math.max(this.maxUpdatesInOneAge, this.updatesThisAge);
      if (this.updatesThisAge == 2) {
         LOGGER.warn("[AutoEndTickDuplicate] EventUpdate called more than once for playerAge={}", var1);
      }
   }

   private void logPacket(class_2828 packet) {
      if (mc.field_1724 != null) {
         long var2 = System.nanoTime();
         int var4 = mc.field_1724.field_6012;
         if (var4 == this.movementPacketAge) {
            this.movementPacketsThisAge = this.movementPacketsThisAge - -2 - 1;
         } else {
            this.movementPacketAge = var4;
            this.movementPacketsThisAge = 1;
         }

         this.maxMovementPacketsInOneAge = Math.max(this.maxMovementPacketsInOneAge, this.movementPacketsThisAge);
         if (this.previousMovePacketNanos != 0L) {
            this.shortestMovePacketGapNanos = Math.min(this.shortestMovePacketGapNanos, var2 + ~this.previousMovePacketNanos + 1L);
         }

         this.previousMovePacketNanos = var2;
         this.movePacketsInWindow = this.movePacketsInWindow - -2 - 1;
         if (packet.method_36171()) {
            this.positionedMovePacketsInWindow = this.positionedMovePacketsInWindow - -2 - 1;
         }

         long var5 = var2 + ~this.movePacketWindowStartedNanos + 1L;
         if (var5 >= 1000000000L) {
            double var7 = (double)var5 / 1.0E9;
            double var9 = (double)this.movePacketsInWindow / var7;
            double var11 = this.shortestMovePacketGapNanos == Long.MAX_VALUE ? -1.0 : (double)this.shortestMovePacketGapNanos / 1000000.0;
            LOGGER.info(
               "[AutoEndMoveRate] rate={}pps total={} positioned={} maxSameTick={} updateMaxSameTick={} shortestGap={}ms age={}",
               new Object[]{
                  String.format(Locale.ROOT, "%.2f", var9),
                  this.movePacketsInWindow,
                  this.positionedMovePacketsInWindow,
                  this.maxMovementPacketsInOneAge,
                  this.maxUpdatesInOneAge,
                  String.format(Locale.ROOT, "%.3f", var11),
                  var4
               }
            );
            this.movePacketWindowStartedNanos = var2;
            this.shortestMovePacketGapNanos = Long.MAX_VALUE;
            this.movePacketsInWindow = 0;
            this.positionedMovePacketsInWindow = 0;
            this.maxMovementPacketsInOneAge = this.movementPacketsThisAge;
            this.maxUpdatesInOneAge = this.updatesThisAge;
         }
      }
   }

   public boolean shouldUseVanillaBreakingInput() {
      if (this.isEnabled()
         && mc.field_1724 != null
         && mc.field_1687 != null
         && mc.field_1755 == null
         && this.cycleState == AutoEnd.CycleState.FARMING
         && this.targetBlock != null
         && !this.targetBrushable
         && !this.pathRequested
         && !this.findStack()
         && this.findPos6(this.targetBlock)
         && this.findPos(this.targetBlock)) {
         byte var10000;
         label33: {
            if (mc.field_1765 instanceof class_3965 var2 && var2.method_17777().equals(this.targetBlock)) {
               var10000 = 1;
               break label33;
            }

            var10000 = 0;
         }

         int var1 = var10000;
         if (var1 != 0) {
            this.breakingStarted = true;
            this.potBreakAttemptTicks = 10;
         }

         return var1 != 0;
      } else {
         return false;
      }
   }

   private void findPlayer6() {
      if (this.isPlayer8()) {
         if (mc.field_1765 instanceof class_3965 var1 && var1.method_17777().equals(this.targetBlock)) {
            this.brushingStarted = true;
            if (mc.field_1690 != null) {
               mc.field_1690.field_1904.method_23481(true);
            }

            if ((!mc.field_1724.method_6115() || !mc.field_1724.method_6030().method_31574(class_1802.field_42716)) && this.brushUseCooldown <= 0) {
               mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, var1);
               this.brushUseCooldown = 4;
            }

            if (!this.findPos8(this.targetBlock)) {
               class_2338 var3 = this.targetBlock.method_10062();
               this.findPlayer4();
               this.findSource(var3, false);
            }

            return;
         }

         this.brushingStarted = false;
      }
   }

   private void findSource(class_2338 source, boolean fromPot) {
      this.findSource2(source, fromPot, true);
   }

   private void findSource2(class_2338 source, boolean fromPot, boolean successfulAutoEndAction) {
      this.findStack4();
      this.collectPlayer();
      this.lootOrigin = source.method_10062();
      this.lootFromPot = fromPot;
      this.lootRecordSession = System.currentTimeMillis();
      this.recordedLootEntities.clear();
      if (fromPot && successfulAutoEndAction) {
         this.brokenPots = this.brokenPots - -2 - 1;
         this.returnHomePending = this.brokenPots >= Math.max(1, Math.round(this.potsBeforeHome.getValue()));
      }

      this.lootGoal = null;
      this.lootTicks = 0;
      this.lootEmptyTicks = 0;
      this.findStopPath(false);
   }

   private void findWorld4() {
      this.lootTicks = this.lootTicks - -2 - 1;
      if (!this.findPos6(this.lootOrigin) && (!this.isPlayer() || !this.findPos8(this.lootOrigin))) {
         List<class_1542> var5 = this.logStack();
         this.logDrops(var5);
         class_1542 var6 = var5.stream()
            .filter(drop -> this.containsStack5(drop.method_6983()))
            .min((first, second) -> Double.compare(mc.field_1724.method_5858(first), mc.field_1724.method_5858(second)))
            .orElse(null);
         boolean var3 = var6 != null;
         if (var3) {
            this.lootEmptyTicks = 0;
         } else if (this.lootTicks >= 12) {
            this.lootEmptyTicks = this.lootEmptyTicks - -2 - 1;
         }

         if (!var3) {
            if (this.pathRequested || this.lootGoal != null || this.findStack2()) {
               this.findStack4();
               this.lootGoal = null;
            }

            if (this.lootEmptyTicks >= 8 || this.lootTicks >= 120) {
               this.collectPlayer3();
            }
         } else {
            class_2338 var4 = var6.method_24515();
            if (!this.findItem(var6)) {
               this.findGoal(var4);
            } else if (this.pathRequested || this.findStack2()) {
               this.findStack4();
            }
         }
      } else {
         class_2338 var1 = this.lootOrigin.method_10062();
         boolean var2 = this.findPos8(var1);
         this.findStack4();
         this.collectPlayer2();
         this.findStopPath(false);
         this.targetBlock = var1;
         this.targetBrushable = var2;
         this.scanDelay = 0;
      }
   }

   private List<class_1542> logStack() {
      if (this.lootOrigin == null) {
         return List.of();
      } else {
         class_238 var1 = new class_238(this.lootOrigin).method_1009(12.0, 3.0, 12.0);
         return mc.field_1687.method_8390(class_1542.class, var1, item -> !item.method_5805() || item.method_6983().method_7960());
      }
   }

   private void logDrops(List<class_1542> drops) {
      if (this.lootOrigin != null && !drops.isEmpty() && !this.lootRecordWriteFailed) {
         class_243 var2 = class_243.method_24953(this.lootOrigin);
         ArrayList<AutoEnd.LootRecord> var3 = new ArrayList<>();

         for (class_1542 var5 : drops) {
            if (!(var5.method_73189().method_1025(var2) > 9.0)) {
               class_1799 var6 = var5.method_6983();
               String var7 = this.findStack3(var6);
               String var8 = this.recordedLootEntities.get(var5.method_5667());
               if (!var7.equals(var8)) {
                  var3.add(new AutoEnd.LootRecord(var5, var6.method_7972(), var7, var8 != null));
               }
            }
         }

         if (!var3.isEmpty()) {
            try {
               if (!AUTOEND_DIR.exists() && !AUTOEND_DIR.mkdirs()) {
                  String var10x = String.valueOf(AUTOEND_DIR);
                  throw new IOException("could not create " + var10x);
               }

               StringBuilder var12 = new StringBuilder();

               for (AutoEnd.LootRecord var15 : var3) {
                  var12.append(LOOT_RECORD_GSON.toJson(this.handleRecord(var15))).append(System.lineSeparator());
               }

               File var14 = new File(AUTOEND_DIR, "autoend-loot-observed.jsonl");
               Files.writeString(var14.toPath(), var12.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

               for (AutoEnd.LootRecord var17 : var3) {
                  class_1542 var18 = var17.entity();
                  class_1799 var9 = var17.stack();
                  this.recordedLootEntities.put(var18.method_5667(), var17.state());
                  LOGGER.info(
                     "[AutoEndLoot] source={} pos={} item={} count={} name='{}' entity={} file={}",
                     new Object[]{
                        this.lootFromPot ? "pot" : "brush",
                        this.lootOrigin,
                        class_7923.field_41178.method_10221(var9.method_7909()),
                        var9.method_7947(),
                        var9.method_7964().getString(),
                        var18.method_5667(),
                        "autoend-loot-observed.jsonl"
                     }
                  );
               }
            } catch (IOException var10) {
               this.lootRecordWriteFailed = true;
               LOGGER.error("AutoEnd could not write loot discovery catalog {}", "autoend-loot-observed.jsonl", var10);
            }
         }
      }
   }

   private JsonObject handleRecord(AutoEnd.LootRecord record) {
      class_1542 var2 = record.entity();
      class_1799 var3 = record.stack();
      JsonObject var4 = new JsonObject();
      var4.addProperty("time", LocalDateTime.now().format(LOOT_RECORD_TIME));
      var4.addProperty("event", record.updated() ? "drop_updated" : "drop_observed");
      var4.addProperty("session_id", Long.toString(this.lootRecordSession));
      var4.addProperty("source_type", this.lootFromPot ? "decorated_pot" : "brushing");
      var4.addProperty("loot_tick", this.lootTicks);
      var4.addProperty("dimension", mc.field_1687.method_27983().method_29177().toString());
      var4.addProperty("source_x", this.lootOrigin.method_10263());
      var4.addProperty("source_y", this.lootOrigin.method_10264());
      var4.addProperty("source_z", this.lootOrigin.method_10260());
      var4.addProperty("drop_x", var2.method_23317());
      var4.addProperty("drop_y", var2.method_23318());
      var4.addProperty("drop_z", var2.method_23321());
      var4.addProperty("distance_from_source", Math.sqrt(var2.method_73189().method_1025(class_243.method_24953(this.lootOrigin))));
      var4.addProperty("entity_id", ((EntityAccessor)var2).mentions$getId());
      var4.addProperty("entity_uuid", var2.method_5845());
      var4.addProperty("item", class_7923.field_41178.method_10221(var3.method_7909()).toString());
      var4.addProperty("translation_key", var3.method_7909().method_7876());
      var4.addProperty("count", var3.method_7947());
      var4.addProperty("name", var3.method_7964().getString());
      var4.addProperty("name_raw", var3.method_7964().toString());
      if (var3.method_65130() != null) {
         var4.addProperty("custom_name", var3.method_65130().getString());
         var4.addProperty("custom_name_raw", var3.method_65130().toString());
      }

      JsonArray var5 = new JsonArray();
      JsonArray var6 = new JsonArray();
      class_9290 var7 = (class_9290)var3.method_58694(class_9334.field_49632);
      if (var7 != null) {
         for (class_2561 var9 : var7.comp_2400()) {
            var5.add(var9.getString());
            var6.add(var9.toString());
         }
      }

      var4.add("lore", var5);
      var4.add("lore_raw", var6);
      JsonArray var10 = new JsonArray();
      var3.method_7909().method_40131().method_40228().map(tag -> tag.comp_327().toString()).sorted().forEach(var10::add);
      var4.add("item_tags", var10);
      var4.addProperty("components_nbt", itemNbt(var3));
      var4.addProperty("component_changes", var3.method_57380().toString());
      class_9279 var11 = (class_9279)var3.method_58694(class_9334.field_49628);
      if (var11 != null) {
         var4.addProperty("custom_data", var11.method_57461().toString());
      }

      return var4;
   }

   private String findStack3(class_1799 stack) {
      String var10000 = String.valueOf(class_7923.field_41178.method_10221(stack.method_7909()));
      String var2 = String.valueOf(stack.method_57380());
      int var3 = stack.method_7947();
      return var10000 + "|" + var3 + "|" + var2;
   }

   private boolean findItem(class_1542 item) {
      if (item != null && mc.field_1724 != null) {
         double var2 = item.method_23317() - mc.field_1724.method_23317();
         double var4 = item.method_23321() - mc.field_1724.method_23321();
         return !(var2 * var2 + var4 * var4 <= 0.2) || !(Math.abs(item.method_23318() - mc.field_1724.method_23318()) <= 1.5);
      } else {
         return false;
      }
   }

   private void findGoal(class_2338 goal) {
      if (goal != null) {
         boolean var2 = this.findStack();
         int var3 = !goal.equals(this.lootGoal) ? 1 : 0;
         if (var3 != 0 || !this.pathRequested || !var2 && this.repathDelay <= 0) {
            this.lootGoal = goal.method_10062();
            this.pathRequested = this.navigator.navigate(this.lootGoal, 0, var3 != 0);
            this.repathDelay = 3;
         }
      }
   }

   private void collectPlayer3() {
      this.findStack4();
      this.collectPlayer2();
      if (this.returnHomePending) {
         this.logWorld6();
      } else {
         this.scanDelay = 0;
      }
   }

   private void collectPlayer2() {
      this.lootOrigin = null;
      this.lootGoal = null;
      this.lootTicks = 0;
      this.lootEmptyTicks = 0;
      this.lootFromPot = false;
      this.lootRecordSession = 0L;
      this.recordedLootEntities.clear();
      this.logPlayer4();
   }

   private void collectPlayer() {
      if (mc.field_1724 == null) {
         this.logPlayer4();
      } else {
         for (int var1 = 0; var1 < this.lootHotbarSnapshot.length; var1++) {
            this.lootHotbarSnapshot[var1] = mc.field_1724.method_31548().method_5438(var1).method_7972();
         }
      }
   }

   private void logPlayer4() {
      for (int var1 = 0; var1 < this.lootHotbarSnapshot.length; var1++) {
         this.lootHotbarSnapshot[var1] = null;
      }
   }

   private void logWorld12() {
      if (this.lootOrigin != null && mc.field_1724 != null) {
         for (int var1 = 0; var1 < this.lootHotbarSnapshot.length; var1++) {
            class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
            class_1799 var3 = this.lootHotbarSnapshot[var1];
            if (!var2.method_7960() && !this.containsStack4(var2) && !this.containsStack3(var2)) {
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

   private void logWorld6() {
      this.homeForPortalAfterStorage = false;
      this.logWorld19();
      this.findWorld11();
      this.findStack4();
      this.patrolGoal = null;
      this.findStopPath(false);
      this.findState();
      this.findWorld10();
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
      this.homeChest = null;
      this.supplyChest = null;
      this.portalTarget = null;
      this.portalWalkGoal = null;
      this.darenaCommandPos = null;
      this.darenaCommandWorld = null;
      this.darenaMenuClickCooldown = 0;
      this.homeCommandPos = null;
      this.homeCommandWorld = null;
      this.homeTeleportConfirmed = false;
      this.pvpClearTicks = 0;
      this.pvpRemainingSeconds = -1;
      this.portalEntryPos = null;
      this.portalEntryWorld = null;
      this.cycleTicks = 0;
      this.actionDelay = 0;
      this.cycleState = AutoEnd.CycleState.PVP_WAIT;
   }

   private void logWorld19() {
      String var1 = this.logWorld16();
      String var2 = this.detectedAnarchyNumber;
      this.storageRunActive = var1 == null || var2 == null || var1.equals(var2);
      this.farmAnarchyBeforeStorage = this.storageRunActive ? var2 : null;
      if (this.storageRunActive) {
         LOGGER.info("AutoEnd will deposit on /an{} and then return to farm /an{}", var1, var2);
      }
   }

   private String logWorld16() {
      String var1 = this.storageAnarchyNumber.getValue();
      if (var1 == null) {
         return null;
      } else {
         String var2 = var1.replaceAll("[^0-9]", "");
         return var2.isEmpty() ? null : var2;
      }
   }

   private boolean isWorld2() {
      if (mc.field_1687 == null) {
         return false;
      } else if (mc.field_1687.method_27983().equals(class_1937.field_25181)) {
         return true;
      } else {
         String var1 = mc.field_1687.method_27983().method_29177().method_12832().toLowerCase(Locale.ROOT);
         return var1.equals("funend") || var1.startsWith("funend_");
      }
   }

   private boolean isPlayer2() {
      if (this.cycleState != AutoEnd.CycleState.HOME_WAIT
         && this.cycleState != AutoEnd.CycleState.ANARCHY_WAIT
         && this.cycleState != AutoEnd.CycleState.FARM_RETURN_WAIT) {
         int var1 = class_3532.method_15357(mc.field_1724.method_23318());
         if (var1 >= 2 && var1 <= 6) {
            LOGGER.warn("Emergency void recovery at y={} state={}; teleporting directly home", var1, this.cycleState);
            this.findWorld11();
            this.findStack4();
            this.patrolGoal = null;
            this.findStopPath(false);
            this.collectPlayer2();
            this.findWorld7();
            this.findState();
            this.findWorld13();
            this.findWorld10();
            this.homeChest = null;
            this.supplyChest = null;
            this.portalTarget = null;
            this.portalWalkGoal = null;
            this.darenaCommandPos = null;
            this.darenaCommandWorld = null;
            this.darenaMenuClickCooldown = 0;
            this.pvpClearTicks = 0;
            this.pvpRemainingSeconds = -1;
            this.portalEntryPos = null;
            this.portalEntryWorld = null;
            this.deathRecoveryPending = false;
            this.deathRecoveryActive = false;
            this.returnHomePending = false;
            this.actionDelay = 0;
            this.cycleTicks = 0;
            this.logWorld4();
            this.cycleState = AutoEnd.CycleState.HOME_WAIT;
            this.logPlayer11();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @EventHandler
   public void onMoveInput(EventMoveInput event) {
      if (this.isEnabled()
         && (this.activeConsumable != null || this.immediateDiscardStopping || this.hubRecoveryHolding || this.screenResumeDelay > 0 || mc.field_1755 != null)) {
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

   private boolean isWorld5() {
      if (this.hubCommandCooldown > 0) {
         this.hubCommandCooldown = this.hubCommandCooldown + -2 + 1;
      }

      if (!this.isWorld7()) {
         this.hubConfirmTicks = 0;
         this.hubRecoveryHolding = false;
         return false;
      } else {
         this.hubRecoveryHolding = true;
         this.logPlayer11();
         this.findState();
         this.hubConfirmTicks = this.hubConfirmTicks - -2 - 1;
         if (this.hubConfirmTicks >= 80 && this.hubCommandCooldown <= 0) {
            AutoEnd.CycleState var1 = this.cycleState;
            int var2 = (var1 == AutoEnd.CycleState.ANARCHY_WAIT || var1 == AutoEnd.CycleState.FARM_RETURN_WAIT) && this.expectedAnarchyNumber != null ? 1 : 0;
            String var3 = this.expectedAnarchyNumber;
            String var4 = var2 != 0 ? "an" + var3 : this.logWorld5();
            if (var4 == null) {
               LOGGER.warn("AutoEnd confirmed hub spawn, but anarchy number is not configured");
               NotificationManager.send("Укажи номер в настройке AutoEnd «Анархия»", NotificationManager.Type.INFO);
               this.hubCommandCooldown = 200;
               return true;
            } else {
               boolean var5 = this.anarchySwitchPending;
               boolean var6 = this.storageRunActive;
               String var7 = this.farmAnarchyBeforeStorage;
               boolean var8 = this.deathRecoveryActive;
               this.logPlayer14();
               this.anarchyCommandPos = this.findWorld9();
               this.anarchyCommandWorld = mc.field_1687;
               this.expectedAnarchyNumber = var4.substring(2);
               mc.field_1724.field_3944.method_45730(var4);
               LOGGER.info("AutoEnd confirmed hub at {}; sent /{}", mc.field_1724.method_24515(), var4);
               NotificationManager.send("Возвращаюсь на /" + var4, NotificationManager.Type.MODULE);
               this.hubConfirmTicks = 0;
               this.hubCommandCooldown = 200;
               this.hubRecoveryHolding = true;
               this.anarchySwitchPending = false;
               if (var2 != 0) {
                  this.storageRunActive = var6;
                  this.farmAnarchyBeforeStorage = var7;
                  this.deathRecoveryActive = var8;
                  this.expectedAnarchyNumber = var3;
                  this.cycleState = var1;
                  this.cycleTicks = 0;
               } else if (var5) {
                  this.deathRecoveryActive = true;
                  this.cycleState = AutoEnd.CycleState.ANARCHY_WAIT;
                  this.cycleTicks = 0;
               }

               return true;
            }
         } else {
            return true;
         }
      }
   }

   private boolean isWorld7() {
      class_2338 var1 = mc.field_1724.method_24515();
      if (Math.abs(var1.method_10263()) <= 6 && Math.abs(var1.method_10260()) <= 6 && var1.method_10264() >= 69 && var1.method_10264() <= 71) {
         for (int var2 = 0; var2 < 36; var2++) {
            class_1799 var3 = mc.field_1724.method_31548().method_5438(var2);
            if (var3.method_31574(class_1802.field_8251) && var3.method_7964().getString().toLowerCase(Locale.ROOT).contains("выбор сервера")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private String logWorld5() {
      List var1 = this.anarchyNumberList();
      if (var1.isEmpty()) {
         this.currentAnarchyIndex = 0;
         return null;
      } else {
         this.currentAnarchyIndex = Math.floorMod(this.currentAnarchyIndex, var1.size());
         String var2 = (String)var1.get(this.currentAnarchyIndex);
         return "an" + var2;
      }
   }

   private void logWorld14() {
      String var1 = this.updateWorld();
      if (var1 != null) {
         List var2 = this.anarchyNumberList();
         int var3 = !this.anarchySwitchPending && this.cycleState != AutoEnd.CycleState.ANARCHY_WAIT && this.cycleState != AutoEnd.CycleState.FARM_RETURN_WAIT
            ? 0
            : 1;
         if (var3 != 0) {
            String var4 = this.expectedAnarchyNumber;
            if (var4 == null && !var2.isEmpty()) {
               var4 = (String)var2.get(Math.floorMod(this.currentAnarchyIndex, var2.size()));
            }

            if (var4 != null && !var4.equals(var1)) {
               return;
            }
         }

         int var6 = var2.indexOf(var1);
         if (var6 < 0) {
            if (!var1.equals(this.detectedAnarchyNumber)) {
               LOGGER.info("AutoEnd detected /an{} on scoreboard, but it is not in the configured list", var1);
            }

            this.detectedAnarchyNumber = var1;
         } else {
            int var5 = this.currentAnarchyIndex == var6 && var1.equals(this.detectedAnarchyNumber) ? 0 : 1;
            if (this.currentAnarchyIndex != var6) {
               this.deathsOnCurrentAnarchy = 0;
            }

            this.currentAnarchyIndex = var6;
            this.detectedAnarchyNumber = var1;
            if (var5 != 0) {
               LOGGER.info("AutoEnd synchronized current anarchy from scoreboard: /an{} (list index {})", var1, var6);
            }
         }
      }
   }

   private String updateWorld() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
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
            String var8 = this.logText(var2.method_1114().getString());
            if (var8 != null) {
               return var8;
            } else {
               for (class_9011 var6 : var1.method_1184(var2)) {
                  String var7 = class_268.method_1142(var1.method_1164(var6.comp_2127()), var6.method_55387()).getString();
                  var8 = this.logText(var7);
                  if (var8 != null) {
                     return var8;
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   private String logText(String text) {
      if (text != null && !text.isBlank()) {
         Matcher var2 = ANARCHY_SCOREBOARD_PATTERN.matcher(text);
         return var2.find() ? var2.group(1) : null;
      } else {
         return null;
      }
   }

   private void logPlayer14() {
      this.findWorld11();
      this.findStack4();
      this.patrolGoal = null;
      this.findStopPath(false);
      this.collectPlayer2();
      this.findWorld7();
      this.findState();
      this.findWorld13();
      this.findWorld10();
      this.findPlayer3();
      this.initialPortalCheckTicks = 100;
   }

   private void logPlayer7() {
      this.hubConfirmTicks = 0;
      this.hubCommandCooldown = 0;
      this.hubRecoveryHolding = false;
   }

   private void logPlayer11() {
      this.navigator.pauseMovement();
      this.suppressSprint();
      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);
         if (mc.field_1724.field_3913 != null) {
            mc.field_1724.field_3913.field_54155 = class_10185.field_54098;
         }
      }
   }

   private void logText2() {
      if (!this.deathRecoveryPending) {
         this.storageRunActive = false;
         this.farmAnarchyBeforeStorage = null;
         this.expectedAnarchyNumber = null;
         this.logWorld();
         this.deathRecoveryPending = true;
         this.deathRecoveryActive = false;
         this.returnHomePending = false;
         this.findStack4();
         this.patrolGoal = null;
         this.findStopPath(false);
         this.collectPlayer2();
         this.findState();
         this.findWorld10();
      }
   }

   private void logWorld() {
      List var1 = this.anarchyNumberList();
      if (var1.isEmpty()) {
         this.currentAnarchyIndex = 0;
         this.deathsOnCurrentAnarchy = 0;
      } else {
         this.currentAnarchyIndex = Math.floorMod(this.currentAnarchyIndex, var1.size());
         this.deathsOnCurrentAnarchy = this.deathsOnCurrentAnarchy - -2 - 1;
         int var2 = Math.max(1, Math.round(this.deathsBeforeAnarchySwitch.getValue()));
         LOGGER.info("AutoEnd death on /an{}: {}/{}", new Object[]{var1.get(this.currentAnarchyIndex), this.deathsOnCurrentAnarchy, var2});
         if (this.deathsOnCurrentAnarchy >= var2) {
            this.deathsOnCurrentAnarchy = 0;
            if (var1.size() >= 2) {
               String var3 = (String)var1.get(this.currentAnarchyIndex);
               this.currentAnarchyIndex = (this.currentAnarchyIndex - -2 - 1) % var1.size();
               String var4 = (String)var1.get(this.currentAnarchyIndex);
               this.anarchySwitchPending = true;
               LOGGER.info("AutoEnd death limit reached on /an{}; next anarchy is /an{}", var3, var4);
               NotificationManager.send("Лимит смертей: /an" + var3 + " → /an" + var4, NotificationManager.Type.MODULE);
            }
         }
      }
   }

   private void logWorld10() {
      this.findWorld11();
      this.findStack4();
      this.patrolGoal = null;
      this.findStopPath(false);
      this.collectPlayer2();
      this.findState();
      this.findWorld10();
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
      this.homeChest = null;
      this.supplyChest = null;
      this.portalTarget = null;
      this.portalWalkGoal = null;
      this.darenaCommandPos = null;
      this.darenaCommandWorld = null;
      this.darenaMenuClickCooldown = 0;
      this.pvpClearTicks = 0;
      this.pvpRemainingSeconds = -1;
      this.portalEntryPos = null;
      this.portalEntryWorld = null;
      this.brokenPots = 0;
      this.returnHomePending = false;
      this.actionDelay = 0;
      this.cycleTicks = 0;
      this.deathRecoveryPending = false;
      this.deathRecoveryActive = true;
      if (this.anarchySwitchPending) {
         String var1 = this.logWorld5();
         if (var1 != null) {
            this.anarchyCommandPos = this.findWorld9();
            this.anarchyCommandWorld = mc.field_1687;
            this.expectedAnarchyNumber = var1.substring(2);
            mc.field_1724.field_3944.method_45730(var1);
            this.anarchySwitchPending = false;
            this.cycleState = AutoEnd.CycleState.ANARCHY_WAIT;
            LOGGER.info("AutoEnd sent /{} after reaching the configured death limit", var1);
            return;
         }

         this.anarchySwitchPending = false;
      }

      this.logWorld4();
      this.cycleState = AutoEnd.CycleState.HOME_WAIT;
   }

   private void handleWorld() {
      this.cycleTicks = this.cycleTicks - -2 - 1;
      switch (this.cycleState) {
         case FARMING:
         default:
            break;
         case PVP_WAIT:
            this.logWorld15();
            break;
         case DARENA_WAIT:
            this.logWorld18();
            break;
         case ANARCHY_WAIT:
            this.logWorld9();
            break;
         case FARM_RETURN_WAIT:
            this.logWorld13();
            break;
         case HOME_WAIT:
            this.logWorld8();
            break;
         case FIND_CHEST:
            this.logPlayer2();
            break;
         case WALK_CHEST:
            this.logPlayer18();
            break;
         case OPEN_CHEST:
            this.logPlayer12();
            break;
         case DEPOSIT:
            this.logPlayer21();
            break;
         case FIND_SUPPLY:
            this.logPlayer15();
            break;
         case WALK_SUPPLY:
            this.logPlayer8();
            break;
         case OPEN_SUPPLY:
            this.logPlayer20();
            break;
         case TAKE_SUPPLY:
            this.calculatePlayer2();
            break;
         case FIND_PORTAL:
            this.findWorld6();
            break;
         case WALK_PORTAL:
            this.logPlayer19();
            break;
         case ENTER_PORTAL:
            this.calculatePlayer();
      }
   }

   private void logWorld15() {
      AutoEnd.PvpCooldown var1 = this.logWorld17();
      if (var1.active()) {
         this.pvpClearTicks = 0;
         this.pvpRemainingSeconds = var1.seconds();
         this.findPlayer5();
      } else {
         this.pvpRemainingSeconds = 0;
         this.pvpClearTicks = this.pvpClearTicks - -2 - 1;
         if (this.pvpClearTicks < 3) {
            this.findPlayer5();
         } else {
            this.logWorld3();
            this.darenaCommandPos = this.findWorld9();
            this.darenaCommandWorld = mc.field_1687;
            mc.field_1724.field_3944.method_45730("darena");
            this.cycleState = AutoEnd.CycleState.DARENA_WAIT;
            this.cycleTicks = 0;
         }
      }
   }

   private boolean isPlayer3() {
      return mc.field_1724 != null && mc.field_1724.field_7512 instanceof class_1707
         ? this.cycleState != AutoEnd.CycleState.DARENA_WAIT
            && this.cycleState != AutoEnd.CycleState.OPEN_CHEST
            && this.cycleState != AutoEnd.CycleState.DEPOSIT
            && this.cycleState != AutoEnd.CycleState.OPEN_SUPPLY
            && this.cycleState != AutoEnd.CycleState.TAKE_SUPPLY
         : false;
   }

   private boolean isWorld3() {
      boolean var1 = mc.field_1724.field_7512 instanceof class_1707;
      int var2 = !this.automationContainerOpened && !this.isPlayer3() ? 0 : 1;
      if (var1 && var2 != 0) {
         this.automationContainerOpenTicks = this.automationContainerOpenTicks - -2 - 1;
         if (this.automationContainerOpenTicks < 200) {
            return false;
         } else {
            AutoEnd.CycleState var3 = this.cycleState;
            LOGGER.warn("Closing timed-out AutoEnd container after {} ticks in state {}", this.automationContainerOpenTicks, var3);
            this.logPlayer11();
            mc.field_1724.method_3137();
            mc.method_1507(null);
            this.automationContainerOpened = false;
            this.automationContainerOpenTicks = 0;
            this.actionDelay = 0;
            this.screenResumeDelay = 3;
            switch (var3) {
               case DARENA_WAIT:
                  this.darenaMenuClickCooldown = 0;
                  this.cycleTicks = 59;
               case ANARCHY_WAIT:
               case FARM_RETURN_WAIT:
               case HOME_WAIT:
               case FIND_CHEST:
               case WALK_CHEST:
               case FIND_SUPPLY:
               case WALK_SUPPLY:
               default:
                  break;
               case OPEN_CHEST:
               case DEPOSIT:
                  this.homeChest = null;
                  this.cycleState = AutoEnd.CycleState.FIND_CHEST;
                  this.cycleTicks = 0;
                  this.scanDelay = 20;
                  this.depositSlot = 0;
                  break;
               case OPEN_SUPPLY:
               case TAKE_SUPPLY:
                  this.supplyChest = null;
                  this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
                  this.cycleTicks = 0;
                  this.scanDelay = 20;
            }

            return true;
         }
      } else {
         this.automationContainerOpenTicks = 0;
         return false;
      }
   }

   private void logWorld18() {
      this.findStack4();
      if (mc.field_1724.field_7512 instanceof class_1707 var3) {
         if (this.darenaMenuClickCooldown <= 0 && this.isHandler2(var3)) {
            this.darenaMenuClickCooldown = 20;
         }
      } else {
         int var1 = this.darenaCommandWorld != null && mc.field_1687 != this.darenaCommandWorld ? 1 : 0;
         if (var1 == 0 && this.darenaCommandPos != null) {
            var1 = this.findWorld9().method_1025(this.darenaCommandPos) > 64.0 ? 1 : 0;
         }

         if (var1 != 0) {
            if (!this.isWorld4()) {
               this.logWorld4();
               this.cycleState = AutoEnd.CycleState.HOME_WAIT;
               this.cycleTicks = 0;
            }
         } else if (this.cycleTicks > 0 && this.cycleTicks % 60 == 0) {
            this.darenaCommandPos = this.findWorld9();
            this.darenaCommandWorld = mc.field_1687;
            this.darenaMenuClickCooldown = 0;
            mc.field_1724.field_3944.method_45730("darena");
         }
      }
   }

   private boolean isWorld4() {
      if (!this.storageRunActive) {
         return false;
      } else {
         String var1 = this.logWorld16();
         if (var1 != null && !var1.equals(this.detectedAnarchyNumber)) {
            this.findStack4();
            this.findState();
            this.anarchyCommandPos = this.findWorld9();
            this.anarchyCommandWorld = mc.field_1687;
            this.expectedAnarchyNumber = var1;
            mc.field_1724.field_3944.method_45730("an" + var1);
            this.cycleState = AutoEnd.CycleState.ANARCHY_WAIT;
            this.cycleTicks = 0;
            LOGGER.info("AutoEnd sent /an{} before depositing loot", var1);
            NotificationManager.send("Иду на склад /an" + var1, NotificationManager.Type.MODULE);
            return true;
         } else {
            this.storageRunActive = false;
            this.farmAnarchyBeforeStorage = null;
            return false;
         }
      }
   }

   private void logWorld9() {
      this.findStack4();
      int var1 = this.expectedAnarchyNumber != null && this.expectedAnarchyNumber.equals(this.detectedAnarchyNumber) ? 1 : 0;
      int var2 = this.anarchyCommandWorld != null && mc.field_1687 != this.anarchyCommandWorld ? 1 : 0;
      if (var2 == 0 && this.anarchyCommandPos != null) {
         var2 = this.findWorld9().method_1025(this.anarchyCommandPos) > 64.0 ? 1 : 0;
      }

      int var3 = this.storageRunActive && this.expectedAnarchyNumber != null && this.expectedAnarchyNumber.equals(this.logWorld16()) ? 1 : 0;
      int var4 = var3 != 0 ? var1 : (var2 == 0 && var1 == 0 ? 0 : 1);
      if ((var4 == 0 || this.cycleTicks < 20) && (var3 != 0 || this.cycleTicks < 100)) {
         if (var3 != 0 && this.cycleTicks > 0 && this.cycleTicks % 100 == 0) {
            this.anarchyCommandPos = this.findWorld9();
            this.anarchyCommandWorld = mc.field_1687;
            String var5 = this.expectedAnarchyNumber;
            mc.field_1724.field_3944.method_45730("an" + var5);
            LOGGER.warn("AutoEnd is still waiting for storage; retried /an{}", this.expectedAnarchyNumber);
         }
      } else {
         this.anarchyCommandPos = null;
         this.anarchyCommandWorld = null;
         this.expectedAnarchyNumber = null;
         this.logWorld4();
         this.cycleState = AutoEnd.CycleState.HOME_WAIT;
         this.cycleTicks = 0;
      }
   }

   private void logWorld13() {
      this.findStack4();
      int var1 = this.expectedAnarchyNumber != null && this.expectedAnarchyNumber.equals(this.detectedAnarchyNumber) ? 1 : 0;
      if (var1 != 0 && this.cycleTicks >= 20) {
         String var2 = this.farmAnarchyBeforeStorage;
         this.storageRunActive = false;
         this.farmAnarchyBeforeStorage = null;
         this.expectedAnarchyNumber = null;
         this.anarchyCommandPos = null;
         this.anarchyCommandWorld = null;
         this.exhaustedHomeChests.clear();
         this.exhaustedSupplyChests.clear();
         this.homeChest = null;
         this.supplyChest = null;
         this.portalTarget = null;
         this.portalWalkGoal = null;
         this.homeForPortalAfterStorage = true;
         this.logWorld4();
         this.cycleState = AutoEnd.CycleState.HOME_WAIT;
         this.cycleTicks = 0;
         this.scanDelay = 0;
         LOGGER.info("AutoEnd returned from storage to farm /an{}; sent /home before portal entry", var2);
      } else if (this.cycleTicks > 0 && this.cycleTicks % 100 == 0 && this.farmAnarchyBeforeStorage != null) {
         this.anarchyCommandPos = this.findWorld9();
         this.anarchyCommandWorld = mc.field_1687;
         this.expectedAnarchyNumber = this.farmAnarchyBeforeStorage;
         String var3 = this.farmAnarchyBeforeStorage;
         mc.field_1724.field_3944.method_45730("an" + var3);
         LOGGER.warn("AutoEnd is still waiting to return to farm; retried /an{}", this.farmAnarchyBeforeStorage);
      }
   }

   private boolean isHandler2(class_1707 handler) {
      int var2 = handler.method_17388() * 9;

      for (int var3 = 0; var3 < var2; var3++) {
         class_1735 var4 = handler.method_7611(var3);
         if (!var4.method_7677().method_7960() && var4.method_7677().method_31574(class_1802.field_8323)) {
            mc.field_1761.method_2906(handler.field_7763, var4.field_7874, 0, class_1713.field_7790, mc.field_1724);
            LOGGER.info("Selected /darena pufferfish in slot {}", var4.field_7874);
            return true;
         }
      }

      return false;
   }

   private AutoEnd.PvpCooldown logWorld17() {
      if (mc.field_1705 != null && mc.field_1705.method_1740() != null) {
         for (class_345 var2 : ((BossBarHudAccessor)mc.field_1705.method_1740()).mentions$getBossBars().values()) {
            String var3 = var2.method_5414().getString().toLowerCase(Locale.ROOT);
            int var4 = !var3.contains("pvp") && !var3.contains("пвп") && !var3.contains("combat") ? 0 : 1;
            if (var4 != 0) {
               Matcher var5 = PVP_SECONDS_PATTERN.matcher(var3);
               if (var5.find()) {
                  try {
                     double var10 = Double.parseDouble(var5.group(1).replace(',', '.'));
                     int var8 = Math.max(0, (int)Math.ceil(var10));
                     return new AutoEnd.PvpCooldown(var8 > 0, var8);
                  } catch (NumberFormatException var9) {
                  }
               }

               int var6 = Math.max(0, Math.round(var2.method_5412() * 30.0F));
               return new AutoEnd.PvpCooldown(true, var6);
            }
         }

         return AutoEnd.PvpCooldown.INACTIVE;
      } else {
         return AutoEnd.PvpCooldown.INACTIVE;
      }
   }

   private boolean isWorld8() {
      if (mc.field_1705 != null && mc.field_1705.method_1740() != null) {
         for (class_345 var2 : ((BossBarHudAccessor)mc.field_1705.method_1740()).mentions$getBossBars().values()) {
            String var3 = var2.method_5414().getString().toLowerCase(Locale.ROOT);
            if (var3.contains("пвп")) {
               return true;
            }

            if (var3.contains("pvp") || var3.contains("пвп")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void logWorld8() {
      this.findStack4();
      boolean var1 = this.homeTeleportConfirmed;
      int var2 = var1 || this.homeCommandWorld != null && mc.field_1687 != this.homeCommandWorld ? 1 : 0;
      if (var2 == 0 && this.homeCommandPos != null) {
         var2 = this.findWorld9().method_1025(this.homeCommandPos) > 64.0 ? 1 : 0;
      }

      if (var2 != 0) {
         boolean var3 = this.homeForPortalAfterStorage;
         AutoEnd.CycleState var4 = var3
            ? AutoEnd.CycleState.FIND_PORTAL
            : (this.deathRecoveryActive ? (this.isStack() ? AutoEnd.CycleState.FIND_SUPPLY : AutoEnd.CycleState.FIND_PORTAL) : AutoEnd.CycleState.FIND_CHEST);
         this.homeForPortalAfterStorage = false;
         this.homeTeleportConfirmed = false;
         this.cycleState = var4;
         this.cycleTicks = 0;
         this.scanDelay = 0;
         LOGGER.info("Home arrival confirmed by {}; next state={}", var1 ? "server chat" : "world/position change", var4);
      } else if (this.cycleTicks > 0 && this.cycleTicks % 120 == 0) {
         this.logWorld4();
      }
   }

   private void logWorld4() {
      this.homeTeleportConfirmed = false;
      this.homeCommandPos = this.findWorld9();
      this.homeCommandWorld = mc.field_1687;
      mc.field_1724.field_3944.method_45730("home");
   }

   private boolean isNormalizedMessage(String normalizedMessage) {
      return !normalizedMessage.contains("home")
         ? false
         : !normalizedMessage.contains("телепортирование к home")
            && !normalizedMessage.contains("телепортирован к home")
            && !normalizedMessage.contains("teleporting to home")
            && !normalizedMessage.contains("teleported to home");
   }

   private void logPlayer2() {
      if (!this.isPlayer6() && this.scanDelay <= 0) {
         this.scanDelay = 10;
         this.homeChest = this.sendWorld();
         if (this.homeChest != null) {
            LOGGER.info("Selected home chest {} near field_1724 {}", this.homeChest, mc.field_1724.method_24515());
            this.cycleState = AutoEnd.CycleState.WALK_CHEST;
            this.cycleTicks = 0;
            this.pathRequested = false;
            this.repathDelay = 0;
         } else if (this.cycleTicks > 120) {
            LOGGER.warn("No home chest found within {} horizontal blocks of {}", 2, mc.field_1724.method_24515());
            this.exhaustedHomeChests.clear();
            this.cycleTicks = 0;
         }
      }
   }

   private void logPlayer18() {
      if (!this.isPos2(this.homeChest)) {
         this.findStack4();
         this.homeChest = null;
         this.cycleState = AutoEnd.CycleState.FIND_CHEST;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      } else if (this.findPos5(this.homeChest) == null && this.findPos9(this.homeChest) == null) {
         this.logPlayer16();
      } else {
         this.findStack4();
         this.cycleState = AutoEnd.CycleState.OPEN_CHEST;
         this.cycleTicks = 0;
         this.actionDelay = 0;
         this.setPlayer();
      }
   }

   private void logPlayer16() {
      if (this.homeChest != null) {
         boolean var1 = this.navigator.isWorking();
         if (!this.pathRequested || !var1 && this.repathDelay <= 0) {
            this.pathRequested = this.navigator.navigate(this.homeChest, 1, false);
            this.repathDelay = 8;
         }
      }
   }

   private void logPlayer12() {
      if (mc.field_1724.field_7512 instanceof class_1707 var4) {
         this.automationContainerOpened = true;
         this.depositSlot = var4.method_7629().method_5439();
         this.cycleState = AutoEnd.CycleState.DEPOSIT;
         this.cycleTicks = 0;
         this.actionDelay = 3;
      } else if (!this.isPos2(this.homeChest)) {
         this.homeChest = null;
         this.cycleState = AutoEnd.CycleState.FIND_CHEST;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      } else if (this.actionDelay <= 0) {
         boolean var1 = this.findPos3(this.homeChest);
         class_3965 var5 = this.findPos5(this.homeChest);
         if (var5 == null && !var1) {
            if (this.cycleTicks > 100) {
               this.logPlayer6();
            }
         } else {
            if (var5 == null) {
               var5 = this.findPos9(this.homeChest);
            }

            if (var5 == null) {
               this.cycleState = AutoEnd.CycleState.WALK_CHEST;
               this.cycleTicks = 0;
               this.pathRequested = false;
               this.repathDelay = 0;
               this.actionDelay = 0;
               this.setPlayer();
            } else {
               class_1269 var3 = mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, var5);
               mc.field_1724.method_6104(class_1268.field_5808);
               LOGGER.info("Tried to open home chest {} side={} result={}", new Object[]{this.homeChest, var5.method_17780(), var3});
               this.actionDelay = 12;
               if (this.cycleTicks > 100) {
                  this.logPlayer6();
               }
            }
         }
      }
   }

   private void logPlayer6() {
      if (this.homeChest != null) {
         this.exhaustedHomeChests.add(this.homeChest.method_10062());
      }

      this.homeChest = null;
      this.cycleState = AutoEnd.CycleState.FIND_CHEST;
      this.cycleTicks = 0;
      this.scanDelay = 0;
      this.actionDelay = 0;
      this.setPlayer();
   }

   private void logPlayer21() {
      if (mc.field_1724.field_7512 instanceof class_1707 var1) {
         if (this.actionDelay <= 0) {
            int var6 = var1.method_7629().method_5439();
            int var3 = this.calculateHandler2(var1, var6);
            if (var3 >= 0) {
               class_1735 var7 = (class_1735)var1.field_7761.get(var3);
               class_1799 var9 = var7.method_7677().method_7972();
               mc.field_1761.method_2906(var1.field_7763, var7.field_7874, 1, class_1713.field_7795, mc.field_1724);
               LOGGER.info(
                  "Discarded filtered AutoEnd loot item={} count={} name='{}'",
                  new Object[]{class_7923.field_41178.method_10221(var9.method_7909()), var9.method_7947(), var9.method_7964().getString()}
               );
               this.actionDelay = 3;
            } else {
               int var4 = this.calculateHandler(var1, Math.max(this.depositSlot, var6));
               if (var4 >= 0) {
                  class_1735 var8 = (class_1735)var1.field_7761.get(var4);
                  mc.field_1761.method_2906(var1.field_7763, var8.field_7874, 0, class_1713.field_7794, mc.field_1724);
                  this.depositSlot = var4 - -2 - 1;
                  this.actionDelay = 3;
               } else {
                  boolean var5 = this.isHandler(var1, var6);
                  this.findWorld10();
                  if (var5) {
                     this.exhaustedHomeChests.add(this.homeChest.method_10062());
                     this.homeChest = null;
                     this.cycleState = AutoEnd.CycleState.FIND_CHEST;
                     this.cycleTicks = 0;
                     this.scanDelay = 0;
                  } else {
                     this.homeChest = null;
                     if (this.isStack()) {
                        this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
                        this.cycleTicks = 0;
                        this.scanDelay = 0;
                     } else {
                        this.logWorld7();
                     }

                     this.setPlayer();
                  }
               }
            }
         }
      } else {
         this.automationContainerOpened = false;
         this.homeChest = null;
         this.cycleState = AutoEnd.CycleState.FIND_CHEST;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      }
   }

   private int calculateHandler(class_1707 handler, int start) {
      for (int var3 = Math.max(0, start); var3 < handler.field_7761.size(); var3++) {
         class_1735 var4 = (class_1735)handler.field_7761.get(var3);
         if (!var4.method_7677().method_7960() && !this.containsStack4(var4.method_7677())) {
            return var3;
         }
      }

      return -1;
   }

   private boolean isHandler(class_1707 handler, int chestSlots) {
      return this.calculateHandler(handler, chestSlots) >= 0;
   }

   private int calculateHandler2(class_1707 handler, int chestSlots) {
      for (int var3 = Math.max(0, chestSlots); var3 < handler.field_7761.size(); var3++) {
         class_1735 var4 = (class_1735)handler.field_7761.get(var3);
         if (this.containsStack5(var4.method_7677())) {
            return var3;
         }
      }

      return -1;
   }

   private boolean containsStack5(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
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
                  && !var2.contains("хлопушк")
               ? stack.method_31574(class_1802.field_20391)
                  || stack.method_31574(class_1802.field_8153)
                  || stack.method_31574(class_1802.field_8539)
                  || stack.method_31574(class_1802.field_8745)
                  || stack.method_31574(class_1802.field_8606)
                  || stack.method_31574(class_1802.field_8276)
                  || stack.method_31574(class_1802.field_8711)
                  || stack.method_31574(class_1802.field_8536)
                  || stack.method_31574(class_1802.field_8233)
                  || stack.method_31574(class_1802.field_8719)
                  || stack.method_31574(class_1802.field_8140)
                  || stack.method_31574(class_1802.field_8448)
                  || stack.method_31574(class_1802.field_8864)
                  || stack.method_31574(class_1802.field_8469)
                  || stack.method_31574(class_1802.field_8777)
                  || stack.method_31574(class_1802.field_8620)
                  || stack.method_31574(class_1802.field_8687)
                  || stack.method_31574(class_1802.field_8894)
                  || stack.method_31574(class_1802.field_8070)
                  || stack.method_31574(class_1802.field_8301)
                  || stack.method_31574(class_1802.field_8657)
                  || stack.method_31574(class_1802.field_17515)
                  || stack.method_31574(class_1802.field_20417)
                  || stack.method_31574(class_1802.field_8323)
               : true;
         }
      } else {
         return false;
      }
   }

   private boolean isPlayer7() {
      if (mc.field_1724 != null
         && mc.field_1761 != null
         && !this.automationContainerOpened
         && this.activeConsumable == null
         && this.previousSelectedSlot < 0
         && !this.brushingStarted
         && (mc.field_1755 == null || this.automationInventoryOpened)
         && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
         int var1 = -1;

         for (int var2 = 0; var2 < 36; var2++) {
            class_1799 var3 = mc.field_1724.method_31548().method_5438(var2);
            if (this.containsStack5(var3)) {
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
            this.logPlayer11();
            class_243 var6 = mc.field_1724.method_18798();
            mc.field_1724.method_18800(0.0, var6.field_1351, 0.0);
            if (this.immediateDiscardCooldown > 0) {
               this.immediateDiscardCooldown = this.immediateDiscardCooldown + -2 + 1;
               return true;
            } else if (this.immediateDiscardSettleTicks < 3) {
               this.immediateDiscardSettleTicks = this.immediateDiscardSettleTicks - -2 - 1;
               return true;
            } else if (var1 >= 0) {
               class_1799 var7 = mc.field_1724.method_31548().method_5438(var1);
               class_1799 var4 = var7.method_7972();
               int var5 = var1 < 9 ? 36 - ~var1 - 1 : var1;
               mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, var5, 1, class_1713.field_7795, mc.field_1724);
               if (var1 < 9) {
                  this.pickedHotbarSlots.remove(var1);
               }

               this.immediateDiscardSettleTicks = 0;
               this.immediateDiscardCooldown = 1;
               LOGGER.info(
                  "Immediately discarded filtered AutoEnd loot slot={} item={} count={} name='{}'",
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

   private void findWorld10() {
      if (this.automationContainerOpened && mc.field_1724 != null && mc.field_1724.field_7512 instanceof class_1707) {
         mc.field_1724.method_3137();
         mc.method_1507(null);
         this.automationContainerOpened = false;
         this.automationContainerOpenTicks = 0;
      } else {
         this.automationContainerOpened = false;
         this.automationContainerOpenTicks = 0;
      }
   }

   private class_2338 sendWorld() {
      class_2338 var1 = mc.field_1724.method_24515();
      class_2338 var2 = null;
      double var3 = Double.MAX_VALUE;

      for (class_2338 var6 : class_2338.method_25996(var1, 2, 4, 2)) {
         int var7 = var6.method_10263() + ~var1.method_10263() + 1;
         int var8 = var6.method_10260() + ~var1.method_10260() + 1;
         if (var7 * var7 - ~(var8 * var8) - 1 <= 4 && this.isPos2(var6) && !this.exhaustedHomeChests.contains(var6)) {
            class_2338 var9 = var6.method_10062();
            double var10 = mc.field_1724.method_33571().method_1025(var9.method_46558());
            if (!this.findChest(var9) && var10 < var3) {
               var2 = var9;
               var3 = var10;
            }
         }
      }

      return var2;
   }

   private List<class_2338> logCenterChunkX(int centerChunkX, int centerChunkZ, int chunkRadius) {
      ArrayList<class_2338> var4 = new ArrayList<>();

      for (int var5 = centerChunkX + ~chunkRadius + 1; var5 <= centerChunkX - ~chunkRadius - 1; var5++) {
         for (int var6 = centerChunkZ + ~chunkRadius + 1; var6 <= centerChunkZ - ~chunkRadius - 1; var6++) {
            class_2818 var7 = mc.field_1687.method_2935().method_21730(var5, var6);
            if (var7 != null) {
               for (class_2338 var9 : var7.method_12214().keySet()) {
                  if (this.isPos2(var9)) {
                     var4.add(var9.method_10062());
                  }
               }
            }
         }
      }

      return var4;
   }

   private boolean isPos2(class_2338 pos) {
      return pos != null && mc.field_1687 != null
         ? !mc.field_1687.method_8320(pos).method_27852(class_2246.field_10034) && !mc.field_1687.method_8320(pos).method_27852(class_2246.field_10380)
         : false;
   }

   private void logWorld7() {
      if (!this.isWorld()) {
         this.cycleState = AutoEnd.CycleState.FIND_PORTAL;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      }
   }

   private boolean isWorld() {
      if (this.storageRunActive && this.farmAnarchyBeforeStorage != null) {
         String var1 = this.farmAnarchyBeforeStorage;
         this.findWorld11();
         this.findStack4();
         this.findState();
         this.findWorld10();
         this.anarchyCommandPos = this.findWorld9();
         this.anarchyCommandWorld = mc.field_1687;
         this.expectedAnarchyNumber = var1;
         mc.field_1724.field_3944.method_45730("an" + var1);
         this.cycleState = AutoEnd.CycleState.FARM_RETURN_WAIT;
         this.cycleTicks = 0;
         this.actionDelay = 0;
         this.scanDelay = 0;
         LOGGER.info("AutoEnd finished storage work and sent /an{} to resume farming", var1);
         NotificationManager.send("Склад готов, возвращаюсь на /an" + var1, NotificationManager.Type.MODULE);
         return true;
      } else {
         return false;
      }
   }

   private void logPlayer15() {
      if (!this.isPlayer6()) {
         if (!this.isStack()) {
            this.logWorld7();
         } else if (this.scanDelay <= 0) {
            this.scanDelay = 10;
            this.supplyChest = this.findWorld8();
            if (this.supplyChest != null) {
               this.unavailableSupplyKinds.clear();
               this.cycleState = AutoEnd.CycleState.WALK_SUPPLY;
               this.cycleTicks = 0;
               this.pathRequested = false;
               this.repathDelay = 0;
            } else if (this.cycleTicks > 120) {
               LOGGER.warn("AutoEnd supply chest with sign '{}' was not found", this.supplySign.getValue());
               this.logWorld7();
            }
         }
      }
   }

   private boolean isPlayer6() {
      String var1 = this.logWorld16();
      if (var1 == null) {
         return false;
      } else if (this.detectedAnarchyNumber == null) {
         this.findStack4();
         this.logPlayer11();
         return true;
      } else if (var1.equals(this.detectedAnarchyNumber)) {
         return false;
      } else {
         if (!this.storageRunActive) {
            this.storageRunActive = true;
            this.farmAnarchyBeforeStorage = this.detectedAnarchyNumber;
            LOGGER.info("AutoEnd blocked chest work on /an{}; storage is /an{}", this.detectedAnarchyNumber, var1);
         }

         return this.isWorld4();
      }
   }

   private void logPlayer8() {
      if (this.isPos2(this.supplyChest) && this.findChest(this.supplyChest)) {
         if (mc.field_1724.method_33571().method_1025(this.supplyChest.method_46558()) <= 18.0625) {
            this.findStack4();
            this.cycleState = AutoEnd.CycleState.OPEN_SUPPLY;
            this.cycleTicks = 0;
            this.actionDelay = 0;
            this.setPlayer();
         } else {
            boolean var1 = this.navigator.isWorking();
            if (!this.pathRequested || !var1 && this.repathDelay <= 0) {
               this.pathRequested = this.navigator.navigate(this.supplyChest, 1, false);
               this.repathDelay = 8;
            }
         }
      } else {
         this.findStack4();
         this.supplyChest = null;
         this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      }
   }

   private void logPlayer20() {
      if (mc.field_1724.field_7512 instanceof class_1707) {
         this.automationContainerOpened = true;
         this.cycleState = AutoEnd.CycleState.TAKE_SUPPLY;
         this.cycleTicks = 0;
         this.actionDelay = 3;
      } else if (!this.isPos2(this.supplyChest) || !this.findChest(this.supplyChest)) {
         this.supplyChest = null;
         this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      } else if (this.actionDelay <= 0) {
         boolean var1 = this.findPos3(this.supplyChest);
         class_3965 var2 = this.findPos5(this.supplyChest);
         if (var2 == null && !var1) {
            if (this.cycleTicks > 100) {
               this.calculatePlayer3();
            }
         } else {
            if (var2 == null) {
               var2 = this.findPos9(this.supplyChest);
            }

            if (var2 == null) {
               if (this.cycleTicks > 100) {
                  this.calculatePlayer3();
               }
            } else {
               class_1269 var3 = mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, var2);
               mc.field_1724.method_6104(class_1268.field_5808);
               LOGGER.info("Tried to open supply chest {} side={} result={}", new Object[]{this.supplyChest, var2.method_17780(), var3});
               this.actionDelay = 12;
               if (this.cycleTicks > 100) {
                  this.calculatePlayer3();
               }
            }
         }
      }
   }

   private void calculatePlayer3() {
      if (this.supplyChest != null) {
         this.exhaustedSupplyChests.add(this.supplyChest.method_10062());
      }

      this.supplyChest = null;
      this.unavailableSupplyKinds.clear();
      this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
      this.cycleTicks = 0;
      this.scanDelay = 0;
      this.actionDelay = 0;
      this.setPlayer();
   }

   private void calculatePlayer2() {
      if (mc.field_1724.field_7512 instanceof class_1707 var1) {
         if (this.actionDelay <= 0) {
            AutoEnd.SupplyKind var6 = this.calculateStack();
            if (var6 == null) {
               boolean var7 = this.isStack();
               this.findWorld10();
               if (var7) {
                  this.exhaustedSupplyChests.add(this.supplyChest.method_10062());
                  this.supplyChest = null;
                  this.unavailableSupplyKinds.clear();
                  this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
                  this.cycleTicks = 0;
                  this.scanDelay = 0;
               } else {
                  this.supplyChest = null;
                  this.logWorld7();
               }
            } else {
               int var3 = var1.method_17388() * 9;
               int var4 = this.findHandler2(var1, var3, var6);
               if (var4 < 0) {
                  var4 = this.findHandler3(var1, var3, var6);
               }

               if (var4 < 0) {
                  this.unavailableSupplyKinds.add(var6);
                  this.actionDelay = 1;
               } else {
                  int var5 = Math.max(0, this.calculateKind(var6) + ~this.collectKind(var6) + 1);
                  if (!this.findHandler(var1, var4, var3, var5)) {
                     this.findWorld10();
                     this.exhaustedSupplyChests.add(this.supplyChest.method_10062());
                     this.supplyChest = null;
                     this.unavailableSupplyKinds.clear();
                     this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
                     this.cycleTicks = 0;
                     this.scanDelay = 0;
                  } else {
                     this.actionDelay = 3;
                  }
               }
            }
         }
      } else {
         this.automationContainerOpened = false;
         this.supplyChest = null;
         this.unavailableSupplyKinds.clear();
         this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      }
   }

   private int findHandler3(class_1707 handler, int chestSlots, AutoEnd.SupplyKind kind) {
      for (int var4 = 0; var4 < chestSlots; var4++) {
         if (this.containsStack2(handler.method_7611(var4).method_7677(), kind)) {
            return var4;
         }
      }

      return -1;
   }

   private int findHandler2(class_1707 handler, int chestSlots, AutoEnd.SupplyKind kind) {
      int var4 = Math.max(chestSlots, handler.field_7761.size() + -10 + 1);

      for (int var5 = chestSlots; var5 < var4; var5++) {
         if (this.containsStack2(handler.method_7611(var5).method_7677(), kind)) {
            return var5;
         }
      }

      return -1;
   }

   private boolean findHandler(class_1707 handler, int source, int chestSlots, int wanted) {
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

   private class_2338 findWorld8() {
      class_2338 var1 = mc.field_1724.method_24515();
      int var2 = Math.min(3, (Integer)mc.field_1690.method_42503().method_41753());
      int var3 = var1.method_10263() >> 4;
      int var4 = var1.method_10260() >> 4;
      return this.logCenterChunkX(var3, var4, var2)
         .stream()
         .filter(pos -> !this.exhaustedSupplyChests.contains(pos))
         .filter(this::findChest)
         .filter(pos -> !(var1.method_10262(pos) <= 1600.0))
         .min(Comparator.comparingDouble(pos -> var1.method_10262(pos)))
         .orElse(null);
   }

   private boolean findChest(class_2338 chest) {
      if (!this.isPos2(chest)) {
         return false;
      } else {
         for (class_2338 var3 : class_2338.method_10097(chest.method_10069(-1, -1, -1), chest.method_10069(1, 2, 1))) {
            if (mc.field_1687.method_8321(var3) instanceof class_2625 var4 && this.containsSign(var4) && this.findCandidate(chest, this.findSignPos(var3))) {
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
         if (this.isPos2(var3)) {
            return var3.method_10062();
         }
      } else if (var2.method_26204() instanceof class_7715) {
         class_2338 var10 = signPos.method_10093(((class_2350)var2.method_11654(class_7715.field_40319)).method_10153());
         if (this.isPos2(var10)) {
            return var10.method_10062();
         }
      }

      class_2338 var11 = null;
      double var4 = Double.MAX_VALUE;

      for (class_2338 var7 : class_2338.method_25996(signPos, 2, 2, 2)) {
         if (this.isPos2(var7)) {
            double var8 = signPos.method_10262(var7);
            if (var8 < var4) {
               var11 = var7.method_10062();
               var4 = var8;
            }
         }
      }

      return var11;
   }

   private boolean findCandidate(class_2338 candidate, class_2338 assigned) {
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
      String var5 = this.calculateText(sign.method_49854());
      String var6 = this.calculateText(sign.method_49853());
      String var2 = this.calculateText2(var6 + " " + var5);

      for (String var4 : this.calculatePlayer4()) {
         if (!var4.isBlank() && var2.contains(var4)) {
            return true;
         }
      }

      return false;
   }

   private String calculateText(class_8242 text) {
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < 4; var3++) {
         var2.append(' ').append(text.method_49859(var3, false).getString());
      }

      return var2.toString();
   }

   private List<String> calculatePlayer4() {
      ArrayList var1 = new ArrayList();
      String var2 = this.supplySign.getValue();
      if (var2 != null) {
         for (String var6 : var2.split("[,;\\s]+")) {
            String var7 = this.calculateText2(var6).trim();
            if (!var7.isBlank()) {
               var1.add(var7);
            }
         }
      }

      if (var1.isEmpty()) {
         var1.add(this.calculateText2("снабжение"));
      }

      return var1;
   }

   private String calculateText2(String text) {
      return text == null ? "" : text.replaceAll("§.", "").toLowerCase(Locale.ROOT).trim();
   }

   private boolean isStack() {
      if (this.autoSupplies.getValue() && mc.field_1724 != null) {
         for (AutoEnd.SupplyKind var4 : AutoEnd.SupplyKind.values()) {
            if (this.isKind2(var4) && this.collectKind(var4) < this.calculateKind(var4)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private AutoEnd.SupplyKind calculateStack() {
      for (AutoEnd.SupplyKind var4 : AutoEnd.SupplyKind.values()) {
         if (this.isKind2(var4) && !this.unavailableSupplyKinds.contains(var4) && this.collectKind(var4) < this.calculateKind(var4)) {
            return var4;
         }
      }

      return null;
   }

   private boolean isKind2(AutoEnd.SupplyKind kind) {
      return switch (kind) {
         case FOOD -> this.takeFood.getValue();
         case INVISIBILITY -> this.takeInvisibility.getValue();
         case SPEED -> this.takeSpeed.getValue();
      };
   }

   private int calculateKind(AutoEnd.SupplyKind kind) {
      return Math.max(1, Math.round(switch (kind) {
         case FOOD -> this.foodAmount.getValue();
         case INVISIBILITY -> this.invisibilityAmount.getValue();
         case SPEED -> this.speedAmount.getValue();
      }));
   }

   private int collectKind(AutoEnd.SupplyKind kind) {
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
      if (stack != null && !stack.method_7960()) {
         if (stack.method_31574(class_1802.field_42716)) {
            return true;
         } else {
            for (AutoEnd.SupplyKind var5 : AutoEnd.SupplyKind.values()) {
               if (this.isKind2(var5) && this.containsStack2(stack, var5)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean containsStack2(class_1799 stack, AutoEnd.SupplyKind kind) {
      if (stack != null && !stack.method_7960()) {
         return (Boolean)(
            switch (kind) {
               case FOOD -> stack.method_58694(class_9334.field_50075) != null
                  && !((class_4174)stack.method_58694(class_9334.field_50075)).comp_2493()
                  && !this.calculateText2(stack.method_7964().getString()).contains("пласт")
               ? 1
               : 0;
               case INVISIBILITY -> this.containsStack(stack, class_1294.field_5905, "невид", "инвиз", "invis");
               case SPEED -> this.containsStack(stack, class_1294.field_5904, "скорост", "speed");
            }
         );
      } else {
         return false;
      }
   }

   private boolean containsStack(class_1799 stack, class_6880<class_1291> effect, String... nameFallbacks) {
      if (!stack.method_31574(class_1802.field_8574)) {
         return false;
      } else {
         class_1844 var4 = (class_1844)stack.method_58694(class_9334.field_49651);
         if (var4 != null) {
            for (class_1293 var6 : var4.method_57397()) {
               if (var6.method_5579().equals(effect)) {
                  return true;
               }
            }
         }

         String var10 = this.calculateText2(stack.method_7964().getString());

         for (String var9 : nameFallbacks) {
            if (var10.contains(var9)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean isAllowPotions(boolean allowPotions) {
      if (this.consumableReleaseTicks > 0) {
         if (mc.field_1690 != null) {
            mc.field_1690.field_1904.method_23481(false);
         }

         this.consumableReleaseTicks = this.consumableReleaseTicks + -2 + 1;
         return true;
      } else if (this.activeConsumable != null) {
         return this.isWorld9();
      } else if (this.autoSupplies.getValue() && mc.field_1755 == null) {
         AutoEnd.SupplyKind var2 = null;
         if (this.takeFood.getValue() && mc.field_1724.method_7344().method_7586() < 20) {
            var2 = AutoEnd.SupplyKind.FOOD;
         } else if (allowPotions && this.takeInvisibility.getValue() && this.isEffect(class_1294.field_5905)) {
            var2 = AutoEnd.SupplyKind.INVISIBILITY;
         } else if (allowPotions && this.takeSpeed.getValue() && this.isEffect(class_1294.field_5904)) {
            var2 = AutoEnd.SupplyKind.SPEED;
         }

         return var2 != null && this.isKind3(var2);
      } else {
         return false;
      }
   }

   private boolean isPlayer4() {
      return switch (this.cycleState) {
         case DARENA_WAIT, ANARCHY_WAIT, FARM_RETURN_WAIT, OPEN_CHEST, DEPOSIT, OPEN_SUPPLY, TAKE_SUPPLY, ENTER_PORTAL -> true;
         default -> false;
      };
   }

   private boolean isEffect(class_6880<class_1291> effect) {
      class_1293 var2 = mc.field_1724.method_6112(effect);
      return var2 != null && (var2.method_48559() || var2.method_5584() > 200);
   }

   private boolean isKind3(AutoEnd.SupplyKind kind) {
      int var2 = mc.field_1724.method_31548().method_67532();
      int var3 = this.logKind(kind);
      if (var3 < 0) {
         return false;
      } else {
         this.consumablePreviousSlot = var2;
         this.consumableHotbarSlot = var3;
         this.logSlot(var3);
         this.activeConsumable = kind;
         this.consumableUseDelay = 2;
         this.consumableTicks = 0;
         this.consumableInteractCooldown = 0;
         this.consumableAvoidChest = this.findWorld3();
         LOGGER.info("Pausing {} to use {} from hotbar slot {}", new Object[]{this.cycleState, kind, var3});
         return true;
      }
   }

   private boolean isWorld9() {
      if (mc.field_1755 == null && this.isKind(this.activeConsumable)) {
         if (this.consumableHotbarSlot >= 0
            && this.containsStack2(mc.field_1724.method_31548().method_5438(this.consumableHotbarSlot), this.activeConsumable)
            && (this.consumableTicks = this.consumableTicks - -2 - 1) <= 140) {
            this.logSlot(this.consumableHotbarSlot);
            boolean var1 = this.findWorld();
            if (!var1 && !mc.field_1724.method_6115()) {
               mc.field_1690.field_1904.method_23481(false);
               return true;
            } else {
               int var10000 = this.consumableUseDelay;
               this.consumableUseDelay = this.consumableUseDelay + -2 + 1;
               if (var10000 > 0) {
                  return true;
               } else {
                  mc.field_1690.field_1904.method_23481(true);
                  if (!mc.field_1724.method_6115()) {
                     if (this.consumableInteractCooldown <= 0) {
                        class_1269 var2 = mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                        LOGGER.info("Tried to use {} from slot {} result={}", new Object[]{this.activeConsumable, this.consumableHotbarSlot, var2});
                        this.consumableInteractCooldown = 10;
                     } else {
                        this.consumableInteractCooldown = this.consumableInteractCooldown + -2 + 1;
                     }
                  } else {
                     this.consumableInteractCooldown = 10;
                  }

                  return true;
               }
            }
         } else {
            this.findWorld11();
            return this.consumableReleaseTicks > 0;
         }
      } else {
         if (mc.field_1755 == null) {
            LOGGER.info("class_10127 {} completed; resuming {}", this.activeConsumable, this.cycleState);
         }

         this.findWorld11();
         return this.consumableReleaseTicks <= 0;
      }
   }

   private class_2338 findWorld3() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1765 instanceof class_3965 var1 && var1.method_17783() == class_240.field_1332 && this.isPos(var1.method_17777())) {
            return var1.method_17777().method_10062();
         }

         class_2338 var4 = mc.field_1724.method_24515();

         for (class_2338 var3 : class_2338.method_25996(var4, 6, 4, 6)) {
            if (this.isPos(var3)) {
               return var3.method_10062();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean findWorld() {
      if (this.consumableAvoidChest == null || mc.field_1724 == null || mc.field_1687 == null) {
         return true;
      } else if (!this.isPos(this.consumableAvoidChest)) {
         this.consumableAvoidChest = this.findWorld3();
         return this.consumableAvoidChest != null;
      } else {
         class_243 var1 = this.consumableAvoidChest.method_46558();
         double var2 = var1.field_1352 - mc.field_1724.method_23317();
         double var4 = var1.field_1350 - mc.field_1724.method_23321();
         float var6 = (float)Math.toDegrees(Math.atan2(var4, var2)) - 90.0F;
         float var7 = class_3532.method_15393(var6 + 180.0F);
         float var8 = class_3532.method_15393(var7 - mc.field_1724.method_36454());
         float var9 = -28.0F;
         float var10 = var9 - mc.field_1724.method_36455();
         if (mc.field_1724.method_24828()) {
            mc.field_1724.method_36456(mc.field_1724.method_36454() + class_3532.method_15363(var8, -14.0F, 14.0F));
            mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455() + class_3532.method_15363(var10, -7.0F, 7.0F), -90.0F, 90.0F));
         }

         byte var10000;
         label39: {
            if (mc.field_1765 instanceof class_3965 var12 && var12.method_17783() == class_240.field_1332 && this.isPos(var12.method_17777())) {
               var10000 = 1;
               break label39;
            }

            var10000 = 0;
         }

         int var11 = var10000;
         return var11 != 0 || !(Math.abs(var8) <= 12.0F) || !(Math.abs(var10) <= 8.0F);
      }
   }

   private boolean isPos(class_2338 pos) {
      return this.isPos2(pos) || pos != null && mc.field_1687 != null && mc.field_1687.method_8320(pos).method_27852(class_2246.field_16328);
   }

   private boolean isKind(AutoEnd.SupplyKind kind) {
      if (kind == null) {
         return false;
      } else {
         return switch (kind) {
            case FOOD -> mc.field_1724.method_7344().method_7586() < 20;
            case INVISIBILITY -> this.isEffect(class_1294.field_5905);
            case SPEED -> this.isEffect(class_1294.field_5904);
         };
      }
   }

   private int logKind(AutoEnd.SupplyKind kind) {
      for (int var2 = 0; var2 < 9; var2++) {
         if (this.containsStack2(mc.field_1724.method_31548().method_5438(var2), kind)) {
            return var2;
         }
      }

      return -1;
   }

   private void logSlot(int slot) {
      if (slot >= 0 && slot <= 8 && mc.field_1724.method_31548().method_67532() != slot) {
         mc.field_1724.method_31548().method_61496(slot);
      }
   }

   private boolean isStack2() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         class_1799 var1 = mc.field_1724.method_31548().method_5438(8);
         int var2 = !var1.method_7960() && !this.containsStack3(var1) ? 1 : 0;
         int var3 = !this.hotbarCleanupPending && this.pickedHotbarSlots.isEmpty() && var2 == 0 && !this.automationInventoryOpened ? 0 : 1;
         if (var3 == 0) {
            return false;
         } else if (this.activeConsumable != null || this.previousSelectedSlot >= 0 || this.brushingStarted || this.automationContainerOpened) {
            return false;
         } else if (!this.automationInventoryOpened) {
            if (mc.field_1755 != null) {
               return false;
            } else {
               this.logPlayer11();
               mc.method_1507(new class_490(mc.field_1724));
               this.automationInventoryOpened = true;
               this.hotbarCleanupDelay = 3;
               LOGGER.info("Opened player inventory to move picked hotbar loot {}", this.pickedHotbarSlots);
               return true;
            }
         } else if (mc.field_1755 instanceof class_490 && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
            this.logPlayer11();
            int var10000 = this.hotbarCleanupDelay;
            this.hotbarCleanupDelay = this.hotbarCleanupDelay + -2 + 1;
            if (var10000 > 0) {
               return true;
            } else {
               for (int var5 : new ArrayList<>(this.pickedHotbarSlots)) {
                  class_1799 var6 = mc.field_1724.method_31548().method_5438(var5);
                  if (!var6.method_7960() && !this.containsStack4(var6) && !this.containsStack3(var6)) {
                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, 36 - ~var5 - 1, 0, class_1713.field_7794, mc.field_1724);
                     LOGGER.info("Moved picked stack from hotbar slot {} into main inventory", var5);
                     this.pickedHotbarSlots.remove(var5);
                     this.hotbarCleanupDelay = 3;
                     return true;
                  }

                  this.pickedHotbarSlots.remove(var5);
               }

               class_1799 var7 = mc.field_1724.method_31548().method_5438(8);
               if (!var7.method_7960() && !this.containsStack3(var7)) {
                  int var9 = this.collectStack();
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
                  this.findWorld13();
                  boolean var8 = mc.field_1724.method_31548().method_5438(8).method_7960();
                  if (var8) {
                     this.logSlot(8);
                  }

                  this.screenResumeDelay = 2;
                  LOGGER.info(
                     var8 ? "Hotbar cleanup completed; selected empty ninth slot" : "Hotbar cleanup completed; left armor/head untouched in ninth slot"
                  );
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

   private int collectStack() {
      for (int var1 = 0; var1 < 8; var1++) {
         if (mc.field_1724.method_31548().method_5438(var1).method_7960()) {
            return var1;
         }
      }

      return -1;
   }

   private boolean containsStack3(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_10192 var2 = (class_10192)stack.method_58694(class_9334.field_54196);
         if (var2 == null) {
            return false;
         } else {
            class_1304 var3 = var2.comp_3174();
            return var3 != class_1304.field_6169 && var3 != class_1304.field_6174 && var3 != class_1304.field_6172 && var3 != class_1304.field_6166;
         }
      } else {
         return false;
      }
   }

   private void findPlayer7() {
      if (mc.field_1724 != null
         && mc.field_1755 == null
         && this.activeConsumable == null
         && this.previousSelectedSlot < 0
         && !this.brushingStarted
         && !this.automationInventoryOpened) {
         if (!mc.field_1724.method_31548().method_5438(8).method_7960()) {
            if (!this.containsStack3(mc.field_1724.method_31548().method_5438(8))) {
               this.hotbarCleanupPending = true;
            }
         } else {
            this.logSlot(8);
         }
      }
   }

   private void findWorld13() {
      if (this.automationInventoryOpened && mc.field_1724 != null && mc.field_1755 instanceof class_490) {
         mc.field_1724.method_3137();
         mc.method_1507(null);
      }

      this.automationInventoryOpened = false;
      this.hotbarCleanupDelay = 0;
   }

   private void findWorld7() {
      this.pickedHotbarSlots.clear();
      this.hotbarCleanupPending = false;
      this.automationInventoryOpened = false;
      this.hotbarCleanupDelay = 0;
      this.immediateDiscardCooldown = 0;
      this.immediateDiscardSettleTicks = 0;
      this.immediateDiscardStopping = false;
      this.logPlayer4();
   }

   private void findWorld11() {
      boolean var1 = this.activeConsumable == null && (mc.field_1724 == null || !mc.field_1724.method_6115());
      if (mc.field_1690 != null) {
         var1 |= mc.field_1690.field_1904.method_1434();
         mc.field_1690.field_1904.method_23481(false);
      }

      if (mc.field_1724 != null && this.consumablePreviousSlot >= 0) {
         this.logSlot(8);
      }

      this.activeConsumable = null;
      this.consumablePreviousSlot = -1;
      this.consumableHotbarSlot = -1;
      this.consumableUseDelay = 0;
      this.consumableTicks = 0;
      this.consumableInteractCooldown = 0;
      this.consumableAvoidChest = null;
      this.consumableReleaseTicks = var1 ? 2 : 0;
   }

   private void findWorld6() {
      if (this.scanDelay <= 0) {
         this.scanDelay = 20;
         this.portalTarget = this.findWorld2();
         if (this.portalTarget != null) {
            this.findPortal(this.portalTarget);
         }
      }
   }

   private void findPortal(class_2338 portal) {
      this.findStack4();
      this.findState();
      this.portalTarget = this.logSeed(portal);
      this.portalWalkGoal = this.findPortal2(this.portalTarget);
      this.portalEntryWorld = mc.field_1687;
      this.portalEntryPos = this.findWorld9();
      this.cycleState = AutoEnd.CycleState.WALK_PORTAL;
      this.cycleTicks = 0;
      this.scanDelay = 0;
      this.pathRequested = false;
      this.repathDelay = 0;
      this.initialPortalCheckTicks = 0;
   }

   private class_2338 findWorld2() {
      class_2338 var1 = mc.field_1724.method_24515();

      for (class_2338 var3 : class_2338.method_25996(var1, 48, 20, 48)) {
         if (this.isPos3(var3)) {
            return this.logSeed(var3);
         }
      }

      return null;
   }

   private class_2338 logSeed(class_2338 seed) {
      if (!this.isPos3(seed)) {
         return seed == null ? null : seed.method_10062();
      } else {
         int var2 = seed.method_10263();
         int var3 = seed.method_10263();
         int var4 = seed.method_10260();
         int var5 = seed.method_10260();

         for (int var6 = seed.method_10263() + -5 + 1; var6 <= seed.method_10263() - -5 - 1; var6++) {
            for (int var7 = seed.method_10260() + -5 + 1; var7 <= seed.method_10260() - -5 - 1; var7++) {
               class_2338 var8 = new class_2338(var6, seed.method_10264(), var7);
               if (this.isPos3(var8)) {
                  var2 = Math.min(var2, var6);
                  var3 = Math.max(var3, var6);
                  var4 = Math.min(var4, var7);
                  var5 = Math.max(var5, var7);
               }
            }
         }

         return new class_2338((var2 - ~var3 - 1) / 2, seed.method_10264(), (var4 - ~var5 - 1) / 2);
      }
   }

   private boolean isPos3(class_2338 pos) {
      return pos != null && mc.field_1687 != null && mc.field_1687.method_8320(pos).method_27852(class_2246.field_10027);
   }

   private void logPlayer19() {
      if (this.isWorld6()) {
         this.setState();
      } else if (!this.isPos3(this.portalTarget)) {
         this.findStack4();
         this.portalTarget = null;
         this.portalWalkGoal = null;
         this.cycleState = AutoEnd.CycleState.FIND_PORTAL;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      } else {
         double var1 = mc.field_1724.method_23317() - ((double)this.portalTarget.method_10263() + 0.5);
         double var3 = mc.field_1724.method_23321() - ((double)this.portalTarget.method_10260() + 0.5);
         double var5 = mc.field_1724.method_23318() - (double)this.portalTarget.method_10264();
         if (var1 * var1 + var3 * var3 <= 7.0 && Math.abs(var5) <= 3.5) {
            this.logReason("near portal frame");
         } else if (this.navigator.hasFailed() && var1 * var1 + var3 * var3 <= 64.0 && Math.abs(var5) <= 4.5) {
            this.logReason("portal route failed");
         } else {
            if (this.portalWalkGoal == null || !this.navigator.isWalkableNode(this.portalWalkGoal)) {
               this.portalWalkGoal = this.findPortal2(this.portalTarget);
            }

            if (this.portalWalkGoal == null) {
               this.findStack4();
               this.portalTarget = null;
               this.cycleState = AutoEnd.CycleState.FIND_PORTAL;
               this.cycleTicks = 0;
               this.scanDelay = 0;
            } else {
               boolean var7 = this.navigator.isWorking();
               if (!this.pathRequested || !var7 && this.repathDelay <= 0) {
                  this.pathRequested = this.navigator.navigate(this.portalWalkGoal, 0, false);
                  this.repathDelay = 8;
               }
            }
         }
      }
   }

   private void logReason(String reason) {
      LOGGER.info(
         "Switching to direct End portal entry: {} (portal={}, player={})",
         new Object[]{reason, this.portalTarget, mc.field_1724 == null ? null : mc.field_1724.method_24515()}
      );
      this.findStack4();
      this.cycleState = AutoEnd.CycleState.ENTER_PORTAL;
      this.cycleTicks = 0;
   }

   private class_2338 findPortal2(class_2338 portal) {
      if (portal != null && mc.field_1724 != null) {
         double var2 = mc.field_1724.method_23317() - ((double)portal.method_10263() + 0.5);
         double var4 = mc.field_1724.method_23321() - ((double)portal.method_10260() + 0.5);
         int var6;
         int var7;
         if (Math.abs(var2) >= Math.abs(var4)) {
            var6 = var2 >= 0.0 ? 1 : -1;
            var7 = 0;
         } else {
            var6 = 0;
            var7 = var4 >= 0.0 ? 1 : -1;
         }

         int var8 = -var7;
         int var9 = var6;

         for (int var10 = 2; var10 <= 4; var10++) {
            for (int var11 = 0; var11 <= 1; var11++) {
               int[] var12 = var11 == 0 ? new int[]{0} : new int[]{-var11, var11};

               for (int var16 : var12) {
                  int var17 = portal.method_10263() - ~(var6 * var10) - 1 - ~(var8 * var16) - 1;
                  int var18 = portal.method_10260() - ~(var7 * var10) - 1 - ~(var9 * var16) - 1;

                  for (int var19 = 0; var19 <= 4; var19++) {
                     class_2338 var20 = new class_2338(var17, portal.method_10264() - ~var19 - 1, var18);
                     if (this.navigator.isWalkableNode(var20)) {
                        return var20.method_10062();
                     }

                     if (var19 > 0) {
                        class_2338 var21 = new class_2338(var17, portal.method_10264() + ~var19 + 1, var18);
                        if (this.navigator.isWalkableNode(var21)) {
                           return var21.method_10062();
                        }
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean isWorld6() {
      int var1 = this.portalEntryWorld != null && mc.field_1687 != this.portalEntryWorld ? 1 : 0;
      int var2 = this.portalEntryPos != null && this.findWorld9().method_1025(this.portalEntryPos) > 256.0 ? 1 : 0;
      return this.cycleTicks <= 8 || var1 == 0 && var2 == 0;
   }

   private void calculatePlayer() {
      if (this.isWorld6()) {
         this.setState();
      } else if (!this.isPos3(this.portalTarget)) {
         this.findState();
         this.portalTarget = null;
         this.portalWalkGoal = null;
         this.cycleState = AutoEnd.CycleState.FIND_PORTAL;
         this.cycleTicks = 0;
         this.scanDelay = 0;
      } else {
         if (mc.field_1690 != null) {
            double var1 = (double)this.portalTarget.method_10263() + 0.5 - mc.field_1724.method_23317();
            double var3 = (double)this.portalTarget.method_10260() + 0.5 - mc.field_1724.method_23321();
            double var5 = var1 * var1 + var3 * var3;
            float var7 = (float)Math.toDegrees(Math.atan2(var3, var1)) - 90.0F;
            float var8 = class_3532.method_15393(var7 - mc.field_1724.method_36454());
            float var9 = class_3532.method_15363(var8, -14.0F, 14.0F);
            mc.field_1724.method_36456(mc.field_1724.method_36454() + var9);
            int var10 = Math.abs(var8) <= 38.0F ? 1 : 0;
            mc.field_1690.field_1894.method_23481(var10 != 0 && var5 > 0.04);
            mc.field_1690.field_1867.method_23481(false);
            mc.field_1690.field_1903.method_23481(var10 != 0);
         }

         if (this.cycleTicks > 220) {
            this.findState();
            this.findStack4();
            this.portalTarget = null;
            this.portalWalkGoal = null;
            this.cycleState = AutoEnd.CycleState.FIND_PORTAL;
            this.cycleTicks = 0;
            this.scanDelay = 0;
         }
      }
   }

   private void setState() {
      this.findWorld11();
      this.findState();
      this.findStack4();
      this.brokenPots = 0;
      this.returnHomePending = false;
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
      this.homeChest = null;
      this.supplyChest = null;
      this.portalTarget = null;
      this.portalWalkGoal = null;
      this.patrolGoal = null;
      this.darenaCommandPos = null;
      this.darenaCommandWorld = null;
      this.darenaMenuClickCooldown = 0;
      this.homeCommandPos = null;
      this.homeCommandWorld = null;
      this.homeTeleportConfirmed = false;
      this.pvpClearTicks = 0;
      this.pvpRemainingSeconds = -1;
      this.portalEntryPos = null;
      this.portalEntryWorld = null;
      this.deathRecoveryPending = false;
      this.deathRecoveryActive = false;
      this.homeForPortalAfterStorage = false;
      this.cycleState = AutoEnd.CycleState.FARMING;
      this.cycleTicks = 0;
      this.scanDelay = 0;
   }

   private void findState() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1867.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
      }
   }

   private void findPlayer3() {
      this.findWorld11();
      this.cycleState = AutoEnd.CycleState.FARMING;
      this.brokenPots = 0;
      this.returnHomePending = false;
      this.deathRecoveryPending = false;
      this.deathRecoveryActive = false;
      this.initialPortalCheckTicks = 0;
      this.cycleTicks = 0;
      this.actionDelay = 0;
      this.homeChest = null;
      this.supplyChest = null;
      this.portalTarget = null;
      this.portalWalkGoal = null;
      this.depositSlot = 0;
      this.automationContainerOpened = false;
      this.automationContainerOpenTicks = 0;
      this.exhaustedHomeChests.clear();
      this.exhaustedSupplyChests.clear();
      this.unavailableSupplyKinds.clear();
      this.patrolGoal = null;
      this.darenaCommandPos = null;
      this.darenaCommandWorld = null;
      this.darenaMenuClickCooldown = 0;
      this.homeCommandPos = null;
      this.homeCommandWorld = null;
      this.homeTeleportConfirmed = false;
      this.pvpClearTicks = 0;
      this.pvpRemainingSeconds = -1;
      this.portalEntryPos = null;
      this.portalEntryWorld = null;
      this.anarchyCommandPos = null;
      this.anarchyCommandWorld = null;
      this.expectedAnarchyNumber = null;
      this.farmAnarchyBeforeStorage = null;
      this.storageRunActive = false;
      this.homeForPortalAfterStorage = false;
      this.lastMovementPos = null;
      this.stationaryTicks = 0;
      this.stalledRepaths = 0;
   }

   private void logPlayer5() {
      if (this.cycleState == AutoEnd.CycleState.DARENA_WAIT
         || this.cycleState == AutoEnd.CycleState.ANARCHY_WAIT
         || this.cycleState == AutoEnd.CycleState.FARM_RETURN_WAIT
         || this.cycleState == AutoEnd.CycleState.HOME_WAIT
         || this.cycleState == AutoEnd.CycleState.OPEN_CHEST
         || this.cycleState == AutoEnd.CycleState.DEPOSIT
         || this.cycleState == AutoEnd.CycleState.OPEN_SUPPLY
         || this.cycleState == AutoEnd.CycleState.TAKE_SUPPLY
         || this.cycleState == AutoEnd.CycleState.ENTER_PORTAL) {
         this.logPlayer3();
      } else if (!this.navigator.hasFailed()) {
         int var7 = !this.pathRequested && !this.navigator.isWorking() ? 0 : 1;
         if (var7 == 0) {
            this.logPlayer3();
         } else {
            class_2338 var2 = this.navigator.getGoal();
            if (var2 == null) {
               this.logPlayer13();
            } else {
               class_2338 var3 = this.navigator.getWaypoint();
               double var4 = mc.field_1724.method_73189().method_1025(class_243.method_24953(var2));
               if (!var2.equals(this.progressTrackedGoal)) {
                  this.progressTrackedGoal = var2.method_10062();
                  this.progressTrackedWaypoint = var3 == null ? null : var3.method_10062();
                  this.bestProgressDistanceSq = var4;
                  this.noRouteProgressTicks = 0;
               } else {
                  int var6 = var3 == null ? (this.progressTrackedWaypoint != null ? 1 : 0) : (!var3.equals(this.progressTrackedWaypoint) ? 1 : 0);
                  if (var6 != 0) {
                     this.progressTrackedWaypoint = var3 == null ? null : var3.method_10062();
                     this.bestProgressDistanceSq = Math.min(this.bestProgressDistanceSq, var4);
                     this.noRouteProgressTicks = 0;
                  } else if (var4 + 1.0 < this.bestProgressDistanceSq) {
                     this.bestProgressDistanceSq = var4;
                     this.noRouteProgressTicks = 0;
                  } else {
                     this.noRouteProgressTicks = this.noRouteProgressTicks - -2 - 1;
                     if (this.noRouteProgressTicks >= 120) {
                        LOGGER.warn(
                           "AutoEnd route to {} made no progress for {} ticks at {} (waypoint={}); starting home recovery",
                           new Object[]{var2, this.noRouteProgressTicks, mc.field_1724.method_24515(), this.navigator.getWaypoint()}
                        );
                        this.logPlayer9();
                     }
                  }
               }
            }
         }
      } else if (this.cycleState == AutoEnd.CycleState.WALK_PORTAL && this.isPos3(this.portalTarget)) {
         this.logReason("portal route failed before stall reset");
      } else {
         this.logForce(true);
         LOGGER.warn("AutoEnd navigator could not build or execute route to {}", this.navigator.getGoal());
         if (this.cycleState == AutoEnd.CycleState.FARMING) {
            if (this.isPlayer5()) {
               return;
            }

            if (this.lootOrigin != null) {
               class_2338 var1 = this.lootGoal;
               LOGGER.warn("Stopping loot collection from {} after route failure to {}", this.lootOrigin, var1);
               this.collectPlayer3();
            } else {
               this.logPlayer17();
            }
         } else {
            this.logPlayer10();
         }
      }
   }

   private void logPlayer10() {
      this.findStack4();
      if (this.cycleState == AutoEnd.CycleState.WALK_CHEST && this.homeChest != null) {
         this.exhaustedHomeChests.add(this.homeChest.method_10062());
         this.homeChest = null;
         this.cycleState = AutoEnd.CycleState.FIND_CHEST;
      } else if (this.cycleState == AutoEnd.CycleState.WALK_SUPPLY && this.supplyChest != null) {
         this.exhaustedSupplyChests.add(this.supplyChest.method_10062());
         this.supplyChest = null;
         this.unavailableSupplyKinds.clear();
         this.cycleState = AutoEnd.CycleState.FIND_SUPPLY;
      } else if (this.cycleState == AutoEnd.CycleState.WALK_PORTAL) {
         this.portalTarget = null;
         this.portalWalkGoal = null;
         this.cycleState = AutoEnd.CycleState.FIND_PORTAL;
      } else if (this.cycleState == AutoEnd.CycleState.PVP_WAIT) {
         this.patrolGoal = null;
      }

      this.cycleTicks = 0;
      this.scanDelay = 0;
   }

   private void logPlayer3() {
      this.lastMovementPos = null;
      this.stationaryTicks = 0;
      this.stalledRepaths = 0;
      this.nonExecutingPathTicks = 0;
      this.logPlayer13();
   }

   private void logPlayer13() {
      this.progressTrackedGoal = null;
      this.progressTrackedWaypoint = null;
      this.bestProgressDistanceSq = Double.MAX_VALUE;
      this.noRouteProgressTicks = 0;
   }

   private void logPlayer17() {
      class_243 var1 = this.findWorld9();
      if (this.targetBlock != null) {
         class_2338 var2 = this.targetBlock.method_10062();
         this.temporarilySkippedTargets.put(var2, 1200);
         LOGGER.warn("Skipping failed {} target {} for {} ticks", new Object[]{this.targetBrushable ? "brushable" : "pot", var2, 1200});
         this.findStack4();
         this.findStopPath(false);
         this.scanDelay = 1;
      } else {
         this.findStack4();
         this.patrolGoal = null;
         this.scanDelay = 0;
         this.repathDelay = 8;
      }

      this.stationaryTicks = 0;
      this.nonExecutingPathTicks = 0;
      this.lastMovementPos = var1;
      this.stalledRepaths = 0;
   }

   private boolean isPlayer5() {
      class_2338 var1 = mc.field_1724.method_24515();
      if (this.routeFailureAnchor != null && !(this.routeFailureAnchor.method_10262(var1) > 4.0)) {
         this.routeFailuresAtAnchor = this.routeFailuresAtAnchor - -2 - 1;
         if (this.routeFailuresAtAnchor < 8) {
            return false;
         } else {
            LOGGER.warn("AutoEnd is trapped at {} after {} consecutive route failures; starting home recovery", var1, this.routeFailuresAtAnchor);
            this.routeFailureAnchor = null;
            this.routeFailuresAtAnchor = 0;
            this.logPlayer9();
            return true;
         }
      } else {
         this.routeFailureAnchor = var1.method_10062();
         this.routeFailuresAtAnchor = 1;
         return false;
      }
   }

   private void logPlayer9() {
      this.logPlayer13();
      this.findStack4();
      this.patrolGoal = null;
      this.findStopPath(false);
      this.collectPlayer2();
      this.logWorld6();
      this.logPlayer11();
   }

   private void logForce(boolean force) {
      if (force || (this.navigationLogTicks = this.navigationLogTicks - -2 - 1) >= 10) {
         this.navigationLogTicks = 0;
         if (mc.field_1724 != null && mc.field_1690 != null) {
            boolean var2 = this.navigator.isWorking();
            boolean var3 = this.navigator.hasPath();
            if (force || this.pathRequested || var2 || this.targetBlock != null || this.patrolGoal != null) {
               class_243 var4 = mc.field_1724.method_18798();
               LOGGER.info(
                  "[AutoEndNav] state={} target={} brush={} patrol={} pos={} vel=({},{},{}) ground={} sprint={} requested={} hasPath={} pathing={} failed={} pathSize={} keys[F={},B={},L={},R={},J={},S={}] goal={} waypoint={}",
                  new Object[]{
                     this.cycleState,
                     this.targetBlock,
                     this.targetBrushable,
                     this.patrolGoal,
                     mc.field_1724.method_24515(),
                     String.format(Locale.ROOT, "%.3f", var4.field_1352),
                     String.format(Locale.ROOT, "%.3f", var4.field_1351),
                     String.format(Locale.ROOT, "%.3f", var4.field_1350),
                     mc.field_1724.method_24828(),
                     mc.field_1724.method_5624(),
                     this.pathRequested,
                     var3,
                     var2,
                     this.navigator.hasFailed(),
                     this.navigator.getPathSize(),
                     mc.field_1690.field_1894.method_1434(),
                     mc.field_1690.field_1881.method_1434(),
                     mc.field_1690.field_1913.method_1434(),
                     mc.field_1690.field_1849.method_1434(),
                     mc.field_1690.field_1903.method_1434(),
                     mc.field_1690.field_1867.method_1434(),
                     this.navigator.getGoal(),
                     this.navigator.getWaypoint()
                  }
               );
            }
         }
      }
   }

   private class_243 findWorld9() {
      return new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
   }

   private AutoEnd.TargetScanResult findWorld5() {
      class_2338 var1 = mc.field_1724.method_24515();
      int var2 = var1.method_10263() >> 4;
      int var3 = var1.method_10260() >> 4;
      int var4 = Math.min(8, (Integer)mc.field_1690.method_42503().method_41753());
      boolean var5 = this.isPlayer();
      if (this.targetScanCenterChunkX != var2 || this.targetScanCenterChunkZ != var3 || this.targetScanRadius != var4 || this.targetScanCanBrush != var5) {
         this.findCenterChunkX(var2, var3, var4, var5);
      }

      int var6 = var4 * 2 - -2 - 1;
      int var7 = var6 * var6;
      int var8 = 0;

      while (this.targetScanCursor < var7 && var8 < 24) {
         int var9 = this.targetScanCursor / var6;
         int var10 = this.targetScanCursor % var6;
         this.targetScanCursor = this.targetScanCursor - -2 - 1;
         var8++;
         int var11 = var2 + ~var4 + 1 - ~var9 - 1;
         int var12 = var3 + ~var4 + 1 - ~var10 - 1;
         class_2818 var13 = mc.field_1687.method_2935().method_21730(var11, var12);
         if (var13 != null) {
            for (class_2338 var15 : var13.method_12214().keySet()) {
               if (!this.temporarilySkippedTargets.containsKey(var15)) {
                  boolean var16 = this.findPos6(var15);
                  int var17 = var5 && this.findPos8(var15) ? 1 : 0;
                  if (var16 || var17 != 0) {
                     double var18 = var1.method_10262(var15);
                     if (var18 < this.targetScanNearestDistance) {
                        this.targetScanNearestDistance = var18;
                        this.targetScanNearest = new AutoEnd.TargetSelection(var15.method_10062(), var17 != 0);
                     }
                  }
               }
            }
         }
      }

      if (this.targetScanCursor < var7) {
         return new AutoEnd.TargetScanResult(false, null);
      } else {
         AutoEnd.TargetSelection var20 = this.targetScanNearest;
         this.findPlayer();
         return new AutoEnd.TargetScanResult(true, var20);
      }
   }

   private void findCenterChunkX(int centerChunkX, int centerChunkZ, int chunkRadius, boolean canBrush) {
      this.targetScanCenterChunkX = centerChunkX;
      this.targetScanCenterChunkZ = centerChunkZ;
      this.targetScanRadius = chunkRadius;
      this.targetScanCanBrush = canBrush;
      this.targetScanCursor = 0;
      this.targetScanNearestDistance = 22500.0;
      this.targetScanNearest = null;
   }

   private void findPlayer() {
      this.targetScanCenterChunkX = Integer.MIN_VALUE;
      this.targetScanCenterChunkZ = Integer.MIN_VALUE;
      this.targetScanRadius = -1;
      this.targetScanCursor = 0;
      this.targetScanNearestDistance = 22500.0;
      this.targetScanNearest = null;
      this.targetScanCanBrush = false;
   }

   private void findPlayer5() {
      if (mc.field_1724 != null) {
         if (this.patrolGoal != null) {
            double var1 = mc.field_1724.method_23317() - ((double)this.patrolGoal.method_10263() + 0.5);
            double var3 = mc.field_1724.method_23321() - ((double)this.patrolGoal.method_10260() + 0.5);
            if (var1 * var1 + var3 * var3 <= 2.25) {
               this.findStack4();
               this.patrolGoal = null;
            }
         }

         if (this.patrolGoal == null && this.repathDelay <= 0) {
            ThreadLocalRandom var5 = ThreadLocalRandom.current();
            class_2338 var2 = mc.field_1724.method_24515();
            this.patrolGoal = this.logOrigin(var2, var5);
            if (this.patrolGoal == null) {
               this.repathDelay = 8;
            } else {
               this.pathRequested = this.navigator.navigate(this.patrolGoal, 0, true);
               this.repathDelay = 8;
            }
         }
      }
   }

   private class_2338 logOrigin(class_2338 origin, ThreadLocalRandom random) {
      for (int var3 = 0; var3 < 24; var3++) {
         double var4 = random.nextDouble(Math.PI * 2);
         int var6 = random.nextInt(10, 25);
         int var7 = origin.method_10263() - ~((int)Math.round(Math.cos(var4) * (double)var6)) - 1;
         int var8 = origin.method_10260() - ~((int)Math.round(Math.sin(var4) * (double)var6)) - 1;

         for (int var9 = 0; var9 <= 6; var9++) {
            class_2338 var10 = new class_2338(var7, origin.method_10264() - ~var9 - 1, var8);
            if (this.navigator.isWalkableNode(var10)) {
               return var10;
            }

            if (var9 > 0) {
               class_2338 var11 = new class_2338(var7, origin.method_10264() + ~var9 + 1, var8);
               if (this.navigator.isWalkableNode(var11)) {
                  return var11;
               }
            }
         }
      }

      return null;
   }

   private void logWorld3() {
      if (this.patrolGoal != null || this.pathRequested || this.findStack()) {
         this.findStack4();
      }

      this.patrolGoal = null;
      this.pathRequested = false;
      this.repathDelay = 0;
   }

   private void logWorld11() {
      this.logForce2(false);
   }

   private void logForce2(boolean force) {
      if (this.targetBlock != null && (force || !this.pathRequested)) {
         class_2338 var2 = this.targetBrushable ? this.targetBlock.method_10084() : this.targetBlock;
         List var3 = this.targetBrushable ? List.of(var2) : this.findPot(this.targetBlock);
         boolean var4 = var3.isEmpty() ? this.navigator.navigate(var2, 0, force) : this.navigator.navigateToAny(var3, force);
         this.pathRequested = var4;
         this.repathDelay = 8;
         LOGGER.info(
            "Submitted internal {} route to {} from {} (force={}, accepted={}, pathSize={})",
            new Object[]{this.targetBrushable ? "brushable" : "pot", var2, mc.field_1724.method_24515(), force, var4, this.navigator.getPathSize()}
         );
      }
   }

   private List<class_2338> findPot(class_2338 pot) {
      if (pot != null && mc.field_1724 != null && mc.field_1687 != null) {
         class_2338 var2 = mc.field_1724.method_24515();
         int var3 = !this.findPos4(pot) ? 1 : 0;
         int[] var4 = new int[]{0, -1, 1, -2, 2, -3, 3};

         for (int var8 : var4) {
            int var9 = pot.method_10264() - ~var8 - 1;
            ArrayList var10 = new ArrayList();

            for (int var11 = -3; var11 <= 3; var11++) {
               for (int var12 = -3; var12 <= 3; var12++) {
                  if (var11 != 0 || var12 != 0) {
                     class_2338 var13 = new class_2338(pot.method_10263() - ~var11 - 1, var9, pot.method_10260() - ~var12 - 1);
                     if (var3 == 0 || !var13.equals(var2)) {
                        class_243 var14 = new class_243(
                           (double)var13.method_10263() + 0.5, (double)var13.method_10264() + 1.62, (double)var13.method_10260() + 0.5
                        );
                        if (!(var14.method_1025(pot.method_46558()) > 18.0625) && this.navigator.isWalkableNode(var13) && this.findStand(var13, pot)) {
                           var10.add(var13.method_10062());
                        }
                     }
                  }
               }
            }

            if (!var10.isEmpty()) {
               var10.sort(Comparator.comparingDouble(pos -> var2.method_10262((class_2338)pos)));
               return List.copyOf(var10);
            }
         }

         return List.of();
      } else {
         return List.of();
      }
   }

   private void findWorld12() {
      this.temporarilySkippedTargets.replaceAll((pos, ticks) -> ticks + -2 + 1);
      this.temporarilySkippedTargets.entrySet().removeIf(entry -> entry.getValue() > 0);
   }

   private boolean findStand(class_2338 stand, class_2338 pot) {
      class_243 var3 = new class_243((double)stand.method_10263() + 0.5, (double)stand.method_10264() + 1.62, (double)stand.method_10260() + 0.5);
      class_3965 var4 = mc.field_1687.method_17742(new class_3959(var3, pot.method_46558(), class_3960.field_17558, class_242.field_1348, mc.field_1724));
      return var4.method_17783() != class_240.field_1332 || !var4.method_17777().equals(pot);
   }

   private boolean findPos4(class_2338 pos) {
      if (pos != null && mc.field_1724 != null && mc.field_1687 != null) {
         class_243 var2 = mc.field_1724.method_33571();
         class_243 var3 = pos.method_46558();
         class_3965 var4 = mc.field_1687.method_17742(new class_3959(var2, var3, class_3960.field_17558, class_242.field_1348, mc.field_1724));
         return var4.method_17783() != class_240.field_1332 || !var4.method_17777().equals(pos);
      } else {
         return false;
      }
   }

   private class_3965 findPos5(class_2338 pos) {
      return pos != null
            && mc.field_1724 != null
            && mc.field_1687 != null
            && mc.field_1765 instanceof class_3965 var2
            && var2.method_17783() == class_240.field_1332
            && this.findIntended(pos, var2.method_17777())
            && !(mc.field_1724.method_33571().method_1025(var2.method_17784()) > 18.0625)
         ? var2
         : null;
   }

   private class_3965 findPos9(class_2338 pos) {
      if (pos != null && mc.field_1724 != null && mc.field_1687 != null) {
         class_243 var2 = mc.field_1724.method_33571();
         class_3965 var3 = this.findPos5(pos);
         if (var3 != null) {
            return var3;
         } else {
            ArrayList<class_243> var4 = new ArrayList<>();
            class_243 var5 = pos.method_46558();

            for (class_2350 var9 : class_2350.values()) {
               var4.add(var5.method_1031((double)var9.method_10148() * 0.49, (double)var9.method_10164() * 0.49, (double)var9.method_10165() * 0.49));
            }

            var4.sort(Comparator.comparingDouble(var2::method_1025));

            for (class_243 var11 : var4) {
               class_3965 var12 = mc.field_1687.method_17742(new class_3959(var2, var11, class_3960.field_17558, class_242.field_1348, mc.field_1724));
               if (var12.method_17783() == class_240.field_1332
                  && this.findIntended(pos, var12.method_17777())
                  && var2.method_1025(var12.method_17784()) <= 18.0625) {
                  return var12;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private boolean findIntended(class_2338 intended, class_2338 hit) {
      if (intended.equals(hit)) {
         return true;
      } else {
         return this.isPos2(intended) && this.isPos2(hit) && intended.method_10264() == hit.method_10264()
            ? Math.abs(intended.method_10263() + ~hit.method_10263() + 1) - ~Math.abs(intended.method_10260() + ~hit.method_10260() + 1) - 1 != 1
            : false;
      }
   }

   private boolean findPos7(class_2338 pos) {
      if (mc.field_1765 instanceof class_3965 var2 && var2.method_17777().equals(pos)) {
         return false;
      }

      return true;
   }

   private boolean findPos6(class_2338 pos) {
      return pos == null || mc.field_1687 == null || !mc.field_1687.method_8320(pos).method_27852(class_2246.field_42752);
   }

   private boolean findPos8(class_2338 pos) {
      return pos != null && mc.field_1687 != null
         ? mc.field_1687.method_8320(pos).method_27852(class_2246.field_42728) || mc.field_1687.method_8320(pos).method_27852(class_2246.field_43227)
         : false;
   }

   private boolean findPlayer2() {
      return this.targetBrushable ? this.findPos8(this.targetBlock) : this.findPos6(this.targetBlock);
   }

   private boolean findPos(class_2338 pos) {
      return pos != null && mc.field_1724.method_33571().method_1025(pos.method_46558()) <= 18.0625;
   }

   private boolean findPos2(class_2338 pos) {
      if (pos != null && mc.field_1724 != null) {
         class_2338 var2 = mc.field_1724.method_24515();
         if (var2.method_10263() == pos.method_10263() && var2.method_10260() == pos.method_10260()) {
            double var3 = mc.field_1724.method_23318();
            return !(var3 >= (double)pos.method_10264() + 0.7) || !(var3 <= (double)pos.method_10264() + 1.35);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean findPos3(class_2338 pos) {
      if (pos == null) {
         this.setPlayer();
         return false;
      } else if (!mc.field_1724.method_24828()) {
         this.preciseAimYawVelocity = 0.0F;
         this.preciseAimPitchVelocity = 0.0F;
         return false;
      } else {
         if (!pos.equals(this.preciseAimBlock)) {
            this.setPlayer();
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
            this.setRandom(var2);
         } else if ((this.preciseAimProfileTicks = this.preciseAimProfileTicks + -2 + 1) <= 0) {
            this.preciseAimYawSpeedTarget = var2.nextFloat(10.0F, 15.0F);
            this.preciseAimPitchSpeedTarget = var2.nextFloat(7.0F, 11.5F);
            this.setRandom(var2);
            this.preciseAimProfileTicks = var2.nextInt(18, 33);
         }

         this.preciseAimYawSpeed = this.preciseAimYawSpeed + class_3532.method_15363(this.preciseAimYawSpeedTarget - this.preciseAimYawSpeed, -0.25F, 0.25F);
         this.preciseAimPitchSpeed = this.preciseAimPitchSpeed
            + class_3532.method_15363(this.preciseAimPitchSpeedTarget - this.preciseAimPitchSpeed, -0.2F, 0.2F);
         this.preciseAimOffsetX = this.preciseAimOffsetX + class_3532.method_15350(this.preciseAimOffsetXTarget - this.preciseAimOffsetX, -0.012, 0.012);
         this.preciseAimOffsetY = this.preciseAimOffsetY + class_3532.method_15350(this.preciseAimOffsetYTarget - this.preciseAimOffsetY, -0.01, 0.01);
         this.preciseAimOffsetZ = this.preciseAimOffsetZ + class_3532.method_15350(this.preciseAimOffsetZTarget - this.preciseAimOffsetZ, -0.012, 0.012);
         class_243 var3 = pos.method_46558();
         class_243 var4 = var3.method_1031(this.preciseAimOffsetX, this.preciseAimOffsetY, this.preciseAimOffsetZ);
         class_243 var5 = mc.field_1724.method_33571();
         double var6 = var4.field_1352 - var5.field_1352;
         double var8 = var4.field_1351 - var5.field_1351;
         double var10 = var4.field_1350 - var5.field_1350;
         double var12 = Math.sqrt(var6 * var6 + var10 * var10);
         float var14 = (float)Math.toDegrees(Math.atan2(var10, var6)) - 90.0F;
         float var15 = (float)(-Math.toDegrees(Math.atan2(var8, var12)));
         float var16 = class_3532.method_15393(var14 - mc.field_1724.method_36454());
         float var17 = var15 - mc.field_1724.method_36455();
         float var18 = this.calculateError(var16, this.preciseAimYawSpeed, 34.0F);
         float var19 = this.calculateError(var17, this.preciseAimPitchSpeed, 24.0F);
         this.preciseAimYawVelocity = this.preciseAimYawVelocity + class_3532.method_15363(var18 - this.preciseAimYawVelocity, -1.75F, 1.75F);
         this.preciseAimPitchVelocity = this.preciseAimPitchVelocity + class_3532.method_15363(var19 - this.preciseAimPitchVelocity, -1.25F, 1.25F);
         float var20 = Math.abs(this.preciseAimYawVelocity) > Math.abs(var16) ? var16 : this.preciseAimYawVelocity;
         float var21 = Math.abs(this.preciseAimPitchVelocity) > Math.abs(var17) ? var17 : this.preciseAimPitchVelocity;
         mc.field_1724.method_36456(mc.field_1724.method_36454() + var20);
         mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455() + var21, -90.0F, 90.0F));
         if (var20 == var16) {
            this.preciseAimYawVelocity *= 0.35F;
         }

         if (var21 == var17) {
            this.preciseAimPitchVelocity *= 0.35F;
         }

         return !(Math.abs(var16) <= 2.25F)
            || !(Math.abs(var17) <= 1.75F)
            || !(Math.abs(this.preciseAimYawVelocity) <= 2.0F)
            || !(Math.abs(this.preciseAimPitchVelocity) <= 1.6F);
      }
   }

   private float calculateError(float error, float maxSpeed, float slowAngle) {
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

   private void setRandom(ThreadLocalRandom random) {
      this.preciseAimOffsetXTarget = random.nextDouble(-0.16, 0.16);
      this.preciseAimOffsetYTarget = random.nextDouble(-0.13, 0.15);
      this.preciseAimOffsetZTarget = random.nextDouble(-0.16, 0.16);
   }

   private void setPlayer() {
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

   private boolean isPlayer() {
      return this.findStart(0, 35) == -1;
   }

   private boolean isPlayer8() {
      if (mc.field_1724.method_6047().method_31574(class_1802.field_42716)) {
         return true;
      } else {
         int var1 = this.findStart(0, 8);
         if (var1 != -1) {
            if (this.previousSelectedSlot == -1) {
               this.previousSelectedSlot = mc.field_1724.method_31548().method_67532();
            }

            this.brushHotbarSlot = var1;
            mc.field_1724.method_31548().method_61496(var1);
            this.brushUseCooldown = 1;
            return false;
         } else {
            int var2 = this.findStart(9, 35);
            if (var2 != -1 && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
               if (this.previousSelectedSlot == -1) {
                  this.previousSelectedSlot = mc.field_1724.method_31548().method_67532();
               }

               this.brushHotbarSlot = this.previousSelectedSlot;
               this.brushInventorySlot = var2;
               mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, var2, this.brushHotbarSlot, class_1713.field_7791, mc.field_1724);
               this.brushMovedToHotbar = true;
               this.brushUseCooldown = 1;
               return false;
            } else {
               return false;
            }
         }
      }
   }

   private int findStart(int start, int end) {
      for (int var3 = start; var3 <= end; var3++) {
         if (mc.field_1724.method_31548().method_5438(var3).method_31574(class_1802.field_42716)) {
            return var3;
         }
      }

      return -1;
   }

   private void findPlayer4() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1904.method_23481(false);
      }

      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1724.method_6115()) {
         mc.field_1761.method_2897(mc.field_1724);
      }

      if (mc.field_1724 != null
         && mc.field_1761 != null
         && this.brushMovedToHotbar
         && this.brushInventorySlot >= 9
         && this.brushHotbarSlot >= 0
         && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
         mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.brushInventorySlot, this.brushHotbarSlot, class_1713.field_7791, mc.field_1724);
      }

      if (mc.field_1724 != null && this.previousSelectedSlot >= 0) {
         this.logSlot(8);
      }

      this.brushingStarted = false;
      this.brushUseCooldown = 0;
      this.previousSelectedSlot = -1;
      this.brushInventorySlot = -1;
      this.brushHotbarSlot = -1;
      this.brushMovedToHotbar = false;
   }

   private boolean findStack2() {
      return this.navigator.isWorking();
   }

   private boolean findStack() {
      return this.navigator.isWorking();
   }

   private void findStack4() {
      this.navigator.stop();
      this.pathRequested = false;
      this.repathDelay = 0;
   }

   private void findStopPath(boolean stopPath) {
      if (this.brushingStarted || this.previousSelectedSlot != -1 || this.brushMovedToHotbar) {
         this.findPlayer4();
      }

      if (stopPath) {
         this.findStack4();
      }

      this.targetBlock = null;
      this.targetBrushable = false;
      this.breakingStarted = false;
      this.potBreakAttemptTicks = 0;
      this.pathRequested = false;
      this.repathDelay = 0;
      this.aimTicks = 0;
      this.findPlayer();
      this.setPlayer();
   }

   private static enum CycleState {
      FARMING,
      PVP_WAIT,
      DARENA_WAIT,
      ANARCHY_WAIT,
      FARM_RETURN_WAIT,
      HOME_WAIT,
      FIND_CHEST,
      WALK_CHEST,
      OPEN_CHEST,
      DEPOSIT,
      FIND_SUPPLY,
      WALK_SUPPLY,
      OPEN_SUPPLY,
      TAKE_SUPPLY,
      FIND_PORTAL,
      WALK_PORTAL,
      ENTER_PORTAL;
   }

   private static record LootRecord(class_1542 entity, class_1799 stack, String state, boolean updated) {
   }

   private static record PvpCooldown(boolean active, int seconds) {
      private static final AutoEnd.PvpCooldown INACTIVE = new AutoEnd.PvpCooldown(false, 0);
   }

   private static enum SupplyKind {
      FOOD,
      INVISIBILITY,
      SPEED;
   }

   private static record TargetScanResult(boolean complete, AutoEnd.TargetSelection selection) {
   }

   private static record TargetSelection(class_2338 pos, boolean brushable) {
   }
}
