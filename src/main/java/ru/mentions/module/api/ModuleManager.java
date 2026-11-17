package ru.mentions.module.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.maintenance.CleanupModule;
import ru.mentions.manager.event_impl.EventKey;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.impl.combat.AimBot;
import ru.mentions.module.impl.combat.AntiBot;
import ru.mentions.module.impl.combat.AttackAura;
import ru.mentions.module.impl.combat.AutoArmor;
import ru.mentions.module.impl.combat.AutoSwap;
import ru.mentions.module.impl.combat.AutoTotem;
import ru.mentions.module.impl.combat.ClickPearlModule;
import ru.mentions.module.impl.combat.Criticals;
import ru.mentions.module.impl.combat.CrystalAura;
import ru.mentions.module.impl.combat.HitBoxes;
import ru.mentions.module.impl.combat.MaceHelper;
import ru.mentions.module.impl.combat.NoFriendDamage;
import ru.mentions.module.impl.combat.NoSlotChange;
import ru.mentions.module.impl.combat.PotionHelper;
import ru.mentions.module.impl.combat.TargetPearl;
import ru.mentions.module.impl.combat.UseTracker;
import ru.mentions.module.impl.display.Arrows;
import ru.mentions.module.impl.display.ClickGui;
import ru.mentions.module.impl.display.ClientTheme;
import ru.mentions.module.impl.display.GpsHud;
import ru.mentions.module.impl.display.InterFace;
import ru.mentions.module.impl.display.Profiles;
import ru.mentions.module.impl.misc.AlertHelper;
import ru.mentions.module.impl.misc.AutoBuy;
import ru.mentions.module.impl.misc.AutoParser;
import ru.mentions.module.impl.misc.Optimization;
import ru.mentions.module.impl.misc.SafeMode;
import ru.mentions.module.impl.movement.AirStuck;
import ru.mentions.module.impl.movement.NoSlow;
import ru.mentions.module.impl.player.InventoryMove;
import ru.mentions.module.impl.player.KtLeave;
import ru.mentions.module.impl.misc.Actions;
import ru.mentions.module.impl.movement.NoWeb;
import ru.mentions.module.impl.movement.Speed;
import ru.mentions.module.impl.movement.Spider;
import ru.mentions.module.impl.movement.Sprint;
import ru.mentions.module.impl.pasted.AimAssistSystem;
import ru.mentions.module.impl.pasted.CameraTweaksSystem;
import ru.mentions.module.impl.pasted.CustomSky;
import ru.mentions.module.impl.pasted.GlowEsp;
import ru.mentions.module.impl.pasted.HitRefraction;
import ru.mentions.module.impl.pasted.PlasmaPinch;
import ru.mentions.module.impl.pasted.ShowInvisibleSystem;
import ru.mentions.module.impl.pasted.TriggerBotSystem;
import ru.mentions.module.impl.pasted.ZoomSystem;
import ru.mentions.module.impl.player.AuctionHelper;
import ru.mentions.module.impl.player.AutoAccept;
import ru.mentions.module.impl.player.AutoLeave;
import ru.mentions.module.impl.player.AutoTool;
import ru.mentions.module.impl.player.ClickHelper;
import ru.mentions.module.impl.player.ElytraHelper;
import ru.mentions.module.impl.player.FreeCamera;
import ru.mentions.module.impl.player.FreeLook;
import ru.mentions.module.impl.player.InventoryMove;
import ru.mentions.module.impl.player.LockSlot;
import ru.mentions.module.impl.player.NoDelay;
import ru.mentions.module.impl.player.NoPush;
import ru.mentions.module.impl.player.PvpSafe;
import ru.mentions.module.impl.player.WorldTracker;
import ru.mentions.module.impl.render.AspectRatio;
import ru.mentions.module.impl.render.Atmosphere;
import ru.mentions.module.impl.render.BlockEsp;
import ru.mentions.module.impl.render.ChinaHat;
import ru.mentions.module.impl.render.ColorGrade;
import ru.mentions.module.impl.render.Cosmetics;
import ru.mentions.module.impl.render.CrossHair;
import ru.mentions.module.impl.render.Emotions;
import ru.mentions.module.impl.render.EntityEsp;
import ru.mentions.module.impl.render.FireFlies;
import ru.mentions.module.impl.render.FogBlur;
import ru.mentions.module.impl.render.Gamma;
import ru.mentions.module.impl.render.GlassBlock;
import ru.mentions.module.impl.render.GlassHands;
import ru.mentions.module.impl.render.Globals;
import ru.mentions.module.impl.render.Grybochki;
import ru.mentions.module.impl.render.Hands;
import ru.mentions.module.impl.render.Hands3D;
import ru.mentions.module.impl.render.HealthAlert;
import ru.mentions.module.impl.render.ItemEsp;
import ru.mentions.module.impl.render.ItemPhysics;
import ru.mentions.module.impl.render.ItemReplacer;
import ru.mentions.module.impl.render.JumpCircle;
import ru.mentions.module.impl.render.JumpCube;
import ru.mentions.module.impl.render.KillEffect;
import ru.mentions.module.impl.render.MaceAlert;
import ru.mentions.module.impl.render.NameTag;
import ru.mentions.module.impl.render.NoRender;
import ru.mentions.module.impl.render.Particles;
import ru.mentions.module.impl.render.PearlTracer;
import ru.mentions.module.impl.render.PlayerChams;
import ru.mentions.module.impl.render.Poprygunchik;
import ru.mentions.module.impl.render.Prism;
import ru.mentions.module.impl.render.ScanWorld;
import ru.mentions.module.impl.render.ShaderEsp;
import ru.mentions.module.impl.render.ShaderSky;
import ru.mentions.module.impl.render.SpinBot;
import ru.mentions.module.impl.render.Structures;
import ru.mentions.module.impl.render.Svetoch;
import ru.mentions.module.impl.render.SwingAnimation;
import ru.mentions.module.impl.render.TntTimer;
import ru.mentions.module.impl.render.TotemGhost;
import ru.mentions.module.impl.render.Tracers;
import ru.mentions.module.impl.render.Trails;
import ru.mentions.module.impl.render.Trajectories;
import ru.mentions.module.impl.render.ViewModel;
import ru.mentions.module.impl.render.Wings;
import ru.mentions.module.impl.render.WorldCubes;
import ru.mentions.module.impl.render.WorldTweaks;
import ru.mentions.module.impl.render.lyrics.KineticLyrics;
import ru.mentions.module.impl.utils.AntiAFK;
import ru.mentions.module.impl.utils.AppleFarmer;
import ru.mentions.module.impl.utils.AuraCrafter;
import ru.mentions.module.impl.utils.AutoClanUpgrade;
import ru.mentions.module.impl.utils.AutoDuel;
import ru.mentions.module.impl.utils.AutoEnd;
import ru.mentions.module.impl.utils.AutoFish;
import ru.mentions.module.impl.utils.AutoInvest;
import ru.mentions.module.impl.utils.AutoPotion;
import ru.mentions.module.impl.utils.AutoReconnect;
import ru.mentions.module.impl.utils.AutoResell;
import ru.mentions.module.impl.utils.AutoStorage;
import ru.mentions.module.impl.utils.AutoWarden;
import ru.mentions.module.impl.utils.BaseFinder;
import ru.mentions.module.impl.utils.ChatFilter;
import ru.mentions.module.impl.utils.ChestStealer;
import ru.mentions.module.impl.utils.DanjHelper;
import ru.mentions.module.impl.utils.FakePlayer;
import ru.mentions.module.impl.utils.GlassFarmer;
import ru.mentions.module.impl.utils.ItemScroller;
import ru.mentions.module.impl.utils.MoneyFarm;
import ru.mentions.module.impl.utils.NameProtect;
import ru.mentions.module.impl.utils.PotionFarmer;
import ru.mentions.module.impl.utils.ReportHelper;
import ru.mentions.module.impl.utils.SPJoiner;
import ru.mentions.module.impl.utils.UnHook;
import ru.mentions.module.impl.utils.WardenHelper;

@Environment(EnvType.CLIENT)
public final class ModuleManager extends LinkedHashMap<Class<? extends Module>, Module> {
   public void init() {
      this.addSorted(
         new ClientTheme(),
         new AttackAura(),
         new AutoSwap(),
         new PotionHelper(),
         new AutoTotem(),
         new AimBot(),
         new NoFriendDamage(),
         new NoSlotChange(),
         new HitBoxes(),
         new Criticals(),
         new CrystalAura(),
         new AntiBot(),
         new UseTracker(),
         new MaceHelper(),
         new Sprint(),
         new NoSlow(),
         new Spider(),
         new NoWeb(),
         new Speed(),
         new AirStuck(),
         new KtLeave(),
         new Actions(),
         new ClickGui(),
         new Arrows(),
         new GpsHud(),
         new EntityEsp(),
         new NameTag(),
         new NoRender(),
         new SwingAnimation(),
         new GlassHands(),
         new GlassBlock(),
         new ShaderEsp(),
         new GlowEsp(),
         new ShaderSky(),
         new CustomSky(),
         new HitRefraction(),
         new PlasmaPinch(),
         new Grybochki(),
         new Poprygunchik(),
         new Prism(),
         new Emotions(),
         new WorldTweaks(),
         new Gamma(),
         new Particles(),
         new HealthAlert(),
         new BlockEsp(),
         new ChinaHat(),
         new Cosmetics(),
         new JumpCircle(),
         new JumpCube(),
         new CrossHair(),
         new Trails(),
         new WorldCubes(),
         new Trajectories(),
         new ColorGrade(),
         new FireFlies(),
         new Svetoch(),
         new ScanWorld(),
         new FogBlur(),
         new TotemGhost(),
         new KillEffect(),
         new AspectRatio(),
         new Atmosphere(),
         new Hands(),
         new Hands3D(),
         new Tracers(),
         new TntTimer(),
         new Structures(),
         new PearlTracer(),
         new ItemEsp(),
         new Wings(),
         new PlayerChams(),
         new ViewModel(),
         new ItemReplacer(),
         new InterFace(),
         new ReportHelper(),
         new AutoAccept(),
         new NoPush(),
         new NoDelay(),
         new ClickHelper(),
         new WorldTracker(),
         new InventoryMove(),
         new ElytraHelper(),
         new AutoLeave(),
         new AutoTool(),
         new AuctionHelper(),
         new AutoBuy(),
         new AutoParser(),
         new SafeMode(),
         new AlertHelper(),
         new Optimization(),
         new LockSlot(),
         new FreeCamera(),
         new FreeLook(),
         new PvpSafe(),
         new AppleFarmer(),
         new UnHook(),
         new NameProtect(),
         new GlassFarmer(),
         new PotionFarmer(),
         new SPJoiner(),
         new FakePlayer(),
         new ChestStealer(),
         new AutoClanUpgrade(),
         new AutoStorage(),
         new WardenHelper(),
         new AuraCrafter(),
         new AutoResell(),
         new ItemScroller(),
         new AutoDuel(),
         new AutoPotion(),
         new DanjHelper(),
         new AutoInvest(),
         new AutoEnd(),
         new AutoWarden(),
         new ClickPearlModule(),
         new TargetPearl(),
         new BaseFinder(),
         new MoneyFarm(),
         new CleanupModule(),
         new Profiles(),
         new AutoArmor(),
         new AntiAFK(),
         new AutoFish(),
         new ChatFilter(),
         new AutoReconnect(),
         new SpinBot(),
         new MaceAlert(),
         new ItemPhysics(),
         new AimAssistSystem(),
         new TriggerBotSystem(),
         new ZoomSystem(),
         new ShowInvisibleSystem(),
         new CameraTweaksSystem(),
         new KineticLyrics(),
         new Globals()
      );
      this.values().stream().filter(Module::isAutoEnabled).forEach(module -> module.setEnabled(true, false));
      Client.eventHandler().subscribe(this);
   }

   public void addSorted(Module... modules) {
      Arrays.stream(modules).forEach(module -> this.put((Class<? extends Module>)module.getClass(), module));
   }

   public void unregister(Module... modules) {
      Arrays.stream(modules).forEach(module -> this.remove(module.getClass()));
   }

   @EventHandler
   public void onKeyboardPress(EventKey event) {
      this.values().stream().filter(Module::isAllowDisable).filter(module -> module.getKey() == event.getKey()).forEach(Module::toggle);
   }

   public <T extends Module> T get(String name) {
      return this.values().stream().filter(module -> module.getName().equalsIgnoreCase(name)).map(module -> (T)module).findFirst().orElse(null);
   }

   public <T extends Module> T get(Class<T> clazz) {
      Module exact = (Module)super.get(clazz);
      return exact != null
         ? clazz.cast(exact)
         : this.values().stream().filter(module -> clazz.isAssignableFrom(module.getClass())).map(clazz::cast).findFirst().orElse(null);
   }

   public List<Module> get(Category category) {
      return this.values().stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
   }

   public Module getModule(String name) {
      return this.values().stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   @Override
   public Collection<Module> values() {
      return super.values().stream().sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
   }
}
