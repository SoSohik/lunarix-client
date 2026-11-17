package ru.mentions.maintenance.mem;

import java.util.List;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import ru.mentions.maintenance.DiagLog;
import ru.mentions.maintenance.ModuleState;

public final class MixinMemoryBootstrap {
   private MixinMemoryBootstrap() {
   }

   public static boolean attach(MemoryJarIndex index, MemoryClassLoader loader) {
      try {
         MixinBootstrap.init();
      } catch (Throwable var14) {
         DiagLog.fail("mixin.init", var14);
         return false;
      }

      Thread thread = Thread.currentThread();
      ClassLoader previous = thread.getContextClassLoader();

      boolean var5;
      try {
         thread.setContextClassLoader(loader);
         List<String> configs = FabricEntrypoints.mixinConfigs(index);

         for (String config : configs) {
            try {
               Mixins.addConfiguration(config);
               ModuleState.addMixinConfig(config);
            } catch (Throwable var13) {
               DiagLog.fail("mixin.config:" + config, var13);
            }
         }

         DiagLog.note("mixin", "configs=" + configs.size());
         return !configs.isEmpty();
      } catch (Throwable var15) {
         DiagLog.fail("mixin.attach", var15);
         var5 = false;
      } finally {
         thread.setContextClassLoader(previous);
      }

      return var5;
   }
}
