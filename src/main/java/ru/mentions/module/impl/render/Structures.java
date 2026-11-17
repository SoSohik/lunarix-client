package ru.mentions.module.impl.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2680;
import net.minecraft.class_2767;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.notification.NotificationManager;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.ItemRender;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Structures",
   desc = "Таймери дії трапок і пластів у світі навколо гравця",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Structures extends Module {
   public final BooleanSetting traps = new BooleanSetting(this, "Трапки", true);
   public final BooleanSetting plasts = new BooleanSetting(this, "Пласти", true);
   public final BooleanSetting notifyPlacement = new BooleanSetting(this, "Сповіщення при появі", true);
   public final SliderSetting scale = new SliderSetting(this, "Розмір плашки", 1.0F, 0.4F, 2.0F, 0.1F);
   public final SliderSetting maxDistance = new SliderSetting(this, "Дистанція", 64.0F, 16.0F, 128.0F, 4.0F);
   private static final int MIN_PLACEMENT_BLOCKS = 35;
   private static final int REGULAR_TRAP_DURATION_TICKS = 300;
   private static final int DRAGON_TRAP_DURATION_TICKS = 600;
   private static final int FLAT_PLAST_DURATION_TICKS = 1200;
   private static final int TALL_PLAST_DURATION_TICKS = 400;
   private static final int PLACEMENT_HINT_TIMEOUT_TICKS = 60;
   private static final Set<String> TERRAIN_BLOCK_IDS = Set.of(
      "grass_block",
      "dirt",
      "coarse_dirt",
      "dirt_path",
      "podzol",
      "rooted_dirt",
      "cave_vines",
      "cave_vines_plant",
      "short_grass",
      "tall_grass",
      "fern",
      "large_fern"
   );
   private static final Set<String> DRAGON_MARKER_BLOCK_IDS = Set.of("lodestone", "respawn_anchor");
   private static final Set<String> PLACEMENT_MARKER_BLOCK_IDS = Set.of(
      "dead_tube_coral_block",
      "lodestone",
      "respawn_anchor",
      "netherite_block",
      "gilded_blackstone",
      "sea_lantern",
      "quartz_pillar",
      "chiseled_quartz_block",
      "smooth_quartz_slab",
      "quartz_stairs",
      "chiseled_polished_blackstone",
      "polished_blackstone_bricks",
      "polished_blackstone_brick_stairs",
      "polished_blackstone_brick_slab",
      "polished_blackstone_wall",
      "polished_basalt",
      "blackstone_slab",
      "blackstone_stairs",
      "andesite_stairs",
      "diorite_wall",
      "diorite_stairs",
      "polished_diorite",
      "polished_diorite_slab",
      "light_gray_stained_glass",
      "white_stained_glass",
      "black_concrete",
      "coal_block",
      "smooth_stone_slab",
      "crying_obsidian"
   );
   private final ConcurrentLinkedQueue<Structures.BlockChange> blockChanges = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<Structures.PendingSound> receivedSounds = new ConcurrentLinkedQueue<>();
   private final List<Structures.PendingSound> pendingSounds = new ArrayList<>();
   private final List<Structures.ActiveStructure> activeStructures = new ArrayList<>();
   private volatile long currentTick = 0L;

   public static Structures getInstance() {
      return Instance.get(Structures.class);
   }

   @Override
   public void onEnable() {
      this.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.reset();
      super.onDisable();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.reset();
   }

   private void reset() {
      this.blockChanges.clear();
      this.receivedSounds.clear();
      this.pendingSounds.clear();
      this.activeStructures.clear();
      this.currentTick = 0L;
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (this.isEnabled() && mc.field_1724 != null && !event.isSend()) {
         class_2596<?> packet = event.getPacket();
         if (packet instanceof class_2626 blockPacket) {
            this.recordBlockChange(blockPacket.method_11309(), blockPacket.method_11308());
         } else if (packet instanceof class_2637 chunkDelta) {
            chunkDelta.method_30621(this::recordBlockChange);
         } else if (packet instanceof class_2767 soundPacket) {
            this.recordPlacementSound(soundPacket);
         }
      }
   }

   private void recordPlacementSound(class_2767 packet) {
      class_6880<class_3414> soundEntry = packet.method_11894();
      if (soundEntry != null) {
         String path = "";

         try {
            if (soundEntry.comp_349() != null && ((class_3414)soundEntry.comp_349()).comp_3319() != null) {
               path = ((class_3414)soundEntry.comp_349()).comp_3319().method_12832();
            }
         } catch (Throwable var13) {
         }

         if (path.isEmpty()) {
            path = soundEntry.toString().toLowerCase(Locale.ROOT);
         }

         float pitch = packet.method_11892();
         int soundKind = -1;
         boolean dragon = false;
         if (path.contains("piston.extend") && isPitchNear(pitch, 0.5F)) {
            soundKind = 0;
         } else if (path.contains("anvil.place") && isPitchNear(pitch, 0.5F)) {
            soundKind = 1;
         } else if (path.contains("ender_dragon.growl")) {
            soundKind = 2;
            dragon = true;
         }

         if (soundKind != -1) {
            double maxDist = this.maxDistance.getValue().doubleValue();
            double dx = packet.method_11890() - mc.field_1724.method_23317();
            double dz = packet.method_11893() - mc.field_1724.method_23321();
            if (dx * dx + dz * dz <= maxDist * maxDist) {
               this.receivedSounds
                  .add(new Structures.PendingSound(soundKind, dragon, packet.method_11890(), packet.method_11889(), packet.method_11893(), this.currentTick));
            }
         }
      }
   }

   private static boolean isPitchNear(float pitch, float target) {
      return Math.abs(pitch - target) < 0.08F;
   }

   private void recordBlockChange(class_2338 pos, class_2680 state) {
      if (!state.method_26215() && pos != null && mc.field_1724 != null) {
         double maxDist = this.maxDistance.getValue().doubleValue();
         double dx = (double)pos.method_10263() - mc.field_1724.method_23317();
         double dz = (double)pos.method_10260() - mc.field_1724.method_23321();
         if (!(dx * dx + dz * dz > maxDist * maxDist)) {
            class_2248 block = state.method_26204();
            class_2960 id = class_7923.field_41175.method_10221(block);
            if (id != null) {
               String path = id.method_12832();
               boolean terrain = TERRAIN_BLOCK_IDS.contains(path);
               boolean dragon = DRAGON_MARKER_BLOCK_IDS.contains(path);
               boolean marker = PLACEMENT_MARKER_BLOCK_IDS.contains(path);
               boolean trapMarker = path.endsWith("_stairs") || path.endsWith("_slab") || path.equals("cobweb") || path.equals("smooth_stone_slab");
               this.blockChanges
                  .add(
                     new Structures.BlockChange(new class_2338(pos.method_10263(), pos.method_10264(), pos.method_10260()), terrain, dragon, marker, trapMarker)
                  );
            }
         }
      }
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      if (this.isEnabled() && mc.field_1724 != null && mc.field_1687 != null) {
         this.currentTick++;

         Structures.PendingSound ps;
         while ((ps = this.receivedSounds.poll()) != null) {
            this.pendingSounds.add(ps);
         }

         this.pendingSounds.removeIf(s -> s.consumed || this.currentTick - s.receivedAtTick > 60L);
         this.aggregateBlockChanges();

         for (Structures.ActiveStructure st : this.activeStructures) {
            long age = this.currentTick - st.placedAtTick;
            boolean expired = age >= (long)st.durationTicks || age >= 10L && st.areSampledBlocksDestroyed();
            if (expired) {
               st.expired = true;
            }
         }

         this.activeStructures.removeIf(stx -> stx.expired);
      }
   }

   private void aggregateBlockChanges() {
      if (!this.blockChanges.isEmpty()) {
         List<Structures.PlacementBatch> batches = new ArrayList<>();

         Structures.BlockChange change;
         while ((change = this.blockChanges.poll()) != null) {
            Structures.PlacementBatch matched = null;

            for (Structures.PlacementBatch b : batches) {
               if (b.containsNearbyPosition(change.pos)) {
                  matched = b;
                  break;
               }
            }

            if (matched == null) {
               matched = new Structures.PlacementBatch();
               matched.placedAtTick = this.currentTick;
               batches.add(matched);
            }

            matched.addBlockChange(change);
         }

         for (Structures.PlacementBatch batch : batches) {
            this.createStructure(batch);
         }
      }
   }

   private void createStructure(Structures.PlacementBatch batch) {
      if (batch.blockCount >= 35 && batch.hasNonTerrainBlock) {
         double centerX = (double)(batch.minX + batch.maxX) / 2.0;
         double centerY = (double)(batch.minY + batch.maxY) / 2.0;
         double centerZ = (double)(batch.minZ + batch.maxZ) / 2.0;
         int height = batch.height();
         Structures.PendingSound sound = this.findMatchingSound(centerX, centerY, centerZ);
         if (sound != null || batch.hasPlacementMarker) {
            if (sound != null) {
               sound.consumed = true;
            }

            boolean isTrap = batch.hasTrapMarker;
            boolean isDragon = batch.hasDragonMarker || sound != null && sound.dragon || this.consumeNearbyDragonSound(centerX, centerY, centerZ);
            if (!isTrap || this.traps.getValue()) {
               if (isTrap || this.plasts.getValue()) {
                  double renderX = centerX + 0.5;
                  double renderY = (double)batch.maxY + 1.2;
                  double renderZ = centerZ + 0.5;
                  int durationTicks;
                  String name;
                  class_1799 icon;
                  if (isTrap) {
                     if (isDragon) {
                        durationTicks = 600;
                        name = "Драконяча трапка";
                        icon = new class_1799(class_1802.field_8233);
                     } else {
                        durationTicks = 300;
                        name = "Трапка";
                        icon = new class_1799(class_1802.field_8255);
                     }
                  } else {
                     boolean isFlat = height <= 3 || height < batch.width() && height < batch.depth();
                     if (isFlat) {
                        durationTicks = 1200;
                        name = "Пласт";
                        icon = new class_1799(class_1802.field_8554);
                     } else {
                        durationTicks = 400;
                        name = "Високий пласт";
                        icon = new class_1799(class_1802.field_8281);
                     }
                  }

                  if (!this.hasRecentMatchingOverlay(isTrap, renderX, renderY, renderZ)) {
                     Structures.ActiveStructure structure = new Structures.ActiveStructure(
                        isTrap, name, renderX, renderY, renderZ, this.currentTick, durationTicks, icon, batch.sampledBlocks
                     );
                     this.activeStructures.add(structure);
                     if (this.notifyPlacement.getValue()) {
                        NotificationManager.send(name + " (" + durationTicks / 20 + "с)", NotificationManager.Type.INFO, icon, 2500L, "DingDong");
                     }
                  }
               }
            }
         }
      }
   }

   private Structures.PendingSound findMatchingSound(double x, double y, double z) {
      Structures.PendingSound best = null;
      Structures.PendingSound bestDragon = null;

      for (Structures.PendingSound s : this.pendingSounds) {
         if (!s.consumed) {
            double dx = s.x - x;
            double dy = s.y - y;
            double dz = s.z - z;
            if (dx * dx + dy * dy + dz * dz <= 484.0) {
               if (s.soundKind == 2) {
                  if (bestDragon == null || s.receivedAtTick > bestDragon.receivedAtTick) {
                     bestDragon = s;
                  }
               } else if (best == null || s.receivedAtTick > best.receivedAtTick) {
                  best = s;
               }
            }
         }
      }

      return best != null ? best : bestDragon;
   }

   private boolean consumeNearbyDragonSound(double x, double y, double z) {
      for (Structures.PendingSound s : this.pendingSounds) {
         if (!s.consumed && s.soundKind == 2) {
            double dx = s.x - x;
            double dy = s.y - y;
            double dz = s.z - z;
            if (dx * dx + dy * dy + dz * dz <= 484.0) {
               s.consumed = true;
               return true;
            }
         }
      }

      return false;
   }

   private boolean hasRecentMatchingOverlay(boolean isTrap, double x, double y, double z) {
      for (Structures.ActiveStructure st : this.activeStructures) {
         if (!st.expired
            && st.isTrap == isTrap
            && Math.abs(st.x - x) <= 3.0
            && Math.abs(st.y - y) <= 4.0
            && Math.abs(st.z - z) <= 3.0
            && this.currentTick - st.placedAtTick < 40L) {
            return true;
         }
      }

      return false;
   }

   @EventHandler
   public void onDisplay(EventDisplay eventDisplay) {
      if (this.isEnabled() && mc.field_1724 != null && mc.field_1687 != null && !this.activeStructures.isEmpty()) {
         double maxDistSq = Math.pow((double)this.maxDistance.getValue().floatValue(), 2.0);
         double px = mc.field_1724.method_23317();
         double py = mc.field_1724.method_23318();
         double pz = mc.field_1724.method_23321();
         float uiScale = this.scale.getValue();
         Font font = Fonts.sf_medium;
         Render2D.beginOverlay();

         for (Structures.ActiveStructure st : this.activeStructures) {
            if (!st.expired) {
               double distSq = (st.x - px) * (st.x - px) + (st.y - py) * (st.y - py) + (st.z - pz) * (st.z - pz);
               if (!(distSq > maxDistSq)) {
                  class_243 worldPos = new class_243(st.x, st.y, st.z);
                  class_243 screen = Projection.worldSpaceToScreenSpace(worldPos);
                  if (!(screen.field_1350 <= 0.0) && !(screen.field_1350 >= 1.0)) {
                     float sx = (float)screen.field_1352;
                     float sy = (float)screen.field_1351;
                     int remainingSeconds = st.remainingSeconds(this.currentTick);
                     String timerText = remainingSeconds + "с";
                     String nameText = st.name;
                     float fontSize = 7.0F * uiScale;
                     float nameW = font.getWidth(nameText, fontSize);
                     float timerW = font.getWidth(timerText, fontSize);
                     float iconSize = 12.0F * uiScale;
                     float padding = 4.0F * uiScale;
                     float cardW = iconSize + nameW + timerW + padding * 4.0F;
                     float cardH = Math.max(16.0F * uiScale, fontSize + 8.0F * uiScale);
                     float cardX = sx - cardW / 2.0F;
                     float cardY = sy - cardH / 2.0F;
                     int bgColor = ColorUtil.getColor(20, 20, 24, 220);
                     RenderUtil.Render2D.rect(cardX, cardY, cardW, cardH, bgColor, 5.0F * uiScale);
                     int outlineColor = remainingSeconds <= 3
                        ? ColorUtil.getColor(255, 60, 60, 220)
                        : (remainingSeconds <= 6 ? ColorUtil.getColor(255, 180, 50, 200) : ThemeColor.getOutlineColor(0.28F));
                     Draw.outline(cardX, cardY, cardW, cardH, 0.75F, outlineColor, 5.0F * uiScale);
                     float itemX = cardX + padding;
                     float itemY = cardY + (cardH - iconSize) / 2.0F;
                     if (eventDisplay.getDrawContext() != null && st.icon != null && !st.icon.method_7960()) {
                        ItemRender.drawItemWithContext(eventDisplay.getDrawContext(), st.icon, itemX, itemY, 0.75F * uiScale, 1.0F);
                     }

                     float textX = itemX + iconSize + padding;
                     float textY = cardY + (cardH - fontSize) / 2.0F + 0.5F;
                     font.draw(nameText, textX, textY, fontSize, ColorUtil.getColor(245, 245, 245, 255));
                     int timerColor = remainingSeconds <= 3
                        ? ColorUtil.getColor(255, 60, 60, 255)
                        : (remainingSeconds <= 6 ? ColorUtil.getColor(255, 180, 50, 255) : ColorUtil.client());
                     font.draw(timerText, textX + nameW + padding, textY, fontSize, timerColor);
                  }
               }
            }
         }

         Render2D.endOverlay();
      }
   }

   public static final class ActiveStructure {
      final boolean isTrap;
      final String name;
      final double x;
      final double y;
      final double z;
      final long placedAtTick;
      final int durationTicks;
      final class_1799 icon;
      final List<class_2338> sampledBlocks;
      boolean expired = false;

      ActiveStructure(
         boolean isTrap, String name, double x, double y, double z, long placedAtTick, int durationTicks, class_1799 icon, List<class_2338> sampledBlocks
      ) {
         this.isTrap = isTrap;
         this.name = name;
         this.x = x;
         this.y = y;
         this.z = z;
         this.placedAtTick = placedAtTick;
         this.durationTicks = durationTicks;
         this.icon = icon;
         this.sampledBlocks = sampledBlocks;
      }

      int remainingSeconds(long tick) {
         return Math.max(0, (int)Math.ceil((double)((long)this.durationTicks - (tick - this.placedAtTick)) / 20.0));
      }

      boolean areSampledBlocksDestroyed() {
         if (IMinecraft.mc.field_1687 != null && !this.sampledBlocks.isEmpty()) {
            int airCount = 0;
            int loadedCount = 0;

            for (class_2338 pos : this.sampledBlocks) {
               if (IMinecraft.mc.field_1687.method_22340(pos)) {
                  loadedCount++;
                  if (IMinecraft.mc.field_1687.method_8320(pos).method_26215()) {
                     airCount++;
                  }
               }
            }

            return loadedCount >= 4 && airCount >= (int)((double)loadedCount * 0.8);
         } else {
            return false;
         }
      }
   }

   private static record BlockChange(class_2338 pos, boolean terrain, boolean dragon, boolean marker, boolean trapMarker) {
   }

   private static final class PendingSound {
      final int soundKind;
      final boolean dragon;
      final double x;
      final double y;
      final double z;
      final long receivedAtTick;
      boolean consumed;

      PendingSound(int soundKind, boolean dragon, double x, double y, double z, long receivedAtTick) {
         this.soundKind = soundKind;
         this.dragon = dragon;
         this.x = x;
         this.y = y;
         this.z = z;
         this.receivedAtTick = receivedAtTick;
         this.consumed = false;
      }
   }

   private static final class PlacementBatch {
      int blockCount;
      long placedAtTick;
      int minX;
      int minY;
      int minZ;
      int maxX;
      int maxY;
      int maxZ;
      boolean hasNonTerrainBlock;
      boolean hasDragonMarker;
      boolean hasPlacementMarker;
      boolean hasTrapMarker;
      final List<class_2338> sampledBlocks = new ArrayList<>();

      boolean containsNearbyPosition(class_2338 pos) {
         return this.blockCount == 0
            ? true
            : pos.method_10263() >= this.minX - 4
               && pos.method_10263() <= this.maxX + 4
               && pos.method_10264() >= this.minY - 4
               && pos.method_10264() <= this.maxY + 4
               && pos.method_10260() >= this.minZ - 4
               && pos.method_10260() <= this.maxZ + 4;
      }

      void addBlockChange(Structures.BlockChange change) {
         class_2338 pos = change.pos;
         if (this.blockCount == 0) {
            this.minX = this.maxX = pos.method_10263();
            this.minY = this.maxY = pos.method_10264();
            this.minZ = this.maxZ = pos.method_10260();
         } else {
            this.minX = Math.min(this.minX, pos.method_10263());
            this.maxX = Math.max(this.maxX, pos.method_10263());
            this.minY = Math.min(this.minY, pos.method_10264());
            this.maxY = Math.max(this.maxY, pos.method_10264());
            this.minZ = Math.min(this.minZ, pos.method_10260());
            this.maxZ = Math.max(this.maxZ, pos.method_10260());
         }

         this.blockCount++;
         if (!change.terrain) {
            this.hasNonTerrainBlock = true;
         }

         if (change.dragon) {
            this.hasDragonMarker = true;
         }

         if (change.marker) {
            this.hasPlacementMarker = true;
         }

         if (change.trapMarker) {
            this.hasTrapMarker = true;
         }

         if (this.sampledBlocks.size() < 8 && this.blockCount % 5 == 1) {
            this.sampledBlocks.add(pos);
         }
      }

      int width() {
         return this.maxX - this.minX + 1;
      }

      int height() {
         return this.maxY - this.minY + 1;
      }

      int depth() {
         return this.maxZ - this.minZ + 1;
      }
   }
}
