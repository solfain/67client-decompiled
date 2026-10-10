package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;

public class ZoomModule extends Module {
   private static final double EASE_SPEED = (double)14.0F;
   public final SliderSetting factor = (SliderSetting)this.addSetting(new SliderSetting("Factor", "Zoom factor", (double)4.0F, (double)2.0F, (double)10.0F, (double)0.5F, "x"));
   public final BooleanSetting smooth = (BooleanSetting)this.addSetting(new BooleanSetting("Smooth", "Smooth zoom in/out", true));
   private double current = (double)1.0F;
   private long lastNanos = 0L;

   public ZoomModule() {
      super("Zoom", "Optical zoom on a key", Category.MISC);
   }

   public double currentFactor() {
      long now = System.nanoTime();
      double dt = this.lastNanos == 0L ? (double)0.0F : (double)(now - this.lastNanos) / (double)1.0E9F;
      this.lastNanos = now;
      double target = this.isEnabled() ? (double)this.factor.getFloat() : (double)1.0F;
      if (!(Boolean)this.smooth.get()) {
         this.current = target;
         return this.current;
      } else {
         double t = (double)1.0F - Math.exp((double)-14.0F * Math.max((double)0.0F, dt));
         this.current += (target - this.current) * t;
         if (Math.abs(this.current - target) < 0.001) {
            this.current = target;
         }

         return this.current;
      }
   }
}
