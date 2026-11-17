package ru.mentions.module.impl.utils;

import java.io.File;
import net.fabricmc.loader.api.FabricLoader;

final class AutoWardenPaths {
   static final File DIRECTORY = new File(FabricLoader.getInstance().getConfigDir().toFile(), "autowarden");

   private AutoWardenPaths() {
   }
}
