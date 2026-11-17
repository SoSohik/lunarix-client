package ru.mentions.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1044;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_9801;

@Environment(EnvType.CLIENT)
public final class WinterSnowRenderer {
    private static final class_2960 SNOW_VS = class_2960.method_60655("client", "core/winter_snow");
    private static final class_2960 SNOW_FS = class_2960.method_60655("client", "core/winter_snow");
    private static final RenderPipeline SNOW = RenderPipelineHelper.invokeMethod67887(
            RenderPipeline.builder(new Snippet[]{RenderPipelineHelper.getField60125()})
                    .withLocation(class_2960.method_60655("client", "pipeline/winter_snow"))
                    .withVertexShader(SNOW_VS)
                    .withFragmentShader(SNOW_FS)
                    .withVertexFormat(class_290.field_1575, class_5596.field_27382)
                    .withSampler("Sampler0")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build()
    );
    private static final RenderPipeline SNOW_COVER = RenderPipelineHelper.invokeMethod67887(
            RenderPipeline.builder(new Snippet[]{RenderPipelineHelper.getField60125()})
                    .withLocation(class_2960.method_60655("client", "pipeline/winter_snow_cover"))
                    .withVertexShader(SNOW_VS)
                    .withFragmentShader(SNOW_FS)
                    .withVertexFormat(class_290.field_1575, class_5596.field_27382)
                    .withSampler("Sampler0")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                    .withDepthWrite(true)
                    .withCull(false)
                    .build()
    );

    private WinterSnowRenderer() { }

    public static void draw(class_287 buffer, int quads, class_2960 textureId) {
        draw(buffer, quads, textureId, SNOW, "Render-Winter-Textured-Snow");
    }

    public static void drawCover(class_287 buffer, int quads, class_2960 textureId) {
        draw(buffer, quads, textureId, SNOW_COVER, "Render-Winter-Snow-Cover");
    }

    private static void draw(class_287 buffer, int quads, class_2960 textureId,
                             RenderPipeline pipeline, String name) {
        if (buffer == null || quads <= 0 || textureId == null) return;
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null || mc.field_1687 == null) return;
        class_1044 texture = mc.method_1531().method_4619(textureId);
        class_9801 mesh;
        try {
            mesh = buffer.method_60794();
        } catch (Throwable t) {
            return;
        }
        if (mesh == null) return;
        try {
            com.mojang.blaze3d.buffers.GpuBuffer vertices = RenderSystem.getDevice().createBuffer(
                    () -> name, 32, mesh.method_60818());
            try {
                class_276 target = mc.method_1522();
                if (target == null || target.method_71639() == null || target.method_71640() == null) return;
                RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> name,
                        target.method_71639(), OptionalInt.empty(),
                        target.method_71640(), OptionalDouble.empty());
                try {
                    pass.setPipeline(pipeline);
                    RenderSystem.bindDefaultUniforms(pass);
                    pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                    pass.bindTexture("Sampler0", texture.method_71659(), texture.method_75484());
                    pass.setVertexBuffer(0, vertices);
                    com.mojang.blaze3d.systems.RenderSystem.class_5590 indices =
                            RenderSystem.getSequentialBuffer(class_5596.field_27382);
                    pass.setIndexBuffer(indices.method_68274(quads * 6), indices.method_31924());
                    pass.drawIndexed(quads * 6, 1, 0, 0);
                } finally {
                    pass.close();
                }
            } finally {
                vertices.close();
            }
        } catch (Throwable ignored) {
        } finally {
            mesh.close();
        }
    }
}
