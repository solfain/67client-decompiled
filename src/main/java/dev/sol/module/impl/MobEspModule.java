package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;

public class MobEspModule extends Module {
   public final ColorSetting hostile = (ColorSetting)this.addSetting(new ColorSetting("Hostile", "Hostile color", -45715));
   public final ColorSetting passive = (ColorSetting)this.addSetting(new ColorSetting("Passive", "Passive color", -12654960));
   public final BooleanSetting passiveToo = (BooleanSetting)this.addSetting(new BooleanSetting("Passive Too", "Include passive mobs", false));
   public final BooleanSetting tracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each mob", false));

   public MobEspModule() {
      super("MobESP", "Highlights hostile mobs", Category.RENDER);
   }
}
