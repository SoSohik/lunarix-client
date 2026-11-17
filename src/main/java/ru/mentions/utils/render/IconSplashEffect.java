package ru.mentions.utils.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public class IconSplashEffect {
   private static final List<IconSplashEffect.SplashParticle> particles = new ArrayList<>();

   public static void spawn(float originX, float originY, int count, int accentColor) {
      ThreadLocalRandom rnd = ThreadLocalRandom.current();

      for (int i = 0; i < count; i++) {
         float angle = rnd.nextFloat() * (float) (Math.PI * 2);
         float speed = 0.18F + rnd.nextFloat() * 0.45F;
         float vx = (float)Math.cos((double)angle) * speed;
         float vy = (float)Math.sin((double)angle) * speed - 0.1F;
         float size = 3.5F + rnd.nextFloat() * 3.5F;
         int maxAge = 50 + rnd.nextInt(35);
         particles.add(new IconSplashEffect.SplashParticle(originX, originY, vx, vy, size, maxAge, accentColor));
      }
   }

   public static void updateAndRender(float globalAlpha) {
      if (!particles.isEmpty() && !(globalAlpha <= 0.01F)) {
         Iterator<IconSplashEffect.SplashParticle> it = particles.iterator();

         while (it.hasNext()) {
            IconSplashEffect.SplashParticle p = it.next();
            p.update();
            if (p.isDead()) {
               it.remove();
            } else {
               p.render(globalAlpha);
            }
         }
      }
   }

   public static void clear() {
      particles.clear();
   }

   public static class SplashParticle {
      public float x;
      public float y;
      public float vx;
      public float vy;
      public float size;
      public int maxAge;
      public int age;
      public int accentColor;

      public SplashParticle(float x, float y, float vx, float vy, float size, int maxAge, int accentColor) {
         this.x = x;
         this.y = y;
         this.vx = vx;
         this.vy = vy;
         this.size = size;
         this.maxAge = maxAge;
         this.age = 0;
         this.accentColor = accentColor;
      }

      public void update() {
         this.x = this.x + this.vx;
         this.y = this.y + this.vy;
         this.vy += 0.008F;
         this.vx *= 0.955F;
         this.vy *= 0.955F;
         this.age++;
      }

      public boolean isDead() {
         return this.age >= this.maxAge;
      }

      public void render(float globalAlpha) {
         float progress = (float)this.age / (float)this.maxAge;
         float alpha = (1.0F - progress) * globalAlpha;
         if (!(alpha <= 0.01F)) {
            float currentSize = this.size * (0.85F + (1.0F - progress) * 0.35F);
            float half = currentSize / 2.0F;
            int baseWhite = ColorUtil.getColor(255, 255, 255, alpha * 0.95F);
            int tint = ColorUtil.replAlpha(this.accentColor, alpha * 0.85F);
            int glow = ColorUtil.replAlpha(this.accentColor, alpha * 0.4F);
            RenderUtil.Render2D.glow(this.x - half, this.y - half, currentSize, currentSize, glow, half, 4.0F, 1.0F);
            RenderUtil.Render2D.rect(this.x - half * 0.6F, this.y - half * 0.6F, currentSize * 0.6F, currentSize * 0.6F, tint, half * 0.6F);
            RenderUtil.Render2D.rect(this.x - half * 0.25F, this.y - half * 0.25F, currentSize * 0.25F, currentSize * 0.25F, baseWhite, half * 0.25F);
         }
      }
   }
}
