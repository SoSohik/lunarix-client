package ru.mentions.utils.render.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FontAtlas {
   private static final Logger LOGGER = LoggerFactory.getLogger("client/Font");
   private final class_2960 jsonId;
   private final class_2960 textureId;
   private final Map<Integer, Glyph> glyphs;
   private float atlasWidth = 512.0F;
   private float atlasHeight = 512.0F;
   private float fontSize = 32.0F;
   private float lineHeight = 40.0F;
   private float distanceRange = 4.0F;
   private boolean yOriginBottom = false;
   private final AtomicBoolean loaded = new AtomicBoolean(false);
   final Map<String, Float> widthCache = new LinkedHashMap<String, Float>(512, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, Float> eldest) {
         return this.size() > 2048;
      }
   };

   public FontAtlas(class_2960 jsonId, class_2960 textureId) {
      this.jsonId = jsonId;
      this.textureId = textureId;
      this.glyphs = new HashMap<>();
   }

   public void forceLoad() {
      if (!this.loaded.get()) {
         synchronized (this) {
            if (!this.loaded.get()) {
               this.doLoad();
            }
         }
      }
   }

   public void ensureLoaded() {
      if (!this.loaded.get()) {
         synchronized (this) {
            if (!this.loaded.get()) {
               this.doLoad();
            }
         }
      }
   }

   private void doLoad() {
      try {
         Optional resourceOpt = class_310.method_1551().method_1478().method_14486(this.jsonId);
         if (resourceOpt.isEmpty()) {
            LOGGER.warn("Font JSON not found: {}", this.jsonId);
            this.loaded.set(true);
            return;
         }

         try (
            InputStream is = ((class_3298)resourceOpt.get()).method_14482();
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
         ) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            this.parseJson(root);
            this.loaded.set(true);
            LOGGER.info("Loaded font: {} with {} glyphs", this.jsonId, this.glyphs.size());
         }
      } catch (Exception var10) {
         LOGGER.error("Failed to load font: {}", this.jsonId, var10);
         this.loaded.set(true);
      }
   }

   private void parseJson(JsonObject root) {
      float emSize = 1.0F;
      if (root.has("atlas")) {
         JsonObject atlas = root.getAsJsonObject("atlas");
         this.atlasWidth = this.getFloat(atlas, "width", 512.0F);
         this.atlasHeight = this.getFloat(atlas, "height", 512.0F);
         this.fontSize = this.getFloat(atlas, "size", 32.0F);
         this.distanceRange = this.getFloat(atlas, "distanceRange", 4.0F);
         if (atlas.has("yOrigin")) {
            String origin = atlas.get("yOrigin").getAsString();
            this.yOriginBottom = origin.equalsIgnoreCase("bottom");
         }

         LOGGER.info(
            "Atlas: {}x{}, size={}, distanceRange={}, yOrigin={}",
            new Object[]{this.atlasWidth, this.atlasHeight, this.fontSize, this.distanceRange, this.yOriginBottom ? "bottom" : "top"}
         );
      }

      if (root.has("metrics")) {
         JsonObject metrics = root.getAsJsonObject("metrics");
         emSize = this.getFloat(metrics, "emSize", 1.0F);
         float normalizedLineHeight = this.getFloat(metrics, "lineHeight", 1.2F);
         this.lineHeight = normalizedLineHeight * this.fontSize;
      }

      if (root.has("glyphs")) {
         for (JsonElement elem : root.getAsJsonArray("glyphs")) {
            JsonObject g = elem.getAsJsonObject();
            this.parseMsdfGlyph(g, emSize);
         }
      }

      this.populateUkrainianAndCyrillicFallbacks();
   }

   private void populateUkrainianAndCyrillicFallbacks() {
      Glyph gI = this.glyphs.get(73);
      if (gI != null) {
         if (!this.glyphs.containsKey(1030)) {
            this.glyphs.put(1030, new Glyph(1030, gI.x, gI.y, gI.width, gI.height, gI.xOffset, gI.yOffset, gI.xAdvance, this.atlasWidth, this.atlasHeight));
         }

         if (!this.glyphs.containsKey(1031)) {
            this.glyphs.put(1031, new Glyph(1031, gI.x, gI.y, gI.width, gI.height, gI.xOffset, gI.yOffset, gI.xAdvance, this.atlasWidth, this.atlasHeight));
         }
      }

      Glyph gi = this.glyphs.get(105);
      if (gi != null) {
         if (!this.glyphs.containsKey(1110)) {
            this.glyphs.put(1110, new Glyph(1110, gi.x, gi.y, gi.width, gi.height, gi.xOffset, gi.yOffset, gi.xAdvance, this.atlasWidth, this.atlasHeight));
         }

         if (!this.glyphs.containsKey(1111)) {
            this.glyphs.put(1111, new Glyph(1111, gi.x, gi.y, gi.width, gi.height, gi.xOffset, gi.yOffset, gi.xAdvance, this.atlasWidth, this.atlasHeight));
         }
      }

      Glyph gG = this.glyphs.get(1043);
      if (gG != null && !this.glyphs.containsKey(1168)) {
         this.glyphs.put(1168, new Glyph(1168, gG.x, gG.y, gG.width, gG.height, gG.xOffset, gG.yOffset, gG.xAdvance, this.atlasWidth, this.atlasHeight));
      }

      Glyph gg = this.glyphs.get(1075);
      if (gg != null && !this.glyphs.containsKey(1169)) {
         this.glyphs.put(1169, new Glyph(1169, gg.x, gg.y, gg.width, gg.height, gg.xOffset, gg.yOffset, gg.xAdvance, this.atlasWidth, this.atlasHeight));
      }

      Glyph gApo = this.glyphs.get(39);
      if (gApo != null) {
         if (!this.glyphs.containsKey(8217)) {
            this.glyphs
               .put(
                  8217, new Glyph(8217, gApo.x, gApo.y, gApo.width, gApo.height, gApo.xOffset, gApo.yOffset, gApo.xAdvance, this.atlasWidth, this.atlasHeight)
               );
         }

         if (!this.glyphs.containsKey(700)) {
            this.glyphs
               .put(700, new Glyph(700, gApo.x, gApo.y, gApo.width, gApo.height, gApo.xOffset, gApo.yOffset, gApo.xAdvance, this.atlasWidth, this.atlasHeight));
         }

         if (!this.glyphs.containsKey(8216)) {
            this.glyphs
               .put(
                  8216, new Glyph(8216, gApo.x, gApo.y, gApo.width, gApo.height, gApo.xOffset, gApo.yOffset, gApo.xAdvance, this.atlasWidth, this.atlasHeight)
               );
         }
      }
   }

   private void parseMsdfGlyph(JsonObject g, float emSize) {
      int unicode = -1;
      if (g.has("unicode")) {
         unicode = g.get("unicode").getAsInt();
      } else if (g.has("char")) {
         String charStr = g.get("char").getAsString();
         if (!charStr.isEmpty()) {
            unicode = charStr.codePointAt(0);
         }
      } else if (g.has("id")) {
         unicode = g.get("id").getAsInt();
      }

      if (unicode >= 0) {
         float advance = this.getFloat(g, "advance", 0.0F) * this.fontSize;
         if (advance == 0.0F) {
            advance = this.getFloat(g, "xadvance", 0.0F);
         }

         float x = 0.0F;
         float y = 0.0F;
         float w = 0.0F;
         float h = 0.0F;
         float xOffset = 0.0F;
         float yOffset = 0.0F;
         if (g.has("atlasBounds")) {
            JsonObject bounds = g.getAsJsonObject("atlasBounds");
            float left = this.getFloat(bounds, "left", 0.0F);
            float bottom = this.getFloat(bounds, "bottom", 0.0F);
            float right = this.getFloat(bounds, "right", 0.0F);
            float top = this.getFloat(bounds, "top", 0.0F);
            x = left;
            w = right - left;
            h = top - bottom;
            y = this.yOriginBottom ? this.atlasHeight - top : bottom;
         } else if (g.has("x") && g.has("y") && g.has("width") && g.has("height")) {
            x = this.getFloat(g, "x", 0.0F);
            y = this.getFloat(g, "y", 0.0F);
            w = this.getFloat(g, "width", 0.0F);
            h = this.getFloat(g, "height", 0.0F);
         }

         if (g.has("planeBounds")) {
            JsonObject plane = g.getAsJsonObject("planeBounds");
            float pLeft = this.getFloat(plane, "left", 0.0F);
            float pBottom = this.getFloat(plane, "bottom", 0.0F);
            float pRight = this.getFloat(plane, "right", 0.0F);
            float pTop = this.getFloat(plane, "top", 0.0F);
            xOffset = pLeft * this.fontSize;
            float ascender = 0.95F;
            yOffset = (ascender - pTop) * this.fontSize;
         } else if (g.has("xoffset") && g.has("yoffset")) {
            xOffset = this.getFloat(g, "xoffset", 0.0F);
            yOffset = this.getFloat(g, "yoffset", 0.0F);
         }

         this.glyphs.put(unicode, new Glyph(unicode, x, y, w, h, xOffset, yOffset, advance, this.atlasWidth, this.atlasHeight));
      }
   }

   private float getFloat(JsonObject obj, String key, float def) {
      return obj.has(key) ? obj.get(key).getAsFloat() : def;
   }

   public Glyph getGlyph(int codePoint) {
      Glyph g = this.glyphs.get(codePoint);
      if (g != null) {
         return g;
      } else {
         int fallback = resolveFallbackCodePoint(codePoint);
         return fallback != codePoint ? this.glyphs.get(fallback) : null;
      }
   }

   public boolean hasGlyph(int codePoint) {
      return this.glyphs.containsKey(codePoint) || this.glyphs.containsKey(resolveFallbackCodePoint(codePoint));
   }

   public static int resolveFallbackCodePoint(int codePoint) {
      return switch (codePoint) {
         case 96, 699, 700, 8216, 8217 -> 39;
         case 1025 -> 1045;
         case 1028 -> 1045;
         case 1030 -> 73;
         case 1031 -> 73;
         case 1105 -> 1077;
         case 1108 -> 1077;
         case 1110 -> 105;
         case 1111 -> 105;
         case 1168 -> 1043;
         case 1169 -> 1075;
         case 8211, 8212 -> 45;
         case 8220, 8221, 8222 -> 34;
         case 8230 -> 46;
         default -> codePoint;
      };
   }

   public class_2960 getTextureId() {
      return this.textureId;
   }

   public float getFontSize() {
      return this.fontSize;
   }

   public float getLineHeight() {
      return this.lineHeight;
   }

   public float getAtlasWidth() {
      return this.atlasWidth;
   }

   public float getAtlasHeight() {
      return this.atlasHeight;
   }

   public float getDistanceRange() {
      return this.distanceRange;
   }

   public boolean isLoaded() {
      return this.loaded.get();
   }

   public int getGlyphCount() {
      return this.glyphs.size();
   }
}
