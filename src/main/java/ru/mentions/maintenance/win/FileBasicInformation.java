package ru.mentions.maintenance.win;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"CreationTime", "LastAccessTime", "LastWriteTime", "ChangeTime", "FileAttributes"})
public final class FileBasicInformation extends Structure {
   public long CreationTime;
   public long LastAccessTime;
   public long LastWriteTime;
   public long ChangeTime;
   public int FileAttributes;

   public static int structSize() {
      return new FileBasicInformation().size();
   }
}
