package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1665;
import net.minecraft.class_1667;
import net.minecraft.class_1676;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1753;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9278;
import net.minecraft.class_9334;
import net.minecraft.class_9799;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Trajectories",
   desc = "Показывает траекторию, иконку и точку падения жемчуга, стрелы и трезубца",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Trajectories extends Module {
   private static final int MAX_STEPS = 200;
   public final MultiBooleanSetting items = new MultiBooleanSetting(
      this, "Предметы", BooleanSetting.of("Жемчуг эндера", true), BooleanSetting.of("Стрелы", true), BooleanSetting.of("Трезубец", true)
   );
   public final BooleanSetting fromHand = new BooleanSetting(this, "Из рук", true);
   public final BooleanSetting flying = new BooleanSetting(this, "Летящие снаряды", true);
   public final BooleanSetting showLine = new BooleanSetting(this, "Линия траектории", true);
   public final BooleanSetting glow = new BooleanSetting(this, "Свечение линии", false).setVisible(() -> this.showLine.getValue());
   public final BooleanSetting showIcon = new BooleanSetting(this, "Иконка предмета", true);
   public final BooleanSetting showTime = new BooleanSetting(this, "Время до падения", true);
   public final SliderSetting lineWidth = new SliderSetting(this, "Толщина линии", 2.0F, 1.0F, 5.0F, 0.5F).setVisible(() -> this.showLine.getValue());
   public final SliderSetting indicatorSize = new SliderSetting(this, "Размер метки", 0.5F, 0.1F, 2.0F, 0.1F);
   private static final int HELD_KEY = 0;
   private final class_9799 allocator = new class_9799(131072);
   private final Map<Integer, Trajectories.SmoothedState> smoothed = new LinkedHashMap<>();
   private final List<Trajectories.SmoothedState> renderStates = new ArrayList<>();
   private long lastFrameNs = 0L;
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "trajectories_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline FILL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "trajectories_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "trajectories_line", class_12247.method_75927(LINE_PIPELINE).method_75929(16384).method_75938()
   );
   private static final class_1921 FILL_LAYER = class_1921.method_75940(
      "trajectories_fill", class_12247.method_75927(FILL_PIPELINE).method_75929(4096).method_75938()
   );
   private static final RenderPipeline RIBBON_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "trajectories_ribbon"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final class_1921 RIBBON_LAYER = class_1921.method_75940(
      "trajectories_ribbon", class_12247.method_75927(RIBBON_PIPELINE).method_75929(32768).method_75938()
   );
   private static final RenderPipeline GLOW_RIBBON_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "trajectories_glow_ribbon"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 GLOW_RIBBON_LAYER = class_1921.method_75940(
      "trajectories_glow_ribbon", class_12247.method_75927(GLOW_RIBBON_PIPELINE).method_75929(32768).method_75938()
   );

   @Override
   protected void onDisable() {
      this.smoothed.clear();
      this.renderStates.clear();
      this.lastFrameNs = 0L;
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float partial = e.getTickDelta();
         long now = System.nanoTime();
         float dt = this.lastFrameNs == 0L ? 0.05F : Math.min(0.1F, (float)(now - this.lastFrameNs) / 1.0E9F);
         this.lastFrameNs = now;
         this.updateSmoothedPredictions(partial, dt);
         if (!this.renderStates.isEmpty()) {
            class_4587 stack = e.getMatrixStack();
            class_243 cam = mc.field_1773.method_19418().method_71156();
            Matrix4f matrix = stack.method_23760().method_23761();
            class_4598 immediate = class_4597.method_22991(this.allocator);
            if (this.showLine.getValue()) {
               ArrayList<Trajectories.RibbonData> ribbons = new ArrayList<>(this.renderStates.size());

               for (Trajectories.SmoothedState smoothedState : this.renderStates) {
                  Trajectories.RibbonData data = this.buildRibbon(smoothedState, cam);
                  if (data != null) {
                     ribbons.add(data);
                  }
               }

               if (this.glow.getValue()) {
                  class_4588 glowBuf = immediate.method_73477(GLOW_RIBBON_LAYER);

                  for (Trajectories.RibbonData data : ribbons) {
                     emitGlowRibbon(glowBuf, matrix, data);
                  }
               }

               class_4588 ribbonBuf = immediate.method_73477(RIBBON_LAYER);

               for (Trajectories.RibbonData data : ribbons) {
                  emitRibbon(ribbonBuf, matrix, data);
               }
            }

            class_4588 fill = immediate.method_73477(FILL_LAYER);

            for (Trajectories.SmoothedState smoothedStatex : this.renderStates) {
               this.drawLandingMarkerFill(fill, matrix, cam, smoothedStatex);
            }

            class_4588 lines = immediate.method_73477(LINE_LAYER);

            for (Trajectories.SmoothedState state : this.renderStates) {
               this.drawLandingMarkerLines(lines, matrix, cam, state);
            }

            immediate.method_22993();
         }
      }
   }

   @EventHandler
   public void onDisplay(EventDisplay e) {
      if (mc.field_1724 != null && !this.renderStates.isEmpty()) {
         if (this.showIcon.getValue() || this.showTime.getValue()) {
            for (Trajectories.SmoothedState state : this.renderStates) {
               class_243 screen = Projection.worldSpaceToScreenSpace(state.impact.method_1031(0.0, 0.25, 0.0));
               if (!(screen.field_1350 <= 0.0) && !(screen.field_1350 >= 1.0)) {
                  float centerX = (float)screen.field_1352;
                  float centerY = (float)screen.field_1351;
                  float iconSize = 10.0F;
                  float boxSize = iconSize + 4.0F;
                  if (this.showIcon.getValue() && !state.icon.method_7960()) {
                     RenderUtil.Render2D.rect(centerX - boxSize / 2.0F, centerY - boxSize / 2.0F, boxSize, boxSize, ColorUtil.getColor(11, 11, 11, 140), 4.0F);
                     ItemRender.drawItemWithContext(
                        e.getDrawContext(), state.icon, centerX - iconSize / 2.0F, centerY - iconSize / 2.0F, iconSize / 16.0F, 1.0F
                     );
                  }

                  if (this.showTime.getValue()) {
                     String text = String.format(Locale.ROOT, "%.1fs", (float)state.ticks / 20.0F);
                     float fontSize = 6.0F;
                     float textWidth = Fonts.sf_regular.getWidth(text, fontSize) + 6.0F;
                     float textX = centerX - textWidth / 2.0F;
                     float textY = centerY + boxSize / 2.0F + 2.0F;
                     RenderUtil.Render2D.rect(textX, textY, textWidth, fontSize + 3.0F, ColorUtil.getColor(0, 0, 0, 128), 3.0F);
                     Fonts.sf_regular.draw(text, textX + 3.0F, textY + 1.5F, fontSize, ColorUtil.getColor(255));
                  }
               }
            }
         }
      }
   }

   private void updateSmoothedPredictions(float partial, float dt) {
      for (Trajectories.SmoothedState state : this.smoothed.values()) {
         state.active = false;
      }

      if (this.fromHand.getValue()) {
         this.collectFromHand(partial, dt);
      }

      if (this.flying.getValue()) {
         this.collectFlying(partial, dt);
      }

      this.smoothed.entrySet().removeIf(entry -> !entry.getValue().active);
      this.renderStates.clear();
      this.renderStates.addAll(this.smoothed.values());
   }

   private void pushRaw(int key, Trajectories.Prediction raw, float dt) {
      if (raw != null) {
         Trajectories.SmoothedState state = this.smoothed.get(key);
         if (state == null) {
            state = Trajectories.SmoothedState.from(raw);
            this.smoothed.put(key, state);
         } else {
            state.blendToward(raw, dt, 100.0F);
         }

         state.active = true;
      }
   }

   private void collectFromHand(float partial, float dt) {
      class_1799 main = mc.field_1724.method_6047();
      class_1799 off = mc.field_1724.method_6079();
      if (this.items.getValue("Жемчуг эндера")) {
         if (main.method_31574(class_1802.field_8634)) {
            this.pushRaw(0, this.simulateThrow(main, partial, 1.5, 0.03, false), dt);
         } else if (off.method_31574(class_1802.field_8634)) {
            this.pushRaw(0, this.simulateThrow(off, partial, 1.5, 0.03, true), dt);
         }
      }

      if (this.items.getValue("Стрелы")) {
         if (mc.field_1724.method_6115() && mc.field_1724.method_6030().method_7909() == class_1802.field_8102) {
            float pull = class_1753.method_7722(mc.field_1724.method_6048());
            if (pull >= 0.1F) {
               boolean offHand = mc.field_1724.method_6058() == class_1268.field_5810;
               this.pushRaw(0, this.simulateThrow(new class_1799(class_1802.field_8107), partial, (double)pull * 3.0, 0.05, offHand), dt);
            }
         } else {
            class_1799 crossbow = null;
            boolean crossbowOffHand = false;
            if (main.method_31574(class_1802.field_8399)) {
               crossbow = main;
            } else if (off.method_31574(class_1802.field_8399)) {
               crossbow = off;
               crossbowOffHand = true;
            }

            if (crossbow != null && isCrossbowCharged(crossbow)) {
               this.pushRaw(0, this.simulateThrow(this.getCrossbowProjectile(crossbow), partial, 3.15, 0.05, crossbowOffHand), dt);
            }
         }
      }

      int useTime;
      if (this.items.getValue("Трезубец")
         && mc.field_1724.method_6115()
         && mc.field_1724.method_6030().method_7909() == class_1802.field_8547
         && (useTime = mc.field_1724.method_6048()) > 0) {
         float force = Math.min((float)useTime / 10.0F, 1.0F);
         if (force < 0.1F) {
            force = 0.1F;
         }

         boolean offHand = mc.field_1724.method_6058() == class_1268.field_5810;
         this.pushRaw(0, this.simulateThrow(new class_1799(class_1802.field_8547), partial, 2.5 * (double)force, 0.05, offHand), dt);
      }
   }

   private class_1799 getCrossbowProjectile(class_1799 crossbow) {
      class_9278 charged = (class_9278)crossbow.method_58694(class_9334.field_49649);
      return charged != null && !charged.method_57437().isEmpty() ? (class_1799)charged.method_57437().getFirst() : new class_1799(class_1802.field_8107);
   }

   private void collectFlying(float partial, float dt) {
      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (!(entity.method_18798().method_1027() < 4.0E-4)) {
            class_1799 icon;
            double gravity;
            if (entity instanceof class_1684) {
               if (!this.items.getValue("Жемчуг эндера")) {
                  continue;
               }

               gravity = 0.03;
               icon = new class_1799(class_1802.field_8634);
            } else if (entity instanceof class_1685) {
               class_1685 trident = (class_1685)entity;
               if (!this.items.getValue("Трезубец") || trident.method_24828() || trident.method_7441()) {
                  continue;
               }

               gravity = 0.05;
               icon = new class_1799(class_1802.field_8547);
            } else {
               class_1667 arrow;
               if (!(entity instanceof class_1667) && !(entity instanceof class_1665)
                  || !this.items.getValue("Стрелы")
                  || entity instanceof class_1667 && (arrow = (class_1667)entity).method_24828()) {
                  continue;
               }

               gravity = 0.05;
               icon = new class_1799(class_1802.field_8107);
            }

            class_243 pos = entity.method_30950(partial);
            class_243 vel = entity.method_18798();
            class_1297 class_12972;
            if (entity instanceof class_1676 projectile) {
               class_12972 = projectile.method_24921();
            } else {
               class_12972 = null;
            }

            this.pushRaw(entity.method_5628(), this.simulate(pos, vel, gravity, entity, class_12972, icon), dt);
         }
      }
   }

   private Trajectories.Prediction simulateThrow(class_1799 icon, float partial, double speed, double gravity, boolean offHand) {
      float yaw = mc.field_1724.method_5705(partial);
      float pitch = mc.field_1724.method_5695(partial);
      double dx = (double)(
         -class_3532.method_15374((double)(yaw * (float) (Math.PI / 180.0))) * class_3532.method_15362((double)(pitch * (float) (Math.PI / 180.0)))
      );
      double dy = (double)(-class_3532.method_15374((double)(pitch * (float) (Math.PI / 180.0))));
      double dz = (double)(
         class_3532.method_15362((double)(yaw * (float) (Math.PI / 180.0))) * class_3532.method_15362((double)(pitch * (float) (Math.PI / 180.0)))
      );
      class_243 velocity = new class_243(dx, dy, dz).method_1029().method_1021(speed);
      class_243 playerVel = mc.field_1724.method_18798();
      velocity = velocity.method_1031(playerVel.field_1352, mc.field_1724.method_24828() ? 0.0 : playerVel.field_1351, playerVel.field_1350);
      class_243 lerped = mc.field_1724.method_30950(partial);
      double eyeY = lerped.field_1351 + (double)mc.field_1724.method_5751();
      class_243 start = new class_243(lerped.field_1352, eyeY, lerped.field_1350).method_1019(new class_243(dx, dy, dz).method_1029().method_1021(0.2));
      Trajectories.Prediction prediction = this.simulate(start, velocity, gravity, mc.field_1724, mc.field_1724, icon);
      if (prediction == null) {
         return null;
      } else {
         class_243 forward = new class_243(dx, dy, dz).method_1029();
         class_243 right = class_243.method_1030(0.0F, yaw + 90.0F);
         if (offHand) {
            right = right.method_1021(-1.0);
         }

         class_243 handOffset = right.method_1021(0.32).method_1031(0.0, -0.18, 0.0).method_1019(forward.method_1021(0.25));
         List<class_243> points = prediction.points;
         int blend = Math.min(10, points.size());

         for (int i = 0; i < blend; i++) {
            float w = 1.0F - (float)i / (float)blend;
            w *= w;
            points.set(i, points.get(i).method_1019(handOffset.method_1021((double)w)));
         }

         return prediction;
      }
   }

   private Trajectories.Prediction simulate(class_243 pos, class_243 vel, double gravity, class_1297 ignore, class_1297 owner, class_1799 icon) {
      ArrayList<class_243> points = new ArrayList<>(128);
      points.add(pos);
      double step = 0.25;
      double drag = Math.pow(0.99, step);
      double stepGravity = gravity * step;

      for (int tick = 0; tick < 200; tick++) {
         for (int sub = 0; sub < 4; sub++) {
            class_243 next = pos.method_1019(vel.method_1021(step));
            class_3965 blockHit = mc.field_1687.method_17742(new class_3959(pos, next, class_3960.field_17558, class_242.field_1348, ignore));
            class_243 segmentEnd = blockHit.method_17783() == class_240.field_1332 ? blockHit.method_17784() : next;
            class_1297 hitEntity = null;
            class_243 entityImpact = null;
            double closest = Double.MAX_VALUE;
            class_238 sweep = new class_238(pos, segmentEnd).method_1014(0.25);

            for (class_1297 ent : mc.field_1687
               .method_8333(ignore, sweep, other -> other instanceof class_1309 && other.method_5805() && other != owner && !other.method_7325())) {
               class_243 clip = (class_243)ent.method_5829().method_1014(0.25).method_992(pos, segmentEnd).orElse(null);
               double dist;
               if (clip != null && (dist = clip.method_1025(pos)) < closest) {
                  closest = dist;
                  hitEntity = ent;
                  entityImpact = clip;
               }
            }

            if (hitEntity != null) {
               points.add(entityImpact);
               return new Trajectories.Prediction(densify(points), entityImpact, tick + 1, hitEntity, icon);
            }

            if (blockHit.method_17783() == class_240.field_1332) {
               points.add(segmentEnd);
               return new Trajectories.Prediction(densify(points), segmentEnd, tick + 1, null, icon);
            }

            points.add(next);
            pos = next;
            vel = vel.method_1021(drag).method_1023(0.0, stepGravity, 0.0);
            if (next.field_1351 < (double)(mc.field_1687.method_31607() - 16)) {
               return null;
            }
         }
      }

      return null;
   }

   private static List<class_243> densify(List<class_243> points) {
      if (points.size() < 2) {
         return points;
      } else {
         ArrayList<class_243> out = new ArrayList<>(points.size() * 2);
         out.add(points.getFirst());

         for (int i = 0; i + 1 < points.size(); i++) {
            class_243 a = points.get(i);
            class_243 b = points.get(i + 1);
            out.add(a.method_35590(b, 0.33));
            out.add(a.method_35590(b, 0.66));
            out.add(b);
         }

         return out;
      }
   }

   private Trajectories.RibbonData buildRibbon(Trajectories.SmoothedState state, class_243 cam) {
      List<class_243> points = state.points;
      int start = 0;

      while (points.size() - start > 2 && points.get(start).method_1025(cam) < 0.16) {
         start++;
      }

      int n = points.size() - start;
      if (n < 2) {
         return null;
      } else {
         boolean livingHit = state.hitEntity instanceof class_1309;
         int baseColor = livingHit ? ColorUtil.getColor(64, 255, 64, 255) : ColorUtil.fade(1);
         Trajectories.RibbonData data = new Trajectories.RibbonData();
         data.cr = channel(baseColor, 16);
         data.cg = channel(baseColor, 8);
         data.cb = channel(baseColor, 0);
         data.rel = new class_243[n];
         data.side = new class_243[n];
         data.alpha = new float[n];
         data.halfW = new float[n];
         double tanHalfFov = Math.tan(Math.toRadians((double)((Integer)mc.field_1690.method_41808().method_41753()).floatValue()) / 2.0);
         double pxToWorld = 2.0 * tanHalfFov / (double)Math.max(1, mc.method_22683().method_4506());
         float widthPx = this.lineWidth.getValue();

         for (int i = 0; i < n; i++) {
            data.rel[i] = points.get(start + i).method_1020(cam);
         }

         double dist = 0.0;
         class_243 prevSide = new class_243(0.0, 1.0, 0.0);

         for (int i = 0; i < n; i++) {
            class_243 tangent = data.rel[Math.min(i + 1, n - 1)].method_1020(data.rel[Math.max(i - 1, 0)]);
            class_243 s = tangent.method_1036(data.rel[i]);
            s = s.method_1027() < 1.0E-8 ? prevSide : s.method_1029();
            prevSide = s;
            data.side[i] = s;
            data.halfW[i] = (float)(0.5 * (double)widthPx * data.rel[i].method_1033() * pxToWorld);
            if (i > 0) {
               dist += points.get(start + i).method_1022(points.get(start + i - 1));
            }

            data.alpha[i] = (float)class_3532.method_15350((dist - 0.1) / 0.55, 0.0, 1.0);
         }

         return data;
      }
   }

   private static void emitRibbon(class_4588 buf, Matrix4f m, Trajectories.RibbonData d) {
      int n = d.rel.length;

      for (int i = 0; i < n - 1; i++) {
         int a1 = (int)(d.alpha[i] * 255.0F);
         int a2 = (int)(d.alpha[i + 1] * 255.0F);
         if (a1 > 0 || a2 > 0) {
            float t1 = (float)i / (float)(n - 1);
            float t2 = (float)(i + 1) / (float)(n - 1);
            int r1 = lighten(d.cr, t1);
            int g1 = lighten(d.cg, t1);
            int b1 = lighten(d.cb, t1);
            int r2 = lighten(d.cr, t2);
            int g2 = lighten(d.cg, t2);
            int b2 = lighten(d.cb, t2);
            class_243 c1 = d.rel[i];
            class_243 c2 = d.rel[i + 1];
            class_243 o1 = d.side[i].method_1021((double)d.halfW[i]);
            class_243 o2 = d.side[i + 1].method_1021((double)d.halfW[i + 1]);
            class_243 i1 = o1.method_1021(0.7);
            class_243 i2 = o2.method_1021(0.7);
            vtx(buf, m, c1.method_1020(i1), r1, g1, b1, a1);
            vtx(buf, m, c2.method_1020(i2), r2, g2, b2, a2);
            vtx(buf, m, c2.method_1019(i2), r2, g2, b2, a2);
            vtx(buf, m, c1.method_1019(i1), r1, g1, b1, a1);
            vtx(buf, m, c1.method_1019(i1), r1, g1, b1, a1);
            vtx(buf, m, c2.method_1019(i2), r2, g2, b2, a2);
            vtx(buf, m, c2.method_1019(o2), r2, g2, b2, 0);
            vtx(buf, m, c1.method_1019(o1), r1, g1, b1, 0);
            vtx(buf, m, c1.method_1020(i1), r1, g1, b1, a1);
            vtx(buf, m, c2.method_1020(i2), r2, g2, b2, a2);
            vtx(buf, m, c2.method_1020(o2), r2, g2, b2, 0);
            vtx(buf, m, c1.method_1020(o1), r1, g1, b1, 0);
         }
      }
   }

   private static void emitGlowRibbon(class_4588 buf, Matrix4f m, Trajectories.RibbonData d) {
      int n = d.rel.length;

      for (int i = 0; i < n - 1; i++) {
         int a1 = (int)(d.alpha[i] * 0.3F * 255.0F);
         int a2 = (int)(d.alpha[i + 1] * 0.3F * 255.0F);
         if (a1 > 0 || a2 > 0) {
            class_243 c1 = d.rel[i];
            class_243 c2 = d.rel[i + 1];
            class_243 o1 = d.side[i].method_1021((double)(d.halfW[i] * 3.0F));
            class_243 o2 = d.side[i + 1].method_1021((double)(d.halfW[i + 1] * 3.0F));
            vtx(buf, m, c1, d.cr, d.cg, d.cb, a1);
            vtx(buf, m, c2, d.cr, d.cg, d.cb, a2);
            vtx(buf, m, c2.method_1019(o2), d.cr, d.cg, d.cb, 0);
            vtx(buf, m, c1.method_1019(o1), d.cr, d.cg, d.cb, 0);
            vtx(buf, m, c1, d.cr, d.cg, d.cb, a1);
            vtx(buf, m, c2, d.cr, d.cg, d.cb, a2);
            vtx(buf, m, c2.method_1020(o2), d.cr, d.cg, d.cb, 0);
            vtx(buf, m, c1.method_1020(o1), d.cr, d.cg, d.cb, 0);
         }
      }
   }

   private static int lighten(int c, float t) {
      return c + (int)((float)(255 - c) * 0.45F * t);
   }

   private static void vtx(class_4588 buf, Matrix4f m, class_243 v, int r, int g, int b, int a) {
      buf.method_22918(m, (float)v.field_1352, (float)v.field_1351, (float)v.field_1350).method_1336(r, g, b, a);
   }

   private void drawLandingMarkerFill(class_4588 fill, Matrix4f matrix, class_243 cam, Trajectories.SmoothedState state) {
      float radius = this.indicatorSize.getValue();
      if (!(radius <= 0.01F)) {
         class_243 impact = state.impact;
         boolean livingHit = state.hitEntity instanceof class_1309;
         int baseColor = livingHit ? ColorUtil.getColor(64, 255, 64, 160) : ColorUtil.multAlpha(ColorUtil.fade(1), 0.6F);
         int edgeColor = livingHit ? ColorUtil.getColor(64, 255, 64, 40) : ColorUtil.multAlpha(ColorUtil.fade(1), 0.15F);
         float cx = (float)(impact.field_1352 - cam.field_1352);
         float cy = (float)(impact.field_1351 - cam.field_1351) + 0.02F;
         float cz = (float)(impact.field_1350 - cam.field_1350);
         int cr = channel(baseColor, 16);
         int cg = channel(baseColor, 8);
         int cb = channel(baseColor, 0);
         int ca = alphaOf(baseColor);
         int er = channel(edgeColor, 16);
         int eg = channel(edgeColor, 8);
         int eb = channel(edgeColor, 0);
         int ea = alphaOf(edgeColor);

         for (int i = 0; i < 24; i++) {
            double a0 = (Math.PI * 2) * (double)i / 24.0;
            double a1 = (Math.PI * 2) * (double)(i + 1) / 24.0;
            float x0 = cx + (float)(Math.cos(a0) * (double)radius);
            float z0 = cz + (float)(Math.sin(a0) * (double)radius);
            float x1 = cx + (float)(Math.cos(a1) * (double)radius);
            float z1 = cz + (float)(Math.sin(a1) * (double)radius);
            fill.method_22918(matrix, cx, cy, cz).method_1336(cr, cg, cb, ca);
            fill.method_22918(matrix, x0, cy, z0).method_1336(er, eg, eb, ea);
            fill.method_22918(matrix, x1, cy, z1).method_1336(er, eg, eb, ea);
            fill.method_22918(matrix, cx, cy, cz).method_1336(cr, cg, cb, ca);
         }
      }
   }

   private void drawLandingMarkerLines(class_4588 lines, Matrix4f matrix, class_243 cam, Trajectories.SmoothedState state) {
      float radius = this.indicatorSize.getValue();
      if (!(radius <= 0.01F)) {
         class_243 impact = state.impact;
         boolean livingHit = state.hitEntity instanceof class_1309;
         int baseColor = livingHit ? ColorUtil.getColor(64, 255, 64, 160) : ColorUtil.multAlpha(ColorUtil.fade(1), 0.6F);
         int crossColor = livingHit ? ColorUtil.getColor(64, 255, 64, 200) : ColorUtil.multAlpha(baseColor, 1.0F);
         float cx = (float)(impact.field_1352 - cam.field_1352);
         float cy = (float)(impact.field_1351 - cam.field_1351) + 0.02F;
         float cz = (float)(impact.field_1350 - cam.field_1350);
         int xcr = channel(crossColor, 16);
         int xcg = channel(crossColor, 8);
         int xcb = channel(crossColor, 0);
         int xca = alphaOf(crossColor);
         float cross = Math.max(0.15F, radius * 0.35F);
         drawLine(lines, matrix, cx - cross, cy, cz, cx + cross, cy, cz, xcr, xcg, xcb, xca);
         drawLine(lines, matrix, cx, cy, cz - cross, cx, cy, cz + cross, xcr, xcg, xcb, xca);
      }
   }

   private static void drawLine(class_4588 buffer, Matrix4f matrix, float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int a) {
      buffer.method_22918(matrix, x0, y0, z0).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
   }

   private static int channel(int color, int shift) {
      return color >> shift & 0xFF;
   }

   private static int alphaOf(int color) {
      int a = color >> 24 & 0xFF;
      return a == 0 ? 255 : a;
   }

   private static boolean isCrossbowCharged(class_1799 stack) {
      class_9278 charged = (class_9278)stack.method_58694(class_9334.field_49649);
      return charged != null && !charged.method_57442();
   }

   @Environment(EnvType.CLIENT)
   private static record Prediction(List<class_243> points, class_243 impact, int ticks, class_1297 hitEntity, class_1799 icon) {
   }

   @Environment(EnvType.CLIENT)
   private static final class RibbonData {
      class_243[] rel;
      class_243[] side;
      float[] alpha;
      float[] halfW;
      int cr;
      int cg;
      int cb;
   }

   @Environment(EnvType.CLIENT)
   private static final class SmoothedState {
      List<class_243> points = new ArrayList<>();
      class_243 impact = class_243.field_1353;
      int ticks;
      class_1297 hitEntity;
      class_1799 icon = class_1799.field_8037;
      boolean active;

      static Trajectories.SmoothedState from(Trajectories.Prediction raw) {
         Trajectories.SmoothedState state = new Trajectories.SmoothedState();
         state.points = new ArrayList<>(raw.points());
         state.impact = raw.impact();
         state.ticks = raw.ticks();
         state.hitEntity = raw.hitEntity();
         state.icon = raw.icon().method_7972();
         return state;
      }

      void blendToward(Trajectories.Prediction raw, float dt, float speed) {
         float k = 1.0F - (float)Math.exp((double)(-dt * speed));
         if (this.impact.method_1025(raw.impact()) > 4096.0) {
            this.points = new ArrayList<>(raw.points());
            this.impact = raw.impact();
         } else {
            this.impact = this.impact.method_35590(raw.impact(), (double)k);
            this.blendPoints(raw.points(), k);
         }

         this.ticks = raw.ticks();
         this.hitEntity = raw.hitEntity();
         this.icon = raw.icon().method_7972();
      }

      private void blendPoints(List<class_243> target, float k) {
         if (target.isEmpty()) {
            this.points.clear();
         } else {
            int count = Math.max(this.points.size(), target.size());
            if (count <= 1) {
               this.points = new ArrayList<>(target);
            } else {
               List<class_243> from = resample(this.points, count);
               List<class_243> to = resample(target, count);
               this.points = new ArrayList<>(count);

               for (int i = 0; i < count; i++) {
                  float weight = k * (0.15F + 0.85F * (float)i / (float)(count - 1));
                  this.points.add(from.get(i).method_35590(to.get(i), (double)weight));
               }
            }
         }
      }

      private static List<class_243> resample(List<class_243> src, int count) {
         if (src.isEmpty()) {
            return new ArrayList<>();
         } else if (src.size() != 1 && count > 1) {
            double total = 0.0;

            for (int i = 0; i + 1 < src.size(); i++) {
               total += src.get(i).method_1022(src.get(i + 1));
            }

            ArrayList<class_243> out = new ArrayList<>(count);
            out.add(src.getFirst());

            for (int i2 = 1; i2 < count - 1; i2++) {
               out.add(sampleAtDistance(src, total * (double)i2 / (double)(count - 1)));
            }

            out.add(src.getLast());
            return out;
         } else {
            ArrayList<class_243> out = new ArrayList<>(count);
            class_243 only = src.getFirst();

            for (int i = 0; i < count; i++) {
               out.add(only);
            }

            return out;
         }
      }

      private static class_243 sampleAtDistance(List<class_243> src, double dist) {
         double walked = 0.0;

         for (int i = 0; i + 1 < src.size(); i++) {
            class_243 a = src.get(i);
            class_243 b;
            double seg = a.method_1022(b = src.get(i + 1));
            if (walked + seg >= dist) {
               double t = seg <= 1.0E-8 ? 0.0 : (dist - walked) / seg;
               return a.method_35590(b, t);
            }

            walked += seg;
         }

         return src.getLast();
      }
   }
}
