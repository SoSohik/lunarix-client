package ru.mentions.module.api.settings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;

@Environment(EnvType.CLIENT)
public class PixelGridSetting extends Setting<boolean[][]> {
   public final int size;

   public PixelGridSetting(Module parent, String name, int size) {
      super(parent, name, new boolean[size][size]);
      this.size = size;
      this.reset();
   }

   public int center() {
      return this.size / 2;
   }

   public boolean inBounds(int x, int y) {
      return x >= 0 && y >= 0 && x < this.size && y < this.size;
   }

   public boolean get(int x, int y) {
      return this.inBounds(x, y) && this.getValue()[y][x];
   }

   public void put(int x, int y, boolean filled) {
      if (this.inBounds(x, y)) {
         this.getValue()[y][x] = filled;
      }
   }

   public void clear() {
      for (boolean[] row : this.getValue()) {
         Arrays.fill(row, false);
      }
   }

   public boolean isEmpty() {
      boolean[][] blArray = this.getValue();
      int n = blArray.length;

      for (int i = 0; i < n; i++) {
         boolean[] row;
         for (boolean cell : row = blArray[i]) {
            if (cell) {
               return false;
            }
         }
      }

      return true;
   }

   public void reset() {
      this.clear();
      int c = this.center();
      int gap = Math.max(1, this.size / 7);
      int length = Math.max(2, this.size / 5);

      for (int i = gap; i < gap + length && c + i < this.size; i++) {
         this.put(c, c - i, true);
         this.put(c, c + i, true);
         this.put(c - i, c, true);
         this.put(c + i, c, true);
      }
   }

   public List<String> toRows() {
      ArrayList<String> rows = new ArrayList<>(this.size);

      for (boolean[] row : this.getValue()) {
         StringBuilder sb = new StringBuilder(this.size);

         for (boolean cell : row) {
            sb.append((char)(cell ? '1' : '0'));
         }

         rows.add(sb.toString());
      }

      return rows;
   }

   public void fromRows(List<String> rows) {
      this.clear();

      for (int y = 0; y < this.size && y < rows.size(); y++) {
         String row = rows.get(y);

         for (int x = 0; x < this.size && x < row.length(); x++) {
            this.getValue()[y][x] = row.charAt(x) == '1';
         }
      }
   }

   public PixelGridSetting setVisible(Supplier<Boolean> value) {
      return (PixelGridSetting)super.setVisible(value);
   }
}
