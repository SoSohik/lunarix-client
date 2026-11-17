package ru.mentions.utils.render;

import java.lang.reflect.Method;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class IrisCompat {
   private static boolean checked = false;
   private static boolean present = false;
   private static Object apiInstance;
   private static Method isShaderPackInUseMethod;

   private IrisCompat() {
   }

   private static void init() {
      if (!checked) {
         checked = true;
         boolean modPresent = FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus");
         if (modPresent) {
            for (String className : new String[]{"net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi"}) {
               try {
                  Class<?> apiClass = Class.forName(className);
                  Method getInstance = apiClass.getMethod("getInstance");
                  apiInstance = getInstance.invoke(null);
                  isShaderPackInUseMethod = apiClass.getMethod("isShaderPackInUse");
                  present = true;
                  return;
               } catch (Throwable var9) {
               }
            }
         }
      }
   }

   public static boolean shadersActive() {
      init();
      if (!present) {
         return false;
      } else {
         try {
            Object result = isShaderPackInUseMethod.invoke(apiInstance);
            return result instanceof Boolean && (Boolean)result;
         } catch (Throwable var1) {
            return false;
         }
      }
   }
}
