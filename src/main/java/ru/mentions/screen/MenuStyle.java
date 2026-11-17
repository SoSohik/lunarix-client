package ru.mentions.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.FrameTextures;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScreenBlur;

@Environment(EnvType.CLIENT)
public final class MenuStyle {
   private MenuStyle() {
   }

   public static boolean isMenuScreen(class_437 screen) {
      return screen instanceof class_500 || screen instanceof class_526 || screen instanceof class_429;
   }

   private static float scaleFix() {
      class_310 mc = IMinecraft.mc;
      if (mc != null && mc.method_22683() != null) {
         float currentScale = (float)mc.method_22683().method_4495();
         return currentScale <= 0.0F ? 1.0F : 2.0F / currentScale;
      } else {
         return 1.0F;
      }
   }

   public static int frameWidth() {
      class_310 mc = IMinecraft.mc;
      return mc == null ? 1 : (int)((float)mc.method_22683().method_4486() / scaleFix());
   }

   public static int frameHeight() {
      class_310 mc = IMinecraft.mc;
      return mc == null ? 1 : (int)((float)mc.method_22683().method_4502() / scaleFix());
   }

   public static void renderBackground(int mouseX, int mouseY) {
      int fw = frameWidth();
      int fh = frameHeight();
      if (fw > 0 && fh > 0) {
         float fix = scaleFix();
         Render2D.beginOverlay();
         FrameTextures.ensure();
         MainMenuScreen.renderMenuBg(fw, fh, 1.0F, (float)mouseX / fix, (float)mouseY / fix);
         ScreenBlur.capture(1);
         RenderUtil.Blur.blur(0.0F, 0.0F, (float)fw, (float)fh, 1.0F, ColorUtil.getColor(0, 0.1F));
         RenderUtil.Render2D.rect(0.0F, 0.0F, (float)fw, (float)fh, ColorUtil.getColor(0, 0.1F));
         Render2D.endOverlay();
      }
   }
}
