package ru.mentions.cosmetics;

import java.util.Map;
import net.minecraft.class_2960;
import ru.mentions.cosmetics.anim.GeoAnimation;
import ru.mentions.cosmetics.geo.GeoModel;

public final class CosmeticEntry {
   public final int index;
   public final int id;
   public final String name;
   public final String author;
   public final CosmeticType type;
   public final class_2960 texture;
   public final int textureWidth;
   public final int textureHeight;
   public final GeoModel model;
   public final Map<String, GeoAnimation> animations;
   public final int position;
   public final float offsetX;
   public final float offsetY;
   public final float offsetZ;
   public final float yaw;
   public final float pitch;
   public final float roll;
   public final float scale;
   public final float previewY;
   public final float previewScale;
   public final int capeFrames;
   public final int capeFrameWidth;
   public final int capeFrameHeight;
   public final int capeFrameTicks;
   public final int textureFrames;
   public final int textureFrameHeight;
   public final int textureFrameTicks;

   public CosmeticEntry(
      int index,
      int id,
      String name,
      String author,
      CosmeticType type,
      class_2960 texture,
      int textureWidth,
      int textureHeight,
      GeoModel model,
      Map<String, GeoAnimation> animations,
      int position,
      float offsetX,
      float offsetY,
      float offsetZ,
      float yaw,
      float pitch,
      float roll,
      float scale,
      float previewY,
      float previewScale,
      int capeFrames,
      int capeFrameWidth,
      int capeFrameHeight,
      int capeFrameTicks,
      int textureFrames,
      int textureFrameHeight,
      int textureFrameTicks
   ) {
      this.index = index;
      this.id = id;
      this.name = name;
      this.author = author;
      this.type = type;
      this.texture = texture;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.model = model;
      this.animations = animations;
      this.position = position;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.yaw = yaw;
      this.pitch = pitch;
      this.roll = roll;
      this.scale = scale;
      this.previewY = previewY;
      this.previewScale = previewScale;
      this.capeFrames = capeFrames;
      this.capeFrameWidth = capeFrameWidth;
      this.capeFrameHeight = capeFrameHeight;
      this.capeFrameTicks = capeFrameTicks;
      this.textureFrames = textureFrames;
      this.textureFrameHeight = textureFrameHeight;
      this.textureFrameTicks = textureFrameTicks;
   }

   public float textureV(float v, long now) {
      if (this.textureFrames > 1 && this.textureFrameHeight > 0) {
         long frame = (long)Math.floorMod(now / ((long)Math.max(1, this.textureFrameTicks) * 50L), this.textureFrames);
         return ((float)frame + v) * (float)this.textureFrameHeight / (float)this.textureHeight;
      } else {
         return v;
      }
   }

   public boolean isCape() {
      return this.type == CosmeticType.CAPE;
   }

   public boolean isGeometry() {
      return this.model != null;
   }

   public boolean hasCapeFrames() {
      return this.capeFrames > 1;
   }
}
