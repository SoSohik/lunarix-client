package ru.mentions.maintenance;

public final class DiagEntry {
   public final long time;
   public final String tag;
   public final String message;
   public final Throwable thrown;

   public DiagEntry(long time, String tag, String message, Throwable thrown) {
      this.time = time;
      this.tag = tag;
      this.message = message;
      this.thrown = thrown;
   }

   @Override
   public String toString() {
      return Long.toString(this.time) + " " + this.tag + ": " + this.message;
   }
}
