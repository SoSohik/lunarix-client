package ru.mentions.mixin;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_408.class})
public class ChatScreenMixin {
   @Shadow
   protected class_342 field_2382;
   @Unique
   private List<String> mentions$suggestions = Collections.emptyList();
   @Unique
   private int mentions$selected = 0;
   @Unique
   private int mentions$boxX;
   @Unique
   private int mentions$boxY;
   @Unique
   private int mentions$boxW;
   @Unique
   private int mentions$boxCount;
   @Unique
   private static final int MENTIONS_LINE_H = 12;

   @Inject(
      method = {"method_44056"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void interceptMessage(String message, boolean addToHistory, CallbackInfo ci) {
      if (!message.isEmpty()) {
         char prefix = Client.get().commandManager().getPrefix();
         if (message.charAt(0) == prefix) {
            ci.cancel();
            Client.get().commandManager().handleMessage(message);
         }
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("TAIL")}
   )
   private void mentions$renderSuggestions(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         this.mentions$boxCount = 0;
         String text = this.field_2382.method_1882();
         char prefix = Client.get().commandManager().getPrefix();
         if (!text.isEmpty() && text.charAt(0) == prefix) {
            this.mentions$suggestions = Client.get().commandManager().getSuggestions(text);
            if (!this.mentions$suggestions.isEmpty()) {
               if (this.mentions$selected >= this.mentions$suggestions.size()) {
                  this.mentions$selected = 0;
               }

               class_327 tr = class_310.method_1551().field_1772;
               int count = Math.min(this.mentions$suggestions.size(), 10);
               int width = 0;

               for (int i = 0; i < count; i++) {
                  width = Math.max(width, tr.method_1727(this.mentions$suggestions.get(i)));
               }

               width += 6;
               int boxH = count * 12;
               int x = this.field_2382.method_46426() - 2;
               int y = this.field_2382.method_46427() - boxH - 1;
               this.mentions$boxX = x;
               this.mentions$boxY = y;
               this.mentions$boxW = width;
               this.mentions$boxCount = count;
               context.method_25294(x, y, x + width, y + boxH, -436207616);
               context.method_25294(x, y, x + width, y + 1, 1090519039);

               for (int i = 0; i < count; i++) {
                  int ly = y + i * 12;
                  boolean sel = i == this.mentions$selected;
                  if (sel) {
                     context.method_25294(x, ly, x + width, ly + 12, 1442840575);
                  }

                  context.method_51433(tr, this.mentions$suggestions.get(i), x + 3, ly + 2, sel ? -171 : -4473925, false);
               }
            }
         } else {
            this.mentions$suggestions = Collections.emptyList();
         }
      }
   }

   @Inject(
      method = {"method_25404"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentions$keyPressed(class_11908 input, CallbackInfoReturnable<Boolean> cir) {
      if (!this.mentions$suggestions.isEmpty()) {
         int n = this.mentions$suggestions.size();
         switch (input.method_74228()) {
            case 258:
               this.mentions$apply(this.mentions$suggestions.get(Math.min(this.mentions$selected, n - 1)));
               cir.setReturnValue(true);
               break;
            case 264:
               this.mentions$selected = (this.mentions$selected + 1) % n;
               cir.setReturnValue(true);
               break;
            case 265:
               this.mentions$selected = (this.mentions$selected - 1 + n) % n;
               cir.setReturnValue(true);
         }
      }
   }

   @Inject(
      method = {"method_25402"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mentions$mouseClicked(class_11909 click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
      int button = click.method_74245();
      double mouseX = click.comp_4798();
      double mouseY = click.comp_4799();
      if (!this.mentions$suggestions.isEmpty() && this.mentions$boxCount != 0 && button == 0) {
         int idx;
         if (mouseX >= (double)this.mentions$boxX
            && mouseX <= (double)(this.mentions$boxX + this.mentions$boxW)
            && mouseY >= (double)this.mentions$boxY
            && mouseY <= (double)(this.mentions$boxY + this.mentions$boxCount * 12)
            && (idx = (int)((mouseY - (double)this.mentions$boxY) / 12.0)) >= 0
            && idx < this.mentions$boxCount
            && idx < this.mentions$suggestions.size()) {
            this.mentions$apply(this.mentions$suggestions.get(idx));
            cir.setReturnValue(true);
         }
      }
   }

   @Unique
   private void mentions$apply(String suggestion) {
      this.field_2382.method_1852(suggestion);
      this.field_2382.method_1872(false);
      this.mentions$selected = 0;
   }
}
