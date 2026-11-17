package ru.mentions.cosmetics.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;

public final class CosmeticRenderLayers {
   private static final Map<class_2960, class_1921> LAYERS = new HashMap<>();

   private CosmeticRenderLayers() {
   }

   public static class_1921 layer(class_2960 texture) {
      class_1921 cached = LAYERS.get(texture);
      if (cached == null) {
         cached = class_12249.method_75994(texture);
         LAYERS.put(texture, cached);
      }

      return cached;
   }
}
