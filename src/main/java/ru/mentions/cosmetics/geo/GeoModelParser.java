package ru.mentions.cosmetics.geo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;

public final class GeoModelParser {
   private static final float[] DEFAULT_ORIGIN = new float[]{0.0F, 0.0F, 0.0F};
   private static final float[] DEFAULT_SIZE = new float[]{1.0F, 1.0F, 1.0F};
   private static final float[] NO_ROTATION = new float[]{0.0F, 0.0F, 0.0F};

   private GeoModelParser() {
   }

   public static GeoModel parse(String json) {
      try {
         JsonObject root = JsonParser.parseString(json).getAsJsonObject();
         return parseRoot(root);
      } catch (Exception var2) {
         return null;
      }
   }

   private static GeoModel parseRoot(JsonObject root) {
      JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
      if (geometries != null && !geometries.isEmpty()) {
         JsonObject geometry = geometries.get(0).getAsJsonObject();
         JsonObject description = geometry.getAsJsonObject("description");
         GeoModel model = new GeoModel();
         if (description != null) {
            if (description.has("texture_width")) {
               model.textureWidth = description.get("texture_width").getAsInt();
            }

            if (description.has("texture_height")) {
               model.textureHeight = description.get("texture_height").getAsInt();
            }
         }

         JsonArray bones = geometry.getAsJsonArray("bones");
         if (bones == null) {
            return model;
         } else {
            Map<String, GeoBone> parsed = new HashMap<>();

            for (JsonElement element : bones) {
               JsonObject boneJson = element.getAsJsonObject();
               GeoBone bone = parseBone(boneJson, model.textureWidth, model.textureHeight);
               if (bone != null) {
                  parsed.put(bone.name, bone);
               }
            }

            for (JsonElement elementx : bones) {
               JsonObject boneJson = elementx.getAsJsonObject();
               if (boneJson.has("name")) {
                  GeoBone bone = parsed.get(boneJson.get("name").getAsString());
                  if (bone != null) {
                     GeoBone parent = boneJson.has("parent") ? parsed.get(boneJson.get("parent").getAsString()) : null;
                     if (parent != null && parent != bone) {
                        parent.children.add(bone);
                        bone.parent = parent;
                     } else {
                        model.topLevel.add(bone);
                     }
                  }
               }
            }

            model.index();
            return model;
         }
      } else {
         return null;
      }
   }

   private static GeoBone parseBone(JsonObject json, int textureWidth, int textureHeight) {
      if (!json.has("name")) {
         return null;
      } else {
         GeoBone bone = new GeoBone(json.get("name").getAsString());
         float pivotX = 0.0F;
         float pivotY = 0.0F;
         float pivotZ = 0.0F;
         if (json.has("pivot")) {
            JsonArray pivot = json.getAsJsonArray("pivot");
            pivotX = -pivot.get(0).getAsFloat();
            pivotY = pivot.get(1).getAsFloat();
            pivotZ = pivot.get(2).getAsFloat();
            bone.pivot.set(pivotX, pivotY, pivotZ);
         }

         float rotX = 0.0F;
         float rotY = 0.0F;
         float rotZ = 0.0F;
         if (json.has("rotation")) {
            JsonArray rotation = json.getAsJsonArray("rotation");
            rotX = (float)Math.toRadians((double)(-rotation.get(0).getAsFloat()));
            rotY = (float)Math.toRadians((double)(-rotation.get(1).getAsFloat()));
            rotZ = (float)Math.toRadians((double)rotation.get(2).getAsFloat());
         }

         bone.setRest(rotX, rotY, rotZ, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         JsonArray cubes = json.getAsJsonArray("cubes");
         if (cubes != null) {
            for (JsonElement element : cubes) {
               bone.cubes.add(parseCube(element.getAsJsonObject(), textureWidth, textureHeight));
            }
         }

         JsonArray meshes = json.getAsJsonArray("meshes");
         if (meshes != null) {
            for (JsonElement element : meshes) {
               parseMesh(bone, element.getAsJsonObject(), textureWidth, textureHeight);
            }
         }

         return bone;
      }
   }

   private static void parseMesh(GeoBone bone, JsonObject mesh, int textureWidth, int textureHeight) {
      JsonArray vertices = mesh.getAsJsonArray("vertices");
      JsonArray faces = mesh.getAsJsonArray("faces");
      if (vertices != null && faces != null) {
         float[] pivot = floats(mesh, "pivot", DEFAULT_ORIGIN);
         float[] rotation = floats(mesh, "rotation", NO_ROTATION);
         int[] order = new int[]{0, 1, 3, 2};

         for (JsonElement element : faces) {
            JsonObject face = element.getAsJsonObject();
            JsonArray indices = face.getAsJsonArray("vertices");
            JsonArray uvs = face.getAsJsonArray("uv");
            if (indices != null && indices.size() == 4 && uvs != null && uvs.size() == 4) {
               GeoCube cube = new GeoCube(0.0F, 0.0F, 0.0F);
               cube.pivot.set(-pivot[0], pivot[1], pivot[2]);
               cube.rotation
                  .set((float)Math.toRadians((double)(-rotation[0])), (float)Math.toRadians((double)(-rotation[1])), (float)Math.toRadians((double)rotation[2]));
               GeoQuad quad = new GeoQuad(0.0F, 0.0F, 0.0F);

               for (int i = 0; i < 4; i++) {
                  int index = order[i];
                  JsonArray position = vertices.get(indices.get(index).getAsInt()).getAsJsonArray();
                  JsonArray uv = uvs.get(index).getAsJsonArray();
                  quad.set(
                     i,
                     -position.get(0).getAsFloat() / 16.0F,
                     position.get(1).getAsFloat() / 16.0F,
                     position.get(2).getAsFloat() / 16.0F,
                     uv.get(0).getAsFloat() / (float)textureWidth,
                     uv.get(1).getAsFloat() / (float)textureHeight
                  );
               }

               float ax = quad.positions[3] - quad.positions[0];
               float ay = quad.positions[4] - quad.positions[1];
               float az = quad.positions[5] - quad.positions[2];
               float bx = quad.positions[6] - quad.positions[0];
               float by = quad.positions[7] - quad.positions[1];
               float bz = quad.positions[8] - quad.positions[2];
               float nx = ay * bz - az * by;
               float ny = az * bx - ax * bz;
               float nz = ax * by - ay * bx;
               float length = (float)Math.sqrt((double)(nx * nx + ny * ny + nz * nz));
               if (length > 0.0F) {
                  GeoQuad normalised = new GeoQuad(nx / length, ny / length, nz / length);
                  System.arraycopy(quad.positions, 0, normalised.positions, 0, quad.positions.length);
                  System.arraycopy(quad.uvs, 0, normalised.uvs, 0, quad.uvs.length);
                  quad = normalised;
               }

               cube.quads[0] = quad;
               bone.cubes.add(cube);
            }
         }
      }
   }

   private static GeoCube parseCube(JsonObject json, int textureWidth, int textureHeight) {
      float[] origin = floats(json, "origin", DEFAULT_ORIGIN);
      float[] size = floats(json, "size", DEFAULT_SIZE);
      float[] pivot = floats(json, "pivot", DEFAULT_ORIGIN);
      float[] rotation = floats(json, "rotation", NO_ROTATION);
      float inflate = json.has("inflate") ? json.get("inflate").getAsFloat() : 0.0F;
      boolean mirror = json.has("mirror") && json.get("mirror").getAsBoolean();
      GeoCube cube = new GeoCube(size[0], size[1], size[2]);
      cube.pivot.set(-pivot[0], pivot[1], pivot[2]);
      cube.rotation
         .set((float)Math.toRadians((double)(-rotation[0])), (float)Math.toRadians((double)(-rotation[1])), (float)Math.toRadians((double)rotation[2]));
      buildQuads(cube, json, origin, size, inflate, mirror, textureWidth, textureHeight);
      return cube;
   }

   private static void buildQuads(
      GeoCube cube, JsonObject json, float[] origin, float[] size, float inflate, boolean mirror, int textureWidth, int textureHeight
   ) {
      float x = origin[0] - inflate;
      float y = origin[1] - inflate;
      float z = origin[2] - inflate;
      float sx = size[0] + inflate * 2.0F;
      float sy = size[1] + inflate * 2.0F;
      float sz = size[2] + inflate * 2.0F;
      float xMin = -(x + sx) / 16.0F;
      float xMax = -x / 16.0F;
      float yMin = y / 16.0F;
      float yMax = (y + sy) / 16.0F;
      float zMin = z / 16.0F;
      float zMax = (z + sz) / 16.0F;
      float u = 0.0F;
      float v = 0.0F;
      JsonObject perFace = null;
      if (json.has("uv")) {
         JsonElement uv = json.get("uv");
         if (uv.isJsonArray()) {
            JsonArray array = uv.getAsJsonArray();
            if (array.size() >= 2) {
               u = array.get(0).getAsFloat();
               v = array.get(1).getAsFloat();
            }
         } else if (uv.isJsonObject()) {
            perFace = uv.getAsJsonObject();
         }
      }

      if (perFace != null) {
         cube.quads[0] = perFaceQuad(perFace, "east", xMin, yMin, zMin, xMin, yMax, zMax, -1.0F, 0.0F, 0.0F, textureWidth, textureHeight);
         cube.quads[1] = perFaceQuad(perFace, "west", xMax, yMin, zMin, xMax, yMax, zMax, 1.0F, 0.0F, 0.0F, textureWidth, textureHeight);
         cube.quads[2] = perFaceQuad(perFace, "down", xMin, yMin, zMin, xMax, yMin, zMax, 0.0F, -1.0F, 0.0F, textureWidth, textureHeight);
         cube.quads[3] = perFaceQuad(perFace, "up", xMin, yMax, zMin, xMax, yMax, zMax, 0.0F, 1.0F, 0.0F, textureWidth, textureHeight);
         cube.quads[4] = perFaceQuad(perFace, "north", xMin, yMin, zMin, xMax, yMax, zMin, 0.0F, 0.0F, -1.0F, textureWidth, textureHeight);
         cube.quads[5] = perFaceQuad(perFace, "south", xMin, yMin, zMax, xMax, yMax, zMax, 0.0F, 0.0F, 1.0F, textureWidth, textureHeight);
      } else {
         float width = size[0];
         float height = size[1];
         float depth = size[2];
         cube.quads[0] = boxQuad("east", xMin, yMin, zMin, xMin, yMax, zMax, -1.0F, 0.0F, 0.0F, u, v, depth, height, width, textureWidth, textureHeight, mirror);
         cube.quads[1] = boxQuad("west", xMax, yMin, zMin, xMax, yMax, zMax, 1.0F, 0.0F, 0.0F, u, v, depth, height, width, textureWidth, textureHeight, mirror);
         cube.quads[2] = boxQuad("down", xMin, yMin, zMin, xMax, yMin, zMax, 0.0F, -1.0F, 0.0F, u, v, depth, height, width, textureWidth, textureHeight, mirror);
         cube.quads[3] = boxQuad("up", xMin, yMax, zMin, xMax, yMax, zMax, 0.0F, 1.0F, 0.0F, u, v, depth, height, width, textureWidth, textureHeight, mirror);
         cube.quads[4] = boxQuad(
            "north", xMin, yMin, zMin, xMax, yMax, zMin, 0.0F, 0.0F, -1.0F, u, v, depth, height, width, textureWidth, textureHeight, mirror
         );
         cube.quads[5] = boxQuad("south", xMin, yMin, zMax, xMax, yMax, zMax, 0.0F, 0.0F, 1.0F, u, v, depth, height, width, textureWidth, textureHeight, mirror);
      }
   }

   private static GeoQuad perFaceQuad(
      JsonObject perFace,
      String face,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float nx,
      float ny,
      float nz,
      int textureWidth,
      int textureHeight
   ) {
      if (!perFace.has(face)) {
         return null;
      } else {
         JsonObject faceJson = perFace.getAsJsonObject(face);
         JsonArray uv = faceJson.getAsJsonArray("uv");
         JsonArray uvSize = faceJson.getAsJsonArray("uv_size");
         if (uv != null && uvSize != null && uv.size() >= 2 && uvSize.size() >= 2) {
            float u = uv.get(0).getAsFloat() / (float)textureWidth;
            float v = uv.get(1).getAsFloat() / (float)textureHeight;
            float du = uvSize.get(0).getAsFloat() / (float)textureWidth;
            float dv = uvSize.get(1).getAsFloat() / (float)textureHeight;
            GeoQuad quad = new GeoQuad(nx, ny, nz);
            buildFace(quad, face, x0, y0, z0, x1, y1, z1, u, v, du, dv);
            return quad;
         } else {
            return null;
         }
      }
   }

   private static GeoQuad boxQuad(
      String face,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float nx,
      float ny,
      float nz,
      float u,
      float v,
      float depth,
      float height,
      float width,
      int textureWidth,
      int textureHeight,
      boolean mirror
   ) {
      float pu;
      float pv;
      float du;
      float dv;
      switch (face) {
         case "north":
            pu = (u + depth) / (float)textureWidth;
            pv = (v + depth) / (float)textureHeight;
            du = width / (float)textureWidth;
            dv = height / (float)textureHeight;
            break;
         case "south":
            pu = (u + depth * 2.0F + width) / (float)textureWidth;
            pv = (v + depth) / (float)textureHeight;
            du = width / (float)textureWidth;
            dv = height / (float)textureHeight;
            break;
         case "east":
            pu = (u + depth + width) / (float)textureWidth;
            pv = (v + depth) / (float)textureHeight;
            du = depth / (float)textureWidth;
            dv = height / (float)textureHeight;
            break;
         case "west":
            pu = u / (float)textureWidth;
            pv = (v + depth) / (float)textureHeight;
            du = depth / (float)textureWidth;
            dv = height / (float)textureHeight;
            break;
         case "up":
            pu = (u + depth) / (float)textureWidth;
            pv = v / (float)textureHeight;
            du = width / (float)textureWidth;
            dv = depth / (float)textureHeight;
            break;
         case "down":
            pu = (u + depth + width) / (float)textureWidth;
            pv = (v + depth) / (float)textureHeight;
            du = width / (float)textureWidth;
            dv = -depth / (float)textureHeight;
            break;
         default:
            pu = 0.0F;
            pv = 0.0F;
            du = 0.0F;
            dv = 0.0F;
      }

      GeoQuad quad = new GeoQuad(nx, ny, nz);
      if (mirror) {
         buildFace(quad, face, x1, y0, z0, x0, y1, z1, pu + du, pv, -du, dv);
      } else {
         buildFace(quad, face, x0, y0, z0, x1, y1, z1, pu, pv, du, dv);
      }

      return quad;
   }

   private static void buildFace(GeoQuad quad, String face, float x0, float y0, float z0, float x1, float y1, float z1, float u, float v, float du, float dv) {
      float u1 = u + du;
      float v1 = v + dv;
      switch (face) {
         case "north":
            quad.set(0, x1, y1, z0, u, v);
            quad.set(1, x0, y1, z0, u1, v);
            quad.set(2, x0, y0, z0, u1, v1);
            quad.set(3, x1, y0, z0, u, v1);
            break;
         case "south":
            quad.set(0, x0, y1, z1, u, v);
            quad.set(1, x1, y1, z1, u1, v);
            quad.set(2, x1, y0, z1, u1, v1);
            quad.set(3, x0, y0, z1, u, v1);
            break;
         case "east":
            quad.set(0, x1, y1, z1, u, v);
            quad.set(1, x1, y1, z0, u1, v);
            quad.set(2, x1, y0, z0, u1, v1);
            quad.set(3, x1, y0, z1, u, v1);
            break;
         case "west":
            quad.set(0, x0, y1, z0, u, v);
            quad.set(1, x0, y1, z1, u1, v);
            quad.set(2, x0, y0, z1, u1, v1);
            quad.set(3, x0, y0, z0, u, v1);
            break;
         case "up":
            quad.set(0, x0, y1, z0, u, v);
            quad.set(1, x0, y1, z1, u, v1);
            quad.set(2, x1, y1, z1, u1, v1);
            quad.set(3, x1, y1, z0, u1, v);
            break;
         case "down":
            quad.set(0, x1, y0, z0, u, v);
            quad.set(1, x1, y0, z1, u, v1);
            quad.set(2, x0, y0, z1, u1, v1);
            quad.set(3, x0, y0, z0, u1, v);
            break;
         default:
            quad.set(0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            quad.set(1, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            quad.set(2, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            quad.set(3, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      }
   }

   private static float[] floats(JsonObject json, String key, float[] fallback) {
      if (!json.has(key)) {
         return fallback;
      } else {
         JsonArray array = json.getAsJsonArray(key);
         float[] out = new float[array.size()];

         for (int i = 0; i < array.size(); i++) {
            out[i] = array.get(i).getAsFloat();
         }

         return out;
      }
   }
}
