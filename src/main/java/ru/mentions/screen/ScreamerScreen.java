package ru.mentions.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class ScreamerScreen extends class_437 implements IMinecraft {
   private static final class_2960 IMAGE = class_2960.method_60655("client", "textures/screamer.png");
   private static final long DURATION_NANOS = 2000000000L;
   private final class_437 parent;
   private final long startNanos = System.nanoTime();
   private boolean closed = false;

   public ScreamerScreen(class_437 parent) {
      super(class_2561.method_43470("screamer"));
      this.parent = parent;
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25422() {
      return false;
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (DualWindowService.isCleanPass()) {
         if (System.nanoTime() - this.startNanos >= DURATION_NANOS) {
            if (!this.closed) {
               this.closed = true;
               mc.method_1507(this.parent);
            }

            return;
         }

         float scaleFix = 2.0F / (float)mc.method_22683().method_4495();
         float w = (float)mc.method_22683().method_4486() / scaleFix;
         float h = (float)mc.method_22683().method_4502() / scaleFix;
         Render2D.beginOverlay();
         RenderUtil.Images.texture(IMAGE, 0.0F, 0.0F, w, h, ColorUtil.getColor(255));
         Render2D.endOverlay();
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      return true;
   }

   public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
      return true;
   }

   public boolean method_25406(class_11909 click) {
      return true;
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      return true;
   }

   public boolean method_25404(class_11908 input) {
      return true;
   }

   public boolean method_25400(class_11905 input) {
      return true;
   }
}
