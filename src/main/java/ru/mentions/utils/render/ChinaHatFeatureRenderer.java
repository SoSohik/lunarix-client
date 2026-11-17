package ru.mentions.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_10799;
import net.minecraft.class_11659;
import net.minecraft.class_12247;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_7833;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.Client;
import ru.mentions.module.impl.render.ChinaHat;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public class ChinaHatFeatureRenderer extends class_3887<class_10055, class_591> {
   private static final float PI2 = (float) (Math.PI * 2);
   private static final int CIRCLE_SEGMENTS = 128;
   private static final int OUTLINE_SEGMENTS = 64;
   public static final RenderPipeline CHINA_HAT_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/china_hat")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27381)
         .build()
   );
   public static final class_1921 CHINA_HAT = class_1921.method_75940(
      "china_hat", class_12247.method_75927(CHINA_HAT_PIPELINE).method_75937().method_75929(8192).method_75938()
   );
   public static final RenderPipeline CHINA_HAT_OUTLINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/china_hat_outline")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_29345)
         .build()
   );
   public static final class_1921 CHINA_HAT_OUTLINE = class_1921.method_75940(
      "china_hat_outline", class_12247.method_75927(CHINA_HAT_OUTLINE_PIPELINE).method_75937().method_75929(4096).method_75938()
   );

   public ChinaHatFeatureRenderer(class_3883<class_10055, class_591> context) {
      super(context);
   }

   public void method_4199(class_4587 matrixStack, class_11659 queue, int light, class_10055 state, float limbAngle, float limbDistance) {
      class_310 mc = class_310.method_1551();
      ChinaHat chinaHat = ChinaHat.getInstance();
      if (chinaHat != null && chinaHat.isEnabled()) {
         if (mc.field_1724 != null) {
            boolean local = this.isLocalPlayer(state, mc);
            boolean friend = this.isFriendPlayer(state);
            if (local || friend) {
               if (!local || !mc.field_1690.method_31044().method_31034()) {
                  matrixStack.method_22903();
                  ((class_591)this.method_17165()).field_3398.method_22703(matrixStack);
                  matrixStack.method_22907(class_7833.field_40718.rotationDegrees(180.0F));
                  matrixStack.method_22907(class_7833.field_40716.rotationDegrees(90.0F));
                  matrixStack.method_46416(0.0F, 0.5F, 0.0F);
                  class_4598 immediate = mc.method_22940().method_23000();
                  this.renderFlatHat(matrixStack, immediate, chinaHat);
                  this.renderOutline(matrixStack, immediate, chinaHat);
                  immediate.method_22993();
                  matrixStack.method_22909();
               }
            }
         }
      }
   }

   private boolean isFriendPlayer(class_10055 state) {
      try {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 == null) {
            return false;
         }

         if (mc.field_1687.method_8469(state.field_53528) instanceof class_1657 p) {
            return Client.get().friendManager().isFriend(p.method_5477().getString());
         }
      } catch (Exception var5) {
      }

      return false;
   }

   private boolean isLocalPlayer(class_10055 state, class_310 mc) {
      try {
         if (state.field_53528 == mc.field_1724.method_5628()) {
            return true;
         }
      } catch (Exception var5) {
      }

      try {
         if (state.field_53525 != null && mc.field_1724.method_5477() != null) {
            return state.field_53525.getString().equals(mc.field_1724.method_5477().getString());
         }
      } catch (Exception var4) {
      }

      return false;
   }

   private void renderFlatHat(class_4587 stack, class_4597 provider, ChinaHat chinaHat) {
      class_4588 consumer = provider.method_73477(CHINA_HAT);
      Matrix4f matrix = stack.method_23760().method_23761();
      float width = 0.55F;
      float coneHeight = 0.31F;
      int alpha = 200;
      float animSpeed = 5.0F;
      int centerColor = this.getGradientColor(0, 128, chinaHat, animSpeed);
      centerColor = ColorUtil.replAlpha(centerColor, alpha);
      consumer.method_22918(matrix, 0.0F, coneHeight, 0.0F).method_39415(centerColor);

      for (int i = 0; i <= 128; i++) {
         int color = this.getGradientColor(i, 128, chinaHat, animSpeed);
         color = ColorUtil.replAlpha(color, alpha);
         float angle = (float)i * (float) (Math.PI * 2) / 128.0F;
         float x = -class_3532.method_15374((double)angle) * width;
         float z = class_3532.method_15362((double)angle) * width;
         consumer.method_22918(matrix, x, 0.0F, z).method_39415(color);
      }

      for (int var22 = 128; var22 >= 0; var22--) {
         int color = this.getGradientColor(var22, 128, chinaHat, animSpeed);
         color = ColorUtil.replAlpha(color, alpha);
         float angle = (float)var22 * (float) (Math.PI * 2) / 128.0F;
         float x = -class_3532.method_15374((double)angle) * width;
         float z = class_3532.method_15362((double)angle) * width;
         consumer.method_22918(matrix, x, 0.0F, z).method_39415(color);
      }

      consumer.method_22918(matrix, 0.0F, coneHeight, 0.0F).method_39415(centerColor);
   }

   private void renderOutline(class_4587 stack, class_4597 provider, ChinaHat chinaHat) {
      class_4588 consumer = provider.method_73477(CHINA_HAT_OUTLINE);
      Matrix4f matrix = stack.method_23760().method_23761();
      float width = 0.55F;
      float animSpeed = 5.0F;
      int outlineAlpha = 255;

      for (int i = 0; i <= 64; i++) {
         int color = this.getGradientColor(i * 2, 128, chinaHat, animSpeed);
         color = ColorUtil.replAlpha(color, outlineAlpha);
         float angle = (float)i * (float) (Math.PI * 2) / 64.0F;
         float x = -class_3532.method_15374((double)angle) * width;
         float z = class_3532.method_15362((double)angle) * width;
         consumer.method_22918(matrix, x, 0.0F, z).method_39415(color);
      }
   }

   private int getGradientColor(int index, int size, ChinaHat chinaHat, float animSpeed) {
      long time = System.currentTimeMillis();
      float timeOffset = (float)time / (1000.0F / animSpeed) % (float)size;
      return ColorUtil.fade((int)((float)index * (360.0F / (float)size) + timeOffset * (360.0F / (float)size)));
   }
}
