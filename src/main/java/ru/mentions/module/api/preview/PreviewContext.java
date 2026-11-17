package ru.mentions.module.api.preview;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_745;

@Environment(EnvType.CLIENT)
public final class PreviewContext {
   private class_745 dummy;
   private class_243 anchor = class_243.field_1353;
   private float distance;
   private float height;
   private int phase;
   private long startedAt = System.currentTimeMillis();

   public class_745 dummy() {
      return this.dummy;
   }

   public class_243 anchor() {
      return this.anchor;
   }

   public float distance() {
      return this.distance;
   }

   public float height() {
      return this.height;
   }

   public int phase() {
      return this.phase;
   }

   public long elapsedMs() {
      return System.currentTimeMillis() - this.startedAt;
   }

   public void setDummy(class_745 dummy) {
      this.dummy = dummy;
   }

   public void setAnchor(class_243 anchor) {
      this.anchor = anchor;
   }

   public void setDistance(float distance) {
      this.distance = distance;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public void nextPhase() {
      this.phase++;
   }

   public void reset() {
      this.dummy = null;
      this.anchor = class_243.field_1353;
      this.phase = 0;
      this.startedAt = System.currentTimeMillis();
   }
}
