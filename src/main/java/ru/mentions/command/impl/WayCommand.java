package ru.mentions.command.impl;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.theme.ThemeColor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorFormatting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.other.Projection;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Font;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public class WayCommand extends Command implements IMinecraft {
   private static final Path FILE = Path.of("C:/lunarix/client1_21_11/waypoints.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Map<String, WayCommand.Waypoint> waypoints = new ConcurrentHashMap<>();

   public WayCommand() {
      super("way", ".way <add|delete|clear|list>", "Вейпоинты — метки на координатах");
      this.load();
      Client.eventHandler().subscribe(this);
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.showHelp();
      } else {
         String var2 = args[0].toLowerCase();
         switch (var2) {
            case "add":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("§7Использование: §f.way add <имя> [x y z]");
                  return;
               }

               String name = args[1];
               class_243 pos;
               if (args.length >= 5) {
                  try {
                     pos = new class_243(Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
                  } catch (NumberFormatException var7) {
                     ChatUtils.addChatMessage("§cОшибка: неверные координаты");
                     return;
                  }
               } else {
                  if (mc.field_1724 == null) {
                     ChatUtils.addChatMessage("§cВы не в мире, укажите координаты: §f.way add <имя> <x> <y> <z>");
                     return;
                  }

                  pos = mc.field_1724.method_73189();
               }

               boolean replaced = this.waypoints.put(name.toLowerCase(), new WayCommand.Waypoint(name, pos)) != null;
               this.save();
               ChatUtils.addChatMessage(
                  (replaced ? "§aВейпоинт обновлён: §f" : "§aВейпоинт добавлен: §f")
                     + name
                     + " §7("
                     + (int)pos.field_1352
                     + ", "
                     + (int)pos.field_1351
                     + ", "
                     + (int)pos.field_1350
                     + ")"
               );
               break;
            case "delete":
            case "del":
            case "remove":
               if (args.length < 2) {
                  ChatUtils.addChatMessage("§7Использование: §f.way delete <имя>");
                  return;
               }

               if (this.waypoints.remove(args[1].toLowerCase()) != null) {
                  this.save();
                  ChatUtils.addChatMessage("§aВейпоинт удалён: §f" + args[1]);
               } else {
                  ChatUtils.addChatMessage("§cВейпоинт не найден: §f" + args[1]);
               }
               break;
            case "clear":
               int count = this.waypoints.size();
               this.waypoints.clear();
               this.save();
               ChatUtils.addChatMessage("§aУдалено вейпоинтов: §f" + count);
               break;
            case "list":
               if (this.waypoints.isEmpty()) {
                  ChatUtils.addChatMessage("§7Вейпоинтов нет");
                  return;
               }

               ChatUtils.addChatMessage("§7Вейпоинты (§f" + this.waypoints.size() + "§7):");

               for (WayCommand.Waypoint w : this.waypoints.values()) {
                  ChatUtils.addChatMessage(
                     "§8• §f" + w.name() + " §7— §f" + (int)w.pos().field_1352 + ", " + (int)w.pos().field_1351 + ", " + (int)w.pos().field_1350
                  );
               }
               break;
            default:
               this.showHelp();
         }
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String lower = subPrefix.toLowerCase();
      if (!lower.startsWith("delete ") && !lower.startsWith("del ") && !lower.startsWith("remove ")) {
         if (lower.startsWith("add ") && mc.field_1724 != null) {
            String rest = subPrefix.substring(4);
            String[] tokens = rest.split(" ", -1);
            if (tokens.length >= 2 && !tokens[0].isEmpty()) {
               int px = (int)Math.floor(mc.field_1724.method_23317());
               int py = (int)Math.floor(mc.field_1724.method_23318());
               int pz = (int)Math.floor(mc.field_1724.method_23321());
               String[] coords = new String[]{String.valueOf(px), String.valueOf(py), String.valueOf(pz)};
               String typed = tokens[tokens.length - 1];
               int idx = tokens.length - 2;
               if (idx >= 0 && idx < 3 && coords[idx].startsWith(typed)) {
                  StringBuilder sb = new StringBuilder("add ").append(tokens[0]);

                  for (int i = 0; i < tokens.length - 2 && i < 3; i++) {
                     sb.append(' ').append(tokens[1 + i]);
                  }

                  sb.append(' ').append(coords[idx]);
                  return List.of(sb.toString());
               }
            }
         }

         return Stream.of("add", "delete", "clear", "list").filter(s -> s.startsWith(lower)).toList();
      } else {
         String sub = lower.substring(0, lower.indexOf(32));
         String namePart = lower.substring(sub.length() + 1);
         return this.waypoints.values().stream().filter(w -> w.name().toLowerCase().startsWith(namePart)).map(w -> sub + " " + w.name()).toList();
      }
   }

   @EventHandler
   public void onDisplay(EventDisplay e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.waypoints.isEmpty()) {
         if (!mc.field_1690.field_1842) {
            Font font = Fonts.sf_medium;
            float fontSize = 8.0F;
            class_243 camPos = mc.field_1773.method_19418().method_71156();
            float maxRenderDist = (float)Math.max(64, (Integer)mc.field_1690.method_42503().method_41753() * 16 - 16);

            for (WayCommand.Waypoint w : this.waypoints.values()) {
               float dist = (float)camPos.method_1022(w.pos());
               class_243 renderPos = w.pos();
               boolean clamped = dist > maxRenderDist;
               if (clamped) {
                  renderPos = camPos.method_1019(w.pos().method_1020(camPos).method_1029().method_1021((double)maxRenderDist));
               }

               class_243 screen = Projection.worldSpaceToScreenSpace(renderPos);
               if (!(screen.field_1350 <= 0.0) && !(screen.field_1350 >= 1.0)) {
                  String text = w.name() + " " + ColorFormatting.getColor(ThemeColor.getDarkTextColor(1.0F)) + (int)dist + "м";
                  float wr = font.getWidth(text, fontSize) + 10.0F;
                  float h = 13.0F;
                  float x = (float)screen.field_1352 - wr / 2.0F;
                  float y = (float)screen.field_1351 - h - 6.0F;
                  RenderUtil.Blur.blur(x, y, wr, h, 1.0F, 4.0F, ColorUtil.getRectColor(1.0F));
                  RenderUtil.Render2D.outline(x, y, wr, h, 0.5F, ThemeColor.getOutlineColor(1.0F), 4.0F);
                  font.drawCentered(text, x + wr / 2.0F, y + 1.8F, fontSize, ThemeColor.getTextColor(1.0F));
                  if (!clamped) {
                     RenderUtil.Render2D.rect((float)screen.field_1352 - 1.5F, (float)screen.field_1351 - 1.5F, 3.0F, 3.0F, ColorUtil.getClientColor1(1), 1.5F);
                  }
               }
            }
         }
      }
   }

   private void save() {
      JsonArray arr = new JsonArray();

      for (WayCommand.Waypoint w : this.waypoints.values()) {
         JsonObject obj = new JsonObject();
         obj.addProperty("name", w.name());
         obj.addProperty("x", w.pos().field_1352);
         obj.addProperty("y", w.pos().field_1351);
         obj.addProperty("z", w.pos().field_1350);
         arr.add(obj);
      }

      try {
         Files.createDirectories(FILE.getParent());

         try (OutputStreamWriter wtr = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(arr, wtr);
         }
      } catch (IOException var7) {
         var7.printStackTrace();
      }
   }

   private void load() {
      if (Files.exists(FILE)) {
         try (InputStreamReader r = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            this.waypoints.clear();

            for (JsonElement el : arr) {
               JsonObject obj = el.getAsJsonObject();
               String name = obj.get("name").getAsString();
               class_243 pos = new class_243(obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble());
               this.waypoints.put(name.toLowerCase(), new WayCommand.Waypoint(name, pos));
            }
         } catch (Exception var10) {
            var10.printStackTrace();
         }
      }
   }

   private void showHelp() {
      ChatUtils.addChatMessage("§7.way §fadd §7<имя> [x y z] §8| §7.way §fdelete §7<имя> §8| §7.way §fclear §8| §7.way §flist");
   }

   public Collection<WayCommand.Waypoint> getWaypoints() {
      return this.waypoints.values();
   }

   public void addWaypoint(String name, class_243 pos) {
      this.waypoints.put(name.toLowerCase(), new WayCommand.Waypoint(name, pos));
      this.save();
   }

   public boolean removeWaypoint(String name) {
      boolean removed = this.waypoints.remove(name.toLowerCase()) != null;
      if (removed) {
         this.save();
      }

      return removed;
   }

   @Environment(EnvType.CLIENT)
   public static record Waypoint(String name, class_243 pos) {
   }
}
