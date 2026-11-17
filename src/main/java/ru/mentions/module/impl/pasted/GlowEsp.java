package ru.mentions.module.impl.pasted;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1308;
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
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.render.ShaderEspOutlinePipeline;
import ru.mentions.utils.render.ShaderEspRenderer;

@ModuleInfo(
   name = "Glow ESP",
   category = Category.PASTED,
   desc = "Яскраве неонове контурне світіння навколо гравців та сутностей"
)
@Environment(EnvType.CLIENT)
public class GlowEsp extends Module {
   private static GlowEsp instance;
   public BooleanSetting localPlayer = new BooleanSetting(this, "Локальний гравець", true);
   public BooleanSetting players = new BooleanSetting(this, "Р\u0098РіСЂРѕРєРё", true);
   public BooleanSetting ignoreNaked = new BooleanSetting(this, "Р\u0098РіРЅРѕСЂРёСЂРѕРІР°С‚СЊ РіРѕР»С‹С…", false).setVisible(() -> this.players.getValue());
   public BooleanSetting mobs = new BooleanSetting(this, "Моби", true);
   public BooleanSetting items = new BooleanSetting(this, "Предмети", false);
   public ModeSetting typeColor = new ModeSetting(this, "Режим кольору", "Тема", "Свій", "Веселка");
   public ColorSetting tintColor = new ColorSetting(this, "Колір", -11141121).setVisible(() -> this.typeColor.is("Свій"));
   public BooleanSetting friendColor = new BooleanSetting(this, "Колір друзів", true);
   public ColorSetting friendTint = new ColorSetting(this, "Колір друга", -11141291).setVisible(() -> this.friendColor.getValue());
   public SliderSetting glowRadius = new SliderSetting(this, "Радіус світіння", 0.7F, 0.1F, 1.0F, 0.05F);
   public SliderSetting glowStrength = new SliderSetting(this, "Р\u0098РЅС‚РµРЅСЃРёРІРЅРѕСЃС‚СЊ", 2.4F, 0.5F, 5.0F, 0.1F);
   public SliderSetting glowFalloff = new SliderSetting(this, "Спад світіння", 1.15F, 0.2F, 3.0F, 0.05F);
   public BooleanSetting fill = new BooleanSetting(this, "Заливка силуету", true);
   public SliderSetting fillOpacity = new SliderSetting(this, "Щільність заливки", 0.3F, 0.0F, 1.0F, 0.02F).setVisible(() -> this.fill.getValue());
   public BooleanSetting pulse = new BooleanSetting(this, "Пульсація", false);
   public SliderSetting pulseSpeed = new SliderSetting(this, "Швидкість пульсації", 1.2F, 0.2F, 4.0F, 0.1F).setVisible(() -> this.pulse.getValue());
   private final Set<class_1297> glowTargets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

   public GlowEsp() {
      instance = this;
   }

   public static GlowEsp getInstance() {
      return instance;
   }

   @Override
   protected void onDisable() {
      this.glowTargets.clear();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.glowTargets.clear();
      if (this.isEnabled()) {
         ShaderEspRenderer.getInstance().invalidate();
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      this.glowTargets.clear();
      if (this.isEnabled() && mc.field_1687 != null && mc.field_1724 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (this.shouldTarget(entity)) {
               this.glowTargets.add(entity);
            }
         }

         this.syncPipelineParams();
      }
   }

   public boolean isGlowTarget(class_1297 entity) {
      return this.isEnabled() && this.glowTargets.contains(entity);
   }

   public boolean hasTargets() {
      return !this.glowTargets.isEmpty();
   }

   public void onRender3D(class_276 outlineFbo) {
      if (this.isEnabled() && mc.field_1687 != null && mc.field_1724 != null) {
         if (!this.glowTargets.isEmpty() && outlineFbo != null && outlineFbo.method_71639() != null) {
            class_276 mainFbo = mc.method_1522();
            if (mainFbo != null) {
               this.syncPipelineParams();
               ShaderEspRenderer.getInstance().renderFromFbo(outlineFbo, mainFbo.field_1482, mainFbo.field_1481);
            }
         }
      }
   }

   public void syncPipelineParams() {
      ShaderEspOutlinePipeline.Params p = ShaderEspRenderer.getInstance().params;
      p.glowEnabled = true;
      p.glowRadius = this.glowRadius.getValue();
      p.glowStrength = this.glowStrength.getValue();
      p.glowFalloff = this.glowFalloff.getValue();
      p.outlineEnabled = true;
      p.outlineStrength = 1.8F;
      p.outlineWidth = 2;
      p.outlineMode = 0;
      p.fillEnabled = this.fill.getValue();
      p.fillOpacity = this.fill.getValue() ? this.fillOpacity.getValue() : 0.0F;
      p.opacity = 1.0F;
      p.pulseEnabled = this.pulse.getValue();
      p.pulseSpeed = this.pulseSpeed.getValue();
      p.pulseAmount = 0.45F;
      int baseColor = this.resolveColor(0);
      p.color = baseColor;
      p.friendEnabled = this.friendColor.getValue();
      p.friendColor = this.friendTint.getValue();
   }

   public int resolveColor(int index) {
      if (this.typeColor.is("Свій")) {
         return this.tintColor.getValue();
      } else {
         return this.typeColor.is("Веселка") ? ColorUtil.rainbow(3000, index * 20, 1.0F, 1.0F, 1.0F) : ColorUtil.client();
      }
   }

   private boolean shouldTarget(class_1297 entity) {
      if (!(entity instanceof class_746)) {
         if (entity instanceof class_1657 p) {
            if (p.method_5797() != null && p.method_5797().getString().startsWith("Ghost_")) {
               return false;
            } else {
               return !this.players.getValue() ? false : !this.ignoreNaked.getValue() || !this.isNaked(p);
            }
         } else if (entity instanceof class_1308) {
            return this.mobs.getValue();
         } else {
            return entity instanceof class_1542 ? this.items.getValue() : false;
         }
      } else {
         return this.localPlayer.getValue() && mc.field_1690.method_31044().method_31034();
      }
   }

   private boolean isNaked(class_1657 player) {
      return player.method_6118(class_1304.field_6169).method_7960()
         && player.method_6118(class_1304.field_6174).method_7960()
         && player.method_6118(class_1304.field_6172).method_7960()
         && player.method_6118(class_1304.field_6166).method_7960();
   }
}
