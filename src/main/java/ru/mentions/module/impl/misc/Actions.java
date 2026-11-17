package ru.mentions.module.impl.misc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_327;
import net.minecraft.class_332;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;

@ModuleInfo(
   name = "Actions",
   category = Category.OTHER,
   desc = "Синхронизация точек и меток через Supabase с друзьями на текущем сервере"
)
@Environment(EnvType.CLIENT)
public class Actions extends Module {
   private static final String SUPABASE_URL = "https://bgbglvqfrfswopxukdga.supabase.co/rest/v1/actions_points";
   private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnYmdsdnFmcmZzd29weHVrZGdhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTQ2MDEzNjQsImV4cCI6MjExMDE3NzM2NH0.rbIiDPtOzyYtt8ytX2BGJJOX8-Ds1Yy-tEMTvF9SsEk";
   
   public BindSetting pointBind = new BindSetting(this, "Бинд точки", 71); // 'G' key
   public BooleanSetting showDistance = new BooleanSetting(this, "Показывать дистанцию", true);
   public SliderSetting syncInterval = new SliderSetting(this, "Интервал обновления (сек)", 3.0F, 1.0F, 10.0F, 1.0F);

   public static class ActionPoint {
      public String user;
      public String server;
      public double x, y, z;
      public long timestamp;

      public ActionPoint(String user, String server, double x, double y, double z, long timestamp) {
         this.user = user;
         this.server = server;
         this.x = x;
         this.y = y;
         this.z = z;
         this.timestamp = timestamp;
      }
   }

   private final Map<String, ActionPoint> points = new ConcurrentHashMap<>();
   private ScheduledExecutorService scheduler;
   private final Gson gson = new Gson();

   @Override
   public void onEnable() {
      this.points.clear();
      this.scheduler = Executors.newSingleThreadScheduledExecutor();
      this.scheduler.scheduleAtFixedRate(this::fetchPoints, 0L, (long)this.syncInterval.getValue().intValue(), TimeUnit.SECONDS);
   }

   @Override
   public void onDisable() {
      if (this.scheduler != null && !this.scheduler.isShutdown()) {
         this.scheduler.shutdownNow();
      }
      this.points.clear();
   }

   private String getCurrentServer() {
      if (mc.method_1562() != null && mc.method_1562().method_45734() != null) {
         return mc.method_1562().method_45734().field_3761;
      }
      return "singleplayer";
   }

   @EventHandler
   public void onKey(EventKey event) {
      if (this.pointBind.getValue() != null && event.getKey() == this.pointBind.getValue() && mc.field_1724 != null) {
         double x = mc.field_1724.method_23317();
         double y = mc.field_1724.method_23318();
         double z = mc.field_1724.method_23321();
         String user = mc.field_1724.method_7334().name();
         String server = this.getCurrentServer();
         
         this.points.put(user, new ActionPoint(user, server, x, y, z, System.currentTimeMillis()));
         Executors.newSingleThreadExecutor().submit(() -> this.sendPoint(user, server, x, y, z));
      }
   }

   private void sendPoint(String user, String server, double x, double y, double z) {
      try {
         URL url = URI.create(SUPABASE_URL).toURL();
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("POST");
         conn.setRequestProperty("apikey", SUPABASE_KEY);
         conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
         conn.setRequestProperty("Content-Type", "application/json");
         conn.setRequestProperty("Prefer", "resolution=merge-duplicates");
         conn.setDoOutput(true);

         JsonObject obj = new JsonObject();
         obj.addProperty("username", user);
         obj.addProperty("server_ip", server);
         obj.addProperty("pos_x", x);
         obj.addProperty("pos_y", y);
         obj.addProperty("pos_z", z);
         obj.addProperty("timestamp", System.currentTimeMillis());

         byte[] out = obj.toString().getBytes(StandardCharsets.UTF_8);
         try (OutputStream os = conn.getOutputStream()) {
            os.write(out);
         }
         conn.getResponseCode();
      } catch (Exception ignored) {}
   }

   private void fetchPoints() {
      try {
         String currentServer = this.getCurrentServer();
         String queryUrl = SUPABASE_URL + "?server_ip=eq." + currentServer + "&select=*";
         URL url = URI.create(queryUrl).toURL();
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("apikey", SUPABASE_KEY);
         conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

         if (conn.getResponseCode() == 200) {
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
               JsonArray arr = gson.fromJson(reader, JsonArray.class);
               long now = System.currentTimeMillis();
               for (int i = 0; i < arr.size(); i++) {
                  JsonObject o = arr.get(i).getAsJsonObject();
                  String u = o.get("username").getAsString();
                  String s = o.get("server_ip").getAsString();
                  double px = o.get("pos_x").getAsDouble();
                  double py = o.get("pos_y").getAsDouble();
                  double pz = o.get("pos_z").getAsDouble();
                  long t = o.has("timestamp") ? o.get("timestamp").getAsLong() : now;
                  if (now - t < 120000L) { // active within 2 minutes
                     this.points.put(u, new ActionPoint(u, s, px, py, pz, t));
                  }
               }
            }
         }
      } catch (Exception ignored) {}
   }

   @EventHandler
   public void onDisplay(EventDisplay event) {
      if (mc.field_1724 == null || this.points.isEmpty()) return;
      
      class_332 ctx = event.getDrawContext();
      class_327 textRenderer = mc.field_1772;
      String currentServer = this.getCurrentServer();
      
      int yOffset = 70;
      for (ActionPoint p : this.points.values()) {
         if (!p.server.equals(currentServer)) continue;
         
         double dist = mc.field_1724.method_5649(p.x, p.y, p.z);
         double distanceMeters = Math.sqrt(dist);
         String text = "Point: " + p.user;
         if (this.showDistance.getValue()) {
            text += " (" + String.format("%.1fm", distanceMeters) + ")";
         }
         
         ctx.method_51433(textRenderer, text, 10, yOffset, 0xFF55FFFF, true);
         yOffset += 12;
      }
   }
}
