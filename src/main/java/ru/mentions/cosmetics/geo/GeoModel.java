package ru.mentions.cosmetics.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GeoModel {
   public final List<GeoBone> topLevel = new ArrayList<>();
   public final Map<String, GeoBone> byName = new HashMap<>();
   public int textureWidth = 64;
   public int textureHeight = 64;

   public void index() {
      this.byName.clear();

      for (GeoBone bone : this.topLevel) {
         this.indexRecursive(bone);
      }
   }

   private void indexRecursive(GeoBone bone) {
      this.byName.put(bone.name, bone);

      for (GeoBone child : bone.children) {
         this.indexRecursive(child);
      }
   }

   public void resetPose() {
      for (GeoBone bone : this.topLevel) {
         this.resetRecursive(bone);
      }
   }

   private void resetRecursive(GeoBone bone) {
      bone.captureRest();

      for (GeoBone child : bone.children) {
         this.resetRecursive(child);
      }
   }
}
