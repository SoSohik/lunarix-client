package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4185;
import net.minecraft.class_419;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mentions.module.impl.utils.AutoReconnect;
import ru.mentions.screen.AltManagerScreen;

@Environment(EnvType.CLIENT)
@Mixin({class_419.class})
public abstract class DisconnectedScreenMixin {
   @Inject(
      method = {"method_25426"},
      at = {@At("TAIL")}
   )
   private void autoreconnect$init(CallbackInfo ci) {
      AutoReconnect module = AutoReconnect.get();
      if (module != null) {
         class_2561 reason = ((DisconnectedScreenAccessor)this).mentions$reason();
         module.onDisconnectedScreenInit(reason == null ? "" : reason.getString());

         try {
            class_437 screen = (class_437)this;
            ((ScreenAccessor)this)
               .mentions$addDrawableChild(
                  class_4185.method_46430(class_2561.method_43470("Аккаунты"), button -> class_310.method_1551().method_1507(new AltManagerScreen(screen)))
                     .method_46434(screen.field_22789 - 105, 10, 95, 20)
                     .method_46431()
               );
         } catch (Throwable var6) {
         }

         if (module.isCounting()) {
            try {
               class_437 screen = (class_437)this;
               ((ScreenAccessor)this)
                  .mentions$addDrawableChild(
                     class_4185.method_46430(class_2561.method_43470(module.cancelLabel()), button -> module.cancelReconnect())
                        .method_46434(screen.field_22789 / 2 - 100, screen.field_22790 - 30, 200, 20)
                        .method_46431()
                  );
            } catch (Throwable var5) {
            }
         }
      }
   }
}
