package ru.mentions.utils.render;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_1011.class_1012;

@Environment(EnvType.CLIENT)
public class UrlImageTexture {
   private static final Map<String, UrlImageTexture> CACHE = new ConcurrentHashMap<>();
   private final String url;
   private final class_2960 id;
   private volatile boolean loading = false;
   private volatile boolean loaded = false;
   private volatile boolean failed = false;
   private int width = 0;
   private int height = 0;

   private UrlImageTexture(String url) {
      this.url = url;
      this.id = class_2960.method_60655("white", "url_images/" + Integer.toHexString(url.hashCode()));
   }

   public static UrlImageTexture get(String url) {
      return CACHE.computeIfAbsent(url, UrlImageTexture::new);
   }

   public static class_2960 getIdentifier(String url) {
      UrlImageTexture texture = get(url);
      texture.loadAsync();
      return texture.isLoaded() ? texture.id : null;
   }

   public void loadAsync() {
      if (!this.loaded && !this.loading && !this.failed) {
         this.loading = true;
         Thread thread = new Thread(() -> {
            try {
               if (isSupportedImageUrl(this.url)) {
                  URL target = URI.create(this.url).toURL();
                  HttpURLConnection connection = (HttpURLConnection)target.openConnection();
                  connection.setConnectTimeout(5000);
                  connection.setReadTimeout(8000);
                  connection.setRequestProperty("User-Agent", "WhiteClient/1.0");

                  try (InputStream stream = connection.getInputStream()) {
                     BufferedImage image = ImageIO.read(stream);
                     if (image != null) {
                        int uploadWidth = image.getWidth();
                        int uploadHeight = image.getHeight();
                        int[] pixels = image.getRGB(0, 0, uploadWidth, uploadHeight, null, 0, uploadWidth);
                        class_310.method_1551().execute(() -> this.upload(uploadWidth, uploadHeight, pixels));
                        return;
                     }

                     this.failed = true;
                     return;
                  } finally {
                     connection.disconnect();
                  }
               }

               this.failed = true;
            } catch (Exception var23) {
               this.failed = true;
               return;
            } finally {
               this.loading = false;
            }
         }, "url-image-loader");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private void upload(int uploadWidth, int uploadHeight, int[] pixels) {
      if (!this.loaded) {
         class_1011 nativeImage = new class_1011(class_1012.field_4997, uploadWidth, uploadHeight, false);

         for (int y = 0; y < uploadHeight; y++) {
            for (int x = 0; x < uploadWidth; x++) {
               nativeImage.method_61941(x, y, pixels[y * uploadWidth + x]);
            }
         }

         class_1043 texture = new class_1043(() -> this.id.toString(), nativeImage);
         texture.method_4524();
         class_310.method_1551().method_1531().method_4616(this.id, texture);
         this.width = uploadWidth;
         this.height = uploadHeight;
         this.loaded = true;
         this.failed = false;
      }
   }

   private static boolean isSupportedImageUrl(String url) {
      String lower = url.toLowerCase(Locale.ROOT);
      int query = lower.indexOf(63);
      if (query >= 0) {
         lower = lower.substring(0, query);
      }

      return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
   }

   public class_2960 getId() {
      this.loadAsync();
      return this.loaded ? this.id : null;
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public boolean isFailed() {
      return this.failed;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void destroy() {
      class_310.method_1551().execute(() -> class_310.method_1551().method_1531().method_4615(this.id));
      this.loaded = false;
      this.failed = false;
   }

   public static void clearCache() {
      CACHE.values().forEach(UrlImageTexture::destroy);
      CACHE.clear();
   }
}
