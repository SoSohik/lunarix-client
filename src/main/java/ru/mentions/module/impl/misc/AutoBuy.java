package ru.mentions.module.impl.misc;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_437;
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
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.screen.AutoBuyScreen;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;

@ModuleInfo(
   name = "AutoBuy",
   category = Category.OTHER,
   desc = "Покупает предметы из аукциона и магазина по правилам: название, цена за штуку, количество. Правила правятся в окне «Правила покупки»."
)
@Environment(EnvType.CLIENT)
public class AutoBuy extends Module implements IMinecraft {
   private static final Pattern NUMBER_PATTERN = Pattern.compile("(?<![\\d.])([\\d][\\d\\s., ]*)(?![\\d.])");
   private static final String[] PRICE_MARKERS = new String[]{"price", "цена", "купить", "стоим", "монет", "мон", "coin", "money", "$", "₽", "€", "руб", "р."};
   private static final String[] AUCTION_MARKERS = new String[]{"auction", "аукцион", "аук", "market", "маркет", "bazaar", "базар", "trade", "торг"};
   public final ButtonSetting openRules = new ButtonSetting(this, "Правила покупки", () -> {
      if (mc.field_1724 != null) {
         mc.method_1507(new AutoBuyScreen(mc.field_1755));
      }
   });
   public final ButtonSetting installPresets = new ButtonSetting(this, "Установить каталог", () -> {
      int added = AutoBuyPresets.install(AutoBuyRules.get());
      ChatUtils.addChatMessage(added > 0 ? "[AutoBuy] Добавлено правил из каталога: " + added : "[AutoBuy] Каталог уже установлен.");
   });
   public final ModeSetting scope = new ModeSetting(this, "Контейнер", "Любой контейнер", "Аукцион и магазин");
   public final SliderSetting pricePercent = new SliderSetting(this, "Процент от лимита", 100.0F, 10.0F, 500.0F, 5.0F);
   public final BindSetting toggleBind = new BindSetting(this, "Переключить в контейнере", -1, false);
   public final BooleanSetting quickMove = new BooleanSetting(this, "Быстрое перемещение", false);
   public final BooleanSetting report = new BooleanSetting(this, "Писать в чат", true);
   public final SliderSetting scanDelay = new SliderSetting(this, "Задержка сканирования (мс)", 35.0F, 10.0F, 500.0F, 5.0F);
   public final SliderSetting buyHold = new SliderSetting(this, "Удержание перед кликом (мс)", 120.0F, 0.0F, 1000.0F, 10.0F);
   public final SliderSetting clickCooldown = new SliderSetting(this, "Кулдаун клика (мс)", 180.0F, 50.0F, 2000.0F, 10.0F);
   public final SliderSetting maxScanSlots = new SliderSetting(this, "Макс. слотов", 54.0F, 1.0F, 54.0F, 1.0F);
   private final Set<String> boughtKeys = new HashSet<>();
   private long lastScanAt = 0L;
   private long lastClickAt = 0L;
   private long pendingSince = 0L;
   private int pendingSlot = -1;
   private String pendingKey = "";
   private class_437 lastScreen = null;

   public static AutoBuy get() {
      return Client.get() != null && Client.get().moduleManager() != null ? Client.get().moduleManager().get(AutoBuy.class) : null;
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.lastScreen != mc.field_1755) {
            this.lastScreen = mc.field_1755;
            this.boughtKeys.clear();
            this.resetPending();
         }

         if (mc.field_1755 instanceof class_476 screen) {
            if (!this.isSearchableScreen(screen)) {
               this.resetPending();
            } else {
               List<AutoBuyRule> rules = AutoBuyRules.get().all();
               if (rules.isEmpty()) {
                  this.resetPending();
               } else {
                  long now = System.currentTimeMillis();
                  if (now - this.lastScanAt >= (long)this.scanDelay.getValue().floatValue()) {
                     this.lastScanAt = now;
                     this.scan(screen, rules, now);
                  }
               }
            }
         } else {
            this.resetPending();
         }
      }
   }

   public boolean isSearchableScreen(class_476 screen) {
      return this.scope.is("Любой контейнер") ? true : this.isShopScreen(screen);
   }

   private void scan(class_476 screen, List<AutoBuyRule> rules, long now) {
      class_1707 handler = (class_1707)screen.method_17577();
      if (handler == null) {
         this.resetPending();
      } else if (!mc.field_1724.field_7512.method_34255().method_7960()) {
         this.resetPending();
      } else {
         int containerEnd = Math.min(handler.method_17388() * 9, (int)this.maxScanSlots.getValue().floatValue());
         int percent = Math.round(this.pricePercent.getValue());
         AutoBuy.Candidate best = null;

         for (int slotId = 0; slotId < containerEnd; slotId++) {
            class_1735 slot = handler.method_7611(slotId);
            if (slot != null && slot.method_7681()) {
               int price = getPrice(slot.method_7677());
               if (price > 0) {
                  int count = Math.max(1, slot.method_7677().method_7947());
                  String key = makeKey(slotId, price, count, slot.method_7677());
                  if (!this.boughtKeys.contains(key)) {
                     for (AutoBuyRule rule : rules) {
                        if (rule != null && rule.isValid() && rule.matches(slot.method_7677()) && count >= rule.minQuantity) {
                           int unitPrice = (int)Math.ceil((double)price / (double)count);
                           if (unitPrice <= rule.limit(percent)) {
                              AutoBuy.Candidate candidate = new AutoBuy.Candidate(slotId, price, count, unitPrice, key, rule);
                              if (best == null || candidate.isBetterThan(best)) {
                                 best = candidate;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (best == null) {
            this.resetPending();
         } else if (best.slotId != this.pendingSlot || !best.key.equals(this.pendingKey)) {
            this.pendingSlot = best.slotId;
            this.pendingKey = best.key;
            this.pendingSince = now;
         } else if (now - this.pendingSince >= (long)this.buyHold.getValue().floatValue()) {
            if (now - this.lastClickAt >= (long)this.clickCooldown.getValue().floatValue()) {
               class_1735 slot = handler.method_7611(best.slotId);
               if (slot != null && slot.method_7681()) {
                  int price = getPrice(slot.method_7677());
                  int count = Math.max(1, slot.method_7677().method_7947());
                  if (price == best.price && count == best.count) {
                     this.click(handler, best);
                  } else {
                     this.resetPending();
                  }
               } else {
                  this.resetPending();
               }
            }
         }
      }
   }

   private void click(class_1707 handler, AutoBuy.Candidate candidate) {
      class_1713 action = this.quickMove.getValue() ? class_1713.field_7794 : class_1713.field_7790;
      mc.field_1761.method_2906(handler.field_7763, candidate.slotId, 0, action, mc.field_1724);
      this.boughtKeys.add(candidate.key);
      this.lastClickAt = System.currentTimeMillis();
      this.resetPending();
      if (this.report.getValue()) {
         ChatUtils.addChatMessage(
            "[AutoBuy] Куплено: " + candidate.rule.name + " x" + candidate.count + " за " + candidate.price + " (" + candidate.unitPrice + " за штуку)"
         );
      }
   }

   private boolean isShopScreen(class_476 screen) {
      String title = plain(screen.method_25440().getString()).toLowerCase(Locale.ROOT);

      for (String marker : AUCTION_MARKERS) {
         if (title.contains(marker)) {
            return true;
         }
      }

      return false;
   }

   public static int getPrice(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_9290 lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632);
         if (lore == null) {
            return -1;
         } else {
            for (class_2561 line : lore.comp_2400()) {
               String text = line.getString();
               String lower = plain(text).toLowerCase(Locale.ROOT);
               boolean looksLikePrice = false;

               for (String marker : PRICE_MARKERS) {
                  if (lower.contains(marker) || text.contains(marker)) {
                     looksLikePrice = true;
                     break;
                  }
               }

               if (looksLikePrice) {
                  Matcher matcher = NUMBER_PATTERN.matcher(text.replace(' ', ' '));

                  while (matcher.find()) {
                     String digits = matcher.group(1).replaceAll("[^0-9]", "");
                     if (!digits.isEmpty()) {
                        try {
                           long value = Long.parseLong(digits);
                           if (value > 0L && value <= 2147483647L) {
                              return (int)value;
                           }
                        } catch (NumberFormatException var11) {
                        }
                     }
                  }
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   public static String plain(String value) {
      return AutoBuyRule.normalize(value);
   }

   private static String makeKey(int slot, int price, int count, class_1799 stack) {
      return slot + "|" + price + "|" + count + "|" + stack.method_7964().getString().hashCode();
   }

   private void resetPending() {
      this.pendingKey = "";
      this.pendingSlot = -1;
      this.pendingSince = 0L;
   }

   private static final class Candidate {
      private final int slotId;
      private final int price;
      private final int count;
      private final int unitPrice;
      private final String key;
      private final AutoBuyRule rule;

      private Candidate(int slotId, int price, int count, int unitPrice, String key, AutoBuyRule rule) {
         this.slotId = slotId;
         this.price = price;
         this.count = count;
         this.unitPrice = unitPrice;
         this.key = key;
         this.rule = rule;
      }

      private boolean isBetterThan(AutoBuy.Candidate other) {
         if (this.unitPrice != other.unitPrice) {
            return this.unitPrice < other.unitPrice;
         } else if (this.rule.maxPrice != other.rule.maxPrice) {
            return this.rule.maxPrice < other.rule.maxPrice;
         } else {
            return this.count != other.count ? this.count > other.count : this.slotId < other.slotId;
         }
      }
   }
}
