package ru.mentions.cosmetics.render;

import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4587.class_4665;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mentions.cosmetics.CosmeticEntry;
import ru.mentions.cosmetics.geo.GeoBone;
import ru.mentions.cosmetics.geo.GeoCube;
import ru.mentions.cosmetics.geo.GeoModel;
import ru.mentions.cosmetics.geo.GeoQuad;

public final class GeoRenderer {
   private static final Vector3f POSITION = new Vector3f();
   private static final Vector3f NORMAL = new Vector3f();

   private GeoRenderer() {
   }

   public static void render(CosmeticEntry cosmetic, class_4665 world, class_4588 consumer, int light, int tint) {
      render(cosmetic, world, consumer, light, tint, System.currentTimeMillis());
   }

   public static void render(CosmeticEntry cosmetic, class_4665 world, class_4588 consumer, int light, int tint, long now) {
      GeoModel model = cosmetic.model;
      if (model != null) {
         class_4587 stack = new class_4587();
         stack.method_34425(world.method_23761());

         for (GeoBone bone : model.topLevel) {
            bone(bone, stack, consumer, light, tint, cosmetic, now);
         }
      }
   }

   private static void bone(GeoBone bone, class_4587 stack, class_4588 consumer, int light, int tint, CosmeticEntry cosmetic, long now) {
      if (GeoTransforms.visible(bone)) {
         stack.method_22903();

         try {
            stack.method_34425(GeoTransforms.bone(new Matrix4f(), bone));

            for (GeoCube cube : bone.cubes) {
               cube(cube, stack, consumer, light, tint, cosmetic, now);
            }

            for (GeoBone child : bone.children) {
               bone(child, stack, consumer, light, tint, cosmetic, now);
            }
         } finally {
            stack.method_22909();
         }
      }
   }

   private static void cube(GeoCube cube, class_4587 stack, class_4588 consumer, int light, int tint, CosmeticEntry cosmetic, long now) {
      stack.method_22903();

      try {
         stack.method_34425(GeoTransforms.cube(new Matrix4f(), cube));
         class_4665 current = stack.method_23760();
         Matrix4f matrix = current.method_23761();

         for (GeoQuad quad : cube.quads) {
            if (quad != null) {
               NORMAL.set(quad.nx, quad.ny, quad.nz);
               current.method_56821(NORMAL, NORMAL);

               for (int i = 0; i < 4; i++) {
                  int position = i * 3;
                  int uv = i * 2;
                  POSITION.set(quad.positions[position], quad.positions[position + 1], quad.positions[position + 2]);
                  matrix.transformPosition(POSITION);
                  consumer.method_23919(
                     POSITION.x,
                     POSITION.y,
                     POSITION.z,
                     tint,
                     quad.uvs[uv],
                     cosmetic.textureV(quad.uvs[uv + 1], now),
                     655360,
                     light,
                     NORMAL.x,
                     NORMAL.y,
                     NORMAL.z
                  );
               }
            }
         }
      } finally {
         stack.method_22909();
      }
   }
}
