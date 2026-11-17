package ru.mentions.manager.events.orbit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.orbit.listeners.ArrayBackedImmutableList;
import ru.mentions.manager.events.orbit.listeners.IListener;
import ru.mentions.manager.events.orbit.listeners.LambdaListener;
import ru.mentions.manager.events.orbit.listeners.ListOperations;

@Environment(EnvType.CLIENT)
public class EventBus implements IEventBus {
   private final Map<Object, List<IListener>> listenerCache;
   private final Map<Class<?>, List<IListener>> staticListenerCache;
   private final Map<Class<?>, List<IListener>> listenerMap;
   private final Supplier<List<IListener>> listenerListFactory;
   private final List<EventBus.LambdaFactoryInfo> lambdaFactoryInfos = new CopyOnWriteArrayList<>();

   public EventBus(
      Map<Object, List<IListener>> listenerCache,
      Map<Class<?>, List<IListener>> staticListenerCache,
      Map<Class<?>, List<IListener>> listenerMap,
      Supplier<List<IListener>> listenerListFactory
   ) {
      this.listenerCache = listenerCache;
      this.staticListenerCache = staticListenerCache;
      this.listenerMap = listenerMap;
      this.listenerListFactory = listenerListFactory;
   }

   public static EventBus threadSafe() {
      return new EventBus(Collections.synchronizedMap(new IdentityHashMap<>()), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);
   }

   public static EventBus threadUnsafe() {
      return new EventBus(new IdentityHashMap<>(), new IdentityHashMap<>(), new IdentityHashMap<>(), CopyOnWriteArrayList::new);
   }

   @Override
   public void registerLambdaFactory(String packagePrefix, LambdaListener.Factory factory) {
      this.lambdaFactoryInfos.add(new EventBus.LambdaFactoryInfo(packagePrefix, factory));
   }

   @Override
   public boolean isListening(Class<?> eventClass) {
      List<IListener> listeners = this.listenerMap.get(eventClass);
      return listeners != null && !listeners.isEmpty();
   }

   public boolean isSubscribed(IListener listener) {
      List<IListener> listeners = this.listenerMap.get(listener.getTarget());
      return listeners != null && ListOperations.contains(listeners, listener);
   }

   public boolean isSubscribed(Object object) {
      return this.listenerCache.containsKey(object);
   }

   public boolean isSubscribed(Class<?> staticListener) {
      for (IListener listener : this.staticListenerCache.get(staticListener)) {
         if (listener instanceof LambdaListener lambdaListener) {
            if (lambdaListener.owner != staticListener) {
               break;
            }

            if (!this.isSubscribed(listener)) {
               return true;
            }
         }
      }

      return true;
   }

   public boolean areAnySubscribed(Class<?> staticListener) {
      List<IListener> listeners = this.staticListenerCache.get(staticListener);
      if (listeners == null) {
         return false;
      } else {
         for (IListener listener : listeners) {
            if (this.isSubscribed(listener)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean areAllSubscribed(Class<?> staticListener) {
      List<IListener> listeners = this.staticListenerCache.get(staticListener);
      if (listeners == null) {
         return false;
      } else {
         for (IListener listener : listeners) {
            if (!this.isSubscribed(listener)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public <T> T post(T event) {
      List<IListener> listeners = this.listenerMap.get(event.getClass());
      if (listeners != null) {
         for (IListener listener : listeners) {
            try {
               listener.call(event);
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }
      }

      return event;
   }

   @Override
   public <T extends ICancellable> T post(T event) {
      List<IListener> listeners = this.listenerMap.get(event.getClass());
      if (listeners != null) {
         event.setCancelled(false);

         for (IListener listener : listeners) {
            try {
               listener.call(event);
            } catch (Exception var6) {
               var6.printStackTrace();
            }

            if (event.isCancelled()) {
               break;
            }
         }
      }

      return event;
   }

   @Override
   public void subscribe(Object object) {
      if (!this.listenerCache.containsKey(object)) {
         this.subscribe(this.getInstanceListeners(object), false);
      }
   }

   @Override
   public void subscribe(Class<?> klass) {
      List<IListener> listeners = this.getStaticListeners(klass);
      this.subscribe(listeners, true);
   }

   @Override
   public void subscribe(IListener listener) {
      this.subscribe(listener, true);
   }

   private void subscribe(List<IListener> listeners, boolean check) {
      for (IListener listener : listeners) {
         this.subscribe(listener, check);
      }
   }

   private void subscribe(IListener listener, boolean check) {
      List listeners = this.listenerMap.computeIfAbsent(listener.getTarget(), klass -> this.listenerListFactory.get());
      if (!check || !ListOperations.contains(listeners, listener)) {
         ListOperations.insert(listeners, listener);
      }
   }

   @Override
   public void unsubscribe(Object object) {
      List<IListener> listeners = this.listenerCache.get(object);
      if (listeners != null) {
         this.unsubscribe(listeners);
         this.listenerCache.remove(object);
      }
   }

   @Override
   public void unsubscribe(Class<?> klass) {
      List<IListener> listeners = this.staticListenerCache.get(klass);
      if (listeners != null) {
         this.unsubscribe(listeners);
      }
   }

   @Override
   public void unsubscribe(IListener listener) {
      List<IListener> listeners = this.listenerMap.get(listener.getTarget());
      if (listeners != null) {
         ListOperations.remove(listeners, listener);
      }
   }

   private void unsubscribe(List<IListener> listeners) {
      for (IListener listener : listeners) {
         this.unsubscribe(listener);
      }
   }

   private List<IListener> getStaticListeners(Class<?> klass) {
      return this.staticListenerCache.computeIfAbsent(klass, o -> this.getListeners((Class<?>)o, null, true));
   }

   private List<IListener> getInstanceListeners(Object object) {
      return this.listenerCache.computeIfAbsent(object, o -> this.getListeners(o.getClass(), o, false));
   }

   private List<IListener> getListeners(Class<?> klass, Object object, boolean staticOnly) {
      ArrayList<IListener> listeners = new ArrayList<>();
      this.getListeners(listeners, klass, object, staticOnly);
      return new ArrayBackedImmutableList<>(listeners);
   }

   private void getListeners(List<IListener> listeners, Class<?> klass, Object object, boolean staticOnly) {
      while (klass != null) {
         LambdaListener.Factory factory = null;

         for (Method method : klass.getDeclaredMethods()) {
            if (this.isValid(method, staticOnly)) {
               if (LambdaListener.requireLambdaFactoryRegistration() && factory == null) {
                  factory = this.getLambdaFactory(klass);
               }

               listeners.add(new LambdaListener(factory, klass, object, method));
            }
         }

         klass = klass.getSuperclass();
      }
   }

   private boolean isValid(Method method, boolean staticOnly) {
      if (staticOnly && !Modifier.isStatic(method.getModifiers())) {
         return false;
      } else if (!method.isAnnotationPresent(EventHandler.class)) {
         return false;
      } else if (method.getReturnType() != void.class) {
         return false;
      } else {
         return method.getParameterCount() != 1 ? false : !method.getParameters()[0].getType().isPrimitive();
      }
   }

   private LambdaListener.Factory getLambdaFactory(Class<?> klass) {
      for (EventBus.LambdaFactoryInfo info : this.lambdaFactoryInfos) {
         if (klass.getName().startsWith(info.packagePrefix)) {
            return info.factory;
         }
      }

      throw new NoLambdaFactoryException(klass);
   }

   @Environment(EnvType.CLIENT)
   private static record LambdaFactoryInfo(String packagePrefix, LambdaListener.Factory factory) {
   }
}
