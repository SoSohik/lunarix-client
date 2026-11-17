package ru.mentions.manager.events.orbit;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.orbit.listeners.IListener;
import ru.mentions.manager.events.orbit.listeners.LambdaListener;

@Environment(EnvType.CLIENT)
public interface IEventBus {
   void registerLambdaFactory(String var1, LambdaListener.Factory var2);

   boolean isListening(Class<?> var1);

   <T> T post(T var1);

   <T extends ICancellable> T post(T var1);

   void subscribe(Object var1);

   void subscribe(Class<?> var1);

   void subscribe(IListener var1);

   void unsubscribe(Object var1);

   void unsubscribe(Class<?> var1);

   void unsubscribe(IListener var1);
}
