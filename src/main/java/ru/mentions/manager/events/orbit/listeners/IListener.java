package ru.mentions.manager.events.orbit.listeners;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IListener {
   void call(Object var1);

   Class<?> getTarget();

   int getPriority();

   boolean isStatic();
}
