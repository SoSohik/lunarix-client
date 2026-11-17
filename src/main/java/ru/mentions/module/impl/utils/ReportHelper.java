package ru.mentions.module.impl.utils;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2815;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_476;
import net.minecraft.class_7439;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.api.settings.impl.BindSetting;
import ru.mentions.module.api.settings.impl.BooleanSetting;
import ru.mentions.module.api.settings.impl.ColorSetting;
import ru.mentions.module.api.settings.impl.SliderSetting;
import ru.mentions.utils.colors.ColorUtil;
import ru.mentions.utils.math.ChatUtils;
import ru.mentions.utils.other.Instance;
import ru.mentions.utils.render.RenderUtil;
import ru.mentions.utils.render.font.Fonts;

@ModuleInfo(
   name = "Report Helper",
   desc = "Очередь репортов и подсветка игроков",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class ReportHelper extends Module {
   private static final Pattern NICK_PATTERN = Pattern.compile("(?i)\\b[A-Za-z0-9_]{3,16}\\b");
   private static final Pattern REPORT_TITLE_PATTERN = Pattern.compile(
      "(?iu)(?:жалоба\\s+на|репорт\\s+на|report\\s+(?:on|for)|complaint\\s+(?:on|for))\\s+([A-Za-z0-9_]{3,16})"
   );
   private static final Pattern WAIT_SECONDS_PATTERN = Pattern.compile("(?iu)подождать:?\\s*(\\d+)\\s*сек");
   private static final long AUTO_LIST_INTERVAL_MS = 60000L;
   private static final long AUTO_LIST_PAGE_TIMEOUT_MS = 2500L;
   private static final int MAX_AUTO_LIST_PAGES = 10;
   public final BindSetting reportBind = new BindSetting(this, "Бинд репорта");
   public final ColorSetting reportColor = new ColorSetting(this, "Цвет подсветки", -43691);
   public final SliderSetting delaySeconds = new SliderSetting(this, "Задержка репорта", 31.0F, 31.0F, 90.0F, 1.0F);
   public final BooleanSetting hud = new BooleanSetting(this, "Худ", true);
   public final BooleanSetting autoCheckList = new BooleanSetting(this, "Проверять список", false);
   private final Deque<String> reportQueue = new ArrayDeque<>();
   private final Set<String> queuedNames = new LinkedHashSet<>();
   private final Set<String> reportedNames = new LinkedHashSet<>();
   private final Set<String> listNames = new LinkedHashSet<>();
   private String lastAttackedName;
   private String waitingName;
   private long waitingSince;
   private long lastListScan;
   private long nextReportAllowedAt;
   private long lastAutoListRequest;
   private long autoListRequestedAt;
   private boolean autoListScreen;
   private int autoListPagesClicked;
   private long autoListPageClickedAt;
   private String autoListLastPageSignature = "";
   private String lastFailedName;
   private static final RenderPipeline CRYSTAL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("client", "report_crystal"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27379)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final class_1921 CRYSTAL_LAYER = class_1921.method_75940(
      "report_crystal", class_12247.method_75927(CRYSTAL_PIPELINE).method_75929(4096).method_75938()
   );
   private final class_9799 crystalAllocator = new class_9799(16384);

   public static ReportHelper get() {
      return Instance.get(ReportHelper.class);
   }

   public static boolean isAutoListActive() {
      try {
         ReportHelper helper = get();
         return helper != null && helper.isEnabled() && helper.autoListScreen;
      } catch (Exception var1) {
         return false;
      }
   }

   public static boolean isReported(class_1297 entity) {
      return !(entity instanceof class_1657 player) ? false : isReportedName(player.method_5477().getString());
   }

   public static boolean isReportedName(String name) {
      try {
         ReportHelper helper = get();
         return helper != null && helper.isEnabled() && helper.shouldHighlightName(name);
      } catch (Exception var2) {
         return false;
      }
   }

   public static int getColorForEntity(class_1297 entity, int fallback) {
      return isReported(entity) ? getReportColor(fallback) : fallback;
   }

   public static int getReportColor(int fallback) {
      try {
         ReportHelper helper = get();
         return helper != null && helper.isEnabled() ? helper.reportColor.getValue() : fallback;
      } catch (Exception var2) {
         return fallback;
      }
   }

   public static int getReportColorRgb(int fallback) {
      return getReportColor(fallback) & 16777215;
   }

   public static boolean hasHighlightedTargets() {
      try {
         ReportHelper helper = get();
         return helper != null && helper.isEnabled() && helper.getAllHighlightedCount() > 0;
      } catch (Exception var1) {
         return false;
      }
   }

   public static boolean shouldHighlight(class_1297 entity) {
      try {
         ReportHelper helper = get();
         return helper != null && helper.isEnabled() && helper.shouldHighlightEntity(entity);
      } catch (Exception var2) {
         return false;
      }
   }

   public static int getShaderColor(int fallback, Collection<class_1297> targets) {
      return hasHighlightedTargets() ? getReportColor(fallback) : fallback;
   }

   @Override
   protected void onDisable() {
      this.reportQueue.clear();
      this.queuedNames.clear();
      this.waitingName = null;
      this.autoListScreen = false;
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.autoListScreen = false;
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      class_1297 class_12972 = event.getTarget();
      class_1657 player;
      if (class_12972 instanceof class_1657 && (player = (class_1657)class_12972) != mc.field_1724) {
         this.lastAttackedName = player.method_5477().getString();
      }
   }

   @EventHandler
   public void onKey(EventKey event) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         if (this.reportBind.getValue() != -1 && event.getKey() == this.reportBind.getValue()) {
            this.queueLastAttacked();
         }
      }
   }

   @EventHandler
   public void onTick(EventTick event) {
      this.requestReportListIfNeeded();
      this.scanReportListScreen();
      this.processQueue();
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (!event.isSend()) {
         if (event.getPacket() instanceof class_7439 message) {
            this.handleServerText(message.comp_763().getString());
         }
      }
   }

   @EventHandler(
      priority = -400
   )
   public void onDisplay(EventDisplay event) {
      if (this.hud.getValue() && mc.field_1724 != null && !mc.field_1690.field_1842) {
         float targetScale = 2.0F;
         float currentScale = (float)mc.method_22683().method_4495();
         float scaleFix = targetScale / currentScale;
         float screenWidth = (float)mc.method_22683().method_4486() / scaleFix;
         String title = "Репорт Хелпер";
         String target = this.lastAttackedName == null ? "Цель: Нет" : "Цель: " + this.lastAttackedName;
         String queue = "Очередь: " + this.reportQueue.size();
         Object wait = this.waitingName == null ? this.cooldownText() : "Отправлен: " + this.waitingName;
         if (this.lastFailedName != null && this.waitingName == null && this.reportQueue.isEmpty()) {
            wait = "Ошибка: " + this.lastFailedName;
         }

         String marked = "Помечено: " + this.getAllHighlightedCount();
         float width = Math.max(118.0F, Math.max(Fonts.sf_medium.getWidth(target, 5.5F), Fonts.sf_medium.getWidth((String)wait, 5.5F)) + 16.0F);
         float x = screenWidth - width - 8.0F;
         float y = 42.0F;
         float height = 55.0F;
         RenderUtil.Blur.blur(x, y, width, height, 1.0F, 5.0F, ColorUtil.getRectColor(1.0F));
         Fonts.sf_medium.draw(title, x + 6.0F, y + 5.0F, 6.5F, this.reportColor.getValue());
         Fonts.sf_medium.draw(target, x + 6.0F, y + 18.0F, 5.5F, ColorUtil.WHITE);
         Fonts.sf_medium.draw(queue + " | " + marked, x + 6.0F, y + 29.0F, 5.5F, ColorUtil.WHITE);
         Fonts.sf_medium.draw((String)wait, x + 6.0F, y + 40.0F, 5.5F, ColorUtil.WHITE);
      }
   }

   @EventHandler
   public void onRender3D(EventRender3D e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float tickDelta = mc.method_61966().method_60637(true);
         class_243 cam = mc.field_1773.method_19418().method_71156();
         class_4587 stack = e.getMatrixStack();
         Matrix4f mat = stack.method_23760().method_23761();
         class_4598 immediate = class_4597.method_22991(this.crystalAllocator);
         int color = this.reportColor.getValue();
         int cr = color >> 16 & 0xFF;
         int cg = color >> 8 & 0xFF;
         int cb = color & 0xFF;
         long time = System.currentTimeMillis();
         boolean any = false;

         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player != mc.field_1724 && this.shouldHighlightName(player.method_5477().getString()) && !(mc.field_1724.method_5858(player) > 16384.0)) {
               class_243 pos = player.method_30950(tickDelta);
               long phase = (long)(player.method_5477().getString().hashCode() & 65535) * 7L;
               float spin = (float)((time + phase) % 4000L) / 4000.0F * (float) (Math.PI * 2);
               double bob = Math.sin((double)(time + phase) / 900.0) * 0.1;
               double cx = pos.field_1352;
               double cy = pos.field_1351 + (double)player.method_17682() + 0.85 + bob;
               double cz = pos.field_1350;
               this.drawCrystal(immediate.method_73477(CRYSTAL_LAYER), mat, cam, cx, cy, cz, spin, cr, cg, cb);
               any = true;
            }
         }

         if (any) {
            immediate.method_22993();
         }
      }
   }

   private void drawCrystal(class_4588 vc, Matrix4f mat, class_243 cam, double cx, double cy, double cz, float spin, int cr, int cg, int cb) {
      float r = 0.18F;
      float h = 0.3F;
      float x = (float)(cx - cam.field_1352);
      float y = (float)(cy - cam.field_1351);
      float z = (float)(cz - cam.field_1350);
      float[][] eq = new float[4][2];

      for (int i = 0; i < 4; i++) {
         float a = spin + (float)i * (float) (Math.PI / 2);
         eq[i][0] = x + (float)Math.cos((double)a) * r;
         eq[i][1] = z + (float)Math.sin((double)a) * r;
      }

      for (int var24 = 0; var24 < 4; var24++) {
         int j = (var24 + 1) % 4;
         float shadeTop = var24 % 2 == 0 ? 1.0F : 0.78F;
         float shadeBot = var24 % 2 == 0 ? 0.68F : 0.52F;
         this.tri(
            vc,
            mat,
            x,
            y + h,
            z,
            eq[var24][0],
            y,
            eq[var24][1],
            eq[j][0],
            y,
            eq[j][1],
            (int)((float)cr * shadeTop),
            (int)((float)cg * shadeTop),
            (int)((float)cb * shadeTop),
            210
         );
         this.tri(
            vc,
            mat,
            x,
            y - h,
            z,
            eq[j][0],
            y,
            eq[j][1],
            eq[var24][0],
            y,
            eq[var24][1],
            (int)((float)cr * shadeBot),
            (int)((float)cg * shadeBot),
            (int)((float)cb * shadeBot),
            210
         );
      }
   }

   private void tri(
      class_4588 vc, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a
   ) {
      vc.method_22918(mat, x1, y1, z1).method_1336(r, g, b, a);
      vc.method_22918(mat, x2, y2, z2).method_1336(r, g, b, a);
      vc.method_22918(mat, x3, y3, z3).method_1336(r, g, b, a);
   }

   private void queueLastAttacked() {
      if (this.lastAttackedName != null && !this.lastAttackedName.isBlank()) {
         this.queueReport(this.lastAttackedName);
      } else {
         ChatUtils.addChatMessage("Репорт Хелпер: Сначала ударь игрока.");
      }
   }

   private void queueReport(String name) {
      String normalized = this.normalizeName(name);
      if (!normalized.isEmpty() && !this.isSelf(normalized) && !this.hasReportedName(normalized) && !this.queuedNames.contains(normalized)) {
         this.reportQueue.addLast(name);
         this.queuedNames.add(normalized);
         this.lastFailedName = null;
         ChatUtils.addChatMessage("Репорт Хелпер: Добавлен в очередь " + name);
      }
   }

   private void processQueue() {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null && !this.reportQueue.isEmpty()) {
         long now = System.currentTimeMillis();
         if (this.waitingName != null) {
            if (now - this.waitingSince <= 12000L) {
               return;
            }

            this.queueFirst(this.waitingName);
            this.waitingName = null;
         }

         if (now >= this.nextReportAllowedAt) {
            String name = this.reportQueue.pollFirst();
            if (name != null) {
               this.queuedNames.remove(this.normalizeName(name));
               if (!this.hasReportedName(name) && !this.isSelf(name)) {
                  this.sendCommand("report " + name + " чит");
                  this.waitingName = name;
                  this.waitingSince = now;
               }
            }
         }
      }
   }

   private void queueFirst(String name) {
      String normalized = this.normalizeName(name);
      if (!normalized.isEmpty() && !this.isSelf(normalized) && !this.hasReportedName(normalized) && !this.queuedNames.contains(normalized)) {
         this.reportQueue.addFirst(name);
         this.queuedNames.add(normalized);
      }
   }

   private void requestReportListIfNeeded() {
      if (this.autoCheckList.getValue() && mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         if (mc.field_1755 == null && this.waitingName == null) {
            long now = System.currentTimeMillis();
            if (this.autoListScreen && now - this.autoListRequestedAt > 5000L) {
               this.autoListScreen = false;
            }

            if (now - this.lastAutoListRequest >= 60000L) {
               this.lastAutoListRequest = now;
               this.autoListRequestedAt = now;
               this.autoListScreen = true;
               this.autoListPagesClicked = 0;
               this.autoListPageClickedAt = 0L;
               this.autoListLastPageSignature = "";
               this.sendCommand("report list");
            }
         }
      }
   }

   private void scanReportListScreen() {
      if (mc.field_1755 != null && mc.field_1724 != null) {
         if (mc.field_1755 instanceof class_476) {
            class_1703 class_17032 = mc.field_1724.field_7512;
            if (mc.field_1724.field_7512 instanceof class_1707) {
               class_1707 handler = (class_1707)class_17032;
               long now = System.currentTimeMillis();
               if (now - this.lastListScan < 500L) {
                  return;
               }

               this.lastListScan = now;
               boolean foundAny = false;
               int nextSlot = -1;
               int containerSlots = handler.method_17388() * 9;

               for (int i = 0; i < Math.min(containerSlots, handler.field_7761.size()); i++) {
                  class_1735 slot = (class_1735)handler.field_7761.get(i);
                  class_1799 stack = slot.method_7677();
                  if (!stack.method_7960()) {
                     String name = this.extractReportName(stack);
                     if (name != null) {
                        foundAny = true;
                        this.listNames.add(this.normalizeName(name));
                        this.reportedNames.add(this.normalizeName(name));
                     }

                     if (nextSlot == -1 && this.isNextPageStack(stack)) {
                        nextSlot = i;
                     }
                  }
               }

               if (foundAny && this.waitingName != null && this.hasReportedName(this.waitingName)) {
                  this.waitingName = null;
               }

               if (this.autoListScreen) {
                  boolean reportScreen = this.isReportListScreen() || foundAny || nextSlot != -1;
                  if (!reportScreen) {
                     this.autoListScreen = false;
                     return;
                  }

                  String signature = this.pageSignature(handler, containerSlots);
                  if (signature.equals(this.autoListLastPageSignature)) {
                     if (now - this.autoListPageClickedAt > 2500L) {
                        this.closeAutoList(handler);
                     }

                     return;
                  }

                  if (nextSlot != -1 && this.autoListPagesClicked < 10 && mc.field_1761 != null) {
                     this.autoListLastPageSignature = signature;
                     this.autoListPagesClicked++;
                     this.autoListPageClickedAt = now;
                     this.autoListRequestedAt = now;
                     mc.field_1761.method_2906(handler.field_7763, nextSlot, 0, class_1713.field_7790, mc.field_1724);
                     return;
                  }

                  this.closeAutoList(handler);
               }

               return;
            }
         }
      }
   }

   private String pageSignature(class_1707 handler, int containerSlots) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < Math.min(containerSlots, handler.field_7761.size()); i++) {
         class_1799 stack = ((class_1735)handler.field_7761.get(i)).method_7677();
         if (!stack.method_7960()) {
            sb.append(i).append(':').append(stack.method_7964().getString()).append(';');
         }
      }

      return sb.toString();
   }

   private boolean isReportListScreen() {
      if (mc.field_1755 == null) {
         return false;
      } else {
         String title = this.clean(mc.field_1755.method_25440().getString()).toLowerCase(Locale.ROOT);
         return title.contains("репорт") || title.contains("жалоб") || title.contains("report");
      }
   }

   private String extractReportName(class_1799 stack) {
      String fromName = this.extractReportName(stack.method_7964().getString());
      if (fromName != null) {
         return fromName;
      } else {
         class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
         if (lore == null) {
            return null;
         } else {
            for (class_2561 line : lore.comp_2400()) {
               String name = this.extractReportName(line.getString());
               if (name != null) {
                  return name;
               }
            }

            return null;
         }
      }
   }

   private boolean isNextPageStack(class_1799 stack) {
      if (this.containsNextPageText(stack.method_7964().getString())) {
         return true;
      } else {
         class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
         if (lore == null) {
            return false;
         } else {
            for (class_2561 line : lore.comp_2400()) {
               if (this.containsNextPageText(line.getString())) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private boolean containsNextPageText(String raw) {
      String lower = this.clean(raw).toLowerCase(Locale.ROOT);
      return lower.contains("впер") || lower.contains("следующ") || lower.contains("next");
   }

   private void closeAutoList(class_1707 handler) {
      this.autoListScreen = false;
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         mc.field_1724.field_3944.method_52787(new class_2815(handler.field_7763));
      }

      mc.method_1507(null);
   }

   private String extractReportName(String raw) {
      String text = this.clean(raw);
      if (text.isEmpty()) {
         return null;
      } else {
         Matcher title = REPORT_TITLE_PATTERN.matcher(text);
         if (title.find()) {
            return title.group(1);
         } else {
            String lower = text.toLowerCase(Locale.ROOT);
            if (!lower.contains("жалоб") && !lower.contains("репорт") && !lower.contains("report")) {
               return null;
            } else {
               Matcher nick = NICK_PATTERN.matcher(text);

               while (nick.find()) {
                  String value = nick.group();
                  if (!this.isCommonWord(value)) {
                     return value;
                  }
               }

               return null;
            }
         }
      }
   }

   private void handleServerText(String raw) {
      String text = this.clean(raw);
      if (!text.isEmpty()) {
         String lower = text.toLowerCase(Locale.ROOT);
         if (this.waitingName != null) {
            if (this.isCooldownMessage(lower)) {
               this.queueFirst(this.waitingName);
               this.waitingName = null;
               this.nextReportAllowedAt = System.currentTimeMillis() + this.parseWaitMs(lower);
            } else if (this.isAfkMessage(lower)) {
               this.lastFailedName = this.waitingName;
               this.waitingName = null;
            } else if (this.isNotFoundMessage(lower)) {
               this.lastFailedName = this.waitingName;
               this.waitingName = null;
            } else if (this.isAlreadyReportedMessage(lower)) {
               this.markReported(this.waitingName);
               this.waitingName = null;
            } else if (this.isSuccessMessage(lower)) {
               this.markReported(this.waitingName);
               this.waitingName = null;
               this.nextReportAllowedAt = System.currentTimeMillis() + this.getReportDelayMs();
            }
         }

         String listName;
         if ((listName = this.extractReportName(text)) != null) {
            this.markReported(listName);
         }
      }
   }

   private boolean isSuccessMessage(String lower) {
      return lower.contains("жалоба успешно отправлена") || lower.contains("репорт на") && lower.contains("был принят");
   }

   private boolean isCooldownMessage(String lower) {
      return lower.contains("необходимо подождать") || lower.contains("cooldown");
   }

   private boolean isAlreadyReportedMessage(String lower) {
      return lower.contains("уже отправляли") || lower.contains("already");
   }

   private boolean isAfkMessage(String lower) {
      return lower.contains("игрок находится в афк") || lower.contains("afk");
   }

   private boolean isNotFoundMessage(String lower) {
      return lower.contains("игрок не найден") || lower.contains("player not found");
   }

   private long parseWaitMs(String lower) {
      Matcher matcher = WAIT_SECONDS_PATTERN.matcher(lower);
      return matcher.find() ? Math.max(1L, Long.parseLong(matcher.group(1))) * 1000L + 500L : 5000L;
   }

   private long getReportDelayMs() {
      return Math.max(31000L, this.delaySeconds.getValue().longValue() * 1000L);
   }

   private void markReported(String name) {
      String normalized = this.normalizeName(name);
      if (!normalized.isEmpty() && !this.isSelf(normalized)) {
         this.reportedNames.add(normalized);
         this.listNames.add(normalized);
      }
   }

   private boolean shouldHighlightEntity(class_1297 entity) {
      class_1657 player;
      return entity instanceof class_1657 && this.shouldHighlightName((player = (class_1657)entity).method_5477().getString());
   }

   private boolean hasReportedName(String name) {
      String normalized = this.normalizeName(name);
      return this.reportedNames.contains(normalized) || this.listNames.contains(normalized);
   }

   private boolean shouldHighlightName(String name) {
      String normalized = this.normalizeName(name);
      return this.hasReportedName(normalized)
         || this.queuedNames.contains(normalized)
         || this.waitingName != null && this.normalizeName(this.waitingName).equals(normalized);
   }

   private int getAllHighlightedCount() {
      LinkedHashSet<String> all = new LinkedHashSet<>(this.reportedNames);
      all.addAll(this.listNames);
      all.addAll(this.queuedNames);
      if (this.waitingName != null) {
         all.add(this.normalizeName(this.waitingName));
      }

      return all.size();
   }

   private String cooldownText() {
      long left = Math.max(0L, this.nextReportAllowedAt - System.currentTimeMillis());
      return left <= 0L ? "Готово" : "Кд: " + (left + 999L) / 1000L + "с";
   }

   private void sendCommand(String command) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         mc.field_1724.field_3944.method_45730(command.startsWith("/") ? command.substring(1) : command);
      }
   }

   private boolean isSelf(String name) {
      return mc.method_1548() != null && this.normalizeName(mc.method_1548().method_1676()).equals(this.normalizeName(name));
   }

   private String normalizeName(String name) {
      if (name == null) {
         return "";
      } else {
         Matcher matcher = NICK_PATTERN.matcher(this.clean(name));
         return matcher.find() ? matcher.group().toLowerCase(Locale.ROOT) : "";
      }
   }

   private String clean(String value) {
      String stripped = class_124.method_539(value == null ? "" : value);
      return stripped == null ? "" : stripped.replace('§', ' ').trim();
   }

   private boolean isCommonWord(String word) {
      String lower = word.toLowerCase(Locale.ROOT);
      return lower.equals("report")
         || lower.equals("complaint")
         || lower.equals("status")
         || lower.equals("date")
         || lower.equals("reason")
         || lower.equals("minecraft")
         || lower.equals("list")
         || lower.equals("next")
         || lower.equals("page");
   }
}
