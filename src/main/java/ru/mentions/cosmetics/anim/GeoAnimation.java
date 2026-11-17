package ru.mentions.cosmetics.anim;

import java.util.Map;

public final class GeoAnimation {
   public final String name;
   public final boolean loop;
   public final float length;
   public final Map<String, GeoAnimation.BoneTrack> bones;

   public GeoAnimation(String name, boolean loop, float length, Map<String, GeoAnimation.BoneTrack> bones) {
      this.name = name;
      this.loop = loop;
      this.length = length;
      this.bones = bones;
   }

   public static final class BoneTrack {
      public final GeoAnimation.Keyframes rotation;
      public final GeoAnimation.Keyframes position;
      public final GeoAnimation.Keyframes scale;

      public BoneTrack(GeoAnimation.Keyframes rotation, GeoAnimation.Keyframes position, GeoAnimation.Keyframes scale) {
         this.rotation = rotation;
         this.position = position;
         this.scale = scale;
      }

      public boolean isEmpty() {
         return (this.rotation == null || this.rotation.isEmpty())
            && (this.position == null || this.position.isEmpty())
            && (this.scale == null || this.scale.isEmpty());
      }
   }

   public static final class Keyframes {
      private final float[] times;
      private final float[][] values;

      public Keyframes(float[] times, float[][] values) {
         this.times = times;
         this.values = values;
      }

      public boolean isEmpty() {
         return this.times.length == 0;
      }

      public void sample(float time, float[] out) {
         int count = this.times.length;
         if (count == 0) {
            out[0] = 0.0F;
            out[1] = 0.0F;
            out[2] = 0.0F;
         } else if (count != 1 && !(time <= this.times[0])) {
            if (time >= this.times[count - 1]) {
               System.arraycopy(this.values[count - 1], 0, out, 0, 3);
            } else {
               for (int i = 0; i < count - 1; i++) {
                  float from = this.times[i];
                  float to = this.times[i + 1];
                  if (!(time < from) && !(time > to)) {
                     float span = to - from;
                     float f = span <= 0.0F ? 0.0F : (time - from) / span;

                     for (int c = 0; c < 3; c++) {
                        out[c] = this.values[i][c] + f * (this.values[i + 1][c] - this.values[i][c]);
                     }

                     return;
                  }
               }

               System.arraycopy(this.values[count - 1], 0, out, 0, 3);
            }
         } else {
            System.arraycopy(this.values[0], 0, out, 0, 3);
         }
      }
   }
}
