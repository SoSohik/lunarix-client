package ru.mentions.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.module.impl.combat.HitBoxes;
import ru.mentions.module.impl.pasted.GlowEsp;
import ru.mentions.module.impl.player.NoPush;
import ru.mentions.module.impl.render.ShaderEsp;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
@Mixin({class_1297.class})
public class EntityMixin implements IMinecraft {
   @ModifyExpressionValue(
      method = {"method_5784"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_66247()Z",
         ordinal = 1
      )}
   )
   public boolean fixFalldistanceValue(boolean original) {
      return this == mc.field_1724 ? true : original;
   }

   @Inject(
      method = {"method_5871"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void client$getTargetingMargin(CallbackInfoReturnable<Float> cir) {
      class_1297 self = (class_1297)this;
      if (self instanceof class_1657) {
         if (HitBoxes.get().isEnabled()) {
            if (self instanceof class_1657 player && Client.get().friendManager().isFriend(player.method_5477().getString())) {
               return;
            }

            float base = (Float)cir.getReturnValue();
            float extra = HitBoxes.get().size.getValue();
            cir.setReturnValue(base + extra);
         }
      }
   }

   @Inject(
      method = {"method_5851"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void espMarkGlowing(CallbackInfoReturnable<Boolean> cir) {
      if (!(Boolean)cir.getReturnValue()) {
         ShaderEsp esp = ShaderEsp.getInstance();
         if (esp != null && esp.isEnabled() && ShaderEsp.isEspTarget((class_1297)this)) {
            cir.setReturnValue(true);
         } else {
            GlowEsp glow = GlowEsp.getInstance();
            if (glow != null && glow.isEnabled() && glow.isGlowTarget((class_1297)this)) {
               cir.setReturnValue(true);
            }
         }
      }
   }

   @Inject(
      method = {"method_22861"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void espFriendTeamColor(CallbackInfoReturnable<Integer> cir) {
      ShaderEsp esp = ShaderEsp.getInstance();
      GlowEsp glow = GlowEsp.getInstance();
      boolean shaderActive = esp != null && esp.isEnabled();
      boolean glowActive = glow != null && glow.isEnabled();
      if (shaderActive || glowActive) {
         class_1297 self = (class_1297)this;
         if (self instanceof class_1657 p) {
            if (Client.get().friendManager().isFriend(p.method_5477().getString())) {
               cir.setReturnValue(5635925);
            }
         }
      }
   }

   @Inject(
      method = {"method_5697"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPushAwayFrom(class_1297 entity, CallbackInfo ci) {
      class_1297 self = (class_1297)this;
      if (self instanceof class_746) {
         NoPush noPush = NoPush.get();
         if (noPush != null && noPush.isEnabled()) {
            if (entity instanceof class_1657 && noPush.entity.getValue()) {
               ci.cancel();
            } else if (entity instanceof class_1309 && !(entity instanceof class_1657) && noPush.entity.getValue()) {
               ci.cancel();
            }
         }
      }
   }
}
