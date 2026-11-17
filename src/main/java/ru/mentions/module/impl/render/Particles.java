package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_156;
import net.minecraft.class_1667;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2663;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5819;
import net.minecraft.class_745;
import net.minecraft.class_746;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_2902.class_2903;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.MotionEvent;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.preview.ModulePreview;
import ru.mentions.module.api.preview.PreviewContext;
import ru.mentions.module.api.preview.PreviewSettings;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.taskript.StopWatch;

@ModuleInfo(
   name = "Particles",
   desc = "Кастомные частицы вокруг игрока",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Particles extends Module implements ModulePreview {
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public BooleanSetting onAttack = new BooleanSetting(this, "При атаке", true);
   public SliderSetting onAtSizs = new SliderSetting(this, "Кол в атаке", 20.0F, 5.0F, 50.0F, 1.0F).setVisible(() -> this.onAttack.getValue());
   public BooleanSetting totem = new BooleanSetting(this, "При тотеме", true);
   public SliderSetting onTtSizs = new SliderSetting(this, "Кол при тотеме", 75.0F, 20.0F, 150.0F, 1.0F).setVisible(() -> this.totem.getValue());
   public BooleanSetting perka = new BooleanSetting(this, "При кидании", true);
   public SliderSetting onPerkSizs = new SliderSetting(this, "Кол при кидании", 3.0F, 1.0F, 8.0F, 1.0F).setVisible(() -> this.perka.getValue());
   public BooleanSetting worlds = new BooleanSetting(this, "В мире", true);
   public SliderSetting onworldsSizs = new SliderSetting(this, "Кол в мире", 10.0F, 2.0F, 60.0F, 1.0F).setVisible(() -> this.worlds.getValue());
   public BooleanSetting move = new BooleanSetting(this, "При движении", true);
   private final ModeSetting particleMode = new ModeSetting(this, "Тип частиц", "Client", "Circle", "Bloom", "Star", "Heart", "Snow", "Dollar", "Pumpkin");
   private final SliderSetting size = new SliderSetting(this, "Размер", 0.5F, 0.0F, 1.0F, 0.1F);
   public BooleanSetting glows = new BooleanSetting(this, "Свечения", true);
   private final SliderSetting glowsiz = new SliderSetting(this, "Сила свечения", 0.5F, 0.0F, 1.0F, 0.1F).setVisible(() -> this.glows.getValue());
   public BooleanSetting rotations = new BooleanSetting(this, "Ротация", true);
   private final PreviewSettings previewSettings = PreviewSettings.of(this, 4.0F, 0.0F, 2.0F);
   public final List<Particles.Particle> targetParticles = new ArrayList<>();
   public final List<Particles.Particle> totemParticles = new ArrayList<>();
   public final List<Particles.Particle> frameParticles = new ArrayList<>();
   public final List<Particles.Particle> worldParticles = new ArrayList<>();
   private long totemSpawnStartTime = 0L;
   private class_1297 totemTargetEntity = null;
   private int totemParticlesLeftToSpawn = 0;
   private int totalTotemParticlesToSpawn = 0;
   private long lastUpdateTime = System.nanoTime();
   private final class_9799 boxAllocator = new class_9799(4096);
   private static final Map<Particles.ParticleType, class_1921> RENDER_LAYER_CACHE = new ConcurrentHashMap<>();
   private static final RenderPipeline TEXTURED_QUADS_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("client", "pipeline/world/textured_quads"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final Function<class_2960, class_1921> ROMB_ESP = class_156.method_34866(texture -> {
      class_12247 setup = class_12247.method_75927(TEXTURED_QUADS_PIPELINE).method_75934("Sampler0", texture).method_75937().method_75929(1536).method_75938();
      return class_1921.method_75940("wtex", setup);
   });
   private static final Vector3f REUSABLE_NORMAL = new Vector3f(0.0F, 0.0F, 1.0F);

   @Override
   public void toggle() {
      super.toggle();
      this.clear();
   }

   @EventHandler
   public void onEvent(WorldLoadEvent event) {
      this.clear();
   }

   private void spawnParticle(List<Particles.Particle> particles, class_243 position, class_243 velocity, boolean isWorld) {
      float particleSize = 0.05F + this.size.getValue() * 0.2F;
      int color = ColorUtil.fade(particles.size() * 100);
      String var8 = this.particleMode.getValue();

      Particles.ParticleType type = switch (var8) {
         case "Heart" -> Particles.ParticleType.HEART;
         case "Star" -> Particles.ParticleType.STAR;
         case "Snow" -> Particles.ParticleType.SNOOW;
         case "Bloom" -> Particles.ParticleType.BLOOM;
         case "Circle" -> Particles.ParticleType.CIRCLE;
         case "Client" -> Particles.ParticleType.STAR_NEW;
         case "Dollar" -> Particles.ParticleType.DOLLAR;
         case "Pumpkin" -> Particles.ParticleType.PUMPKIN;
         default -> Particles.ParticleType.BLOOM;
      };
      particles.add(
         new Particles.Particle(
            type,
            position.method_1031(0.0, (double)particleSize, 0.0),
            velocity,
            particles.size(),
            (int)MathUtil.step(MathUtil.randomValue(0.0F, 360.0F), 15.0F),
            color,
            particleSize,
            1.1F,
            isWorld,
            false
         )
      );
   }

   public void spawnParticleTotem(List<Particles.Particle> particles, class_243 position, class_243 velocity) {
      float particleSize = 0.05F + this.size.getValue() * 0.2F;
      String colors = this.particleMode.getValue();

      Particles.ParticleType type = switch (colors) {
         case "Heart" -> Particles.ParticleType.HEART;
         case "Star" -> Particles.ParticleType.STAR;
         case "Snow" -> Particles.ParticleType.SNOOW;
         case "Circle" -> Particles.ParticleType.CIRCLE;
         case "Bloom" -> Particles.ParticleType.BLOOM;
         case "Client" -> Particles.ParticleType.STAR_NEW;
         case "Dollar" -> Particles.ParticleType.DOLLAR;
         case "Pumpkin" -> Particles.ParticleType.PUMPKIN;
         default -> Particles.ParticleType.BLOOM;
      };
      int[] colors = new int[]{
         ColorUtil.getColor(221, 218, 127), ColorUtil.getColor(127, 221, 144), ColorUtil.getColor(255, 215, 0), ColorUtil.getColor(50, 205, 50)
      };
      int col = colors[MathUtil.randomInt(0, colors.length - 1)];
      particles.add(
         new Particles.Particle(
            type,
            position.method_1031(0.0, (double)particleSize, 0.0),
            velocity,
            particles.size(),
            (int)MathUtil.step(MathUtil.randomValue(0.0F, 360.0F), 15.0F),
            col,
            particleSize,
            1.1F,
            false,
            true
         )
      );
   }

   @EventHandler
   public void onEvent(AttackEvent event) {
      if (this.onAttack.getValue()) {
         this.spawnAttackBurst(event.getTarget());
      }
   }

   public void spawnAttackBurst(class_1297 target) {
      if (target != null) {
         float motion = 1.1F;

         for (int i = 0; (float)i < this.onAtSizs.getValue(); i++) {
            this.spawnParticle(
               this.targetParticles,
               new class_243(
                  target.method_23317() + (double)MathUtil.randomValue(-0.035F, 0.035F),
                  target.method_23318() + (double)MathUtil.randomValue(0.1F, target.method_17682()),
                  target.method_23321() + (double)MathUtil.randomValue(-0.035F, 0.035F)
               ),
               new class_243(
                  (double)MathUtil.randomValue(-motion + 0.2F, motion - 0.2F),
                  (double)MathUtil.randomValue(-motion, motion / 2.0F),
                  (double)MathUtil.randomValue(-motion + 0.2F, motion - 0.2F)
               ),
               false
            );
         }
      }
   }

   public void spawnTotemBurst(class_1297 target, int count) {
      if (target != null) {
         for (int i = 0; i < count; i++) {
            double angle = MathUtil.randomValue(0.0, Math.PI * 2);
            double speedXZ = MathUtil.randomValue(2.5, 4.0) * 0.1F;
            this.spawnParticleTotem(
               this.totemParticles,
               new class_243(
                  target.method_23317() + MathUtil.randomValue(-0.2, 0.2),
                  target.method_23318() + (double)MathUtil.randomValue(0.1F, target.method_17682()),
                  target.method_23321() + MathUtil.randomValue(-0.2, 0.2)
               ),
               new class_243(Math.cos(angle) * speedXZ, MathUtil.randomValue(0.0, (double)(target.method_17682() / 4.0F)), Math.sin(angle) * speedXZ)
            );
         }
      }
   }

   public void spawnThrowBurst(class_1297 target) {
      if (target != null) {
         class_243 pos = target.method_73189().method_1031(0.0, (double)(target.method_17682() * 0.6F), 0.0);

         for (int i = 0; (float)i < this.onPerkSizs.getValue() * 8.0F; i++) {
            this.spawnParticle(
               this.frameParticles,
               pos.method_1031(MathUtil.randomValue(-0.1, 0.1), MathUtil.randomValue(-0.1, 0.1), MathUtil.randomValue(-0.1, 0.1)),
               new class_243(MathUtil.randomValue(-0.15, 0.15), MathUtil.randomValue(-0.15, 0.15), MathUtil.randomValue(-0.15, 0.15)),
               true
            );
         }
      }
   }

   @Override
   public PreviewSettings previewSettings() {
      return this.previewSettings;
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   @Override
   public void previewSpawn(PreviewContext ctx) {
      class_745 target = ctx.dummy();
      if (target != null) {
         ArrayList<Runnable> demos = new ArrayList<>(3);
         if (this.onAttack.getValue()) {
            demos.add(() -> this.lambda$previewSpawn$5(target));
         }

         if (this.totem.getValue()) {
            demos.add(() -> this.lambda$previewSpawn$6((class_1297)target));
         }

         if (this.perka.getValue()) {
            demos.add(() -> this.lambda$previewSpawn$7((class_1297)target));
         }

         if (demos.isEmpty()) {
            demos.add(() -> this.lambda$previewSpawn$8((class_1297)target));
         }

         demos.get(Math.floorMod(ctx.phase(), demos.size())).run();
      }
   }

   @Override
   public void previewStop() {
      this.clear();
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      if (e.getPacket() instanceof class_2663 packet) {
         if (packet.method_11470() == 35) {
            class_1297 entity = packet.method_11469(mc.field_1687);
            if (entity == null) {
               return;
            }

            if (this.totem.getValue()) {
               this.totemTargetEntity = entity;
               this.totemParticlesLeftToSpawn = this.totalTotemParticlesToSpawn = this.onTtSizs.getValue().intValue();
               this.totemSpawnStartTime = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   public void onEvent(EventTick e) {
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1687 != null) {
         if (this.perka.getValue()) {
            for (class_1297 entity : mc.field_1687.method_18112()) {
               class_1685 trident;
               if ((entity instanceof class_1684 || entity instanceof class_1667 || entity instanceof class_1685)
                  && (!(entity instanceof class_1685) || !(trident = (class_1685)entity).method_24828())) {
                  boolean isMoving = entity.field_6038 != entity.method_23317()
                     || entity.field_5971 != entity.method_23318()
                     || entity.field_5989 != entity.method_23321();
                  if (isMoving) {
                     class_243 pos = entity.method_73189();

                     for (int i = 0; (float)i < this.onPerkSizs.getValue(); i++) {
                        this.spawnParticle(
                           this.frameParticles,
                           new class_243(
                              pos.field_1352 + class_3532.method_15366(class_5819.method_43047(), -0.1, 0.1),
                              pos.field_1351 + class_3532.method_15366(class_5819.method_43047(), -0.1, 0.1),
                              pos.field_1350 + class_3532.method_15366(class_5819.method_43047(), -0.1, 0.1)
                           ),
                           new class_243(
                              class_3532.method_15366(class_5819.method_43047(), -0.15, 0.15),
                              class_3532.method_15366(class_5819.method_43047(), -0.15, 0.15),
                              class_3532.method_15366(class_5819.method_43047(), -0.15, 0.15)
                           ),
                           true
                        );
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onEvent(MotionEvent e) {
      if (this.move.getValue() && this.hasPlayerMoved()) {
         this.spawnParticle(
            this.frameParticles,
            new class_243(
               mc.field_1724.method_23317() + MathUtil.randomValue(-0.1, 0.1),
               mc.field_1724.method_23318() + (double)MathUtil.randomValue(0.0F, mc.field_1724.method_17682()),
               mc.field_1724.method_23321() + MathUtil.randomValue(-0.1, 0.1)
            ),
            new class_243(MathUtil.randomValue(-0.3, 0.3), MathUtil.randomValue(-0.03, 0.3), MathUtil.randomValue(-0.3, 0.3)),
            true
         );
      }

      if (this.worlds.getValue()) {
         if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
         }

         int r = 24;

         for (int i = 0; (float)i < this.onworldsSizs.getValue(); i++) {
            class_243 additional = mc.field_1724
               .method_73189()
               .method_1031((double)MathUtil.randomValue((float)(-r), (float)r), 0.0, (double)MathUtil.randomValue((float)(-r), (float)r));
            class_2338 topPos = mc.field_1687.method_8598(class_2903.field_13197, class_2338.method_49638(additional));
            double x = (double)((float)topPos.method_10263() + MathUtil.randomValue(-3.0F, 3.0F));
            double z = (double)((float)topPos.method_10260() + MathUtil.randomValue(-3.0F, 3.0F));
            double y = mc.field_1724.method_23318() + (double)MathUtil.randomValue(mc.field_1724.method_17682(), (float)r);
            class_243 spawnPos = new class_243(x, y, z);

            while (!mc.field_1687.method_22347(class_2338.method_49638(spawnPos)) && spawnPos.field_1351 < (double)mc.field_1687.method_31600()) {
               spawnPos = spawnPos.method_1031(0.0, 1.0, 0.0);
            }

            this.spawnParticle(
               this.worldParticles,
               spawnPos,
               new class_243(
                  mc.field_1724.method_18798().field_1352 + (double)MathUtil.randomValue(-0.6F, 0.6F),
                  MathUtil.randomValue(-0.01, 0.44),
                  mc.field_1724.method_18798().field_1350 + (double)MathUtil.randomValue(-0.6F, 0.6F)
               ),
               true
            );
         }
      }

      if (this.totemParticlesLeftToSpawn > 0 && this.totemTargetEntity != null) {
         if (!this.totemTargetEntity.method_5805() && mc.field_1687.method_8469(this.totemTargetEntity.method_5628()) == null) {
            this.totemParticlesLeftToSpawn = 0;
            this.totemTargetEntity = null;
         } else {
            long elapsed = System.currentTimeMillis() - this.totemSpawnStartTime;
            long totalDuration = 1500L;
            if (elapsed < 1500L) {
               int expectedSpawnedSoFar = (int)((double)elapsed / (double)totalDuration * (double)this.totalTotemParticlesToSpawn);
               int currentSpawned = this.totalTotemParticlesToSpawn - this.totemParticlesLeftToSpawn;
               int toSpawnThisTick = expectedSpawnedSoFar - currentSpawned;
               if (toSpawnThisTick > this.totemParticlesLeftToSpawn) {
                  toSpawnThisTick = this.totemParticlesLeftToSpawn;
               }

               for (int i = 0; i < toSpawnThisTick; i++) {
                  double angle = MathUtil.randomValue(0.0, Math.PI * 2);
                  double speedXZ = MathUtil.randomValue(2.5, 4.0) * 0.1F;
                  double motionX = Math.cos(angle) * speedXZ;
                  double motionZ = Math.sin(angle) * speedXZ;
                  double motionY = MathUtil.randomValue(0.0, (double)(this.totemTargetEntity.method_17682() / 4.0F));
                  this.spawnParticleTotem(
                     this.totemParticles,
                     new class_243(
                        this.totemTargetEntity.method_23317() + MathUtil.randomValue(-0.2, 0.2),
                        this.totemTargetEntity.method_23318() + (double)MathUtil.randomValue(0.1F, this.totemTargetEntity.method_17682()),
                        this.totemTargetEntity.method_23321() + MathUtil.randomValue(-0.2, 0.2)
                     ),
                     new class_243(motionX, motionY, motionZ)
                  );
                  this.totemParticlesLeftToSpawn--;
               }
            } else {
               for (int i = 0; i < this.totemParticlesLeftToSpawn; i++) {
                  double angle = MathUtil.randomValue(0.0, Math.PI * 2);
                  double speedXZ = MathUtil.randomValue(2.5, 4.0) * 0.1F;
                  double motionX = Math.cos(angle) * speedXZ;
                  double motionZ = Math.sin(angle) * speedXZ;
                  double motionY = MathUtil.randomValue(0.0, (double)(this.totemTargetEntity.method_17682() / 4.0F));
                  this.spawnParticleTotem(
                     this.totemParticles,
                     new class_243(
                        this.totemTargetEntity.method_23317() + MathUtil.randomValue(-0.2, 0.2),
                        this.totemTargetEntity.method_23318() + (double)MathUtil.randomValue(0.1F, this.totemTargetEntity.method_17682()),
                        this.totemTargetEntity.method_23321() + MathUtil.randomValue(-0.2, 0.2)
                     ),
                     new class_243(motionX, motionY, motionZ)
                  );
               }

               this.totemParticlesLeftToSpawn = 0;
               this.totemTargetEntity = null;
            }
         }
      }

      this.removeExpiredParticles(this.frameParticles, 3000L);
      this.removeExpiredParticles(this.totemParticles, 3500L);
      this.removeExpiredParticles(this.worldParticles, 3500L);
      this.removeExpiredParticles(this.targetParticles, 6000L);
   }

   private boolean hasPlayerMoved() {
      return mc.field_1724.field_6038 != mc.field_1724.method_23317()
         || mc.field_1724.field_5971 != mc.field_1724.method_23318()
         || mc.field_1724.field_5989 != mc.field_1724.method_23321();
   }

   private void removeExpiredParticles(List<Particles.Particle> particles, long lifespan) {
      particles.removeIf(particle -> particle.time().finished((double)lifespan));
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.boxAllocator.method_60809();
   }

   @EventHandler
   public void onEvent(EventRender3D event) {
      class_4587 matrix = event.getMatrixStack();
      long now = System.nanoTime();
      double deltaTime = (double)(now - this.lastUpdateTime) / 1.0E9;
      this.lastUpdateTime = now;
      matrix.method_22903();
      class_4598 immediate = class_4597.method_22991(this.boxAllocator);
      if (this.glows.getValue()) {
         this.renderParticlesGlow(matrix, immediate, this.targetParticles, 2000L, 2500L, deltaTime);
         this.renderParticlesGlow(matrix, immediate, this.totemParticles, 1000L, 1500L, deltaTime);
         this.renderParticlesGlow(matrix, immediate, this.worldParticles, 1000L, 1500L, deltaTime);
         this.renderParticlesGlow(matrix, immediate, this.frameParticles, 750L, 1000L, deltaTime);
      }

      this.renderParticles(matrix, immediate, this.targetParticles, 2000L, 2500L, deltaTime);
      this.renderParticles(matrix, immediate, this.totemParticles, 1000L, 1500L, deltaTime);
      this.renderParticles(matrix, immediate, this.worldParticles, 1000L, 1500L, deltaTime);
      this.renderParticles(matrix, immediate, this.frameParticles, 750L, 1000L, deltaTime);
      immediate.method_22993();
      matrix.method_22909();
   }

   private void renderParticles(
      class_4587 matrix, class_4598 immediate, List<Particles.Particle> particles, long fadeInTime, long fadeOutTime, double deltaTime
   ) {
      if (!particles.isEmpty()) {
         matrix.method_22903();

         for (Particles.Particle particle : particles) {
            particle.update(deltaTime);
            boolean notFinishedFadeIn = !particle.time().finished((double)fadeInTime);
            boolean finishedFadeOut = particle.time().finished((double)fadeOutTime);
            if (notFinishedFadeIn) {
               particle.animation().run(1.0, 0.5, Easings.LINEAR, true);
            } else if (finishedFadeOut) {
               particle.animation().run(0.0, 2.0, Easings.LINEAR, true);
            }

            if (particle.animation.isAlive()) {
               particle.animation.update();
            }

            float animValue;
            int alpha;
            if ((alpha = (int)((animValue = particle.animation.get()) * 255.0F)) > 0) {
               int color = ColorUtil.replAlpha(particle.color(), alpha);
               class_243 v = particle.position();
               this.renderParticle(
                  matrix, immediate, particle, (float)v.field_1352, (float)v.field_1351, (float)v.field_1350, particle.size * animValue, color, alpha
               );
            }
         }

         matrix.method_22909();
      }
   }

   private void renderParticle(class_4587 matrix, class_4598 immediate, Particles.Particle particle, float x, float y, float z, float pos, int color, int alpha) {
      matrix.method_22903();
      setupOrientationMatrix(matrix, x, y, z);
      matrix.method_22907(mc.field_1773.method_19418().method_23767());
      if (this.rotations.getValue()) {
         matrix.method_22907(class_7833.field_40718.rotationDegrees(particle.rotate()));
      }

      class_4665 entry = matrix.method_23760();
      Matrix4f matrix4f = entry.method_23761();
      Matrix3f normalMatrix = entry.method_23762();
      class_4588 buffer = immediate.method_73477(ROMB_ESP.apply(particle.type().texture()));
      this.drawTexturedQuad(buffer, matrix4f, normalMatrix, -pos, -pos, pos * 2.0F, pos * 2.0F, color, alpha);
      if (particle.type == Particles.ParticleType.BLOOM && this.glows.getValue()) {
         this.drawTexturedQuad(buffer, matrix4f, normalMatrix, -pos / 2.0F, -pos / 2.0F, pos, pos, color, alpha);
      }

      matrix.method_22909();
   }

   private void renderParticlesGlow(
      class_4587 matrix, class_4598 immediate, List<Particles.Particle> particles, long fadeInTime, long fadeOutTime, double deltaTime
   ) {
      if (!particles.isEmpty()) {
         matrix.method_22903();

         for (Particles.Particle particle : particles) {
            float animValue = particle.animation.get();
            int alpha = (int)(animValue * 255.0F * this.glowsiz.getValue());
            if (alpha > 0) {
               int color = ColorUtil.replAlpha(particle.color(), alpha);
               class_243 v = particle.position();
               this.renderParticle2(
                  matrix, immediate, particle, (float)v.field_1352, (float)v.field_1351, (float)v.field_1350, particle.size * animValue, color, alpha
               );
            }
         }

         matrix.method_22909();
      }
   }

   private void renderParticle2(
      class_4587 matrix, class_4598 immediate, Particles.Particle particle, float x, float y, float z, float pos, int color, int alpha
   ) {
      matrix.method_22903();
      setupOrientationMatrix(matrix, x, y, z);
      matrix.method_22907(mc.field_1773.method_19418().method_23767());
      if (this.rotations.getValue()) {
         matrix.method_22907(class_7833.field_40718.rotationDegrees(particle.rotate()));
      }

      class_4665 entry = matrix.method_23760();
      Matrix4f matrix4f = entry.method_23761();
      Matrix3f normalMatrix = entry.method_23762();
      class_4588 buffer = immediate.method_73477(ROMB_ESP.apply(Particles.ParticleType.BLOOM.texture));
      this.drawTexturedQuad(buffer, matrix4f, normalMatrix, -pos * 3.0F, -pos * 3.0F, pos * 6.0F, pos * 6.0F, color, alpha);
      matrix.method_22909();
   }

   private void drawTexturedQuad(class_4588 buffer, Matrix4f matrix, Matrix3f normalMatrix, float x, float y, float width, float height, int color, int alpha) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      REUSABLE_NORMAL.set(0.0F, 0.0F, 1.0F);
      normalMatrix.transform(REUSABLE_NORMAL);
      REUSABLE_NORMAL.normalize();
      float x2 = x + width;
      float y2 = y + height;
      buffer.method_22918(matrix, x, y, 0.0F)
         .method_1336(r, g, b, alpha)
         .method_22913(0.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
      buffer.method_22918(matrix, x2, y, 0.0F)
         .method_1336(r, g, b, alpha)
         .method_22913(1.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
      buffer.method_22918(matrix, x2, y2, 0.0F)
         .method_1336(r, g, b, alpha)
         .method_22913(1.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
      buffer.method_22918(matrix, x, y2, 0.0F)
         .method_1336(r, g, b, alpha)
         .method_22913(0.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
   }

   public static void setupOrientationMatrix(class_4587 matrix, float x, float y, float z) {
      setupOrientationMatrix(matrix, (double)x, (double)y, (double)z);
   }

   public static void setupOrientationMatrix(class_4587 matrix, double x, double y, double z) {
      class_243 renderPos = mc.method_1561().field_4686.method_71156();
      matrix.method_22904(x - renderPos.field_1352, y - renderPos.field_1351, z - renderPos.field_1350);
   }

   private void clear() {
      this.targetParticles.clear();
      this.frameParticles.clear();
      this.totemParticles.clear();
      this.worldParticles.clear();
      this.totemParticlesLeftToSpawn = 0;
      this.totalTotemParticlesToSpawn = 0;
      this.totemTargetEntity = null;
      this.totemSpawnStartTime = 0L;
   }

   public static boolean isBlockSolid(double x, double y, double z) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2338 pos = class_2338.method_49637(x, y, z);
         return mc.field_1687.method_8320(pos).method_26234(mc.field_1687, pos);
      }
   }

   private void lambda$previewSpawn$8(class_1297 target) {
      this.spawnAttackBurst(target);
   }

   private void lambda$previewSpawn$7(class_1297 target) {
      this.spawnThrowBurst(target);
   }

   private void lambda$previewSpawn$6(class_1297 target) {
      this.spawnTotemBurst(target, this.onTtSizs.getValue().intValue());
   }

   private void lambda$previewSpawn$5(class_1297 target) {
      this.spawnAttackBurst(target);
   }

   @Environment(EnvType.CLIENT)
   public static class Particle {
      private final Particles.ParticleType type;
      private class_243 position;
      private class_243 velocity;
      private final int index;
      private final int color;
      private final float size;
      private static final double BASE_VELOCITY = 0.05;
      private final double speedMultiplier;
      private final boolean isWorldParticle;
      private final boolean isTotem;
      private final StopWatch time = new StopWatch();
      private final Animation animation = new Animation();
      private float rotate = 0.0F;

      public Particle(
         Particles.ParticleType type,
         class_243 position,
         class_243 velocity,
         int index,
         int rotate,
         int color,
         float size,
         double speedMultiplier,
         boolean isWorldParticle,
         boolean isTotem
      ) {
         this.type = type;
         this.position = position;
         this.velocity = isTotem ? velocity.method_1021(0.08) : velocity.method_1021(0.05);
         this.index = index;
         this.color = color;
         this.rotate = (float)rotate;
         this.size = size;
         this.speedMultiplier = speedMultiplier;
         this.isWorldParticle = isWorldParticle;
         this.isTotem = isTotem;
         this.time.reset();
      }

      public void update(double deltaTime) {
         double ticks = deltaTime * 120.0;
         if (!(ticks <= 0.0)) {
            double bounciness = 0.85;
            double gravity = this.isWorldParticle ? 1.0E-4 : 5.0E-4;
            double nextX = this.position.field_1352 + this.velocity.field_1352 * ticks;
            double nextY = this.position.field_1351 + this.velocity.field_1351 * ticks;
            double nextZ = this.position.field_1350 + this.velocity.field_1350 * ticks;
            if (Particles.isBlockSolid(nextX, this.position.field_1351, this.position.field_1350)) {
               this.velocity = new class_243(-this.velocity.field_1352 * bounciness, this.velocity.field_1351, this.velocity.field_1350);
            } else {
               this.position = new class_243(nextX, this.position.field_1351, this.position.field_1350);
            }

            if (Particles.isBlockSolid(this.position.field_1352, nextY, this.position.field_1350)) {
               this.velocity = new class_243(this.velocity.field_1352, -this.velocity.field_1351 * bounciness, this.velocity.field_1350);
            } else {
               this.position = new class_243(this.position.field_1352, nextY, this.position.field_1350);
            }

            if (Particles.isBlockSolid(this.position.field_1352, this.position.field_1351, nextZ)) {
               this.velocity = new class_243(this.velocity.field_1352, this.velocity.field_1351, -this.velocity.field_1350 * bounciness);
            } else {
               this.position = new class_243(this.position.field_1352, this.position.field_1351, nextZ);
            }

            this.velocity = this.velocity.method_1023(0.0, gravity * ticks, 0.0);
            this.rotate = (float)((double)this.rotate + 2.0 * ticks);
         }
      }

      @Generated
      public Particles.ParticleType type() {
         return this.type;
      }

      @Generated
      public class_243 position() {
         return this.position;
      }

      @Generated
      public class_243 velocity() {
         return this.velocity;
      }

      @Generated
      public int index() {
         return this.index;
      }

      @Generated
      public int color() {
         return this.color;
      }

      @Generated
      public float size() {
         return this.size;
      }

      @Generated
      public double speedMultiplier() {
         return this.speedMultiplier;
      }

      @Generated
      public boolean isWorldParticle() {
         return this.isWorldParticle;
      }

      @Generated
      public boolean isTotem() {
         return this.isTotem;
      }

      @Generated
      public StopWatch time() {
         return this.time;
      }

      @Generated
      public Animation animation() {
         return this.animation;
      }

      @Generated
      public float rotate() {
         return this.rotate;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum ParticleType {
      HEART("heart", true),
      STAR("star", true),
      BLOOM("glow", true),
      CIRCLE("circle", true),
      STAR_NEW("client", true),
      SNOOW("snowflake", true),
      DOLLAR("dollar", true),
      PUMPKIN("pumpkin", true);

      private final class_2960 texture;
      private final boolean rotatable;

      private ParticleType(String name, boolean rotatable) {
         this.texture = class_2960.method_60655("client", "textures/particles/" + name + ".png");
         this.rotatable = rotatable;
      }

      @Generated
      public class_2960 texture() {
         return this.texture;
      }

      @Generated
      public boolean rotatable() {
         return this.rotatable;
      }
   }
}
