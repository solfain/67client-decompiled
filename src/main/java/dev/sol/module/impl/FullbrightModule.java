package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.SliderSetting;

public class FullbrightModule extends Module {
   public final SliderSetting gamma = (SliderSetting)this.addSetting(new SliderSetting("Gamma", "Brightness boost", (double)12.0F, (double)1.0F, (double)15.0F, (double)1.0F));

   public FullbrightModule() {
      super("FullBright", "Maximum brightness everywhere", Category.RENDER);
   }
}
