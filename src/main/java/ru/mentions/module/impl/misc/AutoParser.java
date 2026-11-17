package ru.mentions.module.impl.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_476;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ButtonSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;

@ModuleInfo(
   name = "AutoParser",
   category = Category.OTHER,
   desc = "Собирает минимальную цену каждого предмета из открытого контейнера: листает страницы аукциона и обходит поиск. Цену можно сразу вписать в правило AutoBuy."
)
@Environment(EnvType.CLIENT)
public class AutoParser extends Module implements IMinecraft {
   private static final String[] NEXT_MARKERS = new String[]{"следующ", "впер", "далее", "next", "forward", "▶", ">>"};
   private static final int MAX_PAGE_KEYS = 128;
   public final BooleanSetting nextPage = new BooleanSetting(this, "Листать страницы", true);
   public final BooleanSetting searchQueue = new BooleanSetting(this, "Очередь поиска", false);
   public final BooleanSetting autoUpdate = new BooleanSetting(this, "Автосохранение цены", false);
   public final BooleanSetting stopAfterPass = new BooleanSetting(this, "Стоп после прохода", false);
   public final BooleanSetting report = new BooleanSetting(this, "Писать в чат", true);
   public final SliderSetting scanDelayMs = new SliderSetting(this, "Задержка сканирования (мс)", 120.0F, 20.0F, 2000.0F, 10.0F);
   public final SliderSetting pageDelayMs = new SliderSetting(this, "Задержка страницы (мс)", 450.0F, 100.0F, 5000.0F, 50.0F);
   public final SliderSetting searchDelayMs = new SliderSetting(this, "Задержка поиска (мс)", 900.0F, 200.0F, 10000.0F, 50.0F);
   public final SliderSetting maxSlots = new SliderSetting(this, "Макс. слотов", 54.0F, 1.0F, 54.0F, 1.0F);
   public final StringSetting searchCommand = new StringSetting(this, "Команда поиска", "/ah search {query}");
   public final BindSetting toggleBind = new BindSetting(this, "Переключить парсер", -1, false);
   public final ButtonSetting restart = new ButtonSetting(this, "Начать заново", () -> {
      AutoParser parser = get();
      if (parser != null) {
         parser.reset();
         parser.setEnabled(true);
         if (parser.report.getValue()) {
            ChatUtils.addChatMessage("[AutoParser] Начал заново.");
         }
      }
   });
   private final Map<AutoBuyRule, Integer> lowest = new HashMap<>();
   private final Map<AutoBuyRule, Integer> seen = new HashMap<>();
   private final Set<String> pageKeys = new HashSet<>();
   private int queueIndex = 0;
   private long nextActionAt = 0L;
   private long lastScanAt = 0L;
   private long lastPageClickAt = 0L;
   private boolean waitingForResult = false;

   public static AutoParser get() {
      return Client.get() != null && Client.get().moduleManager() != null ? Client.get().moduleManager().get(AutoParser.class) : null;
   }

   public static int lowestPrice(AutoBuyRule rule) {
      AutoParser parser = get();
      return parser == null ? -1 : parser.lowestOf(rule);
   }

   public static int seenCount(AutoBuyRule rule) {
      AutoParser parser = get();
      return parser == null ? 0 : parser.seenOf(rule);
   }

   public int lowestOf(AutoBuyRule rule) {
      if (rule == null) {
         return -1;
      } else {
         Integer value = this.lowest.get(rule);
         return value == null ? -1 : value;
      }
   }

   public int seenOf(AutoBuyRule rule) {
      if (rule == null) {
         return 0;
      } else {
         Integer value = this.seen.get(rule);
         return value == null ? 0 : value;
      }
   }

   public int applyLowest(AutoBuyRule rule) {
      int price = this.lowestOf(rule);
      if (price <= 0) {
         return -1;
      } else {
         AutoBuyRules.get().setPrice(rule, price);
         rule.enabled = true;
         AutoBuyRules.get().save();
         if (this.report.getValue()) {
            ChatUtils.addChatMessage("[AutoParser] " + rule.name + " → лимит " + price);
         }

         return price;
      }
   }

   public static int applyLowestTo(AutoBuyRule rule) {
      AutoParser parser = get();
      return parser == null ? -1 : parser.applyLowest(rule);
   }

   public void reset() {
      this.queueIndex = 0;
      this.waitingForResult = false;
      this.pageKeys.clear();
      this.lowest.clear();
      this.seen.clear();
      this.nextActionAt = System.currentTimeMillis() + 250L;
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         long now = System.currentTimeMillis();
         if (now >= this.nextActionAt) {
            if (this.searchQueue.getValue()) {
               this.handleSearchQueue(now);
            }

            if (mc.field_1755 instanceof class_476 screen) {
               if (now - this.lastScanAt >= (long)this.scanDelayMs.getValue().floatValue()) {
                  this.lastScanAt = now;
                  this.scanPage(screen);
               }

               if (!this.searchQueue.getValue() && this.nextPage.getValue()) {
                  this.clickNextPage(screen, now);
               }
            }
         }
      }
   }

   private void scanPage(class_476 screen) {
      class_1707 handler = (class_1707)screen.method_17577();
      if (handler != null) {
         int end = Math.min(containerSlots(handler), (int)this.maxSlots.getValue().floatValue());
         List<AutoParser.FoundLot> found = new ArrayList<>();

         for (int slotId = 0; slotId < end; slotId++) {
            class_1735 slot = handler.method_7611(slotId);
            if (slot != null && slot.method_7681()) {
               class_1799 stack = slot.method_7677();
               int price = AutoBuy.getPrice(stack);
               if (price > 0) {
                  AutoBuyRule rule = this.findRule(stack);
                  if (rule != null) {
                     found.add(new AutoParser.FoundLot(rule, price, Math.max(1, stack.method_7947())));
                  }
               }
            }
         }

         found.sort(Comparator.comparingInt(AutoParser.FoundLot::unitPrice));
         boolean dirty = false;

         for (AutoParser.FoundLot lot : found) {
            this.record(lot);
            if (this.autoUpdate.getValue()) {
               int price = this.lowestOf(lot.rule);
               if (price > 0 && price < lot.rule.maxPrice) {
                  lot.rule.maxPrice = price;
                  lot.rule.enabled = true;
                  dirty = true;
               }
            }
         }

         if (dirty) {
            AutoBuyRules.get().save();
         }
      }
   }

   private void record(AutoParser.FoundLot lot) {
      Integer known = this.lowest.get(lot.rule);
      int unit = lot.unitPrice();
      this.lowest.put(lot.rule, known == null ? unit : Math.min(known, unit));
      Integer count = this.seen.get(lot.rule);
      this.seen.put(lot.rule, count == null ? 1 : count + 1);
   }

   private AutoBuyRule findRule(class_1799 stack) {
      for (AutoBuyRule rule : AutoBuyRules.get().all()) {
         if (rule != null && rule.hasName() && rule.matchesItem(stack)) {
            return rule;
         }
      }

      return null;
   }

   private void clickNextPage(class_476 screen, long now) {
      if (now - this.lastPageClickAt >= (long)this.pageDelayMs.getValue().floatValue()) {
         class_1707 handler = (class_1707)screen.method_17577();
         if (handler != null) {
            int end = containerSlots(handler);

            for (int slotId = 0; slotId < end; slotId++) {
               class_1735 slot = handler.method_7611(slotId);
               if (slot != null && slot.method_7681()) {
                  class_1799 stack = slot.method_7677();
                  if (isNextButton(stack)) {
                     String text = AutoBuy.plain(searchText(stack));
                     if (this.pageKeys.add(screen.method_25440().getString() + "|" + slotId + "|" + text)) {
                        if (this.pageKeys.size() > 128) {
                           this.pageKeys.clear();
                        }

                        mc.field_1761.method_2906(handler.field_7763, slotId, 0, class_1713.field_7790, mc.field_1724);
                        this.lastPageClickAt = now;
                        this.nextActionAt = now + (long)this.pageDelayMs.getValue().floatValue();
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private void handleSearchQueue(long now) {
      List<AutoBuyRule> rules = new ArrayList<>();

      for (AutoBuyRule rule : AutoBuyRules.get().all()) {
         if (rule != null && !AutoBuyRule.normalize(rule.name).isEmpty()) {
            rules.add(rule);
         }
      }

      if (!rules.isEmpty()) {
         long delay = Math.max(250L, (long)this.searchDelayMs.getValue().floatValue());
         if (this.waitingForResult) {
            this.waitingForResult = false;
            this.nextActionAt = now + delay;
         } else {
            if (this.queueIndex >= rules.size()) {
               this.queueIndex = 0;
               if (this.stopAfterPass.getValue()) {
                  if (this.report.getValue()) {
                     ChatUtils.addChatMessage("[AutoParser] Проход завершён.");
                  }

                  this.setEnabled(false);
                  return;
               }
            }

            AutoBuyRule rulex = rules.get(this.queueIndex++);
            String template = this.searchCommand.getValue();
            if (template != null && !template.isBlank()) {
               String command = template.replace("{query}", rulex.name.trim()).trim();
               if (command.startsWith("/")) {
                  command = command.substring(1);
               }

               if (!command.isEmpty() && mc.field_1724.field_3944 != null) {
                  mc.field_1724.field_3944.method_45730(command);
                  this.waitingForResult = true;
                  this.nextActionAt = now + delay;
               }
            }
         }
      }
   }

   private static int containerSlots(class_1707 handler) {
      return Math.max(0, handler.method_17388() * 9);
   }

   private static boolean isNextButton(class_1799 stack) {
      String text = searchText(stack).toLowerCase(Locale.ROOT);

      for (String marker : NEXT_MARKERS) {
         if (text.contains(marker)) {
            return true;
         }
      }

      return false;
   }

   private static String searchText(class_1799 stack) {
      StringBuilder sb = new StringBuilder(stack.method_7964().getString());
      class_9290 lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632);
      if (lore != null) {
         for (class_2561 line : lore.comp_2400()) {
            sb.append(' ').append(line.getString());
         }
      }

      return sb.toString();
   }

   private static final class FoundLot {
      private final AutoBuyRule rule;
      private final int price;
      private final int count;

      private FoundLot(AutoBuyRule rule, int price, int count) {
         this.rule = rule;
         this.price = price;
         this.count = count;
      }

      private int unitPrice() {
         return (int)Math.ceil((double)this.price / (double)this.count);
      }
   }
}
