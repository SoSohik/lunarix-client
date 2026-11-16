package ru.mentions.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10034;
import net.minecraft.class_437;
import net.minecraft.class_572;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.screen.EmotionAddScreen;
import ru.mentions.screen.EmotionWheelScreen;
import ru.mentions.utils.emote.EmoteLibrary;
import ru.mentions.utils.emote.EmoteMetadata;
import ru.mentions.utils.emote.EmotePlayback;
import ru.mentions.utils.emotion.EmotionRegistry;

@ModuleInfo(
   name = "Emotions",
   category = Category.RENDER,
   allowDisable = false,
   desc = "Эмоции"
)
@Environment(EnvType.CLIENT)
public class Emotions extends Module {
   private static Emotions instance;
   public BindSetting openWheel = new BindSetting(this, "Открыть колесо", 345, true);
   public SliderSetting fadeSpeed = new SliderSetting(this, "Плавность", 1.0F, 0.3F, 2.5F, 0.05F);
   public SliderSetting scale = new SliderSetting(this, "Размах", 1.0F, 0.3F, 2.0F, 0.05F);
   public SliderSetting duration = new SliderSetting(this, "Длительность", 6.0F, 2.0F, 14.0F, 0.5F);
   public ModeSetting page1slot1 = this.slot("Слот 1-1", "floss");
   public ModeSetting page1slot2 = this.slot("Слот 1-2", "dance2");
   public ModeSetting page1slot3 = this.slot("Слот 1-3", "masturbate");
   public ModeSetting page1slot4 = this.slot("Слот 1-4", "crying");
   public ModeSetting page1slot5 = this.slot("Слот 1-5", "successful_pose");
   public ModeSetting page1slot6 = this.slot("Слот 1-6", "salute");
   public ModeSetting page2slot1 = this.slot("Слот 2-1", "dance1");
   public ModeSetting page2slot2 = this.slot("Слот 2-2", "gait3");
   public ModeSetting page2slot3 = this.slot("Слот 2-3", "dance");
   public ModeSetting page2slot4 = this.slot("Слот 2-4", "outrage");
   public ModeSetting page2slot5 = this.slot("Слот 2-5", "sit_thoughtfully");
   public ModeSetting page2slot6 = this.slot("Слот 2-6", "hype");
   public ModeSetting page3slot1 = this.slot("Слот 3-1", "dance2");
   public ModeSetting page3slot2 = this.slot("Слот 3-2", "look_through_a_magnifying_glass");
   public ModeSetting page3slot3 = this.slot("Слот 3-3", "sneak_around");
   public ModeSetting page3slot4 = this.slot("Слот 3-4", "malicious_refusal");
   public ModeSetting page3slot5 = this.slot("Слот 3-5", "crouch");
   public ModeSetting page3slot6 = this.slot("Слот 3-6", "twerk");
   private final ModeSetting[][] pages = new ModeSetting[][]{
      {this.page1slot1, this.page1slot2, this.page1slot3, this.page1slot4, this.page1slot5, this.page1slot6},
      {this.page2slot1, this.page2slot2, this.page2slot3, this.page2slot4, this.page2slot5, this.page2slot6},
      {this.page3slot1, this.page3slot2, this.page3slot3, this.page3slot4, this.page3slot5, this.page3slot6}
   };
   private String activeEmotion = null;
   private long emotionStart = 0L;

   public Emotions() {
      instance = this;
      this.setEnabled(true, false);
   }

   public static Emotions getInstance() {
      return instance;
   }

   private ModeSetting slot(String label, String defaultValue) {
      ModeSetting setting = new ModeSetting(this, label, EmotionRegistry.names());
      setting.set(defaultValue);
      setting.setVisible(() -> false);
      return setting;
   }

   public int pageCount() {
      return this.pages.length;
   }

   public String slotValue(int page, int slot) {
      if (page >= 0 && page < this.pages.length && slot >= 0 && slot < 6) {
         String value = this.pages[page][slot].getValue();
         return value != null && !value.equals("—") ? value : null;
      } else {
         return null;
      }
   }

   public void setSlot(int page, int slot, String emotion) {
      if (page >= 0 && page < this.pages.length && slot >= 0 && slot < 6) {
         this.pages[page][slot].set(emotion == null ? "—" : emotion);
      }
   }

   public boolean hasEmotion(String emotion) {
      ModeSetting[][] modeSettingArray = this.pages;
      int n = modeSettingArray.length;

      for (int i = 0; i < n; i++) {
         ModeSetting[] page;
         for (ModeSetting s : page = modeSettingArray[i]) {
            if (emotion != null && emotion.equals(s.getValue())) {
               return true;
            }
         }
      }

      return false;
   }

   public void removeEmotion(String emotion) {
      if (emotion != null) {
         ModeSetting[][] modeSettingArray = this.pages;
         int n = modeSettingArray.length;

         for (int i = 0; i < n; i++) {
            ModeSetting[] page;
            for (ModeSetting s : page = modeSettingArray[i]) {
               if (emotion.equals(s.getValue())) {
                  s.set("—");
               }
            }
         }
      }
   }

   public String activeEmotion() {
      return this.activeEmotion;
   }

   public float emotionSeconds() {
      return this.activeEmotion == null ? -1.0F : (float)(System.currentTimeMillis() - this.emotionStart) / 1000.0F;
   }

   public void play(String emotion) {
      EmoteMetadata meta = EmoteLibrary.resolve(emotion);
      if (meta != null) {
         this.activeEmotion = meta.id();
         EmotePlayback.play(meta.id());
         this.emotionStart = System.currentTimeMillis();
      }
   }

   public void stop() {
      this.activeEmotion = null;
      EmotePlayback.stopEmote();
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (this.activeEmotion != null) {
         float sec = (float)(System.currentTimeMillis() - this.emotionStart) / 1000.0F;
         float maxSec = this.duration != null ? this.duration.getValue() : 6.0F;
         if (sec >= maxSec || (sec > 0.5F && !EmotePlayback.isPlaying())) {
            this.stop();
         }
      }
   }

   @Override
   public void toggle() {
      this.setEnabled(true, false);
   }

   public void openWheelScreen() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         mc.method_1507(new EmotionWheelScreen());
      }
   }

   public void openAddScreen(class_437 parent) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         mc.method_1507(new EmotionAddScreen(parent));
      }
   }

   @EventHandler
   public void onKeyPress(EventKey event) {
      int bind = this.openWheel.getValue();
      if (bind > 0 && event.getKey() == bind) {
         if (mc.field_1755 == null) {
            this.openWheelScreen();
         }
      }
   }

   private static float smooth(float x) {
      return x * x * (3.0F - 2.0F * x);
   }

   public void applyModelAnimation(class_572<?> model, class_10034 state) {
   }

   private void resetPose(class_572<?> model) {
      model.field_3398.field_3654 = 0.0F;
      model.field_3398.field_3675 = 0.0F;
      model.field_3398.field_3674 = 0.0F;
      model.field_3391.field_3654 = 0.0F;
      model.field_3391.field_3675 = 0.0F;
      model.field_3391.field_3674 = 0.0F;
      model.field_3401.field_3654 = 0.0F;
      model.field_3401.field_3675 = 0.0F;
      model.field_3401.field_3674 = 0.0F;
      model.field_27433.field_3654 = 0.0F;
      model.field_27433.field_3675 = 0.0F;
      model.field_27433.field_3674 = 0.0F;
      model.field_3392.field_3654 = 0.0F;
      model.field_3392.field_3675 = 0.0F;
      model.field_3397.field_3654 = 0.0F;
      model.field_3397.field_3675 = 0.0F;
   }
}
