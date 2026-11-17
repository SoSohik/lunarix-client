package ru.mentions.module.impl.utils;

import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AutoWardenNavigator extends AutoEndNavigator {
   private static final Logger LOGGER = LoggerFactory.getLogger("mentions/AutoWardenNavigator");

   public AutoWardenNavigator(class_310 mc) {
      super(mc);
      LOGGER.debug("AutoWardenNavigator ready, baritone-free pathing backend");
   }
}
