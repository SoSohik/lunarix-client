package ru.mentions.utils.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.impl.display.ClientTheme;

@Environment(EnvType.CLIENT)
public class GuiSounds {
   private static final String DIR = "gui/";
   private static long lastTick;
   private static long lastType;
   private static long lastHover;

   private static boolean pass(long last, long delay) {
      return System.currentTimeMillis() - last >= delay;
   }

   private static void play(String name, float volume) {
      if (ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiSounds.getValue()) {
         SoundUtil.playSound_wav("gui/" + name, volume);
      }
   }

   private static void play(String name, float volume, float pitch) {
      if (ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiSounds.getValue()) {
         SoundUtil.playSound_wav("gui/" + name, volume, pitch);
      }
   }

   private static void playDirect(String soundName, float volume) {
      if (ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiSounds.getValue()) {
         SoundUtil.playSound_wav(soundName, volume);
      }
   }

   private static void playDirect(String soundName, float volume, float pitch) {
      if (ClientTheme.INSTANCE == null || ClientTheme.INSTANCE.uiSounds.getValue()) {
         SoundUtil.playSound_wav(soundName, volume, pitch);
      }
   }

   public static void hover() {
      if (pass(lastHover, 35L)) {
         lastHover = System.currentTimeMillis();
         playDirect("hover_tick", 0.35F, 0.98F + (float)(Math.random() * 0.05F));
      }
   }

   public static void click() {
      playDirect("button_click", 0.4F);
   }

   public static void checkbox(boolean value) {
      playDirect(value ? "guienablecheckbox" : "guidisablecheckbox", 0.42F);
   }

   public static void tab() {
      playDirect("moduleopen", 0.38F);
   }

   public static void success() {
      playDirect("bell", 0.45F);
   }

   public static void error() {
      playDirect("CFG_LOAD_ERROR", 0.45F);
   }

   public static void alert() {
      playDirect("trevoga", 0.45F);
   }

   public static void open() {
      play("gui_open", 0.45F);
   }

   public static void close() {
      play("gui_close", 0.45F);
   }

   public static void editor() {
      play("gui_anime", 0.4F);
   }

   public static void category(int index, int total) {
      float t = total <= 1 ? 0.0F : (float)index / (float)(total - 1);
      play("gui_category", 0.45F, 0.97F + 0.08F * t);
   }

   public static void theme(int index, int total) {
      float t = total <= 1 ? 0.0F : (float)index / (float)(total - 1);
      play("gui_theme", 0.45F, 0.95F + 0.14F * t);
   }

   public static void expand(boolean opened) {
      play(opened ? "gui_module_open" : "gui_module_close", 0.45F);
   }

   public static void toggle(boolean value) {
      play(value ? "gui_boolean_enable" : "gui_boolean_disable", 0.4F);
   }

   public static void chip(int index, int total) {
      float t = total <= 1 ? 0.0F : (float)index / (float)(total - 1);
      play("gui_mode_multi", 0.45F, 0.95F + 0.15F * t);
   }

   public static void chipMulti(boolean value) {
      play(value ? "gui_boolean2_enable" : "gui_boolean2_disable", 0.4F);
   }

   public static void button() {
      play("gui_open_button", 0.4F);
   }

   public static void sliderGrab() {
      play("gui_click", 0.35F);
   }

   public static void sliderTick(float percent) {
      if (pass(lastTick, 22L)) {
         lastTick = System.currentTimeMillis();
         play("gui_slider", 0.35F, 0.9F + 0.35F * Math.max(0.0F, Math.min(1.0F, percent)));
      }
   }

   public static void sliderRelease() {
      play("gui_slider", 0.3F, 1.25F);
   }

   public static void bindStart() {
      play("gui_binding", 0.45F);
   }

   public static void bindSet() {
      play("gui_bind", 0.4F);
   }

   public static void bindReset() {
      play("gui_clear", 0.4F);
   }

   public static void editStart() {
      play("gui_click", 0.4F);
   }

   public static void editCommit() {
      play("gui_bind", 0.35F);
   }

   public static void editCancel() {
      play("gui_clear", 0.35F);
   }

   public static void type() {
      if (pass(lastType, 25L)) {
         lastType = System.currentTimeMillis();
         play("gui_key_click", 0.35F, 1.0F + (float)(Math.random() * 0.12F - 0.06F));
      }
   }

   public static void erase() {
      if (pass(lastType, 25L)) {
         lastType = System.currentTimeMillis();
         play("gui_key_click", 0.3F, 0.88F);
      }
   }

   public static void searchClear() {
      play("gui_clear", 0.4F);
   }

   public static void picker(boolean opened) {
      play(opened ? "gui_multi_open" : "gui_multi_close", 0.4F);
   }

   public static void colorTick(float percent) {
      if (pass(lastTick, 28L)) {
         lastTick = System.currentTimeMillis();
         play("gui_slider", 0.3F, 0.9F + 0.35F * Math.max(0.0F, Math.min(1.0F, percent)));
      }
   }

   public static void scroll() {
      if (pass(lastTick, 55L)) {
         lastTick = System.currentTimeMillis();
         play("gui_scroll", 0.35F);
      }
   }
}
