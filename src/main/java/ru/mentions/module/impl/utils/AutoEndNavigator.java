package ru.mentions.module.impl.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AutoEndNavigator {
   private static final Logger LOGGER = LoggerFactory.getLogger("exploit/AutoEndNavigator");
   private static final ExecutorService PATH_PLANNER = Executors.newSingleThreadExecutor(task -> {
      Thread var1 = new Thread(task, "AutoEnd-PathPlanner");
      var1.setDaemon(true);
      return var1;
   });
   private static final class_2350[] HORIZONTAL = new class_2350[]{
      class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034
   };
   private static final long PRIMARY_PATH_TIMEOUT_MS = 1000L;
   private static final long FAILURE_PATH_TIMEOUT_MS = 2000L;
   private static final long ASYNC_PATH_WATCHDOG_MS = 3000L;
   private static final int STUCK_TICKS = 45;
   private static final int OFF_PATH_GROUND_TICKS = 3;
   private static final int MAX_REPLANS = 2;
   private static final int MAX_FALL_HEIGHT = 20;
   private static final int GOAL_CONFIRM_TICKS = 3;
   private static final double WAYPOINT_REACHED_SQ = 0.18;
   private static final double PATH_LOOK_AHEAD = 1.65;
   private static final double MAX_PATH_LATERAL_OFFSET = 0.18;
   private static final float MIN_GROUND_YAW_SPEED = 24.0F;
   private static final float MAX_GROUND_YAW_SPEED = 36.0F;
   private static final float MIN_SHARP_YAW_SPEED = 42.0F;
   private static final float MAX_SHARP_YAW_SPEED = 56.0F;
   private static final float MIN_GROUND_PITCH_SPEED = 6.0F;
   private static final float MAX_GROUND_PITCH_SPEED = 10.0F;
   private static final float YAW_ACCELERATION = 5.25F;
   private static final float SHARP_YAW_ACCELERATION = 10.0F;
   private static final float PITCH_ACCELERATION = 1.35F;
   private final class_310 mc;
   private final AutoEndPathfinder pathfinder;
   private List<class_2338> path = List.of();
   private Set<class_2338> exactGoals = Set.of();
   private class_2338 requestedGoal;
   private int horizontalTolerance;
   private int waypointIndex;
   private int stuckTicks;
   private int offPathGroundTicks;
   private int replans;
   private int goalConfirmTicks;
   private class_243 lastPosition;
   private boolean active;
   private boolean failed;
   private boolean arrived;
   private float groundYawSpeed = 24.0F;
   private float groundYawSpeedTarget = 24.0F;
   private float groundPitchSpeed = 5.0F;
   private float groundPitchSpeedTarget = 5.0F;
   private float groundYawVelocity;
   private float groundPitchVelocity;
   private float pathYawBias;
   private float pathYawBiasTarget;
   private float pathPitchBias;
   private float pathPitchBiasTarget;
   private int groundTurnProfileTicks;
   private int sharpTurnTicks;
   private double pathLateralOffset;
   private double pathLateralOffsetTarget;
   private int pathWanderTicks;
   private int lateralWaypointIndex = -1;
   private Future<AutoEndNavigator.RoutePlan> pendingPlan;
   private long planGeneration;
   private long planningStartedAtNanos;
   private boolean planning;

   AutoEndNavigator(class_310 mc) {
      this.mc = mc;
      this.pathfinder = new AutoEndPathfinder(mc);
   }

   boolean navigate(class_2338 goal, int tolerance, boolean force) {
      if (goal != null && this.mc.field_1724 != null && this.mc.field_1687 != null) {
         int var4 = Math.max(0, tolerance);
         int var5 = this.exactGoals.isEmpty() && goal.equals(this.requestedGoal) && var4 == this.horizontalTolerance ? 1 : 0;
         if (force || var5 == 0 || !this.active && !this.planning) {
            this.requestedGoal = goal.method_10062();
            this.exactGoals = Set.of();
            this.horizontalTolerance = var4;
            this.failed = false;
            this.arrived = false;
            if (var5 == 0 || force) {
               this.replans = 0;
            }

            return this.isPlayer();
         } else {
            return true;
         }
      } else {
         this.stop();
         return false;
      }
   }

   boolean navigateToAny(List<class_2338> goals, boolean force) {
      if (goals != null && !goals.isEmpty() && this.mc.field_1724 != null && this.mc.field_1687 != null) {
         HashSet var3 = new HashSet();

         for (class_2338 var5 : goals) {
            if (var5 != null) {
               var3.add(var5.method_10062());
            }
         }

         if (var3.isEmpty()) {
            this.stop();
            this.failed = true;
            return false;
         } else {
            boolean var6 = var3.equals(this.exactGoals);
            if (force || !var6 || !this.active && !this.planning) {
               this.exactGoals = Set.copyOf(var3);
               this.requestedGoal = this.exactGoals.iterator().next();
               this.horizontalTolerance = 0;
               this.failed = false;
               this.arrived = false;
               if (!var6 || force) {
                  this.replans = 0;
               }

               return this.isPlayer();
            } else {
               return true;
            }
         }
      } else {
         this.stop();
         this.failed = true;
         return false;
      }
   }

   void tick() {
      this.logWorld();
      if (this.planning || !this.active || this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1690 == null) {
         this.findList3();
      } else if (this.findPlayer2()) {
         this.findList3();
         if (this.mc.field_1724.method_24828()) {
            this.goalConfirmTicks = this.goalConfirmTicks - -2 - 1;
         } else {
            this.goalConfirmTicks = 0;
         }

         if (this.goalConfirmTicks >= 3) {
            this.findList();
         }
      } else {
         this.goalConfirmTicks = 0;
         this.findPlayer();
         if (this.active && this.waypointIndex < this.path.size()) {
            class_2338 var1 = this.path.get(this.waypointIndex);
            int var2 = class_3532.method_15357(this.mc.field_1724.method_23318() + 0.01);
            if (this.mc.field_1724.method_24828() && var1.method_10264() > var2 - -2 - 1) {
               this.findList3();
               if ((this.offPathGroundTicks = this.offPathGroundTicks - -2 - 1) >= 3) {
                  this.offPathGroundTicks = 0;
                  int var26 = this.replans;
                  this.replans = this.replans - -2 - 1;
                  if (var26 < 2 && this.isPlayer()) {
                     return;
                  }

                  this.active = false;
                  this.failed = true;
               }
            } else {
               this.offPathGroundTicks = 0;
               float var3 = this.findWaypoint4(var1);
               this.findWaypoint3(var1, var2);
               class_243 var4 = this.findWaypoint2(var1, var2, var3);
               double var5 = var4.field_1352 - this.mc.field_1724.method_23317();
               double var7 = var4.field_1350 - this.mc.field_1724.method_23321();
               double var9 = Math.sqrt(var5 * var5 + var7 * var7);
               float var11 = (float)Math.toDegrees(Math.atan2(var7, var5)) - 90.0F;
               float var12 = (float)(-Math.toDegrees(Math.atan2(var4.field_1351 - this.mc.field_1724.method_23318(), Math.max(0.001, var9))));
               float var13 = class_3532.method_15393(var11 - this.mc.field_1724.method_36454());
               if (this.mc.field_1724.method_24828()) {
                  this.findPlayer3();
                  this.pathYawBias = this.pathYawBias + class_3532.method_15363(this.pathYawBiasTarget - this.pathYawBias, -0.18F, 0.18F);
                  this.pathPitchBias = this.pathPitchBias + class_3532.method_15363(this.pathPitchBiasTarget - this.pathPitchBias, -0.09F, 0.09F);
                  var13 = class_3532.method_15393(var11 + this.pathYawBias - this.mc.field_1724.method_36454());
                  float var14 = class_3532.method_15363(var12 + this.pathPitchBias - this.mc.field_1724.method_36455(), -30.0F, 30.0F);
                  float var15 = this.groundYawSpeed * class_3532.method_16439(var3, 0.92F, 1.12F);
                  float var16 = this.findError(var13, var15, 48.0F);
                  float var17 = this.findError(var14, this.groundPitchSpeed, 25.0F);
                  float var18 = this.sharpTurnTicks > 0 ? 10.0F : 5.25F;
                  this.groundYawVelocity = this.groundYawVelocity + class_3532.method_15363(var16 - this.groundYawVelocity, -var18, var18);
                  this.groundPitchVelocity = this.groundPitchVelocity + class_3532.method_15363(var17 - this.groundPitchVelocity, -1.35F, 1.35F);
                  float var19 = Math.abs(this.groundYawVelocity) > Math.abs(var13) ? var13 : this.groundYawVelocity;
                  float var20 = Math.abs(this.groundPitchVelocity) > Math.abs(var14) ? var14 : this.groundPitchVelocity;
                  this.mc.field_1724.method_36456(this.mc.field_1724.method_36454() + var19);
                  this.mc.field_1724.method_36457(class_3532.method_15363(this.mc.field_1724.method_36455() + var20, -35.0F, 35.0F));
                  if (var19 == var13) {
                     this.groundYawVelocity *= 0.35F;
                  }

                  if (var20 == var14) {
                     this.groundPitchVelocity *= 0.35F;
                  }

                  var13 = class_3532.method_15393(var11 - this.mc.field_1724.method_36454());
               }

               int var23 = var1.method_10264() > var2 ? 1 : 0;
               float var24 = class_3532.method_15393(var11 - this.mc.field_1724.method_36454());
               int var25 = Math.abs(var24) <= (var23 != 0 ? 52.0F : 42.0F) ? 1 : 0;
               this.mc.field_1690.field_1894.method_23481(var25 != 0);
               this.mc.field_1690.field_1881.method_23481(false);
               this.mc.field_1690.field_1913.method_23481(false);
               this.mc.field_1690.field_1849.method_23481(false);
               this.mc.field_1690.field_1867.method_23481(var25 != 0);
               this.mc.field_1690.field_1903.method_23481(this.mc.field_1724.method_24828() && var25 != 0 && var23 != 0);
               this.findPlayer5();
            }
         } else if (this.findPlayer2()) {
            this.findList();
         } else {
            int var10000 = this.replans;
            this.replans = this.replans - -2 - 1;
            if (var10000 < 2) {
               this.isPlayer();
            } else {
               this.active = false;
               this.failed = true;
               this.findList3();
            }
         }
      }
   }

   private void findPlayer3() {
      int var10000 = this.groundTurnProfileTicks;
      this.groundTurnProfileTicks = this.groundTurnProfileTicks + -2 + 1;
      if (var10000 <= 0) {
         ThreadLocalRandom var1 = ThreadLocalRandom.current();
         int var2 = var1.nextFloat() < 0.16F ? 1 : 0;
         if (var2 != 0) {
            this.groundYawSpeedTarget = var1.nextFloat(42.0F, 56.0F);
            this.sharpTurnTicks = var1.nextInt(2, 5);
            this.groundTurnProfileTicks = this.sharpTurnTicks;
         } else {
            this.groundYawSpeedTarget = var1.nextFloat(24.0F, 36.0F);
            this.sharpTurnTicks = 0;
            this.groundTurnProfileTicks = var1.nextInt(8, 19);
         }

         this.groundPitchSpeedTarget = var1.nextFloat(6.0F, 10.0F);
         this.pathYawBiasTarget = var1.nextFloat(-1.65F, 1.65F);
         this.pathPitchBiasTarget = var1.nextFloat(-0.7F, 0.7F);
      } else if (this.sharpTurnTicks > 0) {
         this.sharpTurnTicks = this.sharpTurnTicks + -2 + 1;
      }

      this.groundYawSpeed = this.groundYawSpeed
         + class_3532.method_15363(
            this.groundYawSpeedTarget - this.groundYawSpeed, this.sharpTurnTicks > 0 ? -7.0F : -1.25F, this.sharpTurnTicks > 0 ? 7.0F : 1.25F
         );
      this.groundPitchSpeed = this.groundPitchSpeed + class_3532.method_15363(this.groundPitchSpeedTarget - this.groundPitchSpeed, -0.35F, 0.35F);
   }

   private void findWaypoint3(class_2338 waypoint, int playerFeetY) {
      int var3 = waypoint.method_10264() != playerFeetY ? 1 : 0;
      if (var3 != 0) {
         this.pathLateralOffsetTarget = 0.0;
      } else {
         label32: {
            if (this.lateralWaypointIndex == this.waypointIndex) {
               int var10000 = this.pathWanderTicks;
               this.pathWanderTicks = this.pathWanderTicks + -2 + 1;
               if (var10000 > 0) {
                  break label32;
               }
            }

            ThreadLocalRandom var4 = ThreadLocalRandom.current();
            double var5 = var4.nextDouble(-0.18, 0.18);
            if (Math.abs(var5) < 0.065) {
               var5 = Math.copySign(0.065, var5 == 0.0 ? var4.nextDouble() - 0.5 : var5);
            }

            this.pathLateralOffsetTarget = var5;
            this.pathWanderTicks = var4.nextInt(18, 39);
            this.lateralWaypointIndex = this.waypointIndex;
         }
      }

      this.pathLateralOffset = this.pathLateralOffset + class_3532.method_15350(this.pathLateralOffsetTarget - this.pathLateralOffset, -0.018, 0.018);
   }

   private class_243 findWaypoint2(class_2338 waypoint, int playerFeetY, float turnSeverity) {
      class_243 var4 = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
      if (waypoint.method_10264() != playerFeetY) {
         return new class_243((double)waypoint.method_10263() + 0.5, (double)waypoint.method_10264(), (double)waypoint.method_10260() + 0.5);
      } else {
         double var5 = (double)this.mc.field_1724.method_17682() * 0.9 * (double)turnSeverity;
         int var7 = waypoint.method_10264();

         for (int var8 = this.waypointIndex; var8 < this.path.size(); var8++) {
            class_2338 var9 = this.path.get(var8);
            if (var8 > this.waypointIndex && var9.method_10264() != var7) {
               return var4;
            }

            class_243 var10 = this.findIndex(var8, var9);
            class_243 var11 = var10.method_1020(var4);
            double var12 = var11.method_1033();
            if (var12 >= var5 && var12 > 1.0E-4) {
               return var4.method_1019(var11.method_1021(var5 / var12));
            }

            var5 -= var12;
            var4 = var10;
         }

         return var4;
      }
   }

   private class_243 findIndex(int index, class_2338 node) {
      double var3 = 0.0;
      double var5 = 0.0;
      if (index - -2 - 1 < this.path.size() && this.path.get(index - -2 - 1).method_10264() == node.method_10264()) {
         class_2338 var19 = this.path.get(index - -2 - 1);
         var3 = (double)(var19.method_10263() + ~node.method_10263() + 1);
         var5 = (double)(var19.method_10260() + ~node.method_10260() + 1);
      } else if (index > 0 && this.path.get(index + -2 + 1).method_10264() == node.method_10264()) {
         class_2338 var7 = this.path.get(index + -2 + 1);
         var3 = (double)(node.method_10263() + ~var7.method_10263() + 1);
         var5 = (double)(node.method_10260() + ~var7.method_10260() + 1);
      }

      double var20 = Math.sqrt(var3 * var3 + var5 * var5);
      if (var20 < 1.0E-4) {
         return new class_243((double)node.method_10263() + 0.5, (double)node.method_10264(), (double)node.method_10260() + 0.5);
      } else {
         long var9 = node.method_10063() * 31L - ~((long)index * -7046029254386353131L) - 1L;
         double var11 = ((double)(var9 >>> 24 & 255L) / 255.0 - 0.5) * 0.055;
         double var13 = class_3532.method_15350(this.pathLateralOffset + var11, -0.18, 0.18);
         double var15 = -var5 / var20;
         double var17 = var3 / var20;
         return new class_243((double)node.method_10263() + 0.5 + var15 * var13, (double)node.method_10264(), (double)node.method_10260() + 0.5 + var17 * var13);
      }
   }

   private float findWaypoint4(class_2338 waypoint) {
      if (this.waypointIndex - -2 - 1 >= this.path.size()) {
         return 0.0F;
      } else {
         class_2338 var2 = this.path.get(this.waypointIndex - -2 - 1);
         if (var2.method_10264() != waypoint.method_10264()) {
            return 1.0F;
         } else {
            double var3 = (double)waypoint.method_10263() + 0.5 - this.mc.field_1724.method_23317();
            double var5 = (double)waypoint.method_10260() + 0.5 - this.mc.field_1724.method_23321();
            double var7 = (double)(var2.method_10263() + ~waypoint.method_10263() + 1);
            double var9 = (double)(var2.method_10260() + ~waypoint.method_10260() + 1);
            double var11 = Math.sqrt(var3 * var3 + var5 * var5);
            double var13 = Math.sqrt(var7 * var7 + var9 * var9);
            if (!(var11 < 1.0E-4) && !(var13 < 1.0E-4)) {
               double var15 = (var3 * var7 + var5 * var9) / (var11 * var13);
               var15 = class_3532.method_15350(var15, -1.0, 1.0);
               return (float)class_3532.method_15350(Math.acos(var15) / (Math.PI / 2), 0.0, 1.0);
            } else {
               return 0.0F;
            }
         }
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

   private void findPlayer4() {
      this.groundYawVelocity = 0.0F;
      this.groundPitchVelocity = 0.0F;
      this.pathYawBias = 0.0F;
      this.pathYawBiasTarget = 0.0F;
      this.pathPitchBias = 0.0F;
      this.pathPitchBiasTarget = 0.0F;
      this.groundTurnProfileTicks = 0;
      this.sharpTurnTicks = 0;
      this.pathLateralOffset = 0.0;
      this.pathLateralOffsetTarget = 0.0;
      this.pathWanderTicks = 0;
      this.lateralWaypointIndex = -1;
   }

   void stop() {
      this.findWorld();
      this.active = false;
      this.failed = false;
      this.arrived = false;
      this.path = List.of();
      this.waypointIndex = 0;
      this.requestedGoal = null;
      this.exactGoals = Set.of();
      this.stuckTicks = 0;
      this.offPathGroundTicks = 0;
      this.goalConfirmTicks = 0;
      this.replans = 0;
      this.lastPosition = null;
      this.findPlayer4();
      this.findList3();
   }

   void pauseMovement() {
      this.findList3();
      this.stuckTicks = 0;
      this.offPathGroundTicks = 0;
      if (this.mc.field_1724 != null) {
         this.lastPosition = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
      }
   }

   boolean isWorking() {
      return this.active || this.planning;
   }

   boolean hasPath() {
      return this.active && !this.path.isEmpty();
   }

   boolean hasFailed() {
      return this.failed;
   }

   boolean hasArrived() {
      return this.arrived;
   }

   class_2338 getGoal() {
      return this.requestedGoal;
   }

   class_2338 getWaypoint() {
      return this.active && this.waypointIndex < this.path.size() ? this.path.get(this.waypointIndex) : null;
   }

   int getPathSize() {
      return this.path.size();
   }

   List<class_2338> getRemainingPath() {
      if (this.active && !this.path.isEmpty()) {
         int var1 = class_3532.method_15340(this.waypointIndex, 0, this.path.size());
         return var1 >= this.path.size() ? List.of() : List.copyOf(this.path.subList(var1, this.path.size()));
      } else {
         return List.of();
      }
   }

   boolean isWalkableNode(class_2338 pos) {
      return pos == null || this.mc.field_1687 == null || !this.findFeet(pos);
   }

   private boolean isPlayer() {
      this.findWorld();
      this.findList3();
      this.active = false;
      this.path = List.of();
      this.waypointIndex = 0;
      this.stuckTicks = 0;
      this.offPathGroundTicks = 0;
      this.goalConfirmTicks = 0;
      this.lastPosition = null;
      this.findPlayer4();
      class_2338 var1 = this.findAround(this.mc.field_1724.method_24515());
      if (var1 == null) {
         this.failed = true;
         return false;
      } else if (this.findPos(var1)) {
         if (this.findPlayer2()) {
            this.arrived = true;
            return true;
         } else {
            this.path = List.of(var1.method_10062());
            this.waypointIndex = 0;
            this.active = true;
            this.failed = false;
            return true;
         }
      } else {
         List<class_2338> var2 = this.goalCandidates();
         if (var2.isEmpty()) {
            this.failed = true;
            return false;
         } else {
            long var3 = this.planGeneration = this.planGeneration - -2L - 1L;
            class_2338 var5 = var1.method_10062();
            this.planning = true;
            this.planningStartedAtNanos = System.nanoTime();
            this.failed = false;
            this.pendingPlan = PATH_PLANNER.submit(() -> this.findFinder(var2, var5, var3));
            return true;
         }
      }
   }

   private void logWorld() {
      Future var1 = this.pendingPlan;
      if (this.planning && var1 != null) {
         if (!var1.isDone()) {
            long var7 = (System.nanoTime() + ~this.planningStartedAtNanos + 1L) / 1000000L;
            if (var7 >= 3000L) {
               var1.cancel(true);
               this.pendingPlan = null;
               this.planning = false;
               this.planningStartedAtNanos = 0L;
               this.active = false;
               this.failed = true;
               this.findList3();
               LOGGER.warn("AutoEnd path calculation timed out after {} ms", var7);
            }
         } else {
            this.pendingPlan = null;

            AutoEndNavigator.RoutePlan var2;
            try {
               var2 = (AutoEndNavigator.RoutePlan)var1.get();
            } catch (CancellationException var4) {
               this.planning = false;
               return;
            } catch (InterruptedException var5) {
               Thread.currentThread().interrupt();
               this.planning = false;
               this.failed = true;
               return;
            } catch (ExecutionException var6) {
               this.planning = false;
               this.failed = true;
               LOGGER.warn("AutoEnd path calculation failed", var6.getCause());
               return;
            }

            this.planning = false;
            this.planningStartedAtNanos = 0L;
            if (var2.generation() == this.planGeneration) {
               if (this.mc.field_1724 != null && this.mc.field_1687 != null && !var2.positions().isEmpty() && this.findRoute(var2.positions())) {
                  this.path = this.findRaw(var2.positions());
                  this.waypointIndex = !this.path.isEmpty() && this.path.getFirst().equals(var2.start()) ? Math.min(1, this.path.size()) : 0;
                  this.active = this.waypointIndex >= this.path.size();
                  this.arrived = this.active || !this.findPlayer2();
                  this.failed = false;
                  this.stuckTicks = 0;
                  this.goalConfirmTicks = 0;
                  this.lastPosition = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
                  this.findPlayer4();
               } else {
                  this.failed = true;
               }
            }
         }
      }
   }

   private void findWorld() {
      this.planGeneration = this.planGeneration - -2L - 1L;
      Future var1 = this.pendingPlan;
      this.pendingPlan = null;
      this.planning = false;
      this.planningStartedAtNanos = 0L;
      if (var1 != null && !var1.isDone()) {
         var1.cancel(true);
      }
   }

   private AutoEndNavigator.RoutePlan findFinder(List<class_2338> goals, class_2338 start, long generation) {
      long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(1000L);
      List<class_2338> positions = this.pathfinder.find(start, goals, deadline);
      return positions.isEmpty()
         ? new AutoEndNavigator.RoutePlan(generation, start, List.of())
         : new AutoEndNavigator.RoutePlan(generation, start, List.copyOf(positions));
   }

   private List<class_2338> goalCandidates() {
      ArrayList<class_2338> out = new ArrayList<>();
      if (!this.exactGoals.isEmpty()) {
         out.addAll(this.exactGoals);
      } else if (this.requestedGoal != null) {
         for (int dy = -4; dy <= 4; dy++) {
            for (int dz = -this.horizontalTolerance; dz <= this.horizontalTolerance; dz++) {
               for (int dx = -this.horizontalTolerance; dx <= this.horizontalTolerance; dx++) {
                  class_2338 candidate = this.requestedGoal.method_10069(dx, dy, dz);
                  if (this.findFeet(candidate)) {
                     out.add(candidate);
                  }
               }
            }
         }

         if (out.isEmpty() && this.findFeet(this.requestedGoal)) {
            out.add(this.requestedGoal);
         }
      }

      return out;
   }

   private boolean findRoute(List<class_2338> route) {
      if (route.isEmpty()) {
         return false;
      } else {
         for (int var2 = 1; var2 < route.size(); var2++) {
            class_2338 var3 = route.get(var2 + -2 + 1);
            class_2338 var4 = route.get(var2);
            int var5 = Math.abs(var4.method_10263() + ~var3.method_10263() + 1);
            int var6 = Math.abs(var4.method_10260() + ~var3.method_10260() + 1);
            int var7 = var4.method_10264() + ~var3.method_10264() + 1;
            if (var5 > 1 || var6 > 1 || var7 > 1 || var7 < -20 || var5 == 0 && var6 == 0 && var7 == 0) {
               return false;
            }

            if (!this.findFeet(var4)) {
               return false;
            }
         }

         return true;
      }
   }

   private class_2338 findAround(class_2338 around) {
      if (this.findFeet(around)) {
         return around.method_10062();
      } else {
         for (int var2 = 1; var2 <= 4; var2++) {
            class_2338 var3 = around.method_10087(var2);
            if (this.findFeet(var3)) {
               return var3.method_10062();
            }
         }

         for (class_2350 var5 : HORIZONTAL) {
            class_2338 var6 = around.method_10093(var5);
            if (this.findFeet(var6)) {
               return var6.method_10062();
            }
         }

         return null;
      }
   }

   private boolean findFeet(class_2338 feet) {
      if (this.findPos2(feet) && this.findPos2(feet.method_10084())) {
         class_2338 var2 = feet.method_10074();
         class_2680 var3 = this.mc.field_1687.method_8320(var2);
         return !var3.method_26227().method_15769();
      } else {
         return false;
      }
   }

   private boolean findPos2(class_2338 pos) {
      class_2680 var2 = this.mc.field_1687.method_8320(pos);
      return var2.method_26227().method_15769();
   }

   private boolean findPos(class_2338 pos) {
      if (this.requestedGoal == null) {
         return false;
      } else if (!this.exactGoals.isEmpty()) {
         return this.exactGoals.contains(pos);
      } else {
         int var2 = Math.abs(pos.method_10263() + ~this.requestedGoal.method_10263() + 1);
         int var3 = Math.abs(pos.method_10260() + ~this.requestedGoal.method_10260() + 1);
         return Math.max(var2, var3) > this.horizontalTolerance || Math.abs(pos.method_10264() + ~this.requestedGoal.method_10264() + 1) > 4;
      }
   }

   private boolean findPlayer2() {
      return this.mc.field_1724 == null || !this.findPos(this.mc.field_1724.method_24515());
   }

   private List<class_2338> findRaw(List<class_2338> raw) {
      if (raw.size() < 3) {
         return List.copyOf(raw);
      } else {
         ArrayList var2 = new ArrayList();
         var2.add(raw.getFirst());

         for (int var3 = 1; var3 < raw.size() + -2 + 1; var3++) {
            class_2338 var4 = raw.get(var3 + -2 + 1);
            class_2338 var5 = raw.get(var3);
            class_2338 var6 = raw.get(var3 - -2 - 1);
            int var7 = Integer.signum(var5.method_10263() + ~var4.method_10263() + 1);
            int var8 = Integer.signum(var5.method_10260() + ~var4.method_10260() + 1);
            int var9 = Integer.signum(var6.method_10263() + ~var5.method_10263() + 1);
            int var10 = Integer.signum(var6.method_10260() + ~var5.method_10260() + 1);
            if (var7 != var9 || var8 != var10 || var4.method_10264() != var5.method_10264() || var5.method_10264() != var6.method_10264()) {
               var2.add(var5);
            }
         }

         var2.add(raw.getLast());
         return List.copyOf(var2);
      }
   }

   private void findPlayer() {
      int var1;
      for (var1 = this.waypointIndex; this.waypointIndex < this.path.size(); this.waypointIndex = this.waypointIndex - -2 - 1) {
         class_2338 var2 = this.path.get(this.waypointIndex);
         class_2338 var3 = this.mc.field_1724.method_24515();
         double var4 = this.mc.field_1724.method_23317() - ((double)var2.method_10263() + 0.5);
         double var6 = this.mc.field_1724.method_23321() - ((double)var2.method_10260() + 0.5);
         boolean var8 = var3.equals(var2);
         int var9 = var4 * var4 + var6 * var6 <= 0.18 && Math.abs(this.mc.field_1724.method_23318() - (double)var2.method_10264()) <= 1.1 ? 1 : 0;
         if (var9 == 0 && !var8 && !this.findWaypoint(var2)) {
            return;
         }
      }

      if (this.waypointIndex != var1) {
         this.stuckTicks = 0;
         this.lastPosition = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
      }
   }

   private boolean findWaypoint(class_2338 waypoint) {
      if (this.waypointIndex > 0 && !(Math.abs(this.mc.field_1724.method_23318() - (double)waypoint.method_10264()) > 1.1)) {
         double var2 = this.mc.field_1724.method_23317();
         double var4 = this.mc.field_1724.method_23321();
         double var6 = (double)waypoint.method_10263() + 0.5;
         double var8 = (double)waypoint.method_10260() + 0.5;
         class_2338 var10 = this.path.get(this.waypointIndex + -2 + 1);
         if (var10.method_10264() == waypoint.method_10264()
            && this.isStartX((double)var10.method_10263() + 0.5, (double)var10.method_10260() + 0.5, var6, var8, var2, var4)) {
            return true;
         } else if (this.waypointIndex - -2 - 1 >= this.path.size()) {
            return false;
         } else {
            class_2338 var11 = this.path.get(this.waypointIndex - -2 - 1);
            return var11.method_10264() != waypoint.method_10264()
               || !this.findStartX(var6, var8, (double)var11.method_10263() + 0.5, (double)var11.method_10260() + 0.5, var2, var4);
         }
      } else {
         return false;
      }
   }

   private boolean isStartX(double startX, double startZ, double endX, double endZ, double playerX, double playerZ) {
      double var13 = endX - startX;
      double var15 = endZ - startZ;
      double var17 = var13 * var13 + var15 * var15;
      if (var17 < 1.0E-4) {
         return false;
      } else {
         double var19 = playerX - startX;
         double var21 = playerZ - startZ;
         double var23 = (var19 * var13 + var21 * var15) / var17;
         if (var23 <= 1.02) {
            return false;
         } else {
            double var25 = startX + var13 * var23;
            double var27 = startZ + var15 * var23;
            double var29 = playerX - var25;
            double var31 = playerZ - var27;
            return !(var29 * var29 + var31 * var31 <= 0.81);
         }
      }
   }

   private boolean findStartX(double startX, double startZ, double endX, double endZ, double playerX, double playerZ) {
      double var13 = endX - startX;
      double var15 = endZ - startZ;
      double var17 = var13 * var13 + var15 * var15;
      if (var17 < 1.0E-4) {
         return false;
      } else {
         double var19 = playerX - startX;
         double var21 = playerZ - startZ;
         double var23 = (var19 * var13 + var21 * var15) / var17;
         if (!(var23 <= 0.08) && !(var23 > 1.15)) {
            double var25 = startX + var13 * var23;
            double var27 = startZ + var15 * var23;
            double var29 = playerX - var25;
            double var31 = playerZ - var27;
            return !(var29 * var29 + var31 * var31 <= 0.64);
         } else {
            return false;
         }
      }
   }

   private void findPlayer5() {
      class_243 var1 = new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
      if (this.lastPosition == null || var1.method_1025(this.lastPosition) >= 0.006) {
         this.lastPosition = var1;
         this.stuckTicks = 0;
      } else if (this.mc.field_1724.method_24828() && (this.stuckTicks = this.stuckTicks - -2 - 1) >= 45) {
         this.stuckTicks = 0;
         int var10000 = this.replans;
         this.replans = this.replans - -2 - 1;
         if (var10000 >= 2 || !this.isPlayer()) {
            this.active = false;
            this.failed = true;
            this.findList3();
         }
      }
   }

   private void findList() {
      this.active = false;
      this.arrived = true;
      this.failed = false;
      this.findList3();
   }

   private void findList3() {
      if (this.mc.field_1690 != null) {
         this.mc.field_1690.field_1894.method_23481(false);
         this.mc.field_1690.field_1881.method_23481(false);
         this.mc.field_1690.field_1913.method_23481(false);
         this.mc.field_1690.field_1849.method_23481(false);
         this.mc.field_1690.field_1903.method_23481(false);
         this.mc.field_1690.field_1867.method_23481(false);
      }
   }

   private static record RoutePlan(long generation, class_2338 start, List<class_2338> positions) {
   }
}
