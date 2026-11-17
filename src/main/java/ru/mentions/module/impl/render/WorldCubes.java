package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_156;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "World Cubes",
   desc = "Сетка по миру со свечением",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class WorldCubes extends Module {
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -16711681).setVisible(() -> this.typeColor.is("Свой"));
   public SliderSetting range = new SliderSetting(this, "Радиус (ячейки)", 4.0F, 1.0F, 30.0F, 1.0F);
   public SliderSetting vRange = new SliderSetting(this, "Высота (ячейки)", 2.0F, 1.0F, 20.0F, 1.0F);
   public SliderSetting lineAlphaS = new SliderSetting(this, "Прозрачность линий", 0.22F, 0.04F, 1.0F, 0.02F);
   public BooleanSetting showGlow = new BooleanSetting(this, "Свечение в углах", true);
   public SliderSetting glowSize = new SliderSetting(this, "Размер свечения", 0.08F, 0.02F, 1.0F, 0.02F).setVisible(() -> this.showGlow.getValue());
   public SliderSetting glowBright = new SliderSetting(this, "Яркость свечения", 1.8F, 0.5F, 4.0F, 0.1F).setVisible(() -> this.showGlow.getValue());
   private final Animation shiftAnim = new Animation();
   private int currOx;
   private int currOy;
   private int currOz;
   private int prevOx;
   private int prevOy;
   private int prevOz;
   private boolean firstFrame = true;
   private static final int CELL = 12;
   private final class_9799 allocator = new class_9799(262144);
   public static final RenderPipeline ROMB_ESP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> ROMB_ESP = class_156.method_34866(texture -> {
      class_12247 setup = class_12247.method_75927(ROMB_ESP_PIPELINE).method_75934("Sampler0", texture).method_75937().method_75929(1536).method_75938();
      return class_1921.method_75940("wtex", setup);
   });
   private static final RenderPipeline LINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "world_cubes_grid"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final class_1921 LINE_LAYER = class_1921.method_75940(
      "world_cubes_lines", class_12247.method_75927(LINE_PIPELINE).method_75929(16384).method_75938()
   );

   public int getColor() {
      return this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   @Override
   protected void onEnable() {
      this.firstFrame = true;
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int ox = snapDown(mc.field_1724.method_23317());
         int oy = snapDown(mc.field_1724.method_23318());
         int oz = snapDown(mc.field_1724.method_23321());
         if (this.firstFrame) {
            this.currOx = ox;
            this.currOy = oy;
            this.currOz = oz;
            this.prevOx = ox;
            this.prevOy = oy;
            this.prevOz = oz;
            this.shiftAnim.set(1.0);
            this.firstFrame = false;
         } else if (ox != this.currOx || oy != this.currOy || oz != this.currOz) {
            this.prevOx = this.currOx;
            this.prevOy = this.currOy;
            this.prevOz = this.currOz;
            this.currOx = ox;
            this.currOy = oy;
            this.currOz = oz;
            this.shiftAnim.set(0.0);
            this.shiftAnim.run(1.0, 0.25, Easings.QUAD_OUT);
         }

         this.shiftAnim.update();
         float t = this.shiftAnim.get();
         int col = this.getColor();
         int cr = col >> 16 & 0xFF;
         int cg = col >> 8 & 0xFF;
         int cb = col & 0xFF;
         float baseAlpha = this.lineAlphaS.getValue() * 255.0F;
         int rI = this.range.getValue().intValue();
         int vI = this.vRange.getValue().intValue();
         class_243 cam = mc.field_1773.method_19418().method_71156();
         class_4587 stack = e.getMatrixStack();
         Matrix4f mat = stack.method_23760().method_23761();
         class_4598 immediate = class_4597.method_22991(this.allocator);
         class_4588 lb = immediate.method_73477(LINE_LAYER);
         if (t < 0.999F) {
            int oldA = (int)(baseAlpha * (1.0F - t));
            this.drawLines(lb, mat, cam, this.prevOx, this.prevOy, this.prevOz, cr, cg, cb, oldA, rI, vI);
         }

         int newA = (int)(baseAlpha * t);
         this.drawLines(lb, mat, cam, this.currOx, this.currOy, this.currOz, cr, cg, cb, newA, rI, vI);
         if (this.showGlow.getValue()) {
            float gs = this.glowSize.getValue();
            float brite = this.glowBright.getValue();
            int newGlowA = Math.min(255, (int)(15.0F * t * brite));
            this.drawGlows(
               immediate,
               ROMB_ESP.apply(Particles.ParticleType.BLOOM.texture()),
               mat,
               cam,
               this.currOx,
               this.currOy,
               this.currOz,
               cr,
               cg,
               cb,
               newGlowA,
               gs * 2.0F,
               rI,
               vI
            );
            newGlowA = Math.min(255, (int)(255.0F * t * brite));
            this.drawGlows(
               immediate,
               ROMB_ESP.apply(Particles.ParticleType.CIRCLE.texture()),
               mat,
               cam,
               this.currOx,
               this.currOy,
               this.currOz,
               cr,
               cg,
               cb,
               newGlowA,
               gs,
               rI,
               vI
            );
         }

         immediate.method_22993();
      }
   }

   private void drawLines(class_4588 lb, Matrix4f mat, class_243 cam, int ox, int oy, int oz, int cr, int cg, int cb, int alpha, int rI, int vI) {
      if (alpha > 0) {
         int xMin = ox - rI * 12;
         int xMax = ox + rI * 12;
         int zMin = oz - rI * 12;
         int zMax = oz + rI * 12;
         int yMin = oy - (vI - 1) * 12;
         int yMax = oy + vI * 12;

         for (int x = xMin; x <= xMax; x += 12) {
            for (int z = zMin; z <= zMax; z += 12) {
               float rx = (float)((double)x - cam.field_1352);
               float rz = (float)((double)z - cam.field_1350);
               lb.method_22918(mat, rx, (float)((double)yMin - cam.field_1351), rz).method_1336(cr, cg, cb, alpha);
               lb.method_22918(mat, rx, (float)((double)yMax - cam.field_1351), rz).method_1336(cr, cg, cb, alpha);
            }
         }

         for (int y = yMin; y <= yMax; y += 12) {
            float ry = (float)((double)y - cam.field_1351);

            for (int z = zMin; z <= zMax; z += 12) {
               float rz = (float)((double)z - cam.field_1350);
               lb.method_22918(mat, (float)((double)xMin - cam.field_1352), ry, rz).method_1336(cr, cg, cb, alpha);
               lb.method_22918(mat, (float)((double)xMax - cam.field_1352), ry, rz).method_1336(cr, cg, cb, alpha);
            }
         }

         for (int var24 = yMin; var24 <= yMax; var24 += 12) {
            float ry = (float)((double)var24 - cam.field_1351);

            for (int x = xMin; x <= xMax; x += 12) {
               float rx = (float)((double)x - cam.field_1352);
               lb.method_22918(mat, rx, ry, (float)((double)zMin - cam.field_1350)).method_1336(cr, cg, cb, alpha);
               lb.method_22918(mat, rx, ry, (float)((double)zMax - cam.field_1350)).method_1336(cr, cg, cb, alpha);
            }
         }
      }
   }

   private void drawGlows(
      class_4598 immediate,
      class_1921 glowLayer,
      Matrix4f mat,
      class_243 cam,
      int ox,
      int oy,
      int oz,
      int cr,
      int cg,
      int cb,
      int alpha,
      float gs,
      int rI,
      int vI
   ) {
      if (alpha > 0) {
         int xMin = ox - rI * 12;
         int xMax = ox + rI * 12;
         int zMin = oz - rI * 12;
         int zMax = oz + rI * 12;
         int yMin = oy - (vI - 1) * 12;
         int yMax = oy + vI * 12;
         class_4184 camera = mc.field_1773.method_19418();
         Quaternionf q = camera.method_23767();
         Vector3f c0 = new Vector3f(-gs, -gs, 0.0F).rotate(q);
         Vector3f c1 = new Vector3f(gs, -gs, 0.0F).rotate(q);
         Vector3f c2 = new Vector3f(gs, gs, 0.0F).rotate(q);
         Vector3f c3 = new Vector3f(-gs, gs, 0.0F).rotate(q);
         float yawRad = (float)Math.toRadians((double)camera.method_19330());
         float pitchRad = (float)Math.toRadians((double)camera.method_19329());
         float cosPitch = (float)Math.cos((double)pitchRad);
         float lx = (float)(-Math.sin((double)yawRad) * (double)cosPitch);
         float ly = (float)(-Math.sin((double)pitchRad));
         float lz = (float)(Math.cos((double)yawRad) * (double)cosPitch);
         class_4588 gb = immediate.method_73477(glowLayer);

         for (int x = xMin; x <= xMax; x += 12) {
            for (int y = yMin; y <= yMax; y += 12) {
               for (int z = zMin; z <= zMax; z += 12) {
                  float rx = (float)((double)x - cam.field_1352);
                  float ry = (float)((double)y - cam.field_1351);
                  float rz = (float)((double)z - cam.field_1350);
                  if (!(rx * lx + ry * ly + rz * lz < -gs * 2.0F)) {
                     gb.method_22918(mat, rx + c0.x, ry + c0.y, rz + c0.z)
                        .method_1336(cr, cg, cb, alpha)
                        .method_22913(0.0F, 1.0F)
                        .method_22922(class_4608.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                     gb.method_22918(mat, rx + c1.x, ry + c1.y, rz + c1.z)
                        .method_1336(cr, cg, cb, alpha)
                        .method_22913(1.0F, 1.0F)
                        .method_22922(class_4608.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                     gb.method_22918(mat, rx + c2.x, ry + c2.y, rz + c2.z)
                        .method_1336(cr, cg, cb, alpha)
                        .method_22913(1.0F, 0.0F)
                        .method_22922(class_4608.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                     gb.method_22918(mat, rx + c3.x, ry + c3.y, rz + c3.z)
                        .method_1336(cr, cg, cb, alpha)
                        .method_22913(0.0F, 0.0F)
                        .method_22922(class_4608.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                  }
               }
            }
         }
      }
   }

   private static int snapDown(double v) {
      return (int)Math.floor(v / 12.0) * 12;
   }
}
