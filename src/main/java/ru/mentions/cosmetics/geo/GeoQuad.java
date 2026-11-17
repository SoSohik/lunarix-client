package ru.mentions.cosmetics.geo;

public final class GeoQuad {
   public final float[] positions = new float[12];
   public final float[] uvs = new float[8];
   public final float nx;
   public final float ny;
   public final float nz;

   public GeoQuad(float nx, float ny, float nz) {
      this.nx = nx;
      this.ny = ny;
      this.nz = nz;
   }

   public void set(int index, float x, float y, float z, float u, float v) {
      int p = index * 3;
      this.positions[p] = x;
      this.positions[p + 1] = y;
      this.positions[p + 2] = z;
      int t = index * 2;
      this.uvs[t] = u;
      this.uvs[t + 1] = v;
   }
}
