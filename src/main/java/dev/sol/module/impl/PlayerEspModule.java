package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;

public class PlayerEspModule extends Module {
   public final ModeSetting style = (ModeSetting)this.addSetting(new ModeSetting("Style", "Highlight style", "Outline", new String[]{"Box", "Outline", "Glow"}));
   public final BooleanSetting themeColor = (BooleanSetting)this.addSetting(new BooleanSetting("Theme Color", "Use the theme color", true));
   public final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", "Color when Theme Color is off", -6237441));
   public final BooleanSetting tracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each player", false));

   public PlayerEspModule() {
      super("PlayerESP", "Highlights players through walls", Category.RENDER);
   }
}
