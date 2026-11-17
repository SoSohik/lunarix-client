package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_746;
import net.minecraft.class_1269.class_9860;
import net.minecraft.class_1269.class_9861;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.UsingItemEvent;
import ru.mentions.module.impl.player.ClickHelper;

@Environment(EnvType.CLIENT)
@Mixin({class_636.class})
public class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"method_2919"},
      at = {@At("RETURN")}
   )
   public void interactItemHook(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
      Object object = cir.getReturnValue();
      class_9860 success;
      if (object instanceof class_9860 && !(success = (class_9860)object).comp_2909().equals(class_9861.field_52427)) {
         UsingItemEvent event = new UsingItemEvent((byte)0);
         event.hook();
      }
   }

   @Inject(
      method = {"method_2918"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAttackEntity(class_1657 player, class_1297 target, CallbackInfo ci) {
      AttackEvent event = new AttackEvent(target);
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_2897"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void stopUsingItemHook(CallbackInfo ci) {
      UsingItemEvent event = new UsingItemEvent((byte)2);
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_2919"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void gameModeHook(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
      UsingItemEvent event = new UsingItemEvent((byte)-1);
      event.hook();
      if (event.isCancelled()) {
         cir.setReturnValue(class_1269.field_5811);
      }
   }

   @Inject(
      method = {"method_2896"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onInteractBlock(class_746 player, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir) {
      if (hand == class_1268.field_5810
         && ClickHelper.getInstance() != null
         && ClickHelper.getInstance().isEnabled()
         && ClickHelper.getInstance().isNoLeftHandPlace()) {
         class_1799 off = player.method_6079();
         if (off != null && !off.method_7960()) {
            String name = off.method_7964().getString().toLowerCase();
            if (name.contains("сфер")
               || name.contains("талисман")
               || name.contains("шар")
               || name.contains("head")
               || name.contains("skull")
               || name.contains("голов")) {
               cir.setReturnValue(class_1269.field_5811);
            }
         }
      }
   }
}
