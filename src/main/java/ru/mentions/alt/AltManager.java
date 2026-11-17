package ru.mentions.alt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_320;
import ru.mentions.mixin.MinecraftClientAccessor;

@Environment(EnvType.CLIENT)
public class AltManager {
   private static final Path FILE = Path.of("C:/lunarix/client1_21_11/alts.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static AltManager instance;
   private final List<Account> accounts = new ArrayList<>();
   private String activeName = null;
   private boolean booted = false;

   public static AltManager get() {
      if (instance == null) {
         instance = new AltManager();
         instance.load();
      }

      return instance;
   }

   public List<Account> getAccounts() {
      return this.accounts;
   }

   public boolean exists(String name) {
      return this.accounts.stream().anyMatch(a -> a.name.equalsIgnoreCase(name));
   }

   public Account add(String name) {
      name = name == null ? "" : name.trim();
      if (!name.isEmpty() && !this.exists(name)) {
         Account account = new Account(name);
         this.accounts.add(account);
         this.save();
         return account;
      } else {
         return null;
      }
   }

   public String randomName() {
      return this.randomName("");
   }

   public String randomName(String base) {
      base = base == null ? "" : base.trim();
      StringBuilder cleaned = new StringBuilder();

      for (char c : base.toCharArray()) {
         if (Character.isLetterOrDigit(c) || c == '_') {
            cleaned.append(c);
         }
      }

      if (cleaned.length() > 12) {
         cleaned.setLength(12);
      }

      base = cleaned.toString();
      String pool = "abcdefghijklmnopqrstuvwxyz0123456789";
      Random r = new Random();

      for (int attempt = 0; attempt < 60; attempt++) {
         int maxLen = Math.max(1, Math.min(12, 16 - base.length()));
         int minLen = Math.min(4, maxLen);
         int n = minLen + r.nextInt(maxLen - minLen + 1);
         StringBuilder sb = new StringBuilder(base);

         for (int i = 0; i < n; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz0123456789".charAt(r.nextInt("abcdefghijklmnopqrstuvwxyz0123456789".length())));
         }

         String name = sb.toString();
         if (!name.isEmpty() && name.length() <= 16 && !this.exists(name)) {
            return name;
         }
      }

      return (base.isEmpty() ? "Player" : base) + r.nextInt(10000);
   }

   public void remove(Account account) {
      if (this.accounts.remove(account)) {
         this.save();
      }
   }

   public void toggleFavorite(Account account) {
      if (account != null) {
         account.favorite = !account.favorite;
         this.save();
      }
   }

   public List<Account> getSorted() {
      ArrayList<Account> sorted = new ArrayList<>();

      for (Account a : this.accounts) {
         if (a.favorite) {
            sorted.add(a);
         }
      }

      for (Account ax : this.accounts) {
         if (!ax.favorite) {
            sorted.add(ax);
         }
      }

      return sorted;
   }

   public void login(Account account) {
      if (account != null) {
         this.applySession(account);
         this.activeName = account.name;
         this.save();
      }
   }

   private void applySession(Account account) {
      class_310 mc = class_310.method_1551();
      if (mc != null) {
         class_320 session = new class_320(account.name, account.offlineUuid(), "0", Optional.empty(), Optional.empty());
         ((MinecraftClientAccessor)mc).setSession(session);
      }
   }

   public void bootstrap() {
      if (!this.booted) {
         class_310 mc = class_310.method_1551();
         if (mc != null && mc.method_1548() != null) {
            this.booted = true;
            if (this.activeName != null) {
               for (Account a : this.accounts) {
                  if (a.name.equalsIgnoreCase(this.activeName)) {
                     this.applySession(a);
                     return;
                  }
               }
            }
         }
      }
   }

   public String currentName() {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.method_1548() != null) {
         return mc.method_1548().method_1676();
      } else {
         return this.activeName != null ? this.activeName : "Player";
      }
   }

   public void save() {
      JsonObject root = new JsonObject();
      JsonArray arr = new JsonArray();

      for (Account a : this.accounts) {
         JsonObject o = new JsonObject();
         o.addProperty("name", a.name);
         o.addProperty("type", a.type.name());
         o.addProperty("created", a.created);
         o.addProperty("favorite", a.favorite);
         arr.add(o);
      }

      root.add("accounts", arr);
      if (this.activeName != null) {
         root.addProperty("active", this.activeName);
      }

      try {
         Files.createDirectories(FILE.getParent());

         try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
         }
      } catch (IOException var8) {
         var8.printStackTrace();
      }
   }

   public void load() {
      this.accounts.clear();
      if (Files.exists(FILE)) {
         try {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
               JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
               if (root.has("active")) {
                  this.activeName = root.get("active").getAsString();
               }

               if (root.has("accounts")) {
                  for (JsonElement el : root.getAsJsonArray("accounts")) {
                     JsonObject o = el.getAsJsonObject();
                     String name = o.has("name") ? o.get("name").getAsString() : null;
                     if (name != null && !name.isBlank()) {
                        Account.Type type = Account.Type.OFFLINE;
                        if (o.has("type")) {
                           try {
                              type = Account.Type.valueOf(o.get("type").getAsString());
                           } catch (IllegalArgumentException var12) {
                           }
                        }

                        long created = o.has("created") ? o.get("created").getAsLong() : System.currentTimeMillis();
                        Account acc = new Account(name, type, created);
                        if (o.has("favorite")) {
                           acc.favorite = o.get("favorite").getAsBoolean();
                        }

                        this.accounts.add(acc);
                     }
                  }

                  return;
               }
            }
         } catch (Exception var14) {
            var14.printStackTrace();
         }
      }
   }
}
