package ru.mentions.module.impl.utils;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1531;
import net.minecraft.class_1921;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2818;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Warden Helper",
   desc = "Отслеживание сундуков в Варден зоне: таймеры, ESP и неймтеги",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class WardenHelper extends Module {
   private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
   private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+)\\s*(с|s|сек|sec)");
   private final Map<class_2338, WardenHelper.ChestData> trackedChests = new ConcurrentHashMap<>();
   private final Set<class_2338> gpsNotified = new HashSet<>();
   private final Set<class_2338> chatNotified = new HashSet<>();
   private static final int COLOR_RED = ColorUtil.getColor(255, 60, 60, 180);
   private static final int COLOR_YELLOW = ColorUtil.getColor(255, 220, 50, 180);
   private static final int COLOR_GREEN = ColorUtil.getColor(60, 255, 60, 180);
   private static final int COLOR_UNKNOWN = ColorUtil.getColor(180, 180, 180, 120);
   private final class_9799 allocator = new class_9799(262144);
   private static final RenderPipeline BOX_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "warden_helper_esp"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 BOX_LAYER = class_1921.method_75940(
      "warden_helper_esp", class_12247.method_75927(BOX_PIPELINE).method_75929(4096).method_75938()
   );

   @Override
   public void onEnable() {
      super.onEnable();
      this.trackedChests.clear();
      this.gpsNotified.clear();
      this.chatNotified.clear();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.trackedChests.clear();
      this.gpsNotified.clear();
      this.chatNotified.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.trackedChests.clear();
      this.gpsNotified.clear();
      this.chatNotified.clear();
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.isInWardenZone()) {
            this.updateTimersFromHolograms();
         }
      }
   }

   private boolean isInWardenZone() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         double x = mc.field_1724.method_23317();
         double z = mc.field_1724.method_23321();
         return Math.abs(Math.abs(x) - 2000.0) <= 150.0 && Math.abs(Math.abs(z) - 2000.0) <= 150.0;
      }
   }

   private boolean isChestInZone(class_2338 pos) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         double x = (double)pos.method_10263();
         double y = (double)pos.method_10264();
         double z = (double)pos.method_10260();
         boolean inXZ = Math.abs(Math.abs(x) - 2000.0) <= 150.0 && Math.abs(Math.abs(z) - 2000.0) <= 150.0;
         boolean inY = Math.abs(y - mc.field_1724.method_23318()) <= 30.0;
         return inXZ && inY;
      }
   }

   private void updateTimersFromHolograms() {
      for (Entry<class_2338, WardenHelper.ChestData> entry : this.trackedChests.entrySet()) {
         class_2338 pos = entry.getKey();
         WardenHelper.ChestData data = entry.getValue();
         class_238 searchBox = new class_238(pos).method_1009(1.0, 3.0, 1.0);
         List<class_1531> stands = mc.field_1687.method_8390(class_1531.class, searchBox, e -> e.method_16914());
         boolean foundTimer = false;

         for (class_1531 stand : stands) {
            String name = stand.method_5797().getString();
            String clean = ColorUtil.removeFormatting(name);
            long seconds;
            if (clean != null && (seconds = this.parseTimeToSeconds(clean)) >= 0L) {
               data.updateFromHologram(seconds);
               foundTimer = true;
               break;
            }
         }

         if (!foundTimer) {
            data.hologramVisible = false;
         }
      }
   }

   private long parseTimeToSeconds(String text) {
      Matcher timeMatcher = TIME_PATTERN.matcher(text);
      if (timeMatcher.find()) {
         if (timeMatcher.group(3) != null) {
            int hours = Integer.parseInt(timeMatcher.group(1));
            int minutes = Integer.parseInt(timeMatcher.group(2));
            int seconds = Integer.parseInt(timeMatcher.group(3));
            return (long)hours * 3600L + (long)minutes * 60L + (long)seconds;
         } else {
            int minutes = Integer.parseInt(timeMatcher.group(1));
            int seconds = Integer.parseInt(timeMatcher.group(2));
            return (long)minutes * 60L + (long)seconds;
         }
      } else {
         Matcher secMatcher = SECONDS_PATTERN.matcher(text);
         if (secMatcher.find()) {
            return Long.parseLong(secMatcher.group(1));
         } else {
            String stripped = text.replaceAll("[^0-9:]", "").trim();
            long val;
            return !stripped.isEmpty() && stripped.matches("\\d+") && (val = Long.parseLong(stripped)) > 0L && val < 36000L ? val : -1L;
         }
      }
   }

   @EventHandler(
      priority = -500
   )
   public void onRender3D(EventRender3D event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.isInWardenZone()) {
            Matrix4f matrix = event.getMatrixStack().method_23760().method_23761();
            class_243 camPos = mc.field_1773.method_19418().method_71156();
            class_4598 immediate = class_4597.method_22991(this.allocator);
            class_4588 buffer = immediate.method_73477(BOX_LAYER);
            class_1923 playerChunkPos = mc.field_1724.method_31476();
            int renderDistance = (Integer)mc.field_1690.method_42503().method_41753();

            for (int chunkX = playerChunkPos.field_9181 - renderDistance; chunkX <= playerChunkPos.field_9181 + renderDistance; chunkX++) {
               for (int chunkZ = playerChunkPos.field_9180 - renderDistance; chunkZ <= playerChunkPos.field_9180 + renderDistance; chunkZ++) {
                  class_2818 chunk = mc.field_1687.method_8497(chunkX, chunkZ);
                  if (chunk != null) {
                     for (class_2586 blockEntity : chunk.method_12214().values()) {
                        class_2338 pos;
                        if (blockEntity instanceof class_2595 && this.isChestInZone(pos = blockEntity.method_11016())) {
                           if (!this.trackedChests.containsKey(pos)) {
                              this.trackedChests.put(pos, new WardenHelper.ChestData(pos));
                           }

                           WardenHelper.ChestData data;
                           if ((data = this.trackedChests.get(pos)).hasTimer()) {
                              int color = this.getTimerColor(data);
                              float x1 = (float)((double)pos.method_10263() - camPos.field_1352);
                              float y1 = (float)((double)pos.method_10264() - camPos.field_1351);
                              float z1 = (float)((double)pos.method_10260() - camPos.field_1350);
                              int cB = ColorUtil.replAlpha(color, 0.8F);
                              int cT = ColorUtil.replAlpha(color, 0.0F);
                              this.drawGradientBox(buffer, matrix, x1, y1, z1, x1 + 1.0F, y1 + 1.0F, z1 + 1.0F, cB, cT);
                           }
                        }
                     }
                  }
               }
            }

            immediate.method_22993();
         }
      }
   }

   private String formatSeconds(int totalSec) {
      if (totalSec <= 0) {
         return "0:00";
      } else {
         int m = totalSec / 60;
         int s = totalSec % 60;
         return String.format("%d:%02d", m, s);
      }
   }

   private int getTimerColor(WardenHelper.ChestData data) {
      if (!data.hasTimer()) {
         return COLOR_UNKNOWN;
      } else {
         float remaining = data.getRemainingSeconds();
         if (remaining <= 0.0F) {
            float pulse = (float)(Math.sin((double)System.currentTimeMillis() / 200.0) * 0.3 + 0.7);
            int g = (int)(255.0F * pulse);
            return ColorUtil.getColor(0, g, (int)(128.0F * pulse), 220);
         } else if (remaining > 120.0F) {
            return COLOR_RED;
         } else if (remaining > 20.0F) {
            float factor = 1.0F - (remaining - 20.0F) / 100.0F;
            return ColorUtil.interpolateColor(COLOR_RED, COLOR_YELLOW, factor);
         } else {
            float factor = 1.0F - remaining / 20.0F;
            return ColorUtil.interpolateColor(COLOR_YELLOW, COLOR_GREEN, factor);
         }
      }
   }

   private void drawGradientBox(class_4588 b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int cB, int cT) {
      b.method_22918(m, x1, y1, z1).method_39415(cB);
      b.method_22918(m, x2, y1, z1).method_39415(cB);
      b.method_22918(m, x2, y1, z2).method_39415(cB);
      b.method_22918(m, x1, y1, z2).method_39415(cB);
      b.method_22918(m, x1, y2, z1).method_39415(cT);
      b.method_22918(m, x1, y2, z2).method_39415(cT);
      b.method_22918(m, x2, y2, z2).method_39415(cT);
      b.method_22918(m, x2, y2, z1).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cB);
      b.method_22918(m, x1, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y1, z1).method_39415(cB);
      b.method_22918(m, x1, y1, z2).method_39415(cB);
      b.method_22918(m, x2, y1, z2).method_39415(cB);
      b.method_22918(m, x2, y2, z2).method_39415(cT);
      b.method_22918(m, x1, y2, z2).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cB);
      b.method_22918(m, x1, y1, z2).method_39415(cB);
      b.method_22918(m, x1, y2, z2).method_39415(cT);
      b.method_22918(m, x1, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y1, z1).method_39415(cB);
      b.method_22918(m, x2, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y2, z2).method_39415(cT);
      b.method_22918(m, x2, y1, z2).method_39415(cB);
   }

   @Environment(EnvType.CLIENT)
   private static class ChestData {
      final class_2338 pos;
      long readyAtMs = -1L;
      long lastHologramUpdateMs = 0L;
      boolean hologramVisible = false;
      long lastReadSeconds = -1L;

      ChestData(class_2338 pos) {
         this.pos = pos;
      }

      void updateFromHologram(long remainingSeconds) {
         this.hologramVisible = true;
         this.lastHologramUpdateMs = System.currentTimeMillis();
         long newReadyAt = System.currentTimeMillis() + remainingSeconds * 1000L;
         if (this.readyAtMs == -1L) {
            this.readyAtMs = newReadyAt;
            this.lastReadSeconds = remainingSeconds;
         } else {
            float currentEstimate = this.getRemainingSeconds();
            float diff = Math.abs(currentEstimate - (float)remainingSeconds);
            if (diff > 3.0F) {
               this.readyAtMs = newReadyAt;
            }

            this.lastReadSeconds = remainingSeconds;
         }
      }

      boolean hasTimer() {
         return this.readyAtMs > 0L;
      }

      float getRemainingSeconds() {
         if (this.readyAtMs <= 0L) {
            return -1.0F;
         } else {
            float remaining = (float)(this.readyAtMs - System.currentTimeMillis()) / 1000.0F;
            return Math.max(0.0F, remaining);
         }
      }

      void reset() {
         this.readyAtMs = -1L;
         this.hologramVisible = false;
         this.lastReadSeconds = -1L;
      }
   }
}
