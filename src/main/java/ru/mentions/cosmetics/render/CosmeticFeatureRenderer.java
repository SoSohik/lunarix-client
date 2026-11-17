package ru.mentions.cosmetics.render;

import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_591;
import net.minecraft.class_630;
import org.joml.Quaternionf;
import ru.mentions.Client;
import ru.mentions.cosmetics.CosmeticEntry;
import ru.mentions.cosmetics.CosmeticType;
import ru.mentions.cosmetics.CosmeticsManager;
import ru.mentions.cosmetics.anim.CosmeticAnimator;
import ru.mentions.module.impl.render.Cosmetics;
import ru.mentions.module.impl.render.Wings;

public class CosmeticFeatureRenderer extends class_3887<class_10055, class_591> {
   private static final int FULL_BRIGHT = 15728880;
   private static final Quaternionf ROLL_180 = new Quaternionf().rotationZ((float) Math.PI);
   private static final CosmeticAnimator ANIMATOR = new CosmeticAnimator();

   public CosmeticFeatureRenderer(class_3883<class_10055, class_591> context) {
      super(context);
   }

   public void method_4199(class_4587 matrices, class_11659 queue, int light, class_10055 state, float limbAngle, float limbDistance) {
      Cosmetics module = Client.get().moduleManager().get(Cosmetics.class);
      if (module != null && module.isEnabled() && state != null && !state.field_53542 && !state.field_53333) {
         class_310 client = class_310.method_1551();
         if (client != null && client.field_1724 != null) {
            class_1657 player = client.field_1724;
            if (!module.audience.is("Только себе") || state.field_53528 == player.method_5628()) {
               CosmeticsManager manager = CosmeticsManager.get();
               manager.load();
               class_591 biped = (class_591)this.method_17165();
               int shade = module.unshaded.getValue() ? 15728880 : light;
               long now = System.currentTimeMillis();
               CosmeticAnimator.Pose pose = poseOf(state);
               boolean inWater = state.field_53458;
               boolean animated = module.animations.getValue();
               int owner = state.field_53528;
               Wings wingsModule = Client.get().moduleManager().get(Wings.class);
               boolean wingsModuleActive = wingsModule != null && wingsModule.isEnabled();

               for (CosmeticType type : CosmeticType.values()) {
                  if (!wingsModuleActive || type != CosmeticType.WINGS) {
                     CosmeticEntry cosmetic = manager.worn(type);
                     if (cosmetic != null && cosmetic.texture != null) {
                        boolean head = cosmetic.type.attachesToHead(cosmetic.position);
                        class_630 part = head ? biped.field_3398 : biped.field_3391;
                        if (part != null && part.field_3665) {
                           class_4587 local = new class_4587();
                           matrices.method_22903();

                           try {
                              part.method_22703(matrices);
                              local.method_34425(matrices.method_23760().method_23761());
                           } finally {
                              matrices.method_22909();
                           }

                           float globalScale = module.globalScale.getValue();
                           if (cosmetic.isCape()) {
                              local.method_22905(globalScale, globalScale, globalScale);
                           } else {
                              geometry(cosmetic, local, globalScale);
                           }

                           class_1921 layer = CosmeticRenderLayers.layer(cosmetic.texture);
                           if (cosmetic.isCape()) {
                              CapeRenderer.Shape shape = capeShape(module);
                              queue.method_73483(
                                 new class_4587(),
                                 layer,
                                 (entry, consumer) -> CapeRenderer.render(
                                       cosmetic, local.method_23760(), consumer, shade, -1, shape, animated ? now : 0L, limbDistance
                                    )
                              );
                           } else {
                              queue.method_73483(new class_4587(), layer, (entry, consumer) -> {
                                 if (animated) {
                                    ANIMATOR.apply(cosmetic, pose, inWater, now, owner);
                                 } else if (cosmetic.model != null) {
                                    cosmetic.model.resetPose();
                                 }

                                 GeoRenderer.render(cosmetic, local.method_23760(), consumer, shade, -1, animated ? now : 0L);
                              });
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static CosmeticAnimator.Pose poseOf(class_10055 state) {
      if (state.field_53411) {
         return CosmeticAnimator.Pose.ELYTRA;
      } else if (state.method_62613(class_4050.field_18079)) {
         return CosmeticAnimator.Pose.SWIMMING;
      } else if (state.field_53410) {
         return CosmeticAnimator.Pose.SNEAKING;
      } else {
         return state.field_53451 > 0.01F ? CosmeticAnimator.Pose.WALKING : CosmeticAnimator.Pose.IDLE;
      }
   }

   private static void geometry(CosmeticEntry cosmetic, class_4587 local, float globalScale) {
      local.method_22907(ROLL_180);
      local.method_46416(cosmetic.offsetX, cosmetic.offsetY, cosmetic.offsetZ);
      local.method_22907(
         new Quaternionf()
            .rotationZYX(
               (float)Math.toRadians((double)cosmetic.roll), (float)Math.toRadians((double)cosmetic.yaw), (float)Math.toRadians((double)cosmetic.pitch)
            )
      );
      float scale = cosmetic.scale * globalScale;
      local.method_22905(scale, scale, scale);
   }

   static CapeRenderer.Shape capeShape(Cosmetics module) {
      CapeRenderer.Shape shape = new CapeRenderer.Shape();
      if (module != null) {
         shape.width = module.capeWidth.getValue();
         shape.length = module.capeLength.getValue();
         shape.topY = module.capeTop.getValue();
         shape.backZ = module.capeDepth.getValue();
         shape.bend = module.capeBend.getValue();
         shape.wave = module.animations.getValue() ? module.capeWave.getValue() : 0.0F;
         shape.waveSpeed = module.capeWaveSpeed.getValue();
         shape.bottomRatio = module.capeHem.getValue();
      }

      return shape;
   }
}
