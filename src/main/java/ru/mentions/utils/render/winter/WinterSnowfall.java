package ru.mentions.utils.render.winter;

import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2464;
import net.minecraft.class_2488;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_4184;
import net.minecraft.class_4588;
import net.minecraft.class_638;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.mentions.utils.render.WinterSnowRenderer;

@Environment(EnvType.CLIENT)
public final class WinterSnowfall {
    private static final class_2960 SOFT_FLAKE = class_2960.method_60655("client", "pictures/particles/snow_soft.png");
    private static final class_2960 DETAILED_FLAKE = class_2960.method_60655("client", "pictures/particles/snowflake.png");
    private static final int MAX_FLAKES = 4200;
    private static final class_2960 SNOW_COVER = class_2960.method_60655("client", "pictures/particles/snow_cover.png");
    private static final int MAX_GROUND_QUADS = 60000;
    private static final float GRID_REFRESH_SECONDS = 3.0f;
    private static final int GRID_MARGIN = 3;
    private static final int MAX_COLUMN_SCAN = 48;
    private static final int FLOATS_PER_QUAD = 24;
    private static final float EDGE_EPS = 0.006f;
    private static final float LIP_DEPTH = 0.035f;
    private static final float SIDE_V = 0.18f;

    public static final class Config {
        public float radius = 32.0f;
        public float height = 24.0f;
        public float density = 1.2f;
        public float flakeSize = 1.0f;
        public float fallSpeed = 1.0f;
        public float wind = 1.0f;
        public float opacity = 1.0f;
        public float blizzard = 0.0f;
        public boolean skyOnly = true;
        public boolean groundSnow = true;
        public float groundRadius = 40.0f;
        public float groundDepth = 0.09f;
        public int color = 0xFFEAF4FF;
    }

    private static final WinterSnowfall INSTANCE = new WinterSnowfall();

    public static WinterSnowfall getInstance() {
        return INSTANCE;
    }

    private final List<Flake> flakes = new ArrayList<>(1024);
    private final Random random = new Random();
    private final class_2338.class_2339 scratchPos = new class_2338.class_2339();
    private final Matrix4f sprite = new Matrix4f();
    private final Quaternionf inverseCamera = new Quaternionf();
    private final Vector3f scratchVelocity = new Vector3f();

    private float[] surfaceBase = new float[0];
    private boolean[] surfaceFull = new boolean[0];
    private float[] surfaceLight = new float[0];
    private int[] surfaceY = new int[0];
    private class_265[] surfaceShapes = new class_265[0];
    private float[] mesh = new float[FLOATS_PER_QUAD * 4096];
    private int meshQuads;
    private float builtDepth = -1.0f;
    private int resolvedY;
    private class_265 currentShape;
    private int currentWorldX;
    private int currentWorldZ;
    private final float[] segStart = new float[16];
    private final float[] segEnd = new float[16];
    private final float[] segStartNext = new float[16];
    private final float[] segEndNext = new float[16];
    private class_265 resolvedShape;
    private int gridRadius = -1;
    private int gridOriginX = Integer.MIN_VALUE;
    private int gridOriginY = Integer.MIN_VALUE;
    private int gridOriginZ = Integer.MIN_VALUE;
    private float gridAge = Float.MAX_VALUE;
    private long lastFrameNanos;
    private float clock;

    private WinterSnowfall() {
    }

    public void reset() {
        this.flakes.clear();
        this.lastFrameNanos = 0L;
        this.clock = 0.0f;
        this.gridRadius = -1;
        this.gridOriginX = Integer.MIN_VALUE;
        this.gridOriginY = Integer.MIN_VALUE;
        this.gridOriginZ = Integer.MIN_VALUE;
        this.gridAge = Float.MAX_VALUE;
        this.meshQuads = 0;
        this.surfaceShapes = new class_265[0];
        this.surfaceBase = new float[0];
    }

    public void render(class_4184 camera, Config config) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1687 == null || mc.field_1724 == null || camera == null) {
            reset();
            return;
        }

        long now = System.nanoTime();
        if (this.lastFrameNanos == 0L) this.lastFrameNanos = now;
        float dt = class_3532.method_15363((float) ((now - this.lastFrameNanos) / 1.0E9d), 0.0f, 0.05f);
        this.lastFrameNanos = now;
        this.clock += dt;

        class_243 cam = camera.method_71156();
        float ambient = ambientLight(mc, cam);
        if (config.groundSnow) {
            updateGrid(mc.field_1687, cam, config, dt);
            drawGround(camera, cam, config, ambient);
        }
        updateFlakes(mc.field_1687, cam, config, dt);
        drawFlakes(camera, cam, config, ambient);
    }

    private int targetFlakes(Config config) {
        float area = (float) Math.PI * config.radius * config.radius;
        int count = Math.round(area * 0.055f * config.density * (1.0f + config.blizzard * 0.55f));
        return class_3532.method_15340(count, 60, MAX_FLAKES);
    }

    private void updateFlakes(class_638 level, class_243 cam, Config config, float dt) {
        int target = targetFlakes(config);
        float cull = (config.radius + 5.0f) * (config.radius + 5.0f);
        for (int i = this.flakes.size() - 1; i >= 0; i--) {
            Flake flake = this.flakes.get(i);
            double dx = flake.x - cam.field_1352;
            double dz = flake.z - cam.field_1350;
            if (dx * dx + dz * dz > cull || flake.y > cam.field_1351 + config.height * 1.5) {
                this.flakes.remove(i);
            }
        }
        while (this.flakes.size() > target) this.flakes.remove(this.flakes.size() - 1);

        boolean coldStart = this.flakes.size() < target / 3;
        int budget = Math.min(target - this.flakes.size(), coldStart ? MAX_FLAKES : 240);
        for (int i = 0; i < budget; i++) {
            Flake flake = spawn(level, cam, config, !coldStart);
            if (flake != null) this.flakes.add(flake);
        }

        float windStrength = config.wind * (1.6f + config.blizzard * 5.2f);
        double windAngle = this.clock * 0.045;
        double gust = 0.72 + 0.28 * Math.sin(this.clock * 0.37)
                + 0.14 * Math.sin(this.clock * 1.13 + 1.7);
        double windX = Math.cos(windAngle) * windStrength * gust;
        double windZ = Math.sin(windAngle) * windStrength * gust;
        float flutter = 1.0f - config.blizzard * 0.55f;

        for (int i = 0; i < this.flakes.size(); i++) {
            Flake flake = this.flakes.get(i);
            double swayX = Math.sin(this.clock * flake.swayFrequency + flake.phase)
                    * flake.swayAmplitude * flutter;
            double swayZ = Math.cos(this.clock * flake.swayFrequency * 0.83 + flake.phase * 1.37)
                    * flake.swayAmplitude * flutter;
            double follow = Math.min(1.0, dt * (1.1 + flake.drag * 2.4));
            flake.vx += ((windX * flake.drag + swayX) - flake.vx) * follow;
            flake.vz += ((windZ * flake.drag + swayZ) - flake.vz) * follow;
            flake.vy += (flake.terminal - flake.vy) * Math.min(1.0, dt * 2.2);
            flake.x += flake.vx * dt;
            flake.y += flake.vy * dt;
            flake.z += flake.vz * dt;
            flake.rotation += flake.spin * dt;

            if (flake.y <= flake.groundY + 0.04 || flake.y < cam.field_1351 - config.height * 0.8) {
                Flake replacement = spawn(level, cam, config, true);
                if (replacement != null) this.flakes.set(i, replacement);
                else flake.y = cam.field_1351 + config.height * (0.6 + this.random.nextDouble() * 0.4);
            }
        }
    }

    private Flake spawn(class_638 level, class_243 cam, Config config, boolean fromTop) {
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double distance = Math.sqrt(this.random.nextDouble()) * config.radius;
        double x = cam.field_1352 + Math.cos(angle) * distance;
        double z = cam.field_1350 + Math.sin(angle) * distance;
        double ground = flakeGround(level, class_3532.method_15357(x), class_3532.method_15357(z));
        if (config.skyOnly && ground > cam.field_1351 + config.height * 0.9) return null;

        double y = fromTop
                ? cam.field_1351 + config.height * (0.75 + this.random.nextDouble() * 0.45)
                : cam.field_1351 - config.height * 0.5 + this.random.nextDouble() * config.height * 1.6;
        if (y <= ground + 0.2) y = ground + 0.2 + this.random.nextDouble() * 2.0;

        Flake flake = new Flake();
        flake.x = x;
        flake.y = y;
        flake.z = z;
        flake.groundY = ground;
        float roll = this.random.nextFloat();
        float base;
        if (roll < 0.62f) base = random(0.035f, 0.075f);
        else if (roll < 0.92f) base = random(0.075f, 0.145f);
        else base = random(0.145f, 0.30f);
        flake.size = base * config.flakeSize;
        flake.detailed = roll >= 0.92f && this.random.nextFloat() < 0.45f;
        float weight = class_3532.method_15363(base / 0.30f, 0.12f, 1.0f);
        flake.drag = 0.35f + (1.0f - weight) * 0.65f;
        flake.terminal = -(0.85 + weight * 3.1) * config.fallSpeed
                * (1.0 + config.blizzard * 1.35);
        flake.swayFrequency = random(0.6f, 1.9f);
        flake.swayAmplitude = random(0.35f, 1.05f) * (1.25f - weight * 0.6f);
        flake.phase = random(0.0f, 6.2831855f);
        flake.spin = random(-1.6f, 1.6f);
        flake.rotation = random(0.0f, 6.2831855f);
        flake.brightness = random(0.78f, 1.0f);
        flake.vy = flake.terminal * 0.6;
        return flake;
    }

    private void drawFlakes(class_4184 camera, class_243 cam, Config config, float ambient) {
        if (this.flakes.isEmpty()) return;
        Matrix4f view = viewPose(camera);
        Quaternionf rotation = camera.method_23767();
        rotation.invert(this.inverseCamera);
        float light = class_3532.method_15363(0.42f + ambient * 0.58f, 0.32f, 1.0f);
        float red = ((config.color >> 16) & 0xFF) / 255.0f * light;
        float green = ((config.color >> 8) & 0xFF) / 255.0f * light;
        float blue = (config.color & 0xFF) / 255.0f * light;
        float fadeStart = config.radius * 0.55f;
        float streakScale = 0.045f + config.blizzard * 0.075f;
        drawFlakePass(camera, cam, config, view, rotation, false, SOFT_FLAKE,
                red, green, blue, fadeStart, streakScale);
        drawFlakePass(camera, cam, config, view, rotation, true, DETAILED_FLAKE,
                red, green, blue, fadeStart, streakScale);
    }

    private void drawFlakePass(class_4184 camera, class_243 cam, Config config, Matrix4f view,
                               Quaternionf rotation, boolean detailedPass, class_2960 texture,
                               float red, float green, float blue, float fadeStart, float streakScale) {
        int capacity = 0;
        for (Flake flake : this.flakes) if (flake.detailed == detailedPass) capacity++;
        if (capacity == 0) return;
        int vertexSize = class_290.field_1575.getVertexSize();
        try (class_9799 memory = new class_9799(vertexSize * 4 * capacity)) {
            class_287 buffer = new class_287(memory, class_5596.field_27382, class_290.field_1575);
            int quads = 0;
            for (Flake flake : this.flakes) {
                if (flake.detailed != detailedPass) continue;
                if (appendFlake(buffer, view, rotation, flake, cam, config,
                        red, green, blue, fadeStart, streakScale)) quads++;
            }
            if (quads > 0) WinterSnowRenderer.draw(buffer, quads, texture);
        } catch (Throwable ignored) {
        }
    }

    private boolean appendFlake(class_287 buffer, Matrix4f view, Quaternionf rotation,
                                Flake flake, class_243 cam, Config config, float red, float green,
                                float blue, float fadeStart, float streakScale) {
        double dx = flake.x - cam.field_1352;
        double dy = flake.y - cam.field_1351;
        double dz = flake.z - cam.field_1350;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        double limit = config.radius + 4.0;
        if (distanceSq > limit * limit) return false;
        double distance = Math.sqrt(distanceSq);
        float fade = distance <= fadeStart ? 1.0f : 1.0f - class_3532.method_15363(
                (float) ((distance - fadeStart) / (config.radius - fadeStart + 1.0)), 0.0f, 1.0f);
        if (fade <= 0.02f) return false;
        if (distance < 0.35) fade *= (float) (distance / 0.35);
        float alpha = class_3532.method_15363(config.opacity * flake.brightness * fade * 0.92f, 0.0f, 1.0f);
        if (alpha <= 0.008f) return false;

        double speed = Math.sqrt(flake.vx * flake.vx + flake.vy * flake.vy + flake.vz * flake.vz);
        float stretch = 1.0f + class_3532.method_15363((float) (speed * streakScale), 0.0f, 2.6f);
        float tilt = flake.rotation;
        if (stretch > 1.05f && speed > 1.0E-4) {
            this.scratchVelocity.set((float) flake.vx, (float) flake.vy, (float) flake.vz);
            this.scratchVelocity.rotate(this.inverseCamera);
            tilt = (float) Math.atan2(this.scratchVelocity.x, this.scratchVelocity.y);
        }

        Matrix4f matrix = this.sprite.set(view).translate((float) flake.x, (float) flake.y, (float) flake.z)
                .rotate(rotation).rotateZ(tilt);
        float halfWidth = flake.size * 0.5f;
        float halfHeight = flake.size * stretch * 0.5f;
        buffer.method_22918(matrix, -halfWidth, -halfHeight, 0.0f).method_22913(0.0f, 1.0f)
                .method_22915(red, green, blue, alpha);
        buffer.method_22918(matrix, halfWidth, -halfHeight, 0.0f).method_22913(1.0f, 1.0f)
                .method_22915(red, green, blue, alpha);
        buffer.method_22918(matrix, halfWidth, halfHeight, 0.0f).method_22913(1.0f, 0.0f)
                .method_22915(red, green, blue, alpha);
        buffer.method_22918(matrix, -halfWidth, halfHeight, 0.0f).method_22913(0.0f, 0.0f)
                .method_22915(red, green, blue, alpha);
        return true;
    }

    private void updateGrid(class_638 level, class_243 cam, Config config, float dt) {
        int radius = class_3532.method_15340((int) Math.ceil(config.groundRadius), 4, 48) + GRID_MARGIN;
        int camX = class_3532.method_15357(cam.field_1352);
        int camZ = class_3532.method_15357(cam.field_1350);
        this.gridAge += dt;
        boolean moved = Math.abs(camX - this.gridOriginX) >= GRID_MARGIN
                || Math.abs(camZ - this.gridOriginZ) >= GRID_MARGIN
                || Math.abs(class_3532.method_15357(cam.field_1351) - this.gridOriginY) > 24;
        boolean resized = radius != this.gridRadius || config.groundDepth != this.builtDepth;
        if (!moved && !resized && this.gridAge < GRID_REFRESH_SECONDS) return;

        int side = radius * 2 + 1;
        int cells = side * side;
        if (this.surfaceBase.length != cells) {
            this.surfaceBase = new float[cells];
            this.surfaceFull = new boolean[cells];
            this.surfaceLight = new float[cells];
            this.surfaceY = new int[cells];
            this.surfaceShapes = new class_265[cells];
        }
        this.gridRadius = radius;
        this.gridOriginX = camX;
        this.gridOriginZ = camZ;
        this.gridOriginY = class_3532.method_15357(cam.field_1351);
        this.gridAge = 0.0f;
        this.builtDepth = config.groundDepth;

        for (int gz = 0; gz < side; gz++) {
            int worldZ = camZ - radius + gz;
            for (int gx = 0; gx < side; gx++) {
                int worldX = camX - radius + gx;
                int index = gz * side + gx;
                this.surfaceBase[index] = Float.NaN;
                this.surfaceFull[index] = false;
                this.surfaceShapes[index] = null;
                if (!resolveSurface(level, worldX, worldZ)) continue;
                class_265 shape = this.resolvedShape;
                int y = this.resolvedY;
                this.surfaceY[index] = y;
                this.surfaceShapes[index] = shape;
                this.surfaceBase[index] = (float) (y + shape.method_1105(class_2350.class_2351.field_11052));
                this.surfaceFull[index] = coversWholeTop(shape);
                this.surfaceLight[index] = columnLight(level, worldX, y + 1, worldZ);
            }
        }
        buildMesh(config);
    }

    private boolean resolveSurface(class_638 level, int x, int z) {
        try {
            if (!level.method_2935().method_12123(x >> 4, z >> 4)) return false;
            int top = level.method_8624(class_2902.class_2903.field_13202, x, z);
            int minY = level.method_31607();
            if (top <= minY) return false;
            int bottom = Math.max(minY, top - MAX_COLUMN_SCAN);
            for (int y = top - 1; y >= bottom; y--) {
                this.scratchPos.method_10103(x, y, z);
                class_2680 state = level.method_8320(this.scratchPos);
                if (state.method_26215()) continue;
                if (!state.method_26227().method_15769()) return false;
                if (isInvisible(state)) continue;
                class_265 shape = state.method_26218(level, this.scratchPos);
                if (shape.method_1110()) continue;
                boolean snowLayer = state.method_26204() instanceof class_2488;
                if (!snowLayer && state.method_26220(level, this.scratchPos).method_1110()) continue;
                this.resolvedY = y;
                this.resolvedShape = shape;
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean isInvisible(class_2680 state) {
        if (state.method_27852(class_2246.field_10499) || state.method_27852(class_2246.field_31037) || state.method_27852(class_2246.field_10369)) return true;
        return state.method_26217() == class_2464.field_11455 && !state.method_31709();
    }

    private static boolean coversWholeTop(class_265 shape) {
        double top = shape.method_1105(class_2350.class_2351.field_11052);
        for (class_238 box : shape.method_1090()) {
            if (box.field_1325 >= top - 1.0E-3 && box.field_1323 <= 1.0E-3 && box.field_1321 <= 1.0E-3
                    && box.field_1320 >= 1.0 - 1.0E-3 && box.field_1324 >= 1.0 - 1.0E-3) return true;
        }
        return false;
    }

    private float columnLight(class_638 level, int x, int y, int z) {
        try {
            this.scratchPos.method_10103(x, y, z);
            float light = level.method_22339(this.scratchPos) / 15.0f;
            return 0.40f + class_3532.method_15363(light, 0.0f, 1.0f) * 0.60f;
        } catch (Throwable ignored) {
            return 1.0f;
        }
    }

    private void buildMesh(Config config) {
        this.meshQuads = 0;
        int side = this.gridRadius * 2 + 1;
        float depth = class_3532.method_15363(config.groundDepth, 0.02f, 0.4f);
        for (int gz = 0; gz < side; gz++) {
            for (int gx = 0; gx < side; gx++) {
                int index = gz * side + gx;
                class_265 shape = this.surfaceShapes[index];
                if (shape == null) continue;
                int worldX = this.gridOriginX - this.gridRadius + gx;
                int worldZ = this.gridOriginZ - this.gridRadius + gz;
                int blockY = this.surfaceY[index];
                float light = this.surfaceLight[index];
                this.currentShape = shape;
                this.currentWorldX = worldX;
                this.currentWorldZ = worldZ;
                for (class_238 box : shape.method_1090()) {
                    double w = box.field_1320 - box.field_1323;
                    double d = box.field_1324 - box.field_1321;
                    if (w < 0.06 || d < 0.06) continue;
                    appendBox(gx, gz, worldX, blockY, worldZ, box, light, depth);
                }
            }
        }
    }

    private void appendBox(int gx, int gz, int worldX, int blockY, int worldZ, class_238 box,
                           float light, float depth) {
        float top = (float) (blockY + box.field_1325);
        float area = (float) ((box.field_1320 - box.field_1323) * (box.field_1324 - box.field_1321));
        float thicknessScale = area >= 0.9f ? 1.0f : class_3532.method_15363(0.45f + area * 0.6f, 0.45f, 1.0f);
        boolean west = box.field_1323 <= 1.0E-3;
        boolean east = box.field_1320 >= 1.0 - 1.0E-3;
        boolean north = box.field_1321 <= 1.0E-3;
        boolean south = box.field_1324 >= 1.0 - 1.0E-3;

        double wx0 = worldX + box.field_1323;
        double wx1 = worldX + box.field_1320;
        double wz0 = worldZ + box.field_1321;
        double wz1 = worldZ + box.field_1324;

        int ao00 = west && north ? cornerOcclusion(gx, gz, -1, -1, top) : 0;
        int ao01 = west && south ? cornerOcclusion(gx, gz, -1, 1, top) : 0;
        int ao11 = east && south ? cornerOcclusion(gx, gz, 1, 1, top) : 0;
        int ao10 = east && north ? cornerOcclusion(gx, gz, 1, -1, top) : 0;

        float h00 = top + thickness(wx0, wz0, depth, thicknessScale, ao00);
        float h01 = top + thickness(wx0, wz1, depth, thicknessScale, ao01);
        float h11 = top + thickness(wx1, wz1, depth, thicknessScale, ao11);
        float h10 = top + thickness(wx1, wz0, depth, thicknessScale, ao10);

        float lx0 = (float) (wx0 - this.gridOriginX) - EDGE_EPS;
        float lx1 = (float) (wx1 - this.gridOriginX) + EDGE_EPS;
        float lz0 = (float) (wz0 - this.gridOriginZ) - EDGE_EPS;
        float lz1 = (float) (wz1 - this.gridOriginZ) + EDGE_EPS;
        float oy = this.gridOriginY;
        float u0 = (float) box.field_1323, u1 = (float) box.field_1320;
        float v0 = (float) box.field_1321, v1 = (float) box.field_1324;

        pushQuad(lx0, h00 - oy, lz0, u0, v0, light * occlusionShade(ao00),
                lx0, h01 - oy, lz1, u0, v1, light * occlusionShade(ao01),
                lx1, h11 - oy, lz1, u1, v1, light * occlusionShade(ao11),
                lx1, h10 - oy, lz0, u1, v0, light * occlusionShade(ao10));

        float lip = LIP_DEPTH * thicknessScale;
        float bottom = top - lip - oy;
        float sideX = light * 0.80f;
        float sideZ = light * 0.88f;
        if (!west || needsSide(gx - 1, gz, top)) {
            emitSide(box, 0, lx0, lz0, lz1, h00 - oy, h01 - oy, bottom, sideX, top - blockY);
        }
        if (!east || needsSide(gx + 1, gz, top)) {
            emitSide(box, 1, lx1, lz0, lz1, h10 - oy, h11 - oy, bottom, sideX, top - blockY);
        }
        if (!north || needsSide(gx, gz - 1, top)) {
            emitSide(box, 2, lz0, lx0, lx1, h00 - oy, h10 - oy, bottom, sideZ, top - blockY);
        }
        if (!south || needsSide(gx, gz + 1, top)) {
            emitSide(box, 3, lz1, lx0, lx1, h01 - oy, h11 - oy, bottom, sideZ, top - blockY);
        }
    }

    private void emitSide(class_238 box, int side, float plane, float a0, float a1,
                          float hA, float hB, float bottom, float shade, float localTop) {
        class_265 shape = this.currentShape;
        float originAlong = side < 2 ? (float) (this.currentWorldZ - this.gridOriginZ)
                : (float) (this.currentWorldX - this.gridOriginX);
        int segments = 1;
        float[] segStart = this.segStart;
        float[] segEnd = this.segEnd;
        segStart[0] = a0;
        segEnd[0] = a1;
        if (shape != null) {
            for (class_238 other : shape.method_1090()) {
                if (other == box) continue;
                if (other.field_1322 > localTop + 1.0E-3 || other.field_1325 <= localTop + 1.0E-3) continue;
                boolean covers = switch (side) {
                    case 0 -> other.field_1323 <= box.field_1323 + 1.0E-3 && other.field_1320 > box.field_1323 + 1.0E-3;
                    case 1 -> other.field_1320 >= box.field_1320 - 1.0E-3 && other.field_1323 < box.field_1320 - 1.0E-3;
                    case 2 -> other.field_1321 <= box.field_1321 + 1.0E-3 && other.field_1324 > box.field_1321 + 1.0E-3;
                    default -> other.field_1324 >= box.field_1324 - 1.0E-3 && other.field_1321 < box.field_1324 - 1.0E-3;
                };
                if (!covers) continue;
                float c0 = originAlong + (float) (side < 2 ? other.field_1321 : other.field_1323) - EDGE_EPS;
                float c1 = originAlong + (float) (side < 2 ? other.field_1324 : other.field_1320) + EDGE_EPS;
                int count = 0;
                float[] nextStart = this.segStartNext;
                float[] nextEnd = this.segEndNext;
                for (int i = 0; i < segments && count < nextStart.length - 1; i++) {
                    float s0 = segStart[i], s1 = segEnd[i];
                    if (c1 <= s0 || c0 >= s1) {
                        nextStart[count] = s0; nextEnd[count++] = s1;
                        continue;
                    }
                    if (c0 > s0) { nextStart[count] = s0; nextEnd[count++] = c0; }
                    if (c1 < s1) { nextStart[count] = c1; nextEnd[count++] = s1; }
                }
                System.arraycopy(nextStart, 0, segStart, 0, count);
                System.arraycopy(nextEnd, 0, segEnd, 0, count);
                segments = count;
                if (segments == 0) return;
            }
        }
        float span = Math.max(1.0E-4f, a1 - a0);
        for (int i = 0; i < segments; i++) {
            float s0 = segStart[i], s1 = segEnd[i];
            if (s1 - s0 < 0.02f) continue;
            float t0 = (s0 - a0) / span, t1 = (s1 - a0) / span;
            float y0 = class_3532.method_16439(t0, hA, hB);
            float y1 = class_3532.method_16439(t1, hA, hB);
            float u0 = s0 - class_3532.method_15375(s0), u1 = u0 + (s1 - s0);
            float low = shade * 0.92f;
            if (side < 2) {
                pushQuad(plane, bottom, s0, u0, 0.0f, low,
                        plane, y0, s0, u0, SIDE_V, shade,
                        plane, y1, s1, u1, SIDE_V, shade,
                        plane, bottom, s1, u1, 0.0f, low);
            } else {
                pushQuad(s0, bottom, plane, u0, 0.0f, low,
                        s0, y0, plane, u0, SIDE_V, shade,
                        s1, y1, plane, u1, SIDE_V, shade,
                        s1, bottom, plane, u1, 0.0f, low);
            }
        }
    }

    private boolean needsSide(int gx, int gz, float top) {
        int side = this.gridRadius * 2 + 1;
        if (gx < 0 || gz < 0 || gx >= side || gz >= side) return true;
        int index = gz * side + gx;
        float base = this.surfaceBase[index];
        if (Float.isNaN(base)) return true;
        return !(this.surfaceFull[index] && base >= top - 1.0E-3f);
    }

    private int cornerOcclusion(int gx, int gz, int dx, int dz, float top) {
        boolean a = higher(gx + dx, gz, top);
        boolean b = higher(gx, gz + dz, top);
        if (a && b) return 3;
        return (a ? 1 : 0) + (b ? 1 : 0) + (higher(gx + dx, gz + dz, top) ? 1 : 0);
    }

    private boolean higher(int gx, int gz, float top) {
        int side = this.gridRadius * 2 + 1;
        if (gx < 0 || gz < 0 || gx >= side || gz >= side) return false;
        float base = this.surfaceBase[gz * side + gx];
        return !Float.isNaN(base) && base > top + 0.3f;
    }

    private static float occlusionShade(int ao) {
        return 1.0f - ao * 0.13f;
    }

    private static float thickness(double x, double z, float depth, float scale, int ao) {
        float n = valueNoise(x * 0.55, z * 0.55) * 0.65f + valueNoise(x * 1.9 + 17.3, z * 1.9 - 4.1) * 0.35f;
        return depth * scale * (0.72f + n * 0.56f) + depth * 0.32f * ao;
    }

    private static float valueNoise(double x, double z) {
        int ix = class_3532.method_15357(x);
        int iz = class_3532.method_15357(z);
        float fx = (float) (x - ix);
        float fz = (float) (z - iz);
        fx = fx * fx * (3.0f - 2.0f * fx);
        fz = fz * fz * (3.0f - 2.0f * fz);
        float a = hash(ix, iz);
        float b = hash(ix + 1, iz);
        float c = hash(ix, iz + 1);
        float d = hash(ix + 1, iz + 1);
        return class_3532.method_16439(fz, class_3532.method_16439(fx, a, b), class_3532.method_16439(fx, c, d));
    }

    private void pushQuad(float x0, float y0, float z0, float u0, float v0, float s0,
                          float x1, float y1, float z1, float u1, float v1, float s1,
                          float x2, float y2, float z2, float u2, float v2, float s2,
                          float x3, float y3, float z3, float u3, float v3, float s3) {
        if (this.meshQuads >= MAX_GROUND_QUADS) return;
        int need = (this.meshQuads + 1) * FLOATS_PER_QUAD;
        if (this.mesh.length < need) {
            this.mesh = java.util.Arrays.copyOf(this.mesh, Math.max(need, this.mesh.length * 2));
        }
        int o = this.meshQuads * FLOATS_PER_QUAD;
        float[] m = this.mesh;
        m[o] = x0; m[o + 1] = y0; m[o + 2] = z0; m[o + 3] = u0; m[o + 4] = v0; m[o + 5] = s0;
        m[o + 6] = x1; m[o + 7] = y1; m[o + 8] = z1; m[o + 9] = u1; m[o + 10] = v1; m[o + 11] = s1;
        m[o + 12] = x2; m[o + 13] = y2; m[o + 14] = z2; m[o + 15] = u2; m[o + 16] = v2; m[o + 17] = s2;
        m[o + 18] = x3; m[o + 19] = y3; m[o + 20] = z3; m[o + 21] = u3; m[o + 22] = v3; m[o + 23] = s3;
        this.meshQuads++;
    }

    private void drawGround(class_4184 camera, class_243 cam, Config config, float ambient) {
        if (this.meshQuads <= 0) return;
        float radius = Math.min(config.groundRadius, this.gridRadius - GRID_MARGIN);
        float fadeStart = radius * 0.72f;
        float fadeRange = Math.max(1.0f, radius - fadeStart);
        float sun = class_3532.method_15363(0.55f + ambient * 0.45f, 0.4f, 1.0f);
        float cr = ((config.color >> 16) & 0xFF) / 255.0f;
        float cg = ((config.color >> 8) & 0xFF) / 255.0f;
        float cb = (config.color & 0xFF) / 255.0f;

        Matrix4f view = new Matrix4f().rotation(camera.method_23767().invert(this.inverseCamera));
        float ox = (float) (this.gridOriginX - cam.field_1352);
        float oy = (float) (this.gridOriginY - cam.field_1351);
        float oz = (float) (this.gridOriginZ - cam.field_1350);
        int vertexSize = class_290.field_1575.getVertexSize();
        try (class_9799 memory = new class_9799(vertexSize * 4 * Math.max(64, this.meshQuads))) {
            class_287 buffer = new class_287(memory, class_5596.field_27382, class_290.field_1575);
            int quads = 0;
            float[] m = this.mesh;
            for (int q = 0; q < this.meshQuads; q++) {
                int o = q * FLOATS_PER_QUAD;
                float cx = (m[o] + m[o + 12]) * 0.5f + ox;
                float cz = (m[o + 2] + m[o + 14]) * 0.5f + oz;
                float flat = (float) Math.sqrt(cx * cx + cz * cz);
                if (flat > radius) continue;
                float fade = flat <= fadeStart ? 1.0f : 1.0f - (flat - fadeStart) / fadeRange;
                float alpha = class_3532.method_15363(config.opacity * fade, 0.0f, 1.0f);
                if (alpha <= 0.02f) continue;
                for (int v = 0; v < 4; v++) {
                    int p = o + v * 6;
                    float shade = class_3532.method_15363(m[p + 5] * sun, 0.0f, 1.0f);
                    float r = cr * shade * 0.97f;
                    float g = cg * (0.03f + shade * 0.97f);
                    float b = Math.min(1.0f, cb * (0.16f + shade * 0.86f));
                    buffer.method_22918(view, m[p] + ox, m[p + 1] + oy, m[p + 2] + oz)
                            .method_22913(m[p + 3], m[p + 4]).method_22915(r, g, b, alpha);
                }
                quads++;
            }
            if (quads > 0) WinterSnowRenderer.drawCover(buffer, quads, SNOW_COVER);
        } catch (Throwable ignored) {
        }
    }

    private double flakeGround(class_638 level, int x, int z) {
        if (resolveSurface(level, x, z)) {
            return this.resolvedY + this.resolvedShape.method_1105(class_2350.class_2351.field_11052);
        }
        try {
            return level.method_8624(class_2902.class_2903.field_13202, x, z);
        } catch (Throwable ignored) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    private static Matrix4f viewPose(class_4184 camera) {
        class_243 cam = camera.method_71156();
        return new Matrix4f()
                .rotation(camera.method_23767().invert(new Quaternionf()))
                .translate((float) -cam.field_1352, (float) -cam.field_1351, (float) -cam.field_1350);
    }

    private float ambientLight(class_310 mc, class_243 cam) {
        try {
            int light = mc.field_1687.method_22339(class_2338.method_49638(cam));
            return class_3532.method_15363(light / 15.0f, 0.25f, 1.0f);
        } catch (Throwable ignored) {
            return 1.0f;
        }
    }

    private float random(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private static float hash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return ((h ^ (h >> 16)) & 0xFFFF) / 65535.0f;
    }

    private static final class Flake {
        double x, y, z;
        double vx, vy, vz;
        double terminal = -1.5;
        double groundY = Double.NEGATIVE_INFINITY;
        float size = 0.08f;
        float drag = 0.6f;
        float swayFrequency = 1.0f;
        float swayAmplitude = 0.6f;
        float phase;
        float spin;
        float rotation;
        float brightness = 1.0f;
        boolean detailed;
    }
}
