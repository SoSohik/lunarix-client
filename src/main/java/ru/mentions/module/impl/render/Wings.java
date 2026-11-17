package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
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
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Wings",
   desc = "Косметические крылья за спиной",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Wings extends Module {
   private static final int ANGELIC = 0;
   private static final int DRAGON = 1;
   private static final int BUTTERFLY = 2;
   private static final int PHOENIX = 3;
   private static final int CRYSTAL = 4;
   private static final int MECHANICAL = 5;
   private static final int FAIRY = 6;
   private static final int DEMON = 7;
   private static final Wings.WingPoint[][] SHAPES = new Wings.WingPoint[8][];
   private static final int[] RIBS_DEFAULT = new int[]{2, 4, 7, 9, 11};
   private static final int[] RIBS_SMALL = new int[]{2, 4, 6, 8};
   private static final float DEFAULT_SPREAD = 8.0F;
   private static final int DEFAULT_ALPHA = 220;
   public final ModeSetting wingType = new ModeSetting(
      this, "Тип крыльев", "Angelic", "Angelic", "Dragon", "Butterfly", "Phoenix", "Crystal", "Mechanical", "Fairy", "Demon"
   );
   public final ModeSetting targets = new ModeSetting(this, "Цели", "Себя", "Себя", "Друзья", "Игроки", "Все");
   public final BooleanSetting showInFirstPerson = new BooleanSetting(this, "Видеть от 1-го лица", false);
   public final SliderSetting wingScale = new SliderSetting(this, "Масштаб", 1.0F, 0.3F, 3.0F, 0.1F);
   public final SliderSetting height = new SliderSetting(this, "Высота", 1.38F, 0.8F, 2.5F, 0.05F);
   public final SliderSetting depthOffset = new SliderSetting(this, "Отступ", 0.15F, 0.0F, 0.5F, 0.01F);
   public final BooleanSetting flapping = new BooleanSetting(this, "Махание", true);
   public final SliderSetting flapStrength = new SliderSetting(this, "Сила махания", 30.0F, 5.0F, 60.0F, 1.0F).setVisible(() -> this.flapping.getValue());
   public final SliderSetting flapSpeed = new SliderSetting(this, "Скорость махания", 3.0F, 0.5F, 8.0F, 0.5F).setVisible(() -> this.flapping.getValue());
   public final BooleanSetting throughWalls = new BooleanSetting(this, "Сквозь стены", false);
   public final ColorSetting wingColor = new ColorSetting(this, "Цвет", -589504257);
   private final class_9799 allocator = new class_9799(131072);
   private static final RenderPipeline FILL_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "wings_fill_depth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final RenderPipeline FILL_NO_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "wings_fill_nodepth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final RenderPipeline LINE_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "wings_line_depth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline LINE_NO_DEPTH = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "wings_line_nodepth"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 FILL_DEPTH_LAYER = class_1921.method_75940(
      "wings_fill_depth", class_12247.method_75927(FILL_DEPTH).method_75929(4096).method_75938()
   );
   private static final class_1921 FILL_NO_DEPTH_LAYER = class_1921.method_75940(
      "wings_fill_nodepth", class_12247.method_75927(FILL_NO_DEPTH).method_75929(4096).method_75938()
   );
   private static final class_1921 LINE_DEPTH_LAYER = class_1921.method_75940(
      "wings_line_depth", class_12247.method_75927(LINE_DEPTH).method_75929(16384).method_75938()
   );
   private static final class_1921 LINE_NO_DEPTH_LAYER = class_1921.method_75940(
      "wings_line_nodepth", class_12247.method_75927(LINE_NO_DEPTH).method_75929(16384).method_75938()
   );
   private float selfBodyYaw;
   private boolean selfBodyYawInit;

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.allocator.method_60809();
      this.selfBodyYawInit = false;
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_243 cam = mc.field_1773.method_19418().method_71156();
         float tickDelta = e.getTickDelta();
         class_4598 immediate = class_4597.method_22991(this.allocator);
         boolean depth = !this.throughWalls.getValue();
         Wings.WingPoint[] shape = this.getCurrentShape();
         int[] ribIdx = shape.length <= 10 ? RIBS_SMALL : RIBS_DEFAULT;
         int rawColor = this.wingColor.getValue();
         int baseColor = alpha(rawColor) < 30 ? setAlpha(rawColor, 220) : rawColor;
         String t = this.targets.getValue();
         boolean renderSelf = t.equalsIgnoreCase("Себя") || t.equalsIgnoreCase("Все");
         boolean renderFriends = t.equalsIgnoreCase("Друзья") || t.equalsIgnoreCase("Все");
         boolean renderPlayers = t.equalsIgnoreCase("Игроки") || t.equalsIgnoreCase("Все");
         List<class_1657> targetsList = new ArrayList<>();
         boolean isFirstPerson = mc.field_1690.method_31044().method_31034();
         if (renderSelf && (!isFirstPerson || this.showInFirstPerson.getValue()) && mc.field_1724.method_5805() && !this.hasElytra(mc.field_1724)) {
            targetsList.add(mc.field_1724);
         }

         if (renderFriends || renderPlayers) {
            for (class_1657 p : mc.field_1687.method_18456()) {
               if (p != mc.field_1724 && p.method_5805() && !this.hasElytra(p)) {
                  boolean friend = Client.get() != null
                     && Client.get().friendManager() != null
                     && Client.get().friendManager().isFriend(p.method_5477().getString());
                  if (friend ? renderFriends : renderPlayers) {
                     targetsList.add(p);
                  }
               }
            }
         }

         class_4588 fill = immediate.method_73477(depth ? FILL_DEPTH_LAYER : FILL_NO_DEPTH_LAYER);

         for (class_1657 px : targetsList) {
            this.renderWingsOn(e.getMatrixStack(), fill, null, px, tickDelta, cam, shape, ribIdx, baseColor);
         }

         class_4588 line = immediate.method_73477(depth ? LINE_DEPTH_LAYER : LINE_NO_DEPTH_LAYER);

         for (class_1657 px : targetsList) {
            this.renderWingsOn(e.getMatrixStack(), null, line, px, tickDelta, cam, shape, ribIdx, baseColor);
         }

         immediate.method_22993();
         this.allocator.method_60809();
      }
   }

   private void renderWingsOn(
      class_4587 stack,
      class_4588 fill,
      class_4588 line,
      class_1657 player,
      float tickDelta,
      class_243 cam,
      Wings.WingPoint[] shape,
      int[] ribIdx,
      int baseColor
   ) {
      double x = class_3532.method_16436((double)tickDelta, player.field_6014, player.method_23317()) - cam.field_1352;
      double y = class_3532.method_16436((double)tickDelta, player.field_6036, player.method_23318()) - cam.field_1351;
      double z = class_3532.method_16436((double)tickDelta, player.field_5969, player.method_23321()) - cam.field_1350;
      float bodyYaw = this.resolveBodyYaw(player, tickDelta);
      float move = class_3532.method_15363((float)player.method_18798().method_1033() * 10.0F, 0.0F, 1.0F);
      Wings.WingPose pose = this.resolvePose(player, tickDelta);
      if (pose != null) {
         float flap = 0.0F;
         if (this.flapping.getValue()) {
            float speedMul = this.flapSpeed.getValue() / 3.0F;
            float ampMul = this.flapStrength.getValue() / 30.0F;
            flap = (float)Math.sin((double)(((float)player.field_6012 + tickDelta) * pose.flapSpeed * speedMul)) * pose.flapAmplitude * ampMul;
         }

         float open = (8.0F + flap + move * pose.motionSpreadBoost) * pose.openMultiplier;
         float ws = this.wingScale.getValue() * pose.scaleMultiplier;
         int glowColor = interpolateColor(baseColor, -1, 0.28F);
         int coreColor = interpolateColor(baseColor, -1, 0.55F);
         stack.method_22903();
         stack.method_22904(x, y, z);
         stack.method_22907(class_7833.field_40716.rotationDegrees(180.0F - bodyYaw));
         if (pose.preTranslateY != 0.0F || pose.preTranslateZ != 0.0F) {
            stack.method_22904(0.0, (double)pose.preTranslateY, (double)pose.preTranslateZ);
         }

         if (pose.pitchRotation != 0.0F) {
            stack.method_22907(class_7833.field_40714.rotationDegrees(pose.pitchRotation));
         }

         if (pose.rollRotation != 0.0F) {
            stack.method_22907(class_7833.field_40718.rotationDegrees(pose.rollRotation));
         }

         stack.method_22904(0.0, (double)pose.anchorY, (double)(pose.anchorZ + this.depthOffset.getValue()));
         stack.method_22905(ws, ws, ws);
         this.renderWingSide(stack, fill, line, -1.0F, open, baseColor, glowColor, coreColor, pose, shape, ribIdx);
         this.renderWingSide(stack, fill, line, 1.0F, open, baseColor, glowColor, coreColor, pose, shape, ribIdx);
         stack.method_22909();
      }
   }

   private void renderWingSide(
      class_4587 stack,
      class_4588 fill,
      class_4588 line,
      float side,
      float open,
      int baseColor,
      int glowColor,
      int coreColor,
      Wings.WingPose pose,
      Wings.WingPoint[] shape,
      int[] ribIdx
   ) {
      stack.method_22903();
      stack.method_22904((double)(side * pose.sideOffset), (double)pose.sideYOffset, (double)pose.sideZOffset);
      stack.method_22907(class_7833.field_40716.rotationDegrees(side * open));
      stack.method_22907(class_7833.field_40718.rotationDegrees(side * pose.sideRoll));
      stack.method_22907(class_7833.field_40714.rotationDegrees(pose.sidePitch));
      if (fill != null) {
         this.drawWingLayer(fill, stack, side, 1.22F, setAlpha(glowColor, 48), setAlpha(glowColor, 0), shape);
         this.drawWingLayer(fill, stack, side, 0.84F, setAlpha(coreColor, 57), setAlpha(coreColor, 0), shape);
         this.drawWingLayer(fill, stack, side, 1.0F, setAlpha(baseColor, 220), setAlpha(baseColor, 10), shape);
      }

      if (line != null) {
         this.drawWingOutline(line, stack, side, 1.0F, setAlpha(baseColor, 136), shape);
         this.drawWingRibs(line, stack, side, 0.96F, setAlpha(glowColor, 44), shape, ribIdx);
      }

      stack.method_22909();
   }

   private void drawWingLayer(class_4588 buf, class_4587 stack, float side, float scale, int rootColor, int edgeColor, Wings.WingPoint[] shape) {
      Matrix4f m = stack.method_23760().method_23761();

      for (int i = 0; i < shape.length; i++) {
         Wings.WingPoint cur = shape[i];
         Wings.WingPoint next = shape[(i + 1) % shape.length];
         buf.method_22918(m, 0.0F, 0.0F, 0.0F).method_39415(rootColor);
         buf.method_22918(m, side * cur.x * scale, cur.y * scale, 0.0F).method_39415(applyAlphaMul(edgeColor, cur.alphaMul));
         buf.method_22918(m, side * next.x * scale, next.y * scale, 0.0F).method_39415(applyAlphaMul(edgeColor, next.alphaMul));
      }
   }

   private void drawWingOutline(class_4588 buf, class_4587 stack, float side, float scale, int color, Wings.WingPoint[] shape) {
      Matrix4f m = stack.method_23760().method_23761();

      for (Wings.WingPoint p : shape) {
         buf.method_22918(m, side * p.x * scale, p.y * scale, 0.0F).method_39415(color);
      }

      buf.method_22918(m, side * shape[0].x * scale, shape[0].y * scale, 0.0F).method_39415(color);
   }

   private void drawWingRibs(class_4588 buf, class_4587 stack, float side, float scale, int color, Wings.WingPoint[] shape, int[] ribIdx) {
      Matrix4f m = stack.method_23760().method_23761();

      for (int idx : ribIdx) {
         if (idx < shape.length) {
            Wings.WingPoint p = shape[idx];
            buf.method_22918(m, 0.0F, 0.0F, 0.0F).method_39415(setAlpha(color, Math.max(8, (int)((float)alpha(color) * 0.75F))));
            buf.method_22918(m, side * p.x * scale, p.y * scale, 0.0F).method_39415(applyAlphaMul(color, p.alphaMul));
         }
      }
   }

   private Wings.WingPose resolvePose(class_1657 player, float tickDelta) {
      float pitch = class_3532.method_16439(tickDelta, player.field_5982, player.method_36455());
      if (player.method_6128()) {
         float flightProgress = 1.0F;
         float pitchRotation = flightProgress * (-90.0F - pitch);
         return new Wings.WingPose(0.34F, 0.46F, 0.0F, 0.0F, pitchRotation, 0.0F, 0.76F, 0.92F, 0.1F, 0.58F, 0.05F, 0.06F, -5.0F, -2.0F, 0.13F);
      } else if (player.method_5799()) {
         return null;
      } else {
         return player.method_5715()
            ? new Wings.WingPose(0.0F, 0.0F, 0.96F, 0.1F, 18.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.02F, -11.0F, -4.0F, 0.12F)
            : new Wings.WingPose(0.0F, 0.0F, this.height.getValue(), 0.1F, 0.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.02F, -11.0F, -4.0F, 0.12F);
      }
   }

   private boolean hasElytra(class_1657 p) {
      return p.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833;
   }

   private static float rotLerp(float delta, float start, float end) {
      return start + delta * class_3532.method_15393(end - start);
   }

   private float resolveBodyYaw(class_1657 player, float tickDelta) {
      float target = rotLerp(tickDelta, player.field_6259, player.field_6220);
      if (player != mc.field_1724) {
         return target;
      } else if (this.selfBodyYawInit && player.field_6012 >= 2) {
         this.selfBodyYaw = approachDegrees(this.selfBodyYaw, target, 14.0F);
         return this.selfBodyYaw;
      } else {
         this.selfBodyYaw = target;
         this.selfBodyYawInit = true;
         return this.selfBodyYaw;
      }
   }

   private static float approachDegrees(float current, float target, float maxDelta) {
      float delta = class_3532.method_15393(target - current);
      delta = class_3532.method_15363(delta, -maxDelta, maxDelta);
      return current + delta;
   }

   private static int alpha(int c) {
      return c >> 24 & 0xFF;
   }

   private static int setAlpha(int c, int a) {
      return (a & 0xFF) << 24 | c & 16777215;
   }

   private static int applyAlphaMul(int c, float m) {
      return setAlpha(c, Math.max(0, Math.min(255, (int)((float)alpha(c) * m))));
   }

   private static int interpolateColor(int c1, int c2, float t) {
      int r = (int)((float)(c1 >> 16 & 0xFF) + (float)((c2 >> 16 & 0xFF) - (c1 >> 16 & 0xFF)) * t);
      int g = (int)((float)(c1 >> 8 & 0xFF) + (float)((c2 >> 8 & 0xFF) - (c1 >> 8 & 0xFF)) * t);
      int b = (int)((float)(c1 & 0xFF) + (float)((c2 & 0xFF) - (c1 & 0xFF)) * t);
      int a = (int)((float)alpha(c1) + (float)(alpha(c2) - alpha(c1)) * t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private Wings.WingPoint[] getCurrentShape() {
      String type = this.wingType.getValue();
      if (type == null) {
         return SHAPES[0];
      } else if (type.equalsIgnoreCase("Dragon") || type.contains("Дракон")) {
         return SHAPES[1];
      } else if (type.equalsIgnoreCase("Butterfly") || type.contains("Бабоч")) {
         return SHAPES[2];
      } else if (type.equalsIgnoreCase("Phoenix") || type.contains("Феникс")) {
         return SHAPES[3];
      } else if (type.equalsIgnoreCase("Crystal") || type.contains("Кристал")) {
         return SHAPES[4];
      } else if (type.equalsIgnoreCase("Mechanical") || type.contains("Механич")) {
         return SHAPES[5];
      } else if (type.equalsIgnoreCase("Fairy") || type.contains("Фея")) {
         return SHAPES[6];
      } else {
         return !type.equalsIgnoreCase("Demon") && !type.contains("Демон") ? SHAPES[0] : SHAPES[7];
      }
   }

   @Override
   public void onDisable() {
      this.selfBodyYawInit = false;
      this.allocator.method_60809();
      super.onDisable();
   }

   static {
      SHAPES[0] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.08F, 0.1F, 0.88F),
         new Wings.WingPoint(0.28F, 0.42F, 0.85F),
         new Wings.WingPoint(0.62F, 0.98F, 0.75F),
         new Wings.WingPoint(0.96F, 0.5F, 0.6F),
         new Wings.WingPoint(1.28F, 0.65F, 0.45F),
         new Wings.WingPoint(1.38F, 0.12F, 0.35F),
         new Wings.WingPoint(1.15F, -0.22F, 0.3F),
         new Wings.WingPoint(1.28F, -0.72F, 0.22F),
         new Wings.WingPoint(0.92F, -0.52F, 0.2F),
         new Wings.WingPoint(0.85F, -1.05F, 0.14F),
         new Wings.WingPoint(0.56F, -0.8F, 0.16F),
         new Wings.WingPoint(0.32F, -1.22F, 0.12F),
         new Wings.WingPoint(0.1F, -0.58F, 0.18F)
      };
      SHAPES[1] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.1F, 0.15F, 0.95F),
         new Wings.WingPoint(0.25F, 0.55F, 0.88F),
         new Wings.WingPoint(0.6F, 1.15F, 0.75F),
         new Wings.WingPoint(0.95F, 0.85F, 0.62F),
         new Wings.WingPoint(1.55F, 1.25F, 0.48F),
         new Wings.WingPoint(1.7F, 0.45F, 0.4F),
         new Wings.WingPoint(1.45F, -0.15F, 0.32F),
         new Wings.WingPoint(1.25F, -0.75F, 0.25F),
         new Wings.WingPoint(0.8F, -0.45F, 0.22F),
         new Wings.WingPoint(0.55F, -0.95F, 0.18F),
         new Wings.WingPoint(0.22F, -0.65F, 0.15F),
         new Wings.WingPoint(0.06F, -0.35F, 0.2F)
      };
      SHAPES[2] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.1F, 0.15F, 0.95F),
         new Wings.WingPoint(0.35F, 0.65F, 0.9F),
         new Wings.WingPoint(0.75F, 1.1F, 0.8F),
         new Wings.WingPoint(1.2F, 0.95F, 0.7F),
         new Wings.WingPoint(1.3F, 0.45F, 0.6F),
         new Wings.WingPoint(0.95F, 0.1F, 0.52F),
         new Wings.WingPoint(0.65F, -0.05F, 0.45F),
         new Wings.WingPoint(1.05F, -0.35F, 0.35F),
         new Wings.WingPoint(1.0F, -0.85F, 0.25F),
         new Wings.WingPoint(0.6F, -1.05F, 0.18F),
         new Wings.WingPoint(0.25F, -0.75F, 0.22F),
         new Wings.WingPoint(0.08F, -0.3F, 0.3F)
      };
      SHAPES[3] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.12F, 0.16F, 0.95F),
         new Wings.WingPoint(0.32F, 0.58F, 0.88F),
         new Wings.WingPoint(0.7F, 1.25F, 0.78F),
         new Wings.WingPoint(1.1F, 0.95F, 0.65F),
         new Wings.WingPoint(1.5F, 1.05F, 0.5F),
         new Wings.WingPoint(1.65F, 0.35F, 0.38F),
         new Wings.WingPoint(1.4F, -0.2F, 0.32F),
         new Wings.WingPoint(1.55F, -0.75F, 0.24F),
         new Wings.WingPoint(1.3F, -1.15F, 0.18F),
         new Wings.WingPoint(0.95F, -0.85F, 0.16F),
         new Wings.WingPoint(0.65F, -1.25F, 0.14F),
         new Wings.WingPoint(0.32F, -0.9F, 0.12F),
         new Wings.WingPoint(0.09F, -0.45F, 0.16F)
      };
      SHAPES[4] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.18F, 0.12F, 0.9F),
         new Wings.WingPoint(0.5F, 0.5F, 0.8F),
         new Wings.WingPoint(0.9F, 0.85F, 0.68F),
         new Wings.WingPoint(1.35F, 0.55F, 0.55F),
         new Wings.WingPoint(1.1F, 0.15F, 0.48F),
         new Wings.WingPoint(1.45F, -0.25F, 0.38F),
         new Wings.WingPoint(1.15F, -0.65F, 0.32F),
         new Wings.WingPoint(0.8F, -0.4F, 0.28F),
         new Wings.WingPoint(0.55F, -0.85F, 0.22F),
         new Wings.WingPoint(0.25F, -0.55F, 0.24F),
         new Wings.WingPoint(0.09F, -0.2F, 0.32F)
      };
      SHAPES[5] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.1F, 0.1F, 0.95F),
         new Wings.WingPoint(0.28F, 0.38F, 0.88F),
         new Wings.WingPoint(0.62F, 0.58F, 0.78F),
         new Wings.WingPoint(1.05F, 0.52F, 0.65F),
         new Wings.WingPoint(1.42F, 0.38F, 0.52F),
         new Wings.WingPoint(1.35F, 0.0F, 0.45F),
         new Wings.WingPoint(1.58F, -0.22F, 0.36F),
         new Wings.WingPoint(1.15F, -0.42F, 0.3F),
         new Wings.WingPoint(0.78F, -0.28F, 0.28F),
         new Wings.WingPoint(0.55F, -0.65F, 0.22F),
         new Wings.WingPoint(0.2F, -0.42F, 0.24F),
         new Wings.WingPoint(0.06F, -0.15F, 0.3F)
      };
      SHAPES[6] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.12F, 0.15F, 0.92F),
         new Wings.WingPoint(0.35F, 0.52F, 0.86F),
         new Wings.WingPoint(0.58F, 0.92F, 0.72F),
         new Wings.WingPoint(0.82F, 0.98F, 0.62F),
         new Wings.WingPoint(0.88F, 0.62F, 0.52F),
         new Wings.WingPoint(0.68F, 0.22F, 0.45F),
         new Wings.WingPoint(0.52F, -0.12F, 0.38F),
         new Wings.WingPoint(0.42F, -0.52F, 0.3F),
         new Wings.WingPoint(0.24F, -0.65F, 0.24F),
         new Wings.WingPoint(0.1F, -0.35F, 0.28F)
      };
      SHAPES[7] = new Wings.WingPoint[]{
         new Wings.WingPoint(0.1F, 0.12F, 0.92F),
         new Wings.WingPoint(0.3F, 0.45F, 0.85F),
         new Wings.WingPoint(0.68F, 0.82F, 0.72F),
         new Wings.WingPoint(1.1F, 0.65F, 0.58F),
         new Wings.WingPoint(1.52F, 0.75F, 0.45F),
         new Wings.WingPoint(1.65F, 0.28F, 0.35F),
         new Wings.WingPoint(1.45F, -0.15F, 0.3F),
         new Wings.WingPoint(1.72F, -0.62F, 0.24F),
         new Wings.WingPoint(1.5F, -0.98F, 0.18F),
         new Wings.WingPoint(1.1F, -0.78F, 0.16F),
         new Wings.WingPoint(0.7F, -1.2F, 0.14F),
         new Wings.WingPoint(0.32F, -0.92F, 0.12F),
         new Wings.WingPoint(0.1F, -0.48F, 0.18F)
      };
   }

   private static final class WingPoint {
      final float x;
      final float y;
      final float alphaMul;

      WingPoint(float x, float y, float alphaMul) {
         this.x = x;
         this.y = y;
         this.alphaMul = alphaMul;
      }
   }

   private static final class WingPose {
      final float preTranslateY;
      final float preTranslateZ;
      final float anchorY;
      final float anchorZ;
      final float pitchRotation;
      final float rollRotation;
      final float openMultiplier;
      final float scaleMultiplier;
      final float motionSpreadBoost;
      final float flapAmplitude;
      final float sideOffset;
      final float sideYOffset;
      final float sideZOffset;
      final float sideRoll;
      final float sidePitch;
      final float flapSpeed;

      WingPose(
         float pty,
         float ptz,
         float ay,
         float az,
         float pr,
         float rr,
         float om,
         float sm,
         float msb,
         float fa,
         float so,
         float szo,
         float sr,
         float sp,
         float fs
      ) {
         this.preTranslateY = pty;
         this.preTranslateZ = ptz;
         this.anchorY = ay;
         this.anchorZ = az;
         this.pitchRotation = pr;
         this.rollRotation = rr;
         this.openMultiplier = om;
         this.scaleMultiplier = sm;
         this.motionSpreadBoost = msb;
         this.flapAmplitude = fa;
         this.sideOffset = so;
         this.sideYOffset = 0.0F;
         this.sideZOffset = szo;
         this.sideRoll = sr;
         this.sidePitch = sp;
         this.flapSpeed = fs;
      }
   }
}
