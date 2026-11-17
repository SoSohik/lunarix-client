package ru.mentions.screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_156;
import net.minecraft.class_243;
import net.minecraft.class_640;
import org.lwjgl.glfw.GLFW;
import ru.mentions.Client;
import ru.mentions.command.Command;
import ru.mentions.command.impl.WayCommand;
import ru.mentions.config.ConfigManager;
import ru.mentions.lang.Lang;
import ru.mentions.module.impl.display.ClientTheme;
import ru.mentions.module.impl.display.GpsHud;
import ru.mentions.screen.editor.EditorWidgets;
import ru.mentions.screen.editor.Rect;
import ru.mentions.screen.editor.SettingsPanel;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.funtime.FunTimeEventsService;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.math.MathUtil;
import ru.mentions.utils.math.ServerUtil;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.Scissor;
import ru.mentions.utils.render.font.Fonts;

@Environment(EnvType.CLIENT)
public final class ManagementPanel implements IMinecraft {
   private final SettingsPanel themeSettings = new SettingsPanel();
   private final EditorWidgets themeWidgets = new EditorWidgets();
   private final List<ManagementPanel.Hit> navigation = new ArrayList<>();
   private final List<ManagementPanel.Hit> hits = new ArrayList<>();
   private final ManagementPanel.TextInput name = new ManagementPanel.TextInput(64);
   private final ManagementPanel.TextInput filter = new ManagementPanel.TextInput(64);
   private final ManagementPanel.TextInput tokenInput = new ManagementPanel.TextInput(128);
   private boolean onlyMyServer = false;
   public int eventsSubTab = 0;
   private final List<String> configs = new ArrayList<>();
   private final List<String> online = new ArrayList<>();
   private final List<String> savedFriends = new ArrayList<>();
   private final List<WayCommand.Waypoint> gpsPoints = new ArrayList<>();
   private final List<ManagementPanel.Hit> scrollRegions = new ArrayList<>();
   private final float[] scroll = new float[2];
   private final float[] maxScroll = new float[2];
   private ManagementPanel.Tab tab;
   private ManagementPanel.TextInput focused;
   private String selected;
   private String pendingName;
   private boolean pendingDelete;
   private String status = "";
   private boolean error;
   private Object connection;
   private long nextPlayersRefresh;
   private float x;
   private float y;
   private float width;
   private float height;
   private float unit = 1.0F;
   private float alpha;
   private float mouseX;
   private float mouseY;

   public boolean active() {
      return this.tab != null;
   }

   public String title() {
      return this.active() ? this.tab.getDisplayName() : "";
   }

   public boolean selected(ManagementPanel.Tab candidate) {
      return this.tab == candidate;
   }

   public boolean typing() {
      return this.focused != null || this.hasConfirmation();
   }

   public boolean hasConfirmation() {
      return this.pendingName != null;
   }

   public void clearFocus() {
      this.themeSettings.releaseDrag();
      this.focused = null;
      this.pendingName = null;
      this.hits.clear();
   }

   public void close() {
      this.clearFocus();
      this.tab = null;
   }

   public void select(ManagementPanel.Tab tab) {
      this.clearFocus();
      this.tab = tab;
      this.themeSettings.reset();
      this.filter.set("");
      this.scroll[0] = this.scroll[1] = 0.0F;
      this.status = "";
      this.error = false;
      this.onOpened();
   }

   public void onOpened() {
      this.clearFocus();
      if (this.tab == ManagementPanel.Tab.CONFIGS) {
         this.refreshConfigs();
      }

      if (this.tab == ManagementPanel.Tab.FRIENDS) {
         this.refreshFriends();
      }

      if (this.tab == ManagementPanel.Tab.GPS) {
         this.refreshGps();
      }

      if (this.tab == ManagementPanel.Tab.EVENTS) {
         this.tokenInput.value = FunTimeEventsService.getClientToken();
         FunTimeEventsService.refresh(null);
      }
   }

   public void beginNavigation() {
      this.navigation.clear();
   }

   public void drawNavigation(ManagementPanel.Tab tab, float x, float y, float w, float h, float unit, float alpha, float mx, float my, Runnable select) {
      boolean active = this.tab == tab;
      boolean hovered = MathUtil.isHovered(mx, my, x, y, w, h);
      RenderUtil.Render2D.rect(x, y, w, h, ColorUtil.getColor(active ? 42 : (hovered ? 36 : 20), alpha), 4.0F * unit);
      RenderUtil.Render2D.outline(
         x,
         y,
         w,
         h,
         0.6F * unit,
         active ? ColorUtil.replAlpha(ColorUtil.client(), alpha) : ColorUtil.getColor(105, alpha * (hovered ? 1.0F : 0.45F)),
         4.0F * unit
      );
      String tabName = tab.getDisplayName();
      float size = Math.min(7.0F * unit, (w - 3.0F * unit) / Math.max(1.0F, Fonts.sf_medium.getWidth(tabName, 1.0F)));
      Fonts.sf_medium.drawCentered(tabName, x + w / 2.0F, y + (h - size) / 2.0F - unit, size, ColorUtil.getColor(!active && !hovered ? 200 : 245, alpha));
      this.navigation.add(new ManagementPanel.Hit(x, y, w, h, select));
   }

   public boolean clickNavigation(float mx, float my, int button) {
      if (button == 0 && !this.hasConfirmation()) {
         for (ManagementPanel.Hit hit : this.navigation) {
            if (hit.contains(mx, my)) {
               hit.action.run();
               this.navigation.clear();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void render(float x, float y, float w, float h, float unit, float alpha, float mx, float my) {
      this.x = x;
      this.y = y;
      this.unit = unit;
      this.width = w / unit;
      this.height = h / unit;
      this.alpha = alpha;
      this.mouseX = (mx - x) / unit;
      this.mouseY = (my - y) / unit;
      this.hits.clear();
      this.scrollRegions.clear();
      RenderUtil.Render2D.glow(
         this.x, this.y, this.width * this.unit, 20.0F * this.unit, ColorUtil.getColor(0, 0.04F * this.alpha), 6.0F * this.unit, 8.0F, 1.0F
      );
      RenderUtil.Render2D.rect(this.x, this.y, this.width * this.unit, 20.0F * this.unit, ColorUtil.getColor(0, 0.15F * this.alpha), 6.0F * this.unit);
      String catIcon = this.tab == ManagementPanel.Tab.THEMES
         ? "E"
         : (this.tab == ManagementPanel.Tab.CONFIGS ? "F" : (this.tab == ManagementPanel.Tab.GPS ? "C" : (this.tab == ManagementPanel.Tab.EVENTS ? "P" : "D")));
      Fonts.mentions_2
         .draw(catIcon, this.x + 8.0F * this.unit, this.y + 6.0F * this.unit, 7.0F * this.unit, ColorUtil.replAlpha(ColorUtil.client(), this.alpha));
      String catTitle = this.tab == ManagementPanel.Tab.THEMES
         ? Lang.pick("Теми", "Темы", "Themes")
         : (
            this.tab == ManagementPanel.Tab.CONFIGS
               ? Lang.pick("Конфігурації", "Конфигурации", "Configurations")
               : (
                  this.tab == ManagementPanel.Tab.GPS
                     ? Lang.pick("GPS точки", "GPS точки", "GPS Points")
                     : (
                        this.tab == ManagementPanel.Tab.EVENTS
                           ? Lang.pick("Івенти FunTime (Live)", "Ивенты FunTime (API)", "FunTime Events")
                           : Lang.pick("Список друзів", "Список друзей", "Friends List")
                     )
               )
         );
      Fonts.sf_bold.draw(catTitle, this.x + 20.0F * this.unit, this.y + 5.5F * this.unit, 7.0F * this.unit, ColorUtil.getColor(220, this.alpha));
      if (this.tab == ManagementPanel.Tab.CONFIGS) {
         this.button(Lang.pick("Оновити", "Обновить", "Refresh"), this.width - 138.0F, 2.0F, 48.0F, true, this::refreshConfigs, 6.5F, false);
         this.button(Lang.pick("Відкрити папку", "Открыть папку", "Open folder"), this.width - 86.0F, 2.0F, 78.0F, true, this::openFolder, 6.5F, false);
      } else if (this.tab == ManagementPanel.Tab.FRIENDS) {
         this.button(Lang.pick("Оновити", "Обновить", "Refresh"), this.width - 56.0F, 2.0F, 48.0F, true, () -> {
            this.refreshFriends();
            this.message(Lang.pick("Список гравців оновлено.", "Список игроков обновлён.", "Player list refreshed."), false);
         }, 6.5F, false);
      } else if (this.tab == ManagementPanel.Tab.GPS) {
         this.button(Lang.pick("Оновити", "Обновить", "Refresh"), this.width - 56.0F, 2.0F, 48.0F, true, this::refreshGps, 6.5F, false);
      } else if (this.tab == ManagementPanel.Tab.EVENTS) {
         this.button(
            FunTimeEventsService.isLoading() ? Lang.pick("Оновлення...", "Загрузка...", "Loading...") : Lang.pick("Оновити", "Обновить", "Refresh"),
            this.width - 66.0F,
            2.0F,
            58.0F,
            true,
            () -> FunTimeEventsService.refresh(status -> this.message(status, false)),
            6.5F,
            false
         );
      }

      float contentY = 26.0F;
      float contentH = this.height - contentY;
      RenderUtil.Render2D.glow(
         this.x,
         this.y + contentY * this.unit,
         this.width * this.unit,
         contentH * this.unit,
         ColorUtil.getColor(0, 0.04F * this.alpha),
         7.0F * this.unit,
         8.0F,
         1.0F
      );
      RenderUtil.Render2D.rect(
         this.x, this.y + contentY * this.unit, this.width * this.unit, contentH * this.unit, ColorUtil.getColor(0, 0.15F * this.alpha), 7.0F * this.unit
      );
      if (this.tab == ManagementPanel.Tab.THEMES) {
         this.themeWidgets.setMouse(mx, my);
         this.themeSettings
            .layoutEmbedded(
               Client.get().moduleManager().get(ClientTheme.class),
               "Настройки оформления",
               new Rect(x, y + contentY * unit, w, contentH * unit),
               this.themeWidgets
            );
         if (GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 0) == 0) {
            this.themeSettings.releaseDrag();
         } else {
            this.themeSettings.rawMouseMoved(mx, my);
         }

         this.themeSettings.draw(this.themeWidgets, alpha);
      } else {
         Scissor.enable(this.x, this.y + contentY * this.unit, this.width * this.unit, contentH * this.unit, 2.0F);

         try {
            if (this.tab == ManagementPanel.Tab.CONFIGS) {
               this.drawConfigs();
            }

            if (this.tab == ManagementPanel.Tab.GPS) {
               this.drawGps();
            }

            if (this.tab == ManagementPanel.Tab.EVENTS) {
               this.drawEvents();
            }

            if (this.tab == ManagementPanel.Tab.FRIENDS) {
               Object current = mc.field_1724 == null ? null : mc.field_1724.field_3944;
               if (current != this.connection || System.currentTimeMillis() >= this.nextPlayersRefresh) {
                  this.refreshFriends();
               }

               this.drawFriends();
            }

            this.drawStatus();
            if (this.hasConfirmation()) {
               this.drawConfirmation();
            }
         } finally {
            Scissor.disable();
         }
      }
   }

   private void drawConfigs() {
      this.input(this.name, Lang.pick("Назва нового конфігу...", "Имя нового конфига...", "New config name..."), 8.0F, 34.0F, this.width - 76.0F);
      this.button(Lang.pick("Створити", "Создать", "Create"), this.width - 64.0F, 34.0F, 56.0F, true, this::createConfig, 7.0F, true);
      this.input(this.filter, Lang.pick("Пошук конфігів...", "Поиск конфигов...", "Search configs..."), 8.0F, 56.0F, this.width - 16.0F);
      List<String> visible = this.filtered(this.configs);
      this.text(
         Lang.pick("Збережені конфіги (", "Сохранённые конфиги (", "Saved configs (") + visible.size() + "/" + this.configs.size() + ")",
         8.0F,
         80.0F,
         7.0F,
         -2038288
      );
      this.list(visible, 8.0F, 94.0F, this.width - 16.0F, this.height - 177.0F, 0, false);
      float bw = (this.width - 24.0F) / 3.0F;
      float by = this.height - 75.0F;
      boolean hasSelection = this.selected != null && visible.contains(this.selected);
      this.button(Lang.pick("Завантажити", "Загрузить", "Load"), 8.0F, by, bw, hasSelection, this::loadConfig, 7.0F, true);
      this.button(Lang.pick("Зберегти", "Сохранить", "Save"), 12.0F + bw, by, bw, hasSelection, () -> this.confirm(false), 7.0F, false);
      this.button(Lang.pick("Видалити", "Удалить", "Delete"), 16.0F + 2.0F * bw, by, bw, hasSelection, () -> this.confirm(true), 7.0F, false);
      float sw = (this.width - 24.0F) / 2.0F;
      this.button(Lang.pick("Експорт", "Экспорт", "Export"), 8.0F, by + 21.0F, sw, hasSelection, this::exportConfig, 7.0F, false);
      this.button(Lang.pick("Імпорт", "Импорт", "Import"), 12.0F + sw, by + 21.0F, sw, !this.name.value.isBlank(), this::importConfig, 7.0F, false);
   }

   private void drawFriends() {
      this.input(this.filter, Lang.pick("Пошук гравців...", "Поиск игроков...", "Search players..."), 8.0F, 34.0F, this.width - 16.0F);
      float column = (this.width - 24.0F) / 2.0F;
      List<String> players = this.filtered(this.online);
      List<String> friends = this.filtered(this.savedFriends);
      this.text(Lang.pick("Гравці на сервері (", "Игроки на сервере (", "Players on server (") + players.size() + ")", 8.0F, 58.0F, 7.0F, -2038288);
      this.text(Lang.pick("У друзях (", "В друзьях (", "Friends (") + friends.size() + ")", 16.0F + column, 58.0F, 7.0F, -2038288);
      this.list(players, 8.0F, 72.0F, column, this.height - 108.0F, 0, true);
      this.list(friends, 16.0F + column, 72.0F, column, this.height - 108.0F, 1, true);
   }

   private void drawGps() {
      this.input(this.name, Lang.pick("Назва нової точки...", "Имя новой точки...", "New waypoint name..."), 8.0F, 34.0F, this.width - 96.0F);
      this.button(
         Lang.pick("Сюди", "Сюда", "Here"), this.width - 84.0F, 34.0F, 36.0F, mc.field_1724 != null && !this.name.value.isBlank(), this::addGpsHere, 6.5F, true
      );
      this.button(Lang.pick("Скинути", "Сброс", "Reset"), this.width - 44.0F, 34.0F, 36.0F, GpsHud.getTarget() != null, () -> {
         GpsHud.clearTarget();
         this.message(Lang.pick("GPS скинуто.", "GPS сброшен.", "GPS reset."), false);
      }, 6.5F, false);
      this.input(this.filter, Lang.pick("Пошук точок...", "Поиск точек...", "Search waypoints..."), 8.0F, 56.0F, this.width - 16.0F);
      this.text(Lang.pick("Вейпоінти (", "Вейпоинты (", "Waypoints (") + this.gpsPoints.size() + ")", 8.0F, 80.0F, 7.0F, -2038288);
      this.gpsList(8.0F, 94.0F, this.width - 16.0F, this.height - 130.0F);
   }

   private void gpsList(float x, float y, float w, float h) {
      h = Math.max(1.0F, h);
      int side = 1;
      this.rect(x, y, w, h, -15855594, 5.0F);
      this.outline(x, y, w, h, -14539212, 5.0F);
      this.maxScroll[side] = Math.max(0.0F, (float)this.gpsPoints.size() * 26.0F - h);
      this.scroll[side] = MathUtil.clamp(this.scroll[side], 0.0F, this.maxScroll[side]);
      this.scrollRegions.add(new ManagementPanel.Hit(x, y, w, h, null));
      Scissor.enable(this.x + x * this.unit, this.y + y * this.unit, w * this.unit, h * this.unit, 2.0F);

      try {
         if (this.gpsPoints.isEmpty()) {
            this.text("Нет вейпоинтов. Добавьте выше или через .way add.", x + 8.0F, y + 10.0F, 6.5F, -7695450);
         }

         int first = Math.max(0, (int)(this.scroll[side] / 26.0F));
         int last = Math.min(this.gpsPoints.size(), first + (int)Math.ceil((double)(h / 26.0F)) + 1);

         for (int i = first; i < last; i++) {
            WayCommand.Waypoint wp = this.gpsPoints.get(i);
            float rowY = y + (float)(i * 26) + 2.0F - this.scroll[side];
            boolean isHov = MathUtil.isHovered(this.mouseX, this.mouseY, x + 3.0F, rowY, w - 8.0F, 22.0F);
            int cardBg = isHov ? ColorUtil.getColor(28, 32, 44, (int)(200.0F * this.alpha)) : ColorUtil.getColor(18, 20, 28, (int)(180.0F * this.alpha));
            this.rect(x + 3.0F, rowY, w - 8.0F, 22.0F, cardBg, 4.0F);
            if (isHov) {
               this.outline(x + 3.0F, rowY, w - 8.0F, 22.0F, ColorUtil.getColor(255, 0.12F * this.alpha), 4.0F);
            }

            float dotR = 2.5F;
            RenderUtil.Render2D.rect(
               this.x + (x + 9.0F) * this.unit,
               this.y + (rowY + 9.0F) * this.unit,
               dotR * 2.0F * this.unit,
               dotR * 2.0F * this.unit,
               ColorUtil.replAlpha(ColorUtil.client(), 0.6F * this.alpha),
               dotR * this.unit
            );
            String coords = (int)wp.pos().field_1352 + ", " + (int)wp.pos().field_1351 + ", " + (int)wp.pos().field_1350;
            Fonts.sf_medium
               .drawFadingText(
                  wp.name(),
                  this.x + (x + 18.0F) * this.unit,
                  this.y + (rowY + 3.5F) * this.unit,
                  (w - 110.0F) * this.unit,
                  this.color(-2564372),
                  7.0F * this.unit
               );
            this.text(coords, x + 18.0F, rowY + 13.0F, 6.0F, -7695450);
            float clippedY = Math.max(y, rowY);
            float clippedH = Math.min(y + h, rowY + 22.0F) - clippedY;
            if (!(clippedH <= 0.0F)) {
               float bx = x + w - 96.0F;
               this.button("GPS", bx, rowY + 3.0F, 40.0F, true, () -> this.gpsNavigate(wp), 6.5F, true);
               this.hits.remove(this.hits.size() - 1);
               this.hits.add(new ManagementPanel.Hit(bx, clippedY, 40.0F, clippedH, () -> this.gpsNavigate(wp)));
               this.button("Удалить", bx + 44.0F, rowY + 3.0F, 44.0F, true, () -> this.removeGps(wp), 6.5F, false);
               this.hits.remove(this.hits.size() - 1);
               this.hits.add(new ManagementPanel.Hit(bx + 44.0F, clippedY, 44.0F, clippedH, () -> this.removeGps(wp)));
            }
         }
      } finally {
         Scissor.disable();
      }

      if (this.maxScroll[side] > 0.0F) {
         float thumb = Math.max(12.0F, h * h / ((float)this.gpsPoints.size() * 26.0F));
         float offset = (h - thumb) * this.scroll[side] / this.maxScroll[side];
         this.rect(x + w - 3.0F, y + offset, 2.0F, thumb, -10128756, 1.0F);
      }
   }

   private void drawEvents() {
      this.input(
         this.tokenInput,
         Lang.pick("FunTime токен (ft_...) або залиште пустим для демо", "FunTime client-token (ft_...)", "FunTime client-token"),
         8.0F,
         34.0F,
         this.width - 96.0F
      );
      this.button(Lang.pick("Зберегти", "Сохранить", "Save"), this.width - 84.0F, 34.0F, 76.0F, true, () -> {
         FunTimeEventsService.setClientToken(this.tokenInput.value);
         this.message(Lang.pick("Токен збережено! Оновлюю івенти...", "Токен сохранён! Обновляю ивенты...", "Token saved! Refreshing..."), false);
      }, 6.5F, true);
      float subTabW = 72.0F;
      this.button(Lang.pick("Івенти", "Ивенты", "Events"), 8.0F, 56.0F, subTabW, true, () -> {
         this.eventsSubTab = 0;
         this.scroll[1] = 0.0F;
      }, 6.5F, this.eventsSubTab == 0);
      this.button(Lang.pick("Авто-шахти", "Авто-шахты", "Auto-Mines"), 12.0F + subTabW, 56.0F, subTabW, true, () -> {
         this.eventsSubTab = 1;
         this.scroll[1] = 0.0F;
      }, 6.5F, this.eventsSubTab == 1);
      float searchX = 16.0F + subTabW * 2.0F;
      float searchW = this.width - searchX - 92.0F;
      this.input(this.filter, Lang.pick("Пошук по анархії чи назві...", "Поиск по анархии или названию...", "Search..."), searchX, 56.0F, searchW);
      this.button(
         this.onlyMyServer ? Lang.pick("Моя: УВІМК", "Моя: ВКЛ", "My: ON") : Lang.pick("Усі сервери", "Все сервера", "All servers"),
         this.width - 84.0F,
         56.0F,
         76.0F,
         true,
         () -> this.onlyMyServer = !this.onlyMyServer,
         6.0F,
         false
      );
      int curAnarchy = ServerUtil.getAnarchy();
      String q = this.filter.value.trim().toLowerCase(Locale.ROOT);
      String modeBadge = FunTimeEventsService.isDemoMode() ? " " + Lang.pick("[ДЕМО-РЕЖИМ]", "[ДЕМО-РЕЖИМ]", "[DEMO]") : " [API]";
      if (this.eventsSubTab == 0) {
         List<FunTimeEventsService.FunTimeEvent> allEvents = FunTimeEventsService.getEvents();
         List<FunTimeEventsService.FunTimeEvent> filtered = new ArrayList<>();

         for (FunTimeEventsService.FunTimeEvent ev : allEvents) {
            if (!this.onlyMyServer || curAnarchy <= 0 || ev.getServerNumber() == curAnarchy) {
               if (!q.isEmpty()) {
                  String searchHaystack = (ev.server() + " " + ev.name() + " " + ev.getDisplayName() + " " + ev.type() + " " + ev.loot())
                     .toLowerCase(Locale.ROOT);
                  if (!searchHaystack.contains(q)) {
                     continue;
                  }
               }

               filtered.add(ev);
            }
         }

         this.text(
            Lang.pick("Івенти (", "Ивенты (", "Events (") + filtered.size() + ")" + modeBadge + "  " + FunTimeEventsService.getLastStatus(),
            8.0F,
            80.0F,
            6.5F,
            -2038288
         );
         this.eventsList(filtered, 8.0F, 94.0F, this.width - 16.0F, this.height - 130.0F);
      } else {
         List<FunTimeEventsService.FunTimeMine> allMines = FunTimeEventsService.getMines();
         List<FunTimeEventsService.FunTimeMine> filtered = new ArrayList<>();

         for (FunTimeEventsService.FunTimeMine m : allMines) {
            if (!this.onlyMyServer || curAnarchy <= 0 || m.getServerNumber() == curAnarchy) {
               if (!q.isEmpty()) {
                  String searchHaystack = (m.server() + " " + m.name() + " " + m.rarity() + " " + m.getRarityLocalized()).toLowerCase(Locale.ROOT);
                  if (!searchHaystack.contains(q)) {
                     continue;
                  }
               }

               filtered.add(m);
            }
         }

         this.text(
            Lang.pick("Авто-шахти (", "Авто-шахты (", "Auto-Mines (") + filtered.size() + ")" + modeBadge + "  " + FunTimeEventsService.getLastStatus(),
            8.0F,
            80.0F,
            6.5F,
            -2038288
         );
         this.minesList(filtered, 8.0F, 94.0F, this.width - 16.0F, this.height - 130.0F);
      }
   }

   private void eventsList(List<FunTimeEventsService.FunTimeEvent> list, float x, float y, float w, float h) {
      h = Math.max(1.0F, h);
      int side = 1;
      float rowHeight = 32.0F;
      this.rect(x, y, w, h, -15855594, 5.0F);
      this.outline(x, y, w, h, -14539212, 5.0F);
      this.maxScroll[side] = Math.max(0.0F, (float)list.size() * rowHeight - h);
      this.scroll[side] = MathUtil.clamp(this.scroll[side], 0.0F, this.maxScroll[side]);
      this.scrollRegions.add(new ManagementPanel.Hit(x, y, w, h, null));
      Scissor.enable(this.x + x * this.unit, this.y + y * this.unit, w * this.unit, h * this.unit, 2.0F);

      try {
         if (list.isEmpty()) {
            if (FunTimeEventsService.isLoading()) {
               this.text(
                  Lang.pick("Завантаження активних івентів з api.funtime.su...", "Загрузка активных ивентов с api.funtime.su...", "Loading events..."),
                  x + 8.0F,
                  y + 10.0F,
                  6.5F,
                  -7695450
               );
               return;
            } else {
               if (this.onlyMyServer && ServerUtil.getAnarchy() <= 0) {
                  this.text(
                     Lang.pick(
                        "Ви не на сервері анархії FunTime. Натисніть 'Усі сервери' вище.",
                        "Вы не на сервере анархии FunTime. Нажмите 'Все сервера' выше.",
                        "Not on a FunTime server."
                     ),
                     x + 8.0F,
                     y + 10.0F,
                     6.5F,
                     -7695450
                  );
               } else {
                  this.text(
                     Lang.pick("Немає активних івентів за поточним фільтром.", "Нет активных ивентов по текущему фильтру.", "No active events."),
                     x + 8.0F,
                     y + 10.0F,
                     6.5F,
                     -7695450
                  );
               }

               return;
            }
         }

         int first = Math.max(0, (int)(this.scroll[side] / rowHeight));
         int last = Math.min(list.size(), first + (int)Math.ceil((double)(h / rowHeight)) + 1);

         for (int i = first; i < last; i++) {
            FunTimeEventsService.FunTimeEvent ev = list.get(i);
            float rowY = y + (float)i * rowHeight + 2.0F - this.scroll[side];
            boolean isHov = MathUtil.isHovered(this.mouseX, this.mouseY, x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F);
            boolean isCurrent = ev.isCurrentServer();
            int cardBg = isCurrent
               ? (isHov ? ColorUtil.getColor(36, 44, 60, (int)(220.0F * this.alpha)) : ColorUtil.getColor(24, 30, 44, (int)(200.0F * this.alpha)))
               : (isHov ? ColorUtil.getColor(28, 32, 44, (int)(200.0F * this.alpha)) : ColorUtil.getColor(18, 20, 28, (int)(180.0F * this.alpha)));
            this.rect(x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F, cardBg, 4.0F);
            if (isCurrent) {
               this.outline(x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F, ColorUtil.replAlpha(ColorUtil.client(), 0.45F * this.alpha), 4.0F);
            } else if (isHov) {
               this.outline(x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F, ColorUtil.getColor(255, 0.12F * this.alpha), 4.0F);
            }

            float dotR = 2.5F;
            RenderUtil.Render2D.rect(
               this.x + (x + 8.0F) * this.unit,
               this.y + (rowY + 13.5F) * this.unit,
               dotR * 2.0F * this.unit,
               dotR * 2.0F * this.unit,
               ColorUtil.replAlpha(ev.getAccentColor(), 0.9F * this.alpha),
               dotR * this.unit
            );
            float srvBadgeW = Fonts.sf_bold.getWidth(ev.getFormattedServer(), 6.0F * this.unit) + 6.0F * this.unit;
            int srvBg = isCurrent ? ColorUtil.replAlpha(ColorUtil.client(), 0.25F * this.alpha) : ColorUtil.getColor(255, 0.08F * this.alpha);
            RenderUtil.Render2D.rect(this.x + (x + 16.0F) * this.unit, this.y + (rowY + 4.0F) * this.unit, srvBadgeW, 9.5F * this.unit, srvBg, 2.5F * this.unit);
            Fonts.sf_bold
               .draw(
                  ev.getFormattedServer(),
                  this.x + (x + 19.0F) * this.unit,
                  this.y + (rowY + 5.5F) * this.unit,
                  6.0F * this.unit,
                  isCurrent ? ColorUtil.client() : -3484442
               );
            String eventDisplayName = ev.getDisplayName();
            float titleX = (x + 20.0F) * this.unit + srvBadgeW;
            Fonts.sf_bold.draw(eventDisplayName, this.x + titleX, this.y + (rowY + 4.5F) * this.unit, 7.0F * this.unit, ColorUtil.getColor(245, this.alpha));
            float phaseX = titleX + Fonts.sf_bold.getWidth(eventDisplayName, 7.0F * this.unit) + 6.0F * this.unit;
            Fonts.sf_regular
               .draw(
                  "[" + ev.getPhaseLocalized() + "]",
                  this.x + phaseX,
                  this.y + (rowY + 5.5F) * this.unit,
                  5.5F * this.unit,
                  ColorUtil.getColor(170, this.alpha)
               );
            float btnGap = 4.0F;
            int srvNum = ev.getServerNumber();
            String anText = srvNum > 0 ? "/an" + srvNum : "Anarchy";
            float anTextW = Fonts.sf_bold.getWidth(anText, 5.5F * this.unit) / this.unit;
            float anW = Math.max(34.0F, anTextW + 8.0F);
            float anX = x + w - 10.0F - anW;
            String copyLabel = Lang.pick("Копія", "Копия", "Copy");
            float copyW = 34.0F;
            float copyX = anX - btnGap - copyW;
            float gpsW = 30.0F;
            float gpsX = copyX - btnGap - gpsW;
            String timerStr = ev.getTimerString();
            int remSec = ev.getRemainingSeconds();
            int timerCol = remSec <= 15 ? -52429 : (remSec <= 60 ? -19712 : -16718218);
            float timerW = Fonts.sf_bold.getWidth(timerStr, 7.0F * this.unit) + 8.0F * this.unit;
            float timerGap = 6.0F;
            float timerX = ((ev.hasCoordinates() ? gpsX : copyX) - timerGap - timerW / this.unit) * this.unit;
            RenderUtil.Render2D.rect(
               this.x + timerX, this.y + (rowY + 4.0F) * this.unit, timerW, 11.0F * this.unit, ColorUtil.getColor(0, 0.35F * this.alpha), 3.0F * this.unit
            );
            RenderUtil.Render2D.outline(
               this.x + timerX,
               this.y + (rowY + 4.0F) * this.unit,
               timerW,
               11.0F * this.unit,
               0.5F * this.unit,
               ColorUtil.replAlpha(timerCol, 0.4F * this.alpha),
               3.0F * this.unit
            );
            Fonts.sf_bold
               .drawCentered(
                  timerStr, this.x + timerX + timerW / 2.0F, this.y + (rowY + 5.5F) * this.unit, 7.0F * this.unit, ColorUtil.replAlpha(timerCol, this.alpha)
               );
            String coordsText = ev.hasCoordinates()
               ? "X: " + ev.x() + "  Y: " + ev.y() + "  Z: " + ev.z()
               : Lang.pick("Координати невідомі", "Координаты неизвестны", "Coordinates unknown");
            if (ev.loot() != null && !ev.loot().isEmpty()) {
               coordsText = coordsText + "  •  " + ev.loot();
            }

            Fonts.sf_regular.draw(coordsText, this.x + (x + 18.0F) * this.unit, this.y + (rowY + 17.5F) * this.unit, 5.8F * this.unit, -7695450);
            float clippedY = Math.max(y, rowY);
            float clippedH = Math.min(y + h, rowY + 28.0F) - clippedY;
            if (clippedH > 0.0F) {
               if (ev.hasCoordinates()) {
                  this.button(
                     "GPS",
                     gpsX,
                     rowY + 3.0F,
                     gpsW,
                     true,
                     () -> {
                        WayCommand way = this.wayCommand();
                        if (way != null) {
                           way.addWaypoint(
                              ev.getDisplayName() + " (" + ev.getFormattedServer() + ")", new class_243((double)ev.x(), (double)ev.y(), (double)ev.z())
                           );
                           ChatUtils.addChatMessage(
                              "§a[GPS] Маркер встановлено: §f"
                                 + ev.getDisplayName()
                                 + " §e["
                                 + ev.x()
                                 + " "
                                 + ev.y()
                                 + " "
                                 + ev.z()
                                 + "] §7("
                                 + ev.getFormattedServer()
                                 + ")"
                           );
                           this.message(
                              Lang.pick("GPS маркер встановлено на ", "GPS метка установлена на ", "GPS waypoint set to ") + ev.x() + ", " + ev.z(), false
                           );
                        }
                     },
                     5.5F,
                     true
                  );
                  this.hits.remove(this.hits.size() - 1);
                  this.hits
                     .add(
                        new ManagementPanel.Hit(
                           gpsX,
                           clippedY,
                           gpsW,
                           clippedH,
                           () -> {
                              WayCommand way = this.wayCommand();
                              if (way != null) {
                                 way.addWaypoint(
                                    ev.getDisplayName() + " (" + ev.getFormattedServer() + ")", new class_243((double)ev.x(), (double)ev.y(), (double)ev.z())
                                 );
                                 ChatUtils.addChatMessage(
                                    "§a[GPS] Маркер встановлено: §f"
                                       + ev.getDisplayName()
                                       + " §e["
                                       + ev.x()
                                       + " "
                                       + ev.y()
                                       + " "
                                       + ev.z()
                                       + "] §7("
                                       + ev.getFormattedServer()
                                       + ")"
                                 );
                                 this.message(
                                    Lang.pick("GPS маркер встановлено на ", "GPS метка установлена на ", "GPS waypoint set to ") + ev.x() + ", " + ev.z(),
                                    false
                                 );
                              }
                           }
                        )
                     );
               }

               this.button(copyLabel, copyX, rowY + 3.0F, copyW, true, () -> {
                  String toCopy = ev.hasCoordinates() ? ev.x() + " " + ev.y() + " " + ev.z() : ev.getFormattedServer() + " " + ev.getDisplayName();
                  mc.field_1774.method_1455(toCopy);
                  this.message(Lang.pick("Скопійовано: ", "Скопировано: ", "Copied: ") + toCopy, false);
               }, 5.5F, false);
               this.hits.remove(this.hits.size() - 1);
               this.hits.add(new ManagementPanel.Hit(copyX, clippedY, copyW, clippedH, () -> {
                  String toCopy = ev.hasCoordinates() ? ev.x() + " " + ev.y() + " " + ev.z() : ev.getFormattedServer() + " " + ev.getDisplayName();
                  mc.field_1774.method_1455(toCopy);
                  this.message(Lang.pick("Скопійовано: ", "Скопировано: ", "Copied: ") + toCopy, false);
               }));
               this.button(anText, anX, rowY + 3.0F, anW, true, () -> {
                  if (srvNum > 0) {
                     if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
                        mc.field_1724.field_3944.method_45730("an" + srvNum);
                     }

                     this.message(Lang.pick("Вхід на /an", "Вход на /an", "Connecting to /an") + srvNum + "...", false);
                  } else {
                     this.message(Lang.pick("Номер анархії не знайдено", "Номер анархии не найден", "Anarchy number not found"), true);
                  }
               }, 5.5F, false);
               this.hits.remove(this.hits.size() - 1);
               this.hits.add(new ManagementPanel.Hit(anX, clippedY, anW, clippedH, () -> {
                  if (srvNum > 0) {
                     if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
                        mc.field_1724.field_3944.method_45730("an" + srvNum);
                     }

                     this.message(Lang.pick("Вхід на /an", "Вход на /an", "Connecting to /an") + srvNum + "...", false);
                  } else {
                     this.message(Lang.pick("Номер анархії не знайдено", "Номер анархии не найден", "Anarchy number not found"), true);
                  }
               }));
            }
         }
      } finally {
         Scissor.disable();
      }

      if (this.maxScroll[side] > 0.0F) {
         float thumb = Math.max(12.0F, h * h / ((float)list.size() * rowHeight));
         float offset = (h - thumb) * this.scroll[side] / this.maxScroll[side];
         this.rect(x + w - 3.0F, y + offset, 2.0F, thumb, -10128756, 1.0F);
      }
   }

   private void minesList(List<FunTimeEventsService.FunTimeMine> list, float x, float y, float w, float h) {
      h = Math.max(1.0F, h);
      int side = 1;
      float rowHeight = 32.0F;
      this.rect(x, y, w, h, -15855594, 5.0F);
      this.outline(x, y, w, h, -14539212, 5.0F);
      this.maxScroll[side] = Math.max(0.0F, (float)list.size() * rowHeight - h);
      this.scroll[side] = MathUtil.clamp(this.scroll[side], 0.0F, this.maxScroll[side]);
      this.scrollRegions.add(new ManagementPanel.Hit(x, y, w, h, null));
      Scissor.enable(this.x + x * this.unit, this.y + y * this.unit, w * this.unit, h * this.unit, 2.0F);

      try {
         if (list.isEmpty()) {
            if (FunTimeEventsService.isLoading()) {
               this.text(
                  Lang.pick("Завантаження авто-шахт FunTime...", "Загрузка авто-шахт FunTime...", "Loading mines..."), x + 8.0F, y + 10.0F, 6.5F, -7695450
               );
            } else {
               this.text(Lang.pick("Немає активних авто-шахт.", "Нет активных авто-шахт.", "No active mines."), x + 8.0F, y + 10.0F, 6.5F, -7695450);
            }

            return;
         }

         int first = Math.max(0, (int)(this.scroll[side] / rowHeight));
         int last = Math.min(list.size(), first + (int)Math.ceil((double)(h / rowHeight)) + 1);

         for (int i = first; i < last; i++) {
            FunTimeEventsService.FunTimeMine mine = list.get(i);
            float rowY = y + (float)i * rowHeight + 2.0F - this.scroll[side];
            boolean isHov = MathUtil.isHovered(this.mouseX, this.mouseY, x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F);
            boolean isCurrent = mine.isCurrentServer();
            int cardBg = isCurrent
               ? (isHov ? ColorUtil.getColor(36, 44, 60, (int)(220.0F * this.alpha)) : ColorUtil.getColor(24, 30, 44, (int)(200.0F * this.alpha)))
               : (isHov ? ColorUtil.getColor(28, 32, 44, (int)(200.0F * this.alpha)) : ColorUtil.getColor(18, 20, 28, (int)(180.0F * this.alpha)));
            this.rect(x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F, cardBg, 4.0F);
            if (isCurrent) {
               this.outline(x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F, ColorUtil.replAlpha(ColorUtil.client(), 0.45F * this.alpha), 4.0F);
            } else if (isHov) {
               this.outline(x + 3.0F, rowY, w - 8.0F, rowHeight - 4.0F, ColorUtil.getColor(255, 0.12F * this.alpha), 4.0F);
            }

            float dotR = 2.5F;
            RenderUtil.Render2D.rect(
               this.x + (x + 8.0F) * this.unit,
               this.y + (rowY + 13.5F) * this.unit,
               dotR * 2.0F * this.unit,
               dotR * 2.0F * this.unit,
               ColorUtil.replAlpha(mine.getRarityColor(), 0.9F * this.alpha),
               dotR * this.unit
            );
            float srvBadgeW = Fonts.sf_bold.getWidth(mine.getFormattedServer(), 6.0F * this.unit) + 6.0F * this.unit;
            int srvBg = isCurrent ? ColorUtil.replAlpha(ColorUtil.client(), 0.25F * this.alpha) : ColorUtil.getColor(255, 0.08F * this.alpha);
            RenderUtil.Render2D.rect(this.x + (x + 16.0F) * this.unit, this.y + (rowY + 4.0F) * this.unit, srvBadgeW, 9.5F * this.unit, srvBg, 2.5F * this.unit);
            Fonts.sf_bold
               .draw(
                  mine.getFormattedServer(),
                  this.x + (x + 19.0F) * this.unit,
                  this.y + (rowY + 5.5F) * this.unit,
                  6.0F * this.unit,
                  isCurrent ? ColorUtil.client() : -3484442
               );
            float titleX = (x + 20.0F) * this.unit + srvBadgeW;
            Fonts.sf_bold.draw(mine.name(), this.x + titleX, this.y + (rowY + 4.5F) * this.unit, 7.0F * this.unit, ColorUtil.getColor(245, this.alpha));
            float rarX = titleX + Fonts.sf_bold.getWidth(mine.name(), 7.0F * this.unit) + 6.0F * this.unit;
            Fonts.sf_bold
               .draw(
                  "[" + mine.getRarityLocalized() + "]",
                  this.x + rarX,
                  this.y + (rowY + 5.5F) * this.unit,
                  5.5F * this.unit,
                  ColorUtil.replAlpha(mine.getRarityColor(), this.alpha)
               );
            float btnGap = 4.0F;
            int srvNum = mine.getServerNumber();
            String anText = srvNum > 0 ? "/an" + srvNum : "Anarchy";
            float anTextW = Fonts.sf_bold.getWidth(anText, 5.5F * this.unit) / this.unit;
            float anW = Math.max(34.0F, anTextW + 8.0F);
            float anX = x + w - 10.0F - anW;
            String warpLabel = Lang.pick("/mine", "/mine", "/mine");
            float warpW = 34.0F;
            float warpX = anX - btnGap - warpW;
            String timerStr = mine.getTimerString();
            int remSec = mine.getRemainingSeconds();
            int timerCol = remSec <= 20 ? -52429 : (remSec <= 60 ? -19712 : -16718218);
            float timerW = Fonts.sf_bold.getWidth(timerStr, 7.0F * this.unit) + 8.0F * this.unit;
            float timerGap = 6.0F;
            float timerX = (warpX - timerGap - timerW / this.unit) * this.unit;
            RenderUtil.Render2D.rect(
               this.x + timerX, this.y + (rowY + 4.0F) * this.unit, timerW, 11.0F * this.unit, ColorUtil.getColor(0, 0.35F * this.alpha), 3.0F * this.unit
            );
            RenderUtil.Render2D.outline(
               this.x + timerX,
               this.y + (rowY + 4.0F) * this.unit,
               timerW,
               11.0F * this.unit,
               0.5F * this.unit,
               ColorUtil.replAlpha(timerCol, 0.4F * this.alpha),
               3.0F * this.unit
            );
            Fonts.sf_bold
               .drawCentered(
                  timerStr, this.x + timerX + timerW / 2.0F, this.y + (rowY + 5.5F) * this.unit, 7.0F * this.unit, ColorUtil.replAlpha(timerCol, this.alpha)
               );
            String subInfo = Lang.pick("Авто-оновлення копальні", "Авто-обновление шахты", "Mine refresh");
            if (!mine.getNextRarityLocalized().isEmpty()) {
               subInfo = subInfo + "  •  " + Lang.pick("Наступна: ", "Следующая: ", "Next: ") + mine.getNextRarityLocalized();
            }

            Fonts.sf_regular.draw(subInfo, this.x + (x + 18.0F) * this.unit, this.y + (rowY + 17.5F) * this.unit, 5.8F * this.unit, -7695450);
            float clippedY = Math.max(y, rowY);
            float clippedH = Math.min(y + h, rowY + 28.0F) - clippedY;
            if (clippedH > 0.0F) {
               this.button(warpLabel, warpX, rowY + 3.0F, warpW, true, () -> {
                  if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
                     mc.field_1724.field_3944.method_45730("mine");
                     this.message(Lang.pick("Телепортація на /mine...", "Телепортация на /mine...", "Teleporting to /mine..."), false);
                  }
               }, 5.5F, true);
               this.hits.remove(this.hits.size() - 1);
               this.hits.add(new ManagementPanel.Hit(warpX, clippedY, warpW, clippedH, () -> {
                  if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
                     mc.field_1724.field_3944.method_45730("mine");
                     this.message(Lang.pick("Телепортація на /mine...", "Телепортация на /mine...", "Teleporting to /mine..."), false);
                  }
               }));
               this.button(anText, anX, rowY + 3.0F, anW, true, () -> {
                  if (srvNum > 0) {
                     if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
                        mc.field_1724.field_3944.method_45730("an" + srvNum);
                     }

                     this.message(Lang.pick("Вхід на /an", "Вход на /an", "Connecting to /an") + srvNum + "...", false);
                  } else {
                     this.message(Lang.pick("Номер анархії не знайдено", "Номер анархии не найден", "Anarchy number not found"), true);
                  }
               }, 5.5F, false);
               this.hits.remove(this.hits.size() - 1);
               this.hits.add(new ManagementPanel.Hit(anX, clippedY, anW, clippedH, () -> {
                  if (srvNum > 0) {
                     if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
                        mc.field_1724.field_3944.method_45730("an" + srvNum);
                     }

                     this.message(Lang.pick("Вхід на /an", "Вход на /an", "Connecting to /an") + srvNum + "...", false);
                  } else {
                     this.message(Lang.pick("Номер анархії не знайдено", "Номер анархии не найден", "Anarchy number not found"), true);
                  }
               }));
            }
         }
      } finally {
         Scissor.disable();
      }

      if (this.maxScroll[side] > 0.0F) {
         float thumb = Math.max(12.0F, h * h / ((float)list.size() * rowHeight));
         float offset = (h - thumb) * this.scroll[side] / this.maxScroll[side];
         this.rect(x + w - 3.0F, y + offset, 2.0F, thumb, -10128756, 1.0F);
      }
   }

   private void refreshGps() {
      this.gpsPoints.clear();
      WayCommand way = this.wayCommand();
      if (way != null) {
         this.gpsPoints.addAll(way.getWaypoints());
         this.gpsPoints.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
      }

      this.hits.clear();
   }

   private WayCommand wayCommand() {
      for (Command cmd : Client.get().commandManager().getCommands().values()) {
         if (cmd instanceof WayCommand) {
            return (WayCommand)cmd;
         }
      }

      return null;
   }

   private void addGpsHere() {
      if (mc.field_1724 != null) {
         String wpName = this.name.value.trim();
         WayCommand way = this.wayCommand();
         if (way != null && !wpName.isEmpty()) {
            way.addWaypoint(wpName, mc.field_1724.method_73189());
            this.name.set("");
            this.refreshGps();
            this.message("Точка добавлена: " + wpName + ".", false);
         }
      }
   }

   private void removeGps(WayCommand.Waypoint wp) {
      WayCommand way = this.wayCommand();
      if (way != null) {
         if (way.removeWaypoint(wp.name())) {
            this.refreshGps();
            this.message("Точка удалена: " + wp.name() + ".", false);
         }
      }
   }

   private void gpsNavigate(WayCommand.Waypoint wp) {
      GpsHud.setTarget((float)wp.pos().field_1352, (float)wp.pos().field_1350);
      GpsHud module = Client.get().moduleManager().get(GpsHud.class);
      if (module != null && !module.isEnabled()) {
         module.setEnabled(true);
      }

      this.message("GPS → " + wp.name() + " (" + (int)wp.pos().field_1352 + ", " + (int)wp.pos().field_1350 + ")", false);
   }

   private List<String> filtered(List<String> values) {
      String query = this.filter.value.trim().toLowerCase(Locale.ROOT);
      return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).contains(query)).toList();
   }

   private void list(List<String> values, float x, float y, float w, float h, int side, boolean friends) {
      h = Math.max(1.0F, h);
      this.rect(x, y, w, h, -15855594, 5.0F);
      this.outline(x, y, w, h, -14539212, 5.0F);
      this.maxScroll[side] = Math.max(0.0F, (float)values.size() * 26.0F - h);
      this.scroll[side] = MathUtil.clamp(this.scroll[side], 0.0F, this.maxScroll[side]);
      this.scrollRegions.add(new ManagementPanel.Hit(x, y, w, h, null));
      Scissor.enable(this.x + x * this.unit, this.y + y * this.unit, w * this.unit, h * this.unit, 2.0F);

      try {
         if (values.isEmpty()) {
            String empty = !this.filter.value.isBlank()
               ? "Нет совпадений"
               : (
                  friends
                     ? (side == 0 && this.connection == null ? "Нет подключения" : (side == 0 ? "Никого нет онлайн" : "Нет сохранённых друзей"))
                     : "Нет конфигов. Создайте выше."
               );
            this.text(empty, x + 8.0F, y + 10.0F, 6.5F, -7695450);
         }

         int first = Math.max(0, (int)(this.scroll[side] / 26.0F));
         int last = Math.min(values.size(), first + (int)Math.ceil((double)(h / 26.0F)) + 1);

         for (int i = first; i < last; i++) {
            String value = values.get(i);
            float rowY = y + (float)(i * 26) + 2.0F - this.scroll[side];
            boolean chosen = !friends && value.equals(this.selected);
            boolean member = friends && Client.get().friendManager().isFriend(value);
            boolean isHov = MathUtil.isHovered(this.mouseX, this.mouseY, x + 3.0F, rowY, w - 8.0F, 22.0F);
            int cardBg = chosen
               ? ColorUtil.getColor(36, 40, 54, (int)(220.0F * this.alpha))
               : (isHov ? ColorUtil.getColor(28, 32, 44, (int)(200.0F * this.alpha)) : ColorUtil.getColor(18, 20, 28, (int)(180.0F * this.alpha)));
            this.rect(x + 3.0F, rowY, w - 8.0F, 22.0F, cardBg, 4.0F);
            if (chosen) {
               this.outline(x + 3.0F, rowY, w - 8.0F, 22.0F, ColorUtil.client(), 4.0F);
               RenderUtil.Render2D.glow(
                  this.x + (x + 3.0F) * this.unit,
                  this.y + rowY * this.unit,
                  (w - 8.0F) * this.unit,
                  22.0F * this.unit,
                  ColorUtil.replAlpha(ColorUtil.client(), 0.16F * this.alpha),
                  4.0F * this.unit,
                  6.0F,
                  1.0F
               );
            } else if (isHov) {
               this.outline(x + 3.0F, rowY, w - 8.0F, 22.0F, ColorUtil.getColor(255, 0.12F * this.alpha), 4.0F);
            }

            if (!friends) {
               float dotR = 2.5F;
               RenderUtil.Render2D.rect(
                  this.x + (x + 9.0F) * this.unit,
                  this.y + (rowY + 9.0F) * this.unit,
                  dotR * 2.0F * this.unit,
                  dotR * 2.0F * this.unit,
                  ColorUtil.replAlpha(ColorUtil.client(), (chosen ? 1.0F : 0.4F) * this.alpha),
                  dotR * this.unit
               );
            } else {
               float avatarR = 4.0F;
               RenderUtil.Render2D.rect(
                  this.x + (x + 8.0F) * this.unit,
                  this.y + (rowY + 7.0F) * this.unit,
                  avatarR * 2.0F * this.unit,
                  avatarR * 2.0F * this.unit,
                  ColorUtil.getColor(member ? 96 : 45, member ? 255 : 55, member ? 198 : 70, (int)(200.0F * this.alpha)),
                  avatarR * this.unit
               );
            }

            String label = !friends && value.equals("auto") ? value + " [autosave]" : value;
            float actionWidth = side == 1 ? 40.0F : 22.0F;
            Fonts.sf_medium
               .drawFadingText(
                  label,
                  this.x + (x + (float)(friends ? 20 : 18)) * this.unit,
                  this.y + (rowY + 7.5F) * this.unit,
                  (w - (friends ? actionWidth + 28.0F : 26.0F)) * this.unit,
                  this.color(chosen ? -1 : -2564372),
                  7.0F * this.unit
               );
            if (chosen && !friends) {
               float indR = 3.0F;
               RenderUtil.Render2D.rect(
                  this.x + (x + w - 16.0F) * this.unit,
                  this.y + (rowY + 8.0F) * this.unit,
                  indR * 2.0F * this.unit,
                  indR * 2.0F * this.unit,
                  ColorUtil.replAlpha(ColorUtil.client(), this.alpha),
                  indR * this.unit
               );
            }

            float clippedY = Math.max(y, rowY);
            float clippedH = Math.min(y + h, rowY + 22.0F) - clippedY;
            if (!(clippedH <= 0.0F)) {
               if (friends) {
                  float bx = x + w - actionWidth - 8.0F;
                  this.button(
                     side == 1 ? "Удалить" : (member ? "-" : "+"),
                     bx,
                     rowY + 3.0F,
                     actionWidth,
                     true,
                     () -> this.setFriend(value, side == 0 && !member),
                     side == 1 ? 6.5F : 8.0F,
                     side == 0 && !member
                  );
                  this.hits.remove(this.hits.size() - 1);
                  this.hits.add(new ManagementPanel.Hit(bx, clippedY, actionWidth, clippedH, () -> this.setFriend(value, side == 0 && !member)));
               } else {
                  this.hits.add(new ManagementPanel.Hit(x + 3.0F, clippedY, w - 8.0F, clippedH, () -> this.selected = value));
               }
            }
         }
      } finally {
         Scissor.disable();
      }

      if (this.maxScroll[side] > 0.0F) {
         float thumb = Math.max(12.0F, h * h / ((float)values.size() * 26.0F));
         float offset = (h - thumb) * this.scroll[side] / this.maxScroll[side];
         this.rect(x + w - 3.0F, y + offset, 2.0F, thumb, -10128756, 1.0F);
      }
   }

   private void input(ManagementPanel.TextInput input, String placeholder, float x, float y, float w) {
      boolean isHover = MathUtil.isHovered(this.mouseX, this.mouseY, x, y, w, 18.0F);
      boolean isFocus = this.focused == input;
      int bgCol = isFocus ? -15460576 : (isHover ? -15592420 : -15855593);
      this.rect(x, y, w, 18.0F, bgCol, 4.0F);
      int borderCol = isFocus ? ColorUtil.client() : (isHover ? -12235166 : -14407112);
      this.outline(x, y, w, 18.0F, borderCol, 4.0F);
      if (isFocus) {
         RenderUtil.Render2D.glow(
            this.x + x * this.unit,
            this.y + y * this.unit,
            w * this.unit,
            18.0F * this.unit,
            ColorUtil.replAlpha(ColorUtil.client(), 0.15F * this.alpha),
            4.0F * this.unit,
            6.0F,
            1.0F
         );
      }

      this.hits.add(new ManagementPanel.Hit(x, y, w, 18.0F, () -> this.focused = input));
      Scissor.enable(this.x + (x + 6.0F) * this.unit, this.y + y * this.unit, (w - 12.0F) * this.unit, 18.0F * this.unit, 2.0F);

      try {
         float caretWidth = Fonts.sf_medium.getWidth(input.value.substring(0, input.cursor), 7.0F);
         float offset = this.focused == input ? Math.max(0.0F, caretWidth - w + 16.0F) : 0.0F;
         if (this.focused == input && input.cursor != input.anchor) {
            float start = Fonts.sf_medium.getWidth(input.value.substring(0, Math.min(input.cursor, input.anchor)), 7.0F);
            float end = Fonts.sf_medium.getWidth(input.value.substring(0, Math.max(input.cursor, input.anchor)), 7.0F);
            this.rect(x + 6.0F + start - offset, y + 3.0F, end - start, 12.0F, -12693395);
         }

         this.text(input.value.isEmpty() ? placeholder : input.value, x + 6.0F - offset, y + 5.5F, 7.0F, input.value.isEmpty() ? -8748138 : -920843);
         if (this.focused == input && System.currentTimeMillis() / 500L % 2L == 0L) {
            this.rect(x + 6.0F + caretWidth - offset, y + 3.5F, 0.8F, 11.0F, ColorUtil.client());
         }
      } finally {
         Scissor.disable();
      }
   }

   private void button(String label, float x, float y, float w, boolean enabled, Runnable action) {
      this.button(label, x, y, w, enabled, action, 7.0F, false);
   }

   private void button(String label, float x, float y, float w, boolean enabled, Runnable action, float fontSize) {
      this.button(label, x, y, w, enabled, action, fontSize, false);
   }

   private void button(String label, float x, float y, float w, boolean enabled, Runnable action, float fontSize, boolean accent) {
      boolean hover = enabled && MathUtil.isHovered(this.mouseX, this.mouseY, x, y, w, 17.0F);
      int bg = !enabled
         ? -14934490
         : (
            hover
               ? (accent ? ColorUtil.replAlpha(ColorUtil.client(), 0.35F * this.alpha) : -13815230)
               : (accent ? ColorUtil.replAlpha(ColorUtil.client(), 0.18F * this.alpha) : -14671058)
         );
      this.rect(x, y, w, 17.0F, bg, 4.0F);
      int border = accent ? ColorUtil.replAlpha(ColorUtil.client(), (hover ? 0.9F : 0.5F) * this.alpha) : (hover ? -10984840 : -13288372);
      this.outline(x, y, w, 17.0F, border, 4.0F);
      if (hover && enabled) {
         RenderUtil.Render2D.glow(
            this.x + x * this.unit,
            this.y + y * this.unit,
            w * this.unit,
            17.0F * this.unit,
            accent ? ColorUtil.replAlpha(ColorUtil.client(), 0.2F * this.alpha) : ColorUtil.getColor(0, 0.08F * this.alpha),
            4.0F * this.unit,
            6.0F,
            1.0F
         );
      }

      int textCol = enabled ? (accent ? -1 : (hover ? -1 : -2959130)) : -10130049;
      Fonts.sf_medium
         .drawCentered(
            label, this.x + (x + w / 2.0F) * this.unit, this.y + (y + (17.0F - fontSize) / 2.0F - 1.0F) * this.unit, fontSize * this.unit, this.color(textCol)
         );
      if (enabled) {
         this.hits.add(new ManagementPanel.Hit(x, y, w, 17.0F, action));
      }
   }

   private void drawStatus() {
      String defMsg = this.tab == ManagementPanel.Tab.CONFIGS
         ? Lang.pick("Оберіть конфіг для керування.", "Выберите конфиг для управления.", "Select config to manage.")
         : (
            this.tab == ManagementPanel.Tab.GPS
               ? Lang.pick(
                  "GPS встановлює мітку до точки; Видалити прибирає вейпоінт.",
                  "GPS ставит стрелку к точке; Удалить убирает вейпоинт.",
                  "GPS points arrow; Delete removes waypoint."
               )
               : (
                  this.tab == ManagementPanel.Tab.EVENTS
                     ? Lang.pick(
                        "Клікніть GPS, Копіювати або /an для переходу на сервер.",
                        "Кликните GPS, Копировать или /an для перехода.",
                        "Click GPS, Copy or /an to join server."
                     )
                     : Lang.pick("+/- додає або видаляє з друзів.", "+/- добавляет или удаляет из друзей.", "+/- adds or removes from friends.")
               )
         );
      String value = this.status.isEmpty() ? defMsg : this.status;
      float y = this.height - 28.0F;

      for (int line = 0; line < 3 && !value.isEmpty(); line++) {
         int end = value.length();

         while (end > 1 && Fonts.sf_medium.getWidth(value.substring(0, end), 6.0F) > this.width - 16.0F) {
            end--;
         }

         this.text(value.substring(0, end), 8.0F, y + (float)(line * 8), 6.0F, this.error ? -23645 : -7695450);
         value = value.substring(end).stripLeading();
      }
   }

   private void confirm(boolean delete) {
      if (this.selected != null) {
         this.pendingName = this.selected;
         this.pendingDelete = delete;
         this.focused = null;
         this.hits.clear();
      }
   }

   private void drawConfirmation() {
      this.hits.clear();
      RenderUtil.Render2D.rect(this.x, this.y, this.width * this.unit, this.height * this.unit, ColorUtil.getColor(0, 0.7F * this.alpha));
      float cardW = Math.min(this.width - 40.0F, 240.0F);
      float cardH = 100.0F;
      float cardX = (this.width - cardW) / 2.0F;
      float cardY = (this.height - cardH) / 2.0F;
      RenderUtil.Render2D.glow(
         this.x + cardX * this.unit,
         this.y + cardY * this.unit,
         cardW * this.unit,
         cardH * this.unit,
         ColorUtil.getColor(0, 0.25F * this.alpha),
         8.0F * this.unit,
         12.0F,
         1.0F
      );
      this.rect(cardX, cardY, cardW, cardH, -15263196, 7.0F);
      this.outline(cardX, cardY, cardW, cardH, ColorUtil.client(), 7.0F);
      String title = this.pendingDelete
         ? Lang.pick("Видалити конфіг?", "Удалить конфиг?", "Delete config?")
         : Lang.pick("Перезаписати конфіг?", "Перезаписать конфиг?", "Overwrite config?");
      this.text(title, cardX + 12.0F, cardY + 12.0F, 8.5F, -920843);
      Fonts.sf_medium
         .drawFadingText(
            this.pendingName,
            this.x + (cardX + 12.0F) * this.unit,
            this.y + (cardY + 28.0F) * this.unit,
            (cardW - 24.0F) * this.unit,
            ColorUtil.replAlpha(ColorUtil.client(), this.alpha),
            7.5F * this.unit
         );
      String desc = this.pendingDelete
         ? Lang.pick("Ця дія безповоротно видалить файл.", "Это действие безвозвратно удалит файл.", "This action permanently deletes file.")
         : Lang.pick("Файл буде перезаписано поточними налаштуваннями.", "Файл будет перезаписан текущими настройками.", "File will be overwritten.");
      this.text(desc, cardX + 12.0F, cardY + 44.0F, 6.0F, -5195576);
      float btnW = (cardW - 32.0F) / 2.0F;
      float btnY = cardY + cardH - 26.0F;
      this.button(Lang.pick("Скасувати", "Отмена", "Cancel"), cardX + 12.0F, btnY, btnW, true, () -> this.pendingName = null, 7.0F, false);
      this.button(
         this.pendingDelete ? Lang.pick("Видалити", "Удалить", "Delete") : Lang.pick("Перезаписати", "Перезаписать", "Overwrite"),
         cardX + 20.0F + btnW,
         btnY,
         btnW,
         true,
         this::applyConfirmation,
         7.0F,
         !this.pendingDelete
      );
   }

   private void applyConfirmation() {
      String target = this.pendingName;
      boolean delete = this.pendingDelete;
      this.pendingName = null;
      this.perform(() -> {
         if (delete) {
            Client.get().configManager().deleteChecked(target);
         } else {
            Client.get().configManager().saveChecked(target, true);
         }

         this.refreshConfigsChecked();
         this.message((delete ? "Deleted " : "Overwrote ") + target + ".", false);
      });
   }

   private void refreshConfigsChecked() throws IOException {
      List<String> values = Client.get().configManager().listConfigsChecked();
      this.configs.clear();
      this.configs.addAll(values);
      if (!this.configs.contains(this.selected)) {
         this.selected = null;
      }

      this.hits.clear();
   }

   private void refreshConfigs() {
      this.perform(() -> {
         this.refreshConfigsChecked();
         this.message("Found " + this.configs.size() + " configs. Auto is maintained by autosave.", false);
      });
   }

   private void createConfig() {
      this.perform(() -> {
         String value = ConfigManager.validateName(this.name.value);
         Client.get().configManager().saveChecked(value, false);
         this.refreshConfigsChecked();
         this.selected = value;
         this.scroll[0] = (float)this.configs.indexOf(value) * 22.0F;
         this.filter.set("");
         this.name.set("");
         this.message("Created " + value + ".", false);
      });
   }

   private void loadConfig() {
      String value = this.selected;
      this.perform(() -> {
         try {
            Client.get().configManager().loadChecked(value);
         } catch (RuntimeException | IOException var3) {
            throw new IOException("Load failed; some settings may have changed. " + var3.getMessage(), var3);
         }

         this.message("Loaded " + value + ".", false);
      });
   }

   private void exportConfig() {
      String value = this.selected;
      this.perform(() -> {
         String json = Client.get().configManager().exportChecked(value);
         mc.field_1774.method_1455(json);
         this.message("Copied " + value + " to clipboard.", false);
      });
   }

   private void importConfig() {
      String value = this.name.value.trim();
      String json = mc.field_1774.method_1460();
      this.perform(() -> {
         if (json != null && !json.isBlank()) {
            Client.get().configManager().importChecked(ConfigManager.validateName(value), json);
            this.refreshConfigsChecked();
            this.selected = value;
            this.name.set("");
            this.message("Imported " + value + ".", false);
         } else {
            throw new IOException("Clipboard is empty.");
         }
      });
   }

   private void openFolder() {
      this.perform(() -> {
         Path folder = Client.get().configManager().getConfigDir();
         Files.createDirectories(folder);
         class_156.method_668().method_672(folder.toFile());
         this.message("Opened " + folder + ".", false);
      });
   }

   private void refreshFriends() {
      this.online.clear();
      this.connection = mc.field_1724 == null ? null : mc.field_1724.field_3944;
      Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      if (this.connection != null) {
         for (class_640 entry : mc.field_1724.field_3944.method_2880()) {
            if (entry.method_2966() != null && entry.method_2966().name() != null && !entry.method_2966().name().isBlank()) {
               names.add(entry.method_2966().name());
            }
         }
      }

      this.online.addAll(names);
      this.savedFriends.clear();
      this.savedFriends.addAll(Client.get().friendManager().getFriends());
      this.savedFriends.sort(String.CASE_INSENSITIVE_ORDER);
      this.nextPlayersRefresh = System.currentTimeMillis() + 500L;
      this.hits.clear();
   }

   private void setFriend(String player, boolean present) {
      this.perform(() -> {
         Client.get().friendManager().setFriendChecked(player, present);
         this.refreshFriends();
         this.message((present ? "Added " : "Removed ") + player + (present ? " as a friend." : " from friends."), false);
      });
   }

   private void perform(ManagementPanel.Operation action) {
      try {
         action.run();
      } catch (RuntimeException | IOException var3) {
         this.message(var3.getMessage() == null ? var3.getClass().getSimpleName() : var3.getMessage(), true);
      }

      this.hits.clear();
   }

   private void message(String value, boolean error) {
      this.status = value.replace('\n', ' ').replace('\r', ' ');
      this.error = error;
   }

   public boolean click(float mx, float my, int button) {
      if (!this.active()) {
         return false;
      } else if (this.tab == ManagementPanel.Tab.THEMES) {
         return this.themeSettings.handleClick(mx, my, button);
      } else {
         mx = (mx - this.x) / this.unit;
         my = (my - this.y) / this.unit;
         boolean modal = this.hasConfirmation();
         boolean inside = MathUtil.isHovered(mx, my, 0.0F, 0.0F, this.width, this.height);
         this.focused = null;
         if (button == 0) {
            for (int i = this.hits.size() - 1; i >= 0; i--) {
               ManagementPanel.Hit hit = this.hits.get(i);
               if (hit.contains(mx, my)) {
                  hit.action.run();
                  this.hits.clear();
                  return true;
               }
            }
         }

         return inside || modal;
      }
   }

   public boolean scrolled(float mx, float my, double amount) {
      if (!this.active()) {
         return false;
      } else if (this.tab == ManagementPanel.Tab.THEMES) {
         return this.themeSettings.handleScroll(mx, my, amount);
      } else if (this.hasConfirmation()) {
         return true;
      } else {
         mx = (mx - this.x) / this.unit;
         my = (my - this.y) / this.unit;

         for (int i = 0; i < this.scrollRegions.size(); i++) {
            if (this.scrollRegions.get(i).contains(mx, my)) {
               this.scroll[i] = MathUtil.clamp(this.scroll[i] - (float)amount * 22.0F, 0.0F, this.maxScroll[i]);
               this.hits.clear();
               return true;
            }
         }

         return false;
      }
   }

   public boolean escape() {
      if (this.hasConfirmation()) {
         this.pendingName = null;
         this.hits.clear();
         return true;
      } else if (this.focused != null) {
         this.focused = null;
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(class_11908 input) {
      if (!this.active()) {
         return false;
      } else {
         int key = input.comp_4795();
         if (key == 256) {
            return this.escape();
         } else if (this.hasConfirmation()) {
            return true;
         } else if (this.tab == ManagementPanel.Tab.THEMES) {
            return false;
         } else if (key == 258) {
            if (this.tab == ManagementPanel.Tab.EVENTS) {
               this.focused = this.focused != this.tokenInput ? this.tokenInput : this.filter;
            } else {
               this.focused = this.tab == ManagementPanel.Tab.CONFIGS && this.focused != this.name ? this.name : this.filter;
            }

            return true;
         } else if (this.focused == null) {
            return false;
         } else {
            if (key != 257 && key != 335) {
               this.focused.key(input);
               if (this.focused == this.filter) {
                  this.scroll[0] = this.scroll[1] = 0.0F;
               }
            } else if (this.focused == this.name) {
               this.createConfig();
            } else if (this.focused == this.tokenInput) {
               FunTimeEventsService.setClientToken(this.tokenInput.value);
               this.message("Токен сохранён!", false);
               this.focused = null;
            } else {
               this.focused = null;
            }

            return true;
         }
      }
   }

   public boolean charTyped(class_11905 input) {
      if (this.hasConfirmation()) {
         return true;
      } else if (this.focused == null) {
         return false;
      } else {
         if (input.method_74227()) {
            this.focused.insert(input.method_74226());
         }

         if (this.focused == this.filter) {
            this.scroll[0] = this.scroll[1] = 0.0F;
         }

         return true;
      }
   }

   private int color(int color) {
      return ColorUtil.multAlpha(color, this.alpha);
   }

   private void rect(float x, float y, float w, float h, int color) {
      this.rect(x, y, w, h, color, 4.0F);
   }

   private void rect(float x, float y, float w, float h, int color, float r) {
      RenderUtil.Render2D.rect(this.x + x * this.unit, this.y + y * this.unit, w * this.unit, h * this.unit, this.color(color), r * this.unit);
   }

   private void outline(float x, float y, float w, float h, int color) {
      this.outline(x, y, w, h, color, 4.0F);
   }

   private void outline(float x, float y, float w, float h, int color, float r) {
      RenderUtil.Render2D.outline(
         this.x + x * this.unit, this.y + y * this.unit, w * this.unit, h * this.unit, 0.5F * this.unit, this.color(color), r * this.unit
      );
   }

   private void text(String text, float x, float y, float size, int color) {
      Fonts.sf_medium.draw(text, this.x + x * this.unit, this.y + y * this.unit, size * this.unit, this.color(color));
   }

   static {
      try {
         Class.forName(ManagementPanel.Hit.class.getName());
         Class.forName(ManagementPanel.Tab.class.getName());
      } catch (Throwable var1) {
      }
   }

   private static record Hit(float x, float y, float w, float h, Runnable action) {
      boolean contains(float mx, float my) {
         return MathUtil.isHovered(mx, my, this.x, this.y, this.w, this.h);
      }
   }

   private interface Operation {
      void run() throws IOException;
   }

   public static enum Tab {
      CONFIGS("Configs", "Конфіги", "Конфигурации"),
      THEMES("Themes", "Теми", "Темы"),
      FRIENDS("Friends", "Друзі", "Друзья"),
      GPS("GPS", "GPS", "GPS"),
      EVENTS("Events", "Івенти", "Ивенты");

      public final String label;
      public final String labelUa;
      public final String labelRu;

      private Tab(String label) {
         this(label, label, label);
      }

      private Tab(String label, String labelUa, String labelRu) {
         this.label = label;
         this.labelUa = labelUa;
         this.labelRu = labelRu;
      }

      public String getDisplayName() {
         return Lang.pick(this.labelUa, this.labelRu, this.label);
      }
   }

   private static final class TextInput {
      private final int limit;
      private String value = "";
      private int cursor;
      private int anchor;

      TextInput(int limit) {
         this.limit = limit;
      }

      void set(String value) {
         this.value = value;
         this.cursor = this.anchor = value.length();
      }

      void insert(String text) {
         text = text.codePoints()
            .filter(c -> !Character.isISOControl(c))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
         int from = Math.min(this.cursor, this.anchor);
         int to = Math.max(this.cursor, this.anchor);
         int available = this.limit - this.value.length() + to - from;
         if (text.length() > available) {
            int end = Math.max(0, available);
            if (end > 0 && Character.isHighSurrogate(text.charAt(end - 1))) {
               end--;
            }

            text = text.substring(0, end);
         }

         this.value = this.value.substring(0, from) + text + this.value.substring(to);
         this.cursor = this.anchor = from + text.length();
      }

      void key(class_11908 input) {
         int key = input.comp_4795();
         boolean control = (input.comp_4797() & 10) != 0;
         boolean shift = (input.comp_4797() & 1) != 0;
         if (control && key == 65) {
            this.anchor = 0;
            this.cursor = this.value.length();
         } else if (control && key == 86) {
            this.insert(IMinecraft.mc.field_1774.method_1460());
         } else if (!control || key != 67 && key != 88) {
            if (key != 259 && key != 261) {
               if (key == 263 || key == 262 || key == 268 || key == 269) {
                  if (key == 268) {
                     this.cursor = 0;
                  } else if (key == 269) {
                     this.cursor = this.value.length();
                  } else if (!shift && this.cursor != this.anchor) {
                     this.cursor = key == 263 ? Math.min(this.cursor, this.anchor) : Math.max(this.cursor, this.anchor);
                  } else if (key == 263 && this.cursor > 0) {
                     this.cursor = this.value.offsetByCodePoints(this.cursor, -1);
                  } else if (key == 262 && this.cursor < this.value.length()) {
                     this.cursor = this.value.offsetByCodePoints(this.cursor, 1);
                  }

                  if (!shift) {
                     this.anchor = this.cursor;
                  }
               }
            } else {
               if (this.cursor == this.anchor) {
                  if (key == 259 && this.cursor > 0) {
                     this.anchor = this.value.offsetByCodePoints(this.cursor, -1);
                  }

                  if (key == 261 && this.cursor < this.value.length()) {
                     this.anchor = this.value.offsetByCodePoints(this.cursor, 1);
                  }
               }

               this.insert("");
            }
         } else if (this.cursor != this.anchor) {
            IMinecraft.mc.field_1774.method_1455(this.value.substring(Math.min(this.cursor, this.anchor), Math.max(this.cursor, this.anchor)));
            if (key == 88) {
               this.insert("");
            }
         }
      }
   }
}
