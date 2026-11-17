package ru.mentions.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleManager;

final class HudModuleOrder {
   private final List<Module> source = new ArrayList<>();
   private final List<String> names = new ArrayList<>();
   private final List<Module> ordered = new ArrayList<>();

   List<Module> get(ModuleManager manager) {
      boolean changed = false;
      int index = 0;

      for (Entry<Class<? extends Module>, Module> entry : manager.entrySet()) {
         Module module = entry.getValue();
         String name = module.getName();
         if (index == this.source.size()) {
            this.source.add(module);
            this.names.add(name);
            changed = true;
         } else if (this.source.get(index) != module || !this.names.get(index).equals(name)) {
            this.source.set(index, module);
            this.names.set(index, name);
            changed = true;
         }

         index++;
      }

      if (index < this.source.size()) {
         this.source.subList(index, this.source.size()).clear();
         this.names.subList(index, this.names.size()).clear();
         changed = true;
      }

      if (changed) {
         this.ordered.clear();
         this.ordered.addAll(this.source);
         this.ordered.sort(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
      }

      return this.ordered;
   }
}
