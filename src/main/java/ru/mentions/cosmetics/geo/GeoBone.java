package ru.mentions.cosmetics.geo;

import java.util.ArrayList;
import java.util.List;

public final class GeoBone {
   public final String name;
   public GeoBone parent;
   public final List<GeoBone> children = new ArrayList<>();
   public final List<GeoCube> cubes = new ArrayList<>();
   public final Vec3f pivot = new Vec3f();
   public float restRotX;
   public float restRotY;
   public float restRotZ;
   public float restPosX;
   public float restPosY;
   public float restPosZ;
   public float restScaleX = 1.0F;
   public float restScaleY = 1.0F;
   public float restScaleZ = 1.0F;
   public float rotX;
   public float rotY;
   public float rotZ;
   public float posX;
   public float posY;
   public float posZ;
   public float scaleX = 1.0F;
   public float scaleY = 1.0F;
   public float scaleZ = 1.0F;
   public boolean hidden;

   public GeoBone(String name) {
      this.name = name;
   }

   public void setRest(float rotX, float rotY, float rotZ, float posX, float posY, float posZ, float scaleX, float scaleY, float scaleZ) {
      this.restRotX = rotX;
      this.restRotY = rotY;
      this.restRotZ = rotZ;
      this.restPosX = posX;
      this.restPosY = posY;
      this.restPosZ = posZ;
      this.restScaleX = scaleX;
      this.restScaleY = scaleY;
      this.restScaleZ = scaleZ;
      this.captureRest();
   }

   public void captureRest() {
      this.rotX = this.restRotX;
      this.rotY = this.restRotY;
      this.rotZ = this.restRotZ;
      this.posX = this.restPosX;
      this.posY = this.restPosY;
      this.posZ = this.restPosZ;
      this.scaleX = this.restScaleX;
      this.scaleY = this.restScaleY;
      this.scaleZ = this.restScaleZ;
   }
}
