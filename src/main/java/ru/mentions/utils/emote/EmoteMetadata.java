package ru.mentions.utils.emote;

import com.zigythebird.playeranimcore.animation.Animation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(EnvType.CLIENT)
public record EmoteMetadata(String id, String displayName, class_2960 icon, Animation animation) {
}
