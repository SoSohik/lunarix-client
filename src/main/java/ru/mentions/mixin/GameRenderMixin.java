package ru.mentions.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11228;
import net.minecraft.class_11246;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_4184;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import net.minecraft.class_757;
import net.minecraft.class_758;
import net.minecraft.class_7833;
import net.minecraft.class_9779;
import net.minecraft.class_758.class_4596;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.inventorypreset.InventoryPresetOverlay;
import ru.mentions.module.impl.pasted.ZoomSystem;
import ru.mentions.module.impl.render.AspectRatio;
import ru.mentions.module.impl.render.ColorGrade;
import ru.mentions.module.impl.render.NoRender;
import ru.mentions.screen.ClickGuiScreen;
import ru.mentions.screen.HandsEditor;
import ru.mentions.utils.player.Spectator;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_757.class})
public abstract class GameRenderMixin {
   @Shadow
   @Final
   private class_310 field_4015;
   @Shadow
   @Final
   private class_11228 field_59965;
   @Shadow
   @Final
   private class_758 field_60793;
   @Shadow
   @Final
   class_11246 field_59966;
   @Shadow
   @Final
   private class_4184 field_18765;

   @Inject(
      method = {"close"},
      at = {@At("RETURN")}
   )
   private void onClose(CallbackInfo ci) {
      ColorGrade.closeRenderer();
      if (Client.get() != null && Client.get().render2D() != null) {
         Client.get().render2D().close();
      }
   }

   @Inject(
      method = {"method_3188"},
      at = {@At("RETURN")}
   )
   private void gradeCompletedWorld(class_9779 tickCounter, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         ColorGrade.renderScene();
      }
   }

   @Inject(
      method = {"method_3192"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11228;method_70890(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
         shift = Shift.AFTER
      )},
      require = 0
   )
   private void afterGuiRender(class_9779 tickCounter, boolean tick, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         boolean menuWithoutWorld = (this.field_4015.field_1687 == null || this.field_4015.field_1724 == null)
            && this.field_4015.field_1755 instanceof ClickGuiScreen;
         if (menuWithoutWorld || this.field_4015.field_1687 != null && this.field_4015.field_1724 != null) {
            if (!this.isLoadingScreen(this.field_4015.field_1755)) {
               if (this.field_4015.method_18506() == null) {
                  if (this.shouldRenderOnTop(this.field_4015.field_1755)) {
                     this.field_59966.method_70926();
                     int mouseX = (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683());
                     int mouseY = (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683());
                     float tickDelta = tickCounter.method_60637(false);
                     class_332 context = new class_332(this.field_4015, this.field_59966, mouseX, mouseY);
                     class_437 class_4372 = this.field_4015.field_1755;
                     if (class_4372 instanceof ClickGuiScreen) {
                        if (menuWithoutWorld) {
                           Render2D.beginOverlay();
                        }

                        ((ClickGuiScreen)class_4372).renderOverlay(context, tickCounter);
                     }

                     if (this.field_4015.field_1755 instanceof class_490) {
                        InventoryPresetOverlay.render(context, mouseX, mouseY);
                     }

                     this.field_59965.method_70890(this.field_60793.method_71109(class_4596.field_60101));
                  }
               }
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = {"method_3188"},
      at = {@At(
         value = "INVOKE",
         target = "Ljava/lang/Math;max(FF)F",
         ordinal = 0
      )}
   )
   private float onNauseaDistortion(float original) {
      NoRender noRender = NoRender.getInstance();
      return noRender != null && noRender.isEnabled() && noRender.ignoreZalupa.getValue() ? 0.0F : original;
   }

   @Inject(
      method = {"method_3189"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShowFloatingItem(class_1799 stack, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreTotemPop.getValue()) {
         ci.cancel();
      }
   }

   @Unique
   private boolean shouldRenderOnTop(class_437 screen) {
      if (screen == null) {
         return true;
      } else if (screen instanceof ClickGuiScreen) {
         return true;
      } else {
         return screen instanceof class_408 ? true : screen instanceof class_490;
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

   @Shadow
   public abstract float method_3196(class_4184 var1, float var2, boolean var3);

   @Inject(
      method = {"method_3196"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void mentions$onGetFov(class_4184 camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
      ZoomSystem zoom = ZoomSystem.getInstance();
      if (zoom != null && zoom.isEnabled()) {
         float modified = zoom.modifyFov((Float)cir.getReturnValue(), tickDelta);
         cir.setReturnValue(modified);
      }
   }

   @Inject(
      method = {"method_3172"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void spectator$hideHand(CallbackInfo ci) {
      if (!DualWindowService.isCleanPass() && Spectator.isActive()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_3188"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4597$class_4598;method_22993()V",
         shift = Shift.AFTER
      )}
   )
   private void captureHandsForEditor(class_9779 tickCounter, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         HandsEditor editor = HandsEditor.getInstance();
         if (editor.isActive()) {
            editor.captureAfterHands();
         }
      }
   }

   @Inject(
      method = {"method_3188"},
      at = {@At(
         value = "INVOKE_STRING",
         target = "Lnet/minecraft/class_3695;method_15405(Ljava/lang/String;)V",
         args = {"ldc=hand"}
      )}
   )
   public void hookWorldRender(
      class_9779 tickCounter,
      CallbackInfo ci,
      @Local(ordinal = 0) Matrix4f projection,
      @Local(ordinal = 1) Matrix4f view,
      @Local(ordinal = 0) float tickDelta,
      @Local class_4587 matrixStack
   ) {
      if (!DualWindowService.isCleanPass()) {
         if (this.field_4015.field_1687 != null && this.field_4015.field_1724 != null) {
            class_4587 worldSpaceStack = new class_4587();
            worldSpaceStack.method_22907(class_7833.field_40714.rotationDegrees(this.field_18765.method_19329()));
            worldSpaceStack.method_22907(class_7833.field_40716.rotationDegrees(this.field_18765.method_19330() + 180.0F));
            RenderUtil.Render3D.lastProjMat.set(this.field_4015.field_1773.method_22973(this.method_3196(this.field_18765, tickDelta, true)));
            RenderUtil.Render3D.lastModMat.set(RenderSystem.getModelViewMatrix());
            RenderUtil.Render3D.lastWorldSpaceMatrix.set(worldSpaceStack.method_23760().method_23761());
         }
      }
   }

   @Inject(
      method = {"method_22973"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetBasicProjectionMatrix(float fov, CallbackInfoReturnable<Matrix4f> cir) {
      AspectRatio module = AspectRatio.getInstance();
      if (module != null && module.isEnabled()) {
         float targetAspect = module.getAspectRatio();
         if (targetAspect > 0.001F) {
            float defaultAspect = (float)this.field_4015.method_22683().method_4489() / (float)this.field_4015.method_22683().method_4506();
            Matrix4f matrix = (Matrix4f)cir.getReturnValue();
            matrix.m00(matrix.m00() * (defaultAspect / targetAspect));
         }
      }
   }
}
