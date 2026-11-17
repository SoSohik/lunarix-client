package ru.mentions.cosmetics.geo;

public final class GeoCube {
   public final GeoQuad[] quads = new GeoQuad[6];
   public float sizeX;
   public float sizeY;
   public float sizeZ;
   public final Vec3f pivot = new Vec3f();
   public final Vec3f rotation = new Vec3f();

   public GeoCube(float sizeX, float sizeY, float sizeZ) {
      this.sizeX = sizeX;
      this.sizeY = sizeY;
      this.sizeZ = sizeZ;
   }
}
