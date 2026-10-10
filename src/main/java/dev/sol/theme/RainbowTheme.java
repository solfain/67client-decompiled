package dev.sol.theme;

import dev.sol.util.Colors;

public class RainbowTheme extends Theme {
   public RainbowTheme() {
      super("Rainbow", -26368, false);
   }

   public int accent() {
      double seconds = (double)(System.nanoTime() % 1000000000000L) / (double)1.0E9F;
      return Colors.hsvToRgb((float)(seconds * (double)36.0F % (double)360.0F), 0.72F, 1.0F);
   }
}
