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
import net.minecraft.class_1309;
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
import net.minecraft.class_7833;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.preview.ModulePreview;
import ru.mentions.module.api.preview.PreviewContext;
import ru.mentions.module.api.preview.PreviewSettings;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;

@ModuleInfo(
   name = "Target Esp",
   desc = "Отображения таргета",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class TargetEsp extends Module implements ModulePreview {
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public ModeSetting type = new ModeSetting(
      this,
      "Режим",
      "Призраки",
      "Картинка",
      "Кольцо",
      "Бублик",
      "Кубики",
      "Ореол",
      "Треугольник",
      "Молния",
      "Спираль",
      "Орбита",
      "Угли",
      "Руны",
      "Пульс",
      "Цепи",
      "Души",
      "Celestial",
      "Кристаллы",
      "Ромб",
      "Sword",
      "Cylinder",
      "Marker",
      "Skull",
      "Aura",
      "Гроза",
      "Пентограма",
      "Атом",
      "Карты",
      "Мечи",
      "Девочка-целочка"
   );
   public SliderSetting lightningCount = new SliderSetting(this, "Количество молний", 4.0F, 1.0F, 12.0F, 1.0F).setVisible(() -> this.type.is("Гроза"));
   public SliderSetting lightningWidth = new SliderSetting(this, "Толщина молний", 1.0F, 0.2F, 3.0F, 0.1F).setVisible(() -> this.type.is("Гроза"));
   public SliderSetting sysRadius = new SliderSetting(this, "Радиус", 0.7F, 0.4F, 1.6F, 0.05F).setVisible(this::isParticleMode);
   public SliderSetting sysSize = new SliderSetting(this, "Размер частиц", 0.17F, 0.04F, 0.4F, 0.01F).setVisible(this::isParticleMode);
   public SliderSetting sysDensity = new SliderSetting(this, "Плотность", 60.0F, 10.0F, 120.0F, 5.0F).setVisible(this::isParticleMode);
   public SliderSetting sysSpeed = new SliderSetting(this, "Скорость", 0.7F, 0.1F, 2.0F, 0.1F).setVisible(this::isParticleMode);
   public ModeSetting typeGhost = new ModeSetting(this, "Тип призраков", "1", "2", "3", "4").setVisible(() -> this.type.is("Призраки"));
   public ModeSetting typeImages = new ModeSetting(this, "Тип картинки", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11")
      .setVisible(() -> this.type.is("Картинка"));
   public SliderSetting chainSpeed = new SliderSetting(this, "Скорость цепей", 1.0F, 0.1F, 4.0F, 0.05F).setVisible(() -> this.type.is("Цепи"));
   public SliderSetting soulSpeed = new SliderSetting(this, "Скорость душ", 1.0F, 0.1F, 4.0F, 0.05F).setVisible(() -> this.type.is("Души"));
   public SliderSetting celestialSpeed = new SliderSetting(this, "Скорость Celestial", 1.0F, 0.1F, 4.0F, 0.05F).setVisible(() -> this.type.is("Celestial"));
   public SliderSetting crystalSpeed = new SliderSetting(this, "Скорость кристаллов", 1.0F, 0.1F, 4.0F, 0.05F).setVisible(() -> this.type.is("Кристаллы"));
   public SliderSetting diamondSpeed = new SliderSetting(this, "Скорость ромба", 1.0F, 0.1F, 4.0F, 0.05F).setVisible(() -> this.type.is("Ромб"));
   public SliderSetting diamondSize = new SliderSetting(this, "Размер ромба", 0.7F, 0.3F, 1.5F, 0.05F).setVisible(() -> this.type.is("Ромб"));
   public ModeSetting diamondStyle = new ModeSetting(this, "Стиль ромба", "Стиль 1", "Стиль 2", "Скобки", "Маркер").setVisible(() -> this.type.is("Ромб"));
   public SliderSetting speed = new SliderSetting(this, "Скорость призраков", 900.0F, 400.0F, 1500.0F, 50.0F).setVisible(() -> this.type.is("Призраки"));
   public SliderSetting sizeGlow = new SliderSetting(this, "Сила свечения", 0.35F, 0.1F, 0.75F, 0.05F).setVisible(() -> this.type.is("Призраки"));
   public SliderSetting sizeGlowOFF = new SliderSetting(this, "Размер свечения", 0.7F, 0.1F, 2.0F, 0.05F).setVisible(() -> this.type.is("Призраки"));
   public SliderSetting speed_2 = new SliderSetting(this, "Скорость картинки", 1000.0F, 500.0F, 3000.0F, 100.0F).setVisible(() -> this.type.is("Картинка"));
   public SliderSetting size_2 = new SliderSetting(this, "Размер картинки", 1.0F, 0.25F, 3.0F, 0.05F).setVisible(() -> this.type.is("Картинка"));
   public SliderSetting ringSpeed = new SliderSetting(this, "Скорость кольца", 1800.0F, 600.0F, 4000.0F, 100.0F).setVisible(() -> this.type.is("Кольцо"));
   public SliderSetting bublikSpeed = new SliderSetting(this, "Скорость бублика", 1400.0F, 600.0F, 4000.0F, 100.0F).setVisible(() -> this.type.is("Бублик"));
   public SliderSetting bublikSize = new SliderSetting(this, "Размер бублика", 1.0F, 0.5F, 2.5F, 0.05F).setVisible(() -> this.type.is("Бублик"));
   private final PreviewSettings previewSettings = PreviewSettings.of(this, 4.0F, 0.0F, 2.0F);
   private final class_9799 boxAllocator = new class_9799(262144);
   private class_1309 previewTarget;
   public class_1309 target = null;
   public Animation alpha = new Animation();
   public Animation alpha_2 = new Animation();
   private float animationNurik = 0.0F;
   private long currentTimeSpirits = 0L;
   private static final RenderPipeline RING_FILL_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField56860()})
         .withLocation(class_2960.method_60655("client", "ring_esp_fill"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderPipeline RING_LINE_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField56860()})
         .withLocation(class_2960.method_60655("client", "ring_esp_line"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final class_1921 RING_FILL_LAYER = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940(
      "ring_esp_fill", class_12247.method_75927(RING_FILL_PIPELINE).method_75929(65536).method_75938()
   );
   public static final class_1921 RING_LINE_LAYER = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940(
      "ring_esp_line", class_12247.method_75927(RING_LINE_PIPELINE).method_75929(16384).method_75938()
   );
   public static final RenderPipeline CHAIN_ESP_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField60125()})
         .withLocation("pipeline/chain_tex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> CHAIN_ESP = class_156.method_34866(texture -> {
      class_12247 setup = class_12247.method_75927(CHAIN_ESP_PIPELINE).method_75934("Sampler0", texture).method_75937().method_75929(1536).method_75938();
      return ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940("chain_tex", setup);
   });
   public static final RenderPipeline ROMB_ESP_PIPELINE = ru.mentions.utils.render.RenderPipelineHelper.invokeMethod67887(
      RenderPipeline.builder(new Snippet[]{ru.mentions.utils.render.RenderPipelineHelper.getField60125()})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> ROMB_ESP = class_156.method_34866(texture -> {
      class_12247 setup = class_12247.method_75927(ROMB_ESP_PIPELINE).method_75934("Sampler0", texture).method_75937().method_75929(1536).method_75938();
      return ru.mentions.utils.render.RenderPipelineHelper.invokeMethod75940("wtex", setup);
   });
   private class_1309 lastHitTarget = null;
   private long lastHitAt = 0L;

   public boolean isNativeMeshMode() {
      return this.type.is("Sword") || this.type.is("Cylinder") || this.type.is("Marker") || this.type.is("Skull") || this.type.is("Aura");
   }

   public boolean isParticleMode() {
      return this.type.is("Ореол")
         || this.type.is("Треугольник")
         || this.type.is("Молния")
         || this.type.is("Спираль")
         || this.type.is("Орбита")
         || this.type.is("Угли")
         || this.type.is("Руны")
         || this.type.is("Пульс");
   }

   public boolean isLegacyShapeMode() {
      return this.type.is("Цепи")
         || this.type.is("Девочка-целочка")
         || this.type.is("Души")
         || this.type.is("Celestial")
         || this.type.is("Кристаллы")
         || this.type.is("Ромб")
         || this.type.is("Пентограма")
         || this.type.is("Атом")
         || this.type.is("Карты")
         || this.type.is("Мечи");
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.boxAllocator.method_60809();
   }

   @Override
   public PreviewSettings previewSettings() {
      return this.previewSettings;
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   @Override
   public void previewStart(PreviewContext ctx) {
      this.previewTarget = ctx.dummy();
   }

   @Override
   public void previewTick(PreviewContext ctx) {
      this.previewTarget = ctx.dummy();
   }

   @Override
   public void previewSpawn(PreviewContext ctx) {
      if (ctx.dummy() != null) {
         ctx.dummy().field_6235 = 10;
      }
   }

   @Override
   public void previewStop() {
      this.previewTarget = null;
      this.target = null;
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (event.getTarget() instanceof class_1309 living && living.method_5805()) {
         this.lastHitTarget = living;
         this.lastHitAt = System.currentTimeMillis();
         LightningTargetESP.getInstance().onAttack(living);
      }
   }

   private class_1309 resolveTarget() {
      if (AttackAura.target != null && AttackAura.target.method_5805()) {
         return AttackAura.target;
      } else {
         return this.lastHitTarget != null && this.lastHitTarget.method_5805() && System.currentTimeMillis() - this.lastHitAt < 4000L
            ? this.lastHitTarget
            : null;
      }
   }

   @EventHandler
   public void onRender(EventRender3D e) {
      this.alpha.update();
      class_1309 currentTarget = this.previewTarget != null ? this.previewTarget : this.resolveTarget();
      if (currentTarget != null) {
         this.target = currentTarget;
      }

      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.alpha.run(currentTarget != null ? 1.0 : 0.0, 0.15F, Easings.SINE_OUT);
         float alphaPC = this.alpha.get();
         class_4598 immediate = class_4597.method_22991(this.boxAllocator);
         if (alphaPC > 0.001F && this.target != null && this.type.is("Призраки") && this.typeGhost.is("2")) {
            long currentTime2 = System.currentTimeMillis();
            if (this.currentTimeSpirits == 0L) {
               this.currentTimeSpirits = currentTime2;
            }

            long timeDiff;
            if ((timeDiff = currentTime2 - this.currentTimeSpirits) > 0L) {
               this.animationNurik = this.animationNurik + (float)(5L * timeDiff) / (float)this.speed.getValue().longValue();
            }

            this.currentTimeSpirits = currentTime2;
            class_4587 matrices = e.getMatrixStack();
            class_243 lerpedPos = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos = mc.field_1773.method_19418().method_71156();
            double x = lerpedPos.field_1352 - cameraPos.field_1352;
            double y = lerpedPos.field_1351 - cameraPos.field_1351;
            double z = lerpedPos.field_1350 - cameraPos.field_1350;
            alphaPC = (float)this.alpha.getValue();
            this.alpha_2.update();
            int hurtTicks = this.target.field_6235;
            float hurtPC2 = (float)Math.sin((double)hurtTicks * (Math.PI / 20));
            this.alpha_2.run((double)hurtPC2, 0.1F, Easings.SINE_OUT);
            float atts = this.alpha_2.get();
            int fadeColor = ColorUtil.fade(1);
            int redColor2 = ColorUtil.getColor(200, 70, 70, (int)(255.0F * alphaPC));
            int baseColor = ColorUtil.overCol(ColorUtil.multAlpha(fadeColor, alphaPC), redColor2, atts);
            int n2 = 3;
            int n3 = 12;
            int n4 = 3 * n2;
            matrices.method_22903();
            class_4184 camera = mc.field_1773.method_19418();

            for (int i = 0; i < n4; i += n2) {
               for (int j = 0; j < n3; j++) {
                  float f2 = this.animationNurik + (float)j * 0.1F;
                  float f3 = 0.6F;
                  float f4 = 0.4F;
                  int n5 = (int)Math.pow((double)i, 2.0);
                  matrices.method_22903();
                  double particleX = x + (double)f3 * Math.sin((double)(f2 + (float)n5));
                  double particleY = y + (double)f4 + 0.3F * Math.sin((double)(this.animationNurik + (float)j * 0.2F)) + (double)(0.2F * (float)i);
                  double particleZ = z + (double)f3 * Math.cos((double)(f2 - (float)n5));
                  matrices.method_22904(particleX, particleY, particleZ);
                  float scale2 = (0.006F + (float)j / 2000.0F) * alphaPC;
                  matrices.method_22905(scale2, scale2, scale2);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
                  Matrix4f matrix = matrices.method_23760().method_23761();
                  class_4588 consumer3 = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png")));
                  int n7 = -20;
                  int n8 = 35;
                  consumer3.method_22918(matrix, (float)n7, (float)(n7 + n8), 0.0F)
                     .method_39415(baseColor)
                     .method_22913(0.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)(n7 + n8), 0.0F)
                     .method_39415(baseColor)
                     .method_22913(1.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)n7, 0.0F)
                     .method_39415(baseColor)
                     .method_22913(1.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)n7, (float)n7, 0.0F)
                     .method_39415(baseColor)
                     .method_22913(0.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  n7 = (int)(-20.0F - 20.0F * this.sizeGlowOFF.getValue());
                  n8 = (int)(35.0F + 40.0F * this.sizeGlowOFF.getValue());
                  consumer3.method_22918(matrix, (float)n7, (float)(n7 + n8), 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(0.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)(n7 + n8), 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(1.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)n7, 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(1.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)n7, (float)n7, 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(0.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  matrices.method_22909();
               }
            }

            matrices.method_22909();
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Призраки") && this.typeGhost.is("4")) {
            long currentTime2x = System.currentTimeMillis();
            if (this.currentTimeSpirits == 0L) {
               this.currentTimeSpirits = currentTime2x;
            }

            long timeDiff;
            if ((timeDiff = currentTime2x - this.currentTimeSpirits) > 0L) {
               this.animationNurik = this.animationNurik + (float)(5L * timeDiff) / (float)this.speed.getValue().longValue();
            }

            this.currentTimeSpirits = currentTime2x;
            class_4587 matrices = e.getMatrixStack();
            class_243 lerpedPos = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos = mc.field_1773.method_19418().method_71156();
            double x = lerpedPos.field_1352 - cameraPos.field_1352;
            double y = lerpedPos.field_1351 - cameraPos.field_1351;
            double z = lerpedPos.field_1350 - cameraPos.field_1350;
            alphaPC = (float)this.alpha.getValue();
            this.alpha_2.update();
            int hurtTicks = this.target.field_6235;
            float hurtPC2 = (float)Math.sin((double)hurtTicks * (Math.PI / 20));
            this.alpha_2.run((double)hurtPC2, 0.1F, Easings.SINE_OUT);
            float atts = this.alpha_2.get();
            int fadeColor = ColorUtil.fade(1);
            int redColor2 = ColorUtil.getColor(200, 70, 70, (int)(255.0F * alphaPC));
            int baseColor = ColorUtil.overCol(ColorUtil.multAlpha(fadeColor, alphaPC), redColor2, atts);
            int n2 = 3;
            int n3 = 24;
            int n4 = 3 * n2;
            matrices.method_22903();
            class_4184 camera = mc.field_1773.method_19418();

            for (int i = 0; i < n4; i += n2) {
               for (int j = 0; j < n3; j++) {
                  float f2 = this.animationNurik + (float)j * 0.05F;
                  float f3 = this.target.method_17681();
                  float f4 = 0.45F;
                  int n5 = (int)Math.pow((double)i, 2.0);
                  matrices.method_22903();
                  double particleX = x + (double)f3 * Math.sin((double)(f2 + (float)n5));
                  double particleY = y + (double)f4 + 0.1F * Math.sin((double)(this.animationNurik + (float)j * 0.1F)) + (double)(0.2F * (float)i);
                  double particleZ = z + (double)f3 * Math.cos((double)(f2 - (float)n5));
                  matrices.method_22904(particleX, particleY, particleZ);
                  float scale2 = (0.009F + (float)j / 2000.0F) * alphaPC;
                  matrices.method_22905(scale2, scale2, scale2);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
                  Matrix4f matrix = matrices.method_23760().method_23761();
                  class_4588 consumer3 = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png")));
                  int n7 = -12;
                  int n8 = 16;
                  consumer3.method_22918(matrix, (float)n7, (float)(n7 + n8), 0.0F)
                     .method_39415(baseColor)
                     .method_22913(0.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)(n7 + n8), 0.0F)
                     .method_39415(baseColor)
                     .method_22913(1.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)n7, 0.0F)
                     .method_39415(baseColor)
                     .method_22913(1.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)n7, (float)n7, 0.0F)
                     .method_39415(baseColor)
                     .method_22913(0.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  n7 = (int)(-12.0F - 20.0F * this.sizeGlowOFF.getValue());
                  n8 = (int)(16.0F + 40.0F * this.sizeGlowOFF.getValue());
                  consumer3.method_22918(matrix, (float)n7, (float)(n7 + n8), 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(0.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)(n7 + n8), 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(1.0F, 1.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)(n7 + n8), (float)n7, 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(1.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  consumer3.method_22918(matrix, (float)n7, (float)n7, 0.0F)
                     .method_39415(ColorUtil.replAlpha(baseColor, alphaPC * this.sizeGlow.getValue()))
                     .method_22913(0.0F, 0.0F)
                     .method_22922(class_4608.field_21444)
                     .method_60803(15728880)
                     .method_22914(0.0F, 0.0F, 1.0F);
                  matrices.method_22909();
               }
            }

            matrices.method_22909();
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Призраки") && this.typeGhost.is("1")) {
            long speed_f = this.speed.getValue().longValue();
            long currentTime = System.currentTimeMillis();
            if (this.currentTimeSpirits == 0L) {
               this.currentTimeSpirits = currentTime;
            }

            long timeDiff2;
            if ((timeDiff2 = currentTime - this.currentTimeSpirits) > 0L) {
               this.animationNurik += (float)(5L * timeDiff2) / (float)speed_f;
            }

            this.currentTimeSpirits = currentTime;
            int hurtTicks2 = this.target.field_6235;
            float hurtPC3 = (float)Math.sin((double)hurtTicks2 * (Math.PI / 20));
            int redColor = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int color5 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColor, hurtPC3);
            int color2 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColor, hurtPC3);
            int color32 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColor, hurtPC3);
            int color4 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(360), alphaPC), redColor, hurtPC3);
            class_4587 matrices2 = e.getMatrixStack();
            class_243 lerpedPos2 = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos2 = mc.field_1773.method_19418().method_71156();
            long time = System.currentTimeMillis();
            double speed = (double)time / (double)speed_f;
            float radius = this.target.method_17681() + 0.25F - 0.25F * alphaPC;
            int ghostCount = 3;
            int cound = 10;

            for (int i = 0; i < ghostCount; i++) {
               for (int s = 0; s < cound; s++) {
                  matrices2.method_22903();
                  float f2 = this.animationNurik + (float)s * 0.1F;
                  double angle = speed + (double)i * ((Math.PI * 2) / (double)ghostCount);
                  double offX = Math.cos(angle + (double)f2) * (double)radius;
                  double offZ = Math.sin(angle + (double)f2) * (double)radius;
                  double offY = Math.sin(speed + (double)i) * 0.6 + 0.15 * Math.sin(speed + (double)((float)s * 0.11F));
                  matrices2.method_22904(
                     lerpedPos2.field_1352 - cameraPos2.field_1352 + offX,
                     lerpedPos2.field_1351 - cameraPos2.field_1351 + (double)(this.target.method_17682() / 2.0F) + offY,
                     lerpedPos2.field_1350 - cameraPos2.field_1350 + offZ
                  );
                  matrices2.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
                  matrices2.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
                  float scale = (0.12F + (float)s / 40.0F) * 1.25F * alphaPC;
                  matrices2.method_22905(scale, scale, scale);
                  Matrix4f matrix2 = matrices2.method_23760().method_23761();
                  class_4588 consumer = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png")));
                  drawGradientQuad(consumer, matrix2, color5, color2, color32, color4, (int)(255.0F * alphaPC));
                  scale = (0.12F + (float)s / 40.0F) * 100.0F * this.sizeGlowOFF.getValue() / 10.0F * alphaPC;
                  matrices2.method_22905(scale, scale, scale);
                  class_4588 consumer2 = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/glow.png")));
                  drawGradientQuad(consumer2, matrix2, color5, color2, color32, color4, (int)(255.0F * this.sizeGlow.getValue() * 1.0F * alphaPC));
                  matrices2.method_22909();
               }
            }
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Призраки") && this.typeGhost.is("3")) {
            long speed_fx = this.speed.getValue().longValue();
            long currentTimex = System.currentTimeMillis();
            if (this.currentTimeSpirits == 0L) {
               this.currentTimeSpirits = currentTimex;
            }

            long timeDiff3;
            if ((timeDiff3 = currentTimex - this.currentTimeSpirits) > 0L) {
               this.animationNurik += (float)(5L * timeDiff3) / (float)speed_fx;
            }

            this.currentTimeSpirits = currentTimex;
            int hurtTicks3 = this.target.field_6235;
            float hurtPC4 = (float)Math.sin((double)hurtTicks3 * (Math.PI / 20));
            int redColor = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int color6 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColor, hurtPC4);
            int color2 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColor, hurtPC4);
            int color33 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColor, hurtPC4);
            int color4 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(360), alphaPC), redColor, hurtPC4);
            class_4587 matrices3 = e.getMatrixStack();
            class_243 lerpedPos3 = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos3 = mc.field_1773.method_19418().method_71156();
            long time = System.currentTimeMillis();
            double speed = (double)time / (double)speed_fx;
            float radius = this.target.method_17681() + 0.25F - 0.25F * alphaPC;
            int ghostCount = 3;
            int cound = 9;

            for (int i = 0; i < ghostCount; i++) {
               for (int s = 0; s < cound; s++) {
                  matrices3.method_22903();
                  float f2 = this.animationNurik + (float)s * 0.1F;
                  double angle = speed + (double)i * ((Math.PI * 2) / (double)ghostCount);
                  double offX = Math.cos(angle + (double)f2) * (double)radius;
                  double offZ = Math.sin(angle + (double)f2) * (double)radius;
                  double offY = Math.sin(speed) * 0.7 + 0.2 * Math.sin(speed + (double)((float)s * 0.11F));
                  matrices3.method_22904(
                     lerpedPos3.field_1352 - cameraPos3.field_1352 + offX,
                     lerpedPos3.field_1351 - cameraPos3.field_1351 + (double)(this.target.method_17682() / 2.0F) + offY,
                     lerpedPos3.field_1350 - cameraPos3.field_1350 + offZ
                  );
                  matrices3.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
                  matrices3.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
                  float scale = (0.12F + (float)s / 50.0F) * 1.5F;
                  matrices3.method_22905(scale, scale, scale);
                  Matrix4f matrix3 = matrices3.method_23760().method_23761();
                  class_4588 consumer = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png")));
                  drawGradientQuad(consumer, matrix3, color6, color2, color33, color4, (int)(255.0F * alphaPC));
                  scale = (0.12F + (float)s / 50.0F) * 100.0F * this.sizeGlowOFF.getValue() / 10.0F;
                  matrices3.method_22905(scale, scale, scale);
                  class_4588 consumer2 = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/glow.png")));
                  drawGradientQuad(consumer2, matrix3, color6, color2, color33, color4, (int)(255.0F * this.sizeGlow.getValue() * 1.0F * alphaPC));
                  matrices3.method_22909();
               }
            }
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Картинка")) {
            int hurtTicks4 = this.target.field_6235;
            float hurtPC = (float)Math.sin((double)hurtTicks4 * (Math.PI / 20));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPC, 0.15F, Easings.SINE_OUT);
            int redColor3 = ColorUtil.getColor(185, 80, 80, (int)(255.0F * alphaPC));
            int color = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColor3, this.alpha_2.get());
            int color22 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColor3, this.alpha_2.get());
            int color34 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColor3, this.alpha_2.get());
            int color42 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(360), alphaPC), redColor3, this.alpha_2.get());
            class_243 lerpedPos4 = this.target.method_30950(e.getTickDelta());
            double x2 = lerpedPos4.field_1352;
            double y2 = lerpedPos4.field_1351;
            double z2 = lerpedPos4.field_1350;
            class_243 cameraPos4 = mc.field_1773.method_19418().method_71156();
            long currentTimeMillis = System.currentTimeMillis();
            float rotate = (float)MathUtil.clamps(
               0.0, 720.0, (Math.sin((double)currentTimeMillis / this.speed_2.getValue().doubleValue()) + 1.0) / 2.0 * 360.0 * 2.0
            );
            float size = this.size_2.getValue() - this.size_2.getValue() * 0.4F * this.alpha_2.get() + 0.8F - 0.8F * alphaPC;
            String imageName = "target.png";
            if (this.typeImages.is("2")) {
               imageName = "marker.png";
            } else if (this.typeImages.is("3")) {
               imageName = "targets.png";
            } else if (this.typeImages.is("4")) {
               imageName = "target1.png";
            } else if (this.typeImages.is("5")) {
               imageName = "target2.png";
            } else if (this.typeImages.is("6")) {
               imageName = "target_esp_1.png";
            } else if (this.typeImages.is("7")) {
               imageName = "target_esp_2.png";
            } else if (this.typeImages.is("8")) {
               imageName = "target_esp_3.png";
            } else if (this.typeImages.is("9")) {
               imageName = "target_esp_4.png";
            } else if (this.typeImages.is("10")) {
               imageName = "target_esp_5.png";
            } else if (this.typeImages.is("11")) {
               imageName = "bloom.png";
            }

            drawBillboardTESP(
               immediate,
               e.getMatrixStack(),
               mc.field_1773.method_19418(),
               x2 - cameraPos4.field_1352,
               y2 - cameraPos4.field_1351 + (double)(this.target.method_17682() / 1.75F),
               z2 - cameraPos4.field_1350,
               size,
               size,
               rotate,
               visualTexture(imageName),
               ColorUtil.overCol(color, color22, 0.5F),
               (int)(255.0F * alphaPC)
            );
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Бублик")) {
            int hurtTicks5 = this.target.field_6235;
            float hurtPC = (float)Math.sin((double)hurtTicks5 * (Math.PI / 20));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPC, 0.1F, Easings.SINE_OUT);
            int redColor4 = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int color = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColor4, this.alpha_2.get());
            int color23 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColor4, this.alpha_2.get());
            int color35 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColor4, this.alpha_2.get());
            int color43 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(360), alphaPC), redColor4, this.alpha_2.get());
            class_4587 matrices5 = e.getMatrixStack();
            class_243 lerpedPos5 = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos5 = mc.field_1773.method_19418().method_71156();
            double duration = this.bublikSpeed.getValue().doubleValue();
            double elapsed = (double)System.currentTimeMillis() % duration;
            boolean down = elapsed > duration / 2.0;
            double raw = elapsed / (duration / 2.0);
            raw = down ? raw - 1.0 : 1.0 - raw;
            double progress = raw < 0.5 ? 2.0 * raw * raw : 1.0 - Math.pow(-2.0 * raw + 2.0, 2.0) / 2.0;
            float height = this.target.method_17682();
            float yBase = 0.12F + (float)progress * Math.max(0.2F, height - 0.24F);
            float direction = down ? -1.0F : 1.0F;
            double spin = (double)System.currentTimeMillis() / (this.bublikSpeed.getValue().doubleValue() * 0.55);
            float radius = this.target.method_17681() - 0.15F;
            int count = (int)(80.0F * alphaPC);

            for (int layer = 2; layer >= 0; layer--) {
               float layerAlpha = alphaPC * (1.0F - (float)layer * 0.28F);
               float yLayer = yBase + direction * (float)layer * 0.075F;
               float radiusLayer = radius + (float)layer * 0.025F;

               for (int i2 = 0; i2 < count; i2++) {
                  float pc = (float)i2 / (float)count;
                  double angle = (double)(pc * (float) (Math.PI * 2)) + spin + (double)layer * 0.18;
                  double x3 = Math.cos(angle) * (double)radiusLayer;
                  double z3 = Math.sin(angle) * (double)radiusLayer;
                  float size = (0.2F + (float)layer * 0.018F) * this.bublikSize.getValue();
                  int localAlpha = (int)((float)(layer == 0 ? 255 : 105) * layerAlpha);
                  if (layer == 0) {
                     matrices5.method_22903();
                     matrices5.method_22904(
                        lerpedPos5.field_1352 - cameraPos5.field_1352 + x3,
                        lerpedPos5.field_1351 - cameraPos5.field_1351 + (double)yLayer,
                        lerpedPos5.field_1350 - cameraPos5.field_1350 + z3
                     );
                     matrices5.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
                     matrices5.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
                     matrices5.method_22907(class_7833.field_40718.rotationDegrees((float)Math.toDegrees(-angle) + (float)(spin * 80.0)));
                     matrices5.method_22905(size * 1.25F, size * 1.25F, size);
                     drawGradientQuad(
                        immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png"))),
                        matrices5.method_23760().method_23761(),
                        color,
                        color23,
                        color35,
                        color43,
                        localAlpha
                     );
                     matrices5.method_22909();
                  }
               }
            }
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Кольцо")) {
            int hurtTicks6 = this.target.field_6235;
            float hurtPC = (float)Math.sin((double)hurtTicks6 * (Math.PI / 10));
            int redColor5 = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            double duration = this.ringSpeed.getValue().doubleValue();
            double elapsed = (double)System.currentTimeMillis() % duration;
            boolean side = elapsed > duration / 2.0;
            double raw = elapsed / (duration / 2.0);
            raw = side ? raw - 1.0 : 1.0 - raw;
            double progress = raw < 0.5 ? 2.0 * raw * raw : 1.0 - Math.pow(-2.0 * raw + 2.0, 2.0) / 2.0;
            float height2 = this.target.method_17682();
            double eased = (double)height2 / 1.7 * (progress > 0.5 ? 1.0 - progress : progress) * (double)(side ? -1 : 1) / 0.7;
            class_4587 matrices6 = e.getMatrixStack();
            class_243 lerpedPos6 = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos6 = mc.field_1773.method_19418().method_71156();
            matrices6.method_22903();
            matrices6.method_22904(
               lerpedPos6.field_1352 - cameraPos6.field_1352, lerpedPos6.field_1351 - cameraPos6.field_1351, lerpedPos6.field_1350 - cameraPos6.field_1350
            );
            Matrix4f matrix4 = matrices6.method_23760().method_23761();
            float radius = this.target.method_17681() - 0.1F + 0.35F - 0.35F * alphaPC;
            float yBase = (float)((double)height2 * progress);
            float yTop = (float)((double)height2 * progress + eased);
            class_4588 fillBuf = immediate.method_73477(RING_FILL_LAYER);

            for (int seg = 0; seg < 360; seg++) {
               float a0 = (float)Math.toRadians((double)seg);
               float a1 = (float)Math.toRadians((double)(seg + 1));
               int c0 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(seg), alphaPC), redColor5, hurtPC);
               int c1 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(seg), alphaPC), redColor5, hurtPC);
               float x0 = (float)(Math.cos((double)a0) * (double)radius);
               float z0 = (float)(Math.sin((double)a0) * (double)radius);
               float x1 = (float)(Math.cos((double)a1) * (double)radius);
               float z1 = (float)(Math.sin((double)a1) * (double)radius);
               fillBuf.method_22918(matrix4, x0, yBase, z0).method_39415(ColorUtil.replAlpha(c0, (int)(120.0F * alphaPC)));
               fillBuf.method_22918(matrix4, x1, yBase, z1).method_39415(ColorUtil.replAlpha(c1, (int)(120.0F * alphaPC)));
               fillBuf.method_22918(matrix4, x1, yTop, z1).method_39415(ColorUtil.replAlpha(c1, 0));
               fillBuf.method_22918(matrix4, x0, yTop, z0).method_39415(ColorUtil.replAlpha(c0, 0));
            }

            class_4588 lineBuf = immediate.method_73477(RING_LINE_LAYER);

            for (int seg = 0; seg < 360; seg++) {
               float a0 = (float)Math.toRadians((double)seg);
               float a1 = (float)Math.toRadians((double)(seg + 1));
               int c = ColorUtil.multAlpha(ColorUtil.overCol(ColorUtil.multBright(ColorUtil.fade(1), 0.7F), redColor5, hurtPC), alphaPC);
               lineBuf.method_22918(matrix4, (float)(Math.cos((double)a0) * (double)radius), yBase, (float)(Math.sin((double)a0) * (double)radius))
                  .method_39415(ColorUtil.replAlpha(c, (int)(150.0F * alphaPC)));
               lineBuf.method_22918(matrix4, (float)(Math.cos((double)a1) * (double)radius), yBase, (float)(Math.sin((double)a1) * (double)radius))
                  .method_39415(ColorUtil.replAlpha(c, (int)(150.0F * alphaPC)));
            }

            matrices6.method_22909();
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Кубики")) {
            int hurtTicks7 = this.target.field_6235;
            float hurtPC = (float)Math.sin((double)hurtTicks7 * (Math.PI / 10));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPC, 0.1F, Easings.SINE_OUT);
            int redColor6 = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int color7 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(1), alphaPC), redColor6, this.alpha_2.get());
            class_4587 matrices7 = e.getMatrixStack();
            class_243 cameraPos7 = mc.field_1773.method_19418().method_71156();
            class_243 targetPos = this.target.method_30950(e.getTickDelta());
            float time = (float)Math.cos((double)System.currentTimeMillis() / 2000.0) * 360.0F + this.alpha_2.get() * 20.0F;
            int cound = 12;
            float width = this.target.method_17681() * 1.5F;
            float sizeFI = (1.0F - 0.3F * this.alpha_2.get()) * alphaPC;
            class_4184 camera2 = mc.field_1773.method_19418();

            for (int i3 = 0; i3 < 360; i3 += cound) {
               float val = 0.70000005F;
               float sin = (float)(Math.sin((double)((float)Math.toRadians((double)((float)i3 + time)))) * (double)width * (double)val);
               float cos = (float)(Math.cos((double)((float)Math.toRadians((double)((float)i3 + time)))) * (double)width * (double)val);
               double x4 = targetPos.field_1352 + (double)sin;
               double z4 = targetPos.field_1350 + (double)cos;
               double y3 = targetPos.field_1351 + (double)this.target.method_17682() * Math.abs(MathUtil.sin((double)i3));
               matrices7.method_22903();
               matrices7.method_22904(x4 - cameraPos7.field_1352, y3 - cameraPos7.field_1351, z4 - cameraPos7.field_1350);
               matrices7.method_22907(camera2.method_23767());
               float gs = 0.6F * sizeFI;
               matrices7.method_22905(gs, gs, gs);
               drawGradientQuad(
                  immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_1.png"))),
                  matrices7.method_23760().method_23761(),
                  ColorUtil.multAlpha(color7, 0.3F),
                  ColorUtil.multAlpha(color7, 0.3F),
                  ColorUtil.multAlpha(color7, 0.3F),
                  ColorUtil.multAlpha(color7, 0.3F),
                  (int)(alphaPC * 0.35F * 255.0F)
               );
               matrices7.method_22909();
            }

            for (int var166 = 0; var166 < 360; var166 += cound) {
               float val = 0.70000005F;
               float sin = (float)(Math.sin((double)((float)Math.toRadians((double)((float)var166 + time)))) * (double)width * (double)val);
               float cos = (float)(Math.cos((double)((float)Math.toRadians((double)((float)var166 + time)))) * (double)width * (double)val);
               double x5 = targetPos.field_1352 + (double)sin;
               double z5 = targetPos.field_1350 + (double)cos;
               double y4 = targetPos.field_1351 + (double)this.target.method_17682() * Math.abs(MathUtil.sin((double)var166));
               class_243 cubePos = new class_243(x5, y4, z5);
               Vector3f directionToTarget = new Vector3f(
                     (float)(targetPos.field_1352 - cubePos.field_1352),
                     (float)(targetPos.field_1351 - cubePos.field_1351),
                     (float)(targetPos.field_1350 - cubePos.field_1350)
                  )
                  .normalize();
               matrices7.method_22903();
               matrices7.method_22904(x5 - cameraPos7.field_1352, y4 - cameraPos7.field_1351, z5 - cameraPos7.field_1350);
               matrices7.method_22907(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), directionToTarget));
               Matrix4f matrix5 = matrices7.method_23760().method_23761();
               float size = 0.06F * sizeFI;
               drawCubeFillTESP(immediate.method_73477(RING_FILL_LAYER), matrix5, size, ColorUtil.replAlpha(color7, (int)(alphaPC * 0.2F * 255.0F)));
               matrices7.method_22909();
            }

            for (int var167 = 0; var167 < 360; var167 += cound) {
               float val = 0.70000005F;
               float sin = (float)(Math.sin((double)((float)Math.toRadians((double)((float)var167 + time)))) * (double)width * (double)val);
               float cos = (float)(Math.cos((double)((float)Math.toRadians((double)((float)var167 + time)))) * (double)width * (double)val);
               double x6 = targetPos.field_1352 + (double)sin;
               double z6 = targetPos.field_1350 + (double)cos;
               double y5 = targetPos.field_1351 + (double)this.target.method_17682() * Math.abs(MathUtil.sin((double)var167));
               class_243 cubePos = new class_243(x6, y5, z6);
               Vector3f directionToTarget = new Vector3f(
                     (float)(targetPos.field_1352 - cubePos.field_1352),
                     (float)(targetPos.field_1351 - cubePos.field_1351),
                     (float)(targetPos.field_1350 - cubePos.field_1350)
                  )
                  .normalize();
               matrices7.method_22903();
               matrices7.method_22904(x6 - cameraPos7.field_1352, y5 - cameraPos7.field_1351, z6 - cameraPos7.field_1350);
               matrices7.method_22907(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), directionToTarget));
               Matrix4f matrix6 = matrices7.method_23760().method_23761();
               float size = 0.06F * sizeFI;
               drawCubeOutlineTESP(immediate.method_73477(RING_LINE_LAYER), matrix6, size, ColorUtil.replAlpha(color7, (int)(alphaPC * 255.0F)));
               matrices7.method_22909();
            }
         }

         if (alphaPC > 0.001F && this.target != null && this.isLegacyShapeMode()) {
            class_4587 matricesL = e.getMatrixStack();
            class_243 lerpedPosL = this.target.method_30950(e.getTickDelta());
            class_243 cameraPosL = mc.field_1773.method_19418().method_71156();
            class_4184 cameraL = mc.field_1773.method_19418();
            double txL = lerpedPosL.field_1352 - cameraPosL.field_1352;
            double tyL = lerpedPosL.field_1351 - cameraPosL.field_1351;
            double tzL = lerpedPosL.field_1350 - cameraPosL.field_1350;
            long nowL = System.currentTimeMillis();
            float radiusL = this.target.method_17681() * 1.15F;
            float heightL = this.target.method_17682();
            int shapeColor = ColorUtil.multAlpha(ColorUtil.fade(1), alphaPC);
            if (this.type.is("Цепи") || this.type.is("Девочка-целочка")) {
               renderChainsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, this.chainSpeed.getValue());
            } else if (this.type.is("Души")) {
               renderSoulsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, this.soulSpeed.getValue());
            } else if (this.type.is("Celestial")) {
               renderCelestialESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, this.celestialSpeed.getValue());
            } else if (this.type.is("Кристаллы")) {
               renderCrystalsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL, this.crystalSpeed.getValue());
            } else if (this.type.is("Ромб")) {
               renderDiamondESP(
                  immediate,
                  matricesL,
                  cameraL,
                  txL,
                  tyL,
                  tzL,
                  heightL,
                  shapeColor,
                  alphaPC,
                  nowL,
                  this.diamondSpeed.getValue(),
                  this.diamondSize.getValue(),
                  this.diamondStyle.getIndex()
               );
            } else if (this.type.is("Пентограма")) {
               renderPentagramESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            } else if (this.type.is("Атом")) {
               renderAtomESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            } else if (this.type.is("Карты")) {
               renderCardsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            } else if (this.type.is("Мечи")) {
               renderSwordsESP(immediate, matricesL, cameraL, txL, tyL, tzL, radiusL, heightL, shapeColor, alphaPC, nowL);
            }
         }

         if (alphaPC > 0.001F && this.target != null && this.type.is("Гроза")) {
            LightningTargetESP.getInstance().lightningCount = this.lightningCount.getValue();
            LightningTargetESP.getInstance().lightningWidth = this.lightningWidth.getValue();
            LightningTargetESP.getInstance().render(immediate, e.getMatrixStack(), mc.field_1773.method_19418(), e.getTickDelta(), this.target, alphaPC);
         }

         if (alphaPC > 0.001F && this.target != null && this.isNativeMeshMode()) {
            class_4587 nativeMatrices = e.getMatrixStack();
            class_243 nativePos = this.target.method_30950(e.getTickDelta());
            class_243 nativeCameraPos = mc.field_1773.method_19418().method_71156();
            double nx = nativePos.field_1352 - nativeCameraPos.field_1352;
            double ny = nativePos.field_1351 - nativeCameraPos.field_1351;
            double nz = nativePos.field_1350 - nativeCameraPos.field_1350;
            float nr = Math.max(0.35F, this.target.method_17681() * 0.72F);
            float nh = this.target.method_17682();
            int nc = ColorUtil.multAlpha(ColorUtil.fade(1), alphaPC);
            long nt = System.currentTimeMillis();
            class_4588 lines = immediate.method_73477(RING_LINE_LAYER);
            if (this.type.is("Cylinder")) {
               renderNativeCylinder(lines, nativeMatrices, nx, ny, nz, nr, nh, nc, nt);
            } else if (this.type.is("Marker")) {
               renderNativeMarker(lines, nativeMatrices, nx, ny, nz, nr, nh, nc, nt);
            } else if (this.type.is("Sword")) {
               renderNativeSword(lines, nativeMatrices, nx, ny, nz, nr, nh, nc, nt);
            } else if (this.type.is("Skull")) {
               renderNativeSkull(lines, nativeMatrices, nx, ny, nz, nr, nh, nc, nt);
            } else if (this.type.is("Aura")) {
               renderNativeAura(lines, nativeMatrices, nx, ny, nz, nr, nh, nc, nt);
            }
         }

         if (alphaPC > 0.001F && this.target != null && this.isParticleMode()) {
            int hurtTicks8 = this.target.field_6235;
            float hurtPC8 = (float)Math.sin((double)hurtTicks8 * (Math.PI / 20));
            this.alpha_2.update();
            this.alpha_2.run((double)hurtPC8, 0.1F, Easings.SINE_OUT);
            int redColor7 = ColorUtil.getColor(255, 100, 100, (int)(255.0F * alphaPC));
            int sysColor = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(1), alphaPC), redColor7, this.alpha_2.get());
            class_4587 matrices8 = e.getMatrixStack();
            class_243 lerpedPos8 = this.target.method_30950(e.getTickDelta());
            class_243 cameraPos8 = mc.field_1773.method_19418().method_71156();
            class_4184 camera8 = mc.field_1773.method_19418();
            double tx = lerpedPos8.field_1352 - cameraPos8.field_1352;
            double ty = lerpedPos8.field_1351 - cameraPos8.field_1351;
            double tz = lerpedPos8.field_1350 - cameraPos8.field_1350;
            double sysTime = (double)System.currentTimeMillis() / 1000.0 * this.sysSpeed.getValue().doubleValue();
            float radius = this.sysRadius.getValue() * this.target.method_17681() * 1.6F;
            float psize = this.sysSize.getValue();
            int density = Math.round(this.sysDensity.getValue());
            int effect = 0;
            if (this.type.is("Треугольник")) {
               effect = 1;
            } else if (this.type.is("Молния")) {
               effect = 2;
            } else if (this.type.is("Спираль")) {
               effect = 3;
            } else if (this.type.is("Орбита")) {
               effect = 4;
            } else if (this.type.is("Угли")) {
               effect = 5;
            } else if (this.type.is("Руны")) {
               effect = 6;
            } else if (this.type.is("Пульс")) {
               effect = 7;
            }

            class_4588 sysBuf = immediate.method_73477(ROMB_ESP.apply(class_2960.method_60655("client", "textures/visuals/particles_3.png")));
            int alpha255 = (int)(255.0F * alphaPC);
            if (effect == 0) {
               for (int si = 0; si < density; si++) {
                  double angle = sysTime * 2.0 + (double)si * ((Math.PI * 2) / (double)density);
                  double bobY = Math.sin(sysTime + (double)si * 0.35) * 0.15;
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * (double)radius,
                     ty + 0.1 + bobY,
                     tz + Math.sin(angle) * (double)radius,
                     psize,
                     sysColor,
                     alpha255
                  );
               }
            } else if (effect == 1) {
               for (int v = 0; v < 3; v++) {
                  double baseAngle = sysTime * 1.6 + (double)v * (Math.PI * 2.0 / 3.0);
                  int trail = density / 3;

                  for (int ti = 0; ti < trail; ti++) {
                     double angle = baseAngle - (double)ti * 0.08;
                     float fade = 1.0F - (float)ti / (float)trail;
                     double py = ty + (double)(this.target.method_17682() * 0.5F) + Math.sin(sysTime * 2.0 + (double)v * 2.0) * 0.2;
                     int a = (int)((float)alpha255 * fade);
                     this.drawSysPoint(
                        matrices8, camera8, sysBuf, tx + Math.cos(angle) * (double)radius, py, tz + Math.sin(angle) * (double)radius, psize * fade, sysColor, a
                     );
                  }
               }
            } else if (effect == 2) {
               double height = (double)this.target.method_17682() + 0.5;
               int segs = Math.max(6, density / 4);
               double px = 0.0;
               double pz = 0.0;

               for (int gi = 0; gi <= segs; gi++) {
                  double frac = (double)gi / (double)segs;
                  double jitter = (Math.sin(sysTime * 20.0 + (double)gi * 3.7) + Math.sin(sysTime * 13.0 + (double)gi * 1.9)) * 0.5;
                  px = px * 0.5 + jitter * 0.12 * (double)radius;
                  pz = pz * 0.5 + Math.cos(sysTime * 17.0 + (double)gi * 2.3) * 0.12 * (double)radius;
                  this.drawSysPoint(matrices8, camera8, sysBuf, tx + px, ty + frac * height, tz + pz, psize * 0.7F, sysColor, alpha255);
               }
            } else if (effect == 3) {
               double height = (double)this.target.method_17682() + 0.4;

               for (int si = 0; si < density; si++) {
                  double frac = ((double)si / (double)density + sysTime * 0.35) % 1.0;
                  double angle = frac * Math.PI * 2.0 * 2.0 + sysTime;
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * (double)radius * 0.8,
                     ty + frac * height,
                     tz + Math.sin(angle) * (double)radius * 0.8,
                     psize * 0.8F,
                     sysColor,
                     alpha255
                  );
               }
            } else if (effect == 4) {
               for (int si = 0; si < density; si++) {
                  double angle = sysTime * 1.8 + (double)si * ((Math.PI * 2) / (double)density);
                  double ox = Math.cos(angle) * (double)radius;
                  double oz = Math.sin(angle) * (double)radius;
                  double tilt = Math.sin(sysTime * 0.9) * 0.6;
                  double py = ty + (double)(this.target.method_17682() * 0.6F) + oz * tilt;
                  this.drawSysPoint(matrices8, camera8, sysBuf, tx + ox, py, tz + oz * Math.cos(tilt), psize, sysColor, alpha255);
               }
            } else if (effect == 5) {
               for (int si = 0; si < density; si++) {
                  double seed = (double)si * 0.61803398875;
                  double frac = (seed + sysTime * 0.4) % 1.0;
                  double angle = seed * Math.PI * 2.0 * 5.0 + sysTime * 0.5;
                  double r = (double)radius * (0.3 + 0.7 * frac);
                  int a = (int)((float)alpha255 * (1.0F - (float)frac));
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * r,
                     ty + frac * (double)this.target.method_17682() * 1.4,
                     tz + Math.sin(angle) * r,
                     psize * (1.0F - (float)frac * 0.6F),
                     sysColor,
                     a
                  );
               }
            } else if (effect == 6) {
               int runes = Math.min(8, Math.max(3, density / 15));

               for (int ri = 0; ri < runes; ri++) {
                  double angle = sysTime * 0.8 + (double)ri * ((Math.PI * 2) / (double)runes);
                  double py = ty + 0.05 + Math.abs(Math.sin(sysTime + (double)ri)) * 0.25;
                  this.drawSysPoint(
                     matrices8,
                     camera8,
                     sysBuf,
                     tx + Math.cos(angle) * (double)radius,
                     py,
                     tz + Math.sin(angle) * (double)radius,
                     psize * 1.6F,
                     sysColor,
                     alpha255
                  );
               }
            } else if (effect == 7) {
               int pulses = 3;

               for (int p = 0; p < pulses; p++) {
                  double frac = (sysTime * 0.7 + (double)p / (double)pulses) % 1.0;
                  double r = (double)radius * (0.2 + frac * 1.3);
                  int a = (int)((float)alpha255 * (1.0F - (float)frac));
                  int ringPts = Math.max(8, density / pulses);

                  for (int qi = 0; qi < ringPts; qi++) {
                     double angle = (double)qi * ((Math.PI * 2) / (double)ringPts);
                     this.drawSysPoint(matrices8, camera8, sysBuf, tx + Math.cos(angle) * r, ty + 0.08, tz + Math.sin(angle) * r, psize * 0.8F, sysColor, a);
                  }
               }
            }
         }

         immediate.method_22993();
      }
   }

   private void drawSysPoint(class_4587 matrices, class_4184 camera, class_4588 buffer, double x, double y, double z, float size, int color, int alpha) {
      if (alpha > 0) {
         matrices.method_22903();
         matrices.method_22904(x, y, z);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
         matrices.method_22905(size, size, size);
         Matrix4f m = matrices.method_23760().method_23761();
         buffer.method_22918(m, -0.5F, -0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(0.0F, 1.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buffer.method_22918(m, 0.5F, -0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(1.0F, 1.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buffer.method_22918(m, 0.5F, 0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(1.0F, 0.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buffer.method_22918(m, -0.5F, 0.5F, 0.0F)
            .method_39415(ColorUtil.replAlpha(color, alpha))
            .method_22913(0.0F, 0.0F)
            .method_22922(class_4608.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         matrices.method_22909();
      }
   }

   private static void drawGradientQuad(class_4588 buffer, Matrix4f matrix, int color, int alpha) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
   }

   private static void drawGradientQuad(class_4588 buffer, Matrix4f matrix, int color, int color2, int color3, int color4, int alpha) {
      buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color, alpha))
         .method_22913(0.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color2, alpha))
         .method_22913(1.0F, 1.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color3, alpha))
         .method_22913(1.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F)
         .method_39415(ColorUtil.replAlpha(color4, alpha))
         .method_22913(0.0F, 0.0F)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
   }

   private static void drawCubeFillTESP(class_4588 buf, Matrix4f m, float s, int color) {
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
   }

   private static void drawCubeOutlineTESP(class_4588 buf, Matrix4f m, float s, int color) {
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
   }

   private static void nativeLine(class_4588 buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
      buffer.method_22918(matrix, x1, y1, z1).method_39415(color);
      buffer.method_22918(matrix, x2, y2, z2).method_39415(color);
   }

   private static void nativeCircle(class_4588 buffer, Matrix4f matrix, float y, float radius, int color, int segments) {
      for (int i = 0; i < segments; i++) {
         float a = (float)((Math.PI * 2) * (double)i / (double)segments);
         float b = (float)((Math.PI * 2) * (double)(i + 1) / (double)segments);
         nativeLine(
            buffer,
            matrix,
            (float)Math.cos((double)a) * radius,
            y,
            (float)Math.sin((double)a) * radius,
            (float)Math.cos((double)b) * radius,
            y,
            (float)Math.sin((double)b) * radius,
            color
         );
      }
   }

   private static void renderNativeCylinder(
      class_4588 buffer, class_4587 matrices, double x, double y, double z, float radius, float height, int color, long time
   ) {
      matrices.method_22903();
      matrices.method_22904(x, y, z);
      Matrix4f m = matrices.method_23760().method_23761();
      nativeCircle(buffer, m, 0.04F, radius, color, 48);
      nativeCircle(buffer, m, height - 0.04F, radius, color, 48);
      nativeCircle(buffer, m, 0.04F + (height - 0.08F) * (0.5F + 0.5F * (float)Math.sin((double)time / 500.0)), radius * 0.88F, color, 48);

      for (int i = 0; i < 8; i++) {
         float a = (float)((Math.PI * 2) * (double)i / 8.0);
         nativeLine(
            buffer,
            m,
            (float)Math.cos((double)a) * radius,
            0.04F,
            (float)Math.sin((double)a) * radius,
            (float)Math.cos((double)a) * radius,
            height - 0.04F,
            (float)Math.sin((double)a) * radius,
            color
         );
      }

      matrices.method_22909();
   }

   private static void renderNativeMarker(
      class_4588 buffer, class_4587 matrices, double x, double y, double z, float radius, float height, int color, long time
   ) {
      matrices.method_22903();
      matrices.method_22904(x, y, z);
      matrices.method_22907(class_7833.field_40718.rotationDegrees((float)(time / 18L % 360L)));
      Matrix4f m = matrices.method_23760().method_23761();
      float mid = height * 0.58F;
      float r = radius * 0.65F;
      nativeCircle(buffer, m, mid, r, color, 4);
      nativeLine(buffer, m, 0.0F, mid, -r, 0.0F, height, 0.0F, color);
      nativeLine(buffer, m, r, mid, 0.0F, 0.0F, height, 0.0F, color);
      nativeLine(buffer, m, 0.0F, mid, r, 0.0F, height, 0.0F, color);
      nativeLine(buffer, m, -r, mid, 0.0F, 0.0F, height, 0.0F, color);
      nativeLine(buffer, m, -r, mid, 0.0F, r, mid, 0.0F, color);
      matrices.method_22909();
   }

   private static void renderNativeSword(class_4588 buffer, class_4587 matrices, double x, double y, double z, float radius, float height, int color, long time) {
      matrices.method_22903();
      matrices.method_22904(x, y + (double)height * 0.5, z);
      matrices.method_22907(class_7833.field_40718.rotationDegrees((float)(time / 22L % 360L)));
      Matrix4f m = matrices.method_23760().method_23761();
      float len = Math.max(0.7F, height * 0.72F);
      float w = radius * 0.42F;
      nativeLine(buffer, m, 0.0F, len * 0.5F, 0.0F, 0.0F, -len * 0.28F, 0.0F, color);
      nativeLine(buffer, m, 0.0F, len * 0.5F, 0.0F, -w, len * 0.28F, 0.0F, color);
      nativeLine(buffer, m, 0.0F, len * 0.5F, 0.0F, w, len * 0.28F, 0.0F, color);
      nativeLine(buffer, m, -w, len * 0.28F, 0.0F, w, len * 0.28F, 0.0F, color);
      nativeLine(buffer, m, -w * 0.55F, len * 0.28F, 0.0F, w * 0.55F, -len * 0.28F, 0.0F, color);
      nativeLine(buffer, m, w * 0.55F, len * 0.28F, 0.0F, -w * 0.55F, -len * 0.28F, 0.0F, color);
      nativeLine(buffer, m, -w * 0.55F, -len * 0.28F, 0.0F, w * 0.55F, -len * 0.28F, 0.0F, color);
      nativeLine(buffer, m, 0.0F, -len * 0.28F, -w * 0.5F, 0.0F, -len * 0.5F, w * 0.5F, color);
      nativeLine(buffer, m, 0.0F, -len * 0.28F, w * 0.5F, 0.0F, -len * 0.5F, -w * 0.5F, color);
      matrices.method_22909();
   }

   private static void renderNativeSkull(class_4588 buffer, class_4587 matrices, double x, double y, double z, float radius, float height, int color, long time) {
      matrices.method_22903();
      matrices.method_22904(x, y + (double)height * 0.5, z);
      matrices.method_22907(class_7833.field_40718.rotationDegrees((float)(time / 30L % 360L)));
      Matrix4f m = matrices.method_23760().method_23761();
      float w = radius * 0.72F;
      float h = Math.max(0.28F, height * 0.3F);
      float d = w * 0.7F;
      float[][] corners = new float[][]{{-w, -h, -d}, {w, -h, -d}, {w, h, -d}, {-w, h, -d}, {-w, -h, d}, {w, -h, d}, {w, h, d}, {-w, h, d}};
      int[][] edges = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

      for (int[] edge : edges) {
         float[] a = corners[edge[0]];
         float[] b = corners[edge[1]];
         nativeLine(buffer, m, a[0], a[1], a[2], b[0], b[1], b[2], color);
      }

      nativeLine(buffer, m, -w * 0.55F, -h, -d, -w * 0.55F, -h * 1.45F, d * 0.25F, color);
      nativeLine(buffer, m, w * 0.55F, -h, -d, w * 0.55F, -h * 1.45F, d * 0.25F, color);
      matrices.method_22909();
   }

   private static void renderNativeAura(class_4588 buffer, class_4587 matrices, double x, double y, double z, float radius, float height, int color, long time) {
      for (int i = 0; i < 8; i++) {
         double a = (double)time / 850.0 + (double)i * (Math.PI / 4);
         double r = (double)radius * (0.72 + 0.18 * Math.sin((double)time / 600.0 + (double)i));
         double py = y + (double)height * (0.12 + 0.76 * ((double)(i % 4) / 3.0)) + Math.sin((double)time / 420.0 + (double)i) * 0.12;
         matrices.method_22903();
         matrices.method_22904(x + Math.cos(a) * r, py, z + Math.sin(a) * r);
         matrices.method_22907(class_7833.field_40718.rotationDegrees((float)(time / 12L % 360L + (long)i * 27L)));
         Matrix4f m = matrices.method_23760().method_23761();
         drawCubeOutlineTESP(buffer, m, Math.max(0.045F, radius * 0.13F), color);
         matrices.method_22909();
      }
   }

   public static class_2960 visualTexture(String name) {
      return class_2960.method_60655("client", "textures/visuals/" + name);
   }

   private static double animSeconds(long now, double speed) {
      return (double)(now % 3600000L) / 1000.0 * speed;
   }

   public static void drawBillboardTESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double x,
      double y,
      double z,
      float sx,
      float sy,
      float rotZ,
      class_2960 texture,
      int color,
      int alpha
   ) {
      if (alpha > 0 && !(sx <= 0.0F) && !(sy <= 0.0F)) {
         matrices.method_22903();
         matrices.method_22904(x, y, z);
         matrices.method_22907(camera.method_23767());
         if (rotZ != 0.0F) {
            matrices.method_22907(new Quaternionf().rotationZ(rotZ));
         }

         matrices.method_22905(sx, sy, 1.0F);
         Matrix4f m = matrices.method_23760().method_23761();
         drawGradientQuad(immediate.method_73477(ROMB_ESP.apply(texture)), m, color, color, color, color, alpha);
         matrices.method_22909();
      }
   }

   public static void renderChainsESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now,
      float speed
   ) {
      if (alphaPC <= 0.001F) return;
      double t = animSeconds(now, speed);
      class_2960 texture = visualTexture("chain.png");
      int links = 36;
      float linkH = 0.42F;
      int alpha = (int)(255.0F * alphaPC);
      double centerY = ty + (double)height * 0.5;
      float maxTilt = 45.0F;
      float currentTilt = (float)Math.sin(t * 1.8) * maxTilt;

      for (int c = 0; c < 2; c++) {
         float tilt = c == 0 ? currentTilt : -currentTilt;
         double spin = (c == 0 ? 1.0 : -1.0) * t * 2.5;

         matrices.method_22903();
         matrices.method_22904(tx, centerY, tz);
         matrices.method_22907(class_7833.field_40718.rotationDegrees(tilt));
         matrices.method_22907(class_7833.field_40716.rotationDegrees((float)Math.toDegrees(spin)));
         Matrix4f matrix = matrices.method_23760().method_23761();

         class_4588 buffer = immediate.method_73477(CHAIN_ESP.apply(texture));
         int clr = ColorUtil.replAlpha(color, alpha);
         float r = radius * 1.15F;

         for (int i = 0; i < links; i++) {
            double a0 = (double)i * (Math.PI * 2.0 / (double)links);
            double a1 = (double)(i + 1) * (Math.PI * 2.0 / (double)links);

            float x0 = (float)(Math.cos(a0) * (double)r);
            float z0 = (float)(Math.sin(a0) * (double)r);
            float x1 = (float)(Math.cos(a1) * (double)r);
            float z1 = (float)(Math.sin(a1) * (double)r);

            float halfH = linkH * 0.5F;
            float u0 = (float)i * (10.0F / (float)links) + (float)(spin * 1.2);
            float u1 = (float)(i + 1) * (10.0F / (float)links) + (float)(spin * 1.2);

            buffer.method_22918(matrix, x0, -halfH, z0).method_39415(clr).method_22913(u0, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
            buffer.method_22918(matrix, x1, -halfH, z1).method_39415(clr).method_22913(u1, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
            buffer.method_22918(matrix, x1, halfH, z1).method_39415(clr).method_22913(u1, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
            buffer.method_22918(matrix, x0, halfH, z0).method_39415(clr).method_22913(u0, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);

            // Double sided
            buffer.method_22918(matrix, x0, halfH, z0).method_39415(clr).method_22913(u0, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);
            buffer.method_22918(matrix, x1, halfH, z1).method_39415(clr).method_22913(u1, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);
            buffer.method_22918(matrix, x1, -halfH, z1).method_39415(clr).method_22913(u1, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);
            buffer.method_22918(matrix, x0, -halfH, z0).method_39415(clr).method_22913(u0, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);
         }

         matrices.method_22909();
      }
   }

   public static void renderSoulsESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now,
      float speed
   ) {
      double t = animSeconds(now, speed);
      class_2960 texture = visualTexture("glow.png");
      int count = 6;
      int alpha = (int)(230.0F * alphaPC);

      for (int i = 0; i < count; i++) {
         double angle = t * 1.2 + (double)i * ((Math.PI * 2) / (double)count);
         double r = (double)radius * (0.85 + 0.15 * Math.sin(t + (double)i));
         double px = tx + Math.cos(angle) * r;
         double pz = tz + Math.sin(angle) * r;
         double py = ty + (double)height * 0.55 + Math.sin(t * 1.5 + (double)i) * (double)height * 0.25;
         float pulse = 0.22F + 0.05F * (float)Math.sin(t * 3.0 + (double)i);
         drawBillboardTESP(immediate, matrices, camera, px, py, pz, pulse * 2.4F, pulse * 2.4F, 0.0F, texture, color, (int)((float)alpha * 0.35F));
         drawBillboardTESP(immediate, matrices, camera, px, py, pz, pulse, pulse, 0.0F, texture, color, alpha);

         for (int k = 1; k <= 2; k++) {
            double a2 = angle - (double)k * 0.28;
            float fade = 1.0F - (float)k * 0.32F;
            drawBillboardTESP(
               immediate,
               matrices,
               camera,
               tx + Math.cos(a2) * r,
               py,
               tz + Math.sin(a2) * r,
               pulse * fade,
               pulse * fade,
               0.0F,
               texture,
               color,
               (int)((float)alpha * fade * 0.8F)
            );
         }
      }
   }

   public static void renderCelestialESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now,
      float speed
   ) {
      double t = animSeconds(now, speed);
      class_2960 texture = visualTexture("glow.png");
      int orbit = 10;
      double rr = (double)radius * 1.1;
      double yBase = ty + (double)height * 0.5;
      int alpha = (int)(210.0F * alphaPC);

      for (int i = 0; i < orbit; i++) {
         double angle = t + (double)i * ((Math.PI * 2) / (double)orbit);
         double px = tx + Math.cos(angle) * rr;
         double pz = tz + Math.sin(angle) * rr;
         double py = yBase + Math.sin(angle * 2.0 + t) * 0.18;
         float size = 0.18F + 0.04F * (float)Math.sin(t * 2.0 + (double)i);
         drawBillboardTESP(immediate, matrices, camera, px, py, pz, size, size, 0.0F, texture, color, alpha);
      }

      float star = 0.5F + 0.05F * (float)Math.sin(t * 1.7);
      drawBillboardTESP(immediate, matrices, camera, tx, yBase, tz, star, star, (float)(t * 0.7), texture, color, alpha);
      drawBillboardTESP(immediate, matrices, camera, tx, yBase, tz, star * 1.7F, star * 1.7F, 0.0F, texture, color, (int)((float)alpha * 0.3F));
   }

   public static void renderCrystalsESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now,
      float speed
   ) {
      double t = animSeconds(now, speed);
      int count = 5;
      int alpha = (int)(235.0F * alphaPC);

      for (int i = 0; i < count; i++) {
         double angle = t * 0.8 + (double)i * ((Math.PI * 2) / (double)count);
         double px = tx + Math.cos(angle) * (double)radius;
         double pz = tz + Math.sin(angle) * (double)radius;
         double py = ty + (double)height * 0.5 + Math.sin(t * 1.3 + (double)i) * (double)height * 0.3;
         class_2960 texture = visualTexture("diamond_" + (i % 4 + 1) + ".png");
         float spin = (float)(t * 1.6 + (double)i);
         float sx = 0.28F * (0.35F + 0.65F * Math.abs((float)Math.cos((double)spin)));
         float sy = 0.42F;
         drawBillboardTESP(immediate, matrices, camera, px, py, pz, sx, sy, 0.0F, texture, color, alpha);
      }
   }

   public static void renderPentagramESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now
   ) {
      if (alphaPC <= 0.001F || radius <= 0.01F) return;
      double t = animSeconds(now, 1.0);
      class_2960 c1 = visualTexture("circles1.png");
      class_2960 c2 = visualTexture("circles2.png");
      double groundY = ty + 0.02;
      float rot1 = (float)(t * 90.0);
      float rot2 = (float)(-t * 90.0);
      float scale1 = radius * 0.95F;
      float scale2 = radius * 1.15F;
      int a = (int)(255.0F * alphaPC);

      drawGroundDiscTESP(immediate, matrices, tx, groundY, tz, scale1, rot1, c1, color, a);
      drawGroundDiscTESP(immediate, matrices, tx, groundY, tz, scale2, rot2, c2, color, (int)((float)a * 0.9F));
   }

   private static void drawGroundDiscTESP(
      class_4598 immediate,
      class_4587 matrices,
      double x,
      double y,
      double z,
      float scale,
      float rotZ,
      class_2960 texture,
      int color,
      int alpha
   ) {
      if (alpha <= 0 || scale <= 0.0F) return;
      matrices.method_22903();
      matrices.method_22904(x, y, z);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(rotZ));
      matrices.method_22905(scale, scale, scale);
      Matrix4f m = matrices.method_23760().method_23761();
      class_4588 buffer = immediate.method_73477(ROMB_ESP.apply(texture));
      int c = ColorUtil.replAlpha(color, alpha);

      // Top face
      buffer.method_22918(m, -1.0F, 1.0F, 0.0F).method_39415(c).method_22913(0.0F, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(m, 1.0F, 1.0F, 0.0F).method_39415(c).method_22913(1.0F, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(m, 1.0F, -1.0F, 0.0F).method_39415(c).method_22913(1.0F, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(m, -1.0F, -1.0F, 0.0F).method_39415(c).method_22913(0.0F, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);

      // Bottom face
      buffer.method_22918(m, -1.0F, -1.0F, 0.0F).method_39415(c).method_22913(0.0F, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);
      buffer.method_22918(m, 1.0F, -1.0F, 0.0F).method_39415(c).method_22913(1.0F, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);
      buffer.method_22918(m, 1.0F, 1.0F, 0.0F).method_39415(c).method_22913(1.0F, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);
      buffer.method_22918(m, -1.0F, 1.0F, 0.0F).method_39415(c).method_22913(0.0F, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, -1.0F);

      matrices.method_22909();
   }

   public static void renderAtomESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now
   ) {
      if (alphaPC <= 0.001F) return;
      double t = animSeconds(now, 1.0);
      class_2960 glow = visualTexture("glow.png");
      double centerY = ty + (double)height * 0.5;
      int alpha = (int)(255.0F * alphaPC);
      float thick = 0.025F;
      int segments = 48;
      int ringCount = 3;

      for (int ring = 0; ring < ringCount; ring++) {
         float ringOffset = (float)ring * 60.0F;
         double ringSpin = t * 70.0 + (double)ring * 120.0;

         matrices.method_22903();
         matrices.method_22904(tx, centerY, tz);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(ringOffset));
         matrices.method_22907(class_7833.field_40716.rotationDegrees((float)ringSpin));
         Matrix4f matrix = matrices.method_23760().method_23761();

         class_4588 fillBuf = immediate.method_73477(RING_FILL_LAYER);
         int c = ColorUtil.replAlpha(color, (int)((float)alpha * 0.75F));

         for (int i = 0; i < segments; i++) {
            double a0 = (double)i * (Math.PI * 2.0 / (double)segments);
            double a1 = (double)(i + 1) * (Math.PI * 2.0 / (double)segments);

            float rIn = radius - thick;
            float rOut = radius + thick;

            float x0In = (float)(Math.cos(a0) * (double)rIn);
            float z0In = (float)(Math.sin(a0) * (double)rIn);
            float x1In = (float)(Math.cos(a1) * (double)rIn);
            float z1In = (float)(Math.sin(a1) * (double)rIn);

            float x0Out = (float)(Math.cos(a0) * (double)rOut);
            float z0Out = (float)(Math.sin(a0) * (double)rOut);
            float x1Out = (float)(Math.cos(a1) * (double)rOut);
            float z1Out = (float)(Math.sin(a1) * (double)rOut);

            fillBuf.method_22918(matrix, x0In, 0.0F, z0In).method_39415(c);
            fillBuf.method_22918(matrix, x1In, 0.0F, z1In).method_39415(c);
            fillBuf.method_22918(matrix, x1Out, 0.0F, z1Out).method_39415(c);
            fillBuf.method_22918(matrix, x0Out, 0.0F, z0Out).method_39415(c);
         }

         int electrons = 4;
         for (int p = 0; p < electrons; p++) {
            double pAngle = (double)p * (Math.PI * 2.0 / (double)electrons) + t * 2.5;
            double px = Math.cos(pAngle) * (double)radius;
            double pz = Math.sin(pAngle) * (double)radius;

            drawBillboardQuad(immediate, matrices, camera, px, 0.0, pz, 0.22F, 0.22F, glow, color, alpha);
         }

         matrices.method_22909();
      }
   }

   private static void drawBillboardQuad(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double x,
      double y,
      double z,
      float sx,
      float sy,
      class_2960 texture,
      int color,
      int alpha
   ) {
      if (alpha <= 0) return;
      matrices.method_22903();
      matrices.method_22904(x, y, z);
      matrices.method_22907(camera.method_23767());
      matrices.method_22905(sx, sy, 1.0F);
      Matrix4f m = matrices.method_23760().method_23761();
      drawGradientQuad(immediate.method_73477(ROMB_ESP.apply(texture)), m, color, color, color, color, alpha);
      matrices.method_22909();
   }

   public static void renderCardsESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now
   ) {
      double t = animSeconds(now, 1.0);
      class_2960 cardTex = visualTexture("cards.png");
      int cards = 5;
      int alpha = (int)(230.0F * alphaPC);

      for (int i = 0; i < cards; i++) {
         double angle = t * 1.3 + (double)i * (Math.PI * 2.0 / (double)cards);
         double px = tx + Math.cos(angle) * (double)radius * 1.1;
         double pz = tz + Math.sin(angle) * (double)radius * 1.1;
         double py = ty + (double)height * 0.5 + Math.sin(t * 2.0 + (double)i) * 0.15;
         float rot = (float)(angle + Math.PI / 2.0);
         drawBillboardTESP(immediate, matrices, camera, px, py, pz, 0.35F, 0.5F, rot, cardTex, color, alpha);
      }
   }

   public static void renderSwordsESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float radius,
      float height,
      int color,
      float alphaPC,
      long now
   ) {
      if (alphaPC <= 0.001F) return;
      class_2960 swordTex = visualTexture("sword.png");
      int swords = 8;
      int alpha = (int)(255.0F * alphaPC);
      double centerY = ty + (double)height * 0.45;
      double speedVal = 2200.0;
      double orbitAngle = ((double)(now % (long)speedVal) / speedVal) * (Math.PI * 2.0);

      for (int i = 0; i < swords; i++) {
         double swordAngle = orbitAngle + (double)i * (Math.PI * 2.0 / (double)swords);
         double bob = Math.sin((double)now * 0.0035 + (double)i * (Math.PI / 4.0)) * 0.12;
         double px = tx + Math.cos(swordAngle) * (double)radius;
         double pz = tz + Math.sin(swordAngle) * (double)radius;
         double py = centerY + bob;

         matrices.method_22903();
         matrices.method_22904(px, py, pz);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
         Matrix4f matrix = matrices.method_23760().method_23761();
         class_4588 consumer = immediate.method_73477(ROMB_ESP.apply(swordTex));
         int c = ColorUtil.replAlpha(color, alpha);
         float sw = 0.18F;
         float sh = 0.36F;
         consumer.method_22918(matrix, -sw, -sh, 0.0F).method_39415(c).method_22913(0.0F, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
         consumer.method_22918(matrix, sw, -sh, 0.0F).method_39415(c).method_22913(1.0F, 1.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
         consumer.method_22918(matrix, sw, sh, 0.0F).method_39415(c).method_22913(1.0F, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
         consumer.method_22918(matrix, -sw, sh, 0.0F).method_39415(c).method_22913(0.0F, 0.0F).method_22922(class_4608.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
         matrices.method_22909();
      }
   }

   public static void renderDiamondESP(
      class_4598 immediate,
      class_4587 matrices,
      class_4184 camera,
      double tx,
      double ty,
      double tz,
      float height,
      int color,
      float alphaPC,
      long now,
      float speed,
      float size,
      int styleIndex
   ) {
      double t = animSeconds(now, speed);
      String[] textures = new String[]{"diamond_1.png", "diamond_2.png", "diamond_3.png", "diamond_4.png"};
      class_2960 texture = visualTexture(textures[Math.floorMod(styleIndex, textures.length)]);
      class_2960 glow = visualTexture("glow.png");
      double py = ty + (double)height * 0.55 + Math.sin(t * 1.2) * 0.08;
      float spin = (float)(t * 1.8);
      float sx = size * (0.3F + 0.7F * Math.abs((float)Math.cos((double)spin)));
      int alpha = (int)(235.0F * alphaPC);
      drawBillboardTESP(immediate, matrices, camera, tx, py, tz, size * 1.7F, size * 1.7F, 0.0F, glow, color, (int)((float)alpha * 0.3F));
      drawBillboardTESP(immediate, matrices, camera, tx, py, tz, sx, size, 0.0F, texture, color, alpha);
   }
}
