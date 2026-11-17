package ru.mentions.module.impl.misc;

import java.util.function.BinaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_4061;
import net.minecraft.class_4063;
import net.minecraft.class_4066;
import net.minecraft.class_7172;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Optimization",
   category = Category.OTHER,
   autoEnabled = true,
   desc = "Облегчает HUD; опционально ограничивает настройки рендера Minecraft с восстановлением"
)
@Environment(EnvType.CLIENT)
public final class Optimization extends Module {
   private static Optimization instance;
   private static boolean lifecycleRegistered;
   private boolean stopping;
   private Optimization.MinecraftCaps minecraftCaps;
   public final BooleanSetting flatPanels = new BooleanSetting(this, "Простые фоны вместо blur", true);
   public final BooleanSetting disableGlow = new BooleanSetting(this, "Без свечения интерфейса", true);
   public final BooleanSetting simpleGlass = new BooleanSetting(this, "Без искажения стекла", true).setVisible(() -> !this.flatPanels.getValue());
   public final BooleanSetting reuseBlur = new BooleanSetting(this, "Один blur на кадр HUD", true).setVisible(() -> !this.flatPanels.getValue());
   public final SliderSetting blurPasses = new SliderSetting(this, "Проходы blur", 1.0F, 1.0F, 3.0F, 1.0F).setVisible(() -> !this.flatPanels.getValue());
   public final BooleanSetting disableMenuParticles = new BooleanSetting(this, "Без партиклів у меню", true);
   public final BooleanSetting disableScanLines = new BooleanSetting(this, "Без scan-lines у меню", true);
   public final BooleanSetting limitNameTags = new BooleanSetting(this, "Ограничить дальность неймтегов", true);
   public final SliderSetting nameTagRange = new SliderSetting(this, "Дальность неймтегов", 128.0F, 16.0F, 256.0F, 8.0F)
      .setVisible(() -> this.limitNameTags.getValue());
   public final BooleanSetting minecraftOptimization = new BooleanSetting(this, "Оптимизация Minecraft", false);
   public final BooleanSetting limitRenderDistance = new BooleanSetting(this, "Ограничить чанки", true).setVisible(() -> this.minecraftOptimization.getValue());
   public final SliderSetting renderDistanceCap = new SliderSetting(this, "Максимум чанков", 12.0F, 2.0F, 32.0F, 1.0F)
      .setVisible(() -> this.minecraftOptimization.getValue() && this.limitRenderDistance.getValue());
   public final BooleanSetting limitEntityDistance = new BooleanSetting(this, "Ограничить дальность сущностей", true)
      .setVisible(() -> this.minecraftOptimization.getValue());
   public final SliderSetting entityDistanceCap = new SliderSetting(this, "Дальность сущностей (%)", 100.0F, 50.0F, 200.0F, 25.0F)
      .setVisible(() -> this.minecraftOptimization.getValue() && this.limitEntityDistance.getValue());
   public final BooleanSetting limitParticles = new BooleanSetting(this, "Ограничить частицы", true).setVisible(() -> this.minecraftOptimization.getValue());
   public final SliderSetting particleCap = new SliderSetting(this, "Частицы: 1 меньше, 2 минимум", 1.0F, 1.0F, 2.0F, 1.0F)
      .setVisible(() -> this.minecraftOptimization.getValue() && this.limitParticles.getValue());
   public final BooleanSetting disableEntityShadows = new BooleanSetting(this, "Без теней сущностей", true)
      .setVisible(() -> this.minecraftOptimization.getValue());
   public final BooleanSetting limitClouds = new BooleanSetting(this, "Ограничить облака", true).setVisible(() -> this.minecraftOptimization.getValue());
   public final SliderSetting cloudCap = new SliderSetting(this, "Облака: 0 выкл, 1 быстро", 1.0F, 0.0F, 1.0F, 1.0F)
      .setVisible(() -> this.minecraftOptimization.getValue() && this.limitClouds.getValue());
   public final BooleanSetting frameSync = new BooleanSetting(this, "FrameSync: лимит FPS", false).setVisible(() -> this.minecraftOptimization.getValue());
   public final SliderSetting fpsCap = new SliderSetting(this, "Лимит FPS", 60.0F, 20.0F, 240.0F, 5.0F)
      .setVisible(() -> this.minecraftOptimization.getValue() && this.frameSync.getValue());
   public final BooleanSetting forceVsync = new BooleanSetting(this, "FrameSync: включить VSync", false)
      .setVisible(() -> this.minecraftOptimization.getValue() && this.frameSync.getValue());
   public static final String SOUND_OFF = "Выключено";
   public static final String SOUND_REDUCE = "Приглушение";
   public static final String SOUND_MUTE = "Отключение";
   public final BooleanSetting soundReducer = new BooleanSetting(this, "Приглушение звуков", false).setVisible(() -> this.minecraftOptimization.getValue());
   public final ModeSetting soundMode = new ModeSetting(this, "Режим звука", "Приглушение", "Выключено", "Приглушение", "Отключение")
      .setVisible(() -> this.minecraftOptimization.getValue() && this.soundReducer.getValue());
   public final SliderSetting soundVolume = new SliderSetting(this, "Громкость", 0.5F, 0.0F, 1.0F, 0.05F)
      .setVisible(() -> this.minecraftOptimization.getValue() && this.soundReducer.getValue() && !this.soundMode.is("Отключение"));
   public final MultiBooleanSetting soundGroups = new MultiBooleanSetting(
         this,
         "Группы звуков",
         BooleanSetting.of("splash_potion", true),
         BooleanSetting.of("experience_orb", true),
         BooleanSetting.of("lingering_potion", true),
         BooleanSetting.of("creeper", true),
         BooleanSetting.of("tnt", true),
         BooleanSetting.of("anvil", true),
         BooleanSetting.of("bell", true),
         BooleanSetting.of("arrow", true),
         BooleanSetting.of("ghast", true),
         BooleanSetting.of("portal", true),
         BooleanSetting.of("totem", true),
         BooleanSetting.of("warden", true),
         BooleanSetting.of("wither", true),
         BooleanSetting.of("phantom", true),
         BooleanSetting.of("trident", true),
         BooleanSetting.of("skeleton", true),
         BooleanSetting.of("sculk", true)
      )
      .setVisible(() -> this.minecraftOptimization.getValue() && this.soundReducer.getValue() && !this.soundMode.is("Выключено"));
   public final BooleanSetting disableBobView = new BooleanSetting(this, "Без покачивания камеры", false)
      .setVisible(() -> this.minecraftOptimization.getValue());
   public final BooleanSetting disableHurtCam = new BooleanSetting(this, "Без тряски от урона", false).setVisible(() -> this.minecraftOptimization.getValue());
   public final BooleanSetting disableVignette = new BooleanSetting(this, "Без виньетки", false).setVisible(() -> this.minecraftOptimization.getValue());
   public final BooleanSetting disableAttackIndicator = new BooleanSetting(this, "Без индикатора атаки", false)
      .setVisible(() -> this.minecraftOptimization.getValue());

   public Optimization() {
      if (instance != null) {
         instance.restoreMinecraft();
      }

      instance = this;
      if (!lifecycleRegistered) {
         lifecycleRegistered = true;
         ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
            if (instance != null) {
               instance.pollMinecraft(client);
            }
         });
         ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
            if (instance != null) {
               instance.stopping = true;
               instance.restoreMinecraft();
            }
         });
      }
   }

   @Override
   public void setEnabled(boolean enabled, boolean notification) {
      try {
         super.setEnabled(enabled, notification);
      } finally {
         if (!this.isEnabled()) {
            this.restoreMinecraft();
         }
      }
   }

   private void pollMinecraft(class_310 client) {
      if (!this.stopping
         && this.isEnabled()
         && this.minecraftOptimization.getValue()
         && client.field_1687 != null
         && client.field_1724 != null
         && client.field_1690 != null) {
         if (this.minecraftCaps == null || this.minecraftCaps.options != client.field_1690) {
            this.restoreMinecraft();
            this.minecraftCaps = new Optimization.MinecraftCaps(client.field_1690);
         }

         this.minecraftCaps.renderDistance.update(this.limitRenderDistance.getValue(), this.renderDistanceCap.getValue().intValue());
         this.minecraftCaps.entityDistance.update(this.limitEntityDistance.getValue(), this.entityDistanceCap.getValue().doubleValue() / 100.0);
         this.minecraftCaps
            .particles
            .update(this.limitParticles.getValue(), this.particleCap.getValue() >= 2.0F ? class_4066.field_18199 : class_4066.field_18198);
         this.minecraftCaps.shadows.update(this.disableEntityShadows.getValue(), false);
         this.minecraftCaps.clouds.update(this.limitClouds.getValue(), this.cloudCap.getValue() < 1.0F ? class_4063.field_18162 : class_4063.field_18163);
         this.minecraftCaps.maxFps.update(this.frameSync.getValue(), this.fpsCap.getValue().intValue());
         this.minecraftCaps.vsync.update(this.forceVsync.getValue() && this.frameSync.getValue(), true);
         this.minecraftCaps.bobView.update(this.disableBobView.getValue(), false);
         this.minecraftCaps.hurtCam.update(this.disableHurtCam.getValue(), 0.0);
         this.minecraftCaps.vignette.update(this.disableVignette.getValue(), false);
         this.minecraftCaps.attackIndicator.update(this.disableAttackIndicator.getValue(), class_4061.field_18151);
      } else {
         this.restoreMinecraft();
      }
   }

   private void restoreMinecraft() {
      if (this.minecraftCaps != null) {
         this.minecraftCaps.restore();
      }
   }

   private static boolean active() {
      return instance != null && instance.isEnabled();
   }

   public static boolean flatPanels() {
      return active() && instance.flatPanels.getValue();
   }

   public static boolean skipGlow() {
      return active() && instance.disableGlow.getValue();
   }

   public static boolean simpleGlass() {
      return active() && instance.simpleGlass.getValue();
   }

   public static boolean reuseBlur() {
      return active() && instance.reuseBlur.getValue();
   }

   public static boolean menuParticles() {
      return !active() || !instance.disableMenuParticles.getValue();
   }

   public static boolean menuScanLines() {
      return !active() || !instance.disableScanLines.getValue();
   }

   public static int blurPasses(int requested) {
      return active() ? Math.min(requested, instance.blurPasses.getValue().intValue()) : requested;
   }

   public static double nameTagRangeSquared() {
      if (active() && instance.limitNameTags.getValue()) {
         double range = instance.nameTagRange.getValue().doubleValue();
         return range * range;
      } else {
         return Double.POSITIVE_INFINITY;
      }
   }

   public static float soundVolumeFactor(String soundId) {
      Optimization mod = instance;
      if (mod == null || !mod.isEnabled() || !mod.minecraftOptimization.getValue() || !mod.soundReducer.getValue() || soundId == null || soundId.isEmpty()) {
         return 1.0F;
      } else if (mod.soundMode.is("Выключено")) {
         return 1.0F;
      } else {
         boolean matched = false;

         for (BooleanSetting group : mod.soundGroups.getValues()) {
            if (group.getValue()) {
               String key = group.getName().toLowerCase();
               if ("bell".equals(key)) {
                  if (soundId.contains("bell")) {
                     matched = true;
                     break;
                  }
               } else if (soundId.contains(key)) {
                  matched = true;
                  break;
               }
            }
         }

         if (!matched) {
            return 1.0F;
         } else {
            return mod.soundMode.is("Отключение") ? 0.0F : mod.soundVolume.getValue();
         }
      }
   }

   private static final class MinecraftCaps {
      private final class_315 options;
      private final RenderOptionOverride<Integer> renderDistance;
      private final RenderOptionOverride<Double> entityDistance;
      private final RenderOptionOverride<class_4066> particles;
      private final RenderOptionOverride<Boolean> shadows;
      private final RenderOptionOverride<class_4063> clouds;
      private final RenderOptionOverride<Integer> maxFps;
      private final RenderOptionOverride<Boolean> vsync;
      private final RenderOptionOverride<Boolean> bobView;
      private final RenderOptionOverride<Double> hurtCam;
      private final RenderOptionOverride<Boolean> vignette;
      private final RenderOptionOverride<class_4061> attackIndicator;

      private MinecraftCaps(class_315 options) {
         this.options = options;
         this.renderDistance = bind(options.method_42503(), Math::min);
         this.entityDistance = bind(options.method_42517(), Math::min);
         this.particles = bind(options.method_42475(), (value, cap) -> value.ordinal() >= cap.ordinal() ? value : cap);
         this.shadows = bind(options.method_42435(), (value, cap) -> value && cap);
         this.clouds = bind(options.method_42528(), (value, cap) -> value.ordinal() <= cap.ordinal() ? value : cap);
         this.maxFps = bind(options.method_42524(), Math::min);
         this.vsync = bind(options.method_42433(), (value, cap) -> value || cap);
         this.bobView = bind(options.method_42448(), (value, cap) -> value && cap);
         this.hurtCam = bind(options.method_48974(), Math::min);
         this.vignette = bind(options.method_75335(), (value, cap) -> value && cap);
         this.attackIndicator = bind(options.method_42565(), (value, cap) -> value.ordinal() >= cap.ordinal() ? value : cap);
      }

      private static <T> RenderOptionOverride<T> bind(class_7172<T> option, BinaryOperator<T> reduce) {
         return new RenderOptionOverride<>(option::method_41753, option::method_41748, reduce);
      }

      private void restore() {
         this.renderDistance.restore();
         this.entityDistance.restore();
         this.particles.restore();
         this.shadows.restore();
         this.clouds.restore();
      }
   }
}
