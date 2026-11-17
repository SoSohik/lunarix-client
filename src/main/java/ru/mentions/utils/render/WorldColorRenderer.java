package ru.mentions.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_12137;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class WorldColorRenderer {
    private static final WorldColorRenderer INSTANCE = new WorldColorRenderer();
    private static final class_2960 PIPELINE_ID = class_2960.method_60655("client", "pipeline/atmosphere_world_color");
    private static final class_2960 VERTEX_SHADER = class_2960.method_60655("client", "core/world_color");
    private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("client", "core/world_color");
    private static final RenderPipeline PIPELINE = RenderPipelineHelper.invokeMethod67887(
            RenderPipeline.builder(new Snippet[]{RenderPipelineHelper.getField60125()})
                    .withLocation(PIPELINE_ID)
                    .withVertexShader(VERTEX_SHADER)
                    .withFragmentShader(FRAGMENT_SHADER)
                    .withVertexFormat(class_290.field_60033, class_5596.field_27379)
                    .withUniform("WorldColorUniforms", class_10789.field_60031)
                    .withSampler("SceneSampler")
                    .withSampler("DepthTexture")
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build()
    );
    private static final int BUFFER_SIZE = 48;

    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private GpuTexture sceneTexture;
    private GpuTextureView sceneTextureView;
    private ByteBuffer dataBuffer;
    private class_12137 sampler;
    private int lastWidth;
    private int lastHeight;
    private boolean initialized;
    private long lastError;

    private WorldColorRenderer() {
    }

    public static WorldColorRenderer getInstance() {
        return INSTANCE;
    }

    private void ensureInitialized() {
        if (!this.initialized) {
            this.dataBuffer = MemoryUtil.memAlloc(BUFFER_SIZE);
            ByteBuffer dummy = MemoryUtil.memAlloc(4);
            dummy.putInt(0);
            dummy.flip();
            this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "client:atmosphere_dummy_vertex", 32, dummy);
            MemoryUtil.memFree(dummy);
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "client:atmosphere_uniform", 136, BUFFER_SIZE);
            this.sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            this.initialized = true;
        }
    }

    private void ensureSceneTexture(int width, int height) {
        if (this.sceneTexture == null || width != this.lastWidth || height != this.lastHeight) {
            if (this.sceneTextureView != null) {
                this.sceneTextureView.close();
                this.sceneTextureView = null;
            }
            if (this.sceneTexture != null) {
                this.sceneTexture.close();
                this.sceneTexture = null;
            }
            this.sceneTexture = RenderSystem.getDevice().createTexture(() -> "client:atmosphere_scene", 5, TextureFormat.RGBA8, width, height, 1, 1);
            this.sceneTextureView = RenderSystem.getDevice().createTextureView(this.sceneTexture);
            this.lastWidth = width;
            this.lastHeight = height;
        }
    }

    public void render(int color, float saturation, float brightness,
                       float strength, float contrast, float snowMask,
                       float hazeStrength, float hazeDistance) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1687 == null || mc.field_1724 == null
                || RenderSystem.tryGetDevice() == null) return;
        class_276 target = mc.method_1522();
        if (target == null || target.method_30277() == null
                || target.method_71639() == null || target.method_71640() == null
                || target.field_1482 <= 1 || target.field_1481 <= 1) return;
        int width = target.field_1482;
        int height = target.field_1481;
        this.ensureInitialized();
        this.ensureSceneTexture(width, height);
        if (this.sceneTextureView == null) return;
        try {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(target.method_30277(), this.sceneTexture, 0, 0, 0, 0, 0, width, height);

            float r = ((color >> 16) & 255) / 255.0f;
            float g = ((color >> 8) & 255) / 255.0f;
            float b = (color & 255) / 255.0f;
            this.dataBuffer.clear();
            this.dataBuffer.putFloat(r).putFloat(g).putFloat(b).putFloat(1.0f);
            this.dataBuffer.putFloat(clamp(saturation, 0.0f, 2.0f))
                    .putFloat(clamp(brightness, 0.1f, 2.0f))
                    .putFloat(clamp(strength, 0.0f, 1.0f))
                    .putFloat(clamp(contrast, 0.5f, 1.5f));
            this.dataBuffer.putFloat(clamp(snowMask, 0.0f, 1.0f))
                    .putFloat(clamp(hazeStrength, 0.0f, 1.0f))
                    .putFloat(clamp(hazeDistance, 1.0f, 256.0f))
                    .putFloat(0.0f);
            this.dataBuffer.flip();
            encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);

            RenderPass renderPass = encoder.createRenderPass(
                    () -> "client:atmosphere_world_color",
                    target.method_71639(), OptionalInt.empty(),
                    target.method_71640(), OptionalDouble.empty()
            );
            try {
                renderPass.setPipeline(PIPELINE);
                renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
                renderPass.bindTexture("SceneSampler", this.sceneTextureView, this.sampler);
                renderPass.bindTexture("DepthTexture", target.method_71640(), this.sampler);
                renderPass.setUniform("WorldColorUniforms", this.uniformBuffer);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.draw(0, 6);
            } finally {
                renderPass.close();
            }
        } catch (Throwable e) {
            long now = System.currentTimeMillis();
            if (now - this.lastError > 5000) {
                this.lastError = now;
                System.err.println("[Atmosphere] world color grading failed: " + e);
            }
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void close() {
        if (this.sceneTextureView != null) {
            this.sceneTextureView.close();
            this.sceneTextureView = null;
        }
        if (this.sceneTexture != null) {
            this.sceneTexture.close();
            this.sceneTexture = null;
        }
        if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
            this.uniformBuffer = null;
        }
        if (this.dummyVertexBuffer != null) {
            this.dummyVertexBuffer.close();
            this.dummyVertexBuffer = null;
        }
        if (this.dataBuffer != null) {
            MemoryUtil.memFree(this.dataBuffer);
            this.dataBuffer = null;
        }
        this.sampler = null;
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.initialized = false;
    }
}
