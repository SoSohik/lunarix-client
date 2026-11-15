package ru.mentions.utils.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;

public class RenderPipelineHelper {
    public static RenderPipeline invokeMethod67887(RenderPipeline pipeline) {
        try {
            Method m = class_10799.class.getDeclaredMethod("method_67887", RenderPipeline.class);
            m.setAccessible(true);
            return (RenderPipeline) m.invoke(null, pipeline);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Snippet getField56860() {
        try {
            Field f = class_10799.class.getDeclaredField("field_56860");
            f.setAccessible(true);
            return (Snippet) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Snippet getField60125() {
        try {
            Field f = class_10799.class.getDeclaredField("field_60125");
            f.setAccessible(true);
            return (Snippet) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class_1921 invokeMethod75940(String name, class_12247 layer) {
        try {
            Method m = class_1921.class.getDeclaredMethod("method_75940", String.class, class_12247.class);
            m.setAccessible(true);
            return (class_1921) m.invoke(null, name, layer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
