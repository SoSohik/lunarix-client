package ru.mentions.module.impl.player;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_332;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.other.UseCooldowns;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Use World Tracker",
   desc = "Кулдауны хилок всех игроков в мире прямо над ником",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class WorldTracker extends Module {
   private static final float FONT_SIZE = 6.0F;
   private static final float PADDING = 3.5F;
   private static final float GAP = 4.0F;
   private static final float ROW_H = 12.0F;
   public final MultiBooleanSetting items = new MultiBooleanSetting(
      this,
      "Предметы",
      new BooleanSetting(UseCooldowns.Item.NOTCH.label, true),
      new BooleanSetting(UseCooldowns.Item.GAPPLE.label, true),
      new BooleanSetting(UseCooldowns.Item.HEAL.label, true),
      new BooleanSetting(UseCooldowns.Item.NAUSEA.label, true),
      new BooleanSetting(UseCooldowns.Item.CHORUS.label, true),
      new BooleanSetting(UseCooldowns.Item.KELP.label, false),
      new BooleanSetting(UseCooldowns.Item.SCRAP.label, false)
   );
   public final BooleanSetting names = new BooleanSetting(this, "Названия", true);
   public final BooleanSetting background = new BooleanSetting(this, "Фон", true);
   public final BooleanSetting blur = new BooleanSetting(this, "Блюр фон", true).setVisible(() -> this.background.getValue());
   public final BooleanSetting debug = new BooleanSetting(this, "Логи детекта", false).onAction(() -> UseCooldowns.debug = get().debug.getValue());

   public static WorldTracker get() {
      return Instance.get(WorldTracker.class);
   }

   @EventHandler
   public void onUpdate(EventUpdate event) {
      UseCooldowns.tick(event);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      UseCooldowns.clear();
   }

   @Override
   protected void onDisable() {
      UseCooldowns.debug = false;
   }

   @Override
   protected void onEnable() {
      UseCooldowns.debug = this.debug.getValue();
   }

   public void render(class_332 context, class_1657 player, float centerX, float topY) {
      if (this.isEnabled()) {
         ArrayList<UseCooldowns.Item> active = new ArrayList<>();

         for (UseCooldowns.Item item : UseCooldowns.of(player.method_5667()).keySet()) {
            if (this.items.getValue(item.label)) {
               active.add(item);
            }
         }

         if (!active.isEmpty()) {
            Font font = Fonts.sf_regular;
            ArrayList<String> labels = new ArrayList<>();
            ArrayList<String> times = new ArrayList<>();
            float w = 3.0F;

            for (UseCooldowns.Item itemx : active) {
               String label = this.names.getValue() ? itemx.label + " " : "";
               String time = UseCooldowns.format(UseCooldowns.remaining(player.method_5667(), itemx));
               labels.add(label);
               times.add(time);
               w += font.getWidth(label, 6.0F) + font.getWidth(time, 6.0F) + 4.0F;
            }

            float bgX = centerX - w / 2.0F;
            float bgY = topY - 12.0F - 2.0F;
            if (this.background.getValue() && this.blur.getValue()) {
               RenderUtil.Blur.blur(bgX, bgY, w, 12.0F, 1.0F, 4.0F, ColorUtil.multAlpha(ColorUtil.getColor(0), 0.2F));
            } else if (this.background.getValue()) {
               RenderUtil.Render2D.rect(bgX, bgY, w, 12.0F, ColorUtil.multAlpha(ColorUtil.getColor(0), 0.4F), 4.0F);
            }

            Client.get().render2D().flushAll();
            float cx = bgX + 3.5F;
            float textY = bgY + 6.0F - 3.65F;

            for (int i = 0; i < active.size(); i++) {
               String label = labels.get(i);
               String time = times.get(i);
               if (!label.isEmpty()) {
                  font.draw(label, cx, textY, 6.0F, ColorUtil.getColor(220));
                  cx += font.getWidth(label, 6.0F);
               }

               font.draw(time, cx, textY, 6.0F, ColorUtil.client());
               cx += font.getWidth(time, 6.0F) + 4.0F;
            }
         }
      }
   }
}
