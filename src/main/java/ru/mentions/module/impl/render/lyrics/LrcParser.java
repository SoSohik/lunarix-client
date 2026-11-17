package ru.mentions.module.impl.render.lyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LrcParser {
   private static final Pattern TIMESTAMP_TAG = Pattern.compile("\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?\\]");
   private static final Pattern OFFSET_TAG = Pattern.compile("(?i)\\[offset:\\s*([+-]?\\d+)\\]");

   private LrcParser() {
   }

   public static List<LyricLine> parse(String lrcContent) {
      if (lrcContent != null && !lrcContent.isBlank()) {
         List<LyricLine> lines = new ArrayList<>();
         String[] rawLines = lrcContent.split("\\r?\\n");
         long fileOffsetMs = 0L;

         for (String line : rawLines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
               Matcher offsetMatcher = OFFSET_TAG.matcher(trimmed);
               if (offsetMatcher.find()) {
                  try {
                     fileOffsetMs = Long.parseLong(offsetMatcher.group(1));
                  } catch (NumberFormatException var22) {
                  }
                  break;
               }
            }
         }

         for (String linex : rawLines) {
            String trimmed = linex.trim();
            if (!trimmed.isEmpty()) {
               Matcher matcher = TIMESTAMP_TAG.matcher(trimmed);
               List<Long> timestamps = new ArrayList<>();

               while (matcher.find()) {
                  try {
                     long minutes = Long.parseLong(matcher.group(1));
                     long seconds = Long.parseLong(matcher.group(2));
                     String fractionStr = matcher.group(3);
                     long millis = 0L;
                     if (fractionStr != null && !fractionStr.isEmpty()) {
                        millis = Long.parseLong(fractionStr);
                        if (fractionStr.length() == 1) {
                           millis *= 100L;
                        } else if (fractionStr.length() == 2) {
                           millis *= 10L;
                        }
                     }

                     long totalTimeMs = minutes * 60000L + seconds * 1000L + millis + fileOffsetMs;
                     timestamps.add(Math.max(0L, totalTimeMs));
                  } catch (NumberFormatException var21) {
                  }
               }

               if (!timestamps.isEmpty()) {
                  String text = TIMESTAMP_TAG.matcher(trimmed).replaceAll("").trim();
                  if (!text.isEmpty()) {
                     for (Long t : timestamps) {
                        lines.add(new LyricLine(t, text));
                     }
                  }
               }
            }
         }

         Collections.sort(lines);
         return lines;
      } else {
         return Collections.emptyList();
      }
   }

   public static List<LyricLine> parsePlain(String plainLyrics, long durationMs) {
      if (plainLyrics != null && !plainLyrics.isBlank()) {
         List<LyricLine> lines = new ArrayList<>();
         String[] rawLines = plainLyrics.split("\\r?\\n");
         List<String> validTexts = new ArrayList<>();

         for (String r : rawLines) {
            String t = r.trim();
            if (!t.isEmpty() && !t.startsWith("[") && !t.endsWith("]")) {
               validTexts.add(t);
            }
         }

         if (validTexts.isEmpty()) {
            return Collections.emptyList();
         } else {
            long effectiveDuration = durationMs > 10000L ? durationMs : (long)validTexts.size() * 3500L;
            long startOffset = Math.min(8000L, effectiveDuration / 10L);
            long usableDuration = Math.max(10000L, effectiveDuration - startOffset - 5000L);
            long step = usableDuration / (long)validTexts.size();

            for (int i = 0; i < validTexts.size(); i++) {
               long timeMs = startOffset + (long)i * step;
               lines.add(new LyricLine(timeMs, validTexts.get(i)));
            }

            return lines;
         }
      } else {
         return Collections.emptyList();
      }
   }
}
