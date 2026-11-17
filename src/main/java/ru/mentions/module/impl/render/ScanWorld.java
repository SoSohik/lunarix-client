package ru.mentions.module.impl.render;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2663;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.shader.ScanWorldRenderer;

@ModuleInfo(
   name = "Scan World",
   category = Category.RENDER,
   desc = "Сканирующая волна по миру"
)
@Environment(EnvType.CLIENT)
public class ScanWorld extends Module {
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -11477761).setVisible(() -> this.typeColor.is("Свой"));
   public BooleanSetting periodic = new BooleanSetting(this, "Периодически", true);
   public SliderSetting interval = new SliderSetting(this, "Интервал", 3.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> this.periodic.getValue());
   public BooleanSetting onTotem = new BooleanSetting(this, "При тотеме", true);
   public BooleanSetting onKill = new BooleanSetting(this, "При убийстве", true);
   public SliderSetting duration = new SliderSetting(this, "Длительность", 2.5F, 0.5F, 8.0F, 0.1F);
   public SliderSetting width = new SliderSetting(this, "Ширина", 10.0F, 1.0F, 32.0F, 1.0F);
   public SliderSetting maxRadius = new SliderSetting(this, "Радиус", 80.0F, 10.0F, 256.0F, 2.0F);
   private static final byte DEATH_STATUS = 3;
   private static final byte TOTEM_STATUS = 35;
   private static final long KILL_WINDOW_MS = 6500L;
   private long lastTriggerTime;
   private int lastTargetId = -1;
   private long lastAttackTime;

   public int getColor() {
      return this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   @Override
   protected void onEnable() {
      this.lastTargetId = -1;
      if (mc.field_1724 != null) {
         this.lastTriggerTime = System.currentTimeMillis();
         ScanWorldRenderer.getInstance().startScan(mc.field_1724.method_73189());
      }
   }

   @Override
   protected void onDisable() {
      ScanWorldRenderer.getInstance().stopScan();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      this.lastTargetId = -1;
      ScanWorldRenderer.getInstance().stopScan();
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.periodic.getValue()) {
            long now = System.currentTimeMillis();
            if (now - this.lastTriggerTime >= (long)(this.interval.getValue() * 1000.0F)) {
               class_243 center = mc.field_1724.method_73189();
               ScanWorldRenderer.getInstance().startScan(center);
               this.lastTriggerTime = now;
            }
         }
      }
   }

   @EventHandler
   public void onAttack(AttackEvent e) {
      class_1297 class_12972 = e.getTarget();
      class_1309 living;
      if (class_12972 instanceof class_1309 && (living = (class_1309)class_12972) != mc.field_1724) {
         this.lastTargetId = living.method_5628();
         this.lastAttackTime = System.currentTimeMillis();
      }
   }

   @EventHandler
   public void onPacket(EventPacket e) {
      if (e.getPacket() instanceof class_2663 packet) {
         if (mc.field_1687 != null) {
            if (packet.method_11470() == 35 && this.onTotem.getValue()) {
               class_1297 entity = packet.method_11469(mc.field_1687);
               if (entity == null) {
                  return;
               }

               ScanWorldRenderer.getInstance().startScan(entity.method_73189());
            } else if (packet.method_11470() == 3 && this.onKill.getValue()) {
               if (!(packet.method_11469(mc.field_1687) instanceof class_1309 living)) {
                  return;
               }

               if (living.method_5628() != this.lastTargetId) {
                  return;
               }

               if (System.currentTimeMillis() - this.lastAttackTime > 6500L) {
                  return;
               }

               this.lastTargetId = -1;
               ScanWorldRenderer.getInstance().startScan(living.method_73189());
            }
         }
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int col = this.getColor();
         int r = col >> 16 & 0xFF;
         int g = col >> 8 & 0xFF;
         int b = col & 0xFF;
         ScanWorldRenderer renderer = ScanWorldRenderer.getInstance();
         renderer.setDuration(this.duration.getValue());
         renderer.setWidth(this.width.getValue());
         renderer.setMaxRadius(this.maxRadius.getValue());
         renderer.setOuterColor(new Color(r, g, b, 90));
         renderer.setMidColor(new Color(r, g, b, 90));
         renderer.setInnerColor(new Color(r, g, b, 180));
         renderer.setScanlineColor(new Color(r, g, b, 90));
         renderer.render(
            e.getMatrixStack().method_23760().method_23761(), mc.field_1773.method_22973(((Integer)mc.field_1690.method_41808().method_41753()).floatValue())
         );
      }
   }
}
