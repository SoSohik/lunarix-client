package ru.mentions.manager.events.orbit.listeners;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.orbit.EventHandler;

@Environment(EnvType.CLIENT)
public class LambdaListener implements IListener {
   private static final LambdaListener.LookupFactory LOOKUP_FACTORY;
   private static final Map<Method, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();
   private final Class<?> target;
   private final boolean isStatic;
   private final int priority;
   private final Consumer<Object> executor;
   public final Class<?> owner;

   public LambdaListener(LambdaListener.Factory factory, Class<?> klass, Object object, Method method) {
      this.target = method.getParameters()[0].getType();
      this.isStatic = Modifier.isStatic(method.getModifiers());
      this.priority = method.getAnnotation(EventHandler.class).priority();
      this.owner = klass;

      try {
         this.executor = (Consumer<Object>)(
            this.isStatic
               ? (Object)staticLambdaFactory(factory, klass, method).invoke()
               : (Object)instanceLambdaFactory(factory, klass, method).invoke((Object)object)
         );
      } catch (Throwable var6) {
         throw new RuntimeException(var6);
      }
   }

   private static MethodHandle staticLambdaFactory(LambdaListener.Factory factory, Class<?> klass, Method method) throws Throwable {
      String name = method.getName();
      Lookup lookup = LOOKUP_FACTORY.of(factory, klass);
      MethodType methodType = MethodType.methodType(void.class, method.getParameters()[0].getType());
      MethodHandle methodHandle = lookup.findStatic(klass, name, methodType);
      MethodType invokedType = MethodType.methodType(Consumer.class);
      return LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(void.class, Object.class), methodHandle, methodType)
         .getTarget();
   }

   private static MethodHandle instanceLambdaFactory(LambdaListener.Factory factory, Class<?> klass, Method method) throws Throwable {
      MethodHandle lambdaFactory = methodHandleCache.get(method);
      if (lambdaFactory != null) {
         return lambdaFactory;
      } else {
         String name = method.getName();
         Lookup lookup = LOOKUP_FACTORY.of(factory, klass);
         MethodType methodType = MethodType.methodType(void.class, method.getParameters()[0].getType());
         MethodHandle methodHandle = lookup.findVirtual(klass, name, methodType);
         MethodType invokedType = MethodType.methodType(Consumer.class, klass);
         lambdaFactory = LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(void.class, Object.class), methodHandle, methodType)
            .getTarget();
         methodHandleCache.put(method, lambdaFactory);
         return lambdaFactory;
      }
   }

   @Override
   public void call(Object event) {
      this.executor.accept(event);
   }

   @Override
   public Class<?> getTarget() {
      return this.target;
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @Override
   public boolean isStatic() {
      return this.isStatic;
   }

   public static boolean requireLambdaFactoryRegistration() {
      return LOOKUP_FACTORY.requireLambdaFactory;
   }

   static {
      LambdaListener.LookupFactory lookupFactory;
      try {
         try {
            final Method privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, Lookup.class);
            lookupFactory = new LambdaListener.LookupFactory(true) {
               @Override
               public Lookup of(LambdaListener.Factory lambdaFactory, Class<?> klass) throws ReflectiveOperationException {
                  return lambdaFactory.create(privateLookupInMethod, klass);
               }
            };
         } catch (NoSuchMethodException var3) {
            final Constructor lookupConstructor = Lookup.class.getDeclaredConstructor(Class.class);
            lookupConstructor.setAccessible(true);
            lookupFactory = new LambdaListener.LookupFactory(false) {
               @Override
               public Lookup of(LambdaListener.Factory lambdaFactory, Class<?> klass) throws ReflectiveOperationException {
                  return (Lookup)lookupConstructor.newInstance(klass);
               }
            };
         }
      } catch (SecurityException | ReflectiveOperationException var4) {
         lookupFactory = new LambdaListener.LookupFactory(false) {
            @Override
            public Lookup of(LambdaListener.Factory lambdaFactory, Class<?> klass) {
               return MethodHandles.publicLookup().in(klass);
            }
         };
      }

      LOOKUP_FACTORY = lookupFactory;
   }

   @Environment(EnvType.CLIENT)
   public interface Factory {
      Lookup create(Method var1, Class<?> var2) throws ReflectiveOperationException;
   }

   @Environment(EnvType.CLIENT)
   private abstract static class LookupFactory {
      public final boolean requireLambdaFactory;

      public LookupFactory(boolean requireLambdaFactory) {
         this.requireLambdaFactory = requireLambdaFactory;
      }

      public abstract Lookup of(LambdaListener.Factory var1, Class<?> var2) throws ReflectiveOperationException;
   }
}
