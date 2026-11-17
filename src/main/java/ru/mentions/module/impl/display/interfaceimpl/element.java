package ru.mentions.module.impl.display.interfaceimpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public interface element extends IMinecraft {
   void onRender(DragSetting var1, InterFace var2);
}
