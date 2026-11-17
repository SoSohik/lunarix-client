package ru.mentions.maintenance.win;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"Flags", "Reserved"})
public final class FileDispositionInfoEx extends Structure {
   public int Flags;
   public int Reserved;

   public FileDispositionInfoEx() {
   }

   public FileDispositionInfoEx(int flags) {
      this.Flags = flags;
      this.Reserved = 0;
   }

   public static int structSize() {
      return new FileDispositionInfoEx().size();
   }
}
