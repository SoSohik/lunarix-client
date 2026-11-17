package ru.mentions.module.api.settings.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;

@Environment(EnvType.CLIENT)
public class BlockListSetting extends Setting<List<String>> {
   public String input = "";
   private static List<String> ALL_IDS;
   private static String[] SEARCH_KEYS;
   private static final Map<String, String> DISPLAY = new HashMap<>();
   private static final Map<String, Integer> SWATCH = new HashMap<>();

   public BlockListSetting(Module parent, String name) {
      super(parent, name, new ArrayList<>());
   }

   public List<String> getIds() {
      return this.getValue();
   }

   public boolean addId(String raw) {
      String id = normalize(raw);
      if (id != null && !this.getValue().contains(id)) {
         this.getValue().add(id);
         return true;
      } else {
         return false;
      }
   }

   public boolean addCurrentInput() {
      if (this.addId(this.input)) {
         this.input = "";
         return true;
      } else {
         return false;
      }
   }

   public void remove(String id) {
      this.getValue().remove(id);
   }

   public Set<class_2248> resolveBlocks() {
      HashSet<class_2248> blocks = new HashSet<>();

      for (Object rawId : this.getValue()) {
         String id = (String)rawId;
         class_2960 ident = class_2960.method_12829(id);
         if (ident != null) {
            class_7923.field_41175.method_17966(ident).ifPresent(blocks::add);
         }
      }

      return blocks;
   }

   public static String normalize(String raw) {
      if (raw == null) {
         return null;
      } else {
         Object s = raw.trim().toLowerCase();
         if (((String)s).isEmpty()) {
            return null;
         } else {
            if (!((String)s).contains(":")) {
               s = "minecraft:" + (String)s;
            }

            class_2960 id;
            return (id = class_2960.method_12829((String)s)) != null && class_7923.field_41175.method_10250(id) ? (String)s : null;
         }
      }
   }

   public static String shortName(String id) {
      int i = id.indexOf(58);
      return i >= 0 ? id.substring(i + 1) : id;
   }

   private static void buildCache() {
      ArrayList<String> ids = new ArrayList<>();

      try {
         for (class_2960 id : class_7923.field_41175.method_10235()) {
            ids.add(id.toString());
         }
      } catch (Exception var9) {
      }

      ids.sort(String::compareTo);
      String[] keys = new String[ids.size()];

      for (int i = 0; i < ids.size(); i++) {
         String id = ids.get(i);

         String name;
         try {
            name = computeDisplayName(id);
         } catch (Exception var8) {
            name = shortName(id);
         }

         int sw;
         try {
            sw = computeSwatch(id);
         } catch (Exception var7) {
            sw = -11184811;
         }

         DISPLAY.put(id, name);
         SWATCH.put(id, sw);
         keys[i] = (shortName(id) + " " + name + " " + id).toLowerCase();
      }

      SEARCH_KEYS = keys;
      ALL_IDS = ids;
   }

   public static List<String> allIds() {
      if (ALL_IDS == null) {
         buildCache();
      }

      return ALL_IDS;
   }

   public static List<String> search(String query) {
      if (ALL_IDS == null) {
         buildCache();
      }

      String q = query == null ? "" : query.trim().toLowerCase();
      if (q.isEmpty()) {
         return ALL_IDS;
      } else {
         ArrayList<String> out = new ArrayList<>();

         for (int i = 0; i < ALL_IDS.size(); i++) {
            if (SEARCH_KEYS[i].contains(q)) {
               out.add(ALL_IDS.get(i));
            }
         }

         return out;
      }
   }

   public static String displayName(String id) {
      return DISPLAY.computeIfAbsent(id, BlockListSetting::computeDisplayName);
   }

   public static int swatchColor(String id) {
      return SWATCH.computeIfAbsent(id, BlockListSetting::computeSwatch);
   }

   private static String computeDisplayName(String id) {
      class_2960 ident = class_2960.method_12829(id);
      Optional block;
      return ident != null && (block = class_7923.field_41175.method_17966(ident)).isPresent()
         ? ((class_2248)block.get()).method_9518().getString()
         : shortName(id);
   }

   private static int computeSwatch(String id) {
      class_2960 ident = class_2960.method_12829(id);
      int c;
      Optional block;
      return ident != null
            && (block = class_7923.field_41175.method_17966(ident)).isPresent()
            && (c = ((class_2248)block.get()).method_26403().field_16011) != 0
         ? 0xFF000000 | c & 16777215
         : -11184811;
   }

   public BlockListSetting setVisible(Supplier<Boolean> value) {
      return (BlockListSetting)super.setVisible(value);
   }
}
