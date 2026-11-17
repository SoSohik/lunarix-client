package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1306;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.DelimiterSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.screen.ClickGuiScreen;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Hands",
   desc = "Положение и размер рук от первого лица",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Hands extends Module {
   private boolean editorActive;
   private boolean restoreSwingAnimation;
   private boolean restoreGlassHands;
   public ButtonSetting openEditor = new ButtonSetting(this, "Редактор рук", () -> {
      class_437 patt0$temp = mc.field_1755;
      if (patt0$temp instanceof ClickGuiScreen) {
         ((ClickGuiScreen)patt0$temp).openHandsEditor();
      }
   });
   public DelimiterSetting rightTitle = new DelimiterSetting(this, "Правая рука");
   public SliderSetting rx = new SliderSetting(this, "Правая X", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting ry = new SliderSetting(this, "Правая Y", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting rz = new SliderSetting(this, "Правая Z", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting rScale = new SliderSetting(this, "Правая размер", 1.0F, 0.1F, 3.0F, 0.05F);
   public ButtonSetting resetRightButton = new ButtonSetting(this, "Сбросить правую", this::resetRight);
   public DelimiterSetting leftTitle = new DelimiterSetting(this, "Левая рука");
   public SliderSetting lx = new SliderSetting(this, "Левая X", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting ly = new SliderSetting(this, "Левая Y", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting lz = new SliderSetting(this, "Левая Z", 0.0F, -2.0F, 2.0F, 0.05F);
   public SliderSetting lScale = new SliderSetting(this, "Левая размер", 1.0F, 0.1F, 3.0F, 0.05F);
   public ButtonSetting resetLeftButton = new ButtonSetting(this, "Сбросить левую", this::resetLeft);
   public ButtonSetting resetAllButton = new ButtonSetting(this, "Сбросить всё", this::resetAll);

   public static Hands get() {
      return Instance.get(Hands.class);
   }

   @Override
   protected void onEnable() {
      SwingAnimation swingAnimation = SwingAnimation.get();
      if (this.restoreSwingAnimation && swingAnimation != null && !swingAnimation.isEnabled()) {
         swingAnimation.setEnabled(true, false);
      }

      GlassHands glassHands = GlassHands.getInstance();
      if (this.restoreGlassHands && glassHands != null && !glassHands.isEnabled()) {
         glassHands.setEnabled(true, false);
      }
   }

   @Override
   protected void onDisable() {
      SwingAnimation swingAnimation = SwingAnimation.get();
      boolean bl = this.restoreSwingAnimation = swingAnimation != null && swingAnimation.isEnabled();
      if (swingAnimation != null && swingAnimation.isEnabled()) {
         swingAnimation.setEnabled(false, false);
      }

      GlassHands glassHands;
      boolean bl2 = this.restoreGlassHands = (glassHands = GlassHands.getInstance()) != null && glassHands.isEnabled();
      if (glassHands != null && glassHands.isEnabled()) {
         glassHands.setEnabled(false, false);
      }
   }

   public void applyHandTranslation(class_4587 matrices, class_1306 arm) {
      if (this.shouldApplyTransforms()) {
         float x = arm == class_1306.field_6183 ? this.rx.getValue() : this.lx.getValue();
         float y = arm == class_1306.field_6183 ? this.ry.getValue() : this.ly.getValue();
         float z = arm == class_1306.field_6183 ? this.rz.getValue() : this.lz.getValue();
         matrices.method_46416(x, y, z);
      }
   }

   public void applyHandScale(class_4587 matrices, class_1306 arm) {
      if (this.shouldApplyTransforms()) {
         float s = arm == class_1306.field_6183 ? this.rScale.getValue() : this.lScale.getValue();
         matrices.method_22905(s, s, s);
      }
   }

   public void applyEmptyHandAlignment(class_4587 matrices, class_1306 arm) {
      if (this.shouldApplyTransforms()) {
         matrices.method_46416(arm == class_1306.field_6183 ? -0.08F : 0.08F, 0.08F, 0.0F);
      }
   }

   public boolean shouldApplyTransforms() {
      return this.isEnabled() || this.editorActive;
   }

   public boolean isEditorActive() {
      return this.editorActive;
   }

   public void setEditorActive(boolean editorActive) {
      this.editorActive = editorActive;
   }

   public void resetRight() {
      this.rx.set(0.0F);
      this.ry.set(0.0F);
      this.rz.set(0.0F);
      this.rScale.set(1.0F);
   }

   public void resetLeft() {
      this.lx.set(0.0F);
      this.ly.set(0.0F);
      this.lz.set(0.0F);
      this.lScale.set(1.0F);
   }

   public void resetAll() {
      this.resetRight();
      this.resetLeft();
   }
}
