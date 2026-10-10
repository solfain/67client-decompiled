package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.util.Colors;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import net.minecraft.class_310;
import net.minecraft.class_746;

public class CustomAccessoriesModule extends Module {
   public final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", "Base tint for every accessory", -26368));
   public final BooleanSetting rainbow = (BooleanSetting)this.addSetting(new BooleanSetting("Rainbow", "Cycle every accessory through the rainbow", false));
   public final SliderSetting glow = (SliderSetting)this.addSetting(new SliderSetting("Glow", "Soft outer-halo intensity", (double)70.0F, (double)0.0F, (double)100.0F, (double)5.0F, "%"));
   public final BooleanSetting firstPerson = (BooleanSetting)this.addSetting(new BooleanSetting("First Person", "Also show your accessories in first-person view", false));
   public final BooleanSetting cape = (BooleanSetting)this.addSetting(new BooleanSetting("Cape", "A flowing cloth cape down your back", true));
   public final ModeSetting capeStyle = (ModeSetting)this.addSetting(new ModeSetting("Cape Style", "Cape look", "Sol", new String[]{"Sol", "Wave", "Grid", "Solid"}));
   public final BooleanSetting capePhysics = (BooleanSetting)this.addSetting(new BooleanSetting("Cape Physics", "Sway & billow with your movement", true));
   public final BooleanSetting trail = (BooleanSetting)this.addSetting(new BooleanSetting("Trail", "A glowing trail left behind as you move", true));
   public final ModeSetting trailStyle = (ModeSetting)this.addSetting(new ModeSetting("Trail Style", "Trail look", "Ribbon", new String[]{"Ribbon", "Sparkle", "Echo"}));
   public final SliderSetting trailLength = (SliderSetting)this.addSetting(new SliderSetting("Trail Length", "How long the trail lingers", 1.2, 0.2, (double)4.0F, 0.1, "s"));
   public final BooleanSetting aura = (BooleanSetting)this.addSetting(new BooleanSetting("Aura", "An orbiting aura around your feet", false));
   public final ModeSetting auraStyle = (ModeSetting)this.addSetting(new ModeSetting("Aura Style", "Aura look", "Orbit", new String[]{"Orbit", "Ring"}));
   public final BooleanSetting crown = (BooleanSetting)this.addSetting(new BooleanSetting("Crown", "A floating, spinning sun above your head", false));
   private final Deque<TrailNode> trailNodes = new ArrayDeque();
   private static final int MAX_TRAIL = 256;

   public CustomAccessoriesModule() {
      super("CustomAccessories", "Client-side cosmetics — cape, trail, aura & crown", Category.VISUALS);
      ModeSetting var10000 = this.capeStyle;
      BooleanSetting var10001 = this.cape;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::get);
      BooleanSetting var1 = this.capePhysics;
      var10001 = this.cape;
      Objects.requireNonNull(var10001);
      var1.visibleWhen(var10001::get);
      ModeSetting var2 = this.trailStyle;
      var10001 = this.trail;
      Objects.requireNonNull(var10001);
      var2.visibleWhen(var10001::get);
      SliderSetting var3 = this.trailLength;
      var10001 = this.trail;
      Objects.requireNonNull(var10001);
      var3.visibleWhen(var10001::get);
      ModeSetting var4 = this.auraStyle;
      var10001 = this.aura;
      Objects.requireNonNull(var10001);
      var4.visibleWhen(var10001::get);
   }

   public Deque<TrailNode> trailNodes() {
      return this.trailNodes;
   }

   public void onTick() {
      if (!(Boolean)this.trail.get()) {
         if (!this.trailNodes.isEmpty()) {
            this.trailNodes.clear();
         }
      } else {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null) {
            double x = player.method_23317();
            double y = player.method_23318() + (double)player.method_17682() * (double)0.5F;
            double z = player.method_23321();
            this.trailNodes.addLast(new TrailNode(x, y, z, System.nanoTime()));
            long cutoff = System.nanoTime() - (long)((double)Math.max(0.2F, this.trailLength.getFloat()) * (double)1.0E9F);

            while(!this.trailNodes.isEmpty() && ((TrailNode)this.trailNodes.peekFirst()).nanos < cutoff) {
               this.trailNodes.removeFirst();
            }

            while(this.trailNodes.size() > 256) {
               this.trailNodes.removeFirst();
            }
         }
      }

   }

   protected void onDisable() {
      this.clear();
   }

   public void clear() {
      this.trailNodes.clear();
   }

   public int currentRgb() {
      if ((Boolean)this.rainbow.get()) {
         float hue = (float)(System.currentTimeMillis() % 4000L) / 4000.0F * 360.0F;
         return Colors.hsvToRgb(hue, 0.8F, 1.0F) & 16777215;
      } else {
         return (Integer)this.color.get() & 16777215;
      }
   }

   public float glowStrength() {
      return this.glow.getFloat() / 100.0F;
   }

   public final class TrailNode {
      public double doubleVal;
      public double doubleVal2;
      public double doubleVal3;
      public long nanos;

      TrailNode(double x, double y, double z, long nanos) {
         this.doubleVal = x;
         this.doubleVal2 = y;
         this.doubleVal3 = z;
         this.nanos = nanos;
      }

      public float ageSeconds(long now) {
         return (float)(now - this.nanos) / 1.0E9F;
      }
   }
}
