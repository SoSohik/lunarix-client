package ru.mentions.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import net.minecraft.class_12079.class_10726;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mentions.Client;
import ru.mentions.module.impl.render.Globals;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
@Mixin({class_742.class})
public abstract class PlayerListEntryMixin {
   @Unique
   private static final class_2960 LUNARIX_COSMIC_CAPE_ID = class_2960.method_60655("lunarix", "textures/capes/lunarix_cosmic.png");
   @Unique
   private static final class_10726 LUNARIX_CAPE_ASSET = new class_10726(LUNARIX_COSMIC_CAPE_ID);

   @Inject(
      method = {"method_52814"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void replaceCape(CallbackInfoReturnable<class_8685> cir) {
      if (!DualWindowService.isCleanPass()) {
         class_742 player = (class_742)this;
         class_310 client = class_310.method_1551();
         if (client.field_1724 != null) {
            boolean isLocal = player.method_5667().equals(client.field_1724.method_5667());
            boolean isFriend = Client.get().friendManager().isFriend(player.method_5477().getString());
            boolean isSoft = Globals.isSoftUser(player.method_5477().getString());
            if (isLocal || isFriend || isSoft) {
               class_8685 old = (class_8685)cir.getReturnValue();
               if (old != null) {
                  cir.setReturnValue(new class_8685(old.comp_1626(), LUNARIX_CAPE_ASSET, LUNARIX_CAPE_ASSET, old.comp_1629(), old.comp_1630()));
               }
            }
         }
      }
   }
}
