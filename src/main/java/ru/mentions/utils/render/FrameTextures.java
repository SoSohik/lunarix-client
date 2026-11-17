package ru.mentions.utils.render;

import java.io.InputStream;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

@Environment(EnvType.CLIENT)
public final class FrameTextures {
   private static final class_2960[] FRAME = new class_2960[]{
      class_2960.method_60655("client", "textures/frame/menu.png"),
      class_2960.method_60655("client", "textures/frame/menu2.png"),
      class_2960.method_60655("client", "textures/frame/menu3.png"),
      class_2960.method_60655("client", "textures/frame/menu4.png"),
      class_2960.method_60655("client", "textures/frame/rectgui.png"),
      class_2960.method_60655("client", "textures/frame/rectthemegui.png"),
      class_2960.method_60655("client", "textures/frame/background.png"),
      class_2960.method_60655("client", "textures/tg.png")
   };
   private static boolean loaded = false;

   private FrameTextures() {
   }

   public static void ensure() {
      if (!loaded) {
         class_310 mc = class_310.method_1551();
         if (mc != null && mc.method_1478() != null) {
            for (class_2960 id : FRAME) {
               try {
                  Optional res = mc.method_1478().method_14486(id);
                  if (!res.isEmpty()) {
                     try (InputStream in = ((class_3298)res.get()).method_14482()) {
                        class_1011 image = class_1011.method_4309(in);
                        class_1043 texture = new class_1043(() -> id.toString(), image);
                        texture.method_4524();
                        mc.method_1531().method_4616(id, texture);
                     }
                  }
               } catch (Exception var11) {
               }
            }

            loaded = true;
         }
      }
   }
}
