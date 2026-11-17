package ru.mentions.manager.events.orbit;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NoLambdaFactoryException extends RuntimeException {
   public NoLambdaFactoryException(Class<?> klass) {
      super("No registered lambda listener for '" + klass.getName() + "'.");
   }
}
