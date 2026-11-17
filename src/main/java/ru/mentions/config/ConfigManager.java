package ru.mentions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BlockListSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.BooleanSettingHud;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.DragSetting;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.module.api.settings.impl.ModeSettingHud;
import ru.mentions.module.api.settings.impl.MultiBooleanSetting;
import ru.mentions.module.api.settings.impl.PixelGridSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.module.api.settings.impl.StringSetting;
import ru.mentions.module.impl.utils.UnHook;
import ru.mentions.screen.MainMenuScreen;
import ru.mentions.screen.Menu;
import ru.mentions.screen.Menu2;

@Environment(EnvType.CLIENT)
public class ConfigManager {
   public static final String AUTO_CONFIG = "auto";
   private static final Path CONFIG_DIR = Path.of("C:/lunarix/client1_21_11/config");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private int tickTimer = 0;

   public void setup() {
      try {
         Files.createDirectories(CONFIG_DIR);
      } catch (IOException var2) {
         var2.printStackTrace();
      }

      Client.eventHandler().subscribe(this);
   }

   public void init() {
      this.load("auto");
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (++this.tickTimer >= 1200) {
         this.tickTimer = 0;
         if (!UnHook.unhooked) {
            this.save("auto");
         }
      }
   }

   public void autoSave() {
      if (!UnHook.unhooked) {
         this.save("auto");
      }
   }

   public void save(String name) {
      try {
         this.writeConfig(name, true, false);
      } catch (RuntimeException | IOException var3) {
         var3.printStackTrace();
      }
   }

   public void saveChecked(String name, boolean overwrite) throws IOException {
      this.writeConfig(name, overwrite, overwrite);
   }

   private JsonObject snapshot() {
      JsonObject root = new JsonObject();
      JsonObject modules = new JsonObject();

      for (Module module : Client.get().moduleManager().values()) {
         JsonObject moduleObj = new JsonObject();
         moduleObj.addProperty("enabled", module.isEnabled());
         moduleObj.addProperty("key", module.getKey());
         JsonObject settings = new JsonObject();

         for (Setting<?> setting : module.getSettings()) {
            this.serializeSetting(settings, setting);
         }

         moduleObj.add("settings", settings);
         modules.add(module.getName(), moduleObj);
      }

      root.add("modules", modules);
      root.addProperty("menuBg", MainMenuScreen.getBgSaved());
      JsonObject guiState = new JsonObject();
      guiState.addProperty("offsetX", Menu.guiOffsetX);
      guiState.addProperty("offsetY", Menu.guiOffsetY);
      guiState.addProperty("extraW", Menu.extraW);
      guiState.addProperty("extraH", Menu.extraH);
      guiState.addProperty("setExtraW", Menu.setExtraW);
      guiState.addProperty("setExtraH", Menu.setExtraH);
      guiState.addProperty("extraW2", Menu2.extraW2);
      guiState.addProperty("extraH2", Menu2.extraH2);
      root.add("guiState", guiState);
      return root;
   }

   private void writeConfig(String name, boolean overwrite, boolean requireExisting) throws IOException {
      Path file = this.configFile(name);
      Files.createDirectories(file.getParent());
      if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
         this.requireRegularConfig(file);
         if (!overwrite) {
            throw new FileAlreadyExistsException("Name already exists. Select it and use Overwrite.");
         }
      } else if (requireExisting) {
         throw new NoSuchFileException("Config no longer exists. Refresh the list.");
      }

      String json = GSON.toJson(this.snapshot());
      Path temp = Files.createTempFile(file.getParent(), ".config-", ".tmp");

      try {
         Files.writeString(temp, json, StandardCharsets.UTF_8);
         if (overwrite) {
            if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
               this.requireRegularConfig(file);
            }

            try {
               Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException var11) {
               Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
         } else {
            Files.move(temp, file);
         }
      } finally {
         Files.deleteIfExists(temp);
      }
   }

   public String exportChecked(String name) throws IOException {
      Path file = this.configFile(name);
      this.requireRegularConfig(file);
      return Files.readString(file, StandardCharsets.UTF_8);
   }

   public void importChecked(String name, String json) throws IOException {
      JsonObject root;
      try {
         root = JsonParser.parseString(json).getAsJsonObject();
      } catch (RuntimeException var10) {
         throw new IOException("Import failed: not a JSON object.", var10);
      }

      if (root != null && root.has("modules") && root.get("modules").isJsonObject()) {
         Path file = this.configFile(name);
         Files.createDirectories(file.getParent());
         if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new FileAlreadyExistsException("A config with that name already exists.");
         } else {
            Path temp = Files.createTempFile(file.getParent(), ".config-", ".tmp");

            try {
               Files.writeString(temp, GSON.toJson(root), StandardCharsets.UTF_8);
               Files.move(temp, file);
            } finally {
               Files.deleteIfExists(temp);
            }
         }
      } else {
         throw new IOException("Import failed: missing modules object.");
      }
   }

   public static boolean isReservedSystemFile(String name) {
      if (name == null) {
         return true;
      } else {
         String lower = name.toLowerCase(Locale.ROOT);
         return lower.equals("cosmetics") || lower.equals("draggables") || lower.equals("proxy") || lower.equals("theme");
      }
   }

   public boolean load(String name) {
      if (isReservedSystemFile(name)) {
         return false;
      } else {
         try {
            this.loadChecked(name);
            return true;
         } catch (RuntimeException | IOException var3) {
            return false;
         }
      }
   }

   public void loadChecked(String name) throws IOException {
      Path file = this.configFile(name);
      this.requireRegularConfig(file);

      try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
         JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
         JsonObject modules = root.getAsJsonObject("modules");
         if (modules == null) {
            throw new IOException("Not a module config: missing modules object.");
         }

         for (Module module : Client.get().moduleManager().values()) {
            if (modules.has(module.getName())) {
               JsonObject moduleObj = modules.getAsJsonObject(module.getName());
               if (moduleObj.has("key")) {
                  module.setKey(moduleObj.get("key").getAsInt());
               }

               if (moduleObj.has("settings")) {
                  JsonObject settings = moduleObj.getAsJsonObject("settings");

                  for (Setting<?> setting : module.getSettings()) {
                     this.deserializeSetting(settings, setting);
                  }
               }
            }
         }

         if (root.has("menuBg")) {
            MainMenuScreen.setBgSaved(root.get("menuBg").getAsInt());
         }

         if (root.has("guiState")) {
            JsonObject guiState = root.getAsJsonObject("guiState");
            if (guiState.has("offsetX")) {
               Menu.guiOffsetX = guiState.get("offsetX").getAsFloat();
            }

            if (guiState.has("offsetY")) {
               Menu.guiOffsetY = guiState.get("offsetY").getAsFloat();
            }

            if (guiState.has("extraW")) {
               Menu.extraW = guiState.get("extraW").getAsFloat();
            }

            if (guiState.has("extraH")) {
               Menu.extraH = guiState.get("extraH").getAsFloat();
            }

            if (guiState.has("setExtraW")) {
               Menu.setExtraW = guiState.get("setExtraW").getAsFloat();
            }

            if (guiState.has("setExtraH")) {
               Menu.setExtraH = guiState.get("setExtraH").getAsFloat();
            }

            if (guiState.has("extraW2")) {
               Menu2.extraW2 = guiState.get("extraW2").getAsFloat();
            }

            if (guiState.has("extraH2")) {
               Menu2.extraH2 = guiState.get("extraH2").getAsFloat();
            }
         }

         for (Module modulex : Client.get().moduleManager().values()) {
            if (modules.has(modulex.getName())) {
               JsonObject moduleObjx = modules.getAsJsonObject(modulex.getName());
               if (moduleObjx.has("enabled")) {
                  modulex.setEnabled(moduleObjx.get("enabled").getAsBoolean(), false);
               }
            }
         }
      }
   }

   public void deleteChecked(String name) throws IOException {
      Path file = this.configFile(name);
      this.requireRegularConfig(file);
      Files.delete(file);
   }

   public static String validateName(String name) {
      if (name == null) {
         throw new IllegalArgumentException("Enter a config name.");
      } else {
         String clean = name.trim();
         if (!clean.isEmpty()
            && clean.length() <= 64
            && clean.matches("[\\p{L}\\p{N}_ .-]+")
            && !clean.contains("..")
            && !clean.startsWith(".")
            && !clean.endsWith(".")
            && !clean.toLowerCase(Locale.ROOT).endsWith(".json")) {
            String device = clean.split("\\.", 2)[0].toUpperCase(Locale.ROOT);
            if (device.matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) {
               throw new IllegalArgumentException("That name is reserved by Windows.");
            } else {
               return clean;
            }
         } else {
            throw new IllegalArgumentException("Use 1-64 letters, numbers, spaces, _ or -; no paths or .json suffix.");
         }
      }
   }

   private Path configFile(String name) {
      Path directory = CONFIG_DIR.toAbsolutePath().normalize();
      Path file = directory.resolve(validateName(name) + ".json").normalize();
      if (!directory.equals(file.getParent())) {
         throw new IllegalArgumentException("Invalid config path.");
      } else {
         return file;
      }
   }

   private void requireRegularConfig(Path file) throws IOException {
      if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
         throw new IOException("Config is missing or is not a regular file (links are not allowed).");
      }
   }

   public Path getConfigDir() {
      return CONFIG_DIR;
   }

   public List<String> listConfigs() {
      try {
         return this.listConfigsChecked();
      } catch (RuntimeException | IOException var2) {
         var2.printStackTrace();
         return new ArrayList<>();
      }
   }

   public List<String> listConfigsChecked() throws IOException {
      Files.createDirectories(CONFIG_DIR);
      ArrayList<String> configs = new ArrayList<>();

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(CONFIG_DIR, "*.json")) {
         for (Path path : stream) {
            if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
               String fileName = path.getFileName().toString();
               String name = fileName.substring(0, fileName.length() - 5);
               if (!isReservedSystemFile(name)) {
                  try {
                     if (validateName(name).equals(name)) {
                        configs.add(name);
                     }
                  } catch (IllegalArgumentException var9) {
                  }
               }
            }
         }
      }

      configs.sort(String.CASE_INSENSITIVE_ORDER);
      return configs;
   }

   private void serializeSetting(JsonObject obj, Setting<?> setting) {
      if (setting instanceof DragSetting drag) {
         JsonObject vec = new JsonObject();
         vec.addProperty("x", drag.position.x);
         vec.addProperty("y", drag.position.y);
         obj.add(setting.getName(), vec);
      } else if (setting instanceof MultiBooleanSetting ms) {
         JsonObject msObj = new JsonObject();

         for (BooleanSetting val : ms.getValues()) {
            msObj.addProperty(val.getName().toLowerCase(), val.getValue());
         }

         obj.add(setting.getName(), msObj);
      } else if (setting instanceof BlockListSetting bl) {
         JsonArray arr = new JsonArray();

         for (String id : bl.getIds()) {
            arr.add(id);
         }

         obj.add(setting.getName(), arr);
      } else if (!(setting instanceof PixelGridSetting grid)) {
         Object value = setting.getValue();
         if (value instanceof Boolean b) {
            obj.addProperty(setting.getName(), b);
         } else if (value instanceof Float f) {
            obj.addProperty(setting.getName(), f);
         } else if (value instanceof String s) {
            obj.addProperty(setting.getName(), s);
         } else if (value instanceof Integer i) {
            obj.addProperty(setting.getName(), i);
         }
      } else {
         JsonArray arr = new JsonArray();

         for (String row : grid.toRows()) {
            arr.add(row);
         }

         obj.add(setting.getName(), arr);
      }
   }

   private void deserializeSetting(JsonObject obj, Setting<?> setting) {
      if (obj.has(setting.getName())) {
         JsonElement el = obj.get(setting.getName());
         if (setting instanceof DragSetting drag && el.isJsonObject()) {
            JsonObject vec = el.getAsJsonObject();
            drag.position.set(vec.get("x").getAsFloat(), vec.get("y").getAsFloat());
            drag.targetPosition.set(drag.position);
            drag.animationX.set((double)drag.position.x);
            drag.animationY.set((double)drag.position.y);
            return;
         }

         if (setting instanceof MultiBooleanSetting ms && el.isJsonObject()) {
            JsonObject msObj = el.getAsJsonObject();

            for (BooleanSetting val : ms.getValues()) {
               String key = val.getName().toLowerCase();
               if (msObj.has(key)) {
                  val.set(msObj.get(key).getAsBoolean());
               }
            }

            return;
         }

         if (setting instanceof BlockListSetting bl && el.isJsonArray()) {
            bl.getIds().clear();

            for (JsonElement e : el.getAsJsonArray()) {
               String id = e.getAsString();
               if (!bl.getIds().contains(id)) {
                  bl.getIds().add(id);
               }
            }

            return;
         }

         if (setting instanceof PixelGridSetting grid && el.isJsonArray()) {
            ArrayList<String> rows = new ArrayList<>();

            for (JsonElement ex : el.getAsJsonArray()) {
               rows.add(ex.getAsString());
            }

            grid.fromRows(rows);
            return;
         }

         if (setting instanceof BooleanSetting bs) {
            bs.set(el.getAsBoolean());
         } else if (setting instanceof BooleanSettingHud bs) {
            bs.set(el.getAsBoolean());
         } else if (setting instanceof SliderSetting ss) {
            ss.set(el.getAsFloat());
         } else if (setting instanceof ModeSetting ms) {
            ms.set(el.getAsString());
         } else if (setting instanceof ModeSettingHud ms) {
            ms.set(el.getAsString());
         } else if (setting instanceof BindSetting bind) {
            bind.set(el.getAsInt());
         } else if (setting instanceof ColorSetting cs) {
            cs.set(el.getAsInt());
         } else if (setting instanceof StringSetting str) {
            str.set(el.getAsString());
         }
      }
   }
}
