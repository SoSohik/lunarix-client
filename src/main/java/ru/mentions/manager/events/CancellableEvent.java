package ru.mentions.manager.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.orbit.ICancellable;

@Environment(EnvType.CLIENT)
public class CancellableEvent extends Event implements ICancellable {
   private boolean cancelled;

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Override
   public void cancel() {
      ICancellable.super.cancel();
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }
}
