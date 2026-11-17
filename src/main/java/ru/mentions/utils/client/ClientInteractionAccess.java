package ru.mentions.utils.client;

import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_7204;
import ru.mentions.mixin.ClientPlayerInteractionManagerAccessor;

public final class ClientInteractionAccess {
   private ClientInteractionAccess() {
   }

   public static void syncSelectedSlot(class_636 manager) {
      ((ClientPlayerInteractionManagerAccessor)manager).mentions$syncSelectedSlot();
   }

   public static void sendSequencedPacket(class_636 manager, class_638 world, class_7204 creator) {
      ((ClientPlayerInteractionManagerAccessor)manager).mentions$sendSequencedPacket(world, creator);
   }
}
