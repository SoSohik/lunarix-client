package ru.mentions.mixin;

import java.lang.reflect.Method;
import java.util.Collections;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1066;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3283;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the player connected when a resource pack fails to reload while joining a server or while
 * already playing.
 * <p>
 * Vanilla reacts to a resource pack failure by either crashing the game or by throwing the player
 * back to the title screen. The title screen branch is exactly what happens when a server sends a
 * custom resource pack: the pack is kept as a required pack, so clearing the <em>selected</em>
 * packs does not remove it, the forced reload fails again and the client aborts the whole
 * connection.
 * <p>
 * The "we are connected" state is detected through the network handler instead of the world/player
 * fields, because those are still {@code null} during the join. While connected the failed server
 * pack is discarded, the resources are reloaded and the connection is kept alive.
 */
@Environment(EnvType.CLIENT)
@Mixin({class_310.class})
public abstract class MinecraftClientResourcePackMixin {
   @Unique
   private static final Logger LUNARIX_PACK_LOGGER = LoggerFactory.getLogger("Lunarix/ResourcePack");
   @Unique
   private static final String LUNARIX_FAIL_TOAST = "method_49292";
   @Unique
   private static long lunarix$lastRecovery;
   @Unique
   private static boolean lunarix$recovering;

   /**
    * {@code method_24226} is the vanilla in-game resource reload failure handler. When no other pack
    * is selected it crashes the game, otherwise it starts a forced reload. While connected we never
    * want either, so the crash call is replaced by our recovery.
    */
   @Redirect(
      method = {"method_24226"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_156;method_24155(Ljava/lang/Throwable;)V"
      )}
   )
   private void lunarix$preventPackCrash(Throwable throwable) {
      if (this.lunarix$isConnected()) {
         this.lunarix$recover(throwable);
      } else {
         class_156.method_24155(throwable);
      }
   }

   /**
    * {@code method_63507} is the shader/resource failure handler. A broken server pack can break
    * core shaders as well, and vanilla answers with a crash screen or the title screen.
    */
   @Inject(
      method = {"method_63507"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lunarix$keepConnectionOnPackFailure(Exception exception, CallbackInfo ci) {
      if (!this.lunarix$isConnected()) {
         return;
      }

      ci.cancel();
      this.lunarix$recover(exception);
   }

   /**
    * Last safety net: {@code method_49294} resets the client back to the title screen. While a
    * connection is alive that would throw the player out of the server, so it is blocked and a
    * clean reload is started instead.
    */
   @Inject(
      method = {"method_49294"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lunarix$blockAbortToTitle(CallbackInfo ci) {
      if (!this.lunarix$isConnected()) {
         return;
      }

      LUNARIX_PACK_LOGGER.warn("Blocked vanilla resource pack recovery abort while connected");
      ci.cancel();
      this.lunarix$recover(null);
   }

   @Unique
   private boolean lunarix$isConnected() {
      class_310 client = (class_310)(Object)this;
      return client.method_1562() != null;
   }

   @Unique
   private void lunarix$recover(Throwable throwable) {
      long now = System.currentTimeMillis();
      if (lunarix$recovering || now - lunarix$lastRecovery < 4000L) {
         LUNARIX_PACK_LOGGER.warn("Resource pack reload failed again, ignoring the repeated recovery", throwable);
         return;
      }

      lunarix$recovering = true;
      lunarix$lastRecovery = now;

      try {
         class_310 client = (class_310)(Object)this;
         LUNARIX_PACK_LOGGER.warn("Resource pack reload failed while connected, dropping the broken pack and keeping the connection", throwable);

         // Drop the stuck "loading resources" overlay first, otherwise the reload request below is
         // only queued and never runs while the failed overlay is still visible.
         client.method_18502(null);

         // Drop the failed server pack completely. Vanilla does the same right before aborting to
         // the title screen, but we reload the resources afterwards instead of leaving the server.
         class_1066 serverPacks = client.method_1516();
         if (serverPacks != null) {
            serverPacks.method_55535();
         }

         class_3283 packManager = client.method_1520();
         if (packManager != null) {
            packManager.method_14447(Collections.emptyList());
            packManager.method_14445();
         }

         client.method_1521();
      } catch (Throwable error) {
         LUNARIX_PACK_LOGGER.error("Could not recover from the resource pack failure", error);
         this.lunarix$showFailToast();
      } finally {
         lunarix$recovering = false;
      }
   }

   @Unique
   private void lunarix$showFailToast() {
      try {
         class_310 client = (class_310)(Object)this;
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
}
