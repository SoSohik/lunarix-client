package ru.mentions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import lombok.Generated;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.command.CommandManager;
import ru.mentions.config.ConfigManager;
import ru.mentions.friend.FriendManager;
import ru.mentions.inventorypreset.InventoryPresetManager;
import ru.mentions.lang.Lang;
import ru.mentions.manager.GuiManager;
import ru.mentions.manager.events.orbit.EventBus;
import ru.mentions.manager.rotation.ComponentManager;
import ru.mentions.module.api.ModuleManager;
import ru.mentions.module.impl.misc.AutoBuyKeys;
import ru.mentions.rpc.DiscordRPCManager;
import ru.mentions.rpc.RPC;
import ru.mentions.screen.Menu;
import ru.mentions.screen.Menu2;
import ru.mentions.utils.RussianUi;
import ru.mentions.utils.render.Render2D;
import ru.mentions.utils.render.font.FontInitializer;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
   public static Client get;
   private static final EventBus eventHandler = EventBus.threadSafe();
   private ModuleManager moduleManager;
   private Render2D render2D;
   private Menu clickGuiScreen;
   private Menu2 menu2Screen;
   private ComponentManager componentManager;
   private CommandManager commandManager;
   private ConfigManager configManager;
   private FriendManager friendManager;
   private InventoryPresetManager inventoryPresetManager;
   private final RPC rpc = new RPC();
   public static String build = "1.0 alpha";
   private GuiManager guiManager;

   public static Client get() {
      return get;
   }

   public void start() {
      eventHandler.registerLambdaFactory("", (lookupInMethod, klass) -> (Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
      FontInitializer.register();
   }

   public void onInitializeClient() {
      get = this;
      Lang.init();
      this.start();
      this.rpc.startRpc();
      this.configManager = new ConfigManager();
      this.configManager.setup();
      this.friendManager = new FriendManager();
      this.friendManager.init();
      this.inventoryPresetManager = new InventoryPresetManager();
      this.inventoryPresetManager.init();
      this.moduleManager = new ModuleManager();
      this.moduleManager.init();
      AutoBuyKeys.install();
      this.componentManager = new ComponentManager();
      this.componentManager.init();
      this.commandManager = new CommandManager();
      this.commandManager.init();
      this.configManager.init();
      this.guiManager = new GuiManager();
      this.guiManager.init();
      RussianUi.text("");
      this.render2D = new Render2D();
      this.clickGuiScreen = new Menu();
      this.menu2Screen = new Menu2();
      Menu.selectedTheme = this.guiManager.getCurrentTheme();
      Menu.preSelectedTheme = this.guiManager.getCurrentTheme();
      Runtime.getRuntime().addShutdownHook(new Thread(this::unload));
   }

   public void unload() {
      DiscordRPCManager.getInstance().stop();
      if (this.configManager != null) {
         this.configManager.autoSave();
      }
   }

   public DiscordRPCManager discordRpcManager() {
      return DiscordRPCManager.getInstance();
   }

   @Generated
   public ModuleManager moduleManager() {
      return this.moduleManager;
   }

   @Generated
   public Render2D render2D() {
      return this.render2D;
   }

   @Generated
   public Menu clickGuiScreen() {
      return this.clickGuiScreen;
   }

   @Generated
   public Menu2 menu2Screen() {
      return this.menu2Screen;
   }

   @Generated
   public ComponentManager componentManager() {
      return this.componentManager;
   }

   @Generated
   public CommandManager commandManager() {
      return this.commandManager;
   }

   @Generated
   public ConfigManager configManager() {
      return this.configManager;
   }

   @Generated
   public FriendManager friendManager() {
      return this.friendManager;
   }

   @Generated
   public InventoryPresetManager inventoryPresetManager() {
      return this.inventoryPresetManager;
   }

   @Generated
   public RPC rpc() {
      return this.rpc;
   }

   @Generated
   public GuiManager guiManager() {
      return this.guiManager;
   }

   @Generated
   public static EventBus eventHandler() {
      return eventHandler;
   }
}
