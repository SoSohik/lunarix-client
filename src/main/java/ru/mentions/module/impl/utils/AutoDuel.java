package ru.mentions.module.impl.utils;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_2596;
import net.minecraft.class_3944;
import net.minecraft.class_640;
import net.minecraft.class_7439;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventUpdate;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.ModeSetting;
import ru.mentions.utils.other.TimerUtil;

@ModuleInfo(
   name = "Auto Duel",
   desc = "Автоматически отправляет вызовы на дуэли (ReallyWorld)",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoDuel extends Module {
   private static final Pattern pattern = Pattern.compile("^\\w{3,16}$");
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Шары", "Щит", "Шипы 3", "Незеритка", "Читерский рай", "Лук", "Классик", "Тотемы", "Нодебафф");
   private double lastPosX;
   private double lastPosY;
   private double lastPosZ;
   private final List<String> sent = new ArrayList<>();
   private int currentPlayerIndex = 0;
   private final TimerUtil counter = new TimerUtil();
   private final TimerUtil counter2 = new TimerUtil();
   private final TimerUtil counterChoice = new TimerUtil();
   private final TimerUtil counterTo = new TimerUtil();
   private static final String MENU_KIT = "kit";
   private static final String MENU_SETUP = "setup";
   private final Map<Integer, String> pendingMenus = new HashMap<>();

   @Override
   protected void onDisable() {
      this.pendingMenus.clear();
   }

   @EventHandler
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         List<String> players = this.getOnlinePlayers();
         double distance = Math.sqrt(
            Math.pow(this.lastPosX - mc.field_1724.method_23317(), 2.0)
               + Math.pow(this.lastPosY - mc.field_1724.method_23318(), 2.0)
               + Math.pow(this.lastPosZ - mc.field_1724.method_23321(), 2.0)
         );
         if (distance > 500.0) {
            this.toggle();
         }

         this.lastPosX = mc.field_1724.method_23317();
         this.lastPosY = mc.field_1724.method_23318();
         this.lastPosZ = mc.field_1724.method_23321();
         if (this.counter2.isReached(800L * (long)players.size())) {
            this.sent.clear();
            this.currentPlayerIndex = 0;
            this.counter2.reset();
         }

         this.handleContainer();
         if (!players.isEmpty() && this.counter.isReached(1000L)) {
            if (this.currentPlayerIndex >= players.size()) {
               this.currentPlayerIndex = 0;
            }

            String player;
            if (!this.sent.contains(player = players.get(this.currentPlayerIndex)) && !player.equals(this.getSelfName())) {
               this.sendCommand("duel " + player);
               this.sent.add(player);
            }

            this.currentPlayerIndex++;
            this.counter.reset();
         }
      }
   }

   private void handleContainer() {
      if (!(mc.field_1724.field_7512 instanceof class_1707 handler)) {
         this.pendingMenus.clear();
      } else {
         String menu = this.pendingMenus.get(handler.field_7763);
         if (menu != null) {
            if (mc.field_1755 != null) {
               mc.method_1507(null);
            }

            if ("kit".equals(menu)) {
               if (this.counterChoice.isReached(150L)) {
                  int slot = this.getKitSlot();
                  if (slot >= 0) {
                     mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7794, mc.field_1724);
                  }

                  this.counterChoice.reset();
               }
            } else if ("setup".equals(menu) && this.counterTo.isReached(150L)) {
               mc.field_1761.method_2906(handler.field_7763, 0, 0, class_1713.field_7794, mc.field_1724);
               this.counterTo.reset();
            }
         }
      }
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (!event.isSend()) {
         class_2596<?> packet = event.getPacket();
         if (packet instanceof class_3944 open) {
            String title = open.method_17594().getString();
            if (title.contains("Выбор набора (1/1)")) {
               this.pendingMenus.put(open.method_17592(), "kit");
            } else if (title.contains("Настройка поединка")) {
               this.pendingMenus.put(open.method_17592(), "setup");
            }
         }

         class_7439 chat;
         String text;
         if (packet instanceof class_7439
            && (
               (text = (chat = (class_7439)packet).comp_763().getString().toLowerCase()).contains("начало")
                     && text.contains("через")
                     && text.contains("секунд!")
                  || text.isEmpty()
            )) {
            this.toggle();
         }
      }
   }

   private int getKitSlot() {
      if (this.mode.is("Щит")) {
         return 0;
      } else if (this.mode.is("Шипы 3")) {
         return 1;
      } else if (this.mode.is("Лук")) {
         return 2;
      } else if (this.mode.is("Тотемы")) {
         return 3;
      } else if (this.mode.is("Нодебафф")) {
         return 4;
      } else if (this.mode.is("Шары")) {
         return 5;
      } else if (this.mode.is("Классик")) {
         return 6;
      } else if (this.mode.is("Читерский рай")) {
         return 7;
      } else {
         return this.mode.is("Незеритка") ? 8 : -1;
      }
   }

   private String getSelfName() {
      return mc.method_1548().method_1676();
   }

   private void sendCommand(String command) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         mc.field_1724.field_3944.method_45730(command.startsWith("/") ? command.substring(1) : command);
      }
   }

   private List<String> getOnlinePlayers() {
      ArrayList<String> names = new ArrayList<>();
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         for (class_640 entry : mc.field_1724.field_3944.method_2880()) {
            GameProfile profile = entry.method_2966();
            String name;
            if (profile != null && (name = profile.name()) != null && pattern.matcher(name).matches()) {
               names.add(name);
            }
         }

         return names;
      } else {
         return names;
      }
   }
}
