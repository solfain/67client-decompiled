package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.SliderSetting;

public class SwingSpeedModule extends Module {
   public final SliderSetting speed = (SliderSetting)this.addSetting(new SliderSetting("Speed", "Swing speed multiplier (<1 = slower/smoother)", 0.3, 0.1, (double)3.0F, 0.1, "x"));

   public SwingSpeedModule() {
      super("SwingSpeed", "Adjusts hand swing animation speed", Category.CLIENT);
   }

   public float multiplier() {
      return this.speed.getFloat();
   }
}
