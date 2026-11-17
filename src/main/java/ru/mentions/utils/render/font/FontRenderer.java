package ru.mentions.utils.render.font;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FontRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger("client/FontRenderer");
   private final FontPipeline pipeline = new FontPipeline();
   private final Map<String, FontAtlas> fonts = new HashMap<>();
   private boolean initialized = false;

   public void loadFont(String name, String path) {
      class_2960 jsonId = class_2960.method_60655("client", "fonts/" + path + ".json");
      class_2960 textureId = class_2960.method_60655("client", "fonts/" + path + ".png");
      FontAtlas atlas = new FontAtlas(jsonId, textureId);
      this.fonts.put(name, atlas);
      LOGGER.info("Registered font: {} -> {}", name, path);
   }

   public void loadAllFonts(Map<String, String> registry) {
      for (Entry<String, String> entry : registry.entrySet()) {
         this.loadFont(entry.getKey(), entry.getValue());
      }
   }

   public void initialize() {
      if (!this.initialized) {
         LOGGER.info("Initializing {} fonts...", this.fonts.size());
         long startTime = System.currentTimeMillis();

         for (Entry<String, FontAtlas> entry : this.fonts.entrySet()) {
            entry.getValue().forceLoad();
         }

         this.initialized = true;
         LOGGER.info("All fonts initialized in {}ms", System.currentTimeMillis() - startTime);
      }
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public FontAtlas getFont(String name) {
      return this.fonts.get(name);
   }

   public void drawText(String fontName, String text, float x, float y, float size, int color) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         this.pipeline.drawText(atlas, text, x, y, size, color, 0.0F, 0, 0.0F);
      }
   }

   public void drawText(String fontName, String text, float x, float y, float size, int color, float rotation) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         this.pipeline.drawText(atlas, text, x, y, size, color, 0.0F, 0, rotation);
      }
   }

   public void drawTextWithOutline(String fontName, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         this.pipeline.drawText(atlas, text, x, y, size, color, outlineWidth, outlineColor, 0.0F);
      }
   }

   public void drawTextWithOutline(String fontName, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         this.pipeline.drawText(atlas, text, x, y, size, color, outlineWidth, outlineColor, rotation);
      }
   }

   public void drawTextFading(String fontName, String text, float x, float y, float size, float maxWidth, int color) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         this.pipeline.drawTextFading(atlas, text, x, y, size, maxWidth, color);
      }
   }

   public void drawTextFadingReverse(String fontName, String text, float x, float y, float size, float maxWidth, int color) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         this.pipeline.drawTextFadingReverse(atlas, text, x, y, size, maxWidth, color);
      }
   }

   public void drawTextRightAlignFadeLeft(String fontName, String text, float rightX, float y, float size, float fadeStartX, int color) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         this.pipeline.drawTextRightAlignFadeLeft(atlas, text, rightX, y, size, fadeStartX, color);
      }
   }

   public void drawCenteredText(String fontName, String text, float x, float y, float size, int color) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         float width = this.pipeline.getTextWidth(atlas, text, size);
         this.pipeline.drawText(atlas, text, x - width / 2.0F, y, size, color, 0.0F, 0, 0.0F);
      }
   }

   public void drawCenteredText(String fontName, String text, float x, float y, float size, int color, float rotation) {
      FontAtlas atlas = this.fonts.get(fontName);
      if (atlas != null) {
         float width = this.pipeline.getTextWidth(atlas, text, size);
         float height = this.pipeline.getTextHeight(atlas, text, size);
         float centerY = y + height / 2.0F;
         this.pipeline.drawTextRotatedAroundPoint(atlas, text, x - width / 2.0F, y, size, color, 0.0F, 0, rotation, x, centerY);
      }
   }

   public float getTextWidth(String fontName, String text, float size) {
      FontAtlas atlas = this.fonts.get(fontName);
      return atlas == null ? 0.0F : this.pipeline.getTextWidth(atlas, text, size);
   }

   public float getLineHeight(String fontName, float size) {
      FontAtlas atlas = this.fonts.get(fontName);
      return atlas == null ? size : atlas.getLineHeight() / atlas.getFontSize() * size;
   }

   public void flush() {
      this.pipeline.flush();
   }

   public void close() {
      this.pipeline.close();
      this.fonts.clear();
      this.initialized = false;
   }
}
