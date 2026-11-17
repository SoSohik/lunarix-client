package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.utils.math.ChatUtils;

@ModuleInfo(
   name = "Penguin Companion",
   desc = "Renders the gugu gaga penguin model near the player",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class PenguinCompanion extends Module {
   private static final Logger LOGGER = LoggerFactory.getLogger("client/PenguinCompanion");
   private static final class_2960 MODEL_ID = class_2960.method_60655("client", "model_new/companion_mesh.bin");
   private static final String TEXTURE_PREFIX = "model_new/textures/";
   public SliderSetting scale = new SliderSetting(this, "Scale", 2.45F, 0.35F, 4.0F, 0.05F);
   private final class_9799 allocator = new class_9799(1048576);
   private PenguinCompanion.NanalyMesh mesh;
   private boolean loadAttempted;
   private class_243 companionPos;
   private float companionYaw;
   private float walkCycle;
   private float walkAmount;
   private long lastFrameNanos;
   private static final Map<class_2960, class_1921> LAYERS = new HashMap<>();
   private static final RenderPipeline MODEL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("client", "nanaly_model"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27379)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );

   @Override
   protected void onEnable() {
      this.companionPos = null;
      this.walkCycle = 0.0F;
      this.walkAmount = 0.0F;
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         PenguinCompanion.NanalyMesh model = this.getMesh();
         if (model != null) {
            class_4587 matrices = e.getMatrixStack();
            class_243 cam = mc.field_1773.method_19418().method_71156();
            class_1309 followTarget = this.getFollowTarget();
            class_243 desiredPos = this.getDesiredPosition(followTarget, e.getTickDelta());
            this.updateCompanionPosition(desiredPos, followTarget);
            class_243 worldPos = this.companionPos;
            class_243 pos = worldPos.method_1020(cam);
            matrices.method_22903();
            matrices.method_22904(pos.field_1352, pos.field_1351, pos.field_1350);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F - this.companionYaw));
            matrices.method_22905(this.scale.getValue(), this.scale.getValue(), this.scale.getValue());
            Matrix4f matrix = matrices.method_23760().method_23761();
            class_4598 immediate = class_4597.method_22991(this.allocator);

            for (PenguinCompanion.MeshGroup group : model.groups) {
               class_4588 buffer = immediate.method_73477(layer(group.texture));

               for (int i = 0; i < group.vertices.length; i += 8) {
                  float sourceX = group.vertices[i];
                  float sourceDepth = group.vertices[i + 1];
                  float sourceY = group.vertices[i + 2];
                  float u = group.vertices[i + 3];
                  float v = group.vertices[i + 4];
                  float y = sourceY - model.minY;
                  float z = -(sourceDepth - model.centerDepth);
                  float[] folded = foldArms(model, group.texture, sourceX, sourceY, sourceX, y, z);
                  float[] animated = animateWalk(model, group.texture, sourceX, sourceY, folded[0], folded[1], folded[2], this.walkCycle, this.walkAmount);
                  buffer.method_22918(matrix, animated[0], animated[1], animated[2]).method_1336(255, 255, 255, 255).method_22913(u, v);
               }
            }

            immediate.method_22993();
            matrices.method_22909();
         }
      }
   }

   private class_1309 getFollowTarget() {
      AttackAura aura = AttackAura.get();
      return (class_1309)(aura != null && aura.isEnabled() && AttackAura.target != null && AttackAura.target.method_5805() ? AttackAura.target : mc.field_1724);
   }

   private class_243 getDesiredPosition(class_1309 followTarget, float tickDelta) {
      class_243 targetPos = followTarget.method_30950(tickDelta);
      if (followTarget == mc.field_1724) {
         float yaw = mc.field_1724.method_36454();
         double radians = Math.toRadians((double)yaw);
         class_243 look = new class_243(-Math.sin(radians), 0.0, Math.cos(radians));
         return targetPos.method_1019(look.method_1021(1.15));
      } else {
         class_243 awayFromPlayer = targetPos.method_1020(mc.field_1724.method_30950(tickDelta));
         awayFromPlayer = awayFromPlayer.method_37268() < 1.0E-4 ? new class_243(0.0, 0.0, 1.0) : awayFromPlayer.method_1029();
         return targetPos.method_1019(awayFromPlayer.method_1021(0.95));
      }
   }

   private void updateCompanionPosition(class_243 desiredPos, class_1309 lookTarget) {
      long now = System.nanoTime();
      float delta = this.lastFrameNanos == 0L ? 0.05F : class_3532.method_15363((float)(now - this.lastFrameNanos) / 1.0E9F, 0.0F, 0.08F);
      this.lastFrameNanos = now;
      class_243 previousPos = this.companionPos;
      if (this.companionPos != null && !(this.companionPos.method_1025(desiredPos) > 256.0)) {
         class_243 toDesired = desiredPos.method_1020(this.companionPos);
         double horizontalDistance = Math.sqrt(toDesired.field_1352 * toDesired.field_1352 + toDesired.field_1350 * toDesired.field_1350);
         if (horizontalDistance > 0.24) {
            double speed = lookTarget == mc.field_1724 ? 4.6 : 6.2;
            double step = Math.min(horizontalDistance, speed * (double)delta);
            this.companionPos = this.companionPos
               .method_1019(new class_243(toDesired.field_1352 / horizontalDistance * step, 0.0, toDesired.field_1350 / horizontalDistance * step));
         }

         this.companionPos = new class_243(this.companionPos.field_1352, desiredPos.field_1351, this.companionPos.field_1350);
      } else {
         this.companionPos = desiredPos;
      }

      double moved = 0.0;
      if (previousPos != null) {
         double dx = this.companionPos.field_1352 - previousPos.field_1352;
         double dz = this.companionPos.field_1350 - previousPos.field_1350;
         moved = Math.sqrt(dx * dx + dz * dz);
      }

      float targetWalk = delta <= 0.0F ? 0.0F : class_3532.method_15363((float)(moved / (double)delta) * 0.22F, 0.0F, 1.0F);
      this.walkAmount = class_3532.method_16439(0.2F, this.walkAmount, targetWalk);
      if (this.walkAmount < 0.03F) {
         this.walkAmount = 0.0F;
      }

      this.walkCycle += (float)moved * 9.0F;
      class_243 lookPos = lookTarget.method_73189().method_1031(0.0, (double)lookTarget.method_17682() * 0.45, 0.0);
      class_243 lookVector = lookPos.method_1020(this.companionPos);
      float targetYaw = (float)Math.toDegrees(Math.atan2(lookVector.field_1352, lookVector.field_1350));
      this.companionYaw = lerpAngle(this.companionYaw, targetYaw, 0.2F);
   }

   private static float[] foldArms(PenguinCompanion.NanalyMesh model, class_2960 texture, float sourceX, float sourceY, float x, float y, float z) {
      float height = (sourceY - model.minY) / Math.max(model.maxY - model.minY, 1.0E-4F);
      float absX = Math.abs(sourceX);
      String path = texture.method_12832();
      boolean cloth = path.contains("cloth");
      if (cloth && !(absX < 0.16F) && !(height < 0.22F) && !(height > 0.72F)) {
         float side = Math.signum(sourceX);
         float outer = smoothStep(0.16F, 0.42F, absX);
         float vertical = smoothBand(height, 0.22F, 0.34F, 0.58F, 0.72F);
         float influence = outer * vertical * 0.72F;
         float targetX = side * 0.15F;
         float targetZ = z + 0.08F;
         return new float[]{class_3532.method_16439(influence, x, targetX), y - 0.015F * influence, class_3532.method_16439(influence, z, targetZ)};
      } else {
         return new float[]{x, y, z};
      }
   }

   private static float[] animateWalk(
      PenguinCompanion.NanalyMesh model, class_2960 texture, float sourceX, float sourceY, float x, float y, float z, float cycle, float amount
   ) {
      if (amount <= 0.0F) {
         return new float[]{x, y, z};
      } else {
         float height = (sourceY - model.minY) / Math.max(model.maxY - model.minY, 1.0E-4F);
         float absX = Math.abs(sourceX);
         float side = sourceX >= 0.0F ? 1.0F : -1.0F;
         float phase = class_3532.method_15374((double)(cycle + (side > 0.0F ? 0.0F : (float) Math.PI)));
         float lift = Math.max(phase, 0.0F);
         String path = texture.method_12832();
         if (path.contains("cloth") && height < 0.2F && absX > 0.025F && absX < 0.22F) {
            float foot = smoothBand(height, 0.0F, 0.03F, 0.16F, 0.22F);
            return new float[]{x + side * 0.01F * lift * foot * amount, y + 0.045F * lift * foot * amount, z + phase * 0.07F * foot * amount};
         } else if (path.contains("cloth") && height >= 0.22F && height <= 0.62F && absX > 0.16F) {
            float paw = smoothBand(height, 0.22F, 0.34F, 0.54F, 0.66F) * smoothStep(0.16F, 0.38F, absX);
            return new float[]{x + side * 0.012F * phase * paw * amount, y + 0.01F * lift * paw * amount, z - phase * 0.035F * paw * amount};
         } else if (height > 0.18F && height < 0.78F && absX < 0.34F) {
            float body = smoothBand(height, 0.18F, 0.36F, 0.62F, 0.78F);
            return new float[]{x + side * 0.002F * phase * body * amount, y, z - Math.abs(phase) * 0.01F * body * amount};
         } else {
            return new float[]{x, y, z};
         }
      }
   }

   private static float lerpAngle(float from, float to, float delta) {
      float diff = class_3532.method_15393(to - from);
      return from + diff * delta;
   }

   private static float smoothStep(float edge0, float edge1, float value) {
      float t = class_3532.method_15363((value - edge0) / Math.max(edge1 - edge0, 1.0E-4F), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static float smoothBand(float value, float fadeInStart, float fadeInEnd, float fadeOutStart, float fadeOutEnd) {
      return smoothStep(fadeInStart, fadeInEnd, value) * (1.0F - smoothStep(fadeOutStart, fadeOutEnd, value));
   }

   private PenguinCompanion.NanalyMesh getMesh() {
      if (this.mesh == null && !this.loadAttempted) {
         this.loadAttempted = true;

         try {
            Optional resource = class_310.method_1551().method_1478().method_14486(MODEL_ID);
            if (resource.isEmpty()) {
               LOGGER.warn("Nanaly mesh not found: {}", MODEL_ID);
               if (mc.field_1724 != null) {
                  ChatUtils.addChatMessage("§cPenguin Companion: mesh не найден §7" + MODEL_ID);
               }

               return null;
            }

            try (InputStream in = ((class_3298)resource.get()).method_14482()) {
               this.mesh = PenguinCompanion.NanalyMesh.read(in.readAllBytes());
               LOGGER.info("Loaded Nanaly mesh with {} groups", this.mesh.groups.length);
               if (mc.field_1724 != null) {
                  ChatUtils.addChatMessage("§aPenguin Companion: mesh загружен §7(" + this.mesh.groups.length + " groups)");
               }
            }
         } catch (RuntimeException | IOException var7) {
            LOGGER.error("Failed to load Nanaly mesh", var7);
            if (mc.field_1724 != null) {
               ChatUtils.addChatMessage("§cPenguin Companion: ошибка загрузки mesh, смотри latest.log");
            }
         }

         return this.mesh;
      } else {
         return this.mesh;
      }
   }

   private static class_1921 layer(class_2960 texture) {
      return LAYERS.computeIfAbsent(
         texture,
         id -> class_1921.method_75940(
               "nanaly_model_" + id.method_12832().replace('/', '_').replace('.', '_'),
               class_12247.method_75927(MODEL_PIPELINE).method_75934("Sampler0", id).method_75937().method_75929(1048576).method_75938()
            )
      );
   }

   @Environment(EnvType.CLIENT)
   private static record MeshGroup(class_2960 texture, float[] vertices) {
   }

   @Environment(EnvType.CLIENT)
   private static class NanalyMesh {
      final PenguinCompanion.MeshGroup[] groups;
      final float minY;
      final float maxY;
      final float centerDepth;

      NanalyMesh(PenguinCompanion.MeshGroup[] groups, float minY, float maxY, float centerDepth) {
         this.groups = groups;
         this.minY = minY;
         this.maxY = maxY;
         this.centerDepth = centerDepth;
      }

      static PenguinCompanion.NanalyMesh read(byte[] bytes) {
         ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
         byte[] magic = new byte[5];
         buf.get(magic);
         if (!"NMSH1".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IllegalArgumentException("Bad Nanaly mesh magic");
         } else {
            int groupCount = buf.getInt();
            PenguinCompanion.MeshGroup[] groups = new PenguinCompanion.MeshGroup[groupCount];
            float minY = Float.POSITIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float minDepth = Float.POSITIVE_INFINITY;
            float maxDepth = Float.NEGATIVE_INFINITY;

            for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
               int textureNameLength = Short.toUnsignedInt(buf.getShort());
               byte[] textureNameBytes = new byte[textureNameLength];
               buf.get(textureNameBytes);
               String textureName = new String(textureNameBytes, StandardCharsets.UTF_8);
               int vertexCount = buf.getInt();
               float[] vertices = new float[vertexCount * 8];

               for (int i = 0; i < vertices.length; i++) {
                  vertices[i] = buf.getFloat();
               }

               for (int var17 = 0; var17 < vertices.length; var17 += 8) {
                  minDepth = Math.min(minDepth, vertices[var17 + 1]);
                  maxDepth = Math.max(maxDepth, vertices[var17 + 1]);
                  minY = Math.min(minY, vertices[var17 + 2]);
                  maxY = Math.max(maxY, vertices[var17 + 2]);
               }

               class_2960 texture = class_2960.method_60655("client", "model_new/textures/" + textureName);
               groups[groupIndex] = new PenguinCompanion.MeshGroup(texture, vertices);
            }

            return new PenguinCompanion.NanalyMesh(groups, minY, maxY, (minDepth + maxDepth) * 0.5F);
         }
      }
   }
}
