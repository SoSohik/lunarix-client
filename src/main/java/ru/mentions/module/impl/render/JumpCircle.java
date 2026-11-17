package ru.mentions.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mentions.manager.event_impl.EventJump;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.preview.ModulePreview;
import ru.mentions.module.api.preview.PreviewContext;
import ru.mentions.module.api.preview.PreviewSettings;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;

@ModuleInfo(
   name = "Jump Circle",
   desc = "Кружочек под тобой при прижке",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class JumpCircle extends Module implements ModulePreview {
   private final List<JumpCircle.Circle> circles = new ArrayList<>();
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public SliderSetting size_c = new SliderSetting(this, "Размер", 1.0F, 0.2F, 3.0F, 0.1F);
   public BooleanSetting glow = new BooleanSetting(this, "Свечение", true);
   private final PreviewSettings previewSettings = PreviewSettings.of(this, 3.5F, 0.0F, 2.0F);
   class_9799 allocator = new class_9799(262144);
   private static final int QUAD_BUFFER_SIZE_BYTES = 1024;
   private static final RenderPipeline TEXTURED_QUADS_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("client", "pipeline/world/textured_quads"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final Function<class_2960, class_1921> ROMB_ESP = class_156.method_34866(texture -> {
      class_12247 setup = class_12247.method_75927(TEXTURED_QUADS_PIPELINE).method_75934("Sampler0", texture).method_75937().method_75929(1536).method_75938();
      return class_1921.method_75940("wtex", setup);
   });

   @EventHandler
   public void onJump(EventJump e) {
      this.circles.add(new JumpCircle.Circle(this, mc.field_1724.method_73189().method_1031(0.0, 0.05, 0.0)));
   }

   @Override
   public PreviewSettings previewSettings() {
      return this.previewSettings;
   }

   @Override
   public void previewSpawn(PreviewContext ctx) {
      this.circles.add(new JumpCircle.Circle(this, ctx.anchor().method_1031(0.0, 0.05, 0.0)));
   }

   @Override
   public void previewStop() {
      this.circles.clear();
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      if (!this.circles.isEmpty()) {
         this.circles.removeIf(c -> System.currentTimeMillis() - c.time > 2500L);
         if (!this.circles.isEmpty()) {
            class_4598 immediate = class_4597.method_22991(this.allocator);
            class_4587 pose = e.getMatrixStack();
            int color = ColorUtil.fade(1);
            int alpha = 255;

            for (JumpCircle.Circle c2 : this.circles) {
               if (System.currentTimeMillis() - c2.time > 800L && !c2.isBack) {
                  c2.animation.run(0.0, 0.5, Easings.SINE_OUT);
                  c2.animation2.run(2.0, 0.5, Easings.SINE_OUT);
                  c2.isBack = true;
               }

               c2.animation.update();
               c2.animation2.update();
               float rad = (float)c2.animation2.getValue();
               double posX = c2.vector3d.field_1352 - mc.field_1773.method_19418().method_71156().field_1352;
               double posY = c2.vector3d.field_1351 - mc.field_1773.method_19418().method_71156().field_1351;
               double posZ = c2.vector3d.field_1350 - mc.field_1773.method_19418().method_71156().field_1350;
               float size = this.size_c.getValue() * rad;
               pose.method_22903();
               pose.method_22904(posX, posY, posZ);
               pose.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
               class_4665 entry = pose.method_23760();
               Matrix4f matrix4f = entry.method_23761();
               Matrix3f normalMatrix = entry.method_23762();
               class_4588 buffer = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/jump_new.png")));
               this.drawTexturedQuad(
                  buffer,
                  matrix4f,
                  normalMatrix,
                  -size / 2.0F,
                  -size / 2.0F,
                  size,
                  size,
                  new int[]{ColorUtil.fade(0), ColorUtil.fade(90), ColorUtil.fade(180), ColorUtil.fade(360)},
                  (int)((float)alpha * c2.animation.get())
               );
               pose.method_22909();
               if (this.glow.getValue()) {
                  pose.method_22903();
                  pose.method_22904(posX, posY, posZ);
                  this.drawGlowLayers(immediate, pose, size, c2.animation.get());
                  pose.method_22909();
               }
            }

            immediate.method_22993();
         }
      }
   }

   private void drawGlowLayers(class_4598 immediate, class_4587 pose, float radius, float alpha) {
      class_4588 buffer = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/jump_new.png")));
      int layers = 20;
      float maxHeight = radius * 0.15F;
      float expand = radius * 0.25F;
      Vector3f normal = new Vector3f(0.0F, 1.0F, 0.0F);
      pose.method_23760().method_23762().transform(normal);
      normal.normalize();

      for (int i = 0; i < layers; i++) {
         float progress = (float)i / (float)layers;
         float yOff = maxHeight * progress;
         float layerAlpha = alpha * (1.0F - progress) * 0.15F;
         if (!(layerAlpha <= 0.004F)) {
            float r = radius + expand * progress;
            float half = r / 2.0F;
            int a = (int)(layerAlpha * 255.0F);
            Matrix4f matrix = pose.method_23760().method_23761();
            int c0 = ColorUtil.fade(0);
            int c90 = ColorUtil.fade(90);
            int c180 = ColorUtil.fade(180);
            int c270 = ColorUtil.fade(270);
            buffer.method_22918(matrix, -half, yOff, -half)
               .method_1336(c0 >> 16 & 0xFF, c0 >> 8 & 0xFF, c0 & 0xFF, a)
               .method_22913(0.0F, 0.0F)
               .method_22922(class_4608.field_21444)
               .method_60803(15728880)
               .method_22914(normal.x, normal.y, normal.z);
            buffer.method_22918(matrix, -half, yOff, half)
               .method_1336(c90 >> 16 & 0xFF, c90 >> 8 & 0xFF, c90 & 0xFF, a)
               .method_22913(0.0F, 1.0F)
               .method_22922(class_4608.field_21444)
               .method_60803(15728880)
               .method_22914(normal.x, normal.y, normal.z);
            buffer.method_22918(matrix, half, yOff, half)
               .method_1336(c180 >> 16 & 0xFF, c180 >> 8 & 0xFF, c180 & 0xFF, a)
               .method_22913(1.0F, 1.0F)
               .method_22922(class_4608.field_21444)
               .method_60803(15728880)
               .method_22914(normal.x, normal.y, normal.z);
            buffer.method_22918(matrix, half, yOff, -half)
               .method_1336(c270 >> 16 & 0xFF, c270 >> 8 & 0xFF, c270 & 0xFF, a)
               .method_22913(1.0F, 0.0F)
               .method_22922(class_4608.field_21444)
               .method_60803(15728880)
               .method_22914(normal.x, normal.y, normal.z);
         }
      }
   }

   private void drawTexturedQuad(class_4588 buffer, Matrix4f matrix, Matrix3f normalMatrix, float x, float y, float width, float height, int[] ints, int alpha) {
      Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F);
      normalMatrix.transform(normal);
      normal.normalize();
      float x2 = x + width;
      float y2 = y + height;
      buffer.method_22918(matrix, x, y, 0.0F)
         .method_39415(ColorUtil.replAlpha(ints[0], alpha))
         .method_22913(0.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(normal.x, normal.y, normal.z);
      buffer.method_22918(matrix, x2, y, 0.0F)
         .method_39415(ColorUtil.replAlpha(ints[1], alpha))
         .method_22913(1.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(normal.x, normal.y, normal.z);
      buffer.method_22918(matrix, x2, y2, 0.0F)
         .method_39415(ColorUtil.replAlpha(ints[2], alpha))
         .method_22913(1.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(normal.x, normal.y, normal.z);
      buffer.method_22918(matrix, x, y2, 0.0F)
         .method_39415(ColorUtil.replAlpha(ints[3], alpha))
         .method_22913(0.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(normal.x, normal.y, normal.z);
   }

   @Environment(EnvType.CLIENT)
   private class Circle {
      private final class_243 vector3d;
      private final long time;
      private final Animation animation = new Animation();
      private final Animation animation2 = new Animation();
      private boolean isBack;

      public Circle(JumpCircle jumpCircle, class_243 vector3d) {
         this.vector3d = vector3d;
         this.time = System.currentTimeMillis();
         this.animation.run(1.0, 0.5, Easings.SINE_OUT);
         this.animation2.run(1.0, 0.5, Easings.SINE_OUT);
      }
   }
}
