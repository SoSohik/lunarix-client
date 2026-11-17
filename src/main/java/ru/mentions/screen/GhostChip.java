package ru.mentions.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.theme.Theme;
import ru.mentions.utils.animation.Animation;

@Environment(EnvType.CLIENT)
final class GhostChip {
   Theme theme;
   final Animation anim = new Animation();
}
