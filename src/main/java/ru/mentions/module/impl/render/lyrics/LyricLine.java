package ru.mentions.module.impl.render.lyrics;

public record LyricLine(long timestampMs, String text) implements Comparable<LyricLine> {
   public int compareTo(LyricLine o) {
      return Long.compare(this.timestampMs, o.timestampMs);
   }
}
