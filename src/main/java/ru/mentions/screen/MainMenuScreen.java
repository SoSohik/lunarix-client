package ru.mentions.screen;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;
import ru.mentions.alt.AltManager;
import ru.mentions.lang.Lang;
import ru.mentions.module.impl.misc.Optimization;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.Easings;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.other.GuiSounds;
import ru.mentions.utils.render.Draw;
import ru.mentions.utils.render.FrameTextures;
import ru.mentions.utils.render.IconSplashEffect;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.ScreenBlur;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;
import ru.mentions.window.DualWindowService;

@Environment(EnvType.CLIENT)
public class MainMenuScreen extends class_437 implements IMinecraft {
   public static final class_2960 LOGO_TEXTURE = class_2960.method_60655("client", "textures/logo.png");
   public Animation alpha = new Animation();
   private final List<MainMenuScreen.MenuButton> buttons = new ArrayList<>();
   private MainMenuScreen.MenuButton multiplayerButton;
   private MainMenuScreen.MenuButton singleplayerButton;
   private MainMenuScreen.MenuButton settingsButton;
   private MainMenuScreen.MenuButton accountsButton;
   private MainMenuScreen.MenuButton modsButton;
   private MainMenuScreen.MenuButton exitButton;
   boolean exit = false;
   private float scaleFix = 1.0F;
   double lastMouseX;
   double lastMouseY;
   private boolean exitSlideDragging = false;
   private float exitSlideProgress = 0.0F;
   private final Animation langHover = new Animation();
   private static final class_2960[] MENU_BGS = new class_2960[]{
      class_2960.method_60655("client", "textures/frame/menu.png"),
      class_2960.method_60655("client", "textures/frame/menu2.png"),
      class_2960.method_60655("client", "textures/frame/menu3.png"),
      class_2960.method_60655("client", "textures/frame/menu4.png")
   };
   private static final float[] MENU_BG_W = new float[]{2560.0F, 2560.0F, 2560.0F, 1920.0F};
   private static final float[] MENU_BG_H = new float[]{1440.0F, 1440.0F, 1440.0F, 1080.0F};
   private static int menuBgSaved = 0;
   private static float menuBgPos = 0.0F;
   private static float menuBgTarget = 0.0F;
   private static float menuBgLastPos = 0.0F;
   private static boolean menuBgInited = false;
   private static final Animation menuBgAnim = new Animation();
   private final Animation edgeArrowL = new Animation();
   private final Animation edgeArrowR = new Animation();
   private static final float POINTER_RESPONSE_SECONDS = 0.14F;
   private float smoothPointerX;
   private float smoothPointerY;
   private long lastPointerUpdateNanos;
   private static MainMenuScreen.TimeOfDay manualTimeOverride = null;
   private static boolean manualBgSwitched = false;
   private final Animation timeBadgeHover = new Animation();
   private float timeBadgeX;
   private float timeBadgeY;
   private float timeBadgeW;
   private float timeBadgeH;
   private final List<MainMenuScreen.MenuParticle> particles = new ArrayList<>();

   public MainMenuScreen.TimeOfDay getTimeOfDay() {
      return manualTimeOverride != null ? manualTimeOverride : MainMenuScreen.TimeOfDay.current();
   }

   private void initParticles(float screenW, float screenH) {
      if (this.particles.isEmpty()) {
         Random rnd = new Random(1337L);

         for (int i = 0; i < 36; i++) {
            MainMenuScreen.MenuParticle p = new MainMenuScreen.MenuParticle();
            p.reset(screenW > 0.0F ? screenW : 960.0F, screenH > 0.0F ? screenH : 540.0F, rnd);
            this.particles.add(p);
         }
      }
   }

   public static void startBgSwitchTo(int targetIndex) {
      int target = (targetIndex % MENU_BGS.length + MENU_BGS.length) % MENU_BGS.length;
      menuBgSaved = target;
      float cur = menuBgAnim.get();
      int curRound = Math.round(cur);
      int curNorm = (curRound % MENU_BGS.length + MENU_BGS.length) % MENU_BGS.length;
      int diff = target - curNorm;
      if (diff > 2) {
         diff -= 4;
      }

      if (diff < -2) {
         diff += 4;
      }

      menuBgTarget = (float)(curRound + diff);
      menuBgAnim.run((double)menuBgTarget, 0.65F, Easings.CUBIC_OUT);
   }

   public MainMenuScreen() {
      super(class_2561.method_43470("MainMenuScreen"));
   }

   public static void setBgSaved(int i) {
      menuBgSaved = (i % MENU_BGS.length + MENU_BGS.length) % MENU_BGS.length;
   }

   public static int getBgSaved() {
      return menuBgSaved;
   }

   protected void method_25426() {
      this.exit = false;
      this.smoothPointerX = 0.0F;
      this.smoothPointerY = 0.0F;
      this.lastPointerUpdateNanos = 0L;
      this.alpha.set(0.0);
      this.alpha.run(1.0, 0.5, Easings.BACK_OUT);
      AltManager.get().bootstrap();
      this.buildButtons();
      MainMenuScreen.TimeOfDay tod = this.getTimeOfDay();
      if (!manualBgSwitched) {
         setBgSaved(tod.bgIndex);
         menuBgTarget = (float)tod.bgIndex;
         menuBgAnim.set((double)tod.bgIndex);
      }
   }

   private void buildButtons() {
      this.buttons.clear();
      this.singleplayerButton = new MainMenuScreen.MenuButton(
         Lang.pick("Одиночна гра", "Одиночная игра", "Singleplayer"),
         "Singleplayer",
         Lang.pick("Грати в офлайн-світи", "Играть в оффлайн-миры", "Play the offline worlds"),
         "Play the offline worlds",
         () -> mc.method_1507(new class_526(this))
      );
      this.multiplayerButton = new MainMenuScreen.MenuButton(
         Lang.pick("Мережева гра", "Сетевая игра", "Multiplayer"),
         "Multiplayer",
         Lang.pick("Грати на серверах", "Подключиться к мультиплеер-серверам", "Join and play on multiplayer servers"),
         "Join and play on multiplayer servers",
         () -> mc.method_1507(new class_500(this))
      );
      this.buttons.add(this.singleplayerButton);
      this.buttons.add(this.multiplayerButton);
      if (FabricLoader.getInstance().isModLoaded("modmenu")) {
         this.modsButton = new MainMenuScreen.MenuButton(
            Lang.pick("Моди", "Моды", "Mods"),
            "Mods",
            Lang.pick("Керування встановленими модифікаціями", "Управление установленными модификациями", "Manage your installed modifications"),
            "Manage your installed modifications",
            () -> {
               try {
                  Class<?> modMenuClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                  class_437 modsScreen = (class_437)modMenuClass.getConstructor(class_437.class).newInstance(this);
                  mc.method_1507(modsScreen);
               } catch (Exception var3) {
                  var3.printStackTrace();
               }
            }
         );
         this.buttons.add(this.modsButton);
      } else {
         this.modsButton = null;
      }

      this.accountsButton = new MainMenuScreen.MenuButton(
         Lang.pick("Акаунти", "Аккаунты", "Accounts"),
         "Accounts",
         Lang.pick("Керування акаунтами", "Переключение и управление аккаунтами", "Switch and manage your accounts"),
         "Switch and manage your accounts",
         () -> mc.method_1507(new AltManagerScreen(this))
      );
      MainMenuScreen.MenuButton clickGuiButton = new MainMenuScreen.MenuButton(
         Lang.pick("Меню клієнта", "Меню клиента", "Click GUI"),
         "Click GUI",
         Lang.pick("Відкрити меню налаштувань", "Открыть меню клиента", "Open the client menu"),
         "Open the client menu",
         () -> mc.method_1507(new Menu())
      );
      this.settingsButton = new MainMenuScreen.MenuButton(
         Lang.pick("Налаштування", "Настройки", "Settings"),
         "Settings",
         Lang.pick("Налаштування гри та відео", "Настройки игры и видео", "Adjust game and video options"),
         "Adjust game and video options",
         () -> mc.method_1507(new class_429(this, mc.field_1690))
      );
      this.exitButton = new MainMenuScreen.MenuButton(
         Lang.pick("Вихід", "Выход", "Exit"),
         "Exit",
         Lang.pick("Закрити гру", "Закрыть игру и выйти на рабочий стол", "Close the game and return to desktop"),
         "Close the game and return to desktop",
         true,
         () -> mc.method_1592()
      );
      this.buttons.add(this.settingsButton);
      this.buttons.add(clickGuiButton);
      this.buttons.add(this.accountsButton);
      this.buttons.add(this.exitButton);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (!DualWindowService.isCleanPass()) {
         this.closeCheck();
         this.scaleFix = 2.0F / (float)mc.method_22683().method_4495();
         mouseX = (int)((float)mouseX / this.scaleFix);
         mouseY = (int)((float)mouseY / this.scaleFix);
         this.lastMouseX = (double)mouseX;
         this.lastMouseY = (double)mouseY;
         this.alpha.update();
         this.renderOverlay(context);
      }
   }

   public void renderOverlay(class_332 context) {
      if (!DualWindowService.isCleanPass()) {
         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         this.scaleFix = targetScale / currentScale;
         int screenWidth = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
         int screenHeight = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
         if (context != null) {
            context.method_51448().pushMatrix();
         }

         Render2D.beginOverlay();
         FrameTextures.ensure();
         float alphaVal = Math.max(0.0F, Math.min(1.0F, this.alpha.get()));
         MainMenuScreen.TimeOfDay tod = this.getTimeOfDay();
         this.initParticles((float)screenWidth, (float)screenHeight);
         this.updatePointerMotion(screenWidth, screenHeight);
         float bgMouseX = (float)screenWidth * 0.5F + this.smoothPointerX * Math.min((float)screenWidth * 0.5F, 200.0F);
         float bgMouseY = (float)screenHeight * 0.5F + this.smoothPointerY * Math.min((float)screenHeight * 0.5F, 125.0F);
         renderMenuBg(screenWidth, screenHeight, alphaVal, bgMouseX, bgMouseY);
         ScreenBlur.capture(1);
         RenderUtil.Blur.blur(0.0F, 0.0F, (float)screenWidth, (float)screenHeight, alphaVal, ColorUtil.getColor(0, alphaVal * (tod.lightUi ? 0.42F : 0.25F)));
         RenderUtil.Render2D.rect(0.0F, 0.0F, (float)screenWidth, (float)screenHeight, ColorUtil.getColor(4, 5, 9, alphaVal * (tod.lightUi ? 0.4F : 0.28F)));
         this.drawAtmosphere(screenWidth, screenHeight, alphaVal, tod);

         for (MainMenuScreen.MenuParticle p : this.particles) {
            p.update((float)screenWidth, (float)screenHeight, tod);
            int pCol = ColorUtil.replAlpha(tod.primaryColor, p.alpha * alphaVal);
            Draw.rect(p.x, p.y, p.size, p.size, pCol, p.size * 0.5F);
            if (tod == MainMenuScreen.TimeOfDay.NIGHT && p.size > 2.0F && !Optimization.skipGlow()) {
               Draw.glow(p.x - 1.5F, p.y - 1.5F, p.size + 3.0F, p.size + 3.0F, tod.primaryColor, p.size, 3.5F, p.alpha * 0.35F * alphaVal);
            }
         }

         this.drawPointerLight(screenWidth, screenHeight, alphaVal, tod);
         ScreenBlur.capture(4);
         int timeAccent = ColorUtil.interpolateColor(ColorUtil.client(), tod.primaryColor, 0.45F);
         float greetingY = 14.0F + (1.0F - alphaVal) * -20.0F;
         this.drawGreetingCard(screenWidth, screenHeight, alphaVal, tod, greetingY);
         float totalBlockHeight = 83.0F;
         float buttonStartY = (float)screenHeight / 2.0F - totalBlockHeight / 2.0F;
         float appearY = (1.0F - alphaVal) * 34.0F;
         float logoSize = 48.0F;
         float emblemY = buttonStartY + appearY - 96.0F;
         float centerX = (float)screenWidth / 2.0F;
         RenderUtil.Render2D.glow(
            centerX - logoSize / 2.0F,
            emblemY - logoSize / 2.0F,
            logoSize,
            logoSize,
            ColorUtil.replAlpha(timeAccent, alphaVal * 0.55F),
            logoSize / 2.0F,
            18.0F,
            1.0F
         );
         RenderUtil.Images.texture(LOGO_TEXTURE, centerX - logoSize / 2.0F, emblemY - logoSize / 2.0F, logoSize, logoSize, ColorUtil.getColor(255, alphaVal));
         float titleY = emblemY + logoSize / 2.0F + 26.0F;
         Fonts.sf_bold.drawCentered("LUNARIX", centerX, titleY, 32.0F, ColorUtil.getColor(255, 255, 255, alphaVal));
         float highlightMouseX = (this.smoothPointerX + 1.0F) * (float)screenWidth * 0.5F;

         for (MainMenuScreen.MenuButton b : this.buttons) {
            b.updatePointer(this.lastMouseX, this.lastMouseY, highlightMouseX);
         }

         this.drawMenuButtons(screenWidth, screenHeight, alphaVal, tod);
         this.drawTimePill(screenWidth, screenHeight, alphaVal, tod);
         this.drawEdgeArrows(screenWidth, screenHeight, alphaVal);
         IconSplashEffect.updateAndRender(alphaVal);
         Render2D.endOverlay();
         if (context != null) {
            context.method_51448().popMatrix();
         }
      }
   }

   public static void renderMenuBg(int screenWidth, int screenHeight, float alphaVal, float mouseX, float mouseY) {
      float parallaxStrength = 0.04F;
      float offsetX = ((float)screenWidth / 2.0F - mouseX) * parallaxStrength;
      float offsetY = ((float)screenHeight / 2.0F - mouseY) * parallaxStrength;
      menuBgAnim.update();
      if (!menuBgInited) {
         menuBgTarget = (float)menuBgSaved;
         menuBgPos = (float)menuBgSaved;
         menuBgAnim.set((double)menuBgSaved);
         menuBgInited = true;
      }

      float pos = menuBgAnim.get();
      float dir = Math.signum(pos - menuBgLastPos);
      menuBgLastPos = pos;
      int len = MENU_BGS.length;
      float norm = (pos % (float)len + (float)len) % (float)len;
      int floorI = (int)Math.floor((double)norm);
      float frac = norm - (float)floorI;
      int idx0 = floorI % len;
      int idx1 = (idx0 + 1) % len;
      float slide = (float)screenWidth * 0.06F;
      int tIdx;
      int fIdx;
      float t;
      if (!(dir >= 0.0F) && !(frac < 0.001F) && !(frac > 0.999F)) {
         t = 1.0F - frac;
         fIdx = idx1;
         tIdx = idx0;
      } else {
         t = frac;
         fIdx = idx0;
         tIdx = idx1;
      }

      if (t < 0.001F) {
         float[] r = bgRect(fIdx, screenWidth, screenHeight, offsetX, offsetY, 0.0F);
         RenderUtil.Images.texture(MENU_BGS[fIdx], r[0], r[1], r[2], r[3], ColorUtil.getColor(255, alphaVal));
      } else {
         float a0 = (1.0F - t) * alphaVal;
         float a1 = t * alphaVal;
         float[] r0 = bgRect(fIdx, screenWidth, screenHeight, offsetX, offsetY, -dir * t * slide);
         float[] r1 = bgRect(tIdx, screenWidth, screenHeight, offsetX, offsetY, -dir * (t - 1.0F) * slide);
         RenderUtil.Images.texture(MENU_BGS[fIdx], r0[0], r0[1], r0[2], r0[3], ColorUtil.getColor(255, a0));
         RenderUtil.Images.texture(MENU_BGS[tIdx], r1[0], r1[1], r1[2], r1[3], ColorUtil.getColor(255, a1));
      }
   }

   private static float[] bgRect(int index, int screenW, int screenH, float offX, float offY, float slideOff) {
      float bgScale = 1.18F;
      float iw = MENU_BG_W[index];
      float ih = MENU_BG_H[index];
      float s = Math.max((float)screenW / iw, (float)screenH / ih) * bgScale;
      float w = iw * s;
      float h = ih * s;
      float x = ((float)screenW - w) / 2.0F + offX + slideOff;
      float y = ((float)screenH - h) / 2.0F + offY;
      return new float[]{x, y, w, h};
   }

   private void drawEdgeArrows(int screenW, int screenH, float alphaVal) {
      boolean leftHot = (float)this.lastMouseX < 36.0F
         || MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, 6.0F, (float)screenH / 2.0F - 14.0F, 28.0F, 28.0F);
      boolean rightHot = (float)this.lastMouseX > (float)screenW - 36.0F
         || MathUtil.isHovered((float)this.lastMouseX, (float)this.lastMouseY, (float)screenW - 34.0F, (float)screenH / 2.0F - 14.0F, 28.0F, 28.0F);
      this.edgeArrowL.update();
      this.edgeArrowR.update();
      this.edgeArrowL.run(leftHot ? 1.0 : 0.0, 0.25, Easings.QUAD_OUT);
      this.edgeArrowR.run(rightHot ? 1.0 : 0.0, 0.25, Easings.QUAD_OUT);
      float l = this.edgeArrowL.get();
      float r = this.edgeArrowR.get();
      if (l > 0.01F) {
         this.drawArrow(6.0F, (float)screenH / 2.0F - 14.0F, l, alphaVal, false);
      }

      if (r > 0.01F) {
         this.drawArrow((float)screenW - 34.0F, (float)screenH / 2.0F - 14.0F, r, alphaVal, true);
      }
   }

   private void drawArrow(float x, float y, float hp, float alphaVal, boolean right) {
      Font draw = Fonts.sf_regular;
      float size = 7.0F + 6.0F * hp;
      float cx = x + 14.0F;
      float cy = y + 14.0F;
      float a = alphaVal * hp;
      String g = right ? ">" : "<";
      draw.drawCentered(g, cx + 1.5F, cy + 1.5F, size, ColorUtil.getColor(0, a * 0.4F));
      draw.drawCentered(g, cx, cy, size, ColorUtil.getColor(255, a));
   }

   private void startBgSwitch(int dir) {
      manualBgSwitched = true;
      int cur = Math.round(menuBgAnim.get());
      menuBgSaved = ((cur + dir) % MENU_BGS.length + MENU_BGS.length) % MENU_BGS.length;
      menuBgAnim.run((double)(menuBgTarget += (float)dir), 0.55F, Easings.CUBIC_OUT);
   }

   private void drawMenuButtons(int screenWidth, int screenHeight, float alphaVal, MainMenuScreen.TimeOfDay tod) {
      this.updateExitSlideProgress();
      float gap = 4.0F;
      float panelWidth = Math.min(220.0F, (float)screenWidth - 36.0F);
      float cardWidth = (panelWidth - gap) / 2.0F;
      float cardHeight = 25.0F;
      float totalCardsWidth = cardWidth * 2.0F + gap;
      float smallGap = 4.0F;
      int smallCount = this.modsButton == null ? 2 : 3;
      float smallWidth = (totalCardsWidth - smallGap * (float)(smallCount - 1)) / (float)smallCount;
      float smallHeight = 25.0F;
      float exitWidth = Math.min(150.0F, totalCardsWidth * 0.65F);
      float exitHeight = 25.0F;
      float totalBlockHeight = cardHeight + gap + smallHeight + gap + exitHeight;
      float startX = (float)screenWidth / 2.0F - totalCardsWidth / 2.0F;
      float startY = (float)screenHeight / 2.0F - totalBlockHeight / 2.0F;
      float appearY = (1.0F - alphaVal) * 34.0F;
      float currentY = startY + appearY;
      boolean lightBg = tod.lightUi;
      this.singleplayerButton.drawHero(startX, currentY, cardWidth, cardHeight, alphaVal, 0, lightBg);
      this.multiplayerButton.drawHero(startX + cardWidth + gap, currentY, cardWidth, cardHeight, alphaVal, 1, lightBg);
      float var22;
      this.settingsButton.drawCompact(startX, var22 = currentY + cardHeight + gap, smallWidth, smallHeight, alphaVal, "l", lightBg);
      float smallX;
      this.accountsButton.drawCompact(smallX = startX + smallWidth + smallGap, var22, smallWidth, smallHeight, alphaVal, "m", lightBg);
      if (this.modsButton != null) {
         float var24;
         this.modsButton.drawCompact(var24 = smallX + smallWidth + smallGap, var22, smallWidth, smallHeight, alphaVal, "n", lightBg);
      }

      float exitX = (float)screenWidth / 2.0F - exitWidth / 2.0F;
      this.exitButton.drawExit(exitX, currentY = var22 + smallHeight + gap, exitWidth, exitHeight, alphaVal, this.exitSlideProgress, this.exitSlideDragging, lightBg);
   }

   private void updateExitSlideProgress() {
      if (this.exitSlideDragging && this.exitButton != null) {
         float trackX = this.exitButton.getLastX() + 5.0F;
         float trackW = Math.max(1.0F, this.exitButton.getLastW() - 34.0F);
         this.exitSlideProgress = Math.max(0.0F, Math.min(1.0F, ((float)this.lastMouseX - trackX) / trackW));
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float mouseX = (float)(click.comp_4798() / (double)this.scaleFix);
      float mouseY = (float)(click.comp_4799() / (double)this.scaleFix);
      int button = click.method_74245();
      this.lastMouseX = (double)mouseX;
      this.lastMouseY = (double)mouseY;
      if (button == 0) {
         float totalBlockHeight = 83.0F;
         float screenH = (float)mc.method_22683().method_4502() / this.scaleFix;
         float screenW = (float)mc.method_22683().method_4486() / this.scaleFix;
         float buttonStartY = screenH / 2.0F - totalBlockHeight / 2.0F;
         float appearY = (1.0F - this.alpha.get()) * 34.0F;
         float emblemY = buttonStartY + appearY - 96.0F;
         float logoX = screenW / 2.0F;
         if (MathUtil.isHovered(mouseX, mouseY, logoX - 45.0F, emblemY - 26.0F, 90.0F, 75.0F)) {
            int timeAccent = ColorUtil.interpolateColor(ColorUtil.client(), this.getTimeOfDay().primaryColor, 0.45F);
            IconSplashEffect.spawn(logoX, emblemY, 22, timeAccent);
            GuiSounds.click();
            return true;
         }

         if (this.isTimeBadgeHovered(mouseX, mouseY)) {
            this.cycleTimeOfDay();
            return true;
         }

         for (MainMenuScreen.MenuButton btn : this.buttons) {
            if (btn.contains((double)mouseX, (double)mouseY)) {
               if (btn == this.exitButton) {
                  this.exitSlideDragging = true;
                  this.updateExitSlideProgress();
                  return true;
               }

               btn.onClick();
               return true;
            }
         }

         if (mouseX < 36.0F) {
            this.startBgSwitch(-1);
            GuiSounds.click();
            return true;
         }

         if (mouseX > (float)mc.method_22683().method_4486() / this.scaleFix - 36.0F) {
            this.startBgSwitch(1);
            GuiSounds.click();
            return true;
         }
      }

      return super.method_25402(click, doubled);
   }

   public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
      if (this.exitSlideDragging) {
         this.lastMouseX = click.comp_4798() / (double)this.scaleFix;
         this.lastMouseY = click.comp_4799() / (double)this.scaleFix;
         this.updateExitSlideProgress();
         return true;
      } else {
         return super.method_25403(click, deltaX, deltaY);
      }
   }

   public boolean method_25406(class_11909 click) {
      if (this.exitSlideDragging) {
         this.lastMouseX = click.comp_4798() / (double)this.scaleFix;
         this.lastMouseY = click.comp_4799() / (double)this.scaleFix;
         this.updateExitSlideProgress();
         this.exitSlideDragging = false;
         if (this.exitSlideProgress >= 0.9F) {
            this.exitSlideProgress = 1.0F;
            this.exitButton.onClick();
         } else {
            this.exitSlideProgress = 0.0F;
         }

         return true;
      } else {
         return super.method_25406(click);
      }
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25422() {
      if (!this.exit) {
         this.exit = false;
      }

      return false;
   }

   private void closeCheck() {
      if (this.exit && this.alpha.isFinished()) {
         this.method_25419();
         this.alpha.run(0.0, 0.5, Easings.SINE_OUT);
         this.exit = false;
      }
   }

   private void updatePointerMotion(int screenWidth, int screenHeight) {
      long now = System.nanoTime();
      double elapsed = this.lastPointerUpdateNanos == 0L ? 0.0 : Math.max(0.0, Math.min(0.1, (double)(now - this.lastPointerUpdateNanos) * 1.0E-9));
      this.lastPointerUpdateNanos = now;
      float targetX = Math.max(-1.0F, Math.min(1.0F, (float)this.lastMouseX * 2.0F / (float)Math.max(1, screenWidth) - 1.0F));
      float targetY = Math.max(-1.0F, Math.min(1.0F, (float)this.lastMouseY * 2.0F / (float)Math.max(1, screenHeight) - 1.0F));
      float blend = (float)(-Math.expm1(-elapsed / 0.14F));
      this.smoothPointerX = this.smoothPointerX + (targetX - this.smoothPointerX) * blend;
      this.smoothPointerY = this.smoothPointerY + (targetY - this.smoothPointerY) * blend;
   }

   private void drawPointerLight(int screenWidth, int screenHeight, float alphaVal, MainMenuScreen.TimeOfDay tod) {
      if (!Optimization.skipGlow() && !(alphaVal <= 0.01F)) {
         float width = (float)screenWidth * 0.9F;
         float height = (float)screenHeight * 0.7F;
         float x = ((float)screenWidth - width) * 0.5F + this.smoothPointerX * 18.0F;
         float y = (float)screenHeight * 0.32F - height * 0.5F + this.smoothPointerY * 10.0F;
         int accent = ColorUtil.interpolateColor(ColorUtil.client(), tod.primaryColor, 0.4F);
         int clear = ColorUtil.replAlpha(accent, 0.0F);
         int light = ColorUtil.replAlpha(accent, alphaVal * 0.06F);
         Draw.gradientRect(x, y, width, height, new int[]{clear, clear, clear, clear, light, clear, clear, clear, clear}, 0.0F);
      }
   }

   private void drawAtmosphere(int screenW, int screenH, float alphaVal, MainMenuScreen.TimeOfDay tod) {
      if (!(alphaVal <= 0.01F)) {
         int themeColor = ColorUtil.client();
         int clear = ColorUtil.replAlpha(themeColor, 0.0F);
         int bottomTint = ColorUtil.replAlpha(themeColor, alphaVal * 0.45F);
         int topTint = ColorUtil.replAlpha(themeColor, alphaVal * 0.3F);
         float horizonH = (float)screenH * 0.35F;
         Draw.gradientRect(0.0F, (float)screenH - horizonH, (float)screenW, horizonH, new int[]{clear, clear, bottomTint, bottomTint}, 0.0F);
         float skyH = (float)screenH * 0.25F;
         Draw.gradientRect(0.0F, 0.0F, (float)screenW, skyH, new int[]{topTint, topTint, clear, clear}, 0.0F);
      }
   }

   private static String profileDisplayName() {
      String name = System.getProperty("lunarix.displayName");
      if (name == null || name.isBlank()) {
         name = System.getProperty("lunarix.username");
      }

      if ((name == null || name.isBlank()) && mc.method_1548() != null) {
         name = mc.method_1548().method_1676();
      }

      return name == null || name.isBlank() ? "Player" : name.trim();
   }

   private void drawGreetingCard(int screenW, int screenH, float alphaVal, MainMenuScreen.TimeOfDay tod, float y) {
      if (!(alphaVal <= 0.01F)) {
         String playerName = profileDisplayName();
         String greeting = tod.greeting() + ", " + playerName + "!";
         String clockTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
         String subtitle = tod.subtitle() + "  •  " + clockTime;
         float maxTextW = Math.max(Fonts.sf_bold.getWidth(greeting, 8.0F), Fonts.sf_regular.getWidth(subtitle, 6.0F));
         float cardW = Math.max(190.0F, maxTextW + 44.0F);
         float cardH = 26.0F;
         float cardX = (float)screenW / 2.0F - cardW / 2.0F;
         float cardRadius = 8.0F;
         if (!Optimization.skipGlow()) {
            Draw.glow(cardX, y, cardW, cardH, tod.primaryColor, cardRadius, 8.0F, 0.22F * alphaVal);
         }

         RenderUtil.Blur.blur(cardX, y, cardW, cardH, 1.0F, cardRadius, ColorUtil.getColor(10, alphaVal * (tod.lightUi ? 0.55F : 0.38F)));
         Draw.outline(cardX, y, cardW, cardH, 0.8F, ColorUtil.replAlpha(tod.primaryColor, alphaVal * 0.3F), cardRadius);
         float iconBoxSize = 18.0F;
         float iconBoxX = cardX + 4.5F;
         float iconBoxY = y + (cardH - iconBoxSize) / 2.0F;
         Draw.rect(iconBoxX, iconBoxY, iconBoxSize, iconBoxSize, ColorUtil.replAlpha(tod.primaryColor, alphaVal * 0.18F), iconBoxSize * 0.5F);
         Draw.rect(iconBoxX + iconBoxSize / 2.0F - 2.5F, iconBoxY + iconBoxSize / 2.0F - 2.5F, 5.0F, 5.0F, ColorUtil.replAlpha(tod.lightColor, alphaVal), 2.5F);
         float textX = iconBoxX + iconBoxSize + 6.0F;
         Fonts.sf_bold.draw(greeting, textX, y + 4.5F, 8.0F, tod.lightUi ? ColorUtil.getColor(22, 26, 40, alphaVal) : ColorUtil.getColor(255, alphaVal));
         Fonts.sf_regular.draw(
            subtitle,
            textX,
            y + 15.0F,
            6.0F,
            tod.lightUi
               ? ColorUtil.replAlpha(ColorUtil.getColor(96, 106, 138), alphaVal * 0.9F)
               : ColorUtil.replAlpha(ColorUtil.getColor(220, 225, 235), alphaVal * 0.72F)
         );
      }
   }

   private void drawTimePill(int screenW, int screenH, float alphaVal, MainMenuScreen.TimeOfDay tod) {
      if (!(alphaVal <= 0.01F)) {
         String clockTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
         String modeLabel = manualTimeOverride != null ? tod.label() : "AUTO • " + tod.label();
         String text = clockTime + " • " + modeLabel;
         float textW = Fonts.sf_regular.getWidth(text, 6.5F);
         this.timeBadgeW = textW + 16.0F;
         this.timeBadgeH = 17.0F;
         this.timeBadgeX = (float)screenW - this.timeBadgeW - 12.0F;
         this.timeBadgeY = 10.0F;
         boolean hovered = this.isTimeBadgeHovered((float)this.lastMouseX, (float)this.lastMouseY);
         this.timeBadgeHover.update();
         this.timeBadgeHover.run(hovered ? 1.0 : 0.0, 0.18, Easings.QUAD_OUT);
         float hp = this.timeBadgeHover.get();
         int fill = ColorUtil.getColor(12, alphaVal * (0.35F + hp * 0.12F));
         RenderUtil.Blur.blur(this.timeBadgeX, this.timeBadgeY, this.timeBadgeW, this.timeBadgeH, 1.0F, 8.5F, fill);
         Draw.outline(
            this.timeBadgeX,
            this.timeBadgeY,
            this.timeBadgeW,
            this.timeBadgeH,
            0.7F,
            ColorUtil.replAlpha(tod.primaryColor, alphaVal * (0.25F + hp * 0.35F)),
            8.5F
         );
         if (hp > 0.01F && !Optimization.skipGlow()) {
            Draw.glow(this.timeBadgeX, this.timeBadgeY, this.timeBadgeW, this.timeBadgeH, tod.primaryColor, 8.5F, 6.0F, hp * 0.25F * alphaVal);
         }

         Fonts.sf_regular
            .drawCentered(
               text, this.timeBadgeX + this.timeBadgeW / 2.0F, this.timeBadgeY + 4.8F, 6.5F, ColorUtil.replAlpha(tod.lightColor, alphaVal * (0.8F + hp * 0.2F))
            );
      }
   }

   private void drawBuildBadge(int screenWidth, int screenHeight, float alphaVal) {
      if (!(alphaVal <= 0.01F)) {
         float badgeX = 14.0F;
         float badgeY = (float)screenHeight - 24.0F;
         float badgeH = 14.0F;
         String text = "Build 1.0 alpha";
         float textW = Fonts.sf_medium.getWidth(text, 6.5F);
         float dotSize = 4.0F;
         float badgeW = textW + 18.0F;
         RenderUtil.Render2D.glow(badgeX, badgeY, badgeW, badgeH, ColorUtil.getColor(0, alphaVal * 0.12F), 7.0F, 8.0F, 1.0F);
         RenderUtil.Blur.blur(badgeX, badgeY, badgeW, badgeH, alphaVal * 0.8F, 7.0F, ColorUtil.getColor(12, 16, 24, alphaVal * 0.35F));
         RenderUtil.Render2D.rect(badgeX, badgeY, badgeW, badgeH, ColorUtil.getColor(255, 255, 255, alphaVal * 0.08F), 7.0F);
         RenderUtil.Render2D.rect(badgeX + 1.0F, badgeY + 0.5F, badgeW - 2.0F, 0.75F, ColorUtil.getColor(255, 255, 255, alphaVal * 0.25F), 0.5F);
         RenderUtil.Render2D.outline(badgeX, badgeY, badgeW, badgeH, 0.5F, ColorUtil.getColor(255, 255, 255, alphaVal * 0.14F), 7.0F);
         int dotCol = ColorUtil.getColor(80, 240, 140, alphaVal * 0.95F);
         RenderUtil.Render2D.glow(
            badgeX + 5.5F,
            badgeY + badgeH / 2.0F - dotSize / 2.0F,
            dotSize,
            dotSize,
            ColorUtil.getColor(80, 240, 140, alphaVal * 0.35F),
            dotSize / 2.0F,
            4.0F,
            1.0F
         );
         RenderUtil.Render2D.rect(badgeX + 5.5F, badgeY + badgeH / 2.0F - dotSize / 2.0F, dotSize, dotSize, dotCol, dotSize / 2.0F);
         Fonts.sf_medium.draw(text, badgeX + 13.5F, badgeY + 4.0F, 6.5F, ColorUtil.getColor(240, alphaVal * 0.9F));
      }
   }

   private boolean isTimeBadgeHovered(float mx, float my) {
      return MathUtil.isHovered(mx, my, this.timeBadgeX, this.timeBadgeY, this.timeBadgeW, this.timeBadgeH);
   }

   private void cycleTimeOfDay() {
      if (manualTimeOverride == null) {
         manualTimeOverride = MainMenuScreen.TimeOfDay.MORNING;
      } else if (manualTimeOverride == MainMenuScreen.TimeOfDay.MORNING) {
         manualTimeOverride = MainMenuScreen.TimeOfDay.DAY;
      } else if (manualTimeOverride == MainMenuScreen.TimeOfDay.DAY) {
         manualTimeOverride = MainMenuScreen.TimeOfDay.EVENING;
      } else if (manualTimeOverride == MainMenuScreen.TimeOfDay.EVENING) {
         manualTimeOverride = MainMenuScreen.TimeOfDay.NIGHT;
      } else {
         manualTimeOverride = null;
      }

      MainMenuScreen.TimeOfDay current = this.getTimeOfDay();
      startBgSwitchTo(current.bgIndex);
      GuiSounds.button();
   }

   @Environment(EnvType.CLIENT)
   private static class MenuButton {
      private final String textRu;
      private final String textEn;
      private final String descRu;
      private final String descEn;
      private final Runnable action;
      private final boolean danger;
      private final Animation hoverAnim = new Animation();
      private boolean hovered;
      private double mouseX;
      private double mouseY;
      private float highlightMouseX;
      private float lastX;
      private float lastY;
      private float lastW;
      private float lastH;

      public MenuButton(String textRu, String textEn, String descRu, String descEn, Runnable action) {
         this(textRu, textEn, descRu, descEn, false, action);
      }

      public MenuButton(String textRu, String textEn, String descRu, String descEn, boolean danger, Runnable action) {
         this.textRu = textRu;
         this.textEn = textEn;
         this.descRu = descRu;
         this.descEn = descEn;
         this.danger = danger;
         this.action = action;
         this.hoverAnim.set(0.0);
      }

      private String text() {
         return Lang.pick(this.textRu, this.textEn);
      }

      private String description() {
         return Lang.pick(this.descRu, this.descEn);
      }

      public float getWidth() {
         Font font = Fonts.sf_regular;
         return Math.max(font.getWidth(this.text(), 8.0F), font.getWidth(this.description(), 6.0F)) + 34.0F;
      }

      public float getHoverValue() {
         return this.hoverAnim.get();
      }

      public void updatePointer(double mouseX, double mouseY, float highlightMouseX) {
         this.mouseX = mouseX;
         this.mouseY = mouseY;
         this.highlightMouseX = highlightMouseX;
      }

      public boolean contains(double mouseX, double mouseY) {
         return MathUtil.isHovered((float)mouseX, (float)mouseY, this.lastX, this.lastY, this.lastW, this.lastH);
      }

      private void rememberBounds(float x, float y, float width, float height) {
         this.lastX = x;
         this.lastY = y;
         this.lastW = width;
         this.lastH = height;
         boolean wasHovered = this.hovered;
         this.hovered = this.contains(this.mouseX, this.mouseY);
         if (this.hovered && !wasHovered) {
            GuiSounds.hover();
         }

         this.hoverAnim.update();
         double hoverTarget = this.hovered ? 1.0 : 0.0;
         if (this.hoverAnim.getToValue() != hoverTarget) {
            this.hoverAnim.run(hoverTarget, 0.18, Easings.QUAD_OUT);
         }
      }

      private void drawHoverAccent(float x, float y, float width, float height, float radius, float globalAlpha) {
         float hp = Math.max(0.0F, Math.min(1.0F, this.hoverAnim.get()));
         if (!(hp <= 0.01F) && !(globalAlpha <= 0.01F) && !(width <= 16.0F)) {
            int accent = ColorUtil.client();
            int clear = ColorUtil.replAlpha(accent, 0.0F);
            int tint = ColorUtil.replAlpha(accent, globalAlpha * hp * 0.045F);
            Draw.gradientRect(x, y, width, height, new int[]{tint, tint, clear, clear}, radius);
            float highlightWidth = Math.min(32.0F, width - 16.0F);
            float highlightX = Math.max(x + 8.0F, Math.min(x + width - 8.0F - highlightWidth, this.highlightMouseX - highlightWidth * 0.5F));
            int edge = ColorUtil.replAlpha(accent, globalAlpha * hp * 0.3F);
            Draw.gradientRect(highlightX, y + 1.0F, highlightWidth, 1.0F, new int[]{clear, edge, clear, clear, edge, clear, clear, edge, clear}, 0.0F);
         }
      }

      public float getLastX() {
         return this.lastX;
      }

      public float getLastW() {
         return this.lastW;
      }

      public void drawHero(float x, float y, float width, float height, float globalAlpha, int variant, boolean lightBg) {
         this.rememberBounds(x, y, width, height);
         float hp = this.hoverAnim.get();
         float radius = 9.0F;
         int accent = ColorUtil.overCol(ColorUtil.getColor(125, 125, 125), ColorUtil.client(), hp);
         RenderUtil.Blur.blur(
            x, y, width, height, 1.0F, 6.0F, ColorUtil.getColor(12, globalAlpha * ((lightBg ? 0.52F : 0.3F) + hp * (lightBg ? 0.16F : 0.1F)))
         );
         this.drawHoverAccent(x, y, width, height, 6.0F, globalAlpha);
         int textColor = lightBg
            ? ColorUtil.replAlpha(ColorUtil.getColor(24, 28, 42), globalAlpha * (0.35F + hp * 0.65F))
            : ColorUtil.replAlpha(ColorUtil.getColor(255), globalAlpha * (0.3F + hp * 0.7F));
         Fonts.sf_regular.draw(this.text(), x + 12.0F, y + 7.8F, 7.0F, textColor);
         Fonts.icon
            .drawCentered(
               variant == 0 ? "j" : "k",
               x + width - 15.0F,
               y + 9.25F,
               6.0F,
               ColorUtil.replAlpha(accent, globalAlpha * ((lightBg ? 0.55F : 0.3F) + hp * (lightBg ? 0.45F : 0.7F)))
            );
      }

      public void drawCompact(float x, float y, float width, float height, float globalAlpha, String icon, boolean lightBg) {
         this.rememberBounds(x, y, width, height);
         float hp = this.hoverAnim.get();
         float radius = 9.0F;
         int accent = ColorUtil.overCol(ColorUtil.getColor(255), ColorUtil.client(), hp);
         int fill = ColorUtil.getColor(12, globalAlpha * ((lightBg ? 0.52F : 0.3F) + hp * (lightBg ? 0.16F : 0.1F)));
         RenderUtil.Blur.blur(x, y, width, height, 1.0F, radius, fill);
         this.drawHoverAccent(x, y, width, height, radius, globalAlpha);
         int textColor = lightBg
            ? ColorUtil.replAlpha(ColorUtil.getColor(24, 28, 42), globalAlpha * (0.35F + hp * 0.65F))
            : ColorUtil.replAlpha(ColorUtil.getColor(255), globalAlpha * (0.3F + hp * 0.7F));
         Fonts.sf_regular.draw(this.text(), x + 12.0F, y + 7.8F, 7.0F, textColor);
         Fonts.icon
            .drawCentered(
               icon, x + width - 15.0F, y + 9.25F, 6.0F, ColorUtil.replAlpha(accent, globalAlpha * ((lightBg ? 0.5F : 0.2F) + hp * (lightBg ? 0.5F : 0.8F)))
            );
      }

      public void drawExit(float x, float y, float width, float height, float globalAlpha, float slideProgress, boolean dragging, boolean lightBg) {
         this.rememberBounds(x, y, width, height);
         float hp = this.hoverAnim.get();
         float radius = 9.0F;
         int accent = ColorUtil.getColor(255, dragging ? 145 : 255, dragging ? 145 : 255);
         int fill = ColorUtil.getColor(20, globalAlpha * ((lightBg ? 0.55F : 0.35F) + hp * (lightBg ? 0.15F : 0.1F)));
         float progress = Math.max(0.0F, Math.min(1.0F, slideProgress));
         float knobSize = height - 8.0F;
         float knobX = x + 5.0F + (width - knobSize - 10.0F) * progress;
         RenderUtil.Blur.blur(x, y, width, height, 1.0F, radius, fill);
         RenderUtil.Render2D.rect(
            knobX,
            y + 4.0F,
            knobSize,
            knobSize,
            ColorUtil.getColor(lightBg ? 30 : 255, lightBg ? 34 : 255, lightBg ? 50 : 255, globalAlpha * (0.02F + hp * 0.03F + (dragging ? 0.02F : 0.0F))),
            4.0F
         );
         Fonts.icon
            .drawCentered(
               "i",
               knobX + knobSize / 2.0F,
               y + height / 2.0F - 3.0F,
               7.0F,
               ColorUtil.replAlpha(accent, globalAlpha * (0.2F + hp * 0.2F + progress * 0.2F))
            );
         Fonts.sf_regular
            .drawCentered(
               Lang.pick("Посуньте, щоб вийти", "Сдвиньте, чтобы выйти", "Slide to exit"),
               x + width / 2.0F + 8.0F,
               y + height / 2.0F - 4.4F,
               7.0F,
               lightBg
                  ? ColorUtil.replAlpha(ColorUtil.getColor(24, 28, 42), globalAlpha * (0.3F + hp * 0.4F - progress * 0.24F))
                  : ColorUtil.getColor(255, globalAlpha * (0.1F + hp * 0.2F - progress * 0.16F))
            );
      }

      public void draw(float x, float y, float width, float height, float globalAlpha) {
         this.rememberBounds(x, y, width, height);
         float hp = this.hoverAnim.get();
         float radius = 9.0F;
         int accent = ColorUtil.getColor(110, 255, 133);
         int fill = this.danger
            ? ColorUtil.overCol(ColorUtil.getColor(12, globalAlpha * (0.3F - 0.2F * hp)), ColorUtil.getColor(45, 0, 0, globalAlpha * 0.3F), hp)
            : ColorUtil.getColor(12, globalAlpha * (0.3F - 0.2F * hp));
         RenderUtil.Blur.blur(x, y, width, height, 1.0F, radius, fill);
         Font font = Fonts.sf_regular;
         int titleTarget = this.danger ? ColorUtil.getColor(255, 125, 125) : accent;
         int titleColor = ColorUtil.replAlpha(ColorUtil.interpolateColor(ColorUtil.getColor(160, 160, 160, 1.0F), titleTarget, hp), globalAlpha);
         String text = this.text();
         float titleX = x + width / 2.0F - font.getWidth(text, 8.0F) / 2.0F;
         font.draw(text, titleX, y + 12.0F, 8.0F, titleColor);
      }

      public boolean isHovered() {
         return this.hovered;
      }

      public void onClick() {
         GuiSounds.button();
         if (this.action != null) {
            this.action.run();
         }
      }
   }

   private static class MenuParticle {
      float x;
      float y;
      float vx;
      float vy;
      float size;
      float alpha;
      float baseAlpha;
      float phase;
      float phaseSpeed;

      void reset(float screenW, float screenH, Random rnd) {
         this.x = rnd.nextFloat() * screenW;
         this.y = rnd.nextFloat() * screenH;
         this.size = 1.0F + rnd.nextFloat() * 2.2F;
         this.baseAlpha = 0.2F + rnd.nextFloat() * 0.5F;
         this.alpha = this.baseAlpha;
         this.phase = rnd.nextFloat() * 6.28F;
         this.phaseSpeed = 0.02F + rnd.nextFloat() * 0.035F;
      }

      void update(float screenW, float screenH, MainMenuScreen.TimeOfDay time) {
         this.phase = this.phase + this.phaseSpeed;
         switch (time) {
            case MORNING:
               this.vy = -0.25F - this.size * 0.06F;
               this.vx = (float)Math.sin((double)this.phase) * 0.25F;
               break;
            case DAY:
               this.vy = -0.12F - this.size * 0.04F;
               this.vx = (float)Math.cos((double)(this.phase * 0.8F)) * 0.35F;
               break;
            case EVENING:
               this.vy = -0.2F - this.size * 0.05F;
               this.vx = 0.3F + (float)Math.sin((double)this.phase) * 0.2F;
               break;
            case NIGHT:
               this.vy = -0.06F + (float)Math.sin((double)(this.phase * 0.5F)) * 0.12F;
               this.vx = (float)Math.cos((double)(this.phase * 0.7F)) * 0.2F;
         }

         this.x = this.x + this.vx;
         this.y = this.y + this.vy;
         this.alpha = this.baseAlpha * (0.6F + 0.4F * (float)Math.sin((double)this.phase));
         if (this.y < -10.0F) {
            this.y = screenH + 5.0F;
            this.x = (float)(Math.random() * (double)screenW);
         } else if (this.y > screenH + 10.0F) {
            this.y = -5.0F;
         }

         if (this.x < -10.0F) {
            this.x = screenW + 5.0F;
         } else if (this.x > screenW + 10.0F) {
            this.x = -5.0F;
         }
      }
   }

   public static enum TimeOfDay {
      MORNING(
         0,
         "Доброе утро",
         "Доброго ранку",
         "Good morning",
         "Свежее утро — время для побед",
         "Свіжий ранок — час для перемог",
         "Fresh morning — ready for wins",
         -18611,
         -8062,
         -28928,
         956282701,
         419419471,
         true
      ),
      DAY(
         1,
         "Добрый день",
         "Доброго дня",
         "Good afternoon",
         "В самом разгаре дня",
         "У самому розпалі дня",
         "In the prime of the day",
         -14043402,
         -8268550,
         -16611119,
         942257910,
         411161850,
         true
      ),
      EVENING(
         2,
         "Добрый вечер",
         "Доброго вечора",
         "Good evening",
         "Теплый закат и вечерний чилл",
         "Теплий захід сонця та вечірній чил",
         "Warm sunset and evening chill",
         -44462,
         -30080,
         -2818048,
         956256850,
         419400320,
         false
      ),
      NIGHT(
         3,
         "Доброй ночи",
         "Доброї ночі",
         "Good night",
         "Ночной гринд никогда не спит",
         "Нічний ґрінд ніколи не спить",
         "Night grind never sleeps",
         -8630785,
         -5011201,
         -10149889,
         947670527,
         414419199,
         false
      );

      public final int bgIndex;
      private final String ruGreeting;
      private final String uaGreeting;
      private final String enGreeting;
      private final String ruSubtitle;
      private final String uaSubtitle;
      private final String enSubtitle;
      public final int primaryColor;
      public final int lightColor;
      public final int darkColor;
      public final int glowColor;
      public final int ambientTint;
      public final boolean lightUi;

      private TimeOfDay(
         int bgIndex,
         String ruGreeting,
         String uaGreeting,
         String enGreeting,
         String ruSubtitle,
         String uaSubtitle,
         String enSubtitle,
         int primaryColor,
         int lightColor,
         int darkColor,
         int glowColor,
         int ambientTint,
         boolean lightUi
      ) {
         this.bgIndex = bgIndex;
         this.ruGreeting = ruGreeting;
         this.uaGreeting = uaGreeting;
         this.enGreeting = enGreeting;
         this.ruSubtitle = ruSubtitle;
         this.uaSubtitle = uaSubtitle;
         this.enSubtitle = enSubtitle;
         this.primaryColor = primaryColor;
         this.lightColor = lightColor;
         this.darkColor = darkColor;
         this.glowColor = glowColor;
         this.ambientTint = ambientTint;
         this.lightUi = lightUi;
      }

      public String greeting() {
         return Lang.pick(this.uaGreeting, this.ruGreeting, this.enGreeting);
      }

      public String subtitle() {
         return Lang.pick(this.uaSubtitle, this.ruSubtitle, this.enSubtitle);
      }

      public String label() {
         switch (this) {
            case MORNING:
               return Lang.pick("Ранок", "Утро", "Morning");
            case DAY:
               return Lang.pick("День", "День", "Day");
            case EVENING:
               return Lang.pick("Вечір", "Вечер", "Evening");
            default:
               return Lang.pick("Ніч", "Ночь", "Night");
         }
      }

      public static MainMenuScreen.TimeOfDay current() {
         int hour = LocalTime.now().getHour();
         if (hour >= 5 && hour < 12) {
            return MORNING;
         } else if (hour >= 12 && hour < 18) {
            return DAY;
         } else {
            return hour >= 18 && hour < 23 ? EVENING : NIGHT;
         }
      }
   }
}
