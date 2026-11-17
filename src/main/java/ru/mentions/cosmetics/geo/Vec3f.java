package ru.mentions.cosmetics.geo;

public final class Vec3f {
   public float x;
   public float y;
   public float z;

   public Vec3f() {
      this(0.0F, 0.0F, 0.0F);
   }

   public Vec3f(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3f set(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
      return this;
   }

   public Vec3f copy() {
      return new Vec3f(this.x, this.y, this.z);
   }
}
