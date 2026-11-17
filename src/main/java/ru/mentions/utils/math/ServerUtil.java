package ru.mentions.utils.math;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2596;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_2761;
import net.minecraft.class_3532;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import org.apache.commons.lang3.StringUtils;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.mixin.BossBarHudAccessor;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.taskript.StopWatch;

@Environment(EnvType.CLIENT)
public final class ServerUtil implements IMinecraft {
   private static final StopWatch pvpWatch = new StopWatch();
   public static String server = "Local   ";
   public static float TPS = 20.0F;
   public static long timestamp;
   public static int anarchy;
   public static boolean pvpEnd;

   public static void tick() {
      anarchy = getAnarchyMode();
      server = getServer();
      pvpEnd = inPvpEnd();
      if (inPvp()) {
         pvpWatch.reset();
      }
   }

   public static void packet(EventPacket e) {
      class_2596<?> packet = e.getPacket();
      if (packet instanceof class_2761) {
         long nanoTime = System.nanoTime();
         float maxTPS = 20.0F;
         float rawTPS = maxTPS * (1.0E9F / (float)(nanoTime - timestamp));
         TPS = class_3532.method_15363(rawTPS, 0.0F, maxTPS);
         timestamp = nanoTime;
      }
   }

   public static boolean nullCheck() {
      return mc.field_1724 == null || mc.field_1687 == null;
   }

   private static String getServer() {
      if (!nullCheck() && mc.method_1562() != null && mc.method_1562().method_45734() != null && mc.method_1562().method_52790() != null) {
         String serverIp = mc.method_1562().method_45734().field_3761.toLowerCase();
         String brand = mc.method_1562().method_52790().toLowerCase();
         if (brand.contains("botfilter")) {
            return "FunTime";
         } else if (brand.contains("§6spooky§ccore")) {
            return "SpookyTime";
         } else if (brand.contains("holyworld") || brand.contains("vk.com/idwok")) {
            return "HolyWorld";
         } else {
            return serverIp.contains("reallyworld") ? "ReallyWorld" : serverIp;
         }
      } else {
         return "Vanilla";
      }
   }

   private static int getAnarchyMode() {
      class_269 scoreboard = mc.field_1687.method_8428();
      class_266 objective = scoreboard.method_1189(class_8646.field_45157);
      String var2 = server;
      switch (var2) {
         case "FunTime":
            String[] string;
            if (objective != null && (string = objective.method_1114().getString().split("-")).length > 1) {
               return Integer.parseInt(string[1]);
            }
            break;
         case "HolyWorld":
            for (class_9011 scoreboardEntry : scoreboard.method_1184(objective)) {
               String text = class_268.method_1142(scoreboard.method_1164(scoreboardEntry.comp_2127()), scoreboardEntry.method_55387()).getString();
               String stringx;
               if (!text.isEmpty() && (stringx = StringUtils.substringBetween(text, "#", " -◆-")) != null && !stringx.isEmpty()) {
                  return Integer.parseInt(stringx.replace(" 1.20", ""));
               }
            }
      }

      return -1;
   }

   public static boolean isPvp() {
      return !pvpWatch.finished(500.0);
   }

   private static boolean inPvp() {
      return ((BossBarHudAccessor)mc.field_1705.method_1740())
         .mentions$getBossBars()
         .values()
         .stream()
         .map(c -> c.method_5414().getString().toLowerCase())
         .anyMatch(s -> s.contains("pvp") || s.contains("пвп"));
   }

   private static boolean inPvpEnd() {
      return ((BossBarHudAccessor)mc.field_1705.method_1740())
         .mentions$getBossBars()
         .values()
         .stream()
         .map(c -> c.method_5414().getString().toLowerCase())
         .anyMatch(s -> (s.contains("pvp") || s.contains("пвп")) && (s.contains("0") || s.contains("1")));
   }

   public static String getWorldType() {
      return mc.field_1687.method_27983().method_29177().method_12832();
   }

   public static boolean isCopyTime() {
      return server.equals("SpookyTime");
   }

   public static boolean isFunTime() {
      return server.equals("FunTime");
   }

   public static boolean isReallyWorld() {
      return server.equals("ReallyWorld");
   }

   public static boolean isHolyWorld() {
      return server.equals("HolyWorld");
   }

   public static boolean isVanilla() {
      return server.equals("Vanilla");
   }

   @Generated
   private ServerUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static int getAnarchy() {
      return anarchy;
   }

   @Generated
   public static boolean isPvpEnd() {
      return pvpEnd;
   }
}
