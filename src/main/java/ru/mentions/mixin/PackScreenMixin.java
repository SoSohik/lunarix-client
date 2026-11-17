package ru.mentions.mixin;

import java.io.File;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_5375;
import net.minecraft.class_7919;
import net.minecraft.class_4185.class_7840;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.mentions.module.impl.utils.UnHook;

@Environment(EnvType.CLIENT)
@Mixin({class_5375.class})
public abstract class PackScreenMixin {
   @Unique
   private static final Logger LOGGER = LoggerFactory.getLogger(PackScreenMixin.class);
   @Unique
   private static final class_2561 OPEN_FOLDER = class_2561.method_43471("pack.openFolder");
   @Unique
   private static final class_2561 FOLDER_INFO = class_2561.method_43471("pack.folderInfo");
   @Shadow
   private Path field_25474;

   @Redirect(
      method = {"method_25426"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4185$class_7840;method_46431()Lnet/minecraft/class_4185;"
      )
   )
   private class_4185 redirectOpenFolderButton(class_7840 builder) {
      class_2561 builderMessage = ((MessageAccessor)builder).getMessage();
      return builderMessage.equals(OPEN_FOLDER) ? class_4185.method_46430(OPEN_FOLDER, button -> {
         File folderToOpen = UnHook.unhooked && UnHook.resourcePackFolder != null ? UnHook.resourcePackFolder : this.field_25474.toFile();
         if (!folderToOpen.exists()) {
            folderToOpen.mkdirs();
         }

         LOGGER.info("Opening folder: {}", folderToOpen.getPath());
         class_156.method_668().method_672(folderToOpen);
      }).method_46436(class_7919.method_47407(FOLDER_INFO)).method_46431() : builder.method_46431();
   }
}
