package ru.mentions.cosmetics.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mentions.cosmetics.geo.GeoBone;
import ru.mentions.cosmetics.geo.GeoCube;
import ru.mentions.cosmetics.geo.GeoModel;
import ru.mentions.cosmetics.geo.GeoQuad;

public final class GeoTransforms {
   private static final float UNIT = 0.0625F;

   private GeoTransforms() {
   }

   public static boolean visible(GeoBone bone) {
      return !bone.hidden && (bone.scaleX != 0.0F || bone.scaleY != 0.0F || bone.scaleZ != 0.0F);
   }

   public static Matrix4f bone(Matrix4f matrix, GeoBone bone) {
      matrix.translate(-bone.posX * 0.0625F, bone.posY * 0.0625F, bone.posZ * 0.0625F);
      matrix.translate(bone.pivot.x * 0.0625F, bone.pivot.y * 0.0625F, bone.pivot.z * 0.0625F);
      rotate(matrix, bone.rotX, bone.rotY, bone.rotZ);
      matrix.scale(bone.scaleX, bone.scaleY, bone.scaleZ);
      return matrix.translate(-bone.pivot.x * 0.0625F, -bone.pivot.y * 0.0625F, -bone.pivot.z * 0.0625F);
   }

   public static Matrix4f cube(Matrix4f matrix, GeoCube cube) {
      matrix.translate(cube.pivot.x * 0.0625F, cube.pivot.y * 0.0625F, cube.pivot.z * 0.0625F);
      rotate(matrix, cube.rotation.x, cube.rotation.y, cube.rotation.z);
      return matrix.translate(-cube.pivot.x * 0.0625F, -cube.pivot.y * 0.0625F, -cube.pivot.z * 0.0625F);
   }

   public static float[] bounds(GeoModel model) {
      if (model == null) {
         return null;
      } else {
         float[] box = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

         for (GeoBone bone : model.topLevel) {
            grow(bone, new Matrix4f(), box);
         }

         return box[0] <= box[3] ? box : null;
      }
   }

   private static void grow(GeoBone bone, Matrix4f parent, float[] box) {
      if (visible(bone)) {
         Matrix4f matrix = bone(new Matrix4f(parent), bone);
         Vector3f corner = new Vector3f();

         for (GeoCube cube : bone.cubes) {
            Matrix4f transform = cube(new Matrix4f(matrix), cube);

            for (GeoQuad quad : cube.quads) {
               if (quad != null) {
                  for (int i = 0; i < 12; i += 3) {
                     corner.set(quad.positions[i], quad.positions[i + 1], quad.positions[i + 2]);
                     transform.transformPosition(corner);
                     box[0] = Math.min(box[0], corner.x);
                     box[1] = Math.min(box[1], corner.y);
                     box[2] = Math.min(box[2], corner.z);
                     box[3] = Math.max(box[3], corner.x);
                     box[4] = Math.max(box[4], corner.y);
                     box[5] = Math.max(box[5], corner.z);
                  }
               }
            }
         }

         for (GeoBone child : bone.children) {
            grow(child, matrix, box);
         }
      }
   }

   private static void rotate(Matrix4f matrix, float x, float y, float z) {
      matrix.rotateZ(z).rotateY(y).rotateX(x);
   }
}
