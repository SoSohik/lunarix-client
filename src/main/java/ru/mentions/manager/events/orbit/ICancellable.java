package ru.mentions.manager.events.orbit;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ICancellable {
   void setCancelled(boolean var1);

   default void cancel() {
      this.setCancelled(true);
   }

   boolean isCancelled();
}
