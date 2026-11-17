package ru.mentions.cosmetics.anim;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import ru.mentions.cosmetics.CosmeticEntry;
import ru.mentions.cosmetics.geo.GeoBone;
import ru.mentions.cosmetics.geo.GeoModel;

public final class CosmeticAnimator {
   private static final String[][] CANDIDATES = new String[][]{
      {"idle", "main", "animation", "anim", "stand"},
      {"walking", "walk", "moving", "move"},
      {"sneaking", "sneak", "crouch", "sneak_walk"},
      {"swimming", "swimmings", "swim", "flying", "fly"},
      {"elytra", "glide", "gliding"}
   };
   private static final String[] VARIANTS = new String[]{"", "water", "sneak"};
   private final Map<CosmeticAnimator.Playback, Long> startedAt = new HashMap<>();
   private final Map<CosmeticAnimator.Playback, CosmeticAnimator.Pose> lastPose = new HashMap<>();
   private final Map<CosmeticAnimator.Playback, Boolean> lastWater = new HashMap<>();
   private final float[] scratch = new float[3];

   public void apply(CosmeticEntry entry, CosmeticAnimator.Pose pose, boolean inWater, long now) {
      this.apply(entry, pose, inWater, now, -1);
   }

   public void apply(CosmeticEntry entry, CosmeticAnimator.Pose pose, boolean inWater, long now, int owner) {
      GeoModel model = entry.model;
      if (model != null) {
         model.resetPose();
         if (!entry.animations.isEmpty()) {
            CosmeticAnimator.Playback key = new CosmeticAnimator.Playback(entry.index, owner);
            GeoAnimation animation = resolve(entry, pose, inWater);
            if (animation == null) {
               model.resetPose();
            } else {
               boolean waterChanged = !Boolean.valueOf(inWater).equals(this.lastWater.get(key));
               if (waterChanged || pose != this.lastPose.get(key)) {
                  this.startedAt.put(key, now);
                  this.lastPose.put(key, pose);
                  this.lastWater.put(key, inWater);
               }

               Long start = this.startedAt.get(key);
               float elapsed = (float)(now - (start == null ? now : start)) / 1000.0F;
               float time = animation.loop && animation.length > 0.0F ? elapsed % animation.length : Math.min(elapsed, animation.length);
               model.resetPose();

               for (Entry<String, GeoAnimation.BoneTrack> trackEntry : animation.bones.entrySet()) {
                  GeoBone bone = model.byName.get(trackEntry.getKey());
                  if (bone != null) {
                     GeoAnimation.BoneTrack track = trackEntry.getValue();
                     if (track.rotation != null) {
                        track.rotation.sample(time, this.scratch);
                        bone.rotX = bone.restRotX + (float)Math.toRadians((double)(-this.scratch[0]));
                        bone.rotY = bone.restRotY + (float)Math.toRadians((double)(-this.scratch[1]));
                        bone.rotZ = bone.restRotZ + (float)Math.toRadians((double)this.scratch[2]);
                     }

                     if (track.position != null) {
                        track.position.sample(time, this.scratch);
                        bone.posX = bone.restPosX + this.scratch[0];
                        bone.posY = bone.restPosY + this.scratch[1];
                        bone.posZ = bone.restPosZ + this.scratch[2];
                     }

                     if (track.scale != null) {
                        track.scale.sample(time, this.scratch);
                        bone.scaleX = bone.restScaleX * this.scratch[0];
                        bone.scaleY = bone.restScaleY * this.scratch[1];
                        bone.scaleZ = bone.restScaleZ * this.scratch[2];
                     }
                  }
               }
            }
         }
      }
   }

   public void forget(int cosmeticIndex) {
      this.startedAt.keySet().removeIf(key -> key.index == cosmeticIndex);
      this.lastPose.keySet().removeIf(key -> key.index == cosmeticIndex);
      this.lastWater.keySet().removeIf(key -> key.index == cosmeticIndex);
   }

   private static GeoAnimation resolve(CosmeticEntry entry, CosmeticAnimator.Pose pose, boolean inWater) {
      String variant = inWater ? "water" : "";
      String[] names = CANDIDATES[pose.ordinal()];

      for (String name : names) {
         GeoAnimation animation = entry.animations.get(name + "." + variant);
         if (animation == null && !variant.isEmpty()) {
            animation = entry.animations.get(name);
         }

         if (animation != null) {
            return animation;
         }
      }

      String token = names[0];

      for (Entry<String, GeoAnimation> entryAnim : entry.animations.entrySet()) {
         String key = entryAnim.getKey().toLowerCase(Locale.ROOT);
         if (key.equals(token) || key.equals(token + "." + variant) || key.endsWith("." + token)) {
            return entryAnim.getValue();
         }
      }

      for (String name : names) {
         GeoAnimation animationx = entry.animations.get(name);
         if (animationx != null) {
            return animationx;
         }
      }

      return entry.animations.isEmpty() ? null : entry.animations.values().iterator().next();
   }

   private static record Playback(int index, int owner) {
   }

   public static enum Pose {
      IDLE,
      WALKING,
      SNEAKING,
      SWIMMING,
      ELYTRA;
   }
}
