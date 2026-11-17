package ru.mentions.manager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GuiManager {
   public static final File file = new File("C:/lunarix/client1_21_11/config", "theme/theme.json");
   private Theme currentTheme = Theme.VIOLKA;

   public void init() {
      try {
         File parent = file.getParentFile();
         if (parent != null && !parent.exists()) {
            parent.mkdirs();
         }

         if (!file.exists()) {
            file.createNewFile();
            this.saveSettings();
         } else {
            this.readSettings();
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   public void setGuiTheme(Theme theme) {
      this.currentTheme = theme;
      this.saveSettings();
   }

   public Theme getCurrentTheme() {
      return this.currentTheme;
   }

   private void saveSettings() {
      try (FileWriter writer = new FileWriter(file)) {
         Properties props = new Properties();
         props.setProperty("theme", this.currentTheme.name());
         props.store(writer, "GUI Settings");
      } catch (IOException var6) {
         var6.printStackTrace();
      }
   }

   private void readSettings() {
      try (FileReader reader = new FileReader(file)) {
         Properties props = new Properties();
         props.load(reader);
         this.currentTheme = Theme.valueOf(props.getProperty("theme", Theme.VIOLKA.name()));
      } catch (IllegalArgumentException | IOException var6) {
         var6.printStackTrace();
      }
   }
}
