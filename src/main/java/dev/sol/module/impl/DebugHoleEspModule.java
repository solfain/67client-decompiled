package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.render.HoleEspRenderer;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;

public class DebugHoleEspModule extends Module {
   public final ModeSetting depth = (ModeSetting)this.addSetting(new ModeSetting("Depth", "Hole depth check", "2", new String[]{"2", "3", "Any"}));
   public final ColorSetting safe = (ColorSetting)this.addSetting(new ColorSetting("Safe", "Safe hole color", -12654960));
   public final ColorSetting unsafe = (ColorSetting)this.addSetting(new ColorSetting("Unsafe", "Unsafe hole color", -45715));
   public final BooleanSetting tracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each hole", false));

   public DebugHoleEspModule() {
      super("DebugHoleESP", "Marks safe crystal-pvp holes", Category.RENDER);
   }

   public void onTick() {
      HoleEspRenderer.scan(this);
   }

   protected void onDisable() {
      HoleEspRenderer.clear();
   }
}
