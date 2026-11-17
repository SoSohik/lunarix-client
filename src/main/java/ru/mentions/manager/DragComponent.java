package ru.mentions.manager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.MousePressEvent;
import ru.mentions.manager.event_impl.MouseReleaseEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Component;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.utils.taskript.Script;

@Environment(EnvType.CLIENT)
public class DragComponent extends Component implements IMinecraft {
   private DragSetting selected;
   private final Vector2f offset = new Vector2f();
   private final List<Module> modules = new CopyOnWriteArrayList<>();
   private final Script script = new Script();
   private static final float GRID = 8.0F;
   private static final float SNAP = 5.0F;
   private final List<Float> guidesV = new CopyOnWriteArrayList<>();
   private final List<Float> guidesH = new CopyOnWriteArrayList<>();
   private final Animation gridAnim = new Animation();
   private final Animation guideVAlpha = new Animation();
   private final Animation guideHAlpha = new Animation();
   private final Animation guideVPos = new Animation();
   private final Animation guideHPos = new Animation();

   public void post(Matrix3x2f matrices) {
      this.script.update();
      int screenWidth = mc.method_22683().method_4489() / 2;
      int screenHeight = mc.method_22683().method_4506() / 2;
      boolean shouldRender = mc.field_1755 instanceof class_408;
      if (!shouldRender) {
         this.selected = null;
      }

      this.initModules();
      this.handleAnimation();
      boolean dragging = shouldRender && this.selected != null && this.selected.active;
      this.gridAnim.update();
      this.gridAnim.run(dragging ? 1.0 : 0.0, 0.25, Easings.SINE_OUT, true);
      if (this.gridAnim.get() > 0.01F) {
         this.drawGrid(screenWidth, screenHeight, this.gridAnim.get());
      }

      this.guidesV.clear();
      this.guidesH.clear();
      boolean freeMove = class_3675.method_15987(mc.method_22683(), 342) || class_3675.method_15987(mc.method_22683(), 346);
      if (this.selected != null && this.selected.active) {
         Vector2f mouse = this.cursorHud();
         float posX = mouse.x + this.offset.x;
         float posY = mouse.y + this.offset.y;
         if (!this.selected.lockX) {
            this.selected.targetPosition.x = posX;
         }

         this.selected.targetPosition.y = posY;
         if (!freeMove) {
            this.applySnapping();
         }

         this.handleDrags(screenWidth, screenHeight);
      }

      if (this.gridAnim.get() > 0.01F) {
         String hint = "ALT - свободное перемещение";
         Fonts.sf_bold
            .draw(
               hint,
               (float)screenWidth / 2.0F - Fonts.sf_bold.getWidth(hint, 15.0F) / 2.0F,
               (float)screenHeight / 2.0F + 120.0F,
               15.0F,
               ColorUtil.getColor(255, 0.3F * this.gridAnim.get())
            );
      }

      this.updateGuideAnimations();
      this.drawGuides(screenWidth, screenHeight, this.gridAnim.get());
   }

   private void updateGuideAnimations() {
      boolean hasV = !this.guidesV.isEmpty();
      boolean hasH = !this.guidesH.isEmpty();
      this.guideVAlpha.update();
      this.guideHAlpha.update();
      this.guideVPos.update();
      this.guideHPos.update();
      if (hasV) {
         float target = this.guidesV.get(0);
         if (this.guideVAlpha.get() <= 0.01F) {
            this.guideVPos.set((double)target);
         } else {
            this.guideVPos.run((double)target, 0.15, Easings.BACK_OUT, true);
         }
      }

      if (hasH) {
         float target = this.guidesH.get(0);
         if (this.guideHAlpha.get() <= 0.01F) {
            this.guideHPos.set((double)target);
         } else {
            this.guideHPos.run((double)target, 0.15, Easings.BACK_OUT, true);
         }
      }

      this.guideVAlpha.run(hasV ? 1.0 : 0.0, 0.2, Easings.SINE_OUT, true);
      this.guideHAlpha.run(hasH ? 1.0 : 0.0, 0.2, Easings.SINE_OUT, true);
   }

   private void drawGrid(int width, int height, float alpha) {
      int color = ColorUtil.getColor(255, 255, 255, (int)(16.0F * alpha));
      int colorMajor = ColorUtil.getColor(255, 255, 255, (int)(34.0F * alpha));
      int i = 0;

      for (float x = 0.0F; x <= (float)width; i++) {
         RenderUtil.Render2D.rect(x, 0.0F, 0.5F, (float)height, i % 5 == 0 ? colorMajor : color);
         x += 8.0F;
      }

      i = 0;

      for (float y = 0.0F; y <= (float)height; i++) {
         RenderUtil.Render2D.rect(0.0F, y, (float)width, 0.5F, i % 5 == 0 ? colorMajor : color);
         y += 8.0F;
      }

      int center = ColorUtil.getColor(255, 120, 170, (int)(70.0F * alpha));
      RenderUtil.Render2D.rect((float)width / 2.0F, 0.0F, 0.6F, (float)height, center);
      RenderUtil.Render2D.rect(0.0F, (float)height / 2.0F, (float)width, 0.6F, center);
   }

   private void drawGuides(int width, int height, float alpha) {
      if (!(alpha <= 0.01F)) {
         float aV = this.guideVAlpha.get() * alpha;
         float aH = this.guideHAlpha.get() * alpha;
         if (aV > 0.01F) {
            int color = ColorUtil.getColor(255, 120, 200, (int)(200.0F * aV));
            RenderUtil.Render2D.rect(this.guideVPos.get() - 0.3F, 0.0F, 0.6F, (float)height, color);
         }

         if (aH > 0.01F) {
            int color = ColorUtil.getColor(255, 120, 200, (int)(200.0F * aH));
            RenderUtil.Render2D.rect(0.0F, this.guideHPos.get() - 0.3F, (float)width, 0.6F, color);
         }
      }
   }

   private void applySnapping() {
      float sx = this.selected.targetPosition.x;
      float sy = this.selected.targetPosition.y;
      float sw = this.selected.size.x;
      float sh = this.selected.size.y;
      float[] sXs = new float[]{sx, sx + sw / 2.0F, sx + sw};
      float[] sYs = new float[]{sy, sy + sh / 2.0F, sy + sh};
      Float bestX = null;
      float bestXDist = 5.0F;
      float guideX = 0.0F;
      Float bestY = null;
      float bestYDist = 5.0F;
      float guideY = 0.0F;

      for (Module module : this.modules) {
         for (Setting<?> setting : module.getSettings()) {
            DragSetting o;
            if (setting instanceof DragSetting && (o = (DragSetting)setting) != this.selected && o.active && !(o.size.x <= 0.0F) && !(o.size.y <= 0.0F)) {
               float[] oXs = new float[]{o.position.x, o.position.x + o.size.x / 2.0F, o.position.x + o.size.x};
               float[] oYs = new float[]{o.position.y, o.position.y + o.size.y / 2.0F, o.position.y + o.size.y};

               for (float oe : oXs) {
                  for (float se : sXs) {
                     float d = Math.abs(se - oe);
                     if (d < bestXDist) {
                        bestXDist = d;
                        bestX = sx + (oe - se);
                        guideX = oe;
                     }
                  }
               }

               for (float oe : oYs) {
                  for (float sex : sYs) {
                     float d = Math.abs(sex - oe);
                     if (d < bestYDist) {
                        bestYDist = d;
                        bestY = sy + (oe - sex);
                        guideY = oe;
                     }
                  }
               }
            }
         }
      }

      if (!this.selected.lockX) {
         if (bestX != null) {
            this.selected.targetPosition.x = bestX;
            this.guidesV.add(guideX);
         } else {
            this.selected.targetPosition.x = (float)Math.round(sx / 8.0F) * 8.0F;
         }
      }

      if (bestY != null) {
         this.selected.targetPosition.y = bestY;
         this.guidesH.add(guideY);
      } else {
         this.selected.targetPosition.y = (float)Math.round(sy / 8.0F) * 8.0F;
      }
   }

   private void handleDrags(int width, int height) {
      for (Module module : this.modules) {
         for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof DragSetting) {
               DragSetting drag = (DragSetting)setting;
               if (drag.active) {
                  drag.position.x = (float)drag.animationX.getValue();
                  drag.position.y = (float)drag.animationY.getValue();
                  float maxX = Math.max(0.0F, (float)width - drag.size.x);
                  float maxY = Math.max(0.0F, (float)height - drag.size.y);
                  drag.position.x = Math.max(0.0F, Math.min(maxX, drag.position.x));
                  drag.position.y = Math.max(0.0F, Math.min(maxY, drag.position.y));
                  drag.targetPosition.x = Math.max(0.0F, Math.min(maxX, drag.targetPosition.x));
                  drag.targetPosition.y = Math.max(0.0F, Math.min(maxY, drag.targetPosition.y));
               }
            }
         }
      }
   }

   private void handleAnimation() {
      this.modules.forEach(m -> m.getSettings().forEach(s -> {
            if (s instanceof DragSetting drag) {
               drag.animationX.update();
               drag.animationY.update();
               if (this.script.isFinished()) {
                  drag.animationX.run((double)drag.targetPosition.x, 0.06, true);
                  drag.animationY.run((double)drag.targetPosition.y, 0.06, true);
               }
            }
         }));
   }

   private void initModules() {
      this.modules.clear();
      Client.get()
         .moduleManager()
         .values()
         .stream()
         .filter(m -> m.isEnabled() && m.getSettings().stream().anyMatch(s -> s instanceof DragSetting))
         .forEach(this.modules::add);
   }

   @EventHandler
   public void onEvent(MousePressEvent event) {
      if (event.getKey() == 0) {
         if (event.getScreen() instanceof class_408) {
            for (Module module : this.modules) {
               for (Setting<?> setting : module.getSettings()) {
                  if (setting instanceof DragSetting) {
                     DragSetting dragSetting = (DragSetting)setting;
                     if (dragSetting.active) {
                        Vector2f position = dragSetting.position;
                        Vector2f scale = dragSetting.size;
                        Vector2f mouse = this.getMouse((double)((float)event.getMouseX()), (double)((float)event.getMouseY()));
                        if (!dragSetting.structure
                           && !dragSetting.blocksDragAt(mouse.x, mouse.y)
                           && isHover((double)mouse.x, (double)mouse.y, (double)position.x, (double)position.y, (double)scale.x, (double)scale.y)) {
                           this.selected = dragSetting;
                           this.offset.set((double)position.x - (double)mouse.x, (double)position.y - (double)mouse.y);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onEvent(MouseReleaseEvent event) {
      if (this.selected != null) {
         this.selected.targetPosition.set(this.selected.position);
         this.selected = null;
      }
   }

   public static boolean isHover(double mouseX, double mouseY, double x, double y, double width, double height) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   public Vector2f getMouse(double mouseX, double mouseY) {
      return new Vector2f((float)(mouseX * (double)mc.method_22683().method_4495() / 2.0), (float)(mouseY * (double)mc.method_22683().method_4495() / 2.0));
   }

   private Vector2f cursorHud() {
      class_1041 window = mc.method_22683();
      double x = mc.field_1729.method_1603() * (double)window.method_4486() / (double)window.method_4480();
      double y = mc.field_1729.method_1604() * (double)window.method_4502() / (double)window.method_4507();
      return this.getMouse(x, y);
   }

   @Generated
   public DragSetting getSelected() {
      return this.selected;
   }

   @Generated
   public Vector2f getOffset() {
      return this.offset;
   }

   @Generated
   public List<Module> getModules() {
      return this.modules;
   }

   @Generated
   public Script getScript() {
      return this.script;
   }

   @Generated
   public List<Float> getGuidesV() {
      return this.guidesV;
   }

   @Generated
   public List<Float> getGuidesH() {
      return this.guidesH;
   }

   @Generated
   public Animation getGridAnim() {
      return this.gridAnim;
   }

   @Generated
   public Animation getGuideVAlpha() {
      return this.guideVAlpha;
   }

   @Generated
   public Animation getGuideHAlpha() {
      return this.guideHAlpha;
   }

   @Generated
   public Animation getGuideVPos() {
      return this.guideVPos;
   }

   @Generated
   public Animation getGuideHPos() {
      return this.guideHPos;
   }
}
