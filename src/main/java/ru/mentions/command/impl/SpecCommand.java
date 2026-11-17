package ru.mentions.command.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_742;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.player.Spectator;

@Environment(EnvType.CLIENT)
public class SpecCommand extends Command implements IMinecraft {
   private static final List<String> ACTIONS = List.of("off", "next", "prev", "info");
   private static final int CHECK_RADIUS = 2;
   private boolean lastTerrainOk = true;

   public SpecCommand() {
      super("spec", ".spec [ник|id|off|next|prev|info]", "Вселиться в сущность (только клиент)");
      Client.eventHandler().subscribe(this);
   }

   @Override
   public void execute(String[] args) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (args.length != 0) {
            String var6 = args[0].toLowerCase(Locale.ROOT);
            switch (var6) {
               case "off":
               case "stop":
               case "exit":
                  if (!Spectator.isActive()) {
                     ChatUtils.addChatMessage("§7Ты никого не спектуешь");
                     return;
                  }

                  this.stop();
                  break;
               case "next":
                  this.cycle(1);
                  break;
               case "prev":
                  this.cycle(-1);
                  break;
               case "info":
                  this.info();
                  break;
               default:
                  class_1297 found = this.find(args[0]);
                  if (found == null) {
                     ChatUtils.addChatMessage("§cНе найдено: §r" + args[0]);
                     return;
                  }

                  this.attach(found);
            }
         } else if (Spectator.isActive()) {
            this.stop();
         } else {
            class_1297 looked = mc.field_1692;
            if (looked != null && looked != mc.field_1724) {
               this.attach(looked);
            } else {
               ChatUtils.addChatMessage("§7Наведись на сущность или укажи ник: §f.spec <ник>");
            }
         }
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (Spectator.validate()) {
         ChatUtils.addChatMessage("§7Цель пропала — камера вернулась");
         this.lastTerrainOk = true;
      } else if (Spectator.isActive()) {
         boolean ok = Spectator.isTerrainLoaded(Spectator.getTarget());
         if (ok != this.lastTerrainOk) {
            ChatUtils.addChatMessage(ok ? "§aТерра вокруг цели прогрузилась" : "§eТерра вокруг цели не прогружена — сервер не шлёт эти чанки");
            this.lastTerrainOk = ok;
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      Spectator.reset();
      this.lastTerrainOk = true;
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String prefix = subPrefix.toLowerCase(Locale.ROOT);
      Stream<String> names = mc.field_1687 == null
         ? Stream.empty()
         : mc.field_1687.method_18456().stream().filter(p -> p != mc.field_1724).map(p -> p.method_5477().getString());
      return Stream.concat(names, ACTIONS.stream()).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix)).distinct().collect(Collectors.toList());
   }

   private void attach(class_1297 entity) {
      if (entity == mc.field_1724) {
         ChatUtils.addChatMessage("§7В себя вселяться незачем");
      } else if (!Spectator.start(entity)) {
         ChatUtils.addChatMessage("§cНе получилось вселиться");
      } else {
         this.lastTerrainOk = Spectator.isTerrainLoaded(entity);
         ChatUtils.addChatMessage(
            "§7Вселился в §f"
               + entity.method_5477().getString()
               + " §8["
               + entity.method_5628()
               + "]§7, §f"
               + (int)Math.sqrt(mc.field_1724.method_5858(entity))
               + "§7 блоков §8| §7терра: "
               + this.terrainStatus(entity)
         );
         if (!this.lastTerrainOk) {
            ChatUtils.addChatMessage("§7Сервер не шлёт чанки в той точке — подгрузить их с клиента нельзя");
         }
      }
   }

   private String terrainStatus(class_1297 entity) {
      int total = Spectator.chunksInSquare(2);
      int loaded = Spectator.loadedChunksAround(entity, 2);
      String color = loaded == total ? "§a" : (loaded == 0 ? "§c" : "§e");
      return color + loaded + "§7/§f" + total + " §7чанков";
   }

   private void info() {
      class_1297 entity = Spectator.getTarget();
      if (entity == null) {
         ChatUtils.addChatMessage("§7Ты никого не спектуешь");
      } else {
         ChatUtils.addChatMessage("§7Цель: §f" + entity.method_5477().getString() + " §8[" + entity.method_5628() + "]");
         ChatUtils.addChatMessage("§7Дистанция: §f" + (int)Math.sqrt(mc.field_1724.method_5858(entity)) + " §8| §7терра: " + this.terrainStatus(entity));
         ChatUtils.addChatMessage(
            "§7Всего чанков у клиента: §f" + mc.field_1687.method_2935().method_14151() + " §8| §7прорисовка: §f" + (int)mc.field_1769.method_34812()
         );
      }
   }

   private void stop() {
      class_1297 old = Spectator.getTarget();
      Spectator.stop();
      ChatUtils.addChatMessage("§7Камера вернулась" + (old != null ? " от §f" + old.method_5477().getString() : ""));
   }

   private class_1297 find(String query) {
      String lower = query.toLowerCase(Locale.ROOT);

      for (class_742 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && player.method_5477().getString().equalsIgnoreCase(query)) {
            return player;
         }
      }

      for (class_742 playerx : mc.field_1687.method_18456()) {
         if (playerx != mc.field_1724 && playerx.method_5477().getString().toLowerCase(Locale.ROOT).startsWith(lower)) {
            return playerx;
         }
      }

      try {
         class_1297 byId = mc.field_1687.method_8469(Integer.parseInt(query));
         if (byId != null && byId != mc.field_1724) {
            return byId;
         }
      } catch (NumberFormatException var5) {
      }

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity != mc.field_1724 && entity.method_5477().getString().toLowerCase(Locale.ROOT).startsWith(lower)) {
            return entity;
         }
      }

      return null;
   }

   private void cycle(int direction) {
      ArrayList candidates = new ArrayList(
         mc.field_1687
            .method_18456()
            .stream()
            .filter(p -> p != mc.field_1724)
            .sorted(Comparator.comparingDouble(p -> p.method_5858(mc.field_1724)))
            .collect(Collectors.toList())
      );
      if (candidates.isEmpty()) {
         ChatUtils.addChatMessage("§7Рядом никого нет");
      } else {
         int index = candidates.indexOf(Spectator.getTarget());
         int next = index < 0 ? (direction > 0 ? 0 : candidates.size() - 1) : Math.floorMod(index + direction, candidates.size());
         this.attach((class_1297)candidates.get(next));
      }
   }
}
