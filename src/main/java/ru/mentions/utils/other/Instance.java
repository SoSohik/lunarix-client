package ru.mentions.utils.other;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.module.api.Module;

@Environment(EnvType.CLIENT)
public final class Instance {
   private static final ConcurrentMap<Class<? extends Module>, Module> instanceModules = new ConcurrentHashMap<>();

   public static <T extends Module> T get(Class<T> clazz) {
      return clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> Client.get().moduleManager().get((Class<? extends Module>)instance)));
   }

   public static <T extends Module> T get(String module) {
      return Client.get().moduleManager().get(module);
   }

   @Generated
   private Instance() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
