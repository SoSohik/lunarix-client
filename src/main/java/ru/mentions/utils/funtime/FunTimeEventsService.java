package ru.mentions.utils.funtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.lang.Lang;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.math.ServerUtil;

@Environment(EnvType.CLIENT)
public final class FunTimeEventsService {
   private static final Path LOCAL_TOKEN_FILE = Path.of("lunarix/funtime_token.txt");
   private static final Path FALLBACK_TOKEN_FILE = Path.of("C:/lunarix/client1_21_11/funtime_token.txt");
   private static final String API_BASE_V2 = "https://api.funtime.su/v2";
   private static final String API_EVENTS_V2 = "https://api.funtime.su/v2/events";
   private static final String API_MINES_V2 = "https://api.funtime.su/v2/mines";
   private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "FunTime-API-Worker");
      t.setDaemon(true);
      return t;
   });
   private static String clientToken = "";
   private static final List<FunTimeEventsService.FunTimeEvent> events = new CopyOnWriteArrayList<>();
   private static final List<FunTimeEventsService.FunTimeMine> mines = new CopyOnWriteArrayList<>();
   private static boolean loading = false;
   private static String lastStatus = Lang.pick("Готовий до завантаження", "Готов к загрузке", "Ready to fetch");
   private static long lastUpdatedMs = 0L;
   private static boolean isDemo = false;
   private static final Pattern COORDS_XYZ_PATTERN = Pattern.compile(
      "(?i)[xх]\\s*[:=]?\\s*(-?\\d+)[^\\d-]+[yу]\\s*[:=]? *(-?\\d+)[^\\d-]+[zз]\\s*[:=]? *(-?\\d+)"
   );
   private static final Pattern COORDS_XZ_PATTERN = Pattern.compile("(?i)[xх]\\s*[:=]?\\s*(-?\\d+)[^\\d-]+[zз]\\s*[:=]? *(-?\\d+)");
   private static final Pattern TIME_MIN_PATTERN = Pattern.compile("(?i)через\\s+(\\d+)\\s*(?:мин|м|min)");
   private static final Pattern TIME_SEC_PATTERN = Pattern.compile("(?i)через\\s+(\\d+)\\s*(?:сек|с|sec)");
   private static final Pattern ANARCHY_NUM_PATTERN = Pattern.compile("(?i)(?:анархи[ияе]|anarchy|an)\\s*#?\\s*(\\d+)");

   public static String getClientToken() {
      return clientToken;
   }

   public static void setClientToken(String token) {
      clientToken = token != null ? token.trim() : "";
      saveToken();
      refresh(null);
   }

   public static List<FunTimeEventsService.FunTimeEvent> getEvents() {
      if (isDemo) {
         checkDemoCycle();
      }

      return Collections.unmodifiableList(events);
   }

   public static List<FunTimeEventsService.FunTimeMine> getMines() {
      if (isDemo && mines.isEmpty()) {
         loadDemoMines();
      }

      return Collections.unmodifiableList(mines);
   }

   public static boolean isLoading() {
      return loading;
   }

   public static String getLastStatus() {
      return lastStatus;
   }

   public static boolean isDemoMode() {
      return isDemo;
   }

   public static long getLastUpdatedMs() {
      return lastUpdatedMs;
   }

   public static void refresh(Consumer<String> onComplete) {
      if (!loading) {
         loading = true;
         lastStatus = Lang.pick("Оновлення з api.funtime.su...", "Загрузка с api.funtime.su...", "Fetching api.funtime.su...");
         EXECUTOR.submit(
            () -> {
               try {
                  if (clientToken == null || clientToken.isBlank()) {
                     isDemo = true;
                     loadDemoEvents();
                     loadDemoMines();
                     lastStatus = Lang.pick(
                        "Демо / Чат-трекер (введіть токен FunTime API)", "Демо-режим (введите токен FunTime API)", "Demo / Chat tracker mode"
                     );
                     lastUpdatedMs = System.currentTimeMillis();
                     loading = false;
                     if (onComplete != null) {
                        onComplete.accept(lastStatus);
                     }

                     return;
                  }

                  List<FunTimeEventsService.FunTimeEvent> fetchedEvents = fetchEventsFromApi("https://api.funtime.su/v2/events", clientToken);
                  if (fetchedEvents != null) {
                     events.clear();
                     events.addAll(fetchedEvents);
                     events.sort(Comparator.comparingInt(FunTimeEventsService.FunTimeEvent::getRemainingSeconds));
                     isDemo = false;
                     lastStatus = Lang.pick(
                        "Оновлено: " + events.size() + " івентів", "Обновлено: " + events.size() + " ивентов", "Updated: " + events.size() + " events"
                     );
                     lastUpdatedMs = System.currentTimeMillis();
                  }

                  try {
                     List<FunTimeEventsService.FunTimeMine> fetchedMines = fetchMinesFromApi("https://api.funtime.su/v2/mines", clientToken);
                     if (fetchedMines != null) {
                        mines.clear();
                        mines.addAll(fetchedMines);
                        mines.sort(Comparator.comparingInt(FunTimeEventsService.FunTimeMine::getRemainingSeconds));
                     }
                  } catch (Exception var7) {
                  }
               } catch (Exception var8) {
                  lastStatus = Lang.pick("Помилка API: " + var8.getMessage(), "Ошибка API: " + var8.getMessage(), "API Error: " + var8.getMessage());
                  if (events.isEmpty()) {
                     isDemo = true;
                     loadDemoEvents();
                     loadDemoMines();
                  }
               } finally {
                  loading = false;
                  if (onComplete != null) {
                     onComplete.accept(lastStatus);
                  }
               }
            }
         );
      }
   }

   public static void onChatMessage(String rawMessage) {
      if (rawMessage != null && !rawMessage.isEmpty()) {
         String clean = rawMessage.replaceAll("§[0-9a-fk-or]", "").trim();
         String lower = clean.toLowerCase(Locale.ROOT);
         boolean isMystic = lower.contains("мистик") || lower.contains("мистический") || lower.contains("містик");
         boolean isDeath = lower.contains("сундук смерти") || lower.contains("скриня смерті") || lower.contains("смерти");
         boolean isVolcano = lower.contains("вулкан") || lower.contains("извержение");
         boolean isBeacon = lower.contains("маяк-убийца") || lower.contains("маяк убийца") || lower.contains("маяк-вбивця");
         boolean isDragon = lower.contains("дракон") && (lower.contains("края") || lower.contains("краю") || lower.contains("возрождение"));
         if (isMystic || isDeath || isVolcano || isBeacon || isDragon) {
            String type;
            String name;
            String loot;
            if (isMystic) {
               type = "MYSTIC";
               name = Lang.pick("Містична скриня", "Мистический сундук", "Mystic Chest");
               loot = Lang.pick("Донат зілля, Тотеми, Броня бога", "Донат зелья, Тотемы, Броня бога", "Donate Potions, Totems, God Armor");
            } else if (isDeath) {
               type = "DEATH_CHEST";
               name = Lang.pick("Скриня смерті", "Сундук смерти", "Death Chest");
               loot = Lang.pick("Сфери, Шар стійкості, Талісмани", "Сферы, Шар стойкости, Талисманы", "Spheres, Resilience Orbs, Talismans");
            } else if (isVolcano) {
               type = "VOLCANO";
               name = Lang.pick("Вулкан", "Вулкан", "Volcano");
               loot = Lang.pick(
                  "Діамантові блоки, Незерит, Містичний лут", "Алмазные блоки, Незерит, Мистический лут", "Diamond Blocks, Netherite, Mystic Loot"
               );
            } else if (isBeacon) {
               type = "BEACON";
               name = Lang.pick("Маяк-вбивця", "Маяк-убийца", "Killer Beacon");
               loot = Lang.pick("Маяки, Золоті яблука, Крони", "Маяки, Золотые яблоки, Кроны", "Beacons, Golden Apples, Crowns");
            } else {
               type = "DRAGON";
               name = Lang.pick("Дракон Краю", "Дракон Края", "Ender Dragon");
               loot = Lang.pick("Яйце дракона, Незеритова броня 5/3", "Яйцо дракона, Незеритовая броня 5/3", "Dragon Egg, Netherite Armor 5/3");
            }

            int x = 0;
            int y = 68;
            int z = 0;
            boolean hasCoords = false;
            Matcher mXYZ = COORDS_XYZ_PATTERN.matcher(clean);
            if (mXYZ.find()) {
               try {
                  x = Integer.parseInt(mXYZ.group(1));
                  y = Integer.parseInt(mXYZ.group(2));
                  z = Integer.parseInt(mXYZ.group(3));
                  hasCoords = true;
               } catch (Exception var29) {
               }
            } else {
               Matcher mXZ = COORDS_XZ_PATTERN.matcher(clean);
               if (mXZ.find()) {
                  try {
                     x = Integer.parseInt(mXZ.group(1));
                     z = Integer.parseInt(mXZ.group(2));
                     hasCoords = true;
                  } catch (Exception var28) {
                  }
               }
            }

            int srvNum = ServerUtil.getAnarchy();
            Matcher mAn = ANARCHY_NUM_PATTERN.matcher(clean);
            if (mAn.find()) {
               try {
                  srvNum = Integer.parseInt(mAn.group(1));
               } catch (Exception var27) {
               }
            }

            String server = "anarchy" + (srvNum > 0 ? srvNum : 101);
            int timeSec = 180;
            Matcher mMin = TIME_MIN_PATTERN.matcher(clean);
            if (mMin.find()) {
               try {
                  timeSec = Integer.parseInt(mMin.group(1)) * 60;
               } catch (Exception var26) {
               }
            } else {
               Matcher mSec = TIME_SEC_PATTERN.matcher(clean);
               if (mSec.find()) {
                  try {
                     timeSec = Integer.parseInt(mSec.group(1));
                  } catch (Exception var25) {
                  }
               }
            }

            String phase = "ACTIVE";
            if (lower.contains("появится") || lower.contains("через") || lower.contains("ожидание") || lower.contains("з'явиться")) {
               phase = "STARTING";
            } else if (lower.contains("упал") || lower.contains("падение") || lower.contains("падає")) {
               phase = "FALLING";
               timeSec = Math.min(timeSec, 60);
            } else if (lower.contains("открыт") || lower.contains("залутан") || lower.contains("відкрито")) {
               phase = "OPEN";
               timeSec = 45;
            }

            long now = System.currentTimeMillis();
            FunTimeEventsService.FunTimeEvent newEv = new FunTimeEventsService.FunTimeEvent(server, type, name, timeSec, phase, loot, x, y, z, hasCoords, now);
            events.removeIf(e -> e.type().equalsIgnoreCase(type) && e.getServerNumber() == newEv.getServerNumber());
            events.add(0, newEv);
            events.sort(Comparator.comparingInt(FunTimeEventsService.FunTimeEvent::getRemainingSeconds));
            lastUpdatedMs = now;
            lastStatus = Lang.pick("Чат-радар: оновлено " + name, "Чат-радар: обновлен " + name, "Chat radar: updated " + name);
            if (hasCoords) {
               ChatUtils.addChatMessage("§b[Lunarix §7• §bІвенти] §fВиявлено з чату: §e" + name + " §7[X: §f" + x + " §7Z: §f" + z + "§7] §e(" + server + ")");
            }
         }
      }
   }

   private static List<FunTimeEventsService.FunTimeEvent> fetchEventsFromApi(String urlStr, String token) throws Exception {
      URL url = URI.create(urlStr).toURL();
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(6000);
      conn.setReadTimeout(6000);
      conn.setRequestProperty("client-token", token);
      conn.setRequestProperty("User-Agent", "Lunarix-Client/1.0");
      conn.setRequestProperty("Accept", "application/json");
      int code = conn.getResponseCode();
      if (code != 200) {
         throw new RuntimeException("HTTP " + code + " от API FunTime");
      } else {
         String json = readStream(conn.getInputStream());
         JsonElement root = JsonParser.parseString(json);
         List<FunTimeEventsService.FunTimeEvent> list = new ArrayList<>();
         long now = System.currentTimeMillis();
         JsonArray arr = null;
         if (root.isJsonArray()) {
            arr = root.getAsJsonArray();
         } else if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();
            if (obj.has("events") && obj.get("events").isJsonArray()) {
               arr = obj.getAsJsonArray("events");
            } else if (obj.has("server")) {
               list.add(parseEventObj(obj, now));
               return list;
            }
         }

         if (arr != null) {
            for (JsonElement el : arr) {
               if (el.isJsonObject()) {
                  FunTimeEventsService.FunTimeEvent ev = parseEventObj(el.getAsJsonObject(), now);
                  if (ev != null) {
                     list.add(ev);
                  }
               }
            }
         }

         return list;
      }
   }

   private static FunTimeEventsService.FunTimeEvent parseEventObj(JsonObject obj, long now) {
      try {
         String server = obj.has("server") ? obj.get("server").getAsString() : "anarchy";
         String type = obj.has("type") ? obj.get("type").getAsString() : "EVENT";
         String name = obj.has("name") ? obj.get("name").getAsString() : type;
         int timeLeft = obj.has("timeLeftSeconds") ? obj.get("timeLeftSeconds").getAsInt() : 0;
         String phase = obj.has("phase") ? obj.get("phase").getAsString() : "ACTIVE";
         String loot = obj.has("loot") ? obj.get("loot").getAsString() : "";
         int x = 0;
         int y = 64;
         int z = 0;
         boolean hasCoords = false;
         if (obj.has("coordinates") && obj.get("coordinates").isJsonObject()) {
            JsonObject c = obj.getAsJsonObject("coordinates");
            x = c.has("x") ? c.get("x").getAsInt() : 0;
            y = c.has("y") ? c.get("y").getAsInt() : 64;
            z = c.has("z") ? c.get("z").getAsInt() : 0;
            hasCoords = true;
         } else if (obj.has("x") && obj.has("z")) {
            x = obj.get("x").getAsInt();
            y = obj.has("y") ? obj.get("y").getAsInt() : 64;
            z = obj.get("z").getAsInt();
            hasCoords = true;
         }

         return new FunTimeEventsService.FunTimeEvent(server, type, name, timeLeft, phase, loot, x, y, z, hasCoords, now);
      } catch (Exception var14) {
         return null;
      }
   }

   private static List<FunTimeEventsService.FunTimeMine> fetchMinesFromApi(String urlStr, String token) throws Exception {
      URL url = URI.create(urlStr).toURL();
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(6000);
      conn.setReadTimeout(6000);
      conn.setRequestProperty("client-token", token);
      conn.setRequestProperty("User-Agent", "Lunarix-Client/1.0");
      conn.setRequestProperty("Accept", "application/json");
      if (conn.getResponseCode() != 200) {
         return null;
      } else {
         String json = readStream(conn.getInputStream());
         JsonElement root = JsonParser.parseString(json);
         List<FunTimeEventsService.FunTimeMine> list = new ArrayList<>();
         long now = System.currentTimeMillis();
         if (root.isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray()) {
               if (el.isJsonObject()) {
                  JsonObject o = el.getAsJsonObject();
                  String srv = o.has("server") ? o.get("server").getAsString() : "";
                  String name = o.has("name") ? o.get("name").getAsString() : Lang.pick("Шахта", "Шахта", "Mine");
                  String rar = o.has("rarity") ? o.get("rarity").getAsString() : "Обычная";
                  String nextRar = o.has("nextRarity") ? o.get("nextRarity").getAsString() : "";
                  int rst = o.has("resetInSeconds") ? o.get("resetInSeconds").getAsInt() : 0;
                  list.add(new FunTimeEventsService.FunTimeMine(srv, name, rar, nextRar, rst, now));
               }
            }
         }

         return list;
      }
   }

   private static String readStream(InputStream is) throws Exception {
      String var4;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
         StringBuilder sb = new StringBuilder();

         String line;
         while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
         }

         var4 = sb.toString();
      }

      return var4;
   }

   private static void checkDemoCycle() {
      long now = System.currentTimeMillis();
      boolean changed = false;
      ThreadLocalRandom rnd = ThreadLocalRandom.current();

      for (int i = 0; i < events.size(); i++) {
         FunTimeEventsService.FunTimeEvent ev = events.get(i);
         if (ev.getRemainingSeconds() <= 0) {
            changed = true;
            String nextPhase;
            int nextTime;
            if ("STARTING".equals(ev.phase)) {
               nextPhase = "ACTIVE";
               nextTime = 180 + rnd.nextInt(240);
            } else {
               if (!"ACTIVE".equals(ev.phase)) {
                  nextPhase = "STARTING";
                  nextTime = 120 + rnd.nextInt(600);
                  int curAnarchy = ServerUtil.getAnarchy() > 0 ? ServerUtil.getAnarchy() : 101;
                  int anNum = rnd.nextBoolean() && curAnarchy > 0 ? curAnarchy : 101 + rnd.nextInt(12);
                  int rx = rnd.nextInt(3000) - 1500;
                  int rz = rnd.nextInt(3000) - 1500;
                  events.set(i, new FunTimeEventsService.FunTimeEvent("anarchy" + anNum, ev.type, ev.name, nextTime, nextPhase, ev.loot, rx, 68, rz, true, now));
                  continue;
               }

               nextPhase = "LOOT";
               nextTime = 45 + rnd.nextInt(40);
            }

            events.set(
               i, new FunTimeEventsService.FunTimeEvent(ev.server, ev.type, ev.name, nextTime, nextPhase, ev.loot, ev.x, ev.y, ev.z, ev.hasCoordinates, now)
            );
         }
      }

      for (int ix = 0; ix < mines.size(); ix++) {
         FunTimeEventsService.FunTimeMine m = mines.get(ix);
         if (m.getRemainingSeconds() <= 0) {
            String nextRar = m.nextRarity();
            String newNext = rnd.nextBoolean() ? "Мифическая" : (rnd.nextBoolean() ? "Редкая" : "Донатная");
            mines.set(ix, new FunTimeEventsService.FunTimeMine(m.server, m.name, nextRar, newNext, 180 + rnd.nextInt(360), now));
         }
      }

      if (changed) {
         events.sort(Comparator.comparingInt(FunTimeEventsService.FunTimeEvent::getRemainingSeconds));
      }
   }

   private static void loadDemoEvents() {
      long now = System.currentTimeMillis();
      events.clear();
      int curAnarchy = ServerUtil.getAnarchy() > 0 ? ServerUtil.getAnarchy() : 101;
      events.add(
         new FunTimeEventsService.FunTimeEvent(
            "anarchy" + curAnarchy,
            "DEATH_CHEST",
            Lang.pick("Скриня смерті", "Сундук смерти", "Death Chest"),
            115,
            "STARTING",
            Lang.pick("Сфери, Шар стійкості, Талісмани", "Сферы, Шар стойкости, Талисманы", "Spheres, Resilience Orbs, Talismans"),
            1250,
            68,
            -450,
            true,
            now
         )
      );
      events.add(
         new FunTimeEventsService.FunTimeEvent(
            "anarchy" + curAnarchy,
            "VOLCANO",
            Lang.pick("Вулкан", "Вулкан", "Volcano"),
            340,
            "ACTIVE",
            Lang.pick("Діамантові блоки, Незерит, Містичний лут", "Алмазные блоки, Незерит, Мистический лут", "Diamond Blocks, Netherite, Mystic Loot"),
            -820,
            72,
            1430,
            true,
            now
         )
      );
      events.add(
         new FunTimeEventsService.FunTimeEvent(
            "anarchy" + (curAnarchy == 101 ? 102 : 101),
            "MYSTIC",
            Lang.pick("Містична скриня", "Мистический сундук", "Mystic Chest"),
            185,
            "STARTING",
            Lang.pick("Донат зілля, Тотеми, Броня бога", "Донат зелья, Тотемы, Броня бога", "Donate Potions, Totems, God Armor"),
            340,
            65,
            890,
            true,
            now
         )
      );
      events.add(
         new FunTimeEventsService.FunTimeEvent(
            "anarchy105",
            "BEACON",
            Lang.pick("Маяк-вбивця", "Маяк-убийца", "Killer Beacon"),
            520,
            "ACTIVE",
            Lang.pick("Маяки, Золоті яблука, Крони", "Маяки, Золотые яблоки, Кроны", "Beacons, Golden Apples, Crowns"),
            -120,
            69,
            -740,
            true,
            now
         )
      );
      events.add(
         new FunTimeEventsService.FunTimeEvent(
            "anarchy107",
            "DRAGON",
            Lang.pick("Дракон Краю", "Дракон Края", "Ender Dragon"),
            980,
            "WAITING",
            Lang.pick("Яйце дракона, Незеритова броня 5/3", "Яйцо дракона, Незеритовая броня 5/3", "Dragon Egg, Netherite Armor 5/3"),
            0,
            65,
            0,
            true,
            now
         )
      );
   }

   private static void loadDemoMines() {
      long now = System.currentTimeMillis();
      mines.clear();
      int curAnarchy = ServerUtil.getAnarchy() > 0 ? ServerUtil.getAnarchy() : 101;
      mines.add(
         new FunTimeEventsService.FunTimeMine(
            "anarchy" + curAnarchy, Lang.pick("Авто-шахта #1", "Авто-шахта #1", "Auto Mine #1"), "Редкая", "Мифическая", 145, now
         )
      );
      mines.add(
         new FunTimeEventsService.FunTimeMine("anarchy" + curAnarchy, Lang.pick("Авто-шахта #2", "Авто-шахта #2", "Auto Mine #2"), "Обычная", "Редкая", 75, now)
      );
      mines.add(
         new FunTimeEventsService.FunTimeMine(
            "anarchy" + (curAnarchy == 101 ? 102 : 101), Lang.pick("Авто-шахта VIP", "Авто-шахта VIP", "Auto Mine VIP"), "Мифическая", "Донатная", 420, now
         )
      );
      mines.add(new FunTimeEventsService.FunTimeMine("anarchy106", Lang.pick("Авто-шахта #3", "Авто-шахта #3", "Auto Mine #3"), "Донатная", "Обычная", 30, now));
   }

   private static void loadToken() {
      try {
         if (Files.exists(LOCAL_TOKEN_FILE)) {
            clientToken = Files.readString(LOCAL_TOKEN_FILE, StandardCharsets.UTF_8).trim();
            return;
         }

         if (Files.exists(FALLBACK_TOKEN_FILE)) {
            clientToken = Files.readString(FALLBACK_TOKEN_FILE, StandardCharsets.UTF_8).trim();
         }
      } catch (Exception var1) {
      }
   }

   private static void saveToken() {
      try {
         if (LOCAL_TOKEN_FILE.getParent() != null) {
            Files.createDirectories(LOCAL_TOKEN_FILE.getParent());
         }

         Files.writeString(LOCAL_TOKEN_FILE, clientToken != null ? clientToken : "", StandardCharsets.UTF_8);
      } catch (Exception var1) {
      }
   }

   static {
      loadToken();
      if (clientToken.isBlank()) {
         loadDemoEvents();
         loadDemoMines();
      } else {
         refresh(null);
      }
   }

   public static record FunTimeEvent(
      String server, String type, String name, int timeLeftSeconds, String phase, String loot, int x, int y, int z, boolean hasCoordinates, long fetchedAtMs
   ) {
      public int getRemainingSeconds() {
         long elapsed = (System.currentTimeMillis() - this.fetchedAtMs) / 1000L;
         return Math.max(0, this.timeLeftSeconds - (int)elapsed);
      }

      public String getTimerString() {
         int rem = this.getRemainingSeconds();
         return String.format(Locale.ROOT, "%d:%02d", rem / 60, rem % 60);
      }

      public String getFormattedServer() {
         if (this.server != null && !this.server.isBlank()) {
            String clean = this.server.toLowerCase(Locale.ROOT).replace("anarchy", "").replace("an", "").trim();
            return !clean.isEmpty() ? Lang.pick("Анархія " + clean, "Анархия " + clean, "Anarchy " + clean) : this.server;
         } else {
            return Lang.pick("Анархія", "Анархия", "Anarchy");
         }
      }

      public int getServerNumber() {
         try {
            String clean = this.server.replaceAll("\\D+", "");
            if (!clean.isEmpty()) {
               return Integer.parseInt(clean);
            }
         } catch (Exception var2) {
         }

         return -1;
      }

      public boolean isCurrentServer() {
         int currentAnarchy = ServerUtil.getAnarchy();
         return currentAnarchy > 0 && currentAnarchy == this.getServerNumber();
      }

      public int getAccentColor() {
         String t = (this.type != null ? this.type : "").toUpperCase(Locale.ROOT);
         if (t.contains("DEATH") || t.contains("СМЕРТ")) {
            return -1754827;
         } else if (t.contains("VOLCANO") || t.contains("ВУЛКАН")) {
            return -37632;
         } else if (t.contains("MYSTIC") || t.contains("МІСТИК") || t.contains("МИСТИК")) {
            return -10752;
         } else if (t.contains("BEACON") || t.contains("МАЯК")) {
            return -16718337;
         } else {
            return !t.contains("DRAGON") && !t.contains("ДРАКОН") ? ColorUtil.client() : -5635841;
         }
      }

      public String getPhaseLocalized() {
         if (this.phase != null && !this.phase.isBlank()) {
            String p = this.phase.toUpperCase(Locale.ROOT);
            if (p.contains("START") || p.contains("WAIT") || p.contains("SPAWN")) {
               return Lang.pick("Очікування", "Ожидание", "Waiting");
            } else if (p.contains("FALL") || p.contains("DROP")) {
               return Lang.pick("Падіння", "Падение", "Falling");
            } else if (p.contains("ACTIVE") || p.contains("RUN")) {
               return Lang.pick("Активний", "Активен", "Active");
            } else if (p.contains("LOOT") || p.contains("OPEN")) {
               return Lang.pick("Відкрито", "Открыт", "Open");
            } else {
               return !p.contains("END") && !p.contains("OVER") ? this.phase : Lang.pick("Завершено", "Завершён", "Ended");
            }
         } else {
            return Lang.pick("Активний", "Активен", "Active");
         }
      }

      public String getDisplayName() {
         String t = (this.type != null ? this.type : "").toUpperCase(Locale.ROOT);
         if (t.contains("DEATH") || t.contains("СМЕРТ")) {
            return Lang.pick("Скриня смерті", "Сундук смерти", "Death Chest");
         } else if (t.contains("VOLCANO") || t.contains("ВУЛКАН")) {
            return Lang.pick("Вулкан", "Вулкан", "Volcano");
         } else if (t.contains("MYSTIC") || t.contains("МІСТИК") || t.contains("МИСТИК")) {
            return Lang.pick("Містична скриня", "Мистический сундук", "Mystic Chest");
         } else if (t.contains("BEACON") || t.contains("МАЯК")) {
            return Lang.pick("Маяк-вбивця", "Маяк-убийца", "Killer Beacon");
         } else if (!t.contains("DRAGON") && !t.contains("ДРАКОН")) {
            return this.name != null && !this.name.isBlank() ? this.name : t;
         } else {
            return Lang.pick("Дракон Краю", "Дракон Края", "Ender Dragon");
         }
      }
   }

   public static record FunTimeMine(String server, String name, String rarity, String nextRarity, int resetInSeconds, long fetchedAtMs) {
      public int getRemainingSeconds() {
         long elapsed = (System.currentTimeMillis() - this.fetchedAtMs) / 1000L;
         return Math.max(0, this.resetInSeconds - (int)elapsed);
      }

      public String getTimerString() {
         int rem = this.getRemainingSeconds();
         return String.format(Locale.ROOT, "%d:%02d", rem / 60, rem % 60);
      }

      public String getFormattedServer() {
         if (this.server != null && !this.server.isBlank()) {
            String clean = this.server.toLowerCase(Locale.ROOT).replace("anarchy", "").replace("an", "").trim();
            return !clean.isEmpty() ? Lang.pick("Анархія " + clean, "Анархия " + clean, "Anarchy " + clean) : this.server;
         } else {
            return Lang.pick("Анархія", "Анархия", "Anarchy");
         }
      }

      public int getServerNumber() {
         try {
            String clean = this.server.replaceAll("\\D+", "");
            if (!clean.isEmpty()) {
               return Integer.parseInt(clean);
            }
         } catch (Exception var2) {
         }

         return -1;
      }

      public boolean isCurrentServer() {
         int cur = ServerUtil.getAnarchy();
         return cur > 0 && cur == this.getServerNumber();
      }

      public int getRarityColor() {
         String r = (this.rarity != null ? this.rarity : "").toLowerCase(Locale.ROOT);
         if (r.contains("мифич") || r.contains("міфіч") || r.contains("mythic")) {
            return -5635841;
         } else if (r.contains("редк") || r.contains("рідк") || r.contains("rare")) {
            return -16723201;
         } else {
            return !r.contains("донат") && !r.contains("donate") && !r.contains("legend") ? -5592406 : -10752;
         }
      }

      public String getRarityLocalized() {
         String r = (this.rarity != null ? this.rarity : "").toLowerCase(Locale.ROOT);
         if (r.contains("мифич") || r.contains("міфіч") || r.contains("mythic")) {
            return Lang.pick("Міфічна", "Мифическая", "Mythic");
         } else if (r.contains("редк") || r.contains("рідк") || r.contains("rare")) {
            return Lang.pick("Рідкісна", "Редкая", "Rare");
         } else if (r.contains("донат") || r.contains("donate") || r.contains("legend")) {
            return Lang.pick("Донатна", "Донатная", "Donate");
         } else {
            return !r.contains("обычн") && !r.contains("звичайн") && !r.contains("common") ? this.rarity : Lang.pick("Звичайна", "Обычная", "Common");
         }
      }

      public String getNextRarityLocalized() {
         if (this.nextRarity != null && !this.nextRarity.isBlank()) {
            String r = this.nextRarity.toLowerCase(Locale.ROOT);
            if (r.contains("мифич") || r.contains("міфіч") || r.contains("mythic")) {
               return Lang.pick("Міфічна", "Мифическая", "Mythic");
            } else if (r.contains("редк") || r.contains("рідк") || r.contains("rare")) {
               return Lang.pick("Рідкісна", "Редкая", "Rare");
            } else if (r.contains("донат") || r.contains("donate") || r.contains("legend")) {
               return Lang.pick("Донатна", "Донатная", "Donate");
            } else {
               return !r.contains("обычн") && !r.contains("звичайн") && !r.contains("common") ? this.nextRarity : Lang.pick("Звичайна", "Обычная", "Common");
            }
         } else {
            return "";
         }
      }
   }
}
