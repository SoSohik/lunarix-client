package ru.mentions.cosmetics.anim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class GeoAnimationParser {
   private GeoAnimationParser() {
   }

   public static Map<String, GeoAnimation> parse(String json) {
      Map<String, GeoAnimation> out = new HashMap<>();
      if (json != null && !json.isEmpty()) {
         try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject animations = root.getAsJsonObject("animations");
            if (animations == null) {
               return out;
            } else {
               for (Entry<String, JsonElement> entry : animations.entrySet()) {
                  JsonObject animationJson = entry.getValue().getAsJsonObject();
                  boolean loop = animationJson.has("loop") && animationJson.get("loop").getAsBoolean();
                  float length = animationJson.has("animation_length") ? animationJson.get("animation_length").getAsFloat() : 1.0F;
                  Map<String, GeoAnimation.BoneTrack> bones = new HashMap<>();
                  JsonObject bonesJson = animationJson.getAsJsonObject("bones");
                  if (bonesJson != null) {
                     for (Entry<String, JsonElement> boneEntry : bonesJson.entrySet()) {
                        JsonObject boneJson = boneEntry.getValue().getAsJsonObject();
                        GeoAnimation.BoneTrack track = new GeoAnimation.BoneTrack(
                           keyframes(boneJson.get("rotation")), keyframes(boneJson.get("position")), keyframes(boneJson.get("scale"))
                        );
                        if (!track.isEmpty()) {
                           bones.put(boneEntry.getKey(), track);
                        }
                     }
                  }

                  out.put(entry.getKey(), new GeoAnimation(entry.getKey(), loop, length, bones));
               }

               return out;
            }
         } catch (Exception var15) {
            return new HashMap<>();
         }
      } else {
         return out;
      }
   }

   private static GeoAnimation.Keyframes keyframes(JsonElement element) {
      if (element == null) {
         return null;
      } else {
         float[] constant = vector(element);
         if (constant != null) {
            return new GeoAnimation.Keyframes(new float[]{0.0F}, new float[][]{constant});
         } else if (!element.isJsonObject()) {
            return null;
         } else {
            List<float[]> samples = new ArrayList<>();

            for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
               float time;
               try {
                  time = Float.parseFloat(entry.getKey());
               } catch (NumberFormatException var7) {
                  continue;
               }

               float[] value = vector(entry.getValue());
               if (value != null) {
                  samples.add(new float[]{time, value[0], value[1], value[2]});
               }
            }

            if (samples.isEmpty()) {
               return null;
            } else {
               samples.sort(Comparator.comparingDouble(samplex -> (double)samplex[0]));
               float[] times = new float[samples.size()];
               float[][] values = new float[samples.size()][3];

               for (int i = 0; i < samples.size(); i++) {
                  float[] sample = samples.get(i);
                  times[i] = sample[0];
                  values[i][0] = sample[1];
                  values[i][1] = sample[2];
                  values[i][2] = sample[3];
               }

               return new GeoAnimation.Keyframes(times, values);
            }
         }
      }
   }

   private static float[] vector(JsonElement element) {
      if (element == null) {
         return null;
      } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
         float value = element.getAsFloat();
         return new float[]{value, value, value};
      } else if (element.isJsonArray()) {
         JsonArray array = element.getAsJsonArray();
         return array.size() < 3 ? null : new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
      } else {
         if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("vector")) {
               return vector(object.get("vector"));
            }

            if (object.has("pre") && object.has("post")) {
               float[] pre = vector(object.get("pre"));
               float[] post = vector(object.get("post"));
               if (pre != null && post != null) {
                  return new float[]{(pre[0] + post[0]) * 0.5F, (pre[1] + post[1]) * 0.5F, (pre[2] + post[2]) * 0.5F};
               }

               return pre != null ? pre : post;
            }
         }

         return null;
      }
   }
}
