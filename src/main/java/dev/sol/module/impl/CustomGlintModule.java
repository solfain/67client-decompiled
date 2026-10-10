package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.util.Colors;
import java.util.Locale;

public class CustomGlintModule extends Module {
   public final ModeSetting style = (ModeSetting)this.addSetting(new ModeSetting("Style", "Glint texture. Default recolors the vanilla foil; the rest are custom patterns", "Default", new String[]{"Default", "Ender", "Void", "Galaxy", "Toxic", "Amethyst", "Cyber", "Solar", "Prismatic", "Abyss"}));
   public final ModeSetting mode = (ModeSetting)this.addSetting(new ModeSetting("Mode", "Where the glint color comes from", "Solid", new String[]{"Solid", "Rainbow", "Theme"}));
   public final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", "Glint color", -26368));
   public final SliderSetting strength = (SliderSetting)this.addSetting(new SliderSetting("Strength", "Glint intensity", (double)60.0F, (double)0.0F, (double)100.0F, (double)5.0F, "%"));
   public final SliderSetting speed = (SliderSetting)this.addSetting(new SliderSetting("Speed", "Rainbow cycle speed", (double)100.0F, (double)10.0F, (double)300.0F, (double)10.0F, "%"));

   public CustomGlintModule() {
      super("CustomGlint", "Recolors or restyles the enchantment glint", Category.RENDER);
      this.mode.visibleWhen(() -> this.style.check("Default"));
      this.color.visibleWhen(() -> this.style.check("Default") && this.mode.check("Solid"));
      this.speed.visibleWhen(() -> this.style.check("Default") && this.mode.check("Rainbow"));
   }

   public boolean isActive() {
      return this.isEnabled();
   }

   public boolean usesTexture() {
      return !this.style.check("Default");
   }

   public String textureName() {
      return ((String)this.style.get()).toLowerCase(Locale.ROOT);
   }

   public float strengthUnit() {
      return this.strength.getFloat() / 100.0F;
   }

   public int glintColor() {
      int var10000;
      label17: {
         byte r = -1;
         switch (s) {
            case "Rainbow":
               var10000 = this.rainbow();
               break label17;
            case "Theme":
               var10000 = SolClient.themes().current().accent();
               break label17;
         }

         var10000 = (Integer)this.color.get();
      }

      float s = this.strength.getFloat() / 100.0F;
      int r = Math.round((float)Colors.red(var10000) * s);
      int g = Math.round((float)Colors.green(var10000) * s);
      int b = Math.round((float)Colors.blue(var10000) * s);
      return Colors.rgb(r, g, b);
   }

   private int rainbow() {
      double seconds = (double)(System.nanoTime() % 1000000000000L) / (double)1.0E9F;
      double hue = seconds * (double)36.0F * (double)(this.speed.getFloat() / 100.0F) % (double)360.0F;
      return Colors.hsvToRgb((float)hue, 0.85F, 1.0F);
   }
}
