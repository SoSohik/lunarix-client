package ru.mentions.theme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public class ThemeManager {
   private static final Path THEME_DIR = Path.of("C:/lunarix/client1_21_11/theme");
   private static final Path ACTIVE_FILE = THEME_DIR.resolve(".active");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static ThemeManager instance;
   public static final int MAX_THEMES = 9;
   private final List<Theme> themes = new ArrayList<>();
   private Theme active;

   public static ThemeManager get() {
      if (instance == null) {
         instance = new ThemeManager();
         instance.load();
      }

      return instance;
   }

   public List<Theme> getThemes() {
      return this.themes;
   }

   public Theme getActive() {
      if (this.active == null) {
         Theme theme = this.active = this.themes.isEmpty() ? new Theme("Default") : this.themes.get(0);
         if (this.themes.isEmpty()) {
            this.themes.add(this.active);
         }
      }

      return this.active;
   }

   public void setActive(Theme theme) {
      if (theme != null) {
         this.active = theme;
         this.saveActiveName();
      }
   }

   public boolean canCreate() {
      return this.themes.size() < 9;
   }

   public Theme createTheme(String name) {
      if (!this.canCreate()) {
         return this.getActive();
      } else {
         name = this.uniqueName(name, null);
         Theme base = this.getActive();
         Theme created = base.copy(name);
         this.themes.add(created);
         this.setActive(created);
         this.saveTheme(created);
         return created;
      }
   }

   public void renameTheme(Theme theme, String newName) {
      if (theme != null && !theme.builtin) {
         newName = newName == null ? "" : newName.trim();
         if (!newName.isEmpty() && !newName.equalsIgnoreCase(theme.name)) {
            try {
               Files.deleteIfExists(THEME_DIR.resolve(theme.name + ".json"));
            } catch (IOException var5) {
            }

            theme.name = this.uniqueName(newName, theme);
            this.saveTheme(theme);
            if (this.active == theme) {
               this.saveActiveName();
            }
         }
      }
   }

   public void deleteTheme(Theme theme) {
      if (theme != null && !theme.builtin && this.themes.size() > 1) {
         this.themes.remove(theme);

         try {
            Files.deleteIfExists(THEME_DIR.resolve(theme.name + ".json"));
         } catch (IOException var3) {
         }

         if (this.active == theme) {
            this.active = this.themes.get(0);
            this.saveActiveName();
         }
      }
   }

   private static Theme builtin(String name, int visual, int hud) {
      Theme t = new Theme(name);
      t.builtin = true;
      t.colors[0] = visual;
      t.colors[5] = hud;
      t.colors[3] = Theme.rgb(255, 255, 255);
      t.colors[4] = Theme.rgb(177, 177, 177);
      t.colors[1] = Theme.rgb(15, 15, 15);
      t.colors[2] = ColorUtil.getColor(255, 255, 255, 15);
      t.colors[6] = ColorUtil.getColor(255, 255, 255, 15);
      t.colors[7] = ColorUtil.getColor(255, 255, 255, 10);
      t.setSlider(0, 0.9F);
      t.setSlider(1, 4.0F);
      t.setBool(0, true);
      t.setBool(1, false);
      return t;
   }

   public void load() {
      this.themes.clear();
      this.themes.add(builtin("Blue", Theme.rgb(124, 143, 246), Theme.rgb(143, 161, 250)));
      this.themes.add(builtin("Red", Theme.rgb(246, 124, 126), Theme.rgb(246, 124, 126)));

      try {
         Files.createDirectories(THEME_DIR);

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(THEME_DIR, "*.json")) {
            for (Path path : stream) {
               Theme t2 = this.readTheme(path);
               if (t2 != null && !this.nameExists(t2.name, null)) {
                  this.themes.add(t2);
               }
            }
         }
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      String activeName = this.readActiveName();
      this.active = this.themes.stream().filter(t -> t.name.equals(activeName)).findFirst().orElse(this.themes.get(0));
   }

   public void saveTheme(Theme theme) {
      if (theme != null && !theme.builtin) {
         try {
            Files.createDirectories(THEME_DIR);
            JsonObject root = new JsonObject();
            root.addProperty("name", theme.name);

            for (int i = 0; i < 8; i++) {
               root.addProperty(keyOf(i), theme.colors[i]);
            }

            for (int var11 = 0; var11 < theme.sliders.length; var11++) {
               root.addProperty(sliderKeyOf(var11), theme.sliders[var11]);
            }

            for (int var12 = 0; var12 < theme.booleans.length; var12++) {
               root.addProperty(boolKeyOf(var12), theme.booleans[var12]);
            }

            Path file = THEME_DIR.resolve(theme.name + ".json");

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8)) {
               GSON.toJson(root, writer);
            }
         } catch (IOException var10) {
            var10.printStackTrace();
         }
      }
   }

   public void saveActive() {
      this.saveTheme(this.getActive());
   }

   private Theme readTheme(Path path) {
      try {
         Theme var13;
         try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            String name = root.has("name") ? root.get("name").getAsString() : stripExt(path.getFileName().toString());
            Theme theme = new Theme(name);

            for (int i = 0; i < 8; i++) {
               String key = keyOf(i);
               if (root.has(key)) {
                  theme.colors[i] = root.get(key).getAsInt();
               }
            }

            for (int ix = 0; ix < theme.sliders.length; ix++) {
               String key = sliderKeyOf(ix);
               if (root.has(key)) {
                  theme.setSlider(ix, root.get(key).getAsFloat());
               }
            }

            for (int ixx = 0; ixx < theme.booleans.length; ixx++) {
               String key = boolKeyOf(ixx);
               if (root.has(key)) {
                  theme.setBool(ixx, root.get(key).getAsBoolean());
               }
            }

            var13 = theme;
         }

         return var13;
      } catch (RuntimeException | IOException var10) {
         var10.printStackTrace();
         return null;
      }
   }

   private void saveActiveName() {
      try {
         Files.createDirectories(THEME_DIR);
         Files.write(ACTIVE_FILE, this.getActive().name.getBytes(StandardCharsets.UTF_8));
      } catch (IOException var2) {
      }
   }

   private String readActiveName() {
      try {
         if (Files.exists(ACTIVE_FILE)) {
            return new String(Files.readAllBytes(ACTIVE_FILE), StandardCharsets.UTF_8).trim();
         }
      } catch (IOException var2) {
      }

      return null;
   }

   private String uniqueName(String base, Theme exclude) {
      if (base == null || base.isBlank()) {
         base = "Theme";
      }

      if ((base = base.replaceAll("[\\\\/:*?\"<>|]", "_").trim()).isEmpty()) {
         base = "Theme";
      }

      String name = base;
      int n = 1;

      while (this.nameExists(name, exclude)) {
         name = base + " " + ++n;
      }

      return name;
   }

   private boolean nameExists(String name, Theme exclude) {
      return this.themes.stream().anyMatch(t -> t != exclude && t.name.equalsIgnoreCase(name));
   }

   private static String keyOf(int i) {
      return switch (i) {
         case 0 -> "accent";
         case 1 -> "background";
         case 2 -> "outline";
         case 3 -> "text";
         case 4 -> "text2";
         case 5 -> "element";
         case 6 -> "separator";
         case 7 -> "lightbg";
         default -> "c" + i;
      };
   }

   private static String sliderKeyOf(int i) {
      return switch (i) {
         case 0 -> "opacity";
         case 1 -> "blur";
         default -> "s" + i;
      };
   }

   private static String boolKeyOf(int i) {
      return switch (i) {
         case 0 -> "shadow";
         case 1 -> "distortion";
         default -> "b" + i;
      };
   }

   private static String stripExt(String fileName) {
      int dot = fileName.lastIndexOf(46);
      return dot > 0 ? fileName.substring(0, dot) : fileName;
   }
}
