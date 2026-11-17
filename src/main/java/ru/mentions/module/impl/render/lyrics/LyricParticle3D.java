package ru.mentions.module.impl.render.lyrics;

import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_4184;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;

public class LyricParticle3D {
   private final String text;
   private final class_243 basePosition;
   private final long spawnTimeMs;
   private final long durationMs;
   private final float floatHeight;
   private final float tiltSign;
   private boolean forceExpire = false;

   public LyricParticle3D(String text, class_243 basePosition, long spawnTimeMs, long durationMs, float floatHeight) {
      this.text = text;
      this.basePosition = basePosition;
      this.spawnTimeMs = spawnTimeMs;
      this.durationMs = durationMs;
      this.floatHeight = floatHeight;
      this.tiltSign = Math.random() > 0.5 ? 1.0F : -1.0F;
   }

   public class_243 getBasePosition() {
      return this.basePosition;
   }

   public void dismiss() {
      this.forceExpire = true;
   }

   public boolean isDead(long nowMs) {
      return this.forceExpire || nowMs - this.spawnTimeMs >= this.durationMs;
   }

   public String getText() {
      return this.text;
   }

   public void render(Font font, String animMode, int colorRgb, boolean showBackground, boolean checkOcclusion, float textScale, long nowMs) {
      long elapsed = nowMs - this.spawnTimeMs;
      if (elapsed >= 0L && elapsed <= this.durationMs && !this.forceExpire) {
         if (IMinecraft.mc.field_1724 != null && IMinecraft.mc.field_1687 != null && IMinecraft.mc.field_1773 != null) {
            float progress = class_3532.method_15363((float)elapsed / (float)this.durationMs, 0.0F, 1.0F);
            float alpha = 1.0F;
            double offsetY = 0.0;
            float animScale = 1.0F;
            String displayText = this.text;
            switch (animMode) {
               case "Typewriter":
                  if (progress < 0.3F) {
                     float typeProgress = progress / 0.3F;
                     int visibleChars = Math.min(this.text.length(), (int)Math.ceil((double)((float)this.text.length() * typeProgress)));
                     displayText = this.text.substring(0, Math.max(1, visibleChars));
                     alpha = Math.min(1.0F, progress / 0.08F);
                     offsetY = (double)progress * (double)this.floatHeight * 0.2;
                  } else if (progress < 0.75F) {
                     displayText = this.text;
                     alpha = 1.0F;
                     offsetY = (double)progress * (double)this.floatHeight * 0.2;
                  } else {
                     float eraseProgress = (progress - 0.75F) / 0.25F;
                     int remainingChars = Math.max(0, this.text.length() - (int)Math.floor((double)((float)this.text.length() * eraseProgress)));
                     displayText = this.text.substring(0, remainingChars);
                     alpha = 1.0F - eraseProgress * 0.7F;
                     offsetY = (double)progress * (double)this.floatHeight * 0.2;
                  }
                  break;
               case "PopScale":
                  if (progress < 0.18F) {
                     float t = progress / 0.18F;
                     alpha = easeOutCubic(t);
                     animScale = 0.35F + 0.65F * easeOutBack(t);
                     offsetY = -0.04 * (double)(1.0F - easeOutCubic(t));
                  } else if (progress < 0.8F) {
                     alpha = 1.0F;
                     animScale = 1.0F;
                     offsetY = 0.0;
                  } else {
                     float t = (progress - 0.8F) / 0.2F;
                     alpha = 1.0F - easeInCubic(t);
                     animScale = 1.0F - 0.05F * t;
                     offsetY = 0.03 * (double)easeInCubic(t);
                  }
                  break;
               case "KineticSlide":
                  if (progress < 0.2F) {
                     float t = progress / 0.2F;
                     alpha = easeOutCubic(t);
                     offsetY = -0.15 * (double)(1.0F - easeOutCubic(t));
                     animScale = 0.92F + 0.08F * t;
                  } else if (progress < 0.8F) {
                     alpha = 1.0F;
                     offsetY = 0.0;
                     animScale = 1.0F;
                  } else {
                     float t = (progress - 0.8F) / 0.2F;
                     alpha = 1.0F - easeInCubic(t);
                     offsetY = 0.05 * (double)easeInCubic(t);
                     animScale = 1.0F - 0.04F * t;
                  }
                  break;
               case "Fade":
                  if (progress < 0.15F) {
                     alpha = progress / 0.15F;
                  } else if (progress < 0.8F) {
                     alpha = 1.0F;
                  } else {
                     alpha = 1.0F - (progress - 0.8F) / 0.2F;
                  }
                  break;
               default:
                  if (progress < 0.15F) {
                     float t = progress / 0.15F;
                     alpha = easeOutCubic(t);
                     offsetY = -0.06 * (double)(1.0F - easeOutCubic(t));
                     animScale = 0.93F + 0.07F * easeOutBack(t);
                  } else if (progress < 0.8F) {
                     alpha = 1.0F;
                     offsetY = 0.0;
                     animScale = 1.0F;
                  } else {
                     float t = (progress - 0.8F) / 0.2F;
                     alpha = 1.0F - easeInCubic(t);
                     offsetY = 0.04 * (double)easeInCubic(t);
                     animScale = 1.0F - 0.03F * t;
                  }
            }

            alpha = class_3532.method_15363(alpha, 0.0F, 1.0F);
            if (!(alpha <= 0.01F) && displayText != null && !displayText.isBlank()) {
               class_243 curWorldPos = new class_243(this.basePosition.field_1352, this.basePosition.field_1351 + offsetY, this.basePosition.field_1350);
               if (Projection.isInFrontOfCamera(curWorldPos)) {
                  class_4184 camera = IMinecraft.mc.field_1773.method_19418();
                  if (camera != null) {
                     class_243 camPos = camera.method_71156();
                     double dist = camPos.method_1022(curWorldPos);
                     if (!(dist < 0.4) && !(dist > 35.0)) {
                        if (checkOcclusion) {
                           class_3959 raycast = new class_3959(camPos, curWorldPos, class_3960.field_17558, class_242.field_1348, IMinecraft.mc.field_1724);
                           class_239 hit = IMinecraft.mc.field_1687.method_17742(raycast);
                           if (hit != null && hit.method_17783() == class_240.field_1332) {
                              alpha *= 0.25F;
                              if (alpha <= 0.02F) {
                                 return;
                              }
                           }
                        }

                        class_243 screen = Projection.worldSpaceToScreenSpace(curWorldPos);
                        if (screen != null && !(screen.field_1350 < 0.0) && !(screen.field_1350 > 1.0)) {
                           float screenX = (float)screen.field_1352;
                           float screenY = (float)screen.field_1351;
                           float referenceDist = 3.0F;
                           float baseSize = 13.5F * Math.max(0.3F, textScale);
                           float scale = referenceDist / (float)Math.max(1.0, dist) * animScale;
                           float fontSize = class_3532.method_15363(baseSize * scale, 3.5F * textScale, 40.0F * textScale);
                           int finalColor = ColorUtil.replAlpha(colorRgb, alpha);
                           if (showBackground) {
                              float textW = font.getWidth(displayText, fontSize);
                              float textH = fontSize * 1.15F;
                              float rx = screenX - textW / 2.0F - 4.0F;
                              float ry = screenY - 1.5F;
                              RenderUtil.Render2D.rect(rx, ry, textW + 8.0F, textH + 3.0F, ColorUtil.getColor(12, 13, 16, (int)(alpha * 0.6F * 255.0F)), 3.0F);
                           }

                           font.drawCentered(displayText, screenX, screenY, fontSize, finalColor);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static float easeOutCubic(float t) {
      return 1.0F - (float)Math.pow((double)(1.0F - t), 3.0);
   }

   private static float easeInCubic(float t) {
      return (float)Math.pow((double)t, 3.0);
   }

   private static float easeOutBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow((double)(t - 1.0F), 3.0) + c1 * (float)Math.pow((double)(t - 1.0F), 2.0);
   }
}
