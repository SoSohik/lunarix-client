package ru.mentions.module.impl.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;

final class AutoEndPathfinder {
   private static final int[][] SIDES = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
   private static final int[][][] DIAGONALS = new int[][][]{
      {{1, 1}, {1, 0}, {0, 1}}, {{1, -1}, {1, 0}, {0, -1}}, {{-1, 1}, {-1, 0}, {0, 1}}, {{-1, -1}, {-1, 0}, {0, -1}}
   };
   private static final double SIDE_COST = 1.0;
   private static final double DIAGONAL_COST = 1.41421356237;
   private static final int MAX_FALL = 20;
   private static final int MAX_EXPANSIONS = 20000;
   private final class_310 mc;

   AutoEndPathfinder(class_310 mc) {
      this.mc = mc;
   }

   List<class_2338> find(class_2338 start, List<class_2338> goals, long deadlineNanos) {
      if (start != null && goals != null && !goals.isEmpty()) {
         class_1937 world = this.mc.field_1687;
         if (world == null) {
            return Collections.emptyList();
         } else {
            List<class_2338> targets = new ArrayList<>(goals.size());

            for (class_2338 goal : goals) {
               if (goal != null) {
                  targets.add(goal);
               }
            }

            if (targets.isEmpty()) {
               return Collections.emptyList();
            } else {
               Map<Long, AutoEndPathfinder.Node> best = new HashMap<>();
               Set<Long> closed = new HashSet<>();
               PriorityQueue<AutoEndPathfinder.Node> open = new PriorityQueue<>(64, (a, b) -> {
                  int c = Double.compare(a.f, b.f);
                  return c != 0 ? c : Long.compare(a.order, b.order);
               });
               AutoEndPathfinder.Node startNode = new AutoEndPathfinder.Node(
                  start.method_10263(), start.method_10264(), start.method_10260(), 0.0, heuristic(start, targets), null
               );
               best.put(key(startNode), startNode);
               open.add(startNode);
               int expansions = 0;
               AutoEndPathfinder.Node goalNode = null;

               while (!open.isEmpty() && expansions < 20000) {
                  if ((expansions & 63) == 0 && System.nanoTime() > deadlineNanos) {
                     return Collections.emptyList();
                  }

                  AutoEndPathfinder.Node current = open.poll();
                  if (current == null) {
                     break;
                  }

                  long currentKey = key(current);
                  if (closed.add(currentKey)) {
                     expansions++;
                     if (isTarget(current, targets)) {
                        goalNode = current;
                        break;
                     }

                     for (AutoEndPathfinder.Node neighbour : this.expand(world, current)) {
                        long nKey = key(neighbour);
                        if (!closed.contains(nKey)) {
                           AutoEndPathfinder.Node existing = best.get(nKey);
                           if (existing == null || !(existing.g <= neighbour.g)) {
                              neighbour.order = current.order + 1L;
                              neighbour.f = neighbour.g + heuristic(neighbour, targets);
                              best.put(nKey, neighbour);
                              open.add(neighbour);
                           }
                        }
                     }
                  }
               }

               return goalNode == null ? Collections.emptyList() : this.reconstruct(best, goalNode);
            }
         }
      } else {
         return Collections.emptyList();
      }
   }

   private List<class_2338> reconstruct(Map<Long, AutoEndPathfinder.Node> best, AutoEndPathfinder.Node goalNode) {
      List<class_2338> reversed = new ArrayList<>();
      AutoEndPathfinder.Node node = goalNode;
      int guard = 0;

      while (node != null && guard++ < 20000) {
         reversed.add(new class_2338(node.x, node.y, node.z));
         AutoEndPathfinder.Node parent = node.parent;
         if (parent == null) {
            break;
         }

         node = best.get(key(parent));
      }

      Collections.reverse(reversed);
      if (!reversed.isEmpty()) {
         reversed.remove(0);
      }

      return reversed;
   }

   private List<AutoEndPathfinder.Node> expand(class_1937 world, AutoEndPathfinder.Node from) {
      List<AutoEndPathfinder.Node> out = new ArrayList<>(8);

      for (int[] d : SIDES) {
         this.addStep(world, from, d[0], 0, d[1], 1.0, out);
      }

      for (int[][] diag : DIAGONALS) {
         int dx = diag[0][0];
         int dz = diag[0][1];
         int ax = diag[1][0];
         int az = diag[1][1];
         int bx = diag[2][0];
         int bz = diag[2][1];

         for (int drop = 1; drop <= 20; drop++) {
            int ny = from.y - drop;
            int tx = from.x + dx;
            int tz = from.z + dz;
            if (this.isFloor(world, tx, ny, tz)
               && this.isPassable(world, tx, ny, tz)
               && this.isPassable(world, tx, ny + 1, tz)
               && this.isPassable(world, from.x + ax, ny, from.z + az)
               && this.isPassable(world, from.x + bx, ny, from.z + bz)) {
               out.add(new AutoEndPathfinder.Node(tx, ny, tz, from.g + 1.41421356237 + (double)drop, 0.0, from));
               break;
            }
         }
      }

      return out;
   }

   private void addStep(class_1937 world, AutoEndPathfinder.Node from, int dx, int ignoredY, int dz, double cost, List<AutoEndPathfinder.Node> out) {
      int tx = from.x + dx;
      int tz = from.z + dz;
      if (this.isStandable(world, tx, from.y, tz)) {
         out.add(new AutoEndPathfinder.Node(tx, from.y, tz, from.g + cost, 0.0, from));
      } else if (this.isPassable(world, from.x, from.y + 1, from.z) && this.isStandable(world, tx, from.y + 1, tz)) {
         out.add(new AutoEndPathfinder.Node(tx, from.y + 1, tz, from.g + cost + 0.6, 0.0, from));
      } else {
         for (int drop = 1; drop <= 20; drop++) {
            int ny = from.y - drop;
            if (this.isPassable(world, tx, ny + 1, tz) && this.isFloor(world, tx, ny, tz) && this.isPassable(world, tx, ny, tz)) {
               out.add(new AutoEndPathfinder.Node(tx, ny, tz, from.g + cost + (double)drop, 0.0, from));
               return;
            }

            if (!this.isPassable(world, tx, ny, tz)) {
               return;
            }
         }
      }
   }

   private boolean isStandable(class_1937 world, int x, int y, int z) {
      return this.isPassable(world, x, y, z) && this.isPassable(world, x, y + 1, z) && this.isFloor(world, x, y, z);
   }

   private boolean isPassable(class_1937 world, int x, int y, int z) {
      class_2680 state = world.method_8320(new class_2338(x, y, z));
      return state.method_26215() || state.method_45474();
   }

   private boolean isFloor(class_1937 world, int x, int y, int z) {
      class_2680 state = world.method_8320(new class_2338(x, y - 1, z));
      return !state.method_26215() && !state.method_45474();
   }

   private static boolean isTarget(AutoEndPathfinder.Node node, List<class_2338> targets) {
      for (class_2338 target : targets) {
         if (target.method_10263() == node.x && target.method_10264() == node.y && target.method_10260() == node.z) {
            return true;
         }
      }

      return false;
   }

   private static double heuristic(AutoEndPathfinder.Node node, List<class_2338> targets) {
      double best = Double.MAX_VALUE;

      for (class_2338 target : targets) {
         int dx = Math.abs(target.method_10263() - node.x);
         int dy = Math.abs(target.method_10264() - node.y);
         int dz = Math.abs(target.method_10260() - node.z);
         int flat = Math.max(dx, dz);
         int diff = Math.min(dx, dz);
         double h = 1.41421356237 * (double)diff + (double)(flat - diff) + (double)dy;
         if (h < best) {
            best = h;
            if (h == 0.0) {
               break;
            }
         }
      }

      return best;
   }

   private static double heuristic(class_2338 node, List<class_2338> targets) {
      return heuristic(new AutoEndPathfinder.Node(node.method_10263(), node.method_10264(), node.method_10260(), 0.0, 0.0, null), targets);
   }

   private static long key(AutoEndPathfinder.Node node) {
      return key(node.x, node.y, node.z);
   }

   private static long key(int x, int y, int z) {
      return (long)(x & 67108863) << 38 | (long)(z & 67108863) << 12 | (long)(y & 4095);
   }

   private static final class Node {
      final int x;
      final int y;
      final int z;
      final double g;
      final AutoEndPathfinder.Node parent;
      double f;
      long order;

      Node(int x, int y, int z, double g, double f, AutoEndPathfinder.Node parent) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.g = g;
         this.f = f;
         this.parent = parent;
      }
   }
}
