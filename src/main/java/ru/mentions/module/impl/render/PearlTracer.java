package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_1676;
import net.minecraft.class_1684;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;

@ModuleInfo(
   name = "Pearl Tracer",
   desc = "Траектория и точка падения летящих жемчужин",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class PearlTracer extends Module {
   public BooleanSetting showTrajectory = new BooleanSetting(this, "Траектория", true);
   public BooleanSetting showLanding = new BooleanSetting(this, "Точка падения", true);
   public BooleanSetting ignoreOwn = new BooleanSetting(this, "Игнорировать свои", true);
   public BooleanSetting trackFriends = new BooleanSetting(this, "Отслеживать друзей", true);
   public ColorSetting trajectoryColor = new ColorSetting(this, "Цвет траектории", -1);
   public ColorSetting landingColor = new ColorSetting(this, "Цвет точки падения", -1);
   public ColorSetting friendColor = new ColorSetting(this, "Цвет друзей", -10046721);
   private final Map<Integer, PearlTracer.Trace> traces = new LinkedHashMap<>();
   private final class_9799 allocator = new class_9799(131072);
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "pearl_tracer_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "pearl_tracer_line", class_12247.method_75927(LINE_PIPELINE).method_75929(16384).method_75938()
   );

   @Override
   protected void onDisable() {
      this.traces.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.allocator.method_60809();
      this.traces.clear();
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (PearlTracer.Trace t : this.traces.values()) {
            t.seen = false;
         }

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1684) {
               class_1297 owner = entity instanceof class_1676 ? ((class_1676)entity).method_24921() : null;
               boolean own = owner == mc.field_1724;
               if (!own || !this.ignoreOwn.getValue()) {
                  int id = entity.method_5628();
                  PearlTracer.Trace trace = this.traces.get(id);
                  if (trace == null) {
                     trace = new PearlTracer.Trace();
                     this.simulate(entity, trace);
                     this.traces.put(id, trace);
                  }

                  trace.seen = true;
                  boolean friend = false;
                  if (owner != null && !own && owner.method_5477() != null) {
                     friend = Client.get().friendManager().isFriend(owner.method_5477().getString());
                  }

                  trace.friend = friend;
                  this.updateProgress(trace, entity.method_30950(e.getTickDelta()));
               }
            }
         }

         this.traces.entrySet().removeIf(en -> !en.getValue().seen);
         if (!this.traces.isEmpty()) {
            class_4587 stack = e.getMatrixStack();
            class_243 cam = mc.field_1773.method_19418().method_71156();
            Matrix4f matrix = stack.method_23760().method_23761();
            class_4598 immediate = class_4597.method_22991(this.allocator);
            class_4588 buf = immediate.method_73477(LINE_LAYER);

            for (PearlTracer.Trace tracex : this.traces.values()) {
               int color = this.trackFriends.getValue() && tracex.friend ? this.friendColor.getValue() : this.trajectoryColor.getValue();
               if (this.showTrajectory.getValue()) {
                  this.drawTrajectory(buf, matrix, cam, tracex, color);
               }

               if (this.showLanding.getValue() && tracex.landed) {
                  int landColor = this.trackFriends.getValue() && tracex.friend ? this.friendColor.getValue() : this.landingColor.getValue();
                  this.drawLanding(buf, matrix, cam, tracex, landColor);
               }
            }

            immediate.method_22993();
         }
      }
   }

   private void simulate(class_1297 entity, PearlTracer.Trace trace) {
      trace.path.clear();
      trace.landed = false;
      class_243 pos = entity.method_30950(1.0F);
      class_243 vel = entity.method_18798();

      for (int step = 0; step < 200; step++) {
         trace.path.add(pos);
         vel = new class_243(vel.field_1352 * 0.99, (vel.field_1351 - 0.03) * 0.99, vel.field_1350 * 0.99);
         class_243 next = pos.method_1019(vel);
         class_3965 hit = mc.field_1687.method_17742(new class_3959(pos, next, class_3960.field_17558, class_242.field_1348, entity));
         if (hit.method_17783() == class_240.field_1332) {
            class_243 impact = hit.method_17784();
            trace.landing = impact;
            trace.landed = true;
            trace.path.add(impact);
            return;
         }

         pos = next;
         if (next.field_1351 < (double)(mc.field_1687.method_31607() - 16)) {
            return;
         }
      }

      if (!trace.path.isEmpty()) {
         trace.landing = trace.path.get(trace.path.size() - 1);
         trace.landed = true;
      }
   }

   private void updateProgress(PearlTracer.Trace trace, class_243 position) {
      List<class_243> path = trace.path;
      int count = path.size();
      if (count >= 2) {
         float found = trace.progress;
         int index = (int)trace.progress;
         int start = index == 0 ? 0 : index - 1;
         int end = Math.min(count - 1, index + 10);
         double best = Double.MAX_VALUE;

         for (int i = start; i < end; i++) {
            class_243 from = path.get(i);
            class_243 to = path.get(i + 1);
            double dx = to.field_1352 - from.field_1352;
            double dy = to.field_1351 - from.field_1351;
            double dz = to.field_1350 - from.field_1350;
            double lenSq = dx * dx + dy * dy + dz * dz;
            if (!(lenSq < 1.0E-4)) {
               double t = (
                     (position.field_1352 - from.field_1352) * dx + (position.field_1351 - from.field_1351) * dy + (position.field_1350 - from.field_1350) * dz
                  )
                  / lenSq;
               t = Math.max(0.0, Math.min(1.0, t));
               double ox = t * dx + from.field_1352 - position.field_1352;
               double oy = t * dy + from.field_1351 - position.field_1351;
               double oz = t * dz + from.field_1350 - position.field_1350;
               double dist = ox * ox + oy * oy + oz * oz;
               if (dist < best) {
                  best = dist;
                  found = (float)((double)i + t);
               }
            }
         }

         if (found > trace.progress) {
            trace.progress = found;
         }
      }
   }

   private void drawTrajectory(class_4588 buf, Matrix4f m, class_243 cam, PearlTracer.Trace trace, int color) {
      List<class_243> path = trace.path;
      int count = path.size();
      int index = (int)trace.progress;
      if (count > 1 && index < count - 1) {
         double t = (double)trace.progress - (double)index;
         class_243 from = path.get(index);
         class_243 to = path.get(index + 1);
         class_243 start = from.method_1019(to.method_1020(from).method_1021(t));
         class_243 prev = start;

         for (int i = index + 1; i < count; i++) {
            class_243 p = path.get(i);
            this.line(buf, m, cam, prev, p, color);
            prev = p;
         }
      }
   }

   private void drawLanding(class_4588 buf, Matrix4f m, class_243 cam, PearlTracer.Trace trace, int color) {
      class_243 p = trace.landing;
      double h = 0.5;
      this.line(buf, m, cam, p.method_1031(-h, 0.0, 0.0), p.method_1031(h, 0.0, 0.0), color);
      this.line(buf, m, cam, p.method_1031(0.0, -h, 0.0), p.method_1031(0.0, h, 0.0), color);
      this.line(buf, m, cam, p.method_1031(0.0, 0.0, -h), p.method_1031(0.0, 0.0, h), color);
      int shaded = (color >>> 24) / 2 << 24 | color & 16777215;
      double min = -0.3;
      double max = 0.3;
      double top = 1.8;

      for (double y : new double[]{0.0, top}) {
         this.line(buf, m, cam, p.method_1031(min, y, min), p.method_1031(max, y, min), shaded);
         this.line(buf, m, cam, p.method_1031(max, y, min), p.method_1031(max, y, max), shaded);
         this.line(buf, m, cam, p.method_1031(max, y, max), p.method_1031(min, y, max), shaded);
         this.line(buf, m, cam, p.method_1031(min, y, max), p.method_1031(min, y, min), shaded);
      }

      this.line(buf, m, cam, p.method_1031(min, 0.0, min), p.method_1031(min, top, min), shaded);
      this.line(buf, m, cam, p.method_1031(max, 0.0, min), p.method_1031(max, top, min), shaded);
      this.line(buf, m, cam, p.method_1031(max, 0.0, max), p.method_1031(max, top, max), shaded);
      this.line(buf, m, cam, p.method_1031(min, 0.0, max), p.method_1031(min, top, max), shaded);
   }

   private void line(class_4588 buf, Matrix4f m, class_243 cam, class_243 a, class_243 b, int color) {
      buf.method_22918(m, (float)(a.field_1352 - cam.field_1352), (float)(a.field_1351 - cam.field_1351), (float)(a.field_1350 - cam.field_1350))
         .method_39415(color);
      buf.method_22918(m, (float)(b.field_1352 - cam.field_1352), (float)(b.field_1351 - cam.field_1351), (float)(b.field_1350 - cam.field_1350))
         .method_39415(color);
   }

   private static final class Trace {
      final List<class_243> path = new ArrayList<>();
      class_243 landing;
      boolean landed;
      float progress;
      boolean friend;
      boolean seen;
   }
}
