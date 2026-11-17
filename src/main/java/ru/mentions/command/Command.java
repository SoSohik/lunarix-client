package ru.mentions.command;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Command {
   private final String name;
   private final String usage;
   private final String description;

   public Command(String name, String usage, String description) {
      this.name = name;
      this.usage = usage;
      this.description = description;
   }

   public String getName() {
      return this.name;
   }

   public String getUsage() {
      return this.usage;
   }

   public String getDescription() {
      return this.description;
   }

   public abstract void execute(String[] var1);

   public List<String> getSuggestions(String subPrefix) {
      return Collections.emptyList();
   }
}
