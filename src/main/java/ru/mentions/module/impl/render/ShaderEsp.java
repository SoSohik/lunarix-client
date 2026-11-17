package ru.mentions.module.impl.render;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_276;
import net.minecraft.class_746;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.preview.ModulePreview;
import ru.mentions.module.api.preview.PreviewContext;
import ru.mentions.module.api.preview.PreviewSettings;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.ShaderEspOutlinePipeline;
import ru.mentions.utils.render.ShaderEspRenderer;

@ModuleInfo(
   name = "Shader ESP",
   category = Category.RENDER,
   desc = "Шейдерный ESP с ореолом свечения для сущностей"
)
@Environment(EnvType.CLIENT)
public class ShaderEsp extends Module implements ModulePreview {
   private static ShaderEsp instance;
   public ButtonSetting previewButton = PreviewSettings.button(this);
   public BooleanSetting localPlayer = new BooleanSetting(this, "Локальный игрок", true);
   public BooleanSetting players = new BooleanSetting(this, "Игроки", true);
   public BooleanSetting ignoreNaked = new BooleanSetting(this, "Игнорировать голых", false).setVisible(() -> this.players.getValue());
   public BooleanSetting mobs = new BooleanSetting(this, "Мобы", true);
   public BooleanSetting items = new BooleanSetting(this, "Предметы", false);
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   public ColorSetting tintColor = new ColorSetting(this, "Цвет", -52395).setVisible(() -> this.typeColor.is("Свой"));
   public SliderSetting saturation = new SliderSetting(this, "Насыщенность", 1.15F, 0.0F, 2.0F, 0.05F);
   public SliderSetting opacity = new SliderSetting(this, "Прозрачность", 1.0F, 0.05F, 1.0F, 0.01F);
   public BooleanSetting friendColor = new BooleanSetting(this, "Цвет друзей", true);
   public ColorSetting friendTint = new ColorSetting(this, "Цвет друга", -11141291).setVisible(() -> this.friendColor.getValue());
   public BooleanSetting glow = new BooleanSetting(this, "Свечение", true);
   public SliderSetting glowRadius = new SliderSetting(this, "Радиус свечения", 0.55F, 0.0F, 1.0F, 0.01F).setVisible(() -> this.glow.getValue());
   public SliderSetting glowStrength = new SliderSetting(this, "Сила свечения", 1.6F, 0.0F, 4.0F, 0.05F).setVisible(() -> this.glow.getValue());
   public SliderSetting glowFalloff = new SliderSetting(this, "Спад свечения", 1.1F, 0.2F, 4.0F, 0.05F).setVisible(() -> this.glow.getValue());
   public BooleanSetting fill = new BooleanSetting(this, "Заливка", true);
   public SliderSetting fillOpacity = new SliderSetting(this, "Плотность заливки", 0.45F, 0.0F, 1.5F, 0.01F).setVisible(() -> this.fill.getValue());
   public SliderSetting innerGlow = new SliderSetting(this, "Внутреннее свечение", 0.6F, 0.0F, 2.0F, 0.05F).setVisible(() -> this.fill.getValue());
   public BooleanSetting outline = new BooleanSetting(this, "Обводка", true);
   public ModeSetting outlineMode = new ModeSetting(this, "Тип обводки", "Снаружи", "Внутри", "Обе").setVisible(() -> this.outline.getValue());
   public SliderSetting outlineWidth = new SliderSetting(this, "Ширина обводки", 2.0F, 1.0F, 5.0F, 1.0F).setVisible(() -> this.outline.getValue());
   public SliderSetting outlineStrength = new SliderSetting(this, "Яркость обводки", 1.8F, 0.0F, 3.0F, 0.05F).setVisible(() -> this.outline.getValue());
   public SliderSetting outlineWhite = new SliderSetting(this, "Белизна обводки", 0.45F, 0.0F, 1.0F, 0.01F).setVisible(() -> this.outline.getValue());
   public BooleanSetting pulse = new BooleanSetting(this, "Пульсация", false);
   public SliderSetting pulseSpeed = new SliderSetting(this, "Скорость пульсации", 1.0F, 0.1F, 5.0F, 0.1F).setVisible(() -> this.pulse.getValue());
   public SliderSetting pulseAmount = new SliderSetting(this, "Сила пульсации", 0.3F, 0.0F, 1.0F, 0.05F).setVisible(() -> this.pulse.getValue());
   public BooleanSetting shimmer = new BooleanSetting(this, "Шиммер", false);
   public SliderSetting shimmerWidth = new SliderSetting(this, "Ширина шиммера", 0.04F, 0.01F, 0.15F, 0.01F).setVisible(() -> this.shimmer.getValue());
   public SliderSetting shimmerPeriod = new SliderSetting(this, "Период шиммера", 5.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> this.shimmer.getValue());
   public SliderSetting shimmerBrightness = new SliderSetting(this, "Яркость шиммера", 0.8F, 0.0F, 2.0F, 0.05F).setVisible(() -> this.shimmer.getValue());
   private final PreviewSettings previewSettings = PreviewSettings.withoutInterval(this, 4.0F, 0.0F);
   private static final Set<class_1297> allTargets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
   public class_276 customFboReference;
   private class_1297 previewDummy;

   public ShaderEsp() {
      instance = this;
   }

   public static ShaderEsp getInstance() {
      return instance;
   }

   public static boolean isEspTarget(class_1297 entity) {
      return allTargets.contains(entity);
   }

   public static Set<class_1297> getAllTargets() {
      return allTargets;
   }

   public int getColor() {
      return this.typeColor.is("Тема") ? ColorUtil.getClientColor1(1) : this.tintColor.getValue();
   }

   @Override
   protected void onEnable() {
      ShaderEspRenderer.getInstance().invalidate();
   }

   @Override
   protected void onDisable() {
      allTargets.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      allTargets.clear();
      if (this.isEnabled()) {
         ShaderEspRenderer.getInstance().invalidate();
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      allTargets.clear();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (this.shouldTarget(entity)) {
               allTargets.add(entity);
            }
         }

         if (this.previewDummy != null && !this.previewDummy.method_31481()) {
            allTargets.add(this.previewDummy);
         }
      }
   }

   @Override
   public PreviewSettings previewSettings() {
      return this.previewSettings;
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   @Override
   public void previewStart(PreviewContext ctx) {
      this.previewDummy = ctx.dummy();
   }

   @Override
   public void previewTick(PreviewContext ctx) {
      this.previewDummy = ctx.dummy();
   }

   @Override
   public void previewSpawn(PreviewContext ctx) {
   }

   @Override
   public void previewStop() {
      this.previewDummy = null;
      allTargets.clear();
   }

   public void onRender3D() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (!allTargets.isEmpty()) {
            class_276 fb = mc.method_1522();
            if (fb != null) {
               class_276 outlineFbo = this.customFboReference;
               if (outlineFbo != null && outlineFbo.method_71639() != null) {
                  ShaderEspRenderer renderer = ShaderEspRenderer.getInstance();
                  this.applySettings(renderer.params);
                  renderer.renderFromFbo(outlineFbo, fb.field_1482, fb.field_1481);
               }
            }
         }
      }
   }

   private void applySettings(ShaderEspOutlinePipeline.Params p) {
      p.color = this.getColor();
      p.friendEnabled = this.friendColor.getValue();
      p.friendColor = this.friendTint.getValue();
      p.opacity = this.opacity.getValue();
      p.saturation = this.saturation.getValue();
      p.glowEnabled = this.glow.getValue();
      p.glowRadius = this.glowRadius.getValue();
      p.glowStrength = this.glowStrength.getValue();
      p.glowFalloff = this.glowFalloff.getValue();
      p.fillEnabled = this.fill.getValue();
      p.fillOpacity = this.fillOpacity.getValue();
      p.innerGlow = this.innerGlow.getValue();
      p.outlineEnabled = this.outline.getValue();
      p.outlineMode = this.outlineMode.getIndex();
      p.outlineWidth = this.outlineWidth.getValue().intValue();
      p.outlineStrength = this.outlineStrength.getValue();
      p.outlineWhite = this.outlineWhite.getValue();
      p.pulseEnabled = this.pulse.getValue();
      p.pulseSpeed = this.pulseSpeed.getValue();
      p.pulseAmount = this.pulseAmount.getValue();
      p.shimmerEnabled = this.shimmer.getValue();
      p.shimmerWidth = this.shimmerWidth.getValue();
      p.shimmerPeriodSec = this.shimmerPeriod.getValue();
      p.shimmerBrightness = this.shimmerBrightness.getValue();
   }

   private boolean shouldTarget(class_1297 entity) {
      if (!(entity instanceof class_1657 p)) {
         if (entity instanceof class_746) {
            return this.localPlayer.getValue();
         } else if (entity instanceof class_1309) {
            return this.mobs.getValue();
         } else {
            return entity instanceof class_1542 ? this.items.getValue() : false;
         }
      } else if (p.method_5797() != null && p.method_5797().getString().startsWith("Ghost_")) {
         return false;
      } else {
         return !this.players.getValue() ? false : !this.ignoreNaked.getValue() || hasArmor(p);
      }
   }

   private static boolean hasArmor(class_1657 p) {
      return !p.method_6118(class_1304.field_6169).method_7960()
         || !p.method_6118(class_1304.field_6174).method_7960()
         || !p.method_6118(class_1304.field_6172).method_7960()
         || !p.method_6118(class_1304.field_6166).method_7960();
   }
}
