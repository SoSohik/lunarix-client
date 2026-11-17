package ru.mentions.module.impl.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_476;
import net.minecraft.class_6880;
import net.minecraft.class_9290;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import ru.mentions.lang.Lang;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.RussianItemNames;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.math.StopGPT;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.potion.CustomBuffs;

@ModuleInfo(
   name = "Auction Helper",
   category = Category.PLAYER,
   desc = "Подсвечивает 3 самых дешёвых лота."
)
@Environment(EnvType.CLIENT)
public class AuctionHelper extends Module {
   public final BindSetting searchKey = new BindSetting(this, Lang.isUkrainian() ? "Клавіша пошуку" : "Клавиша поиска");
   public final BindSetting sellKey = new BindSetting(this, Lang.isUkrainian() ? "Клавіша продажу" : "Клавиша продажи");
   public final BooleanSetting autoNextPage = new BooleanSetting(this, Lang.isUkrainian() ? "Гортати сторінки" : "Листать страницы", true);
   public final SliderSetting maxPagesSetting = new SliderSetting(this, Lang.isUkrainian() ? "Макс. сторінок" : "Макс. страниц", 50.0F, 1.0F, 200.0F, 1.0F)
      .setVisible(() -> this.autoNextPage.getValue());
   public final BooleanSetting legitSell = new BooleanSetting(this, Lang.isUkrainian() ? "Безпалевний продаж" : "Беспалевная продажа", true);
   public final SliderSetting cacheTtlSetting = new SliderSetting(
      this, Lang.isUkrainian() ? "Актуальність цін (хв)" : "Актуальность цен (мин)", 3.0F, 1.0F, 10.0F, 0.5F
   );
   public final SliderSetting minLookTime = new SliderSetting(this, Lang.isUkrainian() ? "Мін. перегляд (сек)" : "Мин. просмотр (сек)", 5.0F, 2.0F, 10.0F, 0.5F)
      .setVisible(() -> this.legitSell.getValue());
   public final SliderSetting maxLookTime = new SliderSetting(
         this, Lang.isUkrainian() ? "Макс. перегляд (сек)" : "Макс. просмотр (сек)", 10.0F, 5.0F, 20.0F, 0.5F
      )
      .setVisible(() -> this.legitSell.getValue());
   private static final ConcurrentHashMap<String, AuctionHelper.PriceEntry> priceHistory = new ConcurrentHashMap<>();
   private String savedCacheKey = "";
   private int targetWaitTicks = 0;
   private AuctionHelper.SellState sellState = AuctionHelper.SellState.IDLE;
   private final StopGPT timer = new StopGPT();
   private String savedItemName = "";
   private int savedItemCount = 1;
   private long savedSellPrice = 0L;
   private int waitTicks = 0;
   private List<String> handEnchants = new ArrayList<>();
   private class_9304 handEnchantComp = null;
   private float handDurabilityPercent = -1.0F;
   private static final float DURABILITY_TOLERANCE = 0.15F;
   private class_1792 savedItem = null;
   private CustomBuffs.CustomBuff savedCustomBuff = null;
   private int nextPageSlot = -1;
   private int pagesScanned = 0;

   public static AuctionHelper get() {
      return Instance.get(AuctionHelper.class);
   }

   @EventHandler
   public void onKey(EventKey e) {
      if (mc.field_1724 != null && e.getKey() != -1) {
         if (this.isEnabled()) {
            if (this.sellState == AuctionHelper.SellState.IDLE) {
               if (this.searchKey.get() != -1 && e.getKey() == this.searchKey.get()) {
                  this.performSearch();
               } else if (this.sellKey.get() != -1 && e.getKey() == this.sellKey.get()) {
                  this.startSell();
               }
            }
         }
      }
   }

   private void performSearch() {
      class_1799 hand = mc.field_1724.method_6047();
      if (hand.method_7960()) {
         this.actionbar(Lang.isUkrainian() ? "§cНемає предмета!" : "§cНет предмета!");
      } else {
         CustomBuffs.CustomBuff customBuff = CustomBuffs.findByItemStack(hand);
         String name = customBuff != null ? customBuff.getRawSearchName() : this.cleanItemName(RussianItemNames.getRussianName(hand));
         if (!name.isEmpty()) {
            mc.field_1724.field_3944.method_45730("ah search " + name);
            this.actionbar((Lang.isUkrainian() ? "§aПошук: " : "§aПоиск: ") + name);
         }
      }
   }

   private String getItemCacheKey(class_1799 stack, CustomBuffs.CustomBuff buff) {
      if (stack != null && !stack.method_7960()) {
         StringBuilder sb = new StringBuilder();
         if (buff != null) {
            sb.append("buff:").append(buff.getId());
         } else {
            if (stack.method_7909() != null) {
               sb.append("item:").append(stack.method_7909().method_7876());
            }

            String customName = stack.method_7964().getString().replaceAll("§.", "").trim();
            if (!customName.isEmpty()) {
               sb.append(":name:").append(customName);
            }
         }

         List<String> enchants = getEnchantNames(stack);
         if (!enchants.isEmpty()) {
            List<String> sorted = new ArrayList<>(enchants);
            sorted.sort(String::compareTo);
            sb.append(":ench:").append(String.join(",", sorted));
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private void startSell() {
      class_1799 hand = mc.field_1724.method_6047();
      if (hand.method_7960()) {
         this.actionbar(Lang.isUkrainian() ? "§cНемає предмета!" : "§cНет предмета!");
      } else {
         this.savedCustomBuff = CustomBuffs.findByItemStack(hand);
         this.savedItemName = this.savedCustomBuff != null
            ? this.savedCustomBuff.getRawSearchName()
            : this.cleanItemName(RussianItemNames.getRussianName(hand));
         this.savedItemCount = hand.method_7947();
         this.savedItem = hand.method_7909();
         this.handEnchants = getEnchantNames(hand);
         class_9304 comp = (class_9304)hand.method_58694(class_9334.field_49633);
         this.handEnchantComp = comp != null && !comp.method_57543() ? comp : null;
         if (!this.savedItemName.isEmpty()) {
            float dur;
            this.handDurabilityPercent = this.hasMending(hand) ? -1.0F : ((dur = this.getDurabilityPercent(hand)) < 0.999F ? dur : -1.0F);
            this.savedCacheKey = this.getItemCacheKey(hand, this.savedCustomBuff);
            long ttlMs = (long)(this.cacheTtlSetting.getValue() * 60.0F * 1000.0F);
            AuctionHelper.PriceEntry cached = priceHistory.get(this.savedCacheKey);
            if (cached != null && cached.isValid(ttlMs)) {
               long remSec = cached.getRemainingSeconds(ttlMs);
               long min = remSec / 60L;
               long sec = remSec % 60L;
               this.savedSellPrice = this.roundPrice(cached.pricePerOne * (long)this.savedItemCount);
               String timeStr = min > 0L ? min + " хв " + sec + " с" : sec + " с";
               String timeStrRu = min > 0L ? min + " мин " + sec + " с" : sec + " с";
               this.chat(
                  Lang.isUkrainian()
                     ? "§a[AH] Ціна з історії: §l$" + this.formatNumber(this.savedSellPrice) + " §7(актуальна ще " + timeStr + ")"
                     : "§a[AH] Цена из истории: §l$" + this.formatNumber(this.savedSellPrice) + " §7(актуальна ещё " + timeStrRu + ")"
               );
               this.actionbar(Lang.isUkrainian() ? "§a[AH] Продаж за історією цін..." : "§a[AH] Продажа по истории цен...");
               if (mc.field_1755 != null) {
                  mc.field_1724.method_7346();
                  mc.method_1507(null);
               }

               int delay = this.legitSell.getValue() ? 18 + ThreadLocalRandom.current().nextInt(8) : 4;
               this.targetWaitTicks = delay;
               this.waitTicks = 0;
               this.sellState = AuctionHelper.SellState.WAIT_BEFORE_SELL;
            } else {
               if (cached != null) {
                  this.chat(
                     Lang.isUkrainian()
                        ? "§e[AH] Час актуальності ціни минув (" + (int)this.cacheTtlSetting.getValue().floatValue() + " хв). Оновлюю з аукціону..."
                        : "§e[AH] Время актуальности цены истекло (" + (int)this.cacheTtlSetting.getValue().floatValue() + " мин). Обновляю с аукциона..."
                  );
               }

               mc.field_1724.field_3944.method_45730("ah search " + this.savedItemName);
               if (this.handEnchantComp != null || !this.handEnchants.isEmpty()) {
                  String enchList = this.handEnchants.isEmpty()
                     ? (Lang.isUkrainian() ? "ванільні зачарування" : "ванильные зачары")
                     : String.join(", ", this.handEnchants);
                  this.chat((Lang.isUkrainian() ? "§e[AH] Зачарування: §f" : "§e[AH] Зачарования: §f") + enchList);
               }

               if (this.handDurabilityPercent >= 0.0F) {
                  this.chat((Lang.isUkrainian() ? "§e[AH] Міцність: §f" : "§e[AH] Прочность: §f") + (int)(this.handDurabilityPercent * 100.0F) + "%");
               }

               this.actionbar(Lang.isUkrainian() ? "§e[AH] Аналіз цін та продаж..." : "§e[AH] Анализ цен и продажа...");
               this.sellState = AuctionHelper.SellState.WAITING_AH_OPEN;
               this.timer.reset();
               this.waitTicks = 0;
               this.pagesScanned = 0;
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.field_1724 != null && this.sellState != AuctionHelper.SellState.IDLE) {
         switch (this.sellState) {
            case WAITING_AH_OPEN:
               if (++this.waitTicks > 60) {
                  this.chat(Lang.isUkrainian() ? "§c[AH] Аукціон не відкрився." : "§c[AH] Аукцион не открылся.");
                  this.resetState();
                  return;
               }

               if (mc.field_1755 instanceof class_476 screen) {
                  class_1707 handler = (class_1707)screen.method_17577();
                  int containerSlots = handler.method_17388() * 9;
                  boolean hasItems = false;

                  for (int i = 0; i < containerSlots; i++) {
                     if (handler.method_7611(i).method_7681()) {
                        hasItems = true;
                        break;
                     }
                  }

                  if (!hasItems && this.waitTicks < 25) {
                     return;
                  }

                  if (this.waitTicks < 5) {
                     return;
                  }

                  this.sellState = AuctionHelper.SellState.READING_PRICES;
                  this.waitTicks = 0;
               }
               break;
            case READING_PRICES:
               if (!(mc.field_1755 instanceof class_476 screen)) {
                  this.resetState();
                  return;
               }

               class_1707 handler = (class_1707)screen.method_17577();
               int containerSlots = handler.method_17388() * 9;
               long cheapestPerOne = Long.MAX_VALUE;
               int found = 0;
               float closestDurDiff = Float.MAX_VALUE;
               long closestCheapest = Long.MAX_VALUE;
               int i = 0;

               for (; i < containerSlots; i++) {
                  class_1735 slot = handler.method_7611(i);
                  int price = getPrice(slot);
                  if (price > 0 && (this.savedItem == null || slot.method_7677().method_7909() == this.savedItem)) {
                     if (this.savedCustomBuff != null) {
                        CustomBuffs.CustomBuff slotBuff = CustomBuffs.findByItemStack(slot.method_7677());
                        if (slotBuff != this.savedCustomBuff) {
                           continue;
                        }
                     }

                     if (this.handEnchantComp != null) {
                        class_9304 slotComp = (class_9304)slot.method_7677().method_58694(class_9334.field_49633);
                        if (!this.enchantCompMatch(this.handEnchantComp, slotComp)) {
                           continue;
                        }
                     } else if (!this.handEnchants.isEmpty()) {
                        try {
                           if (!this.enchantsMatch(this.handEnchants, getEnchantNames(slot.method_7677()))) {
                              continue;
                           }
                        } catch (Exception var21) {
                           continue;
                        }
                     }

                     long perOne = (long)price / (long)Math.max(slot.method_7677().method_7947(), 1);
                     if (this.handDurabilityPercent >= 0.0F) {
                        float slotDur = this.getDurabilityPercent(slot.method_7677());
                        float diff = Math.abs(slotDur - this.handDurabilityPercent);
                        if (diff < closestDurDiff || diff == closestDurDiff && perOne < closestCheapest) {
                           closestDurDiff = diff;
                           closestCheapest = perOne;
                        }

                        if (diff > 0.15F) {
                           continue;
                        }
                     }

                     found++;
                     if (perOne < cheapestPerOne) {
                        cheapestPerOne = perOne;
                     }
                  }
               }

               if (found == 0 && this.handDurabilityPercent >= 0.0F && closestCheapest != Long.MAX_VALUE) {
                  this.chat(
                     Lang.isUkrainian()
                        ? "§e[AH] Точної міцності немає, найближча (" + (int)(closestDurDiff * 100.0F) + "% різниця)"
                        : "§e[AH] Точной прочности нет, ближайшая (" + (int)(closestDurDiff * 100.0F) + "% разница)"
                  );
                  cheapestPerOne = closestCheapest;
                  found = 1;
               }

               if (found == 0 || cheapestPerOne == Long.MAX_VALUE) {
                  i = this.autoNextPage.getValue() ? this.findNextPageSlot(handler, containerSlots) : -1;
                  if (i >= 0 && this.pagesScanned < this.maxPagesSetting.getValue().intValue()) {
                     this.nextPageSlot = i;
                     this.pagesScanned++;
                     this.sellState = AuctionHelper.SellState.NEXT_PAGE;
                     this.waitTicks = 0;
                     return;
                  }

                  this.chat(
                     Lang.isUkrainian()
                        ? "§c[AH] Немає відповідних лотів (переглянуто сторінок: " + this.pagesScanned + ")."
                        : "§c[AH] Нет подходящих лотов (просмотрено страниц: " + this.pagesScanned + ")."
                  );
                  mc.field_1724.method_7346();
                  mc.method_1507(null);
                  this.resetState();
                  return;
               }

               long totalPrice;
               this.savedSellPrice = totalPrice = this.roundPrice(cheapestPerOne * (long)this.savedItemCount);
               if (!this.savedCacheKey.isEmpty()) {
                  priceHistory.put(this.savedCacheKey, new AuctionHelper.PriceEntry(cheapestPerOne, System.currentTimeMillis(), this.savedItemName));
               }

               this.chat(
                  Lang.isUkrainian()
                     ? "§a[AH] Знайшов " + found + " лотів. Ціна за шт: $" + this.formatNumber(cheapestPerOne) + ". Сума: §l$" + this.formatNumber(totalPrice)
                     : "§a[AH] Нашёл " + found + " лотов. Цена за шт: $" + this.formatNumber(cheapestPerOne) + ". Сумма: §l$" + this.formatNumber(totalPrice)
               );
               if (this.legitSell.getValue()) {
                  float minS = this.minLookTime.getValue();
                  float maxS = Math.max(minS, this.maxLookTime.getValue());
                  float lookSeconds = minS + ThreadLocalRandom.current().nextFloat() * (maxS - minS);
                  this.targetWaitTicks = (int)(lookSeconds * 20.0F);
                  this.waitTicks = 0;
                  this.actionbar(
                     Lang.isUkrainian()
                        ? "§e[AH] Переглядаю аукціон (~" + String.format(Locale.ROOT, "%.1f", lookSeconds) + " с)..."
                        : "§e[AH] Просматриваю аукцион (~" + String.format(Locale.ROOT, "%.1f", lookSeconds) + " с)..."
                  );
                  this.sellState = AuctionHelper.SellState.LOOKING_AT_AUCTION;
               } else {
                  mc.field_1724.method_7346();
                  mc.method_1507(null);
                  this.sellState = AuctionHelper.SellState.SEND;
                  this.waitTicks = 0;
               }
               break;
            case NEXT_PAGE:
               if (!(mc.field_1755 instanceof class_476 screen)) {
                  this.resetState();
                  return;
               }

               if (this.waitTicks == 0) {
                  class_1707 screenHandler = (class_1707)screen.method_17577();
                  mc.field_1761.method_2906(screenHandler.field_7763, this.nextPageSlot, 0, class_1713.field_7790, mc.field_1724);
               }

               if (++this.waitTicks >= 6) {
                  this.sellState = AuctionHelper.SellState.READING_PRICES;
                  this.waitTicks = 0;
               }
               break;
            case LOOKING_AT_AUCTION:
               if (!(mc.field_1755 instanceof class_476)) {
                  this.resetState();
                  return;
               }

               if (++this.waitTicks >= this.targetWaitTicks) {
                  mc.field_1724.method_7346();
                  mc.method_1507(null);
                  this.targetWaitTicks = 18 + ThreadLocalRandom.current().nextInt(8);
                  this.waitTicks = 0;
                  this.sellState = AuctionHelper.SellState.WAIT_BEFORE_SELL;
               }
               break;
            case WAIT_BEFORE_SELL:
               if (++this.waitTicks >= this.targetWaitTicks) {
                  if (this.savedSellPrice > 0L) {
                     mc.field_1724.field_3944.method_45730("ah sell " + this.savedSellPrice);
                     this.actionbar(
                        Lang.isUkrainian()
                           ? "§a[AH] Виставлено за $" + this.formatNumber(this.savedSellPrice)
                           : "§a[AH] Выставлено за $" + this.formatNumber(this.savedSellPrice)
                     );
                  }

                  this.resetState();
               }
               break;
            case SEND:
               if (++this.waitTicks < 5) {
                  return;
               }

               if (this.savedSellPrice > 0L) {
                  mc.field_1724.field_3944.method_45730("ah sell " + this.savedSellPrice);
                  this.actionbar(
                     Lang.isUkrainian()
                        ? "§a[AH] Виставлено за $" + this.formatNumber(this.savedSellPrice)
                        : "§a[AH] Выставлено за $" + this.formatNumber(this.savedSellPrice)
                  );
               }

               this.resetState();
         }
      }
   }

   public void renderSlot(class_332 context, class_1735 slot, int rank) {
      int fill = switch (rank) {
         case 1 -> 1616248661;
         case 2 -> 1627389781;
         default -> 1627367936;
      };

      int outline = switch (rank) {
         case 1 -> -16720640;
         case 2 -> -2237184;
         default -> -30720;
      };
      int x = slot.field_7873;
      int y = slot.field_7872;
      context.method_25294(x, y, x + 16, y + 16, fill);
      context.method_25294(x, y, x + 16, y + 1, outline);
      context.method_25294(x, y + 15, x + 16, y + 16, outline);
      context.method_25294(x, y, x + 1, y + 16, outline);
      context.method_25294(x + 15, y, x + 16, y + 16, outline);
   }

   public static int getPrice(class_1735 slot) {
      if (!slot.method_7681()) {
         return 0;
      } else {
         class_9290 lore = (class_9290)slot.method_7677().method_57353().method_58694(class_9334.field_49632);
         if (lore == null) {
            return 0;
         } else {
            for (class_2561 line : lore.comp_2400()) {
               String text = line.getString();
               String lower = text.toLowerCase();
               if (text.contains("$") || lower.contains("цена") || lower.contains("стоимость") || text.contains("₽")) {
                  Matcher m = Pattern.compile("(?i)(?:цена|стоимость)?[:\\s]*([0-9][0-9\\s,\\.]*)\\s*(?:\\$|₽|руб|монет|\\(|$)").matcher(text);
                  if (m.find()) {
                     String num = m.group(1).replaceAll("[^0-9]", "");
                     if (!num.isEmpty()) {
                        try {
                           return Integer.parseInt(num);
                        } catch (NumberFormatException var10) {
                        }
                     }
                  }

                  String digits = text.replaceAll("[^0-9]", "");
                  if (!digits.isEmpty()) {
                     try {
                        return Integer.parseInt(digits);
                     } catch (NumberFormatException var9) {
                     }
                  }
               }
            }

            return 0;
         }
      }
   }

   private long roundPrice(long price) {
      return price <= 1L ? 1L : Math.max(1L, price - 1L);
   }

   public static List<String> getEnchantNames(class_1799 stack) {
      ArrayList<String> result = new ArrayList<>();
      if (stack != null && !stack.method_7960()) {
         class_9290 lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632);
         if (lore == null) {
            return result;
         } else {
            for (class_2561 line : lore.comp_2400()) {
               String raw = line.getString().trim();
               if (!raw.isEmpty()
                  && !raw.contains("$")
                  && !raw.toLowerCase().contains("цена")
                  && !raw.contains("Продавец")
                  && !raw.contains("Истекает")
                  && !raw.contains("Нажмите")
                  && !raw.contains("Починка")
                  && (raw.matches(".*[IVX1-9]+$") || isKnownEnchant(raw))) {
                  result.add(raw);
               }
            }

            return result;
         }
      } else {
         return result;
      }
   }

   private static boolean isKnownEnchant(String s) {
      return s.contains("Острота")
         || s.contains("Эффективность")
         || s.contains("Прочность")
         || s.contains("Удача")
         || s.contains("Добыча")
         || s.contains("Небесная кара")
         || s.contains("Заговор огня")
         || s.contains("Бич")
         || s.contains("Разящий")
         || s.contains("Вампиризм")
         || s.contains("Окисление")
         || s.contains("Яд")
         || s.contains("Детекция")
         || s.contains("Бульдозер")
         || s.contains("Магнит")
         || s.contains("Авто-Плавка")
         || s.contains("Паутина")
         || s.contains("Пингер")
         || s.contains("Опытный")
         || s.contains("Шёлковое")
         || s.contains("Шелковое")
         || s.contains("Подводная")
         || s.contains("Защита")
         || s.contains("Отделка")
         || s.contains("Улучшение")
         || s.contains("Шипы")
         || s.contains("Легкость")
         || s.contains("Книга");
   }

   private boolean enchantCompMatch(class_9304 hand, class_9304 slot) {
      if (slot != null && !slot.method_57543()) {
         for (class_6880 entry : hand.method_57534()) {
            if (hand.method_57536(entry) != slot.method_57536(entry)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean enchantsMatch(List<String> hand, List<String> slot) {
      if (hand.isEmpty()) {
         return true;
      } else if (slot.isEmpty()) {
         return false;
      } else {
         for (String he : hand) {
            String base = this.extractBase(he);
            int lvl = this.extractLevel(he);
            boolean found = false;

            for (String se : slot) {
               if ((this.extractBase(se).contains(base) || base.contains(this.extractBase(se))) && this.extractLevel(se) == lvl) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         return true;
      }
   }

   private String extractBase(String s) {
      return s.replaceAll("\\s+[IVX0-9]+$", "").trim();
   }

   private int extractLevel(String s) {
      String trimmed = s.trim();
      int sp = trimmed.lastIndexOf(32);
      if (sp == -1) {
         return 1;
      } else {
         String suf = trimmed.substring(sp + 1);
         int r = this.parseRoman(suf);
         if (r > 0) {
            return r;
         } else {
            try {
               return Integer.parseInt(suf);
            } catch (NumberFormatException var7) {
               return 1;
            }
         }
      }
   }

   private int parseRoman(String r) {
      return switch (r) {
         case "I" -> 1;
         case "II" -> 2;
         case "III" -> 3;
         case "IV" -> 4;
         case "V" -> 5;
         case "VI" -> 6;
         case "VII" -> 7;
         case "VIII" -> 8;
         case "IX" -> 9;
         case "X" -> 10;
         case "XI" -> 11;
         case "XII" -> 12;
         case "XIII" -> 13;
         case "XIV" -> 14;
         case "XV" -> 15;
         default -> 0;
      };
   }

   private String cleanItemName(String name) {
      if (name != null && !name.isEmpty()) {
         name = name.replaceAll("§.", "");
         StringBuilder sb = new StringBuilder();

         for (char c : name.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '\'' || c == '_') {
               sb.append(c);
            }
         }

         return sb.toString()
            .trim()
            .replace("Пузырёк опыта 15 Ур", "Пузырек с уровнем 15")
            .replace("Пузырёк опыта 30 Ур", "Пузырек с уровнем 30")
            .replace("Пузырёк опыта 45 Ур", "Пузырек с уровнем 45")
            .replace("Пузырёк опыта 50 Ур", "Пузырек с уровнем 50")
            .replace("xxx", "")
            .replace("TNT - TIER WHITE", "Таер вайт")
            .replace("TNT - TIER BLACK", "Таер блэк")
            .replace("Динамит", "Динамит обычный")
            .replace("Динаміт", "Динамит обычный")
            .replace("Яйцо призыва крестьянина-зомби", "яйцо зомби-крестьянина")
            .replace("Снежок заморозка", "Снежок заморозка");
      } else {
         return "";
      }
   }

   private float getDurabilityPercent(class_1799 stack) {
      Integer damage = (Integer)stack.method_58694(class_9334.field_49629);
      Integer maxDamage = (Integer)stack.method_58694(class_9334.field_50072);
      return damage != null && maxDamage != null && maxDamage != 0 ? (float)(maxDamage - damage) / (float)maxDamage.intValue() : 1.0F;
   }

   private boolean hasMending(class_1799 stack) {
      class_9304 comp = (class_9304)stack.method_58694(class_9334.field_49633);
      if (comp != null && !comp.method_57543()) {
         for (class_6880 entry : comp.method_57534()) {
            if (entry.method_55840().contains("mending")) {
               return true;
            }
         }
      }

      class_9290 lore;
      if ((lore = (class_9290)stack.method_57353().method_58694(class_9334.field_49632)) != null) {
         for (class_2561 line : lore.comp_2400()) {
            if (line.getString().contains("Починка")) {
               return true;
            }
         }
      }

      return false;
   }

   private int findNextPageSlot(class_1707 handler, int containerSlots) {
      int lastRowStart;
      for (int i = lastRowStart = containerSlots - 9; i < containerSlots; i++) {
         class_1735 slot = handler.method_7611(i);
         if (slot.method_7681()) {
            String name = slot.method_7677().method_7964().getString().replaceAll("§.", "");
            if (name.contains("Следующая страница") || name.contains("Наступна сторінка") || name.contains("Next page")) {
               return i;
            }
         }
      }

      return -1;
   }

   private String formatNumber(long n) {
      return String.format("%,d", n).replace(',', '.');
   }

   private void actionbar(String msg) {
      ChatUtils.addChatMessage(msg);
   }

   private void chat(String msg) {
      ChatUtils.addChatMessage(msg);
   }

   private void resetState() {
      this.sellState = AuctionHelper.SellState.IDLE;
      this.waitTicks = 0;
      this.targetWaitTicks = 0;
      this.savedCacheKey = "";
      this.savedItemName = "";
      this.savedItemCount = 1;
      this.savedSellPrice = 0L;
      this.handEnchants.clear();
      this.handEnchantComp = null;
      this.handDurabilityPercent = -1.0F;
      this.savedItem = null;
      this.savedCustomBuff = null;
      this.nextPageSlot = -1;
      this.pagesScanned = 0;
   }

   @Override
   public void onDisable() {
      this.resetState();
      super.onDisable();
   }

   public static class PriceEntry {
      public final long pricePerOne;
      public final long timestamp;
      public final String itemName;

      public PriceEntry(long pricePerOne, long timestamp, String itemName) {
         this.pricePerOne = pricePerOne;
         this.timestamp = timestamp;
         this.itemName = itemName;
      }

      public boolean isValid(long ttlMs) {
         return System.currentTimeMillis() - this.timestamp < ttlMs;
      }

      public long getRemainingSeconds(long ttlMs) {
         long elapsed = System.currentTimeMillis() - this.timestamp;
         return Math.max(0L, (ttlMs - elapsed) / 1000L);
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum SellState {
      IDLE,
      WAITING_AH_OPEN,
      READING_PRICES,
      NEXT_PAGE,
      LOOKING_AT_AUCTION,
      WAIT_BEFORE_SELL,
      SEND;
   }
}
