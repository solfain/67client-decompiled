package dev.sol.module;

import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

import dev.sol.module.impl.AimAssistModule;
import dev.sol.module.impl.AnchorMacroModule;
import dev.sol.module.impl.ArmorTrimHiderModule;
import dev.sol.module.impl.AutoClickerModule;
import dev.sol.module.impl.AutoCrystalModule;
import dev.sol.module.impl.AutoInventoryTotemModule;
import dev.sol.module.impl.AutoTotemModule;
import dev.sol.module.impl.AutoTpaModule;
import dev.sol.module.impl.AutoWalkModule;
import dev.sol.module.impl.BlockEntityEspModule;
import dev.sol.module.impl.BlockEspModule;
import dev.sol.module.impl.ChatMacroModule;
import dev.sol.module.impl.ChunkFinderModule;
import dev.sol.module.impl.CoordSnapperModule;
import dev.sol.module.impl.CustomCrosshairModule;
import dev.sol.module.impl.CustomFovModule;
import dev.sol.module.impl.DebugHoleEspModule;
import dev.sol.module.impl.DoubleAnchorModule;
import dev.sol.module.impl.ElytraSwapModule;
import dev.sol.module.impl.FakePayModule;
import dev.sol.module.impl.FakeRolesModule;
import dev.sol.module.impl.FakeStatsModule;
import dev.sol.module.impl.FastUseModule;
import dev.sol.module.impl.FreeLookModule;
import dev.sol.module.impl.FreecamModule;
import dev.sol.module.impl.FullbrightModule;
import dev.sol.module.impl.HitBoxModule;
import dev.sol.module.impl.HitParticlesModule;
import dev.sol.module.impl.HoverTotemModule;
import dev.sol.module.impl.JumpCirclesModule;
import dev.sol.module.impl.MaceBomberModule;
import dev.sol.module.impl.MaceSwapModule;
import dev.sol.module.impl.MobEspModule;
import dev.sol.module.impl.MotionBlurModule;
import dev.sol.module.impl.NameProtectModule;
import dev.sol.module.impl.NameTagsModule;
import dev.sol.module.impl.PlayerEspModule;
import dev.sol.module.impl.RegionMapModule;
import dev.sol.module.impl.ShieldBreakerModule;
import dev.sol.module.impl.SkinProtectModule;
import dev.sol.module.impl.SpawnerNametagsModule;
import dev.sol.module.impl.SpawnerProtectModule;
import dev.sol.module.impl.StaffListModule;
import dev.sol.module.impl.StorageEspModule;
import dev.sol.module.impl.SwingSpeedModule;
import dev.sol.module.impl.TriggerbotModule;
import dev.sol.module.impl.WeatherNotifierModule;
import dev.sol.module.impl.ZoomModule;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ModuleManager {
   private final List<Module> modules = new ArrayList();
   private final Map<Category, List<Module>> byCategory = new LinkedHashMap();
   public Modules.ClickGuiModule clickGui;
   public Modules.HudModule hud;
   public Modules.SpotifyModule spotify;
   public Modules.BlockOutlineModule blockOutline;
   public Modules.SusChunkFinderModule susChunkFinder;
   public FullbrightModule fullbright;
   public AutoWalkModule autoWalk;
   public WeatherNotifierModule weatherNotifier;
   public SwingSpeedModule swingSpeed;
   public StorageEspModule storageEsp;
   public BlockEspModule blockEsp;
   public AutoTotemModule autoTotem;
   public MaceSwapModule maceSwap;
   public AnchorMacroModule anchorMacro;
   public AutoCrystalModule autoCrystal;
   public HitBoxModule hitBox;
   public ElytraSwapModule elytraSwap;
   public HoverTotemModule hoverTotem;
   public ShieldBreakerModule shieldBreaker;
   public TriggerbotModule triggerbot;
   public AimAssistModule aimAssist;
   public DoubleAnchorModule doubleAnchor;
   public MaceBomberModule maceBomber;
   public NameProtectModule nameProtect;
   public SkinProtectModule skinProtect;
   public NameTagsModule nameTags;
   public FastUseModule fastUse;
   public AutoInventoryTotemModule autoInventoryTotem;
   public PlayerEspModule playerEsp;
   public MobEspModule mobEsp;
   public BlockEntityEspModule blockEntityEsp;
   public SpawnerNametagsModule spawnerNametags;
   public DebugHoleEspModule debugHoleEsp;
   public FreecamModule freecam;
   public AutoTpaModule autoTpa;
   public JumpCirclesModule jumpCircles;
   public CustomCrosshairModule customCrosshair;
   public ZoomModule zoom;
   public CustomFovModule customFov;
   public HitParticlesModule hitParticles;
   public MotionBlurModule motionBlur;
   public ChunkFinderModule chunkFinder;
   public FreeLookModule freeLook;
   public AutoClickerModule autoClicker;
   public CoordSnapperModule coordSnapper;
   public RegionMapModule regionMap;
   public ChatMacroModule chatMacro;
   public FakePayModule fakePay;
   public FakeStatsModule fakeStats;
   public FakeRolesModule fakeRoles;
   public StaffListModule staffList;
   public ArmorTrimHiderModule armorTrimHider;
   public SpawnerProtectModule spawnerProtect;
   private Runnable openGuiAction = () -> {
   };
   private BiConsumer<Module, Boolean> toggleListener = (m, e) -> {
   };

   public ModuleManager() {
      for(Category category : Category.values()) {
         this.byCategory.put(category, new ArrayList());
      }

      this.susChunkFinder = new Modules.SusChunkFinderModule();
      this.registerPlaceholders();
      this.register(this.blockOutline = new Modules.BlockOutlineModule());
      this.register(this.hitParticles = new HitParticlesModule());
      this.register(this.motionBlur = new MotionBlurModule());
      this.register(this.hud = new Modules.HudModule());
      this.register(this.spotify = new Modules.SpotifyModule());
      this.register(this.chatMacro = new ChatMacroModule());
      this.run("ConfigShare", "Import/export configs via codes", Category.CLIENT, new BooleanSetting("Include HUD Layout", "Share HUD positions too", true));
      this.register(this.swingSpeed = new SwingSpeedModule());
      this.register(this.jumpCircles = new JumpCirclesModule());
      this.register(this.clickGui = new Modules.ClickGuiModule());
   }

   private void registerPlaceholders() {
      this.register(this.autoTotem = new AutoTotemModule());
      this.register(this.autoCrystal = new AutoCrystalModule());
      this.register(this.anchorMacro = new AnchorMacroModule());
      this.register(this.doubleAnchor = new DoubleAnchorModule());
      this.register(this.autoInventoryTotem = new AutoInventoryTotemModule());
      this.register(this.aimAssist = new AimAssistModule());
      this.register(this.maceSwap = new MaceSwapModule());
      this.register(this.hitBox = new HitBoxModule());
      this.register(this.elytraSwap = new ElytraSwapModule());
      this.register(this.hoverTotem = new HoverTotemModule());
      this.register(this.shieldBreaker = new ShieldBreakerModule());
      this.register(this.triggerbot = new TriggerbotModule());
      this.register(this.maceBomber = new MaceBomberModule());
      this.register(this.skinProtect = new SkinProtectModule());
      this.register(this.nameProtect = new NameProtectModule());
      this.register(this.freecam = new FreecamModule());
      this.register(this.autoTpa = new AutoTpaModule());
      this.register(this.autoClicker = new AutoClickerModule());
      this.register(this.fastUse = new FastUseModule());
      this.register(this.nameTags = new NameTagsModule());
      this.register(this.fakePay = new FakePayModule());
      this.register(this.weatherNotifier = new WeatherNotifierModule());
      this.register(this.fakeStats = new FakeStatsModule());
      this.register(this.fakeRoles = new FakeRolesModule());
      this.register(this.armorTrimHider = new ArmorTrimHiderModule());
      this.register(this.customCrosshair = new CustomCrosshairModule());
      this.run("Media/StaffNames/Icons", "Marks media and staff players with icons", Category.WORLD, new BooleanSetting("Media", "Show media icons", true), new BooleanSetting("Staff", "Show staff icons", true), new ModeSetting("Position", "Icon position", "Prefix", new String[]{"Prefix", "Suffix"}));
      this.register(this.staffList = new StaffListModule());
      this.register(this.customFov = new CustomFovModule());
      this.register(this.coordSnapper = new CoordSnapperModule());
      this.register(this.autoWalk = new AutoWalkModule());
      this.register(this.zoom = new ZoomModule());
      this.register(this.freeLook = new FreeLookModule());
      this.register(this.spawnerProtect = new SpawnerProtectModule());
      this.register(this.blockEsp = new BlockEspModule());
      this.register(this.storageEsp = new StorageEspModule());
      this.register(this.blockEntityEsp = new BlockEntityEspModule());
      this.register(this.debugHoleEsp = new DebugHoleEspModule());
      this.register(this.fullbright = new FullbrightModule());
      this.register(this.playerEsp = new PlayerEspModule());
      this.register(this.mobEsp = new MobEspModule());
      this.register(this.spawnerNametags = new SpawnerNametagsModule());
      this.register(this.susChunkFinder);
      this.register(this.chunkFinder = new ChunkFinderModule());
      this.register(this.regionMap = new RegionMapModule());
   }

   private void run(String name, String description, Category category, Setting<?>... settings) {
      this.register(new Modules.Placeholder(name, description, category, settings));
   }

   private static final Set<String> START_TICK = Set.of("AimAssist", "Triggerbot", "Auto Crystal", "Anchor Macro", "Double Anchor", "Mace Swap", "Mace Bomber", "Shield Breaker", "HitBox", "Auto Totem", "Inv Totem", "Hover Totem", "Elytra Swap");

   private static boolean startTick(Module module) {
      return START_TICK.contains(module.getName());
   }

   public void register(Module module) {
      this.modules.add(module);
      List<Module> list = (List)this.byCategory.get(module.getCategory());
      list.add(module);
      list.sort(Comparator.comparing((Module m) -> m.getName().toLowerCase(Locale.ROOT)));
      module.setToggleCallback(this::notifyToggle);
   }

   public List<Module> all() {
      return this.modules;
   }

   public List<Module> inCategory(Category category) {
      return (List)this.byCategory.get(category);
   }

   public void setOpenGuiAction(Runnable action) {
      this.openGuiAction = action;
   }

   public void setToggleListener(BiConsumer<Module, Boolean> listener) {
      this.toggleListener = listener;
   }

   public void notifyToggle(Module module, boolean enabled) {
      this.toggleListener.accept(module, enabled);
   }

   public boolean onKeyPressed(int keyCode) {
      if (this.clickGui.getKeybind().matches(keyCode)) {
         this.openGuiAction.run();
         return true;
      } else {
         boolean handled = false;

         for(Module module : this.modules) {
            if (module != this.clickGui && module.getKeybind().matches(keyCode)) {
               module.toggle();
               handled = true;
            }
         }

         for(Module modulex : this.modules) {
            if (modulex.isEnabled() && modulex.onKeyPress(keyCode)) {
               handled = true;
            }
         }

         return handled;
      }
   }

   public void onTick() {
      for(Module module : this.modules) {
         if (module.isEnabled() && !startTick(module)) {
            module.onTick();
         }
      }

   }

   public void onCombatTick() {
      for(Module module : this.modules) {
         if (module.isEnabled() && startTick(module)) {
            module.onTick();
         }
      }

   }
}
