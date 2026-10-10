package dev.sol.module;

import dev.sol.render.SusChunkRenderer;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.Setting;
import dev.sol.settings.SliderSetting;
import dev.sol.suschunk.SusChunkScanner;
import java.util.Objects;

public final class Modules {
   private Modules() {
   }

   public static class BlockOutlineModule extends Module {
      public final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", "Outline color", -26368));
      public final BooleanSetting rainbow = (BooleanSetting)this.addSetting(new BooleanSetting("Rainbow", "Cycle the outline through the rainbow", false));
      public final SliderSetting glow = (SliderSetting)this.addSetting(new SliderSetting("Glow", "Outer glow intensity", (double)60.0F, (double)0.0F, (double)100.0F, (double)5.0F, "%"));
      public final SliderSetting thickness = (SliderSetting)this.addSetting(new SliderSetting("Thickness", "Core line width", (double)2.5F, (double)1.0F, (double)6.0F, (double)0.5F, "px"));
      public final SliderSetting fillOpacity = (SliderSetting)this.addSetting(new SliderSetting("Fill", "Transparent fill inside the block", (double)12.0F, (double)0.0F, (double)60.0F, (double)2.0F, "%"));
      public final ModeSetting animation = (ModeSetting)this.addSetting(new ModeSetting("Animation", "Outline animation style", "Pulse", new String[]{"Pulse", "Gradient Flow", "Static"}));

      public BlockOutlineModule() {
         super("CustomBlockOutline", "Glowing animated outline on the targeted block", Category.VISUALS);
         this.setEnabled(true);
      }
   }

   public static class ClickGuiModule extends Module {
      public final BooleanSetting blur = (BooleanSetting)this.addSetting(new BooleanSetting("Blur", "Gaussian-blur the world behind the GUI", true));
      public final SliderSetting blurStrength = (SliderSetting)this.addSetting(new SliderSetting("Blur Strength", "How strong the background blur is", (double)6.0F, (double)1.0F, (double)10.0F, (double)1.0F));
      public final ModeSetting font = (ModeSetting)this.addSetting(new ModeSetting("Font", "GUI font (Xuong TTF or vanilla-style)", "Xuong", new String[]{"Xuong", "Vanilla"}));

      public ClickGuiModule() {
         super("ClickGUI", "The Client menu. In menus, Shift opens it too.", Category.CLIENT);
         this.getKeybind().set(344);
      }
   }

   public static class HudModule extends Module {
      public final BooleanSetting watermark = (BooleanSetting)this.addSetting(new BooleanSetting("Watermark", "The Sol badge", true));
      public final BooleanSetting arrayList = (BooleanSetting)this.addSetting(new BooleanSetting("ArrayList", "Enabled modules list", true));
      public final BooleanSetting fps = (BooleanSetting)this.addSetting(new BooleanSetting("FPS", "Framerate readout", true));
      public final BooleanSetting ping = (BooleanSetting)this.addSetting(new BooleanSetting("Ping", "Latency readout", false));
      public final BooleanSetting coordinates = (BooleanSetting)this.addSetting(new BooleanSetting("Coordinates", "Block position readout", true));
      public final BooleanSetting direction = (BooleanSetting)this.addSetting(new BooleanSetting("Direction", "Facing readout", true));
      public final BooleanSetting tps = (BooleanSetting)this.addSetting(new BooleanSetting("TPS", "Server tick rate estimate", false));
      public final BooleanSetting cps = (BooleanSetting)this.addSetting(new BooleanSetting("CPS", "Clicks per second", false));
      public final BooleanSetting armor = (BooleanSetting)this.addSetting(new BooleanSetting("Armor", "Equipped armor + durability", false));
      public final BooleanSetting potions = (BooleanSetting)this.addSetting(new BooleanSetting("Potions", "Active effects with timers", false));
      public final BooleanSetting keystrokes = (BooleanSetting)this.addSetting(new BooleanSetting("Keystrokes", "WASD + mouse + space display", false));
      public final BooleanSetting radar = (BooleanSetting)this.addSetting(new BooleanSetting("Radar", "Circular player radar", true));
      public final BooleanSetting themeSync = (BooleanSetting)this.addSetting(new BooleanSetting("List Theme Sync", "ArrayList follows the theme color", true));
      public final ColorSetting listColor = (ColorSetting)this.addSetting(new ColorSetting("List Color", "ArrayList color when Theme Sync is off", -26368));
      public final SliderSetting radarRange = (SliderSetting)this.addSetting(new SliderSetting("Radar Range", "Scan radius in blocks", (double)48.0F, (double)16.0F, (double)128.0F, (double)4.0F, "m"));
      public final BooleanSetting radarHeads = (BooleanSetting)this.addSetting(new BooleanSetting("Radar Heads", "Skin faces instead of dots", true));
      public final BooleanSetting notifications = (BooleanSetting)this.addSetting(new BooleanSetting("Notifications", "Themed toasts when modules toggle & the weather changes", true));
      public final SliderSetting notifyDuration = (SliderSetting)this.addSetting(new SliderSetting("Notify Duration", "How long a toast lingers before fading out", (double)2.5F, (double)1.0F, (double)6.0F, (double)0.5F, "s"));

      public HudModule() {
         super("HUD", "All HUD elements — move & resize via chat (T)", Category.CLIENT);
         this.setEnabled(true);
         SliderSetting var10000 = this.notifyDuration;
         BooleanSetting var10001 = this.notifications;
         Objects.requireNonNull(var10001);
         var10000.visibleWhen(var10001::get);
      }
   }

   public static class Placeholder extends Module {
      public Placeholder(String name, String description, Category category, Setting<?>... settings) {
         super(name, description, category);

         for(Setting<?> setting : settings) {
            this.addSetting(setting);
         }

      }
   }

   public static class SpotifyModule extends Module {
      public final ModeSetting source = (ModeSetting)this.addSetting(new ModeSetting("Source", "Auto uses the real Spotify session (Windows); Demo shows sample data", "Auto", new String[]{"Auto", "Demo"}));
      public final BooleanSetting controls = (BooleanSetting)this.addSetting(new BooleanSetting("Controls", "Show prev / play / next buttons", true));
      public final BooleanSetting volume = (BooleanSetting)this.addSetting(new BooleanSetting("Volume", "Show a slider for Spotify's app volume (Windows)", true));
      public final BooleanSetting hideWhenIdle = (BooleanSetting)this.addSetting(new BooleanSetting("Hide When Idle", "Hide the card when nothing plays", true));

      public SpotifyModule() {
         super("SpotifyHUD", "Now playing — skip and seek from the HUD", Category.CLIENT);
         this.setEnabled(true);
      }
   }

   public static class SusChunkFinderModule extends Module {
      public final SliderSetting sensitivity = (SliderSetting)this.addSetting((new SliderSetting("Sensitivity", "Higher = stricter: more weighted evidence before a chunk flags", (double)3.0F, (double)1.0F, (double)10.0F, (double)1.0F)).withLabel((v) -> (int)v + " (" + (int)v * 5 + ")"));
      public final BooleanSetting amethyst = (BooleanSetting)this.addSetting(new BooleanSetting("Amethyst", "Hidden grown clusters via server block light 5 — the strongest signal", true));
      public final BooleanSetting kelp = (BooleanSetting)this.addSetting(new BooleanSetting("Kelp", "Fully grown / unusually tall kelp columns", true));
      public final BooleanSetting bamboo = (BooleanSetting)this.addSetting(new BooleanSetting("Bamboo", "Fully grown, max-height bamboo", true));
      public final BooleanSetting berries = (BooleanSetting)this.addSetting(new BooleanSetting("Berries", "Sweet berry bushes at max growth stage", true));
      public final BooleanSetting vines = (BooleanSetting)this.addSetting(new BooleanSetting("Vines", "Vines grown far down from their support", true));
      public final BooleanSetting dripstone = (BooleanSetting)this.addSetting(new BooleanSetting("Dripstone", "Dripstone spikes longer than natural generation", true));
      public final SliderSetting scanSpeed = (SliderSetting)this.addSetting(new SliderSetting("Scan Speed", "Chunks scanned per tick", (double)10.0F, (double)2.0F, (double)24.0F, (double)1.0F));
      public final SliderSetting renderY = (SliderSetting)this.addSetting(new SliderSetting("Render Y", "Height the flat chunk highlights render at", (double)100.0F, (double)-64.0F, (double)320.0F, (double)1.0F));
      public final SliderSetting fillOpacity = (SliderSetting)this.addSetting(new SliderSetting("Fill Opacity", "Fill opacity of the chunk quads", (double)90.0F, (double)0.0F, (double)255.0F, (double)1.0F));
      public final BooleanSetting outline = (BooleanSetting)this.addSetting(new BooleanSetting("Outline", "Crisp border along the chunk edges", true));
      public final SliderSetting outlineOpacity = (SliderSetting)this.addSetting(new SliderSetting("Outline Opacity", "Border opacity", (double)200.0F, (double)0.0F, (double)255.0F, (double)1.0F));
      public final BooleanSetting smartMode = (BooleanSetting)this.addSetting(new BooleanSetting("Smart Mode", "Merge nearby flags into zones with a centroid marker", true));
      public final SliderSetting mergeRadius = (SliderSetting)this.addSetting(new SliderSetting("Merge Radius", "Flags within this many chunks merge into one zone", (double)3.0F, (double)1.0F, (double)8.0F, (double)1.0F, " ch"));
      public final BooleanSetting centroidMarker = (BooleanSetting)this.addSetting(new BooleanSetting("Centroid Marker", "Mark each zone's weighted centre — the likely base spot", true));
      public final BooleanSetting showOnRadar = (BooleanSetting)this.addSetting(new BooleanSetting("Show on Radar", "Pulse sus zones on the Radar; far zones clamp to the edge", true));
      public final ModeSetting notifications = (ModeSetting)this.addSetting(new ModeSetting("Notifications", "Announce each new zone once, with coords and distance", "Toast", new String[]{"Toast", "Chat", "Off"}));
      public final SusChunkScanner scanner = new SusChunkScanner(this);

      public SusChunkFinderModule() {
         super("SusChunkFinder", "Finds long-loaded chunks — bases — via amethyst light & growth", Category.RENDER);
         SliderSetting var10000 = this.outlineOpacity;
         BooleanSetting var10001 = this.outline;
         Objects.requireNonNull(var10001);
         var10000.visibleWhen(var10001::get);
      }

      public void onTick() {
         this.scanner.tick();
      }

      protected void onDisable() {
         this.scanner.clear();
         SusChunkRenderer.reset();
      }
   }
}
