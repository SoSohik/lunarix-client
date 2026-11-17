package ru.mentions.utils.render;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_1011.class_1012;
import org.w3c.dom.Node;

@Environment(EnvType.CLIENT)
public class GifTexture {
   private final List<class_2960> frameIds = new ArrayList<>();
   private final List<Integer> delays = new ArrayList<>();
   private int totalDuration = 0;
   private long startTime = -1L;
   private volatile boolean loaded = false;
   private int width = 0;
   private int height = 0;

   public GifTexture(InputStream stream) {
      this.load(stream);
   }

   private void load(InputStream stream) {
      try {
         ImageInputStream iis = ImageIO.createImageInputStream(stream);
         Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
         if (!readers.hasNext()) {
            return;
         }

         ImageReader reader = readers.next();
         reader.setInput(iis, false);
         int frameCount = reader.getNumImages(true);
         String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
         IIOMetadataNode globalMeta = null;

         try {
            globalMeta = (IIOMetadataNode)reader.getStreamMetadata().getAsTree(reader.getStreamMetadata().getNativeMetadataFormatName());
         } catch (Exception var29) {
         }

         BufferedImage firstFrame = reader.read(0);
         int canvasW = firstFrame.getWidth();
         int canvasH = firstFrame.getHeight();
         IIOMetadataNode ld;
         if (globalMeta != null && (ld = this.findNode(globalMeta, "LogicalScreenDescriptor")) != null) {
            try {
               canvasW = Integer.parseInt(ld.getAttribute("logicalScreenWidth"));
            } catch (Exception var28) {
            }

            try {
               canvasH = Integer.parseInt(ld.getAttribute("logicalScreenHeight"));
            } catch (Exception var27) {
            }
         }

         this.width = canvasW;
         this.height = canvasH;
         ArrayList<int[]> pixelData = new ArrayList<>();
         BufferedImage canvas = new BufferedImage(canvasW, canvasH, 2);
         BufferedImage prevCanvas = null;

         for (int i = 0; i < frameCount; i++) {
            BufferedImage rawFrame = i == 0 ? firstFrame : reader.read(i);
            IIOMetadataNode frameMeta = null;

            try {
               frameMeta = (IIOMetadataNode)reader.getImageMetadata(i).getAsTree(reader.getImageMetadata(i).getNativeMetadataFormatName());
            } catch (Exception var26) {
            }

            int fx = 0;
            int fy = 0;
            String disposal = "none";
            if (frameMeta != null) {
               IIOMetadataNode id = this.findNode(frameMeta, "ImageDescriptor");
               if (id != null) {
                  try {
                     fx = Integer.parseInt(id.getAttribute("imageLeftPosition"));
                  } catch (Exception var25) {
                  }

                  try {
                     fy = Integer.parseInt(id.getAttribute("imageTopPosition"));
                  } catch (Exception var24) {
                  }
               }

               IIOMetadataNode gce;
               if ((gce = this.findNode(frameMeta, "GraphicControlExtension")) != null) {
                  disposal = gce.getAttribute("disposalMethod");
               }
            }

            if ("restoreToPrevious".equalsIgnoreCase(disposal)) {
               prevCanvas = this.copyImage(canvas);
            }

            Graphics2D g = canvas.createGraphics();
            g.drawImage(rawFrame, fx, fy, null);
            g.dispose();
            pixelData.add(canvas.getRGB(0, 0, canvasW, canvasH, null, 0, canvasW));
            this.delays.add(this.readDelay(reader, i));
            this.frameIds.add(class_2960.method_60655("white", "gif/" + uid + "/" + i));
            Graphics2D gc = canvas.createGraphics();
            gc.setComposite(AlphaComposite.Clear);
            if ("restoreToBackgroundColor".equalsIgnoreCase(disposal)) {
               gc.fillRect(fx, fy, rawFrame.getWidth(), rawFrame.getHeight());
            } else if ("restoreToPrevious".equalsIgnoreCase(disposal) && prevCanvas != null) {
               gc.setComposite(AlphaComposite.Src);
               gc.drawImage(prevCanvas, 0, 0, null);
            }

            gc.dispose();
         }

         reader.dispose();

         for (int d : this.delays) {
            this.totalDuration += d;
         }

         int uploadW = canvasW;
         int uploadH = canvasH;
         class_310.method_1551().execute(() -> {
            int i = 0;

            while (i < pixelData.size()) {
               int[] px = pixelData.get(i);
               class_1011 img = new class_1011(class_1012.field_4997, uploadW, uploadH, false);

               for (int y = 0; y < uploadH; y++) {
                  for (int x = 0; x < uploadW; x++) {
                     img.method_61941(x, y, px[y * uploadW + x]);
                  }
               }

               int idx = i++;
               class_1043 tex = new class_1043(() -> "white:gif_" + uid + "_" + idx, img);
               tex.method_4524();
               class_310.method_1551().method_1531().method_4616(this.frameIds.get(idx), tex);
            }

            this.loaded = true;
         });
      } catch (Exception var30) {
         var30.printStackTrace();
      }
   }

   private int readDelay(ImageReader reader, int index) {
      try {
         IIOMetadataNode root = (IIOMetadataNode)reader.getImageMetadata(index).getAsTree(reader.getImageMetadata(index).getNativeMetadataFormatName());
         IIOMetadataNode gce = this.findNode(root, "GraphicControlExtension");
         if (gce != null) {
            int cs = Integer.parseInt(gce.getAttribute("delayTime"));
            return Math.max(cs * 10, 20);
         }
      } catch (Exception var6) {
      }

      return 100;
   }

   private IIOMetadataNode findNode(IIOMetadataNode root, String name) {
      for (int i = 0; i < root.getLength(); i++) {
         if (root.item(i).getNodeName().equalsIgnoreCase(name)) {
            return (IIOMetadataNode)root.item(i);
         }

         Node node = root.item(i);
         IIOMetadataNode child;
         IIOMetadataNode found;
         if (node instanceof IIOMetadataNode && (found = this.findNode(child = (IIOMetadataNode)node, name)) != null) {
            return found;
         }
      }

      return null;
   }

   private BufferedImage copyImage(BufferedImage src) {
      BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), 2);
      Graphics2D g = copy.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.dispose();
      return copy;
   }

   public class_2960 currentFrame() {
      if (this.loaded && !this.frameIds.isEmpty()) {
         if (this.frameIds.size() == 1) {
            return this.frameIds.get(0);
         } else {
            if (this.startTime == -1L) {
               this.startTime = System.currentTimeMillis();
            }

            long elapsed = (System.currentTimeMillis() - this.startTime) % (long)this.totalDuration;
            int acc = 0;

            for (int i = 0; i < this.frameIds.size(); i++) {
               if (elapsed < (long)(acc += this.delays.get(i))) {
                  return this.frameIds.get(i);
               }
            }

            return this.frameIds.get(this.frameIds.size() - 1);
         }
      } else {
         return null;
      }
   }

   public void draw(class_332 ctx, int x, int y, int w, int h) {
      this.draw(ctx, x, y, w, h, 1.0F);
   }

   public void draw(class_332 ctx, int x, int y, int w, int h, float alpha) {
      int a = (int)(Math.min(Math.max(alpha, 0.0F), 1.0F) * 255.0F) & 0xFF;
      this.draw(ctx, x, y, w, h, a << 24 | 16777215);
   }

   public void draw(class_332 ctx, int x, int y, int w, int h, int color) {
      class_2960 frame = this.currentFrame();
      if (frame != null) {
         ctx.method_25291(class_10799.field_56883, frame, x, y, 0.0F, 0.0F, w, h, w, h, color);
      }
   }

   public void reset() {
      this.startTime = System.currentTimeMillis();
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void destroy() {
      class_310 mc = class_310.method_1551();
      ArrayList<class_2960> copy = new ArrayList<>(this.frameIds);
      mc.execute(() -> copy.forEach(id -> mc.method_1531().method_4615(id)));
      this.frameIds.clear();
      this.delays.clear();
      this.loaded = false;
   }
}
