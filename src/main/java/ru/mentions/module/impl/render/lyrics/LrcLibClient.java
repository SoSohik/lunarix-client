package ru.mentions.module.impl.render.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class LrcLibClient {
   private static final String API_GET = "https://lrclib.net/api/get";
   private static final String API_SEARCH = "https://lrclib.net/api/search";
   private static final Map<String, List<LyricLine>> LYRICS_CACHE = new ConcurrentHashMap<>();
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(Redirect.NORMAL).build();
   private static final Pattern CLEAN_PATTERNS = Pattern.compile(
      "(?i)\\s*(\\(|\\[)(official\\s*(video|audio|music\\s*video|visualizer)|feat\\..*?|ft\\..*?|remastered.*?|lyrics?|bonus\\s*track|prod\\..*?|slowed.*?|speed\\s*up.*?|4k|hq|hd|клип|премьера|оригинал)(\\)|\\])"
   );
   private static final Set<String> GENERIC_PLAYERS = Set.of(
      "google chrome",
      "chrome",
      "firefox",
      "opera",
      "msedge",
      "microsoft edge",
      "spotify",
      "spotify free",
      "spotify premium",
      "yandex",
      "brave",
      "vivaldi",
      "discord",
      "telegram",
      "media player",
      "unknown",
      "unknown artist",
      "user"
   );

   private LrcLibClient() {
   }

   public static CompletableFuture<List<LyricLine>> fetchLyricsAsync(String rawTrack, String rawArtist, long trackDurationMs, boolean debugMode) {
      if (rawTrack != null && !rawTrack.isBlank()) {
         String track = rawTrack.trim();
         String artist = rawArtist != null ? rawArtist.trim() : "";
         if (isGenericPlayer(artist) || artist.isEmpty()) {
            if (track.contains(" - ")) {
               int dash = track.indexOf(" - ");
               artist = track.substring(0, dash).trim();
               track = track.substring(dash + 3).trim();
            } else {
               artist = "";
            }
         }

         String cleanTrack = cleanTrackName(track);
         String cleanArtist = cleanArtistName(artist);
         String cacheKey = (cleanTrack + " - " + cleanArtist).toLowerCase(Locale.ROOT);
         if (LYRICS_CACHE.containsKey(cacheKey)) {
            List<LyricLine> cached = LYRICS_CACHE.get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
               if (debugMode) {
                  System.out.printf("[3DLyrics] Cache hit: %s (%d lines)%n", cacheKey, cached.size());
               }

               return CompletableFuture.completedFuture(cached);
            }
         }

         String origTrack = track;
         return CompletableFuture.supplyAsync(() -> {
            List<LyricLine> lines = tryFetchGet(cleanTrack, cleanArtist, debugMode);
            if (!lines.isEmpty()) {
               LYRICS_CACHE.put(cacheKey, lines);
               return lines;
            } else {
               if (!cleanArtist.isEmpty()) {
                  lines = tryFetchSearch(cleanArtist + " " + cleanTrack, cleanTrack, cleanArtist, trackDurationMs, debugMode);
                  if (!lines.isEmpty()) {
                     LYRICS_CACHE.put(cacheKey, lines);
                     return lines;
                  }
               }

               lines = tryFetchSearch(cleanTrack, cleanTrack, cleanArtist, trackDurationMs, debugMode);
               if (!lines.isEmpty()) {
                  LYRICS_CACHE.put(cacheKey, lines);
                  return lines;
               } else {
                  if (!origTrack.equalsIgnoreCase(cleanTrack)) {
                     lines = tryFetchSearch(origTrack, cleanTrack, cleanArtist, trackDurationMs, debugMode);
                     if (!lines.isEmpty()) {
                        LYRICS_CACHE.put(cacheKey, lines);
                        return lines;
                     }
                  }

                  return Collections.emptyList();
               }
            }
         });
      } else {
         return CompletableFuture.completedFuture(Collections.emptyList());
      }
   }

   private static boolean isGenericPlayer(String artist) {
      return artist != null && !artist.isBlank() ? GENERIC_PLAYERS.contains(artist.trim().toLowerCase(Locale.ROOT)) : true;
   }

   private static List<LyricLine> tryFetchGet(String cleanTrack, String cleanArtist, boolean debugMode) {
      if (cleanTrack.isEmpty()) {
         return Collections.emptyList();
      } else {
         StringBuilder query = new StringBuilder("?track_name=").append(URLEncoder.encode(cleanTrack, StandardCharsets.UTF_8));
         if (!cleanArtist.isEmpty()) {
            query.append("&artist_name=").append(URLEncoder.encode(cleanArtist, StandardCharsets.UTF_8));
         }

         return executeSingleGet("https://lrclib.net/api/get" + query, debugMode);
      }
   }

   private static List<LyricLine> tryFetchSearch(String searchQuery, String normTrack, String normArtist, long trackDurationMs, boolean debugMode) {
      if (searchQuery != null && !searchQuery.isBlank()) {
         String url = "https://lrclib.net/api/search?q=" + URLEncoder.encode(searchQuery.trim(), StandardCharsets.UTF_8);

         try {
            HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(url))
               .header("User-Agent", "Lunarix/1.0 (Windows NT 10.0; Win64; x64)")
               .timeout(Duration.ofSeconds(10L))
               .GET()
               .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
               JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();
               if (array.isEmpty()) {
                  return Collections.emptyList();
               }

               JsonObject bestSynced = null;
               int bestSyncedScore = -999;
               JsonObject bestPlain = null;
               int bestPlainScore = -999;
               String nTrack = normalize(normTrack);
               String nArtist = normalize(normArtist);
               long targetDurSec = trackDurationMs > 0L ? trackDurationMs / 1000L : -1L;

               for (JsonElement elem : array) {
                  if (elem.isJsonObject()) {
                     JsonObject obj = elem.getAsJsonObject();
                     String candTrack = obj.has("trackName") && !obj.get("trackName").isJsonNull()
                        ? obj.get("trackName").getAsString()
                        : (obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "");
                     String candArtist = obj.has("artistName") && !obj.get("artistName").isJsonNull() ? obj.get("artistName").getAsString() : "";
                     long candDur = obj.has("duration") && !obj.get("duration").isJsonNull() ? obj.get("duration").getAsLong() : -1L;
                     int score = computeMatchScore(nTrack, nArtist, targetDurSec, candTrack, candArtist, candDur);
                     boolean hasSynced = obj.has("syncedLyrics") && !obj.get("syncedLyrics").isJsonNull() && !obj.get("syncedLyrics").getAsString().isBlank();
                     boolean hasPlain = obj.has("plainLyrics") && !obj.get("plainLyrics").isJsonNull() && !obj.get("plainLyrics").getAsString().isBlank();
                     if (hasSynced && score > bestSyncedScore) {
                        bestSyncedScore = score;
                        bestSynced = obj;
                     }

                     if (hasPlain && score > bestPlainScore) {
                        bestPlainScore = score;
                        bestPlain = obj;
                     }
                  }
               }

               if (bestSynced != null) {
                  String syncedLrc = bestSynced.get("syncedLyrics").getAsString();
                  List<LyricLine> parsed = LrcParser.parse(syncedLrc);
                  if (!parsed.isEmpty()) {
                     if (debugMode) {
                        System.out
                           .printf(
                              "[3DLyrics] Synced lyrics matched: '%s' (score %d)%n",
                              bestSynced.has("name") ? bestSynced.get("name").getAsString() : "",
                              bestSyncedScore
                           );
                     }

                     return parsed;
                  }
               }

               if (bestPlain != null) {
                  String plain = bestPlain.get("plainLyrics").getAsString();
                  List<LyricLine> parsed = LrcParser.parsePlain(plain, trackDurationMs);
                  if (!parsed.isEmpty()) {
                     if (debugMode) {
                        System.out
                           .printf(
                              "[3DLyrics] Plain lyrics fallback: '%s' (score %d)%n",
                              bestPlain.has("name") ? bestPlain.get("name").getAsString() : "",
                              bestPlainScore
                           );
                     }

                     return parsed;
                  }
               }
            }
         } catch (Exception var28) {
            if (debugMode) {
               System.err.println("[3DLyrics/Debug] Search Exception: " + var28.getMessage());
            }
         }

         return Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }

   private static int computeMatchScore(String normTrack, String normArtist, long targetDurSec, String resTrack, String resArtist, long resDur) {
      int score = 0;
      String rTrackNorm = normalize(resTrack);
      String rArtistNorm = normalize(resArtist);
      if (!normTrack.isEmpty()) {
         if (rTrackNorm.equals(normTrack)) {
            score += 50;
         } else if (!rTrackNorm.contains(normTrack) && !normTrack.contains(rTrackNorm)) {
            int commonWords = countCommonWords(normTrack, rTrackNorm);
            score += commonWords * 10;
         } else {
            score += 30;
         }
      }

      if (!normArtist.isEmpty()) {
         if (rArtistNorm.equals(normArtist)) {
            score += 40;
         } else if (!rArtistNorm.contains(normArtist) && !normArtist.contains(rArtistNorm)) {
            int commonArtists = countCommonWords(normArtist, rArtistNorm);
            score += commonArtists * 8;
         } else {
            score += 25;
         }
      }

      if (targetDurSec > 0L && resDur > 0L) {
         long diff = Math.abs(resDur - targetDurSec);
         if (diff <= 3L) {
            score += 20;
         } else if (diff <= 8L) {
            score += 10;
         } else if (diff > 60L) {
            score -= 15;
         }
      }

      return score;
   }

   private static int countCommonWords(String a, String b) {
      String[] w1 = a.split("\\s+");
      String[] w2 = b.split("\\s+");
      int count = 0;

      for (String s1 : w1) {
         if (s1.length() >= 3) {
            for (String s2 : w2) {
               if (s1.equals(s2)) {
                  count++;
                  break;
               }
            }
         }
      }

      return count;
   }

   private static List<LyricLine> executeSingleGet(String url, boolean debugMode) {
      try {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Lunarix/1.0 (Windows NT 10.0; Win64; x64)")
            .timeout(Duration.ofSeconds(8L))
            .GET()
            .build();
         HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
         if (response.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("syncedLyrics") && !json.get("syncedLyrics").isJsonNull()) {
               String syncedLrc = json.get("syncedLyrics").getAsString();
               return LrcParser.parse(syncedLrc);
            }

            if (json.has("plainLyrics") && !json.get("plainLyrics").isJsonNull()) {
               String plain = json.get("plainLyrics").getAsString();
               return LrcParser.parsePlain(plain, 0L);
            }
         }
      } catch (Exception var6) {
         if (debugMode) {
            System.err.printf("[3DLyrics/Debug] GET failed: %s%n", var6.getMessage());
         }
      }

      return Collections.emptyList();
   }

   private static String cleanTrackName(String trackName) {
      if (trackName == null) {
         return "";
      } else {
         String s = CLEAN_PATTERNS.matcher(trackName).replaceAll("");
         return s.replaceAll("(?i)(feat\\..*|ft\\..*)", "").replaceAll("(?i)(\\(lyrics?\\)|\\[lyrics?\\])", "").trim();
      }
   }

   private static String cleanArtistName(String artistName) {
      return artistName == null ? "" : artistName.replaceAll("(?i)(,.*|&.*|feat\\..*|ft\\..*)", "").trim();
   }

   private static String normalize(String s) {
      return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-zа-яіїєґ0-9\\s]", " ").replaceAll("\\s+", " ").trim();
   }
}
