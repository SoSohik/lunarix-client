package ru.mentions.manager.rotation;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.mentions.manager.event_impl.EventMoveInput;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.impl.combat.AimBot;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.render.LightningTargetESP;
import ru.mentions.module.impl.render.TargetEsp;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.aura.GCDUtil;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.player.MoveUtil;

@Environment(EnvType.CLIENT)
public class RotationProcess extends Component {
   public static RotationProcess.RotationTask currentTask = RotationProcess.RotationTask.IDLE;
   public static float currentYawSpeed;
   public static float currentPitchSpeed;
   public static float currentYawReturnSpeed;
   public static float currentPitchReturnSpeed;
   public static int currentPriority;
   public static int currentTimeout;
   public static int idleTicks;
   public static Rotation targetRotation;
   private final class_9799 boxAllocator = new class_9799(262144);
   private float animationNurik = 0.0F;
   private long currentTimeSpirits = 0L;
   public class_1309 target = null;
   private String activeEspMode = null;
   public Animation alpha = new Animation();
   public Animation alpha_2 = new Animation();
   public static final RenderPipeline ROMB_ESP_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField60125()})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> ROMB_ESP = class_156.method_34866(texture -> {
      class_12247 setup = class_12247.method_75927(ROMB_ESP_PIPELINE).method_75934("Sampler0", texture).method_75937().method_75929(1536).method_75938();
      return ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940("wtex", setup);
   });
   private static final RenderPipeline RING_FILL_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField56860()})
         .withLocation(class_2960.method_60655("client", "ring_esp_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline RING_LINE_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField56860()})
         .withLocation(class_2960.method_60655("client", "ring_esp_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 RING_FILL_LAYER = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940(
      "ring_esp_fill", class_12247.method_75927(RING_FILL_PIPELINE).method_75929(65536).method_75938()
   );
   private static final class_1921 RING_LINE_LAYER = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940(
      "ring_esp_line", class_12247.method_75927(RING_LINE_PIPELINE).method_75929(16384).method_75938()
   );
   private final RotationProcess.JitterPreset[] jitterPresets = new RotationProcess.JitterPreset[]{new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)((Math.sin((double)t / 80.0) + Math.cos((double)t / 35.0) * 0.35) * 13.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)((Math.cos((double)t / 95.0) + Math.sin((double)t / 42.0) * 0.4) * 11.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 55.0 + Math.sin((double)t / 350.0)) * 15.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 65.0 + Math.cos((double)t / 280.0)) * 14.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)((Math.cos((double)t / 42.0) * 0.7 + Math.sin((double)t / 120.0)) * 17.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)((Math.sin((double)t / 58.0) * 0.5 + Math.cos((double)t / 140.0)) * 12.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 25.0) * (9.0 + Math.sin((double)t / 300.0) * 4.0));
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 48.0) * (13.0 + Math.cos((double)t / 250.0) * 3.0));
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)((Math.sin((double)t / 70.0) + Math.sin((double)t / 17.0) * 0.25) * 16.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)((Math.cos((double)t / 90.0) + Math.cos((double)t / 23.0) * 0.2) * 13.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)((Math.cos((double)t / 110.0) * 0.8 + Math.sin((double)t / 38.0) * 0.6) * 12.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)((Math.sin((double)t / 100.0) * 0.7 + Math.cos((double)t / 29.0) * 0.4) * 15.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 170.0 + 1.2) * 21.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)((Math.cos((double)t / 52.0) + Math.sin((double)t / 240.0)) * 10.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)((Math.cos((double)t / 36.0) * 0.45 + Math.sin((double)t / 145.0)) * 18.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)((Math.sin((double)t / 44.0) * 0.35 + Math.cos((double)t / 180.0)) * 14.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)(Math.sin((double)t / 60.0 + Math.cos((double)t / 180.0)) * 14.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)(Math.cos((double)t / 73.0 + Math.sin((double)t / 210.0)) * 17.0);
      }
   }, new RotationProcess.JitterPreset() {
      @Override
      public float getYaw(long t) {
         return (float)((Math.sin((double)t / 48.0) * 0.9 + Math.cos((double)t / 140.0) * 0.5) * 15.0);
      }

      @Override
      public float getPitch(long t) {
         return (float)((Math.cos((double)t / 84.0) * 0.8 + Math.sin((double)t / 32.0) * 0.3) * 12.0);
      }
   }};
   private final RotationProcess.SpeedPreset[] speedPresets = new RotationProcess.SpeedPreset[]{new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 40.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 12.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 55.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 15.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 30.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 10.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 48.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 18.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 60.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 14.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 35.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 16.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 50.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 13.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 44.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 17.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 58.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 11.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 32.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 19.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 46.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 12.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 53.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 15.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 37.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 18.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 42.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 13.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 57.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 16.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 34.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 11.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 49.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 20.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 62.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 14.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 39.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 17.0F;
      }
   }, new RotationProcess.SpeedPreset() {
      @Override
      public float getYawSpeed() {
         return 52.0F;
      }

      @Override
      public float getPitchSpeed() {
         return 12.0F;
      }
   }};
   private float currentSpeedYaw = 0.0F;
   private float currentSpeedPitch = 0.0F;
   float jitterIntensity = 0.0F;
   private long jitterSwitchTime = ThreadLocalRandom.current().nextLong(1000L, 2001L);
   private long nextJitterSwitch = System.currentTimeMillis() + this.jitterSwitchTime;
   private int currentJitterPreset = 0;
   private int nextJitterPreset = 1;
   private long speedSwitchTime = ThreadLocalRandom.current().nextLong(2000L, 4001L);
   private long nextSpeedSwitch = System.currentTimeMillis() + this.speedSwitchTime;
   private int currentSpeedPreset = 0;
   private int nextSpeedPreset = 1;
   private static final long JITTER_SWITCH_TIME = 1000L;
   private float lastYawJitter;
   private float lastPitchJitter;
   private static final long SPEED_SWITCH_TIME = 2500L;
   private float lastYawSpeed;
   private float lastPitchSpeed;
   int tick;

   public static boolean isRotating() {
      return !currentTask.equals(RotationProcess.RotationTask.IDLE);
   }

   private void resetRotation() {
      Rotation targetRotation = new Rotation(FreeLookUtil.freeYaw, FreeLookUtil.freePitch);
      if (ServerUtil.isHolyWorld()) {
         this.stopRotation();
      } else if (updateRotation(targetRotation, currentYawReturnSpeed, currentPitchReturnSpeed)) {
         this.stopRotation();
      }
   }

   public static void resetParentTimeout() {
      currentTimeout = 0;
      currentTask = RotationProcess.RotationTask.IDLE;
      currentPriority = 0;
      FreeLookUtil.setActive(false);
   }

   @EventHandler
   public void onEventMovement(EventMoveInput eventMoveInput) {
      if (currentTask.equals(RotationProcess.RotationTask.RESET)) {
         MoveUtil.fixMovement(eventMoveInput, mc.field_1724.method_36454(), mc.field_1773.method_19418().method_19330());
      }
   }

   @EventHandler
   public void onEvent(EventTick event) {
      if (currentTask.equals(RotationProcess.RotationTask.AIM) && idleTicks > currentTimeout) {
         currentTask = RotationProcess.RotationTask.RESET;
      }

      if (currentTask.equals(RotationProcess.RotationTask.RESET)) {
         if (!ServerUtil.isHolyWorld() && !ServerUtil.isCopyTime()) {
            this.resetRotation();
         } else {
            this.stopRotation();
         }
      }

      idleTicks++;
   }

   public static void update(
      Rotation target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation
   ) {
      if (currentPriority <= priority) {
         if (currentTask.equals(RotationProcess.RotationTask.IDLE) && !clientRotation) {
            FreeLookUtil.active = true;
         }

         currentYawSpeed = yawSpeed;
         currentPitchSpeed = pitchSpeed;
         currentYawReturnSpeed = yawReturnSpeed;
         currentPitchReturnSpeed = pitchReturnSpeed;
         currentTimeout = timeout;
         currentPriority = priority;
         currentTask = RotationProcess.RotationTask.AIM;
         targetRotation = target;
         updateRotation(target, yawSpeed, pitchSpeed);
      }
   }

   public static void update(Rotation targetRotation, float turnSpeed, float returnSpeed, int timeout, int priority) {
      update(targetRotation, turnSpeed, turnSpeed, returnSpeed, returnSpeed, timeout, priority, false);
   }

   static boolean updateRotation(Rotation targetRotation, float yawSpeed, float pitchSpeed) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         Rotation currentRotation = new Rotation(mc.field_1724);
         float pitchDelta = targetRotation.pitch - currentRotation.pitch;
         float yawDelta = class_3532.method_15393(targetRotation.yaw - currentRotation.yaw);
         float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
         float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);
         mc.field_1724
            .method_36456(
               mc.field_1724.field_6241 = mc.field_1724.field_6241 + GCDUtil.getSensitivity(class_3532.method_15363(yawDelta, -clampedYaw, clampedYaw))
            );
         mc.field_1724
            .method_36457(
               class_3532.method_15363(
                  mc.field_1724.method_36455() + GCDUtil.getSensitivity(class_3532.method_15363(pitchDelta, -clampedPitch, clampedPitch)), -90.0F, 90.0F
               )
            );
         idleTicks = 0;
         return new Rotation(mc.field_1724).getDelta(targetRotation) < 1.0F;
      }
   }

   public void stopRotation() {
      currentTask = RotationProcess.RotationTask.IDLE;
      currentPriority = 0;
      FreeLookUtil.setActive(false);
   }

   public static void releaseRotation() {
      if (!isRotating() && !FreeLookUtil.active) {
         return;
      }

      if (mc.field_1724 != null && FreeLookUtil.initialized) {
         mc.field_1724.method_36456(mc.field_1724.field_6241 = FreeLookUtil.freeYaw);
         mc.field_1724.method_36457(FreeLookUtil.freePitch);
      }

      currentTask = RotationProcess.RotationTask.IDLE;
      currentPriority = 0;
      idleTicks = 0;
      FreeLookUtil.setActive(false);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.boxAllocator.method_60809();
   }

   private void renderCorner(
      class_4587 matrices, class_4588 consumer, float x, float y, float rotation, float scale, int c1, int c2, int c3, int c4, float alpha
   ) {
      matrices.method_22903();
      matrices.method_46416(x, y, 0.0F);
      matrices.method_22907(class_7833.field_40718.rotationDegrees(rotation));
      matrices.method_22905(scale * 0.6F, scale * 0.6F, 1.0F);
      Matrix4f mat = matrices.method_23760().method_23761();
      drawGradientQuad(consumer, mat, c1, c2, c3, c4, (int)(255.0F * alpha));
      matrices.method_22909();
   }

   @EventHandler(
      priority = -250
   )
   public void onEventMovement(EventRender3D e) {
      AttackAura aura = AttackAura.get();
      AimBot aimBot = AimBot.get();
      this.alpha.update();
      class_1309 currentTarget = null;
      String curMode = null;
      if (aura != null && aura.isEnabled() && AttackAura.target != null) {
         currentTarget = AttackAura.target;
         curMode = aura.typeTargetESP.getValue();
      } else if (aimBot != null && aimBot.isEnabled() && AimBot.target != null) {
         currentTarget = AimBot.target;
         curMode = aimBot.typeTargetESP.getValue();
      }

      if (currentTarget != null) {
         this.target = currentTarget;
         this.activeEspMode = curMode;
      } else if (this.alpha.get() <= 0.01F) {
         this.target = null;
         this.activeEspMode = null;
      }

      String espMode = this.activeEspMode != null ? this.activeEspMode : (aura != null ? aura.typeTargetESP.getValue() : "Не отображать");
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.alpha.run(currentTarget != null ? 1.0 : 0.0, 0.2F, Easings.SINE_OUT);
         float alphaPC = this.alpha.get();
         class_4598 immediate = class_4597.method_22991(this.boxAllocator);
         if (alphaPC > 0.001F && this.target != null && (espMode.equals("Гроза") || espMode.equals("Гроза"))) {
            LightningTargetESP.getInstance().render(immediate, e.getMatrixStack(), mc.field_1773.method_19418(), e.getTickDelta(), this.target, alphaPC);
         }

         if (alphaPC > 0.001F && this.target != null && espMode.equals("Картинка")) {
            int hurtTicks = this.target.field_6235;
            float hurtPC = (float)Math.sin((double)hurtTicks * (Math.PI / 20));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPC, 0.12F, Easings.SINE_OUT);
            int redColor = ColorUtil.getColor(185, 80, 80, (int)(255.0F * alphaPC));
            int color = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColor, this.alpha_2.get());
            int color2 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColor, this.alpha_2.get());
            int color3 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColor, this.alpha_2.get());
            int color4 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(360), alphaPC), redColor, this.alpha_2.get());
            class_4587 matrices = e.getMatrixStack();
            class_4588 consumer = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/union.png")));
            class_243 lerpedPos = this.target.method_30950(e.getTickDelta());
            double x = lerpedPos.field_1352;
            double y = lerpedPos.field_1351;
            double z = lerpedPos.field_1350;
            class_243 cameraPos = mc.field_1773.method_19418().method_71156();
            matrices.method_22903();
            matrices.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351 + (double)(this.target.method_17682() / 1.75F), z - cameraPos.field_1350);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
            long currentTimeMillis = System.currentTimeMillis();
            float rotate = (float)MathUtil.clamps(0.0, 720.0, (Math.sin((double)currentTimeMillis / 1000.0) + 1.0) / 2.0 * 360.0 * 2.0);
            matrices.method_22907(class_7833.field_40718.rotationDegrees(rotate));
            Matrix4f bloomMatrix = matrices.method_23760().method_23761();
            float size = 0.7F;
            float offset = 0.3F * size;
            float hitAnim = this.alpha_2.get();
            long time = System.currentTimeMillis();
            float rotation = 0.0F;
            float sizeA = 0.1F;
            float xan = 0.2F - 0.2F * alphaPC;
            this.renderCorner(
               matrices, consumer, offset + sizeA * hitAnim + xan, offset + sizeA * hitAnim + xan, 135.0F, size, color, color2, color3, color4, alphaPC
            );
            this.renderCorner(
               matrices, consumer, -offset - sizeA * hitAnim - xan, offset + sizeA * hitAnim + xan, -135.0F, size, color, color2, color3, color4, alphaPC
            );
            this.renderCorner(
               matrices, consumer, -offset - sizeA * hitAnim - xan, -offset - sizeA * hitAnim - xan, -45.0F, size, color, color2, color3, color4, alphaPC
            );
            this.renderCorner(
               matrices, consumer, offset + sizeA * hitAnim + xan, -offset - sizeA * hitAnim - xan, 45.0F, size, color, color2, color3, color4, alphaPC
            );
            matrices.method_22909();
         }

         if (alphaPC > 0.001F && this.target != null && espMode.equals("Кольцо")) {
            int hurtTicks = this.target.field_6235;
            float hurtPC = (float)Math.sin((double)hurtTicks * (Math.PI / 10));
            int redColor = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            double duration = 1200.0;
            double elapsed = (double)System.currentTimeMillis() % duration;
            boolean side = elapsed > duration / 2.0;
            double raw = elapsed / (duration / 2.0);
            raw = side ? raw - 1.0 : 1.0 - raw;
            double progress = raw < 0.5 ? 2.0 * raw * raw : 1.0 - Math.pow(-2.0 * raw + 2.0, 2.0) / 2.0;
            float height2 = this.target.method_17682();
            double eased = (double)height2 / 1.7 * (progress > 0.5 ? 1.0 - progress : progress) * (double)(side ? -1 : 1) / 0.7;
            class_4587 matrices = e.getMatrixStack();
            class_243 lerpedPos = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos = mc.field_1773.method_19418().method_71156();
            matrices.method_22903();
            matrices.method_22904(
               lerpedPos.field_1352 - cameraPos.field_1352, lerpedPos.field_1351 - cameraPos.field_1351, lerpedPos.field_1350 - cameraPos.field_1350
            );
            Matrix4f matrix = matrices.method_23760().method_23761();
            float radius = this.target.method_17681() - 0.1F + 0.35F - 0.35F * alphaPC;
            float yBase = (float)((double)height2 * progress);
            float yTop = (float)((double)height2 * progress + eased);
            class_4588 fillBuf = immediate.method_73477(RING_FILL_LAYER);

            for (int seg = 0; seg < 360; seg++) {
               float a0 = (float)Math.toRadians((double)seg);
               float a1 = (float)Math.toRadians((double)(seg + 1));
               int c0 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(seg), alphaPC), redColor, hurtPC);
               int c1 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(seg), alphaPC), redColor, hurtPC);
               float x0 = (float)(Math.cos((double)a0) * (double)radius);
               float z0 = (float)(Math.sin((double)a0) * (double)radius);
               float x1 = (float)(Math.cos((double)a1) * (double)radius);
               float z1 = (float)(Math.sin((double)a1) * (double)radius);
               fillBuf.method_22918(matrix, x0, yBase, z0).method_39415(ColorUtil.replAlpha(c0, (int)(120.0F * alphaPC)));
               fillBuf.method_22918(matrix, x1, yBase, z1).method_39415(ColorUtil.replAlpha(c1, (int)(120.0F * alphaPC)));
               fillBuf.method_22918(matrix, x1, yTop, z1).method_39415(ColorUtil.replAlpha(c1, 0));
               fillBuf.method_22918(matrix, x0, yTop, z0).method_39415(ColorUtil.replAlpha(c0, 0));
            }

            class_4588 lineBuf = immediate.method_73477(RING_LINE_LAYER);

            for (int seg = 0; seg < 360; seg++) {
               float a0 = (float)Math.toRadians((double)seg);
               float a1 = (float)Math.toRadians((double)(seg + 1));
               int c = ColorUtil.multAlpha(ColorUtil.overCol(ColorUtil.multBright(ColorUtil.fade(1), 0.7F), redColor, hurtPC), alphaPC);
               lineBuf.method_22918(matrix, (float)(Math.cos((double)a0) * (double)radius), yBase, (float)(Math.sin((double)a0) * (double)radius))
                  .method_39415(ColorUtil.replAlpha(c, (int)(150.0F * alphaPC)));
               lineBuf.method_22918(matrix, (float)(Math.cos((double)a1) * (double)radius), yBase, (float)(Math.sin((double)a1) * (double)radius))
                  .method_39415(ColorUtil.replAlpha(c, (int)(150.0F * alphaPC)));
            }

            matrices.method_22909();
         }

         if (alphaPC > 0.001F && this.target != null && espMode.equals("Духи")) {
            long currentTime = System.currentTimeMillis();
            if (this.currentTimeSpirits == 0L) {
               this.currentTimeSpirits = currentTime;
            }

            long timeDiff;
            if ((timeDiff = currentTime - this.currentTimeSpirits) > 0L) {
               this.animationNurik += (float)(5L * timeDiff) / 700.0F;
            }

            this.currentTimeSpirits = currentTime;
            class_4587 matrices = e.getMatrixStack();
            class_243 lerpedPos = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos = mc.field_1773.method_19418().method_71156();
            double x = lerpedPos.field_1352 - cameraPos.field_1352;
            double y = lerpedPos.field_1351 - cameraPos.field_1351;
            double z = lerpedPos.field_1350 - cameraPos.field_1350;
            alphaPC = (float)this.alpha.getValue();
            this.alpha_2.update();
            int hurtTicks2 = this.target.field_6235;
            float hurtPC2 = (float)Math.sin((double)hurtTicks2 * (Math.PI / 20));
            this.alpha_2.run((double)hurtPC2, 0.1F, Easings.SINE_OUT);
            float atts = this.alpha_2.get();
            int fadeColor = ColorUtil.fade(1);
            int redColor2 = ColorUtil.getColor(200, 70, 70, (int)(255.0F * alphaPC));
            int baseColor = ColorUtil.overCol(ColorUtil.multAlpha(fadeColor, alphaPC), redColor2, atts);
            int n2 = 3;
            int n3 = 12;
            int n4 = 3 * n2;
            matrices.method_22903();
            class_4184 camera = mc.field_1773.method_19418();

            for (int i = 0; i < n4; i += n2) {
               for (int j = 0; j < n3; j++) {
                  float f2 = this.animationNurik + (float)j * 0.1F;
                  float f3 = 0.6F;
                  float f4 = 0.4F;
                  int n5 = (int)Math.pow((double)i, 2.0);
                  matrices.method_22903();
                  double particleX = x + (double)f3 * Math.sin((double)(f2 + (float)n5));
                  double particleY = y + (double)f4 + 0.3F * Math.sin((double)(this.animationNurik + (float)j * 0.2F)) + (double)(0.2F * (float)i);
                  double particleZ = z + (double)f3 * Math.cos((double)(f2 - (float)n5));
                  matrices.method_22904(particleX, particleY, particleZ);
                  float scale = (0.005F + (float)j / 2000.0F) * alphaPC;
                  matrices.method_22905(scale, scale, scale);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
                  Matrix4f matrix = matrices.method_23760().method_23761();
                  class_4588 consumer = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png")));
                  int n7 = -20;
                  int n8 = 35;
                  consumer.method_22918(matrix, (float)n7, (float)(n7 + n8), 0.0F)
                     .method_39415(baseColor)
                     .method_22913(0.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer.method_22918(matrix, (float)(n7 + n8), (float)(n7 + n8), 0.0F)
                     .method_39415(baseColor)
                     .method_22913(1.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer.method_22918(matrix, (float)(n7 + n8), (float)n7, 0.0F)
                     .method_39415(baseColor)
                     .method_22913(1.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer.method_22918(matrix, (float)n7, (float)n7, 0.0F)
                     .method_39415(baseColor)
                     .method_22913(0.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  int var254 = -50;
                  int var256 = 95;
                  consumer.method_22918(matrix, (float)var254, (float)(var254 + var256), 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * 0.1F))
                     .method_22913(0.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer.method_22918(matrix, (float)(var254 + var256), (float)(var254 + var256), 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * 0.1F))
                     .method_22913(1.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer.method_22918(matrix, (float)(var254 + var256), (float)var254, 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * 0.1F))
                     .method_22913(1.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer.method_22918(matrix, (float)var254, (float)var254, 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * 0.1F))
                     .method_22913(0.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  matrices.method_22909();
               }
            }

            matrices.method_22909();
         }

         if (alphaPC > 0.001F && this.target != null && espMode.equals("Кубики")) {
            int hurtTicks3 = this.target.field_6235;
            float hurtPC = (float)Math.sin((double)hurtTicks3 * (Math.PI / 10));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPC, 0.1F, Easings.SINE_OUT);
            int redColor3 = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int color = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(1), alphaPC), redColor3, this.alpha_2.get());
            class_4587 matrices = e.getMatrixStack();
            class_243 cameraPos = mc.field_1773.method_19418().method_71156();
            class_243 targetPos = this.target.method_30950(e.getTickDelta());
            long speed_f = 40L;
            long currentTimex = System.currentTimeMillis();
            if (this.currentTimeSpirits == 0L) {
               this.currentTimeSpirits = currentTimex;
            }

            long timeDiff;
            if ((timeDiff = currentTimex - this.currentTimeSpirits) > 0L) {
               this.animationNurik += (float)(5L * timeDiff) / (float)speed_f;
            }

            this.currentTimeSpirits = currentTimex;
            float time = this.animationNurik - 12.0F * this.alpha_2.get();
            int cound = 12;
            float width = this.target.method_17681() * 1.5F;
            float sizeFI = (1.0F - 0.3F * this.alpha_2.get()) * alphaPC;
            class_4184 camera = mc.field_1773.method_19418();

            for (int i = 0; i < 360; i += cound) {
               float val = 0.70000005F;
               float sin = (float)(Math.sin((double)((float)Math.toRadians((double)((float)i + time)))) * (double)width * (double)val);
               float cos = (float)(Math.cos((double)((float)Math.toRadians((double)((float)i + time)))) * (double)width * (double)val);
               double x = targetPos.field_1352 + (double)sin;
               double z = targetPos.field_1350 + (double)cos;
               double y = targetPos.field_1351 + (double)this.target.method_17682() * Math.abs(MathUtil.sin((double)i));
               matrices.method_22903();
               matrices.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351, z - cameraPos.field_1350);
               matrices.method_22907(camera.method_23767());
               float gs = 0.6F * sizeFI;
               matrices.method_22905(gs, gs, gs);
               drawGradientQuad(
                  immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_1.png"))),
                  matrices.method_23760().method_23761(),
                  ColorUtil.multAlpha(color, 0.3F),
                  ColorUtil.multAlpha(color, 0.3F),
                  ColorUtil.multAlpha(color, 0.3F),
                  ColorUtil.multAlpha(color, 0.3F),
                  (int)(alphaPC * 0.35F * 255.0F)
               );
               matrices.method_22909();
            }

            for (int var62 = 0; var62 < 360; var62 += cound) {
               float val = 0.70000005F;
               float sin = (float)(Math.sin((double)((float)Math.toRadians((double)((float)var62 + time)))) * (double)width * (double)val);
               float cos = (float)(Math.cos((double)((float)Math.toRadians((double)((float)var62 + time)))) * (double)width * (double)val);
               double x = targetPos.field_1352 + (double)sin;
               double z = targetPos.field_1350 + (double)cos;
               double y = targetPos.field_1351 + (double)this.target.method_17682() * Math.abs(MathUtil.sin((double)var62));
               class_243 cubePos = new class_243(x, y, z);
               Vector3f directionToTarget = new Vector3f(
                     (float)(targetPos.field_1352 - cubePos.field_1352),
                     (float)(targetPos.field_1351 - cubePos.field_1351),
                     (float)(targetPos.field_1350 - cubePos.field_1350)
                  )
                  .normalize();
               matrices.method_22903();
               matrices.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351, z - cameraPos.field_1350);
               matrices.method_22907(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), directionToTarget));
               Matrix4f matrix = matrices.method_23760().method_23761();
               float size = 0.06F * sizeFI;
               drawCubeFillTESP(immediate.method_73477(RING_FILL_LAYER), matrix, size, ColorUtil.replAlpha(color, (int)(alphaPC * 0.2F * 255.0F)));
               matrices.method_22909();
            }

            for (int var63 = 0; var63 < 360; var63 += cound) {
               float val = 0.70000005F;
               float sin = (float)(Math.sin((double)((float)Math.toRadians((double)((float)var63 + time)))) * (double)width * (double)val);
               float cos = (float)(Math.cos((double)((float)Math.toRadians((double)((float)var63 + time)))) * (double)width * (double)val);
               double x = targetPos.field_1352 + (double)sin;
               double z = targetPos.field_1350 + (double)cos;
               double y = targetPos.field_1351 + (double)this.target.method_17682() * Math.abs(MathUtil.sin((double)var63));
               class_243 cubePos = new class_243(x, y, z);
               Vector3f directionToTarget = new Vector3f(
                     (float)(targetPos.field_1352 - cubePos.field_1352),
                     (float)(targetPos.field_1351 - cubePos.field_1351),
                     (float)(targetPos.field_1350 - cubePos.field_1350)
                  )
                  .normalize();
               matrices.method_22903();
               matrices.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351, z - cameraPos.field_1350);
               matrices.method_22907(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), directionToTarget));
               Matrix4f matrix = matrices.method_23760().method_23761();
               float size = 0.06F * sizeFI;
               drawCubeOutlineTESP(immediate.method_73477(RING_LINE_LAYER), matrix, size, ColorUtil.replAlpha(color, (int)(alphaPC * 255.0F)));
               matrices.method_22909();
            }
         }

         if (espMode.equals("Куб")) {
            aura.renderTargetCubeFragments(e, immediate);
            if (alphaPC > 0.001F && this.target != null) {
               aura.renderTargetCube(e, this.target, alphaPC, immediate);
            }
         }

         if (alphaPC > 0.001F && this.target != null && espMode.equals("Бублик")) {
            int hurtTicksB = this.target.field_6235;
            float hurtPCB = (float)Math.sin((double)hurtTicksB * (Math.PI / 20));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPCB, 0.1F, Easings.SINE_OUT);
            int redColorB = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int colorB0 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColorB, this.alpha_2.get());
            int colorB1 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColorB, this.alpha_2.get());
            int colorB2 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColorB, this.alpha_2.get());
            int colorB3 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(360), alphaPC), redColorB, this.alpha_2.get());
            class_4587 matricesB = e.getMatrixStack();
            class_243 lerpedPosB = this.target.method_30950(e.getTickDelta());
            class_243 cameraPosB = mc.field_1773.method_19418().method_71156();
            double duration = 1400.0;
            double elapsed = (double)System.currentTimeMillis() % duration;
            boolean down = elapsed > duration / 2.0;
            double raw = elapsed / (duration / 2.0);
            raw = down ? raw - 1.0 : 1.0 - raw;
            double progress = raw < 0.5 ? 2.0 * raw * raw : 1.0 - Math.pow(-2.0 * raw + 2.0, 2.0) / 2.0;
            float height = this.target.method_17682();
            float yBase = 0.12F + (float)progress * Math.max(0.2F, height - 0.24F);
            float direction = down ? -1.0F : 1.0F;
            double spin = (double)System.currentTimeMillis() / 770.0000000000001;
            float radius = this.target.method_17681() - 0.15F;
            int count = (int)(80.0F * alphaPC);

            for (int layer = 2; layer >= 0; layer--) {
               float layerAlpha = alphaPC * (1.0F - (float)layer * 0.28F);
               float yLayer = yBase + direction * (float)layer * 0.075F;
               float radiusLayer = radius + (float)layer * 0.025F;

               for (int i2 = 0; i2 < count; i2++) {
                  float pc = (float)i2 / (float)count;
                  double angle = (double)(pc * (float) (Math.PI * 2)) + spin + (double)layer * 0.18;
                  double x3 = Math.cos(angle) * (double)radiusLayer;
                  double z3 = Math.sin(angle) * (double)radiusLayer;
                  float size = 0.2F + (float)layer * 0.018F;
                  int localAlpha = (int)((float)(layer == 0 ? 255 : 105) * layerAlpha);
                  if (layer == 0) {
                     matricesB.method_22903();
                     matricesB.method_22904(
                        lerpedPosB.field_1352 - cameraPosB.field_1352 + x3,
                        lerpedPosB.field_1351 - cameraPosB.field_1351 + (double)yLayer,
                        lerpedPosB.field_1350 - cameraPosB.field_1350 + z3
                     );
                     matricesB.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
                     matricesB.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
                     matricesB.method_22907(class_7833.field_40718.rotationDegrees((float)Math.toDegrees(-angle) + (float)(spin * 80.0)));
                     matricesB.method_22905(size * 1.25F, size * 1.25F, size);
                     drawGradientQuad(
                        immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png"))),
                        matricesB.method_23760().method_23761(),
                        colorB0,
                        colorB1,
                        colorB2,
                        colorB3,
                        localAlpha
                     );
                     matricesB.method_22909();
                  }
               }
            }
         }

         boolean legacyShapeEsp = espMode.equals("Цепи")
            || espMode.equals("Девочка-целочка")
            || espMode.equals("Души")
            || espMode.equals("Celestial")
            || espMode.equals("Кристаллы")
            || espMode.equals("Ромб")
            || espMode.equals("Пентограма")
            || espMode.equals("Атом")
            || espMode.equals("Карты")
            || espMode.equals("Мечи");
         if (alphaPC > 0.001F && this.target != null && legacyShapeEsp) {
            class_4587 matricesL = e.getMatrixStack();
            class_243 lerpedPosL = this.target.method_30950(e.getTickDelta());
            class_243 cameraPosL = mc.field_1773.method_19418().method_71156();
            class_4184 cameraL = mc.field_1773.method_19418();
            double txL = lerpedPosL.field_1352 - cameraPosL.field_1352;
            double tyL = lerpedPosL.field_1351 - cameraPosL.field_1351;
            double tzL = lerpedPosL.field_1350 - cameraPosL.field_1350;
            long nowL = System.currentTimeMillis();
            float radiusL = this.target.method_17681() * 1.15F;
            float heightL = this.target.method_17682();
            int shapeColor = ColorUtil.multAlpha(ColorUtil.fade(1), alphaPC);
            if (espMode.equals("Цепи") || espMode.equals("Девочка-целочка")) {
               TargetEsp.renderChainsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, 1.0F);
            } else if (espMode.equals("Души")) {
               TargetEsp.renderSoulsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, 1.0F);
            } else if (espMode.equals("Celestial")) {
               TargetEsp.renderCelestialESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, 1.0F);
            } else if (espMode.equals("Кристаллы")) {
               TargetEsp.renderCrystalsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, 1.0F);
            } else if (espMode.equals("Ромб")) {
               TargetEsp.renderDiamondESP(immediate, matricesL, cameraL, txL, tyL, tzL, heightL, shapeColor, alphaPC, nowL, 1.0F, 0.7F, 0);
            } else if (espMode.equals("Пентограма")) {
               TargetEsp.renderPentagramESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            } else if (espMode.equals("Атом")) {
               TargetEsp.renderAtomESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            } else if (espMode.equals("Карты")) {
               TargetEsp.renderCardsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            } else if (espMode.equals("Мечи")) {
               TargetEsp.renderSwordsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            }
         }

         boolean particleEsp = espMode.equals("Система")
            || espMode.equals("Ореол")
            || espMode.equals("Треугольник")
            || espMode.equals("Молния")
            || espMode.equals("Спираль")
            || espMode.equals("Орбита")
            || espMode.equals("Угли")
            || espMode.equals("Руны")
            || espMode.equals("Пульс");
         if (alphaPC > 0.001F && this.target != null && particleEsp) {
            int hurtTicks8 = this.target.field_6235;
            float hurtPC8 = (float)Math.sin((double)hurtTicks8 * (Math.PI / 20));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPC8, 0.1F, Easings.SINE_OUT);
            int redColor7 = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int sysColor = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(1), alphaPC), redColor7, this.alpha_2.get());
            class_4587 matrices8 = e.getMatrixStack();
            class_243 lerpedPos8 = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos8 = mc.field_1773.method_19418().method_71156();
            class_4184 camera8 = mc.field_1773.method_19418();
            double tx = lerpedPos8.field_1352 - cameraPos8.field_1352;
            double ty = lerpedPos8.field_1351 - cameraPos8.field_1351;
            double tz = lerpedPos8.field_1350 - cameraPos8.field_1350;
            int effect = 0;
            float radiusCfg = 0.7F;
            float psize = 0.17F;
            int density = 60;
            double speedCfg = 0.7;
            if (currentTarget == AttackAura.target && aura != null) {
               effect = aura.sysEffect.getIndex();
               radiusCfg = aura.sysRadius.getValue();
               psize = aura.sysSize.getValue();
               density = Math.round(aura.sysDensity.getValue());
               speedCfg = aura.sysSpeed.getValue().doubleValue();
            } else if (aimBot != null) {
               effect = aimBot.sysEffect.getIndex();
               radiusCfg = aimBot.sysRadius.getValue();
               psize = aimBot.sysSize.getValue();
               density = Math.round(aimBot.sysDensity.getValue());
               speedCfg = aimBot.sysSpeed.getValue().doubleValue();
            }

            if (espMode.equals("Ореол")) {
               effect = 0;
            } else if (espMode.equals("Треугольник")) {
               effect = 1;
            } else if (espMode.equals("Молния")) {
               effect = 2;
            } else if (espMode.equals("Спираль")) {
               effect = 3;
            } else if (espMode.equals("Орбита")) {
               effect = 4;
            } else if (espMode.equals("Угли")) {
               effect = 5;
            } else if (espMode.equals("Руны")) {
               effect = 6;
            } else if (espMode.equals("Пульс")) {
               effect = 7;
            }

            double sysTime = (double)System.currentTimeMillis() / 1000.0 * speedCfg;
            float radius = radiusCfg * this.target.method_17681() * 1.6F;
            class_4588 sysBuf = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png")));
            int alpha255 = (int)(255.0F * alphaPC);
            if (effect == 0) {
               for (int si = 0; si < density; si++) {
                  double angle = sysTime * 2.0 + (double)si * ((Math.PI * 2) / (double)density);
                  double bobY = Math.sin(sysTime + (double)si * 0.35) * 0.15;
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * (double)radius,
                     ty + 0.1 + bobY,
                     tz + Math.sin(angle) * (double)radius,
                     psize,
                     sysColor,
                     alpha255
                  );
               }
            } else if (effect == 1) {
               for (int v = 0; v < 3; v++) {
                  double baseAngle = sysTime * 1.6 + (double)v * (Math.PI * 2.0 / 3.0);
                  int trail = Math.max(1, density / 3);

                  for (int ti = 0; ti < trail; ti++) {
                     double angle = baseAngle - (double)ti * 0.08;
                     float fade = 1.0F - (float)ti / (float)trail;
                     double py = ty + (double)(this.target.method_17682() * 0.5F) + Math.sin(sysTime * 2.0 + (double)v * 2.0) * 0.2;
                     int a = (int)((float)alpha255 * fade);
                     this.drawSysPoint(
                        matrices8, camera8, sysBuf, tx + Math.cos(angle) * (double)radius, py, tz + Math.sin(angle) * (double)radius, psize * fade, sysColor, a
                     );
                  }
               }
            } else if (effect == 2) {
               double height = (double)this.target.method_17682() + 0.5;
               int segs = Math.max(6, density / 4);
               double px = 0.0;
               double pz = 0.0;

               for (int gi = 0; gi <= segs; gi++) {
                  double frac = (double)gi / (double)segs;
                  double jitter = (Math.sin(sysTime * 20.0 + (double)gi * 3.7) + Math.sin(sysTime * 13.0 + (double)gi * 1.9)) * 0.5;
                  px = px * 0.5 + jitter * 0.12 * (double)radius;
                  pz = pz * 0.5 + Math.cos(sysTime * 17.0 + (double)gi * 2.3) * 0.12 * (double)radius;
                  this.drawSysPoint(matrices8, camera8, sysBuf, tx + px, ty + frac * height, tz + pz, psize * 0.7F, sysColor, alpha255);
               }
            } else if (effect == 3) {
               double height = (double)this.target.method_17682() + 0.4;

               for (int si = 0; si < density; si++) {
                  double frac = ((double)si / (double)density + sysTime * 0.35) % 1.0;
                  double angle = frac * Math.PI * 2.0 * 2.0 + sysTime;
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * (double)radius * 0.8,
                     ty + frac * height,
                     tz + Math.sin(angle) * (double)radius * 0.8,
                     psize * 0.8F,
                     sysColor,
                     alpha255
                  );
               }
            } else if (effect == 4) {
               for (int si = 0; si < density; si++) {
                  double angle = sysTime * 1.8 + (double)si * ((Math.PI * 2) / (double)density);
                  double ox = Math.cos(angle) * (double)radius;
                  double oz = Math.sin(angle) * (double)radius;
                  double tilt = Math.sin(sysTime * 0.9) * 0.6;
                  double py = ty + (double)(this.target.method_17682() * 0.6F) + oz * tilt;
                  this.drawSysPoint(matrices8, camera8, sysBuf, tx + ox, py, tz + oz * Math.cos(tilt), psize, sysColor, alpha255);
               }
            } else if (effect == 5) {
               for (int si = 0; si < density; si++) {
                  double seed = (double)si * 0.61803398875;
                  double frac = (seed + sysTime * 0.4) % 1.0;
                  double angle = seed * Math.PI * 2.0 * 5.0 + sysTime * 0.5;
                  double r = (double)radius * (0.3 + 0.7 * frac);
                  int a = (int)((float)alpha255 * (1.0F - (float)frac));
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * r,
                     ty + frac * (double)this.target.method_17682() * 1.4,
                     tz + Math.sin(angle) * r,
                     psize * (1.0F - (float)frac * 0.6F),
                     sysColor,
                     a
                  );
               }
            } else if (effect == 6) {
               int runes = Math.min(8, Math.max(3, density / 15));

               for (int ri = 0; ri < runes; ri++) {
                  double angle = sysTime * 0.8 + (double)ri * ((Math.PI * 2) / (double)runes);
                  double py = ty + 0.05 + Math.abs(Math.sin(sysTime + (double)ri)) * 0.25;
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * (double)radius,
                     py,
                     tz + Math.sin(angle) * (double)radius,
                     psize * 1.6F,
                     sysColor,
                     alpha255
                  );
               }
            } else if (effect == 7) {
               int pulses = 3;

               for (int p = 0; p < pulses; p++) {
                  double frac = (sysTime * 0.7 + (double)p / (double)pulses) % 1.0;
                  double r = (double)radius * (0.2 + frac * 1.3);
                  int a = (int)((float)alpha255 * (1.0F - (float)frac));
                  int ringPts = Math.max(8, density / pulses);

                  for (int qi = 0; qi < ringPts; qi++) {
                     double angle = (double)qi * ((Math.PI * 2) / (double)ringPts);
                     this.drawSysPoint(matrices8, camera8, sysBuf, tx + Math.cos(angle) * r, ty + 0.08, tz + Math.sin(angle) * r, psize * 0.8F, sysColor, a);
                  }
               }
            }
         }

         immediate.method_22993();
      }
   }

   private static void drawCubeFillTESP(class_4588 buf, Matrix4f m, float s, int color) {
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
   }

   private static void drawCubeOutlineTESP(class_4588 buf, Matrix4f m, float s, int color) {
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
   }

   private static void drawGradientQuad(class_4588 buffer, Matrix4f matrix, int color, int color2, int color3, int color4, int alpha) {
      buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color, alpha))
         .method_22913(0.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color2, alpha))
         .method_22913(1.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color3, alpha))
         .method_22913(1.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color4, alpha))
         .method_22913(0.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
   }

   private void drawSysPoint(class_4587 matrices, class_4184 camera, class_4588 buffer, double x, double y, double z, float size, int color, int alpha) {
      if (alpha > 0) {
         matrices.method_22903();
         matrices.method_22904(x, y, z);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
         matrices.method_22905(size, size, size);
         Matrix4f m = matrices.method_23760().method_23761();
         buffer.method_22918(m, -0.5F, -0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(0.0F, 1.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buffer.method_22918(m, 0.5F, -0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(1.0F, 1.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buffer.method_22918(m, 0.5F, 0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(1.0F, 0.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buffer.method_22918(m, -0.5F, 0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(0.0F, 0.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         matrices.method_22909();
      }
   }

   @Environment(EnvType.CLIENT)
   private interface JitterPreset {
      float getYaw(long var1);

      float getPitch(long var1);
   }

   @Environment(EnvType.CLIENT)
   public static enum RotationTask {
      AIM,
      RESET,
      IDLE;
   }

   @Environment(EnvType.CLIENT)
   private interface SpeedPreset {
      float getYawSpeed();

      float getPitchSpeed();
   }
}
