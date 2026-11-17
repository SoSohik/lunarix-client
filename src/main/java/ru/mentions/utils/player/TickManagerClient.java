package ru.mentions.utils.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public class TickManagerClient implements IMinecraft {
   public static float tick = 1.0F;
}
