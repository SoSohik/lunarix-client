package ru.mentions.screen;

import java.awt.Color;
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
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
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
import ru.mentions.module.api.settings.impl.DelimiterSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.module.impl.display.ClickGui;
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
import ru.mentions.utils.render.FrameTextures;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class Menu2 extends class_437 implements IMinecraft, ClickGuiScreen {
   public static float S = 1.0F;
   public static float extraW2 = 0.0F;
   public static float extraH2 = 0.0F;
   private boolean resizing = false;
   private float resizeStartX = 0.0F;
   private float resizeStartY = 0.0F;
   private float resizeStartExtraW = 0.0F;
   private float resizeStartExtraH = 0.0F;
   private float resizeBlurFade = 0.0F;
   private long lastFrameNano = 0L;
   private float lastDeltaTime = 0.016F;
   private static final float BASE_W = 630.0F;
   private static final float BASE_H = 430.0F;
   private static final float BAR_H = 26.0F;
   private static final float FOOT_H = 15.0F;
   private static final float PAD = 9.0F;
   private static final float GAP = 7.0F;
   private static final int COLS = 3;
   private static final float TAB_H = 18.0F;
   private static final float TAB_GAP = 5.0F;
   private static final float TAB_TOP = 9.0F;
   private static final float ROW_H = 15.0F;
   private static final float ROW_SLIDER_H = 26.0F;
   private static final float ROW_GAP = 5.0F;
   private static final float ROW_DELIM_H = 13.0F;
   private final Animation anim = new Animation();
   private boolean exit = false;
   private float scaleFix = 1.0F;
   private double lastMouseX;
   private double lastMouseY;
   private float u = 1.0F;
   private float panelX;
   private float panelY;
   private float panelW;
   private float panelH;
   private float barY;
   private float barH;
   private float footY;
   private float contentX;
   private float contentY;
   private float contentW;
   private float contentH;
   private float colW;
   private float searchX;
   private float searchY;
   private float searchW;
   private float searchH;
   private float closeX;
   private float closeW;
   private float cosmeticsX;
   private float cosmeticsW;
   private float tabsY;
   private float popClipX;
   private float popClipY;
   private float popClipW;
   private float popClipH;
   private final List<Menu2.Hit> hits = new ArrayList<>();
   private final ManagementPanel management = new ManagementPanel();
   private Category active = Category.COMBAT;
   public int pastedSubTab = 0;
   private Module expanded = null;
   private float scrollTarget = 0.0F;
   private float scrollAnim = 0.0F;
   private float scrollVel = 0.0F;
   private float maxScroll = 0.0F;
   private SliderSetting draggingSlider = null;
   private float dragTrackX;
   private float dragTrackW;
   private ColorSetting draggingColor = null;
   private int draggingColorBar;
   private float dragColorX;
   private float dragColorW;
   private BindSetting activeBind = null;
   private Module bindingModule = null;
   private StringSetting activeString = null;
   private String stringBuffer = "";
   private boolean searchActive = false;
   private int logoClicks = 0;
   private long lastLogoClickMs = 0L;
   private String searchQuery = "";
   private final IdentityHashMap<Setting<?>, ru.mentions.utils.animation.satoshi.Animation> visAnims = new IdentityHashMap<>();
   private final IdentityHashMap<Setting<?>, ru.mentions.utils.animation.satoshi.Animation> dropAnims = new IdentityHashMap<>();
   private final HashMap<String, ru.mentions.utils.animation.satoshi.Animation> chipAnims = new HashMap<>();
   private final HashMap<String, Float> descCache = new HashMap<>();
   private final HashMap<String, Float> smoothCache = new HashMap<>();

   public Menu2() {
      super(class_2561.method_43470("MenuTile"));
   }

   protected void method_25426() {
      this.exit = false;
      this.searchActive = false;
      this.searchQuery = "";
      this.bindingModule = null;
      this.activeBind = null;
      this.activeString = null;
      this.expanded = null;
      this.scrollTarget = 0.0F;
      this.scrollAnim = 0.0F;
      this.scrollVel = 0.0F;
      this.resizing = false;
      this.lastFrameNano = 0L;
      this.lastDeltaTime = 0.016F;
      this.management.onOpened();
      GuiSounds.open();
      GuiMusicPlayer.start(0.15F);
      this.anim.run(1.0, 0.34, Easings.EXPO_OUT);
      Menu.selectedTheme = Client.get().guiManager().getCurrentTheme();
      Menu.preSelectedTheme = Client.get().guiManager().getCurrentTheme();
      Menu.themes = Theme.values();
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         this.closeCheck();
         this.anim.update();
         this.scaleFix = this.computeScaleFix();
         mouseX = (int)((float)mouseX / this.scaleFix);
         mouseY = (int)((float)mouseY / this.scaleFix);
         this.lastMouseX = (double)mouseX;
         this.lastMouseY = (double)mouseY;
      }
   }

   public void method_25432() {
      this.management.clearFocus();
      OverlayEditors.closeAll();
      super.method_25432();
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25422() {
      if (this.management.escape()) {
         return false;
      } else if (this.searchActive) {
         this.searchActive = false;
         GuiSounds.searchClear();
         return false;
      } else if (this.activeBind != null) {
         this.activeBind = null;
         GuiSounds.bindReset();
         return false;
      } else if (this.bindingModule != null) {
         this.bindingModule = null;
         GuiSounds.bindReset();
         return false;
      } else if (this.activeString != null) {
         this.activeString = null;
         GuiSounds.editCancel();
         return false;
      } else {
         this.startExit();
         return false;
      }
   }

   private void startExit() {
      if (!this.exit) {
         this.anim.run(0.0, 0.22, Easings.EXPO_IN);
         this.exit = true;
         GuiSounds.close();
      }
   }

   private void closeCheck() {
      if (this.exit && this.anim.isFinished()) {
         this.method_25419();
         GuiMusicPlayer.stop();
         this.exit = false;
      }
   }

   private float computeScaleFix() {
      float currentScale = (float)mc.method_22683().method_4495();
      return currentScale <= 0.0F ? 1.0F : 2.0F / currentScale;
   }

   private int screenW() {
      return (int)((float)mc.method_22683().method_4486() / this.scaleFix);
   }

   private int screenH() {
      return (int)((float)mc.method_22683().method_4502() / this.scaleFix);
   }

   private ru.mentions.utils.animation.satoshi.Animation dropAnim(Setting<?> key) {
      return this.dropAnims.computeIfAbsent(key, k -> new EaseInOutQuad(220, 1.0));
   }

   private ru.mentions.utils.animation.satoshi.Animation visAnim(Module parent, Setting<?> setting) {
      return this.visAnims.computeIfAbsent(setting, k -> new EaseInOutQuad(220, 1.0));
   }

   private float settingVis(Module parent, Setting<?> setting) {
      ru.mentions.utils.animation.satoshi.Animation anim = this.visAnim(parent, setting);
      anim.setDirection(Boolean.TRUE.equals(setting.getVisible().get()) ? Direction.FORWARDS : Direction.BACKWARDS);
      return anim.getOutput();
   }

   private ru.mentions.utils.animation.satoshi.Animation chipAnim(String key) {
      return this.chipAnims.computeIfAbsent(key, k -> new EaseInOutQuad(250, 1.0));
   }

   private float descHeight(Font font, Module f, float width) {
      String desc = f.getDesc() == null ? "" : f.getDesc();
      String key = desc + "_" + f.getName() + "_" + width;
      Float cached = this.descCache.get(key);
      if (cached != null) {
         return cached;
      } else {
         float height = desc.isEmpty() ? 0.0F : font.getWrappedHeight(desc, width, 6.5F * this.u);
         this.descCache.put(key, height);
         return height;
      }
   }

   private String query() {
      return this.searchQuery.trim().toLowerCase(Locale.ROOT);
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

   private List<Module> visibleModules() {
      ArrayList<Module> list = new ArrayList<>();

      for (Module f : Client.get().moduleManager().values()) {
         if (this.moduleVisible(f) && !f.isHidden()) {
            list.add(f);
         }
      }

      return list;
   }

   @Override
   public void renderOverlay(class_332 context, class_9779 tickCounter) {
      if (!DualWindowService.isCleanPass()) {
         this.scaleFix = this.computeScaleFix();
         int sw = this.screenW();
         int sh = this.screenH();
         long nowNano = System.nanoTime();
         this.lastDeltaTime = this.lastFrameNano == 0L ? 0.016F : Math.min((float)(nowNano - this.lastFrameNano) / 1.0E9F, 0.08F);
         this.lastFrameNano = nowNano;
         float dt = this.lastDeltaTime;
         if (this.resizing) {
            this.resizeBlurFade = Math.min(1.0F, this.resizeBlurFade + dt * 6.0F);
         } else if (!(extraW2 > 1.0F) && !(extraH2 > 1.0F)) {
            this.resizeBlurFade = Math.max(0.0F, this.resizeBlurFade - dt * 3.0F);
         } else {
            this.resizeBlurFade = Math.max(0.42F, this.resizeBlurFade - dt * 1.6F);
         }

         float g = this.anim.get();
         if (context != null) {
            context.method_51448().pushMatrix();
         }

         this.hits.clear();
         this.management.beginNavigation();
         OverlayEditor editor = OverlayEditors.active();
         if (editor != null) {
            float editorMouseX = (float)(mc.field_1729.method_68879(mc.method_22683()) / (double)this.scaleFix);
            float editorMouseY = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.scaleFix);
            editor.render((float)sw, (float)sh, editorMouseX, editorMouseY, g);
            Render2D.endOverlay();
            if (context != null) {
               context.method_51448().popMatrix();
            }
         } else {
            SliderSetting sizeSlider = Client.get().moduleManager().get(ClickGui.class).size;
            if (this.draggingSlider != sizeSlider) {
               S = sizeSlider.getValue();
            }

            FrameTextures.ensure();
            ScreenBlur.capture(1);
            RenderUtil.Blur.blur(0.0F, 0.0F, (float)sw, (float)sh, g, 0.0F, ColorUtil.getColor(0, 0.5F * g));
            ScreenBlur.capture();
            this.layout(sw, sh, g);
            float pop = 0.94F + 0.06F * g;
            float popInsetX = this.panelW * (1.0F - pop) / 2.0F;
            float popInsetY = this.panelH * (1.0F - pop) / 2.0F;
            this.popClipX = this.panelX + popInsetX;
            this.popClipY = this.panelY + popInsetY;
            this.popClipW = this.panelW - 2.0F * popInsetX;
            this.popClipH = this.panelH - 2.0F * popInsetY;
            Scissor.enable(this.popClipX, this.popClipY, this.popClipW, this.popClipH, 2.0F);
            Scissor.disable();
            this.drawTabs(g);
            Scissor.enable(this.popClipX, this.popClipY, this.popClipW, this.popClipH, 2.0F);
            this.drawBar(g);
            if (this.management.active()) {
               this.management.render(this.contentX, this.contentY, this.contentW, this.contentH, this.u, g, (float)this.lastMouseX, (float)this.lastMouseY);
            } else {
               this.drawCards(g);
            }

            this.drawFooter(g);
            Scissor.reset();
            Render2D.endOverlay();
            if (context != null) {
               context.method_51448().popMatrix();
            }
         }
      }
   }

   private void layout(int sw, int sh, float g) {
      float basePanelW = Math.min((float)sw * 0.66F * S, (float)sw * 0.96F);
      float basePanelH = Math.min((float)sh * 0.76F * S, (float)sh * 0.92F);
      float unit = Math.min(basePanelW / 630.0F, basePanelH / 430.0F);
      this.u = unit;
      this.panelW = 630.0F * unit + extraW2;
      this.panelH = 430.0F * unit + extraH2;
      float slide = this.exit ? 34.0F * unit - 34.0F * unit * g : -34.0F * unit + 34.0F * unit * g;
      this.panelX = (float)sw / 2.0F - this.panelW / 2.0F;
      this.panelY = (float)sh / 2.0F - this.panelH / 2.0F + slide;
      this.barH = 26.0F * unit;
      this.barY = this.panelY;
      this.footY = this.panelY + this.panelH - 15.0F * unit;
      this.contentX = this.panelX + 9.0F * unit;
      this.contentY = this.barY + this.barH + 9.0F * unit;
      this.contentW = this.panelW - 18.0F * unit;
      this.contentH = this.footY - 9.0F * unit - this.contentY;
      this.colW = (this.contentW - 7.0F * unit * 2.0F) / 3.0F;
      this.tabsY = this.panelY - 9.0F * unit - 18.0F * unit;
      this.searchH = 15.0F * unit;
      this.searchW = 132.0F * unit;
      this.searchX = this.contentX;
      this.searchY = this.barY + (this.barH - this.searchH) / 2.0F;
      this.closeW = 46.0F * unit;
      this.closeX = this.panelX + this.panelW - 9.0F * unit - this.closeW;
      this.cosmeticsW = 58.0F * unit;
      this.cosmeticsX = this.closeX - 8.0F * unit - this.cosmeticsW;
   }

   private void drawTabs(float g) {
      Font icon = Fonts.mentions_2;
      Font text = Fonts.sf_regular;
      float unit = this.u;
      float total = 0.0F;
      Category[] cats = Category.values();

      for (Category c : cats) {
         total += 16.0F * unit + text.getWidth(c.getName(), 7.0F * unit) + 12.0F * unit;
      }

      for (ManagementPanel.Tab tab : ManagementPanel.Tab.values()) {
         if (tab != ManagementPanel.Tab.THEMES) {
            total += text.getWidth(tab.label, 7.0F * unit) + 20.0F * unit;
         }
      }

      total += 5.0F * unit * (float)(cats.length + ManagementPanel.Tab.values().length - 2);
      float fit = Math.min(1.0F, ((float)this.screenW() - 16.0F * unit) / total);
      unit *= fit;
      total *= fit;
      float tx = (float)this.screenW() / 2.0F - total / 2.0F;
      float ty = this.tabsY;
      float th = 18.0F * unit;

      for (Category c : cats) {
         boolean isActive = c == Category.THEMES ? this.management.selected(ManagementPanel.Tab.THEMES) : !this.management.active() && this.active == c;
         float tw = 16.0F * unit + text.getWidth(c.getDisplayName(), 7.0F * unit) + 12.0F * unit;
         ru.mentions.utils.animation.satoshi.Animation hoverAnim = this.chipAnim("tab:" + c.getName());
         boolean hovered = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, tx, ty, tw, th);
         hoverAnim.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
         float hv = hoverAnim.getOutput();
         ru.mentions.utils.animation.satoshi.Animation selAnim = this.chipAnim("tabsel:" + c.getName());
         selAnim.setDirection(isActive ? Direction.FORWARDS : Direction.BACKWARDS);
         float sel = selAnim.getOutput();
         RenderUtil.Render2D.glow(tx, ty, tw, th, ColorUtil.getColor(0, 0.12F * g * (0.4F + 0.6F * sel)), 7.0F * unit, 8.0F, 1.0F);
         RenderUtil.Render2D.rect(
            tx,
            ty,
            tw,
            th,
            ColorUtil.overCol(ColorUtil.getColor(0, (0.35F + 0.25F * hv) * g), ColorUtil.getColor(255, 0.08F * g), hv * (1.0F - sel)),
            7.0F * unit
         );
         RenderUtil.Render2D.outline(
            tx,
            ty,
            tw,
            th,
            0.6F * unit,
            ColorUtil.replAlpha(ColorUtil.overCol(ColorUtil.getColor(255), ColorUtil.client(), sel), g * (0.1F + 0.55F * sel + 0.25F * hv)),
            7.0F * unit
         );
         int nameCol = ColorUtil.getColor(255, g * (0.45F + 0.45F * sel + 0.25F * hv));
         icon.drawCentered(
            c.getIcon(), tx + 9.0F * unit, ty + 5.0F * unit, 7.0F * unit, ColorUtil.overCol(nameCol, ColorUtil.replAlpha(ColorUtil.client(), g), sel)
         );
         text.draw(c.getDisplayName(), tx + 16.0F * unit, ty + 5.5F * unit, 7.0F * unit, nameCol);
         this.hit(tx, ty, tw, th, 0, () -> this.selectCategory(c));
         tx += tw + 5.0F * unit;
      }

      for (ManagementPanel.Tab tabx : ManagementPanel.Tab.values()) {
         if (tabx != ManagementPanel.Tab.THEMES) {
            float tw = text.getWidth(tabx.getDisplayName(), 7.0F * unit) + 20.0F * unit;
            this.management.drawNavigation(tabx, tx, ty, tw, th, unit, g, (float)this.lastMouseX, (float)this.lastMouseY, () -> this.selectManagement(tab));
            tx += tw + 5.0F * unit;
         }
      }
   }

   private void selectManagement(ManagementPanel.Tab tab) {
      this.searchActive = false;
      this.searchQuery = "";
      this.activeBind = null;
      this.bindingModule = null;
      if (this.activeString != null) {
         this.activeString.set(this.stringBuffer);
      }

      this.activeString = null;
      this.draggingSlider = null;
      this.draggingColor = null;
      this.expanded = null;
      this.management.select(tab);
      this.hits.clear();
      GuiSounds.category(Category.values().length + tab.ordinal(), Category.values().length + 3);
   }

   private void selectCategory(Category c) {
      if (c == Category.THEMES) {
         this.selectManagement(ManagementPanel.Tab.THEMES);
      } else if (c == Category.SETTINGS) {
         this.selectManagement(ManagementPanel.Tab.CONFIGS);
      } else {
         boolean wasManagement = this.management.active();
         this.management.close();
         if (this.searchActive) {
            this.searchActive = false;
            this.searchQuery = "";
         }

         if (this.active != c || wasManagement) {
            this.active = c;
            this.scrollTarget = 0.0F;
            this.scrollVel = 0.0F;
            this.expanded = null;
         }

         GuiSounds.category(c.ordinal(), Category.values().length);
      }
   }

   private void drawBar(float g) {
      float unit = this.u;
      float w = this.panelW;
      float baseW2 = 630.0F * unit;
      float baseH2 = 430.0F * unit;
      if (this.resizeBlurFade > 0.01F && (extraW2 > 1.0F || extraH2 > 1.0F)) {
         float blurStrength = g * this.resizeBlurFade;
         if (extraW2 > 1.0F) {
            float ewX = this.panelX + baseW2 - 4.0F * unit;
            float ewW = extraW2 + 4.0F * unit;
            RenderUtil.Blur.blur(ewX, this.panelY, ewW, this.panelH, blurStrength, 10.0F * unit, ColorUtil.getColor(0, 0.45F * blurStrength));
            RenderUtil.Render2D.rect(ewX, this.panelY, ewW, this.panelH, ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.04F), 10.0F * unit);
            if (this.resizing) {
               RenderUtil.Render2D.rect(
                  this.panelX + baseW2 - 0.5F * unit,
                  this.panelY + 4.0F * unit,
                  1.0F * unit,
                  this.panelH - 8.0F * unit,
                  ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.35F),
                  0.5F * unit
               );
            }
         }

         if (extraH2 > 1.0F) {
            float ehY = this.panelY + baseH2 - 4.0F * unit;
            float ehH = extraH2 + 4.0F * unit;
            RenderUtil.Blur.blur(this.panelX, ehY, this.panelW, ehH, blurStrength, 10.0F * unit, ColorUtil.getColor(0, 0.45F * blurStrength));
            RenderUtil.Render2D.rect(this.panelX, ehY, this.panelW, ehH, ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.04F), 10.0F * unit);
            if (this.resizing) {
               RenderUtil.Render2D.rect(
                  this.panelX + 4.0F * unit,
                  this.panelY + baseH2 - 0.5F * unit,
                  this.panelW - 8.0F * unit,
                  1.0F * unit,
                  ColorUtil.replAlpha(ColorUtil.client(), blurStrength * 0.35F),
                  0.5F * unit
               );
            }
         }
      }

      RenderUtil.Render2D.rect(this.panelX, this.panelY, w, this.panelH, ColorUtil.getColor(0, 0.66F * g), 10.0F * unit);
      RenderUtil.Render2D.rect(this.panelX, this.panelY, w, this.barH, ColorUtil.getColor(0, 0.3F * g), 10.0F * unit, 10.0F * unit, 0.0F, 0.0F);
      RenderUtil.Render2D.outline(this.panelX, this.panelY, w, this.panelH, 0.7F * unit, ColorUtil.replAlpha(ColorUtil.getColor(255), 0.09F * g), 10.0F * unit);
      float menu2LogoSize = 13.0F * unit;
      RenderUtil.Render2D.glow(
         this.panelX + 18.0F * unit - menu2LogoSize / 2.0F,
         this.panelY + 11.0F * unit - menu2LogoSize / 2.0F,
         menu2LogoSize,
         menu2LogoSize,
         ColorUtil.replAlpha(ColorUtil.client(), g * 0.25F),
         menu2LogoSize / 2.0F,
         8.0F,
         1.0F
      );
      RenderUtil.Images.texture(
         class_2960.method_60655("client", "textures/logo.png"),
         this.panelX + 18.0F * unit - menu2LogoSize / 2.0F,
         this.panelY + 11.0F * unit - menu2LogoSize / 2.0F,
         menu2LogoSize,
         menu2LogoSize,
         ColorUtil.getColor(255, g)
      );
      this.hit(
         this.panelX + 18.0F * unit - menu2LogoSize / 2.0F,
         this.panelY + 11.0F * unit - menu2LogoSize / 2.0F,
         menu2LogoSize,
         menu2LogoSize,
         0,
         this::onLogoClick
      );
      Font regular = Fonts.sf_regular;
      if (!this.management.active()) {
         boolean focus = this.searchActive;
         boolean hovered = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, this.searchX, this.searchY, this.searchW, this.searchH);
         ru.mentions.utils.animation.satoshi.Animation focusAnim = this.chipAnim("search");
         focusAnim.setDirection(!focus && !hovered ? Direction.BACKWARDS : Direction.FORWARDS);
         float fa = focusAnim.getOutput();
         RenderUtil.Blur.blur(
            this.searchX, this.searchY, this.searchW, this.searchH, g, 6.0F * unit, ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.background(), 0.55F), g)
         );
         RenderUtil.Render2D.rect(this.searchX, this.searchY, this.searchW, this.searchH, ColorUtil.getColor(0, 0.35F * g), 6.0F * unit);
         RenderUtil.Render2D.outline(
            this.searchX, this.searchY, this.searchW, this.searchH, 0.6F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * 0.6F * fa), 6.0F * unit
         );
         Fonts.gui.draw("A", this.searchX + 6.0F * unit, this.searchY + 4.5F * unit, 6.5F * unit, ColorUtil.getColor(255, g * (0.35F + 0.5F * fa)));
         float textX = this.searchX + 6.0F * unit + 11.0F * unit;
         float textW = this.searchW - (textX - this.searchX) - 7.0F * unit;
         if (this.searchQuery.isEmpty() && !focus) {
            regular.draw(Lang.pick("Пошук", "Поиск", "Search"), textX, this.searchY + 4.5F * unit, 7.0F * unit, ColorUtil.getColor(255, g * 0.35F));
         } else {
            regular.drawFadingTextReverse(this.searchQuery, textX, this.searchY + 4.5F * unit, textW, ColorUtil.getColor(255, g * 0.8F), 7.0F * unit);
            float caret = focus && System.currentTimeMillis() / 500L % 2L == 0L ? 1.0F : 0.0F;
            float caretX = textX + Math.min(regular.getWidth(this.searchQuery, 7.0F * unit), textW) + 1.5F * unit;
            RenderUtil.Render2D.rect(
               caretX, this.searchY + 4.0F * unit, 0.7F * unit, 7.5F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * caret), 0.3F * unit
            );
         }

         this.hit(this.searchX, this.searchY, this.searchW, this.searchH, 0, () -> {
            if (this.activeString != null) {
               this.activeString.set(this.stringBuffer);
               this.activeString = null;
               GuiSounds.editCommit();
            }

            if (!this.searchActive) {
               GuiSounds.editStart();
            }

            this.searchActive = true;
         });
         this.hit(this.searchX, this.searchY, this.searchW, this.searchH, 1, () -> {
            if (!this.searchQuery.isEmpty()) {
               this.searchQuery = "";
               GuiSounds.searchClear();
            }

            this.searchActive = false;
         });
      } else {
         regular.draw(this.management.title(), this.searchX + 30.0F * unit, this.searchY + 4.5F * unit, 8.0F * unit, ColorUtil.getColor(235, g));
      }

      float sx = this.searchX + this.searchW + 10.0F * unit;
      float sy = this.barY + (this.barH - 7.0F * unit) / 2.0F;
      if (Menu.themes == null) {
         Menu.themes = Theme.values();
      }

      int themeIndex = 0;

      for (Theme theme : Menu.themes) {
         theme.animation.setDirection(Menu.selectedTheme == theme ? Direction.FORWARDS : Direction.BACKWARDS);
         float sel = theme.animation.getOutput();
         RenderUtil.Render2D.rect(
            sx + 1.0F * unit * sel,
            sy + 1.0F * unit * sel,
            7.0F * unit - 2.0F * unit * sel,
            7.0F * unit - 2.0F * unit * sel,
            ColorUtil.replAlpha(theme.getClient(), (0.45F + 0.55F * sel) * g),
            7.0F * unit
         );
         RenderUtil.Render2D.outline(
            sx - 0.75F * unit * sel,
            sy - 0.75F * unit * sel,
            7.0F * unit + 1.5F * unit * sel,
            7.0F * unit + 1.5F * unit * sel,
            0.4F * unit,
            ColorUtil.replAlpha(theme.getClient(), g * sel),
            7.0F * unit
         );
         int capturedIndex = themeIndex;
         this.hit(sx - 1.0F * unit, sy - 1.0F * unit, 9.0F * unit, 9.0F * unit, 0, () -> this.selectTheme(theme, capturedIndex));
         sx += 12.0F * unit;
         themeIndex++;
      }

      boolean cosHover = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, this.cosmeticsX, this.searchY, this.cosmeticsW, this.searchH);
      ru.mentions.utils.animation.satoshi.Animation cosAnim = this.chipAnim("cosmetics");
      cosAnim.setDirection(cosHover ? Direction.FORWARDS : Direction.BACKWARDS);
      float co = cosAnim.getOutput();
      RenderUtil.Render2D.rect(
         this.cosmeticsX,
         this.searchY,
         this.cosmeticsW,
         this.searchH,
         ColorUtil.overCol(ColorUtil.getColor(0, 0.35F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.35F * g), co),
         6.0F * unit
      );
      RenderUtil.Render2D.outline(
         this.cosmeticsX,
         this.searchY,
         this.cosmeticsW,
         this.searchH,
         0.6F * unit,
         ColorUtil.replAlpha(ColorUtil.client(), g * (0.15F + 0.5F * co)),
         6.0F * unit
      );
      regular.drawCentered(
         Lang.pick("Косметика", "Косметика", "Cosmetics"),
         this.cosmeticsX + this.cosmeticsW / 2.0F,
         this.searchY + 4.5F * unit,
         7.0F * unit,
         ColorUtil.getColor(255, g * (0.55F + 0.45F * co))
      );
      this.hit(this.cosmeticsX, this.searchY, this.cosmeticsW, this.searchH, 0, this::openCosmetics);
      boolean closeHover = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, this.closeX, this.searchY, this.closeW, this.searchH);
      ru.mentions.utils.animation.satoshi.Animation closeAnim = this.chipAnim("close");
      closeAnim.setDirection(closeHover ? Direction.FORWARDS : Direction.BACKWARDS);
      float ch = closeAnim.getOutput();
      RenderUtil.Render2D.rect(
         this.closeX,
         this.searchY,
         this.closeW,
         this.searchH,
         ColorUtil.overCol(ColorUtil.getColor(0, 0.35F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.35F * g), ch),
         6.0F * unit
      );
      RenderUtil.Render2D.outline(
         this.closeX, this.searchY, this.closeW, this.searchH, 0.6F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * (0.15F + 0.5F * ch)), 6.0F * unit
      );
      String closeLabel = Lang.pick("Закрити", "Закрыть", "Close");
      regular.drawCentered(
         closeLabel, this.closeX + this.closeW / 2.0F, this.searchY + 4.5F * unit, 7.0F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * ch))
      );
      this.hit(this.closeX, this.searchY, this.closeW, this.searchH, 0, this::startExit);
   }

   private void selectTheme(Theme theme, int index) {
      Menu.preSelectedTheme = Menu.selectedTheme;
      Menu.selectedTheme = theme;
      Client.get().guiManager().setGuiTheme(theme);
      GuiSounds.theme(index, Menu.themes.length);
   }

   private void drawFooter(float g) {
      float unit = this.u;
      RenderUtil.Render2D.rect(this.panelX, this.footY, this.panelW, 15.0F * unit, ColorUtil.getColor(0, 0.3F * g), 0.0F, 0.0F, 10.0F * unit, 10.0F * unit);
      Font regular = Fonts.sf_regular;
      String left = "Lunarix " + Client.build;
      regular.draw(left, this.contentX, this.footY + 4.5F * unit, 6.0F * unit, ColorUtil.getColor(255, g * 0.45F));
      String theme = Menu.selectedTheme != null ? RussianUi.text(Menu.selectedTheme.getName()) : "mentioons";
      regular.draw(
         theme,
         this.contentX + regular.getWidth(left, 6.0F * unit) + 8.0F * unit,
         this.footY + 4.5F * unit,
         6.0F * unit,
         ColorUtil.replAlpha(ColorUtil.client(), g * 0.65F)
      );
      int count = this.visibleModules().size();
      String right = this.management.active() ? this.management.title() : count + " modules";
      regular.draw(
         right,
         this.panelX + this.panelW - 9.0F * unit - regular.getWidth(right, 6.0F * unit) - 14.0F * unit,
         this.footY + 4.5F * unit,
         6.0F * unit,
         ColorUtil.getColor(255, g * 0.35F)
      );
      float gripX = this.panelX + this.panelW - 14.0F * unit;
      float gripY = this.panelY + this.panelH - 12.0F * unit;
      boolean gripHover = MathUtil.isHovered(
         (float)this.lastMouseX, (float)this.lastMouseY, gripX - 3.0F * unit, gripY - 3.0F * unit, 16.0F * unit, 16.0F * unit
      );
      float gripAlpha = g * (!gripHover && !this.resizing ? 0.35F : 0.95F);
      int gripColor = ColorUtil.replAlpha(ColorUtil.client(), gripAlpha);
      RenderUtil.Render2D.rect(gripX + 6.0F * unit, gripY + 6.5F * unit, 3.5F * unit, 1.2F * unit, gripColor, 0.6F * unit);
      RenderUtil.Render2D.rect(gripX + 3.0F * unit, gripY + 4.0F * unit, 6.5F * unit, 1.2F * unit, gripColor, 0.6F * unit);
      RenderUtil.Render2D.rect(gripX, gripY + 1.5F * unit, 9.5F * unit, 1.2F * unit, gripColor, 0.6F * unit);
   }

   private float settingsWidth() {
      return this.colW - 20.0F * this.u;
   }

   private float settingHeight(Setting<?> setting, float w) {
      float unit = this.u;
      if (setting instanceof SliderSetting) {
         return 26.0F * unit;
      } else if (setting instanceof ModeSetting mode) {
         float h = 15.0F * unit;
         if (mode.opened) {
            h += 4.0F * unit + (float)mode.values.size() * 16.0F * unit;
         }

         return h;
      } else if (setting instanceof MultiBooleanSetting multi) {
         float h = 15.0F * unit;
         if (multi.opened) {
            h += 4.0F * unit + (float)multi.getValues().size() * 16.0F * unit;
         }

         return h;
      } else if (setting instanceof ColorSetting color) {
         float h = 15.0F * unit;
         if (color.pickerOpen) {
            h += 4.0F * unit + 34.0F * unit;
         }

         return h;
      } else if (setting instanceof BooleanSetting) {
         return 15.0F * unit;
      } else if (setting instanceof ButtonSetting) {
         return 15.0F * unit;
      } else if (setting instanceof BindSetting) {
         return 15.0F * unit;
      } else if (setting instanceof StringSetting) {
         return 15.0F * unit;
      } else {
         return setting instanceof DelimiterSetting ? 13.0F * unit : 0.0F;
      }
   }

   private float settingsHeight(Module f, float w) {
      float total = 0.0F;

      for (Setting<?> setting : f.getSettings()) {
         float vis = this.settingVis(f, setting);
         if (!(vis <= 0.01F)) {
            float h = this.settingHeight(setting, w);
            if (!(h <= 0.0F)) {
               total += (h + 5.0F * this.u) * vis;
            }
         }
      }

      return total;
   }

   private float cardHeight(Module f) {
      float unit = this.u;
      float descH = this.descHeight(Fonts.sf_regular, f, this.colW - 16.0F * unit);
      float h = 5.0F * unit + 13.0F * unit + descH + 5.0F * unit;
      if (f == this.expanded) {
         float sh = this.settingsHeight(f, this.settingsWidth());
         if (sh > 0.0F) {
            h += 5.0F * unit + sh;
         }
      }

      return h;
   }

   private void drawCards(float g) {
      List<Module> modules = this.visibleModules();
      float unit = this.u;
      Scissor.enable(this.contentX, this.contentY, this.contentW, this.contentH, 2.0F);
      float scrollFactor = 1.0F - (float)Math.exp((double)(-15.0F * this.lastDeltaTime));
      this.scrollAnim = this.scrollAnim + (this.scrollTarget - this.scrollAnim) * scrollFactor;
      if (Math.abs(this.scrollTarget - this.scrollAnim) < 0.1F) {
         this.scrollAnim = this.scrollTarget;
         this.scrollVel = 0.0F;
      }

      float[] columnY = new float[3];

      for (int c = 0; c < 3; c++) {
         columnY[c] = this.contentY + 4.0F * unit - this.scrollAnim;
      }

      int index = 0;

      for (Module f : modules) {
         int col = index % 3;
         float x = this.contentX + (float)col * (this.colW + 7.0F * unit);
         float y = columnY[col];
         float h = this.cardHeight(f);
         if (y + h >= this.contentY - 40.0F * unit && y <= this.contentY + this.contentH + 40.0F * unit) {
            this.drawCard(f, x, y, h, g);
         }

         columnY[col] = y + h + 7.0F * unit;
         index++;
      }

      float tallest = 0.0F;

      for (int c = 0; c < 3; c++) {
         tallest = Math.max(tallest, columnY[c]);
      }

      this.maxScroll = Math.max(0.0F, tallest + this.scrollAnim - (this.contentY + 4.0F * unit) - (this.contentH - 4.0F * unit));
      if (this.scrollTarget > this.maxScroll) {
         this.scrollTarget = this.maxScroll;
      }

      if (this.searching() && modules.isEmpty()) {
         Font regular = Fonts.sf_regular;
         float cx = this.contentX + this.contentW / 2.0F;
         float cy = this.contentY + this.contentH / 2.0F;
         Fonts.gui.drawCentered("A", cx, cy - 8.0F * unit, 9.0F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * 0.45F));
         regular.drawCentered("Nothing found", cx, cy + 6.0F * unit, 7.0F * unit, ColorUtil.getColor(255, g * 0.5F));
      }

      Scissor.disable();
   }

   private void drawCard(Module f, float x, float y, float cardH, float g) {
      float unit = this.u;
      Font title = Fonts.sf_medium;
      Font regular = Fonts.sf_regular;
      f.getAnimation16().setDirection(f.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
      float enabled = f.getAnimation16().getOutput();
      boolean hovered = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, x, y, this.colW, cardH);
      f.getAnimation12().setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
      float hv = f.getAnimation12().getOutput();
      RenderUtil.Render2D.rect(
         x,
         y,
         this.colW,
         cardH,
         ColorUtil.overCol(ColorUtil.getColor(255, (0.045F + 0.045F * hv) * g), ColorUtil.getColor(255, 0.1F * g), enabled),
         6.0F * unit
      );
      RenderUtil.Render2D.outline(x, y, this.colW, cardH, 0.5F * unit, ColorUtil.replAlpha(ColorUtil.getColor(255), (0.05F + 0.08F * hv) * g), 6.0F * unit);
      if (enabled > 0.01F) {
         RenderUtil.Render2D.rect(
            x, y + 5.0F * unit, 2.0F * unit, cardH - 10.0F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * enabled * (0.7F + 0.3F * hv)), 1.0F * unit
         );
      }

      float titleY = y + 5.0F * unit;
      float toggleW = 20.0F * unit;
      float toggleH = 9.0F * unit;
      float toggleX = x + this.colW - 7.0F * unit - toggleW;
      float toggleY = titleY + 1.5F * unit;
      float expandX = toggleX - 4.0F * unit - 11.0F * unit;
      float expandY = titleY + 1.0F * unit;
      float expandW = 11.0F * unit;
      float titleW = expandX - 6.0F * unit - x;
      title.drawFadingText(
         f.getBigName(),
         x + 7.0F * unit,
         titleY,
         Math.max(20.0F * unit, titleW),
         ColorUtil.getColor(255, g * (0.35F + 0.5F * enabled + 0.25F * hv)),
         8.0F * unit
      );
      ru.mentions.utils.animation.satoshi.Animation togHover = this.chipAnim("tog:" + f.getName());
      boolean togHovered = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, toggleX, toggleY, toggleW, toggleH);
      togHover.setDirection(togHovered ? Direction.FORWARDS : Direction.BACKWARDS);
      float th = togHover.getOutput();
      RenderUtil.Render2D.rect(
         toggleX,
         toggleY,
         toggleW,
         toggleH,
         ColorUtil.overCol(ColorUtil.getColor(255, (0.1F + 0.06F * th) * g), ColorUtil.replAlpha(ColorUtil.client(), 0.55F * g), enabled),
         toggleH
      );
      float knob = 1.5F * unit + (toggleW - toggleH - 1.0F * unit) * enabled;
      RenderUtil.Render2D.rect(
         toggleX + knob,
         toggleY + 1.5F * unit,
         toggleH - 3.0F * unit,
         toggleH - 3.0F * unit,
         ColorUtil.getColor(255, g * (0.55F + 0.45F * Math.max(enabled, th))),
         toggleH - 3.0F * unit
      );
      boolean expHovered = MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, expandX, expandY, expandW, expandW);
      ru.mentions.utils.animation.satoshi.Animation expAnim = this.chipAnim("exp:" + f.getName());
      boolean isOpen = f == this.expanded;
      expAnim.setDirection(!isOpen && !expHovered ? Direction.BACKWARDS : Direction.FORWARDS);
      float ep = expAnim.getOutput();
      RenderUtil.Render2D.rect(
         expandX,
         expandY,
         expandW,
         expandW,
         ColorUtil.overCol(
            ColorUtil.getColor(255, 0.07F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.3F * g), isOpen ? 1.0F : Math.min(1.0F, ep * 0.5F + 0.5F * th)
         ),
         4.0F * unit
      );
      regular.drawCentered(isOpen ? "-" : "+", expandX + expandW / 2.0F, expandY + 2.0F * unit, 7.0F * unit, ColorUtil.getColor(255, g * (0.45F + 0.45F * ep)));
      float descY = titleY + 13.0F * unit;
      float descW = this.colW - 14.0F * unit;
      String desc = f.getDesc() == null ? "" : f.getDesc();
      if (!desc.isEmpty()) {
         regular.drawWrappedText(desc, x + 7.0F * unit, descY, descW, ColorUtil.getColor(255, g * (0.18F + 0.35F * enabled + 0.18F * hv)), 6.5F * unit);
      }

      float bodyH = 5.0F * unit + 13.0F * unit + this.descHeight(regular, f, descW) + 5.0F * unit;
      this.hit(x, y, this.colW, bodyH, 0, () -> this.toggleModule(f));
      this.hit(x, y, this.colW, bodyH, 1, () -> this.toggleExpanded(f));
      this.hit(x, y, this.colW, bodyH, 2, () -> this.startBinding(f));
      this.hit(toggleX - 1.5F * unit, toggleY - 1.5F * unit, toggleW + 3.0F * unit, toggleH + 3.0F * unit, 0, () -> this.toggleModule(f));
      this.hit(expandX, expandY, expandW, expandW, 0, () -> this.toggleExpanded(f));
      if (isOpen) {
         float sh = this.settingsHeight(f, this.settingsWidth());
         if (sh > 0.0F) {
            float insetY = y + bodyH;
            float insetH = 5.0F * unit + sh;
            RenderUtil.Render2D.rect(x + 4.0F * unit, insetY, this.colW - 8.0F * unit, insetH, ColorUtil.getColor(0, 0.22F * g), 5.0F * unit);
            this.drawSettings(f, x + 10.0F * unit, insetY + 5.0F * unit, this.settingsWidth(), g);
         }
      }
   }

   private void toggleModule(Module f) {
      if (!f.isAllowDisable()) {
         this.toggleExpanded(f);
      } else {
         f.setEnabled(!f.isEnabled());
         GuiSounds.toggle(f.isEnabled());
      }
   }

   private void toggleExpanded(Module f) {
      if (this.expanded == f) {
         this.expanded = null;
         GuiSounds.expand(false);
      } else {
         this.expanded = f;
         GuiSounds.expand(true);
      }
   }

   private void startBinding(Module f) {
      this.bindingModule = f;
      GuiSounds.bindStart();
   }

   private void drawSettings(Module f, float x, float y, float w, float g) {
      Font regular = Fonts.sf_regular;
      float unit = this.u;
      float cy = y;

      for (Setting<?> setting : f.getSettings()) {
         float vis = this.settingVis(f, setting);
         if (!(vis <= 0.01F)) {
            float h = this.settingHeight(setting, w);
            if (!(h <= 0.0F)) {
               this.drawSetting(f, setting, x, cy, w, h, vis, g, regular, unit);
               cy += (h + 5.0F * unit) * vis;
            }
         }
      }
   }

   private void drawSetting(Module f, Setting<?> setting, float x, float y, float w, float h, float vis, float g, Font regular, float unit) {
      boolean visible = Boolean.TRUE.equals(setting.getVisible().get());
      g *= vis;
      float rowH = Math.min(h, setting instanceof SliderSetting ? 26.0F * unit : (setting instanceof DelimiterSetting ? 13.0F * unit : 15.0F * unit));
      boolean hovered = visible && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, x, y, w, h);
      if (setting instanceof DelimiterSetting) {
         String label = setting.getDisplayName();
         regular.draw(label, x, y + 3.0F * unit, 6.5F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * 0.85F));
         float lw = regular.getWidth(label, 6.5F * unit);
         RenderUtil.Render2D.rect(
            x + lw + 6.0F * unit,
            y + 6.0F * unit,
            Math.max(0.0F, w - lw - 6.0F * unit),
            0.7F * unit,
            ColorUtil.replAlpha(ColorUtil.getColor(255), g * 0.12F),
            0.4F * unit
         );
      } else {
         ru.mentions.utils.animation.satoshi.Animation hoverAnim = this.chipAnim("set:" + f.getName() + ":" + setting.getName());
         hoverAnim.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
         float hv = hoverAnim.getOutput();
         if (setting instanceof SliderSetting s) {
            RenderUtil.Render2D.rect(x, y, w, rowH, ColorUtil.getColor(255, (0.055F + 0.03F * hv) * g), 4.0F * unit);
            regular.draw(s.getDisplayName(), x + 6.0F * unit, y + 4.0F * unit, 6.5F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * hv)));
            String val = s.displayValue();
            regular.draw(
               val,
               x + w - 6.0F * unit - regular.getWidth(val, 6.5F * unit),
               y + 4.0F * unit,
               6.5F * unit,
               ColorUtil.replAlpha(ColorUtil.client(), g * (0.65F + 0.35F * hv))
            );
            float trackX = x + 6.0F * unit;
            float trackW = w - 12.0F * unit;
            float trackY = y + 16.5F * unit;
            float percent = MathUtil.clamp((s.getValue() - s.min) / Math.max(1.0E-5F, s.max - s.min), 0.0F, 1.0F);
            s.getAnimation().update();
            s.getAnimation().run((double)percent, 0.06F, Easings.LINEAR);
            float shown = s.getAnimation().get();
            RenderUtil.Render2D.rect(trackX, trackY, trackW, 3.0F * unit, ColorUtil.getColor(0, g * 0.4F), 1.5F * unit);
            RenderUtil.Render2D.rect(
               trackX, trackY, trackW * shown, 3.0F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * (0.55F + 0.45F * hv)), 1.5F * unit
            );
            RenderUtil.Render2D.rect(
               trackX + trackW * shown - 3.0F * unit, trackY - 1.5F * unit, 6.0F * unit, 6.0F * unit, ColorUtil.getColor(255, g), 6.0F * unit
            );
            RenderUtil.Render2D.rect(
               trackX + trackW * shown - 2.0F * unit, trackY - 0.5F * unit, 4.0F * unit, 4.0F * unit, ColorUtil.replAlpha(ColorUtil.client(), g), 4.0F * unit
            );
            if (visible) {
               this.hit(x, y, w, rowH, 0, () -> {
                  this.draggingSlider = s;
                  this.dragTrackX = trackX;
                  this.dragTrackW = trackW;
                  GuiSounds.sliderGrab();
               });
            }

            if (this.draggingSlider == s) {
               float perc = MathUtil.clamp(((float)this.lastMouseX - this.dragTrackX) / Math.max(1.0F, this.dragTrackW), 0.0F, 1.0F);
               float newVal = MathUtil.clamp(MathUtil.round(s.min + (s.max - s.min) * perc, s.increment), s.min, s.max);
               if (s.getValue() != newVal) {
                  s.set(newVal);
                  GuiSounds.sliderTick(perc);
               }
            }
         } else if (setting instanceof ModeSetting s) {
            this.drawComboHeader(s, s.getDisplayName(), s.getDisplayValue(), s.opened, x, y, w, rowH, hv, g, regular, unit, f);
            if (visible) {
               this.hit(x, y, w, rowH, 0, () -> {
                  s.opened = !s.opened;
                  GuiSounds.picker(s.opened);
               });
            }

            float oy = y + rowH + 4.0F * unit;
            if (s.opened) {
               int i = 0;

               for (String valx : s.values) {
                  float oy2 = oy + (float)i * 16.0F * unit;
                  boolean oh = visible && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, x, oy2, w, 15.0F * unit);
                  boolean isSel = valx.equals(s.getValue());
                  ru.mentions.utils.animation.satoshi.Animation a = this.chipAnim("mode:" + f.getName() + ":" + s.getName() + ":" + valx);
                  a.setDirection(!isSel && !oh ? Direction.BACKWARDS : Direction.FORWARDS);
                  float pa = a.getOutput();
                  RenderUtil.Render2D.rect(
                     x,
                     oy2,
                     w,
                     15.0F * unit,
                     ColorUtil.overCol(
                        ColorUtil.getColor(255, (0.05F + 0.05F * pa) * g), ColorUtil.replAlpha(ColorUtil.client(), 0.25F * g), isSel ? pa : pa * 0.4F
                     ),
                     4.0F * unit
                  );
                  regular.draw(
                     RussianUi.text(valx),
                     x + 8.0F * unit,
                     oy2 + 4.5F * unit,
                     6.5F * unit,
                     ColorUtil.overCol(ColorUtil.getColor(255, g * (0.45F + 0.45F * pa)), ColorUtil.replAlpha(ColorUtil.client(), g), isSel ? 1.0F : 0.0F)
                  );
                  if (isSel) {
                     regular.drawCentered(">", x + w - 9.0F * unit, oy2 + 4.5F * unit, 6.5F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * pa));
                  }

                  int capturedIndex = i;
                  if (visible) {
                     this.hit(
                        x,
                        oy2,
                        w,
                        15.0F * unit,
                        0,
                        () -> {
                           if (!val.equals(s.getValue())) {
                              GuiSounds.chip(capturedIndex, s.values.size());
                              if ("Тип уведомления".equals(s.getName())
                                 || "typeNotify".equals(s.getName())
                                 || s.getName().toLowerCase().contains("звук")
                                 || s.getName().toLowerCase().contains("sound")) {
                                 NotificationSound.playPreview(val);
                              }
                           }

                           s.set(val);
                           s.opened = false;
                        }
                     );
                  }

                  i++;
               }
            }
         } else if (setting instanceof MultiBooleanSetting s) {
            int total = s.getValues().size();
            int selected = 0;

            for (BooleanSetting b : s.getValues()) {
               if (!b.getValue()) {
                  selected++;
               }
            }

            String counter = total - selected + " / " + total;
            this.drawComboHeader(s, s.getDisplayName(), counter, s.opened, x, y, w, rowH, hv, g, regular, unit, f);
            if (visible) {
               this.hit(x, y, w, rowH, 0, () -> {
                  s.opened = !s.opened;
                  GuiSounds.picker(s.opened);
               });
            }

            float oy = y + rowH + 4.0F * unit;
            if (s.opened) {
               int i = 0;

               for (BooleanSetting bx : s.getValues()) {
                  float oy2x = oy + (float)i * 16.0F * unit;
                  boolean on = bx.getValue();
                  boolean ohx = visible && MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, x, oy2x, w, 15.0F * unit);
                  ru.mentions.utils.animation.satoshi.Animation ax = this.chipAnim("multi:" + f.getName() + ":" + s.getName() + ":" + bx.getName());
                  ax.setDirection(!on && !ohx ? Direction.BACKWARDS : Direction.FORWARDS);
                  float pax = ax.getOutput();
                  RenderUtil.Render2D.rect(
                     x,
                     oy2x,
                     w,
                     15.0F * unit,
                     ColorUtil.overCol(
                        ColorUtil.getColor(255, (0.05F + 0.05F * pax) * g), ColorUtil.replAlpha(ColorUtil.client(), 0.25F * g), on ? pax : pax * 0.4F
                     ),
                     4.0F * unit
                  );
                  RenderUtil.Render2D.rect(
                     x + 5.0F * unit,
                     oy2x + 4.5F * unit,
                     6.0F * unit,
                     6.0F * unit,
                     ColorUtil.overCol(ColorUtil.getColor(0, 0.45F * g), ColorUtil.replAlpha(ColorUtil.client(), g), on ? pax : 0.0F),
                     2.0F * unit
                  );
                  regular.draw(bx.getDisplayName(), x + 15.0F * unit, oy2x + 4.5F * unit, 6.5F * unit, ColorUtil.getColor(255, g * (0.45F + 0.45F * pax)));
                  if (visible) {
                     this.hit(x, oy2x, w, 15.0F * unit, 0, () -> {
                        b.set(!b.getValue());
                        GuiSounds.chipMulti(b.getValue());
                     });
                  }

                  i++;
               }
            }
         } else if (setting instanceof BooleanSetting s) {
            boolean value = s.getValue();
            ru.mentions.utils.animation.satoshi.Animation onAnim = this.chipAnim("bool:" + f.getName() + ":" + s.getName());
            onAnim.setDirection(value ? Direction.FORWARDS : Direction.BACKWARDS);
            float on = onAnim.getOutput();
            RenderUtil.Render2D.rect(x, y, w, rowH, ColorUtil.getColor(255, (0.055F + 0.03F * hv) * g), 4.0F * unit);
            regular.drawFadingText(
               s.getDisplayName(), x + 6.0F * unit, y + 4.5F * unit, w - 30.0F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * hv)), 6.5F * unit
            );
            float tw = 17.0F * unit;
            float tx = x + w - 6.0F * unit - tw;
            RenderUtil.Render2D.rect(
               tx,
               y + 3.5F * unit,
               tw,
               8.0F * unit,
               ColorUtil.overCol(ColorUtil.getColor(255, 0.1F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.55F * g), on),
               4.0F * unit
            );
            float knob = 1.25F * unit + (tw - 8.0F * unit - 1.0F * unit) * on;
            RenderUtil.Render2D.rect(
               tx + knob, y + 4.75F * unit, 5.5F * unit, 5.5F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * Math.max(on, hv))), 5.5F * unit
            );
            if (visible) {
               this.hit(x, y, w, rowH, 0, () -> {
                  s.set(!s.getValue());
                  GuiSounds.toggle(s.getValue());
               });
            }
         } else if (setting instanceof ButtonSetting s) {
            s.pressAnim.update();
            float press = s.pressAnim.get();
            float accent = Math.max(hv, press);
            RenderUtil.Render2D.rect(
               x, y, w, rowH, ColorUtil.overCol(ColorUtil.getColor(255, 0.07F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.28F * g), accent), 4.0F * unit
            );
            RenderUtil.Render2D.outline(x, y, w, rowH, 0.5F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * accent), 4.0F * unit);
            regular.drawCentered(
               s.getDisplayName(),
               x + w / 2.0F,
               y + 4.5F * unit,
               6.5F * unit,
               ColorUtil.overCol(ColorUtil.getColor(255, g * (0.55F + 0.45F * hv)), ColorUtil.replAlpha(ColorUtil.client(), g), press)
            );
            if (visible) {
               this.hit(x, y, w, rowH, 0, () -> {
                  s.press();
                  GuiSounds.button();
               });
            }
         } else if (setting instanceof BindSetting s) {
            boolean binding = this.activeBind == s;
            ru.mentions.utils.animation.satoshi.Animation ax = this.chipAnim("bind:" + f.getName() + ":" + s.getName());
            ax.setDirection(binding ? Direction.FORWARDS : Direction.BACKWARDS);
            float ba = ax.getOutput();
            RenderUtil.Render2D.rect(x, y, w, rowH, ColorUtil.getColor(255, (0.055F + 0.03F * hv) * g), 4.0F * unit);
            regular.drawFadingText(
               s.getDisplayName(), x + 6.0F * unit, y + 4.5F * unit, w - 40.0F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * hv)), 6.5F * unit
            );
            String keyName = binding ? "..." : (s.get() == -1 ? "n/a" : Keyboard.keyName(s.get()));
            float bw = regular.getWidth(keyName, 6.5F * unit) + 10.0F * unit;
            float bx = x + w - 6.0F * unit - bw;
            RenderUtil.Render2D.rect(
               bx,
               y + 3.0F * unit,
               bw,
               9.0F * unit,
               ColorUtil.overCol(ColorUtil.getColor(0, 0.35F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.3F * g), ba),
               3.0F * unit
            );
            RenderUtil.Render2D.outline(bx, y + 3.0F * unit, bw, 9.0F * unit, 0.5F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * ba), 3.0F * unit);
            regular.drawCentered(
               keyName,
               bx + bw / 2.0F,
               y + 4.5F * unit,
               6.5F * unit,
               ColorUtil.overCol(ColorUtil.getColor(255, g * 0.65F), ColorUtil.replAlpha(ColorUtil.client(), g), ba)
            );
            if (visible) {
               this.hit(bx - 2.0F * unit, y, bw + 4.0F * unit, rowH, 0, () -> {
                  this.activeBind = s;
                  GuiSounds.bindStart();
               });
            }
         } else if (setting instanceof StringSetting s) {
            boolean editing = this.activeString == s;
            ru.mentions.utils.animation.satoshi.Animation ax = this.chipAnim("str:" + f.getName() + ":" + s.getName());
            ax.setDirection(editing ? Direction.FORWARDS : Direction.BACKWARDS);
            float ea = ax.getOutput();
            RenderUtil.Render2D.rect(x, y, w, rowH, ColorUtil.getColor(255, (0.055F + 0.03F * hv) * g), 4.0F * unit);
            regular.drawFadingText(
               s.getDisplayName(), x + 6.0F * unit, y + 4.5F * unit, w - 50.0F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * hv)), 6.5F * unit
            );
            String shownx = editing ? this.stringBuffer + "_" : s.getValue();
            if (shownx.isEmpty()) {
               shownx = "...";
            }

            float bw = Math.min(regular.getWidth(shownx, 6.5F * unit) + 10.0F * unit, w * 0.5F);
            float bx = x + w - 6.0F * unit - bw;
            RenderUtil.Render2D.rect(
               bx,
               y + 3.0F * unit,
               bw,
               9.0F * unit,
               ColorUtil.overCol(ColorUtil.getColor(0, 0.35F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.3F * g), ea),
               3.0F * unit
            );
            RenderUtil.Render2D.outline(bx, y + 3.0F * unit, bw, 9.0F * unit, 0.5F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * ea), 3.0F * unit);
            regular.drawFadingText(
               shownx, bx + 5.0F * unit, y + 4.5F * unit, bw - 8.0F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * Math.max(ea, hv))), 6.5F * unit
            );
            if (visible) {
               this.hit(bx - 2.0F * unit, y, bw + 4.0F * unit, rowH, 0, () -> {
                  this.activeString = s;
                  this.stringBuffer = s.getValue();
                  this.searchActive = false;
                  GuiSounds.editStart();
               });
            }
         } else {
            if (setting instanceof ColorSetting s) {
               s.pickerAnim.setDirection(s.pickerOpen ? Direction.FORWARDS : Direction.BACKWARDS);
               float open = s.pickerAnim.getOutput();
               RenderUtil.Render2D.rect(x, y, w, rowH, ColorUtil.getColor(255, (0.055F + 0.03F * hv) * g), 4.0F * unit);
               regular.drawFadingText(
                  s.getDisplayName(), x + 6.0F * unit, y + 4.5F * unit, w - 34.0F * unit, ColorUtil.getColor(255, g * (0.55F + 0.45F * hv)), 6.5F * unit
               );
               int col = s.getValue();
               int pr = col >> 16 & 0xFF;
               int pg = col >> 8 & 0xFF;
               int pb = col & 0xFF;
               float pcx = x + w - 6.0F * unit - 9.0F * unit;
               float pcy = y + 3.0F * unit;
               RenderUtil.Render2D.rect(pcx, pcy, 9.0F * unit, 9.0F * unit, ColorUtil.getColor(pr, pg, pb, g), 9.0F * unit);
               RenderUtil.Render2D.outline(
                  pcx - 0.75F * unit,
                  pcy - 0.75F * unit,
                  10.5F * unit,
                  10.5F * unit,
                  0.5F * unit,
                  ColorUtil.getColor(pr, pg, pb, g * (0.5F + 0.5F * hv)),
                  9.0F * unit
               );
               if (visible) {
                  this.hit(x, y, w, rowH, 0, () -> {
                     s.pickerOpen = !s.pickerOpen;
                     GuiSounds.picker(s.pickerOpen);
                  });
               }

               if (open > 0.01F) {
                  float[] hsb = Color.RGBtoHSB(pr, pg, pb, null);
                  float bx = x + 6.0F * unit;
                  float bwd = w - 12.0F * unit;
                  int segs = 16;
                  float seg = bwd / (float)segs;

                  for (int bar = 0; bar < 3; bar++) {
                     float by = y + rowH + 4.0F * unit + (float)bar * 12.0F * unit;

                     for (int i = 0; i < segs; i++) {
                        float t0 = (float)i / (float)segs;
                        float t1 = (float)(i + 1) / (float)segs;
                        int c0 = bar == 0
                           ? Color.HSBtoRGB(t0, 1.0F, 1.0F)
                           : (bar == 1 ? Color.HSBtoRGB(hsb[0], t0, hsb[2]) : Color.HSBtoRGB(hsb[0], hsb[1], t0));
                        int c1 = bar == 0
                           ? Color.HSBtoRGB(t1, 1.0F, 1.0F)
                           : (bar == 1 ? Color.HSBtoRGB(hsb[0], t1, hsb[2]) : Color.HSBtoRGB(hsb[0], hsb[1], t1));
                        int a0 = ColorUtil.replAlpha(c0, g * open);
                        int a1 = ColorUtil.replAlpha(c1, g * open);
                        RenderUtil.Render2D.gradientRect(
                           bx + (float)i * seg,
                           by,
                           seg + (i == segs - 1 ? 0.5F : 0.0F),
                           4.0F * unit,
                           new int[]{a0, a1, a1, a0},
                           i == 0 ? 2.0F * unit : 0.0F,
                           i == segs - 1 ? 2.0F * unit : 0.0F,
                           i == segs - 1 ? 2.0F * unit : 0.0F,
                           i == 0 ? 2.0F * unit : 0.0F
                        );
                     }

                     float kx = bx + bwd * hsb[bar];
                     int kc = bar == 0 ? Color.HSBtoRGB(hsb[0], 1.0F, 1.0F) : Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                     RenderUtil.Render2D.rect(kx - 3.0F * unit, by - 1.0F * unit, 6.0F * unit, 6.0F * unit, ColorUtil.getColor(255, g * open), 6.0F * unit);
                     RenderUtil.Render2D.rect(kx - 2.0F * unit, by, 4.0F * unit, 4.0F * unit, ColorUtil.replAlpha(kc, g * open), 4.0F * unit);
                     if (visible && this.draggingColor == s && this.draggingColorBar == bar) {
                     }

                     int capturedBar = bar;
                     float capX = bx;
                     float capW = bwd;
                     if (visible && open > 0.01F) {
                        this.hit(bx - 2.0F * unit, by - 3.0F * unit, bwd + 4.0F * unit, 10.0F * unit, 0, () -> {
                           this.draggingColor = s;
                           this.draggingColorBar = capturedBar;
                           this.dragColorX = capX;
                           this.dragColorW = capW;
                           GuiSounds.sliderGrab();
                        });
                     }
                  }

                  if (this.draggingColor == s) {
                     float[] cur = Color.RGBtoHSB(pr, pg, pb, null);
                     float t = MathUtil.clamp(((float)this.lastMouseX - this.dragColorX) / Math.max(1.0F, this.dragColorW), 0.0F, 1.0F);
                     float[] nh = new float[]{cur[0], cur[1], cur[2]};
                     nh[this.draggingColorBar] = t;
                     int rgb = Color.HSBtoRGB(nh[0], nh[1], nh[2]);
                     int newCol = col & 0xFF000000 | rgb & 16777215;
                     if (newCol != col) {
                        s.set(newCol);
                        GuiSounds.colorTick(t);
                     }
                  }
               }
            }
         }
      }
   }

   private void drawComboHeader(
      Setting<?> s, String label, String value, boolean opened, float x, float y, float w, float h, float hv, float g, Font regular, float unit, Module f
   ) {
      RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.getColor(255, (0.055F + 0.03F * hv) * g), 4.0F * unit);
      float vw = regular.getWidth(value, 6.5F * unit) + 10.0F * unit;
      float chipX = x + w - 16.0F * unit - vw;
      regular.drawFadingText(
         label, x + 6.0F * unit, y + 4.5F * unit, Math.max(0.0F, chipX - x - 10.0F * unit), ColorUtil.getColor(255, g * (0.55F + 0.45F * hv)), 6.5F * unit
      );
      ru.mentions.utils.animation.satoshi.Animation a = this.chipAnim("combo:" + f.getName() + ":" + s.getName());
      a.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
      float pa = a.getOutput();
      RenderUtil.Render2D.rect(
         chipX,
         y + 3.0F * unit,
         vw,
         9.0F * unit,
         ColorUtil.overCol(ColorUtil.getColor(0, 0.35F * g), ColorUtil.replAlpha(ColorUtil.client(), 0.3F * g), pa),
         3.0F * unit
      );
      RenderUtil.Render2D.outline(chipX, y + 3.0F * unit, vw, 9.0F * unit, 0.5F * unit, ColorUtil.replAlpha(ColorUtil.client(), g * pa), 3.0F * unit);
      regular.drawFadingText(
         value,
         chipX + 5.0F * unit,
         y + 4.5F * unit,
         vw - 8.0F * unit,
         ColorUtil.overCol(ColorUtil.getColor(255, g * 0.7F), ColorUtil.replAlpha(ColorUtil.client(), g), pa),
         6.5F * unit
      );
      regular.drawCentered(
         opened ? "-" : "+", x + w - 8.0F * unit, y + 4.5F * unit, 6.5F * unit, ColorUtil.replAlpha(ColorUtil.getColor(255), g * (0.35F + 0.45F * pa))
      );
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
         GuiSounds.click();
         mc.method_1507(new ScreamerScreen(this));
      }
   }

   private void hit(float x, float y, float w, float h, int button, Runnable action) {
      this.hits.add(new Menu2.Hit(x, y, w, h, button, action));
   }

   private boolean dispatchClick(double mouseX, double mouseY, int button) {
      for (int i = this.hits.size() - 1; i >= 0; i--) {
         Menu2.Hit hit = this.hits.get(i);
         if (hit.button == button && MathUtil.isHovered((float)mouseX, (float)mouseY, hit.x, hit.y, hit.w, hit.h)) {
            hit.action.run();
            return true;
         }
      }

      return false;
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         float editorMouseX = (float)(mc.field_1729.method_68879(mc.method_22683()) / (double)this.scaleFix);
         float editorMouseY = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.scaleFix);
         return editor.mouseClicked(editorMouseX, editorMouseY, click.method_74245());
      } else {
         float mouseX = (float)(click.comp_4798() / (double)this.scaleFix);
         float mouseY = (float)(click.comp_4799() / (double)this.scaleFix);
         this.lastMouseX = (double)mouseX;
         this.lastMouseY = (double)mouseY;
         int button = click.method_74245();
         if (this.exit) {
            return true;
         } else if (this.management.hasConfirmation()) {
            return this.management.click(mouseX, mouseY, button);
         } else if (this.management.clickNavigation(mouseX, mouseY, button)) {
            return true;
         } else if (this.management.active() && this.management.click(mouseX, mouseY, button)) {
            return true;
         } else if (this.bindingModule != null) {
            this.bindingModule = null;
            GuiSounds.bindReset();
            return true;
         } else if (this.activeBind == null) {
            if (this.activeString != null) {
               this.activeString.set(this.stringBuffer);
               this.activeString = null;
               GuiSounds.editCommit();
            }

            float gripX = this.panelX + this.panelW - 16.0F * this.u;
            float gripY = this.panelY + this.panelH - 14.0F * this.u;
            if (button == 0 && MathUtil.isHovered(mouseX, mouseY, gripX, gripY, 18.0F * this.u, 18.0F * this.u)) {
               this.resizing = true;
               this.resizeStartX = mouseX;
               this.resizeStartY = mouseY;
               this.resizeStartExtraW = extraW2;
               this.resizeStartExtraH = extraH2;
               this.resizeBlurFade = 1.0F;
               GuiSounds.sliderGrab();
               return true;
            } else {
               return this.dispatchClick((double)mouseX, (double)mouseY, button) ? true : super.method_25402(click, doubled);
            }
         } else {
            if (button != 0 && button != 1) {
               this.activeBind.set(button);
               GuiSounds.bindSet();
            } else {
               GuiSounds.bindReset();
            }

            this.activeBind = null;
            return true;
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
         if (this.resizing) {
            int sw = this.screenW();
            int sh = this.screenH();
            float maxExtraW = Math.max(0.0F, (float)sw * 0.95F - 630.0F * this.u);
            float maxExtraH = Math.max(0.0F, (float)sh * 0.95F - 430.0F * this.u);
            extraW2 = MathUtil.clamp(this.resizeStartExtraW + (mouseX - this.resizeStartX), 0.0F, maxExtraW);
            extraH2 = MathUtil.clamp(this.resizeStartExtraH + (mouseY - this.resizeStartY), 0.0F, maxExtraH);
            this.resizeBlurFade = 1.0F;
            return true;
         } else if (this.draggingSlider != null) {
            float perc = MathUtil.clamp((mouseX - this.dragTrackX) / Math.max(1.0F, this.dragTrackW), 0.0F, 1.0F);
            SliderSetting s = this.draggingSlider;
            float newVal = MathUtil.clamp(MathUtil.round(s.min + (s.max - s.min) * perc, s.increment), s.min, s.max);
            if (s.getValue() != newVal) {
               s.set(newVal);
               GuiSounds.sliderTick(perc);
            }

            return true;
         } else if (this.draggingColor != null) {
            float t = MathUtil.clamp((mouseX - this.dragColorX) / Math.max(1.0F, this.dragColorW), 0.0F, 1.0F);
            int col = this.draggingColor.getValue();
            float[] hsb = Color.RGBtoHSB(col >> 16 & 0xFF, col >> 8 & 0xFF, col & 0xFF, null);
            hsb[this.draggingColorBar] = t;
            int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
            int newCol = col & 0xFF000000 | rgb & 16777215;
            if (newCol != col) {
               this.draggingColor.set(newCol);
               GuiSounds.colorTick(t);
            }

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
         if (this.resizing) {
            this.resizing = false;
            GuiSounds.sliderRelease();
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
         GuiSounds.bindReset();
         return true;
      } else if (this.activeBind != null) {
         boolean reset = key == 256 || key == 261;
         this.activeBind.set(reset ? -1 : key);
         this.activeBind = null;
         GuiSounds.bindReset();
         return true;
      } else {
         ClickGui clickGui = Client.get().moduleManager().get(ClickGui.class);
         if (clickGui != null && clickGui.isMenuKey(key)) {
            mc.method_1507(null);
            return true;
         } else if (this.searchActive) {
            if (key == 257 || key == 335) {
               this.searchActive = false;
               GuiSounds.editCommit();
            } else if (key == 259 && !this.searchQuery.isEmpty()) {
               this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
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
      } else if (this.searchActive) {
         if (input.method_74227() && this.searchQuery.length() < 24) {
            this.searchQuery = this.searchQuery + input.method_74226();
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
      OverlayEditor editor = OverlayEditors.active();
      if (editor != null) {
         float editorMouseX = (float)(mouseXRaw / (double)this.scaleFix);
         float editorMouseY = (float)(mouseYRaw / (double)this.scaleFix);
         return editor.mouseScrolled(editorMouseX, editorMouseY, verticalAmount);
      } else {
         float mouseX = (float)(mouseXRaw / (double)this.scaleFix);
         float mouseY = (float)(mouseYRaw / (double)this.scaleFix);
         if (this.management.active()) {
            return this.management.scrolled(mouseX, mouseY, verticalAmount);
         } else if (MathUtil.isHovered(mouseX, mouseY, this.contentX, this.contentY, this.contentW, this.contentH)) {
            this.scrollTarget = MathUtil.clamp((float)((double)this.scrollTarget - verticalAmount * 25.0 * (double)S), 0.0F, this.maxScroll);
            return true;
         } else {
            return super.method_25401(mouseXRaw, mouseYRaw, horizontalAmount, verticalAmount);
         }
      }
   }

   @Override
   public boolean isSearchActive() {
      return this.searchActive || this.management.typing();
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
      HandsEditor.getInstance().open();
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
      this.anim.run(1.0, 0.22, Easings.EXPO_OUT);
   }

   private static final class Hit {
      final float x;
      final float y;
      final float w;
      final float h;
      final int button;
      final Runnable action;

      Hit(float x, float y, float w, float h, int button, Runnable action) {
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
         this.button = button;
         this.action = action;
      }
   }
}
