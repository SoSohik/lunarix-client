package ru.mentions.maintenance.state;

import ru.mentions.maintenance.DiagLog;
import ru.mentions.maintenance.ModuleState;

public final class Dormancy {
   private Dormancy() {
   }

   public static void mark() {
      ModuleState.setDormant(true);
      DiagLog.note("dormant", "configs=" + ModuleState.mixinConfigs().size());
   }
}
