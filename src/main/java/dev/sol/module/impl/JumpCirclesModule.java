package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_2390;
import net.minecraft.class_5819;
import net.minecraft.class_746;

public class JumpCirclesModule extends Module {
   public final ModeSetting style = (ModeSetting)this.addSetting(new ModeSetting("Style", "Ripple: rings that grow outward. Halo: a spinning ring. Pulse: a disc that flashes twice.", "Ripple", new String[]{"Ripple", "Halo", "Pulse"}));
   public final SliderSetting size = (SliderSetting)this.addSetting(new SliderSetting("Size", "Effect radius in blocks", (double)1.0F, (double)0.5F, (double)3.0F, 0.1, "x"));
   public final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", "Effect color (alpha is animated)", -26368));
   public final SliderSetting lifetime = (SliderSetting)this.addSetting(new SliderSetting("Lifetime", "How long each effect lasts", (double)1.2F, (double)0.4F, (double)4.0F, 0.1, "s"));
   public final SliderSetting rings = (SliderSetting)this.addSetting(new SliderSetting("Rings", "Ripple: number of rings", (double)3.0F, (double)1.0F, (double)5.0F, (double)1.0F));
   public final SliderSetting spin = (SliderSetting)this.addSetting(new SliderSetting("Spin", "Halo: rotation speed", (double)120.0F, (double)0.0F, (double)360.0F, (double)10.0F, "\u00b0/s"));
   public final BooleanSetting rainbow = (BooleanSetting)this.addSetting(new BooleanSetting("Rainbow", "Cycle the color through the rainbow", false));
   public final BooleanSetting particles = (BooleanSetting)this.addSetting(new BooleanSetting("Sparks", "Kick out a flat ring of sparks when you jump", true));
   public final SliderSetting maxCircles = (SliderSetting)this.addSetting(new SliderSetting("Max", "Most effects at once. The oldest goes first", (double)8.0F, (double)1.0F, (double)30.0F, (double)1.0F));
   private final Deque<JumpCircle> circles = new ArrayDeque();
   private int spawnCounter;

   public JumpCirclesModule() {
      super("JumpCircles", "Draws a ground effect under you every time you jump", Category.RENDER);
   }

   public Deque<JumpCircle> circles() {
      return this.circles;
   }

   protected void onDisable() {
      this.clear();
   }

   public void clear() {
      this.circles.clear();
   }

   public int baseRgb() {
      return (Integer)this.color.get() & 16777215;
   }

   public void onPlayerJump(class_746 player) {
      if (this.isEnabled()) {
         double x = player.method_23317();
         double y = player.method_23318();
         double z = player.method_23321();
         float yLift = 0.01F + (float)(this.spawnCounter % 8) * 0.001F;
         ++this.spawnCounter;
         this.circles.addLast(new JumpCircle(x, y, z, player.method_36454(), yLift, System.nanoTime()));
         int max = Math.max(1, this.maxCircles.getInt());

         while(this.circles.size() > max) {
            this.circles.removeFirst();
         }

         if ((Boolean)this.particles.get()) {
            this.spawnParticles(player);
         }
      }

   }

   private void spawnParticles(class_746 player) {
      class_5819 random = player.method_59922();
      int rgb = this.baseRgb();
      int count = 14;
      double offset = random.method_43058() * Math.PI * (double)2.0F;

      for(int i = 0; i < count; ++i) {
         double angle = offset + Math.PI * (double)2.0F * (double)i / (double)count;
         double speed = 0.18 + random.method_43058() * 0.08;
         player.method_73183().method_8406(new class_2390(rgb, 0.75F), player.method_23317() + Math.cos(angle) * 0.25, player.method_23318() + 0.08, player.method_23321() + Math.sin(angle) * 0.25, Math.cos(angle) * speed, 0.02, Math.sin(angle) * speed);
      }

   }

   public final class JumpCircle {
      public double doubleVal;
      public double doubleVal2;
      public double doubleVal3;
      public float yawDegrees;
      public float yLift;
      public long spawnNanos;

      JumpCircle(double x, double y, double z, float yawDegrees, float yLift, long spawnNanos) {
         this.doubleVal = x;
         this.doubleVal2 = y;
         this.doubleVal3 = z;
         this.yawDegrees = yawDegrees;
         this.yLift = yLift;
         this.spawnNanos = spawnNanos;
      }

      public float ageSeconds(long nowNanos) {
         return (float)(nowNanos - this.spawnNanos) / 1.0E9F;
      }
   }
}
