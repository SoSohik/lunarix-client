package ru.mentions.module.impl.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public class LightningTargetESP {
   private static final LightningTargetESP INSTANCE = new LightningTargetESP();
   private final class_310 mc = class_310.method_1551();
   private final ArrayList<LightningTargetESP.Bolt> bolts = new ArrayList<>();
   private long lastBoltSpawn = 0L;
   public float lightningCount = 4.0F;
   public float lightningWidth = 1.0F;
   public boolean hurtColor = true;
   private float appearValue = 0.0F;
   private class_1309 lastTarget = null;
   private class_243 lastTargetPos = null;
   private long lastAttackTime = 0L;
   private class_1309 lastAttackedEntity = null;

   public static LightningTargetESP getInstance() {
      return INSTANCE;
   }

   public void onAttack(class_1309 entity) {
      this.lastAttackTime = System.currentTimeMillis();
      this.lastAttackedEntity = entity;
   }

   public void render(class_4598 immediate, class_4587 matrices, class_4184 camera, float tickDelta, class_1309 currentTarget, float alphaPC) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         boolean hasTarget = currentTarget != null && currentTarget.method_5805();
         this.appearValue = this.animateTo(this.appearValue, hasTarget ? 1.0F : 0.0F, 0.08F);
         float masterAlpha = this.appearValue * alphaPC;
         if (hasTarget) {
            this.lastTarget = currentTarget;
            class_243 p = currentTarget.method_30950(tickDelta);
            this.lastTargetPos = new class_243(p.field_1352, p.field_1351, p.field_1350);
         }

         if (masterAlpha <= 0.001F) {
            this.bolts.clear();
            if (!hasTarget) {
               this.lastTarget = null;
               this.lastTargetPos = null;
            }
         } else {
            this.renderLightning(immediate, matrices, camera, tickDelta, hasTarget, masterAlpha);
         }
      }
   }

   private void renderLightning(class_4598 immediate, class_4587 matrices, class_4184 camera, float partialTicks, boolean hasTarget, float masterAlpha) {
      if (this.lastTarget != null || this.lastTargetPos != null) {
         long now = System.currentTimeMillis();
         int maxBolts = Math.max(4, Math.round(this.lightningCount) * 2);
         long spawnInterval = (long)Math.max(15.0F, 65.0F / Math.max(0.1F, this.lightningCount / 3.0F));
         if (hasTarget && this.lastTarget != null && this.lastTarget.method_5805() && now - this.lastBoltSpawn > spawnInterval && this.bolts.size() < maxBolts) {
            this.bolts.add(this.spawnBolt(this.lastTarget));
            this.lastBoltSpawn = now;
         }

         this.bolts.removeIf(b -> now - b.spawn > b.life);
         if (!this.bolts.isEmpty()) {
            class_243 basePos;
            if (this.lastTarget != null && this.lastTarget.method_5805()) {
               basePos = this.lastTarget.method_30950(partialTicks);
            } else {
               basePos = this.lastTargetPos;
            }

            if (basePos != null) {
               class_243 cam = camera.method_71156();
               float hurtPC = this.getHurtPC(this.lastTarget);
               int baseESP = ColorUtil.fade(1);
               int redColor = -50116;
               int glowColor = this.blendColor(baseESP, redColor, hurtPC);
               int coreColor = this.blendColor(-1, redColor, hurtPC);
               float widthScale = Math.max(0.2F, this.lightningWidth);
               class_4588 outerBuffer = immediate.method_73477(TargetEsp.RING_FILL_LAYER);

               for (LightningTargetESP.Bolt bolt : this.bolts) {
                  float alpha = this.boltAlpha(bolt, now) * masterAlpha;
                  if (!(alpha <= 0.02F)) {
                     int outerA = (int)(alpha * 45.0F);
                     if (outerA > 0) {
                        this.appendRibbon(outerBuffer, matrices, basePos, cam, bolt.points, 0.045F * widthScale, this.setAlpha(glowColor, outerA));

                        for (List<class_243> branch : bolt.branches) {
                           this.appendRibbon(
                              outerBuffer, matrices, basePos, cam, branch, 0.03F * widthScale, this.setAlpha(glowColor, (int)((float)outerA * 0.85F))
                           );
                        }
                     }
                  }
               }

               class_4588 glowBuffer = immediate.method_73477(TargetEsp.RING_FILL_LAYER);

               for (LightningTargetESP.Bolt boltx : this.bolts) {
                  float alpha = this.boltAlpha(boltx, now) * masterAlpha;
                  if (!(alpha <= 0.02F)) {
                     int glowA = (int)(alpha * 95.0F);
                     if (glowA > 0) {
                        this.appendRibbon(glowBuffer, matrices, basePos, cam, boltx.points, 0.018F * widthScale, this.setAlpha(glowColor, glowA));

                        for (List<class_243> branch : boltx.branches) {
                           this.appendRibbon(
                              glowBuffer, matrices, basePos, cam, branch, 0.012F * widthScale, this.setAlpha(glowColor, (int)((float)glowA * 0.8F))
                           );
                        }
                     }
                  }
               }

               class_4588 coreBuffer = immediate.method_73477(TargetEsp.RING_FILL_LAYER);

               for (LightningTargetESP.Bolt boltxx : this.bolts) {
                  float alpha = this.boltAlpha(boltxx, now) * masterAlpha;
                  if (!(alpha <= 0.02F)) {
                     int coreA = (int)(alpha * 235.0F);
                     if (coreA > 0) {
                        this.appendRibbon(coreBuffer, matrices, basePos, cam, boltxx.points, 0.006F * widthScale, this.setAlpha(coreColor, coreA));

                        for (List<class_243> branch : boltxx.branches) {
                           this.appendRibbon(
                              coreBuffer, matrices, basePos, cam, branch, 0.004F * widthScale, this.setAlpha(coreColor, (int)((float)coreA * 0.85F))
                           );
                        }
                     }
                  }
               }

               class_2960 bloomTex = TargetEsp.visualTexture("bloom.png");

               for (LightningTargetESP.Bolt boltxxx : this.bolts) {
                  float alpha = this.boltAlpha(boltxxx, now) * masterAlpha;
                  if (!(alpha <= 0.03F)) {
                     int wideBloomA = (int)(alpha * 45.0F);
                     if (wideBloomA > 0) {
                        for (int i = 0; i < boltxxx.points.size(); i += 2) {
                           class_243 p = basePos.method_1019(boltxxx.points.get(i));
                           TargetEsp.drawBillboardTESP(
                              immediate,
                              matrices,
                              camera,
                              p.field_1352 - cam.field_1352,
                              p.field_1351 - cam.field_1351,
                              p.field_1350 - cam.field_1350,
                              0.28F * widthScale,
                              0.28F * widthScale,
                              0.0F,
                              bloomTex,
                              glowColor,
                              wideBloomA
                           );
                        }
                     }

                     int nodeA = (int)(alpha * 120.0F);
                     if (nodeA > 0) {
                        for (int i = 0; i < boltxxx.points.size(); i++) {
                           class_243 p = basePos.method_1019(boltxxx.points.get(i));
                           TargetEsp.drawBillboardTESP(
                              immediate,
                              matrices,
                              camera,
                              p.field_1352 - cam.field_1352,
                              p.field_1351 - cam.field_1351,
                              p.field_1350 - cam.field_1350,
                              0.08F * widthScale,
                              0.08F * widthScale,
                              0.0F,
                              bloomTex,
                              coreColor,
                              nodeA
                           );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private LightningTargetESP.Bolt spawnBolt(class_1309 target) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      float radius = target.method_17681() * 0.5F;
      float height = target.method_17682();
      double startAngle = random.nextDouble() * Math.PI * 2.0;
      double startY = 0.15 + random.nextDouble() * Math.max(0.2, (double)height - 0.4);
      class_243 start = new class_243(Math.cos(startAngle) * (double)radius * 0.7, startY, Math.sin(startAngle) * (double)radius * 0.7);
      double theta = random.nextDouble() * Math.PI * 2.0;
      double phi = Math.toRadians((random.nextDouble() - 0.5) * 150.0);
      class_243 dir = new class_243(Math.cos(phi) * Math.cos(theta), Math.sin(phi) * 0.8 + 0.25, Math.cos(phi) * Math.sin(theta)).method_1029();
      double length = 0.35 + random.nextDouble() * 0.45;
      class_243 end = start.method_1019(dir.method_1021(length));
      ArrayList<class_243> points = new ArrayList<>(List.of(start, end));
      points = this.displacePoints(points, 0.09, 3);
      ArrayList<List<class_243>> branches = new ArrayList<>();
      int branchCount = 1 + random.nextInt(3);

      for (int i = 0; i < branchCount; i++) {
         int anchorIndex = 1 + random.nextInt(Math.max(1, points.size() - 2));
         class_243 anchor = points.get(anchorIndex);
         class_243 branchDir = this.randomPerpendicular(dir);
         if (random.nextBoolean()) {
            branchDir = branchDir.method_1021(-1.0);
         }

         double branchLength = 0.12 + random.nextDouble() * 0.18;
         class_243 branchEnd = anchor.method_1019(branchDir.method_1021(branchLength));
         ArrayList<class_243> branchPoints = new ArrayList<>(List.of(anchor, branchEnd));
         branches.add(this.displacePoints(branchPoints, 0.05, 2));
      }

      return new LightningTargetESP.Bolt(points, branches, 140L + random.nextLong(160L), random.nextFloat() * 6.28F);
   }

   private ArrayList<class_243> displacePoints(ArrayList<class_243> input, double amount, int passes) {
      ArrayList<class_243> points = new ArrayList<>(input);
      double amt = amount;

      for (int pass = 0; pass < passes; pass++) {
         ArrayList<class_243> next = new ArrayList<>(points.size() * 2);

         for (int i = 0; i < points.size() - 1; i++) {
            class_243 a = points.get(i);
            class_243 b = points.get(i + 1);
            next.add(a);
            class_243 dir = b.method_1020(a);
            if (dir.method_1027() < 1.0E-7) {
               next.add(a.method_1019(b).method_1021(0.5));
            } else {
               class_243 perp = this.randomPerpendicular(dir.method_1029());
               double offset = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0 * amt;
               next.add(a.method_1019(b).method_1021(0.5).method_1019(perp.method_1021(offset)));
            }
         }

         next.add(points.get(points.size() - 1));
         points = next;
         amt *= 0.5;
      }

      return points;
   }

   private class_243 randomPerpendicular(class_243 dir) {
      class_243 arbitrary = Math.abs(dir.field_1351) < 0.9 ? new class_243(0.0, 1.0, 0.0) : new class_243(1.0, 0.0, 0.0);
      class_243 perp1 = dir.method_1036(arbitrary).method_1029();
      class_243 perp2 = dir.method_1036(perp1).method_1029();
      double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
      return perp1.method_1021(Math.cos(angle)).method_1019(perp2.method_1021(Math.sin(angle)));
   }

   private boolean appendRibbon(class_4588 buffer, class_4587 matrices, class_243 base, class_243 cam, List<class_243> points, float width, int color) {
      int a = color >> 24 & 0xFF;
      if (a > 0 && points.size() >= 2) {
         Matrix4f matrix = matrices.method_23760().method_23761();
         float half = width * 0.5F;
         int appended = 0;

         for (int i = 0; i < points.size() - 1; i++) {
            class_243 p1 = base.method_1019(points.get(i));
            class_243 p2 = base.method_1019(points.get(i + 1));
            class_243 segDir = p2.method_1020(p1);
            if (!(segDir.method_1027() < 1.0E-8)) {
               class_243 viewDir = p1.method_1019(p2).method_1021(0.5).method_1020(cam);
               if (!(viewDir.method_1027() < 1.0E-8)) {
                  class_243 side = segDir.method_1036(viewDir);
                  double sideLen = side.method_1033();
                  if (!(sideLen < 1.0E-8) && Double.isFinite(sideLen)) {
                     side = side.method_1021(1.0 / sideLen);
                     float x1 = (float)(p1.field_1352 - cam.field_1352);
                     float y1 = (float)(p1.field_1351 - cam.field_1351);
                     float z1 = (float)(p1.field_1350 - cam.field_1350);
                     float x2 = (float)(p2.field_1352 - cam.field_1352);
                     float y2 = (float)(p2.field_1351 - cam.field_1351);
                     float z2 = (float)(p2.field_1350 - cam.field_1350);
                     float sx = (float)side.field_1352 * half;
                     float sy = (float)side.field_1351 * half;
                     float sz = (float)side.field_1350 * half;
                     buffer.method_22918(matrix, x1 - sx, y1 - sy, z1 - sz).method_39415(color);
                     buffer.method_22918(matrix, x1 + sx, y1 + sy, z1 + sz).method_39415(color);
                     buffer.method_22918(matrix, x2 + sx, y2 + sy, z2 + sz).method_39415(color);
                     buffer.method_22918(matrix, x2 - sx, y2 - sy, z2 - sz).method_39415(color);
                     appended++;
                  }
               }
            }
         }

         return appended > 0;
      } else {
         return false;
      }
   }

   private float boltAlpha(LightningTargetESP.Bolt bolt, long now) {
      float life = class_3532.method_15363((float)(now - bolt.spawn) / (float)bolt.life, 0.0F, 1.0F);
      float fade = life < 0.18F ? life / 0.18F : 1.0F - (life - 0.18F) / 0.82F;
      float flicker = 0.78F + 0.22F * (float)Math.sin((double)now * 0.045 + (double)bolt.phase);
      return class_3532.method_15363(fade, 0.0F, 1.0F) * flicker;
   }

   private float getHurtPC(class_1309 target) {
      if (!this.hurtColor) {
         return 0.0F;
      } else {
         float fromHurtTime = 0.0F;
         if (target != null && target.field_6235 > 0) {
            float hurtTicks = class_3532.method_15363((float)target.field_6235, 0.0F, 10.0F);
            fromHurtTime = hurtTicks / 10.0F;
         }

         float fromAttack = 0.0F;
         long elapsed = System.currentTimeMillis() - this.lastAttackTime;
         if (elapsed < 420L && (target == null || target == this.lastAttackedEntity || target == this.lastTarget)) {
            fromAttack = 1.0F - (float)elapsed / 420.0F;
         }

         float progress = Math.max(fromHurtTime, fromAttack);
         return class_3532.method_15363(progress * progress * (3.0F - 2.0F * progress), 0.0F, 1.0F);
      }
   }

   private int blendColor(int c1, int c2, float factor) {
      factor = class_3532.method_15363(factor, 0.0F, 1.0F);
      int r1 = c1 >> 16 & 0xFF;
      int g1 = c1 >> 8 & 0xFF;
      int b1 = c1 & 0xFF;
      int a1 = c1 >> 24 & 0xFF;
      int r2 = c2 >> 16 & 0xFF;
      int g2 = c2 >> 8 & 0xFF;
      int b2 = c2 & 0xFF;
      int a2 = c2 >> 24 & 0xFF;
      int r = (int)((float)r1 + (float)(r2 - r1) * factor);
      int g = (int)((float)g1 + (float)(g2 - g1) * factor);
      int b = (int)((float)b1 + (float)(b2 - b1) * factor);
      int a = (int)((float)a1 + (float)(a2 - a1) * factor);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private int setAlpha(int color, int alpha) {
      alpha = Math.max(0, Math.min(255, alpha));
      return alpha << 24 | color & 16777215;
   }

   private float animateTo(float current, float target, float delta) {
      if (current < target) {
         return Math.min(current + delta, target);
      } else {
         return current > target ? Math.max(current - delta, target) : current;
      }
   }

   private static class Bolt {
      final List<class_243> points;
      final List<List<class_243>> branches;
      final long spawn;
      final long life;
      final float phase;

      Bolt(List<class_243> points, List<List<class_243>> branches, long life, float phase) {
         this.points = points;
         this.branches = branches;
         this.spawn = System.currentTimeMillis();
         this.life = life;
         this.phase = phase;
      }
   }
}
