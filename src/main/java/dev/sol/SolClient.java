package dev.sol;

import dev.sol.config.ConfigManager;
import dev.sol.config.ConfigStore;
import dev.sol.gui.ClickGuiScreen;
import dev.sol.gui.ClickGuiState;
import dev.sol.hud.HudManager;
import dev.sol.license.LicenseGuard;
import dev.sol.license.ProtectedContent;
import dev.sol.module.Category;
import dev.sol.module.ModuleManager;
import dev.sol.module.impl.FreecamModule;
import dev.sol.notification.NotificationManager;
import dev.sol.render.MotionBlurRenderer;
import dev.sol.render.OverlayRenderer;
import dev.sol.render.SusChunkRenderer;
import dev.sol.spotify.SpotifyService;
import dev.sol.suschunk.ServerLightCache;
import dev.sol.theme.ThemeManager;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolClient implements ClientModInitializer {
   public static final String MOD_ID = "solclient";
   public static final String NAME = "Sol Client";
   public static final String VERSION = "1.0.0";
   public static final Logger LOGGER = LoggerFactory.getLogger("Sol Client");
   private static ModuleManager modules;
   private static ThemeManager themes;
   private static ConfigManager config;
   private static ConfigStore configStore;
   private static HudManager hud;
   private static NotificationManager notifications;
   private static SpotifyService spotify;

   public static ModuleManager modules() {
      return modules;
   }

   public static ThemeManager themes() {
      return themes;
   }

   public static ConfigManager config() {
      return config;
   }

   public static ConfigStore configStore() {
      return configStore;
   }

   public static HudManager hud() {
      return hud;
   }

   public static SpotifyService spotify() {
      return spotify;
   }

   public static NotificationManager notifications() {
      return notifications;
   }

   public void onInitializeClient() {
      byte[] contentKey = LicenseGuard.enforce(LOGGER);
      ProtectedContent.init(contentKey);
      ProtectedContent.activate(LOGGER, "dev.sol.secured.SecuredFeature");
      LOGGER.info("{} {} initializing", "Sol Client", "1.0.0");
      themes = new ThemeManager();
      modules = new ModuleManager();
      spotify = new SpotifyService();
      notifications = new NotificationManager(themes, modules.hud);
      hud = new HudManager(modules, themes, spotify, notifications);
      config = new ConfigManager(modules, themes);
      ConfigManager var10000 = config;
      HudManager var10002 = hud;
      Objects.requireNonNull(var10002);
      Supplier var4 = var10002::toJson;
      HudManager var10003 = hud;
      Objects.requireNonNull(var10003);
      var10000.addSection("hud", var4, var10003::fromJson);
      var10000 = config;
      ClickGuiState var5 = ClickGuiScreen.state();
      Objects.requireNonNull(var5);
      Supplier var6 = var5::toJson;
      ClickGuiState var9 = ClickGuiScreen.state();
      Objects.requireNonNull(var9);
      var10000.addSection("panels", var6, var9::fromJson);
      config.load();
      configStore = new ConfigStore(config);
      configStore.loadAll();
      modules.setOpenGuiAction(() -> class_310.method_1551().method_1507(new ClickGuiScreen()));
      modules.setToggleListener((module, enabled) -> {
         if (!ConfigStore.applying && module.getCategory() != Category.CLIENT && class_310.method_1551().field_1687 != null && (Boolean)modules.hud.notifications.get()) {
            notifications.push(module.getName(), enabled);
         }

      });
      OverlayRenderer.init(hud, notifications);
      spotify.start();
      ClientPlayConnectionEvents.JOIN.register((ClientPlayConnectionEvents.Join)(handler, sender, client) -> clearSusState());
      ClientPlayConnectionEvents.DISCONNECT.register((ClientPlayConnectionEvents.Disconnect)(handler, client) -> clearSusState());
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(client) -> modules.onTick());
      ClientTickEvents.START_CLIENT_TICK.register((ClientTickEvents.StartTick)(client) -> {
         FreecamModule f = modules.freecam;
         if (f != null && f.isActive()) {
            FreecamModule.reapplyBodyInput(client);
         }

         modules.onCombatTick();
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientLifecycleEvents.ClientStopping)(client) -> {
         config.save();
         spotify.stop();
      });
   }

   private static void clearSusState() {
      ServerLightCache.get().clear();
      modules.susChunkFinder.scanner.clear();
      SusChunkRenderer.reset();
      if (modules.chunkFinder != null) {
         modules.chunkFinder.clear();
      }

      if (modules.blockEntityEsp != null) {
         modules.blockEntityEsp.clear();
      }

      if (modules.jumpCircles != null) {
         modules.jumpCircles.clear();
      }

      if (modules.hitParticles != null) {
         modules.hitParticles.clear();
      }

      MotionBlurRenderer.reset();
   }
}
