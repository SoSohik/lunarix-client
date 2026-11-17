package ru.mentions.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.TriggerBot;
import ru.mentions.utils.aura.TriggerUAttack;
import ru.mentions.utils.aura.UAttack;
import ru.mentions.utils.other.Instance;

@ModuleInfo(
   name = "Sprint",
   desc = "Автоматический спринт",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class Sprint extends Module {
   public int tick;
   private long sprintBlockedUntil;

   public static Sprint get() {
      return Instance.get(Sprint.class);
   }

   public void blockSprintFor(long ms) {
      this.sprintBlockedUntil = Math.max(this.sprintBlockedUntil, System.currentTimeMillis() + Math.max(0L, ms));
      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);
      }

      if (mc.field_1690 != null) {
         mc.field_1690.field_1867.method_23481(false);
      }
   }

   public boolean isSprintBlocked() {
      return System.currentTimeMillis() < this.sprintBlockedUntil;
   }

   @EventHandler
   public void onEvent(WorldLoadEvent e) {
      this.tick += 4;
   }

   @EventHandler
   public void onEvent(EventUpdate eventUpdate) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!mc.field_1724.method_5805()) {
            this.tick += 2;
         }

         if (this.tick != 0) {
            mc.field_1724.method_5728(false);
            mc.field_1690.field_1867.method_23481(false);
            this.tick--;
         } else if (this.isSprintBlocked()) {
            mc.field_1724.method_5728(false);
            mc.field_1690.field_1867.method_23481(false);
         } else if (UAttack.resetSprintTick(AttackAura.target, AttackAura.get().getRanges())
            && AttackAura.get().isEnabled()
            && AttackAura.get().typeSprint.is("Silent")) {
            mc.field_1690.field_1867.method_23481(false);
         } else if (UAttack.resetSprintTick(AttackAura.target, AttackAura.get().getRanges())
            && AttackAura.get().isEnabled()
            && AttackAura.get().typeSprint.is("Packet")) {
            mc.field_1724.method_5728(false);
            mc.field_1690.field_1867.method_23481(false);
         } else {
            TriggerBot triggerBot = Client.get().moduleManager().get(TriggerBot.class);
            if (triggerBot != null
               && TriggerUAttack.resetSprintTick(TriggerBot.targets, triggerBot.getRanges())
               && triggerBot.isEnabled()
               && triggerBot.typeSprint.is("Silent")) {
               mc.field_1690.field_1867.method_23481(false);
            } else if (triggerBot != null
               && TriggerUAttack.resetSprintTick(TriggerBot.targets, triggerBot.getRanges())
               && triggerBot.isEnabled()
               && triggerBot.typeSprint.is("Packet")) {
               mc.field_1724.method_5728(false);
               mc.field_1690.field_1867.method_23481(false);
            } else {
               boolean horizontal = mc.field_1724.field_5976 && !mc.field_1724.field_34927;
               boolean sneaking = mc.field_1724.method_5715() && !mc.field_1724.method_5681();
               if (!horizontal && mc.field_1724.field_6250 > 0.0F) {
                  boolean var7 = true;
               } else {
                  boolean var10000 = false;
               }

               if (!sneaking) {
                  mc.field_1690.field_1867.method_23481(true);
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.sprintBlockedUntil = 0L;
      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);
      }
   }
}
