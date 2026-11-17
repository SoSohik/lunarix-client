package ru.mentions.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.mentions.utils.render.ScreenBlur;

@Environment(EnvType.CLIENT)
public class SquarePlayerScreen extends class_437 {
   private final SquarePlayerPanel playerPanel = new SquarePlayerPanel();
   private final float panelWidth;
   private final float panelHeight;

   public SquarePlayerScreen(float panelWidth, float panelHeight) {
      super(class_2561.method_43470("Media Player"));
      this.panelWidth = panelWidth;
      this.panelHeight = panelHeight;
   }

   public SquarePlayerScreen() {
      this(510.0F, 408.0F);
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      ScreenBlur.capture();
      float x = ((float)this.field_22789 - this.panelWidth) / 2.0F;
      float y = ((float)this.field_22790 - this.panelHeight) / 2.0F;
      float size = Math.min(this.panelWidth, this.panelHeight);
      this.playerPanel.render(context, x, y, size, (float)mouseX, (float)mouseY, delta);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      return this.playerPanel.mouseClicked(click.comp_4798(), click.comp_4799(), click.method_74245()) ? true : super.method_25402(click, doubled);
   }

   public boolean method_25406(class_11909 click) {
      return this.playerPanel.mouseReleased(click.comp_4798(), click.comp_4799(), click.method_74245()) ? true : super.method_25406(click);
   }

   public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
      return this.playerPanel.mouseDragged(click.comp_4798(), click.comp_4799(), click.method_74245(), deltaX, deltaY)
         ? true
         : super.method_25403(click, deltaX, deltaY);
   }

   public boolean method_25421() {
      return false;
   }
}
