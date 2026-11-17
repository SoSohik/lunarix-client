package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_338;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.render.Globals;
import ru.mentions.module.impl.utils.ChatFilter;
import ru.mentions.utils.funtime.FunTimeEventsService;

@Environment(EnvType.CLIENT)
@Mixin({class_338.class})
public class ChatHudMixin {
   @Inject(
      method = {"method_1812"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void chatfilter$hide(class_2561 text, CallbackInfo ci) {
      checkGlobalsSignature(text);
      if (ChatFilter.shouldHide(text)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_44811"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void chatfilter$hideSigned(class_2561 text, class_7469 signature, class_7591 indicator, CallbackInfo ci) {
      checkGlobalsSignature(text);
      if (ChatFilter.shouldHide(text)) {
         ci.cancel();
      }
   }

   private static void checkGlobalsSignature(class_2561 text) {
      if (text != null) {
         String msg = text.getString();

         try {
            FunTimeEventsService.onChatMessage(msg);
         } catch (Throwable var6) {
         }

         if (msg != null && !msg.isEmpty()) {
            if (msg.contains("\u200b\u200c\u200d") || msg.toLowerCase().contains("[lunarix]") || msg.toLowerCase().contains("[mentions]")) {
               class_310 client = class_310.method_1551();
               if (client.field_1687 != null && client.field_1724 != null) {
                  for (class_1657 player : client.field_1687.method_18456()) {
                     String pName = player.method_5477().getString();
                     if (!player.method_5667().equals(client.field_1724.method_5667()) && msg.contains(pName)) {
                        Globals.addSoftUser(pName);
                     }
                  }
               }
            }
         }
      }
   }

   @ModifyVariable(
      method = {"method_1812"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private class_2561 chatfilter$transform(class_2561 text) {
      class_2561 replaced = ChatFilter.transform(text);
      return replaced != null ? replaced : text;
   }

   @ModifyVariable(
      method = {"method_44811"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private class_2561 chatfilter$transformSigned(class_2561 text) {
      class_2561 replaced = ChatFilter.transform(text);
      return replaced != null ? replaced : text;
   }
}
