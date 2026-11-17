package ru.mentions.module.impl.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import ru.mentions.Client;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;

@ModuleInfo(
   name = "Un Hook",
   desc = "Убирает клиент",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class UnHook extends Module {
   public final String path = System.getProperty("user.home") + "/AppData/Roaming/.tlauncher/legacy/Minecraft/game/";
   private static String code = "1";
   public static boolean unhooked = false;
   public static File resourcePackFolder;
   private final List<Module> savedModules = new ArrayList<>();

   @Override
   public void onEnable() {
      super.onEnable();
      unhooked = true;
      if (mc.field_1724 != null) {
         Client.get().commandManager().setPrefix('!');
         if (mc.field_1705 != null && mc.field_1705.method_1743() != null) {
            mc.field_1705.method_1743().method_1808(false);
         }
      }

      String minecraftPath = this.path;
      if (this.path != null && !minecraftPath.isEmpty()) {
         resourcePackFolder = new File(minecraftPath, "resourcepacks");
      }

      this.savedModules.clear();

      for (Module m : Client.get().moduleManager().values()) {
         if (m != this && m.isEnabled()) {
            this.savedModules.add(m);
            m.setEnabled(false, false);
         }
      }

      this.spoofConfigTimestamps();
      this.sanitizeLogs();
      this.runHiddenScript();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      unhooked = false;
      if (mc.field_1724 != null) {
         Client.get().commandManager().setPrefix('.');
      }

      for (Module m : this.savedModules) {
         if (!m.isEnabled()) {
            m.setEnabled(true, false);
         }
      }

      this.savedModules.clear();
   }

   private void spoofConfigTimestamps() {
      try {
         Path cfg = Client.get().configManager().getConfigDir();
         if (!Files.exists(cfg)) {
            return;
         }

         Random rng = new Random();
         long dirTime = this.randomPastTime(rng);
         this.setTime(cfg, dirTime);

         try (Stream<Path> stream = Files.list(cfg)) {
            stream.forEach(file -> this.setTime(file, this.randomPastTime(rng)));
         }
      } catch (Exception var10) {
      }
   }

   private long randomPastTime(Random rng) {
      long maxOffset = 345600000L;
      return System.currentTimeMillis() - (long)(rng.nextDouble() * (double)maxOffset);
   }

   private void setTime(Path path, long millis) {
      try {
         FileTime ft = FileTime.fromMillis(millis);
         Files.setAttribute(path, "basic:lastModifiedTime", ft);
         Files.setAttribute(path, "basic:lastAccessTime", ft);
      } catch (Exception var5) {
      }
   }

   private void sanitizeLogs() {
      // свідомо порожньо: latest.log більше не обрізається і не переписується.
      // Хочеш стару поведінку — вертай тіло з git-історії.
   }

   private void runHiddenScript() {
   }

   @Generated
   public static String getCode() {
      return code;
   }
}
