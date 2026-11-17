package ru.mentions.module.impl.render.lyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class LyricLineSplitter {
   private static final Pattern PUNCTUATION_SPLIT = Pattern.compile("(?<=[,;—–-]|\n|\\.{3})\\s+");
   private static final String[] CONJUNCTIONS = new String[]{
      "and",
      "but",
      "or",
      "so",
      "because",
      "though",
      "that",
      "with",
      "when",
      "і",
      "й",
      "та",
      "а",
      "але",
      "бо",
      "що",
      "як",
      "коли",
      "де",
      "и",
      "но",
      "что",
      "как",
      "если",
      "где",
      "когда"
   };

   private LyricLineSplitter() {
   }

   public static List<LyricLine> splitLongLines(List<LyricLine> originalLines) {
      if (originalLines != null && !originalLines.isEmpty()) {
         List<LyricLine> result = new ArrayList<>();

         for (int i = 0; i < originalLines.size(); i++) {
            LyricLine current = originalLines.get(i);
            long nextTimeMs = i + 1 < originalLines.size() ? originalLines.get(i + 1).timestampMs() : current.timestampMs() + 4500L;
            long lineDuration = Math.max(1400L, nextTimeMs - current.timestampMs());
            String text = current.text().trim();
            if (!text.isEmpty()) {
               List<String> phrases = smartSplitPhrases(text);
               if (phrases.size() <= 1) {
                  result.add(current);
               } else {
                  int totalChars = 0;

                  for (String p : phrases) {
                     totalChars += Math.max(3, p.length());
                  }

                  long currentOffset = 0L;

                  for (int pIdx = 0; pIdx < phrases.size(); pIdx++) {
                     String phrase = phrases.get(pIdx);
                     int pLen = Math.max(3, phrase.length());
                     long pDuration = (long)((double)lineDuration * ((double)pLen / (double)totalChars));
                     pDuration = Math.max(900L, pDuration);
                     long phraseStartMs = current.timestampMs() + currentOffset;
                     result.add(new LyricLine(phraseStartMs, phrase));
                     currentOffset += pDuration;
                  }
               }
            }
         }

         Collections.sort(result);
         return result;
      } else {
         return Collections.emptyList();
      }
   }

   private static List<String> smartSplitPhrases(String text) {
      List<String> list = new ArrayList<>();
      String[] punctParts = PUNCTUATION_SPLIT.split(text);
      if (punctParts.length > 1) {
         for (String part : punctParts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
               trimmed = cleanTrailingPunctuation(trimmed);
               if (!trimmed.isEmpty()) {
                  list.add(trimmed);
               }
            }
         }

         if (list.size() > 1) {
            return mergeShortOrphanPhrases(list);
         }

         list.clear();
      }

      String[] words = text.split("\\s+");
      if (words.length <= 5) {
         list.add(text);
         return list;
      } else {
         int mid = words.length / 2;
         int bestSplit = -1;
         int bestScore = -100;

         for (int i = 2; i < words.length - 2; i++) {
            String wordLower = words[i].toLowerCase().replaceAll("[^a-zа-яіїєґ]", "");
            int distFromCenter = Math.abs(i - mid);
            int score = 20 - distFromCenter * 3;

            for (String conj : CONJUNCTIONS) {
               if (conj.equals(wordLower)) {
                  score += 40;
                  break;
               }
            }

            if (score > bestScore) {
               bestScore = score;
               bestSplit = i;
            }
         }

         if (bestSplit > 0) {
            StringBuilder part1 = new StringBuilder();
            StringBuilder part2 = new StringBuilder();

            for (int i = 0; i < words.length; i++) {
               if (i < bestSplit) {
                  if (part1.length() > 0) {
                     part1.append(" ");
                  }

                  part1.append(words[i]);
               } else {
                  if (part2.length() > 0) {
                     part2.append(" ");
                  }

                  part2.append(words[i]);
               }
            }

            list.add(cleanTrailingPunctuation(part1.toString().trim()));
            list.add(part2.toString().trim());
            return list;
         } else {
            StringBuilder b1 = new StringBuilder();
            StringBuilder b2 = new StringBuilder();

            for (int ix = 0; ix < words.length; ix++) {
               if (ix < mid) {
                  if (b1.length() > 0) {
                     b1.append(" ");
                  }

                  b1.append(words[ix]);
               } else {
                  if (b2.length() > 0) {
                     b2.append(" ");
                  }

                  b2.append(words[ix]);
               }
            }

            list.add(b1.toString());
            list.add(b2.toString());
            return list;
         }
      }
   }

   private static String cleanTrailingPunctuation(String s) {
      return s == null ? "" : s.replaceAll("[,;—–\\-]+$", "").trim();
   }

   private static List<String> mergeShortOrphanPhrases(List<String> phrases) {
      List<String> merged = new ArrayList<>();

      for (int i = 0; i < phrases.size(); i++) {
         String curr = phrases.get(i);
         String[] words = curr.split("\\s+");
         if (words.length == 1 && curr.length() <= 4 && !merged.isEmpty()) {
            int lastIdx = merged.size() - 1;
            merged.set(lastIdx, merged.get(lastIdx) + ", " + curr);
         } else if (words.length == 1 && curr.length() <= 4 && i + 1 < phrases.size()) {
            phrases.set(i + 1, curr + " " + phrases.get(i + 1));
         } else {
            merged.add(curr);
         }
      }

      return merged;
   }
}
