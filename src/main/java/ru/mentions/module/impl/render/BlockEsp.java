package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7923;
import net.minecraft.class_9799;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Block ESP",
   category = Category.RENDER,
   desc = "Подсвечивает блоки в радиусе"
)
@Environment(EnvType.CLIENT)
public class BlockEsp extends Module {
   public SliderSetting radius = new SliderSetting(this, "Радиус", 30.0F, 5.0F, 60.0F, 5.0F);
   private static BlockEsp instance;
   private final List<String> blockIds = new CopyOnWriteArrayList<>();
   private final List<class_2338> foundBlocks = new CopyOnWriteArrayList<>();
   private final class_9799 allocator = new class_9799(262144);
   private int scanTick = 0;
   private static final RenderPipeline BOX_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "block_esp"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 BOX_LAYER = class_1921.method_75940("block_esp", class_12247.method_75927(BOX_PIPELINE).method_75929(4096).method_75938());

   public BlockEsp() {
      instance = this;
   }

   public static BlockEsp getInstance() {
      return instance;
   }

   public boolean addBlock(class_2248 block) {
      String id = class_7923.field_41175.method_10221(block).toString();
      if (this.blockIds.contains(id)) {
         return false;
      } else {
         this.blockIds.add(id);
         this.rescan();
         return true;
      }
   }

   public boolean removeBlock(class_2248 block) {
      boolean removed = this.blockIds.remove(class_7923.field_41175.method_10221(block).toString());
      if (removed) {
         this.rescan();
      }

      return removed;
   }

   public int clearBlocks() {
      int count = this.blockIds.size();
      this.blockIds.clear();
      this.rescan();
      return count;
   }

   public Set<class_2248> getTargetBlocks() {
      HashSet<class_2248> targetBlocks = new HashSet<>();

      for (String id : this.blockIds) {
         class_2960 identifier = class_2960.method_12829(id);
         if (identifier != null) {
            class_7923.field_41175.method_17966(identifier).ifPresent(targetBlocks::add);
         }
      }

      return targetBlocks;
   }

   @Override
   protected void onDisable() {
      this.foundBlocks.clear();
      this.scanTick = 0;
   }

   public void rescan() {
      this.scanTick = 5;
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         Set<class_2248> targetBlocks = this.getTargetBlocks();
         if (targetBlocks.isEmpty()) {
            this.foundBlocks.clear();
         } else if (++this.scanTick >= 5) {
            this.scanTick = 0;
            ArrayList<class_2338> found = new ArrayList<>();
            int r = this.radius.getValue().intValue();
            int px = mc.field_1724.method_31477();
            int py = mc.field_1724.method_31478();
            int pz = mc.field_1724.method_31479();
            int minY = mc.field_1687.method_31607();
            int maxY = mc.field_1687.method_31600();
            class_2339 mutable = new class_2339();

            label49:
            for (int x = px - r; x <= px + r; x++) {
               for (int z = pz - r; z <= pz + r; z++) {
                  if (mc.field_1687.method_8393(x >> 4, z >> 4)) {
                     for (int y = Math.max(minY, py - r); y <= Math.min(maxY, py + r); y++) {
                        mutable.method_10103(x, y, z);
                        class_2248 block = mc.field_1687.method_8320(mutable).method_26204();
                        if (targetBlocks.contains(block)) {
                           found.add(mutable.method_10062());
                           if (found.size() >= 2000) {
                              break label49;
                           }
                        }
                     }
                  }
               }
            }

            this.foundBlocks.clear();
            this.foundBlocks.addAll(found);
         }
      } else {
         this.foundBlocks.clear();
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (mc.field_1687 != null && mc.field_1724 != null && !this.foundBlocks.isEmpty()) {
         class_4598 immediate = class_4597.method_22991(this.allocator);
         Matrix4f matrix = e.getMatrixStack().method_23760().method_23761();
         class_243 cam = mc.field_1773.method_19418().method_71156();
         int color = ColorUtil.fade(1);
         int cB = ColorUtil.replAlpha(color, 0.7F);
         int cT = ColorUtil.replAlpha(color, 0.0F);
         class_4588 buffer = immediate.method_73477(BOX_LAYER);

         for (class_2338 pos : this.foundBlocks) {
            float x0 = (float)((double)pos.method_10263() - cam.field_1352);
            float y0 = (float)((double)pos.method_10264() - cam.field_1351);
            float z0 = (float)((double)pos.method_10260() - cam.field_1350);
            drawGradientBox(buffer, matrix, x0, y0, z0, x0 + 1.0F, y0 + 1.0F, z0 + 1.0F, cB, cT);
         }

         immediate.method_22993();
      }
   }

   private static void drawGradientBox(class_4588 b, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int cB, int cT) {
      b.method_22918(m, x0, y0, z0).method_39415(cB);
      b.method_22918(m, x1, y0, z0).method_39415(cB);
      b.method_22918(m, x1, y0, z1).method_39415(cB);
      b.method_22918(m, x0, y0, z1).method_39415(cB);
      b.method_22918(m, x0, y1, z0).method_39415(cT);
      b.method_22918(m, x0, y1, z1).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cT);
      b.method_22918(m, x1, y1, z0).method_39415(cT);
      b.method_22918(m, x0, y0, z1).method_39415(cB);
      b.method_22918(m, x1, y0, z1).method_39415(cB);
      b.method_22918(m, x1, y1, z1).method_39415(cT);
      b.method_22918(m, x0, y1, z1).method_39415(cT);
      b.method_22918(m, x1, y0, z0).method_39415(cB);
      b.method_22918(m, x0, y0, z0).method_39415(cB);
      b.method_22918(m, x0, y1, z0).method_39415(cT);
      b.method_22918(m, x1, y1, z0).method_39415(cT);
      b.method_22918(m, x0, y0, z0).method_39415(cB);
      b.method_22918(m, x0, y0, z1).method_39415(cB);
      b.method_22918(m, x0, y1, z1).method_39415(cT);
      b.method_22918(m, x0, y1, z0).method_39415(cT);
      b.method_22918(m, x1, y0, z1).method_39415(cB);
      b.method_22918(m, x1, y0, z0).method_39415(cB);
      b.method_22918(m, x1, y1, z0).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cT);
   }
}
