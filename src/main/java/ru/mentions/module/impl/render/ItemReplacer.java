package ru.mentions.module.impl.render;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11566;
import net.minecraft.class_1799;
import net.minecraft.class_3489;
import net.minecraft.class_9280;
import net.minecraft.class_9334;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;

@ModuleInfo(
   name = "Item Replacer",
   desc = "Заменяет модель мечей на кастомную",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ItemReplacer extends Module {
   public static ItemReplacer INSTANCE;
   private static final String[] WEAPONS = new String[]{
      "Abominable Blade",
      "Abominable Great Saber",
      "Abominable Scythe",
      "Acidic Cleaver",
      "Amethyst Shuriken",
      "Ancient Royal Great Sword",
      "Aquatic Sacred Blade",
      "Arcanethyst",
      "Ashura's Blade",
      "Awakened Lichblade",
      "Blood Edge",
      "Bloody Death",
      "Bramblethorn",
      "Brimstone Claymore",
      "Carian Knight's Sword",
      "Chrono Blade",
      "Corrupted Mythic Blade",
      "Creation Splitter",
      "Crescent Rose",
      "Cyber Katana",
      "Cyber Mantis Blade",
      "Cyber Sword",
      "Cybernetic Chainsaw Blade",
      "Cybernetic Katana",
      "Cybernetic Knife",
      "Dainsleif",
      "Dark Blade",
      "Dark Cleaver",
      "Death Knight's Dagger",
      "Death Knight's Sword",
      "Demigod's Unholy Blade",
      "Demigod's Unholy Halberd",
      "Demon Lord's Great Axe",
      "Demon Lord's Sword",
      "Demonic Blade",
      "Demonic Cleaver",
      "Divine Axe Rhitta",
      "Divine Justice",
      "Divine Punisher",
      "Divine Reaper",
      "Dragon Slaying blade",
      "Edge Of The Astral Plane",
      "Emberblade",
      "Enigma",
      "Epic Sword",
      "Estoc",
      "Fallen God's Spear",
      "Fallen God's Sword",
      "Floral Longsword",
      "Floral Sabre",
      "Forest Guardian's Glaive",
      "Frost Axe",
      "Frost Blade",
      "Frost Scythe",
      "Hearthflame",
      "Hero Sword",
      "Holy Moonlight Sword",
      "Hornet's Needle",
      "Icewhisper",
      "Jade Halberd",
      "Katana",
      "Legendary Sword",
      "Longsword",
      "Magi Scythe",
      "Masamune",
      "Mjolnir",
      "Molten Blade",
      "Molten Sword",
      "Muramasa",
      "Mystical Spellblade",
      "Mythic Blade",
      "Ocean's Rage",
      "Partisan",
      "Pharaoh's Treasure",
      "Pheonix Grace",
      "Plague Longsword",
      "Power Fuse Hammer",
      "Power Fuse Sword",
      "Requiem of the Ninth Abyss",
      "Ribbon Cleaver",
      "Righteous Relic",
      "Rivers Of Blood",
      "Royal Chakram",
      "Royal Rapier",
      "Sabre",
      "Scissor Blade",
      "Sculk Cleaver",
      "Sculk Scythe",
      "Sculk Sword",
      "Sentinel's Will",
      "Silverine Blade",
      "Soul Claws",
      "Soul Collector",
      "Soul Devourer",
      "Soul Edge",
      "Soul Harvester",
      "Soul Stealer",
      "Soulrender",
      "Star's Edge",
      "Steel Sword",
      "Stop Sign",
      "Storm Bringer",
      "Storm's Edge",
      "Sunbreak",
      "Tengen's Blade",
      "Terra Blade",
      "Thousand Demon Daggers",
      "Thunder Bringer",
      "Thunderbrand",
      "True Excalibur",
      "Vampiric Needle",
      "Wakizashi",
      "Watcher Claymore",
      "Watching Warglaive",
      "Waxweaver",
      "Whisperwind",
      "Wickpiercer",
      "Wraith Scythe",
      "Yoru"
   };
   private static final Map<class_1799, ItemReplacer.CachedModel> CACHE = new IdentityHashMap<>();
   private static final int CACHE_LIMIT = 512;
   public final BooleanSetting selfOnly = new BooleanSetting(this, "Только у меня", true);
   public final ModeSetting model = new ModeSetting(this, "Модель", WEAPONS);

   public ItemReplacer() {
      INSTANCE = this;
   }

   @Override
   protected void onDisable() {
      CACHE.clear();
      super.onDisable();
   }

   public class_1799 replaceModel(class_1799 stack, class_11566 owner) {
      if (!this.isEnabled() || stack == null || stack.method_7960()) {
         return stack;
      } else if (!stack.method_31573(class_3489.field_42611)) {
         return stack;
      } else if (this.selfOnly.getValue() && owner != null && owner != mc.field_1724) {
         return stack;
      } else {
         String selected = this.model.getValue();
         class_9280 value = new class_9280(List.of(), List.of(), List.of(selected), List.of());
         if (value.equals(stack.method_57381(class_9334.field_49637))) {
            return stack;
         } else {
            int signature = stack.method_57353().hashCode();
            ItemReplacer.CachedModel cached = CACHE.get(stack);
            if (cached != null && cached.signature == signature && cached.model.equals(selected)) {
               return cached.stack;
            } else {
               class_1799 copy = stack.method_7972();
               copy.method_57379(class_9334.field_49637, value);
               if (CACHE.size() > 512) {
                  CACHE.clear();
               }

               CACHE.put(stack, new ItemReplacer.CachedModel(copy, selected, signature));
               return copy;
            }
         }
      }
   }

   private static record CachedModel(class_1799 stack, String model, int signature) {
   }
}
