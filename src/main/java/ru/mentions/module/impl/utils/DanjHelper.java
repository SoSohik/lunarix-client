package ru.mentions.module.impl.utils;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.Collections;
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
import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.minecraft.class_1921;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2818;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3719;
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
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.notification.NotificationManager;

@ModuleInfo(
   name = "DanjHelper",
   desc = "Отслеживание бочек в данже: таймеры открытия и градиентный ESP",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class DanjHelper extends Module {
   public final ModeSetting soundMode = new ModeSetting(
      this,
      "Звук уведомления",
      "Bell",
      "Trevoga",
      "Modern",
      "Bubble",
      "ApplePay",
      "Celestial",
      "Zenith",
      "Classic",
      "Soft",
      "Cyber",
      "Skeet",
      "DingDong",
      "Laser",
      "WexSide",
      "Spek",
      "Metallic",
      "Tone",
      "Pop",
      "По умолчанию",
      "Выкл"
   );
   private final Map<class_2338, DanjHelper.DanjBlockData> trackedBlocks = new ConcurrentHashMap<>();
   private final Set<class_2338> gpsNotified = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Set<class_2338> chatNotified = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
   private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+)\\s*(с|s|сек|sec)");
   private static final int COLOR_RED = ColorUtil.getColor(255, 60, 60, 180);
   private static final int COLOR_YELLOW = ColorUtil.getColor(255, 220, 50, 180);
   private static final int COLOR_GREEN = ColorUtil.getColor(60, 255, 60, 180);
   private static final int COLOR_UNKNOWN = ColorUtil.getColor(180, 180, 180, 120);
   private final class_9799 allocator = new class_9799(262144);
   private static final RenderPipeline BOX_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "danj_helper_esp"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 BOX_LAYER = class_1921.method_75940(
      "danj_helper_esp", class_12247.method_75927(BOX_PIPELINE).method_75929(4096).method_75938()
   );

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.isInDanjZone()) {
            this.scanBarrelsAndHolograms();
            this.checkNotifications();
         }
      }
   }

   private boolean isInDanjZone() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         double x = mc.field_1724.method_23317();
         double z = mc.field_1724.method_23321();
         return Math.abs(Math.abs(x) - 2000.0) <= 150.0 && Math.abs(Math.abs(z) - 2000.0) <= 150.0;
      }
   }

   private boolean isBlockInZone(class_2338 pos) {
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

   private void scanBarrelsAndHolograms() {
      class_238 box = mc.field_1724.method_5829().method_1014(32.0);

      for (class_1297 entity : mc.field_1687.method_8335(mc.field_1724, box)) {
         long seconds;
         String clean;
         class_1531 armorStand;
         if (entity instanceof class_1531
            && (armorStand = (class_1531)entity).method_16914()
            && armorStand.method_5797() != null
            && (clean = ColorUtil.removeFormatting(armorStand.method_5797().getString())) != null
            && (seconds = this.parseTimeToSeconds(clean)) >= 0L) {
            class_2338 pos = armorStand.method_24515().method_10074();
            this.trackedBlocks.computeIfAbsent(pos, DanjHelper.DanjBlockData::new).updateFromHologram(seconds);
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

   private void checkNotifications() {
      for (Entry<class_2338, DanjHelper.DanjBlockData> entry : this.trackedBlocks.entrySet()) {
         class_2338 pos = entry.getKey();
         DanjHelper.DanjBlockData data = entry.getValue();
         float remaining;
         if (data.hasTimer() && (remaining = data.getRemainingSeconds()) <= 30.0F && remaining > 0.0F) {
            if (!this.gpsNotified.contains(pos)) {
               mc.field_1724.field_3944.method_45729(".gps set " + pos.method_10263() + " " + pos.method_10260());
               this.gpsNotified.add(pos);
            }

            if (!this.chatNotified.contains(pos)) {
               String msg = "Бочка откроется через "
                  + (int)remaining
                  + " сек! ["
                  + pos.method_10263()
                  + ", "
                  + pos.method_10264()
                  + ", "
                  + pos.method_10260()
                  + "]";
               ChatUtils.addChatMessage("§b[DanjHelper] " + msg);
               NotificationManager.sendWithSound(msg, NotificationManager.Type.WARNING, this.soundMode.getValue());
               this.chatNotified.add(pos);
            }
         }
      }
   }

   @EventHandler(
      priority = -500
   )
   public void onRender3D(EventRender3D event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.isInDanjZone()) {
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
                        if (blockEntity instanceof class_3719 && this.isBlockInZone(pos = blockEntity.method_11016())) {
                           if (!this.trackedBlocks.containsKey(pos)) {
                              this.trackedBlocks.put(pos, new DanjHelper.DanjBlockData(pos));
                           }

                           DanjHelper.DanjBlockData data = this.trackedBlocks.get(pos);
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

            immediate.method_22993();
         }
      }
   }

   private int getTimerColor(DanjHelper.DanjBlockData data) {
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

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.resetState();
   }

   @Override
   public void onDisable() {
      this.resetState();
      super.onDisable();
   }

   private void resetState() {
      this.trackedBlocks.clear();
      this.gpsNotified.clear();
      this.chatNotified.clear();
   }

   @Environment(EnvType.CLIENT)
   private static class DanjBlockData {
      final class_2338 pos;
      long readyAtMs = -1L;

      DanjBlockData(class_2338 pos) {
         this.pos = pos;
      }

      void updateFromHologram(long remainingSeconds) {
         this.readyAtMs = System.currentTimeMillis() + remainingSeconds * 1000L;
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
   }
}
