package ru.mentions.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11659;
import net.minecraft.class_12122;
import net.minecraft.class_12152;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.manager.event_impl.GlassHandsRenderEvent;
import ru.mentions.manager.event_impl.HandAnimationEvent;
import ru.mentions.manager.event_impl.RenderItemEvent;
import ru.mentions.module.impl.render.GlassHands;
import ru.mentions.module.impl.render.Hands;
import ru.mentions.module.impl.render.Hands3D;
import ru.mentions.module.impl.render.NoRender;
import ru.mentions.module.impl.render.SwingAnimation;
import ru.mentions.screen.HandsEditor;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_759.class})
public abstract class HeldItemRendererMixin {
   @Shadow
   protected abstract void method_3219(class_4587 matrices, class_11659 queue, int light, float equipProgress, float swingProgress, class_1306 arm);

   @Shadow
   protected abstract void method_3224(class_4587 matrices, class_1306 arm, float equipProgress);

   @Shadow
   protected abstract void method_65816(float swingProgress, class_4587 matrices, int side, class_1306 arm);

   @Shadow
   public abstract void method_3233(class_1309 entity, class_1799 item, class_811 mode, class_4587 matrices, class_11659 queue, int light);

   @Inject(
      method = {"method_3228"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hands3d$renderFirstPerson(
      class_742 player,
      float tickProgress,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_11659 queue,
      int light,
      CallbackInfo ci
   ) {
      if (DualWindowService.isCleanPass()) {
         return;
      }

      Hands3D module = Hands3D.get();
      if (module == null || !module.isEnabled()) {
         return;
      }

      // порожня рука, використання предмета (лук, арбалет, спис, щит, підзорна труба)
      // та особливі предмети лишаються на ванільному рендері
      if (player.method_31550() || item.method_7960() || player.method_6115()) {
         return;
      }

      if (!module.bothHands.getValue() && hand == class_1268.field_5810) {
         return;
      }

      if (item.method_31574(class_1802.field_8399) || item.method_31574(class_1802.field_8102)) {
         return;
      }

      class_1306 arm = hand == class_1268.field_5808 ? player.method_6068() : player.method_6068().method_5928();
      int side = arm == class_1306.field_6183 ? 1 : -1;

      if (module.showArm.getValue()) {
         matrices.method_22903();
         this.method_3219(matrices, queue, light, equipProgress, swingProgress, arm);
         matrices.method_22909();
      }

      float s = module.scale.getValue();
      float ox = module.offsetX.getValue() * (float)side;
      float oy = module.offsetY.getValue();
      float oz = module.offsetZ.getValue();
      float rx = module.rotateX.getValue();
      float ry = module.rotateY.getValue() * (float)side;
      float rz = module.rotateZ.getValue() * (float)side;

      matrices.method_22903();
      this.method_3224(matrices, arm, equipProgress);
      HandAnimationEvent animationEvent = new HandAnimationEvent(matrices, hand, swingProgress, equipProgress, arm);
      animationEvent.hook();
      if (!animationEvent.isCancelled() && module.vanillaSwing.getValue()) {
         class_12122 swingType = item.method_75218().comp_4975();
         if (swingType == class_12122.field_63400) {
            class_12152.method_75391(swingProgress, matrices, side, arm);
         } else if (swingType != class_12122.field_63398) {
            this.method_65816(swingProgress, matrices, side, arm);
         }
      }

      matrices.method_46416(ox, oy, oz);
      if (rx != 0.0F) {
         matrices.method_22907(class_7833.field_40714.rotationDegrees(rx));
      }

      if (ry != 0.0F) {
         matrices.method_22907(class_7833.field_40716.rotationDegrees(ry));
      }

      if (rz != 0.0F) {
         matrices.method_22907(class_7833.field_40718.rotationDegrees(rz));
      }

      if (s != 1.0F) {
         matrices.method_22905(s, s, s);
      }

      class_811 renderMode = arm == class_1306.field_6183 ? class_811.field_4322 : class_811.field_4321;
      this.method_3233(player, item, renderMode, matrices, queue, light);
      matrices.method_22909();
      ci.cancel();
   }

   @WrapWithCondition(
      method = {"method_3222"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 1
      )}
   )
   private boolean cancelMapRotationX(class_4587 matrices, Quaternionfc quaternion) {
      return DualWindowService.isCleanPass();
   }

   @WrapWithCondition(
      method = {"method_3222"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 2
      )}
   )
   private boolean cancelMapRotationY(class_4587 matrices, Quaternionfc quaternion) {
      return DualWindowService.isCleanPass();
   }

   @WrapOperation(
      method = {"method_22976"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 0
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/class_759;method_33303(Lnet/minecraft/class_746;)Lnet/minecraft/class_759$class_5773;"
         ),
         to = @At(
            value = "FIELD",
            target = "Lnet/minecraft/class_759$class_5773;field_28387:Z"
         )
      )}
   )
   private void replacePitchRotation(class_4587 matrices, Quaternionfc quaternion, Operation<Void> original, @Local(ordinal = 3) float h) {
      if (DualWindowService.isCleanPass()) {
         original.call(new Object[]{matrices, quaternion});
      } else {
         if (!NoRender.getInstance().removeCamreZalupa.getValue()) {
            float cameraPitch = class_310.method_1551().field_1773.method_19418().method_19329();
            matrices.method_22907(class_7833.field_40714.rotationDegrees((cameraPitch - h) * 0.1F));
         }
      }
   }

   @WrapOperation(
      method = {"method_22976"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 1
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/class_759;method_33303(Lnet/minecraft/class_746;)Lnet/minecraft/class_759$class_5773;"
         ),
         to = @At(
            value = "FIELD",
            target = "Lnet/minecraft/class_759$class_5773;field_28387:Z"
         )
      )}
   )
   private void replaceYawRotation(class_4587 matrices, Quaternionfc quaternion, Operation<Void> original, @Local(ordinal = 4) float i) {
      if (DualWindowService.isCleanPass()) {
         original.call(new Object[]{matrices, quaternion});
      } else {
         if (!NoRender.getInstance().removeCamreZalupa.getValue()) {
            float cameraYaw = class_310.method_1551().field_1773.method_19418().method_19330();
            matrices.method_22907(class_7833.field_40716.rotationDegrees((cameraYaw - i) * 0.1F));
         }
      }
   }

   @ModifyExpressionValue(
      method = {"method_22976"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_759$class_5773;field_28387:Z"
      )}
   )
   private boolean white$showEditorMainHand(boolean original) {
      return original || !DualWindowService.isCleanPass() && HandsEditor.getInstance().isActive();
   }

   @ModifyExpressionValue(
      method = {"method_22976"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_759$class_5773;field_28388:Z"
      )}
   )
   private boolean white$showEditorOffHand(boolean original) {
      return original || !DualWindowService.isCleanPass() && HandsEditor.getInstance().isActive();
   }

   @ModifyArg(
      method = {"method_22976"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 0
      ),
      index = 5
   )
   private class_1799 white$useEditorMainHandItem(class_1799 original) {
      if (!DualWindowService.isCleanPass() && HandsEditor.getInstance().isActive()) {
         class_746 player = class_310.method_1551().field_1724;
         return player == null ? original : HandsEditor.getInstance().previewStackFor(player.method_6068());
      } else {
         return original;
      }
   }

   @ModifyArg(
      method = {"method_22976"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 1
      ),
      index = 5
   )
   private class_1799 white$useEditorOffHandItem(class_1799 original) {
      if (!DualWindowService.isCleanPass() && HandsEditor.getInstance().isActive()) {
         class_746 player = class_310.method_1551().field_1724;
         return player == null ? original : HandsEditor.getInstance().previewStackFor(player.method_6068().method_5928());
      } else {
         return original;
      }
   }

   @Inject(
      method = {"method_22976"},
      at = {@At("HEAD")}
   )
   private void onRenderItemPre(float tickProgress, class_4587 matrices, class_11659 queue, class_746 player, int light, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         HandsEditor editor = HandsEditor.getInstance();
         if (editor.isActive()) {
            editor.captureBeforeHands();
         } else {
            GlassHands glassHands = GlassHands.getInstance();
            if (glassHands != null && glassHands.isEnabled()) {
               new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.PRE, matrices, tickProgress).hook();
            }
         }
      }
   }

   @Inject(
      method = {"method_22976"},
      at = {@At("TAIL")}
   )
   private void onRenderItemPost(float tickProgress, class_4587 matrices, class_11659 queue, class_746 player, int light, CallbackInfo ci) {
      if (!DualWindowService.isCleanPass()) {
         if (!HandsEditor.getInstance().isActive()) {
            GlassHands glassHands = GlassHands.getInstance();
            if (glassHands != null && glassHands.isEnabled()) {
               new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.POST, matrices, tickProgress).hook();
            }
         }
      }
   }

   @Inject(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22903()V",
         shift = Shift.AFTER,
         ordinal = 0
      )}
   )
   private void onRenderFirstPersonItem(
      class_742 player,
      float tickProgress,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_11659 orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      if (!DualWindowService.isCleanPass()) {
         class_1306 arm = hand == class_1268.field_5808 ? player.method_6068() : player.method_6068().method_5928();
         RenderItemEvent event = new RenderItemEvent(matrices, hand, arm);
         event.hook();
         Hands hands;
         if (!item.method_7960() && (hands = Hands.get()) != null) {
            hands.applyHandTranslation(matrices, arm);
         }
      }
   }

   @WrapWithCondition(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3224(Lnet/minecraft/class_4587;Lnet/minecraft/class_1306;F)V",
         ordinal = 4
      )}
   )
   private boolean cancelEquipOffsetForCustomAnim(class_759 instance, class_4587 matrices, class_1306 arm, float equipProgress) {
      if (arm != class_310.method_1551().field_1724.method_6068()) {
         return true;
      } else {
         SwingAnimation hands = SwingAnimation.get();
         return hands != null && hands.isEnabled() && hands.auraCheck() ? hands.swingMode.is("Отключено") : true;
      }
   }

   @WrapOperation(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_65816(FLnet/minecraft/class_4587;ILnet/minecraft/class_1306;)V"
      )}
   )
   private void handAnimationHook(
      class_759 instance,
      float swingProgress,
      class_4587 matrices,
      int armX,
      class_1306 arm,
      Operation<Void> original,
      @Local(ordinal = 0,argsOnly = true) class_742 player,
      @Local(ordinal = 0,argsOnly = true) class_1268 hand,
      @Local(ordinal = 3,argsOnly = true) float equipProgress,
      @Local(ordinal = 0,argsOnly = true) class_1799 item
   ) {
      if (item.method_7909() instanceof class_1764) {
         original.call(new Object[]{instance, swingProgress, matrices, armX, arm});
      } else {
         HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress, equipProgress, arm);
         event.hook();
         if (!event.isCancelled()) {
            original.call(new Object[]{instance, swingProgress, matrices, armX, arm});
         }
      }
   }

   @Inject(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 0
      )}
   )
   public void injectBeforeRenderCrossBowItem(
      class_742 player,
      float tickProgress,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_11659 orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      if (!DualWindowService.isCleanPass()) {
         Hands viewModel = Hands.get();
         if (viewModel.shouldApplyTransforms()) {
            boolean isMainHand = hand == class_1268.field_5808;
            class_1306 arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
            viewModel.applyHandScale(matrices, arm);
            HandsEditor.getInstance().updateHandBounds(arm, matrices);
         }
      }
   }

   @Inject(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 1
      )}
   )
   public void injectBeforeRenderItem(
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_11659 orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      if (!DualWindowService.isCleanPass()) {
         Hands viewModel = Hands.get();
         if (viewModel.shouldApplyTransforms()) {
            boolean isMainHand = hand == class_1268.field_5808;
            class_1306 arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
            viewModel.applyHandScale(matrices, arm);
            HandsEditor.getInstance().updateHandBounds(arm, matrices);
         }
      }
   }
}
