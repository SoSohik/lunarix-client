package ru.mentions.screen;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_9779;
import ru.mentions.Client;
import ru.mentions.lang.Lang;
import ru.mentions.manager.Theme;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.module.impl.display.ClickGui;
import ru.mentions.module.impl.display.ClientTheme;
import ru.mentions.module.impl.misc.Optimization;
import ru.mentions.screen.editor.OverlayEditor;
import ru.mentions.screen.editor.OverlayEditors;
import ru.mentions.utils.RussianUi;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.animation.satoshi.Direction;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.Keyboard;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.other.GuiMusicPlayer;
import ru.mentions.utils.other.GuiSounds;
import ru.mentions.utils.other.NotificationSound;
import ru.mentions.utils.render.ClickGuiDotsPipeline;
import ru.mentions.utils.render.FrameTextures;
import ru.mentions.utils.render.GrayscalePipeline;
import ru.mentions.utils.render.HalftoneDotsPipeline;
import ru.mentions.utils.render.IconSplashEffect;
import ru.mentions.utils.render.MenuRaysPipeline;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScanLinesPipeline;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class Menu extends class_437 implements IMinecraft, ClickGuiScreen {
   public static float S = 1.0F;
   public static float extraW = 0.0F;
   public static float extraH = 0.0F;
   public static float setExtraW = 0.0F;
   public static float setExtraH = 0.0F;
   public static float guiOffsetX = 0.0F;
   public static float guiOffsetY = 0.0F;
   private boolean draggingMain = false;
   private float dragStartX = 0.0F;
   private float dragStartY = 0.0F;
   private boolean resizingMain = false;
   private boolean resizingSet = false;
   private float resizeStartX = 0.0F;
   private float resizeStartY = 0.0F;
   private float resizeStartExtraW = 0.0F;
   private float resizeStartExtraH = 0.0F;
   private float mainGripX;
   private float mainGripY;
   private float resizeBlurFade = 0.0F;
   private float setResizeBlurFade = 0.0F;
   private long lastFrameNano = 0L;
   private float lastDeltaTime = 0.016F;
   boolean exit = false;
   public Animation glomalAnim = new Animation();
   private final Animation tgHover = new Animation();
   private float tgBtnX;
   private float tgBtnY;
   private float tgBtnR;
   private final Animation cosmeticsHover = new Animation();
   private float cosmeticsBtnX;
   private float cosmeticsBtnY;
   private float cosmeticsBtnR;
   private final HandsEditor handsEditor = HandsEditor.getInstance();
   private float scaleFix = 1.0F;
   private float popClipX;
   private float popClipY;
   private float popClipW;
   private float popClipH;
   public final ru.mentions.utils.animation.satoshi.Animation animConfigsActive = new EaseInOutQuad(300, 1.0);
   public final ru.mentions.utils.animation.satoshi.Animation animConfigsHover = new EaseInOutQuad(300, 1.0);
   public final ru.mentions.utils.animation.satoshi.Animation animFriendsActive = new EaseInOutQuad(300, 1.0);
   public final ru.mentions.utils.animation.satoshi.Animation animFriendsHover = new EaseInOutQuad(300, 1.0);
   public final ru.mentions.utils.animation.satoshi.Animation animGpsActive = new EaseInOutQuad(300, 1.0);
   public final ru.mentions.utils.animation.satoshi.Animation animGpsHover = new EaseInOutQuad(300, 1.0);
   public final ru.mentions.utils.animation.satoshi.Animation animEventsActive = new EaseInOutQuad(300, 1.0);
   public final ru.mentions.utils.animation.satoshi.Animation animEventsHover = new EaseInOutQuad(300, 1.0);
   double lastMouseX;
   double lastMouseY;
   public static Theme selectedTheme;
   public static Theme preSelectedTheme;
   public static Theme[] themes;
   public static ru.mentions.utils.animation.satoshi.Animation animation14 = new EaseInOutQuad(300, 1.0);
   public static ru.mentions.utils.animation.satoshi.Animation animCategoryReset = new EaseInOutQuad(300, 1.0);
   public static ru.mentions.utils.animation.satoshi.Animation animUserInfo = new EaseInOutQuad(300, 1.0);
   public static ru.mentions.utils.animation.satoshi.Animation animation1 = new EaseInOutQuad(300, 1.0);
   public static ru.mentions.utils.animation.satoshi.Animation animation2 = new EaseInOutQuad(300, 1.0);
   public static ru.mentions.utils.animation.satoshi.Animation animation3 = new EaseInOutQuad(300, 1.0);
   public static ru.mentions.utils.animation.satoshi.Animation animation4 = new EaseInOutQuad(300, 1.0);
   public Module select = null;
   public Category active = Category.COMBAT;
   private final ManagementPanel management = new ManagementPanel();
   private int catDir = 0;
   private float scrollTarget = 0.0F;
   private float scrollAnim = 0.0F;
   private float scrollVel = 0.0F;
   private float maxScroll = 0.0F;
   private float settingScrollTarget = 0.0F;
   private float settingScrollAnim = 0.0F;
   private float settingScrollVel = 0.0F;
   private float settingMaxScroll = 0.0F;
   private SliderSetting draggingSlider = null;
   private BindSetting activeBind = null;
   private Module bindingModule = null;
   private StringSetting activeString = null;
   private String stringBuffer = "";
   private ColorSetting draggingColor = null;
   private int draggingColorBar = 0;
   public static boolean searchActive = false;
   private static String searchQuery = "";
   private long searchTypeTime = System.currentTimeMillis();
   private int logoClicks = 0;
   private long lastLogoClickMs = 0L;
   private final ru.mentions.utils.animation.satoshi.Animation animSearchFocus = new EaseInOutQuad(300, 1.0, Direction.BACKWARDS);
   private final ru.mentions.utils.animation.satoshi.Animation animSearchText = new EaseInOutQuad(300, 1.0, Direction.BACKWARDS);
   private final ru.mentions.utils.animation.satoshi.Animation animSearchEmpty = new EaseInOutQuad(300, 1.0, Direction.BACKWARDS);
   private static final int SEARCH_LIMIT = 24;
   public int pastedSubTab = 0;
   public static final String[] PASTED_SUBTABS = new String[]{"Усі", "System DLC", "WexSide", "Wild"};
   private final ru.mentions.utils.animation.satoshi.Animation[] pastedTabAnims = new ru.mentions.utils.animation.satoshi.Animation[]{
      new EaseInOutQuad(250, 1.0), new EaseInOutQuad(250, 1.0), new EaseInOutQuad(250, 1.0), new EaseInOutQuad(250, 1.0)
   };
   private final HashMap<String, ru.mentions.utils.animation.satoshi.Animation> chipAnims = new HashMap<>();
   private final GrayscalePipeline grayscalePipeline = new GrayscalePipeline();
   private final ClickGuiDotsPipeline dotsPipeline = new ClickGuiDotsPipeline();
   private final ScanLinesPipeline scanLinesPipeline = new ScanLinesPipeline();
   private final MenuRaysPipeline raysPipeline = new MenuRaysPipeline();
   private final HalftoneDotsPipeline halftonePipeline = new HalftoneDotsPipeline();
   private final List<Menu.GuiParticle> particles = new ArrayList<>();
   private long lastParticle;
   private long particleDelay = 90L;
   private final HashMap<String, Float> descHeights = new HashMap<>();
   private final HashMap<String, float[]> smoothVals = new HashMap<>();
   private final IdentityHashMap<Setting<?>, ru.mentions.utils.animation.satoshi.Animation> visAnims = new IdentityHashMap<>();
   private final IdentityHashMap<Setting<?>, float[]> chipsHeights = new IdentityHashMap<>();

   public Menu() {
      super(class_2561.method_43470("mentioons"));
   }

   private ru.mentions.utils.animation.satoshi.Animation chipAnim(String key) {
      return this.chipAnims.computeIfAbsent(key, k -> new EaseInOutQuad(300, 1.0));
   }

   private boolean effect(String name) {
      return name.equals("Размывать фон") || name.equals("Затемнять фон");
   }

   private void spawnParticle(int screenWidth, int screenHeight) {
      if (!Optimization.menuParticles()) {
         if (!this.particles.isEmpty()) {
            this.particles.clear();
         }
      } else if (System.currentTimeMillis() - this.lastParticle >= this.particleDelay) {
         this.lastParticle = System.currentTimeMillis();
         this.particleDelay = 70L + (long)(Math.random() * 90.0);
         if (this.particles.size() <= 60) {
            this.particles.add(new Menu.GuiParticle((float)(Math.random() * (double)screenWidth), (float)(Math.random() * (double)screenHeight)));
         }
      }
   }

   private static float smoothstep(float edge0, float edge1, float value) {
      float t = class_3532.method_15363((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private void renderParticles(float globalAnim) {
      for (int i = this.particles.size() - 1; i >= 0; i--) {
         Menu.GuiParticle p = this.particles.get(i);
         float t = p.progress();
         if (t >= 1.0F) {
            this.particles.remove(i);
         } else {
            float grow = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
            float radius = p.maxRadius * grow * S;
            float alpha;
            if (!(radius < 0.4F) && !((alpha = smoothstep(0.0F, 0.18F, t) * (1.0F - smoothstep(0.45F, 1.0F, t)) * globalAnim * 0.75F) < 0.004F)) {
               float thickness = 1.1F + 2.6F * (1.0F - grow);
               float py = p.y - p.drift * grow * S;
               RenderUtil.Render2D.outline(
                  p.x - radius, py - radius, radius * 2.0F, radius * 2.0F, thickness, ColorUtil.replAlpha(ColorUtil.client(), alpha), radius
               );
            }
         }
      }
   }

   private void drawScanLines(int screenWidth, int screenHeight, float globalAnim) {
      if (!(globalAnim <= 0.01F) && Optimization.menuScanLines()) {
         this.scanLinesPipeline
            .draw((float)screenWidth, (float)screenHeight, globalAnim * 0.18F, ColorUtil.getColor(255), 4.0F * S, 1.0F, 50.0F, 3000.0F, 100.0F);
      }
   }

   private float descHeight(Font font, String desc) {
      return this.descHeight(font, desc, 140.0F * S);
   }

   private float descHeight(Font font, String desc, float width) {
      return this.descHeights.computeIfAbsent(desc + "_" + S + "_" + (int)width, d -> font.getWrappedHeight(desc, width, 6.5F * S));
   }

   private float smooth(String key, float target) {
      float[] v = this.smoothVals.computeIfAbsent(key, k -> new float[]{target});
      float factor = 1.0F - (float)Math.exp((double)(-16.0F * this.lastDeltaTime));
      v[0] += (target - v[0]) * factor;
      return v[0];
   }

   private float visAnim(Module f, Setting<?> setting) {
      ru.mentions.utils.animation.satoshi.Animation a = this.visAnims.computeIfAbsent(setting, k -> new EaseInOutQuad(300, 1.0));
      a.setDirection(setting.getVisible().get() ? Direction.FORWARDS : Direction.BACKWARDS);
      return a.getOutput();
   }

   private float chipsHeight(Iterable<String> values, float width) {
      Font font = Fonts.sf_medium;
      float px = 0.0F;
      float py = 0.0F;

      for (String val : values) {
         float tw = font.getWidth(RussianUi.text(val), 6.0F * S) + 8.0F * S;
         if (px + tw > width && px > 0.0F) {
            px = 0.0F;
            py += 12.0F * S;
         }

         px += tw + 3.0F * S;
      }

      return py + 10.0F * S;
   }

   private float chipsHeight(Setting<?> key, Iterable<String> values, float width) {
      float[] cached = this.chipsHeights.get(key);
      if (cached != null && cached[0] == width) {
         return cached[1];
      } else {
         float height = this.chipsHeight(values, width);
         this.chipsHeights.put(key, new float[]{width, height});
         return height;
      }
   }

   private float modeChipsHeight(ModeSetting s, float width) {
      return this.chipsHeight(s, s.values, width);
   }

   private float multiChipsHeight(MultiBooleanSetting s, float width) {
      float[] cached = this.chipsHeights.get(s);
      return cached != null && cached[0] == width ? cached[1] : this.chipsHeight(s, this.multiNames(s), width);
   }

   private String query() {
      return searchQuery.trim().toLowerCase(Locale.ROOT);
   }

   private boolean searching() {
      return !this.query().isEmpty();
   }

   private boolean moduleVisible(Module f) {
      String q = this.query();
      if (q.isEmpty()) {
         if (f.getCategory() != this.active) {
            return false;
         } else {
            if (this.active == Category.PASTED) {
               if (this.pastedSubTab == 1 && !f.getDesc().toLowerCase(Locale.ROOT).contains("systemdlc")) {
                  return false;
               }

               if (this.pastedSubTab == 2 && !f.getDesc().toLowerCase(Locale.ROOT).contains("wexside")) {
                  return false;
               }

               if (this.pastedSubTab == 3 && !f.getDesc().toLowerCase(Locale.ROOT).contains("wild")) {
                  return false;
               }
            }

            return true;
         }
      } else if (!f.getName().toLowerCase(Locale.ROOT).contains(q)
         && !f.getBigName().toLowerCase(Locale.ROOT).contains(q)
         && !f.getDesc().toLowerCase(Locale.ROOT).contains(q)
         && !f.getCategory().getName().toLowerCase(Locale.ROOT).contains(q)) {
         for (Setting<?> setting : f.getSettings()) {
            if (setting.getName().toLowerCase(Locale.ROOT).contains(q) || setting.getDisplayName().toLowerCase(Locale.ROOT).contains(q)) {
               return true;
            }

            if (setting instanceof MultiBooleanSetting multi) {
               for (BooleanSetting option : multi.getValues()) {
                  if (option.getName().toLowerCase(Locale.ROOT).contains(q) || option.getDisplayName().toLowerCase(Locale.ROOT).contains(q)) {
                     return true;
                  }
               }
            }

            if (setting instanceof ModeSetting mode) {
               for (String value : mode.values) {
                  if (value.toLowerCase(Locale.ROOT).contains(q) || RussianUi.text(value).toLowerCase(Locale.ROOT).contains(q)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private int searchResults() {
      int count = 0;

      for (Module f : Client.get().moduleManager().values()) {
         if (this.moduleVisible(f)) {
            count++;
         }
      }

      return count;
   }

   private void searchChanged() {
      this.searchTypeTime = System.currentTimeMillis();
      this.scrollTarget = 0.0F;
   }

   private void clearSearch() {
      if (!searchQuery.isEmpty()) {
         searchQuery = "";
         this.searchChanged();
      }
   }

   private List<String> multiNames(MultiBooleanSetting s) {
      ArrayList<String> names = new ArrayList<>();

      for (BooleanSetting b : s.getValues()) {
         names.add(b.getDisplayName());
      }

      return names;
   }

   protected void method_25426() {
      this.exit = false;
      searchActive = false;
      this.searchTypeTime = System.currentTimeMillis();
      this.bindingModule = null;
      this.activeBind = null;
      this.activeString = null;
      this.draggingSlider = null;
      this.draggingColor = null;
      this.draggingMain = false;
      this.management.onOpened();
      this.resizingMain = false;
      this.resizingSet = false;
      this.lastFrameNano = 0L;
      this.lastDeltaTime = 0.016F;
      GuiSounds.open();
      GuiMusicPlayer.start(0.15F);
      this.glomalAnim.run(1.0, 0.34, Easings.EXPO_OUT);
      selectedTheme = Client.get().guiManager().getCurrentTheme();
      preSelectedTheme = Client.get().guiManager().getCurrentTheme();
      themes = Theme.values();
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         this.closeCheck();
         this.glomalAnim.update();
         mouseX = (int)((float)mouseX / this.scaleFix);
         mouseY = (int)((float)mouseY / this.scaleFix);
         this.lastMouseX = (double)mouseX;
         this.lastMouseY = (double)mouseY;
      }
   }

   @Override
   public void renderOverlay(class_332 context, class_9779 tickCounter) {
      if (!DualWindowService.isCleanPass()) {
         long nowNano = System.nanoTime();
         this.lastDeltaTime = this.lastFrameNano == 0L ? 0.016F : Math.min((float)(nowNano - this.lastFrameNano) / 1.0E9F, 0.08F);
         this.lastFrameNano = nowNano;
         float dt = this.lastDeltaTime;
         if (this.resizingMain) {
            this.resizeBlurFade = Math.min(1.0F, this.resizeBlurFade + dt * 6.0F);
         } else if (!(extraW > 1.0F) && !(extraH > 1.0F)) {
            this.resizeBlurFade = Math.max(0.0F, this.resizeBlurFade - dt * 3.0F);
         } else {
            this.resizeBlurFade = Math.max(0.42F, this.resizeBlurFade - dt * 1.6F);
         }

         if (this.resizingSet) {
            this.setResizeBlurFade = Math.min(1.0F, this.setResizeBlurFade + dt * 6.0F);
         } else if (!(setExtraW > 1.0F) && !(setExtraH > 1.0F)) {
            this.setResizeBlurFade = Math.max(0.0F, this.setResizeBlurFade - dt * 3.0F);
         } else {
            this.setResizeBlurFade = Math.max(0.42F, this.setResizeBlurFade - dt * 1.6F);
         }

         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         this.scaleFix = targetScale / currentScale;
         int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
         int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
         this.glomalAnim.update();
         float globalAnim = this.glomalAnim.get();
         FrameTextures.ensure();
         if (context != null) {
            context.method_51448().pushMatrix();
         }

         this.management.beginNavigation();
         OverlayEditor editor;
         if ((editor = OverlayEditors.active()) != null) {
            float editorMouseX = (float)(mc.field_1729.method_68879(mc.method_22683()) / (double)this.scaleFix);
            float editorMouseY = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.scaleFix);
            editor.render((float)screenWidth, (float)screenHeight, editorMouseX, editorMouseY, globalAnim);
            Render2D.endOverlay();
            if (context != null) {
               context.method_51448().popMatrix();
            }
         } else {
            if (this.effect("Серый фон")) {
               this.grayscalePipeline.draw(globalAnim);
            }

            ScreenBlur.capture(1);
            if (this.effect("Размывать фон")) {
               RenderUtil.Blur.blur(
                  0.0F, 0.0F, (float)screenWidth, (float)screenHeight, globalAnim, 0.0F, ColorUtil.multAlpha(ColorUtil.client(), 0.18F * globalAnim)
               );
            }

            if (this.effect("Затемнять фон")) {
               RenderUtil.Images.texture(
                  class_2960.method_60655("client", "textures/frame/background.png"),
                  0.0F,
                  0.0F,
                  (float)screenWidth,
                  (float)screenHeight,
                  ColorUtil.multAlpha(ColorUtil.client(), globalAnim)
               );
            }

            ScreenBlur.capture();
            SliderSetting sizeSlider = Client.get().moduleManager().get(ClickGui.class).size;
            if (this.draggingSlider != sizeSlider) {
               S = sizeSlider.getValue();
            }

            float baseW = 380.0F * S;
            float baseH = 280.0F * S;
            float w = baseW + extraW;
            float h = baseH + extraH;
            float x = (float)screenWidth / 2.0F - w / 2.0F + guiOffsetX;
            float y = (float)screenHeight / 2.0F
               - h / 2.0F
               + guiOffsetY
               + (this.exit ? 60.0F * S - 60.0F * S * globalAnim : -60.0F * S + 60.0F * S * globalAnim);
            float popScale = 0.94F + 0.06F * globalAnim;
            Font draw = Fonts.sf_medium;
            Font regular = Fonts.sf_medium;
            Font guiicon = Fonts.gui;
            ScreenBlur.capture();
            if (this.resizeBlurFade > 0.01F && (extraW > 1.0F || extraH > 1.0F)) {
               float blurStrength = globalAnim * this.resizeBlurFade;
               if (extraW > 1.0F) {
                  float ewX = x + baseW - 6.0F * S;
                  float ewW = extraW + 6.0F * S;
                  RenderUtil.Blur.blur(
                     ewX, y, ewW, h, blurStrength, 8.0F * S, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.65F), blurStrength)
                  );
                  RenderUtil.Render2D.rect(ewX, y, ewW, h, ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.05F), 8.0F * S);
                  if (this.resizingMain) {
                     RenderUtil.Render2D.rect(
                        x + baseW - 0.5F * S, y + 6.0F * S, 1.0F * S, h - 12.0F * S, ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.35F), 0.5F * S
                     );
                  }
               }

               if (extraH > 1.0F) {
                  float ehY = y + baseH - 6.0F * S;
                  float ehH = extraH + 6.0F * S;
                  RenderUtil.Blur.blur(
                     x, ehY, w, ehH, blurStrength, 8.0F * S, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.65F), blurStrength)
                  );
                  RenderUtil.Render2D.rect(x, ehY, w, ehH, ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.05F), 8.0F * S);
                  if (this.resizingMain) {
                     RenderUtil.Render2D.rect(
                        x + 6.0F * S, y + baseH - 0.5F * S, w - 12.0F * S, 1.0F * S, ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.35F), 0.5F * S
                     );
                  }
               }
            }

            float ht = 20.0F * S;
            float searchW = 140.0F * S;
            float xt = x + 6.0F * S;
            float yt = y + h + 6.0F * S;
            if (!this.management.active()) {
               boolean searchHover = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xt, yt, searchW, ht);
               animation3.setDirection(!searchHover && !searchActive ? Direction.BACKWARDS : Direction.FORWARDS);
               this.animSearchFocus.setDirection(searchActive ? Direction.FORWARDS : Direction.BACKWARDS);
               this.animSearchText.setDirection(!searchQuery.isEmpty() ? Direction.FORWARDS : Direction.BACKWARDS);
               float hvs = animation3.getOutput();
               float focus = this.animSearchFocus.getOutput();
               float typed = this.animSearchText.getOutput();
               RenderUtil.Blur.blur(
                  xt, yt, searchW, ht, globalAnim, 6.0F * S, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.6F), globalAnim)
               );
               RenderUtil.Render2D.glow(xt, yt, searchW, ht, ColorUtil.getColor(0, 0.06F * globalAnim), 6.0F * S, 8.0F, 1.0F);
               RenderUtil.Render2D.rect(
                  xt,
                  yt,
                  searchW,
                  ht,
                  ColorUtil.overCol(ColorUtil.getColor(0, 0.2F * globalAnim), ColorUtil.getColor(25, 0.4F * globalAnim), Math.max(hvs, focus)),
                  6.0F * S
               );
               RenderUtil.Render2D.outline(xt, yt, searchW, ht, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * focus), 6.0F * S);
               guiicon.draw(
                  "A",
                  xt + 7.0F * S,
                  yt + 7.0F * S,
                  6.0F * S,
                  ColorUtil.overCol(
                     ColorUtil.getColor(255, globalAnim * (0.3F + 0.5F * hvs)), ColorUtil.replAlpha(ColorUtil.client(), globalAnim * (0.3F + 0.7F * hvs)), hvs
                  )
               );
               float xSearchText = xt + 6.5F * S + 11.0F * S;
               float wSearchText = searchW - (xSearchText - xt) - 8.0F * S;
               float hidePlaceholder = Math.max(typed, focus);
               if (hidePlaceholder < 0.99F) {
                  regular.draw(
                     Lang.pick("Пошук", "Поиск", "Search"),
                     xSearchText,
                     yt + 5.5F * S + 4.0F * S * hidePlaceholder,
                     7.0F * S,
                     ColorUtil.getColor(255, globalAnim * (1.0F - hidePlaceholder) * (0.3F + 0.5F * hvs))
                  );
               }

               if (typed > 0.01F) {
                  regular.drawFadingTextReverse(
                     searchQuery,
                     xSearchText,
                     yt + 5.5F * S - 4.0F * S * (1.0F - typed),
                     wSearchText,
                     ColorUtil.getColor(255, globalAnim * typed * (0.5F + 0.5F * Math.max(hvs, focus))),
                     7.0F * S
                  );
               }

               int justTyped = System.currentTimeMillis() - this.searchTypeTime < 500L ? 1 : 0;
               float caretTarget = focus * (justTyped == 0 && System.currentTimeMillis() / 500L % 2L != 0L ? 0.0F : 1.0F);
               float caret = this.smooth("search:caret", caretTarget);
               float caretX = this.smooth("search:caretx", xSearchText + Math.min(regular.getWidth(searchQuery, 7.0F * S), wSearchText) + 1.5F * S);
               RenderUtil.Render2D.rect(caretX, yt + 6.5F * S, 0.6F * S, 6.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * caret), 0.3F * S);
            }

            float insetX = w * (1.0F - popScale) / 2.0F;
            float insetY = h * (1.0F - popScale) / 2.0F;
            this.popClipX = x + insetX;
            this.popClipY = y + insetY;
            this.popClipW = w - 2.0F * insetX;
            this.popClipH = h - 2.0F * insetY;
            Scissor.enable(this.popClipX, this.popClipY, this.popClipW, this.popClipH, 2.0F);
            boolean isLiquidGlass = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlass();
            boolean isLiquidGlassOld = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlassOld();
            boolean enableBlur = ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiBlur.getValue();
            boolean isWhite = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isWhiteTheme();
            if (isLiquidGlass) {
               RenderUtil.Render2D.glow(
                  x,
                  y,
                  w,
                  h,
                  isWhite ? ColorUtil.getColor(200, 215, 240, (int)(110.0F * globalAnim)) : ColorUtil.multAlpha(ColorUtil.client(), 0.18F * globalAnim),
                  10.0F * S,
                  18.0F,
                  1.0F
               );
               if (enableBlur) {
                  RenderUtil.Blur.blur(
                     x,
                     y,
                     w,
                     h,
                     globalAnim * 1.8F,
                     10.0F * S,
                     isWhite ? ColorUtil.getColor(245, 248, 255, 0.5F * globalAnim) : ColorUtil.getColor(10, 14, 22, 0.35F * globalAnim)
                  );
               }

               int topGlass = isWhite ? ColorUtil.getColor(255, 255, 255, 0.85F * globalAnim) : ColorUtil.getColor(255, 255, 255, 0.14F * globalAnim);
               int botGlass = isWhite ? ColorUtil.getColor(235, 240, 250, 0.9F * globalAnim) : ColorUtil.getColor(16, 22, 34, 0.38F * globalAnim);
               RenderUtil.Render2D.gradientRect(x, y, w, h, new int[]{topGlass, topGlass, botGlass, botGlass}, 10.0F * S);
               RenderUtil.Render2D.outline(x, y, w, h, 0.75F * S, ColorUtil.getColor(255, 255, 255, 0.2F * globalAnim), 10.0F * S);
               RenderUtil.Render2D.rect(x + 2.0F * S, y + 0.5F * S, w - 4.0F * S, 1.0F * S, ColorUtil.getColor(255, 255, 255, 0.45F * globalAnim), 0.5F * S);
               RenderUtil.Render2D.rect(x + 1.0F * S, y + 1.0F * S, w - 2.0F * S, 24.0F * S, ColorUtil.getColor(255, 255, 255, 0.05F * globalAnim), 9.0F * S);
               RenderUtil.Render2D.rect(x + 6.0F * S, y + 6.0F * S, 30.0F * S, h - 12.0F * S, ColorUtil.getColor(12, 16, 26, 0.35F * globalAnim), 7.0F * S);
               RenderUtil.Render2D.outline(
                  x + 6.0F * S, y + 6.0F * S, 30.0F * S, h - 12.0F * S, 0.5F * S, ColorUtil.getColor(255, 255, 255, 0.16F * globalAnim), 7.0F * S
               );
               RenderUtil.Render2D.rect(x + 7.0F * S, y + 6.5F * S, 28.0F * S, 0.75F * S, ColorUtil.getColor(255, 255, 255, 0.35F * globalAnim), 0.5F * S);
            } else if (isLiquidGlassOld) {
               RenderUtil.Render2D.glow(x, y, w, h, ColorUtil.multAlpha(ColorUtil.client(), 0.22F * globalAnim), 10.0F * S, 16.0F, 1.0F);
               if (enableBlur) {
                  int glassTint = ColorUtil.interpolateColor2(ColorUtil.getColor(12, 16, 26), ColorUtil.client(), 0.25F);
                  RenderUtil.Blur.blur(x, y, w, h, globalAnim * 1.5F, 10.0F * S, ColorUtil.multAlpha(glassTint, 0.55F * globalAnim));
               }

               RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.getColor(18, 24, 38, 0.5F * globalAnim), 8.0F * S);
               RenderUtil.Render2D.outline(x, y, w, h, 1.0F * S, ColorUtil.getColor(255, 255, 255, 0.25F * globalAnim), 8.0F * S);
               RenderUtil.Render2D.rect(x + 1.0F * S, y + 1.0F * S, w - 2.0F * S, 24.0F * S, ColorUtil.getColor(255, 255, 255, 0.07F * globalAnim), 7.0F * S);
               RenderUtil.Render2D.rect(x + 6.0F * S, y + 6.0F * S, 30.0F * S, h - 12.0F * S, ColorUtil.getColor(14, 18, 30, 0.45F * globalAnim), 6.0F * S);
               RenderUtil.Render2D.outline(
                  x + 6.0F * S, y + 6.0F * S, 30.0F * S, h - 12.0F * S, 0.5F * S, ColorUtil.getColor(255, 255, 255, 0.16F * globalAnim), 6.0F * S
               );
            } else {
               RenderUtil.Render2D.glow(x, y, w, h - 0.5F * S, ColorUtil.getColor(0, 0.15F * globalAnim), 8.0F * S, 15.0F, 1.0F);
               if (enableBlur) {
                  int panelTint = ColorUtil.interpolateColor2(ColorUtil.multDark(ColorUtil.background(), 0.6F), ColorUtil.client(), 0.22F);
                  RenderUtil.Blur.blur(x, y, w, h, globalAnim, 8.0F * S, ColorUtil.multAlpha(panelTint, globalAnim));
               }

               RenderUtil.Images.texture(
                  class_2960.method_60655("client", "textures/frame/rectgui.png"), x, y, w, h, ColorUtil.multAlpha(ColorUtil.client(), globalAnim)
               );
               RenderUtil.Render2D.glow(x + 6.0F * S, y + 6.0F * S, 30.0F * S, h - 12.0F * S, ColorUtil.getColor(0, 0.04F * globalAnim), 6.0F * S, 8.0F, 1.0F);
               RenderUtil.Render2D.rect(x + 6.0F * S, y + 6.0F * S, 30.0F * S, h - 12.0F * S, ColorUtil.getColor(0, 0.15F * globalAnim), 6.0F * S);
            }

            float xPanelMini = x + 6.0F * S;
            float yPanelMini = y + 6.0F * S;
            boolean isLogoHov = MathUtil.isHovered(
               (float)this.lastMouseX, (float)this.lastMouseY, xPanelMini + 30.0F * S / 2.0F - 12.0F * S / 2.0F, yPanelMini + 7.0F * S, 12.0F * S, 12.0F * S
            );
            animUserInfo.setDirection(isLogoHov ? Direction.FORWARDS : Direction.BACKWARDS);
            float animL = animUserInfo.getOutput();
            float logoScale = 14.0F * S + 2.0F * S * animL;
            float logoDrawX = xPanelMini + 30.0F * S / 2.0F - logoScale / 2.0F;
            float logoDrawY = yPanelMini + 13.0F * S - logoScale / 2.0F;
            RenderUtil.Render2D.glow(
               logoDrawX,
               logoDrawY,
               logoScale,
               logoScale,
               ColorUtil.replAlpha(ColorUtil.client(), globalAnim * (0.1F + animL * 0.35F)),
               logoScale / 2.0F,
               10.0F,
               1.0F
            );
            RenderUtil.Images.texture(
               class_2960.method_60655("client", "textures/logo.png"), logoDrawX, logoDrawY, logoScale, logoScale, ColorUtil.getColor(255, globalAnim)
            );
            Category[] mainCats = new Category[]{Category.COMBAT, Category.MOVEMENT, Category.PLAYER, Category.OTHER, Category.RENDER, Category.PASTED};
            float cy = yPanelMini + 26.0F * S;

            for (Category category : mainCats) {
               category.alphaS.setDirection(!this.management.active() && this.active == category ? Direction.FORWARDS : Direction.BACKWARDS);
               float anim = category.alphaS.getOutput();
               ru.mentions.utils.animation.satoshi.Animation animUF = category.alphaS2;
               boolean isHv = MathUtil.isHovered(
                  (float)this.lastMouseX, (float)this.lastMouseY, xPanelMini + 30.0F * S / 2.0F - 16.0F * S / 2.0F, cy, 16.0F * S, 16.0F * S
               );
               animUF.setDirection(isHv ? Direction.FORWARDS : Direction.BACKWARDS);
               float animL2 = animUF.getOutput();
               String name = category.getIcon();
               RenderUtil.Render2D.outline(
                  xPanelMini + 30.0F * S / 2.0F - 16.0F * S / 2.0F - 1.0F * S * anim + 1.0F * S,
                  cy - 1.0F * S * anim + 1.0F * S,
                  16.0F * S + 2.0F * S * anim - 2.0F * S,
                  16.0F * S + 2.0F * S * anim - 2.0F * S,
                  0.5F * S,
                  ColorUtil.replAlpha(ColorUtil.client(), globalAnim * anim),
                  5.0F * S
               );
               RenderUtil.Render2D.glow(
                  xPanelMini + 30.0F * S / 2.0F - 12.0F * S / 2.0F + 5.8F * S,
                  cy + 7.8F * S,
                  0.1F * S,
                  0.1F * S,
                  ColorUtil.replAlpha(ColorUtil.client(), globalAnim * animL2 * 0.15F * anim),
                  8.0F * S,
                  9.0F,
                  1.0F
               );
               Fonts.mentions_2
                  .drawCentered(
                     name,
                     xPanelMini + 30.0F * S / 2.0F,
                     cy + 4.5F * S,
                     8.0F * S,
                     ColorUtil.multAlpha(ColorUtil.overCol(ColorUtil.getColor(255, 0.15F * globalAnim + 0.5F * animL2), ColorUtil.client(), anim), globalAnim)
                  );
               String sgff2 = category.getDisplayName();
               float xAnimADd = 16.0F * S - 16.0F * S * animL2;
               Scissor.disable();
               RenderUtil.Render2D.glow(
                  xPanelMini - draw.getWidth(sgff2, 8.0F * S) - 4.0F * S + xAnimADd,
                  cy + 7.0F * S - 6.0F * S,
                  7.0F * S + draw.getWidth(sgff2, 8.0F * S),
                  14.0F * S,
                  ColorUtil.multAlpha(ColorUtil.getColor(0), globalAnim * animL2 * 0.1F),
                  4.0F * S,
                  6.0F,
                  1.0F
               );
               RenderUtil.Blur.blur(
                  xPanelMini - draw.getWidth(sgff2, 8.0F * S) - 4.0F * S + xAnimADd,
                  cy + 7.0F * S - 6.0F * S,
                  7.0F * S + draw.getWidth(sgff2, 8.0F * S),
                  14.0F * S,
                  globalAnim * animL2,
                  4.0F * S,
                  ColorUtil.multAlpha(ColorUtil.background(), globalAnim * animL2 * 0.2F)
               );
               draw.draw(
                  sgff2,
                  xPanelMini - draw.getWidth(sgff2, 8.0F * S) + xAnimADd,
                  cy + 9.0F * S - 6.0F * S,
                  8.0F * S,
                  ColorUtil.getColor(200, globalAnim * animL2)
               );
               Scissor.enable(this.popClipX, this.popClipY, this.popClipW, this.popClipH, 2.0F);
               cy += 20.0F * S;
            }

            float bottomH = 120.0F * S;
            float bottomStartY = yPanelMini + (h - 12.0F * S) - bottomH - 4.0F * S;
            RenderUtil.Render2D.rect(xPanelMini + 5.0F * S, bottomStartY - 6.0F * S, 20.0F * S, 0.5F * S, ColorUtil.getColor(255, 0.08F * globalAnim));
            float btnX = xPanelMini + 30.0F * S / 2.0F - 16.0F * S / 2.0F;
            boolean isThemesActive = !this.management.active() && this.active == Category.THEMES;
            this.drawSidebarBtn(
               btnX,
               bottomStartY,
               "E",
               Lang.pick("Теми", "Темы", "Themes"),
               isThemesActive,
               Category.THEMES.alphaS,
               Category.THEMES.alphaS2,
               S,
               globalAnim,
               draw
            );
            boolean isEventsActive = this.management.active() && this.management.selected(ManagementPanel.Tab.EVENTS);
            this.drawSidebarBtn(
               btnX,
               bottomStartY + 20.0F * S,
               "P",
               Lang.pick("Івенти", "Ивенты", "Events"),
               isEventsActive,
               this.animEventsActive,
               this.animEventsHover,
               S,
               globalAnim,
               draw
            );
            boolean isConfigsActive = this.management.active() && this.management.selected(ManagementPanel.Tab.CONFIGS);
            this.drawSidebarBtn(
               btnX,
               bottomStartY + 40.0F * S,
               "F",
               Lang.pick("Конфіги", "Конфиги", "Configs"),
               isConfigsActive,
               this.animConfigsActive,
               this.animConfigsHover,
               S,
               globalAnim,
               draw
            );
            boolean isFriendsActive = this.management.active() && this.management.selected(ManagementPanel.Tab.FRIENDS);
            this.drawSidebarBtn(
               btnX,
               bottomStartY + 60.0F * S,
               "D",
               Lang.pick("Друзі", "Друзья", "Friends"),
               isFriendsActive,
               this.animFriendsActive,
               this.animFriendsHover,
               S,
               globalAnim,
               draw
            );
            boolean isGpsActive = this.management.active() && this.management.selected(ManagementPanel.Tab.GPS);
            this.drawSidebarBtn(btnX, bottomStartY + 80.0F * S, "C", "GPS", isGpsActive, this.animGpsActive, this.animGpsHover, S, globalAnim, draw);
            boolean isSettingsActive = !this.management.active() && this.active == Category.SETTINGS;
            this.drawSidebarBtn(
               btnX,
               bottomStartY + 100.0F * S,
               "G",
               Lang.pick("Налаштування", "Настройки", "Settings"),
               isSettingsActive,
               Category.SETTINGS.alphaS,
               Category.SETTINGS.alphaS2,
               S,
               globalAnim,
               draw
            );
            if (this.management.active()) {
               this.management.render(x + 42.0F * S, y + 6.0F * S, w - 48.0F * S, h - 12.0F * S, S, globalAnim, (float)this.lastMouseX, (float)this.lastMouseY);
               this.drawMainGrip(x, y, w, h, globalAnim);
               this.drawCosmeticsButton(screenWidth, screenHeight, globalAnim);
               Scissor.reset();
               Render2D.endOverlay();
               if (context != null) {
                  context.method_51448().popMatrix();
               }
            } else {
               float typed2 = this.animSearchText.getOutput();
               float catPanX = x + 6.0F * S + 36.0F * S;
               float catPanW = w - 48.0F * S;
               if (isLiquidGlass) {
                  RenderUtil.Render2D.rect(catPanX, y + 6.0F * S, catPanW, 20.0F * S, ColorUtil.getColor(12, 16, 26, 0.35F * globalAnim), 7.0F * S);
                  RenderUtil.Render2D.outline(
                     catPanX, y + 6.0F * S, catPanW, 20.0F * S, 0.5F * S, ColorUtil.getColor(255, 255, 255, 0.18F * globalAnim), 7.0F * S
                  );
                  RenderUtil.Render2D.rect(
                     catPanX + 2.0F * S, y + 6.5F * S, catPanW - 4.0F * S, 0.75F * S, ColorUtil.getColor(255, 255, 255, 0.35F * globalAnim), 0.5F * S
                  );
               } else if (isLiquidGlassOld) {
                  RenderUtil.Render2D.rect(catPanX, y + 6.0F * S, catPanW, 20.0F * S, ColorUtil.getColor(16, 22, 36, 0.45F * globalAnim), 6.0F * S);
                  RenderUtil.Render2D.outline(
                     catPanX, y + 6.0F * S, catPanW, 20.0F * S, 0.5F * S, ColorUtil.getColor(255, 255, 255, 0.15F * globalAnim), 6.0F * S
                  );
                  RenderUtil.Render2D.rect(
                     catPanX + 1.0F * S, y + 7.0F * S, catPanW - 2.0F * S, 6.0F * S, ColorUtil.getColor(255, 255, 255, 0.05F * globalAnim), 5.0F * S
                  );
               } else {
                  RenderUtil.Render2D.glow(catPanX, y + 6.0F * S, catPanW, 20.0F * S, ColorUtil.getColor(0, 0.04F * globalAnim), 6.0F * S, 8.0F, 1.0F);
                  RenderUtil.Render2D.rect(catPanX, y + 6.0F * S, catPanW, 20.0F * S, ColorUtil.getColor(0, 0.15F * globalAnim), 6.0F * S);
               }

               String catName = this.active.getIcon();
               float catSlideY = 5.0F * S - 5.0F * S * animCategoryReset.getOutput();
               float catSlideX = 8.0F * S - 8.0F * S * animCategoryReset.getOutput();
               Fonts.mentions_2
                  .draw(
                     catName,
                     catPanX + 8.0F * S + catSlideX,
                     y + 12.7F * S + catSlideY,
                     7.0F * S,
                     ColorUtil.multAlpha(ColorUtil.client(), globalAnim * animCategoryReset.getOutput())
                  );
               draw.draw(
                  this.active.getDisplayName(),
                  catPanX + 20.0F * S + catSlideX,
                  y + 11.8F * S + catSlideY,
                  7.0F * S,
                  ColorUtil.getColor(200, globalAnim * animCategoryReset.getOutput())
               );
               if (this.active == Category.PASTED && !this.searching()) {
                  float subX = catPanX + 68.0F * S + catSlideX;
                  float subY = y + 9.5F * S;
                  float subH = 13.0F * S;

                  for (int i = 0; i < PASTED_SUBTABS.length; i++) {
                     String subTitle = PASTED_SUBTABS[i];
                     float txtW = draw.getWidth(subTitle, 6.5F * S);
                     float subW = txtW + 12.0F * S;
                     boolean isSel = this.pastedSubTab == i;
                     boolean isHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, subX, subY, subW, subH);
                     ru.mentions.utils.animation.satoshi.Animation anim = this.pastedTabAnims[i];
                     anim.setDirection(isSel ? Direction.FORWARDS : Direction.BACKWARDS);
                     float out = anim.getOutput();
                     if (out > 0.01F) {
                        RenderUtil.Render2D.glow(
                           subX, subY, subW, subH, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * out * 0.18F), 4.0F * S, 6.0F * S, 1.0F
                        );
                        RenderUtil.Render2D.rect(subX, subY, subW, subH, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * out * 0.25F), 3.5F * S);
                        RenderUtil.Render2D.outline(
                           subX, subY, subW, subH, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * out * 0.7F), 3.5F * S
                        );
                     } else {
                        RenderUtil.Render2D.rect(subX, subY, subW, subH, ColorUtil.getColor(255, (isHov ? 0.08F : 0.035F) * globalAnim), 3.5F * S);
                     }

                     int txtCol = isSel ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(200, (isHov ? 0.95F : 0.6F) * globalAnim);
                     draw.drawCentered(subTitle, subX + subW / 2.0F, subY + 3.0F * S, 6.5F * S, txtCol);
                     subX += subW + 4.0F * S;
                  }
               }

               int found = this.searchResults();
               String foundText = found + " " + (found == 1 ? "результат" : (found >= 2 && found <= 4 ? "результата" : "результатов"));
               draw.draw(
                  foundText,
                  catPanX + catPanW - 8.0F * S - draw.getWidth(foundText, 6.5F * S),
                  y + 12.2F * S + 5.0F * S - 5.0F * S * typed2,
                  6.5F * S,
                  ColorUtil.replAlpha(ColorUtil.client(), globalAnim * typed2)
               );
               if (isLiquidGlass) {
                  RenderUtil.Render2D.rect(
                     catPanX, y + 6.0F * S + 26.0F * S, catPanW, h - 12.0F * S - 26.0F * S, ColorUtil.getColor(10, 14, 24, 0.35F * globalAnim), 8.0F * S
                  );
                  RenderUtil.Render2D.outline(
                     catPanX,
                     y + 6.0F * S + 26.0F * S,
                     catPanW,
                     h - 12.0F * S - 26.0F * S,
                     0.5F * S,
                     ColorUtil.getColor(255, 255, 255, 0.16F * globalAnim),
                     8.0F * S
                  );
                  RenderUtil.Render2D.rect(
                     catPanX + 2.0F * S,
                     y + 6.0F * S + 26.5F * S,
                     catPanW - 4.0F * S,
                     0.75F * S,
                     ColorUtil.getColor(255, 255, 255, 0.3F * globalAnim),
                     0.5F * S
                  );
               } else if (isLiquidGlassOld) {
                  RenderUtil.Render2D.rect(
                     catPanX, y + 6.0F * S + 26.0F * S, catPanW, h - 12.0F * S - 26.0F * S, ColorUtil.getColor(14, 18, 30, 0.45F * globalAnim), 7.0F * S
                  );
                  RenderUtil.Render2D.outline(
                     catPanX,
                     y + 6.0F * S + 26.0F * S,
                     catPanW,
                     h - 12.0F * S - 26.0F * S,
                     0.5F * S,
                     ColorUtil.getColor(255, 255, 255, 0.14F * globalAnim),
                     7.0F * S
                  );
               } else {
                  RenderUtil.Render2D.glow(
                     catPanX, y + 6.0F * S + 26.0F * S, catPanW, h - 12.0F * S - 26.0F * S, ColorUtil.getColor(0, 0.04F * globalAnim), 7.0F * S, 8.0F, 1.0F
                  );
                  RenderUtil.Render2D.rect(
                     catPanX, y + 6.0F * S + 26.0F * S, catPanW, h - 12.0F * S - 26.0F * S, ColorUtil.getColor(0, 0.15F * globalAnim), 7.0F * S
                  );
               }

               Scissor.enable(catPanX, y + 6.0F * S + 26.0F * S, catPanW, h - 12.0F * S - 26.0F * S, 2.0F);
               float scrollFactor = 1.0F - (float)Math.exp((double)(-15.0F * dt));
               this.scrollAnim = this.scrollAnim + (this.scrollTarget - this.scrollAnim) * scrollFactor;
               if (Math.abs(this.scrollTarget - this.scrollAnim) < 0.1F) {
                  this.scrollAnim = this.scrollTarget;
                  this.scrollVel = 0.0F;
               }

               float xModule = catPanX + 5.0F * S + (float)this.catDir * (1.0F - animCategoryReset.getOutput()) * 12.0F * S;
               float yModule = y + 6.0F * S + 30.0F * S - this.scrollAnim;
               float listTop = y + 6.0F * S + 26.0F * S;
               float listBottom = listTop + (h - 12.0F * S - 26.0F * S);
               float availableCardW = catPanW - 18.0F * S;
               float cardW = Math.max(140.0F * S, availableCardW / 2.0F);
               float cardGap = 8.0F * S;
               if (this.active == Category.THEMES && !this.searching()) {
                  this.select = null;
                  yModule = this.renderThemesTab(catPanX, yModule, catPanW, h - 12.0F * S - 26.0F * S, globalAnim, dt, draw, regular);
               } else if (this.active == Category.SETTINGS && !this.searching()) {
                  this.renderSettingsTab(catPanX, yModule, catPanW, h - 12.0F * S - 26.0F * S, globalAnim, dt, draw, regular);
               } else {
                  ArrayList<Module> visM = new ArrayList<>();

                  for (Module f : Client.get().moduleManager().values()) {
                     boolean isV = this.moduleVisible(f);
                     f.getAnimation14().setDirection(isV ? Direction.FORWARDS : Direction.BACKWARDS);
                     if (isV) {
                        visM.add(f);
                     }
                  }

                  float catAnim = animCategoryReset.getOutput();
                  int rowIdx = 0;

                  for (int mi = 0; mi < visM.size(); mi += 2) {
                     Module fa = visM.get(mi);
                     Module fb = mi + 1 < visM.size() ? visM.get(mi + 1) : null;
                     float hA = Math.max(34.0F * S, 22.0F * S + this.descHeight(draw, fa.getDesc(), cardW - 10.0F * S) + 6.0F * S);
                     float hB = fb != null ? Math.max(34.0F * S, 22.0F * S + this.descHeight(draw, fb.getDesc(), cardW - 10.0F * S) + 6.0F * S) : 0.0F;
                     float rowH = Math.max(hA, hB);
                     float curRowY = yModule + (1.0F - catAnim) * 8.0F * S;
                     float rowX = catPanX + 5.0F * S;
                     float cardAlpha = globalAnim * catAnim;
                     if (curRowY + rowH >= listTop && curRowY <= listBottom && cardAlpha > 0.02F) {
                        this.drawModuleCard(fa, rowX, curRowY, cardW, rowH, cardAlpha, draw, regular);
                        if (fb != null) {
                           this.drawModuleCard(fb, rowX + cardW + cardGap, curRowY, cardW, rowH, cardAlpha, draw, regular);
                        }
                     }

                     yModule += rowH + 6.0F * S;
                     rowIdx++;
                  }
               }

               this.animSearchEmpty.setDirection(this.searching() && found == 0 ? Direction.FORWARDS : Direction.BACKWARDS);
               float emptyAnim = this.animSearchEmpty.getOutput();
               if (emptyAnim > 0.01F) {
                  float xEmpty = catPanX + catPanW / 2.0F;
                  float yEmpty = y + 6.0F * S + 26.0F * S + (h - 12.0F * S - 26.0F * S) / 2.0F - 8.0F * S + 6.0F * S - 6.0F * S * emptyAnim;
                  guiicon.drawCentered("A", xEmpty, yEmpty, 8.0F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * emptyAnim * 0.4F));
                  draw.drawCentered("Ничего не найдено", xEmpty, yEmpty + 14.0F * S, 7.0F * S, ColorUtil.getColor(255, globalAnim * emptyAnim * 0.5F));
               }

               float contentHeight = yModule + this.scrollAnim - (y + 6.0F * S + 30.0F * S);
               float viewHeight = h - 12.0F * S - 26.0F * S - 4.0F * S;
               this.maxScroll = Math.max(0.0F, contentHeight - viewHeight);
               if (this.scrollTarget > this.maxScroll) {
                  this.scrollTarget = this.maxScroll;
               }

               Scissor.disable();
               Scissor.reset();
               this.drawMainGrip(x, y, w, h, globalAnim);
               float baseSetW = 150.0F * S;
               float baseSetH = 130.0F * S;
               float setW = baseSetW + setExtraW;
               float setH = baseSetH + setExtraH;
               float setX = this.settingsX(screenWidth, x, w);
               if (this.setResizeBlurFade > 0.01F && (setExtraW > 1.0F || setExtraH > 1.0F)) {
                  float sBlurStrength = globalAnim * this.setResizeBlurFade;
                  if (setExtraW > 1.0F) {
                     float sewX = setX + baseSetW - 4.0F * S;
                     float sewW = setExtraW + 4.0F * S;
                     RenderUtil.Blur.blur(
                        sewX, y, sewW, setH, sBlurStrength, 8.0F * S, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.65F), sBlurStrength)
                     );
                     RenderUtil.Render2D.rect(sewX, y, sewW, setH, ColorUtil.replAlpha(ColorUtil.client(), sBlurStrength * 0.05F), 8.0F * S);
                     if (this.resizingSet) {
                        RenderUtil.Render2D.rect(
                           setX + baseSetW - 0.5F * S,
                           y + 4.0F * S,
                           1.0F * S,
                           setH - 8.0F * S,
                           ColorUtil.replAlpha(ColorUtil.client(), sBlurStrength * 0.35F),
                           0.5F * S
                        );
                     }
                  }

                  if (setExtraH > 1.0F) {
                     float sehY = y + baseSetH - 4.0F * S;
                     float sehH = setExtraH + 4.0F * S;
                     RenderUtil.Blur.blur(
                        setX, sehY, setW, sehH, sBlurStrength, 8.0F * S, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.65F), sBlurStrength)
                     );
                     RenderUtil.Render2D.rect(setX, sehY, setW, sehH, ColorUtil.replAlpha(ColorUtil.client(), sBlurStrength * 0.05F), 8.0F * S);
                     if (this.resizingSet) {
                        RenderUtil.Render2D.rect(
                           setX + 4.0F * S,
                           y + baseSetH - 0.5F * S,
                           setW - 8.0F * S,
                           1.0F * S,
                           ColorUtil.replAlpha(ColorUtil.client(), sBlurStrength * 0.35F),
                           0.5F * S
                        );
                     }
                  }
               }

               float setInsetX = setW * (1.0F - popScale) / 2.0F;
               float setInsetY = setH * (1.0F - popScale) / 2.0F;
               Scissor.enable(setX + setInsetX, y + setInsetY, setW - 2.0F * setInsetX, setH - 2.0F * setInsetY, 2.0F);
               float xSetBase = setX + 6.0F * S;
               float ySetBase = y + 6.0F * S;
               float wSetBase = setW - 12.0F * S;
               float hSetBase = setH - 12.0F * S;
               if (isLiquidGlass) {
                  RenderUtil.Render2D.glow(setX, y, setW, setH, ColorUtil.multAlpha(ColorUtil.client(), 0.18F * globalAnim), 10.0F * S, 18.0F, 1.0F);
                  if (enableBlur) {
                     RenderUtil.Blur.blur(setX, y, setW, setH, globalAnim * 1.8F, 10.0F * S, ColorUtil.getColor(10, 14, 22, 0.35F * globalAnim));
                  }

                  int topGlass = ColorUtil.getColor(255, 255, 255, 0.14F * globalAnim);
                  int botGlass = ColorUtil.getColor(16, 22, 34, 0.38F * globalAnim);
                  RenderUtil.Render2D.gradientRect(setX, y, setW, setH, new int[]{topGlass, topGlass, botGlass, botGlass}, 10.0F * S);
                  RenderUtil.Render2D.outline(setX, y, setW, setH, 0.75F * S, ColorUtil.getColor(255, 255, 255, 0.2F * globalAnim), 10.0F * S);
                  RenderUtil.Render2D.rect(
                     setX + 2.0F * S, y + 0.5F * S, setW - 4.0F * S, 1.0F * S, ColorUtil.getColor(255, 255, 255, 0.45F * globalAnim), 0.5F * S
                  );
                  RenderUtil.Render2D.rect(
                     setX + 1.0F * S, y + 1.0F * S, setW - 2.0F * S, 18.0F * S, ColorUtil.getColor(255, 255, 255, 0.05F * globalAnim), 9.0F * S
                  );
                  RenderUtil.Render2D.rect(xSetBase, ySetBase, wSetBase, hSetBase, ColorUtil.getColor(12, 16, 26, 0.35F * globalAnim), 8.0F * S);
                  RenderUtil.Render2D.outline(xSetBase, ySetBase, wSetBase, hSetBase, 0.5F * S, ColorUtil.getColor(255, 255, 255, 0.16F * globalAnim), 8.0F * S);
                  RenderUtil.Render2D.rect(
                     xSetBase + 2.0F * S, ySetBase + 0.5F * S, wSetBase - 4.0F * S, 0.75F * S, ColorUtil.getColor(255, 255, 255, 0.3F * globalAnim), 0.5F * S
                  );
               } else if (isLiquidGlassOld) {
                  RenderUtil.Render2D.glow(setX, y, setW, setH, ColorUtil.multAlpha(ColorUtil.client(), 0.18F * globalAnim), 10.0F * S, 16.0F, 1.0F);
                  if (enableBlur) {
                     int glassTint = ColorUtil.interpolateColor2(ColorUtil.getColor(12, 16, 26), ColorUtil.client(), 0.25F);
                     RenderUtil.Blur.blur(setX, y, setW, setH, globalAnim * 1.5F, 10.0F * S, ColorUtil.multAlpha(glassTint, 0.55F * globalAnim));
                  }

                  RenderUtil.Render2D.rect(setX, y, setW, setH, ColorUtil.getColor(18, 24, 38, 0.5F * globalAnim), 8.0F * S);
                  RenderUtil.Render2D.outline(setX, y, setW, setH, 1.0F * S, ColorUtil.getColor(255, 255, 255, 0.25F * globalAnim), 8.0F * S);
                  RenderUtil.Render2D.rect(
                     setX + 1.0F * S, y + 1.0F * S, setW - 2.0F * S, 18.0F * S, ColorUtil.getColor(255, 255, 255, 0.07F * globalAnim), 7.0F * S
                  );
                  RenderUtil.Render2D.rect(xSetBase, ySetBase, wSetBase, hSetBase, ColorUtil.getColor(14, 18, 30, 0.45F * globalAnim), 7.0F * S);
                  RenderUtil.Render2D.outline(xSetBase, ySetBase, wSetBase, hSetBase, 0.5F * S, ColorUtil.getColor(255, 255, 255, 0.15F * globalAnim), 7.0F * S);
               } else {
                  RenderUtil.Render2D.glow(setX, y, setW, setH - 0.5F * S, ColorUtil.getColor(0, 0.15F * globalAnim), 8.0F * S, 15.0F, 1.0F);
                  if (enableBlur) {
                     int panelTint = ColorUtil.interpolateColor2(ColorUtil.multDark(ColorUtil.background(), 0.6F), ColorUtil.client(), 0.22F);
                     RenderUtil.Blur.blur(setX, y, setW, setH, globalAnim, 8.0F * S, ColorUtil.multAlpha(panelTint, globalAnim));
                  }

                  RenderUtil.Images.texture(
                     class_2960.method_60655("client", "textures/frame/rectthemegui.png"),
                     setX,
                     y,
                     setW,
                     setH,
                     ColorUtil.multAlpha(ColorUtil.client(), globalAnim)
                  );
                  RenderUtil.Render2D.glow(xSetBase, ySetBase, wSetBase, hSetBase, ColorUtil.getColor(0, 0.04F * globalAnim), 7.0F * S, 8.0F, 1.0F);
                  RenderUtil.Render2D.rect(xSetBase, ySetBase, wSetBase, hSetBase, ColorUtil.getColor(0, 0.15F * globalAnim), 7.0F * S);
               }

               Scissor.enable(xSetBase, ySetBase, wSetBase, hSetBase, 2.0F);
               animation2.setDirection(this.select != null ? Direction.FORWARDS : Direction.BACKWARDS);
               draw.drawCentered(
                  "Выберите модуль",
                  xSetBase + wSetBase / 2.0F,
                  ySetBase + 72.0F * S + 8.0F * S * animation2.getOutput(),
                  7.0F * S,
                  ColorUtil.getColor(255, (globalAnim - animation2.getOutput()) * 0.8F)
               );
               RenderUtil.Images.texture(
                  class_2960.method_60655("client", "textures/gui.png"),
                  (float)((int)(xSetBase + wSetBase / 2.0F - 30.0F * S - 6.0F * S)),
                  (float)((int)(ySetBase + 4.0F * S + 10.0F * S - 10.0F * S * animation2.getOutput())),
                  (float)((int)(60.0F * S)),
                  (float)((int)(54.0F * S)),
                  ColorUtil.replAlpha(ColorUtil.WHITE, globalAnim - animation2.getOutput())
               );
               float setScrollFactor = 1.0F - (float)Math.exp((double)(-15.0F * dt));
               this.settingScrollAnim = this.settingScrollAnim + (this.settingScrollTarget - this.settingScrollAnim) * setScrollFactor;
               if (Math.abs(this.settingScrollTarget - this.settingScrollAnim) < 0.1F) {
                  this.settingScrollAnim = this.settingScrollTarget;
                  this.settingScrollVel = 0.0F;
               }

               float xST = xSetBase + 10.0F * S;
               float yST = ySetBase + 10.0F * S - this.settingScrollAnim;
               float wST = wSetBase - 20.0F * S;
               float setTop = ySetBase;
               float setBottom = ySetBase + hSetBase;

               for (Module fx : Client.get().moduleManager().values()) {
                  fx.animation3.setDirection(fx == this.select ? Direction.FORWARDS : Direction.BACKWARDS);
                  float fa = fx.animation3.getOutput();
                  if (fa > 0.0F) {
                     for (Setting<?> setting : fx.getSettings()) {
                        Setting s;
                        float vis;
                        if (setting instanceof SliderSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sa = fa * vis;
                           float percent = class_3532.method_15363(
                              (((SliderSetting)s).getValue() - ((SliderSetting)s).min) / (((SliderSetting)s).max - ((SliderSetting)s).min), 0.0F, 1.0F
                           );
                           s.getAnimation().update();
                           s.getAnimation().run((double)percent, 0.06F, Easings.LINEAR);
                           boolean onScreen = yST + 25.0F * S >= setTop && yST <= setBottom;
                           boolean hovS2 = onScreen && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, 24.0F * S);
                           ((SliderSetting)s).animation.setDirection(hovS2 ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hover = ((SliderSetting)s).animation.getOutput();
                           float track = wST - 12.0F * S;
                           if (onScreen) {
                              RenderUtil.Render2D.glow(xST, yST, wST, 25.0F * S, ColorUtil.getColor(0, 0.06F * globalAnim * sa), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(xST, yST, wST, 25.0F * S, ColorUtil.getColor(40, 0.15F * globalAnim * sa), 5.0F * S);
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, 25.0F * S, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sa * hover), 5.0F * S
                              );
                              draw.draw(
                                 s.getDisplayName(), xST + 6.0F * S, yST + 5.0F * S, 6.5F * S, ColorUtil.getColor(255, globalAnim * sa * (0.5F + 0.5F * hover))
                              );
                              String val = ((SliderSetting)s).displayValue();
                              draw.draw(
                                 val,
                                 xST + wST - 6.0F * S - draw.getWidth(val, 6.0F * S),
                                 yST + 5.25F * S,
                                 6.0F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sa * (0.6F + 0.4F * hover))
                              );
                              RenderUtil.Render2D.rect(
                                 xST + 6.0F * S, yST + 16.5F * S, track, 3.0F * S, ColorUtil.getColor(0, globalAnim * sa * 0.15F), 1.5F * S
                              );
                              RenderUtil.Render2D.rect(
                                 xST + 6.0F * S,
                                 yST + 16.5F * S,
                                 track * s.getAnimation().get(),
                                 3.0F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sa * (0.5F + 0.5F * hover)),
                                 1.5F * S
                              );
                              RenderUtil.Render2D.rect(
                                 xST + 6.0F * S + track * s.getAnimation().get() - 3.0F * S,
                                 yST + 15.0F * S,
                                 6.0F * S,
                                 6.0F * S,
                                 ColorUtil.getColor((int)(200.0F + 55.0F * hover), globalAnim * sa),
                                 6.0F * S
                              );
                              RenderUtil.Render2D.rect(
                                 xST + 6.0F * S + track * s.getAnimation().get() - 2.0F * S,
                                 yST + 16.0F * S,
                                 4.0F * S,
                                 4.0F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sa * (0.7F + 0.3F * hover)),
                                 6.0F * S
                              );
                           }

                           if (this.draggingSlider == s) {
                              float perc = MathUtil.clamp(((float)this.lastMouseX - (xST + 6.0F * S)) / track, 0.0F, 1.0F);
                              float newVal = MathUtil.clamp(
                                 MathUtil.round(((SliderSetting)s).min + (((SliderSetting)s).max - ((SliderSetting)s).min) * perc, ((SliderSetting)s).increment),
                                 ((SliderSetting)s).min,
                                 ((SliderSetting)s).max
                              );
                              if (((SliderSetting)s).getValue() != newVal) {
                                 ((SliderSetting)s).set(newVal);
                                 GuiSounds.sliderTick(perc);
                              }
                           }

                           yST += 30.0F * S * fa * vis;
                        }

                        if (setting instanceof ModeSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sax = fa * vis;
                           float hMode = 15.0F * S + this.modeChipsHeight((ModeSetting)s, wST - 12.0F * S) + 4.0F * S;
                           boolean onScreenx = yST + hMode >= setTop && yST <= setBottom;
                           boolean hovS2x = onScreenx && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, hMode);
                           ((ModeSetting)s).animation.setDirection(hovS2x ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hoverx = ((ModeSetting)s).animation.getOutput();
                           if (onScreenx) {
                              RenderUtil.Render2D.glow(xST, yST, wST, hMode, ColorUtil.getColor(0, 0.06F * globalAnim * sax), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(xST, yST, wST, hMode, ColorUtil.getColor(40, 0.15F * globalAnim * sax), 5.0F * S);
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, hMode, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * hoverx), 5.0F * S
                              );
                              String current = ((ModeSetting)s).getDisplayValue();
                              draw.drawFadingText(
                                 s.getDisplayName(),
                                 xST + 6.0F * S,
                                 yST + 4.5F * S,
                                 wST - draw.getWidth(current, 6.0F * S) - 16.0F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.5F + 0.5F * hoverx)),
                                 6.5F * S
                              );
                              draw.draw(
                                 current,
                                 xST + wST - 6.0F * S - draw.getWidth(current, 6.0F * S),
                                 yST + 4.75F * S,
                                 6.0F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * (0.6F + 0.4F * hoverx))
                              );
                              float chipMaxW = wST - 12.0F * S;
                              float px = 0.0F;
                              float py = 0.0F;

                              for (String val : ((ModeSetting)s).values) {
                                 float tw = draw.getWidth(RussianUi.text(val), 6.0F * S) + 8.0F * S;
                                 if (px + tw > chipMaxW && px > 0.0F) {
                                    px = 0.0F;
                                    py += 12.0F * S;
                                 }

                                 float cx = xST + 3.0F * S + px;
                                 float cyy = yST + 15.0F * S + py;
                                 ru.mentions.utils.animation.satoshi.Animation chip = this.chipAnim(fx.getName() + ":" + s.getName() + ":" + val);
                                 chip.setDirection(val.equals(((ModeSetting)s).getValue()) ? Direction.FORWARDS : Direction.BACKWARDS);
                                 float sel = chip.getOutput();
                                 RenderUtil.Render2D.rect(
                                    cx,
                                    cyy,
                                    tw,
                                    10.0F * S,
                                    ColorUtil.overCol(
                                       ColorUtil.getColor(0, 0.25F * globalAnim * sax),
                                       ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * (0.5F + 0.5F * hoverx)),
                                       sel
                                    ),
                                    3.0F * S
                                 );
                                 regular.drawCentered(
                                    RussianUi.text(val),
                                    cx + tw / 2.0F,
                                    cyy + 1.5F * S,
                                    6.0F * S,
                                    ColorUtil.getColor(255, globalAnim * sax * (0.35F + 0.65F * sel) * (0.7F + 0.3F * hoverx))
                                 );
                                 px += tw + 3.0F * S;
                              }
                           }

                           yST += (hMode + 5.0F * S) * fa * vis;
                        }

                        if (setting instanceof MultiBooleanSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sax = fa * vis;
                           float hMulti = 15.0F * S + this.multiChipsHeight((MultiBooleanSetting)s, wST - 12.0F * S) + 4.0F * S;
                           boolean onScreenx = yST + hMulti >= setTop && yST <= setBottom;
                           boolean hovS2x = onScreenx && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, hMulti);
                           s.getAnimation1().setDirection(hovS2x ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hoverx = s.getAnimation1().getOutput();
                           if (onScreenx) {
                              RenderUtil.Render2D.glow(xST, yST, wST, hMulti, ColorUtil.getColor(0, 0.06F * globalAnim * sax), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(xST, yST, wST, hMulti, ColorUtil.getColor(40, 0.15F * globalAnim * sax), 5.0F * S);
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, hMulti, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * hoverx), 5.0F * S
                              );
                              int total = ((MultiBooleanSetting)s).getValues().size();
                              int selectedCount = 0;

                              for (BooleanSetting b : ((MultiBooleanSetting)s).getValues()) {
                                 if (b.getValue()) {
                                    selectedCount++;
                                 }
                              }

                              String counter = selectedCount + " / " + total;
                              draw.drawFadingText(
                                 s.getDisplayName(),
                                 xST + 6.0F * S,
                                 yST + 4.5F * S,
                                 wST - draw.getWidth(counter, 6.0F * S) - 16.0F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.5F + 0.5F * hoverx)),
                                 6.5F * S
                              );
                              draw.draw(
                                 counter,
                                 xST + wST - 6.0F * S - draw.getWidth(counter, 6.0F * S),
                                 yST + 4.75F * S,
                                 6.0F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * (0.6F + 0.4F * hoverx))
                              );
                              float chipMaxW = wST - 12.0F * S;
                              float px = 0.0F;
                              float py = 0.0F;

                              for (BooleanSetting bx : ((MultiBooleanSetting)s).getValues()) {
                                 float tw = draw.getWidth(RussianUi.text(bx.getDisplayName()), 6.0F * S) + 8.0F * S;
                                 if (px + tw > chipMaxW && px > 0.0F) {
                                    px = 0.0F;
                                    py += 12.0F * S;
                                 }

                                 float cx = xST + 3.0F * S + px;
                                 float cyy = yST + 15.0F * S + py;
                                 ru.mentions.utils.animation.satoshi.Animation chip = this.chipAnim(fx.getName() + ":" + s.getName() + ":" + bx.getName());
                                 chip.setDirection(bx.getValue() ? Direction.FORWARDS : Direction.BACKWARDS);
                                 float sel = chip.getOutput();
                                 RenderUtil.Render2D.rect(
                                    cx,
                                    cyy,
                                    tw,
                                    10.0F * S,
                                    ColorUtil.overCol(
                                       ColorUtil.getColor(0, 0.25F * globalAnim * sax),
                                       ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * (0.5F + 0.5F * hoverx)),
                                       sel
                                    ),
                                    3.0F * S
                                 );
                                 regular.drawCentered(
                                    bx.getDisplayName(),
                                    cx + tw / 2.0F,
                                    cyy + 1.5F * S,
                                    6.0F * S,
                                    ColorUtil.getColor(255, globalAnim * sax * (0.35F + 0.65F * sel) * (0.7F + 0.3F * hoverx))
                                 );
                                 px += tw + 3.0F * S;
                              }
                           }

                           yST += (hMulti + 5.0F * S) * fa * vis;
                        }

                        if (setting instanceof BooleanSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sax = fa * vis;
                           boolean onScreen2 = yST + 16.0F * S >= setTop && yST <= setBottom;
                           boolean hovS = onScreen2 && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, 16.0F * S);
                           ((BooleanSetting)s).animation.setDirection(hovS ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hover2 = ((BooleanSetting)s).animation.getOutput();
                           ((BooleanSetting)s).animation2.setDirection(((BooleanSetting)s).getValue() ? Direction.FORWARDS : Direction.BACKWARDS);
                           float sanimation2 = ((BooleanSetting)s).animation2.getOutput();
                           if (onScreen2) {
                              RenderUtil.Render2D.glow(xST, yST, wST, 16.0F * S, ColorUtil.getColor(0, 0.06F * globalAnim * sax), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(xST, yST, wST, 16.0F * S, ColorUtil.getColor(40, 0.15F * globalAnim * sax), 5.0F * S);
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, 16.0F * S, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * hover2), 5.0F * S
                              );
                              draw.draw(
                                 s.getDisplayName(),
                                 xST + 6.0F * S,
                                 yST + 4.0F * S,
                                 6.5F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.5F + 0.5F * hover2))
                              );
                              RenderUtil.Render2D.rect(
                                 xST + wST - 6.0F * S - 12.0F * S,
                                 yST + 4.5F * S,
                                 12.0F * S,
                                 7.0F * S,
                                 ColorUtil.overCol(
                                    ColorUtil.getColor(0, 0.2F * globalAnim * sax),
                                    ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * (0.5F + 0.5F * hover2)),
                                    sanimation2
                                 ),
                                 3.5F * S
                              );
                              RenderUtil.Render2D.rect(
                                 xST + wST - 6.0F * S - 12.0F * S + 1.25F * S + 4.5F * S * sanimation2,
                                 yST + 5.75F * S,
                                 4.5F * S,
                                 4.5F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.2F + 0.4F * sanimation2 + 0.4F * hover2)),
                                 2.5F * S
                              );
                           }

                           yST += 21.0F * S * fa * vis;
                        }

                        if (setting instanceof ButtonSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sax = fa * vis;
                           boolean onScreen3 = yST + 16.0F * S >= setTop && yST <= setBottom;
                           boolean hovS = onScreen3 && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, 16.0F * S);
                           ru.mentions.utils.animation.satoshi.Animation btn = this.chipAnim(fx.getName() + ":" + s.getName() + ":btn");
                           btn.setDirection(hovS ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hoverx = btn.getOutput();
                           ((ButtonSetting)s).pressAnim.update();
                           float press = ((ButtonSetting)s).pressAnim.get();
                           float accent = Math.max(hoverx, press);
                           if (onScreen3) {
                              RenderUtil.Render2D.glow(xST, yST, wST, 16.0F * S, ColorUtil.getColor(0, 0.06F * globalAnim * sax), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(
                                 xST,
                                 yST,
                                 wST,
                                 16.0F * S,
                                 ColorUtil.overCol(
                                    ColorUtil.getColor(40, 0.15F * globalAnim * sax), ColorUtil.replAlpha(ColorUtil.client(), 0.25F * globalAnim * sax), accent
                                 ),
                                 5.0F * S
                              );
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, 16.0F * S, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * accent), 5.0F * S
                              );
                              RenderUtil.Render2D.glow(
                                 xST, yST, wST, 16.0F * S, ColorUtil.replAlpha(ColorUtil.client(), 0.15F * globalAnim * sax * press), 5.0F * S, 8.0F, 1.0F
                              );
                              draw.drawCentered(
                                 s.getDisplayName(),
                                 xST + wST / 2.0F,
                                 yST + 4.0F * S,
                                 6.5F * S,
                                 ColorUtil.overCol(
                                    ColorUtil.getColor(255, globalAnim * sax * (0.5F + 0.5F * hoverx)),
                                    ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax),
                                    press
                                 )
                              );
                           }

                           yST += 21.0F * S * fa * vis;
                        }

                        if (setting instanceof BindSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sax = fa * vis;
                           boolean onScreen4 = yST + 16.0F * S >= setTop && yST <= setBottom;
                           boolean hovS = onScreen4 && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, 16.0F * S);
                           ((BindSetting)s).animation.setDirection(hovS ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hover3 = ((BindSetting)s).animation.getOutput();
                           if (onScreen4) {
                              RenderUtil.Render2D.glow(xST, yST, wST, 16.0F * S, ColorUtil.getColor(0, 0.06F * globalAnim * sax), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(xST, yST, wST, 16.0F * S, ColorUtil.getColor(40, 0.15F * globalAnim * sax), 5.0F * S);
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, 16.0F * S, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * hover3), 5.0F * S
                              );
                              boolean binding = this.activeBind == s;
                              ru.mentions.utils.animation.satoshi.Animation bindAct = this.chipAnim(fx.getName() + ":" + s.getName() + ":act");
                              bindAct.setDirection(binding ? Direction.FORWARDS : Direction.BACKWARDS);
                              float act = bindAct.getOutput();
                              String keyName = binding ? "..." : (((BindSetting)s).get() == -1 ? "n/a" : Keyboard.keyName(((BindSetting)s).get()));
                              float bw = this.smooth(fx.getName() + ":" + s.getName() + ":bw", draw.getWidth(keyName, 6.0F * S) + 10.0F * S);
                              draw.drawFadingText(
                                 s.getDisplayName(),
                                 xST + 6.0F * S,
                                 yST + 4.0F * S,
                                 wST - bw - 16.0F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.5F + 0.5F * hover3)),
                                 6.5F * S
                              );
                              RenderUtil.Render2D.rect(
                                 xST + wST - 6.0F * S - bw,
                                 yST + 3.0F * S,
                                 bw,
                                 10.0F * S,
                                 ColorUtil.overCol(
                                    ColorUtil.getColor(0, 0.25F * globalAnim * sax), ColorUtil.replAlpha(ColorUtil.client(), 0.15F * globalAnim * sax), act
                                 ),
                                 3.0F * S
                              );
                              RenderUtil.Render2D.outline(
                                 xST + wST - 6.0F * S - bw,
                                 yST + 3.0F * S,
                                 bw,
                                 10.0F * S,
                                 0.5F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * act),
                                 3.0F * S
                              );
                              RenderUtil.Render2D.glow(
                                 xST + wST - 6.0F * S - bw,
                                 yST + 3.0F * S,
                                 bw,
                                 10.0F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), 0.12F * globalAnim * sax * act),
                                 3.0F * S,
                                 6.0F,
                                 1.0F
                              );
                              regular.drawCentered(
                                 keyName,
                                 xST + wST - 6.0F * S - bw / 2.0F,
                                 yST + 4.5F * S,
                                 6.0F * S,
                                 ColorUtil.replAlpha(
                                    ColorUtil.overCol(ColorUtil.getColor(255), ColorUtil.client(), act),
                                    globalAnim * sax * (0.45F + 0.55F * Math.max(act, hover3))
                                 )
                              );
                           }

                           yST += 21.0F * S * fa * vis;
                        }

                        if (setting instanceof StringSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sax = fa * vis;
                           boolean onScreen5 = yST + 16.0F * S >= setTop && yST <= setBottom;
                           boolean hovS = onScreen5 && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, 16.0F * S);
                           s.getAnimation1().setDirection(hovS ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hover4 = s.getAnimation1().getOutput();
                           if (onScreen5) {
                              RenderUtil.Render2D.glow(xST, yST, wST, 16.0F * S, ColorUtil.getColor(0, 0.06F * globalAnim * sax), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(xST, yST, wST, 16.0F * S, ColorUtil.getColor(40, 0.15F * globalAnim * sax), 5.0F * S);
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, 16.0F * S, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * hover4), 5.0F * S
                              );
                              boolean editing = this.activeString == s;
                              ru.mentions.utils.animation.satoshi.Animation editAct = this.chipAnim(fx.getName() + ":" + s.getName() + ":act");
                              editAct.setDirection(editing ? Direction.FORWARDS : Direction.BACKWARDS);
                              float act = editAct.getOutput();
                              String shown = editing
                                 ? this.stringBuffer + (System.currentTimeMillis() / 400L % 2L == 0L ? "_" : "")
                                 : ((StringSetting)s).getValue();
                              if (shown.isEmpty()) {
                                 shown = "...";
                              }

                              float bw = this.smooth(
                                 fx.getName() + ":" + s.getName() + ":bw", Math.min(draw.getWidth(shown, 6.0F * S) + 10.0F * S, wST * 0.55F)
                              );
                              draw.drawFadingText(
                                 s.getDisplayName(),
                                 xST + 6.0F * S,
                                 yST + 4.0F * S,
                                 wST - bw - 16.0F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.5F + 0.5F * hover4)),
                                 6.5F * S
                              );
                              RenderUtil.Render2D.rect(
                                 xST + wST - 6.0F * S - bw,
                                 yST + 3.0F * S,
                                 bw,
                                 10.0F * S,
                                 ColorUtil.overCol(
                                    ColorUtil.getColor(0, 0.25F * globalAnim * sax), ColorUtil.replAlpha(ColorUtil.client(), 0.15F * globalAnim * sax), act
                                 ),
                                 3.0F * S
                              );
                              RenderUtil.Render2D.outline(
                                 xST + wST - 6.0F * S - bw,
                                 yST + 3.0F * S,
                                 bw,
                                 10.0F * S,
                                 0.5F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * act),
                                 3.0F * S
                              );
                              RenderUtil.Render2D.glow(
                                 xST + wST - 6.0F * S - bw,
                                 yST + 3.0F * S,
                                 bw,
                                 10.0F * S,
                                 ColorUtil.replAlpha(ColorUtil.client(), 0.12F * globalAnim * sax * act),
                                 3.0F * S,
                                 6.0F,
                                 1.0F
                              );
                              regular.drawFadingText(
                                 shown,
                                 xST + wST - 6.0F * S - bw + 5.0F * S,
                                 yST + 4.5F * S,
                                 bw - 8.0F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.45F + 0.55F * Math.max(act, hover4))),
                                 6.0F * S
                              );
                           }

                           yST += 21.0F * S * fa * vis;
                        }

                        if (setting instanceof ColorSetting && (vis = this.visAnim(fx, s = setting)) > 0.01F) {
                           float sax = fa * vis;
                           ((ColorSetting)s).pickerAnim.setDirection(((ColorSetting)s).pickerOpen ? Direction.FORWARDS : Direction.BACKWARDS);
                           float open = ((ColorSetting)s).pickerAnim.getOutput();
                           float hCol = 16.0F * S + open * 35.0F * S;
                           boolean onScreen6 = yST + hCol >= setTop && yST <= setBottom;
                           boolean hovS3 = onScreen6 && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, xST, yST, wST, hCol);
                           s.getAnimation1().setDirection(hovS3 ? Direction.FORWARDS : Direction.BACKWARDS);
                           float hover5 = s.getAnimation1().getOutput();
                           float bx = xST + 6.0F * S;
                           float bwd = wST - 12.0F * S;
                           int col = ((ColorSetting)s).getValue();
                           String kb = fx.getName() + ":" + s.getName();
                           if (onScreen6) {
                              RenderUtil.Render2D.glow(xST, yST, wST, hCol, ColorUtil.getColor(0, 0.06F * globalAnim * sax), 5.0F * S, 7.0F, 1.0F);
                              RenderUtil.Render2D.rect(xST, yST, wST, hCol, ColorUtil.getColor(40, 0.15F * globalAnim * sax), 5.0F * S);
                              RenderUtil.Render2D.outline(
                                 xST, yST, wST, hCol, 0.5F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * sax * hover5), 5.0F * S
                              );
                              draw.drawFadingText(
                                 s.getDisplayName(),
                                 xST + 6.0F * S,
                                 yST + 4.0F * S,
                                 wST - 34.0F * S,
                                 ColorUtil.getColor(255, globalAnim * sax * (0.5F + 0.5F * hover5)),
                                 6.5F * S
                              );
                              int pr = (int)this.smooth(kb + ":pr", (float)(col >> 16 & 0xFF));
                              int pg = (int)this.smooth(kb + ":pg", (float)(col >> 8 & 0xFF));
                              int pb = (int)this.smooth(kb + ":pb", (float)(col & 0xFF));
                              float pcx = xST + wST - 6.0F * S - 9.0F * S;
                              float pcy = yST + 4.0F * S;
                              RenderUtil.Render2D.rect(pcx, pcy, 8.0F * S, 8.0F * S, ColorUtil.getColor(pr, pg, pb, globalAnim * sax), 8.0F * S);
                              RenderUtil.Render2D.outline(
                                 pcx - 0.75F * S,
                                 pcy - 0.75F * S,
                                 9.5F * S,
                                 9.5F * S,
                                 0.25F * S,
                                 ColorUtil.getColor(pr, pg, pb, globalAnim * sax * (0.4F + 0.6F * hover5)),
                                 8.0F * S
                              );
                              RenderUtil.Render2D.glow(
                                 pcx, pcy, 8.0F * S, 8.0F * S, ColorUtil.getColor(pr, pg, pb, 0.1F * globalAnim * sax), 8.0F * S, 7.0F, 1.0F
                              );
                              if (open > 0.01F) {
                                 float[] hsb = Color.RGBtoHSB(col >> 16 & 0xFF, col >> 8 & 0xFF, col & 0xFF, null);
                                 float shh = this.smooth(kb + ":h", hsb[0]);
                                 float shs = this.smooth(kb + ":s", hsb[1]);
                                 float shb = this.smooth(kb + ":b", hsb[2]);
                                 float[] smoothHsb = new float[]{shh, shs, shb};
                                 int segs = 16;
                                 float seg = bwd / (float)segs;

                                 for (int bar = 0; bar < 3; bar += 1) {
                                    float by = yST + 18.0F * S + (float)(bar * 12) * S;

                                    for (int i = 0; i < segs; i += 1) {
                                       float t0 = (float)i / (float)segs;
                                       float t1 = (float)(i + 1) / (float)segs;

                                       int c0 = switch (bar) {
                                          case 0 -> Color.HSBtoRGB(t0, 1.0F, 1.0F);
                                          case 1 -> Color.HSBtoRGB(shh, t0, shb);
                                          default -> Color.HSBtoRGB(shh, shs, t0);
                                       };

                                       int c1 = switch (bar) {
                                          case 0 -> Color.HSBtoRGB(t1, 1.0F, 1.0F);
                                          case 1 -> Color.HSBtoRGB(shh, t1, shb);
                                          default -> Color.HSBtoRGB(shh, shs, t1);
                                       };
                                       int a0 = ColorUtil.replAlpha(c0, globalAnim * sax * open);
                                       int a1 = ColorUtil.replAlpha(c1, globalAnim * sax * open);
                                       RenderUtil.Render2D.gradientRect(
                                          bx + (float)i * seg,
                                          by,
                                          seg + (i == segs - 1 ? 0.0F : 0.5F * S),
                                          4.0F * S,
                                          new int[]{a0, a1, a1, a0},
                                          i == 0 ? 2.0F * S : 0.0F,
                                          i == segs - 1 ? 2.0F * S : 0.0F,
                                          i == segs - 1 ? 2.0F * S : 0.0F,
                                          i == 0 ? 2.0F * S : 0.0F
                                       );
                                    }

                                    float kx = bx + bwd * smoothHsb[bar];

                                    int kc = switch (bar) {
                                       case 0 -> Color.HSBtoRGB(shh, 1.0F, 1.0F);
                                       default -> Color.HSBtoRGB(shh, shs, shb);
                                    };
                                    RenderUtil.Render2D.rect(
                                       kx - 3.0F * S, by - 1.0F * S, 6.0F * S, 6.0F * S, ColorUtil.getColor(255, globalAnim * sax * open), 6.0F * S
                                    );
                                    RenderUtil.Render2D.rect(kx - 2.0F * S, by, 4.0F * S, 4.0F * S, ColorUtil.replAlpha(kc, globalAnim * sax * open), 6.0F * S);
                                 }
                              }
                           }

                           if (open > 0.01F && this.draggingColor == s) {
                              float[] hsb = Color.RGBtoHSB(col >> 16 & 0xFF, col >> 8 & 0xFF, col & 0xFF, null);
                              float t = MathUtil.clamp(((float)this.lastMouseX - bx) / bwd, 0.0F, 1.0F);
                              float[] nh = new float[]{hsb[0], hsb[1], hsb[2]};
                              nh[this.draggingColorBar] = t;
                              int rgb = Color.HSBtoRGB(nh[0], nh[1], nh[2]);
                              int newCol = col & 0xFF000000 | rgb & 16777215;
                              if (newCol != col) {
                                 ((ColorSetting)s).set(newCol);
                                 GuiSounds.colorTick(t);
                              }
                           }

                           yST += (hCol + 5.0F * S) * fa * vis;
                        }
                     }
                  }
               }

               float contentHS = yST + this.settingScrollAnim - (ySetBase + 10.0F * S);
               this.settingMaxScroll = Math.max(0.0F, contentHS - (hSetBase - 14.0F * S));
               if (this.settingScrollTarget > this.settingMaxScroll) {
                  this.settingScrollTarget = this.settingMaxScroll;
               }

               Scissor.disable();
               if (this.select != null) {
                  float setGripX = setX + setW - 12.0F * S;
                  float setGripY = y + setH - 12.0F * S;
                  boolean setGripHover = MathUtil.isHovered(
                     (float)this.lastMouseX, (float)this.lastMouseY, setGripX - 3.0F * S, setGripY - 3.0F * S, 14.0F * S, 14.0F * S
                  );
                  float setGripAlpha = globalAnim * (!setGripHover && !this.resizingSet ? 0.35F : 0.95F);
                  int setGripColor = ColorUtil.replAlpha(ColorUtil.client(), setGripAlpha);
                  RenderUtil.Render2D.rect(setGripX + 5.0F * S, setGripY + 6.5F * S, 3.0F * S, 1.0F * S, setGripColor, 0.5F * S);
                  RenderUtil.Render2D.rect(setGripX + 2.5F * S, setGripY + 4.0F * S, 5.5F * S, 1.0F * S, setGripColor, 0.5F * S);
                  RenderUtil.Render2D.rect(setGripX, setGripY + 1.5F * S, 8.0F * S, 1.0F * S, setGripColor, 0.5F * S);
               }

               Scissor.reset();
               this.drawCosmeticsButton(screenWidth, screenHeight, globalAnim);
               IconSplashEffect.updateAndRender(globalAnim);
               Render2D.endOverlay();
               if (context != null) {
                  context.method_51448().popMatrix();
               }
            }
         }
      }
   }

   private float settingsX(int screenWidth, float guiX, float guiW) {
      float sx = guiX + guiW + 8.0F * S;
      float setW = 150.0F * S + setExtraW;
      if (sx + setW > (float)screenWidth - 8.0F * S) {
         sx = (float)screenWidth - setW - 8.0F * S;
      }

      return sx;
   }

   private void drawCosmeticsButton(int screenWidth, int screenHeight, float globalAnim) {
      float radius = 13.0F * S;
      float bx = (float)screenWidth - radius - 13.0F * S;
      float by = (float)screenHeight - radius - 13.0F * S;
      this.cosmeticsBtnX = bx;
      this.cosmeticsBtnY = by;
      this.cosmeticsBtnR = radius;
      boolean hov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, bx - radius, by - radius, radius * 2.0F, radius * 2.0F);
      this.cosmeticsHover.update();
      this.cosmeticsHover.run(hov ? 1.0 : 0.0, 0.18F, Easings.QUAD_OUT);
      float hp = this.cosmeticsHover.get();
      int accent = ColorUtil.client();
      RenderUtil.Render2D.glow(
         bx - radius, by - radius, radius * 2.0F, radius * 2.0F, ColorUtil.replAlpha(accent, globalAnim * 0.12F * (0.6F + 0.4F * hp)), radius, 8.0F * S, 1.0F
      );
      RenderUtil.Blur.blur(
         bx - radius,
         by - radius,
         radius * 2.0F,
         radius * 2.0F,
         globalAnim * (0.35F + 0.15F * hp),
         10.0F * S,
         ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.background(), 0.8F), globalAnim)
      );
      RenderUtil.Render2D.outline(
         bx - radius - 0.5F * S,
         by - radius - 0.5F * S,
         radius * 2.0F + 1.0F * S,
         radius * 2.0F + 1.0F * S,
         0.5F * S,
         ColorUtil.replAlpha(accent, globalAnim * (0.3F + 0.4F * hp)),
         radius
      );
      float icon = radius * 1.35F;
      float ix = bx - icon / 2.0F;
      float iy = by - icon / 2.0F;
      int col = ColorUtil.replAlpha(ColorUtil.getColor(255), globalAnim * (0.7F + 0.3F * hp));
      RenderUtil.Render2D.rect(ix + icon * 0.45F, iy + icon * 0.04F, icon * 0.1F, icon * 0.1F, col, icon * 0.05F);
      RenderUtil.Render2D.rect(ix + icon * 0.38F, iy + icon * 0.12F, icon * 0.1F, icon * 0.1F, col, icon * 0.05F);

      for (int k = 0; k < 5; k++) {
         float t = (float)k / 5.0F;
         float w = icon * (0.66F - 0.24F * t);
         RenderUtil.Render2D.rect(ix + icon * 0.5F - w / 2.0F, iy + icon * (0.26F + 0.12F * (float)k), w, icon * 0.09F, col, icon * 0.045F);
      }

      RenderUtil.Render2D.rect(ix + icon * 0.24F, iy + icon * 0.88F, icon * 0.52F, icon * 0.09F, col, icon * 0.045F);
   }

   private void drawMenuTelegramButton(int screenWidth, int screenHeight, float globalAnim) {
      float radius = 13.0F * S;
      float bx = (float)screenWidth - radius - 13.0F * S;
      float by = (float)screenHeight - radius - 13.0F * S;
      this.tgBtnX = bx;
      this.tgBtnY = by;
      this.tgBtnR = radius;
      boolean hov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, bx - radius, by - radius, radius * 2.0F, radius * 2.0F);
      this.tgHover.update();
      this.tgHover.run(hov ? 1.0 : 0.0, 0.18F, Easings.QUAD_OUT);
      float hp = this.tgHover.get();
      RenderUtil.Render2D.glow(
         bx - radius,
         by - radius,
         radius * 2.0F,
         radius * 2.0F,
         ColorUtil.replAlpha(ColorUtil.client(), globalAnim * 0.12F * (0.6F + 0.4F * hp)),
         radius,
         8.0F * S,
         1.0F
      );
      RenderUtil.Blur.blur(
         bx - radius,
         by - radius,
         radius * 2.0F,
         radius * 2.0F,
         globalAnim * (0.35F + 0.15F * hp),
         10.0F * S,
         ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.background(), 0.8F), globalAnim)
      );
      RenderUtil.Render2D.outline(
         bx - radius - 0.5F * S,
         by - radius - 0.5F * S,
         radius * 2.0F + 1.0F * S,
         radius * 2.0F + 1.0F * S,
         0.5F * S,
         ColorUtil.replAlpha(ColorUtil.client(), globalAnim * (0.3F + 0.4F * hp)),
         radius
      );
      float iconSize = radius * 1.3F;
      RenderUtil.Images.texture(
         class_2960.method_60655("client", "textures/tg.png"),
         bx - iconSize / 2.0F,
         by - iconSize / 2.0F,
         iconSize,
         iconSize,
         ColorUtil.replAlpha(ColorUtil.getColor(255), globalAnim * (0.7F + 0.3F * hp))
      );
   }

   private void drawModuleCard(Module f, float x, float y, float cardW, float moduleH, float globalAnim, Font draw, Font regular) {
      float canim1 = f.getAnimation14().getOutput();
      f.getAnimation16().setDirection(f.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
      float moduleEnable = f.getAnimation16().getOutput();
      boolean isHover = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, x, y, cardW, moduleH);
      f.getAnimation12().setDirection(isHover ? Direction.FORWARDS : Direction.BACKWARDS);
      float hanim = f.getAnimation12().getOutput();
      f.animation1.setDirection(f == this.select ? Direction.FORWARDS : Direction.BACKWARDS);
      float selectAnim = f.animation1.getOutput();
      boolean isLiquidGlass = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlass();
      boolean isLiquidGlassOld = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlassOld();
      if (isLiquidGlass) {
         float cornerRadius = 6.0F * S;
         RenderUtil.Blur.blur(x, y, cardW, moduleH, 0.75F * globalAnim * canim1, cornerRadius, ColorUtil.getColor(12, 16, 26, 0.18F * globalAnim * canim1));
         int topGlass = ColorUtil.getColor(255, 255, 255, (0.14F + 0.08F * hanim) * globalAnim * canim1);
         int botGlass = ColorUtil.getColor(180, 200, 225, (0.07F + 0.05F * hanim) * globalAnim * canim1);
         RenderUtil.Render2D.gradientRect(x, y, cardW, moduleH, new int[]{topGlass, topGlass, botGlass, botGlass}, cornerRadius);
         if (moduleEnable > 0.01F) {
            RenderUtil.Render2D.glow(
               x,
               y,
               cardW,
               moduleH,
               ColorUtil.multAlpha(ColorUtil.client(), (0.22F + 0.12F * hanim) * globalAnim * canim1 * moduleEnable),
               cornerRadius,
               10.0F * S,
               1.0F
            );
            RenderUtil.Render2D.rect(
               x, y, cardW, moduleH, ColorUtil.multAlpha(ColorUtil.client(), (0.1F + 0.05F * hanim) * globalAnim * canim1 * moduleEnable), cornerRadius
            );
         }

         RenderUtil.Render2D.outline(
            x, y, cardW, moduleH, 0.65F * S, ColorUtil.getColor(255, 255, 255, (0.14F + 0.16F * hanim) * globalAnim * canim1), cornerRadius
         );
         RenderUtil.Render2D.rect(
            x + 2.0F * S, y + 0.5F * S, cardW - 4.0F * S, 0.85F * S, ColorUtil.getColor(255, 255, 255, (0.35F + 0.25F * hanim) * globalAnim * canim1), 0.5F * S
         );
         RenderUtil.Render2D.rect(
            x + 1.0F * S,
            y + 1.5F * S,
            cardW - 2.0F * S,
            Math.min(6.0F * S, moduleH / 3.0F),
            ColorUtil.getColor(255, 255, 255, (0.04F + 0.04F * hanim) * globalAnim * canim1),
            cornerRadius - 1.0F * S
         );
         if (moduleEnable > 0.01F) {
            RenderUtil.Render2D.outline(
               x,
               y,
               cardW,
               moduleH,
               0.65F * S,
               ColorUtil.replAlpha(ColorUtil.client(), (0.4F + 0.25F * hanim) * globalAnim * canim1 * moduleEnable),
               cornerRadius
            );
         }
      } else if (isLiquidGlassOld) {
         RenderUtil.Render2D.rect(
            x,
            y,
            cardW,
            moduleH,
            ColorUtil.overCol(ColorUtil.getColor(18, 24, 38, 0.45F * globalAnim * canim1), ColorUtil.getColor(32, 44, 70, 0.65F * globalAnim * canim1), hanim),
            5.0F * S
         );
         RenderUtil.Render2D.outline(
            x,
            y,
            cardW,
            moduleH,
            0.5F * S,
            ColorUtil.overCol(
               ColorUtil.getColor(255, 255, 255, 0.12F * globalAnim * canim1), ColorUtil.getColor(255, 255, 255, 0.28F * globalAnim * canim1), hanim
            ),
            5.0F * S
         );
         RenderUtil.Render2D.rect(
            x + 1.0F * S,
            y + 1.0F * S,
            cardW - 2.0F * S,
            Math.min(8.0F * S, moduleH / 2.0F),
            ColorUtil.getColor(255, 255, 255, 0.05F * globalAnim * canim1),
            4.0F * S
         );
         RenderUtil.Render2D.outline(
            x - 1.0F * S * moduleEnable + 1.0F * S,
            y - 1.0F * S * moduleEnable + 1.0F * S,
            cardW + 2.0F * S * moduleEnable - 2.0F * S,
            moduleH + 2.0F * S * moduleEnable - 2.0F * S,
            0.5F * S,
            ColorUtil.replAlpha(ColorUtil.client(), globalAnim * canim1 * moduleEnable * (1.0F - 0.3F * hanim)),
            5.0F * S
         );
      } else {
         RenderUtil.Render2D.rect(
            x,
            y,
            cardW,
            moduleH,
            ColorUtil.overCol(ColorUtil.getColor(0, 0.15F * globalAnim * canim1), ColorUtil.getColor(25, 0.3F * globalAnim * canim1), hanim),
            5.0F * S
         );
         RenderUtil.Render2D.outline(
            x - 1.0F * S * moduleEnable + 1.0F * S,
            y - 1.0F * S * moduleEnable + 1.0F * S,
            cardW + 2.0F * S * moduleEnable - 2.0F * S,
            moduleH + 2.0F * S * moduleEnable - 2.0F * S,
            0.5F * S,
            ColorUtil.replAlpha(ColorUtil.client(), globalAnim * canim1 * moduleEnable * (1.0F - 0.3F * hanim)),
            5.0F * S
         );
      }

      RenderUtil.Render2D.rect(
         x + cardW - 16.0F * S - 5.0F * S,
         y + 5.0F * S,
         16.0F * S,
         9.0F * S,
         ColorUtil.overCol(
            ColorUtil.getColor(0, 0.2F * globalAnim * canim1),
            ColorUtil.replAlpha(ColorUtil.client(), globalAnim * canim1 * (1.0F - 0.3F * hanim)),
            moduleEnable
         ),
         4.5F * S
      );
      RenderUtil.Render2D.rect(
         x + cardW - 16.0F * S - 5.0F * S + 2.0F * S + 6.0F * S * moduleEnable,
         y + 5.0F * S + 1.5F * S,
         6.0F * S,
         6.0F * S,
         ColorUtil.getColor(255, globalAnim * (0.3F + 0.7F * moduleEnable) * canim1),
         3.0F * S
      );
      boolean bindingNow = this.bindingModule == f;
      ru.mentions.utils.animation.satoshi.Animation bindAct = this.chipAnim(f.getName() + ":modbind");
      bindAct.setDirection(bindingNow ? Direction.FORWARDS : Direction.BACKWARDS);
      float bindActive = bindAct.getOutput();
      String keyName = bindingNow ? "..." : (f.getKey() == -1 ? "n/a" : Keyboard.keyName(f.getKey()).replace("NONE", "n/a"));
      float keyW = this.smooth(f.getName() + ":modbindw", draw.getWidth(keyName, 6.5F * S) + 10.0F * S);
      float keyX = x + cardW - 16.0F * S - 5.0F * S - 5.0F * S - keyW;
      float keyY = y + 4.0F * S;
      boolean keyHover = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, keyX, keyY, keyW, 11.0F * S);
      ru.mentions.utils.animation.satoshi.Animation bindHov = this.chipAnim(f.getName() + ":modbindhov");
      bindHov.setDirection(keyHover ? Direction.FORWARDS : Direction.BACKWARDS);
      float bindHover = bindHov.getOutput();
      float pulse = bindingNow ? 0.55F + 0.45F * (float)Math.sin((double)((float)(System.currentTimeMillis() % 1000L) / 1000.0F) * Math.PI * 2.0) : 0.0F;
      float bindAccent = Math.max(bindHover * 0.5F, bindActive);
      RenderUtil.Render2D.rect(
         keyX,
         keyY,
         keyW,
         11.0F * S,
         ColorUtil.overCol(
            ColorUtil.getColor(0, (0.12F + 0.13F * bindHover) * globalAnim * canim1),
            ColorUtil.replAlpha(ColorUtil.client(), (0.15F + 0.2F * pulse) * globalAnim * canim1),
            bindActive
         ),
         3.0F * S
      );
      RenderUtil.Render2D.outline(
         keyX,
         keyY,
         keyW,
         11.0F * S,
         0.5F * S,
         ColorUtil.replAlpha(ColorUtil.client(), globalAnim * canim1 * bindAccent * (bindingNow ? 0.4F + 0.6F * pulse : 1.0F)),
         3.0F * S
      );
      RenderUtil.Render2D.glow(
         keyX, keyY, keyW, 11.0F * S, ColorUtil.replAlpha(ColorUtil.client(), 0.12F * globalAnim * canim1 * bindActive * pulse), 3.0F * S, 6.0F, 1.0F
      );
      regular.drawCentered(
         keyName,
         keyX + keyW / 2.0F,
         keyY + 1.5F * S,
         6.5F * S,
         ColorUtil.replAlpha(
            ColorUtil.overCol(ColorUtil.getColor(255), ColorUtil.client(), Math.max(bindActive, moduleEnable)),
            globalAnim * canim1 * (0.35F + 0.35F * moduleEnable + 0.3F * Math.max(bindHover, bindActive))
         )
      );
      Fonts.sf_medium
         .draw(f.getBigName(), x + 6.0F * S, y + 6.0F * S, 8.0F * S, ColorUtil.getColor(255, globalAnim * canim1 * (0.2F + 0.6F * moduleEnable + 0.2F * hanim)));
      Fonts.sf_regular
         .drawWrappedText(
            f.getDesc(),
            x + 6.0F * S,
            y + 6.0F * S + 14.0F * S,
            cardW - 12.0F * S,
            ColorUtil.getColor(255, globalAnim * canim1 * (0.15F + 0.5F * moduleEnable + 0.2F * hanim)),
            6.5F * S
         );
      RenderUtil.Render2D.rect(
         x, y, cardW, moduleH, ColorUtil.replAlpha(ColorUtil.client(), 0.3F * globalAnim * selectAnim * (0.2F + 0.5F * moduleEnable) * canim1), 5.0F * S
      );
   }

   private boolean clickModuleCard(Module f, float x, float y, float cardW, float moduleH, float mouseX, float mouseY, class_11909 click, Font draw) {
      if (MathUtil.isHovered(mouseX, mouseY, x, y, cardW, moduleH)) {
         String keyName = this.bindingModule == f ? "..." : (f.getKey() == -1 ? "n/a" : Keyboard.keyName(f.getKey()).replace("NONE", "n/a"));
         float keyW = draw.getWidth(keyName, 6.5F * S) + 10.0F * S;
         float keyX = x + cardW - 16.0F * S - 5.0F * S - 5.0F * S - keyW;
         if (MathUtil.isHovered(mouseX, mouseY, keyX - 2.0F * S, y + 2.0F * S, keyW + 4.0F * S, 14.0F * S) && click.method_74245() == 0) {
            this.bindingModule = f;
            GuiSounds.bindStart();
            return true;
         } else if (click.method_74245() == 2) {
            this.bindingModule = f;
            GuiSounds.bindStart();
            return true;
         } else {
            if (click.method_74245() == 0) {
               if (f.isAllowDisable()) {
                  f.setEnabled(!f.isEnabled());
               } else {
                  this.select = f == this.select ? null : f;
                  this.settingScrollTarget = 0.0F;
                  this.settingScrollAnim = 0.0F;
                  GuiSounds.expand(this.select == f);
               }
            }

            if (click.method_74245() == 1) {
               this.select = f == this.select ? null : f;
               this.settingScrollTarget = 0.0F;
               this.settingScrollAnim = 0.0F;
               GuiSounds.expand(this.select == f);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private void drawMainGrip(float x, float y, float w, float h, float globalAnim) {
      float gripX = x + w - 14.0F * S;
      float gripY = y + h - 14.0F * S;
      this.mainGripX = gripX;
      this.mainGripY = gripY;
      boolean hovered = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, gripX - 3.0F * S, gripY - 3.0F * S, 16.0F * S, 16.0F * S);
      float alpha = globalAnim * (!hovered && !this.resizingMain ? 0.35F : 0.95F);
      int color = ColorUtil.replAlpha(ColorUtil.client(), alpha);
      RenderUtil.Render2D.rect(gripX + 6.0F * S, gripY + 8.0F * S, 3.5F * S, 1.2F * S, color, 0.6F * S);
      RenderUtil.Render2D.rect(gripX + 3.0F * S, gripY + 5.0F * S, 6.5F * S, 1.2F * S, color, 0.6F * S);
      RenderUtil.Render2D.rect(gripX, gripY + 2.0F * S, 9.5F * S, 1.2F * S, color, 0.6F * S);
   }

   private void startMainResize(float mouseX, float mouseY) {
      this.resizingMain = true;
      this.resizeStartX = mouseX;
      this.resizeStartY = mouseY;
      this.resizeStartExtraW = extraW;
      this.resizeStartExtraH = extraH;
      this.resizeBlurFade = 1.0F;
      GuiSounds.sliderGrab();
   }

   private void selectManagement(ManagementPanel.Tab tab) {
      searchActive = false;
      this.clearSearch();
      this.activeBind = null;
      this.bindingModule = null;
      if (this.activeString != null) {
         this.activeString.set(this.stringBuffer);
      }

      this.activeString = null;
      this.draggingSlider = null;
      this.draggingColor = null;
      this.select = null;
      this.resizingSet = false;
      this.management.select(tab);
      GuiSounds.category(Category.values().length + tab.ordinal(), Category.values().length + 3);
   }

   @Override
   public boolean isSearchActive() {
      return searchActive || this.management.typing();
   }

   @Override
   public void openCosmetics() {
      this.beforeEditorOpen();
      mc.method_1507(new CosmeticsScreen(this));
      GuiSounds.editor();
   }

   @Override
   public void openHandsEditor() {
      this.beforeEditorOpen();
      this.handsEditor.open();
      GuiSounds.editor();
   }

   @Override
   public void openPreviewEditor(Module target) {
      this.beforeEditorOpen();
      PreviewEditor.getInstance().open(target);
      GuiSounds.editor();
   }

   @Override
   public void openCrosshairEditor() {
      this.beforeEditorOpen();
      CrosshairEditor.getInstance().open();
      GuiSounds.editor();
   }

   private void beforeEditorOpen() {
      this.exit = false;
      this.glomalAnim.run(1.0, 0.22F, Easings.EXPO_OUT);
   }

   public void method_25432() {
      this.management.clearFocus();
      OverlayEditors.closeAll();
      super.method_25432();
   }

   private void drawSidebarBtn(
      float bx,
      float by,
      String icon,
      String label,
      boolean isActive,
      ru.mentions.utils.animation.satoshi.Animation activeAnim,
      ru.mentions.utils.animation.satoshi.Animation hoverAnim,
      float S,
      float globalAnim,
      Font draw
   ) {
      float xPanelMini = bx + 16.0F * S / 2.0F - 15.0F * S;
      activeAnim.setDirection(isActive ? Direction.FORWARDS : Direction.BACKWARDS);
      float anim = activeAnim.getOutput();
      boolean isHv = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, bx, by, 16.0F * S, 16.0F * S);
      hoverAnim.setDirection(isHv ? Direction.FORWARDS : Direction.BACKWARDS);
      float animL2 = hoverAnim.getOutput();
      RenderUtil.Render2D.outline(
         bx - 1.0F * S * anim + 1.0F * S,
         by - 1.0F * S * anim + 1.0F * S,
         16.0F * S + 2.0F * S * anim - 2.0F * S,
         16.0F * S + 2.0F * S * anim - 2.0F * S,
         0.5F * S,
         ColorUtil.replAlpha(ColorUtil.client(), globalAnim * anim),
         5.0F * S
      );
      RenderUtil.Render2D.glow(
         bx + 5.8F * S, by + 7.8F * S, 0.1F * S, 0.1F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim * animL2 * 0.15F * anim), 8.0F * S, 9.0F, 1.0F
      );
      Fonts.mentions_2
         .drawCentered(
            icon,
            bx + 8.0F * S,
            by + 4.5F * S,
            8.0F * S,
            ColorUtil.multAlpha(ColorUtil.overCol(ColorUtil.getColor(255, 0.15F * globalAnim + 0.5F * animL2), ColorUtil.client(), anim), globalAnim)
         );
      float xAnimADd = 16.0F * S - 16.0F * S * animL2;
      Scissor.disable();
      RenderUtil.Render2D.glow(
         xPanelMini - draw.getWidth(label, 8.0F * S) - 4.0F * S + xAnimADd,
         by + 7.0F * S - 6.0F * S,
         7.0F * S + draw.getWidth(label, 8.0F * S),
         14.0F * S,
         ColorUtil.multAlpha(ColorUtil.getColor(0), globalAnim * animL2 * 0.1F),
         4.0F * S,
         6.0F,
         1.0F
      );
      RenderUtil.Blur.blur(
         xPanelMini - draw.getWidth(label, 8.0F * S) - 4.0F * S + xAnimADd,
         by + 7.0F * S - 6.0F * S,
         7.0F * S + draw.getWidth(label, 8.0F * S),
         14.0F * S,
         globalAnim * animL2,
         4.0F * S,
         ColorUtil.multAlpha(ColorUtil.background(), globalAnim * animL2 * 0.2F)
      );
      draw.draw(label, xPanelMini - draw.getWidth(label, 8.0F * S) + xAnimADd, by + 9.0F * S - 6.0F * S, 8.0F * S, ColorUtil.getColor(200, globalAnim * animL2));
      Scissor.enable(this.popClipX, this.popClipY, this.popClipW, this.popClipH, 2.0F);
   }

   private float renderThemesTab(float x, float yModule, float w, float listH, float globalAnim, float dt, Font draw, Font regular) {
      float startX = x + 8.0F * S;
      boolean isGlass = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlass();
      draw.draw("Монотонные темы", startX, yModule, 7.5F * S, ColorUtil.getColor(235, globalAnim));
      float curY = yModule + 15.0F * S;
      int cols = 3;
      float cardGap = 6.0F * S;
      float cardW = (w - 16.0F * S - (float)(cols - 1) * cardGap) / (float)cols;
      float cardH = 30.0F * S;
      Theme[] allThemes = Theme.values();
      Theme curActiveTheme = selectedTheme != null ? selectedTheme : Theme.NIGHT;
      boolean isMonotoneActive = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.themeType.is("Монотонные");

      for (int i = 0; i < allThemes.length; i++) {
         Theme theme = allThemes[i];
         int c = i % cols;
         int r = i / cols;
         float cx = startX + (float)c * (cardW + cardGap);
         float cy = curY + (float)r * (cardH + cardGap);
         boolean isSel = isMonotoneActive && theme == curActiveTheme;
         boolean isHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, cx, cy, cardW, cardH);
         int bgCol = isGlass
            ? (
               isSel
                  ? ColorUtil.getColor(35, 45, 68, (int)(200.0F * globalAnim))
                  : (isHov ? ColorUtil.getColor(28, 36, 52, (int)(170.0F * globalAnim)) : ColorUtil.getColor(18, 24, 38, (int)(140.0F * globalAnim)))
            )
            : (
               isSel
                  ? ColorUtil.getColor(36, 38, 48, (int)(220.0F * globalAnim))
                  : (isHov ? ColorUtil.getColor(28, 30, 40, (int)(200.0F * globalAnim)) : ColorUtil.getColor(20, 22, 28, (int)(180.0F * globalAnim)))
            );
         RenderUtil.Render2D.rect(cx, cy, cardW, cardH, bgCol, 5.0F * S);
         if (isSel) {
            RenderUtil.Render2D.outline(cx, cy, cardW, cardH, 1.2F * S, ColorUtil.replAlpha(theme.getClient(), globalAnim), 5.0F * S);
            RenderUtil.Render2D.glow(cx, cy, cardW, cardH, ColorUtil.replAlpha(theme.getClient(), 0.25F * globalAnim), 5.0F * S, 8.0F, 1.0F);
         } else if (isHov) {
            RenderUtil.Render2D.outline(cx, cy, cardW, cardH, 0.6F * S, ColorUtil.getColor(255, 0.18F * globalAnim), 5.0F * S);
         }

         float circleR = 4.0F * S;
         float circleX = cx + 8.0F * S;
         float circleY = cy + cardH / 2.0F;
         RenderUtil.Render2D.rect(
            circleX - circleR, circleY - circleR, circleR * 2.0F, circleR * 2.0F, ColorUtil.replAlpha(theme.getClient(), globalAnim), circleR
         );
         regular.draw(
            RussianUi.text(theme.getName()),
            circleX + circleR * 2.0F + 5.0F * S,
            cy + cardH / 2.0F - 3.5F * S,
            6.5F * S,
            isSel ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(180, globalAnim)
         );
         if (isSel) {
            float indR = 3.0F * S;
            float indX = cx + cardW - 10.0F * S;
            float indY = cy + cardH / 2.0F;
            RenderUtil.Render2D.rect(indX - indR, indY - indR, indR * 2.0F, indR * 2.0F, ColorUtil.replAlpha(theme.getClient(), globalAnim), indR);
            RenderUtil.Render2D.glow(
               indX - indR, indY - indR, indR * 2.0F, indR * 2.0F, ColorUtil.replAlpha(theme.getClient(), 0.5F * globalAnim), indR, 6.0F, 1.0F
            );
         }
      }

      int rows = (allThemes.length + cols - 1) / cols;
      curY += (float)rows * (cardH + cardGap) + 14.0F * S;
      draw.draw("Градиентные темы", startX, curY, 7.5F * S, ColorUtil.getColor(235, globalAnim));
      curY += 15.0F * S;
      List<ClientTheme.GradientPreset> presets = ClientTheme.GRADIENT_PRESETS;
      boolean isGradActive = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.themeType.is("Градиентные");
      String curGradName = ClientTheme.INSTANCE != null ? ClientTheme.INSTANCE.gradientTheme.getValue() : "";

      for (int i = 0; i < presets.size(); i++) {
         ClientTheme.GradientPreset preset = presets.get(i);
         int cxx = i % cols;
         int rx = i / cols;
         float cxxx = startX + (float)cxx * (cardW + cardGap);
         float cyx = curY + (float)rx * (cardH + cardGap);
         boolean isSelx = isGradActive && preset.name().equals(curGradName);
         boolean isHovx = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, cxxx, cyx, cardW, cardH);
         int bgColx = isGlass
            ? (
               isSelx
                  ? ColorUtil.getColor(35, 45, 68, (int)(200.0F * globalAnim))
                  : (isHovx ? ColorUtil.getColor(28, 36, 52, (int)(170.0F * globalAnim)) : ColorUtil.getColor(18, 24, 38, (int)(140.0F * globalAnim)))
            )
            : (
               isSelx
                  ? ColorUtil.getColor(36, 38, 48, (int)(220.0F * globalAnim))
                  : (isHovx ? ColorUtil.getColor(28, 30, 40, (int)(200.0F * globalAnim)) : ColorUtil.getColor(20, 22, 28, (int)(180.0F * globalAnim)))
            );
         RenderUtil.Render2D.rect(cxxx, cyx, cardW, cardH, bgColx, 5.0F * S);
         if (isSelx) {
            RenderUtil.Render2D.outline(cxxx, cyx, cardW, cardH, 1.2F * S, ColorUtil.replAlpha(0xFF000000 | preset.color1(), globalAnim), 5.0F * S);
            RenderUtil.Render2D.glow(cxxx, cyx, cardW, cardH, ColorUtil.replAlpha(0xFF000000 | preset.color1(), 0.25F * globalAnim), 5.0F * S, 8.0F, 1.0F);
         } else if (isHovx) {
            RenderUtil.Render2D.outline(cxxx, cyx, cardW, cardH, 0.6F * S, ColorUtil.getColor(255, 0.18F * globalAnim), 5.0F * S);
         }

         float barW = 12.0F * S;
         float barH = 14.0F * S;
         float barX = cxxx + 8.0F * S;
         float barY = cyx + (cardH - barH) / 2.0F;
         RenderUtil.Render2D.rect(barX, barY, barW, barH, 0xFF000000 | preset.color1(), 3.0F * S);
         RenderUtil.Render2D.rect(barX + barW / 2.0F, barY, barW / 2.0F, barH, 0xFF000000 | preset.color2(), 2.0F * S);
         regular.draw(
            preset.russianName(),
            barX + barW + 6.0F * S,
            cyx + cardH / 2.0F - 3.5F * S,
            6.5F * S,
            isSelx ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(180, globalAnim)
         );
         if (isSelx) {
            float indR = 3.0F * S;
            float indX = cxxx + cardW - 10.0F * S;
            float indY = cyx + cardH / 2.0F;
            RenderUtil.Render2D.rect(indX - indR, indY - indR, indR * 2.0F, indR * 2.0F, ColorUtil.replAlpha(0xFF000000 | preset.color1(), globalAnim), indR);
            RenderUtil.Render2D.glow(
               indX - indR, indY - indR, indR * 2.0F, indR * 2.0F, ColorUtil.replAlpha(0xFF000000 | preset.color1(), 0.5F * globalAnim), indR, 6.0F, 1.0F
            );
         }
      }

      int gradRows = (presets.size() + cols - 1) / cols;
      curY += (float)gradRows * (cardH + cardGap) + 14.0F * S;
      draw.draw("Кастомная тема", startX, curY, 7.5F * S, ColorUtil.getColor(235, globalAnim));
      curY += 15.0F * S;
      boolean isCustomActive = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.themeType.is("Кастомная");
      float customCardH = 34.0F * S;
      float customFullW = w - 16.0F * S;
      boolean isCustomHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, startX, curY, customFullW, customCardH);
      int customBg = isGlass
         ? (
            isCustomActive
               ? ColorUtil.getColor(35, 45, 68, (int)(200.0F * globalAnim))
               : (isCustomHov ? ColorUtil.getColor(28, 36, 52, (int)(170.0F * globalAnim)) : ColorUtil.getColor(18, 24, 38, (int)(140.0F * globalAnim)))
         )
         : (
            isCustomActive
               ? ColorUtil.getColor(36, 38, 48, (int)(220.0F * globalAnim))
               : (isCustomHov ? ColorUtil.getColor(28, 30, 40, (int)(200.0F * globalAnim)) : ColorUtil.getColor(20, 22, 28, (int)(180.0F * globalAnim)))
         );
      RenderUtil.Render2D.rect(startX, curY, customFullW, customCardH, customBg, 6.0F * S);
      if (isCustomActive) {
         int c1 = ClientTheme.INSTANCE != null ? ClientTheme.INSTANCE.customColor1.getValue() : ColorUtil.client();
         RenderUtil.Render2D.outline(startX, curY, customFullW, customCardH, 1.2F * S, ColorUtil.replAlpha(c1, globalAnim), 6.0F * S);
         RenderUtil.Render2D.glow(startX, curY, customFullW, customCardH, ColorUtil.replAlpha(c1, 0.25F * globalAnim), 6.0F * S, 8.0F, 1.0F);
      } else if (isCustomHov) {
         RenderUtil.Render2D.outline(startX, curY, customFullW, customCardH, 0.6F * S, ColorUtil.getColor(255, 0.18F * globalAnim), 6.0F * S);
      }

      int cColor1 = ClientTheme.INSTANCE != null ? ClientTheme.INSTANCE.customColor1.getValue() : -8613898;
      int cColor2 = ClientTheme.INSTANCE != null ? ClientTheme.INSTANCE.customColor2.getValue() : -623490;
      boolean isCustomGrad = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.customMode.is("Градиент");
      float cBarW = 16.0F * S;
      float cBarH = 16.0F * S;
      float cBarX = startX + 10.0F * S;
      float cBarY = curY + (customCardH - cBarH) / 2.0F;
      RenderUtil.Render2D.rect(cBarX, cBarY, cBarW, cBarH, cColor1, 4.0F * S);
      if (isCustomGrad) {
         RenderUtil.Render2D.rect(cBarX + cBarW / 2.0F, cBarY, cBarW / 2.0F, cBarH, cColor2, 3.0F * S);
      }

      regular.draw(
         "Своя цветовая палитра",
         cBarX + cBarW + 8.0F * S,
         curY + customCardH / 2.0F - 3.5F * S,
         7.0F * S,
         isCustomActive ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(200, globalAnim)
      );
      float cBtnW = 60.0F * S;
      float cBtnH = 18.0F * S;
      float cBtnX = startX + customFullW - cBtnW - 8.0F * S;
      float cBtnY = curY + (customCardH - cBtnH) / 2.0F;
      boolean isCBtnHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, cBtnX, cBtnY, cBtnW, cBtnH);
      RenderUtil.Render2D.rect(
         cBtnX,
         cBtnY,
         cBtnW,
         cBtnH,
         isCustomActive
            ? ColorUtil.replAlpha(ColorUtil.client(), 0.85F * globalAnim)
            : (isCBtnHov ? ColorUtil.getColor(45, 52, 68, (int)(180.0F * globalAnim)) : ColorUtil.getColor(35, 40, 50, (int)(160.0F * globalAnim))),
         4.0F * S
      );
      draw.drawCentered(
         isCustomActive ? "Выбрано" : "Выбрать",
         cBtnX + cBtnW / 2.0F,
         cBtnY + 5.5F * S,
         6.0F * S,
         isCustomActive ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(180, globalAnim)
      );
      curY += customCardH + 14.0F * S;
      draw.draw("Настройки оформления", startX, curY, 7.5F * S, ColorUtil.getColor(235, globalAnim));
      curY += 15.0F * S;
      float fullW = w - 16.0F * S;
      float optH = 34.0F * S;
      int optBg = isGlass ? ColorUtil.getColor(20, 26, 40, (int)(150.0F * globalAnim)) : ColorUtil.getColor(20, 22, 28, (int)(180.0F * globalAnim));
      RenderUtil.Render2D.rect(startX, curY, fullW, optH, optBg, 6.0F * S);
      RenderUtil.Render2D.outline(startX, curY, fullW, optH, 0.5F * S, ColorUtil.getColor(255, 0.08F * globalAnim), 6.0F * S);
      regular.draw("Стиль UI", startX + 10.0F * S, curY + optH / 2.0F - 3.5F * S, 7.0F * S, ColorUtil.getColor(230, globalAnim));
      float modeBtnW = 66.0F * S;
      float modeBtnH = 18.0F * S;
      float bOldX = startX + fullW - modeBtnW - 8.0F * S;
      float bGlassX = bOldX - modeBtnW - 4.0F * S;
      float bNormX = bGlassX - modeBtnW - 4.0F * S;
      float bY = curY + (optH - modeBtnH) / 2.0F;
      boolean isGlassOld = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlassOld();
      boolean isNormSel = !isGlass && !isGlassOld;
      RenderUtil.Render2D.rect(
         bNormX,
         bY,
         modeBtnW,
         modeBtnH,
         isNormSel ? ColorUtil.replAlpha(ColorUtil.client(), 0.8F * globalAnim) : ColorUtil.getColor(35, 40, 50, (int)(180.0F * globalAnim)),
         4.0F * S
      );
      draw.drawCentered(
         "Обычный", bNormX + modeBtnW / 2.0F, bY + 5.5F * S, 6.0F * S, isNormSel ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(170, globalAnim)
      );
      RenderUtil.Render2D.rect(
         bGlassX,
         bY,
         modeBtnW,
         modeBtnH,
         isGlass ? ColorUtil.replAlpha(ColorUtil.client(), 0.8F * globalAnim) : ColorUtil.getColor(35, 40, 50, (int)(180.0F * globalAnim)),
         4.0F * S
      );
      draw.drawCentered(
         "Liquid Glass",
         bGlassX + modeBtnW / 2.0F,
         bY + 5.5F * S,
         6.0F * S,
         isGlass ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(170, globalAnim)
      );
      RenderUtil.Render2D.rect(
         bOldX,
         bY,
         modeBtnW,
         modeBtnH,
         isGlassOld ? ColorUtil.replAlpha(ColorUtil.client(), 0.8F * globalAnim) : ColorUtil.getColor(35, 40, 50, (int)(180.0F * globalAnim)),
         4.0F * S
      );
      draw.drawCentered(
         "Liquid Old", bOldX + modeBtnW / 2.0F, bY + 5.5F * S, 6.0F * S, isGlassOld ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(170, globalAnim)
      );
      curY += optH + 6.0F * S;
      boolean blurVal = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.uiBlur.getValue();
      RenderUtil.Render2D.rect(startX, curY, fullW, optH, optBg, 6.0F * S);
      RenderUtil.Render2D.outline(startX, curY, fullW, optH, 0.5F * S, ColorUtil.getColor(255, 0.08F * globalAnim), 6.0F * S);
      regular.draw("Размытие фона UI", startX + 10.0F * S, curY + optH / 2.0F - 3.5F * S, 7.0F * S, ColorUtil.getColor(230, globalAnim));
      float togW = 28.0F * S;
      float togH = 14.0F * S;
      float togX = startX + fullW - togW - 10.0F * S;
      float togY = curY + (optH - togH) / 2.0F;
      RenderUtil.Render2D.rect(
         togX,
         togY,
         togW,
         togH,
         blurVal ? ColorUtil.replAlpha(ColorUtil.client(), 0.85F * globalAnim) : ColorUtil.getColor(40, 44, 55, (int)(180.0F * globalAnim)),
         togH / 2.0F
      );
      float circleDotX = blurVal ? togX + togW - togH / 2.0F : togX + togH / 2.0F;
      RenderUtil.Render2D.rect(
         circleDotX - (togH - 4.0F * S) / 2.0F,
         togY + 2.0F * S,
         togH - 4.0F * S,
         togH - 4.0F * S,
         ColorUtil.getColor(255, globalAnim),
         (togH - 4.0F * S) / 2.0F
      );
      curY += optH + 6.0F * S;
      boolean soundsVal = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.uiSounds.getValue();
      RenderUtil.Render2D.rect(startX, curY, fullW, optH, optBg, 6.0F * S);
      RenderUtil.Render2D.outline(startX, curY, fullW, optH, 0.5F * S, ColorUtil.getColor(255, 0.08F * globalAnim), 6.0F * S);
      regular.draw("Звуки интерфейса", startX + 10.0F * S, curY + optH / 2.0F - 3.5F * S, 7.0F * S, ColorUtil.getColor(230, globalAnim));
      float tog2X = startX + fullW - togW - 10.0F * S;
      float tog2Y = curY + (optH - togH) / 2.0F;
      RenderUtil.Render2D.rect(
         tog2X,
         tog2Y,
         togW,
         togH,
         soundsVal ? ColorUtil.replAlpha(ColorUtil.client(), 0.85F * globalAnim) : ColorUtil.getColor(40, 44, 55, (int)(180.0F * globalAnim)),
         togH / 2.0F
      );
      float circleDot2X = soundsVal ? tog2X + togW - togH / 2.0F : tog2X + togH / 2.0F;
      RenderUtil.Render2D.rect(
         circleDot2X - (togH - 4.0F * S) / 2.0F,
         tog2Y + 2.0F * S,
         togH - 4.0F * S,
         togH - 4.0F * S,
         ColorUtil.getColor(255, globalAnim),
         (togH - 4.0F * S) / 2.0F
      );
      return curY + optH + 12.0F * S;
   }

   private void renderSettingsTab(float x, float yModule, float w, float listH, float globalAnim, float dt, Font draw, Font regular) {
      float startX = x + 8.0F * S;
      float cardW = w - 16.0F * S;
      boolean isGlass = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isLiquidGlass();
      String headerConfigs = "Конфигурации клиента";
      draw.draw(headerConfigs, startX, yModule, 7.5F * S, ColorUtil.getColor(230, globalAnim));
      float curY = yModule + 15.0F * S;
      float configCardH = 40.0F * S;
      boolean isConfigCardHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, startX, curY, cardW, configCardH);
      int cardBg = isGlass
         ? (isConfigCardHov ? ColorUtil.getColor(35, 45, 68, (int)(180.0F * globalAnim)) : ColorUtil.getColor(20, 26, 40, (int)(140.0F * globalAnim)))
         : (isConfigCardHov ? ColorUtil.getColor(28, 30, 40, (int)(200.0F * globalAnim)) : ColorUtil.getColor(20, 22, 28, (int)(180.0F * globalAnim)));
      RenderUtil.Render2D.rect(startX, curY, cardW, configCardH, cardBg, 6.0F * S);
      if (isGlass) {
         RenderUtil.Render2D.outline(
            startX, curY, cardW, configCardH, 0.7F * S, ColorUtil.getColor(255, (isConfigCardHov ? 0.35F : 0.18F) * globalAnim), 6.0F * S
         );
         RenderUtil.Render2D.rect(startX + 1.0F * S, curY + 1.0F * S, cardW - 2.0F * S, 10.0F * S, ColorUtil.getColor(255, 0.05F * globalAnim), 5.0F * S);
      } else {
         RenderUtil.Render2D.outline(
            startX, curY, cardW, configCardH, 0.5F * S, ColorUtil.getColor(255, (isConfigCardHov ? 0.18F : 0.08F) * globalAnim), 6.0F * S
         );
      }

      draw.draw("Папка с конфигами", startX + 10.0F * S, curY + 9.0F * S, 7.0F * S, ColorUtil.getColor(245, globalAnim));
      Path cfgPath = Client.get().configManager().getConfigDir();
      String pathStr = cfgPath != null ? cfgPath.toString() : "C:/lunarix/client1_21_11/config";
      Fonts.sf_regular.draw(pathStr, startX + 10.0F * S, curY + 23.0F * S, 6.0F * S, ColorUtil.getColor(160, globalAnim));
      float btnW = 74.0F * S;
      float btnH = 20.0F * S;
      float btnFolderX = startX + cardW - btnW - 10.0F * S;
      float btnFolderY = curY + (configCardH - btnH) / 2.0F;
      boolean isBtnHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, btnFolderX, btnFolderY, btnW, btnH);
      RenderUtil.Render2D.rect(
         btnFolderX,
         btnFolderY,
         btnW,
         btnH,
         isBtnHov ? ColorUtil.replAlpha(ColorUtil.client(), 0.85F * globalAnim) : ColorUtil.replAlpha(ColorUtil.client(), 0.6F * globalAnim),
         4.0F * S
      );
      if (isBtnHov) {
         RenderUtil.Render2D.glow(btnFolderX, btnFolderY, btnW, btnH, ColorUtil.replAlpha(ColorUtil.client(), 0.3F * globalAnim), 4.0F * S, 8.0F, 1.0F);
      }

      Fonts.sf_bold.drawCentered("Открыть", btnFolderX + btnW / 2.0F, btnFolderY + 5.5F * S, 6.5F * S, ColorUtil.getColor(255, globalAnim));
      curY += configCardH + 16.0F * S;
      String headerStyle = "Стиль интерфейса (UI Style)";
      draw.draw(headerStyle, startX, curY, 7.5F * S, ColorUtil.getColor(230, globalAnim));
      curY += 15.0F * S;
      float styleW = (cardW - 8.0F * S) / 2.0F;
      float styleH = 44.0F * S;
      boolean s1Hov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, startX, curY, styleW, styleH);
      boolean s1Sel = !isGlass;
      int s1Bg = s1Sel
         ? ColorUtil.getColor(36, 40, 52, (int)(220.0F * globalAnim))
         : (s1Hov ? ColorUtil.getColor(28, 30, 40, (int)(190.0F * globalAnim)) : ColorUtil.getColor(20, 22, 28, (int)(170.0F * globalAnim)));
      RenderUtil.Render2D.rect(startX, curY, styleW, styleH, s1Bg, 6.0F * S);
      if (s1Sel) {
         RenderUtil.Render2D.outline(startX, curY, styleW, styleH, 1.2F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim), 6.0F * S);
         RenderUtil.Render2D.glow(startX, curY, styleW, styleH, ColorUtil.replAlpha(ColorUtil.client(), 0.18F * globalAnim), 6.0F * S, 8.0F, 1.0F);
      } else if (s1Hov) {
         RenderUtil.Render2D.outline(startX, curY, styleW, styleH, 0.6F * S, ColorUtil.getColor(255, 0.15F * globalAnim), 6.0F * S);
      }

      draw.draw("Обычный", startX + 10.0F * S, curY + 9.0F * S, 7.5F * S, s1Sel ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(190, globalAnim));
      Fonts.sf_regular.draw("Классический плоский дизайн", startX + 10.0F * S, curY + 24.0F * S, 6.0F * S, ColorUtil.getColor(140, globalAnim));
      float s2X = startX + styleW + 8.0F * S;
      boolean s2Hov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, s2X, curY, styleW, styleH);
      int s2Bg = isGlass
         ? ColorUtil.getColor(40, 54, 82, (int)(220.0F * globalAnim))
         : (s2Hov ? ColorUtil.getColor(26, 36, 56, (int)(190.0F * globalAnim)) : ColorUtil.getColor(18, 24, 38, (int)(170.0F * globalAnim)));
      RenderUtil.Render2D.rect(s2X, curY, styleW, styleH, s2Bg, 6.0F * S);
      if (isGlass) {
         RenderUtil.Render2D.outline(s2X, curY, styleW, styleH, 1.2F * S, ColorUtil.replAlpha(ColorUtil.client(), globalAnim), 6.0F * S);
         RenderUtil.Render2D.glow(s2X, curY, styleW, styleH, ColorUtil.replAlpha(ColorUtil.client(), 0.25F * globalAnim), 6.0F * S, 10.0F, 1.0F);
         RenderUtil.Render2D.rect(s2X + 1.0F * S, curY + 1.0F * S, styleW - 2.0F * S, 12.0F * S, ColorUtil.getColor(255, 0.08F * globalAnim), 5.0F * S);
      } else if (s2Hov) {
         RenderUtil.Render2D.outline(s2X, curY, styleW, styleH, 0.6F * S, ColorUtil.getColor(255, 0.2F * globalAnim), 6.0F * S);
      }

      draw.draw("Liquid Glass", s2X + 10.0F * S, curY + 9.0F * S, 7.5F * S, isGlass ? ColorUtil.getColor(255, globalAnim) : ColorUtil.getColor(190, globalAnim));
      Fonts.sf_regular.draw("Жидкое стекло, глянец, блюр", s2X + 10.0F * S, curY + 24.0F * S, 6.0F * S, ColorUtil.getColor(160, globalAnim));
      curY += styleH + 16.0F * S;
      String headerOptions = "Параметры графики и звука";
      draw.draw(headerOptions, startX, curY, 7.5F * S, ColorUtil.getColor(230, globalAnim));
      curY += 15.0F * S;
      float toggleH = 32.0F * S;
      float swW = 24.0F * S;
      float swH = 12.0F * S;
      boolean whiteVal = ClientTheme.INSTANCE != null && ClientTheme.INSTANCE.isWhiteTheme();
      boolean blurVal = ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiBlur.getValue();
      boolean sndVal = ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiSounds.getValue();
      boolean wHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, startX, curY, cardW, toggleH);
      RenderUtil.Render2D.rect(
         startX,
         curY,
         cardW,
         toggleH,
         wHov ? ColorUtil.getColor(26, 30, 42, (int)(190.0F * globalAnim)) : ColorUtil.getColor(20, 22, 30, (int)(170.0F * globalAnim)),
         6.0F * S
      );
      RenderUtil.Render2D.outline(startX, curY, cardW, toggleH, 0.5F * S, ColorUtil.getColor(255, (wHov ? 0.2F : 0.08F) * globalAnim), 6.0F * S);
      draw.draw("Белая основа (Светлая тема)", startX + 10.0F * S, curY + 11.5F * S, 7.0F * S, ColorUtil.getColor(230, globalAnim));
      float sw0X = startX + cardW - swW - 12.0F * S;
      float sw0Y = curY + (toggleH - swH) / 2.0F;
      RenderUtil.Render2D.rect(
         sw0X,
         sw0Y,
         swW,
         swH,
         whiteVal ? ColorUtil.replAlpha(ColorUtil.client(), 0.8F * globalAnim) : ColorUtil.getColor(50, 52, 60, (int)(200.0F * globalAnim)),
         6.0F * S
      );
      float dot0X = whiteVal ? sw0X + swW - 11.0F * S : sw0X + 2.0F * S;
      RenderUtil.Render2D.rect(dot0X, sw0Y + 1.5F * S, 9.0F * S, 9.0F * S, ColorUtil.getColor(255, globalAnim), 4.5F * S);
      curY += toggleH + 6.0F * S;
      boolean bHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, startX, curY, cardW, toggleH);
      RenderUtil.Render2D.rect(
         startX,
         curY,
         cardW,
         toggleH,
         bHov ? ColorUtil.getColor(26, 30, 42, (int)(190.0F * globalAnim)) : ColorUtil.getColor(20, 22, 30, (int)(170.0F * globalAnim)),
         6.0F * S
      );
      RenderUtil.Render2D.outline(startX, curY, cardW, toggleH, 0.5F * S, ColorUtil.getColor(255, (bHov ? 0.2F : 0.08F) * globalAnim), 6.0F * S);
      draw.draw("Размытие фона меню (UI Blur)", startX + 10.0F * S, curY + 11.5F * S, 7.0F * S, ColorUtil.getColor(230, globalAnim));
      float swX = startX + cardW - swW - 12.0F * S;
      float swY = curY + (toggleH - swH) / 2.0F;
      RenderUtil.Render2D.rect(
         swX,
         swY,
         swW,
         swH,
         blurVal ? ColorUtil.replAlpha(ColorUtil.client(), 0.8F * globalAnim) : ColorUtil.getColor(50, 52, 60, (int)(200.0F * globalAnim)),
         6.0F * S
      );
      float dotX = blurVal ? swX + swW - 11.0F * S : swX + 2.0F * S;
      RenderUtil.Render2D.rect(dotX, swY + 1.5F * S, 9.0F * S, 9.0F * S, ColorUtil.getColor(255, globalAnim), 4.5F * S);
      curY += toggleH + 6.0F * S;
      boolean sHov = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, startX, curY, cardW, toggleH);
      RenderUtil.Render2D.rect(
         startX,
         curY,
         cardW,
         toggleH,
         sHov ? ColorUtil.getColor(26, 30, 42, (int)(190.0F * globalAnim)) : ColorUtil.getColor(20, 22, 30, (int)(170.0F * globalAnim)),
         6.0F * S
      );
      RenderUtil.Render2D.outline(startX, curY, cardW, toggleH, 0.5F * S, ColorUtil.getColor(255, (sHov ? 0.2F : 0.08F) * globalAnim), 6.0F * S);
      draw.draw("Звуки интерфейса (UI Sounds)", startX + 10.0F * S, curY + 11.5F * S, 7.0F * S, ColorUtil.getColor(230, globalAnim));
      float sw2X = startX + cardW - swW - 12.0F * S;
      float sw2Y = curY + (toggleH - swH) / 2.0F;
      RenderUtil.Render2D.rect(
         sw2X,
         sw2Y,
         swW,
         swH,
         sndVal ? ColorUtil.replAlpha(ColorUtil.client(), 0.8F * globalAnim) : ColorUtil.getColor(50, 52, 60, (int)(200.0F * globalAnim)),
         6.0F * S
      );
      float dot2X = sndVal ? sw2X + swW - 11.0F * S : sw2X + 2.0F * S;
      RenderUtil.Render2D.rect(dot2X, sw2Y + 1.5F * S, 9.0F * S, 9.0F * S, ColorUtil.getColor(255, globalAnim), 4.5F * S);
   }

   private void closeCheck() {
      if (this.exit && this.glomalAnim.isFinished()) {
         this.method_25419();
         GuiMusicPlayer.stop();
         this.exit = false;
      }
   }

   private void onLogoClick() {
      long now = System.currentTimeMillis();
      if (now - this.lastLogoClickMs > 1500L) {
         this.logoClicks = 0;
      }

      this.lastLogoClickMs = now;
      this.logoClicks++;
      if (this.logoClicks >= 3) {
         this.logoClicks = 0;
         mc.method_1507(new ScreamerScreen(this));
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float mouseX = (float)((int)(click.comp_4798() / (double)this.scaleFix));
      float mouseY = (float)((int)(click.comp_4799() / (double)this.scaleFix));
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         return editor.mouseClicked(mouseX, mouseY, click.method_74245());
      } else if (this.exit) {
         return true;
      } else if (this.management.hasConfirmation()) {
         return this.management.click(mouseX, mouseY, click.method_74245());
      } else if (this.management.clickNavigation(mouseX, mouseY, click.method_74245())) {
         return true;
      } else if (this.management.active()
         && click.method_74245() == 0
         && MathUtil.isHovered(mouseX, mouseY, this.mainGripX - 4.0F * S, this.mainGripY - 4.0F * S, 20.0F * S, 20.0F * S)) {
         this.startMainResize(mouseX, mouseY);
         return true;
      } else if (this.management.active() && this.management.click(mouseX, mouseY, click.method_74245())) {
         return true;
      } else if (click.method_74245() == 0
         && MathUtil.isHovered(
            mouseX,
            mouseY,
            this.cosmeticsBtnX - this.cosmeticsBtnR,
            this.cosmeticsBtnY - this.cosmeticsBtnR,
            this.cosmeticsBtnR * 2.0F,
            this.cosmeticsBtnR * 2.0F
         )) {
         this.openCosmetics();
         return true;
      } else if (this.bindingModule != null) {
         this.bindingModule = null;
         GuiSounds.bindReset();
         return true;
      } else if (this.activeBind != null) {
         if (this.activeBind.allowMouse && click.method_74245() != 0 && click.method_74245() != 1) {
            this.activeBind.set(click.method_74245());
            GuiSounds.bindSet();
         } else {
            GuiSounds.bindReset();
         }

         this.activeBind = null;
         if (Client.get() != null && Client.get().configManager() != null) {
            Client.get().configManager().autoSave();
         }
         return true;
      } else {
         if (this.activeString != null) {
            this.activeString.set(this.stringBuffer);
            this.activeString = null;
            GuiSounds.editCommit();
         }

         int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
         int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
         float w = 380.0F * S + extraW;
         float h = 280.0F * S + extraH;
         float x = (float)screenWidth / 2.0F - w / 2.0F + guiOffsetX;
         float y = (float)screenHeight / 2.0F - h / 2.0F + guiOffsetY;
         float setW = 150.0F * S + setExtraW;
         float setH = 130.0F * S + setExtraH;
         float setX = this.settingsX(screenWidth, x, w);
         if (!this.management.active() && this.active == Category.PASTED && !this.searching() && click.method_74245() == 0) {
            Font draw = Fonts.sf_medium;
            float catPanX = x + 6.0F * S + 36.0F * S;
            float subX = catPanX + 68.0F * S;
            float subY = y + 9.5F * S;
            float subH = 13.0F * S;

            for (int i = 0; i < PASTED_SUBTABS.length; i++) {
               float txtW = draw.getWidth(PASTED_SUBTABS[i], 6.5F * S);
               float subW = txtW + 12.0F * S;
               if (MathUtil.isHovered(mouseX, mouseY, subX, subY, subW, subH)) {
                  if (this.pastedSubTab != i) {
                     this.pastedSubTab = i;
                     this.scrollTarget = 0.0F;
                     this.scrollVel = 0.0F;
                     GuiSounds.category(i, PASTED_SUBTABS.length);
                  }

                  return true;
               }

               subX += subW + 4.0F * S;
            }
         }

         if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, x + 36.0F * S, y, w - 50.0F * S, 26.0F * S)) {
            this.draggingMain = true;
            this.dragStartX = mouseX - guiOffsetX;
            this.dragStartY = mouseY - guiOffsetY;
            GuiSounds.sliderGrab();
            return true;
         } else if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, x + w - 18.0F * S, y + h - 18.0F * S, 20.0F * S, 20.0F * S)) {
            this.startMainResize(mouseX, mouseY);
            return true;
         } else if (click.method_74245() == 0
            && this.select != null
            && MathUtil.isHovered(mouseX, mouseY, setX + setW - 16.0F * S, y + setH - 16.0F * S, 18.0F * S, 18.0F * S)) {
            this.resizingSet = true;
            this.resizeStartX = mouseX;
            this.resizeStartY = mouseY;
            this.resizeStartExtraW = setExtraW;
            this.resizeStartExtraH = setExtraH;
            this.setResizeBlurFade = 1.0F;
            GuiSounds.sliderGrab();
            return true;
         } else {
            float ht = 20.0F * S;
            float wt = 78.0F * S;
            float xt = x + 6.0F * S;
            float yt = y + h + 6.0F * S;
            if (!this.management.active() && MathUtil.isHovered(mouseX, mouseY, xt, yt, 140.0F * S, 20.0F * S)) {
               if (click.method_74245() == 1) {
                  this.clearSearch();
                  searchActive = false;
                  GuiSounds.searchClear();
               } else if (click.method_74245() == 0) {
                  if (!searchActive) {
                     GuiSounds.editStart();
                  }

                  searchActive = true;
                  this.searchTypeTime = System.currentTimeMillis();
               }

               return true;
            } else {
               searchActive = false;
               float xPanelMini = x + 6.0F * S;
               float yPanelMini = y + 6.0F * S;
               float logoCenterX = xPanelMini + 30.0F * S / 2.0F;
               float logoCenterY = yPanelMini + 8.05F * S;
               if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, logoCenterX - 10.0F * S, logoCenterY - 6.0F * S, 20.0F * S, 16.0F * S)) {
                  IconSplashEffect.spawn(logoCenterX, logoCenterY, 20, ColorUtil.client());
                  GuiSounds.click();
                  this.onLogoClick();
                  return true;
               } else {
                  Category[] mainCats = new Category[]{Category.COMBAT, Category.MOVEMENT, Category.PLAYER, Category.OTHER, Category.RENDER, Category.PASTED};
                  float f = yPanelMini + 26.0F * S;
                  int catIndex = 0;

                  for (Category category : mainCats) {
                     if (MathUtil.isHovered(mouseX, mouseY, xPanelMini + 30.0F * S / 2.0F - 16.0F * S / 2.0F, f, 16.0F * S, 16.0F * S)
                        && click.method_74245() == 0
                        && (this.active != category || this.searching() || this.management.active())) {
                        this.management.close();
                        this.clearSearch();
                        if (this.active == Category.THEMES && this.select instanceof ClientTheme) {
                           this.select = null;
                        }

                        if (this.active != category) {
                           this.catDir = Integer.compare(category.ordinal(), this.active.ordinal());
                           this.active = category;
                           animCategoryReset.reset();
                        }

                        this.scrollTarget = 0.0F;
                        this.scrollVel = 0.0F;
                        GuiSounds.category(catIndex, mainCats.length);
                        return true;
                     }

                     f += 20.0F * S;
                     catIndex++;
                  }

                  float bottomH = 120.0F * S;
                  float bottomStartY = yPanelMini + (h - 12.0F * S) - bottomH - 4.0F * S;
                  float btnX = xPanelMini + 30.0F * S / 2.0F - 16.0F * S / 2.0F;
                  if (MathUtil.isHovered(mouseX, mouseY, btnX, bottomStartY, 16.0F * S, 16.0F * S) && click.method_74245() == 0) {
                     this.management.close();
                     this.clearSearch();
                     this.active = Category.THEMES;
                     this.select = null;
                     this.settingScrollTarget = 0.0F;
                     this.settingScrollAnim = 0.0F;
                     this.scrollTarget = 0.0F;
                     this.scrollVel = 0.0F;
                     animCategoryReset.reset();
                     GuiSounds.category(5, 7);
                     return true;
                  } else if (MathUtil.isHovered(mouseX, mouseY, btnX, bottomStartY + 20.0F * S, 16.0F * S, 16.0F * S) && click.method_74245() == 0) {
                     this.clearSearch();
                     this.selectManagement(ManagementPanel.Tab.EVENTS);
                     GuiSounds.category(1, 4);
                     return true;
                  } else if (MathUtil.isHovered(mouseX, mouseY, btnX, bottomStartY + 40.0F * S, 16.0F * S, 16.0F * S) && click.method_74245() == 0) {
                     this.clearSearch();
                     this.selectManagement(ManagementPanel.Tab.CONFIGS);
                     GuiSounds.category(0, 3);
                     return true;
                  } else if (MathUtil.isHovered(mouseX, mouseY, btnX, bottomStartY + 60.0F * S, 16.0F * S, 16.0F * S) && click.method_74245() == 0) {
                     this.clearSearch();
                     this.selectManagement(ManagementPanel.Tab.FRIENDS);
                     GuiSounds.category(1, 3);
                     return true;
                  } else if (MathUtil.isHovered(mouseX, mouseY, btnX, bottomStartY + 80.0F * S, 16.0F * S, 16.0F * S) && click.method_74245() == 0) {
                     this.clearSearch();
                     this.selectManagement(ManagementPanel.Tab.GPS);
                     GuiSounds.category(2, 3);
                     return true;
                  } else if (MathUtil.isHovered(mouseX, mouseY, btnX, bottomStartY + 100.0F * S, 16.0F * S, 16.0F * S) && click.method_74245() == 0) {
                     this.management.close();
                     this.clearSearch();
                     this.active = Category.SETTINGS;
                     this.select = null;
                     this.settingScrollTarget = 0.0F;
                     this.settingScrollAnim = 0.0F;
                     this.scrollTarget = 0.0F;
                     this.scrollVel = 0.0F;
                     animCategoryReset.reset();
                     GuiSounds.category(6, 7);
                     return true;
                  } else if (this.management.active()) {
                     return true;
                  } else {
                     Font draw = Fonts.sf_medium;
                     float catPanW = w - 48.0F * S;
                     float availableCardW = catPanW - 18.0F * S;
                     float cardW = Math.max(140.0F * S, availableCardW / 2.0F);
                     float cardGap = 8.0F * S;
                     float xModule = x + 6.0F * S + 36.0F * S + 5.0F * S;
                     float xModule2 = xModule + cardW + cardGap;
                     float yModule = y + 6.0F * S + 30.0F * S - this.scrollAnim;
                     boolean insidePanel = MathUtil.isHovered(
                        mouseX, mouseY, x + 6.0F * S + 36.0F * S, y + 6.0F * S + 26.0F * S, catPanW, h - 12.0F * S - 26.0F * S
                     );
                     float catPanX = x + 6.0F * S + 36.0F * S;
                     if (this.active == Category.PASTED && !this.searching() && click.method_74245() == 0) {
                        float subX = catPanX + 68.0F * S;
                        float subY = y + 9.5F * S;
                        float subH = 13.0F * S;

                        for (int i = 0; i < PASTED_SUBTABS.length; i++) {
                           float txtW = draw.getWidth(PASTED_SUBTABS[i], 6.5F * S);
                           float subW = txtW + 12.0F * S;
                           if (MathUtil.isHovered(mouseX, mouseY, subX, subY, subW, subH)) {
                              if (this.pastedSubTab != i) {
                                 this.pastedSubTab = i;
                                 this.scrollTarget = 0.0F;
                                 this.scrollVel = 0.0F;
                                 GuiSounds.category(i, PASTED_SUBTABS.length);
                              }

                              return true;
                           }

                           subX += subW + 4.0F * S;
                        }
                     }

                     if (this.active == Category.THEMES && !this.searching() && insidePanel) {
                        float startX = catPanX + 8.0F * S;
                        float curY = y + 6.0F * S + 30.0F * S - this.scrollAnim + 15.0F * S;
                        int cols = 3;
                        float tCardGap = 6.0F * S;
                        float tCardW = (catPanW - 16.0F * S - (float)(cols - 1) * tCardGap) / (float)cols;
                        float cardH = 30.0F * S;
                        Theme[] allThemes = Theme.values();

                        for (int i = 0; i < allThemes.length; i++) {
                           int c = i % cols;
                           int r = i / cols;
                           float cx = startX + (float)c * (tCardW + tCardGap);
                           float cy = curY + (float)r * (cardH + tCardGap);
                           if (MathUtil.isHovered(mouseX, mouseY, cx, cy, tCardW, cardH) && click.method_74245() == 0) {
                              if (ClientTheme.INSTANCE != null) {
                                 ClientTheme.INSTANCE.themeType.set("Монотонные");
                                 ClientTheme.INSTANCE.solidTheme.set(allThemes[i].getName());
                              }

                              animation14.reset();
                              preSelectedTheme = selectedTheme;
                              selectedTheme = allThemes[i];
                              Client.get().guiManager().setGuiTheme(allThemes[i]);
                              GuiSounds.theme(i, allThemes.length);
                              return true;
                           }
                        }

                        int rows = (allThemes.length + cols - 1) / cols;
                        curY += (float)rows * (cardH + tCardGap) + 14.0F * S + 15.0F * S;
                        List<ClientTheme.GradientPreset> presets = ClientTheme.GRADIENT_PRESETS;

                        for (int ix = 0; ix < presets.size(); ix++) {
                           ClientTheme.GradientPreset preset = presets.get(ix);
                           int c = ix % cols;
                           int r = ix / cols;
                           float cx = startX + (float)c * (tCardW + tCardGap);
                           float cy = curY + (float)r * (cardH + tCardGap);
                           if (MathUtil.isHovered(mouseX, mouseY, cx, cy, tCardW, cardH) && click.method_74245() == 0) {
                              if (ClientTheme.INSTANCE != null) {
                                 ClientTheme.INSTANCE.themeType.set("Градиентные");
                                 ClientTheme.INSTANCE.gradientTheme.set(preset.name());
                                 ClientTheme.INSTANCE.syncTheme();
                              }

                              animation14.reset();
                              GuiSounds.theme(ix, presets.size());
                              return true;
                           }
                        }

                        int gradRows = (presets.size() + cols - 1) / cols;
                        curY += (float)gradRows * (cardH + tCardGap) + 14.0F * S + 15.0F * S;
                        float customCardH = 34.0F * S;
                        float customFullW = catPanW - 16.0F * S;
                        if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, customFullW, customCardH)) {
                           if (ClientTheme.INSTANCE != null) {
                              ClientTheme.INSTANCE.themeType.set("Кастомная");
                              ClientTheme.INSTANCE.syncTheme();
                           }

                           animation14.reset();
                           GuiSounds.button();
                           return true;
                        }

                        curY += customCardH + 14.0F * S + 15.0F * S;
                        float fullW = catPanW - 16.0F * S;
                        float optH = 34.0F * S;
                        float modeBtnW = 66.0F * S;
                        float modeBtnH = 18.0F * S;
                        float bOldX = startX + fullW - modeBtnW - 8.0F * S;
                        float bGlassX = bOldX - modeBtnW - 4.0F * S;
                        float bNormX = bGlassX - modeBtnW - 4.0F * S;
                        float bY = curY + (optH - modeBtnH) / 2.0F;
                        if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, bNormX, bY, modeBtnW, modeBtnH)) {
                           if (ClientTheme.INSTANCE != null) {
                              ClientTheme.INSTANCE.uiStyle.set("Обычный");
                              GuiSounds.button();
                           }

                           return true;
                        }

                        if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, bGlassX, bY, modeBtnW, modeBtnH)) {
                           if (ClientTheme.INSTANCE != null) {
                              ClientTheme.INSTANCE.uiStyle.set("Liquid Glass");
                              GuiSounds.button();
                           }

                           return true;
                        }

                        if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, bOldX, bY, modeBtnW, modeBtnH)) {
                           if (ClientTheme.INSTANCE != null) {
                              ClientTheme.INSTANCE.uiStyle.set("Liquid Glass Old");
                              GuiSounds.button();
                           }

                           return true;
                        }

                        curY += optH + 6.0F * S;
                        if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, fullW, optH)) {
                           if (ClientTheme.INSTANCE != null) {
                              boolean nv = !ClientTheme.INSTANCE.uiBlur.getValue();
                              ClientTheme.INSTANCE.uiBlur.set(nv);
                              GuiSounds.toggle(nv);
                           }

                           return true;
                        }

                        curY += optH + 6.0F * S;
                        if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, fullW, optH)) {
                           if (ClientTheme.INSTANCE != null) {
                              boolean nv = !ClientTheme.INSTANCE.uiSounds.getValue();
                              ClientTheme.INSTANCE.uiSounds.set(nv);
                              GuiSounds.toggle(nv);
                           }

                           return true;
                        }
                     }

                     if (this.active == Category.SETTINGS && !this.searching() && insidePanel) {
                        float startX = catPanX + 8.0F * S;
                        float curY = y + 6.0F * S + 30.0F * S - this.scrollAnim + 16.0F * S;
                        float settingsCardW = catPanW - 16.0F * S;
                        float configCardH = 40.0F * S;
                        if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, settingsCardW, configCardH)) {
                           try {
                              Path cfgDir = Client.get().configManager().getConfigDir();
                              Files.createDirectories(cfgDir);
                              class_156.method_668().method_672(cfgDir.toFile());
                              GuiSounds.button();
                           } catch (Exception var66) {
                              var66.printStackTrace();
                           }

                           return true;
                        } else {
                           curY += configCardH + 16.0F * S;
                           curY += 15.0F * S;
                           float styleW = (settingsCardW - 8.0F * S) / 2.0F;
                           float styleH = 44.0F * S;
                           if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, styleW, styleH)) {
                              if (ClientTheme.INSTANCE != null) {
                                 ClientTheme.INSTANCE.uiStyle.set("Обычный");
                              }

                              GuiSounds.button();
                              return true;
                           } else if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX + styleW + 8.0F * S, curY, styleW, styleH)) {
                              if (ClientTheme.INSTANCE != null) {
                                 ClientTheme.INSTANCE.uiStyle.set("Liquid Glass");
                              }

                              GuiSounds.button();
                              return true;
                           } else {
                              curY += styleH + 16.0F * S;
                              curY += 15.0F * S;
                              float toggleH = 32.0F * S;
                              if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, settingsCardW, toggleH)) {
                                 if (ClientTheme.INSTANCE != null) {
                                    boolean next = !ClientTheme.INSTANCE.whiteTheme.getValue();
                                    ClientTheme.INSTANCE.whiteTheme.set(next);
                                    GuiSounds.toggle(next);
                                 }

                                 return true;
                              } else {
                                 curY += toggleH + 6.0F * S;
                                 if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, settingsCardW, toggleH)) {
                                    if (ClientTheme.INSTANCE != null) {
                                       boolean next = !ClientTheme.INSTANCE.uiBlur.getValue();
                                       ClientTheme.INSTANCE.uiBlur.set(next);
                                       GuiSounds.toggle(next);
                                    }

                                    return true;
                                 } else {
                                    curY += toggleH + 6.0F * S;
                                    if (click.method_74245() == 0 && MathUtil.isHovered(mouseX, mouseY, startX, curY, settingsCardW, toggleH)) {
                                       if (ClientTheme.INSTANCE != null) {
                                          boolean next = !ClientTheme.INSTANCE.uiSounds.getValue();
                                          ClientTheme.INSTANCE.uiSounds.set(next);
                                          GuiSounds.toggle(next);
                                       }

                                       return true;
                                    } else {
                                       return true;
                                    }
                                 }
                              }
                           }
                        }
                     } else {
                        ArrayList<Module> visM = new ArrayList<>();

                        for (Module f2 : Client.get().moduleManager().values()) {
                           if (this.moduleVisible(f2)) {
                              visM.add(f2);
                           }
                        }

                        float catAnimClick = animCategoryReset.getOutput();

                        for (int mi = 0; mi < visM.size(); mi += 2) {
                           Module fa = visM.get(mi);
                           Module fb = mi + 1 < visM.size() ? visM.get(mi + 1) : null;
                           float hA = Math.max(34.0F * S, 22.0F * S + this.descHeight(draw, fa.getDesc(), cardW - 10.0F * S) + 6.0F * S);
                           float hB = fb != null ? Math.max(34.0F * S, 22.0F * S + this.descHeight(draw, fb.getDesc(), cardW - 10.0F * S) + 6.0F * S) : 0.0F;
                           float rowH = Math.max(hA, hB);
                           float curRowY = yModule + (1.0F - catAnimClick) * 8.0F * S;
                           if (insidePanel) {
                              if (this.clickModuleCard(fa, xModule, curRowY, cardW, rowH, mouseX, mouseY, click, draw)) {
                                 return true;
                              }

                              if (fb != null && this.clickModuleCard(fb, xModule2, curRowY, cardW, rowH, mouseX, mouseY, click, draw)) {
                                 return true;
                              }
                           }

                           yModule += rowH + 6.0F * S;
                        }

                        float xSetting = setX + 6.0F * S;
                        float hSetting;
                        float wSetting;
                        float setY;
                        float ySetting;
                        if (MathUtil.isHovered(
                              mouseX,
                              mouseY,
                              xSetting,
                              ySetting = (
                                    setY = y + (this.exit ? 60.0F * S - 60.0F * S * this.glomalAnim.get() : -60.0F * S + 60.0F * S * this.glomalAnim.get())
                                 )
                                 + 6.0F * S,
                              wSetting = setW - 12.0F * S,
                              hSetting = setH - 12.0F * S
                           )
                           && this.select != null) {
                           float xST = xSetting + 10.0F * S;
                           float yST = ySetting + 10.0F * S - this.settingScrollAnim;
                           float wST = wSetting - 20.0F * S;

                           for (Setting<?> setting : this.select.getSettings()) {
                              Setting s;
                              float vis;
                              if (setting instanceof SliderSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 if (Boolean.TRUE.equals(s.getVisible().get())
                                    && click.method_74245() == 0
                                    && MathUtil.isHovered(mouseX, mouseY, xST + 3.0F * S, yST + 11.0F * S, wST - 6.0F * S, 13.0F * S)) {
                                    this.draggingSlider = (SliderSetting)s;
                                    GuiSounds.sliderGrab();
                                 }

                                 yST += 30.0F * S * vis;
                              }

                              if (setting instanceof ModeSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 float hMode = 15.0F * S + this.modeChipsHeight((ModeSetting)s, wST - 12.0F * S) + 4.0F * S;
                                 if (Boolean.TRUE.equals(s.getVisible().get()) && click.method_74245() == 0) {
                                    float chipMaxW = wST - 12.0F * S;
                                    float px = 0.0F;
                                    float py = 0.0F;
                                    int chipIndex = 0;

                                    for (String val : ((ModeSetting)s).values) {
                                       float tw = draw.getWidth(RussianUi.text(val), 6.0F * S) + 8.0F * S;
                                       if (px + tw > chipMaxW && px > 0.0F) {
                                          px = 0.0F;
                                          py += 12.0F * S;
                                       }

                                       if (MathUtil.isHovered(mouseX, mouseY, xST + 3.0F * S + px, yST + 15.0F * S + py, tw, 10.0F * S)) {
                                          if (!val.equals(((ModeSetting)s).getValue())) {
                                             GuiSounds.chip(chipIndex, ((ModeSetting)s).values.size());
                                             if ("Тип уведомления".equals(s.getName())
                                                || "typeNotify".equals(s.getName())
                                                || s.getName().toLowerCase().contains("звук")
                                                || s.getName().toLowerCase().contains("sound")) {
                                                NotificationSound.playPreview(val);
                                             }
                                          }

                                          ((ModeSetting)s).set(val);
                                       }

                                       px += tw + 3.0F * S;
                                       chipIndex++;
                                    }
                                 }

                                 yST += (hMode + 5.0F * S) * vis;
                              }

                              if (setting instanceof MultiBooleanSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 float hMulti = 15.0F * S + this.multiChipsHeight((MultiBooleanSetting)s, wST - 12.0F * S) + 4.0F * S;
                                 if (Boolean.TRUE.equals(s.getVisible().get()) && click.method_74245() == 0) {
                                    float chipMaxW = wST - 12.0F * S;
                                    float px = 0.0F;
                                    float py = 0.0F;

                                    for (BooleanSetting b : ((MultiBooleanSetting)s).getValues()) {
                                       float twx = draw.getWidth(RussianUi.text(b.getDisplayName()), 6.0F * S) + 8.0F * S;
                                       if (px + twx > chipMaxW && px > 0.0F) {
                                          px = 0.0F;
                                          py += 12.0F * S;
                                       }

                                       if (MathUtil.isHovered(mouseX, mouseY, xST + 3.0F * S + px, yST + 15.0F * S + py, twx, 10.0F * S)) {
                                          b.set(!b.getValue());
                                          GuiSounds.chipMulti(b.getValue());
                                       }

                                       px += twx + 3.0F * S;
                                    }
                                 }

                                 yST += (hMulti + 5.0F * S) * vis;
                              }

                              if (setting instanceof BooleanSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 if (Boolean.TRUE.equals(s.getVisible().get())
                                    && click.method_74245() == 0
                                    && MathUtil.isHovered(mouseX, mouseY, xST, yST, wST, 16.0F * S)) {
                                    ((BooleanSetting)s).set(!((BooleanSetting)s).getValue());
                                    GuiSounds.toggle(((BooleanSetting)s).getValue());
                                 }

                                 yST += 21.0F * S * vis;
                              }

                              if (setting instanceof ButtonSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 if (Boolean.TRUE.equals(s.getVisible().get())
                                    && click.method_74245() == 0
                                    && MathUtil.isHovered(mouseX, mouseY, xST, yST, wST, 16.0F * S)) {
                                    ((ButtonSetting)s).press();
                                    GuiSounds.button();
                                 }

                                 yST += 21.0F * S * vis;
                              }

                              if (setting instanceof BindSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 float bw;
                                 if (Boolean.TRUE.equals(s.getVisible().get())
                                    && click.method_74245() == 0
                                    && MathUtil.isHovered(
                                       mouseX,
                                       mouseY,
                                       xST
                                          + wST
                                          - 6.0F * S
                                          - (
                                             bw = draw.getWidth(((BindSetting)s).get() == -1 ? "n/a" : Keyboard.keyName(((BindSetting)s).get()), 6.0F * S)
                                                + 10.0F * S
                                          )
                                          - 2.0F * S,
                                       yST + 1.0F * S,
                                       bw + 4.0F * S,
                                       14.0F * S
                                    )) {
                                    this.activeBind = (BindSetting)s;
                                    GuiSounds.bindStart();
                                 }

                                 yST += 21.0F * S * vis;
                              }

                              if (setting instanceof StringSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 float bw;
                                 if (Boolean.TRUE.equals(s.getVisible().get())
                                    && click.method_74245() == 0
                                    && MathUtil.isHovered(
                                       mouseX,
                                       mouseY,
                                       xST
                                          + wST
                                          - 6.0F * S
                                          - (
                                             bw = Math.min(
                                                draw.getWidth(((StringSetting)s).getValue().isEmpty() ? "..." : ((StringSetting)s).getValue(), 6.0F * S)
                                                   + 10.0F * S,
                                                wST * 0.55F
                                             )
                                          )
                                          - 2.0F * S,
                                       yST + 1.0F * S,
                                       bw + 4.0F * S,
                                       14.0F * S
                                    )) {
                                    this.activeString = (StringSetting)s;
                                    this.stringBuffer = ((StringSetting)s).getValue();
                                    GuiSounds.editStart();
                                 }

                                 yST += 21.0F * S * vis;
                              }

                              if (setting instanceof ColorSetting && (vis = this.visAnim(this.select, s = setting)) > 0.01F) {
                                 float open = ((ColorSetting)s).pickerAnim.getOutput();
                                 float hCol = 16.0F * S + open * 35.0F * S;
                                 if (Boolean.TRUE.equals(s.getVisible().get()) && click.method_74245() == 0) {
                                    if (MathUtil.isHovered(mouseX, mouseY, xST, yST, wST, 16.0F * S)) {
                                       ((ColorSetting)s).pickerOpen = !((ColorSetting)s).pickerOpen;
                                       GuiSounds.picker(((ColorSetting)s).pickerOpen);
                                    } else if (((ColorSetting)s).pickerOpen) {
                                       for (int bar = 0; bar < 3; bar++) {
                                          float by = yST + 18.0F * S + (float)(bar * 12) * S;
                                          if (MathUtil.isHovered(mouseX, mouseY, xST + 6.0F * S, by - 3.0F * S, wST - 12.0F * S, 10.0F * S)) {
                                             this.draggingColor = (ColorSetting)s;
                                             this.draggingColorBar = bar;
                                             GuiSounds.sliderGrab();
                                          }
                                       }
                                    }
                                 }

                                 yST += (hCol + 5.0F * S) * vis;
                              }
                           }
                        }

                        return super.method_25402(click, doubled);
                     }
                  }
               }
            }
         }
      }
   }

   public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         return editor.mouseDragged((float)(deltaX / (double)this.scaleFix), (float)(deltaY / (double)this.scaleFix));
      } else {
         float mouseX = (float)(click.comp_4798() / (double)this.scaleFix);
         float mouseY = (float)(click.comp_4799() / (double)this.scaleFix);
         this.lastMouseX = (double)mouseX;
         this.lastMouseY = (double)mouseY;
         if (this.draggingMain) {
            guiOffsetX = mouseX - this.dragStartX;
            guiOffsetY = mouseY - this.dragStartY;
            int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
            int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
            float limitX = (float)screenWidth / 2.0F;
            float limitY = (float)screenHeight / 2.0F;
            guiOffsetX = MathUtil.clamp(guiOffsetX, -limitX + 30.0F, limitX - 30.0F);
            guiOffsetY = MathUtil.clamp(guiOffsetY, -limitY + 30.0F, limitY - 30.0F);
            return true;
         } else if (this.resizingMain) {
            int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
            int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
            float maxExtraW = Math.max(0.0F, (float)screenWidth - 380.0F * S - 40.0F * S);
            float maxExtraH = Math.max(0.0F, (float)screenHeight - 280.0F * S - 60.0F * S);
            extraW = MathUtil.clamp(this.resizeStartExtraW + (mouseX - this.resizeStartX), 0.0F, maxExtraW);
            extraH = MathUtil.clamp(this.resizeStartExtraH + (mouseY - this.resizeStartY), 0.0F, maxExtraH);
            this.resizeBlurFade = 1.0F;
            return true;
         } else if (this.resizingSet) {
            int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
            int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
            float maxSetExtraW = Math.max(0.0F, (float)screenWidth * 0.45F);
            float maxSetExtraH = Math.max(0.0F, (float)screenHeight * 0.65F);
            setExtraW = MathUtil.clamp(this.resizeStartExtraW + (mouseX - this.resizeStartX), 0.0F, maxSetExtraW);
            setExtraH = MathUtil.clamp(this.resizeStartExtraH + (mouseY - this.resizeStartY), 0.0F, maxSetExtraH);
            this.setResizeBlurFade = 1.0F;
            return true;
         } else {
            return super.method_25403(click, deltaX, deltaY);
         }
      }
   }

   public boolean method_25406(class_11909 click) {
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         return editor.mouseReleased(click.method_74245());
      } else {
         if (this.draggingMain) {
            this.draggingMain = false;
            Client.get().configManager().autoSave();
         }

         if (this.resizingMain || this.resizingSet) {
            this.resizingMain = false;
            this.resizingSet = false;
            GuiSounds.sliderRelease();
            Client.get().configManager().autoSave();
         }

         if (this.draggingSlider != null || this.draggingColor != null) {
            GuiSounds.sliderRelease();
         }

         this.draggingSlider = null;
         this.draggingColor = null;
         return super.method_25406(click);
      }
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         if (key == 256) {
            editor.saveAndExit();
         }

         return true;
      } else if (this.management.keyPressed(input)) {
         return true;
      } else if (this.bindingModule != null) {
         boolean reset = key == 256 || key == 261;
         this.bindingModule.setKey(reset ? -1 : key);
         this.bindingModule = null;
         if (reset) {
            GuiSounds.bindReset();
         } else {
            GuiSounds.bindSet();
         }

         return true;
      } else if (this.activeBind != null) {
         boolean reset = key == 256 || key == 261;
         this.activeBind.set(reset ? -1 : key);
         this.activeBind = null;
         if (reset) {
            GuiSounds.bindReset();
         } else {
            GuiSounds.bindSet();
         }
         if (Client.get() != null && Client.get().configManager() != null) {
            Client.get().configManager().autoSave();
         }
         return true;
      } else {
         ClickGui clickGui = Client.get().moduleManager().get(ClickGui.class);
         if (clickGui != null && clickGui.isMenuKey(key)) {
            mc.method_1507(null);
            return true;
         } else if (searchActive) {
            if (key == 256) {
               if (!searchQuery.isEmpty()) {
                  this.clearSearch();
               } else {
                  searchActive = false;
               }

               GuiSounds.searchClear();
            } else if (key == 257 || key == 335) {
               searchActive = false;
               GuiSounds.editCommit();
            } else if (key == 259 && !searchQuery.isEmpty()) {
               searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
               this.searchChanged();
               GuiSounds.erase();
            }

            return true;
         } else if (this.activeString == null) {
            return super.method_25404(input);
         } else {
            if (key == 257 || key == 335) {
               this.activeString.set(this.stringBuffer);
               this.activeString = null;
               GuiSounds.editCommit();
            } else if (key == 256) {
               this.activeString = null;
               GuiSounds.editCancel();
            } else if (key == 259 && !this.stringBuffer.isEmpty()) {
               this.stringBuffer = this.stringBuffer.substring(0, this.stringBuffer.length() - 1);
               GuiSounds.erase();
            }

            return true;
         }
      }
   }

   public boolean method_25400(class_11905 input) {
      if (this.management.charTyped(input)) {
         return true;
      } else if (searchActive) {
         if (input.method_74227() && searchQuery.length() < 24) {
            searchQuery = searchQuery + input.method_74226();
            this.searchChanged();
            GuiSounds.type();
         }

         return true;
      } else if (this.activeString == null) {
         return super.method_25400(input);
      } else {
         if (input.method_74227()) {
            String str = input.method_74226();
            if (!this.activeString.isOnlyNumber() || str.matches("[0-9.,-]+")) {
               this.stringBuffer = this.stringBuffer + str;
               GuiSounds.type();
            }
         }

         return true;
      }
   }

   public boolean method_25401(double mouseXRaw, double mouseYRaw, double horizontalAmount, double verticalAmount) {
      float mouseX = (float)(mouseXRaw / (double)this.scaleFix);
      float mouseY = (float)(mouseYRaw / (double)this.scaleFix);
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         return editor.mouseScrolled(mouseX, mouseY, verticalAmount);
      } else if (this.management.active()) {
         return this.management.scrolled(mouseX, mouseY, verticalAmount);
      } else {
         int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
         int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
         float w = 380.0F * S + extraW;
         float h = 280.0F * S + extraH;
         float x = (float)screenWidth / 2.0F - w / 2.0F + guiOffsetX;
         float y = (float)screenHeight / 2.0F - h / 2.0F + guiOffsetY;
         float catPanW = w - 48.0F * S;
         if (MathUtil.isHovered(mouseX, mouseY, x + 6.0F * S + 36.0F * S, y + 6.0F * S + 26.0F * S, catPanW, h - 12.0F * S - 26.0F * S)) {
            float before = this.scrollTarget;
            this.scrollTarget = MathUtil.clamp((float)((double)this.scrollTarget - verticalAmount * 25.0 * (double)S), 0.0F, this.maxScroll);
            if (this.scrollTarget != before) {
               GuiSounds.scroll();
            }

            return true;
         } else {
            float setW = 150.0F * S + setExtraW;
            float setH = 130.0F * S + setExtraH;
            float setX = this.settingsX(screenWidth, x, w);
            float setY;
            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               setX + 6.0F * S,
               (setY = y + (this.exit ? 60.0F * S - 60.0F * S * this.glomalAnim.get() : -60.0F * S + 60.0F * S * this.glomalAnim.get())) + 6.0F * S,
               setW - 12.0F * S,
               setH - 12.0F * S
            )) {
               float before = this.settingScrollTarget;
               this.settingScrollTarget = MathUtil.clamp(
                  (float)((double)this.settingScrollTarget - verticalAmount * 25.0 * (double)S), 0.0F, this.settingMaxScroll
               );
               if (this.settingScrollTarget != before) {
                  GuiSounds.scroll();
               }

               return true;
            } else {
               return super.method_25401(mouseXRaw, mouseYRaw, horizontalAmount, verticalAmount);
            }
         }
      }
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25422() {
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         editor.saveAndExit();
         return false;
      } else if (this.management.escape()) {
         return false;
      } else {
         if (!this.exit) {
            this.glomalAnim.run(0.0, 0.22F, Easings.EXPO_IN);
            this.exit = true;
            GuiSounds.close();
         }

         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class GuiParticle {
      final float x;
      final float y;
      final float maxRadius;
      final float lifeMs;
      final float drift;
      final long born = System.currentTimeMillis();

      GuiParticle(float x, float y) {
         this.x = x;
         this.y = y;
         this.maxRadius = 10.0F + (float)Math.random() * 7.0F;
         this.lifeMs = 1400.0F + (float)Math.random() * 700.0F;
         this.drift = 4.0F + (float)Math.random() * 6.0F;
      }

      float progress() {
         return (float)(System.currentTimeMillis() - this.born) / this.lifeMs;
      }
   }
}
