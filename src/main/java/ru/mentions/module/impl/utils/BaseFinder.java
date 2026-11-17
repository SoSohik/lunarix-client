package ru.mentions.module.impl.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.other.TimerUtil;
import ru.mentions.utils.render.WorldRenderBatcher;

@ModuleInfo(
   name = "BaseFinder",
   desc = "Ищет базы",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class BaseFinder extends Module {
   private static final int CLUSTER = 4;
   private static final int MIN_Y = -64;
   private static final int MAX_Y = 320;
   private final SliderSetting range = new SliderSetting(this, "Радиус", 80.0F, 8.0F, 128.0F, 8.0F);
   private final SliderSetting interval = new SliderSetting(this, "Таймер", 4.0F, 0.0F, 100.0F, 5.0F);
   private final SliderSetting minHits = new SliderSetting(this, "Мин. блоков", 4.0F, 1.0F, 32.0F, 1.0F);
   private final TimerUtil timer = new TimerUtil();
   private final List<class_2338> hits = new ArrayList<>();
   private final List<BaseFinder.Region> bases = new ArrayList<>();

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.clear();
   }

   @Override
   protected void onEnable() {
      this.clear();
      super.onEnable();
   }

   @Override
   protected void onDisable() {
      this.clear();
      super.onDisable();
   }

   private void clear() {
      this.hits.clear();
      this.bases.clear();
      this.timer.reset();
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (this.timer.every((long)(this.interval.getValue() * 1000.0F))) {
            this.scan();
         }
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D event) {
      if (!this.bases.isEmpty()) {
         class_4587 stack = event.getMatrixStack();
         WorldRenderBatcher.begin(stack);

         try {
            for (BaseFinder.Region region : this.bases) {
               WorldRenderBatcher.drawBox(region.toBox(), -16718337, 0.05F);
            }
         } finally {
            WorldRenderBatcher.end();
         }
      }
   }

   private void scan() {
      class_2338 origin = mc.field_1724.method_24515();
      int radius = this.range.getValue().intValue();
      int radiusSq = radius * radius;
      int ox = origin.method_10263();
      int oy = origin.method_10264();
      int oz = origin.method_10260();
      int minY = Math.max(-64, oy - 48);
      int maxY = Math.min(320, oy + 48);
      this.hits.clear();

      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (dx * dx + dz * dz <= radiusSq) {
               int bx = ox + dx;
               int bz = oz + dz;

               for (int y = minY; y <= maxY; y++) {
                  class_2338 pos = new class_2338(bx, y, bz);
                  if (isBaseBlock(mc.field_1687.method_8320(pos))) {
                     this.hits.add(pos);
                  }
               }
            }
         }
      }

      this.cluster();
   }

   private static boolean isBaseBlock(class_2680 state) {
      if (state != null && !state.method_26215()) {
         class_2248 block = state.method_26204();
         return block == class_2246.field_10034
            || block == class_2246.field_10380
            || block == class_2246.field_16328
            || block == class_2246.field_9980
            || block == class_2246.field_10535
            || block == class_2246.field_10485
            || block == class_2246.field_10069
            || block == class_2246.field_10121
            || block == class_2246.field_10603
            || block == class_2246.field_10312
            || block == class_2246.field_10181
            || block == class_2246.field_10593
            || block == class_2246.field_40276;
      } else {
         return false;
      }
   }

   private void cluster() {
      this.bases.clear();
      if (!this.hits.isEmpty()) {
         int need = this.minHits.getValue().intValue();
         List<class_2338> rest = new ArrayList<>(this.hits);

         while (!rest.isEmpty()) {
            class_2338 seed = rest.remove(0);
            int minX = seed.method_10263();
            int minY = seed.method_10264();
            int minZ = seed.method_10260();
            int maxX = minX;
            int maxY = minY;
            int maxZ = minZ;
            int count = 1;

            for (int i = rest.size() - 1; i >= 0; i--) {
               class_2338 other = rest.get(i);
               if (Math.abs(other.method_10263() - minX) <= 4 && Math.abs(other.method_10264() - minY) <= 4 && Math.abs(other.method_10260() - minZ) <= 4) {
                  rest.remove(i);
                  count++;
                  minX = Math.min(minX, other.method_10263());
                  minY = Math.min(minY, other.method_10264());
                  minZ = Math.min(minZ, other.method_10260());
                  maxX = Math.max(maxX, other.method_10263());
                  maxY = Math.max(maxY, other.method_10264());
                  maxZ = Math.max(maxZ, other.method_10260());
               }
            }

            if (count >= need) {
               this.bases.add(new BaseFinder.Region(minX, minY, minZ, maxX, maxY, maxZ));
            }
         }

         this.bases.sort(Comparator.comparingDouble(region -> (double)(region.minX + region.minZ)));
      }
   }

   private static final class Region {
      final int minX;
      final int minY;
      final int minZ;
      final int maxX;
      final int maxY;
      final int maxZ;

      Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
         this.minX = minX;
         this.minY = minY;
         this.minZ = minZ;
         this.maxX = maxX;
         this.maxY = maxY;
         this.maxZ = maxZ;
      }

      class_238 toBox() {
         return new class_238(
            new class_243((double)this.minX, (double)this.minY, (double)this.minZ),
            new class_243((double)this.maxX + 1.0, (double)this.maxY + 1.0, (double)this.maxZ + 1.0)
         );
      }
   }
}
