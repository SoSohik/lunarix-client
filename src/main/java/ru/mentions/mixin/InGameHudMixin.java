package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.Client;
import ru.mentions.manager.DragComponent;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.module.impl.render.CrossHair;
import ru.mentions.module.impl.render.NoRender;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_329.class})
public abstract class InGameHudMixin {
   @Inject(
      method = {"method_1753"},
      at = {@At("TAIL")}
   )
   public void onRender(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 != null) {
            ScreenBlur.frame();
            class_437 screen = mc.field_1755;
            if (!this.isLoadingScreen(screen)) {
               context.method_71048();
               Render2D.beginOverlay();
               context.method_51448().pushMatrix();
               EventDisplay event = new EventDisplay(context, tickCounter.method_60637(false));
               Client.get().componentManager().get(DragComponent.class).post(context.method_51448());
               event.hook();
               Client.get().render2D().flushAll();
               context.method_51448().popMatrix();
               Render2D.endOverlay();
            }
         }
      }
   }

   @Inject(
      method = {"method_1765"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderStatusEffectOverlay(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1736"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void removeVanillaCrosshair(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      try {
         CrossHair crosshairModule = CrossHair.getInstance();
         if (crosshairModule.isEnabled()) {
            ci.cancel();
         }
      } catch (Exception var5) {
      }
   }

   @Unique
   private boolean isLoadingScreen(class_437 screen) {
      if (screen == null) {
         return false;
      } else {
         String className = screen.getClass().getSimpleName().toLowerCase();
         String fullName = screen.getClass().getName().toLowerCase();
         if (className.contains("loading")) {
            return true;
         } else if (className.contains("progress")) {
            return true;
         } else if (className.contains("connecting")) {
            return true;
         } else if (className.contains("downloading")) {
            return true;
         } else if (className.contains("terrain")) {
            return true;
         } else if (className.contains("generating")) {
            return true;
         } else if (className.contains("saving")) {
            return true;
         } else if (className.contains("reload")) {
            return true;
         } else if (className.contains("resource")) {
            return true;
         } else {
            return className.contains("pack") ? true : fullName.contains("mojang");
         }
      }
   }

   @Inject(
      method = {"method_61980"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderNauseaOverlay(class_332 context, float nauseaStrength, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreZalupa.getValue()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_55803"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderScoreboard(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreScoreboard.getValue()) {
         ci.cancel();
      } else {
         InterFace interFace = InterFace.getInstance();
         if (interFace != null
            && interFace.isEnabled()
            && interFace.customScoreboard != null
            && interFace.customScoreboard.getValue()
            && interFace.element.getValue("ScoreBoard")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_70837"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderBossBar(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreBossBar.getValue()) {
         ci.cancel();
      }
   }
}
