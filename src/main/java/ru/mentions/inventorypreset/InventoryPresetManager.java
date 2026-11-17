package ru.mentions.inventorypreset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_490;
import net.minecraft.class_7923;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public final class InventoryPresetManager {
   private static final Path DIRECTORY = Path.of("C:/lunarix/client1_21_11/inventory-presets");
   private static final Path CURRENT_FILE = DIRECTORY.resolve("current.txt");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final class_1304[] ARMOR = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
   private static final Pattern DECORATIVE_BRACKETS = Pattern.compile("^\\s*[\\[({<【〖].*?[\\])}>】〗]\\s*");
   private static final Pattern EDGE_DECORATION = Pattern.compile("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$");
   private final class_310 mc = class_310.method_1551();
   private final Map<String, InventoryPreset> presets = new LinkedHashMap<>();
   private final ArrayDeque<InventoryPresetManager.ClickStep> clickSteps = new ArrayDeque<>();
   private final Random random = new Random();
   private String activeName;
   private boolean arranging;
   private int arrangeSyncId = -1;
   private int recoverySlot = -1;
   private int missingScroll;
   private int noMovePasses;
   private long nextClickAt;

   public void init() {
      this.loadAll();
      Client.eventHandler().subscribe(this);
   }

   public List<InventoryPreset> presets() {
      return this.presets.values().stream().sorted(Comparator.comparingLong(InventoryPreset::createdAt).reversed()).toList();
   }

   public Optional<InventoryPreset> activePreset() {
      return Optional.ofNullable(this.activeName == null ? null : this.presets.get(key(this.activeName)));
   }

   public String activeName() {
      return this.activeName;
   }

   public boolean isArranging() {
      return this.arranging;
   }

   public int missingScroll(int total, int visible) {
      this.missingScroll = Math.max(0, Math.min(this.missingScroll, Math.max(0, total - visible)));
      return this.missingScroll;
   }

   public void scrollMissing(int direction, int total, int visible) {
      this.missingScroll = Math.max(0, Math.min(Math.max(0, total - visible), this.missingScroll + direction));
   }

   public InventoryPreset saveCurrent(String requestedName) {
      if (this.mc.field_1724 == null) {
         return null;
      } else {
         String name = normalizeName(requestedName);
         if (name.isBlank()) {
            return null;
         } else {
            ArrayList<InventoryPreset.Entry> slots = new ArrayList<>(41);

            for (int i = 0; i < 36; i++) {
               slots.add(InventoryPreset.Entry.fromStack(this.mc.field_1724.method_31548().method_5438(i)));
            }

            for (class_1304 equipmentSlot : ARMOR) {
               slots.add(InventoryPreset.Entry.fromStack(this.mc.field_1724.method_6118(equipmentSlot)));
            }

            slots.add(InventoryPreset.Entry.fromStack(this.mc.field_1724.method_6079()));
            InventoryPreset preset = new InventoryPreset(name, System.currentTimeMillis(), slots);
            this.presets.put(key(name), preset);
            this.save(preset);
            return preset;
         }
      }
   }

   public void activate(InventoryPreset preset) {
      if (preset != null) {
         this.cancelArrange(false);
         this.activeName = preset.name();
         this.missingScroll = 0;
         this.saveCurrentName();
      }
   }

   public void deactivate() {
      this.cancelArrange(false);
      this.activeName = null;
      this.missingScroll = 0;
      this.saveCurrentName();
   }

   public void delete(InventoryPreset preset) {
      if (preset != null) {
         this.presets.remove(key(preset.name()));
         if (this.activeName != null && this.activeName.equalsIgnoreCase(preset.name())) {
            this.deactivate();
         }

         try {
            Files.deleteIfExists(this.fileFor(preset.name()));
         } catch (IOException var3) {
            var3.printStackTrace();
         }
      }
   }

   public class_1799 currentStack(int logicalSlot) {
      if (this.mc.field_1724 == null) {
         return class_1799.field_8037;
      } else if (logicalSlot >= 0 && logicalSlot < 36) {
         return this.mc.field_1724.method_31548().method_5438(logicalSlot);
      } else if (logicalSlot >= 36 && logicalSlot < 40) {
         return this.mc.field_1724.method_6118(ARMOR[logicalSlot - 36]);
      } else {
         return logicalSlot == 40 ? this.mc.field_1724.method_6079() : class_1799.field_8037;
      }
   }

   public InventoryPresetManager.SlotState stateFor(int logicalSlot) {
      InventoryPreset preset = this.activePreset().orElse(null);
      if (preset == null) {
         return InventoryPresetManager.SlotState.NONE;
      } else {
         InventoryPreset.Entry expected = preset.slot(logicalSlot);
         if (expected.isEmpty()) {
            return InventoryPresetManager.SlotState.CORRECT;
         } else if (expected.matches(this.currentStack(logicalSlot))) {
            return InventoryPresetManager.SlotState.CORRECT;
         } else {
            return this.missingCounts(preset).containsKey(expected.matchKey())
               ? InventoryPresetManager.SlotState.MISSING
               : InventoryPresetManager.SlotState.WRONG_SLOT;
         }
      }
   }

   public Map<String, InventoryPresetManager.MissingItem> missingItems() {
      InventoryPreset preset = this.activePreset().orElse(null);
      if (preset == null) {
         return Map.of();
      } else {
         Map<String, Integer> counts = this.missingCounts(preset);
         LinkedHashMap<String, InventoryPresetManager.MissingItem> result = new LinkedHashMap<>();

         for (Entry<String, Integer> entry : counts.entrySet()) {
            InventoryPreset.Entry source = preset.slots()
               .stream()
               .filter(slot -> slot.matchKey().equals(entry.getKey()))
               .findFirst()
               .orElse(InventoryPreset.Entry.EMPTY);
            result.put(entry.getKey(), new InventoryPresetManager.MissingItem(source, entry.getValue()));
         }

         return result;
      }
   }

   public boolean isComplete() {
      InventoryPreset preset = this.activePreset().orElse(null);
      if (preset != null && this.mc.field_1724 != null) {
         for (int i = 0; i < 41; i++) {
            InventoryPreset.Entry expected = preset.slot(i);
            if (!expected.isEmpty() && !expected.matches(this.currentStack(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean startArrange() {
      if (this.activePreset().isEmpty() || this.mc.field_1724 == null || !(this.mc.field_1755 instanceof class_490)) {
         return false;
      } else if (!this.mc.field_1724.field_7512.method_34255().method_7960()) {
         ChatUtils.addChatMessage("Сначала положите предмет с курсора в слот");
         return false;
      } else {
         this.clickSteps.clear();
         this.arranging = true;
         this.arrangeSyncId = this.mc.field_1724.field_7512.field_7763;
         this.recoverySlot = -1;
         this.noMovePasses = 0;
         this.nextClickAt = 0L;
         return true;
      }
   }

   public void cancelArrange(boolean notify) {
      if (this.arranging && notify) {
         ChatUtils.addChatMessage("Раскладка пресета остановлена");
      }

      this.recoverCursor();
      this.arranging = false;
      this.clickSteps.clear();
      this.arrangeSyncId = -1;
      this.recoverySlot = -1;
      this.noMovePasses = 0;
      this.nextClickAt = 0L;
   }

   public void searchAuction(InventoryPresetManager.MissingItem missing) {
      if (this.mc.field_1724 != null && missing != null) {
         String query = cleanSearchName(missing.entry().displayName());
         if (query.isBlank()) {
            class_1799 fallback = missing.entry().toStack();
            query = fallback.method_7960() ? missing.entry().itemId() : fallback.method_7964().getString();
         }

         if (!(query = cleanSearchName(query)).isBlank()) {
            this.mc.field_1724.field_3944.method_45730("ah search " + query);
         }
      }
   }

   public static String cleanSearchName(String source) {
      if (source == null) {
         return "";
      } else {
         String value = class_124.method_539(source);
         if (value == null) {
            return "";
         } else {
            value = DECORATIVE_BRACKETS.matcher(value).replaceFirst("");
            String var4;
            String[] words = (var4 = value.replaceAll("\\s+", " ").trim()).split(" ");
            if (words.length >= 3 && words[0].equalsIgnoreCase(words[words.length - 1])) {
               var4 = String.join(" ", Arrays.copyOfRange(words, 1, words.length - 1));
            }

            value = EDGE_DECORATION.matcher(var4.trim()).replaceAll("").trim();
            return value.replaceAll("\\s+", " ");
         }
      }
   }

   public static int logicalToScreenSlot(int logical) {
      if (logical >= 0 && logical < 9) {
         return 36 + logical;
      } else if (logical >= 9 && logical < 36) {
         return logical;
      } else if (logical >= 36 && logical < 40) {
         return 5 + (logical - 36);
      } else {
         return logical == 40 ? 45 : -1;
      }
   }

   public static int screenToLogicalSlot(int screenSlot) {
      if (screenSlot >= 36 && screenSlot <= 44) {
         return screenSlot - 36;
      } else if (screenSlot >= 9 && screenSlot <= 35) {
         return screenSlot;
      } else if (screenSlot >= 5 && screenSlot <= 8) {
         return 36 + (screenSlot - 5);
      } else {
         return screenSlot == 45 ? 40 : -1;
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (this.arranging) {
         if (this.mc.field_1724 != null
            && this.mc.field_1761 != null
            && this.mc.field_1755 instanceof class_490
            && this.mc.field_1724.field_7512.field_7763 == this.arrangeSyncId) {
            if (System.currentTimeMillis() >= this.nextClickAt) {
               if (this.clickSteps.isEmpty() && !this.mc.field_1724.field_7512.method_34255().method_7960()) {
                  if (!this.queueCursorRecovery()) {
                     this.nextClickAt = System.currentTimeMillis() + 150L;
                  }
               } else if (!this.clickSteps.isEmpty() || this.planNextMove()) {
                  InventoryPresetManager.ClickStep step = this.clickSteps.peekFirst();
                  if (step != null) {
                     if (step.slot() >= 0 && step.slot() < this.mc.field_1724.field_7512.field_7761.size()) {
                        if (!step.matches(
                           this.mc.field_1724.field_7512.method_34255(),
                           this.mc.field_1724.field_7512.method_7611(step.slot()).method_7677(),
                           step.actionType() == class_1713.field_7791
                              ? this.mc.field_1724.field_7512.method_7611(logicalToScreenSlot(step.button())).method_7677()
                              : class_1799.field_8037
                        )) {
                           this.clickSteps.clear();
                           if (!this.mc.field_1724.field_7512.method_34255().method_7960()) {
                              this.queueCursorRecovery();
                           }

                           this.nextClickAt = System.currentTimeMillis() + 150L;
                        } else {
                           this.clickSteps.pollFirst();
                           this.mc
                              .field_1761
                              .method_2906(this.mc.field_1724.field_7512.field_7763, step.slot(), step.button(), step.actionType(), this.mc.field_1724);
                           this.noMovePasses = 0;
                           long baseDelay = step.actionType() != class_1713.field_7791 && step.button() != 1 ? 65L : 35L;
                           this.nextClickAt = System.currentTimeMillis() + baseDelay + (long)this.random.nextInt(31);
                           if (this.clickSteps.isEmpty() && this.mc.field_1724.field_7512.method_34255().method_7960()) {
                              this.recoverySlot = -1;
                           }
                        }
                     } else {
                        this.clickSteps.clear();
                        this.nextClickAt = System.currentTimeMillis() + 150L;
                     }
                  }
               } else if (this.isComplete()) {
                  this.arranging = false;
                  ChatUtils.addChatMessage("Пресет полностью разложен");
                  this.deactivate();
               } else {
                  if (++this.noMovePasses >= 20) {
                     this.arranging = false;
                     ChatUtils.addChatMessage("Нет безопасного перемещения: проверьте количество предметов и свободный слот");
                  } else {
                     this.nextClickAt = System.currentTimeMillis() + 100L;
                  }
               }
            }
         } else {
            this.cancelArrange(true);
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.cancelArrange(false);
   }

   private boolean planNextMove() {
      InventoryPreset preset = this.activePreset().orElse(null);
      if (preset == null) {
         return false;
      } else {
         for (int target = 36; target < 40; target++) {
            if (this.planTargetIfNeeded(preset, target)) {
               return true;
            }
         }

         if (this.planTargetIfNeeded(preset, 40)) {
            return true;
         } else {
            for (int var3 = 0; var3 < 9; var3++) {
               if (this.planTargetIfNeeded(preset, var3)) {
                  return true;
               }
            }

            for (int var4 = 9; var4 < 36; var4++) {
               if (this.planTargetIfNeeded(preset, var4)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private boolean planTargetIfNeeded(InventoryPreset preset, int target) {
      InventoryPreset.Entry wanted = preset.slot(target);
      class_1799 targetStack = this.currentStack(target);
      return !wanted.isEmpty() && !wanted.matches(targetStack) && this.planTargetMove(preset, wanted, target, targetStack);
   }

   private boolean planTargetMove(InventoryPreset preset, InventoryPreset.Entry wanted, int target, class_1799 targetStack) {
      int targetScreen = logicalToScreenSlot(target);
      if (targetScreen < 0) {
         return false;
      } else if (wanted.sameItem(targetStack)) {
         int source = this.findSource(preset, wanted, target, false);
         if (source < 0) {
            return false;
         } else {
            class_1799 sourceStack = this.currentStack(source);
            int sourceScreen = logicalToScreenSlot(source);
            if (sourceScreen < 0) {
               return false;
            } else if (!class_1799.method_31577(sourceStack, targetStack)) {
               return this.queueWholeMove(source, target, sourceStack, targetStack);
            } else {
               int needed = wanted.count() - targetStack.method_7947();
               if (sourceStack.method_7947() > needed) {
                  return this.queuePartialMove(sourceScreen, targetScreen, sourceStack, targetStack, needed);
               } else {
                  this.recoverySlot = sourceScreen;
                  InventoryPresetManager.StackSnapshot sourceSnapshot = InventoryPresetManager.StackSnapshot.of(sourceStack);
                  InventoryPresetManager.StackSnapshot targetSnapshot = InventoryPresetManager.StackSnapshot.of(targetStack);
                  this.addPickup(sourceScreen, InventoryPresetManager.StackSnapshot.EMPTY, sourceSnapshot);
                  this.addPickup(targetScreen, sourceSnapshot, targetSnapshot);
                  int remainder = Math.max(0, sourceStack.method_7947() + targetStack.method_7947() - targetStack.method_7914());
                  if (remainder > 0) {
                     this.addPickup(sourceScreen, sourceSnapshot.withCount(remainder), InventoryPresetManager.StackSnapshot.EMPTY);
                  }

                  return true;
               }
            }
         }
      } else {
         int source = this.findSource(preset, wanted, target, true);
         if (source < 0) {
            source = this.findSource(preset, wanted, target, false);
         }

         if (source < 0) {
            return false;
         } else {
            int sourceScreen = logicalToScreenSlot(source);
            if (sourceScreen < 0) {
               return false;
            } else {
               class_1799 sourceStack = this.currentStack(source);
               return targetStack.method_7960() && sourceStack.method_7947() > wanted.count()
                  ? this.queuePartialMove(sourceScreen, targetScreen, sourceStack, targetStack, wanted.count())
                  : this.queueWholeMove(source, target, sourceStack, targetStack);
            }
         }
      }
   }

   private boolean queueWholeMove(int source, int target, class_1799 sourceStack, class_1799 targetStack) {
      int sourceScreen = logicalToScreenSlot(source);
      int targetScreen = logicalToScreenSlot(target);
      if (sourceScreen >= 0 && targetScreen >= 0) {
         this.recoverySlot = -1;
         InventoryPresetManager.StackSnapshot sourceSnapshot = InventoryPresetManager.StackSnapshot.of(sourceStack);
         InventoryPresetManager.StackSnapshot targetSnapshot = InventoryPresetManager.StackSnapshot.of(targetStack);
         if (target >= 0 && target < 9) {
            this.addSwap(sourceScreen, target, sourceSnapshot, targetSnapshot);
            return true;
         } else if (source >= 0 && source < 9) {
            this.addSwap(targetScreen, source, targetSnapshot, sourceSnapshot);
            return true;
         } else {
            int buffer = this.chooseHotbarBuffer();
            if (buffer < 0) {
               return false;
            } else {
               int bufferScreen = logicalToScreenSlot(buffer);
               InventoryPresetManager.StackSnapshot bufferSnapshot = InventoryPresetManager.StackSnapshot.of(this.currentStack(buffer));
               this.addSwap(sourceScreen, buffer, sourceSnapshot, bufferSnapshot);
               this.addSwap(targetScreen, buffer, targetSnapshot, sourceSnapshot);
               this.addSwap(sourceScreen, buffer, bufferSnapshot, targetSnapshot);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private int chooseHotbarBuffer() {
      for (int logical = 0; logical < 9; logical++) {
         if (this.currentStack(logical).method_7960()) {
            return logical;
         }
      }

      return 0;
   }

   private boolean queuePartialMove(int sourceScreen, int targetScreen, class_1799 sourceStack, class_1799 targetStack, int amount) {
      if (amount > 0 && !sourceStack.method_7960() && sourceStack.method_7947() > amount) {
         if (!targetStack.method_7960() && !class_1799.method_31577(sourceStack, targetStack)) {
            return false;
         } else if (targetStack.method_7947() + amount > sourceStack.method_7914()) {
            return false;
         } else {
            this.recoverySlot = sourceScreen;
            InventoryPresetManager.StackSnapshot source = InventoryPresetManager.StackSnapshot.of(sourceStack);
            this.addPickup(sourceScreen, InventoryPresetManager.StackSnapshot.EMPTY, source);

            for (int moved = 0; moved < amount; moved++) {
               InventoryPresetManager.StackSnapshot cursorBefore = source.withCount(sourceStack.method_7947() - moved);
               int targetCount = targetStack.method_7947() + moved;
               InventoryPresetManager.StackSnapshot targetBefore = targetCount == 0
                  ? InventoryPresetManager.StackSnapshot.EMPTY
                  : source.withCount(targetCount);
               this.addPickup(targetScreen, 1, cursorBefore, targetBefore);
            }

            int remainder = sourceStack.method_7947() - amount;
            if (remainder > 0) {
               this.addPickup(sourceScreen, source.withCount(remainder), InventoryPresetManager.StackSnapshot.EMPTY);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private int findSource(InventoryPreset preset, InventoryPreset.Entry wanted, int target, boolean requireEnoughCount) {
      for (int logical = 0; logical < 41; logical++) {
         InventoryPreset.Entry expectedHere;
         class_1799 stack;
         if (logical != target
            && wanted.sameItem(stack = this.currentStack(logical))
            && (!requireEnoughCount || stack.method_7947() >= wanted.count())
            && ((expectedHere = preset.slot(logical)).isEmpty() || !expectedHere.matches(stack))) {
            return logical;
         }
      }

      return -1;
   }

   private void addPickup(int slot, InventoryPresetManager.StackSnapshot expectedCursor, InventoryPresetManager.StackSnapshot expectedSlot) {
      this.addPickup(slot, 0, expectedCursor, expectedSlot);
   }

   private void addPickup(int slot, int button, InventoryPresetManager.StackSnapshot expectedCursor, InventoryPresetManager.StackSnapshot expectedSlot) {
      this.clickSteps
         .add(
            new InventoryPresetManager.ClickStep(slot, button, class_1713.field_7790, expectedCursor, expectedSlot, InventoryPresetManager.StackSnapshot.EMPTY)
         );
   }

   private void addSwap(int slot, int hotbarSlot, InventoryPresetManager.StackSnapshot expectedSlot, InventoryPresetManager.StackSnapshot expectedHotbar) {
      this.clickSteps
         .add(
            new InventoryPresetManager.ClickStep(
               slot, hotbarSlot, class_1713.field_7791, InventoryPresetManager.StackSnapshot.EMPTY, expectedSlot, expectedHotbar
            )
         );
   }

   private void recoverCursor() {
      if (this.mc.field_1724 != null
         && this.mc.field_1761 != null
         && this.mc.field_1755 instanceof class_490
         && !this.mc.field_1724.field_7512.method_34255().method_7960()) {
         int destination = this.findCursorDestination(this.mc.field_1724.field_7512.method_34255());
         if (destination >= 0) {
            this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, destination, 0, class_1713.field_7790, this.mc.field_1724);
         }
      }
   }

   private boolean queueCursorRecovery() {
      if (this.mc.field_1724 != null && !this.mc.field_1724.field_7512.method_34255().method_7960()) {
         class_1799 cursor = this.mc.field_1724.field_7512.method_34255();
         int destination = this.findCursorDestination(cursor);
         if (destination < 0) {
            return false;
         } else {
            this.clickSteps
               .add(
                  new InventoryPresetManager.ClickStep(
                     destination,
                     0,
                     class_1713.field_7790,
                     InventoryPresetManager.StackSnapshot.of(cursor),
                     InventoryPresetManager.StackSnapshot.of(this.mc.field_1724.field_7512.method_7611(destination).method_7677()),
                     InventoryPresetManager.StackSnapshot.EMPTY
                  )
               );
            this.recoverySlot = -1;
            return true;
         }
      } else {
         return true;
      }
   }

   private int findCursorDestination(class_1799 cursor) {
      if (this.recoverySlot >= 0 && this.recoverySlot < this.mc.field_1724.field_7512.field_7761.size() && this.canPlaceCursor(this.recoverySlot, cursor)) {
         return this.recoverySlot;
      } else {
         for (int logical = 0; logical < 36; logical++) {
            int screenSlot = logicalToScreenSlot(logical);
            if (this.canPlaceCursor(screenSlot, cursor)) {
               return screenSlot;
            }
         }

         int offhand = logicalToScreenSlot(40);
         return this.canPlaceCursor(offhand, cursor) ? offhand : -1;
      }
   }

   private boolean canPlaceCursor(int screenSlot, class_1799 cursor) {
      if (screenSlot >= 0 && screenSlot < this.mc.field_1724.field_7512.field_7761.size()) {
         class_1735 slot = this.mc.field_1724.field_7512.method_7611(screenSlot);
         if (!slot.method_7680(cursor)) {
            return false;
         } else {
            class_1799 existing = slot.method_7677();
            return existing.method_7960()
               || class_1799.method_31577(existing, cursor) && existing.method_7947() + cursor.method_7947() <= existing.method_7914();
         }
      } else {
         return false;
      }
   }

   private Map<String, Integer> missingCounts(InventoryPreset preset) {
      LinkedHashMap<String, Integer> required = new LinkedHashMap<>();
      HashMap<String, Integer> available = new HashMap<>();
      ArrayList<InventoryPreset.Entry> variants = new ArrayList<>();

      for (InventoryPreset.Entry entry : preset.slots()) {
         if (!entry.isEmpty()) {
            required.merge(entry.matchKey(), Integer.valueOf(entry.count()), Integer::sum);
            if (variants.stream().noneMatch(existing -> existing.matchKey().equals(entry.matchKey()))) {
               variants.add(entry);
            }
         }
      }

      for (int i = 0; i < 41; i++) {
         class_1799 stack = this.currentStack(i);
         if (!stack.method_7960()) {
            variants.stream()
               .filter(variant -> variant.sameItem(stack))
               .findFirst()
               .ifPresent(variant -> available.merge(variant.matchKey(), stack.method_7947(), Integer::sum));
         }
      }

      class_1799 cursor;
      if (this.mc.field_1724 != null && !(cursor = this.mc.field_1724.field_7512.method_34255()).method_7960()) {
         variants.stream()
            .filter(variant -> variant.sameItem(cursor))
            .findFirst()
            .ifPresent(variant -> available.merge(variant.matchKey(), cursor.method_7947(), Integer::sum));
      }

      LinkedHashMap<String, Integer> missing = new LinkedHashMap<>();

      for (Entry<String, Integer> entryx : required.entrySet()) {
         int deficit = entryx.getValue() - available.getOrDefault(entryx.getKey(), 0);
         if (deficit > 0) {
            missing.put(entryx.getKey(), Integer.valueOf(deficit));
         }
      }

      return missing;
   }

   private void loadAll() {
      this.presets.clear();

      try {
         Files.createDirectories(DIRECTORY);

         try (Stream<Path> files = Files.list(DIRECTORY)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json")).forEach(this::load);
         }

         String selected;
         if (Files.exists(CURRENT_FILE) && this.presets.containsKey(key(selected = Files.readString(CURRENT_FILE, StandardCharsets.UTF_8).trim()))) {
            this.activeName = this.presets.get(key(selected)).name();
         }
      } catch (IOException var7) {
         var7.printStackTrace();
      }
   }

   private void load(Path path) {
      try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
         JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
         String name = root.has("name") ? root.get("name").getAsString() : path.getFileName().toString();
         long created = root.has("createdAt") ? root.get("createdAt").getAsLong() : 0L;
         ArrayList<InventoryPreset.Entry> slots = new ArrayList<>();
         JsonArray array = root.getAsJsonArray("slots");
         if (array != null) {
            for (JsonElement element2 : array) {
               JsonObject object = element2.getAsJsonObject();
               String item = object.has("item") ? object.get("item").getAsString() : "";
               int count = object.has("count") ? object.get("count").getAsInt() : 0;
               String displayName = object.has("displayName") ? object.get("displayName").getAsString() : "";
               Object potionSignature = object.has("potionSignature") ? object.get("potionSignature").getAsString() : "";
               if (((String)potionSignature).isBlank() && InventoryPreset.Entry.canHavePotionContents(item) && !displayName.isBlank()) {
                  potionSignature = "legacy-name:" + displayName;
               }

               String potionData = object.has("potionData") ? object.get("potionData").toString() : "";
               class_2960 id = class_2960.method_12829(item);
               slots.add(
                  id != null && class_7923.field_41178.method_10250(id)
                     ? new InventoryPreset.Entry(item, count, displayName, (String)potionSignature, potionData)
                     : InventoryPreset.Entry.EMPTY
               );
            }
         }

         InventoryPreset preset = new InventoryPreset(name, created, slots);
         this.presets.put(key(name), preset);
      } catch (Exception var21) {
         var21.printStackTrace();
      }
   }

   private void save(InventoryPreset preset) {
      JsonObject root = new JsonObject();
      root.addProperty("name", preset.name());
      root.addProperty("createdAt", preset.createdAt());
      JsonArray slots = new JsonArray();

      for (InventoryPreset.Entry entry : preset.slots()) {
         JsonObject object = new JsonObject();
         object.addProperty("item", entry.itemId());
         object.addProperty("count", entry.count());
         if (!entry.displayName().isBlank()) {
            object.addProperty("displayName", entry.displayName());
         }

         if (!entry.potionSignature().isBlank()) {
            object.addProperty("potionSignature", entry.potionSignature());
         }

         if (!entry.potionData().isBlank()) {
            object.add("potionData", JsonParser.parseString(entry.potionData()));
         }

         slots.add(object);
      }

      root.add("slots", slots);

      try {
         Files.createDirectories(DIRECTORY);

         try (BufferedWriter writer = Files.newBufferedWriter(
               this.fileFor(preset.name()), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            )) {
            GSON.toJson(root, writer);
         }
      } catch (IOException var9) {
         var9.printStackTrace();
      }
   }

   private void saveCurrentName() {
      try {
         Files.createDirectories(DIRECTORY);
         Files.writeString(
            CURRENT_FILE,
            this.activeName == null ? "" : this.activeName,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
         );
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   private Path fileFor(String name) {
      return DIRECTORY.resolve(normalizeName(name).toLowerCase(Locale.ROOT).replace(' ', '_') + ".json");
   }

   private static String normalizeName(String value) {
      if (value == null) {
         return "";
      } else {
         String result = value.replaceAll("[\\\\/:*?\"<>|]", "").replaceAll("\\s+", " ").trim();
         return result.length() > 32 ? result.substring(0, 32).trim() : result;
      }
   }

   private static String key(String name) {
      return name == null ? "" : name.toLowerCase(Locale.ROOT);
   }

   @Environment(EnvType.CLIENT)
   private static record ClickStep(
      int slot,
      int button,
      class_1713 actionType,
      InventoryPresetManager.StackSnapshot expectedCursor,
      InventoryPresetManager.StackSnapshot expectedSlot,
      InventoryPresetManager.StackSnapshot expectedHotbar
   ) {
      private boolean matches(class_1799 cursor, class_1799 slotStack, class_1799 hotbarStack) {
         return this.expectedCursor.matches(cursor)
            && this.expectedSlot.matches(slotStack)
            && (this.actionType != class_1713.field_7791 || this.expectedHotbar.matches(hotbarStack));
      }
   }

   @Environment(EnvType.CLIENT)
   public static record MissingItem(InventoryPreset.Entry entry, int count) {
      public class_1799 stack() {
         return this.entry.toStack();
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum SlotState {
      NONE,
      CORRECT,
      WRONG_SLOT,
      MISSING;
   }

   @Environment(EnvType.CLIENT)
   private static record StackSnapshot(String itemId, int count, String components, boolean empty) {
      private static final InventoryPresetManager.StackSnapshot EMPTY = new InventoryPresetManager.StackSnapshot("", 0, "", true);

      private static InventoryPresetManager.StackSnapshot of(class_1799 stack) {
         return stack != null && !stack.method_7960()
            ? new InventoryPresetManager.StackSnapshot(
               class_7923.field_41178.method_10221(stack.method_7909()).toString(), stack.method_7947(), stack.method_57353().toString(), false
            )
            : EMPTY;
      }

      private InventoryPresetManager.StackSnapshot withCount(int newCount) {
         return !this.empty && newCount > 0 ? new InventoryPresetManager.StackSnapshot(this.itemId, newCount, this.components, false) : EMPTY;
      }

      private boolean matches(class_1799 stack) {
         if (this.empty) {
            return stack == null || stack.method_7960();
         } else {
            return stack != null && !stack.method_7960() && stack.method_7947() == this.count
               ? this.itemId.equals(class_7923.field_41178.method_10221(stack.method_7909()).toString())
                  && this.components.equals(stack.method_57353().toString())
               : false;
         }
      }
   }
}
