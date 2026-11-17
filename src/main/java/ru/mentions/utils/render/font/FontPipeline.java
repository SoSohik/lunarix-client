package ru.mentions.utils.render.font;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.system.MemoryUtil;
import ru.mentions.utils.colors.ColorFormatting;
import ru.mentions.utils.render.DrawBatcher;

@Environment(EnvType.CLIENT)
public class FontPipeline implements DrawBatcher.Batched {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/msdf");
   private static final class_2960 SHADER_ID = class_2960.method_60655("client", "core/msdf");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(SHADER_ID)
         .withFragmentShader(SHADER_ID)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("FontData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final int[] LEGACY_COLORS = new int[32];
   private static final String LEGACY_CODE_CHARS;
   private static final int MAX_CHARS = 256;
   private static final int BUFFER_SIZE = 16448;
   private static final int PREPROCESS_CACHE_SIZE = 256;
   private static final Map<String, String> preprocessCache;
   private static final int INITIAL_UNIFORM_BUFFERS = 32;
   private GpuBuffer[] uniformBuffers;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;
   private final Map<GpuTexture, GpuTextureView> textureViewCache = new HashMap<>();
   private static final int HEADER_SIZE = 64;
   private int batchedChars = 0;
   private FontAtlas currentAtlas = null;
   private float currentOutlineWidth = 0.0F;
   private int currentOutlineColor = 0;
   private final ArrayList<FontPipeline.FontChunk> chunks = new ArrayList<>();
   private static final Map<Integer, Integer> SMALL_CAPS_MAP;
   private static final int LIGHTNING_CP;

   private void appendGlyph(
      float x, float y, float w, float h, float u0, float v0, float u1, float v1, int color, float rotation, float pivotX, float pivotY, float glyphScale
   ) {
      this.ensureInitialized();
      if (this.batchedChars >= 256 || this.dataBuffer != null && this.dataBuffer.position() + 64 > this.dataBuffer.capacity()) {
         FontAtlas prevAtlas = this.currentAtlas;
         float prevOutlineWidth = this.currentOutlineWidth;
         int prevOutlineColor = this.currentOutlineColor;
         this.flush();
         this.currentAtlas = prevAtlas;
         this.currentOutlineWidth = prevOutlineWidth;
         this.currentOutlineColor = prevOutlineColor;
      }

      if (this.batchedChars == 0) {
         this.dataBuffer.clear();
         this.dataBuffer.position(64);
      }

      this.dataBuffer.putFloat(x);
      this.dataBuffer.putFloat(y);
      this.dataBuffer.putFloat(w);
      this.dataBuffer.putFloat(h);
      this.dataBuffer.putFloat(u0);
      this.dataBuffer.putFloat(v0);
      this.dataBuffer.putFloat(u1);
      this.dataBuffer.putFloat(v1);
      float a = (float)(color >> 24 & 0xFF) / 255.0F;
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      this.dataBuffer.putFloat(r);
      this.dataBuffer.putFloat(g);
      this.dataBuffer.putFloat(b);
      this.dataBuffer.putFloat(a);
      this.dataBuffer.putFloat(rotation);
      this.dataBuffer.putFloat(pivotX);
      this.dataBuffer.putFloat(pivotY);
      this.dataBuffer.putFloat(glyphScale);
      this.batchedChars++;
      if (this.batchedChars >= 256) {
         FontAtlas prevAtlas = this.currentAtlas;
         float prevOutlineWidth = this.currentOutlineWidth;
         int prevOutlineColor = this.currentOutlineColor;
         this.flush();
         this.currentAtlas = prevAtlas;
         this.currentOutlineWidth = prevOutlineWidth;
         this.currentOutlineColor = prevOutlineColor;
      }
   }

   private int getFixedScaledWidth() {
      class_310 client = class_310.method_1551();
      return client != null && client.method_22683() != null ? (int)Math.ceil((double)client.method_22683().method_4489() / 2.0) : 960;
   }

   private int getFixedScaledHeight() {
      class_310 client = class_310.method_1551();
      return client != null && client.method_22683() != null ? (int)Math.ceil((double)client.method_22683().method_4506() / 2.0) : 540;
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(16448);
         ByteBuffer dummyData = MemoryUtil.memAlloc(4);
         dummyData.putInt(0);
         dummyData.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:font_dummy_vertex", 32, dummyData);
         MemoryUtil.memFree(dummyData);
         this.uniformBuffers = new GpuBuffer[32];
         this.initialized = true;
      }
   }

   private GpuBuffer nextUniformBuffer() {
      int idx = this.chunks.size();
      if (idx == this.uniformBuffers.length) {
         this.uniformBuffers = Arrays.copyOf(this.uniformBuffers, this.uniformBuffers.length * 2);
      }

      GpuBuffer buf = this.uniformBuffers[idx];
      if (buf == null) {
         this.uniformBuffers[idx] = buf = RenderSystem.getDevice().createBuffer(() -> "client:font_uniform_" + idx, 136, 16448L);
      }

      return buf;
   }

   public void drawText(FontAtlas atlas, String text, float x, float y, float size, int color) {
      this.drawText(atlas, text, x, y, size, color, 0.0F, 0, 0.0F);
   }

   public void drawText(FontAtlas atlas, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation) {
      class_310 client = class_310.method_1551();
      if (client.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            text = this.preprocessText(text);
            text = ColorFormatting.get(text);
            atlas.ensureLoaded();
            if (atlas.getGlyphCount() != 0) {
               this.ensureInitialized();
               if (DrawBatcher.isEnabled()) {
                  DrawBatcher.register(this);
               }

               if (this.currentAtlas != null
                  && (this.currentAtlas != atlas || this.currentOutlineWidth != outlineWidth || this.currentOutlineColor != outlineColor)) {
                  this.flush();
               }

               this.currentAtlas = atlas;
               this.currentOutlineWidth = outlineWidth;
               this.currentOutlineColor = outlineColor;
               float scale = size / atlas.getFontSize();
               float cursorX = x;
               float cursorY = y;
               float rotationRad = (float)Math.toRadians((double)rotation);
               float pivotX = 0.0F;
               float pivotY = 0.0F;
               if (rotation != 0.0F) {
                  pivotX = x + this.getTextWidth(atlas, text, size) / 2.0F;
                  pivotY = y + this.getTextHeight(atlas, text, size) / 2.0F;
               }

               int currentColor = color;
               int i = 0;

               while (i < text.length()) {
                  FontPipeline.ColorAdvance colorAdvance = this.tryParseColorCode(text, i, color, currentColor);
                  if (colorAdvance.matched()) {
                     currentColor = colorAdvance.color();
                     i += colorAdvance.skip();
                  } else {
                     int codePoint = text.codePointAt(i);
                     int charCount = Character.charCount(codePoint);
                     if (codePoint == 10) {
                        cursorX = x;
                        cursorY += atlas.getLineHeight() * scale;
                        i += charCount;
                     } else {
                        Glyph glyph = atlas.getGlyph(codePoint);
                        if (glyph != null) {
                           float glyphX = cursorX + glyph.xOffset * scale;
                           float glyphY = cursorY + glyph.yOffset * scale;
                           float glyphW = glyph.width * scale;
                           float glyphH = glyph.height * scale;
                           if (glyph.width > 0.0F && glyph.height > 0.0F) {
                              this.appendGlyph(
                                 glyphX, glyphY, glyphW, glyphH, glyph.u0, glyph.v0, glyph.u1, glyph.v1, currentColor, rotationRad, pivotX, pivotY, scale
                              );
                           }

                           cursorX += glyph.xAdvance * scale;
                           if (this.batchedChars >= 256) {
                              this.flush();
                              this.currentAtlas = atlas;
                           }

                           i += charCount;
                        } else {
                           Glyph fallback = atlas.getGlyph(63);
                           if (fallback != null && fallback.width > 0.0F && fallback.height > 0.0F) {
                              this.appendGlyph(
                                 cursorX + fallback.xOffset * scale,
                                 cursorY + fallback.yOffset * scale,
                                 fallback.width * scale,
                                 fallback.height * scale,
                                 fallback.u0,
                                 fallback.v0,
                                 fallback.u1,
                                 fallback.v1,
                                 currentColor,
                                 rotationRad,
                                 pivotX,
                                 pivotY,
                                 scale
                              );
                              cursorX += fallback.xAdvance * scale;
                           } else {
                              cursorX += size * 0.5F;
                           }

                           if (this.batchedChars >= 256) {
                              this.flush();
                              this.currentAtlas = atlas;
                           }

                           i += charCount;
                        }
                     }
                  }
               }

               if (!DrawBatcher.isEnabled()) {
                  this.flush();
               }
            }
         }
      }
   }

   public void drawTextFading(FontAtlas atlas, String text, float x, float y, float size, float maxWidth, int color) {
      class_310 client = class_310.method_1551();
      if (client.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            if (this.getTextWidth(atlas, text, size) <= maxWidth) {
               this.drawText(atlas, text, x, y, size, color, 0.0F, 0, 0.0F);
            } else {
               int baseAlpha = color >> 24 & 0xFF;
               if (baseAlpha != 0) {
                  int rgb = color & 16777215;
                  float fadeStart = maxWidth - 40.0F;
                  text = this.preprocessText(text);
                  text = ColorFormatting.get(text);
                  atlas.ensureLoaded();
                  if (atlas.getGlyphCount() != 0) {
                     this.ensureInitialized();
                     if (DrawBatcher.isEnabled()) {
                        DrawBatcher.register(this);
                     }

                     if (this.currentAtlas != null && (this.currentAtlas != atlas || this.currentOutlineWidth != 0.0F || this.currentOutlineColor != 0)) {
                        this.flush();
                     }

                     this.currentAtlas = atlas;
                     this.currentOutlineWidth = 0.0F;
                     this.currentOutlineColor = 0;
                     float scale = size / atlas.getFontSize();
                     float currentX = 0.0F;
                     int i = 0;

                     while (i < text.length()) {
                        FontPipeline.ColorAdvance colorAdvance = this.trySkipColorCode(text, i);
                        if (colorAdvance.matched()) {
                           i += colorAdvance.skip();
                        } else {
                           int codePoint = text.codePointAt(i);
                           int charCount = Character.charCount(codePoint);
                           Glyph glyph = atlas.getGlyph(codePoint);
                           float advance;
                           if (glyph != null) {
                              advance = glyph.xAdvance * scale;
                           } else {
                              Glyph fallback = atlas.getGlyph(63);
                              advance = fallback != null ? fallback.xAdvance * scale : size * 0.5F;
                           }

                           if (currentX + advance > maxWidth) {
                              break;
                           }

                           int finalAlpha = baseAlpha;
                           if (currentX > fadeStart) {
                              float fadeProgress = (currentX - fadeStart) / (maxWidth - fadeStart);
                              finalAlpha = (int)((float)baseAlpha * (1.0F - fadeProgress));
                           }

                           if (finalAlpha > 0 && glyph != null && glyph.width > 0.0F && glyph.height > 0.0F) {
                              this.appendGlyph(
                                 x + currentX + glyph.xOffset * scale,
                                 y + glyph.yOffset * scale,
                                 glyph.width * scale,
                                 glyph.height * scale,
                                 glyph.u0,
                                 glyph.v0,
                                 glyph.u1,
                                 glyph.v1,
                                 finalAlpha << 24 | rgb,
                                 0.0F,
                                 0.0F,
                                 0.0F,
                                 scale
                              );
                              if (this.batchedChars >= 256) {
                                 this.flush();
                                 this.currentAtlas = atlas;
                              }
                           }

                           currentX += advance;
                           i += charCount;
                        }
                     }

                     if (!DrawBatcher.isEnabled()) {
                        this.flush();
                     }
                  }
               }
            }
         }
      }
   }

   public void drawTextFadingReverse(FontAtlas atlas, String text, float x, float y, float size, float maxWidth, int color) {
      class_310 client = class_310.method_1551();
      if (client.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            if (this.getTextWidth(atlas, text, size) <= maxWidth) {
               this.drawText(atlas, text, x, y, size, color, 0.0F, 0, 0.0F);
            } else {
               int baseAlpha = color >> 24 & 0xFF;
               if (baseAlpha != 0) {
                  int rgb = color & 16777215;
                  float fadeWidth = 40.0F;
                  text = this.preprocessText(text);
                  text = ColorFormatting.get(text);
                  atlas.ensureLoaded();
                  if (atlas.getGlyphCount() != 0) {
                     this.ensureInitialized();
                     if (DrawBatcher.isEnabled()) {
                        DrawBatcher.register(this);
                     }

                     if (this.currentAtlas != null && (this.currentAtlas != atlas || this.currentOutlineWidth != 0.0F || this.currentOutlineColor != 0)) {
                        this.flush();
                     }

                     this.currentAtlas = atlas;
                     this.currentOutlineWidth = 0.0F;
                     this.currentOutlineColor = 0;
                     float scale = size / atlas.getFontSize();
                     float totalWidth = this.getTextWidth(atlas, text, size);
                     float skipWidth = totalWidth - maxWidth;
                     float skipped = 0.0F;
                     float drawX = 0.0F;
                     int i = 0;

                     while (i < text.length()) {
                        FontPipeline.ColorAdvance colorAdvance = this.trySkipColorCode(text, i);
                        if (colorAdvance.matched()) {
                           i += colorAdvance.skip();
                        } else {
                           int codePoint = text.codePointAt(i);
                           int charCount = Character.charCount(codePoint);
                           Glyph glyph = atlas.getGlyph(codePoint);
                           float advance;
                           if (glyph != null) {
                              advance = glyph.xAdvance * scale;
                           } else {
                              Glyph fallback = atlas.getGlyph(63);
                              advance = fallback != null ? fallback.xAdvance * scale : size * 0.5F;
                           }

                           if (skipped < skipWidth) {
                              skipped += advance;
                              i += charCount;
                           } else {
                              if (drawX + advance > maxWidth) {
                                 break;
                              }

                              int finalAlpha = baseAlpha;
                              if (drawX < fadeWidth) {
                                 finalAlpha = (int)((float)baseAlpha * (drawX / fadeWidth));
                              }

                              if (finalAlpha > 0 && glyph != null && glyph.width > 0.0F && glyph.height > 0.0F) {
                                 this.appendGlyph(
                                    x + drawX + glyph.xOffset * scale,
                                    y + glyph.yOffset * scale,
                                    glyph.width * scale,
                                    glyph.height * scale,
                                    glyph.u0,
                                    glyph.v0,
                                    glyph.u1,
                                    glyph.v1,
                                    finalAlpha << 24 | rgb,
                                    0.0F,
                                    0.0F,
                                    0.0F,
                                    scale
                                 );
                                 if (this.batchedChars >= 256) {
                                    this.flush();
                                    this.currentAtlas = atlas;
                                 }
                              }

                              drawX += advance;
                              i += charCount;
                           }
                        }
                     }

                     if (!DrawBatcher.isEnabled()) {
                        this.flush();
                     }
                  }
               }
            }
         }
      }
   }

   public void drawTextRightAlignFadeLeft(FontAtlas atlas, String text, float rightX, float y, float size, float fadeStartX, int color) {
      class_310 client = class_310.method_1551();
      if (client.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            float totalWidth = this.getTextWidth(atlas, text, size);
            float startX = rightX - totalWidth;
            if (startX >= fadeStartX) {
               this.drawText(atlas, text, startX, y, size, color, 0.0F, 0, 0.0F);
            } else {
               int baseAlpha = color >> 24 & 0xFF;
               if (baseAlpha != 0) {
                  int rgb = color & 16777215;
                  float fadeWidth = Math.min(32.0F, totalWidth * 0.45F);
                  if (fadeWidth < 1.0F) {
                     fadeWidth = 1.0F;
                  }

                  text = this.preprocessText(text);
                  text = ColorFormatting.get(text);
                  atlas.ensureLoaded();
                  if (atlas.getGlyphCount() != 0) {
                     this.ensureInitialized();
                     if (DrawBatcher.isEnabled()) {
                        DrawBatcher.register(this);
                     }

                     if (this.currentAtlas != null && (this.currentAtlas != atlas || this.currentOutlineWidth != 0.0F || this.currentOutlineColor != 0)) {
                        this.flush();
                     }

                     this.currentAtlas = atlas;
                     this.currentOutlineWidth = 0.0F;
                     this.currentOutlineColor = 0;
                     float scale = size / atlas.getFontSize();
                     float cursorX = 0.0F;
                     int i = 0;

                     while (i < text.length()) {
                        FontPipeline.ColorAdvance colorAdvance = this.trySkipColorCode(text, i);
                        if (colorAdvance.matched()) {
                           i += colorAdvance.skip();
                        } else {
                           int codePoint = text.codePointAt(i);
                           int charCount = Character.charCount(codePoint);
                           Glyph glyph = atlas.getGlyph(codePoint);
                           Glyph fallback;
                           float advance = glyph != null
                              ? glyph.xAdvance * scale
                              : ((fallback = atlas.getGlyph(63)) != null ? fallback.xAdvance * scale : size * 0.5F);
                           float glyphX = startX + cursorX;
                           float glyphRight = glyphX + advance;
                           int finalAlpha = baseAlpha;
                           if (glyphRight <= fadeStartX) {
                              finalAlpha = 0;
                           } else if (glyphX < fadeStartX + fadeWidth) {
                              float t = (glyphRight - fadeStartX) / fadeWidth;
                              if (t < 0.0F) {
                                 t = 0.0F;
                              }

                              if (t > 1.0F) {
                                 t = 1.0F;
                              }

                              finalAlpha = (int)((float)baseAlpha * t);
                           }

                           if (finalAlpha > 0 && glyph != null && glyph.width > 0.0F && glyph.height > 0.0F) {
                              this.appendGlyph(
                                 glyphX + glyph.xOffset * scale,
                                 y + glyph.yOffset * scale,
                                 glyph.width * scale,
                                 glyph.height * scale,
                                 glyph.u0,
                                 glyph.v0,
                                 glyph.u1,
                                 glyph.v1,
                                 finalAlpha << 24 | rgb,
                                 0.0F,
                                 0.0F,
                                 0.0F,
                                 scale
                              );
                              if (this.batchedChars >= 256) {
                                 this.flush();
                                 this.currentAtlas = atlas;
                              }
                           }

                           cursorX += advance;
                           i += charCount;
                        }
                     }

                     if (!DrawBatcher.isEnabled()) {
                        this.flush();
                     }
                  }
               }
            }
         }
      }
   }

   public void drawTextRotatedAroundPoint(
      FontAtlas atlas, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation, float pivotX, float pivotY
   ) {
      class_310 client = class_310.method_1551();
      if (client.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            text = this.preprocessText(text);
            text = ColorFormatting.get(text);
            atlas.ensureLoaded();
            if (atlas.getGlyphCount() != 0) {
               this.ensureInitialized();
               if (DrawBatcher.isEnabled()) {
                  DrawBatcher.register(this);
               }

               if (this.currentAtlas != null
                  && (this.currentAtlas != atlas || this.currentOutlineWidth != outlineWidth || this.currentOutlineColor != outlineColor)) {
                  this.flush();
               }

               this.currentAtlas = atlas;
               this.currentOutlineWidth = outlineWidth;
               this.currentOutlineColor = outlineColor;
               float scale = size / atlas.getFontSize();
               float cursorX = x;
               float cursorY = y;
               float rotationRad = (float)Math.toRadians((double)rotation);
               int currentColor = color;
               int i = 0;

               while (i < text.length()) {
                  FontPipeline.ColorAdvance colorAdvance = this.tryParseColorCode(text, i, color, currentColor);
                  if (colorAdvance.matched()) {
                     currentColor = colorAdvance.color();
                     i += colorAdvance.skip();
                  } else {
                     int codePoint = text.codePointAt(i);
                     int charCount = Character.charCount(codePoint);
                     if (codePoint == 10) {
                        cursorX = x;
                        cursorY += atlas.getLineHeight() * scale;
                        i += charCount;
                     } else {
                        Glyph glyph = atlas.getGlyph(codePoint);
                        if (glyph != null) {
                           float glyphX = cursorX + glyph.xOffset * scale;
                           float glyphY = cursorY + glyph.yOffset * scale;
                           float glyphW = glyph.width * scale;
                           float glyphH = glyph.height * scale;
                           if (glyph.width > 0.0F && glyph.height > 0.0F) {
                              this.appendGlyph(
                                 glyphX, glyphY, glyphW, glyphH, glyph.u0, glyph.v0, glyph.u1, glyph.v1, currentColor, rotationRad, pivotX, pivotY, scale
                              );
                           }

                           cursorX += glyph.xAdvance * scale;
                           if (this.batchedChars >= 256) {
                              this.flush();
                              this.currentAtlas = atlas;
                           }

                           i += charCount;
                        } else {
                           Glyph fallback = atlas.getGlyph(63);
                           if (fallback != null && fallback.width > 0.0F && fallback.height > 0.0F) {
                              this.appendGlyph(
                                 cursorX + fallback.xOffset * scale,
                                 cursorY + fallback.yOffset * scale,
                                 fallback.width * scale,
                                 fallback.height * scale,
                                 fallback.u0,
                                 fallback.v0,
                                 fallback.u1,
                                 fallback.v1,
                                 currentColor,
                                 rotationRad,
                                 pivotX,
                                 pivotY,
                                 scale
                              );
                              cursorX += fallback.xAdvance * scale;
                           } else {
                              cursorX += size * 0.5F;
                           }

                           if (this.batchedChars >= 256) {
                              this.flush();
                              this.currentAtlas = atlas;
                           }

                           i += charCount;
                        }
                     }
                  }
               }

               if (!DrawBatcher.isEnabled()) {
                  this.flush();
               }
            }
         }
      }
   }

   @Override
   public int batchLayer() {
      return 3;
   }

   private void sealChunk() {
      if (this.batchedChars != 0 && this.currentAtlas != null) {
         class_310 client = class_310.method_1551();
         if (client != null && client.method_1522() != null) {
            class_1044 texture = client.method_1531().method_4619(this.currentAtlas.getTextureId());
            if (texture == null) {
               this.batchedChars = 0;
               this.currentAtlas = null;
               if (this.dataBuffer != null) {
                  this.dataBuffer.limit(this.dataBuffer.capacity());
                  this.dataBuffer.clear();
                  this.dataBuffer.position(64);
               }
            } else {
               GpuTexture gpuTexture;
               try {
                  gpuTexture = texture.method_68004();
               } catch (Exception var11) {
                  this.batchedChars = 0;
                  this.currentAtlas = null;
                  if (this.dataBuffer != null) {
                     this.dataBuffer.limit(this.dataBuffer.capacity());
                     this.dataBuffer.clear();
                     this.dataBuffer.position(64);
                  }

                  return;
               }

               int count = this.batchedChars;
               int endPosition = this.dataBuffer.position();
               this.dataBuffer.position(0);
               this.dataBuffer.putFloat((float)this.getFixedScaledWidth());
               this.dataBuffer.putFloat((float)this.getFixedScaledHeight());
               this.dataBuffer.putFloat(2.0F);
               this.dataBuffer.putFloat(this.currentOutlineWidth);
               this.dataBuffer.putFloat((float)(this.currentOutlineColor >> 16 & 0xFF) / 255.0F);
               this.dataBuffer.putFloat((float)(this.currentOutlineColor >> 8 & 0xFF) / 255.0F);
               this.dataBuffer.putFloat((float)(this.currentOutlineColor & 0xFF) / 255.0F);
               this.dataBuffer.putFloat((float)(this.currentOutlineColor >> 24 & 0xFF) / 255.0F);
               this.dataBuffer.putFloat(this.currentAtlas.getAtlasWidth());
               this.dataBuffer.putFloat(this.currentAtlas.getAtlasHeight());
               this.dataBuffer.putFloat(this.currentAtlas.getDistanceRange());
               this.dataBuffer.putFloat(this.currentAtlas.getFontSize());
               this.dataBuffer.putInt(count);
               this.dataBuffer.putInt(0);
               this.dataBuffer.putInt(0);
               this.dataBuffer.putInt(0);
               this.dataBuffer.position(0);
               this.dataBuffer.limit(endPosition);

               try {
                  GpuBuffer uniformBuffer = this.nextUniformBuffer();
                  RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uniformBuffer.slice(0L, (long)this.dataBuffer.remaining()), this.dataBuffer);
                  this.chunks.add(new FontPipeline.FontChunk(uniformBuffer, this.getCachedTextureView(gpuTexture), count));
               } finally {
                  this.dataBuffer.limit(this.dataBuffer.capacity());
                  this.dataBuffer.clear();
                  this.dataBuffer.position(64);
                  this.batchedChars = 0;
                  this.currentAtlas = null;
               }
            }
         } else {
            this.batchedChars = 0;
            this.currentAtlas = null;
            if (this.dataBuffer != null) {
               this.dataBuffer.limit(this.dataBuffer.capacity());
               this.dataBuffer.clear();
               this.dataBuffer.position(64);
            }
         }
      } else {
         this.batchedChars = 0;
         this.currentAtlas = null;
         if (this.dataBuffer != null) {
            this.dataBuffer.limit(this.dataBuffer.capacity());
            this.dataBuffer.clear();
            this.dataBuffer.position(64);
         }
      }
   }

   @Override
   public void uploadBatch(CommandEncoder encoder) {
      this.sealChunk();
   }

   @Override
   public void drawBatch(RenderPass pass) {
      if (!this.chunks.isEmpty()) {
         class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         pass.setPipeline(PIPELINE);
         pass.setVertexBuffer(0, this.dummyVertexBuffer);

         for (FontPipeline.FontChunk chunk : this.chunks) {
            if (!chunk.view().isClosed()) {
               pass.bindTexture("Sampler0", chunk.view(), sampler);
               pass.setUniform("FontData", chunk.buffer());
               pass.draw(0, chunk.count() * 6);
            }
         }

         this.chunks.clear();
      }
   }

   @Override
   public void discardBatch() {
      this.chunks.clear();
      this.batchedChars = 0;
      this.currentAtlas = null;
      if (this.dataBuffer != null) {
         this.dataBuffer.limit(this.dataBuffer.capacity());
         this.dataBuffer.clear();
         this.dataBuffer.position(64);
      }
   }

   public void flush() {
      this.sealChunk();
      if (!DrawBatcher.isEnabled() && !this.chunks.isEmpty()) {
         DrawBatcher.drawImmediate(this, false);
      }
   }

   private GpuTextureView getCachedTextureView(GpuTexture gpuTexture) {
      GpuTextureView view = this.textureViewCache.get(gpuTexture);
      if (view == null || view.isClosed()) {
         this.textureViewCache.entrySet().removeIf(e -> {
            if (!e.getKey().isClosed() && !e.getValue().isClosed()) {
               return false;
            } else {
               if (!e.getValue().isClosed()) {
                  e.getValue().close();
               }

               return true;
            }
         });
         view = RenderSystem.getDevice().createTextureView(gpuTexture);
         this.textureViewCache.put(gpuTexture, view);
      }

      return view;
   }

   public float getTextWidth(FontAtlas atlas, String text, float size) {
      atlas.ensureLoaded();
      float scale = size / atlas.getFontSize();
      text = this.preprocessText(text);
      Float cached = atlas.widthCache.get(text);
      if (cached != null) {
         return cached * scale;
      } else {
         String stripped = ColorFormatting.stripForWidth(text);
         Float base = stripped.equals(text) ? null : atlas.widthCache.get(stripped);
         if (base == null) {
            base = this.computeBaseWidth(atlas, stripped);
            atlas.widthCache.put(stripped, base);
         }

         atlas.widthCache.put(text, base);
         return base * scale;
      }
   }

   private float computeBaseWidth(FontAtlas atlas, String text) {
      float width = 0.0F;
      float maxWidth = 0.0F;
      int i = 0;

      while (i < text.length()) {
         FontPipeline.ColorAdvance colorAdvance = this.trySkipColorCode(text, i);
         if (colorAdvance.matched()) {
            i += colorAdvance.skip();
         } else {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            if (codePoint == 10) {
               maxWidth = Math.max(maxWidth, width);
               width = 0.0F;
               i += charCount;
            } else {
               Glyph glyph = atlas.getGlyph(codePoint);
               Glyph fallback;
               float var11;
               float var12;
               float var13;
               width = glyph != null
                  ? (var11 = width + glyph.xAdvance)
                  : ((fallback = atlas.getGlyph(63)) != null ? (var12 = width + fallback.xAdvance) : (var13 = width + atlas.getFontSize() * 0.5F));
               i += charCount;
            }
         }
      }

      return Math.max(maxWidth, width);
   }

   public float getTextHeight(FontAtlas atlas, String text, float size) {
      atlas.ensureLoaded();
      float scale = size / atlas.getFontSize();
      int lines = 1;

      for (int i = 0; i < text.length(); i++) {
         if (text.charAt(i) == '\n') {
            lines++;
         }
      }

      return (float)lines * atlas.getLineHeight() * scale;
   }

   private String preprocessText(String text) {
      String cached = preprocessCache.get(text);
      if (cached != null) {
         return cached;
      } else {
         StringBuilder sb = null;
         int i = 0;

         while (i < text.length()) {
            int cp = text.codePointAt(i);
            int cc = Character.charCount(cp);
            if (cp == LIGHTNING_CP) {
               if (sb == null) {
                  sb = new StringBuilder(text.substring(0, i));
               }
            } else {
               Integer rep = SMALL_CAPS_MAP.get(cp);
               if (rep != null) {
                  if (sb == null) {
                     sb = new StringBuilder(text.substring(0, i));
                  }

                  sb.appendCodePoint(rep);
               } else if (sb != null) {
                  sb.appendCodePoint(cp);
               }
            }

            i += cc;
         }

         String result = sb != null ? sb.toString() : text;
         preprocessCache.put(text, result);
         return result;
      }
   }

   private FontPipeline.ColorAdvance tryParseColorCode(String text, int index, int defaultColor, int currentColor) {
      ColorFormatting.ColorTag tag = ColorFormatting.parseTag(text, index, defaultColor);
      if (tag != null) {
         return new FontPipeline.ColorAdvance(tag.length(), tag.color(), true);
      } else {
         int codePoint = text.codePointAt(index);
         int charCount = Character.charCount(codePoint);
         if ((codePoint == 167 || codePoint == 38) && index + charCount < text.length()) {
            int nextCodePoint = text.codePointAt(index + charCount);
            if (nextCodePoint == 35 && index + charCount + 6 < text.length()) {
               try {
                  String hex = text.substring(index + charCount + 1, index + charCount + 7);
                  int parsed = 0xFF000000 | Integer.parseInt(hex, 16);
                  return new FontPipeline.ColorAdvance(charCount + 7, parsed, true);
               } catch (Exception var12) {
               }
            }

            int code;
            if ((code = LEGACY_CODE_CHARS.indexOf(Character.toLowerCase((char)nextCodePoint))) >= 0) {
               int parsed = code < 16 ? LEGACY_COLORS[code] : (code == 21 ? defaultColor : currentColor);
               return new FontPipeline.ColorAdvance(charCount + Character.charCount(nextCodePoint), parsed, true);
            }
         }

         return new FontPipeline.ColorAdvance(0, currentColor, false);
      }
   }

   private FontPipeline.ColorAdvance trySkipColorCode(String text, int index) {
      return this.tryParseColorCode(text, index, 0, 0);
   }

   public void close() {
      this.batchedChars = 0;
      this.currentAtlas = null;
      this.discardBatch();

      for (GpuTextureView view : this.textureViewCache.values()) {
         if (!view.isClosed()) {
            view.close();
         }
      }

      this.textureViewCache.clear();
      if (this.uniformBuffers != null) {
         for (GpuBuffer buf : this.uniformBuffers) {
            if (buf != null) {
               buf.close();
            }
         }

         this.uniformBuffers = null;
      }

      if (this.dummyVertexBuffer != null) {
         this.dummyVertexBuffer.close();
         this.dummyVertexBuffer = null;
      }

      if (this.dataBuffer != null) {
         MemoryUtil.memFree(this.dataBuffer);
         this.dataBuffer = null;
      }

      this.initialized = false;
   }

   static {
      for (int i = 0; i < 16; i++) {
         int j = (i >> 3 & 1) * 85;
         int r = (i >> 2 & 1) * 170 + j;
         int g = (i >> 1 & 1) * 170 + j;
         int b = (i & 1) * 170 + j;
         if (i == 6) {
            r += 85;
         }

         LEGACY_COLORS[i] = 0xFF000000 | r << 16 | g << 8 | b;
         LEGACY_COLORS[i + 16] = (r & 16579836) >> 2 << 24 | r << 16 | g << 8 | b;
      }

      LEGACY_CODE_CHARS = new String(new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r'});
      preprocessCache = new LinkedHashMap<String, String>(257, 0.75F, true) {
         @Override
         protected boolean removeEldestEntry(Entry<String, String> eldest) {
            return this.size() > 256;
         }
      };
      SMALL_CAPS_MAP = new HashMap<>();
      SMALL_CAPS_MAP.put(7424, 97);
      SMALL_CAPS_MAP.put(665, 98);
      SMALL_CAPS_MAP.put(7428, 99);
      SMALL_CAPS_MAP.put(7429, 100);
      SMALL_CAPS_MAP.put(7431, 101);
      SMALL_CAPS_MAP.put(42800, 102);
      SMALL_CAPS_MAP.put(610, 103);
      SMALL_CAPS_MAP.put(668, 104);
      SMALL_CAPS_MAP.put(618, 105);
      SMALL_CAPS_MAP.put(7434, 106);
      SMALL_CAPS_MAP.put(7435, 107);
      SMALL_CAPS_MAP.put(671, 108);
      SMALL_CAPS_MAP.put(7437, 109);
      SMALL_CAPS_MAP.put(628, 110);
      SMALL_CAPS_MAP.put(7439, 111);
      SMALL_CAPS_MAP.put(7448, 112);
      SMALL_CAPS_MAP.put(42927, 113);
      SMALL_CAPS_MAP.put(640, 114);
      SMALL_CAPS_MAP.put(7451, 116);
      SMALL_CAPS_MAP.put(7452, 117);
      SMALL_CAPS_MAP.put(7456, 118);
      SMALL_CAPS_MAP.put(7457, 119);
      SMALL_CAPS_MAP.put(655, 121);
      SMALL_CAPS_MAP.put(7458, 122);
      LIGHTNING_CP = "⚡".codePointAt(0);
   }

   @Environment(EnvType.CLIENT)
   private static record ColorAdvance(int skip, int color, boolean matched) {
   }

   @Environment(EnvType.CLIENT)
   private static record FontChunk(GpuBuffer buffer, GpuTextureView view, int count) {
   }
}
