package ru.mentions.mixin;

import java.lang.reflect.Method;
import java.util.Collections;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the player connected when a resource pack fails to reload while playing on a server.
 * Vanilla reacts to such a failure either by crashing the game or by disconnecting the level and
 * switching to the title screen. Both behaviours are undesirable while a world is already loaded,
 * so in that case the broken packs are simply dropped and the resources are reloaded again.
 */
@Environment(EnvType.CLIENT)
@Mixin({class_310.class})
public abstract class MinecraftClientResourcePackMixin {
   @Unique
   private static final Logger LUNARIX_PACK_LOGGER = LoggerFactory.getLogger("Lunarix/ResourcePack");
   @Unique
   private static final String LUNARIX_CLEAR_PACKS = "method_31186";
   @Unique
   private static final String LUNARIX_RELOAD_PACKS = "method_1521";
   @Unique
   private static final String LUNARIX_FAIL_TOAST = "method_49292";
   @Unique
   private static long lunarix$lastRecovery;

   @Inject(
      method = {"method_63507"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lunarix$keepWorldOnPackFailure(Exception exception, CallbackInfo ci) {
      class_310 client = (class_310)(Object)this;
      if (client.field_1687 == null || client.field_1724 == null) {
         return;
      }

      ci.cancel();
      this.lunarix$recoverInWorld(client, exception);
   }

   @WrapOperation(
      method = {"method_24226"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_156;method_24155(Ljava/lang/Throwable;)V"
      )}
   )
   private void lunarix$preventPackCrash(Throwable throwable, Operation<Void> original) {
      class_310 client = (class_310)(Object)this;
      if (client.field_1687 != null && client.field_1724 != null) {
         this.lunarix$recoverInWorld(client, throwable);
      } else {
         original.call(new Object[]{throwable});
      }
   }

   @Inject(
      method = {"method_49294"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lunarix$blockAbortToTitle(CallbackInfo ci) {
      class_310 client = (class_310)(Object)this;
      if (client.field_1687 != null && client.field_1724 != null) {
         LUNARIX_PACK_LOGGER.warn("Blocked vanilla resource pack recovery abort while in world");
         ci.cancel();
      }
   }

   @Unique
   private void lunarix$recoverInWorld(class_310 client, Throwable throwable) {
      long now = System.currentTimeMillis();
      if (now - lunarix$lastRecovery < 4000L) {
         LUNARIX_PACK_LOGGER.warn("Resource pack reload failed again while in world, ignoring the repeated recovery", throwable);
         return;
      }

      lunarix$lastRecovery = now;
      LUNARIX_PACK_LOGGER.warn("Resource pack reload failed while in world, keeping the connection instead of disconnecting", throwable);
      if (!this.lunarix$dropFailedPacks(client, throwable)) {
         this.lunarix$showFailToast(client);
      }
   }

   @Unique
   private boolean lunarix$dropFailedPacks(class_310 client, Throwable throwable) {
      try {
         Method clear = lunarix$findMethod(class_310.class, LUNARIX_CLEAR_PACKS, 3);
         if (clear != null) {
            clear.setAccessible(true);
            clear.invoke(client, throwable, class_2561.method_43471("resourcePack.runtime_failure"), null);
            return true;
         }
      } catch (Throwable error) {
         LUNARIX_PACK_LOGGER.warn("Could not use vanilla resource pack recovery, trying a manual reload", error);
      }

      try {
         Object repository = lunarix$readField(client, "field_1715");
         Method setSelected = repository == null ? null : lunarix$findMethod(repository.getClass(), "method_14447", 1);
         if (setSelected != null) {
            setSelected.setAccessible(true);
            setSelected.invoke(repository, Collections.emptyList());
         }

         Method reload = lunarix$findMethod(class_310.class, LUNARIX_RELOAD_PACKS, 0);
         if (reload != null) {
            reload.setAccessible(true);
            reload.invoke(client);
            return true;
         }
      } catch (Throwable error) {
         LUNARIX_PACK_LOGGER.error("Manual resource pack recovery failed", error);
      }

      return false;
   }

   @Unique
   private void lunarix$showFailToast(class_310 client) {
      try {
         Method toast = lunarix$findMethod(class_310.class, LUNARIX_FAIL_TOAST, 1);
         if (toast != null) {
            toast.setAccessible(true);
            toast.invoke(client, class_2561.method_43471("resourcePack.load_fail"));
         }
      } catch (Throwable error) {
         LUNARIX_PACK_LOGGER.debug("Could not show resource pack failure toast", error);
      }
   }

   @Unique
   private static Method lunarix$findMethod(Class<?> owner, String name, int parameterCount) {
      for (Method method : owner.getDeclaredMethods()) {
         if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
            return method;
         }
      }

      return null;
   }

   @Unique
   private static Object lunarix$readField(Object owner, String name) throws ReflectiveOperationException {
      var field = owner.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return field.get(owner);
   }
}
